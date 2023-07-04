package com.stubhub.domain.inventory.listings.v2.helper;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.fulfillment.pdf.v1.intf.request.AddPDFOnListingRequest;
import com.stubhub.domain.fulfillment.pdf.v1.intf.response.AddPDFOnListingResponse;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.entity.SeatProduct;
import com.stubhub.domain.inventory.listings.v2.util.SeatProductsContext;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

public class ListingFulfilmentHelperTest {
	
	@InjectMocks
	private ListingFulfilmentHelper  listingFulfilmentHelper;
	@Mock
	private SvcLocator svcLocator;	
	@Mock
	private WebClient webClient;
	@Mock
	private SHAPIThreadLocal shapiThreadLocal;
	@Mock
	private MediaType mediaType;
	@Mock
	private Response response;
	@Mock
	private SeatProductsContext seatProductsContext ;
	
	private Listing listing;
	
	@SuppressWarnings("unchecked")
	@BeforeMethod
	public void setUp() {
		listing = new Listing();
		listing.setEventId(12345l);
		listing.setId(23456l);
		listing.setQuantityRemain(3);
		listing.setSellerId(3456l);
	
		listingFulfilmentHelper = new ListingFulfilmentHelper() {
			public String getProperty(String propertyName, String defaultValue) {
				if ("fulfillment.pdf.v1.api.url".equals(propertyName)){
					return "http://api-int.slcq015.com/fulfillment/pdf/v1/listing/fileInfo";
				}
				return "";
			}
		};
		MockitoAnnotations.initMocks(this);

		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
	}
	
	@Test
	public void validateFileInfoIds_happypath(){
		//populate AddPDFOnListingRequest
		AddPDFOnListingRequest addPDFOnListingRequest = new AddPDFOnListingRequest();
		addPDFOnListingRequest.setEventId(listing.getEventId());
		addPDFOnListingRequest.setListingId(listing.getId());
		addPDFOnListingRequest.setQuantityRemain(listing.getQuantityRemain().longValue());
		addPDFOnListingRequest.setSellerId(listing.getSellerId());
		addPDFOnListingRequest.setValidateSeatNumbers(false);
		ArrayList<SeatProduct> seatProductList=new ArrayList<SeatProduct>(1);
		List<TicketSeat>  ticketSeatList=new ArrayList<TicketSeat>(1);
		List<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat> ticketSeats = new ArrayList<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat>(1);
		
		SeatProduct seat1= new SeatProduct();
		seat1.setSeat("test seat");
		seat1.setFulfillmentArtifact("1234");
		seat1.setRow("row");
		seat1.setSeatId(123l);
		seat1.setProductType(ProductType.TICKET);
		seatProductList.add(seat1);
		 
		List<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat> fulFilTicketSeatList =new ArrayList<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat>();
		for(SeatProduct seat: seatProductList){
			com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat fulTicketSeat = new com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat();
			fulTicketSeat.setFileInfoId(Long.valueOf(seat.getFulfillmentArtifact()));
			fulTicketSeat.setRow(seat.getRow());
			fulTicketSeat.setSeat(seat.getSeat());
			fulTicketSeat.setSeatType(seat.getProductType().name());
			
			fulFilTicketSeatList.add(fulTicketSeat);
		}
		addPDFOnListingRequest.setTicketSeats(fulFilTicketSeatList);
		
	    Mockito.when(seatProductsContext.getBarcodeSeatProductList()).thenReturn(seatProductList);
	    
		TicketSeat dbSeat = new TicketSeat();
		dbSeat.setRow("row");
		dbSeat.setSection("section");
		dbSeat.setSeatDesc("test");
		dbSeat.setGeneralAdmissionInd(true);
		dbSeat.setTixListTypeId(1l);
		dbSeat.setSeatStatusId(1l);
		ticketSeatList.add(dbSeat);
		listing.setSection(CommonConstants.GENERAL_ADMISSION);
		Mockito.when(seatProductsContext.getTicketSeatsFromCache()).thenReturn(ticketSeatList);
		listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing);
		
	     Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
	     webClient.accept(MediaType.APPLICATION_JSON);
	     Mockito.when(webClient.post(Mockito.any())).thenReturn(getResponse());
		Assert.assertTrue(listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing));
		Assert.assertTrue(listingFulfilmentHelper.validatePDFFileInfo(addPDFOnListingRequest));
	}

	@Test
	public void validateFileInfoIds_happypath2(){
		//populate AddPDFOnListingRequest
		AddPDFOnListingRequest addPDFOnListingRequest = new AddPDFOnListingRequest();
		addPDFOnListingRequest.setEventId(listing.getEventId());
		addPDFOnListingRequest.setListingId(listing.getId());
		addPDFOnListingRequest.setQuantityRemain(listing.getQuantityRemain().longValue());
		addPDFOnListingRequest.setSellerId(listing.getSellerId());
		addPDFOnListingRequest.setValidateSeatNumbers(false);
		ArrayList<SeatProduct> seatProductList=new ArrayList<SeatProduct>(1);
		List<TicketSeat>  ticketSeatList=new ArrayList<TicketSeat>(1);
		List<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat> ticketSeats = new ArrayList<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat>(1);
		
		SeatProduct seat1= new SeatProduct();
		seat1.setSeat("test seat");
		seat1.setFulfillmentArtifact("1234");
		seat1.setRow("row");
		seat1.setSeatId(123l);
		seat1.setProductType(ProductType.PARKING_PASS);
		seatProductList.add(seat1);
		 
		List<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat> fulFilTicketSeatList =new ArrayList<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat>();
		for(SeatProduct seat: seatProductList){
			com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat fulTicketSeat = new com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat();
			fulTicketSeat.setFileInfoId(Long.valueOf(seat.getFulfillmentArtifact()));
			fulTicketSeat.setRow(seat.getRow());
			fulTicketSeat.setSeat(seat.getSeat());
			fulTicketSeat.setSeatType(seat.getProductType().name());
			
			fulFilTicketSeatList.add(fulTicketSeat);
		}
		addPDFOnListingRequest.setTicketSeats(fulFilTicketSeatList);
		
	    Mockito.when(seatProductsContext.getBarcodeSeatProductList()).thenReturn(seatProductList);
	    
		TicketSeat dbSeat = new TicketSeat();
		dbSeat.setRow("row");
		dbSeat.setSection("section");
		dbSeat.setSeatDesc("test");
		dbSeat.setGeneralAdmissionInd(true);
		dbSeat.setTixListTypeId(1l);
		dbSeat.setSeatStatusId(1l);
		ticketSeatList.add(dbSeat);
		listing.setSection(CommonConstants.GENERAL_ADMISSION);
		Mockito.when(seatProductsContext.getTicketSeatsFromCache()).thenReturn(ticketSeatList);
		listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing);
		
	     Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
	     webClient.accept(MediaType.APPLICATION_JSON);
	     Mockito.when(webClient.post(Mockito.any())).thenReturn(getResponse());
		Assert.assertTrue(listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing));
		Assert.assertTrue(listingFulfilmentHelper.validatePDFFileInfo(addPDFOnListingRequest));
	}

	@Test
	public void validateFileInfoIds_seatandTicketNullCheck(){
		//populate AddPDFOnListingRequest
		AddPDFOnListingRequest addPDFOnListingRequest = new AddPDFOnListingRequest();
		addPDFOnListingRequest.setEventId(listing.getEventId());
		addPDFOnListingRequest.setListingId(listing.getId());
		addPDFOnListingRequest.setQuantityRemain(listing.getQuantityRemain().longValue());
		addPDFOnListingRequest.setSellerId(listing.getSellerId());
		addPDFOnListingRequest.setValidateSeatNumbers(false);
		ArrayList<SeatProduct> seatProductList=new ArrayList<SeatProduct>(1);
		List<TicketSeat>  ticketSeatList=new ArrayList<TicketSeat>(1);
		List<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat> ticketSeats = new ArrayList<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat>(1);
		
		SeatProduct seat1= new SeatProduct();
		seat1.setSeat("test seat");
		seat1.setFulfillmentArtifact("1234");
		seat1.setRow("row");
		seat1.setSeatId(123l);
		seat1.setProductType(ProductType.TICKET);
		 
		seatProductList.add(seat1);
		List<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat> fulFilTicketSeatList =new ArrayList<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat>();
		for(SeatProduct seat: seatProductList){
			com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat fulTicketSeat = new com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat();
			fulTicketSeat.setFileInfoId(Long.valueOf(seat.getFulfillmentArtifact()));
			fulTicketSeat.setRow(seat.getRow());
			fulTicketSeat.setSeat(seat.getSeat());
			fulTicketSeat.setSeatType(ProductType.PARKING_PASS.toString());
			
			fulFilTicketSeatList.add(fulTicketSeat);
		}
		addPDFOnListingRequest.setTicketSeats(fulFilTicketSeatList);
		
	    Mockito.when(seatProductsContext.getBarcodeSeatProductList()).thenReturn(null);
	    Mockito.when(seatProductsContext.getPassedSeatProductList(Mockito.anyBoolean())).thenReturn(seatProductList);
	
		TicketSeat dbSeat = new TicketSeat();
		dbSeat.setRow("row");
		dbSeat.setSection("section");
		dbSeat.setSeatDesc("test");
		dbSeat.setGeneralAdmissionInd(true);
		dbSeat.setTixListTypeId(1l);
		dbSeat.setSeatStatusId(1l);
		ticketSeatList.add(dbSeat);
		listing.setSection(CommonConstants.GENERAL_ADMISSION);
		Mockito.when(seatProductsContext.getTicketSeatsFromCache()).thenReturn(ticketSeatList);
		listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing);
		
	     Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
	     webClient.accept(MediaType.APPLICATION_JSON);
	     Mockito.when(webClient.post(Mockito.any())).thenReturn(getResponse());
		Assert.assertTrue(listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing));
		Assert.assertTrue(listingFulfilmentHelper.validatePDFFileInfo(addPDFOnListingRequest));
	}
	
	@Test
	public void validateFileInfoIds_error(){
		//populate AddPDFOnListingRequest
		AddPDFOnListingRequest addPDFOnListingRequest = new AddPDFOnListingRequest();
		addPDFOnListingRequest.setEventId(listing.getEventId());
		addPDFOnListingRequest.setListingId(listing.getId());
		addPDFOnListingRequest.setQuantityRemain(listing.getQuantityRemain().longValue());
		addPDFOnListingRequest.setSellerId(listing.getSellerId());
		addPDFOnListingRequest.setValidateSeatNumbers(false);
		ArrayList<SeatProduct> seatProductList=new ArrayList<SeatProduct>(1);
		List<TicketSeat>  ticketSeatList=new ArrayList<TicketSeat>(1);
		List<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat> ticketSeats = new ArrayList<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat>(1);
		
		SeatProduct seat1= new SeatProduct();
		seat1.setSeat("test seat");
		seat1.setFulfillmentArtifact("1234");
		seat1.setRow("row");
		seat1.setSeatId(123l);
		seat1.setProductType(ProductType.TICKET);
		 
		seatProductList.add(seat1);
		List<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat> fulFilTicketSeatList =new ArrayList<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat>();
		for(SeatProduct seat: seatProductList){
			com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat fulTicketSeat = new com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat();
			fulTicketSeat.setFileInfoId(Long.valueOf(seat.getFulfillmentArtifact()));
			fulTicketSeat.setRow(seat.getRow());
			fulTicketSeat.setSeat(seat.getSeat());
			fulTicketSeat.setSeatType(ProductType.PARKING_PASS.toString());
			
			fulFilTicketSeatList.add(fulTicketSeat);
		}
		addPDFOnListingRequest.setTicketSeats(fulFilTicketSeatList);
		
	    Mockito.when(seatProductsContext.getBarcodeSeatProductList()).thenReturn(seatProductList);
	
		TicketSeat dbSeat = new TicketSeat();
		dbSeat.setRow("row");
		dbSeat.setSection("section");
		dbSeat.setSeatDesc("test");
		dbSeat.setGeneralAdmissionInd(true);
		dbSeat.setTixListTypeId(2l);
		dbSeat.setSeatStatusId(1l);
		ticketSeatList.add(dbSeat);
		listing.setSection(CommonConstants.GENERAL_ADMISSION);
		Mockito.when(seatProductsContext.getTicketSeatsFromCache()).thenReturn(ticketSeatList);
		listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing);
		
	     Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
	     webClient.accept(MediaType.APPLICATION_JSON);
	     Mockito.when(webClient.post(Mockito.any())).thenReturn(getResponse_error());
		Assert.assertFalse(listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing));
		Assert.assertFalse(listingFulfilmentHelper.validatePDFFileInfo(addPDFOnListingRequest));
		
	}
	@Test
	public void validateFileInfoIds_purchaseerror(){
		//populate AddPDFOnListingRequest
		AddPDFOnListingRequest addPDFOnListingRequest = new AddPDFOnListingRequest();
		addPDFOnListingRequest.setEventId(listing.getEventId());
		addPDFOnListingRequest.setListingId(listing.getId());
		addPDFOnListingRequest.setQuantityRemain(listing.getQuantityRemain().longValue());
		addPDFOnListingRequest.setSellerId(listing.getSellerId());
		addPDFOnListingRequest.setValidateSeatNumbers(false);
		ArrayList<SeatProduct> seatProductList=new ArrayList<SeatProduct>(1);
		List<TicketSeat>  ticketSeatList=new ArrayList<TicketSeat>(1);
		List<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat> ticketSeats = new ArrayList<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat>(1);
		
		SeatProduct seat1= new SeatProduct();
		seat1.setSeat("test seat");
		seat1.setFulfillmentArtifact("1234");
		seat1.setRow("row");
		seat1.setSeatId(123l);
		seat1.setProductType(ProductType.TICKET);
		 
		seatProductList.add(seat1);
		List<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat> fulFilTicketSeatList =new ArrayList<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat>();
		for(SeatProduct seat: seatProductList){
			com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat fulTicketSeat = new com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat();
			fulTicketSeat.setFileInfoId(Long.valueOf(seat.getFulfillmentArtifact()));
			fulTicketSeat.setRow(seat.getRow());
			fulTicketSeat.setSeat(seat.getSeat());
			fulTicketSeat.setSeatType(ProductType.PARKING_PASS.toString());
			
			fulFilTicketSeatList.add(fulTicketSeat);
		}
		addPDFOnListingRequest.setTicketSeats(fulFilTicketSeatList);
		
	    Mockito.when(seatProductsContext.getBarcodeSeatProductList()).thenReturn(seatProductList);
	
		TicketSeat dbSeat = new TicketSeat();
		dbSeat.setRow("row");
		dbSeat.setSection("section");
		dbSeat.setSeatDesc("test");
		dbSeat.setGeneralAdmissionInd(true);
		dbSeat.setTixListTypeId(2l);
		dbSeat.setSeatStatusId(3l);//set the seat status as purchased which causes error
		ticketSeatList.add(dbSeat);
		listing.setSection(CommonConstants.GENERAL_ADMISSION);
		Mockito.when(seatProductsContext.getTicketSeatsFromCache()).thenReturn(ticketSeatList);
		listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing);
		
	     Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
	     webClient.accept(MediaType.APPLICATION_JSON);
	     Mockito.when(webClient.post(Mockito.any())).thenReturn(getResponse_error());
		Assert.assertFalse(listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing));
		Assert.assertFalse(listingFulfilmentHelper.validatePDFFileInfo(addPDFOnListingRequest));
	}
	
	@Test
	public void validateFileInfoIds_500error(){
		//populate AddPDFOnListingRequest
		AddPDFOnListingRequest addPDFOnListingRequest = new AddPDFOnListingRequest();
		addPDFOnListingRequest.setEventId(listing.getEventId());
		addPDFOnListingRequest.setListingId(listing.getId());
		addPDFOnListingRequest.setQuantityRemain(listing.getQuantityRemain().longValue());
		addPDFOnListingRequest.setSellerId(listing.getSellerId());
		addPDFOnListingRequest.setValidateSeatNumbers(false);
		ArrayList<SeatProduct> seatProductList=new ArrayList<SeatProduct>(1);
		List<TicketSeat>  ticketSeatList=new ArrayList<TicketSeat>(1);
		List<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat> ticketSeats = new ArrayList<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat>(1);
		
		SeatProduct seat1= new SeatProduct();
		seat1.setSeat("test seat");
		seat1.setFulfillmentArtifact("1234");
		seat1.setRow("row");
		seat1.setSeatId(123l);
		seat1.setProductType(ProductType.TICKET);
		 
		seatProductList.add(seat1);
		List<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat> fulFilTicketSeatList =new ArrayList<com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat>();
		for(SeatProduct seat: seatProductList){
			com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat fulTicketSeat = new com.stubhub.domain.fulfillment.pdf.v1.intf.request.TicketSeat();
			fulTicketSeat.setFileInfoId(Long.valueOf(seat.getFulfillmentArtifact()));
			fulTicketSeat.setRow(seat.getRow());
			fulTicketSeat.setSeat(seat.getSeat());
			fulTicketSeat.setSeatType(ProductType.PARKING_PASS.toString());
			
			fulFilTicketSeatList.add(fulTicketSeat);
		}
		addPDFOnListingRequest.setTicketSeats(fulFilTicketSeatList);
		
	    Mockito.when(seatProductsContext.getBarcodeSeatProductList()).thenReturn(seatProductList);
	
		TicketSeat dbSeat = new TicketSeat();
		dbSeat.setRow("row");
		dbSeat.setSection("section");
		dbSeat.setSeatDesc("test");
		dbSeat.setGeneralAdmissionInd(true);
		dbSeat.setTixListTypeId(2l);
		dbSeat.setSeatStatusId(1l);
		ticketSeatList.add(dbSeat);
		listing.setSection(CommonConstants.GENERAL_ADMISSION);
		Mockito.when(seatProductsContext.getTicketSeatsFromCache()).thenReturn(ticketSeatList);
		listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing);
		
	     Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
	     webClient.accept(MediaType.APPLICATION_JSON);
	     Mockito.when(webClient.post(Mockito.any())).thenReturn(getResponse_500error());
		Assert.assertFalse(listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing));
		Assert.assertFalse(listingFulfilmentHelper.validatePDFFileInfo(addPDFOnListingRequest));
	}

	@Test
	public void validateFileInfoIds_nullProductListcheck(){
		
		Mockito.when(seatProductsContext.getBarcodeSeatProductList()).thenReturn(null);
		Mockito.when(seatProductsContext.getTicketSeatsFromCache()).thenReturn(new ArrayList<TicketSeat>());
		Assert.assertFalse(listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing));
	}
	
	@Test
	public void validateFileInfoIds_nullTicketSeatcheck(){
	//	AddPDFOnListingRequest addPDFOnListingRequest =new AddPDFOnListingRequest();
		ArrayList<SeatProduct> seatProductList = new ArrayList<SeatProduct>(1);
		SeatProduct seat1= new SeatProduct();
		seat1.setSeat("test seat");
		seat1.setFulfillmentArtifact("1234");
		seat1.setRow("row");
		seat1.setSeatId(123l);
		seat1.setSeat("seat");
		 
		seatProductList.add(seat1);
		
	    Mockito.when(seatProductsContext.getBarcodeSeatProductList()).thenReturn(seatProductList);
		Mockito.when(seatProductsContext.getTicketSeatsFromCache()).thenReturn(new ArrayList<TicketSeat>());
		Assert.assertFalse(listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing));
	}

	@Test
	public void validateFileInfoIds_gatrue(){
	//	AddPDFOnListingRequest addPDFOnListingRequest =new AddPDFOnListingRequest();
		ArrayList<SeatProduct> seatProductList = new ArrayList<SeatProduct>(1);
		SeatProduct seat1= new SeatProduct();
		seat1.setSeat("test seat");
		seat1.setFulfillmentArtifact("1234");
		seat1.setRow("row");
		seat1.setSeatId(123l);
		seat1.setProductType(ProductType.TICKET);
		 
		seatProductList.add(seat1);
		
	    Mockito.when(seatProductsContext.getBarcodeSeatProductList()).thenReturn(seatProductList);
	    
	    ArrayList<TicketSeat> ticketSeatList  = new ArrayList<TicketSeat>(3);
		TicketSeat dbSeat = new TicketSeat();
		dbSeat.setRow("row");
		dbSeat.setSection("section");
		dbSeat.setSeatDesc("test");
		dbSeat.setTixListTypeId(2l);
		dbSeat.setSeatStatusId(1l);
		ticketSeatList.add(dbSeat);
		listing.setSection(CommonConstants.GENERAL_ADMISSION);
		Mockito.when(seatProductsContext.getTicketSeatsFromCache()).thenReturn(ticketSeatList);
		listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing);
	}
		
	@Test
	public void validateFileInfoIds_gafalse(){
	//	AddPDFOnListingRequest addPDFOnListingRequest =new AddPDFOnListingRequest();
		ArrayList<SeatProduct> seatProductList = new ArrayList<SeatProduct>(1);
		SeatProduct seat1= new SeatProduct();
		seat1.setSeat("test seat");
		seat1.setFulfillmentArtifact("1234");
		seat1.setRow("row");
		seat1.setSeatId(123l);
		seat1.setProductType(ProductType.TICKET);
		 
		seatProductList.add(seat1);
		
	    Mockito.when(seatProductsContext.getBarcodeSeatProductList()).thenReturn(seatProductList);
	    
	    ArrayList<TicketSeat> ticketSeatList  = new ArrayList<TicketSeat>(3);
		TicketSeat dbSeat = new TicketSeat();
		dbSeat.setRow("row");
		dbSeat.setSection("section");
		dbSeat.setSeatDesc("test");
		dbSeat.setTixListTypeId(2l);
		dbSeat.setSeatStatusId(1l);
		ticketSeatList.add(dbSeat);
		listing.setSection("GA");
		Mockito.when(seatProductsContext.getTicketSeatsFromCache()).thenReturn(ticketSeatList);
		listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing);
	}

	@Test
	public void validateFileInfoIds_dbzerosizecheck(){
		Mockito.when(seatProductsContext.getBarcodeSeatProductList()).thenReturn(new ArrayList<SeatProduct>(3));
		Mockito.when(seatProductsContext.getTicketSeatsFromCache()).thenReturn(new ArrayList<TicketSeat>());
		Assert.assertFalse(listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing));
	}
	
	@Test
	public void validateFileInfoIds_mismatchcheck(){
		ArrayList<SeatProduct> seatProductList = new ArrayList<SeatProduct>(1);
		SeatProduct seat1= new SeatProduct();
		seat1.setSeat("test seat");
		seat1.setFulfillmentArtifact("1234");
		seat1.setRow("row");
		seat1.setSeatId(123l);
		seat1.setProductType(ProductType.TICKET);
		 
		seatProductList.add(seat1);
		//SELLAPI-956 10/13/15 START
		SeatProduct seat2= new SeatProduct();
		seat2.setSeat("test seat");
		seat2.setFulfillmentArtifact("");
		seat2.setRow("row");
		seat2.setSeatId(123l);
		seat2.setProductType(ProductType.TICKET);
		 
		seatProductList.add(seat2);
		//SELLAPI-956 10/13/15 END
		
	    Mockito.when(seatProductsContext.getBarcodeSeatProductList()).thenReturn(seatProductList);
	    
	    ArrayList<TicketSeat> ticketSeatList  = new ArrayList<TicketSeat>(3);
		TicketSeat dbSeat = new TicketSeat();
		dbSeat.setRow("row");
		dbSeat.setSection("section");
		dbSeat.setSeatDesc("test");
		ticketSeatList.add(dbSeat);
		TicketSeat dbSeat1 = new TicketSeat();
		dbSeat1.setRow("row");
		dbSeat1.setSection("section");
		dbSeat1.setSeatDesc("test");
		dbSeat1.setSeatStatusId(1l);
		ticketSeatList.add(dbSeat1);
		TicketSeat dbSeat2 = new TicketSeat();
		dbSeat2.setRow("row");
		dbSeat2.setSection("section");
		dbSeat2.setSeatDesc("test");
		dbSeat2.setSeatStatusId(1l);
		ticketSeatList.add(dbSeat2);
		Mockito.when(seatProductsContext.getTicketSeatsFromCache()).thenReturn(ticketSeatList);
		Assert.assertFalse(listingFulfilmentHelper.validateFileInfoIds(seatProductsContext, listing));
	}
	
	private Response getResponse() {
		Response response =  new Response() {
			@Override
			public int getStatus() {
				return 200;
			}
			@Override
			public MultivaluedMap<String, Object> getMetadata() {				
				return null;
			}
			@Override
			public Object getEntity() {
				AddPDFOnListingResponse pdfResponse= new AddPDFOnListingResponse();
				pdfResponse.setFulfillmentStatus("fulfilled");
				
				return pdfResponse;
			}
		};
		return response;
	}
	
	private Response getResponse_error() {
		Response response =  new Response() {
			@Override
			public int getStatus() {
				return 200;
			}
			@Override
			public MultivaluedMap<String, Object> getMetadata() {				
				return null;
			}
			@Override
			public Object getEntity() {
				AddPDFOnListingResponse pdfResponse= new AddPDFOnListingResponse();
				pdfResponse.setFulfillmentStatus("notfulfilled");
				
				return pdfResponse;
			}
		};
		return response;
	}
	private Response getResponse_500error() {
		Response response =  new Response() {
			@Override
			public int getStatus() {
				return 500;
			}
			@Override
			public MultivaluedMap<String, Object> getMetadata() {				
				return null;
			}
			@Override
			public Object getEntity() {
				AddPDFOnListingResponse pdfResponse= new AddPDFOnListingResponse();
				pdfResponse.setFulfillmentStatus("notfulfilled");
				
				return pdfResponse;
			}
		};
		return response;
	}

}