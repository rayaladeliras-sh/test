package com.stubhub.domain.inventory.listings.v2.newflow.validator;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.RequestValidationException;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.TicketTrait;

@Component
@Scope("prototype")
public class TicketTraitsRequestValidator extends RequestValidator {

  private final static Logger log = LoggerFactory.getLogger(TicketTraitsRequestValidator.class);

  private static final String SEAT_TRAIT_ID_MEMBER = "15880";
  private static final String SEAT_TRAIT_NAME_MEMBER =
      "You need to be a member to buy a ticket in this section";

  public TicketTraitsRequestValidator(ListingRequest listingRequest) {
    super(listingRequest);
  }


  @Override
  public void validate() {
    List<TicketTrait> ticketTraits = listingRequest.getTicketTraits();
    for (TicketTrait ticketTrait : ticketTraits) {
      if (isPartnerTicketsCategory(ticketTrait.getId(), ticketTrait.getName())) {
        log.error("message=\"Restricted ticket trait in the request\" listingId={}",
            listingRequest.getListingId());
        throw new RequestValidationException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidTicketTrait,
            "Invalid ticket traits in the request");
      }
    }

  }

  private boolean isPartnerTicketsCategory(String id, String name) {
    if (id != null) {
      if (id.equals(SEAT_TRAIT_ID_MEMBER)) {
        return true;
      }
    }
    if (name != null) {
      if (name.equalsIgnoreCase(SEAT_TRAIT_NAME_MEMBER)) {
        return true;
      }
    }
    return false;
  }

}
