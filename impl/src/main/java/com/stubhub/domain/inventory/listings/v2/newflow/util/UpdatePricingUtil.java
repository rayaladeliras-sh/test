package com.stubhub.domain.inventory.listings.v2.newflow.util;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.FulfillmentMethod;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.PriceException;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.PricingHelper;
import com.stubhub.domain.inventory.v2.enums.TicketMedium;
import com.stubhub.domain.pricing.intf.aip.v1.request.PriceRequest;
import com.stubhub.domain.pricing.intf.aip.v1.request.PriceRequestList;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponse;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponseList;
import com.stubhub.newplatform.common.entity.Money;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdatePricingUtil {

  private static final Logger log = LoggerFactory.getLogger(UpdatePricingUtil.class);

  private static final String STUBHUB_PRO = "StubHubPro";
  private static final String RELIST = "Relist";
  private static final String INDY = "Indy";

  @Autowired
  private PricingHelper pricingHelper;

  public PriceResponse getPriceResponse(ListingDTO listingDTO) {
    ArrayList<ListingDTO> listingDTOs = new ArrayList<ListingDTO>();
    listingDTOs.add(listingDTO);
    PriceResponse presp = null;
    try {
      Object[] responses = batchPriceCalculationsAIP(listingDTOs);

      if (responses[0] instanceof PriceResponse) {
        presp = (PriceResponse) responses[0];
      } else {
        ListingError le = (ListingError) responses[0];
        if (le.getCode().equals(ErrorCode.LISTING_PRICE_TOO_LOW)) {
          if (listingDTO.getListingRequest().getAdjustPrice() != null
              && listingDTO.getListingRequest().getAdjustPrice().equals(Boolean.TRUE)) {
            presp = processErrorForMinPrice(responses, listingDTO);
            listingDTO.getDbListing().setPriceAdjusted(true);
          } else {
            throw new ListingException(le.getType(), ErrorCodeEnum.listingPriceTooLow,
                le.getMessage());
          }
        } else if (le.getCode().equals(ErrorCode.LISTING_PRICE_TOO_HIGH)) {
          throw new ListingException(le.getType(), ErrorCodeEnum.listingPriceTooHigh,
              le.getMessage());
        } else {
          throw new PriceException(le.getType(), ErrorCodeEnum.pricingApiError, le.getMessage());
        }
      }
    } catch (ListingException le) {
      throw le;
    } catch (Throwable th) {
      log.error("message=\"Error getting priceResponse\"", th);
      throw new ListingException(ErrorType.SYSTEMERROR, ErrorCodeEnum.systemError,
          "System errors encountered");
    }
    return presp;
  }

  private Object[] batchPriceCalculationsAIP(List<ListingDTO> listingDTOs) {
    PriceRequestList priceRequestList = new PriceRequestList();
    List<PriceRequest> priceRequests = new ArrayList<PriceRequest>(listingDTOs.size());

    int requestKeyCounter = 0;
    for (ListingDTO listingDTO : listingDTOs) {
      PriceRequest priceRequest = getPriceRequest(listingDTO, requestKeyCounter++);
      priceRequests.add(priceRequest);
    }
    priceRequestList.setPriceRequest(priceRequests);
    PriceResponseList priceResponseList = pricingHelper.getListingAIPPricings(priceRequestList);
    List<PriceResponse> priceResponses = priceResponseList.getPriceResponse();
    return pricingHelper.transform(new Object[listingDTOs.size()], priceResponses);
  }

  private PriceRequest getPriceRequest(ListingDTO listingDTO, int requestKeyCounter) {
    Listing dbListing = listingDTO.getDbListing();
    PriceRequest priceRequest = new PriceRequest();
    /*Need to pass listingId and ListingCreatedDate to priceingAPI for Update Call */
    priceRequest.setListingId(dbListing.getId());
    if (null != dbListing.getCreatedDate())
    {
    	DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    	String listingDate = sdf.format(dbListing.getCreatedDate().getTime());
    	priceRequest.setListingCreatedDate(listingDate);
    }
    priceRequest.setRequestKey(Integer.toString(requestKeyCounter));
    priceRequest.setEventId(dbListing.getEventId());
    setFullfillmentAndPredeliveryType(listingDTO, priceRequest);
    pricingHelper.setAmountPerTicketAndAmountTypeAndAmountType(listingDTO, priceRequest);
    if(dbListing.getFaceValue() != null) {
      priceRequest.setFaceValue(dbListing.getFaceValue());
    }
    priceRequest.setSection(dbListing.getSection());
    priceRequest.setRow(dbListing.getRow());
    if (StringUtils.trimToEmpty(dbListing.getCreatedBy()).contains(RELIST)) {
      priceRequest.setListingSource(RELIST);
    } else if(isListingSourceIndy(dbListing.getCreatedBy())){
        priceRequest.setListingSource(INDY);
    } else {
      priceRequest.setListingSource(STUBHUB_PRO);
    }

    priceRequest.setIncludePayout(true);
    priceRequest.setAdjustToMinListPrice(false);
    priceRequest.setSellerId(listingDTO.getSellerInfo().getSellerId());
    priceRequest.setSellerGuid(listingDTO.getSellerInfo().getSellerGuid());

    return priceRequest;

  }

  private void setFullfillmentAndPredeliveryType(ListingDTO listingDTO, PriceRequest priceRequest) {

    Listing listing = listingDTO.getDbListing();
    String fulfillmentType = "";
    String preDeliveryType = "Manual";

    log.info("PriceRequest=" + priceRequest.toString() +
            " listing.getTicketMedium=" + listing.getTicketMedium() +
            " listing.getFulfillmentMethod=" + listing.getFulfillmentMethod() +
            " listing.getFulfillmentDeliveryMethods=" + listing.getFulfillmentDeliveryMethods());

    if (listing.getDeliveryOption() == DeliveryOption.PREDELIVERY.getValue()) {
      preDeliveryType = "Predelivery";
    }

    if (listing.getTicketMedium() == TicketMedium.BARCODE.getId()) {
      fulfillmentType = "Barcode";
    } else if (listing.getTicketMedium() == TicketMedium.PDF.getId()) {
      fulfillmentType = "Pdf";
    } else if (listing.getTicketMedium() == TicketMedium.FLASHSEAT.getId()) {
      fulfillmentType = "FlashSeat";
    } else if (listing.getTicketMedium() == TicketMedium.EXTMOBILE.getId()) {
      fulfillmentType = "ExternalMobileTransfer";
    } else if (listing.getTicketMedium() == TicketMedium.MOBILE.getId()) {
      fulfillmentType = "Mobile";
    } else if (FulfillmentMethod.OTHERPREDELIVERY.equals(listing.getFulfillmentMethod())) {
      fulfillmentType = "Other";
      preDeliveryType = "Predelivery";
    } else {
      String fmDMList = listing.getFulfillmentDeliveryMethods() == null ? ""
          : listing.getFulfillmentDeliveryMethods();
      if (fmDMList.contains("|7,") || fmDMList.startsWith("7,")) {
        fulfillmentType = "LMS";
      } else if (isLMSPredelivery(listing, fmDMList)) {
        fulfillmentType = "LMS";
        preDeliveryType = "Predelivery";
      } else if (fmDMList.contains("|10,") || fmDMList.startsWith("10,")) {
        fulfillmentType = "UPS";
      } else if (fmDMList.contains("|11,") || fmDMList.contains("|12,")
          || fmDMList.startsWith("11,") || fmDMList.startsWith("12,")) {
        fulfillmentType = "Shipping";
      } else {
        fulfillmentType = "LMS";
      }
    }
    priceRequest.setFulfillmentType(fulfillmentType);
    priceRequest.setPredeliveryType(preDeliveryType);

  }

  private boolean isLMSPredelivery(Listing listing, String fmDMList) {
    if (fmDMList.contains("|9,") || fmDMList.startsWith("9,")) {
      if (listing.getLmsApprovalStatus() != null && listing.getLmsApprovalStatus() == 2) {
        return true;
      }
    }
    return false;
  }

  // FIXME This is currently duplicated at UpdateListingAsyncHelper2 and ListingPriceDetailsHelper.
  // Remove the duplicated method.
  private boolean isListingSourceIndy(String createdBy) {
    createdBy = StringUtils.trimToEmpty(createdBy);
    if (StringUtils.isNotEmpty(createdBy)) {
      createdBy = createdBy.toLowerCase();
      if (createdBy.contains("api_uk_sell_buyer20") || createdBy.contains("access@stubhub.com")
          || createdBy.contains("corp.ebay.com") ||
          createdBy.contains("siebel")
          || createdBy.contains("sth")) {
        log.info("_message=\"listing source is Indy\" listingsource={} createdBy={}", INDY,
            createdBy);
        return true;
      }
    }
    return false;
  }

  private PriceResponse processErrorForMinPrice(Object[] responses, ListingDTO listingDTO) {
    ListingError le = (ListingError) responses[0];
    ListingError[] errors = new ListingError[responses.length];

    for (int i = 0; i < responses.length; i++) {

      ArrayList<ListingDTO> listingDTOs = new ArrayList<ListingDTO>();
      String errorParam = le.getParameter();
      int startIndexAmt = errorParam.indexOf("amount=");
      int endIndexAmt = errorParam.indexOf(", currency");
      String amount = errorParam.substring(startIndexAmt + 7, endIndexAmt);
      int startIndexCur = errorParam.indexOf("currency=");
      int endIndexCur = errorParam.indexOf("]");
      String currency = errorParam.substring(startIndexCur + 9, endIndexCur);
      Money minPrice = new Money(amount, currency);
      listingDTO.getListingRequest().setPricePerProduct(minPrice);
      listingDTO.getListingRequest().setPayoutPerProduct(null);

      listingDTOs.add(listingDTO);
      Object[] responseList = batchPriceCalculationsAIP(listingDTOs);
      if (responseList[0] != null) {
        if (responseList[0] instanceof PriceResponse) {
          PriceResponse priceResp = (PriceResponse) responseList[0];
          errors[i] = null;
          return priceResp;
        } else {
          ListingError error = (ListingError) responseList[0];
          errors[i] = error;
          log.error("message=\"Pricing API Error encountered\" code={} message=\"{}\"",
              le.getCode(), le.getMessage());
          throw new ListingException(error.getType(), ErrorCodeEnum.pricingApiError,
              le.getMessage());
        }
      }
    }
    return null;
  }
}
