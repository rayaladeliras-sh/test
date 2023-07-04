package com.stubhub.domain.inventory.listings.v2.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event;
import com.stubhub.domain.infrastructure.config.client.core.management.SHConfigMBean;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.FulfillmentMethod;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingType;
import com.stubhub.domain.inventory.listings.v2.tns.dto.FraudStatusUpdateRequest;
import com.stubhub.domain.inventory.listings.v2.tns.util.AbstractFraudAPIHelper;
import com.stubhub.domain.inventory.listings.v2.tns.util.FraudAPIHelper;
import com.stubhub.domain.inventory.listings.v2.tns.util.FraudEvaluationHelper;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.newplatform.common.cache.store.CacheStore;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;

import junit.framework.Assert;

public class FraudEvaluationHelperTest {

	@Mock
	MasterStubhubPropertiesWrapper masterStubhubProperties;
	@Mock
	private MessageHubAPIHelper messageHubHelper;
	@Mock
	private EventHelper eventHelper;
	@Mock
	private InventoryMgr inventoryMgr;
	@Mock
	private ObjectMapper objectMapper;
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private CacheStore cacheStore;
	
	@InjectMocks
	private FraudEvaluationHelper fraudEvalutionHelper;
	
	@Mock
	private JmsTemplate fraudSellerDeactivationMsgProducer;
	
	private AbstractFraudAPIHelper fraudAPIHelper;
	
	@BeforeMethod
	public void setUp () {
		MockitoAnnotations.initMocks(this);
		fraudEvalutionHelper = new FraudEvaluationHelper();
		fraudAPIHelper = new FraudAPIHelper();
		
		ReflectionTestUtils.setField(fraudAPIHelper, "objectMapper", objectMapper);
		ReflectionTestUtils.setField(fraudAPIHelper, "masterStubhubProperties", masterStubhubProperties);
		ReflectionTestUtils.setField(fraudEvalutionHelper, "messageHubHelper", messageHubHelper);
		ReflectionTestUtils.setField(fraudAPIHelper, "eventHelper", eventHelper);
		ReflectionTestUtils.setField(fraudEvalutionHelper, "inventoryMgr", inventoryMgr);
		ReflectionTestUtils.setField(fraudAPIHelper, "restTemplate", restTemplate);
		ReflectionTestUtils.setField(fraudEvalutionHelper, "cacheStore", cacheStore);
		ReflectionTestUtils.setField(fraudEvalutionHelper, "fraudAPIHelper", fraudAPIHelper);
		
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("1");
		ResponseEntity<String> responseEntity = new ResponseEntity<String>("{}", HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class),Mockito.anyVararg())).thenReturn(responseEntity);
		Mockito.doNothing().when(cacheStore).put(Mockito.anyString(), Mockito.any(Serializable.class),Mockito.anyInt());
		Mockito.when(cacheStore.get(Mockito.anyString())).thenReturn(null);
		Mockito.doNothing().when(fraudSellerDeactivationMsgProducer).send(Mockito.any(MessageCreator.class));

	}
	
	@Test
	public void testSubmitFraudListingEmail() {
		fraudEvalutionHelper.submitFraudListingEmailRequest("123456","1234", 500l);
		Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class),Mockito.anyVararg())).thenReturn(null);
		fraudEvalutionHelper.submitFraudListingEmailRequest("123456","1234", 500l);
		ResponseEntity<String> responseEntity = new ResponseEntity<String>("{}", HttpStatus.INTERNAL_SERVER_ERROR);
		Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class),Mockito.anyVararg())).thenReturn(responseEntity);
		fraudEvalutionHelper.submitFraudListingEmailRequest("123456","1234", 500l);
		responseEntity = new ResponseEntity<String>("{}", HttpStatus.NOT_FOUND);
		Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class),Mockito.anyVararg())).thenReturn(responseEntity);
		fraudEvalutionHelper.submitFraudListingEmailRequest("123456","1234", 500l);
		Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class),Mockito.anyVararg())).thenThrow(new RuntimeException("Exception!!"));
		fraudEvalutionHelper.submitFraudListingEmailRequest("123456","1234", 500l);
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenThrow(new RuntimeException("Exception!!"));
		fraudEvalutionHelper.submitFraudListingEmailRequest("123456","1234", 500l);
	}
	
//	private ListingResponse getListingResponse() {
//		ListingResponse response = new ListingResponse();
//		response.setId("123456");
//		response.setSellerId(1234l);
//		return response;
//		
//	}
	
	@Test
	public void testSendListingRejectEmail() {
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("1234");
		ResponseEntity<String> responseEntity = new ResponseEntity<String>("{}", HttpStatus.OK);
		fraudEvalutionHelper.sendListingRejectSMS(1234l, 3456l);
		Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class),Mockito.anyVararg())).thenReturn(null);
		fraudEvalutionHelper.sendListingRejectSMS(1234l, 3456l);
		responseEntity = new ResponseEntity<String>("{}", HttpStatus.NOT_FOUND);
		Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class),Mockito.anyVararg())).thenReturn(responseEntity);
		fraudEvalutionHelper.sendListingRejectSMS(1234l, 3456l);
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenThrow(new RuntimeException("Exception!!"));
		fraudEvalutionHelper.sendListingRejectSMS(1234l, 3456l);
	}
	
	@Test
	public void testSendListingAcceptEmail() throws JsonParseException, JsonMappingException, IOException {
	    String eventJson =
		        "{\"id\":9813001,\"dateLastChance\":\"2017-04-18T19:00:00-04:00\",\"name\":\"NHL Eastern Conference Quarterfinals Tickets: Montreal Canadiens at New York Rangers (Home Game 2 - Series Game 4)\",\"status\":\"active\",\"expiredInd\":false,\"locale\":\"en_US\",\"currencyCode\":\"USD\",\"eventDateUTC\":\"2017-04-18T23:00:00Z\",\"eventDateLocal\":\"2017-04-18T19:00:00-04:00\",\"timezone\":\"US/Eastern\",\"bobId\":\"1\",\"eventUrl\":\"new-york-rangers-new-york-madison-square-garden-4-18-2017-9813001\",\"webURI\":\"new-york-rangers-tickets-new-york-rangers-new-york-madison-square-garden-4-18-2017/event/9813001/\",\"seoURI\":\"new-york-rangers-tickets-new-york-rangers-new-york-madison-square-garden-4-18-2017\",\"allowedViewingDomains\":[\"stubhub.com\"],\"categories\":[{\"id\":32,\"name\":\"Hockey Tickets\",\"webURI\":\"hockey-tickets/category/32/\",\"seoURI\":\"hockey-tickets\",\"locale\":\"en_US\"}],\"groupings\":[{\"id\":108321,\"name\":\"New York Rangers Playoff\",\"webURI\":\"new-york-rangers-playoff-tickets/grouping/108321/\",\"seoURI\":\"new-york-rangers-playoff-tickets\",\"locale\":\"en_US\"},{\"id\":109618,\"name\":\"Montreal Canadiens Playoff\",\"relationType\":\"PLAYOFF\",\"webURI\":\"montreal-canadiens-playoff-tickets/grouping/109618/\",\"seoURI\":\"montreal-canadiens-playoff-tickets\",\"locale\":\"en_US\"}],\"performers\":[{\"id\":2764,\"name\":\"New York Rangers\",\"webURI\":\"new-york-rangers-tickets/performer/2764/\",\"seoURI\":\"new-york-rangers-tickets\",\"locale\":\"en_US\"},{\"id\":7554,\"name\":\"Montreal Canadiens\",\"role\":\"AWAY_TEAM\",\"webURI\":\"montreal-canadiens-tickets/performer/7554/\",\"seoURI\":\"montreal-canadiens-tickets\"}],\"geography\":{\"id\":664,\"name\":\"New York Metro\",\"webURI\":\"new-york-metro-tickets/geography/664/\",\"seoURI\":\"new-york-metro-tickets\",\"locale\":\"en_US\"},\"ancestors\":{\"categories\":[{\"id\":28,\"name\":\"Sports tickets\",\"url\":\"sports-tickets\",\"webURI\":\"sports-tickets/category/28/\",\"seoURI\":\"sports-tickets\",\"locale\":\"en_US\"},{\"id\":32,\"name\":\"Hockey Tickets\",\"url\":\"hockey-tickets\",\"webURI\":\"hockey-tickets/category/32/\",\"seoURI\":\"hockey-tickets\",\"locale\":\"en_US\"}],\"groupings\":[{\"id\":144,\"name\":\"2016-2017 NHL\",\"url\":\"nhl-tickets\",\"webURI\":\"nhl-tickets/grouping/144/\",\"seoURI\":\"nhl-tickets\",\"locale\":\"en_US\"},{\"id\":108319,\"name\":\"NHL Playoff Tickets\",\"url\":\"nhl-playoff-tickets\",\"webURI\":\"nhl-playoff-tickets/grouping/108319/\",\"seoURI\":\"nhl-playoff-tickets\",\"locale\":\"en_US\"},{\"id\":108321,\"name\":\"New York Rangers Playoff\",\"url\":\"new-york-rangers-playoff-tickets\",\"webURI\":\"new-york-rangers-playoff-tickets/grouping/108321/\",\"seoURI\":\"new-york-rangers-playoff-tickets\",\"locale\":\"en_US\"}],\"performers\":[{\"id\":2764,\"name\":\"New York Rangers\",\"url\":\"new-york-rangers-tickets\",\"webURI\":\"new-york-rangers-tickets/performer/2764/\",\"seoURI\":\"new-york-rangers-tickets\"}],\"geographies\":[{\"id\":196976,\"name\":\"United States\",\"url\":\"united-states-tickets\",\"webURI\":\"united-states-tickets/geography/196976/\",\"seoURI\":\"united-states-tickets\",\"locale\":\"en_US\"},{\"id\":561,\"name\":\"New York\",\"url\":\"new-york-tickets\",\"webURI\":\"new-york-tickets/geography/561/\",\"seoURI\":\"new-york-tickets\",\"locale\":\"en_US\"},{\"id\":664,\"name\":\"New York Metro\",\"url\":\"new-york-metro-tickets\",\"webURI\":\"new-york-metro-tickets/geography/664/\",\"seoURI\":\"new-york-metro-tickets\",\"locale\":\"en_US\"}]},\"externalEventMappings\":[{\"namespace\":\"zv\",\"id\":\"374671589\"}],\"venue\":{\"id\":1282,\"name\":\"Madison Square Garden\",\"venueInfoUrl\":\"madison-square-garden\",\"venueEventsUrl\":\"madison-square-garden-tickets\",\"webURI\":\"madison-square-garden-tickets/venue/1282/\",\"seoURI\":\"madison-square-garden-tickets\",\"address1\":\"4 Pennsylvania Plaza\",\"locality\":\"New York\",\"postalCode\":\"10001\",\"state\":\"NY\",\"country\":\"US\",\"configurationId\":\"595667\",\"blendedIndicator\":false,\"longitude\":-73.993371,\"latitude\":40.750354},\"seoMeta\":{\"metaDescription\":\"Rangers - Canadiens Playoff Tickets - Buy and sell Montreal Canadiens vs New York Rangers NHL Eastern Conference Quarterfinals Tickets for April 18 at Madison Square Garden in New York, NY on StubHub!\",\"seoDescription\":\"Montreal Canadiens at New York Rangers\",\"seoTitle\":\"Canadiens - Rangers NHL Eastern Conference Quarterfinals Tickets - 2017-4-18\",\"keywords\":\"New York Rangers, Montreal Canadiens, NHL Eastern Conference Quarterfinals, New York Rangers Montreal Canadiens, Rangers vs Madison Square Garden, New York Rangers v Montreal Canadiens, Madison Square Garden, Rangers Canadiens 4/18, New York Rangers 4 18, NHL Eastern Conference Quarterfinals 04/18/2017, ticket, tickets, buy, sell\",\"title\":\"New York Rangers vs Montreal Canadiens [04/18/2017] Tickets on StubHub!\",\"locale\":\"en_US\"},\"images\":[{\"url\":\"http://i.ebayimg.com/images/g/bXQAAOSwls5Y7WFM/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/bXQAAOSwls5Y7WFM/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"Adobe Stock\",\"source\":\"Adobe Stock\",\"primaryInd\":false,\"height\":\"2811\",\"width\":\"4263\",\"imgOptimizationStatus\":\"unprocessed\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/CZYAAOSwv0tVeKAR/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/CZYAAOSwv0tVeKAR/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/YC0AAOSwKrhVeKBI/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/YC0AAOSwKrhVeKBI/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/1pkAAOSwl8NVeJ-U/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/1pkAAOSwl8NVeJ-U/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"Shutterstock\",\"source\":\"Shutterstock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/esMAAOSwpDdVeJ~5/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/esMAAOSwpDdVeJ~5/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/JVYAAOSwv0tVeKDl/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/JVYAAOSwv0tVeKDl/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/qDwAAOSwZd1VeKAi/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/qDwAAOSwZd1VeKAi/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/B28AAOSweW5VeKIZ/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/B28AAOSweW5VeKIZ/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"Corbis\",\"source\":\"Corbis\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/c5MAAOSw~OdVeKAy/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/c5MAAOSw~OdVeKAy/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/ljEAAOSwZd1VeJ-v/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/ljEAAOSwZd1VeJ-v/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/0w4AAOSweW5VeKC2/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/0w4AAOSweW5VeKC2/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/1fYAAOSwstxVeKDQ/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/1fYAAOSwstxVeKDQ/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/lwQAAOSwBahVeJ~N/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/lwQAAOSwBahVeJ~N/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"}],\"eventAttributes\":{\"gameType\":\"PLAYOFF\",\"eventType\":\"Not Known\",\"logisticsPath\":\"28/32/144/108319/108321\"},\"displayAttributes\":{\"title\":\"New York Rangers vs Montreal Canadiens [04/18/2017] Tickets on StubHub!\",\"hiddenInd\":false,\"locale\":\"en_US\",\"hideEventDate\":false,\"hideEventTime\":false,\"integratedEventInd\":false},\"mobileAttributes\":{\"stubhubMobileTicket\":true},\"dynamicAttributes\":[{\"name\":\"act_primary\",\"value\":\"New York Rangers\",\"type\":\"stubhub_display_attrib\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"game_type\",\"value\":\"PLAYOFF\",\"type\":\"stubhub_catalog_attrib\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"allowedViewingDomain\",\"value\":\"stubhub.com\",\"type\":\"stubhub_catalog_attrib\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_entity_feature_pq\",\"value\":\"0.244286\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_event_feature_kids_score\",\"value\":\"0.522123\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"logistics_path\",\"value\":\"28/32/144/108319/108321\",\"type\":\"Others\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_entity_feature_tpv_ems\",\"value\":\"0.0\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"act_secondary\",\"value\":\"Montreal Canadiens\",\"type\":\"stubhub_display_attrib\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_event_feature_nsfw_score\",\"value\":\"0.208771\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_entity_feature_value\",\"value\":\"0.638235\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"hide_event_date\",\"value\":\"false\",\"type\":\"stubhub_display_attrib\",\"status\":\"Active\"}]}";
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("1");
		ResponseEntity<String> responseEntity = new ResponseEntity<String>("{\"customer\":{\"userCookieGuid\":\"1234567890ASDF\",\"defaultContact\":{\"name\":{\"firstName\":\"FNU\",\"lastName\":\"LNU\"}}}}", HttpStatus.OK);
		ResponseEntity<String> userInfoResponseEntity = new ResponseEntity<String>("{\"customer\":{\"userCookieGuid\":\"1234567890ASDF\",\"defaultContact\":{\"name\":{\"firstName\":\"FNU\",\"lastName\":\"LNU\"}}}}", HttpStatus.OK);
		ResponseEntity<Event> eventResponseEntity = new ResponseEntity<Event>(getEvent(), HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class),Mockito.anyVararg())).thenReturn(responseEntity,userInfoResponseEntity,eventResponseEntity);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(getListing());
		fraudEvalutionHelper.sendListingAcceptEmail(1234l);
		userInfoResponseEntity = new ResponseEntity<String>("{\"customer\":{\"userCookieGuid\":\"1234567890ASDF\",\"defaultContact\":{\"name\":{\"firstName\":\"\",\"lastName\":\"\"}}}}", HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class),Mockito.anyVararg())).thenReturn(responseEntity,userInfoResponseEntity,eventResponseEntity);
		fraudEvalutionHelper.sendListingAcceptEmail(1234l);
		userInfoResponseEntity = new ResponseEntity<String>("{\"customer\":{\"userCookieGuid\":\"1234567890ASDF\",\"defaultContact\":{}}}", HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class),Mockito.anyVararg())).thenReturn(responseEntity,userInfoResponseEntity,eventResponseEntity);
		fraudEvalutionHelper.sendListingAcceptEmail(1234l);
		userInfoResponseEntity = new ResponseEntity<String>("abcdefgh", HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class),Mockito.anyVararg())).thenReturn(responseEntity,userInfoResponseEntity,eventResponseEntity);
		fraudEvalutionHelper.sendListingAcceptEmail(1234l);
		responseEntity = new ResponseEntity<String>("abcdefgh", HttpStatus.OK);
		userInfoResponseEntity = new ResponseEntity<String>("{\"customer\":{\"userCookieGuid\":\"1234567890ASDF\",\"defaultContact\":{\"name\":{\"firstName\":\"\",\"lastName\":\"\"}}}}", HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class),Mockito.anyVararg())).thenReturn(responseEntity,responseEntity,responseEntity,userInfoResponseEntity,eventResponseEntity);
		fraudEvalutionHelper.sendListingAcceptEmail(1234l);
		 responseEntity = new ResponseEntity<String>("{\"customer\":{\"userCookieGuid\":\"1234567890ASDF\",\"defaultContact\":{\"name\":{\"firstName\":\"FNU\",\"lastName\":\"LNU\"}}}}", HttpStatus.OK);
		userInfoResponseEntity = new ResponseEntity<String>("{\"customer\":{\"userCookieGuid\":\"1234567890ASDF\",\"preferredLocale\":\"en_US\",\"defaultContact\":{\"name\":{\"firstName\":\"FNU\",\"lastName\":\"LNU\"}}}}", HttpStatus.OK);
		Event event = null; 
		eventResponseEntity = new ResponseEntity<Event>(event, HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class),Mockito.anyVararg())).thenReturn(responseEntity,userInfoResponseEntity,eventResponseEntity);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(getListing());
		fraudEvalutionHelper.sendListingAcceptEmail(1234l);
		eventResponseEntity = new ResponseEntity<Event>(getEvent(), HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class),Mockito.anyVararg())).thenReturn(responseEntity,userInfoResponseEntity,eventResponseEntity);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(null);
		fraudEvalutionHelper.sendListingAcceptEmail(1234l);
	}
	
	@Test
	public void testProcessListingUpdate()  {
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("1");
		ResponseEntity<String> responseEntity = new ResponseEntity<String>("{\"customer\":{\"userCookieGuid\":\"1234567890ASDF\",\"defaultContact\":{\"name\":{\"firstName\":\"FNU\",\"lastName\":\"LNU\"}}}}", HttpStatus.OK);
		ResponseEntity<String> userInfoResponseEntity = new ResponseEntity<String>("{\"customer\":{\"userCookieGuid\":\"1234567890ASDF\",\"defaultContact\":{\"name\":{\"firstName\":\"FNU\",\"lastName\":\"LNU\"}}}}", HttpStatus.OK);
		Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class),Mockito.anyVararg())).thenReturn(responseEntity,userInfoResponseEntity);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(getListing());
		Mockito.when(inventoryMgr.getActiveListingsBySellerId(Mockito.anyLong())).thenReturn(getActiveListings());
		FraudStatusUpdateRequest request = getUpdateRequest();
		fraudEvalutionHelper.processListingUpdate(request,fraudSellerDeactivationMsgProducer);
		request = getUpdateRequest();
		request.setFraudCheckStatusId(1000l);
		fraudEvalutionHelper.processListingUpdate(request,fraudSellerDeactivationMsgProducer);
		request = getUpdateRequest();
		request.setFraudCheckStatusId(1000l);
		request.setIsSellerDeactivated(true);
		Mockito.when(cacheStore.get(Mockito.anyString())).thenReturn("abcd");
		fraudEvalutionHelper.processListingUpdate(request,fraudSellerDeactivationMsgProducer);
		Mockito.when(cacheStore.get(Mockito.anyString())).thenReturn(null,null,"abcd");
		fraudEvalutionHelper.processListingUpdate(request,fraudSellerDeactivationMsgProducer);
//		Mockito.when(cacheStore.get(Mockito.anyString())).thenReturn(null,null,"abcd");
//		Mockito.when(inventoryMgr.getActiveListing(Mockito.anyLong())).thenThrow(Exception.class);
//		try {
//		fraudEvalutionHelper.processListingUpdate(request);
//		Assert.fail();
//		}catch(Exception e) {
//			
//		}

	}
	
	private FraudStatusUpdateRequest getUpdateRequest() {
		FraudStatusUpdateRequest request = new FraudStatusUpdateRequest();
		
		request.setFraudCheckStatus("FRAUD_CHECK_STAUS");
		request.setFraudCheckStatusId(500l);
		request.setFraudResolutionId(1234l);
		request.setListingId(12345l);
		request.setSellerId(1234l);
		request.setIsSellerDeactivated(false);
		
		return request;
	}

	private List<Listing> getActiveListings(){
		return Arrays.asList(getListing(),getListing(),getListing());
	}
	
	
	private Listing getListing() {
		Listing listing = new Listing();
		listing.setId(12345L);
		listing.setExternalId("EXT-1234");
		listing.setEventId(1234L);
		listing.setSellerId(123456L);
		listing.setListPrice(new Money("12"));
		listing.setSection("Lower Box");
		listing.setQuantity(4);
		listing.setQuantityRemain(2);
		listing.setListingType(ListingType.TICKETS_WITH_PARKING_PASSES_INCLUDED.getId());
		listing.setFulfillmentMethod(FulfillmentMethod.BARCODE);
		listing.setListingType(123l);
		listing.setTicketMedium(123);
		
		TicketSeat ts1 = new TicketSeat();
		ts1.setSection("Lower Box");
		ts1.setRow("1");
		ts1.setSeatNumber("1");
		ts1.setSeatStatusId(1L);
		
		TicketSeat ts2 = new TicketSeat();
		ts2.setSection("Lower Box");
		ts2.setRow("1");
		ts2.setSeatNumber("2");
		ts2.setSeatStatusId(1L);
		
		List<TicketSeat> ticketSeatList = new ArrayList<TicketSeat>();
		ticketSeatList.add(ts1);
		ticketSeatList.add(ts2);
		listing.setTicketSeats(ticketSeatList);
		
		return listing;
	}
	
	 private Event getEvent() throws JsonParseException, JsonMappingException, IOException {

		    String eventJson =
		        "{\"id\":9813001,\"dateLastChance\":\"2017-04-18T19:00:00-04:00\",\"name\":\"NHL Eastern Conference Quarterfinals Tickets: Montreal Canadiens at New York Rangers (Home Game 2 - Series Game 4)\",\"status\":\"active\",\"expiredInd\":false,\"locale\":\"en_US\",\"currencyCode\":\"USD\",\"eventDateUTC\":\"2017-04-18T23:00:00Z\",\"eventDateLocal\":\"2017-04-18T19:00:00-04:00\",\"timezone\":\"US/Eastern\",\"bobId\":\"1\",\"eventUrl\":\"new-york-rangers-new-york-madison-square-garden-4-18-2017-9813001\",\"webURI\":\"new-york-rangers-tickets-new-york-rangers-new-york-madison-square-garden-4-18-2017/event/9813001/\",\"seoURI\":\"new-york-rangers-tickets-new-york-rangers-new-york-madison-square-garden-4-18-2017\",\"allowedViewingDomains\":[\"stubhub.com\"],\"categories\":[{\"id\":32,\"name\":\"Hockey Tickets\",\"webURI\":\"hockey-tickets/category/32/\",\"seoURI\":\"hockey-tickets\",\"locale\":\"en_US\"}],\"groupings\":[{\"id\":108321,\"name\":\"New York Rangers Playoff\",\"webURI\":\"new-york-rangers-playoff-tickets/grouping/108321/\",\"seoURI\":\"new-york-rangers-playoff-tickets\",\"locale\":\"en_US\"},{\"id\":109618,\"name\":\"Montreal Canadiens Playoff\",\"relationType\":\"PLAYOFF\",\"webURI\":\"montreal-canadiens-playoff-tickets/grouping/109618/\",\"seoURI\":\"montreal-canadiens-playoff-tickets\",\"locale\":\"en_US\"}],\"performers\":[{\"id\":2764,\"name\":\"New York Rangers\",\"webURI\":\"new-york-rangers-tickets/performer/2764/\",\"seoURI\":\"new-york-rangers-tickets\",\"locale\":\"en_US\"},{\"id\":7554,\"name\":\"Montreal Canadiens\",\"role\":\"AWAY_TEAM\",\"webURI\":\"montreal-canadiens-tickets/performer/7554/\",\"seoURI\":\"montreal-canadiens-tickets\"}],\"geography\":{\"id\":664,\"name\":\"New York Metro\",\"webURI\":\"new-york-metro-tickets/geography/664/\",\"seoURI\":\"new-york-metro-tickets\",\"locale\":\"en_US\"},\"ancestors\":{\"categories\":[{\"id\":28,\"name\":\"Sports tickets\",\"url\":\"sports-tickets\",\"webURI\":\"sports-tickets/category/28/\",\"seoURI\":\"sports-tickets\",\"locale\":\"en_US\"},{\"id\":32,\"name\":\"Hockey Tickets\",\"url\":\"hockey-tickets\",\"webURI\":\"hockey-tickets/category/32/\",\"seoURI\":\"hockey-tickets\",\"locale\":\"en_US\"}],\"groupings\":[{\"id\":144,\"name\":\"2016-2017 NHL\",\"url\":\"nhl-tickets\",\"webURI\":\"nhl-tickets/grouping/144/\",\"seoURI\":\"nhl-tickets\",\"locale\":\"en_US\"},{\"id\":108319,\"name\":\"NHL Playoff Tickets\",\"url\":\"nhl-playoff-tickets\",\"webURI\":\"nhl-playoff-tickets/grouping/108319/\",\"seoURI\":\"nhl-playoff-tickets\",\"locale\":\"en_US\"},{\"id\":108321,\"name\":\"New York Rangers Playoff\",\"url\":\"new-york-rangers-playoff-tickets\",\"webURI\":\"new-york-rangers-playoff-tickets/grouping/108321/\",\"seoURI\":\"new-york-rangers-playoff-tickets\",\"locale\":\"en_US\"}],\"performers\":[{\"id\":2764,\"name\":\"New York Rangers\",\"url\":\"new-york-rangers-tickets\",\"webURI\":\"new-york-rangers-tickets/performer/2764/\",\"seoURI\":\"new-york-rangers-tickets\"}],\"geographies\":[{\"id\":196976,\"name\":\"United States\",\"url\":\"united-states-tickets\",\"webURI\":\"united-states-tickets/geography/196976/\",\"seoURI\":\"united-states-tickets\",\"locale\":\"en_US\"},{\"id\":561,\"name\":\"New York\",\"url\":\"new-york-tickets\",\"webURI\":\"new-york-tickets/geography/561/\",\"seoURI\":\"new-york-tickets\",\"locale\":\"en_US\"},{\"id\":664,\"name\":\"New York Metro\",\"url\":\"new-york-metro-tickets\",\"webURI\":\"new-york-metro-tickets/geography/664/\",\"seoURI\":\"new-york-metro-tickets\",\"locale\":\"en_US\"}]},\"externalEventMappings\":[{\"namespace\":\"zv\",\"id\":\"374671589\"}],\"venue\":{\"id\":1282,\"name\":\"Madison Square Garden\",\"venueInfoUrl\":\"madison-square-garden\",\"venueEventsUrl\":\"madison-square-garden-tickets\",\"webURI\":\"madison-square-garden-tickets/venue/1282/\",\"seoURI\":\"madison-square-garden-tickets\",\"address1\":\"4 Pennsylvania Plaza\",\"locality\":\"New York\",\"postalCode\":\"10001\",\"state\":\"NY\",\"country\":\"US\",\"configurationId\":\"595667\",\"blendedIndicator\":false,\"longitude\":-73.993371,\"latitude\":40.750354},\"seoMeta\":{\"metaDescription\":\"Rangers - Canadiens Playoff Tickets - Buy and sell Montreal Canadiens vs New York Rangers NHL Eastern Conference Quarterfinals Tickets for April 18 at Madison Square Garden in New York, NY on StubHub!\",\"seoDescription\":\"Montreal Canadiens at New York Rangers\",\"seoTitle\":\"Canadiens - Rangers NHL Eastern Conference Quarterfinals Tickets - 2017-4-18\",\"keywords\":\"New York Rangers, Montreal Canadiens, NHL Eastern Conference Quarterfinals, New York Rangers Montreal Canadiens, Rangers vs Madison Square Garden, New York Rangers v Montreal Canadiens, Madison Square Garden, Rangers Canadiens 4/18, New York Rangers 4 18, NHL Eastern Conference Quarterfinals 04/18/2017, ticket, tickets, buy, sell\",\"title\":\"New York Rangers vs Montreal Canadiens [04/18/2017] Tickets on StubHub!\",\"locale\":\"en_US\"},\"images\":[{\"url\":\"http://i.ebayimg.com/images/g/bXQAAOSwls5Y7WFM/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/bXQAAOSwls5Y7WFM/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"Adobe Stock\",\"source\":\"Adobe Stock\",\"primaryInd\":false,\"height\":\"2811\",\"width\":\"4263\",\"imgOptimizationStatus\":\"unprocessed\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/CZYAAOSwv0tVeKAR/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/CZYAAOSwv0tVeKAR/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/YC0AAOSwKrhVeKBI/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/YC0AAOSwKrhVeKBI/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/1pkAAOSwl8NVeJ-U/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/1pkAAOSwl8NVeJ-U/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"Shutterstock\",\"source\":\"Shutterstock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/esMAAOSwpDdVeJ~5/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/esMAAOSwpDdVeJ~5/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/JVYAAOSwv0tVeKDl/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/JVYAAOSwv0tVeKDl/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/qDwAAOSwZd1VeKAi/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/qDwAAOSwZd1VeKAi/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/B28AAOSweW5VeKIZ/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/B28AAOSweW5VeKIZ/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"Corbis\",\"source\":\"Corbis\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/c5MAAOSw~OdVeKAy/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/c5MAAOSw~OdVeKAy/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/ljEAAOSwZd1VeJ-v/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/ljEAAOSwZd1VeJ-v/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/0w4AAOSweW5VeKC2/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/0w4AAOSweW5VeKC2/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/1fYAAOSwstxVeKDQ/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/1fYAAOSwstxVeKDQ/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"},{\"url\":\"http://i.ebayimg.com/images/g/lwQAAOSwBahVeJ~N/s-l1600.jpg\",\"urlSsl\":\"https://ssli.ebayimg.com/images/g/lwQAAOSwBahVeJ~N/s-l1600.jpg\",\"type\":\"REGULAR\",\"resizableInd\":true,\"credit\":\"iStock\",\"source\":\"iStock\",\"primaryInd\":false,\"height\":\"1000\",\"width\":\"1600\",\"locale\":\"en_US\"}],\"eventAttributes\":{\"gameType\":\"PLAYOFF\",\"eventType\":\"Not Known\",\"logisticsPath\":\"28/32/144/108319/108321\"},\"displayAttributes\":{\"title\":\"New York Rangers vs Montreal Canadiens [04/18/2017] Tickets on StubHub!\",\"hiddenInd\":false,\"locale\":\"en_US\",\"hideEventDate\":false,\"hideEventTime\":false,\"integratedEventInd\":false},\"mobileAttributes\":{\"stubhubMobileTicket\":true},\"dynamicAttributes\":[{\"name\":\"act_primary\",\"value\":\"New York Rangers\",\"type\":\"stubhub_display_attrib\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"game_type\",\"value\":\"PLAYOFF\",\"type\":\"stubhub_catalog_attrib\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"allowedViewingDomain\",\"value\":\"stubhub.com\",\"type\":\"stubhub_catalog_attrib\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_entity_feature_pq\",\"value\":\"0.244286\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_event_feature_kids_score\",\"value\":\"0.522123\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"logistics_path\",\"value\":\"28/32/144/108319/108321\",\"type\":\"Others\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_entity_feature_tpv_ems\",\"value\":\"0.0\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"act_secondary\",\"value\":\"Montreal Canadiens\",\"type\":\"stubhub_display_attrib\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_event_feature_nsfw_score\",\"value\":\"0.208771\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"zvents_entity_feature_value\",\"value\":\"0.638235\",\"type\":\"Features\",\"status\":\"active\",\"locale\":\"en_US\",\"dataType\":\"String\"},{\"name\":\"hide_event_date\",\"value\":\"false\",\"type\":\"stubhub_display_attrib\",\"status\":\"Active\"}]}";
		    ObjectMapper mapper = new ObjectMapper();
		    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		    Event event = (Event) mapper.readValue(eventJson, Event.class);
		    return event;

		  }
	 
		@Test
		public void testSubmitToListingDeactivationQueue()  {
			Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("1");
			ResponseEntity<String> responseEntity = new ResponseEntity<String>("{\"customer\":{\"userCookieGuid\":\"1234567890ASDF\",\"defaultContact\":{\"name\":{\"firstName\":\"FNU\",\"lastName\":\"LNU\"}}}}", HttpStatus.OK);
			ResponseEntity<String> userInfoResponseEntity = new ResponseEntity<String>("{\"customer\":{\"userCookieGuid\":\"1234567890ASDF\",\"defaultContact\":{\"name\":{\"firstName\":\"FNU\",\"lastName\":\"LNU\"}}}}", HttpStatus.OK);
			Mockito.when(restTemplate.exchange(Mockito.anyString(),Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class),Mockito.anyVararg())).thenReturn(responseEntity,userInfoResponseEntity);
			Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(getListing());
			Mockito.when(inventoryMgr.getActiveListingsBySellerId(Mockito.anyLong())).thenReturn(getActiveListings());
			FraudStatusUpdateRequest request = getUpdateRequest();
			fraudEvalutionHelper.submitToListingDeactivationQueue(fraudSellerDeactivationMsgProducer, request);
			request = getUpdateRequest();
			request.setFraudCheckStatusId(1000l);
			fraudEvalutionHelper.submitToListingDeactivationQueue(fraudSellerDeactivationMsgProducer, request);
			request = getUpdateRequest();
			request.setFraudCheckStatusId(1000l);
			request.setIsSellerDeactivated(true);
			Mockito.when(cacheStore.get(Mockito.anyString())).thenReturn("abcd");
			fraudEvalutionHelper.submitToListingDeactivationQueue(fraudSellerDeactivationMsgProducer, request);
			Mockito.when(cacheStore.get(Mockito.anyString())).thenReturn(null,null,"abcd");
			fraudEvalutionHelper.submitToListingDeactivationQueue(fraudSellerDeactivationMsgProducer, request);
//			Mockito.when(cacheStore.get(Mockito.anyString())).thenReturn(null,null,"abcd");
//			Mockito.when(inventoryMgr.getActiveListing(Mockito.anyLong())).thenThrow(Exception.class);
//			try {
//			fraudEvalutionHelper.processListingUpdate(request);
//			Assert.fail();
//			}catch(Exception e) {
//				
//			}

		}
	
}
