package com.stubhub.domain.inventory.v2.enums;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.v2.enums.TicketMedium;

public class TicketMediumTest {

	@Test
	public void getGetByValueExactMatch() {
		TicketMedium tm = TicketMedium.getByName("PDF");
		Assert.assertEquals(tm, TicketMedium.PDF);
		Assert.assertEquals(tm.getId(), TicketMedium.PDF.getId());
	}

	@Test
	public void getGetByValueLowerCase() {
		TicketMedium tm = TicketMedium.getByName("pDf");
		Assert.assertEquals(tm, TicketMedium.PDF);
		Assert.assertEquals(tm.getId(), TicketMedium.PDF.getId());
	}

	@Test
	public void getGetByValueWrongValue() {
		TicketMedium tm = TicketMedium.getByName("SF");
		Assert.assertNull(tm);
	}

	@Test
	public void getGetByValueNullParam() {
		TicketMedium tm = TicketMedium.getByName(null);
		Assert.assertNull(tm);
	}

}
