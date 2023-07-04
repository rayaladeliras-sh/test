package com.stubhub.domain.inventory.listings.v2.controller.helper;

import java.util.List;

import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.v2.DTO.Buyer;

public class ListingHolder{
	Listing listing;
	List<TicketSeat> soldSeats;
	boolean isExternal;
	boolean externalCallSuccess;
	
	long requestKey;
	String fulfillmentType;
	Buyer buyer;
	
	
	
	public Buyer getBuyer() {
		return buyer;
	}
	public void setBuyer(Buyer buyer) {
		this.buyer = buyer;
	}
	public String getFulfillmentType() {
		return fulfillmentType;
	}
	public void setFulfillmentType(String fulfillmentType) {
		this.fulfillmentType = fulfillmentType;
	}
	
	public long getRequestKey() {
		return requestKey;
	}
	public void setRequestKey(long requestKey) {
		this.requestKey = requestKey;
	}
	
	
	public ListingHolder(Listing listing, List<TicketSeat> soldSeats, long requestKey, Buyer buyer){
		this.listing = listing;
		this.soldSeats = soldSeats;		
		this.requestKey = requestKey;
		this.buyer = buyer;
		
	}
	public Listing getListing() {
		return listing;
	}
	public void setListing(Listing listing) {
		this.listing = listing;
	}
	public List<TicketSeat> getSoldSeats() {
		return soldSeats;
	}
	public void setSoldSeats(List<TicketSeat> soldSeats) {
		this.soldSeats = soldSeats;
	}
	public boolean isExternal() {
		return isExternal;
	}
	public void setExternal(boolean isExternal) {
		this.isExternal = isExternal;
	}
	public boolean isExternalCallSuccess() {
		return externalCallSuccess;
	}
	public void setExternalCallSuccess(boolean externalCallSuccess) {
		this.externalCallSuccess = externalCallSuccess;
	}	
	
}