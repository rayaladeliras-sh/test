package com.stubhub.domain.inventory.listings.v2.impl;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.accountmanagement.services.orders.v3.intf.dto.OrderItem;
import com.stubhub.domain.i18n.infra.soa.core.I18nServiceContext;
import com.stubhub.domain.infrastructure.common.exception.base.SHRuntimeException;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHBadRequestException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHForbiddenException;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.entity.SplitOption;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.controller.helper.IntegrationHelper;
import com.stubhub.domain.inventory.listings.v2.helper.AdvisoryCurrencyHelper;
import com.stubhub.domain.inventory.listings.v2.helper.ListingControlHelper;
import com.stubhub.domain.inventory.listings.v2.helper.ListingCreateProcess;
import com.stubhub.domain.inventory.listings.v2.helper.ListingHelper;
import com.stubhub.domain.inventory.listings.v2.helper.RelistHelper;
import com.stubhub.domain.inventory.listings.v2.helper.TransferValidator;
import com.stubhub.domain.inventory.listings.v2.tns.FraudEvaluationService;
import com.stubhub.domain.inventory.listings.v2.util.JMSMessageHelper;
import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.DTO.RelistListing;
import com.stubhub.domain.inventory.v2.DTO.RelistRequest;
import com.stubhub.domain.inventory.v2.DTO.RelistResponse;
import com.stubhub.domain.inventory.v2.DTO.RelistResponse.RelistListingResponse;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingInternal;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;

import junit.framework.Assert;

public class ListingServiceTest extends SHInventoryTest {
	private HttpHeaders headers;
	private UriInfo uriInfo;
	private MessageContext messageContext;
	private ListingCreateProcess listingCreateProcess;

	private ListingServiceImpl listingService = null;

	private ExtendedSecurityContext securityContext;
	private SHServiceContext shServiceContext;
	private I18nServiceContext i18nServiceContext;

	private ListingHelper listingHelper;
	// markup

	@InjectMocks
	private ListingServiceImpl listingServiceImpl;

	@Mock
	private IntegrationHelper integrationHelper;

	@Mock
	private RelistHelper relistHelper;

	@Mock
	private JMSMessageHelper jmsMessageHelper;

	@Mock
	private TransferValidator transferValidator;

	@Mock
	private InventoryMgr inventoryMgr;

	@Mock
	private AdvisoryCurrencyHelper advisoryCurrencyHelper;

	@Mock
	private MasterStubhubPropertiesWrapper masterStubhubProperties;

	@Mock
	private FraudEvaluationService fraudEvaluationService;

	@Mock
	private ListingControlHelper listingControlHelper;

	@SuppressWarnings("unchecked")
	@BeforeTest
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		uriInfo = super.mockUriInfo();
		headers = super.mockHeaders(null, null);
		messageContext = mock(MessageContext.class);
		HttpHeaders headers = Mockito.mock(HttpHeaders.class);
		List<String> headerList = new ArrayList<String>();
		headerList.add("test value");
		Mockito.when(headers.getRequestHeader(Mockito.anyString())).thenReturn(headerList);

		MultivaluedMap<String, String> mvMap = mock(MultivaluedMap.class);
		Mockito.when(mvMap.getFirst("status")).thenReturn("ALL");
		UriInfo uriInfo = mock(UriInfo.class);
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(mvMap);

		shServiceContext = mockServiceContext("1000");
		listingService = new ListingServiceImpl();

		listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null, null);
		listingHelper = Mockito.mock(ListingHelper.class);
		relistHelper = Mockito.mock(RelistHelper.class);
		integrationHelper = Mockito.mock(IntegrationHelper.class);
		jmsMessageHelper = Mockito.mock(JMSMessageHelper.class);
		transferValidator = Mockito.mock(TransferValidator.class);
		inventoryMgr = Mockito.mock(InventoryMgr.class);
		advisoryCurrencyHelper = Mockito.mock(AdvisoryCurrencyHelper.class);
		masterStubhubProperties = Mockito.mock(MasterStubhubPropertiesWrapper.class);
		fraudEvaluationService = Mockito.mock(FraudEvaluationService.class);
		listingControlHelper = Mockito.mock(ListingControlHelper.class);

		setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);
		setBeanProperty(listingService, "listingHelper", listingHelper);
		setBeanProperty(listingService, "jmsMessageHelper", jmsMessageHelper);
		setBeanProperty(listingService, "httpHeaders", headers);
		setBeanProperty(listingService, "uriInfo", uriInfo);
		setBeanProperty(listingService, "integrationHelper", integrationHelper);
		setBeanProperty(listingService, "relistHelper", relistHelper);
		setBeanProperty(listingService, "transferValidator", transferValidator);
		setBeanProperty(listingService, "inventoryMgr", inventoryMgr);
		setBeanProperty(listingService, "advisoryCurrencyHelper", advisoryCurrencyHelper);
		setBeanProperty(listingService, "masterStubhubProperties", masterStubhubProperties);
		setBeanProperty(listingService, "fraudEvaluationService", fraudEvaluationService);
		setBeanProperty(listingService, "listingControlHelper", listingControlHelper);

		setBeanProperty(listingService, "context", messageContext);
		listingService.setMessageContext(messageContext);
		securityContext = mockSecurityContext("1000");
		Map<String, Object> extendedInfo = new HashMap<String, Object>();
		extendedInfo.put("http://stubhub.com/claims/subscriber", "api_us_sell_indy03@testmail.com");
		Mockito.when(securityContext.getExtendedInfo()).thenReturn(extendedInfo);
		shServiceContext.setExtendedSecurityContext(securityContext);

		i18nServiceContext = Mockito.mock(I18nServiceContext.class);
		Mockito.when(i18nServiceContext.getLocale()).thenReturn(Locale.US);
		Mockito.when(i18nServiceContext.getSHStore()).thenReturn("1");
		Mockito.when(messageContext.getHttpHeaders()).thenReturn(headers);
		Mockito.when(messageContext.getUriInfo()).thenReturn(uriInfo);
		Mockito.doNothing().when(fraudEvaluationService).submitToQueue(Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString(), Mockito.any(ListingStatus.class));

		Mockito.when(listingControlHelper.isListingBlock(anyBoolean(), anyBoolean(), anyBoolean(), anyString()))
				.thenReturn(false);

	}

	////// test methods go here

	@Test
	public void testCallListingServiceSuccess() throws Exception {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);
		Map<String, Object> extendedInfo = securityContext.getExtendedInfo();
		extendedInfo.put("http://stubhub.com/claims/operatorapp", "SHIP");

		List<ListingResponse> resps = new ArrayList<ListingResponse>();
		resps.add(new ListingResponse());

		ListingCreateProcess listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null,
				null);
		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(resps);
		setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);

		ListingRequest request = new ListingRequest();
		ListingResponse response = listingService.updateListing("1000", request, shServiceContext, i18nServiceContext);

		Assert.assertTrue("Simple updateListing should work", response.getErrors() == null);

		// test AUTHZ mode -------------------------------------------

		String GUID = "ABCDEFG1234567";

		Map<String, String> attrMap = new HashMap<String, String>();
		attrMap.put("role", "R2");
		attrMap.put("operatorId", "value");
		attrMap.put("proxiedId", "1");

		shServiceContext.setAttributeMap(attrMap);

		Mockito.when(integrationHelper.getUserGuidFromUid(Mockito.anyLong())).thenReturn(GUID);

		response = listingService.updateListing("1000", request, shServiceContext, i18nServiceContext);
		Assert.assertTrue("AUTHZ Mode with ID as a value of proxiedId should work", response.getErrors() == null);

		attrMap.put("proxiedId", GUID);

		response = listingService.updateListing("1000", request, shServiceContext, i18nServiceContext);
		Assert.assertTrue("AUTHZ Mode with GUID as a value of proxiedId should work", response.getErrors() == null);

	}

	@Test
	public void testCallHandleBadRequestException() throws Exception {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);

		ListingCreateProcess listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null,
				null);
		ListingError error = new ListingError(ErrorType.INPUTERROR, ErrorCode.DUPLICATE_EXTERNAL_LISTING_ID,
				"Error Message", "externalListingId");
		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenThrow(new ListingBusinessException(error));
		setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);

		try {
			ListingRequest request = new ListingRequest();
			ListingResponse response = listingService.updateListing("1000", request, shServiceContext,
					i18nServiceContext);

			Assert.fail("The call should have thrown SHBadRequestException");
		} catch (SHBadRequestException ex) {
		} catch (Throwable th) {
			Assert.fail("The call should have thrown SHBadRequestException and not regular Exception ");
		}
	}

	@Test
	public void testCallHandleOtherException() throws Exception {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);

		ListingCreateProcess listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null,
				null);
		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenThrow(new NullPointerException());
		setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);

		try {
			ListingRequest request = new ListingRequest();
			ListingResponse response = listingService.updateListing("1000", request, shServiceContext,
					i18nServiceContext);

			Assert.fail("The call should have thrown ListingBusinessException");
		} catch (SHSystemException ex) {
		} catch (Throwable th) {
			Assert.fail("The call should have thrown SHSystemException and not regular Exception ");
		}
	}

	/**
	 * 
	 * @return Result
	 * @throws Exception
	 */
	@Test
	public void validateContextNull() throws Exception {
		ListingRequest request = new ListingRequest();
		SHServiceContext tempServiceCtx = shServiceContext;
		tempServiceCtx.setExtendedSecurityContext(null);
		request.setBuyerSeesPerProduct(new com.stubhub.newplatform.common.entity.Money("10"));
		request.setFaceValue(new com.stubhub.newplatform.common.entity.Money("5"));
		request.setPurchasePrice(new com.stubhub.newplatform.common.entity.Money("5"));
		request.setDeliveryOption(DeliveryOption.BARCODE);
		request.setSplitOption(SplitOption.NOSINGLES);

		try {
			ListingResponse response = listingService.updateListing("1000", request, tempServiceCtx,
					i18nServiceContext);

			Assert.fail("Update listing with securityContext == null should fail with SHForbiddenException");
		} catch (SHForbiddenException ex) {
		}
	}

	@Test
	public void testCreateSingleListingError() throws Exception {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);

		ListingCreateProcess listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null,
				null);
		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenThrow(new NullPointerException());
		setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);

		try {
			ListingRequest request = new ListingRequest();
			ListingResponse response = listingService.createListing(request, shServiceContext, i18nServiceContext);

			Assert.fail("The call should have thrown en exception");
		} catch (SHSystemException ex) {
		} catch (Throwable th) {
			Assert.fail("The call should have thrown SHSystemException and not regular Exception ");
		}
	}

	@Test
	public void testCreateSingleListingSuccess() throws Exception {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);

		ListingCreateProcess listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null,
				null);
		ArrayList<ListingResponse> resps = new ArrayList<ListingResponse>();
		ListingResponse response1 = super.makeListingResponse("123456", "active");
		response1.setEventId("123456");
		response1.setSellerId(12345l);
		resps.add(response1);
		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(resps);
		setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);

		shServiceContext.setSHStore("2");

		ListingRequest request = new ListingRequest();
		request.setStatus(ListingStatus.ACTIVE);
		ListingResponse response = listingService.createListing(request, shServiceContext, i18nServiceContext);

		Assert.assertTrue(response.getId() != null);
	}

	@Test
	public void testCreateSingleListingListingBusinessException() throws Exception {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);

		ListingCreateProcess listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null,
				null);
		ArrayList<ListingResponse> resps = new ArrayList<ListingResponse>();
		resps.add(super.makeListingResponse("123456", "active"));

		ListingError le = new ListingError();
		le.setType(ErrorType.BUSINESSERROR);

		ListingBusinessException lbe = new ListingBusinessException(le);

		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenThrow(lbe);
		setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);

		shServiceContext.setSHStore("2");

		ListingRequest request = new ListingRequest();
		try {
			ListingResponse response = listingService.createListing(request, shServiceContext, i18nServiceContext);
			Assert.fail("should have thrown an exception");
		} catch (Exception e) {
			// Assert.assertTrue("SHBadRequestException should have been thrown", e
			// instanceof SHBadRequestException);
		}

	}

	@Test
	public void testCreateSingleListingListingRuntimeException() throws Exception {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);

		ListingCreateProcess listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null,
				null);
		ArrayList<ListingResponse> resps = new ArrayList<ListingResponse>();
		resps.add(super.makeListingResponse("123456", "active"));

		ListingError le = new ListingError();
		le.setType(ErrorType.BUSINESSERROR);

		SHRuntimeException lbe = new SHRuntimeException();

		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenThrow(lbe);
		setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);

		shServiceContext.setSHStore("2");

		ListingRequest request = new ListingRequest();
		try {
			ListingResponse response = listingService.createListing(request, shServiceContext, i18nServiceContext);
			Assert.fail("should have thrown an exception");
		} catch (Exception e) {
			// Assert.assertTrue("SHBadRequestException should have been thrown", e
			// instanceof SHBadRequestException);
		}

	}

	// @Test
	public void testCreateSingleListingAuthzSuccess() throws Exception {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);

		ListingCreateProcess listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null,
				null);
		ArrayList<ListingResponse> resps = new ArrayList<ListingResponse>();
		resps.add(super.makeListingResponse("123456", "active"));

		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(resps);
		setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);

		shServiceContext.setSHStore("2");

		Map<String, String> attrMap = new HashMap<String, String>();
		attrMap.put("role", "R2");
		attrMap.put("operatorId", "csbpmagent1");
		attrMap.put("proxiedId", "1000000051");

		shServiceContext.setAttributeMap(attrMap);

		ListingRequest request = new ListingRequest();
		ListingResponse response = listingService.createListing(request, shServiceContext, i18nServiceContext);

		Assert.assertTrue(response.getId() != null);
	}

	// @Test
	public void testCreateSingleListingWithInvalidShStore() throws Exception {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);

		ListingCreateProcess listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null,
				null);
		ArrayList<ListingResponse> resps = new ArrayList<ListingResponse>();
		resps.add(super.makeListingResponse("123456", "active"));

		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(resps);
		setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);

		shServiceContext.setSHStore("test");

		ListingRequest request = new ListingRequest();
		ListingResponse response = listingService.createListing(request, shServiceContext, i18nServiceContext);

		Assert.assertTrue(response.getId() != null);
	}

	// @Test
	public void testCreateSingleListingWithDefaultShStore() throws Exception {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);

		ListingCreateProcess listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null,
				null);
		ArrayList<ListingResponse> resps = new ArrayList<ListingResponse>();
		resps.add(super.makeListingResponse("123456", "active"));

		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(resps);
		setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);

		shServiceContext.setSHStore(null);

		ListingRequest request = new ListingRequest();
		ListingResponse response = listingService.createListing(request, shServiceContext, i18nServiceContext);

		Assert.assertTrue(response.getId() != null);
	}

	/**
	 * Relist with an empty request
	 * 
	 * @result RelistResponse
	 */
	@Test
	public void testRelist() {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);

		// RelistRequest r = new RelistRequest();
		RelistRequest r = getRelistRequestMockForTransfer();
		shServiceContext.setExtendedSecurityContext(securityContext);
		Mockito.when(transferValidator.getUserGuid(anyString())).thenReturn("USERGUID");
		RelistResponse response = listingService.relist(r, shServiceContext, i18nServiceContext);
		Assert.assertNotNull(response);
	}

	/**
	 * Relist with invalid seller
	 * 
	 * @result SHForbiddenException
	 */
	@Test
	public void testRelistInvalidSellerId() {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);

		RelistRequest r = new RelistRequest();
		shServiceContext.setExtendedSecurityContext(null);
		try {
			listingService.relist(r, shServiceContext, i18nServiceContext);
		} catch (SHForbiddenException e) {
			Assert.assertTrue(true);
		}
	}

	/**
	 * Relist with null relist request
	 * 
	 * @result RelistResponse
	 */
	@Test
	public void testNullRelistRequest() {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);
		shServiceContext.setExtendedSecurityContext(securityContext);
		RelistResponse response = listingService.relist(getRelistRequestMock(), shServiceContext, i18nServiceContext);
		Assert.assertNull(response.getListings());

	}

	/**
	 * Relist GA PDF Listing
	 * 
	 * @result RelistResponse
	 */
	// @Test
	public void testRelistGAPDFListing() throws Exception {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);

		RelistRequest r = getRelistRequestMock();
		shServiceContext.setExtendedSecurityContext(securityContext);
		setBeanProperty(listingService, "relistHelper", relistHelper);
		Mockito.when(relistHelper.validateListingWithOrderDetails((RelistRequest) Mockito.anyObject(), Mockito.anyMap(), Mockito.any(Locale.class)))
				.thenReturn(getOrderDetails());
		Mockito.when(relistHelper.createListingRequests(Mockito.any(RelistRequest.class), Mockito.anyMap(), Mockito.anyMap()))
				.thenReturn(getListingGAPDF());
		RelistResponse response = listingService.relist(getRelistRequestMock(), shServiceContext, i18nServiceContext);
		Assert.assertTrue(response.getListings() != null);

	}

	/**
	 * Relist for transfer to a friend
	 * 
	 * @result RelistResponse
	 */

	// @Test
	public void testRelistForTransfer() throws Exception {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);
		RelistRequest relistRequest = getRelistRequestMockForTransfer();
		shServiceContext.setExtendedSecurityContext(securityContext);
		setBeanProperty(listingService, "relistHelper", relistHelper);
		setBeanProperty(listingService, "inventoryMgr", inventoryMgr);
		Mockito.when(relistHelper.validateListingWithOrderDetails((RelistRequest) Mockito.anyObject(), Mockito.anyMap(), Mockito.any(Locale.class)))
		.thenReturn(getOrderDetails());
		Mockito.when(relistHelper.createListingRequests(Mockito.any(RelistRequest.class), Mockito.anyMap(), Mockito.anyMap()))
		.thenReturn(getListingGAPDF());
		List<RelistListingResponse> listResponse = getMockRelistResponse();
		RelistResponse relistResponse = new RelistResponse();
		relistResponse.setListings(listResponse);
		ListingResponse listingResponse = new ListingResponse();
		listingResponse.setId("1000");
		List<ListingResponse> resps = new ArrayList<ListingResponse>();
		resps.add(listingResponse);
		List<TicketSeat> ticketList = new ArrayList<>();
		ticketList.add(new TicketSeat());
		Listing listingInfo = new Listing();
		listingInfo.setId(100l);
		listingInfo.setSellerPaymentTypeId(2l);
		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(resps);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(listingInfo);
		Mockito.when(listingHelper.getTicketSeatsInfoByTicketId(anyString())).thenReturn(ticketList);
		Mockito.when(transferValidator.getUserGuid(anyString())).thenReturn("USERGUID");
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				return null;
			}
		}).when(jmsMessageHelper).sendShareWithFriendsMessage(anyLong(), anyList(), anyString(), anyString(),
				anyString(), anyString());
		RelistResponse response = listingService.relist(relistRequest, shServiceContext, i18nServiceContext);
		Assert.assertTrue(response.getListings() != null);
		Assert.assertTrue(response.getListings().size() > 0);
	}

	// @Test
	public void testRelistForTransfer_2ndCase() throws Exception {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);
		RelistRequest relistRequest = getRelistRequestMockForTransfer();
		shServiceContext.setExtendedSecurityContext(securityContext);
		setBeanProperty(listingService, "relistHelper", relistHelper);
		setBeanProperty(listingService, "inventoryMgr", inventoryMgr);
		Mockito.when(relistHelper.validateListingWithOrderDetails((RelistRequest) Mockito.anyObject(), Mockito.anyMap(), Mockito.any(Locale.class)))
		.thenReturn(getOrderDetails());
		Mockito.when(relistHelper.createListingRequests(Mockito.any(RelistRequest.class), Mockito.anyMap(), Mockito.anyMap()))
		.thenReturn(getListingGAPDF());
		List<RelistListingResponse> listResponse = getMockRelistResponse();
		RelistResponse relistResponse = new RelistResponse();
		relistResponse.setListings(listResponse);
		ListingResponse listingResponse = new ListingResponse();
		listingResponse.setId("1000");
		List<ListingResponse> resps = new ArrayList<ListingResponse>();
		resps.add(listingResponse);
		List<TicketSeat> ticketList = new ArrayList<>();
		ticketList.addAll(getTicketSeatForTransfer());
		Listing listingInfo = new Listing();
		listingInfo.setId(100l);
		listingInfo.setSellerPaymentTypeId(2l);
		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(resps);
		Mockito.when(listingHelper.getTicketSeatsInfoByTicketId(anyString())).thenReturn(ticketList);
		Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(listingInfo);
		Mockito.when(transferValidator.getUserGuid(anyString())).thenReturn("USERGUID");
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				return null;
			}
		}).when(jmsMessageHelper).sendShareWithFriendsMessage(anyLong(), anyList(), anyString(), anyString(),
				anyString(), anyString());
		RelistResponse response = listingService.relist(relistRequest, shServiceContext, i18nServiceContext);
		Assert.assertTrue(response.getListings() != null);
		Assert.assertTrue(response.getListings().size() > 0);
	}

	@Test
	public void testRelistForTransferExceptionCase() throws Exception {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);
		RelistRequest relistRequest = getRelistRequestMockForTransfer();
		shServiceContext.setExtendedSecurityContext(securityContext);
		setBeanProperty(listingService, "relistHelper", relistHelper);
		Mockito.when(relistHelper.validateListingWithOrderDetails((RelistRequest) Mockito.anyObject(), Mockito.anyMap(), Mockito.any(Locale.class)))
		.thenReturn(getOrderDetails());
		Mockito.when(relistHelper.createListingRequests(Mockito.any(RelistRequest.class), Mockito.anyMap(), Mockito.anyMap()))
		.thenReturn(getListingGAPDF());
		List<RelistListingResponse> listResponse = getMockRelistResponse();
		RelistResponse relistResponse = new RelistResponse();
		relistResponse.setListings(listResponse);
		List<ListingResponse> resps = new ArrayList<ListingResponse>();
		resps.add(new ListingResponse());
		List<TicketSeat> ticketList = new ArrayList<>();
		ticketList.addAll(getTicketSeatForTransfer());
		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(resps);
		Mockito.when(listingHelper.getTicketSeatsInfoByTicketId(anyString())).thenReturn(ticketList);
		Mockito.when(transferValidator.getUserGuid(anyString())).thenReturn(null);
		;
		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				return null;
			}
		}).when(jmsMessageHelper).sendShareWithFriendsMessage(anyLong(), anyList(), anyString(), anyString(),
				anyString(), anyString());
		try {
			RelistResponse response = listingService.relist(relistRequest, shServiceContext, i18nServiceContext);
		} catch (SHBadRequestException ex) {
			Assert.assertNotNull(ex);
		}
	}

	/**
	 * Relist GA PDF Listing
	 * 
	 * @throws SHBadRequestException
	 */
	@Test
	public void testRelistListingException() throws Exception {
		SHAPIContext ctx = new SHAPIContext();
		ctx.setSignedJWTAssertion("-------some-dummy-assertion----------");
		SHAPIThreadLocal.set(ctx);

		RelistRequest r = getRelistRequestMock();
		shServiceContext.setExtendedSecurityContext(securityContext);
		setBeanProperty(listingService, "relistHelper", relistHelper);
		Mockito.when(relistHelper.validateListingWithOrderDetails((RelistRequest) Mockito.anyObject(), Mockito.anyMap(), Mockito.any(Locale.class)))
		.thenReturn(getOrderDetails());
		Mockito.when(relistHelper.createListingRequests(Mockito.any(RelistRequest.class), Mockito.anyMap(), Mockito.anyMap()))
		.thenReturn(getListingGAPDF());
		try {
			RelistResponse response = listingService.relist(getRelistRequestMock(), shServiceContext,
					i18nServiceContext);
		} catch (SHBadRequestException ex) {
			Assert.assertNotNull(ex);
		}
	}

	// helper method to return orderdetails
	private Map<Long, List<OrderItem>> getOrderDetails() {
		Map<Long, List<OrderItem>> orderDetails = new HashMap<Long, List<OrderItem>>();
		orderDetails.put(12345L, new ArrayList<OrderItem>());
		OrderItem item = new OrderItem();
		item.setId(1000L);
		item.setSection("General Admission");
		item.setSaleStatus("Approved");
		orderDetails.get(12345L).add(item);
		return orderDetails;
	}

	// helper method to return orderdetails for transfer case
	private Map<Long, List<OrderItem>> getOrderDetailsForTransfer() {
		Map<Long, List<OrderItem>> orderDetails = new HashMap<Long, List<OrderItem>>();
		orderDetails.put(12345L, new ArrayList<OrderItem>());
		OrderItem item = new OrderItem();
		item.setId(1000L);
		item.setSection("General Admission");
		item.setSaleStatus("Approved");
		item.setRow("row");
		item.setSeat("seatNumber");
		orderDetails.get(12345L).add(item);
		return orderDetails;
	}

	private List<TicketSeat> getTicketSeatForTransfer() {
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setTicketSeatId(123l);
		ticketSeat.setSeatNumber("seatNumber");
		ticketSeat.setRow("row");
		ticketSeat.setSection("General Admission");
		List<TicketSeat> ticketSeatList = new ArrayList<>();
		ticketSeatList.add(ticketSeat);
		return ticketSeatList;

	}

	// Order with one GA PDF listing
	private List<ListingRequest> getListingGAPDF() {
		ListingRequest req = new ListingRequest();
		req.setEventId("190206");
		req.setPricePerProduct(new Money("65.00"));
		req.setSection("General Admission");
		req.setDeliveryOption(DeliveryOption.PDF);
		req.setQuantity(30);

		List<ListingRequest> list = new ArrayList<ListingRequest>();
		list.add(req);
		return list;
	}

	private RelistRequest getRelistRequestMock() {
		List<RelistListing> listOfListings = new ArrayList<RelistListing>();
		RelistRequest relistRequest = new RelistRequest();
		RelistListing relistListing = new RelistListing();
		relistListing.setOrderId(120916L);
		relistListing.setPricePerItem(new BigDecimal(23.50));
		listOfListings.add(relistListing);
		relistRequest.setListings(listOfListings);
		return relistRequest;
	}

	private RelistRequest getRelistRequestMockForTransfer() {
		List<RelistListing> listOfListings = new ArrayList<RelistListing>();
		RelistRequest relistRequest = new RelistRequest();
		RelistListing relistListing = new RelistListing();
		relistListing.setOrderId(12345L);
		relistListing.setPricePerItem(new BigDecimal(0.0));
		relistListing.setToEmailId("70@testmail.com");
		relistListing.setToCustomerGUID("6C21FF95408F3BC0E04400144FB7AAA6");
		listOfListings.add(relistListing);
		relistRequest.setListings(listOfListings);
		return relistRequest;
	}

	private List<RelistListingResponse> getMockRelistResponse() {
		RelistResponse relistResponse = new RelistResponse();
		RelistListingResponse response = new RelistListingResponse("", ListingStatus.HIDDEN);
		List<RelistListingResponse> listings = new ArrayList<>();
		listings.add(response);
		return listings;
	}

	@Test
	public void testGetListing() {
		MultivaluedMap<String, String> mvMap = mock(MultivaluedMap.class);
		Mockito.when(uriInfo.getQueryParameters()).thenReturn(mvMap);
		Mockito.when(mvMap.getFirst("status")).thenReturn("");

		List<Product> products = new ArrayList<Product>();
		Product product1 = new Product();
		products.add(product1);

		ListingResponse listingResponse = Mockito.mock(ListingResponse.class);
		Mockito.when(listingResponse.getProducts()).thenReturn(products);

		Mockito.when(listingHelper.populateListingDetails(Mockito.anyString(), Mockito.anyLong(),
				Mockito.any(Locale.class), Mockito.anyLong(), Mockito.any(SHServiceContext.class),Mockito.anyString()))
				.thenReturn(listingResponse);
		listingService.getListing("13243242342321", shServiceContext, i18nServiceContext);
	}

	@Test
	public void testGetListing_AuthZSecContext() {
		SHServiceContext tempServiceCtx = shServiceContext;
		tempServiceCtx.setExtendedSecurityContext(null);
		listingService.getListing("13243242342321", tempServiceCtx, i18nServiceContext);
	}

	@Test(expectedExceptions = { SHForbiddenException.class })
	public void testGetListing_NullSHServiceContext() {
		listingService.getListing("13243242342321", null, i18nServiceContext);
	}

	@Test(expectedExceptions = { SHForbiddenException.class })
	public void testGetListing_AuthZSecContextNull() {
		SHServiceContext tempServiceCtx = shServiceContext;
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_ROLE, null);
		tempServiceCtx.setExtendedSecurityContext(null);
		listingService.getListing("13243242342321", tempServiceCtx, i18nServiceContext);
	}

	@Test
	public void testListingPing() {
		ListingResponse response = listingService.listingPing();
		Assert.assertTrue(response != null);
	}

	@Test
	public void testCreateListing_AuthZWithGuid() {
		ListingCreateProcess listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null,
				null);
		ArrayList<ListingResponse> resps = new ArrayList<ListingResponse>();
		ListingResponse response = super.makeListingResponse("123456", "active");
		response.setEventId("123456");
		response.setSellerId(12345l);
		resps.add(response);

		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(resps);
		try {
			setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SHServiceContext tempServiceCtx = shServiceContext;
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_ROLE, "R2");
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_OPERATOR_ID, "csbpmagent1");
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_PROXIED_ID, "C77991557A315E14E04400212861B256");
		ListingRequest request = new ListingRequest();
		request.setStatus(ListingStatus.HIDDEN);
		securityContext = mockSecurityContext("1000");
		tempServiceCtx.setExtendedSecurityContext(securityContext);
		listingService.createListing(request, tempServiceCtx, i18nServiceContext);
	}

	// SELLAPI-3234
	@Test(expectedExceptions = { SHRuntimeException.class })
	public void testGetListing_NullListingId() {
		SHServiceContext tempServCtx = shServiceContext;
		tempServCtx.setAttribute(SHServiceContext.ATTR_ROLE, "R1");
		tempServCtx.setAttribute(SHServiceContext.ATTR_OPERATOR_ID, "");
		tempServCtx.setAttribute(SHServiceContext.ATTR_PROXIED_ID, "12345");
		listingService.getListing("", tempServCtx, i18nServiceContext);
	}

	@Test
	public void testCreateListing_AuthZWithUserId() {
		ListingCreateProcess listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null,
				null);
		ArrayList<ListingResponse> resps = new ArrayList<ListingResponse>();
		ListingResponse response = super.makeListingResponse("123456", "active");
		response.setEventId("123456");
		response.setSellerId(12345l);
		resps.add(response);
		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(resps);
		try {
			setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SHServiceContext tempServiceCtx = shServiceContext;
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_ROLE, "R3");
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_OPERATOR_ID, "csbpmagent1");
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_PROXIED_ID, "12345");
		ListingRequest request = new ListingRequest();
		request.setStatus(ListingStatus.HIDDEN);
		securityContext = mockSecurityContext("1000");
		tempServiceCtx.setExtendedSecurityContext(securityContext);
		listingService.createListing(request, tempServiceCtx, i18nServiceContext);
	}

	@Test
	public void testCreateListing_WithoutAuthZ() {
		ListingCreateProcess listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null,
				null);
		ArrayList<ListingResponse> resps = new ArrayList<ListingResponse>();
		ListingResponse response = super.makeListingResponse("123456", "active");
		response.setEventId("123456");
		response.setSellerId(12345l);
		resps.add(response);
		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(resps);
		try {
			setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SHServiceContext tempServiceCtx = shServiceContext;
		ListingRequest request = new ListingRequest();
		request.setStatus(ListingStatus.ACTIVE);
		securityContext = mockSecurityContext("1000");
		tempServiceCtx.setExtendedSecurityContext(securityContext);
		listingService.createListing(request, tempServiceCtx, i18nServiceContext);

	}

	@Test
	public void testCreateListing_WithAuthZEmptyOperatorId() {

		ListingCreateProcess listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null,
				null);
		ArrayList<ListingResponse> resps = new ArrayList<ListingResponse>();
		ListingResponse response = super.makeListingResponse("123456", "active");
		response.setEventId("123456");
		response.setSellerId(12345l);
		resps.add(response);
		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(resps);
		try {
			setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SHServiceContext tempServiceCtx = shServiceContext;
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_ROLE, "R1");
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_OPERATOR_ID, "");
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_PROXIED_ID, "12345");
		ListingRequest request = new ListingRequest();
		request.setStatus(ListingStatus.HIDDEN);
		securityContext = mockSecurityContext("1000");
		;
		tempServiceCtx.setExtendedSecurityContext(securityContext);
		listingService.createListing(request, tempServiceCtx, i18nServiceContext);
	}

	@Test
	public void testCreateListing_WithAuthZEmptyProxiedId() {
		ListingCreateProcess listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null,
				null);
		ArrayList<ListingResponse> resps = new ArrayList<ListingResponse>();
		ListingResponse response = super.makeListingResponse("123456", "active");
		response.setEventId("123456");
		response.setSellerId(12345l);
		resps.add(response);
		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(resps);
		try {
			setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SHServiceContext tempServiceCtx = shServiceContext;
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_ROLE, "R2");
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_OPERATOR_ID, "csbpmagent1");
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_PROXIED_ID, "");
		ListingRequest request = new ListingRequest();
		request.setStatus(ListingStatus.HIDDEN);
		securityContext = mockSecurityContext("1000");
		;
		tempServiceCtx.setExtendedSecurityContext(securityContext);
		listingService.createListing(request, tempServiceCtx, i18nServiceContext);
	}

	@Test
	public void testCreateListing_WithAuthZEmptyRole() {
		ListingCreateProcess listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null,
				null);
		ArrayList<ListingResponse> resps = new ArrayList<ListingResponse>();
		ListingResponse response = super.makeListingResponse("123456", "active");
		response.setEventId("123456");
		response.setSellerId(12345l);
		resps.add(response);
		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(resps);
		try {
			setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SHServiceContext tempServiceCtx = shServiceContext;
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_ROLE, "");
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_OPERATOR_ID, "csbpmagent1");
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_PROXIED_ID, "12345");
		ListingRequest request = new ListingRequest();
		request.setStatus(ListingStatus.HIDDEN);
		securityContext = mockSecurityContext("1000");
		;
		tempServiceCtx.setExtendedSecurityContext(securityContext);
		listingService.createListing(request, tempServiceCtx, i18nServiceContext);
	}

	@Test
	public void testCreateListing_WithAuthZInvalidRole() {
		ListingCreateProcess listingCreateProcess = (ListingCreateProcess) mockClass(ListingCreateProcess.class, null,
				null);
		ArrayList<ListingResponse> resps = new ArrayList<ListingResponse>();
		ListingResponse response = super.makeListingResponse("123456", "active");
		response.setEventId("123456");
		response.setSellerId(12345l);
		resps.add(response);
		Mockito.when(listingCreateProcess.createOrUpdateListings(Mockito.any(BulkListingInternal.class),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean()))
				.thenReturn(resps);
		try {
			setBeanProperty(listingService, "listingCreateProcess", listingCreateProcess);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SHServiceContext tempServiceCtx = shServiceContext;
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_ROLE, "R1");
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_OPERATOR_ID, "csbpmagent1");
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_PROXIED_ID, "12345");
		ListingRequest request = new ListingRequest();
		request.setStatus(ListingStatus.HIDDEN);
		securityContext = mockSecurityContext("1000");
		;
		tempServiceCtx.setExtendedSecurityContext(securityContext);
		listingService.createListing(request, tempServiceCtx, i18nServiceContext);
	}

	@Test
	public void testCreateListing_WithServiceContextNull() {
		ListingRequest request = new ListingRequest();
		request.setStatus(ListingStatus.ACTIVE);
		SHServiceContext tempSHServiceCtx = Mockito.mock(SHServiceContext.class);
		Mockito.when(tempSHServiceCtx.getExtendedSecurityContext()).thenReturn(null);
		try {
			listingService.createListing(request, tempSHServiceCtx, i18nServiceContext);
		} catch (Exception ex) {
			Assert.assertNotNull(ex);
		}

	}

	@Test
	public void testCreateListing_WithServiceContext() {
		ListingRequest request = new ListingRequest();
		request.setStatus(ListingStatus.ACTIVE);
		SHServiceContext tempSHServiceCtx = Mockito.mock(SHServiceContext.class);
		Mockito.when(tempSHServiceCtx.getExtendedSecurityContext()).thenReturn(null);
		try {
			listingService.createListing(request, tempSHServiceCtx, i18nServiceContext);
		} catch (Exception ex) {
			Assert.assertNotNull(ex);
		}

	}

	@Test
	public void testCreateListing_AuthZWithGuidAndApp() {
		SHServiceContext tempServiceCtx = shServiceContext;
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_ROLE, "R2");
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_OPERATOR_ID, "csbpmagent1");
		tempServiceCtx.setAttribute(SHServiceContext.ATTR_PROXIED_ID, "C77991557A315E14E04400212861B256");
		ListingRequest request = new ListingRequest();
		request.setStatus(ListingStatus.HIDDEN);
		securityContext = mockSecurityContextWithAppName("1000");
		tempServiceCtx.setExtendedSecurityContext(securityContext);
		try {
			listingService.createListing(request, tempServiceCtx, i18nServiceContext);
		} catch (Exception ex) {
			Assert.assertNotNull(ex);
		}

	}

}
