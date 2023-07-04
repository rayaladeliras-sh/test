package com.stubhub.domain.inventory.listings.v2.tns.tasks;

import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.tns.dto.FraudStatusUpdateRequest;
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
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ListingConfluentCloudConsumer {

    @Value("${tns.fraud.prevention.broker-list:kfk001.stubcloudprod.com:9092,kfk002.stubcloudprod.com:9092," +
            "kfk003.stubcloudprod.com:9092,kfk004.stubcloudprod.com:9092,kfk005.stubcloudprod.com:9092}")
    private String brokerList;

    @Value("${tns.fraud.prevention.sasl.jaas.config}")
    private String saslConfig;

    private KafkaConsumer<String, String> consumer;

    @Value("${tns.fraud.prevention.listing.eval.result.consumer.topic:payment.fraud.listing.eval.result}")
    private String topic;

    @Value("${ConfluentCloudConsumer.enabled:true}")
    private Boolean enable;

    @Autowired
    private FraudEvaluationHelper fraudEvaluationHelper;
    @Autowired
    @Qualifier(value = "fraudListingDeactivationMsgProducer")
    private JmsTemplate fraudListingDeactivationMsgProducer;
    private final ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();

    private static final Logger log = LoggerFactory.getLogger(ListingConfluentCloudConsumer.class);
    private static final String LOG_STATEMENT = "api_domain=listing operation=onMessage listener=ListingConfluentCloudConsumer message=\"{}\" ";
    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        threadPool.setCorePoolSize(5);
        threadPool.setMaxPoolSize(10);
        threadPool.setWaitForTasksToCompleteOnShutdown(true);
        threadPool.initialize();

        log.info("Start initiating ListingConfluentCloudConsumer");
        Properties props = new Properties();
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "PLAIN");
        props.put("ssl.endpoint.identification.algorithm", "https");
        props.put("bootstrap.servers", brokerList);
        props.put("sasl.jaas.config", saslConfig);
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("group.id", "tns-listing-fraud-eval-result-t");
        props.put("client.id", "tns-listing-fraud-eval-result-t");
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
        log.info("Success initiating ListingConfluentCloudConsumer");

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
                        log.error(LOG_STATEMENT, "some error happened when consumer data", e);
                        //then ignore exception
                    }
                }
            }, 5000, 5000, TimeUnit.MILLISECONDS);
        }
    }

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private InventoryMgr inventoryMgr;

    public void consumer() {
        log.debug(LOG_STATEMENT, "ready to poll");

            ConsumerRecords<String, String> records = consumer.poll(5000);
            log.debug(LOG_STATEMENT.concat("records count={}"), "success receive records", records.count());
            for (ConsumerRecord<String, String> record : records) {
                log.info(LOG_STATEMENT.concat("consumerRecordKey={}  consumerRecordValue={}"), "Received message from  fraudevaluation on kafka channel", record.key(), record.value());

                String json = record.value();
                ListingEvalResponseMessage resp = null;
                try {
                    resp = objectMapper.readValue(json, ListingEvalResponseMessage.class);
                } catch (IOException e) {
                    log.error(LOG_STATEMENT, "Transform to json failed");
                    continue;
                }
                FraudStatusUpdateRequest request = new FraudStatusUpdateRequest();
                Long listingId = resp.getListingId();
                request.setListingId(listingId);
                AsyncListingFraudDecision fraudCheckStatus = resp.getFraudCheckStatus();
                if (AsyncListingFraudDecision.Accepted.equals(fraudCheckStatus)) {
                    request.setFraudCheckStatusId(500L);
                } else if (AsyncListingFraudDecision.Rejected.equals(fraudCheckStatus)) {
                    request.setFraudCheckStatusId(1000L);
                } else {
                    log.info(LOG_STATEMENT, "Fraud status is Unknown");
                    continue;
                }
                request.setFraudCheckStatus(fraudCheckStatus.name());

                Listing listing = inventoryMgr.getListing(listingId);
                log.info(LOG_STATEMENT.concat(", listingId={}, buyerId={}"), "Query Listing success", listingId, listing.getSellerId());
                Long sellerId = listing.getSellerId();
                request.setSellerId(sellerId);


                FraudStatusUpdateTask task = new FraudStatusUpdateTask(request, fraudEvaluationHelper,
                        fraudListingDeactivationMsgProducer);
                threadPool.submit(task);
            }

    }

}
