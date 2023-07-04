package com.stubhub.domain.inventory.listings.v2.newflow.task;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.infrastructure.config.client.core.SHConfig;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;

public class UpdateFaceValueTaskTest {

  @Mock
  private ListingDTO listingDTO;

  @Mock
  private MasterStubhubPropertiesWrapper masterStubhubPropertiesWrapper;
  
  @Mock
  private SHConfig shConfig;

  @InjectMocks
  private UpdateFaceValueTask updateFaceValueTask = new UpdateFaceValueTask(listingDTO);

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    String FACE_VALUE_COUNTRIES = "GB,LU,AT,DE";
    when(masterStubhubPropertiesWrapper.getProperty(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(FACE_VALUE_COUNTRIES);
    when(shConfig.getProperty("canada.site.switch", Boolean.class, false)).thenReturn(true);
  }

  @Test
  public void testUpdateFaceValueSucess() {
    Listing dbListing = new Listing();
    dbListing.setFaceValue(new Money("10", "USD"));
    when(listingDTO.getDbListing()).thenReturn(dbListing);

    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setFaceValue(new Money("12", "USD"));
    when(listingDTO.getListingRequest()).thenReturn(listingRequest);

    updateFaceValueTask.call();
    Assert.assertEquals(dbListing.getFaceValue().getAmount().compareTo(new BigDecimal(12.00)), 0);
  }

  @Test(expectedExceptions = ListingException.class)
  public void testUpdateFaceValueError() {
    Listing dbListing = new Listing();
    dbListing.setFaceValue(new Money("10", "GBP"));
    Event event = new Event();
    event.setCountry("GB");
    dbListing.setEvent(event);
    when(listingDTO.getDbListing()).thenReturn(dbListing);

    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setFaceValue(new Money("0", "GBP"));
    when(listingDTO.getListingRequest()).thenReturn(listingRequest);

    updateFaceValueTask.call();
  }

  @Test(expectedExceptions = ListingException.class)
  public void testUpdateFaceValueCurrencyError() {
    Listing dbListing = new Listing();
    dbListing.setFaceValue(new Money("10", "GBP"));
    dbListing.setCurrency(Currency.getInstance("GBP"));
    Event event = new Event();
    event.setCountry("GB");
    dbListing.setEvent(event);
    when(listingDTO.getDbListing()).thenReturn(dbListing);

    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setFaceValue(new Money("12", "USD"));
    when(listingDTO.getListingRequest()).thenReturn(listingRequest);

    updateFaceValueTask.call();
  }

  @Test
  public void testUpdateFaceValueZero() {
    Listing dbListing = new Listing();
    dbListing.setFaceValue(new Money("10", "USD"));
    dbListing.setEventId(1234567890l);
    Event event = new Event();
    event.setCountry("US");
    dbListing.setEvent(event);
    when(listingDTO.getDbListing()).thenReturn(dbListing);

    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setFaceValue(new Money("0", "USD"));
    when(listingDTO.getListingRequest()).thenReturn(listingRequest);

    updateFaceValueTask.call();
    Assert.assertNull(dbListing.getFaceValue());
  }

}
