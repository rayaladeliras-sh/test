package com.stubhub.domain.inventory.listings.v2.seller;

import com.stubhub.domain.inventory.listings.v2.listeners.TicketsPaymentTypeUpdateListener;
import com.stubhub.domain.inventory.listings.v2.tns.util.FraudEvaluationHelper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SellerSpiConvertTrackingConsumer {
  
  @Autowired
  @Qualifier("ticketPaymentTypeUpdateListener")
  private TicketsPaymentTypeUpdateListener ticketsPaymentTypeUpdateListener;
  
  @Value("${seller.confluent.cloud.kafka.broker:kfk001.stubcloudprod.com:9092,kfk002.stubcloudprod.com:9092," +
          "kfk003.stubcloudprod.com:9092,kfk004.stubcloudprod.com:9092,kfk005.stubcloudprod.com:9092}")
  private String brokerList;
  @Value("${seller.confluent.cloud.kafka.sasl.jaas.config}")
  private String saslConfig;
  String topic = "queue.sellerpayment.listing.updatePaymentType.name";

  @Value("${ConfluentCloudConsumer.enabled:true}")
  private Boolean enable;

  ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
  KafkaConsumer<String, String> consumer;
  ObjectMapper objectMapper = new ObjectMapper();
  private static final Logger log = LoggerFactory.getLogger(SellerSpiConvertTrackingConsumer.class);
  
  @PostConstruct
  public void init() {
    Properties props = new Properties();
    props.put("security.protocol", "SASL_SSL");
    props.put("sasl.mechanism", "PLAIN");
    props.put("ssl.endpoint.identification.algorithm", "https");
    props.put("bootstrap.servers", brokerList);
    props.put("sasl.jaas.config", saslConfig);
    props.put("key.deserializer", StringDeserializer.class.getName());
    props.put("value.deserializer", StringDeserializer.class.getName());
    props.put("group.id", "seller-spi-convert-tracking-listing");
    props.put("client.id", "seller-spi-convert-tracking-listing");
  
    consumer = new KafkaConsumer<String, String>(props);
    consumer.subscribe(Collections.singletonList(topic));
    log.info("Successfully initialize seller consumer, {}, {}", brokerList, saslConfig);

    if(enable) {
      try {
        consumer();
      } catch (org.apache.kafka.common.errors.SslAuthenticationException sslAuthenticationException) {
        log.error("sslAuthenticationException, stop consume", sslAuthenticationException);
        return;
      }

      executorService.scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
          try {
            consumer();
          } catch (Exception e) {
            log.error("some error happened when consumer data", e);
            //then ignore exception
          }
        }
      }, 5000, 5000, TimeUnit.MILLISECONDS);
    }

  }

  public void consumer() {

        ConsumerRecords<String, String> records = consumer.poll(5000);
        for (ConsumerRecord<String, String> record : records) {
          String value = record.value();
          log.info("Seller listing listener consumed record={}", value);
          SpiConvertTrackingMessage spiConvertTrackingMessage = null;
          try {
            spiConvertTrackingMessage = objectMapper.readValue(value, SpiConvertTrackingMessage.class);
            log.info("Seller listing listener successfully convert kafka message={}", spiConvertTrackingMessage);
            ticketsPaymentTypeUpdateListener.taskAfterKafkaMessage(spiConvertTrackingMessage);
            log.info("Seller listing task finished");
          } catch (IOException e) {
            log.error("Transform to json failed");
          }
        }

  }
}
