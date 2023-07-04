package com.stubhub.domain.inventory.biz.v2.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.stubhub.domain.fulfillment.pdf.v1.intf.request.CopyTicketSeat;
import com.stubhub.domain.fulfillment.pdf.v1.intf.response.CloneAndLinkTicketResponse;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.fulfillment.pdf.v1.intf.response.CloneFileInfoResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.BaseCostResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.DeliveryMethodResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.EventFulfillmentWindowResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.FulfillmentMethodResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.FulfillmentWindowResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.ListingFulfillmentWindowResponse;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.inventory.biz.v2.impl.util.FulfillmentServiceAdapter;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

public class FulfillmentServiceAdapterTest {
  @Mock
  SvcLocator svcLocator;

  @Mock
  MasterStubhubPropertiesWrapper masterStubhubProperties;

  // @Spy
  // Logger log = LoggerFactory.getLogger(FulfillmentServiceAdapter.class);
  //
  // void log(String callerFQCN, Priority level, Object message, Throwable t) {

  @Mock
  private WebClient webClient;

  @Mock
  private ObjectMapper objectMapper;

  @InjectMocks
  FulfillmentServiceAdapter fulfillmentServiceAdapter;

  @BeforeMethod
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    Mockito
        .when(masterStubhubProperties.getProperty("fulfillment.window.v1.shape.api.url",
            "https://api.stubcloudprod.com/fulfillment/window/v1/event/{eventId}/?sellerContactId={sellerContactId}"))
        .thenReturn(
            "https://api.stubcloudprod.com/fulfillment/window/v1/event/{eventId}/?sellerContactId={sellerContactId}");
    Mockito
        .when(masterStubhubProperties.getProperty("pdf.clone.v1.shape.api.url",
            "https://api.stubcloudprod.com/fulfillment/pdf/v1/clone"))
        .thenReturn("https://api.stubcloudprod.com/fulfillment/pdf/v1/clone");
    Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
  }

  private Response getResponse_200_ValidEFWR01() {
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
        String response =
            "{\"eventId\": 9167852, \"fulfillmentWindows\": [{\"fulfillmentMethod\": {\"id\": 2, \"name\": \"Barcode - PreDelivery (Non-STH)\"},\"endTime\": \"2017-04-05T04:00:00Z\",}]";
        return new ByteArrayInputStream(response.getBytes());
      }
    };
    return response;
  }

  private Response getResponse_200_ValidEFWR02() {
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
        return null;
      }
    };
    return response;
  }


  private Response getResponse_500_ValidEFWR01() {
    Response response = new Response() {

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

  private EventFulfillmentWindowResponse getEFWR01() {
    EventFulfillmentWindowResponse efwr = new EventFulfillmentWindowResponse();
    efwr.setFulfillmentWindows(getFulfillmentWindowResponse01());
    return efwr;
  }

  private Collection<FulfillmentWindowResponse> getFulfillmentWindowResponse01() {

    List<FulfillmentWindowResponse> listFFWindows = new ArrayList<FulfillmentWindowResponse>();
    listFFWindows.add(getFulfillmentWindow(12145L, 10L, "UPS", 24L, "2015-06-12T11:00:00",
        "2015-08-12T11:00:00", 5.5));
    listFFWindows.add(getFulfillmentWindow(10452L, 4L, "PDF - PreDelivery", 10L,
        "2015-06-12T11:00:00", "2015-08-12T11:00:00", 0.0));
    listFFWindows.add(getFulfillmentWindow(10953L, 6L, "Barcode - PreDelivery (Non-STH)", 8L,
        "2015-06-12T11:00:00", "2015-08-12T11:00:00", 0.0));
    return listFFWindows;
  }

  private FulfillmentWindowResponse getFulfillmentWindow(Long winId, Long ffMethodId,
      String ffMethodName, Long deliveryMethodId, String startTime, String endTime, double amount) {

    FulfillmentWindowResponse ffw = new FulfillmentWindowResponse();
    FulfillmentMethodResponse ffm = new FulfillmentMethodResponse();
    DeliveryMethodResponse dmr = new DeliveryMethodResponse();

    ffm.setId(ffMethodId);
    ffm.setName(ffMethodName);

    dmr.setId(deliveryMethodId);

    ffw.setFulfillmentMethod(ffm);
    ffw.setDeliveryMethod(dmr);
    ffw.setBaseCost(new BaseCostResponse(BigDecimal.valueOf(amount), BigDecimal.valueOf(amount),
        BigDecimal.valueOf(amount), "USD"));
    ffw.setStartTime(convertToCalendar(startTime));
    ffw.setEndTime(convertToCalendar(endTime));

    return ffw;
  }

  private Calendar convertToCalendar(String dateStr) {
    Calendar cal = Calendar.getInstance();
    try {
      DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      Date date = format.parse(dateStr);
      cal = Calendar.getInstance();
      cal.setTime(date);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return cal;
  }

  @Test
  public void getFulfillmentWindowsShape() throws Exception {

    Long eventId = new Long(9167852L);
    Long userId = new Long(9167853L);

    Mockito.when(webClient.get()).thenReturn(getResponse_200_ValidEFWR01());

    EventFulfillmentWindowResponse efwr = getEFWR01();
    Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(),
        Mockito.eq(EventFulfillmentWindowResponse.class))).thenReturn(efwr);

    EventFulfillmentWindowResponse response =
        fulfillmentServiceAdapter.getFulfillmentWindowsShape(eventId, userId);
    Assert.assertNotNull(response);

  }

  @Test
  public void getFulfillmentWindowsShape03() throws Exception {

    Long eventId = new Long(9167852L);
    Long userId = new Long(9167853L);

    Mockito.when(webClient.get()).thenReturn(getResponse_200_ValidEFWR02());
    Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(),
        Mockito.eq(EventFulfillmentWindowResponse.class))).thenReturn(null);

    Assert.assertNull(fulfillmentServiceAdapter.getFulfillmentWindowsShape(eventId, userId));
  }


  @Test(expectedExceptions = {SHSystemException.class})
  public void getFulfillmentWindowsShape01() {

    Long eventId = new Long(9167852L);
    Long userId = new Long(9167853L);

    Mockito.when(webClient.get()).thenReturn(getResponse_500_ValidEFWR01());

    fulfillmentServiceAdapter.getFulfillmentWindowsShape(eventId, userId);
  }

  @Test
  public void testGetFulfillmentWindows() {
    fulfillmentServiceAdapter.getFulfillmentWindows(getEFWR01());
  }

  @Test
  public void testGetFulfillmentWindowsShapeForListing() throws Exception {
    Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString()))
        .thenReturn("http://api-int.slcq015.com/fulfillment/window/v1/listing");
    Mockito.when(objectMapper.configure(
        Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false)))
        .thenReturn(objectMapper);
    Mockito.when(webClient.get()).thenReturn(getLFWResponse200());
    Mockito
        .when(objectMapper.readValue((InputStream) Mockito.anyObject(),
            Mockito.eq(ListingFulfillmentWindowResponse.class)))
        .thenReturn(new ListingFulfillmentWindowResponse());
    ListingFulfillmentWindowResponse lfwr1 =
        fulfillmentServiceAdapter.getFulfillmentWindowsShapeForListing(12345L, 1223345L);
    Assert.assertNotNull(lfwr1);
    ListingFulfillmentWindowResponse lfwr2 =
        fulfillmentServiceAdapter.getFulfillmentWindowsShapeForListing(12345L, null);
    Assert.assertNotNull(lfwr2);
  }

  @Test
  public void testGetFulfillmentWindowsShapeForListing500() throws Exception {
    Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString()))
        .thenReturn("http://api-int.slcq015.com/fulfillment/window/v1/listing");
    Mockito.when(objectMapper.configure(
        Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false)))
        .thenReturn(objectMapper);
    Mockito.when(webClient.get()).thenReturn(getLFWResponse500());
    Mockito
        .when(objectMapper.readValue((InputStream) Mockito.anyObject(),
            Mockito.eq(ListingFulfillmentWindowResponse.class)))
        .thenReturn(new ListingFulfillmentWindowResponse());
    ListingFulfillmentWindowResponse lfwr1 =
        fulfillmentServiceAdapter.getFulfillmentWindowsShapeForListing(12345L, 1223345L);
    Assert.assertNull(lfwr1);
    ListingFulfillmentWindowResponse lfwr2 =
        fulfillmentServiceAdapter.getFulfillmentWindowsShapeForListing(12345L, null);
    Assert.assertNull(lfwr2);
  }

  @Test
  public void testGetFulfillmentWindowsShapeForListingException() throws Exception {
    Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString()))
        .thenReturn("http://api-int.slcq015.com/fulfillment/window/v1/listing");
    Mockito.when(objectMapper.configure(
        Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false)))
        .thenReturn(objectMapper);
    Mockito.when(webClient.get()).thenThrow(new RuntimeException());
    Mockito
        .when(objectMapper.readValue((InputStream) Mockito.anyObject(),
            Mockito.eq(ListingFulfillmentWindowResponse.class)))
        .thenReturn(new ListingFulfillmentWindowResponse());
    ListingFulfillmentWindowResponse lfwr1 =
        fulfillmentServiceAdapter.getFulfillmentWindowsShapeForListing(12345L, 1223345L);
    Assert.assertNull(lfwr1);
    ListingFulfillmentWindowResponse lfwr2 =
        fulfillmentServiceAdapter.getFulfillmentWindowsShapeForListing(12345L, null);
    Assert.assertNull(lfwr2);
  }

  private Response getLFWResponse200() {
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
        String response = "{\"listingId\": 12345,\"fulfillmentWindows\": []}";
        return new ByteArrayInputStream(response.getBytes());

      }
    };
    return response;
  }

  private Response getLFWResponse500() {
    Response response = new Response() {

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

  @Test(expectedExceptions = {SHSystemException.class})
  public void testCloneFileInfo_NullPointer() {
    Long stubTransId = 33L;
    Long listingId = 9L;
    List<CopyTicketSeat> copyTicketSeatList = new ArrayList<CopyTicketSeat>();
    WebClient webClient = mock(WebClient.class);
    Response response = mock(Response.class);
    when(webClient.post(anyObject())).thenReturn(response);
    when(svcLocator.locate(anyString(), anyList())).thenReturn(webClient);
    when(response.getStatus()).thenReturn(200);
    CloneFileInfoResponse cloneFileInfoResponse = new CloneFileInfoResponse();
    cloneFileInfoResponse.setFileInfoId(null);
    when(response.getEntity()).thenReturn(cloneFileInfoResponse);
    List<Long> cloneFileInfo = fulfillmentServiceAdapter.cloneFileInfo(listingId, stubTransId,copyTicketSeatList);
  }

  @Test
  public void testCloneFileInfo() {
    Long stubTransId = 33L;
    Long listingId = 9L;
    List<CopyTicketSeat> copyTicketSeatList = new ArrayList<CopyTicketSeat>();
    List<Long> fileInfoList = new ArrayList<>();
    Long newFileInfoId = 13L;
    fileInfoList.add(newFileInfoId);
    WebClient webClient = mock(WebClient.class);
    Response response = mock(Response.class);
    when(webClient.post(anyObject())).thenReturn(response);
    when(svcLocator.locate(anyString(), anyList())).thenReturn(webClient);
    when(response.getStatus()).thenReturn(200);
    CloneAndLinkTicketResponse cloneFileInfoResponse = new CloneAndLinkTicketResponse();
    cloneFileInfoResponse.setFileInfoIdList(fileInfoList);
    when(response.getEntity()).thenReturn(cloneFileInfoResponse);
    List<Long> cloneFileInfo = fulfillmentServiceAdapter.cloneFileInfo(listingId, stubTransId,copyTicketSeatList);
    assertEquals(newFileInfoId, cloneFileInfo.get(0));
  }

}
