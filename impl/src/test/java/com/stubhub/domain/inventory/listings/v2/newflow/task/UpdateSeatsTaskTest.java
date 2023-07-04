package com.stubhub.domain.inventory.listings.v2.newflow.task;

import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfiguration;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.entity.ListingCheck;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ProductInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.UpdateListingInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.TicketSeatHelper;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.api.VenueConfigV3ApiHelper;
import com.stubhub.domain.inventory.listings.v2.util.CmaValidator;
import com.stubhub.domain.inventory.listings.v2.util.InventorySolrUtil;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.newplatform.common.entity.Money;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static com.stubhub.common.exception.ErrorType.INPUTERROR;
import static com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum.invalidRowWords;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class UpdateSeatsTaskTest {

  private static final String TICKET_ROW = "1";
  private static final String TICKET_SEAT = "3";

  private ListingDTO listingDTO = mock(ListingDTO.class);
  private TicketSeatHelper ticketSeatHelper = mock(TicketSeatHelper.class);

  private InventorySolrUtil inventorySolrUtil = mock(InventorySolrUtil.class);
  private CmaValidator cmaValidator = new CmaValidator();

  private VenueConfigV3ApiHelper venueConfigV3ApiHelper;

  @InjectMocks
  private UpdateSeatsTask updateSeatsTask = new UpdateSeatsTask(listingDTO);

  @BeforeMethod
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    ReflectionTestUtils.setField(updateSeatsTask, "ticketSeatHelper", ticketSeatHelper);
    venueConfigV3ApiHelper = mock(VenueConfigV3ApiHelper.class);
    when(venueConfigV3ApiHelper.getVenueDetails(any(Long.class))).thenReturn(mockVenueConfiguration());
    ReflectionTestUtils.setField(cmaValidator, "venueConfigV3ApiHelper", venueConfigV3ApiHelper);
    ReflectionTestUtils.setField(updateSeatsTask, "cmaValidator", cmaValidator);
    when(inventorySolrUtil.isListingExists(any(Long.class), any(Long.class),
            any(String.class), any(String.class), any(String.class),
            any(Long.class)))
        .thenReturn(new ListingCheck());
    ReflectionTestUtils.setField(updateSeatsTask, "inventorySolrUtil", inventorySolrUtil);
  }

  @Test
  public void testUpdateTicketFailWhenCanNotFindSeat() {
    String row = "1", anotherRow = "2";
    Product ticket = aTicket(row, TICKET_SEAT);
    TicketSeat ticketSeat = aTicketSeat(anotherRow, TICKET_SEAT);

    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfoOf(ticket));
    when(listingDTO.getDbListing()).thenReturn(aListing(asList(ticketSeat), 1));
    when(listingDTO.getListingRequest()).thenReturn(new ListingRequest());
    try {
      updateSeatsTask.execute();
      fail("Should not reach here");
    } catch (ListingException e) {
      assertTrue(e.getCustomMessage().contains("Cannot locate seat product to update"));
    }
  }

  @Test
  public void testUpdateTicketSuccess() {
    Product ticket = aTicket();
    TicketSeat ticketSeat = aTicketSeat();

    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfoOf(ticket));
    when(listingDTO.getDbListing()).thenReturn(aListing(asList(ticketSeat), 1));
    when(listingDTO.getListingRequest()).thenReturn(new ListingRequest());

    updateSeatsTask.execute();

    assertEquals(listingDTO.getDbListing().getTicketSeats().get(0).getRow(), TICKET_ROW);
    assertEquals(listingDTO.getDbListing().getTicketSeats().get(0).getSeatNumber(), TICKET_SEAT);
  }

  @Test
  public void testUpdateTicketSuccessWhenExistingSeatRowIsNull() {
    Product ticket = aTicket();
    TicketSeat ticketSeat = aTicketSeat(null, null);
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setProducts(asList(ticket));

    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfoOf(ticket));
    when(listingDTO.getDbListing()).thenReturn(aListing(asList(ticketSeat), 1));
    when(listingDTO.getListingRequest()).thenReturn(listingRequest);

    updateSeatsTask.execute();

    assertEquals(listingDTO.getDbListing().getTicketSeats().get(0).getRow(), TICKET_ROW);
    assertEquals(listingDTO.getDbListing().getTicketSeats().get(0).getSeatNumber(), TICKET_SEAT);
  }

  @Test
  public void testUpdateParkingPassSuccessWhenExistingSeatRowIsNull() {
    Product parkingPass = aParkingPass();
    TicketSeat ticketSeat = aParkingPassTicketSeat();
    ListingRequest listingRequest = new ListingRequest();

    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfoOf(parkingPass));
    when(listingDTO.getDbListing()).thenReturn(aListing(asList(ticketSeat), 0));
    when(listingDTO.getListingRequest()).thenReturn(listingRequest);

    updateSeatsTask.execute();

    assertEquals(listingDTO.getDbListing().getTicketSeats().get(0).getRow(), TICKET_ROW);
    assertEquals(listingDTO.getDbListing().getTicketSeats().get(0).getSeatNumber(), TICKET_SEAT);
  }

  @Test
  public void testUpdateTicketSuccessWhenExistingSeatRowIsNull2() {
    Product ticket = aTicket();
    TicketSeat ticketSeat = aParkingPassTicketSeat();

    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfoOf(ticket));
    when(listingDTO.getDbListing()).thenReturn(aListing(asList(ticketSeat), 1));
    when(listingDTO.getListingRequest()).thenReturn(new ListingRequest());

    updateSeatsTask.execute();

    assertEquals(listingDTO.getDbListing().getTicketSeats().get(0).getRow(), TICKET_ROW);
    assertEquals(listingDTO.getDbListing().getTicketSeats().get(0).getSeatNumber(), TICKET_SEAT);
  }

  @Test
  public void testUpdateParkingPassFailWhenCanNotLocateProduct() {
    Product ticket = aTicket("GA", "newSeat");
    TicketSeat ticketSeat = aParkingPassTicketSeat();
    ticketSeat.setGeneralAdmissionInd(true);
    ticketSeat.setSeatStatusId(2L);

    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfoOf(ticket));
    when(listingDTO.getDbListing()).thenReturn(aListing(asList(ticketSeat), 0));
    when(listingDTO.getListingRequest()).thenReturn(new ListingRequest());

    try {
      updateSeatsTask.execute();
      fail("Should not reach here");
    } catch (ListingException e) {
      assertTrue(e.getCustomMessage().contains("Cannot locate seat product to update"));
    }
  }

  @Test
  public void testUpdateTicketSuccessWhenTicketStatusIsNotOne() {
    Product ticket = aTicket();
    TicketSeat ticketSeat = aTicketSeat();
    ticketSeat.setSeatStatusId(100L);

    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfoOf(ticket));
    when(listingDTO.getDbListing()).thenReturn(aListing(asList(ticketSeat), 1));
    when(listingDTO.getListingRequest()).thenReturn(new ListingRequest());

    try {
      updateSeatsTask.execute();
      fail("Should not reach here");
    } catch (ListingException e) {
      assertNotNull(e);
      assertTrue(e.getCustomMessage().contains("Cannot locate seat product to update"));
    }
  }
  
  @Test
  public void testUpdateTicketTaskExternalIdSuccess() {
    Product ticket1 = aTicket("General Admission", null);
    ticket1.setExternalId("11");
    Product ticket2 = aTicket("General Admission", null);
    ticket2.setExternalId("12");

    TicketSeat ticketSeat1 = aTicketSeat(null, null);
    ticketSeat1.setExternalSeatId("11");
    TicketSeat ticketSeat2 = aTicketSeat(null, null);
    ticketSeat1.setExternalSeatId("12");

    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfoOf(ticket1, ticket2));
    when(listingDTO.getDbListing()).thenReturn(aListing(asList(ticketSeat1, ticketSeat2), 2));
    when(listingDTO.getListingRequest()).thenReturn(new ListingRequest());

    ListingDTO dto = updateSeatsTask.call();

    assertNotNull(dto);
  }
  
  @Test
  public void testUpdateTicketTaskSameSeatSuccess() {
    Product ticket1 = aTicket("General Admission", "1");
    ticket1.setExternalId("11");
    Product ticket2 = aTicket("General Admission", "2");
    ticket2.setExternalId("12");

    TicketSeat ticketSeat1 = aTicketSeat("General Admission", null);
    ticketSeat1.setExternalSeatId("11");
    TicketSeat ticketSeat2 = aTicketSeat("General Admission", null);
    ticketSeat1.setExternalSeatId("12");

    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfoOf(ticket1, ticket2));
    when(listingDTO.getDbListing()).thenReturn(aListing(asList(ticketSeat1, ticketSeat2), 2));
    when(listingDTO.getListingRequest()).thenReturn(new ListingRequest());

    ListingDTO dto = updateSeatsTask.call();

    assertNotNull(dto);
  }

  @DataProvider(name = "bannedFullValues")
  public static Object[][] bannedFullValues() {
    return CmaValidatorTestUtil.bannedFullValues();
  }

  @Test(dataProvider = "bannedFullValues")
  public void givenCMAListingWithBannedFullValueInRowField_thenItFails(String bannedValue) {
    Product ticket = aTicket(bannedValue, TICKET_SEAT);
    TicketSeat ticketSeat = aTicketSeat(bannedValue, TICKET_SEAT);

    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfoOf(ticket));
    when(listingDTO.getDbListing()).thenReturn(aCmaListing(asList(ticketSeat), 1));
    when(listingDTO.getListingRequest()).thenReturn(new ListingRequest());

    try {
      updateSeatsTask.execute();
      fail(format("An exception should've been thrown before reaching this point - value [%s]", bannedValue));
    } catch (ListingException e) {
      assertEquals(INPUTERROR, e.getType());
      assertEquals(invalidRowWords, e.getErrorCodeEnum());
      assertEquals("Row/Seat contains words that are not allowed", e.getCustomMessage());
    }
  }

  @Test(dataProvider = "bannedFullValues")
  public void givenCMAListingWithBannedFullValueInSeatField_thenItFails(String bannedValue) {
    Product product = aTicket(TICKET_ROW, bannedValue);
    TicketSeat ticketSeat = aTicketSeat(TICKET_ROW, bannedValue);

    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfoOf(product));
    when(listingDTO.getDbListing()).thenReturn(aCmaListing(asList(ticketSeat), 1));
    when(listingDTO.getListingRequest()).thenReturn(new ListingRequest());

    try {
      updateSeatsTask.execute();
      fail(format("An exception should've been thrown before reaching this point - value [%s]", bannedValue));
    } catch (ListingException e) {
      assertEquals(INPUTERROR, e.getType());
      assertEquals(invalidRowWords, e.getErrorCodeEnum());
      assertEquals("Row/Seat contains words that are not allowed", e.getCustomMessage());
    }
  }

  @DataProvider(name = "bannedPartialValuesForRow")
  public static Object[][] bannedPartialValuesForRow() {
    return CmaValidatorTestUtil.bannedPartialValuesForRow();
  }

  @Test(dataProvider = "bannedPartialValuesForRow")
  public void givenCMAListingWithBannedPartialValueInRowField_thenItFails(String bannedValue) {
    Product ticket = aTicket(bannedValue, TICKET_SEAT);
    TicketSeat ticketSeat = aTicketSeat(bannedValue, TICKET_SEAT);

    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfoOf(ticket));
    when(listingDTO.getDbListing()).thenReturn(aCmaListing(asList(ticketSeat), 1));
    when(listingDTO.getListingRequest()).thenReturn(new ListingRequest());

    try {
      updateSeatsTask.execute();
      fail(format("An exception should've been thrown before reaching this point - value [%s]", bannedValue));
    } catch (ListingException e) {
      assertEquals(INPUTERROR, e.getType());
      assertEquals(invalidRowWords, e.getErrorCodeEnum());
      assertEquals("Row/Seat contains words that are not allowed", e.getCustomMessage());
    }
  }

  @DataProvider(name = "bannedPartialValuesFoSeat")
  public static Object[][] bannedPartialValuesForSeat() {
    return CmaValidatorTestUtil.bannedPartialValuesForSeat();
  }

  @Test(dataProvider = "bannedPartialValuesForSeat")
  public void givenCMAListingWithBannedPartialValueInSeatField_thenItFails(String bannedValue) {
    Product product = aTicket(TICKET_ROW, bannedValue);
    TicketSeat ticketSeat = aTicketSeat(TICKET_ROW, bannedValue);

    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfoOf(product));
    when(listingDTO.getDbListing()).thenReturn(aCmaListing(asList(ticketSeat), 1));
    when(listingDTO.getListingRequest()).thenReturn(new ListingRequest());

    try {
      updateSeatsTask.execute();
      fail(format("An exception should've been thrown before reaching this point - value [%s]", bannedValue));
    } catch (ListingException e) {
      assertEquals(INPUTERROR, e.getType());
      assertEquals(invalidRowWords, e.getErrorCodeEnum());
      assertEquals("Row/Seat contains words that are not allowed", e.getCustomMessage());
    }
  }

  @Test
  public void testUpdateFaceValue() {
    Product prod = new Product();
    prod.setRow("B");
    prod.setSeat("1");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.UPDATE);
    Money faceValue = new Money("100.0", "GBP");
    prod.setFaceValue(faceValue);

    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfoOf(prod));
    when(listingDTO.getDbListing()).thenReturn(aCmaListing(asList(aTicketSeat("B", "1")), 1));

    updateSeatsTask.call();

    List<TicketSeat> ticketSeatList = this.listingDTO.getDbListing().getTicketSeats();
    TicketSeat newSeat = ticketSeatList.get(ticketSeatList.size() - 1);
    assertEquals(faceValue, newSeat.getFaceValue());
    assertEquals(Currency.getInstance(faceValue.getCurrency()), newSeat.getCurrency());

  }

  @Test
  public void testUpdateFaceValueFromListing() {
    Product prod = new Product();
    prod.setRow("B");
    prod.setSeat("1");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.UPDATE);
    Money faceValue = new Money("100.0", "GBP");

    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfoOf(prod));
    Listing listing = aCmaListing(asList(aTicketSeat("B", "1")), 1);
    listing.setFaceValue(faceValue);
    when(listingDTO.getDbListing()).thenReturn(listing);

    updateSeatsTask.call();

    List<TicketSeat> ticketSeatList = this.listingDTO.getDbListing().getTicketSeats();
    TicketSeat newSeat = ticketSeatList.get(ticketSeatList.size() - 1);
    assertEquals(faceValue, newSeat.getFaceValue());
    assertEquals(Currency.getInstance(faceValue.getCurrency()), newSeat.getCurrency());

  }

  private Listing aListing(List<TicketSeat> seats, int remainingQuantity) {
    return aListing(seats, remainingQuantity, anEvent(), "USD");
  }

  private Listing aListing(List<TicketSeat> seats, int remainingQunatity, Event event, String currencyCode) {
    Listing listing = new Listing();
    listing.setTicketSeats(seats);
    listing.setQuantityRemain(remainingQunatity);
    listing.setEvent(event);
    listing.setCurrency(Currency.getInstance(currencyCode));
    listing.setSection("Section Ducks");
    return listing;
  }

  private Listing aCmaListing(List<TicketSeat> seats, int remainingQuantity) {
    return aListing(seats, remainingQuantity, aCMAEvent(), "GBP");
  }

  private UpdateListingInfo updateListingInfoOf(Product ...product) {
    ProductInfo productInfo = new ProductInfo();
    productInfo.setUpdateProducts(asList(product));
    UpdateListingInfo info = new UpdateListingInfo();
    info.setProductInfo(productInfo);
    return info;
  }

  private Product aProduct(ProductType productType,String row, String seat) {
    Product product = new Product();
    product.setProductType(productType);
    product.setExternalId("11");
    product.setRow(row);
    product.setSeat(seat);
    product.setUniqueTicketNumber("123");
    return product;
  }

  private Product aTicket(String row, String seat) {
    return aProduct(ProductType.TICKET, row, seat);
  }

  private Product aTicket() {
    return aTicket(TICKET_ROW, TICKET_SEAT);
  }

  private Product aParkingPass() {
    return aProduct(ProductType.PARKING_PASS, TICKET_ROW, TICKET_SEAT);
  }

  private TicketSeat aTicketSeat(String row, String seat) {
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setTixListTypeId(1L);
    ticketSeat.setGeneralAdmissionInd(false);
    ticketSeat.setRow(row);
    ticketSeat.setSeatNumber(seat);
    ticketSeat.setSeatStatusId(1L);
    ticketSeat.setExternalSeatId("22");
    ticketSeat.setUniqueTicketNumber("1234");
    return ticketSeat;
  }

  private TicketSeat aTicketSeat() {
    return aTicketSeat(TICKET_ROW, TICKET_SEAT);
  }

  private TicketSeat aParkingPassTicketSeat() {
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setTixListTypeId(2L);
    ticketSeat.setGeneralAdmissionInd(false);
    ticketSeat.setSeatStatusId(1L);
    ticketSeat.setExternalSeatId("11");
    return ticketSeat;
  }

  private Event anEvent() {
    Event event = new Event();
    event.setActive(true);
    event.setBookOfBusinessId(1l);
    event.setCountry("US");
    event.setCurrency(Currency.getInstance("USD"));
    event.setDescription("Wicked New York Tickets");
    event.setEventDate(Calendar.getInstance());
    event.setGaIndicator(false);
    event.setGenreParent(700154l);
    event.setGenrePath("174/700188/43992/700154/");
    event.setGeographyParent(198377l);
    event.setGeoPath("6628/201883/6629/198377/");
    event.setId(123456789l);
    event.setIsCreditToTeamAccountSupported(false);
    event.setIsEticketAllowed(false);
    event.setIsIntegrated(false);
    event.setJdkTimeZone(TimeZone.getDefault());
    event.setRowScrubbing(false);
    event.setSectionScrubbing(false);
    event.setVenueConfigId(1234l);
    event.setVenueDesc("Apollo Victoria Theatre");

    return event;
  }

  private Event aCMAEvent() {
    Event event = new Event();
    event.setActive(true);
    event.setBookOfBusinessId(1L);
    event.setCountry("GB");
    event.setCurrency(Currency.getInstance("GBP"));
    event.setDescription("Sample CMA Event");
    event.setEventDate(Calendar.getInstance());
    event.setGaIndicator(false);
    event.setGenreParent(700154L);
    event.setGenrePath("174/700188/43992/700154/");
    event.setGeographyParent(198377L);
    event.setGeoPath("6628/201883/6629/198377/");
    event.setId(123456789L);
    event.setIsCreditToTeamAccountSupported(false);
    event.setIsEticketAllowed(false);
    event.setIsIntegrated(false);
    event.setJdkTimeZone(TimeZone.getDefault());
    event.setRowScrubbing(false);
    event.setSectionScrubbing(false);
    event.setVenueConfigId(1234L);
    event.setVenueDesc("O2 Arena London");

    return event;
  }

  private static VenueConfiguration mockVenueConfiguration() {
    VenueConfiguration venueConfig = new VenueConfiguration();
    venueConfig.setGeneralAdmissionOnly(false);
    venueConfig.setSeatingZones(Collections.EMPTY_LIST);
    return venueConfig;
  }
}
