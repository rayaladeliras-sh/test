package com.stubhub.domain.inventory.listings.v2.newflow.helper;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.catalog.events.intf.TicketTrait;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.SeatTrait;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.EventDateParseException;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.api.EventV3APIHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component("_eventHelper")
public class EventHelper {

  private final static Logger log = LoggerFactory.getLogger(EventHelper.class);
  private static final String EVENT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

  @Autowired
  private EventV3APIHelper eventV3APIHelper;

  /*
  public void validate(Event event) {
    Set<String> validStatuses = new HashSet<String>();
    validStatuses.add(CommonConstants.ACTIVE.toUpperCase());
    validStatuses.add(CommonConstants.CONTINGENT.toUpperCase());
    validStatuses.add(CommonConstants.SCHEDULED.toUpperCase());
    validStatuses.add("REVIEW");

    boolean isValidStatus =
        (event.getStatus() != null && validStatuses.contains(event.getStatus().toUpperCase()));
    if (!isValidStatus) {
      log.error("");
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.eventNotActive);
    }

    Calendar lastChanceDate = getDateLastChance(event);

    Calendar today = Calendar.getInstance(TimeZone.getTimeZone(event.getTimezone()));
    if (event.getExpiredInd() && lastChanceDate.before(today)) {
      log.error("");
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.eventExpired);
    }
  }
  */
  public Calendar getDateLastChance(Event event) {
    return parseEventDate(event, event.getDateLastChance());
  }

  public Calendar getEventDateLocal(Event event) {
    return parseEventDate(event, event.getEventDateLocal());
  }

  public Calendar parseEventDate(Event event, String data) {
    TimeZone timeZone = TimeZone.getTimeZone(event.getTimezone());
    Calendar calendar = Calendar.getInstance(timeZone);

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(EVENT_DATE_FORMAT);
    simpleDateFormat.setTimeZone(timeZone);

    try {
      calendar.setTime(simpleDateFormat.parse(data));
    } catch (ParseException e) {
      // TODO: Add Log
      throw new EventDateParseException(ErrorType.SYSTEMERROR, ErrorCodeEnum.systemError);
    }
    return calendar;
  }

  public Event getEvent(Long eventId, boolean isTraits) {
    return eventV3APIHelper.getEventV3ById(eventId, null, isTraits);
  }

  public com.stubhub.domain.inventory.datamodel.entity.Event convert(Event eventV3) {
    com.stubhub.domain.inventory.datamodel.entity.Event event = null;
    if (eventV3 != null) {
      event = new com.stubhub.domain.inventory.datamodel.entity.Event();
      event.setId(eventV3.getId());
      event.setDescription(eventV3.getName());
      event.setCurrency(Currency.getInstance(eventV3.getCurrencyCode()));
      event.setJdkTimeZone((TimeZone.getTimeZone(eventV3.getTimezone())));
      
      //set the eventDate
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
      simpleDateFormat.setTimeZone(TimeZone.getTimeZone(eventV3.getTimezone()));
      Calendar eventDateLocal = Calendar.getInstance(TimeZone.getTimeZone(eventV3.getTimezone()));
      try {
        eventDateLocal.setTime(simpleDateFormat.parse(eventV3.getEventDateLocal()));
      } catch (ParseException e) {
        log.error("message=\"Error parsing eventDateLocal\" eventDateLocal={} ",eventDateLocal);
      }
      event.setEventDate(eventDateLocal);

      if (eventV3.getVenue() != null) {
        event.setVenueConfigId(Long.valueOf(eventV3.getVenue().getConfigurationId()));
        event.setVenueDesc(eventV3.getVenue().getName());
        event.setCountry(eventV3.getVenue().getCountry());
        event.setEventState(eventV3.getVenue().getState());
      }

      List<Long> ticketTraitIds = new ArrayList<Long>();
      List<TicketTrait> ticketTraits = new ArrayList<TicketTrait>();
      if (eventV3.getSeatTraits() != null) {
        List<SeatTrait> seatTraits = eventV3.getSeatTraits();
        for (SeatTrait st : seatTraits) {
          if (st.getId() != null) {
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
      if (eventV3.getDisplayAttributes() != null
          && eventV3.getDisplayAttributes().getIntegratedEventInd() != null) {
        event.setIsIntegrated(eventV3.getDisplayAttributes().getIntegratedEventInd());
      }
    }

    return event;
  }

}
