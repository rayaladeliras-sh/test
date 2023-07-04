package com.stubhub.domain.inventory.v2.enums;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ListingAvailabilityStatusEnumTest {
	@Test
	public void testListingStatusEnum() {
		Assert.assertEquals(ListingAvailabilityStatusEnum.AVAILABLE, ListingAvailabilityStatusEnum.AVAILABLE);
		Assert.assertEquals(ListingAvailabilityStatusEnum.UNAVAILABLE, ListingAvailabilityStatusEnum.UNAVAILABLE);
		Assert.assertEquals(ListingAvailabilityStatusEnum.UNACCEPTABLE, ListingAvailabilityStatusEnum.UNACCEPTABLE);
	}
}
