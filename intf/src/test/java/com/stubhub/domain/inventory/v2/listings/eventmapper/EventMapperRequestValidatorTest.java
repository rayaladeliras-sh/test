package com.stubhub.domain.inventory.v2.listings.eventmapper;

import java.util.Locale;

import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.util.EventMappingException;
import com.stubhub.domain.inventory.v2.DTO.EventInfo;

public class EventMapperRequestValidatorTest {

  @InjectMocks
  private EventMapperRequestValidator eventMapperRequestValidator;
  Locale locale;
  String userId;


  @BeforeMethod
  public void setup() {
    locale = new Locale("US");
    userId = "10001";

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testValidate_NoError() {
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");
    eventMapperRequestValidator.validate(eventInfo);
  }

  @Test(expectedExceptions = EventMappingException.class)
  public void testValidate_VenueNull() {
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setDate("2016-10-09T08:00");
    eventMapperRequestValidator.validate(eventInfo);

  }

  @Test(expectedExceptions = EventMappingException.class)
  public void testValidate_NameNull() {
    EventInfo eventInfo = new EventInfo();
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");
    eventMapperRequestValidator.validate(eventInfo);

  }

  @Test
  public void testValidate_LocalDateNullAndUTCPresent() {
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");
    eventMapperRequestValidator.validate(eventInfo);

  }

  @Test
  public void testValidate_UTCDateNullAndLocalDatePresent() {
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setEventLocalDate("2016-10-09T08:00");
    eventMapperRequestValidator.validate(eventInfo);

  }

  @Test(expectedExceptions = EventMappingException.class)
  public void testValidate_UTCDateNullAndLocalDateNull() {
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventMapperRequestValidator.validate(eventInfo);

  }

  @Test(expectedExceptions = EventMappingException.class)
  public void testValidate_UTC_Invalidate() {
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016/10/09T08:00");
    eventMapperRequestValidator.validate(eventInfo);

  }

  @Test(expectedExceptions = EventMappingException.class)
  public void testValidate_Local_Invalidate() {
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016/10/09T08:00");
    eventMapperRequestValidator.validate(eventInfo);

  }
}
