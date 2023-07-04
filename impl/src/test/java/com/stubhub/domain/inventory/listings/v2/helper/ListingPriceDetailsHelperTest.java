package com.stubhub.domain.inventory.listings.v2.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.stubhub.domain.inventory.datamodel.entity.Event;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.dao.BulkUploadSellerDAO;
import com.stubhub.domain.inventory.datamodel.entity.BulkUploadSeller;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.FulfillmentMethod;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.pricing.intf.aip.v1.error.ErrorCode;
import com.stubhub.domain.pricing.intf.aip.v1.request.PriceRequestList;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponse;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponseList;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;

public class ListingPriceDetailsHelperTest extends SHInventoryTest
{
	
	private MasterStubhubPropertiesWrapper masterStubhubProperties;
	private ListingPriceDetailsHelper helper;
	private BulkUploadSellerDAO bulkUploadSellerDAO;

	@BeforeMethod
	public void setUp(){
		helper =new ListingPriceDetailsHelper();
		masterStubhubProperties = mock(MasterStubhubPropertiesWrapper.class);
		ReflectionTestUtils.setField(helper, "masterStubhubProperties", masterStubhubProperties);
	}
	
	@Test
	public void batchCreatePriceCalc_UPS () throws Exception
	{
		ListingPriceUtil mocklistingPriceUtil = (ListingPriceUtil)mockClass(ListingPriceUtil.class, null, null);
	
	//	ListingPriceDetailsHelper helper = new ListingPriceDetailsHelper();
		setBeanProperty(helper, "listingPriceUtil", mocklistingPriceUtil);	
		
		PriceResponseList priceRespList = super.getPriceResponseList(30f, 8f, 3, 0);
		
		when(mocklistingPriceUtil.getListingPricingsAIP(Mockito.any(SHAPIContext.class), 
				Mockito.any(PriceRequestList.class))).thenReturn(priceRespList);
		
		SHAPIContext ctx = new SHAPIContext ();
		
		Listing l1 = new Listing ();
		l1.setEventId ( 8993478L );  
		l1.setTicketMedium(1);
		l1.setSection("SECTION230");
		l1.setRow ("row10" );		
		l1.setSellerPaymentTypeId(1l);
		l1.setFulfillmentDeliveryMethods("19,10");
		l1.setCurrency(Currency.getInstance("USD"));
		l1.setListPrice(new Money(new BigDecimal(30.00d), "USD"));
		l1.setQuantity(10);
		l1.setListingSource(10);
		l1.setSellerId(1000010549L);
		l1.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event1 = new Event();
		event1.setCountry("US");
		l1.setEvent(event1);
		
		Listing l2 = new Listing ();
		l2.setEventId ( 8993478L );
		l2.setTicketMedium(1);
		l2.setFulfillmentDeliveryMethods("19,10");
		l2.setSellerPaymentTypeId(1l);
		l2.setSection("SECTION240");
		l2.setRow ("row11" );
		l2.setCurrency(Currency.getInstance("USD"));
		l2.setListPrice(new Money(new BigDecimal(20.00d), "USD"));
		l2.setQuantity(10);
		l2.setListingSource(10);
		l2.setSellerId(1000010549L);
		l2.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event2 = new Event();
		event2.setCountry("US");
		l2.setEvent(event2);

		Listing l3 = new Listing ();
		l3.setEventId ( 8993478L );
		l3.setTicketMedium(1);
		l3.setFulfillmentDeliveryMethods("19,10");
		l3.setSellerPaymentTypeId(1l);
		l3.setSection("SECTION240");
		l3.setRow ("row11" );
		l3.setCurrency(Currency.getInstance("USD"));
		l3.setListPrice(new Money(new BigDecimal(20.00d), "USD"));
		l3.setQuantity(10);
		l3.setListingSource(10);
		l3.setSellerId(1000010549L);
		l3.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event3 = new Event();
		event3.setCountry("US");
		l3.setEvent(event3);
		
		List<Listing> listings = new ArrayList<Listing>();
		listings.add ( l1 );
		listings.add ( l2 );
		listings.add ( l3 );
		
		List<ListingRequest> listingRequests = new ArrayList<ListingRequest>();
		ListingRequest lr1 = new ListingRequest();
		ListingRequest lr2 = new ListingRequest();
		ListingRequest lr3 = new ListingRequest();

		listingRequests.add(lr1);
		listingRequests.add(lr2);
		listingRequests.add(lr3);
		
		
		Object [] results = helper.batchPriceCalculationsAIP ( ctx, null, listings, listingRequests);
		
		int resCount = 0;
		for ( int i=0; i<results.length; i++ ) {
			if ( results[i] instanceof PriceResponse)
				resCount++;
		}
		Assert.assertTrue("Expected 3 PriceResponse objects and no errors", resCount == 3);
	}
	
	@Test
	public void batchCreatePriceCalc_UPS1 () throws Exception
	{
		ListingPriceUtil mocklistingPriceUtil = (ListingPriceUtil)mockClass(ListingPriceUtil.class, null, null);
	
	//	ListingPriceDetailsHelper helper = new ListingPriceDetailsHelper();
		setBeanProperty(helper, "listingPriceUtil", mocklistingPriceUtil);	
		
		PriceResponseList priceRespList = super.getPriceResponseList(30f, 8f, 3, 0);
		
		when(mocklistingPriceUtil.getListingPricingsAIP(Mockito.any(SHAPIContext.class), 
				Mockito.any(PriceRequestList.class))).thenReturn(priceRespList);
		
		SHAPIContext ctx = new SHAPIContext ();
		
		Listing l1 = new Listing ();
		l1.setEventId ( 8993478L );  
		l1.setTicketMedium(1);
		l1.setSection("SECTION230");
		l1.setRow ("row10" );		
		l1.setSellerPaymentTypeId(1l);
		l1.setFulfillmentDeliveryMethods("10,12");
		l1.setCurrency(Currency.getInstance("USD"));
		l1.setListPrice(new Money(new BigDecimal(30.00d), "USD"));
		l1.setQuantity(10);
		l1.setListingSource(10);
		l1.setSellerId(1000010549L);
		l1.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event1 = new Event();
		event1.setCountry("US");
		l1.setEvent(event1);
		
		Listing l2 = new Listing ();
		l2.setEventId ( 8993478L );
		l2.setTicketMedium(1);
		l2.setFulfillmentDeliveryMethods("10,12");
		l2.setSellerPaymentTypeId(1l);
		l2.setSection("SECTION240");
		l2.setRow ("row11" );
		l2.setCurrency(Currency.getInstance("USD"));
		l2.setListPrice(new Money(new BigDecimal(20.00d), "USD"));
		l2.setQuantity(10);
		l2.setListingSource(10);
		l2.setSellerId(1000010549L);
		l2.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event2 = new Event();
		event2.setCountry("US");
		l2.setEvent(event2);

		Listing l3 = new Listing ();
		l3.setEventId ( 8993478L );
		l3.setTicketMedium(1);
		l3.setFulfillmentDeliveryMethods("10,12");
		l3.setSellerPaymentTypeId(1l);
		l3.setSection("SECTION240");
		l3.setRow ("row11" );
		l3.setCurrency(Currency.getInstance("USD"));
		l3.setListPrice(new Money(new BigDecimal(20.00d), "USD"));
		l3.setQuantity(10);
		l3.setListingSource(10);
		l3.setSellerId(1000010549L);
		l3.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event3 = new Event();
		event3.setCountry("US");
		l3.setEvent(event3);
		
		List<Listing> listings = new ArrayList<Listing>();
		listings.add ( l1 );
		listings.add ( l2 );
		listings.add ( l3 );
		
		List<ListingRequest> listingRequests = new ArrayList<ListingRequest>();
		ListingRequest lr1 = new ListingRequest();
		ListingRequest lr2 = new ListingRequest();
		ListingRequest lr3 = new ListingRequest();

		listingRequests.add(lr1);
		listingRequests.add(lr2);
		listingRequests.add(lr3);

		Object [] results = helper.batchPriceCalculationsAIP ( ctx, null, listings, listingRequests);
		int resCount = 0;
		for ( int i=0; i<results.length; i++ ) {
			if ( results[i] instanceof PriceResponse)
				resCount++;
		}
		Assert.assertTrue("Expected 3 PriceResponse objects and no errors", resCount == 3);
	}
	
	@Test
	public void batchCreatePriceCalc_LOCDEL () throws Exception
	{
		ListingPriceUtil mocklistingPriceUtil = (ListingPriceUtil)mockClass(ListingPriceUtil.class, null, null);
	
	//	ListingPriceDetailsHelper helper = new ListingPriceDetailsHelper();
		setBeanProperty(helper, "listingPriceUtil", mocklistingPriceUtil);	
		
		PriceResponseList priceRespList = super.getPriceResponseList(30f, 8f, 3, 0);
		
		when(mocklistingPriceUtil.getListingPricingsAIP(Mockito.any(SHAPIContext.class), 
				Mockito.any(PriceRequestList.class))).thenReturn(priceRespList);
		
		SHAPIContext ctx = new SHAPIContext ();
		
		Listing l1 = new Listing ();
		l1.setEventId ( 8993478L );  
		l1.setTicketMedium(1);
		l1.setSection("SECTION230");
		l1.setRow ("row10" );		
		l1.setSellerPaymentTypeId(1l);
		l1.setFulfillmentDeliveryMethods("17,10");
		l1.setCurrency(Currency.getInstance("USD"));
		l1.setListPrice(new Money(new BigDecimal(30.00d), "USD"));
		l1.setQuantity(10);
		l1.setListingSource(10);
		l1.setSellerId(1000010549L);
		l1.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event1 = new Event();
		event1.setCountry("US");
		l1.setEvent(event1);
		
		Listing l2 = new Listing ();
		l2.setEventId ( 8993478L );
		l2.setTicketMedium(1);
		l2.setFulfillmentDeliveryMethods("17,10");
		l2.setSellerPaymentTypeId(1l);
		l2.setSection("SECTION240");
		l2.setRow ("row11" );
		l2.setCurrency(Currency.getInstance("USD"));
		l2.setListPrice(new Money(new BigDecimal(20.00d), "USD"));
		l2.setQuantity(10);
		l2.setListingSource(10);
		l2.setSellerId(1000010549L);
		l2.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event2 = new Event();
		event2.setCountry("US");
		l2.setEvent(event2);

		Listing l3 = new Listing ();
		l3.setEventId ( 8993478L );
		l3.setTicketMedium(1);
		l3.setFulfillmentDeliveryMethods("17,10");
		l3.setSellerPaymentTypeId(1l);
		l3.setSection("SECTION240");
		l3.setRow ("row11" );
		l3.setCurrency(Currency.getInstance("USD"));
		l3.setListPrice(new Money(new BigDecimal(20.00d), "USD"));
		l3.setQuantity(10);
		l3.setListingSource(10);
		l3.setSellerId(1000010549L);
		l3.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event3 = new Event();
		event3.setCountry("US");
		l3.setEvent(event3);
		
		List<Listing> listings = new ArrayList<Listing>();
		listings.add ( l1 );
		listings.add ( l2 );
		listings.add ( l3 );
		
		List<ListingRequest> listingRequests = new ArrayList<ListingRequest>();
		ListingRequest lr1 = new ListingRequest();
		ListingRequest lr2 = new ListingRequest();
		ListingRequest lr3 = new ListingRequest();

		listingRequests.add(lr1);
		listingRequests.add(lr2);
		listingRequests.add(lr3);
		
		
		Object [] results = helper.batchPriceCalculationsAIP ( ctx, null, listings, listingRequests);		
		int resCount = 0;
		for ( int i=0; i<results.length; i++ ) {
			if ( results[i] instanceof PriceResponse)
				resCount++;
		}
		Assert.assertTrue("Expected 2 PriceResponse objects and no errors", resCount == 3);
	}
	
	@Test
	public void batchCreatePriceCalc() throws Exception
	{
		ListingPriceUtil mocklistingPriceUtil = (ListingPriceUtil)mockClass(ListingPriceUtil.class, null, null);
	
	//	ListingPriceDetailsHelper helper = new ListingPriceDetailsHelper();
		setBeanProperty(helper, "listingPriceUtil", mocklistingPriceUtil);	
		
		PriceResponseList priceRespList = super.getPriceResponseList(30f, 8f, 3, 0);
		
		when(mocklistingPriceUtil.getListingPricingsAIP(Mockito.any(SHAPIContext.class), 
				Mockito.any(PriceRequestList.class))).thenReturn(priceRespList);
		
		SHAPIContext ctx = new SHAPIContext ();
		
		Listing l1 = new Listing ();
		l1.setEventId ( 8993478L );  
		l1.setFulfillmentMethod(FulfillmentMethod.BARCODE);
		l1.setSection("SECTION230");
		l1.setRow ("row10" );		
		l1.setSellerPaymentTypeId(1l);
		l1.setFulfillmentDeliveryMethods("7,");
		l1.setCurrency(Currency.getInstance("USD"));
		l1.setListPrice(new Money(new BigDecimal(30.00d), "USD"));
		l1.setQuantity(10);
		l1.setListingSource(10);
		l1.setSellerId(1000010549L);
		l1.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event1 = new Event();
		event1.setCountry("US");
		l1.setEvent(event1);
		
		Listing l2 = new Listing ();
		l2.setEventId ( 8993478L );
		l2.setTicketMedium(1);
		l2.setFulfillmentDeliveryMethods("8,10");
		l2.setSellerPaymentTypeId(1l);
		l2.setSection("SECTION240");
		l2.setRow ("row11" );
		l2.setCurrency(Currency.getInstance("USD"));
		l2.setListPrice(new Money(new BigDecimal(20.00d), "USD"));
		l2.setQuantity(10);
		l2.setListingSource(10);
		l2.setSellerId(1000010549L);
		Event event2 = new Event();
		event2.setCountry("US");
		l2.setEvent(event2);

		Listing l3 = new Listing ();
		l3.setEventId ( 8993478L );
		l3.setTicketMedium(1);
		l3.setFulfillmentDeliveryMethods("7,");
		l3.setSellerPaymentTypeId(1l);
		l3.setSection("SECTION240");
		l3.setRow ("row11" );
		l3.setCurrency(Currency.getInstance("USD"));
		l3.setListPrice(new Money(new BigDecimal(20.00d), "USD"));
		l3.setQuantity(10);
		l3.setListingSource(10);
		l3.setSellerId(1000010549L);
		l3.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event3 = new Event();
		event3.setCountry("US");
		l3.setEvent(event3);
		
		List<Listing> listings = new ArrayList<Listing>();
		listings.add ( l1 );
		listings.add ( l2 );
		listings.add ( l3 );
		
		List<ListingRequest> listingRequests = new ArrayList<ListingRequest>();
		ListingRequest lr1 = new ListingRequest();
		ListingRequest lr2 = new ListingRequest();
		ListingRequest lr3 = new ListingRequest();

		listingRequests.add(lr1);
		listingRequests.add(lr2);
		listingRequests.add(lr3);
		
		
		Object [] results = helper.batchPriceCalculationsAIP ( ctx, null, listings, listingRequests);		
		int resCount = 0;
		for ( int i=0; i<results.length; i++ ) {
			if ( results[i] instanceof PriceResponse)
				resCount++;
		}
		Assert.assertTrue("Expected 2 PriceResponse objects and no errors", resCount == 3);
	}
	
	@Test
	public void batchCreatePriceCalcAllSuccess () throws Exception
	{
		ListingPriceUtil mocklistingPriceUtil = (ListingPriceUtil)mockClass(ListingPriceUtil.class, null, null);
	
	//	ListingPriceDetailsHelper helper = new ListingPriceDetailsHelper();
		setBeanProperty(helper, "listingPriceUtil", mocklistingPriceUtil);	
		
		PriceResponseList priceRespList = super.getPriceResponseList(30f, 8f, 3, 0);
		
		when(mocklistingPriceUtil.getListingPricingsAIP(Mockito.any(SHAPIContext.class), 
				Mockito.any(PriceRequestList.class))).thenReturn(priceRespList);
		
		SHAPIContext ctx = new SHAPIContext ();
		
		Listing l1 = new Listing ();
		l1.setEventId ( 8993478L );  
		l1.setTicketMedium(1);
		l1.setSection("SECTION230");
		l1.setRow ("row10" );		
		l1.setTicketMedium(TicketMedium.EXTFLASH.getValue());
		l1.setSellerPaymentTypeId(1l);
		l1.setCurrency(Currency.getInstance("USD"));
		l1.setListPrice(new Money(new BigDecimal(30.00d), "USD"));
		l1.setQuantity(10);
		l1.setListingSource(10);
		l1.setSellerId(1000010549L);
		Event event1 = new Event();
		event1.setCountry("US");
		l1.setEvent(event1);
		
		Listing l2 = new Listing ();
		l2.setEventId ( 8993478L );
		l2.setTicketMedium(1);
		l2.setTicketMedium(TicketMedium.FLASHSEAT.getValue());
		l2.setSellerPaymentTypeId(1l);
		l2.setSection("SECTION240");
		l2.setRow ("row11" );
		l2.setCurrency(Currency.getInstance("USD"));
		l2.setListPrice(new Money(new BigDecimal(20.00d), "USD"));
		l2.setQuantity(10);
		l2.setListingSource(10);
		l2.setSellerId(1000010549L);
		l2.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event2 = new Event();
		event2.setCountry("US");
		l2.setEvent(event2);

		Listing l3 = new Listing ();
		l3.setEventId ( 8993478L );
		l3.setTicketMedium(TicketMedium.EXTMOBILE.getValue());
		l3.setSellerPaymentTypeId(1l);
		l3.setSection("SECTION240");
		l3.setRow ("row11" );
		l3.setCurrency(Currency.getInstance("USD"));
		l3.setListPrice(new Money(new BigDecimal(20.00d), "USD"));
		l3.setQuantity(10);
		l3.setListingSource(10);
		l3.setSellerId(1000010549L);
		l3.setDeliveryOption(DeliveryOption.MANUAL.getValue());
		Event event3 = new Event();
		event3.setCountry("US");
		l3.setEvent(event3);
		
		List<Listing> listings = new ArrayList<Listing>();
		listings.add ( l1 );
		listings.add ( l2 );
		listings.add ( l3 );
		
		List<ListingRequest> listingRequests = new ArrayList<ListingRequest>();
		ListingRequest lr1 = new ListingRequest();
		ListingRequest lr2 = new ListingRequest();
		ListingRequest lr3 = new ListingRequest();

		listingRequests.add(lr1);
		listingRequests.add(lr2);
		listingRequests.add(lr3);
		
		
		Object [] results = helper.batchPriceCalculationsAIP ( ctx, null, listings, listingRequests);		
		int resCount = 0;
		for ( int i=0; i<results.length; i++ ) {
			if ( results[i] instanceof PriceResponse)
				resCount++;
		}
		Assert.assertTrue("Expected 2 PriceResponse objects and no errors", resCount == 3);
	}
	
	@Test
	public void batchUpdatePriceCalcAllSuccess () throws Exception
	{
		ListingPriceUtil mocklistingPriceUtil = (ListingPriceUtil)mockClass(ListingPriceUtil.class, null, null);
		
		//ListingPriceDetailsHelper helper = new ListingPriceDetailsHelper();
		setBeanProperty(helper, "listingPriceUtil", mocklistingPriceUtil);	
		
		PriceResponseList priceRespList = super.getPriceResponseList(30f, 8f, 6, 0);
		
		when(mocklistingPriceUtil.getListingPricingsAIP(Mockito.any(SHAPIContext.class), 
				Mockito.any(PriceRequestList.class))).thenReturn(priceRespList);
	
		SHAPIContext ctx = new SHAPIContext ();
		
		// current listings
		Listing l1 = new Listing ();
		l1.setId(10010l);
		l1.setEventId ( 8993478L );  
		l1.setTicketMedium(TicketMedium.MOBILE.getValue());
		l1.setSection("SECTION230");
		l1.setRow ("row10" );		
		l1.setSellerPaymentTypeId(1l);
		l1.setCurrency(Currency.getInstance("USD"));
		l1.setListPrice(new Money(new BigDecimal(30.00d), "USD"));
		l1.setQuantity(10);
		l1.setListingSource(10);
		l1.setSellerId(1000010549L);
		l1.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Money m = new Money();
		m.setAmount(new BigDecimal(200));
		l1.setFaceValue(m);
		l1.setCreatedBy("Relist|V1|Api_UK_sell_buyer20|DefaultApplication");
		Event event1 = new Event();
		event1.setCountry("US");
		l1.setEvent(event1);
		
		Listing l2 = new Listing ();
		l2.setId(10020l);
		l2.setEventId ( 8993478L );
		l2.setTicketMedium(TicketMedium.MOBILE.getValue());
		l2.setFulfillmentMethod(FulfillmentMethod.SHIPPING);
		l2.setSellerPaymentTypeId(1l);
		l2.setSection("SECTION240");
		l2.setRow ("row11" );
		l2.setCurrency(Currency.getInstance("USD"));
		l2.setListPrice(new Money(new BigDecimal(20.00d), "USD"));
		l2.setQuantity(10);
		l2.setListingSource(10);
		l2.setSellerId(1000010549L);
		l2.setDeliveryOption(DeliveryOption.MANUAL.getValue());
		Event event2 = new Event();
		event2.setCountry("US");
		l2.setEvent(event2);

		Listing l3 = new Listing ();
		l3.setId(10021l);
		l3.setEventId ( 8993478L );
		l3.setTicketMedium(TicketMedium.MOBILE.getValue());
		l3.setFulfillmentDeliveryMethods("12,38,0.0,,2015-10-29T08:00:00Z|12,39,0.0,,2015-10-29T08:00:00Z");
		l3.setSellerPaymentTypeId(1l);
		l3.setSection("SECTION240");
		l3.setRow ("row11" );
		l3.setCurrency(Currency.getInstance("USD"));
		l3.setListPrice(new Money(new BigDecimal(21.00d), "USD"));
		l3.setQuantity(10);
		l3.setListingSource(10);
		l3.setSellerId(1000010549L);
		l3.setDeliveryOption(DeliveryOption.MANUAL.getValue());
		Event event3 = new Event();
		event3.setCountry("US");
		l3.setEvent(event3);

		Listing l4 = new Listing ();
		l4.setId(10022l);
		l4.setEventId ( 8993478L );
		l4.setTicketMedium(TicketMedium.MOBILE.getValue());
		l4.setFulfillmentDeliveryMethods("11,38,0.0,,2015-10-29T08:00:00Z|11,39,0.0,,2015-10-29T08:00:00Z");
		l4.setSellerPaymentTypeId(1l);
		l4.setSection("SECTION240");
		l4.setRow ("row11" );
		l4.setCurrency(Currency.getInstance("USD"));
		l4.setDisplayPricePerTicket(new Money(new BigDecimal(22.00d), "USD"));
		l4.setQuantity(10);
		l4.setListingSource(10);
		l4.setSellerId(1000010549L);
		l4.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		l4.setFulfillmentMethod(FulfillmentMethod.LMSPREDELIVERY);
		Event event4 = new Event();
		event4.setCountry("US");
		l4.setEvent(event4);
	
		Listing l5 = new Listing ();
		l5.setId(10023l);
		l5.setEventId ( 8993478L );
		l5.setTicketMedium(3);
		l5.setFulfillmentDeliveryMethods("10,38,0.0,,2015-10-29T08:00:00Z|10,39,0.0,,2015-10-29T08:00:00Z");
		l5.setSellerPaymentTypeId(1l);
		l5.setSection("SECTION240");
		l5.setRow ("row11" );
		l5.setCurrency(Currency.getInstance("USD"));
		l5.setListPrice(new Money(new BigDecimal(22.00d), "USD"));
		l5.setQuantity(10);
		l5.setListingSource(10);
		l5.setSellerId(1000010549L);
		l5.setDeliveryOption(DeliveryOption.MANUAL.getValue());
		Event event5 = new Event();
		event5.setCountry("US");
		l5.setEvent(event5);

		Listing l6 = new Listing ();
		l6.setId(10024l);
		l6.setEventId ( 8993478L );
		l6.setTicketMedium(TicketMedium.PDF.getValue());
		l6.setSellerPaymentTypeId(1l);
		l6.setSection("SECTION240");
		l6.setRow ("row11" );
		l6.setCurrency(Currency.getInstance("USD"));
		l6.setListPrice(new Money(new BigDecimal(22.00d), "USD"));
		l6.setQuantity(10);
		l6.setListingSource(10);
		l6.setSellerId(1000010549L);
		l6.setDeliveryOption(DeliveryOption.MANUAL.getValue());
		l6.setTicketMedium(2);
		Event event6 = new Event();
		event6.setCountry("US");
		l6.setEvent(event6);

		
		Map<Long,Listing> curListingsMap = new HashMap<Long,Listing> ();
		curListingsMap.put(l1.getId(), l1);
		curListingsMap.put(l2.getId(), l2);
		curListingsMap.put(l3.getId(), l3);
		curListingsMap.put(l4.getId(), l4);
		curListingsMap.put(l5.getId(), l5);
		curListingsMap.put(l6.getId(), l6);
		
		// new listings from request
		Listing nl1 = new Listing ();
		nl1.setId(10010l);
		nl1.setListPrice(new Money(new BigDecimal(30.00d), "USD"));
		nl1.setEvent(event1);

		Listing nl2 = new Listing ();
		nl2.setId(10020l);
		nl2.setListPrice(new Money(new BigDecimal(20.00d), "USD"));
		nl2.setEvent(event2);

		Listing nl3 = new Listing ();
		nl3.setId(10021l);
		nl3.setDisplayPricePerTicket(new Money(new BigDecimal(21.00d), "USD"));
		nl3.setEvent(event3);

		Listing nl4 = new Listing ();
		nl4.setId(10022l);
		nl4.setListPrice(new Money(new BigDecimal(22.00d), "USD"));
		nl4.setEvent(event4);

		Listing nl5 = new Listing ();
		nl5.setId(10023l);
		nl5.setListPrice(new Money(new BigDecimal(22.00d), "USD"));
		nl5.setEvent(event5);
		
		Listing nl6 = new Listing ();
		nl6.setId(10024l);
		nl6.setListPrice(new Money(new BigDecimal(22.00d), "USD"));
		nl6.setEvent(event6);
		
		List<Listing> listings = new ArrayList<Listing>();
		listings.add ( nl1 );
		listings.add ( nl2 );
		listings.add ( nl3 );
		listings.add ( nl4 );
		listings.add ( nl5 );
		listings.add ( nl6 );
		
		List<ListingRequest> listingRequests = new ArrayList<ListingRequest>();
		ListingRequest lr1 = new ListingRequest();
		ListingRequest lr2 = new ListingRequest();
		ListingRequest lr3 = new ListingRequest();
		ListingRequest lr4 = new ListingRequest();
		ListingRequest lr5 = new ListingRequest();
		ListingRequest lr6 = new ListingRequest();


		listingRequests.add(lr1);
		listingRequests.add(lr2);
		listingRequests.add(lr3);
		listingRequests.add(lr4);
		listingRequests.add(lr5);
		listingRequests.add(lr6);

		
		Object [] results = helper.batchPriceCalculationsAIP ( ctx, curListingsMap, listings, listingRequests);
		
		int resCount = 0;
		for ( int i=0; i<results.length; i++ ) {
			if ( results[i] instanceof PriceResponse)
				resCount++;
		}
		Assert.assertTrue("Expected 6 PriceResponse objects and no errors", resCount == 6);
	}
	
	@Test
	public void batchCreatePriceCalcWithErrors () throws Exception
	{
		ListingPriceUtil mocklistingPriceUtil = (ListingPriceUtil)mockClass(ListingPriceUtil.class, null, null);
		
	//	ListingPriceDetailsHelper helper = new ListingPriceDetailsHelper();
		setBeanProperty(helper, "listingPriceUtil", mocklistingPriceUtil);	
		
		PriceResponseList priceRespList = super.getPriceResponseList(30f, 8f, 2, 2);
		
		when(mocklistingPriceUtil.getListingPricingsAIP(Mockito.any(SHAPIContext.class), 
				Mockito.any(PriceRequestList.class))).thenReturn(priceRespList);		
		
		SHAPIContext ctx = new SHAPIContext ();
		
		Listing l1 = new Listing ();
		l1.setEventId ( 8993478L );  
		l1.setTicketMedium(1);
		l1.setSection("SECTION230"); 
		l1.setRow ("row10" );		
		l1.setFulfillmentMethod(FulfillmentMethod.PDF);
		l1.setSellerPaymentTypeId(1l);
		l1.setCurrency(Currency.getInstance("USD"));
		l1.setListPrice(new Money(new BigDecimal(30.00d), "USD"));
		l1.setQuantity(10);
		l1.setListingSource(10);
		l1.setSellerId(1000010549L);
		l1.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		l1.setSystemStatus(ListingStatus.ACTIVE.toString());
		Event event1 = new Event();
		event1.setCountry("US");
		l1.setEvent(event1);
		
		Listing l2 = new Listing (); 
		l2.setEventId ( 8993478L );
		l2.setTicketMedium(1);
		l2.setFulfillmentMethod(FulfillmentMethod.PDF);
		l2.setSellerPaymentTypeId(1l);
		l2.setSection("SECTION240");
		l2.setRow ("row11" );
		l2.setCurrency(Currency.getInstance("USD"));
		l2.setListPrice(new Money(new BigDecimal(20.00d), "USD"));
		l2.setQuantity(10);
		l2.setListingSource(10);
		l2.setSellerId(1000010549L);
		l2.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event2 = new Event();
		event2.setCountry("US");
		l2.setEvent(event2);
		
		Listing l3 = new Listing ();
		l3.setEventId ( 8993478L );  
		l3.setTicketMedium(1);
		l3.setSection("SECTION260");
		l3.setRow ("row12" );		
		l3.setFulfillmentMethod(FulfillmentMethod.PDF);
		l3.setSellerPaymentTypeId(1l);
		l3.setCurrency(Currency.getInstance("USD"));
		l3.setListPrice(new Money(new BigDecimal(30.00d), "USD"));
		l3.setQuantity(10);
		l3.setListingSource(10);
		l3.setSellerId(1000010549L);
		l3.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event3 = new Event();
		event3.setCountry("US");
		l3.setEvent(event3);
		
		Listing l4 = new Listing ();
		l4.setEventId ( 8993478L );
		l4.setTicketMedium(1);
		l4.setFulfillmentMethod(FulfillmentMethod.PDF);
		l4.setSellerPaymentTypeId(1l);
		l4.setSection("SECTION270");
		l4.setRow ("row13" );
		l4.setCurrency(Currency.getInstance("USD"));
		l4.setListPrice(new Money(new BigDecimal(20.00d), "USD"));
		l4.setQuantity(10);
		l4.setListingSource(10);
		l4.setSellerId(1000010549L);
		l4.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event4 = new Event();
		event4.setCountry("US");
		l4.setEvent(event4);
		
		List<Listing> listings = new ArrayList<Listing>();
		listings.add ( l1 );
		listings.add ( l2 );
		listings.add ( l3 );
		listings.add ( l4 );		
		
		List<ListingRequest> listingRequests = new ArrayList<ListingRequest>();
		ListingRequest lr1 = new ListingRequest();
		lr1.setEventId("8993478L");  
		lr1.setSection("SECTION230"); 
		lr1.setPricePerProduct(new Money(new BigDecimal(30.00d), "USD"));
		lr1.setQuantity(10);
		Product prod1 = new Product();
		prod1.setRow("row10" );
		prod1.setFaceValue(new Money(new BigDecimal(30.00d), "USD"));
		
		List<Product> products = new ArrayList<Product>();
		products.add(prod1);
		lr1.setProducts(products) ;

		
		
		ListingRequest lr2 = new ListingRequest();
		lr2.setEventId("8993478L");  
		lr2.setSection("SECTION240"); 
		lr2.setPricePerProduct(new Money(new BigDecimal(20.00d), "USD"));
		lr2.setQuantity(10);
		Product prod2 = new Product();
		prod2.setRow("row11" );
		prod2.setFaceValue(new Money(new BigDecimal(20.00d), "USD"));
		
		List<Product> products2 = new ArrayList<Product>();
		products.add(prod2);
		lr2.setProducts(products2) ;
		
		ListingRequest lr3 = new ListingRequest();
		lr3.setEventId("8993478L");  
		lr3.setSection("SECTION260"); 
		lr3.setPricePerProduct(new Money(new BigDecimal(30.00d), "USD"));
		lr3.setQuantity(10);
		Product prod3 = new Product();
		prod3.setRow("row12" );
		prod3.setFaceValue(new Money(new BigDecimal(30.00d), "USD"));
		
		List<Product> products3 = new ArrayList<Product>();
		products.add(prod3);
		lr3.setProducts(products3) ;
		
		
		ListingRequest lr4 = new ListingRequest();
		lr4.setEventId("8993478L");  
		lr4.setSection("SECTION270"); 
		lr4.setPricePerProduct(new Money(new BigDecimal(20.00d), "USD"));
		lr4.setQuantity(10);
		Product prod4 = new Product();
		prod4.setRow("row13" );
		prod4.setFaceValue(new Money(new BigDecimal(20.00d), "USD"));
		
		List<Product> products4 = new ArrayList<Product>();
		products.add(prod4);
		lr4.setProducts(products4) ;


		listingRequests.add(lr1);
		listingRequests.add(lr2);
		listingRequests.add(lr3);
		listingRequests.add(lr4);

		
		
		Object [] results = helper.batchPriceCalculationsAIP ( ctx, null, listings, listingRequests);		
		int resCount = 0;
		int errCount = 0;
		for ( int i=0; i<results.length; i++ ) {
			if ( results[i] instanceof PriceResponse)
				resCount++;
			else
				errCount++;
		}
		Assert.assertTrue("Expected 2 PriceResponse objects and 2 ListingError objects", resCount == 2 && errCount == 2 );
		
		PriceResponseList rlist = new PriceResponseList();
    	List<PriceResponse>list = new ArrayList<PriceResponse>();
    	PriceResponse error = new PriceResponse();
    	com.stubhub.domain.pricing.intf.aip.v1.error.Error e = new com.stubhub.domain.pricing.intf.aip.v1.error.Error();
    	e.setCode(ErrorCode.SYSTEM_ERROR);
    	e.setType(ErrorType.SYSTEMERROR);
    	e.setMessage("System Error");
    	List<com.stubhub.domain.pricing.intf.aip.v1.error.Error>erl = new ArrayList<com.stubhub.domain.pricing.intf.aip.v1.error.Error>();
    	erl.add(e);
    	error.setErrors(erl);
    	list.add(error);
    	rlist.setPriceResponse(list);
		
		when(mocklistingPriceUtil.getListingPricingsAIP(Mockito.any(SHAPIContext.class), Mockito.any(PriceRequestList.class))).thenReturn(rlist);
		List<Listing> listings1 = new ArrayList<Listing>();
		listings1.add(l1);
		
		List<ListingRequest> listingRequests1 = new ArrayList<ListingRequest>();
		ListingRequest lrn1 = new ListingRequest();
		lrn1.setEventId("8993478L");  
		lrn1.setSection("SECTION230"); 
		lrn1.setPricePerProduct(new Money(new BigDecimal(30.00d), "USD"));
		lrn1.setQuantity(10);
		Product prodn1 = new Product();
		prodn1.setRow("row10" );
		prodn1.setFaceValue(new Money(new BigDecimal(30.00d), "USD"));
		
		List<Product> products5 = new ArrayList<Product>();
		products.add(prod1);
		lrn1.setProducts(products5) ;
		
		listingRequests1.add(lrn1);
		
		List<Listing> listingsNew1 = new ArrayList<Listing>();
		listingsNew1.add ( l1 );
		
		Object [] result1 = helper.batchPriceCalculationsAIP ( ctx, null, listingsNew1, listingRequests1);
				
	}
	
	@Test
	public void batchCreatePriceCalcMinimumPriceError() throws Exception {
		ListingPriceUtil mocklistingPriceUtil = (ListingPriceUtil)mockClass(ListingPriceUtil.class, null, null);
		setBeanProperty(helper, "listingPriceUtil", mocklistingPriceUtil);	
		
		PriceResponseList priceRespList = new PriceResponseList();
    	List<PriceResponse> list = new ArrayList<PriceResponse>();
    	
    	PriceResponse error = new PriceResponse();
    	com.stubhub.domain.pricing.intf.aip.v1.error.Error e = new com.stubhub.domain.pricing.intf.aip.v1.error.Error();
    	e.setMessage("Minimum Posting Price Error");
    	e.setCode(ErrorCode.MINIMUM_LIST_PRICE_ERROR);
    	List<com.stubhub.domain.pricing.intf.aip.v1.error.Error> erl = new ArrayList<com.stubhub.domain.pricing.intf.aip.v1.error.Error>();
    	erl.add(e);
    	error.setErrors(erl);
    	list.add(error);
    	priceRespList.setPriceResponse(list);
		
		when(mocklistingPriceUtil.getListingPricingsAIP(Mockito.any(SHAPIContext.class), 
				Mockito.any(PriceRequestList.class))).thenReturn(priceRespList);
		
		SHAPIContext ctx = new SHAPIContext ();
		Listing l1 = new Listing ();
		l1.setEventId ( 8993478L );  
		l1.setTicketMedium(1);
		l1.setSection("SECTION230");
		l1.setRow ("row10" );		
		l1.setFulfillmentMethod(FulfillmentMethod.LMS);
		l1.setSellerPaymentTypeId(1l);
		l1.setCurrency(Currency.getInstance("USD"));
		l1.setListPrice(new Money(new BigDecimal(2.00d), "USD"));
		l1.setQuantity(10);
		l1.setListingSource(10);
		l1.setSellerId(1000010549L);
		l1.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event1 = new Event();
		event1.setCountry("US");
		l1.setEvent(event1);
		
		List<Listing> listings = new ArrayList<Listing>();
		listings.add ( l1 );
		List<ListingRequest> listingRequests1 = new ArrayList<ListingRequest>();
		ListingRequest lrn1 = new ListingRequest();

		listingRequests1.add(lrn1);
		
		Object [] results = helper.batchPriceCalculationsAIP ( ctx, null, listings, listingRequests1);
		
	}
	
	@Test
	public void batchCreatePriceCalcMaximumPriceError() throws Exception {
		ListingPriceUtil mocklistingPriceUtil = (ListingPriceUtil)mockClass(ListingPriceUtil.class, null, null);
		setBeanProperty(helper, "listingPriceUtil", mocklistingPriceUtil);	
		
		PriceResponseList priceRespList = new PriceResponseList();
    	List<PriceResponse> list = new ArrayList<PriceResponse>();
    	
    	PriceResponse error = new PriceResponse();
    	com.stubhub.domain.pricing.intf.aip.v1.error.Error e = new com.stubhub.domain.pricing.intf.aip.v1.error.Error();
    	e.setMessage("Maximum Posting Price Error");
    	e.setCode(ErrorCode.MAXIMUM_LIST_PRICE_ERROR);
    	List<com.stubhub.domain.pricing.intf.aip.v1.error.Error> erl = new ArrayList<com.stubhub.domain.pricing.intf.aip.v1.error.Error>();
    	erl.add(e);
    	error.setErrors(erl);
    	list.add(error);
    	priceRespList.setPriceResponse(list);
		
		when(mocklistingPriceUtil.getListingPricingsAIP(Mockito.any(SHAPIContext.class), 
				Mockito.any(PriceRequestList.class))).thenReturn(priceRespList);
		
		SHAPIContext ctx = new SHAPIContext ();
		Listing l1 = new Listing ();
		l1.setEventId ( 8993478L );  
		l1.setTicketMedium(1);
		l1.setSection("SECTION230");
		l1.setRow ("row10" );		
		l1.setFulfillmentMethod(FulfillmentMethod.LMS);
		l1.setSellerPaymentTypeId(1l);
		l1.setCurrency(Currency.getInstance("USD"));
		l1.setListPrice(new Money(new BigDecimal(2.00d), "USD"));
		l1.setQuantity(10);
		l1.setListingSource(10);
		l1.setSellerId(1000010549L);
		l1.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event1 = new Event();
		event1.setCountry("US");
		l1.setEvent(event1);
		
		List<Listing> listings = new ArrayList<Listing>();
		listings.add ( l1 );
		
		List<ListingRequest> listingRequests1 = new ArrayList<ListingRequest>();
		ListingRequest lrn1 = new ListingRequest();

		listingRequests1.add(lrn1);
		
		Object [] results = helper.batchPriceCalculationsAIP ( ctx, null, listings, listingRequests1);	}
	
	@Test
	public void fulfillmentAndDeliveryPreDelivery () throws Exception
	{
		ListingPriceUtil mocklistingPriceUtil = (ListingPriceUtil)mockClass(ListingPriceUtil.class, null, null);
		
		//ListingPriceDetailsHelper helper = new ListingPriceDetailsHelper();
		setBeanProperty(helper, "listingPriceUtil", mocklistingPriceUtil);	
		
		PriceResponseList priceRespList = super.getPriceResponseList(30f, 8f, 6, 0);
		priceRespList.setPriceResponse(priceRespList.getPriceResponse().subList(0, 2));
		
		when(mocklistingPriceUtil.getListingPricingsAIP(Mockito.any(SHAPIContext.class), 
				Mockito.any(PriceRequestList.class))).thenReturn(priceRespList);
	
		SHAPIContext ctx = new SHAPIContext ();
		
		
		
		Listing l1 = new Listing ();
		l1.setId(10025l);
		l1.setEventId ( 8993478L );
		l1.setLmsApprovalStatus(2);
		l1.setTicketMedium(1);
		l1.setFulfillmentDeliveryMethods("10,38,0.0,,2015-10-29T08:00:00Z|10,39,0.0,,2015-10-29T08:00:00Z|9,39,0.0,,2015-10-29T08:00:00Z");
		l1.setSellerPaymentTypeId(1l);
		l1.setSection("SECTION240");
		l1.setRow ("row11" );
		l1.setCurrency(Currency.getInstance("USD"));
		l1.setListPrice(new Money(new BigDecimal(22.00d), "USD"));
		l1.setQuantity(10);
		l1.setListingSource(10);
		l1.setSellerId(1000010549L);
		l1.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event1 = new Event();
		event1.setCountry("US");
		l1.setEvent(event1);
		
		Listing l2 = new Listing ();
		l2.setId(10026l);
		l2.setEventId ( 8993478L );
		l2.setLmsApprovalStatus(2);
		l2.setTicketMedium(1);
		l2.setFulfillmentDeliveryMethods("10,38,0.0,,2015-10-29T08:00:00Z|10,39,0.0,,2015-10-29T08:00:00Z|7,39,0.0,,2015-10-29T08:00:00Z");
		l2.setSellerPaymentTypeId(1l);
		l2.setSection("SECTION240");
		l2.setRow ("row11" );
		l2.setCurrency(Currency.getInstance("USD"));
		l2.setListPrice(new Money(new BigDecimal(22.00d), "USD"));
		l2.setQuantity(10);
		l2.setListingSource(10);
		l2.setSellerId(1000010549L);
		l2.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		Event event2 = new Event();
		event2.setCountry("US");
		l2.setEvent(event2);
		
		
		Map<Long,Listing> curListingsMap = new HashMap<Long,Listing> ();		
		curListingsMap.put(l1.getId(), l1);
		curListingsMap.put(l2.getId(), l2);
		
		
		
		Listing nl1 = new Listing ();
		nl1.setId(10025l);
		nl1.setListPrice(new Money(new BigDecimal(22.00d), "USD"));
		nl1.setEvent(event1);
		
		Listing nl2 = new Listing ();
		nl2.setId(10026l);
		nl2.setListPrice(new Money(new BigDecimal(22.00d), "USD"));
		nl2.setEvent(event2);

		List<Listing> listings = new ArrayList<Listing>();
		
		listings.add ( nl1 );
		listings.add ( nl2 );
		
		List<ListingRequest> listingRequests1 = new ArrayList<ListingRequest>();
		ListingRequest lrn1 = new ListingRequest();
		ListingRequest lrn2 = new ListingRequest();

		listingRequests1.add(lrn1);
		listingRequests1.add(lrn2);

		
		Object [] results = helper.batchPriceCalculationsAIP ( ctx, curListingsMap, listings, listingRequests1);	
		
		int resCount = 0;
		for ( int i=0; i<results.length; i++ ) {
			if ( results[i] instanceof PriceResponse)
				resCount++;
		}
		Assert.assertTrue("Expected 2 PriceResponse objects and no errors", resCount == 2);
	}
	
	@Test
	public void validatePricesTest() throws Exception {
		SHAPIContext ctx = new SHAPIContext ();
		List<Listing> listings = new ArrayList<Listing>();
		Listing l1 = new Listing();
		l1.setSystemStatus("INCOMPLETE");
		l1.setSellerRequestedStatus("ACTIVE");
		Event event1 = new Event();
		event1.setCountry("US");
		l1.setEvent(event1);
		listings.add(l1);
		
		List<ListingRequest> listingRequests1 = new ArrayList<ListingRequest>();
		ListingRequest lrn1 = new ListingRequest();


		listingRequests1.add(lrn1);

		
		Object [] results = helper.batchPriceCalculationsAIP ( ctx, null, listings, listingRequests1);	

		int resCount = 0;
		int errCount = 0;
		int ignoreCount = 0;
		for ( int i=0; i<results.length; i++ ) {
			if ( results[i] instanceof PriceResponse)
				resCount++;
			else if ( results[i] instanceof ListingError) {
				errCount++;
			} else {
				ignoreCount++;
			}
				
		}
		Assert.assertTrue(ignoreCount == 1);
		
		l1.setSystemStatus("ACTIVE");
		l1.setCurrency(Currency.getInstance("USD"));
		l1.setDisplayPricePerTicket(new Money("15", "GBP"));
		results = helper.batchPriceCalculationsAIP(ctx, null, listings, listingRequests1);
		resCount = 0;
		errCount = 0;
		ignoreCount = 0;
		for ( int i=0; i<results.length; i++ ) {
			if ( results[i] instanceof PriceResponse)
				resCount++;
			else if ( results[i] instanceof ListingError) {
				errCount++;
			} else {
				ignoreCount++;
			}
				
		}
		Assert.assertTrue(errCount == 1);
		
		l1.setDisplayPricePerTicket(new Money("10000000", "USD"));
		results = helper.batchPriceCalculationsAIP(ctx, null, listings, listingRequests1);
		resCount = 0;
		errCount = 0;
		ignoreCount = 0;
		for ( int i=0; i<results.length; i++ ) {
			if ( results[i] instanceof PriceResponse)
				resCount++;
			else if ( results[i] instanceof ListingError) {
				errCount++;
			} else {
				ignoreCount++;
			}
				
		}
		Assert.assertTrue(errCount == 1);
	}
	
	@Test
    public void validatePricesTestFaceValue() throws Exception {
	    SHAPIContext ctx = new SHAPIContext ();
        List<Listing> listings = new ArrayList<Listing>();
        Listing l = new Listing();
        listings.add(l);
        l.setSystemStatus("ACTIVE");
        l.setCurrency(Currency.getInstance("USD"));
        l.setListPrice(new Money("17", "USD"));
        l.setFaceValue(new Money("15", "GBP"));
		Event event = new Event();
		event.setCountry("US");
		l.setEvent(event);
        
		List<ListingRequest> listingRequests1 = new ArrayList<ListingRequest>();
		ListingRequest lrn1 = new ListingRequest();

		listingRequests1.add(lrn1);
        
        Object [] results = helper.batchPriceCalculationsAIP(ctx, null, listings, listingRequests1);
        int resCount = 0;
        int errCount = 0;
        int ignoreCount = 0;
        for ( int i=0; i<results.length; i++ ) {
            if ( results[i] instanceof PriceResponse)
                resCount++;
            else if ( results[i] instanceof ListingError) {
                errCount++;
            } else {
                ignoreCount++;
            }
              
        }
        Assert.assertTrue(errCount == 1);
    }
	
	@Test
	public void batchCreatePriceCalcAllSuccess_MarkUp() throws Exception
	{
		ListingPriceUtil mocklistingPriceUtil = (ListingPriceUtil)mockClass(ListingPriceUtil.class, null, null);
		BulkUploadSellerDAO bulkUploadSellerDAO = Mockito.mock(BulkUploadSellerDAO.class);
		BulkUploadSeller  bulkUploadSeller = Mockito.mock(BulkUploadSeller.class);
		setBeanProperty(helper, "listingPriceUtil", mocklistingPriceUtil);	
		setBeanProperty(helper, "bulkUploadSellerDAO", bulkUploadSellerDAO);
		PriceResponseList priceRespList = super.getPriceResponseList(30f, 8f, 6, 0);
		priceRespList.setPriceResponse(priceRespList.getPriceResponse().subList(0, 1));
		
		when(mocklistingPriceUtil.getListingPricingsAIP(Mockito.any(SHAPIContext.class), 
				Mockito.any(PriceRequestList.class))).thenReturn(priceRespList);
	
		
		SHAPIContext ctx = new SHAPIContext ();
		
		Listing l1 = new Listing ();
		l1.setEventId ( 8993478L );  
		l1.setTicketMedium(1);
		l1.setSection("SECTION230");
		l1.setRow ("row10" );		
		l1.setFulfillmentMethod(FulfillmentMethod.FLASHSEAT);
		l1.setTicketMedium(4);
		l1.setSellerPaymentTypeId(1l);
		l1.setCurrency(Currency.getInstance("USD"));
		l1.setListPrice(new Money(new BigDecimal(30.00d), "USD"));
		l1.setQuantity(10);
		l1.setListingSource(10);
		l1.setSellerId(1000010549L);
		l1.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		l1.setMarkup(true);
		l1.setSellerPayoutAmountPerTicket(new Money(new BigDecimal(100.00d), "USD"));
		List<Listing> listings = new ArrayList<Listing>();
		listings.add ( l1 );
		Event event1 = new Event();
		event1.setCountry("US");
		l1.setEvent(event1);
		
		when(bulkUploadSellerDAO.get(1000010549L)).thenReturn(bulkUploadSeller);
		when(bulkUploadSeller.isSkipDynamicCalculation()).thenReturn(true);
		when(bulkUploadSeller.getMarkUp()).thenReturn(0.05);
		when(bulkUploadSeller.getAutoBulkDefaultSellFee()).thenReturn(0.05);
		
		List<ListingRequest> listingRequests1 = new ArrayList<ListingRequest>();
		ListingRequest lrn1 = new ListingRequest();

		listingRequests1.add(lrn1);
		
		Object [] results = helper.batchPriceCalculationsAIP ( ctx, null, listings, listingRequests1);	
		
		PriceResponse temp = (PriceResponse) results[0];
		Money expectedAmount = new Money(new BigDecimal("35.00"),"USD");
		Assert.assertTrue(expectedAmount.equals(temp.getDisplayPrice()));
	}
	
	
	@SuppressWarnings("unchecked")
    @Test(expectedExceptions = Exception.class)
	public void batchCreatePriceCalcAllSuccess_Error() throws Exception
	{
	    ListingPriceUtil mocklistingPriceUtil = (ListingPriceUtil)mockClass(ListingPriceUtil.class, null, null);
	    BulkUploadSellerDAO bulkUploadSellerDAO = Mockito.mock(BulkUploadSellerDAO.class);
	    BulkUploadSeller  bulkUploadSeller = Mockito.mock(BulkUploadSeller.class);
	    setBeanProperty(helper, "listingPriceUtil", mocklistingPriceUtil);  
	    setBeanProperty(helper, "bulkUploadSellerDAO", bulkUploadSellerDAO);
	    PriceResponseList priceRespList = super.getPriceResponseList(30f, 8f, 6, 0);
	    priceRespList.setPriceResponse(priceRespList.getPriceResponse().subList(0, 1));
	    when(mocklistingPriceUtil.getListingPricingsAIP(Mockito.any(SHAPIContext.class), 
	        Mockito.any(PriceRequestList.class))).thenReturn(priceRespList);
	    
	    SHAPIContext ctx = new SHAPIContext ();
	    Listing l1 = new Listing ();
	    l1.setEventId ( 8993478L );  
	    l1.setTicketMedium(1);
	    l1.setSection("SECTION230");
	    l1.setRow ("row10" );       
	    l1.setFulfillmentMethod(FulfillmentMethod.PDF);
	    l1.setSellerPaymentTypeId(1l);
	    l1.setCurrency(Currency.getInstance("USD"));
	    l1.setListPrice(new Money(new BigDecimal(30.00d), "USD"));
	    l1.setQuantity(10);
	    l1.setListingSource(10);
	    l1.setSellerId(1000010549L);
	    l1.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
	    l1.setMarkup(true);
	    l1.setSellerPayoutAmountPerTicket(new Money(new BigDecimal(100.00d), "USD"));
	    List<Listing> listings = new ArrayList<Listing>();
	    listings.add ( l1 );
		Event event1 = new Event();
		event1.setCountry("US");
		l1.setEvent(event1);
	        
	    when(bulkUploadSellerDAO.get(1000010549L)).thenThrow(Exception.class);
	    when(bulkUploadSeller.isSkipDynamicCalculation()).thenReturn(true);
	    when(bulkUploadSeller.getMarkUp()).thenReturn(0.05);
	    when(bulkUploadSeller.getAutoBulkDefaultSellFee()).thenReturn(0.05);
	    
		List<ListingRequest> listingRequests1 = new ArrayList<ListingRequest>();
		ListingRequest lrn1 = new ListingRequest();

		listingRequests1.add(lrn1);

		Object [] results = helper.batchPriceCalculationsAIP ( ctx, null, listings, listingRequests1);	
	    
	    PriceResponse temp = (PriceResponse) results[0];
	    Money expectedAmount = new Money(new BigDecimal("35.00"),"USD");
	    Assert.assertTrue(expectedAmount.equals(temp.getDisplayPrice()));
	}
	
	@Test
	public void batchCreatePriceCalcZeroPrice() throws Exception
    {
        SHAPIContext ctx = new SHAPIContext ();
        Listing l1 = new Listing ();
        l1.setEventId ( 8993478L );
        l1.setTicketMedium(1);
        l1.setSection("SECTION230");
        l1.setRow ("row10" );       
        l1.setFulfillmentMethod(FulfillmentMethod.PDF);
        l1.setSellerPaymentTypeId(1l);
        l1.setCurrency(Currency.getInstance("USD"));
        l1.setListPrice(new Money(new BigDecimal(0d), "USD"));
        l1.setSystemStatus("HIDDEN");
        l1.setQuantity(10);
        l1.setListingSource(10);
        l1.setSellerId(1000010549L);
        l1.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
        List<Listing> listings = new ArrayList<Listing>();
        listings.add ( l1 );
		Event event1 = new Event();
		event1.setCountry("US");
		l1.setEvent(event1);
        
		List<ListingRequest> listingRequests1 = new ArrayList<ListingRequest>();
		ListingRequest lrn1 = new ListingRequest();

		listingRequests1.add(lrn1);
		
		Object [] results = helper.batchPriceCalculationsAIP ( ctx, null, listings, listingRequests1);	

        PriceResponse temp = (PriceResponse) results[0];
        Assert.assertNull(temp);
    }
	   
	@Test
	public void batchCreatePriceCalcAllSuccess_NonMarkUp() throws Exception
	{
		ListingPriceUtil mocklistingPriceUtil = (ListingPriceUtil)mockClass(ListingPriceUtil.class, null, null);
		BulkUploadSellerDAO bulkUploadSellerDAO = Mockito.mock(BulkUploadSellerDAO.class);
		BulkUploadSeller  bulkUploadSeller = Mockito.mock(BulkUploadSeller.class);
		setBeanProperty(helper, "listingPriceUtil", mocklistingPriceUtil);	
		setBeanProperty(helper, "bulkUploadSellerDAO", bulkUploadSellerDAO);
		PriceResponseList priceRespList = super.getPriceResponseList(30f, 8f, 6, 0);
		priceRespList.setPriceResponse(priceRespList.getPriceResponse().subList(0, 1));
		
		when(mocklistingPriceUtil.getListingPricingsAIP(Mockito.any(SHAPIContext.class), 
				Mockito.any(PriceRequestList.class))).thenReturn(priceRespList);
	
		
		SHAPIContext ctx = new SHAPIContext ();
		
		Listing l1 = new Listing ();
		l1.setEventId ( 8993478L );  
		l1.setTicketMedium(1);
		l1.setSection("SECTION230");
		l1.setRow ("row10" );		
		l1.setFulfillmentMethod(FulfillmentMethod.UPS);
		l1.setSellerPaymentTypeId(1l);
		l1.setCurrency(Currency.getInstance("USD"));
		l1.setListPrice(new Money(new BigDecimal(30.00d), "USD"));
		l1.setQuantity(10);
		l1.setListingSource(10);
		l1.setSellerId(1000010549L);
		l1.setDeliveryOption(DeliveryOption.MANUAL.getValue());
		l1.setMarkup(false);
		l1.setSellerPayoutAmountPerTicket(new Money(new BigDecimal(100.00d), "USD"));
		Event event1 = new Event();
		event1.setCountry("US");
		l1.setEvent(event1);
		List<Listing> listings = new ArrayList<Listing>();
		listings.add ( l1 );
		
		List<ListingRequest> listingRequests1 = new ArrayList<ListingRequest>();
		ListingRequest lrn1 = new ListingRequest();

		listingRequests1.add(lrn1);
		
		Object [] results = helper.batchPriceCalculationsAIP ( ctx, null, listings, listingRequests1);	
		
		PriceResponse temp = (PriceResponse) results[0];
		Money expectedAmount = new Money(new BigDecimal("35.00"),"USD");
		Assert.assertTrue(expectedAmount.equals(temp.getDisplayPrice()));
	}
	
	
	@Test
	public void batchCreatePriceCalcAllSuccess_MarkUp_SkipCalculationFalse() throws Exception
	{
		ListingPriceUtil mocklistingPriceUtil = (ListingPriceUtil)mockClass(ListingPriceUtil.class, null, null);
		BulkUploadSellerDAO bulkUploadSellerDAO = Mockito.mock(BulkUploadSellerDAO.class);
		BulkUploadSeller  bulkUploadSeller = Mockito.mock(BulkUploadSeller.class);
		setBeanProperty(helper, "listingPriceUtil", mocklistingPriceUtil);	
		setBeanProperty(helper, "bulkUploadSellerDAO", bulkUploadSellerDAO);
		PriceResponseList priceRespList = super.getPriceResponseList(30f, 8f, 6, 0);
		priceRespList.setPriceResponse(priceRespList.getPriceResponse().subList(0, 1));
		
		when(mocklistingPriceUtil.getListingPricingsAIP(Mockito.any(SHAPIContext.class), 
				Mockito.any(PriceRequestList.class))).thenReturn(priceRespList);
	
		
		SHAPIContext ctx = new SHAPIContext ();
		
		Listing l1 = new Listing ();
		l1.setEventId ( 8993478L );  
		l1.setTicketMedium(1);
		l1.setSection("SECTION230");
		l1.setRow ("row10" );		
		l1.setFulfillmentMethod(FulfillmentMethod.PDF);
		l1.setSellerPaymentTypeId(1l);
		l1.setCurrency(Currency.getInstance("USD"));
		l1.setListPrice(new Money(new BigDecimal(30.00d), "USD"));
		l1.setQuantity(10);
		l1.setListingSource(10);
		l1.setSellerId(1000010549L);
		l1.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		l1.setMarkup(true);
		l1.setSellerPayoutAmountPerTicket(new Money(new BigDecimal(100.00d), "USD"));
		Event event1 = new Event();
		event1.setCountry("US");
		l1.setEvent(event1);
		List<Listing> listings = new ArrayList<Listing>();
		listings.add ( l1 );
		
		when(bulkUploadSellerDAO.get(1000010549L)).thenReturn(bulkUploadSeller);
		when(bulkUploadSeller.isSkipDynamicCalculation()).thenReturn(false);
		when(bulkUploadSeller.getMarkUp()).thenReturn(0.05);
		when(bulkUploadSeller.getAutoBulkDefaultSellFee()).thenReturn(0.05);
		
		List<ListingRequest> listingRequests1 = new ArrayList<ListingRequest>();
		ListingRequest lrn1 = new ListingRequest();

		listingRequests1.add(lrn1);

		
		Object [] results = helper.batchPriceCalculationsAIP ( ctx, null, listings, listingRequests1);	
		
		PriceResponse temp = (PriceResponse) results[0];
		Money expectedAmount = new Money(new BigDecimal("35.00"),"USD");
		Assert.assertTrue(expectedAmount.equals(temp.getDisplayPrice()));
	}

	
	@Test
	public void batchCreatePriceCalcAllSuccess_MarkUp_BulkSellerNull() throws Exception
	{
		ListingPriceUtil mocklistingPriceUtil = (ListingPriceUtil)mockClass(ListingPriceUtil.class, null, null);
		BulkUploadSellerDAO bulkUploadSellerDAO = Mockito.mock(BulkUploadSellerDAO.class);
		BulkUploadSeller  bulkUploadSeller = Mockito.mock(BulkUploadSeller.class);
		setBeanProperty(helper, "listingPriceUtil", mocklistingPriceUtil);	
		setBeanProperty(helper, "bulkUploadSellerDAO", bulkUploadSellerDAO);
		PriceResponseList priceRespList = super.getPriceResponseList(30f, 8f, 6, 0);
		priceRespList.setPriceResponse(priceRespList.getPriceResponse().subList(0, 1));
		
		when(mocklistingPriceUtil.getListingPricingsAIP(Mockito.any(SHAPIContext.class), 
				Mockito.any(PriceRequestList.class))).thenReturn(priceRespList);
	
		
		SHAPIContext ctx = new SHAPIContext ();
		
		Listing l1 = new Listing ();
		l1.setEventId ( 8993478L );  
		l1.setTicketMedium(1);
		l1.setSection("SECTION230");
		l1.setRow ("row10" );		
		l1.setFulfillmentMethod(FulfillmentMethod.PDF);
		l1.setSellerPaymentTypeId(1l);
		l1.setCurrency(Currency.getInstance("USD"));
		l1.setListPrice(new Money(new BigDecimal(30.00d), "USD"));
		l1.setQuantity(10);
		l1.setListingSource(10);
		l1.setSellerId(1000010549L);
		l1.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
		l1.setMarkup(true);
		l1.setSellerPayoutAmountPerTicket(new Money(new BigDecimal(100.00d), "USD"));
		Event event1 = new Event();
		event1.setCountry("US");
		l1.setEvent(event1);
		List<Listing> listings = new ArrayList<Listing>();
		listings.add ( l1 );
		
		when(bulkUploadSellerDAO.get(1000010549L)).thenReturn(null);
		
		List<ListingRequest> listingRequests1 = new ArrayList<ListingRequest>();
		ListingRequest lrn1 = new ListingRequest();

		listingRequests1.add(lrn1);

		Object [] results = helper.batchPriceCalculationsAIP ( ctx, null, listings, listingRequests1);	
		
		PriceResponse temp = (PriceResponse) results[0];
		Money expectedAmount = new Money(new BigDecimal("35.00"),"USD");
		Assert.assertTrue(expectedAmount.equals(temp.getDisplayPrice()));
	}
	
	public void setBeanProperty(Object objInstance, String propertyName,
			Object newVal) throws Exception {
		Field[] fields = objInstance.getClass().getDeclaredFields();
		objInstance.getClass().getDeclaredMethods();

		if (fields != null) {
			for (Field field : fields) {
				if (field.getName().equalsIgnoreCase(propertyName)) {
					field.setAccessible(true);
					field.set(objInstance, newVal);
				}
			}
		}
	}
	
    private BaseMatcher getMatcher(){
        BaseMatcher matcher=new BaseMatcher() {
              @Override
              public boolean matches(Object item) {
                    return true;
              }

              @Override
              public void describeTo(Description description) {
              }

        };

        return matcher;

    }	
	
};