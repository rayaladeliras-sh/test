package com.stubhub.domain.inventory.listings.v2.tasks;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.fulfillment.window.v1.intf.EventFulfillmentWindowResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.EventInhanddate;
import com.stubhub.domain.inventory.biz.v2.impl.util.FulfillmentServiceAdapter;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.FulfillmentMethod;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.util.FulfillmentServiceHelper;
import com.stubhub.newplatform.common.util.DateUtil;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;
import org.slf4j.MDC;

public class DeliveryAndFullfilmentOptionsTask implements RecallableInventoryTask<Listing>
{
	private static final Log log = LogFactory.getLog(DeliveryAndFullfilmentOptionsTask.class);
	
	private FulfillmentServiceAdapter fulfillmentServiceAdapter;
	private FulfillmentServiceHelper fulfillmentServiceHelper;
	
	private Listing listing;
	private Listing currentListing;
	private SHAPIContext apiContext;
	private boolean isCreate;
	private boolean isBulk;
	private List<FulfillmentWindow> fulfillmentWindows;

	private EventFulfillmentWindowResponse efwResponse = null;

	private static String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
	private final Map<String, String> context;
	private static final Map<String, String> EMPTY_CONTEXT = new HashMap<>();

	public DeliveryAndFullfilmentOptionsTask(Listing listing, Listing currentListing, SHAPIContext apiContext, 
			FulfillmentServiceHelper fulfillmentServiceHelper )
	{
		this (listing, currentListing, apiContext, fulfillmentServiceHelper, new FulfillmentServiceAdapter(), false, false, null);
	}
	
	public DeliveryAndFullfilmentOptionsTask(Listing listing, Listing currentListing, SHAPIContext apiContext, 
			FulfillmentServiceHelper fulfillmentServiceHelper, FulfillmentServiceAdapter fulfillmentServiceAdapter,
			boolean isCreate, boolean isBulk, List<FulfillmentWindow> fulfillmentWindows ) {
		this.listing = listing;
		this.currentListing = currentListing;
		this.apiContext = apiContext;  
		this.fulfillmentServiceHelper = fulfillmentServiceHelper;
		this.fulfillmentServiceAdapter = fulfillmentServiceAdapter;
		this.isCreate = isCreate;
		this.isBulk = isBulk;
		this.fulfillmentWindows = fulfillmentWindows;
		this.context = MDC.getCopyOfContextMap() != null ? MDC.getCopyOfContextMap() : EMPTY_CONTEXT;
	}
	
	/**
	 * Run the same task instance with a different listing and currentListing
	 * @param listing
	 * @param currentListing	  
	 * @return
	 *
	 */
	public void callAgain (Listing listing, Listing currentListing )
	{
		this.listing = listing;
		this.currentListing = currentListing;
		call();
	}
	
	public Listing call() {
		MDC.setContextMap(this.context);
		// create listing fulfillment and delivery logic
		if ( isCreate ) {
			log.debug("START common populateFulfillmentOptions task for create" );
			SHAPIThreadLocal.set(apiContext);
			List<FulfillmentWindow> fulfillmentWindows = this.fulfillmentWindows;
			if(fulfillmentWindows == null) {
				EventFulfillmentWindowResponse efwResponse = getCachedEventFulfillmentWindowResponse(listing);
				fulfillmentWindows = fulfillmentServiceAdapter.getFulfillmentWindows(efwResponse);
			}
			
			fulfillmentServiceHelper.populateFulfillmentOptions(listing, fulfillmentWindows);
			validateInHandDate(listing, efwResponse);
			log.debug("END common populateFulfillmentOptions task "); 
			return currentListing;
		}
		
		// Update listing fulfillment and delivery logic
		log.debug("START common populateFulfillmentOptions task for update" );
		
		// update delivery option
		if (listing.getFulfillmentMethod() != null || listing.isLmsExtensionRequired() || listing.getIsLmsApproval() == true ) {
			Listing newListing = new Listing();
			newListing.setEventId(currentListing.getEventId());
			newListing.setEvent(currentListing.getEvent());
			newListing.setSellerContactId(currentListing.getSellerContactId());
			newListing.setSellerContactGuid(currentListing.getSellerContactGuid());
			if(listing.getDeliveryOption() != null) {
				newListing.setDeliveryOption(listing.getDeliveryOption());
			} else {
				newListing.setDeliveryOption(currentListing.getDeliveryOption());
			}
			newListing.setFulfillmentMethod(listing.getFulfillmentMethod());
			newListing.setTicketMedium(listing.getTicketMedium());
			newListing.setIsLmsApproval(listing.getIsLmsApproval());
			newListing.setLmsApprovalStatus(listing.getLmsApprovalStatus());
			newListing.setFulfillmentDeliveryMethods(currentListing.getFulfillmentDeliveryMethods());
			newListing.setListingSource(listing.getListingSource());
			if(listing.getEndDate() != null){
				newListing.setEndDate(listing.getEndDate());
			}
			
			if(listing.isLmsExtensionRequired()) {
				newListing.setLmsExtensionRequired(listing.isLmsExtensionRequired());
				if(newListing.getTicketMedium() == null) {
					newListing.setTicketMedium(currentListing.getTicketMedium());
				}
				if(newListing.getFulfillmentMethod() == null) {
					String fmDMList = newListing.getFulfillmentDeliveryMethods();
					if(fmDMList != null) {
						if(fmDMList.contains("|10,") || fmDMList.startsWith("10,")){
							newListing.setFulfillmentMethod(FulfillmentMethod.UPS);
						} else if(fmDMList.contains("|11,") || fmDMList.contains("|12,") || fmDMList.startsWith("11,") || fmDMList.startsWith("12,")){
							newListing.setFulfillmentMethod(FulfillmentMethod.SHIPPING);
						}
					}
				}
			}
			
			SHAPIThreadLocal.set(apiContext);			

			
			List<FulfillmentWindow> fulfillmentWindows = this.fulfillmentWindows;
			if(fulfillmentWindows == null) {
				EventFulfillmentWindowResponse efwResponse = getCachedEventFulfillmentWindowResponse(newListing);
				fulfillmentWindows = fulfillmentServiceAdapter.getFulfillmentWindows(efwResponse);
			}
			
			if(!fulfillmentServiceHelper.populateFulfillmentOptions(newListing, fulfillmentWindows)) {
				ListingError listingError = new ListingError(
						ErrorType.BUSINESSERROR, ErrorCode.DELIVERY_OPTION_NOT_SUPPORTED, "populateFulfillmentOptions return false",
							"listingId");
				throw new ListingBusinessException(listingError);
			}
			
			if(newListing.getTicketMedium() != null) {
			    currentListing.setTicketMedium(newListing.getTicketMedium());
			}
			currentListing.setEndDate(newListing.getEndDate());
			currentListing.setFulfillmentMethod(getFulfillmentWindow(newListing));
			if(newListing.getLmsApprovalStatus() != null) {
				currentListing.setLmsApprovalStatus(newListing.getLmsApprovalStatus());
			}
			if(newListing.getSystemStatus() != null) {
				currentListing.setSystemStatus(newListing.getSystemStatus());
			}
			if(newListing.getFulfillmentDeliveryMethods() != null) {
				currentListing.setFulfillmentDeliveryMethods(newListing.getFulfillmentDeliveryMethods());
			}
			if(newListing.getDeliveryOption() != null) {
					currentListing.setDeliveryOption(newListing.getDeliveryOption());
			}
			if(newListing.getConfirmOption() != null) {
					currentListing.setConfirmOption(newListing.getConfirmOption());
			}

			if(newListing.getInhandDate() != null) {
					currentListing.setInhandDate(newListing.getInhandDate());
			}
			//if contactGuid has changed  and its not bulk listing and  the ticket is of the type paper ticket 
			//then make fulfillment window re evaluation call
		}else if(listing.getSellerContactGuid() != null && !listing.getSellerContactGuid().equals(currentListing.getSellerContactGuidOld()) 
				&& !isBulk && fulfillmentServiceHelper.isShipping(currentListing)){
			EventFulfillmentWindowResponse efwResponse = getCachedEventFulfillmentWindowResponse(currentListing);
			List<FulfillmentWindow> fulfillmentWindows = fulfillmentServiceAdapter.getFulfillmentWindows(efwResponse);
			ticketMediumToFulfillmentMethod(currentListing);
			if(!fulfillmentServiceHelper.populateFulfillmentOptions(currentListing, fulfillmentWindows)) {
				ListingError listingError = new ListingError(
						ErrorType.BUSINESSERROR, ErrorCode.DELIVERY_OPTION_NOT_SUPPORTED, "populateFulfillmentOptions return false",
							"listingId");
				throw new ListingBusinessException(listingError);
			}
		}else{
			//updating  only SaleEndDate
			if(listing.getEndDate() != null){
				Event event = currentListing.getEvent();
				Calendar eventCalendar = Calendar.getInstance(event.getJdkTimeZone());
				eventCalendar.set(Calendar.YEAR, listing.getEndDate().get(Calendar.YEAR));
				eventCalendar.set(Calendar.MONTH, listing.getEndDate().get(Calendar.MONTH));
				eventCalendar.set(Calendar.DAY_OF_MONTH, listing.getEndDate().get(Calendar.DAY_OF_MONTH));
				eventCalendar.set(Calendar.HOUR_OF_DAY, listing.getEndDate().get(Calendar.HOUR_OF_DAY));
				eventCalendar.set(Calendar.MINUTE, listing.getEndDate().get(Calendar.MINUTE));
				eventCalendar.set(Calendar.SECOND, listing.getEndDate().get(Calendar.SECOND));
				eventCalendar.set(Calendar.MILLISECOND, 000);
				Calendar calendar = DateUtil.convertCalendarToUtc(eventCalendar);				
				listing.setEndDate(calendar);				
				if(listing.getEndDate().before(DateUtil.getNowCalUTC())) {
					ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_SALE_ENDATE, "Invalid Sale End Date", "saleEndDate");
					throw new ListingBusinessException(listingError);
				}			
				currentListing.setFulfillmentMethod(getFulfillmentWindow(currentListing));
				SHAPIThreadLocal.set(apiContext);
				Calendar saleEndDate =fulfillmentServiceHelper.calculateSaleEndDate(currentListing, null);	
				if(saleEndDate == null){
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR, ErrorCode.DELIVERY_OPTION_NOT_SUPPORTED, "Sale date from fulfillmentServiceHelper cannot be null",
							"listingId");
					throw new ListingBusinessException(listingError);
				}else{
					log.debug("saleEndDate = TimeZone="+ saleEndDate.getTimeZone()+ " - " + saleEndDate.get(Calendar.DAY_OF_MONTH) + "/" + saleEndDate.get(Calendar.MONTH) + "/"+ saleEndDate.get(Calendar.YEAR) + "T" +saleEndDate.get(Calendar.HOUR_OF_DAY) + "-"+ saleEndDate.get(Calendar.MINUTE) + "-"+saleEndDate.get(Calendar.SECOND) + " Millisecond = " + saleEndDate.getTimeInMillis() );
					log.debug("listing.getEndDate() = TimeZone="+ listing.getEndDate().getTimeZone()+ " - "  + listing.getEndDate().get(Calendar.DAY_OF_MONTH) + "/" + listing.getEndDate().get(Calendar.MONTH) + "/"+ listing.getEndDate().get(Calendar.YEAR) + "T" +listing.getEndDate().get(Calendar.HOUR_OF_DAY) + "-"+ listing.getEndDate().get(Calendar.MINUTE) + "-"+listing.getEndDate().get(Calendar.SECOND) + " Millisecond = " + listing.getEndDate().getTimeInMillis());
					
					if(saleEndDate.before(listing.getEndDate())){
						// EXTSELL-2 the sale end date sent by the seller at the time of listing creation is after the fulfillment window, adjust the sale end date and accept the listing
						log.info("_message=\" Adujsting listing request end date with FF sale end date \""+ "saleEndDate ="+saleEndDate +" listing.getEndDate()="+listing.getEndDate());
						currentListing.setEndDate(listing.getEndDate());	
						
					}else{
						currentListing.setEndDate(listing.getEndDate());	
					}
				}
			}
		}
		if(listing.getInhandDate() != null) {
			if(currentListing.getDeliveryOption() == null || currentListing.getDeliveryOption().intValue() == DeliveryOption.MANUAL.getValue()) {
						Listing newListing = new Listing();
						newListing.setInhandDate(listing.getInhandDate());
						newListing.setAdjustInhandDate(listing.isAdjustInhandDate());
						newListing.setEvent(currentListing.getEvent());
						if(listing.getTicketMedium() != null) {
								newListing.setTicketMedium(listing.getTicketMedium());
						} else {
								newListing.setTicketMedium(currentListing.getTicketMedium());
						}
						newListing.setEventId(currentListing.getEventId());
						newListing.setSellerContactId(currentListing.getSellerContactId());
						newListing.setSellerContactGuid(currentListing.getSellerContactGuid());

						validateInHandDate(newListing, getCachedEventFulfillmentWindowResponse(newListing));

						if(newListing.getDeclaredInhandDate() != null) {
							listing.setDeclaredInhandDate(newListing.getDeclaredInhandDate());
						}
						if(newListing.getInhandDate() != null) {
							listing.setInhandDate(newListing.getInhandDate());
							listing.setInhandDateValidated(newListing.isInhandDateValidated());
							listing.setInHandDateAdjusted(newListing.isInHandDateAdjusted());
						 }

			} else if(currentListing.getDeliveryOption().intValue() == DeliveryOption.PREDELIVERY.getValue()) {
					listing.setInhandDate(currentListing.getInhandDate());
			}
		}

		log.debug("END ommon populateFulfillmentOptions task for update");
		return currentListing;
	}

	private void validateInHandDate(Listing listing, EventFulfillmentWindowResponse efwResponse) {
		if(efwResponse != null) {
			Calendar eihDate = null;
			Calendar lihDate = null;
			Map<String, EventInhanddate> inHandDateSettings = efwResponse.getInHandDateSettings();
			if(inHandDateSettings != null && listing.getTicketMedium() != null) {
				String eihDateString = null;
				String lihDateString = null;
				
				TicketMedium tm = TicketMedium.getTicketMedium(listing.getTicketMedium().intValue());
				String tmString = tm.name().toLowerCase();
				
				if(inHandDateSettings.get(tmString) != null) {
					eihDateString = inHandDateSettings.get(tmString).getEihd();
					lihDateString = inHandDateSettings.get(tmString).getLihd();
				}

				SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
				sdf.setLenient(false);
				sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
				try {
					if (eihDateString != null) {
						eihDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
						eihDate.setTime(sdf.parse(eihDateString));
					}
					if (lihDateString != null) {
						lihDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
						lihDate.setTime(sdf.parse(lihDateString));
					}
				} catch (ParseException e) {
					log.error("ParseException occurred while parsing eih/lih date eventId=" + listing.getEventId());
					ListingError listingError = new ListingError(ErrorType.SYSTEMERROR,
							ErrorCode.SYSTEM_ERROR, "System error occured", null);
					throw new ListingBusinessException(listingError);
				}
			} else {
				eihDate = efwResponse.getEarliestInHandDate();
				lihDate = efwResponse.getLatestInHandDate();
			}
			if(lihDate != null) {
				fulfillmentServiceHelper.validateAndSetInHandDate(listing, eihDate, lihDate);
			}
		}
	}
	
	private EventFulfillmentWindowResponse getCachedEventFulfillmentWindowResponse (Listing listing ) {
		if ( efwResponse == null ) {
			efwResponse = fulfillmentServiceAdapter.getFulfillmentWindowsShape(listing.getEventId(), 
					listing.getSellerContactId());
		}
		return efwResponse;
	}
	
	private static FulfillmentMethod getFulfillmentWindow(Listing listing) {
		FulfillmentMethod fulfillmentMethod = null;
		if(listing.getTicketMedium() != null) {
			if(listing.getTicketMedium() == TicketMedium.BARCODE.getValue()){
				fulfillmentMethod = FulfillmentMethod.BARCODE;
					 
			}else if(listing.getTicketMedium() == TicketMedium.PDF.getValue()){
				fulfillmentMethod = FulfillmentMethod.PDF;
			}else if(listing.getTicketMedium() == TicketMedium.PAPER.getValue()){			
				if(listing.getLmsApprovalStatus() != null){
					fulfillmentMethod = FulfillmentMethod.LMS;
				}else{
					fulfillmentMethod = FulfillmentMethod.UPS;
				}			
			}
		}
		return fulfillmentMethod;
	}
	
	  /**
	   * Convert listingTicketMedium to FulFillmentMethod
	   * 
	   * @param listing
	   * @param response
	   */
	public static void ticketMediumToFulfillmentMethod(Listing listing) {
		if(listing.getListingSource() != null && listing.getListingSource() == 8){
			listing.setFulfillmentMethod(FulfillmentMethod.BARCODEPREDELIVERYSTH);
		}
		if (listing.getTicketMedium() != null && TicketMedium.BARCODE.getValue() == listing.getTicketMedium().intValue()) {
			listing.setFulfillmentMethod(FulfillmentMethod.BARCODE);
		} else if (listing.getTicketMedium() != null && TicketMedium.PDF.getValue() == listing.getTicketMedium().intValue()) {
			listing.setFulfillmentMethod(FulfillmentMethod.PDF);
		} else if (listing.getTicketMedium() != null && (TicketMedium.FLASHSEAT.getValue() == listing.getTicketMedium().intValue() || TicketMedium.EXTFLASH.getValue() == listing.getTicketMedium().intValue())) {
			listing.setFulfillmentMethod(FulfillmentMethod.FLASHSEAT);
		} else if (listing.getTicketMedium() != null && TicketMedium.MOBILE.getValue() == listing.getTicketMedium().intValue()) {
			listing.setFulfillmentMethod(FulfillmentMethod.MOBILE);
		} else if (listing.getTicketMedium() != null && TicketMedium.EXTMOBILE.getValue() == listing.getTicketMedium().intValue()) {
			listing.setFulfillmentMethod(FulfillmentMethod.MOBILETRANSFER);
		} else if (listing.getTicketMedium() != null && TicketMedium.EVENTCARD.getValue() == listing.getTicketMedium().intValue()) {
			listing.setFulfillmentMethod(FulfillmentMethod.EVENTCARD);
		} else if (listing.getTicketMedium() != null && TicketMedium.SEASONCARD.getValue() == listing.getTicketMedium().intValue()) {
			listing.setFulfillmentMethod(FulfillmentMethod.SEASONCARD);
		} else if (listing.getTicketMedium() != null && TicketMedium.RFID.getValue() == listing.getTicketMedium().intValue()) {
			listing.setFulfillmentMethod(FulfillmentMethod.RFID);
		} else if (listing.getTicketMedium() != null && TicketMedium.WRISTBAND.getValue() == listing.getTicketMedium().intValue()) {
			listing.setFulfillmentMethod(FulfillmentMethod.WRISTBAND);
		} else if (listing.getTicketMedium() != null && TicketMedium.GUESTLIST.getValue() == listing.getTicketMedium().intValue()) {
			listing.setFulfillmentMethod(FulfillmentMethod.GUESTLIST);
		} else if (listing.getFulfillmentDeliveryMethods() != null) {
			String fmDMList = listing.getFulfillmentDeliveryMethods();
			if (fmDMList.contains("|11,") || fmDMList.contains("|12,") || fmDMList.contains("|15,") || fmDMList.startsWith("11,")
					|| fmDMList.startsWith("12,") || fmDMList.startsWith("15,")) {
				listing.setFulfillmentMethod(FulfillmentMethod.SHIPPING);
			} else if (fmDMList.contains("|10,") || fmDMList.startsWith("10,")) {
				listing.setFulfillmentMethod(FulfillmentMethod.UPS);
			} else if (fmDMList.contains("|9,") || fmDMList.contains("|7,") || fmDMList.startsWith("9,")
					|| fmDMList.startsWith("7,")) {
				listing.setFulfillmentMethod(FulfillmentMethod.LMS);
			} else if (fmDMList.contains("|17,") || fmDMList.startsWith("17,")) {
				listing.setFulfillmentMethod(FulfillmentMethod.LOCALDELIVERY);
			} else if (fmDMList.contains("|8,") || fmDMList.startsWith("8,")) {
				listing.setFulfillmentMethod(FulfillmentMethod.OTHERPREDELIVERY);
			}
		}
	}

	@Override
	public boolean ifNeedToRunTask() {
		return true;
	}
}