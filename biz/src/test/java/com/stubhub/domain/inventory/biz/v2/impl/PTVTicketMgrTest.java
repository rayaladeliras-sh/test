package com.stubhub.domain.inventory.biz.v2.impl;
/*

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.biz.v2.impl.PTVTicketMgrImpl;
import com.stubhub.domain.inventory.datamodel.dao.PTVTicketDAO;
import com.stubhub.domain.inventory.datamodel.entity.PTVTicket;

public class PTVTicketMgrTest {
	private PTVTicketMgrImpl ptvTicketMgrImpl;
	
	private PTVTicketDAO ptvTicketDAO;
	
	@BeforeMethod
	public void setUp(){
		ptvTicketMgrImpl = new PTVTicketMgrImpl();
		ptvTicketDAO = mock(PTVTicketDAO.class);
		ReflectionTestUtils.setField(ptvTicketMgrImpl, "ptvTicketDAO", ptvTicketDAO);
	}
	
	@Test
	public void findPTVTicketsTest(){
		List<PTVTicket> ptvTickets = new ArrayList<PTVTicket>();
		PTVTicket ptvTicket = new PTVTicket();
		ptvTicket.setControlCode("controlCode");
		ptvTicket.setGa(true);
		ptvTicket.setPtvTicketId(123L);
		ptvTicket.setReIssuedConrolCode("reIssuedConrolCode");
		ptvTicket.setReIssuedGA(true);
		ptvTicket.setReIssuedRow("reIssuedRow");
		ptvTicket.setReIssuedSeat("reIssuedSeat");
		ptvTicket.setReIssuedsection("reIssuedsection");
		ptvTicket.setRow("row");
		ptvTicket.setSeat("seat");
		ptvTicket.setStatus(1l);
		ptvTickets.add(ptvTicket);
		when(ptvTicketDAO.findByTicketId(anyLong())).thenReturn(ptvTickets);
		List<PTVTicket> ptvTickets2 = ptvTicketMgrImpl.findPTVTickets(123456L);
		Assert.assertNotNull(ptvTickets2);
	}
	
	@Test
	public void findPTVTicketsBySeatIdTest(){		
		PTVTicket ptvTicket = new PTVTicket();
		ptvTicket.setControlCode("controlCode");
		ptvTicket.setGa(true);
		ptvTicket.setPtvTicketId(123L);
		ptvTicket.setReIssuedConrolCode("reIssuedConrolCode");
		ptvTicket.setReIssuedGA(true);
		ptvTicket.setReIssuedRow("reIssuedRow");
		ptvTicket.setReIssuedSeat("reIssuedSeat");
		ptvTicket.setReIssuedsection("reIssuedsection");
		ptvTicket.setRow("row");
		ptvTicket.setSeat("seat");
		ptvTicket.setStatus(1l);		
		when(ptvTicketDAO.findByTicketSeatId(anyLong(), anyLong())).thenReturn(ptvTicket);
		PTVTicket ptvTicket2 = ptvTicketMgrImpl.findPTVTicketsBySeatId(123L,123456L);
		Assert.assertNotNull(ptvTicket2.getControlCode());
		Assert.assertNotNull(ptvTicket2.getReIssuedConrolCode());
		Assert.assertNotNull(ptvTicket2.getReIssuedRow());
		Assert.assertNotNull(ptvTicket2.getReIssuedSeat());
		Assert.assertNotNull(ptvTicket2.getReIssuedsection());
		Assert.assertNotNull(ptvTicket2.getRow());
		Assert.assertNotNull(ptvTicket2.getSeat());
		Assert.assertNotNull(ptvTicket2.getGa());
		Assert.assertNotNull(ptvTicket2.getPtvTicketId());
		Assert.assertNotNull(ptvTicket2.getReIssuedGA());
		Assert.assertNotNull(ptvTicket2.getStatus());
		Assert.assertNotNull(ptvTicket2.toString());
		
	}
}
*/
