package com.stubhub.domain.inventory.listings.v2.newflow.dto;

public class BusinessAddress {
  private String lineAddress1;
  private String lineAddress2;
  private String city;
  private String state;
  private String country;
  private String postCode;

  public BusinessAddress(String lineAddress1, String lineAddress2, String city, String state, String country, String postCode) {
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

  public String getLineAddress2() {
    return lineAddress2;
  }

  public String getCity() {
    return city;
  }

  public String getState() {
    return state;
  }

  public String getCountry() {
    return country;
  }

  public String getPostCode() {
    return postCode;
  }

  @Override
  public String toString() {
    return "BusinessAddress {" +
        "lineAddress1='" + lineAddress1 + '\'' +
        ", lineAddress2='" + lineAddress2 + '\'' +
        ", city='" + city + '\'' +
        ", state='" + state + '\'' +
        ", country='" + country + '\'' +
        ", postCode='" + postCode + '\'' +
        '}';
  }
}
