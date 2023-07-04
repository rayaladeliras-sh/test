package com.stubhub.domain.inventory.biz.v2.impl;

import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.biz.v2.intf.ListingTicketMediumMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.datamodel.dao.ListingDAO;
import com.stubhub.domain.inventory.datamodel.dao.TTOrderDAO;
import com.stubhub.domain.inventory.datamodel.dao.UserAgentDAO;
import com.stubhub.domain.inventory.datamodel.entity.*;
import junit.framework.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class InventoryMgrTest {

  @InjectMocks
  private InventoryMgrImpl inventoryMgrImpl;

  @Mock
  private ListingDAO listingDAO;

  @Mock
  private TicketSeatMgr ticketSeatMgr;

  @Mock
  private ListingSeatTraitMgr listingSeatTraitMgr;

  @Mock
  private UserAgentDAO userAgentDAO;

  @Mock
  private TTOrderDAO ttOrderDAO;

  @Mock
  private ListingTicketMediumMgr listingTicketMediumMgr;

  Listing listing;

  @BeforeMethod
  public void setUp() {

    MockitoAnnotations.initMocks(this);
    listing = populateListing();

    handleMockInvocations();
  }

  private Listing populateListing() {
    Listing listing = new Listing();

    List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setTicketSeatId(12367l);
    ticketSeats.add(ticketSeat);
    listing.setTicketSeats(ticketSeats);

    List<ListingSeatTrait> listingSeatTraits = new ArrayList<ListingSeatTrait>();
    ListingSeatTrait listingSeatTrait = new ListingSeatTrait();
    listingSeatTraits.add(listingSeatTrait);
    listing.setSeatTraits(listingSeatTraits);

    List<ListingTicketMediumXref> listingTicketMediumXrefs = new ArrayList<>();
    ListingTicketMediumXref listingTicketMediumXref = new ListingTicketMediumXref();
    listingTicketMediumXrefs.add(listingTicketMediumXref);
    listing.setTicketMediums(listingTicketMediumXrefs);

    listing.setId(26543278L);
    return listing;
  }

  private void handleMockInvocations() {

    when(listingDAO.addListing(listing)).thenReturn(listing);

    handleCreateInvocations();

    handleUpdateInovcations();

  }

  private void handleCreateInvocations() {
    // Handle Create Inovcations
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(ticketSeatMgr).addTicketSeat(any(TicketSeat.class));

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(listingSeatTraitMgr).addSeatTrait(any(ListingSeatTrait.class));

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(listingTicketMediumMgr).addTicketMedium(any(ListingTicketMediumXref.class));
  }

  private void handleUpdateInovcations() {
    // Handle Update Inovcations
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(ticketSeatMgr).updateTicketSeat(any(TicketSeat.class));
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        return null;
      }
    }).when(listingSeatTraitMgr).addSeatTrait(any(ListingSeatTrait.class));
    when(listingDAO.updateListing(listing)).thenReturn(listing);
  }

  @Test
  public void addListingTest() {
    inventoryMgrImpl.addListing(listing);
    Assert.assertNotNull(listing.getId());
  }

  @Test
  public void addListingErrorTest() {
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        throw new Exception();
      }
    }).when(ticketSeatMgr).addTicketSeat(any(TicketSeat.class));

    try {
      inventoryMgrImpl.addListing(listing);
    } catch (Exception e) {
      Assert.assertTrue(true);
    }
    Assert.assertNotNull(listing.getId());
  }

  @Test
  public void updateListingTest() {
    inventoryMgrImpl.updateListing(listing);
    inventoryMgrImpl.updateListings(Arrays.asList(new Listing[]{listing}));
    Assert.assertNotNull(listing.getId());
    inventoryMgrImpl.updateListings(Arrays.asList(new Listing[]{}));
  }

  @Test
  public void updateListingErrorTest() {

    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        throw new Exception();
      }
    }).when(ticketSeatMgr).updateTicketSeat(any(TicketSeat.class));

    try {
      inventoryMgrImpl.updateListing(listing);
    } catch (Exception e) {
      Assert.assertTrue(true);
    }

    Assert.assertNotNull(listing.getId());
  }

  @Test
  public void getListingTest() {
    Long id = new Long(2456783L);
    when(listingDAO.getListingById(any(Long.class))).thenReturn(new Listing());
    inventoryMgrImpl.getListing(id);

  }

  @Test
  public void getTTOrderTest() {
    TTOrder ttOrder = new TTOrder();
    when(ttOrderDAO.addTTOrder(any(TTOrder.class))).thenReturn(ttOrder);
    ttOrder = inventoryMgrImpl.addTTOrder(ttOrder);
    Assert.assertNotNull(ttOrder);
    Assert.assertNotNull(inventoryMgrImpl.getTtOrderDAO());

  }

  @Test
  public void testgetListingWithLocale() throws Exception {
    when(listingDAO.getListingById(any(Long.class))).thenReturn(new Listing());
    inventoryMgrImpl.getListing(123L, Locale.US);
    inventoryMgrImpl.getListing(123L, Locale.UK);
  }

  @Test
  public void findListing() {
    when(listingDAO.findListing(any(Long.class), any(String.class), any(String.class),
        any(String.class))).thenReturn(new Listing());
    inventoryMgrImpl.findListing(12345L, "Black Box", "12", "1,2");
  }

  @Test
  public void findListing1() {
    when(listingDAO.findListingBySectionRow(any(Long.class), any(String.class), any(String.class)))
        .thenReturn(new Listing());
    inventoryMgrImpl.findListingBySectionRow(12345L, "Black Box", "12");
  }

  @Test
  public void getListingBySellerIdExternalIdAndStatusTest() {
    List<Listing> listings = new ArrayList<Listing>();
    Listing listing = new Listing();
    listing.setId(123L);  
    listing.setSystemStatus("ACTIVE");
    listings.add(listing);
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.add(Calendar.HOUR, +24);
    listing.setEndDate(calendar);
    listing.setExternalId("12345");
    listing.setQuantityRemain(2);
    when(listingDAO.getListingBySellerIdAndExternalId(any(Long.class), any(String.class)))
        .thenReturn(listings);
    Listing elisting = inventoryMgrImpl.getListingBySellerIdExternalIdAndStatus(12345L, "1234");
    Assert.assertNotNull(elisting);

    // set remaining quantity to zero; we should get a null listing. SELLAPI-1814
    listing.setQuantityRemain(0);
    elisting = inventoryMgrImpl.getListingBySellerIdExternalIdAndStatus(12345L, "1234");
    Assert.assertNull(elisting);

  }

  @Test
  public void getListingBySellerIdExternalIdAndStatusTestExpired() {
    List<Listing> listings = new ArrayList<Listing>();
    Listing listing = new Listing();
    listing.setId(123L);
    listing.setSystemStatus("ACTIVE");
    listings.add(listing);
    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    calendar.add(Calendar.SECOND, -1);
    listing.setEndDate(calendar);
    listing.setExternalId("12345");
    when(listingDAO.getListingBySellerIdAndExternalId(any(Long.class), any(String.class)))
        .thenReturn(listings);
    Listing elisting = inventoryMgrImpl.getListingBySellerIdExternalIdAndStatus(12345L, "1234");
    Assert.assertNull(elisting);

  }

  @Test
  public void getListingBySellerIdAndExternalIdTest() {
    List<Listing> listings = new ArrayList<Listing>();
    Listing listing = new Listing();
    listing.setId(123L);
    listing.setSystemStatus("ACTIVE");
    listings.add(listing);
    when(listingDAO.getListingBySellerIdAndExternalId(any(Long.class), any(String.class)))
        .thenReturn(listings);
    inventoryMgrImpl.getListingBySellerIdAndExternalId(12345L, "1234");
  }

  @Test
  public void hasSectionHadBadTermsTest() {
    when(listingDAO.hasSectionHadBadTerms(any(String.class))).thenReturn(true);
    inventoryMgrImpl.hasSectionHadBadTerms("%4677");
  }

  @Test
  public void hasRowHadBadTermsTest() {
    when(listingDAO.hasRowHadBadTerms(any(String.class))).thenReturn(true);
    inventoryMgrImpl.hasRowHadBadTerms("%4677");
  }

  @Test
  public void getUserAgentIDTest() {
    UserAgent userAgent = new UserAgent();
    userAgent.setUserAgentId(123L);
    when(userAgentDAO.findByHashId(any(Long.class))).thenReturn(null);
    when(userAgentDAO.persist(any(UserAgent.class))).thenReturn(userAgent);
    inventoryMgrImpl.getUserAgentID("UserAgent");
  }

  @Test
  public void getListingsTest() {
    List<Long> listingIds = new ArrayList<Long>();
    listingIds.add(1234L);
    listingIds.add(235432L);

    List<Listing> listings = new ArrayList<Listing>();
    Listing listing = new Listing();
    listing.setId(1234L);
    listing.setEventId(544647L);
    listing = new Listing();
    listing.setId(235432L);
    listing.setEventId(2354326876L);
    when(listingDAO.getListings(Mockito.anyList())).thenReturn(listings);
    List<Listing> listingsResult = inventoryMgrImpl.getListings(listingIds);
  }

  // SELLAPI-1181 09/09/15 START
  @Test
  public void getListingsForExternalId() {
    Long sellerId = 123L;
    List<String> externalIds = new ArrayList<String>();
    externalIds.add("1234");
    externalIds.add("235432");

    List<Listing> listings = new ArrayList<Listing>();
    Listing listing = new Listing();
    listing.setExternalId("1234");
    listing.setEventId(544647L);
    listing = new Listing();
    listing.setExternalId("235432");
    listing.setEventId(2354326876L);
    when(listingDAO.getListings(Mockito.anyList())).thenReturn(listings);
    List<Listing> listingsResult = inventoryMgrImpl.getListings(sellerId, externalIds);
  }

  @Test
  public void getListingsForExternalId_moreThan500() {
    Long sellerId = 123L;
    List<String> externalIds = new ArrayList<String>();
    List<Listing> listings = new ArrayList<Listing>();
    Listing listing = new Listing();
    for (Long i = 0L; i < 700L; i++) {
      externalIds.add("100" + i);
      listing = new Listing();
      listing.setExternalId("100" + i);
      listing.setEventId(100 + i);
      listings.add(listing);
    }
    when(listingDAO.getListings(Mockito.anyList())).thenReturn(listings);
    List<Listing> listingsResult = inventoryMgrImpl.getListings(sellerId, externalIds);
  }
  // SELLAPI-1181 09/09/15 END


  @Test
  public void getListingsTest_moreThan500() {
    List<Long> listingIds = new ArrayList<Long>();
    List<Listing> listings = new ArrayList<Listing>();
    Listing listing = new Listing();
    for (Long i = 0L; i < 700L; i++) {
      listingIds.add(100 + i);
      listing = new Listing();
      listing.setId(100 + i);
      listing.setEventId(100 + i);
      listings.add(listing);
    }
    when(listingDAO.getListings(Mockito.anyList())).thenReturn(listings);
    List<Listing> listingsResult = inventoryMgrImpl.getListings(listingIds);
  }

  @Test
  public void testGetAllFlashListingsForSeller() {
    when(listingDAO.getAllPendingFlashListings(12345L)).thenReturn(new ArrayList<Listing>());
    Assert.assertNotNull(inventoryMgrImpl.getAllPendingFlashListings(12345L));
  }
  
  @Test
  public void updateSystemStatusTest() {
    inventoryMgrImpl.updateSystemStatus(listing);
    Assert.assertNotNull(listing.getId());
  }
}
