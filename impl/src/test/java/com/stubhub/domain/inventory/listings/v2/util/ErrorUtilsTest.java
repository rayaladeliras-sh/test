package com.stubhub.domain.inventory.listings.v2.util;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.listings.v2.util.ErrorUtils;

public class ErrorUtilsTest {
	
	@Test
	public void responsesFromErrorTest() {
		
		List<ListingRequest> requests = new ArrayList<ListingRequest> ();
		for (long i=0; i < 5; i++) {
			ListingRequest lreq = new ListingRequest();
			lreq.setListingId(i);
			lreq.setExternalListingId(null);
			requests.add(lreq);
		}
		
		for (long i=10; i < 15; i++) {
			ListingRequest lr = new ListingRequest();
			lr.setListingId(null);
			lr.setExternalListingId("" + i);
			requests.add(lr);
		}
		
		ListingError error = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.INVALID_CCID, "Seller credit card not found","sellerCCId");
		List<ListingResponse> lresp = ErrorUtils.responsesFromError (requests, error);
		Assert.assertEquals(lresp.size(), 10);
	}
	
	@Test
	public void respFromErrorTest01() {
		
		Listing listing = new Listing();
		listing.setId(1L);
		listing.setExternalId("100");
		ListingError error = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.INVALID_CCID, "Seller credit card not found","sellerCCId");
		ListingResponse lresp = ErrorUtils.respFromError (listing, error);
		Assert.assertNotNull(lresp);
		
	}
	
	@Test
	public void respFromErrorTest02() {
		
		Listing listing = new Listing();
		listing.setId(null);
		listing.setExternalId("100");
		ListingError error = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.INVALID_CCID, "Seller credit card not found","sellerCCId");
		ListingResponse lresp = ErrorUtils.respFromError (listing, error);
		Assert.assertNotNull(lresp);
		
	}
	
	@Test
	public void populateRespWithErrorsTest01() {
		ListingResponse lresp = new ListingResponse();
		String extListingId = "100";
		Long listingId = 1L;
		List<ListingError> errors = new ArrayList<ListingError>();
		ListingError error01 = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.INVALID_CCID, 
				"Seller credit card not found","sellerCCId");
		ListingError error02 = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.DUPLICATE_EXTERNAL_LISTING_ID, 
				"A listing with the same external listing ID already exists",
				"externalListingId");
		errors.add(error01);
		errors.add(error02);
		ErrorUtils.populateRespWithErrors (lresp, extListingId, errors, listingId);
		Assert.assertEquals("1", lresp.getId());
	}
	@Test
	public void populateRespWithErrorsTest02() {
		ListingResponse lresp = new ListingResponse();
		String extListingId = "100";
		List<ListingError> errors = new ArrayList<ListingError>();
		ListingError error01 = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.INVALID_CCID, 
				"Seller credit card not found","sellerCCId");
		ListingError error02 = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.DUPLICATE_EXTERNAL_LISTING_ID, 
				"A listing with the same external listing ID already exists",
				"externalListingId");
		errors.add(error01);
		errors.add(error02);
		ErrorUtils.populateRespWithErrors (lresp, extListingId, errors, null);
		Assert.assertEquals("E:100", lresp.getId());
	}

}
