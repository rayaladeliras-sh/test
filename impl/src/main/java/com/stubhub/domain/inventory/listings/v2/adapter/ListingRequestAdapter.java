package com.stubhub.domain.inventory.listings.v2.adapter;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.entity.SaleMethod;
import com.stubhub.domain.inventory.common.entity.SplitOption;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.FulfillmentMethod;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingSource;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.util.TicketSeatUtils;
import com.stubhub.domain.inventory.listings.v2.util.TimeUtils;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.DTO.TicketTrait;
import com.stubhub.newplatform.common.util.DateUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListingRequestAdapter 
{
	private final static Logger LOG = LoggerFactory.getLogger(ListingRequestAdapter.class);

	private static final Integer MANUAL_CONFIRM = 3;
	
	private static final List<String> FLS_TRAIT_IDS = Arrays.asList("13688", "13701", "14911", "14907");
	private static final List<String> XFER_TRAIT_IDS = Arrays.asList("14912", "14913");
	private static final List<String> MOBILE_TRAIT_IDS = Arrays.asList("14699", "15070", "15291");
	private static final List<String> GC_TRAIT_IDS = Arrays.asList("14800", "14799", "14899");
	private static final List<String> LOCALDELIVERY_TRAIT_IDS = Arrays.asList("14910");

	public static Listing convert(ListingRequest request)
	{
		return convert ( request, false, null ); 
	}
	
	public static Listing convert(ListingRequest request, boolean isCreate) {
		return convert(request, isCreate, null);
	}
	
	public static Listing convert(ListingRequest request, boolean isCreate, Event event)   
	{
		Listing listing = new Listing();
		
		// UPDATE MODE: if update mode set some ids found in request
		if ( !isCreate ) {
			if ( request.getListingId() != null )
				listing.setId(request.getListingId());
		}
		
		try {
			// always place the eventId in listing if passed
			if ( request.getEventId() != null ) {
				listing.setEventId(Long.valueOf(request.getEventId()));
			}
		} catch (NumberFormatException e) {
			ListingError error = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_EVENTID, "EventId is expected to be numeric", "eventId");
			throw new ListingBusinessException (error);
		}

		if ( request.getPayoutPerProduct() != null ) {
			listing.setSellerPayoutAmountPerTicket(request.getPayoutPerProduct());
		}
		if ( request.getPricePerProduct() != null ) {
			listing.setListPrice(request.getPricePerProduct());
		}

		if (request.getPurchasePricePerProduct() != null
			&& request.getPurchasePricePerProduct().getAmount() != null) {

			listing.setPurchasePricePerProduct(request.getPurchasePricePerProduct());

			if (request.getPurchasePricePerProduct().getCurrency() != null) {
				listing.setPurchasePriceCurrency(
					Currency.getInstance(request.getPurchasePricePerProduct().getCurrency()));
			}
		}

		if (request.getSalesTaxPaid() != null) {
			listing.setSalesTaxPaid(request.getSalesTaxPaid());
		} else {
			listing.setSalesTaxPaid(true);
		}

		if(request.getBuyerSeesPerProduct() != null && request.getBuyerSeesPerProduct().getAmount() != null) {
			listing.setDisplayPricePerTicket(request.getBuyerSeesPerProduct());
			
			// TODO: obsolete
			listing.setMaxPricePerTicket(request.getBuyerSeesPerProduct());
			listing.setMinPricePerTicket(request.getBuyerSeesPerProduct());  
		}
		if(request.getFaceValue() != null && request.getFaceValue().getAmount() != null) {
			listing.setFaceValue(request.getFaceValue());
		}
		if(request.getPurchasePrice() != null && request.getPurchasePrice().getAmount() != null) {
			listing.setTicketCost(request.getPurchasePrice());
		}
		
		// pass quantity onto the listing
		if (request.getQuantity() !=null ) {
			listing.setQuantity( request.getQuantity() );
			if (isCreate ) {
				listing.setQuantityRemain(listing.getQuantity());
			}
		}
		
		//set hide seat info ind
		if (request.isHideSeats() != null && request.isHideSeats().booleanValue()) {
			listing.setHideSeatInfoInd(Boolean.TRUE);
		} else {
			listing.setHideSeatInfoInd(Boolean.FALSE);
		}
		//auto pricing
		if (request.isAutoPricingEnabledInd() != null && request.isAutoPricingEnabledInd().booleanValue()) {
			listing.setAutoPricingEnabledInd(Boolean.TRUE);
		} else {
			listing.setAutoPricingEnabledInd(Boolean.FALSE);
		}
		//Changes for EXTSELL-168
		listing.setComments(request.getComments());
		listing.setSellerInternalNote(request.getInternalNotes());
		listing.setSellerComments(request.getComments());

		com.stubhub.domain.inventory.v2.enums.TicketMedium ticketMedium = request.getTicketMedium();
		if (ticketMedium != null) {		
		    listing.setTicketMedium(ticketMedium.getId());
		}
		
		Integer lmsApprovalStatus = request.getLmsApprovalStatus();
		if (lmsApprovalStatus != null) {
			
			listing.setIsLmsApproval(true);
			listing.setLmsApprovalStatus(lmsApprovalStatus);
		}
		
		// map TicketMedium and set to defaults if not set		
		if ( !mapDeliveryOption(listing, request)  && isCreate ) {
			listing.setTicketMedium(TicketMedium.PAPER.getValue());
			listing.setConfirmOption(MANUAL_CONFIRM);
			
			// No delivery option and isCreate, then set isElectronicDelivery flag
			listing.setIsElectronicDelivery(request.getIsElectronicDelivery());
		}
		
		// For now delivery option is always manual as default 
		if ( isCreate && listing.getDeliveryOption() == null ) {
			if (isArtifactExists(request)) {
				listing.setDeliveryOption(com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.PREDELIVERY.getValue());
			} else {
				listing.setDeliveryOption(com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.MANUAL.getValue());
			}
		}
		
		// inhand date
		if(StringUtils.trimToNull(request.getInhandDate()) != null) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar inhandDate = Calendar.getInstance();
				inhandDate.setTime(sdf.parse(request.getInhandDate()));
				listing.setInhandDate(inhandDate);
			} catch (ParseException e) {
				ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_DATE_FORMAT, "Invalid date format", "inhandDate");
				throw new ListingBusinessException(listingError);
			}
		}
		// sale end date
		listing.setSaleEndDateIndicator(Boolean.FALSE);
		if(StringUtils.trimToNull(request.getSaleEndDate()) != null) {
			try {
				SimpleDateFormat sdf = null;
				if(request.getSaleEndDate().contains("T")) {
					sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				} else {
					sdf = new SimpleDateFormat("yyyy-MM-dd");
				}
				Calendar saleEndDate = Calendar.getInstance();
				
				Date dSaleEndDate = sdf.parse(request.getSaleEndDate());
				saleEndDate.setTime(dSaleEndDate);
				
				listing.setEndDate(saleEndDate);
				listing.setSaleEndDateIndicator(Boolean.TRUE);
								
				checkNewValueOfSaleEndDate(event, dSaleEndDate);
			} catch (ParseException e) {
				ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_DATE_FORMAT, "Invalid date format", "saleEndDate");
				throw new ListingBusinessException(listingError);
			}
		}

		listing.setSplitQuantity(request.getSplitQuantity());
		listing.setExternalId(request.getExternalListingId());
		listing.setSellerContactId(request.getContactId());
		listing.setSellerContactGuid(request.getContactGuid());

		
		if ( isCreate ) {
			Calendar utcNow = DateUtil.getNowCalUTC();
			listing.setCreatedDate(utcNow);
		}

		listing.setCcGuid(request.getCcId());
		listing.setSellerPaymentTypeId(request.getPaymentType());			

		// get status from passed request
		if (request.getStatus() != null) {
			if(request.getStatus().equals(ListingStatus.ACTIVE)){
				listing.setSystemStatus(request.getStatus().name());
				listing.setSellerRequestedStatus(request.getStatus().name());
			}
			else if(request.getStatus().equals(ListingStatus.INACTIVE)){
				listing.setSystemStatus(request.getStatus().name());
				listing.setSellerRequestedStatus(request.getStatus().name());
			}
			else if(request.getStatus().equals(ListingStatus.DELETED)){
				listing.setSystemStatus(request.getStatus().name());
				listing.setSellerRequestedStatus(request.getStatus().name());
			}
			else if(request.getStatus().equals(ListingStatus.PENDING)){
				listing.setSystemStatus(request.getStatus().name());
				listing.setSellerRequestedStatus(request.getStatus().name());
			}
			else if(request.getStatus().equals(ListingStatus.HIDDEN)){
				listing.setSystemStatus(request.getStatus().name());
				listing.setSellerRequestedStatus(request.getStatus().name());
				if(isCreate) {
					// 5 indicates CS action through Stubtex
					listing.setListingSource(ListingSource.Substitution.getId()); 
				}
			}
			else if (request.getStatus().equals(ListingStatus.INCOMPLETE)) {
				listing.setSystemStatus(request.getStatus().name());
				listing.setSellerRequestedStatus(request.getStatus().name());
			}
		}
		// default status == ACTIVE (only for create request)		
		if (isCreate && listing.getSystemStatus()==null) {
			listing.setSystemStatus(ListingStatus.ACTIVE.name());
			listing.setSellerRequestedStatus(ListingStatus.ACTIVE.name());
		}
		
		// some additional create defaults
		if ( isCreate ) {
			listing.setSellerCobrand("www");
			listing.setSaleMethod(SaleMethod.FIXED.getValue());			
		}
		
		if (request.getSplitOption() != null) {
			if (request.getSplitOption() == SplitOption.NOSINGLES) {
				listing.setSplitQuantity(1);
				listing.setSplitOption((short)2);
			} else if (request.getSplitOption() == SplitOption.NONE) {
				listing.setSplitQuantity(0);
				listing.setSplitOption((short)0);
			} else if (request.getSplitOption() == SplitOption.MULTIPLES) {
				listing.setSplitOption((short)1);
				if (listing.getSplitQuantity() == null) {
					listing.setSplitQuantity(1);
				}
			}
		}
		
		// set product ticket rows so we can do the price lookup
		if ( isCreate && request.getProducts()!=null && request.getProducts().size()>0 ) {
			listing.setQuantity( new Integer(request.getProducts().size()) );
			listing.setQuantityRemain(listing.getQuantity());
			
			String row = "";
			StringBuilder rows = new StringBuilder ();
			for ( Product prod: request.getProducts() ) {
				if ( !row.equals(prod.getRow())) {
					row = prod.getRow();
					rows.append(row).append(',');
				}
			}
			// if there are rows
			if ( rows.length() > 0 ) { 
				rows.setLength( rows.length() - 1 );
				listing.setRow( rows.toString() );
			}	
			// if no rows, allow no rows only for STH listing
			else if ( listing.getListingSource()==null || !listing.getListingSource().equals(8) ) {
				ListingError listingError = new ListingError(ErrorType.INPUTERROR, 
						ErrorCode.INVALID_ROW, "Invalid/missing row in product format", "products");
				throw new ListingBusinessException(listingError);
			}
		}
		
		listing.setSection(request.getSection());
		
		// if create, default section == General Admission values
		if ( isCreate ) {
			if ( StringUtils.trimToNull(listing.getSection()) == null ) {
				listing.setSection(CommonConstants.GENERAL_ADMISSION);
				listing.setSeats(CommonConstants.GENERAL_ADMISSION);				
				listing.setRow( CommonConstants.GA_ROW_DESC );
			}
			else if ( TicketSeatUtils.isGASection(listing.getSection())) {
				listing.setSeats(CommonConstants.GENERAL_ADMISSION);				
				listing.setRow( CommonConstants.GA_ROW_DESC );
			}
		}
		
		listing.setTealeafSessionGuid(request.getTealeafSessionId());
		listing.setThreatMatrixRefId(request.getThreatMatrixSessionId());
		
		if(Boolean.TRUE.equals(request.getLmsExtension())) {
		    listing.setLmsExtensionRequired(true);
		}
		if(DeliveryOption.LMS.equals(request.getDeliveryOption()) && listing.getTicketMedium() != null && TicketMedium.PAPER.getValue() != listing.getTicketMedium()) {
		    listing.setLmsExtensionRequired(true); // for seasoncard when lms is requested
		}
		
		if(request.getAdjustPrice()!=null && request.getAdjustPrice()){
			listing.setAdjustPrice(true);
		}
		if(request.isAdjustInhandDate()!=null && !request.isAdjustInhandDate()){
			listing.setAdjustInhandDate(false);
		}else{
			listing.setAdjustInhandDate(true);
		}
		//MarkUp changes
		if(request.isMarkup()!=null && request.isMarkup()){
			LOG.debug("_message=\"Markup field present in ListingRequest\" eventId={}", request.getEventId());
			listing.setMarkup(true);
		}
		// Ticket Class Support
		listing.setTicketClass(request.getTicketClass());
		return listing;
	}
	
	static void checkNewValueOfSaleEndDate(Event event, Date endDate) {
		
		if (event == null) return;
		
		Calendar cEventDate = event.getEventDate();
		
		if (cEventDate == null) return;
		Date eventDate = cEventDate.getTime();
		
		if (!TimeUtils.isValidSaleEndDate(eventDate, endDate)) {
			ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_SALE_ENDATE, "Invalid value of Sale End Date", "saleEndDate");
			throw new ListingBusinessException(listingError);
		}
	}
	
	/**
	 * mapDeliveryOption
	 * @param listing
	 * @param request
	 */
	private static boolean mapDeliveryOption(Listing listing, ListingRequest request) {
		DeliveryOption deliveryOption = request.getDeliveryOption();
		boolean isArtifactExists = isArtifactExists(request);
		
		List<String> ticketTraitIds = new ArrayList<String>();
        if(request.getTicketTraits() != null && !request.getTicketTraits().isEmpty()) {
            for(TicketTrait tt : request.getTicketTraits()) {
                if(StringUtils.trimToNull(tt.getId()) != null) {
                    ticketTraitIds.add(tt.getId());
                }
            }
        }
		if(deliveryOption != null) {
			if (deliveryOption.equals(DeliveryOption.FLASHSEAT)) {
				listing.setFulfillmentMethod(FulfillmentMethod.FLASHSEAT);
				listing.setTicketMedium(TicketMedium.FLASHSEAT.getValue());
			}
			else if(deliveryOption.equals(DeliveryOption.MOBILETRANSFER)) {
				listing.setFulfillmentMethod(FulfillmentMethod.MOBILETRANSFER);
				listing.setTicketMedium(TicketMedium.EXTMOBILE.getValue());
			}
			else if(deliveryOption.equals(DeliveryOption.MOBILE)) {
				listing.setFulfillmentMethod(FulfillmentMethod.MOBILE);
				listing.setTicketMedium(TicketMedium.MOBILE.getValue());
			}
			else if(deliveryOption.equals(DeliveryOption.LOCALDELIVERY)) {
			    listing.setFulfillmentMethod(FulfillmentMethod.LOCALDELIVERY);
                listing.setTicketMedium(TicketMedium.PAPER.getValue());
            }
			else if (deliveryOption.equals(DeliveryOption.UPS)) {
				listing.setFulfillmentMethod(FulfillmentMethod.UPS);
				listing.setTicketMedium(TicketMedium.PAPER.getValue());
			}
			else if (deliveryOption.equals(DeliveryOption.SHIPPING)) {
				listing.setFulfillmentMethod(FulfillmentMethod.SHIPPING);
				listing.setTicketMedium(TicketMedium.PAPER.getValue());
			}
			else if (deliveryOption.equals(DeliveryOption.LMS)) {
                listing.setFulfillmentMethod(FulfillmentMethod.LMS);
                listing.setTicketMedium(TicketMedium.PAPER.getValue());
            }
			else if (deliveryOption.equals(DeliveryOption.FEDEX)) {
				listing.setFulfillmentMethod(FulfillmentMethod.FEDEX);
				listing.setTicketMedium(TicketMedium.PAPER.getValue());
			} 
			else if (deliveryOption.equals(DeliveryOption.PDF)) {
				listing.setFulfillmentMethod(FulfillmentMethod.PDF);
				listing.setTicketMedium(TicketMedium.PDF.getValue());
				deriveFulfillmentMethodFromCommentsElectronic(request.getComments(), ticketTraitIds, listing);
			}
			else if (deliveryOption.equals(DeliveryOption.BARCODE) || deliveryOption.equals(DeliveryOption.STH)) {
				listing.setFulfillmentMethod(FulfillmentMethod.BARCODE);
				listing.setTicketMedium(TicketMedium.BARCODE.getValue());
				
				if(deliveryOption.equals(DeliveryOption.STH)) {
					listing.setListingSource(8); //STHGen3
					listing.setFulfillmentMethod(FulfillmentMethod.BARCODEPREDELIVERYSTH);
				}
				if(deliveryOption.equals(DeliveryOption.BARCODE) && isFlash(request.getComments(), ticketTraitIds)) {
		            listing.setFulfillmentMethod(FulfillmentMethod.FLASHSEAT);
		            listing.setTicketMedium(TicketMedium.FLASHSEAT.getValue());
				}
			}
			else {
				return false;
			}
			FulfillmentMethod fm = listing.getFulfillmentMethod();
			if(FulfillmentMethod.UPS.equals(fm) || FulfillmentMethod.SHIPPING.equals(fm) || FulfillmentMethod.LMS.equals(fm) || FulfillmentMethod.LOCALDELIVERY.equals(fm)) {
			    listing.setPaperFulfillmentMethod(fm);
			    deriveFulfillmentMethodFromCommentsPaper(request.getTicketMedium(), request.getComments(), ticketTraitIds, listing);
			}
			if(isArtifactExists) {
				LOG.info("_message=\"Artifacts exist for this request, so setting the delivery option to PREDELIVERY\"");
				listing.setDeliveryOption(com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.PREDELIVERY.getValue());
			}
			return true;
		}
		else {
			boolean isDeliveryOptionSet = deriveFulfillmentMethodFromCommentsElectronic(request.getComments(), ticketTraitIds, listing);
			if(!isDeliveryOptionSet) {
			    isDeliveryOptionSet = deriveFulfillmentMethodFromCommentsPaper(request.getTicketMedium(), request.getComments(), ticketTraitIds, listing);
			}
			if(isArtifactExists) {
				LOG.info("_message=\"Artifacts exist for this request, so setting the delivery option to PREDELIVERY\"");
				listing.setDeliveryOption(com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.PREDELIVERY.getValue());
				isDeliveryOptionSet = true;
			}
			return isDeliveryOptionSet;
		}
	}
	
	private static boolean deriveFulfillmentMethodFromCommentsElectronic(String comments, List<String> ticketTraitIds, Listing listing) {
		if(StringUtils.trimToNull(comments) == null && ticketTraitIds.isEmpty()) {
			return false;
		}
		if(isFlash(comments, ticketTraitIds)) {
			listing.setFulfillmentMethod(FulfillmentMethod.FLASHSEAT);
			listing.setTicketMedium(TicketMedium.FLASHSEAT.getValue());
		} else if(isMobileTransfer(comments, ticketTraitIds)) {
			listing.setFulfillmentMethod(FulfillmentMethod.MOBILETRANSFER);
			listing.setTicketMedium(TicketMedium.EXTMOBILE.getValue());
		} else if(isMobile(comments, ticketTraitIds)) {
			listing.setFulfillmentMethod(FulfillmentMethod.MOBILE);
			listing.setTicketMedium(TicketMedium.MOBILE.getValue());
		} else {
			return false;
		}
		return true;
	}
	
	private static boolean deriveFulfillmentMethodFromCommentsPaper(com.stubhub.domain.inventory.v2.enums.TicketMedium ticketMedium, String comments, List<String> ticketTraitIds, Listing listing) {
        if(StringUtils.trimToNull(comments) == null && ticketTraitIds.isEmpty() && ticketMedium == null) {
            return false;
        }
        if(com.stubhub.domain.inventory.v2.enums.TicketMedium.EVENTCARD.equals(ticketMedium) || isEventCard(comments, ticketTraitIds)) {
            listing.setFulfillmentMethod(FulfillmentMethod.EVENTCARD);
            listing.setTicketMedium(TicketMedium.EVENTCARD.getValue());
        } else if(com.stubhub.domain.inventory.v2.enums.TicketMedium.SEASONCARD.equals(ticketMedium) || isSeasonCard(comments)) {
          listing.setFulfillmentMethod(FulfillmentMethod.SEASONCARD);
          listing.setTicketMedium(TicketMedium.SEASONCARD.getValue());
        } else if(com.stubhub.domain.inventory.v2.enums.TicketMedium.WRISTBAND.equals(ticketMedium) || isWristband(comments)) {
            listing.setFulfillmentMethod(FulfillmentMethod.WRISTBAND);
            listing.setTicketMedium(TicketMedium.WRISTBAND.getValue());
        } else if(com.stubhub.domain.inventory.v2.enums.TicketMedium.RFID.equals(ticketMedium) || isRFID(comments)) {
            listing.setFulfillmentMethod(FulfillmentMethod.RFID);
            listing.setTicketMedium(TicketMedium.RFID.getValue());
        } else if(com.stubhub.domain.inventory.v2.enums.TicketMedium.GUESTLIST.equals(ticketMedium) || isGuestlist(comments)) {
            listing.setFulfillmentMethod(FulfillmentMethod.GUESTLIST);
            listing.setTicketMedium(TicketMedium.GUESTLIST.getValue());
        } else if(isLocalDelivery(comments, ticketTraitIds)) {
          listing.setFulfillmentMethod(FulfillmentMethod.LOCALDELIVERY);
          listing.setTicketMedium(TicketMedium.PAPER.getValue());
        } else {
            return false;
        }
        return true;
    }
	
	private static boolean isFlash(String comments, List<String> ticketTraitIds) {
		if(StringUtils.trimToNull(comments) != null) {
			if(comments.toLowerCase().contains("fls")) {
				return true;
			}
		}
		if(!ticketTraitIds.isEmpty() && !Collections.disjoint(ticketTraitIds, FLS_TRAIT_IDS)) {
		    return true;
		}
		return false;
	}
	
	private static boolean isMobileTransfer(String comments, List<String> ticketTraitIds) {
		if(StringUtils.trimToNull(comments) != null && comments.toLowerCase().contains("xfer")) {
			return true;
		}
		if(!ticketTraitIds.isEmpty() && !Collections.disjoint(ticketTraitIds, XFER_TRAIT_IDS)) {
            return true;
        }
		return false;
	}
	
	private static boolean isMobile(String comments, List<String> ticketTraitIds) {
		if(StringUtils.trimToNull(comments) != null && comments.toLowerCase().contains("mobile")) {
			return true;
		}
		if(!ticketTraitIds.isEmpty() && !Collections.disjoint(ticketTraitIds, MOBILE_TRAIT_IDS)) {
            return true;
        }
		return false;
	}
	
	private static boolean isEventCard(String comments, List<String> ticketTraitIds) {
	    if(StringUtils.trimToNull(comments) != null) {
            if(comments.toLowerCase().contains("gc") || comments.toLowerCase().contains("credit card")) {
                return true;
            }
	    }
	    if(!ticketTraitIds.isEmpty() && !Collections.disjoint(ticketTraitIds, GC_TRAIT_IDS)) {
            return true;
        }
        return false;
    }
	
	private static boolean isLocalDelivery(String comments, List<String> ticketTraitIds) {
        if(StringUtils.trimToNull(comments) != null) {
            if(comments.toLowerCase().contains("local delivery") || comments.toLowerCase().contains("local pickup") 
            		|| comments.toLowerCase().contains("lpu")) {
                return true;
            }
        }
        if(!ticketTraitIds.isEmpty() && !Collections.disjoint(ticketTraitIds, LOCALDELIVERY_TRAIT_IDS)) {
            return true;
        }
        return false;
    }
	
	private static boolean isSeasonCard(String comments) {
		if(StringUtils.trimToNull(comments) != null) {
			if(comments.toLowerCase().contains("seasoncard")) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isWristband(String comments) {
		if(StringUtils.trimToNull(comments) != null) {
			if(comments.toLowerCase().contains("wristband")) {
				return true;
			}
		}
		return false;
	}

	private static boolean isRFID(String comments) {
		if(StringUtils.trimToNull(comments) != null) {
			if(comments.toLowerCase().contains("rfid")) {
				return true;
			}
		}
		return false;
	}

	private static boolean isGuestlist(String comments) {
		if(StringUtils.trimToNull(comments) != null) {
			if(comments.toLowerCase().contains("guestlist")) {
				return true;
			}
		}
		return false;
	}

	public static boolean isArtifactExists(ListingRequest request) {
		boolean isArtifactExists =false;
		List <Product> products =request.getProducts();
		if(products !=null && products.size()>0){
			for (Product product : products) {
				if (product.getFulfillmentArtifact() != null && !product.getFulfillmentArtifact().trim().equals("")) {
					isArtifactExists = true;
				} else {
					isArtifactExists = false;
				}
			}
		}
		
		LOG.info("_message=\"Artifacts in the request check\" isArtifactExists={} ", isArtifactExists);
		return isArtifactExists;
	}
	
}
