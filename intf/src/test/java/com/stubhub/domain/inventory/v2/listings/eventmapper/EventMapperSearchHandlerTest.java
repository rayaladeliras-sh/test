package com.stubhub.domain.inventory.v2.listings.eventmapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.util.EventMappingException;
import com.stubhub.domain.inventory.v2.DTO.EventInfo;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.ShipEvent;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.ShipEvents;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

import junit.framework.Assert;

public class EventMapperSearchHandlerTest {

  @InjectMocks
  private EventMapperSearchHandler eventMapperSearchHandler;

  private SvcLocator svcLocator;
  private ObjectMapper objectMapper;
  private WebClient webClient;

  @BeforeMethod
  public void setUp() {
    eventMapperSearchHandler = new EventMapperSearchHandler() {
      protected String getProperty(String propertyName, String defaultValue) {
        if ("search.catalog.events.ship.v3.url".equals(propertyName))
          return "http://api-int.slcq015.com/search/catalog/events/ship/v3";
        return "";
      }
    };
    svcLocator = Mockito.mock(SvcLocator.class);
    objectMapper = Mockito.mock(ObjectMapper.class);
    webClient = Mockito.mock(WebClient.class);
    ReflectionTestUtils.setField(eventMapperSearchHandler, "svcLocator", svcLocator);
    ReflectionTestUtils.setField(eventMapperSearchHandler, "objectMapper", objectMapper);
    Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
  }


  @Test
  public void testMapEventWithLocalDate() throws Exception {
    EventInfo eventInfo = getEventInfo();
    eventInfo.setCity("San Francisco");
    eventInfo.setState("CA");
    eventInfo.setCountry("US");
    eventInfo.setZipCode("94105");
    Mockito.when(webClient.get()).thenReturn(getResponse());
    Mockito
        .when(
            objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(ShipEvents.class)))
        .thenReturn(getMappingEvents());
    ShipEvents events = eventMapperSearchHandler.searchEvents(null, eventInfo, null);
    Assert.assertNotNull(events);
  }


  @Test
  public void testMapEventWithLocalDateRange() throws Exception {
    EventInfo eventInfo = getEventInfo();
    eventInfo.setEventLocalDate("2020-09-09T11:00");
    Mockito.when(webClient.get()).thenReturn(getResponse());
    ShipEvents shipEvents = getMappingEvents();
    Mockito
        .when(
            objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(ShipEvents.class)))
        .thenReturn(shipEvents);
    ShipEvents events = eventMapperSearchHandler.searchEvents(Locale.US, eventInfo, null);
    Assert.assertNotNull(events);
  }

  @Test
  public void testMapEventWithUTCDateRange() throws Exception {
    EventInfo eventInfo = getEventInfo();
    eventInfo.setEventLocalDate(null);
    eventInfo.setDate("2020-09-09T17:00");
    Mockito.when(webClient.get()).thenReturn(getResponse());
    ShipEvents shipEvents = getMappingEvents();
    Mockito
        .when(
            objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(ShipEvents.class)))
        .thenReturn(shipEvents);
    ShipEvents events = eventMapperSearchHandler.searchEvents(Locale.US, eventInfo, null);
    Assert.assertNotNull(events);
  }

  @Test(expectedExceptions = EventMappingException.class)
  public void testMapEventError() throws Exception {
    EventInfo eventInfo = getEventInfo();
    Mockito.when(webClient.get()).thenReturn(getResponseError());
    ShipEvents events = eventMapperSearchHandler.searchEvents(null, eventInfo, null);
    Assert.assertNotNull(events);
  }

  @Test(expectedExceptions = EventMappingException.class)
  public void testMapEventNullResponse() throws Exception {
    EventInfo eventInfo = getEventInfo();
    Mockito.when(webClient.get()).thenReturn(null);
    ShipEvents events = eventMapperSearchHandler.searchEvents(null, eventInfo, null);
    Assert.assertNotNull(events);
  }

  @Test
  public void testMapEventNoEvent() throws Exception {
    EventInfo eventInfo = getEventInfo();
    Mockito.when(webClient.get()).thenReturn(getResponseNoEvent());
    ShipEvents shipEvents = getMappingEvents();
    shipEvents.setNumFound(0L);
    Mockito
        .when(
            objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(ShipEvents.class)))
        .thenReturn(shipEvents);
    ShipEvents events = eventMapperSearchHandler.searchEvents(null, eventInfo, null);
    Assert.assertEquals(0, events.getEvents().size());
  }
  
/*  @Test
  public void testMapEventNameWithLocalDate() throws Exception {
    EventInfo eventInfo = getEventInfo();
    eventInfo.setCity("San Francisco");
    eventInfo.setState("CA");
    eventInfo.setCountry("US");
    eventInfo.setZipCode("94105");
    Mockito.when(webClient.get()).thenReturn(getResponse());
    Mockito
        .when(
            objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(ShipEvents.class)))
        .thenReturn(getMappingEvents());
    List<ShipEvent> events = eventMapperSearchHandler.searchEventsByEventName(eventInfo);
    Assert.assertNotNull(events);
  }
  
  @Test
  public void testMapEventNameWithLocalDateRange() throws Exception {
    EventInfo eventInfo = getEventInfo();
    eventInfo.setEventLocalDate("2020-09-09T11:00");
    Mockito.when(webClient.get()).thenReturn(getResponse());
    ShipEvents shipEvents = getMappingEvents();
    Mockito
        .when(
            objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(ShipEvents.class)))
        .thenReturn(shipEvents);
    List<ShipEvent> events = eventMapperSearchHandler.searchEventsByEventName(eventInfo);
    Assert.assertNotNull(events);
  }

  @Test
  public void testMapEventNameWithUTCDateRange() throws Exception {
    EventInfo eventInfo = getEventInfo();
    eventInfo.setEventLocalDate(null);
    eventInfo.setDate("2020-09-09T17:00");
    Mockito.when(webClient.get()).thenReturn(getResponse());
    ShipEvents shipEvents = getMappingEvents();
    Mockito
        .when(
            objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(ShipEvents.class)))
        .thenReturn(shipEvents);
    List<ShipEvent> events = eventMapperSearchHandler.searchEventsByEventName(eventInfo);
    Assert.assertNotNull(events);
  }

  @Test(expectedExceptions = EventMappingException.class)
  public void testMapEventNameError() throws Exception {
    EventInfo eventInfo = getEventInfo();
    Mockito.when(webClient.get()).thenReturn(getResponseError());
    List<ShipEvent> events = eventMapperSearchHandler.searchEventsByEventName(eventInfo);
    Assert.assertNotNull(events);
  }
  

  @Test(expectedExceptions = EventMappingException.class)
  public void testMapEventNameNullResponse() throws Exception {
    EventInfo eventInfo = getEventInfo();
    Mockito.when(webClient.get()).thenReturn(null);
    List<ShipEvent> events = eventMapperSearchHandler.searchEventsByEventName(eventInfo);
    Assert.assertNotNull(events);
  }

  @Test
  public void testMapEventNameNoEvent() throws Exception {
    EventInfo eventInfo = getEventInfo();
    Mockito.when(webClient.get()).thenReturn(getResponseNoEvent());
    ShipEvents shipEvents = getMappingEvents();
    shipEvents.setNumFound(0L);
    Mockito
        .when(
            objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(ShipEvents.class)))
        .thenReturn(shipEvents);
    List<ShipEvent> events = eventMapperSearchHandler.searchEventsByEventName(eventInfo);
    Assert.assertEquals(0, events.size());
  } */
  
  private Response getResponseError() {
    Response response = new Response() {

      @Override
      public int getStatus() {
        return 500;
      }

      @Override
      public MultivaluedMap<String, Object> getMetadata() {
        return null;
      }

      @Override
      public Object getEntity() {
        return null;
      }
    };
    return response;
  }

  private Response getResponseNoEvent() {
    Response response = new Response() {

      @Override
      public int getStatus() {
        return 200;
      }

      @Override
      public MultivaluedMap<String, Object> getMetadata() {
        return null;
      }

      @Override
      public Object getEntity() {
        String events = "{ \"numFound\": 0, \"events\": [] }";
        return new ByteArrayInputStream(events.getBytes());
      }
    };
    return response;
  }

  private Response getResponse() {
    Response response = new Response() {

      @Override
      public int getStatus() {
        return 200;
      }

      @Override
      public MultivaluedMap<String, Object> getMetadata() {
        return null;
      }

      @Override
      public Object getEntity() {
        String events =
            "{\"numFound\":1,\"events\":[{\"id\":9401028,\"status\":\"Active\",\"name\":\"Wicked New York\"}]}";
        return new ByteArrayInputStream(events.getBytes());
      }
    };
    return response;
  }


  private EventInfo getEventInfo() {
    EventInfo eventInfo = new EventInfo();
    eventInfo.setName("Test event");
    eventInfo.setVenue("Test venue");
    eventInfo.setEventLocalDate("2020-09-09T10:00");
    eventInfo.setDate("2020-09-09T18:00");
    return eventInfo;
  }

  private ShipEvents getMappingEvents() throws Exception {
    ShipEvents shipEvents = new ShipEvents();
    shipEvents.setNumFound(1L);
    List<ShipEvent> events = new ArrayList<ShipEvent>();
    ShipEvent event = new ShipEvent();
    event.setId(123);
    event.setName("Test Event");
    event.setEventDateLocal("2020-09-09T10:00");

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    Calendar eventDateUTC = Calendar.getInstance();
    eventDateUTC.setTime(sdf.parse("2020-09-09T18:00"));
    event.setEventDateUTC(eventDateUTC);

    events.add(event);
    shipEvents.setEvents(events);
    return shipEvents;
  }

}
