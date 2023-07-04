package com.stubhub.domain.inventory.listings.v2.newflow.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.FulfillmentInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.InhandDateHelper;


@Component
@Scope("prototype")
public class UpdateInhandDateTask extends RegularTask {

  private static final Logger logger = LoggerFactory.getLogger(UpdateInhandDateTask.class);

  @Autowired
  private InhandDateHelper inhandDateHelper;

  public UpdateInhandDateTask(ListingDTO dto) {
    super(dto);
  }

  @Override
  protected void preExecute() {}

  @Override
  protected void execute() {

    Listing dbListing = listingDTO.getDbListing();
    Integer tm = dbListing.getTicketMedium();
    logger.info("Start in-hand process for ticketMedium={}",tm);
    inhandDateHelper.processInhandDate(listingDTO, tm);

  }


  @Override
  protected void postExecute() {
    Listing dbListing = listingDTO.getDbListing();
    FulfillmentInfo fulfillmentInfo = listingDTO.getFulfillmentInfo();
    dbListing.setInhandDate(fulfillmentInfo.getInHandDate());
    dbListing.setInHandDateAdjusted(fulfillmentInfo.isInHandDateAdjusted());
    if (fulfillmentInfo.getDeclaredInhandDate() != null) {
      dbListing.setDeclaredInhandDate(fulfillmentInfo.getDeclaredInhandDate());
    }
    logger.info("end in-hand process for ticketMedium={}",dbListing.getTicketMedium());
  }

}
