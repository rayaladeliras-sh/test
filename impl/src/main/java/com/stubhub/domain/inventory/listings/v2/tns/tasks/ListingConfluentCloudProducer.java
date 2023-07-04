package com.stubhub.domain.inventory.listings.v2.tns.tasks;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Component
public class ListingConfluentCloudProducer {

    @Value("${tns.fraud.prevention.broker-list:kfk001.stubcloudprod.com:9092,kfk002.stubcloudprod.com:9092," +
            "kfk003.stubcloudprod.com:9092,kfk004.stubcloudprod.com:9092,kfk005.stubcloudprod.com:9092}")
    private String brokerList;

    @Value("${tns.fraud.prevention.sasl.jaas.config}")
    private String saslConfig;


    private KafkaProducer<String, String> producer;

    @Value("${tns.fraud.prevention.listing.eval.producer.topic:payment.fraud.listing.to.eval}")
    private String topic;

    @Value("${ConfluentCloudProducer.enabled:true}")
    private Boolean enable;

    private static final Logger log = LoggerFactory.getLogger(ListingConfluentCloudProducer.class);
    private static final String LOG_STATEMENT = "api_domain=listing producer=ListingConfluentCloudProducer message=\"{}\" ";


    @PostConstruct
    public void init() {
        if (!enable) {
            log.warn("disable ListingConfluentProducer");
            return;
        }

        log.info("Start initiating ListingConfluentProducer");
        Properties props = new Properties();
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "PLAIN");
        props.put("ssl.endpoint.identification.algorithm", "https");
        props.put("bootstrap.servers", brokerList);
        props.put("sasl.jaas.config", saslConfig);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        props.put("client-id", "tns-listing-fraud-eval");
        producer = new KafkaProducer<>(props);
        log.info("Success initiating ListingConfluentProducer");

    }


    public void send(String messageValue) {
        send(null, messageValue);
    }

    public void send(final String messageKey, final String messageValue) {

        if (!enable) {
            log.warn("skip send, as ListingConfluentProducer is disabled");
            return;
        }

        log.info(LOG_STATEMENT.concat("messageKey={} messageValue={}"), "ListingConfluentProducer send message start", messageKey, messageValue);
        try {
            producer.send(new ProducerRecord<>(topic, messageKey, messageValue), new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception != null) {
                        log.error(LOG_STATEMENT.concat("RecordMetadata={}"), "Exception occurred while sending Confluent Kafka message", metadata, exception);
                    } else {
                        log.info(LOG_STATEMENT.concat("messageKey={} messageValue={} RecordMetadata={}"), "ListingConfluentProducer send message success", messageKey, messageValue, metadata);
                    }
                }
            });
        } catch (Exception e) {
            log.error(LOG_STATEMENT, "Exception occurred while sending Confluent Kafka message", e);
        }
    }

}
