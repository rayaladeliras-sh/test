package com.stubhub.domain.inventory.listings.v2.helper;

import java.io.ByteArrayInputStream;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

public class GlobalRegistryServiceHelperTest {
	private GlobalRegistryServiceHelper globalRegHelper;
	private SvcLocator svcLocator;
	private WebClient webClient;
	private ObjectMapper objectMapper;
	private static final String GLOBALREGISTRY_API_URL = "global.registry.v2.api.url";
	private static final String GLOBALREGISTRY_API_URL_DEFAULT = "http://api-int.stubprod.com/i18n/globalregistry/v2/countries/{countrycode}";

	
	@BeforeMethod
	public void setUp(){
		globalRegHelper = new GlobalRegistryServiceHelper() {
			protected String getProperty(String propertyName, String defaultValue) {
				if (GLOBALREGISTRY_API_URL.equals(propertyName)) {
					return GLOBALREGISTRY_API_URL_DEFAULT;
				}
				
				return "";
			}			
		};
		svcLocator = Mockito.mock(SvcLocator.class);
		webClient  = Mockito.mock(WebClient.class);
		objectMapper  = new ObjectMapper();
		ReflectionTestUtils.setField(globalRegHelper, "svcLocator", svcLocator);
		ReflectionTestUtils.setField(globalRegHelper, "objectMapper", objectMapper);
		MockitoAnnotations.initMocks(this);
	}
	
	
	@Test
	public void testGolobalReg200(){
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getGlobalRegApiSResponse200());
		Boolean sectionZoneToggle = globalRegHelper.getSectionZoneToggleByCountry("US");
		Assert.assertTrue(sectionZoneToggle);
	}
	
	@Test
	public void testGolobalRegNullReponse(){
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getGlobalRegApiSResponseNull());
		
		Boolean sectionZoneToggle = globalRegHelper.getSectionZoneToggleByCountry("US");
		Assert.assertFalse(sectionZoneToggle);
	}
	
	
	
	@Test
	public void testGolobalReg404(){
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getGlobalRegApiSResponse404());
		Boolean sectionZoneToggle = globalRegHelper.getSectionZoneToggleByCountry("US");
		Assert.assertFalse(sectionZoneToggle);
	}
	
	
	
	
	private Response getGlobalRegApiSResponse200() {
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
				String response = "{\r\n    \"countryCode\": \"US\",\r\n    \"defaultLocale\": \"en-US\",\r\n    \"defaultCurrency\": \"USD\",\r\n    \"supportedLocales\": [\r\n        \"en-US\"\r\n    ],\r\n    \"supportedCurrencies\": [\r\n        \"USD\"\r\n    ],\r\n    \"features\": [\r\n        {\r\n            \"name\": \"ulf.paymentMethod.directDeposit.intlDescrption\",\r\n            \"value\": false,\r\n            \"description\": \"show international description for ACH\"\r\n        },\r\n        {\r\n            \"name\": \"ulf.paymentMethod.directDeposit.display\",\r\n            \"value\": true,\r\n            \"description\": \"Show direct deposit payment method even if he doesn\\ufffdt have account\"\r\n        },\r\n        {\r\n            \"name\": \"event.faceValue.show\",\r\n            \"value\": false,\r\n            \"description\": \"Feature to show face value based on event location\"\r\n        },\r\n        {\r\n            \"name\": \"myaccount.resell.payment.showGlobalPaypalHeader\",\r\n            \"value\": false,\r\n            \"description\": \"myaccount resell payment page show global paypal header\"\r\n        },\r\n        {\r\n            \"name\": \"myaccount.orderPage.enableChangeDeliveryAddress\",\r\n            \"value\": true,\r\n            \"description\": \"Flag to enable change delivery address\"\r\n        },\r\n        {\r\n            \"name\": \"myaccount.listing.price.isIncludeVATRequired\",\r\n            \"value\": false,\r\n            \"description\": \"myaccount listing price isIncludeVATRequired\"\r\n        },\r\n        {\r\n            \"name\": \"myaccount.enablePiBsfInOrderResell\",\r\n            \"value\": false,\r\n            \"description\": \"myaccount enablePiBsfInOrderResell\"\r\n        },\r\n        {\r\n            \"name\": \"sellhub.requestEventFormLink.display\",\r\n            \"value\": true,\r\n            \"description\": \"Show the functionality to request list event by link to, if event is not found\"\r\n        },\r\n        {\r\n            \"name\": \"ulf.paymentMethod.check.hide\",\r\n            \"value\": true,\r\n            \"description\": \"Hide check payment method, unless it is default payment method\"\r\n        },\r\n        {\r\n            \"name\": \"sellhub.disable.multiselection.radioButton.display\",\r\n            \"value\": false,\r\n            \"description\": \"Disable multiple selection of events\"\r\n        },\r\n        {\r\n            \"name\": \"i18n.faceValue.display\",\r\n            \"value\": false,\r\n            \"description\": \"Face value Display\"\r\n        },\r\n        {\r\n            \"name\": \"ulf.srs.tooltip.display\",\r\n            \"value\": false,\r\n            \"description\": \"Show seat section row tooltip on mouse over\"\r\n        },\r\n        {\r\n            \"name\": \"sellhub.listParkingPassTab.hide\",\r\n            \"value\": false,\r\n            \"description\": \"Hide the parking pass tab\"\r\n        },\r\n        {\r\n            \"name\": \"ulf.faceValue.optionalField\",\r\n            \"value\": true,\r\n            \"description\": \"Face value is a non-mandatory field\"\r\n        },\r\n        {\r\n            \"name\": \"ulf.only.privateSeller\",\r\n            \"value\": false,\r\n            \"description\": \"Allow only private seller\"\r\n        },\r\n        {\r\n            \"name\": \"event.sectionZoneToggle.show\",\r\n            \"value\": true,\r\n            \"description\": \"Feature to show section zone toggle based on event location\"\r\n        },\r\n        {\r\n            \"name\": \"ulf.faceValue.display\",\r\n            \"value\": false,\r\n            \"description\": \"Show facevalue option on delivery page\"\r\n        },\r\n        {\r\n            \"name\": \"myaccount.listing.faceValueRequired\",\r\n            \"value\": false,\r\n            \"description\": \"myaccount listing faceValueRequired\"\r\n        },\r\n        {\r\n            \"name\": \"ulf.business.seller.infoLink.display\",\r\n            \"value\": true,\r\n            \"description\": \"Show help services for more information of busineess seller\"\r\n        }\r\n    ]\r\n}";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		return response;
	}
	
	private Response getGlobalRegApiSResponseNull() {
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
				String response = null;
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		return response;
	}
	
	private Response getGlobalRegApiSResponse404() {
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
				String response = null;
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		return response;
	}


}
