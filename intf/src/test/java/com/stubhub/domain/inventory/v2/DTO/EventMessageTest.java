package com.stubhub.domain.inventory.v2.DTO;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.v2.DTO.EventMessage;


public class EventMessageTest {
	
	@Test
	 public void testGetSet() {
		
		EventMessage eventMessage=new EventMessage();
		int type=1;
		String header= "Header";
		String message="message";
		
		eventMessage.setHeader(header);
		Assert.assertEquals(eventMessage.getHeader(),header);
		eventMessage.setMessage(message);
		Assert.assertEquals(eventMessage.getMessage(),message);
		eventMessage.setType(type);
		Assert.assertNotNull(eventMessage.getType());
		
		
		Assert.assertNotNull(eventMessage.hashCode());
		Assert.assertNotNull(new EventMessage().hashCode());
		Assert.assertTrue(eventMessage.equals(eventMessage));
		Assert.assertFalse(eventMessage.equals(new EventMessage()));
		Assert.assertFalse(new EventMessage().equals(eventMessage));
		Assert.assertFalse(new EventMessage().equals(null));
		Assert.assertEquals(eventMessage, eventMessage);
		Assert.assertNotEquals(eventMessage, new Integer(0));	
		
	
		Object other = new EventMessage();
		Object different = new String();
		
		
		Assert.assertNotNull(eventMessage.hashCode());
		Assert.assertFalse(eventMessage.equals(different));
		Assert.assertFalse(eventMessage.equals(other));
		Assert.assertFalse(eventMessage.equals(null));

}
}