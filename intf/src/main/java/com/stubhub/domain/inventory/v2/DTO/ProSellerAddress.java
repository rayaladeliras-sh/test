package com.stubhub.domain.inventory.v2.DTO;

import org.codehaus.jackson.map.annotate.JsonRootName;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@JsonRootName("address")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
    name = "address"
)
public class ProSellerAddress {

  private String lineAddress1;
  private String lineAddress2;
  private String city;
  private String state;
  private String country;
  private String postCode;

  public ProSellerAddress() {
  }

  public ProSellerAddress(String lineAddress1, String lineAddress2, String city, String state, String country, String postCode) {
    this.lineAddress1 = lineAddress1;
    this.lineAddress2 = lineAddress2;
    this.city = city;
    this.state = state;
    this.country = country;
    this.postCode = postCode;
  }

  public String getLineAddress1() {
    return lineAddress1;
  }

  public void setLineAddress1(String lineAddress1) {
    this.lineAddress1 = lineAddress1;
  }

  public String getLineAddress2() {
    return lineAddress2;
  }

  public void setLineAddress2(String lineAddress2) {
    this.lineAddress2 = lineAddress2;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getPostCode() {
    return postCode;
  }

  public void setPostCode(String postCode) {
    this.postCode = postCode;
  }

  @Override
  public String toString() {
    return "ProSellerAddress {" +
        "lineAddress1='" + lineAddress1 + '\'' +
        ", lineAddress2='" + lineAddress2 + '\'' +
        ", city='" + city + '\'' +
        ", state='" + state + '\'' +
        ", country='" + country + '\'' +
        ", postCode='" + postCode + '\'' +
        '}';
  }
}
