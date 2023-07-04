package com.stubhub.domain.inventory.v2.listings.eventmapper;

import java.util.Locale;

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
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.ShipEvent;

@Component("eventMapperAdaptor")
public class EventMapperAdaptor {

  private static final Logger LOG = LoggerFactory.getLogger(EventMapperAdaptor.class);

  @Autowired
  @Qualifier("eventMapperResolver")
  private EventMapperResolver eventMapperResolver;

  @Autowired
  @Qualifier("eventMapperRequestValidator")
  private EventMapperRequestValidator eventMapperRequestValidator;
  
  public ShipEvent mapEvent(Locale locale, EventInfo eventInfo, String sellerId, String userToken) throws EventMappingException {
    ShipEvent event = null;
    try {
      eventMapperRequestValidator.validate(eventInfo);
      event = eventMapperResolver.resolveEvent(locale, eventInfo, sellerId, userToken);
      if (event == null) {
        LOG.debug("_message=\"No event match found\" request={}", eventInfo.formatForLog());
        return event;
      } 
      return event;

    } catch (EventMappingException e) {
      LOG.error(
          "_message=\"Error occured while calling mapEvent\" errorType = {} errorMessage = {} errorCode = {} eventMapRequest = {}",
          e.getEventError().getType(), e.getEventError().getMessage(),
          e.getEventError().getErrorCode(), eventInfo.formatForLog());
      throw e;
    } catch (Exception e) {
      LOG.error(
          "_message=\"Error occured while calling mapEvent API from SellAPI\" error={} eventMapRequest = {}",
          e.getMessage(), eventInfo.formatForLog());
      EventError error = new EventError(ErrorType.SYSTEMERROR, ErrorCode.SYSTEM_ERROR,
          ErrorEnum.SYSTEM_ERROR.getMessage(), e.getMessage());
      throw new EventMappingException(error);
    }   
  }

  public ShipEvent mapEvent(Locale locale, EventInfo eventInfo, String sellerId)
      throws EventMappingException {
    return mapEvent(locale, eventInfo, sellerId, null);
  }

  public void setEventMapperRequestValidator(
      EventMapperRequestValidator eventMapperRequestValidator) {
    this.eventMapperRequestValidator = eventMapperRequestValidator;
  }

}
