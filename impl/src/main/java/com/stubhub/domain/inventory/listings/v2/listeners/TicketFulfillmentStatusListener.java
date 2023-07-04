package com.stubhub.domain.inventory.listings.v2.listeners;

import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * Created by jicui on 11/2/15.
 */
@Component("ticketFulfillmentStatusListener")
@Scope
@Configurable
public class TicketFulfillmentStatusListener implements MessageListener {
    private final static Logger log = LoggerFactory.getLogger(TicketFulfillmentStatusListener.class);

    private static final String PENDING_PDF_REVIEW="1";

    @Autowired
    private InventoryMgr inventoryMgr;

    @Override
    public void onMessage(Message message) {
        try {
            MapMessage map=(MapMessage)message;
            Long ticketId = map.getLong("ticketId");
            String statusId=map.getString("inBoundStatus");
            Listing listing=inventoryMgr.getListing(Long.valueOf(ticketId));
            //only update for PDF case for now
            if(statusId.equals(PENDING_PDF_REVIEW)){
                if(listing.getSystemStatus().equals(ListingStatus.ACTIVE.toString())){//only update when the ticket is active
                    listing.setSystemStatus(ListingStatus.PENDING_PDF_REVIEW.toString());
                    inventoryMgr.updateListingOnly(listing);
                    log.info("api_domain=Inventory api_resource=ticketFulfillmentListener api_message=\"ticket fulfillment status update\" listingId={} statusId={}",ticketId,statusId);
                }
                log.info("api_domain=Inventory api_resource=ticketFulfillmentListener api_message=\"listing can not be updated as not in active status\" listingId={} statusId={}",ticketId,statusId);
            }
        } catch (Exception e) {
            log.error("api_domain=Inventory api_resource=ticketFulfillmentListener errorMsg={} map={}",e,message);
        }
    }
}
