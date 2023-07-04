package com.stubhub.domain.inventory.listings.v2.util;

import junit.framework.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.BusinessInfo;

public class BusinessHelperTest {

  @InjectMocks
  private BusinessHelper businessHelper;

  @Mock
  private RestTemplate restTemplate;

  private ResponseEntity<String> responseEntity;

  @BeforeMethod
  public void setUp () {
    MockitoAnnotations.initMocks(this);

    businessHelper = new BusinessHelper() {
      protected String getProperty(String propertyName, String defaultValue) {
        switch (propertyName) {
          case "stubhub.business.service.api.url":
            return "url";
          case "newapi.accessToken":
            return "token";
        }
        return "";
      }
    };

    ReflectionTestUtils.setField(businessHelper, "restTemplate", restTemplate);
  }

  @Test
  public void successGetBusinessInfo() {
    String companyName = "Stark Industries";
    Long businessId = 1234L;
    String businessGuid = "guid";

    responseEntity = buildResponseEntity(HttpStatus.OK, companyName, businessId, businessGuid);
    mockRestTemplate(responseEntity);

    BusinessInfo businessInfo = businessHelper.getBusinessInfo(1L);

    Assert.assertNotNull("businessInfo can not be null", businessInfo);
    Assert.assertEquals("companyName must be the one received", businessInfo.getCompanyName(), companyName);
    Assert.assertEquals("businessId must be the one received", businessInfo.getBusinessId(), businessId);
    Assert.assertEquals("businessGuid must be the one received", businessInfo.getBusinessGuid(), businessGuid);
  }

  @Test
  public void failGetBusinessInfo() {
    responseEntity = buildResponseEntity(HttpStatus.NOT_FOUND, null, null, null);
    mockRestTemplate(responseEntity);

    BusinessInfo businessInfo = businessHelper.getBusinessInfo(1L);

    Assert.assertNull(businessInfo);
  }

  private static ResponseEntity buildResponseEntity(HttpStatus status, String companyName, Long businessId, String businessGuid) {
    return new ResponseEntity<>("{\"companyName\": \"" + companyName
        + "\", \"businessId\": " + businessId
        + ", \"businessGuid\": \"" + businessGuid + "\"}", status);
  }

  private void mockRestTemplate(ResponseEntity responseEntity) {
    Mockito.when(restTemplate.exchange(
        Mockito.anyString(),
        Mockito.any(HttpMethod.class),
        Mockito.any(HttpEntity.class),
        Mockito.any(Class.class))
    ).thenReturn(responseEntity);
  }
}
