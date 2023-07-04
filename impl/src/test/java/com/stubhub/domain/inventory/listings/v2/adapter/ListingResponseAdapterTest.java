package com.stubhub.domain.inventory.listings.v2.adapter;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.catalog.events.biz.intf.VenueConfigSectionsBO;
import com.stubhub.domain.catalog.events.intf.TicketTrait;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.EventAttribute;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.SeatTrait;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHForbiddenException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHResourceNotFoundException;
import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.entity.ErrorEnum;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.entity.SaleMethod;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.common.util.PaginationInput;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.FulfillmentMethod;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.entity.ExpectedDeliveryDate;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.enums.ProxyRoleTypeEnum;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import junit.framework.Assert;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.*;

public class ListingResponseAdapterTest {
	
	MasterStubhubPropertiesWrapper masterStubhubProperties;
	
	@InjectMocks
	private ListingResponseAdapter la;

	VenueConfigSectionsBO mockVCS;
	
	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		masterStubhubProperties = Mockito.mock(MasterStubhubPropertiesWrapper.class);
		Mockito.when(masterStubhubProperties.getProperty("listing.country.facevalue.required","UK,GB,DE")).thenReturn("UK,GB,DE");
		mockVCS = Mockito.mock(VenueConfigSectionsBO.class);
		la.setVenueConfigSectionsBO(mockVCS);
	}
	
	public void getListings(){
		PaginationInput input = new PaginationInput();
		input.setEntriesPerPage(10);
		input.setPageNumber(1);
		
		SolrDocumentList list = new SolrDocumentList();	
		list.setNumFound(2);
		list.setStart(0);	
		list.add(mockSolrDocument(100, "ACTIVE", 10001,100f));
		list.add(mockSolrDocument(200, "InACTIVE", 10001,200f));	
		NamedList responseList = new NamedList();
		responseList.add("response", list);	
		QueryResponse response = new QueryResponse();
		response.setResponse(responseList);
		
		try {
			//ListingsResponse listingResponse = ListingResponseAdapter.convertQueryResponseToListingEntities(response, input);
		} catch (Exception e) {
			Assert.fail("unexpected exception "+e.getMessage());
		}
	}
	
	@Test
	public void convert_GAtest(){
		Listing listing = new Listing();
		
		Event event = new Event();
		event.setJdkTimeZone(TimeZone.getDefault());
		event.setEventDate(Calendar.getInstance());
		event.setCountry("UK");
		listing.setEvent(event);

		listing.setSection("GeneralTest");
		listing.setId(1000l);
		listing.setComments("comments");
		listing.setConfirmOption(1);
		listing.setCorrelationId("10001");
		listing.setDeliveryOption(1);
		listing.setEndDate(Calendar.getInstance());
		listing.setEventId(10001l);
		listing.setExternalId("10001");
		listing.setFaceValue(new Money(new BigDecimal(10), "USD"));
//		listing.setFulfillmentMethod(FulfillmentMethod.PDF);
		listing.setTicketMedium(TicketMedium.PDF.getValue());
		listing.setInhandDate(Calendar.getInstance());
		listing.setListPrice(new Money(new BigDecimal(10), "USD"));
		listing.setLmsApprovalStatus(1);
		listing.setQuantity(2);
		listing.setQuantityRemain(2);
		listing.setRow("A");
		listing.setSeats("1,2");
		listing.setSection("Lower Box");
		listing.setSellerPayoutAmountPerTicket(new Money(new BigDecimal(10), "USD"));
		listing.setSystemStatus("ACTIVE");
		listing.setSellerCCId(1L);
		listing.setSaleMethod(3L);
		listing.setMinPricePerTicket(new Money(new BigDecimal(3), "USD"));
		listing.setMaxPricePerTicket(new Money(new BigDecimal(100), "USD"));
		listing.setSellerContactId(2L);
		listing.setSellerContactGuid("4x8uX_QvyN_acu_F");
		listing.setSellerPaymentTypeId(2L);
		Product product = new Product();

		
		 TicketSeat ticketSeat = new TicketSeat();
			
			ticketSeat.setGeneralAdmissionInd(false);
			listing.setSection("not general admissions");
			ticketSeat.setTixListTypeId(2l);
			List<TicketSeat> ticketSeatList = new ArrayList<TicketSeat>();
			ticketSeatList.add(ticketSeat);
			listing.setTicketSeats(ticketSeatList); 
		ProxyRoleTypeEnum proxyRoleType = null;
		ListingResponse response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, false, false, getEvent(), null);
		Assert.assertFalse(response.getProducts().get(0).getGa());
	}
	
	@Test
	public void testProductFVUniqueTicketNumber(){
		Listing listing = new Listing();
		
		Event event = new Event();
		event.setJdkTimeZone(TimeZone.getDefault());
		event.setEventDate(Calendar.getInstance());
		event.setCountry("UK");
		listing.setEvent(event);

		listing.setSection("GeneralTest");
		listing.setId(1000l);
		listing.setComments("comments");
		listing.setConfirmOption(1);
		listing.setCorrelationId("10001");
		listing.setDeliveryOption(1);
		listing.setEndDate(Calendar.getInstance());
		listing.setEventId(10001l);
		listing.setExternalId("10001");
		listing.setFaceValue(new Money(new BigDecimal(10), "USD"));
//		listing.setFulfillmentMethod(FulfillmentMethod.PDF);
		listing.setTicketMedium(TicketMedium.PDF.getValue());
		listing.setInhandDate(Calendar.getInstance());
		listing.setListPrice(new Money(new BigDecimal(10), "USD"));
		listing.setLmsApprovalStatus(1);
		listing.setQuantity(2);
		listing.setQuantityRemain(2);
		listing.setRow("A");
		listing.setSeats("1,2");
		listing.setSection("Lower Box");
		listing.setSellerPayoutAmountPerTicket(new Money(new BigDecimal(10), "USD"));
		listing.setSystemStatus("ACTIVE");
		listing.setSellerCCId(1L);
		listing.setSaleMethod(3L);
		listing.setMinPricePerTicket(new Money(new BigDecimal(3), "USD"));
		listing.setMaxPricePerTicket(new Money(new BigDecimal(100), "USD"));
		listing.setSellerContactId(2L);
		listing.setSellerContactGuid("4x8uX_QvyN_acu_F");
		listing.setSellerPaymentTypeId(2L);
		Product product = new Product();
		
		 TicketSeat ticketSeat = new TicketSeat();
			
			ticketSeat.setGeneralAdmissionInd(false);
			listing.setSection("not general admissions");
			ticketSeat.setTixListTypeId(2l);
			ticketSeat.setFaceValue(new Money(new BigDecimal(10), "USD"));
			ticketSeat.setUniqueTicketNumber("12323");
			List<TicketSeat> ticketSeatList = new ArrayList<TicketSeat>();
			ticketSeatList.add(ticketSeat);
			listing.setTicketSeats(ticketSeatList); 
		ProxyRoleTypeEnum proxyRoleType = null;
		ListingResponse response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, false, false, getEvent(), null);
		Assert.assertEquals(10, response.getProducts().get(0).getFaceValue().getAmount().intValue());
		Assert.assertEquals("12323", response.getProducts().get(0).getUniqueTicketNumber());
		Assert.assertEquals("USD", response.getProducts().get(0).getFaceValue().getCurrency());


	}
	
	
	
	@Test
	public void testEventRestrictionSeatTrait(){
		Listing listing = new Listing();
		
		Event event = new Event();
		event.setJdkTimeZone(TimeZone.getDefault());
		event.setEventDate(Calendar.getInstance());
		event.setCountry("UK");
		listing.setEvent(event);

		listing.setSection("GeneralTest");
		listing.setId(1000l);
		listing.setComments("comments");
		listing.setConfirmOption(1);
		listing.setCorrelationId("10001");
		listing.setDeliveryOption(1);
		listing.setEndDate(Calendar.getInstance());
		listing.setEventId(10001l);
		listing.setExternalId("10001");
		listing.setFaceValue(new Money(new BigDecimal(10), "USD"));
//		listing.setFulfillmentMethod(FulfillmentMethod.PDF);
		listing.setTicketMedium(TicketMedium.PDF.getValue());
		listing.setInhandDate(Calendar.getInstance());
		listing.setListPrice(new Money(new BigDecimal(10), "USD"));
		listing.setLmsApprovalStatus(1);
		listing.setQuantity(2);
		listing.setQuantityRemain(2);
		listing.setRow("A");
		listing.setSeats("1,2");
		listing.setSection("Lower Box");
		listing.setSellerPayoutAmountPerTicket(new Money(new BigDecimal(10), "USD"));
		listing.setSystemStatus("ACTIVE");
		listing.setSellerCCId(1L);
		listing.setSaleMethod(3L);
		listing.setMinPricePerTicket(new Money(new BigDecimal(3), "USD"));
		listing.setMaxPricePerTicket(new Money(new BigDecimal(100), "USD"));
		listing.setSellerContactId(2L);
		listing.setSellerContactGuid("4x8uX_QvyN_acu_F");
		listing.setSellerPaymentTypeId(2L);
		Product product = new Product();
		
		 TicketSeat ticketSeat = new TicketSeat();
			
			ticketSeat.setGeneralAdmissionInd(false);
			listing.setSection("not general admissions");
			ticketSeat.setTixListTypeId(2l);
			ticketSeat.setFaceValue(new Money(new BigDecimal(10), "USD"));
			ticketSeat.setUniqueTicketNumber("12323");
			List<TicketSeat> ticketSeatList = new ArrayList<TicketSeat>();
			ticketSeatList.add(ticketSeat);
			listing.setTicketSeats(ticketSeatList); 
		ProxyRoleTypeEnum proxyRoleType = null;
		ListingResponse response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, false, false, getEventWithEvenWideSeatTrait(), null);
		Set<com.stubhub.domain.inventory.v2.DTO.TicketTrait> ticketTraits = response.getTicketTraits();
		Assert.assertEquals(false, ticketTraits.isEmpty());
		Assert.assertEquals("Event Restrictions", ticketTraits.iterator().next().getType());
	}
	
	private com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event getEvent(){
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();
		eventV3.setTimezone(TimeZone.getDefault().getDisplayName());
		eventV3.setEventDateUTC("2015-10-30T21:00:00Z");
		eventV3.setEventDateLocal("2015-10-30T21:00:00+01:00");
		eventV3.setName("Wicked");
		
		eventV3.setSeatTraits(new ArrayList<SeatTrait>());
		
		
		SeatTrait st = new SeatTrait();
		st.setId(13372l);
		st.setName("xyz");
		st.setType("1");
		eventV3.getSeatTraits().add(st);
		
		return eventV3;
	}
	
	
	private com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event getEventWithEvenWideSeatTrait(){
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = new com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event();
		eventV3.setTimezone(TimeZone.getDefault().getDisplayName());
		eventV3.setEventDateUTC("2015-10-30T21:00:00Z");
		eventV3.setEventDateLocal("2015-10-30T21:00:00+01:00");
		eventV3.setName("Wicked");
		
		eventV3.setSeatTraits(new ArrayList<SeatTrait>());
		
		
		SeatTrait st = new SeatTrait();
		st.setId(133472l);
		st.setName("xyz");
		st.setType("Event Restrictions");
		eventV3.getSeatTraits().add(st);
		
		return eventV3;
	}
	
	@Test
    public void convert_1(){
	  Listing listing = getListingMock();
		listing.setHideSeatInfoInd(Boolean.FALSE);

	  Money money = new Money("5.0");
	  listing.setTicketCost(money);
	  
	  List<ListingSeatTrait> seatTraits = new ArrayList<ListingSeatTrait>();
	  ListingSeatTrait st = new ListingSeatTrait();
	  st.setSupplementSeatTraitId(13372L);
	  seatTraits.add(st);
	  listing.setSeatTraits(seatTraits);
	  ListingResponseAdapter.convert(listing, true, null, null, false, false, false,getEvent(), null);
	}
	
	@Test
    public void convert_3(){
      Listing listing = getListingMock();
      listing.setDeliveryOption(com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.MANUAL.getValue());
      Money money = new Money("5.0");
      listing.setTicketCost(money);
      
      TicketSeat ticketSeat = new TicketSeat();
      
      ticketSeat.setGeneralAdmissionInd(false);
      listing.setSection("not general admissions");
      ticketSeat.setExternalSeatId("uyryuyyuuy");
      ticketSeat.setTixListTypeId(2l);
      List<TicketSeat> ticketSeatList = new ArrayList<TicketSeat>();
      ticketSeatList.add(ticketSeat);
      listing.setTicketSeats(ticketSeatList); 
      
      List<ListingSeatTrait> seatTraits = new ArrayList<ListingSeatTrait>();
      ListingSeatTrait st = new ListingSeatTrait();
      st.setSupplementSeatTraitId(13372L);
      seatTraits.add(st);
      listing.setSeatTraits(seatTraits);
      ListingResponseAdapter.convert(listing, true, null, null, false, false,false, getEvent(), null);
    }
	
	@Test
    public void convert_2(){
      Listing listing = getListingMock();
      listing.setInhandDate(null);
      listing.setBusinessGuid(UUID.randomUUID().toString());
      listing.setVenueConfigSectionsId(6L);
      Money money = new Money("5.0");
      listing.setTicketCost(money);
      
      com.stubhub.domain.inventory.listings.v2.entity.DeliveryMethod deliveryMethod = new com.stubhub.domain.inventory.listings.v2.entity.DeliveryMethod();
      ExpectedDeliveryDate ed = new ExpectedDeliveryDate();
      ed.setExpectedDate(Calendar.getInstance());
      deliveryMethod.setExpectedDeliveryDate(ed);
      List<com.stubhub.domain.inventory.listings.v2.entity.DeliveryMethod> deliveryMethodList = new ArrayList<com.stubhub.domain.inventory.listings.v2.entity.DeliveryMethod>();
      
      deliveryMethodList.add(deliveryMethod);
      
      List<ListingSeatTrait> seatTraits = new ArrayList<ListingSeatTrait>();
      ListingSeatTrait st = new ListingSeatTrait();
      st.setSupplementSeatTraitId(13372L);
      seatTraits.add(st);
      listing.setSeatTraits(seatTraits);
      ListingResponseAdapter.convert(listing, false, deliveryMethodList, null, false, false,false, getEvent(), null);
    }
	
	@Test
    public void convert_4(){
      Listing listing = getListingMock();
      listing.setInhandDate(Calendar.getInstance());
      listing.setEndDate(Calendar.getInstance());
      listing.setBusinessGuid(UUID.randomUUID().toString());
      listing.setVenueConfigSectionsId(6L);
      Money money = new Money("5.0");
      listing.setTicketCost(money);
      
      com.stubhub.domain.inventory.listings.v2.entity.DeliveryMethod deliveryMethod = new com.stubhub.domain.inventory.listings.v2.entity.DeliveryMethod();
      ExpectedDeliveryDate ed = new ExpectedDeliveryDate();
      ed.setExpectedDate(Calendar.getInstance());
      deliveryMethod.setExpectedDeliveryDate(ed);
      List<com.stubhub.domain.inventory.listings.v2.entity.DeliveryMethod> deliveryMethodList = new ArrayList<com.stubhub.domain.inventory.listings.v2.entity.DeliveryMethod>();
      
      deliveryMethodList.add(deliveryMethod);
      
      List<ListingSeatTrait> seatTraits = new ArrayList<ListingSeatTrait>();
      ListingSeatTrait st = new ListingSeatTrait();
      st.setSupplementSeatTraitId(13372L);
      seatTraits.add(st);
      listing.setSeatTraits(seatTraits);
      ListingResponseAdapter.convert(listing, true, deliveryMethodList, ProxyRoleTypeEnum.Fulfillment, false, false, false,getEvent(), null);
    }
	
	@Test
	public void convert(){
		Listing listing = getListingMock();
		listing.setHideSeatInfoInd(Boolean.TRUE);
		listing.setAutoPricingEnabledInd(Boolean.TRUE);
		ProxyRoleTypeEnum proxyRoleType = ProxyRoleTypeEnum.Super;
		
		ListingResponse response = ListingResponseAdapter.convert(listing, true, null, proxyRoleType, true, true, true, getEvent(), "abcd");
		//ListingResponse response = la.convert(listing, true, null, proxyRoleType);
		Assert.assertEquals(response.getPreDelivered(), Boolean.TRUE);
		Assert.assertEquals(response.getStatus(), ListingStatus.ACTIVE);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.PDF);
   		Assert.assertEquals(response.getCcId(),new Long(1L));
   		Assert.assertEquals(response.getSaleMethod(),SaleMethod.DECLINING);
   		Assert.assertEquals(response.getSellerIpAddress(),"12.13.145.176");
		listing.setScrubbedSectionName("GA");
		listing.setTicketMedium(TicketMedium.BARCODE.getValue());
		listing.setAutoPricingEnabledInd(Boolean.FALSE);
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true, getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.BARCODE);
		Assert.assertEquals(response.getScrubbedSectionName(), "GA");
		//Assert.assertFalse(response.isAutoPricingEnabledInd());
		listing.setTicketMedium(TicketMedium.FLASHSEAT.getValue());
		listing.setAutoPricingEnabledInd(null);
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, false, true, getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.FLASHSEAT);
		//Assert.assertFalse(response.isAutoPricingEnabledInd());
		
		listing.setTicketMedium(TicketMedium.EXTFLASH.getValue());
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, false, true, getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.FLASHSEAT);
		
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		listing.setFulfillmentDeliveryMethods("10,12,5.0,,2014-11-20T03:00:00Z");

		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, false, true, getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.UPS);
		
        listing.setSplitQuantity(3);
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		listing.setFulfillmentDeliveryMethods("9,12,5.0,,2014-11-20T03:00:00Z");
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.LMS);  	
		Assert.assertEquals(response.getSplitQuantity(), new Integer(3));
		proxyRoleType=ProxyRoleTypeEnum.Super;
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		listing.setFulfillmentDeliveryMethods("9,12,5.0,,2014-11-20T03:00:00Z");
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.LMS);
		
		listing.setFulfillmentDeliveryMethods("7,12,5.0,,2014-11-20T03:00:00Z");
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		
	}
	
	
	@Test
    public void parkingPassOnlyEvent(){
      Listing listing = getListingMock();
      Money money = new Money("5.0");
      listing.setTicketCost(money);
      
      List<ListingSeatTrait> seatTraits = new ArrayList<ListingSeatTrait>();
      ListingSeatTrait st = new ListingSeatTrait();
      st.setSupplementSeatTraitId(13372L);
      seatTraits.add(st);
      listing.setSeatTraits(seatTraits);
      com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event event = getEvent();
      EventAttribute eventAttribute = new EventAttribute();
      eventAttribute.setEventType("Parking");
      event.setEventAttributes(eventAttribute);
      
      ListingResponse response = ListingResponseAdapter.convert(listing, true, null, null, false, false, false, event, null);
      Assert.assertTrue(response.getIsParkingPassOnlyEvent());
      
      eventAttribute.setEventType("Not known");
      response = ListingResponseAdapter.convert(listing, true, null, null, false, false, false, event, null);
      Assert.assertFalse(response.getIsParkingPassOnlyEvent());
      
      event.setEventAttributes(null);      
      response = ListingResponseAdapter.convert(listing, true, null, null, false, false, false, event, null);
      Assert.assertFalse(response.getIsParkingPassOnlyEvent());
    }
	
	@Test
	public void convert_PrimaryTicketFalse(){
		Listing listing = new Listing();
		 TicketSeat ticketSeat = new TicketSeat();
		
		ticketSeat.setGeneralAdmissionInd(true);
		ticketSeat.setTixListTypeId(2l);
		List<TicketSeat> ticketSeatList = new ArrayList<TicketSeat>();
		ticketSeatList.add(ticketSeat);
		listing.setTicketSeats(ticketSeatList); 
		Product product = new Product();
		product.setGa(true);
	
		
		listing.setId(1000l);
		listing.setComments("comments");
		listing.setConfirmOption(1);
		listing.setCorrelationId("10001");
		listing.setDeliveryOption(1);
		listing.setEndDate(Calendar.getInstance());
		listing.setEventId(10001l);
		listing.setExternalId("10001");
		listing.setFaceValue(null);
		listing.setTicketMedium(TicketMedium.PDF.getValue());
		listing.setListPrice(new Money(new BigDecimal(10), "USD"));
		listing.setLmsApprovalStatus(1);
		listing.setQuantity(2);
		listing.setQuantityRemain(2);
		listing.setRow("A");
		listing.setSeats("1,2");
		listing.setSection("Lower Box");
		listing.setSellerPayoutAmountPerTicket(new Money(new BigDecimal(10), "USD"));
		listing.setSystemStatus("ACTIVE");
		listing.setSellerCCId(1L);
		listing.setSaleMethod(3L);
		listing.setMinPricePerTicket(new Money(new BigDecimal(3), "USD"));
		listing.setMaxPricePerTicket(new Money(new BigDecimal(100), "USD"));
		listing.setSellerContactId(2L);
		listing.setSellerContactGuid("4x8uX_QvyN_acu_F");
		listing.setSellerPaymentTypeId(2L);
		listing.setSection("General Admission");
		Assert.assertEquals((listing.getSection().equalsIgnoreCase(CommonConstants.GENERAL_ADMISSION)),product.getGa().booleanValue());
		
		Event event = new Event();
		event.setJdkTimeZone(TimeZone.getDefault());
		event.setEventDate(Calendar.getInstance());
		event.setCountry("UK");
		listing.setEvent(event);
		
		ListingSeatTrait listingSeatTrait = new ListingSeatTrait();
		listingSeatTrait.setSupplementSeatTraitId(13371L);
		listingSeatTrait.setTicketId(123L);
		
		List<ListingSeatTrait> seatTraitList = new ArrayList<ListingSeatTrait>();
		seatTraitList.add(listingSeatTrait);
		
		TicketTrait eventTrait = new TicketTrait();
		eventTrait.setId(13371L);
		eventTrait.setName("ABCD");
		eventTrait.setType("ABCD");
		
		List<TicketTrait> eventTraitList = new ArrayList<TicketTrait>();
		eventTraitList.add(eventTrait);
		
		event.setTicketTrait(eventTraitList);
		listing.setSeatTraits(seatTraitList);
		
		ProxyRoleTypeEnum proxyRoleType = null;
		ListingResponse response = ListingResponseAdapter.convert(listing, true, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getPreDelivered(), Boolean.TRUE);
		Assert.assertEquals(response.getStatus(), ListingStatus.ACTIVE);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.PDF);
   		Assert.assertEquals(response.getCcId(),new Long(1L));
   		Assert.assertEquals(response.getSaleMethod(),SaleMethod.DECLINING);
		listing.setScrubbedSectionName("GA");
		listing.setTicketMedium(TicketMedium.BARCODE.getValue());
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.BARCODE);
		Assert.assertEquals(response.getScrubbedSectionName(), "GA");
		
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		listing.setFulfillmentDeliveryMethods("10,12,5.0,,2014-11-20T03:00:00Z");
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.UPS);

		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		listing.setFulfillmentDeliveryMethods("9,12,5.0,,2014-11-20T03:00:00Z|10,12,5.0,,2014-11-20T03:00:00Z");
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.UPS);
		
		
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		listing.setFulfillmentDeliveryMethods("12,21,5.0,,2014-11-20T03:00:00Z");
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.SHIPPING);

		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		listing.setFulfillmentDeliveryMethods("11,21,5.0,,2014-11-20T03:00:00Z");
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.SHIPPING);
		
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		listing.setFulfillmentDeliveryMethods("9,12,5.0,,2014-11-20T03:00:00Z|12,21,5.0,,2014-11-20T03:00:00Z");
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.SHIPPING);

		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		listing.setFulfillmentDeliveryMethods("9,12,5.0,,2014-11-20T03:00:00Z|11,21,5.0,,2014-11-20T03:00:00Z");
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.SHIPPING);

		
        listing.setSplitQuantity(3);
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		listing.setFulfillmentDeliveryMethods("9,12,5.0,,2014-11-20T03:00:00Z");
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.LMS);  	
		Assert.assertEquals(response.getSplitQuantity(), new Integer(3));
		
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		listing.setFulfillmentDeliveryMethods("7,12,5.0,,2014-11-20T03:00:00Z");
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.LMS);  	
		
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		listing.setFulfillmentDeliveryMethods("7,12,5.0,,2014-11-20T03:00:00Z|9,12,5.0,,2014-11-20T03:00:00Z");
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.LMS);  	

		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		listing.setFulfillmentDeliveryMethods("9,12,5.0,,2014-11-20T03:00:00Z|7,12,5.0,,2014-11-20T03:00:00Z");
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.LMS);  	
		
		proxyRoleType=ProxyRoleTypeEnum.Super;
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		listing.setFulfillmentDeliveryMethods("9,12,5.0,,2014-11-20T03:00:00Z");
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.LMS); 
		
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		listing.setFulfillmentMethod(null);
		listing.setFulfillmentDeliveryMethods("17,5.0,,2014-11-20T03:00:00Z");
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.LOCALDELIVERY);
		
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		listing.setFulfillmentDeliveryMethods("8,5.0,,2014-11-20T03:00:00Z");
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.WILLCALL);
		
		listing.setTicketMedium(TicketMedium.MOBILE.getValue());
		listing.setFulfillmentDeliveryMethods("21,5.0,,2014-11-20T03:00:00Z");
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.MOBILE);
		
		listing.setTicketMedium(TicketMedium.EXTMOBILE.getValue());
		listing.setFulfillmentDeliveryMethods("18,5.0,,2014-11-20T03:00:00Z");
		response = ListingResponseAdapter.convert(listing, false, null, proxyRoleType, false, true, true,getEvent(), null);
		Assert.assertEquals(response.getDeliveryOption(), DeliveryOption.MOBILETRANSFER);
	}
	
	@Test
	public void testGetListingResponse() {
		ListingResponse response = new ListingResponse();
		Listing listing = new Listing();
		listing.setId(123456L);
		listing.setSystemStatus("ACTIVE");
		listing.setEventId(12345L);
		listing.setSellerId(12345L);
		response = ListingResponseAdapter.getListingResponse(listing);
		Assert.assertNotNull(response);
	}
	
	@Test
    public void testGetListingResponse_1() {
        ListingResponse response = new ListingResponse();
        Listing listing = new Listing();
        listing.setId(123456L);
        listing.setSystemStatus("PENDING LOCK");
		listing.setEventId(12345L);
		listing.setSellerId(12345L);
        response = ListingResponseAdapter.getListingResponse(listing);
        Assert.assertNotNull(response);
    }
	
	@Test
    public void testGetVenueConfigSectionsBO() {
        VenueConfigSectionsBO bo = la.getVenueConfigSectionsBO();
        Assert.assertNotNull(bo);
    }
	
	private SolrDocument mockSolrDocument(long id, String status, long eventId, float price) {
		SolrDocument doc = new SolrDocument();
		doc.setField("TICKET_ID", id + "");
		doc.setField("TICKET_STATUS", status);
		doc.setField("EXTERNAL_LISTING_ID", "10000");
		doc.setField("SECTION", "Lower");
		doc.setField("ROW_DESC", "1");
		doc.setField("SEATS", "1,2,3");
		doc.setField("QUANTITY_REMAIN", 2);
		doc.setField("QUANTITY", 2);
		doc.setField("SOLD_QUANTITY", 2);
		doc.setField("CURRENCY_CODE", "USD");
		doc.setField("FACE_VALUE", 100.00f);
		doc.setField("TICKET_PRICE", price);
		doc.setField("PRICE_PER_TICKET", price);
		doc.setField("SELLER_PAYOUT_AMT_PER_TICKET", 1250.f);
		doc.setField("DELIVERY_OPTION_ID", "1");
		doc.setField("TICKET_MEDIUM", "1");
		doc.setField("LMS_APPROVAL_STATUS_ID", "1");
		doc.setField("EXPECTED_INHAND_DATE", new Date());
		doc.setField("SOLD_DATE", new Date());
		doc.setField("SPLIT", "2");
		doc.setField("SALE_END_DATE", new Date());
		//doc.setField("VENUE_CONFIG_SECTIONS_ID", 12345l);
		List<String> traits = new ArrayList<String>();
		traits.add("959,966,102,203,601,101|Actual 4th row,50 yd line,Parking Pass,Alcohol-free seating,Student Ticket,Aisle|3,3,1,2,2,1|Seller Comments,Seller Comments,Ticket Feature,Listing Disclosure,Listing Disclosure,Ticket Feature");
		doc.setField("TICKET_TRAIT_INFO", traits);
		doc.setField("EVENT_DESCRIPTION", "U2");
		doc.setField("EVENT_ID", eventId + "");
		doc.setField("EVENT_DATE_LOCAL", "2014-04-03T12:00:00Z");
		doc.setField("VENUE_DESCRIPTION", "O2 Arena");
		doc.setField("VENUE_DESCRIPTION", "O2 Arena");
		return doc;
	}
	 
	@Test(expectedExceptions={SHResourceNotFoundException.class})
	public void test404ListingException(){
		ListingError listingError = new ListingError(ErrorType.NOT_FOUND, ErrorCode.BULK_JOB_NOT_FOUND, ErrorEnum.BULK_JOB_NOT_FOUND.getMessage(), "123");
		ListingBusinessException listingException = new ListingBusinessException(listingError);
		ListingResponseAdapter.errorMappingThrowException(listingException);
	}
	
	@Test(expectedExceptions={SHForbiddenException.class})
	public void test403ListingException(){
		ListingError listingError = new ListingError(ErrorType.AUTHORIZATIONERROR, ErrorCode.SELLER_NOT_AUTHORIZED, "Seller is not authorized to update the listing", null);
		ListingBusinessException listingException = new ListingBusinessException(listingError);
		ListingResponseAdapter.errorMappingThrowException(listingException);
	}
	
	@Test
	public void testGetListingRespWithData() {
		Listing listing = new Listing();
		listing.setAdjustPrice(true);
		listing.setPriceAdjusted(true);
		listing.setListPrice(new Money("1", "USD"));
		listing.setId(12345L);
		listing.setEventId(12345L);
		listing.setSellerId(12345L);
		ListingResponseAdapter.getListingRespWithData(listing, true);
	}
	
	@Test
	public void testGetListingRespWithDataIsInHandAdjusted() {
		Listing listing = new Listing();
		listing.setAdjustPrice(true);
		listing.setPriceAdjusted(true);
		listing.setListPrice(new Money("1", "USD"));
		listing.setId(12345L);
		listing.setInHandDateAdjusted(true);
		listing.setSaleEndDateAdjusted(true);
		listing.setInhandDate(Calendar.getInstance());
		listing.setEndDate(Calendar.getInstance());
		listing.setEventId(12345L);
		listing.setSellerId(12345L);
		ListingResponseAdapter.getListingRespWithData(listing, true);
	}

    @Test
    public void testGetErrorMapping() {
      ListingError lerror = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.DUPLICATE_BARCODE_ERROR, "test1", "test1");
      ListingBusinessException listingException = new ListingBusinessException(lerror);
      Assert.assertNotNull(ListingResponseAdapter.getErrorMapping(listingException));
    }
     
    @Test
    public void testGetErrorMapping_ex() {
      
      ListingError lerror = new ListingError(ErrorType.SYSTEMERROR, ErrorCode.DUPLICATE_BARCODE_ERROR, "test1", "test1");
      ListingBusinessException listingException = new ListingBusinessException(lerror);
      boolean exceptionThrown = false;
      try{
        ListingResponseAdapter.getErrorMapping(listingException);
      }catch(SHSystemException se){
        exceptionThrown = true;
      }
      Assert.assertTrue(exceptionThrown);
    }
    
    @Test
    public void testGetErrorMapping_auth() {
      ListingError lerror = new ListingError(ErrorType.AUTHENTICATIONERROR, ErrorCode.DUPLICATE_BARCODE_ERROR, "test1", "test1");
      ListingBusinessException listingException = new ListingBusinessException(lerror);
      Assert.assertNotNull(ListingResponseAdapter.getErrorMapping(listingException));
    }
    
	public static Listing getListingMock() {
		Listing listing = new Listing();
		
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setSeatStatusId(1L);
		ticketSeat.setGeneralAdmissionInd(true);
		ticketSeat.setTixListTypeId(1l);
		ticketSeat.setTicketId(1000L);
		
		List<TicketSeat> ticketSeatList = new ArrayList<TicketSeat>();
		ticketSeatList.add(ticketSeat);
		ticketSeatList.add(ticketSeat);
		listing.setTicketSeats(ticketSeatList);
		
		Product product = new Product();
		product.setGa(true);
		
		listing.setId(1000l);
		listing.setComments("comments");
		listing.setConfirmOption(1);
		listing.setCorrelationId("10001");
		listing.setDeliveryOption(1);
		
		Calendar today = Calendar.getInstance();
		Calendar fiveDaysFromToday = Calendar.getInstance();
		fiveDaysFromToday.add(Calendar.DATE, 5);
		Calendar tenDaysFromToday = Calendar.getInstance();
		tenDaysFromToday.add(Calendar.DATE, 10);
		listing.setCreatedDate(today);
		
		
		listing.setEndDate(fiveDaysFromToday);
		listing.setEventId(10001l);
		listing.setExternalId("10001");
		listing.setFaceValue(new Money(new BigDecimal(10), "USD"));
//		listing.setFulfillmentMethod(FulfillmentMethod.PDF);
		listing.setTicketMedium(TicketMedium.PDF.getValue());
		listing.setInhandDate(Calendar.getInstance());
		listing.setListPrice(new Money(new BigDecimal(10), "USD"));
		listing.setLmsApprovalStatus(1);
		listing.setQuantity(2);
		listing.setQuantityRemain(2);
		listing.setSplitOption(Short.valueOf("2"));
		listing.setRow("A");
		listing.setSeats("1,2");
		listing.setSection("Lower Box");
		listing.setSellerPayoutAmountPerTicket(new Money(new BigDecimal(10), "USD"));
		listing.setSystemStatus("ACTIVE");
		listing.setSellerCCId(1L);
		listing.setSaleMethod(3L);
		listing.setMinPricePerTicket(new Money(new BigDecimal(3), "USD"));
		listing.setMaxPricePerTicket(new Money(new BigDecimal(100), "USD"));
		listing.setSellerContactId(2L);
		listing.setSellerContactGuid("4x8uX_QvyN_acu_F");
		listing.setSellerPaymentTypeId(2L);
		listing.setSection("General Admission");
		listing.setListPrice(new Money(new BigDecimal(80), "USD"));
		listing.setDisplayPricePerTicket(new Money(new BigDecimal(100), "USD"));
		listing.setVenueConfigSectionsId(1234l);
		listing.setIpAddress("12.13.145.176");
		
		Event event = new Event();
		event.setId(5555L);
		event.setJdkTimeZone(TimeZone.getDefault());
		event.setEventDate(tenDaysFromToday);
		listing.setEvent(event);
		return listing;
	}
	
}
