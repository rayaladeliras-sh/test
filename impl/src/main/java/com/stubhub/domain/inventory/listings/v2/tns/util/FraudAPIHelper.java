package com.stubhub.domain.inventory.listings.v2.tns.util;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.infrastructure.common.exception.base.SHRuntimeException;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHBadRequestException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHResourceNotFoundException;
import com.stubhub.domain.inventory.listings.v2.tns.dto.FradEvaluationMessageRequest;
import com.stubhub.domain.inventory.listings.v2.util.EventHelper;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class FraudAPIHelper extends AbstractFraudAPIHelper {

	private static final Logger log = LoggerFactory.getLogger(FraudAPIHelper.class);

	private static final String DOMAIN = "inventory";

	@Autowired
	protected EventHelper eventHelper;

	@Autowired
	@Qualifier(value = "fraudEvaluationMsgProducer")
	private JmsTemplate fraudEvaluationMsgProducer;

	@Value("${tns.listing.fraud.evaluation.sendmail.queue}")
	private String listingEmailQueue;

	public boolean submitFraudListingEmailRequest(String listingId, String sellerId, Long fraudCheckStatusId) {
		log.info("api_domain={}, api_method={}, calling, listingId={}, sellerId={}, fraudCheckStatusId={}  ", DOMAIN,
				"submitFraudListingEmailRequest", listingId, sellerId, fraudCheckStatusId);
		try {

			SHMonitor mon = SHMonitorFactory.getMonitor();
			try {
				mon.start();

				Map<String, String> msg = new LinkedHashMap<>();
				msg.put("listingId", listingId);
				msg.put("fraudCheckStatusId", String.valueOf(fraudCheckStatusId));
				msg.put("sellerId", sellerId);

				fraudEvaluationMsgProducer.convertAndSend(listingEmailQueue, msg);

			} catch (Exception e) {
				log.error(
						"api_domain={} api_method={} message=\"Exception while making fraudEvaluationPostMessage API call\" listingId={}, sellerId={}, fraudCheckStatusId={} exception=\"{}\" ",
						DOMAIN, "submitFraudListingEmailRequest", listingId, sellerId, fraudCheckStatusId,
						ExceptionUtils.getFullStackTrace(e), e);
			} finally {
				mon.stop();
				log.info(SHMonitoringContext.get()
						+ "api_domain={} api_method={} _message=\"Called fraudEvaluationPostMessage api\", customerApiUrl={}, listingId={}, sellerId={}, fraudCheckStatusId={},   _respTime={}",
						DOMAIN, "submitFraudListingEmailRequest", null, listingId, sellerId,
						fraudCheckStatusId, mon.getTime());
			}

		} catch (Exception e) {
			log.error(
					"api_domain={} api_method={} message=\"Unknown exception while making fraudEvaluationPostMessage API call\" listingId={}, sellerId={}, fraudCheckStatusId={} exception=\"{}\" ",
					DOMAIN, "submitFraudListingEmailRequest", listingId, sellerId, fraudCheckStatusId,
					ExceptionUtils.getFullStackTrace(e), e);
		} finally {
			log.info(
					"api_domain={}, api_method={}, complete, apiURI={}, listingId={}, sellerId={}, fraudCheckStatusId={}  ",
					DOMAIN, "submitFraudListingEmailRequest", null, listingId, sellerId, fraudCheckStatusId);
		}
		return false;
	}


	protected static final String CATALOG_API_URL_WITH_SEAT_TRAITS = "tns.catalog.get.event.v3.api.url";

	public com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event getEventById(Long eventId) {
		//NOTE: this place cannot simply replaced with the shared method in EventHelper, as no
		//"APIContext is null; Cannot propagate context"
		com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event event = null;
		SHResourceNotFoundException shNfException = null;
		SHRuntimeException shException = null;
		String getEventApiUrl = null;
		try {
			log.info("api_domain={}, api_method={}, calling,  eventId={},  ", DOMAIN, "getEventById", getEventApiUrl,
					eventId);

			getEventApiUrl = getProperty(CATALOG_API_URL_WITH_SEAT_TRAITS,
					"http://api-int.stubprod.com/catalog/events/v3/{eventId}/?mode=internal&isSeatTraitsRequired=true&source=sell");
			getEventApiUrl = getEventApiUrl.replace("{eventId}", eventId.toString());
			log.info(
					"api_domain={}, api_method={}, message=\"get event information\" ,  getEventApiUrl={}, eventId={}, ",
					DOMAIN, "getEventById", getEventApiUrl, eventId);
			objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			ResponseEntity<Event> responseEvent = null;
			SHMonitor mon = SHMonitorFactory.getMonitor();
			try {
				mon.start();
				responseEvent = callAPI(null, Event.class, getEventApiUrl, HttpMethod.GET, "getEventById");
			} catch (Exception e) {
				log.error(
						"api_domain={}, api_method={}, message=\"Exception while callng GET event API\",  eventId={}, exception={}",
						DOMAIN, "getEventById", eventId, ExceptionUtils.getFullStackTrace(e), e);
			} finally {
				mon.stop();
				log.info(SHMonitoringContext.get() + " _operation=getEventById" + " _message= service call for eventId="
						+ eventId + "  _respTime=" + mon.getTime());
			}
			if (responseEvent != null) {
				if (HttpStatus.OK.equals(responseEvent.getStatusCode())) {
					log.info(
							"api_domain={}, api_method={}, message=\"Event API response success\",  eventId={}, responseCode={} ",
							DOMAIN, "getEventById", eventId, responseEvent.getStatusCode());
					event = responseEvent.getBody();
					eventHelper.validateIfEventExpired(event, false);
				} else {
					log.error(
							"api_domain={}, api_method={}, message=\"Invalid response code while fetching events\",  eventId={}, responseCode={} response={}",
							DOMAIN, "getEventById", eventId, responseEvent.getStatusCode(), responseEvent);
					shNfException = new SHResourceNotFoundException("eventId=" + eventId + " not found");
					shNfException.setErrorCode("inventory.listings.invalidEvent");
					throw shNfException;
				}
			} else {
				log.error(
						"api_domain={}, api_method={}, message=\"Invalid response while fetching events\",  eventId={}, ",
						DOMAIN, "getEventById", eventId);
				SHSystemException shSysException = new SHSystemException(
						"Invalid response while fetching event for eventId : " + eventId);
				shSysException.setErrorCode("inventory.listings.invalidEvent");
				throw shSysException;
			}
		} catch (SHResourceNotFoundException shnf) {
			log.error(
					"api_domain={}, api_method={}, message=\"Exception response while fetching events\",  eventId={}, exception={}",
					DOMAIN, "getEventById", eventId, ExceptionUtils.getFullStackTrace(shnf), shnf);
			throw shnf;
		} catch (SHBadRequestException shbr) {
			log.error(
					"api_domain={}, api_method={}, message=\"Exception response while fetching events\",  eventId={}, exception={}",
					DOMAIN, "getEventById", eventId, ExceptionUtils.getFullStackTrace(shbr), shbr);
			throw shbr;
		} catch (Exception e) {
			log.error(
					"api_domain={}, api_method={}, message=\"Exception response while fetching events\",  eventId={}, exception={}",
					DOMAIN, "getEventById", eventId, ExceptionUtils.getFullStackTrace(e), e);
			shException = new SHSystemException("An internal processing error occurred in the system", e);
			shException.setErrorCode("inventory.listings.systemError");
			throw shException;
		} finally {
			log.info("api_domain={}, api_method={}, complete,  eventId={}, getEventApiUrl={} ", DOMAIN, "getEventById",
					eventId, getEventApiUrl);
		}
		return event;
	}

}
