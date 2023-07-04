package com.stubhub.test.inventory;

import java.util.Map;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

import com.stubhub.test.platform.Run;
import com.stubhub.test.platform.Setter;

public class ActiveMQTrigger implements Setter {

	private Run run = null;
	ActiveMQConnectionFactory factory = null;
	Connection connection = null;
	Session session = null;
	Destination dest = null;
	MessageProducer producer = null;
	public Boolean TRANSACTIONAL = false;
	public String QUEUE_NAME ;
	public static final String STATUS_NOTIFICATION_QUEUE = "com.stubhub.inventory.status.notification.queue";
	public static final String BUSINESS_QUEUE="domain.user.update.listing.business.queue";

	private void init(String env) throws JMSException {
		this.run.trace(String.format("tcp://%smqm001.%s.com:61616", env, env));
		factory = new ActiveMQConnectionFactory(String.format("tcp://%smqm001.%s.com:61616", env, env));
		this.run.trace(factory.getBrokerURL());
		connection = factory.createConnection();
		connection.start();
		this.run.trace(connection.getClientID());
		session = connection.createSession(TRANSACTIONAL, Session.AUTO_ACKNOWLEDGE);
		dest = new ActiveMQQueue(QUEUE_NAME);
		producer = session.createProducer(dest);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	}

	public void publishToQueue(String env, String listingId, String ticketSeatId) throws JMSException {
		this.run.trace(env + "  " + listingId + "  " + ticketSeatId);
		try {
			QUEUE_NAME=STATUS_NOTIFICATION_QUEUE;
			init(env);
		} catch (Exception e) {
			this.run.trace("Cause : "+e.getCause());
			this.run.trace("Error : "+e.getMessage());
			e.printStackTrace();
		}

		MapMessage mapMsg = session.createMapMessage();
		mapMsg.setLong("listingId", Long.parseLong(listingId));
		mapMsg.setLong("ticketSeatId", Long.parseLong(ticketSeatId));
		producer.send(mapMsg);

	}

	public void publishToBusinessQueue(String env,String userId,String action)throws JMSException{

		this.run.trace(env + "  " + userId + "  " + action);
		try {
			QUEUE_NAME=BUSINESS_QUEUE;
			init(env);
		} catch (Exception e) {
			this.run.trace("Cause : "+e.getCause());
			this.run.trace("Error : "+e.getMessage());
			e.printStackTrace();
		}

		MapMessage mapMsg = session.createMapMessage();
		mapMsg.setLong("userId", Long.parseLong(userId));
		mapMsg.setString("action", action);
		producer.send(mapMsg);
	}
	
	public void publishToQueue(String env,String queueName,  Map<String,String> paramMap) throws JMSException {
		this.run.trace("Queue Submission Started. ENV:"+env + "  queueName:" +queueName+" paramMap:" + paramMap );
		try {
			QUEUE_NAME=queueName;
			init(env);
			MapMessage mapMsg = session.createMapMessage();
			for(String key: paramMap.keySet()) {
				this.run.trace(key+" : "+String.valueOf(paramMap.get(key)));
				mapMsg.setString(key, String.valueOf(paramMap.get(key)));
			}
			producer.send(mapMsg);
			this.run.trace("Queue Submission Done. ENV:"+env + "  queueName:" +queueName+" paramMap:" + paramMap );

		} catch (Exception e) {
			this.run.trace("Cause : "+e.getCause());
			this.run.trace("Error : "+e.getMessage());
			e.printStackTrace();
		}

	}

	@Override
	public void setRun(Run arg0) {
		this.run = arg0;

	}

}
