package com.stubhub.domain.inventory.listings.v2.helper;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.PDFTicketMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.PDFTicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.entity.VenueConfigSectionOrZone;
import com.stubhub.domain.inventory.listings.v2.util.InventorySolrUtil;
import com.stubhub.domain.inventory.listings.v2.util.ListingSeatTraitsHelper;
import com.stubhub.domain.inventory.listings.v2.util.SeatProductsContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;

public class SeatsAndTraitsManipulator 
{
	private final static Logger log = Logger.getLogger(SeatsAndTraitsManipulator.class);

	/**
	 * Process seat traits and return current Listing modified
	 * @throws UnsupportedEncodingException 
	 */
	public static Listing processSeatsTraits (Listing listing, Listing currentListing,
			SHAPIContext apiContext, SeatProductsContext seatProdContext,
			ListingSeatTraitsHelper listingSeatTraitsHelper,
			TicketSeatMgr ticketSeatMgr, InventoryMgr inventoryMgr,  
			PDFTicketMgr pdfTicketMgr, 
			InventorySolrUtil inventorySolrUtil ) throws UnsupportedEncodingException
	{
		log.debug("START TicketSeatTraitUpdateTask async task");
		SHAPIThreadLocal.set(apiContext);
		int modifiedQuantityCount = 0;
		
		if (listing.getQuantity() != null) {
			modifiedQuantityCount = currentListing.getQuantityRemain() - listing.getQuantity();
		}
				
		List<TicketSeat> curTicketSeats = currentListing.getTicketSeats();
		Boolean checkDuplicateSRSInd = false;
		VenueConfigSectionOrZone venueConfigSectionOrZone = new VenueConfigSectionOrZone();
		// update section if changes
		if ( (StringUtils.trimToNull(listing.getSection()) != null) ) {
				
			if (listing.getEvent().getSectionScrubbing() || listing.getEvent().getRowScrubbing()) {
				if (inventoryMgr.hasSectionHadBadTerms(listing.getSection().trim())) {
					ListingError listingError = new ListingError(
							ErrorType.INPUTERROR,
							ErrorCode.INVALID_SECTION_WORDS, 
							"Section contains words that are not allowed", "section");
					throw new ListingBusinessException(listingError);
				}

				//SELLAPI-1546
				if(currentListing.getVenueConfigSectionsId() == null || !listing.getSection().equals(currentListing.getSection())){				
					log.debug("Scrubbing the section eventId=" + listing.getEventId());
					Long venueConfigSectionsId = 0L;
					Long venueConfigZoneId = 0L;
					
					String row = null;
					if(listing.getIsPiggyBack() == false && currentListing.getIsPiggyBack() == false) {
						if(StringUtils.trimToNull(listing.getRow()) != null){
							row = listing.getRow();
						}
						else{
							row = currentListing.getRow();
						}
						venueConfigSectionOrZone = listingSeatTraitsHelper.getVenueConfigSectionOrZoneId(listing.getEvent().getVenueConfigId(),
								listing.getSection(),row,null, listing.getEvent().getCountry(), listing.getEvent().getEnableHybridMap());						
					} else {
						String[] rows = null;
						if(StringUtils.trimToNull(listing.getRow()) != null){
							rows = listing.getRow().split(",");
						}else if(StringUtils.trimToNull(currentListing.getRow()) != null) {
							rows = currentListing.getRow().split(",");
						}
						venueConfigSectionOrZone = listingSeatTraitsHelper.getVenueConfigSectionOrZoneId(listing.getEvent().getVenueConfigId(),
								listing.getSection(),rows[0],rows[1], listing.getEvent().getCountry(), listing.getEvent().getEnableHybridMap());
					}
					venueConfigSectionsId = venueConfigSectionOrZone.getVenueConfigSectionId();
					if(venueConfigSectionsId!=null && venueConfigSectionsId > 0) {
						currentListing.setVenueConfigSectionsId(venueConfigSectionsId);
						listing.setVenueConfigSectionsId(venueConfigSectionsId);
					} else {
						listing.setVenueConfigSectionsId(null);
						currentListing.setVenueConfigSectionsId(null);
					}
					
					venueConfigZoneId = venueConfigSectionOrZone.getVenueConfigZoneId();
					if(venueConfigZoneId!=null && venueConfigZoneId > 0) {
						currentListing.setVenueConfigZoneId(venueConfigZoneId);
						listing.setVenueConfigZoneId(venueConfigZoneId);
					} else {
						listing.setVenueConfigZoneId(null);
						currentListing.setVenueConfigZoneId(null);
					}
					
					if((venueConfigSectionsId!=null && venueConfigSectionsId > 0) ||
						(venueConfigZoneId!=null && venueConfigZoneId > 0)	){
						currentListing.setSectionScrubSchedule(false);
						listing.setSectionScrubSchedule(false);
					}else{
						listing.setSectionScrubSchedule(true);
						currentListing.setSectionScrubSchedule(true);
					}
				}
				
			}
			
			if ( !listing.getSection().equals(currentListing.getSection()) ) {			  
				if (currentListing.getSystemStatus().equals(
						ListingStatus.INCOMPLETE.name()) || (listing.getSystemStatus() != null && listing.getSystemStatus().equals(ListingStatus.INCOMPLETE.name()))) 
				{
					if (curTicketSeats == null)
						curTicketSeats = ticketSeatMgr.findTicketSeatsByTicketId(currentListing.getId());
					
					for (int i = 0; i < curTicketSeats.size(); i++) {
						TicketSeat seat = curTicketSeats.get(i);
						//SELLAPI-1547. update seat only if it is not in SOLD status.
						if(seat.getSeatStatusId() != null && seat.getSeatStatusId().longValue() != 3){
							if (CommonConstants.GENERAL_ADMISSION.equals(listing.getSection())) {
								seat.setGeneralAdmissionInd(true);
							}
							seat.setSection(listing.getSection().trim());
						}
					}
					currentListing.setSection(listing.getSection().trim());
					currentListing.setTicketSeats(curTicketSeats);
					checkDuplicateSRSInd = true;
				} 
				else {
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR,
							ErrorCode.LISTING_ACTION_NOTALLOWED, "Cannot change section for listing status != INCOMPLETE", "section");
					throw new ListingBusinessException(listingError);
				}
			}
		}
		
		//SELLAPI-1135 sonar-rules, Expressions should not be too complex
		// donot allow splits/quantity change for single file uploads
		if (currentListing.getId() != null && isSingleFileUploadWithSplit(listing, currentListing, modifiedQuantityCount)) {
			List<PDFTicketSeat> pdsSeats = pdfTicketMgr.findPDFTicketSeats(currentListing.getId());
			if (pdsSeats.size() == 0) {
				ListingError listingError = new ListingError(
						ErrorType.BUSINESSERROR,
						ErrorCode.UPDATE_NOTALLOWED_SINGLE_PDF_FILE, "Update not allowed",
						"quantity");
				throw new ListingBusinessException(listingError);
			}
		}
		
		// update SplitOption and SplitQuantity
		if ( listing.getSplitOption() != null || listing.getSplitQuantity() != null ) {
			
			// merge passed with current listing
			if ( listing.getSplitOption() == null )
				listing.setSplitOption(currentListing.getSplitOption());
			
			if ( listing.getSplitQuantity() == null )
				listing.setSplitQuantity(currentListing.getSplitQuantity());
			
			// update current listing values
			if ( listing.getSplitOption() == 2) {
				currentListing.setSplitOption((short) 2);
				currentListing.setSplitQuantity(1);
			}
			else if ( listing.getSplitOption() == 0) { 
				currentListing.setSplitQuantity(currentListing.getQuantityRemain());
				currentListing.setSplitOption((short) 0);
			} 
			else if ( listing.getSplitOption() == 1) { 
				currentListing.setSplitQuantity(listing.getSplitQuantity());
				//SELLAPI-1135 sonar-rules, Expressions should not be too complex				
				if(currentListing.getSplitQuantity() == 3 && currentListing.getQuantityRemain() >= 3){
					currentListing.setSplitQuantity(currentListing.getSplitQuantity());
					currentListing.setSplitOption((short) 1);	
				}else if(currentListing.getSplitQuantity() == 2 && currentListing.getQuantityRemain() >= 2) {
					currentListing.setSplitQuantity(currentListing.getSplitQuantity());
					currentListing.setSplitOption((short) 1);	
				}else if(currentListing.getQuantityRemain() % currentListing.getSplitQuantity() == 0){
					currentListing.setSplitQuantity(currentListing.getSplitQuantity());
					currentListing.setSplitOption((short) 1);
				}else {
					ListingError listingError = new ListingError(
							ErrorType.INPUTERROR, ErrorCode.INVALID_SPLIT_VALUE,
							"", "splitQuantity");
					throw new ListingBusinessException(listingError);
				}
			}
		} 
		
		if(currentListing.getIsPiggyBack()) {
		    currentListing.setSplitOption((short) 0);
            currentListing.setSplitQuantity(currentListing.getQuantityRemain());
		}
		
		// if no split option at this point, simply set to default
		if ( currentListing.getSplitOption() == null ) {
			currentListing.setSplitOption( (short)2 );
			currentListing.setSplitQuantity(1);
		}
		
		// process seat traits
		listingSeatTraitsHelper.processSeatTraits ( seatProdContext );
		
		if(venueConfigSectionOrZone.isGeneralAdmission()) {
		  for (TicketSeat currentTicketSeat : currentListing.getTicketSeats()) {
		    currentTicketSeat.setGeneralAdmissionInd(true);
	      }
        }
		
		return currentListing;
	}

	public static boolean isSingleFileUploadWithSplit(Listing listing, Listing currentListing,
			int modifiedQuantityCount) {
		if(currentListing.getTicketMedium() == TicketMedium.PDF.getValue()
				&& currentListing.getDeliveryOption() == DeliveryOption.PREDELIVERY.getValue()){
			if(modifiedQuantityCount > 0){
				return true;
			}else if((listing.getSplitOption() != null && listing.getSplitOption() > (short)0)
					|| (listing.getSplitQuantity() != null && listing.getSplitQuantity() > 0)){
				return true;
			}
		}
		return false;
		
	}
}

