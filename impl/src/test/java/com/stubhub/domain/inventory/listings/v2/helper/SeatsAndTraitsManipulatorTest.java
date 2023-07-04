package com.stubhub.domain.inventory.listings.v2.helper;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyBoolean;

import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.fulfillment.common.enums.ListingStatus;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.PDFTicketMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.entity.VenueConfigSectionOrZone;
import com.stubhub.domain.inventory.listings.v2.util.InventorySolrUtil;
import com.stubhub.domain.inventory.listings.v2.util.ListingSeatTraitsHelper;
import com.stubhub.domain.inventory.listings.v2.util.SeatProductsContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;

public class SeatsAndTraitsManipulatorTest {

	private static final Log log = LogFactory.getLog(SeatsAndTraitsManipulatorTest.class);

	private SeatsAndTraitsManipulator SeatsAndTraitsManipulator;
	private ObjectMapper objectMapper;
	private SHAPIContext apiContext;
	private SeatProductsContext seatProdContext;
	private ListingSeatTraitsHelper listingSeatTraitsHelper;
	private TicketSeatMgr ticketSeatMgr;
	private InventoryMgr inventoryMgr;
	private PDFTicketMgr pdfTicketMgr;
	private InventorySolrUtil inventorySolrUtil;
	private Long venueConfigSectionsId = 0L;

	@BeforeMethod
	public void setUp() {
		SeatsAndTraitsManipulator = new SeatsAndTraitsManipulator() {
			ObjectMapper objectMapper = null;
			SHAPIContext apiContext = null;
			SeatProductsContext seatProdContext = null;
			ListingSeatTraitsHelper listingSeatTraitsHelper = null;
			TicketSeatMgr ticketSeatMgr = null;
			InventoryMgr inventoryMgr = null;
			PDFTicketMgr pdfTicketMgr = null;
			InventorySolrUtil inventorySolrUtil = null;
		};

		objectMapper = Mockito.mock(ObjectMapper.class);
		ReflectionTestUtils.setField(SeatsAndTraitsManipulator, "objectMapper", objectMapper);

		apiContext = Mockito.mock(SHAPIContext.class);
		ReflectionTestUtils.setField(SeatsAndTraitsManipulator, "apiContext", apiContext);

		seatProdContext = Mockito.mock(SeatProductsContext.class);
		ReflectionTestUtils.setField(SeatsAndTraitsManipulator, "seatProdContext", seatProdContext);

		listingSeatTraitsHelper = Mockito.mock(ListingSeatTraitsHelper.class);
		ReflectionTestUtils.setField(SeatsAndTraitsManipulator, "listingSeatTraitsHelper", listingSeatTraitsHelper);

		ticketSeatMgr = Mockito.mock(TicketSeatMgr.class);
		ReflectionTestUtils.setField(SeatsAndTraitsManipulator, "ticketSeatMgr", ticketSeatMgr);

		inventoryMgr = Mockito.mock(InventoryMgr.class);
		ReflectionTestUtils.setField(SeatsAndTraitsManipulator, "inventoryMgr", inventoryMgr);

		pdfTicketMgr = Mockito.mock(PDFTicketMgr.class);
		ReflectionTestUtils.setField(SeatsAndTraitsManipulator, "pdfTicketMgr", pdfTicketMgr);

		inventorySolrUtil = Mockito.mock(InventorySolrUtil.class);
		ReflectionTestUtils.setField(SeatsAndTraitsManipulator, "inventorySolrUtil", inventorySolrUtil);

		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void processSeatsTraitsTest() throws UnsupportedEncodingException {
		List<VenueConfigSectionOrZone> listVenueConfigSecZone = new ArrayList<>();
		
		VenueConfigSectionOrZone venueConfigSection1 = new VenueConfigSectionOrZone();
		venueConfigSection1.setVenueConfigSectionId(111L);
		venueConfigSection1.setGeneralAdmission(false);
		venueConfigSection1.setVenueConfigZoneId(0l);
		listVenueConfigSecZone.add(venueConfigSection1);
		
		VenueConfigSectionOrZone venueConfigSection2 = new VenueConfigSectionOrZone();
		venueConfigSection2.setVenueConfigZoneId(112l);
		listVenueConfigSecZone.add(venueConfigSection2);
		
		for(VenueConfigSectionOrZone venueConfigSecZone : listVenueConfigSecZone){

			Listing listing = new Listing();
			listing.setSection("some_section");
			Event event = new Event();
			listing.setEvent(event);
			listing.getEvent().setRowScrubbing(true);
			listing.getEvent().setSectionScrubbing(true);
			listing.setIsPiggyBack(false);
			listing.setRow("row");
			

			when(listingSeatTraitsHelper.getVenueConfigSectionOrZoneId(anyLong(), anyString(), anyString(), anyString(), anyString(), anyBoolean()))
					.thenReturn(venueConfigSecZone);
	
			Listing currentListing = new Listing();
			List<TicketSeat> curTicketSeats = new ArrayList<TicketSeat>();
			currentListing.setTicketSeats(null);
			currentListing.setIsPiggyBack(false);
			currentListing.setSystemStatus(ListingStatus.INCOMPLETE.toString());
			currentListing.setId(0l);
			currentListing.setTicketMedium(1);
			when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(curTicketSeats);
			listing = SeatsAndTraitsManipulator.processSeatsTraits(listing, currentListing, apiContext, seatProdContext,
					listingSeatTraitsHelper, ticketSeatMgr, inventoryMgr, pdfTicketMgr, inventorySolrUtil);
			Assert.assertNotNull(listing);
			Assert.assertEquals(listing.getSection(), "some_section");
			Assert.assertEquals(listing.getSystemStatus(), ListingStatus.INCOMPLETE.toString());
			Assert.assertFalse(listing.getSectionScrubSchedule());
		}
	}
	
	@Test
	public void processSeatsTraitsTest_2ndCase() throws UnsupportedEncodingException {
		
		List<VenueConfigSectionOrZone> listVenueConfigSecZone = new ArrayList<>();
		
		VenueConfigSectionOrZone venueConfigSection1 = new VenueConfigSectionOrZone();
		venueConfigSection1.setVenueConfigSectionId(0l);
		venueConfigSection1.setGeneralAdmission(true);
		venueConfigSection1.setVenueConfigZoneId(null);
		listVenueConfigSecZone.add(venueConfigSection1);
		
		VenueConfigSectionOrZone venueConfigSection2 = new VenueConfigSectionOrZone();
		venueConfigSection2.setVenueConfigZoneId(0l);
		listVenueConfigSecZone.add(venueConfigSection2);

		for(VenueConfigSectionOrZone venueConfigSecZone : listVenueConfigSecZone){

			Listing listing = new Listing();
			listing.setSection("some_section");
			Event event = new Event();
			listing.setEvent(event);
			listing.getEvent().setRowScrubbing(true);
			listing.getEvent().setSectionScrubbing(true);
			listing.setIsPiggyBack(true);
			listing.setRow("row1,row2");
	
			when(listingSeatTraitsHelper.getVenueConfigSectionOrZoneId(anyLong(), anyString(), anyString(), anyString(), anyString(), anyBoolean()))
					.thenReturn(venueConfigSecZone);
	
			Listing currentListing = new Listing();
			List<TicketSeat> curTicketSeats = new ArrayList<TicketSeat>();
			currentListing.setTicketSeats(curTicketSeats);
			TicketSeat seat1 = new TicketSeat();
			seat1.setSection("LAWN");
			seat1.setGeneralAdmissionInd(false);
			seat1.setRow("1");
			seat1.setSeatNumber("1");
			curTicketSeats.add(seat1);
			currentListing.setIsPiggyBack(true);
			currentListing.setSystemStatus(ListingStatus.INCOMPLETE.toString());
			currentListing.setId(0l);
			currentListing.setTicketMedium(1);
			when(ticketSeatMgr.findTicketSeatsByTicketId(anyLong())).thenReturn(curTicketSeats);
			listing = SeatsAndTraitsManipulator.processSeatsTraits(listing, currentListing, apiContext, seatProdContext,
					listingSeatTraitsHelper, ticketSeatMgr, inventoryMgr, pdfTicketMgr, inventorySolrUtil);
			Assert.assertNotNull(listing);
			Assert.assertEquals(listing.getSection(), "some_section");
			Assert.assertEquals(listing.getSystemStatus(), ListingStatus.INCOMPLETE.toString());
			Assert.assertTrue(listing.getSectionScrubSchedule());
		}
	}

}
