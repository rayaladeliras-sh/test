package com.stubhub.domain.inventory.listings.v2.newflow.helper.api;

import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.SeatingSection;
import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.SeatingZone;
import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfiguration;
import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfigurations;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.listings.v2.util.CommonUtils;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Locale;

@Component
public class VenueConfigV3ApiHelper {
    private final static Logger log = LoggerFactory.getLogger(VenueConfigV3ApiHelper.class);

    @Autowired
    private SvcLocator svcLocator;

    @Autowired
    private ObjectMapper objectMapper;

    @Cacheable("venueConfigByEventIdCache")
    public VenueConfiguration getVenueDetails(Long eventId) {
        VenueConfigurations venueConfigs = null;
        String venueConfigUrl = getProperty("catalog.get.venue.config.v3.api.url", "http://api-int.stubprod.com/catalog-read/v3/venues/venueconfigurations?eventId={eventId}&source=sell&isSectionZoneRequired=true");
        venueConfigUrl = venueConfigUrl.replace("{eventId}", eventId.toString());

        log.info("_message=\"get venue information\" eventId={} venueConfigUrl={}", eventId, venueConfigUrl);

        try {
            objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            WebClient webClient = svcLocator.locate(venueConfigUrl);
            webClient.accept(MediaType.APPLICATION_JSON);

            SHMonitor mon = SHMonitorFactory.getMonitor();
            Response response = null;
            try {
                mon.start();
                response = webClient.get();
            } finally {
                mon.stop();
                log.info(SHMonitoringContext.get() + " _operation=getVenueDetails" + " _message= service call for eventId=" + eventId + "  _respTime=" + mon.getTime());
            }

            if (Response.Status.OK.getStatusCode() == response.getStatus()) {
                log.info("_message=\"venue api call successful\" eventId={}",eventId);
                InputStream is = (InputStream) response.getEntity();
                venueConfigs = objectMapper.readValue(is, VenueConfigurations.class);
            } else {
                log.error("_message=\"venue api call failed\" responseCode={}, eventId={}",response.getStatus(), eventId);
            }
        } catch (Exception e) {
            log.error("_message=\"Exception occured while calling venue api\", eventId={}", eventId, e);
        }

        if(venueConfigs != null) {
            return venueConfigs.getVenueConfiguration().get(0);
        }
        return null;
    }

    @Cacheable("localizedSectionCache")
    public SeatingSection getLocalizedSeatingSection(Long sectionId, Locale locale) {
        String seatingSectionUrl = getProperty("catalog.get.venue.seatingsection.v3.api.url", "http://api-int.stubprod.com/catalog-read/v3/venues/seatingSections/{sectionId}?source=sell");
        seatingSectionUrl = seatingSectionUrl.replace("{sectionId}", sectionId.toString());

        log.info("_message=\"get localized seating information\" sectionId={} seatingSectionUrl={}", sectionId, seatingSectionUrl);

        try {
            objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            WebClient webClient = svcLocator.locate(seatingSectionUrl);
            webClient.accept(MediaType.APPLICATION_JSON);
            if (locale != null) {
                webClient.acceptLanguage(CommonUtils.getWellFormedLocaleString(locale));
            }
            SHMonitor mon = SHMonitorFactory.getMonitor();
            Response response = null;
            try {
                mon.start();
                response = webClient.get();
            } finally {
                mon.stop();
                log.info(SHMonitoringContext.get() + " _operation=getLocalizedSeatingSection" + " _message=seating section  service call  for sectionId=" + sectionId + "  _respTime=" + mon.getTime());
            }

            if (Response.Status.OK.getStatusCode() == response.getStatus()) {
                log.info("_message=\"venue seating section api call successful\" sectionId={}",sectionId);
                InputStream is = (InputStream) response.getEntity();
                return objectMapper.readValue(is, SeatingSection.class);

            } else {
                log.error("_message=\"venue seating section  api call failed\" responseCode={}, sectionId={}",response.getStatus(), sectionId);
            }
        } catch (Exception e) {
            log.error("_message=\"Exception occured while calling venue seating section api\", sectionId={}", sectionId, e);
        }

        return null;
    }

    @Cacheable("localizedZoneCache")
    public SeatingZone getLocalizedSeatingZone(Long zoneId, Locale locale) {
        String seatingZoneUrl = getProperty("catalog.get.venue.seatingzone.v3.api.url", "http://api-int.stubprod.com/catalog-read/v3/venues/seatingZones/{zoneId}?source=sell");
        seatingZoneUrl = seatingZoneUrl.replace("{zoneId}", zoneId.toString());

        log.info("_message=\"get localized zone information\" zoneId={} seatingZonUrl={}", zoneId, seatingZoneUrl);

        try {
            objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            WebClient webClient = svcLocator.locate(seatingZoneUrl);
            webClient.accept(MediaType.APPLICATION_JSON);
            if (locale != null) {
                webClient.acceptLanguage(CommonUtils.getWellFormedLocaleString(locale));
            }
            SHMonitor mon = SHMonitorFactory.getMonitor();
            Response response = null;
            try {
                mon.start();
                response = webClient.get();
            } finally {
                mon.stop();
                log.info(SHMonitoringContext.get() + " _operation=getLocalizedZoneName" + " _message=seating zone  service call  for zoneId=" + zoneId + "  _respTime=" + mon.getTime());
            }

            if (Response.Status.OK.getStatusCode() == response.getStatus()) {
                log.info("_message=\"venue seating zone api call successful\" zoneId={}",zoneId);
                InputStream is = (InputStream) response.getEntity();
                return objectMapper.readValue(is, SeatingZone.class);

            } else {
                log.error("_message=\"Venue seating zone  api call failed\" responseCode={}, zoneId={}",response.getStatus(), zoneId);
            }
        } catch (Exception e) {
            log.error("_message=\"Exception occured while calling venue seating zone api\", zoneId={}", zoneId, e);
        }

        return null;
    }

    protected String getProperty(String propertyName, String defaultValue) {
        return MasterStubHubProperties.getProperty(propertyName, defaultValue);
    }
}
