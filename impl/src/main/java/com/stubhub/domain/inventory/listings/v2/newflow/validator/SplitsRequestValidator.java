package com.stubhub.domain.inventory.listings.v2.newflow.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;

@Component
@Scope("prototype")
public class SplitsRequestValidator extends RequestValidator {

  private final static Logger log = LoggerFactory.getLogger(SplitsRequestValidator.class);

  public SplitsRequestValidator(ListingRequest listingRequest) {
    super(listingRequest);
  }

  @Override
  public void validate() {
    if (listingRequest.getSplitQuantity() < 1) {
      log.error("message=\"Invalid split quantity\" listingId={}", listingRequest.getListingId());
      throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidSplitValue);
    }
  }
}
