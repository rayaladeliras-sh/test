/**
 * 
 */
package com.stubhub.domain.inventory.v2.bulk.DTO;

import java.util.List;
import java.util.Locale;

import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.enums.ProxyRoleTypeEnum;

/**
 * @author sjayaswal
 *
 */
public class BulkListingInternal {

	private Long paymentType;
	
	private Long ccId;
	
	private Long contactId;
	
	private Long sellerId;
	
	private Long jobId;
	
	private Long groupId;
	
	private String assertion;
	
	private String sellerGuid;
	
	private Locale locale ;
	
	private List<ListingRequest> requestsBody;
	
	private String subscriber;
	
	private String operatorId;
	
	private ProxyRoleTypeEnum role;
	
	private Integer sellShStoreId;
	
	public Integer getSellShStoreId() {
		return sellShStoreId;
	}

	public void setSellShStoreId(Integer sellShStoreId) {
		this.sellShStoreId = sellShStoreId;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public ProxyRoleTypeEnum getRole() {
		return role;
	}

	public void setRole(ProxyRoleTypeEnum role) {
		this.role = role;
	}

	/**
	 * @return the paymentType
	 */
	public Long getPaymentType() {
		return paymentType;
	}

	/**
	 * @param paymentType the paymentType to set
	 */
	public void setPaymentType(Long paymentType) {
		this.paymentType = paymentType;
	}

	/**
	 * @return the ccId
	 */
	public Long getCcId() {
		return ccId;
	}

	/**
	 * @param ccId the ccId to set
	 */
	public void setCcId(Long ccId) {
		this.ccId = ccId;
	}

	/**
	 * @return the contactId
	 */
	public Long getContactId() {
		return contactId;
	}

	/**
	 * @param contactId the contactId to set
	 */
	public void setContactId(Long contactId) {
		this.contactId = contactId;
	}

	/**
	 * @return the createListingBody
	 */
	public List<ListingRequest> getCreateListingBody() {
		return requestsBody;
	}

	/**
	 * @param createListingBody the createListingBody to set
	 */
	public void setCreateListingBody(List<ListingRequest> requestsBody) {
		this.requestsBody = requestsBody;
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
	
	public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    /**
	 * @return the groupId
	 */
	public Long getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the assertion
	 */
	public String getAssertion() {
		return assertion;
	}

	/**
	 * @param assertion the assertion to set
	 */
	public void setAssertion(String assertion) {
		this.assertion = assertion;
	}

	public String getSellerGuid() {
		return sellerGuid;
	}

	public void setSellerGuid(String sellerGuid) {
		this.sellerGuid = sellerGuid;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public String getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(String subscriber) {
		this.subscriber = subscriber;
	}
	
}
