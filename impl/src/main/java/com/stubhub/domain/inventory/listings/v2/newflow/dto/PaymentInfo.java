package com.stubhub.domain.inventory.listings.v2.newflow.dto;

public class PaymentInfo {
  
  private Long paymentTypeId;
  
  private Long sellerCCId;

  public Long getPaymentTypeId() {
    return paymentTypeId;
  }

  public void setPaymentTypeId(Long paymentTypeId) {
    this.paymentTypeId = paymentTypeId;
  }

  public Long getSellerCCId() {
    return sellerCCId;
  }

  public void setSellerCCId(Long sellerCCId) {
    this.sellerCCId = sellerCCId;
  } 

}
