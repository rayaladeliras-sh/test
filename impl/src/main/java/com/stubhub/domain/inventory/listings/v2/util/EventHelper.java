package com.stubhub.domain.inventory.listings.v2.util;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.catalog.events.intf.TicketTrait;
import com.stubhub.domain.catalog.read.v3.intf.common.dto.response.AncestorItem;
import com.stubhub.domain.catalog.read.v3.intf.common.dto.response.CommonAttribute;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.SeatTrait;
import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.SeatingSection;
import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.SeatingZone;
import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfiguration;
import com.stubhub.domain.common.util.StringUtils;
import com.stubhub.domain.infrastructure.common.exception.derived.SHBadRequestException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHResourceNotFoundException;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.api.EventV3APIHelper;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.api.VenueConfigV3ApiHelper;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component("eventHelper")
public class EventHelper {
	
	private final static Logger log = LoggerFactory.getLogger(EventHelper.class);

	private static final String PARKING = "Parking";
	private final static String ENABLE_HYBRIDMAP_DYNAMIC_ATTR = "enable_hybrid_map";

	@Autowired
	private EventV3APIHelper eventV3APIHelper;

	@Autowired
	private VenueConfigV3ApiHelper venueConfigV3ApiHelper;

	public enum EventState {
		ACTIVE, INACTIVE, DELETED, DRAFT, REVIEW, COMPLETED, CANCELLED, RESTRICTED, POSTPONED,
		SCHEDULED, CONTINGENT
	}

	public Event getEventById(Long eventId, String filters, Locale locale, boolean getTraits) throws ParseException {
		return getEventById(eventId, filters, locale, getTraits, false);
	}

	public com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event getEventV3ById(Long eventId, Locale locale, boolean getTraits){
		return getEventV3ById(eventId, locale, getTraits, false);
	}

	public Event getEventById(Long eventId, String filters, Locale locale, boolean getTraits, boolean validateExpiredEventNotReqd) throws ParseException  {
		Event event = null;
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = getEventV3ById(eventId, locale, getTraits, validateExpiredEventNotReqd);
		if(eventV3 != null) {
			event = new Event();
			event.setActive(true);
			event.setBookOfBusinessId(Long.valueOf(eventV3.getBobId()));
			event.setCurrency(Currency.getInstance(eventV3.getCurrencyCode()));
			
			String genrePath = constructGenrePath(eventV3);
			if(StringUtils.trimToNull(genrePath) != null) {
				event.setGenrePath(genrePath);
				String genrePathSubString = genrePath.substring(0, genrePath.length()-1);
				int i = genrePathSubString.lastIndexOf("/");
				event.setGenreParent(Long.parseLong(genrePathSubString.substring(i+1)));
			}
			
			
			String geoPath = constructGeoPath(eventV3);
			if(StringUtils.trimToNull(geoPath) != null) {
				event.setGeoPath(geoPath);
				String geoPathSubString = geoPath.substring(0, geoPath.length()-1);
				int i = geoPathSubString.lastIndexOf("/");
				event.setGeographyParent(Long.parseLong(geoPathSubString.substring(i+1)));
			}
			
			event.setId(eventV3.getId());
			event.setDescription(eventV3.getName());
			event.setIsEticketAllowed(true);
			event.setLocale(eventV3.getLocale());
			
			event.setJdkTimeZone((TimeZone.getTimeZone(eventV3.getTimezone())));
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
			simpleDateFormat.setTimeZone(TimeZone.getTimeZone(eventV3.getTimezone()));
			Calendar eventDateLocal = Calendar.getInstance(TimeZone.getTimeZone(eventV3.getTimezone()));
			eventDateLocal.setTime(simpleDateFormat.parse(eventV3.getEventDateLocal()));
			event.setEventDate(eventDateLocal);
			
			//determine parking pass only event
			if (eventV3.getEventAttributes() != null && PARKING.equalsIgnoreCase(eventV3.getEventAttributes().getEventType())) {
			    event.setParkingOnlyEvent(Boolean.TRUE);
	        }
			
			if(eventV3.getVenue() != null) {
				event.setVenueConfigId(Long.valueOf(eventV3.getVenue().getConfigurationId()));
			    event.setVenueDesc(eventV3.getVenue().getName());
			    event.setCountry(eventV3.getVenue().getCountry());
			    event.setEventState(eventV3.getVenue().getState());
			    
			}
			
			if(getTraits) {
				List<Long> ticketTraitIds = new ArrayList<Long>();
				List<TicketTrait> ticketTraits = new ArrayList<TicketTrait>();
				if (eventV3 != null && eventV3.getSeatTraits() != null) {
					List<SeatTrait> seatTraits = eventV3.getSeatTraits();
					for(SeatTrait st : seatTraits){					
						if(st.getId() != null) {
							log.debug("seatTraitId=" + st.getId());
							ticketTraitIds.add(st.getId());
							TicketTrait tt = new TicketTrait();
							tt.setId(st.getId());
							tt.setName(st.getName());
							tt.setType(st.getType());
							ticketTraits.add(tt);
						}
					}
					event.setTicketTraitId(ticketTraitIds);
					event.setTicketTrait(ticketTraits);
				}
			}
			if(eventV3.getDisplayAttributes() != null && eventV3.getDisplayAttributes().getIntegratedEventInd() != null) {
				event.setIsIntegrated(eventV3.getDisplayAttributes().getIntegratedEventInd());
			}
			
			if(StringUtils.trimToNull(filters) != null && filters.contains("venue")) {
				VenueConfiguration venueConfig = venueConfigV3ApiHelper.getVenueDetails(eventId);
				if(venueConfig != null) {
					event.setGaIndicator(venueConfig.getGeneralAdmissionOnly());
					if(venueConfig.getMap() != null){
						event.setSectionScrubbing(venueConfig.getMap().getSectionScrubbing());
						event.setRowScrubbing(venueConfig.getMap().getRowScrubbing());
					}
					
				}
			}
			
			 List<? extends CommonAttribute> dynamicAttributes = eventV3.getDynamicAttributes();
			 if (dynamicAttributes != null){
				 for(CommonAttribute attribute : dynamicAttributes){
					 if(ENABLE_HYBRIDMAP_DYNAMIC_ATTR.equalsIgnoreCase(attribute.getName())){
						 if(!StringUtils.isNullorEmpty(attribute.getValue())){
							log.info("_message=\"Value of enableHybridMap toggle\" eventId={} enableHybridMap={}", event.getId(), attribute.getValue());
							 event.setEnableHybridMap(Boolean.parseBoolean(attribute.getValue()));
						 }
					 }
					 
				 }
			 }
		}
		return event;
	}

	public VenueConfiguration getVenueDetails(Long eventId){
		return venueConfigV3ApiHelper.getVenueDetails(eventId);
	}

	//getting localized section name
	public String getLocalizedSeatingSectionName(Long sectionId, Locale locale){
		
		SeatingSection seatingSection = venueConfigV3ApiHelper.getLocalizedSeatingSection(sectionId,locale);
		if(seatingSection != null){
			return seatingSection.getName();
		}
		return null;
	}
	
	//getting localized zone name
	public String getLocalizedSeatingZoneName(Long zoneId, Locale locale){
		
		SeatingZone seatingZone = venueConfigV3ApiHelper.getLocalizedSeatingZone(zoneId,locale);
		if(seatingZone != null){
			return seatingZone.getName();
		}
		return null;
	}

	protected String getProperty(String propertyName, String defaultValue) {
		return MasterStubHubProperties.getProperty(propertyName, defaultValue);
	}

	/**
	 * Make sure event is valid
	 */
	public Event getEventObject (Locale locale, Listing listing, Long eventId, boolean getTraits) 
			
	{
		Event event = null;
		//SELLAPI-1550. If deleting listing, don't do any further check
		boolean isDeleteListing = false;
		if(listing.getSystemStatus() != null) {
		  isDeleteListing = listing.getSystemStatus().equals(  
            ListingStatus.DELETED.toString());
        }
		try {
			String filterStr = "event genrePath geoPath venue";
			if ( getTraits ) {
				filterStr = filterStr + " ticketTraits";
			}
			if (log.isDebugEnabled()) {
				log.debug("message=\"Before Get Event Call\"");
			}
			//for delete listing, no need validate event status
			event = getEventById(eventId, filterStr, locale, getTraits, isDeleteListing);
		} 
		catch (SHResourceNotFoundException e) {
			ListingError listingError = new ListingError(
					ErrorType.NOT_FOUND, ErrorCode.INVALID_EVENTID,
					"Either the event is expired or the ID is invalid", "");
			throw new ListingBusinessException(listingError);
		}
		catch (SHBadRequestException e) {
			if(isDeleteListing){  //SELLAPI-1550. If deleting listing, don't do any further check
				
				return event;
				}
			if(e.getErrorCode().equals("inventory.listings.inActiveEvent"))
			{
				ListingError listingError = new ListingError(
						ErrorType.BUSINESSERROR, ErrorCode.EVENT_NOT_ACTIVE,
						"Event is not Active", "");
				throw new ListingBusinessException(listingError);	
			}
			else
			{
				ListingError listingError = new ListingError(
						ErrorType.BUSINESSERROR, ErrorCode.EVENT_EXPIRED,
						"The event has expired ", "");
				throw new ListingBusinessException(listingError);
			}
			
		}
		catch (Exception e) {
			log.error(
					"error_message=\"Exception occured while calling getEventMeta api\""+"eventId="
							+ eventId, e);
			ListingError listingError = new ListingError(
					ErrorType.SYSTEMERROR, ErrorCode.SYSTEM_ERROR,
					"System error occured", "");
			throw new ListingBusinessException(listingError);
		}
		if (event == null || !event.getActive()) {
			ListingError listingError = new ListingError(
					ErrorType.BUSINESSERROR, ErrorCode.INVALID_EVENTID, "Event ID is invalid",
					"eventId");
			throw new ListingBusinessException(listingError);
		}
		return event;
	}

	public com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event getEventV3ById(Long eventId, Locale pLocale, boolean getSeatTraits, boolean validateExpiredEventNotReqd){

		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event event = eventV3APIHelper.getEventV3ById(eventId, pLocale, getSeatTraits);

		// Removing validation as a part of SELLAPI-4356. The event validation should be
		// handled by upstream clients and inventory apis should only concern themselves
		// with the state of the listing.
		if (!validateExpiredEventNotReqd) {
			validateIfEventExpired(event, false);
		}

		return event;
	}



	/**
	 * Validates if event is expired
	 * @param event the event response
	 * @throws ParseException 
	 * @throws SHBadRequestException
	 */
	public void validateIfEventExpired(com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event event, boolean includeInvalidEvents) {
		SHBadRequestException shException = null;
		Set<String> validStatuses = new HashSet<String>();
		validStatuses.add(EventState.ACTIVE.name());
		validStatuses.add(EventState.CONTINGENT.name());
		validStatuses.add(EventState.SCHEDULED.name());
		if (includeInvalidEvents) {
            validStatuses.add(EventState.REVIEW.name());
            validStatuses.add(EventState.POSTPONED.name());
            validStatuses.add(EventState.INACTIVE.name());
        }

		boolean isValidStatus = (null != event && event.getStatus() != null && validStatuses.contains(event.getStatus().toUpperCase()));
		if (!isValidStatus) {
			if(null!=event) {
    			log.error("_message=\"catalog api response event NOT_ACTIVE\" eventId={}",event.getId());
    			shException = new SHBadRequestException("eventId="+event.getId()+" inActive");
			} else {
    			log.error("_message=\"catalog api response event NOT_ACTIVE\"");
    			shException = new SHBadRequestException("The event is inActive");
			}
			shException.setErrorCode("inventory.listings.inActiveEvent");
			throw shException;
		}
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(event.getTimezone()));
        Calendar lastChanceDate = Calendar.getInstance(TimeZone.getTimeZone(event.getTimezone()));
		try {
			lastChanceDate.setTime(simpleDateFormat.parse(event.getDateLastChance()));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

		Calendar today = Calendar.getInstance(TimeZone.getTimeZone(event.getTimezone()));
		if (event.getExpiredInd() && lastChanceDate.before(today)) {
			log.error("_message=\"catalog api response event EXPIRED\" eventId={}",event.getId());
			shException = new SHBadRequestException("eventId="+event.getId()+" Expired");
			shException.setErrorCode("inventory.listings.eventExpired");
			throw shException;
		}
	}
	
	public String constructGenrePath(com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event event) {
		StringBuffer genrePath = new StringBuffer();
		if(event != null) {
			if (event.getAncestors() != null && event.getAncestors().getCategories() != null) {
				for (AncestorItem index : event.getAncestors().getCategories()) {
					genrePath.append(index.getId() + "/");
				}
			}
			if (event.getAncestors() != null && event.getAncestors().getGroupings() != null) {
				for (AncestorItem index : event.getAncestors().getGroupings()) {
					genrePath.append(index.getId() + "/");
				}
			}
			if (event.getAncestors() != null && event.getAncestors().getPerformers() != null) {
				for (AncestorItem index : event.getAncestors().getPerformers()) {
					genrePath.append(index.getId() + "/");
				}
			}
			log.debug("_message=\"genrePath={}, eventId={}\"", genrePath, event.getId());
		}
		return genrePath.toString();
	}
	
	public String constructGeoPath(com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event event) {
		StringBuffer geoPath = new StringBuffer();
		if(event != null) {
			if (event.getAncestors() != null && event.getAncestors().getGeographies() != null ) {
				for (AncestorItem index : event.getAncestors().getGeographies()) {
					geoPath.append(index.getId() + "/");
				}
			}
			if(event.getVenue() != null) {
				geoPath.append(event.getVenue().getId() + "/");
			}
			log.debug("_message=\"geoNodeIds={}, eventId={}\"", geoPath, event.getId());
		}
		return geoPath.toString();
	}

	public static final String DYNAMIC_ATTRIBUTE_LIABILITY_WAIVER = "liabilityWaiver";

	public static boolean isLiabilityWaiver(com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3) {
		List<CommonAttribute> eventsAttributes= (List<CommonAttribute>) eventV3.getDynamicAttributes();
		for (CommonAttribute commonAttribute:eventsAttributes){
			if(commonAttribute.getName().equals(DYNAMIC_ATTRIBUTE_LIABILITY_WAIVER)){
				if(Boolean.parseBoolean(commonAttribute.getValue())){
					return true;
				}
			}
		}
		return false;
	}
	
}