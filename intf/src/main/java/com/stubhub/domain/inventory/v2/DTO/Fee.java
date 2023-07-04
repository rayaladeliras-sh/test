package com.stubhub.domain.inventory.v2.DTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.stubhub.domain.inventory.v2.enums.FeeType;
import com.stubhub.newplatform.common.entity.Money;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"type", "description", "amount"})
@XmlRootElement(name = "fee")
public class Fee {
	
	@XmlElement(name = "type", required = false)
	private FeeType type;
	
	@XmlElement(name = "description", required = false)
	private String description;
	
	@XmlElement(name = "amount", required = false)
	private Money amount;

	public FeeType getType() {
		return type;
	}

	public void setType(FeeType type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Money getAmount() {
		return amount;
	}

	public void setAmount(Money amount) {
		this.amount = amount;
	}
}
