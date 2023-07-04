package com.stubhub.domain.inventory.listings.v2.newflow.task;

import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.enums.TicketMedium;
import com.stubhub.newplatform.common.entity.Money;

public class DeleteListingTaskTest {

  @InjectMocks
  private DeleteListingTask deleteListingTask;

  @BeforeMethod
  public void setup() {
    ListingRequest request = new ListingRequest();
    request.setPricePerProduct(new Money("49.99", "USD"));
    request.setTicketMedium(TicketMedium.BARCODE);
    ListingDTO listingDTO = new ListingDTO(request);
    listingDTO.setStatus(ListingStatus.ACTIVE);
    listingDTO.setDbListing(new Listing());
    deleteListingTask = new DeleteListingTask(listingDTO);
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testExecuteSuccess() {
    ListingDTO dto = deleteListingTask.call();
    Assert.assertEquals(dto.getDbListing().getSystemStatus(), ListingStatus.DELETED.name());
  }
}
