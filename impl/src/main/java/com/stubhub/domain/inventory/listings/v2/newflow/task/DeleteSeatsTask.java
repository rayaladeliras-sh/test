package com.stubhub.domain.inventory.listings.v2.newflow.task;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.TicketSeatHelper;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.enums.ProductType;

@Component
@Scope("prototype")
public class DeleteSeatsTask extends RegularTask {

  private static final Logger logger = LoggerFactory.getLogger(DeleteSeatsTask.class);

  @Autowired
  private TicketSeatHelper ticketSeatHelper;

  private boolean isPiggyBackRows;

  private final static Long PIGGYBACK_SEATING = 501l;
  private final static Long PARKING_PASS = 102l;


  public DeleteSeatsTask(ListingDTO dto) {
    super(dto);
  }

  @Override
  protected void preExecute() {
    isPiggyBackRows = listingDTO.getDbListing().getIsPiggyBack();
  }

  @Override
  protected void execute() {
    List<Product> products = listingDTO.getUpdateListingInfo().getProductInfo().getDeleteProducts();
    for (Iterator<Product> itr = products.iterator(); itr.hasNext();) {
      Product product = itr.next();
      if (ProductType.PARKING_PASS.equalsEnum(product.getProductType())) {
        boolean isDeleted = deleteParkingPass();
        if(isDeleted) {
          setToPendingLock();
        }
        continue;
      }
      TicketSeat ticket = findTicketSeatEqSeatProduct(product);

      if (ticket == null) {
        logger.error("message=\"Cannot locate seat product to delete\" listingId={} rowSeat={}",
            listingDTO.getDbListing().getId(), ticketSeatHelper.getRowSeat(product));
        throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.listingActionNotallowed,
            "Cannot locate seat product to delete: " + ticketSeatHelper.getRowSeat(product));
      }
      itr.remove();
      ticket.setSeatStatusId(4L);

      updateQuantityInCurrentListing(-1);

      if (product.getSeat() != null) {
        String seats = ticketSeatHelper.delFromCSVString(listingDTO.getDbListing().getSeats(), product.getSeat());
        listingDTO.getDbListing().setSeats(seats);
      }
      
      if (!listingDTO.getDbListing().getSystemStatus().equals(ListingStatus.PENDING_LOCK.toString())) {
        setToPendingLock();
      }
      
    }

  }

  @Override
  protected void postExecute() {
    ticketSeatHelper.validateSeatsAndRows(listingDTO);
    if (isPiggyBackRows && !listingDTO.getDbListing().getIsPiggyBack()) {
      for (ListingSeatTrait lst : listingDTO.getDbListing().getSeatTraits()) {
        if (lst.getSupplementSeatTraitId().longValue() == PIGGYBACK_SEATING) {
          lst.setMarkForDelete(true);
        }
      }
    }
  }

  private boolean deleteParkingPass() {
    boolean isDeleted = false;
    List<TicketSeat> ticketSeatsFromDB = listingDTO.getDbListing().getTicketSeats();
    for (TicketSeat ts : ticketSeatsFromDB) {
      if (ts.getSeatStatusId().intValue() == 1 && ts.getTixListTypeId().intValue() == 2) {
        ts.setSeatStatusId(4L); // 4 == deleted
        listingDTO.getDbListing().setListingType(1L); // Changing the tix_list_type_id from 3 to 1
        isDeleted = true;
      }
    }
    for (ListingSeatTrait lst : listingDTO.getDbListing().getSeatTraits()) {
      if (lst.getSupplementSeatTraitId().longValue() == PARKING_PASS) {
        lst.setMarkForDelete(true);
      }
    }
    
    return isDeleted;
  }

 private TicketSeat findTicketSeatEqSeatProduct(Product product) {

    for (TicketSeat ticketSeat : listingDTO.getDbListing().getTicketSeats()) {
      if (ticketSeat.getSeatStatusId().intValue() == 1) { // if valid seat
        if (ticketSeatHelper.isSameExternalSeatId(product, ticketSeat) || (ticketSeatHelper.isSameSeatNumber(product, ticketSeat)
            && (ticketSeatHelper.isRowBlankOrNA(ticketSeat) || ticketSeatHelper.isSameRow(product, ticketSeat)))) {
          return ticketSeat;
        }
      }
    }
    return null;
  }

  public void updateQuantityInCurrentListing(int deltaValue) {
    Listing currentListing = listingDTO.getDbListing();
    currentListing.setQuantity(currentListing.getQuantity() + deltaValue);
    currentListing.setQuantityRemain(currentListing.getQuantityRemain() + deltaValue);

    if (currentListing.getSplitOption() != null && currentListing.getSplitOption() == 0) {
      // its a no-split ticket, so update the split quantity to
      // same as the quantity
      currentListing.setSplitQuantity(currentListing.getQuantityRemain());
    }
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
