package com.stubhub.domain.inventory.v2.listings.eventmapper;

import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.util.EventMappingException;
import com.stubhub.domain.inventory.v2.DTO.EventInfo;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.ShipEvent;

public class EventMapperAdaptorTest {

  @InjectMocks
  private EventMapperAdaptor eventMapperAdaptor;

  @Mock
  EventMapperResolver eventMapperResolver;

  EventInfo eventInfo;
  String userId;

  private EventMapperRequestValidator eventMapperRequestValidator =
      new EventMapperRequestValidator();;

  @BeforeMethod
  public void setup() {
    eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");
    userId = "10001";
    MockitoAnnotations.initMocks(this);
    eventMapperAdaptor.setEventMapperRequestValidator(eventMapperRequestValidator);
  }

  @Test
  public void testMapEventValidationPassed() throws Exception {
    eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");
    userId = "10001";
    Mockito.when(eventMapperResolver.resolveEvent(new Locale("US"), eventInfo, userId, null))
        .thenReturn(new ShipEvent());
    AssertJUnit.assertNotNull(eventMapperAdaptor.mapEvent(new Locale("US"), eventInfo, userId));

  }


  @Test(expectedExceptions = EventMappingException.class)
  public void testMapEventValidationFailed() throws Exception {
    eventInfo = new EventInfo();
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");
    userId = "10001";
    eventMapperAdaptor.mapEvent(new Locale("US"), eventInfo, userId);
  }



  @Test(expectedExceptions = EventMappingException.class)
  public void testMapEventExceptionScenario() throws Exception {
    EventInfo eventInfo2 = new EventInfo();
    eventInfo2.setName("Test Event 2");
    eventInfo2.setVenue("Test Venue 2");
    eventInfo.setDate("2016-10-10");
    when(eventMapperResolver.resolveEvent(new Locale("US"), eventInfo2, userId, null))
        .thenThrow(Exception.class);
    eventMapperAdaptor.mapEvent(new Locale("US"), eventInfo2, userId);
  }

  @Test
  public void testMapEventNullScenario() throws Exception {
    EventInfo eventInfo2 = new EventInfo();
    eventInfo2.setName("Test Event 2");
    eventInfo2.setVenue("Test Venue 2");
    eventInfo2.setDate("2016-10-09T08:00");
    when(eventMapperResolver.resolveEvent(new Locale("US"), eventInfo2, userId, null))
        .thenReturn(null);
    eventMapperAdaptor.mapEvent(new Locale("US"), eventInfo2, userId);
  }

  @Test
  public void testMapEventUserGuidScenario() {
    String userGuid = "a14bb756ddb6e99c9061d023a388bdb";
    Mockito.when(eventMapperResolver.resolveEvent(new Locale("US"), eventInfo, userId, userGuid))
        .thenReturn(new ShipEvent());
    AssertJUnit.assertNotNull(eventMapperAdaptor.mapEvent(new Locale("US"), eventInfo, userId, userGuid));
  }
  
  @Test(expectedExceptions = Exception.class)
  public void testMapEventUserGuidExceptionScenario() {
    String userGuid = "a14bb756ddb6e99c9061d023a388bdb";
    Mockito.when(eventMapperResolver.resolveEvent(new Locale("US"), eventInfo, userId, userGuid))
        .thenThrow(Exception.class);
    eventMapperAdaptor.mapEvent(new Locale("US"), eventInfo, userId, userGuid);

  }
}
