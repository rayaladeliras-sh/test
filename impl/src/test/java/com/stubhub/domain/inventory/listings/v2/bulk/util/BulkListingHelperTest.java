/**
 * 
 */
package com.stubhub.domain.inventory.listings.v2.bulk.util;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.hamcrest.BaseMatcher;
import org.hibernate.StaleStateException;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.common.entity.ErrorEnum;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.JsonUtil;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.BulkJob;
import com.stubhub.domain.inventory.datamodel.entity.BulkListingGroup;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.BulkStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.BulkUploadType;
import com.stubhub.domain.inventory.listings.v2.helper.ListingCatalogSolrUtil;
import com.stubhub.domain.inventory.listings.v2.helper.ListingCreateProcess;
import com.stubhub.domain.inventory.listings.v2.util.ListingTextValidatorUtil;
import com.stubhub.domain.inventory.listings.v2.util.ResourceManager;
import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.domain.inventory.v2.DTO.EventInfo;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkJobResponse;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkJobStatusRequest;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingInternal;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingRequest;
import com.stubhub.newplatform.common.entity.Money;

/**
 * @author sjayaswal
 *
 */
public class BulkListingHelperTest  extends SHInventoryTest{

private BulkListingHelper bulkListingHelper;
	
	private BulkInventoryMgr bulkInventoryMgr;
	private InventoryMgr inventoryMgr;
	
	private ListingCreateProcess listingCreateProcess;
	
	private ListingCatalogSolrUtil listingCatalogSolrUtil;
	
	private Long sellerId = 123456L;
	private String sellerGuid="qwertpoiuy";
	private String subscriber="Bulk|V2|SHIP|DefaultApplication";
	private BulkListingRequest createBulkListingRequest;
	private BulkListingRequest updateBulkListingRequest;
	private String assertionHeader = "assertionHeader#client Ip# user Agent";
	private HttpHeaders headers;
	Map<String, com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest> bulkListingRequestMap = new HashMap<String, com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest>();

	//SELLAPI-1092 7/14/15 START
	ListingTextValidatorUtil listingTextValidatorUtil = new ListingTextValidatorUtil();
	ResourceManager rm = new ResourceManager();
	//SELLAPI-1092 7/14/15 END
	
	@BeforeMethod
	public void setUp() throws Exception 
	{
		MockitoAnnotations.initMocks(this);
		headers = (HttpHeaders) mockClass ( HttpHeaders.class, null, null);
		List<String> values = new ArrayList<String>();
		values.add("clientIp");
		Mockito.when(headers.getRequestHeader("X-FORWARDED-FOR")).thenReturn(values);
		MultivaluedMap<String, String> headersMap =  new MetadataMap<String, String>();
		headersMap.add(HttpHeaders.USER_AGENT, "userAgent");
		Mockito.when(headers.getRequestHeaders()).thenReturn(headersMap);
		
		bulkListingHelper = new BulkListingHelper();
		bulkInventoryMgr=Mockito.mock(BulkInventoryMgr.class);
		inventoryMgr=Mockito.mock(InventoryMgr.class);
		listingCreateProcess=Mockito.mock(ListingCreateProcess.class);
		listingCatalogSolrUtil=Mockito.mock(ListingCatalogSolrUtil.class);
		
		ReflectionTestUtils.setField(bulkListingHelper, "bulkInventoryMgr", bulkInventoryMgr);
		ReflectionTestUtils.setField(bulkListingHelper, "inventoryMgr", inventoryMgr);
		ReflectionTestUtils.setField(bulkListingHelper, "listingCreateProcess", listingCreateProcess);
		ReflectionTestUtils.setField(bulkListingHelper, "listingCatalogSolrUtil", listingCatalogSolrUtil);
		ReflectionTestUtils.setField(bulkListingHelper, "jmsTemplate", Mockito.mock(JmsTemplate.class));
		
		//SELLAPI-1092 7/14/15 START
		ReflectionTestUtils.setField(listingTextValidatorUtil, "resourceManager", rm);
		ReflectionTestUtils.setField(bulkListingHelper, "listingTextValidatorUtil", listingTextValidatorUtil);
		//SELLAPI-1092 7/14/15 END

		createBulkListingRequest = new BulkListingRequest();
		List<ListingRequest> listings = new ArrayList<ListingRequest>();
		ListingRequest listing=new ListingRequest();
		listing.setEventId("123456789");
		listing.setExternalListingId("98765");
		listing.setPricePerProduct(new Money("100"));
		listing.setSection("section");
		List<Product> products = new ArrayList<Product>();
		Product product = new Product();
		product.setRow("row1");
		product.setSeat("seat1");
		products.add(product);
		product = new Product();
		product.setRow("row1");
		product.setSeat("seat2");
		products.add(product);
		listing.setProducts(products);
		listings.add(listing);
		
		listing=new ListingRequest();
		listing.setEventId("123456789");
		listing.setExternalListingId("987651");
		listing.setPricePerProduct(new Money("100"));
		listing.setSection("section");
		products = new ArrayList<Product>();
		product = new Product();
		product.setRow("row2");
		product.setSeat("seat2");
		products.add(product);
		product = new Product();
		product.setRow("row3");
		product.setSeat("seat3");
		products.add(product);
		listing.setProducts(products);
		listings.add(listing);
		createBulkListingRequest.setListings(listings);
		
		updateBulkListingRequest = new BulkListingRequest();
		listings = new ArrayList<ListingRequest>();
		listing=new ListingRequest();
		listing.setEventId("123456789");
		listing.setListingId(123456L);
		listing.setExternalListingId("98765");
		listing.setPricePerProduct(new Money("100"));
		listing.setSection("section");
		products = new ArrayList<Product>();
		product = new Product();
		product.setRow("row1");
		product.setSeat("seat1");
		products.add(product);
		product = new Product();
		product.setRow("row1");
		product.setSeat("seat2");
		products.add(product);
		listing.setProducts(products);
		listings.add(listing);
		
		listing=new ListingRequest();
		listing.setEventId("123456789");
		listing.setListingId(1234567L);
		listing.setExternalListingId("987651");
		listing.setPricePerProduct(new Money("100"));
		listing.setSection("section");
		products = new ArrayList<Product>();
		product = new Product();
		product.setRow("row2");
		product.setSeat("seat2");
		products.add(product);
		product = new Product();
		product.setRow("row3");
		product.setSeat("seat3");
		products.add(product);
		listing.setProducts(products);
		listings.add(listing);
		updateBulkListingRequest.setListings(listings);
		
		BulkJob bulkJob = new BulkJob();
		bulkJob.setBulkJobId(100L);
		bulkJob.setBulkStatusId(2L);
		bulkJob.setUserId(123456L);
		Mockito.when(bulkInventoryMgr.getJobById(Mockito.anyLong())).thenReturn(bulkJob);
	}
	
	private List<BulkJob> populateBulkJob(){
		List<BulkJob> allJobs= new ArrayList<BulkJob>();

		BulkJob job = new BulkJob();
		job.setUserId(123456L);
		job.setBulkJobId(10L);
		job.setBulkStatusId(2L);
		
		allJobs.add(job);
		return allJobs;
		
	}
	@Test
	public void testBulkCreateListing_BulkStatusNull(){
		
		Mockito.when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(populateBulkJob());
		
		bulkListingHelper.bulkCreateListing(sellerId, sellerGuid, subscriber, null, createBulkListingRequest, assertionHeader, headers);
	}

	@Test(expectedExceptions={ListingBusinessException.class})
	public void testBulkCreateListing_ThrowException(){
		
		Mockito.when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(populateBulkJob());
		
		bulkListingHelper.bulkCreateListing(sellerId, sellerGuid, subscriber, null, getTooBigBulkListingRequest(), assertionHeader, headers);
	}

	@Test(expectedExceptions={ListingBusinessException.class})
	public void testBulkCreateListing_ThrowExceptionFailedUpdate(){
		
		Mockito.when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(populateBulkJob());
		Mockito.doThrow(Exception.class).when(bulkInventoryMgr).updateJob(Mockito.any(BulkJob.class));
		bulkListingHelper.bulkCreateListing(sellerId, sellerGuid, subscriber, null, getTooBigBulkListingRequest(), assertionHeader, headers);
	}

	private BulkListingRequest getTooBigBulkListingRequest() {
		BulkListingRequest bulkRequest = new BulkListingRequest();
		List<ListingRequest> listings = new ArrayList<ListingRequest>();
		ListingRequest listing=new ListingRequest();
		listing.setEventId("123456789");
		listing.setExternalListingId("98765");
		listing.setPricePerProduct(new Money("100"));
		listing.setSection("{\"listings\":[{\"eventId\":9177760,\"pricePerProduct\":\"12.00\",\"section\":\"SECTION-A\",\"deliveryOption\":\"STH\",\"status\":\"ACTIVE\",\"products\":[{\"row\":\"1\",\"seat\":\"1\",\"fulfillmentArtifact\":253292900,\"operation\":\"ADD\",\"productType\":\"ticket\"},{\"row\":\"1\",\"seat\":\"2\",\"fulfillmentArtifact\":253292901,\"operation\":\"ADD\",\"productType\":\"ticket\"},{\"row\":\"1\",\"seat\":\"3\",\"fulfillmentArtifact\":253292902,\"operation\":\"ADD\",\"productType\":\"ticket\"}],\"externalListingId\":\"STH_253292900_9177760_0.5438516478773306\",\"ccId\":\"7RZBb0R0bM4uz-Li\",\"paymentType\":1,\"splitOption\":\"none\",\"ticketTraits\":[{\"name\":\"Only seats in row\"},{\"name\":\"Aisle\"},{\"name\":\"Full Suite (not shared)\"},{\"name\":\"2 Adult and 1 Child Tickets (2 to 12 years old)\"},{\"name\":\"4 Adult and 4 Child tickets (2 to 12 years old)\"},{\"name\":\"Alcohol-free seating\"},{\"name\":\"Student ticket - ID required (25 and under)\"},{\"name\":\"ADAWheelchair accessible\"},{\"name\":\"VIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area PackageVIP Viewing Area Package\"},{\"name\":\"Wheelchair only\"},{\"name\":\"Partial view (printed on ticket)\"},{\"name\":\"Possible obstruction (printed on ticket)\"},{\"name\":\"Under 25s Ticket\"},{\"name\":\"Partial Suite (shared) - Reserved seating\"},{\"name\":\"Restricted Leg Room\"},{\"name\":\"Partial Suite Standing Room Only - Not a reserved seat\"},{\"name\":\"VIP Hot Ticket Package\"},{\"name\":\"21 and over section\"},{\"name\":\"Limited or obstructed view (printed on ticket)\"},{\"name\":\"Access to the Guinness Grandstand\"},{\"name\":\"Limited view of Jumbotron/video screen (printed on ticket)\"},{\"name\":\"VIP Viewing Area + Hot Ticket Package\"},{\"name\":\"Senior ticket (65 and older)\"},{\"name\":\"Ringmaster Zone - Pre-show experience with access meet performers and get autographs and receive a special family photo on the Ringling Red Carpet with circus celebrities\"},{\"name\":\"Seats behind railing above player's exit tunnel\"},{\"name\":\"Includes unlimited food and soft drinks\"},{\"name\":\"In-seat wait service\"},{\"name\":\"Last row of section\"},{\"name\":\"Directly behind the dugout\"},{\"name\":\"12-person suite\"},{\"name\":\"Includes unlimited food and drinks (beer, wine and liquor)\"},{\"name\":\"Includes unlimited food and drinks (beer, wine and soft drinks)\"},{\"name\":\"Actual 3rd row of section\"},{\"name\":\"Actual 4th row of section\"},{\"name\":\"Within 10 rows of home dugout\"},{\"name\":\"Within 10 rows of field\"},{\"name\":\"Bobblehead day\"},{\"name\":\"Within 10 rows of visitor dugout\"},{\"name\":\"14-person suite\"},{\"name\":\"1st row behind visiting team bullpen\"},{\"name\":\"Actual 5th row of section\"},{\"name\":\"20-person suite\"},{\"name\":\"VIP Experience - Access the VIP Suite via exclusive entrance starting 1 hour before show and during intermission with wines and hors-d?oeuvres, private restrooms, terrace, coat check service and includes a show program, souvenir photo and complimentary parking (show your ticket to the attendant)\"},{\"name\":\"Rail Lounge VIP Experience - includes VIP express festival entrance, access to Rail Lounge adjacent to stage, posh restroom facilities, private cash bar, crowd-free merchandise shopping, limited edition festival poster, massage services and festival concierge\"},{\"name\":\"1st row behind home team bullpen\"},{\"name\":\"Directly beside the dugout\"},{\"name\":\"Actual 2nd row of section\"},{\"name\":\"16-person suite\"},{\"name\":\"18-person suite\"},{\"name\":\"Full Suite - Includes private restroom\"},{\"name\":\"Actual 1st row of section\"},{\"name\":\"Behind protective net\"},{\"name\":\"All-you-can-eat section (includes food and non-alcoholic beverages)\"},{\"name\":\"Under overhang\"},{\"name\":\"Padded seats\"},{\"name\":\"Valet parking\"},{\"name\":\"Behind home plate\"}]}]}");
		List<Product> products = new ArrayList<Product>();
		Product product = new Product();
		product.setRow("row1");
		product.setSeat("seat1");
		products.add(product);
		product = new Product();
		product.setRow("row1");
		product.setSeat("seat2");
		products.add(product);
		listing.setProducts(products);
		listings.add(listing);
		bulkRequest.setListings(listings);
		return bulkRequest;
	}

	@Test(expectedExceptions={ListingBusinessException.class})
	public void testBulkCreateListing_ThrowExceptionEmptyRequest(){
		
		Mockito.when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(populateBulkJob());
		Mockito.doThrow(Exception.class).when(bulkInventoryMgr).updateJob(Mockito.any(BulkJob.class));
		bulkListingHelper.bulkCreateListing(sellerId, sellerGuid, subscriber, null, getEmptyBulkListingRequest(), assertionHeader, headers);
	}

	private BulkListingRequest getEmptyBulkListingRequest() {
		BulkListingRequest bulkRequest = new BulkListingRequest();
		List<ListingRequest> listings = new ArrayList<ListingRequest>();
		ListingRequest listing=new ListingRequest();
		listing.setEventId("123456789");
		listing.setExternalListingId("98765");
		listing.setPricePerProduct(new Money("100"));
		listing.setSection("{\"listings\":[]}");
		List<Product> products = new ArrayList<Product>();
		Product product = new Product();
		product.setRow("row1");
		product.setSeat("seat1");
		products.add(product);
		product = new Product();
		product.setRow("row1");
		product.setSeat("seat2");
		products.add(product);
		listing.setProducts(products);
		bulkRequest.setListings(listings);
		return bulkRequest;
	}

	@Test
	public void testBulkCreateListing_Headers(){
		
		Mockito.when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(populateBulkJob());
		
		bulkListingHelper.bulkCreateListing(sellerId, sellerGuid, subscriber, null, createBulkListingRequest, assertionHeader, headers);
	}
	
	
	@Test
	public void testBulkCreateListing_allJobsNull(){
		BulkListingHelper bulkListingHelperSpy = Mockito.spy(bulkListingHelper);
		BulkListingGroup group = new BulkListingGroup();
		group.setBulkListingGroupId(1l);;
		Mockito.doReturn(group).when(bulkListingHelperSpy).createGroup(anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), anyLong(), (BulkUploadType)Matchers.argThat(getMatcher()));

		
		Mockito.when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(null);
		BulkJob job = new BulkJob();
		bulkListingHelperSpy.bulkCreateListing(sellerId, sellerGuid, subscriber, null, createBulkListingRequest, assertionHeader, headers);
	}
	
	@Test
	public void testBulkCreateListing_BulkStatusNotNull(){
		BulkListingHelper bulkListingHelperSpy = Mockito.spy(bulkListingHelper);
		BulkListingGroup group = new BulkListingGroup();
		group.setBulkListingGroupId(1l);;
		Mockito.doReturn(group).when(bulkListingHelperSpy).createGroup(anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), anyLong(), (BulkUploadType)Matchers.argThat(getMatcher()));
		
		
		Mockito.when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(null);
		
		bulkListingHelperSpy.bulkCreateListing(sellerId, sellerGuid, subscriber, BulkStatus.CREATED, createBulkListingRequest, assertionHeader, headers);
	}
	
    private BaseMatcher getMatcher()
    {
        BaseMatcher matcher=new BaseMatcher() {
              @Override
              public boolean matches(Object item) {
                    return true;
              }

              @Override
              public void describeTo(org.hamcrest.Description description) {
              }

        };
        return matcher;
    }

	
	@Test
	public void testBulkCreateGroups_MoreThan50(){
		BulkListingHelper bulkListingHelperSpy = Mockito.spy(bulkListingHelper);
		BulkListingGroup group = new BulkListingGroup();
		group.setBulkListingGroupId(1l);;
		Mockito.doReturn(group).when(bulkListingHelperSpy).createGroup(anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), anyLong(), (BulkUploadType)Matchers.argThat(getMatcher()));

		
		Mockito.when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(null);
		BulkListingRequest createBulkListingTemp=new BulkListingRequest();
		createBulkListingTemp.setListings(populateListings(true,false, false, false));
		bulkListingHelperSpy.bulkCreateListing(sellerId, sellerGuid, subscriber, BulkStatus.CREATED, createBulkListingTemp, assertionHeader, headers);
	}
	
	
	@Test
	public void testBulkCreateGroups_mixed_errors(){
		
		BulkListingHelper bulkListingHelperSpy = Mockito.spy(bulkListingHelper);
		BulkListingGroup group = new BulkListingGroup();
		group.setBulkListingGroupId(1l);;

		Mockito.doReturn(group).when(bulkListingHelperSpy).createGroup(anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), anyLong(), (BulkUploadType)Matchers.argThat(getMatcher()));
		Mockito.when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(null);
		BulkListingRequest createBulkListingTemp=new BulkListingRequest();
		createBulkListingTemp.setListings(populateListings(false,false, true, false));
		bulkListingHelperSpy.bulkCreateListing(sellerId, sellerGuid, subscriber, BulkStatus.CREATED, createBulkListingTemp, assertionHeader, headers);
	}
	

	@Test
	public void testBulkCreateGroups_ExtListId_MaxLengthError(){
		
		Mockito.when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(null);
		BulkListingRequest createBulkListingTemp=new BulkListingRequest();
		createBulkListingTemp.setListings(populateListings(false,false, false, true));
		bulkListingHelper.bulkCreateListing(sellerId, sellerGuid, subscriber, BulkStatus.CREATED, createBulkListingTemp, assertionHeader, headers);
	}
	
	
	
	//SELLAPI-1135 10/22/15 START
	@Test 
	public void errorGroupMappingTest() {
		Map<String, List<BulkListingRequest>> groupedRequestMap = new HashMap <String, List<BulkListingRequest>>();
		ReflectionTestUtils.invokeMethod(bulkListingHelper, "errorGroupMapping", 
		groupedRequestMap, updateBulkListingRequest.getListings().get(0), ErrorCode.UNKNOWN_ERROR);
		Assert.assertTrue(groupedRequestMap.containsKey("Error"));
	}
	
	
	@Test (expectedExceptions=HibernateOptimisticLockingFailureException.class)
	public void testUpdateJob(){
		Long groupId = 98765L;
		BulkListingGroup bulkListingGroup = new BulkListingGroup();
		bulkListingGroup.setAssertion(assertionHeader);
		bulkListingGroup.setBulkJobId(100L);
		bulkListingGroup.setBulkListingGroupId(groupId);
		bulkListingGroup.setBulkStatusId(BulkStatus.ERROR.getId());
		bulkListingGroup.setUserId(sellerId);
		bulkListingGroup.setUserGuid(sellerGuid);
		bulkListingGroup.setMachineNode("machineNode");
		bulkListingGroup.setBulkUploadTypeId(BulkUploadType.CREATE.getId());
		Mockito.when(bulkInventoryMgr.getGroupById(groupId)).thenReturn(bulkListingGroup);
		BulkListingInternal bli = new BulkListingInternal();
		bli.setAssertion(assertionHeader);
		bli.setCcId(7654321L);
		bli.setContactId(987655434566L);
		bli.setCreateListingBody(populateListings(true, false, false, false));
		bli.setGroupId(98765L);
		bli.setSellerGuid(sellerGuid);
		bli.setSellerId(sellerId);
		Mockito.when(listingCreateProcess.createListings(any(BulkListingInternal.class), Mockito.anyString(),  Mockito.anyString())).thenReturn(populateListingResponse());
		List<BulkListingGroup> bulkListingGroups = new ArrayList<BulkListingGroup>();
		bulkListingGroups.add(bulkListingGroup);
		Mockito.when(bulkInventoryMgr.getGroupsByJobId(Mockito.anyLong())).thenReturn(bulkListingGroups);
		Mockito.when(bulkInventoryMgr.getBulkListingRequests(groupId)).thenReturn(populateBulkListingRequestEntity(false, false));
	
		Mockito.doThrow(new HibernateOptimisticLockingFailureException(new StaleStateException(""))).when(
		bulkInventoryMgr).updateJob(any(BulkJob.class));
		BulkJob bulkJob = new BulkJob();
		bulkJob.setBulkJobId(100L);
		bulkJob.setUserId(123456L);
		bulkJob.setBulkStatusId(2L); // not completed
		Mockito.when(bulkInventoryMgr.getJobById(Mockito.anyLong())).thenReturn(bulkJob);
	
		BulkJob bulkJob01 = new BulkJob();
		bulkJob01.setBulkJobId(101L);
		bulkJob01.setUserId(123457L);
		bulkJob01.setBulkStatusId(2L); // not completed
	
		ReflectionTestUtils.invokeMethod(bulkListingHelper, "updateJob", 
		sellerId, BulkStatus.COMPLETED, bulkJob01);
	}
	//SELLAPI-1135 10/22/15 END
	
	
	
	
	
	private List<ListingRequest> populateListings(boolean isEventId, boolean isEvent, boolean isMixed, boolean isExtListIdError){
		List<ListingRequest> listings = new ArrayList<ListingRequest>();
		ListingRequest listing;
		for(int i=0;i<=101;i++){
			listing=new ListingRequest();
			if(isEventId){ 
				listing.setEventId("123456789");
				listing.setExternalListingId("98765"+i);
			}
			if(isEvent){
				EventInfo event = new EventInfo();
				event.setName("Event name");
				event.setDate("03/23/2015");
				event.setVenue("Some Venue");
				event.setZipCode("33125");
				event.setCity("SomeCity");
				listing.setEvent(event);
				listing.setExternalListingId("98765"+i);
			}
			if(isMixed)
			{
				if(i<=50){
					listing.setEventId("123456789");
					listing.setExternalListingId("98765"+i);
				}else if(i<=75){
					listing.setEventId(null);
					listing.setExternalListingId("98765"+i);
				}else{
					listing.setEventId("123456789");
					listing.setExternalListingId(null);
				}
				
			}
			if(isExtListIdError){
				listing.setEventId("123456789");
				listing.setExternalListingId("98768888888888888888888888888888888888888888888888888888885"+i);
			}
			
			listing.setPricePerProduct(new Money("100"));
			listing.setSection("section"+i);
			List<Product> products = new ArrayList<Product>();
			Product product = new Product();
			product.setRow("row1");
			product.setSeat("seat1");
			products.add(product);
			product = new Product();
			product.setRow("row1");
			product.setSeat("seat2");
			products.add(product);
			listing.setProducts(products);
			listings.add(listing);
		}

		return listings;
	}
	
	@Test
	public void testBulkCreateGroups_NoEvent(){
		
		Mockito.when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(null);
		BulkListingRequest createBulkListingTemp=new BulkListingRequest();
		createBulkListingTemp.setListings(populateListings(false,false, false, false));
		bulkListingHelper.bulkCreateListing(sellerId, sellerGuid, subscriber, BulkStatus.CREATED, createBulkListingTemp, assertionHeader, headers);
	}
	
	@Test
	public void testBulkCreateGroups_WithEvent(){
		
		BulkListingHelper bulkListingHelperSpy = Mockito.spy(bulkListingHelper);
		BulkListingGroup group = new BulkListingGroup();
		group.setBulkListingGroupId(1l);;
		Mockito.doReturn(group).when(bulkListingHelperSpy).createGroup(anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), anyLong(), (BulkUploadType)Matchers.argThat(getMatcher()));
		
		
		Mockito.when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(null);
		BulkListingRequest createBulkListingTemp=new BulkListingRequest();
		createBulkListingTemp.setListings(populateListings(false,true, false, false));
		bulkListingHelperSpy.bulkCreateListing(sellerId, sellerGuid, subscriber, BulkStatus.CREATED, createBulkListingTemp, assertionHeader, headers);
	}
	
	
	@Test
	public void testProcessListingRequestByGroupId(){
		Long groupId = 98765L;
		
		BulkListingGroup bulkListingGroup = new BulkListingGroup();
		bulkListingGroup.setAssertion(assertionHeader);
		bulkListingGroup.setBulkJobId(100L);
		bulkListingGroup.setBulkListingGroupId(groupId);
		bulkListingGroup.setBulkStatusId(2L);
		bulkListingGroup.setUserId(sellerId);
		bulkListingGroup.setUserGuid(sellerGuid);
		bulkListingGroup.setMachineNode("machineNode");
		bulkListingGroup.setBulkUploadTypeId(BulkUploadType.CREATE.getId());
		Mockito.when(bulkInventoryMgr.getGroupById(groupId)).thenReturn(bulkListingGroup);
		
		BulkListingInternal bli = new BulkListingInternal();
		bli.setAssertion(assertionHeader);
		bli.setCcId(7654321L);
		bli.setContactId(987655434566L);
		bli.setCreateListingBody(populateListings(true, false, false, false));
		bli.setGroupId(98765L);
		bli.setSellerGuid(sellerGuid);
		bli.setSellerId(sellerId);
		Mockito.when(listingCreateProcess.createListings(any(BulkListingInternal.class), Mockito.anyString(),  Mockito.anyString())).thenReturn(populateListingResponse());
		
		List<BulkListingGroup> bulkListingGroups = new ArrayList<BulkListingGroup>();
		bulkListingGroups.add(bulkListingGroup);
		Mockito.when(bulkInventoryMgr.getGroupsByJobId(Mockito.anyLong())).thenReturn(bulkListingGroups);
		Mockito.when(bulkInventoryMgr.getBulkListingRequests(groupId)).thenReturn(populateBulkListingRequestEntity(false, false));
		
		bulkListingHelper.processListingRequestByGroupId(groupId, "machineNode", false);
	}
	
	@Test
	public void testProcessListingRequestByGroupId_update(){
		Long groupId = 98765L;
		
		BulkListingGroup bulkListingGroup = new BulkListingGroup();
		bulkListingGroup.setAssertion(assertionHeader);
		bulkListingGroup.setBulkJobId(100L);
		bulkListingGroup.setBulkListingGroupId(groupId);
		bulkListingGroup.setBulkStatusId(2L);
		bulkListingGroup.setUserId(sellerId);
		bulkListingGroup.setUserGuid(sellerGuid);
		bulkListingGroup.setMachineNode("machineNode");
		bulkListingGroup.setBulkUploadTypeId(BulkUploadType.UPDATE.getId());
		Mockito.when(bulkInventoryMgr.getGroupById(groupId)).thenReturn(bulkListingGroup);
		
		BulkListingInternal bli = new BulkListingInternal();
		bli.setAssertion(assertionHeader);
		bli.setCcId(7654321L);
		bli.setContactId(987655434566L);
		bli.setCreateListingBody(populateListings(true, false, false, false));
		bli.setGroupId(98765L);
		bli.setSellerGuid(sellerGuid);
		bli.setSellerId(sellerId);
		Mockito.when(listingCreateProcess.createListings(any(BulkListingInternal.class), Mockito.anyString(),  Mockito.anyString())).thenReturn(populateListingResponse());
		
		List<BulkListingGroup> bulkListingGroups = new ArrayList<BulkListingGroup>();
		bulkListingGroups.add(bulkListingGroup);
		Mockito.when(bulkInventoryMgr.getGroupsByJobId(Mockito.anyLong())).thenReturn(bulkListingGroups);
		Mockito.when(bulkInventoryMgr.getBulkListingRequests(groupId)).thenReturn(populateBulkListingRequestEntity(false, false));
		
		bulkListingHelper.processListingRequestByGroupId(groupId, "machineNode", false);
	}
	
	@Test
	public void testProcessListingRequestByGroupId_retry_AlreadyProcessed(){
		Long groupId = 98765L;
		
		BulkListingGroup bulkListingGroup = new BulkListingGroup();
		bulkListingGroup.setAssertion(assertionHeader);
		bulkListingGroup.setBulkJobId(100L);
		bulkListingGroup.setBulkListingGroupId(groupId);
		bulkListingGroup.setBulkStatusId(3L);
		bulkListingGroup.setUserId(sellerId);
		bulkListingGroup.setUserGuid(sellerGuid);
		bulkListingGroup.setMachineNode("machineNode");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis()-600000);
		
		bulkListingGroup.setLastUpdatedDate(cal);
		Mockito.when(bulkInventoryMgr.getGroupById(groupId)).thenReturn(bulkListingGroup);
		
		BulkListingInternal bli = new BulkListingInternal();
		bli.setAssertion(assertionHeader);
		bli.setCcId(7654321L);
		bli.setContactId(987655434566L);
		
		List<ListingRequest> listings = new ArrayList<ListingRequest>();
		ListingRequest listing= new ListingRequest();
		listing.setEventId("768723648732");
		listing.setExternalListingId("98765432");
		listing.setPricePerProduct(new Money("100"));
		listing.setListingId(123456L);
		listing.setSection("section");
		List<Product> products = new ArrayList<Product>();
		Product product = new Product();
		product.setRow("row1");
		product.setSeat("seat1");
		products.add(product);
		product = new Product();
		product.setRow("row1");
		product.setSeat("seat2");
		products.add(product);
		listing.setProducts(products);
		
		listings.add(listing);
		
		
		
		bli.setCreateListingBody(listings);
		bli.setGroupId(98765L);
		bli.setSellerGuid(sellerGuid);
		bli.setSellerId(sellerId);
		Mockito.when(listingCreateProcess.createListings(any(BulkListingInternal.class), Mockito.anyString(),  Mockito.anyString())).thenReturn(populateListingResponse());
		
		List<BulkListingGroup> bulkListingGroups = new ArrayList<BulkListingGroup>();
		bulkListingGroups.add(bulkListingGroup);
		Mockito.when(bulkInventoryMgr.getGroupsByJobId(Mockito.anyLong())).thenReturn(bulkListingGroups);
		
		List<com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest> bulkListingRequests = new ArrayList<com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest>();
		com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest bulkListingRequestEntity = new com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest();
		bulkListingRequestEntity.setExternalListingId("728hkjhkjwe");
		bulkListingRequestEntity.setBulkListingGroupId(98765L);
		bulkListingRequestEntity.setBulkListingRequestId(123L);
		String listingRequestClob = "{\"faceValue\":null,\"pricePerProduct\":{\"amount\":100.00,\"currency\":\"USD\"},\"purchasePrice\":null,\"buyerSeesPerProduct\":null,\"payoutPerProduct\":null,\"quantity\":null,\"inhandDate\":null,\"saleEndDate\":\"2014-06-10\",\"section\":\"section-100\",\"products\":[{\"row\":\"A4\",\"seat\":\"11\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"12\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"13\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"14\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"15\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"16\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"17\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"18\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null}],\"splitQuantity\":null,\"splitOption\":null,\"deliveryOption\":\"PDF\",\"ticketTraits\":null,\"internalNotes\":null,\"externalListingId\":null,\"status\":\"INCOMPLETE\",\"paymentType\":null,\"ccId\":null,\"contactId\":null,\"tealeafSessionId\":null,\"threatMatrixSessionId\":null,\"eventId\":\"8993478\",\"event\":null}";
		bulkListingRequestEntity.setListingRequestClob(listingRequestClob);
		bulkListingRequestEntity.setListingId(677218687213L);
		bulkListingRequests.add(bulkListingRequestEntity);

		
		
		Mockito.when(bulkInventoryMgr.getBulkListingRequests(groupId)).thenReturn(bulkListingRequests);
		
		bulkListingHelper.processListingRequestByGroupId(groupId, "machineNode", false);
	}
	
	@Test
	public void testUpdateListingRequestByGroupId(){
		BulkListingGroup bulkListingGroup = new BulkListingGroup();
		bulkListingGroup.setAssertion(assertionHeader);
		bulkListingGroup.setBulkJobId(100L);
		bulkListingGroup.setBulkListingGroupId(98765L);
		bulkListingGroup.setBulkStatusId(2L);
		bulkListingGroup.setUserId(sellerId);
		bulkListingGroup.setUserGuid(sellerGuid);
		bulkListingGroup.setMachineNode("machineNode");
		populateBulkListingRequestEntity(false, false);
		bulkListingHelper.updateListingRequestByGroupId(bulkListingGroup, populateListingResponse(), bulkListingRequestMap);
	}
	
	@Test
	public void testUpdateListingRequestByGroupIdErrors(){
		BulkListingGroup bulkListingGroup = new BulkListingGroup();
		bulkListingGroup.setAssertion(assertionHeader);
		bulkListingGroup.setBulkJobId(100L);
		bulkListingGroup.setBulkListingGroupId(98765L);
		bulkListingGroup.setBulkStatusId(2L);
		bulkListingGroup.setUserId(sellerId);
		bulkListingGroup.setUserGuid(sellerGuid);
		bulkListingGroup.setMachineNode("machineNode");
		populateBulkListingRequestEntity(false, false);
		List<ListingResponse> listingResponses = new ArrayList<ListingResponse>();
		ListingResponse response = new ListingResponse();
		response.setId("121211");
		response.setExternalListingId("987651");
		response.setErrors(new ArrayList<ListingError>());
		listingResponses.add(response);
		bulkListingRequestMap.put("121211", new com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest());
		
		bulkListingHelper.updateListingRequestByGroupId(bulkListingGroup, listingResponses, bulkListingRequestMap);
	}
	
	private List<ListingResponse> populateListingResponse(){
		List<ListingResponse> listingResponses = new ArrayList<ListingResponse>();
		ListingResponse response ;
		for(Long i=1L;i<=100;i++){
			response = new ListingResponse();
			response.setId("12121"+i);
			response.setExternalListingId("98765"+i);
			response.setStatus(ListingStatus.ACTIVE);
			listingResponses.add(response);
		}
		return listingResponses;
	}
	
	private List<com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest> populateBulkListingRequestEntity(boolean isError, boolean isJsonMappingException){
		List<com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest> bulkListingRequests = new ArrayList<com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest>();
		com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest bulkListingRequestEntity;
		for(Long i=1L;i<=100;i++){
			bulkListingRequestEntity  = new com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest();
			bulkListingRequestEntity.setBulkListingGroupId(98765L);
			bulkListingRequestEntity.setBulkListingRequestId(123+i);
			bulkListingRequestEntity.setExternalListingId("98765"+i);
			
			String listingRequestClob;
			if(isError){
				listingRequestClob="someString";
			}else if (isJsonMappingException){
				listingRequestClob = "{\"faceValue\":null,\"pricePerProduct\":{\"amount12\":100.00,\"currency\":\"USD\"},\"purchasePrice\":null,\"buyerSeesPerProduct\":null,\"payoutPerProduct\":null,\"quantity\":null,\"inhandDate\":null,\"saleEndDate\":\"2014-06-10\",\"section\":\"section-100\",\"products\":[{\"row\":\"A4\",\"seat\":\"11\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"12\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"13\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"14\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"15\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"16\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"17\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"18\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null}],\"splitQuantity\":null,\"splitOption\":null,\"deliveryOption\":\"PDF\",\"ticketTraits\":null,\"internalNotes\":null,\"externalListingId\":null,\"status\":\"INCOMPLETE\",\"paymentType\":null,\"ccId\":null,\"contactId\":null,\"tealeafSessionId\":null,\"threatMatrixSessionId\":null,\"eventId\":\"8993478\",\"event\":null}";
			}else{
				listingRequestClob = "{\"faceValue\":null,\"pricePerProduct\":{\"amount\":100.00,\"currency\":\"USD\"},\"purchasePrice\":null,\"buyerSeesPerProduct\":null,\"payoutPerProduct\":null,\"quantity\":null,\"inhandDate\":null,\"saleEndDate\":\"2014-06-10\",\"section\":\"section-100\",\"products\":[{\"row\":\"A4\",\"seat\":\"11\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"12\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"13\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"14\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"15\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"16\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"17\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null},{\"row\":\"A4\",\"seat\":\"18\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null}],\"splitQuantity\":null,\"splitOption\":null,\"deliveryOption\":\"PDF\",\"ticketTraits\":null,\"internalNotes\":null,\"externalListingId\":null,\"status\":\"INCOMPLETE\",\"paymentType\":null,\"ccId\":null,\"contactId\":null,\"tealeafSessionId\":null,\"threatMatrixSessionId\":null,\"eventId\":\"8993478\",\"event\":null}";	
			}
			
			bulkListingRequestEntity.setListingRequestClob(listingRequestClob);
			bulkListingRequestMap.put(bulkListingRequestEntity.getExternalListingId(), bulkListingRequestEntity);
			bulkListingRequests.add(bulkListingRequestEntity);
		}

		return bulkListingRequests;
	}
	
	
	@Test
	public void testProcessListingRequestByGroupId_SellerJobRunning(){
		
		BulkListingGroup bulkListingGroup = new BulkListingGroup();
		bulkListingGroup.setAssertion(assertionHeader);
		bulkListingGroup.setBulkJobId(100L);
		bulkListingGroup.setBulkListingGroupId(98765L);
		bulkListingGroup.setBulkStatusId(2L);
		bulkListingGroup.setUserId(sellerId);
		bulkListingGroup.setUserGuid("qwertypoiuy");
		bulkListingGroup.setMachineNode("machineNode");
		Mockito.when(bulkInventoryMgr.getGroupById(Mockito.anyLong())).thenReturn(bulkListingGroup);
		
		BulkJob bulkJob = new BulkJob();
		bulkJob.setBulkJobId(100L);
		bulkJob.setBulkStatusId(2L);
		bulkJob.setUserId(sellerId);
		Mockito.when(bulkInventoryMgr.getJobById(100L)).thenReturn(bulkJob);
		
		List<BulkJob> bulkJobs = new ArrayList<BulkJob>();
		bulkJob = new BulkJob();
		bulkJob.setBulkJobId(101L);
		bulkJob.setBulkStatusId(1L);
		bulkJob.setUserId(sellerId);
		bulkJobs.add(bulkJob);
		Mockito.when(bulkInventoryMgr.getPendingJobsBySellerId(sellerId)).thenReturn(bulkJobs);
		
		List<BulkListingGroup> bulkListingGroups = new ArrayList<BulkListingGroup>();
		bulkListingGroups.add(bulkListingGroup);
		Mockito.when(bulkInventoryMgr.getGroupsByJobId(101L)).thenReturn(bulkListingGroups);
		bulkListingHelper.processListingRequestByGroupId(98765L, "machineNode", false);
	}
	
	@Test
	public void testProcessListingRequestByGroupId_JsonError(){
		Long groupId = 98765L;
		
		BulkListingGroup bulkListingGroup = new BulkListingGroup();
		bulkListingGroup.setAssertion(assertionHeader);
		bulkListingGroup.setBulkJobId(100L);
		bulkListingGroup.setBulkListingGroupId(groupId);
		bulkListingGroup.setBulkStatusId(2L);
		bulkListingGroup.setUserId(sellerId);
		bulkListingGroup.setUserGuid(sellerGuid);
		bulkListingGroup.setMachineNode("machineNode");
		bulkListingGroup.setBulkUploadTypeId(BulkUploadType.CREATE.getId());
		Mockito.when(bulkInventoryMgr.getGroupById(groupId)).thenReturn(bulkListingGroup);
		
		BulkListingInternal bli = new BulkListingInternal();
		bli.setAssertion(assertionHeader);
		bli.setCcId(7654321L);
		bli.setContactId(987655434566L);
		bli.setCreateListingBody(populateListings(true, false, false, false));
		bli.setGroupId(98765L);
		bli.setSellerGuid(sellerGuid);
		bli.setSellerId(sellerId);
		
		Mockito.when(listingCreateProcess.createListings(any(BulkListingInternal.class),  Mockito.anyString(),  Mockito.anyString())).thenReturn(populateListingResponse());
		
		Mockito.when(bulkInventoryMgr.getBulkListingRequests(groupId)).thenReturn(populateBulkListingRequestEntity(true, false));
		
		bulkListingHelper.processListingRequestByGroupId(groupId, "machineNode", false);
	}
	
	@Test
	public void multipleSellerJobs(){
		BulkListingGroup bulkListingGroup = new BulkListingGroup();
		bulkListingGroup.setAssertion(assertionHeader);
		bulkListingGroup.setBulkJobId(100L);
		bulkListingGroup.setBulkListingGroupId(98765L);
		bulkListingGroup.setBulkStatusId(2L);
		bulkListingGroup.setUserId(sellerId);
		bulkListingGroup.setUserGuid("qwertypoiuy");
		bulkListingGroup.setMachineNode("machineNode");
		Mockito.when(bulkInventoryMgr.getGroupById(Mockito.anyLong())).thenReturn(bulkListingGroup);
		
		BulkJob bulkJob = new BulkJob();
		bulkJob.setBulkJobId(100L);
		bulkJob.setBulkStatusId(2L);
		bulkJob.setUserId(sellerId);
		Mockito.when(bulkInventoryMgr.getJobById(100L)).thenReturn(bulkJob);
		
		List<BulkJob> bulkJobs = new ArrayList<BulkJob>();
		bulkJob = new BulkJob();
		bulkJob.setBulkJobId(101L);
		bulkJob.setBulkStatusId(1L);
		bulkJob.setUserId(sellerId);
		bulkJobs.add(bulkJob);
		bulkJob = new BulkJob();
		bulkJob.setBulkJobId(102L);
		bulkJob.setBulkStatusId(1L);
		bulkJob.setUserId(sellerId);
		bulkJobs.add(bulkJob);
		Mockito.when(bulkInventoryMgr.getPendingJobsBySellerId(Mockito.anyLong())).thenReturn(bulkJobs);
		
		List<BulkListingGroup> bulkListingGroups = new ArrayList<BulkListingGroup>();
		bulkListingGroups.add(bulkListingGroup);
		Mockito.when(bulkInventoryMgr.getGroupsByJobId(101L)).thenReturn(bulkListingGroups);
		bulkListingHelper.processListingRequestByGroupId(98765L, "machineNode", false);
	}
	
	@Test
	public void testJsonMappingException(){
		Long groupId = 98765L;
		
		BulkListingGroup bulkListingGroup = new BulkListingGroup();
		bulkListingGroup.setAssertion(assertionHeader);
		bulkListingGroup.setBulkJobId(100L);
		bulkListingGroup.setBulkListingGroupId(groupId);
		bulkListingGroup.setBulkStatusId(2L);
		bulkListingGroup.setUserId(sellerId);
		bulkListingGroup.setUserGuid(sellerGuid);
		bulkListingGroup.setMachineNode("machineNode");
		bulkListingGroup.setBulkUploadTypeId(BulkUploadType.CREATE.getId());
		Mockito.when(bulkInventoryMgr.getGroupById(groupId)).thenReturn(bulkListingGroup);
		
		BulkListingInternal bli = new BulkListingInternal();
		bli.setAssertion(assertionHeader);
		bli.setCcId(7654321L);
		bli.setContactId(987655434566L);
		bli.setCreateListingBody(populateListings(true, false, false, false));
		bli.setGroupId(98765L);
		bli.setSellerGuid(sellerGuid);
		bli.setSellerId(sellerId);
		
		Mockito.when(listingCreateProcess.createListings(any(BulkListingInternal.class),  Mockito.anyString(),  Mockito.anyString())).thenReturn(populateListingResponse());
		
		Mockito.when(bulkInventoryMgr.getBulkListingRequests(groupId)).thenReturn(populateBulkListingRequestEntity(false, true));
		
		BulkJob bulkJob = new BulkJob();
		bulkJob.setBulkJobId(100L);
		bulkJob.setBulkStatusId(2L);
		bulkJob.setUserId(123456L);
		Mockito.when(bulkInventoryMgr.getJobById(100L)).thenReturn(bulkJob);
		bulkListingHelper.processListingRequestByGroupId(groupId, "machineNode", false);
	}

	
	@Test
	public void testGetJobStatuses(){
		//mock setup
		BulkJob bulkJob = new BulkJob();
		bulkJob.setBulkJobId(123L);
		bulkJob.setUserId(sellerId);
		bulkJob.setBulkStatusId(5L);
		when(bulkInventoryMgr.getJobById(anyLong())).thenReturn(bulkJob);
		
		String requestClob = "{\"faceValue\":null,\"pricePerProduct\":{\"amount\":120.00,\"currency\":\"USD\"},\"purchasePrice\":null,\"buyerSeesPerProduct\":null,\"payoutPerProduct\":null,\"quantity\":null,\"inhandDate\":null,\"saleEndDate\":\"2014-07-25\",\"section\":\"112\",\"products\":[{\"row\":\"4\",\"seat\":\"11\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null}],\"splitQuantity\":null,\"splitOption\":\"NONE\",\"deliveryOption\":\"UPS\",\"ticketTraits\":null,\"internalNotes\":null,\"externalListingId\":\"1234567\",\"status\":\"ACTIVE\",\"paymentType\":null,\"ccId\":null,\"contactId\":null,\"tealeafSessionId\":null,\"threatMatrixSessionId\":null,\"eventId\":\"9015843\",\"event\":null}";
		com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest listingReq = new com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest();
		listingReq.setBulkListingGroupId(12345L);
		listingReq.setBulkListingRequestId(456L);
		listingReq.setExternalListingId("123abc");
		listingReq.setListingId(88888888L);
		listingReq.setListingRequestClob(requestClob);
		listingReq.setListingStatus("ACTIVE");
		List<com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest> listingResponses = new ArrayList<com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest>();
		listingResponses.add(listingReq);
		when(bulkInventoryMgr.getJobStatuses(123L)).thenReturn(listingResponses);

		// execute
		bulkListingHelper.getJobStatuses(sellerId, 123L);
		
		// verify
		verify(bulkInventoryMgr, times(1)).getJobById(anyLong());
		verify(bulkInventoryMgr, times(1)).getJobStatuses(123L);
	}
	
	@Test
	public void testGetJobStatusesJobNotFound(){
		//mock setup
		BulkJob bulkJob = new BulkJob();
		bulkJob.setBulkJobId(123L);
		bulkJob.setUserId(sellerId);
		bulkJob.setBulkStatusId(5L);
		when(bulkInventoryMgr.getJobById(anyLong())).thenReturn(null);
		
		String requestClob = "{\"faceValue\":null,\"pricePerProduct\":{\"amount\":120.00,\"currency\":\"USD\"},\"purchasePrice\":null,\"buyerSeesPerProduct\":null,\"payoutPerProduct\":null,\"quantity\":null,\"inhandDate\":null,\"saleEndDate\":\"2014-07-25\",\"section\":\"112\",\"products\":[{\"row\":\"4\",\"seat\":\"11\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null}],\"splitQuantity\":null,\"splitOption\":\"NONE\",\"deliveryOption\":\"UPS\",\"ticketTraits\":null,\"internalNotes\":null,\"externalListingId\":\"1234567\",\"status\":\"ACTIVE\",\"paymentType\":null,\"ccId\":null,\"contactId\":null,\"tealeafSessionId\":null,\"threatMatrixSessionId\":null,\"eventId\":\"9015843\",\"event\":null}";
		com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest listingReq = new com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest();
		listingReq.setBulkListingGroupId(12345L);
		listingReq.setBulkListingRequestId(456L);
		listingReq.setExternalListingId("123abc");
		listingReq.setListingId(88888888L);
		listingReq.setListingRequestClob(requestClob);
		listingReq.setListingStatus("ACTIVE");
		List<com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest> listingResponses = new ArrayList<com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest>();
		listingResponses.add(listingReq);
		when(bulkInventoryMgr.getJobStatuses(123L)).thenReturn(listingResponses);

		// execute
		try{
			bulkListingHelper.getJobStatuses(sellerId, 123L);
		}
		catch(ListingBusinessException e){
			Assert.assertEquals(e.getListingError().getCode(), ErrorCode.BULK_JOB_NOT_FOUND);
			Assert.assertEquals(e.getListingError().getType(), ErrorType.NOT_FOUND);
		}
		// verify
		verify(bulkInventoryMgr, times(1)).getJobById(anyLong());
		verify(bulkInventoryMgr, never()).getJobStatuses(123L);
	}
	
	@Test
	public void testGetJobStatusesJobBelongingToOtherUser(){
		//mock setup
		BulkJob bulkJob = new BulkJob();
		bulkJob.setBulkJobId(123L);
		bulkJob.setUserId(988L);
		bulkJob.setBulkStatusId(5L);
		when(bulkInventoryMgr.getJobById(anyLong())).thenReturn(null);
		
		String requestClob = "{\"faceValue\":null,\"pricePerProduct\":{\"amount\":120.00,\"currency\":\"USD\"},\"purchasePrice\":null,\"buyerSeesPerProduct\":null,\"payoutPerProduct\":null,\"quantity\":null,\"inhandDate\":null,\"saleEndDate\":\"2014-07-25\",\"section\":\"112\",\"products\":[{\"row\":\"4\",\"seat\":\"11\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null}],\"splitQuantity\":null,\"splitOption\":\"NONE\",\"deliveryOption\":\"UPS\",\"ticketTraits\":null,\"internalNotes\":null,\"externalListingId\":\"1234567\",\"status\":\"ACTIVE\",\"paymentType\":null,\"ccId\":null,\"contactId\":null,\"tealeafSessionId\":null,\"threatMatrixSessionId\":null,\"eventId\":\"9015843\",\"event\":null}";
		com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest listingReq = new com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest();
		listingReq.setBulkListingGroupId(12345L);
		listingReq.setBulkListingRequestId(456L);
		listingReq.setExternalListingId("123abc");
		listingReq.setListingId(88888888L);
		listingReq.setListingRequestClob(requestClob);
		listingReq.setListingStatus("ACTIVE");
		List<com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest> listingResponses = new ArrayList<com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest>();
		listingResponses.add(listingReq);
		when(bulkInventoryMgr.getJobStatuses(123L)).thenReturn(listingResponses);

		// execute
		try{
			bulkListingHelper.getJobStatuses(sellerId, 123L);
		}
		catch(ListingBusinessException e){
			Assert.assertEquals(e.getListingError().getCode(), ErrorCode.BULK_JOB_NOT_FOUND);
			Assert.assertEquals(e.getListingError().getType(), ErrorType.NOT_FOUND);
		}
		// verify
		verify(bulkInventoryMgr, times(1)).getJobById(anyLong());
		verify(bulkInventoryMgr, never()).getJobStatuses(123L);
	}
	
	@Test
	public void testGetJobStatusesListingError(){
		//mock setup
		BulkJob bulkJob = new BulkJob();
		bulkJob.setBulkJobId(123L);
		bulkJob.setUserId(sellerId);
		bulkJob.setBulkStatusId(5L);
		when(bulkInventoryMgr.getJobById(anyLong())).thenReturn(bulkJob);
		
		String requestClob = "{\"faceValue\":null,\"pricePerProduct\":{\"amount\":120.00,\"currency\":\"USD\"},\"purchasePrice\":null,\"buyerSeesPerProduct\":null,\"payoutPerProduct\":null,\"quantity\":null,\"inhandDate\":null,\"saleEndDate\":\"2014-07-25\",\"section\":\"112\",\"products\":[{\"row\":\"4\",\"seat\":\"11\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null}],\"splitQuantity\":null,\"splitOption\":\"NONE\",\"deliveryOption\":\"UPS\",\"ticketTraits\":null,\"internalNotes\":null,\"externalListingId\":\"1234567\",\"status\":\"ACTIVE\",\"paymentType\":null,\"ccId\":null,\"contactId\":null,\"tealeafSessionId\":null,\"threatMatrixSessionId\":null,\"eventId\":\"9015843\",\"event\":null}";
		com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest listingReq = new com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest();
		listingReq.setBulkListingGroupId(12345L);
		listingReq.setBulkListingRequestId(456L);
		listingReq.setExternalListingId("123abc");
		listingReq.setListingId(88888888L);
		listingReq.setListingRequestClob(requestClob);
		listingReq.setListingStatus("INCOMPLETE");
		List<ListingError> listingErrors = new ArrayList<ListingError>();
		listingErrors.add(new ListingError(ErrorType.INPUTERROR,
				ErrorCode.MISSING_EVENT_INFO,
				ErrorEnum.MISSING_EVENT_INFO.getMessage(),
				"event"));
		listingReq.setErrorCode(JsonUtil.toJson(listingErrors));
		List<com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest> listingResponses = new ArrayList<com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest>();
		listingResponses.add(listingReq);
		when(bulkInventoryMgr.getJobStatuses(123L)).thenReturn(listingResponses);

		// execute
		BulkJobResponse bulkJobResponse = bulkListingHelper.getJobStatuses(sellerId, 123L);
		
		// verify
		verify(bulkInventoryMgr, times(1)).getJobById(anyLong());
		verify(bulkInventoryMgr, times(1)).getJobStatuses(123L);
		List<com.stubhub.domain.inventory.v2.bulk.DTO.ListingResponse> listingResps = bulkJobResponse.getListings();
		Assert.assertEquals(listingResps.size(), 1);
		List<ListingError> errors = listingResps.get(0).getErrors();
		Assert.assertEquals(errors.size(), 1);
		ListingError error = errors.get(0);
		Assert.assertEquals(error.getCode(), ErrorCode.MISSING_EVENT_INFO);
	}
	
	@Test
	public void testBulkUpdateListing(){
		Mockito.when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(populateBulkJob());
		BulkListingRequest localUpdatelist= createBulkListingRequest;
		localUpdatelist.getListings().get(0).setListingId(1234567L);
		localUpdatelist.getListings().get(1).setListingId(1234567890L);
		List<Listing> listings = new ArrayList<Listing>();
		Listing listing = new Listing();
		listing.setId(1234L);
		listing.setEventId(544647L);
		listing = new Listing();
		listing.setId(235432L);
		listing.setEventId(2354326876L);
		when(inventoryMgr.getListings(Mockito.anyList())).thenReturn(listings);
		bulkListingHelper.bulkUpdateListing(sellerId, sellerGuid, subscriber, null, localUpdatelist, assertionHeader, headers);	
	}
	
	@Test
	public void testBulkUpdateListing_duplicateExtListId(){
		Mockito.when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(populateBulkJob());
		BulkListingRequest localUpdatelist= createBulkListingRequest;
		localUpdatelist.getListings().get(0).setListingId(1234567L);
		localUpdatelist.getListings().get(1).setListingId(1234567890L);
		localUpdatelist.getListings().get(1).setExternalListingId("98765");
		List<Listing> listings = new ArrayList<Listing>();
		Listing listing = new Listing();
		listing.setId(1234L);
		listing.setEventId(544647L);
		listings.add(listing);
		listing = new Listing();
		listing.setId(235432L);
		listing.setEventId(2354326876L);
		listings.add(listing);
		when(inventoryMgr.getListings(Mockito.anyList())).thenReturn(listings);
		bulkListingHelper.bulkUpdateListing(sellerId, sellerGuid, subscriber, null, localUpdatelist, assertionHeader, headers);	
	}
	
	@Test
	public void testBulkUpdateListing_missingListingId(){
		Mockito.when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(populateBulkJob());
		BulkListingRequest localUpdatelist= createBulkListingRequest;
		bulkListingHelper.bulkUpdateListing(sellerId, sellerGuid, subscriber, null, localUpdatelist, assertionHeader, headers);	
	}
	
	@Test
	public void testBulkUpdateListing_withHibernateException(){
		Mockito.when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(populateBulkJob());
		Mockito.doThrow(new HibernateOptimisticLockingFailureException(new StaleStateException(""))).doNothing().when(
				bulkInventoryMgr).updateJob(any(BulkJob.class));
		BulkListingRequest localUpdatelist= createBulkListingRequest;
		bulkListingHelper.bulkUpdateListing(sellerId, sellerGuid, subscriber, null, localUpdatelist, assertionHeader, headers);	
	}
	
	@Test
	public void testBulkUpdateListing_MissingExternalListingId(){
		when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(populateBulkJob());
		List<Long> missedListingIds = new ArrayList<Long>();
		missedListingIds.add(123456L);
		when(listingCatalogSolrUtil.getEventByListingId(Mockito.anyList(), Mockito.anyLong())).thenReturn(missedListingIds);
		List<Listing> listings = new ArrayList<Listing>();
		Listing listing = new Listing();
		listing.setId(123456L);
		listing.setEventId(544647L);
		listing.setExternalId("5446470");
		listings.add(listing);
		listing = new Listing();
		listing.setId(1234567L);
		listing.setEventId(2354326876L);
		listing.setExternalId("5446471");
		listings.add(listing);
		when(inventoryMgr.getListings(Mockito.anyList())).thenReturn(listings);

		//SELLAPI-1181 09/09/15 START
		List <ListingRequest> lr = updateBulkListingRequest.getListings();
		for(ListingRequest lr01 : lr) {
			lr01.setExternalListingId(null);
		}
		//SELLAPI-1181 09/09/15 END
		
		BulkListingRequest localUpdatelist= updateBulkListingRequest;
		bulkListingHelper.bulkUpdateListing(sellerId, sellerGuid, subscriber, null, localUpdatelist, assertionHeader, headers);	
	}
	
	//SELLAPI-1181 09/10/15 START
	@Test
	public void testBulkUpdateListing_ExternalListingId(){
		
		when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(populateBulkJob());
		List<String> missedListingIds = new ArrayList<String>();
		missedListingIds.add("987651");
		when(listingCatalogSolrUtil.getEventByExternalId(Mockito.anyLong(), Mockito.anyList())).thenReturn(missedListingIds);
		List<Listing> listings = new ArrayList<Listing>();
		Listing listing = new Listing();
		listing.setId(123456L);
		listing.setEventId(544647L);
		listing.setExternalId("98765");
		listings.add(listing);
		listing = new Listing();
		listing.setId(1234567L);
		listing.setEventId(2354326876L);
		listing.setExternalId("987651");
		listings.add(listing);
		when(inventoryMgr.getListings(Mockito.anyLong(), Mockito.anyList())).thenReturn(listings);

		List <ListingRequest> lr = updateBulkListingRequest.getListings();
		for(ListingRequest lr01 : lr) {
			lr01.setListingId(null);
		}
		
		BulkListingRequest localUpdatelist= updateBulkListingRequest;
		bulkListingHelper.bulkUpdateListing(sellerId, sellerGuid, subscriber, null, localUpdatelist, assertionHeader, headers);	
	}
	
	//SELLAPI-1181 09/10/15 END

	@Test
	public void testBulkUpdateListing_OnlyListingId() {

		BulkListingRequest localUpdatelist = new BulkListingRequest();
		{
			ArrayList<ListingRequest> listings = new ArrayList<ListingRequest>();
			localUpdatelist.setListings(listings);

			ListingRequest listing;
			listing = new ListingRequest();
			listings.add(listing);
			listing.setEventId("123L");
			listing.setListingId(123456L);
			listing.setStatus(ListingStatus.INACTIVE);

			listing = new ListingRequest();
			listings.add(listing);
			listing.setEventId("123L");
			listing.setListingId(123457L);
			listing.setStatus(ListingStatus.INACTIVE);
		}

		when(bulkInventoryMgr.getAllJobsForSeller(sellerId)).thenReturn(populateBulkJob());
		List<Long> missedListingIds = new ArrayList<Long>();
		when(listingCatalogSolrUtil.getEventByListingId(Mockito.anyList(), Mockito.anyLong())).thenReturn(missedListingIds);
		List<Listing> listings = new ArrayList<Listing>();
		Listing listing = new Listing();
		listing.setId(123456L);
		listing.setEventId(544647L);
		listings.add(listing);
		listing = new Listing();
		listing.setId(1234567L);
		listing.setEventId(2354326876L);
		listings.add(listing);
		when(inventoryMgr.getListings(Mockito.anyList())).thenReturn(listings);

		bulkListingHelper.bulkUpdateListing(sellerId, sellerGuid, subscriber, null, localUpdatelist, assertionHeader, headers);
	}
	
	@Test
    public void testUpdateJobStatus() {
        BulkJob bulkJob = new BulkJob();
        bulkJob.setBulkJobId(123L);
        bulkJob.setUserId(sellerId);
        bulkJob.setBulkStatusId(3L);
        when(bulkInventoryMgr.getJobById(anyLong())).thenReturn(bulkJob);
        
        String requestClob = "{\"faceValue\":null,\"pricePerProduct\":{\"amount\":120.00,\"currency\":\"USD\"},\"purchasePrice\":null,\"buyerSeesPerProduct\":null,\"payoutPerProduct\":null,\"quantity\":null,\"inhandDate\":null,\"saleEndDate\":\"2014-07-25\",\"section\":\"112\",\"products\":[{\"row\":\"4\",\"seat\":\"11\",\"fulfillmentArtifact\":null,\"operation\":\"ADD\",\"productType\":\"TICKET\",\"externalId\":null}],\"splitQuantity\":null,\"splitOption\":\"NONE\",\"deliveryOption\":\"UPS\",\"ticketTraits\":null,\"internalNotes\":null,\"externalListingId\":\"1234567\",\"status\":\"ACTIVE\",\"paymentType\":null,\"ccId\":null,\"contactId\":null,\"tealeafSessionId\":null,\"threatMatrixSessionId\":null,\"eventId\":\"9015843\",\"event\":null}";
        com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest listingReq = new com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest();
        listingReq.setBulkListingGroupId(12345L);
        listingReq.setBulkListingRequestId(456L);
        listingReq.setExternalListingId("123abc");
        listingReq.setListingId(88888888L);
        listingReq.setListingRequestClob(requestClob);
        listingReq.setListingStatus("ACTIVE");
        List<com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest> listingResponses = new ArrayList<com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest>();
        listingResponses.add(listingReq);
        when(bulkInventoryMgr.getJobStatuses(123L)).thenReturn(listingResponses);
        
        Listing listing = new Listing();
        listing.setId(88888888L);
        listing.setSystemStatus("ACTIVE");
        when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
        
        BulkJobStatusRequest jobStatusRequest = new BulkJobStatusRequest();
        jobStatusRequest.setJobStatus("ERROR");
        jobStatusRequest.setListingStatus("DELETED");

        BulkJobResponse response = bulkListingHelper.updateJobStatus(sellerId, 123L, jobStatusRequest);
        Assert.assertEquals(response.getStatus(), "ERROR");
    }
	
	@Test(expectedExceptions=ListingBusinessException.class)
    public void testUpdateJobStatusDifferentUser() {
	  BulkJob bulkJob = new BulkJob();
      bulkJob.setBulkJobId(123L);
      bulkJob.setUserId(sellerId);
      bulkJob.setBulkStatusId(3L);
      when(bulkInventoryMgr.getJobById(anyLong())).thenReturn(bulkJob);
      
      BulkJobStatusRequest jobStatusRequest = new BulkJobStatusRequest();
      jobStatusRequest.setJobStatus("ERROR");
      jobStatusRequest.setListingStatus("DELETED");
      
      bulkListingHelper.updateJobStatus(123L, 123L, jobStatusRequest);
	}
	
	@Test(expectedExceptions=ListingBusinessException.class)
    public void testUpdateJobStatusInvalidInput() {
      BulkJob bulkJob = new BulkJob();
      bulkJob.setBulkJobId(123L);
      bulkJob.setUserId(sellerId);
      bulkJob.setBulkStatusId(3L);
      when(bulkInventoryMgr.getJobById(anyLong())).thenReturn(bulkJob);
      
      BulkJobStatusRequest jobStatusRequest = new BulkJobStatusRequest();
      jobStatusRequest.setJobStatus("COMPLETED");
      jobStatusRequest.setListingStatus("DELETED");
      
      bulkListingHelper.updateJobStatus(sellerId, 123L, jobStatusRequest); 
    }
	
	@Test(expectedExceptions=ListingBusinessException.class)
    public void testUpdateJobStatusInvalidStatus() {
      BulkJob bulkJob = new BulkJob();
      bulkJob.setBulkJobId(123L);
      bulkJob.setUserId(sellerId);
      bulkJob.setBulkStatusId(5L);
      when(bulkInventoryMgr.getJobById(anyLong())).thenReturn(bulkJob);
      
      BulkJobStatusRequest jobStatusRequest = new BulkJobStatusRequest();
      jobStatusRequest.setJobStatus("ERROR");
      
      bulkListingHelper.updateJobStatus(sellerId, 123L, jobStatusRequest);
    }
	
}
