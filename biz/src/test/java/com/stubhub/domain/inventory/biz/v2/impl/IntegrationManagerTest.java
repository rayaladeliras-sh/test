package com.stubhub.domain.inventory.biz.v2.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.util.PartnerIntegrationConstants;
import com.stubhub.domain.inventory.datamodel.dao.ListingDAO;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingType;
import com.stubhub.domain.inventory.v2.enums.TicketSeatStatusEnum;
import com.stubhub.domain.partnerintegration.datamodel.enums.TicketStatusEnum;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.LockInventoryRequest;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.LockInventoryResponse;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.PartnerListing;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.PartnerProduct;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.ProductType;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.UnlockInventoryRequest;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.UnlockInventoryResponse;
import com.stubhub.domain.user.contactsV2.intf.CustomerContactV2Details;
import com.stubhub.domain.user.contactsV2.intf.CustomerContactV2Details.Address;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.common.util.DateUtil;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

public class IntegrationManagerTest {

  private static final Long listingId = 1211395778l;
  private static final Long eventId = 9444637l;
  private static final Long sellerId = 1000019l;
  private static final Long ticketId = 2778861923l;
  private static final Long ticketSeatId = 1223012l;
  private static final String sellerGuid = "C77991557A0E5E14E04400212861B256";
  private static final String section = "MOCK 51";
  private static final String row = "26";
  private static final String seat = "13";
  private static final String userGuidApiUrl =
      "https://api-int.stubprod.com/user/customers/v2/sellerId/guid";

  private String userGuidResponse =
      "{\"customer\":{\"userCookieGuid\":\"C77991557A0E5E14E04400212861B256\"}}";
  @InjectMocks
  private IntegrationManagerImpl integrationManager;

  @Mock
  private ListingDAO listingDAO;

  @Mock
  private InventoryMgr inventoryMgr;

  @Mock
  private TicketSeatMgr ticketSeatMgr;

  @Mock
  private MasterStubhubPropertiesWrapper masterStubhubPropertiesWrapper;

  @Mock
  private SvcLocator svcLocator;

  @Mock
  private WebClient webClient;

  private Listing listing;
  private List<TicketSeat> ticketSeats;
  private LockInventoryResponse lockInventoryResponse;
  private UnlockInventoryResponse unlockInventoryResponse;

  @BeforeMethod
  @Before
  public void init() {
    // integrationManager = new IntegrationManagerImpl();
    MockitoAnnotations.initMocks(this);

    listing = new Listing();
    listing.setId(listingId);
    listing.setEventId(eventId);
    listing.setSellerId(sellerId);
    listing.setSystemStatus(ListingStatus.PENDING.toString());
    listing.setSellerPaymentTypeId(12345l);
    listing.setSellerContactId(12345l);
    listing.setListingType(1l);
    listing.setDeliveryOption(2);
    ticketSeats = getTicketSeats(1);
    listing.setTicketSeats(ticketSeats);
    when(svcLocator.locate(userGuidApiUrl)).thenReturn(webClient);
    when(masterStubhubPropertiesWrapper.getProperty(
        PartnerIntegrationConstants.NEWAPI_ACCESS_TOKEN_KEY,
        PartnerIntegrationConstants.ACCESS_TOCKEN_DEFAULT_VALUE))
            .thenReturn(PartnerIntegrationConstants.ACCESS_TOCKEN_DEFAULT_VALUE);
    when(masterStubhubPropertiesWrapper.getProperty(
        PartnerIntegrationConstants.CUSTOMER_GUID_API_URL,
        PartnerIntegrationConstants.DEFAULT_CUSTOMER_GUID_API_URL)).thenReturn(userGuidApiUrl);
  }

  private List<TicketSeat> getTicketSeats(long tixListTypeId) {
    ticketSeats = new ArrayList<>();
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setTicketId(ticketId);
    ticketSeat.setTicketSeatId(ticketSeatId);
    ticketSeat.setSection(section);
    ticketSeat.setRow(row);
    ticketSeat.setSeatNumber(seat);
    ticketSeat.setTixListTypeId(tixListTypeId);
    ticketSeat.setSeatStatusId(TicketSeatStatusEnum.AVAILABLE.getCode().longValue());
    ticketSeats.add(ticketSeat);
    TicketSeat ticketSeat1 = new TicketSeat();
    ticketSeat1.setTicketId(ticketId);
    ticketSeat1.setTicketSeatId(ticketSeatId);
    ticketSeat1.setSection(section);
    ticketSeat1.setRow(row);
    ticketSeat1.setSeatNumber("14");
    ticketSeat1.setTixListTypeId(tixListTypeId);
    ticketSeat1.setSeatStatusId(TicketSeatStatusEnum.REMOVED.getCode().longValue());
    ticketSeats.add(ticketSeat1);
    return ticketSeats;
  }

  private CustomerContactV2Details createSellerContact() {
    CustomerContactV2Details contactV2 = new CustomerContactV2Details();
    Address address = new Address();
    address.setCity("San Francisco");
    address.setLine1("Fremont St");
    address.setState("CA");
    contactV2.setAddress(address);
    contactV2.setPhoneNumber("123-456-7890");
    contactV2.setEmail("test@gmail.com");
    return contactV2;
  }

  private PartnerListing createPartnerListing() {
    PartnerListing partnerListing = new PartnerListing();
    partnerListing.setId(listingId);
    partnerListing.setSellerId(1000019l);
    partnerListing.setEventId(eventId);
    partnerListing.setPricePerProduct("124");
    partnerListing.setTicketMediumId(4);

    PartnerProduct partnerProduct = new PartnerProduct();
    partnerProduct.setSeatId(ticketSeatId);
    partnerProduct.setFulfillmentArtifact("7yu8-0o9i8u7y");
    partnerProduct.setSection(section);
    partnerProduct.setSeat(seat);
    partnerProduct.setRow(row);
    partnerProduct.setProductType(ProductType.TICKET);
    partnerProduct.setSeatStatus(TicketStatusEnum.AVAILABLE);
    partnerProduct.setGa(false);
    List<PartnerProduct> products = new ArrayList<>();
    products.add(partnerProduct);
    partnerListing.setProducts(products);
    return partnerListing;
  }


  @Test
  @org.junit.Test
  public void getListingTest() {
    when(inventoryMgr.getListing(listingId)).thenReturn(listing);
    when(ticketSeatMgr.findAllTicketSeatsByTicketId(listingId)).thenReturn(ticketSeats);
    Listing newListing = integrationManager.getListing(listingId);
    verify(inventoryMgr, times(1)).getListing(listingId);
    verify(ticketSeatMgr, times(1)).findAllTicketSeatsByTicketId(listingId);

    assertNotNull(newListing);
    assertTrue(listing.getId() == newListing.getId());
    assertTrue(newListing.getTicketSeats().size() == 2);
  }

  @Test
  @org.junit.Test
  public void createLockInventoryRequestTest() {
    CustomerContactV2Details contactV2 = createSellerContact();
    LockInventoryRequest lockInventoryRequest =
        integrationManager.createLockInventoryRequest(listing, contactV2);
    assertNotNull(lockInventoryRequest);
    assertEquals(eventId, lockInventoryRequest.getEventId());
    assertNotNull(lockInventoryRequest.getListing());
    assertEquals(listingId, lockInventoryRequest.getListing().getId());
    assertEquals(sellerId, lockInventoryRequest.getListing().getSellerId());
    assertEquals(1, lockInventoryRequest.getListing().getProducts().size());
    PartnerProduct partnerProduct = lockInventoryRequest.getListing().getProducts().get(0);
    assertEquals(section, partnerProduct.getSection());
    assertEquals(row, partnerProduct.getRow());
    assertEquals(seat, partnerProduct.getSeat());
    assertEquals(ProductType.TICKET, partnerProduct.getProductType());
  }

  @Test
  @org.junit.Test
  public void createUnlockInventoryRequestTest() {
    listing.setListPrice(new Money(new BigDecimal(22.00d), "USD"));
    listing.setQuantityRemain(2);
    listing.setEndDate(DateUtil.getNextBusinessDay(DateUtil.getNowCalUTC()));
    ticketSeats.get(0).setTixListTypeId(2L);

    CustomerContactV2Details contactV2 = createSellerContact();
    UnlockInventoryRequest unlockInventoryRequest =
        integrationManager.createUnlockInventoryRequest(listing, contactV2);
    assertNotNull(unlockInventoryRequest);
    assertEquals(eventId, unlockInventoryRequest.getEventId());
    PartnerListing partnerListing = unlockInventoryRequest.getListing();
    assertNotNull(partnerListing);
    assertEquals(listingId, partnerListing.getId());
    assertEquals(sellerId, partnerListing.getSellerId());
    assertEquals(1, partnerListing.getProducts().size());
    PartnerProduct partnerProduct = partnerListing.getProducts().get(0);
    assertEquals(section, partnerProduct.getSection());
    assertEquals(row, partnerProduct.getRow());
    assertTrue((Double.parseDouble(partnerListing.getPricePerProduct()) - 4400) < 0.5);
  }

  @Test
  @org.junit.Test
  public void updateListingAfterLockTest() {
    listing.setSystemStatus("PENDING LOCK");

    lockInventoryResponse = new LockInventoryResponse();
    lockInventoryResponse.setInventoryId("123231-506558");
    lockInventoryResponse.setListing(createPartnerListing());
    lockInventoryResponse.setSellerContact(createSellerContact());
    lockInventoryResponse.setIsRegistration(false);
    when(ticketSeatMgr.findActiveTicketSeatsByTicketId(listingId)).thenReturn(ticketSeats);
    when(inventoryMgr.getListing(listingId)).thenReturn(listing);
    when(inventoryMgr.updateListingOnly(any(Listing.class))).thenReturn(listing);

    integrationManager.updateListingAfterLock(lockInventoryResponse);
  }

  @Test
  @org.junit.Test
  public void updateGiftCertificateListingAfterLockTest() {
    listing.setSystemStatus("PENDING LOCK");
    ticketSeats = getTicketSeats(ListingType.GIFT_CERTIFICATE.getId());

    lockInventoryResponse = new LockInventoryResponse();
    lockInventoryResponse.setInventoryId("123231-506558");
    lockInventoryResponse.setListing(createPartnerListing());
    lockInventoryResponse.setSellerContact(createSellerContact());
    lockInventoryResponse.setIsRegistration(true);
    when(ticketSeatMgr.findActiveTicketSeatsByTicketId(listingId)).thenReturn(ticketSeats);
    when(inventoryMgr.getListing(listingId)).thenReturn(listing);
    when(inventoryMgr.updateListingOnly(any(Listing.class))).thenReturn(listing);

    integrationManager.updateListingAfterLock(lockInventoryResponse);
  }

  @Test
  @org.junit.Test
  public void updateParkingPassListingAfterLockTest() {
    listing.setSystemStatus("PENDING LOCK");
    ticketSeats = getTicketSeats(ListingType.PARKING_PASSES_ONLY.getId());

    lockInventoryResponse = new LockInventoryResponse();
    lockInventoryResponse.setInventoryId("123231-506558");
    lockInventoryResponse.setListing(createPartnerListing());
    lockInventoryResponse.setSellerContact(createSellerContact());
    lockInventoryResponse.setIsRegistration(true);
    when(ticketSeatMgr.findActiveTicketSeatsByTicketId(listingId)).thenReturn(ticketSeats);
    when(inventoryMgr.getListing(listingId)).thenReturn(listing);
    when(inventoryMgr.updateListingOnly(any(Listing.class))).thenReturn(listing);

    integrationManager.updateListingAfterLock(lockInventoryResponse);
  }

  @Test
  @org.junit.Test
  public void updateListingAfterUnlockTest() {
    unlockInventoryResponse = new UnlockInventoryResponse();
    unlockInventoryResponse.setListing(createPartnerListing());
    unlockInventoryResponse.setSellerContact(createSellerContact());

    Mockito.doNothing().when(listingDAO).updateTicketStatus(1234L);
    integrationManager.updateListingAfterUnlock(unlockInventoryResponse);
  }


  private Response getUserGuidResponse(final boolean errorStatus) {
    InputStream is = new ByteArrayInputStream(userGuidResponse.getBytes());
    Response partnerEventsResponse =
        Response.status(errorStatus ? Status.BAD_REQUEST : Status.OK).entity(is).build();
    return partnerEventsResponse;
  }

  @Test
  @org.junit.Test
  public void testGetUserGuid() {
    when(svcLocator.locate(userGuidApiUrl)).thenReturn(webClient);
    when(webClient.get()).thenReturn(getUserGuidResponse(false));
    String actualUserGuid = integrationManager.getUserGuid(sellerId);
    assertEquals(actualUserGuid, sellerGuid);
  }

  @Test
  @org.junit.Test
  public void testGetUserGuidNull() {
    when(svcLocator.locate(userGuidApiUrl)).thenReturn(webClient);
    when(webClient.get()).thenReturn(getUserGuidResponse(true));
    String userGuid = integrationManager.getUserGuid(sellerId);
    assertNull(userGuid);
  }

  @SuppressWarnings("unchecked")
  @Test
  @org.junit.Test
  public void testGetUserGuidNullWithException() {
    when(svcLocator.locate(userGuidApiUrl)).thenReturn(webClient);
    when(webClient.get()).thenThrow(Exception.class);
    String userGuid = integrationManager.getUserGuid(sellerId);
    assertNull(userGuid);
  }

  @Test
  public void createUnlockInventoryRequestForInactiveListingTest() {
    listing.setListPrice(new Money(new BigDecimal(22.00d), "USD"));
    listing.setQuantityRemain(2);
    listing.setEndDate(DateUtil.getNextBusinessDay(DateUtil.getNowCalUTC()));
    listing.setSystemStatus("INACTIVE");
    ticketSeats.get(0).setTixListTypeId(2L);
    UnlockInventoryRequest unlockInventoryRequest =
            integrationManager.createUnlockInventoryRequest(listing, createSellerContact());
    assertNotNull(unlockInventoryRequest);
    assertEquals(eventId, unlockInventoryRequest.getEventId());
    PartnerListing partnerListing = unlockInventoryRequest.getListing();
    assertNotNull(partnerListing);
    assertEquals(listingId, partnerListing.getId());
    assertEquals(sellerId, partnerListing.getSellerId());
    assertEquals(2, partnerListing.getProducts().size());
    PartnerProduct partnerProduct = partnerListing.getProducts().get(0);
    assertEquals(section, partnerProduct.getSection());
    assertEquals(row, partnerProduct.getRow());
    assertTrue((Double.parseDouble(partnerListing.getPricePerProduct()) - 4400) < 0.5);
  }
}
