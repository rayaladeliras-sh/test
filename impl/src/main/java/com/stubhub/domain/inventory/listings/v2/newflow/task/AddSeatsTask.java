package com.stubhub.domain.inventory.listings.v2.newflow.task;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.TicketSeatHelper;
import com.stubhub.domain.inventory.listings.v2.util.CmaValidator;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.enums.InventoryType;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.common.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope("prototype")
public class AddSeatsTask extends RegularTask {

  private final static Long PARKING_PASS = 102L;

  public static final Logger logger = LoggerFactory.getLogger(AddSeatsTask.class);

  @Autowired
  private ListingSeatTraitMgr listingSeatTraitMgr;

  @Autowired
  TicketSeatHelper ticketSeatHelper;

  @Autowired
  private CmaValidator cmaValidator;

  private List<Product> parkingPassFromRequest = null;
  private List<Product> ticketSeatsFromRequest = null;
  private Listing dbListing = null;

  public AddSeatsTask(ListingDTO dto) {
    super(dto);
  }

  @Override
  protected void preExecute() {
    assertIsNotPredeliveryListing();
    classifyProducts();
    validateParkingPass();
    validateTicketSeats();
  }

  private void assertIsNotPredeliveryListing() {
    dbListing = listingDTO.getDbListing();
    if (DeliveryOption.PREDELIVERY.getValue() == dbListing.getDeliveryOption()
        && isListingActiveOrPending(dbListing)) {
      logger.error("message=\"BAD Request to ADD seats to a predelivered listing\", listingId={} ",
          dbListing.getId());
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingActionNotallowed,
          "Cannot add seats to a predelivered listing");
    }
  }

  @Override
  protected void execute() {
    addParkingPass();
    addTicketSeats();
  }

  @Override
  protected void postExecute() {
    ticketSeatHelper.validateSeatsAndRows(listingDTO);
    assertRowAndSeatAreCmaCompliant();
  }

  private boolean isSTHListing() {
    return listingDTO.getDbListing().getListingSource() != null
        && listingDTO.getDbListing().getListingSource().equals(8);
  }

  private boolean isParkingPassSupported() {
    Event event = listingDTO.getDbListing().getEvent();
    if (event != null && event.getIsIntegrated()) {
      return listingSeatTraitMgr.isParkingSupportedForEvent(event.getId());
    }
    return true;
  }

  private TicketSeat createParkingPass(Listing listing) {
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

  private TicketSeat createTicketSeat(Listing currentListing, TicketSeat pLikeSeat, Product product) {

    TicketSeat likeSeat = pLikeSeat;
    if (likeSeat == null) {
      likeSeat = new TicketSeat();
      likeSeat.setTixListTypeId(1L); // regular ticket
      likeSeat.setSeatStatusId(1L);
      likeSeat.setSection(currentListing.getSection());
      if (isGASection(currentListing.getSection()) || currentListing.getEvent().getGaIndicator()) {
        likeSeat.setSeatNumber(null); // seat number is null for GA listing
        likeSeat.setRow(CommonConstants.GA_ROW_DESC);
        likeSeat.setGeneralAdmissionInd(true);
      }
    }

    TicketSeat seat = new TicketSeat();
    seat.setTicketId(currentListing.getId());
    seat.setSection(likeSeat.getSection());
    seat.setSeatDesc(likeSeat.getSeatDesc());
    seat.setRow(StringUtils.trimToNull(product.getRow()));
    seat.setSeatNumber(StringUtils.trimToNull(product.getSeat()));

    if (likeSeat.getGeneralAdmissionInd() != null) {
      seat.setGeneralAdmissionInd(likeSeat.getGeneralAdmissionInd());
    } else {
      seat.setGeneralAdmissionInd(false);
    }

    seat.setSeatStatusId(1L); // ticket type
    seat.setTixListTypeId(1L); // available
    seat.setExternalSeatId(product.getExternalId());

    // for GA, set GA row and seats only if no info passed
    if (seat.getGeneralAdmissionInd()) {
      if (StringUtils.trimToNull(seat.getRow()) == null
          || seat.getRow().equalsIgnoreCase(CommonConstants.GA_ROW_DESC)) {
        seat.setRow(CommonConstants.GA_ROW_DESC);
      }
    }

    seat.setTixListTypeId(likeSeat.getTixListTypeId());
    seat.setSeatStatusId(likeSeat.getSeatStatusId());

    Money productFaceValue = product.getFaceValue();
    if (productFaceValue != null && productFaceValue.getAmount() != null) {
      seat.setFaceValue(productFaceValue);
      if (productFaceValue.getCurrency() != null) {
        seat.setCurrency(Currency.getInstance(productFaceValue.getCurrency()));
      }
    } else {
      Money currentListingFaceValue = currentListing.getFaceValue();
      if (currentListingFaceValue != null && currentListingFaceValue.getAmount() != null){
        seat.setFaceValue(currentListingFaceValue);
        if (currentListingFaceValue.getCurrency() != null) {
          seat.setCurrency(Currency.getInstance(currentListingFaceValue.getCurrency()));
        }
      }
    }

    seat.setUniqueTicketNumber(product.getUniqueTicketNumber());
    
    if (product.getInventoryType() != null) {
      if (product.getInventoryType() == InventoryType.CONSIGNMENT) {
        seat.setInventoryTypeId(com.stubhub.domain.inventory.datamodel.entity.enums.InventoryType.CONSIGNMENT.getId());
      } else if (product.getInventoryType() == InventoryType.OWNED) {
        seat.setInventoryTypeId(com.stubhub.domain.inventory.datamodel.entity.enums.InventoryType.OWNED.getId());
      } else if (product.getInventoryType() == InventoryType.SELLITNOW) {
        seat.setInventoryTypeId(com.stubhub.domain.inventory.datamodel.entity.enums.InventoryType.SELLITNOW.getId());
      }
    }
    
    Calendar utcNow = DateUtil.getNowCalUTC();
    seat.setCreatedDate(utcNow);
    seat.setLastUpdatedDate(utcNow);
    seat.setCreatedBy(CommonConstants.LISTING_API_V2);
    seat.setLastUpdatedBy(CommonConstants.LISTING_API_V2);
    return seat;
  }

  private TicketSeat findLikeSeat(List<TicketSeat> ticketSeatsFromDB) {
    if (ticketSeatsFromDB != null && ticketSeatsFromDB.size() > 0) {
      for (TicketSeat seat : ticketSeatsFromDB) {
        if (seat.getTixListTypeId() == 1l && seat.getSeatStatusId() == 1l) {
          return seat;
        }
      }
    }
    return null;
  }

  private boolean isGASection(String section) {
    return (section != null) && (section.equalsIgnoreCase(CommonConstants.GENERAL_ADMISSION));
  }

  private void updateQuantity(int deltaValue) {

    dbListing.setQuantity(dbListing.getQuantity() + deltaValue);
    dbListing.setQuantityRemain(dbListing.getQuantityRemain() + deltaValue);

    if (dbListing.getSplitOption() != null && dbListing.getSplitOption() == 0) {
      // its a no-split ticket, so update the split quantity to
      // same as the quantity
      dbListing.setSplitQuantity(dbListing.getQuantityRemain());
    }
  }

  /**
   * process the addProducts and splits between parking pass & seats
   */
  private void classifyProducts() {

    List<Product> products = listingDTO.getUpdateListingInfo().getProductInfo().getAddProducts();
    parkingPassFromRequest = new ArrayList<>();
    ticketSeatsFromRequest = new ArrayList<>();
    for (Product product : products) {
      if (product.getProductType().equals(ProductType.PARKING_PASS)) {
        parkingPassFromRequest.add(product);
      } else {
        ticketSeatsFromRequest.add(product);
      }
    }
  }

  /**
   * parking pass validation
   */
  private void validateParkingPass() {

    if (parkingPassFromRequest != null && !parkingPassFromRequest.isEmpty()) {
      if (!isSTHListing() && !isParkingPassSupported()) {
        logger.error(
            "message=\"Parking pass is not supported for this event\", eventId={} listingId={} ",
            dbListing.getEventId(), dbListing.getId());
        throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.parkingPassNotSupported);
      }
    }
    final String ERROR_MESSAGE = "cannot add a parking pass that already exists";
    if (parkingPassFromRequest != null && parkingPassFromRequest.size() > 1) {
      logger.error("message=\"Cannot add multiple parking passes to a listing\", listingId={}",
          dbListing.getId());
      throw new ListingException(ErrorType.INPUTERROR,
          ErrorCodeEnum.multipleParkingPassesNotSupported);
    } else {
      if ((parkingPassFromRequest != null && !parkingPassFromRequest.isEmpty())
          && dbListing.getListingType().equals(3L)) {
        logger.error("message=\"" + ERROR_MESSAGE + "\", listingId={} rowSeat={} ",
            dbListing.getId(), "PARKING PASS");
        throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.listingActionNotallowed,
            ERROR_MESSAGE);
      }
    }
  }

  /*
   * product validation
   */
  private void validateTicketSeats() {
    if (ticketSeatsFromRequest == null || ticketSeatsFromRequest.isEmpty()) {
      return;
    }

    assertTicketsAreNotDuplicates();
    assertFaceValueIsProvided();
  }

  private void assertRowAndSeatAreCmaCompliant() {
    if (!isCMAEvent()) {
      return;
    }

    for (Product ticketSeat : ticketSeatsFromRequest) {
      assertRowIsCmaCompliant(ticketSeat);
      assertSeatNumberIsCmaCompliant(ticketSeat);
    }
  }

  private void assertRowIsCmaCompliant(Product ticketSeat) {
    if (ticketSeat.getRow() != null && !cmaValidator.isValidRow(dbListing.getEventId(), ticketSeat, dbListing.getSection())) {
      logger.error("message=\"Row/Seat contains words that are not allowed\":\" listingId={} row={}",
              dbListing.getId(), ticketSeat.getRow());
      throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidRowWords,
              "Row/Seat contains words that are not allowed");
    }
  }

  private void assertSeatNumberIsCmaCompliant(Product ticketSeat) {
    if (ticketSeat.getSeat() != null && !cmaValidator.isValidSeat(ticketSeat.getSeat())) {
      logger.error("message=\"Row/Seat contains words that are not allowed\":\" listingId={} seat={}",
              dbListing.getId(), ticketSeat.getSeat());
      throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidRowWords,
              "Row/Seat contains words that are not allowed");
    }
  }

  private boolean isCMAEvent() {
    return null != dbListing.getEvent().getCountry() && "GB".equalsIgnoreCase(dbListing.getEvent().getCountry())
            && "GBP".equalsIgnoreCase(dbListing.getCurrency().getCurrencyCode());
  }

  private void assertTicketsAreNotDuplicates() {
    List<TicketSeat> ticketSeatsFromDBForCheck = dbListing.getTicketSeats();
    for (Product product : ticketSeatsFromRequest) {
      TicketSeat ticketSeat = ticketSeatHelper.findTicketSeatEqSeatProduct(product, ticketSeatsFromDBForCheck);
      if (ticketSeat != null) {
        final String ERROR_MESSAGE = "Cannot add a seat product that already exists";
        logger.error("message=\"" + ERROR_MESSAGE + "\", listingId={} rowSeat={} ",
            dbListing.getId(), ticketSeatHelper.getRowSeat(product));
        throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.listingActionNotallowed,
            ERROR_MESSAGE + " (" + ticketSeatHelper.getRowSeat(product) + ")");
      }
    }
  }

  private void assertFaceValueIsProvided() {
    Money faceValue = dbListing.getTicketSeats().get(0).getFaceValue();
    if(faceValue != null && dbListing.getFaceValue().getAmount() == null && listingDTO.getListingRequest().getFaceValue() == null) {
      boolean hasProductFaceValue = true;
      for (Product product : ticketSeatsFromRequest) {
        if(product.getFaceValue() == null) {
          hasProductFaceValue = false;
          break;
        }
      }
      if(!hasProductFaceValue) {
        logger.error("message=\"Face Value missing in the request and db\" listingId={}", dbListing.getId());
        throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.listingActionNotallowed,
            "Missing Face Value");
      }
    }
  }

  private void addParkingPass() {
    if (parkingPassFromRequest != null && !parkingPassFromRequest.isEmpty()) {
      TicketSeat parkingPass = createParkingPass(dbListing);
      dbListing.getTicketSeats().add(parkingPass);
      updateListingAttributesForParkingPass();
      // add parking pass trait
      addParkingPassTrait();
    }
  }

  private void updateListingAttributesForParkingPass() {
    dbListing.setListingType(3L);
    dbListing.setSplitOption((short) 0);
    dbListing.setSplitQuantity(dbListing.getQuantityRemain());
  }

  private void updateListingAttributesForTicketSeats(Product product, TicketSeat ticketSeat) {
    updateQuantity(1);

    if (product.getSeat() != null && !product.getSeat().isEmpty() && !ticketSeat.getGeneralAdmissionInd()) {
      String seats = ticketSeatHelper.addToCSVString(dbListing.getSeats(), product.getSeat());
      dbListing.setSeats(seats);
    }

    // add unique row only if not there
    String listingRows = dbListing.getRow();
    if (product.getRow() != null && !product.getRow().isEmpty() && listingRows != null) {
      listingRows = ticketSeatHelper.addToCSVStringUnique(listingRows, product.getRow());
      dbListing.setRow(listingRows);
    }
  }

  private void addParkingPassTrait() {

    List<ListingSeatTrait> seatTraits = dbListing.getSeatTraits();
    if (seatTraits == null) {
      seatTraits = new ArrayList<>();
    }
    // create parking seat trait object
    ListingSeatTrait seatTrait = ticketSeatHelper.makeListingSeatTrait(dbListing.getId(),
        PARKING_PASS, CommonConstants.LISTING_API_V2, CommonConstants.LISTING_API_V2);
    seatTraits.add(seatTrait);
    dbListing.setSeatTraits(seatTraits);
  }

  private void addTicketSeats() {

    List<TicketSeat> ticketSeatsFromDBForCheck = dbListing.getTicketSeats();
    TicketSeat likeSeat = findLikeSeat(ticketSeatsFromDBForCheck);
    for (Product product : ticketSeatsFromRequest) {
      TicketSeat ticketSeat = createTicketSeat(dbListing, likeSeat, product);

      dbListing.getTicketSeats().add(ticketSeat);
      updateListingAttributesForTicketSeats(product, ticketSeat);
    }
  }
  
  private boolean isListingActiveOrPending(Listing listing) {
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
