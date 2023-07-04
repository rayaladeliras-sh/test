package com.stubhub.domain.inventory.listings.v2.util;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.BusinessAddress;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.BusinessInfo;
import com.stubhub.domain.inventory.v2.DTO.BusinessResponse;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import java.util.Arrays;

@Component("businessHelper")
public class BusinessHelper {

  private static final String BUSINESS_SERVICE_API_URL = "stubhub.business.service.api.url";
  private static final String BUSINESS_SERVICE_API_URL_DEFAULT = "https://api.stubcloudprod.com/user/business/v1/user/";

  private static final String NEWAPI_ACCESS_TOKEN_KEY = "newapi.accessToken";
  private static final String ACCESS_TOKEN_DEFAULT_VALUE = "JYf0azPrf1RAvhUhpGZudVU9bBEa";

  private static final String DOMAIN = "inventory";

  private RestTemplate restTemplate = new RestTemplate();

  private static final Logger log = LoggerFactory.getLogger(BusinessHelper.class);

  private ObjectMapper objectMapper = new ObjectMapper();

  public BusinessInfo getBusinessInfo(Long sellerId) {
    String businessServiceApiUrl = getProperty(BUSINESS_SERVICE_API_URL, BUSINESS_SERVICE_API_URL_DEFAULT);
    businessServiceApiUrl += String.valueOf(sellerId);

    log.info("api_domain={} api_method={} url={}", DOMAIN, "getBusinessInfo", businessServiceApiUrl);

    SHMonitor mon = SHMonitorFactory.getMonitor();
    try {
      final HttpHeaders headers = new HttpHeaders();
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      headers.setContentType(MediaType.APPLICATION_JSON);
      final String appToken = getProperty(NEWAPI_ACCESS_TOKEN_KEY, ACCESS_TOKEN_DEFAULT_VALUE);
      headers.add("Authorization", "Bearer " + appToken);

      mon.start();

      ResponseEntity<String> responseEntity = restTemplate.exchange(businessServiceApiUrl, HttpMethod.GET, new HttpEntity<>("", headers), String.class);

      if (!HttpStatus.Series.SUCCESSFUL.equals(responseEntity.getStatusCode().series())) {
        log.error("api_domain={} api_method={} message=\"Business not found\" sellerId={} respCode={}",
            DOMAIN, "getBusinessInfo", sellerId, responseEntity.getStatusCode().value());
        return null;
      }
      String responseBody = responseEntity.getBody();

      BusinessResponse businessResponse = objectMapper.readValue(responseBody, BusinessResponse.class);

      log.info(SHMonitoringContext.get() + "api_domain={} api_method={} sellerId={} response={}",
          DOMAIN, "getBusinessInfo", sellerId, businessResponse);

      BusinessAddress address = null;
      if (businessResponse.getAddress() != null) {
        address = new BusinessAddress(
            businessResponse.getAddress().getAddress1(),
            businessResponse.getAddress().getAddress2(),
            businessResponse.getAddress().getCity(),
            businessResponse.getAddress().getState(),
            businessResponse.getAddress().getCountry(),
            businessResponse.getAddress().getPostcode()
        );
      }
      return new BusinessInfo(businessResponse.getCompanyName(), businessResponse.getBusinessId(), businessResponse.getBusinessGuid(), address);
    } catch (Exception ex) {
      log.error("api_domain={} api_method={} message=\"Unknown exception while making getBusinessInfo API call\" sellerId={} error=\"{}\"",
          DOMAIN, "getBusinessInfo", sellerId, ex.getMessage());
    } finally {
      mon.stop();
      log.info(SHMonitoringContext.get() + "api_domain={} api_method={} url={} sellerId={} time={}", DOMAIN, "getBusinessInfo",
          businessServiceApiUrl, sellerId, mon.getTime());
    }
    return null;
  }

  /**
   * Returns property value for the given propertyName. This protected method has been created to get around the static nature of the
   * MasterStubHubProperties' methods for Unit tests. The test classes are expected to override this method with custom implementation.
   *
   * @param propertyName
   * @param defaultValue
   * @return
   */
  protected String getProperty(String propertyName, String defaultValue) {
    return MasterStubHubProperties.getProperty(propertyName, defaultValue);
  }
}