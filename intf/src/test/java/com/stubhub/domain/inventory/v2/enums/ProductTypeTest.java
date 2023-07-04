package com.stubhub.domain.inventory.v2.enums;

import junit.framework.Assert;
import org.testng.annotations.Test;

public class ProductTypeTest {

	@Test
	public void productTypeTest () {
		
		ProductType pt1 = ProductType.PARKING_PASS;
		ProductType pt2 = ProductType.fromString("parking_pass");
		Assert.assertTrue( pt1.equalsEnum(pt2) );
		Assert.assertTrue( pt1.equals(pt2) );
		
		pt1 = ProductType.TICKET;
		pt2 = ProductType.fromString("TICKET");
		Assert.assertTrue( pt1.equalsEnum(pt2) );
		Assert.assertTrue( pt1.equals(pt2) );
		
		pt2 = ProductType.fromString("parking_pass_xx");
		Assert.assertNull ( pt2 );
		
		pt2 = ProductType.fromString("");	// default if nothing is passed
		Assert.assertTrue( pt2.equalsEnum(ProductType.TICKET) );
		Assert.assertTrue( pt2.equals(ProductType.TICKET) );
	}
}