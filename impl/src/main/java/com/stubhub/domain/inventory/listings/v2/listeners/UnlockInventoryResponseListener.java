package com.stubhub.domain.inventory.listings.v2.listeners;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.biz.v2.intf.IntegrationManager;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.UnlockInventoryResponse;

/**
 * Unlock Inventory Response Listener.
 * 
 * @author rkesara
 *
 */
public class UnlockInventoryResponseListener implements MessageListener {
  private final static Logger logger =
      LoggerFactory.getLogger(UnlockInventoryResponseListener.class);

  private static final String LOG_PREFIX = "_domain=inventory _operation=unlock _type=response";

  @Autowired
  private IntegrationManager integrationManager;

  @Override
  public void onMessage(Message message) {

    SHMonitor monitor = SHMonitorFactory.getMonitor().start();

    try {
      MapMessage mapMessage = (MapMessage) message;
      String response = mapMessage.getString("payload");

      if (StringUtils.isBlank(response)) {
        monitor.stop();
        logger.error("{} {} _message=\"{}\" _status=BAD_REQUEST _respTime={}",
            new Object[] {SHMonitoringContext.get(), LOG_PREFIX,
                "Unable to find response in message...", monitor.getTime()});
        return;
      }

      // map response to LockInventoryResponse
      UnlockInventoryResponse unlockInventoryResponse = null;
      try {
        final ObjectMapper mapper = new ObjectMapper();
        unlockInventoryResponse = mapper.readValue(response, UnlockInventoryResponse.class);
      } catch (Exception e) {
        monitor.stop();
        logger.error("{} {} _message=\"{}\" _status=ERROR response={} _respTime={}",
            new Object[] {SHMonitoringContext.get(), LOG_PREFIX,
                "Unable to transform response in message...", response, monitor.getTime()},
            e);
        return;
      }

      if (unlockInventoryResponse == null || unlockInventoryResponse.getListing() == null) {
        monitor.stop();
        logger.error("{} {} _message=\"{}\" _status=BAD_REQUEST _respTime={}",
            new Object[] {SHMonitoringContext.get(), LOG_PREFIX,
                "Unable to find response in message...", monitor.getTime()});
        return;
      }

      // update status field in tickets to indicate unlock completion
      integrationManager.updateTicketStatus(unlockInventoryResponse.getListing().getId(), (short)0);
    } catch (Exception e) {
      monitor.stop();
      logger.error("{} {} _message=\"{}\" _status=ERROR _respTime={}",
          new Object[] {SHMonitoringContext.get(), LOG_PREFIX, e.getMessage(), monitor.getTime()},
          e);
    }

  }

}
