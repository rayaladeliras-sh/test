package com.stubhub.domain.inventory.biz.v2.intf;

import java.util.List;

import com.stubhub.domain.inventory.datamodel.entity.PDFTicket;
import com.stubhub.domain.inventory.datamodel.entity.PDFTicketSeat;



public interface PDFTicketMgr {

	//public List<PDFTicket> findPDFTickets(long ticketId);
	
	public List<PDFTicketSeat> findPDFTicketSeats(long ticketId);
	
}
