package com.stubhub.domain.inventory.listings.v2.helper;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.datamodel.entity.FulfillmentMethod;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOptions;
import com.stubhub.domain.inventory.listings.v2.entity.UserContact;
import com.stubhub.domain.inventory.listings.v2.util.EventHelper;
import com.stubhub.domain.inventory.listings.v2.util.JMSMessageHelper;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.LockInventoryRequest;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.UnlockInventoryRequest;

public class JMSMessageHelperTest {

  @InjectMocks
  private JMSMessageHelper jmsMessageHelper = new JMSMessageHelper();

  @Mock
  private EventHelper eventHelper;

  @Mock
  private UserHelper user;

  @Mock
  private Connection mockConnection;
  @Mock
  private MessageProducer mockProducer;
  @Mock
  private Session mockSession;
  @Mock
  private MapMessage mockMapMessage;
  @Mock
  private TextMessage mockTextMessage;

  @BeforeMethod
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    JmsTemplate mockTemplate = new JmsTemplate(new ConnectionFactory() {
      @Override
      public Connection createConnection(String arg0, String arg1) throws JMSException {
        return mockConnection;
      }

      @Override
      public Connection createConnection() throws JMSException {
        return mockConnection;
      }
    });
    mockTemplate.setDefaultDestinationName("defaultQueue");

    ReflectionTestUtils.setField(jmsMessageHelper, "unlockBarcodeMessageTemplate", mockTemplate);
    ReflectionTestUtils.setField(jmsMessageHelper, "lockBarcodeMessageTemplate", mockTemplate);
    ReflectionTestUtils.setField(jmsMessageHelper, "lmsFormMessageTemplate", mockTemplate);
    ReflectionTestUtils.setField(jmsMessageHelper, "lmsLookupFormMessageTemplate", mockTemplate);
    ReflectionTestUtils.setField(jmsMessageHelper, "shareWithFriendsTemplate", mockTemplate);
    ReflectionTestUtils.setField(jmsMessageHelper, "lockInventoryRequestProducer", mockTemplate);
    ReflectionTestUtils.setField(jmsMessageHelper, "unlockInventoryRequestProducer", mockTemplate);
    ReflectionTestUtils.setField(jmsMessageHelper, "partnerLockInventoryRequestProducer",
        mockTemplate);
    ReflectionTestUtils.setField(jmsMessageHelper, "partnerUnlockInventoryRequestProducer",
        mockTemplate);

    try {
      when(mockConnection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(mockSession);
      when(mockSession.createMapMessage()).thenReturn(mockMapMessage);
      when(mockSession.createTextMessage(anyString())).thenReturn(mockTextMessage);

    } catch (Exception e) {
      e.printStackTrace();
    }
/*
		lmsLookupFormMessageTemplate = Mockito.mock(JmsTemplate.class);
		ReflectionTestUtils.setField(jmsMessageHelper, "lmsLookupFormMessageTemplate", lmsLookupFormMessageTemplate);*/		
  }

  @Test
  @org.junit.Test
  public void sendUnlockBarcodeMessageTest() throws JMSException {
    when(mockSession.createProducer(any(Destination.class))).thenReturn(mockProducer);
    jmsMessageHelper.sendUnlockBarcodeMessage(1234567L);
    verify(mockSession, times(1)).createTextMessage(anyString());
  }

  @Test
  @org.junit.Test
  public void sendUnlockBarcodeMessageWithException() throws JMSException {
    jmsMessageHelper.sendUnlockBarcodeMessage(1234567L);
    verify(mockSession, times(1)).createTextMessage(anyString());
  }

  @Test
  @org.junit.Test
  public void sendShareWithFriendsMessageTest() throws JMSException {
    when(mockSession.createProducer(any(Destination.class))).thenReturn(mockProducer);
    List<Map<String, String>> orderItemToSeatMap = new ArrayList<>();
    jmsMessageHelper.sendShareWithFriendsMessage(1234567L, orderItemToSeatMap, "70@testmail.com",
        "6C21FF95408F3BC0E04400144FB7AAA6", "1234568", null);
    verify(mockSession, times(1)).createTextMessage(anyString());
  }

  @Test
  @org.junit.Test
  public void sendShareWithFriendsMessageTestWithPaymentId() throws JMSException {
    when(mockSession.createProducer(any(Destination.class))).thenReturn(mockProducer);
    List<Map<String, String>> orderItemToSeatMap = new ArrayList<>();
    jmsMessageHelper.sendShareWithFriendsMessage(1234567L, orderItemToSeatMap, "70@testmail.com",
        "6C21FF95408F3BC0E04400144FB7AAA6", "1234568", "2");
    verify(mockSession, times(1)).createTextMessage(anyString());
  }

  @Test
  @org.junit.Test
  public void sendShareWithFriendsMessageTestWithException() throws JMSException {
    // when(mockSession.createProducer(any(Destination.class))).thenReturn(mockProducer);
    List<Map<String, String>> orderItemToSeatMap = new ArrayList<>();
    jmsMessageHelper.sendShareWithFriendsMessage(1234567L, orderItemToSeatMap, "70@testmail.com",
        "6C21FF95408F3BC0E04400144FB7AAA6", "1234568", null);
    verify(mockSession, times(1)).createTextMessage(anyString());
  }

  @Test
  @org.junit.Test
  public void sendLockMessageTest() throws JMSException {
    when(mockSession.createProducer(any(Destination.class))).thenReturn(mockProducer);
    jmsMessageHelper.sendLockMessage(1234567L);
    verify(mockSession, times(1)).createTextMessage(anyString());
  }

  @Test
  @org.junit.Test
  public void sendLockMessageTestWithException() throws JMSException {
    jmsMessageHelper.sendLockMessage(1234567L);
    verify(mockSession, times(1)).createTextMessage(anyString());
  }

  private Listing getListing() throws Exception {
    Listing testListing = new Listing();
    UserContact userContact = new UserContact();
    Mockito.when(user.getDefaultUserContact(Mockito.any(Long.class))).thenReturn(userContact);
    com.stubhub.domain.inventory.datamodel.entity.Event dEvent =
        new com.stubhub.domain.inventory.datamodel.entity.Event();
    dEvent.setId(1000l);
    dEvent.setActive(true);
    dEvent.setCurrency(Currency.getInstance("USD"));
    dEvent.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
    dEvent.setDescription("Event description");
    dEvent.setEventDate(new GregorianCalendar(2012, 10, 1));
    dEvent.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
    dEvent.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
    List<FulfillmentMethod> fulfillmentMethods = new ArrayList<FulfillmentMethod>();
    FulfillmentMethod fulfillmentMethod = new FulfillmentMethod();
    fulfillmentMethod.setName(DeliveryOptions.PDF);
    Calendar date = Calendar.getInstance();
    date.add(Calendar.DATE, 2);
    fulfillmentMethod.setEndDate(date);
    fulfillmentMethods.add(fulfillmentMethod);
    dEvent.setFulfillmentMethods(fulfillmentMethods);
    dEvent.setGenrePath("45/34");
    dEvent.setGeoPath("12/34");
    when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(dEvent);


    testListing.setSellerId(12345L);
    testListing.setEventId(12345L);
    testListing.setId(12345L);
    testListing.setSeats("1,2");
    com.stubhub.newplatform.common.entity.Money money =
        new com.stubhub.newplatform.common.entity.Money();
    money.setAmount(new BigDecimal(5.0));
    money.setCurrency("USD");
    testListing.setListPrice(money);
    testListing.setQuantity(3);
    testListing.setLastModifiedDate(Calendar.getInstance());
    testListing.setEndDate(Calendar.getInstance());
    testListing.setQuantityRemain(3);
    testListing.setSystemStatus("ACTIVE");
    return testListing;
  }

  @Test
  @org.junit.Test
  public void sendCreateLMSListingMessageTest() throws Exception {
    when(mockSession.createProducer(any(Destination.class))).thenReturn(mockProducer);
    jmsMessageHelper.sendCreateLMSListingMessage(getListing());
    verify(mockSession, times(1)).createMapMessage();
  }
  
  @Test
  @org.junit.Test
  public void sendCreateLMSListingMessageTestWithException() throws Exception {
    jmsMessageHelper.sendCreateLMSListingMessage(getListing());
    verify(mockSession, times(1)).createMapMessage();
  }

  @Test
  @org.junit.Test
  public void sendLockInventoryMessageTest() throws Exception {
    when(mockSession.createProducer(any(Destination.class))).thenReturn(mockProducer);
    jmsMessageHelper.sendLockInventoryMessage(12345L);
    verify(mockSession, times(1)).createMapMessage();
  }

  @Test
  @org.junit.Test
  public void sendLockInventoryMessageTestWithException() throws Exception {
    jmsMessageHelper.sendLockInventoryMessage(12345L);
    verify(mockSession, times(1)).createMapMessage();
  }

  @Test
  @org.junit.Test
  public void sendLockTransferMessageTest() throws JMSException {
    List<Map<String, String>> transferredSeatMap = new ArrayList<>();
    Map<String, String> map = new HashMap<>();
    map.put("itemId", "2906366759");
    map.put("seatId", "2907141907");
    transferredSeatMap.add(map);
    when(mockSession.createProducer(any(Destination.class))).thenReturn(mockProducer);
    jmsMessageHelper.sendLockTransferMessage("12345", true, transferredSeatMap);
    verify(mockSession, times(1)).createMapMessage();
  }

  @Test
  @org.junit.Test
  public void sendLockTransferMessageTestWithException() throws JMSException {
    List<Map<String, String>> transferredSeatMap = new ArrayList<>();
    Map<String, String> map = new HashMap<>();
    map.put("itemId", "2906366759");
    map.put("seatId", "2907141907");
    transferredSeatMap.add(map);
    jmsMessageHelper.sendLockTransferMessage("12345", true, transferredSeatMap);
    verify(mockSession, times(1)).createMapMessage();
  }

  @Test
  @org.junit.Test
  public void sendUnlockInventoryMessageTest() throws Exception {
    when(mockSession.createProducer(any(Destination.class))).thenReturn(mockProducer);
    jmsMessageHelper.sendUnlockInventoryMessage(12345L);
    verify(mockSession, times(1)).createMapMessage();
  }

  @Test
  @org.junit.Test
  public void sendUnlockInventoryMessageTestWithException() throws Exception {
    jmsMessageHelper.sendUnlockInventoryMessage(12345L);
    verify(mockSession, times(1)).createMapMessage();
  }

  @Test
  @org.junit.Test
  public void sendPartnerLockInventoryMessageTest() throws Exception {
    when(mockSession.createProducer(any(Destination.class))).thenReturn(mockProducer);
    jmsMessageHelper.sendPartnerLockInventoryMessage(new LockInventoryRequest(), true, null, false);
    verify(mockSession, times(1)).createMapMessage();
  }

  @Test
  @org.junit.Test
  public void sendPartnerLockInventoryMessageTestWithException() throws Exception {
    jmsMessageHelper.sendPartnerLockInventoryMessage(new LockInventoryRequest(), true, null, false);
    verify(mockSession, times(1)).createMapMessage();
  }

  @Test
  @org.junit.Test
  public void sendPartnerUnlockInventoryMessageTest() throws Exception {
    when(mockSession.createProducer(any(Destination.class))).thenReturn(mockProducer);
    jmsMessageHelper.sendPartnerUnlockInventoryMessage(new UnlockInventoryRequest(), false);
    verify(mockSession, times(1)).createMapMessage();
  }

  @Test
  @org.junit.Test
  public void sendPartnerUnlockInventoryMessageTestWithException() throws Exception {
    jmsMessageHelper.sendPartnerUnlockInventoryMessage(new UnlockInventoryRequest(), false);
    verify(mockSession, times(1)).createMapMessage();
  }

}
