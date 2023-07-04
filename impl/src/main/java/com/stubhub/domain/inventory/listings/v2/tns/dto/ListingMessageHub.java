package com.stubhub.domain.inventory.listings.v2.tns.dto;

public class ListingMessageHub {

	private String type;
	private Integer quantity;
	private String medium;
	private String fulfillmentMethodId;
	private EventMessageHub event;
	private String section;
	private String rows;
	private String seats;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getMedium() {
		return medium;
	}

	public void setMedium(String medium) {
		this.medium = medium;
	}

	public String getFulfillmentMethodId() {
		return fulfillmentMethodId;
	}

	public void setFulfillmentMethodId(String fulfillmentMethodId) {
		this.fulfillmentMethodId = fulfillmentMethodId;
	}

	public EventMessageHub getEvent() {
		return event;
	}

	public void setEvent(EventMessageHub event) {
		this.event = event;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getRows() {
		return rows;
	}

	public void setRows(String rows) {
		this.rows = rows;
	}

	public String getSeats() {
		return seats;
	}

	public void setSeats(String seats) {
		this.seats = seats;
	}

}
