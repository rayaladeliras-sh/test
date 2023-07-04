package com.stubhub.domain.inventory.listings.v2.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.stubhub.domain.infrastructure.common.core.concurrent.SHThreadLocals;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContextSerializer;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.ResponseReader;
import org.apache.cxf.jaxrs.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.fulfillment.pdf.v1.intf.request.AddPDFOnListingRequest;
import com.stubhub.domain.fulfillment.pdf.v1.intf.response.AddPDFOnListingResponse;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.entity.SeatProduct;
import com.stubhub.domain.inventory.listings.v2.util.SeatProductsContext;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;
/**
 * 
 * @author vrachapudi
 *
 */

@Component("listingFulfilHelper")
public class ListingFulfilmentHelper {     
	
	@Autowired
	private SvcLocator svcLocator;
	
	private final static Logger log = LoggerFactory
			.getLogger(ListingFulfilmentHelper.class);

	/**
	 * 
	 * @param newListing
	 * @param dbListing
	 * @param ldata
	 * @return
	 * 
	 * This method is used for update listing , revisit this method for create Listing usecase 
	 */
	
	
	public boolean validateFileInfoIds(SeatProductsContext seatProdCtx ,Listing listing){
		 boolean isValidFileInfo =false;
		List<SeatProduct> seatProductList= seatProdCtx.getBarcodeSeatProductList();
		if(seatProductList==null){
			seatProductList =seatProdCtx.getPassedSeatProductList(false);
		}
		List<TicketSeat> currentSeats =seatProdCtx.getTicketSeatsFromCache();
	
		AddPDFOnListingRequest addPDFOnListingRequest = new AddPDFOnListingRequest();
		addPDFOnListingRequest.setEventId(listing.getEventId());
		addPDFOnListingRequest.setListingId(listing.getId());
		if(listing.getQuantityRemain()!=null){
			addPDFOnListingRequest.setQuantityRemain(listing.getQuantityRemain().longValue());
		}
		
		addPDFOnListingRequest.setSellerId(listing.getSellerId()); 
		addPDFOnListingRequest.setValidateSeatNumbers(false);

		//List<TicketSeat> ticketSeatList = listing.getTicketSeats();//getBarcodeSeatProductList from Seat
		//List<TicketSeat> existingSeatList  = dbListing.getTicketSeats();//getTicketsSeatsFromCache 
		if(seatProductList==null || seatProductList.size()==0){
			log.error("Invalid Input ticket seats");
			return false;
		}
		
		
		//TODO Revisit create scenarios 
		if(currentSeats ==null ||currentSeats.size()==0){
			log.error("Invalid Existing ticket seats");
			return false;
		}
		
		
		
		if (CommonConstants.GENERAL_ADMISSION.equalsIgnoreCase(listing .getSection())) {
			addPDFOnListingRequest.setIsGA(true);
		} else {
			addPDFOnListingRequest.setIsGA(false);
		}

	
		List<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat> fulFilTicketSeatList =new ArrayList<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat>();
		//SELLAPI-956 10/13/15 START
		//if all seats don't have fulfillment artifact id
		//then throw error
		int artifactsProcessed = 0;
		StringBuffer sbSeats = new StringBuffer();
		for(SeatProduct seat: seatProductList){
			com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat fulTicketSeat = new com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat();
			if (seat.getFulfillmentArtifact() != null && !seat.getFulfillmentArtifact().isEmpty()) {
                fulTicketSeat.setFileInfoIds(seat.getFulfillmentArtifact());
                artifactsProcessed++;
            }
			else {
				sbSeats.append(seat.getSeat());
			}
			fulTicketSeat.setSeatType(seat.getProductType().name()); //SELLAPI-956 10/14/15
			if (seat.getProductType().name().equals(ProductType.TICKET.name())){
				fulTicketSeat.setRow(seat.getRow());
				fulTicketSeat.setSeat(seat.getSeat());
				fulTicketSeat.setSeatId(seat.getSeatId());
			} else {
				fulTicketSeat.setRow("LOT");
				fulTicketSeat.setSeat("Parking Pass");
				fulTicketSeat.setSeatId(seat.getSeatId());
			}
			
			fulFilTicketSeatList.add(fulTicketSeat);
		}
		if (artifactsProcessed > 0 && artifactsProcessed < seatProductList.size()){
			log.error("Fulfillment Artifact Error, either missing fulfillment artifact or invalid number for seats: " + sbSeats.toString());
			return false;
		}
		//SELLAPI-956 10/13/15 END
		addPDFOnListingRequest.setTicketSeats(fulFilTicketSeatList);
		
		
		
		List<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat> dbTicketSeatList =new ArrayList<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat>();
		//check for null dbticketSeatList and load the dbticketSeatList from the db 
		//TODO
		 for(TicketSeat tseat :currentSeats){
			 if( tseat.getSeatStatusId()!=null && tseat.getSeatStatusId()!=3l){
				com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat fulTicketSeat = new com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat();
				if(tseat.getGeneralAdmissionInd()!=null && tseat.getGeneralAdmissionInd()){
					addPDFOnListingRequest.setIsGA(true);
				}
				fulTicketSeat.setRow(tseat.getRow());
				fulTicketSeat.setSeat(tseat.getSeatNumber());
				fulTicketSeat.setSeatId(tseat.getTicketSeatId());
				fulTicketSeat.setSection(tseat.getSection());
				fulTicketSeat.setFileInfoIds(tseat.getFulfillmentArtifactIds());
				
					 if(tseat.getTixListTypeId()!=null && tseat.getTixListTypeId().longValue()==2){
						 fulTicketSeat.setSeatType(ProductType.PARKING_PASS.toString());
					 }else if(tseat.getTixListTypeId()!=null && tseat.getTixListTypeId().longValue()==1){
						fulTicketSeat.setSeatType(ProductType.TICKET.name()); //SELLAPI-956 10/14/15
					 }
					 dbTicketSeatList.add(fulTicketSeat);
	          }
		 }
		 addPDFOnListingRequest.setExistingSeats(dbTicketSeatList);
		 
		//before calling make sure that ticketSeatReq and existingTicketSeats length should be same or else 
			if(dbTicketSeatList.size()!=seatProductList.size()){
				log.error("Mismatch in input ticket seats and existing ticketseats in the database");
				return false;
			}
	    isValidFileInfo= validatePDFFileInfo(addPDFOnListingRequest);
		return isValidFileInfo;
		}

	private SHServiceContextSerializer serializer = new SHServiceContextSerializer();

	protected boolean validatePDFFileInfo( AddPDFOnListingRequest addPDFOnListingRequest) {

		AddPDFOnListingResponse pdfResponse = null;
		boolean isValidFileInfo =false;
		String requestUrl = null; 
		try {
			requestUrl = getProperty("fulfillment.pdf.v1.api.url", "http://api-int.stubprod.com/fulfillment/pdf/v1/listing/fileInfo");
			
			//String requestJson = JsonUtil.toJsonWrapRoot(requestList);
			ResponseReader reader = new ResponseReader();
			reader.setEntityClass(AddPDFOnListingResponse.class);
			List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
			responseReader.add(reader);

			log.info("Making validatePDFFileInfo service call = " + requestUrl);

			//SHAPIThreadLocal.set(apiContext);
			WebClient webClient = svcLocator.locate(requestUrl,responseReader);
            webClient.type(MediaType.APPLICATION_JSON);
			webClient.accept(MediaType.APPLICATION_JSON);
			//webClient.header("assertion", SHAPIThreadLocal.getAPIContext().getSignedJWTAssertion());

			SHServiceContext shServiceContext = (SHServiceContext) SHThreadLocals.get(SHServiceContext.SERVICE_CONTEXT_HEADER);
			if (shServiceContext != null && shServiceContext.getAttributeMap() != null) {
				String serializedServiceContext = serializer.serialize(shServiceContext);
				webClient.header(SHServiceContext.SERVICE_CONTEXT_HEADER, serializedServiceContext);
			}

			ClientConfiguration config = WebClient.getConfig(webClient);
			if (config != null) {
				config.getInInterceptors().add(new LoggingInInterceptor());
				config.getOutInterceptors().add(new LoggingOutInterceptor());
			}

			SHMonitor mon = SHMonitorFactory.getMonitor();
			Response response = null;
			try {
				mon.start();
				response = webClient.post(addPDFOnListingRequest);
			} finally {
				mon.stop();
				log.info(SHMonitoringContext.get() + " _operation=validatePDFFileInfo" + " _message= service call" + "  _respTime=" + mon.getTime());
			}

			if (Response.Status.OK.getStatusCode() == response.getStatus()) {
				pdfResponse = (AddPDFOnListingResponse) response.getEntity();
				if(pdfResponse.getFulfillmentStatus()!=null && pdfResponse.getFulfillmentStatus().equalsIgnoreCase("fulfilled")){
					isValidFileInfo=true;
				}
			} else {
				isValidFileInfo=false;
				throw new IOException ( "Error encountred calling API. Response code: " + response.getStatus() );
			}
		}
		catch (Exception e) {
			log.error ( "Error making remote API call to API URL: " + requestUrl, e);
		}
		return isValidFileInfo;
	}
	protected String getProperty(String propertyName, String defaultValue) {
		return MasterStubHubProperties.getProperty(propertyName, defaultValue);
	}
		
}




