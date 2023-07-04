/**
 * 
 */
package com.stubhub.domain.inventory.listings.v2.impl;

import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.domain.inventory.listings.v2.bulk.util.BulkListingJobController;

/**
 * @author sjayaswal
 *
 */
public class BulkListingJobControllerTest extends SHInventoryTest{

	private BulkListingJobController bulkListingJobController;
	

	@BeforeTest
	public void setUp() throws Exception 
	{
		MockitoAnnotations.initMocks(this);
		bulkListingJobController = new BulkListingJobController();
		this.bulkListingJobController.init();

			
	}
	
	@Test
	public void testControllerStartup(){
		
	}
	
}
