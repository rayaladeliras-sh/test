package com.stubhub.domain.inventory.listings.v2.newflow.handler;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.UpdateListingEnum;
import com.stubhub.domain.inventory.listings.v2.util.JMSMessageHelper;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;

@Component
@Scope("prototype")
public class UpdateListingHandler extends DefaultBusinessFlowHandler {
  private final static Logger log = LoggerFactory.getLogger(UpdateListingHandler.class);
  @Autowired
  private UpdateListingTaskFactory updateListingTaskFactory;
  @Autowired
  private JMSMessageHelper jmsMessageHelper;

  private List<UpdateListingEnum> updateAttributeList;

  public UpdateListingHandler(ListingDTO listingDTO, List<UpdateListingEnum> updateAttributeList) {
    super(listingDTO);
    this.updateAttributeList = new ArrayList<UpdateListingEnum>(updateAttributeList);
  }

  @Override
  public ListingResponse execute() {
    Listing listing = listingDTO.getDbListing();
    if (updateAttributeList != null && !updateAttributeList.isEmpty()) {
      // Add all the tasks
      for (UpdateListingEnum updateListingEnum : updateAttributeList) {
        addRegularTasks(updateListingTaskFactory.getTasks(updateListingEnum, listingDTO));
      }

      runTasks();

      // Call Manager to create DB Listing
      listing = persistData(listing);
      
      // Unlock if the listing is deleted
      unlockInventory(listing);
      
	  /*SELLAPI-3243: Check if the Update Attribute list has Delete seats or Update Quantity)*/
      if(ListingStatus.PENDING_LOCK.toString().equals(listing.getSystemStatus()) &&
    		  needToSendLockMessage()) {
        sendMessages(listing.getId());
      }
    }

    // Generate Response
    ListingResponse response = generateResponse(listing);

    return response;
  }

  private boolean needToSendLockMessage() {
    return (updateAttributeList.contains(UpdateListingEnum.DELETE_SEATS)
        || updateAttributeList.contains(UpdateListingEnum.QUANTITY)
        || updateAttributeList.contains(UpdateListingEnum.TICKET_TRAITS));
  }

  private void unlockInventory(Listing listing){
    if (ListingStatus.DELETED.toString().equals(listing.getSystemStatus())) {
      if ((TicketMedium.BARCODE.getValue() == listing.getTicketMedium()
          || TicketMedium.FLASHSEAT.getValue() == listing.getTicketMedium())
          && DeliveryOption.PREDELIVERY.getValue() == listing.getDeliveryOption()) {
        log.debug(
            "message=\"Barcode/Flashseat predelivered listing, sending unlock inventory message\" listingId={}",
            listing.getId());
        jmsMessageHelper.sendUnlockInventoryMessage(listing.getId());
      }
    }
  }
}
