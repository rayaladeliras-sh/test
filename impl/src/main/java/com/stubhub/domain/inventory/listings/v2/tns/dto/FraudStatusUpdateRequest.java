package com.stubhub.domain.inventory.listings.v2.tns.dto;

public class FraudStatusUpdateRequest {

	private Long listingId;
	private Long fraudCheckStatusId;
	private String fraudCheckStatus;
	private Long sellerId;
	private Long fraudResolutionId;
	private boolean isSellerDeactivated;
	private int retryCount = 0;
//	private Long userDeactivationReasonId;

	public Long getListingId() {
		return listingId;
	}

	public void setListingId(Long listingId) {
		this.listingId = listingId;
	}

	public Long getFraudCheckStatusId() {
		return fraudCheckStatusId;
	}

	public void setFraudCheckStatusId(Long fraudCheckStatusId) {
		this.fraudCheckStatusId = fraudCheckStatusId;
	}

	public String getFraudCheckStatus() {
		return fraudCheckStatus;
	}

	public void setFraudCheckStatus(String fraudCheckStatus) {
		this.fraudCheckStatus = fraudCheckStatus;
	}

	public Long getSellerId() {
		return sellerId;
	}

	public void setSellerId(Long sellerId) {
		this.sellerId = sellerId;
	}

	public Long getFraudResolutionId() {
		return fraudResolutionId;
	}

	public void setFraudResolutionId(Long fraudResolutionId) {
		this.fraudResolutionId = fraudResolutionId;
	}

	public boolean isIsSellerDeactivated() {
		return isSellerDeactivated;
	}

	public void setIsSellerDeactivated(boolean isSellerDeactivated) {
		this.isSellerDeactivated = isSellerDeactivated;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

//	public Long getUserDeactivationReasonId() {
//		return userDeactivationReasonId;
//	}
//
//	public void setUserDeactivationReasonId(Long userDeactivationReasonId) {
//		this.userDeactivationReasonId = userDeactivationReasonId;
//	}

}
