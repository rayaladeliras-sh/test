package com.stubhub.domain.inventory.biz.v2.intf;

import java.util.List;

import com.stubhub.domain.inventory.datamodel.entity.BulkJob;
import com.stubhub.domain.inventory.datamodel.entity.BulkListingGroup;
import com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest;

public interface BulkInventoryMgr {

	public void createJob(BulkJob bulkJob);
	
	public void updateJob(BulkJob bulkJob);
	
	public BulkJob getJobById(Long bulkJobId);
	
	public List<BulkJob> getPendingJobsBySellerId(Long sellerId);
	
	public void createBulkListingGroup(BulkListingGroup group);
	
	public void updateBulkListingGroup(BulkListingGroup group);
	
	public void createBulkListingRequests(List<BulkListingRequest> bulkListingRequests);
	
	public List<BulkListingGroup> getAvailableGroups();
	
	public List<BulkListingGroup> getGroupsByJobId(Long bulkJobId);
	
	public List<BulkListingRequest> getBulkListingRequests(Long bulkListingGroupId);
	
	public List<BulkJob> getAllJobsForSeller(Long sellerId);
	
	public BulkListingGroup getGroupById(Long bulkListingGroupId);
	
	public void updateBulkListingRequests(List<BulkListingRequest> bulkListingRequests);
	
	public void updateBulkListingGroups(List<BulkListingGroup>  bulkListingGroups);

	public List<BulkListingRequest> getJobStatuses(Long jobGuid);
}
