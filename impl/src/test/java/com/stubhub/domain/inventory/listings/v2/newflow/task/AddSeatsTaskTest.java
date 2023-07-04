package com.stubhub.domain.inventory.listings.v2.newflow.task;

import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfiguration;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.enums.DeliveryOptionEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.*;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.TicketSeatHelper;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.api.VenueConfigV3ApiHelper;
import com.stubhub.domain.inventory.listings.v2.util.CmaValidator;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.enums.InventoryType;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.newplatform.common.entity.Money;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static com.stubhub.common.exception.ErrorType.INPUTERROR;
import static com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum.invalidRowWords;
import static java.lang.String.format;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AddSeatsTaskTest {

  private static final Long EVENT_ID = 1271578799L;
  private static final Integer MANUAL_DELIVERY = (int) DeliveryOptionEnum.MANUAL_DELIVERY.getDeliveryOption();
  private static final Integer PRE_DELIVERY = (int) DeliveryOptionEnum.PRE_DELIVERY.getDeliveryOption();
  private static final String CURRENCY_CODE_FOR_CMA = "GBP";

  @Mock
  private ListingSeatTraitMgr listingSeatTraitMgr;
  
  @Mock
  private TicketSeatHelper ticketSeatHelper;

  private CmaValidator cmaValidator = new CmaValidator();

  private VenueConfigV3ApiHelper venueConfigV3ApiHelper;

  private ListingDTO listingDTO;

  private AddSeatsTask addSeatsTask;
  

  @BeforeMethod
  public void setup() {
    initMocks(this);

    listingDTO = aListingDTO();

    addSeatsTask = new AddSeatsTask(listingDTO);
    ReflectionTestUtils.setField(addSeatsTask, "listingSeatTraitMgr", listingSeatTraitMgr);
    ReflectionTestUtils.setField(addSeatsTask, "ticketSeatHelper", ticketSeatHelper);
    venueConfigV3ApiHelper = mock(VenueConfigV3ApiHelper.class);
    when(venueConfigV3ApiHelper.getVenueDetails(any(Long.class))).thenReturn(mockVenueConfiguration());
    ReflectionTestUtils.setField(cmaValidator, "venueConfigV3ApiHelper", venueConfigV3ApiHelper);
    ReflectionTestUtils.setField(addSeatsTask, "cmaValidator", cmaValidator);

    when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(true);
  }

  @Test(priority = 1)
  public void testUpdateAddSeats() {
    when(ticketSeatHelper.addToCSVString(anyString(), anyString())).thenReturn("2,1");

    ListingDTO dto = addSeatsTask.call();

    assertEquals(dto.getDbListing().getId(), new Long(1271578716));
    assertEquals(dto.getDbListing().getSeats(), "2,1");
  }

  @Test(priority = 2)
  public void testUpdateAddSeatsPredelivery() {
    this.listingDTO.getDbListing().setDeliveryOption(PRE_DELIVERY);
    try {
      addSeatsTask.call();
    } catch (ListingException e) {
      assertEquals(e.getMessage(), "Cannot add seats to a predelivered listing");
    }

  }

  @Test(priority = 3)
  public void testUpdateAddSeatsParkingPassError() {
    List<Product> products = someTicketsAndParkingPasses();
    this.listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(false);

    try {
      addSeatsTask.call();
    } catch (ListingException e) {
      assertEquals(e.getMessage(), "Parking pass is not supported for this event");
    }

  }

  @Test(priority = 4)
  public void testUpdateAddSeatsParkingPassSuccess() {
    List<Product> products = someTicketsAndParkingPasses();
    this.listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    this.listingDTO.getDbListing().setListingType(1L);

    ListingDTO dto = addSeatsTask.call();

    assertNotNull(dto);
  }

  @Test(priority = 5)
  public void testUpdateAddSeatsAlreadyExists() {

    List<Product> products = new ArrayList<>();
    Product prod1 = new Product();
    prod1.setRow("B");
    prod1.setSeat("2");
    prod1.setProductType(ProductType.TICKET);
    prod1.setOperation(Operation.ADD);
    products.add(prod1);

    this.listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);

    try {
      addSeatsTask.call();
    } catch (ListingException e) {
      assertEquals(e.getMessage(), "Cannot add a seat product that already exists (row:B, seat:2)");
    }

  }

  @Test(priority = 6)
  public void testUpdateAddSeatsMissingSeat() {

    List<Product> products = new ArrayList<>();
    Product prod1 = new Product();
    prod1.setProductType(ProductType.TICKET);
    prod1.setOperation(Operation.ADD);
    products.add(prod1);

    this.listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);

    try {
      addSeatsTask.call();
    } catch (ListingException e) {
      assertEquals(e.getMessage(),
          "Update operatios seats resulted in inconsistent rows with some having values and others empty");
    }
  }

  @Test(priority = 7)
  public void testUpdateAddSeatsEmptySeat() {

    List<Product> products = new ArrayList<>();
    Product prod1 = new Product();
    prod1.setProductType(ProductType.TICKET);
    prod1.setOperation(Operation.ADD);
    prod1.setRow("B");
    prod1.setSeat("");
    products.add(prod1);

    List<TicketSeat> ticketSeats = aTicketSeat();

    this.listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    this.listingDTO.getDbListing().setTicketSeats(ticketSeats);

    try {
      addSeatsTask.call();
    } catch (ListingException e) {
      System.out.println(e.getMessage());
      assertEquals(e.getMessage(),
          "Update operatios seats resulted in inconsistent seats with some having values and others empty");
    }

  }

  @Test(priority = 8)
  public void testUpdateAddGASeatError() {

    List<Product> products = new ArrayList<>();
    Product prod1 = new Product();
    prod1.setProductType(ProductType.TICKET);
    prod1.setOperation(Operation.ADD);
    prod1.setRow("B");
    prod1.setSeat("");
    prod1.setExternalId("abcd1234");
    products.add(prod1);

    List<TicketSeat> ticketSeats = someGeneralAdmissionTicketSeats();

    this.listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    this.listingDTO.getDbListing().setTicketSeats(ticketSeats);

    try {
      addSeatsTask.call();
    } catch (ListingException e) {
      System.out.println(e.getMessage());
      assertEquals(e.getMessage(), "Missing row / seat information to add to listing");
    }

  }

  @Test(priority = 9)
  public void testUpdateAddGASeat() {

    List<Product> products = new ArrayList<>();
    Product prod1 = new Product();
    prod1.setProductType(ProductType.TICKET);
    prod1.setOperation(Operation.ADD);
    prod1.setRow("B");
    prod1.setSeat("");
    products.add(prod1);

    List<TicketSeat> ticketSeats = someGeneralAdmissionTicketSeats();

    this.listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    this.listingDTO.getDbListing().setTicketSeats(ticketSeats);

    try {
      addSeatsTask.call();
    } catch (ListingException e) {
      assertEquals(e.getMessage(),
          "Update operatios seats resulted in inconsistent seats with some having values and others empty");
    }

  }


  @Test(priority = 10)
  public void testUpdateAddSeatsPiggyback() {
    List<Product> products = new ArrayList<>();
    Product prod = new Product();
    prod.setRow("C");
    prod.setSeat("2");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.ADD);
    products.add(prod);

    List<TicketSeat> ticketSeats = aTicketSeat();

    this.listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    this.listingDTO.getDbListing().setTicketMedium(3);
    this.listingDTO.getDbListing().setTicketSeats(ticketSeats);
    List<ListingSeatTrait> seatTraits = new ArrayList<>();
    this.listingDTO.getDbListing().setSeatTraits(seatTraits);

    ListingDTO dto = addSeatsTask.call();

    assertNotNull(dto);
    assertEquals(dto.getDbListing().getId(), new Long(1271578716));
  }

  @Test(priority = 11)
  public void testUpdateAddSeatsPiggybackError() {
    List<Product> products = new ArrayList<>();
    Product prod = new Product();
    prod.setRow("C");
    prod.setSeat("2");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.ADD);
    products.add(prod);

    List<TicketSeat> ticketSeats = aTicketSeat();

    this.listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    this.listingDTO.getDbListing().setTicketMedium(3);
    this.listingDTO.getDbListing().setTicketSeats(ticketSeats);
    this.listingDTO.getDbListing().setQuantityRemain(0);
    List<ListingSeatTrait> seatTraits = new ArrayList<>();
    this.listingDTO.getDbListing().setSeatTraits(seatTraits);

     try {
       addSeatsTask.call();
    } catch (ListingException e) {
      assertEquals(e.getMessage(), "Invalid piggyback number of seats. Minimum of 2 is required.");
    }
  }


  @Test(priority = 12)
  public void testUpdateAddSeatsTicketStatus() {
    TicketSeat ts = new TicketSeat();
    ts.setSection("Orchestra Center");
    ts.setRow("B");
    ts.setSeatNumber("2");
    ts.setTixListTypeId(1L); // regular ticket
    ts.setSeatStatusId(2L);
    ts.setTicketId(123456789L);

    List<TicketSeat> ticketSeats = new ArrayList<>();
    ticketSeats.add(ts);

    List<Product> products = new ArrayList<>();
    Product prod = new Product();
    prod.setRow("C");
    prod.setSeat("2");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.ADD);
    products.add(prod);


    this.listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    this.listingDTO.getDbListing().setTicketMedium(3);
    this.listingDTO.getDbListing().setTicketSeats(ticketSeats);
    this.listingDTO.getDbListing().setQuantityRemain(1);
    List<ListingSeatTrait> seatTraits = new ArrayList<>();
    this.listingDTO.getDbListing().setSeatTraits(seatTraits);

      ListingDTO dto = addSeatsTask.call();
      assertNotNull(dto);
      assertEquals(dto.getDbListing().getSection(), "Orchestra Center");
  }

  @Test(priority = 13)
  public void testUpdateAddSeats2ParkingPassesInRequest() {
    List<Product> products = new ArrayList<>();
    Product prod = new Product();
    prod.setRow("LOT");
    prod.setSeat("Parking Lot");
    prod.setProductType(ProductType.PARKING_PASS);
    prod.setOperation(Operation.ADD);
    products.add(prod);

    Product prod1 = new Product();
    prod1.setRow("LOT");
    prod1.setSeat("Parking Lot");
    prod1.setProductType(ProductType.PARKING_PASS);
    prod1.setOperation(Operation.ADD);
    products.add(prod1);

    List<TicketSeat> ticketSeats = aTicketSeat();

    this.listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    this.listingDTO.getDbListing().setTicketMedium(3);
    this.listingDTO.getDbListing().setTicketSeats(ticketSeats);
    this.listingDTO.getDbListing().setQuantityRemain(0);
    List<ListingSeatTrait> seatTraits = new ArrayList<>();
    this.listingDTO.getDbListing().setSeatTraits(seatTraits);

     try {
       addSeatsTask.call();
    } catch (ListingException e) {
      assertEquals(e.getMessage(), "Cannot add multiple parking passes to a listing");
    }
  }

  @Test(priority = 14)
  public void testUpdateAddSeatsParkingPassExistsError() {
    List<Product> products = new ArrayList<>();
    Product prod = new Product();
    prod.setRow("LOT");
    prod.setSeat("Parking Lot");
    prod.setProductType(ProductType.PARKING_PASS);
    prod.setOperation(Operation.ADD);
    products.add(prod);

    List<TicketSeat> ticketSeats = aTicketSeat();

    this.listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    this.listingDTO.getDbListing().setTicketMedium(3);
    this.listingDTO.getDbListing().setTicketSeats(ticketSeats);
    this.listingDTO.getDbListing().setQuantityRemain(0);
    this.listingDTO.getDbListing().setListingType(3L);
    List<ListingSeatTrait> seatTraits = new ArrayList<>();
    this.listingDTO.getDbListing().setSeatTraits(seatTraits);

     try {
       addSeatsTask.call();
    } catch (ListingException e) {
      assertEquals(e.getMessage(), "cannot add a parking pass that already exists");
    }

  }

  @Test(priority = 15)
  public void testUpdateAddSeatsDuplicateSeatError() {
    List<Product> products = new ArrayList<>();
    Product prod = new Product();
    prod.setRow("B");
    prod.setSeat("2");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.ADD);
    products.add(prod);

    List<TicketSeat> ticketSeats = aTicketSeat();
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setRow("B");
    ticketSeat.setSeatNumber("2");
    ticketSeat.setTicketSeatId(1234567L);
    ticketSeat.setSeatStatusId(1L);
    ticketSeats.add(ticketSeat);

    this.listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    this.listingDTO.getDbListing().setTicketMedium(3);
    this.listingDTO.getDbListing().setTicketSeats(ticketSeats);
    this.listingDTO.getDbListing().setQuantityRemain(0);
    this.listingDTO.getDbListing().setListingType(3L);
    List<ListingSeatTrait> seatTraits = new ArrayList<>();
    this.listingDTO.getDbListing().setSeatTraits(seatTraits);
    when(ticketSeatHelper.findTicketSeatEqSeatProduct(Mockito.any(Product.class), Mockito.anyList())).thenReturn(ticketSeat);
    when(ticketSeatHelper.getRowSeat(Mockito.any(Product.class))).thenReturn("B, 2");

    try {
       addSeatsTask.call();
    } catch (ListingException e) {
      assertEquals(e.getMessage(), "Cannot add a seat product that already exists (B, 2)");
    }
  }

  @Test(priority = 15)
  public void testAddFaceValue() {
    List<Product> products = new ArrayList<>();
    Product prod = new Product();
    prod.setRow("B");
    prod.setSeat("1");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.ADD);
    Money faceValue = new Money("100.0", "GBP");
    prod.setFaceValue(faceValue);
    products.add(prod);

    List<TicketSeat> ticketSeats = aTicketSeat();

    this.listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    this.listingDTO.getDbListing().setTicketMedium(3);
    this.listingDTO.getDbListing().setTicketSeats(ticketSeats);
    this.listingDTO.getDbListing().setQuantityRemain(0);
    this.listingDTO.getDbListing().setListingType(3L);

    addSeatsTask.call();

    List<TicketSeat> ticketSeatList = this.listingDTO.getDbListing().getTicketSeats();
    TicketSeat newSeat = ticketSeatList.get(ticketSeatList.size() - 1);
    assertEquals(faceValue, newSeat.getFaceValue());
    assertEquals(Currency.getInstance(faceValue.getCurrency()), newSeat.getCurrency());

  }

  @Test(priority = 15)
  public void testAddFaceValueFromListing() {
    List<Product> products = new ArrayList<>();
    Product prod = new Product();
    prod.setRow("B");
    prod.setSeat("1");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.ADD);
    Money faceValue = new Money("100.0", "GBP");

    products.add(prod);

    List<TicketSeat> ticketSeats = aTicketSeat();

    this.listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    this.listingDTO.getDbListing().setFaceValue(faceValue);
    this.listingDTO.getDbListing().setTicketMedium(3);
    this.listingDTO.getDbListing().setTicketSeats(ticketSeats);
    this.listingDTO.getDbListing().setQuantityRemain(0);
    this.listingDTO.getDbListing().setListingType(3L);

    addSeatsTask.call();

    List<TicketSeat> ticketSeatList = this.listingDTO.getDbListing().getTicketSeats();
    TicketSeat newSeat = ticketSeatList.get(ticketSeatList.size() - 1);
    assertEquals(faceValue, newSeat.getFaceValue());
    assertEquals(Currency.getInstance(faceValue.getCurrency()), newSeat.getCurrency());

  }

  @DataProvider(name = "bannedFullValues")
  public static Object[][] bannedFullValues() {
    return CmaValidatorTestUtil.bannedFullValues();
  }

  @Test(dataProvider = "bannedFullValues")
  public void givenCMAListingWithBannedFullValueInRowField_thenItFails(String bannedValue) {
    listingDTO = aListingDTOWith(bannedValue, "1", CURRENCY_CODE_FOR_CMA, aCMAEvent());
    ReflectionTestUtils.setField(addSeatsTask, "listingDTO", listingDTO);

    try {
      addSeatsTask.call();
      fail(format("An exception should've been thrown before reaching this point - value [%s]", bannedValue));
    } catch (ListingException e) {
      assertEquals(INPUTERROR, e.getType());
      assertEquals(invalidRowWords, e.getErrorCodeEnum());
      assertEquals(e.getMessage(), "Row/Seat contains words that are not allowed");
    }
  }

  @Test(dataProvider = "bannedFullValues")
  public void givenCMAListingWithBannedFullValueInSeatField_thenItFails(String bannedValue) {
    listingDTO = aListingDTOWith("1", bannedValue, CURRENCY_CODE_FOR_CMA, aCMAEvent());
    ReflectionTestUtils.setField(addSeatsTask, "listingDTO", listingDTO);

    try {
      addSeatsTask.call();
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
    listingDTO = aListingDTOWith(bannedValue, "1", CURRENCY_CODE_FOR_CMA, aCMAEvent());
    ReflectionTestUtils.setField(addSeatsTask, "listingDTO", listingDTO);

    try {
      addSeatsTask.call();
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
    listingDTO = aListingDTOWith("1", bannedValue, CURRENCY_CODE_FOR_CMA, aCMAEvent());
    ReflectionTestUtils.setField(addSeatsTask, "listingDTO", listingDTO);

    try {
      addSeatsTask.call();
      fail(format("An exception should've been thrown before reaching this point - value [%s]", bannedValue));
    } catch (ListingException e) {
      assertEquals(INPUTERROR, e.getType());
      assertEquals(invalidRowWords, e.getErrorCodeEnum());
      assertEquals("Row/Seat contains words that are not allowed", e.getCustomMessage());
    }
  }

  private ListingDTO aListingDTO() {
    ListingDTO dto = new ListingDTO(new ListingRequest());
    dto.setSellerInfo(aSeller());
    dto.setDbListing(aListing());
    dto.setHeaderInfo(someHeaderInfo());
    dto.setUpdateListingInfo(updateListingInfoOf(aTicket()));
    return dto;
  }

  private ListingDTO aListingDTOWith(String row, String seat, String currencyCode, Event event) {
    ListingDTO dto = new ListingDTO(new ListingRequest());
    dto.setSellerInfo(aSeller());
    dto.setDbListing(aListing(row, seat, currencyCode, event, aTicketSeat()));
    dto.setHeaderInfo(someHeaderInfo());
    dto.setUpdateListingInfo(updateListingInfoOf(aTicket(row, seat)));
    return dto;
  }

  private UpdateListingInfo updateListingInfoOf(List<Product> products) {
    ProductInfo productInfo = new ProductInfo();
    productInfo.setAddProducts(products);
    UpdateListingInfo info = new UpdateListingInfo();
    info.setProductInfo(productInfo);
    return info;
  }

  private HeaderInfo someHeaderInfo() {
    HeaderInfo headerInfo = new HeaderInfo();
    headerInfo.setSubscriber("Single|V2|Api_UK_sell_buyer20|DefaultApplication");
    headerInfo.setClientIp("10.10.10.10");
    return headerInfo;
  }

  private SellerInfo aSeller() {
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    return sellerInfo;
  }

  private Listing aListing() {
    return aListing("B", "2", "USD", anEvent(), aTicketSeat());
  }

  private Listing aListing(String row, String seat, String currencyCode, Event event, List<TicketSeat> seats) {
    Listing dbListing = new Listing();
    dbListing.setId(1271578716L);
    dbListing.setEventId(event.getId());
    dbListing.setEvent(event);
    dbListing.setSection("Orchestra Center");
    dbListing.setRow(row);
    dbListing.setSeats(seat);
    dbListing.setTicketSeats(seats);
    dbListing.setQuantity(seats.size());
    dbListing.setQuantityRemain(seats.size());
    dbListing.setSplitQuantity(1);
    dbListing.setCurrency(Currency.getInstance(currencyCode));
    dbListing.setDeliveryOption(MANUAL_DELIVERY);
    dbListing.setFulfillmentDeliveryMethods(
        "10,22,5.25,,2017-09-19T19:00:00Z|10,23,5.25,,2017-09-19T19:00:00Z|10,24,5.25,,2017-09-14T19:00:00Z");
    dbListing.setSystemStatus("ACTIVE");
    return dbListing;
  }

  private Event anEvent() {
    Event event = new Event();
    event.setId(EVENT_ID);
    event.setActive(true);
    event.setBookOfBusinessId(1L);
    event.setCountry("US");
    event.setCurrency(Currency.getInstance("USD"));
    event.setDescription("Wicked New York Tickets");
    event.setEventDate(Calendar.getInstance());
    event.setGaIndicator(false);
    event.setGenreParent(700154L);
    event.setGenrePath("174/700188/43992/700154/");
    event.setGeographyParent(198377L);
    event.setGeoPath("6628/201883/6629/198377/");
    event.setIsCreditToTeamAccountSupported(false);
    event.setIsEticketAllowed(false);
    event.setIsIntegrated(true);
    event.setJdkTimeZone(TimeZone.getDefault());
    event.setRowScrubbing(false);
    event.setSectionScrubbing(false);    
    event.setVenueConfigId(1234L);
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

  private List<Product> aTicket() {
    return aTicket("B", "1");
  }

  private List<Product> aTicket(String row, String seat) {
    Product ticket = new Product();
    ticket.setRow(row);
    ticket.setSeat(seat);
    ticket.setProductType(ProductType.TICKET);
    ticket.setInventoryType(InventoryType.SELLITNOW);
    ticket.setOperation(Operation.ADD);

    List<Product> products = new ArrayList<>();
    products.add(ticket);
    return products;
  }

  private List<Product> someTicketsAndParkingPasses() {
    Product parkingPass = new Product();
    parkingPass.setRow("LOT");
    parkingPass.setSeat("Parking Pass");
    parkingPass.setProductType(ProductType.PARKING_PASS);
    parkingPass.setOperation(Operation.ADD);

    Product ticket = new Product();
    ticket.setRow("B");
    ticket.setSeat("3");
    ticket.setProductType(ProductType.TICKET);
    ticket.setOperation(Operation.ADD);

    List<Product> products = new ArrayList<>();
    products.add(parkingPass);
    products.add(ticket);
    return products;
  }

  private List<TicketSeat> aTicketSeat(String row, String seat) {
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setSection("Orchestra Center");
    ticketSeat.setRow(row);
    ticketSeat.setSeatNumber(seat);
    ticketSeat.setTixListTypeId(1L); // regular ticket
    ticketSeat.setSeatStatusId(1L);
    ticketSeat.setTicketId(123456789L);

    List<TicketSeat> seats = new ArrayList<>();
    seats.add(ticketSeat);
    return seats;
  }

  private List<TicketSeat> aTicketSeat() {
    return aTicketSeat("B", "2");
  }

  private List<TicketSeat> someGeneralAdmissionTicketSeats() {
    TicketSeat seat = new TicketSeat();
    seat.setSection("General Admission");
    seat.setRow("N/A");
    seat.setSeatNumber("GA");
    seat.setTixListTypeId(1L); // regular ticket
    seat.setSeatStatusId(1L);
    seat.setTicketId(123456789L);
    seat.setExternalSeatId("abcd1234");
    seat.setGeneralAdmissionInd(true);

    List<TicketSeat> seats = new ArrayList<>();
    seats.add(seat);
    return seats;
  }

  private static VenueConfiguration mockVenueConfiguration() {
    VenueConfiguration venueConfig = new VenueConfiguration();
    venueConfig.setGeneralAdmissionOnly(false);
    venueConfig.setSeatingZones(Collections.EMPTY_LIST);
    return venueConfig;
  }
}


