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


@Component("eventMapperExactDateTimeMatcher")
public class EventMapperExactDateTimeMatcher {

  private final static Logger LOG = LoggerFactory.getLogger(EventMapperExactDateTimeMatcher.class);

  public List<ShipEvent> matchEventsByLocalDate(List<ShipEvent> events, String dateTime) {

    List<ShipEvent> matchedEvents = null;
    if (StringUtils.trimToNull(dateTime) != null) {
      matchedEvents = matchLocalDateTime(events, dateTime);
    } else {
      String message = "LocalDate passed in the request is null.";
      EventError error =
          new EventError(ErrorType.INPUTERROR, ErrorCode.INPUT_ERROR, message, message);
      throw new EventMappingException(error);
    }

    return matchedEvents;
  }

  private List<ShipEvent> matchLocalDateTime(List<ShipEvent> events, String dateTime) {
    List<ShipEvent> matchedEvents = new ArrayList<>();
    for (ShipEvent eventMatchedFromCatalogAPI : events) {
      String eventDateTimeLocal = eventMatchedFromCatalogAPI.getEventDateLocal();
      if (eventDateTimeLocal != null) {
        Calendar inputDateLocal = DateTimeUtil.getLocalDateFromString(dateTime);
        Calendar eventDateLocal = DateTimeUtil.getLocalDateFromString(eventDateTimeLocal);
        if (inputDateLocal.equals(eventDateLocal)) {
          matchedEvents.add(eventMatchedFromCatalogAPI);
        } else {
          LOG.debug("_message=\"EventLocalDate UNMATCHED by using ExactDateTime Stratergy\"");
        }
      } else {
        LOG.debug("_message=\"No Localdate time present in the catalog response\"");
      }
    }
    return matchedEvents;
  }

  public List<ShipEvent> matchEventsByUTCDate(List<ShipEvent> events, String dateTime) {
    List<ShipEvent> matchedEvents = new ArrayList<>();
    if (StringUtils.trimToNull(dateTime) != null) {
      for (ShipEvent eventMatchedFromCatalogAPI : events) {
        if (eventMatchedFromCatalogAPI.getEventDateUTC() != null) {
          Calendar inputDateUTC = DateTimeUtil.getUTCDateFromString(dateTime);
          Calendar eventDateUTC =
              DateTimeUtil.getUTCDateFromDate(eventMatchedFromCatalogAPI.getEventDateUTC());
          if (inputDateUTC.equals(eventDateUTC)) {
            matchedEvents.add(eventMatchedFromCatalogAPI);
          } else {
            LOG.debug("_message=\"EventUtcDate UNMATCHED by using ExactDateTime Stratergy\"");

          }
        } else {
          LOG.debug("_message=\"No EventUtcDate time present in the catalog response\"");
        }
      }
    } else {
      String message = "UTC Date passed in the request is null.";
      EventError error =
          new EventError(ErrorType.INPUTERROR, ErrorCode.INPUT_ERROR, message, message);
      throw new EventMappingException(error);
    }
    return matchedEvents;
  }

}
