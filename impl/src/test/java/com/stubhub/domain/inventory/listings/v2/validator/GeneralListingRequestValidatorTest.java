package com.stubhub.domain.inventory.listings.v2.validator;

import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.testng.annotations.BeforeMethod;

import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;

public class GeneralListingRequestValidatorTest extends SHInventoryTest {

	private static final Long EVENT_ID = 1L;
	@BeforeMethod
	public void setUp () throws Exception {
	}

//	@Test
	public void testExternalIdValidation () {
		ListingRequest req = new ListingRequest();
		
		req.setCcId( "1000" );
		req.setEventId("2000");
		
		//SELLAPI-1092 7/15/15 START
		ListingRequestValidator validator = new ListingRequestValidator();
		List<ListingError> errors = validator.validate(req, EVENT_ID, false, false, Locale.US, "",null, null, "US",null);
		//SELLAPI-1092 7/15/15 END
		
		if ( errors!=null && errors.size() > 0 ) {
			Assert.fail( "No reason for general validation to fail for single listing");
		}
		
		// test for bulk listing
		//SELLAPI-1092 7/15/15 START
		errors = validator.validate(req, EVENT_ID, true, false, Locale.US, "", null, null, "US",null);
		//SELLAPI-1092 7/15/15 END
		
		if ( errors==null || errors.size() == 0 ) {
			Assert.fail( "Validation should fail becase request does not have required field: externalListingId");
		}
	}

}
