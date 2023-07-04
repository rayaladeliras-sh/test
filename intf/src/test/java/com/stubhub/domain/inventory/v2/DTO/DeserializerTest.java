package com.stubhub.domain.inventory.v2.DTO;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.junit.Assert;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.entity.SaleMethod;
import com.stubhub.domain.inventory.common.entity.SplitOption;
import com.stubhub.domain.inventory.v2.enums.TicketMedium;

public class DeserializerTest {

	@Test
	public void testDeliveryOption() throws Exception 
	{
		DeliveryOptionDeserializer deliveryOptionDeserializer = new DeliveryOptionDeserializer();
		
		JsonParser jp = createParser ( "{ \"deliveryOption\":\"UPS\" }" );
		jp.nextToken(); jp.nextToken(); jp.nextToken();
	
		DeliveryOption option = deliveryOptionDeserializer.deserialize( jp, null);
		Assert.assertTrue( option != null );
	}
	
	@Test
	public void testListingStatus() throws Exception 
	{
		ListingStatusDeserializer listingStatusDeserializer = new ListingStatusDeserializer();
		
		JsonParser jp = createParser ( "{ \"listingStatus\":\"ACTIVE\" }" );
		jp.nextToken(); jp.nextToken(); jp.nextToken();
	
		ListingStatus option = listingStatusDeserializer.deserialize( jp, null);
		Assert.assertTrue( option != null );
	}
		
	@Test
	public void testSaleMethod() throws Exception 
	{
		SaleMethodDeserilaizer saleMethodDeserilaizer = new SaleMethodDeserilaizer();
		
		JsonParser jp = createParser ( "{ \"listingStatus\":\"FIXED\" }" );
		jp.nextToken(); jp.nextToken(); jp.nextToken();
	
		SaleMethod option = saleMethodDeserilaizer.deserialize( jp, null);
		Assert.assertTrue( option != null );
	}
		
		
	@Test
	public void testSplitOption() throws Exception 
	{
		SplitOptionDeserializer splitOptionDeserializer = new SplitOptionDeserializer();
		
		JsonParser jp = createParser ( "{ \"listingStatus\":\"MULTIPLES\" }" );
		jp.nextToken(); jp.nextToken(); jp.nextToken();
	
		SplitOption option = splitOptionDeserializer.deserialize( jp, null);
		Assert.assertTrue( option != null );
	}
	
	@Test
	public void testTicketMediumOption() throws Exception {
		TicketMediumDeserializer deserializer = new TicketMediumDeserializer();

		JsonParser jp = createParser("{ \"ticketMedium\":\"PDF\" }");
		
		jp.nextToken();
		jp.nextToken();
		jp.nextToken();

		TicketMedium value = deserializer.deserialize(jp, null);
		Assert.assertEquals(value, TicketMedium.PDF);
	}
	
	private JsonParser createParser ( String jsonValue ) throws Exception
	{
		JsonFactory jfactory = new JsonFactory();
		JsonParser jParser = jfactory.createJsonParser(  jsonValue  );
		return jParser;
	}
}
