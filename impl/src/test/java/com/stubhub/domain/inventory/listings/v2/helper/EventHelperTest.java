package com.stubhub.domain.inventory.listings.v2.helper;

import com.stubhub.domain.catalog.read.v3.intf.common.dto.response.AncestorItem;
import com.stubhub.domain.catalog.read.v3.intf.common.dto.response.Ancestors;
import com.stubhub.domain.catalog.read.v3.intf.common.dto.response.CommonAttribute;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.EventAttribute;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.EventDisplayAttribute;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.SeatTrait;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Venue;
import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.*;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHBadRequestException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHResourceNotFoundException;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.api.EventV3APIHelper;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.api.VenueConfigV3ApiHelper;
import com.stubhub.domain.inventory.listings.v2.util.EventHelper;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventHelperTest {

	private EventHelper eventHelper;
	private EventV3APIHelper eventV3APIHelper;
	private VenueConfigV3ApiHelper venueConfigV3ApiHelper;

	private SvcLocator svcLocator;
	private WebClient webClient;
	private ObjectMapper objectMapper;
	
	@BeforeMethod
	public void setUp(){
		eventHelper = new EventHelper() {
			protected String getProperty(String propertyName, String defaultValue) {
				return EventHelperTest.this.getProperty(propertyName);
			}			
		};
		eventV3APIHelper = new EventV3APIHelper() {
			protected String getProperty(String propertyName, String defaultValue) {
				return EventHelperTest.this.getProperty(propertyName);
			}
		};
		venueConfigV3ApiHelper = new VenueConfigV3ApiHelper() {
			protected String getProperty(String propertyName, String defaultValue) {
				return EventHelperTest.this.getProperty(propertyName);
			}
		};
		svcLocator = Mockito.mock(SvcLocator.class);
		webClient  = Mockito.mock(WebClient.class);
		objectMapper  = Mockito.mock(ObjectMapper.class);

		ReflectionTestUtils.setField(eventV3APIHelper, "svcLocator", svcLocator);
		ReflectionTestUtils.setField(eventV3APIHelper, "objectMapper", objectMapper);

		ReflectionTestUtils.setField(venueConfigV3ApiHelper, "svcLocator", svcLocator);
		ReflectionTestUtils.setField(venueConfigV3ApiHelper, "objectMapper", objectMapper);

		ReflectionTestUtils.setField(eventHelper, "eventV3APIHelper", eventV3APIHelper);
		ReflectionTestUtils.setField(eventHelper, "venueConfigV3ApiHelper", venueConfigV3ApiHelper);
	}

	private String getProperty(String propertyName) {
		if ("catalog.api.url".equals(propertyName)) {
			return "https://api.srwd34.com/catalog/events/v1/{eventId}/metadata";
		}
		if ("sell.declingDatePeriod".equals(propertyName)) {
			return "2";
		}
		if("catalog.get.venue.seatingsection.v3.api.url".equals(propertyName)){
			return  "http://api-int.stubprod.com/catalog-read/v3/venues/seatingSections/{sectionId}";
		}
		return "";
	}

	@Test
	public void testGetEventById() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getEventV3Response());
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventResponse = getEventV3ResponseObject();
		Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event.class))).thenReturn(eventResponse);
		VenueConfigurations venueConfigs = getVenueConfigs();
		Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfigurations.class))).thenReturn(venueConfigs);
		Event event = eventHelper.getEventById(12345L, "event venue", null, true);
		Assert.assertNotNull(event);
	}
	
	@Test
	public void testGetEventByIdOtherAttr() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getEventV3Response());
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventResponse = getEventV3ResponseObject();
		List <? extends CommonAttribute> dynamicAttributes =eventResponse.getDynamicAttributes();
        for(CommonAttribute ca:dynamicAttributes)
        {
        	ca.setValue("TESTVALUE");
        }
		
		Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event.class))).thenReturn(eventResponse);
		VenueConfigurations venueConfigs = getVenueConfigs();
		Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfigurations.class))).thenReturn(venueConfigs);
		Event event = eventHelper.getEventById(12345L, "event venue", null, true);
		Assert.assertNotNull(event);
	}
	
	@Test
	public void testGetEventByIdEnableHybridMap() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getEventV3Response());
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventResponse = getEventV3ResponseObjectWithEnableHybridMap();

		Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event.class))).thenReturn(eventResponse);
		VenueConfigurations venueConfigs = getVenueConfigs();
		Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfigurations.class))).thenReturn(venueConfigs);
		Event event = eventHelper.getEventById(12345L, "event venue", null, true);
		Assert.assertNotNull(event);
	}
	
	@Test
	public void testGetEventByIdNoAttr() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getEventV3Response());
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventResponse = getEventV3ResponseObject();
		eventResponse.setDynamicAttributes(null);
		Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event.class))).thenReturn(eventResponse);
		VenueConfigurations venueConfigs = getVenueConfigs();
		Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfigurations.class))).thenReturn(venueConfigs);
		Event event = eventHelper.getEventById(12345L, "event venue", null, true);
		Assert.assertNotNull(event);
	}
	
	@Test
	public void testGetEventByIdEmptyAttr() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getEventV3Response());
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventResponse = getEventV3ResponseObject();
		eventResponse.setDynamicAttributes(new ArrayList());
		Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event.class))).thenReturn(eventResponse);
		VenueConfigurations venueConfigs = getVenueConfigs();
		Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfigurations.class))).thenReturn(venueConfigs);
		Event event = eventHelper.getEventById(12345L, "event venue", null, true);
		Assert.assertNotNull(event);
	}
	
	@Test
	public void testGetEventV3ById() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getEventV3Response());
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventResponse = getEventV3ResponseObject();
		Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event.class))).thenReturn(eventResponse);
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event event = eventHelper.getEventV3ById(123L, Locale.US, true);
		Assert.assertNotNull(event);
	}
	
	@Test(expectedExceptions = SHSystemException.class)
	public void testGetEventV3ByIdException(){
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.doThrow(Exception.class).when(webClient).get();
		eventHelper.getEventV3ById(123L, Locale.US, true);
	}
	
	@Test(expectedExceptions = SHResourceNotFoundException.class)
	public void testGetEventV3ByIdNon200(){
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getEventV3ResponseBadRequest());
		eventHelper.getEventV3ById(123L, Locale.US, true);
	}
	
	@Test
    public void testGetEventObject() {
        Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
        Mockito.when(webClient.get()).thenReturn(getEventV3ResponseBadRequest());
        Listing listing = new Listing();
        listing.setSystemStatus("ACTIVE");
        
        try {
          eventHelper.getEventObject(Locale.US,listing,123L,true);
        } catch (ListingBusinessException e) {
          Assert.assertTrue(true);
        }catch(Exception e){
          Assert.fail("Should not have thrown any other exception");
        }
    }
	
	@Test
    public void testGetEventObjectBadRequestInvalidEvent() {
        Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
        Mockito.when(webClient.get()).thenReturn(getEventV3Response());
        Listing listing = new Listing();
        listing.setSystemStatus("ACTIVE");
        
        try {
          eventHelper.getEventObject(Locale.US,listing,123L,true);
        } catch (ListingBusinessException e) {
          Assert.assertTrue(true);
        }catch(Exception e){
          Assert.fail("Should not have thrown any other exception");
        }
    }
	
	@Test
    public void testGetEventObjectUnknownErrorRequest() {
        Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
        Mockito.when(webClient.get()).thenReturn(getEventV3ResponseConflictRequest());
        Listing listing = new Listing();
        listing.setSystemStatus("ACTIVE");
        
        try {
          eventHelper.getEventObject(Locale.US,listing,123L,true);
        } catch (ListingBusinessException e) {
          Assert.assertTrue(true);
        }catch(Exception e){
          Assert.fail("Should not have thrown any other exception");
        }
    }
	
	
	
	@Test
    public void testGetEventObjectBadRequestEvent() throws Exception{
        Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
        Mockito.when(webClient.get()).thenReturn(getExpiredEventV3Response());
        Listing listing = new Listing();
        listing.setSystemStatus("ACTIVE");
        com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventResponse = getEventV3ResponseObject();
        eventResponse.setExpiredInd(true);
        Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event.class))).thenReturn(eventResponse);
        Mockito.when(objectMapper.configure(Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false))).thenReturn(objectMapper);
        
        try {
          eventHelper.getEventObject(Locale.US,listing,123L,true);
        } catch (ListingBusinessException e) {
          Assert.assertTrue(true);
        }catch(Exception e){
          Assert.fail("Should not have thrown any other exception");
        }
    }
	
	@Test
    public void testGetEventObjectBadRequestEventDeleteAction() throws Exception{
        Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
        Mockito.when(webClient.get()).thenReturn(getExpiredEventV3Response());
        Listing listing = new Listing();
        listing.setSystemStatus("DELETED");
         
        try {
          eventHelper.getEventObject(Locale.US,listing,123L,true);
          
        } catch(Exception e){
          Assert.assertTrue(true);
        }
    }
	
	@Test
    public void testGetEventObjectBadRequestInactiveEvent() throws Exception{
        Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
        Mockito.when(webClient.get()).thenReturn(getExpiredEventV3Response());
        Listing listing = new Listing();
        listing.setSystemStatus("ACTIVE");
        com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventResponse = getEventV3ResponseObject();        
        Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event.class))).thenReturn(eventResponse);
        Mockito.when(objectMapper.configure(Mockito.eq(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES), Mockito.eq(false))).thenReturn(objectMapper);
        
        try {
          eventHelper.getEventObject(Locale.US,listing,123L,true);
        } catch (ListingBusinessException e) {
          Assert.assertTrue(true);
        }catch(Exception e){
          Assert.fail("Should not have thrown any other exception");
        }
    }
	
	@Test(expectedExceptions = Exception.class)
	public void testGetEventV3ById500(){
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getEventV3ResponseSystemError());
		eventHelper.getEventV3ById(123L, Locale.US, true);
	}
	
	@Test(expectedExceptions = SHBadRequestException.class)
	public void testValidateIfEventExpired1() throws Exception {
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event event = getEventV3ResponseObject();
		event.setExpiredInd(true);
		event.setDateLastChance("2016-02-20T22:56:05-08:00");
		eventHelper.validateIfEventExpired(event, false);
	}
	
    public void testValidateIfEventExpired2() throws Exception {
        com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event event = getEventV3ResponseObject();
        event.setExpiredInd(true);
        eventHelper.validateIfEventExpired(event, false);
    }

	public void testValidateIfEventInactive() throws Exception {
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event event = getEventV3ResponseObject();
		event.setStatus("inactive");
		eventHelper.validateIfEventExpired(event, false);
	}
	
	public void testValidateIfEventValid() throws Exception {
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event event = getEventV3ResponseObject();
		event.setStatus("contingent");
		eventHelper.validateIfEventExpired(event, false);
		Assert.assertTrue(true);
		event.setStatus("scheduled");
		eventHelper.validateIfEventExpired(event, false);
		Assert.assertTrue(true);
	}
	
	@Test
	public void testGetVenueDetails() throws Exception{
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getVenueConfigResponse());
		VenueConfigurations venueConfigs = getVenueConfigs();
		Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfigurations.class))).thenReturn(venueConfigs);
		VenueConfiguration venueConfig = eventHelper.getVenueDetails(123L);
		Assert.assertNotNull(venueConfig);
	}
	
	@Test
	public void testVenueDetailsException(){
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.doThrow(Exception.class).when(webClient).get();
		VenueConfiguration venueConfig = eventHelper.getVenueDetails(123L);
		Assert.assertNull(venueConfig);
	}
	
	@Test
	public void testVenueDetailsNon200(){
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getVenueConfigResponseNon200());
		VenueConfiguration venueConfig = eventHelper.getVenueDetails(1L);
		Assert.assertNull(venueConfig);
	}
	

	@Test
	public void testGetLocalizedSection() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getVenueSeatingSectionsResponse());
		SeatingSection seatingSection = new SeatingSection();
		seatingSection.setName("test");
		seatingSection.setId(1847922L);
		Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(SeatingSection.class))).thenReturn(seatingSection);
		String seatingSectionResp = eventHelper.getLocalizedSeatingSectionName(1847922L, Locale.US);
		Assert.assertNotNull(seatingSectionResp);
	}
	
	@Test
	public void testGetLocalizedSectionFilure() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getVenueConfigResponseNon200());
		String seatingSectionResp = eventHelper.getLocalizedSeatingSectionName(1847922L, Locale.US);
		Assert.assertNull(seatingSectionResp);
	}
	
	@Test
	public void testGetLocalizedZone() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getVenueSeatingZoneSResponse());
		SeatingZone seatingzone = new SeatingZone();
		seatingzone.setName("test");
		seatingzone.setId(1847922L);
		Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(SeatingZone.class))).thenReturn(seatingzone);
		String seatingZoneResp = eventHelper.getLocalizedSeatingZoneName(1847922L, Locale.US);
		Assert.assertNotNull(seatingZoneResp);
	}
	
	@Test
	public void testGetLocalizedZoneFilure() throws Exception {
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getVenueConfigResponseNon200());
		String seatingZoneResp = eventHelper.getLocalizedSeatingZoneName(1847922L, Locale.US);
		Assert.assertNull(seatingZoneResp);
	}
	
	
	
	private com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event getEventV3ResponseObject() {
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event event = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();
		event.setId(9314333L);
		event.setName("Test Event");
		event.setStatus("ACTIVE");
		event.setExpiredInd(false);
		event.setBobId("1");
		event.setCurrencyCode("USD");
		event.setEventDateLocal("2017-02-20T22:56:05-08:00");
		event.setEventDateUTC("2017-02-21T06:56:05Z");
		event.setTimezone("PST");
		event.setDateLastChance("2017-02-20T22:56:05-08:00");
		
		Ancestors ancestors = new Ancestors();
		List<AncestorItem> categoryIds = new ArrayList<AncestorItem>();
		List<AncestorItem> groupingIds = new ArrayList<AncestorItem>();
		List<AncestorItem> performerIds = new ArrayList<AncestorItem>();
		List<AncestorItem> geographies = new ArrayList<AncestorItem>();
		
		AncestorItem item1 = new AncestorItem();
		item1.setId(1L);
		item1.setName("Category");
		AncestorItem item2 = new AncestorItem();
		item2.setId(2L);
		item2.setName("Grouping");
		AncestorItem item3 = new AncestorItem();
		item3.setId(3L);
		item3.setName("Performer");
		AncestorItem item4 = new AncestorItem();
		item4.setId(4L);
		item4.setName("Geography");
		
		categoryIds.add(item1);
		groupingIds.add(item2);
		performerIds.add(item3);
		geographies.add(item4);
		
		ancestors.setCategories(categoryIds);
		ancestors.setGroupings(groupingIds);
		ancestors.setPerformers(performerIds);
		ancestors.setGeographies(geographies);
		
		event.setAncestors(ancestors);
		
		Venue venue = new Venue();
		venue.setId(12L);
		venue.setName("Park");
		venue.setConfigurationId("1232");
		venue.setCountry("US");
		event.setVenue(venue);
		
		EventDisplayAttribute displayAttributes = new EventDisplayAttribute();
		displayAttributes.setIntegratedEventInd(false);
		event.setDisplayAttributes(displayAttributes);
		
		List<CommonAttribute> dynamicAttributes = new ArrayList<CommonAttribute>();
		dynamicAttributes.add(buildCommonAttribute("show_performer", "false", "stubhub_display_attrib", "active", "en_US", "String"));
		dynamicAttributes.add(buildCommonAttribute("game_type", "Parking", "stubhub_catalog_attrib", "active", "en_US", "String"));
		dynamicAttributes.add(buildCommonAttribute("hide_event_time", "true", "stubhub_display_attrib", "active", "en_US", "Boolean"));
		dynamicAttributes.add(buildCommonAttribute("event_type", "Parking", "Other", "active", "en_US", "String"));
		
		event.setDynamicAttributes(dynamicAttributes);
		
		EventAttribute eventAttribute = new EventAttribute();
	    eventAttribute.setEventType("Parking");
	    event.setEventAttributes(eventAttribute);
		
		List<SeatTrait> seatTraits = new ArrayList<SeatTrait>();
		event.setSeatTraits(seatTraits);
		SeatTrait st1 = new SeatTrait();
		st1.setId(101L);
		st1.setName("Aisle");
		seatTraits.add(st1);
		SeatTrait st2 = new SeatTrait();
		st2.setId(103L);
		st2.setName("Club Pass/Access");
		seatTraits.add(st2);
		return event;
	}
	
	
	private com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event getEventV3ResponseObjectWithEnableHybridMap() {
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event event = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();
		event.setId(9314333L);
		event.setName("Test Event");
		event.setStatus("ACTIVE");
		event.setExpiredInd(false);
		event.setBobId("1");
		event.setCurrencyCode("USD");
		event.setEventDateLocal("2017-02-20T22:56:05-08:00");
		event.setEventDateUTC("2017-02-21T06:56:05Z");
		event.setTimezone("PST");
		event.setDateLastChance("2017-02-20T22:56:05-08:00");
		
		Ancestors ancestors = new Ancestors();
		List<AncestorItem> categoryIds = new ArrayList<AncestorItem>();
		List<AncestorItem> groupingIds = new ArrayList<AncestorItem>();
		List<AncestorItem> performerIds = new ArrayList<AncestorItem>();
		List<AncestorItem> geographies = new ArrayList<AncestorItem>();
		
		AncestorItem item1 = new AncestorItem();
		item1.setId(1L);
		item1.setName("Category");
		AncestorItem item2 = new AncestorItem();
		item2.setId(2L);
		item2.setName("Grouping");
		AncestorItem item3 = new AncestorItem();
		item3.setId(3L);
		item3.setName("Performer");
		AncestorItem item4 = new AncestorItem();
		item4.setId(4L);
		item4.setName("Geography");
		
		categoryIds.add(item1);
		groupingIds.add(item2);
		performerIds.add(item3);
		geographies.add(item4);
		
		ancestors.setCategories(categoryIds);
		ancestors.setGroupings(groupingIds);
		ancestors.setPerformers(performerIds);
		ancestors.setGeographies(geographies);
		
		event.setAncestors(ancestors);
		
		Venue venue = new Venue();
		venue.setId(12L);
		venue.setName("Park");
		venue.setConfigurationId("1232");
		venue.setCountry("US");
		event.setVenue(venue);
		
		EventDisplayAttribute displayAttributes = new EventDisplayAttribute();
		displayAttributes.setIntegratedEventInd(false);
		event.setDisplayAttributes(displayAttributes);
		
		List<CommonAttribute> dynamicAttributes = new ArrayList<CommonAttribute>();
		dynamicAttributes.add(buildCommonAttribute("enable_hybrid_map", "true", "stubhub_display_attrib", "active", "en_US", "String"));
		dynamicAttributes.add(buildCommonAttribute("game_type", "Parking", "stubhub_catalog_attrib", "active", "en_US", "String"));
		dynamicAttributes.add(buildCommonAttribute("hide_event_time", "true", "stubhub_display_attrib", "active", "en_US", "Boolean"));
		dynamicAttributes.add(buildCommonAttribute("event_type", "Parking", "Other", "active", "en_US", "String"));
		
		event.setDynamicAttributes(dynamicAttributes);
		
		EventAttribute eventAttribute = new EventAttribute();
	    eventAttribute.setEventType("Parking");
	    event.setEventAttributes(eventAttribute);
		
		List<SeatTrait> seatTraits = new ArrayList<SeatTrait>();
		event.setSeatTraits(seatTraits);
		SeatTrait st1 = new SeatTrait();
		st1.setId(101L);
		st1.setName("Aisle");
		seatTraits.add(st1);
		SeatTrait st2 = new SeatTrait();
		st2.setId(103L);
		st2.setName("Club Pass/Access");
		seatTraits.add(st2);
		return event;
	}
	
	private VenueConfigurations getVenueConfigs() {
		VenueConfigurations venueConfigs = new VenueConfigurations();
		List<VenueConfiguration> venueConfigList = new ArrayList<VenueConfiguration>();
		Map map = new Map();
		VenueConfiguration venueConfig = new VenueConfiguration();
		venueConfig.setId(1232L);
		venueConfig.setGeneralAdmissionOnly(false);
		map.setSectionScrubbing(true);
		map.setRowScrubbing(false);
		venueConfig.setMap(map);

		venueConfigList.add(venueConfig);
		venueConfigs.setVenueConfiguration(venueConfigList);
		return venueConfigs;
	}
	
	private Response getEventV3Response() {
		Response response =  new Response() {
			
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
				String response = "{\"id\": 9314333,\"name\": \"Nightwish Tickets\",\"status\": \"active\",\"expiredInd\": false,\"eventDateUTC\": \"2016-02-21T00:00:00Z\"}";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		return response;
	}
	
	private Response getExpiredEventV3Response() {
      Response response =  new Response() {
          
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
              String response = "{\"id\": 9314333,\"name\": \"Nightwish Tickets\",\"status\": \"active\",\"expiredInd\": true,\"eventDateUTC\": \"2016-02-21T00:00:00Z\"}";
              return new ByteArrayInputStream(response.getBytes());
          }
      };
      return response;
  }
	
	private Response getEventV3ResponseNotFound() {
		Response response =  new Response() {
			
			@Override
			public int getStatus() {
				return 404;
			}
			
			@Override
			public MultivaluedMap<String, Object> getMetadata() {				
				return null;
			}
			
			@Override
			public Object getEntity() {
				String response = "";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		return response;
	}
	
	private Response getEventV3ResponseBadRequest() {
		Response response =  new Response() {
			
			@Override
			public int getStatus() {
				return 400;
			}
			
			@Override
			public MultivaluedMap<String, Object> getMetadata() {				
				return null;
			}
			
			@Override
			public Object getEntity() {
				String response = "";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		return response;
	}
	
	
	private Response getEventV3ResponseConflictRequest() {
      Response response =  new Response() {
          
          @Override
          public int getStatus() {
              return 409;
          }
          
          @Override
          public MultivaluedMap<String, Object> getMetadata() {               
              return null;
          }
          
          @Override
          public Object getEntity() {
              String response = "";
              return new ByteArrayInputStream(response.getBytes());
          }
      };
      return response;
  }
	
	private Response getEventV3ResponseSystemError() {
		Response response =  new Response() {
			
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
				String response = "";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		return response;
	}
		
	private Response getVenueConfigResponse() {
		Response response =  new Response() {
			
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
				String response = "{\"id\": 1232,\"description\": \"Park\",\"active\": true,\"generalAdmissionOnly\": false,\"venueId\": 12}";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		return response;
	}
	
	private Response getVenueConfigResponseNon200() {
		Response response =  new Response() {
			
			@Override
			public int getStatus() {
				return 404;
			}
			
			@Override
			public MultivaluedMap<String, Object> getMetadata() {				
				return null;
			}
			
			@Override
			public Object getEntity() {
				String response = "";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		return response;
	}
	
	
	private Response getVenueSeatingSectionsResponse() {
		Response response =  new Response() {
			
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
				String response = "{\n    \"id\": 1847922,\n    \"name\": \"VIP 2\",\n    \"primary\": true,\n    \"locale\": \"fr_FR\",\n    \"generalAdmission\": false\n}";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		return response;
	}
	
	private Response getVenueSeatingZoneSResponse() {
		Response response =  new Response() {
			
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
				String response = "{\"id\":1847922,\"name\":\"Lower East Tier\"}";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		return response;
	}
	
	private CommonAttribute buildCommonAttribute(String name, String value, String type, String status, String locale, String dataType) {
		
		CommonAttribute ca = new CommonAttribute();
		ca.setName(name);
		ca.setValue(value);
		ca.setType(type);
		ca.setStatus(status);
		ca.setLocale(locale);
		ca.setDataType(dataType);
		
		return ca;
	}
	
}
