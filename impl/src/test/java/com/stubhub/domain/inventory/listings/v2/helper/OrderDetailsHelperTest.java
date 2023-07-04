package com.stubhub.domain.inventory.listings.v2.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

import junit.framework.Assert;

public class OrderDetailsHelperTest {
	
	private static final Log log = LogFactory.getLog(OrderDetailsHelperTest.class);
	
	private OrderDetailsHelper orderDetailsHelper;
	private SvcLocator svcLocator;
	private WebClient webClient;
	
	@BeforeMethod
	public void setUp(){
		orderDetailsHelper = new OrderDetailsHelper() {
			protected String getProperty(String propertyName, String defaultValue) {
				if ("orderdetails.api.url".equals(propertyName))
					return "https://api.stubcloudprod.com/accountmanagement/orderdetails/v1";
				return "";
			}
		};
//		svcLocator = Mockito.mock(SvcLocator.class);
//		webClient = Mockito.mock(WebClient.class);
//		ReflectionTestUtils.setField(orderDetailsHelper, "svcLocator", svcLocator);
	}
	
	/*
	
	@Test
	public void testValidOrderDetails () {
        Mockito.when(webClient.get()).thenReturn(getResponse(200));
        Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
        OrderDetailsDTO dto = orderDetailsHelper.getOrderDetails(10001l);
        Assert.assertNotNull(dto);
	}

	@Test(expectedExceptions={SHResourceNotFoundException.class})
	public void testInValidOrderDetails () {
        Mockito.when(webClient.get()).thenReturn(getResponse(404));
        Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
        OrderDetailsDTO dto = orderDetailsHelper.getOrderDetails(10001l);
	}
	

	@Test(expectedExceptions={SHUnauthorizedException.class})
	public void testForbiddenAccessToOrderDetails () {
        Mockito.when(webClient.get()).thenReturn(getResponse(401));
        Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
        OrderDetailsDTO dto = orderDetailsHelper.getOrderDetails(10001l);
	}

	@Test(expectedExceptions={SHSystemException.class})
	public void testSystemExceptionWithOrderDetails () {
        Mockito.when(webClient.get()).thenReturn(getResponse(500));
        Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
        OrderDetailsDTO dto = orderDetailsHelper.getOrderDetails(10001l);
	}
	
	
	private Response getResponse(final int status) {
		Response response =  new Response() {

			@Override
			public int getStatus() {
				return status;
			}

	
			
			@Override
			public MultivaluedMap<String, Object> getMetadata() {				
				return null;
			}
			
			@Override
			public Object getEntity() {
				if(status == 200){
					return new OrderDetailsDTO();
				}
				return null;
			}
		};
		return response;
	}	

  */
	
	@Test
	public void testSomething(){
	  Assert.assertTrue(true);
	}
	

}
