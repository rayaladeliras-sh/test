package com.stubhub.domain.inventory.v2.enums;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ListingStatusEnumTest {
	
	@Test
	public void testListingStatusEnum() {
		Assert.assertEquals(ListingStatusEnum.ACTIVE, ListingStatusEnum.getListingStatusEnumByCode("1"));
		Assert.assertEquals(ListingStatusEnum.ACTIVE, ListingStatusEnum.getListingStatusEnumByName("ACTIVE"));
		Assert.assertNull(ListingStatusEnum.getListingStatusEnumByName("ABC"));
		Assert.assertNull(ListingStatusEnum.getListingStatusEnumByCode("10"));
	}
}
