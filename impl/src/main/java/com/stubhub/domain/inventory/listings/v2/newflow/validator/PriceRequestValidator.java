package com.stubhub.domain.inventory.listings.v2.newflow.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.PriceException;
import com.stubhub.domain.inventory.listings.v2.newflow.util.ValidationUtil;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.newplatform.common.entity.Money;

@Component
@Scope("prototype")
public class PriceRequestValidator extends RequestValidator {

  private final static Logger log = LoggerFactory.getLogger(PriceRequestValidator.class);

  public PriceRequestValidator(ListingRequest listingRequest) {
    super(listingRequest);
  }

  @Override
  public void validate() {
    validatePrice(listingRequest.getPricePerProduct(), ErrorCodeEnum.invalidPriceperticket);
    validatePrice(listingRequest.getPayoutPerProduct(), ErrorCodeEnum.invalidPriceperticket);
    validatePrice(listingRequest.getBuyerSeesPerProduct(),
        ErrorCodeEnum.invalidDisplayPriceperticket);
  }

  private void validatePrice(Money price, ErrorCodeEnum errorCodeEnum) {
    if (price != null && !ValidationUtil.isValid(price)) {
      log.error("message=\"Invalid price OR payout in the request\" listingId={}",
          listingRequest.getListingId());
      throw new PriceException(ErrorType.INPUTERROR, errorCodeEnum);
    }
  }

}
