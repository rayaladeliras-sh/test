package com.stubhub.domain.inventory.listings.v2.bulk.listeners;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.stubhub.domain.inventory.biz.v2.intf.BulkUploadQueueMgr;
import com.stubhub.domain.inventory.datamodel.entity.BulkUploadQueue;

public class AutobulkFileReportingListenerTest {
	
	@Mock
	private JmsTemplate autobulkFileProcessingMsgProducer;
	
	@Mock
	private BulkUploadQueueMgr bulkUploadQueueMgr;
	
	@InjectMocks
	private AutobulkFileReportingListener autobulkFileReportingListener;
	
	private MapMessage messageMap;
	private static final long REQUEST_SECURITY_PASSED = 1200;
	private static final Long sellerId = 100000002L;

	@Before
	public void setup() throws JMSException {
		MockitoAnnotations.initMocks(this);

		this.messageMap = mock(MapMessage.class);
		when(messageMap.getString("queueId")).thenReturn("12345");
		when(messageMap.getString("sellerId")).thenReturn("100000002");
		when(messageMap.getString("email")).thenReturn("api_us_sell_indy03@testmail.com");
		when(messageMap.getString("userguid")).thenReturn("7988bd7a92febfcfb63f3d8da1d309d");
		when(messageMap.getString("totalNumOfRecords")).thenReturn("20");
		when(messageMap.getString("numOfInvalidRecords")).thenReturn("0");
		when(messageMap.getString("numOfListingFailures")).thenReturn("3");
		when(messageMap.getString("numOfUnchangedRecords")).thenReturn("0");
		when(messageMap.getString("numOfEventMappingFailures")).thenReturn("2");
		when(messageMap.getString("numOfListingsCreated")).thenReturn("12");
		when(messageMap.getString("numOfListingsUpdated")).thenReturn("2");
		when(messageMap.getString("numOfListingsDeleted")).thenReturn("0");
		when(messageMap.getString("fileStatusId")).thenReturn("2000");		
	}
	
	@Test
	public void testOnMessage_ReceiveValidMessage_NoPending_QueuesToReprocess() throws JMSException {
		when(bulkUploadQueueMgr.getQueueById(any(Long.class))).thenReturn(new BulkUploadQueue());
		when(bulkUploadQueueMgr.getQueuesByStatusAndSellerId(sellerId, REQUEST_SECURITY_PASSED)).thenReturn(null);
		doNothing().when(bulkUploadQueueMgr).updateBulkUploadQueue(any(BulkUploadQueue.class));
		autobulkFileReportingListener.onMessage(messageMap);
	}
	
	
	@Test
	public void testOnMessage_ReceiveValidMessage_Pending_QueuesToReprocess() throws JMSException {
		BulkUploadQueue bulkUploadQueue2 = new BulkUploadQueue();
		bulkUploadQueue2.setQueueId(123456L);
		bulkUploadQueue2.setQueueStatusId(REQUEST_SECURITY_PASSED);
		BulkUploadQueue bulkUploadQueue3 = new BulkUploadQueue();
		bulkUploadQueue3.setQueueId(1234567L);
		bulkUploadQueue3.setQueueStatusId(REQUEST_SECURITY_PASSED);
		List<BulkUploadQueue> pendingQueues = new ArrayList<BulkUploadQueue>();
		pendingQueues.add(bulkUploadQueue2);
		pendingQueues.add(bulkUploadQueue3);

		when(bulkUploadQueueMgr.getQueueById(any(Long.class))).thenReturn(new BulkUploadQueue());
		when(bulkUploadQueueMgr.getQueuesByStatusAndSellerId(sellerId, REQUEST_SECURITY_PASSED)).thenReturn(pendingQueues);
		doNothing().when(bulkUploadQueueMgr).updateBulkUploadQueue(any(BulkUploadQueue.class));
		doNothing().when(autobulkFileProcessingMsgProducer).send(any(MessageCreator.class));
		autobulkFileReportingListener.onMessage(messageMap);
	}
	
	@Test
	public void testOnMessage_ReceiveValidMessage_Pending_QueuesToReprocess_ThrowException() throws JMSException {
		BulkUploadQueue bulkUploadQueue2 = new BulkUploadQueue();
		bulkUploadQueue2.setQueueId(123456L);
		bulkUploadQueue2.setQueueStatusId(REQUEST_SECURITY_PASSED);
		BulkUploadQueue bulkUploadQueue3 = new BulkUploadQueue();
		bulkUploadQueue3.setQueueId(1234567L);
		bulkUploadQueue3.setQueueStatusId(REQUEST_SECURITY_PASSED);
		List<BulkUploadQueue> pendingQueues = new ArrayList<BulkUploadQueue>();
		pendingQueues.add(bulkUploadQueue2);
		pendingQueues.add(bulkUploadQueue3);

		when(bulkUploadQueueMgr.getQueueById(any(Long.class))).thenReturn(new BulkUploadQueue());
		when(bulkUploadQueueMgr.getQueuesByStatusAndSellerId(sellerId, REQUEST_SECURITY_PASSED)).thenReturn(pendingQueues);
		doThrow (Exception.class).when(bulkUploadQueueMgr).updateBulkUploadQueue(any(BulkUploadQueue.class));
		autobulkFileReportingListener.onMessage(messageMap);
	}
	

	@Test
	public void testOnMessage_ReceiveValidMessage_Pending_QueuesToReprocess_send_ThrowException() throws JMSException {
		BulkUploadQueue bulkUploadQueue2 = new BulkUploadQueue();
		bulkUploadQueue2.setQueueId(123456L);
		bulkUploadQueue2.setQueueStatusId(REQUEST_SECURITY_PASSED);
		BulkUploadQueue bulkUploadQueue3 = new BulkUploadQueue();
		bulkUploadQueue3.setQueueId(1234567L);
		bulkUploadQueue3.setQueueStatusId(REQUEST_SECURITY_PASSED);
		List<BulkUploadQueue> pendingQueues = new ArrayList<BulkUploadQueue>();
		pendingQueues.add(bulkUploadQueue2);
		pendingQueues.add(bulkUploadQueue3);

		when(bulkUploadQueueMgr.getQueueById(any(Long.class))).thenReturn(new BulkUploadQueue());
		when(bulkUploadQueueMgr.getQueuesByStatusAndSellerId(sellerId, REQUEST_SECURITY_PASSED)).thenReturn(pendingQueues);
		doNothing().when(bulkUploadQueueMgr).updateBulkUploadQueue(any(BulkUploadQueue.class));
		doThrow (Exception.class).when(autobulkFileProcessingMsgProducer).send(any(MessageCreator.class));
		autobulkFileReportingListener.onMessage(messageMap);
	}
	
	@Test
	public void testOnMessage_ReceiveValidMessage_Pending_QueuesToReprocess_send() throws JMSException {
		BulkUploadQueue bulkUploadQueue2 = new BulkUploadQueue();
		bulkUploadQueue2.setQueueId(123456L);
		bulkUploadQueue2.setQueueStatusId(REQUEST_SECURITY_PASSED);
		bulkUploadQueue2.setUserId(12345L);
		BulkUploadQueue bulkUploadQueue3 = new BulkUploadQueue();
		bulkUploadQueue3.setQueueId(1234567L);
		bulkUploadQueue3.setQueueStatusId(REQUEST_SECURITY_PASSED);
		bulkUploadQueue3.setUserId(12345L);
		List<BulkUploadQueue> pendingQueues = new ArrayList<BulkUploadQueue>();
		pendingQueues.add(bulkUploadQueue2);
		pendingQueues.add(bulkUploadQueue3);

		when(bulkUploadQueueMgr.getQueueById(any(Long.class))).thenReturn(new BulkUploadQueue());
		when(bulkUploadQueueMgr.getQueuesByStatusAndSellerId(sellerId, REQUEST_SECURITY_PASSED)).thenReturn(pendingQueues);
		doNothing().when(bulkUploadQueueMgr).updateBulkUploadQueue(any(BulkUploadQueue.class));
		
		final Session session = mock(Session.class);
		
		Mockito.doAnswer(new Answer<Message>() { 
	        @Override 
	        public Message answer(final InvocationOnMock invocation) throws JMSException { 
	           final Object[] args = invocation.getArguments(); 
	           final MessageCreator arg = (MessageCreator)args[0];
	           return arg.createMessage(session); 
	        } 
	    }).when(autobulkFileProcessingMsgProducer).send(any(MessageCreator.class));
		when(session.createMapMessage()).thenReturn(messageMap);
		
		autobulkFileReportingListener.onMessage(messageMap);
	}	
	
}
