package com.stubhub.domain.inventory.listings.v2.newflow.dto;

import com.stubhub.domain.fulfillment.window.v1.intf.EventInhanddate;
import com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.FulfillmentMethodEnum;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FulfillmentInfo {

  private List<FulfillmentWindow> fulfillmentWindows;

  private Map<Long, FulfillmentWindow> fulfillmentWindowMap =
      new HashMap<Long, FulfillmentWindow>();

  private Integer ticketMediumId;

  private Integer deliveryOptionId;

  private Integer confirmOptionId;

  private Calendar saleEndDate;

  private String fmDmList;

  private Calendar inHandDate;

  private boolean inHandDateAdjusted;

  private Calendar declaredInhandDate;

  private Map<String, EventInhanddate> inHandDateSettings;

  private FulfillmentMethodEnum fulfillmentMethodEnum;

  public List<FulfillmentWindow> getFulfillmentWindows() {
    return new ArrayList<FulfillmentWindow>(fulfillmentWindows);
  }

  public void setFulfillmentWindows(List<FulfillmentWindow> fulfillmentWindows) {
    this.fulfillmentWindows = new ArrayList<FulfillmentWindow>(fulfillmentWindows);
  }

  public Map<Long, FulfillmentWindow> getFulfillmentWindowMap() {
    return fulfillmentWindowMap;
  }

  public void setFulfillmentWindowMap(Map<Long, FulfillmentWindow> fulfillmentWindowMap) {
    this.fulfillmentWindowMap = fulfillmentWindowMap;
  }

  public Integer getTicketMediumId() {
    return ticketMediumId;
  }

  public void setTicketMediumId(Integer ticketMediumId) {
    this.ticketMediumId = ticketMediumId;
  }

  public Integer getDeliveryOptionId() {
    return deliveryOptionId;
  }

  public void setDeliveryOptionId(Integer deliveryOptionId) {
    this.deliveryOptionId = deliveryOptionId;
  }

  public Integer getConfirmOptionId() {
    return confirmOptionId;
  }

  public void setConfirmOptionId(Integer confirmOptionId) {
    this.confirmOptionId = confirmOptionId;
  }

  public Calendar getSaleEndDate() {
    return saleEndDate;
  }

  public void setSaleEndDate(Calendar saleEndDate) {
    this.saleEndDate = saleEndDate;
  }

  public String getFmDmList() {
    return fmDmList;
  }

  public void setFmDmList(String fmDmList) {
    this.fmDmList = fmDmList;
  }

  public FulfillmentMethodEnum getFulfillmentMethodEnum() {
    return fulfillmentMethodEnum;
  }

  public void setFulfillmentMethodEnum(FulfillmentMethodEnum fulfillmentMethodEnum) {
    this.fulfillmentMethodEnum = fulfillmentMethodEnum;
  }

  public Map<String, EventInhanddate> getInHandDateSettings() {
    return inHandDateSettings;
  }

  public void setInHandDateSettings(Map<String, EventInhanddate> inHandDateSettings) {
    this.inHandDateSettings = inHandDateSettings;
  }

  public Calendar getInHandDate() {
    return inHandDate;
  }

  public void setInHandDate(Calendar inHandDate) {
    this.inHandDate = inHandDate;
  }

  public boolean isInHandDateAdjusted() {
    return inHandDateAdjusted;
  }

  public void setInHandDateAdjusted(boolean inHandDateAdjusted) {
    this.inHandDateAdjusted = inHandDateAdjusted;
  }

  public Calendar getDeclaredInhandDate() {
    return declaredInhandDate;
  }

  public void setDeclaredInhandDate(Calendar declaredInhandDate) {
    this.declaredInhandDate = declaredInhandDate;
  }



}
