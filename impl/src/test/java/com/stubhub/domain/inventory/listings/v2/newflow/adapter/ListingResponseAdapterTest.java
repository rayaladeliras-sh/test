package com.stubhub.domain.inventory.listings.v2.newflow.adapter;

import java.util.Calendar;

import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.newplatform.common.entity.Money;

public class ListingResponseAdapterTest {

  @InjectMocks
  private ListingResponseAdapter listingResponseAdapter;

  @BeforeMethod
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testConvertToListingPendingResponseSucess() {
    Listing listing = new Listing();
    listing.setId(123L);
    listing.setExternalId("extID");
    listing.setSystemStatus("PENDING PDF REVIEW");
    listing.setListPrice(new Money("14"));
    ListingResponse listingResponse = listingResponseAdapter.convertToListingResponse(listing);
    Assert.assertEquals(listingResponse.getStatus(),
        com.stubhub.domain.inventory.common.entity.ListingStatus.PENDING);
    Assert.assertEquals(listingResponse.getId(), "123");
    Assert.assertEquals(listingResponse.getExternalListingId(), "extID");
  }

  @Test
  public void testConvertToListingNotPendingResponseSucess() {
    Listing listing = new Listing();
    listing.setId(123L);
    listing.setExternalId("extID");
    listing.setSystemStatus("ACTIVE");
    listing.setListPrice(new Money("14"));
    ListingResponse listingResponse = listingResponseAdapter.convertToListingResponse(listing);
    Assert.assertEquals(listingResponse.getStatus(), ListingStatus.ACTIVE);
    Assert.assertEquals(listingResponse.getId(), "123");
    Assert.assertEquals(listingResponse.getExternalListingId(), "extID");
  }

  @Test
  public void testConvertToListingResponseSucessAdjustPrice() {
    Listing listing = new Listing();
    listing.setId(123L);
    listing.setExternalId("extID");
    listing.setSystemStatus("ACTIVE");
    listing.setPriceAdjusted(true);
    listing.setListPrice(new Money("14"));
    ListingResponse listingResponse = listingResponseAdapter.convertToListingResponse(listing);
    Assert.assertEquals(listingResponse.getStatus(), ListingStatus.ACTIVE);
    Assert.assertEquals(listingResponse.getId(), "123");
    Assert.assertEquals(listingResponse.getExternalListingId(), "extID");
    Assert.assertNotNull(listingResponse.getPricePerProduct());
  }
  
  @Test
  public void testConvertToListingResponseSucessAdjustInhandDate() {
    Listing listing = new Listing();
    listing.setId(123L);
    listing.setExternalId("extID");
    listing.setSystemStatus("ACTIVE");
    listing.setInhandDate(Calendar.getInstance());
    listing.setInHandDateAdjusted(true);
    ListingResponse listingResponse = listingResponseAdapter.convertToListingResponse(listing);
    Assert.assertEquals(listingResponse.getStatus(), ListingStatus.ACTIVE);
    Assert.assertEquals(listingResponse.getId(), "123");
    Assert.assertNotNull(listingResponse.getInhandDate());
  }

}
