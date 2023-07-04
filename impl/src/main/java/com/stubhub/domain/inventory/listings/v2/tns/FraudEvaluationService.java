package com.stubhub.domain.inventory.listings.v2.tns;

import com.stubhub.domain.infrastructure.config.client.core.management.SHConfigMBean;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.listings.v2.helper.ListingHelper;
import com.stubhub.domain.inventory.listings.v2.tns.tasks.FraudEvaluationQueueSubmitTask;
import com.stubhub.domain.inventory.listings.v2.tns.tasks.ListingConfluentCloudProducer;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.json.Json;

@Component
public class FraudEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(FraudEvaluationService.class);

    @Autowired
    @Qualifier(value = "fraudEvaluationMsgProducer")
    private JmsTemplate fraudEvaluationMsgProducer;

    @Autowired
    private ListingHelper listingHelper;

    @Autowired
    private SHConfigMBean shConfig;

    private static final ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();

    private static final String LISTING_SWITCH_NAME = "tns.listing.evaluation.new.flow.enabled";

    private static final int MAX_THREAD_COUNT = 20;
    private static final int CORE_THREAD_COUNT = 10;

    @Autowired
    private ListingConfluentCloudProducer listingConfluentCloudProducer;

    @PostConstruct
    public void init() {
        threadPool.setMaxPoolSize(MAX_THREAD_COUNT);
        threadPool.setCorePoolSize(CORE_THREAD_COUNT);
        threadPool.setWaitForTasksToCompleteOnShutdown(true);
        threadPool.initialize();
    }

    public void submitToQueue(String listingId, String eventId, String sellerId, ListingStatus listingStatus) {
        String status = "TNS_QUEUE_TASK_STARTED";
        log.info(
                "api_domain=\"inventory\" api_resource=\"listing\" api_method=\"createListing\" listingId=\"{}\" listingStatus=\"{}\" eventId=\"{}\" sellerId=\"{}\" message=\"Processing task for TNS Queue submission\"",
                listingId, listingStatus, eventId, sellerId);
        boolean isNewFlowEnabled = false;
        try {
            isNewFlowEnabled = Boolean.parseBoolean(shConfig.getValue(LISTING_SWITCH_NAME));
            if (isNewFlowEnabled) {
                FraudEvaluationQueueSubmitTask queSubmitTask = new FraudEvaluationQueueSubmitTask(listingId, sellerId,
                        eventId, listingStatus, fraudEvaluationMsgProducer, listingHelper);
                threadPool.submit(queSubmitTask);
                status = "TNS_QUEUE_TASK_SUBMITTED";
                log.info(
                        "api_domain=\"inventory\" api_resource=\"listing\" api_method=\"createListing\" listingId=\"{}\" listingStatus=\"{}\" eventId=\"{}\" sellerId=\"{}\" message=\"Task submitted successfuly for TNS Fraud Queue submission\"",
                        listingId, listingStatus, eventId, sellerId);
            } else {
                status = "TNS_QUEUE_TASK_SKIPPED";
                log.info(
                        "api_domain=\"inventory\" api_resource=\"listing\" api_method=\"createListing\" listingId=\"{}\" listingStatus=\"{}\" eventId=\"{}\" sellerId=\"{}\" isNewFlowEnabled=\"{}\" message=\"Skipping TNS Fraud Queue submission\"",
                        listingId, listingStatus, eventId, sellerId, isNewFlowEnabled);
            }
        } catch (Throwable e) {
            status = "TNS_QUEUE_TASK_FAILED";
            log.error(
                    "api_domain=\"inventory\" api_resource=\"listing\" api_method=\"createListing\" listingId=\"{}\" eventId=\"{}\" sellerId=\"{}\" listingStatus=\"{}\" isNewFlowEnabled=\"{}\" message=\"Error while submitting task to TNS Fraud Queue submission\" exception=\"{}\"",
                    listingId, listingStatus, eventId, sellerId, isNewFlowEnabled, ExceptionUtils.getFullStackTrace(e),
                    e);
        } finally {
            log.info(
                    "api_domain=\"inventory\" api_resource=\"listing\" api_method=\"createListing\" success={} listingId=\"{}\" listingStatus=\"{}\" eventId=\"{}\" sellerId=\"{}\" isNewFlowEnabled=\"{}\" message=\"Processing of task complete for TNS Fraud Queue submission\"",
                    status, listingId, listingStatus, eventId, sellerId, isNewFlowEnabled);
        }

    }

    public void submitToCloudConfluentKafka(final String listingId) {
        final String json = Json.createObjectBuilder().add("listingId", listingId).build().toString();
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                listingConfluentCloudProducer.send(listingId, json);
            }
        });
    }

    @PreDestroy
    public void destroy() {
        threadPool.shutdown();
    }

}
