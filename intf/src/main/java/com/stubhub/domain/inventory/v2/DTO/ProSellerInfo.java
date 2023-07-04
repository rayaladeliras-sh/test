package com.stubhub.domain.inventory.v2.DTO;

import org.codehaus.jackson.map.annotate.JsonRootName;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * ProSeller refers to the sellers with sellerType TRADER or BUSINESS
 */
@JsonRootName("proSellerInfo")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
    name = "proSellerInfo"
)
public class ProSellerInfo {

  private String sellerType;

  private String sellerName;

  private ProSellerAddress address;

  public String getSellerType() {
    return sellerType;
  }

  public void setSellerType(String sellerType) {
    this.sellerType = sellerType;
  }

  public String getSellerName() {
    return sellerName;
  }

  public void setSellerName(String sellerName) {
    this.sellerName = sellerName;
  }

  public ProSellerAddress getAddress() {
    return address;
  }

  public void setAddress(ProSellerAddress address) {
    this.address = address;
  }

  @Override
  public String toString() {
    return "ProSellerInfo {" +
        "sellerType='" + sellerType + '\'' +
        ", sellerName='" + sellerName + '\'' +
        ", address='" + address + '\'' +
        '}';
  }
}
