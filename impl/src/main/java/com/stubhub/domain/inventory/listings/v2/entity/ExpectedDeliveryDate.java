package com.stubhub.domain.inventory.listings.v2.entity;

import java.util.Calendar;

public class ExpectedDeliveryDate {
	
	private Calendar expectedDate;
	private String expectedDeliveryDesc;
	private ExpectedDeliveryType expectedDeliveryType;
	
	public enum ExpectedDeliveryType {
		Date, Description
	}
	
	public Calendar getExpectedDate() {
		return expectedDate;
	}
	public void setExpectedDate(Calendar expectedDate) {
		this.expectedDate = expectedDate;
	}
	public String getExpectedDeliveryDesc() {
		return expectedDeliveryDesc;
	}
	public void setExpectedDeliveryDesc(String expectedDeliveryDesc) {
		this.expectedDeliveryDesc = expectedDeliveryDesc;
	}
	public ExpectedDeliveryType getExpectedDeliveryType() {
		return expectedDeliveryType;
	}
	public void setExpectedDeliveryType(ExpectedDeliveryType expectedDeliveryType) {
		this.expectedDeliveryType = expectedDeliveryType;
	}
}
