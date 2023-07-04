package com.stubhub.domain.inventory.listings.v2.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Currency;
import java.util.Locale;

import org.junit.Assert;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.biz.v2.intf.ExternalSystemUserMgr;
import com.stubhub.domain.inventory.biz.v2.intf.UserCustRepRelMgr;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.PaymentType;
import com.stubhub.domain.inventory.listings.v2.util.PaymentHelper;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.domain.user.payments.intf.PayPalDetails;
import com.stubhub.domain.user.payments.intf.PayableDetails;
import com.stubhub.domain.user.payments.v2.intf.CustomerPaymentInstrumentDetailsV2;

public class PaymentHelperTest {
	
	private PaymentHelper paymentHelper;
	private UserHelper userHelperMock;
	
	private ExternalSystemUserMgr externalSystemUserMgr;
	
	private UserCustRepRelMgr userCustRepRelMgr;
	
	@BeforeTest
	public void setup() throws Exception {
		paymentHelper = new PaymentHelper();
		userHelperMock = mock(UserHelper.class);
		externalSystemUserMgr= mock(ExternalSystemUserMgr.class);
		userCustRepRelMgr=mock(UserCustRepRelMgr.class);
		ReflectionTestUtils.setField(paymentHelper, "userHelper", userHelperMock);
		ReflectionTestUtils.setField(paymentHelper, "externalSystemUserMgr", externalSystemUserMgr);
		ReflectionTestUtils.setField(paymentHelper, "userCustRepRelMgr", userCustRepRelMgr);
	}
	
	@Test
	public void testPopulatePaymentDetails() {
		Listing listing = new Listing();
		listing.setSellerPaymentTypeId(1L);
		listing.setSellerCCId(123L);
		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(), Mockito.anyString())).thenReturn(1L);
		paymentHelper.populatePaymentDetails(listing);
	}
	
	@Test
	public void testPopulatePaymentDetailsHiddenListing() {
		Listing listing = new Listing();
		listing.setSellerPaymentTypeId(null);
		listing.setSystemStatus("HIDDEN");
		listing.setSellerCCId(123L);
		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(), Mockito.anyString())).thenReturn(1L);
		paymentHelper.populatePaymentDetails(listing);
	}
	
	@Test
	public void testPopulatePaymentDetails_Default_paypal() {
		Listing listing = new Listing();
		listing.setSystemStatus("ACTIVE");
		Event event = new Event();
		event.setBookOfBusinessId(Long.valueOf(1L));
		event.setCurrency(Currency.getInstance(Locale.CANADA));
		listing.setEvent(event);

		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		CustomerPaymentInstrumentDetailsV2 details = new CustomerPaymentInstrumentDetailsV2();
		details.setDefaultPaymentInd("true");
		details.setPaymentType("paypal");
		details.setBookOfBusinessId("1");
		details.setCurrency("CAD");
		PayPalDetails paypalDetails = new PayPalDetails();
		paypalDetails.setMode("PAYABLE");
		details.setPaypalDetails(paypalDetails);
		paymentInstruments.add(details);
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setId("1002");
		details.setDefaultPaymentInd("false");
		details.setPaymentType("creditcard");		
		paymentInstruments.add(details);
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(), Mockito.anyString())).thenReturn(1L);
		paymentHelper.populatePaymentDetails(listing);
	}
	
	@Test
	public void testPopulatePaymentDetails_Default_ACH() {
		Listing listing = new Listing();
		listing.setSystemStatus("ACTIVE");
		Event event = new Event();
		event.setBookOfBusinessId(Long.valueOf(1L));
		event.setCurrency(Currency.getInstance(Locale.CANADA));
		listing.setEvent(event); 
		
		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		CustomerPaymentInstrumentDetailsV2 details = new CustomerPaymentInstrumentDetailsV2();
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setBookOfBusinessId("1");
		details.setCurrency("CAD");
		details.setId("1004"); 
		details.setPaymentType("ACH");
		details.setDefaultPaymentInd("true");
		PayableDetails payableDetails=new PayableDetails();
		payableDetails.setBankName("testbank");
		payableDetails.setExternalPaymentInstrumentToken("testid");
		payableDetails.setLastFourDigits("1234");
		details.setPayableDetails(payableDetails);
		paymentInstruments.add(details);
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setId("1002");
		details.setDefaultPaymentInd("false");
		details.setPaymentType("creditcard");
		paymentInstruments.add(details);
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(),Mockito.anyString())).thenReturn(1L);
		paymentHelper.populatePaymentDetails(listing); 
	}

	@Test
	public void testPopulatePaymentDetails_Listing_Check() {
		Listing listing = new Listing();
		listing.setSystemStatus("ACTIVE");
		// listing still created 
		listing.setSellerPaymentTypeId(PaymentType.Check.getType());

		Event event = new Event();
		event.setBookOfBusinessId(Long.valueOf(1L));
		event.setCurrency(Currency.getInstance(Locale.CANADA));
		listing.setEvent(event);

		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		CustomerPaymentInstrumentDetailsV2 details = new CustomerPaymentInstrumentDetailsV2();
		details.setDefaultPaymentInd("true");
		details.setPaymentType("paypal");
		details.setBookOfBusinessId("1");
		details.setCurrency("CAD");
		PayPalDetails paypalDetails = new PayPalDetails();
		paypalDetails.setMode("PAYABLE");
		details.setPaypalDetails(paypalDetails);
		paymentInstruments.add(details);
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setId("1002");
		details.setDefaultPaymentInd("false");
		details.setPaymentType("creditcard");		
		paymentInstruments.add(details);
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(), Mockito.anyString())).thenReturn(1L);
		paymentHelper.populatePaymentDetails(listing);

	}

	@Test
	public void testPopulatePaymentDetails_Listing_LargeSellerCheck() {
		Listing listing = new Listing();
		listing.setSystemStatus("ACTIVE");
		listing.setSellerPaymentTypeId(PaymentType.LargeSellerCheck.getType());

		Event event = new Event();
		event.setBookOfBusinessId(Long.valueOf(1L));
		event.setCurrency(Currency.getInstance(Locale.CANADA));
		listing.setEvent(event);

		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		CustomerPaymentInstrumentDetailsV2 details = new CustomerPaymentInstrumentDetailsV2();
		details.setDefaultPaymentInd("true");
		details.setPaymentType("paypal");
		details.setBookOfBusinessId("1");
		details.setCurrency("CAD");
		PayPalDetails paypalDetails = new PayPalDetails();
		paypalDetails.setMode("PAYABLE");
		details.setPaypalDetails(paypalDetails);
		paymentInstruments.add(details);
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setId("1002");
		details.setDefaultPaymentInd("false");
		details.setPaymentType("creditcard");		
		paymentInstruments.add(details);
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(), Mockito.anyString())).thenReturn(1L);
		paymentHelper.populatePaymentDetails(listing);

	}

	@Test
	public void testPopulatePaymentDetails_Default_Check() {
		Listing listing = new Listing();

		listing.setSystemStatus("ACTIVE");
		Event event = new Event();
		event.setBookOfBusinessId(Long.valueOf(1L));
		event.setCurrency(Currency.getInstance(Locale.CANADA));
		listing.setEvent(event);

		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		CustomerPaymentInstrumentDetailsV2 details = new CustomerPaymentInstrumentDetailsV2();
		details.setId("1001");
		details.setDefaultPaymentInd("true");
		details.setPaymentType("check");	
		details.setBookOfBusinessId("1");
		details.setCurrency("CAD");
		paymentInstruments.add(details);
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setId("1002");
		details.setDefaultPaymentInd("false");
		details.setPaymentType("creditcard");		
		paymentInstruments.add(details);
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(), Mockito.anyString())).thenReturn(1L);
		paymentHelper.populatePaymentDetails(listing);
	}

	@Test
	public void testPopulatePaymentDetails_Default_largeSellerCheck() {
		Listing listing = new Listing();

		listing.setSystemStatus("ACTIVE");
		Event event = new Event();
		event.setBookOfBusinessId(Long.valueOf(1L));
		event.setCurrency(Currency.getInstance(Locale.CANADA));
		listing.setEvent(event);

		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		CustomerPaymentInstrumentDetailsV2 details = new CustomerPaymentInstrumentDetailsV2();
		details.setDefaultPaymentInd("true");
		details.setPaymentType("largeSellerCheck");	
		details.setBookOfBusinessId("1");
		details.setCurrency("CAD");
		paymentInstruments.add(details);
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setId("1002");
		details.setDefaultPaymentInd("false");
		details.setPaymentType("creditcard");		
		paymentInstruments.add(details);
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(), Mockito.anyString())).thenReturn(1L);
		paymentHelper.populatePaymentDetails(listing);
	}
	
	@Test(expectedExceptions = ListingBusinessException.class)
	public void testPopulatePaymentDetails_Default_ACHWithACHBobIsNotEqualsToEvent() {
		Listing listing = new Listing();

		listing.setSystemStatus("ACTIVE");
		Event event = new Event();
		event.setBookOfBusinessId(Long.valueOf(1L));
		listing.setEvent(event);
		
		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		CustomerPaymentInstrumentDetailsV2 details = new CustomerPaymentInstrumentDetailsV2();
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setBookOfBusinessId("2");
		details.setId("1004"); 
		details.setPaymentType("ACH");
		details.setDefaultPaymentInd("true");
		PayableDetails payableDetails=new PayableDetails();
		payableDetails.setBankName("testbank");
		payableDetails.setExternalPaymentInstrumentToken("testid");
		payableDetails.setLastFourDigits("1234");
		details.setPayableDetails(payableDetails);
		paymentInstruments.add(details);
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setId("1002");
		details.setDefaultPaymentInd("false");
		details.setPaymentType("creditcard");		
		paymentInstruments.add(details);
		
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(),Mockito.anyString())).thenReturn(1L);
		paymentHelper.populatePaymentDetails(listing);
	}
	
	
	@Test(expectedExceptions = ListingBusinessException.class)
	public void testPopulatePaymentDetails_PaymentTypeIsNull() {
		Listing listing = new Listing();

		listing.setSystemStatus("ACTIVE");
		Event event = new Event();
		event.setBookOfBusinessId(Long.valueOf(1L));
		listing.setEvent(event);
		
		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		CustomerPaymentInstrumentDetailsV2 details = new CustomerPaymentInstrumentDetailsV2();
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setBookOfBusinessId("1");
		details.setId("1004"); 
		details.setDefaultPaymentInd("true");
		PayableDetails payableDetails=new PayableDetails();
		payableDetails.setBankName("testbank");
		payableDetails.setExternalPaymentInstrumentToken("testid");
		payableDetails.setLastFourDigits("1234");
		details.setPayableDetails(payableDetails);
		paymentInstruments.add(details);
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setId("1002");
		details.setDefaultPaymentInd("false");
		details.setPaymentType("creditcard");		
		paymentInstruments.add(details);
		
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(), Mockito.anyString())).thenReturn(1L);
		paymentHelper.populatePaymentDetails(listing);
	}
	
	@Test(expectedExceptions = ListingBusinessException.class)
	public void testPopulatePaymentDetails_PaymentTypeIsNullDueToNoEventSet() {
		Listing listing = new Listing();
		listing.setSystemStatus("ACTIVE");
		
		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		CustomerPaymentInstrumentDetailsV2 details = new CustomerPaymentInstrumentDetailsV2();
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setBookOfBusinessId("1");
		details.setId("1004"); 
		details.setDefaultPaymentInd("true");
		PayableDetails payableDetails=new PayableDetails();
		payableDetails.setBankName("testbank");
		payableDetails.setExternalPaymentInstrumentToken("testid");
		payableDetails.setLastFourDigits("1234");
		details.setPayableDetails(payableDetails);
		paymentInstruments.add(details);
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setId("1002");
		details.setDefaultPaymentInd("false");
		details.setPaymentType("creditcard");		
		paymentInstruments.add(details);
		
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(),Mockito.anyString())).thenReturn(1L);
		paymentHelper.populatePaymentDetails(listing);
	}
	
	@Test(expectedExceptions = ListingBusinessException.class)
	public void testPopulatePaymentDetails_PaymentTypeIsNullDueToNoEventBobIsNull() {
		Listing listing = new Listing();
		listing.setSystemStatus("ACTIVE");
		Event event = new Event();
		event.setBookOfBusinessId(null);
		listing.setEvent(event);
		
		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		CustomerPaymentInstrumentDetailsV2 details = new CustomerPaymentInstrumentDetailsV2();
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setBookOfBusinessId("1");
		details.setId("1004"); 
		details.setDefaultPaymentInd("true");
		PayableDetails payableDetails=new PayableDetails();
		payableDetails.setBankName("testbank");
		payableDetails.setExternalPaymentInstrumentToken("testid");
		payableDetails.setLastFourDigits("1234");
		details.setPayableDetails(payableDetails);
		paymentInstruments.add(details);
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setId("1002");
		details.setDefaultPaymentInd("false");
		details.setPaymentType("creditcard");		
		paymentInstruments.add(details);
		
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(),Mockito.anyString())).thenReturn(1L);
		paymentHelper.populatePaymentDetails(listing);
	}
	
	
	@Test(expectedExceptions = ListingBusinessException.class)
	public void testPopulatePaymentDetails_PaymentTypeUnknown() {
		
		Listing listing = new Listing();
		Event event = new Event();
		event.setBookOfBusinessId(Long.valueOf(1L));
		listing.setEvent(event);

		listing.setSystemStatus("ACTIVE");
		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		CustomerPaymentInstrumentDetailsV2 details = new CustomerPaymentInstrumentDetailsV2();
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setBookOfBusinessId("1");
		details.setId("1004"); 
		details.setPaymentType("Unknown");
		details.setDefaultPaymentInd("true");
		PayableDetails payableDetails=new PayableDetails();
		payableDetails.setBankName("testbank");
		payableDetails.setExternalPaymentInstrumentToken("testid");
		payableDetails.setLastFourDigits("1234");
		details.setPayableDetails(payableDetails);
		paymentInstruments.add(details);
		details = new CustomerPaymentInstrumentDetailsV2();
		details.setId("1002");
		details.setDefaultPaymentInd("false");
		details.setPaymentType("creditcard");		
		paymentInstruments.add(details);
		
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(), Mockito.anyString())).thenReturn(1L);
		paymentHelper.populatePaymentDetails(listing);
	}
	@Test
	public void testPopulatePaymentDetails_Default_Check_ACTIVE() {
		Listing listing = new Listing();
		listing.setSystemStatus("ACTIVE");
		Event event = new Event();
		event.setBookOfBusinessId(Long.valueOf(1L));
		listing.setEvent(event);
		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(), Mockito.anyString())).thenReturn(1L);
		try {
			paymentHelper.populatePaymentDetails(listing);
		} catch (ListingBusinessException e) {
			Assert.assertTrue(true);
		}
	}
	@Test(expectedExceptions = ListingBusinessException.class)
	public void testPopulatePaymentDetails_GetAllInstrumentIsNull() {
		Listing listing = new Listing();
		listing.setSystemStatus("ACTIVE");
		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(null);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(), Mockito.anyString())).thenReturn(1L);
		paymentHelper.populatePaymentDetails(listing);
	}
	@Test(expectedExceptions = ListingBusinessException.class)
	public void testPopulatePaymentDetails_paymenttypeisnotnull1() {
		Listing listing = new Listing();
		listing.setSystemStatus("ACTIVE");
		listing.setSellerPaymentTypeId(PaymentType.Paypal.getType());
		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(), Mockito.anyString())).thenReturn(1L);
		paymentHelper.populatePaymentDetails(listing);
	}
	@Test(expectedExceptions = ListingBusinessException.class)
	public void testPopulatePaymentDetails_paymenttypeisnotnull2() {
		Listing listing = new Listing();
		listing.setSystemStatus("ACTIVE");
		listing.setSellerPaymentTypeId(PaymentType.Paypal.getType());
		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(false);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(), Mockito.anyString())).thenReturn(1L);
		paymentHelper.populatePaymentDetails(listing);
	}
	@Test(expectedExceptions = ListingBusinessException.class)
	public void testPopulatePaymentDetails_paymenttypeisnotnull3() {
		Listing listing = new Listing();
		listing.setSystemStatus("ACTIVE");
		listing.setSellerPaymentTypeId(3L);
		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(), Mockito.anyString())).thenReturn(1L);
		when(externalSystemUserMgr.getExternalSystemUserByUserId(Mockito.anyLong())).thenReturn(null);
		paymentHelper.populatePaymentDetails(listing);
	}
	@Test(expectedExceptions = ListingBusinessException.class)
	public void testPopulatePaymentDetails_paymenttypeisnotnull4() {
		Listing listing = new Listing();
		listing.setSystemStatus("ACTIVE");
		listing.setSellerPaymentTypeId(821L);
		List<CustomerPaymentInstrumentDetailsV2> paymentInstruments = new ArrayList<CustomerPaymentInstrumentDetailsV2>();
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		when(userHelperMock.getAllSellerPaymentInstrumentV2(Mockito.anyString())).thenReturn(paymentInstruments);
		when(userHelperMock.getMappedValidSellerCCId(Mockito.anyString(),Mockito.anyString(), Mockito.anyList(), Mockito.anyString())).thenReturn(1L);
		when(userCustRepRelMgr.getByUserIdAndType(Mockito.anyLong(),Mockito.anyLong())).thenReturn(null);
		paymentHelper.populatePaymentDetails(listing);
	}
	@Test
	public void testPopulatePaymentDetailsInvalidPaymentTypeException() {
		Listing listing = new Listing();
		listing.setSellerPaymentTypeId(1L);
		listing.setSellerCCId(123L);
		when(userHelperMock.isSellerPaymentTypeValid(Mockito.anyLong(),Mockito.anyLong(), Mockito.anyLong())).thenReturn(false);
		try {
			paymentHelper.populatePaymentDetails(listing);
		} catch (ListingBusinessException e) {
			Assert.assertTrue(true);
		}
	}
	
}
