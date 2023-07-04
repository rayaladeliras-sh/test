package com.stubhub.domain.inventory.listings.v2.listeners;

import java.util.List;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.helper.InventoryStatusNotificationHelper;

public class InventoryStatusNotificationListener implements MessageListener {

	private static final Logger logger = LoggerFactory.getLogger(InventoryStatusNotificationListener.class);

	@Autowired
	private InventoryMgr inventoryMgr;

	@Autowired
	private TicketSeatMgr ticketSeatMgr;

	@Autowired
	private InventoryStatusNotificationHelper inventoryStatusNotificationHelper;

	@Autowired
	private ListingSeatTraitMgr listingSeatTraitMgr;

	private static final String LAST_UPDATED_BY = "statusNotify";
	private final static Long PIGGYBACK_SEATING = 501l;

	@Override
	public void onMessage(Message message) {

		MapMessage mapMessage = (MapMessage) message;

		Long listingId = null;
		Long ticketSeatId = null;

		try {
			listingId = mapMessage.getLong("listingId");
			ticketSeatId = mapMessage.getLong("ticketSeatId");
			
			logger.info("_message=\"values received by listener \" listingId={} ticketSeatId={}", listingId,
					ticketSeatId);

			if ((null != ticketSeatId && ticketSeatId > 0) && (null != listingId && listingId > 0)) {
				handleStatusNotify(listingId, ticketSeatId);
			} else {
				logger.info(
						"_message=\"Either listing or ticket Id is missing unable to delete \" listingId={} ticketSeatId={}",
						listingId, ticketSeatId);
			}

		} catch (HibernateOptimisticLockingFailureException e) {
			logger.error("_message=\"Error processing status notification HibernateOptimisticLockingFailureException\" listingId={} ticketSeatId={}",listingId,ticketSeatId, e);
			handleStatusNotify(listingId, ticketSeatId);
		}
		catch (Exception e) {
			logger.error("_message=\"Error processing status notification \" listingId={} ticketSeatId={}",listingId,ticketSeatId, e);
		}

	}
	@Transactional
	private void handleStatusNotify(Long listingId, Long ticketSeatId) {
		List<TicketSeat> ticketSeats = getTicketSeatById(listingId, ticketSeatId);

		if (null != ticketSeats && !ticketSeats.isEmpty()) {
			TicketSeat ticketSeat = ticketSeats.get(0);
			Listing dbListing = getListingWithTicketSeats(ticketSeat.getTicketId());
			if (null != dbListing) {
				dbListing.setSeatTraits(geteSeatTraits(listingId));
				List<TicketSeat> ts = dbListing.getTicketSeats();
				if (null != ts && !ts.isEmpty()) {
					if (!isActiveTicketSeat(ticketSeat, dbListing.getTicketSeats())) {
						logger.info(
								"_message=\"The seat is already been sold or not in active status doing nothing\" listingId={} ticketSeatId={}",
								dbListing.getId(), ticketSeat.getTicketSeatId());
						return;
					}
					updateListingAndTicketSeat(dbListing, ticketSeat);
					logger.info(
							"_message=\"The ticketSeatId has  been successfully removed \" listingId={} ticketSeatId={}",
							dbListing.getId(), ticketSeat.getTicketSeatId());
				} else {
					logger.info(
							"_message=\"The listing have already been sold or not in active status\" listingId={}",
									dbListing.getId());
				}
			}
		}
	}

	private Listing getListingWithTicketSeats(Long listingId) {

		Listing dbListing = inventoryMgr.getListing(listingId);
		List<TicketSeat> ticketSeats = ticketSeatMgr.findActiveTicketSeatsByTicketId(listingId);

		dbListing.setTicketSeats(ticketSeats);

		return dbListing;

	}

	private List<TicketSeat> getTicketSeatById(Long ticketId, Long ticketSeatId) {
		return ticketSeatMgr.findTicketSeatByTicketSeatId(ticketId, ticketSeatId);
	}

	private List<ListingSeatTrait> geteSeatTraits(Long ticketId) {
		return listingSeatTraitMgr.findSeatTraits(ticketId);
	}

	private void updateListingAndTicketSeat(Listing dbListing, TicketSeat deleteSeat) {

		logger.info(
				"_message=\"The ticket is in active status and can be deleted \" listingId={} ticketSeatId={}",
				dbListing.getId(), deleteSeat.getTicketSeatId());
		
		boolean isPiggyBackRows = dbListing.getIsPiggyBack();

		if (dbListing.getTicketSeats().size() == 1) {
			dbListing.setSystemStatus(ListingStatus.DELETED.name());
			dbListing.setLastUpdatedBy(LAST_UPDATED_BY);
		} else {
			updateQuantityInCurrentListing(-1, dbListing);
			updateTicketStatus(dbListing, deleteSeat);
			dbListing.setLastUpdatedBy(LAST_UPDATED_BY);
			try {
				inventoryStatusNotificationHelper.validateSeatsAndRows(dbListing);
				if (isPiggyBackRows && !dbListing.getIsPiggyBack()) {
					for (ListingSeatTrait lst : dbListing.getSeatTraits()) {
						if (lst.getSupplementSeatTraitId().longValue() == PIGGYBACK_SEATING) {
							lst.setMarkForDelete(true);
						}
					}
				}
			} catch (ListingBusinessException lbe) {
				logger.error("mesage={} listingId={}",lbe.getMessage(),dbListing.getId());
				dbListing.setSystemStatus(ListingStatus.DELETED.name());
			}
		}
		inventoryMgr.updateListing(dbListing);

	}

	private void updateTicketStatus(Listing dbListing, TicketSeat deleteSeat) {
		for (TicketSeat ts : dbListing.getTicketSeats()) {
			if (ts.getTicketSeatId().equals(deleteSeat.getTicketSeatId())) {
				ts.setSeatStatusId(4L);
				ts.setLastUpdatedBy(LAST_UPDATED_BY);
				String seats = inventoryStatusNotificationHelper.delFromCSVString(dbListing.getSeats(),
						deleteSeat.getSeatNumber());
				if (null != seats) {
					dbListing.setSeats(seats);

				}
			}
		}
	}

	private void updateQuantityInCurrentListing(int deltaValue, Listing currentListing) {

		currentListing.setQuantity(currentListing.getQuantity() + deltaValue);
		currentListing.setQuantityRemain(currentListing.getQuantityRemain() + deltaValue);
		currentListing.setLastUpdatedBy(LAST_UPDATED_BY);

		if (currentListing.getSplitOption() != null && currentListing.getSplitOption() == 0) {
			// its a no-split ticket, so update the split quantity to
			// same as the quantity
			currentListing.setSplitQuantity(currentListing.getQuantityRemain());
		}
	}
	
	
	private boolean isActiveTicketSeat(TicketSeat deleteSeat, List<TicketSeat> ticketSeats) {
		// checking whether the seat to be deleted is in active state before
		// deleting..
		for (TicketSeat ts : ticketSeats) {
			if (ts.getTicketSeatId().equals(deleteSeat.getTicketSeatId()) && ts.getSeatStatusId().equals(1L)) {
				return true;
			}
		}
		return false;
	}
}
