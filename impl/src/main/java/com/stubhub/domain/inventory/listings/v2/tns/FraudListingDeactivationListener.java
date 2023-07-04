package com.stubhub.domain.inventory.listings.v2.tns;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.stubhub.domain.infrastructure.common.exception.derived.SHValidationErrorException;
import com.stubhub.domain.inventory.listings.v2.tns.dto.FraudStatusUpdateRequest;
import com.stubhub.domain.inventory.listings.v2.tns.tasks.FraudListingDeactivationTask;
import com.stubhub.domain.inventory.listings.v2.tns.util.FraudEvaluationHelper;
import com.stubhub.newplatform.common.util.DyeUtil;

public class FraudListingDeactivationListener implements MessageListener {

	private static final Logger log = LoggerFactory.getLogger(FraudEvaluationListingUpdateListener.class);
	private static final String DOMAIN = "inventory";

	private static final ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
	private static final int MAX_THREAD_COUNT = 20;
	private static final int CORE_THREAD_COUNT = 10;

	@Autowired
	private FraudEvaluationHelper fraudEvaluationHelper;
	
	@Autowired
	@Qualifier(value = "fraudListingDeactivationMsgProducer")
	private JmsTemplate fraudListingDeactivationMsgProducer;

	@PostConstruct
	public void init() {
		threadPool.setMaxPoolSize(MAX_THREAD_COUNT);
		threadPool.setCorePoolSize(CORE_THREAD_COUNT);
		threadPool.setWaitForTasksToCompleteOnShutdown(true);
		threadPool.initialize();
	}

	@Override
	public void onMessage(Message message) {
		DyeUtil.beginDyedOperation("FraudListingDeactivationListener");
		long startTime = System.currentTimeMillis();
		log.info("api_domain={}, api_method={}, message=\"Start processing message\" ", DOMAIN, "onMessage");
		try {
			MapMessage mapMsg = (MapMessage) message;
			FraudStatusUpdateRequest request = buildUpdateRequest(mapMsg);
			FraudListingDeactivationTask task = new FraudListingDeactivationTask(request, fraudEvaluationHelper, fraudListingDeactivationMsgProducer);
			threadPool.submit(task);
		} catch (Exception e) {
			log.error("api_domain={}, api_method={}, message=\"Exception while processing message\" exception={}",
					DOMAIN, "onMessage", ExceptionUtils.getFullStackTrace(e), e);
		} finally {
			DyeUtil.endDyedOperation();
			log.info("api_domain={}, api_method={}, message=\"End processing message\" duration={}", DOMAIN,
					"onMessage", (System.currentTimeMillis() - startTime));
		}

	}

	private FraudStatusUpdateRequest buildUpdateRequest(MapMessage mapMsg) throws NumberFormatException, JMSException {
		String listingId = mapMsg.getString("listingId");
		String sellerId = mapMsg.getString("sellerId");
		String fraudResolutionId = mapMsg.getString("fraudResolutionId");
		int retryCount = mapMsg.getInt("retryCount");
//		String deactivationReasonId = mapMsg.getString("userDeactivationReasonId");
		log.info(
				"api_domain={}, api_method={}, calling, listingId={},  sellerId={}, fraudResolutionId={} retryCount={}",
				DOMAIN, "fraudListingDeactivation", listingId, sellerId, fraudResolutionId , retryCount);
		FraudStatusUpdateRequest request = new FraudStatusUpdateRequest();
		boolean validationFlag = true;
		if (StringUtils.isNumeric(listingId)) {
			request.setListingId(Long.valueOf(listingId));
		} else {
			validationFlag = false;
			log.error(
					"api_domain={}, api_method={}, message=Invalid Listing ID, listingId={},   sellerId={}, fraudResolutionId={} retryCount={}",
					DOMAIN, "fraudListingDeactivation", listingId, sellerId, fraudResolutionId,retryCount );
		}
		if (StringUtils.isNumeric(sellerId)) {
			request.setSellerId(Long.valueOf(sellerId));
		} else {
			log.info(
					"api_domain={}, api_method={}, message=Seller ID is invalid, listingId={},   sellerId={}, fraudResolutionId={} retryCount={}",
					DOMAIN, "fraudListingDeactivation", listingId, sellerId, fraudResolutionId,retryCount );
		}
		if (StringUtils.isNumeric(fraudResolutionId)) {
			request.setFraudResolutionId(Long.valueOf(fraudResolutionId));
		} else {
			log.info(
					"api_domain={}, api_method={}, message=Fraud Resolution ID is invalid, listingId={},   sellerId={}, fraudResolutionId={} retryCount={}",
					DOMAIN, "fraudListingDeactivation", listingId, sellerId, fraudResolutionId,retryCount );
		}
		request.setRetryCount(retryCount);
//		if (StringUtils.isNumeric(deactivationReasonId)) {
//			request.setUserDeactivationReasonId(Long.valueOf(deactivationReasonId));
//		} else {
//			log.info(
//					"api_domain={}, api_method={}, message=User Deactivation Reason ID is invalid, listingId={},   sellerId={}, fraudResolutionId={},   deactivationReasonId={}",
//					DOMAIN, "fraudListingDeactivation", listingId, sellerId, fraudResolutionId, deactivationReasonId);
//		}
		if (!validationFlag) {
			throw new SHValidationErrorException("Fraud Status Update Validation Exception");
		}
		return request;
	}

}
