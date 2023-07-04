package com.stubhub.domain.inventory.listings.v2.helper;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import com.stubhub.newplatform.common.entity.Money;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.TicketSeatHelper;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.GetSRSForBarcodesResponse;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.Ticket;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.SecondaryIntegrationUtil;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.entity.ErrorDetail;
import com.stubhub.domain.inventory.listings.v2.entity.SeatProduct;
import com.stubhub.domain.inventory.listings.v2.util.PrimaryIntegrationUtil;
import com.stubhub.domain.inventory.listings.v2.util.SeatProductsContext;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.newplatform.http.util.HttpClient4Util.SimpleHttpResponse;
import com.stubhub.newplatform.http.util.HttpClient4UtilHelper;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

import junit.framework.Assert;


public class PrimaryIntegrationUtilTest {

  @InjectMocks
  private PrimaryIntegrationUtil primaryIntegrationUtil;


  @Mock
  private SimpleHttpResponse response;

  @Mock
  private SvcLocator svcLocator;

  @Mock
  private WebClient webClient;

  @Mock
  private TicketSeatHelper ticketSeatHelper;

  private Listing listing;
  private List<SeatProduct> seats;

  @BeforeMethod
  public void setUp() throws IOException {

    MockitoAnnotations.initMocks(this);

    primaryIntegrationUtil = new PrimaryIntegrationUtil() {
      protected String getProperty(String propertyName, String defaultValue) {
        if ("primaryIntegration_Invalid_Barcode_Format_Length".equalsIgnoreCase(propertyName)) {
          return "46,47";
        } else if ("primaryIntegration_Invalid_Barcode".equalsIgnoreCase(propertyName)) {
          return "10571,10573,10567,10582,10569,10585,10568,10584,10579";
        } else if ("primaryIntegration_Barcode_Already_Used".equalsIgnoreCase(propertyName)) {
          return "10581,10583,66,226";
        } else if ("inventory.integration.primary.endpoint".equalsIgnoreCase(propertyName)) {
          return "https://intsvc.api.srwd34.com/integrationAPI/PrimaryIntegration";
        }
        return null;
      }

      protected int getPropertyAsInt(String propertyName, int defaultValue) {
        if ("inventory.integrationAPI.request.timeout".equalsIgnoreCase(propertyName)) {
          return 120000;
        }
        return 120000;
      }
    };

    String responseStr =
        "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">   <soap:Body>      <ini:VerifyBarcodeTicketsResponse xmlns:ini=\"http://integration.soap.api.stubcloudprod.com/\"> <IsSuccessful>true</IsSuccessful> <TicketSeatPTVTicket><TicketSeatId>123</TicketSeatId> <PTVTicketId>123</PTVTicketId> </TicketSeatPTVTicket></ini:VerifyBarcodeTicketsResponse> </soap:Body></soap:Envelope>";
    Mockito.when(response.getStatusCode()).thenReturn(200);
    Mockito.when(response.getContent()).thenReturn(responseStr);

    ReflectionTestUtils.setField(primaryIntegrationUtil, "svcLocator", svcLocator);
    when(svcLocator.locate(anyString())).thenReturn(webClient);


    ReflectionTestUtils.setField(primaryIntegrationUtil, "ticketSeatHelper", ticketSeatHelper);
  }

  @Test
  public void testVerifySthInventoryNullContexts() throws Exception {
    String errorMsg = primaryIntegrationUtil.verifySthInventory(null);
    Assert.assertEquals("No seatProdContexts", errorMsg);
  }

  @Test
  public void testVerifySthInventoryEmptyContexts() throws Exception {
    List<SeatProductsContext> contexts = new ArrayList<SeatProductsContext>();
    String errorMsg = primaryIntegrationUtil.verifySthInventory(contexts);
    Assert.assertEquals("No seatProdContexts", errorMsg);
  }

  @Test
  public void testVerifySthInventoryNullFulfillmentArtifact() throws Exception {
    SeatProduct sp = new SeatProduct();
    SeatProductsContext context = new SeatProductsContext(new Listing(), null, null, null);
    context.addArtifactSeatProductToList(sp);
    List<SeatProductsContext> contexts = new ArrayList<SeatProductsContext>();
    contexts.add(context);
    String errorMsg = primaryIntegrationUtil.verifySthInventory(contexts);
    Assert.assertEquals("FulfillmentArtifact is null", errorMsg);
  }

  @Test
  public void testVerifySthInventorySuccess() throws Exception {
    SeatProduct sp = new SeatProduct();
    sp.setFulfillmentArtifact("12345678");
    SeatProductsContext context = new SeatProductsContext(new Listing(), null, null, null);
    context.addArtifactSeatProductToList(sp);

    List<SeatProductsContext> contexts = new ArrayList<SeatProductsContext>();
    contexts.add(context);


    String successStr = "{\"verificationPassed\":true}";
    InputStream is = new ByteArrayInputStream(successStr.getBytes());
    Response response = Response.status(Status.OK).entity(is).build();
    when(webClient.post(anyObject())).thenReturn(response);

    String errorMsg = primaryIntegrationUtil.verifySthInventory(contexts);
    Assert.assertNull(errorMsg);
  }

  @Test
  public void testVerifySthInventoryError() throws Exception {
    SeatProduct sp = new SeatProduct();
    sp.setFulfillmentArtifact("12345678");
    SeatProductsContext context = new SeatProductsContext(new Listing(), null, null, null);
    context.addArtifactSeatProductToList(sp);

    List<SeatProductsContext> contexts = new ArrayList<SeatProductsContext>();
    contexts.add(context);


    String successStr = "{\"error\":\"Error\", \"verificationPassed\":false}";
    InputStream is = new ByteArrayInputStream(successStr.getBytes());
    Response response = Response.status(Status.OK).entity(is).build();
    when(webClient.post(anyObject())).thenReturn(response);

    String errorMsg = primaryIntegrationUtil.verifySthInventory(contexts);
    Assert.assertEquals("Error", errorMsg);
  }

  @Test
  public void testVerifySthInventoryUnknownError() throws Exception {
    SeatProduct sp = new SeatProduct();
    sp.setFulfillmentArtifact("12345678");
    SeatProductsContext context = new SeatProductsContext(new Listing(), null, null, null);
    context.addArtifactSeatProductToList(sp);

    List<SeatProductsContext> contexts = new ArrayList<SeatProductsContext>();
    contexts.add(context);


    String successStr = "{\"verificationPassed\":false}";
    InputStream is = new ByteArrayInputStream(successStr.getBytes());
    Response response = Response.status(Status.OK).entity(is).build();
    when(webClient.post(anyObject())).thenReturn(response);

    String errorMsg = primaryIntegrationUtil.verifySthInventory(contexts);
    Assert.assertEquals("unknownError", errorMsg);
  }

  @Test
  public void testVerifyAndPersistBarcodes() {
    setupVerifyAndPersist();
    InputStream is = new ByteArrayInputStream("{}".getBytes());
    Response response = Response.status(Status.OK).entity(is).build();
    when(webClient.post(anyObject())).thenReturn(response);

    ErrorDetail errorDetail = primaryIntegrationUtil.verifyAndPersistBarcodes(listing, seats,true);
    assertNull(errorDetail);
  }
  
  

  @Test
  public void testVerifyAndPersistBarcodes2() {
    setupVerifyAndPersist();
    listing.setSection("testSection");
    seats.get(0).setProductType(ProductType.TICKET);
    InputStream is = new ByteArrayInputStream("{}".getBytes());
    Response response = Response.status(Status.OK).entity(is).build();
    when(webClient.post(anyObject())).thenReturn(response);

    ErrorDetail errorDetail = primaryIntegrationUtil.verifyAndPersistBarcodes(listing, seats,false);
    assertNull(errorDetail);
  }

  @Test
  public void testVerifyAndPersistBarcodesBadRequestSystemError() {
    setupVerifyAndPersist();
    Response response = Response.status(Status.INTERNAL_SERVER_ERROR).build();
    when(webClient.post(anyObject())).thenReturn(response);

    ErrorDetail errorDetail = primaryIntegrationUtil.verifyAndPersistBarcodes(listing, seats,true);
    assertNotNull(errorDetail);
    assertEquals(errorDetail.getErrorCode(), ErrorCode.SYSTEM_ERROR);
  }

  @Test
  public void testVerifyAndPersistBarcodesBadRequestSkipSecondaryIntegration() {
    setupVerifyAndPersist();

    primaryIntegrationUtil.verifyAndPersistBarcodes(listing, seats,false);
    verify(webClient, never()).get();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testVerifyAndPersistBarcodesSystemError() {
    setupVerifyAndPersist();
    when(webClient.post(anyObject())).thenThrow(Exception.class);
    primaryIntegrationUtil.verifyAndPersistBarcodes(listing, seats,true);
    ErrorDetail errorDetail = primaryIntegrationUtil.verifyAndPersistBarcodes(listing, seats,true);
    assertNotNull(errorDetail);
    assertEquals(errorDetail.getErrorCode(), ErrorCode.SYSTEM_ERROR);
  }

    @Test
    public void testBuyerRestrictTraitIsAdded() {
        setupVerifyAndPersist();
        String jsonSrsResponse = buildSRSJsonResponseWithBuyerRestrictedTicket();
        InputStream is = new ByteArrayInputStream(jsonSrsResponse.getBytes());
        Response response = Response.status(Status.OK).entity(is).build();
        when(webClient.post(anyObject())).thenReturn(response);

        primaryIntegrationUtil.verifyAndPersistBarcodes(listing, seats,false);

        verify(ticketSeatHelper, times(1)).makeListingSeatTrait(listing.getId(), PrimaryIntegrationUtil.BUYER_RESTRICTED_SEAT_TRAIT_ID, CommonConstants.LISTING_API_V2, CommonConstants.LISTING_API_V2);
    }

    private String buildSRSJsonResponseWithBuyerRestrictedTicket() {
        GetSRSForBarcodesResponse getSRSForBarcodesResponse = new GetSRSForBarcodesResponse();
        List<Ticket> tickets = new ArrayList<>();
        Ticket ticket = new Ticket();
        ticket.setBuyerRestricted(true);
        tickets.add(ticket);
        getSRSForBarcodesResponse.setTickets(tickets);
        String body = "{}";
        try {
            body = new ObjectMapper().writeValueAsString(getSRSForBarcodesResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return body;
    }

  private void setupVerifyAndPersist() {
    listing = new Listing();
    listing.setId(12345L);
    listing.setSection("General Admission");
    listing.setRow("testRow");
    listing.setFaceValue(new Money("123"));

    seats = new ArrayList<SeatProduct>();
    SeatProduct barcodeSeat = new SeatProduct();
    barcodeSeat.setSeatId(123L);
    barcodeSeat.setSeat("testSeat");
    barcodeSeat.setRow("testRow");
    barcodeSeat.setFulfillmentArtifact("testBarcode");
    barcodeSeat.setProductType(ProductType.PARKING_PASS);
    barcodeSeat.setFaceValue(new Money("123"));

    seats.add(barcodeSeat);
  }
}
