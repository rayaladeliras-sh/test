package com.stubhub.domain.inventory.listings.v2.newflow.helper;

import com.stubhub.domain.inventory.listings.v2.newflow.dto.EventInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.FulfillmentMethodEnum;
import com.stubhub.domain.inventory.v2.enums.TicketMedium;
import com.stubhub.newplatform.common.entity.Money;

public class PriceRequestDetails {

  private Long listingId;
  private String requestKey;
  private EventInfo eventInfo;
  private String fulfillmentType;
  private String predeliveryType;


  private String section;
  private String row;
  private Long sellerId;
  private String sellerGuid;
  private Money amountPerTicket;
  private String amountType;
  private boolean markup;
  private Money payoutPerProduct;
  private Money buyerSeesPerProduct;
  private Money pricePerProduct;

  private TicketMedium ticketMedium;
  private FulfillmentMethodEnum fulfillmentMethodEnum;


  /**
   * @return the listingId
   */
  public Long getListingId() {
    return listingId;
  }

  /**
   * @param listingId the listingId to set
   */
  public void setListingId(Long listingId) {
    this.listingId = listingId;
  }

  /**
   * @return the requestKey
   */
  public String getRequestKey() {
    return requestKey;
  }

  /**
   * @param requestKey the requestKey to set
   */
  public void setRequestKey(String requestKey) {
    this.requestKey = requestKey;
  }

  /**
   * @return the eventInfo
   */
  public EventInfo getEventInfo() {
    return eventInfo;
  }

  /**
   * @param eventInfo the eventInfo to set
   */
  public void setEventInfo(EventInfo eventInfo) {
    this.eventInfo = eventInfo;
  }

  /**
   * @return the fulfillmentType
   */
  public String getFulfillmentType() {
    return fulfillmentType;
  }

  /**
   * @param fulfillmentType the fulfillmentType to set
   */
  public void setFulfillmentType(String fulfillmentType) {
    this.fulfillmentType = fulfillmentType;
  }

  /**
   * @return the predeliveryType
   */
  public String getPredeliveryType() {
    return predeliveryType;
  }

  /**
   * @param predeliveryType the predeliveryType to set
   */
  public void setPredeliveryType(String predeliveryType) {
    this.predeliveryType = predeliveryType;
  }

  /**
   * @return the section
   */
  public String getSection() {
    return section;
  }

  /**
   * @param section the section to set
   */
  public void setSection(String section) {
    this.section = section;
  }

  /**
   * @return the row
   */
  public String getRow() {
    return row;
  }

  /**
   * @param row the row to set
   */
  public void setRow(String row) {
    this.row = row;
  }

  /**
   * @return the sellerId
   */
  public Long getSellerId() {
    return sellerId;
  }

  /**
   * @param sellerId the sellerId to set
   */
  public void setSellerId(Long sellerId) {
    this.sellerId = sellerId;
  }

  /**
   * @return the sellerGuid
   */
  public String getSellerGuid() {
    return sellerGuid;
  }

  /**
   * @param sellerGuid the sellerGuid to set
   */
  public void setSellerGuid(String sellerGuid) {
    this.sellerGuid = sellerGuid;
  }

  /**
   * @return the amountPerTicket
   */
  public Money getAmountPerTicket() {
    return amountPerTicket;
  }

  /**
   * @param amountPerTicket the amountPerTicket to set
   */
  public void setAmountPerTicket(Money amountPerTicket) {
    this.amountPerTicket = amountPerTicket;
  }

  /**
   * @return the amountType
   */
  public String getAmountType() {
    return amountType;
  }

  /**
   * @param amountType the amountType to set
   */
  public void setAmountType(String amountType) {
    this.amountType = amountType;
  }

  /**
   * @return the ticketMedium
   */
  public TicketMedium getTicketMedium() {
    return ticketMedium;
  }

  /**
   * @param ticketMedium the ticketMedium to set
   */
  public void setTicketMedium(TicketMedium ticketMedium) {
    this.ticketMedium = ticketMedium;
  }

  /**
   * @return the fulfillmentMethodEnum
   */
  public FulfillmentMethodEnum getFulfillmentMethodEnum() {
    return fulfillmentMethodEnum;
  }

  /**
   * @param fulfillmentMethodEnum the fulfillmentMethodEnum to set
   */
  public void setFulfillmentMethodEnum(FulfillmentMethodEnum fulfillmentMethodEnum) {
    this.fulfillmentMethodEnum = fulfillmentMethodEnum;
  }

  /**
   * @return the markup
   */
  public boolean isMarkup() {
    return markup;
  }

  /**
   * @param markup the markup to set
   */
  public void setMarkup(boolean markup) {
    this.markup = markup;
  }

  /**
   * @return the payoutPerProduct
   */
  public Money getPayoutPerProduct() {
    return payoutPerProduct;
  }

  /**
   * @param payoutPerProduct the payoutPerProduct to set
   */
  public void setPayoutPerProduct(Money payoutPerProduct) {
    this.payoutPerProduct = payoutPerProduct;
  }

  /**
   * @return the buyerSeesPerProduct
   */
  public Money getBuyerSeesPerProduct() {
    return buyerSeesPerProduct;
  }

  /**
   * @param buyerSeesPerProduct the buyerSeesPerProduct to set
   */
  public void setBuyerSeesPerProduct(Money buyerSeesPerProduct) {
    this.buyerSeesPerProduct = buyerSeesPerProduct;
  }

  /**
   * @return the pricePerProduct
   */
  public Money getPricePerProduct() {
    return pricePerProduct;
  }

  /**
   * @param pricePerProduct the pricePerProduct to set
   */
  public void setPricePerProduct(Money pricePerProduct) {
    this.pricePerProduct = pricePerProduct;
  }

}
