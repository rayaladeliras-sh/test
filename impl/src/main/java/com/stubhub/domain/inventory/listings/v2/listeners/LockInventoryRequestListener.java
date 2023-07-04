package com.stubhub.domain.inventory.listings.v2.listeners;

import java.util.ArrayList;
import java.util.List;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.biz.v2.intf.IntegrationManager;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.enums.DeliveryOptionEnum;
import com.stubhub.domain.inventory.listings.v2.util.JMSMessageHelper;
import com.stubhub.domain.inventory.listings.v2.util.PartnerIntegrationHelper;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.domain.inventory.v2.enums.TicketMedium;
import com.stubhub.domain.inventory.v2.enums.TicketSeatStatusEnum;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.Attribute;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.LockInventoryRequest;
import com.stubhub.domain.user.contactsV2.intf.CustomerContactV2Details;
import com.stubhub.newplatform.common.util.DateUtil;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;

/**
 * Lock Inventory Request Listener.
 * 
 * @author rkesara
 *
 */
public class LockInventoryRequestListener implements MessageListener {
  private final static Logger logger = LoggerFactory.getLogger(LockInventoryRequestListener.class);

  private static final String LOG_PREFIX = "_domain=inventory _operation=lock _type=request";
  private static final String INVALID_LISTING =
      "Either the listing is invalid or no tickets available in this listing to perform lock";
  private static final String NON_INTEGRATED_PARTNER_MSG =
      "Partner is not integrated on SHIP, hence pushing lock message to Gen3 Queue";
  private static final String INTEGRATED_PARTNER_MSG =
      "Partner is integrated on SHIP, hence pushing lock message to Partner LockRequest Queue";
  private static final String INVALID_DO_TM =
      "Listing DeliveryOption or TicketMedium is not suitable for Predelivery";
  private static final String UNIFY_FLOW_ENABLED =
      "ship.secondary.integration.unify.ini.flow.enabled";

  @Autowired
  private IntegrationManager integrationManager;

  @Autowired
  private UserHelper userHelper;

  @Autowired
  private JMSMessageHelper jmsMessageHelper;

  @Autowired
  private PartnerIntegrationHelper partnerIntegrationHelper;

  @Autowired
  private MasterStubhubPropertiesWrapper masterStubhubPropertiesWrapper;

  @Override
  public void onMessage(Message message) {

    SHMonitor monitor = SHMonitorFactory.getMonitor().start();
    Long listingId = null;
    String sellerGuid = null;
    boolean isTransfer = false;
    boolean isCallback = false;
    String callbackPayload = null;
    boolean forceLock = false;
   

    try {
      MapMessage mapMessage = (MapMessage) message;
      listingId = mapMessage.getLong("listingId");
      if (mapMessage.itemExists("transfer")) {
        isTransfer = mapMessage.getBoolean("transfer");
      }
      if (mapMessage.itemExists("isCallback")) {
        isCallback = mapMessage.getBoolean("isCallback");
        callbackPayload = mapMessage.getString("callbackPayload");
      }
      if(mapMessage.itemExists("forceLock")) {
        forceLock = mapMessage.getBoolean("forceLock");
      }
    

      logger.info("{} {} _status=START listingId={} isTransfer={} isCallback={} forceLock={}",
          new Object[] {SHMonitoringContext.get(), LOG_PREFIX, listingId, isTransfer, isCallback, forceLock});

      /* Step-1: Fetch Listing Details by listingId - filter AVAILABLE & REMOVED seats only */
      Listing listing = integrationManager.getListing(listingId);
      if (listing == null || listing.getTicketSeats() == null) {
        monitor.stop();
        logger.error(
            "{} {} _message=\"{}\" _status=BAD_REQUEST listingId={} isTransfer={} isCallback={} _respTime={}",
            new Object[] {SHMonitoringContext.get(), LOG_PREFIX, INVALID_LISTING, listingId,
                isTransfer, isCallback, monitor.getTime()});
        return;
      }
      //Updating ticket status to 4 if it is partial seat removal
      boolean hasRemovedSeats = false;
      for (TicketSeat ticketSeat : listing.getTicketSeats()) {
        if (TicketSeatStatusEnum.REMOVED.getCode().intValue() == ticketSeat.getSeatStatusId()
            .intValue()) {
          hasRemovedSeats = true;
          // update status field in tickets to indicate unlock completion
          integrationManager.updateTicketStatus(listingId, (short)4);
          break;
        }
      }
      boolean unifyFlow = BooleanUtils.toBoolean(masterStubhubPropertiesWrapper.getProperty(UNIFY_FLOW_ENABLED));
      logger.info("{}  _message=\"{}\" Drop listingId={} to INI for lock if unifyFlow={}", new Object[] {SHMonitoringContext.get(), LOG_PREFIX, listingId, unifyFlow});
      if (!unifyFlow) {
        if (!TicketMedium.FLASHSEAT.getId().equals(listing.getTicketMedium())
            && !partnerIntegrationHelper.isPartnerIntegratedOnSHIP(listing.getEventId())) {
          monitor.stop();
          logger.info(
              "{} {} _message=\"{}\" _status=SUCCESS listingId={} isTransfer={} isCallback={} _respTime={}",
              new Object[] {SHMonitoringContext.get(), LOG_PREFIX, NON_INTEGRATED_PARTNER_MSG,
                  listingId, isTransfer, isCallback, monitor.getTime()});
          jmsMessageHelper.sendLockMessage(listingId);
          return;
        } else {
          logger.info("{} {} _message=\"{}\" listingId={} isTransfer={} isCallback={}",
              new Object[] {SHMonitoringContext.get(), LOG_PREFIX, INTEGRATED_PARTNER_MSG,
                  listingId, isTransfer, isCallback});
        }
      }

      if (listing.getDeliveryOption() == null
          || (listing.getDeliveryOption()
              .intValue() != (int) DeliveryOptionEnum.PRE_DELIVERY.getDeliveryOption()
              && TicketMedium.BARCODE.getId().equals(listing.getTicketMedium()))
          || !(TicketMedium.BARCODE.getId().equals(listing.getTicketMedium())
              || TicketMedium.FLASHSEAT.getId().equals(listing.getTicketMedium()))) {
        monitor.stop();
        logger.error(
            "{} {} _message=\"{}\" _status=BAD_REQUEST listingId={} isTransfer={} isCallback={} deliveryOption={} ticketMedium={} _respTime={}",
            new Object[] {SHMonitoringContext.get(), LOG_PREFIX, INVALID_DO_TM, listingId,
                isTransfer, isCallback, listing.getDeliveryOption(), listing.getTicketMedium(),
                monitor.getTime()});
        return;
      }

      sellerGuid = integrationManager.getUserGuid(listing.getSellerId());
      if (StringUtils.isBlank(sellerGuid)) {
        monitor.stop();
        logger.error(
            "{} {} _message=\"{}\" _status=BAD_REQUEST listingId={} sellerId={} isTransfer={} isCallback={} _respTime={}",
            new Object[] {SHMonitoringContext.get(), LOG_PREFIX, "Unable to lookup userGuid...",
                listingId, listing.getSellerId(), isTransfer, isCallback, monitor.getTime()});
        return;
      }

      CustomerContactV2Details sellerContact =
          userHelper.getDefaultCustomerContactV2(sellerGuid, true);
      if (sellerContact == null) {
        monitor.stop();
        logger.error(
            "{} {} _message=\"{}\" _status=BAD_REQUEST listingId={} sellerId={} sellerGuid={} isTransfer={} isCallback={} _respTime={}",
            new Object[] {SHMonitoringContext.get(), LOG_PREFIX,
                "Unable to lookup seller contact details", listingId, listing.getSellerId(),
                sellerGuid, isTransfer, isCallback, monitor.getTime()});
        return;
      }

      LockInventoryRequest lockInventoryRequest =
          integrationManager.createLockInventoryRequest(listing, sellerContact);
      if (lockInventoryRequest == null || lockInventoryRequest.getListing() == null) {
        monitor.stop();
        logger.error(
            "{} {} _message=\"{}\" _status=BAD_REQUEST listingId={} sellerId={} isTransfer={} isCallback={} _respTime={}",
            new Object[] {SHMonitoringContext.get(), LOG_PREFIX,
                "Unable to create LockInventoryRequest", listingId, listing.getSellerId(),
                isTransfer, isCallback, monitor.getTime()});
        return;
      }
      // we need to populate seatMap for transfer usecase
      if (isTransfer && lockInventoryRequest.getListing() != null) {
        String seatMap = mapMessage.getString("seatMap");
        logger.info("{} {} _message=\"{}\" listingId={} isTransfer={} seatMap='{}'",
            new Object[] {SHMonitoringContext.get(), LOG_PREFIX,
                "Identified as Transfer usecase and hence enriching LockInventoryRequest",
                listingId, isTransfer, seatMap});
        List<Attribute> attributes = lockInventoryRequest.getListing().getAttributes();
        if (attributes == null) {
          attributes = new ArrayList<>();
        }
        attributes.add(new Attribute("transfer", String.valueOf(isTransfer)));
        attributes.add(new Attribute("seatMap", seatMap));
        lockInventoryRequest.getListing().setAttributes(attributes);
      }

      if (hasRemovedSeats || DateUtil.getNowCalUTC().after(listing.getEndDate())) {
        logger.info("{} {} _message=\"{}\" listingId={}",
            new Object[] {SHMonitoringContext.get(), LOG_PREFIX,
                "Identified that there are some seats in removed state OR listing has expired..pushing to unlock queue for checking",
                listingId});
        jmsMessageHelper.sendUnlockInventoryMessage(listingId);
      }
      
      String memberIds = null;
      if(mapMessage.itemExists("memberIds"))
  	  	memberIds = mapMessage.getString("memberIds");
      
      // send partner lock inventory request message
    	  jmsMessageHelper.sendPartnerLockInventoryMessage(lockInventoryRequest, isCallback, callbackPayload,
  	  			forceLock, memberIds);
      
      monitor.stop();
      logger.info(
          "{} {} _message=\"{}\" _status=SUCCESS listingId={} isTransfer={} forceLock={} {} memberIds={} _respTime={}",
          new Object[] {SHMonitoringContext.get(), LOG_PREFIX,
              "Successfully sent PartnerLockInventoryRequest message...", listingId, isTransfer, forceLock,
              lockInventoryRequest, memberIds, monitor.getTime()});
    } catch (Exception e) {
      monitor.stop(); 
      logger.error("{} {} _message=\"{}\" _status=ERROR listingId={} isTransfer={} _respTime={}",
          new Object[] {SHMonitoringContext.get(), LOG_PREFIX, e.getMessage(), listingId,
              isTransfer, monitor.getTime()},
          e);
    }
  }

}
