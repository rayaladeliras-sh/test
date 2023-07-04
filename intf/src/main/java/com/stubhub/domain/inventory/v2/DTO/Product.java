package com.stubhub.domain.inventory.v2.DTO;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.stubhub.domain.inventory.v2.enums.InventoryType;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.domain.inventory.v2.enums.TicketSeatStatusEnum;
import com.stubhub.newplatform.common.entity.Money;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "product")
@XmlType(name = "", propOrder = {"seatId", "row", "seat", "seatStatus", "fulfillmentArtifact",
    "operation", "productType", "externalId", "ga", "faceValue", "uniqueTicketNumber", "inventoryType", "inventoryTypeId"})
public class Product implements Serializable {

  // SELLAPI-1135 sonar-rules, Non-transient non-serializable instance field in serializable class
  private static final long serialVersionUID = 1298592812628279904L;

  @XmlElement(name = "seatId", required = false)
  private Long seatId;

  @XmlElement(name = "row", required = false)
  private String row;

  @XmlElement(name = "seat", required = false)
  private String seat;

  @XmlElement(name = "fulfillmentArtifact", required = false)
  private String fulfillmentArtifact;

  @XmlElement(name = "operation", required = false)
  @JsonDeserialize(using = OperationDeserializer.class)
  private Operation operation = Operation.ADD;

  @XmlElement(name = "productType", required = false)
  @JsonDeserialize(using = ProductTypeDeserializer.class)
  private ProductType productType = ProductType.TICKET;

  @XmlElement(name = "externalId", required = false)
  private String externalId;

  @XmlElement(name = "ga", required = false)
  private Boolean ga;

  @XmlElement(name = "seatStatus", required = false)
  private TicketSeatStatusEnum seatStatus;
  
  @XmlElement(name = "faceValue", required = false)
  private Money faceValue;
	
  @XmlElement(name = "uniqueTicketNumber", required = false)
  private String uniqueTicketNumber;
  
  @XmlElement(name = "inventoryType", required = false)
  @JsonDeserialize(using = InventoryTypeDeserializer.class)
  private InventoryType inventoryType;
  
  @XmlElement(name = "inventoryTypeId", required = false)
  private Long inventoryTypeId;

  public Boolean getGa() {
    return ga;
  }

  public void setGa(Boolean ga) {
    this.ga = ga;
  }

  public String getSeat() {
    return seat;
  }

  public void setSeat(String seat) {
    this.seat = seat;
    if ("Parking Pass".equalsIgnoreCase(seat)) {
      this.productType = ProductType.PARKING_PASS;
    }
  }

  public String getRow() {
    return row;
  }

  public void setRow(String row) {
    this.row = row;
  }

  public String getFulfillmentArtifact() {
    return fulfillmentArtifact;
  }

  public void setFulfillmentArtifact(String fulfillmentArtifact) {
    this.fulfillmentArtifact = fulfillmentArtifact;
  }

  public Operation getOperation() {
    return operation;
  }

  public void setOperation(Operation operation) {
    this.operation = operation;
  }

  public ProductType getProductType() {
    return productType;
  }

  public void setProductType(ProductType productType) {
    this.productType = productType;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  /**
   * @return the seatId
   */
  public Long getSeatId() {
    return seatId;
  }

  /**
   * @param seatId the seatId to set
   */
  public void setSeatId(Long seatId) {
    this.seatId = seatId;
  }

  public TicketSeatStatusEnum getSeatStatus() {
    return seatStatus;
  }

  public void setSeatStatus(TicketSeatStatusEnum seatStatus) {
    this.seatStatus = seatStatus;
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

  public InventoryType getInventoryType() {
    return inventoryType;
  }

  public void setInventoryType(InventoryType inventoryType) {
    this.inventoryType = inventoryType;
  }

  public Long getInventoryTypeId() {
    return inventoryTypeId;
  }

  public void setInventoryTypeId(Long inventoryTypeId) {
    this.inventoryTypeId = inventoryTypeId;
  }
  
}
