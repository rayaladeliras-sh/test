package com.stubhub.domain.inventory.v2.DTO;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "businessResponseLite")
public class BusinessResponse {

  private String companyName;

  private Long businessId;

  private String businessGuid;

  private BusinessAddressResponse address;

  public BusinessResponse() {
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
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

  public BusinessAddressResponse getAddress() {
    return address;
  }

  public void setAddress(BusinessAddressResponse address) {
    this.address = address;
  }

  @Override
  public String toString() {
    return "BusinessResponse {" +
        "companyName='" + companyName + '\'' +
        ", businessId=" + businessId +
        ", businessGuid='" + businessGuid + '\'' +
        ", address=" + address +
        '}';
  }
}