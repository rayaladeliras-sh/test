package com.stubhub.domain.inventory.v2.DTO;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonRootName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "externalOrderResponse")
@XmlType(name = "", propOrder = { "orderId", "listingId", "externalListingId", "endTime", "deleteInventory", "tickets","shoppingCartGuid", "saleType","autoConfirm"})
@JsonRootName(value = "externalOrderResponse")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalOrderResponse {
	
	@XmlElement(name = "orderId")
	private Long orderId;
	
	@XmlElement(name = "listingId")
	private Long listingId;
	
	@XmlElement(name = "externalListingId")
	private String externalListingId;
	
	@XmlElement(name = "endTime")
	private String endTime;
	
	@XmlElement(name = "deleteInventory")
	private Boolean deleteInventory;
	
	@XmlElement(name = "tickets")
	private List<Ticket> tickets;
	
	@XmlElement(name = "shoppingCartGuid")
	private String shoppingCartGuid;
	
	@XmlElement(name = "saleType")
    private Long saleType = 1L;
	
	@XmlElement(name = "autoConfirm")
    private Boolean autoConfirm = Boolean.FALSE;

	public Boolean getAutoConfirm() {
    return autoConfirm;
  }

  public void setAutoConfirm(Boolean autoConfirm) {
    this.autoConfirm = autoConfirm;
  }

  public Long getSaleType() {
      return saleType;
    }

  public void setSaleType(Long saleType) {
    this.saleType = saleType;
  }

  public String getShoppingCartGuid() {
      return shoppingCartGuid;
    }

  public void setShoppingCartGuid(String shoppingCartGuid) {
    this.shoppingCartGuid = shoppingCartGuid;
  }

  public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getListingId() {
		return listingId;
	}

	public void setListingId(Long listingId) {
		this.listingId = listingId;
	}
	
	public String getExternalListingId() {
		return externalListingId;
	}

	public void setExternalListingId(String externalListingId) {
		this.externalListingId = externalListingId;
	}
	
	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public Boolean getDeleteInventory() {
		return deleteInventory;
	}

	public void setDeleteInventory(Boolean deleteInventory) {
		this.deleteInventory = deleteInventory;
	}

	public List<Ticket> getTickets() {
		return tickets;
	}

	public void setTickets(List<Ticket> tickets) {
		this.tickets = tickets;
	}

  @Override
  public String toString() {
    return "ExternalOrderResponse [orderId=" + orderId + ", listingId=" + listingId
        + ", externalListingId=" + externalListingId + ", shoppingCartGuid=" + shoppingCartGuid +
         "]";
  }
	
}
