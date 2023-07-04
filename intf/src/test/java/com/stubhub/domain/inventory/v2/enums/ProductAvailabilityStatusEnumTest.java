package com.stubhub.domain.inventory.v2.enums;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ProductAvailabilityStatusEnumTest {
	@Test
	public void testProductStatusEnum() {
		Assert.assertEquals(ProductAvailabilityStatusEnum.AVAILABLE, ProductAvailabilityStatusEnum.AVAILABLE);
		Assert.assertEquals(ProductAvailabilityStatusEnum.UNAVAILABLE, ProductAvailabilityStatusEnum.UNAVAILABLE);
	}
}
