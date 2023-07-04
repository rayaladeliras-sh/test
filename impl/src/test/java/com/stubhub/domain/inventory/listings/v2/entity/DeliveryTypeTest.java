package com.stubhub.domain.inventory.listings.v2.entity;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.listings.v2.entity.DeliveryType;
import com.stubhub.domain.inventory.listings.v2.enums.DeliveryTypeEnum;

public class DeliveryTypeTest {
	@Test
	public void testGetterSetters(){
			
	DeliveryType deliveryType=new DeliveryType();
		
		
	deliveryType.setActive(true);
	Assert.assertEquals(deliveryType.getActive(),new Boolean("true"));
	deliveryType.setDeliveryTypeEnum(DeliveryTypeEnum.Electronic);
	Assert.assertEquals(deliveryType.getDeliveryTypeEnum(),DeliveryTypeEnum.Electronic);
	deliveryType.setId(1L);
	Assert.assertEquals(deliveryType.getId(),new Long("1"));
	deliveryType.setName("test");
	Assert.assertEquals(deliveryType.getName(),"test");
		
		
		
		
	
		Object other = new DeliveryType();
		Object different = new String();
		
		
		Assert.assertNotNull(deliveryType.hashCode());
		Assert.assertFalse(deliveryType.equals(different));
		Assert.assertFalse(deliveryType.equals(other));
		Assert.assertFalse(deliveryType.equals(null));
		
		
		
		
		
		
		
		
		
		
	}
}
