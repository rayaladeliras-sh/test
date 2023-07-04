package com.stubhub.domain.inventory.biz.v2.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.stubhub.domain.inventory.biz.v2.intf.BulkUploadQueueMgr;
import com.stubhub.domain.inventory.datamodel.dao.BulkUploadQueueDAO;
import com.stubhub.domain.inventory.datamodel.entity.BulkUploadQueue;

@Component("bulkUploadMgr")
public class BulkUploadQueueMgrImpl implements BulkUploadQueueMgr {

	@Autowired
	private BulkUploadQueueDAO bulkUploadQueueDAO;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateBulkUploadQueue(BulkUploadQueue bulkUploadQueue) {
		bulkUploadQueueDAO.update(bulkUploadQueue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<BulkUploadQueue> getQueuesByStatusAndSellerId(Long sellerId, Long status) {
		return bulkUploadQueueDAO.getQueuesByStatusAndSellerId(sellerId, status);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public BulkUploadQueue getQueueById(Long queueId) {
		return bulkUploadQueueDAO.getById(queueId);
	}

}
