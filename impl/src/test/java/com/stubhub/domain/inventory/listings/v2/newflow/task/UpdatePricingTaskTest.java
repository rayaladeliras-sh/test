package com.stubhub.domain.inventory.listings.v2.newflow.task;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Currency;
import java.util.Locale;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stubhub.domain.inventory.common.util.JsonUtil;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.HeaderInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.SellerInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.util.UpdatePricingUtil;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.enums.TicketMedium;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponse;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponseList;
import com.stubhub.newplatform.common.entity.Money;

public class UpdatePricingTaskTest {

  @Mock
  private UpdatePricingUtil updatePricingUtil;

  @Mock
  private ListingDTO listingDTO;

  @InjectMocks
  private UpdatePricingTask updatePricingTask = new UpdatePricingTask(listingDTO);

  @BeforeMethod
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testUpdatePricingTaskSuccess()
      throws JsonParseException, JsonMappingException, IOException {
    when(updatePricingUtil.getPriceResponse(listingDTO)).thenReturn(getPriceResponse());
    when(listingDTO.getListingRequest()).thenReturn(getRequest());
    Listing dbListing = getListingDTO().getDbListing();
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    updatePricingTask.call();
    assertEquals(dbListing.getSellFeeDescription(), "Seller Fee");
  }

  @Test
  public void testUpdatePricingTaskCurrencyError()
      throws JsonParseException, JsonMappingException, IOException {
    when(updatePricingUtil.getPriceResponse(listingDTO)).thenReturn(getPriceResponse());
    ListingRequest request = getRequestWrongCurrency();
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = getListingDTO().getDbListing();
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    try {
      updatePricingTask.call();

    } catch (ListingException e) {
      assertEquals(e.getErrorCodeEnum(), ErrorCodeEnum.invalidCurrency);
    }

    request.setPricePerProduct(null);
    request.setPayoutPerProduct(new Money("49.99", "GBP"));
    try {
      updatePricingTask.call();

    } catch (ListingException e) {
      assertEquals(e.getErrorCodeEnum(), ErrorCodeEnum.invalidCurrency);
    }

    request.setPricePerProduct(null);
    request.setPayoutPerProduct(null);
    request.setBuyerSeesPerProduct(new Money("49.99", "GBP"));
    try {
      updatePricingTask.call();

    } catch (ListingException e) {
      assertEquals(e.getErrorCodeEnum(), ErrorCodeEnum.invalidCurrency);
    }
  }

  @Test
  public void testUpdatePricingTaskPayoutPriceSuccess()
      throws JsonParseException, JsonMappingException, IOException {
    when(updatePricingUtil.getPriceResponse(listingDTO)).thenReturn(getPriceResponse());
    ListingRequest request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = getListingDTO().getDbListing();
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    updatePricingTask.call();
    assertEquals(dbListing.getSellFeeDescription(), "Seller Fee");
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
    dbListing.setQuantity(1);
    dbListing.setCurrency(Currency.getInstance(Locale.US));
    dbListing.setDeliveryOption(2);
    dbListing.setFulfillmentDeliveryMethods(
        "10,22,5.25,,2017-09-19T19:00:00Z|10,23,5.25,,2017-09-19T19:00:00Z|10,24,5.25,,2017-09-14T19:00:00Z");
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    dto.setSellerInfo(sellerInfo);
    dto.setDbListing(dbListing);
    HeaderInfo headerInfo = new HeaderInfo();
    headerInfo.setSubscriber("Single|V2|Api_UK_sell_buyer20|DefaultApplication");
    headerInfo.setClientIp("10.10.10.10");
    dto.setHeaderInfo(headerInfo);
    return dto;
  }

  private PriceResponse getPriceResponse()
      throws JsonParseException, JsonMappingException, IOException {
    final String jsonResponse =
        "{\"priceResponseList\":{\"priceResponse\":[{\"requestKey\":\"0\",\"displayPrice\":{\"amount\":65.24,\"currency\":\"USD\"},\"listingPrice\":{\"amount\":49.99,\"currency\":\"USD\"},\"payout\":{\"amount\":44.99,\"currency\":\"USD\"},\"buyFees\":{\"buyFee\":{\"amount\":10.00,\"currency\":\"USD\"},\"vatBuyFee\":{\"amount\":0.0000,\"currency\":\"USD\"},\"totalBuyFee\":{\"amount\":10.00,\"currency\":\"USD\"}},\"deliveryFees\":{\"deliveryFee\":{\"amount\":5.25,\"currency\":\"USD\"},\"vatDeliveryFee\":{\"amount\":0.00,\"currency\":\"USD\"},\"totalDeliveryFee\":{\"amount\":5.25,\"currency\":\"USD\"}},\"sellFees\":{\"sellFee\":{\"amount\":5.00,\"currency\":\"USD\"},\"vatSellFee\":{\"amount\":0.00,\"currency\":\"USD\"},\"totalSellFee\":{\"amount\":5.00,\"currency\":\"USD\"}},\"maxListingPrice\":{\"amount\":-1.00,\"currency\":\"USD\"},\"bundledType\":{\"id\":\"2\",\"description\":\"LIST_PRICE_PLUS_SERVICE_DELIVERY_FEE\"}}]}}";

    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    PriceResponseList priceResponseList =
        (PriceResponseList) JsonUtil.toObjectWrapRoot(jsonResponse, PriceResponseList.class);

    return priceResponseList.getPriceResponse().get(0);
  }

  private ListingRequest getRequest() {
    ListingRequest request = new ListingRequest();
    request.setPricePerProduct(new Money("49.99", "USD"));
    request.setTicketMedium(TicketMedium.BARCODE);
    return request;
  }

  private ListingRequest getRequestWrongCurrency() {
    ListingRequest request = new ListingRequest();
    request.setPricePerProduct(new Money("49.99", "GBP"));
    return request;
  }

}
