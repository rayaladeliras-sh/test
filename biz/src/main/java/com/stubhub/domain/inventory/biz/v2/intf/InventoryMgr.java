package com.stubhub.domain.inventory.biz.v2.intf;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.solr.client.solrj.response.QueryResponse;

import com.stubhub.domain.inventory.common.util.ListingSearchCriteria;
import com.stubhub.domain.inventory.datamodel.entity.ExternalSystem;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TTOrder;


/**
 * 
 * @author vichalasani
 *
 */
public interface InventoryMgr {
	/**
	 * getListings connects to apache solr to get all the active/inactive listings for the given seller
	 * @param criteria
	 * @return
	 */
	public QueryResponse getListings(ListingSearchCriteria criteria);
	
	
	/**
	 * This method adds a listing to the database
	 * 
	 * @param listingId
	 * @return
	 */
	public Listing getListing(Long listingId);
	
	/**
	 * This method adds a listing to the database
	 *
	 * @param listingId
	 * @return
	 */
	public Listing getListing(Long listingId, Locale locale);

	/**
	 * Add listing in batch as one transaction
	 * @param listings
	 * @return
	 */
    public List<Listing> addListings (List<Listing> listings);
	
	/**
	 * This method adds a listing to the database
	 * 
	 * @param listing
	 * @return
	 */
	public Listing addListing(Listing listing);

	/**
	 * This method allows to update attributes of a listing
	 * 
	 * @param listing
	 * @return Listing
	 */
	public Listing updateListing(Listing listing);
	
	/**
	 * This method allows to update batch of listings
	 * 
	 * @param listing
	 * @return Listing
	 */
	public List<Listing> updateListings( List<Listing> listings);	
	
	/**
	 * 
	 * @param listing
	 * @return
	 */
	public Listing updateListingOnly(Listing listing);

	/**
	 * returns a listing which is ACTIVE or INACTIVE with the input externalListingId and sellerId
	 * 
	 * @param sellerId
	 * @param externalId
	 * @return
	 */
	public Listing getListingBySellerIdExternalIdAndStatus(Long sellerId, String externalId);
	
	/**
	 * returns a listing with the input externalListingId and sellerId
	 * 
	 * @param sellerId
	 * @param externalId
	 * @return
	 */
	public Listing getListingBySellerIdAndExternalId(Long sellerId, String externalId);

	/**
	 * 
	 * @param eventId
	 * @param section
	 * @param row
	 * @param seats
	 * @return
	 */
	public Listing findListing(Long eventId, String section, String row,
			String seats);

	/**
	 * 
	 * @param eventId
	 * @param section
	 * @param row
	 * @return
	 */
	public Listing findListingBySectionRow(Long eventId, String section,
			String row);
	/**
	 * 
	 * @param section
	 * @return
	 */
	public boolean hasSectionHadBadTerms(String section);

	/**
	 * 
	 * @param row
	 * @return
	 */
	public boolean hasRowHadBadTerms(String row);

	public Long getSectionId(Long venueConfigId, String sectionDesc, String rowDesc, String piggyBackRowDesc, int validate);
	
	public Long getUserAgentID(String userAgent);
	
	/**
	 * This method is used to decide if PDF Pending Review is valid status for a listing
	 * @param listingId
	 * @return
	 */
	public boolean isPDFPendingReviewAllowed(Long listingId);
	
	public List<Listing> getListings(List<Long> listingIds);
	
	//SELLAPI-1181 09/02/15 START
	public List<Listing> getListings(Long sellerId, List<String> externalIds);
	//SELLAPI-1181 09/02/15 END
	
	
	public TTOrder addTTOrder(TTOrder ttOrder);
	
	/**
	 * Method to pull all pending Flash listings in DB to predeliver the flash
	 * listing.
	 * 
	 * @param sellerId
	 *            - Seller who linked his flash account
	 * @return {@link List}{@linkplain <}{@link Listing}{@linkplain >}
	 */
	public List<Listing> getAllPendingFlashListings(Long sellerId);
	
	public HashMap<String,Boolean> isSeatsRequired(Long eventId);
	
	public List<Listing> getActiveListingsBySellerId(Long sellerId);
	
	public Listing updateSystemStatus(Listing listing) ;

	public Listing updateListingFraudStatus(Listing listing) ;
	
}
