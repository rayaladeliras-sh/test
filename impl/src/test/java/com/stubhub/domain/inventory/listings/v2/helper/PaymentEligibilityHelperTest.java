package com.stubhub.domain.inventory.listings.v2.helper;

import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;
import junit.framework.Assert;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryang1 on 8/22/16.
 */
public class PaymentEligibilityHelperTest extends SHInventoryTest {
    private PaymentEligibilityHelper paymentEligibilityHelper;
    private SvcLocator svcLocator;
    private WebClient webClient;
    private HttpHeaders headers;

    @BeforeMethod
    public void setUp(){
        paymentEligibilityHelper = new PaymentEligibilityHelper() {
            protected String getProperty(String propertyName, String defaultValue) {
                if ("paymentEligibilityStatusService.internal.api.url".equals(propertyName))
                    return "http://api-int.stubhub.com/i18n/paymenteligibility/v1";
                return "";
            }
        };
        svcLocator = Mockito.mock(SvcLocator.class);
        webClient = Mockito.mock(WebClient.class);
        ReflectionTestUtils.setField(paymentEligibilityHelper, "svcLocator", svcLocator);

        headers = (HttpHeaders) mockClass ( HttpHeaders.class, null, null);
        List<String> values = new ArrayList<String>();
        values.add("en-gb");
        Mockito.when(headers.getRequestHeader("Accept-Language")).thenReturn(values);
        MultivaluedMap<String, String> headersMap =  new MetadataMap<String, String>();
        headersMap.add(HttpHeaders.USER_AGENT, "userAgent");
        Mockito.when(headers.getRequestHeaders()).thenReturn(headersMap);
    }

    @Test
    public void testIsValidPaymentEligibility() throws Exception {
        Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
        Mockito.when(webClient.get()).thenReturn(getResponse());
        paymentEligibilityHelper.isValidPaymentEligibility("ABCDEF", headers);
    }

    @Test
    public void testIsValidPaymentEligibilityError() throws Exception {
        Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
        Mockito.when(webClient.get()).thenReturn(getResponseError());
        try {
            paymentEligibilityHelper.isValidPaymentEligibility("ABCDEF", headers);
        } catch (ListingBusinessException e) {
            Assert.assertEquals(ErrorCode.SYSTEM_ERROR, e.getListingError().getCode());
        }
    }

    @Test
    public void testIsValidPaymentEligibilityException() throws Exception {
        Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
        Mockito.when(webClient.get()).thenReturn(getResponseException());
        try {
            paymentEligibilityHelper.isValidPaymentEligibility("ABCDEF", headers);
        } catch (ListingBusinessException e) {
            Assert.assertEquals(ErrorCode.SYSTEM_ERROR, e.getListingError().getCode());
        }
    }

    private Response getResponse() {
        Response response =  new Response() {

            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public MultivaluedMap<String, Object> getMetadata() {
                return null;
            }

            @Override
            public Object getEntity() {
                String responseString = "{\"allowPaymentAboveThreshold\": false, \"allowPaymentBelowThreshold\": false, \"usePaymentVerifyRuleFlag\": \"VALIDATE_ALL_REQUIRED\", \"createdDate\": \"2016-07-28T06:54:24+0000\"}";
                return new ByteArrayInputStream(responseString.getBytes());
            }
        };
        return response;
    }

    private Response getResponseException() {
        Response response =  new Response() {

            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public MultivaluedMap<String, Object> getMetadata() {
                return null;
            }

            @Override
            public Object getEntity() {
                String responseString = "{\"allowPaymentAboveThreshold\": \"YES\", \"allowPaymentBelowThreshold\": []}";
                return new ByteArrayInputStream(responseString.getBytes());
            }
        };
        return response;
    }

    private Response getResponseError() {
        Response response =  new Response() {

            @Override
            public int getStatus() {
                return 500;
            }

            @Override
            public MultivaluedMap<String, Object> getMetadata() {
                return null;
            }

            @Override
            public Object getEntity() {
                return null;
            }
        };
        return response;
    }
}
