package com.stubhub.domain.inventory.v2.DTO;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.MoreObjects;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonRootName;

import com.stubhub.domain.inventory.common.entity.ListingStatus;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "relistResponse")
@XmlType(name = "", propOrder = {"listings"})
@JsonRootName(value = "relistResponse")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RelistResponse {



  @XmlElement(name = "listings", required = true)
  private List<RelistListingResponse> listings;



  public List<RelistListingResponse> getListings() {
    return listings;
  }

  public void setListings(List<RelistListingResponse> listings) {
    this.listings = listings;
  }

  public void addListing(String listingId, ListingStatus status) {
    if (listings == null) {
      listings = new ArrayList<RelistListingResponse>();
    }
    listings.add(new RelistListingResponse(listingId, status));
  }


  @Override
  public String toString() {
    return "RelistRequest [\"listings=\"" + listings + "]";
  }


  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "listing")
  @XmlType(name = "", propOrder = {"listingId", "status"})
  @JsonRootName(value = "listing")
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class RelistListingResponse {

    public RelistListingResponse() {
    }

    public RelistListingResponse(String listingId, ListingStatus status) {
      super();
      this.listingId = listingId;
      this.status = status;
    }

    @XmlElement(name = "listingId", required = true)
    private String listingId;
    @XmlElement(name = "status", required = true)
    private ListingStatus status;

    public String getListingId() {
      return listingId;
    }

    public void setListingId(String listingId) {
      this.listingId = listingId;
    }

    public ListingStatus getStatus() {
      return status;
    }

    public void setStatus(ListingStatus status) {
      this.status = status;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
              .add("listingId", listingId)
              .add("status", status)
              .toString();
    }
  }


}
