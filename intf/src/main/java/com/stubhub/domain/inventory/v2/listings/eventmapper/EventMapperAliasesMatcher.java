package com.stubhub.domain.inventory.v2.listings.eventmapper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
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
import com.stubhub.domain.inventory.v2.DTO.EventInfo;
import com.stubhub.domain.search.catalog.v3.intf.common.Alias;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.ShipEvent;

@Component("eventMapperAliasesMatcher")
public class EventMapperAliasesMatcher {

  private static final Logger LOG = LoggerFactory.getLogger(EventMapperAliasesMatcher.class);  

  @Autowired
  @Qualifier("kafkaProducer")
  private KafkaProducer kafkaProducer;
  
  @Autowired
  @Qualifier("eventMapperSearchHandler")
  private EventMapperSearchHandler eventMapperSearchHandler;
  
  @Autowired
  @Qualifier("eventMapperResolver")
  private EventMapperResolver eventMapperResolver;
  
  @Autowired
  @Qualifier("kafkaMessageConstructor")
  private KafkaMessageConstructor kafkaMessageConstructor;
  

  public ShipEvent matchEvents(List<ShipEvent> events, EventInfo eventInfo, String sellerId) {

    List<ShipEvent> matchedAliasEvents = matchAlliasesByNameDate(events, eventInfo, sellerId);
    boolean error = false;   
    ShipEvent matchedEvent = null;
    
    // return the selected event
    if (matchedAliasEvents.size() == 1) {
        LOG.info("_message = {} reason = {} eventMapRequest = {} sellerId = {}",
            Constants.SHIP_EVENT_MATCHED_MESSAGE, Constants.ALIASED_MATCH_REASON,
            eventInfo.formatForLog(), sellerId);
        matchedEvent = matchedAliasEvents.get(0);
      }
    
    //check if all aliases has same event id, if yes then match the event
    else if( isAllAliasesOfSameEvent(matchedAliasEvents)){
		 LOG.info("_message=\"Multiple aliases of same event found\"");
		 matchedEvent =  matchedAliasEvents.get(0);
	 }
    else if(isExcludeBulkListing(events)){
    	error = true;
	 }
    else{
    	ShipEvent event = findTBDEvent(events, eventInfo);
        if(event != null){
        	matchedEvent = event;
        }else{
        	error = true; 
        }
    } 
    if(error){
            EventError eventError = new EventError(ErrorType.INPUTERROR,ErrorCode.EVENT_NOT_MAPPED,
          		  ErrorEnum.EVENT_NOT_MAPPED.getMessage(), eventInfo.formatForLog());
            throw new EventMappingException(eventError);
    }
    return matchedEvent;
  } 

  private boolean isExcludeBulkListing(List<ShipEvent> shipEvents) {
	  Collection bulkListing = CollectionUtils.select(shipEvents, new Predicate(){
	      @Override
	      public boolean evaluate(Object event){
	    	  ShipEvent shipEvent = (ShipEvent)event;	    	  
	    	  if (shipEvent.getExcludeBulkListings() != null && shipEvent.getExcludeBulkListings().equalsIgnoreCase("true")) {
	  			return true;
	  		  };
	  		  return false;
	      }
	    });
	  if(bulkListing.size() == shipEvents.size()){		  
		  LOG.warn("_message=\"Multiple events configured on Stubhub with ExcludeBulkListing");
		  return true;
	  }
	return false;
}


private boolean isAllAliasesOfSameEvent(List<ShipEvent> matchedAliasEvents) {
	  Set<Integer> eventIds = new HashSet<>();
	  for(ShipEvent event : matchedAliasEvents){
		  eventIds.add(event.getId());
	  }
	  if(eventIds.size()==1){
		  return true;
	  }
	  
	return false;
}

  /*
   * returns an Event matching venue and date, if there is either 'a TBD event configured on Stubhub' or 'there is a single event configured on Stubhub 
   * and the seller says it is a TBD event'
   */
  private ShipEvent findTBDEvent(final List<ShipEvent> shipEvents, final EventInfo eventInfo) {
	  // all tbd events configured on Stubhub, very likely only one. 
	  Collection tbdEvents = CollectionUtils.select(shipEvents, new Predicate(){
	      @Override
	      public boolean evaluate(Object event){
	    	  ShipEvent shipEvent = (ShipEvent)event;	    	  
	    	  if (shipEvent.getDisplayAttributes() != null && shipEvent.getDisplayAttributes().getHideEventTime() != null &&
	  				shipEvent.getDisplayAttributes().getHideEventTime()) {
	  			return true;
	  		  };
	  		  return false;
	      }
	    });
	  if(tbdEvents.size() > 1){		  
		  LOG.warn("_message=\"Multiple TBD events configured on Stubhub\" eventInfo: {}",eventInfo.formatForLog());
		  return null;
	  }
	  boolean isSellerTBD = false;
	  //if seller sent in eventDateLocal time as 'T00:00' it is sellerTBD event.
	  if(!StringUtils.isBlank(eventInfo.getEventLocalDate())){
		  Calendar inputDateLocal = DateTimeUtil.getLocalDateFromString(eventInfo.getEventLocalDate());
		  if (inputDateLocal.get(Calendar.HOUR) == 0 && inputDateLocal.get(Calendar.MINUTE) == 0) {				
				isSellerTBD = true;
		  }
	  }
	  
	  //if seller sent TBD event and there is one TBD event in SH, match it.
	  //if seller sent TBD event and there is only one event on SH (even if it is not TBD) match it.
	  //if seller did not specify TBD, but there is only event on SH and that is TBD, match it.
	  if(tbdEvents.size() == 1 && isSellerTBD){
		  LOG.info("_message=\"Stubhub TBD event found.\"");
		  return (ShipEvent)tbdEvents.iterator().next();
	  }else if (shipEvents.size() == 1 && isSellerTBD){		  
		  LOG.info("_message=\"Seller TBD event found.\"");
		  return shipEvents.get(0);			
	  }else if(!isSellerTBD && shipEvents.size() == 1 && tbdEvents.size() == 1){
		  LOG.info("_message=\"Seller is not TBD event but SH has a single event that is TBD.\"");
		  return shipEvents.get(0);
	  }
	return null;
 }
  
  
    

private List<ShipEvent> matchAlliasesByNameDate(List<ShipEvent> events, EventInfo eventInfo,
      String sellerId) {


    if (eventInfo.getEventLocalDate() != null) {
      Calendar inputDateLocal = DateTimeUtil.getLocalDateFromString(eventInfo.getEventLocalDate());
      return matchEventNameAndDate(events, eventInfo, sellerId, inputDateLocal, true);

    } else {
      LOG.debug(
          "_message=\"Aliases multiple match. UTCDateTime and Name date range match strategy\"");
      Calendar inputDateUTC = DateTimeUtil.getUTCDateFromString(eventInfo.getDate());
      return matchEventNameAndDate(events, eventInfo, sellerId, inputDateUTC, false);
    }
  }

  private List<ShipEvent> matchEventNameAndDate(List<ShipEvent> events, EventInfo eventInfo,
      String sellerId, Calendar eventDateObj, Boolean local) {
    List<ShipEvent> matchedAliasEvents = new ArrayList<>();
    for (ShipEvent event : events) {
      List<Alias> eventAliasesList = event.getAliases();
      
      // No Aliases object array present in current catalog ShipEvent
      if (eventAliasesList == null || eventAliasesList.size() == 0) {
        LOG.debug("_message=\"Aliases are empty or null in the catalogue response \"");
        continue;
      }

      for (Alias alias : eventAliasesList) {
        Calendar eventDateLocal = null;
        if (local) {
          eventDateLocal = DateTimeUtil.getLocalDateFromString(alias.getDate());
        } else {
          eventDateLocal = DateTimeUtil.getUTCDateFromString(alias.getDate());
        }
        LOG.debug(
            "_message=\"Aliases multiple match. DateTime and Name date range match strategy eventVenue\"");
        if ((alias.getName().equalsIgnoreCase(eventInfo.getName()))
            && (eventDateLocal.equals(eventDateObj))) {
          LOG.debug(
              "_message=\"Aliases name and date match exactly for eventName={} sellerId={}\"",eventInfo.getName(),sellerId);
          matchedAliasEvents.add(event);
        }
      }
    }
    return matchedAliasEvents;
  }
  
}
