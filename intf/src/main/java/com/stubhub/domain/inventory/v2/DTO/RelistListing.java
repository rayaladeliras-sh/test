package com.stubhub.domain.inventory.v2.DTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonRootName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "listings")
@XmlType(name = "",
    propOrder = {"orderId", "pricePerItem", "splitOption", "splitQuantity", "items","toEmailId","toCustomerGUID","autoPricingEnabledInd"})
@JsonRootName(value = "listings")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RelistListing {

  @XmlElement(name = "orderId", required = true)
  private Long orderId;

  @XmlElement(name = "pricePerItem", required = false)
  private BigDecimal pricePerItem;

  @XmlElement(name = "splitOption", required = false)
  private Integer splitOption;

  @XmlElement(name = "splitQuantity", required = false)
  private Integer splitQuantity;

  @XmlElement(name = "items", required = false)
  private List<RelistItem> items;
  
  @XmlElement(name = "toEmailId", required = false)
  private String toEmailId;
  
  @XmlElement(name = "toCustomerGUID", required = false)
  private String toCustomerGUID;
  
  @XmlElement(name = "autoPricingEnabledInd", required = false)
  private Boolean autoPricingEnabledInd;

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public BigDecimal getPricePerItem() {
    return pricePerItem;
  }

  public void setPricePerItem(BigDecimal pricePerItem) {
    this.pricePerItem = pricePerItem;
  }

  public Integer getSplitOption() {
    return splitOption;
  }

  public void setSplitOption(Integer splitOption) {
    this.splitOption = splitOption;
  }

  public Integer getSplitQuantity() {
    return splitQuantity;
  }

  public void setSplitQuantity(Integer splitQuantity) {
    this.splitQuantity = splitQuantity;
  }

  public List<RelistItem> getItems() {
    return items;
  }

  public void setItems(List<RelistItem> items) {
    this.items = items;
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

public Boolean isAutoPricingEnabledInd() {
	return autoPricingEnabledInd;
}

public void setAutoPricingEnabledInd(Boolean autoPricingEnabledInd) {
	this.autoPricingEnabledInd = autoPricingEnabledInd;
}

public void addItem(String itemId) {
    if (items == null) {
      items = new ArrayList<RelistItem>();
    }
    RelistItem item = new RelistItem();
    item.setItemId(itemId);
    items.add(item);
  }

@Override
public String toString() {
	return "RelistListing [orderId=" + orderId + ", pricePerItem=" + pricePerItem + ", splitOption=" + splitOption
			+ ", splitQuantity=" + splitQuantity + ", items=" + items + ", toEmailId=" + toEmailId + ", toCustomerGUID="
			+ toCustomerGUID + ", autoPricingEnabledInd=" + autoPricingEnabledInd + "]";
}

}
