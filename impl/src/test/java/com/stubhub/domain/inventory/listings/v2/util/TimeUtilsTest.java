package com.stubhub.domain.inventory.listings.v2.util;

import java.util.Date;

import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TimeUtilsTest {

  @Test
	public void testIsValidSaleEndDateValid() {
		DateTime eventDate = DateTime.now().plusDays(10);
		
		DateTime saleDate = DateTime.now().plusDays(5);
		boolean result = TimeUtils.isValidSaleEndDate(eventDate.toDate(), saleDate.toDate());
		
		Assert.assertTrue(result);
	}
		
	@Test
	public void testIsValidSaleEndDateNullsSent() {
		boolean result = TimeUtils.isValidSaleEndDate(null, new Date());
		Assert.assertFalse(result);

		result = TimeUtils.isValidSaleEndDate(new Date(), null);
		Assert.assertFalse(result);
		
		result = TimeUtils.isValidSaleEndDate(null, null);
		Assert.assertFalse(result);
	}
	
	@Test
	public void testIsValidSaleEndDateAfterTheDateRangeValid() {
		DateTime eventDate = DateTime.now().plusDays(10);
		
		DateTime saleDate = DateTime.now().plusDays(11);
		boolean result = TimeUtils.isValidSaleEndDate(eventDate.toDate(), saleDate.toDate());
		
		Assert.assertTrue(result);
	}
	
	@Test
	public void testIsValidSaleEndDateAfterTheDateRangeInvalid1() {
		DateTime eventDate = DateTime.now().plusDays(10);
		
		DateTime saleDate = DateTime.now().plusDays(13);
		boolean result = TimeUtils.isValidSaleEndDate(eventDate.toDate(), saleDate.toDate());
		
		Assert.assertFalse(result);
	}
	
	@Test
	public void testIsValidSaleEndDateBeforeTheDateRangeInvalid() {
		DateTime eventDate = DateTime.now().plusDays(10);
		
		DateTime saleDate = DateTime.now().minusDays(1);
		boolean result = TimeUtils.isValidSaleEndDate(eventDate.toDate(), saleDate.toDate());
		
		Assert.assertFalse(result);
	}
	
}
