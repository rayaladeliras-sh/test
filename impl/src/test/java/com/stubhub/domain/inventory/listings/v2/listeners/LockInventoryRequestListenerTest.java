package com.stubhub.domain.inventory.listings.v2.listeners;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.biz.v2.intf.IntegrationManager;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.enums.DeliveryOptionEnum;
import com.stubhub.domain.inventory.listings.v2.util.JMSMessageHelper;
import com.stubhub.domain.inventory.listings.v2.util.PartnerIntegrationHelper;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.domain.inventory.v2.enums.TicketMedium;
import com.stubhub.domain.inventory.v2.enums.TicketSeatStatusEnum;
import com.stubhub.domain.partnerintegration.datamodel.enums.TicketStatusEnum;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.LockInventoryRequest;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.PartnerListing;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.PartnerProduct;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.ProductType;
import com.stubhub.domain.user.contactsV2.intf.CustomerContactV2Details;
import com.stubhub.domain.user.contactsV2.intf.CustomerContactV2Details.Address;
import com.stubhub.domain.user.contactsV2.intf.CustomerContactV2Details.Name;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;

public class LockInventoryRequestListenerTest {

  private static final Long listingId = 1211395778l;
  private static final Long eventId = 9444637l;
  private static final Long sellerId = 1000019l;
  private static final Long ticketId = 2778861923l;
  private static final String section = "MOCK 51";
  private static final String row = "26";
  private static final String seat = "13";
  private String sellerGuid = "11267087";
  private String CALLBACK_PAYLOAD = "{\"msg\":\"true\"}";
  private Listing listing;
  private CustomerContactV2Details contactV2;
  private List<TicketSeat> ticketSeats;
  private LockInventoryRequest lockInventoryRequest;
  private MapMessage message;

  @Mock
  private IntegrationManager integrationManager;

  @InjectMocks
  private LockInventoryRequestListener lockInventoryListener;

  @Mock
  private UserHelper userHelper;

  @Mock
  private PartnerIntegrationHelper partnerIntegrationHelper;

  @Mock
  private JMSMessageHelper jmsMessageHelper;
  
  @Mock
  private MasterStubhubPropertiesWrapper masterStubhubPropertiesWrapper;


  @BeforeMethod
  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);

    message = Mockito.mock(MapMessage.class);

    listing = new Listing();
    listing.setId(listingId);
    listing.setEventId(eventId);
    listing.setSellerId(sellerId);
    listing.setSystemStatus(ListingStatus.PENDING.toString());
    listing.setSellerPaymentTypeId(12345l);
    listing.setSellerContactId(12345l);
    listing.setListingType(1l);
    listing.setDeliveryOption(1);
    listing.setTicketMedium(3);

    ticketSeats = new ArrayList<>();
    TicketSeat ticketSeat = new TicketSeat();
    ticketSeat.setTicketId(ticketId);
    ticketSeat.setSection(section);
    ticketSeat.setRow(row);
    ticketSeat.setSeatNumber(seat);
    ticketSeat.setTixListTypeId(1L);
    ticketSeat.setSeatStatusId(TicketSeatStatusEnum.AVAILABLE.getCode().longValue());
    ticketSeat.setGeneralAdmissionInd(false);
    ticketSeats.add(ticketSeat);
    TicketSeat ticketSeat1 = new TicketSeat();
    ticketSeat1.setTicketId(ticketId);
    ticketSeat1.setSection(section);
    ticketSeat1.setRow(row);
    ticketSeat1.setSeatNumber("14");
    ticketSeat1.setTixListTypeId(1L);
    ticketSeat1.setSeatStatusId(TicketSeatStatusEnum.REMOVED.getCode().longValue());
    ticketSeat1.setGeneralAdmissionInd(false);
    ticketSeats.add(ticketSeat1);
    listing.setTicketSeats(ticketSeats);

    contactV2 = new CustomerContactV2Details();
    Address address = new Address();
    address.setCity("San Francisco");
    address.setLine1("Fremont St");
    address.setState("CA");
    contactV2.setAddress(address);
    Name name = new Name();
    name.setFirstName("Test");
    name.setLastName("Test");
    contactV2.setName(name);
    contactV2.setPhoneNumber("123-456-7890");
    contactV2.setEmail("test@gmail.com");
    contactV2.setId("12345");

    PartnerListing partnerListing = new PartnerListing();
    partnerListing.setId(listingId);
    partnerListing.setSellerId(1000019l);
    partnerListing.setEventId(eventId);
    partnerListing.setPricePerProduct("124");

    PartnerProduct partnerProduct = new PartnerProduct();
    partnerProduct.setSeatId(2778861923l);
    partnerProduct.setFulfillmentArtifact("7yu8-0o9i8u7y");
    partnerProduct.setSection(section);
    partnerProduct.setSeat(seat);
    partnerProduct.setRow(row);
    partnerProduct.setProductType(ProductType.TICKET);
    partnerProduct.setSeatStatus(TicketStatusEnum.AVAILABLE);
    partnerProduct.setAttributes(null);
    List<PartnerProduct> products = new ArrayList<>();
    products.add(partnerProduct);
    partnerListing.setProducts(products);

    lockInventoryRequest = new LockInventoryRequest();
    lockInventoryRequest.setSellerContact(contactV2);
    lockInventoryRequest.setListing(partnerListing);
 
  }

  private void setupReturnValues() throws JMSException {
    when(masterStubhubPropertiesWrapper.getProperty(anyString())).thenReturn("false");
    when(message.getLong("listingId")).thenReturn(listingId);
    when(message.itemExists("transfer")).thenReturn(true);
    when(message.getBoolean("transfer")).thenReturn(true);
    when(message.getString("seatMap")).thenReturn("1234-4567,1235-4568");
    when(integrationManager.getUserGuid(sellerId)).thenReturn(sellerGuid);
    when(userHelper.getDefaultCustomerContactV2(sellerGuid, true)).thenReturn(contactV2);
    // when(message.getString("contactV2")).thenReturn(com.stubhub.domain.inventory.common.util.JsonUtil.toJson(contactV2));
    when(integrationManager.getListing(listingId)).thenReturn(listing);
    when(partnerIntegrationHelper.isPartnerIntegratedOnSHIP(eventId)).thenReturn(true);
    
    when(message.itemExists("isCallback")).thenReturn(true);
    when(message.getString("callbackPayload")).thenReturn(CALLBACK_PAYLOAD);
  }


  @Test
  @org.junit.Test
  public void onMessageTestWithNoTicketSeats() throws JMSException {
    listing.setTicketSeats(null);
    setupReturnValues();

    lockInventoryListener.onMessage(message);
    verify(partnerIntegrationHelper, never()).isPartnerIntegratedOnSHIP(eventId);
  }

  @Test
  @org.junit.Test
  public void onMessageTestWithNonIntegrated() throws JMSException {
    setupReturnValues();
    when(partnerIntegrationHelper.isPartnerIntegratedOnSHIP(eventId)).thenReturn(false);
    Mockito.doNothing().when(jmsMessageHelper).sendLockMessage(listingId);
    lockInventoryListener.onMessage(message);
    verify(jmsMessageHelper, times(1)).sendLockMessage(listingId);
  }

  @Test
  @org.junit.Test
  public void onMessageTestWithIntegratedManualDelivery() throws JMSException {
    listing.setDeliveryOption((int) DeliveryOptionEnum.MANUAL_DELIVERY.getDeliveryOption());
    setupReturnValues();
    lockInventoryListener.onMessage(message);
    verify(integrationManager, times(0)).getUserGuid(listing.getSellerId());
  }

  @Test
  @org.junit.Test
  public void onMessageTestWithIntegratedNullDelivery() throws JMSException {
    listing.setDeliveryOption(null);
    setupReturnValues();
    lockInventoryListener.onMessage(message);
    verify(integrationManager, times(0)).getUserGuid(listing.getSellerId());
  }

  @Test
  @org.junit.Test
  public void onMessageTestWithIntegratedPaperMedium() throws JMSException {
    listing.setTicketMedium(TicketMedium.PAPER.getId());
    setupReturnValues();
    lockInventoryListener.onMessage(message);
    verify(integrationManager, times(0)).getUserGuid(listing.getSellerId());
  }

  @Test
  @org.junit.Test
  public void onMessageTestWithIntegratedNullSellerGuid() throws JMSException {
    sellerGuid = null;
    setupReturnValues();
    lockInventoryListener.onMessage(message);
    verify(userHelper, times(0)).getDefaultCustomerContactV2(sellerGuid, true);
  }

  @Test
  @org.junit.Test
  public void onMessageTestWithIntegratedNullContact() throws JMSException {
    contactV2 = null;
    setupReturnValues();
    lockInventoryListener.onMessage(message);
    verify(integrationManager, times(0)).createLockInventoryRequest(listing, contactV2);
  }

  @Test
  @org.junit.Test
  public void onMessageTestWithIntegratedNullLockRequest() throws JMSException {
    when(integrationManager.createLockInventoryRequest(listing, contactV2)).thenReturn(null);
    setupReturnValues();
    lockInventoryListener.onMessage(message);
    verify(jmsMessageHelper, times(0)).sendUnlockInventoryMessage(listingId);
  }

  @Test
  @org.junit.Test
  public void onMessageTest() throws JMSException {
    when(integrationManager.createLockInventoryRequest(listing, contactV2))
        .thenReturn(lockInventoryRequest);
    setupReturnValues();
    lockInventoryListener.onMessage(message);
    verify(jmsMessageHelper, times(1)).sendUnlockInventoryMessage(listingId);
    verify(jmsMessageHelper, times(1)).sendPartnerLockInventoryMessage((LockInventoryRequest)any(), anyBoolean(), anyString(), anyBoolean(), anyString());
  }

  @Test
  @org.junit.Test
  public void onMessageTestException() throws JMSException {
    when(integrationManager.createLockInventoryRequest(listing, contactV2))
        .thenReturn(lockInventoryRequest);
    Mockito.doThrow(new RuntimeException("Unable to send message")).when(jmsMessageHelper)
        .sendPartnerLockInventoryMessage((LockInventoryRequest)any(), anyBoolean(), anyString(), anyBoolean());
    setupReturnValues();
    lockInventoryListener.onMessage(message);
  }

}
