package com.stubhub.domain.inventory.listings.v2.newflow.task;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.TicketTrait;
import com.stubhub.domain.inventory.v2.enums.Operation;

public class UpdateTicketTraitsTaskTest {

  @Mock
  private ListingDTO listingDTO;

  @Mock
  private ListingSeatTraitMgr listingSeatTraitMgr;

  @InjectMocks
  private UpdateTicketTraitsTask updateTicketTraitsTask;

  @BeforeMethod
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testPreExecuteSuccess(){
    Listing listing=new Listing();
    when(listingDTO.getListingRequest()).thenReturn(new ListingRequest());
    when(listingDTO.getDbListing()).thenReturn(listing);
    updateTicketTraitsTask=new UpdateTicketTraitsTask(listingDTO);
    updateTicketTraitsTask.preExecute();
    Assert.assertNotNull(ReflectionTestUtils.getField(updateTicketTraitsTask,"listingTraitsfromDB"));
  }
  @Test
  public void testExecuteAddParkingPassSuccessWithPiggyback(){
    Listing listing=new Listing();
    List<ListingSeatTrait> listingSeatTraits=new ArrayList<>();
    ListingSeatTrait st = new ListingSeatTrait();
    st.setSupplementSeatTraitId(102L);
    listingSeatTraits.add(st);
    ListingSeatTrait st2 = new ListingSeatTrait();
    st2.setSupplementSeatTraitId(501L);
    listingSeatTraits.add(st2);
    listing.setSeatTraits(listingSeatTraits);
    Event event=new Event();
    List<Long> IDs=new ArrayList<>();
    IDs.add(102L);
    IDs.add(501L);
    event.setTicketTraitId(IDs);
    List<com.stubhub.domain.catalog.events.intf.TicketTrait> ticketTraits=new ArrayList<>();
    com.stubhub.domain.catalog.events.intf.TicketTrait ticketTrait=new com.stubhub.domain.catalog.events.intf.TicketTrait();
    ticketTrait.setId(102L);
    ticketTrait.setName("TraitName");
    ticketTrait.setType("TraitType");
    ticketTraits.add(ticketTrait);
    event.setTicketTrait(ticketTraits);
    listing.setEvent(event);
    List<TicketSeat> ticketSeatList=new ArrayList<>();
    listing.setTicketSeats(ticketSeatList);
    listing.setTicketMedium(2);
    listing.setListingSource(8);
    when(listingDTO.getDbListing()).thenReturn(listing);
    ListingRequest listingRequest=new ListingRequest();
    List<TicketTrait> ticketTraits2=new ArrayList<>();
    TicketTrait ticketTrait2=new TicketTrait();
    ticketTrait2.setName("TraitName");
    ticketTrait2.setId("102");
    ticketTrait2.setType("TraitType");
    ticketTraits2.add(ticketTrait2);
    listingRequest.setTicketTraits(ticketTraits2);
    when(listingDTO.getListingRequest()).thenReturn(listingRequest);
    updateTicketTraitsTask=new UpdateTicketTraitsTask(listingDTO);
    updateTicketTraitsTask.preExecute();
    updateTicketTraitsTask.execute();
    updateTicketTraitsTask.postExecute();
    Assert.assertNotNull(((Listing)ReflectionTestUtils.getField(updateTicketTraitsTask,"dbListing")).getTicketSeats());
    Assert.assertEquals(((Listing)ReflectionTestUtils.getField(updateTicketTraitsTask,"dbListing")).getTicketSeats().size(),1);
    Assert.assertEquals(((Listing)ReflectionTestUtils.getField(updateTicketTraitsTask,"dbListing")).getTicketSeats().get(0).getSeatNumber(),"Parking Pass");
  }

  @Test
  public void testExecuteAddParkingPassFailWithBadRequest(){
    Listing listing=new Listing();
    List<ListingSeatTrait> listingSeatTraits=new ArrayList<>();
    ListingSeatTrait st = new ListingSeatTrait();
    st.setSupplementSeatTraitId(102L);
    listingSeatTraits.add(st);
    listing.setSeatTraits(listingSeatTraits);
    Event event=new Event();
    List<Long> IDs=new ArrayList<>();
    IDs.add(102L);
    event.setTicketTraitId(IDs);
    List<com.stubhub.domain.catalog.events.intf.TicketTrait> ticketTraits=new ArrayList<>();
    com.stubhub.domain.catalog.events.intf.TicketTrait ticketTrait=new com.stubhub.domain.catalog.events.intf.TicketTrait();
    ticketTrait.setId(102L);
    ticketTrait.setName("TraitName");
    ticketTrait.setType("TraitType");
    ticketTraits.add(ticketTrait);
    event.setTicketTrait(ticketTraits);
    listing.setEvent(event);
    List<TicketSeat> ticketSeatList=new ArrayList<>();
    listing.setTicketSeats(ticketSeatList);
    listing.setTicketMedium(2);
    when(listingDTO.getDbListing()).thenReturn(listing);
    ListingRequest listingRequest=new ListingRequest();
    List<TicketTrait> ticketTraits2=new ArrayList<>();
    TicketTrait ticketTrait2=new TicketTrait();
    ticketTrait2.setType("TraitType");
    ticketTraits2.add(ticketTrait2);
    listingRequest.setTicketTraits(ticketTraits2);
    when(listingDTO.getListingRequest()).thenReturn(listingRequest);
    updateTicketTraitsTask=new UpdateTicketTraitsTask(listingDTO);
    updateTicketTraitsTask.preExecute();
    updateTicketTraitsTask.execute();
    updateTicketTraitsTask.postExecute();
    Assert.assertNotNull(((Listing)ReflectionTestUtils.getField(updateTicketTraitsTask,"dbListing")).getTicketSeats());
    Assert.assertEquals(((Listing)ReflectionTestUtils.getField(updateTicketTraitsTask,"dbListing")).getTicketSeats().size(),0);
  }

  @Test
  public void testExecuteAddNonExistTraitSuccess(){
    Listing listing=new Listing();
    List<ListingSeatTrait> listingSeatTraits=new ArrayList<>();
    listing.setSeatTraits(listingSeatTraits);
    Event event=new Event();
    List<Long> IDs=new ArrayList<>();
    IDs.add(199L);
    event.setTicketTraitId(IDs);
    listing.setEvent(event);
    List<TicketSeat> ticketSeatList=new ArrayList<>();
    listing.setTicketSeats(ticketSeatList);
    when(listingDTO.getDbListing()).thenReturn(listing);
    ListingRequest listingRequest=new ListingRequest();
    List<TicketTrait> ticketTraits=new ArrayList<>();
    TicketTrait ticketTrait=new TicketTrait();
    ticketTrait.setName("TraitName");
    ticketTrait.setId("199");
    ticketTrait.setType("TraitType");
    ticketTraits.add(ticketTrait);
    listingRequest.setTicketTraits(ticketTraits);
    when(listingDTO.getListingRequest()).thenReturn(listingRequest);
    updateTicketTraitsTask=new UpdateTicketTraitsTask(listingDTO);
    updateTicketTraitsTask.preExecute();
    updateTicketTraitsTask.execute();
    Assert.assertNotNull(((Listing)ReflectionTestUtils.getField(updateTicketTraitsTask,"dbListing")).getSeatTraits());
    Assert.assertEquals(((Listing)ReflectionTestUtils.getField(updateTicketTraitsTask,"dbListing")).getSeatTraits().size(),1);
    Assert.assertEquals(((Listing)ReflectionTestUtils.getField(updateTicketTraitsTask,"dbListing")).getSeatTraits().get(0).getSupplementSeatTraitId(),new Long(199));
  }

  @Test
  public void testExecuteDeleteParkingPassSuccess(){
    Listing listing=new Listing();
    List<ListingSeatTrait> listingSeatTraits=new ArrayList<>();
    ListingSeatTrait st = new ListingSeatTrait();
    st.setSupplementSeatTraitId(102L);
    listingSeatTraits.add(st);
    listing.setSeatTraits(listingSeatTraits);
    Event event=new Event();
    List<Long> IDs=new ArrayList<>();
    IDs.add(102L);
    event.setTicketTraitId(IDs);
    List<com.stubhub.domain.catalog.events.intf.TicketTrait> ticketTraits=new ArrayList<>();
    com.stubhub.domain.catalog.events.intf.TicketTrait ticketTrait=new com.stubhub.domain.catalog.events.intf.TicketTrait();
    ticketTrait.setId(102L);
    ticketTrait.setName("TraitName");
    ticketTrait.setType("TraitType");
    ticketTraits.add(ticketTrait);
    event.setTicketTrait(ticketTraits);
    listing.setEvent(event);
    List<TicketSeat> ticketSeatList=new ArrayList<>();
    TicketSeat ticketSeat=new TicketSeat();
    ticketSeat.setSeatStatusId(1L);
    ticketSeat.setTixListTypeId(2L);
    ticketSeatList.add(ticketSeat);
    listing.setTicketSeats(ticketSeatList);
    listing.setDeliveryOption(2);
    listing.setTicketMedium(3);
    when(listingDTO.getDbListing()).thenReturn(listing);
    ListingRequest listingRequest=new ListingRequest();
    List<TicketTrait> ticketTraits2=new ArrayList<>();
    TicketTrait ticketTrait2=new TicketTrait();
    ticketTrait2.setName("TraitName");
    ticketTrait2.setId("103");
    ticketTrait2.setType("TraitType");
    ticketTrait2.setOperation(Operation.DELETE);
    ticketTraits2.add(ticketTrait2);
    listingRequest.setTicketTraits(ticketTraits2);
    when(listingDTO.getListingRequest()).thenReturn(listingRequest);
    updateTicketTraitsTask=new UpdateTicketTraitsTask(listingDTO);
    updateTicketTraitsTask.preExecute();
    updateTicketTraitsTask.execute();
    Assert.assertNotNull(((Listing)ReflectionTestUtils.getField(updateTicketTraitsTask,"dbListing")).getTicketSeats());
    Assert.assertEquals(((Listing)ReflectionTestUtils.getField(updateTicketTraitsTask,"dbListing")).getTicketSeats().size(),1);
    Assert.assertEquals(((Listing)ReflectionTestUtils.getField(updateTicketTraitsTask,"dbListing")).getListingType(),new Long(1));
  }

  @Test
  public void testExecuteAddParkingPassFailOnEventNotSupportPakingPass(){
    MockitoAnnotations.initMocks(this);
    Listing listing=new Listing();
    List<ListingSeatTrait> listingSeatTraits=new ArrayList<>();
    ListingSeatTrait st = new ListingSeatTrait();
    st.setSupplementSeatTraitId(102L);
    listingSeatTraits.add(st);
    listing.setSeatTraits(listingSeatTraits);
    Event event=new Event();
    List<Long> IDs=new ArrayList<>();
    IDs.add(102L);
    event.setTicketTraitId(IDs);
    event.setIsIntegrated(true);
    when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(false);
    List<com.stubhub.domain.catalog.events.intf.TicketTrait> ticketTraits=new ArrayList<>();
    com.stubhub.domain.catalog.events.intf.TicketTrait ticketTrait=new com.stubhub.domain.catalog.events.intf.TicketTrait();
    ticketTrait.setId(102L);
    ticketTrait.setName("TraitName");
    ticketTrait.setType("TraitType");
    ticketTraits.add(ticketTrait);
    event.setTicketTrait(ticketTraits);
    listing.setEvent(event);
    List<TicketSeat> ticketSeatList=new ArrayList<>();
    TicketSeat ticketSeat=new TicketSeat();
    ticketSeat.setSeatStatusId(1L);
    ticketSeat.setTixListTypeId(2L);
    ticketSeatList.add(ticketSeat);
    listing.setTicketSeats(ticketSeatList);
    when(listingDTO.getDbListing()).thenReturn(listing);
    ListingRequest listingRequest=new ListingRequest();
    List<TicketTrait> ticketTraits2=new ArrayList<>();
    TicketTrait ticketTrait2=new TicketTrait();
    ticketTrait2.setName("TraitName");
    ticketTrait2.setId("102");
    ticketTrait2.setType("TraitType");
    ticketTrait2.setOperation(Operation.ADD);
    ticketTraits2.add(ticketTrait2);
    listingRequest.setTicketTraits(ticketTraits2);
    when(listingDTO.getListingRequest()).thenReturn(listingRequest);
    updateTicketTraitsTask=new UpdateTicketTraitsTask(listingDTO);
    MockitoAnnotations.initMocks(this);
    updateTicketTraitsTask.preExecute();
    try{
      updateTicketTraitsTask.execute();
      Assert.fail("Should Not reach here");
    }catch (ListingException e){
      Assert.assertNotNull(e);
      Assert.assertEquals(e.getCustomMessage(),"Parking Pass is not supported");
    }

  }
}
