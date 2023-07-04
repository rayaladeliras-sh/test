package com.stubhub.domain.inventory.listings.v2.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.fulfillment.window.v1.intf.DeliveryMethodResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.EventFulfillmentWindowResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.FulfillmentMethodResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.FulfillmentWindowResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.ListingFulfillmentWindowResponse;
import com.stubhub.domain.infrastructure.common.core.concurrent.SHThreadLocals;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.biz.v2.impl.util.FulfillmentServiceAdapter;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.FulfillmentMethod;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.enums.ConfirmOptionEnum;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContextImpl;

public class FulfillmentServiceHelperTest {
	
	private static final Log log = LogFactory.getLog(FulfillmentServiceHelperTest.class);
	private Long listingId = 1L;
	private Long eventId = 2L;
	private Long buyerContactId = 3L;
	Calendar inHandDate = Calendar.getInstance();
	Calendar eventDate = Calendar.getInstance();
	Event event = new Event();
	private List<FulfillmentWindow> windows;
	private FulfillmentWindow window;
	private FulfillmentServiceAdapter fulfillmentServiceAdapter;
	private FulfillmentServiceHelper helper;
	private Listing listing;
	
	@BeforeMethod
	public void setUp () {
		event.setEventDate(Calendar.getInstance());   
		event.setId(4093944L);
		event.setJdkTimeZone(TimeZone.getTimeZone("UTC"));
		eventDate.roll(Calendar.MONTH, Boolean.TRUE);
		helper = new FulfillmentServiceHelper ();
		fulfillmentServiceAdapter = Mockito.mock(FulfillmentServiceAdapter.class);
		ReflectionTestUtils.setField(helper, "fulfillmentServiceAdapter", fulfillmentServiceAdapter);
		windows = new ArrayList<FulfillmentWindow>();
		window = new FulfillmentWindow();
		listing = new Listing();
	
	}
	
	@Test
	public void testFulfillmentLMSStatus2 () throws Exception
	{
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("LMS");
		window.setDeliveryMethodId(1l);

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);
		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);

		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		listing.setFulfillmentDeliveryMethods("9");
		listing.setIsLmsApproval(true);
		listing.setLmsApprovalStatus(2);
		
		GregorianCalendar lih = new GregorianCalendar();
		lih.add(Calendar.DAY_OF_YEAR, 3);

		helper.populateFulfillmentOptions(listing);
	}

	@Test
	public void testFulfillmentWillCallStatus1 () throws Exception
	{
		window.setFulfillmentMethodId(8l);
		window.setFulfillmentMethodName("WILLCALL");
		window.setDeliveryMethodId(10l);

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);

		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);

		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);


		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentMethod(FulfillmentMethod.OTHERPREDELIVERY);
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		listing.setFulfillmentDeliveryMethods("8");
		listing.setIsLmsApproval(false);
		listing.setLmsApprovalStatus(2);

		GregorianCalendar lih = new GregorianCalendar();
		lih.add(Calendar.DAY_OF_YEAR, 3);

		helper.populateFulfillmentOptions(listing);
	}

	@Test
	public void testFulfillmentWillCallStatus2 () throws Exception
	{
		window.setFulfillmentMethodId(8l);
		window.setFulfillmentMethodName("WILLCALL");
		window.setDeliveryMethodId(10l);

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);

		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);

		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);

		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setListingSource(8);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentDeliveryMethods("8");
		listing.setIsLmsApproval(false);
		listing.setLmsApprovalStatus(2);

		GregorianCalendar lih = new GregorianCalendar();
		lih.add(Calendar.DAY_OF_YEAR, 3);

		helper.populateFulfillmentOptions(listing);
	}

	@Test
	public void testFulfillmentSTH () throws Exception
	{
		window.setFulfillmentMethodId(1l);
		window.setFulfillmentMethodName("STH");
		window.setDeliveryMethodId(10l);

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);

		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);

		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);

		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentDeliveryMethods("1");
		listing.setIsLmsApproval(false);
		listing.setLmsApprovalStatus(2);
		listing.setListingSource(8);

		GregorianCalendar lih = new GregorianCalendar();
		lih.add(Calendar.DAY_OF_YEAR, 3);

		helper.populateFulfillmentOptions(listing);
	}

	@Test
	public void testFulfillmentLMSStatus4 () throws Exception 
	{
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("LMS");
		window.setDeliveryMethodId(1l);

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);
		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		listing.setIsLmsApproval(true);
		listing.setLmsApprovalStatus(4);
		listing.setFulfillmentDeliveryMethods("9,7");
		
		GregorianCalendar lih = new GregorianCalendar();
		lih.add(Calendar.DAY_OF_YEAR, 3);

		helper.populateFulfillmentOptions(listing);
	}	
	
	@Test
	public void testFulfillmentLMSStatusFalse () throws Exception 
	{
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("LMS");
		window.setDeliveryMethodId(1l);

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);
		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setIsLmsApproval(false);
		
		GregorianCalendar lih = new GregorianCalendar();
		lih.add(Calendar.DAY_OF_YEAR, 3);

		helper.populateFulfillmentOptions(listing);
	}	
	
	@Test
	public void populateFulfillmentOptions() throws Exception {
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("LMS");
		window.setDeliveryMethodId(1l);

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);
		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		
		GregorianCalendar lih = new GregorianCalendar();
		lih.add(Calendar.DAY_OF_YEAR, 3);

		helper.populateFulfillmentOptions(listing);
	}
	
	@Test
	public void populateFulfillmentOptionsSaleEndDateAfterFFEndTime() throws Exception {
		final Calendar instance = Calendar.getInstance(event.getJdkTimeZone());
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("LMS");
		window.setDeliveryMethodId(1l);

		GregorianCalendar ed = new GregorianCalendar();
		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		Mockito.when(Calendar.getInstance()).thenAnswer(new Answer<Calendar>() {
	        public Calendar answer(InvocationOnMock invocation) {
	            return instance;
	    }
	});
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setEvent(event);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		listing.setListPrice(new Money("100"));
		GregorianCalendar lih = new GregorianCalendar();
		lih.add(Calendar.DAY_OF_YEAR, 3);
		
		listing.setEndDate(lih);
		helper.populateFulfillmentOptions(listing);
		Assert.assertEquals(listing.getEndDate(), window.getEndTime());
	}
	
	@Test
	public void populateFulfillmentOptionsSaleEndDateAfterFFEndTimeListPriceNull() throws Exception {
		final Calendar instance = Calendar.getInstance(event.getJdkTimeZone());
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("LMS");
		window.setDeliveryMethodId(1l);

		GregorianCalendar ed = new GregorianCalendar();
		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		Mockito.when(Calendar.getInstance()).thenAnswer(new Answer<Calendar>() {
	        public Calendar answer(InvocationOnMock invocation) {
	            return instance;
	    }
	});
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setEvent(event);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		listing.setListPrice(null);
		GregorianCalendar lih = new GregorianCalendar();
		lih.add(Calendar.DAY_OF_YEAR, 3);
		
		listing.setEndDate(lih);
		helper.populateFulfillmentOptions(listing);
		Assert.assertEquals(listing.getEndDate(), window.getEndTime());
	}
	
	
	@Test
	public void populateFulfillmentOptionsSaleEndDateBeforeFFEndTime() throws Exception {
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("UPS");
		window.setDeliveryMethodId(1l);

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setEvent(event);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentMethod(FulfillmentMethod.UPS);
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		
		GregorianCalendar lih = new GregorianCalendar();
		lih.add(Calendar.DAY_OF_YEAR, 1);
		listing.setEndDate(lih);
		helper.populateFulfillmentOptions(listing);
		Assert.assertTrue(listing.getEndDate().before(window.getEndTime()));
	}
	
	@Test
	public void populateFulfillmentOptionsNoSaleEndDateInRequest() throws Exception {
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("LMS");
		window.setDeliveryMethodId(1l);

		GregorianCalendar ed = new GregorianCalendar();
		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setEvent(event);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		
		helper.populateFulfillmentOptions(listing);
		Assert.assertEquals(listing.getEndDate(), window.getEndTime());
	}
	
	
	@Test
    public void populateFulfillmentOptionsUPSWithLMSFM() throws Exception {
        window.setFulfillmentMethodId(7l);
        window.setFulfillmentMethodName("LMS");
        window.setDeliveryMethodId(1l);

        GregorianCalendar ed = new GregorianCalendar();
        ed.add(Calendar.DAY_OF_YEAR, 4);
        
        window.setEndTime(ed);
        window.setBaseCost(100d);
        window.setStartTime(new GregorianCalendar());
        windows.add(window);
        
        FulfillmentWindow window1 = new FulfillmentWindow();
        window1.setFulfillmentMethodId(7l);
        window1.setStartTime(new GregorianCalendar());
        window1.setFulfillmentMethodName("LMS");
        window1.setDeliveryMethodId(1l);

        GregorianCalendar ed1 = new GregorianCalendar();
        ed1.add(Calendar.DAY_OF_YEAR, 6);
        
        window1.setEndTime(ed1);
        window1.setBaseCost(100d);
        windows.add(window1);
        
        
        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
        listing.setEventId(1000l);
        listing.setSellerContactId(1000l);
        listing.setDeliveryOption(2);
        listing.setSystemStatus("ACTIVE");
        listing.setFulfillmentMethod(FulfillmentMethod.UPS);
        listing.setTicketMedium(TicketMedium.PAPER.getValue());
        
        GregorianCalendar lih = new GregorianCalendar();
        lih.add(Calendar.DAY_OF_YEAR, 3);

        helper.populateFulfillmentOptions(listing);
    }

	@Test
	public void populateFulfillmentOption_Shipping() throws Exception {
		window.setFulfillmentMethodId(12l);
		window.setFulfillmentMethodName("Dutche Post");
		window.setFulfillmentTypeName("Shipping");
		window.setDeliveryMethodId(1l);

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentMethod(FulfillmentMethod.SHIPPING);
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		
		helper.populateFulfillmentOptions(listing);
		Assert.assertNotNull(listing.getEndDate());
		Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
	}

	@Test
	public void populateFulfillmentOption_Shipping_NoFulfillmentMethod() throws Exception {
		window.setFulfillmentMethodId(11l);
		window.setFulfillmentMethodName("Royal mail");
		window.setFulfillmentTypeName("Shipping");
		window.setDeliveryMethodId(1l);

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");

		
		helper.populateFulfillmentOptions(listing);
		Assert.assertNotNull(listing.getEndDate());
		Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
	}
	

	@Test
	public void populateFulfillmentOption_NonElectronic() throws Exception {
		window.setFulfillmentMethodId(11l);
		window.setFulfillmentMethodName("Royal mail");
		window.setFulfillmentTypeName("Shipping");
		window.setDeliveryMethodId(1l);

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setIsElectronicDelivery(false);

		
		helper.populateFulfillmentOptions(listing);
		Assert.assertNotNull(listing.getEndDate());
		Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
		Assert.assertTrue(listing.getTicketMedium() == TicketMedium.PAPER.getValue());
	}
	
	@Test
	public void populateFulfillmentOption_EISHipping() throws Exception {
		window.setFulfillmentMethodId(11l);
		window.setFulfillmentMethodName("Royal mail");
		window.setFulfillmentTypeName("Shipping");
		window.setDeliveryMethodId(1l);

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setIsElectronicDelivery(true);

		
		helper.populateFulfillmentOptions(listing);
		Assert.assertNotNull(listing.getEndDate());
		Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
		Assert.assertTrue(listing.getTicketMedium() == TicketMedium.PAPER.getValue());
	}
	
	@Test
	public void populateFulfillmentOption_NonElectronicLMS() throws Exception {
		window.setFulfillmentMethodId(7L);
		window.setFulfillmentMethodName("LMS");
		window.setDeliveryMethodId(9L);

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setIsElectronicDelivery(false);

		
		helper.populateFulfillmentOptions(listing);
		Assert.assertNotNull(listing.getEndDate());
		Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
		Assert.assertTrue(listing.getTicketMedium() == TicketMedium.PAPER.getValue());
		
		
	}
	
	@Test
	public void populateFulfillmentOption_NonElectronicLMSET() throws Exception {
		window.setFulfillmentMethodId(7L);
		window.setFulfillmentMethodName("LMS");
		window.setDeliveryMethodId(9L);

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setIsElectronicDelivery(true);

		
		helper.populateFulfillmentOptions(listing);
		Assert.assertNotNull(listing.getEndDate());
		Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
		Assert.assertTrue(listing.getTicketMedium() == TicketMedium.PAPER.getValue());
		
		
	}
	@Test
	public void populateFulfillmentOption_ElectronicUPS() throws Exception {
		window.setFulfillmentMethodId(10L);
		window.setFulfillmentMethodName("UPS");
		window.setFulfillmentTypeName("UPS");
		window.setDeliveryMethodId(9L);

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 7);
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setIsElectronicDelivery(true);

		
		helper.populateFulfillmentOptions(listing);
		Assert.assertNotNull(listing.getEndDate());
		Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
		Assert.assertTrue(listing.getTicketMedium() == TicketMedium.PAPER.getValue());
		
		
	}

	@Test
	public void populateFulfillmentOptions_fulfillmethodNull() throws Exception {
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("LMS");
		window.setDeliveryMethodId(1l);

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);
		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		
		GregorianCalendar lih = new GregorianCalendar();
		lih.add(Calendar.DAY_OF_YEAR, 3);
		
		helper.populateFulfillmentOptions(listing);
	}

	@Test
	public void populateFulfillmentOptionsWithEITest1() throws Exception 
	{
		window.setFulfillmentMethodId(10L);
		window.setFulfillmentMethodName("UPS");
		window.setFulfillmentTypeName("UPS");
		window.setDeliveryMethodId(9L);
		GregorianCalendar ed1 = new GregorianCalendar();
		window.setStartTime(ed1);
		GregorianCalendar ed2 = new GregorianCalendar();
		ed2.add(Calendar.DAY_OF_YEAR, 30);		
		window.setEndTime(ed2);
		window.setBaseCost(100d);
		windows.add(window);
		
		window = new FulfillmentWindow();
		window.setFulfillmentMethodId(5L);
		window.setFulfillmentMethodName("PDF");
		window.setFulfillmentTypeName("PDF");
		window.setDeliveryMethodId(4L);
		Calendar ed3 = new GregorianCalendar();
		window.setStartTime(ed3);
		Calendar ed4 = new GregorianCalendar();
		ed4.add(Calendar.DAY_OF_YEAR, 30);		
		window.setEndTime(ed4);
		window.setBaseCost(100d);
		windows.add(window);
		
		window = new FulfillmentWindow();
		window.setFulfillmentMethodId(3L);
		window.setFulfillmentMethodName("Barcode");
		window.setFulfillmentTypeName("Barcode");
		window.setDeliveryMethodId(2L);
		Calendar ed5 = new GregorianCalendar();
		window.setStartTime(ed5);
		Calendar ed6 = new GregorianCalendar();
		ed6.add(Calendar.DAY_OF_YEAR, 30);		
		window.setEndTime(ed6);
		window.setBaseCost(100d);
		windows.add(window);
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);

		listing.setEvent( getEvent() );
		listing.setEventId(listing.getEvent().getId());
		listing.setSellerContactId(1000l);
		listing.setSystemStatus("ACTIVE");
		listing.setDeliveryOption(null);
		listing.setFulfillmentMethod(null);

		helper.populateFulfillmentOptions(listing);
		Assert.assertTrue( listing.getFulfillmentMethod() == FulfillmentMethod.BARCODE, 
				"No deliveryOption, EI, and FM specified, SHOULD default to BARCODE");

		// delivery option provided and default == false
		listing = new Listing();
		listing.setEvent( getEvent() );
		listing.setEventId(listing.getEvent().getId());
		listing.setSellerContactId(1000l);		
		listing.setIsElectronicDelivery(false);
		listing.setFulfillmentMethod(null);
		listing.setDeliveryOption(null);
		helper.populateFulfillmentOptions(listing);
		Assert.assertTrue( listing.getFulfillmentMethod() == FulfillmentMethod.UPS, 
				"No deliveryOption, ElectronicDelivery==False, no FM specified, SHOULD default to UPS");
		
		// set EI == true
		listing = new Listing();
		listing.setEvent( getEvent() );
		listing.setEventId(listing.getEvent().getId());
		listing.setSellerContactId(1000l);				
		listing.setIsElectronicDelivery(true);
		listing.setFulfillmentMethod(null);
		listing.setDeliveryOption(null);
		helper.populateFulfillmentOptions(listing);
		Assert.assertTrue( listing.getFulfillmentMethod() == FulfillmentMethod.BARCODE, 
				"No deliveryOption and FM specified and EI == true, SHOULD default to PDF");
	}
	
	@Test
	public void populateFulfillmentOptionsWithEITest2 () throws Exception 
	{
		window.setFulfillmentMethodId(7L);
		window.setFulfillmentMethodName("LMS");
		window.setDeliveryMethodId(9L);
		GregorianCalendar ed = new GregorianCalendar();
		window.setStartTime(ed);
		ed.add(Calendar.DAY_OF_YEAR, 30);		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);

		listing.setEvent( getEvent() );
		listing.setEventId(listing.getEvent().getId());
		listing.setSellerContactId(1000l);
		listing.setSystemStatus("ACTIVE");
		listing.setDeliveryOption(null);
		listing.setFulfillmentMethod(null);
		listing.setIsElectronicDelivery(false);

		helper.populateFulfillmentOptions(listing);
		Assert.assertTrue( listing.getFulfillmentMethod() == FulfillmentMethod.LMS, 
				"No deliveryOption and FM specified and EI == true, SHOULD default to LMS because there is no PDF PM option");
	}
	
	@Test
	public void populateFulfillmentOptionsWithEILMSErrorTest2 () throws Exception 
	{
		window.setFulfillmentMethodId(7L);
		window.setFulfillmentMethodName("LMS");
		window.setDeliveryMethodId(9L);
		GregorianCalendar ed = new GregorianCalendar();
		window.setStartTime(ed);
		ed.add(Calendar.DAY_OF_YEAR, 30);		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);

		listing.setEvent( getEvent() );
		listing.setEventId(listing.getEvent().getId());
		listing.setSellerContactId(1000l);
		listing.setSystemStatus("ACTIVE");
		listing.setDeliveryOption(null);
		listing.setFulfillmentMethod(null);
		listing.setIsElectronicDelivery(true);

		helper.populateFulfillmentOptions(listing);
		Assert.assertTrue( listing.getFulfillmentMethod() == FulfillmentMethod.LMS, 
				"No deliveryOption and FM specified and EI == true, SHOULD default to LMS because there is no PDF PM option");
	}
	@Test
	public void populateFulfillmentOptionsWithEIPdfTest () throws Exception 
	{
	window.setFulfillmentMethodId(5L);
		window.setFulfillmentMethodName("PDF");
		window.setFulfillmentTypeName("PDF");
	//	window.setDeliveryMethodId(4L);
		GregorianCalendar ed = new GregorianCalendar();
		window.setStartTime(ed);
		ed.add(Calendar.DAY_OF_YEAR, 30);		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);

		listing.setEvent( getEvent() );
		listing.setEventId(listing.getEvent().getId());
		listing.setSellerContactId(1000l);
		listing.setSystemStatus("ACTIVE");
		listing.setDeliveryOption(null);
		listing.setFulfillmentMethod(null);
		listing.setIsElectronicDelivery(true);

		helper.populateFulfillmentOptions(listing);
		Assert.assertTrue( listing.getFulfillmentMethod() == FulfillmentMethod.PDF, 
				"No deliveryOption and FM specified and EI == true, SHOULD default to LMS because there is no PDF PM option");
	}
	
	
	@Test(expectedExceptions = ListingBusinessException.class)
	public void populateFulfillmentOptionsWithEIPdfErrorTest () throws Exception 
	{
	window.setFulfillmentMethodId(5L);
		window.setFulfillmentMethodName("PDF");
		window.setFulfillmentTypeName("PDF");
	//	window.setDeliveryMethodId(4L);
		GregorianCalendar ed = new GregorianCalendar();
		window.setStartTime(ed);
		ed.add(Calendar.DAY_OF_YEAR, 30);		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);

		listing.setEvent( getEvent() );
		listing.setEventId(listing.getEvent().getId());
		listing.setSellerContactId(1000l);
		listing.setSystemStatus("ACTIVE");
		listing.setDeliveryOption(null);
		listing.setFulfillmentMethod(null);
		listing.setIsElectronicDelivery(false);

		helper.populateFulfillmentOptions(listing);
		//Assert.assertTrue( listing.getFulfillmentMethod() == FulfillmentMethod.PDF, 
				//"No deliveryOption and FM specified and EI == true, SHOULD default to LMS because there is no PDF PM option");
	}
	
	@Test(expectedExceptions = ListingBusinessException.class)
	public void populateFulfillmentOptionsWithEINoFMTest () throws Exception 
	{
	//window.setFulfillmentMethodId(5L);
		window.setFulfillmentMethodName("None");
		window.setFulfillmentTypeName("None");
	//	window.setDeliveryMethodId(4L);
		GregorianCalendar ed = new GregorianCalendar();
		window.setStartTime(ed);
		ed.add(Calendar.DAY_OF_YEAR, 30);		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);

		listing.setEvent( getEvent() );
		listing.setEventId(listing.getEvent().getId());
		listing.setSellerContactId(1000l);
		listing.setSystemStatus("ACTIVE");
		listing.setDeliveryOption(null);
		listing.setFulfillmentMethod(null);
		listing.setIsElectronicDelivery(false);

		helper.populateFulfillmentOptions(listing);
		//Assert.assertTrue( listing.getFulfillmentMethod() == FulfillmentMethod.PDF, 
				//"No deliveryOption and FM specified and EI == true, SHOULD default to LMS because there is no PDF PM option");
	}
	
	
	@Test
	public void populateFulfillmentOptionsWithEIUPSErrorTest () throws Exception 
	{
		window.setFulfillmentMethodId(10L);
		window.setFulfillmentMethodName("UPS");
		window.setFulfillmentTypeName("UPS");
		window.setDeliveryMethodId(9L);
		GregorianCalendar ed = new GregorianCalendar();
		window.setStartTime(ed);
		ed.add(Calendar.DAY_OF_YEAR, 30);		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);

		listing.setEvent( getEvent() );
		listing.setEventId(listing.getEvent().getId());
		listing.setSellerContactId(1000l);
		listing.setSystemStatus("ACTIVE");
		listing.setDeliveryOption(null);
		listing.setFulfillmentMethod(null);
		listing.setIsElectronicDelivery(true);
		helper.populateFulfillmentOptions(listing);
	}
	@Test
	public void populateFulfillmentOptionsWithEIBarcodeTest () throws Exception 
	{
		window = new FulfillmentWindow();
		window.setFulfillmentMethodId(3L);
		window.setFulfillmentMethodName("Barcode");
		window.setFulfillmentTypeName("Barcode");
		window.setDeliveryMethodId(2L);
		GregorianCalendar ed = new GregorianCalendar();
		window.setStartTime(ed);
		ed.add(Calendar.DAY_OF_YEAR, 30);		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);

		listing.setEvent( getEvent() );
		listing.setEventId(listing.getEvent().getId());
		listing.setSellerContactId(1000l);
		listing.setSystemStatus("ACTIVE");
		listing.setDeliveryOption(null);
		listing.setFulfillmentMethod(null);
		listing.setIsElectronicDelivery(true);

		helper.populateFulfillmentOptions(listing);
		
	}
	
	@Test
	public void populateFulfillmentOptionsWithEIBarcodeFalseTest () throws Exception 
	{
		window = new FulfillmentWindow();
		window.setFulfillmentMethodId(3L);
		window.setFulfillmentMethodName("Barcode");
		window.setFulfillmentTypeName("Barcode");
		window.setDeliveryMethodId(2L);
		GregorianCalendar ed = new GregorianCalendar();
		window.setStartTime(ed);
		ed.add(Calendar.DAY_OF_YEAR, 30);		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);

		listing.setEvent( getEvent() );
		listing.setEventId(listing.getEvent().getId());
		listing.setSellerContactId(1000l);
		listing.setSystemStatus("ACTIVE");
		listing.setDeliveryOption(null);
		listing.setFulfillmentMethod(null);
		listing.setIsElectronicDelivery(false);

		helper.populateFulfillmentOptions(listing);
		
	}
	
	
	@Test
	public void populateFulfillmentOptionsWithEITest3() throws Exception 
	{
		window.setFulfillmentMethodId(10L);
		window.setFulfillmentMethodName("UPS");
		window.setFulfillmentTypeName("UPS");
		window.setDeliveryMethodId(9L);
		GregorianCalendar ed1 = new GregorianCalendar();
		window.setStartTime(ed1);
		GregorianCalendar ed2 = new GregorianCalendar();
		ed2.add(Calendar.DAY_OF_YEAR, 30);		
		window.setEndTime(ed2);
		window.setBaseCost(100d);
		windows.add(window);
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEvent( getEvent() );
		listing.setEventId(listing.getEvent().getId());
		listing.setSellerContactId(1000l);
		listing.setSystemStatus("ACTIVE");
		listing.setDeliveryOption(null);
		listing.setFulfillmentMethod(null);

		helper.populateFulfillmentOptions(listing);
		Assert.assertTrue( listing.getFulfillmentMethod() == FulfillmentMethod.UPS, 
				"No deliveryOption, EI, and FM specified, SHOULD default to BARCODE");

		// delivery option provided and default == false
		listing.setEvent( getEvent() );
		listing.setEventId(listing.getEvent().getId());
		listing.setSellerContactId(1000l);		
		listing.setIsElectronicDelivery(false);
		listing.setFulfillmentMethod(null);
		listing.setDeliveryOption(null);
		helper.populateFulfillmentOptions(listing);
		Assert.assertTrue( listing.getFulfillmentMethod() == FulfillmentMethod.UPS, 
				"No deliveryOption, ElectronicDelivery==False, no FM specified, SHOULD default to UPS");
	}
	
	@Test
	public void populateFulfillmentOptionsWithEITest4() throws Exception 
	{
		window.setFulfillmentMethodId(5L);
		window.setFulfillmentMethodName("PDF");
		window.setFulfillmentTypeName("PDF");
		window.setDeliveryMethodId(4L);
		Calendar ed3 = new GregorianCalendar();
		window.setStartTime(ed3);
		Calendar ed4 = new GregorianCalendar();
		ed4.add(Calendar.DAY_OF_YEAR, 30);		
		window.setEndTime(ed4);
		window.setBaseCost(100d);
		windows.add(window);
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);

		listing.setEvent( getEvent() );
		listing.setEventId(listing.getEvent().getId());
		listing.setSellerContactId(1000l);
		listing.setSystemStatus("ACTIVE");
		listing.setDeliveryOption(null);
		listing.setFulfillmentMethod(null);

		helper.populateFulfillmentOptions(listing);
		Assert.assertTrue( listing.getFulfillmentMethod() == FulfillmentMethod.PDF, 
				"No deliveryOption, EI, and FM specified, SHOULD default to BARCODE");
	}

	@Test
	public void testFulfillmentLMSExtension () throws Exception 
	{
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("LMS");
		window.setFulfillmentTypeName("LMS");
		window.setDeliveryMethodId(1l);

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);
		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		FulfillmentWindow window1 = new FulfillmentWindow();
		window1.setFulfillmentMethodId(10L);
		window1.setFulfillmentMethodName("UPS");
		window1.setFulfillmentTypeName("UPS");
		window1.setDeliveryMethodId(9L);
		GregorianCalendar ed1 = new GregorianCalendar();
		window1.setStartTime(ed1);
		GregorianCalendar ed2 = new GregorianCalendar();
		ed2.add(Calendar.DAY_OF_YEAR, 30);		
		window1.setEndTime(ed2);
		window1.setBaseCost(100d);
		windows.add(window1);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentMethod(FulfillmentMethod.UPS);
		listing.setFulfillmentDeliveryMethods("9,");
		listing.setLmsExtensionRequired(true);
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		
		helper.populateFulfillmentOptions(listing);
	}
	
	@Test
    public void testFulfillmentLMSTrustedSeller() throws Exception 
    {
        window.setFulfillmentMethodId(9l);
        window.setFulfillmentMethodName("LMS");
        window.setFulfillmentTypeName("LMS");
        window.setDeliveryMethodId(12L);

        GregorianCalendar ed = new GregorianCalendar();
        ed.add(Calendar.DAY_OF_YEAR, 4);
        
        window.setEndTime(ed);
        window.setBaseCost(100d);
        windows.add(window);
        
        FulfillmentWindow window1 = new FulfillmentWindow();
        window1.setFulfillmentMethodId(10L);
        window1.setFulfillmentMethodName("UPS");
        window1.setFulfillmentTypeName("UPS");
        window1.setDeliveryMethodId(22L);
        GregorianCalendar ed1 = new GregorianCalendar();
        window1.setStartTime(ed1);
        GregorianCalendar ed2 = new GregorianCalendar();
        ed2.add(Calendar.DAY_OF_YEAR, 30);     
        window1.setEndTime(ed2);
        window1.setBaseCost(100d);
        windows.add(window1);
        
        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
        listing.setEventId(1000l);
        listing.setSellerContactId(1000l);
        listing.setDeliveryOption(2);
        listing.setSystemStatus("ACTIVE");
        listing.setFulfillmentMethod(FulfillmentMethod.UPS);
        listing.setFulfillmentDeliveryMethods("9,");
        listing.setTicketMedium(TicketMedium.PAPER.getValue());
        
        helper.populateFulfillmentOptions(listing);
    }
	
	@Test
    public void testFulfillmentLMSTrustedSellerAndShipping() throws Exception 
    {
        window.setFulfillmentMethodId(9l);
        window.setFulfillmentMethodName("LMS");
        window.setFulfillmentTypeName("LMS");
        window.setDeliveryMethodId(12L);

        GregorianCalendar ed = new GregorianCalendar();
        ed.add(Calendar.DAY_OF_YEAR, 30);
        
        window.setEndTime(ed);
        window.setBaseCost(100d);
        windows.add(window);
        
        FulfillmentWindow window1 = new FulfillmentWindow();
        window1.setFulfillmentMethodId(1L);
        window1.setFulfillmentMethodName("UPS");
        window1.setFulfillmentTypeName("UPS");
        window1.setDeliveryMethodId(1L);
        GregorianCalendar ed1 = new GregorianCalendar();
        window1.setStartTime(ed1);
        GregorianCalendar ed2 = new GregorianCalendar();
        ed2.add(Calendar.DAY_OF_YEAR, 4);     
        window1.setEndTime(ed2);
        window1.setBaseCost(100d);
        windows.add(window1);
        
        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
        listing.setEventId(1000l);
        listing.setSellerContactId(1000l);
        listing.setDeliveryOption(2);
        listing.setSystemStatus("ACTIVE");
        listing.setFulfillmentMethod(FulfillmentMethod.UPS);
        listing.setFulfillmentDeliveryMethods("9,");
        listing.setTicketMedium(TicketMedium.PAPER.getValue());
        
        helper.populateFulfillmentOptions(listing);
    }
	
	@Test
	public void CalculateEndDate_Test1() throws Exception {
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("LMS");
		window.setFulfillmentTypeName("LMS");
		window.setDeliveryMethodId(9L);
		GregorianCalendar ed = new GregorianCalendar();
		window.setStartTime(ed);
		ed.add(Calendar.DAY_OF_YEAR, 4);		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("PDF");
		window.setDeliveryMethodId(4L);
		ed = new GregorianCalendar();
		window.setStartTime(ed);
		ed.add(Calendar.DAY_OF_YEAR, 4);		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("Barcode");
		window.setDeliveryMethodId(2L);
		ed = new GregorianCalendar();
		window.setStartTime(ed);
		ed.add(Calendar.DAY_OF_YEAR, 4);		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		helper.calculateSaleEndDate(listing, null);
	}
	
	@Test
	public void CalculateEndDate_Test2() throws Exception {
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("LMS");
		window.setDeliveryMethodId(9L);
		GregorianCalendar ed = new GregorianCalendar();
		window.setStartTime(ed);
		ed.add(Calendar.DAY_OF_YEAR, 4);		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("PDF");
		window.setDeliveryMethodId(4L);
		ed = new GregorianCalendar();
		window.setStartTime(ed);
		ed.add(Calendar.DAY_OF_YEAR, 4);		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("Barcode");
		window.setDeliveryMethodId(2L);
		ed = new GregorianCalendar();
		window.setStartTime(ed);
		ed.add(Calendar.DAY_OF_YEAR, 4);		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentMethod(FulfillmentMethod.PDF);
		helper.calculateSaleEndDate(listing, null);
	}
	
	@Test
	public void CalculateEndDate_Test3() throws Exception {
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("LMS");
		window.setDeliveryMethodId(9L);
		GregorianCalendar ed = new GregorianCalendar();
		window.setStartTime(ed);
		ed.add(Calendar.DAY_OF_YEAR, 4);		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("PDF");
		window.setDeliveryMethodId(4L);
		ed = new GregorianCalendar();
		window.setStartTime(ed);
		ed.add(Calendar.DAY_OF_YEAR, 4);		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		window.setFulfillmentMethodId(9l);
		window.setFulfillmentMethodName("Barcode");
		window.setDeliveryMethodId(2L);
		ed = new GregorianCalendar();
		window.setStartTime(ed);
		ed.add(Calendar.DAY_OF_YEAR, 4);		
		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);
		
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setFulfillmentDeliveryMethods("17,5.25,,2015-09-26T17:00:00Z|10,22,5.25,,2015-09-24T19:00:00Z");
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentMethod(FulfillmentMethod.BARCODE);
		helper.calculateSaleEndDate(listing, null);
	}
	
	@Test
	public void testSaleEndDateMethodForFulfillmentMethods() throws Exception {
		// Set all fulfillment methods in the Window.
		for(FulfillmentMethod method : FulfillmentMethod.values()) {
			FulfillmentWindow window = new FulfillmentWindow();
			window.setFulfillmentMethodId(9l);
			window.setFulfillmentMethodName(method.getName());
			window.setDeliveryMethodId(9L);
			GregorianCalendar ed = new GregorianCalendar();
			window.setStartTime(ed);
			ed.add(Calendar.DAY_OF_YEAR, 4);		
			window.setEndTime(ed);
			window.setBaseCost(100d);
			windows.add(window);			
		}
				
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setFulfillmentDeliveryMethods("7,17,5.25,,2015-09-26T17:00:00Z|10,22,5.25,,2015-09-24T19:00:00Z");
		listing.setSystemStatus("ACTIVE");
		
		// Now set the fulfillment method in the listing as each available enum value of FulfillmentMethod.
		for(FulfillmentMethod method : FulfillmentMethod.values()) {
			listing.setFulfillmentMethod(method);
			Calendar saleEndDate = helper.calculateSaleEndDate(listing, null);
			
			// Sale End Date should not be null.
			Assert.assertNotNull(saleEndDate, "Sale End Date should not be null");
		}
	}
		
	//SELLAPI-1300 09/18/15 START
	@Test
	public void testSaleEndDateMethodForFulfillmentMethods02() throws Exception {
		// Set all fulfillment methods in the Window.
		for(FulfillmentMethod method : FulfillmentMethod.values()) {
			FulfillmentWindow window = new FulfillmentWindow();
			window.setFulfillmentMethodId(9l);
			window.setFulfillmentMethodName(method.getName());
			window.setDeliveryMethodId(9L);
			GregorianCalendar ed = new GregorianCalendar();
			window.setStartTime(ed);
			ed.add(Calendar.DAY_OF_YEAR, 4);		
			window.setEndTime(ed);
			window.setBaseCost(100d);
			windows.add(window);			
		}
				
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setFulfillmentMethod(FulfillmentMethod.UPS);
		listing.setFulfillmentDeliveryMethods("7,17,5.25,,2015-09-26T17:00:00Z|10,22,5.25,,2015-09-24T19:00:00Z");
		listing.setSystemStatus("ACTIVE");
		
		// Now set the fulfillment method in the listing as each available enum value of FulfillmentMethod.
		for(FulfillmentMethod method : FulfillmentMethod.values()) {
			listing.setFulfillmentMethod(method);
			Calendar saleEndDate = helper.calculateSaleEndDate(listing, null);
			
			// Sale End Date should not be null.
			Assert.assertNotNull(saleEndDate, "Sale End Date should not be null");
		}
	}	
	//SELLAPI-1300 09/18/15 END
	
	@Test
	public void testPopulateFulfillmentOptionsFlashNonInstant() {
		window.setFulfillmentMethodId(14l);
		window.setFulfillmentMethodName("FlashSeat - NonInstant");
		window.setDeliveryMethodId(45l);
		window.setTicketMedium("FlashSeat");

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);

		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);

		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setFulfillmentMethod(FulfillmentMethod.FLASHSEAT);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		
		helper.populateFulfillmentOptions(listing);
		Assert.assertNotNull(listing.getEndDate());
		Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
		Assert.assertTrue(listing.getConfirmOption() == ConfirmOptionEnum.MANUAL_CONFIRM.getConfirmStatus());
		Assert.assertTrue(listing.getTicketMedium() == TicketMedium.FLASHSEAT.getValue());
	}
	
	@Test
	public void testPopulateFulfillmentOptionsFlashInstant() {
		window.setFulfillmentMethodId(13l);
		window.setFulfillmentMethodName("FlashSeat - PreDelivery");
		window.setDeliveryMethodId(40l);
		window.setTicketMedium("FlashSeat");

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);

		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);

		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setFulfillmentMethod(FulfillmentMethod.FLASHSEAT);
		listing.setDeliveryOption(1);
		listing.setSystemStatus("ACTIVE");
		
		helper.populateFulfillmentOptions(listing);
		Assert.assertNotNull(listing.getEndDate());
		Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
		Assert.assertTrue(listing.getConfirmOption() == ConfirmOptionEnum.AUTO_CONFIRM.getConfirmStatus());
		Assert.assertTrue(listing.getTicketMedium() == TicketMedium.FLASHSEAT.getValue());
	}
	
	@Test
	public void testPopulateFulfillmentOptionsFlashTransfer() {
		window.setFulfillmentMethodId(19l);
		window.setFulfillmentMethodName("Flash Transfer");
		window.setDeliveryMethodId(44l);
		window.setTicketMedium("ExtFlash");

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);

		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);

		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setFulfillmentMethod(FulfillmentMethod.FLASHSEAT);
		listing.setTicketMedium(TicketMedium.FLASHSEAT.getValue());
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		
		helper.populateFulfillmentOptions(listing);
		Assert.assertNotNull(listing.getEndDate());
		Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
		Assert.assertTrue(listing.getConfirmOption() == ConfirmOptionEnum.MANUAL_CONFIRM.getConfirmStatus());
		Assert.assertTrue(listing.getTicketMedium() == TicketMedium.EXTFLASH.getValue());
	}
	
	@Test
	public void testPopulateFulfillmentOptionsMobileTransfer() {
		window.setFulfillmentMethodId(18l);
		window.setFulfillmentMethodName("Mobile Transfer");
		window.setDeliveryMethodId(43l);
		window.setTicketMedium("ExtMobile");

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);

		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);

		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setFulfillmentMethod(FulfillmentMethod.MOBILETRANSFER);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		
		helper.populateFulfillmentOptions(listing);
		Assert.assertNotNull(listing.getEndDate());
		Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
		Assert.assertTrue(listing.getConfirmOption() == ConfirmOptionEnum.MANUAL_CONFIRM.getConfirmStatus());
		Assert.assertTrue(listing.getTicketMedium() == TicketMedium.EXTMOBILE.getValue());
	}
	
	@Test
	public void testPopulateFulfillmentOptionsMobile() {
		window.setFulfillmentMethodId(21l);
		window.setFulfillmentMethodName("Mobile");
		window.setDeliveryMethodId(42l);
		window.setTicketMedium("Mobile");

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);

		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);

		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setFulfillmentMethod(FulfillmentMethod.MOBILE);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		
		helper.populateFulfillmentOptions(listing);
		Assert.assertNotNull(listing.getEndDate());
		Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
		Assert.assertTrue(listing.getConfirmOption() == ConfirmOptionEnum.MANUAL_CONFIRM.getConfirmStatus());
		Assert.assertTrue(listing.getTicketMedium() == TicketMedium.MOBILE.getValue());
	}
	
	@Test
	public void testPopulateFulfillmentOptionsMobileInstant() {
		window.setFulfillmentMethodId(20l);
		window.setFulfillmentMethodName("Mobile - Instant");
		window.setDeliveryMethodId(41l);
		window.setTicketMedium("Mobile");

		GregorianCalendar ed = new GregorianCalendar();
		ed.add(Calendar.DAY_OF_YEAR, 4);

		window.setEndTime(ed);
		window.setBaseCost(100d);
		windows.add(window);

		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
		
		listing.setEventId(1000l);
		listing.setSellerContactId(1000l);
		listing.setFulfillmentMethod(FulfillmentMethod.MOBILE);
		listing.setDeliveryOption(1);
		listing.setSystemStatus("ACTIVE");
		
		helper.populateFulfillmentOptions(listing);
		Assert.assertNotNull(listing.getEndDate());
		Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
		Assert.assertTrue(listing.getConfirmOption() == ConfirmOptionEnum.AUTO_CONFIRM.getConfirmStatus());
		Assert.assertTrue(listing.getTicketMedium() == TicketMedium.MOBILE.getValue());
	}
	
	@Test
    public void testPopulateFulfillmentOptionsMobileWithoutWindow1() {
        window.setFulfillmentMethodId(3l);
        window.setFulfillmentMethodName("Barcode");
        window.setDeliveryMethodId(42l);
        window.setTicketMedium("Barcode");

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 2);
        window.setEndTime(cal);
        window.setBaseCost(100d);
        windows.add(window);
        
        FulfillmentWindow window1 = new FulfillmentWindow();
        window1.setFulfillmentMethodId(3l);
        window1.setFulfillmentMethodName("Barcode");
        window1.setDeliveryMethodId(43l);

        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_YEAR, 4);
        window1.setEndTime(cal1);
        window1.setBaseCost(100d);
        windows.add(window1);
        
        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
        
        listing.setEventId(1000l);
        listing.setSellerContactId(1000l);
        listing.setFulfillmentMethod(FulfillmentMethod.MOBILE);
        listing.setDeliveryOption(2);
        listing.setSystemStatus("ACTIVE");
        
        helper.populateFulfillmentOptions(listing);
        Assert.assertNotNull(listing.getEndDate());
        Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
        Assert.assertTrue(listing.getConfirmOption() == ConfirmOptionEnum.MANUAL_CONFIRM.getConfirmStatus());
        Assert.assertTrue(listing.getTicketMedium() == TicketMedium.BARCODE.getValue());
    }
	
	@Test
    public void testPopulateFulfillmentOptionsMobileWithoutWindow2() {
        window.setFulfillmentMethodId(2l);
        window.setFulfillmentMethodName("Barcode - PreDelivery (Non-STH)");
        window.setDeliveryMethodId(2l);
        window.setTicketMedium("Barcode");

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 2);
        window.setEndTime(cal);
        window.setBaseCost(100d);
        windows.add(window);
        
        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
        
        listing.setEventId(1000l);
        listing.setSellerContactId(1000l);
        listing.setFulfillmentMethod(FulfillmentMethod.MOBILE);
        listing.setDeliveryOption(2);
        listing.setSystemStatus("ACTIVE");
        
        helper.populateFulfillmentOptions(listing);
        Assert.assertNotNull(listing.getEndDate());
        Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
        Assert.assertTrue(listing.getConfirmOption() == ConfirmOptionEnum.MANUAL_CONFIRM.getConfirmStatus());
        Assert.assertTrue(listing.getTicketMedium() == TicketMedium.BARCODE.getValue());
        Assert.assertTrue(listing.getSystemStatus() == ListingStatus.INCOMPLETE.toString());
    }
	
	@Test
    public void testPopulateFulfillmentOptionsEventCard() {
        window.setFulfillmentMethodId(10l);
        window.setFulfillmentMethodName("UPS");
        window.setDeliveryMethodId(23l);
        window.setTicketMedium("EventCard");

        GregorianCalendar ed = new GregorianCalendar();
        ed.add(Calendar.DAY_OF_YEAR, 4);

        window.setEndTime(ed);
        window.setBaseCost(100d);
        windows.add(window);

        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
        
        listing.setEventId(1000l);
        listing.setSellerContactId(1000l);
        listing.setFulfillmentMethod(FulfillmentMethod.EVENTCARD);
        listing.setDeliveryOption(2);
        listing.setSystemStatus("ACTIVE");
        
        helper.populateFulfillmentOptions(listing);
        Assert.assertNotNull(listing.getEndDate());
        Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
        Assert.assertTrue(listing.getConfirmOption() == ConfirmOptionEnum.MANUAL_CONFIRM.getConfirmStatus());
        Assert.assertTrue(listing.getTicketMedium() == TicketMedium.EVENTCARD.getValue());
    }
	
	@Test
    public void testPopulateFulfillmentOptionsEventCardWithoutWindow() {
        window.setFulfillmentMethodId(7l);
        window.setFulfillmentMethodName("LMS");
        window.setDeliveryMethodId(23l);
        window.setFulfillmentTypeName("LMS");
        window.setTicketMedium("PAPER");

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 2);
        window.setEndTime(cal);
        window.setBaseCost(100d);
        windows.add(window);
        
        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
        
        listing.setEventId(1000l);
        listing.setSellerContactId(1000l);
        listing.setFulfillmentMethod(FulfillmentMethod.EVENTCARD);
        listing.setTicketMedium(TicketMedium.EVENTCARD.getValue());
        listing.setDeliveryOption(2);
        listing.setSystemStatus("ACTIVE");
        
        helper.populateFulfillmentOptions(listing);
        Assert.assertNotNull(listing.getEndDate());
        Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
        Assert.assertTrue(listing.getConfirmOption() == ConfirmOptionEnum.MANUAL_CONFIRM.getConfirmStatus());
        Assert.assertTrue(listing.getTicketMedium() == TicketMedium.PAPER.getValue());
    }
	
	@Test
    public void testPopulateFulfillmentOptionsLocalDelivery() {
        window.setFulfillmentMethodId(17l);
        window.setFulfillmentMethodName("LOCAL DELIVERY");
        window.setDeliveryMethodId(49l);
        window.setTicketMedium("PAPER");

        GregorianCalendar ed = new GregorianCalendar();
        ed.add(Calendar.DAY_OF_YEAR, 4);

        window.setEndTime(ed);
        window.setBaseCost(100d);
        windows.add(window);

        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
        
        listing.setEventId(1000l);
        listing.setSellerContactId(1000l);
        listing.setFulfillmentMethod(FulfillmentMethod.LOCALDELIVERY);
        listing.setDeliveryOption(2);
        listing.setSystemStatus("ACTIVE");
        
        helper.populateFulfillmentOptions(listing);
        Assert.assertNotNull(listing.getEndDate());
        Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
        Assert.assertTrue(listing.getConfirmOption() == ConfirmOptionEnum.MANUAL_CONFIRM.getConfirmStatus());
        Assert.assertTrue(listing.getTicketMedium() == TicketMedium.PAPER.getValue());
    }
	
	@Test
    public void testPopulateFulfillmentOptionsLocalDeliveryWithoutWindow() {
        window.setFulfillmentMethodId(10l);
        window.setFulfillmentMethodName("UPS");
        window.setDeliveryMethodId(24l);
        window.setTicketMedium("PAPER");
        window.setFulfillmentTypeName("UPS");

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 4);
        window.setEndTime(cal);
        window.setBaseCost(100d);
        windows.add(window);
        
        FulfillmentWindow window1 = new FulfillmentWindow();
        window1.setFulfillmentMethodId(10l);
        window1.setFulfillmentMethodName("UPS");
        window1.setDeliveryMethodId(23l);
        window1.setTicketMedium("PAPER");
        window1.setFulfillmentTypeName("UPS");

        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_YEAR, 2);
        window1.setEndTime(cal1);
        window1.setBaseCost(100d);
        windows.add(window1);
        
        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(new EventFulfillmentWindowResponse());
        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindows(Matchers.any(EventFulfillmentWindowResponse.class))).thenReturn(windows);
        
        listing.setEventId(1000l);
        listing.setSellerContactId(1000l);
        listing.setFulfillmentMethod(FulfillmentMethod.LOCALDELIVERY);
        listing.setDeliveryOption(2);
        listing.setSystemStatus("ACTIVE");
        
        helper.populateFulfillmentOptions(listing);
        Assert.assertNotNull(listing.getEndDate());
        Assert.assertNotNull(listing.getFulfillmentDeliveryMethods());
        Assert.assertTrue(listing.getConfirmOption() == ConfirmOptionEnum.MANUAL_CONFIRM.getConfirmStatus());
        Assert.assertTrue(listing.getTicketMedium() == TicketMedium.PAPER.getValue());
    }
	
	private String getResponseJsonStrWithNonArrayListingFulfillmentWindow () throws Exception {
		JSONObject responseJson = new JSONObject ();
		JSONObject listing = new JSONObject ().put("Id", 1L);
		responseJson.put("ListingFulfillmentWindow", new JSONObject().put("Listing", listing));
		listing.put("FulfillmentWindow", buildFulfillmentWindow ("FedEx Priority Overnight", new Long(1), "FedEx", new Double(19.95), "USD", 
				new Long(2), "FedEx Priority Overnight - Continental US", new Long(3), "FedEx"));
		
		return responseJson.toString();
	}
	
	private String getResponseJsonStrWithNullListingFulfillmentWindow () throws Exception {
		JSONObject responseJson = new JSONObject ();
		JSONObject listing = new JSONObject ().put("Id", 1L);
		responseJson.put("ListingFulfillmentWindow", new JSONObject().put("Listing", listing));
		//responseJson.put("ListingFulfillmentWindow", new JSONObject());
		
		return responseJson.toString();
	}
	
	private String getResponseJsonStr () throws Exception {
		JSONObject responseJson = new JSONObject ();
		JSONObject listing = new JSONObject ().put("Id", 1L);
		responseJson.put("ListingFulfillmentWindow", new JSONObject().put("Listing", listing));
		JSONArray fulfillmentWindowArr = new JSONArray ();
		listing.put("FulfillmentWindow", fulfillmentWindowArr);
		
		listing.put("FulfillmentWindow", fulfillmentWindowArr);
		
		fulfillmentWindowArr.put(buildFulfillmentWindow ("FedEx Priority Overnight", new Long(1), "FedEx", new Double(19.95), "USD", 
				new Long(2), "FedEx Priority Overnight - Continental US", new Long(3), "FedEx"));
		fulfillmentWindowArr.put(buildFulfillmentWindow ("FedEx Standard Overnight", new Long(3), "FedEx", new Double(16.95), "USD", 
				new Long(4), "FedEx Standard Overnight", new Long(6), "FedEx"));
		fulfillmentWindowArr.put(buildFulfillmentWindow ("FedEx Two Day", new Long(3), "FedEx", new Double(16.95), "USD", 
				new Long(4), "FedEx Two Day", new Long(6), "FedEx"));
		fulfillmentWindowArr.put(buildFulfillmentWindow ("FedEx Intra Canada Overnight", new Long(3), "FedEx", new Double(16.95), "USD", 
				new Long(4), "FedEx Intra Canada Overnight", new Long(6), "FedEx"));
		fulfillmentWindowArr.put(buildFulfillmentWindow ("Pickup - Event Day", new Long(4), "Pickup", new Double(15.00), "USD", 
				new Long(17), "Pickup - Event Day", new Long(7), "LMS"));
		fulfillmentWindowArr.put(buildFulfillmentWindow ("Pickup", new Long(4), "Pickup", new Double(15.00), "USD", 
				new Long(17), "Pickup", new Long(7), "LMS"));
		fulfillmentWindowArr.put(buildFulfillmentWindow ("Off-site Pickup", new Long(4), "Pickup", new Double(15.00), "USD", 
				new Long(17), "Off-site Pickup", new Long(7), "LMS"));
		fulfillmentWindowArr.put(buildFulfillmentWindow ("Email", new Long(4), "Electronic", new Double(15.00), "USD", 
				new Long(17), "Email", new Long(7), "Electronic"));
		fulfillmentWindowArr.put(buildFulfillmentWindow ("Courier", new Long(4), "", new Double(15.00), "USD", 
				new Long(17), "Courier", new Long(7), ""));
		fulfillmentWindowArr.put(buildFulfillmentWindow ("UPS Worldwide Saver - From US", new Long(5), "UPS", new Double(19.00), "USD", 
				new Long(2), "UPS Worldwide Saver - From US", new Long(3), "UPS"));
		fulfillmentWindowArr.put(buildFulfillmentWindow ("Electronic - Download", new Long(6), "Electronic", new Double(4.95), "USD", 
				new Long(5), "Electronic - Download", new Long(8), "Electronic"));
		fulfillmentWindowArr.put(buildFulfillmentWindow ("Electronic - Instant Download", new Long(7), "Electronic Instant Download", new Double(5.95), "USD", 
				new Long(2), "Electronic - Instant Download", new Long(3), "Electronic Instant Download"));
		
		return responseJson.toString();
	}
	
	private JSONObject buildFulfillmentWindow (
			String deliveryMethodDisplayName, Long deliveryTypeId, String deliveryTypeName, Double baseCostDouble, 
			String baseCostCurrency, Long deliveryMethodId, String deliveryMethodName, Long fulfillmentMethodId,
			String fulfillmentMethodName) throws Exception {
		JSONObject fulfillmentWindow = new JSONObject ().put("DeliveryMethodDisplayName",deliveryMethodDisplayName);
		
		JSONObject deliveryType = new JSONObject ().put("Id", deliveryTypeId);
		deliveryType.put("Name", deliveryTypeName);
		
		JSONObject baseCost = new JSONObject ().put("Amount", baseCostDouble);
		baseCost.put("Currency", baseCostCurrency);

		JSONObject deliveryMethod = new JSONObject ().put("Id", deliveryMethodId);
		deliveryMethod.put("Name", deliveryMethodName);
		deliveryMethod.put("DeliveryType", deliveryType);
		
		JSONObject fulfillmentMethod = new JSONObject ().put("Id", fulfillmentMethodId);
		fulfillmentMethod.put("Name", fulfillmentMethodName);
		//fulfillmentMethod.put("FulfillmentType", deliveryType);
		fulfillmentWindow.put("FulfillmentMethod", fulfillmentMethod);
		fulfillmentWindow.put("BaseCost", baseCost);
		fulfillmentWindow.put("DeliveryMethod", deliveryMethod);
		
		return fulfillmentWindow;
	}

	private Event getEvent(){
		Event event = new Event();
		event.setId(1000l);
		event.setActive(true);
		event.setDescription("Event description");
		
		// event date 2 months from now 
		GregorianCalendar cal = new GregorianCalendar();
		cal.add (Calendar.MONTH, 2);
		event.setEventDate(cal);
		
		// early inhand 5 days from today
		GregorianCalendar eih = new GregorianCalendar();
		eih.add(Calendar.DAY_OF_YEAR, 5);
		event.setEarliestPossibleInhandDate(eih);

		// latest inhand 50 days from today
		GregorianCalendar lih = new GregorianCalendar();
		lih.add(Calendar.DAY_OF_YEAR, 50);
		
		event.setLatestPossibleInhandDate(lih);
		event.setCurrency(Currency.getInstance("USD"));
		event.setJdkTimeZone(TimeZone.getDefault());
		
		//event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
		return event;
	}
	
	@Test
	public void testGetDeliveryMethodsForListingId() {
	    ListingFulfillmentWindowResponse lfwResponse = new ListingFulfillmentWindowResponse();
	    Collection<FulfillmentWindowResponse> fulfillmentWindows = new ArrayList<FulfillmentWindowResponse>();
	    FulfillmentWindowResponse fwr = new FulfillmentWindowResponse();
	    
	    FulfillmentMethodResponse fulfillmentMethod = new FulfillmentMethodResponse();
	    fulfillmentMethod.setId(10L);
	    fulfillmentMethod.setName("UPS");
	    fulfillmentMethod.setFulfillmentTypeId(6L);
	    fulfillmentMethod.setFulfillmentTypeName("UPS");
	    fwr.setFulfillmentMethod(fulfillmentMethod);
	    
	    DeliveryMethodResponse deliveryMethod = new DeliveryMethodResponse();
	    deliveryMethod.setId(24L);
	    deliveryMethod.setName("UPS 2nd Day Air - Intra-USA");
	    deliveryMethod.setDeliveryTypeId(5L);
	    deliveryMethod.setDeliveryTypeName("UPS");
	    fwr.setDeliveryMethod(deliveryMethod);
	    fwr.setDeliveryMethodDisplayName("UPS 2 Day Air");
	    
	    fulfillmentWindows.add(fwr);
	    lfwResponse.setFulfillmentWindows(fulfillmentWindows);
		Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShapeForListing(Mockito.anyLong(), Mockito.anyLong())).thenReturn(lfwResponse);
		inHandDate.roll(Calendar.DATE, true);
				
		helper.getDeliveryMethodsForListingId(listingId, buyerContactId, inHandDate, true, event);
		helper.getDeliveryMethodsForListingId(listingId, buyerContactId, null, true, event);
			
		// with inHand as false
		helper.getDeliveryMethodsForListingId(listingId, buyerContactId, inHandDate, false, event);
	}
	
	@Test
    public void testGetDeliveryMethodsForListingIdNoFulfillmentMethods() {
        Mockito.when(fulfillmentServiceAdapter.getFulfillmentWindowsShapeForListing(Mockito.anyLong(), Mockito.anyLong())).thenReturn(new ListingFulfillmentWindowResponse());
        inHandDate.roll(Calendar.DATE, true);
                
        helper.getDeliveryMethodsForListingId(listingId, buyerContactId, inHandDate, true, event);
        helper.getDeliveryMethodsForListingId(listingId, buyerContactId, null, true, event);
            
        // with inHand as false
        helper.getDeliveryMethodsForListingId(listingId, buyerContactId, inHandDate, false, event);
    }
	
	@Test
	public void testValidateAndSetInHandDate() {
		listing.setInhandDate(Calendar.getInstance());
		listing.setEventId(1123L);
		
		Event event = new Event();
		event.setId(1123L);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		event.setDescription("Test Event");
		Calendar eventDate = Calendar.getInstance();
		eventDate.add(Calendar.MONTH, 1);
		event.setEventDate(eventDate);
		listing.setEvent(event);
		
		Calendar eihDate = Calendar.getInstance();
		eihDate.add(Calendar.DAY_OF_YEAR, 1);
		Calendar lihDate = Calendar.getInstance();
		lihDate.add(Calendar.DAY_OF_YEAR, 25);
		helper.validateAndSetInHandDate(listing, eihDate, lihDate);
	}
	
	@Test
	public void testValidateAndSetInHandDateNull1() {
		listing.setEventId(1123L);
		
		Event event = new Event();
		event.setId(1123L);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		event.setDescription("Test Event");
		Calendar eventDate = Calendar.getInstance();
		eventDate.add(Calendar.MONTH, 1);
		event.setEventDate(eventDate);
		listing.setEvent(event);
		
		helper.validateAndSetInHandDate(listing, null, Calendar.getInstance());
	}
	
	@Test
	public void testValidateAndSetInHandDateNull2() {
		listing.setEventId(1123L);
		
		Event event = new Event();
		event.setId(1123L);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		event.setDescription("Test Event");
		Calendar eventDate = Calendar.getInstance();
		eventDate.add(Calendar.MONTH, 1);
		event.setEventDate(eventDate);
		listing.setEvent(event);
		
		Calendar lihDate = Calendar.getInstance();
		lihDate.add(Calendar.DAY_OF_YEAR, 1);
		
		helper.validateAndSetInHandDate(listing, null, lihDate);
	}
	
	@Test(expectedExceptions={ListingBusinessException.class})
	public void testValidateAndSetInHandDateBusinesserrors1() {
		Calendar inhandDate = Calendar.getInstance();
		inhandDate.add(Calendar.MONTH, 2);
		listing.setInhandDate(inhandDate);
		listing.setEventId(1123L);
		
		Event event = new Event();
		event.setId(1123L);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		event.setDescription("Test Event");
		event.setEventDate(Calendar.getInstance());
		listing.setEvent(event);
		
		helper.validateAndSetInHandDate(listing, null, Calendar.getInstance());
	}

	// changed test case for EXTSELL-4 adjust EHD to LIHD and accept listing
	@Test(expectedExceptions={ListingBusinessException.class})
	public void testValidateAndSetInHandDateBusinesserrors2() {
		Calendar inhandDate = Calendar.getInstance();
		inhandDate.add(Calendar.MONTH, 2);
		listing.setInhandDate(inhandDate);
		listing.setEventId(1123L);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		listing.setListPrice(new Money("100"));
		Event event = new Event();
		event.setId(1123L);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		event.setDescription("Test Event");
		Calendar eventDate = Calendar.getInstance();
		eventDate.add(Calendar.MONTH, 3);
		event.setEventDate(eventDate);
		listing.setEvent(event);
		
		Calendar eihDate = Calendar.getInstance();
		Calendar lihDate = Calendar.getInstance();
		lihDate.add(Calendar.MONTH, 1);
		
		helper.validateAndSetInHandDate(listing, eihDate, lihDate);
	}
	
	// changed test case for EXTSELL-56 adjust EHD to LIHD and accept listing
		@Test
		public void testValidateAndSetInHandDateErrors() throws Exception {
			Calendar inhandDate = Calendar.getInstance();
			inhandDate.add(Calendar.MONTH, 2);
			listing.setInhandDate(inhandDate);
			listing.setAdjustInhandDate(true);
			listing.setEventId(1123L);
			listing.setSellerContactId(1000l);
			listing.setDeliveryOption(2);
			listing.setSystemStatus("ACTIVE");
			listing.setFulfillmentMethod(FulfillmentMethod.LMS);
			Event event = new Event();
			event.setId(1123L);
			event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
			event.setDescription("Test Event");
			Calendar eventDate = Calendar.getInstance();
			eventDate.add(Calendar.MONTH, 4);
			event.setEventDate(eventDate);
			listing.setEvent(event);

			Calendar eihDate = Calendar.getInstance();
			Calendar lihDate = Calendar.getInstance();
			lihDate.add(Calendar.MONTH, 1);
			invokeVoidPrivateMethod("buildLogDataString",listing, null, null, null);
		}
		
	@Test
	public void testValidateAndSetInHandDateIfAdjustInHandDateTrue() {
		Calendar inhandDate = Calendar.getInstance();
		inhandDate.add(Calendar.MONTH, 2);
		listing.setInhandDate(inhandDate);
		listing.setEventId(1123L);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		listing.setListPrice(new Money("100"));
		listing.setAdjustInhandDate(true);
		Event event = new Event();
		event.setId(1123L);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		event.setDescription("Test Event");
		Calendar eventDate = Calendar.getInstance();
		eventDate.add(Calendar.MONTH, 3);
		event.setEventDate(eventDate);
		listing.setEvent(event);
		
		Calendar eihDate = Calendar.getInstance();
		Calendar lihDate = Calendar.getInstance();
		lihDate.add(Calendar.MONTH, 1);
		
		helper.validateAndSetInHandDate(listing, eihDate, lihDate);
	}
	
	@Test
	public void testValidateAndSetInHandDateIfAdjustInHandDateTruePriceNull() {
		Calendar inhandDate = Calendar.getInstance();
		inhandDate.add(Calendar.MONTH, 2);
		listing.setInhandDate(inhandDate);
		listing.setEventId(1123L);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		listing.setListPrice(null);
		listing.setAdjustInhandDate(true);
		Event event = new Event();
		event.setId(1123L);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		event.setDescription("Test Event");
		Calendar eventDate = Calendar.getInstance();
		eventDate.add(Calendar.MONTH, 3);
		event.setEventDate(eventDate);
		listing.setEvent(event);
		
		Calendar eihDate = Calendar.getInstance();
		Calendar lihDate = Calendar.getInstance();
		lihDate.add(Calendar.MONTH, 1);
		
		helper.validateAndSetInHandDate(listing, eihDate, lihDate);
	}
	
	
	@Test
	public void testIsTypeOfPaperTicket() {
	    TicketMedium[] ticketMediums  = TicketMedium.values();
	    for(TicketMedium ticketMedium : ticketMediums){
	    	listing.setTicketMedium(ticketMedium.getValue());
	    	boolean result = helper.isShipping(listing);
	    	if(ticketMedium.getValue() == TicketMedium.EVENTCARD.getValue() ||
	    		ticketMedium.getValue() == TicketMedium.PAPER.getValue() ||
	    		ticketMedium.getValue() == TicketMedium.SEASONCARD.getValue() ||
	    		ticketMedium.getValue() == TicketMedium.RFID.getValue() ||
	    	    ticketMedium.getValue() == TicketMedium.WRISTBAND.getValue() ||
	    		ticketMedium.getValue() == TicketMedium.GUESTLIST.getValue()){
	    		Assert.assertEquals(result, true);
	    	}else{
	    		Assert.assertEquals(result, false);
	    	}
	    	
	    }
	}
	
	
	@Test
	public void testIsTypeOfPaperTicketWithTicketMediumNull() {
	    listing.setTicketMedium(null);
	    Assert.assertEquals(helper.isShipping(listing), false);	    	
	}
	
	// changed test case for EXTSELL-4 adjust EHD to LIHD and accept listing
	@Test(expectedExceptions={ListingBusinessException.class})
	public void testValidateAndSetInHandDateBusinesserrors3() {
		Calendar inhandDate = Calendar.getInstance();
		inhandDate.add(Calendar.DAY_OF_YEAR, 2);
		listing.setInhandDate(inhandDate);
		listing.setEventId(1123L);
		listing.setSellerContactId(1000l);
		listing.setDeliveryOption(2);
		listing.setSystemStatus("ACTIVE");
		listing.setFulfillmentMethod(FulfillmentMethod.LMS);
		listing.setListPrice(new Money("100")); 
		Event event = new Event();
		event.setId(1123L);
		event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
		event.setDescription("Test Event");
		Calendar eventDate = Calendar.getInstance();
		eventDate.add(Calendar.DAY_OF_YEAR, 3);
		event.setEventDate(eventDate);
		listing.setEvent(event);
		
		Calendar eihDate = Calendar.getInstance();
		Calendar lihDate = Calendar.getInstance();
		lihDate.add(Calendar.DAY_OF_YEAR, -1);
		
		helper.validateAndSetInHandDate(listing, eihDate, lihDate);
	}

		// changed test case for EXTSELL-49 adjust EHD to LIHD and accept listing
		@Test
		public void testValidateAndSetInHandDateElseBlockIsAdjustTrue() {
			Calendar inhandDate = Calendar.getInstance();
			inhandDate.add(Calendar.DAY_OF_YEAR, 2);
			listing.setInhandDate(inhandDate);
			listing.setEventId(1123L);
			listing.setSellerContactId(1000l);
			listing.setDeliveryOption(2);
			listing.setSystemStatus("ACTIVE");
			listing.setFulfillmentMethod(FulfillmentMethod.LMS);
			listing.setListPrice(new Money("100")); 
			listing.setAdjustInhandDate(true);
			Event event = new Event();
			event.setId(1123L);
			event.setJdkTimeZone(TimeZone.getTimeZone("PST"));
			event.setDescription("Test Event");
			Calendar eventDate = Calendar.getInstance();
			eventDate.add(Calendar.DAY_OF_YEAR, 3);
			event.setEventDate(eventDate);
			listing.setEvent(event);
			
			Calendar eihDate = Calendar.getInstance();
			Calendar lihDate = Calendar.getInstance();
			lihDate.add(Calendar.DAY_OF_YEAR, -1);
			
			helper.validateAndSetInHandDate(listing, eihDate, lihDate);
		}
		
		@Test
		public void testGetContextFromHeaderServiceContextNull() throws Exception{
			SHThreadLocals.set(SHServiceContext.SERVICE_CONTEXT_HEADER,null);
			Assert.assertTrue(invokePrivateMethod("getContextFromHeader").equals(" sellerId=NA appName=NA"));
		}
		
		@Test
		public void testGetContextFromHeaderServiceContextNotNull() throws Exception{
			SHServiceContext serviceContext = new SHServiceContext();
			SHThreadLocals.set(SHServiceContext.SERVICE_CONTEXT_HEADER,
					serviceContext);
			ExtendedSecurityContextImpl esc = new ExtendedSecurityContextImpl();
			esc.setUserId("1234");
			esc.setApplicationName("appName");
			serviceContext.setExtendedSecurityContext(esc);
			Assert.assertTrue(invokePrivateMethod("getContextFromHeader").equals(" sellerId=1234 appName=appName"));
		}
		
		@Test
		public void testGetContextFromHeaderExtendedServiceContextNotNull() throws Exception{
			SHServiceContext serviceContext = new SHServiceContext();
			SHThreadLocals.set(SHServiceContext.SERVICE_CONTEXT_HEADER,
					serviceContext);
			serviceContext.setExtendedSecurityContext(null);
			invokePrivateMethod("getContextFromHeader");
			Assert.assertTrue(invokePrivateMethod("getContextFromHeader").equals(""));
		}
		
		private String invokePrivateMethod(String methodName, Object... parameters) throws Exception {
			   Method privateMethod = getPrivateMethod(methodName);
			   privateMethod.setAccessible(true);
			   return (String) privateMethod.invoke(helper, parameters);
			 }
		private void invokeVoidPrivateMethod(String methodName, Object... parameters) throws Exception {
			   Method privateMethod = getPrivateMethod(methodName);
			   privateMethod.setAccessible(true);
			   privateMethod.invoke(helper, parameters);
			 }

			 private static Method getPrivateMethod(String methodName) {
			   return declaredMethodsMap.get(methodName);
			 }
			 static Map<String, Method> declaredMethodsMap = new HashMap<String, Method>();

			 static {
			   // Reflection methods
			   Method[] declaredMethods = FulfillmentServiceHelper.class.getDeclaredMethods();
			   for (Method declaredMethod : declaredMethods) {
			     declaredMethodsMap.put(declaredMethod.getName(), declaredMethod);
			   }
			 }
			 
			 
}
