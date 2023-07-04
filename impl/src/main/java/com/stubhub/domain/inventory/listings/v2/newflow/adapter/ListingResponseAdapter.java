package com.stubhub.domain.inventory.listings.v2.newflow.adapter;

import org.springframework.stereotype.Component;

import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.newplatform.common.util.DateUtil;

@Component("listingResponseAdapter")
public class ListingResponseAdapter {
  public ListingResponse convertToListingResponse(Listing listing) {
    ListingResponse listingResponse = new ListingResponse();
    listingResponse.setId(listing.getId().toString());
    listingResponse.setExternalListingId(listing.getExternalId());
    if (listing.getSystemStatus() != null) {
      if ("PENDING PDF REVIEW".equalsIgnoreCase(listing.getSystemStatus())
          || "PENDING LOCK".equalsIgnoreCase(listing.getSystemStatus())) {
        listingResponse.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.PENDING);
      } else {
        listingResponse.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus
            .fromString(listing.getSystemStatus()));
      }
    }
    if (listing.isPriceAdjusted()) {
      listingResponse.setPricePerProduct(listing.getListPrice());
    }
    
    if(listing.isInHandDateAdjusted()) {
      listingResponse.setInhandDate(DateUtil.formatCalendar(listing.getInhandDate(), "MM-dd-yyyy"));
    }
    return listingResponse;
  }
}
