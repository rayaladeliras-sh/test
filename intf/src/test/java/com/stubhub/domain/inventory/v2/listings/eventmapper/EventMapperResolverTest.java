package com.stubhub.domain.inventory.v2.listings.eventmapper;

import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.entity.ErrorEnum;
import com.stubhub.domain.inventory.common.util.EventError;
import com.stubhub.domain.inventory.common.util.EventMappingException;
import com.stubhub.domain.inventory.v2.DTO.EventInfo;
import com.stubhub.domain.search.catalog.v3.intf.common.Alias;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.ShipEvent;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.ShipEvents;


public class EventMapperResolverTest {

  @InjectMocks
  private EventMapperResolver eventMapperResolver;

  @Mock
  private EventMapperExactDateTimeMatcher eventMapperExactDateTimeMatcher;

  @Mock
  private EventMapperRangeDateTimeMatcher eventMapperRangeDateTimeMatcher;

  @Mock
  private EventMapperAliasesMatcher eventMapperAliasesMatcher;

  @Mock
  private EventMapperSearchHandler eventMapperSearchHandler;
  
  @Mock
  private KafkaMessageConstructor kafkaMessageConstructor;
  

  Locale locale;
  String sellerId;

  @BeforeMethod
  public void setup() {
    locale = new Locale("US");
    sellerId = "10001";

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testResolveEventNoEventsMatchedVenueMatched_Single() {
    ShipEvents events = new ShipEvents();
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");   
    events.setVenueMatched(true);

    when(eventMapperSearchHandler.searchEvents(locale, eventInfo, null)).thenReturn(events);
    try {
		eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, null);
		Assert.fail("should have thrown EventMappingException");
	} catch (EventMappingException e) {
		Assert.assertTrue(e.getEventError().getMessage().equals(ErrorEnum.EVENT_NOT_MAPPED.getMessage()));
	}    
  }
  
  @Test
  public void testResolveEventNoEventsMatchedNoVenueMatched_Single() {
    ShipEvents events = new ShipEvents();
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");   
    events.setVenueMatched(false);

    when(eventMapperSearchHandler.searchEvents(locale, eventInfo, null)).thenReturn(events);
    try {
		eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, null);
		Assert.fail("should have thrown EventMappingException");
	} catch (EventMappingException e) {
		Assert.assertTrue(e.getEventError().getMessage().equals(ErrorEnum.EVENT_NOT_MAPPED.getMessage()));
	}    
  }
  
  
  @Test
  public void testResolveEventNoEventsMatched_VenueHasParenthesis_NoEvents() {
	ShipEvents events = new ShipEvents();
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue(TV)");
    eventInfo.setDate("2016-10-09T08:00");
    events.setVenueMatched(true);

    when(eventMapperSearchHandler.searchEvents(locale, eventInfo, null)).thenReturn(events);
    try {
		eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, null);
		Assert.fail("should have thrown EventMappingException");
	} catch (EventMappingException e) {
		Assert.assertTrue(e.getEventError().getMessage().equals(ErrorEnum.EVENT_NOT_MAPPED.getMessage()));
	}
  }

  @Test
  public void testResolveEventNoEventsMatched_VenueHasParenthesis() {
	ShipEvents events = new ShipEvents();
    List<ShipEvent> eventsMatchByLocalDate = new ArrayList<>();
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue(TV)");
    eventInfo.setDate("2016-10-09T08:00");
    eventInfo.setEventLocalDate("2016-10-09T08:00");

    ShipEvents eventsAfterRemovingParenthesis = new ShipEvents();
    List<ShipEvent> eventAfterRemoveParenthesis = new ArrayList<>();
    eventsAfterRemovingParenthesis.setVenueMatched(true);
    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T08:00");
    eventObj.setStatus("ACTIVE");
    eventObj.setExcludeBulkListings("false");
    eventAfterRemoveParenthesis.add(eventObj);
    eventsAfterRemovingParenthesis.setEvents(eventAfterRemoveParenthesis);
    eventsMatchByLocalDate.add(eventObj);
    
    when(eventMapperSearchHandler.searchEvents(locale, eventInfo, null)).thenReturn(events, eventsAfterRemovingParenthesis);
    when(eventMapperExactDateTimeMatcher.matchEventsByLocalDate(eventAfterRemoveParenthesis, eventInfo.getEventLocalDate())).thenReturn(eventsMatchByLocalDate);
    ShipEvent shipEvent = eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, null);
    Assert.assertEquals(shipEvent.getId(), eventObj.getId());
  }

  @Test
  public void testResolveMultipleEventsExactMatched_Single_Local() {
	ShipEvents shipEvents = new ShipEvents();

    List<ShipEvent> events = new ArrayList<>();
    List<ShipEvent> eventsMatchByLocalDate = new ArrayList<>();

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T08:00");
    eventObj.setStatus("ACTIVE");
    events.add(eventObj);
    shipEvents.setEvents(events);
    
    ShipEvent eventObj2 = new ShipEvent();
    eventObj.setId(22);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T09:00");
    eventObj.setStatus("ACTIVE");
    events.add(eventObj2);
    shipEvents.setEvents(events);
    eventsMatchByLocalDate.add(eventObj);

    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setEventLocalDate("2016-10-09T08:00");

    when(eventMapperSearchHandler.searchEvents(locale, eventInfo, null)).thenReturn(shipEvents);
    when(eventMapperExactDateTimeMatcher.matchEventsByLocalDate(events,
        eventInfo.getEventLocalDate())).thenReturn(eventsMatchByLocalDate);
    ShipEvent resolveEvent = eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, null);
    Assert.assertNotNull(resolveEvent);
  }

  @Test
  public void testResolveMultipleEventsExactMatched_NoMatch_local() {
	ShipEvents shipEvents = new ShipEvents();
    List<ShipEvent> events = new ArrayList<>();
    List<ShipEvent> eventsMatchByLocalDate = new ArrayList<>();

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T08:00");
    eventObj.setStatus("ACTIVE");
    events.add(eventObj);
    shipEvents.setEvents(events);

    ShipEvent eventObj2 = new ShipEvent();
    eventObj.setId(22);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T09:00");
    eventObj.setStatus("ACTIVE");
    events.add(eventObj2);
    shipEvents.setEvents(events);
    eventsMatchByLocalDate.add(eventObj);

    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setEventLocalDate("2016-10-09T08:00");

    when(eventMapperSearchHandler.searchEvents(locale, eventInfo, null)).thenReturn(shipEvents);
    when(eventMapperExactDateTimeMatcher.matchEventsByLocalDate(events,
        eventInfo.getEventLocalDate())).thenReturn(new ArrayList<ShipEvent>());
    when(eventMapperRangeDateTimeMatcher.matchEventsByLocalDate(events,
        eventInfo.getEventLocalDate())).thenReturn(new ArrayList<ShipEvent>());
    ShipEvent resolveEvent = eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, null);
    Assert.assertNull(resolveEvent);

  }

  @Test
  public void testResolveMultipleEventsExactMatched_Single_UTC() throws Exception {
	ShipEvents shipEvents = new ShipEvents();
    List<ShipEvent> events = new ArrayList<>();
    List<ShipEvent> eventsMatchByUTCDate = new ArrayList<>();

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyy-MM-dd'T'HH:mm");
    Date date = dateFormat.parse("2016-10-09T08:00");
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateUTC(cal);
    eventObj.setStatus("ACTIVE");
    events.add(eventObj);
    shipEvents.setEvents(events);

    ShipEvent eventObj2 = new ShipEvent();
    eventObj.setId(22);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T09:00");
    eventObj.setStatus("ACTIVE");
    events.add(eventObj2);
    shipEvents.setEvents(events);
    eventsMatchByUTCDate.add(eventObj);

    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");

    when(eventMapperSearchHandler.searchEvents(locale, eventInfo, null)).thenReturn(shipEvents);
    when(eventMapperExactDateTimeMatcher.matchEventsByUTCDate(events, eventInfo.getDate()))
        .thenReturn(eventsMatchByUTCDate);
    Assert.assertNotNull(eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, null));
  }

  @Test
  public void testResolveMultipleEventsNoExactMatch_DateRangeSingleMatch_UTC() {
	ShipEvents shipEvents = new ShipEvents();
    List<ShipEvent> events = new ArrayList<>();
    List<ShipEvent> eventsdateRangeMatchByUTC = new ArrayList<>();

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateUTC(DateTimeUtil.getUTCDateFromString("2016-10-09T08:00"));
    eventObj.setStatus("ACTIVE");
    events.add(eventObj);
    shipEvents.setEvents(events);

    ShipEvent eventObj2 = new ShipEvent();
    eventObj2.setId(22);
    eventObj2.setName("Test Event");
    eventObj2.setEventDateUTC(DateTimeUtil.getUTCDateFromString("2016-10-09T09:00"));
    eventObj2.setStatus("ACTIVE");
    events.add(eventObj2);
    eventsdateRangeMatchByUTC.add(eventObj2);
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");

    when(eventMapperSearchHandler.searchEvents(locale, eventInfo, null)).thenReturn(shipEvents);
    when(eventMapperExactDateTimeMatcher.matchEventsByUTCDate(events, eventInfo.getDate()))
        .thenReturn(new ArrayList<ShipEvent>());
    when(eventMapperRangeDateTimeMatcher.matchEventsByUTCDate(events, eventInfo.getDate()))
        .thenReturn(eventsdateRangeMatchByUTC);
    Assert.assertNotNull(eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, null));

  }


  @Test
  public void testResolveMultipleEventsNoExact_MultipleDateRangeMatch_UTC() {
	ShipEvents shipEvents = new ShipEvents();
    List<ShipEvent> events = new ArrayList<>();
    List<ShipEvent> eventsMatchByUTC = new ArrayList<>();

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateUTC(DateTimeUtil.getUTCDateFromString("2016-10-09T08:00"));
    eventObj.setStatus("ACTIVE");
    events.add(eventObj);
    shipEvents.setEvents(events);

    ShipEvent eventObj2 = new ShipEvent();
    eventObj2.setId(22);
    eventObj2.setName("Test Event");
    eventObj2.setEventDateUTC(DateTimeUtil.getUTCDateFromString("2016-10-09T09:00"));
    eventObj2.setStatus("ACTIVE");
    events.add(eventObj2);
    shipEvents.setEvents(events);
    
    eventsMatchByUTC.add(eventObj);
    eventsMatchByUTC.add(eventObj2);
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");

    when(eventMapperSearchHandler.searchEvents(locale, eventInfo, null)).thenReturn(shipEvents);
    when(eventMapperExactDateTimeMatcher.matchEventsByUTCDate(events, eventInfo.getDate()))
        .thenReturn(new ArrayList<ShipEvent>());
    when(eventMapperRangeDateTimeMatcher.matchEventsByUTCDate(events, eventInfo.getDate()))
        .thenReturn(eventsMatchByUTC);
    when(eventMapperAliasesMatcher.matchEvents(events, eventInfo, sellerId)).thenReturn(eventObj);
    Assert.assertEquals(eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, null).getId(),
        eventObj.getId());

  }

  @Test
  public void testResolveMultipleEventsNoExact_MultipleDateRangeMatch_Local() {
		ShipEvents shipEvents = new ShipEvents();
	    List<ShipEvent> events = new ArrayList<>();
	    List<ShipEvent> eventsMatchByLocalDate = new ArrayList<>();

	    ShipEvent eventObj = new ShipEvent();
	    eventObj.setId(2);
	    eventObj.setName("Test Event");
	    eventObj.setEventDateLocal("2016-10-09T08:00");
	    eventObj.setStatus("ACTIVE");
	    events.add(eventObj);
	    shipEvents.setEvents(events);

	    ShipEvent eventObj2 = new ShipEvent();
	    eventObj2.setId(22);
	    eventObj2.setName("Test Event");
	    eventObj2.setEventDateLocal("2016-10-09T09:00");
	    eventObj2.setStatus("ACTIVE");
	    events.add(eventObj2);
	    shipEvents.setEvents(events);

	    eventsMatchByLocalDate.add(eventObj);
	    eventsMatchByLocalDate.add(eventObj2);
	    EventInfo eventInfo = new EventInfo();
	    eventInfo.setName("Test Event");
	    eventInfo.setVenue("Test Venue");
	    eventInfo.setEventLocalDate("2016-10-09T08:00");

    when(eventMapperSearchHandler.searchEvents(locale, eventInfo, null)).thenReturn(shipEvents);
    when(eventMapperExactDateTimeMatcher.matchEventsByLocalDate(events, eventInfo.getDate()))
        .thenReturn(new ArrayList<ShipEvent>());
    when(eventMapperRangeDateTimeMatcher.matchEventsByLocalDate(events, eventInfo.getDate()))
        .thenReturn(eventsMatchByLocalDate);
    when(eventMapperAliasesMatcher.matchEvents(events, eventInfo, sellerId)).thenReturn(eventObj);
    Assert.assertEquals(eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, null).getId(),
        eventObj.getId());

  }

  @Test
  public void testResolveMultipleEventsNoExact_SingleDateRangeMatch_UTC() {
	ShipEvents shipEvents = new ShipEvents();
    List<ShipEvent> events = new ArrayList<>();
    List<ShipEvent> eventsMatchByUTC = new ArrayList<>();

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateUTC(DateTimeUtil.getUTCDateFromString("2016-10-09T08:00"));
    eventObj.setStatus("ACTIVE");
    events.add(eventObj);
    shipEvents.setEvents(events);

    ShipEvent eventObj2 = new ShipEvent();
    eventObj2.setId(22);
    eventObj2.setName("Test Event");
    eventObj2.setEventDateUTC(DateTimeUtil.getUTCDateFromString("2016-10-09T09:00"));
    eventObj2.setStatus("ACTIVE");
    events.add(eventObj2);
    shipEvents.setEvents(events);

    eventsMatchByUTC.add(eventObj);
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");

    when(eventMapperSearchHandler.searchEvents(locale, eventInfo, null)).thenReturn(shipEvents);
    when(eventMapperExactDateTimeMatcher.matchEventsByUTCDate(events, eventInfo.getDate()))
        .thenReturn(new ArrayList<ShipEvent>());
    when(eventMapperRangeDateTimeMatcher.matchEventsByUTCDate(events, eventInfo.getDate()))
        .thenReturn(eventsMatchByUTC);
    Assert.assertEquals(eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, null).getId(),
        eventObj.getId());

  }

  @Test
  public void testResolveMultipleEventsMultipleExact_Aliases_UTC() {
	ShipEvents shipEvents = new ShipEvents();
    List<ShipEvent> events = new ArrayList<>();
    List<ShipEvent> eventsMatchByUTC = new ArrayList<>();

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateUTC(DateTimeUtil.getUTCDateFromString("2016-10-09T08:00"));
    eventObj.setStatus("ACTIVE");
    events.add(eventObj);
    shipEvents.setEvents(events);

    ShipEvent eventObj2 = new ShipEvent();
    eventObj2.setId(22);
    eventObj2.setName("Test Event");
    eventObj2.setEventDateUTC(DateTimeUtil.getUTCDateFromString("2016-10-09T08:00"));
    eventObj2.setStatus("ACTIVE");
    events.add(eventObj2);
    shipEvents.setEvents(events);
    eventsMatchByUTC.add(eventObj);
    eventsMatchByUTC.add(eventObj2);
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");

    when(eventMapperSearchHandler.searchEvents(locale, eventInfo, null)).thenReturn(shipEvents);
    when(eventMapperExactDateTimeMatcher.matchEventsByUTCDate(events, eventInfo.getDate()))
        .thenReturn(eventsMatchByUTC);
    when(eventMapperAliasesMatcher.matchEvents(events, eventInfo, sellerId)).thenReturn(eventObj);
    Assert.assertEquals(eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, null).getId(),
        eventObj.getId());

  }

  @Test
  public void testResolveEventNoEventsMatched_ExcludeBulkListings_NoEvents() {
	ShipEvents shipEvents = new ShipEvents();
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");
    shipEvents.setVenueMatched(false);
    when(eventMapperSearchHandler.searchEvents(locale, eventInfo, null)).thenReturn(shipEvents);
    try {
		eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, null);
		Assert.fail("should have thrown EventMappingException");
	} catch (EventMappingException e) {
		Assert.assertTrue(e.getEventError().getMessage().equals(ErrorEnum.EVENT_NOT_MAPPED.getMessage()));
	}
  }
  
  @Test
  public void testResolveEventEventsMatched_HiddenBulkListings_throwError() {
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");
    eventInfo.setEventLocalDate("2016-10-09T08:00");
	ShipEvents shipEvents = new ShipEvents();

    List<ShipEvent> excludeBulkListingsEvents = new ArrayList<>();
    List<ShipEvent> matchedEvent = new ArrayList<>();

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T08:00");
    eventObj.setStatus("ACTIVE");
    eventObj.setExcludeBulkListings("true");
    excludeBulkListingsEvents.add(eventObj);
    shipEvents.setEvents(excludeBulkListingsEvents);
    matchedEvent.add(eventObj);
    
    EventError error = new EventError();
    error.setMessage(ErrorEnum.EVENT_NOT_MAPPED.getMessage());
    
    when(eventMapperSearchHandler.searchEvents(locale, eventInfo, null)).thenReturn(shipEvents);
    when(eventMapperAliasesMatcher.matchEvents(excludeBulkListingsEvents, eventInfo, sellerId)).thenThrow(new EventMappingException(error));
    try {
		eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, null);
		Assert.fail("should have thrown EventMappingException"); 
	} catch (EventMappingException e) {
		Assert.assertTrue(e.getEventError().getMessage().equals(ErrorEnum.EVENT_NOT_MAPPED.getMessage()));
	}
  } 

  @Test
  public void testResolveEventEventsMatched_HiddenListings() {
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");
    eventInfo.setEventLocalDate("2016-10-09T08:00");
    
	ShipEvents shipEvents = new ShipEvents();
    List<ShipEvent> excludeBulkListingsEvents = new ArrayList<>();

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T08:00");
    eventObj.setStatus("ACTIVE");
    eventObj.setExcludeBulkListings("true");
    List<Alias> aliases = new ArrayList<>();
    Alias alias1 = new Alias();
    alias1.setName("Tests");
    alias1.setDate("2016-10-09T08:00");
    aliases.add(alias1);
    eventObj.setAliases(aliases);
    excludeBulkListingsEvents.add(eventObj);
    shipEvents.setEvents(excludeBulkListingsEvents);

    when(eventMapperSearchHandler.searchEvents(locale, eventInfo, null)).thenReturn(shipEvents);
    when(eventMapperAliasesMatcher.matchEvents(excludeBulkListingsEvents, eventInfo, sellerId)).thenReturn(eventObj);
    Assert.assertEquals(eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, null).getId(),
            eventObj.getId());
  }
  
  @Test
  public void testResolveEventEventsMatched_NonHiddenBulkListings_throwError() {
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");
    eventInfo.setEventLocalDate("2016-10-09T08:00");

	ShipEvents shipEvents = new ShipEvents();
    List<ShipEvent> excludeBulkListingsEvents = new ArrayList<>();
    List<ShipEvent> matchedEvent = new ArrayList<>();

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T08:00");
    eventObj.setStatus("ACTIVE");
    eventObj.setExcludeBulkListings("false");
    excludeBulkListingsEvents.add(eventObj);
    shipEvents.setEvents(excludeBulkListingsEvents);
    matchedEvent.add(eventObj);
    
    EventError error = new EventError();
    error.setMessage(ErrorEnum.EVENT_NOT_MAPPED.getMessage());
    
    when(eventMapperSearchHandler.searchEvents(locale, eventInfo, null)).thenReturn(shipEvents);
    when(eventMapperAliasesMatcher.matchEvents(excludeBulkListingsEvents, eventInfo, sellerId)).thenThrow(new EventMappingException(error));
    try {
		eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, null);
		Assert.fail("should have thrown EventMappingException"); 
	} catch (EventMappingException e) {
		Assert.assertTrue(e.getEventError().getMessage().equals(ErrorEnum.EVENT_NOT_MAPPED.getMessage()));
	}
  } 
  

  @Test
  public void testResolveMultipleEventsMultipleExact_Aliases_Local() {
	ShipEvents shipEvents = new ShipEvents();
    List<ShipEvent> events = new ArrayList<>();
    List<ShipEvent> eventsMatchByLocalDate = new ArrayList<>();

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T08:00");
    eventObj.setStatus("ACTIVE");
    events.add(eventObj);
    shipEvents.setEvents(events);

    ShipEvent eventObj2 = new ShipEvent();
    eventObj2.setId(22);
    eventObj2.setName("Test Event");
    eventObj2.setEventDateLocal("2016-10-09T08:00");
    eventObj2.setStatus("ACTIVE");
    events.add(eventObj2);
    shipEvents.setEvents(events);

    eventsMatchByLocalDate.add(eventObj);
    eventsMatchByLocalDate.add(eventObj2);
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setEventLocalDate("2016-10-09T08:00");

    when(eventMapperSearchHandler.searchEvents(locale, eventInfo, null)).thenReturn(shipEvents);
    when(eventMapperExactDateTimeMatcher.matchEventsByLocalDate(events, eventInfo.getEventLocalDate()))
        .thenReturn(eventsMatchByLocalDate);
    when(eventMapperAliasesMatcher.matchEvents(eventsMatchByLocalDate, eventInfo, sellerId)).thenReturn(eventObj);
    Assert.assertEquals(eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, null).getId(),
        eventObj.getId());

  }
  
}

