package com.stubhub.domain.inventory.listings.v2.helper.tasks;

import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.PaymentType;
import com.stubhub.domain.inventory.listings.v2.tasks.SellerCCIdUpdateTask;
import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;

public class SellerCCIdUpdateTaskTest extends SHInventoryTest
{
	@BeforeTest
	public void setup() throws Exception
	{
		MockitoAnnotations.initMocks(this);
	}
		
	@Test
	public void test_validSellerCCID () throws Exception
	{
		Listing dblisting = getListing(1000L, "section-10", "R1", "1,2,3,4,5", 5, 5);
		
		Listing newlisting = new Listing();
		newlisting.setId(1000l);
		newlisting.setSellerId(1000l);		
		newlisting.setSellerPaymentTypeId(PaymentType.Paypal.getType());
		newlisting.setSellerCCId(123456L);
		
		UserHelper userHelper =  Mockito.mock (UserHelper.class );
		when(userHelper.getMappedValidSellerCCId(Mockito.anyString(), Mockito.anyString(), 
				Mockito.any(List.class), Mockito.anyString())).thenReturn (new Long(123456L));
		
		SHAPIContext shapiContext = new SHAPIContext();
		SellerCCIdUpdateTask task = new SellerCCIdUpdateTask (newlisting, dblisting, shapiContext, userHelper);
		
		try {
			task.call();
		}
		catch ( ListingBusinessException ex ) {
			Assert.fail("Should not fail because seller's CC ID is valid");			
		}
	}	
	
	@Test
	public void test_validSellerBadCCID () throws Exception
	{
		Listing dblisting = getListing(1000L, "section-10", "R1", "1,2,3,4,5", 5, 5);
		
		Listing newlisting = new Listing();
		newlisting.setId(1000l);
		newlisting.setSellerId(1000l);		
		newlisting.setSellerPaymentTypeId(PaymentType.Paypal.getType());
		newlisting.setCcGuid("123456");
		
		UserHelper userHelper =  Mockito.mock (UserHelper.class );
		when(userHelper.getMappedValidSellerCCId(Mockito.anyString(), Mockito.anyString(), 
				Mockito.any(List.class), Mockito.anyString())).thenReturn (null);
		
		SHAPIContext shapiContext = new SHAPIContext();
		SellerCCIdUpdateTask task = new SellerCCIdUpdateTask (newlisting, dblisting, shapiContext, userHelper);
		
		try {
			task.call();
			Assert.fail("Should not succeed because seller's CC ID is NOT valid");	
		}
		catch ( ListingBusinessException ex ) {
		}
	}	
	
	
}

