package com.stubhub.domain.inventory.listings.v2.entity;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.ObjectUtils;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
	"orderId",
	"orderItemToSeatMap",
	"toEmailId",
	"toCustomerGUID",
	"listingId",
	"paymentType"
})
@XmlRootElement(name = "eventMessage")
public class SharedWithFriendMessage implements Serializable {

	
	private static final long serialVersionUID = 5285961456947347052L;
	
	@XmlElement(name = "orderId", required = true)
	private Long orderId;
	@XmlElement(name = "ticketList", required = false)
	private List<Map<String, String>> orderItemToSeatMap;
	@XmlElement(name = "toEmailId", required = true)
	private String toEmailId;
	@XmlElement(name = "toCustomerGUID", required = false)
	private String toCustomerGUID;
	@XmlElement(name = "listingId", required = true)
	private String listingId;
	@XmlElement(name = "paymentType", required = true)
	private String paymentType;
	
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public List<Map<String, String>> getOrderItemToSeatMap() {
		return Collections.unmodifiableList(orderItemToSeatMap);
	}
	public void setOrderItemToSeatMap(List<Map<String, String>> orderItemToSeatMap) {
		this.orderItemToSeatMap = org.apache.commons.lang3.ObjectUtils.clone(orderItemToSeatMap);
	}
	public String getToEmailId() {
		return toEmailId;
	}
	public void setToEmailId(String toEmailId) {
		this.toEmailId = toEmailId;
	}
	public String getToCustomerGUID() {
		return toCustomerGUID;
	}
	public void setToCustomerGUID(String toCustomerGUID) {
		this.toCustomerGUID = toCustomerGUID;
	}
	public String getListingId() {
		return listingId;
	}
	public void setListingId(String listingId) {
		this.listingId = listingId;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	//TODO modify it as per json format and use it jmshelper class to print the json message
	@Override
	public String toString() {
		return "SharedWithFriendMessage [orderId=" + orderId + ", orderItemToSeatMap=" + orderItemToSeatMap
				+ ", toEmailId=" + toEmailId + ", toCustomerGUID=" + toCustomerGUID + ", listingId=" + listingId
				+ ", paymentType=" + paymentType + "]";
	}
	
	
	
	
}
