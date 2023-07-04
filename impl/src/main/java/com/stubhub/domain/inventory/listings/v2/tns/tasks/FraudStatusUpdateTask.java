package com.stubhub.domain.inventory.listings.v2.tns.tasks;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import com.stubhub.domain.inventory.listings.v2.tns.dto.FraudCheckStatus;
import com.stubhub.domain.inventory.listings.v2.tns.dto.FraudStatusUpdateRequest;
import com.stubhub.domain.inventory.listings.v2.tns.util.FraudEvaluationHelper;
import com.stubhub.newplatform.common.util.DyeUtil;

public class FraudStatusUpdateTask implements Runnable {

	private final FraudEvaluationHelper fraudEvaluationHelper;
	private static final Logger log = LoggerFactory.getLogger(FraudStatusUpdateTask.class);
	private static final String DOMAIN = "inventory";
	private final FraudStatusUpdateRequest request;
	private final JmsTemplate fraudListingDeactivationMsgProducer;

	public FraudStatusUpdateTask(FraudStatusUpdateRequest updateRequest, FraudEvaluationHelper fraudEvaluationHelper, JmsTemplate fraudListingDeactivationMsgProducer) {
		this.request = updateRequest;
		this.fraudEvaluationHelper = fraudEvaluationHelper;
		this.fraudListingDeactivationMsgProducer = fraudListingDeactivationMsgProducer;
	}

	@Override
	public void run() {
		DyeUtil.beginDyedOperation("FraudStatusUpdateTask-" + request.getListingId());
		try {
			Long listingId = request.getListingId();
			Long fraudCheckStatusId = request.getFraudCheckStatusId();
			String fraudCheckStatus = request.getFraudCheckStatus();
			Long sellerId = request.getSellerId();
			Long fraudResolutionId = request.getFraudResolutionId();
			boolean isSellerDeactivated = request.isIsSellerDeactivated();
			log.info(
					"api_domain={}, api_method={}, message=\"Processing Listing update\", listingId={}, fraudCheckStatusId={}, fraudCheckStatus={}, sellerId={}, fraudResolutionId={}, isSellerDeactivated={} ",
					DOMAIN, "call", listingId, fraudCheckStatusId, fraudCheckStatus, sellerId, fraudResolutionId,
					isSellerDeactivated );
			fraudEvaluationHelper.processListingUpdate(request,fraudListingDeactivationMsgProducer);
			if((FraudCheckStatus.REJECTED.getId() == fraudCheckStatusId || FraudCheckStatus.ACCEPTED.getId() == fraudCheckStatusId)) {
				fraudEvaluationHelper.submitFraudListingEmailRequest(listingId.toString(), sellerId.toString(), fraudCheckStatusId);
			}else {
				log.info(
						"api_domain={}, api_method={}, message=\"Email submission is ignored \", listingId={}, fraudCheckStatusId={}, fraudCheckStatus={}, sellerId={}, fraudResolutionId={}, isSellerDeactivated={} ",
						DOMAIN, "call", listingId, fraudCheckStatusId, fraudCheckStatus, sellerId, fraudResolutionId,
						isSellerDeactivated );
			}

		} catch (Exception e) {
			log.error("api_domain={}, api_method={} message=\"Exception during updateCustomer\" exception=\"{}\"",
					DOMAIN, "call", ExceptionUtils.getFullStackTrace(e), e);
		} finally {
			DyeUtil.endDyedOperation();
		}

	}

}
