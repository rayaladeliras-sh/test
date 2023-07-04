package com.stubhub.domain.inventory.listings.v2.helper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.stubhub.domain.infrastructure.common.core.concurrent.SHThreadLocals;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.listings.v2.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.stubhub.domain.accountmanagement.services.orders.v3.intf.dto.OrderDetailsV3DTO;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHResourceNotFoundException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHUnauthorizedException;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;
import org.springframework.web.client.RestTemplate;

@Component("orderDetailsHelper")
public class OrderDetailsHelper {
    private static final String NEWAPI_ACCESS_TOKEN_KEY = "newapi.accessToken";
    private static final String ACCESS_TOCKEN_DEFAULT_VALUE = "JYf0azPrf1RAvhUhpGZudVU9bBEa";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String XSH_SERVICE_CONTEXT = "X-SH-Service-Context";

    private final SHServiceContext DEFAULT_CONTEXT = new SHServiceContext() {
        @Override
        public String getProxiedId() {
            return "DUMMY";
        }
        @Override
        public String getOperatorId() {
            return "InventoryV2";
        }
        @Override
        public String getOperatorRole() {
            return "R1";
        }
    };

    private static final Logger log = LoggerFactory.getLogger(OrderDetailsHelper.class);

    @Autowired
    private ObjectMapper objectMapper;

    private RestTemplate restTemplate = new RestTemplate();

    public OrderDetailsV3DTO getOrderDetails(Long orderId) {
        OrderDetailsV3DTO orderDetails = null;
        String orderDetailsV3Url = getProperty("orderdetails.v3.api.url", "https://api-int.stubprod.com/accountmanagement/orderdetails/v3/{orderId}");
        orderDetailsV3Url = orderDetailsV3Url.replace("{orderId}", orderId.toString());
        log.info("_message=\"get order details\" orderId={} orderDetailsV3Url={}", orderId, orderDetailsV3Url);

        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {

            SHMonitor mon = SHMonitorFactory.getMonitor();
            int status = 0;
            String responseBody = null;
            try {
                HttpHeaders requestHeaders = new HttpHeaders();
                requestHeaders.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                String accessToken = getProperty(NEWAPI_ACCESS_TOKEN_KEY, ACCESS_TOCKEN_DEFAULT_VALUE);
                requestHeaders.set(AUTHORIZATION, BEARER + accessToken);
                SHServiceContext securityContext = (SHServiceContext) SHThreadLocals.get(SHServiceContext.SERVICE_CONTEXT_HEADER);

                if (securityContext == null) {
                    securityContext = DEFAULT_CONTEXT;
                }

                requestHeaders.set(XSH_SERVICE_CONTEXT, "{role="
                        + StringUtils.defaultIfEmpty(securityContext.getOperatorRole(), DEFAULT_CONTEXT.getOperatorRole())
                        + ", operatorId="
                        + StringUtils.defaultIfEmpty(securityContext.getOperatorId(), DEFAULT_CONTEXT.getOperatorId())
                        + ", proxiedId="
                        + StringUtils.defaultIfEmpty(StringUtils.defaultIfEmpty(securityContext.getProxiedId(), securityContext.getUserGuid()), DEFAULT_CONTEXT.getProxiedId())
                        + "}");

                HttpEntity<String> entity = new HttpEntity<>("", requestHeaders);
                mon.start();
                ResponseEntity<String> responseEntity = restTemplate.exchange(orderDetailsV3Url, HttpMethod.GET, entity, String.class);
                status = responseEntity.getStatusCode().value();
                responseBody = responseEntity.getBody();
            } finally {
                mon.stop();
                log.info(SHMonitoringContext.get() + " _operation=getOrderDetails" + " _message= service call for orderId=" + orderId + "  _respTime=" + mon.getTime());
            }

            log.info("_message=\"get order details v3 call\" orderId={} status={}", orderId, status);
            if (Response.Status.OK.getStatusCode() == status) {
                log.info("_message=\"get order details v3 call successful for\" orderId={}", orderId);
                orderDetails = objectMapper.readValue(responseBody, OrderDetailsV3DTO.class);
            } else if (Response.Status.NOT_FOUND.getStatusCode() == status) {
                log.error("_message=\"Invalid Order\" orderID={}", orderId);
                SHResourceNotFoundException srnfe = new SHResourceNotFoundException();
                srnfe.setErrorCode("inventory.listings.orderNotFound");
                srnfe.setDescription("No Order exists in the system for the provided orderId");
                Map<String, String> data = new HashMap<String, String>();
                data.put("orderId", orderId.toString());
                throw srnfe;
            } else if (Response.Status.UNAUTHORIZED.getStatusCode() == status) {
                log.error("_message=\"Unauthorized access to the Order\" orderID={}", orderId);
                SHUnauthorizedException sue = new SHUnauthorizedException();
                sue.setErrorCode("inventory.listings.forbidden");
                sue.setDescription("Not allowed to access this order details");
                Map<String, String> data = new HashMap<String, String>();
                data.put("orderId", orderId.toString());
                throw sue;
            } else {
                log.error("System error occured while calling getOrderDetails api  orderId=" + orderId + " responseCode=" + status);
                throw new SHSystemException("Unable to connect to order details resource end point, please re-try");
            }
        } catch (SHResourceNotFoundException srnfe) {
            throw srnfe;
        } catch (SHUnauthorizedException sue) {
            throw sue;
        } catch (SHSystemException sse) {
            throw sse;
        } catch (Exception e) {
            log.error("System error occured while processing getOrderDetails api response orderId=" + orderId, e);
            throw new SHSystemException("System error occured while calling orderdetails API");
        }
        if (orderDetails == null) {
            log.error("System error occured while calling orderdetails API orderId=" + orderId);
            throw new SHSystemException("System error occured while calling orderdetails API");
        }
        return orderDetails;
    }

    protected String getProperty(String propertyName, String defaultValue) {
        return MasterStubHubProperties.getProperty(propertyName, defaultValue);
    }

}
