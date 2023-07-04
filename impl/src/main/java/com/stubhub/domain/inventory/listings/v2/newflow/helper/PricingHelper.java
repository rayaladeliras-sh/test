package com.stubhub.domain.inventory.listings.v2.newflow.helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.dao.BulkUploadSellerDAO;
import com.stubhub.domain.inventory.datamodel.entity.BulkUploadSeller;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.FulfillmentInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.PricingInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.FulfillmentMethodEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.api.PricingApiHelper;
import com.stubhub.domain.inventory.listings.v2.util.ListingPayout;
import com.stubhub.domain.inventory.listings.v2.util.ListingPayoutUtil;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.enums.TicketMedium;
import com.stubhub.domain.pricing.intf.aip.v1.request.PriceRequest;
import com.stubhub.domain.pricing.intf.aip.v1.request.PriceRequestList;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponse;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponseList;
import com.stubhub.domain.pricing.intf.aip.v1.response.SellFees;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;

@Component
public class PricingHelper extends PricingApiHelper {

  private final static Logger log = LoggerFactory.getLogger(PricingHelper.class);
  private static final String STUBHUB_PRO = "StubHubPro";
  private static final String COMMA_DELIMITER = ",";
  private static final String PAYOUT = "PAYOUT";
  private static final String LISTING_PRICE = "LISTING_PRICE";
  private static final String DISPLAY_PRICE = "DISPLAY_PRICE";


  @Autowired
  public MasterStubhubPropertiesWrapper masterStubhubProperties;

  @Autowired
  private BulkUploadSellerDAO bulkUploadSellerDAO;

//  public PriceResponse getPriceResponse(ListingDTO listingDTO) {
//    ArrayList<ListingDTO> listingDTOs = new ArrayList<ListingDTO>();
//    listingDTOs.add(listingDTO);
//    try {
//      Object[] responses = batchPriceCalculationsAIP(listingDTOs);
//      if (responses[0] instanceof PriceResponse) {
//        PriceResponse presp = (PriceResponse) responses[0];
//        return presp;
//      } else {
//        ListingError le = (ListingError) responses[0];
//        throw new ListingBusinessException(le);
//      }
//    } catch (ListingBusinessException lbe) {
//      throw lbe;
//    } catch (Throwable th) {
//      ListingError lerr = new ListingError(ErrorType.SYSTEMERROR, ErrorCode.SYSTEM_ERROR,
//          "System errors encountered", "");
//      throw new ListingBusinessException(lerr);
//    }
//  }
//
//  public void validate(ListingDTO listingDTO, PriceResponse priceResponse) {
//    if (priceResponse.getMinListingPrice() != null) {
//      double minPostingPrice = priceResponse.getMinListingPrice().getAmount().doubleValue();
//      if (listingDTO.getPricingInfo().getListingPrice().getAmount()
//          .doubleValue() < minPostingPrice) {
//        ListingError listingError = new ListingError(ErrorType.INPUTERROR,
//            ErrorCode.LISTING_PRICE_TOO_LOW, "Price lesser than the lower limit", "pricePerTicket");
//        throw new ListingBusinessException(listingError);
//      }
//    }
//  }
//
//  public void populate(ListingDTO listingDTO, PriceResponse priceResponse) {
//    PricingInfo pricingInfo = new PricingInfo();
//    // displayPrice (Money)
//    pricingInfo.setDisplayPrice(priceResponse.getDisplayPrice());
//    // listingPrice (Money)
//    pricingInfo.setListingPrice(priceResponse.getListingPrice());
//    // payout (Money)
//    pricingInfo.setSellerPayoutPerTicket(priceResponse.getPayout());
//    // minListingPrice (Money)
//    pricingInfo.setMinPricePerTicket(priceResponse.getMinListingPrice());
//    // sellFees (Money)
//    setSellFees(pricingInfo, priceResponse.getSellFees());
//    listingDTO.setPricingInfo(pricingInfo);
//  }
//
//  private void setSellFees(PricingInfo pricingInfo, SellFees sellFees) {
//    if (sellFees != null) {
//      pricingInfo.setSellFeePerTicket(sellFees.getSellFee());
//      pricingInfo.setTotalSellFee(sellFees.getTotalSellFee());
//    }
//  }
//
//
//  private Object[] batchPriceCalculationsAIP(List<ListingDTO> listingDTOs) throws Exception {
//    PriceRequestList priceRequestList = new PriceRequestList();
//    List<PriceRequest> priceRequests = new ArrayList<PriceRequest>(listingDTOs.size());
//    int requestKeyCounter = 0;
//    for (ListingDTO listingDTO : listingDTOs) {
//      PriceRequest priceRequest = getPriceRequest(listingDTO, requestKeyCounter++);
//      priceRequests.add(priceRequest);
//    }
//    priceRequestList.setPriceRequest(priceRequests);
//    PriceResponseList priceResponseList = getListingAIPPricings(priceRequestList);
//    List<PriceResponse> priceResponses = priceResponseList.getPriceResponse();
//    return transform(new Object[listingDTOs.size()], priceResponses);
//  }
//
//  private PriceRequest getPriceRequest(ListingDTO listingDTO, int requestKeyCounter)
//      throws Exception {
//    PriceRequest priceRequest = new PriceRequest();
//    priceRequest.setListingId(null);
//    priceRequest.setRequestKey(Integer.toString(requestKeyCounter));
//    priceRequest.setEventId(listingDTO.getEventInfo().getEventId());
//    priceRequest.setFulfillmentType(
//        getFulfillmentType(listingDTO.getListingRequest(), listingDTO.getFulfillmentInfo()));
//    priceRequest.setPredeliveryType(
//        getPreDeliveryType(listingDTO.getListingRequest(), listingDTO.getFulfillmentInfo()));
//    setAmountPerTicketAndAmountTypeAndAmountType(listingDTO, priceRequest);
//    priceRequest.setSection(listingDTO.getListingRequest().getSection());
//    priceRequest.setRow(getRows(listingDTO.getListingRequest().getProducts()));
//    priceRequest.setListingSource(STUBHUB_PRO);
//    priceRequest.setIncludePayout(true);
//    priceRequest.setAdjustToMinListPrice(false);
//    priceRequest.setSellerId(listingDTO.getSellerInfo().getSellerId());
//    priceRequest.setSellerGuid(listingDTO.getSellerInfo().getSellerGuid());
//    return priceRequest;
//  }
//
  public Object[] transform(Object[] responses, List<PriceResponse> priceResponses) {
    int idx = 0;
    for (PriceResponse priceResponse : priceResponses) {
      if (responses[idx] != null) {
        idx++;
      }
      if (priceResponse.getErrors() != null && priceResponse.getErrors().size() > 0) {
        responses[idx] = _listingError(priceResponse);
      } else {
        responses[idx] = priceResponse;
      }
      idx++;
    }
    return responses;
  }

//  private ListingError listingBusinessError(PriceResponse pr) {
//    com.stubhub.domain.pricing.intf.aip.v1.error.Error e = pr.getErrors().get(0);
//    return new ListingError(ErrorType.BUSINESSERROR, ErrorCode.INPUT_ERROR, e.getMessage(),
//        e.getParameter());
//  }
//
//  private String getRows(List<Product> products) {
//    StringBuilder rows = new StringBuilder();
//    for (Product product : products) {
//      if (StringUtils.isNotBlank(product.getRow())) {
//        rows.append(product.getRow()).append(COMMA_DELIMITER);
//      }
//    }
//    return StringUtils.stripEnd(rows.toString(), COMMA_DELIMITER);
//  }

  public void setAmountPerTicketAndAmountTypeAndAmountType(ListingDTO listingDTO,
      PriceRequest priceRequest) {
    if (listingDTO.getListingRequest().getPayoutPerProduct() != null) {
      if (listingDTO.getListingRequest().isMarkup() != null
          && listingDTO.getListingRequest().isMarkup()) {
        calculateAmountFromPayout(listingDTO, priceRequest);
      } else {
        priceRequest.setAmountPerTicket(listingDTO.getListingRequest().getPayoutPerProduct());
        priceRequest.setAmountType(PAYOUT);
      }
    } else if (listingDTO.getListingRequest().getBuyerSeesPerProduct() != null) {
      priceRequest.setAmountPerTicket(listingDTO.getListingRequest().getBuyerSeesPerProduct());
      priceRequest.setAmountType(DISPLAY_PRICE);
    } else if (listingDTO.getListingRequest().getPricePerProduct() != null) {
      priceRequest.setAmountPerTicket(listingDTO.getListingRequest().getPricePerProduct());
      priceRequest.setAmountType(LISTING_PRICE);
    }
  }

  private void calculateAmountFromPayout(ListingDTO listingDTO, PriceRequest priceRequest) {
    log.info(
        "message=\"Calclating the payout. AmountPerTicket is not Null\" eventId={} listingId={} ",
        listingDTO.getListingRequest().getEventId(), listingDTO.getListingRequest().getListingId());
    BulkUploadSeller bulkSeller = bulkUploadSellerDAO.get(listingDTO.getSellerInfo().getSellerId());
    if (bulkSeller != null) {
      calculateAmountBySkipCalc(priceRequest, listingDTO.getListingRequest().getPayoutPerProduct(),
          bulkSeller);
    } else {
      priceRequest.setAmountPerTicket(listingDTO.getListingRequest().getPayoutPerProduct());
      priceRequest.setAmountType(PAYOUT);
    }
  }

  private void calculateAmountBySkipCalc(PriceRequest preq, Money amountPerTcket,
      BulkUploadSeller bulkSeller) {
    ListingPayout listingPayout = null;
    if (bulkSeller.isSkipDynamicCalculation()) {
      listingPayout = new ListingPayout(amountPerTcket, bulkSeller.getMarkUp());
      preq.setAmountPerTicket(ListingPayoutUtil.calculatePriceAfterMarkUp(listingPayout));
      preq.setAmountType(LISTING_PRICE);
    } else {
      listingPayout = new ListingPayout(amountPerTcket, bulkSeller.getMarkUp(),
          bulkSeller.getAutoBulkDefaultSellFee());
      preq.setAmountPerTicket(ListingPayoutUtil.calculateExpectedPayout(listingPayout));
      preq.setAmountType(PAYOUT);
      log.info(
          "message=\"Calculating MarkupPrice\" skipDynamicCal={} seller={} markup={} defaultSellFee={}  amountPerTcket={} calculatedAmount={}",
          bulkSeller.isSkipDynamicCalculation(), bulkSeller.getUserId(), bulkSeller.getMarkUp(),
          bulkSeller.getAutoBulkDefaultSellFee(), amountPerTcket, preq.getAmountPerTicket());
    }
  }

//  private String getFulfillmentType(ListingRequest listingRequest,
//      FulfillmentInfo fulfillmentInfo) {
//    String fulfillmentType = getFulfillmentType(fulfillmentInfo.getFulfillmentMethodEnum());
//    fulfillmentType = fulfillmentType != null ? fulfillmentType
//        : getFulfillmentType(listingRequest.getTicketMedium());
//    fulfillmentType = fulfillmentType != null ? fulfillmentType
//        : getFulfillmentType(listingRequest, getFmDMList(fulfillmentInfo));
//    return fulfillmentType;
//  }
//
//  private String getFulfillmentType(FulfillmentMethodEnum fulfillmentMethodEnum) {
//    String fulfillmentType = null;
//    switch (fulfillmentMethodEnum) {
//      case FedEx:
//        fulfillmentType = "FedEx";
//        break;
//      case PDF:
//        fulfillmentType = "Pdf";
//        break;
//      case Barcode:
//        fulfillmentType = "Barcode";
//        break;
//      case BarcodePreDeliveryNonSTH:
//        fulfillmentType = "Barcode";
//        break;
//      case LMSPreDelivery:
//        fulfillmentType = "LMS";
//        break;
//      case UPS:
//        fulfillmentType = "UPS";
//        break;
//      case RoyalMail:
//        fulfillmentType = "Shipping";
//        break;
//      case DeutschePost:
//        fulfillmentType = "Shipping";
//        break;
//      case OtherPreDelivery:
//        fulfillmentType = "Other";
//        break;
//      default:
//        break;
//    }
//    return fulfillmentType;
//  }

//  private String getFulfillmentType(TicketMedium ticketMedium) {
//    String fulfillmentType = null;
//    switch (ticketMedium) {
//      case BARCODE:
//        fulfillmentType = "Barcode";
//        break;
//      case PDF:
//        fulfillmentType = "Pdf";
//        break;
//      case FLASHSEAT:
//        fulfillmentType = "FlashSeat";
//        break;
//    }
//    return fulfillmentType;
//  }
//
//  private String getFulfillmentType(ListingRequest listingRequest, String fmDMList) {
//    String fulfillmentType = "";
//    if (fmDMList.contains("|7,") || fmDMList.startsWith("7,")) {
//      fulfillmentType = "LMS";
//    } else if (isLMSPredelivery(listingRequest, fmDMList)) {
//      fulfillmentType = "LMS";
//    } else if (fmDMList.contains("|10,") || fmDMList.startsWith("10,")) {
//      fulfillmentType = "UPS";
//    } else if (fmDMList.contains("|11,") || fmDMList.contains("|12,") || fmDMList.startsWith("11,")
//        || fmDMList.startsWith("12,")) {
//      fulfillmentType = "Shipping";
//    } else {
//      fulfillmentType = "LMS";
//    }
//    return fulfillmentType;
//  }
//
//  private String getPreDeliveryType(ListingRequest listingRequest,
//      FulfillmentInfo fulfillmentInfo) {
//    String preDeliveryType = "Manual"; // according to the pricing API it is "manual"
//    if (fulfillmentInfo.getDeliveryOptionId().equals(DeliveryOption.PREDELIVERY.getValue())
//        || FulfillmentMethodEnum.LMSPreDelivery
//            .equals(fulfillmentInfo.getFulfillmentMethodEnum().getName())
//        || FulfillmentMethodEnum.OtherPreDelivery
//            .equals(fulfillmentInfo.getFulfillmentMethodEnum().getName())
//        || isLMSPredelivery(listingRequest, getFmDMList(fulfillmentInfo))) {
//      preDeliveryType = "Predelivery";
//    }
//    return preDeliveryType;
//  }
//
//  private String getFmDMList(FulfillmentInfo fulfillmentInfo) {
//    return fulfillmentInfo.getFmDmList() == null ? "" : fulfillmentInfo.getFmDmList();
//  }
//
//  private boolean isLMSPredelivery(ListingRequest listingRequest, String fmDMList) {
//    if (fmDMList.contains("|9,") || fmDMList.startsWith("9,")) {
//      if (listingRequest.getLmsApprovalStatus() != null
//          && listingRequest.getLmsApprovalStatus() == 2) {
//        return true;
//      }
//    }
//    return false;
//  }
//
  private ListingError _listingError(PriceResponse pr) {
    com.stubhub.domain.pricing.intf.aip.v1.error.Error pe = pr.getErrors().get(0);
    String message = pe.getMessage();
    if (com.stubhub.domain.pricing.intf.aip.v1.error.ErrorCode.MINIMUM_LIST_PRICE_ERROR
        .equals(pe.getCode())) {
      message = pe.getMessage() + ":" + pe.getParameter();
    }
    if (com.stubhub.domain.pricing.intf.aip.v1.error.ErrorCode.MAXIMUM_LIST_PRICE_ERROR
        .equals(pe.getCode())) {
      message = pe.getMessage() + ":" + pe.getParameter();
    }
    ListingError listingError = new ListingError(pe.getType(),
        convertPricingToListingErrorCode(pe.getCode()), message, pe.getParameter());
    return listingError;
  }

  private ErrorCode convertPricingToListingErrorCode(
      com.stubhub.domain.pricing.intf.aip.v1.error.ErrorCode pricingErrorCode) {
    if (com.stubhub.domain.pricing.intf.aip.v1.error.ErrorCode.MINIMUM_LIST_PRICE_ERROR
        .equals(pricingErrorCode)) {
      return ErrorCode.LISTING_PRICE_TOO_LOW;
    } else if (com.stubhub.domain.pricing.intf.aip.v1.error.ErrorCode.MAXIMUM_LIST_PRICE_ERROR
        .equals(pricingErrorCode)) {
      return ErrorCode.LISTING_PRICE_TOO_HIGH;
    } else {
      return ErrorCode.PRICING_API_ERROR;
    }
  }

}
