package com.stubhub.domain.inventory.listings.v2.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.common.util.ListingException;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.entity.SeatProduct;
import com.stubhub.domain.inventory.listings.v2.enums.ConfirmOptionEnum;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.newplatform.common.entity.Money;

public class SeatProductsManipulator {

  private final static Logger log = Logger.getLogger(SeatProductsManipulator.class);

  /**
   * processTicketProducts
   *
   * @param listing
   * @param seatProdContext
   * @param updateListingRequest
   * @throws ListingException
   */
  public static void processTicketProducts(Listing listing, SeatProductsContext seatProdContext,
                                           ListingRequest updateListingRequest) throws ListingException {

    Listing currentListing = seatProdContext.getCurrentListing();
    List<TicketSeat> ticketSeatsFromDB = seatProdContext.getTicketSeatsFromCache();
    List<SeatProduct> passedProductsList = seatProdContext.getPassedSeatProductList(false);

    //When no face value is present at DB listing and DB seat has face value
    // Update request to Add seat with no face value - Do not allow
    if (null != currentListing.getFaceValue() && null == currentListing.getFaceValue().getAmount()
            && null != ticketSeatsFromDB && ticketSeatsFromDB.size() > 0 && null != ticketSeatsFromDB.get(0)) {
      Money currentTicketSeatFaceValue = ticketSeatsFromDB.get(0).getFaceValue();
      if (null != currentTicketSeatFaceValue && currentTicketSeatFaceValue.getAmount() != null
              && currentTicketSeatFaceValue.getAmount().doubleValue() > 0) {
        if(passedProductsList != null && !passedProductsList.isEmpty()) {
          for (SeatProduct seat : passedProductsList) {
            if (Operation.ADD.equalsEnum(seat.getOperation()) && seat.getFaceValue() == null) {
              ListingError listingError = new ListingError(ErrorType.BUSINESSERROR,
                      ErrorCode.INVALID_FACE_VALUE, "inventory.listings.invalidFaceValue", "faceValue");
              throw new ListingBusinessException(listingError);
            }
          }
        }
      }
    }

    if (DeliveryOption.PREDELIVERY.getValue() == currentListing.getDeliveryOption() && seatProdContext.getHasAddOperation() && seatProdContext.getHasDelOperation()) {
      log.error("BAD Request with both ADD and DELETE operations in the same request listingId="
              + currentListing.getId());
      ListingError listingError =
              new ListingError(ErrorType.BUSINESSERROR, ErrorCode.LISTING_ACTION_NOTALLOWED,
                      "Cannot add and delete seats in the same request", "");
      throw new ListingBusinessException(listingError);
    }

    // (1) Process delete first
    if (seatProdContext.getHasDelOperation()) {
      processDelete(listing, seatProdContext, currentListing, ticketSeatsFromDB, passedProductsList);
    }

    if ((seatProdContext.getHasAddOperation() || seatProdContext.getHasUpdOperation()) && !seatProdContext.isCreate()) {
      if (DeliveryOption.PREDELIVERY.getValue() == currentListing.getDeliveryOption() && isListingActiveOrPending(currentListing)) {
        log.error("BAD Request to ADD seats or UPDATE fulfillment artifacts to a predelivered listing listingId="
                + currentListing.getId());
        ListingError listingError =
                new ListingError(ErrorType.BUSINESSERROR, ErrorCode.LISTING_ACTION_NOTALLOWED,
                        "Cannot add seats or update fulfillment artifacts to a predelivered listing", "");
        throw new ListingBusinessException(listingError);
      }
    }

    // (2) PARKING_PASS if passed ADD parking pass
    SeatProduct passedParkingPass = findAddPassedParkingPass(passedProductsList);
    if (passedParkingPass != null) {

      // parking pass found flag
      boolean needToAddParkingPass = true;

      // if parking pass not supported, throw business exception (for non-sth listing)
      if (!seatProdContext.isSTHListing() &&
              !isParkingPassSupported(currentListing, seatProdContext.getListingSeatTraitMgr())) {

        ListingError listingError = new ListingError(
                ErrorType.INPUTERROR,
                ErrorCode.PARKING_PASS_NOT_SUPPORTED,
                "Barcode pass not supported by event",
                "parkingPass");
        throw new ListingBusinessException(listingError);
      }

      // find if there is one already existing
      TicketSeat curParkingPass = seatProdContext.getCurrentParkingPass();
      if (curParkingPass != null) {
        passedParkingPass.setSeatId(curParkingPass.getTicketSeatId());    // get the ticketSeatId into new parking pass
        needToAddParkingPass = false;
      }

      // add it to DB and get its ticketSeatId
      if (needToAddParkingPass) {
        TicketSeat parkingPass = TicketSeatUtils.createParkingTicketSeat(currentListing);

        // add to seats cache
        seatProdContext.addTicketSeat(parkingPass);

        seatProdContext.addToNewlyAddedTicketSeatList(parkingPass);
        passedParkingPass.setSeatId(parkingPass.getTicketSeatId());

        // set db changed indicator
        seatProdContext.setIsDBTicketsChanged();
      }
      // SEANOTE: set seat trait parking pass always if does not exist (this is now handled in listingSeatTraitsHelper)

      // add barcode pass to change ticket list (only for BARCODE listing)
      if (seatProdContext.getCurrentListing().getTicketMedium().intValue() == TicketMedium.BARCODE.getValue()) {
        seatProdContext.addArtifactSeatProductToList(passedParkingPass);
      }
    }

    // (3) process ADD and UPDATE operations
    if (seatProdContext.getHasUpdOperation() || seatProdContext.getHasAddOperation()) {
      processAddOrUpdate(seatProdContext, currentListing, ticketSeatsFromDB, passedProductsList);
    }

    // validate the resulting seat list (make sure it is all consistent, piggyback, etc.)
    if (seatProdContext.getIsDBTicketsChanged()) {
      seatProdContext.validateAllSeatsAndRows();
      seatProdContext.setTicketSeatsBackInCurrentListing();
    }
  }

  private static void processDelete(Listing listing, SeatProductsContext seatProdContext, Listing currentListing, List<TicketSeat> ticketSeatsFromDB, List<SeatProduct> passedProductsList) {
    for (Iterator<SeatProduct> it = passedProductsList.iterator(); it.hasNext(); ) {
      SeatProduct sprod = it.next();
      if (!Operation.DELETE.equalsEnum(sprod.getOperation())) {
        continue;
      }
      if (ProductType.PARKING_PASS.equalsEnum(sprod.getProductType())) {
        TicketSeat deleted = deleteParkingPass(sprod, ticketSeatsFromDB, currentListing);

        if (deleted != null) {
          // set db changed indicator
          seatProdContext.setIsDBTicketsChanged();
          setToPendingLock(currentListing);
        }
        continue;
      }

      TicketSeat ticket = findTicketSeatEqSeatProduct(sprod, ticketSeatsFromDB);
      if (ticket == null) {
        ListingError listingError = new ListingError(
                ErrorType.INPUTERROR,
                ErrorCode.LISTING_ACTION_NOTALLOWED,
                "Cannot locate seat product to delete: " + getRowSeat(sprod),
                "product");
        throw new ListingBusinessException(listingError);
      }

      it.remove();
      ticket.setSeatStatusId(4L); // 4-deleted

      // set db changed indicator
      seatProdContext.setIsDBTicketsChanged();

      // decrease quantity
      seatProdContext.updateQuantityInCurrentListing(-1);

      // delete the seat from csv seats
      if (sprod.getSeat() != null) {
        String seats = TicketSeatUtils.delFromCSVString(seatProdContext.getCurrentListing().getSeats(), sprod.getSeat());
        seatProdContext.getCurrentListing().setSeats(seats);
      }
      if (!currentListing.getSystemStatus().equals(ListingStatus.PENDING_LOCK.toString())) {
        setToPendingLock(currentListing);
      }

    }

    if (currentListing.getQuantityRemain() == 0 && !seatProdContext.getHasAddOperation()) {
      log.error("BAD Request to DELETE all the remaining seats in the listing listingId=" + currentListing.getId() + ", client=" + listing.getSubscriber());
      ListingError listingError = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.LISTING_ACTION_NOTALLOWED,
              "Please DELETE the listing instead of deleting ALL the individual seats", "");
      throw new ListingBusinessException(listingError);
    }
  }

  private static void setToPendingLock(Listing currentListing) {
    if ((TicketMedium.BARCODE.getValue() == currentListing.getTicketMedium() || TicketMedium.FLASHSEAT.getValue() == currentListing.getTicketMedium())
            && DeliveryOption.PREDELIVERY.getValue() == currentListing.getDeliveryOption()) {
      if (!currentListing.getSystemStatus().equals(ListingStatus.INCOMPLETE.toString())) {
        currentListing.setSystemStatus(ListingStatus.PENDING_LOCK.toString());
        /*SELLAPI-3243*/
        currentListing.setIsLockMessageRequired(true);
      }
    }
  }

  private static void processAddOrUpdate(SeatProductsContext seatProdContext, Listing currentListing, List<TicketSeat> ticketSeatsFromDB, List<SeatProduct> passedProductsList) {
    // if creating listing, reset quantities
    if (seatProdContext.isCreate()) {
      currentListing.setQuantity(0);
      currentListing.setQuantityRemain(0);
    }

    //SELLAPI-1011 06/24/15 START
    //UpdateListingv2 - Error when tried to update the listing with seats and fulfillment artifact
    boolean skipSeatMatch = false;
    Listing listing02 = seatProdContext.getCurrentListing();
    int passedTicketQty = 0;
    for (SeatProduct tempList : passedProductsList) {
      if (!tempList.isParkingPass()) {
        passedTicketQty++;
      }
    }
    if (seatProdContext.getHasUpdOperation() && (passedTicketQty == listing02.getQuantityRemain())) {
      skipSeatMatch = true;
      for (int k = 0; k < ticketSeatsFromDB.size(); k++) {
        TicketSeat ticketSeat = ticketSeatsFromDB.get(k);
        if (ticketSeat.getTixListTypeId() == 1 && !ticketSeat.isGeneralAdmissionInd() &&
                ticketSeat.getSeatNumber() != null &&
                ticketSeat.getSeatNumber().length() > 0) {
          skipSeatMatch = false;
        }
      }
    }
    //SELLAPI-1011 06/24/15 END
    //product list if less than one then there no chance of different FV
    boolean hasDiffFaceValue = false;
    if (passedProductsList.size() > 1) {
      Money productFaceValue = passedProductsList.get(0).getFaceValue();
      if (productFaceValue != null && productFaceValue.getAmount() != null) {
        for (SeatProduct seat : passedProductsList) {
          if ((seat.getFaceValue() != null && seat.getFaceValue().getAmount() != null)
                  && (productFaceValue.getAmount().compareTo(seat.getFaceValue().getAmount()) != 0)) {
            hasDiffFaceValue = true;
            break;
          }
        }
        if (hasDiffFaceValue) {
          currentListing.setSplitOption((short) 0);
          currentListing.setSplitQuantity(currentListing.getQuantityRemain());
        }
      }
    }

    List<TicketSeat> ticketSeatsFromDBForCheck = new ArrayList<TicketSeat>(ticketSeatsFromDB);
    for (Iterator<SeatProduct> it = passedProductsList.iterator(); it.hasNext(); ) {
      SeatProduct sprod = it.next();
      it.remove();

      //if there is no faceValue at seat level, update it with current listing faceValue
      if (currentListing.getFaceValue() != null && currentListing.getFaceValue().getAmount() != null && currentListing.getFaceValue().getAmount().doubleValue() > 0) {
        if (sprod.getFaceValue() == null || (sprod.getFaceValue()!= null && sprod.getFaceValue().getAmount() == null )) {
          sprod.setFaceValue(currentListing.getFaceValue());
        }
      }

      //SELLAPI-1011 06/24/15 START
      TicketSeat ticket;
      if (seatProdContext.isCreate() || !skipSeatMatch) {
        ticket = findTicketSeatEqSeatProduct(sprod, ticketSeatsFromDBForCheck);
      } else {
        if (sprod.isParkingPass()) {
          ticket = findParkingPassSeat(ticketSeatsFromDBForCheck);
        } else {
          TicketSeat ticketSeat = findTicketSeatByExternalId(sprod, ticketSeatsFromDBForCheck);
          ticket = ticketSeat != null ? ticketSeat : findTicketSeatByRow(sprod, ticketSeatsFromDBForCheck);
        }
      }

      if (ticket != null) {
        ticketSeatsFromDBForCheck.remove(ticket);
      }
      //SELLAPI-1011 06/24/15 END

      if (Operation.UPDATE.equalsEnum(sprod.getOperation())) {
        doUpdate(seatProdContext, sprod, ticket);
      } else if (Operation.ADD.equalsEnum(sprod.getOperation())) {
        doAdd(seatProdContext, sprod, ticketSeatsFromDB, ticket);
      }

      if (StringUtils.trimToNull(sprod.getFulfillmentArtifact()) != null) {
        if (seatProdContext.getCurrentListing().getTicketMedium().intValue() == TicketMedium.PDF.getValue()) {
          if (ticket != null) {
            ticket.setFulfillmentArtifactIds(sprod.getFulfillmentArtifact());
            seatProdContext.setIsDBTicketsChanged();
          }
        } else if (seatProdContext.getCurrentListing().getTicketMedium().intValue() == TicketMedium.BARCODE.getValue()) {
          if (!seatProdContext.addArtifactSeatProductToList(sprod)) {
            ListingError listingError = new ListingError(
                    ErrorType.INPUTERROR,
                    ErrorCode.LISTING_ACTION_NOTALLOWED,
                    "Duplicate fullfillmentArtifact for passed ticket: " + getRowSeat(sprod),
                    "fullfillmentArtifact ");
            throw new ListingBusinessException(listingError);
          } else if (seatProdContext.isCreate()) {    // set METHOD=pre-delivery if create and ticketMedium==barcode
            currentListing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
            //currentListing.setInhandDate(DateUtil.getNowCalUTC());
            currentListing.setConfirmOption(ConfirmOptionEnum.AUTO_CONFIRM.getConfirmStatus());
          }
        } else if (seatProdContext.getCurrentListing().getTicketMedium().intValue() == TicketMedium.FLASHSEAT.getValue()) {
          seatProdContext.addToFlashFulfillmentArtifactIds(sprod.getFulfillmentArtifact());
        }
      } else if (!seatProdContext.isCreate() &&
              currentListing.getDeliveryOption() == DeliveryOption.PREDELIVERY.getValue() &&
              currentListing.getTicketMedium().intValue() == TicketMedium.BARCODE.getValue() &&
              currentListing.getSystemStatus().equals(ListingStatus.ACTIVE.name())) {
        ListingError listingError = new ListingError(
                ErrorType.INPUTERROR,
                ErrorCode.INPUT_ERROR,
                "For active listing, missing fulfillmentArtifact to add / update seat information: " + getRowSeat(sprod),
                "fulfillmentArtifact");
        throw new ListingBusinessException(listingError);
      }
    }
  }

  private static void doUpdate(SeatProductsContext seatProdContext, SeatProduct seatProduct, TicketSeat ticketSeat) {
    if (ticketSeat == null) {
      ListingError listingError = new ListingError(
              ErrorType.INPUTERROR,
              ErrorCode.LISTING_ACTION_NOTALLOWED,
              "Cannot locate seat product to update: " + getRowSeat(seatProduct),
              "product");
      throw new ListingBusinessException(listingError);
    }
    if (StringUtils.trimToNull(ticketSeat.getRow()) == null && StringUtils.trimToNull(seatProduct.getRow()) != null) {
      ticketSeat.setRow(seatProduct.getRow().trim());
      seatProdContext.setIsDBTicketsChanged();
    }
    if (StringUtils.trimToNull(ticketSeat.getSeatNumber()) == null && StringUtils.trimToNull(seatProduct.getSeat()) != null) {
      ticketSeat.setSeatNumber(seatProduct.getSeat().trim());
      seatProdContext.setIsDBTicketsChanged();
    } else if (StringUtils.isNotBlank(seatProduct.getSeat()) && !isSameSeatNumber(seatProduct, ticketSeat)) {
      ticketSeat.setSeatNumber(seatProduct.getSeat().trim());
      seatProdContext.setIsDBTicketsChanged();
    }

    if (StringUtils.isNotBlank(seatProduct.getUniqueTicketNumber()) && !isSameUniqueTicketNumber(seatProduct, ticketSeat)) {
      ticketSeat.setUniqueTicketNumber(seatProduct.getUniqueTicketNumber().trim());
      seatProdContext.setIsDBTicketsChanged();
    }

    if(seatProduct.getFaceValue() != null && seatProduct.getFaceValue().getAmount() != null && seatProduct.getFaceValue().getAmount().doubleValue() > 0 ){
      if(ticketSeat.getFaceValue() == null){
        ticketSeat.setFaceValue(seatProduct.getFaceValue());
        seatProdContext.setIsDBTicketsChanged();
      }else{
        if(ticketSeat.getFaceValue().getAmount() != null && seatProduct.getFaceValue().getAmount().compareTo(ticketSeat.getFaceValue().getAmount()) != 0){
          ticketSeat.setFaceValue(seatProduct.getFaceValue());
          seatProdContext.setIsDBTicketsChanged();
        }
      }
    }
    seatProduct.setSeatId(ticketSeat.getTicketSeatId());
  }

  private static void doAdd(SeatProductsContext seatProdContext, SeatProduct seatProduct, List<TicketSeat> ticketSeatsFromDB, TicketSeat pTicketSeat) {
    TicketSeat ticketSeat = pTicketSeat;
    if (ticketSeat != null) {
      // TODO: Need to look into GA and when this is allowed
      if (StringUtils.isEmpty(seatProduct.getRow()) || StringUtils.isEmpty(seatProduct.getSeat())) {
        ListingError listingError = new ListingError(
                ErrorType.INPUTERROR,
                ErrorCode.LISTING_ACTION_NOTALLOWED,
                "Missing row / seat information to add to listing",
                "product");
        throw new ListingBusinessException(listingError);
      }
      ListingError listingError = new ListingError(
              ErrorType.INPUTERROR,
              ErrorCode.LISTING_ACTION_NOTALLOWED,
              "Cannot add a seat product that already exists: " + getRowSeat(seatProduct),
              "product");
      throw new ListingBusinessException(listingError);
    }

    // Create a mew ticket and add it to db ticket list cache (note: if isCreate currentListing == listing )
    ticketSeat = TicketSeatUtils.makeTicketSeatLike(seatProdContext.getCurrentListing(),
            TicketSeatUtils.findLikeSeat(ticketSeatsFromDB), null);

    ticketSeat.setSeatStatusId(1L);        // ticket type
    ticketSeat.setTixListTypeId(1L);    // available
    ticketSeat.setExternalSeatId(seatProduct.getExternalId());
    ticketSeat.setInventoryTypeId(seatProduct.getInventoryTypeId());
    ticketSeat.setUniqueTicketNumber(seatProduct.getUniqueTicketNumber());
    ticketSeat.setFaceValue(seatProduct.getFaceValue());
    if(seatProduct.getFaceValue() != null && seatProduct.getFaceValue().getCurrency() != null){
      ticketSeat.setCurrency(Currency.getInstance(seatProduct.getFaceValue().getCurrency()));
    }


    String seatNumber = seatProduct.getSeat();
    if (seatNumber != null) {
      seatNumber = seatNumber.trim();
    }
    ticketSeat.setSeatNumber(seatNumber);
    ticketSeat.setRow(seatProduct.getRow());

    // for GA, set GA row and seats only if no info passed
    //SELLPAI-956 10/26/15 START
    if (ticketSeat.getGeneralAdmissionInd()) {
      if (StringUtils.trimToNull(ticketSeat.getRow()) == null ||
              ticketSeat.getRow().equalsIgnoreCase(CommonConstants.GA_ROW_DESC)) {
        ticketSeat.setRow(CommonConstants.GA_ROW_DESC);
      }
      if (StringUtils.trimToNull(ticketSeat.getSeatNumber()) == null) {
        ticketSeat.setSeatNumber(null);
      }
    }
    //SELLPAI-956 10/26/15 END

    // Add to the seats cache
    seatProdContext.addTicketSeat(ticketSeat);

    // update seat ID in SeatProduct
    // TODO: IMPORTANT!!!! The ticket seat id == null because we did not add to the DB yet
    // this is needed for verifyBarcode part that should be done in batch after batch db update
    seatProduct.setSeatId(ticketSeat.getTicketSeatId());

    // set db changed indicator
    seatProdContext.setIsDBTicketsChanged();
    seatProdContext.addToNewlyAddedTicketSeatList(ticketSeat);

    // increase quantity
    seatProdContext.updateQuantityInCurrentListing(1);

    // Only for update do the list manipulation (ignore for create)
    if (!seatProdContext.isCreate()) {

      // add seat to csv seats in listing if seat from request is not empty and listing is not GA
      if (seatProduct.getSeat() != null && !seatProduct.getSeat().isEmpty() && !ticketSeat.getGeneralAdmissionInd()) {
        String seats = TicketSeatUtils.addToCSVString(seatProdContext.getCurrentListing().getSeats(), seatProduct.getSeat());
        seatProdContext.getCurrentListing().setSeats(seats);
      }

      // add unique row only if not there
      String listingRows = seatProdContext.getCurrentListing().getRow();
      if (seatProduct.getRow() != null && !seatProduct.getRow().isEmpty() && listingRows != null) {
        listingRows = TicketSeatUtils.addToCSVStringUnique(listingRows, seatProduct.getRow());
        seatProdContext.getCurrentListing().setRow(listingRows);
      }
    }
  }

  /**
   * Get rowSeat formatted in a string
   *
   * @param t
   * @return
   */
  private static String getRowSeat(SeatProduct t) {
    return "(row:" + t.getRow() + ", seat:" + t.getSeat() + ")";
  }

  /**
   * Delete parking pass
   *
   * @param pass
   * @param ticketSeatsFromDB
   * @param currentListing
   */
  private static TicketSeat deleteParkingPass(SeatProduct pass, List<TicketSeat> ticketSeatsFromDB, Listing currentListing) {
    for (int i = 0; i < ticketSeatsFromDB.size(); i++) {
      TicketSeat ts = ticketSeatsFromDB.get(i);
      if (ts.getSeatStatusId().intValue() == 1 && ts.getTixListTypeId().intValue() == 2) {    // parking-pass == 2
        ts.setSeatStatusId(4L); // 4-deleted
        currentListing.setListingType(1L);//Changing the tix_list_type_id from 3 to 1
        return ts;
      }
    }
    return null;
  }

  /**
   * This method will check if parking pass is supported or not if event is integrated
   *
   * @param listing
   * @return
   */
  private static boolean isParkingPassSupported(Listing listing, ListingSeatTraitMgr mgr) {
    Event event = listing.getEvent();
    if (event != null && event.getIsIntegrated()) {
      return mgr.isParkingSupportedForEvent(event.getId());
    }
    return true;
  }

  /**
   * findTiketSeatEqProdSeat
   *
   * @param likeSeat
   * @param ticketSeatsFromDB
   * @return
   */
  public static TicketSeat findTicketSeatEqSeatProduct(SeatProduct likeSeat, List<TicketSeat> ticketSeatsFromDB) {
    for (TicketSeat ticketSeat : ticketSeatsFromDB) {
      if (ticketSeat.getSeatStatusId().intValue() == 1) {    // if valid seat
        if (isSameExternalSeatId(likeSeat, ticketSeat) ||
                (isSameSeatNumber(likeSeat, ticketSeat) && (isRowBlankOrNA(ticketSeat) || isSameRow(likeSeat, ticketSeat)))) {
          return ticketSeat;
        }
      }
    }
    return null;
  }

  public static TicketSeat findParkingPassSeat(List<TicketSeat> ticketSeatsFromDB) {
    for (TicketSeat ticketSeat : ticketSeatsFromDB) {
      if (ticketSeat.getSeatStatusId().intValue() == 1 && ticketSeat.getTixListTypeId().intValue() == 2) {
        return ticketSeat;
      }
    }
    return null;
  }

  private static TicketSeat findTicketSeatByExternalId(SeatProduct seatProduct, List<TicketSeat> ticketSeatsFromDB) {
    for (TicketSeat ticketSeat : ticketSeatsFromDB) {
      if (ticketSeat.getSeatStatusId().intValue() == 1) {    // seat status 1 is valid seat
        String row = seatProduct.getRow();
        if (ticketSeat.isGeneralAdmissionInd() && (row == null || ("N/A".equals(row) || "GA".equals(row)))) {
          return ticketSeat;
        }
        if (isSameExternalSeatId(seatProduct, ticketSeat)) {
          return ticketSeat;
        }
      }
    }
    return null;
  }

  private static TicketSeat findTicketSeatByRow(SeatProduct seatProduct, List<TicketSeat> ticketSeatsFromDB) {
    for (TicketSeat ticketSeat : ticketSeatsFromDB) {
      if (ticketSeat.getSeatStatusId().intValue() == 1
              && ((isRowBlankOrNA(ticketSeat) || isSameRow(seatProduct, ticketSeat)))) {
        return ticketSeat;
      }
    }
    return null;
  }

  /**
   * Get ADD parking pass from barCode seat list and remove from barcodeSeatList
   *
   * @param fromProducts
   * @return BarcodeSeat parking pass found
   */
  private static SeatProduct findAddPassedParkingPass(List<SeatProduct> fromProducts) {
    if (fromProducts != null) {
      for (Iterator<SeatProduct> it = fromProducts.iterator(); it.hasNext(); ) {
        SeatProduct barSeat = it.next();
        if (barSeat.isParkingPass() && Operation.ADD.equalsEnum(barSeat.getOperation())) {
          it.remove();
          return barSeat;
        }
      }
    }
    return null;
  }

  private static boolean isSameExternalSeatId(SeatProduct seatProduct, TicketSeat ticketSeat) {
    if (StringUtils.isNotBlank(seatProduct.getExternalId())
            && StringUtils.isNotBlank(ticketSeat.getExternalSeatId())) {
      return StringUtils.equalsIgnoreCase(seatProduct.getExternalId().trim(), ticketSeat.getExternalSeatId().trim());
    }
    return false;
  }

  private static boolean isSameSeatNumber(SeatProduct seatProduct, TicketSeat ticketSeat) {
    if (StringUtils.isNotBlank(seatProduct.getSeat())
            && StringUtils.isNotBlank(ticketSeat.getSeatNumber())) {
      return StringUtils.equalsIgnoreCase(seatProduct.getSeat().trim(), ticketSeat.getSeatNumber().trim());
    }
    return false;
  }

  private static boolean isSameUniqueTicketNumber(SeatProduct seatProduct, TicketSeat ticketSeat) {
    if (StringUtils.isNotBlank(seatProduct.getUniqueTicketNumber())
            && StringUtils.isNotBlank(ticketSeat.getUniqueTicketNumber())) {
      return StringUtils.equalsIgnoreCase(seatProduct.getUniqueTicketNumber().trim(), ticketSeat.getUniqueTicketNumber().trim());
    }
    return false;
  }

  private static boolean isSameRow(SeatProduct seatProduct, TicketSeat ticketSeat) {
    if (StringUtils.isNotBlank(seatProduct.getRow())
            && StringUtils.isNotBlank(ticketSeat.getRow())) {
      return StringUtils.equalsIgnoreCase(seatProduct.getRow().trim(), ticketSeat.getRow().trim());
    }
    return false;
  }

  private static boolean isRowBlankOrNA(TicketSeat ticketSeat) {
    if (StringUtils.isBlank(ticketSeat.getRow()) || ticketSeat.getRow().equalsIgnoreCase(CommonConstants.GA_ROW_DESC)) {
      return true;
    }
    return false;
  }

  private static boolean isListingActiveOrPending(Listing listing) {
    List<String> statusList = Arrays.asList(
            ListingStatus.ACTIVE.toString(),
            ListingStatus.INACTIVE.toString(),
            ListingStatus.PENDING_LOCK.toString(),
            ListingStatus.PENDING_PDF_REVIEW.toString());

    if(listing.getSystemStatus() != null && statusList.contains(listing.getSystemStatus())) {
      return true;
    }

    return false;
  }

}