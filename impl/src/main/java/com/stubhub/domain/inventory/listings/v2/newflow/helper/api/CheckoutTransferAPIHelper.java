package com.stubhub.domain.inventory.listings.v2.newflow.helper.api;

import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.infrastructure.common.exception.base.SHRuntimeException;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHResourceNotFoundException;
import com.stubhub.domain.inventory.listings.v2.util.CommonUtils;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class CheckoutTransferAPIHelper {

    private final static Logger log = LoggerFactory.getLogger(CheckoutTransferAPIHelper.class);
    private static final String CHECKOUT_TRANSFER_API_ENDPOINT = "checkout.transfer.v1.api.url";
    private static final String NEWAPI_ACCESS_TOKEN_KEY = "newapi.accessToken";

    @Autowired
    private Environment environment;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProxyHelper proxyHelper;

    private RestTemplate restTemplateWithEProxy;

    @PostConstruct
    public void init() {
        restTemplateWithEProxy = proxyHelper.createProxyRestTemplate();
    }

    public void transferOrderToFriend(Long orderId,
                                      List<Map<String, String>> orderItemToSeatMap, String toEmailId, String toCustomerGUID,
                                      String listingId, String sellerPaymentTypeId) throws Exception {
        TransferRequest transferRequest = TransferRequest.builder()
                .orderId(orderId)
                .toCustomerGUID(toCustomerGUID)
                .toEmailId(toEmailId)
                .listingId(listingId)
                .paymentType(sellerPaymentTypeId)
                .orderItemToSeatMap(orderItemToSeatMap)
                .build();
        String reqString = objectMapper.writeValueAsString(transferRequest);
        log.info("transfer request prepared:" + reqString);
        String transferApiUrl = getProperty(CHECKOUT_TRANSFER_API_ENDPOINT, null);
        if (transferApiUrl == null) {
            throw new SHRuntimeException("transferApiUrl not found");
        }
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        //Bearer {PROPERTY:newapi.accessToken}
        requestHeaders.add("Authorization", "Bearer "+getProperty(NEWAPI_ACCESS_TOKEN_KEY, null));

        HttpEntity<TransferRequest> entity = new HttpEntity<>(transferRequest, requestHeaders);
        String responseBody;
        Integer status;
        try {
            ResponseEntity<String> responseEntity = restTemplateWithEProxy.exchange(transferApiUrl, HttpMethod.POST, entity, String.class);
            status = responseEntity.getStatusCode().value();
            responseBody = responseEntity.getBody();
            log.info("_message=\"Cloud transfer order api response successful\" responseBody={}", responseBody);

        } catch (HttpStatusCodeException e) {
            responseBody = e.getResponseBodyAsString();
            status = e.getStatusCode().value();
            log.error("_message=\"Cloud transfer order api failed with error\" status={} responseBody={}", status, responseBody);
            throw e;
        }
    }

    protected String getProperty(String propertyName, String defaultValue) {
        return environment.getProperty(propertyName, defaultValue);
    }


}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TransferRequest {
    Long orderId;
    String listingId;
    List<Map<String, String>> orderItemToSeatMap;
    String toCustomerGUID;
    String toEmailId;
    String paymentType;
}