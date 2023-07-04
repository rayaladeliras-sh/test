package com.stubhub.domain.inventory.listings.v2.helper;

import java.io.ByteArrayInputStream;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.infrastructure.caching.client.core.L2Cache;
import com.stubhub.domain.infrastructure.caching.client.core.L2CacheManager;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

import junit.framework.Assert;

public class SellerEligibilityHelperTest {
	
	private SellerEligibilityHelper sellerEligibilityHelper;
	private SvcLocator svcLocator;
	private WebClient webClient;
	private L2CacheManager cacheManager;
	private L2Cache<Object> mockCache;
	
	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp(){
		sellerEligibilityHelper = new SellerEligibilityHelper() {
			protected String getProperty(String propertyName, String defaultValue) {
				if ("seller.eligibility.api.url".equals(propertyName))
					return "http://apx.srwd34.com/inventorynew/eligibility/v1/sellereligibility";
				return "";
			}
		};
		svcLocator = Mockito.mock(SvcLocator.class);
		webClient = Mockito.mock(WebClient.class);
		cacheManager = Mockito.mock(L2CacheManager.class);
		mockCache = Mockito.mock(L2Cache.class);
		ReflectionTestUtils.setField(sellerEligibilityHelper, "svcLocator", svcLocator);
		ReflectionTestUtils.setField(sellerEligibilityHelper, "cacheManager", cacheManager);

		
	}
	
	@Test
	public void testCheckSellerEligibility_NoCache() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getResponse());
		Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(mockCache);
		Mockito.when(mockCache.get(Mockito.anyString())).thenReturn(null);
	    boolean isEligible = sellerEligibilityHelper.checkSellerEligibility("ABCDEF", "Sell", 12345L);
		Assert.assertTrue(isEligible);
	}
	
	@Test
	public void testCheckSellerEligibility_Cache() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getResponse());
		Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(mockCache);
		Mockito.when(mockCache.get(Mockito.anyString())).thenReturn(true);
	    boolean isEligible = sellerEligibilityHelper.checkSellerEligibility("ABCDEF", "Sell", 12345L);
		Assert.assertTrue(isEligible);
	}
	
	@Test
	public void testCheckSellerEligibilityWhoIsNotAllowedToSell() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getResponseWhoIsNotAllowedToSell());
		Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(mockCache);
		Mockito.when(mockCache.get(Mockito.anyString())).thenReturn(null);
		boolean isEligible = sellerEligibilityHelper.checkSellerEligibility("ABCDEF", "Sell", 12345L);
		Assert.assertFalse(isEligible);
	}
	
	@Test
	public void testCheckSellerEligibilityWhoIsNotAllowedToSell_2ndCase() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getResponseWhoIsNotAllowedToSell_2ndcase());
		Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(mockCache);
		Mockito.when(mockCache.get(Mockito.anyString())).thenReturn(null);
		boolean isEligible = sellerEligibilityHelper.checkSellerEligibility("ABCDEF", "Sell", 12345L);
		Assert.assertTrue(isEligible);
	}
	
	@Test
	public void testCheckSellerEligibilityWhoIsNotAllowedToSell_3rdCase() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getResponseWhoIsNotAllowedToSell_3rdcase());
		Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(mockCache);
		Mockito.when(mockCache.get(Mockito.anyString())).thenReturn(null);
		boolean isEligible = sellerEligibilityHelper.checkSellerEligibility("ABCDEF", "Sell", 12345L);
		Assert.assertFalse(isEligible);
	}
	
	@Test
	public void testCheckSellerEligibility_4thCase() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getResponse_4thCase());
		Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(mockCache);
		Mockito.when(mockCache.get(Mockito.anyString())).thenReturn(null);
		boolean isEligible = sellerEligibilityHelper.checkSellerEligibility("ABCDEF", "Sell", 12345L);
		Assert.assertFalse(isEligible);
	}
	
	@Test
	public void testCheckSellerEligibilityError() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getResponseError());
		Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(mockCache);
		Mockito.when(mockCache.get(Mockito.anyString())).thenReturn(null);
		try {
			sellerEligibilityHelper.checkSellerEligibility("ABCDEF", "Sell", 12345L);
		} catch (ListingBusinessException e) {
			Assert.assertEquals(ErrorCode.SYSTEM_ERROR, e.getListingError().getCode());
		}
	}
	
	@Test
	public void testCheckSellerEligibilityError_2ndCase() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getResponseError_2ndCase());
		Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(mockCache);
		Mockito.when(mockCache.get(Mockito.anyString())).thenReturn(null);
		try {
			sellerEligibilityHelper.checkSellerEligibility("ABCDEF", "Sell", 12345L);
		} catch (ListingBusinessException e) {
			Assert.assertEquals(ErrorCode.SYSTEM_ERROR, e.getListingError().getCode());
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
				String responseString = "{\"allowedToSell\": \"YES\", \"reasons\": []}";
				return new ByteArrayInputStream(responseString.getBytes());
			}
		};
		return response;
	}
	
	
	private Response getResponseWhoIsNotAllowedToSell(){
		Response response = new Response(){

			@Override
			public Object getEntity() {
				String responseString = "{\r\n  \"allowedToSell\": \"NO\",\r\n  \"reasons\": [\r\n    {\r\n      \"reasonCode\": \"something\",\r\n      \"description\": \"something\"\r\n    }\r\n  ]\r\n}";
				return new ByteArrayInputStream(responseString.getBytes());
			}

			@Override
			public int getStatus() {
				return 200;
			}

			@Override
			public MultivaluedMap<String, Object> getMetadata() {
				return null;
			}
			
		};
		return response;
		
	}
	
	private Response getResponseWhoIsNotAllowedToSell_2ndcase(){
		Response response = new Response(){

			@Override
			public Object getEntity() {
				String responseString = "{\r\n  \"allowedToSell\": \"NO\",\r\n  \"reasons\": [\r\n    {\r\n      \"reasonCode\": \"SellerInfoBlock\",\r\n      \"description\": \"SellerInfoForCurrCountryBlock\"\r\n    }\r\n  ]\r\n}";
				return new ByteArrayInputStream(responseString.getBytes());
			}

			@Override
			public int getStatus() {
				return 200;
			}

			@Override
			public MultivaluedMap<String, Object> getMetadata() {
				return null;
			}
			
		};
		return response;
		
	}
	
	private Response getResponseWhoIsNotAllowedToSell_3rdcase(){
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
				String responseString = "{\"allowedToSell\": \"NO\", \"reasons\": []}";
				return new ByteArrayInputStream(responseString.getBytes());
			}
		};
		return response;
		
	}
	
	private Response getResponse_4thCase(){
		Response response = new Response(){

			@Override
			public Object getEntity() {
				String responseString = "{\r\n}";
				return new ByteArrayInputStream(responseString.getBytes());
			}

			@Override
			public int getStatus() {
				// TODO Auto-generated method stub
				return 200;
			}

			@Override
			public MultivaluedMap<String, Object> getMetadata() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		return response;
	}
	
	private Response getResponseError() {
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
				return null;
			}
		};
		return response;
	}
	
	private Response getResponseError_2ndCase() {
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
				String responseString = "{\"allowedToSell\": \"YES\", \"reasons\": []}";
				return new ByteArrayInputStream(responseString.getBytes());
			}
		};
		return response;
	}
	
	
}
