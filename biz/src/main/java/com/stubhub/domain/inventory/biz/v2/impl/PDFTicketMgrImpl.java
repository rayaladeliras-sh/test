package com.stubhub.domain.inventory.biz.v2.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.stubhub.domain.inventory.biz.v2.intf.PDFTicketMgr;
import com.stubhub.domain.inventory.datamodel.dao.PDFTicketDAO;
import com.stubhub.domain.inventory.datamodel.entity.PDFTicketFile;
import com.stubhub.domain.inventory.datamodel.entity.PDFTicketSeat;

@Component("pdfTicketMgr")
public class PDFTicketMgrImpl implements PDFTicketMgr {

	@Autowired
	private PDFTicketDAO pdfTicketDAO;

	@Override
	@Transactional
	public List<PDFTicketSeat> findPDFTicketSeats(long ticketId) {
		List<PDFTicketFile> ticketFiles = pdfTicketDAO
				.findByTicketFiles(ticketId);
		if (ticketFiles != null && ticketFiles.size() > 0) {
			List<Long> ticketFileIds = new ArrayList<Long>();
			for (PDFTicketFile ticketFile : ticketFiles) {
				ticketFileIds.add(ticketFile.getTicketEtixFileId());
			}

			List<PDFTicketSeat> ticketSeats =pdfTicketDAO.findByTicketSeats(ticketFileIds);

			return ticketSeats;
		}
		return new ArrayList<PDFTicketSeat>();
	}

}
