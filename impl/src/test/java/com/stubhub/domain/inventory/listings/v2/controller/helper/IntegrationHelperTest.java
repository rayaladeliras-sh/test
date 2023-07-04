package com.stubhub.domain.inventory.listings.v2.controller.helper;

import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.inventory.biz.v2.intf.IntegrationManager;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingType;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.util.JMSMessageHelper;
import com.stubhub.domain.inventory.v2.DTO.Buyer;
import com.stubhub.domain.inventory.v2.DTO.ExternalOrderRequest;
import com.stubhub.domain.inventory.v2.DTO.ExternalOrderResponse;
import com.stubhub.domain.user.services.customers.v2.intf.GetCustomerResponse;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class IntegrationHelperTest {
	
	@InjectMocks
	private IntegrationHelper integrationHelper;
	
	@Mock
	private SvcLocator svcLocator;
	
	@Mock
	private ObjectMapper objectMapper;
	
	@Mock
	private TicketSeatMgr ticketSeatMgr;
	
	@Mock
	private InventoryMgr inventoryMgr;
	
	@Mock
    private IntegrationManager integrationManager;
	
	@Mock
	private WebClient webClient;
	
	@Mock
	private JMSMessageHelper jmsHelper;
	
	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		integrationHelper = new IntegrationHelper() {
			protected String getProperty(String propertyName, String defaultValue) {
				if ("ship.partner.integration.hold.inventory.api.url".equals(propertyName)) {
					return "https://api-int.slcq015.com/integration/holdinventory/v2";
				}
				return "";
			}
			protected int getPropertyAsInt(String propertyName, int defaultValue) {
				if ("ship.partner.integration.retryCount".equals(propertyName)) {
					return 1;
				}
				return 1;
			}
			protected WebClient createWebClient(final String reserveInventoryURL, List<Object> providers) {
				return webClient;
			}
		};
		ReflectionTestUtils.setField(integrationHelper, "objectMapper", objectMapper);
		ReflectionTestUtils.setField(integrationHelper, "ticketSeatMgr", ticketSeatMgr);
		ReflectionTestUtils.setField(integrationHelper, "inventoryMgr", inventoryMgr);
		ReflectionTestUtils.setField(integrationHelper, "integrationManager", integrationManager);
		ReflectionTestUtils.setField(integrationHelper, "jmsMessageHelper", jmsHelper);
	}
	
	
	
	
	@Test
	public void testReserveInventory() throws Exception {
		
		
		Listing listing = getListing();
		List<TicketSeat> requestedTicketSeats = new ArrayList<TicketSeat>();
		requestedTicketSeats.add(listing.getTicketSeats().get(0));
		GetCustomerResponse customerDetails = new GetCustomerResponse();
		Buyer buyer = new Buyer();
		
		Mockito.when(objectMapper.configure(Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false))).thenReturn(objectMapper);

		
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.post(Mockito.any(ExternalOrderRequest.class))).thenReturn(getReserveResponse200());
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
		
		ListingHolder holder = new ListingHolder(listing,requestedTicketSeats,123L,buyer);
		holder.setFulfillmentType("Barcode");
		
		boolean reserveStatus = integrationHelper.reserveInventory(holder,customerDetails);
		Assert.assertTrue(reserveStatus);
	}
	
	@Test
    public void testReserveInventoryForTT() throws Exception {
        
        
        Listing listing = getListing();
        List<TicketSeat> requestedTicketSeats = new ArrayList<TicketSeat>();
        requestedTicketSeats.add(listing.getTicketSeats().get(0));
        GetCustomerResponse customerDetails = new GetCustomerResponse();
        Buyer buyer = new Buyer();
        
        Mockito.when(objectMapper.configure(Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false))).thenReturn(objectMapper);

        
        Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
        Mockito.when(webClient.post(Mockito.any(ExternalOrderRequest.class))).thenReturn(getTTReserveResponse200());
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
        
        ListingHolder holder = new ListingHolder(listing,requestedTicketSeats,123L,buyer);
        holder.setFulfillmentType("Barcode");
        
        boolean reserveStatus = integrationHelper.reserveInventory(holder,customerDetails);
        Assert.assertTrue(reserveStatus);
    }
	
	@Test
	public void testGetProperty() {
		IntegrationHelper integrationHelper = new IntegrationHelper();
		try {
			String property = integrationHelper.getProperty("abc", "123");
			Assert.fail("should fail");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int intProp = integrationHelper.getPropertyAsInt("123", 123);	
		Assert.assertEquals(intProp,123);
		
		integrationHelper.createWebClient("http://shipurl", null);
		
	}
	
	@Test
	public void testReserveInventory400() throws Exception {
		Listing listing = getListing();
		List<TicketSeat> requestedTicketSeats = new ArrayList<TicketSeat>();
		requestedTicketSeats.add(listing.getTicketSeats().get(0));
		
		Mockito.when(objectMapper.configure(Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false))).thenReturn(objectMapper);
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.post(Mockito.any(ExternalOrderRequest.class))).thenReturn(getReserveResponse400());
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
		
		ExternalOrderResponse externalOrderResponse = getExternalOrderResponse();
		externalOrderResponse.setDeleteInventory(true);
		Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(ExternalOrderResponse.class))).thenReturn(externalOrderResponse);
		
		GetCustomerResponse customerDetails = new GetCustomerResponse();
		Buyer buyer = new Buyer();
		ListingHolder holder = new ListingHolder(listing,requestedTicketSeats,123L,buyer);
		holder.setFulfillmentType("Barcode");
		boolean reserveStatus = integrationHelper.reserveInventory(holder,customerDetails);
		Assert.assertFalse(reserveStatus);
	}
	
	@Test
	public void testReserveInventoryBarcodeUnlock() throws Exception {
		Listing listing = getListing();
		listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		listing.setTicketMedium(TicketMedium.BARCODE.getValue());
		List<TicketSeat> requestedTicketSeats = new ArrayList<TicketSeat>();
		requestedTicketSeats.add(listing.getTicketSeats().get(0));
		
		Mockito.when(objectMapper.configure(Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false))).thenReturn(objectMapper);
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.post(Mockito.any(ExternalOrderRequest.class))).thenReturn(getReserveResponse400());
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
		
		ExternalOrderResponse externalOrderResponse = getExternalOrderResponse();
		externalOrderResponse.setDeleteInventory(true);
		Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(ExternalOrderResponse.class))).thenReturn(externalOrderResponse);
		
		GetCustomerResponse customerDetails = new GetCustomerResponse();
		Buyer buyer = new Buyer();
		ListingHolder holder = new ListingHolder(listing,requestedTicketSeats,123L,buyer);
		holder.setFulfillmentType("Barcode");
		boolean reserveStatus = integrationHelper.reserveInventory(holder,customerDetails);
		Assert.assertFalse(reserveStatus);
	}
	
	@Test
	public void testReserveInventoryBarcodeUnlockAllListing() throws Exception {
		Listing listing = getListing();
		listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		listing.setTicketMedium(TicketMedium.BARCODE.getValue());
		List<TicketSeat> requestedTicketSeats = new ArrayList<TicketSeat>();
		requestedTicketSeats.addAll(listing.getTicketSeats());
		
		Mockito.when(objectMapper.configure(Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false))).thenReturn(objectMapper);
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.post(Mockito.any(ExternalOrderRequest.class))).thenReturn(getReserveResponse400());
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
		
		ExternalOrderResponse externalOrderResponse = getExternalOrderResponse();
		externalOrderResponse.setDeleteInventory(true);
		Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(ExternalOrderResponse.class))).thenReturn(externalOrderResponse);
		
		GetCustomerResponse customerDetails = new GetCustomerResponse();
		Buyer buyer = new Buyer();
		ListingHolder holder = new ListingHolder(listing,requestedTicketSeats,123L,buyer);
		holder.setFulfillmentType("Barcode");
		boolean reserveStatus = integrationHelper.reserveInventory(holder,customerDetails);
		Assert.assertFalse(reserveStatus);
	}
	
	@Test
    public void testReserveInventoryAllSeats400() throws Exception {
        Listing listing = getListing();
        List<TicketSeat> requestedTicketSeats = new ArrayList<TicketSeat>();
        requestedTicketSeats.add(listing.getTicketSeats().get(0));
        requestedTicketSeats.add(listing.getTicketSeats().get(1));
        
        Mockito.when(objectMapper.configure(Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false))).thenReturn(objectMapper);
        Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
        Mockito.when(webClient.post(Mockito.any(ExternalOrderRequest.class))).thenReturn(getReserveResponse400());
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
        
        ExternalOrderResponse externalOrderResponse = getExternalOrderResponse();
        externalOrderResponse.setDeleteInventory(true);
        Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(ExternalOrderResponse.class))).thenReturn(externalOrderResponse);
        
        GetCustomerResponse customerDetails = new GetCustomerResponse();
        Buyer buyer = new Buyer();
        ListingHolder holder = new ListingHolder(listing,requestedTicketSeats,123L,buyer);
        holder.setFulfillmentType("Barcode");
        boolean reserveStatus = integrationHelper.reserveInventory(holder,customerDetails);
        Assert.assertFalse(reserveStatus);
    }
	
	@Test
	public void testReserveInventory404() throws Exception {
		Listing listing = getListing();
		List<TicketSeat> requestedTicketSeats = new ArrayList<TicketSeat>();
		requestedTicketSeats.add(listing.getTicketSeats().get(0));
		
		Mockito.when(objectMapper.configure(Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false))).thenReturn(objectMapper);
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.post(Mockito.any(ExternalOrderRequest.class))).thenReturn(getReserveResponse404());
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
		
		GetCustomerResponse customerDetails = new GetCustomerResponse();
		Buyer buyer = new Buyer();
		ListingHolder holder = new ListingHolder(listing,requestedTicketSeats,123L,buyer);
		holder.setFulfillmentType("Barcode");
		boolean reserveStatus = integrationHelper.reserveInventory(holder,customerDetails);
		Assert.assertFalse(reserveStatus);
	}
	
	@Test(expectedExceptions=SHSystemException.class)
    public void testReserveInventory500() throws Exception {
        Listing listing = getListing();
        List<TicketSeat> requestedTicketSeats = new ArrayList<TicketSeat>();
        requestedTicketSeats.add(listing.getTicketSeats().get(0));
        
        Mockito.when(objectMapper.configure(Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false))).thenReturn(objectMapper);
        Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
        Mockito.when(webClient.post(Mockito.any(ExternalOrderRequest.class))).thenReturn(getReserveResponse500());
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
        
        GetCustomerResponse customerDetails = new GetCustomerResponse();
        Buyer buyer = new Buyer();
        ListingHolder holder = new ListingHolder(listing,requestedTicketSeats,123L,buyer);
        holder.setFulfillmentType("Barcode");
        boolean reserveStatus = integrationHelper.reserveInventory(holder,customerDetails);
        Assert.assertFalse(reserveStatus);
    }
	
	@Test(expectedExceptions=SHSystemException.class)
	public void testReserveInventoryException() throws Exception {
		Listing listing = getListing();
		List<TicketSeat> requestedTicketSeats = new ArrayList<TicketSeat>();
		requestedTicketSeats.add(listing.getTicketSeats().get(0));
		
		SHAPIContext context = new SHAPIContext();
        SHAPIThreadLocal.set(context);
		Mockito.when(objectMapper.configure(Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false))).thenReturn(objectMapper);
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		ClientConfiguration clientConfiguration = new ClientConfiguration();
	    Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
		Mockito.when(webClient.post(Mockito.any(ExternalOrderRequest.class))).thenThrow(new RuntimeException());
		
		GetCustomerResponse customerDetails = new GetCustomerResponse();
		Buyer buyer = new Buyer();
		ListingHolder holder = new ListingHolder(listing,requestedTicketSeats,123L,buyer);
		holder.setFulfillmentType("Barcode");
		boolean reserveStatus = integrationHelper.reserveInventory(holder,customerDetails);
		Assert.assertFalse(reserveStatus);
	}
	
	@Test(expectedExceptions=SHSystemException.class)
    public void testReserveInventoryTimeoutException() throws Exception {
        Listing listing = getListing();
        List<TicketSeat> requestedTicketSeats = new ArrayList<TicketSeat>();
        requestedTicketSeats.add(listing.getTicketSeats().get(0));
        
        SHAPIContext context = new SHAPIContext();
        SHAPIThreadLocal.set(context);
        Mockito.when(objectMapper.configure(Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false))).thenReturn(objectMapper);
        Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
        Mockito.when(webClient.post(Mockito.any(ExternalOrderRequest.class))).thenThrow(SocketTimeoutException.class);
        
        GetCustomerResponse customerDetails = new GetCustomerResponse();
        Buyer buyer = new Buyer();
        ListingHolder holder = new ListingHolder(listing,requestedTicketSeats,123L,buyer);
        holder.setFulfillmentType("Barcode");
        boolean reserveStatus = integrationHelper.reserveInventory(holder,customerDetails);
        Assert.assertFalse(reserveStatus);
    }
	

	
	@Test
	public void testGetShipCustomer(){
		
		ObjectMapper objectMappper = new ObjectMapper();
		integrationHelper.setObjectMapper(objectMappper);
		SHAPIContext context = new SHAPIContext();
		SHAPIThreadLocal.set(context);
		
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getCustomerGuidResponse200(),getCustomerDetailsResponse200());
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
		//Mockito.doNothing().when(listingDetailValidator).validate((ListingsControllerRequest)Mockito.anyObject(), Mockito.anyString());
		
		GetCustomerResponse customerDetails = integrationHelper.getShipCustomer(123456L);
		Assert.assertNotNull(customerDetails);
	}
	
	@Test
    public void testGetShipCustomerError(){
        
        ObjectMapper objectMappper = new ObjectMapper();
        integrationHelper.setObjectMapper(objectMappper);
        SHAPIContext context = new SHAPIContext();
        SHAPIThreadLocal.set(context);
        
        Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
        Mockito.when(webClient.get()).thenReturn(getCustomerGuidResponse200(),getCustomerDetailsResponse401());
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
        //Mockito.doNothing().when(listingDetailValidator).validate((ListingsControllerRequest)Mockito.anyObject(), Mockito.anyString());
        
        try {
          GetCustomerResponse customerDetails = integrationHelper.getShipCustomer(123456L);
          Assert.fail("should have thrown an exception");
        } catch (Exception e) {
          Assert.assertTrue(true);
        }
    }
	
	@Test
	public void testGetShipCustomerNull(){
		
		ObjectMapper objectMappper = new ObjectMapper();
		integrationHelper.setObjectMapper(objectMappper);
		SHAPIContext context = new SHAPIContext();
		SHAPIThreadLocal.set(context);
		
		//Mockito.when(objectMapper.configure(Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false))).thenReturn(objectMapper);
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getReserveResponse404());	
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
		
		
		GetCustomerResponse customerDetails;
        try {
          customerDetails = integrationHelper.getShipCustomer(123456L);
          Assert.fail("should have thrown an exception");
        } catch (Exception e) {
          Assert.assertTrue(true);
        }
    		
    	}
	
	@Test
	public void testGetUserGuidFromUid() throws Exception {
		ObjectMapper objectMappper = new ObjectMapper();
		integrationHelper.setObjectMapper(objectMappper);
		SHAPIContext context = new SHAPIContext();
		SHAPIThreadLocal.set(context);
		
		//Mockito.when(objectMapper.configure(Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false))).thenReturn(objectMapper);
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getCustomerGuidResponse200());	
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
		
		
		String guid = integrationHelper.getUserGuidFromUid(123456L);
		Assert.assertNotNull(guid);
		
	}
	
	@Test
	public void testGetUserGuidFromUidRetries()  {
		ObjectMapper objectMappper = new ObjectMapper();
		integrationHelper.setObjectMapper(objectMappper);
		SHAPIContext context = new SHAPIContext();
		SHAPIThreadLocal.set(context);
		
		//Mockito.when(objectMapper.configure(Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false))).thenReturn(objectMapper);
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		String guid;
		try {
			Mockito.when(webClient.get()).thenThrow(new RuntimeException());	
			guid = integrationHelper.getUserGuidFromUid(123456L);
			Assert.fail("should fail with an exception");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	@Test
	public void testGetCustomer() throws Exception {
		ObjectMapper objectMappper = new ObjectMapper();
		integrationHelper.setObjectMapper(objectMappper);
		SHAPIContext context = new SHAPIContext();
		SHAPIThreadLocal.set(context);
		
		//Mockito.when(objectMapper.configure(Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false))).thenReturn(objectMapper);
		
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getCustomerDetailsResponse200());
		Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);		
		GetCustomerResponse customerDetails = integrationHelper.getShipOrderIntegrationOptinUser("123456");
		Assert.assertNotNull(customerDetails);
		
	}
	
	@Test
	public void testGetCustomerWithRetries() throws Exception {
		ObjectMapper objectMappper = new ObjectMapper();
		integrationHelper.setObjectMapper(objectMappper);
		SHAPIContext context = new SHAPIContext();
		SHAPIThreadLocal.set(context);
		
		//Mockito.when(objectMapper.configure(Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false))).thenReturn(objectMapper);
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenThrow(new RuntimeException());		
		try {
			GetCustomerResponse customerDetails = integrationHelper.getShipOrderIntegrationOptinUser("123456");
			Assert.fail("fail with exception");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	

	
	
	
	//--
	
	private Response getReserveResponse200() {
		Response response = new Response() {

			@Override
			public Object getEntity() {
				String response = "{\"orderId\":12345,\"listingId\":1234,\"externalListingId\":\"ABC123\",\"deleteInventory\":false}";
				return new ByteArrayInputStream(response.getBytes());
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
	
	private Response getTTReserveResponse200() {
      Response response = new Response() {

          @Override
          public Object getEntity() {
              String response = "{\"orderId\":12345,\"listingId\":1234,\"externalListingId\":\"ABC123\",\"deleteInventory\":false,\"shoppingCartGuid\":12345}";
              return new ByteArrayInputStream(response.getBytes());
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
	
	private Response getReserveResponse400() {
		Response response = new Response() {

			@Override
			public Object getEntity() {
				String response = "{\"orderId\":12345,\"listingId\":1234,\"externalListingId\":\"ABC123\",\"deleteInventory\":true}";
				return new ByteArrayInputStream(response.getBytes());
			}

			@Override
			public int getStatus() {
				return 400;
			}

			@Override
			public MultivaluedMap<String, Object> getMetadata() {
				return null;
			}
		};
		return response;
	}
	
	private Response getReserveResponse404() {
		Response response = new Response() {

			@Override
			public Object getEntity() {
				return null;
			}

			@Override
			public int getStatus() {
				return 404;
			}

			@Override
			public MultivaluedMap<String, Object> getMetadata() {
				return null;
			}
		};
		return response;
	}
	
	private Response getReserveResponse500() {
      Response response = new Response() {

          @Override
          public Object getEntity() {
              return null;
          }

          @Override
          public int getStatus() {
              return 500;
          }

          @Override
          public MultivaluedMap<String, Object> getMetadata() {
              return null;
          }
      };
      return response;
  }
	
	private ExternalOrderResponse getExternalOrderResponse() {
		ExternalOrderResponse resp = new ExternalOrderResponse();
		resp.setDeleteInventory(false);
		resp.setOrderId(123L);
		resp.setListingId(12345L);
		resp.setExternalListingId("EXT-1234");
		return resp;
	}
	
	private Listing getListing() {
		Listing listing = new Listing();
		listing.setId(12345L);
		listing.setExternalId("EXT-1234");
		listing.setEventId(1234L);
		listing.setSellerId(123456L);
		listing.setListPrice(new Money("12"));
		listing.setSection("Lower Box");
		listing.setQuantity(4);
		listing.setQuantityRemain(2);
		listing.setListingType(ListingType.TICKETS_WITH_PARKING_PASSES_INCLUDED.getId());
		
		TicketSeat ts1 = new TicketSeat();
		ts1.setSection("Lower Box");
		ts1.setRow("1");
		ts1.setSeatNumber("1");
		ts1.setSeatStatusId(1L);
		
		TicketSeat ts2 = new TicketSeat();
		ts2.setSection("Lower Box");
		ts2.setRow("1");
		ts2.setSeatNumber("2");
		ts2.setSeatStatusId(1L);
		
		List<TicketSeat> ticketSeatList = new ArrayList<TicketSeat>();
		ticketSeatList.add(ts1);
		ticketSeatList.add(ts2);
		listing.setTicketSeats(ticketSeatList);
		
		return listing;
	}
	
	private Response getCustomerGuidResponse200() {
		Response response = new Response() {

			@Override
			public Object getEntity() {
				String response = "{\"customer\":{\"userCookieGuid\":\"ABCDEFGH\"}}";
				return new ByteArrayInputStream(response.getBytes());
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
	
	
	private Response getCustomerDetailsResponse200() {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);
		
		Response response = new Response() {

			@Override
			public Object getEntity() {
				
				try {
					GetCustomerResponse customerDetails = new GetCustomerResponse();
					String jsonString = objectMapper.writeValueAsString(customerDetails);
					return new ByteArrayInputStream(jsonString.getBytes());

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
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
	
	private Response getCustomerDetailsResponse401() {
      final ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, true);
      
      Response response = new Response() {

          @Override
          public Object getEntity() {
              
              try {
                  GetCustomerResponse customerDetails = new GetCustomerResponse();
                  String jsonString = objectMapper.writeValueAsString(customerDetails);
                  return new ByteArrayInputStream(jsonString.getBytes());

              } catch (IOException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                  return null;
              }
          }

          @Override
          public int getStatus() {
              return 401;
          }

          @Override
          public MultivaluedMap<String, Object> getMetadata() {
              return null;
          }
      };
      return response;
  }
	
}
