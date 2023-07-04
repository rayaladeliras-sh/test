package com.stubhub.domain.inventory.listings.v2.entity;

import com.stubhub.domain.inventory.listings.v2.enums.DeliveryTypeEnum;

public class DeliveryType {
	
	private Long id;
	private DeliveryTypeEnum deliveryTypeEnum;
	private String name;
	private Boolean active;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public DeliveryTypeEnum getDeliveryTypeEnum() {
		return deliveryTypeEnum;
	}
	public void setDeliveryTypeEnum(DeliveryTypeEnum deliveryTypeEnum) {
		this.deliveryTypeEnum = deliveryTypeEnum;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}

}
