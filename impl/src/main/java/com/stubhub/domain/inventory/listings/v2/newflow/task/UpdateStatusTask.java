package com.stubhub.domain.inventory.listings.v2.newflow.task;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TaxpayerStatusEnum;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.entity.ListingCheck;
import com.stubhub.domain.inventory.listings.v2.enums.ConfirmOptionEnum;
import com.stubhub.domain.inventory.listings.v2.enums.GeneralAdmissionI18nEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.SellerInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.util.InventorySolrUtil;
import com.stubhub.domain.inventory.listings.v2.util.SellerHelper;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.newplatform.common.util.DateUtil;

@Component
@Scope("prototype")
public class UpdateStatusTask extends RegularTask {

  @Autowired
  private InventorySolrUtil inventorySolrUtil;

  @Autowired
  private InventoryMgr inventoryMgr;

  @Autowired
  private UserHelper userHelper;

  @Autowired
  private SellerHelper sellerHelper;

  private final static Logger log = LoggerFactory.getLogger(UpdateStatusTask.class);
  private static final Integer LMS_APPROVED = 2;
  public final static Integer CONFIRM_OPTION_MANUAL = 3;

  public UpdateStatusTask(ListingDTO dto) {
    super(dto);
  }

  @Override
  protected void preExecute() {}

  @Override
  protected void execute() {
    Listing dbListing = listingDTO.getDbListing();
    ListingRequest request = listingDTO.getListingRequest();
    SellerInfo sellerInfo = listingDTO.getSellerInfo();
    com.stubhub.domain.inventory.common.entity.ListingStatus listingStatus = request.getStatus();
    switch (listingStatus) {
      case INACTIVE:
        deactivateListing(request, dbListing);
        break;
      case ACTIVE:
        activateListing(request, dbListing, sellerInfo);
        break;
      case PENDING:
        updateListingStatusToPending(request, dbListing);
        break;
      case INCOMPLETE:
        dbListing.setSystemStatus(ListingStatus.INCOMPLETE.toString());
        break;
      case HIDDEN:
        updateStatusToHidden(dbListing);
        break;
      default:
    }
  }
  
  private void updateStatusToHidden(Listing dbListing) {
    if(ListingStatus.ACTIVE.toString().equals(dbListing.getSystemStatus())) {
      dbListing.setSystemStatus(ListingStatus.HIDDEN.toString());
    } else {
      log.error("message=\"Invalid listing status\" listingId={} status={}", dbListing.getId(),
          dbListing.getSystemStatus());
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingActionNotallowed,
          "Invalid listing status");
    }
  }

  private void updateListingStatusToPending(ListingRequest request, Listing dbListing) {
    if (dbListing.getTicketMedium() == TicketMedium.PDF.getValue()) {
      if (inventoryMgr.isPDFPendingReviewAllowed(dbListing.getId())) {
        dbListing.setSystemStatus(ListingStatus.PENDING_PDF_REVIEW.toString());
      } else {
        log.error("message=\"Invalid listing status\" listingId={} status={}", dbListing.getId(),
            dbListing.getSystemStatus());
        throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingActionNotallowed,
            "Invalid listing status");
      }
    } else {
      log.error("message=\"Invalid listing status\" listingId={} status={}", dbListing.getId(),
          dbListing.getSystemStatus());
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingActionNotallowed,
          "Invalid listing status");
    }
    verifyTaxPayerStatus(dbListing);
  }

  private void activateListing(ListingRequest request, Listing dbListing, SellerInfo sellerInfo) {
    validateSystemStatus(dbListing);
    validateSellerInfo(dbListing, sellerInfo);
    validatePrice(dbListing);
    validateSplitOption(dbListing);
    validateFraudCheckStatus(dbListing);

    // Check for SRS if listing being activated
    checkSRSListing(dbListing);

    if (ListingStatus.INCOMPLETE.toString().equals(dbListing.getSystemStatus())
        && (dbListing.getTicketMedium() == TicketMedium.BARCODE.getValue()
            || dbListing.getTicketMedium() == TicketMedium.FLASHSEAT.getValue())
        && dbListing.getDeliveryOption() == DeliveryOption.PREDELIVERY.getValue()) {
      dbListing.setInhandDate(DateUtil.getNowCalUTC());
      dbListing.setSystemStatus(ListingStatus.PENDING_LOCK.toString());
      dbListing.setConfirmOption(ConfirmOptionEnum.AUTO_CONFIRM.getConfirmStatus());
    } else if (ListingStatus.INCOMPLETE.toString().equals(dbListing.getSystemStatus())
        && StringUtils.trimToNull(dbListing.getFulfillmentDeliveryMethods()) == null) {
      log.error("message=\"Listing cannot be activated without fulfillment\" listingId={}", dbListing.getId());
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingActionNotallowed,
          "Listing cannot be activated until fulfilled");
    } else {
      dbListing.setSystemStatus(ListingStatus.ACTIVE.toString());
    }

    if (dbListing.getLmsApprovalStatus() != null
        && dbListing.getLmsApprovalStatus() != LMS_APPROVED) {
      boolean isShipping = false;
      String fmDMList = dbListing.getFulfillmentDeliveryMethods();
      if (fmDMList != null) {
        isShipping = deliveryMethodsContainShipping(fmDMList);
      }
      if (!isShipping) {
        dbListing.setSystemStatus(ListingStatus.INCOMPLETE.toString());
        log.info("set listing to INCOMPLETE listingId={} due to getLmsApprovalStatus={} is not approved and not shipping", dbListing.getId(),
                dbListing.getLmsApprovalStatus());
      }
    }
  }

  private void checkSRSListing(Listing listing) {
    if (!isGA(listing)) {
      ListingCheck listingCheck =
          inventorySolrUtil.isListingExists(listing.getEventId(), listing.getSellerId(),
              listing.getSection(), listing.getRow(), listing.getSeats(), listing.getId());
      if (listingCheck.getIsListed()) {
        log.error("message=\"Duplicate SRS error\" listingId=" + listing.getId());
        throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.duplicateSectionRowSeat);
      }
    }

  }

  private void validateSystemStatus(Listing dbListing) {
    if (ListingStatus.PENDING_LOCK.toString().equals(dbListing.getSystemStatus())
        || ListingStatus.PENDING_PDF_REVIEW.toString().equals(dbListing.getSystemStatus())) {
      log.error("message=\"Invalid listing status\" listingId={} status={}", dbListing.getId(),
          dbListing.getSystemStatus());
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingActionNotallowed,
          "Invalid listing status");
    }

  }

  private void validateFraudCheckStatus(Listing dbListing) {
    if (dbListing.getFraudCheckStatusId() != null && dbListing.getFraudCheckStatusId() != 500L) {
      log.error("message=\"fraud check failed\" listingId={} fraudCheckStatus={}",
          dbListing.getId(), dbListing.getFraudCheckStatusId());
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingActionNotallowed,
          "Listing cannot be activated");
    }

  }

  private void validateSplitOption(Listing dbListing) {
    if ((dbListing.getSplitOption() == null) || (dbListing.getSplitQuantity() == null)) {
      log.error("message=\"Missing split option\" listingId={} splitOption={}", dbListing.getId(),
          dbListing.getSplitOption());
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingActionNotallowed,
          "Missing split option or quantity");
    }

  }

  private void validatePrice(Listing dbListing) {
    if ((dbListing.getListPrice() == null) || (dbListing.getListPrice().getAmount() == null)
        || (dbListing.getListPrice().getAmount().doubleValue() <= 0)) {
      log.error("message=\"Invalid listing price\" listingId={} listPrice={}", dbListing.getId(),
          dbListing.getListPrice());
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingActionNotallowed,
          "Invalid listing price");
    }

  }

  private void validateSellerInfo(Listing dbListing, SellerInfo sellerInfo) {
    if (dbListing.getSellerPaymentTypeId() == null) {
      log.error("message=\"Missing seller payment type id\" listingId={} sellerPaymentTypeId={}",
          dbListing.getId(), dbListing.getSellerPaymentTypeId());
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingActionNotallowed,
          "Missing seller payment type id");
    }

    if (dbListing.getSellerContactId() == null) {
      log.error("message=\"Missing seller contact id\" listingId={} sellerContactId={}",
          dbListing.getId(), dbListing.getSellerContactId());
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingActionNotallowed,
          "Missing seller contact id");
    }
    
    if(!ListingStatus.HIDDEN.toString().equals(dbListing.getSystemStatus())) {
      if ((dbListing.getSellerCCId() == null) || (dbListing.getSellerCCId().longValue() == 48411)
          || (dbListing.getAllsellerPaymentInstruments() == null && !userHelper
              .isSellerCCIdValid(sellerInfo.getSellerGuid(), dbListing.getSellerCCId()))) {
        log.error("message=\"Invalid seller cc id\" listingId={} sellerCcId={}", dbListing.getId(),
            dbListing.getSellerCCId());
        throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingActionNotallowed,
            "Invalid seller cc id");
      }
    }

  }

  private void deactivateListing(ListingRequest request, Listing dbListing) {
    if (ListingStatus.PENDING_LOCK.toString().equals(dbListing.getSystemStatus())
        || ListingStatus.INCOMPLETE.toString().equals(dbListing.getSystemStatus())
        || ListingStatus.PENDING_PDF_REVIEW.toString().equals(dbListing.getSystemStatus())) {
      log.error("message=\"Invalid listing status\" listingId={} status={}", dbListing.getId(),
          dbListing.getSystemStatus());
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingActionNotallowed,
          "Invalid listing status");
    }

    dbListing.setSystemStatus(ListingStatus.INACTIVE.toString());
  }

  private boolean isGA(Listing currentListing) {
    boolean isGA = false;
    if (GeneralAdmissionI18nEnum.isGA(currentListing.getSection())) {
      isGA = true;
    } else if (!StringUtils.isBlank(currentListing.getSeats())) {
      String patternString = "^GA\\d+.*";
      Pattern pattern = Pattern.compile(patternString);
      Matcher matcher = pattern.matcher(currentListing.getSeats());
      if (matcher.matches()) {
        isGA = true;
      }
    }
    return isGA;
  }

  /**
   * verifyTaxPayerStatus
   * 
   * @param listing
   * @throws ListingException
   */
  private void verifyTaxPayerStatus(Listing listing) throws ListingException {
    try {
      sellerHelper.populateSellerDetails(listing);
    } catch (com.stubhub.domain.inventory.common.util.ListingException e) {
      log.error(
          "message=\"ListingException occured when populating seller details\" listingId={}, sellerId={}",
          listing.getId(), listing.getSellerId(), e);
    }
    if (listing.getTaxpayerStatus() != null
        && TaxpayerStatusEnum.TINRequired.getStatus().equalsIgnoreCase(listing.getTaxpayerStatus())
        || TaxpayerStatusEnum.TINInvalid.getStatus()
            .equalsIgnoreCase(listing.getTaxpayerStatus())) {
      log.error("message=\"Invalid tax payer status\"  taxPayerStatus={}",
          listing.getTaxpayerStatus());
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingActionNotallowed,
          "Invalid tax payer status");
    }
  }

  // Ideally we should not be relying on fm_dm_list for evaluating the availability of any
  // fulfillment window
  private boolean deliveryMethodsContainShipping(String fmDMList) {
    if (fmDMList.contains("|10,") || fmDMList.contains("|11,") || fmDMList.contains("|12,")) {
      return true;
    } else if (fmDMList.startsWith("10,") || fmDMList.startsWith("11,")
        || fmDMList.startsWith("12,")) {
      return true;

    } else {
      return false;
    }
  }

  @Override
  protected void postExecute() {}

}
