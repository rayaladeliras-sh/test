package com.stubhub.domain.inventory.listings.v2.helper.tasks;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.controller.helper.IntegrationHelper;
import com.stubhub.domain.inventory.listings.v2.controller.helper.ListingHolder;
import com.stubhub.domain.inventory.listings.v2.tasks.HoldInventoryTask;
import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.domain.user.services.customers.v2.intf.GetCustomerResponse;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;

import junit.framework.Assert;

public class HoldInventoryTaskTest extends SHInventoryTest
{
	private IntegrationHelper integrationHelper;
	HoldInventoryTask holdInventoryTask;
	
	
	@BeforeTest
	public void setup() throws Exception
	{
		MockitoAnnotations.initMocks(this);
		integrationHelper = (IntegrationHelper)mockClass (IntegrationHelper.class,  null, null);
		
	}
	
	@Test
	public void callExternalTest() throws Exception{
		SHAPIContext context = new SHAPIContext();
		Listing testListing = new Listing();
		testListing.setSellerId(1L);
		List<TicketSeat> soldSeats = new ArrayList<TicketSeat>();		
		ListingHolder holder = new ListingHolder(testListing,soldSeats,123L,null);		
		HoldInventoryTask holdTask = new HoldInventoryTask(holder,integrationHelper,context); 
		GetCustomerResponse customerResponse = new GetCustomerResponse();
		customerResponse.setShipOrderINTOptIn(1L);	
		when(integrationHelper.getShipCustomer(Mockito.anyLong())).thenReturn(customerResponse);
		when(integrationHelper.reserveInventory(holder,customerResponse)).thenReturn(Boolean.TRUE);
		
		holder = holdTask.call();
		Assert.assertTrue(holder.isExternalCallSuccess());
		Assert.assertTrue(holdTask.ifNeedToRunTask());
		
	}
	
	@Test
	public void callNotExternalTest() throws Exception{
		SHAPIContext context = new SHAPIContext();
		Listing testListing = new Listing();
		testListing.setSellerId(1L);
		testListing.setIsETicket(false);
		List<TicketSeat> soldSeats = new ArrayList<TicketSeat>();		
		ListingHolder holder = new ListingHolder(testListing,soldSeats,123L,null);		
		HoldInventoryTask holdTask = new HoldInventoryTask(holder,integrationHelper,context); 
		GetCustomerResponse customerResponse = new GetCustomerResponse();
		customerResponse.setShipOrderINTOptIn(0L);	
		when(integrationHelper.getShipCustomer(Mockito.anyLong())).thenReturn(customerResponse);	
		holder = holdTask.call();
		Assert.assertFalse(holder.isExternal());		
		
	}
	
	
}