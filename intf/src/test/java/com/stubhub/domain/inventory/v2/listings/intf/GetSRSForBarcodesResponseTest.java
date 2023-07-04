package com.stubhub.domain.inventory.v2.listings.intf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.util.JsonUtil;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.GetSRSForBarcodesResponse;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.Ticket;

public class GetSRSForBarcodesResponseTest {
 // private static final String RESPONSE_JSON =
 //     "{\"tickets\":[{\"eventId\":123456,\"barcode\":\"12345678901234\",\"ticketClass\":\"TICKET\",\"section\":\"Lower 101\",\"row\":\"1\",\"seat\":\"11\",\"ga\":false,\"attributes\":[]}]}";
 private static final String RESPONSE_JSON  = "{\"tickets\":[{\"eventId\":123456,\"barcode\":\"12345678901234\",\"ticketClass\":\"TICKET\",\"section\":\"Lower 101\",\"row\":\"1\",\"seat\":\"11\",\"ga\":false,\"attributes\":[],\"buyerRestricted\":false}],\"proceedWithDelivery\":true}";
  private GetSRSForBarcodesResponse response;

  @BeforeMethod
  public void setup() {
    response = new GetSRSForBarcodesResponse();
    List<Ticket> tickets = new ArrayList<Ticket>();
    Ticket ticket = new Ticket();
    ticket.setEventId(123456L);
    ticket.setBarcode("12345678901234");
    ticket.setTicketClass(Ticket.TicketClass.TICKET);
    ticket.setSection("Lower 101");
    ticket.setRow("1");
    ticket.setSeat("11");
    ticket.setGa(false);
    ticket.setBuyerRestricted(false);
    tickets.add(ticket);
    response.setTickets(tickets);

  }

  @Test
  public void requestToJson() throws JsonGenerationException, JsonMappingException, IOException {
    String responseJson = JsonUtil.toJson(response);

    System.out.println("response:\n " + responseJson);
    Assert.assertEquals(responseJson, RESPONSE_JSON);
  }

  @Test
  public void jsonToRequest() throws JsonParseException, JsonMappingException, IOException {
    GetSRSForBarcodesResponse response = (GetSRSForBarcodesResponse) JsonUtil
        .toObject(RESPONSE_JSON, GetSRSForBarcodesResponse.class);
    List<Ticket> tickets = response.getTickets();
    Assert.assertNotNull(tickets);
    Assert.assertEquals(tickets.size(), 1);
    Ticket ticket = tickets.get(0);
    Assert.assertEquals(ticket.getEventId().longValue(), 123456);
    Assert.assertEquals(ticket.getBarcode(), "12345678901234");
    Assert.assertEquals(ticket.getTicketClass().toString(), "TICKET");
    Assert.assertEquals(ticket.getSection(), "Lower 101");
    Assert.assertEquals(ticket.getRow(), "1");
    Assert.assertEquals(ticket.getSeat(), "11");
    Assert.assertEquals(ticket.getGa(), Boolean.FALSE);
    Assert.assertNull(ticket.getError());
  }

}
