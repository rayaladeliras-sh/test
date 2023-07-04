package com.stubhub.domain.inventory.listings.v2.impl;

import java.util.Calendar;
import java.util.Locale;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.stubhub.domain.infrastructure.communication.client.impl.MessageAPIClient;
import com.stubhub.domain.infrastructure.communication.client.impl.MessageRequestBuilder;
import com.stubhub.domain.infrastructure.communication.dto.MessageRequest;
import com.stubhub.domain.infrastructure.communication.dto.MessageResponse;
import com.stubhub.domain.infrastructure.communication.types.MessageModeType;
import com.stubhub.domain.user.services.customers.v2.intf.CustomerContactInfo;

/**
 * @author gopreddy
 *
 */

@Component
public class MessagePublishAPIHelper {

	protected MessageAPIClient messageAPIClient;

	@Value("${teb.order.placed.email.enable:true}")
	protected boolean newMessageAPIEnabled = false;

	@Value("${teb.order.placed.email.trial.run.enable:false}")
	protected boolean newMessageAPITrialEnabled = false;

	@Value("${authorization.token}")
	private String token;

	@Value("${infrastructure.communication.message.api.url}")
	private String url;

	@Autowired(required = false)
	@Qualifier("emailTextMessageSource")
	MessageSource messageSource;

	private static final Logger logger = LoggerFactory.getLogger(MessagePublishAPIHelper.class);

	private static final String SMS_OPERATOR_ID = "myx";
	private static final String SMS_ROLE_ID = "R2";

	@PostConstruct
	public void init() {
		messageAPIClient = new MessageAPIClient(this.token, this.url);
		logger.info("printing the values, token: {}, url: , token from bean: {}, url from bean: {}", token, url,
				messageAPIClient.getAppAuthorizationToken(), messageAPIClient.getServiceEndPoint());
		logger.info("Initializing MessagePublishAPIHelper with newMessageAPIEnabled = {}, newMessageAPITrialEnabled = {},done",
				newMessageAPIEnabled, newMessageAPITrialEnabled);
	}

	public void sendSMSWithMessageAPI(String templateName, CustomerContactInfo defaultContact, Locale locale, Long shStoreId,
			String userId) {
		MessageRequest messageRequest = new MessageRequestBuilder().addMessageMode(MessageModeType.SMS)
				.addLocale(locale).addMessageTimeStamp(Calendar.getInstance()).addMessageName(templateName)
				.addShStoreId(shStoreId==null?1L:shStoreId)
				.addUser(userId)
				.addOperatorId(SMS_OPERATOR_ID)
				.addProxiedId(userId)
				.addData("NAME", defaultContact.getName().getFirstName() + defaultContact.getName().getLastName())
				.addData("PHONE", defaultContact.getPhone())
				.addRole(SMS_ROLE_ID).buildRequest();
		logger.info("sendSMSWithMessageAPI request: {}", messageRequest);
		MessageResponse response = messageAPIClient.submit(messageRequest);
		logger.info("sendSMSWithMessageAPI response: {}", response);
	}

}
