package com.stubhub.domain.inventory.listings.v2.entity;

import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.newplatform.common.entity.Money;

/**
 * Class to contain information about Product related to seat information 
 * (which can be ticket, parking_pass or other product types). 
 * 
 * @author sadranly
 */
public class SeatProduct {
	private String row;
	private String seat;
	private String fulfillmentArtifact;
	private Long seatId;
	private long ptvTicketId;
	private boolean validated;
	private ErrorDetail errorDetail;
	private Operation operation; 
	private ProductType productType;
	private boolean isParkingPass;
	private String externalId;
	private String uniqueTicketNumber;
	private Money faceValue;
	private Long inventoryTypeId;

	public String getSeat() {
		return seat;
	}

	public void setSeat(String seat) {
		this.seat = seat;
	}

	public Long getSeatId() {
		return seatId;
	}

	public void setSeatId(Long seatId) {
		this.seatId = seatId;
	}

	public long getPtvTicketId() {
		return ptvTicketId;
	}

	public void setPtvTicketId(long ptvTicketId) {
		this.ptvTicketId = ptvTicketId;
	}

	public boolean isValidated() {
		return validated;
	}

	public void setValidated(boolean validated) {
		this.validated = validated;
	}

	public String getRow() {
		return row;
	}

	public void setRow(String row) {
		this.row = row;
	}

	public ErrorDetail getErrorDetail() {
		return errorDetail;
	}

	public void setErrorDetail(ErrorDetail errorDetail) {
		this.errorDetail = errorDetail;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation seatOperation) {
		this.operation = seatOperation;
	}

	public ProductType getProductType() {
		return productType;
	}

	public void setProductType(ProductType productType) {
		this.productType = productType;
		isParkingPass = ProductType.PARKING_PASS.equalsEnum(productType);
	}
	
	public boolean isParkingPass ()
	{
		return isParkingPass;
	}

	public String getFulfillmentArtifact() {
		return fulfillmentArtifact;
	}

	public void setFulfillmentArtifact(String fulfillmentArtifact) {
		this.fulfillmentArtifact = fulfillmentArtifact;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getUniqueTicketNumber() {
		return uniqueTicketNumber;
	}

	public void setUniqueTicketNumber(String uniqueTicketNumber) {
		this.uniqueTicketNumber = uniqueTicketNumber;
	}

	public Money getFaceValue() {
		return faceValue;
	}

	public void setFaceValue(Money faceValue) {
		this.faceValue = faceValue;
	}

    public Long getInventoryTypeId() {
        return inventoryTypeId;
    }

    public void setInventoryTypeId(Long inventoryTypeId) {
        this.inventoryTypeId = inventoryTypeId;
    }
	
}
