package com.stubhub.domain.inventory.listings.v2.newflow.orchestrator;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.entity.SplitOption;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.FulfillmentInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.SellerInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.UpdateListingEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.handler.BusinessFlowHandler;
import com.stubhub.domain.inventory.listings.v2.newflow.handler.UpdateListingHandler;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.EventHelper;
import com.stubhub.domain.inventory.listings.v2.newflow.task.TestUtil;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.DTO.TicketTrait;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;

public class UpdateListingFlowRouterTest {
  @Mock
  private InventoryMgr inventoryMgr;

  @Mock
  private ListingSeatTraitMgr listingSeatTraitMgr;

  @Mock
  private BeanFactory beanFactory;

  @Mock
  private MasterStubhubPropertiesWrapper masterStubhubProperties;

  @Mock
  TicketSeatMgr ticketSeatMgr;

  @Mock
  EventHelper eventHelper;

  @InjectMocks
  private UpdateListingFlowRouter updateListingFlowRouter;

  @BeforeMethod
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGetBusinessFlowHandlerFailOnNotSeller() {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setListingId(1L);
    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(12L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = TestUtil.getDBListing();
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    try {
      updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
    } catch (ListingException e) {
      Assert.assertNotNull(e);
      Assert.assertEquals(e.getErrorCodeEnum(), ErrorCodeEnum.listingActionNotallowed);
      Assert.assertEquals(e.getType(), ErrorType.BUSINESSERROR);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerNewFlowNotEnabled() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setContactId(1L);
    listingRequest.setPaymentType(3L);
    listingRequest.setCcId("1");
    listingRequest.setDeliveryOption(DeliveryOption.PDF);
    listingRequest.setSaleEndDate("1");
    listingRequest.setInhandDate("2020-10-10");
    listingRequest.setLmsExtension(true);
    listingRequest.setLmsApprovalStatus(1);
    listingRequest.setQuantity(2);
    listingRequest.setSection("a");
    List<Product> products = new ArrayList<>();
    Product product = new Product();
    product.setOperation(Operation.UPDATE);
    product.setFulfillmentArtifact("artifact");
    products.add(product);
    listingRequest.setProducts(products);
    listingRequest.setSplitQuantity(2);
    listingRequest.setSplitOption(SplitOption.NONE);
    listingRequest.setHideSeats(true);
    List<TicketTrait> ticketTraits = new ArrayList<>();
    TicketTrait ticketTrait = new TicketTrait();
    ticketTraits.add(ticketTrait);
    listingRequest.setTicketTraits(ticketTraits);
    listingRequest.setComments("comments");
    listingRequest.setPricePerProduct(new Money("12"));
    listingRequest.setStatus(ListingStatus.INCOMPLETE);
    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setSellerContactId(2L);
    listing.setSellerPaymentTypeId(4L);
    listing.setDeliveryOption(2);
    listing.setLmsApprovalStatus(2);
    listing.setQuantityRemain(1);
    listing.setSection("b");
    listing.setTicketMedium(3);
    listing.setSellerId(1L);
    listing.setEventId(123l);
    List<ListingSeatTrait> seatTraits = new ArrayList<>();
    listing.setSplitOption((short) 1);
    listing.setHideSeatInfoInd(false);
    listing.setListPrice(new Money("17"));
    listing.setSystemStatus("active");
    listingDTO.setDbListing(listing);
    listingDTO.setFulfillmentInfo(new FulfillmentInfo());

    TicketSeat ts = new TicketSeat();
    ts.setExternalSeatId("123456");
    ts.setRow("B1");
    ts.setSeatNumber("S1");
    ts.setSeatStatusId(1L);
    ts.setTicketSeatId(4567890L);

    List<TicketSeat> ticketSeats = new ArrayList<>();

    ticketSeats.add(ts);
    listingDTO.setDbListing(listing);


    when(listingSeatTraitMgr.findSeatTraits(Mockito.anyLong())).thenReturn(seatTraits);
    when(ticketSeatMgr.findTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);
    when(eventHelper.getEvent(Mockito.anyLong(), Mockito.anyBoolean()))
        .thenReturn(new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event());
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);

    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("false");
    when(ticketSeatMgr.findTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);
    when(eventHelper.convert(
        Mockito.any(com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event.class)))
            .thenReturn(getEvent());
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNull(router);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerNewFlowEnabled() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(2);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setDeliveryOption(2);
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNotNull(router);
      Assert.assertEquals(router.getClass(), UpdateListingHandler.class);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerNewFlowEnabledPriceUpdatePayout() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setPayoutPerProduct(new Money("16"));

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setListPrice(new Money("15"));
    listing.setSellerPayoutAmountPerTicket(new Money("12"));
    listing.setDeliveryOption(2);
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNotNull(router);
      Assert.assertEquals(router.getClass(), UpdateListingHandler.class);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerNewFlowEnabledPriceUpdateDisplayPrice() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setBuyerSeesPerProduct(new Money("16"));

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setListPrice(new Money("15"));
    listing.setDisplayPricePerTicket(new Money("12"));
    listing.setDeliveryOption(2);
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNotNull(router);
      Assert.assertEquals(router.getClass(), UpdateListingHandler.class);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerNewFlowEnabledUpdateFaceValue() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setFaceValue(new Money("16"));

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setListPrice(new Money("15"));
    listing.setSellerPayoutAmountPerTicket(new Money("12"));
    listing.setFaceValue(new Money("12"));
    listing.setDeliveryOption(2);
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(eventHelper.getEvent(Mockito.anyLong(), Mockito.anyBoolean()))
        .thenReturn(new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event());
    when(eventHelper.convert(
        Mockito.any(com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event.class)))
            .thenReturn(getEvent());
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release4", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNotNull(router);
      Assert.assertEquals(router.getClass(), UpdateListingHandler.class);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }

    try {
      listing.setFaceValue(new Money("16"));
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNotNull(router);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerFailOnvalidateDbListingNulldbListing() {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(2);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);

    when(inventoryMgr.getListing(anyLong())).thenReturn(null);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
    } catch (ListingException e) {
      Assert.assertNotNull(e);
      Assert.assertEquals(e.getErrorCodeEnum(), ErrorCodeEnum.listingNotFound);
      Assert.assertEquals(e.getType(), ErrorType.NOT_FOUND);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerFailOnvalidateDbListingListingNotActive() {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(2);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setDeliveryOption(2);
    listing.setSystemStatus("INACTIVE");

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
    } catch (ListingException e) {
      Assert.assertNotNull(e);
      Assert.assertEquals(e.getErrorCodeEnum(), ErrorCodeEnum.listingNotActive);
      Assert.assertEquals(e.getType(), ErrorType.BUSINESSERROR);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerFailOnExpiredListing() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(2);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setSystemStatus("ACTIVE");
    Calendar endDate = Calendar.getInstance();
    endDate.add(Calendar.DAY_OF_YEAR, -5);
    listing.setEndDate(endDate);
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);

    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
    } catch (ListingException e) {
      Assert.assertNotNull(e);
      Assert.assertEquals(e.getErrorCodeEnum(), ErrorCodeEnum.listingExpired);
      Assert.assertEquals(e.getType(), ErrorType.BUSINESSERROR);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerFailOnvalidateDbListingZeroQuantityRemain() {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(2);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(0);
    listing.setSellerId(1L);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
    } catch (ListingException e) {
      Assert.assertNotNull(e);
      Assert.assertEquals(e.getErrorCodeEnum(), ErrorCodeEnum.listingAlreadySold);
      Assert.assertEquals(e.getType(), ErrorType.BUSINESSERROR);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerDeleteListing() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setStatus(ListingStatus.DELETED);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setSystemStatus("ACTIVE");
    listing.setDeliveryOption(2);
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release2", "false"))
        .thenReturn("true");
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release3", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    when(eventHelper.getEvent(listing.getEventId(),
        (listing.getSeatTraits() != null && !listing.getSeatTraits().isEmpty())))
            .thenReturn(new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event());
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNotNull(router);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerDeliveryOptionPDF() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(2);
    listingRequest.setDeliveryOption(DeliveryOption.UPS);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setTicketMedium(2);
    listing.setDeliveryOption(2);
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNull(router);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerDeliveryOptionWillcall() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(2);
    listingRequest.setDeliveryOption(DeliveryOption.UPS);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setFulfillmentDeliveryMethods("8,");
    listing.setDeliveryOption(2);
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNull(router);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerDeliveryOptionFLASHSEAT() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(2);
    listingRequest.setDeliveryOption(DeliveryOption.UPS);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setTicketMedium(4);
    listing.setDeliveryOption(1);
    listingDTO.setDbListing(listing);
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNull(router);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerDeliveryOptionFlashTransfer() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(2);
    listingRequest.setDeliveryOption(DeliveryOption.UPS);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setTicketMedium(TicketMedium.EXTFLASH.getValue());
    listing.setDeliveryOption(2);
    listingDTO.setDbListing(listing);
    
    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNull(router);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerDeliveryOptionEventCard() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(2);
    listingRequest.setDeliveryOption(DeliveryOption.UPS);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setTicketMedium(6);
    listing.setDeliveryOption(2);
    listingDTO.setDbListing(listing);
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNull(router);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerDeliveryOptionUPS() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(2);
    listingRequest.setDeliveryOption(DeliveryOption.PDF);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setFulfillmentDeliveryMethods("10,");
    listing.setDeliveryOption(2);
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNull(router);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerDeliveryOptionLOCALDELIVERY() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(2);
    listingRequest.setDeliveryOption(DeliveryOption.UPS);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setFulfillmentDeliveryMethods("17,");
    listing.setDeliveryOption(2);
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNull(router);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerDeliveryOptionMobile() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(2);
    listingRequest.setDeliveryOption(DeliveryOption.UPS);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setTicketMedium(TicketMedium.MOBILE.getValue());
    listing.setDeliveryOption(1);
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNull(router);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerDeliveryOptionMobileTransfer() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(2);
    listingRequest.setDeliveryOption(DeliveryOption.UPS);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setTicketMedium(TicketMedium.EXTMOBILE.getValue());
    listing.setDeliveryOption(2);
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNull(router);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerDeliveryOptionSHIPPING() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(2);
    listingRequest.setDeliveryOption(DeliveryOption.PDF);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setFulfillmentDeliveryMethods("12,");
    listing.setDeliveryOption(2);
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNull(router);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerDeliveryOptionLMS() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(2);
    listingRequest.setDeliveryOption(DeliveryOption.UPS);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setFulfillmentDeliveryMethods("7,");
    listing.setDeliveryOption(2);
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNull(router);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerUnknownDeliveryOption() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(2);
    listingRequest.setDeliveryOption(DeliveryOption.UPS);

    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setFulfillmentDeliveryMethods("0,");
    listing.setDeliveryOption(2);
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNull(router);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }
  }

  @Test
  public void testGetBusinessFlowHandlerInhandDateInvalidFormat() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setInhandDate("2011");
    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);

    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setDeliveryOption(2);
    listing.setInhandDate(Calendar.getInstance());
    listingDTO.setDbListing(listing);

    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    try {
      BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
      Assert.assertNull(router);
    } catch (ListingException le) {
      Assert.assertNotNull(le);
      Assert.assertEquals(le.getErrorCodeEnum(), ErrorCodeEnum.invalidDateFormat);
      Assert.assertEquals(le.getType(), ErrorType.INPUTERROR);
    } catch (Exception e) {
      Assert.fail("Should not reach to here");
    }

  }

  @Test
  public void testGetBusinessFlowHandlerAddSeats() throws Exception {
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setProducts(getProducts());
    ListingDTO listingDTO = new ListingDTO(listingRequest);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1L);
    listingDTO.setSellerInfo(sellerInfo);

    Listing listing = new Listing();
    listing.setId(1L);
    listing.setQuantityRemain(1);
    listing.setSellerId(1L);
    listing.setDeliveryOption(2);
    listing.setInhandDate(Calendar.getInstance());
    listing.setEventId(123456L);
    listingDTO.setDbListing(listing);


    when(inventoryMgr.getListing(anyLong())).thenReturn(listing);
    when(eventHelper.getEvent(Mockito.anyLong(), Mockito.anyBoolean()))
        .thenReturn(new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event());
    when(masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1", "false"))
        .thenReturn("true");
    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    UpdateListingHandler updateListingHandler =
        new UpdateListingHandler(listingDTO, updateAttributeList);
    when(beanFactory.getBean(anyString(), Matchers.any(ListingDTO.class), Matchers.any(List.class)))
        .thenReturn(updateListingHandler);
    when(eventHelper.convert(
        Mockito.any(com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event.class)))
            .thenReturn(getEvent());
    BusinessFlowHandler router = updateListingFlowRouter.getBusinessFlowHandler(listingDTO);
    Assert.assertNull(router);

  }

  private List<Product> getProducts() {
    List<Product> products = new ArrayList<>();
    Product prod = new Product();
    prod.setRow("B");
    prod.setSeat("1");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.ADD);
    products.add(prod);


    Product prod1 = new Product();
    prod1.setRow("C");
    prod1.setSeat("1");
    prod1.setProductType(ProductType.TICKET);
    prod1.setOperation(Operation.DELETE);
    products.add(prod1);

    Product prod2 = new Product();
    prod2.setRow("N/A");
    prod2.setProductType(ProductType.TICKET);
    prod2.setOperation(Operation.UPDATE);
    products.add(prod2);
    return products;
  }

  private com.stubhub.domain.inventory.datamodel.entity.Event getEvent() {
    Event event = new Event();
    event.setId(123456789l);

    return event;
  }


}
