package com.stubhub.domain.inventory.listings.v2.helper;


import org.testng.Assert;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.listings.v2.enums.DeliveryOptionEnum;

public class DeliveryOptionEnumTest {
	
	@Test
	 public void testGetSet() {		
		Assert.assertEquals(2l, DeliveryOptionEnum.MANUAL_DELIVERY.getDeliveryOption());
		Assert.assertEquals(1l,DeliveryOptionEnum.PRE_DELIVERY.getDeliveryOption());
	}

}
