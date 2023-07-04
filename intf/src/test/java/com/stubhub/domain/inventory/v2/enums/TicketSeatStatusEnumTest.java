package com.stubhub.domain.inventory.v2.enums;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TicketSeatStatusEnumTest {
	@Test
	public void testTicketSeatStatusEnum() {
		Assert.assertEquals(TicketSeatStatusEnum.AVAILABLE, TicketSeatStatusEnum.getTicketSeatStatusEnumByCode(1));
		Assert.assertEquals(TicketSeatStatusEnum.UNCONFIRMED, TicketSeatStatusEnum.getTicketSeatStatusEnumByCode(2));
		Assert.assertEquals(TicketSeatStatusEnum.SOLD, TicketSeatStatusEnum.getTicketSeatStatusEnumByCode(3));
		Assert.assertEquals(TicketSeatStatusEnum.REMOVED, TicketSeatStatusEnum.getTicketSeatStatusEnumByCode(4));
		Assert.assertEquals(TicketSeatStatusEnum.INCOMPLETE, TicketSeatStatusEnum.getTicketSeatStatusEnumByCode(5));
		Assert.assertEquals(TicketSeatStatusEnum.AVAILABLE, TicketSeatStatusEnum.getTicketSeatStatusEnumByName("AVAILABLE"));
		Assert.assertEquals(TicketSeatStatusEnum.UNCONFIRMED, TicketSeatStatusEnum.getTicketSeatStatusEnumByName("UNCONFIRMED"));
		Assert.assertEquals(TicketSeatStatusEnum.SOLD, TicketSeatStatusEnum.getTicketSeatStatusEnumByName("SOLD"));
		Assert.assertEquals(TicketSeatStatusEnum.REMOVED, TicketSeatStatusEnum.getTicketSeatStatusEnumByName("REMOVED"));
		Assert.assertEquals(TicketSeatStatusEnum.INCOMPLETE, TicketSeatStatusEnum.getTicketSeatStatusEnumByName("INCOMPLETE"));
		Assert.assertNull(TicketSeatStatusEnum.getTicketSeatStatusEnumByCode(10));
		Assert.assertNull(TicketSeatStatusEnum.getTicketSeatStatusEnumByName("ABC"));
	}
}