package com.stubhub.domain.inventory.listings.v2.helper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.stereotype.Component;

import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.newplatform.common.util.DateUtil;

@Component("ticketSeatHelper")
public class TicketSeatHelper {
	
	private final static long TICKETS_SEAT_STATUS_AVAILABLE = 1L;
	private final static long TICKETS_LIST_TYPE_ID = 1L;
	private static final Long PARKING_PASS_SEAT = 2L;
	private static final Long LISTING_TYPE_TICKETS_PARKING = 3L;
	private final static String MODULENAME_Create = "CreateListing";

	public List<TicketSeat> createTicketSeats(Listing listing) {
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		String[] seats = null;
		if (listing.getSeats() != null
				&& !listing.getSeats().equals(CommonConstants.GENERAL_ADMISSION))
			seats = listing.getSeats().split(",");
		String[] rows = null;
		if(listing.getRow() !=null)
			rows = listing.getRow().split(",");
		for (int i = 0; i < listing.getQuantity(); i++) {
			TicketSeat seat = new TicketSeat();
			seat.setTicketId(listing.getId());
			seat.setSection(listing.getSection());
			if(rows.length ==2){
				if(i < listing.getQuantity()/2){
					seat.setRow(rows[0]);
				}else{
					seat.setRow(rows[1]);
				}
			}else{
				seat.setRow(listing.getRow());
			}
			seat.setGeneralAdmissionInd(false);
			if (seats != null && i < seats.length) {
				seat.setSeatNumber(seats[i]);
			} 
			if (listing.getEvent() != null && listing.getEvent().getGaIndicator() == true) {
				seat.setGeneralAdmissionInd(true);
			}
			seat.setTixListTypeId(TICKETS_LIST_TYPE_ID);
			seat.setSeatStatusId(TICKETS_SEAT_STATUS_AVAILABLE);
			Calendar utcNow = DateUtil.getNowCalUTC();
			seat.setCreatedDate(utcNow);
			seat.setLastUpdatedDate(utcNow);
			seat.setCreatedBy(MODULENAME_Create);
			seat.setLastUpdatedBy(MODULENAME_Create);

			ticketSeats.add(seat);
		}
		if(listing.getListingType().longValue() == LISTING_TYPE_TICKETS_PARKING){
			TicketSeat seat = new TicketSeat();
			seat.setTicketId(listing.getId());
			seat.setSection("Lot");
			seat.setRow("LOT");
			seat.setSeatNumber("Parking Pass");
			seat.setSeatDesc("Parking");
			seat.setGeneralAdmissionInd(false);
			seat.setTixListTypeId(PARKING_PASS_SEAT);
			seat.setSeatStatusId(TICKETS_SEAT_STATUS_AVAILABLE);
			Calendar utcNow = DateUtil.getNowCalUTC();
			seat.setCreatedDate(utcNow);
			seat.setLastUpdatedDate(utcNow);
			seat.setCreatedBy(MODULENAME_Create);
			seat.setLastUpdatedBy(MODULENAME_Create);
			ticketSeats.add(seat);
			
		}
		return ticketSeats;
	}
}
