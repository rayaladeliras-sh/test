package com.stubhub.domain.inventory.listings.v2.adapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.entity.SplitOption;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.FulfillmentMethod;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.DTO.TicketTrait;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;

public class ListingRequestAdapterTest 
{
	@Test
	public void testConvertStatus()
	{
		ListingRequest updateReq = new ListingRequest();
		
		updateReq.setStatus(ListingStatus.ACTIVE);
		Listing listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getSystemStatus()!=null && listing.getSystemStatus().equals(ListingStatus.ACTIVE.name() ));
		
		updateReq.setStatus(ListingStatus.INACTIVE);
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getSystemStatus()!=null && listing.getSystemStatus().equals(ListingStatus.INACTIVE.name() ));

		updateReq.setStatus(ListingStatus.DELETED);
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getSystemStatus()!=null && listing.getSystemStatus().equals(ListingStatus.DELETED.name() ));
		
		updateReq.setStatus(ListingStatus.PENDING);
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getSystemStatus()!=null && listing.getSystemStatus().equals(ListingStatus.PENDING.name() ));
		
		updateReq.setStatus(ListingStatus.HIDDEN);
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getSystemStatus()!=null && listing.getSystemStatus().equals(ListingStatus.HIDDEN.name() ));
		Assert.assertFalse( listing.getSystemStatus()!=null && listing.getSystemStatus().equals(ListingStatus.INCOMPLETE.name() ));
		listing = ListingRequestAdapter.convert(updateReq, true, null);
		Assert.assertTrue( listing.getSystemStatus()!=null && listing.getSystemStatus().equals(ListingStatus.HIDDEN.name() ));
		Assert.assertFalse( listing.getSystemStatus()!=null && listing.getSystemStatus().equals(ListingStatus.INCOMPLETE.name() ));
		updateReq.setStatus(ListingStatus.INCOMPLETE);
		listing = ListingRequestAdapter.convert(updateReq, true, null);
		Assert.assertTrue( listing.getSystemStatus()!=null && listing.getSystemStatus().equals(ListingStatus.INCOMPLETE.name() ));	
		updateReq.setStatus(ListingStatus.INVALID);
		listing = ListingRequestAdapter.convert(updateReq, true, null);
		Assert.assertTrue( listing.getSystemStatus()!=null && listing.getSystemStatus().equals(ListingStatus.ACTIVE.name() ));		
		
	}
	
	@Test
	public void testSplitOptions()
	{
		ListingRequest updateReq = new ListingRequest();  

		updateReq.setSplitOption( SplitOption.NOSINGLES );
		Listing listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getSplitQuantity()==1 && listing.getSplitOption()==2 );
		
		updateReq.setSplitOption( SplitOption.NONE );
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getSplitQuantity()==0 && listing.getSplitOption()==0 );
		
		updateReq.setSplitOption( SplitOption.MULTIPLES );
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getSplitQuantity()==1 && listing.getSplitOption()==1 );
	}
	
	@Test
	public void testDeliveryOptions()
	{
		ListingRequest updateReq = new ListingRequest();
		
		updateReq.setDeliveryOption( DeliveryOption.UPS );
		Listing listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.PAPER.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.UPS) );
		
		updateReq.setDeliveryOption( DeliveryOption.FEDEX );
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.PAPER.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.FEDEX) );

		updateReq.setDeliveryOption( DeliveryOption.PDF );
		updateReq.setComments("test");
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.PDF.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.PDF) );
		
		updateReq.setDeliveryOption( DeliveryOption.BARCODE );
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.BARCODE.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.BARCODE) );
		
		updateReq.setDeliveryOption( DeliveryOption.STH );
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.BARCODE.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.BARCODEPREDELIVERYSTH) );

		updateReq.setDeliveryOption( DeliveryOption.LMS );
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.PAPER.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.LMS) );

		updateReq.setDeliveryOption( DeliveryOption.SHIPPING );
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.PAPER.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.SHIPPING) );

		updateReq.setDeliveryOption( DeliveryOption.FLASHSEAT );
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.FLASHSEAT.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.FLASHSEAT) );
		
		updateReq.setDeliveryOption( DeliveryOption.MOBILETRANSFER );
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.EXTMOBILE.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.MOBILETRANSFER) );
		
		updateReq.setDeliveryOption( DeliveryOption.MOBILE );
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.MOBILE.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.MOBILE) );
		
        updateReq.setDeliveryOption( DeliveryOption.LOCALDELIVERY );
        listing = ListingRequestAdapter.convert(updateReq);
        Assert.assertTrue( listing.getTicketMedium()==TicketMedium.PAPER.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.LOCALDELIVERY) );
		
		updateReq.setDeliveryOption(null);
		updateReq.setComments("gc");
        listing = ListingRequestAdapter.convert(updateReq);
        Assert.assertTrue( listing.getTicketMedium()==TicketMedium.EVENTCARD.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.EVENTCARD) );
        
        updateReq.setComments("Credit Card");
        listing = ListingRequestAdapter.convert(updateReq);
        Assert.assertTrue( listing.getTicketMedium()==TicketMedium.EVENTCARD.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.EVENTCARD) );
        
        updateReq.setComments("lpu");
        listing = ListingRequestAdapter.convert(updateReq);
        Assert.assertTrue( listing.getTicketMedium()==TicketMedium.PAPER.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.LOCALDELIVERY) );
        
        updateReq.setComments("local pickup");
        listing = ListingRequestAdapter.convert(updateReq);
        Assert.assertTrue( listing.getTicketMedium()==TicketMedium.PAPER.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.LOCALDELIVERY) );
		
		updateReq.setComments("fls");
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.FLASHSEAT.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.FLASHSEAT) );
		
		updateReq.setComments("xfer");
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.EXTMOBILE.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.MOBILETRANSFER) );
		
		updateReq.setComments("mobile");
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.MOBILE.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.MOBILE) );
		
		updateReq.setDeliveryOption( DeliveryOption.BARCODE );
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.BARCODE.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.BARCODE) );
		
		updateReq.setDeliveryOption( DeliveryOption.PDF );
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.MOBILE.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.MOBILE) );
		
		updateReq.setDeliveryOption( DeliveryOption.BARCODE );
		updateReq.setComments("fls");
        listing = ListingRequestAdapter.convert(updateReq);
        Assert.assertTrue( listing.getTicketMedium()==TicketMedium.FLASHSEAT.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.FLASHSEAT) );
        
        updateReq.setDeliveryOption( DeliveryOption.UPS );
        updateReq.setComments("seasoncard");
        listing = ListingRequestAdapter.convert(updateReq);
        Assert.assertTrue( listing.getTicketMedium()==TicketMedium.SEASONCARD.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.SEASONCARD) );
        
        updateReq.setComments("rfid");
        listing = ListingRequestAdapter.convert(updateReq);
        Assert.assertTrue( listing.getTicketMedium()==TicketMedium.RFID.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.RFID) );
        
        updateReq.setComments("wristband");
        listing = ListingRequestAdapter.convert(updateReq);
        Assert.assertTrue( listing.getTicketMedium()==TicketMedium.WRISTBAND.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.WRISTBAND) );
        
        updateReq.setComments("guestlist");
        listing = ListingRequestAdapter.convert(updateReq);
        Assert.assertTrue( listing.getTicketMedium()==TicketMedium.GUESTLIST.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.GUESTLIST) );
        
        updateReq.setComments(null);
        updateReq.setTicketMedium(com.stubhub.domain.inventory.v2.enums.TicketMedium.EVENTCARD);
        listing = ListingRequestAdapter.convert(updateReq);
        Assert.assertTrue( listing.getTicketMedium()==TicketMedium.EVENTCARD.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.EVENTCARD) );
        
        updateReq.setTicketMedium(com.stubhub.domain.inventory.v2.enums.TicketMedium.SEASONCARD);
        listing = ListingRequestAdapter.convert(updateReq);
        Assert.assertTrue( listing.getTicketMedium()==TicketMedium.SEASONCARD.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.SEASONCARD) );
        
        updateReq.setTicketMedium(com.stubhub.domain.inventory.v2.enums.TicketMedium.RFID);
        listing = ListingRequestAdapter.convert(updateReq);
        Assert.assertTrue( listing.getTicketMedium()==TicketMedium.RFID.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.RFID) );
        
        updateReq.setTicketMedium(com.stubhub.domain.inventory.v2.enums.TicketMedium.WRISTBAND);
        listing = ListingRequestAdapter.convert(updateReq);
        Assert.assertTrue( listing.getTicketMedium()==TicketMedium.WRISTBAND.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.WRISTBAND) );
        
        updateReq.setTicketMedium(com.stubhub.domain.inventory.v2.enums.TicketMedium.GUESTLIST);
        listing = ListingRequestAdapter.convert(updateReq);
        Assert.assertTrue( listing.getTicketMedium()==TicketMedium.GUESTLIST.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.GUESTLIST) );

		updateReq.setDeliveryOption(null);
		updateReq.setComments(null);
		List <Product> products = new ArrayList<Product> ();
		Product prod = new Product();
		prod.setExternalId("ABCD");
		prod.setRow("12");
		prod.setSeat("1");
		prod.setOperation(Operation.ADD);
		prod.setProductType(ProductType.TICKET);
		prod.setFulfillmentArtifact("01234");
		products.add(prod);
		
		updateReq.setProducts(products);
		updateReq.setHideSeats(Boolean.TRUE);
		listing.setDeliveryOption(null);
		listing = ListingRequestAdapter.convert(updateReq, true, null);
		Integer intDO = listing.getDeliveryOption();
		Assert.assertEquals( intDO.intValue(), 1);
		
		products = new ArrayList<Product> ();
		prod = new Product();
		prod.setExternalId("ABCD");
		prod.setRow("12");
		prod.setSeat("Parking Pass");
		prod.setOperation(Operation.ADD);
		
		prod.setFulfillmentArtifact("01234");
		products.add(prod);
		
		updateReq.setProducts(products);
		listing.setDeliveryOption(null);
		listing = ListingRequestAdapter.convert(updateReq, true);
		intDO = listing.getDeliveryOption();
		Assert.assertEquals( intDO.intValue(), 1);
		
		updateReq.setAutoPricingEnabledInd(Boolean.TRUE);
		updateReq.setDeliveryOption(DeliveryOption.BARCODE);
		listing = ListingRequestAdapter.convert(updateReq, true);
		Assert.assertTrue(listing.isAutoPricingEnabledInd());
		listing.setDeliveryOption(null);
		listing = ListingRequestAdapter.convert(updateReq, true);
		intDO = listing.getDeliveryOption();
		Assert.assertEquals( intDO.intValue(), 1);
		
	}
	
	@Test
	public void checkNewValueOfSaleEndDateNullEvent() {
		ListingRequestAdapter.checkNewValueOfSaleEndDate(null, new Date());
		ListingRequestAdapter.checkNewValueOfSaleEndDate(new Event(), null);
		// no exception expected
	}
	
	private Calendar getCalendarFromDateTime(DateTime dt) {
		return dt.toCalendar(Locale.US);
	}

	@Test
	public void checkNewValueOfSaleValidValue() {
		DateTime d = new DateTime();

		Event event = new Event();

		event.setEventDate(getCalendarFromDateTime(d.plusDays(10)));
		ListingRequestAdapter.checkNewValueOfSaleEndDate(event, d.plusDays(5)
				.toDate());
		// no exception expected
	}
	
	@Test(expectedExceptions = ListingBusinessException.class)
	public void checkNewValueOfSaleValueOutsideTheRange() {
		DateTime d = new DateTime();

		Event event = new Event();

		event.setEventDate(getCalendarFromDateTime(d.plusDays(10)));
		ListingRequestAdapter.checkNewValueOfSaleEndDate(event, d.plusDays(15)
				.toDate());

	}
	
	@Test(expectedExceptions=ListingBusinessException.class)
	public void testEventIdValidation() {
		ListingRequest request = new ListingRequest();
		request.setEventId("ABCD");
		ListingRequestAdapter.convert(request, true, null);
	} 
	
	@Test
	public void testderiveFulfillmentMethodFromCommentsElectronic_Flash(){
		List<TicketTrait> ticketTraits = new ArrayList<>();
		ListingRequest updateReq = new ListingRequest();
		TicketTrait tt = new TicketTrait();
		tt.setId("13701");
		ticketTraits.add(tt);
	
		updateReq.setDeliveryOption( DeliveryOption.BARCODE );
		updateReq.setTicketTraits(ticketTraits);
		Listing listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.FLASHSEAT.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.FLASHSEAT) );
		ticketTraits.remove(tt);
		updateReq.setTicketTraits(ticketTraits);
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.BARCODE.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.BARCODE) );
	}
	
	@Test
	public void testderiveFulfillmentMethodFromCommentsElectronic_MobileTransfer(){
		List<TicketTrait> ticketTraits = new ArrayList<>();
		ListingRequest updateReq = new ListingRequest();
		TicketTrait tt = new TicketTrait();
		tt.setId("14912");
		ticketTraits.add(tt);
		
		updateReq.setDeliveryOption( DeliveryOption.PDF );
		updateReq.setTicketTraits(ticketTraits);
		Listing listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.EXTMOBILE.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.MOBILETRANSFER) );
		ticketTraits.remove(tt);
		updateReq.setTicketTraits(ticketTraits);
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.PDF.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.PDF) );
	}
	
	@Test
	public void testderiveFulfillmentMethodFromCommentsElectronic_Mobile(){
		List<TicketTrait> ticketTraits = new ArrayList<>();
		ListingRequest updateReq = new ListingRequest();
		TicketTrait tt = new TicketTrait();
		tt.setId("14699");
		ticketTraits.add(tt);
		
		updateReq.setDeliveryOption( DeliveryOption.PDF );
		updateReq.setTicketTraits(ticketTraits);
		Listing listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.MOBILE.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.MOBILE) );
		ticketTraits.remove(tt);
		updateReq.setTicketTraits(ticketTraits);
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.PDF.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.PDF) );

	}
	
	@Test
	public void testderiveFulfillmentMethodFromCommentsPaper_GC(){
		List<TicketTrait> ticketTraits = new ArrayList<>();
		ListingRequest updateReq = new ListingRequest();
		TicketTrait tt = new TicketTrait();
		tt.setId("14800");
		ticketTraits.add(tt);
		
		updateReq.setDeliveryOption( DeliveryOption.UPS );
		updateReq.setTicketTraits(ticketTraits);
		Listing listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.EVENTCARD.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.EVENTCARD) );
		ticketTraits.remove(tt);
		updateReq.setTicketTraits(ticketTraits);
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.PAPER.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.UPS) );

	}
	@Test
	public void testderiveFulfillmentMethodFromCommentsPaper_LocalDelivery(){
		List<TicketTrait> ticketTraits = new ArrayList<>();
		ListingRequest updateReq = new ListingRequest();
		TicketTrait tt = new TicketTrait();
		tt.setId("14910");
		ticketTraits.add(tt);
		
		updateReq.setDeliveryOption( DeliveryOption.UPS );
		updateReq.setTicketTraits(ticketTraits);
		Listing listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.PAPER.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.LOCALDELIVERY) );
		ticketTraits.remove(tt);
		updateReq.setTicketTraits(ticketTraits);
		listing = ListingRequestAdapter.convert(updateReq);
		Assert.assertTrue( listing.getTicketMedium()==TicketMedium.PAPER.getValue() && listing.getFulfillmentMethod().equals(FulfillmentMethod.UPS) );

	}
}
