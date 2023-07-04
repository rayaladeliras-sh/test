package com.stubhub.domain.inventory.biz.v2.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.stubhub.domain.inventory.datamodel.dao.BulkUploadQueueDAO;
import com.stubhub.domain.inventory.datamodel.entity.BulkUploadQueue;

import junit.framework.Assert;

public class BulkUploadQueueMgrTest {

	@InjectMocks
	private BulkUploadQueueMgrImpl bulkUploadQueueMgr;

	@Mock
	private BulkUploadQueueDAO bulkUploadQueueDAO;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void validateUpdateBulkUploadQueue() {
		doNothing().when(bulkUploadQueueDAO).update(any(BulkUploadQueue.class));
		bulkUploadQueueMgr.updateBulkUploadQueue(new BulkUploadQueue());
	}

	@Test
	public void validateGetQueueById() {
		when(bulkUploadQueueDAO.getById(any(Long.class))).thenReturn(new BulkUploadQueue());
		BulkUploadQueue bulkUploadQueueReturned = bulkUploadQueueMgr.getQueueById(12345L);
		Assert.assertNotNull(bulkUploadQueueReturned);
	}

	@Test
	public void validateGetQueueBySellerIdAndStatus() {
		List<BulkUploadQueue> queues = new ArrayList<>();
		queues.add(new BulkUploadQueue());
		when(bulkUploadQueueDAO.getQueuesByStatusAndSellerId(any(Long.class), any(Long.class))).thenReturn(queues);
		List<BulkUploadQueue> queuesReturned = bulkUploadQueueMgr.getQueuesByStatusAndSellerId(12345L, 2000L);
		Assert.assertNotNull(queuesReturned);
		Assert.assertEquals(1, queuesReturned.size());
	}

}
