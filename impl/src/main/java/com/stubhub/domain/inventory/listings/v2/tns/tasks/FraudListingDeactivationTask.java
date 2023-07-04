package com.stubhub.domain.inventory.listings.v2.tns.tasks;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import com.stubhub.domain.inventory.listings.v2.tns.dto.FraudStatusUpdateRequest;
import com.stubhub.domain.inventory.listings.v2.tns.util.FraudEvaluationHelper;
import com.stubhub.newplatform.common.util.DyeUtil;

public class FraudListingDeactivationTask implements Runnable {
	
	private final FraudEvaluationHelper fraudEvaluationHelper;
	private static final Logger log = LoggerFactory.getLogger(FraudListingDeactivationTask.class);
	private static final String DOMAIN = "inventory";
	private final FraudStatusUpdateRequest request;
	private final JmsTemplate fraudListingDeactivationMsgProducer;
	private static final int MAX_RETRY_COUNT = 5; 
	
	public FraudListingDeactivationTask(FraudStatusUpdateRequest updateRequest, FraudEvaluationHelper fraudEvaluationHelper, JmsTemplate fraudListingDeactivationMsgProducer) {
		this.request = updateRequest;
		this.fraudEvaluationHelper = fraudEvaluationHelper;
		this.fraudListingDeactivationMsgProducer = fraudListingDeactivationMsgProducer;

	}
	
	@Override
	public void run() {
		DyeUtil.beginDyedOperation("FraudListingDeactivationTask-" + request.getListingId());
		Long listingId = request.getListingId();
		Long sellerId = request.getSellerId();
		Long fraudResolutionId = request.getFraudResolutionId();
		int retryCount = request.getRetryCount();
//		String deactivationReasonId = null;
		boolean success = false;
		try {
			// deactivationReasonId =
			// request.getUserDeactivationReasonId()!=null?request.getUserDeactivationReasonId().toString():null;
			log.info(
					"api_domain={}, api_method={}, message=\"Starting Seller Deactivation Process\", listingId={}, sellerId={}, fraudResolutionId={}, retryCount={}",
					DOMAIN, "call", listingId, sellerId, fraudResolutionId, retryCount);
			success = fraudEvaluationHelper.processLisitngDeactivationForSeller(request);
			log.info(
					"api_domain={}, api_method={}, message=\"Seller Deactivation Process Successful\", listingId={}, sellerId={}, fraudResolutionId={}, retryCount={}",
					DOMAIN, "call", listingId, sellerId, fraudResolutionId, retryCount);

		} catch (Throwable e) {
			success = false;
			log.error(
					"api_domain={}, api_method={} message=\"Exception during Fraud Seller Deactivation\" exception=\"{}\"",
					DOMAIN, "call", ExceptionUtils.getFullStackTrace(e), e);
		} finally {
			if (!success) {
				if (request.getRetryCount() < MAX_RETRY_COUNT) {
					request.setRetryCount(request.getRetryCount() + 1);
					log.info(
							"api_domain={}, api_method={}, message=\"Retry For Seller Deactivation Process \", success={} listingId={}, sellerId={}, fraudResolutionId={}, retryCount={} ",
							DOMAIN, "call", success, listingId, sellerId, fraudResolutionId, retryCount);
					fraudEvaluationHelper.submitToListingDeactivationQueue(fraudListingDeactivationMsgProducer,
							request);
				} else {
					log.info(
							"api_domain={}, api_method={}, message=\"Retry Attempts Exhausted For Seller Deactivation Process \", success={} listingId={}, sellerId={}, fraudResolutionId={}, retryCount={} maxRetryCount={}",
							DOMAIN, "call", success, listingId, sellerId, fraudResolutionId, retryCount,
							MAX_RETRY_COUNT);
				}
			}
			log.info(
					"api_domain={}, api_method={}, message=\"Seller Deactivation Process Complete\", success={} listingId={}, sellerId={}, fraudResolutionId={} retryCount={} ",
					DOMAIN, "call", success, listingId, sellerId, fraudResolutionId, retryCount);
			DyeUtil.endDyedOperation();
		}

	}

}
