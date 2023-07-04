package com.stubhub.domain.inventory.listings.v2.listeners;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.util.JMSMessageHelper;


public class ProcessPendingFlashListingsListenerTest {
  
  private MapMessage message;
  private Long sellerId = 12345L;
  
  @InjectMocks
  private ProcessPendingFlashListingsListener processPendingLockListingsListener;
  
  @Mock
  private InventoryMgr inventoryMgr;
  
  @Mock
  private JMSMessageHelper jmsMessageHelper;
  
  @BeforeMethod
  public void init() {
    processPendingLockListingsListener = new ProcessPendingFlashListingsListener();
    MockitoAnnotations.initMocks(this);
    ReflectionTestUtils.setField(processPendingLockListingsListener, "jmsMessageHelper", jmsMessageHelper);
    message = Mockito.mock(MapMessage.class);
    
  }
  
  @Test
  public void sellerIdReceived() throws JMSException {
    List<Listing> listings = new ArrayList<>();
    Listing listing = new Listing();
    listings.add(listing);
    when(message.getLong("sellerId")).thenReturn(sellerId);
    when(inventoryMgr.getAllPendingFlashListings(sellerId)).thenReturn(listings);
    processPendingLockListingsListener.onMessage(message);
    verify(inventoryMgr, times(1)).getAllPendingFlashListings(sellerId);
    
  }
  
  @Test
  public void getPendingLockListingException() throws JMSException {
    List<Listing> listings = new ArrayList<>();
    Listing listing = new Listing();
    listings.add(listing);
    when(message.getLong("sellerId")).thenReturn(sellerId);
    when(inventoryMgr.getAllPendingFlashListings(sellerId)).thenReturn(null);
    processPendingLockListingsListener.onMessage(message);
    verify(inventoryMgr, times(1)).getAllPendingFlashListings(sellerId);
    
  }
  
  @Test
  public void noPendingLockListingsFound() throws JMSException {
    List<Listing> listings = new ArrayList<>();
    when(message.getLong("sellerId")).thenReturn(sellerId);
    when(inventoryMgr.getAllPendingFlashListings(sellerId)).thenReturn(listings);
    processPendingLockListingsListener.onMessage(message);
    verify(inventoryMgr, times(1)).getAllPendingFlashListings(sellerId);
    
  }
  
  @Test
  public void testHandleException() throws JMSException {
    List<Listing> listings = new ArrayList<>();
    when(message.getLong("sellerId")).thenReturn(sellerId);
    when(inventoryMgr.getAllPendingFlashListings(sellerId)).thenThrow(Exception.class); 
    processPendingLockListingsListener.onMessage(message);
    verify(inventoryMgr, times(1)).getAllPendingFlashListings(sellerId);
  }

}
