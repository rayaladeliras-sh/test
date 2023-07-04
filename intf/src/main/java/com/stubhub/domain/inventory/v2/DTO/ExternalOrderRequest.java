package com.stubhub.domain.inventory.v2.DTO;

import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonRootName;

import com.stubhub.newplatform.common.entity.Money;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "externalOrderRequest")
@XmlType(name = "", propOrder = { "orderId", "listingId", "eventId", "externalListingId", "fulfillmentType",
		"sellerPayout", "orderTotal", "sellerId", "sellerGuid", "brokerId", "sellerEmail", "buyerFirstName",
		"buyerLastName", "tickets", "buyerEmailAddress", "isRegisteredListing", "venueConfigSectionId", "vendorEventId" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalOrderRequest {
	
	@XmlElement(name = "orderId")
	private Long orderId;
	
	@XmlElement(name = "listingId")
	private Long listingId;
	
	@XmlElement(name = "eventId")
	private Long eventId;
	
	@XmlElement(name = "externalListingId")
	private String externalListingId;
	
	@XmlElement(name = "fulfillmentType")
	private String fulfillmentType;
	
	@XmlElement(name = "sellerPayout")
	private Money sellerPayout;
	
	@XmlElement(name = "orderTotal")
	private BigDecimal orderTotal;
	
	@XmlElement(name = "sellerId")
	private Long sellerId;
	
	@XmlElement(name = "sellerGuid")
	private String sellerGuid;
	
	@XmlElement(name = "brokerId")
	private Long brokerId;
	
	@XmlElement(name = "sellerEmail")
	private String sellerEmail;
	
	@XmlElement(name = "buyerFirstName")
	private String buyerFirstName;
	
	@XmlElement(name = "buyerLastName")
	private String buyerLastName;
	
	@XmlElement(name = "tickets")
	private List<Ticket> tickets;
	
	@XmlElement(name = "buyerEmailAddress")
	private String buyerEmailAddress;
	
	@XmlElement(name = "isRegisteredListing", required = false)
	private Boolean isRegisteredListing;
	
	@XmlElement(name = "venueConfigSectionId", required = false)
	private Long venueConfigSectionId;

	@XmlElement(name = "vendorEventId", required = false)
	private String vendorEventId;
	
	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getListingId() {
		return listingId;
	}

	public void setListingId(Long listingId) {
		this.listingId = listingId;
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getExternalListingId() {
		return externalListingId;
	}

	public void setExternalListingId(String externalListingId) {
		this.externalListingId = externalListingId;
	}

	public String getFulfillmentType() {
		return fulfillmentType;
	}

	public void setFulfillmentType(String fulfillmentType) {
		this.fulfillmentType = fulfillmentType;
	}

	public Money getSellerPayout() {
		return sellerPayout;
	}

	public void setSellerPayout(Money sellerPayout) {
		this.sellerPayout = sellerPayout;
	}

	public BigDecimal getOrderTotal() {
		return orderTotal;
	}

	public void setOrderTotal(BigDecimal orderTotal) {
		this.orderTotal = orderTotal;
	}

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

	public Long getBrokerId() {
		return brokerId;
	}

	public void setBrokerId(Long brokerId) {
		this.brokerId = brokerId;
	}

	public String getSellerEmail() {
		return sellerEmail;
	}

	public void setSellerEmail(String sellerEmail) {
		this.sellerEmail = sellerEmail;
	}

	public String getBuyerFirstName() {
		return buyerFirstName;
	}

	public void setBuyerFirstName(String buyerFirstName) {
		this.buyerFirstName = buyerFirstName;
	}

	public String getBuyerLastName() {
		return buyerLastName;
	}

	public void setBuyerLastName(String buyerLastName) {
		this.buyerLastName = buyerLastName;
	}

	public List<Ticket> getTickets() {
		return tickets;
	}

	public void setTickets(List<Ticket> tickets) {
		this.tickets = tickets;
	}

	public String getBuyerEmailAddress() {
		return buyerEmailAddress;
	}

	public void setBuyerEmailAddress(String buyerEmailAddress) {
		this.buyerEmailAddress = buyerEmailAddress;
	}

	public Boolean getIsRegisteredListing() {
		return isRegisteredListing;
	}

	public void setIsRegisteredListing(Boolean isRegisteredListing) {
		this.isRegisteredListing = isRegisteredListing;
	}

	public Long getVenueConfigSectionId() {
		return venueConfigSectionId;
	}

	public void setVenueConfigSectionId(Long venueConfigSectionId) {
		this.venueConfigSectionId = venueConfigSectionId;
	}

	public String getVendorEventId() {
		return vendorEventId;
	}

	public void setVendorEventId(String vendorEventId) {
		this.vendorEventId = vendorEventId;
	}
	
}
