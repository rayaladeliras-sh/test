package com.stubhub.domain.inventory.listings.v2.listeners;

import org.testng.annotations.Test;

import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.datamodel.dao.ListingDAO;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

public class LMSLookupListingListenerTest {  
	private static final Long listingId = 1211395778l;
	private static final Long newListingId = 1211395779l;
	private static final String lmsContentXml ="<msg><id>12345</id></msg>";
	private static final Long eventId = 9444637l;
	private static final Long sellerId = 1000019l;
	private static final Long ticketId = 2778861923l;
	private static final String externalId ="1234567";
	private static String section = "MOCK 51";
	private static final String row = "26";
	private static final String seat = "13";
	private static Integer quantity=2;
	private Listing listing;
	private List<Listing> oldListing;
	private List<TicketSeat> ticketSeats;
	private MapMessage message;
	private Session session;

@Mock
private InventoryMgr inventoryManager;

@Mock
private ListingDAO listingDAO;

@InjectMocks
private LMSLookupListingListener lmsLookupListingListener;

@Qualifier(value = "lmsFormMessageTemplate")
private JmsTemplate lmsFormMessageTemplate;


@BeforeMethod
public void init() {

	lmsLookupListingListener = new LMSLookupListingListener();
	MockitoAnnotations.initMocks(this);
	ReflectionTestUtils.setField(lmsLookupListingListener, "inventoryManager", inventoryManager);
	ReflectionTestUtils.setField(lmsLookupListingListener, "listingDAO", listingDAO);
	lmsFormMessageTemplate = Mockito.mock(JmsTemplate.class);
	ReflectionTestUtils.setField(lmsLookupListingListener, "lmsFormMessageTemplate", lmsFormMessageTemplate);

	message = Mockito.mock(MapMessage.class);
	session = Mockito.mock(Session.class);

	  listing = new Listing();
	  listing.setId(listingId);
	  listing.setEventId(eventId);
	  listing.setSellerId(sellerId);
	  listing.setExternalId(externalId);
	  listing.setSection(section);
	  listing.setRow(row);
	  listing.setSeats(seat);
	  listing.setQuantity(quantity);
	  listing.setLmsApprovalStatus(2);
	  listing.setDeliveryOption(1);
	  listing.setTicketMedium(1);
	
	  ticketSeats = new ArrayList<>();
	  TicketSeat ticketSeat = new TicketSeat();
	  ticketSeat.setTicketId(ticketId);
	  ticketSeat.setSection(section);
	  ticketSeat.setRow(row);
	  ticketSeat.setSeatNumber(seat);
	  ticketSeat.setTixListTypeId(1L);
	  ticketSeat.setSeatStatusId(1L);
	  ticketSeat.setGeneralAdmissionInd(false);
	  ticketSeats.add(ticketSeat);
	  listing.setTicketSeats(ticketSeats);
	  
	  oldListing = new ArrayList<>();
	  Listing oldListing1=new Listing();
	  oldListing1.setQuantity(2);
	  oldListing1.setSection(section);
	  oldListing1.setRow(row);
	  oldListing1.setSeats(seat);
	  oldListing1.setSystemStatus("DELETED");
	  oldListing.add(oldListing1);
	 
}
@Test
public void testLMSListingLookupListener() throws JMSException{
    when(message.getLong("listingId")).thenReturn(newListingId);
	when(inventoryManager.getListing(newListingId)).thenReturn(listing);
	when(listingDAO.findLMSListing(eventId, sellerId,section,row)).thenReturn(oldListing);
	
	lmsLookupListingListener.onMessage(message);
	Listing currentListing = inventoryManager.getListing(newListingId);
	Assert.assertNotNull(currentListing);
	List<Listing> oldLMSListing = listingDAO.findLMSListing(eventId, sellerId,section,row);
	Assert.assertEquals(true, lmsLookupListingListener.compareLMSListing(oldLMSListing.get(0),quantity,section,row,seat));
	if ((oldLMSListing!=null)&&lmsLookupListingListener.compareLMSListing(oldLMSListing.get(0),quantity,section,row,seat)){	
		listingDAO.updateTicketLMSApprovalStatus(newListingId);
	}
   }


@SuppressWarnings("unchecked")
@Test
public void onMessageExceptionTest() {
  when(inventoryManager.getListing(listingId)).thenThrow(Exception.class);
  try {
    verify(inventoryManager, never()).getListing(listingId);
  } catch(Exception e) {
    e.printStackTrace();
    Assert.fail("no exception expected", e);
  }
}

@Test
public void testLMSListingLookupListenerCompareListingMismatch() throws JMSException{
    when(message.getLong("listingId")).thenReturn(newListingId);
	when(inventoryManager.getListing(newListingId)).thenReturn(listing);
	when(listingDAO.findLMSListing(eventId, sellerId, section, row)).thenReturn(oldListing);
	
	lmsLookupListingListener.onMessage(message);
	Listing currentListing = inventoryManager.getListing(newListingId);
	Assert.assertNotNull(currentListing);
	List<Listing> oldLMSListing = listingDAO.findLMSListing(eventId, sellerId,section,row);
	quantity=5;
	if ((oldLMSListing!=null)&&lmsLookupListingListener.compareLMSListing(oldLMSListing.get(0),quantity,section,row,seat)){	
		listingDAO.updateTicketLMSApprovalStatus(newListingId);
	}else{
		lmsLookupListingListener.sendCreateLMSListingMessageToOldQueue(lmsContentXml, newListingId);
	}
   }

@Test
public void testLMSListingLookupListenerOldLMSListingNull() throws JMSException{
    when(message.getLong("listingId")).thenReturn(newListingId);
	when(inventoryManager.getListing(newListingId)).thenReturn(listing);
	when(listingDAO.findLMSListing(eventId, sellerId,section,row)).thenReturn(null);
	lmsLookupListingListener.onMessage(message);
	Listing currentListing = inventoryManager.getListing(newListingId);
	Assert.assertNotNull(currentListing);
	List<Listing> oldLMSListing = listingDAO.findLMSListing(eventId, sellerId,section,row);
	oldLMSListing=null;
	Assert.assertNull(oldLMSListing);
   }

@Test
public void testLMSListingLookupListenerListingNull() throws JMSException{
    when(message.getLong("listingId")).thenReturn(newListingId);
	when(inventoryManager.getListing(newListingId)).thenReturn(null);
	lmsLookupListingListener.onMessage(message);
	listing=null;
	Assert.assertNull(listing);
		lmsLookupListingListener.sendCreateLMSListingMessageToOldQueue(lmsContentXml, newListingId);
   }


@Test
public void testLMSListingLookupListenerExternalIdNull() throws JMSException{
	listing.setExternalId(null);
    when(message.getLong("listingId")).thenReturn(newListingId);
    when(message.getString("lmsContentXml")).thenReturn(lmsContentXml);    
	when(inventoryManager.getListing(newListingId)).thenReturn(listing);
	
	lmsLookupListingListener.onMessage(message);
	Listing currentListing = inventoryManager.getListing(newListingId);
	Assert.assertNotNull(currentListing);
	if (currentListing.getExternalId()==null){
		lmsLookupListingListener.sendCreateLMSListingMessageToOldQueue(lmsContentXml, newListingId);
	}
   }

@Test
public void sendCreateLMSListingMessageToOldQueueTest() {
	Mockito.doAnswer(new Answer<Void>() {
	@Override
	public Void answer(InvocationOnMock invocation) throws Throwable {
		return null;
	}
	}).when(lmsFormMessageTemplate).send(Mockito.any(MessageCreator.class));
	lmsLookupListingListener.sendCreateLMSListingMessageToOldQueue(lmsContentXml, newListingId);
}

@Test
public void testLMSListingLookupListenerCompareListingSectionMismatch() throws JMSException{
    when(message.getLong("listingId")).thenReturn(newListingId);
	when(inventoryManager.getListing(newListingId)).thenReturn(listing);
	when(listingDAO.findLMSListing(eventId, sellerId,section,row)).thenReturn(oldListing);
	lmsLookupListingListener.onMessage(message);
	Listing currentListing = inventoryManager.getListing(newListingId);
	Assert.assertNotNull(currentListing);
	List<Listing> oldLMSListing = listingDAO.findLMSListing(eventId, sellerId,section,row);
	quantity=2;
	section = "New Section";
	if ((oldLMSListing!=null)&&lmsLookupListingListener.compareLMSListing(oldLMSListing.get(0),quantity,section,row,seat)){	
		listingDAO.updateTicketLMSApprovalStatus(newListingId);
	}else{
		lmsLookupListingListener.sendCreateLMSListingMessageToOldQueue(lmsContentXml, newListingId);
	}
   }

@Test
public void shouldSendMessage()
{
	lmsFormMessageTemplate.send(new MessageCreator() {
      @Override
      public Message createMessage(Session session) throws JMSException {
         TextMessage  message = session.createTextMessage();
         message.setText("This is test message from MockRunner");
         return message;
      }
   });
}


@Test
public void lmsLookupListingListenerMessageExceptionTest() {
  Mockito.doAnswer(new Answer<Void>() {
    @Override
    public Void answer(InvocationOnMock invocation) throws Throwable {
      throw new Exception();
    }
  }).when(lmsFormMessageTemplate).send(Mockito.any(MessageCreator.class));
  lmsLookupListingListener.sendCreateLMSListingMessageToOldQueue(lmsContentXml, listingId);
}

}
