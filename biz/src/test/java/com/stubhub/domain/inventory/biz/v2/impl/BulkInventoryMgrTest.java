/**
 * 
 */
package com.stubhub.domain.inventory.biz.v2.impl;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.mapping.Array;
import org.springframework.orm.hibernate3.HibernateTemplate;
import com.stubhub.domain.inventory.datamodel.dao.BulkJobDAO;
import com.stubhub.domain.inventory.datamodel.dao.BulkListingGroupDAO;
import com.stubhub.domain.inventory.datamodel.dao.BulkListingRequestDAO;
import com.stubhub.domain.inventory.datamodel.dao.impl.BulkJobDAOImpl;
import com.stubhub.domain.inventory.datamodel.dao.impl.BulkListingGroupDAOImpl;
import com.stubhub.domain.inventory.datamodel.dao.impl.BulkListingRequestDAOImpl;
import com.stubhub.domain.inventory.datamodel.entity.BulkJob;
import com.stubhub.domain.inventory.datamodel.entity.BulkListingGroup;
import com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest;
import com.stubhub.domain.inventory.datamodel.entity.enums.BulkStatus;

/**
 * @author sjayaswal
 *
 */
public class BulkInventoryMgrTest {

	private BulkInventoryMgrImpl bulkInventoryMgrImpl;
	
	private BulkJobDAO bulkJobDAO;
	private BulkListingGroupDAO bulkListingGroupDAO;
	private BulkListingRequestDAO bulkListingRequestDAO;
	private HibernateTemplate hibernateTemplate;
	private SessionFactory sessionFactory;
	private Session currentSession;
	private Query query;
	

	@BeforeMethod
	public void setUp(){
		bulkInventoryMgrImpl = new BulkInventoryMgrImpl();
		bulkJobDAO =  new BulkJobDAOImpl();
		bulkListingGroupDAO = new BulkListingGroupDAOImpl();
		bulkListingRequestDAO =new BulkListingRequestDAOImpl();
	
		hibernateTemplate = mock(HibernateTemplate.class);
		sessionFactory = mock(SessionFactory.class);
		currentSession = mock(Session.class);
		query = mock(Query.class);
		
		ReflectionTestUtils.setField(bulkJobDAO, "hibernateTemplate", hibernateTemplate);
		ReflectionTestUtils.setField(bulkListingGroupDAO, "hibernateTemplate", hibernateTemplate);
		ReflectionTestUtils.setField(bulkListingRequestDAO, "hibernateTemplate", hibernateTemplate);
		when(hibernateTemplate.getSessionFactory()).thenReturn(sessionFactory);
	    when(sessionFactory.getCurrentSession()).thenReturn(currentSession);
		ReflectionTestUtils.setField(bulkInventoryMgrImpl, "bulkJobDAO", bulkJobDAO);
		ReflectionTestUtils.setField(bulkInventoryMgrImpl, "bulkListingGroupDAO", bulkListingGroupDAO);
		ReflectionTestUtils.setField(bulkInventoryMgrImpl, "bulkListingRequestDAO", bulkListingRequestDAO);
	}
	
	@Test
	public void testBulkCreateJob(){
		BulkJob bulkJob = populateBulkJob();
		bulkInventoryMgrImpl.createJob(bulkJob);
	}
	
	@Test
	public void testCreateBulkListingGroup(){
		bulkInventoryMgrImpl.createBulkListingGroup(populateBulkListingGroup());
	}
	
	@Test
	public void testUpdateBulkListingGroup(){
		bulkInventoryMgrImpl.updateBulkListingGroup(populateBulkListingGroup());
	}
	

	@Test
	public void testCreateBulkListingRequests(){
		bulkInventoryMgrImpl.createBulkListingRequests(populateBulkListingRequests());
	}
	
	@Test
	public void testAllAvailableGroups(){
		bulkInventoryMgrImpl.getAvailableGroups();
	}
	
	@Test
	public void testGetBulkListingRequests(){
		bulkInventoryMgrImpl.getBulkListingRequests(5434343L);
	}
	
	@Test
	public void testGetAllJobs(){
		bulkInventoryMgrImpl.getAllJobsForSeller(12345L);
	}
	
	@Test
	public void testGetGroupById(){
		bulkInventoryMgrImpl.getGroupById(876675675L);
	}
	
	

	@Test
	public void testUpdateBulkListingRequests(){
		bulkInventoryMgrImpl.updateBulkListingRequests(populateBulkListingRequests());
	}
	
	
	@Test
	public void testBulkUpdateJob(){
		BulkJob bulkJob = populateBulkJob();
		bulkInventoryMgrImpl.updateJob(bulkJob);
	}
	
	@Test
	public void testGetJobBySellerId(){
		bulkInventoryMgrImpl.getPendingJobsBySellerId(621653652143L);
	}
	
	
	@Test
	public void testGetJobById(){
		bulkInventoryMgrImpl.getJobById(665L);
	}
	
	@Test 
	public void testGetGroupsByJobId(){
		
		bulkInventoryMgrImpl.getGroupsByJobId(67767L);
	}
	
	@Test
	public void testUpdateBulkListingGroups(){
		List<BulkListingGroup> bulkListingGroups = new ArrayList<BulkListingGroup>();
		bulkListingGroups.add(populateBulkListingGroup());
		bulkInventoryMgrImpl.updateBulkListingGroups(bulkListingGroups);
	}
	
	private List<BulkListingRequest> populateBulkListingRequests(){
		List<BulkListingRequest> bulkListingRequests = new ArrayList<BulkListingRequest>();
		BulkListingRequest bulkListingRequest = new BulkListingRequest();
		bulkListingRequest.setBulkListingGroupId(987654L);
		bulkListingRequest.setExternalListingId("someId");
		
		bulkListingRequests.add(bulkListingRequest);
		return bulkListingRequests;
		
	}
	
	private BulkListingGroup populateBulkListingGroup(){
		BulkListingGroup  bulkListingGroup = new BulkListingGroup();
		bulkListingGroup.setUserId(123L);
		bulkListingGroup.setBulkJobId(1234545L);
		bulkListingGroup.setBulkListingGroupId(98766L);
		return bulkListingGroup;
		
	}
	private BulkJob populateBulkJob(){
		BulkJob bulkJob = new BulkJob();
		bulkJob.setBulkStatusId(BulkStatus.TEMPORARY.getId());
		bulkJob.setUserId(123456789L);
		
		
		return bulkJob;
	}
}
