package com.stubhub.domain.inventory.listings.v2.newflow.helper.api;

import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.infrastructure.common.exception.base.SHRuntimeException;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHResourceNotFoundException;
import com.stubhub.domain.inventory.listings.v2.util.CommonUtils;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Locale;

@Component
public class EventV3APIHelper {

    private final static Logger log = LoggerFactory.getLogger(EventV3APIHelper.class);
    private static final String CATALOG_API_URL = "inventory.catalog.get.event.v3.api.url";
    private static final String CATALOG_API_V3ADAPTER_V4_URL = "inventory.catalog.event.v3AdapterOnV4.api.url";

    @Autowired
    private SvcLocator svcLocator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment environment;

    @Autowired
    private ProxyHelper proxyHelper;

    private RestTemplate restTemplateWithEProxy;

    @PostConstruct
    public void init() {
        restTemplateWithEProxy = proxyHelper.createProxyRestTemplate();
    }

    @Cacheable("eventV3Cache")
    public com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event getEventV3ById(Long eventId, Locale pLocale, boolean getSeatTraits) {
        boolean enableCloudCatalogAPI = Boolean.parseBoolean(getProperty("inventory.catalog.cloud.api.enabled", "false"));
        String getEventApiUrl = getProperty(enableCloudCatalogAPI ? CATALOG_API_V3ADAPTER_V4_URL : CATALOG_API_URL, null);
        if (getEventApiUrl == null) {
            throw new SHRuntimeException("eventAPI not found");
        }
        getEventApiUrl = getEventApiUrl.replace("{eventId}", eventId.toString());
        if (getSeatTraits) {
            getEventApiUrl = getEventApiUrl + "&isSeatTraitsRequired=true";
        }
        log.info("_message=\"get event information\" eventId={} getEventApiUrl={}", eventId, getEventApiUrl);

        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SHRuntimeException shException;
        try {

            com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event event = null;
            int status;

            SHMonitor mon = SHMonitorFactory.getMonitor();
            mon.start();
            try {

                if (enableCloudCatalogAPI) {
                    HttpHeaders requestHeaders = new HttpHeaders();
                    requestHeaders.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                    if (pLocale != null) {
                        requestHeaders.set("Accept-Language", CommonUtils.getWellFormedLocaleString(pLocale));
                    }
                    HttpEntity<String> entity = new HttpEntity<>("", requestHeaders);
                    String responseBody;
                    try {
                        ResponseEntity<String> responseEntity = restTemplateWithEProxy.exchange(getEventApiUrl, HttpMethod.GET, entity, String.class);
                        status = responseEntity.getStatusCode().value();
                        responseBody = responseEntity.getBody();
                        event = objectMapper.readValue(responseBody, com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event.class);
                        log.info("_message=\"cloud catalog api response successful\" eventId={}", eventId);

                    } catch (HttpStatusCodeException e) {
                        responseBody = e.getResponseBodyAsString();
                        status = e.getStatusCode().value();
                        log.error("_message=\"cloud catalog api response event error\" eventId={} status={} responseBody={}", eventId, status, responseBody);
                    }

                } else {
                    WebClient webClient = svcLocator.locate(getEventApiUrl);
                    webClient.accept(MediaType.APPLICATION_JSON);
                    if (pLocale != null) {
                        webClient.acceptLanguage(CommonUtils.getWellFormedLocaleString(pLocale));
                    }
                    Response response = webClient.get();
                    status = response.getStatus();
                    if (Response.Status.OK.getStatusCode() == status) {
                        log.info("_message=\"catalog api response successful\" eventId={}", eventId);
                        InputStream is = (InputStream) response.getEntity();
                        event = objectMapper.readValue(is, com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event.class);
                    }
                }

            } finally {
                mon.stop();
                log.info(SHMonitoringContext.get() + " _operation=getEventV3ById" + " _message= service call for eventId=" + eventId + "  _respTime=" + mon.getTime());
            }

            if (Response.Status.OK.getStatusCode() == status) {
                return event;
            } else if (Response.Status.NOT_FOUND.getStatusCode() == status
                    || Response.Status.BAD_REQUEST.getStatusCode() == status) {
                log.error("_message=\"catalog api response event NOT_FOUND\" eventId={}", eventId);
                shException = new SHResourceNotFoundException("eventId=" + eventId + " not found");
                shException.setErrorCode("inventory.listings.invalidEvent");
                throw shException;
            } else {
                log.error("_message=\"catalog api response statusCode\" eventId={} responseStatusCode={}", eventId, status);
                throw new SHRuntimeException("Unknown response statusCode=" + status + " eventId=" + eventId );
            }
        } catch (SHRuntimeException shr) {
            throw shr;
        } catch (Exception e) {
            log.error("_message=\"unknown exception caught while making catalog api call\" eventId={} errorClass={} errorMessage={}", eventId, e.getClass().toString(), e.getMessage());
            shException = new SHSystemException("An internal processing error occurred in the system", e);
            shException.setErrorCode("inventory.listings.systemError");
            throw shException;
        }
    }

    protected String getProperty(String propertyName, String defaultValue) {
        return environment.getProperty(propertyName, defaultValue);
    }
}
