package com.stubhub.domain.inventory.biz.v2.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.biz.v2.impl.TicketSeatMgrImpl;
import com.stubhub.domain.inventory.datamodel.dao.TicketSeatDAO;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;

public class TicketSeatMgrTest {
	
	private TicketSeatMgrImpl ticketSeatMgrImpl;
	
	private TicketSeatDAO ticketSeatDAO;
	
	@BeforeMethod
	public void setUp(){
		ticketSeatMgrImpl = new TicketSeatMgrImpl();
		ticketSeatDAO = mock(TicketSeatDAO.class);
		ReflectionTestUtils.setField(ticketSeatMgrImpl, "ticketSeatDAO", ticketSeatDAO);
	}
	
	@Test
	public void findTicketSeatsByTicketIdTest(){
		when(ticketSeatDAO.findByTicketId(any(Long.class))).thenReturn(new ArrayList<TicketSeat>());
		ticketSeatMgrImpl.findTicketSeatsByTicketId(2345678L);
	}
	
	@Test
	public void addTicketSeatTest(){
		when(ticketSeatDAO.addTicketSeat(any(TicketSeat.class))).thenReturn(new TicketSeat());
		ticketSeatMgrImpl.addTicketSeat(any(TicketSeat.class));
	}
	
	@Test
	public void updateTicketSeatTest(){
		when(ticketSeatDAO.updateTicketSeat(any(TicketSeat.class))).thenReturn(new TicketSeat());
		ticketSeatMgrImpl.updateTicketSeat(any(TicketSeat.class));
	}
	
	@Test
	public void findActiveTicketSeatsByTicketIdTest(){
		when(ticketSeatDAO.findByTicketId(any(Long.class))).thenReturn(new ArrayList<TicketSeat>());
		ticketSeatMgrImpl.findActiveTicketSeatsByTicketId(2345678L);
	}
	
	@Test
    public void findActiveTicketSeatsOnlyByTicketIdTest(){
        when(ticketSeatDAO.findByTicketId(any(Long.class))).thenReturn(new ArrayList<TicketSeat>());
        ticketSeatMgrImpl.findActiveTicketSeatsOnlyByTicketId(2345678L);
    }
	
}
