package com.stubhub.domain.inventory.listings.v2.newflow.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.fulfillment.window.v1.intf.EventInhanddate;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.EventInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.FulfillmentInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.HeaderInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.newplatform.common.util.DateUtil;

@Component
public class InhandDateHelper {

  private static final Logger logger = LoggerFactory.getLogger(InhandDateHelper.class);

  private final static String SELLER_ID_HEADER = " sellerId=";
  private final static String APP_NAME_HEADER = " appName=";
  private static final String SPACE = " ";

  private static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
  private SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);


  public void processInhandDate(ListingDTO listingDTO, Integer tm) {
    // get in-hand date settings from fulfillment info
    Map<String, EventInhanddate> inHandDateSettings =
        listingDTO.getFulfillmentInfo().getInHandDateSettings();
    Calendar eihDate = null;
    Calendar lihDate = null;
    if (inHandDateSettings != null && tm != null) {
      String eihDateString = null;
      String lihDateString = null;
      TicketMedium ticketMedium = TicketMedium.getTicketMedium(tm);
      String ticketMediumName = ticketMedium.name().toLowerCase();

      eihDateString = inHandDateSettings.get(ticketMediumName).getEihd();
      lihDateString = inHandDateSettings.get(ticketMediumName).getLihd();

      sdf.setLenient(false);
      sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
      if (eihDateString != null) {
        eihDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        try {
          eihDate.setTime(sdf.parse(eihDateString));
        } catch (ParseException e) {
          logger.error("message=\"error parsing earliest in-hand date\" eihDateString={}",
              eihDateString);
          throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.invalidDateFormat);
        }
      }

      if (lihDateString != null) {
        lihDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        try {
          lihDate.setTime(sdf.parse(lihDateString));
        } catch (ParseException e) {
          logger.error("message=\"error parsing listing in-hand date\" listingInhandDate={}",
              lihDateString);
          throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidInhanddate);
        }
      }
    }
    if (lihDate != null) {
      validateAndSetInHandDate(listingDTO, eihDate, lihDate);
    }
  }

  private void validateAndSetInHandDate(ListingDTO listingDTO, Calendar eihDate, Calendar lihDate) {

    EventInfo eventInfo = listingDTO.getEventInfo();
    FulfillmentInfo fulfillmentInfo = listingDTO.getFulfillmentInfo();

    Calendar eventDateUTC = DateUtil.convertCalendarToUtc(eventInfo.getEventDate());

    // Validate and set inhand date
    Calendar currentDateUTCBOD = DateUtil.getNowCalUTC();
    setHourMinuteSeconds(currentDateUTCBOD, 0, 0, 0);

    Calendar currentDateUTCEOD = DateUtil.getNowCalUTC();
    setHourMinuteSeconds(currentDateUTCEOD, 23, 59, 59);

    Calendar listingIHDate = stringToCalendar(listingDTO.getListingRequest().getInhandDate());

    // convert the passed IHDate (assumed as TZ of event) to UTC date
    Calendar listingInhandDateEventLocalBOD = new GregorianCalendar(eventInfo.getTimeZone());
    listingInhandDateEventLocalBOD.set(listingIHDate.get(Calendar.YEAR),
        listingIHDate.get(Calendar.MONTH), listingIHDate.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
    Calendar listingInhandDateUTCBOD = DateUtil
        .convertCalendarToNewTimeZone(listingInhandDateEventLocalBOD, TimeZone.getTimeZone("UTC"));

    Calendar listingInhandDateEventLocaleEOD = new GregorianCalendar(eventInfo.getTimeZone());
    listingInhandDateEventLocaleEOD.set(listingIHDate.get(Calendar.YEAR),
        listingIHDate.get(Calendar.MONTH), listingIHDate.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
    Calendar listingInhandDateUTCEOD =
        DateUtil.convertCalendarToUtc(listingInhandDateEventLocaleEOD);

    // in-hand date cannot be after the event date
    if (listingInhandDateUTCBOD.after(eventDateUTC) && listingDTO.getListingRequest().isAdjustInhandDate()!=null && !listingDTO.getListingRequest().isAdjustInhandDate()) {
      logger.error(
          "message=\"The in hand date provided is after the latest possible in hand date or before the earliest possible in hand date for the event\" listingInhandDate={}",
          listingInhandDateUTCBOD);
      throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidInhanddate);
    }
    listingIHDate = listingInhandDateUTCBOD;
    logger.info("message=\"listing InhandDate \" listingInhandDate={}", listingIHDate);

    // If IH date is before event EIHD, then set to EIHD
    if (eihDate != null && listingInhandDateUTCEOD.before(eihDate)) {
      // We used to issue error here but lets be lenient
      listingIHDate = eihDate;
    } else {
      /*
       * If todays date is before LIH and if listing in-hand/delivery date does not fall between
       * todays date and LIH, then throw exception
       */
      StringBuilder logLihDate = new StringBuilder();
      logLihDate.append(lihDate.get(Calendar.MONTH) + 1).append("/")
          .append(lihDate.get(Calendar.DAY_OF_MONTH)).append("/")
          .append(lihDate.get(Calendar.YEAR));

      if (currentDateUTCEOD.before(lihDate)) {
        if (listingInhandDateUTCBOD.after(lihDate)) {
          if (listingDTO.getListingRequest().isAdjustInhandDate()) {
            StringBuilder logString =
                buildLogDataString(listingDTO, listingIHDate, eventDateUTC, logLihDate);
            logger.info(logString.toString());
            listingIHDate = lihDate;
            fulfillmentInfo.setInHandDateAdjusted(true);
          } else {
            logger.error(
                "message=\"The in hand date provided is after the latest possible in hand date or before the earliest possible in hand date for the event\" listingInhandDate={}",
                listingInhandDateUTCBOD);
            throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidInhanddate);
          }
        }
      } else { // if current date is GT or EQ LIH date
        if (listingInhandDateUTCBOD.after(currentDateUTCEOD)) {
          // if listing date is after current date, issue error
          if (listingDTO.getListingRequest().isAdjustInhandDate()) {
            StringBuilder logString =
                buildLogDataString(listingDTO, listingIHDate, eventDateUTC, logLihDate);
            logger.info(logString.toString());
            listingIHDate = currentDateUTCBOD;
            fulfillmentInfo.setInHandDateAdjusted(true);
          } else {
            logger.error(
                "message=\"The in hand date provided is after the latest possible in hand date or before the earliest possible in hand date for the event\" listingInhandDate={}",
                listingInhandDateUTCBOD);
            throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidInhanddate);
          }
        }
      }
    }

    fulfillmentInfo.setInHandDate(listingIHDate);

    if (!fulfillmentInfo.getInHandDate().after(currentDateUTCEOD)) {
      fulfillmentInfo.setDeclaredInhandDate(listingIHDate);
    }
  }

  /**
   * To set BOD and EOD for calendar
   * 
   * @param cal
   * @param hour
   * @param min
   * @param sec
   */
  private void setHourMinuteSeconds(Calendar cal, int hour, int min, int sec) {
    cal.set(Calendar.HOUR_OF_DAY, hour);
    cal.set(Calendar.MINUTE, min);
    cal.set(Calendar.SECOND, sec);
  }

  /**
   * 
   * @param inHandDate
   * @return Calendar instance converting a String date to a Calendar instance.
   */
  private Calendar stringToCalendar(String inHandDate) {

    final String dateFormat = "yyyy-MM-dd";
    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
    Calendar cal = Calendar.getInstance();
    try {
      cal.setTime(sdf.parse(inHandDate));
    } catch (ParseException e) {
      logger.error("message=\"error parsing inhanddate from listing request\" inhandDate={}",
          inHandDate);
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.invalidDateFormat);
    }
    return cal;
  }

  private StringBuilder buildLogDataString(ListingDTO listingDTO, Calendar listingIHDate,
      Calendar eventDateUTC, StringBuilder logLihDate) {
    StringBuilder logString = new StringBuilder();
    Listing listing = listingDTO.getDbListing();
    try {
      logString.append("Adjusting listing request in hand date with LIHD");
      logString.append(geteLogStringFromHeader(listingDTO)).append(SPACE);
      logString.append("LIHD=").append(logLihDate).append(SPACE);
      logString.append("listingIHDate=")
          .append(new StringBuilder().append(listingIHDate.get(Calendar.MONTH) + 1).append("/")
              .append(listingIHDate.get(Calendar.DAY_OF_MONTH)).append("/")
              .append(listingIHDate.get(Calendar.YEAR)))
          .append(SPACE);
      logString.append("Adjusted listingIHDate=").append(logLihDate).append(SPACE);
      logString.append("externalListingId=").append(listing.getExternalId()).append(SPACE);
      logString.append("eventId=").append(listing.getEventId()).append(SPACE);
      logString.append("eventDate=")
          .append(new SimpleDateFormat("MM/dd/yyyy").format(eventDateUTC.getTime())).append(SPACE);
      logString.append("quantity=").append(listing.getQuantity()).append(SPACE);
      logString.append("deliveryOption=" + listing.getDeliveryOption()).append(SPACE);
      if (listing.getListPrice() != null) {
        logString.append("listPrice=").append(listing.getListPrice().getAmount()).append(SPACE)
            .append("currency=").append(listing.getListPrice().getCurrency()).append(SPACE);
      }
    } catch (Exception e) {
      logger.error("message=\"Error while logging the adjusted in hand date\" inhandDate={}",
          logLihDate);
    }
    return logString;
  }

  private String geteLogStringFromHeader(ListingDTO dto) {

    HeaderInfo headerInfo = dto.getHeaderInfo();
    StringBuilder logStringFromHeader = new StringBuilder();
    logStringFromHeader.append(SELLER_ID_HEADER).append(dto.getDbListing().getSellerId())
        .append(APP_NAME_HEADER).append(headerInfo.getSubscriber());

    return logStringFromHeader.toString();
  }

}
