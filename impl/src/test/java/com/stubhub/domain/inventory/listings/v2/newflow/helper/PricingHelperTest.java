package com.stubhub.domain.inventory.listings.v2.newflow.helper;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.dao.BulkUploadSellerDAO;
import com.stubhub.domain.inventory.datamodel.entity.BulkUploadSeller;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.SellerInfo;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.pricing.intf.aip.v1.error.Error;
import com.stubhub.domain.pricing.intf.aip.v1.error.ErrorCode;
import com.stubhub.domain.pricing.intf.aip.v1.request.PriceRequest;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponse;
import com.stubhub.newplatform.common.entity.Money;

public class PricingHelperTest {

  @InjectMocks
  private PricingHelper pricingHelper;

  @Mock
  private ListingDTO listingDTO;

  @Mock
  private ListingRequest listingRequest;

  @Mock
  private BulkUploadSellerDAO bulkUploadSellerDAO;

  @Mock
  private SellerInfo sellerInfo;

  @Mock
  private BulkUploadSeller bulkSeller;

  @BeforeTest
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testTransformSuccess() {
    List<PriceResponse> priceResponses = getSinglePriceResponse();

    Object[] objects = pricingHelper.transform(new Object[priceResponses.size()], priceResponses);

    assertNotNull(objects);
    assertTrue(objects[0] instanceof PriceResponse);

    PriceResponse response = (PriceResponse) objects[0];
    assertNotNull(response);
    assertEquals(response.getDisplayPrice().getAmount().doubleValue(), 11.50);
    assertEquals(response.getListingPrice().getAmount().doubleValue(), 10.00);
  }

  @Test
  public void testTransformPriceTooLow() {
    testErrorResponse(ErrorCode.MINIMUM_LIST_PRICE_ERROR, "Price is too low",
        com.stubhub.domain.inventory.common.util.ErrorCode.LISTING_PRICE_TOO_LOW);
  }

  @Test
  public void testTransformPriceTooHigh() {
    testErrorResponse(ErrorCode.MAXIMUM_LIST_PRICE_ERROR, "Price is too high",
        com.stubhub.domain.inventory.common.util.ErrorCode.LISTING_PRICE_TOO_HIGH);
  }

  @Test
  public void testTransformPriceOtherError() {
    testErrorResponse(ErrorCode.UNKNOWN_ERROR, "Unknown error",
        com.stubhub.domain.inventory.common.util.ErrorCode.PRICING_API_ERROR);
  }

  @Test
  public void testSetAmountPerTicketAndAmountTypeAndAmountTypePayoutPerProduct() {
    when(listingDTO.getListingRequest()).thenReturn(listingRequest);
    Money payoutPerProduct = new Money("20.00");
    when(listingRequest.getPayoutPerProduct()).thenReturn(payoutPerProduct);

    PriceRequest priceRequest = new PriceRequest();
    pricingHelper.setAmountPerTicketAndAmountTypeAndAmountType(listingDTO, priceRequest);

    // Verify
    assertEquals(priceRequest.getAmountType(), "PAYOUT");
    assertEquals(priceRequest.getAmountPerTicket(), payoutPerProduct);
  }

  @Test
  public void testSetAmountPerTicketAndAmountTypeAndAmountTypePayoutPerProductCalc() {
    when(listingDTO.getListingRequest()).thenReturn(listingRequest);
    when(listingDTO.getSellerInfo()).thenReturn(sellerInfo);
    Money payoutPerProduct = new Money("20.00");
    when(listingRequest.getPayoutPerProduct()).thenReturn(payoutPerProduct);
    when(listingRequest.isMarkup()).thenReturn(true);

    when(bulkUploadSellerDAO.get(anyLong())).thenReturn(null);

    PriceRequest priceRequest = new PriceRequest();
    pricingHelper.setAmountPerTicketAndAmountTypeAndAmountType(listingDTO, priceRequest);

    // Verify
    assertEquals(priceRequest.getAmountType(), "PAYOUT");
    assertEquals(priceRequest.getAmountPerTicket(), payoutPerProduct);
  }

  @Test
  public void testSetAmountPerTicketAndAmountTypeAndAmountTypePayoutPerProductCalcSeller() {
    when(listingDTO.getListingRequest()).thenReturn(listingRequest);
    when(listingDTO.getSellerInfo()).thenReturn(sellerInfo);
    Money payoutPerProduct = new Money("20.00");
    when(listingRequest.getPayoutPerProduct()).thenReturn(payoutPerProduct);
    when(listingRequest.isMarkup()).thenReturn(true);

    when(bulkUploadSellerDAO.get(anyLong())).thenReturn(bulkSeller);
    when(bulkSeller.isSkipDynamicCalculation()).thenReturn(false);

    PriceRequest priceRequest = new PriceRequest();
    pricingHelper.setAmountPerTicketAndAmountTypeAndAmountType(listingDTO, priceRequest);

    // Verify
    assertEquals(priceRequest.getAmountType(), "PAYOUT");
    when(bulkSeller.isSkipDynamicCalculation()).thenReturn(true);

    pricingHelper.setAmountPerTicketAndAmountTypeAndAmountType(listingDTO, priceRequest);

    // Verify
    assertEquals(priceRequest.getAmountType(), "LISTING_PRICE");
  }

  @Test
  public void testSetAmountPerTicketAndAmountTypeAndAmountTypeBuyerSeesPerProduct() {
    when(listingDTO.getListingRequest()).thenReturn(listingRequest);
    Money money = new Money("20.00");
    when(listingRequest.getPayoutPerProduct()).thenReturn(null);
    when(listingRequest.getBuyerSeesPerProduct()).thenReturn(money);

    PriceRequest priceRequest = new PriceRequest();
    pricingHelper.setAmountPerTicketAndAmountTypeAndAmountType(listingDTO, priceRequest);

    // Verify
    assertEquals(priceRequest.getAmountType(), "DISPLAY_PRICE");
    assertEquals(priceRequest.getAmountPerTicket(), money);
  }

  @Test
  public void testSetAmountPerTicketAndAmountTypeAndAmountTypePricePerProduct() {
    when(listingDTO.getListingRequest()).thenReturn(listingRequest);
    Money money = new Money("20.00");
    when(listingRequest.getPayoutPerProduct()).thenReturn(null);
    when(listingRequest.getBuyerSeesPerProduct()).thenReturn(null);
    when(listingRequest.getPricePerProduct()).thenReturn(money);

    PriceRequest priceRequest = new PriceRequest();
    pricingHelper.setAmountPerTicketAndAmountTypeAndAmountType(listingDTO, priceRequest);

    // Verify
    assertEquals(priceRequest.getAmountType(), "LISTING_PRICE");
    assertEquals(priceRequest.getAmountPerTicket(), money);
  }

  private void testErrorResponse(ErrorCode code, String message,
      com.stubhub.domain.inventory.common.util.ErrorCode expetcedErrorCode) {
    com.stubhub.common.exception.ErrorType type =
        com.stubhub.common.exception.ErrorType.BUSINESSERROR;
    String parameter = "data";

    List<PriceResponse> priceResponses =
        getErrorResponse(new Error(type, code, message, parameter));

    Object[] objects = pricingHelper.transform(new Object[priceResponses.size()], priceResponses);

    // Verify
    assertNotNull(objects);
    assertTrue(objects[0] instanceof ListingError);

    ListingError listingError = (ListingError) objects[0];
    assertNotNull(listingError);
    assertEquals(listingError.getType(), type);
    assertEquals(listingError.getCode(), expetcedErrorCode);
    assertEquals(listingError.getMessage(),
        getExpectedMessage(expetcedErrorCode, message, parameter));
  }

  private String getExpectedMessage(com.stubhub.domain.inventory.common.util.ErrorCode code,
      String message, String parameter) {
    if (code == com.stubhub.domain.inventory.common.util.ErrorCode.PRICING_API_ERROR) {
      return message;
    } else {
      return message + ":" + parameter;
    }
  }

  private List<PriceResponse> getSinglePriceResponse() {
    List<PriceResponse> priceResponses = new ArrayList<PriceResponse>();
    PriceResponse priceResponse = new PriceResponse();
    priceResponse.setDisplayPrice(new Money(new BigDecimal(11.50), "USD"));
    priceResponse.setListingPrice(new Money(new BigDecimal(10.00), "USD"));
    priceResponses.add(priceResponse);

    return priceResponses;
  }

  private List<PriceResponse> getErrorResponse(Error error) {
    List<Error> errors = new ArrayList<Error>();
    errors.add(error);

    PriceResponse priceResponse = new PriceResponse();
    priceResponse.setErrors(errors);

    List<PriceResponse> priceResponses = new ArrayList<PriceResponse>();
    priceResponses.add(priceResponse);

    return priceResponses;
  }
}
