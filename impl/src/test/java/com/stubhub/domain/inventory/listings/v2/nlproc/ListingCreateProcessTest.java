package com.stubhub.domain.inventory.listings.v2.nlproc;


import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.*;

import javax.ws.rs.core.HttpHeaders;

import org.junit.Assert;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.fulfillment.window.v1.intf.EventFulfillmentWindowResponse;
import com.stubhub.domain.infrastructure.common.exception.derived.SHBadRequestException;
import com.stubhub.domain.inventory.biz.v2.impl.util.FulfillmentServiceAdapter;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.entity.SplitOption;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.EventError;
import com.stubhub.domain.inventory.common.util.EventMappingException;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.listings.v2.helper.ListingCreateProcess;
import com.stubhub.domain.inventory.listings.v2.helper.ListingPriceDetailsHelper;
import com.stubhub.domain.inventory.listings.v2.helper.ListingPriceUtil;
import com.stubhub.domain.inventory.listings.v2.helper.SellerEligibilityHelper;
import com.stubhub.domain.inventory.listings.v2.helper.UpdateListingAsyncHelper2;
import com.stubhub.domain.inventory.listings.v2.tns.FraudEvaluationService;
import com.stubhub.domain.inventory.listings.v2.util.EventHelper;
import com.stubhub.domain.inventory.listings.v2.util.FulfillmentServiceHelper;
import com.stubhub.domain.inventory.listings.v2.util.JMSMessageHelper;
import com.stubhub.domain.inventory.listings.v2.util.ListingSeatTraitsHelper;
import com.stubhub.domain.inventory.listings.v2.util.ListingTextValidatorUtil;
import com.stubhub.domain.inventory.listings.v2.util.PaymentHelper;
import com.stubhub.domain.inventory.listings.v2.util.PrimaryIntegrationUtil;
import com.stubhub.domain.inventory.listings.v2.util.ResourceManager;
import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.domain.inventory.listings.v2.util.SellerHelper;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.domain.inventory.listings.v2.validator.ListingRequestValidator;
import com.stubhub.domain.inventory.v2.DTO.EventInfo;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.DTO.TicketTrait;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingInternal;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.domain.inventory.v2.listings.eventmapper.EventMapperAdaptor;
import com.stubhub.domain.pricing.intf.aip.v1.request.PriceRequestList;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponseList;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.ShipEvent;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;

public class ListingCreateProcessTest extends SHInventoryTest
{
	private ListingCreateProcess listingCreateProcess;	
	private UpdateListingAsyncHelper2 updateListingv2;
	private HttpHeaders headers;
	private EventMapperAdaptor eventMapperAdaptor;
	private EventHelper eventHelper; 
	private UserHelper userHelper;
	private SellerHelper sellerHelper;
	private PaymentHelper paymentHelper;
	private FulfillmentServiceHelper fulfillmentServiceHelper;
	private ListingPriceDetailsHelper listingPriceDetailsHelper;
	private ListingPriceUtil listingPriceUtil;
	private ListingSeatTraitsHelper listingSeatTraitsHelper;
	private InventoryMgr inventoryMgr;
	private TicketSeatMgr ticketSeatMgr;
	private ListingSeatTraitMgr listingSeatTraitMgr;
	private JMSMessageHelper jmsMessageHelper;
	private SellerEligibilityHelper sellerEligibilityHelper;
	private MasterStubhubPropertiesWrapper masterStubhubProperties;
	private FraudEvaluationService fraudEvaluationService;
	
	ResourceManager rm = new ResourceManager();
	ListingTextValidatorUtil listingTextValidatorUtil = new ListingTextValidatorUtil();
	ListingRequestValidator listingRequestValidator = new ListingRequestValidator();

	private FulfillmentServiceAdapter fulfillmentServiceAdapter;
	
	private String clientIp = "client Ip";
	private String userAgent = "userAgent";
	private Calendar saleEndDate;
	
	private static Float BASE_PRICE = 120F;
	private static Float BASE_SELL_FEE = 20F;
	
	private Event globalEvent = null;
	
	@BeforeMethod
	@BeforeTest
	public void setUp() throws Exception 
	{
		MockitoAnnotations.initMocks(this);
		
		headers = super.mockHeaders (null, null);

		listingCreateProcess = new ListingCreateProcess();

		ReflectionTestUtils.setField(listingCreateProcess, "countryWhiteList", new HashSet<>(Arrays.asList("*")));

		fraudEvaluationService = (FraudEvaluationService)mockClass( FraudEvaluationService.class, listingCreateProcess, "fraudEvaluationService");
		Mockito.doNothing().when(fraudEvaluationService).submitToQueue(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString(), Mockito.any(ListingStatus.class));
		eventMapperAdaptor = (EventMapperAdaptor)mockClass( EventMapperAdaptor.class, listingCreateProcess, "eventMapperAdaptor");
		eventHelper = (EventHelper)mockClass( EventHelper.class, listingCreateProcess, "eventHelper");
		sellerEligibilityHelper = (SellerEligibilityHelper) mockClass(SellerEligibilityHelper.class, listingCreateProcess, "sellerEligibilityHelper");
		
		masterStubhubProperties = Mockito.mock(MasterStubhubPropertiesWrapper.class);
		ReflectionTestUtils.setField(listingCreateProcess, "masterStubhubProperties", masterStubhubProperties);
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("UK,GB,DE");
		globalEvent = super.getEvent();
		globalEvent.setCountry("UK");
		//event = eventHelper.getEventObject (locale, listing, eventId, getTraits );
		when(eventHelper.getEventById(Mockito.anyLong(),Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(globalEvent);
		when(eventHelper.getEventObject(Mockito.any(Locale.class), Mockito.any(Listing.class), Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(globalEvent);
		setBeanProperty(listingCreateProcess, "eventHelper", eventHelper );
		setBeanProperty(listingCreateProcess, "eventMapperAdaptor", eventMapperAdaptor );
		
		when(sellerEligibilityHelper.checkSellerEligibility(anyString(), anyString(), anyLong())).thenReturn(true);
		setBeanProperty(listingCreateProcess, "sellerEligibilityHelper", sellerEligibilityHelper);
		
		updateListingv2 = new UpdateListingAsyncHelper2();
		updateListingv2.setup();
		setBeanProperty(listingCreateProcess, "UpdateListingAsyncHelper2", updateListingv2);
		
		rm.resetInstance();
		ReflectionTestUtils.setField(listingTextValidatorUtil, "resourceManager", rm);
		ReflectionTestUtils.setField(listingRequestValidator, "listingTextValidatorUtil", listingTextValidatorUtil);
		ReflectionTestUtils.setField(listingRequestValidator, "masterStubhubProperties", masterStubhubProperties);
		ReflectionTestUtils.setField(updateListingv2,"listingRequestValidator", listingRequestValidator);
		
		userHelper = (UserHelper)mockClass( UserHelper.class, updateListingv2, "userHelper");	
		sellerHelper = (SellerHelper)mockClass( SellerHelper.class, updateListingv2, "sellerHelper");	
		paymentHelper = (PaymentHelper)mockClass( PaymentHelper.class, updateListingv2, "paymentHelper");
		fulfillmentServiceHelper = (FulfillmentServiceHelper)mockClass( FulfillmentServiceHelper.class, 
				updateListingv2, "fulfillmentServiceHelper");
		fulfillmentServiceAdapter = (FulfillmentServiceAdapter)mockClass( FulfillmentServiceAdapter.class, 
				updateListingv2, "fulfillmentServiceAdapter");
		
		listingPriceDetailsHelper = new  ListingPriceDetailsHelper();
		setBeanProperty(updateListingv2, "listingPriceDetailsHelper", listingPriceDetailsHelper);
		
		listingSeatTraitsHelper = new ListingSeatTraitsHelper();
		setBeanProperty(updateListingv2, "listingSeatTraitsHelper", listingSeatTraitsHelper);
		
		jmsMessageHelper = new JMSMessageHelper();
		setBeanProperty(updateListingv2, "jmsMessageHelper", jmsMessageHelper);
						
		listingPriceUtil = (ListingPriceUtil)mockClass(ListingPriceUtil.class, 
				listingPriceDetailsHelper, "listingPriceUtil");
		
		listingSeatTraitMgr = (ListingSeatTraitMgr)mockClass( ListingSeatTraitMgr.class, listingSeatTraitsHelper, "listingSeatTraitMgr" );
		List<ListingSeatTrait> emptyTraitsList = new ArrayList<ListingSeatTrait>();
		Mockito.when(listingSeatTraitMgr.findSeatTraits(Mockito.anyLong())).thenReturn(emptyTraitsList);
		
		// mock populate seller detail 
		Mockito.when(sellerHelper.populateSellerDetails(Mockito.any(Listing.class))).thenReturn(true);
		
		// seller payment valid
		Mockito.when(userHelper.isSellerPaymentTypeValid( Mockito.anyLong(),  Mockito.anyLong(),  Mockito.anyLong())).thenReturn(true);
		// seller valid
		Mockito.when(userHelper.isSellerContactValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
		// get CCID
		Mockito.when(userHelper.getMappedValidSellerCCId(Mockito.anyString(), Mockito.anyString(), Mockito.any(List.class),Mockito.anyString())).thenReturn(10101L);
				
		// payment type valid
		Mockito.when(paymentHelper.isSellerPaymentTypeValidForSeller( Mockito.anyLong(),  Mockito.anyLong())).thenReturn(true);
		Mockito.when(paymentHelper.populatePaymentDetails(Mockito.any(Listing.class))).thenReturn(true);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		
		List<FulfillmentWindow> fulfillmentWindows = new ArrayList<FulfillmentWindow>();
	    FulfillmentWindow fw = new FulfillmentWindow();
	    fw.setFulfillmentMethodId(2L);
	    fulfillmentWindows.add(fw);
	    Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Mockito.any(EventFulfillmentWindowResponse.class))).thenReturn(fulfillmentWindows);
		
		Mockito.when(fulfillmentServiceHelper.populateFulfillmentOptions(Mockito.any(Listing.class))).thenReturn(true);
		Mockito.when(fulfillmentServiceHelper.populateFulfillmentOptions(Mockito.any(Listing.class), Mockito.anyList())).thenReturn(true);
		
		saleEndDate = Calendar.getInstance();
		saleEndDate.add(Calendar.MONTH, 1);		// sale end 1 month from now
		Mockito.when(fulfillmentServiceHelper.calculateSaleEndDate(Mockito.any(Listing.class), Mockito.anyListOf(FulfillmentWindow.class))).thenReturn(saleEndDate);
		
		// Sim new price API call
		Mockito.when(listingPriceUtil.getListingPricingsAIP(Mockito.any(SHAPIContext.class), 
				Mockito.any(PriceRequestList.class))).thenAnswer(new Answer<PriceResponseList>() {
				    @Override
				    public PriceResponseList answer(InvocationOnMock invocation) throws Throwable {
				      Object[] args = invocation.getArguments();
				      Object ctx = args[0];
				      PriceRequestList list = (PriceRequestList)args[1];
				      int size = list.getPriceRequest().size();
				      return getPriceResponseList (BASE_PRICE, BASE_SELL_FEE, size-1, 1);
				    }
				  });
		
		// Sim DB update call
		inventoryMgr = (InventoryMgr)mockClass(InventoryMgr.class, updateListingv2, "inventoryMgr");
		Mockito.when(inventoryMgr.updateListings(Mockito.any(List.class))).thenAnswer(new Answer<List<Listing>>() {
		    @Override
		    public List<Listing> answer(InvocationOnMock invocation) throws Throwable {
		      Object[] args = invocation.getArguments();
		      List<Listing> llist = (List<Listing>)args[0];
		      for ( int i=0; i<llist.size(); i++ ) {
		    	  llist.get(i).setId( (long)(1000 + i) );
		      }
		      return llist;
		    }
		  });
		
		// Sim DB add call
		Mockito.when(inventoryMgr.addListings(Mockito.any(List.class))).thenAnswer(new Answer<List<Listing>>() {
		    @Override
		    public List<Listing> answer(InvocationOnMock invocation) throws Throwable {
		      Object[] args = invocation.getArguments();
		      List<Listing> llist = (List<Listing>)args[0];
		      for ( int i=0; i<llist.size(); i++ ) {
		    	  llist.get(i).setId( (long)(1000 + i) );
		      }
		      return llist;
		    }
		  });
		
	}
	
	////// test methods go here
	
	/**
	 * createBulkListingTest1 
	 */
	@Test
	public void createListingError1Test () throws Exception
	{
		BulkListingInternal bli = new BulkListingInternal();
		
		try {
			List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses = listingCreateProcess.createListings(bli, clientIp, userAgent);
			Assert.fail("The createListings bulk call with no parameters should return an error");
		}
		catch ( Exception ex ) {
		}
	}
	
	/**
	 * createBulkListingTest1 
	 */
	@Test
	public void createListingTest() throws Exception
	{
		BulkListingInternal bli = new BulkListingInternal();
		
		try {
			Event event = super.getEvent();
			event.setParkingOnlyEvent(Boolean.TRUE);

			when(eventHelper.getEventById(Mockito.anyLong(),Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
			when(eventHelper.getEventObject(Mockito.any(Locale.class), Mockito.any(Listing.class), Mockito.anyLong(), Mockito.any(Boolean.class))).thenReturn(event);

			List<ListingRequest> reqlist = new ArrayList<ListingRequest>(); 
			
			reqlist.add( _makeCreateRequest (String.valueOf(event.getId()),null, "120", "section-100", "R10", 10  ) );
			reqlist.add( _makeCreateRequest (String.valueOf(event.getId()),null, "125", "section-100", "R11", 10  ) );
			reqlist.add( _makeCreateRequest (String.valueOf(event.getId()),null, "130", "section-100", "R12", 10  ) );
			reqlist.add( _makeCreateRequest (String.valueOf(event.getId()),null, "135", "section-100", "R13", 10  )) ;
			reqlist.add( _makeCreateRequest (String.valueOf(event.getId()),null, "140", "section-100", "R14", 10  )) ;
			
			bli.setCreateListingBody( reqlist );
			
			bli.setAssertion("---somesdymmtassertionvalue---");
			bli.setSellerId(1000010549L);
			bli.setSellerGuid("E4016068190C25E7E044002128BE217A");
			bli.setSellShStoreId(2);
			
			List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses = listingCreateProcess.createListings(bli, clientIp, userAgent);
			
			Assert.assertTrue( responses!=null && responses.size()==5 );
		}
		catch ( Exception ex ) {
			throw ex;
		}
	}
	
	
	@Test
	public void createListingTestException() throws Exception
	{
		BulkListingInternal bli = new BulkListingInternal();
		
		try {
			Event event = super.getEvent();
			event.setParkingOnlyEvent(Boolean.TRUE);
			
			ListingError error = new ListingError();
			error.setErrorCode("123");
			error.setMessage("message");
			ListingBusinessException e = new ListingBusinessException(error);
			

			when(eventHelper.getEventById(Mockito.anyLong(),Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
			when(eventHelper.getEventObject(Mockito.any(Locale.class), Mockito.any(Listing.class), Mockito.anyLong(), Mockito.any(Boolean.class))).thenThrow(e);

			List<ListingRequest> reqlist = new ArrayList<ListingRequest>(); 
			
			reqlist.add( _makeCreateRequest (String.valueOf(event.getId()),null, "120", "section-100", "R10", 10  ) );
			reqlist.add( _makeCreateRequest (String.valueOf(event.getId()),null, "125", "section-100", "R11", 10  ) );
			reqlist.add( _makeCreateRequest (String.valueOf(event.getId()),null, "130", "section-100", "R12", 10  ) );
			reqlist.add( _makeCreateRequest (String.valueOf(event.getId()),null, "135", "section-100", "R13", 10  )) ;
			reqlist.add( _makeCreateRequest (String.valueOf(event.getId()),null, "140", "section-100", "R14", 10  )) ;
			
			bli.setCreateListingBody( reqlist );
			
			bli.setAssertion("---somesdymmtassertionvalue---");
			bli.setSellerId(1000010549L);
			bli.setSellerGuid("E4016068190C25E7E044002128BE217A");
			bli.setSellShStoreId(2);
			
			List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses = listingCreateProcess.createListings(bli, clientIp, userAgent);
			
			Assert.assertTrue( responses!=null && responses.size()==5 );
		}
		catch ( Exception ex ) {
			throw ex;
		}
	}
	
	
	@Test
	public void createListingTestSTH() throws Exception
	{
		BulkListingInternal bli = new BulkListingInternal();
		
		try {
			Event event = super.getEvent();
			when(eventHelper.getEventById(Mockito.anyLong(),Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
			
			List<ListingRequest> reqlist = new ArrayList<ListingRequest>(); 
			
			reqlist.add( _makeCreateRequestSTH (String.valueOf(event.getId()),null, "120", "section-100", "R10", 10  ) );
			reqlist.add( _makeCreateRequestSTH (String.valueOf(event.getId()),null, "125", "section-100", "R11", 10  ) );
			reqlist.add( _makeCreateRequestSTH (String.valueOf(event.getId()),null, "130", "section-100", "R12", 10  ) );
			reqlist.add( _makeCreateRequestSTH (String.valueOf(event.getId()),null, "135", "section-100", "R13", 10  )) ;
			reqlist.add( _makeCreateRequestSTH (String.valueOf(event.getId()),null, "140", "section-100", "R14", 10  )) ;
			
			bli.setCreateListingBody( reqlist );
			
			bli.setAssertion("---somesdymmtassertionvalue---");
			bli.setSellerId(1000010549L);
			bli.setSellerGuid("E4016068190C25E7E044002128BE217A");
			bli.setSellShStoreId(2);
			
			List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses = listingCreateProcess.createListings(bli, clientIp, userAgent);
			
			Assert.assertTrue( responses!=null && responses.size()==5 );
		}
		catch ( Exception ex ) {
			throw ex;
		}
	}
	
	@Test
	public void createListingTestSellerEligibility() throws Exception
	{
		BulkListingInternal bli = new BulkListingInternal();
		
		try {
			Event event = super.getEvent();
			event.setCountry("US");
			when(eventHelper.getEventById(Mockito.anyLong(),Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
			when(sellerEligibilityHelper.checkSellerEligibility(anyString(), anyString(), anyLong())).thenReturn(false);
			
			List<ListingRequest> reqlist = new ArrayList<ListingRequest>(); 
			
			reqlist.add( _makeCreateRequestSTH (String.valueOf(event.getId()),null, "120", "section-100", "R10", 10  ) );
			reqlist.add( _makeCreateRequestSTH (String.valueOf(event.getId()),null, "125", "section-100", "R11", 10  ) );
			reqlist.add( _makeCreateRequestSTH (String.valueOf(event.getId()),null, "130", "section-100", "R12", 10  ) );
			reqlist.add( _makeCreateRequestSTH (String.valueOf(event.getId()),null, "135", "section-100", "R13", 10  )) ;
			reqlist.add( _makeCreateRequestSTH (String.valueOf(event.getId()),null, "140", "section-100", "R14", 10  )) ;
			
			bli.setCreateListingBody( reqlist );
			
			bli.setAssertion("---somesdymmtassertionvalue---");
			bli.setSellerId(1000010549L);
			bli.setSellerGuid("E4016068190C25E7E044002128BE217A");
			bli.setSellShStoreId(2);
			event.setCountry("US");
			List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses = listingCreateProcess.createListings(bli, clientIp, userAgent);
			List<ListingError> errors =responses.get(0).getErrors();
			Assert.assertEquals("NOT_ALLOWED_TO_LIST",errors.get(0).getCode().toString());
			Assert.assertEquals("Seller not allowed to list", errors.get(0).getMessage());
			Assert.assertNotNull(errors);
			Assert.assertTrue( responses!=null && responses.size()==5 );
		}
		catch ( Exception ex ) {
			throw ex;
		}
	}
	
	@Test
	public void createListingTestSellerEligibilityForHiddenListing() throws Exception
	{
		BulkListingInternal bli = new BulkListingInternal();
		
		try {
			Event event = super.getEvent();
			event.setCountry("US");
			when(eventHelper.getEventById(Mockito.anyLong(),Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
			when(sellerEligibilityHelper.checkSellerEligibility(anyString(), anyString(), anyLong())).thenReturn(false);
			
			List<ListingRequest> reqlist = new ArrayList<ListingRequest>(); 
			
			ListingRequest req = _makeCreateRequestSTH (String.valueOf(event.getId()),null, "120", "section-100", "R10", 10  );
			req.setStatus(ListingStatus.HIDDEN);
			req.setExternalListingId("1234");
			reqlist.add(req);
			
			bli.setCreateListingBody( reqlist );
			
			bli.setAssertion("---somesdymmtassertionvalue---");
			bli.setSellerId(1000010549L);
			bli.setSellerGuid("E4016068190C25E7E044002128BE217A");
			bli.setSellShStoreId(2);
			event.setCountry("US");
			List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses = listingCreateProcess.createListings(bli, clientIp, userAgent);
			List<ListingError> errors =responses.get(0).getErrors();
			Assert.assertNotSame("NOT_ALLOWED_TO_LIST",errors.get(0).getCode().toString());
			Assert.assertNotSame("Seller not allowed to list", errors.get(0).getMessage());
			Assert.assertNotNull(errors);
			Assert.assertTrue( responses!=null && responses.size()==1 );
		}
		catch ( Exception ex ) {
			throw ex;
		}
	}
	
	@Test
	public void createListingTestUK() throws Exception
	{
		BulkListingInternal bli = new BulkListingInternal();
		
		try {
			Event event = super.getEvent();
			event.setCountry("UK");
			when(eventHelper.getEventById(Mockito.anyLong(),Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
			
			List<ListingRequest> reqlist = new ArrayList<ListingRequest>(); 
			
			reqlist.add( _makeCreateRequestUK(String.valueOf(event.getId()),null, "120", "section-100", "R10", 10  ) );
			reqlist.add( _makeCreateRequestUK(String.valueOf(event.getId()),null, "125", "section-100", "R11", 10  ) );
			reqlist.add( _makeCreateRequestUK(String.valueOf(event.getId()),null, "130", "section-100", "R12", 10  ) );
			reqlist.add( _makeCreateRequestUK(String.valueOf(event.getId()),null, "135", "section-100", "R13", 10  )) ;
			reqlist.add( _makeCreateRequestUK(String.valueOf(event.getId()),null, "140", "section-100", "R14", 10  )) ;
			
			bli.setCreateListingBody( reqlist );
			
			bli.setAssertion("---somesdymmtassertionvalue---");
			bli.setSellerId(1000010549L);
			bli.setSellerGuid("E4016068190C25E7E044002128BE217A");
			
			List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses = listingCreateProcess.createListings(bli, clientIp, userAgent);
			
			Assert.assertTrue( responses!=null && responses.size()==5 );
		}
		catch ( Exception ex ) {
			throw ex;
		}
	}
	
	@Test
	public void createListingTestDE() throws Exception
	{
		BulkListingInternal bli = new BulkListingInternal();
		
		try {
			Event event = super.getEvent();
			when(eventHelper.getEventById(Mockito.anyLong(),Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
			
			List<ListingRequest> reqlist = new ArrayList<ListingRequest>(); 
			
			reqlist.add( _makeCreateRequestDE(String.valueOf(event.getId()),null, "120", "section-100", "R10", 10  ) );
			reqlist.add( _makeCreateRequestDE(String.valueOf(event.getId()),null, "125", "section-100", "R11", 10  ) );
			reqlist.add( _makeCreateRequestDE(String.valueOf(event.getId()),null, "130", "section-100", "R12", 10  ) );
			reqlist.add( _makeCreateRequestDE(String.valueOf(event.getId()),null, "135", "section-100", "R13", 10  )) ;
			reqlist.add( _makeCreateRequestDE(String.valueOf(event.getId()),null, "140", "section-100", "R14", 10  )) ;
			
			bli.setCreateListingBody( reqlist );
			
			bli.setAssertion("---somesdymmtassertionvalue---");
			bli.setSellerId(1000010549L);
			bli.setSellerGuid("E4016068190C25E7E044002128BE217A");
			
			List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses = listingCreateProcess.createListings(bli, clientIp, userAgent);
			
			Assert.assertTrue( responses!=null && responses.size()==5 );
		}
		catch ( Exception ex ) {
			throw ex;
		}
}
	
	@Test
	public void updateListing_NoActiveEvent() throws Exception
	{		
		when(eventHelper.getEventObject(Matchers.<Locale>any(), Matchers.<Listing>any(), Matchers.eq(1000l), Matchers.anyBoolean())).thenReturn(null);
		Listing listing = new Listing();
		listing.setId(1000l);
		listing.setEventId(1000l);
		listing.setSellerId(1000l);
		listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
		listing.setQuantity(10);
		listing.setQuantityRemain(8);
		listing.setRow("1");
		listing.setSeats("1,2,3,4,5,6,7,8,9,10");
		listing.setSystemStatus(ListingStatus.ACTIVE.name());
		listing.setDeliveryOption(1);
		when(inventoryMgr.getListing(anyLong())).thenReturn(listing);	
		Mockito.doAnswer(new Answer<Void>() {
	        @Override
	        public Void answer(InvocationOnMock invocation) throws Throwable {
	            return null;
	        }
	    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
		Listing newlisting = new Listing();
		newlisting.setId(1000l);
		newlisting.setSellerId(1000l);
		newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
		newlisting.setQuantity(6);

		List<ListingRequest> reqlist = new ArrayList<ListingRequest>(); 
		reqlist.add( _makeCreateRequest ("1000", null, "140", "section-100", "R14", 10  )) ;
		
		BulkListingInternal bli = new BulkListingInternal();		
		bli.setCreateListingBody( reqlist );
		
		bli.setAssertion("---somesdymmtassertionvalue---");
		bli.setSellerId(1000010549L);
		bli.setSellerGuid("E4016068190C25E7E044002128BE217A");
		
		List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses = listingCreateProcess.createListings(bli, clientIp, userAgent);
		Assert.assertTrue( responses.get(0).getErrors()!=null && responses.get(0).getErrors().size()>0
				&& responses.get(0).getErrors().get(0).getCode() == ErrorCode.INVALID_EVENTID
		);
	}

	@Test
	public void createListing_EventCountryNotAllow() throws Exception
	{
		ReflectionTestUtils.setField(listingCreateProcess, "countryWhiteList", new HashSet<>(Arrays.asList("MX")));

		Listing listing = new Listing();
		listing.setId(1000l);
		listing.setEventId(1000l);
		listing.setSellerId(1000l);
		listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
		listing.setQuantity(10);
		listing.setQuantityRemain(8);
		listing.setRow("1");
		listing.setSeats("1,2,3,4,5,6,7,8,9,10");
		listing.setSystemStatus(ListingStatus.ACTIVE.name());
		listing.setDeliveryOption(1);
		when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
		Mockito.doAnswer(new Answer<Void>() {
	        @Override
	        public Void answer(InvocationOnMock invocation) throws Throwable {
	            return null;
	        }
	    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
		Listing newlisting = new Listing();
		newlisting.setId(1000l);
		newlisting.setSellerId(1000l);
		newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
		newlisting.setQuantity(6);

		List<ListingRequest> reqlist = new ArrayList<ListingRequest>();
		reqlist.add( _makeCreateRequest ("1000", null, "140", "section-100", "R14", 10  )) ;

		BulkListingInternal bli = new BulkListingInternal();
		bli.setCreateListingBody( reqlist );

		bli.setAssertion("---somesdymmtassertionvalue---");
		bli.setSellerId(1000010549L);
		bli.setSellerGuid("E4016068190C25E7E044002128BE217A");

		List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses = listingCreateProcess.createListings(bli, clientIp, userAgent);
		Assert.assertTrue( responses.get(0).getErrors()!=null && responses.get(0).getErrors().size()>0
				&& responses.get(0).getErrors().get(0).getCode() == ErrorCode.INVALID_EVENTID
		);
	}
	
	//// VALIDATIONS FOR UPDATE (bulk or no bulk) //////

	@Test
	public void validateQuantityUpdatesBulk () throws Exception
	{
		ListingRequest request = new ListingRequest();
		
		BulkListingInternal bli = new BulkListingInternal();
		List<ListingRequest> requests = new ArrayList<ListingRequest>(1);
		requests.add(request);
		bli.setCreateListingBody(requests);
		bli.setSellerId( 1000l );
		bli.setSellerGuid( "abcdef" );
		bli.setAssertion("-------some-dummy-assertion----------");
		request.setExternalListingId("12345");
		request.setListingId(23456l);
		request.setQuantity(0);
		List<ListingResponse> responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		ListingResponse resp = responses.get(0);
		
		// expected failure
		Assert.assertTrue( "Quantity == 0 should never be allowed!", 
				resp!=null && resp.getErrors()!=null || resp.getErrors().size()>0 );
		
		request.setQuantity(30000);
		responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		resp = responses.get(0);

		// expected failure
		Assert.assertTrue ("It should not alow quantity == 30000",
				resp != null && (resp.getErrors()!=null || resp.getErrors().size()>0));
		
		// expect success
		request.setQuantity(20);
		responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		resp = responses.get(0);
		
		Assert.assertTrue ("It should not alow quantity == 20. No products are provided",
				resp != null && resp.getErrors()!=null || resp.getErrors().size()>0 );
		try {
			request.setQuantity(0);
			
			List<ListingResponse> responses1 = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		}
		
		catch ( Throwable th ) {
			Assert.fail( "Invalid quantity==0 should have thrown SHBadRequestException ");
		}
		
		try {
			
			
			List<ListingResponse> responses1 = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, false);
		}
		
		catch ( Throwable th ) {
		Assert.assertTrue("Unable to process the request from error, please re-try",resp != null && (resp.getErrors()!=null || resp.getErrors().size()>0));
		}
		
		
	}
	
	@Test
	public void validateQuantityUpdatesNoBulk () throws Exception
	{
		ListingRequest request = new ListingRequest();
		
		BulkListingInternal bli = new BulkListingInternal();
		List<ListingRequest> requests = new ArrayList<ListingRequest>(1);
		requests.add(request);
		bli.setCreateListingBody(requests);
		bli.setSellerId( 1000l );
		bli.setSellerGuid( "abcdef" );
		bli.setAssertion("-------some-dummy-assertion----------");

		try {
			request.setQuantity(0);
			List<ListingResponse> responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, false);
			Assert.fail( "Invalid quantity==0 should have thrown SHBadRequestException");
		}
		catch ( SHBadRequestException bre ) {
		}
		catch ( Throwable th ) {
			Assert.fail( "Invalid quantity==0 should have thrown SHBadRequestException ");
		}
	}

	@Test
	public void validatePricingUpdates () throws Exception
	{
		when(inventoryMgr.getListing(anyLong())).thenReturn(getListing(1000l, "sec-10", "R1", "1,2,3,4,5", 5, 5));	
		
		ticketSeatMgr = (TicketSeatMgr)mockClass( TicketSeatMgr.class, null, null );	
		when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(getTicketSeats(1000L,"sec-10", "R1", 5));
		setBeanProperty (updateListingv2, "ticketSeatMgr", ticketSeatMgr);
		setBeanProperty (listingCreateProcess, "inventoryMgr", inventoryMgr );
		
		ListingRequest request = new ListingRequest();
		
		BulkListingInternal bli = new BulkListingInternal();
		List<ListingRequest> requests = new ArrayList<ListingRequest>(1);
		requests.add(request);
		bli.setCreateListingBody(requests);
		bli.setSellerId( 1000l );
		bli.setSellerGuid( "abcdef" );
		bli.setAssertion("-------some-dummy-assertion----------");
		
		request.setBuyerSeesPerProduct(new com.stubhub.newplatform.common.entity.Money("10"));
		request.setFaceValue(new com.stubhub.newplatform.common.entity.Money("5"));
		request.setPurchasePrice(new com.stubhub.newplatform.common.entity.Money("5"));
		request.setDeliveryOption(DeliveryOption.BARCODE);
		request.setSplitOption(SplitOption.NOSINGLES);
		request.setListingId(1000l);
		request.setQuantity(2);
		request.setEventId( String.valueOf(globalEvent.getId() ));
		
		// Sim new price API call		
		ListingPriceDetailsHelper listingPriceDetailsHelperMock = 
			(ListingPriceDetailsHelper)mockClass(ListingPriceDetailsHelper.class, updateListingv2, "listingPriceDetailsHelper");
		
		Mockito.when(listingPriceDetailsHelperMock.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class), 
				Mockito.anyMap(), Mockito.anyList(), Mockito.anyList())).thenReturn(new Object[1]);
		
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("UK,GB,DE");
		List<ListingResponse> responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		ListingResponse resp = responses.get(0);
		Assert.assertTrue("Simple price updates should work", 
				resp != null && (resp.getErrors() == null || resp.getErrors().size() == 0));
				
		request.setBuyerSeesPerProduct(new com.stubhub.newplatform.common.entity.Money("10000"));
		request.setEventId( String.valueOf(globalEvent.getId() ));
		
		responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		resp = responses.get(0);
		Assert.assertTrue("BuyerSeesPerProduct == 10000 should be valid", 
				resp != null && (resp.getErrors()==null || resp.getErrors().size()==0));

		request.setEventId( String.valueOf(globalEvent.getId() ));
		request.setBuyerSeesPerProduct(new com.stubhub.newplatform.common.entity.Money("0"));
		responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		
		resp = responses.get(0);
		Assert.assertTrue("BuyerSeesPerProduct == 0 should be invalid", 
				resp != null && (resp.getErrors()!=null || resp.getErrors().size()>0));
	}
	
	@Test
	public void validateProductUpdates () throws Exception
	{
		Listing curListing = getListing(1000l, "sec-10", "R1", "1,2", 5, 2);
		when(inventoryMgr.getListing(anyLong())).thenReturn(curListing);	
		
		ticketSeatMgr = (TicketSeatMgr)mockClass( TicketSeatMgr.class, null, null );	
		when(ticketSeatMgr.findActiveTicketSeatsByTicketId(anyLong())).thenReturn(getTicketSeats(1000L,"sec-10", "R1", 2));
		setBeanProperty (updateListingv2, "ticketSeatMgr", ticketSeatMgr);
		setBeanProperty (listingCreateProcess, "inventoryMgr", inventoryMgr );
		
		PrimaryIntegrationUtil primaryIntegrationUtil = Mockito.mock(PrimaryIntegrationUtil.class);
		setBeanProperty (updateListingv2, "primaryIntegrationUtil", primaryIntegrationUtil);
		
		ListingRequest request = new ListingRequest();
		request.setListingId(1000l);
		request.setEventId( String.valueOf(globalEvent.getId() ));		
		
		BulkListingInternal bli = new BulkListingInternal();
		List<ListingRequest> requests = new ArrayList<ListingRequest>(1);
		requests.add(request);
		bli.setCreateListingBody(requests);
		bli.setSellerId( 1000l );
		bli.setSellerGuid( "abcdef" );
		bli.setAssertion("-------some-dummy-assertion----------");
		
		ArrayList<Product> products = new ArrayList<Product>();
		Product prod = new Product();
		prod.setFulfillmentArtifact("10000");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("10");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		
		prod = new Product();
		prod.setFulfillmentArtifact("10010");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("11");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		request.setProducts(products);
		
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("UK,GB,DE");
		List<ListingResponse>  responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		ListingResponse resp = responses.get(0);
		
		// expect success
		Assert.assertTrue("Produts updates passed should be valid", 
				resp != null && (resp.getErrors() == null || resp.getErrors().size()==0) );

		products.clear();
		prod = new Product();
		prod.setFulfillmentArtifact("10000");
		prod.setOperation(Operation.ADD);
		prod.setRow("10,20");		// invalid
		prod.setSeat ("200,201");	// invalid
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		request.setProducts(products);
		
		responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		resp = responses.get(0);
		Assert.assertTrue ("Porduct rows or seats that are CSV should not be allowed!!", 
				resp != null && (resp.getErrors()!=null || resp.getErrors().size()>0) );
	}
	
	@Test
	public void validateProductUpdatesPP () throws Exception
	{
		Listing curListing = getListing(1000l, "sec-10", "R1", "1,2", 5, 2);
		when(inventoryMgr.getListing(anyLong())).thenReturn(curListing);	
		Event event=globalEvent;
		event.setParkingOnlyEvent(Boolean.TRUE);
		when(eventHelper.getEventObject(Mockito.any(Locale.class), Mockito.any(Listing.class), Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(event);
		setBeanProperty(listingCreateProcess, "eventHelper", eventHelper );

		
		ticketSeatMgr = (TicketSeatMgr)mockClass( TicketSeatMgr.class, null, null );	
		when(ticketSeatMgr.findActiveTicketSeatsByTicketId(anyLong())).thenReturn(getTicketSeats(1000L,"sec-10", "R1", 2));
		setBeanProperty (updateListingv2, "ticketSeatMgr", ticketSeatMgr);
		setBeanProperty (listingCreateProcess, "inventoryMgr", inventoryMgr );
		
		PrimaryIntegrationUtil primaryIntegrationUtil = Mockito.mock(PrimaryIntegrationUtil.class);
		setBeanProperty (updateListingv2, "primaryIntegrationUtil", primaryIntegrationUtil);
		
		ListingRequest request = new ListingRequest();
		request.setListingId(1000l);
		request.setEventId( String.valueOf(globalEvent.getId() ));		
		
		BulkListingInternal bli = new BulkListingInternal();
		List<ListingRequest> requests = new ArrayList<ListingRequest>(1);
		requests.add(request);
		bli.setCreateListingBody(requests);
		bli.setSellerId( 1000l );
		bli.setSellerGuid( "abcdef" );
		bli.setAssertion("-------some-dummy-assertion----------");
		
		ArrayList<Product> products = new ArrayList<Product>();
		Product prod = new Product();
		prod.setFulfillmentArtifact("10000");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("10");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		
		prod = new Product();
		prod.setFulfillmentArtifact("10010");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("11");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		request.setProducts(products);
		List<TicketTrait> ticketTraits=new ArrayList<TicketTrait>();
		TicketTrait tt=new TicketTrait();
		tt.setId("101");
		tt.setType("Seat");
		ticketTraits.add(tt);
		request.setTicketTraits(ticketTraits);
		
		
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("UK,GB,DE");
		List<ListingResponse>  responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		ListingResponse resp = responses.get(0);
		
		// expect success
		Assert.assertTrue("Produts updates passed should be valid", 
				resp != null && (resp.getErrors()==null || resp.getErrors().size()==0) );

		products.clear();
		prod = new Product();
		prod.setFulfillmentArtifact("10000");
		prod.setOperation(Operation.ADD);
		prod.setRow("10,20");		// invalid
		prod.setSeat ("200,201");	// invalid
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		request.setProducts(products);
		
		responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		resp = responses.get(0);
		Assert.assertTrue ("Porduct rows or seats that are CSV should not be allowed!!", 
				resp != null && (resp.getErrors()!=null || resp.getErrors().size()>0) );
	}
	
	@Test
	public void validateProductUpdatesPPTTName () throws Exception
	{
		Listing curListing = getListing(1000l, "sec-10", "R1", "1,2", 5, 2);
		when(inventoryMgr.getListing(anyLong())).thenReturn(curListing);	
		Event event=globalEvent;
		event.setParkingOnlyEvent(Boolean.TRUE);
		when(eventHelper.getEventObject(Mockito.any(Locale.class), Mockito.any(Listing.class), Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(event);
		setBeanProperty(listingCreateProcess, "eventHelper", eventHelper );

		
		ticketSeatMgr = (TicketSeatMgr)mockClass( TicketSeatMgr.class, null, null );	
		when(ticketSeatMgr.findActiveTicketSeatsByTicketId(anyLong())).thenReturn(getTicketSeats(1000L,"sec-10", "R1", 2));
		setBeanProperty (updateListingv2, "ticketSeatMgr", ticketSeatMgr);
		setBeanProperty (listingCreateProcess, "inventoryMgr", inventoryMgr );
		
		PrimaryIntegrationUtil primaryIntegrationUtil = Mockito.mock(PrimaryIntegrationUtil.class);
		setBeanProperty (updateListingv2, "primaryIntegrationUtil", primaryIntegrationUtil);
		
		ListingRequest request = new ListingRequest();
		request.setListingId(1000l);
		request.setEventId( String.valueOf(globalEvent.getId() ));		
		
		BulkListingInternal bli = new BulkListingInternal();
		List<ListingRequest> requests = new ArrayList<ListingRequest>(1);
		requests.add(request);
		bli.setCreateListingBody(requests);
		bli.setSellerId( 1000l );
		bli.setSellerGuid( "abcdef" );
		bli.setAssertion("-------some-dummy-assertion----------");
		
		ArrayList<Product> products = new ArrayList<Product>();
		Product prod = new Product();
		prod.setFulfillmentArtifact("10000");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("10");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		
		prod = new Product();
		prod.setFulfillmentArtifact("10010");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("11");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		request.setProducts(products);
		List<TicketTrait> ticketTraits=new ArrayList<TicketTrait>();
		TicketTrait tt=new TicketTrait();
		tt.setId("1");
		tt.setName("Parking pass");
		ticketTraits.add(tt);
		request.setTicketTraits(ticketTraits);
		
		
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("UK,GB,DE");
		List<ListingResponse>  responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		ListingResponse resp = responses.get(0);
		
		// expect success
		Assert.assertTrue("Produts updates passed should be valid", 
				resp != null && (resp.getErrors()==null || resp.getErrors().size()==0) );

		products.clear();
		prod = new Product();
		prod.setFulfillmentArtifact("10000");
		prod.setOperation(Operation.ADD);
		prod.setRow("10,20");		// invalid
		prod.setSeat ("200,201");	// invalid
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		request.setProducts(products);
		
		responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		resp = responses.get(0);
		Assert.assertTrue ("Porduct rows or seats that are CSV should not be allowed!!", 
				resp != null && (resp.getErrors()!=null || resp.getErrors().size()>0) );
	}
	
	@Test
	public void validateProductUpdatesPPTT102 () throws Exception
	{
		Listing curListing = getListing(1000l, "sec-10", "R1", "1,2", 5, 2);
		when(inventoryMgr.getListing(anyLong())).thenReturn(curListing);	
		Event event=globalEvent;
		event.setParkingOnlyEvent(Boolean.TRUE);
		when(eventHelper.getEventObject(Mockito.any(Locale.class), Mockito.any(Listing.class), Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(event);
		setBeanProperty(listingCreateProcess, "eventHelper", eventHelper );

		
		ticketSeatMgr = (TicketSeatMgr)mockClass( TicketSeatMgr.class, null, null );	
		when(ticketSeatMgr.findActiveTicketSeatsByTicketId(anyLong())).thenReturn(getTicketSeats(1000L,"sec-10", "R1", 2));
		setBeanProperty (updateListingv2, "ticketSeatMgr", ticketSeatMgr);
		setBeanProperty (listingCreateProcess, "inventoryMgr", inventoryMgr );
		
		PrimaryIntegrationUtil primaryIntegrationUtil = Mockito.mock(PrimaryIntegrationUtil.class);
		setBeanProperty (updateListingv2, "primaryIntegrationUtil", primaryIntegrationUtil);
		
		ListingRequest request = new ListingRequest();
		request.setListingId(1000l);
		request.setEventId( String.valueOf(globalEvent.getId() ));		
		
		BulkListingInternal bli = new BulkListingInternal();
		List<ListingRequest> requests = new ArrayList<ListingRequest>(1);
		requests.add(request);
		bli.setCreateListingBody(requests);
		bli.setSellerId( 1000l );
		bli.setSellerGuid( "abcdef" );
		bli.setAssertion("-------some-dummy-assertion----------");
		
		ArrayList<Product> products = new ArrayList<Product>();
		Product prod = new Product();
		prod.setFulfillmentArtifact("10000");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("10");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		
		prod = new Product();
		prod.setFulfillmentArtifact("10010");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("11");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		request.setProducts(products);
		List<TicketTrait> ticketTraits=new ArrayList<TicketTrait>();
		TicketTrait tt=new TicketTrait();
		tt.setId("102");
		tt.setType("Seat");
		ticketTraits.add(tt);
		request.setTicketTraits(ticketTraits);
		
		
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("UK,GB,DE");
		List<ListingResponse>  responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		ListingResponse resp = responses.get(0);
		
		// expect success
		Assert.assertTrue("Produts updates passed should be valid", 
				resp != null && (resp.getErrors()==null || resp.getErrors().size()==0) );

		products.clear();
		prod = new Product();
		prod.setFulfillmentArtifact("10000");
		prod.setOperation(Operation.ADD);
		prod.setRow("10,20");		// invalid
		prod.setSeat ("200,201");	// invalid
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		request.setProducts(products);
		
		responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		resp = responses.get(0);
		Assert.assertTrue ("Porduct rows or seats that are CSV should not be allowed!!", 
				resp != null && (resp.getErrors()!=null || resp.getErrors().size()>0) );
	}
	
	@Test
	public void validateProductUpdatesPPTTNull () throws Exception
	{
		Listing curListing = getListing(1000l, "sec-10", "R1", "1,2,3,4,5", 5, 5);
		when(inventoryMgr.getListing(anyLong())).thenReturn(curListing);	
		Event event=globalEvent;
		event.setParkingOnlyEvent(Boolean.TRUE);
		when(eventHelper.getEventObject(Mockito.any(Locale.class), Mockito.any(Listing.class), Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(event);
		setBeanProperty(listingCreateProcess, "eventHelper", eventHelper );

		
		ticketSeatMgr = (TicketSeatMgr)mockClass( TicketSeatMgr.class, null, null );	
		when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(getTicketSeats(1000L,"sec-10", "R1", 5));
		setBeanProperty (updateListingv2, "ticketSeatMgr", ticketSeatMgr);
		setBeanProperty (listingCreateProcess, "inventoryMgr", inventoryMgr );
		
		PrimaryIntegrationUtil primaryIntegrationUtil = Mockito.mock(PrimaryIntegrationUtil.class);
		setBeanProperty (updateListingv2, "primaryIntegrationUtil", primaryIntegrationUtil);
		
		ListingRequest request = new ListingRequest();
		request.setListingId(1000l);
		request.setEventId( String.valueOf(globalEvent.getId() ));		
		
		BulkListingInternal bli = new BulkListingInternal();
		List<ListingRequest> requests = new ArrayList<ListingRequest>(1);
		requests.add(request);
		bli.setCreateListingBody(requests);
		bli.setSellerId( 1000l );
		bli.setSellerGuid( "abcdef" );
		bli.setAssertion("-------some-dummy-assertion----------");
		
		ArrayList<Product> products = new ArrayList<Product>();
		Product prod = new Product();
		prod.setFulfillmentArtifact("10000");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("10");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		
		prod = new Product();
		prod.setFulfillmentArtifact("10010");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("11");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		request.setProducts(products);
		List<TicketTrait> ticketTraits=new ArrayList<TicketTrait>();
		TicketTrait tt=new TicketTrait();
		tt.setId("");
		tt.setType("Seat");
		ticketTraits.add(tt);
		request.setTicketTraits(ticketTraits);
		
		
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("UK,GB,DE");
		List<ListingResponse>  responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		ListingResponse resp = responses.get(0);
		
		

		products.clear();
		prod = new Product();
		prod.setFulfillmentArtifact("10000");
		prod.setOperation(Operation.ADD);
		prod.setRow("10,20");		// invalid
		prod.setSeat ("200,201");	// invalid
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		request.setProducts(products);
		
		responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		resp = responses.get(0);
		Assert.assertTrue ("Porduct rows or seats that are CSV should not be allowed!!", 
				resp != null && (resp.getErrors()!=null || resp.getErrors().size()>0) );
	}
	
	@Test
	public void validateProductUpdatesPPNoTT () throws Exception
	{
		Listing curListing = getListing(1000l, "sec-10", "R1", "1,2", 5, 2);
		when(inventoryMgr.getListing(anyLong())).thenReturn(curListing);	
		Event event=globalEvent;
		event.setParkingOnlyEvent(Boolean.TRUE);
		when(eventHelper.getEventObject(Mockito.any(Locale.class), Mockito.any(Listing.class), Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(event);
		setBeanProperty(listingCreateProcess, "eventHelper", eventHelper );

		
		ticketSeatMgr = (TicketSeatMgr)mockClass( TicketSeatMgr.class, null, null );	
		when(ticketSeatMgr.findActiveTicketSeatsByTicketId(anyLong())).thenReturn(getTicketSeats(1000L,"sec-10", "R1", 2));
		setBeanProperty (updateListingv2, "ticketSeatMgr", ticketSeatMgr);
		setBeanProperty (listingCreateProcess, "inventoryMgr", inventoryMgr );
		
		PrimaryIntegrationUtil primaryIntegrationUtil = Mockito.mock(PrimaryIntegrationUtil.class);
		setBeanProperty (updateListingv2, "primaryIntegrationUtil", primaryIntegrationUtil);
		
		ListingRequest request = new ListingRequest();
		request.setListingId(1000l);
		request.setEventId( String.valueOf(globalEvent.getId() ));		
		
		BulkListingInternal bli = new BulkListingInternal();
		List<ListingRequest> requests = new ArrayList<ListingRequest>(1);
		requests.add(request);
		bli.setCreateListingBody(requests);
		bli.setSellerId( 1000l );
		bli.setSellerGuid( "abcdef" );
		bli.setAssertion("-------some-dummy-assertion----------");
		
		ArrayList<Product> products = new ArrayList<Product>();
		Product prod = new Product();
		prod.setFulfillmentArtifact("10000");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("10");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		
		prod = new Product();
		prod.setFulfillmentArtifact("10010");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("11");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		request.setProducts(products);
		request.setTicketTraits(null);
		
		
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("UK,GB,DE");
		List<ListingResponse>  responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		ListingResponse resp = responses.get(0);
		
		// expect success
		Assert.assertTrue("Produts updates passed should be valid", 
				resp != null && (resp.getErrors()==null || resp.getErrors().size()==0) );

		products.clear();
		prod = new Product();
		prod.setFulfillmentArtifact("10000");
		prod.setOperation(Operation.ADD);
		prod.setRow("10,20");		// invalid
		prod.setSeat ("200,201");	// invalid
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		request.setProducts(products);
		
		responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		resp = responses.get(0);
		Assert.assertTrue ("Porduct rows or seats that are CSV should not be allowed!!", 
				resp != null && (resp.getErrors()!=null || resp.getErrors().size()>0) );
	}
	
	@Test
	public void validateProductUpdatesPPFalse () throws Exception
	{
		Listing curListing = getListing(1000l, "sec-10", "R1", "1,2", 5, 2);
		when(inventoryMgr.getListing(anyLong())).thenReturn(curListing);	
		Event event=globalEvent;
		event.setParkingOnlyEvent(Boolean.FALSE);
		when(eventHelper.getEventObject(Mockito.any(Locale.class), Mockito.any(Listing.class), Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(event);
		setBeanProperty(listingCreateProcess, "eventHelper", eventHelper );

		
		ticketSeatMgr = (TicketSeatMgr)mockClass( TicketSeatMgr.class, null, null );	
		when(ticketSeatMgr.findActiveTicketSeatsByTicketId(anyLong())).thenReturn(getTicketSeats(1000L,"sec-10", "R1", 2));
		setBeanProperty (updateListingv2, "ticketSeatMgr", ticketSeatMgr);
		setBeanProperty (listingCreateProcess, "inventoryMgr", inventoryMgr );
		
		PrimaryIntegrationUtil primaryIntegrationUtil = Mockito.mock(PrimaryIntegrationUtil.class);
		setBeanProperty (updateListingv2, "primaryIntegrationUtil", primaryIntegrationUtil);
		
		ListingRequest request = new ListingRequest();
		request.setListingId(1000l);
		request.setEventId( String.valueOf(globalEvent.getId() ));		
		
		BulkListingInternal bli = new BulkListingInternal();
		List<ListingRequest> requests = new ArrayList<ListingRequest>(1);
		requests.add(request);
		bli.setCreateListingBody(requests);
		bli.setSellerId( 1000l );
		bli.setSellerGuid( "abcdef" );
		bli.setAssertion("-------some-dummy-assertion----------");
		
		ArrayList<Product> products = new ArrayList<Product>();
		Product prod = new Product();
		prod.setFulfillmentArtifact("10000");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("10");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		
		prod = new Product();
		prod.setFulfillmentArtifact("10010");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("11");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		request.setProducts(products);
		List<TicketTrait> ticketTraits=new ArrayList<TicketTrait>();
		TicketTrait tt=new TicketTrait();
		tt.setId("101");
		tt.setType("Seat");
		ticketTraits.add(tt);
		request.setTicketTraits(ticketTraits);
		
		
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("UK,GB,DE");
		List<ListingResponse>  responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		ListingResponse resp = responses.get(0);
		
		// expect success
		Assert.assertTrue("Produts updates passed should be valid", 
				resp != null && (resp.getErrors()==null || resp.getErrors().size()==0) );

		products.clear();
		prod = new Product();
		prod.setFulfillmentArtifact("10000");
		prod.setOperation(Operation.ADD);
		prod.setRow("10,20");		// invalid
		prod.setSeat ("200,201");	// invalid
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		request.setProducts(products);
		
		responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		resp = responses.get(0);
		Assert.assertTrue ("Porduct rows or seats that are CSV should not be allowed!!", 
				resp != null && (resp.getErrors()!=null || resp.getErrors().size()>0) );
	}
	
	@Test
	public void validateProductUpdatesPPNullProducts () throws Exception
	{
		Listing curListing = getListing(1000l, "sec-10", "R1", "1,2,3,4,5", 5, 5);
		when(inventoryMgr.getListing(anyLong())).thenReturn(curListing);	
		Event event=globalEvent;
		event.setParkingOnlyEvent(Boolean.TRUE);
		when(eventHelper.getEventObject(Mockito.any(Locale.class), Mockito.any(Listing.class), Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(event);
		setBeanProperty(listingCreateProcess, "eventHelper", eventHelper );

		
		ticketSeatMgr = (TicketSeatMgr)mockClass( TicketSeatMgr.class, null, null );	
		when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(getTicketSeats(1000L,"sec-10", "R1", 5));
		setBeanProperty (updateListingv2, "ticketSeatMgr", ticketSeatMgr);
		setBeanProperty (listingCreateProcess, "inventoryMgr", inventoryMgr );
		
		PrimaryIntegrationUtil primaryIntegrationUtil = Mockito.mock(PrimaryIntegrationUtil.class);
		setBeanProperty (updateListingv2, "primaryIntegrationUtil", primaryIntegrationUtil);
		
		ListingRequest request = new ListingRequest();
		request.setListingId(1000l);
		request.setEventId( String.valueOf(globalEvent.getId() ));		
		
		BulkListingInternal bli = new BulkListingInternal();
		List<ListingRequest> requests = new ArrayList<ListingRequest>(1);
		requests.add(request);
		bli.setCreateListingBody(requests);
		bli.setSellerId( 1000l );
		bli.setSellerGuid( "abcdef" );
		bli.setAssertion("-------some-dummy-assertion----------");
		
		request.setProducts(null);
		List<TicketTrait> ticketTraits=new ArrayList<TicketTrait>();
		TicketTrait tt=new TicketTrait();
		tt.setId("101");
		tt.setType("Seat");
		ticketTraits.add(tt);
		request.setTicketTraits(ticketTraits);
		
		
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("UK,GB,DE");
		List<ListingResponse>  responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		ListingResponse resp = responses.get(0);
		
		// expect success
		Assert.assertTrue("Produts updates passed should be valid", 
				resp != null && (resp.getErrors()==null || resp.getErrors().size()==0) );

		responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, true);
		resp = responses.get(0);
	}
	
	@Test
	public void createOrUpdateListingWithNoLoaleInRequest () throws Exception
	{
		Event event=globalEvent;
		event.setParkingOnlyEvent(Boolean.FALSE);
		event.setLocale("en_US");
		when(eventHelper.getEventObject(Mockito.any(Locale.class), Mockito.any(Listing.class), Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(event);
		setBeanProperty(listingCreateProcess, "eventHelper", eventHelper );

		
			
		ListingRequest request = new ListingRequest();
		request.setListingId(1000l);
		request.setEventId( String.valueOf(globalEvent.getId() ));		
		
		BulkListingInternal bli = new BulkListingInternal();
		List<ListingRequest> requests = new ArrayList<ListingRequest>(1);
		requests.add(request);
		bli.setCreateListingBody(requests);
		bli.setSellerId( 1000l );
		bli.setSellerGuid( "abcdef" );
		bli.setAssertion("-------some-dummy-assertion----------");
		
		ArrayList<Product> products = new ArrayList<Product>();
		Product prod = new Product();
		prod.setFulfillmentArtifact("10000");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("10");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		
		prod = new Product();
		prod.setFulfillmentArtifact("10010");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("11");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		request.setProducts(products);
		List<TicketTrait> ticketTraits=new ArrayList<TicketTrait>();
		TicketTrait tt=new TicketTrait();
		tt.setId("101");
		tt.setType("Seat");
		ticketTraits.add(tt);
		request.setTicketTraits(ticketTraits);	
		
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("UK,GB,DE");
		List<ListingResponse>  responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, true, true);
		Assert.assertTrue( responses!=null && responses.size()==1 );

		
	}
	
	@Test
	public void createOrUpdateListingWithLoaleInRequest () throws Exception
	{
		Event event=globalEvent;
		event.setParkingOnlyEvent(Boolean.FALSE);
		event.setLocale("en_US");
		when(eventHelper.getEventObject(Mockito.any(Locale.class), Mockito.any(Listing.class), Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(event);
		setBeanProperty(listingCreateProcess, "eventHelper", eventHelper );

		
			
		ListingRequest request = new ListingRequest();
		request.setListingId(1000l);
		request.setEventId( String.valueOf(globalEvent.getId() ));	
		
		BulkListingInternal bli = new BulkListingInternal();
		List<ListingRequest> requests = new ArrayList<ListingRequest>(1);
		requests.add(request);
		bli.setCreateListingBody(requests);
		bli.setSellerId( 1000l );
		bli.setLocale(new Locale("en_US"));
		bli.setSellerGuid( "abcdef" );
		bli.setAssertion("-------some-dummy-assertion----------");
		
		ArrayList<Product> products = new ArrayList<Product>();
		Product prod = new Product();
		prod.setFulfillmentArtifact("10000");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("10");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		
		prod = new Product();
		prod.setFulfillmentArtifact("10010");
		prod.setOperation(Operation.ADD);
		prod.setRow("R1");
		prod.setSeat ("11");
		prod.setProductType(ProductType.TICKET);
		products.add(prod);
		request.setProducts(products);
		List<TicketTrait> ticketTraits=new ArrayList<TicketTrait>();
		TicketTrait tt=new TicketTrait();
		tt.setId("101");
		tt.setType("Seat");
		ticketTraits.add(tt);
		request.setTicketTraits(ticketTraits);	
		
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("UK,GB,DE");
		List<ListingResponse>  responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, true, true);
		Assert.assertTrue( responses!=null && responses.size()==1 );
		
	}
	
	
	@Test
	public void createListingWithEventInfoTest() throws Exception
	{
		BulkListingInternal bli = new BulkListingInternal();
		ShipEvent shipEvent = new ShipEvent();
		shipEvent.setId(12345);
		when(eventMapperAdaptor.mapEvent(Mockito.any(Locale.class), Mockito.any(EventInfo.class), Mockito.anyString())).thenReturn(shipEvent);
		Event event = super.getEvent();
		when(eventHelper.getEventById(Mockito.anyLong(),Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		
		List<ListingRequest> reqlist = new ArrayList<ListingRequest>(); 
		
		reqlist.add( _makeCreateRequest (null, "Giants", "12", "section-111", "R10", 1  ) );
		reqlist.add( _makeCreateRequest (null, "Giants", "13", "section-111", "R11", 1  ) );
		
		bli.setCreateListingBody( reqlist );
		
		bli.setAssertion("---somesdymmtassertionvalue---");
		bli.setSellerId(1000010549L);
		bli.setSellerGuid("E4016068190C25E7E044002128BE217A");
		
		List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses = listingCreateProcess.createListings(bli, clientIp, userAgent);
		Assert.assertTrue( responses!=null && responses.size()==2 );
	}
	
	@Test
	public void createListingWithEventInfoTestError() throws Exception
	{
		BulkListingInternal bli = new BulkListingInternal();
		when(eventMapperAdaptor.mapEvent(Mockito.any(Locale.class), Mockito.any(EventInfo.class), Mockito.anyString())).thenReturn(null);
		Event event = super.getEvent();
		when(eventHelper.getEventById(Mockito.anyLong(),Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		
		List<ListingRequest> reqList = new ArrayList<ListingRequest>(); 
		reqList.add( _makeCreateRequest (null, "Giants", "12", "section-111", "R10", 1  ) );
		reqList.add( _makeCreateRequest (null, "Giants", "13", "section-111", "R11", 1  ) );
		bli.setCreateListingBody( reqList );
		bli.setAssertion("---somesdymmtassertionvalue---");
		bli.setSellerId(1000010549L);
		bli.setSellerGuid("E4016068190C25E7E044002128BE217A");
		
		List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses = listingCreateProcess.createListings(bli, clientIp, userAgent);
		Assert.assertTrue( responses!=null && responses.size()==2 );
		Assert.assertNotNull(responses.get(0).getErrors());
		
		List<ListingRequest> reqList1 = new ArrayList<ListingRequest>();
		ListingRequest req1 = _makeCreateRequest (null, "Giants", "12", "section-111", "R10", 1  );
		req1.getEvent().setVenue("");
		reqList1.add( req1 );
		bli.setCreateListingBody( reqList1 );
		
		List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses1 = listingCreateProcess.createListings(bli, clientIp, userAgent);
		Assert.assertTrue( responses1!=null && responses1.size()==1 );
		Assert.assertNotNull(responses1.get(0).getErrors());
		
		List<ListingRequest> reqList2 = new ArrayList<ListingRequest>();
		ListingRequest req2 = _makeCreateRequest (null, "Giants", "12", "section-111", "R10", 1  );
		req2.getEvent().setName("");
		reqList2.add( req2 );
		bli.setCreateListingBody( reqList2 );
		
		List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses2 = listingCreateProcess.createListings(bli, clientIp, userAgent);
		Assert.assertTrue( responses2!=null && responses2.size()==1 );
		Assert.assertNotNull(responses2.get(0).getErrors());
		
		List<ListingRequest> reqList3 = new ArrayList<ListingRequest>();
		ListingRequest req3 = _makeCreateRequest (null, "Giants", "12", "section-111", "R10", 1  );
		req3.getEvent().setEventLocalDate("");
		reqList3.add( req3 );
		bli.setCreateListingBody( reqList3 );
		
		List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses3 = listingCreateProcess.createListings(bli, clientIp, userAgent);
		Assert.assertTrue( responses3!=null && responses3.size()==1 );
		Assert.assertNotNull(responses3.get(0).getErrors());
	}
	
	@Test
	public void createListingWithEventInfoTestException() throws Exception
	{
		BulkListingInternal bli = new BulkListingInternal();
		
		EventError eventError = new EventError(ErrorType.INPUTERROR, ErrorCode.INPUT_ERROR, "INVALID_DATE_FORMAT", "");
		when(eventMapperAdaptor.mapEvent(Mockito.any(Locale.class), Mockito.any(EventInfo.class), Mockito.anyString())).thenThrow(new EventMappingException(eventError));
		Event event = super.getEvent();
		when(eventHelper.getEventById(Mockito.anyLong(),Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		
		List<ListingRequest> reqList = new ArrayList<ListingRequest>(); 
		reqList.add( _makeCreateRequest (null, "Giants", "12", "section-111", "R10", 1  ) );
		reqList.add( _makeCreateRequest (null, "Giants", "13", "section-111", "R11", 1  ) );
		bli.setCreateListingBody( reqList );
		bli.setAssertion("---somesdymmtassertionvalue---");
		bli.setSellerId(1000010549L);
		bli.setSellerGuid("E4016068190C25E7E044002128BE217A");
		
		List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses = listingCreateProcess.createListings(bli, clientIp, userAgent);
		Assert.assertTrue( responses!=null && responses.size()==2 );
		Assert.assertNotNull(responses.get(0).getErrors());
	}
	
	@Test(expectedExceptions=SHBadRequestException.class)
	public void createListingWithEventInfoTestInputError() throws Exception
	{
		BulkListingInternal bli = new BulkListingInternal();
		List<ListingRequest> reqList = new ArrayList<ListingRequest>(); 
		reqList.add( _makeCreateRequest (null, null, "12", "section-111", "R10", 1  ) );
		bli.setCreateListingBody( reqList );
		bli.setAssertion("---somesdymmtassertionvalue---");
		bli.setSellerId(1000010549L);
		bli.setSellerGuid("E4016068190C25E7E044002128BE217A");
		
		List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, true, false);
	}
	
	private ListingRequest _makeCreateRequest(String eventId, String eventName, String price, String section, String row, int numberSeats)
	{
		ListingRequest req = new ListingRequest();
		req.setEventId ( eventId );
		if ( eventName != null ) {
			EventInfo ei = new EventInfo ();
			ei.setName(eventName);
			ei.setEventLocalDate("2016-12-11T09:00");
			ei.setVenue("ATT Park");
			req.setEvent(ei);
		}
		req.setPricePerProduct(new com.stubhub.newplatform.common.entity.Money(price, "USD"));
		req.setSection(section);
		req.setStatus(ListingStatus.INCOMPLETE);
		req.setDeliveryOption(DeliveryOption.PDF);
		req.setHideSeats(Boolean.TRUE);
		
		List<Product> prods = new ArrayList<Product> ();
		for ( int i=0; i<numberSeats; i++ ) {
			prods.add( getProduct(Operation.ADD, row, String.valueOf(i+1), ProductType.TICKET, null, null) );
		}
		req.setProducts( prods );
		List<TicketTrait> ticketTraitList = new ArrayList<TicketTrait>();
		ticketTraitList.add(buildTicketTrait("102", "Parking"));
		ticketTraitList.add(buildTicketTrait("202", "Wheelchair only"));
		req.setTicketTraits(ticketTraitList);
		return req;
	}
	
	private TicketTrait buildTicketTrait(String id, String name) {
		TicketTrait tt = new TicketTrait();
		tt.setId(id);
		tt.setName(name);
		return tt;
	}
	
	private ListingRequest _makeCreateRequestSTH(String eventId, String eventName, String price, String section, String row, int numberSeats)
	{
		ListingRequest req = new ListingRequest();
		req.setEventId ( eventId );
		if ( eventName != null ) {
			EventInfo ei = new EventInfo ();
			ei.setName(eventName);
			ei.setEventLocalDate("2016-12-11T09:00");
			ei.setVenue("ATT Park");
			req.setEvent(ei);
		}
		req.setPricePerProduct(new com.stubhub.newplatform.common.entity.Money(price, "USD"));
		req.setSection(section);
		req.setStatus(ListingStatus.INCOMPLETE);
		req.setDeliveryOption(DeliveryOption.STH);
		
		List<Product> prods = new ArrayList<Product> ();
		for ( int i=0; i<numberSeats; i++ ) {
			prods.add( getProduct(Operation.ADD, row, String.valueOf(i+1), ProductType.TICKET, null, null) );
		}
		req.setProducts( prods );
		return req;
	}
	
	private ListingRequest _makeCreateRequestUK(String eventId, String eventName, String price, String section, String row, int numberSeats)
	{
		ListingRequest req = new ListingRequest();
		req.setEventId ( eventId );
		if ( eventName != null ) {
			EventInfo ei = new EventInfo ();
			ei.setName(eventName);
			ei.setCity("London");
			ei.setDate("2016-5-11");
			req.setEvent(ei);
		}
		req.setPayoutPerProduct(new com.stubhub.newplatform.common.entity.Money(price, "GBP"));
		req.setSection(section);
		req.setStatus(ListingStatus.INCOMPLETE);
		req.setDeliveryOption(DeliveryOption.PDF);
		req.setHideSeats(Boolean.TRUE);
		
		List<Product> prods = new ArrayList<Product> ();
		for ( int i=0; i<numberSeats; i++ ) {
			prods.add( getProduct(Operation.ADD, row, String.valueOf(i+1), ProductType.TICKET, null, null) );
		}
		req.setProducts( prods );
		return req;
	}
	
	private ListingRequest _makeCreateRequestDE(String eventId, String eventName, String price, String section, String row, int numberSeats)
	{
		ListingRequest req = new ListingRequest();
		req.setEventId ( eventId );
		if ( eventName != null ) {
			EventInfo ei = new EventInfo ();
			ei.setName(eventName);
			ei.setCity("Berlin");
			ei.setDate("2016-5-11");
			req.setEvent(ei);
		}
		req.setBuyerSeesPerProduct(new com.stubhub.newplatform.common.entity.Money(price, "EUR"));
		req.setSection(section);
		req.setStatus(ListingStatus.INCOMPLETE);
		req.setDeliveryOption(DeliveryOption.PDF);
		
		List<Product> prods = new ArrayList<Product> ();
		for ( int i=0; i<numberSeats; i++ ) {
			prods.add( getProduct(Operation.ADD, row, String.valueOf(i+1), ProductType.TICKET, null, null) );
		}
		req.setProducts( prods );
		return req;
	}
	
	
}

