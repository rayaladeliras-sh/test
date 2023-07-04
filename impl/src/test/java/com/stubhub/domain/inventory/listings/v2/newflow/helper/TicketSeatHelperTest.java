package com.stubhub.domain.inventory.listings.v2.newflow.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.mockito.InjectMocks;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.HeaderInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ProductInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.SellerInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.UpdateListingInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;

public class TicketSeatHelperTest {

  @InjectMocks
  private TicketSeatHelper ticketSeatHelper = new TicketSeatHelper();

  @BeforeMethod
  public void setup() {
    initMocks(this);
  }

  @Test
  public void testValidateSeatsAndRows() {
    ticketSeatHelper.validateSeatsAndRows(getListingDTO());
  }

  @Test
  public void testUpdateAddSeatsInconsistentSeats() {

    List<Product> products = new ArrayList<>();
    Product prod1 = new Product();
    prod1.setProductType(ProductType.TICKET);
    prod1.setOperation(Operation.ADD);
    products.add(prod1);

    TicketSeat ts = new TicketSeat();
    ts.setSection("Orchestra Center");
    ts.setRow("B");
    ts.setSeatNumber("1");
    ts.setTixListTypeId(1L); // regular ticket
    ts.setSeatStatusId(1L);
    ts.setTicketId(123456789l);

    List<TicketSeat> ticketSeats = new ArrayList<>();
    ticketSeats.add(ts);
    TicketSeat ts1 = new TicketSeat();
    ts1.setSection("Orchestra Center");
    ts1.setRow("B");
    ts1.setSeatNumber(null);
    ts1.setTixListTypeId(1L); // regular ticket
    ts1.setSeatStatusId(1L);
    ts1.setTicketId(123456789l);
    ticketSeats.add(ts1);
    ListingDTO listingDTO = getListingDTO();

    listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    listingDTO.getDbListing().setEvent(getEvent());
    listingDTO.getDbListing().getEvent().setIsIntegrated(true);
    listingDTO.getDbListing().setDeliveryOption(2);
    listingDTO.getDbListing().setTicketSeats(ticketSeats);

    try {
      ticketSeatHelper.validateSeatsAndRows(listingDTO);
    } catch (ListingException e) {
      assertEquals(e.getMessage(),
          "Update operation for seats resulted in inconsistent seats with some having values and others empty");
    }

  }

  @Test
  public void testUpdateAddSeatsInconsistentRows() {

    List<Product> products = new ArrayList<>();
    Product prod1 = new Product();
    prod1.setProductType(ProductType.TICKET);
    prod1.setOperation(Operation.ADD);
    products.add(prod1);

    TicketSeat ts = new TicketSeat();
    ts.setSection("Orchestra Center");
    ts.setRow("B");
    ts.setSeatNumber("1");
    ts.setTixListTypeId(1L); // regular ticket
    ts.setSeatStatusId(1L);
    ts.setTicketId(123456789l);

    List<TicketSeat> ticketSeats = new ArrayList<>();
    ticketSeats.add(ts);
    TicketSeat ts1 = new TicketSeat();
    ts1.setSection("Orchestra Center");
    ts1.setRow(null);
    ts1.setSeatNumber("2");
    ts1.setTixListTypeId(1L); // regular ticket
    ts1.setSeatStatusId(1L);
    ts1.setTicketId(123456789l);
    ticketSeats.add(ts1);
    ListingDTO listingDTO = getListingDTO();

    listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    listingDTO.getDbListing().setEvent(getEvent());
    listingDTO.getDbListing().getEvent().setIsIntegrated(true);
    listingDTO.getDbListing().setDeliveryOption(2);
    listingDTO.getDbListing().setTicketSeats(ticketSeats);

    try {
      ticketSeatHelper.validateSeatsAndRows(listingDTO);
    } catch (ListingException e) {
      assertEquals(e.getMessage(),
          "Update operation for seats resulted in inconsistent rows with some having values and others empty");
    }

  }

  @Test
  public void testUpdateAddSeatsPiggyback() {

    List<Product> products = new ArrayList<>();
    Product prod = new Product();
    prod.setRow("C");
    prod.setSeat("2");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.ADD);
    products.add(prod);

    ListingDTO listingDTO = getListingDTO();
    List<TicketSeat> ticketSeats = getTicketSeats();
    TicketSeat ts = new TicketSeat();
    ts.setSection("Orchestra Center");
    ts.setRow("C");
    ts.setSeatNumber("2");
    ts.setTixListTypeId(1L); // regular ticket
    ts.setSeatStatusId(1L);
    ts.setTicketId(123456789l);
    ticketSeats.add(ts);

    listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    listingDTO.getDbListing().setEvent(getEvent());
    listingDTO.getDbListing().setQuantityRemain(2);
    listingDTO.getDbListing().getEvent().setIsIntegrated(true);
    listingDTO.getDbListing().setDeliveryOption(2);
    listingDTO.getDbListing().setTicketMedium(3);
    listingDTO.getDbListing().setTicketSeats(ticketSeats);
    List<ListingSeatTrait> seatTraits = new ArrayList<>();
    listingDTO.getDbListing().setSeatTraits(seatTraits);

    ticketSeatHelper.validateSeatsAndRows(listingDTO);

  }

  @Test
  public void testUpdateAddSeatsPiggybackError() {

    List<Product> products = new ArrayList<>();
    Product prod = new Product();
    prod.setRow("C");
    prod.setSeat("3");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.ADD);
    products.add(prod);

    ListingDTO listingDTO = getListingDTO();
    List<TicketSeat> ticketSeats = getTicketSeats();
    TicketSeat ts = new TicketSeat();
    ts.setSection("Orchestra Center");
    ts.setRow("C");
    ts.setSeatNumber("3");
    ts.setTixListTypeId(1L); // regular ticket
    ts.setSeatStatusId(1L);
    ts.setTicketId(123456789l);
    ticketSeats.add(ts);

    listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    listingDTO.getDbListing().setEvent(getEvent());
    listingDTO.getDbListing().getEvent().setIsIntegrated(true);
    listingDTO.getDbListing().setDeliveryOption(2);
    listingDTO.getDbListing().setTicketMedium(3);
    listingDTO.getDbListing().setTicketSeats(ticketSeats);
    List<ListingSeatTrait> seatTraits = new ArrayList<>();
    listingDTO.getDbListing().setSeatTraits(seatTraits);
    try {
      ticketSeatHelper.validateSeatsAndRows(listingDTO);
    } catch (ListingException e) {
      assertEquals(e.getMessage(), "Invalid piggyback number of seats. Minimum of 2 is required.");;
    }

  }

  @Test
  public void testUpdateAddSeatsPiggybackUnbalanced() {

    List<Product> products = new ArrayList<>();
    Product prod = new Product();
    prod.setRow("C");
    prod.setSeat("3");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.ADD);
    products.add(prod);

    ListingDTO listingDTO = getListingDTO();
    List<TicketSeat> ticketSeats = getTicketSeats();
    TicketSeat ts = new TicketSeat();
    ts.setSection("Orchestra Center");
    ts.setRow("B");
    ts.setSeatNumber("3");
    ts.setTixListTypeId(1L); // regular ticket
    ts.setSeatStatusId(1L);
    ts.setTicketId(123456789l);
    ticketSeats.add(ts);

    TicketSeat ts1 = new TicketSeat();
    ts1.setSection("Orchestra Center");
    ts1.setRow("C");
    ts1.setSeatNumber("2");
    ts1.setTixListTypeId(1L); // regular ticket
    ts1.setSeatStatusId(1L);
    ts1.setTicketId(123456789l);
    ticketSeats.add(ts1);

    listingDTO.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    listingDTO.getDbListing().setEvent(getEvent());
    listingDTO.getDbListing().getEvent().setIsIntegrated(true);
    listingDTO.getDbListing().setDeliveryOption(2);
    listingDTO.getDbListing().setTicketMedium(3);
    listingDTO.getDbListing().setTicketSeats(ticketSeats);
    List<ListingSeatTrait> seatTraits = new ArrayList<>();
    listingDTO.getDbListing().setSeatTraits(seatTraits);
    try {
      ticketSeatHelper.validateSeatsAndRows(listingDTO);
    } catch (ListingException e) {
      assertEquals(e.getMessage(), "Unbalanced piggyback rows seats. Numbers (2, 1)");;
    }

  }

  @Test
  public void testUpdateAddSeatsGeneralAdmission() {

    List<Product> products = new ArrayList<>();
    Product prod = new Product();
    prod.setRow("GA");
    prod.setSeat("General Admission");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.ADD);
    products.add(prod);

    ListingDTO listingDTOGA = getListingDTOGA();
    List<TicketSeat> ticketSeats = new ArrayList<>();
    TicketSeat ts = new TicketSeat();
    ts.setSection("General Admission");
    ts.setRow("N/A");
    // ts.setSeatNumber("General Admission");
    ts.setTixListTypeId(1L); // regular ticket
    ts.setSeatStatusId(1L);
    ts.setTicketId(123456789l);
    ticketSeats.add(ts);

    listingDTOGA.getUpdateListingInfo().getProductInfo().setAddProducts(products);
    listingDTOGA.getDbListing().setEvent(getEvent());
    listingDTOGA.getDbListing().getEvent().setIsIntegrated(true);
    listingDTOGA.getDbListing().setRow("N/A");
    listingDTOGA.getDbListing().setSection("General Admission");
    listingDTOGA.getDbListing().setSeats(null);
    listingDTOGA.getDbListing().setDeliveryOption(2);
    listingDTOGA.getDbListing().setTicketMedium(3);
    listingDTOGA.getDbListing().setTicketSeats(ticketSeats);
    List<ListingSeatTrait> seatTraits = new ArrayList<>();
    listingDTOGA.getDbListing().setSeatTraits(seatTraits);

    ticketSeatHelper.validateSeatsAndRows(listingDTOGA);

  }



  @Test
  public void testIsSameExternalSeatId() {
    
    List<Product> products = new ArrayList<>();
    Product prod = new Product();
    prod.setRow("C");
    prod.setSeat("2");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.ADD);
    prod.setExternalId("TZ1234567");
    products.add(prod);

    TicketSeat ts = new TicketSeat();
    ts.setSection("Orchestra Center");
    ts.setRow("C");
    ts.setSeatNumber("2");
    ts.setTixListTypeId(1L); // regular ticket
    ts.setSeatStatusId(1L);
    ts.setTicketId(123456789l);
    ts.setExternalSeatId("TZ1234567");

    boolean result = ticketSeatHelper.isSameExternalSeatId(prod, ts);

    assertTrue(result);

  }
 
  @Test
  public void testIsRowBlankOrNA() {
    
    TicketSeat ts = new TicketSeat();
    ts.setSection("Orchestra Center");
    ts.setRow(CommonConstants.GA_ROW_DESC);
    ts.setSeatNumber("2");
    ts.setTixListTypeId(1L); // regular ticket
    ts.setSeatStatusId(1L);
    ts.setTicketId(123456789l);
    ts.setExternalSeatId("TZ1234567");

    boolean result = ticketSeatHelper.isRowBlankOrNA(ts);
    
    assertTrue(result);

  }
  
  @Test
  public void testIsSameRow() {
    
    List<Product> products = new ArrayList<>();
    Product prod = new Product();
    prod.setRow("C");
    prod.setSeat("2");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.ADD);
    prod.setExternalId("TZ1234567");
    products.add(prod);

    TicketSeat ts = new TicketSeat();
    ts.setSection("Orchestra Center");
    ts.setRow("C");
    ts.setSeatNumber("2");
    ts.setTixListTypeId(1L); // regular ticket
    ts.setSeatStatusId(1L);
    ts.setTicketId(123456789l);
    ts.setExternalSeatId("TZ1234567");

    boolean result = ticketSeatHelper.isSameRow(prod, ts);

    assertTrue(result);

  }
  
  @Test
  public void testIsSameSeatNumber() {
    
    Product prod = new Product();
    prod.setRow("C");
    prod.setSeat("2");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.ADD);
    prod.setExternalId("TZ1234567");

    TicketSeat ts = new TicketSeat();
    ts.setSection("Orchestra Center");
    ts.setRow("C");
    ts.setSeatNumber("2");
    ts.setTixListTypeId(1L); // regular ticket
    ts.setSeatStatusId(1L);
    ts.setTicketId(123456789l);
    ts.setExternalSeatId("TZ1234567");

    boolean result = ticketSeatHelper.isSameSeatNumber(prod, ts);

    assertTrue(result);

  }
  
  @Test
  public void testgetRowSeat() {
    
    Product prod = new Product();
    prod.setRow("C");
    prod.setSeat("2");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.ADD);
    prod.setExternalId("TZ1234567");
    
    String result = ticketSeatHelper.getRowSeat(prod);
    assertNotNull(result);
    assertEquals(result, "(row:C, seat:2)");
    
  }
  
  @Test
  public void testAddToCSVString() {
    String result = ticketSeatHelper.addToCSVString("1,2","3");
    assertNotNull(result);
    assertEquals(result, "1,2,3");
  }
  
  @Test
  public void testAddToCSVStringUnique() {
    String result = ticketSeatHelper.addToCSVStringUnique("A,B", "C");
    assertNotNull(result);
    assertEquals(result, "A,B,C");
  }
  
  @Test
  public void testFindTicketSeatEqSeatProduct() {
    
    Product prod = new Product();
    prod.setRow("C");
    prod.setSeat("2");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.ADD);
    prod.setExternalId("TZ1234567");

    TicketSeat ts = new TicketSeat();
    ts.setSection("Orchestra Center");
    ts.setRow("C");
    ts.setSeatNumber("2");
    ts.setTixListTypeId(1L); // regular ticket
    ts.setSeatStatusId(1L);
    ts.setTicketId(123456789l);
    ts.setExternalSeatId("TZ1234567");
    List<TicketSeat> ticketSeats = new ArrayList<>();
    ticketSeats.add(ts);
    
    TicketSeat ticketseat = ticketSeatHelper.findTicketSeatEqSeatProduct(prod, ticketSeats);
    assertNotNull(ticketseat);
   }
  
  @Test
  public void testDelFromCSVString() {
    String result = ticketSeatHelper.delFromCSVString("A,B,C", "B");
    assertNotNull(result);
    assertEquals(result, "A,C");
  }

  private ListingDTO getListingDTO() {

    ListingRequest request = new ListingRequest();

    Listing dbListing = new Listing();
    ListingDTO dto = new ListingDTO(request);
    dbListing.setId(1271578716L);
    dbListing.setEventId(1271578799L);
    dbListing.setSection("Orchestra Center");
    dbListing.setRow("B");
    dbListing.setSeats("2");
    dbListing.setQuantity(1);
    dbListing.setSplitQuantity(1);
    dbListing.setQuantityRemain(1);
    dbListing.setCurrency(Currency.getInstance(Locale.US));
    dbListing.setDeliveryOption(2);
    dbListing.setFulfillmentDeliveryMethods(
        "10,22,5.25,,2017-09-19T19:00:00Z|10,23,5.25,,2017-09-19T19:00:00Z|10,24,5.25,,2017-09-14T19:00:00Z");
    dbListing.setSystemStatus("ACTIVE");
    dbListing.setTicketSeats(getTicketSeats());
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    dto.setSellerInfo(sellerInfo);
    dto.setDbListing(dbListing);
    HeaderInfo headerInfo = new HeaderInfo();
    headerInfo.setSubscriber("Single|V2|Api_UK_sell_buyer20|DefaultApplication");
    headerInfo.setClientIp("10.10.10.10");
    dto.setHeaderInfo(headerInfo);
    ProductInfo productInfo = new ProductInfo();
    productInfo.setAddProducts(getProducts());
    UpdateListingInfo info = new UpdateListingInfo();
    info.setProductInfo(productInfo);
    dto.setUpdateListingInfo(info);
    return dto;
  }

  private ListingDTO getListingDTOGA() {

    ListingRequest request = new ListingRequest();

    Listing dbListing = new Listing();
    ListingDTO dto = new ListingDTO(request);
    dbListing.setId(1271578716L);
    dbListing.setEventId(1271578799L);
    dbListing.setSection("General Admission");
    dbListing.setRow("G/A");
    // dbListing.setSeats("2");
    dbListing.setQuantity(1);
    dbListing.setSplitQuantity(1);
    dbListing.setQuantityRemain(1);
    dbListing.setCurrency(Currency.getInstance(Locale.US));
    dbListing.setDeliveryOption(2);
    dbListing.setFulfillmentDeliveryMethods(
        "10,22,5.25,,2017-09-19T19:00:00Z|10,23,5.25,,2017-09-19T19:00:00Z|10,24,5.25,,2017-09-14T19:00:00Z");
    dbListing.setSystemStatus("ACTIVE");
    dbListing.setTicketSeats(getTicketSeats());
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    dto.setSellerInfo(sellerInfo);
    dto.setDbListing(dbListing);
    HeaderInfo headerInfo = new HeaderInfo();
    headerInfo.setSubscriber("Single|V2|Api_UK_sell_buyer20|DefaultApplication");
    headerInfo.setClientIp("10.10.10.10");
    dto.setHeaderInfo(headerInfo);
    ProductInfo productInfo = new ProductInfo();
    productInfo.setAddProducts(getProducts());
    UpdateListingInfo info = new UpdateListingInfo();
    info.setProductInfo(productInfo);
    dto.setUpdateListingInfo(info);
    return dto;
  }

  private List<Product> getProducts() {
    List<Product> products = new ArrayList<>();
    Product prod = new Product();
    prod.setRow("B");
    prod.setSeat("1");
    prod.setProductType(ProductType.TICKET);
    prod.setOperation(Operation.ADD);
    products.add(prod);
    return products;
  }

  private List<TicketSeat> getTicketSeats() {
    TicketSeat ts = new TicketSeat();
    ts.setSection("Orchestra Center");
    ts.setRow("B");
    ts.setSeatNumber("2");
    ts.setTixListTypeId(1L); // regular ticket
    ts.setSeatStatusId(1L);
    ts.setTicketId(123456789l);

    List<TicketSeat> ticketSeats = new ArrayList<>();
    ticketSeats.add(ts);
    return ticketSeats;
  }

  private Event getEvent() {
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


}
