package com.stubhub.domain.inventory.listings.v2.tasks;

import org.apache.log4j.Logger;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.PDFTicketMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.util.InventorySolrUtil;
import com.stubhub.domain.inventory.listings.v2.util.ListingSeatTraitsHelper;
import com.stubhub.domain.inventory.listings.v2.util.SeatProductsContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

public class LookupExternalIdTask implements CallableInventoryTask<Listing> 
{
	private InventoryMgr inventoryMgr;
	private Listing listing;
	private Listing currentListing;
	private SHAPIContext apiContext;
	private SeatProductsContext seatProdContext;
	private final Map<String, String> context;
	private static final Map<String, String> EMPTY_CONTEXT = new HashMap<>();

	private final static Logger log = Logger.getLogger(LookupExternalIdTask.class);

	public LookupExternalIdTask(Listing listing, Listing currentListing,
			SHAPIContext apiContext, SeatProductsContext seatProdContext,
			ListingSeatTraitsHelper listingSeatTraitsHelper,
			TicketSeatMgr ticketSeatMgr, InventoryMgr inventoryMgr,  
			PDFTicketMgr pdfTicketMgr, 
			InventorySolrUtil inventorySolrUtil ) {
		this.listing = listing;
		this.currentListing = currentListing;
		this.seatProdContext = seatProdContext;
		this.apiContext = apiContext;
		this.inventoryMgr = inventoryMgr;
		this.context = MDC.getCopyOfContextMap() != null ? MDC.getCopyOfContextMap() : EMPTY_CONTEXT;
	}

	@Override
	public Listing call() throws Exception {
		MDC.setContextMap(this.context);
		log.debug("START TicketSeatTraitUpdateTask async task");
		SHAPIThreadLocal.set(apiContext);

		if (listing.getExternalId() != null) {
			// if create make sure externalListingId is unique (only for ACTIVE and INACTIVE listings)
			if ( seatProdContext.isCreate() ) {
				Listing existingListing = inventoryMgr.getListingBySellerIdExternalIdAndStatus(
								listing.getSellerId(), listing.getExternalId());
				if (existingListing != null) {
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR,
							ErrorCode.DUPLICATE_EXTERNAL_LISTING_ID,
							"A listing with the same external listing ID already exists with stubhubListingId= "+existingListing.getId(),
							"externalListingId");
					throw new ListingBusinessException(listingError);
				}
				currentListing.setExternalId(listing.getExternalId());
			}
			else {	// if update make sure externalListingId is consistent
				if ( !listing.getExternalId().equalsIgnoreCase(currentListing.getExternalId() )) {
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR,
							ErrorCode.MISSING_EXTERNAL_LISTING_ID,
							"Passed externalListingId is not consistent with existing externalListingId: " + currentListing.getExternalId(),
							"externalListingId");
					throw new ListingBusinessException(listingError);
				}
			}
		}
		
		log.debug("END async task");
		return currentListing;
	}

	@Override
	public boolean ifNeedToRunTask() {
		return listing.getExternalId() != null ;
	}
}

