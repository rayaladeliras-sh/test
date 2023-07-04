package com.stubhub.domain.inventory.listings.v2.tns;

import java.util.Locale;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.apache.activemq.command.ActiveMQMapMessage;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.listings.v2.tns.util.FraudEvaluationHelper;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;

public class FraudListingEmailListenerTest {
	
	@Mock
	private FraudEvaluationHelper fraudEvaluationHelper;
	
	@InjectMocks
	private FraudListingEmailListener listener;
	
	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		listener = new FraudListingEmailListener();
		ReflectionTestUtils.setField(listener, "fraudEvaluationHelper", fraudEvaluationHelper);

//		Mockito.doNothing().when(fraudEvaluationHelper)
//		.processListingUpdate(Mockito.any(FraudStatusUpdateRequest.class));
//Mockito.when(fraudEvaluationHelper.submitFraudListingEmailRequest(Mockito.any(ListingResponse.class),
//		Mockito.anyLong())).thenReturn(Boolean.TRUE);
Mockito.doNothing().when(fraudEvaluationHelper).sendListingRejectSMS(Mockito.anyLong(), Mockito.anyLong());
Mockito.doNothing().when(fraudEvaluationHelper).sendListingAcceptEmail(Mockito.anyLong());

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
		message.setString("fraudCheckStatusId", "1000");
		listener.onMessage(message);
		message = getMessage();
		message.setString("fraudCheckStatusId", "200");
		listener.onMessage(message);
		message = getMessage();
		message.setString("fraudCheckStatusId", "502");
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
		Mockito.doThrow(Exception.class).when(fraudEvaluationHelper).sendListingAcceptEmail(Mockito.anyLong());
		message = getMessage();
		listener.onMessage(message);
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
