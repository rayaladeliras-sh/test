package com.stubhub.domain.inventory.listings.v2.helper;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.entity.SplitOption;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.FulfillmentMethod;
import com.stubhub.domain.inventory.listings.v2.entity.VenueConfigSectionOrZone;
import com.stubhub.domain.inventory.listings.v2.util.ListingSeatTraitsHelper;
import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.domain.inventory.listings.v2.util.SeatProductsContext;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.TicketTrait;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

public class ListingSeatTraitsHelperTest extends SHInventoryTest
{
	ListingSeatTraitMgr listingSeatTraitMgr;
	TicketSeatMgr ticketSeatMgr;
	ListingSeatTraitsHelper helper;
	private SvcLocator svcLocator;
	private WebClient webClient;
	private ObjectMapper objectMapper;
	GlobalRegistryServiceHelper globalRegServiceHelper;
	MasterStubhubPropertiesWrapper masterStubhubProperties=null;
	
	@BeforeTest
	public void setup() throws Exception
	{	
		ticketSeatMgr = (TicketSeatMgr)mockClass (TicketSeatMgr.class, null, null);
		helper = new ListingSeatTraitsHelper(){
			protected String getProperty(String propertyName, String defaultValue) {
				if ("get_venue_config_metadata_api".equals(propertyName))
					return "https://api-int.${default_domain}/catalog/venues/v3/venueConfigMetadata?venueConfigId={venueConfigId}&sectionOrZoneName={sectionOrZoneName}&rows={rowDesc},{piggyBackRowDesc}&isSectionStemmingRequired=true&isExactMatch=true";
				return "";
			}
		};
		setBeanProperty (helper, "ticketSeatMgr", ticketSeatMgr);
		listingSeatTraitMgr = Mockito.mock(ListingSeatTraitMgr.class);
		ReflectionTestUtils.setField(helper, "listingSeatTraitMgr", listingSeatTraitMgr);
		
		globalRegServiceHelper = Mockito.mock(GlobalRegistryServiceHelper.class);
		ReflectionTestUtils.setField(helper, "globalRegServiceHelper", globalRegServiceHelper);
		
		svcLocator = Mockito.mock(SvcLocator.class);
		webClient = Mockito.mock(WebClient.class);
		objectMapper = Mockito.mock(ObjectMapper.class);
		ReflectionTestUtils.setField(helper, "svcLocator", svcLocator);
		ReflectionTestUtils.setField(helper, "objectMapper", objectMapper);
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void seatTraitsManipulationAddDelete() throws Exception
	{
		List<Long> ids = new ArrayList<Long>();
		ids.add(101l);
		ids.add(102l);
		
		when(listingSeatTraitMgr.getSeatTraitsFromComments(anyLong(), anyString())).thenReturn(ids);
		when(listingSeatTraitMgr.findSeatTraits(anyLong())).thenReturn(null);
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(true);
		
		// note: this is db listing
		Listing dblisting = new Listing();
		dblisting.setId(1000L);
		dblisting.setEventId(1000l);
		Event event = getEvent( new String[] {"Aisle", "Parking", "other trait"}, new Long[] {101l, 102l, 4990l} );
		event.setIsIntegrated(true);
		dblisting.setEvent(event);
		dblisting.setQuantity(2);
		dblisting.setListingSource(7);
		dblisting.setSplitQuantity(2);
		dblisting.setSplitOption((short) 0);
		dblisting.setIsETicket(true);
		dblisting.setDeliveryOption(DeliveryOption.MANUAL.getValue());
		
		// ADD traits case
		ListingRequest ulistReq = new ListingRequest();
		List<TicketTrait> traits = new ArrayList<TicketTrait>();
		traits.add( getTicketTrait ("101", null, Operation.ADD) );
		traits.add( getTicketTrait ("102", null, Operation.ADD) );
		traits.add( getTicketTrait ("4990", null, Operation.ADD) );
		ulistReq.setTicketTraits( traits );
		
		SeatProductsContext prodCtx = new  SeatProductsContext(dblisting, ulistReq, ticketSeatMgr, listingSeatTraitMgr);
		
		helper.processSeatTraits( prodCtx );
		
		// make sure 2 traits added
		dblisting = prodCtx.getCurrentListing();
		Assert.assertTrue (dblisting.getSeatTraits()!=null && dblisting.getSeatTraits().size()==3, "Failed to add 3 seat traits" );

		// DELETE Traits case
		traits.clear();
		traits.add( getTicketTrait ("4990", null, Operation.DELETE) );	
		traits.add( getTicketTrait ("101", null, Operation.DELETE) );
		ulistReq = new ListingRequest();
		ulistReq.setTicketTraits( traits );

		prodCtx = new  SeatProductsContext(dblisting, ulistReq, ticketSeatMgr, listingSeatTraitMgr);
		helper.processSeatTraits( prodCtx );
		dblisting = prodCtx.getCurrentListing();
		
		int delCount = 0;
		if (dblisting.getSeatTraits() != null ) {
			for (ListingSeatTrait st : dblisting.getSeatTraits() ) {
				if ( st.isMarkForDelete() ) delCount++;
			}
		}
		Assert.assertTrue ( delCount == 2, "Failed to delete 2 Ticket Seat Traits" );
	}	
	
	
	@Test
	public void createListingSeatTrait() throws Exception
	{
		List<Long> ids = new ArrayList<Long>();
		ids.add(101l);
		ids.add(102l);
		
		when(listingSeatTraitMgr.getSeatTraitsFromComments(anyLong(), anyString())).thenReturn(ids);
		when(listingSeatTraitMgr.findSeatTraits(anyLong())).thenReturn(null);
		
		// note: this is db listing
		Listing dblisting = new Listing();
		dblisting.setId(1000L);
		dblisting.setEventId(1000l);
		Event event = getEvent( new String[] {"Aisle", "Parking"}, new Long[] {101l, 102l} );
		event.setIsIntegrated(true);
		dblisting.setEvent(event);
		dblisting.setQuantity(2);
		dblisting.setListingSource(7);
		dblisting.setSplitQuantity(2);
		dblisting.setSplitOption((short) 0);
		dblisting.setIsETicket(true);
		dblisting.setDeliveryOption(DeliveryOption.MANUAL.getValue());
		
		ListingRequest ulistReq = new ListingRequest();
		List<TicketTrait> traits = new ArrayList<TicketTrait>();
		traits.add( getTicketTrait (null, "Aisle", Operation.ADD) );
		traits.add( getTicketTrait (null, "Parking", Operation.ADD) );
		ulistReq.setTicketTraits( traits );
		
		SeatProductsContext prodCtx = new  SeatProductsContext(dblisting, ulistReq, ticketSeatMgr, listingSeatTraitMgr);
		
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(true);
		
		helper.processSeatTraits( prodCtx );
		Assert.assertTrue (prodCtx.getCurrentListing().getSeatTraits()!=null && prodCtx.getCurrentListing().getSeatTraits().size()==2);
	}
	
	@Test
	public void createListingSeatTraitNotSupported() throws Exception
	{
		List<Long> ids = new ArrayList<Long>();
		ids.add(101l);
		ids.add(102l);
		
		when(listingSeatTraitMgr.getSeatTraitsFromComments(anyLong(), anyString())).thenReturn(ids);
		when(listingSeatTraitMgr.findSeatTraits(anyLong())).thenReturn(null);
		
		// note: this is db listing
		Listing dblisting = new Listing();
		dblisting.setId(1000L);
		dblisting.setEventId(1000l);
		Event event = getEvent( new String[] {"Aisle"}, new Long[] {101l} );
		event.setIsIntegrated(true);
		dblisting.setEvent(event);
		dblisting.setQuantity(2);
		dblisting.setListingSource(7);
		dblisting.setSplitQuantity(2);
		dblisting.setSplitOption((short) 0);
		dblisting.setIsETicket(true);
		
		ListingRequest ulistReq = new ListingRequest();
		List<TicketTrait> traits = new ArrayList<TicketTrait>();
		traits.add( getTicketTrait (null, "Aisle", Operation.ADD) );
		traits.add( getTicketTrait (null, "Parking", Operation.ADD) );
		ulistReq.setTicketTraits( traits );
		
		SeatProductsContext prodCtx = new  SeatProductsContext(dblisting, ulistReq, ticketSeatMgr, listingSeatTraitMgr);
		
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(true);
		
		helper.processSeatTraits( prodCtx );
		Assert.assertTrue (prodCtx.getCurrentListing().getSeatTraits()!=null && prodCtx.getCurrentListing().getSeatTraits().size()==1);
	}	
	
	@Test
	public void createListingSplitAndTraitsComboSupported() throws Exception
	{
		List<Long> ids = new ArrayList<Long>();
		ids.add(101l);
		ids.add(102l);
		
		when(listingSeatTraitMgr.getSeatTraitsFromComments(anyLong(), anyString())).thenReturn(ids);
		when(listingSeatTraitMgr.findSeatTraits(anyLong())).thenReturn(null);
		
		// note: this is db listing
		Listing dblisting = new Listing();
		dblisting.setId(1000L);
		dblisting.setEventId(1000l);
		Event event = getEvent( new String[] {"Aisle"}, new Long[] {101l} );
		event.setIsIntegrated(true);
		dblisting.setEvent(event);
		dblisting.setQuantity(2);
		dblisting.setListingSource(7);
		dblisting.setSplitQuantity(1);
		dblisting.setSplitOption((short) 2);
		dblisting.setIsETicket(true);
		
		ListingRequest ulistReq = new ListingRequest();
		List<TicketTrait> traits = new ArrayList<TicketTrait>();
		traits.add( getTicketTrait (null, "Aisle", Operation.ADD) );
		traits.add( getTicketTrait (null, "Parking", Operation.ADD) );
		ulistReq.setSplitOption( SplitOption.NOSINGLES );
		ulistReq.setSplitQuantity(1);
		ulistReq.setTicketTraits( traits );
		
		SeatProductsContext prodCtx = new  SeatProductsContext(dblisting, ulistReq, ticketSeatMgr, listingSeatTraitMgr);
		
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(true);
		
		try {
			helper.processSeatTraits( prodCtx );
		}
		catch ( ListingBusinessException ex ) {
			Assert.fail("Passing splitOption != NONE should not fail because combined Ticket Traits passed is allowed!" );
		}
		
		// no split option passed
		ulistReq.setSplitOption( null );
		ulistReq.setSplitQuantity( null );
		try {
			helper.processSeatTraits( prodCtx );
			Assert.assertTrue( prodCtx.getCurrentListing().getSplitOption() == 0, 
					"Should set SplitOption == NONE as default because no split option is passed" );
		}
		catch ( ListingBusinessException ex ) {
			Assert.fail("Passing splitOption == null should NOT fail because combined Ticket Traits passed should default to SplitOption == NONE" );
		}
	}	
		
	@Test (expectedExceptions = {ListingBusinessException.class})
	public void createListingSeatTraitNoParkingSupport() throws Exception
	{
		List<Long> ids = new ArrayList<Long>();
		ids.add(101l);
		ids.add(102l);
		
		when(listingSeatTraitMgr.getSeatTraitsFromComments(anyLong(), anyString())).thenReturn(ids);
		when(listingSeatTraitMgr.findSeatTraits(anyLong())).thenReturn(null);
		
		// note: this is db listing
		Listing dblisting = new Listing();
		dblisting.setId(1000L);
		dblisting.setEventId(1000l);
		Event event = getEvent( new String[] {"Aisle", "Parking"}, new Long[] {101l, 102l} );
		event.setIsIntegrated(true);
		dblisting.setEvent(event);
		dblisting.setQuantity(2);
		dblisting.setListingSource(7);
		dblisting.setSplitQuantity(2);
		dblisting.setSplitOption((short) 0);
		dblisting.setIsETicket(true);
		
		ListingRequest ulistReq = new ListingRequest();
		List<TicketTrait> traits = new ArrayList<TicketTrait>();
		traits.add( getTicketTrait (null, "Aisle", Operation.ADD) );
		traits.add( getTicketTrait (null, "Parking", Operation.ADD) );
		ulistReq.setTicketTraits( traits );
		
		SeatProductsContext prodCtx = new  SeatProductsContext(dblisting, ulistReq, ticketSeatMgr, listingSeatTraitMgr);
		
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(false);
		
		helper.processSeatTraits( prodCtx );
		Assert.assertTrue (prodCtx.getCurrentListing().getSeatTraits()!=null && prodCtx.getCurrentListing().getSeatTraits().size()==1);
	}
	
	@Test
	public void seatTraitParkingPassAndTraits() throws Exception
	{
		List<Long> ids = new ArrayList<Long>();
		ids.add(101l);
		ids.add(102l);
		
		when(listingSeatTraitMgr.getSeatTraitsFromComments(anyLong(), anyString())).thenReturn(ids);
		when(listingSeatTraitMgr.findSeatTraits(anyLong())).thenReturn(null);
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(true);
		// 2 seats already exit in DB
		when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(getTicketSeats(1000L, "Section-100", "R1", 2));
		
		// note: this is db listing
		Listing dblisting = new Listing();
		dblisting.setId(1000L);
		dblisting.setEventId(1000l);
		Event event = getEvent( new String[] {"Aisle", "Parking"}, new Long[] {101l, 102l} );
		event.setIsIntegrated(true);
		dblisting.setEvent(event);
		dblisting.setQuantity(2);
		dblisting.setListingSource(7);
		dblisting.setSplitQuantity(2);
		dblisting.setSplitOption((short) 0);
		dblisting.setIsETicket(true);
		dblisting.setDeliveryOption(DeliveryOption.MANUAL.getValue());
		dblisting.setTicketMedium(3);

		// ADD parking pass ticket trait
		ListingRequest ulistReq = new ListingRequest();
		List<TicketTrait> traits = new ArrayList<TicketTrait>();
		traits.add( getTicketTrait (null, "Parking", Operation.ADD) );
		ulistReq.setTicketTraits( traits );
		SeatProductsContext prodCtx = new  SeatProductsContext(dblisting, ulistReq, ticketSeatMgr, listingSeatTraitMgr);
		helper.processSeatTraits( prodCtx );
		
		// Check if parking pass added (and parking ticket)
		dblisting = prodCtx.getCurrentListing();
		Assert.assertTrue ( dblisting.getTicketSeats()!=null && dblisting.getTicketSeats().size()==3, 
				"Did not add parking pass seat after adding parking ticket trait");
		
		// DELETE parking pass ticket test 
		traits.clear();
		traits.add( getTicketTrait (null, "Parking", Operation.DELETE) );
		ulistReq.setTicketTraits( traits );
		prodCtx = new  SeatProductsContext(dblisting, ulistReq, ticketSeatMgr, listingSeatTraitMgr);
		helper.processSeatTraits( prodCtx );
		dblisting = prodCtx.getCurrentListing();
		
		int delCount = 0;
		if (dblisting.getTicketSeats() != null ) {
			for (TicketSeat ts : dblisting.getTicketSeats() ) {
				if ( ts.getSeatStatusId() == 4l ) delCount++;
			}
		}
		Assert.assertTrue ( delCount == 1, "Did not delete parking pass seat after deleting parking ticket trait");
	}	
	
	@Test
	public void updateListingSeatTrait_markForDeleteByNames() throws Exception{
		List<Long> ids = new ArrayList<Long>();
		ids.add(101l);
		ids.add(102l);
		
		List<ListingSeatTrait> dbtraits = new ArrayList<ListingSeatTrait>();
		ListingSeatTrait lst = new ListingSeatTrait();
		lst.setId(102L);
		lst.setSupplementSeatTraitId(102L);
		dbtraits.add(lst);
		
		when(listingSeatTraitMgr.getSeatTraitsFromComments(anyLong(), anyString())).thenReturn(ids);
		when(listingSeatTraitMgr.findSeatTraits(anyLong())).thenReturn(dbtraits);
		
		Listing listing = new Listing();
		listing.setId(12l);
		listing.setQuantity(2);
		listing.setSplitQuantity(2);
		listing.setEventId(1000l);
		listing.setSplitOption((short)1);
		listing.setListingSource(8);
		listing.setIsETicket(true);
		listing.setFulfillmentMethod(FulfillmentMethod.PDF);
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(true);
		Event event = getEvent( new String[] {"Aisle", "Parking"}, new Long[] {101l, 102l} );
		event.setIsIntegrated(true);
		listing.setSeatTraits(dbtraits);
		listing.setEvent(event);
		
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ts = new TicketSeat();
		ts.setSection("test");
		ts.setRow("r");
		ts.setSeatNumber("1");
		ts.setSeatStatusId(1L);
		ts.setTixListTypeId(1L);
		ticketSeats.add(ts);
		listing.setTicketSeats(ticketSeats);
		
		ListingRequest ulistReq = new ListingRequest();
		List<TicketTrait> newtraits = new ArrayList<TicketTrait>();
		newtraits.add( getTicketTrait (null, "Aisle", Operation.DELETE) );
		newtraits.add( getTicketTrait (null, "Parking", Operation.DELETE) );
		ulistReq.setTicketTraits( newtraits );
		
		SeatProductsContext prodCtx = new  SeatProductsContext(listing, ulistReq, ticketSeatMgr, listingSeatTraitMgr);
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(true);	
		
		helper.processSeatTraits( prodCtx );
		
		Listing curListing = prodCtx.getCurrentListing();
		Assert.assertTrue (curListing.getSeatTraits()!=null && 
				curListing.getSeatTraits().size()==1 && 
				curListing.getSeatTraits().get(0).isMarkForDelete()==true, "Only one SeatTrait needs to be marked for delete by name" );		
	}
	
	@Test
	public void updateListingSeatTrait_markForDeleteByIds() throws Exception{
		List<Long> ids = new ArrayList<Long>();
		ids.add(101l);
		ids.add(102l);
		
		List<ListingSeatTrait> dbtraits = new ArrayList<ListingSeatTrait>();
		ListingSeatTrait lst = new ListingSeatTrait();
		lst.setId(102L);
		lst.setSupplementSeatTraitId(102L);
		dbtraits.add(lst);
		
		when(listingSeatTraitMgr.getSeatTraitsFromComments(anyLong(), anyString())).thenReturn(ids);
		when(listingSeatTraitMgr.findSeatTraits(anyLong())).thenReturn(dbtraits);
		
		Listing listing = new Listing();
		listing.setId(12l);
		listing.setQuantity(2);
		listing.setSplitQuantity(2);
		listing.setEventId(1000l);
		listing.setSplitOption((short)1);
		listing.setListingSource(8);
		listing.setIsETicket(true);
		listing.setFulfillmentMethod(FulfillmentMethod.PDF);
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ts = new TicketSeat();
		ts.setSection("test");
		ts.setRow("r");
		ts.setSeatNumber("1");
		ts.setSeatStatusId(1L);
		ts.setTixListTypeId(1L);
		ticketSeats.add(ts);
		listing.setTicketSeats(ticketSeats);
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(true);
		Event event = getEvent( new String[] {"Aisle", "Parking"}, new Long[] {101l, 102l} );
		event.setIsIntegrated(true);
		listing.setSeatTraits(dbtraits);
		listing.setEvent(event);
		
		ListingRequest ulistReq = new ListingRequest();
		List<TicketTrait> newtraits = new ArrayList<TicketTrait>();
		newtraits.add( getTicketTrait ("101", null, Operation.DELETE) );
		newtraits.add( getTicketTrait ("102", null, Operation.DELETE) );
		ulistReq.setTicketTraits( newtraits );
		
		SeatProductsContext prodCtx = new  SeatProductsContext(listing, ulistReq, ticketSeatMgr, listingSeatTraitMgr);
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(true);	
		
		helper.processSeatTraits( prodCtx );
		Listing curListing = prodCtx.getCurrentListing();
		Assert.assertTrue (curListing.getSeatTraits()!=null &&
				curListing.getSeatTraits().size()==1 && 
				curListing.getSeatTraits().get(0).isMarkForDelete()==true, "Only one SeatTrait needs to be marked for delete by id" );				
	}
	
	@Test
	public void updateTraitsInferredByPassedListing_testAddParkingPass_exists() throws Exception
	{
		List<Long> ids = new ArrayList<Long>();
		ids.add(101l);
		ids.add(102l);
		List<ListingSeatTrait> dbtraits = new ArrayList<ListingSeatTrait>();
		ListingSeatTrait lst = new ListingSeatTrait();
		lst.setId(102L);
		lst.setSupplementSeatTraitId(102L);
		dbtraits.add(lst);
		when(listingSeatTraitMgr.getSeatTraitsFromComments(anyLong(), anyString())).thenReturn(ids);
		when(listingSeatTraitMgr.findSeatTraits(anyLong())).thenReturn(dbtraits);
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(true);
		
		Listing curlisting = getListing (1000l, "sec10", null, null, 10, 10);
		
		List<TicketSeat>seats = super.getTicketSeats(1000l, "sec10", "10", 2);
		seats.get(1).setTixListTypeId(2l);		// there is a parking pass 
		
		curlisting.setTicketSeats(seats);
		
		ListingRequest ulistReq = new ListingRequest();
		SeatProductsContext prodCtx = new  SeatProductsContext(curlisting, ulistReq, ticketSeatMgr, listingSeatTraitMgr);
		
		TicketSeat parkingPass = getParkingPass(1000L);
		prodCtx.addToNewlyAddedTicketSeatList ( parkingPass );
		
		helper.updateTraitsInferredByPassedListing(prodCtx);
		Assert.assertTrue(prodCtx.getCurrentListing().getSeatTraits() == null,
				"Seat traits should not have changed in currentListing because the parking trait already exists" );
	}
	
	@Test
	public void updateTraitsInferredByPassedListing_testAddParkingPass_notexists() throws Exception
	{
		List<Long> ids = new ArrayList<Long>();
		ids.add(101l);
		List<ListingSeatTrait> dbtraits = new ArrayList<ListingSeatTrait>();
		ListingSeatTrait lst = new ListingSeatTrait();
		lst.setSupplementSeatTraitId(101L);
		dbtraits.add(lst);
		when(listingSeatTraitMgr.getSeatTraitsFromComments(anyLong(), anyString())).thenReturn(ids);
		when(listingSeatTraitMgr.findSeatTraits(anyLong())).thenReturn(dbtraits);
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(true);
		
		Listing newlisting = getListing (1000l, null, null, null, 10, 10);
		
		ListingRequest ulistReq = new ListingRequest();
		SeatProductsContext prodCtx = new  SeatProductsContext(newlisting, ulistReq, ticketSeatMgr, listingSeatTraitMgr);
		
		TicketSeat parkingPass = getParkingPass(1000L);
		prodCtx.addToNewlyAddedTicketSeatList ( parkingPass );
		
		helper.updateTraitsInferredByPassedListing(prodCtx);
		Assert.assertTrue(prodCtx.getCurrentListing().getSeatTraits() != null,
				"Seat traits should have changed in currentListing because the parking trait (102) does not exists" );
	}
	
	@Test
	public void updateTraitsInferredByPassedListing_piggyback_notexists() throws Exception
	{
		List<Long> ids = new ArrayList<Long>();
		ids.add(101l);
		List<ListingSeatTrait> dbtraits = new ArrayList<ListingSeatTrait>();
		ListingSeatTrait lst = new ListingSeatTrait();
		lst.setSupplementSeatTraitId(101L);
		dbtraits.add(lst);
		when(listingSeatTraitMgr.getSeatTraitsFromComments(anyLong(), anyString())).thenReturn(ids);
		when(listingSeatTraitMgr.findSeatTraits(anyLong())).thenReturn(dbtraits);
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(true);
		
		Listing dblisting = getListing (1000l, "section one", "R1,R2", "1,2,1,2", 4, 4);
		
		ListingRequest ulistReq = new ListingRequest();
		SeatProductsContext prodCtx = new  SeatProductsContext(dblisting, ulistReq, ticketSeatMgr, listingSeatTraitMgr);
		
		helper.updateTraitsInferredByPassedListing(prodCtx);
		Assert.assertTrue(prodCtx.getCurrentListing().getSeatTraits() != null,
				"Seat traits should have changed in currentListing because the piggyback trait (501) does not exists" );
	}
	
	@Test
	public void testAddSeatTraitsThroughComments() throws Exception {
		List<Long> ids = new ArrayList<Long>();
		ids.add(101l);
		ids.add(102l);
		ids.add(103l);
		
		when(listingSeatTraitMgr.parseComments(anyLong(), anyString())).thenReturn(ids);
		when(listingSeatTraitMgr.findSeatTraits(anyLong())).thenReturn(null);
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(true);
		
		Listing dbListing = new Listing();
		dbListing.setId(1000L);
		dbListing.setEventId(1000l);
		Event event = getEvent( new String[] {"Aisle", "Parking", "Club Pass/Access"}, new Long[] {101l, 102l, 103l} );
		event.setIsIntegrated(true);
		dbListing.setEvent(event);
		dbListing.setQuantity(2);
		dbListing.setListingSource(7);
		dbListing.setSplitQuantity(2);
		dbListing.setSplitOption((short) 0);
		dbListing.setIsETicket(true);
		dbListing.setDeliveryOption(DeliveryOption.MANUAL.getValue());
		
		// ADD traits case
		ListingRequest listingRequest = new ListingRequest();
		listingRequest.setComments("Aisle,Parking Pass,Club Pass/Access");
		
		SeatProductsContext prodCtx = new  SeatProductsContext(dbListing, listingRequest, ticketSeatMgr, listingSeatTraitMgr);
		
		helper.processSeatTraits(prodCtx);
		
		// make sure 2 traits added
		dbListing = prodCtx.getCurrentListing();
		Assert.assertTrue(dbListing.getSeatTraits()!=null && dbListing.getSeatTraits().size()==3, "Failed to add 3 seat traits" );
	}
	
	@Test
	public void testRemoveSeatTraitsThroughComments() throws Exception {
		List<Long> ids = new ArrayList<Long>();
		ids.add(101l);
		
		List<ListingSeatTrait> dbSeatTraits = new ArrayList<ListingSeatTrait>();
		ListingSeatTrait dbSeatTrait1 = new ListingSeatTrait();
		dbSeatTrait1.setId(121L);
		dbSeatTrait1.setSupplementSeatTraitId(101l);
		dbSeatTraits.add(dbSeatTrait1);
		ListingSeatTrait dbSeatTrait2 = new ListingSeatTrait();
		dbSeatTrait2.setId(122L);
		dbSeatTrait2.setSupplementSeatTraitId(102l);
		dbSeatTraits.add(dbSeatTrait2);
		ListingSeatTrait dbSeatTrait3 = new ListingSeatTrait();
		dbSeatTrait3.setId(123L);
		dbSeatTrait3.setSupplementSeatTraitId(103l);
		dbSeatTraits.add(dbSeatTrait3);
		
		when(listingSeatTraitMgr.parseComments(anyLong(), anyString())).thenReturn(ids);
		when(listingSeatTraitMgr.findSeatTraits(anyLong())).thenReturn(dbSeatTraits);
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(true);
		
		Listing dbListing = new Listing();
		dbListing.setId(1000L);
		dbListing.setEventId(1000l);
		Event event = getEvent( new String[] {"Aisle", "Parking", "Club Pass/Access"}, new Long[] {101l, 102l, 103l} );
		event.setIsIntegrated(true);
		dbListing.setEvent(event);
		dbListing.setQuantity(2);
		dbListing.setListingSource(7);
		dbListing.setSplitQuantity(2);
		dbListing.setSplitOption((short) 0);
		dbListing.setIsETicket(true);
		dbListing.setDeliveryOption(DeliveryOption.MANUAL.getValue());
		dbListing.setTicketMedium(3);
		
		ListingRequest listingRequest = new ListingRequest();
		listingRequest.setComments("Aisle");
		
		SeatProductsContext prodCtx = new  SeatProductsContext(dbListing, listingRequest, ticketSeatMgr, listingSeatTraitMgr);
		
		helper.processSeatTraits(prodCtx);
		
		int delCount = 0;
		if (dbListing.getSeatTraits() != null ) {
			for (ListingSeatTrait st : dbListing.getSeatTraits() ) {
				if ( st.isMarkForDelete() ) delCount++;
			}
		}
		Assert.assertTrue ( delCount == 2, "Failed to delete 2 Ticket Seat Traits" );
	}
	
	
	@Test
	public void testAddSeatTraitsThroughCommentsValidation() throws Exception {
		List<Long> ids = new ArrayList<Long>();
		ids.add(101l);
		ids.add(311l);
		ids.add(501l);
		
		when(listingSeatTraitMgr.parseComments(anyLong(), anyString())).thenReturn(ids);
		when(listingSeatTraitMgr.findSeatTraits(anyLong())).thenReturn(null);
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(true);
		
		Listing dbListing = new Listing();
		dbListing.setId(1000L);
		dbListing.setEventId(1000l);
		Event event = getEvent( new String[] {"Aisle", "Piggyback", "Traditional hard tickets"}, new Long[] {101l, 311l, 501l} );
		event.setIsIntegrated(true);
		dbListing.setEvent(event);
		dbListing.setQuantity(2);
		dbListing.setListingSource(7);
		dbListing.setSplitQuantity(2);
		dbListing.setSplitOption((short) 0);
		dbListing.setIsETicket(true);
		dbListing.setTicketMedium(2);
		
		// ADD traits case
		ListingRequest listingRequest = new ListingRequest();
		listingRequest.setComments("Aisle,Piggyback,Traditional hard tickets");
		
		SeatProductsContext prodCtx = new  SeatProductsContext(dbListing, listingRequest, ticketSeatMgr, listingSeatTraitMgr);
		
		helper.processSeatTraits(prodCtx);
		
		// make sure 2 traits added
		dbListing = prodCtx.getCurrentListing();
		Assert.assertTrue(dbListing.getSeatTraits()!=null && dbListing.getSeatTraits().size()==1, "Failed to add 1 seat trait" );
	}
	
	@Test
	public void testAddSeatTraitsThroughCommentsError() throws Exception {
		List<Long> ids = new ArrayList<Long>();
		ids.add(101l);
		
		when(listingSeatTraitMgr.parseComments(anyLong(), anyString())).thenReturn(ids);
		when(listingSeatTraitMgr.findSeatTraits(anyLong())).thenReturn(null);
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(true);
		
		Listing dbListing = new Listing();
		dbListing.setId(1000L);
		dbListing.setEventId(1000l);
		Event event = getEvent( new String[] {"Aisle", "Piggyback", "Traditional hard tickets"}, new Long[] {101l, 311l, 501l} );
		event.setIsIntegrated(true);
		dbListing.setEvent(event);
		dbListing.setQuantity(2);
		dbListing.setListingSource(7);
		dbListing.setSplitQuantity(1);
		dbListing.setSplitOption((short) 2);
		dbListing.setIsETicket(true);
		dbListing.setTicketMedium(2);
		
		ListingRequest listingRequest = new ListingRequest();
		listingRequest.setComments("Aisle");
		listingRequest.setSplitOption(SplitOption.NOSINGLES);
		
		SeatProductsContext prodCtx = new  SeatProductsContext(dbListing, listingRequest, ticketSeatMgr, listingSeatTraitMgr);
		
		try{
			helper.processSeatTraits(prodCtx);
		}catch(ListingBusinessException e){
			Assert.fail("Seat Traits for Aisle, Piggyback with the NOSINGLES split option should not fail", e);
		}
		
	}
	

	@Test(expectedExceptions=ListingBusinessException.class)
	public void testAddParkingPassThroughCommentsError() throws Exception {
		List<Long> ids = new ArrayList<Long>();
		ids.add(102l);
		
		when(listingSeatTraitMgr.parseComments(anyLong(), anyString())).thenReturn(ids);
		when(listingSeatTraitMgr.findSeatTraits(anyLong())).thenReturn(null);
		when(listingSeatTraitMgr.isParkingSupportedForEvent(anyLong())).thenReturn(false);
		
		Listing dbListing = new Listing();
		dbListing.setId(1000L);
		dbListing.setEventId(1000l);
		Event event = getEvent( new String[] {"Aisle", "Piggyback", "Traditional hard tickets"}, new Long[] {101l, 311l, 501l} );
		event.setIsIntegrated(true);
		dbListing.setEvent(event);
		dbListing.setQuantity(2);
		dbListing.setListingSource(7);
		dbListing.setSplitQuantity(2);
		dbListing.setSplitOption((short) 0);
		dbListing.setIsETicket(true);
		dbListing.setTicketMedium(2);
		
		ListingRequest listingRequest = new ListingRequest();
		listingRequest.setComments("Aisle");
		
		SeatProductsContext prodCtx = new  SeatProductsContext(dbListing, listingRequest, ticketSeatMgr, listingSeatTraitMgr);
		
		helper.processSeatTraits(prodCtx);
	}
	
	@Test
	public void getVenueConfigSectionIdTest() throws JsonParseException, JsonMappingException, IOException {
		Long id = 1761l;
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getVenueConfigSectionIdResponse());
		Mockito.when(globalRegServiceHelper.getSectionZoneToggleByCountry(Mockito.anyString())).thenReturn(true);

		VenueConfigSectionOrZone venueConfigSection = helper.getVenueConfigSectionOrZoneId(3941l, "sectionDescription",
				"rowDesc", "piggyBackRowDesc", "US", true);
		Assert.assertNotNull(venueConfigSection.getVenueConfigSectionId());
		org.testng.Assert.assertEquals(venueConfigSection.getVenueConfigSectionId(), id);

	}
	
	@Test
	public void getVenueConfigSectionIdTest01() throws JsonParseException, JsonMappingException, IOException {
		Long id = 1761l;
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getVenueConfigSectionIdResponse());
		Mockito.when(globalRegServiceHelper.getSectionZoneToggleByCountry(Mockito.anyString())).thenReturn(false);

		VenueConfigSectionOrZone venueConfigSection = helper.getVenueConfigSectionOrZoneId(3941l, "sectionDescription",
				"rowDesc", "piggyBackRowDesc", "US", false);
		Assert.assertNotNull(venueConfigSection.getVenueConfigSectionId());
		org.testng.Assert.assertEquals(venueConfigSection.getVenueConfigSectionId(), id);

	}
	
	@Test
	public void getVenueConfigSectionIdTest02() throws JsonParseException, JsonMappingException, IOException {
		Long id = 1761l;
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getVenueConfigSectionIdResponse());
		Mockito.when(globalRegServiceHelper.getSectionZoneToggleByCountry(Mockito.anyString())).thenReturn(false);

		VenueConfigSectionOrZone venueConfigSection = helper.getVenueConfigSectionOrZoneId(3941l, "sectionDescription",
				"rowDesc", "piggyBackRowDesc", "US", null);
		Assert.assertNotNull(venueConfigSection.getVenueConfigSectionId());
		org.testng.Assert.assertEquals(venueConfigSection.getVenueConfigSectionId(), id);

	}
	
	@Test
	public void getVenueConfigSectionIdTest03() throws JsonParseException, JsonMappingException, IOException {
		Long id = 1761l;
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getVenueConfigSectionIdResponse());
		Mockito.when(globalRegServiceHelper.getSectionZoneToggleByCountry(Mockito.anyString())).thenReturn(true);

		VenueConfigSectionOrZone venueConfigSection = helper.getVenueConfigSectionOrZoneId(3941l, "sectionDescription",
				"rowDesc", "piggyBackRowDesc", "US", null);
		Assert.assertNotNull(venueConfigSection.getVenueConfigSectionId());
		org.testng.Assert.assertEquals(venueConfigSection.getVenueConfigSectionId(), id);

	}
	
	@Test
	public void getVenueConfigSectionId_2ndTest() throws JsonParseException, JsonMappingException, IOException {
		Long id = 1761l;
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getVenueConfigSectionIdResponse());
		Mockito.when(globalRegServiceHelper.getSectionZoneToggleByCountry(Mockito.anyString())).thenReturn(true);

		VenueConfigSectionOrZone venueConfigSection = helper.getVenueConfigSectionOrZoneId(3941l, "sectionDescription",
				"rowDesc", null, "US", true);
		Assert.assertNotNull(venueConfigSection.getVenueConfigSectionId());
		org.testng.Assert.assertEquals(venueConfigSection.getVenueConfigSectionId(), id);

	}
	
	@Test
	public void getVenueConfigSectionId_3rdTest() throws JsonParseException, JsonMappingException, IOException {
		Long id = 0l;
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(Response.status(Status.INTERNAL_SERVER_ERROR).build());
		Mockito.when(globalRegServiceHelper.getSectionZoneToggleByCountry(Mockito.anyString())).thenReturn(true);
		VenueConfigSectionOrZone venueConfigSection = helper.getVenueConfigSectionOrZoneId(3941l, "sectionDescription",
				"rowDesc", null, "US", true);
		org.testng.Assert.assertEquals(venueConfigSection.getVenueConfigSectionId(), id);

	}
	
	@Test
	public void getVenueConfigZoneIdTest() throws JsonParseException, JsonMappingException, IOException {
		Long id = 104740l;
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getVenueConfigZoneIdResponse());
		Mockito.when(globalRegServiceHelper.getSectionZoneToggleByCountry(Mockito.anyString())).thenReturn(true);
		VenueConfigSectionOrZone venueConfigSection = helper.getVenueConfigSectionOrZoneId(3941l, "sectionDescription",
				"rowDesc", "piggyBackRowDesc", "US", true);
		Assert.assertNotNull(venueConfigSection.getVenueConfigZoneId());
		org.testng.Assert.assertEquals(venueConfigSection.getVenueConfigZoneId(), id);
	}
	
	
	@Test
	public void getVenueConfigSectionId_exceptionTest() throws JsonParseException, JsonMappingException, IOException {
		Long id = 0l;
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getVenueConfigSectionIdResponseBadRequest());
		Mockito.when(globalRegServiceHelper.getSectionZoneToggleByCountry(Mockito.anyString())).thenReturn(true);
		VenueConfigSectionOrZone venueConfigSection = helper.getVenueConfigSectionOrZoneId(3941l, "sectionDescription",
				"rowDesc", null, "US", true);
		org.testng.Assert.assertEquals(venueConfigSection.getVenueConfigSectionId(), id);

	}
	
	private Response getVenueConfigSectionIdResponse() {
		Response response = new Response() {

			@Override
			public int getStatus() {
				return 200;
			}

			@Override
			public MultivaluedMap<String, Object> getMetadata() {
				return null;
			}

			@Override
			public Object getEntity() {
				String response = "{\"venueConfigurations\":[{\"seatingZones\":[{\"seatingSections\":[{\"id\":1761,\"name\":\"Diamond Club\",\"generalAdmission\":false}]}]}]}";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		return response;
	}
	
	private Response getVenueConfigZoneIdResponse() {
		Response response = new Response() {

			@Override
			public int getStatus() {
				return 200;
			}

			@Override
			public MultivaluedMap<String, Object> getMetadata() {
				return null;
			}

			@Override
			public Object getEntity() {
				String response = "{\"venueConfigurations\":[{\"seatingZones\":[{\"id\":104740,\"name\":\"Lower North Tier\"}]}]}";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		return response;
	}
	
	private Response getVenueConfigSectionIdResponseBadRequest() {
		Response response = new Response(){

			@Override
			public Object getEntity() {
				String string = "something";
				return new Exception(string);
			}

			@Override
			public int getStatus() {
				return 200;
			}

			@Override
			public MultivaluedMap<String, Object> getMetadata() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		return response;
		
	}
		
	
}
