package com.stubhub.domain.inventory.v2.DTO;

import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.HashSet;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.entity.SaleMethod;
import com.stubhub.domain.inventory.common.entity.SplitOption;
import com.stubhub.newplatform.common.entity.Money;
 
public class ListingResponseTest {

	@Test
	 public void testGetSet() {
		ListingResponse listingResponse =new ListingResponse();
		String comments="COMMENT";
	 	String externalId="231";
		String id= "34";
		int quantity=1;
		String row="Middle";
	 	String seats="2345";
		String section="G";
		String contactGuid="4x8uX_QvyN_acu_F";
		int split=2;
		Money m=new Money();
        try{
        	GregorianCalendar gcal = new GregorianCalendar();
			XMLGregorianCalendar inhandDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
			XMLGregorianCalendar saleEndDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
			listingResponse.setInternalNotes(comments);
			Assert.assertEquals(listingResponse.getInternalNotes(),comments);
			listingResponse.setDeliveryOption(DeliveryOption.BARCODE);
			Assert.assertEquals(listingResponse.getDeliveryOption(),DeliveryOption.BARCODE);
			listingResponse.setExternalListingId(externalId);
			Assert.assertEquals(listingResponse.getExternalListingId(),externalId);
			listingResponse.setFaceValue(m);
			Assert.assertEquals(listingResponse.getFaceValue(),m);
			listingResponse.setId(id);
			Assert.assertEquals(listingResponse.getId(),id);
			listingResponse.setInhandDate(inhandDate.toString());
			Assert.assertEquals(listingResponse.getInhandDate(),inhandDate.toString());
		 	listingResponse.setPreDelivered(true);
		  	Assert.assertNotNull(listingResponse.getPreDelivered());
		 	listingResponse.setQuantity(quantity);
		  	Assert.assertEquals(listingResponse.getQuantity(),Integer.valueOf(quantity));
		  	listingResponse.setQuantityRemain(quantity);
			Assert.assertEquals(listingResponse.getQuantityRemain(),Integer.valueOf(quantity));
 			listingResponse.setRows(row);
 			Assert.assertEquals(listingResponse.getRows(), row);
 			listingResponse.setSaleEndDate(saleEndDate.toString());
 			Assert.assertEquals(listingResponse.getSaleEndDate(), saleEndDate.toString());
 			listingResponse.setSeats(seats);
 			Assert.assertEquals(listingResponse.getSeats(),seats);
 			listingResponse.setSection(section);
 			Assert.assertEquals(listingResponse.getSection(), section);
 			listingResponse.setSplitQuantity(split);
 			Assert.assertEquals(listingResponse.getSplitQuantity(),Integer.valueOf(split));
 			listingResponse.setSplitOption(SplitOption.NOSINGLES);
   			Assert.assertEquals(listingResponse.getSplitOption(),SplitOption.NOSINGLES);
   			listingResponse.setStatus(ListingStatus.ACTIVE);
	   		Assert.assertEquals(listingResponse.getStatus(),ListingStatus.ACTIVE);
	   		listingResponse.setCcId(1L);
	   		Assert.assertEquals(listingResponse.getCcId(),new Long(1L));
	   		listingResponse.setContactId(2L);
	   		Assert.assertEquals(listingResponse.getContactId(),new Long(2L));
	   		listingResponse.setContactGuid(contactGuid);
	   		Assert.assertEquals(listingResponse.getContactGuid(), contactGuid);

	   		listingResponse.setPaymentType("2");
	   		Assert.assertEquals(listingResponse.getPaymentType(),"2");
	   		listingResponse.setSaleMethod(SaleMethod.FIXED);
	   		Assert.assertEquals(listingResponse.getSaleMethod(),SaleMethod.FIXED);
	   		listingResponse.setStartPricePerTicket(m);
	   		Assert.assertEquals(listingResponse.getStartPricePerTicket(),m);
	   		listingResponse.setEndPricePerTicket(m);
	   		Assert.assertEquals(listingResponse.getEndPricePerTicket(),m);
	   		listingResponse.setTicketTraits(new HashSet<TicketTrait>());
			Assert.assertNotNull(listingResponse.getTicketTraits());
			listingResponse.setVenueConfigSectionId(1L);
			Assert.assertEquals(listingResponse.getVenueConfigSectionId(),new Long(1L));
			listingResponse.setPurchasePrice(m);
	   		Assert.assertEquals(listingResponse.getPurchasePrice(),m);
	   		listingResponse.setScrubbedSectionName("GA");
	   		Assert.assertEquals(listingResponse.getScrubbedSectionName(), "GA");
   			
 	   	
 			Assert.assertNotNull(listingResponse.hashCode());
		 	Assert.assertNotNull(new ListingResponse().hashCode());
			Assert.assertTrue(listingResponse.equals(listingResponse));
		  	Assert.assertFalse(listingResponse.equals(new ListingResponse()));
		  	Assert.assertFalse(new ListingResponse().equals(listingResponse));
		  	Assert.assertFalse(new ListingResponse().equals(null));
		 	Assert.assertEquals(listingResponse, listingResponse);
		 	Assert.assertNotEquals(listingResponse, new Integer(0));
		   	
		 	Object other = new ListingResponse();
		 	Object different = new String();
			   	
			Assert.assertNotNull(listingResponse.hashCode());
		 	Assert.assertFalse(listingResponse.equals(different));
 			Assert.assertFalse(listingResponse.equals(other));
 		    Assert.assertFalse(listingResponse.equals(null));
 		    
 		    listingResponse.setEventId("123");
 		    listingResponse.setEventDescription("Test");
 		    listingResponse.setEventDate(null);
 		    listingResponse.setVenueDescription("Test Venue");
 		    Assert.assertNotNull(listingResponse.getEventId());
 		    Assert.assertNotNull(listingResponse.getEventDescription());
 		    Assert.assertNotNull(listingResponse.getVenueDescription());
 		    Assert.assertNull(listingResponse.getEventDate());
 		    
 		    listingResponse.setPrimaryTicket(Boolean.TRUE);
 		    Assert.assertNotNull(listingResponse.isPrimaryTicket());
 		    Assert.assertTrue(listingResponse.isPrimaryTicket().booleanValue());
	   	
 		    listingResponse.setSectionMappingRequired(Boolean.TRUE);
 		    Assert.assertNotNull(listingResponse.isSectionMappingRequired());
 		    Assert.assertTrue(listingResponse.isSectionMappingRequired().booleanValue());

 		    listingResponse.setHideSeats(Boolean.TRUE);
 		    Assert.assertNotNull(listingResponse.isHideSeats());
 		    Assert.assertTrue(listingResponse.isHideSeats().booleanValue());
 		    listingResponse.setSellerId(123L);
 		    listingResponse.setEventDate("2017-04-27");
 		    listingResponse.setPricePerProduct(new Money(new BigDecimal(10),"USD"));
 		    Assert.assertTrue(listingResponse.toString().contains("eventId=123"));
        } catch(Exception e){
        	
        } 
	}
}