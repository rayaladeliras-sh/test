package com.stubhub.domain.inventory.listings.v2.newflow.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.HeaderInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.SellerInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;

public class UpdateHideSeatsIndTaskTest {

  @Mock
  private MasterStubhubPropertiesWrapper masterStubhubPropertiesWrapper;

  private ListingDTO listingDTO = getListingDTO();

  @InjectMocks
  private UpdateHideSeatsIndTask hideSeatsIndTask = new UpdateHideSeatsIndTask(listingDTO);

  @BeforeMethod
  public void setup() {
    initMocks(this);
  }

  @Test(priority = 1)
  public void testHideSeatsIndSuccess() {
    String CHP_DEFAULT = "GB,UK,DE,FR,CA";
    when(masterStubhubPropertiesWrapper.getProperty(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(CHP_DEFAULT);
    listingDTO.getDbListing().setEvent(getEvent());
    ListingDTO dto = hideSeatsIndTask.call();
    assertNotNull(dto);
    assertEquals(dto.getDbListing().getEvent().getCountry(), "US");
  }

 /* @Test(priority = 2)
  public void testHideSeatsIndErrorCountry() {
    String CHP_DEFAULT = "GB,UK,DE,FR,CA";
    when(masterStubhubPropertiesWrapper.getProperty(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(CHP_DEFAULT);
    Event event = getEvent();
    event.setCountry("UK");
    listingDTO.getDbListing().setEvent(event);
    try {
      hideSeatsIndTask.call();
    } catch (ListingException e) {
      assertEquals(e.getCustomMessage(), "Seat hiding is prohibited in UK");
      e.printStackTrace();
    }
  }

  @Test(priority = 3)
  public void testHideSeatsIndNullEvent() {
    String CHP_DEFAULT = "GB,UK,DE,FR,CA";
    when(masterStubhubPropertiesWrapper.getProperty(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(CHP_DEFAULT);
    listingDTO.getDbListing().setEvent(null);
    try {
      hideSeatsIndTask.call();
    } catch (ListingException e) {
      assertEquals(e.getErrorCodeEnum(), ErrorCodeEnum.invalidListingid);
    }
  }
*/
  private ListingDTO getListingDTO() {

    ListingRequest request = new ListingRequest();
    request.setHideSeats(true);

    Listing dbListing = new Listing();
    ListingDTO dto = new ListingDTO(request);
    dbListing.setId(1271578716L);
    dbListing.setEventId(1271578799L);
    dbListing.setSection("Block A");
    dbListing.setRow("22");
    dbListing.setQuantity(1);
    dbListing.setCurrency(Currency.getInstance(Locale.US));
    dbListing.setDeliveryOption(2);
    dbListing.setFulfillmentDeliveryMethods(
        "10,22,5.25,,2017-09-19T19:00:00Z|10,23,5.25,,2017-09-19T19:00:00Z|10,24,5.25,,2017-09-14T19:00:00Z");
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    dto.setSellerInfo(sellerInfo);
    dto.setDbListing(dbListing);
    HeaderInfo headerInfo = new HeaderInfo();
    headerInfo.setSubscriber("Single|V2|Api_UK_sell_buyer20|DefaultApplication");
    headerInfo.setClientIp("10.10.10.10");
    dto.setHeaderInfo(headerInfo);
    return dto;
  }

  private Event getEvent() {
    Event event = new Event();
    event.setActive(true);
    event.setBookOfBusinessId(1l);
    event.setCountry("US");
    event.setCurrency(Currency.getInstance("USD"));
    event.setDescription("Wicked New York Tickets");
    event.setEventDate(Calendar.getInstance());
    event.setGaIndicator(false);
    event.setGenreParent(700154l);
    event.setGenrePath("174/700188/43992/700154/");
    event.setGeographyParent(198377l);
    event.setGeoPath("6628/201883/6629/198377/");
    event.setId(123456789l);
    event.setIsCreditToTeamAccountSupported(false);
    event.setIsEticketAllowed(false);
    event.setIsIntegrated(false);
    event.setJdkTimeZone(TimeZone.getDefault());
    event.setRowScrubbing(false);
    event.setSectionScrubbing(false);
    event.setVenueConfigId(1234l);
    event.setVenueDesc("Apollo Victoria Theatre");

    return event;
  }

}
