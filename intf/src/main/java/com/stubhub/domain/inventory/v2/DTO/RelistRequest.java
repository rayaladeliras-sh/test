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
@XmlRootElement(name = "relistRequest")
@XmlType(name = "", propOrder = {"listings"})
@JsonRootName(value = "relistRequest")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RelistRequest {



  @XmlElement(name = "listings", required = true)
  private List<RelistListing> listings;



  public List<RelistListing> getListings() {
    return listings;
  }

  public void setListings(List<RelistListing> listings) {
    this.listings = listings;
  }


  @Override
  public String toString() {
    return "RelistRequest [\"listings=\"" + listings + "]";
  }

}
