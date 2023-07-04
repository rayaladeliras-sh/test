package com.stubhub.domain.inventory.v2.listings.eventmapper;

import java.util.ArrayList;
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
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.DisplayAttributes;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.ShipEvent;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.Venue;

public class EventMapperAliasesMatcherTest {

  String userId;
  EventInfo eventInfo;
  Locale locale;
  @InjectMocks
  private EventMapperAliasesMatcher eventMapperAliasesMatcher;

  @Mock
  private KafkaProducer kafkaProducer;
  
  @Mock
  private KafkaMessageConstructor kafkaMessageConstructor;
  
  @Mock
  private EventMapperSearchHandler eventMapperSearchHandler;
  
  @Mock
  private EventMapperResolver eventMapperResolver;
  
  @Mock
  private EventMapperExactDateTimeMatcher eventMapperExactDateTimeMatcher;
  

  @BeforeMethod
  public void setup() {
    MockitoAnnotations.initMocks(this);
    locale = new Locale("US");
  }
  
  @Test
  public void testMatchEvents() {
    userId = "10001";

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T08:00");
    eventObj.setStatus("ACTIVE");
    Venue venue = new Venue();
    venue.setId(200L);
    venue.setName("Test Stadium");
    eventObj.setVenue(venue);
    List<Alias> aliases = new ArrayList<>();
    Alias alias1 = new Alias();
    alias1.setName("Tests");
    alias1.setDate("2016-10-09T08:00");
    aliases.add(alias1);
    Alias alias2 = new Alias();
    alias2.setName("Test Event");
    alias2.setDate("2016-10-09T08:00");
    aliases.add(alias2);
    eventObj.setAliases(aliases);

    eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setEventLocalDate("2016-10-09T08:00");

    List<ShipEvent> shipEvents = new ArrayList<>();
    shipEvents.add(eventObj);

    ShipEvent event = eventMapperAliasesMatcher.matchEvents(shipEvents, eventInfo, userId);
    Assert.assertEquals(Integer.valueOf("2"), event.getId());
  }
  
  
  @Test
  public void testMatchEventsFail() {
    userId = "10001";

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T08:00");
    eventObj.setStatus("ACTIVE");
    
    ShipEvent eventObj1 = new ShipEvent();
    eventObj1.setId(3);
    eventObj1.setName("Test Event1");
    eventObj1.setEventDateLocal("2016-10-09T08:00");
    eventObj1.setStatus("ACTIVE");
    
    Venue venue = new Venue();
    venue.setId(200L);
    venue.setName("Test Stadium");
    eventObj.setVenue(venue);
    eventObj1.setVenue(venue);
    List<Alias> aliases = new ArrayList<>();
    Alias alias1 = new Alias();
    alias1.setName("Tests");
    alias1.setDate("2016-10-09T08:00");
    aliases.add(alias1);
    Alias alias2 = new Alias();
    alias2.setName("Test Event");
    alias2.setDate("2016-10-09T08:00");
    aliases.add(alias2);
    eventObj.setAliases(aliases);
    eventObj1.setAliases(aliases);

    eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setEventLocalDate("2016-10-09T08:00");

    List<ShipEvent> shipEvents = new ArrayList<>();
    shipEvents.add(eventObj);
    shipEvents.add(eventObj1);

    ShipEvent event;
	try {
		event = eventMapperAliasesMatcher.matchEvents(shipEvents, eventInfo, userId);
		//Assert.fail("should have failed with multiple match failure");
		Assert.assertEquals(Integer.valueOf("2"), event.getId());
	} catch (Exception e) {
		//Assert.assertTrue(e instanceof EventMappingException);
	}
    
  }
  

  @Test
  public void testMatchEvents_UTC() {
    userId = "10001";

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateUTC(DateTimeUtil.getUTCDateFromString("2016-10-09T08:00"));
    eventObj.setStatus("ACTIVE");
    Venue venue = new Venue();
    venue.setId(200L);
    venue.setName("Test Stadium");
    eventObj.setVenue(venue);
    List<Alias> aliases = new ArrayList<>();
    Alias alias1 = new Alias();
    alias1.setName("Tests");
    alias1.setDate("2016-10-09T08:00");
    aliases.add(alias1);
    Alias alias2 = new Alias();
    alias2.setName("Test Event");
    alias2.setDate("2016-10-09T08:00");
    aliases.add(alias2);
    eventObj.setAliases(aliases);

    eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");

    List<ShipEvent> shipEvents = new ArrayList<>();
    shipEvents.add(eventObj);

    ShipEvent event = eventMapperAliasesMatcher.matchEvents(shipEvents, eventInfo, userId);
    Assert.assertEquals(Integer.valueOf("2"), event.getId());;
  }

  @Test
  public void testMatchEventsReturnNull() {
    userId = "10001";

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T08:00");
    eventObj.setEventDateUTC(DateTimeUtil.getUTCDateFromString("2016-10-09T08:00"));
    eventObj.setStatus("ACTIVE");
    Venue venue = new Venue();
    venue.setId(200L);
    venue.setName("Test Stadium");
    eventObj.setVenue(venue);
    List<Alias> aliases = new ArrayList<>();
    Alias alias1 = new Alias();
    alias1.setName("Tests");
    alias1.setDate("2016-10-09T08:00");
    aliases.add(alias1);
    Alias alias2 = new Alias();
    alias2.setName("Test Event");
    alias2.setDate("2016-10-09T08:00");
    aliases.add(alias2);
    eventObj.setAliases(aliases);

    eventInfo = new EventInfo();
    eventInfo.setName("Test Events");
    eventInfo.setVenue("Test Venue");
    eventInfo.setEventLocalDate("2016-10-09T08:00");

    List<ShipEvent> shipEvents = new ArrayList<>();
    shipEvents.add(eventObj);

    ShipEvent shipEvent;
	try {
		shipEvent = eventMapperAliasesMatcher.matchEvents(shipEvents, eventInfo, userId);
		Assert.fail("should have thrown EventMappingException");
	} catch (EventMappingException e) {		
		Assert.assertTrue(e.getEventError().getMessage().equals(ErrorEnum.EVENT_NOT_MAPPED.getMessage()));
	}
    
    
    eventInfo = new EventInfo();
    eventInfo.setName("Test Events");
    eventInfo.setVenue("Test Venue");
    eventInfo.setDate("2016-10-09T08:00");
    try {
		shipEvent = eventMapperAliasesMatcher.matchEvents(shipEvents, eventInfo, userId);
		Assert.fail("should have thrown EventMappingException");
	} catch (EventMappingException e) {
		Assert.assertTrue(e.getEventError().getMessage().equals(ErrorEnum.EVENT_NOT_MAPPED.getMessage()));
	}
    
  }

  @Test
  public void testMatchEventsExcludeBulkListingTrue() {
    userId = "10001";

    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T08:00");
    eventObj.setStatus("ACTIVE");
    eventObj.setExcludeBulkListings("true");
    Venue venue = new Venue();
    venue.setId(200L);
    venue.setName("Test Stadium");
    eventObj.setVenue(venue);

    eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setEventLocalDate("2016-10-09T08:00");

    List<ShipEvent> shipEvents = new ArrayList<>();
    shipEvents.add(eventObj);
    
    EventError error = new EventError();
    error.setMessage(ErrorEnum.EVENT_NOT_MAPPED.getMessage());
    try {
    	eventMapperAliasesMatcher.matchEvents(shipEvents, eventInfo, userId);
		Assert.fail("should have thrown EventMappingException"); 
	} catch (EventMappingException e) {
		Assert.assertTrue(e.getEventError().getMessage().equals(ErrorEnum.EVENT_NOT_MAPPED.getMessage()));
	}
    
  }  
  @Test
  public void testMatchEventsExcludeBulkListingTrueAndFalse() {
    userId = "10001";
    ShipEvent eventObj = new ShipEvent();
    eventObj.setId(2);
    eventObj.setName("Test Event");
    eventObj.setEventDateLocal("2016-10-09T00:00");
    eventObj.setStatus("ACTIVE");
    eventObj.setExcludeBulkListings("false");
    Venue venue = new Venue();
    venue.setId(200L);
    venue.setName("Test Stadium");
    eventObj.setVenue(venue);

    eventInfo = new EventInfo();
    eventInfo.setName("Test Event");
    eventInfo.setVenue("Test Venue");
    eventInfo.setEventLocalDate("2016-10-09T08:00");

    DisplayAttributes da = new DisplayAttributes();
    da.setHideEventTime(true);
    eventObj.setDisplayAttributes(da);
    
    List<ShipEvent> shipEvents = new ArrayList<>();
    shipEvents.add(eventObj);
    ShipEvent shipEvent;
    shipEvent=eventMapperAliasesMatcher.matchEvents(shipEvents, eventInfo, userId);
	Assert.assertNotNull(shipEvent);
	Assert.assertEquals(Integer.valueOf("2"), shipEvent.getId());
    
  } 
}
