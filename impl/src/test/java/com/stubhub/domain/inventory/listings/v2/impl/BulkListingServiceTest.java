/**
 * 
 */
package com.stubhub.domain.inventory.listings.v2.impl;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHBadRequestException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHForbiddenException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHResourceNotFoundException;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.common.entity.ErrorEnum;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.enums.BulkStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TaxpayerStatusEnum;
import com.stubhub.domain.inventory.listings.v2.bulk.util.BulkListingHelper;
import com.stubhub.domain.inventory.listings.v2.helper.ListingCreateProcess;
import com.stubhub.domain.inventory.listings.v2.helper.PaymentEligibilityHelper;
import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.domain.inventory.listings.v2.util.SellerHelper;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkJobResponse;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkJobStatusRequest;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingRequest;
import com.stubhub.domain.inventory.v2.listings.service.BulkListingService;
import com.stubhub.domain.user.contacts.intf.CustomerContactDetails;
import com.stubhub.domain.user.intf.GetCustomerResponse;
import com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

import junit.framework.Assert;

/**
 * @author sjayaswal
 *
 */
public class BulkListingServiceTest extends SHInventoryTest {

	private SHServiceContext securityContext;
	
	@Mock
	private ListingCreateProcess listingCreateProcess;

	@Mock
	private BulkListingHelper bulkListingHelper;

	@Mock
	private PaymentEligibilityHelper piEligibilityHelper;

	@InjectMocks
	private BulkListingService bulkListingService;
	private SellerHelper sellerHelper;
	private SvcLocator svcLocator;
	private WebClient webClient;

	
	@BeforeMethod
	public void setUp() throws Exception 
	{
		bulkListingService = new BulkListingServiceImpl();

		MockitoAnnotations.initMocks(this);
		
		securityContext = mockServiceContext("1001");
	
		sellerHelper= new SellerHelper(){
			protected String getProperty(String propertyName, String defaultValue) {
				if ("userdefaultcontact.api.url".equals(propertyName))
					return "http://api.srwd34.com/user/customers/v1/";
				return "";
			}
		};
		
		svcLocator = Mockito.mock(SvcLocator.class);
		sellerHelper.setSvcLocator(svcLocator);
		webClient = Mockito.mock(WebClient.class);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);		
		Mockito.when(webClient.get()).thenReturn(getResponse());
		ListingCreateProcess listingCreateProcess = new ListingCreateProcess();
		super.setBeanProperty(bulkListingService, "listingCreateProcess", listingCreateProcess);
		ReflectionTestUtils.setField(bulkListingService, "sellerHelper", sellerHelper);

	}

	@Test
	public void testBulkListing() {
		Map<String, Object> extendedInfo = securityContext.getExtendedSecurityContext().getExtendedInfo();
		extendedInfo.put("http://stubhub.com/claims/operatorapp", "SHIP");
		BulkListingRequest createBulkListingRequest = new BulkListingRequest();

		List<ListingRequest> listings = new ArrayList<ListingRequest>();
		createBulkListingRequest.setListings(listings);

		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("someassertaion");
		SHAPIThreadLocal.set(ctx);

		try {
			Mockito.when(piEligibilityHelper.isValidPaymentEligibility(Mockito.anyString(), Matchers.any(HttpHeaders.class))).thenReturn(true);
			Mockito.when(bulkListingHelper.bulkCreateListing(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
					Matchers.any(BulkStatus.class), Matchers.any(BulkListingRequest.class), Mockito.anyString(), Matchers.any(HttpHeaders.class))).thenReturn(12345L);
			bulkListingService.createBulkListing(createBulkListingRequest, securityContext);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private Response getResponse() {
		Response response =  new Response() {
		
			@Override
			public int getStatus() {
				return 200;
			}
			
			@Override
			public MultivaluedMap<String, Object> getMetadata() {				
				return null;
			}
			
			@Override
			public Object getEntity() {
				GetCustomerResponse resp = new GetCustomerResponse();
				CustomerContactDetails customerContact = new CustomerContactDetails();
				customerContact.setId(123L);
				resp.setCustomerContact(customerContact);
				return resp;
			}
		};
		return response;
	}
	
	
	private Response getErrorResponse() {
		Response response =  new Response() {
		
			@Override
			public int getStatus() {
				return 200;
			}
			
			@Override
			public MultivaluedMap<String, Object> getMetadata() {				
				return null;
			}
			
			@Override
			public Object getEntity() {
				GetCustomerResponse resp = new GetCustomerResponse();
				CustomerContactDetails customerContact = new CustomerContactDetails();
				customerContact.setId(null);
				resp.setCustomerContact(customerContact);
				return resp;
			}
		};
		return response;
	}
	
	@Test
	public void testBulkListing_ErrorResponse(){
		BulkListingRequest createBulkListingRequest = new BulkListingRequest();
		List<ListingRequest> listings = new ArrayList<ListingRequest>();
		createBulkListingRequest.setListings(listings);
		
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("someassertaion");
		SHAPIThreadLocal.set(ctx);
		
		try {
			Mockito.when(webClient.get()).thenReturn(getErrorResponse());
			bulkListingService.createBulkListing(createBulkListingRequest, securityContext);
		}
		catch ( Exception ex ) 
		{ex.printStackTrace();}
	}

	@Test
	public void testBulkListing_ErrorResponseInvalidPI() {
		BulkListingRequest createBulkListingRequest = new BulkListingRequest();
		List<ListingRequest> listings = new ArrayList<ListingRequest>();
		createBulkListingRequest.setListings(listings);

		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("someassertaion");
		SHAPIThreadLocal.set(ctx);

		try {
			Mockito.when(piEligibilityHelper.isValidPaymentEligibility(Mockito.anyString(), Matchers.any(HttpHeaders.class))).thenReturn(false);
			bulkListingService.createBulkListing(createBulkListingRequest, securityContext);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private Response getErrorResponseNotFound() {
		Response response =  new Response() {
		
			@Override
			public int getStatus() {
				return 404;
			}
			
			@Override
			public MultivaluedMap<String, Object> getMetadata() {				
				return null;
			}
			
			@Override
			public Object getEntity() {
				GetCustomerResponse resp = new GetCustomerResponse();
				CustomerContactDetails customerContact = new CustomerContactDetails();
				customerContact.setId(null);
				resp.setCustomerContact(customerContact);
				return resp;
			}
		};
		return response;
	}
	
	@Test
	public void testBulkListing_ErrorResponseNotFound(){
		BulkListingRequest createBulkListingRequest = new BulkListingRequest();
		List<ListingRequest> listings = new ArrayList<ListingRequest>();
		createBulkListingRequest.setListings(listings);
		
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("someassertaion");
		SHAPIThreadLocal.set(ctx);
		
		try {
			Mockito.when(webClient.get()).thenReturn(getErrorResponseNotFound());
			bulkListingService.createBulkListing(createBulkListingRequest, securityContext);
		}
		catch ( Exception ex ) 
		{ex.printStackTrace();}
	}
	
	private Response getErrorResponseInvalidTaxpayer() {
		Response response =  new Response() {
		
			@Override
			public int getStatus() {
				return 200;
			}
			
			@Override
			public MultivaluedMap<String, Object> getMetadata() {				
				return null;
			}
			
			@Override
			public Object getEntity() {
				GetCustomerResponse resp = new GetCustomerResponse();
				CustomerContactDetails customerContact = new CustomerContactDetails();
				customerContact.setId(123L);
				customerContact.setTaxpayerStatus(TaxpayerStatusEnum.TINInvalid.getStatus());
				resp.setCustomerContact(customerContact);
				return resp;
			}
		};
		return response;
	}
	
	@Test
	public void testBulkListing_ErrorResponseTinInvalid(){
		BulkListingRequest createBulkListingRequest = new BulkListingRequest();
		List<ListingRequest> listings = new ArrayList<ListingRequest>();
		createBulkListingRequest.setListings(listings);
		
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("someassertaion");
		SHAPIThreadLocal.set(ctx);
		
		try {
			Mockito.when(webClient.get()).thenReturn(getErrorResponseInvalidTaxpayer());
			bulkListingService.createBulkListing(createBulkListingRequest, securityContext);
		}
		catch ( Exception ex ) 
		{ex.printStackTrace();}
	}
	
	
	private Response getErrorResponseInternalServerError() {
		Response response =  new Response() {
		
			@Override
			public int getStatus() {
				return 500;
			}
			
			@Override
			public MultivaluedMap<String, Object> getMetadata() {				
				return null;
			}
			
			@Override
			public Object getEntity() {
				GetCustomerResponse resp = new GetCustomerResponse();
				CustomerContactDetails customerContact = new CustomerContactDetails();
				customerContact.setId(123L);
				customerContact.setTaxpayerStatus(TaxpayerStatusEnum.TINInvalid.getStatus());
				resp.setCustomerContact(customerContact);
				return resp;
			}
		};
		return response;
	}
	
	@Test
	public void testBulkListing_ErrorResponseServerError(){
		BulkListingRequest createBulkListingRequest = new BulkListingRequest();
		List<ListingRequest> listings = new ArrayList<ListingRequest>();
		createBulkListingRequest.setListings(listings);
		
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("someassertaion");
		SHAPIThreadLocal.set(ctx);
		
		try {
			Mockito.when(webClient.get()).thenReturn(getErrorResponseInternalServerError());
			bulkListingService.createBulkListing(createBulkListingRequest, securityContext);
		}
		catch ( Exception ex ) 
		{ex.printStackTrace();}
	}

	@Test
	public void testJobStatus(){
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("someassertaion");
		SHAPIThreadLocal.set(ctx);

		//setup mocks
		BulkJobResponse mockResponse = new BulkJobResponse();
		when(bulkListingHelper.getJobStatuses(anyLong(), anyLong())).thenReturn(mockResponse);
		
		// call the servcie
		BulkJobResponse response = bulkListingService.getJobStatus("12345", securityContext);
		
		//verifications
		verify(bulkListingHelper, times(1)).getJobStatuses(anyLong(), eq(12345L));
		
	}
	
	@Test(expectedExceptions={SHForbiddenException.class})
	public void testJobStatusNullSecurityContext(){
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("someassertaion");
		SHAPIThreadLocal.set(ctx);

		SHServiceContext tempServiceContext = securityContext;
		tempServiceContext.setExtendedSecurityContext(null);
		//setup mocks
		BulkJobResponse mockResponse = new BulkJobResponse();
		when(bulkListingHelper.getJobStatuses(anyLong(), anyLong())).thenReturn(mockResponse);
		
		// call the servcie
		BulkJobResponse response = bulkListingService.getJobStatus("12345", tempServiceContext);
		
		//verifications, the helper call should not happen as the exception has been thrown prior
		verify(bulkListingHelper, never()).getJobStatuses(anyLong(), anyLong());
		
	}
	
	@Test(expectedExceptions={SHForbiddenException.class})
	public void testJobStatusNullSellerId(){
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("someassertaion");
		SHAPIThreadLocal.set(ctx);
		SHServiceContext tempServiceContext = securityContext;

		//setup mocks
		//need to overwrite the securityContext setup in @BeforeTest as that one will return non null User ID
		ExtendedSecurityContext localSecurityContext = Mockito.mock(ExtendedSecurityContext.class);
		when(localSecurityContext.getUserGuid()).thenReturn(null);

		tempServiceContext.setExtendedSecurityContext(localSecurityContext);
		BulkJobResponse mockResponse = new BulkJobResponse();
		when(bulkListingHelper.getJobStatuses(anyLong(), anyLong())).thenReturn(mockResponse);
		
		// call the servcie
		BulkJobResponse response = bulkListingService.getJobStatus("12345", tempServiceContext);
		
		//verifications, the helper call should not happen as the exception has been thrown prior
		verify(bulkListingHelper, never()).getJobStatuses(anyLong(), eq(12345L));
		
	}
	
	@Test(expectedExceptions={SHBadRequestException.class})
	public void testJobStatusInvalidJobGuid(){
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("someassertaion");
		SHAPIThreadLocal.set(ctx);

		//setup mocks
		//need to overwrite the securityContext setup in @BeforeTest as that one will return non null User ID
		BulkJobResponse mockResponse = new BulkJobResponse();
		when(bulkListingHelper.getJobStatuses(anyLong(), anyLong())).thenReturn(mockResponse);
		
		// call the servcie
		BulkJobResponse response = bulkListingService.getJobStatus("abc", securityContext);
		
		//verifications, the helper call should not happen as the exception has been thrown prior
		verify(bulkListingHelper, never()).getJobStatuses(anyLong(), eq(12345L));
		
	}
	
	@Test(expectedExceptions={SHResourceNotFoundException.class})
	public void testJobStatusListingBusinessException(){
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("someassertaion");
		SHAPIThreadLocal.set(ctx);

		//setup mocks
		ListingError listingError = new ListingError(ErrorType.NOT_FOUND, ErrorCode.BULK_JOB_NOT_FOUND, ErrorEnum.BULK_JOB_NOT_FOUND.getMessage(), "123");
		ListingBusinessException listingException = new ListingBusinessException(listingError);
		when(bulkListingHelper.getJobStatuses(anyLong(), anyLong())).thenThrow(listingException);
		
		// call the servcie
		BulkJobResponse response = bulkListingService.getJobStatus("12345", securityContext);
		
		//verifications
		verify(bulkListingHelper, times(1)).getJobStatuses(anyLong(), eq(12345L));
		
	}
	
	@Test
	public void testBulkUpdateListing(){
		BulkListingRequest createBulkListingRequest = new BulkListingRequest();

		List<ListingRequest> listings = new ArrayList<ListingRequest>();
		createBulkListingRequest.setListings(listings);
		
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("someassertaion");
		SHAPIThreadLocal.set(ctx);
	
		try {
			Mockito.when(bulkListingHelper.bulkCreateListing(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
					Matchers.any(BulkStatus.class), Matchers.any(BulkListingRequest.class),Mockito.anyString(), Matchers.any(HttpHeaders.class))).thenReturn(12345L);
			bulkListingService.updateBulkListing(createBulkListingRequest, securityContext);
		}
		catch ( Exception ex ) 
		{ex.printStackTrace();}
	}
	
	@Test
	public void testBulkListingWithOperApp() {
		ExtendedSecurityContext extendedSecurityContext=mockSecurityContextWithAppName("1000");
		securityContext.setExtendedSecurityContext(extendedSecurityContext);
		BulkListingRequest createBulkListingRequest = new BulkListingRequest();

		List<ListingRequest> listings = new ArrayList<ListingRequest>();
		createBulkListingRequest.setListings(listings);

		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("someassertaion");
		SHAPIThreadLocal.set(ctx);

		try {
			Mockito.when(piEligibilityHelper.isValidPaymentEligibility(Mockito.anyString(), Matchers.any(HttpHeaders.class))).thenReturn(true);
			Mockito.when(bulkListingHelper.bulkCreateListing(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
					Matchers.any(BulkStatus.class), Matchers.any(BulkListingRequest.class), Mockito.anyString(), Matchers.any(HttpHeaders.class))).thenReturn(12345L);
			bulkListingService.createBulkListing(createBulkListingRequest, securityContext);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Test
	public void testBulkListingWithNoOperApp() {
		Map<String, Object> extendedInfo = securityContext.getExtendedSecurityContext().getExtendedInfo();
		extendedInfo.put("http://stubhub.com/claims/operatorapp", "SHIP");
		BulkListingRequest createBulkListingRequest = new BulkListingRequest();

		List<ListingRequest> listings = new ArrayList<ListingRequest>();
		createBulkListingRequest.setListings(listings);

		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("someassertaion");
		SHAPIThreadLocal.set(ctx);

		try {
			Mockito.when(piEligibilityHelper.isValidPaymentEligibility(Mockito.anyString(), Matchers.any(HttpHeaders.class))).thenReturn(true);
			Mockito.when(bulkListingHelper.bulkCreateListing(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
					Matchers.any(BulkStatus.class), Matchers.any(BulkListingRequest.class), Mockito.anyString(), Matchers.any(HttpHeaders.class))).thenReturn(12345L);
			bulkListingService.createBulkListing(createBulkListingRequest, securityContext);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Test
	public void testUpdateJobStatus() {
      BulkJobResponse mockResponse = new BulkJobResponse();
      mockResponse.setJobGuid(12345L);
      mockResponse.setStatus("ERROR");
      when(bulkListingHelper.updateJobStatus(anyLong(), anyLong(), Matchers.any(BulkJobStatusRequest.class))).thenReturn(mockResponse);
      
      BulkJobStatusRequest jobStatusRequest = new BulkJobStatusRequest();
      jobStatusRequest.setJobStatus("ERROR");
      BulkJobResponse response = bulkListingService.updateJobStatus("12345", jobStatusRequest, securityContext);
      Assert.assertEquals("ERROR", response.getStatus()); 
	}
	
	@Test(expectedExceptions = {SHForbiddenException.class})
    public void testUpdateJobStatusNullSecurityContext() {
      SHServiceContext tempServiceContext = securityContext;
      tempServiceContext.setExtendedSecurityContext(null);
  
      BulkJobStatusRequest jobStatusRequest = new BulkJobStatusRequest();
      jobStatusRequest.setJobStatus("ERROR");
      bulkListingService.updateJobStatus("12345", jobStatusRequest, tempServiceContext);
    }
	
	@Test(expectedExceptions={SHForbiddenException.class})
    public void testUpdateJobStatusNullSellerId() {
      SHServiceContext tempServiceContext = securityContext;
      ExtendedSecurityContext localSecurityContext = Mockito.mock(ExtendedSecurityContext.class);
      tempServiceContext.setExtendedSecurityContext(localSecurityContext);
      
      BulkJobStatusRequest jobStatusRequest = new BulkJobStatusRequest();
      jobStatusRequest.setJobStatus("ERROR");
      bulkListingService.updateJobStatus("12345", jobStatusRequest, securityContext);
    }
	
	@Test(expectedExceptions={SHBadRequestException.class})
    public void testUpdateJobStatusInvalidJobId() {
      BulkJobStatusRequest jobStatusRequest = new BulkJobStatusRequest();
      jobStatusRequest.setJobStatus("ERROR");
      bulkListingService.updateJobStatus("abc", jobStatusRequest, securityContext);
    }
	
	@Test(expectedExceptions={SHBadRequestException.class})
    public void testUpdateJobStatusListingBusinessException() {
	  ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.BULK_JOB_NOT_FOUND, ErrorEnum.BULK_JOB_NOT_FOUND.getMessage(), "12345");
      ListingBusinessException listingException = new ListingBusinessException(listingError);
      when(bulkListingHelper.updateJobStatus(anyLong(), anyLong(), Matchers.any(BulkJobStatusRequest.class))).thenThrow(listingException);
      
      BulkJobStatusRequest jobStatusRequest = new BulkJobStatusRequest();
      jobStatusRequest.setJobStatus("ERROR");
      bulkListingService.updateJobStatus("12345", jobStatusRequest, securityContext);
    }
	
	@Test(expectedExceptions={SHSystemException.class})
    public void testUpdateJobStatusException() {
	  when(bulkListingHelper.updateJobStatus(anyLong(), anyLong(), Matchers.any(BulkJobStatusRequest.class))).thenThrow(new RuntimeException());
	  BulkJobStatusRequest jobStatusRequest = new BulkJobStatusRequest();
      jobStatusRequest.setJobStatus("ERROR");
	  bulkListingService.updateJobStatus("12345", jobStatusRequest, securityContext);
    }

}
