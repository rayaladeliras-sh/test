/**
 * 
 */
package com.stubhub.domain.inventory.listings.v2.bulk.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

/**
 * @author sjayaswal
 * 
 */
@Component("bulkListingJobConsumer")
@Scope(value = "prototype")
@Lazy(value = true)
@Configurable
public class BulkListingJobConsumer implements Runnable {

	@Autowired
	private BulkListingHelper bulkListingHelper;

	@Autowired
	private BulkListingJobProducer bulkListingJobProducer;

	private final static Logger log = Logger
			.getLogger(BulkListingJobConsumer.class);

	public void processGroup(Map message) {
		try {
			if(log.isDebugEnabled())
				log.debug("received message from MQ message=" + message);
			String hostName = InetAddress.getLocalHost().getHostName() + "-"
					+ Thread.currentThread().getName();
			Long bulkListingGroupId =(Long) message.get("groupId");
			bulkListingHelper.processListingRequestByGroupId(
					bulkListingGroupId, hostName, false);
			if(log.isDebugEnabled())
				log.debug("completed Processing groupId=" + bulkListingGroupId);
		} catch (Exception e) {
			//TODO - add the group ID if available
			log.error("Unknown system error on BulkListingJobConsumer.", e);
		}
	}

	public void run() {

		while (true) {
			try {
				String hostName = InetAddress.getLocalHost().getHostName()
						+ "-" + Thread.currentThread().getName();
				Long bulkListingGroupId = bulkListingJobProducer.getGroup();
				log.info("Processing bulkListingGroupId=" + bulkListingGroupId
						+ " on Hostname=" + hostName + " and ThreadName="
						+ Thread.currentThread().getName());
				bulkListingHelper.processListingRequestByGroupId(
						bulkListingGroupId, hostName, false);
				log.debug("completed Processing groupId=" + bulkListingGroupId);
			} catch (InterruptedException e) {
				log.error("Interrupted Exception from BulkListingJobConsumer",
						e);
			} catch (UnknownHostException e) {
				log.error("Unknown Host Exception from BulkListingJobConsumer",
						e);
			} catch (HibernateOptimisticLockingFailureException ssoe) {
				log.error("HibernateOptimisticLockingFailureException thrown since the group might be already processed. "
						+ ssoe.getMessage());
			} catch (Throwable t) {
				log.error("Unknown system error on BulkListingJobConsumer.", t);
			}
		}
	}
}
