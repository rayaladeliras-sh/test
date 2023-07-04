package com.stubhub.domain.inventory.listings.v2.helper.tasks;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.util.ListingException;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.domain.inventory.listings.v2.tasks.BusinessInfoUpdateTask;
import com.stubhub.domain.inventory.listings.v2.util.SellerHelper;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;

public class BusinessInfoUpdateTaskTest extends SHInventoryTest {
	protected SellerHelper sellerHelper;
	
	@BeforeTest
	public void setup() throws Exception
	{
		MockitoAnnotations.initMocks(this);
		sellerHelper = Mockito.mock(SellerHelper.class);
	}
	
	@Test
	public void testIsNotCreate () throws Exception {
		Listing listing = new Listing(); 
		listing.setBusinessId(123L);
		listing.setBusinessGuid("ABC");
		
		Listing currentListing = new Listing();
		
		SHAPIContext shapiContext = new SHAPIContext();
		
		BusinessInfoUpdateTask task = new BusinessInfoUpdateTask(listing, currentListing, sellerHelper, shapiContext);
		
		currentListing = task.call();
		
		Assert.assertNull(currentListing.getBusinessGuid());
	}
	
	@Test
	public void testCreatedAndSetSuccessful () throws Exception {
		Listing listing = new Listing(); 
		listing.setBusinessId(123L);
		listing.setBusinessGuid("ABC");
		
		Listing currentListing = new Listing();
		
		SHAPIContext shapiContext = new SHAPIContext();
		
		BusinessInfoUpdateTask task = new BusinessInfoUpdateTask(listing, currentListing, sellerHelper, shapiContext, true);
		
		currentListing = task.call();
		
		Assert.assertEquals(currentListing.getBusinessGuid(), "ABC");
	}
	
	@Test
	public void testThrowException() throws Exception {
		Listing listing = new Listing(); 
		listing.setBusinessId(123L);
		listing.setBusinessGuid("ABC");
		
		Listing currentListing = new Listing();
		
		SHAPIContext shapiContext = new SHAPIContext();
		
		Mockito.when(sellerHelper.addBusinessDetails(listing)).thenThrow(new ListingException("", null));
		
		BusinessInfoUpdateTask task = new BusinessInfoUpdateTask(listing, currentListing, sellerHelper, shapiContext, true);
		
		currentListing = task.call();
		
		Assert.assertNull(currentListing.getBusinessGuid());
	}
	
	@Test
	public void testIfNeedToRunTask () throws Exception {
		Listing listing = new Listing(); 
		listing.setBusinessId(123L);
		listing.setBusinessGuid("ABC");
		
		Listing currentListing = new Listing();
		
		SHAPIContext shapiContext = new SHAPIContext();
		
		BusinessInfoUpdateTask task = new BusinessInfoUpdateTask(listing, currentListing, sellerHelper, shapiContext);
		
		boolean result = task.ifNeedToRunTask();
		
		Assert.assertFalse(result);
	}
}
