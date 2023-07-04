package com.stubhub.domain.inventory.v2.bulk.DTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.util.Response;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "jobGuid",
		"status","listings"
		})
public class ListingResponse extends Response{

	@XmlElement(name = "externalListingId", required = true)
	private String externalListingId;

	@XmlElement(name = "listingId")
	private Long listingId;
	
	@XmlElement(name = "status", required = true)
	private ListingStatus status;

	/**
	 * @return the externalListingId
	 */
	public String getExternalListingId() {
		return externalListingId;
	}

	/**
	 * @param externalListingId the externalListingId to set
	 */
	public void setExternalListingId(String externalListingId) {
		this.externalListingId = externalListingId;
	}

	/**
	 * @return the listingId
	 */
	public Long getListingId() {
		return listingId;
	}

	/**
	 * @param listingId the listingId to set
	 */
	public void setListingId(Long listingId) {
		this.listingId = listingId;
	}

	/**
	 * @return the listingStatus
	 */
	public ListingStatus getStatus() {
		return status;
	}

	/**
	 * @param listingStatus the listingStatus to set
	 */
	public void setStatus(ListingStatus status) {
		this.status = status;
	}
	
	
}
