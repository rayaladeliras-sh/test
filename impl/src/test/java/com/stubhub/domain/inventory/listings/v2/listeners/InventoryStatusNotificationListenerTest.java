package com.stubhub.domain.inventory.listings.v2.listeners;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.helper.InventoryStatusNotificationHelper;
import com.stubhub.domain.inventory.listings.v2.util.JMSMessageHelper;

public class InventoryStatusNotificationListenerTest {

	@Mock
	private InventoryMgr inventoryMgr;

	@Mock
	private TicketSeatMgr ticketSeatMgr;

	@Mock
	private MapMessage message;

	@Mock
	private JMSMessageHelper jmsMessageHelper;

	@Mock
	private InventoryStatusNotificationHelper inventoryStatusNotificationHelper;

	@Mock
	private ListingSeatTraitMgr listingSeatTraitMgr;

	@InjectMocks
	private InventoryStatusNotificationListener listener = new InventoryStatusNotificationListener();

	@BeforeMethod
	public void setup() {
		initMocks(this);
	}

	@Test
	public void testOnMesage() {
		try {
			Long listingId = 123456789L;
			Long ticketSeatId = 987654321L;

			when(message.getLong("listingId")).thenReturn(listingId);
			when(message.getLong("ticketSeatId")).thenReturn(ticketSeatId);
			when(ticketSeatMgr.findTicketSeatByTicketSeatId(Mockito.anyLong(), Mockito.anyLong()))
					.thenReturn(getTicketSeats());
			when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(getListing());
			when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(getTicketSeats());
			listener.onMessage(message);
			verify(ticketSeatMgr, times(1)).findActiveTicketSeatsByTicketId(listingId);

		} catch (JMSException e) {
			fail("Should not reach here...");
		}
	}

	@Test
	public void testOnMesageWithSeatTraits() {
		try {
			Long listingId = 123456789L;
			Long ticketSeatId = 987654321L;

			when(message.getLong("listingId")).thenReturn(listingId);
			when(message.getLong("ticketSeatId")).thenReturn(ticketSeatId);
			when(ticketSeatMgr.findTicketSeatByTicketSeatId(Mockito.anyLong(), Mockito.anyLong()))
					.thenReturn(getTicketSeats());
			when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(gePiggybacktListing());
			when(listingSeatTraitMgr.findSeatTraits(Mockito.anyLong())).thenReturn(getSeatTraits());
			when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong()))
					.thenReturn(getPiggybackTicketSeats());

			when(inventoryStatusNotificationHelper.delFromCSVString(Mockito.anyString(), Mockito.anyString()))
					.thenReturn("1");
			doAnswer(new Answer<Void>() {

				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					Listing listing = (Listing) invocation.getArguments()[0];
					listing = getValidatedPiggybacktListing();
					return null;
				}
			}).when(inventoryStatusNotificationHelper).validateSeatsAndRows(Mockito.any(Listing.class));

			listener.onMessage(message);

			verify(ticketSeatMgr, times(1)).findActiveTicketSeatsByTicketId(listingId);

		} catch (JMSException e) {
			fail("Should not reach here...");
		}
	}
	
	@Test
	public void testOnMesageALreadySoldTickets() {
		try {
			Long listingId = 123456789L;
			Long ticketSeatId = 987654321L;

			when(message.getLong("listingId")).thenReturn(listingId);
			when(message.getLong("ticketSeatId")).thenReturn(ticketSeatId);
			when(ticketSeatMgr.findTicketSeatByTicketSeatId(Mockito.anyLong(), Mockito.anyLong()))
					.thenReturn(getTicketSeats());
			when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(getListing());
			when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(new ArrayList<TicketSeat>());
			listener.onMessage(message);
			verify(ticketSeatMgr, times(1)).findActiveTicketSeatsByTicketId(listingId);

		} catch (JMSException e) {
			fail("Should not reach here...");
		}
	}

	private List<TicketSeat> getTicketSeats() {
		List<TicketSeat> ticketSeats = new ArrayList<>();

		TicketSeat seat = new TicketSeat();
		seat.setTicketId(123456789L);
		seat.setTicketSeatId(456789L);
		seat.setTixListTypeId(1L);
		seat.setSeatNumber("G");
		seat.setSection("112");
		seat.setRow("1");
		ticketSeats.add(seat);

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
		seat.setRow("G");
		ticketSeats.add(seat);

		TicketSeat seat1 = new TicketSeat();
		seat1.setTicketId(123456789L);
		seat1.setTicketSeatId(456788L);
		seat1.setTixListTypeId(1L);
		seat1.setSeatNumber("1");
		seat1.setSection("112");
		seat1.setRow("H");
		ticketSeats.add(seat1);

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

	private Listing getListing() {

		Listing dbListing = new Listing();

		dbListing.setId(1271578716L);
		dbListing.setEventId(1271578799L);
		dbListing.setSection("112");
		dbListing.setRow("G");
		dbListing.setSeats("1");
		dbListing.setQuantity(1);
		dbListing.setSplitQuantity(1);
		dbListing.setQuantityRemain(1);
		dbListing.setSplitOption((short)0);
		dbListing.setCurrency(Currency.getInstance(Locale.US));
		dbListing.setDeliveryOption(2);
		dbListing.setFulfillmentDeliveryMethods(
				"10,22,5.25,,2017-09-19T19:00:00Z|10,23,5.25,,2017-09-19T19:00:00Z|10,24,5.25,,2017-09-14T19:00:00Z");
		dbListing.setSystemStatus("ACTIVE");
		dbListing.setTicketSeats(getTicketSeats());

		return dbListing;
	}

	private Listing gePiggybacktListing() {

		Listing dbListing = new Listing();

		dbListing.setId(1271578716L);
		dbListing.setEventId(1271578799L);
		dbListing.setSection("112");
		dbListing.setRow("G,H");
		dbListing.setSeats("1,1");
		dbListing.setQuantity(2);
		dbListing.setSplitQuantity(1);
		dbListing.setQuantityRemain(2);
		dbListing.setCurrency(Currency.getInstance(Locale.US));
		dbListing.setDeliveryOption(2);
		dbListing.setFulfillmentDeliveryMethods(
				"10,22,5.25,,2017-09-19T19:00:00Z|10,23,5.25,,2017-09-19T19:00:00Z|10,24,5.25,,2017-09-14T19:00:00Z");
		dbListing.setSystemStatus("ACTIVE");
		dbListing.setTicketSeats(getTicketSeats());

		return dbListing;
	}

	private Listing getValidatedPiggybacktListing() {

		Listing dbListing = new Listing();

		dbListing.setId(1271578716L);
		dbListing.setEventId(1271578799L);
		dbListing.setSection("112");
		dbListing.setRow("H");
		dbListing.setSeats("1");
		dbListing.setQuantity(1);
		dbListing.setSplitQuantity(1);
		dbListing.setQuantityRemain(1);
		dbListing.setCurrency(Currency.getInstance(Locale.US));
		dbListing.setDeliveryOption(2);
		dbListing.setFulfillmentDeliveryMethods(
				"10,22,5.25,,2017-09-19T19:00:00Z|10,23,5.25,,2017-09-19T19:00:00Z|10,24,5.25,,2017-09-14T19:00:00Z");
		dbListing.setSystemStatus("ACTIVE");
		dbListing.setTicketSeats(getTicketSeats());

		return dbListing;
	}

}
