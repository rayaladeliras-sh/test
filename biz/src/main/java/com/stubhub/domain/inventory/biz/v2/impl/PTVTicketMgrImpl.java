package com.stubhub.domain.inventory.biz.v2.impl;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.stubhub.domain.inventory.biz.v2.intf.PTVTicketMgr;
import com.stubhub.domain.inventory.datamodel.dao.PTVTicketDAO;
import com.stubhub.domain.inventory.datamodel.entity.PTVTicket;

@Component("ptvTicketMgr")
public class PTVTicketMgrImpl implements PTVTicketMgr {

	@Autowired
	private PTVTicketDAO ptvTicketDAO;
	
	@Override
	@Transactional
	public List<PTVTicket> findPTVTickets(long ticketId) {
		return ptvTicketDAO.findByTicketId(ticketId);
	}
	
	@Override
	@Transactional
	public PTVTicket findPTVTicketsBySeatId(long ticketId, long seatId) {
		return ptvTicketDAO.findByTicketSeatId(ticketId, seatId);
	}
	

}

