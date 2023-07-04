package com.stubhub.domain.inventory.listings.v2.newflow.task;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.util.UpdatePricingUtil;
import com.stubhub.domain.inventory.listings.v2.util.CurrencyConvertHelper;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponse;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;

@Component
@Scope("prototype")
public class UpdatePricingTask extends RegularTask {

  private static final Logger log = LoggerFactory.getLogger(UpdatePricingTask.class);
  @Autowired
  private UpdatePricingUtil updatePricingUtil;

  private PriceResponse priceResponse;

  private static final String USD_TO_CAD_CURRENCY_CONVERTION_VALUE = "usd.to.cad.conversion.value";
  
  private static final String USD_TO_CAD_CURRENCY_CONVERTION_DEFAULT = "1.3514";
  
  @Autowired
  private CurrencyConvertHelper currencyConvertHelper;

  @Autowired
  private MasterStubhubPropertiesWrapper masterStubhubProperties;
  
  public UpdatePricingTask(ListingDTO dto) {
    super(dto);
  }

  @Override
  protected void preExecute() {
    String sellerPriceType=null;
    Money sellerInputPrice=null;
    Money amountPerTicket = null;
    if (listingDTO.getListingRequest().getPayoutPerProduct() != null) {
      amountPerTicket = listingDTO.getListingRequest().getPayoutPerProduct();
      sellerPriceType = "payoutPerProduct";
      sellerInputPrice = new Money(amountPerTicket.getAmount().toString());    
    } else if (listingDTO.getListingRequest().getPricePerProduct() != null) {
      amountPerTicket = listingDTO.getListingRequest().getPricePerProduct();
      sellerPriceType = "pricePerProduct";
      sellerInputPrice = new Money(amountPerTicket.getAmount().toString());
    } else if (listingDTO.getListingRequest().getBuyerSeesPerProduct() != null) {
      amountPerTicket = listingDTO.getListingRequest().getBuyerSeesPerProduct();
      sellerPriceType="buyerSeesPerProduct";
      sellerInputPrice = new Money(amountPerTicket.getAmount().toString());
      if(listingDTO.getDbListing().getFaceValue() != null) {
        listingDTO.getDbListing().getFaceValue().setCurrency(Currency.getInstance(Locale.CANADA).toString());
      }
    }

    Listing listing = listingDTO.getDbListing();

    if ("CAD".equalsIgnoreCase(listing.getCurrency().getCurrencyCode()) && "USD".equalsIgnoreCase(amountPerTicket.getCurrency())) {
      //BigDecimal fxUsdToCadExchnage = new BigDecimal(currencyConvertHelper.convertToCadCurrency("USD","CAD"));
      BigDecimal fxUsdToCadExchnage = new BigDecimal(getUSDToCADConversionValue());
      BigDecimal currentUSDPrice = amountPerTicket.getAmount();
      BigDecimal newCadListPrice = currentUSDPrice.multiply(fxUsdToCadExchnage);
      BigDecimal sellerInputPriceAmount = currentUSDPrice;
      amountPerTicket.setAmount(newCadListPrice);
      amountPerTicket.setCurrency("CAD");
      log.info("setting new CAD price using USD Price for listingId={} USD={} CAD={}",listing.getId(),sellerInputPriceAmount,newCadListPrice);
      listing.setSellerInputPrice(sellerInputPrice);
      listing.setSellerInputPriceType(sellerPriceType);
      listing.setSellerInputCurrency(Currency.getInstance(Locale.US));
    }

    if (listing.getCurrency() != null && amountPerTicket != null
        && !(amountPerTicket.getCurrency().equals(listing.getCurrency().getCurrencyCode()))) {
      log.error(
          "message=\"Invalid currency in the request\" listingId={} inputCurrency={} eventCurrency={}",
          listing.getId(), amountPerTicket.getCurrency(), listing.getCurrency());
      throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidCurrency);
    }
  }

  private String getUSDToCADConversionValue() {
	  return masterStubhubProperties.getProperty(USD_TO_CAD_CURRENCY_CONVERTION_VALUE,USD_TO_CAD_CURRENCY_CONVERTION_DEFAULT);
  }
  
  
  @Override
  protected void execute() {
    priceResponse = updatePricingUtil.getPriceResponse(listingDTO);
  }

  @Override
  protected void postExecute() {

    Listing dbListing = listingDTO.getDbListing();

    Integer quantity = dbListing.getQuantity();
    Currency currency = dbListing.getCurrency();

    dbListing.setDisplayPricePerTicket(priceResponse.getDisplayPrice());
    dbListing.setListPrice(priceResponse.getListingPrice());
    dbListing.setMinPricePerTicket(priceResponse.getMinListingPrice());
    dbListing.setSellerPayoutAmountPerTicket(priceResponse.getPayout());
    dbListing.setSellFeeValuePerTicket(priceResponse.getSellFees().getSellFee());
    dbListing.setSellFeeDescription("Seller Fee");

    Money sellFee = priceResponse.getSellFees().getSellFee();
    BigDecimal totalSellFee =
        BigDecimal.valueOf(sellFee.getAmount().doubleValue()).multiply(new BigDecimal(quantity));
    dbListing.setTotalSellFeeValue(new Money(totalSellFee, currency.getCurrencyCode()));

    Money totalListingPrice =
        new Money(dbListing.getListPrice().getAmount().multiply(new BigDecimal(quantity)),
            dbListing.getListPrice().getCurrency());
    dbListing.setTotalListingPrice(totalListingPrice);

    Money payoutPerTicket = priceResponse.getPayout();

    if (payoutPerTicket != null && payoutPerTicket.getAmount().doubleValue() > 0) {
      BigDecimal totalPayout = BigDecimal.valueOf(payoutPerTicket.getAmount().doubleValue())
          .multiply(new BigDecimal(quantity));
      dbListing.setTotalSellerPayoutAmt(new Money(totalPayout, currency.getCurrencyCode()));
    }
  }
}