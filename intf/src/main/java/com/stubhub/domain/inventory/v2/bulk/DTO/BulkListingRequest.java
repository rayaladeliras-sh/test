/**
 * 
 */
package com.stubhub.domain.inventory.v2.bulk.DTO;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.map.annotate.JsonRootName;

import com.stubhub.domain.inventory.v2.DTO.ListingRequest;


/**
 * @author sjayaswal
 *
 */
@XmlRootElement(name = "bulkListing")
@JsonRootName(value = "bulkListing")

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "listings" })
public class BulkListingRequest implements java.io.Serializable {

	
	@XmlElement(name = "listings", required = true)
	private List<ListingRequest> listings;

	/**
	 * @return the listings
	 */
	public List<ListingRequest> getListings() {
		return listings;
	}

	/**
	 * @param listings the listings to set
	 */
	public void setListings(List<ListingRequest> listings) {
		this.listings = listings;
	}
}
