package com.stubhub.domain.inventory.v2.DTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonRootName;

import com.stubhub.newplatform.common.entity.Money;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ticket")
@XmlType(name = "", propOrder = {"id", "externalTicketSeatId", "section", "row", "seat", "price", "status", "fulfillmentArtifact"})
@JsonRootName(value = "ticket")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ticket {
	
	@XmlElement(name = "id")
	private Long id;
	
	@XmlElement(name = "externalTicketSeatId")
	private String externalTicketSeatId;
	
	@XmlElement(name = "section")
	private String section;

	@XmlElement(name = "row")
	private String row;

	@XmlElement(name = "seat")
	private String seat;
	
	@XmlElement(name = "price")
	private Money price;

	@XmlElement(name = "status")
	private String status;
	
	@XmlElement(name = "fulfillmentArtifact")
	private String fulfillmentArtifact;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getExternalTicketSeatId() {
		return externalTicketSeatId;
	}

	public void setExternalTicketSeatId(String externalTicketSeatId) {
		this.externalTicketSeatId = externalTicketSeatId;
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
	
	public Money getPrice() {
		return price;
	}

	public void setPrice(Money price) {
		this.price = price;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFulfillmentArtifact() {
		return fulfillmentArtifact;
	}

	public void setFulfillmentArtifact(String fulfillmentArtifact) {
		this.fulfillmentArtifact = fulfillmentArtifact;
	}


}
