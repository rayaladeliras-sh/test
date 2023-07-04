package com.stubhub.domain.inventory.v2.enums;

import junit.framework.Assert;
import org.testng.annotations.Test;

public class OperationTest {

	@Test
	public void operationTest () {
		
		Operation op1 = Operation.ADD;
		Operation op2 = Operation.fromString("add");
		Assert.assertTrue( op1.equalsEnum(op2) );
		Assert.assertTrue( op1.equals(op2) );

		op1 = Operation.UPDATE;
		op2 = Operation.fromString("UPDATE");
		Assert.assertTrue( op1.equalsEnum(op2) );
		Assert.assertTrue( op1.equals(op2) );
		
		op1 = Operation.DELETE;
		op2 = Operation.fromString("delete");
		Assert.assertTrue( op1.equalsEnum(op2) );
		Assert.assertTrue( op1.equals(op2) );
		
		op2 = Operation.fromString("del");
		Assert.assertNull ( op2 );
	}
}