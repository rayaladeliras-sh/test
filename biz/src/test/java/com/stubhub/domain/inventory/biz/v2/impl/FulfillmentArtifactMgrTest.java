package com.stubhub.domain.inventory.biz.v2.impl;
/*

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;

import junit.framework.Assert;

import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.datamodel.dao.FileInfoDAO;
import com.stubhub.domain.inventory.datamodel.dao.PTVTicketDAO;
import com.stubhub.domain.inventory.datamodel.dao.PTVTicketSeatXrefDAO;
import com.stubhub.domain.inventory.datamodel.dao.SellerEtixFileDAO;
import com.stubhub.domain.inventory.datamodel.dao.TicketEtixFileDAO;
import com.stubhub.domain.inventory.datamodel.dao.TicketSeatEtixFileDAO;
import com.stubhub.domain.inventory.datamodel.entity.FileInfo;
import com.stubhub.domain.inventory.datamodel.entity.PTVTicket;
import com.stubhub.domain.inventory.datamodel.entity.PTVTicketSeatXref;
import com.stubhub.domain.inventory.datamodel.entity.SellerEtixFile;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeatDetails;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeatEtixFile;
import com.stubhub.domain.inventory.datamodel.entity.TicketsEtixFile;
import com.stubhub.newplatform.common.util.DateUtil;

public class FulfillmentArtifactMgrTest { 
	
	//private FulfillmentArtifactMgrImpl fulfillmentArtifactMgrImpl;
	
	private TicketEtixFileDAO ticketEtixFileDAO;
	
	private TicketSeatEtixFileDAO ticketSeatEtixFileDAO;
	
	private PTVTicketSeatXrefDAO ptvTicketSeatXrefDAO;
	
	private PTVTicketDAO ptvTicketDAO;
	
	private FileInfoDAO fileInfoDAO;
	
	private SellerEtixFileDAO sellerEtixFileDAO;
	
	@BeforeMethod
	public void setUp(){
		//fulfillmentArtifactMgrImpl = new FulfillmentArtifactMgrImpl();
		ticketEtixFileDAO = mock(TicketEtixFileDAO.class);
		ticketSeatEtixFileDAO = mock(TicketSeatEtixFileDAO.class);
		ptvTicketSeatXrefDAO = mock(PTVTicketSeatXrefDAO.class);
		ptvTicketDAO = mock(PTVTicketDAO.class);
		fileInfoDAO = mock(FileInfoDAO.class);
		sellerEtixFileDAO = mock(SellerEtixFileDAO.class);
		*/
/*ReflectionTestUtils.setField(fulfillmentArtifactMgrImpl, "ticketEtixFileDAO", ticketEtixFileDAO);
		ReflectionTestUtils.setField(fulfillmentArtifactMgrImpl, "ticketSeatEtixFileDAO", ticketSeatEtixFileDAO);
		ReflectionTestUtils.setField(fulfillmentArtifactMgrImpl, "ptvTicketSeatXrefDAO", ptvTicketSeatXrefDAO);
		ReflectionTestUtils.setField(fulfillmentArtifactMgrImpl, "ptvTicketDAO", ptvTicketDAO);
		ReflectionTestUtils.setField(fulfillmentArtifactMgrImpl, "fileInfoDAO", fileInfoDAO);
		ReflectionTestUtils.setField(fulfillmentArtifactMgrImpl, "sellerEtixFileDAO", sellerEtixFileDAO);*//*

	}
	
	@Test
	public void testAddFulfillmentArtifactForPDF(){
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setTicketSeatId(12367l);
		ticketSeat.setSection("A");
		ticketSeat.setRow("1");
		ticketSeat.setSeatNumber("1");
		ticketSeat.setFulfillmentArtifactId(123456L);
		TicketsEtixFile ticketsEtixFile = new TicketsEtixFile();
		TicketSeatEtixFile ticketSeatEtixFile = new TicketSeatEtixFile();
		ticketsEtixFile.setFileInfoId(12345L);
		ticketSeatEtixFile.setTicketEtixFileId(1234L);
		ticketsEtixFile.setActive(1);
		ticketsEtixFile.setTicketId(223456783L);
		ticketsEtixFile.setSeats("1,2");
		Calendar nowUtc = DateUtil.getNowCalUTC();
		ticketsEtixFile.setCreatedDate(nowUtc);
		ticketsEtixFile.setLastUpdatedDate(nowUtc);
		ticketsEtixFile.setCreatedBy("Test");
		ticketsEtixFile.setLastUpdatedBy("Test");
		ticketsEtixFile.setTicketEtixFileId(1234L);
		when(ticketEtixFileDAO.addTicketEtixFile(Mockito.any(TicketsEtixFile.class))).thenReturn(ticketsEtixFile);
		when(ticketSeatEtixFileDAO.addTicketSeatEtixFile(Mockito.any(TicketSeatEtixFile.class))).thenReturn(ticketSeatEtixFile);
		fulfillmentArtifactMgrImpl.addFulfillmentArtifactForPDF(ticketSeat);
		Assert.assertEquals(new Long(1234), ticketsEtixFile.getTicketEtixFileId());
	}
	
	@Test
	public void testAddFulfillmentArtifactForBarcode(){
		TicketSeat ticketSeat = new TicketSeat();
		ticketSeat.setTicketSeatId(12367l);
		ticketSeat.setSection("A");
		ticketSeat.setRow("1");
		ticketSeat.setSeatNumber("1");
		ticketSeat.setFulfillmentArtifactId(123456L);
		PTVTicketSeatXref ptvTicketSeatXref = new PTVTicketSeatXref();
		when(ptvTicketSeatXrefDAO.addPTVTicketSeatXref(ptvTicketSeatXref)).thenReturn(ptvTicketSeatXref);
		fulfillmentArtifactMgrImpl.addFulfillmentArtifactForBarcode(ticketSeat);
		Assert.assertNotNull(ptvTicketSeatXref);
		
	}
	
	@Test
	public void testFindByIdForBarcode(){
		when(ptvTicketDAO.findById(any(Long.class))).thenReturn(new PTVTicket());
		fulfillmentArtifactMgrImpl.findByIdForBarcode(6774627343L);
	}

	@Test
	public void testFindByIdForPDF(){
		when(fileInfoDAO.findById(any(Long.class))).thenReturn(new FileInfo());
		fulfillmentArtifactMgrImpl.findByIdForPDF(6774627343L);
	}
	
	@Test
	public void testIsSellerOwnerOfPDF(){
		SellerEtixFile sellerEtixFile = new SellerEtixFile();
		sellerEtixFile.setSellerId(1234L);
		when(sellerEtixFileDAO.getSellerEtixFileByFileInfoId(any(Long.class))).thenReturn(sellerEtixFile);
		boolean isOwner = fulfillmentArtifactMgrImpl.isSellerOwnerOfPDF(12345L, 1234L);
		Assert.assertTrue(isOwner);
	}
	
	@Test
	public void testUpdateFileInfo(){
		when(fileInfoDAO.updateFileInfo(Mockito.any(FileInfo.class))).thenReturn(new FileInfo());
	}
	
	@Test
	public void testGetTicketSeatDetails(){
		when(ticketSeatEtixFileDAO.getTicketSeatDetails(Mockito.anyLong())).thenReturn(new TicketSeatDetails());
		fulfillmentArtifactMgrImpl.getTicketSeatDetails(123L);
	}
}
*/
