package com.stubhub.domain.inventory.listings.v2.newflow.helper;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.mockito.InjectMocks;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.stubhub.domain.catalog.events.intf.TicketTrait;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.SeatTrait;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;

public class EventHelperTest {

  @InjectMocks
  private EventHelper eventHelper;

  @BeforeMethod
  public void setup() {
    initMocks(this);
    eventHelper = new EventHelper();
  }

  @Test
  public void testConvert() {

    try {
      List<SeatTrait> seatTraits = new ArrayList<>();
      SeatTrait seatTrait = new SeatTrait();

      seatTrait.setId(123l);
      seatTrait.setName("SeatTrait");
      seatTrait.setCategoryId(2l);
      seatTrait.setType("TICKET");
      seatTrait.setCategory("TRAITS");
      seatTraits.add(seatTrait);

      Event tempEvent = getEvent();
      tempEvent.setSeatTraits(seatTraits);

      com.stubhub.domain.inventory.datamodel.entity.Event event = eventHelper.convert(tempEvent);
      assertNotNull(event);
      assertEquals(event.getId(), new Long(9813001));
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  @Test
  public void testGetDateLastChance() throws JsonParseException, JsonMappingException, IOException {
    Calendar cal = eventHelper.getDateLastChance(getEvent());
    assertNotNull(cal);
  }

  @Test
  public void testGetEventDateLocal() throws JsonParseException, JsonMappingException, IOException {
    Calendar cal = eventHelper.getEventDateLocal(getEvent());
    assertNotNull(cal);
  }

  /*
  @Test
  public void testValidate(){
    
    try {
      String EVENT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
      SimpleDateFormat sdf = new SimpleDateFormat(EVENT_DATE_FORMAT);
      Calendar cal = Calendar.getInstance();
      cal.add(Calendar.DATE, 2);
      Date dt = cal.getTime();
      String dtString = sdf.format(dt);
      
      Event event1 = getEvent();
      event1.setEventDateLocal(dtString);
      eventHelper.validate(event1);
    } catch (IOException e) {
      fail("should not reach here");
    }
    
  }
  
  @Test
  public void testValidateInvalidStatus() throws JsonParseException, JsonMappingException, IOException{
    
    try {
      Event event1 = getEvent();
      event1.setStatus("INACTIVE");
      eventHelper.validate(event1);
    } catch (ListingException e) {
      assertEquals(e.getMessage(), "Event is not Active");
    }
    
  }
  
  @Test
  public void testValidateExpiredEvent() throws JsonParseException, JsonMappingException, IOException{
    
    try {
      Event event1 = getEvent();
      event1.setExpiredInd(true);
      eventHelper.validate(event1);
    } catch (ListingException e) {
      assertEquals(e.getMessage(), "The event has expired");
    }
    
  }

   */
  
  @Test
  public void testGetEventDateLocalError() throws JsonParseException, JsonMappingException, IOException {
    try {
      Event event1 = getEvent();
      event1.setEventDateLocal("en_US");;
      eventHelper.getEventDateLocal(event1);
    } catch (ListingException e) {
      assertEquals(e.getMessage(), "System error happened while processing");
    }
    
  }
  
  @Test
  public void testConvertEventDateLocalParseError() throws JsonParseException, JsonMappingException, IOException {
    try {
      Event event = getEventDateLocalFail();
      com.stubhub.domain.inventory.datamodel.entity.Event event1 = eventHelper.convert(event);
      assertNotNull(event1);
    } catch (ListingException e) {
      e.printStackTrace();
    }
    
  }

  private Event getEvent() throws JsonParseException, JsonMappingException, IOException {

    String eventJson =
        "{\"id\":9813001,\"dateLastChance\":\"2017-04-18T19:00:00-04:00\",\"name\":\"NHL Eastern Conference Quarterfinals Tickets: Montreal Canadiens at New York Rangers (Home Game 2 - Series Game 4)\",\"status\":\"active\",\"expiredInd\":false,\"locale\":\"en_US\",\"currencyCode\":\"USD\",\"eventDateUTC\":\"2017-04-18T23:00:00Z\",\"eventDateLocal\":\"2017-04-18T19:00:00-04:00\",\"timezone\":\"US/Eastern\",\"bobId\":\"1\",\"eventUrl\":\"new-york-rangers-new-york-madison-square-garden-4-18-2017-9813001\",\"webURI\":\"new-york-rangers-tickets-new-york-rangers-new-york-madison-square-garden-4-18-2017/event/9813001/\",\"seoURI\":\"new-york-rangers-tickets-new-york-rangers-new-york-madison-square-garden-4-18-2017\",\"allowedViewingDomains\":[\"stubhub.com\"],\"categories\":[{\"id\":32,\"name\":\"Hockey Tickets\",\"webURI\":\"hockey-tickets/category/32/\",\"seoURI\":\"hockey-tickets\",\"locale\":\"en_US\"}],\"groupings\":[{\"id\":108321,\"name\":\"New York Rangers Playoff\",\"webURI\":\"new-york-rangers-playoff-tickets/grouping/108321/\",\"seoURI\":\"new-york-rangers-playoff-tickets\",\"locale\":\"en_US\"},{\"id\":109618,\"name\":\"Montreal Canadiens Playoff\",\"relationType\":\"PLAYOFF\",\"webURI\":\"montreal-canadiens-playoff-tickets/grouping/109618/\",\"seoURI\":\"montreal-canadiens-playoff-tickets\",\"locale\":\"en_US\"}],\"performers\":[{\"id\":2764,\"name\":\"New York Rangers\",\"webURI\":\"new-york-rangers-tickets/performer/2764/\",\"seoURI\":\"new-york-rangers-tickets\",\"locale\":\"en_US\"},{\"id\":7554,\"name\":\"Montreal Canadiens\",\"role\":\"AWAY_TEAM\",\"webURI\":\"montreal-canadiens-tickets/performer/7554/\",\"seoURI\":\"montreal-canadiens-tickets\"}],\"geography\":{\"id\":664,\"name\":\"New York Metro\",\"webURI\":\"new-york-metro-tickets/geography/664/\",\"seoURI\":\"new-york-metro-tickets\",\"locale\":\"en_US\"},\"ancestors\":{\"categories\":[{\"id\":28,\"name\":\"Sports tickets\",\"url\":\"sports-tickets\",\"webURI\":\"sports-tickets/category/28/\",\"seoURI\":\"sports-tickets\",\"locale\":\"en_US\"},{\"id\":32,\"name\":\"Hockey Tickets\",\"url\":\"hockey-tickets\",\"webURI\":\"hockey-tickets/category/32/\",\"seoURI\":\"hockey-tickets\",\"locale\":\"en_US\"}],\"groupings\":[{\"id\":144,\"name\":\"2016-2017 NHL\",\"url\":\"nhl-tickets\",\"webURI\":\"nhl-tickets/grouping/144/\",\"seoURI\":\"nhl-tickets\",\"locale\":\"en_US\"},{\"id\":108319,\"name\":\"NHL Playoff Tickets\",\"url\":\"nhl-playoff-tickets\",\"webURI\":\"nhl-playoff-tickets/grouping/108319/\",\"seoURI\":\"nhl-playoff-tickets\",\"locale\":\"en_US\"},{\"id\":108321,\"name\":\"New York Rangers Playoff\",\"url\":\"new-york-rangers-playoff-tickets\",\"webURI\":\"new-york-rangers-playoff-tickets/grouping/108321/\",\"seoURI\":\"new-york-rangers-playoff-tickets\",\"locale\":\"en_US\"}],\"performers\":[{\"id\":2764,\"name\":\"New York Rangers\",\"url\":\"new-york-rangers-tickets\",\"webURI\":\"new-york-rangers-tickets/performer/2764/\",\"seoURI\":\"new-york-rangers-tickets\"}],\"geographies\":[{\"id\":196976,\"name\":\"United States\",\"url\":\"united-states-tickets\",\"webURI\":\"united-states-tickets/geography/196976/\",\"seoURI\":\"united-states-tickets\",\"locale\":\"en_US\"},{\"id\":561,\"name\":\"New York\",\"url\":\"new-york-tickets\",\"webURI\":\"new-york-tickets/geography/561/\",\"seoURI\":\"new-york-tickets\",\"locale\":\"en_US\"},{\"id\":664,\"name\":\"New York Metro\",\"url\":\"new-york-metro-tickets\",\"webURI\":\"new-york-metro-tickets/geography/664/\",\"seoURI\":\"new-york-metro-tickets\",\"locale\":\"en_US\"}]},\"externalEventMappings\":[{\"namespace\":\"zv\",\"id\":\"374671589\"}],\"venue\":{\"id\":1282,\"name\":\"Madison Square Garden\",\"venueInfoUrl\":\"madison-square-garden\",\"venueEventsUrl\":\"madison-square-garden-tickets\",\"webURI\":\"madison-square-garden-tickets/venue/1282/\",\"seoURI\":\"madison-square-garden-tickets\",\"address1\":\"4 Pennsylvania Plaza\",\"locality\":\"New York\",\"postalCode\":\"10001\",\"state\":\"NY\",\"country\":\"US\",\"configurationId\":\"595667\",\"blendedIndicator\":false,\"longitude\":-73.993371,\"latitude\":40.750354},\"seoMeta\":{\"metaDescription\":\"Rangers - Canadiens Playoff Tickets - Buy and sell Montreal Canadiens vs New York Rangers NHL Eastern Conference Quarterfinals Tickets for April 18 at Madison Square Garden in New York, NY on StubHub!\",\"seoDescription\":\"Montreal Canadiens at New York Rangers\",\"seoTitle\":\"Canadiens - Rangers NHL Eastern Conference Quarterfinals Tickets - 2017-4-18\",\"keywords\":\"New York Rangers, Montreal Canadiens, NHL Eastern Conference Quarterfinals, New York Rangers Montreal Canadiens, Rangers vs Madison Square Garden, New York Rangers v Montreal Canadiens, Madison Square Garden, Rangers Canadiens 4/18, New York Rangers 4 18, NHL Eastern Conference Quarterfinals 04/18/2017, ticket, tickets, buy, sell\",\"title\":\"New York Rangers vs Montreal Canadiens [04/18/2017] Tickets on StubHub!\",\"locale\":\"en_US\"},\"images\":[{\"url\":\"http://i.ebayimg.com/images/g/bXQAAOSwls5Y7WFM/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/bXQAAOSwls5Y7WFM/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"Adobe Stock\",\"source\":\"Adobe Stock\",\"primaryInd\":false,\"height\":\"2811\",\"width\":\"4263\",\"imgOptimizationStatus\":\"unprocessed\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/CZYAAOSwv0tVeKAR/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/CZYAAOSwv0tVeKAR/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/YC0AAOSwKrhVeKBI/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/YC0AAOSwKrhVeKBI/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/1pkAAOSwl8NVeJ-U/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/1pkAAOSwl8NVeJ-U/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"Shutterstock\",\"source\":\"Shutterstock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/esMAAOSwpDdVeJ~5/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/esMAAOSwpDdVeJ~5/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/JVYAAOSwv0tVeKDl/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/JVYAAOSwv0tVeKDl/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/qDwAAOSwZd1VeKAi/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/qDwAAOSwZd1VeKAi/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/B28AAOSweW5VeKIZ/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/B28AAOSweW5VeKIZ/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"Corbis\",\"source\":\"Corbis\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/c5MAAOSw~OdVeKAy/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/c5MAAOSw~OdVeKAy/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/ljEAAOSwZd1VeJ-v/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/ljEAAOSwZd1VeJ-v/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/0w4AAOSweW5VeKC2/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/0w4AAOSweW5VeKC2/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/1fYAAOSwstxVeKDQ/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/1fYAAOSwstxVeKDQ/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/lwQAAOSwBahVeJ~N/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/lwQAAOSwBahVeJ~N/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"}],\"eventAttributes\":{\"gameType\":\"PLAYOFF\",\"eventType\":\"Not Known\",\"logisticsPath\":\"28/32/144/108319/108321\"},\"displayAttributes\":{\"title\":\"New York Rangers vs Montreal Canadiens [04/18/2017] Tickets on StubHub!\",\"hiddenInd\":false,\"locale\":\"en_US\",\"hideEventDate\":false,\"hideEventTime\":false,\"integratedEventInd\":false},\"mobileAttributes\":{\"stubhubMobileTicket\":true},\"dynamicAttributes\":[{\"name\":\"act_primary\",\"value\":\"New York Rangers\",\"type\":\"stubhub_display_attrib\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"game_type\",\"value\":\"PLAYOFF\",\"type\":\"stubhub_catalog_attrib\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"allowedViewingDomain\",\"value\":\"stubhub.com\",\"type\":\"stubhub_catalog_attrib\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_entity_feature_pq\",\"value\":\"0.244286\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_event_feature_kids_score\",\"value\":\"0.522123\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"logistics_path\",\"value\":\"28/32/144/108319/108321\",\"type\":\"Others\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_entity_feature_tpv_ems\",\"value\":\"0.0\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"act_secondary\",\"value\":\"Montreal Canadiens\",\"type\":\"stubhub_display_attrib\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_event_feature_nsfw_score\",\"value\":\"0.208771\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_entity_feature_value\",\"value\":\"0.638235\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"hide_event_date\",\"value\":\"false\",\"type\":\"stubhub_display_attrib\",\"status\":\"Active\"}]}";

    ObjectMapper mapper = new ObjectMapper();

    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    Event event = (Event) mapper.readValue(eventJson, Event.class);
    return event;

  }
  
  private Event getEventDateLocalFail() throws JsonParseException, JsonMappingException, IOException {

    String eventJson =
        "{\"id\":9813001,\"dateLastChance\":\"2017-04-18T19:00:00-04:00\",\"name\":\"NHL Eastern Conference Quarterfinals Tickets: Montreal Canadiens at New York Rangers (Home Game 2 - Series Game 4)\",\"status\":\"active\",\"expiredInd\":false,\"locale\":\"en_US\",\"currencyCode\":\"USD\",\"eventDateUTC\":\"18-11-2017T23:00:00Z\",\"eventDateLocal\":\"18042017T19:00:00-04:00\",\"timezone\":\"US/Eastern\",\"bobId\":\"1\",\"eventUrl\":\"new-york-rangers-new-york-madison-square-garden-4-18-2017-9813001\",\"webURI\":\"new-york-rangers-tickets-new-york-rangers-new-york-madison-square-garden-4-18-2017/event/9813001/\",\"seoURI\":\"new-york-rangers-tickets-new-york-rangers-new-york-madison-square-garden-4-18-2017\",\"allowedViewingDomains\":[\"stubhub.com\"],\"categories\":[{\"id\":32,\"name\":\"Hockey Tickets\",\"webURI\":\"hockey-tickets/category/32/\",\"seoURI\":\"hockey-tickets\",\"locale\":\"en_US\"}],\"groupings\":[{\"id\":108321,\"name\":\"New York Rangers Playoff\",\"webURI\":\"new-york-rangers-playoff-tickets/grouping/108321/\",\"seoURI\":\"new-york-rangers-playoff-tickets\",\"locale\":\"en_US\"},{\"id\":109618,\"name\":\"Montreal Canadiens Playoff\",\"relationType\":\"PLAYOFF\",\"webURI\":\"montreal-canadiens-playoff-tickets/grouping/109618/\",\"seoURI\":\"montreal-canadiens-playoff-tickets\",\"locale\":\"en_US\"}],\"performers\":[{\"id\":2764,\"name\":\"New York Rangers\",\"webURI\":\"new-york-rangers-tickets/performer/2764/\",\"seoURI\":\"new-york-rangers-tickets\",\"locale\":\"en_US\"},{\"id\":7554,\"name\":\"Montreal Canadiens\",\"role\":\"AWAY_TEAM\",\"webURI\":\"montreal-canadiens-tickets/performer/7554/\",\"seoURI\":\"montreal-canadiens-tickets\"}],\"geography\":{\"id\":664,\"name\":\"New York Metro\",\"webURI\":\"new-york-metro-tickets/geography/664/\",\"seoURI\":\"new-york-metro-tickets\",\"locale\":\"en_US\"},\"ancestors\":{\"categories\":[{\"id\":28,\"name\":\"Sports tickets\",\"url\":\"sports-tickets\",\"webURI\":\"sports-tickets/category/28/\",\"seoURI\":\"sports-tickets\",\"locale\":\"en_US\"},{\"id\":32,\"name\":\"Hockey Tickets\",\"url\":\"hockey-tickets\",\"webURI\":\"hockey-tickets/category/32/\",\"seoURI\":\"hockey-tickets\",\"locale\":\"en_US\"}],\"groupings\":[{\"id\":144,\"name\":\"2016-2017 NHL\",\"url\":\"nhl-tickets\",\"webURI\":\"nhl-tickets/grouping/144/\",\"seoURI\":\"nhl-tickets\",\"locale\":\"en_US\"},{\"id\":108319,\"name\":\"NHL Playoff Tickets\",\"url\":\"nhl-playoff-tickets\",\"webURI\":\"nhl-playoff-tickets/grouping/108319/\",\"seoURI\":\"nhl-playoff-tickets\",\"locale\":\"en_US\"},{\"id\":108321,\"name\":\"New York Rangers Playoff\",\"url\":\"new-york-rangers-playoff-tickets\",\"webURI\":\"new-york-rangers-playoff-tickets/grouping/108321/\",\"seoURI\":\"new-york-rangers-playoff-tickets\",\"locale\":\"en_US\"}],\"performers\":[{\"id\":2764,\"name\":\"New York Rangers\",\"url\":\"new-york-rangers-tickets\",\"webURI\":\"new-york-rangers-tickets/performer/2764/\",\"seoURI\":\"new-york-rangers-tickets\"}],\"geographies\":[{\"id\":196976,\"name\":\"United States\",\"url\":\"united-states-tickets\",\"webURI\":\"united-states-tickets/geography/196976/\",\"seoURI\":\"united-states-tickets\",\"locale\":\"en_US\"},{\"id\":561,\"name\":\"New York\",\"url\":\"new-york-tickets\",\"webURI\":\"new-york-tickets/geography/561/\",\"seoURI\":\"new-york-tickets\",\"locale\":\"en_US\"},{\"id\":664,\"name\":\"New York Metro\",\"url\":\"new-york-metro-tickets\",\"webURI\":\"new-york-metro-tickets/geography/664/\",\"seoURI\":\"new-york-metro-tickets\",\"locale\":\"en_US\"}]},\"externalEventMappings\":[{\"namespace\":\"zv\",\"id\":\"374671589\"}],\"venue\":{\"id\":1282,\"name\":\"Madison Square Garden\",\"venueInfoUrl\":\"madison-square-garden\",\"venueEventsUrl\":\"madison-square-garden-tickets\",\"webURI\":\"madison-square-garden-tickets/venue/1282/\",\"seoURI\":\"madison-square-garden-tickets\",\"address1\":\"4 Pennsylvania Plaza\",\"locality\":\"New York\",\"postalCode\":\"10001\",\"state\":\"NY\",\"country\":\"US\",\"configurationId\":\"595667\",\"blendedIndicator\":false,\"longitude\":-73.993371,\"latitude\":40.750354},\"seoMeta\":{\"metaDescription\":\"Rangers - Canadiens Playoff Tickets - Buy and sell Montreal Canadiens vs New York Rangers NHL Eastern Conference Quarterfinals Tickets for April 18 at Madison Square Garden in New York, NY on StubHub!\",\"seoDescription\":\"Montreal Canadiens at New York Rangers\",\"seoTitle\":\"Canadiens - Rangers NHL Eastern Conference Quarterfinals Tickets - 2017-4-18\",\"keywords\":\"New York Rangers, Montreal Canadiens, NHL Eastern Conference Quarterfinals, New York Rangers Montreal Canadiens, Rangers vs Madison Square Garden, New York Rangers v Montreal Canadiens, Madison Square Garden, Rangers Canadiens 4/18, New York Rangers 4 18, NHL Eastern Conference Quarterfinals 04/18/2017, ticket, tickets, buy, sell\",\"title\":\"New York Rangers vs Montreal Canadiens [04/18/2017] Tickets on StubHub!\",\"locale\":\"en_US\"},\"images\":[{\"url\":\"http://i.ebayimg.com/images/g/bXQAAOSwls5Y7WFM/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/bXQAAOSwls5Y7WFM/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"Adobe Stock\",\"source\":\"Adobe Stock\",\"primaryInd\":false,\"height\":\"2811\",\"width\":\"4263\",\"imgOptimizationStatus\":\"unprocessed\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/CZYAAOSwv0tVeKAR/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/CZYAAOSwv0tVeKAR/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/YC0AAOSwKrhVeKBI/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/YC0AAOSwKrhVeKBI/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/1pkAAOSwl8NVeJ-U/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/1pkAAOSwl8NVeJ-U/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"Shutterstock\",\"source\":\"Shutterstock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/esMAAOSwpDdVeJ~5/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/esMAAOSwpDdVeJ~5/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/JVYAAOSwv0tVeKDl/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/JVYAAOSwv0tVeKDl/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/qDwAAOSwZd1VeKAi/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/qDwAAOSwZd1VeKAi/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/B28AAOSweW5VeKIZ/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/B28AAOSweW5VeKIZ/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"Corbis\",\"source\":\"Corbis\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/c5MAAOSw~OdVeKAy/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/c5MAAOSw~OdVeKAy/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/ljEAAOSwZd1VeJ-v/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/ljEAAOSwZd1VeJ-v/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/0w4AAOSweW5VeKC2/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/0w4AAOSweW5VeKC2/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/1fYAAOSwstxVeKDQ/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/1fYAAOSwstxVeKDQ/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/lwQAAOSwBahVeJ~N/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/lwQAAOSwBahVeJ~N/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"}],\"eventAttributes\":{\"gameType\":\"PLAYOFF\",\"eventType\":\"Not Known\",\"logisticsPath\":\"28/32/144/108319/108321\"},\"displayAttributes\":{\"title\":\"New York Rangers vs Montreal Canadiens [04/18/2017] Tickets on StubHub!\",\"hiddenInd\":false,\"locale\":\"en_US\",\"hideEventDate\":false,\"hideEventTime\":false,\"integratedEventInd\":false},\"mobileAttributes\":{\"stubhubMobileTicket\":true},\"dynamicAttributes\":[{\"name\":\"act_primary\",\"value\":\"New York Rangers\",\"type\":\"stubhub_display_attrib\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"game_type\",\"value\":\"PLAYOFF\",\"type\":\"stubhub_catalog_attrib\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"allowedViewingDomain\",\"value\":\"stubhub.com\",\"type\":\"stubhub_catalog_attrib\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_entity_feature_pq\",\"value\":\"0.244286\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_event_feature_kids_score\",\"value\":\"0.522123\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"logistics_path\",\"value\":\"28/32/144/108319/108321\",\"type\":\"Others\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_entity_feature_tpv_ems\",\"value\":\"0.0\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"act_secondary\",\"value\":\"Montreal Canadiens\",\"type\":\"stubhub_display_attrib\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_event_feature_nsfw_score\",\"value\":\"0.208771\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_entity_feature_value\",\"value\":\"0.638235\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"hide_event_date\",\"value\":\"false\",\"type\":\"stubhub_display_attrib\",\"status\":\"Active\"}]}";

    ObjectMapper mapper = new ObjectMapper();

    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    Event event = (Event) mapper.readValue(eventJson, Event.class);
    return event;

  }

}
