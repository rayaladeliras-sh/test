/**
 * 
 */
package com.stubhub.domain.inventory.biz.v2.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr;
import com.stubhub.domain.inventory.datamodel.dao.BulkJobDAO;
import com.stubhub.domain.inventory.datamodel.dao.BulkListingGroupDAO;
import com.stubhub.domain.inventory.datamodel.dao.BulkListingRequestDAO;
import com.stubhub.domain.inventory.datamodel.entity.BulkJob;
import com.stubhub.domain.inventory.datamodel.entity.BulkListingGroup;
import com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest;

/**
 * @author sjayaswal
 *
 */
@Component("bulkInventoryMgr")
public class BulkInventoryMgrImpl implements BulkInventoryMgr {

	
	@Autowired
	private BulkJobDAO bulkJobDAO;
	
	@Autowired
	private BulkListingGroupDAO bulkListingGroupDAO;

	@Autowired
	private BulkListingRequestDAO bulkListingRequestDAO;

	
	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr#createJob(com.stubhub.domain.inventory.datamodel.entity.BulkJob)
	 */
	@Override
	public void createJob(BulkJob bulkJob) {
		bulkJobDAO.insert(bulkJob);

	}

	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr#createBulkListingGroup(com.stubhub.domain.inventory.datamodel.entity.BulkListingGroup)
	 */
	@Override
	public void createBulkListingGroup(BulkListingGroup group) {
		bulkListingGroupDAO.insert(group);

	}

	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr#updateBulkListingGroup(com.stubhub.domain.inventory.datamodel.entity.BulkListingGroup)
	 */
	@Override
	public void updateBulkListingGroup(BulkListingGroup group) {
		bulkListingGroupDAO.update(group);
	}

	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr#createBulkListingRequests(java.util.List)
	 */
	@Override
	public void createBulkListingRequests(
			List<BulkListingRequest> bulkListingRequests) {
		bulkListingRequestDAO.persistAll(bulkListingRequests);

	}

	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr#getAvailableGroups()
	 */
	@Override
	public List<BulkListingGroup> getAvailableGroups() {
		return bulkListingGroupDAO.getAllGroups();
	}

	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr#getBulkListingRequests(java.lang.Long)
	 */
	@Override
	public List<BulkListingRequest> getBulkListingRequests(
			Long bulkListingGroupId) {
		return bulkListingRequestDAO.getByGroupId(bulkListingGroupId);
	}

	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr#getJobsBySellerId(java.lang.Long)
	 */
	@Override
	public List<BulkJob> getAllJobsForSeller(Long sellerId) {
		return bulkJobDAO.getAllJobsForSeller(sellerId);
	}

	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr#getGroupById(java.lang.Long)
	 */
	@Override
	public BulkListingGroup getGroupById(Long bulkListingGroupId) {
		return bulkListingGroupDAO.getById(bulkListingGroupId);

	}

	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr#updateBulkListingRequests(java.util.List)
	 */
	@Override
	public void updateBulkListingRequests(
			List<BulkListingRequest> bulkListingRequests) {
		bulkListingRequestDAO.update(bulkListingRequests);
		
	}

	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr#updateJob(com.stubhub.domain.inventory.datamodel.entity.BulkJob)
	 */
	@Override
	public void updateJob(BulkJob bulkJob) {
		bulkJobDAO.update(bulkJob);
		
	}

	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr#getJobBySellerId(java.lang.Long)
	 */
	@Override
	public List<BulkJob> getPendingJobsBySellerId(Long sellerId) {
		
		return bulkJobDAO.getPendingJobsBySellerId(sellerId);
	}

	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr#getJobById(java.lang.Long)
	 */
	@Override
	public BulkJob getJobById(Long bulkJobId) {
		
		return bulkJobDAO.getById(bulkJobId);
	}

	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr#getGroupsByJobId(java.lang.Long)
	 */
	@Override
	public List<BulkListingGroup> getGroupsByJobId(Long bulkJobId) {
		return bulkListingGroupDAO.getGroupsByJobId(bulkJobId);
	}

	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr#updateBulkListingGroups(java.util.List)
	 */
	@Override
	public void updateBulkListingGroups(List<BulkListingGroup> bulkListingGroups) {
		bulkListingGroupDAO.updateAll(bulkListingGroups);
		
	}

	@Override
	public List<BulkListingRequest> getJobStatuses(Long jobGuid) {
		return bulkListingRequestDAO.getByJobGuid(jobGuid);
	}

}
