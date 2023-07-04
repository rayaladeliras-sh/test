package com.stubhub.domain.inventory.v2.listings.intf;

/*
import java.util.ArrayList;

import junit.framework.Assert;

import org.testng.annotations.Test;
import com.stubhub.domain.inventory.v2.listings.intf.DeliveryMethod;
import com.stubhub.domain.inventory.v2.listings.intf.Listing;
import com.stubhub.domain.inventory.v2.listings.intf.ListingAttribute;
import com.stubhub.newplatform.common.entity.Money;

public class ListingTest {
	
	@Test
	 public void testGetSet() {
		
		Listing listing=new Listing();
		Money currentPrice=new Money();
		String currencyCode="US";
		short dirtyTicketInd=1;
		Money displayPricePerTicket=new Money();
		Money faceValue=new Money();
		Money serviceFee=new Money();
		Long eventId=2L;
		String eventName = "Test Event";
		int isPaypalAvailable=1;
		Long listingId=3L;
		int quantity=1;
		String row="2G";
		String seatNumbers="2423";
		Long sectionId=5L;
		String sectionName="ALPHA";
		String sellerSectionName="SELLER-SECTION-NAME";
		short splitOption=1;
		String splitVector="SPLITVECTOR";
		String ticketClass="TICKETCLASS";
		int ticketSplit=1;
		int totalTickets=1;
		Long zoneId=6L;
		String zoneName="MIDDLE";
		
		listing.setCurrencyCode(currencyCode);
		Assert.assertEquals(listing.getCurrencyCode(), currencyCode);
		listing.setCurrentPrice(currentPrice);
		Assert.assertEquals(listing.getCurrentPrice(),currentPrice);
		listing.setDeliveryMethods(new ArrayList<DeliveryMethod>());
		Assert.assertNotNull(listing.getDeliveryMethods());
		listing.setDirtyTicketInd(dirtyTicketInd);
		Assert.assertEquals(listing.getDirtyTicketInd(),Short.valueOf(dirtyTicketInd));
		listing.setDisplayPricePerTicket(displayPricePerTicket);
		Assert.assertEquals(listing.getDisplayPricePerTicket(),displayPricePerTicket);
		listing.setEventId(eventId);
		Assert.assertEquals(listing.getEventId(),eventId);
		listing.setEventName(eventName);
		Assert.assertEquals(listing.getEventName(),eventName);
		listing.setFaceValue(faceValue);
		Assert.assertEquals(listing.getFaceValue(),faceValue);
		listing.setIsPaypalAvailable(isPaypalAvailable);
		Assert.assertEquals(listing.getIsPaypalAvailable(),Integer.valueOf(isPaypalAvailable));
		listing.setListingAttributesList(new ArrayList<ListingAttribute>());
		Assert.assertNotNull(listing.getListingAttributesList());
		listing.setListingId(listingId);
		Assert.assertEquals(listing.getListingId(),listingId);
		listing.setQuantity(quantity);
		Assert.assertEquals(listing.getQuantity(),Integer.valueOf(quantity));
		listing.setRow(row);
		Assert.assertEquals(listing.getRow(), row);
		listing.setSeatNumbers(seatNumbers);
		Assert.assertEquals(listing.getSeatNumbers(),seatNumbers);
		listing.setSectionId(sectionId);
		Assert.assertEquals(listing.getSectionId(), sectionId);
		listing.setSectionName(sectionName);
		Assert.assertEquals(listing.getSectionName(), sectionName);
		listing.setServiceFee(serviceFee);
		Assert.assertNotNull(listing.getServiceFee());
		listing.setSplitOption(splitOption);
		Assert.assertEquals(listing.getSplitOption(),Short.valueOf(splitOption));
		listing.setSplitVector(splitVector);
		Assert.assertEquals(listing.getSplitVector(),splitVector);
		listing.setTicketClass(ticketClass);
		Assert.assertEquals(listing.getTicketClass(), ticketClass);
		listing.setTicketSplit(ticketSplit);
		Assert.assertEquals(listing.getTicketSplit(),Integer.valueOf(ticketSplit));
		listing.setTotalTickets(totalTickets);
		Assert.assertEquals(listing.getTotalTickets(),Integer.valueOf(totalTickets));
		listing.setZoneId(zoneId);
		Assert.assertEquals(listing.getZoneId(),Long.valueOf(zoneId));
		listing.setZoneName(zoneName);
		Assert.assertEquals(listing.getZoneName(), zoneName);
		listing.setSellerSectionName(sellerSectionName);
		Assert.assertEquals(listing.getSellerSectionName(),sellerSectionName);
		
		Assert.assertNotNull(listing.hashCode());
		Assert.assertNotNull(new Listing().hashCode());
		Assert.assertTrue(listing.equals(listing));
		Assert.assertFalse(listing.equals(new Listing()));
		Assert.assertFalse(new Listing().equals(listing));
		Assert.assertFalse(new Listing().equals(null));
		Assert.assertEquals(listing, listing);
					
		//Object other = new CreateIncompleteListingRequest();
		Object different = new String();
			
			
		Assert.assertNotNull(listing.hashCode());
		Assert.assertTrue(listing.equals(listing));
		Assert.assertFalse(listing.equals(different));
		//Assert.assertFalse(listing.equals(other));
		Assert.assertFalse(listing.equals(null));  
		
}

}*/
