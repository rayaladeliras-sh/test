package com.stubhub.domain.inventory.listings.v2.tns.dto;

public class Value {

	private String listingId;
	private String fraudCheckStatusId;
	private String sellerId;
	public String getListingId() {
		return listingId;
	}
	public void setListingId(String listingId) {
		this.listingId = listingId;
	}
	public String getFraudCheckStatusId() {
		return fraudCheckStatusId;
	}
	public void setFraudCheckStatusId(String fraudCheckStatusId) {
		this.fraudCheckStatusId = fraudCheckStatusId;
	}
	public String getSellerId() {
		return sellerId;
	}
	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}
	
}
