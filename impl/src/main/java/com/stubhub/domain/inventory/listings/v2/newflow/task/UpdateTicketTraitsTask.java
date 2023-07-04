package com.stubhub.domain.inventory.listings.v2.newflow.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.TicketTrait;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.newplatform.common.util.DateUtil;

@Component
@Scope("prototype")
public class UpdateTicketTraitsTask extends RegularTask {

  private final static Long AISLE = 101l;
  private final static Long PARKING_PASS = 102l;
  private final static Long TRADITIONAL_HARD_TICKET = 311l;
  private final static Long PIGGYBACK_SEATING = 501l;
  private static final int LISTING_SOURCE_ID_STH = 8;
  private static final String TICKET_FEATURE = "Ticket Feature";
  private final static Long FULL_SUITE = 2566l;
  private final static Logger log = LoggerFactory.getLogger(UpdateTicketTraitsTask.class);

  @Autowired
  private ListingSeatTraitMgr listingSeatTraitMgr;

  public UpdateTicketTraitsTask(ListingDTO dto) {
    super(dto);
  }

  private ListingRequest request = listingDTO.getListingRequest();
  private Listing dbListing = listingDTO.getDbListing();
  private List<ListingSeatTrait> listingTraitsfromDB = null;

  @Override
  protected void preExecute() {
    listingTraitsfromDB = dbListing.getSeatTraits();
    if (listingTraitsfromDB == null) {
      listingTraitsfromDB = new ArrayList<ListingSeatTrait>();
    }
  }

  @Override
  protected void execute() {
    List<Long> seatTraitIds = new ArrayList<Long>();
    List<String> seatTraitNames = new ArrayList<String>();

    for (ListingSeatTrait trait : listingTraitsfromDB) {
      seatTraitIds.add(trait.getSupplementSeatTraitId());
    }

    log.info("message=\"existing seat traits from DB\" seatTraitIds={} listingId={}", seatTraitIds,
        dbListing.getId());

    for (TicketTrait trait : request.getTicketTraits()) {
      if (trait.getName() != null) {
        seatTraitNames.add(trait.getName());
      }
      if (trait.getId() != null) {
        Long id = new Long(trait.getId());
        if (trait.getOperation() == Operation.ADD) {
          if (!seatTraitIds.contains(id)) {
            seatTraitIds.add(id);
          }
        } else if (trait.getOperation() == Operation.DELETE) {
          seatTraitIds.remove(id);
        }
      }
    }

    seatTraitIds = validateAllSeatTraits(seatTraitIds, seatTraitNames, request.getTicketTraits());
    // add the new traits
    for (TicketTrait ticketTrait : request.getTicketTraits()) {
      Long id = ticketTrait.getId() == null ? -1 : Long.valueOf(ticketTrait.getId());
      if (Operation.ADD.equalsEnum(ticketTrait.getOperation())) {
        if (!seatTraitIds.contains(id)) {
          continue;
        }
        ListingSeatTrait dbtrait = getAlreadyExistingTrait(ticketTrait, listingTraitsfromDB);
        if (dbtrait == null) {
          if (id == PARKING_PASS
              && DeliveryOption.PREDELIVERY.getValue() == dbListing.getDeliveryOption()
              && ListingStatus.ACTIVE.name().equals(dbListing.getSystemStatus())) {
            log.error(
                "message=\"BAD Request to ADD parking pass to a predelivered listing\", listingId={} ",
                dbListing.getId());
            throw new ListingException(ErrorType.BUSINESSERROR,
                ErrorCodeEnum.listingActionNotallowed,
                "Cannot add parking pass to a predelivered listing");
          }
          
          ListingSeatTrait seatTrait = createListingSeatTrait(id);
          listingTraitsfromDB.add(seatTrait);

          log.debug("message=\"ADDING SEAT TRAIT\" supplementSeatTraitId={} listingId={}", id,
              dbListing.getId());
          
        }

        if (id == PARKING_PASS) {
          // add parking pass if does not exist
          manageParkingPassIfExist(true);
        }
      } else if (Operation.DELETE.equalsEnum(ticketTrait.getOperation())) {
        ListingSeatTrait dbtrait = getAlreadyExistingTrait(ticketTrait, listingTraitsfromDB);
        if (dbtrait != null) {
          dbtrait.setMarkForDelete(true);
          log.debug("message=\"DELETING SEAT TRAIT\" supplementSeatTraitId={} listingId={}",
              dbtrait.getSupplementSeatTraitId(), dbListing.getId());
          if (id == PARKING_PASS) { // if parking pass, delete
            manageParkingPassIfExist(false);
          }
        }
      }
    }
  }

  @Override
  protected void postExecute() {

  }

  private List<Long> validateAllSeatTraits(List<Long> seatTraitIds, List<String> seatTraitNames,
      List<TicketTrait> passedTraits) {
    List<Long> processedSeatTraitIds = new ArrayList<Long>();

    validateSeatTraitIdsWithEvent(seatTraitIds, processedSeatTraitIds);

    validateSeatTraitNamesWithEvent(seatTraitNames, passedTraits, processedSeatTraitIds);
    
    boolean isFeature = hasFeature(dbListing, processedSeatTraitIds);
    
    if (processedSeatTraitIds.contains(PARKING_PASS)) {
      if (!isParkingPassSupported(dbListing)) {
        log.error("message=\"Parking pass is not supported\" listingId={} eventId={}",
            dbListing.getId(), dbListing.getEventId());
        throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidTicketTrait,
            "Parking Pass is not supported");
      }
    }

    // IGNORE Piggyback because it is handled in ticket operation section
    if (processedSeatTraitIds.contains(PIGGYBACK_SEATING)) {
      processedSeatTraitIds.remove(PIGGYBACK_SEATING);
    }

    if (processedSeatTraitIds.contains(AISLE) || processedSeatTraitIds.contains(FULL_SUITE) || processedSeatTraitIds.contains(PARKING_PASS)
        || dbListing.getIsPiggyBack() || isFeature) {
      dbListing.setSplitOption((short) 0);
      dbListing.setSplitQuantity(dbListing.getQuantityRemain());
    }

    if (dbListing.getTicketMedium() != null
        && dbListing.getTicketMedium() != TicketMedium.PAPER.getValue()) {
      if (processedSeatTraitIds.contains(TRADITIONAL_HARD_TICKET)) {
        processedSeatTraitIds.remove(TRADITIONAL_HARD_TICKET);
      }
    }

    log.info("message=\"UPDATED Listing Seat Trait IDs\" listingId={}  seatTraitIds={}",
        dbListing.getId(), processedSeatTraitIds);

    return processedSeatTraitIds;
  }

  private void validateSeatTraitIdsWithEvent(List<Long> seatTraitIds,
      List<Long> processedSeatTraitIds) {
    if (seatTraitIds != null && seatTraitIds.size() > 0) {
      if (dbListing.getEvent() != null && dbListing.getEvent().getTicketTraitId() != null) {
        List<Long> applicableSeatTraitIdList = dbListing.getEvent().getTicketTraitId();
        if (applicableSeatTraitIdList.size() > 0) {
          for (Long seatTraitId : seatTraitIds) {
            if (applicableSeatTraitIdList.contains(seatTraitId)) {
              processedSeatTraitIds.add(seatTraitId);
            }
          }
        }
      }
    }
  }

  private void validateSeatTraitNamesWithEvent(List<String> seatTraitNames,
      List<TicketTrait> passedTraits, List<Long> processedSeatTraitIds) {
    if (seatTraitNames != null && seatTraitNames.size() > 0) {
      if (dbListing.getEvent() != null && dbListing.getEvent().getTicketTrait() != null) {
        List<com.stubhub.domain.catalog.events.intf.TicketTrait> applicableSeatTraitList =
            dbListing.getEvent().getTicketTrait();

        // get all event traits
        if (applicableSeatTraitList != null && applicableSeatTraitList.size() > 0) {
          for (String seatTraitName : seatTraitNames) {
            for (com.stubhub.domain.catalog.events.intf.TicketTrait ticketTrait : applicableSeatTraitList) {

              // find a trait by name and get its id
              if (seatTraitName.equalsIgnoreCase(ticketTrait.getName())) {
                if (!processedSeatTraitIds.contains(ticketTrait.getId())) {
                  processedSeatTraitIds.add(ticketTrait.getId());
                }

                // set the id back in passed ticketTrait
                TicketTrait tt = findTicketTrait(seatTraitName, ticketTrait.getId(), passedTraits);
                if (tt != null) {
                  tt.setName(seatTraitName);
                  tt.setId(String.valueOf(ticketTrait.getId()));
                }

                break;
              }
            }
          }
        }
      }
    }
  }

  private TicketTrait findTicketTrait(String name, Long id, List<TicketTrait> passedTraits) {
    String strId = String.valueOf(id);
    for (TicketTrait tt : passedTraits) {
      if (tt.getId() != null && strId.equals(tt.getId())) {
        return tt;
      } else if (tt.getName() != null && name.equalsIgnoreCase(tt.getName())) {
        return tt;
      }
    }
    return null;
  }

  private ListingSeatTrait getAlreadyExistingTrait(TicketTrait trait,
      List<ListingSeatTrait> traitsfromDB) {
    if (trait.getId() != null) {
      Long id = Long.valueOf(trait.getId());

      for (ListingSeatTrait lt : traitsfromDB) {
        if (lt.getSupplementSeatTraitId() != null && lt.getSupplementSeatTraitId().equals(id))
          return lt;
      }
    }
    return null;
  }

  private boolean isParkingPassSupported(Listing listing) {
    if (listing != null && listing.getListingSource() != null
        && listing.getListingSource().intValue() == LISTING_SOURCE_ID_STH) {
      return true;
    }
    if (listing != null) {
      Event event = listing.getEvent();
      if (event != null && event.getIsIntegrated()) {
        return listingSeatTraitMgr.isParkingSupportedForEvent(event.getId());
      }
    }
    return true;
  }

  private void manageParkingPassIfExist(boolean add) {
    TicketSeat parkingPass = null;
    for (TicketSeat ticket : dbListing.getTicketSeats()) {
      if (ticket.getSeatStatusId().intValue() == 1 && ticket.getTixListTypeId().intValue() == 2) {
        parkingPass = ticket;
        break;
      }
    }

    if (parkingPass != null) {
      if (!add) { // if delete
        log.debug("message=\"DELETING parking pass seat\" listingId={}", dbListing.getId());
        parkingPass.setSeatStatusId(4L);
        dbListing.setListingType(1L);
        setToPendingLock();
      }
    } else if (add) {// if not there and add, then add
      log.debug("message=\"ADDING parking pass seat\" listingId={}", dbListing.getId());
      parkingPass = createParkingTicketSeat(dbListing);
      dbListing.getTicketSeats().add(parkingPass);
      dbListing.setListingType(3L);
    }
  }

  private TicketSeat createParkingTicketSeat(Listing listing) {
    TicketSeat seat = new TicketSeat();
    seat.setSection("Lot");
    seat.setRow("LOT");
    seat.setTicketId(listing.getId());
    seat.setSeatNumber("Parking Pass");
    seat.setSeatDesc("Parking");
    seat.setGeneralAdmissionInd(false);
    seat.setTixListTypeId(2l);
    seat.setSeatStatusId(1l);
    Calendar utcNow = DateUtil.getNowCalUTC();
    seat.setCreatedDate(utcNow);
    seat.setLastUpdatedDate(utcNow);
    seat.setCreatedBy(CommonConstants.LISTING_API_V2);
    seat.setLastUpdatedBy(CommonConstants.LISTING_API_V2);
    return seat;
  }

  private ListingSeatTrait createListingSeatTrait(Long seatTraitId) {
    ListingSeatTrait seatTrait = new ListingSeatTrait();
    seatTrait.setActive(true);
    seatTrait.setSellerSpecifiedInd(true);
    seatTrait.setExtSystemSpecifiedInd(false);
    seatTrait.setSupplementSeatTraitId(seatTraitId);
    seatTrait.setTicketId(dbListing.getId());
    Calendar utcNow = DateUtil.getNowCalUTC();
    seatTrait.setCreatedDate(utcNow);
    seatTrait.setLastUpdatedDate(utcNow);
    seatTrait.setCreatedBy(CommonConstants.LISTING_API_V2);
    seatTrait.setLastUpdatedBy(CommonConstants.LISTING_API_V2);
    seatTrait.setMarkForDelete(false);
    return seatTrait;
  }
  
  private boolean hasFeature(Listing listing, List<Long> seatTraitIds) {
    boolean isFeature = false;
    if (listing.getEvent() != null && listing.getEvent().getTicketTrait() != null) {
      for (com.stubhub.domain.catalog.events.intf.TicketTrait trait : listing.getEvent()
          .getTicketTrait()) {
        if (seatTraitIds.contains(trait.getId())
            && TICKET_FEATURE.equalsIgnoreCase(trait.getType())) {
          isFeature = true;
          break;
        }
      }
    }
    return isFeature;
  }
  
  private void setToPendingLock() {
    if ((TicketMedium.BARCODE.getValue() == listingDTO.getDbListing().getTicketMedium()
        || TicketMedium.FLASHSEAT.getValue() == listingDTO.getDbListing().getTicketMedium())
        && DeliveryOption.PREDELIVERY.getValue() == listingDTO.getDbListing().getDeliveryOption()) {
      if (!listingDTO.getDbListing().getSystemStatus().equals(ListingStatus.INCOMPLETE.toString())) {
        listingDTO.getDbListing().setSystemStatus(ListingStatus.PENDING_LOCK.toString());
      }
    }
  }

}
