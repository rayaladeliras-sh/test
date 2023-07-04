package com.stubhub.domain.inventory.listings.v2.helper;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;



public class InventoryStatusNotificationHelperTest {
	
	@InjectMocks
	private InventoryStatusNotificationHelper inventoryStatusNotificationHelper = new InventoryStatusNotificationHelper();
	
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testdelFromCSVString() {
		String result = inventoryStatusNotificationHelper.delFromCSVString("A,B,C", "B");
		assertEquals("A,C", result);
	}
	
	@Test
	public void testValidateSeatsAndRows() {
		
		inventoryStatusNotificationHelper.validateSeatsAndRows(getListing());
		
	}
	
	@Test
	public void testValidateSeatsAndRowsInconsistentPiggyback() {
		
		try {
			inventoryStatusNotificationHelper.validateSeatsAndRows(gePiggybacktListing());
		} catch (ListingException e) {
			assertEquals("Unbalanced piggyback rows seats. Numbers (1, 2)", e.getMessage());
		}
		
	}
	
	@Test
	public void testValidateSeatsAndRowsInconsistentPiggybackQtyBelow2() {
		
		try {
			Listing piggyBackListing = gePiggybacktListing();
			piggyBackListing.setTicketSeats(getPiggybackTicketSeatsQty());
			piggyBackListing.setQuantityRemain(1);
			inventoryStatusNotificationHelper.validateSeatsAndRows(piggyBackListing);
		} catch (ListingException e) {
			assertEquals("Invalid piggyback number of seats. Minimum of 2 is required.", e.getMessage());
		}
		
	}
	
	
	private Listing getListing() {

		Listing dbListing = new Listing();

		dbListing.setId(1271578716L);
		dbListing.setEventId(1271578799L);
		dbListing.setSection("112");
		dbListing.setRow("H");
		dbListing.setSeats("1,2");
		dbListing.setQuantity(2);
		dbListing.setSplitQuantity(1);
		dbListing.setQuantityRemain(2);
		dbListing.setCurrency(Currency.getInstance(Locale.US));
		dbListing.setDeliveryOption(2);
		dbListing.setFulfillmentDeliveryMethods(
				"10,22,5.25,,2017-09-19T19:00:00Z|10,23,5.25,,2017-09-19T19:00:00Z|10,24,5.25,,2017-09-14T19:00:00Z");
		dbListing.setSystemStatus("ACTIVE");
		dbListing.setTicketSeats(getTicketSeats());
		dbListing.setSeatTraits(getSeatTraits());

		return dbListing;
	}
	
	private Listing gePiggybacktListing() {

		Listing dbListing = new Listing();

		dbListing.setId(1271578716L);
		dbListing.setEventId(1271578799L);
		dbListing.setSection("112");
		dbListing.setRow("A,B");
		dbListing.setSeats("1,1,2,2");
		dbListing.setQuantity(4);
		dbListing.setSplitQuantity(1);
		dbListing.setQuantityRemain(4);
		dbListing.setCurrency(Currency.getInstance(Locale.US));
		dbListing.setDeliveryOption(2);
		dbListing.setFulfillmentDeliveryMethods(
				"10,22,5.25,,2017-09-19T19:00:00Z|10,23,5.25,,2017-09-19T19:00:00Z|10,24,5.25,,2017-09-14T19:00:00Z");
		dbListing.setSystemStatus("ACTIVE");
		dbListing.setTicketSeats(getPiggybackTicketSeats());
		dbListing.setSeatTraits(getSeatTraits());

		return dbListing;
	}
	
	
	private List<TicketSeat> getTicketSeats() {
		List<TicketSeat> ticketSeats = new ArrayList<>();

		TicketSeat seat = new TicketSeat();
		seat.setTicketId(123456789L);
		seat.setTicketSeatId(456789L);
		seat.setTixListTypeId(1L);
		seat.setSeatNumber("1");
		seat.setSection("112");
		seat.setRow("H");
		seat.setSeatStatusId(4L);
		ticketSeats.add(seat);

		TicketSeat seat1 = new TicketSeat();
		seat1.setTicketId(123456789L);
		seat1.setTicketSeatId(456788L);
		seat1.setTixListTypeId(1L);
		seat1.setSeatNumber("2");
		seat1.setSection("112");
		seat1.setRow("H");
		seat1.setSeatStatusId(1L);
		ticketSeats.add(seat1);

		return ticketSeats;
	}
	
	private List<TicketSeat> getPiggybackTicketSeats() {
		List<TicketSeat> ticketSeats = new ArrayList<>();

		TicketSeat seat = new TicketSeat();
		seat.setTicketId(123456789L);
		seat.setTicketSeatId(456789L);
		seat.setTixListTypeId(1L);
		seat.setSeatNumber("1");
		seat.setSection("112");
		seat.setRow("A");
		seat.setSeatStatusId(4L);
		ticketSeats.add(seat);

		TicketSeat seat1 = new TicketSeat();
		seat1.setTicketId(123456789L);
		seat1.setTicketSeatId(456788L);
		seat1.setTixListTypeId(1L);
		seat1.setSeatNumber("2");
		seat1.setSection("112");
		seat1.setRow("A");
		seat1.setSeatStatusId(1L);
		ticketSeats.add(seat1);
		
		TicketSeat seat2 = new TicketSeat();
		seat2.setTicketId(123456789L);
		seat2.setTicketSeatId(456789L);
		seat2.setTixListTypeId(1L);
		seat2.setSeatNumber("1");
		seat2.setSection("112");
		seat2.setRow("B");
		seat2.setSeatStatusId(1L);
		ticketSeats.add(seat2);

		TicketSeat seat3 = new TicketSeat();
		seat3.setTicketId(123456789L);
		seat3.setTicketSeatId(456788L);
		seat3.setTixListTypeId(1L);
		seat3.setSeatNumber("2");
		seat3.setSection("112");
		seat3.setRow("B");
		seat3.setSeatStatusId(1L);
		ticketSeats.add(seat3);

		return ticketSeats;
	}
	
	private List<TicketSeat> getPiggybackTicketSeatsQty() {
		List<TicketSeat> ticketSeats = new ArrayList<>();

		TicketSeat seat = new TicketSeat();
		seat.setTicketId(123456789L);
		seat.setTicketSeatId(456789L);
		seat.setTixListTypeId(1L);
		seat.setSeatNumber("1");
		seat.setSection("112");
		seat.setRow("A");
		seat.setSeatStatusId(4L);
		ticketSeats.add(seat);

		TicketSeat seat1 = new TicketSeat();
		seat1.setTicketId(123456789L);
		seat1.setTicketSeatId(456788L);
		seat1.setTixListTypeId(1L);
		seat1.setSeatNumber("2");
		seat1.setSection("112");
		seat1.setRow("A");
		seat1.setSeatStatusId(1L);
		ticketSeats.add(seat1);
		
		TicketSeat seat2 = new TicketSeat();
		seat2.setTicketId(123456789L);
		seat2.setTicketSeatId(456789L);
		seat2.setTixListTypeId(1L);
		seat2.setSeatNumber("1");
		seat2.setSection("112");
		seat2.setRow("B");
		seat2.setSeatStatusId(4L);
		ticketSeats.add(seat2);

		TicketSeat seat3 = new TicketSeat();
		seat3.setTicketId(123456789L);
		seat3.setTicketSeatId(456788L);
		seat3.setTixListTypeId(1L);
		seat3.setSeatNumber("2");
		seat3.setSection("112");
		seat3.setRow("B");
		seat3.setSeatStatusId(1L);
		ticketSeats.add(seat3);

		return ticketSeats;
	}
	
	
	private List<ListingSeatTrait> getSeatTraits() {

		List<ListingSeatTrait> listingSeatTraitList = new ArrayList<>();
		ListingSeatTrait listingSeatTrait = new ListingSeatTrait();
		listingSeatTrait.setSupplementSeatTraitId(501L);
		listingSeatTrait.setActive(true);
		listingSeatTraitList.add(listingSeatTrait);

		return listingSeatTraitList;

	}

}
