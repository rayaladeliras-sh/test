package com.stubhub.domain.inventory.biz.v2.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.datamodel.dao.PDFTicketDAO;
import com.stubhub.domain.inventory.datamodel.entity.PDFTicketFile;

public class PDFTicketMgrTest {

	private PDFTicketMgrImpl pdfTicketMgrImpl;

	private PDFTicketDAO pdfTicketDAO;

	@BeforeMethod
	public void setUp() {
		pdfTicketMgrImpl = new PDFTicketMgrImpl();
		pdfTicketDAO = mock(PDFTicketDAO.class);
		ReflectionTestUtils.setField(pdfTicketMgrImpl, "pdfTicketDAO", pdfTicketDAO);
	}

	@Test
	public void findPDFTicketSeats() {
		List<PDFTicketFile> ticketFiles = new ArrayList<PDFTicketFile>();
		PDFTicketFile pdfTicketFile = new PDFTicketFile();
		pdfTicketFile.setFileDisplayName("Ticket.pdf");
		pdfTicketFile.setFileName("Ticket.pdf");
		pdfTicketFile.setTicketEtixFileId(1234L);
		ticketFiles.add(pdfTicketFile);
		when(pdfTicketDAO.findByTicketFiles(any(Long.class))).thenReturn(ticketFiles);
		pdfTicketMgrImpl.findPDFTicketSeats(1234567L);
		when(pdfTicketDAO.findByTicketFiles(any(Long.class))).thenReturn(new ArrayList<PDFTicketFile>());
		pdfTicketMgrImpl.findPDFTicketSeats(1234567L);
		when(pdfTicketDAO.findByTicketFiles(any(Long.class))).thenReturn(new ArrayList<PDFTicketFile>());
		pdfTicketMgrImpl.findPDFTicketSeats(1234567L);
	}

}
