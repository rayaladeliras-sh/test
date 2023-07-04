package com.stubhub.domain.inventory.v2.listings.eventmapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.util.EventMappingException;
import com.stubhub.domain.inventory.v2.DTO.EventInfo;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.ShipEvent;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.Venue;

public class EventMapperExactDateTimeMatcherTest {

  @InjectMocks
  private EventMapperExactDateTimeMatcher eventMapperExactDateTimeMatcher;

  EventInfo eventInfo;

  List<ShipEvent> shipEvents;


  @BeforeMethod
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expectedExceptions = EventMappingException.class)
  public void testEventLocalDateParsingException() {
    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T08:00");
    eventObj.setStatus("ACTIVE");
    Venue venue = new Venue();
    venue.setId(200L);
    venue.setName("Test Stadium");
    eventObj.setVenue(venue);
    eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");

    shipEvents = new ArrayList<>();
    shipEvents.add(eventObj);
    List<ShipEvent> matchedEvents =
        eventMapperExactDateTimeMatcher.matchEventsByLocalDate(shipEvents, "2016-10-09E08:00");
  }

  @Test(expectedExceptions = EventMappingException.class)
  public void testInputEventLocalDateNull() {
    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T08:00");
    eventObj.setStatus("ACTIVE");
    Venue venue = new Venue();
    venue.setId(200L);
    venue.setName("Test Stadium");
    eventObj.setVenue(venue);
    eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");

    shipEvents = new ArrayList<>();
    shipEvents.add(eventObj);

    List<ShipEvent> matchedEvents =
        eventMapperExactDateTimeMatcher.matchEventsByLocalDate(shipEvents, null);
  }

  @Test
  public void testMatchEventsByLocalDate() {

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T08:00");
    eventObj.setStatus("ACTIVE");
    Venue venue = new Venue();
    venue.setId(200L);
    venue.setName("Test Stadium");
    eventObj.setVenue(venue);
    eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");

    shipEvents = new ArrayList<>();
    shipEvents.add(eventObj);

    List<ShipEvent> matchedEvents =
        eventMapperExactDateTimeMatcher.matchEventsByLocalDate(shipEvents, "2016-10-09T08:00");
    Assert.assertEquals(1, matchedEvents.size());
  }

  @Test
  public void testMatchEventsByUTCDate() throws ParseException {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd'T'HH:mm");
    Date date = dateFormat.parse("2016-07-20T18:00:00+0000");
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    cal.setTime(date);

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateUTC(cal);
    eventObj.setStatus("ACTIVE");
    Venue venue = new Venue();
    venue.setId(200L);
    venue.setName("Test Stadium");
    eventObj.setVenue(venue);
    eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-07-20T18:00:00+0000");

    shipEvents = new ArrayList<>();
    shipEvents.add(eventObj);

    List<ShipEvent> matchedEvents =
        eventMapperExactDateTimeMatcher.matchEventsByUTCDate(shipEvents, eventInfo.getDate());
    Assert.assertEquals(1, matchedEvents.size());

  }

  @Test(expectedExceptions = EventMappingException.class)
  public void testMatchEventsByUTCDateInputNull() throws ParseException {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd'T'HH:mm");
    Date date = dateFormat.parse("2016-10-09T08:00");
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateUTC(cal);
    eventObj.setStatus("ACTIVE");
    Venue venue = new Venue();
    venue.setId(200L);
    venue.setName("Test Stadium");
    eventObj.setVenue(venue);
    eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");

    shipEvents = new ArrayList<>();
    shipEvents.add(eventObj);

    List<ShipEvent> matchedEvents =
        eventMapperExactDateTimeMatcher.matchEventsByUTCDate(shipEvents, null);

  }

}
