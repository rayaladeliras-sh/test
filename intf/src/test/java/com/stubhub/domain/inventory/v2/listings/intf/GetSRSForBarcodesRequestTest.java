package com.stubhub.domain.inventory.v2.listings.intf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.GetSRSForBarcodesRequest;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.Ticket;


public class GetSRSForBarcodesRequestTest {
	private static final ObjectMapper jsonMapper = new ObjectMapper();
	//private static final String REQUEST_JSON = "{\"tickets\":[{\"eventId\":123456,\"barcode\":\"12345678901234\",\"ticketClass\":\"TICKET\",\"attributes\":[]}],\"orderId\":null,\"sellerId\":null,\"listingId\":null}";
    private static final String REQUEST_JSON = "{\"tickets\":[{\"eventId\":123456,\"barcode\":\"12345678901234\",\"ticketClass\":\"TICKET\",\"attributes\":[],\"buyerRestricted\":false}],\"orderId\":null,\"sellerId\":null,\"listingId\":null,\"venueConfigSectionId\":null,\"validateBarcode\":true}";

    @BeforeClass
	public void init()
	{
		SerializationConfig serialConf = jsonMapper.getSerializationConfig();
		serialConf.set(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false); //don't throw exceptions on empty objects
		serialConf.set(SerializationConfig.Feature.WRAP_ROOT_VALUE, false);
		jsonMapper.setSerializationConfig(serialConf);
	}

	@Test
	public void requestToJson() throws JsonGenerationException, JsonMappingException, IOException
	{

		GetSRSForBarcodesRequest request = new GetSRSForBarcodesRequest();
		List<Ticket> tickets = new ArrayList<Ticket>();
		Ticket ticket = new Ticket();
		ticket.setEventId(123456L);
		ticket.setBarcode("12345678901234");
		ticket.setTicketClass(Ticket.TicketClass.TICKET);
		ticket.setBuyerRestricted(false);
		tickets.add(ticket);
		request.setTickets(tickets);
		
		String requestJson = jsonMapper.writeValueAsString(request);
		
		System.out.println("request:\n " + requestJson);
		System.out.println("expected:\n " + REQUEST_JSON);

		Assert.assertEquals(requestJson, REQUEST_JSON);
	}
	
	@Test
	public void jsonToRequest() throws JsonParseException, JsonMappingException, IOException
	{
		GetSRSForBarcodesRequest request = jsonMapper.readValue(REQUEST_JSON, GetSRSForBarcodesRequest.class);
		List<Ticket> tickets = request.getTickets();
		Assert.assertNotNull(tickets);
		Assert.assertEquals(tickets.size(), 1);
		Ticket ticket = tickets.get(0);
		Assert.assertEquals(ticket.getEventId().longValue(), 123456);
		Assert.assertEquals(ticket.getBarcode(), "12345678901234");
	}

}
