package com.stubhub.domain.inventory.v2.enums;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ProductMediumEnumTest {
	@Test
	public void testProductMediumEnum() {
		Assert.assertEquals(ProductMediumEnum.BARCODE, ProductMediumEnum.getProductMediumEnumByCode(3));
		Assert.assertEquals(ProductMediumEnum.BARCODE, ProductMediumEnum.getProductMediumEnumByName("BARCODE"));
		Assert.assertNull(ProductMediumEnum.getProductMediumEnumByName("ABC"));
	}
}
