package com.stubhub.domain.inventory.listings.v2.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.JsonUtil;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.entity.ErrorDetail;
import com.stubhub.domain.inventory.listings.v2.entity.SeatProduct;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.TicketSeatHelper;
import com.stubhub.domain.partnerintegration.common.SthTicket;
import com.stubhub.domain.partnerintegration.services.sth.v1.intf.VerifySthInventoryRequest;
import com.stubhub.domain.partnerintegration.services.sth.v1.intf.VerifySthInventoryResponse;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.GetSRSForBarcodesRequest;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.GetSRSForBarcodesResponse;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.Ticket;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.Ticket.TicketClass;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

@Component("primaryIntegrationUtil")
public class PrimaryIntegrationUtil {

    private static final Logger log = LoggerFactory.getLogger(PrimaryIntegrationUtil.class);

    public static final long BUYER_RESTRICTED_SEAT_TRAIT_ID = 15880;

    @Autowired
    private SvcLocator svcLocator;

    @Autowired
    private TicketSeatHelper ticketSeatHelper;

    private static Map<String, ErrorCode> errorCodeMap = new HashMap<String, ErrorCode>();

    private void populateErrorCodes() {
        if (errorCodeMap.size() == 0) {
            errorCodeMap.put("partnerintegration.tickets.invalidbarcode", ErrorCode.INVALID_BARCODE);
            errorCodeMap.put("partnerintegration.tickets.invalidbarcodeformat", ErrorCode.INVALID_BARCODE_FORMAT_LENGTH);
            errorCodeMap.put("partnerintegration.tickets.barcodealreadylocked", ErrorCode.BARCODE_ALREADY_USED);
            errorCodeMap.put("partnerintegration.tickets.barcodealreadylocked01", ErrorCode.BARCODE_ALREADY_USED);
            errorCodeMap.put("partnerintegration.tickets.barcodealreadylocked02", ErrorCode.BARCODE_ALREADY_USED);
            errorCodeMap.put("partnerintegration.tickets.maxfailedattempts", ErrorCode.MAX_FAILED_ATTEMPTS);
            errorCodeMap.put("partnerintegration.tickets.duplicatebarcodeswithinrequest", ErrorCode.DUPLICATE_BARCODE_ERROR);
            errorCodeMap.put("partnerintegration.tickets.configurationerror", ErrorCode.CONFIGURATION_ERROR);
            errorCodeMap.put("partnerintegration.tickets.accounterror", ErrorCode.ACCOUNT_ERROR);
            errorCodeMap.put("partnerintegration.tickets.barcodealreadysold", ErrorCode.BARCODE_ALREADY_SOLD);
            errorCodeMap.put("partnerintegration.tickets.barcodenotforsale", ErrorCode.BARCODE_NOT_FOR_SALE);
            errorCodeMap.put("partnerintegration.tickets.barcodetransferred", ErrorCode.BARCODE_TRANSFERRED);
            errorCodeMap.put("partnerintegration.tickets.consecutiveseaterror", ErrorCode.CONSECUTIVE_SEAT_ERROR);
            errorCodeMap.put("partnerintegration.tickets.eventcancelledorexpired", ErrorCode.EVENT_CANCELLED_OR_EXPIRED);
            errorCodeMap.put("partnerintegration.tickets.invalidbarcodeforevent", ErrorCode.INVALID_BARCODE_FOR_EVENT);
            errorCodeMap.put("partnerintegration.tickets.invalidbarcodeforevent01", ErrorCode.INVALID_BARCODE_FOR_EVENT);
            errorCodeMap.put("partnerintegration.tickets.invalidpiggyback", ErrorCode.INVALID_PIGGYBACK);
            errorCodeMap.put("partnerintegration.tickets.multiplesections", ErrorCode.MULTIPLE_SECTIONS);
            errorCodeMap.put("partnerintegration.tickets.systemerror", ErrorCode.SYSTEM_ERROR);
            errorCodeMap.put("partnerintegration.tickets.systemerror01", ErrorCode.SYSTEM_ERROR);
            errorCodeMap.put("partnerintegration.tickets.systemerror02", ErrorCode.SYSTEM_ERROR);
            errorCodeMap.put("partnerintegration.tickets.apierror", ErrorCode.SYSTEM_ERROR);
            errorCodeMap.put("partnerintegration.tickets.tdcthrottlingerror", ErrorCode.SYSTEM_ERROR);
            errorCodeMap.put("partnerintegration.tickets.unknownerror", ErrorCode.UNKNOWN_ERROR);
            
        }
    }

    /**
     * Returns property value for the given propertyName. This protected method has been created to
     * get around the static nature of the MasterStubHubProperties' methods for Unit tests. The test
     * classes are expected to override this method with custom implementation.
     *
     * @param propertyName
     * @param defaultValue
     * @return
     */
    protected String getProperty(String propertyName, String defaultValue) {
        return MasterStubHubProperties.getProperty(propertyName, defaultValue);
    }

    public String verifySthInventory(List<SeatProductsContext> seatProdContexts) {
        if (seatProdContexts == null || seatProdContexts.isEmpty()) {
            return "No seatProdContexts";
        }
        Listing listing = seatProdContexts.get(0).getCurrentListing();
        List<SthTicket> sthTickets = new ArrayList<SthTicket>();
        for (SeatProductsContext spContext : seatProdContexts) {

            log.info(
                    "Prep call verifySthInventory for listingId: " + spContext.getCurrentListing().getId());
            ArrayList<SeatProduct> seatProductList = spContext.getBarcodeSeatProductList();
            if (seatProductList == null) {
                return "FulfillmentArtifact is null";
            }
            for (SeatProduct sp : seatProductList) {
                SthTicket sthTicket = new SthTicket();
                String ptvTicketId = sp.getFulfillmentArtifact();
                try {
                    sthTicket.setPtvTicketId(Long.parseLong(ptvTicketId));
                } catch (NumberFormatException nfe) {
                    return "FulfillmentArtifact should be number for STH";
                }

                sthTicket.setTicketSeatId(sp.getSeatId());

                sthTickets.add(sthTicket);
            }
        }

        VerifySthInventoryRequest request = new VerifySthInventoryRequest();
        request.setTickets(sthTickets);

        String url = getProperty("partnerintegration.sth.api.url",
                "https://intsvc.api.stubcloudprod.com/partnerintegration/sth/v1");
        WebClient webClient = svcLocator.locate(url);
        webClient.accept(MediaType.APPLICATION_JSON);

        SHMonitor mon = SHMonitorFactory.getMonitor();
        VerifySthInventoryResponse response = null;
        try {
            mon.start();
            response = getResponseStr(webClient.post(request).getEntity());
        } finally {
            mon.stop();
            log.info("{} _operation=verifySthInventory _message=\"service call\" _respTime={}",SHMonitoringContext.get(), mon.getTime());
        }

        if (response.isVerificationPassed()) {
            if(Boolean.TRUE.equals(response.getBuyerRestricted())) {
                addBuyerRestrictedTraitToListing(listing);
            }
            return null;
        } else {
            String errorMsg = response.getError();
            if (errorMsg != null) {
                return errorMsg;
            } else {
                return "unknownError";
            }
        }
    }

    private VerifySthInventoryResponse getResponseStr(Object respEntity)  {
        InputStream responseStream = (InputStream) respEntity;
        byte[] data = new byte[1024];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (true) {
            int n = 0;
            try {
				n = responseStream.read(data);
			} catch (IOException e) {
				log.error("_message=\"Exception while reading the barcodes\"", e);
			}
            if (n == -1)
                break;
            bos.write(data, 0, n);
        }

        String responseString = bos.toString();
        return (VerifySthInventoryResponse) JsonUtil.toObject(responseString,
                VerifySthInventoryResponse.class);
    }

    public ErrorDetail verifyAndPersistBarcodes(Listing listing, List<SeatProduct> seats, boolean validateBarcode) {
        log.info(
                "_message=\"Partner for this event is integrated on Ship, continue with the flow\" listingId=" +
                        listing.getId() + " eventId=" + listing.getEventId());
        try {
            GetSRSForBarcodesRequest barcodesRequest = createGetSRSForBarcodesRequest(listing, seats, validateBarcode);

            String url = getProperty("partnerintegration.api.url",
                    "https://intsvc.api.stubcloudprod.com/partnerintegration/tickets/v1");
            WebClient webClient = svcLocator.locate(url);
            webClient.query("action", "lookup");
            webClient.query("persist", true);
            webClient.accept(MediaType.APPLICATION_JSON);

            SHMonitor mon = SHMonitorFactory.getMonitor();
            Response response;
            try {
                mon.start();
                response = webClient.post(barcodesRequest);
            } finally {
                mon.stop();
                log.info(SHMonitoringContext.get() + " _operation=verifyAndPersistBarcodes" + " _message= service call for eventId=" + listing.getEventId() + "  _respTime=" + mon.getTime());
            }
            log.info("message=\"Verify Barcodes call\" listingId={} eventId={} responseStatus={}", listing.getId(), listing.getEventId(), response.getStatus());
            GetSRSForBarcodesResponse barcodesResponse = getSRSBarcodesResponse(response.getEntity());
            if (Status.OK.getStatusCode() == response.getStatus()) {
                List<Ticket> responseTickets = barcodesResponse.getTickets();
                if(listing.getFaceValue() != null || hasFaceValueAtSeatLevel(listing.getTicketSeats())) {
                    boolean isValidFaceValue = validateFaceValue(responseTickets, listing.getFaceValue(), seats);
                    if(!isValidFaceValue) {
                        ErrorDetail errorDetail = new ErrorDetail();
                        errorDetail.setErrorCode(ErrorCode.INVALID_FACE_VALUE);
                        errorDetail.setErrorDescription("Invalid Face value");
                        return errorDetail;
                    }
                }
                
                if (responseTicketsAreBuyerRestricted(responseTickets)) {
                    addBuyerRestrictedTraitToListing(listing);
                }
                return null;
            } else {
                populateErrorCodes(); 
                ErrorDetail errorDetail = new ErrorDetail();
                List<Ticket> responseTickets = barcodesResponse.getTickets();
                if(responseTickets != null && !responseTickets.isEmpty()) {
                    for(Ticket partnetTicket : responseTickets) {
                        if(StringUtils.trimToNull(partnetTicket.getError()) != null) {
                            ErrorCode errorCode = errorCodeMap.get(partnetTicket.getError());
                            errorDetail.setErrorCode(errorCode);
                            break;
                        }
                    }
                }
              
                if(errorDetail.getErrorCode() == null) {
                    if(StringUtils.trimToNull(barcodesResponse.getError()) != null) {
                        ErrorCode errorCode = errorCodeMap.get(barcodesResponse.getError());
                        errorDetail.setErrorCode(errorCode);
                    }
                }
                
                if(errorDetail.getErrorCode() == null) {
                    errorDetail.setErrorCode(ErrorCode.UNKNOWN_ERROR);
                }
                return errorDetail;
            }
        } catch (Exception e) {
            log.error("_message=\"Exception while validating the barcodes\" listingId=" + listing.getId(), e);
        }
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setErrorCode(ErrorCode.SYSTEM_ERROR);
        return errorDetail;
        
    }
    
    private boolean validateFaceValue(List<Ticket> tickets, Money listingFaceValue) {
    	if(listingFaceValue != null ){
	        BigDecimal amount = listingFaceValue.getAmount();
	        if(amount != null && amount.doubleValue() > 0) {
	            if(tickets != null && tickets.size() > 0) {
	                Money responseFaceValue = tickets.get(0).getFaceValue();
	                if(responseFaceValue != null && responseFaceValue.getAmount() != null) {
	                    if(amount.compareTo(responseFaceValue.getAmount()) != 0) {
	        		        log.info("_message=\"faceValue at listingLevel \" isFaceValueAtListingValid={}", false );
	                        return false;
	                    }
	                }
	            } 
	        }
    	}
        return true;
    }
    
    private boolean validateFaceValue(List<Ticket> tickets, Money listingFaceValue, List<SeatProduct> seatProduct) {
    	
    	boolean result = false;
        
        if(seatProduct != null && tickets != null){
        	if(tickets.size() > 0 && seatProduct.size() > 0 && tickets.size() == seatProduct.size() ){
        		for(int i=0; i< tickets.size(); i++){
        			if(tickets.get(i).getFaceValue() != null && seatProduct.get(i).getFaceValue() != null){
    					Money ticketFaceValue = tickets.get(i).getFaceValue();
    					Money seatProductFaceValue = seatProduct.get(i).getFaceValue();
    					if(ticketFaceValue.getAmount() !=null && seatProductFaceValue.getAmount() != null){
    						if(ticketFaceValue.getAmount().compareTo(seatProductFaceValue.getAmount()) == 0) {
    							result = true;
    	                    }else{
    	        		        log.info("_message=\"faceValue at SeatLevel \" isFaceValueAtSeatLevelValid={}", false );
    	                    	return false;
    	                    }
    					}
        			}
        		}
        		
        	}
        }
        
        if(result){
	        log.info("_message=\"faceValue at SeatLevel \" isFaceValueAtSeatLevelValid={}", true );
        	return true;
        }
        
        return validateFaceValue(tickets,listingFaceValue );
    }
    
    private boolean responseTicketsAreBuyerRestricted(List<Ticket> responseTickets) {
        return responseTickets != null && responseTickets.size() > 0 && responseTickets.get(0).getBuyerRestricted();
    }
    
    public boolean hasFaceValueAtSeatLevel(List<TicketSeat> ticketSeats){
    	if(ticketSeats != null && ticketSeats.size() > 0) {
    		for(TicketSeat ticketSeat:ticketSeats){
    			if(ticketSeat.getFaceValue() != null && ticketSeat.getFaceValue().getAmount() != null 
    					&&  ticketSeat.getFaceValue().getAmount().doubleValue() >0 ){
    		        log.info("_message=\"faceValue at SeatLevel \" hasFaceValueAtSeatLevel={}", true );
    				return true;
    			}
    		}
    	}
    	
    	return false;
    }

    private void addBuyerRestrictedTraitToListing(Listing listing) {
        List<ListingSeatTrait> seatTraits = listing.getSeatTraits();
        if (seatTraits == null) {
            seatTraits = new ArrayList<>();
        }
        ListingSeatTrait buyerRestrictedSeatTrait = ticketSeatHelper.makeListingSeatTrait(listing.getId(), BUYER_RESTRICTED_SEAT_TRAIT_ID, CommonConstants.LISTING_API_V2, CommonConstants.LISTING_API_V2);
        buyerRestrictedSeatTrait.setSellerSpecifiedInd(false);
        buyerRestrictedSeatTrait.setExtSystemSpecifiedInd(true);
        seatTraits.add(buyerRestrictedSeatTrait);
        listing.setSeatTraits(seatTraits);
    }

    private GetSRSForBarcodesRequest createGetSRSForBarcodesRequest(Listing listing,
                                                                    List<SeatProduct> seats, boolean validateBarcode) {
        GetSRSForBarcodesRequest barcodesRequest = new GetSRSForBarcodesRequest();
        barcodesRequest.setListingId(listing.getId());
        barcodesRequest.setSellerId(listing.getSellerId());
        barcodesRequest.setValidateBarcode(validateBarcode);
        List<Ticket> tickets = new ArrayList<Ticket>();

        for (SeatProduct seat : seats) {
            Ticket ticket = new Ticket();
            ticket.setSection(listing.getSection());
            ticket.setRow(seat.getRow());
            ticket.setTicketSeatId(seat.getSeatId());
            if ("General Admission".equals(listing.getSection())) {
                ticket.setGa(true);
                ticket.setSeat("GA");
            } else {
                ticket.setGa(false);
                ticket.setSeat(seat.getSeat());
            }
            if (seat.isParkingPass()) {
                ticket.setTicketClass(TicketClass.PARKING_PASS);
            } else {
                ticket.setTicketClass(TicketClass.TICKET);
            }
            ticket.setBarcode(seat.getFulfillmentArtifact());
            ticket.setEventId(listing.getEventId());
            ticket.setFaceValue(seat.getFaceValue());
            tickets.add(ticket);
        }
        barcodesRequest.setTickets(tickets);
        return barcodesRequest;
    }

    private GetSRSForBarcodesResponse getSRSBarcodesResponse(Object respEntity) throws IOException {
        InputStream responseStream = (InputStream) respEntity;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, false);
        JacksonJaxbJsonProvider j = new JacksonJaxbJsonProvider();
        j.setMapper(objectMapper);
        return (GetSRSForBarcodesResponse) objectMapper.readValue(responseStream,
                GetSRSForBarcodesResponse.class);
    }
}
