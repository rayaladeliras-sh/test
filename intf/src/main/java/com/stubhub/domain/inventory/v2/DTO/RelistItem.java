package com.stubhub.domain.inventory.v2.DTO;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.MoreObjects;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonRootName;

@XmlRootElement(name = "items")
@XmlType(name = "", propOrder = {"itemId"})
@JsonRootName(value = "items")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RelistItem {
  @XmlElement(name = "itemId", required = true)
  private String itemId;


  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("itemId", itemId)
            .toString();
  }
}