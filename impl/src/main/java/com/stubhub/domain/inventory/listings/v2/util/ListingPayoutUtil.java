package com.stubhub.domain.inventory.listings.v2.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.newplatform.common.entity.Money;


/**
 * This class calculates the price after markup  and expcted payout.
 * 
 * @author dearora
 *
 */
public class ListingPayoutUtil {

  private static final String PAYOUT_PARAM = "payout";

  /**
   * Calculate Price after mark up by using request price and markup value from bulkUpload Seller.
   * 
   * expected price = (price/ (1 - Markup)) as Markup is a %
   * 
   * @param payout ListingPayout
   * @return Money Price after Markup
   */
  public static Money calculatePriceAfterMarkUp(ListingPayout payout)
      throws ListingBusinessException {
    Money expectedListPrice = new Money();

    try {

      BigDecimal bPayout = BigDecimal.valueOf(payout.getTotalPayout().getAmount().doubleValue());
      //Converted to percentage
      Double markUpVal = payout.getMarkUp() / 100;

      //Calculate the MarkUp Divisor (1 - Markup) and roundoff with Half Up
      BigDecimal bMarkup = new BigDecimal(markUpVal, new MathContext(2, RoundingMode.HALF_UP));
     /* BigDecimal bMarkupdivisor =
          new BigDecimal(1 - bMarkup.doubleValue(), new MathContext(4, RoundingMode.HALF_UP));*/
      MathContext mc = new MathContext(4, RoundingMode.HALF_UP);
      BigDecimal bMarkupdivisor = BigDecimal.valueOf(1 - bMarkup.doubleValue());
      bMarkupdivisor = bMarkupdivisor.round(mc);
      //Divide the price with markup divisor  price(1 - Markup)
      bPayout = bPayout.divide(bMarkupdivisor, 2, RoundingMode.HALF_UP);
      // Set the payout amount to new variable with currency. 
      expectedListPrice.setAmount(bPayout);
      expectedListPrice.setCurrency(payout.getTotalPayout().getCurrency());
    } catch (Exception ex) {
      ListingError listingError =
          new ListingError(ErrorType.SYSTEMERROR, ErrorCode.SYSTEM_ERROR, ex.getMessage(), PAYOUT_PARAM);
      throw new ListingBusinessException(listingError);
    }

    return expectedListPrice;
  }

  /**
   * 
   * Calculates the Expected Payout by amount , default seller Fee and markup from ListingPayout
   * Formula for Expected Payout
   * 
   * Markup and Seller are %
   * 
   * ExpectPayout = (Price/(1-markup)) *(1 - sellFee)
   * 
   * @param payout ListingPayout having 
   * @return Money Expected Payout
   */
  public static Money calculateExpectedPayout(ListingPayout payout) throws ListingBusinessException {
    Money expectedPayout = new Money();
    try {
      BigDecimal bPayout = BigDecimal.valueOf(payout.getTotalPayout().getAmount().doubleValue());
      // Converted to percentage
      Double markUpVal = payout.getMarkUp() / 100;
      Double defaultSellerFee = payout.getDefaultSellerFee() / 100;
      BigDecimal bMarkup = new BigDecimal(markUpVal, new MathContext(2, RoundingMode.HALF_UP));
      BigDecimal bSellFee =
          new BigDecimal(defaultSellerFee, new MathContext(2, RoundingMode.HALF_UP));

      // Calculation of Markup divisor (1 - Markup) and  sellerFee multiplier (1 - sellFee)
      BigDecimal bMarkupdivisor =BigDecimal.valueOf(1 - bMarkup.doubleValue());
      bMarkupdivisor = bMarkupdivisor.setScale(4, RoundingMode.HALF_UP);
      
      BigDecimal bSellFeeMultiplier =BigDecimal.valueOf(1 - bSellFee.doubleValue());
      bSellFeeMultiplier = bSellFeeMultiplier.setScale(4, RoundingMode.HALF_UP);
      
      //Calculate (Price/(1-markup)) and multiple by sellFeeMultiplier (1 - sellFee) and round it to ceil
      bPayout = bPayout.divide(bMarkupdivisor, 0, RoundingMode.CEILING);
      bPayout = bPayout.multiply(bSellFeeMultiplier);
      
   // Set the expected payout amount to new variable with currency. 
      expectedPayout.setAmount(bPayout);
      expectedPayout.setCurrency(payout.getTotalPayout().getCurrency());
    } catch (Exception ex) {
      throw new ListingBusinessException(new ListingError(ErrorType.SYSTEMERROR, ErrorCode.SYSTEM_ERROR, ex.getMessage(), PAYOUT_PARAM));
    }
    return expectedPayout;

  }

}
