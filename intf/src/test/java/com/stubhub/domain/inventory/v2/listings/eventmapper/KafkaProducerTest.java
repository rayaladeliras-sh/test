package com.stubhub.domain.inventory.v2.listings.eventmapper;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.infrastructure.messaging.client.core.event.Event;
import com.stubhub.domain.infrastructure.messaging.client.core.event.EventBuilder;
import com.stubhub.domain.infrastructure.messaging.client.kafka.KafkaProducerContext;

public class KafkaProducerTest {

  @InjectMocks
  private KafkaProducer kafkaProducer;

  @Mock
  private KafkaProducerContext<?, ?> kafkaProducerContext;

  @BeforeMethod
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testSendMessage() throws Exception {
    String message = "text";
    final Map<String, Object> headers = new HashMap<>();
    headers.put("topic", "catalog_alias_mappings");
    final Event<String> aliasEvent = EventBuilder.withPayload(message, headers);
    when(kafkaProducerContext.send(aliasEvent)).thenReturn(null);

    kafkaProducer.sendMessage(message);
  }

}
