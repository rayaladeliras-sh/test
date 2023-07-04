package com.stubhub.domain.inventory.listings.v2.tns;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.apache.activemq.command.ActiveMQMapMessage;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.listings.v2.tns.dto.FraudStatusUpdateRequest;
import com.stubhub.domain.inventory.listings.v2.tns.util.FraudEvaluationHelper;

public class FraudEvaluationListingUpdateListenerTest {

	@Mock
	private FraudEvaluationHelper fraudEvaluationHelper;

	@InjectMocks
	private FraudEvaluationListingUpdateListener listener;
	
	@Mock
	private JmsTemplate fraudListingDeactivationMsgProducer;

	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		listener = new FraudEvaluationListingUpdateListener();
		ReflectionTestUtils.setField(listener, "fraudEvaluationHelper", fraudEvaluationHelper);
		ReflectionTestUtils.setField(listener, "fraudListingDeactivationMsgProducer", fraudListingDeactivationMsgProducer);
		listener.init();
		Mockito.doNothing().when(fraudEvaluationHelper)
				.processListingUpdate(Mockito.any(FraudStatusUpdateRequest.class),Mockito.any(JmsTemplate.class));
		Mockito.when(fraudEvaluationHelper.submitFraudListingEmailRequest(Mockito.anyString(),Mockito.anyString(),
				Mockito.anyLong())).thenReturn(Boolean.TRUE);
	}

	@Test
	public void testOnMessage() throws JMSException {
		MapMessage message = getMessage();
		listener.onMessage(message);
		message = getMessage();
		message.setString("fraudCheckStatus", "");
		listener.onMessage(message);
		message = getMessage();
		message.setString("listingId", "abcd");
		listener.onMessage(message);
		message = getMessage();
		message.setString("fraudCheckStatusId", "abcd");
		listener.onMessage(message);
		message = getMessage();
		message.setString("sellerId", "abcd");
		listener.onMessage(message);
		message = getMessage();
		message.setString("userDeactivationReasonId", "abcd");
		listener.onMessage(message);
		message = getMessage();
		message.setString("fraudResolutionId", "abcd");
		listener.onMessage(message);
		Mockito.when(fraudEvaluationHelper.submitFraudListingEmailRequest(Mockito.anyString(),Mockito.anyString(),
				Mockito.anyLong())).thenThrow(Exception.class);
		message = getMessage();
		listener.onMessage(message);
		listener.destroy();
	}

	private MapMessage getMessage() {
		ActiveMQMapMessage message = new ActiveMQMapMessage();
		try {
			message.setString("listingId", "1234");
			message.setString("fraudCheckStatusId", "500");
			message.setString("fraudCheckStatus", "Accepted");
			message.setString("sellerId", "4321");
			message.setString("fraudResolutionId", "35");
			message.setString("isSellerDeactivated", "false");
			message.setString("userDeactivationReasonId", "200");
		} catch (Exception e) {

		}
		return message;
	}

}
