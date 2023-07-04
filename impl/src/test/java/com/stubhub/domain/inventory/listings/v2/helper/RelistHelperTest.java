package com.stubhub.domain.inventory.listings.v2.helper;

import java.math.BigDecimal;
import java.util.*;

import javax.ws.rs.core.HttpHeaders;

import com.stubhub.domain.catalog.read.v3.intf.common.dto.response.CommonAttribute;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event;
import com.stubhub.domain.inventory.listings.v2.util.*;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stubhub.domain.accountmanagement.services.orders.v3.intf.dto.OrderDetailsV3DTO;
import com.stubhub.domain.accountmanagement.services.orders.v3.intf.dto.OrderItem;
import com.stubhub.domain.accountmanagement.services.orders.v3.intf.dto.TicketTrait;
import com.stubhub.domain.fulfillment.window.v1.intf.EventFulfillmentWindowResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.FulfillmentMethodResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.FulfillmentWindowResponse;
import com.stubhub.domain.infrastructure.config.client.core.SHConfig;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.biz.v2.impl.util.FulfillmentServiceAdapter;
import com.stubhub.domain.inventory.biz.v2.intf.FulfillmentArtifactMgr;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.datamodel.entity.FileInfo;
import com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeatDetails;
import com.stubhub.domain.inventory.datamodel.entity.enums.TaxpayerStatusEnum;
import com.stubhub.domain.inventory.metadata.v1.event.util.SellerPaymentUtil;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.v2.DTO.RelistItem;
import com.stubhub.domain.inventory.v2.DTO.RelistListing;
import com.stubhub.domain.inventory.v2.DTO.RelistRequest;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.domain.user.payments.v2.intf.CustomerPaymentInstrumentDetailsV2;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.common.util.DateUtil;
import com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContext;

import static com.stubhub.domain.inventory.listings.v2.util.EventHelper.DYNAMIC_ATTRIBUTE_LIABILITY_WAIVER;
import static org.mockito.Matchers.eq;
import static org.testng.Assert.fail;


public class RelistHelperTest extends SHInventoryTest {
  @Mock
  private FulfillmentServiceAdapter fulfillmentServiceAdapter;

  private RelistRequest request = null;
  @Mock
  private HttpHeaders headers;

  private ExtendedSecurityContext securityContext;
  private SHServiceContext shServiceContext;

  @Mock
  private OrderDetailsHelper orderHelper;
  
  private SHConfig shConfig;
  
  @Mock
  private FulfillmentArtifactMgr fulfillmentArtifactMgr;

  @Mock
  private TicketSeatMgr ticketSeatMgr;

  @Mock
  private InventoryMgr inventoryMgr;

  @Mock
  private FulfillmentServiceHelper fulfillmentServiceHelper;

  @Mock
  private UserHelper userHelper;

  @Mock
  private SellerHelper sellerHelper;
  private SellerPaymentUtil sellerPaymentUtil;

  @InjectMocks
  private RelistHelper relistHelper;

  @BeforeTest
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    shServiceContext = mockServiceContext("1000");
    headers = super.mockHeaders(null, null);
    sellerPaymentUtil = new SellerPaymentUtil(shConfig);
    
	shConfig = Mockito.mock(SHConfig.class);
	Mockito.when(shConfig.getProperty(eq("paymentmethod.required.percentage"), Matchers.anyString() )).thenReturn("0");
	ReflectionTestUtils.setField(sellerPaymentUtil, "shConfig", shConfig );

    securityContext = mockSecurityContext("1000");
    Map<String, Object> extendedInfo = new HashMap<String, Object>();
    extendedInfo.put("http://stubhub.com/claims/subscriber", "api_us_sell_indy03@testmail.com");
    Mockito.when(securityContext.getExtendedInfo()).thenReturn(extendedInfo);
    shServiceContext.setExtendedSecurityContext(securityContext);
	fulfillmentServiceAdapter = Mockito.mock(FulfillmentServiceAdapter.class);
	ReflectionTestUtils.setField(relistHelper, "fulfillmentServiceAdapter", fulfillmentServiceAdapter);
  }

  /**
   * Relist with an empty relist request
   * 
   * @result throws a ListingBuisnessException saying the order id is invalid
   */

  @Test
  public void testValidateEmptyRequest() throws Exception {
    request = new RelistRequest();
    try {
      relistHelper.validateRelistListings(request);
    } catch (ListingBusinessException ex) {
      Assert
          .assertTrue(ex.getListingError().getMessage().equals("No listings found in the request"));
    }
  }

  /**
   * Relist with an empty relist listing
   * 
   * @result throws a ListingBuisnessException
   */

  @Test
  public void testValidateEmptyListings() throws Exception {
    request = new RelistRequest();
    try {
      relistHelper.validateRelistListings(getEmptyListings());
    } catch (ListingBusinessException ex) {
      Assert
          .assertTrue(ex.getListingError().getMessage().equals("No listings found in the request"));
    }
  }

  private RelistRequest getEmptyListings() {
    request = new RelistRequest();
    List<RelistListing> listOfListings = new ArrayList<RelistListing>();
    request.setListings(listOfListings);
    return request;
  }

  /**
   * Relist with an null order
   * 
   * @result throws a ListingBuisnessException
   */
  @Test
  public void validateRequestNullOrder() throws Exception {
    request = new RelistRequest();
    try {
      relistHelper.validateRelistListings(getNullOrderListings());
    } catch (ListingBusinessException ex) {
      Assert.assertTrue(ex.getListingError().getMessage().equals("Invalid Order Id"));
    }
  }

  private RelistRequest getNullOrderListings() {
    request = getEmptyListings();
    RelistListing listing = new RelistListing();
    List<RelistListing> listOfListings = new ArrayList<RelistListing>();
    listOfListings.add(listing);
    request.setListings(listOfListings);
    return request;
  }

  /**
   * Relist with an negative order
   * 
   * @result throws a ListingBuisnessException
   */

  @Test
  public void validateRelistRequestNegativeOrder() throws Exception {
    request = new RelistRequest();
    try {
      relistHelper.validateRelistListings(getNegativeOrderListings());
    } catch (ListingBusinessException ex) {
      Assert.assertTrue(ex.getListingError().getMessage().equals("Invalid Order Id"));
    }
  }

  private RelistRequest getNegativeOrderListings() {
    request = getEmptyListings();
    RelistListing listing = new RelistListing();
    listing.setOrderId((long) -1);
    request.getListings().add(listing);
    return request;
  }

  /**
   * Relist with an null price
   * 
   * @result throws a ListingBuisnessException
   */

  @Test
  public void validateRelistListingNullPrice() throws Exception {
    request = new RelistRequest();
    try {
      relistHelper.validateRelistListings(getNullPriceListings());
    } catch (ListingBusinessException ex) {
      Assert.assertTrue(ex.getListingError().getMessage().equals("Invalid pricePerItem"));
    }
  }

  private RelistRequest getNullPriceListings() {
    request = getEmptyListings();
    RelistListing listing = new RelistListing();
    listing.setOrderId((long) 1000);
    listing.setPricePerItem(null);
    request.getListings().add(listing);
    return request;
  }
  
  @Test
    public void validateRelistListingEmptyPrice() throws Exception {
	  Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
      request = new RelistRequest();
      try {
        relistHelper.validateListingWithOrderDetails(getEmptyPriceListings(), null, Locale.US);
      } catch (Exception ex) {
        Assert.assertTrue(ex!=null);
      }
    }
    
    private RelistRequest getEmptyPriceListings() {
  	  	
  	    request = getEmptyListings();
  	    
  	    RelistListing listing = new RelistListing();
  	    listing.setOrderId((long) 1000);
  	    listing.setPricePerItem(null);
  	    listing.setToEmailId("70@testmail.com");
  	    listing.setToCustomerGUID("6C21FF95408F3BC0E04400144FB7AAA6");
  	    
  	    RelistItem relistItem = new RelistItem();
  	    relistItem.setItemId("testing");
  	    List<RelistItem> items = new ArrayList<>();
  	    items.add(relistItem);
  	    listing.setItems(items);
  	    List<RelistListing> relistListings = new ArrayList<>();
  	    relistListings.add(listing);
  	    request.setListings(relistListings);
  	    return request;
  	  }


  /**
   * Relist with two listings and one of them with empty item
   * 
   * @result throws a ListingBuisnessException
   */

  @Test
  public void validateRelistTwoListingWithOneEmptyItem() throws Exception {
    request = new RelistRequest();
    try {
      relistHelper.validateRelistListings(getOrdersOneEmptyItem());
    } catch (ListingBusinessException ex) {
      Assert.assertTrue(ex.getListingError().getMessage()
          .equals("All the listings with the same orderId should have items"));
    }
  }

  private RelistRequest getOrdersOneEmptyItem() {
    request = getEmptyListings();
    List<RelistListing> listOfListings = new ArrayList<RelistListing>();
    RelistListing listing = new RelistListing();
    listing.setOrderId((long) 1000);
    listing.setPricePerItem(new BigDecimal(50.20));
    List<RelistItem> listOfItems = new ArrayList<RelistItem>();
    RelistItem item = new RelistItem();
    item.setItemId("100");
    listOfItems.add(item);
    listing.setItems(listOfItems);
    listOfListings.add(listing);

    RelistListing listing2 = new RelistListing();
    listing2.setOrderId((long) 1000);
    listing2.setPricePerItem(new BigDecimal(90.20));
    listOfListings.add(listing2);

    request.setListings(listOfListings);
    return request;
  }

  /**
   * Relist with two listings with same items
   * 
   * @result throws a ListingBuisnessException
   */

  @Test
  public void validateRelistTwoListingWithSameItem() throws Exception {
    request = new RelistRequest();
    try {
      relistHelper.validateRelistListings(getTwoOrdersWithSameItem());
    } catch (ListingBusinessException ex) {
      Assert.assertTrue(ex.getListingError().getMessage()
          .equals("Multiple listings with the same orderId cannot have same items"));
    }
  }

  private RelistRequest getTwoOrdersWithSameItem() {
    request = getEmptyListings();
    List<RelistListing> listOfListings = new ArrayList<RelistListing>();
    RelistListing listing = new RelistListing();
    listing.setOrderId((long) 1000);
    listing.setPricePerItem(new BigDecimal(50.20));
    List<RelistItem> listOfItems = new ArrayList<RelistItem>();
    RelistItem item = new RelistItem();
    item.setItemId("100");
    listOfItems.add(item);
    listing.setItems(listOfItems);
    listOfListings.add(listing);

    RelistListing listing2 = new RelistListing();
    listing2.setOrderId((long) 1000);
    listing2.setPricePerItem(new BigDecimal(90.20));
    List<RelistItem> listOfItems2 = new ArrayList<RelistItem>();
    RelistItem item2 = new RelistItem();
    item2.setItemId("100");
    listOfItems2.add(item2);
    listing2.setItems(listOfItems2);
    listOfListings.add(listing2);

    request.setListings(listOfListings);
    return request;
  }

  /**
   * Relist listing with same order without items
   * 
   * @result throws a ListingBuisnessException
   */

  @Test
  public void validateRelistListingSameItems() throws Exception {
    request = new RelistRequest();
    try {
      relistHelper.validateRelistListings(getOrdersWithSameItem());
    } catch (ListingBusinessException ex) {
      Assert.assertTrue(ex.getListingError().getMessage()
          .equals("Multiple listings with the same orderId should have items"));
    }
  }

  private RelistRequest getOrdersWithSameItem() {
    request = getEmptyListings();
    List<RelistListing> listOfListings = new ArrayList<RelistListing>();
    RelistListing listing = new RelistListing();
    listing.setOrderId((long) 1000);
    listing.setPricePerItem(new BigDecimal(50.20));
    List<RelistItem> listOfItems = new ArrayList<RelistItem>();
    RelistItem item = new RelistItem();
    item.setItemId("100");
    listOfItems.add(item);
    listOfListings.add(listing);

    RelistListing listing2 = new RelistListing();
    listing2.setOrderId((long) 1000);
    listing2.setPricePerItem(new BigDecimal(50.20));
    List<RelistItem> listOfItems2 = new ArrayList<RelistItem>();
    RelistItem item2 = new RelistItem();
    item2.setItemId("100");
    listOfItems2.add(item2);
    listOfListings.add(listing2);
    request.setListings(listOfListings);
    return request;
  }

  /**
   * Relist listing with order details
   * 
   * @result orderDetails validated
   */
   @Test
  public void validateListingWithOrderDetails() throws Exception {
    request = new RelistRequest();
    Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(getOrderDetails());
    Mockito.when(ticketSeatMgr.findActiveTicketSeatsByOriginalSeatIds(Mockito.anyList())).thenReturn(null);
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);

    Map<Long, List<OrderItem>> orderDetails =
        relistHelper.validateListingWithOrderDetails(getOrders(), orderDetailsMaps, Locale.US);
    Assert.assertNotNull(orderDetails.get(1000L));

  }
   
   /**
    * Relist listing with order details
    * 
    * @result orderDetails validated
    */
    @Test
   public void validateListingWithOrderDetailsTransfer() throws Exception {
     request = new RelistRequest();
     Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
     OrderDetailsHelper orderHelper =
         (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
     Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(getOrderDetails());
     Mockito.when(ticketSeatMgr.findActiveTicketSeatsByOriginalSeatIds(Mockito.anyList())).thenReturn(null);
     setBeanProperty(relistHelper, "OrderHelper", orderHelper);

     Map<Long, List<OrderItem>> orderDetails =
         relistHelper.validateListingWithOrderDetails(getOrdersTransfer(), orderDetailsMaps, Locale.US);
     Assert.assertNotNull(orderDetails.get(1000L));

   }
    /**
     * Relist listing with order details
     * 
     * @result orderDetails validated
     */
     @Test
    public void validateListingWithOrderDetailsAlreadyTransfered() throws Exception {
      request = new RelistRequest();
      OrderDetailsHelper orderHelper =
          (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
      Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(getOrderDetailsTransferred());
      Mockito.when(ticketSeatMgr.findActiveTicketSeatsByOriginalSeatIds(Mockito.anyList())).thenReturn(null);
      setBeanProperty(relistHelper, "OrderHelper", orderHelper);
      Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
      try {
		Map<Long, List<OrderItem>> orderDetails =
		      relistHelper.validateListingWithOrderDetails(getOrdersTransfer(), orderDetailsMaps, Locale.US);
		  Assert.assertNotNull(orderDetails.get(1000L));
	}catch (ListingBusinessException ex) {
      Assert
          .assertTrue(ex.getListingError().getMessage().equals("The order has one or more items that were already transferred"));
    }

    }

    @Test
    public void testValidateForLiabilityWaiver() throws Exception {
      request = new RelistRequest();
      Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);

      OrderDetailsHelper orderHelper =
              (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
      Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(getOrderDetails());
      Mockito.when(ticketSeatMgr.findActiveTicketSeatsByOriginalSeatIds(Mockito.anyList())).thenReturn(null);
      EventHelper eventHelper = Mockito.mock(EventHelper.class);
      setBeanProperty(relistHelper, "orderHelper", orderHelper);
      setBeanProperty(relistHelper, "eventHelper", eventHelper);

      Event eventV3 = new Event();
      List<CommonAttribute> dynamicAttributes = new ArrayList<>();
      CommonAttribute commonAttribute = new CommonAttribute();
      commonAttribute.setName(DYNAMIC_ATTRIBUTE_LIABILITY_WAIVER);
      commonAttribute.setValue("true");
      dynamicAttributes.add(commonAttribute);
      eventV3.setDynamicAttributes(dynamicAttributes);

      Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), eq(Locale.US), eq(false))).thenReturn(eventV3);

      List<OrderItem> items = orderDetailsMaps.get(1000L).getItems();

      //add another item, with different seat id
      OrderItem anotherOrderItem = getOrderDetailsV3DTO(1000L).get(1000L).getItems().get(0);
      anotherOrderItem.setSeatId(1001L);
      items.add(anotherOrderItem);

      for (OrderItem item : items) {
        item.setEventId(1L);
        item.setSaleId(123L);
      }

      RelistRequest relistRequest = getOrders();

      List<RelistItem> reqItems = new ArrayList<>();
      RelistItem relistItem = new RelistItem();
      relistItem.setItemId("1001");
      reqItems.add(relistItem);
      relistRequest.getListings().get(0).setItems(reqItems);

      try {
        Map<Long, List<OrderItem>> orderDetails =
                relistHelper.validateListingWithOrderDetails(relistRequest, orderDetailsMaps, Locale.US);
        fail("exception before me");
      } catch (ListingBusinessException ex) {
        Assert.assertEquals(ex.getMessage(), "Event is LiabilityWaiver, not support split relist");
      }
    }

  @Test
  public void testAisleValidCase() {
    List<OrderItem> list1 = new ArrayList<OrderItem>();
    OrderItem item1 = new OrderItem();
    item1.setId(100L);
    item1.setListingId(1000L);
    item1.setSeatId(2000L);
    List<TicketTrait> listOfTraits = new ArrayList<TicketTrait>();
    TicketTrait tt = new TicketTrait();
    tt.setId(101L);
    listOfTraits.add(tt);
    item1.setTicketTraits(listOfTraits);
    list1.add(item1);

    List<OrderItem> list2 = new ArrayList<OrderItem>();
    OrderItem item2 = new OrderItem();
    item2.setId(400L);
    item2.setListingId(1000L);
    item2.setSeatId(2000L);
    List<TicketTrait> listOfTraits1 = new ArrayList<TicketTrait>();
    TicketTrait tt1 = new TicketTrait();
    tt1.setId(101L);
    listOfTraits1.add(tt1);
    item2.setTicketTraits(listOfTraits1);
    list2.add(item2);
    relistHelper.validateRelistWithTicketFeatures(list1, list2);
  }

  @Test
  public void testAisleValidCase1() {
    List<OrderItem> list1 = new ArrayList<OrderItem>();
    OrderItem item1 = new OrderItem();
    item1.setId(100L);
    item1.setListingId(1000L);
    item1.setSeatId(2000L);
    List<TicketTrait> listOfTraits = new ArrayList<TicketTrait>();
    TicketTrait tt = new TicketTrait();
    tt.setId(101L);
    listOfTraits.add(tt);
    item1.setTicketTraits(listOfTraits);
    list1.add(item1);

    List<OrderItem> list2 = new ArrayList<OrderItem>();
    OrderItem item2 = new OrderItem();
    item2.setId(400L);
    item2.setListingId(1000L);
    item2.setSeatId(2000L);
    List<TicketTrait> listOfTraits1 = new ArrayList<TicketTrait>();
    TicketTrait tt1 = new TicketTrait();
    tt1.setId(102L);
    listOfTraits1.add(tt1);
    item2.setTicketTraits(listOfTraits1);
    list2.add(item2);
    relistHelper.validateRelistWithTicketFeatures(list1, list2);
  }

  @Test
  public void testAisleValidCase2() {
    List<OrderItem> list1 = new ArrayList<OrderItem>();
    OrderItem item1 = new OrderItem();
    item1.setId(100L);
    item1.setListingId(1000L);
    item1.setSeatId(2000L);
    List<TicketTrait> listOfTraits = new ArrayList<TicketTrait>();
    TicketTrait tt = new TicketTrait();
    tt.setId(101L);
    listOfTraits.add(tt);
    item1.setTicketTraits(listOfTraits);
    list1.add(item1);
    OrderItem item11 = new OrderItem();
    item11.setId(100L);
    item11.setListingId(1000L);
    item11.setSeatId(2000L);
    List<TicketTrait> listOfTraitss = new ArrayList<TicketTrait>();
    TicketTrait ttt = new TicketTrait();
    ttt.setId(101L);
    listOfTraits.add(ttt);
    item11.setTicketTraits(listOfTraitss);
    list1.add(item11);

    List<OrderItem> list2 = new ArrayList<OrderItem>();
    OrderItem item2 = new OrderItem();
    item2.setId(400L);
    item2.setListingId(1000L);
    item2.setSeatId(2000L);
    List<TicketTrait> listOfTraits1 = new ArrayList<TicketTrait>();
    TicketTrait tt1 = new TicketTrait();
    tt1.setId(102L);
    listOfTraits1.add(tt1);
    item2.setTicketTraits(listOfTraits1);
    list1.add(item2);
    relistHelper.validateRelistWithTicketFeatures(list1, list2);
  }

  @Test
  public void testAisleErrorCase() {
    List<OrderItem> list1 = new ArrayList<OrderItem>();
    OrderItem item1 = new OrderItem();
    item1.setId(100L);
    item1.setListingId(1000L);
    item1.setSeatId(2000L);
    List<TicketTrait> listOfTraits = new ArrayList<TicketTrait>();
    TicketTrait tt = new TicketTrait();
    tt.setId(101L);
    listOfTraits.add(tt);
    item1.setTicketTraits(listOfTraits);
    list1.add(item1);

    List<OrderItem> list2 = new ArrayList<OrderItem>();
    OrderItem item2 = new OrderItem();
    item2.setId(400L);
    item2.setListingId(1000L);
    item2.setSeatId(2000L);
    List<TicketTrait> listOfTraits1 = new ArrayList<TicketTrait>();
    TicketTrait tt1 = new TicketTrait();
    tt1.setId(101L);
    listOfTraits1.add(tt1);
    item2.setTicketTraits(listOfTraits1);
    list2.add(item2);
    OrderItem item3 = new OrderItem();
    item3.setId(400L);
    item3.setListingId(1000L);
    item3.setSeatId(2000L);
    List<TicketTrait> listOfTraits2 = new ArrayList<TicketTrait>();
    TicketTrait tt2 = new TicketTrait();
    tt2.setId(101L);
    listOfTraits2.add(tt2);
    item2.setTicketTraits(listOfTraits2);
    list2.add(item2);
    try {
      relistHelper.validateRelistWithTicketFeatures(list1, list2);
    } catch (ListingBusinessException ex) {
      Assert.assertEquals(ex.getLocalizedMessage(), "Invalid splitOption with AISLE ticket traits");
    }
  }

  @Test
  public void testCloneMethod() throws Exception {
    Long orderId = 12L;
    request = new RelistRequest();
    List<RelistListing> listings = new ArrayList<RelistListing>();
    RelistListing e = new RelistListing();
    e.setOrderId(orderId);
    listings.add(e);
    request.setListings(listings);
    OrderDetailsV3DTO orderDet = getOrderDetails();
    orderDet.setOrderStatus("Delivered");
    ListingResponse response1 = new ListingResponse();
    response1.setId(orderId.toString());

    Map<Long, List<OrderItem>> map = new HashMap<Long, List<OrderItem>>();
    List<ListingResponse> list = new ArrayList<ListingResponse>();
    list.add(response1);
    List<OrderItem> order = new ArrayList<OrderItem>();
    order.add(getOrderDetails().getItems().get(0));
    map.put(orderId, order);
    Listing pdfListing = new Listing();
    pdfListing.setTicketMedium(2);
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setFulfillmentArtifactId(2000L);
    ArrayList<TicketSeat> arrayOfTicketSeat = new ArrayList<TicketSeat>();
    arrayOfTicketSeat.add(ticketSeat);
    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(orderDet);
    Mockito.when(ticketSeatMgr.findTicketSeatsByTicketId(Mockito.anyLong()))
        .thenReturn(arrayOfTicketSeat);
    Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(pdfListing);
    Mockito.when(inventoryMgr.updateListing(Mockito.any(Listing.class))).thenReturn(new Listing());
    Mockito.when(fulfillmentServiceHelper.calculateSaleEndDate(Mockito.any(Listing.class), Mockito.anyListOf(FulfillmentWindow.class)))
        .thenReturn(Calendar.getInstance());
    Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Mockito.anyLong(), Mockito.anyLong())).thenReturn(getFulfillmentWindows());
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);
    relistHelper.cloneFileInfoIds(list, map, request, "AB1234");
  }
  private EventFulfillmentWindowResponse getFulfillmentWindows()
  {
	  EventFulfillmentWindowResponse event=new EventFulfillmentWindowResponse();
	  event.setEventId(1234l);
	  Collection<FulfillmentWindowResponse> collection=new ArrayList<FulfillmentWindowResponse>();
	  FulfillmentWindowResponse rep=new FulfillmentWindowResponse();
	  FulfillmentMethodResponse method=new FulfillmentMethodResponse();
	  FulfillmentWindow window=new FulfillmentWindow();
	  window.setFulfillmentMethodId(4L);
	 rep.setFulfillmentMethod(method);
	 method.setId(4L);
	  collection.add(rep);
	  
	  event.setFulfillmentWindows(collection);
	  return event;
  }
  @Test
  public void testCloneMethodTicketMedium() throws Exception {
    Long orderId = 12L;
    request = new RelistRequest();
    List<RelistListing> listings = new ArrayList<RelistListing>();
    RelistListing e = new RelistListing();
    e.setOrderId(orderId);
    listings.add(e);
    request.setListings(listings);
    OrderDetailsV3DTO orderDet = getOrderDetails();
    orderDet.setOrderStatus("Delivered");
    ListingResponse response1 = new ListingResponse();
    response1.setId(orderId.toString());

    Map<Long, List<OrderItem>> map = new HashMap<Long, List<OrderItem>>();
    List<ListingResponse> list = new ArrayList<ListingResponse>();
    list.add(response1);
    List<OrderItem> order = new ArrayList<OrderItem>();
    order.add(getOrderDetails().getItems().get(0));
    map.put(orderId, order);
    Listing pdfListing = new Listing();
    pdfListing.setTicketMedium(1);
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setFulfillmentArtifactId(2000L);
    ArrayList<TicketSeat> arrayOfTicketSeat = new ArrayList<TicketSeat>();
    arrayOfTicketSeat.add(ticketSeat);
    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(orderDet);
    Mockito.when(ticketSeatMgr.findTicketSeatsByTicketId(Mockito.anyLong()))
        .thenReturn(arrayOfTicketSeat);
    Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(pdfListing);
    Mockito.when(inventoryMgr.updateListing(Mockito.any(Listing.class))).thenReturn(new Listing());
    Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Mockito.anyLong(), Mockito.anyLong())).thenReturn(getFulfillmentWindows());
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);
    relistHelper.cloneFileInfoIds(list, map, request, "AB1234");
  }

  @Test
  public void testCloneMethodListingNull() throws Exception {
    Long orderId = 12L;
    request = new RelistRequest();
    List<RelistListing> listings = new ArrayList<RelistListing>();
    RelistListing e = new RelistListing();
    e.setOrderId(orderId);
    listings.add(e);
    request.setListings(listings);
    OrderDetailsV3DTO orderDet = getOrderDetails();
    orderDet.setOrderStatus("Delivered");
    ListingResponse response1 = new ListingResponse();
    response1.setId(orderId.toString());

    Map<Long, List<OrderItem>> map = new HashMap<Long, List<OrderItem>>();
    List<ListingResponse> list = new ArrayList<ListingResponse>();
    list.add(response1);
    List<OrderItem> order = new ArrayList<OrderItem>();
    order.add(getOrderDetails().getItems().get(0));
    map.put(orderId, order);
    Listing pdfListing = null;
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setFulfillmentArtifactId(2000L);
    ArrayList<TicketSeat> arrayOfTicketSeat = new ArrayList<TicketSeat>();
    arrayOfTicketSeat.add(ticketSeat);
    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(orderDet);
    Mockito.when(ticketSeatMgr.findTicketSeatsByTicketId(Mockito.anyLong()))
        .thenReturn(arrayOfTicketSeat);
    Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(pdfListing);
    Mockito.when(inventoryMgr.updateListing(Mockito.any(Listing.class))).thenReturn(new Listing());
    Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Mockito.anyLong(), Mockito.anyLong())).thenReturn(getFulfillmentWindows());
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);
    relistHelper.cloneFileInfoIds(list, map, request, "AB1234");
  }
  
  @Test
  public void testCloneMethodTicketMediumNull() throws Exception {
    Long orderId = 12L;
    request = new RelistRequest();
    List<RelistListing> listings = new ArrayList<RelistListing>();
    RelistListing e = new RelistListing();
    e.setOrderId(orderId);
    listings.add(e);
    request.setListings(listings);
    OrderDetailsV3DTO orderDet = getOrderDetails();
    orderDet.setOrderStatus("Delivered");
    ListingResponse response1 = new ListingResponse();
    response1.setId(orderId.toString());

    Map<Long, List<OrderItem>> map = new HashMap<Long, List<OrderItem>>();
    List<ListingResponse> list = new ArrayList<ListingResponse>();
    list.add(response1);
    List<OrderItem> order = new ArrayList<OrderItem>();
    order.add(getOrderDetails().getItems().get(0));
    map.put(orderId, order);
    Listing pdfListing = new Listing();
    pdfListing.setTicketMedium(null);
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setFulfillmentArtifactId(2000L);
    ArrayList<TicketSeat> arrayOfTicketSeat = new ArrayList<TicketSeat>();
    arrayOfTicketSeat.add(ticketSeat);
    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(orderDet);
    Mockito.when(ticketSeatMgr.findTicketSeatsByTicketId(Mockito.anyLong()))
        .thenReturn(arrayOfTicketSeat);
    Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(pdfListing);
    Mockito.when(inventoryMgr.updateListing(Mockito.any(Listing.class))).thenReturn(new Listing());
    Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Mockito.anyLong(), Mockito.anyLong())).thenReturn(getFulfillmentWindows());
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);
    relistHelper.cloneFileInfoIds(list, map, request, "AB1234");
  }

  @Test
  public void testCloneViewedMethod() throws Exception {
    request = new RelistRequest();
    List<RelistListing> listings = new ArrayList<RelistListing>();
    RelistListing e = new RelistListing();
    e.setOrderId(12L);
    listings.add(e);
    request.setListings(listings);
    OrderDetailsV3DTO orderDet = getOrderDetails();
    orderDet.setOrderStatus("Viewed");
    ListingResponse response1 = new ListingResponse();
    response1.setId("12");

    Map<Long, List<OrderItem>> map = new HashMap<Long, List<OrderItem>>();
    List<ListingResponse> list = new ArrayList<ListingResponse>();
    list.add(response1);
    List<OrderItem> order = new ArrayList<OrderItem>();
    order.add(getOrderDetails().getItems().get(0));
    order.get(0).setSeatId(12L);
    map.put(new Long(12), order);
    Listing pdfListing = new Listing();
    pdfListing.setTicketMedium(2);

    FileInfo fileInfo = new FileInfo();
    fileInfo.setFileName("OldFileInfo");



    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(orderDet);
    ArrayList<TicketSeat> value = new ArrayList<TicketSeat>();
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setFulfillmentArtifactId(2000L);
    value.add(ticketSeat);
    Mockito.when(ticketSeatMgr.findTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(value);
    Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(pdfListing);
    Mockito.when(inventoryMgr.updateListing(Mockito.any(Listing.class))).thenReturn(new Listing());
    TicketSeatDetails tsd = new TicketSeatDetails();
    tsd.setFileInfoId(123L);
    Mockito.when(fulfillmentArtifactMgr.getTicketSeatDetails(Mockito.anyLong())).thenReturn(tsd);
    Mockito.when(fulfillmentArtifactMgr.findByIdForPDF(Mockito.anyLong())).thenReturn(fileInfo);
    Mockito.when(fulfillmentServiceHelper.calculateSaleEndDate(Mockito.any(Listing.class), Mockito.anyListOf(FulfillmentWindow.class)))
        .thenReturn(Calendar.getInstance());
    Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Mockito.anyLong(), Mockito.anyLong())).thenReturn(getFulfillmentWindows());
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);
    relistHelper.cloneFileInfoIds(list, map, request, "AB1234");
  }
  
  @Test
  public void testCloneViewedMethodApproved() throws Exception {
    request = new RelistRequest();
    List<RelistListing> listings = new ArrayList<RelistListing>();
    RelistListing e = new RelistListing();
    e.setOrderId(12L);
    listings.add(e);
    request.setListings(listings);
    OrderDetailsV3DTO orderDet = getOrderDetails();
    orderDet.setOrderStatus("Approved");
    ListingResponse response1 = new ListingResponse();
    response1.setId("12");

    Map<Long, List<OrderItem>> map = new HashMap<Long, List<OrderItem>>();
    List<ListingResponse> list = new ArrayList<ListingResponse>();
    list.add(response1);
    List<OrderItem> order = new ArrayList<OrderItem>();
    order.add(getOrderDetails().getItems().get(0));
    order.get(0).setSeatId(12L);
    map.put(new Long(12), order);
    Listing pdfListing = new Listing();
    pdfListing.setTicketMedium(2);

    FileInfo fileInfo = new FileInfo();
    fileInfo.setFileName("OldFileInfo");



    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(orderDet);
    ArrayList<TicketSeat> value = new ArrayList<TicketSeat>();
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setFulfillmentArtifactId(2000L);
    value.add(ticketSeat);
    Mockito.when(ticketSeatMgr.findTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(value);
    Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(pdfListing);
    Mockito.when(inventoryMgr.updateListing(Mockito.any(Listing.class))).thenReturn(new Listing());
    TicketSeatDetails tsd = new TicketSeatDetails();
    tsd.setFileInfoId(123L);
    Mockito.when(fulfillmentArtifactMgr.getTicketSeatDetails(Mockito.anyLong())).thenReturn(tsd);
    Mockito.when(fulfillmentArtifactMgr.findByIdForPDF(Mockito.anyLong())).thenReturn(fileInfo);
    Mockito.when(fulfillmentServiceHelper.calculateSaleEndDate(Mockito.any(Listing.class), Mockito.anyListOf(FulfillmentWindow.class)))
        .thenReturn(Calendar.getInstance());
    Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Mockito.anyLong(), Mockito.anyLong())).thenReturn(getFulfillmentWindows());
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);
    relistHelper.cloneFileInfoIds(list, map, request, "AB1234");
  }

  @Test
  public void testCloneViewedMethodNonPdf() throws Exception {
    request = new RelistRequest();
    List<RelistListing> listings = new ArrayList<RelistListing>();
    RelistListing e = new RelistListing();
    e.setOrderId(12L);
    listings.add(e);
    request.setListings(listings);
    OrderDetailsV3DTO orderDet = getOrderDetails();
    orderDet.setOrderStatus("Viewed");
    ListingResponse response1 = new ListingResponse();
    response1.setId("12");

    Map<Long, List<OrderItem>> map = new HashMap<Long, List<OrderItem>>();
    List<ListingResponse> list = new ArrayList<ListingResponse>();
    list.add(response1);
    List<OrderItem> order = new ArrayList<OrderItem>();
    order.add(getOrderDetails().getItems().get(0));
    order.get(0).setSeatId(12L);
    map.put(new Long(12), order);
    Listing pdfListing = new Listing();
    pdfListing.setTicketMedium(1);

    FileInfo fileInfo = new FileInfo();
    fileInfo.setFileName("OldFileInfo");



    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(orderDet);
    ArrayList<TicketSeat> value = new ArrayList<TicketSeat>();
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setFulfillmentArtifactId(2000L);
    value.add(ticketSeat);
    Mockito.when(ticketSeatMgr.findTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(value);
    Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(pdfListing);
    Mockito.when(inventoryMgr.updateListing(Mockito.any(Listing.class))).thenReturn(new Listing());
    TicketSeatDetails tsd = new TicketSeatDetails();
    tsd.setFileInfoId(123L);
    Mockito.when(fulfillmentArtifactMgr.getTicketSeatDetails(Mockito.anyLong())).thenReturn(tsd);
    Mockito.when(fulfillmentArtifactMgr.findByIdForPDF(Mockito.anyLong())).thenReturn(fileInfo);
    Mockito.when(fulfillmentServiceHelper.calculateSaleEndDate(Mockito.any(Listing.class), Mockito.anyListOf(FulfillmentWindow.class)))
        .thenReturn(Calendar.getInstance());
    Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Mockito.anyLong(), Mockito.anyLong())).thenReturn(getFulfillmentWindows());
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);
    relistHelper.cloneFileInfoIds(list, map, request, "AB1234");
  }

  @Test
  public void testCloneViewedMethod2() throws Exception {
    try {
      request = new RelistRequest();
      List<RelistListing> listings = new ArrayList<RelistListing>();
      RelistListing e = new RelistListing();
      e.setOrderId(12L);
      listings.add(e);
      request.setListings(listings);
      OrderDetailsV3DTO orderDet = getOrderDetails();
      orderDet.setOrderStatus("Viewed");
      ListingResponse response1 = new ListingResponse();
      response1.setId("12");

      Map<Long, List<OrderItem>> map = new HashMap<Long, List<OrderItem>>();
      List<ListingResponse> list = new ArrayList<ListingResponse>();
      list.add(response1);
      List<OrderItem> order = new ArrayList<OrderItem>();
      order.add(getOrderDetails().getItems().get(0));
      order.get(0).setSeatId(12L);
      map.put(new Long(12), order);
      Listing pdfListing = new Listing();
      pdfListing.setTicketMedium(2);
      OrderDetailsHelper orderHelper =
          (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
      Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(orderDet);
      ArrayList<TicketSeat> value = new ArrayList<TicketSeat>();
      TicketSeat ticketSeat = new TicketSeat();
      ticketSeat.setFulfillmentArtifactId(2000L);
      value.add(ticketSeat);
      value.add(ticketSeat);
      Mockito.when(ticketSeatMgr.findTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(value);
      Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(pdfListing);
      Mockito.when(inventoryMgr.updateListing(Mockito.any(Listing.class)))
          .thenReturn(new Listing());
      TicketSeatDetails tsd = new TicketSeatDetails();
      tsd.setFileInfoId(null);
      Mockito.when(fulfillmentArtifactMgr.getTicketSeatDetails(Mockito.anyLong())).thenReturn(tsd);
      setBeanProperty(relistHelper, "OrderHelper", orderHelper);
      Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Mockito.anyLong(), Mockito.anyLong())).thenReturn(getFulfillmentWindows());
      relistHelper.cloneFileInfoIds(list, map, request, "AB1234");
    } catch (ListingBusinessException e) {
      Assert.assertEquals("The listing is predelivered but no valid artifact found",
          e.getLocalizedMessage());
    }

  }

  @Test(expectedExceptions = ListingBusinessException.class)
  public void testUpdatePdfPredeliveredListingNullSaleEndDate() throws Exception {
    Mockito.when(fulfillmentServiceHelper.calculateSaleEndDate(Mockito.any(Listing.class), Mockito.anyListOf(FulfillmentWindow.class)))
        .thenReturn(null);
    Listing listing = new Listing();
    relistHelper.updatePdfPredeliveredListing(listing, "AB1234", null);
  }

  @Test
  public void testUpdatePdfPredeliveredListingActivation1() throws Exception {
    Mockito.when(fulfillmentServiceHelper.calculateSaleEndDate(Mockito.any(Listing.class), Mockito.anyListOf(FulfillmentWindow.class)))
        .thenReturn(Calendar.getInstance());
    Mockito.when(sellerHelper.populateSellerDetails(Mockito.any(Listing.class))).thenReturn(true);
    Listing listing = new Listing();
    listing.setSystemStatus("ACTIVE");
    listing.setListPrice(new Money("12"));
    listing.setSplitOption((short) 2);
    listing.setSplitQuantity(1);
    listing.setSellerId(1234L);
    relistHelper.updatePdfPredeliveredListing(listing, "AB1234", null);
  }

  @Test
  public void testUpdatePdfPredeliveredListingActivation2() throws Exception {
    Mockito.when(fulfillmentServiceHelper.calculateSaleEndDate(Mockito.any(Listing.class), Mockito.anyListOf(FulfillmentWindow.class)))
        .thenReturn(Calendar.getInstance());
    Mockito.when(sellerHelper.populateSellerDetails(Mockito.any(Listing.class))).thenReturn(true);
    Listing listing = getListing();
    listing.setSystemStatus("INCOMPLETE");
    relistHelper.updatePdfPredeliveredListing(listing, "AB1234", null);
  }

  @Test
  public void testUpdatePdfPredeliveredListingActivationFailure() throws Exception {
    Mockito.when(fulfillmentServiceHelper.calculateSaleEndDate(Mockito.any(Listing.class), Mockito.anyListOf(FulfillmentWindow.class)))
        .thenReturn(Calendar.getInstance());
    Mockito.when(sellerHelper.populateSellerDetails(Mockito.any(Listing.class))).thenReturn(true);
    Listing listing = getListing();
    listing.setSellerPaymentTypeId(null);
    listing.setSystemStatus("INCOMPLETE");
    relistHelper.updatePdfPredeliveredListing(listing, "AB1234", null);
  }

  @Test
  public void testValidateListingActivation() throws Exception {
    Mockito.when(sellerHelper.populateSellerDetails(Mockito.any(Listing.class))).thenReturn(true);
    Listing listing = getListing();
    relistHelper.validateListingActivation(listing);
  }

  @Test(expectedExceptions = ListingBusinessException.class)
  public void testValidateListingActivationFailure1() throws Exception {
    Listing listing = getListing();
    listing.setSellerContactId(null);
    relistHelper.validateListingActivation(listing);
  }

  @Test(expectedExceptions = ListingBusinessException.class)
  public void testValidateListingActivationFailure2() throws Exception {
    Listing listing = getListing();
    listing.setTaxpayerStatus(TaxpayerStatusEnum.TINRequired.getStatus());
    relistHelper.validateListingActivation(listing);
  }

  @Test(expectedExceptions = ListingBusinessException.class)
  public void testValidateListingActivationFailure3() throws Exception {
    Listing listing = getListing();
    listing.setListPrice(new Money("0"));
    relistHelper.validateListingActivation(listing);
  }

  @Test(expectedExceptions = ListingBusinessException.class)
  public void testValidateListingActivationFailure4() throws Exception {
    Listing listing = getListing();
    listing.setSplitOption(null);
    relistHelper.validateListingActivation(listing);
  }

  @Test(expectedExceptions = ListingBusinessException.class)
  public void testValidateListingActivationFailure5() throws Exception {
    Listing listing = getListing();
    listing.setFraudCheckStatusId(100L);
    Mockito.when(userHelper.isSellerPaymentContactIdPopulated(Mockito.anyString(),
        Mockito.anyLong(), Mockito.anyListOf(CustomerPaymentInstrumentDetailsV2.class)))
        .thenReturn(false);
    relistHelper.validateListingActivation(listing);
  }

  @Test(expectedExceptions = ListingBusinessException.class)
  public void testValidateListingActivationFailure6() throws Exception {
    Listing listing = getListing();
    listing.setFraudCheckStatusId(100L);
    listing.setSellerCCId(null);
    Mockito.when(userHelper.isSellerPaymentContactIdPopulated(Mockito.anyString(),
        Mockito.anyLong(), Mockito.anyListOf(CustomerPaymentInstrumentDetailsV2.class)))
        .thenReturn(true);
    relistHelper.validateListingActivation(listing);
  }

  @Test(expectedExceptions = ListingBusinessException.class)
  public void testValidateListingActivationFailure7() throws Exception {
    Listing listing = getListing();
    listing.setFraudCheckStatusId(100L);
    Mockito.when(userHelper.isSellerPaymentContactIdPopulated(Mockito.anyString(),
        Mockito.anyLong(), Mockito.anyListOf(CustomerPaymentInstrumentDetailsV2.class)))
        .thenReturn(true);
    Mockito.when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(false);
    relistHelper.validateListingActivation(listing);
  }

  @Test(expectedExceptions = ListingBusinessException.class)
  public void testValidateListingActivationFailure8() throws Exception {
    Listing listing = getListing();
    listing.setFraudCheckStatusId(100L);
    Mockito.when(userHelper.isSellerPaymentContactIdPopulated(Mockito.anyString(),
        Mockito.anyLong(), Mockito.anyListOf(CustomerPaymentInstrumentDetailsV2.class)))
        .thenReturn(true);
    Mockito.when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(true);
    relistHelper.validateListingActivation(listing);
  }

  /**
   * Relist listing with order details
   * 
   * @result orderDetails validated
   */
 @Test
  public void validateListingWithViewedOrderStatus() throws Exception {
    request = new RelistRequest();
    OrderDetailsV3DTO orderDetail = getOrderDetails();
    orderDetail.setOrderStatus("Viewed");
    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(orderDetail);
    Mockito.when(ticketSeatMgr.findActiveTicketSeatsByOriginalSeatIds(Mockito.anyList())).thenReturn(null);
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);
    Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
    Map<Long, List<OrderItem>> orderDetails =
        relistHelper.validateListingWithOrderDetails(getOrders(), orderDetailsMaps, Locale.US);
    Assert.assertNotNull(orderDetails.get(1000L));

  }
 
 /**
  * Relist twice error
  * 
  * @result orderDetails validated
  */
@Test
 public void validateRelistTwiceError() throws Exception {
   request = new RelistRequest();
   List<Long> seats=new ArrayList<Long>();
   seats.add(1000L);
   OrderDetailsV3DTO orderDetail = getOrderDetails();
   orderDetail.setOrderStatus("Viewed");
   Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
   OrderDetailsHelper orderHelper =
       (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
   Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(orderDetail);
   Mockito.when(ticketSeatMgr.findActiveTicketSeatsByOriginalSeatIds(Mockito.anyList())).thenReturn(seats);
   setBeanProperty(relistHelper, "OrderHelper", orderHelper);
   try
   {
   Map<Long, List<OrderItem>> orderDetails =
       relistHelper.validateListingWithOrderDetails(getOrders(), orderDetailsMaps, Locale.US);
   }
   catch(ListingBusinessException ex)
   {
	   Assert.assertEquals(ex.getLocalizedMessage(), "The order has one or more items that were already relisted");
   }

 }
 

  /**
   * Relist listing with order details
   * 
   * @result orderDetails validated
   */
  @Test
  public void validateListingWithDelivereddOrderStatus() throws Exception {
    request = new RelistRequest();
    List<RelistListing> listings = new ArrayList<RelistListing>();
    RelistListing relist1 = new RelistListing();
    relist1.setOrderId(1000L);
    relist1.setItems(null);
    listings.add(relist1);
    request.setListings(listings);
    OrderDetailsV3DTO orderDetail = getOrderDetails();
    orderDetail.setOrderStatus("Delivered");
    Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(orderDetail);
    Mockito.when(ticketSeatMgr.findActiveTicketSeatsByOriginalSeatIds(Mockito.anyList())).thenReturn(null);
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);

    Map<Long, List<OrderItem>> orderDetails =
        relistHelper.validateListingWithOrderDetails(getOrders(), orderDetailsMaps, Locale.US);
    Assert.assertNotNull(orderDetails.get(1000L));

  }

  /**
   * Relist listing with purchased order
   * 
   * @throws ListingBuisnessException
   */
  @Test
  public void validateListingWithPurchasedOrder() throws Exception {
    request = new RelistRequest();
    OrderDetailsV3DTO orderDetails = getOrderDetails();
    orderDetails.setOrderStatus("Purchased");
    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(orderDetails);
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);
    Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
    try {
      relistHelper.validateListingWithOrderDetails(getOrders(), orderDetailsMaps, Locale.US);
    } catch (ListingBusinessException ex) {
      Assert.assertTrue(ex.getListingError().getMessage()
          .equals("The order is in either purchased, suboffered or cancelled status"));
    }


  }

  /**
   * Relist listing with Subsoffered order
   * 
   * @throws ListingBuisnessException
   */
  @Test
  public void validateListingWithSubsofferedOrder() throws Exception {
    request = new RelistRequest();
    OrderDetailsV3DTO orderDetails = getOrderDetails();
    orderDetails.setOrderStatus("Subsoffered");
    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(orderDetails);
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);
    Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
    try {
      relistHelper.validateListingWithOrderDetails(getOrders(), orderDetailsMaps, Locale.US);
    } catch (ListingBusinessException ex) {
      Assert.assertTrue(ex.getListingError().getMessage()
          .equals("The order is in either purchased, suboffered or cancelled status"));
    }


  }


  /**
   * Relist listing with Cancelled order
   * 
   * @throws ListingBuisnessException
   */
  @Test
  public void validateListingWithCancelledOrder() throws Exception {
    request = new RelistRequest();
    OrderDetailsV3DTO orderDetails = getOrderDetails();
    orderDetails.setOrderStatus("Cancelled");
    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(orderDetails);
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);
    Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
    try {
      relistHelper.validateListingWithOrderDetails(getOrders(), orderDetailsMaps, Locale.US);
    } catch (ListingBusinessException ex) {
      Assert.assertTrue(ex.getListingError().getMessage()
          .equals("The order is in either purchased, suboffered or cancelled status"));
    }

  }

  /**
   * Validate listings with null requested items
   * 
   * @result throws a ListingBuisnessException
   */

  @Test
  public void validateListingNullRequestedItems() throws Exception {
    request = new RelistRequest();
    RelistRequest relistRequest = getOrders();
    relistRequest.getListings().get(0).setItems(null);

    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(getOrderDetails());
    Mockito.when(ticketSeatMgr.findActiveTicketSeatsByOriginalSeatIds(Mockito.anyList())).thenReturn(null);
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);
    Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
    Map<Long, List<OrderItem>> orderDetails =
        relistHelper.validateListingWithOrderDetails(relistRequest, orderDetailsMaps, Locale.US);
    Assert.assertNotNull(orderDetails.get(1000L));

  }

  /**
   * Relist with two listings with items not in order details
   * 
   * @result throws a ListingBuisnessException
   */

  @Test
  public void validateListingInvalidItems() throws Exception {
    request = new RelistRequest();
    RelistRequest relistRequest = getOrders();
    relistRequest.setListings(new ArrayList<RelistListing>());
    RelistListing listing1 = new RelistListing();
    listing1.setOrderId(1000L);
    listing1.setPricePerItem(new BigDecimal(20.30));
    listing1.setItems(new ArrayList<RelistItem>());
    RelistItem item = new RelistItem();
    item.setItemId("2");
    RelistItem item2 = new RelistItem();
    item.setItemId("3");
    RelistItem item3 = new RelistItem();
    item.setItemId("4");
    listing1.getItems().add(item);
    listing1.getItems().add(item2);
    listing1.getItems().add(item3);
    relistRequest.getListings().add(listing1);
    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(getOrderDetails());
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);
    Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
    try {
      relistHelper.validateListingWithOrderDetails(relistRequest, orderDetailsMaps, Locale.US);
    } catch (ListingBusinessException ex) {
      Assert.assertEquals(ex.getLocalizedMessage(),
          "one or more of the requested items is not found in the order: {1000}");
    }
  }

  
  /**
   * Validate listings with different event id
   * 
   * @result throws a ListingBuisnessException
   */

  @Test
  public void validateGroupingEventId() throws Exception {
    request = new RelistRequest();

    OrderDetailsV3DTO order = getOrderDetails();
    OrderItem item = new OrderItem();
    item.setId(1000L);
    item.setSeatId(1000L);
    item.setRow("A");
    item.setEventId(2000L);
    item.setTicketMediumId(1L);
    order.getItems().get(0).setEventId(100L);
    order.getItems().add(item);

    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(order);
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);
    Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
    try {
      relistHelper.validateListingWithOrderDetails(getOrders(), orderDetailsMaps, Locale.US);
    } catch (ListingBusinessException ex) {
      Assert.assertTrue(ex.getListingError().getMessage()
          .equals("All items in the listing should have the same EventId"));
    }
  }
  
  
  /**
   * Validate listings with different ticket medium
   * 
   * @result throws a ListingBuisnessException
   */

  @Test
  public void validateGroupingTicketMedium() throws Exception {
    request = new RelistRequest();

    OrderDetailsV3DTO order = getOrderDetails();
    OrderItem item = new OrderItem();
    item.setId(1000L);
    item.setSeatId(1000L);
    item.setRow("A");
    item.setTicketMediumId(1L);
    order.getItems().add(item);

    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(order);
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);
    Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
    try {
      relistHelper.validateListingWithOrderDetails(getOrders(), orderDetailsMaps, Locale.US);
    } catch (ListingBusinessException ex) {
      Assert.assertTrue(ex.getListingError().getMessage()
          .equals("All items in the listing should have the same TicketMedium"));
    }
  }

  /**
   * Validate listings with different sections
   * 
   * @result throws a ListingBuisnessException
   */
  @Test
  public void validateGroupingSection() throws Exception {
    request = new RelistRequest();

    OrderDetailsV3DTO order = getOrderDetails();
    OrderItem item = new OrderItem();
    item.setId(1000L);
    item.setSeatId(1000L);
    item.setRow("A");
    item.setSection("Test Section1");
    item.setFulfillmentMethodId(2L);
    item.setDeliveryMethodId(2L);
    item.setDeliveryTypeId(2L);
    item.setDeliveryOptionId(2L);
    item.setTicketMediumId(2L);
    order.getItems().add(item);
    Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
    OrderDetailsHelper orderHelper =
        (OrderDetailsHelper) mockClass(OrderDetailsHelper.class, null, null);
    Mockito.when(orderHelper.getOrderDetails(Mockito.anyLong())).thenReturn(order);
    setBeanProperty(relistHelper, "OrderHelper", orderHelper);
    try {
      relistHelper.validateListingWithOrderDetails(getOrders(), orderDetailsMaps, Locale.US);
    } catch (ListingBusinessException ex) {
      Assert.assertTrue(ex.getListingError().getMessage()
          .equals("All items in the listing should have the same Section"));
    }
  }

  private OrderDetailsV3DTO getOrderDetails() {
    OrderDetailsV3DTO order = new OrderDetailsV3DTO();
    order.setId((Long) 1000L);
    order.setOrderSource("SellItNow(Bot-flow)");
    order.setItems(null);
    List<OrderItem> listOfItems = new ArrayList<OrderItem>();
    OrderItem item = new OrderItem();
    item.setId(1000L);
    item.setSeatId(1000L);
    item.setRow("A");
    item.setDeliveryMethodId(2L);
    item.setTicketMediumId(2L);
    item.setDeliveryTypeId(2L);
    item.setDeliveryOptionId(2L);
    item.setFulfillmentMethodId(2L);
    item.setSection("Test Section");
    listOfItems.add(item);
    order.setItems(listOfItems);
    return order;

  }
  private OrderDetailsV3DTO getOrderDetailsTransferred() {
	    OrderDetailsV3DTO order = new OrderDetailsV3DTO();
	    order.setId((Long) 1000L);
	    order.setItems(null);
	    List<OrderItem> listOfItems = new ArrayList<OrderItem>();
	    OrderItem item = new OrderItem();
	    item.setId(1000L);
	    item.setSeatId(1000L);
	    item.setRow("A");
	    item.setDeliveryMethodId(2L);
	    item.setTicketMediumId(2L);
	    item.setDeliveryTypeId(2L);
	    item.setDeliveryOptionId(2L);
	    item.setFulfillmentMethodId(2L);
	    item.setSection("Test Section");
	    item.setTransferredInd(1);
	    listOfItems.add(item);
	    order.setItems(listOfItems);
	    return order;

	  }
  private RelistRequest getOrders() {
    request = getEmptyListings();
    List<RelistListing> listOfListings = new ArrayList<RelistListing>();
    RelistListing listing = new RelistListing();
    listing.setOrderId((long) 1000);
    listing.setPricePerItem(new BigDecimal(50.20));

    listOfListings.add(listing);
    request.setListings(listOfListings);
    return request;
  }
  
  private RelistRequest getOrdersTransfer() {
	    request = getEmptyListings();
	    List<RelistListing> listOfListings = new ArrayList<RelistListing>();
	    RelistListing listing = new RelistListing();
	    listing.setOrderId((long) 1000);
	    listing.setPricePerItem(new BigDecimal(50.20));
	    listing.setToCustomerGUID("GUID1");
	    listing.setToEmailId("api_us_sell_indy03@testmail.com");
	    listOfListings.add(listing);
	    request.setListings(listOfListings);
	    return request;
	  }

  /**
   * Validate listings with emptyitems
   * 
   * @result throws a ListingBusinessException
   */

  @Test
  public void validateRelistListingEmptyItems() throws Exception {
    request = new RelistRequest();
    Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
    try {
      relistHelper.validateListingWithOrderDetails(getEmptyItemsListings(), orderDetailsMaps, Locale.US);
    } catch (ListingBusinessException ex) {
      Assert.assertTrue(ex.getListingError().getCode() == ErrorCode.INVALID_ITEM);
    }
  }

  private RelistRequest getEmptyItemsListings() {
    request = getEmptyListings();
    RelistListing listing = new RelistListing();
    listing.setOrderId((long) 1000);
    listing.setPricePerItem(new BigDecimal(50.20));
    listing.setItems(new ArrayList<RelistItem>());
    request.getListings().add(listing);
    return request;
  }

  /* *//**
        * create listing requests with relist request and list of order items
        * 
        * @result listings created
        */
  @Test
  public void testCreateListingRequests() throws Exception {
    request = new RelistRequest();
    Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
    List<ListingRequest> list =
        relistHelper.createListingRequests(getRelistRequest(), getListOfOrderItems(), orderDetailsMaps);
    Assert.assertNotNull(list.get(0));
  }
  
  @Test
  public void testCreateListingRequestsForNonPreDelivered() throws Exception {
    request = new RelistRequest();
    Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(new Listing());
    Map<Long,List<OrderItem>> orderMap=getListOfOrderItems();
    orderMap.get(1000L).get(0).setBarcodeText(null);
    orderMap.get(1000L).get(0).setListingId(1000L);
    
    Map<Long, OrderDetailsV3DTO> orderDetailsMaps=new HashMap<Long, OrderDetailsV3DTO>();
    OrderDetailsV3DTO orderDetails = new OrderDetailsV3DTO();
    orderDetails.setOrderSource("SellItNow(Bot-flow)");
    orderDetailsMaps.put(1000L, orderDetails);
    
    List<ListingRequest> list =
        relistHelper.createListingRequests(getRelistRequest(), orderMap, orderDetailsMaps);
    Assert.assertNotNull(list.get(0));
  }

  @Test
  public void testCreateListingRequestsBarcode() throws Exception {
    request = new RelistRequest();
    Map<Long, List<OrderItem>> orderItems = getListOfOrderItems();
    Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
    orderItems.get(1000L).get(0).setTicketMediumId(3L);
    List<ListingRequest> list = relistHelper.createListingRequests(getRelistRequest(), orderItems, orderDetailsMaps);
    Assert.assertNotNull(list.get(0));
  }

  @Test
  public void testCreateListingRequestsSecureBarcode() throws Exception {
    request = new RelistRequest();
    Map<Long, List<OrderItem>> orderItems = getListOfOrderItems();
    Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
    orderItems.get(1000L).get(0).setTicketMediumId(3L);
    orderItems.get(1000L).get(0).setBarcodeText(null);
    List<ListingRequest> list = relistHelper.createListingRequests(getRelistRequest(), orderItems,orderDetailsMaps);
    Assert.assertNotNull(list.get(0));
  }
  @Test
  public void testCreateListingRequestsBarcodeNull() throws Exception {
    request = new RelistRequest();
    Map<Long, List<OrderItem>> orderItems = getListOfOrderItems();
    Map<Long, OrderDetailsV3DTO> orderDetailsMaps = getOrderDetailsV3DTO(1000L);
    orderItems.get(1000L).get(0).setTicketMediumId(3L);
    orderItems.get(1000L).get(0).setBarcodeText(null);
    orderItems.get(1000L).get(0).setSecureRenderBarcode(null);
    List<ListingRequest> list = relistHelper.createListingRequests(getRelistRequest(), orderItems,orderDetailsMaps);
    Assert.assertNotNull(list.get(0));
  }
  
  @Test
  public void testCreateListingRequestsFulfilmentMethods() throws Exception {
    request = new RelistRequest();
    Map<Long, List<OrderItem>> orderItemsMap = getListOfOrderItems();
    List<OrderItem> orderItems = orderItemsMap.get(1000L);
    orderItems.get(0).setFulfillmentMethodId(1L);
    
    Map<Long, OrderDetailsV3DTO> orderDetailsMap = new HashMap<Long, OrderDetailsV3DTO>();
    OrderDetailsV3DTO orderDetails = new OrderDetailsV3DTO();
    orderDetails.setOrderSource("SellItNow(Bot-flow)");
    orderDetailsMap.put(1000L, orderDetails);
    
    List<ListingRequest> list =
        relistHelper.createListingRequests(getRelistRequest(), orderItemsMap,getOrderDetailsV3DTO(1000L));
    Assert.assertNotNull(list.get(0));

    orderItems.get(0).setFulfillmentMethodId(2L);
    list = relistHelper.createListingRequests(getRelistRequest(), orderItemsMap,orderDetailsMap);
    Assert.assertEquals(list.get(0).getDeliveryOption(), DeliveryOption.BARCODE);

    orderItems.get(0).setFulfillmentMethodId(3L);
    orderDetails.setOrderSource("Unified");
    orderDetailsMap.put(1000L, orderDetails);
    list = relistHelper.createListingRequests(getRelistRequest(), orderItemsMap, orderDetailsMap);
    Assert.assertEquals(list.get(0).getDeliveryOption(), DeliveryOption.BARCODE);

    orderItems.get(0).setFulfillmentMethodId(4L);
    list = relistHelper.createListingRequests(getRelistRequest(), orderItemsMap,getOrderDetailsV3DTO(1000L));
    Assert.assertEquals(list.get(0).getDeliveryOption(), DeliveryOption.PDF);

    orderItems.get(0).setFulfillmentMethodId(5L);
    list = relistHelper.createListingRequests(getRelistRequest(), orderItemsMap, orderDetailsMap);
    Assert.assertEquals(list.get(0).getDeliveryOption(), DeliveryOption.PDF);

    orderItems.get(0).setFulfillmentMethodId(7L);
    list = relistHelper.createListingRequests(getRelistRequest(), orderItemsMap, orderDetailsMap);
    Assert.assertEquals(list.get(0).getDeliveryOption(), DeliveryOption.LMS);

    orderItems.get(0).setFulfillmentMethodId(9L);
    list = relistHelper.createListingRequests(getRelistRequest(), orderItemsMap, orderDetailsMap);
    Assert.assertEquals(list.get(0).getDeliveryOption(), DeliveryOption.LMS);

    orderItems.get(0).setFulfillmentMethodId(10L);
    list = relistHelper.createListingRequests(getRelistRequest(), orderItemsMap, orderDetailsMap);
    Assert.assertEquals(list.get(0).getDeliveryOption(), DeliveryOption.UPS);

    orderItems.get(0).setFulfillmentMethodId(11L);
    list = relistHelper.createListingRequests(getRelistRequest(), orderItemsMap, orderDetailsMap);
    Assert.assertEquals(list.get(0).getDeliveryOption(), DeliveryOption.SHIPPING);

    orderItems.get(0).setFulfillmentMethodId(12L);
    list = relistHelper.createListingRequests(getRelistRequest(), orderItemsMap, orderDetailsMap);
    Assert.assertEquals(list.get(0).getDeliveryOption(), DeliveryOption.SHIPPING);
    
    orderItems.get(0).setFulfillmentMethodId(13L);
    list = relistHelper.createListingRequests(getRelistRequest(), orderItemsMap, orderDetailsMap);
    Assert.assertEquals(list.get(0).getDeliveryOption(), DeliveryOption.FLASHSEAT);
    
    orderItems.get(0).setFulfillmentMethodId(14L);
    list = relistHelper.createListingRequests(getRelistRequest(), orderItemsMap, orderDetailsMap);
    Assert.assertEquals(list.get(0).getDeliveryOption(), DeliveryOption.FLASHSEAT);
    
    orderItems.get(0).setFulfillmentMethodId(19L);
    list = relistHelper.createListingRequests(getRelistRequest(), orderItemsMap, orderDetailsMap);
    Assert.assertEquals(list.get(0).getDeliveryOption(), DeliveryOption.FLASHSEAT);
    
    orderItems.get(0).setFulfillmentMethodId(8L);
    list = relistHelper.createListingRequests(getRelistRequest(), orderItemsMap, orderDetailsMap);
    Assert.assertEquals(list.get(0).getDeliveryOption(), DeliveryOption.WILLCALL);
    
    orderItems.get(0).setFulfillmentMethodId(17L);
    list = relistHelper.createListingRequests(getRelistRequest(), orderItemsMap, orderDetailsMap);
    Assert.assertEquals(list.get(0).getDeliveryOption(), DeliveryOption.LOCALDELIVERY);
    
    orderItems.get(0).setFulfillmentMethodId(18L);
    list = relistHelper.createListingRequests(getRelistRequest(), orderItemsMap, orderDetailsMap);
    Assert.assertEquals(list.get(0).getDeliveryOption(), DeliveryOption.MOBILETRANSFER);
    
    orderItems.get(0).setFulfillmentMethodId(20L);
    list = relistHelper.createListingRequests(getRelistRequest(), orderItemsMap, orderDetailsMap);
    Assert.assertEquals(list.get(0).getDeliveryOption(), DeliveryOption.MOBILE);
    
    orderItems.get(0).setFulfillmentMethodId(21L);
    orderDetails.setOrderSource("SellItNow(Bot-flow)");
    orderDetailsMap.put(1000L, orderDetails);
    list = relistHelper.createListingRequests(getRelistRequest(), orderItemsMap,orderDetailsMap);
    Assert.assertEquals(list.get(0).getDeliveryOption(), DeliveryOption.MOBILE);
    
    
  }


  @Test
  public void testCreateListingRequestsForPPOnly() throws Exception {
    request = new RelistRequest();

    Map<Long, List<OrderItem>> mapOfOrders = getListOfOrderItems();
    List<OrderItem> orderItems = mapOfOrders.get(1000L);
    orderItems.get(0).setType("Parking_Pass");
    List<ListingRequest> list = relistHelper.createListingRequests(getRelistRequest(), mapOfOrders, getOrderDetailsV3DTO(1000L));
    ListingRequest request = list.get(0);
    Assert.assertNotNull(request);
    Assert.assertTrue(request.getProducts().get(0).getProductType() == ProductType.TICKET);
  }

  @Test
  public void testCreateListingRequestsForInvalidItemType() throws Exception {
    request = new RelistRequest();

    Map<Long, List<OrderItem>> mapOfOrders = getListOfOrderItems();
    List<OrderItem> orderItems = mapOfOrders.get(1000L);
    orderItems.get(0).setType("Parking Pass 123");
    List<ListingRequest> list = relistHelper.createListingRequests(getRelistRequest(), mapOfOrders, getOrderDetailsV3DTO(1000L));
    ListingRequest request = list.get(0);
    Assert.assertNotNull(request);
    Assert.assertTrue(request.getProducts().get(0).getProductType() == ProductType.TICKET);
  }

  private Map<Long, List<OrderItem>> getListOfOrderItems() {
    Map<Long, List<OrderItem>> mapOfOrders = new HashMap<Long, List<OrderItem>>();

    List<OrderItem> listOfOrders = new ArrayList<OrderItem>();
    OrderItem item = new OrderItem();
    Money money = new Money();
    money.setAmount(new BigDecimal(50.20));
    money.setCurrency("USD");
    item.setPricePerTicket(money);
    item.setEventId(12063L);
    item.setSection("Test");
    item.setTicketInhandDate(DateUtil.getTodayCalUTC().getTime());
    item.setRow("A");
    item.setSeat("12");
    item.setType(null);
    item.setBarcodeText("456f-345jkl");
    item.setSecureRenderBarcode("3553-U7373263");
    item.setGeneralAdmission(false);
    List<TicketTrait> traits = new ArrayList<TicketTrait>();
    TicketTrait trait = new TicketTrait();
    trait.setId(200L);
    trait.setName("Test123");
    trait.setType(1L);
    traits.add(trait);
    item.setTicketTraits(traits);
    listOfOrders.add(item);
    mapOfOrders.put(1000L, listOfOrders);
    return mapOfOrders;
  }
  
  private Map<Long, OrderDetailsV3DTO> getOrderDetailsV3DTO(Long orderId){
	  Map<Long, OrderDetailsV3DTO> orderDetailsMaps = new HashMap<Long, OrderDetailsV3DTO>();
	  OrderDetailsV3DTO order = new OrderDetailsV3DTO();
	    order.setId((Long) 1000L);
	    order.setOrderSource("SellItNow(Bot-flow)");
	    order.setItems(null);
	    List<OrderItem> listOfItems = new ArrayList<OrderItem>();
	    OrderItem item = new OrderItem();
	    item.setId(1000L);
	    item.setSeatId(1000L);
	    item.setRow("A");
	    item.setDeliveryMethodId(2L);
	    item.setTicketMediumId(2L);
	    item.setDeliveryTypeId(2L);
	    item.setDeliveryOptionId(2L);
	    item.setFulfillmentMethodId(2L);
	    item.setSection("Test Section");
//	    item.setCostPerTicket(new Money("100", "USD"));
        item.setTotalCost(new Money("200", "USD"));
	    listOfItems.add(item);

	    order.setItems(listOfItems);
		  orderDetailsMaps.put(orderId, order);

	  
	  return orderDetailsMaps;
	  
  }

  private RelistRequest getRelistRequest() {
    RelistRequest request = new RelistRequest();
    List<RelistListing> listOfListings = new ArrayList<RelistListing>();
    RelistListing listing = new RelistListing();
    listing.setOrderId(1000L);
    listOfListings.add(listing);
    request.setListings(listOfListings);
    return request;
  }


  /**
   * get order helper
   * 
   * @result orderhelper instance
   */
  @Test
  public void testGetOrderHelper() throws Exception {
    request = new RelistRequest();
    OrderDetailsHelper orderDetails = relistHelper.getOrderHelper();
    Assert.assertNotNull(orderDetails);
  }

  /**
   * set order helper
   * 
   * @result none
   */
  @Test
  public void testSetOrderHelper() throws Exception {
    request = new RelistRequest();
    OrderDetailsHelper helper = new OrderDetailsHelper();
    relistHelper.setOrderHelper(helper);
  }

  private Listing getListing() {
    Listing listing = new Listing();
    listing.setSystemStatus("ACTIVE");
    listing.setListPrice(new Money("12"));
    listing.setSplitOption((short) 2);
    listing.setSplitQuantity(1);
    listing.setSellerId(1234L);
    listing.setSellerGuid("AB1234");
    listing.setSellerPaymentTypeId(1L);
    listing.setSellerContactId(1234L);
    listing.setSellerCCId(12345L);
    return listing;
  }

  public List<ListingResponse> getResponses()
  {
	  List<ListingResponse> responses=new ArrayList<ListingResponse>();
	  ListingResponse resp=new ListingResponse();
	  resp.setId("1000");
	  resp.setStatus(ListingStatus.ACTIVE);
	  responses.add(resp);
	  return responses;
	 
  }
  
  public RelistRequest getRelistRequest1()
  {
	  RelistRequest relistRequest=new RelistRequest();
	  RelistListing listing=new RelistListing();
	  listing.setOrderId(1000L);
	  listing.addItem("100000");
	  List<RelistListing> listings=new ArrayList<RelistListing>();
	  listings.add(listing);
	  relistRequest.setListings(listings);
	  return relistRequest;
	 
  }
  
  public RelistRequest getRelistRequest2()
  {
	  RelistRequest relistRequest=new RelistRequest();
	  RelistListing listing=new RelistListing();
	  listing.setOrderId(1000L);
	  listing.addItem("100000");
	  listing.addItem("100001");
	  List<RelistListing> listings=new ArrayList<RelistListing>();
	  listings.add(listing);
	  relistRequest.setListings(listings);
	  return relistRequest;
	 
  }
  
  public Map<Long,List<OrderItem>> getOrderItems()
  {
	  Map<Long,List<OrderItem>> map=new HashMap<Long,List<OrderItem>>();
	  List<OrderItem> list=new ArrayList<OrderItem>();
	  OrderItem item=new OrderItem();
	  item.setId(100L);
	  item.setSection("General Admission");
	  item.setRow("A");
	  item.setSeat("100000");
	  item.setSeatId(100000L);
	  list.add(item);
	  map.put(1000L, list);
	  return map;
  }
  
  public Map<Long,List<OrderItem>> getOrderItemsNonGa()
  {
	  Map<Long,List<OrderItem>> map=new HashMap<Long,List<OrderItem>>();
	  List<OrderItem> list=new ArrayList<OrderItem>();
	  OrderItem item=new OrderItem();
	  item.setId(100L);
	  item.setSection("TestOne");
	  item.setRow("A");
	  item.setSeat("A");
	  item.setSeatId(100000L);
	  list.add(item);
	  map.put(1000L, list);
	  return map;
  }
  
  public Map<Long,List<OrderItem>> getOrderItemsNonGa1()
  {
	  Map<Long,List<OrderItem>> map=new HashMap<Long,List<OrderItem>>();
	  List<OrderItem> list=new ArrayList<OrderItem>();
	  OrderItem item=new OrderItem();
	  item.setId(100L);
	  item.setSection("TestOne");
	  item.setRow("A");
	  item.setSeatId(100000L);
	  OrderItem item1=new OrderItem();
	  item1.setId(100L);
	  item1.setSection("TestOne");
	  item1.setRow("A");
	  item1.setSeatId(100001L);
	  list.add(item);
	  list.add(item1);
	  map.put(1000L, list);
	  return map;
  }
  
  public List<TicketSeat> listOfSeatsGA()
  {
	  List<TicketSeat> list=new ArrayList<TicketSeat>();
	  TicketSeat ts=new TicketSeat();
	  ts.setSection("General Admission");
	  ts.setRow("A");
	  ts.setSeatNumber("A");
	  list.add(ts);
	  return list;
  }
  public List<TicketSeat> listOfSeatsnonGA()
  {
	  List<TicketSeat> list=new ArrayList<TicketSeat>();
	  TicketSeat ts=new TicketSeat();
	  ts.setSection("TestOne");
	  ts.setRow("A");
	  ts.setSeatNumber("A");
	  list.add(ts);
	  return list;
  }
  public List<TicketSeat> listOfSeatsnonGA1()
  {
	  List<TicketSeat> list=new ArrayList<TicketSeat>();
	  TicketSeat ts=new TicketSeat();
	  ts.setSection("TestOne");
	  ts.setRow("A");
	  list.add(ts);
	  TicketSeat ts1=new TicketSeat();
	  ts1.setSection("TestOne");
	  ts1.setRow("A");
	  list.add(ts1);
	  return list;
  }
  
  @Test
  public void testAddOriginalTicketSeatIdsGA() throws Exception {
		Mockito.when(ticketSeatMgr.findAllTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(listOfSeatsGA());
		Mockito.doNothing().when(ticketSeatMgr).updateTicketSeat(Mockito.any(TicketSeat.class));
	    relistHelper.addOriginalTicketSeatIds(getResponses(), getOrderItems(), getRelistRequest1());
	  }
  
  @Test
  public void testAddOriginalTicketSeatIdsGANullItems() throws Exception {
	    RelistRequest request=getRelistRequest1();
	    request.getListings().get(0).setItems(null);
		Mockito.when(ticketSeatMgr.findAllTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(listOfSeatsGA());
		Mockito.doNothing().when(ticketSeatMgr).updateTicketSeat(Mockito.any(TicketSeat.class));
	    relistHelper.addOriginalTicketSeatIds(getResponses(), getOrderItems(), request);
	  }
  
  @Test
  public void testAddOriginalTicketSeatIdsnonGA() throws Exception {
		Mockito.when(ticketSeatMgr.findAllTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(listOfSeatsnonGA());
		Mockito.doNothing().when(ticketSeatMgr).updateTicketSeat(Mockito.any(TicketSeat.class));
	    relistHelper.addOriginalTicketSeatIds(getResponses(), getOrderItemsNonGa(), getRelistRequest1());
	  }
  
  @Test
  public void testAddOriginalTicketSeatIdsnoSeats() throws Exception {
		Mockito.when(ticketSeatMgr.findAllTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(listOfSeatsnonGA1());
		Mockito.doNothing().when(ticketSeatMgr).updateTicketSeat(Mockito.any(TicketSeat.class));
	    relistHelper.addOriginalTicketSeatIds(getResponses(), getOrderItemsNonGa1(), getRelistRequest2());
	  }

}