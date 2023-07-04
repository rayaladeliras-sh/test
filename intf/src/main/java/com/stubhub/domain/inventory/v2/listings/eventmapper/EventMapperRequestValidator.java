package com.stubhub.domain.inventory.v2.listings.eventmapper;

import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.entity.ErrorEnum;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.EventError;
import com.stubhub.domain.inventory.common.util.EventMappingException;
import com.stubhub.domain.inventory.v2.DTO.EventInfo;

@Component("eventMapperRequestValidator")
public class EventMapperRequestValidator {

  public void validate(EventInfo eventInfo) {

    ErrorEnum errorNum = null;

    if (StringUtils.trimToNull(eventInfo.getVenue()) == null) {
      errorNum = ErrorEnum.MISSING_EVENT_INFO;
    }

    else if (StringUtils.trimToNull(eventInfo.getName()) == null) {
      errorNum = ErrorEnum.MISSING_EVENT_INFO;
    }

    else if ((StringUtils.trimToNull(eventInfo.getDate()) == null)
        && (StringUtils.trimToNull(eventInfo.getEventLocalDate()) == null)) {
      errorNum = ErrorEnum.MISSING_EVENT_INFO;
    } else {
      try {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        if (StringUtils.trimToNull(eventInfo.getEventLocalDate()) != null) {
          dateFormat.parse(eventInfo.getEventLocalDate());
        }
        if (StringUtils.trimToNull(eventInfo.getDate()) != null) {
          dateFormat.parse(eventInfo.getDate());
        }
      } catch (Exception e) {
        errorNum = ErrorEnum.INVALID_DATE_FORMAT;
      }
    }

    if (errorNum != null) {
      EventError error = new EventError(ErrorType.INPUTERROR, ErrorCode.INPUT_ERROR,
          errorNum.getMessage(), "Validation of EventMapper Api failed");
      throw new EventMappingException(error);
    }
  }

}
