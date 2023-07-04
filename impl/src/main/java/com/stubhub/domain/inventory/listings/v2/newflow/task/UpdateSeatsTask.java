package com.stubhub.domain.inventory.listings.v2.newflow.task;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.entity.ListingCheck;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.TicketSeatHelper;
import com.stubhub.domain.inventory.listings.v2.util.CmaValidator;
import com.stubhub.domain.inventory.listings.v2.util.InventorySolrUtil;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.newplatform.common.entity.Money;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

@Component
@Scope("prototype")
public class UpdateSeatsTask extends RegularTask {

  private static final Logger logger = LoggerFactory.getLogger(UpdateSeatsTask.class);

  @Autowired
  private TicketSeatHelper ticketSeatHelper;

  @Autowired
  private CmaValidator cmaValidator;

  @Autowired
  private InventorySolrUtil inventorySolrUtil;

  public UpdateSeatsTask(ListingDTO dto) {
    super(dto);
  }

  @Override
  protected void preExecute() {}

  @Override
  protected void execute() {

    Listing dbListing = listingDTO.getDbListing();
    int passedTicketQty = 0;
    List<Product> products = listingDTO.getUpdateListingInfo().getProductInfo().getUpdateProducts();
    for (Product product : products) {
      if (!product.getProductType().equals(ProductType.PARKING_PASS)) {
        passedTicketQty++;
      }
    }
    List<TicketSeat> dbTicketSeats = listingDTO.getDbListing().getTicketSeats();
    boolean skipSeatMatch = false;
    List<TicketSeat> ticketSeatsFromDBForCheck = new ArrayList<TicketSeat>(dbTicketSeats);
    if (passedTicketQty == dbListing.getQuantityRemain()) {
      skipSeatMatch = true;
      for (TicketSeat ticketSeat : ticketSeatsFromDBForCheck) {
        if (ticketSeat.getTixListTypeId() == 1 && !ticketSeat.isGeneralAdmissionInd()
            && ticketSeat.getSeatNumber() != null && ticketSeat.getSeatNumber().length() > 0) {
          skipSeatMatch = false;
          break;
        }
      }
    }

    for (Product product : products) {
      TicketSeat ticket = getTicketSeat(skipSeatMatch, ticketSeatsFromDBForCheck, product);
      assertTicketNotNull(dbListing, product, ticket);
      ticketSeatsFromDBForCheck.remove(ticket);
      fixTicketRow(product, ticket);
      assertRowIsValid(dbListing, ticket);
      fixSeatNumber(product, ticket);
      assertSeatNumberIsValid(dbListing, ticket);
      assertSeatIsNotDuplicated(dbListing, ticket);

      if(StringUtils.trimToNull(product.getUniqueTicketNumber()) != null && !isSameUniqueTicketNumber(product, ticket)){
    	  ticket.setUniqueTicketNumber(product.getUniqueTicketNumber().trim());
      }


      Money dbListingFaceValue = dbListing.getFaceValue();
      Money ticketFaceValue = ticket.getFaceValue();

      if (ticketFaceValue == null && product.getFaceValue() == null && dbListingFaceValue != null && dbListingFaceValue.getAmount() != null) {
        //copy the listing's face value to seat's face value in case of missing
        product.setFaceValue(dbListingFaceValue);
      }

      Money productFaceValue = product.getFaceValue();
      if (productFaceValue != null && productFaceValue.getAmount() != null && productFaceValue.getAmount().doubleValue() > 0) {
        if (ticketFaceValue == null || (ticketFaceValue.getAmount() != null && productFaceValue.getAmount().compareTo(ticketFaceValue.getAmount()) != 0)) {
          ticket.setFaceValue(productFaceValue);
          if (productFaceValue.getCurrency() != null) {
            ticket.setCurrency(Currency.getInstance(productFaceValue.getCurrency()));
          }
        }
      }
    }
  }

  private void fixSeatNumber(Product product, TicketSeat ticketSeat) {
    if (StringUtils.trimToNull(product.getSeat()) != null && !isSameSeatNumber(product, ticketSeat)) {
      ticketSeat.setSeatNumber(product.getSeat().trim());
    }
  }

  private void fixTicketRow(Product product, TicketSeat ticketSeat) {
    if (StringUtils.trimToNull(product.getRow()) != null && !isSameRow(product, ticketSeat)) {
      ticketSeat.setRow(product.getRow().trim());
    }
  }

  private void assertRowIsValid(Listing listing, TicketSeat ticketSeat) {
    if (ticketSeat.getRow() != null && isCMAEvent(listing) && !cmaValidator.isValidRow(listing.getEventId(), ticketSeat.getRow(), listing.getSection())) {
      logger.error("message=\"Row/Seat contains words that are not allowed\":\" listingId={} row={}",
              listing.getId(), ticketSeat.getRow());
      throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidRowWords,
              "Row/Seat contains words that are not allowed");
    }
  }

  private boolean isCMAEvent(Listing dbListing) {
    return null != dbListing.getEvent().getCountry() && "GB".equalsIgnoreCase(dbListing.getEvent().getCountry())
            && "GBP".equalsIgnoreCase(dbListing.getCurrency().getCurrencyCode());
  }

  private void assertSeatNumberIsValid(Listing listing, TicketSeat ticketSeat) {
    if (ticketSeat.getSeatNumber() != null && isCMAEvent(listing) && !cmaValidator.isValidSeat(ticketSeat.getSeatNumber())) {
      logger.error("message=\"Row/Seat contains words that are not allowed\":\" listingId={} seat={}",
              listing.getId(), ticketSeat.getSeatNumber());
      throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidRowWords,
              "Row/Seat contains words that are not allowed");
    }
  }

  private void assertSeatIsNotDuplicated(Listing listing, TicketSeat ticket) {
    ListingCheck listingCheck = inventorySolrUtil.isListingExists(listing.getEventId(), listing.getSellerId(),
            ticket.getSection(), ticket.getRow(), ticket.getSeatNumber(), listing.getId());
    if (listingCheck.getIsListed()) {
      logger.error("message=\"Cannot update seat product, that seat/row combination already exists\":\" seat={} row={}",
              ticket.getSeatNumber(), ticket.getRow());
      throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.duplicateSectionRowSeat,
              "Row/Seat combination already exists");
    }
  }

  private TicketSeat getTicketSeat(boolean skipSeatMatch, List<TicketSeat> ticketSeats, Product product) {
    TicketSeat ticketSeat;
    if (product.getSeatId() != null) {
      logger.info("UpdateSeatsTask :: findTicketSeatBySeatId");
      ticketSeat = findTicketSeatBySeatId(product, ticketSeats);
    } else if (!skipSeatMatch) {
      logger.info("UpdateSeatsTask :: findTicketSeatEqSeatProduct");
      ticketSeat = findTicketSeatEqSeatProduct(product, ticketSeats);
    } else if (product.getProductType().equals(ProductType.PARKING_PASS)) {
      logger.info("UpdateSeatsTask :: findParkingPassSeat");
      ticketSeat = findParkingPassSeat(ticketSeats);
    } else {
      logger.info("UpdateSeatsTask :: findTicketSeatByExternalId");
      ticketSeat = findTicketSeatByExternalId(product, ticketSeats);
      if (null == ticketSeat) {
        logger.info("UpdateSeatsTask :: findTicketSeatByRow");
        ticketSeat = findTicketSeatByRow(product, ticketSeats);
      }
    }
    return ticketSeat;
  }

  private void assertTicketNotNull(Listing dbListing, Product prod, TicketSeat ticket) {
    if (ticket == null) {
      logger.error("message=\"Cannot locate seat product to update:\" listingId={} rowSeat={}",
          dbListing.getId(), getRowSeat(prod));

      throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.listingActionNotallowed,
          "Cannot locate seat product to update: " + getRowSeat(prod));
    }
  }

  @Override
  protected void postExecute() {
    ticketSeatHelper.validateSeatsAndRows(listingDTO);
  }

  private static boolean isSameExternalSeatId(Product seatProduct, TicketSeat ticketSeat) {
    if (StringUtils.isNotBlank(seatProduct.getExternalId())
        && StringUtils.isNotBlank(ticketSeat.getExternalSeatId())) {
      return StringUtils.equalsIgnoreCase(seatProduct.getExternalId().trim(),
          ticketSeat.getExternalSeatId().trim());
    }
    return false;
  }

  private static boolean isSameRow(Product seatProduct, TicketSeat ticketSeat) {
    if (StringUtils.isNotBlank(seatProduct.getRow())
        && StringUtils.isNotBlank(ticketSeat.getRow())) {
      return StringUtils.equalsIgnoreCase(seatProduct.getRow().trim(), ticketSeat.getRow().trim());
    }
    return false;
  }

  private static boolean isRowBlankOrNA(TicketSeat ticketSeat) {
    if (StringUtils.isBlank(ticketSeat.getRow())
        || ticketSeat.getRow().equalsIgnoreCase(CommonConstants.GA_ROW_DESC)) {
      return true;
    }
    return false;
  }

  private static TicketSeat findParkingPassSeat(List<TicketSeat> ticketSeatsFromDB) {
    for (TicketSeat ticketSeat : ticketSeatsFromDB) {
      if (ticketSeat.getSeatStatusId().intValue() == 1
          && ticketSeat.getTixListTypeId().intValue() == 2) {
        return ticketSeat;
      }
    }
    return null;
  }

  private static TicketSeat findTicketSeatByExternalId(Product seatProduct, List<TicketSeat> ticketSeatsFromDB) {
    for (TicketSeat ticketSeat : ticketSeatsFromDB) {
      if (ticketSeat.getSeatStatusId().intValue() == 1) { // seat status 1 is valid seat
        String row = seatProduct.getRow();
        if (ticketSeat.isGeneralAdmissionInd()
            && (row == null || ("N/A".equals(row) || "GA".equals(row)))) {
          return ticketSeat;
        }
        if (isSameExternalSeatId(seatProduct, ticketSeat)) {
          return ticketSeat;
        }
      }
    }
    return null;
  }

  private static TicketSeat findTicketSeatByRow(Product seatProduct, List<TicketSeat> ticketSeatsFromDB) {
    for (TicketSeat ticketSeat : ticketSeatsFromDB) {
      if (ticketSeat.getSeatStatusId().intValue() == 1
          && ((isRowBlankOrNA(ticketSeat) || isSameRow(seatProduct, ticketSeat)))) {
        return ticketSeat;
      }
    }
    return null;
  }

  private static TicketSeat findTicketSeatBySeatId(Product seatProduct, List<TicketSeat> ticketSeatsFromDB) {
    if (seatProduct.getSeatId() != null) {
      for (TicketSeat ticketSeat : ticketSeatsFromDB) {
        if (ticketSeat.getSeatStatusId().intValue() == 1) {    // if valid seat
          if (ticketSeat.getTicketSeatId() != null && ticketSeat.getTicketSeatId().equals(seatProduct.getSeatId())) {
            return ticketSeat;
          }
        }
      }
    }
    return null;
  }

  private static String getRowSeat(Product t) {
    return "(row:" + t.getRow() + ", seat:" + t.getSeat() + ")";
  }

  private TicketSeat findTicketSeatEqSeatProduct(Product likeSeat, List<TicketSeat> ticketSeatsFromDB) {
    for (TicketSeat ticketSeat : ticketSeatsFromDB) {
      if (ticketSeat.getSeatStatusId().intValue() == 1) { // if valid seat
        if (isSameExternalSeatId(likeSeat, ticketSeat) || (isSameSeatNumber(likeSeat, ticketSeat)
            && (isRowBlankOrNA(ticketSeat) || isSameRow(likeSeat, ticketSeat)))) {
          return ticketSeat;
        }
      }
    }
    return null;
  }

  private boolean isSameSeatNumber(Product seatProduct, TicketSeat ticketSeat) {
    if (StringUtils.isNotBlank(seatProduct.getSeat())
        && StringUtils.isNotBlank(ticketSeat.getSeatNumber())) {
      return StringUtils.equalsIgnoreCase(seatProduct.getSeat().trim(),
          ticketSeat.getSeatNumber().trim());
    }
    return false;
  }
  
  private static boolean isSameUniqueTicketNumber(Product seatProduct, TicketSeat ticketSeat) {
      if (StringUtils.isNotBlank(seatProduct.getUniqueTicketNumber())
              && StringUtils.isNotBlank(ticketSeat.getUniqueTicketNumber())) {
          return StringUtils.equalsIgnoreCase(seatProduct.getUniqueTicketNumber().trim(), ticketSeat.getUniqueTicketNumber().trim());
      }
      return false;
  }
}
