package com.stubhub.domain.inventory.v2.DTO;

import java.io.Serializable;
import java.util.Locale;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "event")
@XmlType(name = "", propOrder = {"eventId", "name", "date", "eventLocalDate", "venue", "address1",
    "address2", "city", "state", "country", "zipCode", "isParkingPassOnlyEvent", "locale", "eventValueString"})
public class EventInfo implements Serializable,SplunkFormattedLog {
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

@XmlElement(name = "eventId", required = false)
  private Long eventId;

  @XmlElement(name = "name", required = false)
  private String name;

  @XmlElement(name = "date", required = false)
  private String date;

  @XmlElement(name = "eventLocalDate", required = false)
  private String eventLocalDate;

  @XmlElement(name = "venue", required = true)
  private String venue;

  @XmlElement(name = "address1", required = false)
  private String address1;

  @XmlElement(name = "address2", required = false)
  private String address2;

  @XmlElement(name = "city", required = false)
  private String city;

  @XmlElement(name = "state", required = false)
  private String state;

  @XmlElement(name = "country", required = false)
  private String country;

  @XmlElement(name = "zipCode", required = false)
  private String zipCode;
  
  @XmlElement(name = "locale", required = false)
  private Locale locale;
  
  @XmlElement(name = "isParkingPassOnlyEvent", required = false)
  private Boolean isParkingPassOnlyEvent;

  @XmlElement(name = "eventValueString", required = false)
  private String eventValueString = null;
  
  @XmlElement(name = "isLanguageEnabled", required = false)
  private Boolean isLanguageEnabled;
  
  public Long getEventId() {
    return eventId;
  }

  public void setEventId(Long eventId) {
    this.eventId = eventId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getEventLocalDate() {
    return eventLocalDate;
  }

  public void setEventLocalDate(String eventLocalDate) {
    this.eventLocalDate = eventLocalDate;
  }

  public String getVenue() {
    return venue;
  }

  public void setVenue(String venue) {
    this.venue = venue;
  }

  public String getAddress1() {
    return address1;
  }

  public void setAddress1(String address1) {
    this.address1 = address1;
  }

  public String getAddress2() {
    return address2;
  }

  public void setAddress2(String address2) {
    this.address2 = address2;
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

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  public Locale getLocale() {
	return locale;
}

public void setLocale(Locale locale) {
	this.locale = locale;
}

public Boolean getIsParkingPassOnlyEvent() {
    return isParkingPassOnlyEvent;
  }

  public void setIsParkingPassOnlyEvent(Boolean isParkingPassOnlyEvent) {
    this.isParkingPassOnlyEvent = isParkingPassOnlyEvent;
  }

  public Boolean getIsLanguageEnabled() {
	return isLanguageEnabled;
  }
	
  public void setIsLanguageEnabled(Boolean isLanguageEnabled) {
    this.isLanguageEnabled = isLanguageEnabled;
  }

  @Override
  public int hashCode() {
    return calcValueString().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof EventInfo) {
      return calcValueString().equals(((EventInfo) obj).calcValueString());
    }
    return false;
  }  

  private String calcValueString() {
    if (eventValueString == null) {
      eventValueString = name + date + venue;
      eventValueString = eventValueString.trim().toLowerCase();
    }
    return eventValueString;
  }

  public String getEventValueString() {
    return eventValueString;
  }

  public void setEventValueString(String eventValueString) {
    this.eventValueString = eventValueString;
  }

  @Override
  public String toString() {
    return "EventInfo [eventId=" + eventId + ", name=" + name + ", date=" + date
        + ", eventLocalDate=" + eventLocalDate + ", venue=" + venue + ", address1=" + address1
        + ", address2=" + address2 + ", city=" + city + ", state=" + state + ", country=" + country
        + ", zipCode=" + zipCode + ", isParkingPassOnlyEvent=" + isParkingPassOnlyEvent
        + ", eventValueString=" + eventValueString + "]";
  }

  @Override
  public String formatForLog() {
    return "eventId=" + eventId + " name=\"" + name + "\" date=" + date
        + " eventLocalDate=" + eventLocalDate + " venue=\"" + venue + "\" ";
  }


}
