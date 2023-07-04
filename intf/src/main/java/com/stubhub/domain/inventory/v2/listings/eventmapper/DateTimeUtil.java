package com.stubhub.domain.inventory.v2.listings.eventmapper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.entity.ErrorEnum;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.EventError;
import com.stubhub.domain.inventory.common.util.EventMappingException;

public class DateTimeUtil {

  private static final String YYYY_MM_DD_T_HH_MM = "yyyy-MM-dd'T'HH:mm";
  private static final Logger LOG = LoggerFactory.getLogger(DateTimeUtil.class);

  public static Date convertInputDate(String dateTime) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(YYYY_MM_DD_T_HH_MM);
    Date date = null;
    try {
      date = dateFormat.parse(dateTime);
    } catch (Exception e) {
      LOG.error("Invalid event date in the request", e);
      EventError error = new EventError(ErrorType.INPUTERROR, ErrorCode.INVALID_EVENT_DATE,
          ErrorEnum.INVALID_EVENT_DATE.getMessage(), "event");
      throw new EventMappingException(error);
    }

    return date;
  }

  public static Calendar getLocalDateFromString(String dateString) {
    Date localDateRequested = convertInputDate(dateString);
    Calendar inputDateLocal = Calendar.getInstance();
    inputDateLocal.setTime(localDateRequested);
    inputDateLocal.set(Calendar.SECOND, 0);
    inputDateLocal.set(Calendar.MILLISECOND, 0);
    return inputDateLocal;
  }

  public static Calendar getLocalDateWithHourFromString(String dateString, int hour) {
    Date localDateRequested = convertInputDate(dateString);
    Calendar inputDateLocal = Calendar.getInstance();
    inputDateLocal.setTime(localDateRequested);
    inputDateLocal.add(Calendar.HOUR, hour);
    return inputDateLocal;
  }

  public static Calendar getUTCDateFromString(String dateString) {
    Date utcDateRequested = convertInputDate(dateString);
    Calendar inputDateUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    inputDateUTC.setTime(utcDateRequested);
    inputDateUTC.set(Calendar.SECOND, 0);
    inputDateUTC.set(Calendar.MILLISECOND, 0);
    return inputDateUTC;
  }

  public static Calendar getUTCDateWithHourFromString(String dateString, int hour) {
    Date utcDateRequested = convertInputDate(dateString);
    Calendar inputDateUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    inputDateUTC.setTime(utcDateRequested);
    inputDateUTC.add(Calendar.HOUR, hour);
    return inputDateUTC;
  }

  public static Calendar getUTCDateFromDate(Calendar utcDate) {
    Calendar inputDateUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    inputDateUTC.setTime(utcDate.getTime());
    inputDateUTC.set(Calendar.SECOND, 0);
    inputDateUTC.set(Calendar.MILLISECOND, 0);
    return inputDateUTC;
  }

}
