package com.stubhub.domain.inventory.listings.v2.helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.catalog.datamodel.entity.VenueConfigSection;
import com.stubhub.domain.catalog.events.biz.intf.VenueConfigSectionsBO;
import com.stubhub.domain.catalog.events.intf.TicketTrait;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.EventMobileAttribute;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Venue;
import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.Map;
import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfiguration;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.SupplementSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.entity.DeliveryMethod;
import com.stubhub.domain.inventory.listings.v2.entity.ExpectedDeliveryDate;
import com.stubhub.domain.inventory.listings.v2.entity.ListingCheck;
import com.stubhub.domain.inventory.listings.v2.entity.UserContact;
import com.stubhub.domain.inventory.listings.v2.util.EventHelper;
import com.stubhub.domain.inventory.listings.v2.util.FulfillmentServiceHelper;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.v2.enums.ProxyRoleTypeEnum;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;

import junit.framework.Assert;

public class ListingHelperTest {

	private ListingHelper listingHelper;
	private InventoryMgr inventoryMgr;
	private EventHelper eventHelper;
	private TicketSeatMgr ticketSeatMgr;
	private MasterStubhubPropertiesWrapper masterStubhubProperties;
	private UserHelper userHelper;
	private FulfillmentServiceHelper fulfillmentServiceHelper;
	private ListingSeatTraitMgr listingSeatTraitMgr;
	private VenueConfigSectionsBO venueConfigSectionsBO;
	private SHServiceContext shServiceContext;

	
	private VenueConfiguration venueConfig; //FAN-237 06/27/16
	
	Long listingId = 123456L;
	Long venueConfigSectionId = 2222211L;
	Long eventId = 11111L;
	com.stubhub.domain.inventory.v2.enums.ProxyRoleTypeEnum proxyRoleType = null;
	
	Locale locale = Locale.US;

	private ListingCheck listingCheck = new ListingCheck();
	
	@BeforeMethod
	public void setUp () throws Exception{

		SHAPIContext shAPIContext;
		shAPIContext = Mockito.mock(SHAPIContext.class);
		shAPIContext.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(shAPIContext);

		listingHelper = new ListingHelper();
		Listing listing = new Listing();
		listing.setSection("section");
		listing.setSeats("1,2,3");
		listing.setEvent(new Event());
		listing.setEventId(123245L);
		listing.setVenueConfigSectionsId(123L);
		listing.setRow("row");

		venueConfigSectionsBO = Mockito.mock(VenueConfigSectionsBO.class);

		inventoryMgr = Mockito.mock(InventoryMgr.class);
		eventHelper= Mockito.mock(EventHelper.class);
		userHelper = Mockito.mock(UserHelper.class);
		listingSeatTraitMgr = Mockito.mock(ListingSeatTraitMgr.class);
		fulfillmentServiceHelper = Mockito.mock(FulfillmentServiceHelper.class);
		ticketSeatMgr=Mockito.mock(TicketSeatMgr.class);
		masterStubhubProperties = Mockito.mock(MasterStubhubPropertiesWrapper.class);
		venueConfigSectionsBO= Mockito.mock(VenueConfigSectionsBO.class);
		shServiceContext = Mockito.mock(SHServiceContext.class);
		ReflectionTestUtils.setField(listingHelper, "inventoryMgr", inventoryMgr);
		ReflectionTestUtils.setField(listingHelper, "eventHelper", eventHelper);
		ReflectionTestUtils.setField(listingHelper, "fulfillmentServiceHelper", fulfillmentServiceHelper);
		ReflectionTestUtils.setField(listingHelper, "ticketSeatMgr", ticketSeatMgr);
		ReflectionTestUtils.setField(listingHelper, "masterStubhubProperties", masterStubhubProperties);
		ReflectionTestUtils.setField(listingHelper, "userHelper", userHelper);
		ReflectionTestUtils.setField(listingHelper, "listingSeatTraitMgr", listingSeatTraitMgr);
		ReflectionTestUtils.setField(listingHelper, "venueConfigSectionsBO", venueConfigSectionsBO);
		
		UserContact userContact = new UserContact();
		userContact.setId(1873213L);
		Mockito.when(userHelper.getDefaultUserContact(Mockito.anyLong())).thenReturn(userContact);
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("UK,GB,DE");
		List<DeliveryMethod> deliveryMethodList = new ArrayList<DeliveryMethod>();
		DeliveryMethod deliveryMethod = new DeliveryMethod();
		deliveryMethod.setId(123213L);
		deliveryMethod.setName("UPS");
		ExpectedDeliveryDate expectedDeliveryDate = new ExpectedDeliveryDate();
		expectedDeliveryDate.setExpectedDate(Calendar.getInstance());
		deliveryMethod.setExpectedDeliveryDate(expectedDeliveryDate);
		Mockito.when(fulfillmentServiceHelper.getDeliveryMethodsForListingId(Mockito.anyLong(),Mockito.anyLong(), Mockito.any(Calendar.class),  Mockito.anyBoolean(),  Mockito.any(Event.class))).thenReturn(deliveryMethodList);		
	}
	
	@Test
	public void testSectionMappingRequired () throws Exception{
		
		Listing listing=getDBListing();
		listing.setVenueConfigSectionsId(null);
		listing.setSectionScrubSchedule(false);
		listing.setDeliveryOption(2);
		Event event = new Event();
		event.setId(165161L);
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, 10);
		event.setEventDate(cal);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		
		Map map = new Map();
		map.setRowScrubbing(Boolean.TRUE);
		map.setSectionScrubbing(Boolean.TRUE);
	
		venueConfig = new VenueConfiguration();
		venueConfig.setMap(map);
		
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		Mockito.when(eventHelper.getVenueDetails(Mockito.anyLong())).thenReturn(venueConfig);
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();

		eventV3.setBobId("1");
		EventMobileAttribute mobileAttribute = new EventMobileAttribute();
		mobileAttribute.setStubhubMobileTicket(true);
		eventV3.setMobileAttributes(mobileAttribute);
		Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), Mockito.any(Locale.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))).thenReturn(eventV3);
		
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);
		
		List<SupplementSeatTrait> seatTraits = new ArrayList<SupplementSeatTrait>();
		SupplementSeatTrait seatTrait = new SupplementSeatTrait();
		seatTrait.setSupplementSeatTraitId(1011L);
		seatTraits.add(seatTrait);
	
		Mockito.when(listingSeatTraitMgr.getSupplementSeatTraitsForListing(Mockito.anyLong())).thenReturn(seatTraits);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987234L, shServiceContext, null);
		
		map.setRowScrubbing(Boolean.FALSE);
		map.setSectionScrubbing(Boolean.TRUE);
		venueConfig.setMap(map);
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987234L, shServiceContext, null);

		map.setRowScrubbing(Boolean.TRUE);
		map.setSectionScrubbing(Boolean.FALSE);
		venueConfig.setMap(map);
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987234L, shServiceContext,null);

		map.setRowScrubbing(Boolean.FALSE);
		map.setSectionScrubbing(Boolean.FALSE);
		venueConfig.setMap(map);
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987234L, shServiceContext,null);

		map.setSectionScrubbing(null);
		venueConfig.setMap(map);
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987234L, shServiceContext,null);

		venueConfig.setMap(null);
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987234L, shServiceContext,null);

		venueConfig = null;
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987234L, shServiceContext,null);
		
		listing.setVenueConfigSectionsId(null);
		listing.setSectionScrubSchedule(true);
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987234L, shServiceContext,null);

		listing.setVenueConfigSectionsId(12653213L);
		listing.setSectionScrubSchedule(false);
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987234L, shServiceContext,null);

	}
	
	@Test(expectedExceptions=ListingBusinessException.class)
	public void testPopulateListingException01() throws Exception{
		Listing listing=getDBListing();
		Event event = new Event();
		event.setId(165161L);
		event.setCountry("UK");
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, 10);
		event.setEventDate(cal);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		
		List<com.stubhub.domain.catalog.events.intf.TicketTrait> ttList = new ArrayList<com.stubhub.domain.catalog.events.intf.TicketTrait>();
		com.stubhub.domain.catalog.events.intf.TicketTrait tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		event.setTicketTrait(ttList);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();

		eventV3.setBobId("1");
		EventMobileAttribute mobileAttribute = new EventMobileAttribute();
		mobileAttribute.setStubhubMobileTicket(true);
		eventV3.setMobileAttributes(mobileAttribute);
		Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), Mockito.any(Locale.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))).thenThrow(Exception.class);
		
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);
		
		List<SupplementSeatTrait> seatTraits = new ArrayList<SupplementSeatTrait>();
		SupplementSeatTrait seatTrait = new SupplementSeatTrait();
		seatTrait.setSupplementSeatTraitId(1011L);
		seatTraits.add(seatTrait);
	
		Mockito.when(listingSeatTraitMgr.getSupplementSeatTraitsForListing(Mockito.anyLong())).thenReturn(seatTraits);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987234L, shServiceContext,null);
		
	}

	@Test(expectedExceptions=ListingBusinessException.class)
	public void testPopulateListingException02() throws Exception{
		Listing listing=getDBListing();
		Event event = new Event();
		event.setId(165161L);
		event.setCountry("UK");
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, 10);
		event.setEventDate(cal);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		
		List<com.stubhub.domain.catalog.events.intf.TicketTrait> ttList = new ArrayList<com.stubhub.domain.catalog.events.intf.TicketTrait>();
		com.stubhub.domain.catalog.events.intf.TicketTrait tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		event.setTicketTrait(ttList);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();

		eventV3.setBobId("1");
		EventMobileAttribute mobileAttribute = new EventMobileAttribute();
		mobileAttribute.setStubhubMobileTicket(true);
		eventV3.setMobileAttributes(mobileAttribute);
		Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), Mockito.any(Locale.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))).thenReturn(eventV3);
		
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);
		
		List<SupplementSeatTrait> seatTraits = new ArrayList<SupplementSeatTrait>();
		SupplementSeatTrait seatTrait = new SupplementSeatTrait();
		seatTrait.setSupplementSeatTraitId(1011L);
		seatTraits.add(seatTrait);
	
		Mockito.when(listingSeatTraitMgr.getSupplementSeatTraitsForListing(Mockito.anyLong())).thenReturn(seatTraits);		
		Mockito.when(listingHelper.getScrubbedSectionName(listing)).thenThrow(Exception.class);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987234L, shServiceContext,null);
		
	}

	@Test
	public void testPopulateListingDetails() throws Exception{
		Listing listing=getDBListing();
		Event event = new Event();
		event.setId(165161L);
		event.setCountry("UK");
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, 10);
		event.setEventDate(cal);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));

		List<com.stubhub.domain.catalog.events.intf.TicketTrait> ttList = new ArrayList<com.stubhub.domain.catalog.events.intf.TicketTrait>();
		com.stubhub.domain.catalog.events.intf.TicketTrait tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Side stage (printed on ticket)");
		tt.setId(1011L);
		ttList.add(tt);
		tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Partial Suite (shared) - Reserved seating");
		tt.setId(1021L);
		ttList.add(tt);
		event.setTicketTrait(ttList);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();

		eventV3.setBobId("1");
		EventMobileAttribute mobileAttribute = new EventMobileAttribute();
		mobileAttribute.setStubhubMobileTicket(true);
		eventV3.setMobileAttributes(mobileAttribute);
		Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), Mockito.any(Locale.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))).thenReturn(eventV3);

		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setTixListTypeId(2L);
		ticketSeat.setRow("row");
		ticketSeat.setExternalSeatId("externalseatId");
		ticketSeats.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);

		List<SupplementSeatTrait> seatTraits = new ArrayList<SupplementSeatTrait>();
		SupplementSeatTrait seatTrait = new SupplementSeatTrait();
		seatTrait.setSupplementSeatTraitId(1011L);
		seatTraits.add(seatTrait);

		Mockito.when(listingSeatTraitMgr.getSupplementSeatTraitsForListing(Mockito.anyLong())).thenReturn(seatTraits);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		Mockito.when(eventHelper.getLocalizedSeatingSectionName(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn("test");

		ListingResponse response = listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987234L, shServiceContext,null);
		//hide seats
		listing.setSellerId(9827439871L);
		listing.setHideSeatInfoInd(Boolean.TRUE);
		Mockito.when(shServiceContext.getOperatorId()).thenReturn(null);
		response = listingHelper.populateListingDetails(null, 12653213L, Locale.US, 9827439872L, shServiceContext,"deliveryMethods");
	}

	@Test
	public void testPopulateListingDetailsWithEmptyFmdmList() throws Exception{
		Listing listing=getDBListing();
		Event event = new Event();
		event.setId(165161L);
		event.setCountry("UK");
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, 10);
		event.setEventDate(cal);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));

		List<com.stubhub.domain.catalog.events.intf.TicketTrait> ttList = new ArrayList<com.stubhub.domain.catalog.events.intf.TicketTrait>();
		com.stubhub.domain.catalog.events.intf.TicketTrait tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Side stage (printed on ticket)");
		tt.setId(1011L);
		ttList.add(tt);
		tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Partial Suite (shared) - Reserved seating");
		tt.setId(1021L);
		ttList.add(tt);
		event.setTicketTrait(ttList);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();

		eventV3.setBobId("1");
		EventMobileAttribute mobileAttribute = new EventMobileAttribute();
		mobileAttribute.setStubhubMobileTicket(true);
		eventV3.setMobileAttributes(mobileAttribute);
		Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), Mockito.any(Locale.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))).thenReturn(eventV3);

		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setTixListTypeId(2L);
		ticketSeat.setRow("row");
		ticketSeat.setExternalSeatId("externalseatId");
		ticketSeats.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);

		List<SupplementSeatTrait> seatTraits = new ArrayList<SupplementSeatTrait>();
		SupplementSeatTrait seatTrait = new SupplementSeatTrait();
		seatTrait.setSupplementSeatTraitId(1011L);
		seatTraits.add(seatTrait);

		Mockito.when(listingSeatTraitMgr.getSupplementSeatTraitsForListing(Mockito.anyLong())).thenReturn(seatTraits);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		Mockito.when(eventHelper.getLocalizedSeatingSectionName(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn("test");

		ListingResponse response = listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987235L, shServiceContext,null);
		//hide seats
		listing.setSellerId(9827439871L);
		listing.setHideSeatInfoInd(Boolean.TRUE);
		Mockito.when(shServiceContext.getOperatorId()).thenReturn(null);
		response = listingHelper.populateListingDetails(null, 12653213L, Locale.US, 9827439872L, shServiceContext,"deliveryMethods");
	}

	@Test
	public void testPopulateListingDetailsWithFmdmList() throws Exception{
		Listing listing=getDBListing();
		listing.setFulfillmentDeliveryMethods("2,48,2.5,,2020-03-03T00:00:00Z");
		Event event = new Event();
		event.setId(165161L);
		event.setCountry("UK");
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, 10);
		event.setEventDate(cal);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));



		List<com.stubhub.domain.catalog.events.intf.TicketTrait> ttList = new ArrayList<com.stubhub.domain.catalog.events.intf.TicketTrait>();
		com.stubhub.domain.catalog.events.intf.TicketTrait tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Side stage (printed on ticket)");
		tt.setId(1011L);
		ttList.add(tt);
		tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Partial Suite (shared) - Reserved seating");
		tt.setId(1021L);
		ttList.add(tt);
		event.setTicketTrait(ttList);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();

		eventV3.setBobId("1");
		EventMobileAttribute mobileAttribute = new EventMobileAttribute();
		mobileAttribute.setStubhubMobileTicket(true);
		eventV3.setMobileAttributes(mobileAttribute);
		Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), Mockito.any(Locale.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))).thenReturn(eventV3);

		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setTixListTypeId(2L);
		ticketSeat.setRow("row");
		ticketSeat.setExternalSeatId("externalseatId");
		ticketSeats.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);

		List<SupplementSeatTrait> seatTraits = new ArrayList<SupplementSeatTrait>();
		SupplementSeatTrait seatTrait = new SupplementSeatTrait();
		seatTrait.setSupplementSeatTraitId(1011L);
		seatTraits.add(seatTrait);

		Mockito.when(listingSeatTraitMgr.getSupplementSeatTraitsForListing(Mockito.anyLong())).thenReturn(seatTraits);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		Mockito.when(eventHelper.getLocalizedSeatingSectionName(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn("test");

		ListingResponse response = listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987235L, shServiceContext,null);
		//hide seats
		listing.setSellerId(9827439871L);
		listing.setHideSeatInfoInd(Boolean.TRUE);
		Mockito.when(shServiceContext.getOperatorId()).thenReturn(null);
		response = listingHelper.populateListingDetails(null, 12653213L, Locale.US, 9827439872L, shServiceContext,"deliveryMethods");
	}

	@Test
	public void testPopulateListingDetailsWithEmptyDeliveryMethod() throws Exception{
		Listing listing=getDBListing();
		listing.setFulfillmentDeliveryMethods("2,,2.5,,2020-03-03T00:00:00Z");
		Event event = new Event();
		event.setId(165161L);
		event.setCountry("UK");
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, 10);
		event.setEventDate(cal);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));

		List<com.stubhub.domain.catalog.events.intf.TicketTrait> ttList = new ArrayList<com.stubhub.domain.catalog.events.intf.TicketTrait>();
		com.stubhub.domain.catalog.events.intf.TicketTrait tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Side stage (printed on ticket)");
		tt.setId(1011L);
		ttList.add(tt);
		tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Partial Suite (shared) - Reserved seating");
		tt.setId(1021L);
		ttList.add(tt);
		event.setTicketTrait(ttList);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();

		eventV3.setBobId("1");
		EventMobileAttribute mobileAttribute = new EventMobileAttribute();
		mobileAttribute.setStubhubMobileTicket(true);
		eventV3.setMobileAttributes(mobileAttribute);
		Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), Mockito.any(Locale.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))).thenReturn(eventV3);

		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setTixListTypeId(2L);
		ticketSeat.setRow("row");
		ticketSeat.setExternalSeatId("externalseatId");
		ticketSeats.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);

		List<SupplementSeatTrait> seatTraits = new ArrayList<SupplementSeatTrait>();
		SupplementSeatTrait seatTrait = new SupplementSeatTrait();
		seatTrait.setSupplementSeatTraitId(1011L);
		seatTraits.add(seatTrait);

		Mockito.when(listingSeatTraitMgr.getSupplementSeatTraitsForListing(Mockito.anyLong())).thenReturn(seatTraits);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		Mockito.when(eventHelper.getLocalizedSeatingSectionName(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn("test");

		ListingResponse response = listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987235L, shServiceContext,null);
		//hide seats
		listing.setSellerId(9827439871L);
		listing.setHideSeatInfoInd(Boolean.TRUE);
		Mockito.when(shServiceContext.getOperatorId()).thenReturn(null);
		response = listingHelper.populateListingDetails(null, 12653213L, Locale.US, 9827439872L, shServiceContext,"deliveryMethods");
	}

	@Test
	public void testPopulateListingDetailsWithEmptyDeliveryDate() throws Exception{
		Listing listing=getDBListing();
		listing.setFulfillmentDeliveryMethods("2,60,2.5,, ");
		Event event = new Event();
		event.setId(165161L);
		event.setCountry("UK");
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, 10);
		event.setEventDate(cal);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));



		List<com.stubhub.domain.catalog.events.intf.TicketTrait> ttList = new ArrayList<com.stubhub.domain.catalog.events.intf.TicketTrait>();
		com.stubhub.domain.catalog.events.intf.TicketTrait tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Side stage (printed on ticket)");
		tt.setId(1011L);
		ttList.add(tt);
		tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Partial Suite (shared) - Reserved seating");
		tt.setId(1021L);
		ttList.add(tt);
		event.setTicketTrait(ttList);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();

		eventV3.setBobId("1");
		EventMobileAttribute mobileAttribute = new EventMobileAttribute();
		mobileAttribute.setStubhubMobileTicket(true);
		eventV3.setMobileAttributes(mobileAttribute);
		Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), Mockito.any(Locale.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))).thenReturn(eventV3);

		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setTixListTypeId(2L);
		ticketSeat.setRow("row");
		ticketSeat.setExternalSeatId("externalseatId");
		ticketSeats.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);

		List<SupplementSeatTrait> seatTraits = new ArrayList<SupplementSeatTrait>();
		SupplementSeatTrait seatTrait = new SupplementSeatTrait();
		seatTrait.setSupplementSeatTraitId(1011L);
		seatTraits.add(seatTrait);

		Mockito.when(listingSeatTraitMgr.getSupplementSeatTraitsForListing(Mockito.anyLong())).thenReturn(seatTraits);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		Mockito.when(eventHelper.getLocalizedSeatingSectionName(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn("test");

		ListingResponse response = listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987235L, shServiceContext,null);
		//hide seats
		listing.setSellerId(9827439871L);
		listing.setHideSeatInfoInd(Boolean.TRUE);
		Mockito.when(shServiceContext.getOperatorId()).thenReturn(null);
		response = listingHelper.populateListingDetails(null, 12653213L, Locale.US, 9827439872L, shServiceContext,"deliveryMethods");
	}

	@Test
	public void testPopulateListingDetailsWithOrdinal75() throws Exception{
		Listing listing=getDBListing();
		listing.setFulfillmentDeliveryMethods("2,75,2.5,,2020-03-03T00:00:00Z");
		Event event = new Event();
		event.setId(165161L);
		event.setCountry("UK");
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, 10);
		event.setEventDate(cal);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));



		List<com.stubhub.domain.catalog.events.intf.TicketTrait> ttList = new ArrayList<com.stubhub.domain.catalog.events.intf.TicketTrait>();
		com.stubhub.domain.catalog.events.intf.TicketTrait tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Side stage (printed on ticket)");
		tt.setId(1011L);
		ttList.add(tt);
		tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Partial Suite (shared) - Reserved seating");
		tt.setId(1021L);
		ttList.add(tt);
		event.setTicketTrait(ttList);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();

		eventV3.setBobId("1");
		EventMobileAttribute mobileAttribute = new EventMobileAttribute();
		mobileAttribute.setStubhubMobileTicket(true);
		eventV3.setMobileAttributes(mobileAttribute);
		Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), Mockito.any(Locale.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))).thenReturn(eventV3);

		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setTixListTypeId(2L);
		ticketSeat.setRow("row");
		ticketSeat.setExternalSeatId("externalseatId");
		ticketSeats.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);

		List<SupplementSeatTrait> seatTraits = new ArrayList<SupplementSeatTrait>();
		SupplementSeatTrait seatTrait = new SupplementSeatTrait();
		seatTrait.setSupplementSeatTraitId(1011L);
		seatTraits.add(seatTrait);

		Mockito.when(listingSeatTraitMgr.getSupplementSeatTraitsForListing(Mockito.anyLong())).thenReturn(seatTraits);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		Mockito.when(eventHelper.getLocalizedSeatingSectionName(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn("test");

		ListingResponse response = listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987235L, shServiceContext,null);
		//hide seats
		listing.setSellerId(9827439871L);
		listing.setHideSeatInfoInd(Boolean.TRUE);
		Mockito.when(shServiceContext.getOperatorId()).thenReturn(null);
		response = listingHelper.populateListingDetails(null, 12653213L, Locale.US, 9827439872L, shServiceContext,"deliveryMethods");
	}

	//SELLAPI-1041-1
	@Test
	public void testPopulateListingDetailsNullUserId() throws Exception{
		
		Listing listing=getDBListing();
		Event event = new Event();
		event.setId(165161L);
		event.setCountry("UK");
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, 10);
		event.setEventDate(cal);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		
		List<com.stubhub.domain.catalog.events.intf.TicketTrait> ttList = new ArrayList<com.stubhub.domain.catalog.events.intf.TicketTrait>();
		com.stubhub.domain.catalog.events.intf.TicketTrait tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Side stage (printed on ticket)");
		tt.setId(1011L);
		ttList.add(tt);
		tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Partial Suite (shared) - Reserved seating");
		tt.setId(1021L);
		ttList.add(tt);
		event.setTicketTrait(ttList);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();

		eventV3.setBobId("1");
		EventMobileAttribute mobileAttribute = new EventMobileAttribute();
		mobileAttribute.setStubhubMobileTicket(true);
		eventV3.setMobileAttributes(mobileAttribute);
		Venue venue = new Venue();
		venue.setCountry("GB");
		eventV3.setVenue(venue);
		Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), Mockito.any(Locale.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))).thenReturn(eventV3);
		
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setTixListTypeId(2L);
		ticketSeat.setRow("row");
		ticketSeat.setExternalSeatId("externalseatId");
		ticketSeats.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);
		
		List<SupplementSeatTrait> seatTraits = new ArrayList<SupplementSeatTrait>();
		SupplementSeatTrait seatTrait = new SupplementSeatTrait();
		seatTrait.setSupplementSeatTraitId(1011L);
		seatTraits.add(seatTrait);
	
		Mockito.when(listingSeatTraitMgr.getSupplementSeatTraitsForListing(Mockito.anyLong())).thenReturn(seatTraits);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		
		//listing.setScrubbedSectionName("test");
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, null, shServiceContext,"deliveryMethods");
		
	}
	
	//SELLAPI-1041-2
	@Test
	public void testPopulateListingDetailsNullUserId_NullV3() throws Exception{

		Listing listing=getDBListing();
		Event event = new Event();
		event.setId(165161L);
		event.setCountry("UK");
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, 10);
		event.setEventDate(cal);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		
		List<com.stubhub.domain.catalog.events.intf.TicketTrait> ttList = new ArrayList<com.stubhub.domain.catalog.events.intf.TicketTrait>();
		com.stubhub.domain.catalog.events.intf.TicketTrait tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Side stage (printed on ticket)");
		tt.setId(1011L);
		ttList.add(tt);
		tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Partial Suite (shared) - Reserved seating");
		tt.setId(1021L);
		ttList.add(tt);
		event.setTicketTrait(ttList);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), Mockito.any(Locale.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))).thenReturn(null);
		
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setTixListTypeId(2L);
		ticketSeat.setRow("row");
		ticketSeat.setExternalSeatId("externalseatId");
		ticketSeats.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);
		
		List<SupplementSeatTrait> seatTraits = new ArrayList<SupplementSeatTrait>();
		SupplementSeatTrait seatTrait = new SupplementSeatTrait();
		seatTrait.setSupplementSeatTraitId(1011L);
		seatTraits.add(seatTrait);
	
		Mockito.when(listingSeatTraitMgr.getSupplementSeatTraitsForListing(Mockito.anyLong())).thenReturn(seatTraits);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		
		//listing.setScrubbedSectionName("test");
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, null, shServiceContext,"deliveryMethods");
		
	}
	
	//SELLAPI-1041-3
	@Test
	public void testPopulateListingDetails_NullVenue() throws Exception{

		Listing listing=getDBListing();
		Event event = new Event();
		event.setId(165161L);
		event.setCountry("UK");
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, 10);
		event.setEventDate(cal);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		
		List<com.stubhub.domain.catalog.events.intf.TicketTrait> ttList = new ArrayList<com.stubhub.domain.catalog.events.intf.TicketTrait>();
		com.stubhub.domain.catalog.events.intf.TicketTrait tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Side stage (printed on ticket)");
		tt.setId(1011L);
		ttList.add(tt);
		tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Partial Suite (shared) - Reserved seating");
		tt.setId(1021L);
		ttList.add(tt);
		event.setTicketTrait(ttList);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();

		eventV3.setBobId("1");
		EventMobileAttribute mobileAttribute = new EventMobileAttribute();
		mobileAttribute.setStubhubMobileTicket(true);
		eventV3.setMobileAttributes(mobileAttribute);
		eventV3.setVenue(null);
		Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), Mockito.any(Locale.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))).thenReturn(eventV3);
		
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setTixListTypeId(2L);
		ticketSeat.setRow("row");
		ticketSeat.setExternalSeatId("externalseatId");
		ticketSeats.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);
		
		List<SupplementSeatTrait> seatTraits = new ArrayList<SupplementSeatTrait>();
		SupplementSeatTrait seatTrait = new SupplementSeatTrait();
		seatTrait.setSupplementSeatTraitId(1011L);
		seatTraits.add(seatTrait);
	
		Mockito.when(listingSeatTraitMgr.getSupplementSeatTraitsForListing(Mockito.anyLong())).thenReturn(seatTraits);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		
		//listing.setScrubbedSectionName("test");
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, null, shServiceContext,"deliveryMethods");
		
	}
	
	//SELLAPI-1041-4
	@Test
	public void testPopulateListingDetails_NullCntry() throws Exception{
		Listing listing=getDBListing();
		Event event = new Event();
		event.setId(165161L);
		event.setCountry("UK");
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, 10);
		event.setEventDate(cal);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		
		List<com.stubhub.domain.catalog.events.intf.TicketTrait> ttList = new ArrayList<com.stubhub.domain.catalog.events.intf.TicketTrait>();
		com.stubhub.domain.catalog.events.intf.TicketTrait tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Side stage (printed on ticket)");
		tt.setId(1011L);
		ttList.add(tt);
		tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Partial Suite (shared) - Reserved seating");
		tt.setId(1021L);
		ttList.add(tt);
		event.setTicketTrait(ttList);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();

		eventV3.setBobId("1");
		EventMobileAttribute mobileAttribute = new EventMobileAttribute();
		mobileAttribute.setStubhubMobileTicket(true);
		eventV3.setMobileAttributes(mobileAttribute);
		eventV3.setVenue(new Venue());
		Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), Mockito.any(Locale.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))).thenReturn(eventV3);
		
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setTixListTypeId(2L);
		ticketSeat.setRow("row");
		ticketSeat.setExternalSeatId("externalseatId");
		ticketSeats.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);
		
		List<SupplementSeatTrait> seatTraits = new ArrayList<SupplementSeatTrait>();
		SupplementSeatTrait seatTrait = new SupplementSeatTrait();
		seatTrait.setSupplementSeatTraitId(1011L);
		seatTraits.add(seatTrait);
	
		Mockito.when(listingSeatTraitMgr.getSupplementSeatTraitsForListing(Mockito.anyLong())).thenReturn(seatTraits);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		
		//listing.setScrubbedSectionName("test");
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, null, shServiceContext,"deliveryMethods");
		
	}
		
	//SELLAPI-1041-5
	@Test
	public void testPopulateListingDetails_NotNullCntry() throws Exception{
		Listing listing=getDBListing();
		Event event = new Event();
		event.setId(165161L);
		event.setCountry("UK");
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, 10);
		event.setEventDate(cal);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		
		List<com.stubhub.domain.catalog.events.intf.TicketTrait> ttList = new ArrayList<com.stubhub.domain.catalog.events.intf.TicketTrait>();
		com.stubhub.domain.catalog.events.intf.TicketTrait tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Side stage (printed on ticket)");
		tt.setId(1011L);
		ttList.add(tt);
		tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
		tt.setName("Partial Suite (shared) - Reserved seating");
		tt.setId(1021L);
		ttList.add(tt);
		event.setTicketTrait(ttList);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();

		eventV3.setBobId("1");
		EventMobileAttribute mobileAttribute = new EventMobileAttribute();
		mobileAttribute.setStubhubMobileTicket(true);
		eventV3.setMobileAttributes(mobileAttribute);
		Venue venue = new Venue();
		venue.setCountry("GB");
		eventV3.setVenue(new Venue());
		Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), Mockito.any(Locale.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))).thenReturn(eventV3);
		
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setTixListTypeId(2L);
		ticketSeat.setRow("row");
		ticketSeat.setExternalSeatId("externalseatId");
		ticketSeats.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);
		
		List<SupplementSeatTrait> seatTraits = new ArrayList<SupplementSeatTrait>();
		SupplementSeatTrait seatTrait = new SupplementSeatTrait();
		seatTrait.setSupplementSeatTraitId(1011L);
		seatTraits.add(seatTrait);
	
		Mockito.when(listingSeatTraitMgr.getSupplementSeatTraitsForListing(Mockito.anyLong())).thenReturn(seatTraits);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		
		//listing.setScrubbedSectionName("test");
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, null, shServiceContext,"deliveryMethods");
		
	}
	
	@Test
	public void testScrubbedSectionName(){
		Listing listing=getDBListing();
		Mockito.when(venueConfigSectionsBO
				.getVenueConfigSectionAliasBySectionId(Mockito.anyLong())).thenReturn(Mockito.mock(VenueConfigSection.class));
		Mockito.when(Mockito.mock(VenueConfigSection.class).getVenueConfigSectionAlias()).thenReturn(Mockito.anyString());
		//Mockito.when(listingHelper.getScrubbedSectionName(listing)).thenReturn(Mockito.anyString());
		listingHelper.getScrubbedSectionName(listing);
		
	}
	

	@Test(expectedExceptions=ListingBusinessException.class)
	public void testPopulateListingDetails_InActiveListing() throws Exception{
		Listing listing=getDBListing();
		listing.setSystemStatus("INACTIVE");
		Event event = new Event();
		event.setId(165161L);
		event.setEventDate(Calendar.getInstance());
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setTixListTypeId(3L);
		ticketSeat.setRow("row");
		ticketSeat.setExternalSeatId("externalseatId");
		ticketSeats.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);
		Mockito.when(shServiceContext.getRole()).thenReturn(null);
		
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, 98274398724L, shServiceContext,null);
		
	}
	
	@Test(expectedExceptions=ListingBusinessException.class)
	public void testPopulateListingDetails_NullListing() throws Exception{
		
		Event event = new Event();
		event.setId(165161L);
		event.setEventDate(Calendar.getInstance());
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(null);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setTixListTypeId(3L);
		ticketSeat.setRow("row");
		ticketSeat.setExternalSeatId("externalseatId");
		ticketSeats.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987234L, shServiceContext,null);
		
	}
	
	@Test(expectedExceptions=ListingBusinessException.class)
	public void testPopulateListingDetails_listingExpired() throws Exception{
		Listing listing=getDBListing();
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, -10);
		listing.setEndDate(cal);
		Event event = new Event();
		event.setId(165161L);
		event.setEventDate(Calendar.getInstance());
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setTixListTypeId(3L);
		ticketSeat.setRow("row");
		ticketSeat.setExternalSeatId("externalseatId");
		ticketSeats.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987234L, shServiceContext,null);
		
	}
	
	@Test(expectedExceptions=ListingBusinessException.class)
	public void testPopulateListingDetails_listingQuantityZero() throws Exception{
		Listing listing=getDBListing();
		listing.setQuantityRemain(0);
		Event event = new Event();
		event.setId(165161L);
		event.setEventDate(Calendar.getInstance());
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setTixListTypeId(3L);
		ticketSeat.setRow("row");
		ticketSeat.setExternalSeatId("externalseatId");
		ticketSeats.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, 982743987234L, shServiceContext,null);
		
	}
	
	@Test
	public void testPopulateListingDetails_StubhubMobileTicket_Barcode() throws Exception{
		Listing listing=getDBListing();
		listing.setTicketMedium(TicketMedium.BARCODE.getValue());
		listing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event = new Event();
		event.setId(165161L);
		event.setCountry("GB");
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, 10);
		event.setEventDate(cal);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		
		List<TicketTrait> ticketTraits= new ArrayList<TicketTrait>();
		TicketTrait trait = new TicketTrait();
		trait.setId(1L);
		trait.setName("SOMEtRAIT");
		trait.setType("TICKET");
		ticketTraits.add(trait);
		event.setTicketTrait(ticketTraits);
		
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();

		eventV3.setBobId("1");
		EventMobileAttribute mobileAttribute = new EventMobileAttribute();
		mobileAttribute.setStubhubMobileTicket(true);
		eventV3.setMobileAttributes(mobileAttribute);
		Venue venue = new Venue();
		venue.setCountry("GB");
		eventV3.setVenue(venue);
		
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), Mockito.any(Locale.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))).thenReturn(eventV3);
		
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setTixListTypeId(2L);
		ticketSeat.setRow("row");
		ticketSeat.setExternalSeatId("externalseatId");
		ticketSeats.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		
		listingHelper.populateListingDetails(null, 12653213L, Locale.UK, 982743987234L, shServiceContext,null);
		
	}
	
	@Test
	public void testPopulateListingDetails_Buyer() throws Exception{
		Listing listing=getDBListing();
		Event event = new Event();
		event.setId(165161L);
		event.setCountry("UK");
		Calendar cal = Calendar.getInstance();
		cal.add(cal.DATE, 10);
		event.setEventDate(cal);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
		Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();

		eventV3.setBobId("1");
		EventMobileAttribute mobileAttribute = new EventMobileAttribute();
		mobileAttribute.setStubhubMobileTicket(true);
		eventV3.setMobileAttributes(mobileAttribute);
		Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), Mockito.any(Locale.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))).thenReturn(eventV3);

		
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setTixListTypeId(2L);
		ticketSeat.setRow("row");
		ticketSeat.setExternalSeatId("externalseatId");
		ticketSeats.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
		
		listingHelper.populateListingDetails(null, 12653213L, Locale.US, 98243987234L, shServiceContext,"deliveryMethods");
		
	}
	
	private Listing getDBListing () {
		Listing dbListing= new Listing ();
		dbListing.setComments("comments");
		dbListing.setTicketMedium(TicketMedium.BARCODE.getValue());
		dbListing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		dbListing.setConfirmOption(new Integer (1));
		dbListing.setCreatedDate(Calendar.getInstance());
		dbListing.setCurrency(Currency.getInstance("USD"));
		dbListing.setDeclaredInhandDate(Calendar.getInstance());
		dbListing.setDeferedActivationDate(Calendar.getInstance());
		dbListing.setDeliveryOption(new Integer (1));
		dbListing.setSellerId(982743987234L);
		dbListing.setExternalId("872348723");
		dbListing.setSellerCCId(987687687L);
		dbListing.setSellerContactId(872387487324L);
		dbListing.setSellerPaymentTypeId(4L);
		dbListing.setLmsApprovalStatus(1);
		dbListing.setScrubbedSectionName("test section");
		com.stubhub.newplatform.common.entity.Money ticketCost = new  com.stubhub.newplatform.common.entity.Money ();
		ticketCost.setAmount(new BigDecimal (10.0));
		dbListing.setFaceValue(ticketCost);
		dbListing.setTicketCost(ticketCost);
		
		Calendar endDate = Calendar.getInstance();
		endDate.roll(Calendar.MONTH, true);
		if (endDate.getTime().getMonth() == 0) {// an edge case that can happen in December where the roll method sets the MONTH as 0 
			endDate.roll(Calendar.YEAR, 1);
			endDate.roll(Calendar.MONTH, 1);
		}
		dbListing.setEndDate(endDate);
		dbListing.setEventId(eventId);
		dbListing.setId(listingId); 
		dbListing.setInhandDate(Calendar.getInstance());
		dbListing.setQuantity(new Integer (1));
		dbListing.setQuantityRemain(new Integer (1));
		dbListing.setRow("Row 13");
		dbListing.setSaleMethod(1L);
		dbListing.setSeats("Seats 123");
		dbListing.setSection("section Name");
		dbListing.setSplitOption(new Short ("1"));
		dbListing.setSplitQuantity(new Integer (2));
		dbListing.setVenueConfigSectionsId(venueConfigSectionId);
		dbListing.setSystemStatus("ACTIVE");
		dbListing.setDisplayPricePerTicket(new com.stubhub.newplatform.common.entity.Money ());
		
		com.stubhub.newplatform.common.entity.Money listPrice = new  com.stubhub.newplatform.common.entity.Money ();
		listPrice.setAmount(new BigDecimal (10.0));
		listPrice.setCurrency("USD");
		dbListing.setListPrice(listPrice);
		
		com.stubhub.newplatform.common.entity.Money faceValue = new  com.stubhub.newplatform.common.entity.Money ();
		faceValue.setAmount(new BigDecimal (20.0));
		dbListing.setFaceValue(faceValue);
		List<ListingSeatTrait> seatTraits = new ArrayList<ListingSeatTrait>();
		ListingSeatTrait seatTrait = new ListingSeatTrait();
		seatTrait.setSupplementSeatTraitId(1L);
		seatTraits.add(seatTrait);
		dbListing.setSeatTraits(seatTraits);
		
		return dbListing;
		
	}
	@Test
    public void testPopulateListingDetailsWithStatus() throws Exception{
        
        Listing listing=getDBListing();
        Event event = new Event();
        event.setId(165161L);
        event.setCountry("UK");
        Calendar cal = Calendar.getInstance();
        cal.add(cal.DATE, 10);
        event.setEventDate(cal);
        event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
        
        List<com.stubhub.domain.catalog.events.intf.TicketTrait> ttList = new ArrayList<com.stubhub.domain.catalog.events.intf.TicketTrait>();
        com.stubhub.domain.catalog.events.intf.TicketTrait tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
        tt.setName("Side stage (printed on ticket)");
        tt.setId(1011L);
        ttList.add(tt);
        tt = new com.stubhub.domain.catalog.events.intf.TicketTrait();
        tt.setName("Partial Suite (shared) - Reserved seating");
        tt.setId(1021L);
        ttList.add(tt);
        event.setTicketTrait(ttList);
        Mockito.when(inventoryMgr.getListing(Mockito.anyLong(), Mockito.any(Locale.class))).thenReturn(listing);
        Mockito.when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class), Mockito.any(Boolean.class))).thenReturn(event);
        com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();

        eventV3.setBobId("1");
        EventMobileAttribute mobileAttribute = new EventMobileAttribute();
        mobileAttribute.setStubhubMobileTicket(true);
        eventV3.setMobileAttributes(mobileAttribute);
        Venue venue = new Venue();
        venue.setCountry("GB");
        eventV3.setVenue(venue);
        Mockito.when(eventHelper.getEventV3ById(Mockito.anyLong(), Mockito.any(Locale.class), Mockito.any(Boolean.class), Mockito.any(Boolean.class))).thenReturn(eventV3);
        
        List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
        TicketSeat ticketSeat = new TicketSeat();
        ticketSeat.setSeatNumber("seat");
        ticketSeat.setTicketSeatId(7621736213L);
        ticketSeat.setTixListTypeId(2L);
        ticketSeat.setRow("row");
        ticketSeat.setExternalSeatId("externalseatId");
        ticketSeats.add(ticketSeat);
        Mockito.when(ticketSeatMgr.findActiveTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeats);
        
        List<SupplementSeatTrait> seatTraits = new ArrayList<SupplementSeatTrait>();
        SupplementSeatTrait seatTrait = new SupplementSeatTrait();
        seatTrait.setSupplementSeatTraitId(1011L);
        seatTraits.add(seatTrait);
    
        Mockito.when(listingSeatTraitMgr.getSupplementSeatTraitsForListing(Mockito.anyLong())).thenReturn(seatTraits);
		Mockito.when(shServiceContext.getRole()).thenReturn(ProxyRoleTypeEnum.Pricing.getName());
        
        //listing.setScrubbedSectionName("test");
        String status="ALL";
        listingHelper.populateListingDetails(status,12653213L, Locale.US, null, shServiceContext,"deliveryMethods");
        
    }
	
	@Test
	public void testGetTicketSeatsInfoByTicketId(){
		List<TicketSeat>  ticketSeatList = new ArrayList<>();
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatNumber("seat");
		ticketSeat.setTicketSeatId(7621736213L);
		ticketSeat.setRow("row");
		ticketSeatList.add(ticketSeat);
		Mockito.when(ticketSeatMgr.findTicketSeatsByTicketId(Mockito.anyLong())).thenReturn(ticketSeatList);
		List<TicketSeat> ticketSeatLists = listingHelper.getTicketSeatsInfoByTicketId("1234");
		Assert.assertTrue(ticketSeatLists.size()>0);
		for (TicketSeat ticketSeatData : ticketSeatLists) {
			Assert.assertEquals(ticketSeatData.getTicketSeatId(), ticketSeatList.get(0).getTicketSeatId());
			Assert.assertEquals(ticketSeatData.getRow(), ticketSeatList.get(0).getRow());
			Assert.assertEquals(ticketSeatData.getSeatNumber(), ticketSeatList.get(0).getSeatNumber());
		}
		
	}
}
