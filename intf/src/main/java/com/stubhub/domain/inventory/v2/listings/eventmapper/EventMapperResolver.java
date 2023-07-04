package com.stubhub.domain.inventory.v2.listings.eventmapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.entity.ErrorEnum;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.EventError;
import com.stubhub.domain.inventory.common.util.EventMappingException;
import com.stubhub.domain.inventory.listings.v2.aspects.ExcludeLogParam;
import com.stubhub.domain.inventory.v2.DTO.EventInfo;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.ShipEvent;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.ShipEvents;


@Component("eventMapperResolver")
public class EventMapperResolver {

  private final static Logger LOG = LoggerFactory.getLogger(EventMapperResolver.class);
  private static final String VENUE_TYPE_VALUE = "venue";
  private static final String EVENT_TYPE_VALUE = "event";


  @Autowired
  @Qualifier("eventMapperExactDateTimeMatcher")
  private EventMapperExactDateTimeMatcher eventMapperExactDateTimeMatcher;

  @Autowired
  @Qualifier("eventMapperAliasesMatcher")
  private EventMapperAliasesMatcher eventMapperAliasesMatcher;

  @Autowired
  @Qualifier("eventMapperRangeDateTimeMatcher")
  private EventMapperRangeDateTimeMatcher eventMapperRangeDateTimeMatcher;


  @Autowired
  @Qualifier("eventMapperSearchHandler")
  private EventMapperSearchHandler eventMapperSearchHandler;
  
  @Autowired
  @Qualifier("kafkaMessageConstructor")
  private KafkaMessageConstructor kafkaMessageConstructor;
  
  public ShipEvent resolveEvent(Locale locale, EventInfo eventInfo, @ExcludeLogParam String sellerId, @ExcludeLogParam String userToken) {
	eventInfo.setIsLanguageEnabled(true);
    ShipEvents events = eventMapperSearchHandler.searchEvents(locale, eventInfo, userToken);
    ShipEvent matchedEvent=null;
    if (events.getEvents().isEmpty()){
    	String venue="";
    	// check if venue has parenthesis
    	int parenthesisPos = eventInfo.getVenue().indexOf("(");
    	if(parenthesisPos != -1){
    		venue  = eventInfo.getVenue().substring(0, parenthesisPos);
    		eventInfo.setVenue(venue);
    	    events = eventMapperSearchHandler.searchEvents(locale, eventInfo, userToken);
    	}	
    } else if(locale == null || Locale.US.equals(locale)) {
    	events = separateMultiLocaleVenues(locale, eventInfo, sellerId, userToken, events);
	}
	
    if (events.getEvents().isEmpty() && !events.getVenueMatched()){
  	  // No match Found
      kafkaMessageConstructor.constructMessage(eventInfo, sellerId, VENUE_TYPE_VALUE);
  	  LOG.warn("_message=\"{}\" reason=\"{}\" eventMapRequest={} sellerId={}",
  	      Constants.VENUE_NOT_FOUND_MESSAGE, Constants.SHIP_EVENT_NOT_FOUND_REASON,
  	      eventInfo.formatForLog(), sellerId);
  	  EventError error = new EventError(ErrorType.INPUTERROR, ErrorCode.EVENT_NOT_MAPPED,
  	          ErrorEnum.EVENT_NOT_MAPPED.getMessage(), eventInfo.formatForLog());
  	      throw new EventMappingException(error);
    }else if (events.getEvents().isEmpty() && events.getVenueMatched()){
    	LOG.warn("_message=\"{}\" reason=\"{}\" eventMapRequest={} sellerId={}",
    	  	Constants.SHIP_EVENT_NOT_FOUND_VENUE_FOUND_MESSAGE, Constants.SHIP_EVENT_NOT_FOUND_REASON,
    	  	eventInfo.formatForLog(), sellerId);
    	EventError error = new EventError(ErrorType.INPUTERROR, ErrorCode.EVENT_NOT_MAPPED,
    			ErrorEnum.EVENT_NOT_MAPPED.getMessage(), eventInfo.formatForLog());
    		throw new EventMappingException(error);
    }else{
		matchedEvent = matchExactTimeHideUnhideEvents(events.getEvents(),eventInfo,sellerId);
		if (matchedEvent!=null){
			return matchedEvent;
		}else{  
	    	//Group by excludeBulkListing true or false and run map event logic
	    	LOG.debug("The number of EventMatchedCount={}",events.getNumFound());
	        List<ShipEvent> hiddenEvents=new ArrayList<>();
	    	List<ShipEvent> nonHiddenEvents = new ArrayList<>();
	    	separateEventsIntoHiddenAndNonHidden(events.getEvents(), hiddenEvents, nonHiddenEvents);  
	    	RuntimeException exception = null;
	        
		    	if(!nonHiddenEvents.isEmpty()){
		    		try {
		    			matchedEvent= matchEvent(locale, eventInfo, sellerId, userToken, nonHiddenEvents);
		    			} catch (RuntimeException ex) {
		    				exception = ex;
		    			}
		    	  }	
		       if(matchedEvent==null && !hiddenEvents.isEmpty()){
		    	   try {
		    		   matchedEvent= eventMapperAliasesMatcher.matchEvents(hiddenEvents, eventInfo, sellerId);
					   } catch (RuntimeException ex) {
						exception = ex;
					   }
		       }
		      
		       if (matchedEvent==null && exception!=null){
		         	LOG.warn("_message=\"{}\" reason=\"{}\" eventMapRequest={} sellerId={}",
		                    Constants.EVENT_NOT_FOUND_MESSAGE, Constants.SHIP_EVENT_NOT_FOUND_ALIASES_REASON,
		                    eventInfo.formatForLog(), sellerId);
		                kafkaMessageConstructor.constructMessage(eventInfo, sellerId, EVENT_TYPE_VALUE);
		                EventError error = new EventError(ErrorType.INPUTERROR,ErrorCode.EVENT_NOT_MAPPED,
		              		  ErrorEnum.EVENT_NOT_MAPPED.getMessage(), eventInfo.formatForLog());
		                throw new EventMappingException(error);
		      }
	      }
	    return matchedEvent;
    }
    }

	private ShipEvents separateMultiLocaleVenues(Locale locale, EventInfo eventInfo, String sellerId, String userToken,
			ShipEvents events) {
		boolean hasIntlEventInResults = false;
		boolean hasUSEventInResults = false;
		for(ShipEvent event : events.getEvents()) {
			if(event.getVenue() == null || Locale.US.getCountry().equalsIgnoreCase(event.getVenue().getCountry())) {
				hasUSEventInResults = true;
			} else {
				hasIntlEventInResults = true;
			}
		}
		if(hasIntlEventInResults) {
			if(hasUSEventInResults) { //Duplicate Venue with Events on same date
				eventInfo.setIsLanguageEnabled(false);
				events = eventMapperSearchHandler.searchEvents(locale, eventInfo, userToken);
				LOG.info("_message=\"{}\" reason=\"{}\" venue={}", Constants.VENUE_EXISTS_IN_MULTI_LOCALE,
						Constants.VENUE_EXISTS_IN_MULTI_LOCALE_REASON, eventInfo.getVenue());
			} else {
				EventInfo venueInfo = new EventInfo();
				venueInfo.setVenue(eventInfo.getVenue());
				venueInfo.setLocale(eventInfo.getLocale());
				venueInfo.setCountry(Locale.US.getCountry());
				ShipEvents venueEvents = eventMapperSearchHandler.searchEvents(locale, venueInfo, userToken);
				if(!venueEvents.getEvents().isEmpty()) { //Duplicate Venues. Events only exist in Intl Venue.
	
					LOG.info("_message=\"{}\" reason=\"{}\" venue={}", Constants.VENUE_EXISTS_IN_MULTI_LOCALE,
							Constants.VENUE_EXISTS_IN_MULTI_LOCALE_REASON, eventInfo.getVenue());
					LOG.warn("_message=\"{}\" reason=\"{}\" eventMapRequest={} sellerId={}",
				    	  	Constants.SHIP_EVENT_NOT_FOUND_VENUE_FOUND_MESSAGE, Constants.SHIP_EVENT_NOT_FOUND_REASON,
				    	  	eventInfo.formatForLog(), sellerId);
				    	EventError error = new EventError(ErrorType.INPUTERROR, ErrorCode.EVENT_NOT_MAPPED,
				    			ErrorEnum.EVENT_NOT_MAPPED.getMessage(), eventInfo.formatForLog());
				    		throw new EventMappingException(error);
				}
			}
		}
		return events;
	}
 
private void separateEventsIntoHiddenAndNonHidden(List<ShipEvent> events, List<ShipEvent> hiddenEvents,
		List<ShipEvent> nonHiddenEvents) {
	for (Iterator<ShipEvent> iterator = events.iterator(); iterator.hasNext();) {
		  ShipEvent event = iterator.next();
	      if(event.getExcludeBulkListings().equalsIgnoreCase("true")){
	    	  hiddenEvents.add(event);
	      }else{
	      	nonHiddenEvents.add(event);
	      }
	  }
}


private ShipEvent matchEvent(Locale locale, EventInfo eventInfo, String sellerId, String userToken,
		List<ShipEvent> events) {
  	 LOG.debug("_message=\"{}\" totalMatchEvents={}", Constants.MULTIPLE_SHIP_EVENTS_FOUND_MESSAGE,
	      events.size());
  	 // Multiple matches from search catalog event api response based upon venue and date or other
	 // params if passed in the request
     if (StringUtils.trimToNull(eventInfo.getEventLocalDate()) != null) {
    	 return resolveEventByExactLocalDate(events, eventInfo, sellerId);
     } else {
    	 return resolveEventByExactUTCDate(events, eventInfo, sellerId);
     }
	}



  private ShipEvent resolveEventByExactUTCDate(List<ShipEvent> events, EventInfo eventInfo,
      String sellerId) {
    List<ShipEvent> eventsMatchingByUTCDateTime =
        eventMapperExactDateTimeMatcher.matchEventsByUTCDate(events, eventInfo.getDate());
    // Exact Match
    if (eventsMatchingByUTCDateTime.size() == 1) {
      ShipEvent exactUtcDateTimeEvent = eventsMatchingByUTCDateTime.get(0);
      LOG.info("_message=\"{}\" reason=\"{}\" timeZone={} eventMapRequest={} sellerId={} shEventId={}",
          Constants.SHIP_EVENT_MATCHED_MESSAGE, Constants.EXACT_UTC_DATE_MATCH_REASON,
          Constants.TIMEZONE_TYPE_UTC, eventInfo.formatForLog(), sellerId,exactUtcDateTimeEvent.getId());
      return exactUtcDateTimeEvent;
    } else if (eventsMatchingByUTCDateTime.isEmpty()) {
      // Not match
      LOG.debug("_message=\"{}\" reason=\"{}\" timeZone={}", Constants.SHIP_EVENT_NOT_MATCHED_MESSAGE,
          Constants.EXACT_UTC_DATE_MATCH_REASON, Constants.TIMEZONE_TYPE_UTC);
      return resolveEventByDateRangeUtc(events, eventInfo, sellerId);
    } else {
      // Multiple match so fall back to Aliases matching strategy
      LOG.debug("_message=\"{}\" timeZone={} totalMatchEvents={}",
          Constants.MULTIPLE_SHIP_EXACT_EVENTS_FOUND_MESSAGE, Constants.TIMEZONE_TYPE_UTC,
          events.size());
      return resolveEventsByEventName(eventsMatchingByUTCDateTime, eventInfo, sellerId);
    }
  }

  private ShipEvent resolveEventByExactLocalDate(List<ShipEvent> events, EventInfo eventInfo,String sellerId) {
    List<ShipEvent> eventsExactMatchingByLocalDateTime = eventMapperExactDateTimeMatcher
        .matchEventsByLocalDate(events, eventInfo.getEventLocalDate());
    // Exact match
    if (eventsExactMatchingByLocalDateTime.size() == 1) {
      ShipEvent matchedDateTimeDescrEvent = eventsExactMatchingByLocalDateTime.get(0);
      LOG.info("_message=\"{}\" reason=\"{}\" timeZone={} eventMapRequest={} sellerId={} shEventId={}",
          Constants.SHIP_EVENT_MATCHED_MESSAGE, Constants.EXACT_LOCAL_DATE_MATCH_REASON,
          Constants.TIMEZONE_TYPE_LOCAL, eventInfo.formatForLog(), sellerId,matchedDateTimeDescrEvent.getId());
      return matchedDateTimeDescrEvent;
    }
    // No match
    else if (eventsExactMatchingByLocalDateTime.isEmpty()) {
      LOG.debug("_message={} reason={}  timeZone={}",
          Constants.SHIP_EVENT_NOT_MATCHED_MESSAGE, Constants.EXACT_LOCAL_DATE_MATCH_REASON,
          Constants.TIMEZONE_TYPE_LOCAL);
      return resolveEventByDateRangeLocalDate(events, eventInfo, sellerId);
    } else {
      // Muliple matches
      LOG.debug("_message={} timeZone={} totalMatchEvents={}",
          Constants.MULTIPLE_SHIP_EXACT_EVENTS_FOUND_MESSAGE, Constants.TIMEZONE_TYPE_LOCAL,
          events.size());
      return resolveEventsByEventName(eventsExactMatchingByLocalDateTime, eventInfo, sellerId);
    }
  }

  private ShipEvent resolveEventByDateRangeLocalDate(List<ShipEvent> events, EventInfo eventInfo,String sellerId) {
    List<ShipEvent> eventsDateRangeMatchingByLocalDateTime = eventMapperRangeDateTimeMatcher
        .matchEventsByLocalDate(events, eventInfo.getEventLocalDate());
    return resolveDateRangeEvents(events, eventsDateRangeMatchingByLocalDateTime, eventInfo, sellerId);
  }

  private ShipEvent resolveEventByDateRangeUtc( List<ShipEvent> events, EventInfo eventInfo, String sellerId) {
    List<ShipEvent> eventsDateRangeMatchingByLocalDateTime = eventMapperRangeDateTimeMatcher.matchEventsByUTCDate(events, eventInfo.getDate());

    return resolveDateRangeEvents(events, eventsDateRangeMatchingByLocalDateTime, eventInfo,sellerId);
  }



  private ShipEvent resolveDateRangeEvents( List<ShipEvent> events, List<ShipEvent> eventsMatchedToDateTime, EventInfo eventInfo, String sellerId) {

    if (eventsMatchedToDateTime.size() == 1) {
      ShipEvent event = eventsMatchedToDateTime.get(0);
      LOG.info("_message=\"{}\" reason=\"{}\" eventMapRequest={} sellerId={} shEventId={}",
          Constants.SHIP_EVENT_MATCHED_MESSAGE, Constants.EXACT_DATE_RANGE_MATCH_REASON,
          eventInfo.formatForLog(), sellerId,event.getId());
      return event;
    } else if (eventsMatchedToDateTime.isEmpty()) {
      LOG.debug("_message={} reason={}", Constants.SHIP_EVENT_NOT_MATCHED_MESSAGE,
          Constants.EXACT_DATE_RANGE_MATCH_REASON);
      return resolveEventsByAliases(events, eventInfo, sellerId);
    } else {
      LOG.info("_message={} totalMatchEvents={}",
          Constants.MULTIPLE_SHIP_EXACT_EVENTS_FOUND_MESSAGE, eventsMatchedToDateTime.size());
      return resolveEventsByEventName(eventsMatchedToDateTime, eventInfo, sellerId);
    }
  }


  private ShipEvent resolveEventsByEventName(List<ShipEvent> events, EventInfo eventInfo, String sellerId) {
	List<ShipEvent> nameMatchedEvents = new ArrayList<>();
	for(ShipEvent event : events) {
		if(event.getName().toLowerCase().contains(eventInfo.getName().toLowerCase())) {
			nameMatchedEvents.add(event);
		}
	}
	if(nameMatchedEvents.size() == 1) {
		return nameMatchedEvents.get(0);
	} else {
		return resolveEventsByAliases(events, eventInfo, sellerId);
	}
  }


  private ShipEvent resolveEventsByAliases(List<ShipEvent> events, EventInfo eventInfo, String sellerId) {
    return eventMapperAliasesMatcher.matchEvents(events, eventInfo, sellerId);
  }
  
  private ShipEvent matchExactTimeHideUnhideEvents(List<ShipEvent> events, EventInfo eventInfo,
	      String sellerId){
	  	List<ShipEvent> eventsMatchingByDateTime;
	  	ShipEvent matchedExactDateTimeDescrEvent = null;
	     if (StringUtils.trimToNull(eventInfo.getEventLocalDate()) != null) {
	    	 eventsMatchingByDateTime = eventMapperExactDateTimeMatcher
	    		        .matchEventsByLocalDate(events, eventInfo.getEventLocalDate());	    	 
	     }else{
	    	 eventsMatchingByDateTime =
	 		        eventMapperExactDateTimeMatcher.matchEventsByUTCDate(events, eventInfo.getDate());	    	 
	     }
		    // Exact Match
		    if (eventsMatchingByDateTime.size() == 1) {
		      matchedExactDateTimeDescrEvent = eventsMatchingByDateTime.get(0);
		      LOG.info("_message=\"{}\" reason=\"{}\" timeZone={} eventMapRequest={} sellerId={} shEventId={}",
		          Constants.SHIP_EVENT_MATCHED_MESSAGE, Constants.EXACT_DATE_TIME_MATCH_REASON,
		          Constants.TIMEZONE_TYPE_UTC, eventInfo.formatForLog(), sellerId,matchedExactDateTimeDescrEvent.getId());	  
		    }
		return matchedExactDateTimeDescrEvent;
  }
}
