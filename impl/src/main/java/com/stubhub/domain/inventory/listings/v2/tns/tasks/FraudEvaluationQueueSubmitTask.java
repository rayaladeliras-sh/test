package com.stubhub.domain.inventory.listings.v2.tns.tasks;

import java.io.IOException;
import java.util.concurrent.Callable;

import javax.jms.JMSException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.SimpleMessageConverter;

import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.listings.v2.helper.ListingHelper;
import com.stubhub.domain.inventory.listings.v2.tns.dto.FraudEvaluationRequest;
import com.stubhub.newplatform.common.util.DyeUtil;

public class FraudEvaluationQueueSubmitTask implements Callable<Boolean> {

	private static final Logger log = LoggerFactory.getLogger(FraudEvaluationQueueSubmitTask.class);

	private final String listingId;
	private final String sellerId;
	private final String eventId;
	private final ListingStatus listingStatus;
	private final JmsTemplate fraudEvaluationMsgProducer;
	private final SimpleMessageConverter messageConverter;

	public FraudEvaluationQueueSubmitTask(String listingId, String sellerId, String eventId,
			ListingStatus listingStatus, JmsTemplate tnsListingFraudEvaluationMsgProducer,
			ListingHelper listingHelper) {
		super();
		this.listingId = listingId;
		this.eventId = eventId;
		this.sellerId = sellerId;
		this.listingStatus = listingStatus;
		this.fraudEvaluationMsgProducer = tnsListingFraudEvaluationMsgProducer;
		this.messageConverter = new SimpleMessageConverter();
	}

	private FraudEvaluationRequest buildFraudRequest(String listingId, String sellerId, String eventId,
			ListingStatus listingStatus) {
		FraudEvaluationRequest request = new FraudEvaluationRequest();
		request.setListingId(listingId);
		request.setSellerId(sellerId);
		request.setEventId(eventId);
		request.setListingStatus(listingStatus.name());
		request.setChangeType("CREATE");
		return request;

	}

	@Override
	public Boolean call() throws Exception {
		Boolean isSubmissionSuccess = Boolean.FALSE;
		String status = "TNS_LISTING_QUEUE_PROCESS_STARTED";
		FraudEvaluationRequest request = null;
		DyeUtil.beginDyedOperation("FraudEvaluationQueueSubmitTask-" + listingId + "--" + listingStatus);
		log.info(
				"api_domain=\"inventory\" api_resource=\"listing\" api_method=\"createListing\" listingId=\"{}\" listingStatus=\"{}\" message=\"Processing for TNS Fraud Evaluation Queue submission\"",
				listingId, listingStatus);
		try {
			log.info(
					"api_domain=\"inventory\" api_resource=\"listing\" api_method=\"createListing\" listingId=\"{}\" eventId=\"{}\" message=\"Processing listings for TNS Queue submission\"",
					listingId, eventId);
			request = buildFraudRequest(listingId, sellerId, eventId, listingStatus);
			log.info(
					"api_domain=\"inventory\" api_resource=\"listing\" api_method=\"createListing\"  listingId=\"{}\" eventId=\"{}\"  message=\"Processing of listings complete for TNS Queue submission\"",
					listingId, eventId);
			final FraudEvaluationRequest finalrequest = request;
			final String jsonString = object2JsonString(finalrequest);
			MessageCreator creator = new MessageCreator() {
				public javax.jms.Message createMessage(javax.jms.Session session) throws JMSException {
					javax.jms.MapMessage message = session.createMapMessage();
					message.setString("listingId", listingId);
					message.setString("listingDetails", jsonString);
					log.info("api_domain=\"inventory\" api_resource=\"listing\" api_method=\"createListing\" listingId=\"{}\" eventId=\"{}\" listingPayload=\"{}\" message=\"{}\" ",listingId, eventId, jsonString, messageConverter.fromMessage(message));
					return message;
				}
			};
			log.info(
					"api_domain=\"inventory\" api_resource=\"listing\" api_method=\"createListing\" listingId=\"{}\" listingStatus=\"{}\" message=\"Message creation complete\"",
					listingId, listingStatus);
			fraudEvaluationMsgProducer.send(creator);
			isSubmissionSuccess = Boolean.TRUE;
			status = "TNS_LISTING_QUEUE_PROCESS_SUCCESS";
			log.info(
					"api_domain=\"inventory\" api_resource=\"listing\" api_method=\"createListing\" listingId=\"{}\" listingStatus=\"{}\" message=\"Submitted successfuly to TNS Fraud Evaluation Queue\"",
					listingId, listingStatus);
		} catch (Throwable e) {
			status = "TNS_LISTING_QUEUE_PROCESS_FAIL";
			log.error(
					"api_domain=\"inventory\" api_resource=\"listing\" api_method=\"createListing\" listingId=\"{}\" listingStatus=\"{}\" message=\"Error while submitting to TNS Fraud Evaluation Queue\" exception=\"{}\"",
					listingId, listingStatus, ExceptionUtils.getFullStackTrace(e), e);
		} finally {
			log.info(
					"api_domain=\"inventory\" api_resource=\"listing\" api_method=\"createListing\" success={} listingId=\"{}\" listingStatus=\"{}\" message=\"Processing complete for TNS Fraud Evaluation Queue submission\"",
					status, listingId, listingStatus);
			DyeUtil.endDyedOperation();
		}

		return isSubmissionSuccess;
	}

	private String object2JsonString(Object object) throws IOException {
		return new ObjectMapper().writer().writeValueAsString(object);
	}

}
