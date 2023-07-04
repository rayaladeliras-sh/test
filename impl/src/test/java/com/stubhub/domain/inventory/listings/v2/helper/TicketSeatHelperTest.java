package com.stubhub.domain.inventory.listings.v2.helper;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.List;

import org.testng.annotations.Test;

import com.stubhub.domain.inventory.biz.v2.impl.TicketSeatMgrImpl;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.helper.TicketSeatHelper;

public class TicketSeatHelperTest {

	@Test
	public void createTicketSeats() throws Exception{
		TicketSeatHelper helper = new TicketSeatHelper();
		TicketSeatMgr ticketSeatMgr = mock(TicketSeatMgrImpl.class);
		setBeanProperty(helper, "ticketSeatMgr", ticketSeatMgr);
		
		Listing listing = new Listing();
		listing.setId(1000l);
		listing.setSection("Box");
		listing.setRow("12");
		listing.setSeats("1,2,3,4,5");
		listing.setQuantity(5);
		listing.setListingType(1l);
		
		List<TicketSeat> seats = helper.createTicketSeats(listing);
		assertTrue(seats.size() == 5);
		assertTrue(seats.get(0).getSeatNumber() != null);
	}

	@Test
	public void createTicketSeats_NoSeats() throws Exception{
		TicketSeatHelper helper = new TicketSeatHelper();
		TicketSeatMgr ticketSeatMgr = mock(TicketSeatMgrImpl.class);
		setBeanProperty(helper, "ticketSeatMgr", ticketSeatMgr);
		
		Listing listing = new Listing();
		listing.setId(1000l);
		listing.setSection("General Admission");
		listing.setRow("12");
		listing.setQuantity(5);
		listing.setListingType(1l);
		
		List<TicketSeat> seats = helper.createTicketSeats(listing);
		assertTrue(seats.size() == 5);

	}
	
	@Test
	public void createTicketSeats_ParkingPass() throws Exception{
		TicketSeatHelper helper = new TicketSeatHelper();
		TicketSeatMgr ticketSeatMgr = mock(TicketSeatMgrImpl.class);
		setBeanProperty(helper, "ticketSeatMgr", ticketSeatMgr);
		
		Listing listing = new Listing();
		listing.setId(1000l);
		listing.setSection("Box");
		listing.setRow("1,2");
		listing.setSeats("1,2,3,4,5");
		listing.setQuantity(5);
		listing.setListingType(3l);
		
		List<TicketSeat> seats = helper.createTicketSeats(listing);
		assertTrue(seats.size() == 6);
		assertTrue(seats.get(0).getSeatNumber() != null);
	}
	
	public void setBeanProperty(Object objInstance, String propertyName,
			Object newVal) throws Exception {
		Field[] fields = objInstance.getClass().getDeclaredFields();
		objInstance.getClass().getDeclaredMethods();

		if (fields != null) {
			for (Field field : fields) {
				if (field.getName().equalsIgnoreCase(propertyName)) {
					field.setAccessible(true);
					field.set(objInstance, newVal);
				}
			}
		}
	}
	
}