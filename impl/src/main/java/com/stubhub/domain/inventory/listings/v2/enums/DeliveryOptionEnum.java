package com.stubhub.domain.inventory.listings.v2.enums;

public enum DeliveryOptionEnum {
  PRE_DELIVERY(1l),MANUAL_DELIVERY(2l);
  
  private long deliveryOption;
  
  private DeliveryOptionEnum(long deliveryOption){
	  this.deliveryOption =deliveryOption;
  }

  public long getDeliveryOption() {
	return deliveryOption;
  }

}
