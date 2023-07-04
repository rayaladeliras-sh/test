package com.stubhub.domain.inventory.listings.v2.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import com.stubhub.domain.infrastructure.config.client.core.SHConfig;
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
import com.stubhub.domain.inventory.listings.v2.enums.DeliveryOptionEnum;
import com.stubhub.domain.inventory.listings.v2.util.JMSMessageHelper;
import com.stubhub.domain.inventory.listings.v2.util.PartnerIntegrationHelper;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.domain.inventory.v2.enums.TicketMedium;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.UnlockInventoryRequest;
import com.stubhub.domain.user.contactsV2.intf.CustomerContactV2Details;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;

/**
 * Unlock Inventory Request Listener.
 * 
 * @author rkesara
 *
 */
public class UnlockInventoryRequestListener implements MessageListener {

  private final static Logger logger =
      LoggerFactory.getLogger(UnlockInventoryRequestListener.class);

  private static final String LOG_PREFIX = "_domain=inventory _operation=lock _type=request";
  private static final String INVALID_LISTING =
      "Either the listing is invalid or no tickets available in this listing to perform unlock";
  private static final String NON_INTEGRATED_PARTNER_MSG =
      "Partner is not integrated on SHIP, hence pushing unlock message to Gen3 Queue";
  private static final String INTEGRATED_PARTNER_MSG =
      "Partner is integrated on SHIP, hence pushing unlock message to Partner UnlockRequest Queue";
  private static final String INVALID_DO_TM =
      "Listing DeliveryOption or TicketMedium is not suitable for Unlock";


  @Autowired
  private IntegrationManager integrationManager;

  @Autowired
  private UserHelper userHelper;

  @Autowired
  private JMSMessageHelper jmsMessageHelper;

  @Autowired
  private PartnerIntegrationHelper partnerIntegrationHelper;

//  @Autowired
//  private MasterStubhubPropertiesWrapper masterStubhubProperties;

  @Autowired
  private SHConfig shConfig;

  private static final String LOG_PREFIX_TEMPLATE =
      "{} {} _operation=unlockInventoryRequest _message=\"{}\" _status=BAD_REQUEST listingId={} sellerId={} _respTime={}";
  private static final String UNLOCK_EXCLUDED_USER = "unlock.excluded.users";
  private static final String UNIFY_FLOW_ENABLED =
      "ship.secondary.integration.unify.ini.flow.enabled";

  @Override
  public void onMessage(Message message) {
    SHMonitor monitor = SHMonitorFactory.getMonitor().start();
    Long listingId = null;
    String sellerGuid = null;
    boolean forceUnlock = false;

    try {
      MapMessage mapMessage = (MapMessage) message;
      listingId = mapMessage.getLong("listingId");
      
      if(mapMessage.itemExists("forceUnlock")) {
        forceUnlock = mapMessage.getBoolean("forceUnlock");
      }

      List<Long> unlockTicketSeatIds = null;

      if(mapMessage.itemExists("ticketSeatIds")) {
        unlockTicketSeatIds = new ArrayList<>();
        String[] ticketSeatIds = mapMessage.getString("ticketSeatIds").split(",");
        for (String ticketSeatId : ticketSeatIds) {
          unlockTicketSeatIds.add(Long.parseLong(ticketSeatId));
        }
      }

      logger.info("{} {} _status=START listingId={} unlockTicketSeatIds={}",
              SHMonitoringContext.get(), LOG_PREFIX, listingId, unlockTicketSeatIds);

      /* Step-1: Fetch Listing Details by listingId - filter AVAILABLE & REMOVED seats only */
      Listing listing = integrationManager.getListing(listingId, unlockTicketSeatIds);

      if (listing == null || listing.getTicketSeats() == null) {
        monitor.stop();
        logger.error("{} {} _message=\"{}\" _status=BAD_REQUEST listingId={} _respTime={}",
            new Object[] {SHMonitoringContext.get(), LOG_PREFIX, INVALID_LISTING, listingId,
                monitor.getTime()});
        return;
      }

      Long sellerId = listing.getSellerId();
      if (isUnlockIgnoredForUser(sellerId)) {
        monitor.stop();
        logger.info(LOG_PREFIX_TEMPLATE, SHMonitoringContext.get(), LOG_PREFIX,
            "Unlock inventory is ignored for this primary seller", listingId, sellerId,
            monitor.getTime());
        return;
      }

      boolean unifyFlow = BooleanUtils.toBoolean(shConfig.getProperty(UNIFY_FLOW_ENABLED));
      logger.info("{}  _message=\"{}\" Drop listingId={} to INI for unlock, unifyFlow={}", new Object[] {SHMonitoringContext.get(), LOG_PREFIX, listingId, unifyFlow});
      if (unifyFlow) {
        if (!TicketMedium.FLASHSEAT.getId().equals(listing.getTicketMedium())
            && !partnerIntegrationHelper.isPartnerIntegratedOnSHIP(listing.getEventId())) {
          monitor.stop();
          logger.info("{} {} _message=\"{}\" _status=SUCCESS listingId={} _respTime={}",
              new Object[] {SHMonitoringContext.get(), LOG_PREFIX, NON_INTEGRATED_PARTNER_MSG,
                  listingId, monitor.getTime()});
          jmsMessageHelper.sendUnlockBarcodeMessage(listingId);
          return;
        } else {
          logger.info("{} {} _message=\"{}\" listingId={}", new Object[] {SHMonitoringContext.get(),
              LOG_PREFIX, INTEGRATED_PARTNER_MSG, listingId});
        }
      }
      if (listing.getDeliveryOption() == null
          || listing.getDeliveryOption()
              .intValue() != (int) DeliveryOptionEnum.PRE_DELIVERY.getDeliveryOption()
          || !(TicketMedium.BARCODE.getId().equals(listing.getTicketMedium())
              || TicketMedium.FLASHSEAT.getId().equals(listing.getTicketMedium()))) {
        monitor.stop();
        logger.error(
            "{} {} _message=\"{}\" _status=BAD_REQUEST listingId={} deliverOption={} ticketMedium={} _respTime={}",
            new Object[] {SHMonitoringContext.get(), LOG_PREFIX, INVALID_DO_TM, listingId,
                listing.getDeliveryOption(), listing.getTicketMedium(), monitor.getTime()});
        return;
      }

      sellerGuid = integrationManager.getUserGuid(listing.getSellerId());
      if (StringUtils.isBlank(sellerGuid)) {
        monitor.stop();
        logger.error(
            "{} {} _message=\"{}\" _status=BAD_REQUEST listingId={} sellerId={} _respTime={}",
            new Object[] {SHMonitoringContext.get(), LOG_PREFIX, "Unable to lookup userGuid...",
                listingId, listing.getSellerId(), monitor.getTime()});
        return;
      }

      CustomerContactV2Details sellerContact =
          userHelper.getDefaultCustomerContactV2(sellerGuid, true);
      if (sellerContact == null) {
        monitor.stop();
        logger.error(
            "{} {} _message=\"{}\" _status=BAD_REQUEST listingId={} sellerId={} sellerGuid={} _respTime={}",
            new Object[] {SHMonitoringContext.get(), LOG_PREFIX,
                "Unable to lookup seller contact details", listingId, listing.getSellerId(),
                sellerGuid, monitor.getTime()});
        return;
      }

      UnlockInventoryRequest unlockInventoryRequest =
          integrationManager.createUnlockInventoryRequest(listing, sellerContact, unlockTicketSeatIds);
      if (unlockInventoryRequest == null || unlockInventoryRequest.getListing() == null
          || unlockInventoryRequest.getListing().getProducts() == null
          || unlockInventoryRequest.getListing().getProducts().isEmpty()) {
        monitor.stop();
        logger.error(
            "{} {} _message=\"{}\" _status=BAD_REQUEST listingId={} sellerId={} _respTime={}",
            SHMonitoringContext.get(), LOG_PREFIX, "There is no seats to unlock", listingId,
            listing.getSellerId(), monitor.getTime());
        return;
      }

      jmsMessageHelper.sendPartnerUnlockInventoryMessage(unlockInventoryRequest, forceUnlock);
      monitor.stop();
      // send partner unlock inventory request message
      logger.info("{} {} _message=\"{}\" _status=SUCCESS listingId={} {} _respTime={}",
          new Object[] {SHMonitoringContext.get(), LOG_PREFIX,
              "Successfully sent PartnerUnlockInventoryRequest message...", listingId,
              unlockInventoryRequest, monitor.getTime()});
    } catch (Exception e) {
      monitor.stop();
      logger.error("{} {} _message=\"{}\" _status=ERROR listingId={} _respTime={}",
          new Object[] {SHMonitoringContext.get(), LOG_PREFIX, e, listingId, monitor.getTime()});
    }
  }

  private boolean isUnlockIgnoredForUser(Long sellerId) {
    String userList = shConfig.getProperty(UNLOCK_EXCLUDED_USER, "90006751");
    List<String> result = Arrays.asList(userList.split("\\s*,\\s*"));
    boolean ignoreUnlock = result.contains(sellerId.toString());
    logger.debug(
        "{} {} _operation=unlockInventory _message=\"UNLOCK_EXCLUDED_USER\" list={} ignoreUnlock={}",
        SHMonitoringContext.get(), LOG_PREFIX, Arrays.toString(result.toArray()), ignoreUnlock);
    return ignoreUnlock;

  }

}
