package com.stubhub.domain.inventory.listings.v2.newflow.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.enums.ConfirmOptionEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.util.TicketSeatUtils;
import com.stubhub.newplatform.common.util.DateUtil;

@Component("updateQuantityTask")
@Scope("prototype")
public class UpdateQuantityTask extends RegularTask {

  private final static Logger log = LoggerFactory.getLogger(UpdateQuantityTask.class);

  @Autowired
  private TicketSeatMgr ticketSeatMgr;

  public UpdateQuantityTask(ListingDTO listingDTO) {
    super(listingDTO);
  }

  @Override
  protected void preExecute() {}

  @Override
  protected void execute() {
    Listing dbListing = listingDTO.getDbListing();
    int modifiedQuantityCount = 0;

    final int reqQuantity = listingDTO.getListingRequest().getQuantity();
    final int dbQty = listingDTO.getDbListing().getQuantityRemain();

    modifiedQuantityCount = dbQty - reqQuantity;

    if (reqQuantity < dbQty) { // delete seats
      if (dbListing.getIsPiggyBack() && reqQuantity < 2) {
        log.error("message=\"Minimum of 2 seats is required for a piggyback listing\" listingId={}",
            dbListing.getId());
        throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidPiggybackRow);
      }
      deleteSeatsFromBottom(modifiedQuantityCount, listingDTO.getDbListing());
    } else if (!TicketSeatUtils.isGASection(dbListing.getSection())
        && !TicketSeatUtils.isGASection(listingDTO.getListingRequest().getSection())) {
      log.error(
          "message=\"Cannot increase quantity in a non GA listing without product array\" listingId={}",
          dbListing.getId());
      throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidGAQuantity);
    } else {
      _addDummyGASeats(modifiedQuantityCount, listingDTO.getDbListing());
    }
    dbListing.setQuantity(dbListing.getQuantity() - modifiedQuantityCount);
    dbListing.setQuantityRemain(dbListing.getQuantityRemain() - modifiedQuantityCount);
    if (dbListing.getSplitOption() != null && dbListing.getSplitOption() == 0) {
      dbListing.setSplitQuantity(dbListing.getQuantityRemain());
    }

    if (modifiedQuantityCount > 0 && dbListing.getTicketMedium() != null
        && (dbListing.getTicketMedium() == TicketMedium.BARCODE.getValue()
            || dbListing.getTicketMedium() == TicketMedium.FLASHSEAT.getValue())
        && dbListing.getDeliveryOption() == DeliveryOption.PREDELIVERY.getValue()) {

      if (!dbListing.getSystemStatus().equals(ListingStatus.INCOMPLETE.name())) {
        dbListing.setInhandDate(DateUtil.getNowCalUTC());
        dbListing.setSystemStatus(ListingStatus.PENDING_LOCK.toString());
        dbListing.setConfirmOption(ConfirmOptionEnum.AUTO_CONFIRM.getConfirmStatus());
      }
    }
  }

  @Override
  protected void postExecute() {

  }

  private void deleteSeatsFromBottom(int changeQty, Listing dbListing) {
    String seats = dbListing.getSeats();
    List<TicketSeat> curTicketSeats =
        ticketSeatMgr.findActiveTicketSeatsOnlyByTicketId(dbListing.getId());
    for (int j = 0; j < changeQty; j++) {
      String removeSeat = null;
      int idx = curTicketSeats.size() - j - 1;
      if (seats != null && seats.contains(",")) {
        removeSeat = seats.substring(seats.lastIndexOf(",") + 1);
        seats = seats.substring(0, seats.lastIndexOf(","));
      }
      boolean deleted = false;
      if (removeSeat != null) {
        for (int i = curTicketSeats.size() - 1; i >= 0; i--) {
          TicketSeat ts = curTicketSeats.get(i);
          if (removeSeat.equalsIgnoreCase(ts.getSeatNumber())
              && ts.getSeatStatusId().longValue() == 1L && ts.getTixListTypeId() == 1L) {
            ts.setSeatStatusId(4L); // 4 = deleted
            deleted = true;
            break;
          }
        }
      }
      if (!deleted) {
        TicketSeat ts = curTicketSeats.get(idx);
        ts.setSeatStatusId(4L); // 4 = deleted
      }
    }
    dbListing.setSeats(seats);
    dbListing.setTicketSeats(curTicketSeats);

  }

  private void _addDummyGASeats(int changeQty, Listing currentListing) {
    List<TicketSeat> curTicketSeats =
        ticketSeatMgr.findActiveTicketSeatsByTicketId(currentListing.getId());
    TicketSeat likeSeat = null;

    if (curTicketSeats.size() > 0) {
      likeSeat = curTicketSeats.get(0);
    }

    for (int i = 0; i < -changeQty; i++) {
      TicketSeat ts = TicketSeatUtils.makeTicketSeatLike(currentListing, likeSeat, null);
      ts.setTixListTypeId(1L);
      ts.setSeatStatusId(1L);
      ts.setGeneralAdmissionInd(true);
      curTicketSeats.add(ts);
      currentListing.setTicketSeats(curTicketSeats);
    }

  }

}
