package com.stubhub.domain.inventory.v2.listings.eventmapper;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.stubhub.domain.infrastructure.messaging.client.core.event.Event;
import com.stubhub.domain.infrastructure.messaging.client.core.event.EventBuilder;
import com.stubhub.domain.infrastructure.messaging.client.kafka.KafkaProducerContext;


@Component("kafkaProducer")
public class KafkaProducer {

  private KafkaProducerContext<?, ?> kafkaProducerContext;
  
  @Autowired
  private ApplicationContext appContext;

  private final static Logger LOG = LoggerFactory.getLogger(KafkaProducer.class);
  private final String TOPIC = "catalog_entity_alias_mappings";



  public void sendMessage(String message) {
    try {
    	if(kafkaProducerContext == null) {
    		kafkaProducerContext = appContext.getBean("kafkaProducerContext",KafkaProducerContext.class);
        }
      final Map<String, Object> headers = new HashMap<>();
      headers.put("topic", TOPIC);
      final Event<String> aliasEvent = EventBuilder.withPayload(message, headers);
      LOG.info("_message=\"Sending message to Kafka Queue\" for topic={} body={}", TOPIC, message);
      kafkaProducerContext.send(aliasEvent);
    } catch (Exception e) {
    	LOG.error("_message=\"Exception occured while sending message to Kafka Queue\" for topic={} body={}", TOPIC, message, e);
    }
  }

}
