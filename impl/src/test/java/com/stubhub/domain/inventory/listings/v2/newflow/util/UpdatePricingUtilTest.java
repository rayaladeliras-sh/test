package com.stubhub.domain.inventory.listings.v2.newflow.util;


import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.JsonUtil;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.FulfillmentMethod;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.SellerInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.PricingHelper;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.enums.TicketMedium;
import com.stubhub.domain.pricing.intf.aip.v1.error.Error;
import com.stubhub.domain.pricing.intf.aip.v1.request.PriceRequestList;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponse;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponseList;
import com.stubhub.newplatform.common.entity.Money;

public class UpdatePricingUtilTest {

  @Mock
  private PricingHelper pricingHelper;

  @InjectMocks
  private UpdatePricingUtil updatePricingUtil = new UpdatePricingUtil();

  @BeforeMethod
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @SuppressWarnings("unchecked")
  @Test(priority = 1)
  public void testGetPriceResponse() throws JsonParseException, JsonMappingException, IOException {
    when(pricingHelper.getListingAIPPricings(any(PriceRequestList.class)))
        .thenReturn(getPriceResponseList());
    when(pricingHelper.transform(Mockito.any(Object[].class), Mockito.anyList()))
        .thenReturn(getPriceResponses());
    PriceResponse response = updatePricingUtil.getPriceResponse(getListingDTO());
    assertNotNull(response);
    double listPrice = response.getListingPrice().getAmount().doubleValue();
    double expected = 49.99d;
    assertEquals(listPrice, expected);
  }

  @SuppressWarnings("unchecked")
  @Test(priority = 2)
  public void testGetPriceResponseBarcodePredelivery()
      throws JsonParseException, JsonMappingException, IOException {
    when(pricingHelper.getListingAIPPricings(any(PriceRequestList.class)))
        .thenReturn(getPriceResponseList());
    when(pricingHelper.transform(Mockito.any(Object[].class), Mockito.anyList()))
        .thenReturn(getPriceResponses());
    ListingDTO listingDTO = getListingDTO();
    listingDTO.getDbListing().setDeliveryOption(1);
    listingDTO.getDbListing().setTicketMedium(3);
    PriceResponse response = updatePricingUtil.getPriceResponse(listingDTO);
    assertNotNull(response);
    double listPrice = response.getListingPrice().getAmount().doubleValue();
    double expected = 49.99d;
    assertEquals(listPrice, expected);
  }

  @SuppressWarnings("unchecked")
  @Test(priority = 3)
  public void testGetPriceResponsePDF()
      throws JsonParseException, JsonMappingException, IOException {
    when(pricingHelper.getListingAIPPricings(any(PriceRequestList.class)))
        .thenReturn(getPriceResponseList());
    when(pricingHelper.transform(Mockito.any(Object[].class), Mockito.anyList()))
        .thenReturn(getPriceResponses());
    ListingDTO listingDTO = getListingDTO();
    listingDTO.getDbListing().setDeliveryOption(1);
    listingDTO.getDbListing().setTicketMedium(2);
    PriceResponse response = updatePricingUtil.getPriceResponse(listingDTO);
    assertNotNull(response);
    double listPrice = response.getListingPrice().getAmount().doubleValue();
    double expected = 49.99d;
    assertEquals(listPrice, expected);
  }

  @SuppressWarnings("unchecked")
  @Test(priority = 4)
  public void testGetPriceResponseFlash()
      throws JsonParseException, JsonMappingException, IOException {
    when(pricingHelper.getListingAIPPricings(any(PriceRequestList.class)))
        .thenReturn(getPriceResponseList());
    when(pricingHelper.transform(Mockito.any(Object[].class), Mockito.anyList()))
        .thenReturn(getPriceResponses());
    ListingDTO listingDTO = getListingDTO();
    listingDTO.getDbListing().setDeliveryOption(1);
    listingDTO.getDbListing().setTicketMedium(4);
    PriceResponse response = updatePricingUtil.getPriceResponse(listingDTO);
    assertNotNull(response);
    double listPrice = response.getListingPrice().getAmount().doubleValue();
    double expected = 49.99d;
    assertEquals(listPrice, expected);
  }

  @SuppressWarnings("unchecked")
  @Test(priority = 5)
  public void testGetPriceResponseLMSPredelivery()
      throws JsonParseException, JsonMappingException, IOException {
    when(pricingHelper.getListingAIPPricings(any(PriceRequestList.class)))
        .thenReturn(getPriceResponseList());
    when(pricingHelper.transform(Mockito.any(Object[].class), Mockito.anyList()))
        .thenReturn(getPriceResponses());
    PriceResponse response = updatePricingUtil.getPriceResponse(getListingDTOLMSPreDelivery());
    assertNotNull(response);
    double listPrice = response.getListingPrice().getAmount().doubleValue();
    double expected = 49.99d;
    assertEquals(listPrice, expected);
  }

  @SuppressWarnings("unchecked")
  @Test(priority = 6)
  public void testGetPriceResponseOtherPredelivery()
      throws JsonParseException, JsonMappingException, IOException {
    when(pricingHelper.getListingAIPPricings(any(PriceRequestList.class)))
        .thenReturn(getPriceResponseList());
    when(pricingHelper.transform(Mockito.any(Object[].class), Mockito.anyList()))
        .thenReturn(getPriceResponses());
    ListingDTO dto = getListingDTO();
    dto.getDbListing().setTicketMedium(1);
    dto.getDbListing().setFulfillmentMethod(FulfillmentMethod.OTHERPREDELIVERY);
    PriceResponse response = updatePricingUtil.getPriceResponse(dto);
    assertNotNull(response);
    double listPrice = response.getListingPrice().getAmount().doubleValue();
    double expected = 49.99d;
    assertEquals(listPrice, expected);
  }

  @SuppressWarnings("unchecked")
  @Test(priority = 7)
  public void testGetPriceResponseLMS()
      throws JsonParseException, JsonMappingException, IOException {
    when(pricingHelper.getListingAIPPricings(any(PriceRequestList.class)))
        .thenReturn(getPriceResponseList());
    when(pricingHelper.transform(Mockito.any(Object[].class), Mockito.anyList()))
        .thenReturn(getPriceResponses());
    PriceResponse response = updatePricingUtil.getPriceResponse(getListingDTOLMS());
    assertNotNull(response);
    double listPrice = response.getListingPrice().getAmount().doubleValue();
    double expected = 49.99d;
    assertEquals(listPrice, expected);
  }

  @SuppressWarnings("unchecked")
  @Test(priority = 8)
  public void testGetPriceResponseShipping()
      throws JsonParseException, JsonMappingException, IOException {
    when(pricingHelper.getListingAIPPricings(any(PriceRequestList.class)))
        .thenReturn(getPriceResponseList());
    when(pricingHelper.transform(Mockito.any(Object[].class), Mockito.anyList()))
        .thenReturn(getPriceResponses());
    PriceResponse response = updatePricingUtil.getPriceResponse(getListingDTOShipping());
    assertNotNull(response);
    double listPrice = response.getListingPrice().getAmount().doubleValue();
    double expected = 49.99d;
    assertEquals(listPrice, expected);
  }


  @Test(priority = 9)
  public void testListingBusinessException() {
    try {
      updatePricingUtil.getPriceResponse(null);
    } catch (ListingException le) {
      assertNotNull(le.getType());
      assertEquals(ErrorType.SYSTEMERROR, le.getType());
      assertTrue(le.getCustomMessage().equals("System errors encountered"));
    }
  }
  
  @SuppressWarnings("unchecked")
  @Test(priority = 10)
  public void testGetPriceResponseWithMinPriceError()
      throws JsonParseException, JsonMappingException, IOException {
    when(pricingHelper.getListingAIPPricings(any(PriceRequestList.class)))
        .thenReturn(getPriceResponseList());
    when(pricingHelper.transform(Mockito.any(Object[].class), Mockito.anyList()))
        .thenReturn(getPriceResponsesWithMinPriceError());
    try {
      updatePricingUtil.getPriceResponse(getListingDTO());
    } catch (ListingException e) {
      assertEquals(e.getErrorCodeEnum(), ErrorCodeEnum.listingPriceTooLow);
    }

  }
  
  @SuppressWarnings("unchecked")
  @Test(priority = 11)
  public void testGetPriceResponseWithErrorPayoutTrue()
      throws JsonParseException, JsonMappingException, IOException {
    when(pricingHelper.getListingAIPPricings(any(PriceRequestList.class)))
        .thenReturn(getPriceResponseList());
    when(pricingHelper.transform(Mockito.any(Object[].class), Mockito.anyList()))
        .thenReturn(getPriceResponsesWithMinPriceError());
    
    PriceResponse response;
    try {
      response=updatePricingUtil.getPriceResponse(getListingDTOMinPrice());
      response.getListingPrice();
    } catch (ListingException e) {
      assertEquals(e.getErrorCodeEnum(), ErrorCodeEnum.pricingApiError);
    }

  }
  
  @SuppressWarnings("unchecked")
  @Test(priority = 12)
  public void testGetPriceResponseWithMaxPriceError()
      throws JsonParseException, JsonMappingException, IOException {
    when(pricingHelper.getListingAIPPricings(any(PriceRequestList.class)))
        .thenReturn(getPriceResponseList());
    when(pricingHelper.transform(Mockito.any(Object[].class), Mockito.anyList()))
        .thenReturn(getPriceResponsesWithMaxPriceError());
    try {
      updatePricingUtil.getPriceResponse(getListingDTO());
    } catch (ListingException e) {
      assertEquals(e.getErrorCodeEnum(), ErrorCodeEnum.listingPriceTooHigh);
    }

  }
  
  @SuppressWarnings("unchecked")
  @Test(priority = 13)
  public void testGetPriceResponseError()
      throws JsonParseException, JsonMappingException, IOException {
    when(pricingHelper.getListingAIPPricings(any(PriceRequestList.class)))
        .thenReturn(getPriceResponseList());
    when(pricingHelper.transform(Mockito.any(Object[].class), Mockito.anyList()))
        .thenReturn(getPriceResponsesWithMinPriceError1());
    try {
      updatePricingUtil.getPriceResponse(getListingDTO());
    } catch (ListingException e) {
      assertEquals(e.getErrorCodeEnum(), ErrorCodeEnum.pricingApiError);
    }

  }

  private ListingDTO getListingDTO() {

    ListingRequest request = new ListingRequest();
    request.setPricePerProduct(new Money("49.99", "USD"));
    request.setTicketMedium(TicketMedium.BARCODE);

    Listing dbListing = new Listing();
    ListingDTO dto = new ListingDTO(request);
    dbListing.setId(1271578716L);
    dbListing.setEventId(1271578799L);
    dbListing.setSection("Block A");
    dbListing.setRow("22");
    dbListing.setDeliveryOption(2);
    dbListing.setFulfillmentDeliveryMethods(
        "10,22,5.25,,2017-09-19T19:00:00Z|10,23,5.25,,2017-09-19T19:00:00Z|10,24,5.25,,2017-09-14T19:00:00Z");
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    dto.setDbListing(dbListing);
    dto.setSellerInfo(sellerInfo);
    dto.setDbListing(dbListing);

    return dto;

  }
  
  private ListingDTO getListingDTOMinPrice() {

    ListingRequest request = new ListingRequest();
    request.setPricePerProduct(new Money("5.50", "USD"));
    request.setTicketMedium(TicketMedium.BARCODE);
    request.setAdjustPrice(true);

    Listing dbListing = new Listing();
    ListingDTO dto = new ListingDTO(request);
    dbListing.setId(1271578716L);
    dbListing.setEventId(1271578799L);
    dbListing.setSection("Block A");
    dbListing.setRow("22");
    dbListing.setDeliveryOption(2);
    dbListing.setFulfillmentDeliveryMethods(
        "10,22,5.25,,2017-09-19T19:00:00Z|10,23,5.25,,2017-09-19T19:00:00Z|10,24,5.25,,2017-09-14T19:00:00Z");
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    dto.setDbListing(dbListing);
    dto.setSellerInfo(sellerInfo);
    dto.setDbListing(dbListing);

    return dto;

  }

  private ListingDTO getListingDTOLMSPreDelivery() {

    ListingRequest request = new ListingRequest();
    request.setPricePerProduct(new Money("49.99", "USD"));
    request.setTicketMedium(TicketMedium.PAPER);

    Listing dbListing = new Listing();
    ListingDTO dto = new ListingDTO(request);
    dbListing.setId(1271578716L);
    dbListing.setEventId(1271578799L);
    dbListing.setSection("Block A");
    dbListing.setRow("22");
    dbListing.setDeliveryOption(2);
    dbListing.setLmsApprovalStatus(2);
    dbListing.setFulfillmentDeliveryMethods(
        "9,22,5.25,,2017-09-19T19:00:00Z|9,23,5.25,,2017-09-19T19:00:00Z|9,24,5.25,,2017-09-14T19:00:00Z");
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    dto.setDbListing(dbListing);
    dto.setSellerInfo(sellerInfo);
    dto.setDbListing(dbListing);

    return dto;

  }

  private ListingDTO getListingDTOLMS() {

    ListingRequest request = new ListingRequest();
    request.setPricePerProduct(new Money("49.99", "USD"));
    request.setTicketMedium(TicketMedium.PAPER);

    Listing dbListing = new Listing();
    ListingDTO dto = new ListingDTO(request);
    dbListing.setId(1271578716L);
    dbListing.setEventId(1271578799L);
    dbListing.setSection("Block A");
    dbListing.setRow("22");
    dbListing.setDeliveryOption(2);
    dbListing.setLmsApprovalStatus(2);
    dbListing.setFulfillmentDeliveryMethods(
        "7,22,5.25,,2017-09-19T19:00:00Z|7,23,5.25,,2017-09-19T19:00:00Z|7,24,5.25,,2017-09-14T19:00:00Z");
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    dto.setDbListing(dbListing);
    dto.setSellerInfo(sellerInfo);
    dto.setDbListing(dbListing);

    return dto;

  }

  private ListingDTO getListingDTOShipping() {

    ListingRequest request = new ListingRequest();
    request.setPricePerProduct(new Money("49.99", "USD"));
    request.setTicketMedium(TicketMedium.PAPER);

    Listing dbListing = new Listing();
    ListingDTO dto = new ListingDTO(request);
    dbListing.setId(1271578716L);
    dbListing.setEventId(1271578799L);
    dbListing.setSection("Block A");
    dbListing.setRow("22");
    dbListing.setDeliveryOption(2);
    dbListing.setLmsApprovalStatus(2);
    dbListing.setFulfillmentDeliveryMethods(
        "11,22,5.25,,2017-09-19T19:00:00Z|11,23,5.25,,2017-09-19T19:00:00Z|11,24,5.25,,2017-09-14T19:00:00Z");
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    dto.setDbListing(dbListing);
    dto.setSellerInfo(sellerInfo);
    dto.setDbListing(dbListing);

    return dto;

  }

  private PriceResponseList getPriceResponseList()
      throws JsonParseException, JsonMappingException, IOException {
    final String jsonResponse =
        "{\"priceResponseList\":{\"priceResponse\":[{\"requestKey\":\"0\",\"displayPrice\":{\"amount\":65.24,\"currency\":\"USD\"},\"listingPrice\":{\"amount\":49.99,\"currency\":\"USD\"},\"payout\":{\"amount\":44.99,\"currency\":\"USD\"},\"buyFees\":{\"buyFee\":{\"amount\":10.00,\"currency\":\"USD\"},\"vatBuyFee\":{\"amount\":0.0000,\"currency\":\"USD\"},\"totalBuyFee\":{\"amount\":10.00,\"currency\":\"USD\"}},\"deliveryFees\":{\"deliveryFee\":{\"amount\":5.25,\"currency\":\"USD\"},\"vatDeliveryFee\":{\"amount\":0.00,\"currency\":\"USD\"},\"totalDeliveryFee\":{\"amount\":5.25,\"currency\":\"USD\"}},\"sellFees\":{\"sellFee\":{\"amount\":5.00,\"currency\":\"USD\"},\"vatSellFee\":{\"amount\":0.00,\"currency\":\"USD\"},\"totalSellFee\":{\"amount\":5.00,\"currency\":\"USD\"}},\"maxListingPrice\":{\"amount\":-1.00,\"currency\":\"USD\"},\"bundledType\":{\"id\":\"2\",\"description\":\"LIST_PRICE_PLUS_SERVICE_DELIVERY_FEE\"}}]}}";

    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    PriceResponseList priceResponseList =
        (PriceResponseList) JsonUtil.toObjectWrapRoot(jsonResponse, PriceResponseList.class);

    return priceResponseList;
  }

  private Object[] getPriceResponses()
      throws JsonParseException, JsonMappingException, IOException {
    PriceResponseList list = getPriceResponseList();
    Object[] priceResponses = new Object[list.getPriceResponse().size()];
    int idx = 0;
    for (PriceResponse priceResponse : list.getPriceResponse()) {
      priceResponses[idx] = priceResponse;
      idx++;
    }
    return priceResponses;
  }

  private PriceResponseList getPriceErrorResponseList() {
    ListingError le = new ListingError();
    le.setType(ErrorType.BUSINESSERROR);
    le.setErrorCode(ErrorCode.LISTING_PRICE_TOO_LOW.toString());
    le.setMessage(
        "Minimum listing price error:expected minimum listing price = Money [amount=6, currency=USD]");
    PriceResponseList priceResponseList = new PriceResponseList();
    List<PriceResponse> prList = new ArrayList<>();

    PriceResponse pr = new PriceResponse();
    Error error = new Error();
    error.setCode(com.stubhub.domain.pricing.intf.aip.v1.error.ErrorCode.MINIMUM_LIST_PRICE_ERROR);
    error.setType(ErrorType.BUSINESSERROR);
    error.setMessage(
        "Minimum listing price error:expected minimum listing price = Money [amount=6, currency=USD]");
    error.setParameter("Minimum listing price error:expected minimum listing price = Money [amount=6, currency=USD]");
    List<Error> errors = new ArrayList<>();
    errors.add(error);
    pr.setErrors(errors);
    prList.add(pr);
    priceResponseList.setPriceResponse(prList);
    return priceResponseList;
  }

  private Object[] getPriceResponsesWithMinPriceError() {
    PriceResponseList list = getPriceErrorResponseList();
    Object[] priceResponses = new Object[list.getPriceResponse().size()];
    List<PriceResponse> prList = list.getPriceResponse();
    int idx = 0;
    for (PriceResponse priceResponse : prList) {
      if (priceResponses[idx] != null) {
        idx++;
      }
      if (priceResponse.getErrors() != null && priceResponse.getErrors().size() > 0) {
        priceResponses[idx] = _listingError(priceResponse);
      } else {
        priceResponses[idx] = priceResponse;
      }
      idx++;
    }
    return priceResponses;
  }
  private Object[] getPriceResponsesWithMinPriceError1() {
    PriceResponseList list = getMaxPriceErrorResponseList1();
    Object[] priceResponses = new Object[list.getPriceResponse().size()];
    List<PriceResponse> prList = list.getPriceResponse();
    int idx = 0;
    for (PriceResponse priceResponse : prList) {
      if (priceResponses[idx] != null) {
        idx++;
      }
      if (priceResponse.getErrors() != null && priceResponse.getErrors().size() > 0) {
        priceResponses[idx] = _listingError(priceResponse);
      } else {
        priceResponses[idx] = priceResponse;
      }
      idx++;
    }
    return priceResponses;
  }
  
  private PriceResponseList getMaxPriceErrorResponseList() {
    ListingError le = new ListingError();
    le.setType(ErrorType.BUSINESSERROR);
    le.setErrorCode(ErrorCode.LISTING_PRICE_TOO_LOW.toString());
    le.setMessage(
        "Minimum listing price error:expected minimum listing price = Money [amount=6, currency=USD]");
    PriceResponseList priceResponseList = new PriceResponseList();
    List<PriceResponse> prList = new ArrayList<>();

    PriceResponse pr = new PriceResponse();
    Error error = new Error();
    error.setCode(com.stubhub.domain.pricing.intf.aip.v1.error.ErrorCode.MAXIMUM_LIST_PRICE_ERROR);
    error.setType(ErrorType.BUSINESSERROR);
    error.setMessage(
        "Maximum listing price error:The club has limited the price for this match to €=130.0");
    error.setParameter("Maximum listing price error:The club has limited the price for this match to €=130.0");
    List<Error> errors = new ArrayList<>();
    errors.add(error);
    pr.setErrors(errors);
    prList.add(pr);
    priceResponseList.setPriceResponse(prList);
    return priceResponseList;
  }
  
  private PriceResponseList getMaxPriceErrorResponseList1() {
    ListingError le = new ListingError();
    le.setType(ErrorType.BUSINESSERROR);
    le.setErrorCode(ErrorCode.INVALID_DISPLAY_PRICEPERTICKET.toString());
    le.setMessage(
        "Minimum listing price error:expected minimum listing price = Money [amount=6, currency=USD]");
    PriceResponseList priceResponseList = new PriceResponseList();
    List<PriceResponse> prList = new ArrayList<>();

    PriceResponse pr = new PriceResponse();
    Error error = new Error();
    error.setCode(com.stubhub.domain.pricing.intf.aip.v1.error.ErrorCode.INVALID_FULFILLMENT_TYPE);
    error.setType(ErrorType.BUSINESSERROR);
    error.setMessage(
        "Maximum listing price error:The club has limited the price for this match to €=130.0");
    error.setParameter("Maximum listing price error:The club has limited the price for this match to €=130.0");
    List<Error> errors = new ArrayList<>();
    errors.add(error);
    pr.setErrors(errors);
    prList.add(pr);
    priceResponseList.setPriceResponse(prList);
    return priceResponseList;
  }
  
  private Object[] getPriceResponsesWithMaxPriceError() {
    PriceResponseList list = getMaxPriceErrorResponseList();
    Object[] priceResponses = new Object[list.getPriceResponse().size()];
    List<PriceResponse> prList = list.getPriceResponse();
    int idx = 0;
    for (PriceResponse priceResponse : prList) {
      if (priceResponses[idx] != null) {
        idx++;
      }
      if (priceResponse.getErrors() != null && priceResponse.getErrors().size() > 0) {
        priceResponses[idx] = _listingError(priceResponse);
      } else {
        priceResponses[idx] = priceResponse;
      }
      idx++;
    }
    return priceResponses;
  }


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
