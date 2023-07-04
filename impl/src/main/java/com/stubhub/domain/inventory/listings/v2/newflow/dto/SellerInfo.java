package com.stubhub.domain.inventory.listings.v2.newflow.dto;

public class SellerInfo {

    private Long sellerId;

    private String sellerGuid;

    private Long sellerContactId;

    private String taxPayerStatus;

    private Long businessId;

    private String businessGuid;

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerGuid() {
        return sellerGuid;
    }

    public void setSellerGuid(String sellerGuid) {
        this.sellerGuid = sellerGuid;
    }

    public Long getSellerContactId() {
        return sellerContactId;
    }

    public void setSellerContactId(Long sellerContactId) {
        this.sellerContactId = sellerContactId;
    }

    public String getTaxPayerStatus() {
        return taxPayerStatus;
    }

    public void setTaxPayerStatus(String taxPayerStatus) {
        this.taxPayerStatus = taxPayerStatus;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getBusinessGuid() {
        return businessGuid;
    }

    public void setBusinessGuid(String businessGuid) {
        this.businessGuid = businessGuid;
    }

}
