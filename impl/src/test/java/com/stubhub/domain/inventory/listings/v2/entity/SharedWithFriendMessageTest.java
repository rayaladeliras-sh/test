package com.stubhub.domain.inventory.listings.v2.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SharedWithFriendMessageTest {
	@Test
	public void testGetterSetters(){
			
		SharedWithFriendMessage sharedWithFriendMessage=new SharedWithFriendMessage();
		Long orderId = 123l;
		Map<String,String> map = new HashedMap();
		List<Map<String,String>> orderItemToSeatMap = new ArrayList<>();
		sharedWithFriendMessage.setListingId("listingId");
		Assert.assertEquals(sharedWithFriendMessage.getListingId(),"listingId");
		sharedWithFriendMessage.setOrderId(123l);
		Assert.assertEquals(sharedWithFriendMessage.getOrderId(), orderId);
		sharedWithFriendMessage.setOrderItemToSeatMap(orderItemToSeatMap);
		Assert.assertNotNull(sharedWithFriendMessage.getOrderItemToSeatMap());
		sharedWithFriendMessage.setPaymentType("paypal");
		Assert.assertEquals(sharedWithFriendMessage.getPaymentType(), "paypal");
		sharedWithFriendMessage.setToCustomerGUID("toCustomerGUID");
		Assert.assertEquals(sharedWithFriendMessage.getToCustomerGUID(), "toCustomerGUID");
		sharedWithFriendMessage.setToEmailId("toEmailId");
		Assert.assertEquals(sharedWithFriendMessage.getToEmailId(), "toEmailId");
		Object other = new SharedWithFriendMessage();
		Object different = new String();
		Assert.assertNotNull(sharedWithFriendMessage.hashCode());
		Assert.assertNotNull(sharedWithFriendMessage.toString());
		Assert.assertFalse(sharedWithFriendMessage.equals(different));
		Assert.assertFalse(sharedWithFriendMessage.equals(other));
		Assert.assertFalse(sharedWithFriendMessage.equals(null));
	}	
}
