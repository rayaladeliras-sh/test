package com.stubhub.domain.inventory.v2.DTO;


import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.entity.SplitOption;
import com.stubhub.domain.inventory.v2.enums.TicketMedium;
import com.stubhub.newplatform.common.entity.Money;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonRootName;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "listing")
@JsonRootName(value = "listing")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "listingId", "faceValue", "pricePerProduct", "purchasePrice", 
		"buyerSeesPerProduct", "payoutPerProduct", "quantity", "inhandDate","adjustInhandDate", "saleEndDate", 
		"section", "products", "hideSeats", "splitQuantity", "splitOption", "deliveryOption", "ticketTraits",
		"internalNotes", "externalListingId", "status", "tealeafSessionId","threatMatrixSessionId", 
		"paymentType", "ccId", "contactId", "contactGuid", "eventId", "event", "isElectronicDelivery", "lmsExtension", 
		"adjustPrice", "requestId", "ticketClass", "comments", "lmsApprovalStatus", "ticketMedium", "markup", "autoPricingEnabledInd",
		"ticketMediums", "purchasePricePerProduct", "salesTaxPaid", "attributes"
		})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListingRequest implements Serializable, SplunkFormattedLog
{
	private static final long serialVersionUID = -3479538438736922279L;

	@XmlElement(name = "listingId", required = false)
	private Long listingId;
	
	// prices 
	@XmlElement(name = "faceValue", required = false)
	private Money faceValue;
	
	@XmlElement(name = "pricePerProduct", required = false)
	private Money pricePerProduct;
	
	@XmlElement(name = "purchasePrice", required = false)
	private Money purchasePrice;
	
	@XmlElement(name = "buyerSeesPerProduct", required = false)
	private Money buyerSeesPerProduct;	
	
	@XmlElement(name = "payoutPerProduct", required = false)
	private Money payoutPerProduct;		
	  
	@XmlElement(name = "quantity", required = false)
	private Integer quantity;
	
	@XmlElement(name = "inhandDate", required = false)
	private String inhandDate;
	
	@XmlElement(name = "adjustInhandDate", required = false)
	private Boolean adjustInhandDate =true;

	@XmlElement(name = "saleEndDate", required = false)
	private String saleEndDate;
	
	@XmlElement(name = "section", required = false)
	private String section;
	
	// Products (that include tickets, parking_pass, etc) 
	@XmlElement(name = "products", required = false)
	private List<Product> products;
	
	@XmlElement(name = "hideSeats", required = false)
	private Boolean hideSeats;
	
	@XmlElement(name = "splitQuantity", required = false)
	private Integer splitQuantity;
	
	@XmlElement(name = "lmsApprovalStatus", required = false)
	private Integer lmsApprovalStatus;
	
	@XmlElement(name="splitOption", required = false)
	@JsonDeserialize(using = SplitOptionDeserializer.class)
	private SplitOption splitOption;
	
	@XmlElement(name="ticketMedium", required = false)
	@JsonDeserialize(using = TicketMediumDeserializer.class)
	private TicketMedium ticketMedium;
		
	@XmlElement(name = "deliveryOption", required = true)
	@JsonDeserialize(using = DeliveryOptionDeserializer.class)
	private DeliveryOption deliveryOption;

	@XmlElement(name = "ticketTraits", required = false)
	private List<TicketTrait> ticketTraits;
	
	@XmlElement(name = "internalNotes", required = false)
	private String internalNotes;

	@XmlElement(name = "externalListingId", required = false)
	private String externalListingId;
	
	@XmlElement(name = "status", required = false)
	@JsonDeserialize(using = ListingStatusDeserializer.class)
	private ListingStatus status;
	
	@XmlElement(name = "paymentType", required = false)
	private Long paymentType;
	
	@XmlElement(name = "ccId", required = false)
	private String ccId;
	
	@XmlElement(name = "contactId", required = false)
	private Long contactId;
	
	@XmlElement(name = "contactGuid", required = false)
	private String contactGuid;
	
	@XmlElement(name = "tealeafSessionId", required = false)
	private String tealeafSessionId;
	
	@XmlElement(name = "threatMatrixSessionId", required = false)
	private String threatMatrixSessionId;
	
	@XmlElement(name = "eventId", required = false)
	private String eventId;
	
	@XmlElement(name = "event", required = false)
	private EventInfo event;
	
	@XmlElement(name ="isElectronicDelivery", required = false )
	private Boolean isElectronicDelivery;
	
	@XmlElement(name = "lmsExtension", required = false)
	private Boolean lmsExtension;
	
	@XmlElement(name = "adjustPrice", required = false)
	private Boolean adjustPrice;
	
	@XmlElement(name = "requestId", required = false)
	private String requestId;

	@XmlElement(name = "ticketClass", required = false)
	private String ticketClass;
	
	@XmlElement(name = "comments", required = false)
	private String comments;

	@XmlElement(name ="markup", required = false )
	private Boolean markup;

	@XmlElement(name = "autoPricingEnabledInd", required = false)
	private Boolean autoPricingEnabledInd;

	@XmlElement(name = "ticketMediums", required = false)
	private List<TicketMediumInfo> ticketMediums;

	@XmlElement(name = "purchasePricePerProduct", required = false)
	private Money purchasePricePerProduct;

	@XmlElement(name = "salesTaxPaid", required = false)
	private Boolean salesTaxPaid;

	@XmlElement(name = "attributes", required = false)
	private Attribute attributes;
	
	public String getRequestId() {
		return requestId;
	}
	
	public Integer getLmsApprovalStatus() {
		return lmsApprovalStatus;
	}

	public void setLmsApprovalStatus(Integer lmsApprovalStatus) {
		this.lmsApprovalStatus = lmsApprovalStatus;
	}
	
	public TicketMedium getTicketMedium() {
		return ticketMedium;
	}

	public void setTicketMedium(TicketMedium ticketMedium) {
		this.ticketMedium = ticketMedium;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Integer getSplitQuantity() {
		return splitQuantity;
	}

	public void setSplitQuantity(Integer splitQuantity) {
		this.splitQuantity = splitQuantity;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getInhandDate() {
		return inhandDate;
	}

	public void setInhandDate(String inhandDate) {
		this.inhandDate = inhandDate;
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

	public DeliveryOption getDeliveryOption() {
		return deliveryOption;
	}

	public void setDeliveryOption(DeliveryOption deliveryOption) {
		this.deliveryOption = deliveryOption;
	}

	public List<TicketTrait> getTicketTraits() {
		return ticketTraits;
	}

	public void setTicketTraits(List<TicketTrait> ticketTraits) {
		this.ticketTraits = ticketTraits;
	}

	public String getInternalNotes() {
		return internalNotes;
	}

	public void setInternalNotes(String internalNotes) {
		this.internalNotes = internalNotes;
	}

	public SplitOption getSplitOption() {
		return splitOption;
	}

	public void setSplitOption(SplitOption splitOption) {
		this.splitOption = splitOption;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public Long getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(Long paymentType) {
		this.paymentType = paymentType;
	}

	public String getCcId() {
		return ccId;
	}

	public void setCcId(String ccId) {
		this.ccId = ccId;
	}

	public String getContactGuid() {
		return contactGuid;
	}

	public void setContactGuid(String contactGuid) {
		this.contactGuid = contactGuid;
	}

	public ListingStatus getStatus() {
		return status;
	}

	public void setStatus(ListingStatus status) {
		this.status = status;
	}

	public String getSaleEndDate() {
		return saleEndDate;
	}

	public void setSaleEndDate(String saleEndDate) {
		this.saleEndDate = saleEndDate;
	}
	
	public String getTealeafSessionId() {
		return tealeafSessionId;
	}

	public void setTealeafSessionId(String tealeafSessionId) {
		this.tealeafSessionId = tealeafSessionId;
	}

	public String getThreatMatrixSessionId() {
		return threatMatrixSessionId;
	}

	public void setThreatMatrixSessionId(String threatMatrixSessionId) {
		this.threatMatrixSessionId = threatMatrixSessionId;
	}

	public Long getContactId() {
		return contactId;
	}

	public void setContactId(Long contactId) {
		this.contactId = contactId;
	}

	public String getExternalListingId() {
		return externalListingId;
	}

	public void setExternalListingId(String externalListingId) {
		this.externalListingId = externalListingId;
	}

	public Money getPricePerProduct() {
		return pricePerProduct;
	}

	public void setPricePerProduct(Money pricePerProduct) {
		this.pricePerProduct = pricePerProduct;
	}

	public Money getBuyerSeesPerProduct() {
		return buyerSeesPerProduct;
	}

	public void setBuyerSeesPerProduct(Money buyerSeesPerProduct) {
		this.buyerSeesPerProduct = buyerSeesPerProduct;
	}

	public Money getPayoutPerProduct() {
		return payoutPerProduct;
	}

	public void setPayoutPerProduct(Money payoutPerProduct) {
		this.payoutPerProduct = payoutPerProduct;
	}

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}
	
	public Boolean isHideSeats() {
		return hideSeats;
	}

	public void setHideSeats(Boolean hideSeats) {
		this.hideSeats = hideSeats;
	}
	
	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public EventInfo getEvent() {
		return event;
	}

	public void setEvent(EventInfo event) {
		this.event = event;
	}
	
	public Boolean getIsElectronicDelivery() {
		return isElectronicDelivery;
	}

	public void setIsElectronicDelivery(Boolean isElectronicDelivery) {
		this.isElectronicDelivery = isElectronicDelivery;
	}
	
	public Boolean getLmsExtension() {
		return lmsExtension;
	}

	public void setLmsExtension(Boolean lmsExtension) {
		this.lmsExtension = lmsExtension;
	}
	
	public Boolean getAdjustPrice() {
		return adjustPrice;
	}

	public void setAdjustPrice(Boolean adjustPrice) {
		this.adjustPrice = adjustPrice;
	}

	public Boolean isAdjustInhandDate() {
		return adjustInhandDate;
	}

	public void setAdjustInhandDate(Boolean adjustInhandDate) {
		this.adjustInhandDate = adjustInhandDate;
	}

	public List<TicketMediumInfo> getTicketMediums() {
		return ticketMediums;
	}

	public void setTicketMediums(List<TicketMediumInfo> ticketMediums) {
		this.ticketMediums = ticketMediums;
	}

	public Money getPurchasePricePerProduct() {
		return purchasePricePerProduct;
	}

	public void setPurchasePricePerProduct(Money purchasePricePerProduct) {
		this.purchasePricePerProduct = purchasePricePerProduct;
	}

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

	public String getTicketClass() {
		return ticketClass;
	}

	public void setTicketClass(String ticketClass) {
		this.ticketClass = ticketClass;
	}
	
	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	/**
	 * @return Boolean the markup
	 */
	public Boolean isMarkup() {
		return markup;
	}

	/**
	 * @param Boolean the markup to set
	 */
	public void setMarkup(Boolean markup) {
		this.markup = markup;
	}

	public Boolean isAutoPricingEnabledInd() {
		return autoPricingEnabledInd;
	}

	public void setAutoPricingEnabledInd(Boolean autoPricingEnabledInd) {
		this.autoPricingEnabledInd = autoPricingEnabledInd;
	}

	public Boolean getSalesTaxPaid() {
		return salesTaxPaid;
	}

	public void setSalesTaxPaid(Boolean salesTaxPaid) {
		this.salesTaxPaid = salesTaxPaid;
	}

	public Attribute getAttributes() {
		return attributes;
	}

	public void setAttributes(Attribute attributes) {
		this.attributes = attributes;
	}

@Override
  public String toString() {
    return "ListingRequest [listingId=" + listingId + ", faceValue=" + faceValue
        + ", pricePerProduct=" + pricePerProduct + ", purchasePrice=" + purchasePrice
        + ", buyerSeesPerProduct=" + buyerSeesPerProduct + ", payoutPerProduct=" + payoutPerProduct
        + ", quantity=" + quantity + ", inhandDate=" + inhandDate + ", saleEndDate=" + saleEndDate
        + ", section=" + section + ", products=" + products + "hideSeats=" + hideSeats 
        + ", splitQuantity=" + splitQuantity
        + ", lmsApprovalStatus=" + lmsApprovalStatus + ", splitOption=" + splitOption
        + ", ticketMedium=" + ticketMedium + ", deliveryOption=" + deliveryOption
        + ", ticketTraits=" + ticketTraits + ", internalNotes=" + internalNotes
        + ", externalListingId=" + externalListingId + ", status=" + status + ", paymentType="
        + paymentType + ", ccId=" + ccId + ", contactId=" + contactId + ", contactGuid=" + contactGuid + ","
        + "tealeafSessionId=" + tealeafSessionId + ", threatMatrixSessionId=" + threatMatrixSessionId + ","
        + " eventId="+ eventId + ", event=" + event + ", lmsExtension=" + lmsExtension + ", adjustPrice="
        + adjustPrice + ", requestId=" + requestId + ", ticketClass=" + ticketClass + ", comments="
        + comments + ", autoPricingEnabledInd=" + autoPricingEnabledInd +  ", ticketMediums=" + ticketMediums + ", salesTaxPaid="+salesTaxPaid +", purchasePricePerProduct=" + purchasePricePerProduct +", + attributes=" + attributes +"]";
  }
  	@Override
	public String   formatForLog(){
		StringBuilder sbRowRequest = new StringBuilder();
		StringBuilder sbSeatRequest = new StringBuilder();
		if (CollectionUtils.isNotEmpty(getProducts())) {
			for (Product product : getProducts()) {
				if(product.getRow() != null && sbRowRequest.indexOf(product.getRow()) < 0 ){
					sbRowRequest.append(product.getRow());
				}
				sbSeatRequest.append(product.getSeat()).append(",");
			}
			if(sbSeatRequest.length() > 0){
				sbSeatRequest.deleteCharAt(sbSeatRequest.length()-1);
			}
		}
		StringBuilder sb = new StringBuilder();
		if(!StringUtils.isBlank(this.eventId)){
			sb.append("mode=eventId ");
		}else{
			sb.append("mode=event ");
		}
		
		
		if (quantity != null && quantity != 0) {
			sb = sb.append("quantity=").append(quantity).append(" ");
		}
		else if(products != null && products.size() > 0){
			sb = sb.append("quantity=").append(products.size()).append(" ");
		}
		if (getListingId() != null) {
			sb = sb.append("listingId=").append(getListingId()).append(" ");
		}
		if (StringUtils.isNotBlank(getEventId())) {
			sb = sb.append("eventId=").append(getEventId()).append(" ");
		}
		if (StringUtils.isNotBlank(getExternalListingId())) {
			sb = sb.append("externalListingId=").append(getExternalListingId()).append(" ");
		}
		if (StringUtils.isNotBlank(getSection())) {
			sb = sb.append("section=").append(getSection()).append(" ");
		}
		if (StringUtils.isNotBlank(sbRowRequest)) {
			sb = sb.append("row=").append(sbRowRequest).append(" ");
		}
		if (StringUtils.isNotBlank(sbSeatRequest)) {
			sb = sb.append("seat=").append(sbSeatRequest).append(" ");
		}
		if (getPricePerProduct() != null) {
			sb = sb.append("listPrice=").append(getPricePerProduct().getAmount()).append(" ");
			sb = sb.append("currency=").append(getPricePerProduct().getCurrency()).append(" ");
		}
		if (getStatus() != null) {
			sb = sb.append("status=").append(getStatus()).append(" ");
		}
		if (getEvent() != null && getEvent().getName() != null) {
			sb = sb.append("eventName=\"").append(getEvent().getName()).append("\" ");
		}
		if (getEvent() != null && getEvent().getEventLocalDate() != null) {
			sb = sb.append("eventDateLocal=").append(getEvent().getEventLocalDate()).append(" ");
		}
		if (getEvent() != null && getEvent().getDate() != null) {
			sb = sb.append("eventDate=").append(getEvent().getDate()).append(" ");
		}
		if (getEvent() != null && getEvent().getVenue() != null) {
			sb = sb.append("venueName=\"").append(getEvent().getVenue()).append("\" ");
		}
		if (getEvent() != null && getEvent().getCity() != null) {
			sb = sb.append("venueCity=\"").append(getEvent().getCity()).append("\" ");
		}
		if (getEvent() != null && getEvent().getState() != null) {
			sb = sb.append("venueState=").append(getEvent().getState()).append(" ");
		}
		if (getDeliveryOption() != null) {
			sb = sb.append("deliveryOption=").append(getDeliveryOption()).append(" ");
		}		
		if (isAutoPricingEnabledInd() != null) {
			sb = sb.append("autoPricingEnabledInd=").append(isAutoPricingEnabledInd()).append(" ");
		}
		if (getTicketMediums() != null) {
			sb = sb.append("ticketMediums=").append(getTicketMediums()).append(" ");
		}
		return sb.toString();
	}

}
