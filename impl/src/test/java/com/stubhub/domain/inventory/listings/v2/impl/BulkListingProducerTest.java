/**
 * 
 */
package com.stubhub.domain.inventory.listings.v2.impl;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr;
import com.stubhub.domain.inventory.datamodel.entity.BulkListingGroup;
import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.domain.inventory.listings.v2.bulk.util.BulkListingJobConsumer;
import com.stubhub.domain.inventory.listings.v2.bulk.util.BulkListingJobProducer;

/**
 * @author sjayaswal
 *
 */
public class BulkListingProducerTest extends SHInventoryTest {

	private BulkListingJobProducer bulkListingJobProducer;
	
	private BulkInventoryMgr bulkInventoryMgr;

	

	@BeforeTest
	public void setUp() throws Exception 
	{
		MockitoAnnotations.initMocks(this);
		bulkListingJobProducer = new BulkListingJobProducer();

		bulkInventoryMgr=Mockito.mock(BulkInventoryMgr.class);
		
		ReflectionTestUtils.setField(bulkListingJobProducer, "bulkInventoryMgr", bulkInventoryMgr);
		
		List<BulkListingGroup> bulkListingGroups= new ArrayList<BulkListingGroup>();
		BulkListingGroup bulkListingGroup = new BulkListingGroup();
		bulkListingGroup.setBulkListingGroupId(123456L);
		bulkListingGroups.add(bulkListingGroup);
		Mockito.when(bulkInventoryMgr.getAvailableGroups()).thenReturn(bulkListingGroups);
		
	}
	
	@Test
	public void testRun(){

		Thread t = new Thread(bulkListingJobProducer);
		t.start();
	}
}
