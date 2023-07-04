package com.stubhub.domain.inventory.biz.v2.intf;

import com.stubhub.domain.inventory.datamodel.entity.FileInfo;
import com.stubhub.domain.inventory.datamodel.entity.PTVTicket;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeatDetails;

public interface FulfillmentArtifactMgr {
	
	/**
	 * This method is used to bind FulfillmentArtifactIds for PDF (fileInfoIds)
	 * @param ticketSeat
	 */
	public void addFulfillmentArtifactForPDF(TicketSeat ticketSeat);
	
	/**
	 * This method is used to bind FulfillmentArtifactIds for Barcode (ptvTicketIds)
	 * @param ticketSeat
	 */
	public void addFulfillmentArtifactForBarcode(TicketSeat ticketSeat);
	
	/**
	 * This method is used to check if fulfillmentArtifactId passed 
	 * is present in db or not for Barcode
	 * @param fulfillmentArtifactId
	 * @return PTVTicket
	 */
	public PTVTicket findByIdForBarcode(Long fulfillmentArtifactId);
	
	/**
	 * This method is used to check if fulfillmentArtifactId passed 
	 * is present in db or not for PDF
	 * @param fulfillmentArtifactId
	 * @return FileInfo
	 */
	public FileInfo findByIdForPDF(Long fulfillmentArtifactId);
	
	/**
	 * Method to validate if the seller is the owner of the pdf file
	 * 
	 * 
	 * @param fileInfoId
	 * @param sellerId
	 * @return
	 */
	public boolean isSellerOwnerOfPDF(Long fileInfoId, Long sellerId);
	
	/**
	 * Method to update File Info record based on Id
	 * @param fileInfo
	 * @return
	 */
	public FileInfo updateFileInfo(FileInfo fileInfo);

	public TicketSeatDetails getTicketSeatDetails(Long ticketSeatId);
	
	public FileInfo addFileInfo(FileInfo fileInfo);
}
