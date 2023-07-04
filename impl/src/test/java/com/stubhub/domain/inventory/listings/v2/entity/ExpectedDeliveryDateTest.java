package com.stubhub.domain.inventory.listings.v2.entity;

import java.util.Calendar;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.listings.v2.entity.DeliveryType;
import com.stubhub.domain.inventory.listings.v2.entity.ExpectedDeliveryDate;

public class ExpectedDeliveryDateTest {
	@Test
	public void testGetterSetters(){
			
		Calendar cal=Calendar.getInstance();
		ExpectedDeliveryDate expectedDeliveryDate=new ExpectedDeliveryDate();
		expectedDeliveryDate.setExpectedDate(cal);
		Assert.assertEquals(expectedDeliveryDate.getExpectedDate(),cal);
		expectedDeliveryDate.setExpectedDeliveryDesc("Test");
		Assert.assertEquals(expectedDeliveryDate.getExpectedDeliveryDesc(),"Test");

		
	
		
		
		
		
	
		Object other = new DeliveryType();
		Object different = new String();
		
		
		Assert.assertNotNull(expectedDeliveryDate.hashCode());
		Assert.assertFalse(expectedDeliveryDate.equals(different));
		Assert.assertFalse(expectedDeliveryDate.equals(other));
		Assert.assertFalse(expectedDeliveryDate.equals(null));
		
		
		
		
		
		
		
		
		
		
	}
}
