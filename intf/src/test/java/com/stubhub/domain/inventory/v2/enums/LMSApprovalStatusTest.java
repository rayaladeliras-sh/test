package com.stubhub.domain.inventory.v2.enums;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LMSApprovalStatusTest {

	@Test
	public void getGetByIdExists() {
		LMSApprovalStatus s = LMSApprovalStatus.getById(1);
		Assert.assertEquals(s, LMSApprovalStatus.PENDING_APROVAL);
		Assert.assertEquals(s.getId(),
				LMSApprovalStatus.PENDING_APROVAL.getId());
		Assert.assertNotNull(s.getDescription());
	}

	@Test
	public void getGetByIdDoesNotExists() {
		LMSApprovalStatus s = LMSApprovalStatus.getById(2015);
		Assert.assertNull(s);
	}

	@Test
	public void getGetByIdNull() {
		LMSApprovalStatus s = LMSApprovalStatus.getById(null);
		Assert.assertNull(s);
	}

}
