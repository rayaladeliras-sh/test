package com.stubhub.domain.inventory.v2.listings.intf;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.v2.listings.intf.DeliveryMethod;
import com.stubhub.newplatform.common.entity.Money;
public class DeliveryMethodTest {
	
	@Test
	 public void testGetSet() {
		
		DeliveryMethod deliveryMethod=new DeliveryMethod();
		
		Calendar cal=Calendar.getInstance();
		Long id=2L;
		String name="NAME";
		
		deliveryMethod.setEstimatedDeliveryTime("2014-11-26T13:54:54-0800");
	//	Assert.assertEquals(deliveryMethod.getEstimatedDeliveryTime(),cal.to);
		deliveryMethod.setId(id);
		Assert.assertEquals(deliveryMethod.getId(),id);
		deliveryMethod.setName(name);
		Assert.assertEquals(deliveryMethod.getName(),name);
		
		Assert.assertNotNull(new DeliveryMethod().hashCode());
		Assert.assertTrue(deliveryMethod.equals(deliveryMethod));
		Assert.assertFalse(deliveryMethod.equals(new DeliveryMethod()));
		Assert.assertFalse(new DeliveryMethod().equals(deliveryMethod));
		Assert.assertFalse(new DeliveryMethod().equals(null));
		Assert.assertEquals(deliveryMethod, deliveryMethod);
		Assert.assertNotSame(deliveryMethod,new Integer(0));	
		
	
		Object other = new DeliveryMethod();
		Object different = new String();
		
		
		Assert.assertNotNull(deliveryMethod.hashCode());
		Assert.assertFalse(deliveryMethod.equals(different));
		Assert.assertFalse(deliveryMethod.equals(other));
		Assert.assertFalse(deliveryMethod.equals(null));
		

}
}