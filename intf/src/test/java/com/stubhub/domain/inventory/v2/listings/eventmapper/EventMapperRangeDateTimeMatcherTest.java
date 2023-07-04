package com.stubhub.domain.inventory.v2.listings.eventmapper;


import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.v2.DTO.EventInfo;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.ShipEvent;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.Venue;

import junit.framework.Assert;


public class EventMapperRangeDateTimeMatcherTest {

  @InjectMocks
  private EventMapperRangeDateTimeMatcher eventMapperRangeDateTimeMatcher;

  EventInfo eventInfo;

  List<ShipEvent> shipEvents;

  @BeforeMethod
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testMatchEventsByRangeLocalDateTime() {
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
    eventInfo.setEventLocalDate("2016-10-09T08:30");

    shipEvents = new ArrayList<>();
    shipEvents.add(eventObj);

    List<ShipEvent> matchedEvents = eventMapperRangeDateTimeMatcher
        .matchEventsByLocalDate(shipEvents, eventInfo.getEventLocalDate());
    Assert.assertEquals(1, matchedEvents.size());

  }


  @Test
  public void testMatchEventsByRangeLocalDateTime_NotInRange() {
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
    eventInfo.setDate("2016-10-09T10:30");

    shipEvents = new ArrayList<>();
    shipEvents.add(eventObj);

    List<ShipEvent> matchedEvents =
        eventMapperRangeDateTimeMatcher.matchEventsByLocalDate(shipEvents, eventInfo.getDate());
    Assert.assertEquals(0, matchedEvents.size());

  }

  @Test
  public void testMatchEventsByRangeLocalDateTime_NotPresent() {
    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setStatus("ACTIVE");
    Venue venue = new Venue();
    venue.setId(200L);
    venue.setName("Test Stadium");
    eventObj.setVenue(venue);
    eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T10:30");

    shipEvents = new ArrayList<>();
    shipEvents.add(eventObj);

    List<ShipEvent> matchedEvents =
        eventMapperRangeDateTimeMatcher.matchEventsByLocalDate(shipEvents, eventInfo.getDate());
    Assert.assertEquals(0, matchedEvents.size());

  }



  @Test
  public void testMatchEventsByRangeUTCDateTime() {
    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateUTC(DateTimeUtil.getUTCDateFromString("2016-10-09T08:00"));
    eventObj.setStatus("ACTIVE");
    Venue venue = new Venue();
    venue.setId(200L);
    venue.setName("Test Stadium");
    eventObj.setVenue(venue);
    eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:30");

    shipEvents = new ArrayList<>();
    shipEvents.add(eventObj);

    List<ShipEvent> matchedEvents =
        eventMapperRangeDateTimeMatcher.matchEventsByUTCDate(shipEvents, eventInfo.getDate());
    Assert.assertEquals(1, matchedEvents.size());

  }

  @Test
  public void testMatchEventsByRangeUTCDateTime_notPresent() {
    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setStatus("ACTIVE");
    Venue venue = new Venue();
    venue.setId(200L);
    venue.setName("Test Stadium");
    eventObj.setVenue(venue);
    eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:30");

    shipEvents = new ArrayList<>();
    shipEvents.add(eventObj);

    List<ShipEvent> matchedEvents =
        eventMapperRangeDateTimeMatcher.matchEventsByUTCDate(shipEvents, eventInfo.getDate());
    Assert.assertEquals(0, matchedEvents.size());

  }

  @Test
  public void testMatchEventsByRangeUTCDateTime_NotInRange() {
    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateUTC(DateTimeUtil.getUTCDateFromString("2016-10-09T08:00"));
    eventObj.setStatus("ACTIVE");
    Venue venue = new Venue();
    venue.setId(200L);
    venue.setName("Test Stadium");
    eventObj.setVenue(venue);
    eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setEventLocalDate("2016-10-09T10:30");

    shipEvents = new ArrayList<>();
    shipEvents.add(eventObj);

    List<ShipEvent> matchedEvents = eventMapperRangeDateTimeMatcher.matchEventsByUTCDate(shipEvents,
        eventInfo.getEventLocalDate());
    Assert.assertEquals(0, matchedEvents.size());

  }
}
