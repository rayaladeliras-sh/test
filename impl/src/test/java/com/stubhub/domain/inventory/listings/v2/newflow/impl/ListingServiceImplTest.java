package com.stubhub.domain.inventory.listings.v2.newflow.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stubhub.domain.i18n.infra.soa.core.I18nServiceContext;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.listings.v2.helper.ListingControlHelper;
import com.stubhub.domain.inventory.listings.v2.helper.ListingCreateProcess;
import com.stubhub.domain.inventory.listings.v2.impl.ListingServiceImpl;
import com.stubhub.domain.inventory.listings.v2.newflow.orchestrator.ListingOrchestrator;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingInternal;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContext;

public class ListingServiceImplTest {

	@InjectMocks
	private ListingServiceImpl listingServiceImpl;

	@Mock
	private ListingRequest listingRequest;

	@Mock
	private SHServiceContext shServiceContext;

	@Mock
	private I18nServiceContext i18nServiceContext;

	@Mock
	private ExtendedSecurityContext securityContext;

	@Mock
	private MessageContext context;

	@Mock
	private HttpHeaders headers;

	@Mock
	private UriInfo uriInfo;

	@Mock
	private MultivaluedMap<String, String> mvMap;

	@Mock
	private MasterStubhubPropertiesWrapper masterStubhubProperties;

	@Mock
	private ListingOrchestrator listingOrchestrator;

	@Mock
	private ListingCreateProcess listingCreateProcess;

	@Mock
	private ListingResponse newFlowListingResponse;

	@Mock
	private ListingResponse oldFlowListingResponse;
	
	@Mock
    private ListingControlHelper listingControlHelper;

	@BeforeTest
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(i18nServiceContext.getLocale()).thenReturn(Locale.US);
		when(i18nServiceContext.getSHStore()).thenReturn("1");

		when(securityContext.getUserId()).thenReturn("1000");
		when(securityContext.getUserGuid()).thenReturn("AB12345");
		when(securityContext.getUserName()).thenReturn("User");

		Map<String, Object> extendedInfo = new HashMap<String, Object>();
		extendedInfo.put("http://stubhub.com/claims/subscriber", "api_us_sell_indy03@testmail.com");
		when(securityContext.getExtendedInfo()).thenReturn(extendedInfo);
		shServiceContext.setExtendedSecurityContext(securityContext);

		when(shServiceContext.getExtendedSecurityContext()).thenReturn(securityContext);

		List<String> headerList = new ArrayList<String>();
		headerList.add("test value");
		when(headers.getRequestHeader(anyString())).thenReturn(headerList);

		when(mvMap.getFirst("status")).thenReturn("ALL");
		when(uriInfo.getQueryParameters()).thenReturn(mvMap);

		when(context.getHttpHeaders()).thenReturn(headers);
		when(context.getUriInfo()).thenReturn(uriInfo);

		when(listingOrchestrator.updateListing(anyString(), (ListingRequest) any(), (SHServiceContext) any(),
				(I18nServiceContext) any(), (MessageContext) any())).thenReturn(newFlowListingResponse);

		List<ListingResponse> listingResponses = new ArrayList<ListingResponse>();
		listingResponses.add(oldFlowListingResponse);

		when(listingCreateProcess.createOrUpdateListings((BulkListingInternal) any(), anyString(), anyString(),
				anyBoolean(), anyBoolean())).thenReturn(listingResponses);
		
		when(listingControlHelper.isListingBlock(anyBoolean(), anyBoolean(), anyBoolean(), anyString())).thenReturn(false);
	}

	@Test
	public void testUpdateListingNewFlow() {
		when(masterStubhubProperties.getProperty("inventory.newflow.global", "false")).thenReturn("true");
		when(masterStubhubProperties.getProperty("inventory.newflow.update.listing", "false")).thenReturn("true");

		ListingResponse response = listingServiceImpl.updateListing("123456", listingRequest, shServiceContext,
				i18nServiceContext);

		// Check if the response is from the new flow
		assertEquals(response, newFlowListingResponse);
	}

	@Test
	public void testUpdateListingOldFlow() {
		when(masterStubhubProperties.getProperty("inventory.newflow.global", "false")).thenReturn("false");
		when(masterStubhubProperties.getProperty("inventory.newflow.update.listing", "false")).thenReturn("false");

		ListingResponse response = listingServiceImpl.updateListing("123456", listingRequest, shServiceContext,
				i18nServiceContext);

		// Check if the response is from the old flow
		assertEquals(response, oldFlowListingResponse);
	}

	@Test
	public void testGlobalSwitch() {
		when(masterStubhubProperties.getProperty("inventory.newflow.global", "false")).thenReturn("false");
		when(masterStubhubProperties.getProperty("inventory.newflow.update.listing", "false")).thenReturn("true");

		ListingResponse response = listingServiceImpl.updateListing("123456", listingRequest, shServiceContext,
				i18nServiceContext);

		// Check if the response is from the old flow
		assertEquals(response, oldFlowListingResponse);
	}

	@Test
	public void testUpdateListingNewFlowSwitchingToOldFlow() {
		when(masterStubhubProperties.getProperty("inventory.newflow.global", "false")).thenReturn("true");
		when(masterStubhubProperties.getProperty("inventory.newflow.update.listing", "false")).thenReturn("true");

		when(listingOrchestrator.updateListing(anyString(), (ListingRequest) any(), (SHServiceContext) any(),
				(I18nServiceContext) any(), (MessageContext) any())).thenReturn(null);

		ListingResponse response = listingServiceImpl.updateListing("123456", listingRequest, shServiceContext,
				i18nServiceContext);

		// Check if the response is from the new flow
		assertEquals(response, oldFlowListingResponse);
	}
}
