package com.stubhub.domain.inventory.listings.v2.helper;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.client.WebClient;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.entity.ErrorEnum;
import com.stubhub.domain.inventory.common.util.ListingException;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.util.SellerHelper;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.domain.user.business.intf.UserBusinessStatus;
import com.stubhub.domain.user.contacts.intf.CustomerContactDetails;
import com.stubhub.domain.user.intf.GetCustomerResponse;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

import junit.framework.Assert;

public class SellerHelperTest {

	private SellerHelper sellerHelper;
	private UserHelper userHelper;
	private SvcLocator svcLocator;
	private WebClient webClient;
	private WebClient businessDetailWebClient;
	
	@BeforeMethod
	public void setUp(){
		sellerHelper = new SellerHelper() {
			protected String getProperty(String propertyName, String defaultValue) {
				if ("userdefaultcontact.api.url".equals(propertyName))
					return "http://api.srwd34.com/user/customers/v1/";
				else if ("userbusinessstatus.api.url".equals(propertyName))
					return "https://api.slcq035.com/user/customers/v1/{customerGuid}/statuses";
				return "";
			}
		};
		userHelper = Mockito.mock(UserHelper.class);
		svcLocator = Mockito.mock(SvcLocator.class);
		webClient = Mockito.mock(WebClient.class);
		Mockito.when(webClient.get()).thenReturn(getResponse());
		businessDetailWebClient = Mockito.mock(WebClient.class);
		Mockito.when(businessDetailWebClient.get()).thenReturn(getBusinessDetailResponse());
		ReflectionTestUtils.setField(sellerHelper, "userHelper", userHelper);
		ReflectionTestUtils.setField(sellerHelper, "svcLocator", svcLocator);
	}
	
	@Test
	public void testPopulateSellerDetailsNoResponse() {
		Listing listing = new Listing();
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);		
		Mockito.when(webClient.get()).thenReturn(getNullResponse());
		
		try {
			sellerHelper.populateSellerDetails(listing);
		} catch (ListingException e) {
			Assert.assertTrue(false);			
		}
		Assert.assertTrue(true);
	}
	
	/*@Test
    public void testPopulateSellerDetailsAuthZ() {
        Listing listing = new Listing();
        Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);      
        Mockito.when(webClient.get()).thenReturn(getResponse());
        Mockito.when(userHelper.isAuthZRequest()).thenReturn(true);
        try {
            sellerHelper.populateSellerDetails(listing);
        } catch (ListingException e) {
            Assert.assertTrue(false);           
        }
        Assert.assertTrue(true);
    }*/
	
	@Test
	public void testPopulateSellerDetailsInvalidSeller() {
		Listing listing = new Listing();
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);		
		try {
			sellerHelper.populateSellerDetails(listing);
		} catch (ListingException e) {
			Assert.assertTrue(false);			
		}
		Assert.assertTrue(true);
	}
	
	@Test
	public void testPopulateSellerDetailsInvalidSeller_NotFoundResponse() {
		Listing listing = new Listing();
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(Response.status(Status.NOT_FOUND).build());
		try {
			sellerHelper.populateSellerDetails(listing);
		} catch (ListingException e) {
			Assert.assertTrue(true);
			Assert.assertEquals(ErrorEnum.INVALID_SELLER_GUID.getCode(), e.getListingError().getCode());
		}
	}
	
	@Test
	public void testPopulateSellerDetailsInvalidSeller_InternalServerErrorResponse() {
		Listing listing = new Listing();
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(Response.status(Status.INTERNAL_SERVER_ERROR).build());
		try {
			sellerHelper.populateSellerDetails(listing);
		} catch (ListingException e) {
			Assert.assertTrue(true);
			Assert.assertEquals(ErrorEnum.SYSTEM_ERROR.getCode(), e.getListingError().getCode());
		}
	}
	
	@Test
	public void testAddBusinessDetails() throws Exception {
		Listing listing = new Listing();
		listing.setSellerGuid("XYZ");
		
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(businessDetailWebClient);
		
		sellerHelper.addBusinessDetails(listing);
		
		Assert.assertEquals(123L, listing.getBusinessId().longValue());
		Assert.assertEquals("ABC", listing.getBusinessGuid());
		
	}
	
	@Test
	public void testAddBusinessDetails_NullSellerGuid() throws Exception {
		Listing listing = new Listing();
		
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(businessDetailWebClient);
		
		sellerHelper.addBusinessDetails(listing);
		
		Assert.assertNull(listing.getBusinessId());
		Assert.assertNull(listing.getBusinessGuid());
	}
	
	@Test
	public void testAddBusinessDetails_NotFoundResopnse() throws Exception {
		Listing listing = new Listing();
		listing.setSellerGuid("XYZ");
		
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(businessDetailWebClient);
		Mockito.when(businessDetailWebClient.get()).thenReturn(Response.status(Status.NOT_FOUND).build());
		
		sellerHelper.addBusinessDetails(listing);
		
		Assert.assertNull(listing.getBusinessId());
		Assert.assertNull(listing.getBusinessGuid());
	}
	
	@Test
	public void testAddBusinessDetails_BadRequestResopnse() throws Exception {
		Listing listing = new Listing();
		listing.setSellerGuid("XYZ");
		
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(businessDetailWebClient);
		Mockito.when(businessDetailWebClient.get()).thenReturn(Response.status(Status.BAD_REQUEST).build());
		
		sellerHelper.addBusinessDetails(listing);
		
		Assert.assertNull(listing.getBusinessId());
		Assert.assertNull(listing.getBusinessGuid());
	}
	
	@Test
	public void testAddBusinessDetails_InternalServerErrorResopnse() throws Exception {
		Listing listing = new Listing();
		listing.setSellerGuid("XYZ");
		
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(businessDetailWebClient);
		Mockito.when(businessDetailWebClient.get()).thenReturn(Response.status(Status.INTERNAL_SERVER_ERROR).build());
		
		sellerHelper.addBusinessDetails(listing);
		
		Assert.assertNull(listing.getBusinessId());
		Assert.assertNull(listing.getBusinessGuid());
	}
	
	@Test
	public void testAddBusinessDetails_GeneralErrorResopnse() throws Exception {
		Listing listing = new Listing();
		listing.setSellerGuid("XYZ");
		
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(businessDetailWebClient);
		Mockito.when(businessDetailWebClient.get()).thenReturn(Response.status(Status.FORBIDDEN).build());
		
		sellerHelper.addBusinessDetails(listing);
		
		Assert.assertNull(listing.getBusinessId());
		Assert.assertNull(listing.getBusinessGuid());
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
	
	private Response getNullResponse() {
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
				return null;
			}
		};
		return response;
	}
	
	private Response getBusinessDetailResponse() {
		Response response = new Response() {
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
				UserBusinessStatus resp = new UserBusinessStatus();
				resp.setBusinessId(123L);
				resp.setBusinessGuid("ABC");
				return resp;
			}
		};
		return response;
	}
	
	
}
