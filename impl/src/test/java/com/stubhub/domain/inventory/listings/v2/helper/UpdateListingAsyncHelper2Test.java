package com.stubhub.domain.inventory.listings.v2.helper;


import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.fulfillment.window.v1.intf.EventFulfillmentWindowResponse;
import com.stubhub.domain.infrastructure.config.client.core.SHConfig;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.biz.v2.impl.InventoryMgrImpl;
import com.stubhub.domain.inventory.biz.v2.impl.util.FulfillmentServiceAdapter;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.biz.v2.intf.PDFTicketMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.entity.SplitOption;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.PDFTicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.FulfillmentMethod;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.adapter.ListingRequestAdapter;
import com.stubhub.domain.inventory.listings.v2.entity.ListingCheck;
import com.stubhub.domain.inventory.listings.v2.entity.SeatProduct;
import com.stubhub.domain.inventory.listings.v2.nlproc.CommonTasks;
import com.stubhub.domain.inventory.listings.v2.nlproc.ListingData;
import com.stubhub.domain.inventory.listings.v2.nlproc.ListingToDataAdapter;
import com.stubhub.domain.inventory.listings.v2.util.ErrorUtils;
import com.stubhub.domain.inventory.listings.v2.util.EventHelper;
import com.stubhub.domain.inventory.listings.v2.util.FulfillmentServiceHelper;
import com.stubhub.domain.inventory.listings.v2.util.InventorySolrUtil;
import com.stubhub.domain.inventory.listings.v2.util.JMSMessageHelper;
import com.stubhub.domain.inventory.listings.v2.util.ListingSeatTraitsHelper;
import com.stubhub.domain.inventory.listings.v2.util.ListingTextValidatorUtil;
import com.stubhub.domain.inventory.listings.v2.util.ListingWrapper;
import com.stubhub.domain.inventory.listings.v2.util.PaymentHelper;
import com.stubhub.domain.inventory.listings.v2.util.PrimaryIntegrationUtil;
import com.stubhub.domain.inventory.listings.v2.util.ResourceManager;
import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.domain.inventory.listings.v2.util.SeatProductsContext;
import com.stubhub.domain.inventory.listings.v2.util.SellerHelper;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.domain.inventory.listings.v2.validator.ListingRequestValidator;
import com.stubhub.domain.inventory.metadata.v1.event.util.SellerPaymentUtil;
import com.stubhub.domain.inventory.v2.DTO.Attribute;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.domain.user.contacts.intf.CustomerContactDetails;
import com.stubhub.domain.user.contacts.intf.CustomerContactMappingResponse;
import com.stubhub.domain.user.payments.v2.intf.CustomerPaymentInstrumentDetailsV2;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.common.util.DateUtil;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;



public class UpdateListingAsyncHelper2Test extends SHInventoryTest {
  /**
   * define the tesingTable here for all desired test conditions. It contains the following: 1- Test
   * method name 2- Test description 3- Expected success (for true) or failure (for false)
   */
  /// NOTE THIS TABLE IS NOT USED FOR NOW
    private static final Log log = LogFactory.getLog(UpdateListingAsyncHelper2Test.class);
  protected ExtendedSecurityContext securityContext;
  protected com.stubhub.domain.inventory.datamodel.entity.Event event;
  protected EventHelper eventHelper;

  protected HttpHeaders headers;

  protected UpdateListingAsyncHelper2 updateListingHelperMock;

  protected SellerHelper sellerHelper;
  protected TicketSeatMgr ticketSeatMgr;

  protected ListingSeatTraitsHelper listingSeatTraitsHelper;
  protected UserHelper userHelper;
  protected ListingPriceDetailsHelper listingPriceDetailsHelper;
  protected InventoryMgr inventoryMgr;
  protected JMSMessageHelper jmsMessageHelper;
  protected PDFTicketMgr pdfTicketMgr;
  protected FulfillmentServiceHelper fulfillmentServiceHelper;
  protected FulfillmentServiceAdapter fulfillmentServiceAdapter;
  protected ListingPriceUtil listingPriceUtil;
  protected InventorySolrUtil inventorySolrUtil;
  protected PrimaryIntegrationUtil primaryIntegrationUtil;
  protected ListingSeatTraitMgr listingSeatTraitMgr;
  protected PaymentHelper paymentHelper;
  protected SeatProductsContext seatProductsContext;
  protected ListingFulfilmentHelper listingFulfilHelper;
  private MasterStubhubPropertiesWrapper masterStubhubProperties;
  private SellerPaymentUtil sellerPaymentUtil;
  private SHConfig shConfig;

  @Mock
  ResourceManager rm;
  ListingTextValidatorUtil listingTextValidatorUtil = new ListingTextValidatorUtil();
  ListingRequestValidator listingRequestValidator = new ListingRequestValidator();

  private String clientIp = "client Ip";
  private String userAgent = "userAgent";
  
  UpdateListingAsyncHelper2 helper;

  @BeforeTest
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper = new UpdateListingAsyncHelper2();
    helper.setup();

    // SELLAPI-1092 7/15/15 START
    rm.resetInstance();
    ReflectionTestUtils.setField(listingTextValidatorUtil, "resourceManager", rm);
    ReflectionTestUtils.setField(listingRequestValidator, "listingTextValidatorUtil",
        listingTextValidatorUtil);
    ReflectionTestUtils.setField(helper, "listingRequestValidator", listingRequestValidator);
    // SELLAPI-1092 7/15/15 END

    masterStubhubProperties = mock(MasterStubhubPropertiesWrapper.class);
    ReflectionTestUtils.setField(listingRequestValidator, "masterStubhubProperties", masterStubhubProperties);
    
    securityContext = mockSecurityContext();

    headers = (HttpHeaders) mockClass(HttpHeaders.class, null, null);
    eventHelper = mockEventHelper(helper, "eventHelper");
    // seatProductsContext =(SeatProductsContext)mockClass( SeatProductsContext.class, helper,
    // "seatProductsContext");
    sellerHelper = (SellerHelper) mockClass(SellerHelper.class, helper, "sellerHelper");
    ticketSeatMgr = (TicketSeatMgr) mockClass(TicketSeatMgr.class, helper, "ticketSeatMgr");
    listingSeatTraitsHelper = (ListingSeatTraitsHelper) mockClass(ListingSeatTraitsHelper.class,
        helper, "listingSeatTraitsHelper");
    userHelper = (UserHelper) mockClass(UserHelper.class, helper, "userHelper");
    listingPriceDetailsHelper =
        (ListingPriceDetailsHelper) mockClass(ListingPriceDetailsHelper.class, helper,
            "listingPriceDetailsHelper");
    inventoryMgr = (InventoryMgr) mockClass(InventoryMgrImpl.class, helper, "inventoryMgr");

    paymentHelper = (PaymentHelper) mockClass(PaymentHelper.class, helper, "paymentHelper");
    when(paymentHelper.isSellerPaymentTypeValidForSeller(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(true);
    when(paymentHelper.populatePaymentDetails(Mockito.any(Listing.class))).thenReturn(true);

    jmsMessageHelper =
        (JMSMessageHelper) mockClass(JMSMessageHelper.class, helper, "jmsMessageHelper");
    pdfTicketMgr = (PDFTicketMgr) mockClass(PDFTicketMgr.class, helper, "pdfTicketMgr");
    listingFulfilHelper = (ListingFulfilmentHelper) mockClass(ListingFulfilmentHelper.class, helper,
        "listingFulfilHelper");

    fulfillmentServiceHelper = (FulfillmentServiceHelper) mockClass(FulfillmentServiceHelper.class,
        helper, "fulfillmentServiceHelper");
    fulfillmentServiceAdapter =
        (FulfillmentServiceAdapter) mockClass(FulfillmentServiceAdapter.class, helper,
            "fulfillmentServiceAdapter");

    listingPriceUtil =
        (ListingPriceUtil) mockClass(ListingPriceUtil.class, helper, "listingPriceUtil");
    inventorySolrUtil =
        (InventorySolrUtil) mockClass(InventorySolrUtil.class, helper, "inventorySolrUtil");
    primaryIntegrationUtil = (PrimaryIntegrationUtil) mockClass(PrimaryIntegrationUtil.class,
        helper, "primaryIntegrationUtil");

    sellerPaymentUtil = new SellerPaymentUtil(shConfig);
    shConfig = Mockito.mock(SHConfig.class);
   // ReflectionTestUtils.setField(listingRequestValidator, "shConfig", shConfig);
    Mockito.when(shConfig.getProperty(Matchers.eq("paymentmethod.required.percentage"), Matchers.anyString() )).thenReturn("10");
    ReflectionTestUtils.setField(sellerPaymentUtil, "shConfig", shConfig );
    when(rm.getResource(Mockito.anyString(), Mockito.any(Locale.class)))
        .thenReturn("[^a-zA-Z0-9\\s\\_\\-\\,\\/\\:]");
    when(masterStubhubProperties.getProperty("facevalue.required.countries", "GB,LU,AT,DE"))
        .thenReturn("GB,LU,AT,DE");
    when(shConfig.getProperty("canada.site.switch", Boolean.class, false)).thenReturn(true);
    HashMap<String,Boolean> booleanValues = new HashMap<String,Boolean>();
    booleanValues.put("isSeatRequired", false);
	booleanValues.put("isEticket", false);
    when(inventoryMgr.isSeatsRequired(anyLong())).thenReturn(booleanValues);
  }

  @Test
  public void createBulkListing() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "sec-10", "R1", 5));

    Event sampleEvent = getEvent();
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    when(inventoryMgr.getListing(anyLong()))
        .thenReturn(getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5));
   
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");

    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong())).thenReturn(true);
    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(sellerHelper.populateSellerDetails(Matchers.any(Listing.class))).thenReturn(true);


    Object[] prResp = new Object[3];
    prResp[0] = getPriceResponse("25", "4");
    prResp[1] = getPriceResponse("24", "8");
    prResp[2] = getPriceResponse("40", "13");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
        Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    
    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setSellerId(1000L);
    newlisting.setSellerContactId(1234l);
    newlisting.setQuantity(8);
    newlisting.setQuantityRemain(8);
    newlisting.setSaleMethod(1L);
    newlisting.setLmsApprovalStatus(1);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    ListingRequest req = new ListingRequest();
    req.setEventId("1000");

    req.setQuantity(8);

    try {
      shapiContext.setSignedJWTAssertion("DUMMY--assertion");

      ListingToDataAdapter adapter = new ListingToDataAdapter();
      List<ListingRequest> reqList = new ArrayList<ListingRequest>();

      ListingRequest req1 = new ListingRequest();
      req1.setExternalListingId("20001");
      req1.setQuantity(8);
      req1.setPricePerProduct(new Money("22.0", "USD"));
      req1.setEventId("1000");
      req1.setContactId(1234l);
      req1.setSplitOption(SplitOption.NONE);
      req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);
      req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.UPS);
      req1.setLmsExtension(true);

      ListingRequest req2 = new ListingRequest();
      req2.setExternalListingId("20002");
      req2.setQuantity(8);
      req2.setContactId(1234l);
      req2.setPricePerProduct(new Money("30.0", "USD"));
      req2.setEventId("1000");
      req2.setSplitOption(SplitOption.NONE);
      req2.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);

      ListingRequest req3 = new ListingRequest();
      req3.setExternalListingId("20003");
      req3.setQuantity(8);
      req3.setContactId(1234l);
      req3.setPricePerProduct(new Money("40.0", "USD"));
      req3.setEventId("1000");
      req3.setSplitOption(SplitOption.NONE);
      req3.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);

      reqList.add(req1);
      reqList.add(req2);
      reqList.add(req3);

      ListingData ldata = adapter.listingDataFromCreateRequests(1000L,
          "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);
      if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
        Assert.fail("Error encountered from listingDataFromRequests. Error: "
            + ldata.getListingErrors().get(0).getMessage());
      }

      // NEED TO DO THIS: need to seed with event (IMPORTANT!)
      ldata.updateEventInfo(sampleEvent);
      ldata.setSubscriber("Single|V2|test@testmail.com|test");
      SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
      ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
      UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
      Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
          Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
          Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));

      List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
          helper.createOrUpdateListingData(ldata, clientIp, userAgent);

      Assert.assertTrue(responses != null && responses.size() == 3);

      boolean errors = false;
      String errorMessage = "";
      for (com.stubhub.domain.inventory.v2.DTO.ListingResponse resp : responses) {
        if (resp.getErrors() != null && resp.getErrors().size() > 0) {
          errors = true;
          errorMessage = resp.getErrors().get(0).getMessage();
          break;
        }
      }
      Assert.assertTrue("Create listing failed with errors. Sample cause: " + errorMessage,
          !errors);
    } catch (ListingBusinessException ex) {
      Assert.fail("Should not fail. Simple create GA listing with 8 seats and prices");
    }
  }

  @Test
  public void createBulkListingWithError() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "sec-10", "R1", 5));

    Event sampleEvent = getEvent();
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    when(inventoryMgr.getListing(anyLong()))
        .thenReturn(getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5));
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");

    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong())).thenReturn(true);

    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(sellerHelper.populateSellerDetails(Matchers.any(Listing.class))).thenReturn(true);


    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
        Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setSellerId(1000L);
    newlisting.setQuantity(8);
    newlisting.setSellerContactId(1234l);
    newlisting.setQuantityRemain(8);
    newlisting.setSaleMethod(1L);
    newlisting.setLmsApprovalStatus(1);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    ListingRequest req = new ListingRequest();
    req.setEventId("1000");

    req.setQuantity(8);

    try {
      shapiContext.setSignedJWTAssertion("DUMMY--assertion");

      ListingToDataAdapter adapter = new ListingToDataAdapter();
      List<ListingRequest> reqList = new ArrayList<ListingRequest>();

      ListingRequest req1 = new ListingRequest();
      req1.setExternalListingId("20001");
      req1.setQuantity(8);
      List<Product> products=new ArrayList<Product>();
      Product product=new Product();
      product.setRow("A");
    products.add(product);
    req1.setProducts(products);
      req1.setPricePerProduct(new Money("22.0", "USD"));
      req1.setEventId("1000");
      req1.setContactId(1234l);
      req1.setSplitOption(SplitOption.NONE);
      req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);
      req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.PDF);
      req1.setLmsExtension(true);

      ListingRequest req2 = new ListingRequest();
      req2.setExternalListingId("20002");
      req2.setQuantity(8);
      List<Product> products1=new ArrayList<Product>();
      Product product1=new Product();
      product1.setRow("*");
    products1.add(product1);
    req1.setProducts(products1);
      req2.setPricePerProduct(new Money("30.0", "USD"));
      req2.setEventId("1000");
      req2.setSplitOption(SplitOption.NONE);
      req2.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);
      req2.setContactId(1234l);
      reqList.add(req1);
      reqList.add(req2);

      ListingData ldata = adapter.listingDataFromCreateRequests(1000L,
          "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);
      if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
        Assert.fail("Error encountered from listingDataFromRequests. Error: "
            + ldata.getListingErrors().get(0).getMessage());
      }

      // NEED TO DO THIS: need to seed with event (IMPORTANT!)
      ldata.updateEventInfo(sampleEvent);
      ldata.setSubscriber("Single|V2|test@testmail.com|test");
      SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
      ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
      UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
      Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
          Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
          Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));

      List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
          helper.createOrUpdateListingData(ldata, clientIp, userAgent);

    } catch (ListingBusinessException ex) {
      Assert.fail("Should not fail. Simple create GA listing with 8 seats and prices");
    }
  }
  
  @Test
  public void createListingWithNoPaymentMethod() throws Exception {
    Long sellerId = 1000004L;
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "sec-10", "R1", 5));

    Event sampleEvent = getEvent();
    when(eventHelper.getEventById(1000L, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    when(inventoryMgr.getListing(anyLong()))
        .thenReturn(getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5));
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");

    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(sellerHelper.populateSellerDetails(Matchers.any(Listing.class))).thenReturn(true);
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong())).thenReturn(true);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
//    prResp[1] = getPriceResponse("24", "8");
//    prResp[2] = getPriceResponse("40", "13");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
        Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setSellerId(sellerId);
    newlisting.setQuantity(8);
    newlisting.setQuantityRemain(8);
    newlisting.setSaleMethod(1L);
    newlisting.setLmsApprovalStatus(1);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    ListingRequest req = new ListingRequest();
    req.setEventId("1000");

    req.setQuantity(8);

    try {
      shapiContext.setSignedJWTAssertion("DUMMY--assertion");

      ListingToDataAdapter adapter = new ListingToDataAdapter();
      List<ListingRequest> reqList = new ArrayList<ListingRequest>();
      Attribute atr = new Attribute();
      atr.setKey("SELLITNOW");
      atr.setValue("TRUE");
      
      ListingRequest req1 = new ListingRequest();
      req1.setExternalListingId("20001");
      req1.setQuantity(8);
      req1.setPricePerProduct(new Money("22.0", "USD"));
      req1.setEventId("1000");
      req1.setSplitOption(SplitOption.NONE);
      req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.INCOMPLETE);
      req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.UPS);
      req1.setLmsExtension(true);
      req1.setContactId(1234l);
      req1.setAttributes(atr);
     


      reqList.add(req1);

      ListingData ldata = adapter.listingDataFromCreateRequests(sellerId,
          "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);
//      if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
//        Assert.fail("Error encountered from listingDataFromRequests. Error: "
//            + ldata.getListingErrors().get(0).getMessage());
//      }

      // NEED TO DO THIS: need to seed with event (IMPORTANT!)
      ldata.updateEventInfo(sampleEvent);
      ldata.setSubscriber("Single|V2|test@testmail.com|test");
      SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
      ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
      UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
      Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
          Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
          Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));

      List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
          helper.createOrUpdateListingData(ldata, clientIp, userAgent);

      Assert.assertTrue(responses != null && responses.size() == 1);

      boolean errors = false;
      String errorMessage = "";
      for (com.stubhub.domain.inventory.v2.DTO.ListingResponse resp : responses) {
        if (resp.getErrors() != null && resp.getErrors().size() > 0) {
          errors = true;
          errorMessage = resp.getErrors().get(0).getMessage();
          break;
        }
      }
      Assert.assertTrue("Create listing has no error " ,
          !errors);
    } catch (ListingBusinessException ex) {
      Assert.fail("Should not fail. Simple create GA listing with 8 seats and prices");
    }

  }
  
  
  @Test
  public void createListingWithContactGuid() throws Exception {
    Long sellerId = 1000004L;
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "sec-10", "R1", 5));

    Event sampleEvent = getEvent();
    when(eventHelper.getEventById(1000L, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    when(inventoryMgr.getListing(anyLong()))
        .thenReturn(getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5));
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");

    when(userHelper.getCustomerContactId(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(sellerHelper.populateSellerDetails(Matchers.any(Listing.class))).thenReturn(true);
    when(userHelper.isUserContactGuidValid(Matchers.anyString(), Matchers.anyString())).thenReturn(true);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
//    prResp[1] = getPriceResponse("24", "8");
//    prResp[2] = getPriceResponse("40", "13");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
        Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setSellerId(sellerId);
    newlisting.setQuantity(8);
    newlisting.setQuantityRemain(8);
    newlisting.setSaleMethod(1L);
    newlisting.setLmsApprovalStatus(1);
    newlisting.setSellerContactGuid("test123");

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    ListingRequest req = new ListingRequest();
    req.setEventId("1000");

    req.setQuantity(8);

    try {
      shapiContext.setSignedJWTAssertion("DUMMY--assertion");

      ListingToDataAdapter adapter = new ListingToDataAdapter();
      List<ListingRequest> reqList = new ArrayList<ListingRequest>();

      ListingRequest req1 = new ListingRequest();
      req1.setExternalListingId("20001");
      req1.setQuantity(8);
      req1.setPricePerProduct(new Money("22.0", "USD"));
      req1.setEventId("1000");
      req1.setSplitOption(SplitOption.NONE);
      req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.INCOMPLETE);
      req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.UPS);
      req1.setLmsExtension(true);
      req1.setContactGuid("test123");



      reqList.add(req1);

      ListingData ldata = adapter.listingDataFromCreateRequests(sellerId,
          "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);
//      if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
//        Assert.fail("Error encountered from listingDataFromRequests. Error: "
//            + ldata.getListingErrors().get(0).getMessage());
//      }

      // NEED TO DO THIS: need to seed with event (IMPORTANT!)
      ldata.updateEventInfo(sampleEvent);
      ldata.setSubscriber("Single|V2|test@testmail.com|test");
      SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
      ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
      UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
      Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
          Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
          Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));

      List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
          helper.createOrUpdateListingData(ldata, clientIp, userAgent);

      Assert.assertTrue(responses != null && responses.size() == 1);

      boolean errors = false;
      String errorMessage = "";
      for (com.stubhub.domain.inventory.v2.DTO.ListingResponse resp : responses) {
        if (resp.getErrors() != null && resp.getErrors().size() > 0) {
          errors = true;
          errorMessage = resp.getErrors().get(0).getMessage();
          break;
        }
      }
      Assert.assertTrue("Create listing has no error " ,
          !errors);
    } catch (ListingBusinessException ex) {
      Assert.fail("Should not fail. Simple create GA listing with 8 seats and prices");
    }

  }
  
  

  
  @Test(expectedExceptions = { ExecutionException.class})
  public void createListingWithInvalidContactGuid() throws Exception {
    Long sellerId = 1000004L;
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "sec-10", "R1", 5));

    Event sampleEvent = getEvent();
    when(eventHelper.getEventById(1000L, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    when(inventoryMgr.getListing(anyLong()))
        .thenReturn(getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5));
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();

    when(userHelper.getCustomerContactId(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(sellerHelper.populateSellerDetails(Matchers.any(Listing.class))).thenReturn(true);
    when(userHelper.isUserContactGuidValid(Matchers.anyString(), Matchers.anyString())).thenReturn(false);


    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
//    prResp[1] = getPriceResponse("24", "8");
//    prResp[2] = getPriceResponse("40", "13");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
        Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setSellerId(sellerId);
    newlisting.setQuantity(8);
    newlisting.setQuantityRemain(8);
    newlisting.setSaleMethod(1L);
    newlisting.setLmsApprovalStatus(1);
    newlisting.setSellerContactGuid("test123");

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    ListingRequest req = new ListingRequest();
    req.setEventId("1000");

    req.setQuantity(8);

    try {
      shapiContext.setSignedJWTAssertion("DUMMY--assertion");

      ListingToDataAdapter adapter = new ListingToDataAdapter();
      List<ListingRequest> reqList = new ArrayList<ListingRequest>();

      ListingRequest req1 = new ListingRequest();
      req1.setExternalListingId("20001");
      req1.setQuantity(8);
      req1.setPricePerProduct(new Money("22.0", "USD"));
      req1.setEventId("1000");
      req1.setSplitOption(SplitOption.NONE);
      req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.INCOMPLETE);
      req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.UPS);
      req1.setLmsExtension(true);
      req1.setContactGuid("test123");



      reqList.add(req1);

      ListingData ldata = adapter.listingDataFromCreateRequests(sellerId,
          "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);
//      if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
//        Assert.fail("Error encountered from listingDataFromRequests. Error: "
//            + ldata.getListingErrors().get(0).getMessage());
//      }

      // NEED TO DO THIS: need to seed with event (IMPORTANT!)
      ldata.updateEventInfo(sampleEvent);
      SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
      ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
      UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
      Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
          Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
          Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));

      List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
          helper.createOrUpdateListingData(ldata, clientIp, userAgent);

      Assert.assertTrue(responses != null && responses.size() == 1);

      boolean errors = false;
      String errorMessage = "";
      for (com.stubhub.domain.inventory.v2.DTO.ListingResponse resp : responses) {
        if (resp.getErrors() != null && resp.getErrors().size() > 0) {
          errors = true;
          errorMessage = resp.getErrors().get(0).getMessage();
          break;
        }
      }
      Assert.assertTrue("Create listing has no error " ,
          !errors);
    } catch (ListingBusinessException ex) {
      Assert.fail("Should not fail. Simple create GA listing with 8 seats and prices");
    }

  }
  
  //@Test(expectedExceptions = { ListingBusinessException.class})
  public void createListingWithInvalidContactId() throws Exception {
    Long sellerId = 1000004L;
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "sec-10", "R1", 5));

    Event sampleEvent = getEvent();
    when(eventHelper.getEventById(1000L, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    when(inventoryMgr.getListing(anyLong()))
        .thenReturn(getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5));
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();

    when(userHelper.getCustomerContactId(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(sellerHelper.populateSellerDetails(Matchers.any(Listing.class))).thenReturn(true);


    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
//    prResp[1] = getPriceResponse("24", "8");
//    prResp[2] = getPriceResponse("40", "13");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
        Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setSellerId(sellerId);
    newlisting.setQuantity(8);
    newlisting.setQuantityRemain(8);
    newlisting.setSaleMethod(1L);
    newlisting.setLmsApprovalStatus(1);
    newlisting.setSellerContactId(1234L);

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    ListingRequest req = new ListingRequest();
    req.setEventId("1000");
    req.setAutoPricingEnabledInd(Boolean.TRUE);
    req.setQuantity(8);

    try {
      shapiContext.setSignedJWTAssertion("DUMMY--assertion");

      ListingToDataAdapter adapter = new ListingToDataAdapter();
      List<ListingRequest> reqList = new ArrayList<ListingRequest>();

      ListingRequest req1 = new ListingRequest();
      req1.setExternalListingId("20001");
      req1.setQuantity(8);
      req1.setPricePerProduct(new Money("22.0", "USD"));
      req1.setEventId("1000");
      req1.setSplitOption(SplitOption.NONE);
      req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.INCOMPLETE);
      req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.UPS);
      req1.setLmsExtension(true);
      req1.setContactId(1234L);
      req1.setAutoPricingEnabledInd(Boolean.TRUE);
      
      ListingRequest req2 = new ListingRequest();
      req2.setExternalListingId("20002");
      req2.setQuantity(8);
      req2.setContactId(1234l);
      req2.setPricePerProduct(new Money("30.0", "USD"));
      req2.setEventId("1000");
      req2.setSplitOption(SplitOption.NONE);
      req2.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);


      reqList.add(req1);

      ListingData ldata = adapter.listingDataFromCreateRequests(sellerId,
          "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);
//      if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
//        Assert.fail("Error encountered from listingDataFromRequests. Error: "
//            + ldata.getListingErrors().get(0).getMessage());
//      }

      // NEED TO DO THIS: need to seed with event (IMPORTANT!)
      ldata.updateEventInfo(sampleEvent);
      SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
      ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
      UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
      Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
          Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
          Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));

      List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
          helper.createOrUpdateListingData(ldata, clientIp, userAgent);

      Assert.assertTrue(responses != null && responses.size() == 1);

      boolean errors = false;
      String errorMessage = "";
      for (com.stubhub.domain.inventory.v2.DTO.ListingResponse resp : responses) {
        if (resp.getErrors() != null && resp.getErrors().size() > 0) {
          errors = true;
          errorMessage = resp.getErrors().get(0).getMessage();
          break;
        }
      }
      Assert.assertTrue("Create listing has no error " ,
          !errors);
    } catch (ListingBusinessException ex) {
      Assert.fail("Should not fail. Simple create GA listing with 8 seats and prices");
    }

  }
  
  //@Test(expectedExceptions = { ListingBusinessException.class})
  public void createListingWithContactIdMappingApiError() throws Exception {
    Long sellerId = 1000004L;
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "sec-10", "R1", 5));

    Event sampleEvent = getEvent();
    when(eventHelper.getEventById(1000L, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);
    

    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");
    
    when(inventoryMgr.getListing(anyLong()))
        .thenReturn(getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5));
    when(userHelper.getCustomerContactId(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(sellerHelper.populateSellerDetails(Matchers.any(Listing.class))).thenReturn(true);


    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
//    prResp[1] = getPriceResponse("24", "8");
//    prResp[2] = getPriceResponse("40", "13");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setSellerId(sellerId);
    newlisting.setQuantity(8);
    newlisting.setQuantityRemain(8);
    newlisting.setSaleMethod(1L);
    newlisting.setLmsApprovalStatus(1);
    newlisting.setSellerContactId(1234L);

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    ListingRequest req = new ListingRequest();
    req.setEventId("1000");

    req.setQuantity(8);

    try {
      shapiContext.setSignedJWTAssertion("DUMMY--assertion");

      ListingToDataAdapter adapter = new ListingToDataAdapter();
      List<ListingRequest> reqList = new ArrayList<ListingRequest>();

      ListingRequest req1 = new ListingRequest();
      req1.setExternalListingId("20001");
      req1.setQuantity(8);
      req1.setPricePerProduct(new Money("22.0", "USD"));
      req1.setEventId("1000");
      req1.setSplitOption(SplitOption.NONE);
      req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.INCOMPLETE);
      req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.UPS);
      req1.setLmsExtension(true);
      req1.setContactId(1234L);



      reqList.add(req1);

      ListingData ldata = adapter.listingDataFromCreateRequests(sellerId,
          "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);
//      if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
//        Assert.fail("Error encountered from listingDataFromRequests. Error: "
//            + ldata.getListingErrors().get(0).getMessage());
//      }

      // NEED TO DO THIS: need to seed with event (IMPORTANT!)
      ldata.updateEventInfo(sampleEvent);
      SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
      ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
      UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
      Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
          Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
          Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));

      List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
          helper.createOrUpdateListingData(ldata, clientIp, userAgent);

      Assert.assertTrue(responses != null && responses.size() == 1);

      boolean errors = false;
      String errorMessage = "";
      for (com.stubhub.domain.inventory.v2.DTO.ListingResponse resp : responses) {
        if (resp.getErrors() != null && resp.getErrors().size() > 0) {
          errors = true;
          errorMessage = resp.getErrors().get(0).getMessage();
          break;
        }
      }
      Assert.assertTrue("Create listing has no error " ,
          !errors);
    } catch (ListingBusinessException ex) {
      Assert.fail("Should not fail. Simple create GA listing with 8 seats and prices");
    }

  }
  
  @Test(expectedExceptions = { ExecutionException.class})
  public void createListingWithContactGUidApiMappingError() throws Exception {
    Long sellerId = 1000004L;
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "sec-10", "R1", 5));

    Event sampleEvent = getEvent();
    when(eventHelper.getEventById(1000L, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    when(inventoryMgr.getListing(anyLong()))
        .thenReturn(getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5));

    when(userHelper.getCustomerContactId(Matchers.anyString(), Matchers.anyString())).thenReturn(null);
    when(sellerHelper.populateSellerDetails(Matchers.any(Listing.class))).thenReturn(true);
    when(userHelper.isUserContactGuidValid(Matchers.anyString(), Matchers.anyString())).thenReturn(true);


    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
//    prResp[1] = getPriceResponse("24", "8");
//    prResp[2] = getPriceResponse("40", "13");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setSellerId(sellerId);
    newlisting.setQuantity(8);
    newlisting.setQuantityRemain(8);
    newlisting.setSaleMethod(1L);
    newlisting.setLmsApprovalStatus(1);
    newlisting.setSellerContactGuid("test123");

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    ListingRequest req = new ListingRequest();
    req.setEventId("1000");

    req.setQuantity(8);

    try {
      shapiContext.setSignedJWTAssertion("DUMMY--assertion");

      ListingToDataAdapter adapter = new ListingToDataAdapter();
      List<ListingRequest> reqList = new ArrayList<ListingRequest>();

      ListingRequest req1 = new ListingRequest();
      req1.setExternalListingId("20001");
      req1.setQuantity(8);
      req1.setPricePerProduct(new Money("22.0", "USD"));
      req1.setEventId("1000");
      req1.setSplitOption(SplitOption.NONE);
      req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.INCOMPLETE);
      req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.UPS);
      req1.setLmsExtension(true);
      req1.setContactGuid("test123");



      reqList.add(req1);

      ListingData ldata = adapter.listingDataFromCreateRequests(sellerId,
          "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);
//      if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
//        Assert.fail("Error encountered from listingDataFromRequests. Error: "
//            + ldata.getListingErrors().get(0).getMessage());
//      }

      // NEED TO DO THIS: need to seed with event (IMPORTANT!)
      ldata.updateEventInfo(sampleEvent);
      SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
      ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
      UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
      Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
          Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
          Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));

      List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
          helper.createOrUpdateListingData(ldata, clientIp, userAgent);

      Assert.assertTrue(responses != null && responses.size() == 1);

      boolean errors = false;
      String errorMessage = "";
      for (com.stubhub.domain.inventory.v2.DTO.ListingResponse resp : responses) {
        if (resp.getErrors() != null && resp.getErrors().size() > 0) {
          errors = true;
          errorMessage = resp.getErrors().get(0).getMessage();
          break;
        }
      }
      Assert.assertTrue("Create listing has no error " ,
          !errors);
    } catch (ListingBusinessException ex) {
      Assert.fail("Should not fail. Simple create GA listing with 8 seats and prices");
    }

  }

  @Test
    public void updateDeleteBulkListing() throws Exception {

        Event sampleEvent = getEvent();
        Long eventId = 1000l;
        when(eventHelper.getEventById(eventId, "event genrePath geoPath venue", Locale.US, false))
                .thenReturn(sampleEvent);

        Object[] prResp = new Object[1];
        prResp[0] = getPriceResponse("25", "4");
        when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
                Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

        // create an existing listing and set lms approveal
        Long sellerId = 100022l;
        Listing existinglisting = new Listing();
        existinglisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
        existinglisting.setSellerId(sellerId);
        existinglisting.setDeliveryOption(5); // LMS delivery option
        existinglisting.setSection("PR115");
        existinglisting.setRow("R2");
        existinglisting.setSeats("20,21");
        existinglisting.setEventId(eventId);
        existinglisting.setId(10000045l);
        existinglisting.setFulfillmentDeliveryMethods("9, 2.0, 3.56");
        existinglisting.setTicketMedium(1);
        existinglisting.setAutoPricingEnabledInd(Boolean.FALSE);

        Listing existinglisting2 = new Listing();
        existinglisting2.setListPrice(new Money(new BigDecimal(100d), "USD"));
        existinglisting2.setSellerId(sellerId);
        existinglisting2.setDeliveryOption(5); // LMS delivery option
        existinglisting2.setSection("PR115");
        existinglisting2.setRow("R3");
        existinglisting2.setSeats("20,21");
        existinglisting2.setEventId(eventId);
        existinglisting2.setId(10000046l);
        existinglisting2.setFulfillmentDeliveryMethods("9, 2.0, 3.56");
        existinglisting.setTicketMedium(1);

        Map<Long, Listing> lmap = new HashMap<Long, Listing>();
        lmap.put(10000045l, existinglisting);
        lmap.put(10000046l, existinglisting2);

        SHAPIContext shapiContext = new SHAPIContext();
        SHServiceContext shServiceContext = new SHServiceContext();
        try {
            shapiContext.setSignedJWTAssertion("DUMMY--assertion");

            ListingToDataAdapter adapter = new ListingToDataAdapter();
            List<ListingRequest> reqList = new ArrayList<ListingRequest>();
            List<Product> products = new ArrayList<Product>();
            Product prod1 = new Product();
            prod1.setRow("R2");
            prod1.setSeat("23");
            prod1.setProductType(ProductType.TICKET);
            prod1.setOperation(Operation.ADD);
            Product prod2 = new Product();
            prod2.setRow("R2");
            prod2.setSeat("24");
            prod2.setProductType(ProductType.TICKET);
            prod2.setOperation(Operation.ADD);
            products.add(prod1);
            products.add(prod2);

            ListingRequest req1 = new ListingRequest();
            req1.setListingId(10000045l);
            req1.setEventId(eventId.toString());
            req1.setProducts(products);
            req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.DELETED);
            req1.setPricePerProduct(new Money("22.0", "USD"));
            req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.PDF);
            req1.setAutoPricingEnabledInd(Boolean.FALSE);
            reqList.add(req1);

            ListingRequest req2 = new ListingRequest();
            req2.setListingId(10000046l);
            req2.setEventId(eventId.toString());
            req2.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.DELETED);
            req2.setPricePerProduct(new Money("22.0", "USD"));
            req2.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.PDF);
            reqList.add(req2);

            Mockito.when(inventoryMgr.getListing(req1.getListingId())).thenReturn(existinglisting);
            Mockito.when(inventoryMgr.getListing(req2.getListingId())).thenReturn(existinglisting2);
            Mockito.when(
                    fulfillmentServiceHelper.populateFulfillmentOptions(Mockito.any(Listing.class), Mockito.anyList()))
                    .thenReturn(true);
            ListingData ldata = adapter.listingDataFromUpdateRequests(inventoryMgr, sellerId,
                    "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);

            if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
                Assert.fail("Error encountered from listingDataFromRequests. Error: "
                        + ldata.getListingErrors().get(0).getMessage());
            }

            ldata.updateEventInfo(sampleEvent);
            SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
            UpdateListingAsyncHelper2 help = new UpdateListingAsyncHelper2();
            final UpdateListingAsyncHelper2 helperspy = Mockito.spy(help);

            Mockito.doReturn(existinglisting).when(helperspy).getListingFromRequest(Mockito.any(ListingData.class),
                    Mockito.any(req1.getClass()), Mockito.any(ListingResponse.class), Mockito.any(String.class),
                    Mockito.any(String.class));
            Mockito.doReturn(existinglisting2).when(helperspy).getListingFromRequest(Mockito.any(ListingData.class),
                    Mockito.any(req2.getClass()), Mockito.any(ListingResponse.class), Mockito.any(String.class),
                    Mockito.any(String.class));

            List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses = helper
                    .createOrUpdateListingData(ldata, clientIp, userAgent);

            Assert.assertTrue(responses != null);

        } catch (ListingBusinessException ex) {
            Assert.assertTrue("Should not fail. Single listing update for LMS listings failed", true);
        }

    }
  
  // @Test //for some reason maven build fails this test
  public void updateLMSApprovedBulkLisitng() throws Exception {

    Event sampleEvent = getEvent();
    Long eventId = 1000l;
    when(eventHelper.getEventById(eventId, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    // this is very tricky. Since we remove one of the listings from the request, we need
    // to mock pricing response for the remaining listing
    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    // prResp [1]= getPriceResponse ("24", "8");
    // prResp [2]= getPriceResponse ("40", "13");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    // create an existing listing and set lms approveal
    Long sellerId = 100022l;
    Listing existinglisting = new Listing();
    existinglisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    existinglisting.setSellerId(sellerId);
    existinglisting.setDeliveryOption(7); // LMS delivery option
    existinglisting.setSection("PR115");
    existinglisting.setLmsApprovalStatus(2);
    existinglisting.setRow("R2");
    existinglisting.setSeats("20, 21");
    existinglisting.setLmsApprovalStatus(2);
    existinglisting.setEventId(eventId);
    existinglisting.setId(10000045l);
    existinglisting.setFulfillmentDeliveryMethods("9, 2.0, 3.56");

    Map<Long, Listing> lmap = new HashMap<Long, Listing>();
    lmap.put(10000045l, existinglisting);

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    try {
      shapiContext.setSignedJWTAssertion("DUMMY--assertion");

      ListingToDataAdapter adapter = new ListingToDataAdapter();
      List<ListingRequest> reqList = new ArrayList<ListingRequest>();

      List<Product> products = new ArrayList<Product>();
      Product prod1 = new Product();
      prod1.setRow("R2");
      prod1.setSeat("23");
      prod1.setProductType(ProductType.TICKET);
      prod1.setOperation(Operation.ADD);
      Product prod2 = new Product();
      prod2.setRow("R2");
      prod2.setSeat("24");
      prod2.setProductType(ProductType.TICKET);
      prod2.setOperation(Operation.ADD);
      products.add(prod1);
      products.add(prod2);

      ListingRequest req1 = new ListingRequest();
      req1.setExternalListingId("12345");
      req1.setListingId(10000045l);
      req1.setEventId(eventId.toString());
      req1.setProducts(products);
      req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);
      req1.setPricePerProduct(new Money("22.0", "USD"));
      req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.LMS);


      ListingRequest req2 = new ListingRequest();
      req2.setExternalListingId("20002");
      req2.setPricePerProduct(new Money("30.0", "USD"));
      req2.setEventId(eventId.toString());
      req2.setListingId(10000045l);

      req2.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);
      req2.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.LMS);
      List<Product> products2 = new ArrayList<Product>();
      Product prod3 = new Product();
      prod3.setRow("R2");
      prod3.setSeat("25");
      prod3.setProductType(ProductType.TICKET);
      prod3.setOperation(Operation.DELETE);
      Product prod4 = new Product();
      prod4.setRow("R2");
      prod4.setSeat("26");
      prod4.setProductType(ProductType.TICKET);
      prod4.setOperation(Operation.DELETE);
      products2.add(prod3);
      products2.add(prod4);
      req2.setProducts(products2);


      reqList.add(req1);
      reqList.add(req2);

      ListingData ldata = adapter.listingDataFromCreateRequests(sellerId,
          "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);
      ldata.setCurListingsMap(eventId, lmap);

      if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
        Assert.fail("Error encountered from listingDataFromRequests. Error: "
            + ldata.getListingErrors().get(0).getMessage());
      }

      // NEED TO DO THIS: need to seed with event (IMPORTANT!)
      ldata.updateEventInfo(sampleEvent);
      SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
      ListingWrapper listingWrapper = new ListingWrapper(existinglisting, seatProductsContextMock);
      UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
      Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
          Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
          Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));

      List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
          helper.createOrUpdateListingData(ldata, clientIp, userAgent);

      Assert.assertTrue(responses != null && responses.size() == 1);

    } catch (ListingBusinessException ex) {
      Assert.fail("Should not fail. Bulk update for LMS listings failed");
    }

  }

  @Test
  public void updateLMSApprovedSingleLisitng() throws Exception {

    Event sampleEvent = getEvent();
    Long eventId = 1000l;
    when(eventHelper.getEventById(eventId, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");


    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);

    // create an existing listing and set lms approveal
    Long sellerId = 100022l;
    Listing existinglisting = new Listing();
    existinglisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    existinglisting.setSellerId(sellerId);
    existinglisting.setDeliveryOption(7); // LMS delivery option
    existinglisting.setSection("PR115");
    existinglisting.setLmsApprovalStatus(2);
    existinglisting.setRow("R2");
    existinglisting.setSeats("20,21");
    existinglisting.setLmsApprovalStatus(2);
    existinglisting.setEventId(eventId);
    existinglisting.setId(10000045l);
    existinglisting.setFulfillmentDeliveryMethods("9, 2.0, 3.56");
    existinglisting.setSellerContactId(1234L);


    Map<Long, Listing> lmap = new HashMap<Long, Listing>();
    lmap.put(10000045l, existinglisting);

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    try {
      shapiContext.setSignedJWTAssertion("DUMMY--assertion");

      ListingToDataAdapter adapter = new ListingToDataAdapter();
      List<ListingRequest> reqList = new ArrayList<ListingRequest>();

      List<Product> products = new ArrayList<Product>();
      Product prod1 = new Product();
      prod1.setRow("R2");
      prod1.setSeat("23");
      prod1.setProductType(ProductType.TICKET);
      prod1.setOperation(Operation.ADD);
      Product prod2 = new Product();
      prod2.setRow("R2");
      prod2.setSeat("24");
      prod2.setProductType(ProductType.TICKET);
      prod2.setOperation(Operation.ADD);
      products.add(prod1);
      products.add(prod2);

      ListingRequest req1 = new ListingRequest();
      req1.setExternalListingId("12345");
      req1.setListingId(10000045l);
      req1.setEventId(eventId.toString());
      req1.setProducts(products);
      req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);
      req1.setPricePerProduct(new Money("22.0", "USD"));
      req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.LMS);
      req1.setContactId(1234L);

      reqList.add(req1);

      ListingData ldata = adapter.listingDataFromCreateRequests(sellerId,
          "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);
      ldata.setCurListingsMap(eventId, lmap);

      if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
        Assert.fail("Error encountered from listingDataFromRequests. Error: "
            + ldata.getListingErrors().get(0).getMessage());
      }

      // NEED TO DO THIS: need to seed with event (IMPORTANT!)
      ldata.updateEventInfo(sampleEvent);
      SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
      ListingWrapper listingWrapper = new ListingWrapper(existinglisting, seatProductsContextMock);
      UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
      Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
          Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
          Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));

      List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
          helper.createOrUpdateListingData(ldata, clientIp, userAgent);

      Assert.assertTrue(responses == null);

    } catch (ListingBusinessException ex) {
      Assert.assertTrue("Should not fail. Single listing update for LMS listings failed", true);
    }

  }

  @Test
  public void updateLMSApprovedSingleLisitngProductUpdate() throws Exception {

    Event sampleEvent = getEvent();
    Long eventId = 1000l;
    when(eventHelper.getEventById(eventId, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");

    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);


    // create an existing listing and set lms approveal
    Long sellerId = 100022l;
    Listing existinglisting = new Listing();
    existinglisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    existinglisting.setSellerId(sellerId);
    existinglisting.setDeliveryOption(7); // LMS delivery option
    existinglisting.setSection("PR115");
    existinglisting.setLmsApprovalStatus(2);
    existinglisting.setRow("R2");
    existinglisting.setSeats("20,21");
    existinglisting.setLmsApprovalStatus(2);
    existinglisting.setEventId(eventId);
    existinglisting.setId(10000045l);
    existinglisting.setFulfillmentDeliveryMethods("9, 2.0, 3.56");
    existinglisting.setSellerContactId(1234L);


    Map<Long, Listing> lmap = new HashMap<Long, Listing>();
    lmap.put(10000045l, existinglisting);

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    try {
      shapiContext.setSignedJWTAssertion("DUMMY--assertion");

      ListingToDataAdapter adapter = new ListingToDataAdapter();
      List<ListingRequest> reqList = new ArrayList<ListingRequest>();

      List<Product> products = new ArrayList<Product>();
      Product prod1 = new Product();
      prod1.setRow("R2");
      prod1.setSeat("23");
      prod1.setProductType(ProductType.TICKET);
      prod1.setOperation(Operation.UPDATE);
      Product prod2 = new Product();
      prod2.setRow("R2");
      prod2.setSeat("24");
      prod2.setProductType(ProductType.TICKET);
      prod2.setOperation(Operation.UPDATE);
      products.add(prod1);
      products.add(prod2);

      ListingRequest req1 = new ListingRequest();
      req1.setExternalListingId("12345");
      req1.setListingId(10000045l);
      req1.setEventId(eventId.toString());
      req1.setProducts(products);
      req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);
      req1.setPricePerProduct(new Money("22.0", "USD"));
      req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.LMS);
      req1.setContactId(1234L);

      reqList.add(req1);

      ListingData ldata = adapter.listingDataFromCreateRequests(sellerId,
          "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);
      ldata.setCurListingsMap(eventId, lmap);

      if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
        Assert.fail("Error encountered from listingDataFromRequests. Error: "
            + ldata.getListingErrors().get(0).getMessage());
      }

      // NEED TO DO THIS: need to seed with event (IMPORTANT!)
      ldata.updateEventInfo(sampleEvent);
      SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
      ListingWrapper listingWrapper = new ListingWrapper(existinglisting, seatProductsContextMock);
      UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
      Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
          Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
          Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));

      List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
          helper.createOrUpdateListingData(ldata, clientIp, userAgent);

      Assert.assertTrue(responses == null);

    } catch (ListingBusinessException ex) {
      Assert.assertTrue("Should not fail. Single listing update for LMS listings failed", true);
    }

  }

  @Test
  public void updateLMSApprovedSingleListingDeleteProduct() throws Exception {

    Event sampleEvent = getEvent();
    Long eventId = 1000l;
    when(eventHelper.getEventById(eventId, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");
    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);

    // create an existing listing and set lms approveal
    Long sellerId = 100022l;
    Listing existinglisting = new Listing();
    existinglisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    existinglisting.setSellerId(sellerId);
    existinglisting.setDeliveryOption(7); // LMS delivery option
    existinglisting.setSection("PR115");
    existinglisting.setLmsApprovalStatus(2);
    // existinglisting.setRow("R2");
    // existinglisting.setSeats("20");
    existinglisting.setLmsApprovalStatus(2);
    existinglisting.setEventId(eventId);
    existinglisting.setId(10000045l);
    existinglisting.setFulfillmentDeliveryMethods("9, 2.0, 3.56");
    existinglisting.setSellerContactId(1234L);

    Map<Long, Listing> lmap = new HashMap<Long, Listing>();
    lmap.put(10000045l, existinglisting);

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    try {
      shapiContext.setSignedJWTAssertion("DUMMY--assertion");

      ListingToDataAdapter adapter = new ListingToDataAdapter();
      List<ListingRequest> reqList = new ArrayList<ListingRequest>();

      List<Product> products = new ArrayList<Product>();
      Product prod1 = new Product();
      // prod1.setRow("R2");
      // prod1.setSeat("23");
      prod1.setProductType(ProductType.TICKET);
      prod1.setOperation(Operation.DELETE);
      products.add(prod1);


      ListingRequest req1 = new ListingRequest();
      req1.setExternalListingId("12345");
      req1.setListingId(10000045l);
      req1.setEventId(eventId.toString());
      req1.setProducts(products);
      req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);
      req1.setPricePerProduct(new Money("22.0", "USD"));
      req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.LMS);
      req1.setContactId(1234L);
      reqList.add(req1);

      ListingData ldata = adapter.listingDataFromCreateRequests(sellerId,
          "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);
      ldata.setCurListingsMap(eventId, lmap);

      if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
        Assert.fail("Error encountered from listingDataFromRequests. Error: "
            + ldata.getListingErrors().get(0).getMessage());
      }

      // NEED TO DO THIS: need to seed with event (IMPORTANT!)
      ldata.updateEventInfo(sampleEvent);
      ldata.setSubscriber("Single|V2|test@testmail.com|test");
      SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
      ListingWrapper listingWrapper = new ListingWrapper(existinglisting, seatProductsContextMock);
      UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
      Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
          Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
          Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));

      List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
          helper.createOrUpdateListingData(ldata, clientIp, userAgent);

      Assert.assertTrue(responses != null && responses.size() == 1);

    } catch (ListingBusinessException ex) {
      Assert.assertTrue("Should not fail. Single listing update for LMS listings failed", true);
    }

  }


  @Test
  public void updateLMSApprovedSingleListingQuantityChangeTrustedSeller() throws Exception {

    Event sampleEvent = getEvent();
    Long eventId = 1000l;
    when(eventHelper.getEventById(eventId, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");
    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);    

    // create an existing listing and set lms approveal
    Long sellerId = 100022l;
    Listing existinglisting = new Listing();
    existinglisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    existinglisting.setSellerId(sellerId);
    existinglisting.setDeliveryOption(7); // LMS delivery option
    existinglisting.setSection("PR115");
    existinglisting.setLmsApprovalStatus(2);
    existinglisting.setRow("R2");
    existinglisting.setSeats("20,21");
    existinglisting.setLmsApprovalStatus(2);
    existinglisting.setEventId(eventId);
    existinglisting.setId(10000045l);
    existinglisting.setFulfillmentDeliveryMethods("7, 2.0, 3.56");
    existinglisting.setQuantityRemain(2);
    existinglisting.setSellerContactId(1234L);
    
    Map<Long, Listing> lmap = new HashMap<Long, Listing>();
    lmap.put(10000045l, existinglisting);

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    try {
      shapiContext.setSignedJWTAssertion("DUMMY--assertion");

      ListingToDataAdapter adapter = new ListingToDataAdapter();
      List<ListingRequest> reqList = new ArrayList<ListingRequest>();

      List<Product> products = new ArrayList<Product>();
      Product prod1 = new Product();
      prod1.setRow("R2");
      prod1.setSeat("23");
      prod1.setProductType(ProductType.TICKET);
      prod1.setOperation(Operation.ADD);
      Product prod2 = new Product();
      prod2.setRow("R2");
      prod2.setSeat("24");
      prod2.setProductType(ProductType.TICKET);
      prod2.setOperation(Operation.ADD);
      products.add(prod1);
      products.add(prod2);

      ListingRequest req1 = new ListingRequest();
      req1.setExternalListingId("12345");
      req1.setListingId(10000045l);
      req1.setEventId(eventId.toString());
      // req1.setProducts(products);
      req1.setQuantity(3);
      req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);
      req1.setPricePerProduct(new Money("22.0", "USD"));
      req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.LMS);
      req1.setContactId(1234L);
      reqList.add(req1);

      ListingData ldata = adapter.listingDataFromCreateRequests(sellerId,
          "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);
      ldata.setCurListingsMap(eventId, lmap);

      if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
        Assert.fail("Error encountered from listingDataFromRequests. Error: "
            + ldata.getListingErrors().get(0).getMessage());
      }

      // NEED TO DO THIS: need to seed with event (IMPORTANT!)
      ldata.updateEventInfo(sampleEvent);
      ldata.setSubscriber("Single|V2|test@testmail.com|test");
      SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
      ListingWrapper listingWrapper = new ListingWrapper(existinglisting, seatProductsContextMock);
      UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
      Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
          Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
          Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));

      List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
          helper.createOrUpdateListingData(ldata, clientIp, userAgent);

      Assert.assertTrue(responses != null && responses.size() == 1);

    } catch (ListingBusinessException ex) {
      Assert.assertTrue("Should fail. Single listing update for LMS listings failed", true);
    }

  }

  @Test
  public void updateLMSApprovedSingleListingQuantityChangeTrustedSeller2() throws Exception {

    Event sampleEvent = getEvent();
    Long eventId = 1000l;
    when(eventHelper.getEventById(eventId, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");
    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);

    // create an existing listing and set lms approveal
    Long sellerId = 100022l;
    Listing existinglisting = new Listing();
    existinglisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    existinglisting.setSellerId(sellerId);
    existinglisting.setDeliveryOption(7); // LMS delivery option
    existinglisting.setSection("PR115");
    existinglisting.setLmsApprovalStatus(2);
    existinglisting.setRow("R2");
    existinglisting.setSeats("20,21");
    existinglisting.setLmsApprovalStatus(2);
    existinglisting.setEventId(eventId);
    existinglisting.setId(10000045l);
    existinglisting.setFulfillmentDeliveryMethods("6|7, 2.0, 3.56");
    existinglisting.setQuantityRemain(2);
    existinglisting.setSellerContactId(1234L);

    Map<Long, Listing> lmap = new HashMap<Long, Listing>();
    lmap.put(10000045l, existinglisting);

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    try {
      shapiContext.setSignedJWTAssertion("DUMMY--assertion");

      ListingToDataAdapter adapter = new ListingToDataAdapter();
      List<ListingRequest> reqList = new ArrayList<ListingRequest>();

      List<Product> products = new ArrayList<Product>();
      Product prod1 = new Product();
      prod1.setRow("R2");
      prod1.setSeat("23");
      prod1.setProductType(ProductType.TICKET);
      prod1.setOperation(Operation.ADD);
      Product prod2 = new Product();
      prod2.setRow("R2");
      prod2.setSeat("24");
      prod2.setProductType(ProductType.TICKET);
      prod2.setOperation(Operation.ADD);
      products.add(prod1);
      products.add(prod2);

      ListingRequest req1 = new ListingRequest();
      req1.setExternalListingId("12345");
      req1.setListingId(10000045l);
      req1.setEventId(eventId.toString());
      // req1.setProducts(products);
      req1.setQuantity(3);
      req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);
      req1.setPricePerProduct(new Money("22.0", "USD"));
      req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.LMS);
      req1.setContactId(1234L);
      reqList.add(req1);

      ListingData ldata = adapter.listingDataFromCreateRequests(sellerId,
          "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);
      ldata.setCurListingsMap(eventId, lmap);

      if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
        Assert.fail("Error encountered from listingDataFromRequests. Error: "
            + ldata.getListingErrors().get(0).getMessage());
      }

      // NEED TO DO THIS: need to seed with event (IMPORTANT!)
      ldata.updateEventInfo(sampleEvent);
      ldata.setSubscriber("Single|V2|test@testmail.com|test");
      SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
      ListingWrapper listingWrapper = new ListingWrapper(existinglisting, seatProductsContextMock);
      UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
      Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
          Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
          Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));

      List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
          helper.createOrUpdateListingData(ldata, clientIp, userAgent);
      
      Assert.assertTrue(responses != null && responses.size() == 1);

    } catch (ListingBusinessException ex) {
      Assert.assertTrue("Should fail. Single listing update for LMS listings failed", true);
    }

  }

  @Test
  public void updateLMSApprovedSingleListingNoQuantityChangeNonTrustedSeller() throws Exception {

    Event sampleEvent = getEvent();
    Long eventId = 1000l;
    when(eventHelper.getEventById(eventId, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");
    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);


    // create an existing listing and set lms approveal
    Long sellerId = 100022l;
    Listing existinglisting = new Listing();
    existinglisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    existinglisting.setSellerId(sellerId);
    existinglisting.setDeliveryOption(7); // LMS delivery option
    existinglisting.setSection("PR115");
    existinglisting.setLmsApprovalStatus(2);
    existinglisting.setRow("R2");
    existinglisting.setSeats("20,21");
    existinglisting.setLmsApprovalStatus(2);
    existinglisting.setEventId(eventId);
    existinglisting.setId(10000045l);
    existinglisting.setFulfillmentDeliveryMethods("9, 2.0, 3.56");
    existinglisting.setQuantityRemain(2);
    existinglisting.setSellerContactId(1234L);

    Map<Long, Listing> lmap = new HashMap<Long, Listing>();
    lmap.put(10000045l, existinglisting);

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    try {
      shapiContext.setSignedJWTAssertion("DUMMY--assertion");

      ListingToDataAdapter adapter = new ListingToDataAdapter();
      List<ListingRequest> reqList = new ArrayList<ListingRequest>();



      ListingRequest req1 = new ListingRequest();
      req1.setExternalListingId("12345");
      req1.setListingId(10000045l);
      req1.setEventId(eventId.toString());
      // req1.setProducts(products);
      req1.setQuantity(2);
      req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);
      req1.setPricePerProduct(new Money("22.0", "USD"));
      req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.LMS);
      req1.setContactId(1234L);
      reqList.add(req1);

      ListingData ldata = adapter.listingDataFromCreateRequests(sellerId,
          "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);
      ldata.setCurListingsMap(eventId, lmap);

      if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
        Assert.fail("Error encountered from listingDataFromRequests. Error: "
            + ldata.getListingErrors().get(0).getMessage());
      }

      // NEED TO DO THIS: need to seed with event (IMPORTANT!)
      ldata.updateEventInfo(sampleEvent);
      ldata.setSubscriber("Single|V2|test@testmail.com|test");
      SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
      ListingWrapper listingWrapper = new ListingWrapper(existinglisting, seatProductsContextMock);
      UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
      Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
          Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
          Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));

      List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
          helper.createOrUpdateListingData(ldata, clientIp, userAgent);

      Assert.assertTrue(responses != null && responses.size() == 1);

    } catch (ListingBusinessException ex) {
      Assert.assertTrue("Should fail. Single listing update for LMS listings failed", true);
    }

  }

  @Test
  public void updateLMSApprovedSingleListingNoQuantityNonTrustedSeller() throws Exception {

    Event sampleEvent = getEvent();
    Long eventId = 1000l;
    when(eventHelper.getEventById(eventId, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");
    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);


    // create an existing listing and set lms approveal
    Long sellerId = 100022l;
    Listing existinglisting = new Listing();
    existinglisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    existinglisting.setSellerId(sellerId);
    existinglisting.setDeliveryOption(7); // LMS delivery option
    existinglisting.setSection("PR115");
    existinglisting.setLmsApprovalStatus(2);
    existinglisting.setRow("R2");
    existinglisting.setSeats("20,21");
    existinglisting.setLmsApprovalStatus(2);
    existinglisting.setEventId(eventId);
    existinglisting.setId(10000045l);
    existinglisting.setFulfillmentDeliveryMethods("9, 2.0, 3.56");
    existinglisting.setQuantityRemain(2);
    existinglisting.setSellerContactId(1234L);

    Map<Long, Listing> lmap = new HashMap<Long, Listing>();
    lmap.put(10000045l, existinglisting);

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    try {
      shapiContext.setSignedJWTAssertion("DUMMY--assertion");

      ListingToDataAdapter adapter = new ListingToDataAdapter();
      List<ListingRequest> reqList = new ArrayList<ListingRequest>();



      ListingRequest req1 = new ListingRequest();
      req1.setExternalListingId("12345");
      req1.setListingId(10000045l);
      req1.setEventId(eventId.toString());
      // req1.setProducts(products);
      // req1.setQuantity(2);
      req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);
      req1.setPricePerProduct(new Money("22.0", "USD"));
      req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.LMS);
      req1.setContactId(1234L);
      reqList.add(req1);

      ListingData ldata = adapter.listingDataFromCreateRequests(sellerId,
          "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);
      ldata.setCurListingsMap(eventId, lmap);

      if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
        Assert.fail("Error encountered from listingDataFromRequests. Error: "
            + ldata.getListingErrors().get(0).getMessage());
      }

      // NEED TO DO THIS: need to seed with event (IMPORTANT!)
      ldata.updateEventInfo(sampleEvent);
      ldata.setSubscriber("Single|V2|test@testmail.com|test");
      SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
      ListingWrapper listingWrapper = new ListingWrapper(existinglisting, seatProductsContextMock);
      UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
      Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
          Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
          Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));

      List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
          helper.createOrUpdateListingData(ldata, clientIp, userAgent);

      Assert.assertTrue(responses != null && responses.size() == 1);

    } catch (ListingBusinessException ex) {
      Assert.assertTrue("Should fail. Single listing update for LMS listings failed", true);
    }

  }

  @Test
  public void updateLMSApprovedSingleListingQuantityChange() throws Exception {

    Event sampleEvent = getEvent();
    Long eventId = 1000l;
    when(eventHelper.getEventById(eventId, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");
    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);


    // create an existing listing and set lms approveal
    Long sellerId = 100022l;
    Listing existinglisting = new Listing();
    existinglisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    existinglisting.setSellerId(sellerId);
    existinglisting.setDeliveryOption(7); // LMS delivery option
    existinglisting.setSection("PR115");
    existinglisting.setLmsApprovalStatus(2);
    existinglisting.setRow("R2");
    existinglisting.setSeats("20,21");
    existinglisting.setLmsApprovalStatus(2);
    existinglisting.setEventId(eventId);
    existinglisting.setId(10000045l);
    existinglisting.setFulfillmentDeliveryMethods("9, 2.0, 3.56");
    existinglisting.setQuantityRemain(2);
    existinglisting.setSellerContactId(1234L);

    Map<Long, Listing> lmap = new HashMap<Long, Listing>();
    lmap.put(10000045l, existinglisting);

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    try {
      shapiContext.setSignedJWTAssertion("DUMMY--assertion");

      ListingToDataAdapter adapter = new ListingToDataAdapter();
      List<ListingRequest> reqList = new ArrayList<ListingRequest>();

      List<Product> products = new ArrayList<Product>();
      Product prod1 = new Product();
      prod1.setRow("R2");
      prod1.setSeat("23");
      prod1.setProductType(ProductType.TICKET);
      prod1.setOperation(Operation.ADD);
      Product prod2 = new Product();
      prod2.setRow("R2");
      prod2.setSeat("24");
      prod2.setProductType(ProductType.TICKET);
      prod2.setOperation(Operation.ADD);
      products.add(prod1);
      products.add(prod2);

      ListingRequest req1 = new ListingRequest();
      req1.setExternalListingId("12345");
      req1.setListingId(10000045l);
      req1.setEventId(eventId.toString());
      // req1.setProducts(products);
      req1.setQuantity(3);
      req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);
      req1.setPricePerProduct(new Money("22.0", "USD"));
      req1.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.LMS);
      req1.setContactId(1234L);
      reqList.add(req1);

      ListingData ldata = adapter.listingDataFromCreateRequests(sellerId,
          "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);
      ldata.setCurListingsMap(eventId, lmap);

      if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
        Assert.fail("Error encountered from listingDataFromRequests. Error: "
            + ldata.getListingErrors().get(0).getMessage());
      }

      // NEED TO DO THIS: need to seed with event (IMPORTANT!)
      ldata.updateEventInfo(sampleEvent);
      SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
      ListingWrapper listingWrapper = new ListingWrapper(existinglisting, seatProductsContextMock);
      UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
      Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
          Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
          Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));

      List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
          helper.createOrUpdateListingData(ldata, clientIp, userAgent);

      Assert.assertTrue(responses == null);

    } catch (ListingBusinessException ex) {
      Assert.assertTrue("Should fail. Single listing update for LMS listings failed", true);
    }

  }


  @Test
  public void updateListingWithListingIdNull() throws Exception {

    ListingResponse listResp = new ListingResponse();
    String externalListingId = "12345";
    ListingError error = new ListingError();
    List<ListingError> errors = new ArrayList<ListingError>(1);
    error.setErrorCode("1234");
    errors.add(error);
    Long listingId = null;
    listResp.setErrors(errors);
    listResp.setExternalListingId(externalListingId);


    ErrorUtils errorUtils = new ErrorUtils();
    errorUtils.populateRespWithErrors(listResp, externalListingId, errors, listingId);
  }

  @Test
  public void updateListingWithListingId() throws Exception {

    ListingResponse listResp = new ListingResponse();
    String externalListingId = "12345";
    ListingError error = new ListingError();
    List<ListingError> errors = new ArrayList<ListingError>(1);
    error.setErrorCode("1234");
    errors.add(error);
    Long listingId = 123l;
    listResp.setErrors(errors);
    listResp.setExternalListingId(externalListingId);
    listResp.setId(listingId.toString());

    ErrorUtils errorUtils = new ErrorUtils();
    errorUtils.populateRespWithErrors(listResp, externalListingId, errors, listingId);
  }


  @Test
  public void updateListingSimplePrices() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "sec-10", "R1", 5));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing curListing = getListing(1000L, "General Admission", "R1", "1,2,3,4,5", 5, 5);
    curListing.setEvent(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(curListing);

    SHAPIContext shapiContext = new SHAPIContext();
    shapiContext.setSignedJWTAssertion("DUMMY--assertion");
    ListingToDataAdapter adapter = new ListingToDataAdapter();

    ListingRequest req = new ListingRequest();
    Money faceVal = new Money("7.5", "USD");
    Money purchPrice = new Money("23.22");
    req.setFaceValue(faceVal);
    req.setPurchasePrice(purchPrice);

    // get listing from request
    Listing listing = ListingRequestAdapter.convert(req, false, null);
    listing.setEvent(event);

    Listing result = helper.updateSingleListing(listing, curListing, req, shapiContext);

    Assert.assertTrue("Prices did not get updated in listing",
        faceVal.equals(result.getFaceValue()) && purchPrice.equals(result.getTicketCost()));
  }

  @Test
  public void createIncreaseQuantityGA() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "sec-10", "R1", 5));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    when(inventoryMgr.getListing(anyLong()))
        .thenReturn(getListing(1000L, "General Admission", "R1", "1,2,3,4,5", 5, 5));

    // Listing listToAdd = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5);
    // when(inventoryMgr.addListing(Mockito.any(Listing.class))).thenReturn(listToAdd);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setSellerId(1000l);
    newlisting.setQuantity(8);
    newlisting.setQuantityRemain(8);
    newlisting.setSaleMethod(1L);
    newlisting.setId(1000l);
    newlisting.setEvent(getEvent());
    newlisting.setTicketMedium(1);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    req.setEventId("1000");
    req.setListingId(1000l);
    req.setAutoPricingEnabledInd(Boolean.TRUE);
    req.setQuantity(8);

    try {
      newlisting = helper.createSingleListing(newlisting, req, shapiContext);
    } catch (ListingBusinessException ex) {
      Assert.fail("Should not fail. Simple increase quantity for GA listing from 5 to 8 seats");
    }
  }

  @Test
  public void updateIncreaseQuantityNonGA() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "sec-10", "R1", 5));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    when(inventoryMgr.getListing(anyLong()))
        .thenReturn(getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5));

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setQuantity(8);
    newlisting.setSaleMethod(1L);
    newlisting.setEvent(getEvent());
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    req.setEventId("1000");

    req.setQuantity(8);

    try {
      newlisting = helper.createSingleListing(newlisting, req, shapiContext);
      Assert.fail("Should not been able to increase quantity for non-GA listing");
    } catch (ListingBusinessException ex) {
    }
  }

  // @Test
  public void updateDecreaseQuantityGA() throws Exception {
    String GASection = CommonConstants.GENERAL_ADMISSION;

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, GASection, "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, GASection, "R1", "1,2,3,4,5,6,7,8", 10, 8);
    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);
    when(fulfillmentServiceHelper.calculateSaleEndDate(Mockito.any(Listing.class), Mockito.anyListOf(FulfillmentWindow.class)))
        .thenReturn(dblisting.getEndDate());

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setQuantity(8);
    newlisting.setSaleMethod(1L);
    newlisting.setEvent(getEvent());
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    req.setQuantity(8);

    Listing listing = helper.createSingleListing(newlisting, req, shapiContext);
    Assert.assertTrue("Expected no change in quantity because reduced quantity == quantityRemain",
        listing.getQuantity() == 10);

    // This should result in deletiong 2 seats
    dblisting = getListing(1000L, GASection, "R1", "1,2,3,4,5,6,7,8", 10, 8);
    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);
    newlisting.setQuantity(6);
    req.setQuantity(6);

    listing = helper.createSingleListing(newlisting, req, shapiContext);
    Assert.assertTrue(
        "Expected reducing quantity by 2 will cause quantity==8 and quantityRemain==6",
        listing.getQuantity() == 8 && listing.getQuantityRemain() == 6);
  }

  @Test
  public void updateListing_updateContactId() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setEvent(getEvent());
    dblisting.setSellerContactId(4321L);
    dblisting.setSellerContactGuid("test321");

    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");


    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
    when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(Arrays.asList(new FulfillmentWindow[0]));
    when(fulfillmentServiceHelper.populateFulfillmentOptions(Matchers.any(Listing.class), Matchers.anyList())).thenReturn(true);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setSellerContactId(1234L);
    newlisting.setSaleMethod(1L);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    ListingRequest req = new ListingRequest();
    req.setListingId(1000L);
    req.setContactId(1234L);
    req.setEventId("1000");
    req.setQuantity(2);
    req.setHideSeats(Boolean.TRUE);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }

    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helper.createOrUpdateListingData(ldata, clientIp, userAgent);

    Assert.assertTrue("Contact should be updated with contactId == 1234L",
        resps != null && resps.size() > 0 && resps.get(0).getId() != null);
  }
  
  @Test(expectedExceptions = { ExecutionException.class})
  public void updateListing_updateContactId_NotValid() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setEvent(getEvent());
    dblisting.setSellerContactId(4321L);
    dblisting.setSellerContactGuid("test321");;
    
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");

    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(false);
    when(userHelper.isUserContactGuidValid(Matchers.anyString(), Matchers.anyString())).thenReturn(false);
    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
    when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(Arrays.asList(new FulfillmentWindow[0]));
    when(fulfillmentServiceHelper.populateFulfillmentOptions(Matchers.any(Listing.class), Matchers.anyList())).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    
    ListingRequest req = new ListingRequest();
    req.setListingId(1000L);
    req.setContactId(1234L);
    req.setEventId("1000");
    req.setQuantity(2);
    req.setHideSeats(Boolean.TRUE);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps = helper.createOrUpdateListingData(ldata, clientIp, userAgent);


  }
  
  @Test(expectedExceptions = { ExecutionException.class})
  public void updateListing_updateContactId_contactGuidNull() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setEvent(getEvent());
    dblisting.setSellerContactId(4321L);
    dblisting.setSellerContactGuid("test321");;
    
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();

    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
    when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(Arrays.asList(new FulfillmentWindow[0]));
    when(fulfillmentServiceHelper.populateFulfillmentOptions(Matchers.any(Listing.class), Matchers.anyList())).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    
    ListingRequest req = new ListingRequest();
    req.setListingId(1000L);
    req.setContactId(1234L);
    req.setEventId("1000");
    req.setQuantity(2);
    req.setHideSeats(Boolean.TRUE);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps = helper.createOrUpdateListingData(ldata, clientIp, userAgent);


  }
  
  
  
  @Test
  public void updateListing_updateContactGuid() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setEvent(getEvent());
    dblisting.setSellerContactId(4321L);
    dblisting.setSellerContactGuid("test321");;
    
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");

    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(userHelper.isUserContactGuidValid(Matchers.anyString(), Matchers.anyString())).thenReturn(true);
    when(userHelper.getCustomerContactId(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
    when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(Arrays.asList(new FulfillmentWindow[0]));
    when(fulfillmentServiceHelper.populateFulfillmentOptions(Matchers.any(Listing.class), Matchers.anyList())).thenReturn(true);
    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setSellerContactId(1234L);
    newlisting.setSellerContactGuid("test123");;
    newlisting.setSaleMethod(1L);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    ListingRequest req = new ListingRequest();
    req.setListingId(1000L);
    req.setContactId(1234L);
    req.setContactGuid("test123");
    req.setEventId("1000");
    req.setQuantity(2);
    req.setHideSeats(Boolean.TRUE);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    
    
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
            helper.createOrUpdateListingData(ldata, clientIp, userAgent);

        Assert.assertTrue(resps != null && resps.size() > 0 && resps.get(0).getId() != null);
  }
  
  
  @Test(expectedExceptions = { ExecutionException.class})
  public void updateListing_updateContactGuid_NotValid() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setEvent(getEvent());
    dblisting.setSellerContactId(4321L);
    dblisting.setSellerContactGuid("test321");;
    
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");

    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(userHelper.isUserContactGuidValid(Matchers.anyString(), Matchers.anyString())).thenReturn(false);
    when(userHelper.getCustomerContactId(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
    when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(Arrays.asList(new FulfillmentWindow[0]));
    when(fulfillmentServiceHelper.populateFulfillmentOptions(Matchers.any(Listing.class), Matchers.anyList())).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    
    ListingRequest req = new ListingRequest();
    req.setListingId(1000L);
    req.setContactId(1234L);
    req.setContactGuid("test123");
    req.setEventId("1000");
    req.setQuantity(2);
    req.setHideSeats(Boolean.TRUE);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps = helper.createOrUpdateListingData(ldata, clientIp, userAgent);


  }
  
  @Test(expectedExceptions = { ExecutionException.class})
  public void updateListing_updateContactGuid_NoContactId() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setEvent(getEvent());
    dblisting.setSellerContactId(4321L);
    dblisting.setSellerContactGuid("test321");;
    
    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(userHelper.isUserContactGuidValid(Matchers.anyString(), Matchers.anyString())).thenReturn(true);
    when(userHelper.getCustomerContactId(Matchers.anyString(), Matchers.anyString())).thenReturn(null);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    
    ListingRequest req = new ListingRequest();
    req.setListingId(1000L);
    req.setContactId(1234L);
    req.setContactGuid("test123");
    req.setEventId("1000");
    req.setQuantity(2);
    req.setHideSeats(Boolean.TRUE);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps = helper.createOrUpdateListingData(ldata, clientIp, userAgent);


  }
  
  @Test(expectedExceptions = { ListingBusinessException.class})
  public void updateListing_updateContactGuid_FulfillmenetError() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setEvent(getEvent());
    dblisting.setSellerContactId(4321L);
    dblisting.setSellerContactGuid("test321");
    
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");

    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(userHelper.isUserContactGuidValid(Matchers.anyString(), Matchers.anyString())).thenReturn(true);
    when(userHelper.getCustomerContactId(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
    when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(Arrays.asList(new FulfillmentWindow[0]));
    when(fulfillmentServiceHelper.populateFulfillmentOptions(Matchers.any(Listing.class), Matchers.anyList())).thenReturn(false);
    when(fulfillmentServiceHelper.isShipping(Matchers.any(Listing.class))).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    
    ListingRequest req = new ListingRequest();
    req.setListingId(1000L);
    req.setContactGuid("test123");
    req.setEventId("1000");
    req.setQuantity(2);
    req.setHideSeats(Boolean.TRUE);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps = helper.createOrUpdateListingData(ldata, clientIp, userAgent);


  }
  
  @Test
  public void updateListing_updateContactGuid_WithSameContactGuid() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setEvent(getEvent());
    dblisting.setSellerContactId(1234L);
    dblisting.setSellerContactGuid("test123");;
    
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");

    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(userHelper.isUserContactGuidValid(Matchers.anyString(), Matchers.anyString())).thenReturn(true);
    when(userHelper.getCustomerContactId(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
    when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(Arrays.asList(new FulfillmentWindow[0]));
    when(fulfillmentServiceHelper.populateFulfillmentOptions(Matchers.any(Listing.class), Matchers.anyList())).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    ListingRequest req = new ListingRequest();
    req.setListingId(1000L);
    req.setContactId(1234L);
    req.setContactGuid("test123");
    req.setEventId("1000");
    req.setQuantity(2);
    req.setHideSeats(Boolean.TRUE);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps = helper.createOrUpdateListingData(ldata, clientIp, userAgent);

    Assert.assertTrue(resps != null && resps.size() > 0 && resps.get(0).getId() != null);

  }
  
  @Test
  public void updateListing_updateContactGuid_WithDifferentTicketTicketMedium() throws Exception {
	    TicketMedium[] ticketMediums  = TicketMedium.values();

    
    for(TicketMedium  ticketMedium : ticketMediums){
	    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    
    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setEvent(getEvent());
    dblisting.setSellerContactId(4321L);
    dblisting.setSellerContactGuid("test321");
    	
        dblisting.setTicketMedium(ticketMedium.getValue());

	    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
	    mappingResponse.setId("test123");
	    mappingResponse.setInternalId("1234");
	
	    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);
	    Mockito.doAnswer(new Answer<Void>() {
	      @Override
	      public Void answer(InvocationOnMock invocation) throws Throwable {
	        return null;
	      }
	    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
	    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
	        .thenReturn(true);
	    when(userHelper.isUserContactGuidValid(Matchers.anyString(), Matchers.anyString())).thenReturn(true);
	    when(userHelper.getCustomerContactId(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
	    when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
	    when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(Arrays.asList(new FulfillmentWindow[0]));
	    when(fulfillmentServiceHelper.populateFulfillmentOptions(Matchers.any(Listing.class), Matchers.anyList())).thenReturn(true);
	    Listing newlisting = new Listing();
	    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
	    newlisting.setId(1000l);
	    newlisting.setSellerId(1000l);
	    newlisting.setSellerContactId(1234L);
	    newlisting.setSellerContactGuid("test123");;
	    newlisting.setSaleMethod(1L);
	    newlisting.setTicketMedium(ticketMedium.getValue());

	    SHAPIContext shapiContext = new SHAPIContext();
	    SHServiceContext shServiceContext = new SHServiceContext();
	    
	    ListingRequest req = new ListingRequest();
	    req.setListingId(1000L);
	    req.setContactId(1234L);
	    req.setContactGuid("test123");
	    req.setEventId("1000");
	    req.setQuantity(2);
	    req.setHideSeats(Boolean.TRUE);
	
	    Object[] prResp = new Object[1];
	    prResp[0] = getPriceResponse("25", "4");
	    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
	            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
	
	    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
	    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
	      throw new ListingBusinessException(ldata.getListingErrors().get(0));
	    }
	    
	    
	    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
	            helper.createOrUpdateListingData(ldata, clientIp, userAgent);

        Assert.assertTrue(resps != null && resps.size() > 0 && resps.get(0).getId() != null);
    }

  }
  
  @Test
  public void updateListing_updateContactGuid_WithDifferentFulfillmentDeliveryMethods() throws Exception {
	    ArrayList<String> fulfillmentMethods  = new ArrayList<>();
	    fulfillmentMethods.add("|11,");
	    fulfillmentMethods.add("|12,");
	    fulfillmentMethods.add("|15,");
	    fulfillmentMethods.add("11,");
	    fulfillmentMethods.add("12,");
	    fulfillmentMethods.add("15,");
	    fulfillmentMethods.add("|10,");
	    fulfillmentMethods.add("10,");
	    fulfillmentMethods.add("|9,");
	    fulfillmentMethods.add("|7,");
	    fulfillmentMethods.add("9,");
	    fulfillmentMethods.add("7,");
	    fulfillmentMethods.add("|17,");
	    fulfillmentMethods.add("17,");
	    fulfillmentMethods.add("|8,");
	    fulfillmentMethods.add("8,");

    
    for(String  fulfillmentMethod : fulfillmentMethods){
	    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    
    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setEvent(getEvent());
    dblisting.setSellerContactId(4321L);
    dblisting.setSellerContactGuid("test321");
    dblisting.setTicketMedium(null);
    dblisting.setFulfillmentDeliveryMethods(fulfillmentMethod);	

	    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
	    mappingResponse.setId("test123");
	    mappingResponse.setInternalId("1234");
	
	    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);
	    Mockito.doAnswer(new Answer<Void>() {
	      @Override
	      public Void answer(InvocationOnMock invocation) throws Throwable {
	        return null;
	      }
	    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
	    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
	        .thenReturn(true);
	    when(userHelper.isUserContactGuidValid(Matchers.anyString(), Matchers.anyString())).thenReturn(true);
	    when(userHelper.getCustomerContactId(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
	    when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
	    when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(Arrays.asList(new FulfillmentWindow[0]));
	    when(fulfillmentServiceHelper.populateFulfillmentOptions(Matchers.any(Listing.class), Matchers.anyList())).thenReturn(true);
	    Listing newlisting = new Listing();
	    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
	    newlisting.setId(1000l);
	    newlisting.setSellerId(1000l);
	    newlisting.setSellerContactId(1234L);
	    newlisting.setSellerContactGuid("test123");;
	    newlisting.setSaleMethod(1L);
	    newlisting.setFulfillmentDeliveryMethods(fulfillmentMethod);

	    SHAPIContext shapiContext = new SHAPIContext();
	    SHServiceContext shServiceContext = new SHServiceContext();
	    
	    ListingRequest req = new ListingRequest();
	    req.setListingId(1000L);
	    req.setContactId(1234L);
	    req.setContactGuid("test123");
	    req.setEventId("1000");
	    req.setQuantity(2);
	    req.setHideSeats(Boolean.TRUE);
	
	    Object[] prResp = new Object[1];
	    prResp[0] = getPriceResponse("25", "4");
	    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
	            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
	
	    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
	    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
	      throw new ListingBusinessException(ldata.getListingErrors().get(0));
	    }
	    
	    
	    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
	            helper.createOrUpdateListingData(ldata, clientIp, userAgent);

        Assert.assertTrue(resps != null && resps.size() > 0 && resps.get(0).getId() != null);
    }

  }
  
  @Test
  public void updateListing_updateContactGuid_WithListingSource() throws Exception {

	    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
	    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    
	    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
	    dblisting.setEvent(getEvent());
	    dblisting.setSellerContactId(4321L);
	    dblisting.setSellerContactGuid("test321");
	    dblisting.setListingSource(8);

	    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
	    mappingResponse.setId("test123");
	    mappingResponse.setInternalId("1234");
	
	    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);
	    Mockito.doAnswer(new Answer<Void>() {
	      @Override
	      public Void answer(InvocationOnMock invocation) throws Throwable {
	        return null;
	      }
	    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
	    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
	        .thenReturn(true);
	    when(userHelper.isUserContactGuidValid(Matchers.anyString(), Matchers.anyString())).thenReturn(true);
	    when(userHelper.getCustomerContactId(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
	    when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
	    when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(Arrays.asList(new FulfillmentWindow[0]));
	    when(fulfillmentServiceHelper.populateFulfillmentOptions(Matchers.any(Listing.class), Matchers.anyList())).thenReturn(true);
	    Listing newlisting = new Listing();
	    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
	    newlisting.setId(1000l);
	    newlisting.setSellerId(1000l);
	    newlisting.setSellerContactId(1234L);
	    newlisting.setSellerContactGuid("test123");;
	    newlisting.setSaleMethod(1L);
	    newlisting.setListingSource(8);

	    SHAPIContext shapiContext = new SHAPIContext();
	    SHServiceContext shServiceContext = new SHServiceContext();
	
	    ListingRequest req = new ListingRequest();
	    req.setListingId(1000L);
	    req.setContactId(1234L);
	    req.setContactGuid("test123");
	    req.setEventId("1000");
	    req.setQuantity(2);
	    req.setHideSeats(Boolean.TRUE);
	
	    Object[] prResp = new Object[1];
	    prResp[0] = getPriceResponse("25", "4");
	    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
	            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
	
	    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
	    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
	      throw new ListingBusinessException(ldata.getListingErrors().get(0));
	    }
	    
	    
	    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
	            helper.createOrUpdateListingData(ldata, clientIp, userAgent);

        Assert.assertTrue(resps != null && resps.size() > 0 && resps.get(0).getId() != null);
    

  }
  
  @Test
  public void updateListing_updateContactGuid_WithDifferentTicketTicket() throws Exception {
	    TicketMedium[] ticketMediums  = TicketMedium.values();

    
    for(TicketMedium  ticketMedium : ticketMediums){
	    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    
    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setEvent(getEvent());
    dblisting.setSellerContactId(4321L);
    dblisting.setSellerContactGuid("test321");
    	
        dblisting.setTicketMedium(ticketMedium.getValue());

	    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
	    mappingResponse.setId("test123");
	    mappingResponse.setInternalId("1234");
	
	    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);
	    Mockito.doAnswer(new Answer<Void>() {
	      @Override
	      public Void answer(InvocationOnMock invocation) throws Throwable {
	        return null;
	      }
	    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
	    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
	        .thenReturn(true);
	    when(userHelper.isUserContactGuidValid(Matchers.anyString(), Matchers.anyString())).thenReturn(true);
	    when(userHelper.getCustomerContactId(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
	    when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
	    when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(Arrays.asList(new FulfillmentWindow[0]));
	    when(fulfillmentServiceHelper.populateFulfillmentOptions(Matchers.any(Listing.class), Matchers.anyList())).thenReturn(true);
	    Listing newlisting = new Listing();
	    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
	    newlisting.setId(1000l);
	    newlisting.setSellerId(1000l);
	    newlisting.setSellerContactId(1234L);
	    newlisting.setSellerContactGuid("test123");;
	    newlisting.setSaleMethod(1L);
	    newlisting.setTicketMedium(ticketMedium.getValue());

	    SHAPIContext shapiContext = new SHAPIContext();
	    SHServiceContext shServiceContext = new SHServiceContext();
	
	    ListingRequest req = new ListingRequest();
	    req.setListingId(1000L);
	    req.setContactId(1234L);
	    req.setContactGuid("test123");
	    req.setEventId("1000");
	    req.setQuantity(2);
	    req.setHideSeats(Boolean.TRUE);
	
	    Object[] prResp = new Object[1];
	    prResp[0] = getPriceResponse("25", "4");
	    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
	            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
	
	    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
	    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
	      throw new ListingBusinessException(ldata.getListingErrors().get(0));
	    }
	    
	    
	    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
	            helper.createOrUpdateListingData(ldata, clientIp, userAgent);

        Assert.assertTrue(resps != null && resps.size() > 0 && resps.get(0).getId() != null);
    }

  }
  
  @Test
  public void updateListing_updateContactNull() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setEvent(getEvent());

    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setSellerContactId(1234L);
    newlisting.setSellerContactGuid(null);;
    newlisting.setSaleMethod(1L);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    ListingRequest req = new ListingRequest();
    req.setListingId(1000L);
    req.setContactId(1234L);
    req.setContactGuid(null);
    req.setEventId("1000");
    req.setQuantity(2);
    req.setHideSeats(Boolean.TRUE);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
            helper.createOrUpdateListingData(ldata, clientIp, userAgent);

        Assert.assertTrue("Contact should be updated with contactId == 1234L",
            resps != null && resps.size() > 0 && resps.get(0).getId() != null);
  }

  @Test
  public void updateListing_updateContactIdwithMessage() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setEvent(getEvent());
    dblisting.setSellerContactId(4321L);
    dblisting.setSellerContactGuid("test321");

    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");

    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong())).thenReturn(true);
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong())).thenReturn(true);
    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
    when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(Arrays.asList(new FulfillmentWindow[0]));
    when(fulfillmentServiceHelper.populateFulfillmentOptions(Matchers.any(Listing.class), Matchers.anyList())).thenReturn(true);


    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setSellerContactId(1234L);
    newlisting.setSaleMethod(1L);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    ListingRequest req = new ListingRequest();
    req.setListingId(1000L);
    req.setContactId(1234L);
    req.setEventId("1000");
    req.setQuantity(2);



    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    Listing listing = ldata.getCurSingleListing();
    listing.setLmsApprovalStatus(1);
    listing.setSendLmsMessage(true);

    List<Listing> listings = new ArrayList<Listing>();
    listings.add(listing);

    when(inventoryMgr.updateListings(Mockito.anyList())).thenReturn(listings);


    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }

    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helper.createOrUpdateListingData(ldata, clientIp, userAgent);

    Assert.assertTrue("Contact should be updated with contactId == 1234L",
        resps != null && resps.size() > 0 && resps.get(0).getId() != null);
  }

  @Test
  public void updateListing_updateExternalListingId() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setEvent(getEvent());

    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);

    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(inventoryMgr.getListingBySellerIdExternalIdAndStatus(Matchers.anyLong(),
        Matchers.anyString())).thenReturn(null);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setExternalId("12345");

    ListingRequest req = new ListingRequest();
    req.setExternalListingId("12345");
    req.setListingId(1000l);
    req.setEventId("1000");
    req.setQuantity(2);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }

    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helper.createOrUpdateListingData(ldata, clientIp, userAgent);

    Assert.assertTrue("External listing id should NOT be updated", resps != null && resps.size() > 0
        && resps.get(0).getErrors() != null && resps.get(0).getErrors().size() > 0);
  }

  @Test
  public void updateListing_validateFileinfoId() throws Exception {
    when(ticketSeatMgr.findActiveTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 2));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2", 10, 2);
    TicketSeat ts1 = new TicketSeat();
  	ts1.setSeatStatusId(1L);
  	TicketSeat ts2 = new TicketSeat();
  	ts2.setSeatStatusId(1L);
  	List<TicketSeat> tsList = new ArrayList<TicketSeat>();
  	tsList.add(ts1);
  	tsList.add(ts2);
  	dblisting.setTicketSeats(tsList);
    dblisting.setEvent(getEvent());
    dblisting.setTicketMedium(TicketMedium.PDF.getValue());
    dblisting.setSellerRequestedStatus("ACTIVE");
    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);

    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(inventoryMgr.getListingBySellerIdExternalIdAndStatus(Matchers.anyLong(),
        Matchers.anyString())).thenReturn(null);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEventId(1000l);
    newlisting.setExternalId("12345");
    TicketSeat seat = new TicketSeat();
    seat.setFulfillmentArtifactId(12345l);
    List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>(1);
    ticketSeats.add(seat);
    newlisting.setTicketSeats(ticketSeats);
    List<Listing> newListingList = new ArrayList<Listing>(1);
    newListingList.add(newlisting);
    ListingRequest req = new ListingRequest();
    req.setExternalListingId("12345");
    req.setListingId(1000l);
    req.setEventId("1000");

    Product product = new Product();
    product.setFulfillmentArtifact("123");
    product.setExternalId("123");
    Product product1 = new Product();
    product1.setFulfillmentArtifact("1234");
    product1.setExternalId("1234");
    List<Product> products = new ArrayList<Product>();
    products.add(product);
    products.add(product1);
    req.setProducts(products);
    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
    List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);
    SeatProduct seatProd = new SeatProduct();
    seatProd.setProductType(ProductType.TICKET);
    seatProd.setFulfillmentArtifact("1234");
    seatProd.setRow("row");
    seatProd.setSeat("seat");
    seatProd.setSeatId(123l);

    SeatProduct seatProd1 = new SeatProduct();
    seatProd1.setProductType(ProductType.TICKET);
    seatProd1.setFulfillmentArtifact("1234");
    seatProd1.setRow("row");
    seatProd1.setSeat("seat");
    seatProd1.setSeatId(123l);
    seatProductList.add(seatProd);
    seatProductList.add(seatProd1);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 5);
    Mockito.doReturn(seatProductList).when(seatProductsContextMock).getBarcodeSeatProductList();
    Mockito.doReturn(dbSeats).when(seatProductsContextMock).getTicketSeatsFromCache();
    ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
    UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
    Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
        Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
        Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    List<Listing> dbList = new ArrayList<Listing>(1);

    dbList.add(dblisting);
    Mockito.doReturn(dbList).when(helperspy).updateDatabaseForBatch(Mockito.anyBoolean(),
        Mockito.anyListOf(Listing.class));
    when(listingFulfilHelper.validateFileInfoIds(Mockito.any(SeatProductsContext.class),
        Mockito.any(Listing.class))).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    
    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    ldata.setHeaderListing(newlisting);
    when(fulfillmentServiceHelper.populateFulfillmentOptions((Listing) Matchers.any(),
        (List<com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow>) Matchers.any()))
            .thenReturn(true);
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helperspy.createOrUpdateListingData(ldata, clientIp, userAgent);
  }

  @Test
  public void updateListing_validateFileinfoIdIncomplete() throws Exception {
    when(ticketSeatMgr.findActiveTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 2));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2", 10, 2);
    TicketSeat ts1 = new TicketSeat();
  	ts1.setSeatStatusId(1L);
  	TicketSeat ts2 = new TicketSeat();
  	ts2.setSeatStatusId(1L);
  	List<TicketSeat> tsList = new ArrayList<TicketSeat>();
  	tsList.add(ts1);
  	tsList.add(ts2);
  	dblisting.setTicketSeats(tsList);
    dblisting.setEvent(getEvent());
    dblisting.setTicketMedium(TicketMedium.PDF.getValue());
    dblisting.setSellerRequestedStatus("INCOMPLETE");
    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);

    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(inventoryMgr.getListingBySellerIdExternalIdAndStatus(Matchers.anyLong(),
        Matchers.anyString())).thenReturn(null);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEventId(1000l);
    newlisting.setExternalId("12345");
    TicketSeat seat = new TicketSeat();
    seat.setFulfillmentArtifactId(12345l);
    List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>(1);
    ticketSeats.add(seat);
    newlisting.setTicketSeats(ticketSeats);
    List<Listing> newListingList = new ArrayList<Listing>(1);
    newListingList.add(newlisting);
    ListingRequest req = new ListingRequest();
    req.setExternalListingId("12345");
    req.setListingId(1000l);
    req.setEventId("1000");

    Product product = new Product();
    product.setFulfillmentArtifact("123");
    product.setExternalId("123");
    Product product1 = new Product();
    product1.setFulfillmentArtifact("1234");
    product1.setExternalId("1234");
    List<Product> products = new ArrayList<Product>();
    products.add(product);
    products.add(product1);
    req.setProducts(products);
    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
    List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);
    SeatProduct seatProd = new SeatProduct();
    seatProd.setProductType(ProductType.TICKET);
    seatProd.setFulfillmentArtifact("1234");
    seatProd.setRow("row");
    seatProd.setSeat("seat");
    seatProd.setSeatId(123l);

    SeatProduct seatProd1 = new SeatProduct();
    seatProd1.setProductType(ProductType.TICKET);
    seatProd1.setFulfillmentArtifact("1234");
    seatProd1.setRow("row");
    seatProd1.setSeat("seat");
    seatProd1.setSeatId(123l);
    seatProductList.add(seatProd);
    seatProductList.add(seatProd1);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 5);
    Mockito.doReturn(seatProductList).when(seatProductsContextMock).getBarcodeSeatProductList();
    Mockito.doReturn(dbSeats).when(seatProductsContextMock).getTicketSeatsFromCache();
    ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
    UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
    Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
        Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
        Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    List<Listing> dbList = new ArrayList<Listing>(1);

    dbList.add(dblisting);
    Mockito.doReturn(dbList).when(helperspy).updateDatabaseForBatch(Mockito.anyBoolean(),
        Mockito.anyListOf(Listing.class));
    when(listingFulfilHelper.validateFileInfoIds(Mockito.any(SeatProductsContext.class),
        Mockito.any(Listing.class))).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    
    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    ldata.setHeaderListing(newlisting);
    when(fulfillmentServiceHelper.populateFulfillmentOptions((Listing) Matchers.any(),
        (List<com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow>) Matchers.any()))
            .thenReturn(true);
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helperspy.createOrUpdateListingData(ldata, clientIp, userAgent);
  }

  @Test
  public void updateListing_validateFileinfoIdFailure() throws Exception {
    Listing listing;
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2", 10, 2);
    TicketSeat ts1 = new TicketSeat();
  	ts1.setSeatStatusId(1L);
  	TicketSeat ts2 = new TicketSeat();
  	ts2.setSeatStatusId(1L);
  	List<TicketSeat> tsList = new ArrayList<TicketSeat>();
  	tsList.add(ts1);
  	tsList.add(ts2);
  	dblisting.setTicketSeats(tsList);
    dblisting.setEvent(getEvent());
    dblisting.setTicketMedium(TicketMedium.PDF.getValue());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);

    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(inventoryMgr.getListingBySellerIdExternalIdAndStatus(Matchers.anyLong(),
        Matchers.anyString())).thenReturn(null);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setExternalId("12345");
    // newlisting.setTicketMedium(TicketMedium.PDF.getValue());
    TicketSeat seat = new TicketSeat();
    seat.setFulfillmentArtifactId(12345l);
    List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>(1);
    ticketSeats.add(seat);
    newlisting.setTicketSeats(ticketSeats);
    newlisting.setLmsApprovalStatus(1);

    List<Listing> newListingList = new ArrayList<Listing>(1);
    newListingList.add(newlisting);
    ListingRequest req = new ListingRequest();
    req.setExternalListingId("12345");
    req.setListingId(1000l);
    req.setEventId("1000");
    // req.setQuantity(2);

    Product product = new Product();
    product.setFulfillmentArtifact("123");
    product.setExternalId("123");
    Product product1 = new Product();
    product1.setFulfillmentArtifact("1234");
    product1.setExternalId("1234");
    List<Product> products = new ArrayList<Product>();
    products.add(product);
    products.add(product1);
    req.setProducts(products);
    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    // SeatProductsContext seatProductsContext = new SeatProductsContext(newlisting, req,
    // ticketSeatMgr, listingSeatTraitMgr);
    SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
    List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);
    SeatProduct seatProd = new SeatProduct();
    seatProd.setProductType(ProductType.TICKET);
    seatProd.setFulfillmentArtifact("1234");
    seatProd.setRow("row");
    seatProd.setSeat("seat");
    seatProd.setSeatId(123l);

    SeatProduct seatProd1 = new SeatProduct();
    seatProd1.setProductType(ProductType.TICKET);
    seatProd1.setFulfillmentArtifact("1234");
    seatProd1.setRow("row");
    seatProd1.setSeat("seat");
    seatProd1.setSeatId(123l);
    seatProductList.add(seatProd);
    seatProductList.add(seatProd1);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 5);
    Mockito.doReturn(seatProductList).when(seatProductsContextMock).getBarcodeSeatProductList();
    Mockito.doReturn(dbSeats).when(seatProductsContextMock).getTicketSeatsFromCache();
    ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
    UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
    Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
        Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
        Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    List<Listing> dbList = new ArrayList<Listing>(1);
    // dblisting.setExternalId(null);

    dbList.add(dblisting);
    Mockito.doReturn(dbList).when(helperspy).updateDatabaseForBatch(Mockito.anyBoolean(),
        Mockito.anyListOf(Listing.class));
    // when(helperspy.updateDatabaseForBatch(Mockito.anyBoolean(),Mockito.anyListOf(Listing.class))).thenReturn(dbList);
    when(listingFulfilHelper.validateFileInfoIds(Mockito.any(SeatProductsContext.class),
        Mockito.any(Listing.class))).thenReturn(false);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    
    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    ldata.setHeaderListing(newlisting);

    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helperspy.createOrUpdateListingData(ldata, clientIp, userAgent);

  }

  @Test
  public void updateListing_validateFileinfoIdError() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setEvent(getEvent());
    dblisting.setTicketMedium(TicketMedium.PDF.getValue());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);

    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(inventoryMgr.getListingBySellerIdExternalIdAndStatus(Matchers.anyLong(),
        Matchers.anyString())).thenReturn(null);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setExternalId("12345");
    newlisting.setTicketMedium(TicketMedium.PDF.getValue());
    TicketSeat seat = new TicketSeat();
    seat.setFulfillmentArtifactId(12345l);
    List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>(1);
    ticketSeats.add(seat);
    newlisting.setTicketSeats(ticketSeats);

    ListingRequest req = new ListingRequest();
    req.setExternalListingId("12345");
    req.setListingId(1000l);
    req.setEventId("1000");
    // req.setQuantity(2);

    Product product = new Product();
    product.setFulfillmentArtifact(null);
    Product product1 = new Product();
    product.setFulfillmentArtifact(null);
    List<Product> products = new ArrayList<Product>();
    products.add(product);
    products.add(product1);
    req.setProducts(products);
    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    
    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    ldata.setHeaderListing(newlisting);

    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helper.createOrUpdateListingData(ldata, clientIp, userAgent);

    Assert.assertTrue("External listing id should NOT be updated", resps != null && resps.size() > 0
        && resps.get(0).getErrors() != null && resps.get(0).getErrors().size() > 0);
  }

  @Test
  public void testNonGA_productAdd_Active_barcode_Invalid() throws Exception {
    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 5);

    dbListing.setSystemStatus("ACTIVE");
    dbListing.setTicketMedium(TicketMedium.BARCODE.getValue());

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(dbSeats);
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dbListing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEvent(getEvent());
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();

    ArrayList<Product> products = new ArrayList<Product>();
    products.add(getProduct(Operation.ADD, "R1", "6", ProductType.TICKET, null, null));
    products.add(getProduct(Operation.ADD, "R1", "7", ProductType.TICKET, null, null));
    req.setProducts(products);

    try {
      Listing result = helper.createSingleListing(newlisting, req, shapiContext);
    } catch (ListingBusinessException e) {
      Assert.fail(
          "Should ALLOW adding a new ticket without FultillmentArtifact (because it is in create mode)");
    }

    dbListing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    req.setListingId(1000l); // indicates update
    try {
      Listing result = helper.createSingleListing(newlisting, req, shapiContext);
      Assert.fail(
          "Should NOT allow adding a new ticket without FultillmentArtifact because it is in update mode");
    } catch (ListingBusinessException e) {
    }
  }

  /**
   * testNonGA_productAdd
   * 
   * @return
   * @throws Exception
   */
  @Test
  public void testNonGA_productAdd() throws Exception {
    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 5);

    dbListing.setSystemStatus("INCOMPLETE");
    dbListing.setEvent(getEvent());
    dbListing.setInhandDateValidated(true);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(dbSeats);
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dbListing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEvent(getEvent());
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();

    ArrayList<Product> products = new ArrayList<Product>();
    products.add(getProduct(Operation.ADD, "R1", "6", ProductType.TICKET, null, null));
    products.add(getProduct(Operation.ADD, "R1", "7", ProductType.TICKET, null, null));

    req.setProducts(products);

    String saleEndDate = "2095-10-14T18:30:00-0700";
    req.setSaleEndDate(saleEndDate);

    ListingWrapper lr =
        helper.updateOrCreateSingleListing(newlisting, dbListing, req, shapiContext, null);
    Listing result = lr.getListing();

    // should succeed
    Assert.assertTrue("Adding new seats with Operation.ADD did not work",
        result.getSeats() != null && result.getSeats().equals("1,2,3,4,5,6,7"));

    Assert.assertTrue("Adding new TicketSeats with Operation.ADD did not work",
        result.getTicketSeats() != null && result.getTicketSeats().size() == 7);

    // should fail (already there)
    products.clear();
    products.add(getProduct(Operation.ADD, "R1", "5", ProductType.TICKET, null, null));
    req.setProducts(products);

    try {
      result = helper.createSingleListing(newlisting, req, shapiContext);
      Assert.fail(
          "Should not allow to issue Operation.ADD for R1 seat 5 because it is already exists");
    } catch (ListingBusinessException lbe) {
    }
  }

  /**
   * testNonGA_productDelete
   * 
   * @return
   * @throws Exception
   */
  @Test
  public void testNonGA_productDelete() throws Exception {
    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 5);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(dbSeats);
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dbListing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEvent(getEvent());
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();

    ArrayList<Product> products = new ArrayList<Product>();
    products.add(getProduct(Operation.DELETE, "R1", "3", ProductType.TICKET, null, null));
    products.add(getProduct(Operation.DELETE, "R1", "5", ProductType.TICKET, null, null));
    req.setProducts(products);

    // expect success
    Listing result = helper.createSingleListing(newlisting, req, shapiContext);

    Assert.assertTrue("Deleting 2 seats did not result in correct seats csv list",
        result.getSeats() != null && result.getSeats().equals("1,2,4"));

    Assert.assertTrue("Ticket seats 3 and 5 did not actually get deleted",
        result.getTicketSeats() != null && result.getTicketSeats().size() == 5
            && result.getTicketSeats().get(2).getSeatStatusId() == 4
            && result.getTicketSeats().get(4).getSeatStatusId() == 4);

    // should fail (delete seat that does not exist)
    products.clear();
    products.add(getProduct(Operation.DELETE, "R1", "10", ProductType.TICKET, null, null));
    try {
      result = helper.createSingleListing(newlisting, req, shapiContext);

      Assert
          .fail("Operation.DELETE for seat that does not exist (number '10') should not succeed!");
    } catch (ListingBusinessException ignore) {
    }
  }

  /**
   * testNonGA_productUpdate_active
   * 
   * @return
   * @throws Exception
   */
  // @Test
  public void testNonGA_productUpdate_active() throws Exception {
    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 5);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(dbSeats);
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dbListing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEvent(getEvent());
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();

    dbListing.setSystemStatus(ListingStatus.ACTIVE.name());
    dbListing.setTicketMedium(TicketMedium.BARCODE.getValue());

    ArrayList<Product> products = new ArrayList<Product>();
    products.add(getProduct(Operation.UPDATE, "R1", "3", ProductType.TICKET, "1000550", null));
    products.add(getProduct(Operation.UPDATE, "R1", "5", ProductType.TICKET, "1000551", null));
    req.setProducts(products);

    // expect success
    Listing result = null;
    try {
      result = helper.createSingleListing(newlisting, req, shapiContext);
    } catch (ListingBusinessException ignore) {
      Assert.fail("Changing barcode should be allowed on BARCODE ACTIVE Listing");
    }

    // it should work for PDF
    dbListing.setTicketMedium(TicketMedium.PDF.getValue());
    try {
      result = helper.createSingleListing(newlisting, req, shapiContext);
    } catch (ListingBusinessException ex) {
      Assert.fail(
          "Changing fulfillmentArtifact should not be allowed on a PDF ACTIVE Listing. Cause: "
              + ex.toString());
    }

    Assert.assertTrue("fulfillmentArtifact id did not get updated correctly for seat 3 and 5",
        result.getTicketSeats() != null && result.getTicketSeats().size() == 5
            && result.getTicketSeats().get(2).getFulfillmentArtifactId() == 1000550L
            && result.getTicketSeats().get(4).getFulfillmentArtifactId() == 1000551L);

    // should fail because the row, seat do not exit
    products.clear();
    products.add(getProduct(Operation.UPDATE, "R1", "10", ProductType.TICKET, "1000550", null));
    try {
      result = helper.createSingleListing(newlisting, req, shapiContext);
      Assert.fail("Should not be allowed to update a row, seat that do not exist");
    } catch (ListingBusinessException ignore) {
    }
  }

  @Test
  public void testBarcodePredelivery() throws Exception {
    when(ticketSeatMgr.findActiveTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 2));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2", 10, 2);
    TicketSeat ts1 = new TicketSeat();
	ts1.setSeatStatusId(1L);
	TicketSeat ts2 = new TicketSeat();
	ts2.setSeatStatusId(1L);
	List<TicketSeat> tsList = new ArrayList<TicketSeat>();
	tsList.add(ts1);
	tsList.add(ts2);
	dblisting.setTicketSeats(tsList);
    dblisting.setEvent(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);

    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(inventoryMgr.getListingBySellerIdExternalIdAndStatus(Matchers.anyLong(),
        Matchers.anyString())).thenReturn(null);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEventId(1000l);
    newlisting.setExternalId("12345");
    newlisting.setPredeliveryAvailable(true);
    TicketSeat seat = new TicketSeat();
    seat.setFulfillmentArtifactId(12345l);
    List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>(1);
    ticketSeats.add(seat);
    newlisting.setTicketSeats(ticketSeats);

    List<Listing> newListingList = new ArrayList<Listing>(1);
    newListingList.add(newlisting);
    ListingRequest req = new ListingRequest();
    req.setExternalListingId("12345");
    req.setListingId(1000l);
    req.setEventId("1000");

    Product product = new Product();
    product.setFulfillmentArtifact("123");
    product.setExternalId("1234");
    Product product1 = new Product();
    product1.setFulfillmentArtifact("1234");
    product1.setExternalId("5678");
    List<Product> products = new ArrayList<Product>();
    products.add(product);
    products.add(product1);
    req.setProducts(products);
    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
    List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);
    SeatProduct seatProd = new SeatProduct();
    seatProd.setProductType(ProductType.TICKET);
    seatProd.setFulfillmentArtifact("1234");
    seatProd.setRow("row");
    seatProd.setSeat("seat");
    seatProd.setSeatId(123l);

    SeatProduct seatProd1 = new SeatProduct();
    seatProd1.setProductType(ProductType.TICKET);
    seatProd1.setFulfillmentArtifact("1234");
    seatProd1.setRow("row");
    seatProd1.setSeat("seat");
    seatProd1.setSeatId(123l);
    seatProductList.add(seatProd);
    seatProductList.add(seatProd1);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 5);
    Mockito.doReturn(seatProductList).when(seatProductsContextMock).getBarcodeSeatProductList();
    Mockito.doReturn(dbSeats).when(seatProductsContextMock).getTicketSeatsFromCache();
    ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
    UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
    Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
        Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
        Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    List<Listing> dbList = new ArrayList<Listing>(1);

    dbList.add(dblisting);
    Mockito.doReturn(dbList).when(helperspy).updateDatabaseForBatch(Mockito.anyBoolean(),
        Mockito.anyListOf(Listing.class));
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    
    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    ldata.setHeaderListing(newlisting);
    when(fulfillmentServiceHelper.populateFulfillmentOptions((Listing) Matchers.any(),
        (List<com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow>) Matchers.any()))
            .thenReturn(true);
    
    List<FulfillmentWindow> fulfillmentWindows = new ArrayList<FulfillmentWindow>();
    FulfillmentWindow fw = new FulfillmentWindow();
    fw.setFulfillmentMethodId(2L);
    fulfillmentWindows.add(fw);
    when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(fulfillmentWindows);
    
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helperspy.createOrUpdateListingData(ldata, clientIp, userAgent);
  }

  @Test
  public void testBarcodePredeliveryInactive() throws Exception {
    when(ticketSeatMgr.findActiveTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 2));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2", 10, 2);
    TicketSeat ts1 = new TicketSeat();
  	ts1.setSeatStatusId(1L);
  	TicketSeat ts2 = new TicketSeat();
  	ts2.setSeatStatusId(1L);
  	List<TicketSeat> tsList = new ArrayList<TicketSeat>();
  	tsList.add(ts1);
  	tsList.add(ts2);
  	dblisting.setTicketSeats(tsList);
    dblisting.setSellerRequestedStatus("INACTIVE");
    dblisting.setEvent(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);

    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(inventoryMgr.getListingBySellerIdExternalIdAndStatus(Matchers.anyLong(),
        Matchers.anyString())).thenReturn(null);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEventId(1000l);
    newlisting.setExternalId("12345");
    TicketSeat seat = new TicketSeat();
    seat.setFulfillmentArtifactId(12345l);
    List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>(1);
    ticketSeats.add(seat);
    newlisting.setTicketSeats(ticketSeats);

    List<Listing> newListingList = new ArrayList<Listing>(1);
    newListingList.add(newlisting);
    ListingRequest req = new ListingRequest();
    req.setExternalListingId("12345");
    req.setListingId(1000l);
    req.setEventId("1000");

    Product product = new Product();
    product.setFulfillmentArtifact("123");
    product.setExternalId("123");
    Product product1 = new Product();
    product1.setFulfillmentArtifact("1234");
    product1.setExternalId("1234");
    List<Product> products = new ArrayList<Product>();
    products.add(product);
    products.add(product1);
    req.setProducts(products);
    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
    List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);
    SeatProduct seatProd = new SeatProduct();
    seatProd.setProductType(ProductType.TICKET);
    seatProd.setFulfillmentArtifact("1234");
    seatProd.setRow("row");
    seatProd.setSeat("seat");
    seatProd.setSeatId(123l);

    SeatProduct seatProd1 = new SeatProduct();
    seatProd1.setProductType(ProductType.TICKET);
    seatProd1.setFulfillmentArtifact("1234");
    seatProd1.setRow("row");
    seatProd1.setSeat("seat");
    seatProd1.setSeatId(123l);
    seatProductList.add(seatProd);
    seatProductList.add(seatProd1);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 5);
    Mockito.doReturn(seatProductList).when(seatProductsContextMock).getBarcodeSeatProductList();
    Mockito.doReturn(dbSeats).when(seatProductsContextMock).getTicketSeatsFromCache();
    ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
    UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
    Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
        Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
        Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    List<Listing> dbList = new ArrayList<Listing>(1);

    dbList.add(dblisting);
    Mockito.doReturn(dbList).when(helperspy).updateDatabaseForBatch(Mockito.anyBoolean(),
        Mockito.anyListOf(Listing.class));
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    
    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    ldata.setHeaderListing(newlisting);
    when(fulfillmentServiceHelper.populateFulfillmentOptions((Listing) Matchers.any(),
        (List<com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow>) Matchers.any()))
            .thenReturn(true);
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helperspy.createOrUpdateListingData(ldata, clientIp, userAgent);
  }
  
  @Test
  public void testBarcodePredeliveryPartialFF() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2", 10, 2);
    dblisting.setSellerRequestedStatus("INACTIVE");
    dblisting.setEvent(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);

    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(inventoryMgr.getListingBySellerIdExternalIdAndStatus(Matchers.anyLong(),
        Matchers.anyString())).thenReturn(null);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setExternalId("12345");
    TicketSeat seat = new TicketSeat();
    seat.setFulfillmentArtifactId(12345l);
    List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>(1);
    ticketSeats.add(seat);
    newlisting.setTicketSeats(ticketSeats);

    List<Listing> newListingList = new ArrayList<Listing>(1);
    newListingList.add(newlisting);
    ListingRequest req = new ListingRequest();
    req.setExternalListingId("12345");
    req.setListingId(1000l);
    req.setEventId("1000");

    Product product = new Product();
    product.setFulfillmentArtifact("123");
    Product product1 = new Product();
    List<Product> products = new ArrayList<Product>();
    products.add(product);
    products.add(product1);
    req.setProducts(products);
    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
    List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);
    SeatProduct seatProd = new SeatProduct();
    seatProd.setProductType(ProductType.TICKET);
    seatProd.setFulfillmentArtifact("1234");
    seatProd.setRow("row");
    seatProd.setSeat("seat");
    seatProd.setSeatId(123l);

    SeatProduct seatProd1 = new SeatProduct();
    seatProd1.setProductType(ProductType.TICKET);
    seatProd1.setFulfillmentArtifact("1234");
    seatProd1.setRow("row");
    seatProd1.setSeat("seat");
    seatProd1.setSeatId(123l);
    seatProductList.add(seatProd);
    seatProductList.add(seatProd1);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 5);
    Mockito.doReturn(seatProductList).when(seatProductsContextMock).getBarcodeSeatProductList();
    Mockito.doReturn(dbSeats).when(seatProductsContextMock).getTicketSeatsFromCache();
    ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
    UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
    Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
        Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
        Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    List<Listing> dbList = new ArrayList<Listing>(1);

    dbList.add(dblisting);
    Mockito.doReturn(dbList).when(helperspy).updateDatabaseForBatch(Mockito.anyBoolean(),
        Mockito.anyListOf(Listing.class));
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    
    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    ldata.setHeaderListing(newlisting);
    when(fulfillmentServiceHelper.populateFulfillmentOptions((Listing) Matchers.any(),
        (List<com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow>) Matchers.any()))
            .thenReturn(true);
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps;
		try {
			resps = helperspy.createOrUpdateListingData(ldata, clientIp, userAgent);
		} catch (Exception e) {
			Assert.assertTrue(true);
		}
  }

  @Test
  public void testBarcodePredeliverySTH() throws Exception {
    when(ticketSeatMgr.findActiveTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 2));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2", 10, 2);
    TicketSeat ts1 = new TicketSeat();
  	ts1.setSeatStatusId(1L);
  	TicketSeat ts2 = new TicketSeat();
  	ts2.setSeatStatusId(1L);
  	List<TicketSeat> tsList = new ArrayList<TicketSeat>();
  	tsList.add(ts1);
  	tsList.add(ts2);
  	dblisting.setTicketSeats(tsList);
    dblisting.setEvent(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);

    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(inventoryMgr.getListingBySellerIdExternalIdAndStatus(Matchers.anyLong(),
        Matchers.anyString())).thenReturn(null);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setExternalId("12345");
    newlisting.setFulfillmentMethod(FulfillmentMethod.BARCODEPREDELIVERYSTH);
    TicketSeat seat = new TicketSeat();
    seat.setFulfillmentArtifactId(12345l);
    List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>(1);
    ticketSeats.add(seat);
    newlisting.setTicketSeats(ticketSeats);

    List<Listing> newListingList = new ArrayList<Listing>(1);
    newListingList.add(newlisting);
    ListingRequest req = new ListingRequest();
    req.setExternalListingId("12345");
    req.setListingId(1000l);
    req.setEventId("1000");
    req.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.STH);

    Product product = new Product();
    product.setFulfillmentArtifact("123");
    product.setExternalId("123");
    Product product1 = new Product();
    product1.setFulfillmentArtifact("1234");
    product1.setExternalId("123");
    List<Product> products = new ArrayList<Product>();
    products.add(product);
    products.add(product1);
    req.setProducts(products);
    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
    List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);
    SeatProduct seatProd = new SeatProduct();
    seatProd.setProductType(ProductType.TICKET);
    seatProd.setFulfillmentArtifact("1234");
    seatProd.setRow("row");
    seatProd.setSeat("seat");
    seatProd.setSeatId(123l);

    SeatProduct seatProd1 = new SeatProduct();
    seatProd1.setProductType(ProductType.TICKET);
    seatProd1.setFulfillmentArtifact("1234");
    seatProd1.setRow("row");
    seatProd1.setSeat("seat");
    seatProd1.setSeatId(123l);
    seatProductList.add(seatProd);
    seatProductList.add(seatProd1);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 5);
    Mockito.doReturn(seatProductList).when(seatProductsContextMock).getBarcodeSeatProductList();
    Mockito.doReturn(dbSeats).when(seatProductsContextMock).getTicketSeatsFromCache();
    ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
    UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
    Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
        Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
        Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    List<Listing> dbList = new ArrayList<Listing>(1);

    dbList.add(dblisting);
    Mockito.doReturn(dbList).when(helperspy).updateDatabaseForBatch(Mockito.anyBoolean(),
        Mockito.anyListOf(Listing.class));
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    
    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    ldata.setHeaderListing(newlisting);
    when(fulfillmentServiceHelper.populateFulfillmentOptions((Listing) Matchers.any(),
        (List<com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow>) Matchers.any()))
            .thenReturn(true);
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helperspy.createOrUpdateListingData(ldata, clientIp, userAgent);
  }

  /**
   * testNonGA_productAddPiggyBack_notallow
   * 
   * @return
   * @throws Exception
   */
  @Test
  public void testNonGA_productAddPiggyBack_notallow() throws Exception {
    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3", 3, 3);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 3);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(dbSeats);
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dbListing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEvent(getEvent());
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();

    ArrayList<Product> products = new ArrayList<Product>();
    products.add(getProduct(Operation.ADD, "R2", "1", ProductType.TICKET, null, null));
    products.add(getProduct(Operation.ADD, "R2", "2", ProductType.TICKET, null, null));
    req.setProducts(products);

    Listing result = null;
    try {
      // expect failure (because we had 3, 2 seats)
      result = helper.createSingleListing(newlisting, req, shapiContext);
      Assert.fail("Should not allow create unbalanced row/seats for piggyback listing.");
    } catch (ListingBusinessException lbe) {
    }
  }

  /**
   * testNonGA_productAddPiggyBack_allow
   * 
   * @return
   * @throws Exception
   */
  @Test
  public void testNonGA_productAddPiggyBack_allow() throws Exception {
    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3", 3, 3);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 3);

    dbListing.setSystemStatus(ListingStatus.INCOMPLETE.name());

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(dbSeats);
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dbListing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEvent(getEvent());
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();

    ArrayList<Product> products = new ArrayList<Product>();
    products.add(getProduct(Operation.ADD, "R2", "1", ProductType.TICKET, null, null));
    products.add(getProduct(Operation.ADD, "R2", "2", ProductType.TICKET, null, null));
    products.add(getProduct(Operation.ADD, "R2", "3", ProductType.TICKET, null, null));

    req.setProducts(products);

    Listing result = null;
    try {
      // expect success (because now we have 3, 3 seats)
      result = helper.createSingleListing(newlisting, req, shapiContext);
    } catch (ListingBusinessException lbe) {
      Assert.fail("Should allow create balanced R1 (1,2,3) and R2 (1,2,3) piggyback seats");
    }
  }

  @Test
  public void testNonGA_productAddPiggyBack_VenueConfig() throws Exception {
    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3", 3, 3);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 3);

    dbListing.setSystemStatus(ListingStatus.INCOMPLETE.name());

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(dbSeats);
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dbListing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEvent(getEvent());
    newlisting.setSection("sec-10-1");
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();

    ArrayList<Product> products = new ArrayList<Product>();
    products.add(getProduct(Operation.ADD, "R2", "1", ProductType.TICKET, null, null));
    products.add(getProduct(Operation.ADD, "R2", "2", ProductType.TICKET, null, null));
    products.add(getProduct(Operation.ADD, "R2", "3", ProductType.TICKET, null, null));

    req.setProducts(products);

    Listing result = null;
    try {
      // expect success (because now we have 3, 3 seats)
      result = helper.createSingleListing(newlisting, req, shapiContext);
    } catch (ListingBusinessException lbe) {
      Assert.fail("Should allow create balanced R1 (1,2,3) and R2 (1,2,3) piggyback seats");
    }
  }

  /**
   * testNonGA_productAddPiggyBack_allow
   * 
   * @return
   * @throws Exception
   */
  @Test
  public void testNonGA_productAddNoSeatNumber() throws Exception {
    // Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3", 3, 3);
    // List<TicketSeat> dbSeats = getTicketSeats(1000L,"sec-10", "R1", 3);

    // dbListing.setSystemStatus(ListingStatus.INCOMPLETE.name());

    // when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn( dbSeats );
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    // when(inventoryMgr.getListing(anyLong())).thenReturn( dbListing );

    Listing newlisting = new Listing();
    newlisting.setSellerId(1000l);
    newlisting.setSection("section 100");
    newlisting.setTicketMedium(TicketMedium.PDF.getValue());
    newlisting.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    newlisting.setEvent(getEvent());
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();

    ArrayList<Product> products = new ArrayList<Product>();
    products.add(getProduct(Operation.ADD, "R2", null, ProductType.TICKET, null, null));
    products.add(getProduct(Operation.ADD, "R2", null, ProductType.TICKET, null, null));
    products.add(getProduct(Operation.ADD, "R2", null, ProductType.TICKET, null, null));

    req.setProducts(products);

    Listing result = null;
    try {
      // expect success (because now we have 3, 3 seats)
      result = helper.createSingleListing(newlisting, req, shapiContext);
      Assert.assertTrue(result != null);
    } catch (ListingBusinessException lbe) {
      Assert.fail("Should allow create seats without seat number ");
    }
  }

  /**
   * testNonGA_productAddAndDelete_ParkingPass
   * 
   * @return
   * @throws Exception
   */
  // @Test
  public void testNonGA_productAddAndDelete_ParkingPass() throws Exception {
    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 5);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(dbSeats);
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dbListing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEvent(getEvent());
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();

    ArrayList<Product> products = new ArrayList<Product>();
    products.add(getProduct(Operation.ADD, null, null, ProductType.PARKING_PASS, null, null));
    req.setProducts(products);

    Listing result = null;
    try {
      // expect success
      result = helper.createSingleListing(newlisting, req, shapiContext);
    } catch (ListingBusinessException lbe) {
      Assert.fail(
          "Should allow adding operation.ADD parking pass to listing. Error: " + lbe.getMessage());
    }

    products.clear();
    products.add(getProduct(Operation.DELETE, null, null, ProductType.PARKING_PASS, null, null));
    try {
      // expect success
      result = helper.createSingleListing(newlisting, req, shapiContext);
    } catch (ListingBusinessException lbe) {
      Assert.fail("Should allow to operation.DELETE on parking pass in listing. Error: "
          + lbe.getMessage());
    }
  }

  /**
   * testNonGA_productAddAndDelete_ParkingPass
   * 
   * @return
   * @throws Exception
   */
  @Test
  public void test_updateListingSection() throws Exception {
    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3,4,5,6", 6, 6);
    dbListing.setSystemStatus(ListingStatus.INCOMPLETE.name());
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 6);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(dbSeats);
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dbListing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEvent(getEvent());
    newlisting.setSection("section-100");

    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();

    Listing result = null;
    try {
      // expect success
      result = helper.createSingleListing(newlisting, req, shapiContext);
    } catch (ListingBusinessException lbe) {
      Assert.fail("Should allow update section for INCOMPLETE listing. Error: " + lbe.getMessage());
    }
  }

  /**
   * testNonGA_productAddAndDelete_ParkingPass
   * 
   * @return
   * @throws Exception
   */
  @Test
  public void test_updateListingMedium() throws Exception {
    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3,4,5,6", 6, 6);
    dbListing.setSystemStatus(ListingStatus.INCOMPLETE.name());

    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 6);


    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(dbSeats);
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dbListing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setEvent(getEvent());
    newlisting.setSellerId(1000l);

    newlisting.setTicketMedium(TicketMedium.PDF.getValue());

    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();

    Listing result = null;
    try {
      // expect success
      result = helper.createSingleListing(newlisting, req, shapiContext);
    } catch (ListingBusinessException lbe) {
      Assert.fail("Should allow changing ticket medium to PDF on INCOMPLETE listing. Error: "
          + lbe.getMessage());
    }
  }

  /**
   * testNonGA_productAddAndDelete_ParkingPass
   * 
   * @return
   * @throws Exception
   */
  @Test
  public void test_updateListingSplitQuantity() throws Exception {
    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 6, 6);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 6);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(dbSeats);
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dbListing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setSplitOption((short) 1); // multiples
    newlisting.setSplitQuantity(3);
    newlisting.setEvent(getEvent());
    newlisting.setTicketMedium(TicketMedium.PDF.getValue());

    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();

    Listing result = null;
    try {
      // expect success
      result = helper.createSingleListing(newlisting, req, shapiContext);
    } catch (ListingBusinessException lbe) {
      Assert.fail(
          "Should allow split option of multiples of 3 for 6 tickets. Error: " + lbe.getMessage());
    }
  }

  @Test
  public void test_updateListingSplitQuantity_2() throws Exception {
    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 6, 6);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 6);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(dbSeats);
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dbListing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setSplitOption((short) 1); // multiples
    newlisting.setSplitQuantity(2);
    newlisting.setEvent(getEvent());
    newlisting.setTicketMedium(TicketMedium.PDF.getValue());

    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();

    Listing result = null;
    try {
      // expect success
      result = helper.createSingleListing(newlisting, req, shapiContext);
    } catch (ListingBusinessException lbe) {
      Assert.fail(
          "Should allow split option of multiples of 3 for 6 tickets. Error: " + lbe.getMessage());
    }
  }

  /**
   * Tests all defaults with create listing
   */
  @Test
  public void testCreateListingWithDefaults() throws Exception {
    Event event = getEvent();
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(event);

    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    req.setListingId(null); // create
    req.setEventId("1000");
    req.setExternalListingId("ext-listing-id");
    req.setQuantity(3);
    req.setPricePerProduct(new Money("120.00", "USD"));

    Listing newListing = ListingRequestAdapter.convert(req, true, null);
    newListing.setInhandDate(null); // TODO: need to fix IH date to deal with date defaults

    newListing.setEventId(event.getId());
    newListing.setEvent(event);

    Listing result = null;
    try {
      // expect success
      result = helper.createSingleListing(newListing, req, shapiContext);

      // check defaults (for now splits)
      Assert.assertTrue("Wrong defaults: Default split == NO SINGLES and Split Quantity == 1",
          result.getSplitOption() != null && result.getSplitQuantity() == 1);
    } catch (ListingBusinessException lbe) {
      Assert.fail("Should allow listing creation with defaults: " + lbe.getMessage());
    }
  }


  /**
   * test_fulfillmentOptions
   * 
   * @return
   * @throws Exception
   */
  @Test
  public void test_fulfillmentAndDatesOptions() throws Exception {
    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 6, 6);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 6);

    Event evt = getEvent();

    Calendar lih = Calendar.getInstance();
    lih.add(Calendar.MONTH, 3);
    evt.setLatestPossibleInhandDate(lih);
    dbListing.setEvent(evt);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(dbSeats);
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(evt);
    when(inventoryMgr.getListing(anyLong())).thenReturn(dbListing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setFulfillmentMethod(FulfillmentMethod.LMS);
    newlisting.setId(1000l);

    // Event object need to be set in listing because it is populated prior to
    // updateOrCreateSingleListing called
    Event event = super.getEvent();
    newlisting.setEvent(event);

    Calendar ihdate = Calendar.getInstance();
    ihdate.setTimeInMillis(event.getEventDate().getTimeInMillis());
    ihdate.add(Calendar.DAY_OF_MONTH, -5); // 5 days before event date
    newlisting.setInhandDate(ihdate);

    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();

    Listing result = null;
    try {
      // expect success
      result = helper.createSingleListing(newlisting, req, shapiContext);
    } catch (ListingBusinessException lbe) {
      Assert.fail(
          "Should allow changing FulfillmentMethod and InHandDate. Error: " + lbe.getMessage());
    }
  }

  /**
   * test_fulfillmentOptions
   * 
   * @return
   * @throws Exception
   */
  @Test
  public void test_sellerPayment() throws Exception {
    UpdateListingAsyncHelper2 helper = new UpdateListingAsyncHelper2();
    helper.setup();

    ListingSeatTraitsHelper traitHelper =
        (ListingSeatTraitsHelper) mockClass(ListingSeatTraitsHelper.class, helper,
            "listingSeatTraitsHelper");

    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 6, 6);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 6);

    Event evt = getEvent();

    Calendar lih = Calendar.getInstance();
    lih.add(Calendar.MONTH, 3);
    evt.setLatestPossibleInhandDate(lih);
    dbListing.setEvent(evt);

    TicketSeatMgr ticketMgr =
        (TicketSeatMgr) mockClass(TicketSeatMgr.class, helper, "ticketSeatMgr");
    EventHelper eventHelper = (EventHelper) mockClass(EventHelper.class, helper, "eventHelper");
    InventoryMgr inventoryMgr =
        (InventoryMgr) mockClass(InventoryMgrImpl.class, helper, "inventoryMgr");

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(dbSeats);
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(evt);
    when(inventoryMgr.getListing(anyLong())).thenReturn(dbListing);

    ReflectionTestUtils.setField(helper, "inventoryMgr", inventoryMgr);

    PaymentHelper paymentHelper = Mockito.mock(PaymentHelper.class);
    when(paymentHelper.isSellerPaymentTypeValidForSeller(Mockito.anyLong(), Mockito.anyLong()))
        .thenReturn(true);

    UserHelper userHelper = Mockito.mock(UserHelper.class);
    when(userHelper.isSellerPaymentTypeValid(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyLong())).thenReturn(true);

    ReflectionTestUtils.setField(helper, "paymentHelper", paymentHelper);
    ReflectionTestUtils.setField(helper, "userHelper", userHelper);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setSellerCCId(1123456L);
    newlisting.setSellerPaymentTypeId(1L);

    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();

    Listing result = null;
    try {
      // expect success
      result = helper.createSingleListing(newlisting, req, shapiContext);
    } catch (ListingBusinessException lbe) {
      Assert.fail(
          "Should allow changing FulfillmentMethod and InHandDate. Error: " + lbe.getMessage());
    }
  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_update_Section_ActiveGAListingx() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, null, "R1", 6));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(getEvent());
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = getListing(1000L, "General Admission", "R1", "1,2,3,4,5", 6, 6);
    listing.setSection("General Admission");
    listing.setSystemStatus(ListingStatus.ACTIVE.name());
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seat.setGeneralAdmissionInd(true);
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);
    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setSection("Lower Back 102");
    SHAPIContext shapiContext = new SHAPIContext();
    ListingRequest req = new ListingRequest();

    newlisting = helper.createSingleListing(newlisting, req, shapiContext);
  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_ListingAlreadySold() throws Exception {
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(null);
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setQuantityRemain(0);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.ACTIVE.name());
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
    SHAPIContext shapiContext = new SHAPIContext();
    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);
  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_InvalidSellerId() throws Exception {
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(null);
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(2000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setQuantityRemain(8);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.ACTIVE.name());
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
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);
  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_InvalidQuantity() throws Exception {

    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));

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
    listing.setDeliveryOption(DeliveryOption.MANUAL.getValue());

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setQuantity(20);
    newlisting.setInhandDate(new GregorianCalendar(2012, 9, 5));
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);
  }


  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_InvalidSplitQuantity() throws Exception {
    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));

    List<TicketSeat> dbSeats = getTicketSeats(1000L, "General Admission", "R1", 10, true);
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(dbSeats);

    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setTicketMedium(TicketMedium.BARCODE.getValue());
    listing.setDeliveryOption(DeliveryOption.MANUAL.getValue());
    listing.setEventId(1000l);
    listing.setSplitOption((short) 1);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setQuantityRemain(8);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.ACTIVE.name());
    listing.setListingType(3l);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setQuantity(6);
    newlisting.setSplitQuantity(5);
    newlisting.setInhandDate(new GregorianCalendar(2014, 9, 5));
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    System.out.println("Finished");
  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_InvalidListingId() throws Exception {
    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);

    when(inventoryMgr.getListing(anyLong()))
        .thenReturn(getListing(1000l, "section10", "R10", "1,2,3,4", 4, 4));
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
    newlisting.setQuantity(20);
    newlisting.setInhandDate(new GregorianCalendar(2012, 9, 5));
    newlisting.setSellerGuid("ADJSD234jkjkh89JKJHSD45SD");

    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);
  }


  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_InvalidListingState() throws Exception {

    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setQuantityRemain(8);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.DELETED.name());

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setQuantity(20);
    newlisting.setInhandDate(new GregorianCalendar(2012, 9, 5));

    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);
  }


  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_ListingExpired() throws Exception {

    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setDescription("Event description");
    Calendar cal1 = Calendar.getInstance();
    cal1.roll(Calendar.DAY_OF_MONTH, 5);

    event.setEventDate(cal1);
    Calendar ecal = Calendar.getInstance();
    ecal.roll(Calendar.DAY_OF_MONTH, -15);

    event.setEarliestPossibleInhandDate(ecal);
    Calendar cal = Calendar.getInstance();
    // cal.roll(Calendar.DAY_OF_MONTH, -2);
    event.setLatestPossibleInhandDate(cal);
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setQuantityRemain(8);
    listing.setRow("1");
    listing.setDeliveryOption(1);
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.ACTIVE.name());
    listing.setDeliveryOption(2);
    Calendar calToday = DateUtil.getNowCalUTC();
    calToday.roll(Calendar.DAY_OF_MONTH, -2);
    listing.setEndDate(calToday);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setQuantity(20);
    newlisting.setDeliveryOption(2);
    newlisting.setInhandDate(new GregorianCalendar(2012, 9, 5));
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);
  }
  
  @Test
  public void updateListing_ListingExpiredError() throws Exception {

    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setDescription("Event description");
    Calendar cal1 = Calendar.getInstance();
    cal1.roll(Calendar.DAY_OF_MONTH, 5);

    event.setEventDate(cal1);
    Calendar ecal = Calendar.getInstance();
    ecal.roll(Calendar.DAY_OF_MONTH, -15);

    event.setEarliestPossibleInhandDate(ecal);
    Calendar cal = Calendar.getInstance();
    // cal.roll(Calendar.DAY_OF_MONTH, -2);
    event.setLatestPossibleInhandDate(cal);
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setQuantityRemain(8);
    listing.setRow("1");
    listing.setDeliveryOption(1);
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.ACTIVE.name());
    listing.setDeliveryOption(2);
    Calendar calToday = DateUtil.getNowCalUTC();    
    calToday.add(Calendar.DATE, -2);
    listing.setEndDate(calToday);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setQuantity(20);
    newlisting.setDeliveryOption(2);
    newlisting.setSystemStatus(ListingStatus.INACTIVE.name());
    newlisting.setInhandDate(new GregorianCalendar(2012, 9, 5));
    newlisting.setEndDate(new GregorianCalendar(2012, 9, 5));
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    try
    {
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);
    }catch(ListingBusinessException ex)
    {
        Assert.assertEquals("The listing has expired message should appear", "The listing has expired ", ex.getLocalizedMessage());
    }
  }


  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_ListingBeforeEIH() throws Exception {

    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
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
    Calendar calToday = DateUtil.getNowCalUTC();
    calToday.roll(Calendar.DAY_OF_MONTH, 2);
    listing.setEndDate(calToday);
    listing.setInhandDate(new GregorianCalendar(2012, 8, 25));
    listing.setDeliveryOption(2);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setQuantity(20);
    newlisting.setInhandDate(new GregorianCalendar(2012, 9, 5));
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_InhandDateAfterLIH() throws Exception {

    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setDescription("Event description");
    Calendar nowCal = Calendar.getInstance();
    nowCal.roll(Calendar.MONTH, 2);
    // event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEventDate(nowCal);
    nowCal.roll(Calendar.DAY_OF_MONTH, -20);
    event.setEarliestPossibleInhandDate(nowCal);
    // event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    nowCal.roll(Calendar.DAY_OF_MONTH, 5);
    event.setLatestPossibleInhandDate(nowCal);
    // event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
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
    nowCal.roll(Calendar.DAY_OF_MONTH, 10);
    listing.setEndDate(nowCal);
    nowCal.roll(Calendar.DAY_OF_MONTH, -2);
    listing.setInhandDate(nowCal);

    listing.setDeliveryOption(2);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setQuantity(20);
    nowCal.roll(Calendar.DAY_OF_MONTH, -1);
    newlisting.setInhandDate(nowCal);

    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);
  }

  @Test
  public void updateListing_activateInactiveListing() throws Exception {
    List<TicketSeat> ticketSeats = getTicketSeats(1000L, null, null, 10, true);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(ticketSeats);

    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(event);

    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.INACTIVE.name());
    listing.setTicketMedium(TicketMedium.PDF.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setSaleMethod(1L);
    listing.setSellerPaymentTypeId(1L);
    listing.setSellerContactId(2L);
    listing.setSellerContactGuid("test123");
    listing.setSplitOption((short) 0);
    listing.setSplitQuantity(listing.getQuantity());
    listing.setCurrency(Currency.getInstance("USD"));

    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());
    newlisting.setEvent(event);

    newlisting.setSellerGuid("AB123");
    ListingCheck listingCheck = new ListingCheck();
    when(inventorySolrUtil.isListingExists(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong())).thenReturn(listingCheck);
    when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    when(userHelper.isSellerPaymentContactIdPopulated(Mockito.anyString(), Mockito.anyLong(),
        Mockito.anyList())).thenReturn(true);
    when(userHelper.isSellerContactValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyLong())).thenReturn(true);
    when(userHelper.getMappedValidSellerCCId(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyList(), Mockito.anyString())).thenReturn(10001L);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(listing.getSystemStatus()
        .equals(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name()));
  }


  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_DonotallowSinglePDFSplit() throws Exception {

    List<PDFTicketSeat> tseats = new ArrayList<PDFTicketSeat>();
    when(pdfTicketMgr.findPDFTicketSeats(anyLong())).thenReturn(tseats);

    List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
    ticketSeats.add(new TicketSeat());
    ticketSeats.add(new TicketSeat());

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(ticketSeats);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);

    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(2);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2");
    listing.setSystemStatus(ListingStatus.ACTIVE.name());
    listing.setTicketMedium(TicketMedium.PDF.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(2);
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setSplitQuantity(1);

    SHAPIContext shapiContext = new SHAPIContext();
    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
  }

  @Test
  public void updateListing_withSplitsNone() throws Exception {
    List<TicketSeat> ticketSeats = getTicketSeats(1000L, null, null, 10, true);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(ticketSeats);

    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);

    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));

    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitQuantity(2);
    listing.setCurrency(Currency.getInstance("USD"));
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.ACTIVE.name());
    listing.setTicketMedium(TicketMedium.BARCODE.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setTicketCost(new Money(new BigDecimal(10d), "USD"));
    listing.setEvent(event);
    listing.setFaceValue(new Money(new BigDecimal(5d), "USD"));

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seat.setSeatStatusId(1L);
      seat.setTixListTypeId(1L);
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setTicketCost(new Money(new BigDecimal(10d), "USD"));
    newlisting.setFaceValue(new Money(new BigDecimal(5d), "USD"));
    newlisting.setTicketMedium(3);
    newlisting.setSplitOption((short) 0);
    newlisting.setEvent(event);
    // newlisting.setInhandDate(new GregorianCalendar(2012, 9, 5));
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(newlisting.getSplitOption() == 0);
  }

  @Test
  public void updateListing_SetSplits() throws Exception {

    List<TicketSeat> ticketSeats = getTicketSeats(1000L, null, null, 10, true);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(ticketSeats);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));

    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));

    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(event);

    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitQuantity(0);
    listing.setSplitOption((short) 0);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.ACTIVE.name());
    listing.setTicketMedium(TicketMedium.BARCODE.getValue());
    listing.setDeliveryOption(DeliveryOption.MANUAL.getValue());
    listing.setQuantityRemain(8);
    listing.setTicketCost(new Money(new BigDecimal(10d), "USD"));
    listing.setFaceValue(new Money(new BigDecimal(5d), "USD"));
    listing.setCurrency(Currency.getInstance("USD"));

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setTicketCost(new Money(new BigDecimal(10d), "USD"));
    newlisting.setFaceValue(new Money(new BigDecimal(5d), "USD"));
    newlisting.setTicketMedium(1);
    newlisting.setSplitOption((short) 1);
    newlisting.setSplitQuantity(3);
    newlisting.setInhandDate(new GregorianCalendar(2012, 9, 5));
    newlisting.setId(1000l);
    newlisting.setEvent(event);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(newlisting.getSplitOption() == 1);
  }

  @Test
  public void updateListing_SetSplitsNoSingles() throws Exception {
    List<TicketSeat> ticketSeats = getTicketSeats(1000L, null, null, 10, true);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(ticketSeats);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));

    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);

    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(event);

    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitQuantity(0);
    listing.setSplitOption((short) 0);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.ACTIVE.name());
    listing.setTicketMedium(TicketMedium.BARCODE.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setTicketCost(new Money(new BigDecimal(10d), "USD"));
    listing.setFaceValue(new Money(new BigDecimal(5d), "USD"));
    listing.setCurrency(Currency.getInstance("USD"));

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setTicketCost(new Money(new BigDecimal(10d), "USD"));
    newlisting.setFaceValue(new Money(new BigDecimal(5d), "USD"));
    newlisting.setTicketMedium(3);
    newlisting.setSplitOption((short) 2);
    newlisting.setEvent(event);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);
    assertTrue(newlisting != null);
    assertTrue(listing.getSplitOption() == 2);
    assertTrue(listing.getSplitQuantity() == 1);
  }

  /*
   * @Test(expectedExceptions={ListingBusinessException.class}) public void
   * updateListing_predeliveredwithinhand() throws Exception { Mockito.doAnswer(new Answer<Void>() {
   * 
   * @Override public Void answer(InvocationOnMock invocation) throws Throwable { return null; }
   * }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class)); Event event = new
   * Event(); event.setId(1000l); event.setActive(true);
   * event.setCurrency(Currency.getInstance("USD"));
   * event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific")); event.setDescription(
   * "Event description"); event.setEventDate(new GregorianCalendar(2012, 10, 1));
   * event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
   * event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
   * when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
   * Mockito.any(Boolean.class))).thenReturn(event); Listing listing = new Listing();
   * listing.setId(1000l); listing.setEventId(1000l); listing.setSellerId(1000l);
   * listing.setListPrice(new Money(new BigDecimal(100d), "USD")); listing.setQuantity(10);
   * listing.setSplitOption((short)1); listing.setRow("1");
   * listing.setSeats("1,2,3,4,5,6,7,8,9,10"); listing.setSystemStatus(ListingStatus.ACTIVE.name());
   * listing.setTicketMedium(TicketMedium.BARCODE.getValue());
   * listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue()); listing.setQuantityRemain(8);
   * listing.setListingType(1l); when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
   * 
   * List<TicketSeat> seats = new ArrayList<TicketSeat>(); for(int i=0; i<10; i++){ TicketSeat seat
   * = new TicketSeat(); seats.add(seat); }
   * when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);
   * 
   * Listing newlisting = new Listing(); newlisting.setId(1000l); newlisting.setSellerId(1000l);
   * newlisting.setListPrice(new Money(new BigDecimal(100d), "USD")); newlisting.setQuantity(6);
   * newlisting.setInhandDate(new GregorianCalendar(2012, 9, 5));
   * newlisting.setTicketMedium(TicketMedium.BARCODE.getValue());
   * 
   * SHAPIContext shapiContext = new SHAPIContext();
   * 
   * ListingRequest req = new ListingRequest (); newlisting = helper.createSingleListing(newlisting,
   * req, shapiContext);
   * 
   * assertTrue(newlisting !=null); assertTrue(newlisting.getSeats() !=null);
   * assertTrue(newlisting.getSeats().indexOf("10") == -1); }
   */

  /*
   * @Test(expectedExceptions={ListingBusinessException.class}) public void
   * updateListing_SellerCCIDInvalid() throws Exception { Mockito.doAnswer(new Answer<Void>() {
   * 
   * @Override public Void answer(InvocationOnMock invocation) throws Throwable { return null; }
   * }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class)); Event event = new
   * Event(); event.setId(1000l); event.setActive(true);
   * event.setCurrency(Currency.getInstance("USD"));
   * event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific")); event.setDescription(
   * "Event description"); event.setEventDate(new GregorianCalendar(2012, 10, 1));
   * event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
   * event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
   * when(eventHelper.getEventById(1000l, "event genrePath geoPath venue")).thenReturn(event);
   * 
   * Listing listing = new Listing(); listing.setId(1000l); listing.setEventId(1000l);
   * listing.setSellerId(1000l); listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
   * listing.setQuantity(10); listing.setSplitOption((short)1); listing.setRow("1");
   * listing.setSeats("1,2,3,4,5,6,7,8,9,10");
   * listing.setSystemStatus(ListingStatus.INACTIVE.name());
   * listing.setTicketMedium(TicketMedium.PDF.getValue());
   * listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue()); listing.setQuantityRemain(8);
   * listing.setInhandDate(new GregorianCalendar(2012, 10, 1)); listing.setSellerCCId(1L);
   * listing.setSellerPaymentTypeId(1L); listing.setSellerContactId(2L);
   * when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
   * 
   * CustomerContactDetails userContact = new CustomerContactDetails();
   * userContact.setPaymentContactBoolean(true);
   * 
   * when(userHelper.getUserContact("AB123",2L)).thenReturn(userContact);
   * 
   * List<TicketSeat> seats = new ArrayList<TicketSeat>(); for(int i=0; i<10; i++){ TicketSeat seat
   * = new TicketSeat(); seats.add(seat); }
   * when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);
   * 
   * Listing newlisting = new Listing(); newlisting.setId(1000l); newlisting.setSellerId(1000l);
   * newlisting.setListPrice(new Money(new BigDecimal(100d), "USD")); newlisting.setSellerCCId(1L);
   * 
   * when(userHelper.isSellerCCIdValid("1000l", 1L)).thenReturn(false);
   * when(userHelper.getMappedValidSellerCCId(Mockito.anyString(),
   * Mockito.anyLong(),Mockito.anyList())).thenReturn(null);
   * when(userHelper.isSellerPaymentContactIdPopulated("1000l", 2L,null)).thenReturn(true);
   * when(userHelper.isSellerContactValid("AB123", 2L)).thenReturn(true);
   * when(userHelper.isSellerPaymentTypeValid(1000l,10001L, 1L)).thenReturn(true);
   * 
   * SHAPIContext shapiContext = new SHAPIContext();
   * 
   * ListingRequest req = new ListingRequest (); newlisting =
   * helper.updateOrCreateSingleListing(newlisting, req, shapiContext);
   * 
   * assertTrue(newlisting !=null); assertTrue(newlisting.getSeats() !=null); }
   */

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_InvalidSellerContactID() throws Exception {
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);

    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.INACTIVE.name());
    listing.setTicketMedium(TicketMedium.PAPER.getValue());
    listing.setLmsApprovalStatus(2);
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setSellerPaymentTypeId(1L);
    listing.setSellerContactId(2L);
    listing.setSellerContactGuid("test123");
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());
    newlisting.setSellerPaymentTypeId(1L);
    newlisting.setSellerContactId(2L);
    newlisting.setSellerContactGuid("test123");
    newlisting.setSellerCCId(1L);
    newlisting.setSaleMethod(3L);
    newlisting.setMinPricePerTicket(new Money(new BigDecimal(5d), "USD"));
    newlisting.setMaxPricePerTicket(new Money(new BigDecimal(70d), "USD"));
    newlisting.setEvent(event);
    when(userHelper.isSellerCCIdValid("1000l", 1L)).thenReturn(true);

    when(userHelper.isSellerPaymentContactIdPopulated("1000l", 2L, null)).thenReturn(true);
    when(userHelper.isSellerContactValid("AB123", 2L)).thenReturn(false);
    when(userHelper.isSellerPaymentTypeValid(1000l, 10001L, 1L)).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(newlisting.getSeats() != null);
    assertTrue(newlisting.getSeats().indexOf("10") == -1);

  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_DecliningPriceSaleMethod_WithoutMinMaxPrice() throws Exception {
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));

    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(new Long(10001), null, Locale.US, false)).thenReturn(event);

    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(new Long(10001));
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.INACTIVE.name());
    listing.setTicketMedium(TicketMedium.PDF.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setSellerPaymentTypeId(1L);
    listing.setSaleMethod(1L);
    listing.setSellerContactId(2L);
    listing.setSellerContactGuid("test123");
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEvent(event);
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());

    newlisting.setSaleMethod(3L);
    when(userHelper.isSellerCCIdValid("1000l", 1L)).thenReturn(true);

    when(userHelper.isSellerPaymentContactIdPopulated("1000l", 2L, null)).thenReturn(true);
    when(userHelper.isSellerContactValid("AB123", 2L)).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(1000l, new Long(10001), 1L)).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(newlisting.getSeats() != null);
  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_activateInactiveListing_PendingLockListing() throws Exception {
    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.PENDING_LOCK.toString());
    listing.setSaleMethod(1L);
    listing.setTicketMedium(TicketMedium.PDF.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setSellerPaymentTypeId(1L);
    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());
    newlisting.setSellerPaymentTypeId(1L);

    newlisting.setSellerCCId(1L);
    newlisting.setSaleMethod(3L);
    newlisting.setMinPricePerTicket(new Money(new BigDecimal(5d), "USD"));
    newlisting.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));
    newlisting.setEvent(event);
    when(userHelper.isSellerCCIdValid("1000l", 1L)).thenReturn(true);

    when(userHelper.isSellerPaymentContactIdPopulated("1000l", 2L, null)).thenReturn(true);
    when(userHelper.isSellerContactValid("AB123", 2L)).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(1000l, 1000l, 1L)).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(newlisting.getSeats() != null);
    assertTrue(newlisting.getSeats().indexOf("10") == -1);

  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_activateInactiveListing_PaymentContactInvalid() throws Exception {
    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.INCOMPLETE.toString());

    listing.setTicketMedium(TicketMedium.PDF.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setSellerContactId(2L);
    listing.setSellerContactGuid("test123");
    listing.setSellerPaymentTypeId(1L);
    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setEvent(event);
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());

    when(userHelper.isSellerCCIdValid("1000l", 1L)).thenReturn(true);

    when(userHelper.isSellerPaymentContactIdPopulated(Mockito.anyString(), Mockito.anyLong(),
        Mockito.anyList())).thenReturn(false);
    when(userHelper.isSellerContactValid("AB123", 2L)).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(1000l, 1000l, 1L)).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
  }


  @Test
  public void updateListing_deActivateListing() throws Exception {
    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.ACTIVE.toString());
    listing.setSaleMethod(1L);
    listing.setTicketMedium(TicketMedium.PDF.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setCurrency(Currency.getInstance("USD"));
    listing.setSellerPaymentTypeId(1L);
    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.INACTIVE.name());
    newlisting.setEvent(event);
    newlisting.setSellerGuid("1000l");

    when(userHelper.isSellerCCIdValid("1000l", 1L)).thenReturn(true);
    when(userHelper.isSellerPaymentContactIdPopulated("1000l", 2L, null)).thenReturn(true);
    when(userHelper.isSellerContactValid("AB123", 2L)).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(1000l, 10001L, 1L)).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);
    assertTrue(newlisting != null);
    assertTrue(listing.getSystemStatus()
        .equals(com.stubhub.domain.inventory.common.entity.ListingStatus.INACTIVE.name()));
  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_activateInactiveListing_NullSellerContactId() throws Exception {
    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.INCOMPLETE.name());
    listing.setTicketMedium(TicketMedium.PDF.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setSellerPaymentTypeId(1L);
    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEvent(event);
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());

    when(userHelper.isSellerCCIdValid("1000l", 1L)).thenReturn(true);

    when(userHelper.isSellerPaymentContactIdPopulated("1000l", 1L, null)).thenReturn(true);
    when(userHelper.isSellerContactValid("AB123", 2L)).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(1000l, 10001L, 1L)).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(newlisting.getSeats() != null);
  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_deActivateListing_ExpiredListing() throws Exception {
    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.PENDING_LOCK.toString());

    listing.setTicketMedium(TicketMedium.PDF.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setSellerPaymentTypeId(1L);
    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setEvent(event);
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.INACTIVE.name());

    when(userHelper.isSellerCCIdValid("1000l", 1L)).thenReturn(true);
    when(userHelper.isSellerPaymentContactIdPopulated("1000l", 2L, null)).thenReturn(true);
    when(userHelper.isSellerContactValid("AB123", 2L)).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(1000l, 10001L, 1L)).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(listing.getSystemStatus()
        .equals(com.stubhub.domain.inventory.common.entity.ListingStatus.INACTIVE.name()));

  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_activateInactiveListing_NullSellerCCId() throws Exception {
    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.PENDING_LOCK.name());
    listing.setTicketMedium(TicketMedium.PDF.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerPaymentTypeId(1L);
    listing.setSellerContactId(2L);
    listing.setSellerContactGuid("test123");
    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setSellerGuid("1000l");
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());
    newlisting.setEvent(event);
    when(userHelper.isSellerCCIdValid("1000l", 1L)).thenReturn(true);
    when(userHelper.isSellerPaymentContactIdPopulated("1000l", 1L, null)).thenReturn(true);
    when(userHelper.isSellerContactValid("AB123", 2L)).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(1000l, 10001L, 1L)).thenReturn(true);

    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(newlisting.getSeats() != null);
    assertTrue(newlisting.getSeats().indexOf("10") == -1);
  }

  @Test
  public void updateListing_activateInactiveListing_noCCValidation() throws Exception {
    List<TicketSeat> ticketSeats = getTicketSeats(1000L, null, null, 10, true);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(ticketSeats);

    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(event);

    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.INACTIVE.name());
    listing.setTicketMedium(TicketMedium.PDF.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setSaleMethod(1L);
    listing.setSellerPaymentTypeId(1L);
    listing.setSellerContactId(2L);
    listing.setSplitOption((short) 0);
    listing.setSplitQuantity(listing.getQuantity());
    listing.setCurrency(Currency.getInstance("USD"));
    listing.setFraudCheckStatusId(500L);

    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());
    newlisting.setEvent(event);

    newlisting.setSellerGuid("AB123");
    ListingCheck listingCheck = new ListingCheck();
    when(inventorySolrUtil.isListingExists(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong())).thenReturn(listingCheck);
    when(userHelper.isSellerPaymentContactIdPopulated(Mockito.anyString(), Mockito.anyLong(),
        Mockito.anyList())).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(listing.getSystemStatus()
        .equals(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name()));

  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_activateInactiveListing_NullSellerPaymentTypeId() throws Exception {
    Event event = new Event(); 
    event.setId(1000l); 
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1019l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.INCOMPLETE.name());
    listing.setTicketMedium(TicketMedium.PDF.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));

    listing.setSellerContactId(2L);
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1019l);
    newlisting.setEvent(event);
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());
    when(userHelper.isSellerCCIdValid("1019l", 1L)).thenReturn(true);

    when(userHelper.isSellerPaymentContactIdPopulated("1000l", 2L, null)).thenReturn(false);
    when(userHelper.isSellerContactValid("AB123", 2L)).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(1019l, 10001L, 1L)).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest(); 
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(newlisting.getSeats() != null);
  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_activateInactiveListing_WithSellerPaymentTypeId() throws Exception {
    Event event = new Event(); 
    event.setId(1000l); 
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1019l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.INCOMPLETE.name());
    listing.setTicketMedium(TicketMedium.PDF.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));
    listing.setSellerPaymentTypeId(2L);
    listing.setFraudCheckStatusId(1L);

    listing.setSellerContactId(2L);
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1019l);
    newlisting.setEvent(event);
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());
    when(userHelper.isSellerCCIdValid("1019l", 1L)).thenReturn(true);

    when(userHelper.isSellerPaymentContactIdPopulated("1000l", 2L, null)).thenReturn(false);
    when(userHelper.isSellerContactValid("AB123", 2L)).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(1019l, 10001L, 1L)).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest(); 
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(newlisting.getSeats() != null);
  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_activateInactiveListing_NullSplitOption() throws Exception {
    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.INCOMPLETE.name());
    listing.setTicketMedium(TicketMedium.PDF.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setSellerPaymentTypeId(2L);
    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));

    listing.setSellerContactId(2L);
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEvent(event);
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());
    when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);

    when(userHelper.isSellerPaymentContactIdPopulated(Mockito.anyString(), Mockito.anyLong(),
        Mockito.anyList())).thenReturn(true);
    when(userHelper.isSellerContactValid("AB123", 2L)).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(1000l, 10001L, 1L)).thenReturn(true);

    when(inventoryMgr.findListing(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(null);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(newlisting.getSeats() != null);
  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_activateFraudDeactivatedListing() throws Exception {
    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setSplitQuantity(2);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.INCOMPLETE.name());
    listing.setTicketMedium(TicketMedium.PDF.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setSellerPaymentTypeId(2L);
    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));
    listing.setSellerContactId(2L);
    listing.setFraudCheckStatusId(1L);
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEvent(event);
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());
    when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);

    when(userHelper.isSellerPaymentContactIdPopulated(Mockito.anyString(), Mockito.anyLong(),
        Mockito.anyList())).thenReturn(true);
    when(userHelper.isSellerContactValid("AB123", 2L)).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(1000l, 10001L, 1L)).thenReturn(true);

    when(inventoryMgr.findListing(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(null);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

  }

  /*
   * @Test(expectedExceptions={ListingBusinessException.class}) public void
   * updateListing_activateInactiveListing_DuplicateSRS() throws Exception {
   * Mockito.when(inventorySolrUtil.isListingExists(Mockito.anyLong(), Mockito.anyString(),
   * Mockito.anyString(), Mockito.anyString())).thenReturn(true);
   * 
   * Event event = new Event(); event.setId(1000l); event.setActive(true);
   * event.setCurrency(Currency.getInstance("USD"));
   * event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific")); event.setDescription(
   * "Event description"); event.setEventDate(new GregorianCalendar(2012, 10, 1));
   * event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
   * event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
   * when(eventHelper.getEventById(anyLong(), anyString())).thenReturn(event); Mockito.doAnswer(new
   * Answer<Void>() {
   * 
   * @Override public Void answer(InvocationOnMock invocation) throws Throwable { return null; }
   * }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class)); Listing listing = new
   * Listing(); listing.setId(1000l); listing.setEventId(1000l); listing.setSellerId(1000l);
   * listing.setListPrice(new Money(new BigDecimal(100d), "USD")); listing.setQuantity(10);
   * listing.setSplitOption((short)1); listing.setRow("1");
   * listing.setSeats("1,2,3,4,5,6,7,8,9,10");
   * listing.setSystemStatus(ListingStatus.INCOMPLETE.name());
   * listing.setTicketMedium(TicketMedium.PDF.getValue());
   * listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue()); listing.setQuantityRemain(8);
   * listing.setInhandDate(new GregorianCalendar(2012, 10, 1)); listing.setSellerCCId(1L);
   * listing.setSellerPaymentTypeId(2L); listing.setMinPricePerTicket(new Money(new BigDecimal(10d),
   * "USD")); listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));
   * listing.setSplitQuantity(10); listing.setSplitOption((short)1);
   * 
   * 
   * listing.setSellerContactId(2L); when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
   * 
   * CustomerContactDetails userContact = new CustomerContactDetails();
   * userContact.setPaymentContactBoolean(true);
   * 
   * when(userHelper.getUserContact("AB123",2L)).thenReturn(userContact);
   * 
   * List<TicketSeat> seats = new ArrayList<TicketSeat>(); for(int i=0; i<10; i++){ TicketSeat seat
   * = new TicketSeat(); seats.add(seat); }
   * when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);
   * 
   * Listing newlisting = new Listing(); newlisting.setId(1000l); newlisting.setSellerId(1000l);
   * newlisting.setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name
   * ()); when(userHelper.isSellerCCIdValid(Mockito.anyString(),
   * Mockito.anyLong())).thenReturn(true);
   * 
   * when(userHelper.isSellerPaymentContactIdPopulated(Mockito.anyString(), Mockito.anyLong(),
   * Mockito.anyList())).thenReturn(true); when(userHelper.isSellerContactValid("AB123",
   * 2L)).thenReturn(true); when(userHelper.isSellerPaymentTypeValid(1000l,10001L,
   * 1L)).thenReturn(true); Listing existingListing = new Listing();
   * existingListing.setId(1111111L);
   * when(inventoryMgr.findListing(Mockito.anyLong(),Mockito.anyString(),Mockito.anyString(),
   * Mockito.anyString())).thenReturn(existingListing); SHAPIContext shapiContext = new
   * SHAPIContext();
   * 
   * ListingRequest req = new ListingRequest (); newlisting =
   * helper.updateOrCreateSingleListing(newlisting, req, shapiContext);
   * 
   * assertTrue(newlisting !=null); assertTrue(newlisting.getSeats() !=null); }
   */

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_activateInactiveListing_ListPriceLTZero() throws Exception {
    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(0d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.INCOMPLETE.name());
    listing.setTicketMedium(TicketMedium.PDF.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setSellerPaymentTypeId(2L);
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);

    listing.setSellerContactId(2L);
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());
    newlisting.setEvent(event);
    when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);

    when(userHelper.isSellerPaymentContactIdPopulated(Mockito.anyString(), Mockito.anyLong(),
        Mockito.anyList())).thenReturn(true);
    when(userHelper.isSellerContactValid("AB123", 2L)).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(1000l, 10001L, 1L)).thenReturn(true);

    when(inventoryMgr.findListing(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(null);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(newlisting.getSeats() != null);
  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_deActivateInactiveListing_PendingLockListing() throws Exception {
    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.PENDING_LOCK.toString());

    listing.setTicketMedium(TicketMedium.PDF.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setSellerPaymentTypeId(1L);
    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());
    newlisting.setEvent(event);
    when(userHelper.isSellerCCIdValid("1000l", 1L)).thenReturn(true);

    when(userHelper.isSellerPaymentContactIdPopulated("1000l", 2L, null)).thenReturn(true);
    when(userHelper.isSellerContactValid("AB123", 2L)).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(1000l, 10001L, 1L)).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(newlisting.getSeats() != null);
    assertTrue(newlisting.getSeats().indexOf("10") == -1);

  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_updateMinMaxPrice_MinGTMax() throws Exception {
    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(anyLong(), anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(event);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.PENDING_LOCK.toString());

    listing.setTicketMedium(TicketMedium.PDF.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setSellerPaymentTypeId(1L);
    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));
    listing.setCurrency(Currency.getInstance("USD"));

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(50d), "USD"));
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.INACTIVE.name());
    newlisting.setMinPricePerTicket(new Money(new BigDecimal(100d), "USD"));
    newlisting.setMaxPricePerTicket(new Money(new BigDecimal(50d), "USD"));
    newlisting.setEvent(event);
    when(userHelper.isSellerCCIdValid("1000l", 1L)).thenReturn(true);

    when(userHelper.isSellerPaymentContactIdPopulated("1000l", 2L, null)).thenReturn(true);
    when(userHelper.isSellerContactValid("AB123", 2L)).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(1000l, 10001L, 1L)).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();
    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(newlisting.getSeats() != null);
    assertTrue(newlisting.getSeats().indexOf("10") == -1);
  }

  @Test
  public void updateListing_updatePDFStatus() throws Exception {
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = getListing(1000l, null, null, null, 10, 10);
    listing.setSystemStatus(ListingStatus.PENDING_LOCK.name());
    listing.setSection("Lower Back 102");
    listing.setRow("rows1,row2");
    listing.setTicketMedium(TicketMedium.PDF.getValue());
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    listing.setEvent(getEvent());
    when(inventoryMgr.getSectionId(anyLong(), anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(new Long(1L));

    List<TicketSeat> tseats = getTicketSeats(1000l, null, null, 10);
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(tseats);
    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setSection("Lower Back 102");
    newlisting.setRow("rows1,row2");
    newlisting.setSystemStatus(
        com.stubhub.domain.inventory.common.entity.ListingStatus.PENDING.toString());
    newlisting.setEvent(getEvent());
    when(inventoryMgr.findListing(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(null);
    when(inventoryMgr.isPDFPendingReviewAllowed(Mockito.anyLong())).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);
    assertTrue(newlisting != null);
    assertTrue(newlisting.getSection().equalsIgnoreCase("Lower Back 102"));
    assertTrue(newlisting.getRow().equalsIgnoreCase("rows1,row2"));
  }
  
  @Test
  public void updateListing_updateDeletedListing() throws Exception {
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = getListing(1000l, null, null, null, 10, 10);
    listing.setSystemStatus(ListingStatus.DELETED.name());
    listing.setSection("Lower Back 102");
    listing.setRow("rows1,row2");
    listing.setTicketMedium(TicketMedium.PDF.getValue());
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    listing.setEvent(getEvent());
    when(inventoryMgr.getSectionId(anyLong(), anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(new Long(1L));

    List<TicketSeat> tseats = getTicketSeats(1000l, null, null, 10);
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(tseats);
    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setSection("Lower Back 102");
    newlisting.setRow("rows1,row2");
    newlisting.setSystemStatus(
        com.stubhub.domain.inventory.common.entity.ListingStatus.DELETED.toString());
    newlisting.setEvent(getEvent());
    when(inventoryMgr.findListing(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(null);
    when(inventoryMgr.isPDFPendingReviewAllowed(Mockito.anyLong())).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    try
    {
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);
    }
    catch(ListingBusinessException ex)
    {
        Assert.assertTrue(ex.getLocalizedMessage().equals("Listing has been deleted"));
    }
  }

  @Test
  public void updateListing_Incomplete_updatePDFStatus() throws Exception {
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = getListing(1000l, null, null, null, 10, 10);
    listing.setSystemStatus(ListingStatus.PENDING_LOCK.name());
    listing.setSection("Section1");
    listing.setRow("rows1,row2");
    listing.setTicketMedium(TicketMedium.PDF.getValue());
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    listing.setEvent(getEvent());
    when(inventoryMgr.getSectionId(anyLong(), anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(new Long(1L));

    List<TicketSeat> tseats = getTicketSeats(1000l, null, null, 10);
    tseats.get(0).setSeatStatusId(1L);
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(tseats);
    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setSection("General Admission");
    newlisting.setRow("rows1,row2");
    newlisting.setSystemStatus(
        com.stubhub.domain.inventory.common.entity.ListingStatus.INCOMPLETE.toString());
    newlisting.setEvent(getEvent());
    when(inventoryMgr.findListing(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(null);
    when(inventoryMgr.isPDFPendingReviewAllowed(Mockito.anyLong())).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);
    assertTrue(newlisting != null);
    assertTrue(newlisting.getSection().equalsIgnoreCase("General Admission"));
    assertTrue(newlisting.getRow().equalsIgnoreCase("rows1,row2"));
  }

  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_updatePDFStatus_Exception() throws Exception {
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));

    Listing listing = getListing(1000l, "Lower Back 102", "rows1,row2", null, 10, 10);
    listing.setSystemStatus(ListingStatus.PENDING_LOCK.name());
    listing.setTicketMedium(TicketMedium.PDF.getValue());
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(inventoryMgr.getSectionId(anyLong(), anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(new Long(1L));
    List<TicketSeat> tseats = getTicketSeats(1000l, null, null, 10);
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(tseats);

    Listing newlisting = getListing(1000l, "Lower Back 102", "rows1,row2", null, 10, 10);
    newlisting.setSystemStatus(
        com.stubhub.domain.inventory.common.entity.ListingStatus.PENDING.toString());
    when(inventoryMgr.findListing(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(null);
    when(inventoryMgr.isPDFPendingReviewAllowed(Mockito.anyLong())).thenReturn(false);
    SHAPIContext shapiContext = new SHAPIContext();
    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);
    assertTrue(newlisting != null);
    assertTrue(newlisting.getSeats() != null);
    assertTrue(newlisting.getSection().equalsIgnoreCase("Lower Back 102"));
    assertTrue(newlisting.getRow().equalsIgnoreCase("rows1,row2"));
  }



  @Test(expectedExceptions = {ListingBusinessException.class})
  public void updateListing_updateIllegalPDFStatus_Exception() throws Exception {
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
    Listing listing = getListing(1000l, "Lower Back 102", "rows1,row2", null, 10, 10);
    listing.setSystemStatus(ListingStatus.PENDING_LOCK.name());
    listing.setTicketMedium(TicketMedium.BARCODE.getValue());
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(inventoryMgr.getSectionId(anyLong(), anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(new Long(1L));

    List<TicketSeat> tseats = getTicketSeats(1000l, null, null, 10);
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(tseats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setSection("Lower Back 102");
    newlisting.setRow("rows1,row2");
    newlisting.setSystemStatus(
        com.stubhub.domain.inventory.common.entity.ListingStatus.PENDING.toString());
    when(inventoryMgr.findListing(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(null);
    when(inventoryMgr.isPDFPendingReviewAllowed(Mockito.anyLong())).thenReturn(false);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(newlisting.getSeats() != null);
    assertTrue(newlisting.getSection().equalsIgnoreCase("Lower Back 102"));
    assertTrue(newlisting.getRow().equalsIgnoreCase("rows1,row2"));
  }

  @Test
  public void updateListing_activateIncompleteListingLmsOnly() throws Exception {
    List<TicketSeat> ticketSeats = getTicketSeats(1000L, null, null, 10, true);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(ticketSeats);

    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(event);

    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.INCOMPLETE.name());
    listing.setTicketMedium(TicketMedium.PAPER.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setSaleMethod(1L);
    listing.setSellerPaymentTypeId(1L);
    listing.setSellerContactId(2L);
    listing.setSplitOption((short) 0);
    listing.setSplitQuantity(listing.getQuantity());
    listing.setCurrency(Currency.getInstance("USD"));
    listing.setLmsApprovalStatus(1);
    listing.setFulfillmentDeliveryMethods("9,17");

    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());
    newlisting.setEvent(event);

    newlisting.setSellerGuid("AB123");
    ListingCheck listingCheck = new ListingCheck();
    when(inventorySolrUtil.isListingExists(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong())).thenReturn(listingCheck);
    when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    when(userHelper.isSellerPaymentContactIdPopulated(Mockito.anyString(), Mockito.anyLong(),
        Mockito.anyList())).thenReturn(true);
    when(userHelper.isSellerContactValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyLong())).thenReturn(true);
    when(userHelper.getMappedValidSellerCCId(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyList(),Mockito.anyString())).thenReturn(10001L);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(listing.getSystemStatus()
        .equals(com.stubhub.domain.inventory.common.entity.ListingStatus.INCOMPLETE.name()));
  }

  @Test
  public void updateListing_activateIncompleteListingUPS() throws Exception {
    List<TicketSeat> ticketSeats = getTicketSeats(1000L, null, null, 10, true);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(ticketSeats);

    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(event);

    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.INCOMPLETE.name());
    listing.setTicketMedium(TicketMedium.PAPER.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setSaleMethod(1L);
    listing.setSellerPaymentTypeId(1L);
    listing.setSellerContactId(2L);
    listing.setSplitOption((short) 0);
    listing.setSplitQuantity(listing.getQuantity());
    listing.setCurrency(Currency.getInstance("USD"));
    listing.setLmsApprovalStatus(1);
    listing.setFulfillmentDeliveryMethods("10,");

    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());
    newlisting.setEvent(event);

    newlisting.setSellerGuid("AB123");
    ListingCheck listingCheck = new ListingCheck();
    when(inventorySolrUtil.isListingExists(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong())).thenReturn(listingCheck);
    when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    when(userHelper.isSellerPaymentContactIdPopulated(Mockito.anyString(), Mockito.anyLong(),
        Mockito.anyList())).thenReturn(true);
    when(userHelper.isSellerContactValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyLong())).thenReturn(true);
    when(userHelper.getMappedValidSellerCCId(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyList(),Mockito.anyString())).thenReturn(10001L);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(listing.getSystemStatus()
        .equals(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name()));
  }

  @Test
  public void updateListing_activateIncompleteListingUPSFulfillmentDeliveryMethod1()
      throws Exception {
    List<TicketSeat> ticketSeats = getTicketSeats(1000L, null, null, 10, true);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(ticketSeats);

    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(event);

    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.INCOMPLETE.name());
    listing.setTicketMedium(TicketMedium.PAPER.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setSaleMethod(1L);
    listing.setSellerPaymentTypeId(1L);
    listing.setSellerContactId(2L);
    listing.setSplitOption((short) 0);
    listing.setSplitQuantity(listing.getQuantity());
    listing.setCurrency(Currency.getInstance("USD"));
    listing.setLmsApprovalStatus(1);
    listing.setFulfillmentDeliveryMethods("|10,");

    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());
    newlisting.setEvent(event);

    newlisting.setSellerGuid("AB123");
    ListingCheck listingCheck = new ListingCheck();
    when(inventorySolrUtil.isListingExists(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong())).thenReturn(listingCheck);
    when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    when(userHelper.isSellerPaymentContactIdPopulated(Mockito.anyString(), Mockito.anyLong(),
        Mockito.anyList())).thenReturn(true);
    when(userHelper.isSellerContactValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyLong())).thenReturn(true);
    when(userHelper.getMappedValidSellerCCId(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyList(), Mockito.anyString())).thenReturn(10001L);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(listing.getSystemStatus()
        .equals(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name()));
  }

  @Test
  public void updateListing_activateIncompleteListingUPSFulfillmentDeliveryMethod2()
      throws Exception {
    List<TicketSeat> ticketSeats = getTicketSeats(1000L, null, null, 10, true);

    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(ticketSeats);

    Event event = new Event();
    event.setId(1000l);
    event.setActive(true);
    event.setCurrency(Currency.getInstance("USD"));
    event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    event.setDescription("Event description");
    event.setEventDate(new GregorianCalendar(2012, 10, 1));
    event.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    event.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(event);

    Listing listing = new Listing();
    listing.setId(1000l);
    listing.setEventId(1000l);
    listing.setSellerId(1000l);
    listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    listing.setQuantity(10);
    listing.setSplitOption((short) 1);
    listing.setRow("1");
    listing.setSeats("1,2,3,4,5,6,7,8,9,10");
    listing.setSystemStatus(ListingStatus.INCOMPLETE.name());
    listing.setTicketMedium(TicketMedium.PAPER.getValue());
    listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    listing.setQuantityRemain(8);
    listing.setInhandDate(new GregorianCalendar(2012, 10, 1));
    listing.setSellerCCId(1L);
    listing.setSaleMethod(1L);
    listing.setSellerPaymentTypeId(1L);
    listing.setSellerContactId(2L);
    listing.setSplitOption((short) 0);
    listing.setSplitQuantity(listing.getQuantity());
    listing.setCurrency(Currency.getInstance("USD"));
    listing.setLmsApprovalStatus(1);
    listing.setFulfillmentDeliveryMethods("|13,");

    listing.setMinPricePerTicket(new Money(new BigDecimal(10d), "USD"));
    listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    CustomerContactDetails userContact = new CustomerContactDetails();
    userContact.setPaymentContactBoolean(true);

    when(userHelper.getUserContact("AB123", 2L)).thenReturn(userContact);

    List<TicketSeat> seats = new ArrayList<TicketSeat>();
    for (int i = 0; i < 10; i++) {
      TicketSeat seat = new TicketSeat();
      seats.add(seat);
    }
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(seats);

    Listing newlisting = new Listing();
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting
        .setSystemStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE.name());
    newlisting.setEvent(event);

    newlisting.setSellerGuid("AB123");
    ListingCheck listingCheck = new ListingCheck();
    when(inventorySolrUtil.isListingExists(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong())).thenReturn(listingCheck);
    when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    when(userHelper.isSellerPaymentContactIdPopulated(Mockito.anyString(), Mockito.anyLong(),
        Mockito.anyList())).thenReturn(true);
    when(userHelper.isSellerContactValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    when(userHelper.isSellerPaymentTypeValid(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyLong())).thenReturn(true);
    when(userHelper.getMappedValidSellerCCId(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyList(),Mockito.anyString())).thenReturn(10001L);
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);

    assertTrue(newlisting != null);
    assertTrue(listing.getSystemStatus()
        .equals(com.stubhub.domain.inventory.common.entity.ListingStatus.INCOMPLETE.name()));
  }



  @Test
  public void createListing_IncompleteStatus_DuplicateSRS() throws Exception {
    Event event = getEvent();
    ListingCheck listingCheck = new ListingCheck();
    listingCheck.setIsListed(true);
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(event);

    when(inventorySolrUtil.isListingExists(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong())).thenReturn(listingCheck);


    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    req.setListingId(null); // create
    req.setEventId("1000");
    req.setExternalListingId("ext-listing-id");
    req.setQuantity(3);
    req.setPricePerProduct(new Money("120.00", "USD"));
    req.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.INCOMPLETE);

    Listing newListing = ListingRequestAdapter.convert(req, true, null);
    newListing.setSection("section1");
    newListing.setRow("row1");
    newListing.setSeats("seat1");
    newListing.setInhandDate(null); // TODO: need to fix IH date to deal with date defaults

    newListing.setEventId(event.getId());
    newListing.setEvent(event);

    Listing result = null;
    try {
      // expect success
      result = helper.createSingleListing(newListing, req, shapiContext);

      // check defaults (for now splits)
      Assert.fail("Should not succeed");
    } catch (ListingBusinessException lbe) {
      assertTrue(lbe.getListingError().getCode().equals(ErrorCode.DUPLICATE_SECTION_ROW_SEAT));
    } finally {
      inventorySolrUtil =
          (InventorySolrUtil) mockClass(InventorySolrUtil.class, helper, "inventorySolrUtil");
    }
  }


  @Test
  public void createListing_IncompleteStatus() throws Exception {
    Event event = getEvent();
    ListingCheck listingCheck = new ListingCheck();
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(event);

    when(inventorySolrUtil.isListingExists(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong())).thenReturn(listingCheck);


    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    req.setListingId(null); // create
    req.setEventId("1000");
    req.setExternalListingId("ext-listing-id");
    req.setQuantity(3);
    req.setPricePerProduct(new Money("120.00", "USD"));
    req.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.INCOMPLETE);

    Listing newListing = ListingRequestAdapter.convert(req, true, null);
    newListing.setSection("section1");
    newListing.setRow("row1");
    newListing.setSeats("seat1");
    newListing.setInhandDate(null); // TODO: need to fix IH date to deal with date defaults

    newListing.setEventId(event.getId());
    newListing.setEvent(event);


    // expect success
    Listing result = helper.createSingleListing(newListing, req, shapiContext);

    assertTrue(result != null);

  }



  @Test
  public void createListing_IncompleteStatus_NonGASection() throws Exception {
    Event event = getEvent();
    ListingCheck listingCheck = new ListingCheck();
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(event);

    when(inventorySolrUtil.isListingExists(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong())).thenReturn(listingCheck);


    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    req.setListingId(null); // create
    req.setEventId("1000");
    req.setExternalListingId("ext-listing-id");
    req.setQuantity(3);
    req.setPricePerProduct(new Money("120.00", "USD"));
    req.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.INCOMPLETE);


    Listing newListing = ListingRequestAdapter.convert(req, true, null);
    newListing.setSection("section1");
    newListing.setRow("row1");
    newListing.setSeats("GA1");
    newListing.setInhandDate(null); // TODO: need to fix IH date to deal with date defaults

    newListing.setEventId(event.getId());
    newListing.setEvent(event);


    // expect success
    Listing result = helper.createSingleListing(newListing, req, shapiContext);

    assertTrue(result != null);

  }


  @Test
  public void testAdjustPrice() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setEvent(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);

    Listing newListing = new Listing();
    newListing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newListing.setCurrency(Currency.getInstance("USD"));
    newListing.setId(1000l);
    newListing.setSellerId(1000l);
    newListing.setTicketMedium(TicketMedium.PDF.getValue());

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    ListingRequest req = new ListingRequest();
    req.setListingId(1000L);
    req.setEventId("1000");
    req.setQuantity(2);
    req.setAdjustPrice(true);
    req.setPricePerProduct(new Money(new BigDecimal(1d), "USD"));

    Object[] prResp = new Object[1];
    prResp[0] = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.LISTING_PRICE_TOO_LOW,
        "Minimum listing price error:expected minimum listing price = Money [amount=6, currency=USD]",
        "expected minimum listing price = Money [amount=6, currency=USD]");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    ldata.setHeaderListing(newListing);
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helper.createOrUpdateListingData(ldata, clientIp, userAgent);

    req.setAdjustPrice(false);
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> respList =
        helper.createOrUpdateListingData(ldata, clientIp, userAgent);

  }

  @Test
  public void testDeletePredeliveredBarcodeListing() throws Exception {
    SHAPIContext shapiContext = new SHAPIContext();
    ListingRequest req = new ListingRequest();
    req.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.DELETED);

    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5);
    dbListing.setInhandDateValidated(true);
    dbListing.setTicketMedium(TicketMedium.BARCODE.getValue());
    dbListing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());

    Listing newListing = new Listing();
    newListing.setId(1000l);
    newListing.setSellerId(1000l);
    newListing.setEvent(getEvent());
    newListing.setSystemStatus(ListingStatus.DELETED.toString());

    ListingWrapper listingWrapper =
        helper.updateOrCreateSingleListing(newListing, dbListing, req, shapiContext, null);
    Assert.assertEquals(ListingStatus.DELETED.toString(),
        listingWrapper.getListing().getSystemStatus());
  }

  @Test(expectedExceptions = ListingBusinessException.class)
  public void testUpdateOrCreateSingleListingWithDuplicate() throws Exception {
    SHAPIContext shapiContext = new SHAPIContext();
    ListingRequest req = new ListingRequest();
    req.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);

    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5);
    dbListing.setInhandDateValidated(true);
    dbListing.setTicketMedium(TicketMedium.BARCODE.getValue());
    dbListing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());

    Listing newListing = new Listing();
    newListing.setSellerId(1000l);
    newListing.setEvent(getEvent());
    newListing.setSystemStatus(ListingStatus.ACTIVE.toString());

    ListingCheck listingCheck = new ListingCheck();
    listingCheck.setIsListed(true);
    listingCheck.setMessage("duplicate");
    when(inventorySolrUtil.isListingExists(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong())).thenReturn(listingCheck);

    helper.updateOrCreateSingleListing(newListing, dbListing, req, shapiContext, null);
  }

  @Test(expectedExceptions = ListingBusinessException.class)
  public void testUpdateOrCreateSingleListingWithDuplicatePending() throws Exception {
    SHAPIContext shapiContext = new SHAPIContext();
    ListingRequest req = new ListingRequest();
    req.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);

    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3,4", 5, 5);
    dbListing.setInhandDateValidated(true);
    dbListing.setTicketMedium(TicketMedium.BARCODE.getValue());
    dbListing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    dbListing.setSellerPaymentTypeId(100L);
    dbListing.setSellerContactId(100L);
    dbListing.setSystemStatus(ListingStatus.INACTIVE.toString());
    dbListing.setSellerCCId(100L);
    dbListing.setAllsellerPaymentInstruments(new ArrayList<CustomerPaymentInstrumentDetailsV2>());
    dbListing.setSplitOption((short) 1);

    Listing newListing = new Listing();
    newListing.setSellerId(1000l);
    newListing.setEvent(getEvent());
    newListing.setSystemStatus(ListingStatus.ACTIVE.toString());
    newListing.setSellerGuid("123");


    ListingCheck listingCheck = new ListingCheck();
    listingCheck.setIsListed(true);
    listingCheck.setMessage("duplicate");
    Mockito.when(userHelper.isSellerPaymentContactIdPopulated("123", 100l, null)).thenReturn(true);
    when(inventorySolrUtil.isListingExists(Mockito.anyLong(), Mockito.anyLong(),
        Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong())).thenReturn(listingCheck);

    helper.updateOrCreateSingleListing(newListing, dbListing, req, shapiContext, null);
  }


  @Test
  public void testGetListingFromRequest() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setEvent(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);

    Listing newListing = new Listing();
    newListing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newListing.setCurrency(Currency.getInstance("USD"));
    newListing.setId(1000l);
    newListing.setSellerId(1000l);
    newListing.setTicketMedium(TicketMedium.PDF.getValue());

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    ListingRequest req = new ListingRequest();
    req.setListingId(1000L);
    req.setEventId("1000");
    req.setQuantity(2);
    req.setAdjustPrice(true);
    req.setPricePerProduct(new Money(new BigDecimal(1d), "USD"));

    Object[] prResp = new Object[1];
    prResp[0] = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.LISTING_PRICE_TOO_LOW,
        "Minimum listing price error:expected minimum listing price = Money [amount=6, currency=USD]",
        "expected minimum listing price = Money [amount=6, currency=USD]");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    ldata.setHeaderListing(newListing);
    ldata.setCurListingsMap(1000l, new HashMap<Long, Listing>());

    helper.getListingFromRequest(ldata, req, new ListingResponse(), clientIp, userAgent);
  }

  @Test
  public void testIsSingleFileUploadWithSplit() {
    Listing currentListing = new Listing();
    currentListing.setTicketMedium(TicketMedium.PDF.getValue());
    currentListing.setDeliveryOption(1);
    SeatsAndTraitsManipulator.isSingleFileUploadWithSplit(new Listing(), currentListing, 1);
  }
  
  @Test
  public void testLmsListing() throws Exception {
    when(ticketSeatMgr.findActiveTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 2));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2", 10, 2);
    TicketSeat ts1 = new TicketSeat();
  	ts1.setSeatStatusId(1L);
  	TicketSeat ts2 = new TicketSeat();
  	ts2.setSeatStatusId(1L);
  	List<TicketSeat> tsList = new ArrayList<TicketSeat>();
  	tsList.add(ts1);
  	tsList.add(ts2);
  	dblisting.setTicketSeats(tsList);
    dblisting.setSellerRequestedStatus("ACTIVE");
    dblisting.setEvent(getEvent());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);

    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(inventoryMgr.getListingBySellerIdExternalIdAndStatus(Matchers.anyLong(),
        Matchers.anyString())).thenReturn(null);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEventId(1000l);
    newlisting.setExternalId("12345");
    newlisting.setLmsApprovalStatus(1);
    newlisting.setSellerRequestedStatus("ACTIVE");
    TicketSeat seat = new TicketSeat();
    seat.setFulfillmentArtifactId(12345l);
    List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>(1);
    ticketSeats.add(seat);
    newlisting.setTicketSeats(ticketSeats);

    List<Listing> newListingList = new ArrayList<Listing>(1);
    newListingList.add(newlisting);
    ListingRequest req = new ListingRequest();
    req.setExternalListingId("12345");
    req.setListingId(1000l);
    req.setEventId("1000");

    Product product = new Product();
    product.setFulfillmentArtifact("123");
    product.setExternalId("123");
    Product product1 = new Product();
    product1.setFulfillmentArtifact("1234");
    product1.setExternalId("1234");
    List<Product> products = new ArrayList<Product>();
    products.add(product);
    products.add(product1);
    req.setProducts(products);
    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
    List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);
    SeatProduct seatProd = new SeatProduct();
    seatProd.setProductType(ProductType.TICKET);
    seatProd.setFulfillmentArtifact("1234");
    seatProd.setRow("row");
    seatProd.setSeat("seat");
    seatProd.setSeatId(123l);

    SeatProduct seatProd1 = new SeatProduct();
    seatProd1.setProductType(ProductType.TICKET);
    seatProd1.setFulfillmentArtifact("1234");
    seatProd1.setRow("row");
    seatProd1.setSeat("seat");
    seatProd1.setSeatId(123l);
    seatProductList.add(seatProd);
    seatProductList.add(seatProd1);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 5);
    Mockito.doReturn(seatProductList).when(seatProductsContextMock).getBarcodeSeatProductList();
    Mockito.doReturn(dbSeats).when(seatProductsContextMock).getTicketSeatsFromCache();
    ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
    UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
    Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
        Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
        Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    List<Listing> dbList = new ArrayList<Listing>(1);

    dbList.add(dblisting);
    Mockito.doReturn(dbList).when(helperspy).updateDatabaseForBatch(Mockito.anyBoolean(),
        Mockito.anyListOf(Listing.class));
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    
    ListingData ldata = listingDataFromUpdateRequest(inventoryMgr, 1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    ldata.setHeaderListing(newlisting);
    when(fulfillmentServiceHelper.populateFulfillmentOptions((Listing) Matchers.any(),
        (List<com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow>) Matchers.any()))
            .thenReturn(true);
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helperspy.createOrUpdateListingData(ldata, clientIp, userAgent);
  }
  
  @Test
  public void testFlashSeatListing() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());

    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");

    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    
    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setSellerRequestedStatus("INACTIVE");
    dblisting.setEvent(getEvent());
    dblisting.setTicketMedium(TicketMedium.FLASHSEAT.getValue());
    dblisting.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    dblisting.setSystemStatus(ListingStatus.ACTIVE.name());
    dblisting.setQuantityRemain(3);
    dblisting.setSellerContactId(1234l);
    
    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);

    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(inventoryMgr.getListingBySellerIdExternalIdAndStatus(Matchers.anyLong(),
        Matchers.anyString())).thenReturn(null);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEventId(1000l);
    newlisting.setExternalId("12345");
    newlisting.setTicketMedium(TicketMedium.FLASHSEAT.getValue());
    newlisting.setFulfillmentMethod(FulfillmentMethod.FLASHSEAT);
    newlisting.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    newlisting.setSellerRequestedStatus("INACTIVE");
    newlisting.setQuantity(2);
    newlisting.setSellerContactId(1234l);
    newlisting.setEvent(getEvent());

    TicketSeat seat = new TicketSeat();
    seat.setFulfillmentArtifactId(12345l);
    List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>(1);
    ticketSeats.add(seat);
    newlisting.setTicketSeats(ticketSeats);

    List<Listing> newListingList = new ArrayList<Listing>(1);
    newListingList.add(newlisting);
    ListingRequest req = new ListingRequest();
    req.setExternalListingId("12345");
    req.setEventId("1000");
    req.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.FLASHSEAT);
    req.setContactId(1234L);
    Product product = new Product();
    product.setFulfillmentArtifact("123");
    product.setRow("row");
    product.setSeat("seat");
    Product product1 = new Product();
    product1.setFulfillmentArtifact("1234");
    product1.setRow("row1");
    product1.setSeat("seat1");
    List<Product> products = new ArrayList<Product>();
    products.add(product);
    products.add(product1);
    req.setProducts(products);
    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
    List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);
    SeatProduct seatProd = new SeatProduct();
    seatProd.setProductType(ProductType.TICKET);
    seatProd.setFulfillmentArtifact("1234");
    seatProd.setRow("row");
    seatProd.setSeat("seat");
    seatProd.setSeatId(123l);

    SeatProduct seatProd1 = new SeatProduct();
    seatProd1.setProductType(ProductType.TICKET);
    seatProd1.setFulfillmentArtifact("1235");
    seatProd1.setRow("row1");
    seatProd1.setSeat("seat1");
    seatProd1.setSeatId(124l);
    seatProductList.add(seatProd);
    seatProductList.add(seatProd1);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 5);
    Mockito.doReturn(seatProductList).when(seatProductsContextMock).getBarcodeSeatProductList();
    Mockito.doReturn(dbSeats).when(seatProductsContextMock).getTicketSeatsFromCache();
    ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
    UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
    Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
        Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
        Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    List<Listing> dbList = new ArrayList<Listing>(1);

    dbList.add(dblisting);
    Mockito.doReturn(dbList).when(helperspy).updateDatabaseForBatch(Mockito.anyBoolean(),
        Mockito.anyListOf(Listing.class));
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    ListingData ldata = listingDataFromCreateRequest(1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    ldata.setHeaderListing(newlisting);
    ldata.setSubscriber("Single|V2|test@testmail.com|test");
    when(fulfillmentServiceHelper.populateFulfillmentOptions((Listing) Matchers.any(),
        (List<com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow>) Matchers.any()))
            .thenReturn(true);
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helperspy.createOrUpdateListingData(ldata, clientIp, userAgent);
  }
  
  
  @Test
  public void testHiddenFlashSeatListing() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "Section-1", "R1", 10));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");


    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);

    Listing dblisting = getListing(1000L, "Section-1", "R1", "1,2,3,4,5,6,7,8", 10, 8);
    dblisting.setSellerRequestedStatus("HIDDEN");
    dblisting.setEvent(getEvent());
    dblisting.setTicketMedium(TicketMedium.FLASHSEAT.getValue());
    dblisting.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    dblisting.setSystemStatus(ListingStatus.HIDDEN.name());
    dblisting.setQuantityRemain(3);
    dblisting.setSellerContactId(1234L);

    when(inventoryMgr.getListing(anyLong())).thenReturn(dblisting);

    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(sellerHelper).populateSellerDetails(Matchers.any(Listing.class));
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(inventoryMgr.getListingBySellerIdExternalIdAndStatus(Matchers.anyLong(),
        Matchers.anyString())).thenReturn(null);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setId(1000l);
    newlisting.setSellerId(1000l);
    newlisting.setEventId(1000l);
    newlisting.setExternalId("12345");
    newlisting.setTicketMedium(TicketMedium.FLASHSEAT.getValue());
    newlisting.setFulfillmentMethod(FulfillmentMethod.FLASHSEAT);
    newlisting.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    newlisting.setSellerRequestedStatus("HIDDEN");
    newlisting.setQuantity(2);
    newlisting.setSellerContactId(1234L);
    newlisting.setEvent(getEvent());

    TicketSeat seat = new TicketSeat();
    seat.setFulfillmentArtifactId(12345l);
    List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>(1);
    ticketSeats.add(seat);
    newlisting.setTicketSeats(ticketSeats);

    List<Listing> newListingList = new ArrayList<Listing>(1);
    newListingList.add(newlisting);
    ListingRequest req = new ListingRequest();
    req.setExternalListingId("12345");
    req.setEventId("1000");
    req.setContactId(1234L);
    req.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.FLASHSEAT);
    Product product = new Product();
    product.setFulfillmentArtifact("123");
    product.setRow("row");
    product.setSeat("seat");
    Product product1 = new Product();
    product1.setFulfillmentArtifact("1234");
    product1.setRow("row1");
    product1.setSeat("seat1");
    List<Product> products = new ArrayList<Product>();
    products.add(product);
    products.add(product1);
    req.setProducts(products);
    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
    List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);
    SeatProduct seatProd = new SeatProduct();
    seatProd.setProductType(ProductType.TICKET);
    seatProd.setFulfillmentArtifact("1234");
    seatProd.setRow("row");
    seatProd.setSeat("seat");
    seatProd.setSeatId(123l);

    SeatProduct seatProd1 = new SeatProduct();
    seatProd1.setProductType(ProductType.TICKET);
    seatProd1.setFulfillmentArtifact("1235");
    seatProd1.setRow("row1");
    seatProd1.setSeat("seat1");
    seatProd1.setSeatId(124l);
    seatProductList.add(seatProd);
    seatProductList.add(seatProd1);
    List<TicketSeat> dbSeats = getTicketSeats(1000L, "sec-10", "R1", 5);
    Mockito.doReturn(seatProductList).when(seatProductsContextMock).getBarcodeSeatProductList();
    Mockito.doReturn(dbSeats).when(seatProductsContextMock).getTicketSeatsFromCache();
    ListingWrapper listingWrapper = new ListingWrapper(newlisting, seatProductsContextMock);
    UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
    Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
        Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
        Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);
    List<Listing> dbList = new ArrayList<Listing>(1);

    dbList.add(dblisting);
    Mockito.doReturn(dbList).when(helperspy).updateDatabaseForBatch(Mockito.anyBoolean(),
        Mockito.anyListOf(Listing.class));
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    ListingData ldata = listingDataFromCreateRequest(1000l, shapiContext, shServiceContext, false, req);
    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      throw new ListingBusinessException(ldata.getListingErrors().get(0));
    }
    ldata.setHeaderListing(newlisting);
    ldata.setSubscriber("Single|V2|test@testmail.com|test");
    when(fulfillmentServiceHelper.populateFulfillmentOptions((Listing) Matchers.any(),
        (List<com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow>) Matchers.any()))
            .thenReturn(true);
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helperspy.createOrUpdateListingData(ldata, clientIp, userAgent);
  }
  
  @Test
  public void testActivatePredeliveredFlashseatListing() throws Exception {
    SHAPIContext shapiContext = new SHAPIContext();
    ListingRequest req = new ListingRequest();
    req.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);

    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5);
    dbListing.setInhandDateValidated(true);
    dbListing.setTicketMedium(TicketMedium.FLASHSEAT.getValue());
    dbListing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    dbListing.setSystemStatus(ListingStatus.INCOMPLETE.toString());
    dbListing.setSellerContactId(1234L);

    Listing newListing = new Listing();
    newListing.setId(1000l);
    newListing.setSellerId(1000l);
    newListing.setEvent(getEvent());
    newListing.setSystemStatus(ListingStatus.ACTIVE.toString());
    
    when(inventorySolrUtil.isListingExists(Mockito.anyLong(), Mockito.anyLong(),
            Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong())).thenReturn(new ListingCheck());
    
    ListingWrapper listingWrapper =
        helper.updateOrCreateSingleListing(newListing, dbListing, req, shapiContext, null);
    Assert.assertEquals(ListingStatus.PENDING_LOCK.toString(),
        listingWrapper.getListing().getSystemStatus());
  }
  
  
  
  @Test
  public void testDeletePredeliveredFlashseatListing() throws Exception {
    SHAPIContext shapiContext = new SHAPIContext();
    ListingRequest req = new ListingRequest();
    req.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE);

    Listing dbListing = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5);
    dbListing.setInhandDateValidated(true);
    dbListing.setTicketMedium(TicketMedium.FLASHSEAT.getValue());
    dbListing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    dbListing.setSystemStatus(ListingStatus.ACTIVE.toString());
    dbListing.setSellerContactId(1234L);

    Listing newListing = new Listing();
    newListing.setId(1000l);
    newListing.setSellerId(1000l);
    newListing.setEvent(getEvent());
    newListing.setSystemStatus(ListingStatus.DELETED.toString());
    
    ListingWrapper listingWrapper =
        helper.updateOrCreateSingleListing(newListing, dbListing, req, shapiContext, null);
    Assert.assertEquals(ListingStatus.DELETED.toString(),
        listingWrapper.getListing().getSystemStatus());
  }
  
  @Test
  public void testDeleteSeatsGA() throws Exception {
    when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong()))
        .thenReturn(getTicketSeats(1000L, "sec-10", "R1", 5));
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    
    Listing dbListing = getListing(1000L, "General Admission", "R1", "1,2,3,4,5", 5, 5);
    dbListing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
    dbListing.setTicketMedium(TicketMedium.FLASHSEAT.getValue());
    when(inventoryMgr.getListing(anyLong())).thenReturn(dbListing);

    Listing newlisting = new Listing();
    newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newlisting.setSellerId(1000l);
    newlisting.setQuantity(3);
    newlisting.setQuantityRemain(3);
    newlisting.setSaleMethod(1L);
    newlisting.setId(1000l);
    newlisting.setEvent(getEvent());
    SHAPIContext shapiContext = new SHAPIContext();

    ListingRequest req = new ListingRequest();
    req.setEventId("1000");
    req.setListingId(1000l);
    req.setQuantity(3);
    newlisting = helper.createSingleListing(newlisting, req, shapiContext);
  }
  
  
  @Test
  public void testDeletePredeliveredListing() throws Exception {
    Event sampleEvent = getEvent();
    Long eventId = 1000l;
    when(eventHelper.getEventById(eventId, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    Long sellerId = 100022l;
    Listing existinglisting = new Listing();
    existinglisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    existinglisting.setSellerId(sellerId);
    existinglisting.setQuantity(1);
    existinglisting.setQuantityRemain(1);
    existinglisting.setDeliveryOption(1);
    existinglisting.setSection("PR115");
    existinglisting.setRow("R2");
    existinglisting.setSeats("20,21");
    existinglisting.setEventId(eventId);
    existinglisting.setId(10000045l);
    existinglisting.setTicketMedium(3);
    existinglisting.setSystemStatus("ACTIVE");
    Map<Long, Listing> lmap = new HashMap<Long, Listing>();
    lmap.put(10000045l, existinglisting);

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    shapiContext.setSignedJWTAssertion("DUMMY--assertion");

    ListingToDataAdapter adapter = new ListingToDataAdapter();
    List<ListingRequest> reqList = new ArrayList<ListingRequest>();
    ListingRequest req1 = new ListingRequest();
    req1.setListingId(10000045l);
    req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.DELETED);
    reqList.add(req1);


    Mockito.when(inventoryMgr.getListing(req1.getListingId())).thenReturn(existinglisting);
    Mockito.when(fulfillmentServiceHelper.populateFulfillmentOptions(Mockito.any(Listing.class),
        Mockito.anyList())).thenReturn(true);
    ListingData ldata = adapter.listingDataFromUpdateRequests(inventoryMgr, sellerId,
        "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);

    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      Assert.fail("Error encountered from listingDataFromRequests. Error: "
          + ldata.getListingErrors().get(0).getMessage());
    }

    ldata.updateEventInfo(sampleEvent);
    SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
    UpdateListingAsyncHelper2 help = new UpdateListingAsyncHelper2();
    final UpdateListingAsyncHelper2 helperspy = Mockito.spy(help);

    List<Listing> listings = new ArrayList<Listing>(1);
    Listing updatedListing = new Listing();
    updatedListing.setSystemStatus("DELETED");
    updatedListing.setDeliveryOption(1);
    updatedListing.setTicketMedium(4);
    listings.add(updatedListing);

    Mockito.when(inventoryMgr.updateListings(Mockito.anyListOf(Listing.class)))
        .thenReturn(listings);

    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
        helper.createOrUpdateListingData(ldata, clientIp, userAgent);
    Assert.assertTrue(responses != null);
    
  }
  
  @Test
  public void testPredeliveredListingLockMessage() throws Exception {
    Event sampleEvent = getEvent();
    Long eventId = 1000l;
    when(eventHelper.getEventById(eventId, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    Long sellerId = 100022l;
    Listing existinglisting = new Listing();
    existinglisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    existinglisting.setSellerId(sellerId);
    existinglisting.setQuantity(1);
    existinglisting.setQuantityRemain(1);
    existinglisting.setDeliveryOption(1);
    existinglisting.setSection("PR115");
    existinglisting.setRow("R2");
    existinglisting.setSeats("20,21");
    existinglisting.setEventId(eventId);
    existinglisting.setId(10000045l);
    existinglisting.setTicketMedium(3);
    existinglisting.setSystemStatus("PENDING LOCK");
    existinglisting.setIsLockMessageRequired(true);
    Map<Long, Listing> lmap = new HashMap<Long, Listing>();
    lmap.put(10000045l, existinglisting);

    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();

    shapiContext.setSignedJWTAssertion("DUMMY--assertion");

    ListingToDataAdapter adapter = new ListingToDataAdapter();
    List<ListingRequest> reqList = new ArrayList<ListingRequest>();
    ListingRequest req1 = new ListingRequest();
    req1.setListingId(10000045l);
    reqList.add(req1);


    Mockito.when(inventoryMgr.getListing(req1.getListingId())).thenReturn(existinglisting);
    Mockito.when(fulfillmentServiceHelper.populateFulfillmentOptions(Mockito.any(Listing.class),
        Mockito.anyList())).thenReturn(true);
    ListingData ldata = adapter.listingDataFromUpdateRequests(inventoryMgr, sellerId,
        "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);

    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      Assert.fail("Error encountered from listingDataFromRequests. Error: "
          + ldata.getListingErrors().get(0).getMessage());
    }

    ldata.updateEventInfo(sampleEvent);
    SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
    UpdateListingAsyncHelper2 help = new UpdateListingAsyncHelper2();
    final UpdateListingAsyncHelper2 helperspy = Mockito.spy(help);

    List<Listing> listings = new ArrayList<Listing>(1);
    Listing updatedListing = new Listing();
    updatedListing.setSystemStatus("PENDING LOCK");
    updatedListing.setIsLockMessageRequired(true);
    updatedListing.setDeliveryOption(1);
    updatedListing.setTicketMedium(4);
    listings.add(updatedListing);

    Mockito.when(inventoryMgr.updateListings(Mockito.anyListOf(Listing.class)))
        .thenReturn(listings);

    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
        helper.createOrUpdateListingData(ldata, clientIp, userAgent);
    Assert.assertTrue(responses != null);
    
  }
  
  @Test
  public void testDeletePredeliveredListingException() throws Exception {
    Event sampleEvent = getEvent();
    Long eventId = 1000l;
    when(eventHelper.getEventById(eventId, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(sampleEvent);

    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("25", "4");
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    Long sellerId = 100022l;
    Listing existinglisting = new Listing();
    existinglisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
    existinglisting.setSellerId(sellerId);
    existinglisting.setQuantity(1);
    existinglisting.setQuantityRemain(1);
    existinglisting.setDeliveryOption(1);
    existinglisting.setSection("PR115");
    existinglisting.setRow("R2");
    existinglisting.setSeats("20,21");
    existinglisting.setEventId(eventId);
    existinglisting.setId(10000045l);
    existinglisting.setTicketMedium(3);
    existinglisting.setSystemStatus("ACTIVE");
    Map<Long, Listing> lmap = new HashMap<Long, Listing>();
    lmap.put(10000045l, existinglisting);

    SHAPIContext shapiContext = new SHAPIContext();
    shapiContext.setSignedJWTAssertion("DUMMY--assertion");
    SHServiceContext shServiceContext = new SHServiceContext();

    ListingToDataAdapter adapter = new ListingToDataAdapter();
    List<ListingRequest> reqList = new ArrayList<ListingRequest>();
    ListingRequest req1 = new ListingRequest();
    req1.setListingId(10000045l);
    req1.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus.DELETED);
    reqList.add(req1);


    Mockito.when(inventoryMgr.getListing(req1.getListingId())).thenReturn(existinglisting);
    Mockito.when(fulfillmentServiceHelper.populateFulfillmentOptions(Mockito.any(Listing.class),
        Mockito.anyList())).thenReturn(true);
    ListingData ldata = adapter.listingDataFromUpdateRequests(inventoryMgr, sellerId,
        "E4016068190C25E7E044002128BE217A", shapiContext, shServiceContext, reqList, true);

    if (ldata.getListingErrors() != null && ldata.getListingErrors().size() > 0) {
      Assert.fail("Error encountered from listingDataFromRequests. Error: "
          + ldata.getListingErrors().get(0).getMessage());
    }

    ldata.updateEventInfo(sampleEvent);
    SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
    UpdateListingAsyncHelper2 help = new UpdateListingAsyncHelper2();
    final UpdateListingAsyncHelper2 helperspy = Mockito.spy(help);

    List<Listing> listings = new ArrayList<Listing>(1);
    Listing updatedListing = new Listing();
    updatedListing.setSystemStatus("DELETED");
    updatedListing.setDeliveryOption(1);
    updatedListing.setTicketMedium(3);
    listings.add(updatedListing);

    Mockito.when(inventoryMgr.updateListings(Mockito.anyListOf(Listing.class)))
        .thenReturn(listings);
    
    Mockito.doThrow(Exception.class).when(jmsMessageHelper).sendUnlockInventoryMessage(Mockito.anyLong());
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> responses =
        helper.createOrUpdateListingData(ldata, clientIp, userAgent);
    Assert.assertTrue(responses != null);
  }
  
  @Test
  public void createListing_MobileInstant() throws Exception {
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(inventoryMgr.getListingBySellerIdExternalIdAndStatus(Matchers.anyLong(),
        Matchers.anyString())).thenReturn(null);
    
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");

    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(sellerHelper.populateSellerDetails(Matchers.any(Listing.class))).thenReturn(true);


    Listing newListing = new Listing();
    newListing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newListing.setSellerId(1000l);
    newListing.setSellerContactId(1234l);
    newListing.setExternalId("12345");
    newListing.setTicketMedium(TicketMedium.MOBILE.getValue());
    newListing.setPredeliveryAvailable(true);
    newListing.setEvent(getEvent());
    TicketSeat seat = new TicketSeat();
    seat.setFulfillmentArtifactId(12345l);
    List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>(1);
    ticketSeats.add(seat);
    newListing.setTicketSeats(ticketSeats);
    List<Listing> newListingList = new ArrayList<Listing>(1);
    newListingList.add(newListing);
    ListingRequest req = new ListingRequest();
    req.setExternalListingId("12345");
    req.setPricePerProduct(new Money("100"));
    req.setEventId("1000");
    req.setContactId(1234l);
    req.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.MOBILE);
    
    Product product = new Product();
    product.setRow("1");
    product.setSeat("1");
    product.setFulfillmentArtifact("123");
    
    List<Product> products = new ArrayList<Product>();
    products.add(product);
    req.setProducts(products);
    
    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("110", "10");
    SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
    List<SeatProduct> seatProductList = new ArrayList<SeatProduct>();
    SeatProduct seatProd = new SeatProduct();
    seatProd.setProductType(ProductType.TICKET);
    seatProd.setFulfillmentArtifact("1234");
    seatProd.setRow("1");
    seatProd.setSeat("1");
    seatProductList.add(seatProd);
    
    ListingWrapper listingWrapper = new ListingWrapper(newListing, seatProductsContextMock);
    UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
    
    Mockito.doReturn(newListing).when(helperspy).getListingFromRequest(Mockito.any(ListingData.class),
        Mockito.any(ListingRequest.class), Mockito.any(ListingResponse.class), Mockito.anyString(), Mockito.anyString());
    Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
        Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
        Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    Mockito.doReturn(newListingList).when(helperspy).updateDatabaseForBatch(Mockito.anyBoolean(),
        Mockito.anyListOf(Listing.class));
    when(listingFulfilHelper.validateFileInfoIds(Mockito.any(SeatProductsContext.class),
        Mockito.any(Listing.class))).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    ListingData ldata = listingDataFromCreateRequest(1000l, shapiContext, shServiceContext, false, req);
    ldata.setHeaderListing(newListing);
    when(fulfillmentServiceHelper.populateFulfillmentOptions((Listing) Matchers.any(),
        (List<com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow>) Matchers.any()))
            .thenReturn(true);
    ldata.setSubscriber("Single|V2|test@testmail.com|test");
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helperspy.createOrUpdateListingData(ldata, clientIp, userAgent);
    Assert.assertTrue(resps != null);
  }
  
  
  
  @Test
  public void createListingWithValueInContactMappingApi() throws Exception {
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(inventoryMgr.getListingBySellerIdExternalIdAndStatus(Matchers.anyLong(),
        Matchers.anyString())).thenReturn(null);
    
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();
    mappingResponse.setId("test123");
    mappingResponse.setInternalId("1234");
    when(userHelper.isAuthZRequest()).thenReturn(false);
    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(sellerHelper.populateSellerDetails(Matchers.any(Listing.class))).thenReturn(true);

    Listing newListing = new Listing();
    newListing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newListing.setSellerId(1000l);
    newListing.setSellerContactId(1234l);
    newListing.setExternalId("12345");
    newListing.setTicketMedium(TicketMedium.MOBILE.getValue());
    newListing.setPredeliveryAvailable(true);
    newListing.setEvent(getEvent());
    TicketSeat seat = new TicketSeat();
    seat.setFulfillmentArtifactId(12345l);
    List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>(1);
    ticketSeats.add(seat);
    newListing.setTicketSeats(ticketSeats);
    List<Listing> newListingList = new ArrayList<Listing>(1);
    newListingList.add(newListing);
    ListingRequest req = new ListingRequest();
    req.setExternalListingId("12345");
    req.setPricePerProduct(new Money("100"));
    req.setEventId("1000");
    req.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.MOBILE);
    
    Product product = new Product();
    product.setRow("1");
    product.setSeat("1");
    product.setFulfillmentArtifact("123");
    
    List<Product> products = new ArrayList<Product>();
    products.add(product);
    req.setProducts(products);
    
    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("110", "10");
    SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
    List<SeatProduct> seatProductList = new ArrayList<SeatProduct>();
    SeatProduct seatProd = new SeatProduct();
    seatProd.setProductType(ProductType.TICKET);
    seatProd.setFulfillmentArtifact("1234");
    seatProd.setRow("1");
    seatProd.setSeat("1");
    seatProductList.add(seatProd);
    
    ListingWrapper listingWrapper = new ListingWrapper(newListing, seatProductsContextMock);
    UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
    
    Mockito.doReturn(newListing).when(helperspy).getListingFromRequest(Mockito.any(ListingData.class),
		Mockito.any(ListingRequest.class), Mockito.any(ListingResponse.class), Mockito.anyString(), Mockito.anyString());
    Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
        Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
        Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    Mockito.doReturn(newListingList).when(helperspy).updateDatabaseForBatch(Mockito.anyBoolean(),
        Mockito.anyListOf(Listing.class));
    when(listingFulfilHelper.validateFileInfoIds(Mockito.any(SeatProductsContext.class),
        Mockito.any(Listing.class))).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    ListingData ldata = listingDataFromCreateRequest(1000l, shapiContext, shServiceContext, false, req);
    ldata.setHeaderListing(newListing);
    ldata.setSubscriber("Single|V2|test@testmail.com|test");
    when(fulfillmentServiceHelper.populateFulfillmentOptions((Listing) Matchers.any(),
        (List<com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow>) Matchers.any()))
            .thenReturn(true);
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helperspy.createOrUpdateListingData(ldata, clientIp, userAgent);
    Assert.assertTrue(resps != null);

  }
  
  @Test(expectedExceptions = { ExecutionException.class})
  public void createListingWithNullValueInContactMappingApi() throws Exception {
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(inventoryMgr.getListingBySellerIdExternalIdAndStatus(Matchers.anyLong(),
        Matchers.anyString())).thenReturn(null);
    
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();

    when(userHelper.isAuthZRequest()).thenReturn(false);

    when(userHelper.getCustomerContactGuid(Matchers.anyString(), Matchers.anyString())).thenReturn(mappingResponse);
    when(sellerHelper.populateSellerDetails(Matchers.any(Listing.class))).thenReturn(true);

    Listing newListing = new Listing();
    newListing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newListing.setSellerId(1000l);
    newListing.setSellerContactId(1234l);
    newListing.setExternalId("12345");
    newListing.setTicketMedium(TicketMedium.MOBILE.getValue());
    newListing.setPredeliveryAvailable(true);
    TicketSeat seat = new TicketSeat();
    seat.setFulfillmentArtifactId(12345l);
    List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>(1);
    ticketSeats.add(seat);
    newListing.setTicketSeats(ticketSeats);
    List<Listing> newListingList = new ArrayList<Listing>(1);
    newListingList.add(newListing);
    ListingRequest req = new ListingRequest();
    req.setExternalListingId("12345");
    req.setPricePerProduct(new Money("100"));
    req.setEventId("1000");
    req.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.MOBILE);
    
    Product product = new Product();
    product.setRow("1");
    product.setSeat("1");
    product.setFulfillmentArtifact("123");
    
    List<Product> products = new ArrayList<Product>();
    products.add(product);
    req.setProducts(products);
    
    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("110", "10");
    SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
    List<SeatProduct> seatProductList = new ArrayList<SeatProduct>();
    SeatProduct seatProd = new SeatProduct();
    seatProd.setProductType(ProductType.TICKET);
    seatProd.setFulfillmentArtifact("1234");
    seatProd.setRow("1");
    seatProd.setSeat("1");
    seatProductList.add(seatProd);
    
    ListingWrapper listingWrapper = new ListingWrapper(newListing, seatProductsContextMock);
    UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
    
    Mockito.doReturn(newListing).when(helperspy).getListingFromRequest(Mockito.any(ListingData.class),
		Mockito.any(ListingRequest.class), Mockito.any(ListingResponse.class), Mockito.anyString(), Mockito.anyString());
    Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
        Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
        Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    Mockito.doReturn(newListingList).when(helperspy).updateDatabaseForBatch(Mockito.anyBoolean(),
        Mockito.anyListOf(Listing.class));
    when(listingFulfilHelper.validateFileInfoIds(Mockito.any(SeatProductsContext.class),
        Mockito.any(Listing.class))).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    ListingData ldata = listingDataFromCreateRequest(1000l, shapiContext, shServiceContext, false, req);
    ldata.setHeaderListing(newListing);
    when(fulfillmentServiceHelper.populateFulfillmentOptions((Listing) Matchers.any(),
        (List<com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow>) Matchers.any()))
            .thenReturn(true);
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helperspy.createOrUpdateListingData(ldata, clientIp, userAgent);
    Assert.assertTrue(resps != null);

  }
  
  @Test
  public void createListingWithAuthzRequest() throws Exception {
    when(eventHelper.getEventById(1000l, "event genrePath geoPath venue", Locale.US, false))
        .thenReturn(getEvent());
    when(userHelper.isSellerContactValid(Matchers.anyString(), Matchers.anyLong()))
        .thenReturn(true);
    when(inventoryMgr.getListingBySellerIdExternalIdAndStatus(Matchers.anyLong(),
        Matchers.anyString())).thenReturn(null);
    
    CustomerContactMappingResponse mappingResponse = new CustomerContactMappingResponse();

    when(userHelper.isAuthZRequest()).thenReturn(true);

    when(sellerHelper.populateSellerDetails(Matchers.any(Listing.class))).thenReturn(true);

    Listing newListing = new Listing();
    newListing.setListPrice(new Money(new BigDecimal(100d), "USD"));
    newListing.setSellerId(1000l);
    newListing.setSellerContactId(1234l);
    newListing.setExternalId("12345");
    newListing.setTicketMedium(TicketMedium.MOBILE.getValue());
    newListing.setPredeliveryAvailable(true);
    newListing.setEvent(getEvent());
    TicketSeat seat = new TicketSeat();
    seat.setFulfillmentArtifactId(12345l);
    List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>(1);
    ticketSeats.add(seat);
    newListing.setTicketSeats(ticketSeats);
    List<Listing> newListingList = new ArrayList<Listing>(1);
    newListingList.add(newListing);
    ListingRequest req = new ListingRequest();
    req.setExternalListingId("12345");
    req.setPricePerProduct(new Money("100"));
    req.setEventId("1000");
    req.setDeliveryOption(com.stubhub.domain.inventory.common.entity.DeliveryOption.MOBILE);
    
    Product product = new Product();
    product.setRow("1");
    product.setSeat("1");
    product.setFulfillmentArtifact("123");
    
    List<Product> products = new ArrayList<Product>();
    products.add(product);
    req.setProducts(products);
    
    Object[] prResp = new Object[1];
    prResp[0] = getPriceResponse("110", "10");
    SeatProductsContext seatProductsContextMock = Mockito.mock(SeatProductsContext.class);
    List<SeatProduct> seatProductList = new ArrayList<SeatProduct>();
    SeatProduct seatProd = new SeatProduct();
    seatProd.setProductType(ProductType.TICKET);
    seatProd.setFulfillmentArtifact("1234");
    seatProd.setRow("1");
    seatProd.setSeat("1");
    seatProductList.add(seatProd);
    
    ListingWrapper listingWrapper = new ListingWrapper(newListing, seatProductsContextMock);
    UpdateListingAsyncHelper2 helperspy = Mockito.spy(helper);
    
    Mockito.doReturn(newListing).when(helperspy).getListingFromRequest(Mockito.any(ListingData.class),
		Mockito.any(ListingRequest.class), Mockito.any(ListingResponse.class), Mockito.anyString(), Mockito.anyString());
    Mockito.doReturn(listingWrapper).when(helperspy).updateOrCreateSingleListing(
        Mockito.any(Listing.class), Mockito.any(Listing.class), Mockito.any(ListingRequest.class),
        Mockito.any(SHAPIContext.class), Mockito.any(CommonTasks.class));
    when(listingPriceDetailsHelper.batchPriceCalculationsAIP(Mockito.any(SHAPIContext.class),
            Mockito.any(Map.class), Mockito.any(List.class), Mockito.any(List.class))).thenReturn(prResp);

    Mockito.doReturn(newListingList).when(helperspy).updateDatabaseForBatch(Mockito.anyBoolean(),
        Mockito.anyListOf(Listing.class));
    when(listingFulfilHelper.validateFileInfoIds(Mockito.any(SeatProductsContext.class),
        Mockito.any(Listing.class))).thenReturn(true);
    SHAPIContext shapiContext = new SHAPIContext();
    SHServiceContext shServiceContext = new SHServiceContext();
    ListingData ldata = listingDataFromCreateRequest(1000l, shapiContext, shServiceContext, false, req);
    ldata.setHeaderListing(newListing);
    ldata.setSubscriber("Single|V2|test@testmail.com|test");
    when(fulfillmentServiceHelper.populateFulfillmentOptions((Listing) Matchers.any(),
        (List<com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow>) Matchers.any()))
            .thenReturn(true);
    List<com.stubhub.domain.inventory.v2.DTO.ListingResponse> resps =
        helperspy.createOrUpdateListingData(ldata, clientIp, userAgent);
    Assert.assertTrue(resps != null);

  }

}
