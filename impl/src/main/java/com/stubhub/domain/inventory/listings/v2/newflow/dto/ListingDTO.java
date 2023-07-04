package com.stubhub.domain.inventory.listings.v2.newflow.dto;

import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;

public class ListingDTO {
	// Listing Type and Original Request
	private ListingType listingType;
	private final ListingRequest listingRequest;
	private Listing dbListing;

	// Various functional Info
	private ListingStatus status;
	private ListingInfo listingInfo;
	private SellerInfo sellerInfo;
	private EventInfo eventInfo;
	private FulfillmentInfo fulfillmentInfo;
	private PricingInfo pricingInfo;
	private PaymentInfo paymentInfo;
	private HeaderInfo headerInfo;
	private UpdateListingInfo updateListingInfo;
	
	public ListingDTO(ListingRequest listingRequest) {
		this.listingRequest = listingRequest;
	}
	
	public ListingRequest getListingRequest() {
		return listingRequest;
	}
	
	public Listing getDbListing() {
		return dbListing;
	}

	public void setDbListing(Listing dbListing) {
		this.dbListing = dbListing;
	}

	public ListingStatus getStatus() {
		return status;
	}

	public void setStatus(ListingStatus status) {
		this.status = status;
	}

	public ListingInfo getListingInfo() {
		return listingInfo;
	}

	public void setListingInfo(ListingInfo listingInfo) {
		this.listingInfo = listingInfo;
	}
	
	public SellerInfo getSellerInfo() {
		return sellerInfo;
	}

	public void setSellerInfo(SellerInfo sellerInfo) {
		this.sellerInfo = sellerInfo;
	}

	public EventInfo getEventInfo() {
		return eventInfo;
	}
	
	public void setEventInfo(EventInfo eventInfo) {
		this.eventInfo = eventInfo;
	}

	public ListingType getListingType() {
		return listingType;
	}

	public void setListingType(ListingType listingType) {
		this.listingType = listingType;
	}

	public FulfillmentInfo getFulfillmentInfo() {
		return fulfillmentInfo;
	}

	public void setFulfillmentInfo(FulfillmentInfo fulfillmentInfo) {
		this.fulfillmentInfo = fulfillmentInfo;
	}

	public PricingInfo getPricingInfo() {
		return pricingInfo;
	}

	public void setPricingInfo(PricingInfo pricingInfo) {
		this.pricingInfo = pricingInfo;
	}

	public PaymentInfo getPaymentInfo() {
		return paymentInfo;
	}

	public void setPaymentInfo(PaymentInfo paymentInfo) {
		this.paymentInfo = paymentInfo;
	}

  public HeaderInfo getHeaderInfo() {
    return headerInfo;
  }

  public void setHeaderInfo(HeaderInfo headerInfo) {
    this.headerInfo = headerInfo;
  }

  public UpdateListingInfo getUpdateListingInfo() {
    return updateListingInfo;
  }

  public void setUpdateListingInfo(UpdateListingInfo updateListingInfo) {
    this.updateListingInfo = updateListingInfo;
  }
  
  
	
}
