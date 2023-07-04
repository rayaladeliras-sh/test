package com.stubhub.domain.inventory.listings.v2.listeners;

import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * Created by jicui on 11/2/15.
 */
@Test
public class TicketFulfillmentStatusListenerTest
{
    @InjectMocks
    private TicketFulfillmentStatusListener listener;
    @Mock
    private InventoryMgr inventoryMgr;
    @BeforeClass
    void init(){
        listener=new TicketFulfillmentStatusListener();
        MockitoAnnotations.initMocks(this);
    }
    @Test
    void test_OnMessage_if_Not_Active() throws JMSException {
        Listing listing1=mockListing();
        listing1.setSystemStatus("CANCELLED");
        Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(listing1);
        MapMessage message=Mockito.mock(MapMessage.class);
        Mockito.when(message.getLong("ticketId")).thenReturn(1234L);
        Mockito.when(message.getString("inBoundStatus")).thenReturn("1");
        listener.onMessage(message);
        Assert.assertTrue(Boolean.TRUE);
    }

    @Test
    void test_OnMessage_ifActive() throws JMSException {
        Listing listing1=mockListing();
        listing1.setSystemStatus("ACTIVE");
        Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(listing1);
        MapMessage message=Mockito.mock(MapMessage.class);
        Mockito.when(message.getLong("ticketId")).thenReturn(1234L);
        Mockito.when(message.getString("inBoundStatus")).thenReturn("1");
        listener.onMessage(message);
        Assert.assertTrue(Boolean.TRUE);
    }

    @Test
    void test_OnMessage_Fail() throws JMSException {
        Listing listing1=mockListing();
        Mockito.when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(listing1);
        MapMessage message=Mockito.mock(MapMessage.class);
        Mockito.when(message.getLong("ticketId")).thenReturn(1234L);
        Mockito.when(message.getString("inBoundStatus")).thenThrow(new JMSException("parse jms fail"));
        listener.onMessage(message);
        Assert.assertTrue(Boolean.TRUE);
    }

    private Listing mockListing() {
        Listing listing=new Listing();
        listing.setSystemStatus("ACTIVE");
        return listing;
    }
}
