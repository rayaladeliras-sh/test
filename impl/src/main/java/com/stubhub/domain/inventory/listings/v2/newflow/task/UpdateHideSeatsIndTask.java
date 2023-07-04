package com.stubhub.domain.inventory.listings.v2.newflow.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;


@Component
@Scope("prototype")
public class UpdateHideSeatsIndTask extends RegularTask {

  private static final Logger log = LoggerFactory.getLogger(UpdateHideSeatsIndTask.class);
  
  private static final String COUNTRIES_HIDESEATS_PROHIBITED =
      "listing.country.hideseats.prohibited";
  
  private static final String CHP_DEFAULT = "GB,DE,FR";

  @Autowired
  private MasterStubhubPropertiesWrapper masterStubhubPropertiesWrapper;
  
  public UpdateHideSeatsIndTask(ListingDTO listingDTO) {
    super(listingDTO);
  }
  
  private ListingRequest request = listingDTO.getListingRequest();
  private Listing dbListing = listingDTO.getDbListing();

  @Override
  protected void preExecute() {
    String countriesHideSeatsProhibited =
        masterStubhubPropertiesWrapper.getProperty(COUNTRIES_HIDESEATS_PROHIBITED, CHP_DEFAULT);

    if (request.isHideSeats() != null && request.isHideSeats()) {
      String country = dbListing.getEvent().getCountry();
      if (countriesHideSeatsProhibited.contains(country)) {
        log.error("message=\"Seat Hiding not allowed in the event country\" eventCountry={}", country);
        throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.hideSeatsNotAllowed,
            "Seat hiding is prohibited in " + country);
      }
    }

  }

  @Override
  protected void execute() {
    if (request.isHideSeats() != null) {
      dbListing.setHideSeatInfoInd(request.isHideSeats());
    }
  }

  @Override
  protected void postExecute() {

  }

 }
