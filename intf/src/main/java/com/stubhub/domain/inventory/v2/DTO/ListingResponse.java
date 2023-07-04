package com.stubhub.domain.inventory.v2.DTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonRootName;

import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.entity.SaleMethod;
import com.stubhub.domain.inventory.common.entity.SplitOption;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.v2.listings.intf.DeliveryMethod;
import com.stubhub.newplatform.common.entity.Money;

@XmlRootElement(name ="listing")
@JsonRootName(value = "listing")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlType(name = "", propOrder = {
		"id",
		"status",
		"eventId",
		"eventDescription",
		"eventCountry",
		"eventDate",
		"createdDate",
		"listingSource",
		"venueDescription",
		"inhandDate",
		"quantity",
		"quantityRemain",
		"section",
		"rows",
		"seats",
		"venueConfigSectionId",
		"splitOption",
		"splitQuantity",
		"deliveryOption",
		"preDelivered",
		"saleEndDate",
		"saleMethod",
		"pricePerProduct",
		"faceValue",
		"purchasePrice",
		"payoutPerProduct",
		"startPricePerTicket",
		"endPricePerTicket",
		"buyerSeesPerProduct",
		"ticketTraits",
		"internalNotes",
		"contactId",
		"contactGuid",
		"paymentType",
		"ccId",
		"externalListingId",
		"fulfillmentDeliveryMethods",
		"products",
		"hideSeats",
		"fees",
		"totalPayout",
		"eventTimezone",
		"ticketMedium",
		"lmsApprovalStatus",
		"zoneId",
		"zoneName",
		"splitVector",
		"businessGUID",
		"ticketClass",
		"stubhubMobileTicket",
		"deliveryMethods",
		"sellerId",
		"scrubbedSectionName",
		"localizedSectionName",
		"primaryTicket",
		"sectionMappingRequired",
		"isScrubbingEnabled",
		"isParkingPassOnlyEvent",
		"confirmOptionId",
		"sellerIpAddress",
		"relist",
		"sellerInputPrice",
		"sellerInputPriceType",
		"snowInd",
        "venueId",
		"venueConfigId",
		"autoPricingEnabledInd",
		"salesTaxPaid",
		"createdBy",
		"purchasePricePerProduct",
		"proSellerInfo"})

public class ListingResponse {

	@XmlElement(name = "id", required = true)
	private String id;

	@XmlElement(name = "status", required = true)
	private ListingStatus status;

	@XmlElement(name = "eventId", required = true)
	private String eventId;

	@XmlElement(name = "eventDescription" , required = true)
	private String eventDescription;

	@XmlElement(name = "eventCountry" , required = true)
	private String eventCountry;

	//ex: 2012-01-12T16:00:00.000-08:00, event date is always local time
	@XmlElement(name = "eventDate", required = true)
	private String eventDate;

	@XmlElement(name = "createdDate", required = false)
	private String createdDate;

	@XmlElement(name = "listingSource", required = false)
	private Integer listingSource;

	@XmlElement(name = "venueDescription" , required = true)
	private String venueDescription;

	//ex: 2012-01-12T16:00:00.000-08:00
	@XmlElement(name = "inhandDate", required = false)
	private String inhandDate;

	@XmlElement(name = "quantity", required = true)
	private Integer quantity;

	@XmlElement(name = "quantityRemain", required = true)
	private Integer quantityRemain;

	@XmlElement(name = "section", required = true)
	private String section;

	@XmlElement(name = "rows", required = true)
	private String rows;

	@XmlElement(name = "seats", required = true)
	private String seats;

	@XmlElement(name = "venueConfigSectionId", required = false)
	private Long venueConfigSectionId;

	@XmlElement(name = "splitOption", required = false)
	private SplitOption splitOption;

	@XmlElement(name = "splitQuantity", required = false)
	private Integer splitQuantity;

	@XmlElement(name = "deliveryOption", required = true)
	private DeliveryOption deliveryOption;

	@XmlElement(name = "preDelivered", required = true)
	private Boolean preDelivered;

	//ex: 2012-01-12T16:00:00.000-08:00
	@XmlElement(name = "saleEndDate", required = true)
	private String saleEndDate;

	@XmlElement(name = "saleMethod", required = true)
	private SaleMethod saleMethod;

	@XmlElement(name = "pricePerProduct", required = false)
	private Money pricePerProduct;

	@XmlElement(name = "purchasePricePerProduct", required = false)
	private Money purchasePricePerProduct;

	@XmlElement(name = "faceValue", required = false)
	private Money faceValue;

	@XmlElement(name = "purchasePrice", required = false)
	private Money purchasePrice;

	@XmlElement(name = "payoutPerProduct", required = true)
	private Money payoutPerProduct;

	@XmlElement(name = "buyerSeesPerProduct", required = true)
	private Money buyerSeesPerProduct;

	@XmlElement(name = "sellerInputPrice", required = false)
	private Money sellerInputPrice;

	@XmlElement(name = "sellerInputPriceType", required = false)
	private String sellerInputPriceType;

	@XmlElement(name = "salesTaxPaid", required = false)
	private Boolean salesTaxPaid;

	@XmlElement(name = "startPricePerTicket", required = false)
	private Money startPricePerTicket;

	@XmlElement(name = "endPricePerTicket", required = false)
	private Money endPricePerTicket;

	@XmlElement(name = "ticketTraits", required = false)
	private Set<TicketTrait> ticketTraits;

	@XmlElement(name = "internalNotes", required = false)
	private String internalNotes;

	@XmlElement(name = "contactId", required = true)
	private Long contactId;

	@XmlElement(name = "contactGuid", required = true)
	private String contactGuid;

	@XmlElement(name = "paymentType", required = true)
	private String paymentType;

	@XmlElement(name = "ccId", required = true)
	private Long ccId;

	@XmlElement(name = "externalListingId", required = false)
	private String externalListingId;

	@XmlElement(name = "fulfillmentDeliveryMethods", required = false)
	private String fulfillmentDeliveryMethods;

	@XmlElement(name = "products", required = false)
	private List<Product> products;

	@XmlElement(name = "hideSeats", required = false)
	private Boolean hideSeats;

	@XmlElement(name = "fees", required = true)
	private List<Fee> fees;

	@XmlElement(name = "totalPayout", required = false)
	private Money totalPayout;

	@XmlElement(name = "eventTimezone",required = false)
	private String eventTimezone;

	@XmlElement(name = "ticketMedium",required = false)
	private Integer ticketMedium;

	@XmlElement(name = "lmsApprovalStatus",required = false)
	private Integer lmsApprovalStatus;

	@XmlElement(name = "zoneId",required = false)
	private Long zoneId;

	@XmlElement(name = "zoneName",required = false)
	private String zoneName;

	@XmlElement(name = "splitVector",required = false)
	private String splitVector;

	@XmlElement(name = "businessGUID",required = false)
	private String businessGUID;

	@XmlElement(name = "ticketClass",required = false)
	private String ticketClass;

	@XmlElement(name = "stubhubMobileTicket",required = false)
	private Integer stubhubMobileTicket;

	@XmlElement(name = "deliveryMethods",required = false)
	private List<DeliveryMethod> deliveryMethods;

	@XmlElement(name = "sellerId",required = false)
	private Long sellerId;

	@XmlElement(name = "scrubbedSectionName",required = false)
	private String scrubbedSectionName;

	@XmlElement(name = "localizedSectionName",required = false)
	private String localizedSectionName;


	@XmlElement(name = "primaryTicket",required = false)
	private Boolean primaryTicket;

	@XmlElement(name = "isScrubbingEnabled", required = false)
	private Boolean isScrubbingEnabled;

	@XmlElement(name = "isParkingPassOnlyEvent", required = false)
	private Boolean isParkingPassOnlyEvent;

	@XmlElement(name = "confirmOptionId", required = false)
	private Integer confirmOptionId;

	@XmlElement(name = "sellerIpAddress", required = false)
	private String sellerIpAddress;

	@XmlElement(name = "relist",required = false)
    private Boolean relist;

	@XmlElement(name = "snowInd",required = false)
    private Integer snowInd;

	@XmlElement(name = "venueId",required = false)
	private Long venueId;

	@XmlElement(name = "venueConfigId",required = false)
	private String venueConfigId;

	@XmlElement(name = "autoPricingEnabledInd", required = false)
	private Boolean autoPricingEnabledInd;

	@XmlElement(name = "createdBy", required = false)
	private String createdBy;

	@XmlElement(name = "proSellerInfo", required = false)
	private ProSellerInfo proSellerInfo;

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Boolean getIsParkingPassOnlyEvent() {
      return isParkingPassOnlyEvent;
    }

    public void setIsParkingPassOnlyEvent(Boolean isParkingPassOnlyEvent) {
      this.isParkingPassOnlyEvent = isParkingPassOnlyEvent;
    }

    public Boolean isPrimaryTicket() {
		return primaryTicket;
	}

	public void setPrimaryTicket(Boolean primaryTicket) {
		this.primaryTicket = primaryTicket;
	}

	@XmlElement(name = "sectionMappingRequired",required = false)
	private Boolean sectionMappingRequired;

	public Boolean isSectionMappingRequired() {
		return sectionMappingRequired;
	}

	public void setSectionMappingRequired(Boolean sectionMappingRequired) {
		this.sectionMappingRequired = sectionMappingRequired;
	}

	public String getScrubbedSectionName() {
		return scrubbedSectionName;
	}

	public void setScrubbedSectionName(String scrubbedSectionName) {
		this.scrubbedSectionName = scrubbedSectionName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ListingStatus getStatus() {
		return status;
	}

	public void setStatus(ListingStatus status) {
		this.status = status;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getEventDescription() {
		return eventDescription;
	}

	public void setEventDescription(String eventDescription) {
		this.eventDescription = eventDescription;
	}

	public String getEventCountry() {
		return eventCountry;
	}

	public void setEventCountry(String eventCountry) {
		this.eventCountry = eventCountry;
	}

	public String getVenueDescription() {
		return venueDescription;
	}

	public void setVenueDescription(String venueDescription) {
		this.venueDescription = venueDescription;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getQuantityRemain() {
		return quantityRemain;
	}

	public void setQuantityRemain(Integer quantityRemain) {
		this.quantityRemain = quantityRemain;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getRows() {
		return rows;
	}

	public void setRows(String rows) {
		this.rows = rows;
	}

	public String getSeats() {
		return seats;
	}

	public void setSeats(String seats) {
		this.seats = seats;
	}

	public Long getVenueConfigSectionId() {
		return venueConfigSectionId;
	}

	public void setVenueConfigSectionId(Long venueConfigSectionId) {
		this.venueConfigSectionId = venueConfigSectionId;
	}

	public SplitOption getSplitOption() {
		return splitOption;
	}

	public void setSplitOption(SplitOption splitOption) {
		this.splitOption = splitOption;
	}

	public Integer getSplitQuantity() {
		return splitQuantity;
	}

	public void setSplitQuantity(Integer splitQuantity) {
		this.splitQuantity = splitQuantity;
	}

	public DeliveryOption getDeliveryOption() {
		return deliveryOption;
	}

	public void setDeliveryOption(DeliveryOption deliveryOption) {
		this.deliveryOption = deliveryOption;
	}

	public Boolean getPreDelivered() {
		return preDelivered;
	}

	public void setPreDelivered(Boolean preDelivered) {
		this.preDelivered = preDelivered;
	}

	public SaleMethod getSaleMethod() {
		return saleMethod;
	}

	public void setSaleMethod(SaleMethod saleMethod) {
		this.saleMethod = saleMethod;
	}

	public Money getFaceValue() {
		return faceValue;
	}

	public void setFaceValue(Money faceValue) {
		this.faceValue = faceValue;
	}

	public Money getPurchasePrice() {
		return purchasePrice;
	}

	public void setPurchasePrice(Money purchasePrice) {
		this.purchasePrice = purchasePrice;
	}

	public Money getStartPricePerTicket() {
		return startPricePerTicket;
	}

	public void setStartPricePerTicket(Money startPricePerTicket) {
		this.startPricePerTicket = startPricePerTicket;
	}

	public Money getEndPricePerTicket() {
		return endPricePerTicket;
	}

	public void setEndPricePerTicket(Money endPricePerTicket) {
		this.endPricePerTicket = endPricePerTicket;
	}

	public Set<TicketTrait> getTicketTraits() {
		return ticketTraits;
	}

	public void setTicketTraits(Set<TicketTrait> ticketTraits) {
		this.ticketTraits = ticketTraits;
	}

	public String getInternalNotes() {
		return internalNotes;
	}

	public void setInternalNotes(String internalNotes) {
		this.internalNotes = internalNotes;
	}

	public Long getContactId() {
		return contactId;
	}

	public void setContactId(Long contactId) {
		this.contactId = contactId;
	}

	public String getContactGuid() {
		return contactGuid;
	}

	public void setContactGuid(String contactGuid) {
		this.contactGuid = contactGuid;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public Long getCcId() {
		return ccId;
	}

	public void setCcId(Long ccId) {
		this.ccId = ccId;
	}

	public String getExternalListingId() {
		return externalListingId;
	}

	public void setExternalListingId(String externalListingId) {
		this.externalListingId = externalListingId;
	}

	public String getEventDate() {
		return eventDate;
	}

	public void setEventDate(String eventDate) {
		this.eventDate = eventDate;
	}

	public String getInhandDate() {
		return inhandDate;
	}

	public void setInhandDate(String inhandDate) {
		this.inhandDate = inhandDate;
	}

	public String getSaleEndDate() {
		return saleEndDate;
	}

	public void setSaleEndDate(String saleEndDate) {
		this.saleEndDate = saleEndDate;
	}


	public String getFulfillmentDeliveryMethods() {
		return fulfillmentDeliveryMethods;
	}

	public void setFulfillmentDeliveryMethods(String fulfillmentDeliveryMethods) {
		this.fulfillmentDeliveryMethods = fulfillmentDeliveryMethods;
	}

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

	public List<Fee> getFees() {
		return fees;
	}

	public void setFees(List<Fee> fees) {
		this.fees = fees;
	}

	public Money getTotalPayout() {
		return totalPayout;
	}

	public void setTotalPayout(Money totalPayout) {
		this.totalPayout = totalPayout;
	}

	public Money getSellerInputPrice() {
		return sellerInputPrice;
	}

	public Boolean getSalesTaxPaid() {
		return salesTaxPaid;
	}

	public void setSalesTaxPaid(Boolean salesTaxPaid) {
		this.salesTaxPaid = salesTaxPaid;
	}

	public void setSellerInputPrice(Money sellerInputPrice) {
		this.sellerInputPrice = sellerInputPrice;
	}

	public String getSellerInputPriceType() {
		return sellerInputPriceType;
	}

	public void setSellerInputPriceType(String sellerInputPriceType) {
		this.sellerInputPriceType = sellerInputPriceType;
	}

	public String getLocalizedSectionName() {
		return localizedSectionName;
	}

	public void setLocalizedSectionName(String localizedSectionName) {
		this.localizedSectionName = localizedSectionName;
	}

	/**
	 * @return the eventTimezone
	 */
	public String getEventTimezone() {
		return eventTimezone;
	}

	/**
	 * @param eventTimezone the eventTimezone to set
	 */
	public void setEventTimezone(String eventTimezone) {
		this.eventTimezone = eventTimezone;
	}

	/**
	 * @return the ticketMedium
	 */
	public Integer getTicketMedium() {
		return ticketMedium;
	}

	/**
	 * @param ticketMedium the ticketMedium to set
	 */
	public void setTicketMedium(Integer ticketMedium) {
		this.ticketMedium = ticketMedium;
	}

	/**
	 * @return the lmsApprovalStatus
	 */
	public Integer getLmsApprovalStatus() {
		return lmsApprovalStatus;
	}

	/**
	 * @param lmsApprovalStatus the lmsApprovalStatus to set
	 */
	public void setLmsApprovalStatus(Integer lmsApprovalStatus) {
		this.lmsApprovalStatus = lmsApprovalStatus;
	}

	/**
	 * @return the zoneId
	 */
	public Long getZoneId() {
		return zoneId;
	}

	/**
	 * @param zoneId the zoneId to set
	 */
	public void setZoneId(Long zoneId) {
		this.zoneId = zoneId;
	}

	/**
	 * @return the zoneName
	 */
	public String getZoneName() {
		return zoneName;
	}

	/**
	 * @param zoneName the zoneName to set
	 */
	public void setZoneName(String zoneName) {
		this.zoneName = zoneName;
	}

	/**
	 * @return the splitVector
	 */
	public String getSplitVector() {
		return splitVector;
	}

	/**
	 * @param splitVector the splitVector to set
	 */
	public void setSplitVector(String splitVector) {
		this.splitVector = splitVector;
	}

	/**
	 * @return the businessGUID
	 */
	public String getBusinessGUID() {
		return businessGUID;
	}

	/**
	 * @param businessGUID the businessGUID to set
	 */
	public void setBusinessGUID(String businessGUID) {
		this.businessGUID = businessGUID;
	}

	/**
	 * @return the ticketClass
	 */
	public String getTicketClass() {
		return ticketClass;
	}

	/**
	 * @param ticketClass the ticketClass to set
	 */
	public void setTicketClass(String ticketClass) {
		this.ticketClass = ticketClass;
	}

	/**
	 * @return the deliveryMethods
	 */
	public List<DeliveryMethod> getDeliveryMethods() {
		return deliveryMethods;
	}

	/**
	 * @param deliveryMethods the deliveryMethods to set
	 */
	public void setDeliveryMethods(List<DeliveryMethod> deliveryMethods) {
		this.deliveryMethods = deliveryMethods;
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

	public Integer getConfirmOptionId() {
		return confirmOptionId;
	}

	public void setConfirmOptionId(Integer confirmOptionId) {
		this.confirmOptionId = confirmOptionId;
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

	public Money getPurchasePricePerProduct() {
		return purchasePricePerProduct;
	}

	public void setPurchasePricePerProduct(Money purchasePricePerProduct) {
		this.purchasePricePerProduct = purchasePricePerProduct;
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
	 * @return the stubhubMobileTicket
	 */
	public Integer getStubhubMobileTicket() {
		return stubhubMobileTicket;
	}

	/**
	 * @param stubhubMobileTicket the stubhubMobileTicket to set
	 */
	public void setStubhubMobileTicket(Integer stubhubMobileTicket) {
		this.stubhubMobileTicket = stubhubMobileTicket;
	}

	@XmlTransient
	private List<ListingError> errors;

	public List<ListingError> getErrors() {
		return errors;
	}

	public void setErrors(List<ListingError> errors) {
		this.errors = errors;
	}

	/**
	 * Adds an error (and create the errors list automatically if null)
	 * @param error
	 */
	public void addError ( ListingError error )
	{
		if ( errors == null ) {
			synchronized ( this ) {
				if ( errors == null )
					errors = new ArrayList<ListingError>();
			}
		}
		errors.add(error);
	}

	public Boolean getIsScrubbingEnabled() {
		return isScrubbingEnabled;
	}

	public void setIsScrubbingEnabled(Boolean isScrubbingEnabled) {
		this.isScrubbingEnabled = isScrubbingEnabled;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public Integer getListingSource() {
		return listingSource;
	}

	public void setListingSource(Integer listingSource) {
		this.listingSource = listingSource;
	}

	public Boolean isHideSeats() {
		return hideSeats;
	}

	public void setHideSeats(Boolean hideSeats) {
		this.hideSeats = hideSeats;

	}

	public String getSellerIpAddress() {
		return sellerIpAddress;
	}

	public void setSellerIpAddress(String sellerIpAddress) {
		this.sellerIpAddress = sellerIpAddress;
	}

	public Boolean getRelist() {
	    return relist;
	}

	public void setRelist(Boolean relist) {
	    this.relist = relist;
	}

	public Boolean isAutoPricingEnabledInd() { return autoPricingEnabledInd; }

	public void setAutoPricingEnabledInd(Boolean autoPricingEnabledInd) { this.autoPricingEnabledInd = autoPricingEnabledInd; }

	public Integer getSnowInd() {
		return snowInd;
	}

	public void setSnowInd(Integer snowInd) {
		this.snowInd = snowInd;
	}

	public Long getVenueId() {
		return venueId;
	}

	public void setVenueId(Long venueId) {
		this.venueId = venueId;
	}

	public String getVenueConfigId() {
		return venueConfigId;
	}

	public void setVenueConfigId(String venueConfigId) {
		this.venueConfigId = venueConfigId;
	}

	public ProSellerInfo getProSellerInfo() {
		return proSellerInfo;
	}

	public void setProSellerInfo(ProSellerInfo proSellerInfo) {
		this.proSellerInfo = proSellerInfo;
	}

	public String toString() {
		StringBuilder sbResponse = new StringBuilder();
		if (sellerId != null) {
			sbResponse = sbResponse.append("sellerId=").append(sellerId).append(" ");
		}
		if (StringUtils.isNotBlank(getId())) {
			sbResponse = sbResponse.append("listingId=").append(getId()).append(" ");
		}
		if (StringUtils.isNotBlank(getEventId())) {
			sbResponse = sbResponse.append("eventId=").append(getEventId()).append(" ");
		}
		if (getEventDate() != null) {
			sbResponse = sbResponse.append("eventDate=").append(getEventDate()).append(" ");
		}
		if (StringUtils.isNotBlank(getVenueDescription())) {
			sbResponse = sbResponse.append("venueDescription=").append(getVenueDescription()).append(" ");
		}
		if(StringUtils.isNotBlank(getEventCountry())) {
			sbResponse = sbResponse.append("eventCountry=").append(getEventCountry()).append(" ");
		}
		if (StringUtils.isNotBlank(getExternalListingId())) {
			sbResponse = sbResponse.append("externalListingId=").append(getExternalListingId()).append(" ");
		}
		if (StringUtils.isNotBlank(getSection())) {
			sbResponse = sbResponse.append("section=").append(getSection()).append(" ");
		}
		if (StringUtils.isNotBlank(getRows())) {
			sbResponse = sbResponse.append("row=").append(getRows()).append(" ");
		}
		if (StringUtils.isNotBlank(getSeats())) {
			sbResponse = sbResponse.append("seat=").append(getSeats()).append(" ");
		}
		if (getPricePerProduct() != null) {
			sbResponse = sbResponse.append("listPrice=").append(getPricePerProduct()).append(" ");
		}
		if (getDeliveryOption() != null) {
			sbResponse = sbResponse.append("deliveryOption=").append(getDeliveryOption()).append(" ");
		}
		if (getStatus() != null && getStatus().name() != null) {
			sbResponse = sbResponse.append("systemStatus=").append(getStatus().name()).append(" ");
		}
		if (getConfirmOptionId() != null) {
			sbResponse = sbResponse.append("confirmOptionId=").append(getConfirmOptionId()).append(" ");
		}
		if (isAutoPricingEnabledInd() != null) {
      sbResponse = sbResponse.append("autoPricingEnabledInd=").append(isAutoPricingEnabledInd()).append(" ");
		}
	  if (getPurchasePricePerProduct() != null) {
		  sbResponse = sbResponse.append("listPrice=").append(getPurchasePricePerProduct()).append(" ");
	  }
	  if (StringUtils.isNotBlank(getCreatedBy())) {
		  sbResponse = sbResponse.append("createdBy=").append(getCreatedBy()).append(" ");
	  }
		if (getProSellerInfo() != null) {
			sbResponse = sbResponse.append("proSellerInfo=").append(getProSellerInfo()).append(" ");
		}
		if (getId() != null) {
			sbResponse = sbResponse.append("statusCode=").append(Response.Status.OK.getStatusCode()).append(" ");
		}
		sbResponse = sbResponse.append("message=").append("success");
		return sbResponse.toString();
	}
}
