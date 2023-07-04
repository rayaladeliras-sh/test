package com.stubhub.domain.inventory.listings.v2.tns.dto;

import com.google.gson.Gson;

public class FraudEvaluationRequest {
	private String listingId;
	private String sellerId;
	private String eventId;
	private String listingStatus;
	private String changeType;
	private String changeSubType;

	public String getListingId() {
		return listingId;
	}

	public void setListingId(String listingId) {
		this.listingId = listingId;
	}

	public String getChangeType() {
		return changeType;
	}

	public void setChangeType(String changeType) {
		this.changeType = changeType;
	}

	public String getChangeSubType() {
		return changeSubType;
	}

	public void setChangeSubType(String changeSubType) {
		this.changeSubType = changeSubType;
	}
	
	public String getSellerId() {
		return sellerId;
	}

	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getListingStatus() {
		return listingStatus;
	}

	public void setListingStatus(String listingStatus) {
		this.listingStatus = listingStatus;
	}

	public String toString() {
		return new Gson().toJson(this);
	}
}
