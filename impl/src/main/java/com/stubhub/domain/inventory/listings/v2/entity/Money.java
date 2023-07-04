package com.stubhub.domain.inventory.listings.v2.entity;

import java.math.BigDecimal;

public class Money {
	private String currencyCode;
	private BigDecimal amount;
	
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

}
