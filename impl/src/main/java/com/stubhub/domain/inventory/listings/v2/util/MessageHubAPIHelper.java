package com.stubhub.domain.inventory.listings.v2.util;

import javax.annotation.PostConstruct;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.StubHubSystemException;
import com.stubhub.domain.infrastructure.common.core.context.SHDyeContext;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.messagehub.client.DyeProvider;
import com.stubhub.platform.messagehub.client.MessageHubApiClient;
import com.stubhub.platform.messagehub.client.MessageHubApiContext;
import com.stubhub.platform.messagehub.client.MessageHubApiHttpClient;
import com.stubhub.platform.messagehub.client.MessageHubRequest;
import com.stubhub.platform.messagehub.client.MessageHubResponse;
import com.stubhub.platform.messagehub.client.exception.MessageHubException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

@Component
public class MessageHubAPIHelper {

    private MessageHubApiClient client;

    private static final Logger logger = LoggerFactory.getLogger(MessageHubAPIHelper.class);

    private int retry = 1;

    @Autowired
    MasterStubhubPropertiesWrapper masterStubhubProperties;


    @PostConstruct
    public void init() {
        DyeProvider dyeProvider = new DyeProvider() {
            @Override
            public String getDye() {
                return SHDyeContext.get().getDye();
            }
        };

        String baseUrl = masterStubhubProperties.getProperty("stubhub.message.gcp.domain", "https://api-int." + masterStubhubProperties.getProperty("internal.domainName") + ".com");
        int read_timeout = Integer.parseInt(masterStubhubProperties.getProperty("stubhub.message.gcp.domain.read.timeout", "30"));
        retry = Integer.parseInt(masterStubhubProperties.getProperty("stubhub.message.gcp.domain.retry", "2"));

        if (retry < 1) {
            retry = 1;
        }

        logger.info("Loading message v2 url at com.stubhub.domain.inventory.listings.v2.util.MessageHubAPIHelper, url = {}", baseUrl);
        MessageHubApiContext context = MessageHubApiContext.builder()
                .baseUrl(baseUrl)
                .authorization("Bearer " + masterStubhubProperties.getProperty("newapi.accessToken"))
                .timeout(read_timeout * 1000)
                .maxConnPerRoute(10)
                .maxConnTotal(30)
                .role("R2")
                .operatorId("sell")
                .build();
        logger.info("_message=\" MessageHub end point\" endpoint={}",context.getBaseUrl());
        logger.info("_message=\" MessageHub auth token \" endpoint={}",context.getAuthorization()); // for debug only. Needs to be removed later

        client = MessageHubApiHttpClient.create(context, dyeProvider);

        try {
            Field messageEndpointURLField = ReflectionUtils.findField(MessageHubApiHttpClient.class, "messageEndpointURL");
            messageEndpointURLField.setAccessible(true);
            logger.info("Client API endpoint={}", ReflectionUtils.getField(messageEndpointURLField, client));
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public void send(MessageHubRequest.MessageHubRequestBuilder request) {
        logger.info("_message=Sending Email using MessageHub API");
        MessageHubRequest req = request.build();
        logger.info("Request storeID={}", req.getShStoreId());
        logger.info("Request data={}", req.getData());
        String body = toJson(req);
        logger.info("\nRequest body = \n {} ", body);

        for (int i = 0; i < retry; i++) {
            try {
                MessageHubResponse response = client.send(req);
                logger.info(response.toString());
                break;
            } catch (MessageHubException e) {
                logger.error("_message=\"Error sending email using MessageHub API\"", e);
            }
        }
    }

    public <T> String toJson(T object) {
        ObjectMapper mapper = new ObjectMapper();

        String jsonInString = null;
        try {
            if (object != null) {
                jsonInString = mapper.writeValueAsString(object);
            }
        } catch (Exception e) {
            throw new StubHubSystemException(e);
        }
        return jsonInString;
    }
}
