/**
 * 
 */
package com.stubhub.domain.inventory.listings.v2.impl;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.domain.inventory.listings.v2.bulk.util.BulkListingHelper;
import com.stubhub.domain.inventory.listings.v2.bulk.util.BulkListingJobConsumer;
import com.stubhub.domain.inventory.listings.v2.bulk.util.BulkListingJobProducer;

/**
 * @author sjayaswal
 *
 */
public class BulkListingJobConsumerTest extends SHInventoryTest {

	private BulkListingJobConsumer bulkListingJobConsumer;

	private BulkListingJobProducer bulkListingJobProducer;
	private BulkListingHelper bulkListingHelper;
	
	@BeforeTest
	public void setUp() throws Exception 
	{
		MockitoAnnotations.initMocks(this);
		bulkListingJobConsumer = new BulkListingJobConsumer();

		bulkListingJobProducer=new BulkListingJobProducer();

		bulkListingHelper=Mockito.mock(BulkListingHelper.class);
		
		ReflectionTestUtils.setField(bulkListingJobConsumer, "bulkListingJobProducer", bulkListingJobProducer);
		ReflectionTestUtils.setField(bulkListingJobConsumer, "bulkListingHelper", bulkListingHelper);
			
	}
	
	@Test
	public void testRun(){
		Thread consumerThread = new Thread(bulkListingJobConsumer);
		consumerThread.start();
	}
	
	@Test
	public void testProcessGroups(){
		BulkListingJobConsumer bulkListingJobConsumer = new BulkListingJobConsumer();
		ReflectionTestUtils.setField(bulkListingJobConsumer, "bulkListingHelper", bulkListingHelper);
		MapMessage mMessage = new MapMessage() {
			@Override
			public void setStringProperty(String name, String value)
					throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setShortProperty(String name, short value) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setObjectProperty(String name, Object value)
					throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setLongProperty(String name, long value) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setJMSType(String type) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setJMSTimestamp(long timestamp) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setJMSReplyTo(Destination replyTo) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setJMSRedelivered(boolean redelivered) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setJMSPriority(int priority) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setJMSMessageID(String id) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setJMSExpiration(long expiration) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setJMSDestination(Destination destination) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setJMSDeliveryMode(int deliveryMode) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setJMSCorrelationIDAsBytes(byte[] correlationID)
					throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setJMSCorrelationID(String correlationID) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setIntProperty(String name, int value) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setFloatProperty(String name, float value) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setDoubleProperty(String name, double value)
					throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setByteProperty(String name, byte value) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setBooleanProperty(String name, boolean value)
					throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean propertyExists(String name) throws JMSException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public String getStringProperty(String name) throws JMSException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public short getShortProperty(String name) throws JMSException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public Enumeration getPropertyNames() throws JMSException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object getObjectProperty(String name) throws JMSException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public long getLongProperty(String name) throws JMSException {
				return 87167342l;
			}
			
			@Override
			public String getJMSType() throws JMSException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public long getJMSTimestamp() throws JMSException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public Destination getJMSReplyTo() throws JMSException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public boolean getJMSRedelivered() throws JMSException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public int getJMSPriority() throws JMSException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public String getJMSMessageID() throws JMSException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public long getJMSExpiration() throws JMSException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public Destination getJMSDestination() throws JMSException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getJMSDeliveryMode() throws JMSException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getJMSCorrelationID() throws JMSException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getIntProperty(String name) throws JMSException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public float getFloatProperty(String name) throws JMSException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public double getDoubleProperty(String name) throws JMSException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public byte getByteProperty(String name) throws JMSException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public boolean getBooleanProperty(String name) throws JMSException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void clearProperties() throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void clearBody() throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void acknowledge() throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setString(String name, String value) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setShort(String name, short value) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setObject(String name, Object value) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setLong(String name, long value) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setInt(String name, int value) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setFloat(String name, float value) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setDouble(String name, double value) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setChar(String name, char value) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setBytes(String name, byte[] value, int offset, int length)
					throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setBytes(String name, byte[] value) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setByte(String name, byte value) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setBoolean(String name, boolean value) throws JMSException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean itemExists(String name) throws JMSException {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public String getString(String name) throws JMSException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public short getShort(String name) throws JMSException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public Object getObject(String name) throws JMSException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Enumeration getMapNames() throws JMSException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public long getLong(String name) throws JMSException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int getInt(String name) throws JMSException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public float getFloat(String name) throws JMSException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public double getDouble(String name) throws JMSException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public char getChar(String name) throws JMSException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public byte[] getBytes(String name) throws JMSException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public byte getByte(String name) throws JMSException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public boolean getBoolean(String name) throws JMSException {
				// TODO Auto-generated method stub
				return false;
			}
		};
		Map<String , Long> message = new HashMap<String, Long>();
		message.put("groupId", 12321l);
		bulkListingJobConsumer.processGroup(message);
		
	}
	
}
