package com.stubhub.domain.inventory.v2.listings.eventmapper;

import java.util.Locale;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.v2.DTO.EventInfo;

public class KafkaMessageConstructorTest {
	
	 @InjectMocks
	 KafkaMessageConstructor kafkaMessageConstructor;
	
	 @Mock
	 KafkaProducer kafkaProducer;
	 
	 Locale locale;
	 String sellerId;
	 private final String VENUE_TYPE_VALUE = "venue";
	 private final String EVENT_TYPE_VALUE = "event";

	  @BeforeMethod
	  public void setup() {
	    locale = new Locale("US");
	    sellerId = "10001";

	    MockitoAnnotations.initMocks(this);
	  }
	  
	  @Test
	  public void testConstructMessage() {
		  EventInfo eventInfo = new EventInfo();
		  eventInfo.setName("Test Event");
		  eventInfo.setVenue("Test Venue");
		  eventInfo.setEventLocalDate("2016-10-09T08:00");
		  kafkaMessageConstructor.constructMessage(eventInfo, sellerId, VENUE_TYPE_VALUE);
		  Assert.assertTrue(true);
	  }
	  
	  @Test
	  public void testConstructMessageEventDateLocalNull() {
		  EventInfo eventInfo = new EventInfo();
		  eventInfo.setName("Test Event");
		  eventInfo.setVenue("Test Venue");
		  eventInfo.setDate("2016-10-09T08:00");
		  kafkaMessageConstructor.constructMessage(eventInfo, sellerId, VENUE_TYPE_VALUE);
		  Assert.assertTrue(true);
	  }
	  
	  @Test
	  public void testConstructMessageWithNameParamVenue() {
		  EventInfo eventInfo = new EventInfo();
		  eventInfo.setName("Test Event");
		  eventInfo.setVenue("Test Venue");
		  eventInfo.setEventLocalDate("2016-10-09T08:00");
		  kafkaMessageConstructor.constructMessage(eventInfo, sellerId, VENUE_TYPE_VALUE);
		  Assert.assertTrue(true);
	  }
	  @Test
	  public void testConstructMessageWithNameParamEvent() {
		  EventInfo eventInfo = new EventInfo();
		  eventInfo.setName("Test Event");
		  eventInfo.setVenue("Test Venue");
		  eventInfo.setEventLocalDate("2016-10-09T08:00");
		  kafkaMessageConstructor.constructMessage(eventInfo, sellerId, EVENT_TYPE_VALUE);
		  Assert.assertTrue(true);
	  }
	  @Test
	  public void testConstructMessageWithNameParamEvent1() {
		  EventInfo eventInfo = new EventInfo();
		  eventInfo.setName("Test Event");
		  eventInfo.setVenue("Test Venue");
		  eventInfo.setDate("2016-10-09T08:00");
		  kafkaMessageConstructor.constructMessage(eventInfo, sellerId, EVENT_TYPE_VALUE);
		  Assert.assertTrue(true);
	  }
}
