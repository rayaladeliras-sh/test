package com.stubhub.domain.inventory.listings.v2.helper;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.client.ResponseReader;
import org.apache.cxf.jaxrs.client.WebClient;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.infrastructure.common.core.concurrent.SHThreadLocals;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.biz.v2.intf.IntegrationManager;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.PaymentType;
import com.stubhub.domain.inventory.listings.v2.entity.Address;
import com.stubhub.domain.inventory.listings.v2.entity.UserContact;
import com.stubhub.domain.inventory.listings.v2.util.BusinessHelper;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.domain.inventory.v2.DTO.ProSellerInfo;
import com.stubhub.domain.payment.intf.payableinstrument.v1.PayableInstrumentType;
import com.stubhub.domain.payment.intf.payableinstrument.v1.PayableInstrumentTypeResponse;
import com.stubhub.domain.user.contacts.intf.CustomerContactDetails;
import com.stubhub.domain.user.contacts.intf.CustomerContactMappingResponse;
import com.stubhub.domain.user.contactsV2.intf.CustomerContactV2Details;
import com.stubhub.domain.user.intf.GetCustomerResponse;
import com.stubhub.domain.user.payments.intf.CheckDetails;
import com.stubhub.domain.user.payments.intf.CreateCustomerPaymentIdResponse;
import com.stubhub.domain.user.payments.intf.CreditCardDetails;
import com.stubhub.domain.user.payments.intf.CreditCardDetails.ExpDate;
import com.stubhub.domain.user.payments.intf.CustomerPaymentInstrumentDetails;
import com.stubhub.domain.user.payments.intf.CustomerPaymentInstrumentMappingsResponse;
import com.stubhub.domain.user.payments.intf.CustomerPaymentInstrumentResponse;
import com.stubhub.domain.user.payments.intf.CustomerPaymentInstrumentsResponse;
import com.stubhub.domain.user.payments.intf.PayPalDetails;
import com.stubhub.domain.user.payments.intf.PayableDetails;
import com.stubhub.domain.user.payments.v2.intf.CustomerPaymentInstrumentDetailsV2;
import com.stubhub.domain.user.payments.v2.intf.CustomerPaymentInstrumentMappingsResponseV2;
import com.stubhub.domain.user.payments.v2.intf.CustomerPaymentInstrumentsResponseV2;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

import junit.framework.Assert;

public class UserHelperTest {
	
	private static final Log log = LogFactory.getLog(UserHelperTest.class);
	
	private UserHelper userHelper;
	private SvcLocator svcLocator;
	private WebClient webClient;
	private IntegrationManager integrationManager;
	private BusinessHelper businessHelper;
	MasterStubhubPropertiesWrapper masterStubhubProperties = null;
	
	private static final String CUSTOMER_CONTACT_V2_GET_API_URL = "usercontact.v2.get.api.url";
	private static final String CUSTOMER_CONTACT_V2_GET_API_URL_DEFAULT = "https://api.stubcloudprod.com/user/customers/v1/{userGuid}/contactsV2/{contactGuid}";
	private static final String CONTACT_MAPPING_API_URL = "usercontact.v1.api.getcontactId.url";
	private static final String CONTACT_MAPPING_API_URL_DEFAULT="https://api.stubcloudprod.com/user/customers/v1/{userGuid}/contactMapping/{contactGuid}";
	private static final String CONTACT_MAPPING_INVERSE_API_URL = "usercontact.v1.api.getcontactGuid.url";
	private static final String CONTACT_MAPPING_INVERSE_API_URL_DEFAULT="https://api.stubcloudprod.com/user/customers/v1/{userGuid}/contactMapping?internalId={internalId}";

	@BeforeMethod
	public void setUp(){ 
		userHelper = new UserHelper() {
			protected String getProperty(String propertyName, String defaultValue) {
				if ("userdefaultcontact.api.url".equals(propertyName))
					return "https://api.srwd34.com/user/customers/v1/";
				if ("usercc.api.url".equals(propertyName))
					return "https://api.srwd34.com/user/customers/v1/{userId}/paymentMethods/{paymentmethodid}";
				if ("usercontact.api.url".equals(propertyName))
					return "https://api.srwd34.com/user/customers/v1/{userId}/contacts/{contactId}";
				if ("getallsellerpayment.api.url".equals(propertyName))
					return "https://api.srwd34.com/user/customers/v1/{sellerId}/paymentInstruments";
				if ("getallsellerpayment.api.url.v2".equals(propertyName))
					return "https://api.slcd010.com/user/customers/v1/{sellerId}/paymentInstruments";
				if(CUSTOMER_CONTACT_V2_GET_API_URL.equals(propertyName))
					return CUSTOMER_CONTACT_V2_GET_API_URL_DEFAULT;
				if (CONTACT_MAPPING_API_URL.equals(propertyName))
					return CONTACT_MAPPING_API_URL_DEFAULT;
				if(CONTACT_MAPPING_INVERSE_API_URL.equals(propertyName))
					return CONTACT_MAPPING_INVERSE_API_URL_DEFAULT;

				return "";
			}
		};
		svcLocator = Mockito.mock(SvcLocator.class);	
		webClient = Mockito.mock(WebClient.class);
		integrationManager = Mockito.mock(IntegrationManager.class);
		businessHelper = Mockito.mock(BusinessHelper.class);
		masterStubhubProperties = Mockito.mock(MasterStubhubPropertiesWrapper.class);
		ReflectionTestUtils.setField(userHelper, "svcLocator", svcLocator);
		ReflectionTestUtils.setField(userHelper, "masterStubhubProperties",
				masterStubhubProperties);
		ReflectionTestUtils.setField(userHelper, "integrationManager", integrationManager);
		ReflectionTestUtils.setField(userHelper, "businessHelper", businessHelper);
	}
	

	
	@Test
	public void testGetDefaultUserContact () {
        Mockito.when(webClient.get()).thenReturn(getResponse("GetCustomerResponse"));
        Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		UserContact userContact = userHelper.getDefaultUserContact(1L);
		Assert.assertNotNull(userContact);
        Address address = userContact.getAddress();
		Assert.assertNotNull(address);
		Assert.assertNotNull(address.getCity());
		Assert.assertNotNull(address.getCountry()); 
		Assert.assertNotNull(address.getState());
		Assert.assertNotNull(address.getStreetAddress1());
		Assert.assertNotNull(address.getStreetAddress2());
		Assert.assertNotNull(address.getZip());
		Assert.assertNotNull(userContact.getEmail());
		Assert.assertNotNull(userContact.getFirstName());
		Assert.assertNotNull(userContact.getLastName());
		Assert.assertNotNull(userContact.getPhoneExt());
		Assert.assertNotNull(userContact.getPhoneNumber());
		Assert.assertNotNull(userContact.getId());
		Assert.assertNotNull(userContact.getIsDefault());
	}
	

	@Test
	public void test_isSellerContactIdActive() throws Exception {
        Mockito.when(webClient.get()).thenReturn(getResponse("CustomerContactDetails"));
		String userGuid = "AB123";
		Long contactId = 10001L;
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(GetCustomerResponse.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		CustomerContactDetails customerContactDetails = new CustomerContactDetails ();
		customerContactDetails.setId(10001L);
		Mockito.when(webClient.get()).thenReturn(getResponseForError());
		Boolean check = userHelper.isSellerContactValid(userGuid, contactId);
		Assert.assertNotNull(check);
		Assert.assertEquals(check, new Boolean(false));
	}
	
	@Test
	public void test_getContactIdActive() throws Exception {
        Mockito.when(webClient.get()).thenReturn(getResponse("CustomerContactDetails"));
		Listing listing = new Listing();
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(GetCustomerResponse.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		//TODO - BUG, please make these integration tests to junits.
		//CustomerContactDetails check = userHelper.getUserContact("AB123", 10001L);
	}
	
	@Test
	public void test_isSellerPaymentContactIdPopulated4Paypal() throws Exception {
        Mockito.when(webClient.get()).thenReturn(getResponse("CustomerContactDetails"));

		String sellerGuid  = "10001";
		Long paymentTypeId = PaymentType.Paypal.getType();
		List<CustomerPaymentInstrumentDetailsV2> sellerPaymentInstruments = getPaymentInstruments();
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(GetCustomerResponse.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Boolean check = userHelper.isSellerPaymentContactIdPopulated(sellerGuid,paymentTypeId, sellerPaymentInstruments);
		Assert.assertNotNull(check);
		Assert.assertEquals(check, new Boolean(true));
	}
	@Test
	public void test_isSellerPaymentContactIdPopulated4ACH() throws Exception {
        Mockito.when(webClient.get()).thenReturn(getResponse("CustomerContactDetails"));
		String sellerGuid  = "10001";
		Long paymentTypeId = PaymentType.ACH.getType();
		List<CustomerPaymentInstrumentDetailsV2> sellerPaymentInstruments = getPaymentInstruments();
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(GetCustomerResponse.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Boolean check = userHelper.isSellerPaymentContactIdPopulated(sellerGuid,paymentTypeId, sellerPaymentInstruments);
		Assert.assertNotNull(check);
		Assert.assertEquals(check, new Boolean(true));
	}
	@Test
	public void test_isSellerPaymentContactIdPopulated4Check() throws Exception {
        Mockito.when(webClient.get()).thenReturn(getResponse("CustomerContactDetails"));
		String sellerGuid  = "10001";
		Long paymentTypeId = PaymentType.Check.getType();
		List<CustomerPaymentInstrumentDetailsV2> sellerPaymentInstruments = getPaymentInstruments();
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(GetCustomerResponse.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Boolean check = userHelper.isSellerPaymentContactIdPopulated(sellerGuid,paymentTypeId, sellerPaymentInstruments);
		Assert.assertNotNull(check);
		Assert.assertEquals(check, new Boolean(true));
	}
	@Test
	public void test_isSellerPaymentContactIdPopulated4SeasonTicketAccount() throws Exception {
        Mockito.when(webClient.get()).thenReturn(getResponse("CustomerContactDetails"));
		String sellerGuid  = "10001";
		Long paymentTypeId = PaymentType.SeasonTicketAccount.getType();
		List<CustomerPaymentInstrumentDetailsV2> sellerPaymentInstruments = getPaymentInstruments();
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(GetCustomerResponse.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Boolean check = userHelper.isSellerPaymentContactIdPopulated(sellerGuid,paymentTypeId, sellerPaymentInstruments);
		Assert.assertNotNull(check);
		Assert.assertEquals(check, new Boolean(true));
	}
	@Test
	public void test_isSellerPaymentContactIdPopulated4CreditCard() throws Exception {
        Mockito.when(webClient.get()).thenReturn(getResponse("CustomerContactDetails"));
		String sellerGuid  = "10001";
		Long paymentTypeId = PaymentType.CreditCard.getType();
		List<CustomerPaymentInstrumentDetailsV2> sellerPaymentInstruments = getPaymentInstruments();
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(GetCustomerResponse.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Boolean check = userHelper.isSellerPaymentContactIdPopulated(sellerGuid,paymentTypeId, sellerPaymentInstruments);
		Assert.assertNotNull(check);
		Assert.assertEquals(check, new Boolean(true));
	}
	@Test
	public void test_isSellerPaymentContactIdPopulated4LargeSellerCheck() throws Exception {
        Mockito.when(webClient.get()).thenReturn(getResponse("CustomerContactDetails"));
		String sellerGuid  = "10001";
		Long paymentTypeId = PaymentType.LargeSellerCheck.getType();
		List<CustomerPaymentInstrumentDetailsV2> sellerPaymentInstruments = getPaymentInstruments();
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(GetCustomerResponse.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Boolean check = userHelper.isSellerPaymentContactIdPopulated(sellerGuid,paymentTypeId, sellerPaymentInstruments);
		Assert.assertNotNull(check);
		Assert.assertEquals(check, new Boolean(true));
	}
	@Test
	public void test_isSellerPaymentContactIdPopulated4Charity() throws Exception {
        Mockito.when(webClient.get()).thenReturn(getResponse("CustomerContactDetails"));
		String sellerGuid  = "10001";
		Long paymentTypeId = PaymentType.Charity.getType();
		List<CustomerPaymentInstrumentDetailsV2> sellerPaymentInstruments = getPaymentInstruments();
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(GetCustomerResponse.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Boolean check = userHelper.isSellerPaymentContactIdPopulated(sellerGuid,paymentTypeId, sellerPaymentInstruments);
		Assert.assertNotNull(check);
		Assert.assertEquals(check, new Boolean(true));
	}
	private List<CustomerPaymentInstrumentDetailsV2> getPaymentInstruments(){
		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		CustomerPaymentInstrumentDetailsV2 details = new CustomerPaymentInstrumentDetailsV2();
		details.setBookOfBusinessId("1");
		details.setId("1002");
		details.setPaymentType("check");
		details.setDefaultPaymentInd("y");
		CheckDetails check = new CheckDetails();
		check.setPayByCompanyName("Stubhub");
		details.setCheckDetails(check);
		paymentInstruments.add(details);
		
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setBookOfBusinessId("1");
		details.setId("1001");
		details.setPaymentType("largeSellerCheck");
		details.setDefaultPaymentInd("n");
		check = new CheckDetails();
		check.setPayByCompanyName("Stubhub");
		details.setCheckDetails(check);
		paymentInstruments.add(details);
		
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setBookOfBusinessId("1");
		details.setId("1003"); 
		details.setPaymentType("Paypal");
		details.setDefaultPaymentInd("n");
		PayPalDetails paypalDetails = new PayPalDetails();
		paypalDetails.setMode("Payable");
		paypalDetails.setEmailAddress("test@test.com");
		details.setPaypalDetails(paypalDetails);
		paymentInstruments.add(details);
		
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setBookOfBusinessId("1");
		details.setId("1004"); 
		details.setPaymentType("ACH");
		details.setDefaultPaymentInd("n");
		PayableDetails payableDetails=new PayableDetails();
		payableDetails.setBankName("testbank");
		payableDetails.setExternalPaymentInstrumentToken("testid");
		payableDetails.setLastFourDigits("1234");
		details.setPayableDetails(payableDetails);
		paymentInstruments.add(details);
		
	return paymentInstruments;
	}
	
	
	@Test
	public void test_isSellerPaymentTypeValid() throws Exception{
		Long sellerId = 10001L;
		Long eventId= 111111L;
		Long paymentTypeId = 1L;

		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(PayableInstrumentTypeResponse.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getResponse("PayableInstrumentTypeResponse"));
		Boolean check = userHelper.isSellerPaymentTypeValid(sellerId,eventId,paymentTypeId);
		Assert.assertTrue( check!=null && check==true );
	}
	
	@Test
	public void getAllSellerPaymentInstrumentV2Test(){
		String sellerGuId  = "10001abcde";		
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(CustomerPaymentInstrumentsResponseV2.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);	    	  
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		
		Mockito.when(webClient.get()).thenReturn(getResponse("customerPaymentInstrumentsResponseV2"));
		List<CustomerPaymentInstrumentDetailsV2> customerPaymentInstrumentDetailsList = userHelper.getAllSellerPaymentInstrumentV2(sellerGuId);
		Assert.assertNotNull(customerPaymentInstrumentDetailsList);	
	}
	
	@Test
	public void getCcGuidTest(){
		String userId  = "10001";
		String ccGuid = "abdsc";
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(CustomerPaymentInstrumentsResponseV2.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getResponse("CustomerPaymentInstrumentMappingsResponseV2"));
		CustomerPaymentInstrumentMappingsResponseV2 customerPaymentInstrumentMappingsResponse = userHelper.getCCid(userId, ccGuid);
		Assert.assertNotNull(customerPaymentInstrumentMappingsResponse);
	}
	
	@Test
	public void getCcGuidTest_NotFoundResponse(){
		String userId  = "10001";
		String ccGuid = "abdsc";
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(CustomerPaymentInstrumentsResponseV2.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(Response.status(Status.NOT_FOUND).build());
		CustomerPaymentInstrumentMappingsResponseV2 customerPaymentInstrumentMappingsResponse = userHelper.getCCid(userId, ccGuid);
		Assert.assertNull(customerPaymentInstrumentMappingsResponse);
	}
	
	@Test
	public void getCcGuidTest_OtherResponse(){
		String userId  = "10001";
		String ccGuid = "abdsc";
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(CustomerPaymentInstrumentsResponseV2.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(Response.status(Status.INTERNAL_SERVER_ERROR).build());
		CustomerPaymentInstrumentMappingsResponseV2 customerPaymentInstrumentMappingsResponse = userHelper.getCCid(userId, ccGuid);
		Assert.assertNull(customerPaymentInstrumentMappingsResponse);
	}
	
	@Test
	public void getHiddenListingCCidTest(){
		String userId  = "10001";
		String ccGuid = "abdsc";
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(CustomerPaymentInstrumentsResponseV2.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
	    SHServiceContext ctx = new SHServiceContext();
	    Map<String, String> attributeMap = new HashMap<String, String>();
	    ctx.setAttributeMap(attributeMap);
	    attributeMap.put(SHServiceContext.ATTR_PROXIED_ID, "123XYZ");
	    attributeMap.put(SHServiceContext.ATTR_OPERATOR_ID, "operatorId");
	    attributeMap.put(SHServiceContext.ATTR_ROLE, "R2");
	    SHThreadLocals.set(SHServiceContext.SERVICE_CONTEXT_HEADER, ctx);
	   // SHThreadLocals shThreadLocals = new SHThreadLocals();
	  //  SHThreadLocals.set("SHServiceContext.SERVICE_CONTEXT_HEADER", "role=R2, proxiedId=1000000054, operatorId=csbpmagent1}");
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
	//	Mockito.when((SHServiceContext)SHThreadLocals.get(Mockito.anyString())).thenReturn(shServiceContext);
		Mockito.when(webClient.get()).thenReturn(getResponse("CustomerPaymentInstrumentMappingsResponseV2"));
		CustomerPaymentInstrumentMappingsResponseV2 customerPaymentInstrumentMappingsResponse = userHelper.getHiddenListingCCid(userId, ccGuid);
		Assert.assertNotNull(customerPaymentInstrumentMappingsResponse);
	}
	
	
	
	@Test
	public void getHiddenListingCCidTest_NotFoundResponse(){
		String userId  = "10001";
		String ccGuid = "abdsc";
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(CustomerPaymentInstrumentsResponseV2.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(Response.status(Status.NOT_FOUND).build());
		CustomerPaymentInstrumentMappingsResponseV2 customerPaymentInstrumentMappingsResponse = userHelper.getHiddenListingCCid(userId, ccGuid);
		Assert.assertNull(customerPaymentInstrumentMappingsResponse);
	}
	@Test
	public void getHiddenListingCCidTest_OtherResponse(){
		String userId  = "10001";
		String ccGuid = "abdsc";
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(CustomerPaymentInstrumentsResponseV2.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(Response.status(Status.INTERNAL_SERVER_ERROR).build());
		CustomerPaymentInstrumentMappingsResponseV2 customerPaymentInstrumentMappingsResponse = userHelper.getHiddenListingCCid(userId, ccGuid);
		Assert.assertNull(customerPaymentInstrumentMappingsResponse);
	}
	
	 @Test
	public void testGetUserCC(){
         Mockito.when(webClient.get()).thenReturn(getResponse("CustomerContactDetails"));
		String userId  = "10001";
		Long ccId = 2L;
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(CustomerPaymentInstrumentsResponse.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);	    	  
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getResponse("CustomerPaymentInstrumentDetails"));
		CustomerPaymentInstrumentDetails customerPaymentInstrumentDetails = userHelper.getUserCC(userId, ccId);
		Assert.assertNotNull(customerPaymentInstrumentDetails);
	}
	 
	 @Test
		public void testGetUserCC_NotFoundResponse(){
         Mockito.when(webClient.get()).thenReturn(getResponse("CustomerContactDetails"));
			String userId  = "10001";
			Long ccId = 2L;
			ResponseReader reader = new ResponseReader();
		    reader.setEntityClass(CustomerPaymentInstrumentsResponse.class);
		    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
		    responseReader.add(reader);	    	  
			Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
			Mockito.when(webClient.get()).thenReturn(Response.status(Status.NOT_FOUND).build());
			CustomerPaymentInstrumentDetails customerPaymentInstrumentDetails = userHelper.getUserCC(userId, ccId);
			Assert.assertNull(customerPaymentInstrumentDetails);
		}
	 
	 @Test
		public void testGetUserCC_NullResponse(){
         Mockito.when(webClient.get()).thenReturn(getResponse("CustomerContactDetails"));
			String userId  = "10001";
			Long ccId = 2L;
			ResponseReader reader = new ResponseReader();
		    reader.setEntityClass(CustomerPaymentInstrumentsResponse.class);
		    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
		    responseReader.add(reader);	    	  
			Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
			Mockito.when(webClient.get()).thenReturn(Response.status(Status.NOT_ACCEPTABLE).build());
			CustomerPaymentInstrumentDetails customerPaymentInstrumentDetails = userHelper.getUserCC(userId, ccId);
			Assert.assertNull(customerPaymentInstrumentDetails);
		}
	
	 
	@Test
	public void testGetPid(){
        Mockito.when(webClient.get()).thenReturn(getResponse("CustomerContactDetails"));
		String userId  = "10001";
		Long ccId = 2L;
		String mode = "mode";
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(CustomerPaymentInstrumentsResponse.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(getResponse("CustomerPaymentInstrumentMappingsResponse"));		
		CustomerPaymentInstrumentMappingsResponse customerPaymentInstrumentMappingResponse = userHelper.getPid(userId, ccId, mode);
		Assert.assertNotNull(customerPaymentInstrumentMappingResponse);
	}
	
	@Test
	public void testGetPid_NotFoundResponse(){
        Mockito.when(webClient.get()).thenReturn(getResponse("CustomerContactDetails"));
		String userId  = "10001";
		Long ccId = 2L;
		String mode = "mode";
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(CustomerPaymentInstrumentsResponse.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(Response.status(Status.NOT_FOUND).build());		
		CustomerPaymentInstrumentMappingsResponse customerPaymentInstrumentMappingResponse = userHelper.getPid(userId, ccId, mode);
		Assert.assertNull(customerPaymentInstrumentMappingResponse);
	}
	
	@Test
	public void testGetPid_InvalidResponse(){
        Mockito.when(webClient.get()).thenReturn(getResponse("CustomerContactDetails"));
		String userId  = "10001";
		Long ccId = 2L;
		String mode = "mode";
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(CustomerPaymentInstrumentsResponse.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Mockito.when(webClient.get()).thenReturn(Response.status(Status.NOT_ACCEPTABLE).build());		
		CustomerPaymentInstrumentMappingsResponse customerPaymentInstrumentMappingResponse = userHelper.getPid(userId, ccId, mode);
		Assert.assertNull(customerPaymentInstrumentMappingResponse);
	}
	
	@Test
	public void test_getUserContact() throws Exception {
		String userGuid = "AB123";
		Long contactId = 10001L;
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(GetCustomerResponse.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);
		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		CustomerContactDetails customerContactDetails = new CustomerContactDetails ();
		customerContactDetails.setId(10001L);
		
		Mockito.when(webClient.get()).thenReturn(getResponse("contact"));
		CustomerContactDetails contact = userHelper.getUserContact(userGuid, contactId);
		Assert.assertNotNull(contact);
		
		Mockito.when(webClient.get()).thenReturn(getResponseForError());
		contact = userHelper.getUserContact(userGuid, contactId);
		Assert.assertNull(contact);
	}	
	
	
	@Test
	public void getMappedValidSellerCCIdV2Test(){
		UserHelper objUserHelper = Mockito.mock(UserHelper.class);	
		String sellerGuId  = "10001";
		String userId  = "1000000049";
		String ccGuid = "C77991557A2C5E14E04400212861B256";
		//String listingStatus = "HIDDEN";
		ResponseReader reader = new ResponseReader();
	    reader.setEntityClass(CustomerPaymentInstrumentsResponseV2.class);
	    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
	    responseReader.add(reader);	    	  
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);		
		Mockito.when(webClient.get()).thenReturn(getResponse("customerPaymentInstrumentsResponseV2"));
		String pid = "HDZVpeKacslTEASm";
		
		 List<CustomerPaymentInstrumentDetailsV2> customerPaymentInstrumentDetailsList = getPaymentInstrumentDetails();
		 getHiddenListingCCidTest();

		 Long sellerCCId = userHelper.getMappedValidSellerCCId(sellerGuId, pid, customerPaymentInstrumentDetailsList,"HIDDEN");
		 Assert.assertNull(sellerCCId);	
		 getCcGuidTest();

		 sellerCCId = userHelper.getMappedValidSellerCCId(sellerGuId, pid, customerPaymentInstrumentDetailsList,"");
		 Assert.assertNull(sellerCCId);	
		
		 getHiddenListingCCidTest_NotFoundResponse();
		 sellerCCId = userHelper.getMappedValidSellerCCId(sellerGuId, pid, customerPaymentInstrumentDetailsList,"HIDDEN");
		 Assert.assertNull(sellerCCId);	
		 
		 SHServiceContext context = (SHServiceContext)SHThreadLocals.get(SHServiceContext.SERVICE_CONTEXT_HEADER);
		 SHThreadLocals.remove(SHServiceContext.SERVICE_CONTEXT_HEADER);
		 
		 Long sellerCCIdHidden = userHelper.getMappedValidSellerCCId(sellerGuId, pid, customerPaymentInstrumentDetailsList,"HIDDEN");
		 Assert.assertNull(sellerCCId);
		 SHThreadLocals.set(SHServiceContext.SERVICE_CONTEXT_HEADER,context);

	}
	
	private List<CustomerPaymentInstrumentDetailsV2> getPaymentInstrumentDetails(){
		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		CustomerPaymentInstrumentDetailsV2 details = new CustomerPaymentInstrumentDetailsV2();
		CreditCardDetails  cardDetails = new CreditCardDetails();
			ExpDate expirationDate = new ExpDate();
			expirationDate.setMonth("10");
			expirationDate.setYear("2020");
			cardDetails.setExpirationDate(expirationDate);
		details.setBookOfBusinessId("1");
		details.setId("HDZVpeKacslTEASm");
		details.setCardDetails(cardDetails);
		details.setPaymentType("check");
		details.setDefaultPaymentInd("y");
		CheckDetails check = new CheckDetails();
		check.setPayByCompanyName("Stubhub");
		details.setCheckDetails(check);
		paymentInstruments.add(details);
		
		return paymentInstruments;
	}
	
	private boolean isCCValid(ExpDate expirationDate) {
		Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		if(today.get(Calendar.YEAR) < Integer.parseInt(expirationDate.getYear())) {
			return true;
		} else if(today.get(Calendar.YEAR) == Integer.parseInt(expirationDate.getYear()) && (today.get(Calendar.MONTH) + 1) <= Integer.parseInt(expirationDate.getMonth())) {
			return true;
		}
		return false;
	}
	
	private Response getResponse(final String entityName) {
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
				if(entityName.equalsIgnoreCase("CreateCustomerPaymentIdResponse")){
					CreateCustomerPaymentIdResponse res = new CreateCustomerPaymentIdResponse();
					res.setId("123");
					return res;
				}else if(entityName.equalsIgnoreCase("CustomerPaymentInstrumentMappingsResponse")){
					CustomerPaymentInstrumentMappingsResponse res = new CustomerPaymentInstrumentMappingsResponse();
					res.setId("123");
					res.setInternalId("111111"); 
					return res;
				}else if(entityName.equalsIgnoreCase("CustomerPaymentInstrumentMappingsResponseV2")){
					return new CustomerPaymentInstrumentMappingsResponseV2();
				}else if(entityName.equalsIgnoreCase("CustomerPaymentInstrumentDetails")){
					return new CustomerPaymentInstrumentDetails();
				}else if(entityName.equalsIgnoreCase("CustomerContactDetails")){
					return new CustomerContactDetails();
				}else if(entityName.equalsIgnoreCase("CustomerPaymentInstrumentResponse")){
					CustomerPaymentInstrumentResponse res = new CustomerPaymentInstrumentResponse();
					res.setId(123L);
					return res;					
				}else if(entityName.equalsIgnoreCase("CustomerPaymentInstrumentsResponse")){
					CustomerPaymentInstrumentsResponse res = new CustomerPaymentInstrumentsResponse();				
    				return res;
				}else if(entityName.equalsIgnoreCase("CustomerPaymentInstrumentsResponseV2")){
    				String responseString = "{\"paymentInstrumentv2\":{\"paymentInstruments\":[{\"id\":\"HDZVpeKacslTEASm\",\"defaultPaymentInd\":\"false\","
    					+ "\"paymentType\":\"creditcard\",\"cardDetails\":{\"cardType\":\"1\",\"expirationDate\":{\"month\":\"3\",\"year\":\"2015\"}," +
    					"\"lastFourDigits\":\"4901\",\"lockedFlag\":\"N\"},\"customerContact\":{\"id\":\"C77991557A235E14E04400212861B256\",\"defaultInd\":\"Y\"," +
    					"\"name\":{\"firstName\":\"efdgh\",\"lastName\":\"hgdbk\"},\"email\":\"Api_US_sell_indy07@testmail.com\"}}," +
    					"{\"id\":\"_sWQk-iVT0NHvtUW\",\"bookOfBusinessId\":\"1\",\"defaultPaymentInd\":\"true\",\"paymentType\":\"paypal\"," +
    					"\"paypalDetails\":{\"mode\":\"PAYABLE\",\"emailAddress\":\"Api_US_sell_indy07@testmail.com\"}}]}}";
    				InputStream is = new ByteArrayInputStream( responseString.getBytes() );			
        			return is;
				}else if (entityName.equalsIgnoreCase("PayableInstrumentTypeResponse")) {
					PayableInstrumentTypeResponse res = new PayableInstrumentTypeResponse();
					res.setPayableInstrumentTypes( _buildPayableInstrumentTypes() );
					return res;
				}else if(entityName.equalsIgnoreCase("GetCustomerResponse")){
                    GetCustomerResponse res = new GetCustomerResponse();
                    CustomerContactDetails contactDetails = new CustomerContactDetails();
                    res.setCustomerContact(contactDetails);
                    contactDetails.setId(1234567L);
                    contactDetails.setDefaultInd("Y");
                    contactDetails.setPaymentContact("Y");
                    contactDetails.setName(new CustomerContactDetails.Name());
                    contactDetails.getName().firstName = "Sam";
                    contactDetails.getName().lastName = "Wise";
                    contactDetails.setPhoneNumber("60225314xxxx");
                    contactDetails.setPhoneExt("602418xxxx");
                    contactDetails.setEmail("118985@testmail.com");

                    contactDetails.setAddress(new CustomerContactDetails.Address());
                    contactDetails.getAddress().setLine1("123 Fremont Dr.");
                    contactDetails.getAddress().setLine2("#204");
                    contactDetails.getAddress().setCity("Tempe");
                    contactDetails.getAddress().setState("AZ");
                    contactDetails.getAddress().setCountry("US");
                    contactDetails.getAddress().setZipOrPostalCode("85281");
		    		return res;
	    		}
				return new CustomerContactDetails();
			}
		};
		return response;
	}
	
	private List<PayableInstrumentType> _buildPayableInstrumentTypes() {
		PayableInstrumentTypeResponse  response = new PayableInstrumentTypeResponse();
		PayableInstrumentType payableInstrumentType = new PayableInstrumentType();
		payableInstrumentType.setId(1L);
		payableInstrumentType.setDescription("Paypal");
		List<PayableInstrumentType> payableInstrumentTypes = new ArrayList<PayableInstrumentType>();
		payableInstrumentTypes.add(payableInstrumentType);
		return payableInstrumentTypes;		
		
	}
	
	private Response getResponseForError() {
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
				return new CustomerContactDetails();
			}
		};
		return response;
	}
	
	
	@Test
	public void testGetCustomerContactByContactGuidV2Success(){
		when(svcLocator.locate(anyString(), anyList())).thenReturn(webClient);
		when(webClient.get()).thenReturn(getUserContactResponse());
		CustomerContactV2Details customerContact = userHelper.getCustomerContactByContactGuidV2("C77991557A365E14E04400212861B256", "sdf342342");
		Assert.assertNotNull(customerContact);	
	}
	
	@Test
	public void testGetCustomerContactByContactGuidV2BadResponse(){
		when(svcLocator.locate(anyString(), anyList())).thenReturn(webClient);
		when(webClient.get()).thenReturn(getUserContactBadResponse());
		CustomerContactV2Details customerContact = userHelper.getCustomerContactByContactGuidV2("C77991557A365E14E04400212861B256", "sdf342342");
		Assert.assertNull(customerContact);	
	}
	
	@Test
	public void testGetUserContactResourceNotFound(){
		when(svcLocator.locate(anyString(), anyList())).thenReturn(webClient);
		when(webClient.get()).thenReturn(getUserContactResourceNotFound());
		CustomerContactV2Details customerContact = userHelper.getCustomerContactByContactGuidV2("C77991557A365E14E04400212861B256", "sdf342342");
		Assert.assertNull(customerContact);	
	}
	
	
	@Test
	public void testGetUserContactMappingSuccess(){
		when(svcLocator.locate(anyString(), anyList())).thenReturn(webClient);
		when(webClient.get()).thenReturn(getUserContactMappingResponse200());
		CustomerContactMappingResponse mappingResponse = userHelper.getCustomerContactId("C77991557A365E14E04400212861B256", "sdf342342");
		Assert.assertNotNull(mappingResponse);	
	}
	
	@Test
	public void testGetUserContactMappingBadResponse(){
		when(svcLocator.locate(anyString(), anyList())).thenReturn(webClient);
		when(webClient.get()).thenReturn(getUserContactBadResponse());
		CustomerContactMappingResponse mappingResponse  = userHelper.getCustomerContactId("C77991557A365E14E04400212861B256", "sdf342342");
		Assert.assertNull(mappingResponse);	
	}
	
	
	@Test
	public void testGetUserContactMappingResourceNotFound(){
		when(svcLocator.locate(anyString(), anyList())).thenReturn(webClient);
		when(webClient.get()).thenReturn(getUserContactResourceNotFound());
		CustomerContactMappingResponse mappingResponse  = userHelper.getCustomerContactId("C77991557A365E14E04400212861B256", "sdf342342");
		Assert.assertNull(mappingResponse);	
	}
	
	@Test
	public void testGetUserContactMappingInverseSuccess(){
		when(svcLocator.locate(anyString(), anyList())).thenReturn(webClient);
		when(webClient.get()).thenReturn(getUserContactMappingResponse200());
		CustomerContactMappingResponse mappingResponse = userHelper.getCustomerContactGuid("C77991557A365E14E04400212861B256", "1234");
		Assert.assertNotNull(mappingResponse);	
	}
	
	@Test
	public void testGetUserContactMappingInverseBadResponse(){
		when(svcLocator.locate(anyString(), anyList())).thenReturn(webClient);
		when(webClient.get()).thenReturn(getUserContactBadResponse());
		CustomerContactMappingResponse mappingResponse  = userHelper.getCustomerContactGuid("C77991557A365E14E04400212861B256", "1234");
		Assert.assertNull(mappingResponse);	
	}
	
	
	@Test
	public void testGetUserContactMappingInverseResourceNotFound(){
		when(svcLocator.locate(anyString(), anyList())).thenReturn(webClient);
		when(webClient.get()).thenReturn(getUserContactResourceNotFound());
		CustomerContactMappingResponse mappingResponse  = userHelper.getCustomerContactGuid("C77991557A365E14E04400212861B256", "1234");
		Assert.assertNull(mappingResponse);	
	}

	@Test
	public void testGetSellerInfoSellerTypeNotFound(){
		when(svcLocator.locate(anyString(), anyList())).thenReturn(webClient);
		when(webClient.get()).thenReturn(getUserContactResourceNotFound());

		Long SELLER_ID = 1234L;

		ProSellerInfo sellerInfo  = userHelper.getSellerInfo(SELLER_ID);
		Assert.assertNull(sellerInfo);
	}

	private Response getUserContactMappingResponse200() {
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
				CustomerContactMappingResponse mappingResponse  = new CustomerContactMappingResponse();
				mappingResponse.setId("sdfsf232");
				mappingResponse.setInternalId("342343243");
				
				return mappingResponse;
			}
		};
		return response;
	}
	
	private Response getUserContactResponse() {
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
				CustomerContactV2Details customerContactDetails = new CustomerContactV2Details();
				CustomerContactV2Details.Address address = new CustomerContactV2Details.Address();
				address.setCity("Young America");
				address.setCountry("US");
				address.setLine1("199");
				address.setLine2("Fremont");
				address.setState("MN");
				address.setZipOrPostalCode("55555");
				customerContactDetails.setAddress(address);
				customerContactDetails.setDefaultInd("Y");
				customerContactDetails.setEmail("test@testmail.com");
				customerContactDetails.setId("1234");
				return customerContactDetails;
			}
		};
		return response;
	}
	
	private Response getUserContactBadResponse() {
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
				return null;
			}
		};
		return response;
	}
	
	
	private Response getUserContactResourceNotFound() {
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
				return null;
			}
		};
		return response;
	}	
	

	

}
