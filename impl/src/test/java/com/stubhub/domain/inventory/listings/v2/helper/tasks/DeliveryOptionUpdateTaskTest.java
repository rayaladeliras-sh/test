package com.stubhub.domain.inventory.listings.v2.helper.tasks;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stubhub.domain.fulfillment.window.v1.intf.BaseCostResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.DeliveryMethodResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.EventFulfillmentWindowResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.EventInhanddate;
import com.stubhub.domain.fulfillment.window.v1.intf.FulfillmentMethodResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.FulfillmentWindowResponse;
import com.stubhub.domain.inventory.biz.v2.impl.util.FulfillmentServiceAdapter;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.FulfillmentMethod;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.tasks.DeliveryAndFullfilmentOptionsTask;
import com.stubhub.domain.inventory.listings.v2.util.FulfillmentServiceHelper;
import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;

public class DeliveryOptionUpdateTaskTest extends SHInventoryTest
{
	private FulfillmentServiceHelper fulfillmentServiceHelper;
	private FulfillmentServiceAdapter fulfillmentServiceAdapter;
	
	@BeforeTest
	public void setup() throws Exception
	{
		MockitoAnnotations.initMocks(this);
		fulfillmentServiceHelper = (FulfillmentServiceHelper)mockClass (FulfillmentServiceHelper.class,  null, null);
		fulfillmentServiceAdapter = Mockito.mock(FulfillmentServiceAdapter.class);
		ReflectionTestUtils.setField(fulfillmentServiceHelper, "fulfillmentServiceAdapter", fulfillmentServiceAdapter);
	}
	//SELLAPI-1172
	@Test
	public void deliveryOptionsTest_1172_01 () throws Exception
	{
		FulfillmentServiceAdapter fulfillmentServiceAdapter = Mockito.mock(FulfillmentServiceAdapter.class);
		
		Listing dblisting = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5);
		dblisting.setEvent( getEvent() );
		
		Listing newListing = new Listing();
		newListing.setListPrice(new Money(new BigDecimal(100d), "USD"));
		newListing.setId(1000l);
		newListing.setSellerId(1000l);
		newListing.setSaleMethod(1L);
		
		Calendar saleEndDate = Calendar.getInstance();
		saleEndDate.add (Calendar.MONTH, 5);	// sale end date should be after listing end date to succeed
		when(fulfillmentServiceHelper.calculateSaleEndDate(Mockito.any(Listing.class), Mockito.anyListOf(FulfillmentWindow.class))).thenReturn(saleEndDate);
		
		Calendar enddate2 = Calendar.getInstance();
		enddate2.add(Calendar.MONTH, 4);		// listing end date
		newListing.setEndDate(enddate2);
		
		Calendar inhanddate = Calendar.getInstance();
		inhanddate.add(Calendar.MONTH, 5);
		newListing.setInhandDate(inhanddate);
		
		SHAPIContext shapiContext = new SHAPIContext();

		DeliveryAndFullfilmentOptionsTask deliveryTask = new DeliveryAndFullfilmentOptionsTask(newListing, dblisting, shapiContext, fulfillmentServiceHelper, fulfillmentServiceAdapter, true, false, null);
		deliveryTask.call();
	}
	
	@Test
	public void deliveryOptions_InhandDateTest () throws Exception
	{
		Listing dblisting = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5);
		dblisting.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		
		dblisting.setEvent( getEvent() );
		
		Listing newlisting = new Listing();
		newlisting.setListPrice(new Money(new BigDecimal(100d), "USD"));
		newlisting.setId(1000l);
		newlisting.setSellerId(1000l);
		newlisting.setSaleMethod(1L);
		newlisting.setTicketMedium(TicketMedium.BARCODE.getValue());
		
		
		Calendar saleEndDate = Calendar.getInstance();
		saleEndDate.add (Calendar.MONTH, 5);	// sale end date should be after listing end date to succeed
		when(fulfillmentServiceHelper.calculateSaleEndDate(Mockito.any(Listing.class), Mockito.anyListOf(FulfillmentWindow.class))).thenReturn(saleEndDate);
		
		Calendar enddate2 = Calendar.getInstance();
		enddate2.add(Calendar.MONTH, 4);		// listing end date
		newlisting.setEndDate(enddate2);
		
		Calendar inhanddate = Calendar.getInstance();
		inhanddate.add(Calendar.MONTH, 5);
		newlisting.setInhandDate(inhanddate);
		
		SHAPIContext shapiContext = new SHAPIContext();

		DeliveryAndFullfilmentOptionsTask deliveryTask = new DeliveryAndFullfilmentOptionsTask(newlisting, dblisting, 
				shapiContext, fulfillmentServiceHelper  );
		deliveryTask.call();
	}
	
	@Test
	public void deliveryOptionsTest() throws Exception
	{
		Listing dblisting = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5);
		dblisting.setEvent( getEvent() );
		
		Listing newListing = new Listing();
		newListing.setListPrice(new Money(new BigDecimal(100d), "USD"));
		newListing.setId(1000l);
		newListing.setSellerId(1000l);
		newListing.setSaleMethod(1L);
		
		Calendar saleEndDate = Calendar.getInstance();
		saleEndDate.add (Calendar.MONTH, 5);	// sale end date should be after listing end date to succeed
		when(fulfillmentServiceHelper.calculateSaleEndDate(Mockito.any(Listing.class), Mockito.anyListOf(FulfillmentWindow.class))).thenReturn(saleEndDate);
		
		Calendar enddate2 = Calendar.getInstance();
		enddate2.add(Calendar.MONTH, 4);		// listing end date
		newListing.setEndDate(enddate2);
		
		Calendar inhanddate = Calendar.getInstance();
		inhanddate.add(Calendar.MONTH, 5);
		newListing.setInhandDate(inhanddate);
		
		SHAPIContext shapiContext = new SHAPIContext();

		DeliveryAndFullfilmentOptionsTask deliveryTask = new DeliveryAndFullfilmentOptionsTask(newListing, dblisting, shapiContext, fulfillmentServiceHelper, fulfillmentServiceAdapter, false, false, null);
		deliveryTask.call();
	}
	
	@Test
	public void deliveryFulfillmentTest() throws Exception
	{
		Listing dblisting = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5);
		dblisting.setEvent( getEvent() );
		
		Listing newListing = new Listing();
		newListing.setListPrice(new Money(new BigDecimal(100d), "USD"));
		newListing.setId(1000l);
		newListing.setSellerId(1000l);
		newListing.setSaleMethod(1L);
		
		newListing.setFulfillmentMethod(FulfillmentMethod.PDF);
		
		SHAPIContext shapiContext = new SHAPIContext();
		when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(getEventFulfillmentWindowResponseFromFulfillment());
		when(fulfillmentServiceHelper.populateFulfillmentOptions((Listing)any(), anyList())).thenReturn(true);
		
		DeliveryAndFullfilmentOptionsTask deliveryTask = new DeliveryAndFullfilmentOptionsTask(newListing, dblisting, shapiContext, fulfillmentServiceHelper, fulfillmentServiceAdapter, false, false, null);
		deliveryTask.call();
	}
	
	@Test
	public void deliveryOptionsTestForLmsExtension() throws Exception {
		Listing dblisting = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5);
		dblisting.setEvent( getEvent() );
		
		Listing newListing = new Listing();
		newListing.setListPrice(new Money(new BigDecimal(100d), "USD"));
		newListing.setId(1000l);
		newListing.setSellerId(1000l);
		newListing.setSaleMethod(1L);
		newListing.setLmsExtensionRequired(true);
		newListing.setEndDate(Calendar.getInstance());
		
		SHAPIContext shapiContext = new SHAPIContext();
		when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(getEventFulfillmentWindowResponseFromFulfillment());
		when(fulfillmentServiceHelper.populateFulfillmentOptions((Listing)any(), anyList())).thenReturn(true);
		
		DeliveryAndFullfilmentOptionsTask deliveryTask = new DeliveryAndFullfilmentOptionsTask(newListing, dblisting, shapiContext, fulfillmentServiceHelper, fulfillmentServiceAdapter, false, false, null);
		deliveryTask.call();
		
		dblisting.setFulfillmentDeliveryMethods("10,");
		deliveryTask.call();
		
		dblisting.setFulfillmentDeliveryMethods("11,");
		deliveryTask.call();
		
		newListing.setFulfillmentMethod(FulfillmentMethod.UPS);
		newListing.setTicketMedium(TicketMedium.PAPER.getValue());
		deliveryTask.call();
	}
	
	@Test
	public void testValidateInHandDate() throws Exception {
		when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(getEventFulfillmentWindowResponseFromFulfillment());
		when(fulfillmentServiceHelper.populateFulfillmentOptions((Listing)any(), anyList())).thenReturn(true);
		Listing dblisting = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5);
		SHAPIContext shapiContext = new SHAPIContext();
		Listing newListing = new Listing();
		newListing.setTicketMedium(TicketMedium.PAPER.getValue());
		newListing.setInhandDate(Calendar.getInstance());
		DeliveryAndFullfilmentOptionsTask deliveryTask = new DeliveryAndFullfilmentOptionsTask(newListing, dblisting, shapiContext, fulfillmentServiceHelper, fulfillmentServiceAdapter, true, false, null);
		deliveryTask.call();
		
		newListing.setTicketMedium(TicketMedium.PDF.getValue());
		deliveryTask.call();
		
		newListing.setTicketMedium(TicketMedium.BARCODE.getValue());
		deliveryTask.call();
		
		newListing.setTicketMedium(TicketMedium.FLASHSEAT.getValue());
		deliveryTask.call();
		
		newListing.setTicketMedium(TicketMedium.EVENTCARD.getValue());
        deliveryTask.call();
		
		EventFulfillmentWindowResponse efwr1 = new EventFulfillmentWindowResponse();
		efwr1.setInHandDateSettings(new HashMap<String, EventInhanddate>());
		when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(efwr1);
		DeliveryAndFullfilmentOptionsTask deliveryTask1 = new DeliveryAndFullfilmentOptionsTask(newListing, dblisting, shapiContext, fulfillmentServiceHelper, fulfillmentServiceAdapter, false, false, null);
		deliveryTask1.call();
		
		EventFulfillmentWindowResponse efwr2 = new EventFulfillmentWindowResponse();
		efwr2.setEarliestInHandDate(Calendar.getInstance());
		Calendar lihDate = Calendar.getInstance();
		lihDate.add(Calendar.MONTH, 5);
		efwr2.setLatestInHandDate(lihDate);
		when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(efwr2);
		DeliveryAndFullfilmentOptionsTask deliveryTask2 = new DeliveryAndFullfilmentOptionsTask(newListing, dblisting, shapiContext, fulfillmentServiceHelper, fulfillmentServiceAdapter, false, false, null);
		deliveryTask2.call();
	}
	
	@Test(expectedExceptions={ListingBusinessException.class})
	public void deliveryOptionUpdateErrorTest() throws Exception {
		when(fulfillmentServiceAdapter.getFulfillmentWindowsShape(Matchers.anyLong(), Matchers.anyLong())).thenReturn(getEventFulfillmentWindowResponseFromFulfillment());
		when(fulfillmentServiceHelper.populateFulfillmentOptions((Listing)any(), anyList())).thenReturn(false);
		Listing dblisting = getListing(1000L, "sec-10", "R1", "1,2,3,4,5", 5, 5);
		SHAPIContext shapiContext = new SHAPIContext();
		Listing newListing = new Listing();
		newListing.setTicketMedium(TicketMedium.PAPER.getValue());
		newListing.setFulfillmentMethod(FulfillmentMethod.UPS);
		newListing.setInhandDate(Calendar.getInstance());
		DeliveryAndFullfilmentOptionsTask deliveryTask = new DeliveryAndFullfilmentOptionsTask(newListing, dblisting, shapiContext, fulfillmentServiceHelper, fulfillmentServiceAdapter, false, false, null);
		deliveryTask.call();
	}
	
	private EventFulfillmentWindowResponse getEventFulfillmentWindowResponseFromFulfillment () {
		EventFulfillmentWindowResponse efwr = new EventFulfillmentWindowResponse();
		
		List <FulfillmentWindowResponse> listFFWindows = new ArrayList<FulfillmentWindowResponse>();
		listFFWindows.add(getFulfillmentWindow(12145L, 10L, "UPS", 24L, "2015-06-12T11:00:00", "2015-08-12T11:00:00", 5.5));
		listFFWindows.add(getFulfillmentWindow(10452L, 4L, "PDF - PreDelivery", 10L, "2015-06-12T11:00:00", "2015-08-12T11:00:00", 0.0));
		listFFWindows.add(getFulfillmentWindow(10953L, 6L, "Barcode - PreDelivery (Non-STH)",8L, "2015-06-12T11:00:00", "2015-08-12T11:00:00", 0.0));
		efwr.setFulfillmentWindows(listFFWindows);
		
		Map<String, EventInhanddate> inHandDateSettings = new HashMap<String, EventInhanddate>();
		
		EventInhanddate barcode = new EventInhanddate();
		barcode.setEihd("2016-01-04T23:30:00+00:00");
		barcode.setLihd("2016-05-04T23:30:00+00:00");
		
		EventInhanddate pdf = new EventInhanddate();
		pdf.setEihd("2016-01-04T23:30:00+00:00");
		pdf.setLihd("2016-05-04T23:30:00+00:00");
		
		EventInhanddate paper = new EventInhanddate();
		paper.setEihd("2016-01-04T23:30:00+00:00");
		paper.setLihd("2016-04-04T23:30:00+00:00");
		
		EventInhanddate flashseat = new EventInhanddate();
		flashseat.setEihd("2016-01-04T23:30:00+00:00");
		flashseat.setLihd("2016-04-04T23:30:00+00:00");
		
		EventInhanddate eventcard = new EventInhanddate();
        eventcard.setEihd("2016-01-04T23:30:00+00:00");
        eventcard.setLihd("2016-04-04T23:30:00+00:00");
		
		inHandDateSettings.put("barcode", barcode);
		inHandDateSettings.put("pdf", pdf);
		inHandDateSettings.put("paper", paper);
		inHandDateSettings.put("flashseat", flashseat);
		inHandDateSettings.put("eventcard", eventcard);
		
		efwr.setInHandDateSettings(inHandDateSettings);
		
		efwr.setEarliestInHandDate(Calendar.getInstance());
		Calendar lihDate = Calendar.getInstance();
		lihDate.add(Calendar.MONTH, 5);
		efwr.setLatestInHandDate(lihDate);
		
		return efwr;
	}
	
	private FulfillmentWindowResponse getFulfillmentWindow(Long winId, Long ffMethodId, String ffMethodName, 
			Long deliveryMethodId, String startTime, String endTime, double amount) {

		FulfillmentWindowResponse ffw = new FulfillmentWindowResponse();
		FulfillmentMethodResponse ffm = new FulfillmentMethodResponse();
		DeliveryMethodResponse dmr = new DeliveryMethodResponse();

		ffm.setId(ffMethodId);
		ffm.setName(ffMethodName);
		
		dmr.setId(deliveryMethodId);
		
		ffw.setFulfillmentMethod(ffm);
		ffw.setDeliveryMethod(dmr);
		ffw.setBaseCost(new BaseCostResponse(BigDecimal.valueOf(amount), BigDecimal.valueOf(amount), BigDecimal.valueOf(amount), "USD"));
		ffw.setStartTime(convertToCalendar(startTime));
		ffw.setEndTime(convertToCalendar(endTime));

		return ffw;
	}
	
	private Calendar convertToCalendar(String dateStr) {
		Calendar cal = Calendar.getInstance();
		try {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			Date date = format.parse(dateStr);
			cal = Calendar.getInstance();
			cal.setTime(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
        return cal;
    }
	
}