package com.stubhub.domain.inventory.listings.v2.listeners;

import java.util.List;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.util.JMSMessageHelper;

/**
 * 
 * Listener to identify all pending flash listings related to a seller and
 * accordingly predeliver them by pushing them to SHIP lock queue.
 *
 */
public class ProcessPendingFlashListingsListener implements MessageListener {

	private final static Logger logger = LoggerFactory.getLogger(ProcessPendingFlashListingsListener.class);
	private static final String LOG_PREFIX = "_domain=inventory";

	@Autowired
	private JMSMessageHelper jmsMessageHelper;

	@Autowired
	private InventoryMgr inventoryMgr;

	@Override
	public void onMessage(Message message) {
		Long sellerId = null;
		SHMonitor monitor = SHMonitorFactory.getMonitor().start();

		try {
			MapMessage mapMessage = (MapMessage) message;
			sellerId = mapMessage.getLong("sellerId");
			logger.info("{} {} _operation=processPendingFlashListingsForSeller _status=START sellerId={}",
					new Object[] { SHMonitoringContext.get(), LOG_PREFIX, sellerId });

			if (sellerId != null) {
				List<Listing> pendingLockListings = inventoryMgr.getAllPendingFlashListings(sellerId);

				if (!CollectionUtils.isEmpty(pendingLockListings)) {
					monitor.stop();
					logger.info(
							"{} {} _operation=processPendingFlashListingsForSeller _message=\"{}\" _status=SUCCESS sellerId={} noOfListings={} _respTime={}",
							new Object[] { SHMonitoringContext.get(), LOG_PREFIX,
									"Dropping listingIds to Lock Inventory Queue", sellerId,
									pendingLockListings.size(), monitor.getTime() });
					for (Listing listing : pendingLockListings) {
						jmsMessageHelper.sendLockInventoryMessage(listing.getId());
					}
				} else {
					monitor.stop();
					logger.info(
							"{} {} _operation=processPendingFlashListingsForSeller _message=\"{}\" _status=SUCCESS sellerId={} _respTime={}",
							new Object[] { SHMonitoringContext.get(), LOG_PREFIX,
									"No PENDING flash listings found for the seller", sellerId, monitor.getTime() });
					return;
				}
			}

			monitor.stop();
			logger.error("{} {} operation=processPendingFlashListingsForSeller _message=\"{}\" _status=ERROR _respTime={}",
					new Object[] { SHMonitoringContext.get(), LOG_PREFIX, "SellerId is null", monitor.getTime() });
			return;
		} catch (Exception e) {
			monitor.stop();
			logger.error(
					"{} {} _operation=processPendingFlashListingsForSeller _message=\"{}\" _status=ERROR sellerId={} _respTime={}",
					new Object[] { SHMonitoringContext.get(), LOG_PREFIX, e.getMessage(), sellerId,
							monitor.getTime() });
		}

	}

}
