package com.stubhub.domain.inventory.v2.listings.intf;

/*

import com.stubhub.newplatform.common.entity.Money;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlRootElement(name="Listing")
@XmlType(name = "", 
		propOrder = {
		"eventId",
        "eventName",
		"currencyCode",
		"totalTickets",
		"listingId",
		"currentPrice",
		"displayPricePerTicket",
		"ticketClass",
		"sectionId",
		"row",
		"quantity",
		"sellerSectionName",
		"seatNumbers",
		"dirtyTicketInd",
		"sectionName",
		"zoneId",
		"zoneName",
		"splitOption",
		"ticketSplit",
		"splitVector",
		"sellerComments",
		"listingAttributesList",
		"faceValue",
		"serviceFee",
		"deliveryMethods",
		"isPaypalAvailable",
		"isStudentTicket",
		"isEPLClubPolicyEvent"
		})
public class Listing {
	
	private Long listingId;
	private Long eventId;
	private String eventName;
	private String currencyCode;
	private Integer totalTickets;
	private Money currentPrice;
	private String ticketClass;
	private Long sectionId;
	private String sectionName;
	private String row;
	private Integer quantity;
	private String sellerSectionName;
	private String seatNumbers;
	private Long zoneId;
	private String zoneName;
	private Short splitOption;
	private Integer ticketSplit;
	private String splitVector;
	private String sellerComments;
	private List<ListingAttribute> listingAttributesList;
	private Money faceValue;
	private Money serviceFee;
	private List<DeliveryMethod> deliveryMethods;
	private Integer isPaypalAvailable;
	private Short dirtyTicketInd;
	private Money displayPricePerTicket;
	private String isStudentTicket;
	private String isEPLClubPolicyEvent;
	
	public Money getDisplayPricePerTicket() {
		return displayPricePerTicket;
	}
	public void setDisplayPricePerTicket(Money displayPricePerTicket) {
		this.displayPricePerTicket = displayPricePerTicket;
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

    public String getEventName() {
        return eventName;
    }
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	public Integer getTotalTickets() {
		return totalTickets;
	}
	public void setTotalTickets(Integer totalTickets) {
		this.totalTickets = totalTickets;
	}
	public Money getCurrentPrice() {
		return currentPrice;
	}
	public void setCurrentPrice(Money currentPrice) {
		this.currentPrice = currentPrice;
	}
	public String getTicketClass() {
		return ticketClass;
	}
	public void setTicketClass(String ticketClass) {
		this.ticketClass = ticketClass;
	}
	public Long getSectionId() {
		return sectionId;
	}
	public void setSectionId(Long sectionId) {
		this.sectionId = sectionId;
	}
	public String getRow() {
		return row;
	}
	public void setRow(String row) {
		this.row = row;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public String getSellerSectionName() {
		return sellerSectionName;
	}
	public void setSellerSectionName(String sellerSectionName) {
		this.sellerSectionName = sellerSectionName;
	}
	public String getSeatNumbers() {
		return seatNumbers;
	}
	public void setSeatNumbers(String seatNumbers) {
		this.seatNumbers = seatNumbers;
	}
	public Long getZoneId() {
		return zoneId;
	}
	public void setZoneId(Long zoneId) {
		this.zoneId = zoneId;
	}
	public String getZoneName() {
		return zoneName;
	}
	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}
	public Short getSplitOption() {
		return splitOption;
	}
	public void setSplitOption(Short splitOption) {
		this.splitOption = splitOption;
	}
	public Integer getTicketSplit() {
		return ticketSplit;
	}
	public void setTicketSplit(Integer ticketSplit) {
		this.ticketSplit = ticketSplit;
	}
	public String getSplitVector() {
		return splitVector;
	}
	public void setSplitVector(String splitVector) {
		this.splitVector = splitVector;
	}

	public String getSellerComments() {
		return sellerComments;
	}
	public void setSellerComments(String sellerComments) {
		this.sellerComments = sellerComments;
	}
	@XmlElementWrapper(name="listingAttributeList")
	@XmlElement(name="listingAttribute")
	public List<ListingAttribute> getListingAttributesList() {
		return listingAttributesList;
	}
	public void setListingAttributesList(
			List<ListingAttribute> listingAttributesList) {
		this.listingAttributesList = listingAttributesList;
	}
	public Money getFaceValue() {
		return faceValue;
	}
	public void setFaceValue(Money faceValue) {
		this.faceValue = faceValue;
	}
	public Money getServiceFee() {
		return serviceFee;
	}
	public void setServiceFee(Money serviceFee) {
		this.serviceFee = serviceFee;
	}
	
	@XmlElementWrapper(name="deliveryMethodList")
	@XmlElement(name="deliveryMethod")
	public List<DeliveryMethod> getDeliveryMethods() {
		return deliveryMethods;
	}
	public void setDeliveryMethods(List<DeliveryMethod> deliveryMethods) {
		this.deliveryMethods = deliveryMethods;
	}
	public Integer getIsPaypalAvailable() {
		return isPaypalAvailable;
	}
	public void setIsPaypalAvailable(Integer isPaypalAvailable) {
		this.isPaypalAvailable = isPaypalAvailable;
	}
	public String getSectionName() {
		return sectionName;
	}
	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}
	public Short getDirtyTicketInd() {
		return dirtyTicketInd;
	}
	public void setDirtyTicketInd(Short dirtyTicketInd) {
		this.dirtyTicketInd = dirtyTicketInd;
	}
	public String getIsStudentTicket() {
		return isStudentTicket;
	}
	public void setIsStudentTicket(String isStudentTicket) {
		this.isStudentTicket = isStudentTicket;
	}
	public String getIsEPLClubPolicyEvent() {
		return isEPLClubPolicyEvent;
	}
	public void setIsEPLClubPolicyEvent(String isEPLClubPolicyEvent) {
		this.isEPLClubPolicyEvent = isEPLClubPolicyEvent;
	}

}
*/
