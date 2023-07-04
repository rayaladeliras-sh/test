package com.stubhub.domain.inventory.v2.DTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.stubhub.newplatform.common.entity.Money;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "product")
@XmlType(name = "", propOrder = { "productId", "productType", "productStatus", "section", "row", "seat", "ga", "medium", "faceValue", "uniqueTicketNumber", "inventoryTypeId", "externalId"})
public class ProductDetail {
	@XmlElement(name="productId", required = false)
	private Long productId;
	
	@XmlElement(name="productType", required = false)
	private String productType;
	
	@XmlElement(name="productStatus", required = false)
	private String productStatus;
	
	@XmlElement(name = "section", required = false)
	private String section;
	
	@XmlElement(name = "row", required = false)
	private String row;
	
	@XmlElement(name="seat", required = false)
	private String seat;
	
	@XmlElement(name = "ga", required = false)
	private Boolean ga;
	
	@XmlElement(name = "medium", required = false)
	private String medium;
	
	@XmlElement(name = "faceValue", required = false)
	private Money faceValue;
		
	@XmlElement(name = "uniqueTicketNumber", required = false)
	private String uniqueTicketNumber;
	
	@XmlElement(name = "inventoryTypeId", required = false)
	private Long inventoryTypeId;
	
	@XmlElement(name = "externalId", required = false)
	private String externalId;

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}
	
	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}
	
	public String getProductStatus() {
		return productStatus;
	}

	public void setProductStatus(String productStatus) {
		this.productStatus = productStatus;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getRow() {
		return row;
	}

	public void setRow(String row) {
		this.row = row;
	}

	public String getSeat() {
		return seat;
	}

	public void setSeat(String seat) {
		this.seat = seat;
	}

	public Boolean getGa() {
		return ga;
	}

	public void setGa(Boolean ga) {
		this.ga = ga;
	}
	
	public String getMedium() {
		return medium;
	}

	public void setMedium(String medium) {
		this.medium = medium;
	}
	
	public Money getFaceValue() {
		return faceValue;
	}
	
	public void setFaceValue(Money faceValue) {
		this.faceValue = faceValue;
	}
	
	public String getUniqueTicketNumber() {
		return uniqueTicketNumber;
	}
	
	public void setUniqueTicketNumber(String uniqueTicketNumber) {
		this.uniqueTicketNumber = uniqueTicketNumber;
	}
	
	public Long getInventoryTypeId() {
		return inventoryTypeId;
	}

	public void setInventoryTypeId(Long inventoryTypeId) {
		this.inventoryTypeId = inventoryTypeId;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public String toString() {
		return "ProductDetail [productId=" + productId + ", productType="
				+ productType + ", productStatus=" + productStatus
				+ ", section=" + section + ", row=" + row + ", seat=" + seat
				+ ", ga=" + ga + ", medium=" + medium + ", faceValue=" + faceValue 
				+ ", uniqueTicketNumber="+ uniqueTicketNumber+ "]";
	}

}
