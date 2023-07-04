package com.stubhub.domain.inventory.v2.listings.eventmapper;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.common.entity.ErrorEnum;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.EventError;
import com.stubhub.domain.inventory.common.util.EventMappingException;
import com.stubhub.domain.inventory.v2.DTO.EventInfo;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.ShipEvents;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

@Component("eventMapperSearchHandler")
public class EventMapperSearchHandler {

    private static final String SEARCH_CATALOG_V3_EVENTS_SHIP_URL =
            "http://api-int.stubprod.com/search/catalog/v3/events/ship";

    private static final String SEARCH_CATALOG_EVENTS_SHIP_V3_URL =
            "search.catalog.events.ship.v3.url";
    
    private final String AUTHORIZATION = "Authorization";

    private final String USER_TOKEN = "Bearer ";

    private final static Logger LOG = LoggerFactory.getLogger(EventMapperSearchHandler.class);

    @Autowired
    private SvcLocator svcLocator;
    
    private ObjectMapper objectMapper;
    
    public ShipEvents searchEvents(Locale locale, EventInfo eventInfo, String userToken)
            throws EventMappingException {

        try {
            String eventMappingAPIUrl =
                    getProperty(SEARCH_CATALOG_EVENTS_SHIP_V3_URL, SEARCH_CATALOG_V3_EVENTS_SHIP_URL);

            eventMappingAPIUrl = eventMappingAPIUrl + "?venueName=" + URLEncoder
                    .encode("\"" + eventInfo.getVenue().trim() + "\"", "UTF-8").replace("+", "%20");

            if (StringUtils.trimToNull(eventInfo.getEventLocalDate()) != null) {
                eventMappingAPIUrl = eventMappingAPIUrl + "&eventDateLocal=" + URLEncoder
                        .encode(eventInfo.getEventLocalDate().substring(0, 10), "UTF-8").replace("+", "%20");
            } else if (StringUtils.trimToNull(eventInfo.getDate()) != null) {
                eventMappingAPIUrl = eventMappingAPIUrl + "&eventDate="
                        + URLEncoder.encode(eventInfo.getDate().substring(0, 10), "UTF-8").replace("+", "%20");
            }
            if (StringUtils.trimToNull(eventInfo.getCity()) != null) {
                eventMappingAPIUrl = eventMappingAPIUrl + "&city="
                        + URLEncoder.encode(eventInfo.getCity(), "UTF-8").replace("+", "%20");
            }
            if (StringUtils.trimToNull(eventInfo.getState()) != null) {
                eventMappingAPIUrl = eventMappingAPIUrl + "&state="
                        + URLEncoder.encode(eventInfo.getState(), "UTF-8").replace("+", "%20");
            }
            if (StringUtils.trimToNull(eventInfo.getCountry()) != null) {
                eventMappingAPIUrl = eventMappingAPIUrl + "&country="
                        + URLEncoder.encode(eventInfo.getCountry(), "UTF-8").replace("+", "%20");
            }
            if (StringUtils.trimToNull(eventInfo.getZipCode()) != null) {
                eventMappingAPIUrl = eventMappingAPIUrl + "&postalCode="
                        + URLEncoder.encode(eventInfo.getZipCode(), "UTF-8").replace("+", "%20");
            }
            if (eventInfo.getIsLanguageEnabled() != null && eventInfo.getIsLanguageEnabled()) {
                eventMappingAPIUrl = eventMappingAPIUrl + "&lang=true";
            }
            
            
            eventMappingAPIUrl = eventMappingAPIUrl + "&excludeBulkListings=false";
            
            LOG.info("eventMappingAPIUrl={}", eventMappingAPIUrl);

            WebClient webClient = svcLocator.locate(eventMappingAPIUrl.toString());
            webClient.accept(MediaType.APPLICATION_JSON);
            if (StringUtils.isNotEmpty(userToken)) {
                webClient.header(AUTHORIZATION, USER_TOKEN+userToken);
            }

            if (locale != null) {
                webClient.acceptLanguage(getWellFormedLocaleString(locale));
            }

            SHMonitor mon = SHMonitorFactory.getMonitor();
            Response response = null;
            try {
                mon.start();
                response = webClient.get();
            } finally {
                mon.stop();
                LOG.info(SHMonitoringContext.get() + " _operation=searchEvents" + " _message= service call " + "_respTime=" + mon.getTime());
            }

            if (response == null) {
                EventError error = new EventError(ErrorType.SYSTEMERROR, ErrorCode.SYSTEM_ERROR,
                        ErrorEnum.SYSTEM_ERROR.getMessage(), "Response is null");
                throw new EventMappingException(error);
            }

            ShipEvents events = null;
            LOG.debug("eventMappingAPIUrl = {}  responseStatus = {}", eventMappingAPIUrl,
                    response.getStatus());
            if (Response.Status.OK.getStatusCode() == response.getStatus()) {
                InputStream is = (InputStream) response.getEntity();
                objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                events = objectMapper.readValue(is, ShipEvents.class);
                LOG.debug("eventMappingAPIUrl={} responseStatus={}",
                        eventMappingAPIUrl, response.getStatus());
                return events;
/*                if (events != null && events.getEvents() != null && events.getNumFound() > 0) {
                    LOG.debug("eventMappingAPIUrl={} responseStatus={} EventMatchedCount={}",
                            eventMappingAPIUrl, response.getStatus(), events.getNumFound());
                    return events.getEvents();
                } else {
                    return new ArrayList<>();
                }*/
            } else {
                String message =
                        "Error occured while calling catalogEvent API error = " + response.getStatus();
                EventError error =
                        new EventError(ErrorType.NOT_FOUND, ErrorCode.INVALID_STATUS, message, message);
                throw new EventMappingException(error);
            }

        } catch (Exception e) {
            EventError error = new EventError(ErrorType.INPUTERROR, ErrorCode.EVENT_NOT_MAPPED,
                    ErrorEnum.EVENT_NOT_MAPPED.getMessage() + " " + e.getMessage(),
                    "No event found for the input event info");
            throw new EventMappingException(error);
        }
    }

/*    public List<ShipEvent> searchEventsByEventName(EventInfo eventInfo)
            throws EventMappingException {

        try {
            String eventMappingAPIUrl =
                    getProperty(SEARCH_CATALOG_EVENTS_SHIP_V3_URL, SEARCH_CATALOG_V3_EVENTS_SHIP_URL);

            eventMappingAPIUrl = eventMappingAPIUrl + "?eventName=" + URLEncoder
                    .encode(eventInfo.getName().trim(), "UTF-8").replace("+", "%20")+"&venueName=" + URLEncoder
                    .encode("\"" + eventInfo.getVenue().trim() + "\"", "UTF-8").replace("+", "%20");

            if (StringUtils.trimToNull(eventInfo.getEventLocalDate()) != null) {
                eventMappingAPIUrl = eventMappingAPIUrl + "&eventDateLocal=" + URLEncoder
                        .encode(eventInfo.getEventLocalDate().substring(0, 10), "UTF-8").replace("+", "%20");
            }else if (StringUtils.trimToNull(eventInfo.getDate()) != null) {
                eventMappingAPIUrl = eventMappingAPIUrl + "&eventDate="
                        + URLEncoder.encode(eventInfo.getDate().substring(0, 10), "UTF-8").replace("+", "%20");
            }
          
            if (eventInfo.getLocale() != null) {
                eventMappingAPIUrl = eventMappingAPIUrl + "&locale=" + eventInfo.getLocale().toString();
            }
            LOG.info("eventNameStringMappingAPIUrl={}", eventMappingAPIUrl);

            WebClient webClient = svcLocator.locate(eventMappingAPIUrl);
            webClient.accept(MediaType.APPLICATION_JSON);

            if (eventInfo.getLocale() != null) {
                webClient.acceptLanguage(getWellFormedLocaleString(eventInfo.getLocale()));
            }
            SHMonitor mon = SHMonitorFactory.getMonitor();
            Response response = null;
            try {
                mon.start();
                response = webClient.get();
            } finally {
                mon.stop();
                LOG.info(SHMonitoringContext.get() + " _operation=searchEventsByEventName" + " _message= service call " + "_respTime=" + mon.getTime());
            }

            if (response == null) {
                EventError error = new EventError(ErrorType.SYSTEMERROR, ErrorCode.SYSTEM_ERROR,
                        ErrorEnum.SYSTEM_ERROR.getMessage(), "Response is null");
                throw new EventMappingException(error);
            }

            ShipEvents events = null;
            LOG.debug("eventNameStringMappingAPIUrl = {}  responseStatus = {}", eventMappingAPIUrl,
                    response.getStatus());
            if (Response.Status.OK.getStatusCode() == response.getStatus()) {
                InputStream is = (InputStream) response.getEntity();
                objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                events = objectMapper.readValue(is, ShipEvents.class);
                if (events != null && events.getEvents() != null && events.getNumFound() > 0) {
                    LOG.debug("eventNameStringMappingAPIUrl={} responseStatus={} EventMatchedCount={}",
                            eventMappingAPIUrl, response.getStatus(), events.getNumFound());
                    return events.getEvents();
                } else {
                    return new ArrayList<>();
                }
            } else {
                String message =
                        "Error occured while calling catalogEvent API error = " + response.getStatus();
                EventError error =
                        new EventError(ErrorType.NOT_FOUND, ErrorCode.INVALID_STATUS, message, message);
                throw new EventMappingException(error);
            }

        } catch (Exception e) {
            EventError error = new EventError(ErrorType.INPUTERROR, ErrorCode.EVENT_NOT_MAPPED,
                    ErrorEnum.EVENT_NOT_MAPPED.getMessage() + " " + e.getMessage(),
                    "No event found for the input event info");
            throw new EventMappingException(error);
        }
    } */

    public static String getWellFormedLocaleString(Locale locale) {
        return locale.getLanguage() + "-" + locale.getCountry();
    }

    protected String getProperty(String propertyName, String defaultValue) {
        return MasterStubHubProperties.getProperty(propertyName, defaultValue);
    }
}
