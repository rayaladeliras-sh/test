package com.stubhub.domain.inventory.biz.v2.intf;

import java.util.List;

import com.stubhub.domain.inventory.datamodel.entity.BulkUploadQueue;

/**
 * Biz interface to handle autobulk report listener.
 * 
 * @author rkesara
 *
 */
public interface BulkUploadQueueMgr {

	/**
	 * Update a bulkupload queue record with latest statistics.
	 * 
	 * @param bulkUploadQueue
	 *            {@link BulkUploadQueue} for update
	 */
	public void updateBulkUploadQueue(BulkUploadQueue bulkUploadQueue);

	/**
	 * Method to fetch pending queues for seller on a given status.
	 * 
	 * @param sellerId
	 *            SellerId
	 * @param status
	 *            Bulk upload queue status
	 * @return {@link List}{@linkplain<}{@link BulkUploadQueue}{@linkplain >}
	 *         records.
	 */
	public List<BulkUploadQueue> getQueuesByStatusAndSellerId(Long sellerId, Long status);

	/**
	 * Fetch a bulkupload queue record by queueId.
	 * 
	 * @param queueId
	 *            Bulkupload queueId
	 * @return {@link BulkUploadQueue}
	 */
	public BulkUploadQueue getQueueById(Long queueId);

}
