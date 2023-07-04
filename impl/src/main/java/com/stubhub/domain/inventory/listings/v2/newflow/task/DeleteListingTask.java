package com.stubhub.domain.inventory.listings.v2.newflow.task;

import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class DeleteListingTask extends RegularTask {

  public DeleteListingTask(ListingDTO dto) {
    super(dto);
  }

  @Override
  protected void preExecute() {
  }

  @Override
  protected void execute() {
    listingDTO.getDbListing().setSystemStatus(ListingStatus.DELETED.toString());
  }

  @Override
  protected void postExecute() {
  }

}
