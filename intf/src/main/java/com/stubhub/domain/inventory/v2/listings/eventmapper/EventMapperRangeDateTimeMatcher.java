package com.stubhub.domain.inventory.v2.listings.eventmapper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.EventError;
import com.stubhub.domain.inventory.common.util.EventMappingException;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.ShipEvent;

@Component("eventMapperRangeDateTimeMatcher")
public class EventMapperRangeDateTimeMatcher {

  private final static Logger LOG = LoggerFactory.getLogger(EventMapperRangeDateTimeMatcher.class);

  public List<ShipEvent> matchEventsByLocalDate(List<ShipEvent> events, String dateTime) {

    List<ShipEvent> matchedEvents = null;
    if (StringUtils.trimToNull(dateTime) != null) {
      matchedEvents = matchLocalDateTime(events, dateTime);
    } else {

      // Validating if local Date time is not passed
      String message = "LocalDate passed in the request is null.";
      EventError error =
          new EventError(ErrorType.INPUTERROR, ErrorCode.INPUT_ERROR, message, message);
      throw new EventMappingException(error);
    }

    return matchedEvents;
  }

  private List<ShipEvent> matchLocalDateTime(List<ShipEvent> events, String localDateTime) {
    List<ShipEvent> matchedEvents = new ArrayList<>();
    Calendar eventDateFrom = DateTimeUtil.getLocalDateWithHourFromString(localDateTime, -1);
    Calendar eventDateTo = DateTimeUtil.getLocalDateWithHourFromString(localDateTime, 1);

    for (ShipEvent eventMatchedFromCatalogAPI : events) {
      String eventDateTimeLocal = eventMatchedFromCatalogAPI.getEventDateLocal();
      if (eventDateTimeLocal != null) {
        Calendar eventDateLocal = DateTimeUtil.getLocalDateFromString(eventDateTimeLocal);
        if ((eventDateLocal.after(eventDateFrom) || eventDateLocal.equals(eventDateFrom))
            && (eventDateLocal.before(eventDateTo) || eventDateLocal.equals(eventDateTo))) {
          matchedEvents.add(eventMatchedFromCatalogAPI);
        } else {
          LOG.debug(
              "_message=\"EventLocalDate UNMATCHED by using DateRangeTime Stratergy\" localDateTime={} eventDateTimeLocal={}",
              localDateTime, eventDateTimeLocal);
        }
      } else {
        LOG.debug("_message=\"No Localdate time present in the catalog response\"");
      }
    }
    return matchedEvents;
  }


  public List<ShipEvent> matchEventsByUTCDate(List<ShipEvent> events, String utcDateTime) {
    List<ShipEvent> matchedEvents = new ArrayList<>();
    Calendar eventDateFrom = DateTimeUtil.getUTCDateWithHourFromString(utcDateTime, -1);
    Calendar eventDateTo = DateTimeUtil.getUTCDateWithHourFromString(utcDateTime, 1);

    for (ShipEvent eventMatchedFromCatalogAPI : events) {
      Calendar eventDateTimeUTC = eventMatchedFromCatalogAPI.getEventDateUTC();
      if (eventDateTimeUTC != null) {
        if ((eventDateTimeUTC.after(eventDateFrom) || eventDateTimeUTC.equals(eventDateFrom))
            && (eventDateTimeUTC.before(eventDateTo) || eventDateTimeUTC.equals(eventDateTo))) {
          matchedEvents.add(eventMatchedFromCatalogAPI);
        } else {
          LOG.debug(
              "_message=\"EventUtcDate UNMATCHED by using DateRangeTime Stratergy\" utcDateTime = {}  eventDateTimeUTC = {}",
              utcDateTime, eventDateTimeUTC);
        }
      } else {
        LOG.debug("_message=\"No EventUtcDate time present in the catalog response\"");

      }
    }
    return matchedEvents;
  }

}
