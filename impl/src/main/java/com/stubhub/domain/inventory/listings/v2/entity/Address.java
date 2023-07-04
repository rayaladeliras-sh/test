package com.stubhub.domain.inventory.listings.v2.entity;

public class Address {
	
	private String streetAddress1;
	private String streetAddress2;
	private String streetAddress3;
	private String city;
	private String state;
	private String country;
	private String zip;
	private String zip4;
	
	public String getStreetAddress1() {
		return streetAddress1;
	}
	public void setStreetAddress1(String streetAddress1) {
		this.streetAddress1 = streetAddress1;
	}
	public String getStreetAddress2() {
		return streetAddress2;
	}
	public void setStreetAddress2(String streetAddress2) {
		this.streetAddress2 = streetAddress2;
	}
	public String getStreetAddress3() {
		return streetAddress3;
	}
	public void setStreetAddress3(String streetAddress3) {
		this.streetAddress3 = streetAddress3;
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
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public String getZip4() {
		return zip4;
	}
	public void setZip4(String zip4) {
		this.zip4 = zip4;
	}
	public Address(String streetAddress1, String streetAddress2,
			String streetAddress3, String city, String state, String country,
			String zip, String zip4) {
		super();
		this.streetAddress1 = streetAddress1;
		this.streetAddress2 = streetAddress2;
		this.streetAddress3 = streetAddress3;
		this.city = city;
		this.state = state;
		this.country = country;
		this.zip = zip;
		this.zip4 = zip4;
	}
	public Address() {
		super();
		// TODO Auto-generated constructor stub
	}
	

}
