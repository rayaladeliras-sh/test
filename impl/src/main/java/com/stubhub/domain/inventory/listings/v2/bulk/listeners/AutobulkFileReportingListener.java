package com.stubhub.domain.inventory.listings.v2.bulk.listeners;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import com.stubhub.domain.inventory.biz.v2.intf.BulkUploadQueueMgr;
import com.stubhub.domain.inventory.datamodel.entity.BulkUploadQueue;
import com.stubhub.domain.inventory.listings.v2.bulk.util.BulkUploadException;
import com.stubhub.newplatform.common.util.DateUtil;

/***
 * Message listener for updating BULK_UPLOAD_QUEUE table with all reporting
 * statistics and also picking next possible queue for processing on SHIP. This
 * listener will ensure that all pending queues except the last uploaded one are
 * ABORTED and last uploaded queue is forwarded to SHIP for processing.
 *
 * @author rkesara
 *
 */
@Component("autobulkFileReportingListener")
@Scope(value = "prototype")
@Lazy(value = true)
@Configurable
public class AutobulkFileReportingListener implements MessageListener {

	private final static Logger log = LoggerFactory.getLogger(AutobulkFileReportingListener.class);

	@Autowired
	private BulkUploadQueueMgr bulkUploadQueueMgr;

	@Autowired
	@Qualifier(value = "autobulkFileProcessingMsgProducer")
	private JmsTemplate autobulkFileProcessingMsgProducer;

	public static final long REQUEST_SECURITY_PASSED = 1200;
	public static final long ABORT = 2300;

	@Override
	public void onMessage(Message message) {
		try {
			MapMessage mapMsg = (MapMessage) message;
			Long queueId = Long.valueOf(mapMsg.getString("queueId"));
			Long sellerId = Long.valueOf(mapMsg.getString("sellerId"));
			String emailAddress = mapMsg.getString("email");
			String userGuid = mapMsg.getString("userguid");
			
			log.info("Entering AutobulkFileReportingListener for queueId={} and sellerId={} for {}", queueId, sellerId, mapMsg);
			BulkUploadQueue bulkUploadQueue = null;
			bulkUploadQueue = bulkUploadQueueMgr.getQueueById(queueId);

			if (bulkUploadQueue != null) {
				bulkUploadQueue.setListingsInFile(Long.valueOf(mapMsg.getString("totalNumOfRecords")));
				bulkUploadQueue.setListingsUnused(Long.valueOf(mapMsg.getString("numOfInvalidRecords")) + Long.valueOf(mapMsg.getString("numOfListingFailures")));
				bulkUploadQueue.setListingsUnchanged(Long.valueOf(mapMsg.getString("numOfUnchangedRecords")));
				bulkUploadQueue.setListingsUnmapped(Long.valueOf(mapMsg.getString("numOfEventMappingFailures")));
				bulkUploadQueue.setListingsAdded(Long.valueOf(mapMsg.getString("numOfListingsCreated")));
				bulkUploadQueue.setListingsModified(Long.valueOf(mapMsg.getString("numOfListingsUpdated")));
				bulkUploadQueue.setListingsDeleted(Long.valueOf(mapMsg.getString("numOfListingsDeleted")));
				bulkUploadQueue.setDateEnded(DateUtil.getNowCalUTC());
				bulkUploadQueue.setLastUpdatedDate(DateUtil.getNowCalUTC());
				bulkUploadQueue.setQueueStatusId(Long.valueOf(mapMsg.getString("fileStatusId")));
				log.info("Successfully updating queueId={} with {}", queueId, bulkUploadQueue);
				// update bulkuploadqueue
				bulkUploadQueueMgr.updateBulkUploadQueue(bulkUploadQueue);
			}

			// Check if there are any queues with "Request Security Passed -
			// 1200" status
			List<BulkUploadQueue> pendingQueues = bulkUploadQueueMgr.getQueuesByStatusAndSellerId(sellerId, REQUEST_SECURITY_PASSED);
			if (pendingQueues == null || pendingQueues.isEmpty()) {
				log.info("There are no pending or waiting queue after queueId={} for sellerId={}", queueId, sellerId);
			} else {
				bulkUploadQueue = pendingQueues.get(0);
				queueId = bulkUploadQueue.getQueueId();

				if (pendingQueues.size() > 1) {
					// ABORT all other queues
					for (int i = 1; i < pendingQueues.size(); i++) {
						BulkUploadQueue queue = pendingQueues.get(i);
						queue.setDateEnded(DateUtil.getNowCalUTC());
						queue.setLastUpdatedDate(DateUtil.getNowCalUTC());
						queue.setQueueStatusId(ABORT);
						log.info("Aborting queueId={} for sellerId={} since queueId={} is the latest queue for processing.", new Object[] { queue.getQueueId(), sellerId, queueId });
						// update bulkuploadqueue
						bulkUploadQueueMgr.updateBulkUploadQueue(queue);
					}
				}

				log.info("Pushing queueId={} for sellerId={} into ship processing queue...", queueId, sellerId);
				// send message to ship processing queue
				sendQueueForProcessing(bulkUploadQueue, emailAddress, userGuid);
			}

		} catch (BulkUploadException e) {
			log.error("BulkUploadException onMessage", e);
		} catch (Exception e) {
			log.error("Exception onMessage", e);
		}

	}
	
	void sendQueueForProcessing(final BulkUploadQueue bulkUploadQueue, final String emailAddress, final String userGuid) throws BulkUploadException {
		try {
			autobulkFileProcessingMsgProducer.send(new MessageCreator() {
				public javax.jms.Message createMessage(javax.jms.Session session) throws JMSException {
					javax.jms.MapMessage message = session.createMapMessage();
					message.setString("queueId", String.valueOf(bulkUploadQueue.getQueueId().longValue()));
					message.setString("sellerId", String.valueOf(bulkUploadQueue.getUserId().longValue()));
					message.setString("filepath", bulkUploadQueue.getFileOriginal());
					message.setString("fileformat", bulkUploadQueue.getFileSource());
					message.setString("filetype",bulkUploadQueue.getFileType());
					message.setString("email",emailAddress);
					message.setString("userguid",userGuid);
					return message;
				}
			});

		} catch (Exception e) {
			log.error("Exception in dropMessagetoAblQueue", e);
			throw new BulkUploadException("Exception in dropMessagetoAblQueue", e);
		}

	}

}
