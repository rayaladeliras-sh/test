package com.stubhub.domain.inventory.v2.bulk.DTO;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.stubhub.domain.inventory.common.util.Response;

@XmlRootElement(name = "bulkJobResponse")
@JsonRootName(value = "bulkJobResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "status", "numberOfInputListings",
		"numberOfProcessedListings", "listings" })
public class BulkJobResponse extends Response {

	@XmlElement(name = "jobGuid", required = true)
	private Long jobGuid;

	@XmlElement(name = "status", required = true)
	private String status;

	private Integer numberOfProcessedListings;

	private Integer numberOfInputListings;

	@XmlElement(name = "listings")
	private List<ListingResponse> listings;

	/**
	 * @return the jobGuid
	 */
	public Long getJobGuid() {
		return jobGuid;
	}

	/**
	 * @param jobGuid the jobGuid to set
	 */
	public void setJobGuid(Long jobGuid) {
		this.jobGuid = jobGuid;
	}

	/**
	 * @return the listingResponse
	 */
	public List<ListingResponse> getListings() {
		return listings;
	}

	/**
	 * @param listingResponse the listingResponse to set
	 */
	public void setListings(List<ListingResponse> listingResponses) {
		this.listings = listingResponses;
	}

	public String getStatus() {
		return status;
	}

	/**
	 * @param jobStatus the jobStatus to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getNumberOfProcessedListings() {
		return numberOfProcessedListings;
	}

	public void setNumberOfProcessedListings(Integer numberOfProcessedListings) {
		this.numberOfProcessedListings = numberOfProcessedListings;
	}

	public Integer getNumberOfInputListings() {
		return numberOfInputListings;
	}

	public void setNumberOfInputListings(Integer numberOfInputListings) {
		this.numberOfInputListings = numberOfInputListings;
	}

}
