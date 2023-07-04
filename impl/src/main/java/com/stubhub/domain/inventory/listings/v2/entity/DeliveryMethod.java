package com.stubhub.domain.inventory.listings.v2.entity;

import com.stubhub.domain.inventory.listings.v2.enums.DeliveryMethodEnum;


public class DeliveryMethod {
	
	private Long id;
	private DeliveryMethodEnum deliveryMethodEnum;
	private DeliveryType deliveryType;
	private String name;
	private Money cost;
	private Money deliveryFeePerTicket;
	private Money maxDeliveryFeePerOrder;
	private ExpectedDeliveryDate expectedDeliveryDate;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public DeliveryMethodEnum getDeliveryMethodEnum() {
		return deliveryMethodEnum;
	}
	public void setDeliveryMethodEnum(DeliveryMethodEnum deliveryMethodEnum) {
		this.deliveryMethodEnum = deliveryMethodEnum;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public DeliveryType getDeliveryType() {
		return deliveryType;
	}
	public void setDeliveryType(DeliveryType deliveryType) {
		this.deliveryType = deliveryType;
	}
	public Money getCost() {
		return cost;
	}
	public void setCost(Money cost) {
		this.cost = cost;
	}
	public Money getDeliveryFeePerTicket() {
		return deliveryFeePerTicket;
	}
	public void setDeliveryFeePerTicket(Money deliveryFeePerTicket) {
		this.deliveryFeePerTicket = deliveryFeePerTicket;
	}
	public Money getMaxDeliveryFeePerOrder() {
		return maxDeliveryFeePerOrder;
	}
	public void setMaxDeliveryFeePerOrder(Money maxDeliveryFeePerOrder) {
		this.maxDeliveryFeePerOrder = maxDeliveryFeePerOrder;
	}
	public ExpectedDeliveryDate getExpectedDeliveryDate() {
		return expectedDeliveryDate;
	}
	public void setExpectedDeliveryDate(ExpectedDeliveryDate expectedDeliveryDate) {
		this.expectedDeliveryDate = expectedDeliveryDate;
	}
	public DeliveryMethod(Long id, DeliveryMethodEnum deliveryMethodEnum,
			DeliveryType deliveryType, String name, Money cost, Money deliveryFeePerTicket, Money maxDeliveryFeePerOrder,
			ExpectedDeliveryDate expectedDeliveryDate) {
		super();
		this.id = id;
		this.deliveryMethodEnum = deliveryMethodEnum;
		this.deliveryType = deliveryType;
		this.name = name;
		this.cost = cost;
		this.deliveryFeePerTicket = deliveryFeePerTicket;
		this.maxDeliveryFeePerOrder = maxDeliveryFeePerOrder;
		this.expectedDeliveryDate = expectedDeliveryDate;
	}
	public DeliveryMethod() {
		super();
		// TODO Auto-generated constructor stub
	}
	

}
