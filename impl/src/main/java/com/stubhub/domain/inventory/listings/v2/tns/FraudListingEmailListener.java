package com.stubhub.domain.inventory.listings.v2.tns;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.stubhub.domain.infrastructure.common.exception.derived.SHValidationErrorException;
import com.stubhub.domain.inventory.listings.v2.tns.dto.FraudCheckStatus;
import com.stubhub.domain.inventory.listings.v2.tns.util.FraudEvaluationHelper;
import com.stubhub.newplatform.common.util.DyeUtil;

public class FraudListingEmailListener implements MessageListener {

	private static final Logger log = LoggerFactory.getLogger(FraudListingEmailListener.class);
	private static final String DOMAIN = "inventory";

	@Autowired
	private FraudEvaluationHelper fraudEvaluationHelper;

	@Override
	public void onMessage(Message message) {
		long startTime = System.currentTimeMillis();
		log.info("api_domain={}, api_method={}, message=\"Recieved message\" ", DOMAIN, "onMessage");
		String listingIdString = null;
		String fraudCheckStatusId = null;
		String sellerId = null;
		try {
			MapMessage mapMsg = (MapMessage) message;
			listingIdString = mapMsg.getString("listingId");
			DyeUtil.beginDyedOperation("FraudListingEmailListener-" + listingIdString);
			log.info("api_domain={}, api_method={}, message=\"Start processing message\" listingId={} ", DOMAIN,
					"onMessage", listingIdString);
			fraudCheckStatusId = mapMsg.getString("fraudCheckStatusId");
			sellerId = mapMsg.getString("sellerId");
			validate(listingIdString, fraudCheckStatusId, sellerId);
			log.info(
					"api_domain={}, api_method={}, message=\"Validation Success\" listingId={}, fraudCheckStatusId={},  sellerId={}",
					DOMAIN, "onMessage", listingIdString, fraudCheckStatusId, sellerId);
			Long listingId = Long.parseLong(listingIdString);
			if (FraudCheckStatus.REJECTED.getId() == Long.parseLong(fraudCheckStatusId)) {
				fraudEvaluationHelper.sendListingRejectSMS(listingId, Long.parseLong(sellerId));
			} else if (isFraudCheckStatus5XX(Long.parseLong(fraudCheckStatusId))) {
				fraudEvaluationHelper.sendListingAcceptEmail(listingId);
			} else {
				log.warn(
						"api_domain={}, api_method={}, message=\"Ignoring the message for unknown fraudCheckStatusId \" listingId={}, fraudCheckStatusId={},  sellerId={} ",
						DOMAIN, "onMessage", listingIdString, fraudCheckStatusId, sellerId);
			}
		} catch (SHValidationErrorException e) {
			log.error(
					"api_domain={}, api_method={}, message=\"Validation Exception while processing message\" listingId={}, fraudCheckStatusId={},  sellerId={} exception={}",
					DOMAIN, "onMessage", listingIdString, fraudCheckStatusId, sellerId,
					ExceptionUtils.getFullStackTrace(e), e);
		} catch (Exception e) {
			log.error("api_domain={}, api_method={}, message=\"Exception while processing message\" exception={}",
					DOMAIN, "onMessage", listingIdString, fraudCheckStatusId, sellerId,
					ExceptionUtils.getFullStackTrace(e), e);
		} finally {
			log.info(
					"api_domain={}, api_method={}, message=\"End processing message\" duration={} listingId={}, fraudCheckStatusId={},  sellerId={}",
					DOMAIN, "onMessage", (System.currentTimeMillis() - startTime), listingIdString, fraudCheckStatusId,
					sellerId);
			DyeUtil.endDyedOperation();
		}
	}

	private void validate(String listingId, String fraudCheckStatusId, String sellerId) {
		boolean validationFlag = true;
		if (!StringUtils.isNumeric(listingId)) {
			validationFlag = false;
			log.error(
					"api_domain={}, api_method={}, message=Invalid Listing ID, listingId={}, fraudCheckStatusId={},  sellerId={}, ",
					DOMAIN, "validate", listingId, fraudCheckStatusId, sellerId);
		}
		if (!StringUtils.isNumeric(fraudCheckStatusId)) {
			validationFlag = false;
			log.error(
					"api_domain={}, api_method={}, message=Invalid FraudCheckStatus ID, listingId={}, fraudCheckStatusId={},  sellerId={}, ",
					DOMAIN, "updateCustomer", listingId, fraudCheckStatusId, sellerId);
		}
		if (!StringUtils.isNumeric(sellerId)) {
			validationFlag = false;
			log.error(
					"api_domain={}, api_method={}, message=Invalid Seller ID, listingId={}, fraudCheckStatusId={},  sellerId={}, ",
					DOMAIN, "updateCustomer", listingId, fraudCheckStatusId, sellerId);
		}
		if (!validationFlag) {
			throw new SHValidationErrorException("Fraud Email Listener Validation Exception");
		}
	}
	
	private boolean isFraudCheckStatus5XX(long fraudCheckStatusId ) {
		return (fraudCheckStatusId >=FraudCheckStatus.ACCEPTED.getId() && fraudCheckStatusId <=599);
	}

}
