package com.stubhub.domain.inventory.listings.v2.newflow.dto;

public class BusinessInfo {

  private String companyName;

  private Long businessId;

  private String businessGuid;

  private BusinessAddress address;

  public BusinessInfo(String companyName, Long businessId, String businessGuid, BusinessAddress address) {
    this.companyName = companyName;
    this.businessId = businessId;
    this.businessGuid = businessGuid;
    this.address = address;
  }

  public String getCompanyName() {
    return companyName;
  }

  public Long getBusinessId() {
    return businessId;
  }

  public String getBusinessGuid() {
    return businessGuid;
  }

  public BusinessAddress getAddress() {
    return address;
  }

  @Override
  public String toString() {
    return "BusinessInfo {" +
        "companyName='" + companyName + '\'' +
        ", businessId=" + businessId +
        ", businessGuid='" + businessGuid + '\'' +
        ", address=" + address +
        '}';
  }
}