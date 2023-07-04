package com.stubhub.domain.inventory.listings.v2.helper;

import static org.mockito.Mockito.never;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingException;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.entity.SeatProduct;
import com.stubhub.domain.inventory.listings.v2.util.SeatProductsContext;
import com.stubhub.domain.inventory.listings.v2.util.SeatProductsManipulator;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.newplatform.common.entity.Money;

//UpdateListingv2 - Error when tried to update the listing with seats and fulfillment artifact
public class SeatProductsManipulatorTest {

	SeatProductsContext seatProductsContext;

	@BeforeMethod
	public void setUp() {
		seatProductsContext = Mockito.mock(SeatProductsContext.class);
		Mockito.doReturn(false).when(seatProductsContext).getHasDelOperation();
		Mockito.doReturn(true).when(seatProductsContext).getHasUpdOperation();
		Mockito.doReturn(false).when(seatProductsContext).getHasAddOperation();
		Mockito.doReturn(false).when(seatProductsContext).isCreate();
		Mockito.doReturn(true).when(seatProductsContext).getIsDBTicketsChanged();
		try {
			Mockito.doNothing().when(seatProductsContext).validateAllSeatsAndRows();
		} catch (ListingException e1) {
			e1.printStackTrace();
		}
		Mockito.doNothing().when(seatProductsContext).setTicketSeatsBackInCurrentListing();
	}

	//SELLAPI-1011 06/25/15 START
	protected SeatProduct getSeatProduct(ProductType prodType, String ffArtifact, String row,
										 String seat, long seatId, Operation operation, String externalId ) {
		SeatProduct seatProd = new SeatProduct();
		seatProd.setProductType(prodType);
		seatProd.setFulfillmentArtifact(ffArtifact);
		seatProd.setRow(row);
		seatProd.setSeat(seat);
		seatProd.setSeatId(seatId);
		seatProd.setOperation(operation);
		seatProd.setExternalId(externalId);
		return seatProd;
	}
	
	protected SeatProduct getSeatProductWithFaceValue(ProductType prodType, String ffArtifact, String row,
			 String seat, long seatId, Operation operation, String externalId, Money faceValue) {
		SeatProduct seatProd = new SeatProduct();
		seatProd.setProductType(prodType);
		seatProd.setFulfillmentArtifact(ffArtifact);
		seatProd.setRow(row);
		seatProd.setSeat(seat);
		seatProd.setSeatId(seatId);
		seatProd.setOperation(operation);
		seatProd.setExternalId(externalId);
		seatProd.setFaceValue(faceValue);
		return seatProd;
	}

	protected List<TicketSeat> getTicketSeats(long ticketId, long satusId, String section, String row,
											  int count, boolean gaInd, boolean noSeatNum, String[] externalId )	{
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		for(int i=0; i<count; i++){
			TicketSeat ts = new TicketSeat();
			ts.setTicketId ( ticketId );
			ts.setSeatStatusId(satusId);
			ts.setTixListTypeId(1L);
			ts.setGeneralAdmissionInd(gaInd);
			ts.setRow(row);
			ts.setSection(section);
			if (noSeatNum) {
				ts.setSeatNumber (null );
			} else {
				ts.setSeatNumber (i+"");
			}
			ts.setExternalSeatId(externalId[i]);
			ticketSeats.add(ts);
		}
		return ticketSeats;
	}

	@Test
	public void processTicketProductTest01() {

		Listing listing = new Listing();
		listing.setQuantityRemain(2);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setSystemStatus(ListingStatus.INACTIVE.name());
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>();
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "15", "3", 123L, Operation.UPDATE, "1"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "15", "4", 124L, Operation.UPDATE, "2"));

		List<TicketSeat> dbSeats = getTicketSeats(123L, 1L, "sec-10", "15", 2, false, true, new String[] {"2","3"});

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
		}
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
	}

	@Test
	public void processTicketProductTest02() {

		Listing listing = new Listing();
		listing.setQuantityRemain(2);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setSystemStatus(ListingStatus.INACTIVE.name());
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);
		//ProductType prodType, String ffArtifact, String row, String seat, long seatId, Operation operation, String externalId
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "15", "3", 123L, Operation.UPDATE, "1"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "15", "4", 124L, Operation.UPDATE, "3"));

		//long ticketId, long satusId, String section, String row, int count, boolean gaInd, boolean noSeatNum, String[] externalId
		List<TicketSeat> dbSeats = getTicketSeats(123L, 1L, "sec-10", "N/A", 2, true, true, new String[] {"2","3"});

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
		}
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
	}
	
	
	@Test
	public void processTicketProductUniqueTikNumTest() {

		Listing listing = new Listing();
		listing.setQuantityRemain(2);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setSystemStatus(ListingStatus.INACTIVE.name());
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);

		SeatProduct seatProd = new SeatProduct();
		seatProd.setProductType(ProductType.TICKET);
		seatProd.setFulfillmentArtifact(null);
		seatProd.setRow("LOT");
		seatProd.setSeat("LOT");
		seatProd.setSeatId(123L);
		seatProd.setOperation(Operation.UPDATE);
		seatProd.setUniqueTicketNumber("12321315");
		seatProductList.add(seatProd);

		SeatProduct seatProd1 = new SeatProduct();
		seatProd1.setProductType(ProductType.TICKET);
		seatProd1.setFulfillmentArtifact(null);
		seatProd1.setRow("15");
		seatProd1.setSeat("6");
		seatProd1.setSeatId(124L);
		seatProd1.setOperation(Operation.UPDATE);
		seatProd1.setUniqueTicketNumber(" ");
		seatProductList.add(seatProd1);

		//long ticketId, long satusId, String section, String row, int count, boolean gaInd, boolean noSeatNum, String[] externalId
		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts = new TicketSeat();
		ts.setTicketId ( 123L );
		ts.setSeatStatusId(1L);
		ts.setTixListTypeId(2L);
		ts.setGeneralAdmissionInd(false);
		ts.setRow("LOT");
		ts.setSection("LOT");
		ts.setSeatNumber ("LOT");
		ts.setUniqueTicketNumber("1232131");
		dbSeats.add(ts);

		TicketSeat ts1 = new TicketSeat();
		ts1.setTicketId ( 124L );
		ts1.setSeatStatusId(1L);
		ts1.setTixListTypeId(1L);
		ts1.setGeneralAdmissionInd(false);
		ts1.setRow("15");
		ts1.setSection("23");
		ts1.setSeatNumber (null);
		ts1.setUniqueTicketNumber("123213");
		dbSeats.add(ts1);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
		}
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
	}
	
	

	@Test(expectedExceptions={ListingBusinessException.class})
	public void processTicketProductTest03() {

		Listing listing = new Listing();
		listing.setQuantityRemain(2);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setSystemStatus(ListingStatus.INACTIVE.name());
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "15", "3", 123L, Operation.UPDATE, "1"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "16", "4", 124L, Operation.UPDATE, "3"));

		List<TicketSeat> dbSeats = getTicketSeats(123L, 4L, "sec-10", "17", 2, true, false, new String[] {"2","3"});

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void processTicketProductTest04() {

		Listing listing = new Listing();
		listing.setQuantityRemain(2);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setSystemStatus(ListingStatus.INACTIVE.name());
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, null, null, 123L, Operation.UPDATE, "1"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, null, "4", 124L, Operation.UPDATE, "2"));

		List<TicketSeat> dbSeats = getTicketSeats(123L, 1L, "sec-10", "15", 2, true, false, new String[] {"2","3"});

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void processTicketProductTest05() {

		Listing listing = new Listing();
		listing.setQuantityRemain(2);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setSystemStatus(ListingStatus.INACTIVE.name());
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, null, "3", 123L, Operation.UPDATE, "1"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "N/A", "4", 124L, Operation.UPDATE, "2"));

		List<TicketSeat> dbSeats = getTicketSeats(123L, 1L, "sec-10", null, 2, true, false, new String[] {"2","3"});

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
		}
	}

	@Test(expectedExceptions={ListingBusinessException.class})
	public void processTicketProductTest06() {

		Listing listing = new Listing();
		listing.setQuantityRemain(1);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setSystemStatus(ListingStatus.INACTIVE.name());
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, null, "3", 123L, Operation.UPDATE, "1"));

		List<TicketSeat> dbSeats = getTicketSeats(123L, 3L, "sec-10", null, 1, true, true, new String[] {"2"});

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void processTicketProductTest07() {

		Listing listing = new Listing();
		listing.setQuantityRemain(1);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setSystemStatus(ListingStatus.INACTIVE.name());
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);

		SeatProduct seatProd = new SeatProduct();
		seatProd.setProductType(ProductType.PARKING_PASS);
		seatProd.setFulfillmentArtifact(null);
		seatProd.setRow("LOT");
		seatProd.setSeat("LOT");
		seatProd.setSeatId(123L);
		seatProd.setOperation(Operation.UPDATE);
		seatProductList.add(seatProd);

		SeatProduct seatProd1 = new SeatProduct();
		seatProd1.setProductType(ProductType.TICKET);
		seatProd1.setFulfillmentArtifact(null);
		seatProd1.setRow("15");
		seatProd1.setSeat("6");
		seatProd1.setSeatId(124L);
		seatProd1.setOperation(Operation.UPDATE);
		seatProductList.add(seatProd1);

		//long ticketId, long satusId, String section, String row, int count, boolean gaInd, boolean noSeatNum, String[] externalId
		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts = new TicketSeat();
		ts.setTicketId ( 123L );
		ts.setSeatStatusId(1L);
		ts.setTixListTypeId(2L);
		ts.setGeneralAdmissionInd(false);
		ts.setRow("LOT");
		ts.setSection("LOT");
		ts.setSeatNumber ("LOT");
		dbSeats.add(ts);

		TicketSeat ts1 = new TicketSeat();
		ts1.setTicketId ( 124L );
		ts1.setSeatStatusId(1L);
		ts1.setTixListTypeId(1L);
		ts1.setGeneralAdmissionInd(false);
		ts1.setRow("15");
		ts1.setSection("23");
		ts1.setSeatNumber (null);
		dbSeats.add(ts1);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
		}
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
	}
	


	@Test(expectedExceptions={ListingBusinessException.class})
	public void processTicketProductTest08() {

		Listing listing = new Listing();
		listing.setQuantityRemain(1);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setSystemStatus(ListingStatus.INACTIVE.name());
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);

		SeatProduct seatProd = new SeatProduct();
		seatProd.setProductType(ProductType.PARKING_PASS);
		seatProd.setFulfillmentArtifact(null);
		seatProd.setRow("LOT");
		seatProd.setSeat("LOT");
		seatProd.setSeatId(123L);
		seatProd.setOperation(Operation.UPDATE);
		seatProductList.add(seatProd);

		SeatProduct seatProd1 = new SeatProduct();
		seatProd1.setProductType(ProductType.TICKET);
		seatProd1.setFulfillmentArtifact(null);
		seatProd1.setRow("15");
		seatProd1.setSeat("6");
		seatProd1.setSeatId(124L);
		seatProd1.setOperation(Operation.UPDATE);
		seatProductList.add(seatProd1);

		//long ticketId, long satusId, String section, String row, int count, boolean gaInd, boolean noSeatNum, String[] externalId
		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts = new TicketSeat();
		ts.setTicketId ( 123L );
		ts.setSeatStatusId(1L);
		ts.setTixListTypeId(1L);
		ts.setGeneralAdmissionInd(false);
		ts.setRow("LOT");
		ts.setSection("LOT");
		ts.setSeatNumber ("LOT");
		dbSeats.add(ts);

		TicketSeat ts1 = new TicketSeat();
		ts1.setTicketId ( 124L );
		ts1.setTixListTypeId(1L);
		ts1.setSeatStatusId(1L);
		ts1.setGeneralAdmissionInd(false);
		ts1.setRow("15");
		ts1.setSection("23");
		ts1.setSeatNumber ("15");
		dbSeats.add(ts1);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
		}
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
	}

	@Test
	public void processTicketProductTest09() {

		Listing listing = new Listing();
		listing.setQuantityRemain(2);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setSystemStatus(ListingStatus.INACTIVE.name());
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>();

		SeatProduct seatProd = new SeatProduct();
		seatProd.setProductType(ProductType.PARKING_PASS);
		seatProd.setFulfillmentArtifact("123");
		seatProd.setRow("LOT");
		seatProd.setSeat("Parking Pass");
		seatProd.setOperation(Operation.UPDATE);
		seatProductList.add(seatProd);

		{
			SeatProduct seatProd1 = new SeatProduct();
			seatProd1.setProductType(ProductType.TICKET);
			seatProd1.setFulfillmentArtifact("123");
			seatProd1.setRow(null);
			seatProd1.setSeat(null);
			seatProd1.setOperation(Operation.UPDATE);
			seatProductList.add(seatProd1);
		}
		{
			SeatProduct seatProd1 = new SeatProduct();
			seatProd1.setProductType(ProductType.TICKET);
			seatProd1.setFulfillmentArtifact("123");
			seatProd1.setRow("N/A");
			seatProd1.setSeat(null);
			seatProd1.setOperation(Operation.UPDATE);
			seatProductList.add(seatProd1);
		}

		//long ticketId, long satusId, String section, String row, int count, boolean gaInd, boolean noSeatNum, String[] externalId
		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts = new TicketSeat();
		ts.setTicketId ( 123L );
		ts.setSeatStatusId(1L);
		ts.setTixListTypeId(2L);
		ts.setGeneralAdmissionInd(false);
		ts.setRow("LOT");
		ts.setSection("LOT");
		ts.setSeatNumber ("Parking Pass");
		dbSeats.add(ts);

		{
			TicketSeat ts1 = new TicketSeat();
			ts1.setTicketId(124L);
			ts1.setSeatStatusId(1L);
			ts1.setTixListTypeId(1L);
			ts1.setGeneralAdmissionInd(true);
			ts1.setRow("GA");
			ts1.setSection("General Admission");
			ts1.setSeatNumber("1");
			dbSeats.add(ts1);
		}
		{
			TicketSeat ts1 = new TicketSeat();
			ts1.setTicketId(125L);
			ts1.setSeatStatusId(1L);
			ts1.setTixListTypeId(1L);
			ts1.setGeneralAdmissionInd(true);
			ts1.setRow("N/A");
			ts1.setSection("General Admission");
			ts1.setSeatNumber(null);
			dbSeats.add(ts1);
		}

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (Exception e) {
			Assert.fail("no exception", e);
		}
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
	}

	@Test
	public void processTicketProductTest_DeleteParkingPass() {

		Listing listing = new Listing();
		listing.setQuantityRemain(2);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setSystemStatus(ListingStatus.INACTIVE.name());
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>();

		SeatProduct seatProd = new SeatProduct();
		seatProd.setProductType(ProductType.PARKING_PASS);
		seatProd.setRow("LOT");
		seatProd.setSeat("Parking Pass");
		seatProd.setOperation(Operation.DELETE);
		seatProductList.add(seatProd);

		//long ticketId, long satusId, String section, String row, int count, boolean gaInd, boolean noSeatNum, String[] externalId
		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();
		{
			TicketSeat ts1 = new TicketSeat();
			ts1.setTicketId(124L);
			ts1.setSeatStatusId(1L);
			ts1.setTixListTypeId(1L);
			ts1.setGeneralAdmissionInd(true);
			ts1.setRow("GA");
			ts1.setSection("General Admission");
			ts1.setSeatNumber("1");
			dbSeats.add(ts1);
		}

		// case :
		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);

		Mockito.doReturn(true).when(seatProductsContext).getHasDelOperation();

		Mockito.when(seatProductsContext.getTicketSeatsFromCache()).thenReturn(dbSeats);

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (Exception e) {
			Assert.fail("no exception", e);
		}
		Mockito.verify(seatProductsContext, never()).setIsDBTicketsChanged();

		// case : can delete
		seatProductList.clear();
		seatProductList.add(seatProd);
		{
			TicketSeat ts = new TicketSeat();
			ts.setTicketId(123L);
			ts.setSeatStatusId(1L);
			ts.setTixListTypeId(2L);
			ts.setGeneralAdmissionInd(false);
			ts.setRow("LOT");
			ts.setSection("LOT");
			ts.setSeatNumber("Parking Pass");
			dbSeats.add(ts);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (Exception e) {
			Assert.fail("no exception", e);
		}
		Mockito.verify(seatProductsContext).setIsDBTicketsChanged();
	}

	@Test
	public void testFindParkingPassSeat() {
		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		{
			TicketSeat ts1 = new TicketSeat();
			ts1.setTicketId(124L);
			ts1.setSeatStatusId(1L);
			ts1.setTixListTypeId(1L);
			ts1.setGeneralAdmissionInd(true);
			ts1.setRow("GA");
			ts1.setSection("General Admission");
			ts1.setSeatNumber("1");
			dbSeats.add(ts1);
		}
		{
			TicketSeat ts1 = new TicketSeat();
			ts1.setTicketId(125L);
			ts1.setSeatStatusId(1L);
			ts1.setTixListTypeId(1L);
			ts1.setGeneralAdmissionInd(true);
			ts1.setRow("N/A");
			ts1.setSection("General Admission");
			ts1.setSeatNumber(null);
			dbSeats.add(ts1);
		}

		Assert.assertNull(SeatProductsManipulator.findParkingPassSeat(dbSeats));

		{
			TicketSeat ts = new TicketSeat();
			ts.setTicketId(123L);
			ts.setSeatStatusId(1L);
			ts.setTixListTypeId(2L);
			ts.setGeneralAdmissionInd(false);
			ts.setRow("LOT");
			ts.setSection("LOT");
			ts.setSeatNumber("Parking Pass");
			dbSeats.add(ts);
		}

		Assert.assertNotNull(SeatProductsManipulator.findParkingPassSeat(dbSeats));

	}
	//SELLAPI-1011 06/25/15 END

	//SELLAPI-956 10/26/15 START
	@Test
	public void processTicketProductTest10() {

		Listing listing = new Listing();
		listing.setQuantityRemain(2);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setSystemStatus(ListingStatus.INCOMPLETE.name());
		listing.setTicketMedium(TicketMedium.PDF.getValue());
		listing.setSection("General Admission");

		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>();
		//getSeatProduct(ProductType prodType, String ffArtifact, String row,
		//String seat, long seatId, Operation operation, String externalId )
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, null, "3", 123L, Operation.ADD, "1"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "N/A", "4", 124L, Operation.ADD, "2"));
		//getTicketSeats(long ticketId, long satusId, String section, String row,
		//		int count, boolean gaInd, boolean noSeatNum, String[] externalId )
		List<TicketSeat> dbSeats = getTicketSeats(123L, 1L, "General Admission", "15", 2, true, true, new String[] {"1","2"});

		Mockito.doReturn(true).when(seatProductsContext).isCreate();
		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(new ArrayList<TicketSeat>()).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
		}
		Assert.assertTrue(true);
	}

	@Test
	public void processTicketProductTest11() {

		Listing listing = new Listing();
		listing.setQuantityRemain(2);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setSystemStatus(ListingStatus.INCOMPLETE.name());
		listing.setTicketMedium(TicketMedium.PDF.getValue());
		listing.setSection("Non General Admission");
		Event event = new Event();
		event.setGaIndicator(false);
		listing.setEvent(event);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>();
		//getSeatProduct(ProductType prodType, String ffArtifact, String row,
		//String seat, long seatId, Operation operation, String externalId )
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, null, null, 123L, Operation.ADD, "1"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "14A", null, 124L, Operation.ADD, "2"));
		//getTicketSeats(long ticketId, long satusId, String section, String row,
		//		int count, boolean gaInd, boolean noSeatNum, String[] externalId )
		List<TicketSeat> dbSeats = getTicketSeats(123L, 1L, "General Admission", "15", 2, true, true, new String[] {"1","2"});

		Mockito.doReturn(true).when(seatProductsContext).isCreate();
		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(new ArrayList<TicketSeat>()).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
			
			listing.getEvent().setGaIndicator(true);
			seatProductList.add(getSeatProduct(ProductType.TICKET, null, null, null, 123L, Operation.ADD, "1"));
	        seatProductList.add(getSeatProduct(ProductType.TICKET, null, "14A", null, 124L, Operation.ADD, "2"));
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
		}
		Assert.assertTrue(true);
	}
	//SELLAPI-956 10/26/15 END

	@Test
	public void processTicketProductsTestDeleteProducts() throws Exception {
		Listing listing = new Listing();
		listing.setId(12345L);
		listing.setSystemStatus("ACTIVE");
		listing.setTicketMedium(TicketMedium.BARCODE.getValue());
		listing.setDeliveryOption(com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.PREDELIVERY.getValue());
		listing.setQuantityRemain(1);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);
		List<TicketSeat> dbSeats = getTicketSeats(123L, 1L, "General Admission", "15", 2, true, false, new String[] {"1","2"});
		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>();
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "15", "0", 123L, Operation.DELETE, "1"));

		Mockito.doReturn(false).when(seatProductsContext).getHasUpdOperation();
		Mockito.doReturn(true).when(seatProductsContext).getHasDelOperation();
		Mockito.doReturn(false).when(seatProductsContext).isCreate();
		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);

		SeatProductsManipulator.processTicketProducts (listing, seatProductsContext, listingRequest);
		Assert.assertEquals(seatProductsContext.getCurrentListing().getSystemStatus(), ListingStatus.PENDING_LOCK.toString());
	}

	@Test
	public void processTicketProductsTestDeleteProductsFlash() throws Exception {
		Listing listing = new Listing();
		listing.setId(12345L);
		listing.setSystemStatus("ACTIVE");
		listing.setTicketMedium(TicketMedium.FLASHSEAT.getValue());
		listing.setDeliveryOption(com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.PREDELIVERY.getValue());
		listing.setQuantityRemain(1);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);
		List<TicketSeat> dbSeats = getTicketSeats(123L, 1L, "General Admission", "15", 2, true, false, new String[] {"1","2"});
		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>();
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "15", "0", 123L, Operation.DELETE, "1"));

		Mockito.doReturn(false).when(seatProductsContext).getHasUpdOperation();
		Mockito.doReturn(true).when(seatProductsContext).getHasDelOperation();
		Mockito.doReturn(false).when(seatProductsContext).isCreate();
		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);

		SeatProductsManipulator.processTicketProducts (listing, seatProductsContext, listingRequest);
		Assert.assertEquals(seatProductsContext.getCurrentListing().getSystemStatus(), ListingStatus.PENDING_LOCK.toString());
	}

	@Test(expectedExceptions=ListingBusinessException.class)
	public void processTicketProductsTestDeleteProductsError() throws Exception {
		Listing listing = new Listing();
		listing.setId(12345L);
		listing.setSystemStatus("ACTIVE");
		listing.setTicketMedium(TicketMedium.BARCODE.getValue());
		listing.setDeliveryOption(com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.PREDELIVERY.getValue());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);
		List<TicketSeat> dbSeats = getTicketSeats(123L, 1L, "General Admission", "15", 2, true, false, new String[] {"1","2"});
		dbSeats.get(0).setSeatStatusId(4L);
		dbSeats.get(1).setSeatStatusId(4L);
		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>();
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "15", "0", 123L, Operation.DELETE, "1"));

		Mockito.doReturn(false).when(seatProductsContext).getHasUpdOperation();
		Mockito.doReturn(true).when(seatProductsContext).getHasDelOperation();
		Mockito.doReturn(false).when(seatProductsContext).isCreate();
		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);

		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);

		SeatProductsManipulator.processTicketProducts (listing, seatProductsContext, listingRequest);
	}

	@Test(expectedExceptions=ListingBusinessException.class)
	public void processTicketProductsTestDeleteAllProducts() throws Exception {
		Listing listing = new Listing();
		listing.setId(12345L);
		listing.setSystemStatus("ACTIVE");
		listing.setTicketMedium(TicketMedium.PDF.getValue());
		listing.setDeliveryOption(com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.MANUAL.getValue());
		listing.setQuantityRemain(0);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);
		List<TicketSeat> dbSeats = getTicketSeats(123L, 1L, "General Admission", "15", 2, true, false, new String[] {"1","2"});
		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>();
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "15", "0", 123L, Operation.DELETE, "1"));

		Mockito.doReturn(false).when(seatProductsContext).getHasAddOperation();
		Mockito.doReturn(false).when(seatProductsContext).getHasUpdOperation();
		Mockito.doReturn(true).when(seatProductsContext).getHasDelOperation();
		Mockito.doReturn(false).when(seatProductsContext).isCreate();
		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);

		SeatProductsManipulator.processTicketProducts (listing, seatProductsContext, listingRequest);
	}

	@Test(expectedExceptions=ListingBusinessException.class)
	public void processTicketProductsTestDeleteAllProductsWithAdd() throws Exception {
		Listing listing = new Listing();
		listing.setId(12345L);
		listing.setSystemStatus("ACTIVE");
		listing.setTicketMedium(TicketMedium.PDF.getValue());
		listing.setDeliveryOption(com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.PREDELIVERY.getValue());
		listing.setQuantityRemain(0);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);
		List<TicketSeat> dbSeats = getTicketSeats(123L, 1L, "General Admission", "15", 2, true, false, new String[] {"1","2"});
		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>();
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "15", "0", 123L, Operation.DELETE, "1"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "15", "0", 123L, Operation.ADD, "1"));

		Mockito.doReturn(true).when(seatProductsContext).getHasAddOperation();
		Mockito.doReturn(false).when(seatProductsContext).getHasUpdOperation();
		Mockito.doReturn(true).when(seatProductsContext).getHasDelOperation();
		Mockito.doReturn(false).when(seatProductsContext).isCreate();
		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);

		SeatProductsManipulator.processTicketProducts (listing, seatProductsContext, listingRequest);
	}

	@Test(expectedExceptions=ListingBusinessException.class)
	public void processTicketProductsTestAddProductsToPredeliveredListing() throws Exception {
		Listing listing = new Listing();
		listing.setId(12345L);
		listing.setSystemStatus("ACTIVE");
		listing.setTicketMedium(TicketMedium.PDF.getValue());
		listing.setDeliveryOption(com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.PREDELIVERY.getValue());
		listing.setQuantityRemain(1);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);
		List<TicketSeat> dbSeats = getTicketSeats(123L, 1L, "General Admission", "15", 2, true, false, new String[] {"1","2"});
		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>();
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "15", "0", 123L, Operation.ADD, "1"));

		Mockito.doReturn(true).when(seatProductsContext).getHasAddOperation();
		Mockito.doReturn(false).when(seatProductsContext).getHasUpdOperation();
		Mockito.doReturn(false).when(seatProductsContext).getHasDelOperation();
		Mockito.doReturn(false).when(seatProductsContext).isCreate();
		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);

		SeatProductsManipulator.processTicketProducts (listing, seatProductsContext, listingRequest);
	}

	//ExternalId: Same but not in order
	//Row: Different
	//SeatNumber: Same
	@Test
	public void processTicketProductTest2502_1_0() {
		Listing listing = new Listing();
		listing.setQuantityRemain(8);
		listing.setId(1243709189L);
		listing.setAdjustPrice(true);
		listing.setSellerPayoutAmountPerTicket(new Money("95", "USD", 4));
		listing.setSection("GA2");
		listing.setSplitQuantity(1);
		listing.setSplitOption((short)2);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setExternalId("465532");
		listing.setSystemStatus(ListingStatus.ACTIVE.name());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(8);
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA", "58", 2968838812L, Operation.UPDATE, "2877719"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA", "57", 2968838813L, Operation.UPDATE, "2877718"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA", "56", 2968838814L, Operation.UPDATE, "2877717"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA", "55", 2968838815L, Operation.UPDATE, "2877716"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA", "54", 2968838816L, Operation.UPDATE, "2877715"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA", "53", 2968838817L, Operation.UPDATE, "2877714"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA", "52", 2968838818L, Operation.UPDATE, "2877713"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA", "51", 2968838819L, Operation.UPDATE, "2877712"));


		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("2877712");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId (1243709189L);
		ts1.setRow("GA1");
		ts1.setSeatNumber ("51");
		ts1.setSeatStatusId(1L);
		ts1.setSection("GA2");
		ts1.setTicketSeatId(2968838812L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("2877713");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId (1243709189L);
		ts2.setRow("GA1");
		ts2.setSeatNumber ("52");
		ts2.setSeatStatusId(1L);
		ts2.setSection("GA2");
		ts2.setTicketSeatId(2968838813L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("2877714");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId (1243709189L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("GA1");
		ts3.setSection("GA2");
		ts3.setSeatNumber ("53");
		ts3.setTicketSeatId(2968838814L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("2877715");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId (1243709189L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("GA1");
		ts4.setSection("GA2");
		ts4.setSeatNumber ("54");
		ts4.setTicketSeatId(2968838815L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("2877716");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId (1243709189L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("GA1");
		ts5.setSection("GA2");
		ts5.setSeatNumber ("55");
		ts5.setTicketSeatId(2968838816L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("2877717");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId (1243709189L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("GA1");
		ts6.setSection("GA2");
		ts6.setSeatNumber ("56");
		ts6.setTicketSeatId(2968838817L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("2877718");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId (1243709189L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("GA1");
		ts7.setSection("GA2");
		ts7.setSeatNumber ("57");
		ts7.setTicketSeatId(2968838818L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("2877719");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId (1243709189L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("GA1");
		ts8.setSection("GA2");
		ts8.setSeatNumber ("58");
		ts8.setTicketSeatId(2968838819L);
		dbSeats.add(ts8);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Same
	//Row: Same
	//SeatNumber: Same
	@Test
	public void processTicketProductTest2502_1_1() {
		Listing listing = new Listing();
		listing.setQuantityRemain(8);
		listing.setId(1243709189L);
		listing.setAdjustPrice(true);
		listing.setSellerPayoutAmountPerTicket(new Money("95", "USD", 4));
		listing.setSection("GA2");
		listing.setSplitQuantity(1);
		listing.setSplitOption((short)2);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setExternalId("465532");
		listing.setSystemStatus(ListingStatus.ACTIVE.name());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(8);
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "51", 2968838812L, Operation.UPDATE, "2877712"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "52", 2968838813L, Operation.UPDATE, "2877713"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "53", 2968838814L, Operation.UPDATE, "2877714"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "54", 2968838815L, Operation.UPDATE, "2877715"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "55", 2968838816L, Operation.UPDATE, "2877716"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "56", 2968838817L, Operation.UPDATE, "2877717"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "57", 2968838818L, Operation.UPDATE, "2877718"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "58", 2968838819L, Operation.UPDATE, "2877719"));


		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("2877712");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId (1243709189L);
		ts1.setRow("GA1");
		ts1.setSeatNumber ("51");
		ts1.setSeatStatusId(1L);
		ts1.setSection("GA2");
		ts1.setTicketSeatId(2968838812L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("2877713");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId (1243709189L);
		ts2.setRow("GA1");
		ts2.setSeatNumber ("52");
		ts2.setSeatStatusId(1L);
		ts2.setSection("GA2");
		ts2.setTicketSeatId(2968838813L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("2877714");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId (1243709189L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("GA1");
		ts3.setSection("GA2");
		ts3.setSeatNumber ("53");
		ts3.setTicketSeatId(2968838814L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("2877715");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId (1243709189L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("GA1");
		ts4.setSection("GA2");
		ts4.setSeatNumber ("54");
		ts4.setTicketSeatId(2968838815L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("2877716");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId (1243709189L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("GA1");
		ts5.setSection("GA2");
		ts5.setSeatNumber ("55");
		ts5.setTicketSeatId(2968838816L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("2877717");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId (1243709189L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("GA1");
		ts6.setSection("GA2");
		ts6.setSeatNumber ("56");
		ts6.setTicketSeatId(2968838817L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("2877718");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId (1243709189L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("GA1");
		ts7.setSection("GA2");
		ts7.setSeatNumber ("57");
		ts7.setTicketSeatId(2968838818L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("2877719");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId (1243709189L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("GA1");
		ts8.setSection("GA2");
		ts8.setSeatNumber ("58");
		ts8.setTicketSeatId(2968838819L);
		dbSeats.add(ts8);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Different
	//Row: Same
	//SeatNumber: Same
	@Test
	public void processTicketProductTest2502_1_2() {
		Listing listing = new Listing();
		listing.setQuantityRemain(8);
		listing.setId(1243709189L);
		listing.setAdjustPrice(true);
		listing.setSellerPayoutAmountPerTicket(new Money("95", "USD", 4));
		listing.setSection("GA2");
		listing.setSplitQuantity(1);
		listing.setSplitOption((short)2);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setExternalId("465532");
		listing.setSystemStatus(ListingStatus.ACTIVE.name());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(8);
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "51", 2968838812L, Operation.UPDATE, "2877722"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "52", 2968838813L, Operation.UPDATE, "2877723"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "53", 2968838814L, Operation.UPDATE, "2877724"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "54", 2968838815L, Operation.UPDATE, "2877725"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "55", 2968838816L, Operation.UPDATE, "2877726"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "56", 2968838817L, Operation.UPDATE, "2877727"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "57", 2968838818L, Operation.UPDATE, "2877728"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "58", 2968838819L, Operation.UPDATE, "2877729"));


		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("2877712");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId (1243709189L);
		ts1.setRow("GA1");
		ts1.setSeatNumber ("51");
		ts1.setSeatStatusId(1L);
		ts1.setSection("GA2");
		ts1.setTicketSeatId(2968838812L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("2877713");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId (1243709189L);
		ts2.setRow("GA1");
		ts2.setSeatNumber ("52");
		ts2.setSeatStatusId(1L);
		ts2.setSection("GA2");
		ts2.setTicketSeatId(2968838813L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("2877714");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId (1243709189L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("GA1");
		ts3.setSection("GA2");
		ts3.setSeatNumber ("53");
		ts3.setTicketSeatId(2968838814L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("2877715");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId (1243709189L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("GA1");
		ts4.setSection("GA2");
		ts4.setSeatNumber ("54");
		ts4.setTicketSeatId(2968838815L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("2877716");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId (1243709189L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("GA1");
		ts5.setSection("GA2");
		ts5.setSeatNumber ("55");
		ts5.setTicketSeatId(2968838816L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("2877717");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId (1243709189L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("GA1");
		ts6.setSection("GA2");
		ts6.setSeatNumber ("56");
		ts6.setTicketSeatId(2968838817L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("2877718");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId (1243709189L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("GA1");
		ts7.setSection("GA2");
		ts7.setSeatNumber ("57");
		ts7.setTicketSeatId(2968838818L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("2877719");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId (1243709189L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("GA1");
		ts8.setSection("GA2");
		ts8.setSeatNumber ("58");
		ts8.setTicketSeatId(2968838819L);
		dbSeats.add(ts8);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: NULL to new value
	//Row: Same
	//SeatNumber: Same
	@Test
	public void processTicketProductTest2502_1_3() {
		Listing listing = new Listing();
		listing.setQuantityRemain(8);
		listing.setId(1243709189L);
		listing.setAdjustPrice(true);
		listing.setSellerPayoutAmountPerTicket(new Money("95", "USD", 4));
		listing.setSection("GA2");
		listing.setSplitQuantity(1);
		listing.setSplitOption((short)2);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setExternalId("465532");
		listing.setSystemStatus(ListingStatus.ACTIVE.name());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(8);
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "51", 2968838812L, Operation.UPDATE, "2877712"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "52", 2968838813L, Operation.UPDATE, "2877713"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "53", 2968838814L, Operation.UPDATE, "2877714"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "54", 2968838815L, Operation.UPDATE, "2877715"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "55", 2968838816L, Operation.UPDATE, "2877716"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "56", 2968838817L, Operation.UPDATE, "2877717"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "57", 2968838818L, Operation.UPDATE, "2877718"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "58", 2968838819L, Operation.UPDATE, "2877719"));


		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId (1243709189L);
		ts1.setRow("GA1");
		ts1.setSeatNumber ("51");
		ts1.setSeatStatusId(1L);
		ts1.setSection("GA2");
		ts1.setTicketSeatId(2968838812L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId(null);
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId (1243709189L);
		ts2.setRow("GA1");
		ts2.setSeatNumber ("52");
		ts2.setSeatStatusId(1L);
		ts2.setSection("GA2");
		ts2.setTicketSeatId(2968838813L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId(null);
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId (1243709189L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("GA1");
		ts3.setSection("GA2");
		ts3.setSeatNumber ("53");
		ts3.setTicketSeatId(2968838814L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId(null);
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId (1243709189L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("GA1");
		ts4.setSection("GA2");
		ts4.setSeatNumber ("54");
		ts4.setTicketSeatId(2968838815L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId(null);
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId (1243709189L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("GA1");
		ts5.setSection("GA2");
		ts5.setSeatNumber ("55");
		ts5.setTicketSeatId(2968838816L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId(null);
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId (1243709189L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("GA1");
		ts6.setSection("GA2");
		ts6.setSeatNumber ("56");
		ts6.setTicketSeatId(2968838817L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId(null);
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId (1243709189L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("GA1");
		ts7.setSection("GA2");
		ts7.setSeatNumber ("57");
		ts7.setTicketSeatId(2968838818L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId(null);
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId (1243709189L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("GA1");
		ts8.setSection("GA2");
		ts8.setSeatNumber ("58");
		ts8.setTicketSeatId(2968838819L);
		dbSeats.add(ts8);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: existing value to NULL
	//Row: Same
	//SeatNumber: Same
	@Test
	public void processTicketProductTest2502_1_4() {
		Listing listing = new Listing();
		listing.setQuantityRemain(8);
		listing.setId(1243709189L);
		listing.setAdjustPrice(true);
		listing.setSellerPayoutAmountPerTicket(new Money("95", "USD", 4));
		listing.setSection("GA2");
		listing.setSplitQuantity(1);
		listing.setSplitOption((short)2);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setExternalId("465532");
		listing.setSystemStatus(ListingStatus.ACTIVE.name());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(8);
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "51", 2968838812L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "52", 2968838813L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "53", 2968838814L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "54", 2968838815L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "55", 2968838816L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "56", 2968838817L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "57", 2968838818L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "58", 2968838819L, Operation.UPDATE, null));


		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("2877712");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId (1243709189L);
		ts1.setRow("GA1");
		ts1.setSeatNumber ("51");
		ts1.setSeatStatusId(1L);
		ts1.setSection("GA2");
		ts1.setTicketSeatId(2968838812L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("2877713");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId (1243709189L);
		ts2.setRow("GA1");
		ts2.setSeatNumber ("52");
		ts2.setSeatStatusId(1L);
		ts2.setSection("GA2");
		ts2.setTicketSeatId(2968838813L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("2877714");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId (1243709189L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("GA1");
		ts3.setSection("GA2");
		ts3.setSeatNumber ("53");
		ts3.setTicketSeatId(2968838814L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("2877715");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId (1243709189L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("GA1");
		ts4.setSection("GA2");
		ts4.setSeatNumber ("54");
		ts4.setTicketSeatId(2968838815L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("2877716");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId (1243709189L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("GA1");
		ts5.setSection("GA2");
		ts5.setSeatNumber ("55");
		ts5.setTicketSeatId(2968838816L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("2877717");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId (1243709189L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("GA1");
		ts6.setSection("GA2");
		ts6.setSeatNumber ("56");
		ts6.setTicketSeatId(2968838817L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("2877718");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId (1243709189L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("GA1");
		ts7.setSection("GA2");
		ts7.setSeatNumber ("57");
		ts7.setTicketSeatId(2968838818L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("2877719");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId (1243709189L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("GA1");
		ts8.setSection("GA2");
		ts8.setSeatNumber ("58");
		ts8.setTicketSeatId(2968838819L);
		dbSeats.add(ts8);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: from NULL to NULL
	//Row: Same
	//SeatNumber: Same
	@Test
	public void processTicketProductTest2502_1_5() {
		Listing listing = new Listing();
		listing.setQuantityRemain(8);
		listing.setId(1243709189L);
		listing.setAdjustPrice(true);
		listing.setSellerPayoutAmountPerTicket(new Money("95", "USD", 4));
		listing.setSection("GA2");
		listing.setSplitQuantity(1);
		listing.setSplitOption((short)2);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setExternalId("465532");
		listing.setSystemStatus(ListingStatus.ACTIVE.name());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(8);
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "51", 2968838812L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "52", 2968838813L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "53", 2968838814L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "54", 2968838815L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "55", 2968838816L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "56", 2968838817L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "57", 2968838818L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "58", 2968838819L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId (1243709189L);
		ts1.setRow("GA1");
		ts1.setSeatNumber ("51");
		ts1.setSeatStatusId(1L);
		ts1.setSection("GA2");
		ts1.setTicketSeatId(2968838812L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId(null);
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId (1243709189L);
		ts2.setRow("GA1");
		ts2.setSeatNumber ("52");
		ts2.setSeatStatusId(1L);
		ts2.setSection("GA2");
		ts2.setTicketSeatId(2968838813L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId(null);
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId (1243709189L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("GA1");
		ts3.setSection("GA2");
		ts3.setSeatNumber ("53");
		ts3.setTicketSeatId(2968838814L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId(null);
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId (1243709189L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("GA1");
		ts4.setSection("GA2");
		ts4.setSeatNumber ("54");
		ts4.setTicketSeatId(2968838815L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId(null);
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId (1243709189L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("GA1");
		ts5.setSection("GA2");
		ts5.setSeatNumber ("55");
		ts5.setTicketSeatId(2968838816L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId(null);
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId (1243709189L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("GA1");
		ts6.setSection("GA2");
		ts6.setSeatNumber ("56");
		ts6.setTicketSeatId(2968838817L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId(null);
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId (1243709189L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("GA1");
		ts7.setSection("GA2");
		ts7.setSeatNumber ("57");
		ts7.setTicketSeatId(2968838818L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId(null);
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId (1243709189L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("GA1");
		ts8.setSection("GA2");
		ts8.setSeatNumber ("58");
		ts8.setTicketSeatId(2968838819L);
		dbSeats.add(ts8);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Same
	//Row: Same
	//SeatNumber: Different
	@Test
	public void processTicketProductTest2502_1_6() {
		Listing listing = new Listing();
		listing.setQuantityRemain(8);
		listing.setId(1243709189L);
		listing.setAdjustPrice(true);
		listing.setSellerPayoutAmountPerTicket(new Money("95", "USD", 4));
		listing.setSection("GA2");
		listing.setSplitQuantity(1);
		listing.setSplitOption((short)2);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setExternalId("465532");
		listing.setSystemStatus(ListingStatus.ACTIVE.name());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(8);
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "61", 2968838812L, Operation.UPDATE, "2877712"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "62", 2968838813L, Operation.UPDATE, "2877713"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "63", 2968838814L, Operation.UPDATE, "2877714"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "64", 2968838815L, Operation.UPDATE, "2877715"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "65", 2968838816L, Operation.UPDATE, "2877716"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "66", 2968838817L, Operation.UPDATE, "2877717"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "67", 2968838818L, Operation.UPDATE, "2877718"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "68", 2968838819L, Operation.UPDATE, "2877719"));


		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("2877712");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId (1243709189L);
		ts1.setRow("GA1");
		ts1.setSeatNumber ("51");
		ts1.setSeatStatusId(1L);
		ts1.setSection("GA2");
		ts1.setTicketSeatId(2968838812L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("2877713");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId (1243709189L);
		ts2.setRow("GA1");
		ts2.setSeatNumber ("52");
		ts2.setSeatStatusId(1L);
		ts2.setSection("GA2");
		ts2.setTicketSeatId(2968838813L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("2877714");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId (1243709189L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("GA1");
		ts3.setSection("GA2");
		ts3.setSeatNumber ("53");
		ts3.setTicketSeatId(2968838814L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("2877715");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId (1243709189L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("GA1");
		ts4.setSection("GA2");
		ts4.setSeatNumber ("54");
		ts4.setTicketSeatId(2968838815L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("2877716");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId (1243709189L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("GA1");
		ts5.setSection("GA2");
		ts5.setSeatNumber ("55");
		ts5.setTicketSeatId(2968838816L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("2877717");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId (1243709189L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("GA1");
		ts6.setSection("GA2");
		ts6.setSeatNumber ("56");
		ts6.setTicketSeatId(2968838817L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("2877718");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId (1243709189L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("GA1");
		ts7.setSection("GA2");
		ts7.setSeatNumber ("57");
		ts7.setTicketSeatId(2968838818L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("2877719");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId(1243709189L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("GA1");
		ts8.setSection("GA2");
		ts8.setSeatNumber ("58");
		ts8.setTicketSeatId(2968838819L);
		dbSeats.add(ts8);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Same
	//Row: Same
	//SeatNumber: NULL to a value
	@Test
	public void processTicketProductTest2502_1_7() {
		Listing listing = new Listing();
		listing.setQuantityRemain(8);
		listing.setId(1243709189L);
		listing.setAdjustPrice(true);
		listing.setSellerPayoutAmountPerTicket(new Money("95", "USD", 4));
		listing.setSection("GA2");
		listing.setSplitQuantity(1);
		listing.setSplitOption((short)2);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setExternalId("465532");
		listing.setSystemStatus(ListingStatus.ACTIVE.name());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(8);
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "61", 2968838812L, Operation.UPDATE, "2877712"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "62", 2968838813L, Operation.UPDATE, "2877713"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "63", 2968838814L, Operation.UPDATE, "2877714"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "64", 2968838815L, Operation.UPDATE, "2877715"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "65", 2968838816L, Operation.UPDATE, "2877716"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "66", 2968838817L, Operation.UPDATE, "2877717"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "67", 2968838818L, Operation.UPDATE, "2877718"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "68", 2968838819L, Operation.UPDATE, "2877719"));


		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("2877712");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId (1243709189L);
		ts1.setRow("GA1");
		ts1.setSeatNumber(null);
		ts1.setSeatStatusId(1L);
		ts1.setSection("GA2");
		ts1.setTicketSeatId(2968838812L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("2877713");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId (1243709189L);
		ts2.setRow("GA1");
		ts2.setSeatNumber(null);
		ts2.setSeatStatusId(1L);
		ts2.setSection("GA2");
		ts2.setTicketSeatId(2968838813L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("2877714");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId (1243709189L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("GA1");
		ts3.setSection("GA2");
		ts3.setSeatNumber(null);
		ts3.setTicketSeatId(2968838814L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("2877715");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId (1243709189L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("GA1");
		ts4.setSection("GA2");
		ts4.setSeatNumber(null);
		ts4.setTicketSeatId(2968838815L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("2877716");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId (1243709189L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("GA1");
		ts5.setSection("GA2");
		ts5.setSeatNumber(null);
		ts5.setTicketSeatId(2968838816L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("2877717");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId (1243709189L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("GA1");
		ts6.setSection("GA2");
		ts6.setSeatNumber(null);
		ts6.setTicketSeatId(2968838817L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("2877718");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId (1243709189L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("GA1");
		ts7.setSection("GA2");
		ts7.setSeatNumber(null);
		ts7.setTicketSeatId(2968838818L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("2877719");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId (1243709189L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("GA1");
		ts8.setSection("GA2");
		ts8.setSeatNumber(null);
		ts8.setTicketSeatId(2968838819L);
		dbSeats.add(ts8);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Same
	//Row: Same
	//SeatNumber: existing value to NULL
	@Test
	public void processTicketProductTest2502_1_8() {
		Listing listing = new Listing();
		listing.setQuantityRemain(8);
		listing.setId(1243709189L);
		listing.setAdjustPrice(true);
		listing.setSellerPayoutAmountPerTicket(new Money("95", "USD", 4));
		listing.setSection("GA2");
		listing.setSplitQuantity(1);
		listing.setSplitOption((short)2);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setExternalId("465532");
		listing.setSystemStatus(ListingStatus.ACTIVE.name());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(8);
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", null, 2968838812L, Operation.UPDATE, "2877712"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", null, 2968838813L, Operation.UPDATE, "2877713"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", null, 2968838814L, Operation.UPDATE, "2877714"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", null, 2968838815L, Operation.UPDATE, "2877715"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", null, 2968838816L, Operation.UPDATE, "2877716"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", null, 2968838817L, Operation.UPDATE, "2877717"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", null, 2968838818L, Operation.UPDATE, "2877718"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", null, 2968838819L, Operation.UPDATE, "2877719"));


		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("2877712");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId (1243709189L);
		ts1.setRow("GA1");
		ts1.setSeatNumber ("51");
		ts1.setSeatStatusId(1L);
		ts1.setSection("GA2");
		ts1.setTicketSeatId(2968838812L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("2877713");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId (1243709189L);
		ts2.setRow("GA1");
		ts2.setSeatNumber ("52");
		ts2.setSeatStatusId(1L);
		ts2.setSection("GA2");
		ts2.setTicketSeatId(2968838813L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("2877714");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId (1243709189L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("GA1");
		ts3.setSection("GA2");
		ts3.setSeatNumber ("53");
		ts3.setTicketSeatId(2968838814L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("2877715");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId (1243709189L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("GA1");
		ts4.setSection("GA2");
		ts4.setSeatNumber ("54");
		ts4.setTicketSeatId(2968838815L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("2877716");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId (1243709189L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("GA1");
		ts5.setSection("GA2");
		ts5.setSeatNumber ("55");
		ts5.setTicketSeatId(2968838816L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("2877717");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId (1243709189L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("GA1");
		ts6.setSection("GA2");
		ts6.setSeatNumber ("56");
		ts6.setTicketSeatId(2968838817L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("2877718");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId (1243709189L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("GA1");
		ts7.setSection("GA2");
		ts7.setSeatNumber ("57");
		ts7.setTicketSeatId(2968838818L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("2877719");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId (1243709189L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("GA1");
		ts8.setSection("GA2");
		ts8.setSeatNumber ("58");
		ts8.setTicketSeatId(2968838819L);
		dbSeats.add(ts8);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Same
	//Row: Same
	//SeatNumber: NULL to NULL
	@Test
	public void processTicketProductTest2502_1_9() {
		Listing listing = new Listing();
		listing.setQuantityRemain(8);
		listing.setId(1243709189L);
		listing.setAdjustPrice(true);
		listing.setSellerPayoutAmountPerTicket(new Money("95", "USD", 4));
		listing.setSection("GA2");
		listing.setSplitQuantity(1);
		listing.setSplitOption((short)2);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setExternalId("465532");
		listing.setSystemStatus(ListingStatus.ACTIVE.name());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(8);
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", null, 2968838812L, Operation.UPDATE, "2877712"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", null, 2968838813L, Operation.UPDATE, "2877713"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", null, 2968838814L, Operation.UPDATE, "2877714"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", null, 2968838815L, Operation.UPDATE, "2877715"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", null, 2968838816L, Operation.UPDATE, "2877716"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", null, 2968838817L, Operation.UPDATE, "2877717"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", null, 2968838818L, Operation.UPDATE, "2877718"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", null, 2968838819L, Operation.UPDATE, "2877719"));


		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("2877712");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId(1243709189L);
		ts1.setRow("GA1");
		ts1.setSeatNumber(null);
		ts1.setSeatStatusId(1L);
		ts1.setSection("GA2");
		ts1.setTicketSeatId(2968838812L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("2877713");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId(1243709189L);
		ts2.setRow("GA1");
		ts2.setSeatNumber(null);
		ts2.setSeatStatusId(1L);
		ts2.setSection("GA2");
		ts2.setTicketSeatId(2968838813L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("2877714");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId(1243709189L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("GA1");
		ts3.setSection("GA2");
		ts3.setSeatNumber(null);
		ts3.setTicketSeatId(2968838814L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("2877715");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId(1243709189L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("GA1");
		ts4.setSection("GA2");
		ts4.setSeatNumber(null);
		ts4.setTicketSeatId(2968838815L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("2877716");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId(1243709189L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("GA1");
		ts5.setSection("GA2");
		ts5.setSeatNumber(null);
		ts5.setTicketSeatId(2968838816L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("2877717");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId(1243709189L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("GA1");
		ts6.setSection("GA2");
		ts6.setSeatNumber(null);
		ts6.setTicketSeatId(2968838817L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("2877718");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId(1243709189L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("GA1");
		ts7.setSection("GA2");
		ts7.setSeatNumber(null);
		ts7.setTicketSeatId(2968838818L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("2877719");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId(1243709189L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("GA1");
		ts8.setSection("GA2");
		ts8.setSeatNumber(null);
		ts8.setTicketSeatId(2968838819L);
		dbSeats.add(ts8);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Different
	//Row: Same
	//SeatNumber: Different
	@Test
	public void processTicketProductTest2502_1_10() {
		Listing listing = new Listing();
		listing.setQuantityRemain(8);
		listing.setId(1243709189L);
		listing.setAdjustPrice(true);
		listing.setSellerPayoutAmountPerTicket(new Money("95", "USD", 4));
		listing.setSection("GA2");
		listing.setSplitQuantity(1);
		listing.setSplitOption((short)2);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setExternalId("465532");
		listing.setSystemStatus(ListingStatus.ACTIVE.name());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(8);
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "61", 2968838812L, Operation.UPDATE, "2877722"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "62", 2968838813L, Operation.UPDATE, "2877723"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "63", 2968838814L, Operation.UPDATE, "2877724"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "64", 2968838815L, Operation.UPDATE, "2877725"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "65", 2968838816L, Operation.UPDATE, "2877726"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "66", 2968838817L, Operation.UPDATE, "2877727"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "67", 2968838818L, Operation.UPDATE, "2877728"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA1", "68", 2968838819L, Operation.UPDATE, "2877729"));


		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("2877712");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId (1243709189L);
		ts1.setRow("GA1");
		ts1.setSeatNumber ("51");
		ts1.setSeatStatusId(1L);
		ts1.setSection("GA2");
		ts1.setTicketSeatId(2968838812L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("2877713");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId (1243709189L);
		ts2.setRow("GA1");
		ts2.setSeatNumber ("52");
		ts2.setSeatStatusId(1L);
		ts2.setSection("GA2");
		ts2.setTicketSeatId(2968838813L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("2877714");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId (1243709189L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("GA1");
		ts3.setSection("GA2");
		ts3.setSeatNumber ("53");
		ts3.setTicketSeatId(2968838814L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("2877715");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId (1243709189L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("GA1");
		ts4.setSection("GA2");
		ts4.setSeatNumber ("54");
		ts4.setTicketSeatId(2968838815L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("2877716");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId (1243709189L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("GA1");
		ts5.setSection("GA2");
		ts5.setSeatNumber ("55");
		ts5.setTicketSeatId(2968838816L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("2877717");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId (1243709189L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("GA1");
		ts6.setSection("GA2");
		ts6.setSeatNumber ("56");
		ts6.setTicketSeatId(2968838817L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("2877718");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId (1243709189L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("GA1");
		ts7.setSection("GA2");
		ts7.setSeatNumber ("57");
		ts7.setTicketSeatId(2968838818L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("2877719");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId (1243709189L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("GA1");
		ts8.setSection("GA2");
		ts8.setSeatNumber ("58");
		ts8.setTicketSeatId(2968838819L);
		dbSeats.add(ts8);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Different
	//Row: Different
	//SeatNumber: Different
	@Test(expectedExceptions={ListingBusinessException.class})
	public void processTicketProductTest2502_1_11() {
		Listing listing = new Listing();
		listing.setQuantityRemain(8);
		listing.setId(1243709189L);
		listing.setAdjustPrice(true);
		listing.setSellerPayoutAmountPerTicket(new Money("95", "USD", 4));
		listing.setSection("GA2");
		listing.setSplitQuantity(1);
		listing.setSplitOption((short)2);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setExternalId("465532");
		listing.setSystemStatus(ListingStatus.ACTIVE.name());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(8);
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA11", "61", 2968838812L, Operation.UPDATE, "2877722"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA12", "62", 2968838813L, Operation.UPDATE, "2877723"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA13", "63", 2968838814L, Operation.UPDATE, "2877724"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA14", "64", 2968838815L, Operation.UPDATE, "2877725"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA15", "65", 2968838816L, Operation.UPDATE, "2877726"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA16", "66", 2968838817L, Operation.UPDATE, "2877727"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA17", "67", 2968838818L, Operation.UPDATE, "2877728"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA18", "68", 2968838819L, Operation.UPDATE, "2877729"));


		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("2877712");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId (1243709189L);
		ts1.setRow("GA1");
		ts1.setSeatNumber ("51");
		ts1.setSeatStatusId(1L);
		ts1.setSection("GA2");
		ts1.setTicketSeatId(2968838812L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("2877713");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId (1243709189L);
		ts2.setRow("GA1");
		ts2.setSeatNumber ("52");
		ts2.setSeatStatusId(1L);
		ts2.setSection("GA2");
		ts2.setTicketSeatId(2968838813L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("2877714");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId (1243709189L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("GA1");
		ts3.setSection("GA2");
		ts3.setSeatNumber ("53");
		ts3.setTicketSeatId(2968838814L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("2877715");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId (1243709189L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("GA1");
		ts4.setSection("GA2");
		ts4.setSeatNumber ("54");
		ts4.setTicketSeatId(2968838815L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("2877716");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId (1243709189L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("GA1");
		ts5.setSection("GA2");
		ts5.setSeatNumber ("55");
		ts5.setTicketSeatId(2968838816L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("2877717");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId (1243709189L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("GA1");
		ts6.setSection("GA2");
		ts6.setSeatNumber ("56");
		ts6.setTicketSeatId(2968838817L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("2877718");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId (1243709189L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("GA1");
		ts7.setSection("GA2");
		ts7.setSeatNumber ("57");
		ts7.setTicketSeatId(2968838818L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("2877719");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId (1243709189L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("GA1");
		ts8.setSection("GA2");
		ts8.setSeatNumber ("58");
		ts8.setTicketSeatId(2968838819L);
		dbSeats.add(ts8);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		} catch (ListingBusinessException e) {
			Assert.assertEquals("Cannot locate seat product to update: (row:GA11, seat:61)", e.getMessage());
			throw e;
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
	}

	//ExternalId: Same
	//Row: Different
	//SeatNumber: Different
	@Test
	public void processTicketProductTest2502_1_12() {
		Listing listing = new Listing();
		listing.setQuantityRemain(8);
		listing.setId(1243709189L);
		listing.setAdjustPrice(true);
		listing.setSellerPayoutAmountPerTicket(new Money("95", "USD", 4));
		listing.setSection("GA2");
		listing.setSplitQuantity(1);
		listing.setSplitOption((short)2);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setExternalId("465532");
		listing.setSystemStatus(ListingStatus.ACTIVE.name());
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(8);
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA11", "61", 2968838812L, Operation.UPDATE, "2877712"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA12", "62", 2968838813L, Operation.UPDATE, "2877713"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA13", "63", 2968838814L, Operation.UPDATE, "2877714"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA14", "64", 2968838815L, Operation.UPDATE, "2877715"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA15", "65", 2968838816L, Operation.UPDATE, "2877716"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA16", "66", 2968838817L, Operation.UPDATE, "2877717"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA17", "67", 2968838818L, Operation.UPDATE, "2877718"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, null, "GA18", "68", 2968838819L, Operation.UPDATE, "2877719"));


		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("2877712");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId (1243709189L);
		ts1.setRow("GA1");
		ts1.setSeatNumber ("51");
		ts1.setSeatStatusId(1L);
		ts1.setSection("GA2");
		ts1.setTicketSeatId(2968838812L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("2877713");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId (1243709189L);
		ts2.setRow("GA1");
		ts2.setSeatNumber ("52");
		ts2.setSeatStatusId(1L);
		ts2.setSection("GA2");
		ts2.setTicketSeatId(2968838813L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("2877714");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId (1243709189L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("GA1");
		ts3.setSection("GA2");
		ts3.setSeatNumber ("53");
		ts3.setTicketSeatId(2968838814L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("2877715");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId (1243709189L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("GA1");
		ts4.setSection("GA2");
		ts4.setSeatNumber ("54");
		ts4.setTicketSeatId(2968838815L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("2877716");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId (1243709189L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("GA1");
		ts5.setSection("GA2");
		ts5.setSeatNumber ("55");
		ts5.setTicketSeatId(2968838816L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("2877717");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId (1243709189L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("GA1");
		ts6.setSection("GA2");
		ts6.setSeatNumber ("56");
		ts6.setTicketSeatId(2968838817L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("2877718");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId (1243709189L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("GA1");
		ts7.setSection("GA2");
		ts7.setSeatNumber ("57");
		ts7.setTicketSeatId(2968838818L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("2877719");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId (1243709189L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("GA1");
		ts8.setSection("GA2");
		ts8.setSeatNumber ("58");
		ts8.setTicketSeatId(2968838819L);
		dbSeats.add(ts8);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Different
	//Row: Same but not in order
	//SeatNumber: Same
	@Test
	public void processTicketProductTest2502_2_0() {
		Listing listing = new Listing();
		listing.setQuantityRemain(10);
		listing.setId(1246680055L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(10);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-GBW28GR7", "ADM0", "121", 29844742061L, Operation.UPDATE, "287700011"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-XAH63AS6", "ADM1", "122", 2984474207L, Operation.UPDATE, "287700012"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-64ZDF6WX", "ADM2", "123", 2984474208L, Operation.UPDATE, "287700013"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-75NXE5HG", "ADM3", "124", 2984474209L, Operation.UPDATE, "287700014"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-98TXD9VM", "ADM4", "125", 2984474210L, Operation.UPDATE, "287700015"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-23PDM3UD", "ADM5", "126", 2984474211L, Operation.UPDATE, "287700016"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-DFV69DL2", "ADM6", "127", 2984474212L, Operation.UPDATE, "287700017"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-MEU22EK9", "ADM7", "128", 2984474213L, Operation.UPDATE, "287700018"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-9KTXX2VM", "ADM8", "129", 2984474214L, Operation.UPDATE, "287700019"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-2LPDG8UD", "ADM9", "130", 2984474215L, Operation.UPDATE, "287700020"));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("287700002");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId(1246680055L);
		ts1.setRow("ADM1");
		ts1.setSeatNumber("122");
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN3");
		ts1.setTicketSeatId(29844742061L);
		ts1.setTixListTypeId(1L);
		ts1.setFulfillmentArtifactId(null);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("287700003");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId(1246680055L);
		ts2.setRow("ADM2");
		ts2.setSeatNumber("123");
		ts2.setSeatStatusId(1L);
		ts2.setSection("LAWN3");
		ts2.setTicketSeatId(2984474207L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("287700004");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId(1246680055L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("ADM3");
		ts3.setSection("LAWN3");
		ts3.setSeatNumber("124");
		ts3.setTicketSeatId(2984474208L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("287700005");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId(1246680055L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("ADM4");
		ts4.setSection("LAWN3");
		ts4.setSeatNumber("125");
		ts4.setTicketSeatId(2984474209L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("287700006");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId(1246680055L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("ADM5");
		ts5.setSection("LAWN3");
		ts5.setSeatNumber("126");
		ts5.setTicketSeatId(2984474210L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("287700007");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId(1246680055L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("ADM6");
		ts6.setSection("LAWN3");
		ts6.setSeatNumber("127");
		ts6.setTicketSeatId(2984474211L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("287700008");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId(1246680055L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("ADM7");
		ts7.setSection("LAWN3");
		ts7.setSeatNumber("128");
		ts7.setTicketSeatId(2984474212L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("287700009");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId(1246680055L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("ADM8");
		ts8.setSection("LAWN3");
		ts8.setSeatNumber("129");
		ts8.setTicketSeatId(2984474213L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId("287700010");
		ts9.setGeneralAdmissionInd(true);
		ts9.setTicketId(1246680055L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow("ADM9");
		ts9.setSection("LAWN3");
		ts9.setSeatNumber("130");
		ts9.setTicketSeatId(2984474214L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId("287700001");
		ts10.setGeneralAdmissionInd(true);
		ts10.setTicketId(1246680055L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow("ADM0");
		ts10.setSection("LAWN3");
		ts10.setSeatNumber("121");
		ts10.setTicketSeatId(2984474215L);
		dbSeats.add(ts10);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Same
	//Row: Same
	//SeatNumber: Same
	@Test
	public void processTicketProductTest2502_2_1() {
		Listing listing = new Listing();
		listing.setQuantityRemain(10);
		listing.setId(1246680055L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(10);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-GBW28GR7", "ADM", "121", 2984474206L, Operation.UPDATE, "287700001"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-XAH63AS6", "ADM", "122", 2984474207L, Operation.UPDATE, "287700002"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-64ZDF6WX", "ADM", "123", 2984474208L, Operation.UPDATE, "287700003"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-75NXE5HG", "ADM", "124", 2984474209L, Operation.UPDATE, "287700004"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-98TXD9VM", "ADM", "125", 2984474210L, Operation.UPDATE, "287700005"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-23PDM3UD", "ADM", "126", 2984474211L, Operation.UPDATE, "287700006"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-DFV69DL2", "ADM", "127", 2984474212L, Operation.UPDATE, "287700007"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-MEU22EK9", "ADM", "128", 2984474213L, Operation.UPDATE, "287700008"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-9KTXX2VM", "ADM", "129", 2984474214L, Operation.UPDATE, "287700009"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-2LPDG8UD", "ADM", "130", 2984474215L, Operation.UPDATE, "287700010"));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("287700001");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId(1246680055L);
		ts1.setRow("ADM");
		ts1.setSeatNumber("121");
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN3");
		ts1.setTicketSeatId(2984474206L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("287700002");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId(1246680055L);
		ts2.setRow("ADM");
		ts2.setSeatNumber("122");
		ts2.setSeatStatusId(1L);
		ts2.setSection("LAWN3");
		ts2.setTicketSeatId(2984474207L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("287700003");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId(1246680055L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("ADM");
		ts3.setSection("LAWN3");
		ts3.setSeatNumber("123");
		ts3.setTicketSeatId(2984474208L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("287700004");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId(1246680055L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("ADM");
		ts4.setSection("LAWN3");
		ts4.setSeatNumber("124");
		ts4.setTicketSeatId(2984474209L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("287700005");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId(1246680055L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("ADM");
		ts5.setSection("LAWN3");
		ts5.setSeatNumber("125");
		ts5.setTicketSeatId(2984474210L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("287700006");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId(1246680055L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("ADM");
		ts6.setSection("LAWN3");
		ts6.setSeatNumber("126");
		ts6.setTicketSeatId(2984474211L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("287700007");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId(1246680055L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("ADM");
		ts7.setSection("LAWN3");
		ts7.setSeatNumber("127");
		ts7.setTicketSeatId(2984474212L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("287700008");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId(1246680055L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("ADM");
		ts8.setSection("LAWN3");
		ts8.setSeatNumber("128");
		ts8.setTicketSeatId(2984474213L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId("287700009");
		ts9.setGeneralAdmissionInd(true);
		ts9.setTicketId(1246680055L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow("ADM");
		ts9.setSection("LAWN3");
		ts9.setSeatNumber("129");
		ts9.setTicketSeatId(2984474214L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId("287700010");
		ts10.setGeneralAdmissionInd(true);
		ts10.setTicketId(1246680055L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow("ADM");
		ts10.setSection("LAWN3");
		ts10.setSeatNumber("130");
		ts10.setTicketSeatId(2984474215L);
		dbSeats.add(ts10);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Different
	//Row: Same
	//SeatNumber: Same
	@Test
	public void processTicketProductTest2502_2_2() {
		Listing listing = new Listing();
		listing.setQuantityRemain(10);
		listing.setId(1246680055L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(10);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-GBW28GR7", "ADM", "121", 2984474206L, Operation.UPDATE, "2877000011"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-XAH63AS6", "ADM", "122", 2984474207L, Operation.UPDATE, "287700012"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-64ZDF6WX", "ADM", "123", 2984474208L, Operation.UPDATE, "287700013"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-75NXE5HG", "ADM", "124", 2984474209L, Operation.UPDATE, "287700014"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-98TXD9VM", "ADM", "125", 2984474210L, Operation.UPDATE, "287700015"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-23PDM3UD", "ADM", "126", 2984474211L, Operation.UPDATE, "287700016"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-DFV69DL2", "ADM", "127", 2984474212L, Operation.UPDATE, "287700017"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-MEU22EK9", "ADM", "128", 2984474213L, Operation.UPDATE, "287700018"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-9KTXX2VM", "ADM", "129", 2984474214L, Operation.UPDATE, "287700019"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-2LPDG8UD", "ADM", "130", 2984474215L, Operation.UPDATE, "287700020"));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("287700001");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId(1246680055L);
		ts1.setRow("ADM");
		ts1.setSeatNumber("121");
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN3");
		ts1.setTicketSeatId(2984474206L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("287700002");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId(1246680055L);
		ts2.setRow("ADM");
		ts2.setSeatNumber("122");
		ts2.setSeatStatusId(1L);
		ts2.setSection("LAWN3");
		ts2.setTicketSeatId(2984474207L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("287700003");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId(1246680055L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("ADM");
		ts3.setSection("LAWN3");
		ts3.setSeatNumber("123");
		ts3.setTicketSeatId(2984474208L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("287700004");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId(1246680055L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("ADM");
		ts4.setSection("LAWN3");
		ts4.setSeatNumber("124");
		ts4.setTicketSeatId(2984474209L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("287700005");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId(1246680055L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("ADM");
		ts5.setSection("LAWN3");
		ts5.setSeatNumber("125");
		ts5.setTicketSeatId(2984474210L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("287700006");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId(1246680055L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("ADM");
		ts6.setSection("LAWN3");
		ts6.setSeatNumber("126");
		ts6.setTicketSeatId(2984474211L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("287700007");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId(1246680055L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("ADM");
		ts7.setSection("LAWN3");
		ts7.setSeatNumber("127");
		ts7.setTicketSeatId(2984474212L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("287700008");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId(1246680055L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("ADM");
		ts8.setSection("LAWN3");
		ts8.setSeatNumber("128");
		ts8.setTicketSeatId(2984474213L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId("287700009");
		ts9.setGeneralAdmissionInd(true);
		ts9.setTicketId(1246680055L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow("ADM");
		ts9.setSection("LAWN3");
		ts9.setSeatNumber("129");
		ts9.setTicketSeatId(2984474214L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId("287700010");
		ts10.setGeneralAdmissionInd(true);
		ts10.setTicketId(1246680055L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow("ADM");
		ts10.setSection("LAWN3");
		ts10.setSeatNumber("130");
		ts10.setTicketSeatId(2984474215L);
		dbSeats.add(ts10);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: NULL to new value
	//Row: Same
	//SeatNumber: Same
	@Test
	public void processTicketProductTest2502_2_3() {
		Listing listing = new Listing();
		listing.setQuantityRemain(10);
		listing.setId(1246680055L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(10);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-GBW28GR7", "ADM", "121", 2984474206L, Operation.UPDATE, "287700001"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-XAH63AS6", "ADM", "122", 2984474207L, Operation.UPDATE, "287700002"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-64ZDF6WX", "ADM", "123", 2984474208L, Operation.UPDATE, "287700003"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-75NXE5HG", "ADM", "124", 2984474209L, Operation.UPDATE, "287700004"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-98TXD9VM", "ADM", "125", 2984474210L, Operation.UPDATE, "287700005"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-23PDM3UD", "ADM", "126", 2984474211L, Operation.UPDATE, "287700006"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-DFV69DL2", "ADM", "127", 2984474212L, Operation.UPDATE, "287700007"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-MEU22EK9", "ADM", "128", 2984474213L, Operation.UPDATE, "287700008"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-9KTXX2VM", "ADM", "129", 2984474214L, Operation.UPDATE, "287700009"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-2LPDG8UD", "ADM", "130", 2984474215L, Operation.UPDATE, "287700010"));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId(1246680055L);
		ts1.setRow("ADM");
		ts1.setSeatNumber("121");
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN3");
		ts1.setTicketSeatId(2984474206L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId(null);
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId(1246680055L);
		ts2.setRow("ADM");
		ts2.setSeatNumber("122");
		ts2.setSeatStatusId(1L);
		ts2.setSection("LAWN3");
		ts2.setTicketSeatId(2984474207L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId(null);
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId(1246680055L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("ADM");
		ts3.setSection("LAWN3");
		ts3.setSeatNumber("123");
		ts3.setTicketSeatId(2984474208L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId(null);
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId(1246680055L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("ADM");
		ts4.setSection("LAWN3");
		ts4.setSeatNumber("124");
		ts4.setTicketSeatId(2984474209L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId(null);
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId(1246680055L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("ADM");
		ts5.setSection("LAWN3");
		ts5.setSeatNumber("125");
		ts5.setTicketSeatId(2984474210L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId(null);
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId(1246680055L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("ADM");
		ts6.setSection("LAWN3");
		ts6.setSeatNumber("126");
		ts6.setTicketSeatId(2984474211L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId(null);
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId(1246680055L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("ADM");
		ts7.setSection("LAWN3");
		ts7.setSeatNumber("127");
		ts7.setTicketSeatId(2984474212L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId(null);
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId(1246680055L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("ADM");
		ts8.setSection("LAWN3");
		ts8.setSeatNumber("128");
		ts8.setTicketSeatId(2984474213L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId(null);
		ts9.setGeneralAdmissionInd(true);
		ts9.setTicketId(1246680055L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow("ADM");
		ts9.setSection("LAWN3");
		ts9.setSeatNumber("129");
		ts9.setTicketSeatId(2984474214L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId(null);
		ts10.setGeneralAdmissionInd(true);
		ts10.setTicketId(1246680055L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow("ADM");
		ts10.setSection("LAWN3");
		ts10.setSeatNumber("130");
		ts10.setTicketSeatId(2984474215L);
		dbSeats.add(ts10);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Existing value to NULL
	//Row: Same
	//SeatNumber: Same
	@Test
	public void processTicketProductTest2502_2_4() {
		Listing listing = new Listing();
		listing.setQuantityRemain(10);
		listing.setId(1246680055L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(10);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-GBW28GR7", "ADM", "121", 2984474206L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-XAH63AS6", "ADM", "122", 2984474207L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-64ZDF6WX", "ADM", "123", 2984474208L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-75NXE5HG", "ADM", "124", 2984474209L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-98TXD9VM", "ADM", "125", 2984474210L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-23PDM3UD", "ADM", "126", 2984474211L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-DFV69DL2", "ADM", "127", 2984474212L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-MEU22EK9", "ADM", "128", 2984474213L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-9KTXX2VM", "ADM", "129", 2984474214L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-2LPDG8UD", "ADM", "130", 2984474215L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("287700001");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId(1246680055L);
		ts1.setRow("ADM");
		ts1.setSeatNumber("121");
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN3");
		ts1.setTicketSeatId(2984474206L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("287700002");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId(1246680055L);
		ts2.setRow("ADM");
		ts2.setSeatNumber("122");
		ts2.setSeatStatusId(1L);
		ts2.setSection("LAWN3");
		ts2.setTicketSeatId(2984474207L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("287700003");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId(1246680055L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("ADM");
		ts3.setSection("LAWN3");
		ts3.setSeatNumber("123");
		ts3.setTicketSeatId(2984474208L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("287700004");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId(1246680055L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("ADM");
		ts4.setSection("LAWN3");
		ts4.setSeatNumber("124");
		ts4.setTicketSeatId(2984474209L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("287700005");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId(1246680055L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("ADM");
		ts5.setSection("LAWN3");
		ts5.setSeatNumber("125");
		ts5.setTicketSeatId(2984474210L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("287700006");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId(1246680055L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("ADM");
		ts6.setSection("LAWN3");
		ts6.setSeatNumber("126");
		ts6.setTicketSeatId(2984474211L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("287700007");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId(1246680055L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("ADM");
		ts7.setSection("LAWN3");
		ts7.setSeatNumber("127");
		ts7.setTicketSeatId(2984474212L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("287700008");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId(1246680055L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("ADM");
		ts8.setSection("LAWN3");
		ts8.setSeatNumber("128");
		ts8.setTicketSeatId(2984474213L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId("287700009");
		ts9.setGeneralAdmissionInd(true);
		ts9.setTicketId(1246680055L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow("ADM");
		ts9.setSection("LAWN3");
		ts9.setSeatNumber("129");
		ts9.setTicketSeatId(2984474214L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId("287700010");
		ts10.setGeneralAdmissionInd(true);
		ts10.setTicketId(1246680055L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow("ADM");
		ts10.setSection("LAWN3");
		ts10.setSeatNumber("130");
		ts10.setTicketSeatId(2984474215L);
		dbSeats.add(ts10);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: NULL to NULL
	//Row: Same
	//SeatNumber: Same
	@Test
	public void processTicketProductTest2502_2_5() {
		Listing listing = new Listing();
		listing.setQuantityRemain(10);
		listing.setId(1246680055L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(10);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-GBW28GR7", "ADM", "121", 2984474206L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-XAH63AS6", "ADM", "122", 2984474207L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-64ZDF6WX", "ADM", "123", 2984474208L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-75NXE5HG", "ADM", "124", 2984474209L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-98TXD9VM", "ADM", "125", 2984474210L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-23PDM3UD", "ADM", "126", 2984474211L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-DFV69DL2", "ADM", "127", 2984474212L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-MEU22EK9", "ADM", "128", 2984474213L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-9KTXX2VM", "ADM", "129", 2984474214L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-2LPDG8UD", "ADM", "130", 2984474215L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId(1246680055L);
		ts1.setRow("ADM");
		ts1.setSeatNumber("121");
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN3");
		ts1.setTicketSeatId(2984474206L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId(null);
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId(1246680055L);
		ts2.setRow("ADM");
		ts2.setSeatNumber("122");
		ts2.setSeatStatusId(1L);
		ts2.setSection("LAWN3");
		ts2.setTicketSeatId(2984474207L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId(null);
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId(1246680055L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("ADM");
		ts3.setSection("LAWN3");
		ts3.setSeatNumber("123");
		ts3.setTicketSeatId(2984474208L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId(null);
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId(1246680055L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("ADM");
		ts4.setSection("LAWN3");
		ts4.setSeatNumber("124");
		ts4.setTicketSeatId(2984474209L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId(null);
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId(1246680055L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("ADM");
		ts5.setSection("LAWN3");
		ts5.setSeatNumber("125");
		ts5.setTicketSeatId(2984474210L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId(null);
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId(1246680055L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("ADM");
		ts6.setSection("LAWN3");
		ts6.setSeatNumber("126");
		ts6.setTicketSeatId(2984474211L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId(null);
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId(1246680055L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("ADM");
		ts7.setSection("LAWN3");
		ts7.setSeatNumber("127");
		ts7.setTicketSeatId(2984474212L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId(null);
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId(1246680055L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("ADM");
		ts8.setSection("LAWN3");
		ts8.setSeatNumber("128");
		ts8.setTicketSeatId(2984474213L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId(null);
		ts9.setGeneralAdmissionInd(true);
		ts9.setTicketId(1246680055L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow("ADM");
		ts9.setSection("LAWN3");
		ts9.setSeatNumber("129");
		ts9.setTicketSeatId(2984474214L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId(null);
		ts10.setGeneralAdmissionInd(true);
		ts10.setTicketId(1246680055L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow("ADM");
		ts10.setSection("LAWN3");
		ts10.setSeatNumber("130");
		ts10.setTicketSeatId(2984474215L);
		dbSeats.add(ts10);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Same
	//Row: Same
	//SeatNumber: Different
	@Test
	public void processTicketProductTest2502_2_6() {
		Listing listing = new Listing();
		listing.setQuantityRemain(10);
		listing.setId(1246680055L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(10);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-GBW28GR7", "ADM", "131", 2984474206L, Operation.UPDATE, "287700001"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-XAH63AS6", "ADM", "132", 2984474207L, Operation.UPDATE, "287700002"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-64ZDF6WX", "ADM", "133", 2984474208L, Operation.UPDATE, "287700003"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-75NXE5HG", "ADM", "134", 2984474209L, Operation.UPDATE, "287700004"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-98TXD9VM", "ADM", "135", 2984474210L, Operation.UPDATE, "287700005"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-23PDM3UD", "ADM", "136", 2984474211L, Operation.UPDATE, "287700006"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-DFV69DL2", "ADM", "137", 2984474212L, Operation.UPDATE, "287700007"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-MEU22EK9", "ADM", "138", 2984474213L, Operation.UPDATE, "287700008"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-9KTXX2VM", "ADM", "139", 2984474214L, Operation.UPDATE, "287700009"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-2LPDG8UD", "ADM", "140", 2984474215L, Operation.UPDATE, "287700010"));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("287700001");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId(1246680055L);
		ts1.setRow("ADM");
		ts1.setSeatNumber("121");
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN3");
		ts1.setTicketSeatId(2984474206L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("287700002");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId(1246680055L);
		ts2.setRow("ADM");
		ts2.setSeatNumber("122");
		ts2.setSeatStatusId(1L);
		ts2.setSection("LAWN3");
		ts2.setTicketSeatId(2984474207L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("287700003");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId(1246680055L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("ADM");
		ts3.setSection("LAWN3");
		ts3.setSeatNumber("123");
		ts3.setTicketSeatId(2984474208L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("287700004");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId(1246680055L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("ADM");
		ts4.setSection("LAWN3");
		ts4.setSeatNumber("124");
		ts4.setTicketSeatId(2984474209L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("287700005");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId(1246680055L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("ADM");
		ts5.setSection("LAWN3");
		ts5.setSeatNumber("125");
		ts5.setTicketSeatId(2984474210L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("287700006");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId(1246680055L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("ADM");
		ts6.setSection("LAWN3");
		ts6.setSeatNumber("126");
		ts6.setTicketSeatId(2984474211L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("287700007");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId(1246680055L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("ADM");
		ts7.setSection("LAWN3");
		ts7.setSeatNumber("127");
		ts7.setTicketSeatId(2984474212L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("287700008");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId(1246680055L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("ADM");
		ts8.setSection("LAWN3");
		ts8.setSeatNumber("128");
		ts8.setTicketSeatId(2984474213L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId("287700009");
		ts9.setGeneralAdmissionInd(true);
		ts9.setTicketId(1246680055L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow("ADM");
		ts9.setSection("LAWN3");
		ts9.setSeatNumber("129");
		ts9.setTicketSeatId(2984474214L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId("287700010");
		ts10.setGeneralAdmissionInd(true);
		ts10.setTicketId(1246680055L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow("ADM");
		ts10.setSection("LAWN3");
		ts10.setSeatNumber("130");
		ts10.setTicketSeatId(2984474215L);
		dbSeats.add(ts10);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Same
	//Row: Same
	//SeatNumber: NULL to a value
	@Test
	public void processTicketProductTest2502_2_7() {
		Listing listing = new Listing();
		listing.setQuantityRemain(10);
		listing.setId(1246680055L);
		listing.setSection("LAWN3");
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		listing.setExternalId("465532");
		listing.setSystemStatus(ListingStatus.ACTIVE.name());
		listing.setComments("ADD BARCODES TO A LISTING");
		listing.setAdjustPrice(false);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(10);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-GBW28GR7", "ADM", "121", 2984474206L, Operation.UPDATE, "287700001"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-XAH63AS6", "ADM", "122", 2984474207L, Operation.UPDATE, "287700002"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-64ZDF6WX", "ADM", "123", 2984474208L, Operation.UPDATE, "287700003"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-75NXE5HG", "ADM", "124", 2984474209L, Operation.UPDATE, "287700004"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-98TXD9VM", "ADM", "125", 2984474210L, Operation.UPDATE, "287700005"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-23PDM3UD", "ADM", "126", 2984474211L, Operation.UPDATE, "287700006"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-DFV69DL2", "ADM", "127", 2984474212L, Operation.UPDATE, "287700007"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-MEU22EK9", "ADM", "128", 2984474213L, Operation.UPDATE, "287700008"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-9KTXX2VM", "ADM", "129", 2984474214L, Operation.UPDATE, "287700009"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-2LPDG8UD", "ADM", "130", 2984474215L, Operation.UPDATE, "287700010"));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("287700001");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId(1246680055L);
		ts1.setRow("ADM");
		ts1.setSeatNumber(null);
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN3");
		ts1.setTicketSeatId(2984474206L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("287700002");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId(1246680055L);
		ts2.setRow("ADM");
		ts2.setSeatNumber(null);
		ts2.setSeatStatusId(1L);
		ts2.setSection("LAWN3");
		ts2.setTicketSeatId(2984474207L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("287700003");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId(1246680055L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("ADM");
		ts3.setSection("LAWN3");
		ts3.setSeatNumber(null);
		ts3.setTicketSeatId(2984474208L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("287700004");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId(1246680055L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("ADM");
		ts4.setSection("LAWN3");
		ts4.setSeatNumber(null);
		ts4.setTicketSeatId(2984474209L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("287700005");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId(1246680055L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("ADM");
		ts5.setSection("LAWN3");
		ts5.setSeatNumber(null);
		ts5.setTicketSeatId(2984474210L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("287700006");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId(1246680055L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("ADM");
		ts6.setSection("LAWN3");
		ts6.setSeatNumber(null);
		ts6.setTicketSeatId(2984474211L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("287700007");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId(1246680055L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("ADM");
		ts7.setSection("LAWN3");
		ts7.setSeatNumber(null);
		ts7.setTicketSeatId(2984474212L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("287700008");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId(1246680055L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("ADM");
		ts8.setSection("LAWN3");
		ts8.setSeatNumber("128");
		ts8.setTicketSeatId(2984474213L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId("287700009");
		ts9.setGeneralAdmissionInd(true);
		ts9.setTicketId(1246680055L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow("ADM");
		ts9.setSection("LAWN3");
		ts9.setSeatNumber(null);
		ts9.setTicketSeatId(2984474214L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId("287700010");
		ts10.setGeneralAdmissionInd(true);
		ts10.setTicketId(1246680055L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow("ADM");
		ts10.setSection("LAWN3");
		ts10.setSeatNumber(null);
		ts10.setTicketSeatId(2984474215L);
		dbSeats.add(ts10);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Same
	//Row: Same
	//SeatNumber: existing value to NULL
	@Test
	public void processTicketProductTest2502_2_8() {
		Listing listing = new Listing();
		listing.setQuantityRemain(10);
		listing.setId(1246680055L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(10);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-GBW28GR7", "ADM", null, 2984474206L, Operation.UPDATE, "287700001"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-XAH63AS6", "ADM", null, 2984474207L, Operation.UPDATE, "287700002"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-64ZDF6WX", "ADM", null, 2984474208L, Operation.UPDATE, "287700003"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-75NXE5HG", "ADM", null, 2984474209L, Operation.UPDATE, "287700004"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-98TXD9VM", "ADM", null, 2984474210L, Operation.UPDATE, "287700005"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-23PDM3UD", "ADM", null, 2984474211L, Operation.UPDATE, "287700006"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-DFV69DL2", "ADM", null, 2984474212L, Operation.UPDATE, "287700007"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-MEU22EK9", "ADM", null, 2984474213L, Operation.UPDATE, "287700008"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-9KTXX2VM", "ADM", null, 2984474214L, Operation.UPDATE, "287700009"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-2LPDG8UD", "ADM", null, 2984474215L, Operation.UPDATE, "287700010"));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("287700001");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId(1246680055L);
		ts1.setRow("ADM");
		ts1.setSeatNumber("121");
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN3");
		ts1.setTicketSeatId(2984474206L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("287700002");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId(1246680055L);
		ts2.setRow("ADM");
		ts2.setSeatNumber("122");
		ts2.setSeatStatusId(1L);
		ts2.setSection("LAWN3");
		ts2.setTicketSeatId(2984474207L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("287700003");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId(1246680055L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("ADM");
		ts3.setSection("LAWN3");
		ts3.setSeatNumber("123");
		ts3.setTicketSeatId(2984474208L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("287700004");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId(1246680055L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("ADM");
		ts4.setSection("LAWN3");
		ts4.setSeatNumber("124");
		ts4.setTicketSeatId(2984474209L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("287700005");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId(1246680055L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("ADM");
		ts5.setSection("LAWN3");
		ts5.setSeatNumber("125");
		ts5.setTicketSeatId(2984474210L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("287700006");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId(1246680055L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("ADM");
		ts6.setSection("LAWN3");
		ts6.setSeatNumber("126");
		ts6.setTicketSeatId(2984474211L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("287700007");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId(1246680055L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("ADM");
		ts7.setSection("LAWN3");
		ts7.setSeatNumber("127");
		ts7.setTicketSeatId(2984474212L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("287700008");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId(1246680055L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("ADM");
		ts8.setSection("LAWN3");
		ts8.setSeatNumber("128");
		ts8.setTicketSeatId(2984474213L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId("287700009");
		ts9.setGeneralAdmissionInd(true);
		ts9.setTicketId(1246680055L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow("ADM");
		ts9.setSection("LAWN3");
		ts9.setSeatNumber("129");
		ts9.setTicketSeatId(2984474214L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId("287700010");
		ts10.setGeneralAdmissionInd(true);
		ts10.setTicketId(1246680055L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow("ADM");
		ts10.setSection("LAWN3");
		ts10.setSeatNumber("130");
		ts10.setTicketSeatId(2984474215L);
		dbSeats.add(ts10);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Same
	//Row: Same
	//SeatNumber: NULL to NULL
	@Test
	public void processTicketProductTest2502_2_9() {
		Listing listing = new Listing();
		listing.setQuantityRemain(10);
		listing.setId(1246680055L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(10);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-GBW28GR7", "ADM", null, 2984474206L, Operation.UPDATE, "287700001"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-XAH63AS6", "ADM", null, 2984474207L, Operation.UPDATE, "287700002"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-64ZDF6WX", "ADM", null, 2984474208L, Operation.UPDATE, "287700003"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-75NXE5HG", "ADM", null, 2984474209L, Operation.UPDATE, "287700004"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-98TXD9VM", "ADM", null, 2984474210L, Operation.UPDATE, "287700005"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-23PDM3UD", "ADM", null, 2984474211L, Operation.UPDATE, "287700006"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-DFV69DL2", "ADM", null, 2984474212L, Operation.UPDATE, "287700007"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-MEU22EK9", "ADM", null, 2984474213L, Operation.UPDATE, "287700008"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-9KTXX2VM", "ADM", null, 2984474214L, Operation.UPDATE, "287700009"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-2LPDG8UD", "ADM", null, 2984474215L, Operation.UPDATE, "287700010"));
		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("287700001");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId(1246680055L);
		ts1.setRow("ADM");
		ts1.setSeatNumber(null);
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN3");
		ts1.setTicketSeatId(2984474206L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("287700002");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId(1246680055L);
		ts2.setRow("ADM");
		ts2.setSeatNumber(null);
		ts2.setSeatStatusId(1L);
		ts2.setSection("LAWN3");
		ts2.setTicketSeatId(2984474207L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("287700003");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId(1246680055L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("ADM");
		ts3.setSection("LAWN3");
		ts3.setSeatNumber(null);
		ts3.setTicketSeatId(2984474208L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("287700004");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId(1246680055L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("ADM");
		ts4.setSection("LAWN3");
		ts4.setSeatNumber(null);
		ts4.setTicketSeatId(2984474209L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("287700005");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId(1246680055L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("ADM");
		ts5.setSection("LAWN3");
		ts5.setSeatNumber(null);
		ts5.setTicketSeatId(2984474210L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("287700006");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId(1246680055L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("ADM");
		ts6.setSection("LAWN3");
		ts6.setSeatNumber(null);
		ts6.setTicketSeatId(2984474211L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("287700007");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId(1246680055L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("ADM");
		ts7.setSection("LAWN3");
		ts7.setSeatNumber(null);
		ts7.setTicketSeatId(2984474212L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("287700008");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId(1246680055L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("ADM");
		ts8.setSection("LAWN3");
		ts8.setSeatNumber("128");
		ts8.setTicketSeatId(2984474213L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId("287700009");
		ts9.setGeneralAdmissionInd(true);
		ts9.setTicketId(1246680055L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow("ADM");
		ts9.setSection("LAWN3");
		ts9.setSeatNumber(null);
		ts9.setTicketSeatId(2984474214L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId("287700010");
		ts10.setGeneralAdmissionInd(true);
		ts10.setTicketId(1246680055L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow("ADM");
		ts10.setSection("LAWN3");
		ts10.setSeatNumber(null);
		ts10.setTicketSeatId(2984474215L);
		dbSeats.add(ts10);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Different
	//Row: Different
	//SeatNumber: Same
	@Test(expectedExceptions={ListingBusinessException.class})
	public void processTicketProductTest2502_2_10() {
		Listing listing = new Listing();
		listing.setQuantityRemain(10);
		listing.setId(1246680055L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(10);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-GBW28GR7", "ADM1", "121", 2984474206L, Operation.UPDATE, "287700011"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-XAH63AS6", "ADM2", "122", 2984474207L, Operation.UPDATE, "287700012"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-64ZDF6WX", "ADM3", "123", 2984474208L, Operation.UPDATE, "287700013"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-75NXE5HG", "ADM4", "124", 2984474209L, Operation.UPDATE, "287700014"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-98TXD9VM", "ADM5", "125", 2984474210L, Operation.UPDATE, "287700015"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-23PDM3UD", "ADM6", "126", 2984474211L, Operation.UPDATE, "287700016"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-DFV69DL2", "ADM7", "127", 2984474212L, Operation.UPDATE, "287700017"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-MEU22EK9", "ADM8", "128", 2984474213L, Operation.UPDATE, "287700018"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-9KTXX2VM", "ADM9", "129", 2984474214L, Operation.UPDATE, "287700019"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-2LPDG8UD", "ADM10", "130", 2984474215L, Operation.UPDATE, "287700020"));


		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("287700001");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId(1246680055L);
		ts1.setRow("ADM");
		ts1.setSeatNumber("121");
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN3");
		ts1.setTicketSeatId(2984474206L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("287700002");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId(1246680055L);
		ts2.setRow("ADM");
		ts2.setSeatNumber("122");
		ts2.setSeatStatusId(1L);
		ts2.setSection("LAWN3");
		ts2.setTicketSeatId(2984474207L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("287700003");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId(1246680055L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("ADM");
		ts3.setSection("LAWN3");
		ts3.setSeatNumber("123");
		ts3.setTicketSeatId(2984474208L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("287700004");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId(1246680055L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("ADM");
		ts4.setSection("LAWN3");
		ts4.setSeatNumber("124");
		ts4.setTicketSeatId(2984474209L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("287700005");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId(1246680055L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("ADM");
		ts5.setSection("LAWN3");
		ts5.setSeatNumber("125");
		ts5.setTicketSeatId(2984474210L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("287700006");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId(1246680055L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("ADM");
		ts6.setSection("LAWN3");
		ts6.setSeatNumber("126");
		ts6.setTicketSeatId(2984474211L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("287700007");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId(1246680055L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("ADM");
		ts7.setSection("LAWN3");
		ts7.setSeatNumber("127");
		ts7.setTicketSeatId(2984474212L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("287700008");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId(1246680055L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("ADM");
		ts8.setSection("LAWN3");
		ts8.setSeatNumber("128");
		ts8.setTicketSeatId(2984474213L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId("287700009");
		ts9.setGeneralAdmissionInd(true);
		ts9.setTicketId(1246680055L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow("ADM");
		ts9.setSection("LAWN3");
		ts9.setSeatNumber("129");
		ts9.setTicketSeatId(2984474214L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId("287700010");
		ts10.setGeneralAdmissionInd(true);
		ts10.setTicketId(1246680055L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow("ADM");
		ts10.setSection("LAWN3");
		ts10.setSeatNumber("130");
		ts10.setTicketSeatId(2984474215L);
		dbSeats.add(ts10);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		} catch (ListingBusinessException e) {
			Assert.assertEquals("Cannot locate seat product to update: (row:ADM1, seat:121)", e.getMessage());
			throw e;
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
	}

	//ExternalId: Different
	//Row: Different
	//SeatNumber: Different
	@Test(expectedExceptions={ListingBusinessException.class})
	public void processTicketProductTest2502_2_11() {
		Listing listing = new Listing();
		listing.setQuantityRemain(10);
		listing.setId(1246680055L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(10);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-GBW28GR7", "ADM1", "131", 2984474206L, Operation.UPDATE, "287700011"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-XAH63AS6", "ADM2", "132", 2984474207L, Operation.UPDATE, "287700012"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-64ZDF6WX", "ADM3", "133", 2984474208L, Operation.UPDATE, "287700013"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-75NXE5HG", "ADM4", "134", 2984474209L, Operation.UPDATE, "287700014"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-98TXD9VM", "ADM5", "135", 2984474210L, Operation.UPDATE, "287700015"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-23PDM3UD", "ADM6", "136", 2984474211L, Operation.UPDATE, "287700016"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-DFV69DL2", "ADM7", "137", 2984474212L, Operation.UPDATE, "287700017"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-MEU22EK9", "ADM8", "138", 2984474213L, Operation.UPDATE, "287700018"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-9KTXX2VM", "ADM9", "139", 2984474214L, Operation.UPDATE, "287700019"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-2LPDG8UD", "ADM10", "140", 2984474215L, Operation.UPDATE, "287700020"));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("287700001");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId(1246680055L);
		ts1.setRow("ADM");
		ts1.setSeatNumber("121");
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN3");
		ts1.setTicketSeatId(2984474206L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("287700002");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId(1246680055L);
		ts2.setRow("ADM");
		ts2.setSeatNumber("122");
		ts2.setSeatStatusId(1L);
		ts2.setSection("LAWN3");
		ts2.setTicketSeatId(2984474207L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("287700003");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId(1246680055L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("ADM");
		ts3.setSection("LAWN3");
		ts3.setSeatNumber("123");
		ts3.setTicketSeatId(2984474208L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("287700004");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId(1246680055L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("ADM");
		ts4.setSection("LAWN3");
		ts4.setSeatNumber("124");
		ts4.setTicketSeatId(2984474209L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("287700005");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId(1246680055L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("ADM");
		ts5.setSection("LAWN3");
		ts5.setSeatNumber("125");
		ts5.setTicketSeatId(2984474210L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("287700006");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId(1246680055L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("ADM");
		ts6.setSection("LAWN3");
		ts6.setSeatNumber("126");
		ts6.setTicketSeatId(2984474211L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("287700007");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId(1246680055L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("ADM");
		ts7.setSection("LAWN3");
		ts7.setSeatNumber("127");
		ts7.setTicketSeatId(2984474212L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("287700008");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId(1246680055L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("ADM");
		ts8.setSection("LAWN3");
		ts8.setSeatNumber("128");
		ts8.setTicketSeatId(2984474213L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId("287700009");
		ts9.setGeneralAdmissionInd(true);
		ts9.setTicketId(1246680055L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow("ADM");
		ts9.setSection("LAWN3");
		ts9.setSeatNumber("129");
		ts9.setTicketSeatId(2984474214L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId("287700010");
		ts10.setGeneralAdmissionInd(true);
		ts10.setTicketId(1246680055L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow("ADM");
		ts10.setSection("LAWN3");
		ts10.setSeatNumber("130");
		ts10.setTicketSeatId(2984474215L);
		dbSeats.add(ts10);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		} catch (ListingBusinessException e) {
			Assert.assertEquals("Cannot locate seat product to update: (row:ADM1, seat:131)", e.getMessage());
			throw e;
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
	}

	//ExternalId: Same
	//Row: Different
	//SeatNumber: Different
	@Test
	public void processTicketProductTest2502_2_12() {
		Listing listing = new Listing();
		listing.setQuantityRemain(10);
		listing.setId(1246680055L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(10);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-GBW28GR7", "ADM1", "131", 2984474206L, Operation.UPDATE, "287700001"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-XAH63AS6", "ADM2", "132", 2984474207L, Operation.UPDATE, "287700002"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-64ZDF6WX", "ADM3", "133", 2984474208L, Operation.UPDATE, "287700003"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-75NXE5HG", "ADM4", "134", 2984474209L, Operation.UPDATE, "287700004"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-98TXD9VM", "ADM5", "135", 2984474210L, Operation.UPDATE, "287700005"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-23PDM3UD", "ADM6", "136", 2984474211L, Operation.UPDATE, "287700006"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-DFV69DL2", "ADM7", "137", 2984474212L, Operation.UPDATE, "287700007"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-MEU22EK9", "ADM8", "138", 2984474213L, Operation.UPDATE, "287700008"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-9KTXX2VM", "ADM9", "139", 2984474214L, Operation.UPDATE, "287700009"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-2LPDG8UD", "ADM10", "140", 2984474215L, Operation.UPDATE, "287700010"));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("287700001");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId(1246680055L);
		ts1.setRow("ADM");
		ts1.setSeatNumber("121");
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN3");
		ts1.setTicketSeatId(2984474206L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("287700002");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId(1246680055L);
		ts2.setRow("ADM");
		ts2.setSeatNumber("122");
		ts2.setSeatStatusId(1L);
		ts2.setSection("LAWN3");
		ts2.setTicketSeatId(2984474207L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("287700003");
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId(1246680055L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("ADM");
		ts3.setSection("LAWN3");
		ts3.setSeatNumber("123");
		ts3.setTicketSeatId(2984474208L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("287700004");
		ts4.setGeneralAdmissionInd(true);
		ts4.setTicketId(1246680055L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("ADM");
		ts4.setSection("LAWN3");
		ts4.setSeatNumber("124");
		ts4.setTicketSeatId(2984474209L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("287700005");
		ts5.setGeneralAdmissionInd(true);
		ts5.setTicketId(1246680055L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("ADM");
		ts5.setSection("LAWN3");
		ts5.setSeatNumber("125");
		ts5.setTicketSeatId(2984474210L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("287700006");
		ts6.setGeneralAdmissionInd(true);
		ts6.setTicketId(1246680055L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("ADM");
		ts6.setSection("LAWN3");
		ts6.setSeatNumber("126");
		ts6.setTicketSeatId(2984474211L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("287700007");
		ts7.setGeneralAdmissionInd(true);
		ts7.setTicketId(1246680055L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("ADM");
		ts7.setSection("LAWN3");
		ts7.setSeatNumber("127");
		ts7.setTicketSeatId(2984474212L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("287700008");
		ts8.setGeneralAdmissionInd(true);
		ts8.setTicketId(1246680055L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("ADM");
		ts8.setSection("LAWN3");
		ts8.setSeatNumber("128");
		ts8.setTicketSeatId(2984474213L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId("287700009");
		ts9.setGeneralAdmissionInd(true);
		ts9.setTicketId(1246680055L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow("ADM");
		ts9.setSection("LAWN3");
		ts9.setSeatNumber("129");
		ts9.setTicketSeatId(2984474214L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId("287700010");
		ts10.setGeneralAdmissionInd(true);
		ts10.setTicketId(1246680055L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow("ADM");
		ts10.setSection("LAWN3");
		ts10.setSeatNumber("130");
		ts10.setTicketSeatId(2984474215L);
		dbSeats.add(ts10);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}

		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//Payload: {"products":[{"fulfillmentArtifact":"C5J7-CLP79DRK","operation":"UPDATE","row":"ADM","seat":"221"},{"fulfillmentArtifact":"C5J7-JKT92ESL","operation":"UPDATE","row":"ADM","seat":"222"},{"fulfillmentArtifact":"C5J7-SYUGD9WQ","operation":"UPDATE","row":"ADM","seat":"223"},{"fulfillmentArtifact":"C5J7-RQVMM3HY","operation":"UPDATE","row":"ADM","seat":"224"},{"fulfillmentArtifact":"C5J7-RVVGG8HY","operation":"UPDATE","row":"ADM","seat":"225"},{"fulfillmentArtifact":"C5J7-SUUMX2WQ","operation":"UPDATE","row":"ADM","seat":"226"}]}
	//ExternalId: NULL to NULL
	//Row: Same
	//SeatNumber: Same
	@Test
	public void processTicketProductTest2502_3_1() {
		Listing listing = new Listing();
		listing.setQuantityRemain(6);
		listing.setId(1247041960L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(6);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-CLP79DRK", "ADM", "221", 2984474206L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-JKT92ESL", "ADM", "222", 2984474207L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-SYUGD9WQ", "ADM", "223", 2984474208L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-RQVMM3HY", "ADM", "224", 2984474209L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-RVVGG8HY", "ADM", "225", 2984474210L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-SUUMX2WQ", "ADM", "226", 2984474211L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(false);
		ts1.setTicketId(1247041960L);
		ts1.setRow("ADM");
		ts1.setSeatNumber("221");
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN2");
		ts1.setTicketSeatId(2986112350L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId(null);
		ts2.setGeneralAdmissionInd(false);
		ts2.setTicketId(1247041960L);
		ts2.setRow("ADM");
		ts2.setSeatNumber("222");
		ts2.setSeatStatusId(1L);
		ts2.setSection("LAWN2");
		ts2.setTicketSeatId(2986112351L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId(null);
		ts3.setGeneralAdmissionInd(false);
		ts3.setTicketId(1247041960L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("ADM");
		ts3.setSection("LAWN2");
		ts3.setSeatNumber("223");
		ts3.setTicketSeatId(2986112352L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId(null);
		ts4.setGeneralAdmissionInd(false);
		ts4.setTicketId(1247041960L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("ADM");
		ts4.setSection("LAWN2");
		ts4.setSeatNumber("224");
		ts4.setTicketSeatId(2986112353L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId(null);
		ts5.setGeneralAdmissionInd(false);
		ts5.setTicketId(1247041960L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("ADM");
		ts5.setSection("LAWN2");
		ts5.setSeatNumber("225");
		ts5.setTicketSeatId(2986112354L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId(null);
		ts6.setGeneralAdmissionInd(false);
		ts6.setTicketId(1247041960L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("ADM");
		ts6.setSection("LAWN2");
		ts6.setSeatNumber("226");
		ts6.setTicketSeatId(2986112355L);
		dbSeats.add(ts6);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: NULL to NULL
	//Row: Different
	//SeatNumber: Same
	@Test(expectedExceptions={ListingBusinessException.class})
	public void processTicketProductTest2502_3_2() {
		Listing listing = new Listing();
		listing.setQuantityRemain(6);
		listing.setId(1247041960L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(6);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-CLP79DRK", "ADM1", "221", 2984474206L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-JKT92ESL", "ADM1", "222", 2984474207L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-SYUGD9WQ", "ADM1", "223", 2984474208L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-RQVMM3HY", "ADM1", "224", 2984474209L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-RVVGG8HY", "ADM1", "225", 2984474210L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-SUUMX2WQ", "ADM1", "226", 2984474211L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(false);
		ts1.setTicketId(1247041960L);
		ts1.setRow("ADM");
		ts1.setSeatNumber("221");
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN2");
		ts1.setTicketSeatId(2986112350L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId(null);
		ts2.setGeneralAdmissionInd(false);
		ts2.setTicketId(1247041960L);
		ts2.setRow("ADM");
		ts2.setSeatNumber("222");
		ts2.setSeatStatusId(1L);
		ts2.setSection("LAWN2");
		ts2.setTicketSeatId(2986112351L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId(null);
		ts3.setGeneralAdmissionInd(false);
		ts3.setTicketId(1247041960L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("ADM");
		ts3.setSection("LAWN2");
		ts3.setSeatNumber("223");
		ts3.setTicketSeatId(2986112352L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId(null);
		ts4.setGeneralAdmissionInd(false);
		ts4.setTicketId(1247041960L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("ADM");
		ts4.setSection("LAWN2");
		ts4.setSeatNumber("224");
		ts4.setTicketSeatId(2986112353L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId(null);
		ts5.setGeneralAdmissionInd(false);
		ts5.setTicketId(1247041960L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("ADM");
		ts5.setSection("LAWN2");
		ts5.setSeatNumber("225");
		ts5.setTicketSeatId(2986112354L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId(null);
		ts6.setGeneralAdmissionInd(false);
		ts6.setTicketId(1247041960L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("ADM");
		ts6.setSection("LAWN2");
		ts6.setSeatNumber("226");
		ts6.setTicketSeatId(2986112355L);
		dbSeats.add(ts6);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		} catch (ListingBusinessException e) {
			Assert.assertEquals("Cannot locate seat product to update: (row:ADM1, seat:221)", e.getMessage());
			throw e;
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
	}

	//ExternalId: NULL to NULL
	//Row: Different
	//SeatNumber: Different
	@Test(expectedExceptions={ListingBusinessException.class})
	public void processTicketProductTest2502_3_3() {
		Listing listing = new Listing();
		listing.setQuantityRemain(6);
		listing.setId(1247041960L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(10);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-CLP79DRK", "ADM1", "231", 2984474206L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-JKT92ESL", "ADM1", "232", 2984474207L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-SYUGD9WQ", "ADM1", "233", 2984474208L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-RQVMM3HY", "ADM1", "234", 2984474209L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-RVVGG8HY", "ADM1", "235", 2984474210L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "C5J7-SUUMX2WQ", "ADM1", "236", 2984474211L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(false);
		ts1.setTicketId(1247041960L);
		ts1.setRow("ADM");
		ts1.setSeatNumber("221");
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN2");
		ts1.setTicketSeatId(2986112350L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId(null);
		ts2.setGeneralAdmissionInd(false);
		ts2.setTicketId(1247041960L);
		ts2.setRow("ADM");
		ts2.setSeatNumber("222");
		ts2.setSeatStatusId(1L);
		ts2.setSection("LAWN2");
		ts2.setTicketSeatId(2986112351L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId(null);
		ts3.setGeneralAdmissionInd(false);
		ts3.setTicketId(1247041960L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("ADM");
		ts3.setSection("LAWN2");
		ts3.setSeatNumber("223");
		ts3.setTicketSeatId(2986112352L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId(null);
		ts4.setGeneralAdmissionInd(false);
		ts4.setTicketId(1247041960L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("ADM");
		ts4.setSection("LAWN2");
		ts4.setSeatNumber("224");
		ts4.setTicketSeatId(2986112353L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId(null);
		ts5.setGeneralAdmissionInd(false);
		ts5.setTicketId(1247041960L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("ADM");
		ts5.setSection("LAWN2");
		ts5.setSeatNumber("225");
		ts5.setTicketSeatId(2986112354L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId(null);
		ts6.setGeneralAdmissionInd(false);
		ts6.setTicketId(1247041960L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("ADM");
		ts6.setSection("LAWN2");
		ts6.setSeatNumber("226");
		ts6.setTicketSeatId(2986112355L);
		dbSeats.add(ts6);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		} catch (ListingBusinessException e) {
			Assert.assertEquals("Cannot locate seat product to update: (row:ADM1, seat:231)", e.getMessage());
			throw e;
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
	}

	//ExternalId: NULL to NULL
	//Row: Same
	//SeatNumber: Same
	@Test
	public void processTicketProductTest8372_1_1() {
		Listing listing = new Listing();
		listing.setQuantityRemain(1);
		listing.setId(1250815718L);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setTicketMedium(2);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(1);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "1234567890", "LOT", "252", 3003163584L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId (1250815718L);
		ts1.setRow("LOT");
		ts1.setSeatNumber("252");
		ts1.setSeatStatusId(1L);
		ts1.setSection("Lot A");
		ts1.setTicketSeatId(3003163584L);
		ts1.setTixListTypeId(2L);
		dbSeats.add(ts1);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: NULL to NULL
	//Row: Same
	//SeatNumber: Same
	@Test
	public void processTicketProductTest8372_1_2() {
		Listing listing = new Listing();
		listing.setQuantityRemain(1);
		listing.setId(1250815718L);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setExternalId(null);
		listing.setTicketMedium(2);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(1);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "1234567890", "LOT", "252", 3003163584L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId (1250815718L);
		ts1.setRow("LOT");
		ts1.setSeatNumber("252");
		ts1.setSeatStatusId(1L);
		ts1.setSection("Lot A");
		ts1.setTicketSeatId(3003163584L);
		ts1.setTixListTypeId(2L);
		dbSeats.add(ts1);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: NULL to NULL
	//Row: Different
	//SeatNumber: Same
	@Test(expectedExceptions={ListingBusinessException.class})
	public void processTicketProductTest8372_1_3() {
		Listing listing = new Listing();
		listing.setQuantityRemain(1);
		listing.setId(1250815718L);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setTicketMedium(2);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(1);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "1234567890", "LOT 1", "252", 3003163584L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId (1250815718L);
		ts1.setRow("LOT");
		ts1.setSeatNumber("252");
		ts1.setSeatStatusId(1L);
		ts1.setSection("Lot A");
		ts1.setTicketSeatId(3003163584L);
		ts1.setTixListTypeId(2L);
		dbSeats.add(ts1);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		} catch (ListingBusinessException e) {
			Assert.assertEquals("Cannot locate seat product to update: (row:LOT 1, seat:252)", e.getMessage());
			throw e;
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
	}

	//ExternalId: NULL to NULL
	//Row: Different
	//SeatNumber: Different
	@Test(expectedExceptions={ListingBusinessException.class})
	public void processTicketProductTest8372_1_4() {
		Listing listing = new Listing();
		listing.setQuantityRemain(1);
		listing.setId(1250815718L);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setTicketMedium(2);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(1);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "1234567890", "LOT 1", "253", 3003163584L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId (1250815718L);
		ts1.setRow("LOT");
		ts1.setSeatNumber("252");
		ts1.setSeatStatusId(1L);
		ts1.setSection("Lot A");
		ts1.setTicketSeatId(3003163584L);
		ts1.setTixListTypeId(2L);
		dbSeats.add(ts1);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		} catch (ListingBusinessException e) {
			Assert.assertEquals("Cannot locate seat product to update: (row:LOT 1, seat:253)", e.getMessage());
			throw e;
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
	}

	//ExternalId: NULL to NULL
	//Row: Same but some has spaces in request
	//SeatNumber: Same
	@Test
	public void processTicketProductTest2469_1_0() {
		Listing listing = new Listing();
		listing.setQuantityRemain(11);
		listing.setId(1251618296L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(11);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-MSCMBXZ7", " 2", "5", 3007014266L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-GLYMXFP9", " 2", "6", 3007014267L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-XKQGGMT2", " 2", "7", 3007014268L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-6YL978QD", " 2", "8", 3007014269L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-7QK762YM", " 2", "9", 3007014270L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-XKQDMMT2", " 2", "10", 3007014271L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-GLYXDFP9", " 2", "11", 3007014272L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-7QK222YM", "2", "12", 3007014273L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-6YL698QD", "2", "13", 3007014274L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-2CS637JX", "2", "14", 3007014275L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-9JR284CG", "2", "15", 3007014276L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(false);
		ts1.setTicketId(1251618296L);
		ts1.setRow("2");
		ts1.setSeatNumber("5");
		ts1.setSeatStatusId(1L);
		ts1.setSection("317");
		ts1.setTicketSeatId(3007014266L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId(null);
		ts2.setGeneralAdmissionInd(false);
		ts2.setTicketId(1251618296L);
		ts2.setRow("2");
		ts2.setSeatNumber("6");
		ts2.setSeatStatusId(1L);
		ts2.setSection("317");
		ts2.setTicketSeatId(3007014267L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId(null);
		ts3.setGeneralAdmissionInd(false);
		ts3.setTicketId(1251618296L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("2");
		ts3.setSection("317");
		ts3.setSeatNumber("7");
		ts3.setTicketSeatId(3007014268L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId(null);
		ts4.setGeneralAdmissionInd(false);
		ts4.setTicketId(1251618296L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("2");
		ts4.setSection("317");
		ts4.setSeatNumber("8");
		ts4.setTicketSeatId(3007014269L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId(null);
		ts5.setGeneralAdmissionInd(false);
		ts5.setTicketId(1251618296L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("2");
		ts5.setSection("317");
		ts5.setSeatNumber("9");
		ts5.setTicketSeatId(3007014270L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId(null);
		ts6.setGeneralAdmissionInd(false);
		ts6.setTicketId(1251618296L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("2");
		ts6.setSection("317");
		ts6.setSeatNumber("10");
		ts6.setTicketSeatId(3007014271L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId(null);
		ts7.setGeneralAdmissionInd(false);
		ts7.setTicketId(1251618296L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("2");
		ts7.setSection("317");
		ts7.setSeatNumber("11");
		ts7.setTicketSeatId(3007014272L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId(null);
		ts8.setGeneralAdmissionInd(false);
		ts8.setTicketId(1251618296L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("2");
		ts8.setSection("317");
		ts8.setSeatNumber("12");
		ts8.setTicketSeatId(3007014273L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId(null);
		ts9.setGeneralAdmissionInd(false);
		ts9.setTicketId(1251618296L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow("2");
		ts9.setSection("317");
		ts9.setSeatNumber("13");
		ts9.setTicketSeatId(3007014274L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId(null);
		ts10.setGeneralAdmissionInd(false);
		ts10.setTicketId(1251618296L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow("2");
		ts10.setSection("317");
		ts10.setSeatNumber("14");
		ts10.setTicketSeatId(3007014275L);
		dbSeats.add(ts10);

		TicketSeat ts11 = new TicketSeat();
		ts11.setExternalSeatId(null);
		ts11.setGeneralAdmissionInd(false);
		ts11.setTicketId(1251618296L);
		ts11.setSeatStatusId(1L);
		ts11.setTixListTypeId(1L);
		ts11.setRow("2");
		ts11.setSection("317");
		ts11.setSeatNumber("15");
		ts11.setTicketSeatId(3007014276L);
		dbSeats.add(ts11);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: NULL to NULL
	//Row: Same
	//SeatNumber: Same
	@Test
	public void processTicketProductTest2469_1_1() {
		Listing listing = new Listing();
		listing.setQuantityRemain(11);
		listing.setId(1251618296L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(11);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-MSCMBXZ7", "2", "5", 3007014266L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-GLYMXFP9", "2", "6", 3007014267L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-XKQGGMT2", "2", "7", 3007014268L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-6YL978QD", "2", "8", 3007014269L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-7QK762YM", "2", "9", 3007014270L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-XKQDMMT2", "2", "10", 3007014271L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-GLYXDFP9", "2", "11", 3007014272L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-7QK222YM", "2", "12", 3007014273L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-6YL698QD", "2", "13", 3007014274L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-2CS637JX", "2", "14", 3007014275L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-9JR284CG", "2", "15", 3007014276L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(false);
		ts1.setTicketId(1251618296L);
		ts1.setRow("2");
		ts1.setSeatNumber("5");
		ts1.setSeatStatusId(1L);
		ts1.setSection("317");
		ts1.setTicketSeatId(3007014266L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId(null);
		ts2.setGeneralAdmissionInd(false);
		ts2.setTicketId(1251618296L);
		ts2.setRow("2");
		ts2.setSeatNumber("6");
		ts2.setSeatStatusId(1L);
		ts2.setSection("317");
		ts2.setTicketSeatId(3007014267L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId(null);
		ts3.setGeneralAdmissionInd(false);
		ts3.setTicketId(1251618296L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("2");
		ts3.setSection("317");
		ts3.setSeatNumber("7");
		ts3.setTicketSeatId(3007014268L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId(null);
		ts4.setGeneralAdmissionInd(false);
		ts4.setTicketId(1251618296L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("2");
		ts4.setSection("317");
		ts4.setSeatNumber("8");
		ts4.setTicketSeatId(3007014269L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId(null);
		ts5.setGeneralAdmissionInd(false);
		ts5.setTicketId(1251618296L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("2");
		ts5.setSection("317");
		ts5.setSeatNumber("9");
		ts5.setTicketSeatId(3007014270L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId(null);
		ts6.setGeneralAdmissionInd(false);
		ts6.setTicketId(1251618296L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("2");
		ts6.setSection("317");
		ts6.setSeatNumber("10");
		ts6.setTicketSeatId(3007014271L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId(null);
		ts7.setGeneralAdmissionInd(false);
		ts7.setTicketId(1251618296L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("2");
		ts7.setSection("317");
		ts7.setSeatNumber("11");
		ts7.setTicketSeatId(3007014272L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId(null);
		ts8.setGeneralAdmissionInd(false);
		ts8.setTicketId(1251618296L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("2");
		ts8.setSection("317");
		ts8.setSeatNumber("12");
		ts8.setTicketSeatId(3007014273L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId(null);
		ts9.setGeneralAdmissionInd(false);
		ts9.setTicketId(1251618296L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow("2");
		ts9.setSection("317");
		ts9.setSeatNumber("13");
		ts9.setTicketSeatId(3007014274L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId(null);
		ts10.setGeneralAdmissionInd(false);
		ts10.setTicketId(1251618296L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow("2");
		ts10.setSection("317");
		ts10.setSeatNumber("14");
		ts10.setTicketSeatId(3007014275L);
		dbSeats.add(ts10);

		TicketSeat ts11 = new TicketSeat();
		ts11.setExternalSeatId(null);
		ts11.setGeneralAdmissionInd(false);
		ts11.setTicketId(1251618296L);
		ts11.setSeatStatusId(1L);
		ts11.setTixListTypeId(1L);
		ts11.setRow("2");
		ts11.setSection("317");
		ts11.setSeatNumber("15");
		ts11.setTicketSeatId(3007014276L);
		dbSeats.add(ts11);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: NULL to NULL
	//Row: Same but some has spaces in request
	//SeatNumber: NULL to NULL
	@Test
	public void processTicketProductTest2469_1_2() {
		Listing listing = new Listing();
		listing.setQuantityRemain(11);
		listing.setId(1251618296L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(11);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-MSCMBXZ7", " 2", null, 3007014266L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-GLYMXFP9", " 2", null, 3007014267L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-XKQGGMT2", " 2", null, 3007014268L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-6YL978QD", " 2", null, 3007014269L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-7QK762YM", " 2", null, 3007014270L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-XKQDMMT2", " 2", null, 3007014271L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-GLYXDFP9", " 2", null, 3007014272L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-7QK222YM", "2", null, 3007014273L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-6YL698QD", "2", null, 3007014274L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-2CS637JX", "2", null, 3007014275L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-9JR284CG", "2", null, 3007014276L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(false);
		ts1.setTicketId(1251618296L);
		ts1.setRow("2");
		ts1.setSeatNumber(null);
		ts1.setSeatStatusId(1L);
		ts1.setSection("317");
		ts1.setTicketSeatId(3007014266L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId(null);
		ts2.setGeneralAdmissionInd(false);
		ts2.setTicketId(1251618296L);
		ts2.setRow("2");
		ts2.setSeatNumber(null);
		ts2.setSeatStatusId(1L);
		ts2.setSection("317");
		ts2.setTicketSeatId(3007014267L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId(null);
		ts3.setGeneralAdmissionInd(false);
		ts3.setTicketId(1251618296L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("2");
		ts3.setSection("317");
		ts3.setSeatNumber(null);
		ts3.setTicketSeatId(3007014268L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId(null);
		ts4.setGeneralAdmissionInd(false);
		ts4.setTicketId(1251618296L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("2");
		ts4.setSection("317");
		ts4.setSeatNumber(null);
		ts4.setTicketSeatId(3007014269L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId(null);
		ts5.setGeneralAdmissionInd(false);
		ts5.setTicketId(1251618296L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("2");
		ts5.setSection("317");
		ts5.setSeatNumber(null);
		ts5.setTicketSeatId(3007014270L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId(null);
		ts6.setGeneralAdmissionInd(false);
		ts6.setTicketId(1251618296L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("2");
		ts6.setSection("317");
		ts6.setSeatNumber(null);
		ts6.setTicketSeatId(3007014271L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId(null);
		ts7.setGeneralAdmissionInd(false);
		ts7.setTicketId(1251618296L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("2");
		ts7.setSection("317");
		ts7.setSeatNumber(null);
		ts7.setTicketSeatId(3007014272L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId(null);
		ts8.setGeneralAdmissionInd(false);
		ts8.setTicketId(1251618296L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("2");
		ts8.setSection("317");
		ts8.setSeatNumber(null);
		ts8.setTicketSeatId(3007014273L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId(null);
		ts9.setGeneralAdmissionInd(false);
		ts9.setTicketId(1251618296L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow("2");
		ts9.setSection("317");
		ts9.setSeatNumber(null);
		ts9.setTicketSeatId(3007014274L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId(null);
		ts10.setGeneralAdmissionInd(false);
		ts10.setTicketId(1251618296L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow("2");
		ts10.setSection("317");
		ts10.setSeatNumber(null);
		ts10.setTicketSeatId(3007014275L);
		dbSeats.add(ts10);

		TicketSeat ts11 = new TicketSeat();
		ts11.setExternalSeatId(null);
		ts11.setGeneralAdmissionInd(false);
		ts11.setTicketId(1251618296L);
		ts11.setSeatStatusId(1L);
		ts11.setTixListTypeId(1L);
		ts11.setRow("2");
		ts11.setSection("317");
		ts11.setSeatNumber(null);
		ts11.setTicketSeatId(3007014276L);
		dbSeats.add(ts11);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: Same but some has spaces in request
	//Row: NULL to NULL
	//SeatNumber: NULL to NULL
	@Test
	public void processTicketProductTest2469_1_3() {
		Listing listing = new Listing();
		listing.setQuantityRemain(11);
		listing.setId(1251618296L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(11);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-MSCMBXZ7", null, null, 3007014266L, Operation.UPDATE, " 1"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-GLYMXFP9", null, null, 3007014267L, Operation.UPDATE, " 2"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-XKQGGMT2", null, null, 3007014268L, Operation.UPDATE, "3"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-6YL978QD", null, null, 3007014269L, Operation.UPDATE, "4"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-7QK762YM", null, null, 3007014270L, Operation.UPDATE, "5"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-XKQDMMT2", null, null, 3007014271L, Operation.UPDATE, "6 "));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-GLYXDFP9", null, null, 3007014272L, Operation.UPDATE, "7 "));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-7QK222YM", null, null, 3007014273L, Operation.UPDATE, "  8"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-6YL698QD", null, null, 3007014274L, Operation.UPDATE, "  9"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-2CS637JX", null, null, 3007014275L, Operation.UPDATE, "10  "));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-9JR284CG", null, null, 3007014276L, Operation.UPDATE, " 11  "));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("1");
		ts1.setGeneralAdmissionInd(false);
		ts1.setTicketId(1251618296L);
		ts1.setRow("2");
		ts1.setSeatNumber("5");
		ts1.setSeatStatusId(1L);
		ts1.setSection("317");
		ts1.setTicketSeatId(3007014266L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("2");
		ts2.setGeneralAdmissionInd(false);
		ts2.setTicketId(1251618296L);
		ts2.setRow("2");
		ts2.setSeatNumber("6");
		ts2.setSeatStatusId(1L);
		ts2.setSection("317");
		ts2.setTicketSeatId(3007014267L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId("3");
		ts3.setGeneralAdmissionInd(false);
		ts3.setTicketId(1251618296L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("2");
		ts3.setSection("317");
		ts3.setSeatNumber("7");
		ts3.setTicketSeatId(3007014268L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId("4");
		ts4.setGeneralAdmissionInd(false);
		ts4.setTicketId(1251618296L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow("2");
		ts4.setSection("317");
		ts4.setSeatNumber("8");
		ts4.setTicketSeatId(3007014269L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId("5");
		ts5.setGeneralAdmissionInd(false);
		ts5.setTicketId(1251618296L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow("2");
		ts5.setSection("317");
		ts5.setSeatNumber("9");
		ts5.setTicketSeatId(3007014270L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId("6");
		ts6.setGeneralAdmissionInd(false);
		ts6.setTicketId(1251618296L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow("2");
		ts6.setSection("317");
		ts6.setSeatNumber("10");
		ts6.setTicketSeatId(3007014271L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId("7");
		ts7.setGeneralAdmissionInd(false);
		ts7.setTicketId(1251618296L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow("2");
		ts7.setSection("317");
		ts7.setSeatNumber("11");
		ts7.setTicketSeatId(3007014272L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId("8");
		ts8.setGeneralAdmissionInd(false);
		ts8.setTicketId(1251618296L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow("2");
		ts8.setSection("317");
		ts8.setSeatNumber("12");
		ts8.setTicketSeatId(3007014273L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId("9");
		ts9.setGeneralAdmissionInd(false);
		ts9.setTicketId(1251618296L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow("2");
		ts9.setSection("317");
		ts9.setSeatNumber("13");
		ts9.setTicketSeatId(3007014274L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId("10");
		ts10.setGeneralAdmissionInd(false);
		ts10.setTicketId(1251618296L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow("2");
		ts10.setSection("317");
		ts10.setSeatNumber("14");
		ts10.setTicketSeatId(3007014275L);
		dbSeats.add(ts10);

		TicketSeat ts11 = new TicketSeat();
		ts11.setExternalSeatId("11");
		ts11.setGeneralAdmissionInd(false);
		ts11.setTicketId(1251618296L);
		ts11.setSeatStatusId(1L);
		ts11.setTixListTypeId(1L);
		ts11.setRow("2");
		ts11.setSection("317");
		ts11.setSeatNumber("15");
		ts11.setTicketSeatId(3007014276L);
		dbSeats.add(ts11);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: NULL to NULL
	//Row: NULL to NULL
	//SeatNumber: Same but some has spaces in request
	@Test
	public void processTicketProductTest2469_1_4() {
		Listing listing = new Listing();
		listing.setQuantityRemain(11);
		listing.setId(1251618296L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(11);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-MSCMBXZ7", null, " 5", 3007014266L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-GLYMXFP9", null, " 6", 3007014267L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-XKQGGMT2", null, "7 ", 3007014268L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-6YL978QD", null, "8 ", 3007014269L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-7QK762YM", null, "9", 3007014270L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-XKQDMMT2", null, "10", 3007014271L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-GLYXDFP9", null, "  11", 3007014272L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-7QK222YM", null, "  12", 3007014273L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-6YL698QD", null, "13  ", 3007014274L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-2CS637JX", null, "14  ", 3007014275L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "JCD7-9JR284CG", null, " 15 ", 3007014276L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(false);
		ts1.setTicketId(1251618296L);
		ts1.setRow(null);
		ts1.setSeatNumber("5");
		ts1.setSeatStatusId(1L);
		ts1.setSection("317");
		ts1.setTicketSeatId(3007014266L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId(null);
		ts2.setGeneralAdmissionInd(false);
		ts2.setTicketId(1251618296L);
		ts2.setRow(null);
		ts2.setSeatNumber("6");
		ts2.setSeatStatusId(1L);
		ts2.setSection("317");
		ts2.setTicketSeatId(3007014267L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId(null);
		ts3.setGeneralAdmissionInd(false);
		ts3.setTicketId(1251618296L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow(null);
		ts3.setSection("317");
		ts3.setSeatNumber("7");
		ts3.setTicketSeatId(3007014268L);
		dbSeats.add(ts3);

		TicketSeat ts4 = new TicketSeat();
		ts4.setExternalSeatId(null);
		ts4.setGeneralAdmissionInd(false);
		ts4.setTicketId(1251618296L);
		ts4.setSeatStatusId(1L);
		ts4.setTixListTypeId(1L);
		ts4.setRow(null);
		ts4.setSection("317");
		ts4.setSeatNumber("8");
		ts4.setTicketSeatId(3007014269L);
		dbSeats.add(ts4);

		TicketSeat ts5 = new TicketSeat();
		ts5.setExternalSeatId(null);
		ts5.setGeneralAdmissionInd(false);
		ts5.setTicketId(1251618296L);
		ts5.setSeatStatusId(1L);
		ts5.setTixListTypeId(1L);
		ts5.setRow(null);
		ts5.setSection("317");
		ts5.setSeatNumber("9");
		ts5.setTicketSeatId(3007014270L);
		dbSeats.add(ts5);

		TicketSeat ts6 = new TicketSeat();
		ts6.setExternalSeatId(null);
		ts6.setGeneralAdmissionInd(false);
		ts6.setTicketId(1251618296L);
		ts6.setSeatStatusId(1L);
		ts6.setTixListTypeId(1L);
		ts6.setRow(null);
		ts6.setSection("317");
		ts6.setSeatNumber("10");
		ts6.setTicketSeatId(3007014271L);
		dbSeats.add(ts6);

		TicketSeat ts7 = new TicketSeat();
		ts7.setExternalSeatId(null);
		ts7.setGeneralAdmissionInd(false);
		ts7.setTicketId(1251618296L);
		ts7.setSeatStatusId(1L);
		ts7.setTixListTypeId(1L);
		ts7.setRow(null);
		ts7.setSection("317");
		ts7.setSeatNumber("11");
		ts7.setTicketSeatId(3007014272L);
		dbSeats.add(ts7);

		TicketSeat ts8 = new TicketSeat();
		ts8.setExternalSeatId(null);
		ts8.setGeneralAdmissionInd(false);
		ts8.setTicketId(1251618296L);
		ts8.setSeatStatusId(1L);
		ts8.setTixListTypeId(1L);
		ts8.setRow(null);
		ts8.setSection("317");
		ts8.setSeatNumber("12");
		ts8.setTicketSeatId(3007014273L);
		dbSeats.add(ts8);

		TicketSeat ts9 = new TicketSeat();
		ts9.setExternalSeatId(null);
		ts9.setGeneralAdmissionInd(false);
		ts9.setTicketId(1251618296L);
		ts9.setSeatStatusId(1L);
		ts9.setTixListTypeId(1L);
		ts9.setRow(null);
		ts9.setSection("317");
		ts9.setSeatNumber("13");
		ts9.setTicketSeatId(3007014274L);
		dbSeats.add(ts9);

		TicketSeat ts10 = new TicketSeat();
		ts10.setExternalSeatId(null);
		ts10.setGeneralAdmissionInd(false);
		ts10.setTicketId(1251618296L);
		ts10.setSeatStatusId(1L);
		ts10.setTixListTypeId(1L);
		ts10.setRow(null);
		ts10.setSection("317");
		ts10.setSeatNumber("14");
		ts10.setTicketSeatId(3007014275L);
		dbSeats.add(ts10);

		TicketSeat ts11 = new TicketSeat();
		ts11.setExternalSeatId(null);
		ts11.setGeneralAdmissionInd(false);
		ts11.setTicketId(1251618296L);
		ts11.setSeatStatusId(1L);
		ts11.setTixListTypeId(1L);
		ts11.setRow(null);
		ts11.setSection("317");
		ts11.setSeatNumber("15");
		ts11.setTicketSeatId(3007014276L);
		dbSeats.add(ts11);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();
		for(SeatProduct seatProduct : seatProductList) {
			Mockito.doReturn(true).when(seatProductsContext).addArtifactSeatProductToList(seatProduct);
		}

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: NULL to NULL
	//Row: Same
	//SeatNumber: Same (GA)
	@Test
	public void processTicketProductTest2469_1_5() {
		Listing listing = new Listing();
		listing.setQuantityRemain(3);
		listing.setId(1234567890L);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setTicketMedium(2);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(3);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "756906303404", "Ga1", "28", 3007014266L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "756906303405", "Ga1", "29", 3007014267L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "756906303409", "Ga1", "30", 3007014268L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId(1234567890L);
		ts1.setRow(null);
		ts1.setSeatNumber("28");
		ts1.setSeatStatusId(1L);
		ts1.setSection("1");
		ts1.setTicketSeatId(3007014266L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId(null);
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId(1234567890L);
		ts2.setRow(null);
		ts2.setSeatNumber("29");
		ts2.setSeatStatusId(1L);
		ts2.setSection("1");
		ts2.setTicketSeatId(3007014267L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId(null);
		ts3.setGeneralAdmissionInd(true);
		ts3.setTicketId(1234567890L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow(null);
		ts3.setSection("1");
		ts3.setSeatNumber("30");
		ts3.setTicketSeatId(3007014268L);
		dbSeats.add(ts3);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: NULL to NULL
	//Row: Same
	//SeatNumber: Same
	@Test
	public void processTicketProductTest2469_1_6() {
		Listing listing = new Listing();
		listing.setQuantityRemain(3);
		listing.setId(1234567890L);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setTicketMedium(2);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(3);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "756906303404", "Ga1", "28", 3007014266L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "756906303405", "Ga1", "29", 3007014267L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "756906303409", "Ga1", "30", 3007014268L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(false);
		ts1.setTicketId(1234567890L);
		ts1.setRow(null);
		ts1.setSeatNumber("28");
		ts1.setSeatStatusId(1L);
		ts1.setSection("1");
		ts1.setTicketSeatId(3007014266L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId(null);
		ts2.setGeneralAdmissionInd(false);
		ts2.setTicketId(1234567890L);
		ts2.setRow(null);
		ts2.setSeatNumber("29");
		ts2.setSeatStatusId(1L);
		ts2.setSection("1");
		ts2.setTicketSeatId(3007014267L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId(null);
		ts3.setGeneralAdmissionInd(false);
		ts3.setTicketId(1234567890L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow(null);
		ts3.setSection("1");
		ts3.setSeatNumber("30");
		ts3.setTicketSeatId(3007014268L);
		dbSeats.add(ts3);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
		Mockito.verify(seatProductsContext).getIsDBTicketsChanged();
	}

	//ExternalId: NULL to NULL
	//Row: existing value to NULL
	//SeatNumber: Same
	@Test(expectedExceptions={ListingBusinessException.class})
	public void processTicketProductTest2469_1_7() {
		Listing listing = new Listing();
		listing.setQuantityRemain(3);
		listing.setId(1234567890L);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setTicketMedium(2);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(3);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "756906303404", null, "28", 3007014266L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "756906303405", null, "29", 3007014267L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "756906303409", null, "30", 3007014268L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(false);
		ts1.setTicketId(1234567890L);
		ts1.setRow("Ga1");
		ts1.setSeatNumber("28");
		ts1.setSeatStatusId(1L);
		ts1.setSection("1");
		ts1.setTicketSeatId(3007014266L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId(null);
		ts2.setGeneralAdmissionInd(false);
		ts2.setTicketId(1234567890L);
		ts2.setRow("Ga1");
		ts2.setSeatNumber("29");
		ts2.setSeatStatusId(1L);
		ts2.setSection("1");
		ts2.setTicketSeatId(3007014267L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId(null);
		ts3.setGeneralAdmissionInd(false);
		ts3.setTicketId(1234567890L);
		ts3.setSeatStatusId(1L);
		ts3.setTixListTypeId(1L);
		ts3.setRow("Ga1");
		ts3.setSection("1");
		ts3.setSeatNumber("30");
		ts3.setTicketSeatId(3007014268L);
		dbSeats.add(ts3);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		} catch (ListingBusinessException e) {
			Assert.assertEquals("Cannot locate seat product to update: (row:null, seat:28)", e.getMessage());
			throw e;
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
	}

	//ExternalId: NULL to NULL
	//Row: Same
	//SeatNumber: Same
	@Test(expectedExceptions={ListingBusinessException.class})
	public void processTicketProductTest2469_1_8() {
		Listing listing = new Listing();
		listing.setQuantityRemain(3);
		listing.setId(1234567890L);
		listing.setDeliveryOption(DeliveryOption.PDF.ordinal());
		listing.setTicketMedium(2);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(3);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "756906303404", "Ga1", "28", 3007014266L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "756906303405", "Ga1", "29", 3007014267L, Operation.UPDATE, null));
		seatProductList.add(getSeatProduct(ProductType.PARKING_PASS, "756906303409", null, null, 3007014268L, Operation.UPDATE, null));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId(null);
		ts1.setGeneralAdmissionInd(false);
		ts1.setTicketId(1234567890L);
		ts1.setRow(null);
		ts1.setSeatNumber("28");
		ts1.setSeatStatusId(1L);
		ts1.setSection("1");
		ts1.setTicketSeatId(3007014266L);
		ts1.setTixListTypeId(1L);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId(null);
		ts2.setGeneralAdmissionInd(false);
		ts2.setTicketId(1234567890L);
		ts2.setRow(null);
		ts2.setSeatNumber("29");
		ts2.setSeatStatusId(1L);
		ts2.setSection("1");
		ts2.setTicketSeatId(3007014267L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		TicketSeat ts3 = new TicketSeat();
		ts3.setExternalSeatId(null);
		ts3.setGeneralAdmissionInd(false);
		ts2.setTicketId(1234567890L);
		ts3.setRow(null);
		ts3.setSeatNumber(null);
		ts3.setSeatStatusId(1L);
		ts3.setSection("1");
		ts3.setTicketSeatId(3007014268L);
		ts3.setTixListTypeId(2L);
		dbSeats.add(ts3);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		} catch (ListingBusinessException e) {
			Assert.assertEquals("Cannot locate seat product to update: (row:null, seat:null)", e.getMessage());
			throw e;
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
	}

	@Test(expectedExceptions={ListingBusinessException.class})
	public void processTicketProductTest_DuplicateFullfillmentArtifact_1() {
		Listing listing = new Listing();
		listing.setQuantityRemain(1);
		listing.setId(1246680055L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(1);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-GBW28GR7", "ADM0", "121", 29844742061L, Operation.UPDATE, "287700011"));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("287700011");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId(1246680055L);
		ts1.setRow("ADM0");
		ts1.setSeatNumber("121");
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN3");
		ts1.setTicketSeatId(29844742061L);
		ts1.setTixListTypeId(1L);
		ts1.setFulfillmentArtifactId(null);
		dbSeats.add(ts1);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		} catch (ListingBusinessException e) {
			Assert.assertEquals("Duplicate fullfillmentArtifact for passed ticket: (row:ADM0, seat:121)", e.getMessage());
			throw e;
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
	}

	@Test(expectedExceptions={ListingBusinessException.class})
	public void processTicketProductTest_DuplicateFullfillmentArtifact_2() {
		Listing listing = new Listing();
		listing.setQuantityRemain(1);
		listing.setId(1246680055L);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setTicketMedium(3);
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(1);
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-GBW28GR7", "ADM0", "121", 29844742061L, Operation.UPDATE, "287700011"));
		seatProductList.add(getSeatProduct(ProductType.TICKET, "Q5J7-XAH63AS6", "ADM1", "122", 2984474207L, Operation.UPDATE, "287700012"));

		List<TicketSeat> dbSeats = new ArrayList<TicketSeat>();

		TicketSeat ts1 = new TicketSeat();
		ts1.setExternalSeatId("287700011");
		ts1.setGeneralAdmissionInd(true);
		ts1.setTicketId(1246680055L);
		ts1.setRow("ADM0");
		ts1.setSeatNumber("121");
		ts1.setSeatStatusId(1L);
		ts1.setSection("LAWN3");
		ts1.setTicketSeatId(29844742061L);
		ts1.setTixListTypeId(1L);
		ts1.setFulfillmentArtifactId(null);
		dbSeats.add(ts1);

		TicketSeat ts2 = new TicketSeat();
		ts2.setExternalSeatId("287700012");
		ts2.setGeneralAdmissionInd(true);
		ts2.setTicketId(1246680055L);
		ts2.setRow("ADM1");
		ts2.setSeatNumber("122");
		ts2.setSeatStatusId(1L);
		ts2.setSection("LAWN3");
		ts2.setTicketSeatId(2984474207L);
		ts2.setTixListTypeId(1L);
		dbSeats.add(ts2);

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
			Assert.fail("no exception expected", e);
		} catch (ListingBusinessException e) {
			Assert.assertEquals("Duplicate fullfillmentArtifact for passed ticket: (row:ADM0, seat:121)", e.getMessage());
			throw e;
		}
		Assert.assertTrue(true);
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
	}
	
	@Test
	public void processTicketProductTest_WithFaceValueAtListingAndProduct() {

		Listing listing = new Listing();
		listing.setQuantityRemain(2);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setSystemStatus(ListingStatus.INACTIVE.name());
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		listing.setFaceValue(new Money("140"));
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);
		//ProductType prodType, String ffArtifact, String row, String seat, long seatId, Operation operation, String externalId
		seatProductList.add(getSeatProductWithFaceValue(ProductType.TICKET, null, "15", "3", 123L, Operation.UPDATE, "1", new Money("20")));
		seatProductList.add(getSeatProductWithFaceValue(ProductType.TICKET, null, "15", "4", 124L, Operation.UPDATE, "3", new Money("30")));

		//long ticketId, long satusId, String section, String row, int count, boolean gaInd, boolean noSeatNum, String[] externalId
		List<TicketSeat> dbSeats = getTicketSeats(123L, 1L, "sec-10", "N/A", 2, true, true, new String[] {"2","3"});

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
		}
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
	}
	
	@Test
	public void processTicketProductTest_WithFaceValueAtListing() {

		Listing listing = new Listing();
		listing.setQuantityRemain(2);
		listing.setDeliveryOption(DeliveryOption.BARCODE.ordinal());
		listing.setSystemStatus(ListingStatus.INACTIVE.name());
		listing.setTicketMedium(TicketMedium.PAPER.getValue());
		listing.setFaceValue(new Money("140"));
		ListingRequest listingRequest = Mockito.mock(ListingRequest.class);

		List<SeatProduct> seatProductList = new ArrayList<SeatProduct>(2);
		//ProductType prodType, String ffArtifact, String row, String seat, long seatId, Operation operation, String externalId
		seatProductList.add(getSeatProductWithFaceValue(ProductType.TICKET, null, "15", "3", 123L, Operation.UPDATE, "1", null));
		seatProductList.add(getSeatProductWithFaceValue(ProductType.TICKET, null, "15", "4", 124L, Operation.UPDATE, "3", null));

		//long ticketId, long satusId, String section, String row, int count, boolean gaInd, boolean noSeatNum, String[] externalId
		List<TicketSeat> dbSeats = getTicketSeats(123L, 1L, "sec-10", "N/A", 2, true, true, new String[] {"2","3"});

		Mockito.doReturn(listing).when(seatProductsContext).getCurrentListing();
		Mockito.doReturn(seatProductList).when(seatProductsContext).getPassedSeatProductList(false);
		Mockito.doReturn(dbSeats).when(seatProductsContext).getTicketSeatsFromCache();

		try {
			SeatProductsManipulator.processTicketProducts(listing, seatProductsContext, listingRequest);
		} catch (ListingException e) {
			e.printStackTrace();
		}
		Mockito.verify(seatProductsContext).getPassedSeatProductList(false);
		Mockito.verify(seatProductsContext).setTicketSeatsBackInCurrentListing();
	}
	
	

}