package com.stubhub.domain.inventory.listings.v2.listeners;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.biz.v2.intf.IntegrationManager;
import com.stubhub.domain.partnerintegration.datamodel.enums.TicketStatusEnum;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.LockInventoryResponse;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.PartnerListing;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.PartnerProduct;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.ProductType;

public class LockInventoryResponseListenerTest {

  private String response;
  private MapMessage message;

  @Mock
  private IntegrationManager integrationManager;

  @InjectMocks
  private LockInventoryResponseListener lockInventoryListener;

  @BeforeMethod
  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);

    message = Mockito.mock(MapMessage.class);

    PartnerListing partnerListing = new PartnerListing();
    partnerListing.setId(12345L);
    partnerListing.setSellerId(12345L);
    partnerListing.setEventId(12345L);
    partnerListing.setPricePerProduct("100");

    PartnerProduct partnerProduct = new PartnerProduct();
    partnerProduct.setSeatId(12345L);
    partnerProduct.setFulfillmentArtifact("7yu8-0o9i8u7y");
    partnerProduct.setSection("sec");
    partnerProduct.setSeat("1");
    partnerProduct.setRow("row");
    partnerProduct.setProductType(ProductType.TICKET);
    partnerProduct.setSeatStatus(TicketStatusEnum.AVAILABLE);
    partnerProduct.setAttributes(null);
    List<PartnerProduct> products = new ArrayList<>();
    products.add(partnerProduct);
    partnerListing.setProducts(products);

    LockInventoryResponse lockInventoryResponse;
    lockInventoryResponse = new LockInventoryResponse();
    lockInventoryResponse.setListing(partnerListing);

    try {
      ObjectMapper mapper = new ObjectMapper();
      response = mapper.writeValueAsString(lockInventoryResponse);
    } catch (Exception e) {

    }
  }

  @Test
  @org.junit.Test
  public void onMessageTestWithNoResponse() throws JMSException {
    when(message.getString("response")).thenReturn(null);
    lockInventoryListener.onMessage(message);
    verify(integrationManager, never()).updateListingAfterLock(any(LockInventoryResponse.class));
  }

  @Test
  @org.junit.Test
  public void onMessageTestWithInvalidResponse() throws JMSException {
    when(message.getString("response")).thenReturn("dummy");
    lockInventoryListener.onMessage(message);
    verify(integrationManager, never()).updateListingAfterLock(any(LockInventoryResponse.class));
  }

  @Test
  @org.junit.Test
  public void onMessageTestWithNoListing() throws JMSException {
    when(message.getString("response")).thenReturn("{\"inventoryId\":\"1234\"}");
    lockInventoryListener.onMessage(message);
    verify(integrationManager, never()).updateListingAfterLock(any(LockInventoryResponse.class));
  }

  @Test
  @org.junit.Test
  public void onMessageTest() throws JMSException {
    when(message.getString("response")).thenReturn(response);
    Mockito.doNothing().when(integrationManager)
        .updateListingAfterLock(Mockito.any(LockInventoryResponse.class));
    lockInventoryListener.onMessage(message);
    verify(integrationManager, never()).updateListingAfterLock(any(LockInventoryResponse.class));
  }

  @Test
  @org.junit.Test
  public void onMessageTestWithException() throws JMSException {
    when(message.getString("response")).thenReturn(response);
    Mockito.doThrow(new RuntimeException("dummy")).when(integrationManager)
        .updateListingAfterLock(Mockito.any(LockInventoryResponse.class));
    lockInventoryListener.onMessage(message);
    verify(integrationManager, never()).updateListingAfterLock(any(LockInventoryResponse.class));
  }

}
