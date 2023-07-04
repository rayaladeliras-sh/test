package com.stubhub.domain.inventory.listings.v2.newflow.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.entity.SplitOption;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;

@Component
@Scope("prototype")
public class UpdateSplitTask extends RegularTask {

  private final static Logger log = LoggerFactory.getLogger(UpdateSplitTask.class);
  private final static Long AISLE = 101l;
  private final static Long PARKING_PASS = 102l;

  public UpdateSplitTask(ListingDTO dto) {
    super(dto);
  }

  @Override
  protected void preExecute() {}

  @Override
  protected void execute() {
    Listing dbListing = listingDTO.getDbListing();
    ListingRequest request = listingDTO.getListingRequest();
    boolean resetSplit = false;
    if (dbListing.getIsPiggyBack()) {
      resetSplit = true;
    }
    List<ListingSeatTrait> seatTraits = dbListing.getSeatTraits();
    if (seatTraits != null && !seatTraits.isEmpty()) {
      for (ListingSeatTrait seatTrait : seatTraits) {
        if(!seatTrait.isMarkForDelete()) {
          //TODO: we need to reset split for all the ticket features - not only aisle and pp
          if (seatTrait.getSupplementSeatTraitId().equals(AISLE) || seatTrait.getSupplementSeatTraitId().equals(PARKING_PASS)) {
            resetSplit = true;
            break;
          }
        }
      }
    }

    if (resetSplit) {
      dbListing.setSplitOption((short) 0);
      dbListing.setSplitQuantity(dbListing.getQuantityRemain());
    } else {
      if (request.getSplitOption() != null) {
        if (SplitOption.NONE.equals(request.getSplitOption())) {
          dbListing.setSplitOption((short) 0);
          dbListing.setSplitQuantity(dbListing.getQuantityRemain());
        } else if (SplitOption.MULTIPLES.equals(request.getSplitOption())) {
          dbListing.setSplitOption((short) 1);
          if (request.getSplitQuantity() != null) {
            dbListing.setSplitQuantity(request.getSplitQuantity());
          }
        } else if (SplitOption.NOSINGLES.equals(request.getSplitOption())) {
          dbListing.setSplitOption((short) 2);
          dbListing.setSplitQuantity(1);
        }

      } else if (request.getSplitQuantity() != null && dbListing.getSplitOption() == 1) {
        dbListing.setSplitQuantity(request.getSplitQuantity());
      }

      if (dbListing.getSplitOption() == 1) {
        if (dbListing.getSplitQuantity() == 3 && dbListing.getQuantityRemain() >= 3) {
          dbListing.setSplitQuantity(dbListing.getSplitQuantity());
        } else if (dbListing.getSplitQuantity() == 2 && dbListing.getQuantityRemain() >= 2) {
          dbListing.setSplitQuantity(dbListing.getSplitQuantity());
        } else if (dbListing.getQuantityRemain() % dbListing.getSplitQuantity() == 0) {
          dbListing.setSplitQuantity(dbListing.getSplitQuantity());
        } else {
          log.error("message=\"INVALID split quantity\" listingId={}", dbListing.getId());
          throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidSplitValue);
        }
      }
    }

    log.info("message=\"Split values for listing\" listingId={} splitOption={} splitQuantity={}",
        dbListing.getId(), dbListing.getSplitOption(), dbListing.getSplitQuantity());

  }

  @Override
  protected void postExecute() {}

}
