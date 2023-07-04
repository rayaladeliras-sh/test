package com.stubhub.domain.inventory.listings.v2.tns;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.jms.JMSException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.util.ReflectionTestUtils;

import com.stubhub.domain.infrastructure.config.client.core.management.SHConfigMBean;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.listings.v2.helper.ListingHelper;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;

public class FraudEvaluationServiceTest {

	@Mock
	private JmsTemplate fraudEvaluationMsgProducer;
	
	@Mock
	 private SHConfigMBean shConfig;
	
	@Mock
    private ListingHelper listingHelper;

	@InjectMocks
	FraudEvaluationService fraudEvaluationService;
	
	@Before
	public void setup() throws JMSException {
		MockitoAnnotations.initMocks(this);
		fraudEvaluationService = new FraudEvaluationService();
		Mockito.doNothing().when(fraudEvaluationMsgProducer).send(Mockito.any(MessageCreator.class));
		ReflectionTestUtils.setField(fraudEvaluationService, "fraudEvaluationMsgProducer", fraudEvaluationMsgProducer);
		ReflectionTestUtils.setField(fraudEvaluationService, "listingHelper", listingHelper);
		Mockito.when(shConfig.getValue(Mockito.anyString())).thenReturn("true");
		Mockito.when(listingHelper.populateListingDetails(Mockito.anyString(), Mockito.anyLong(), Mockito.any(Locale.class), Mockito.anyLong(), Mockito.any(SHServiceContext.class),Mockito.anyString())).thenReturn(getListing());
		
	}

	@Test
	public void testSubmitToQueueExcption() {
		ListingResponse listing = getListing();
		fraudEvaluationService.submitToQueue(listing.getId(),listing.getEventId(), Long.toString(listing.getSellerId()),listing.getStatus());
	}

	@Test
	public void testSubmitToQueue() {
		fraudEvaluationService.init();
		ListingResponse listing = getListing();
		fraudEvaluationService.submitToQueue(listing.getId(),listing.getEventId(), Long.toString(listing.getSellerId()),listing.getStatus());
		fraudEvaluationService.destroy();
	}

	@Test
	public void testSubmitToQueueTaskExcption() {
		fraudEvaluationService.init();
		Mockito.doThrow(Exception.class).when(fraudEvaluationMsgProducer).send(Mockito.any(MessageCreator.class));
		ListingResponse listing = getListing();
		fraudEvaluationService.submitToQueue(listing.getId(),listing.getEventId(), Long.toString(listing.getSellerId()),listing.getStatus());
		}
	
	@Test
	public void testSubmitToQueuePaused() {
		ListingResponse listing = getListing();
		Mockito.when(shConfig.getValue(Mockito.anyString())).thenReturn("false");
		fraudEvaluationService.submitToQueue(listing.getId(),listing.getEventId(), Long.toString(listing.getSellerId()),listing.getStatus());
	}
	
	private ListingResponse getListing() {
		ListingResponse response = new ListingResponse();
		response.setId("12345");
		response.setSellerId(12345l);
		response.setEventId("123455");
		response.setStatus(ListingStatus.ACTIVE);
		return response;
	}

}
