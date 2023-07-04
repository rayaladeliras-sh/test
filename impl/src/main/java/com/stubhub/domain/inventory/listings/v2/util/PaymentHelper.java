package com.stubhub.domain.inventory.listings.v2.util;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.common.util.StringUtils;
import com.stubhub.domain.inventory.biz.v2.intf.ExternalSystemUserMgr;
import com.stubhub.domain.inventory.biz.v2.intf.UserCustRepRelMgr;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.ExternalSystemUser;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.UserCustRepRel;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.PaymentType;
import com.stubhub.domain.inventory.metadata.v1.event.util.SellerPaymentUtil;
import com.stubhub.domain.user.payments.v2.intf.CustomerPaymentInstrumentDetailsV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("paymentHelper")
public class PaymentHelper {

  private final static Logger log = LoggerFactory.getLogger(PaymentHelper.class);

  @Autowired
  private UserHelper userHelper;

  @Autowired
  private ExternalSystemUserMgr externalSystemUserMgr;

  @Autowired
  private UserCustRepRelMgr userCustRepRelMgr;

  public boolean populatePaymentDetails(Listing listing) {
    List<CustomerPaymentInstrumentDetailsV2> sellerPaymentInstruments =
        userHelper.getAllSellerPaymentInstrumentV2(listing.getSellerGuid());

    // SELLAPI-1773
    boolean isPaymentMethodRequired = true;
    if(listing.getSellerId() != null) {
      isPaymentMethodRequired = SellerPaymentUtil.isPaymentMethodRequired(listing.getSellerId().toString());
    }

    if(listing.getSellerPaymentTypeId() != null) { 
      if (listing.getSellerPaymentTypeId().equals(2L) || listing.getSellerPaymentTypeId().equals(821L)) {
        listing.setSellerPaymentTypeId(null);
      }
    }

    if (isPaymentMethodRequired || listing.getSellerPaymentTypeId() != null) {
      if (listing.getSellerPaymentTypeId() != null) {
        if (userHelper.isSellerPaymentTypeValid(listing.getSellerId(), listing.getEventId(),
            listing.getSellerPaymentTypeId())) {
          listing.setSellerPaymentTypeId(listing.getSellerPaymentTypeId());
        } else {
          ListingError listingError = new ListingError(ErrorType.BUSINESSERROR,
              ErrorCode.INVALID_PAYMENT_TYPE, "Invalid payment type", "paymentType");
          throw new ListingBusinessException(listingError);
        }
        if (!isSellerPaymentTypeValidForSeller(listing.getSellerPaymentTypeId(),
            listing.getSellerId())) {
          ListingError listingError = new ListingError(ErrorType.BUSINESSERROR,
              ErrorCode.INVALID_PAYMENT_TYPE, "Invalid payment type", "paymentType");
          throw new ListingBusinessException(listingError);
        }
      } else {
        if (sellerPaymentInstruments != null && !sellerPaymentInstruments.isEmpty()) {
        	
        	//since event info has already been populated into listing object, we just check it is null here.
          //if the bob of the listing event is null or empty then we can't determine what payment instrument to set.
          
          // Book Of Business ID retirement - change to currency code check 
          if(listing.getEvent() != null && listing.getEvent().getCurrency() != null) {
            String currencyCode = listing.getEvent().getCurrency().getCurrencyCode(); 

            for (CustomerPaymentInstrumentDetailsV2 customerPaymentInstrumentDetails : sellerPaymentInstruments) {
              if (customerPaymentInstrumentDetails != null) {
                if ("true".equalsIgnoreCase(customerPaymentInstrumentDetails.getDefaultPaymentInd())) {

                  String paymentType = customerPaymentInstrumentDetails.getPaymentType();

                  String currency = customerPaymentInstrumentDetails.getCurrency(); 

                  log.info("Default payment indicator is true for paymentType={} currencyCode={}", paymentType, currency);

                  if (paymentType != null //add thi logic for handling book of business see SPAY-1495
                          && currencyCode.toString().equals(currency)) {
                    if (paymentType.equalsIgnoreCase("paypal")
                            && "PAYABLE"
                            .equalsIgnoreCase(customerPaymentInstrumentDetails.getPaypalDetails().getMode())) {
                      listing.setSellerPaymentTypeId(PaymentType.Paypal.getType());
                      break;
                    } else if (paymentType.equalsIgnoreCase("check")) {
                      listing.setSellerPaymentTypeId(PaymentType.Check.getType());
                      break;
                    } else if (paymentType.equalsIgnoreCase("LargeSellerCheck")) {
                      listing.setSellerPaymentTypeId(PaymentType.LargeSellerCheck.getType());
                      break;
                    } else if (paymentType.equalsIgnoreCase("ACH")) {
                      listing.setSellerPaymentTypeId(PaymentType.ACH.getType());
                      break;
                    } else {
                      log.warn("paymentType={} not support", paymentType);
                    }
                  }
                } else {
                  // not default payment
                }
              }
            }
          } else {
            log.warn("skip setSellerPaymentTypeId as event not found or event's bob not found");
          }

          log.info("Listing Status before checking for payment types fromListing="
              + listing.getSystemStatus());
          log.info("listing.getSellerCCId() =" +listing.getSellerCCId());

          log.info("listing.getSellerPaymentTypeId() = {}", listing.getSellerPaymentTypeId());

          if(listing.getSellerPaymentTypeId() == null && ListingStatus.HIDDEN.toString().equals(listing.getSystemStatus())){
        	  listing.setSellerPaymentTypeId(1L);  
          }

          if (listing.getSellerPaymentTypeId() == null
                  && !ListingStatus.INCOMPLETE.toString().equals(listing.getSystemStatus())) {

            ListingError listingError = new ListingError(ErrorType.BUSINESSERROR,
                    ErrorCode.SELLER_DEFAULT_PAYMENTTYPE_NOTFOUND, "Default payment type not found",
                    "paymentType");
            throw new ListingBusinessException(listingError);
          }
        } else if (sellerPaymentInstruments == null && !ListingStatus.INCOMPLETE.toString().equals(listing.getSystemStatus())
                && !ListingStatus.HIDDEN.toString().equalsIgnoreCase(listing.getSystemStatus())) {
          ListingError listingError = new ListingError(ErrorType.BUSINESSERROR,
                  ErrorCode.SELLER_DEFAULT_PAYMENTTYPE_NOTFOUND, "Default payment type not found",
                  "paymentType");
          throw new ListingBusinessException(listingError);
        } else if (!ListingStatus.INCOMPLETE.toString().equals(listing.getSystemStatus())
                && !ListingStatus.HIDDEN.toString().equalsIgnoreCase(listing.getSystemStatus())) {
          ListingError listingError = new ListingError(ErrorType.SYSTEMERROR,
                  ErrorCode.SYSTEM_ERROR, "Issue with get seller default payment type", "paymentType");
          throw new ListingBusinessException(listingError);
        }
      }
    }
    // NOTE: If listing.getSellerCCId() != null is already mapped. We ran SellerCCIdUpdateTask
    // before this task that validates and maps it
    if (listing.getSellerCCId() != null) {
      // Long mappedCCId = userHelper.getMappedValidSellerCCId(listing.getSellerGuid(),
      // listing.getSellerCCId(), sellerPaymentInstruments);
      listing.setPaymentResourceId(listing.getSellerCCId());
    } else {
      boolean foundLastUserCC = false;
      if (sellerPaymentInstruments != null) {
        for (CustomerPaymentInstrumentDetailsV2 customerPaymentInstrumentDetails : sellerPaymentInstruments) {
          if (customerPaymentInstrumentDetails != null) {
            if (customerPaymentInstrumentDetails.getPaymentType() != null
                && customerPaymentInstrumentDetails.getPaymentType()
                    .equalsIgnoreCase("creditcard")) {
              String pId = customerPaymentInstrumentDetails.getId();
              // TODO: Need to revisit this - there is some redundancy and duplicate code
              Long mappedCCId = userHelper.getMappedValidSellerCCId(listing.getSellerGuid(), pId,
                  sellerPaymentInstruments,listing.getSystemStatus());
              log.info("listing.getSystemStatus() : " +listing.getSystemStatus());
              if (mappedCCId != null) {
                listing.setSellerCCId(mappedCCId);
                foundLastUserCC = true;
                break;
              }
            }
          }
        }
      }
      if (!foundLastUserCC) {
        if (ListingStatus.INCOMPLETE.toString().equals(listing.getSystemStatus()) || ListingStatus.HIDDEN.toString().equals(listing.getSystemStatus())) {
          listing.setSellerCCId(48411L);
          listing.setPaymentResourceId(48411L);
        } else {
          ListingError listingError = new ListingError(ErrorType.BUSINESSERROR,
              ErrorCode.SELLER_CREDIT_CARD_NOTFOUND, "Seller credit card not found", "ccId");
          throw new ListingBusinessException(listingError);
        }
      }
    }
    return true;
  }

  public boolean isSellerPaymentTypeValidForSeller(Long paymentTypeId, Long userId) {
    // CMTA validation
    if (paymentTypeId == 3l) {
      ExternalSystemUser externalSystemUser =
          externalSystemUserMgr.getExternalSystemUserByUserId(userId);
      if (externalSystemUser == null
          || (StringUtils.trimToNull(externalSystemUser.getExtAccountNumber()) == null)) {
        return false;
      }
    } else if (paymentTypeId == 821L) {
      UserCustRepRel userCustRepRel = userCustRepRelMgr.getByUserIdAndType(userId, 1l);// Type is 1
                                                                                       // for
                                                                                       // Largesellers
      if (userCustRepRel == null) {
        return false;
      }

    }
    return true;
  }
}
