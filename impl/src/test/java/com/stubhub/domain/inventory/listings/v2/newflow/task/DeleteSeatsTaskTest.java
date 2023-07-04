package com.stubhub.domain.inventory.listings.v2.newflow.task;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ProductInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.UpdateListingInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.TicketSeatHelper;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.enums.ProductType;

public class DeleteSeatsTaskTest {

  @Mock
  private ListingDTO listingDTO;

  @Mock
  private TicketSeatHelper ticketSeatHelper;

  @InjectMocks
  DeleteSeatsTask updateDeleteSeatsTask = new DeleteSeatsTask(listingDTO);

  @BeforeMethod
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testExecuteDeleteParkingPassSuccess() {

    UpdateListingInfo updateListingInfo = new UpdateListingInfo();
    ProductInfo productInfo = new ProductInfo();
    List<Product> productList = new ArrayList<>();
    Product product1 = new Product();
    product1.setProductType(ProductType.PARKING_PASS);
    productList.add(product1);
    productInfo.setDeleteProducts(productList);
    updateListingInfo.setProductInfo(productInfo);
    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfo);
    Listing listing = new Listing();
    List<TicketSeat> ticketSeatList = new ArrayList<>();
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setSeatStatusId(1L);
    ticketSeat.setTixListTypeId(2L);
    ticketSeatList.add(ticketSeat);
    listing.setTicketSeats(ticketSeatList);
    List<ListingSeatTrait> listingSeatTraitList = new ArrayList<>();
    ListingSeatTrait listingSeatTrait = new ListingSeatTrait();
    listingSeatTrait.setSupplementSeatTraitId(102L);
    listingSeatTraitList.add(listingSeatTrait);
    listing.setSeatTraits(listingSeatTraitList);
    listing.setQuantityRemain(1);
    listing.setTicketMedium(2);
    listing.setDeliveryOption(2);
    when(listingDTO.getDbListing()).thenReturn(listing);
    when(ticketSeatHelper.isSameExternalSeatId(Mockito.any(Product.class),
        Mockito.any(TicketSeat.class))).thenReturn(true);
    updateDeleteSeatsTask.preExecute();
    updateDeleteSeatsTask.execute();
    Assert.assertEquals(listingDTO.getDbListing().getTicketSeats().get(0).getSeatStatusId(),
        new Long(4));

  }

  @Test
  public void testExecuteDeleteTicketSuccess() {

    UpdateListingInfo updateListingInfo = new UpdateListingInfo();
    ProductInfo productInfo = new ProductInfo();
    List<Product> productList = new ArrayList<>();
    Product product1 = new Product();
    product1.setProductType(ProductType.TICKET);
    product1.setExternalId("123");
    product1.setSeat("1");
    product1.setRow("11");
    productList.add(product1);
    productInfo.setDeleteProducts(productList);
    updateListingInfo.setProductInfo(productInfo);
    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfo);
    Listing listing = new Listing();
    List<TicketSeat> ticketSeatList = new ArrayList<>();
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setSeatStatusId(1L);
    ticketSeat.setTixListTypeId(2L);
    ticketSeat.setExternalSeatId("1234");
    ticketSeat.setSeatNumber("1");
    ticketSeat.setRow("11");
    ticketSeatList.add(ticketSeat);
    listing.setTicketSeats(ticketSeatList);
    List<ListingSeatTrait> listingSeatTraitList = new ArrayList<>();
    ListingSeatTrait listingSeatTrait = new ListingSeatTrait();
    listingSeatTrait.setSupplementSeatTraitId(102L);
    listingSeatTraitList.add(listingSeatTrait);
    listing.setSeatTraits(listingSeatTraitList);
    listing.setQuantityRemain(2);
    listing.setQuantity(2);
    listing.setSplitOption((short) 0);
    listing.setTicketMedium(4);
    listing.setDeliveryOption(1);
    listing.setSystemStatus("PENDING PDF REVIEW");
    listing.setId(999L);
    when(listingDTO.getDbListing()).thenReturn(listing);
    when(ticketSeatHelper.isSameExternalSeatId(Mockito.any(Product.class),
        Mockito.any(TicketSeat.class))).thenReturn(true);
    updateDeleteSeatsTask.preExecute();
    updateDeleteSeatsTask.execute();
    Assert.assertEquals(listingDTO.getDbListing().getQuantityRemain(), new Integer(1));
    Assert.assertEquals(listingDTO.getDbListing().getSystemStatus(), "PENDING LOCK");

  }

/*  @Test
  public void testExecuteDeleteTicketFailOnWhenRemainQuantityIsZero() {

    UpdateListingInfo updateListingInfo = new UpdateListingInfo();
    ProductInfo productInfo = new ProductInfo();
    List<Product> productList = new ArrayList<>();
    Product product1 = new Product();
    product1.setProductType(ProductType.TICKET);
    product1.setExternalId("123");
    product1.setSeat("1");
    product1.setRow("11");
    productList.add(product1);
    productInfo.setDeleteProducts(productList);
    updateListingInfo.setProductInfo(productInfo);


    Listing listing = new Listing();
    List<TicketSeat> ticketSeatList = new ArrayList<>();
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setSeatStatusId(1L);
    ticketSeat.setTixListTypeId(2L);
    ticketSeat.setExternalSeatId("1234");
    ticketSeat.setSeatNumber("1");
    ticketSeat.setRow("11");
    ticketSeatList.add(ticketSeat);
    listing.setTicketSeats(ticketSeatList);
    List<ListingSeatTrait> listingSeatTraitList = new ArrayList<>();
    ListingSeatTrait listingSeatTrait = new ListingSeatTrait();
    listingSeatTrait.setSupplementSeatTraitId(102L);
    listingSeatTraitList.add(listingSeatTrait);
    listing.setSeatTraits(listingSeatTraitList);
    listing.setQuantityRemain(1);
    listing.setQuantity(1);
    listing.setSplitOption((short) 0);
    listing.setTicketMedium(4);
    listing.setDeliveryOption(1);
    listing.setSystemStatus("PENDING PDF REVIEW");
    listing.setId(999L);

    HeaderInfo headerInfo = new HeaderInfo();
    headerInfo.setSubscriber("subscriber");
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    when(listingDTO.getDbListing()).thenReturn(listing);
    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfo);
    when(ticketSeatHelper.isSameExternalSeatId(Mockito.any(Product.class),
        Mockito.any(TicketSeat.class))).thenReturn(true);
    updateDeleteSeatsTask.preExecute();
    try {
      updateDeleteSeatsTask.execute();
      Assert.fail("Should not reach here");
    } catch (ListingException e) {
      Assert.assertNotNull(e);
      Assert.assertTrue(e.getCustomMessage()
          .contains("Please DELETE the listing instead of deleting ALL the individual seats"));
    }
  }
*/
  @Test
  public void testExecuteDeleteTicketFail() {

    UpdateListingInfo updateListingInfo = new UpdateListingInfo();
    ProductInfo productInfo = new ProductInfo();
    List<Product> productList = new ArrayList<>();
    Product product1 = new Product();
    product1.setProductType(ProductType.TICKET);

    productList.add(product1);
    productInfo.setDeleteProducts(productList);
    updateListingInfo.setProductInfo(productInfo);
    when(listingDTO.getUpdateListingInfo()).thenReturn(updateListingInfo);
    Listing listing = new Listing();
    List<TicketSeat> ticketSeatList = new ArrayList<>();
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setSeatStatusId(1L);
    ticketSeat.setTixListTypeId(2L);
    ticketSeatList.add(ticketSeat);
    listing.setTicketSeats(ticketSeatList);
    when(listingDTO.getDbListing()).thenReturn(listing);
    try {
      updateDeleteSeatsTask.execute();
      Assert.fail("Should Not reach here");
    } catch (ListingException e) {
      Assert.assertNotNull(e);
      Assert.assertTrue(e.getCustomMessage().contains("Cannot locate seat product to delete"));
    }
  }

  @Test
  public void testPostExecuteSuccess() {
    ReflectionTestUtils.setField(updateDeleteSeatsTask, "isPiggyBackRows", true);
    Listing listing = new Listing();
    listing.setIsPiggyBack(false);
    List<ListingSeatTrait> listingSeatTraitList = new ArrayList<>();
    ListingSeatTrait listingSeatTrait = new ListingSeatTrait();
    listingSeatTrait.setSupplementSeatTraitId(501L);
    listingSeatTraitList.add(listingSeatTrait);
    listing.setSeatTraits(listingSeatTraitList);
    when(listingDTO.getDbListing()).thenReturn(listing);
    updateDeleteSeatsTask.postExecute();
    Assert.assertTrue(listingDTO.getDbListing().getSeatTraits().get(0).isMarkForDelete());
  }
}
