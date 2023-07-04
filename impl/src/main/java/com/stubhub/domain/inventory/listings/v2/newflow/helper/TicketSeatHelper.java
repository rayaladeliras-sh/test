package com.stubhub.domain.inventory.listings.v2.newflow.helper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.newplatform.common.util.DateUtil;

@Component("_ticketSeatHelper")
public class TicketSeatHelper {

  public final static Logger logger = LoggerFactory.getLogger(TicketSeatHelper.class);

  private final static Long PIGGYBACK_SEATING = 501l;

  public void validateSeatsAndRows(ListingDTO listingDTO) {

    boolean piggybackTraitEsists = false;
    Listing currentListing = listingDTO.getDbListing();
    List<TicketSeat> seats = currentListing.getTicketSeats();
    HashMap<String, Integer> rowsMap = new HashMap<String, Integer>();
    ArrayList<String> rowList = new ArrayList<String>();
    ArrayList<String> seatList = new ArrayList<String>();
    int rowEmpty = -1;
    int seatEmpty = -1;
    boolean rowError = false;
    boolean seatError = false;

    for (TicketSeat ts : seats) {

      if (ts.getSeatStatusId().intValue() != 1 || ts.getTixListTypeId().intValue() != 1)
        continue;

      // check row consistency (has to be either all empty or all have value) and piggyback
      String row = ts.getRow();
      if (StringUtils.trimToNull(row) != null) {
        Integer count = rowsMap.get(row);
        if (count == null) {
          rowsMap.put(row, new Integer(1));
          rowList.add(row);
        } else {
          rowsMap.put(row, new Integer(count + 1));
        }

        // check if row was empty and become not
        if (rowEmpty < 0)
          rowEmpty = 0;
        else if (rowEmpty == 1) {
          rowError = true;
          break;
        }
      } else {
        if (rowEmpty < 0)
          rowEmpty = 1;
        else if (rowEmpty == 0) {
          rowError = true;
          break;
        }
      }

      // check seat consistency (has to be either all empty or all have value)
      String seat = ts.getSeatNumber();
      if (StringUtils.trimToNull(seat) != null) {
        seatList.add(seat);
        if (seatEmpty < 0)
          seatEmpty = 0;
        else if (seatEmpty == 1) {
          seatError = true;
          break;
        }
      } else {
        if (seatEmpty < 0)
          seatEmpty = 1;
        else if (seatEmpty == 0) {
          seatError = true;
          break;
        }
      }
    }
    // check for errors
    if (rowError) {
      logger.error(
          "message=\"Update operatios seats resulted in inconsistent rows with some having values and others empty\", listingId={} ",
          currentListing.getId());
      throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.incorrectQuantityOfSeats,
          "Update operation for seats resulted in inconsistent rows with some having values and others empty");
    } else if (seatError) {
      logger.error(
          "message=\"Update operatios seats resulted in inconsistent seats with some having values and others empty\", listingId={} ",
          currentListing.getId());
      throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.incorrectQuantityOfSeats,
          "Update operation for seats resulted in inconsistent seats with some having values and others empty");
    }

    // Figure out the rows (and check for piggyBack)
    if (rowsMap.size() > 0) {
      int lastVal = -1;
      for (Iterator<Integer> it = rowsMap.values().iterator(); it.hasNext();) {
        Integer val = it.next();
        if (lastVal < 0) {
          lastVal = val;
        } else if (lastVal != val) {
          logger.error("message=\"Unbalanced piggyback rows seats. Numbers (" + lastVal + ", " + val
              + ")\", listingId={} ", currentListing.getId());
          throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidPiggybackRow,
              "Unbalanced piggyback rows seats. Numbers (" + lastVal + ", " + val + ")");
        }
      }
      
      if (rowList.get(0).equals(CommonConstants.GA_ROW_DESC)) {
        currentListing.setRow(CommonConstants.GA_ROW_DESC);
        currentListing.setSeats(CommonConstants.GENERAL_ADMISSION);
      } else {
        currentListing.setRow(makeCSVStringFromList(rowList));
        if (seatList.isEmpty()) {
          currentListing.setSeats(CommonConstants.TO_BE_DEFINED);
        } else {
          currentListing.setSeats(makeCSVStringFromList(seatList));
        }
      }

      // piggyback validations
      if (currentListing.getIsPiggyBack() && currentListing.getQuantityRemain() < 2) {
        logger.error("errorType={} errorCode={} message={}", ErrorType.INPUTERROR,
            ErrorCodeEnum.invalidPiggybackRow,
            "Invalid piggyback number of seats. Minimum of 2 is required.");
        throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidPiggybackRow,
            "Invalid piggyback number of seats. Minimum of 2 is required.");
      }
    }

    // check for existing piggyback seatTraits
    if (currentListing.getIsPiggyBack()) {
      if (currentListing.getSeatTraits() != null && !currentListing.getSeatTraits().isEmpty()) {
        for (ListingSeatTrait trait : currentListing.getSeatTraits()) {
          if (trait.getSupplementSeatTraitId().longValue() == PIGGYBACK_SEATING) {
            piggybackTraitEsists = true;
          }
        }
      }
      if (!piggybackTraitEsists) {
        ListingSeatTrait seatTrait = makeListingSeatTrait(currentListing.getId(), PIGGYBACK_SEATING,
            CommonConstants.LISTING_API_V2, CommonConstants.LISTING_API_V2);
        currentListing.getSeatTraits().add(seatTrait);
      }
      
      currentListing.setSplitOption((short) 0);
      currentListing.setSplitQuantity(currentListing.getQuantityRemain());
    }
  }

  public String makeCSVStringFromList(List<String> seats) {
    StringBuilder sb = new StringBuilder(200);
    for (String str : seats) {
      sb.append(str).append(',');
    }
    sb.setLength(sb.length() - 1);
    return sb.toString();
  }

  public ListingSeatTrait makeListingSeatTrait(long listingId, long supplementSeatTraitId,
      String createdBy, String updatedBy) {
    ListingSeatTrait seatTrait = new ListingSeatTrait();
    seatTrait.setActive(true);
    seatTrait.setSellerSpecifiedInd(true);
    seatTrait.setExtSystemSpecifiedInd(false);
    seatTrait.setSupplementSeatTraitId(supplementSeatTraitId);
    seatTrait.setTicketId(listingId);
    Calendar utcNow = DateUtil.getNowCalUTC();
    seatTrait.setCreatedDate(utcNow);
    seatTrait.setLastUpdatedDate(utcNow);
    seatTrait.setCreatedBy(createdBy);
    seatTrait.setLastUpdatedBy(updatedBy);
    seatTrait.setMarkForDelete(false);
    return seatTrait;
  }

  public boolean isSameExternalSeatId(Product seatProduct, TicketSeat ticketSeat) {
    if (StringUtils.isNotBlank(seatProduct.getExternalId())
        && StringUtils.isNotBlank(ticketSeat.getExternalSeatId())) {
      return StringUtils.equalsIgnoreCase(seatProduct.getExternalId().trim(),
          ticketSeat.getExternalSeatId().trim());
    }
    return false;
  }

  public boolean isRowBlankOrNA(TicketSeat ticketSeat) {
    if (StringUtils.isBlank(ticketSeat.getRow())
        || ticketSeat.getRow().equalsIgnoreCase(CommonConstants.GA_ROW_DESC)) {
      return true;
    }
    return false;
  }

  public boolean isSameRow(Product seatProduct, TicketSeat ticketSeat) {
    if (StringUtils.isNotBlank(seatProduct.getRow())
        && StringUtils.isNotBlank(ticketSeat.getRow())) {
      return StringUtils.equalsIgnoreCase(seatProduct.getRow().trim(), ticketSeat.getRow().trim());
    }
    return false;
  }

  public String getRowSeat(Product t) {
    return "(row:" + t.getRow() + ", seat:" + t.getSeat() + ")";
  }

  public String addToCSVString(String csvList, String item) {
    if (csvList == null || csvList.length() == 0) {
      return item.trim();
    }
    if (item != null && !item.isEmpty()) {
      return csvList.trim() + "," + item.trim();
    } else {
      return csvList.trim();
    }
  }

  public String addToCSVStringUnique(String csvList, String item) {
    String list = "," + csvList + ",";

    // if not there add
    if (list.indexOf("," + item + ",") < 0) {
      return addToCSVString(csvList, item);
    }
    return csvList;
  }

  public TicketSeat findTicketSeatEqSeatProduct(Product likeSeat,
      List<TicketSeat> ticketSeatsFromDB) {
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

  public boolean isSameSeatNumber(Product seatProduct, TicketSeat ticketSeat) {
    if (StringUtils.isNotBlank(seatProduct.getSeat())
        && StringUtils.isNotBlank(ticketSeat.getSeatNumber())) {
      return StringUtils.equalsIgnoreCase(seatProduct.getSeat().trim(),
          ticketSeat.getSeatNumber().trim());
    }
    return false;
  }

  public String delFromCSVString(String csvList, String item) {
    StringBuilder sb = new StringBuilder("," + csvList + ",");
    String delItem = "," + item + ",";
    int i = sb.indexOf(delItem);
    if (i >= 0) {
      sb.delete(i, i + item.length() + 1);
    }
    sb.deleteCharAt(0);
    if (sb.length() > 0) {
      sb.setLength(sb.length() - 1);
    }
    return sb.toString();
  }

}
