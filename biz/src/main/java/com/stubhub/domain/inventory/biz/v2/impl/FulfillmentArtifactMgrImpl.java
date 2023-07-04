package com.stubhub.domain.inventory.biz.v2.impl;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.stubhub.domain.inventory.biz.v2.intf.FulfillmentArtifactMgr;
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

@Component("fulfillmentArtifactMgr")
public class FulfillmentArtifactMgrImpl implements FulfillmentArtifactMgr {

    private final static Log log = LogFactory.getLog(FulfillmentArtifactMgrImpl.class);
    private final static String MODULENAME_CREATEINCOMPLETELISTING = "CreateIncompleteListing";
    private final static String MODULENAME_RELIST = "Relist";

    @Autowired
    private TicketEtixFileDAO ticketEtixFileDAO;

    @Autowired
    private TicketSeatEtixFileDAO ticketSeatEtixFileDAO;

    @Autowired
    private PTVTicketSeatXrefDAO ptvTicketSeatXrefDAO;

    @Autowired
    private PTVTicketDAO ptvTicketDAO;

    @Autowired
    private FileInfoDAO fileInfoDAO;

    @Autowired
    private SellerEtixFileDAO sellerEtixFileDAO;

    @Override
    @Transactional
    public void addFulfillmentArtifactForPDF(TicketSeat ticketSeat) {
        log.debug("binding fileInfoId for seat="+ticketSeat.getSeatNumber() + " fileInfoId="+ticketSeat.getFulfillmentArtifactId());
        TicketsEtixFile ticketsEtixFile = new TicketsEtixFile();
        TicketSeatEtixFile ticketSeatEtixFile = new TicketSeatEtixFile();
        ticketsEtixFile.setFileInfoId(ticketSeat.getFulfillmentArtifactId());
        ticketsEtixFile.setActive(1);
        ticketsEtixFile.setUploadSuccessInd(1);
        ticketsEtixFile.setTicketId(ticketSeat.getTicketId());
        ticketsEtixFile.setSeats(ticketSeat.getSeatNumber());
        Calendar nowUtc = DateUtil.getNowCalUTC();
        ticketsEtixFile.setCreatedDate(nowUtc);
        ticketsEtixFile.setLastUpdatedDate(nowUtc);
        ticketsEtixFile.setCreatedBy(MODULENAME_RELIST);
        ticketsEtixFile.setLastUpdatedBy(MODULENAME_RELIST);
        ticketsEtixFile = ticketEtixFileDAO.addTicketEtixFile(ticketsEtixFile);

        ticketSeatEtixFile = new TicketSeatEtixFile();
        ticketSeatEtixFile.setTicketEtixFileId(ticketsEtixFile.getTicketEtixFileId());
        ticketSeatEtixFile.setTicketSeatId(ticketSeat.getTicketSeatId());
        nowUtc = DateUtil.getNowCalUTC();
        ticketSeatEtixFile.setCreatedDate(nowUtc);
        ticketSeatEtixFile.setLastUpdatedDate(nowUtc);
        ticketSeatEtixFile.setCreatedBy(MODULENAME_RELIST);
        ticketSeatEtixFile.setLastUpdatedBy(MODULENAME_RELIST);
        ticketSeatEtixFileDAO.addTicketSeatEtixFile(ticketSeatEtixFile);
    }

    @Override
    @Transactional
    public void addFulfillmentArtifactForBarcode(TicketSeat ticketSeat) {
        log.debug("binding ptvTicketId for seat="+ticketSeat.getSeatNumber() + " ptvTicketId="+ticketSeat.getFulfillmentArtifactId());
        PTVTicketSeatXref ptvTicketSeatXref = new PTVTicketSeatXref();
        ptvTicketSeatXref.setPtvTicketId(ticketSeat.getFulfillmentArtifactId());
        ptvTicketSeatXref.setTicketSeatId(ticketSeat.getTicketSeatId());
        Calendar nowUtc = DateUtil.getNowCalUTC();
        ptvTicketSeatXref.setCreatedDate(nowUtc);
        ptvTicketSeatXref.setLastUpdatedDate(nowUtc);
        ptvTicketSeatXref.setCreatedBy(MODULENAME_CREATEINCOMPLETELISTING);
        ptvTicketSeatXref.setLastUpdatedBy(MODULENAME_CREATEINCOMPLETELISTING);
        ptvTicketSeatXrefDAO.addPTVTicketSeatXref(ptvTicketSeatXref);
    }

    @Override
    public PTVTicket findByIdForBarcode(Long fulfillmentArtifactId) {
        if(fulfillmentArtifactId != null){
            return ptvTicketDAO.findById(fulfillmentArtifactId);
        }
        return null;
    }

    @Override
    public FileInfo findByIdForPDF(Long fulfillmentArtifactId) {
        if(fulfillmentArtifactId != null){
            return fileInfoDAO.findById(fulfillmentArtifactId);
        }
        return null;
    }

    @Override
    public boolean isSellerOwnerOfPDF(Long fileInfoId, Long sellerId) {
        SellerEtixFile sellerEtixFile = sellerEtixFileDAO.getSellerEtixFileByFileInfoId(fileInfoId);
        if(sellerEtixFile != null && sellerId.equals(sellerEtixFile.getSellerId())) {
            return true;
        }

        return false;
    }

    public FileInfo updateFileInfo(FileInfo fileInfo){
        return fileInfoDAO.updateFileInfo(fileInfo);
    }

    /* (non-Javadoc)
     * @see com.stubhub.domain.inventory.biz.intf.FulfillmentArtifactMgr#getTicketSeatDetails(java.lang.Long)
     */
    @Override
    @Transactional
    public TicketSeatDetails getTicketSeatDetails(Long ticketSeatId) {
        return ticketSeatEtixFileDAO.getTicketSeatDetails(ticketSeatId);
    }

    /* (non-Javadoc)
     * @see com.stubhub.domain.inventory.biz.intf.FulfillmentArtifactMgr#addFileInfo(com.stubhub.domain.inventory.datamodel.entity.FileInfo)
     */
    @Override
    @Transactional
    public FileInfo addFileInfo(FileInfo fileInfo) {
        return fileInfoDAO.saveFileInfo(fileInfo);
    }

}
