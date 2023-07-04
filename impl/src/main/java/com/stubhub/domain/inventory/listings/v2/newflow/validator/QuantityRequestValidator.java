package com.stubhub.domain.inventory.listings.v2.newflow.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.RequestValidationException;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;

@Component
@Scope("prototype")
public class QuantityRequestValidator extends RequestValidator {

  private final static Logger log = LoggerFactory.getLogger(QuantityRequestValidator.class);
  private static int MIN_ALLOWED_QUANTITY = 1;
  private static int MAX_ALLOWED_QUANTITY = 150;

  public QuantityRequestValidator(ListingRequest listingRequest) {
    super(listingRequest);
  }

  @Override
  public void validate() {
    Integer quantity = listingRequest.getQuantity();
    if (quantity < MIN_ALLOWED_QUANTITY) {
      log.error("message=\"INVALID QUANTITY - Quantity less than 1\" listingId={}",
          listingRequest.getListingId());
      throw new RequestValidationException(ErrorType.BUSINESSERROR, ErrorCodeEnum.invalidQuantity);
    } else if (quantity > MAX_ALLOWED_QUANTITY) {
      log.error(
          "message=\"INVALID QUANTITY - Quantity more than max allowed quantity\" maxAllowedQuantity={} listingId={}",
          MAX_ALLOWED_QUANTITY, listingRequest.getListingId());
      throw new RequestValidationException(ErrorType.BUSINESSERROR,
          ErrorCodeEnum.maxAllowedExceeded);
    } else if (listingRequest.getProducts() != null && !listingRequest.getProducts().isEmpty()) {
      log.error(
          "message=\"Error cannot pass quantity and products array (you can pass either but not both)\" listingId={}",
          listingRequest.getListingId());
      throw new RequestValidationException(ErrorType.BUSINESSERROR, ErrorCodeEnum.invalidQuantity,
          "Error cannot pass quantity and products array (you can pass either but not both)");
    }
  }
}
