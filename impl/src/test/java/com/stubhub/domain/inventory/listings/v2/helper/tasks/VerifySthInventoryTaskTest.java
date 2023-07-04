package com.stubhub.domain.inventory.listings.v2.helper.tasks;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.listings.v2.tasks.VerifySthInventoryTask;
import com.stubhub.domain.inventory.listings.v2.util.PrimaryIntegrationUtil;
import com.stubhub.domain.inventory.listings.v2.util.SeatProductsContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;

public class VerifySthInventoryTaskTest {
	
	private PrimaryIntegrationUtil primaryIntegrationUtil;
	
	@BeforeTest
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		primaryIntegrationUtil = Mockito.mock(PrimaryIntegrationUtil.class);
	}
		
	@Test
	public void testVerifySuccess() throws Exception {			
		SHAPIContext shapiContext = new SHAPIContext();
		List<SeatProductsContext> seatProdContexts = new ArrayList<SeatProductsContext>();
		when(primaryIntegrationUtil.verifySthInventory(seatProdContexts)).thenReturn (null);
		VerifySthInventoryTask task = new VerifySthInventoryTask (seatProdContexts, shapiContext, primaryIntegrationUtil);

		Assert.assertTrue(task.call() == seatProdContexts);			
	}	
	
	@Test(expectedExceptions = ListingBusinessException.class)
	public void testVerifyFail() throws Exception {
		SHAPIContext shapiContext = new SHAPIContext();
		List<SeatProductsContext> seatProdContexts = new ArrayList<SeatProductsContext>();
		when(primaryIntegrationUtil.verifySthInventory(seatProdContexts)).thenReturn ("Error");
		VerifySthInventoryTask task = new VerifySthInventoryTask (seatProdContexts, shapiContext, primaryIntegrationUtil);

		task.call();
	}	
	
}

