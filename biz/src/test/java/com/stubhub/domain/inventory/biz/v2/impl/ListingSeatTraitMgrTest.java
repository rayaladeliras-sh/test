package com.stubhub.domain.inventory.biz.v2.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import junit.framework.Assert;

import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.catalog.read.v3.intf.seatTraits.dto.response.SeatTrait;
import com.stubhub.domain.catalog.read.v3.intf.seatTraits.dto.response.SeatTraits;
import com.stubhub.domain.inventory.datamodel.dao.ExternalSystemDAO;
import com.stubhub.domain.inventory.datamodel.dao.ListingSeatTraitDAO;
import com.stubhub.domain.inventory.datamodel.dao.SupplementSeatTraitDAO;
import com.stubhub.domain.inventory.datamodel.dao.VendorStubEventXrefDAO;
import com.stubhub.domain.inventory.datamodel.entity.ExternalSystem;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.SupplementSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.VendorStubEventXref;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

public class ListingSeatTraitMgrTest {
	
	private ListingSeatTraitMgrImpl listingSeatTraitMgrImpl;
	private ListingSeatTraitDAO listingSeatTraitDAO;
	private SupplementSeatTraitDAO supplementSeatTraitDAO;
	private VendorStubEventXrefDAO vendorStubEventXrefDAO;
	private ExternalSystemDAO externalSystemDAO;
	private MasterStubhubPropertiesWrapper masterStubhubProperties;
	private ObjectMapper objectMapper;
	private SvcLocator svcLocator;
	private WebClient webClient;
	
	@BeforeMethod
	public void setUp() {
		listingSeatTraitMgrImpl = new ListingSeatTraitMgrImpl();
		listingSeatTraitDAO = mock(ListingSeatTraitDAO.class);
		supplementSeatTraitDAO = mock(SupplementSeatTraitDAO.class);
		vendorStubEventXrefDAO = mock(VendorStubEventXrefDAO.class);
		externalSystemDAO = mock(ExternalSystemDAO.class);
		masterStubhubProperties = mock(MasterStubhubPropertiesWrapper.class);
		objectMapper = mock(ObjectMapper.class);
		svcLocator = mock(SvcLocator.class);
		webClient = mock(WebClient.class);
		ReflectionTestUtils.setField(listingSeatTraitMgrImpl, "listingSeatTraitDAO", listingSeatTraitDAO);
		ReflectionTestUtils.setField(listingSeatTraitMgrImpl, "supplementSeatTraitDAO", supplementSeatTraitDAO);
		ReflectionTestUtils.setField(listingSeatTraitMgrImpl, "vendorStubEventXrefDAO", vendorStubEventXrefDAO);
		ReflectionTestUtils.setField(listingSeatTraitMgrImpl, "externalSystemDAO", externalSystemDAO);
		ReflectionTestUtils.setField(listingSeatTraitMgrImpl, "masterStubhubProperties", masterStubhubProperties);
		ReflectionTestUtils.setField(listingSeatTraitMgrImpl, "objectMapper", objectMapper);
		ReflectionTestUtils.setField(listingSeatTraitMgrImpl, "svcLocator", svcLocator);
	}
	
	@Test
	public void getSeatTraitsFromCommentsTest(){
		when(listingSeatTraitDAO
		.getSeatTraitsFromComments(any(Long.class),
				any(String.class))).thenReturn(new ArrayList<Long>());
		listingSeatTraitMgrImpl.getSeatTraitsFromComments(12345L, "aisle");
	}
	
	@Test
	public void findSeatTraitsTest(){
		when(listingSeatTraitDAO.findSeatTraits(12345L)).thenReturn(new ArrayList<ListingSeatTrait>());
		listingSeatTraitMgrImpl.findSeatTraits(2456678L);
		
	}
	
	@Test
	public void addSeatTraitTest(){
		ListingSeatTrait seatTrait = new ListingSeatTrait();
		when(listingSeatTraitDAO.addSeatTrait(any(ListingSeatTrait.class))).thenReturn(new ListingSeatTrait());
		listingSeatTraitMgrImpl.addSeatTrait(seatTrait);
	}
	
	@Test
	public void deleteListingSeatTraitTest(){
		ListingSeatTrait seatTrait = new ListingSeatTrait();
		when(listingSeatTraitDAO.deleteListingSeatTrait(any(ListingSeatTrait.class))).thenReturn(new ListingSeatTrait());
		listingSeatTraitMgrImpl.deleteListingSeatTrait(seatTrait);
	}
	
	@Test
	public void getSupplementSeatTraitTest(){
		when(supplementSeatTraitDAO.findById(any(Long.class))).thenReturn(new SupplementSeatTrait());
		listingSeatTraitMgrImpl.getSupplementSeatTrait(12347L);
	}
	
	@Test
	public void getSupplementSeatTraitsForListingTest(){
		when(supplementSeatTraitDAO.getSupplementSeatTraitsForListing(any(Long.class))).thenReturn(new ArrayList<SupplementSeatTrait>());
		listingSeatTraitMgrImpl.getSupplementSeatTraitsForListing(24556789L);
	}
	
	@Test
	public void getSupplementSeatTraitsbySeatTraitIdsTest(){
		when(supplementSeatTraitDAO.getSupplementSeatTraitsForListing(any(Long.class))).thenReturn(new ArrayList<SupplementSeatTrait>());
		List<Long> ids = new ArrayList<Long>();
		ids.add(101l);
		ids.add(102l);
		listingSeatTraitMgrImpl.getSupplementSeatTraitsBySeatTraitIds(ids);
	}
	
	@Test
	public void isParkingSupportedForEventTest_True(){
		Long eventId = 12345L;
		VendorStubEventXref vendorStubEventXref = new VendorStubEventXref();
		vendorStubEventXref.setExtSystemId(1L);
		
		ExternalSystem extSystem = new ExternalSystem();
		extSystem.setParkingPassBarcodeInd(1L);
		when(vendorStubEventXrefDAO.getByEventId(any(Long.class))).thenReturn(vendorStubEventXref);
		when(externalSystemDAO.findById(any(Long.class))).thenReturn(extSystem);
		Boolean isParkingSupported = listingSeatTraitMgrImpl.isParkingSupportedForEvent(eventId);
		Assert.assertTrue(isParkingSupported);
	}

	@Test
	public void isParkingSupportedForEventTest_NullInput(){
		Long eventId = null;		
		Boolean isParkingSupported = listingSeatTraitMgrImpl.isParkingSupportedForEvent(eventId);
		Assert.assertFalse(isParkingSupported);
	}
	
	@Test
	public void testParseComments() throws Exception {
		when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("http://api-int.slcq015.com/catalog-read/v3/seatTraits/?eventId={eventId}&seatingComment={seatingComment}");
		when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		when(webClient.get()).thenReturn(getResponse200());
		when(objectMapper.readValue(any(InputStream.class), Mockito.eq(SeatTraits.class))).thenReturn(getSeatTraitsResponse());
		List<Long> seatTraitIds = listingSeatTraitMgrImpl.parseComments(1234L, "Aisle, Parking pass, Club Pass/Access");
		Assert.assertEquals(3, seatTraitIds.size());
	}
	
	@Test
	public void testParseCommentsError() throws Exception {
		when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString())).thenReturn("http://api-int.slcq015.com/catalog-read/v3/seatTraits/?eventId={eventId}&seatingComment={seatingComment}");
		when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		when(webClient.get()).thenThrow(new RuntimeException());
		List<Long> seatTraitIds = listingSeatTraitMgrImpl.parseComments(1234L, "Aisle, Parking pass, Club Pass/Access");
		Assert.assertEquals(0, seatTraitIds.size());
	}
	
	private Response getResponse200() {
		Response response = new Response() {
			@Override
			public Object getEntity() {
				String response = "{\"seatTraits\":[{\"id\":101},{\"id\":102},{\"id\":103}]}";
				return new ByteArrayInputStream(response.getBytes());
			}

			@Override
			public int getStatus() {
				return 200;
			}

			@Override
			public MultivaluedMap<String, Object> getMetadata() {
				return null;
			}
		};
		return response;
	}
	
	private SeatTraits getSeatTraitsResponse() {
		SeatTraits seatTraitsResponse = new SeatTraits();
		List<SeatTrait> seatTraits = new ArrayList<SeatTrait>();
		SeatTrait seatTrait1 = new SeatTrait();
		seatTrait1.setId(101L);
		SeatTrait seatTrait2 = new SeatTrait();
		seatTrait2.setId(102L);
		SeatTrait seatTrait3 = new SeatTrait();
		seatTrait3.setId(103L);
		seatTraits.add(seatTrait1);
		seatTraits.add(seatTrait2);
		seatTraits.add(seatTrait3);
		seatTraitsResponse.setSeatTraits(seatTraits);
		return seatTraitsResponse;
	}
	
	
}
