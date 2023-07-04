package com.stubhub.domain.inventory.listings.v2.helper;

import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by ryang1 on 8/8/2016.
 */
@Component("paymentEligibilityHelper")
public class PaymentEligibilityHelper {

    private final static Logger LOGGER = LoggerFactory.getLogger(PaymentEligibilityHelper.class);

    @Autowired
    private SvcLocator svcLocator;

    public boolean isPIRequired(HttpHeaders httpHeaders) {
        boolean isPIRequired = false;
        String requirePILocales = getProperty("paymentEligibilityRequiredLocale", "en-gb,fr-fr,de-de");
        List<String> acceptLanguage = httpHeaders.getRequestHeader("Accept-Language");

        if (acceptLanguage != null && acceptLanguage.size() > 0) {
            isPIRequired = requirePILocales.contains(acceptLanguage.get(0).toLowerCase());
        }
        LOGGER.debug("PI Required:" + isPIRequired);
        return isPIRequired;
    }

    public boolean isValidPaymentEligibility(String sellerGUID, HttpHeaders httpHeaders) {

        if (isPIRequired(httpHeaders)) {
            PaymentEligibility paymentEligibility = getPaymentEligibility(sellerGUID);
            if (paymentEligibility == null
                    || (paymentEligibility.getAllowPaymentAboveThreshold() == null
                    && paymentEligibility.getAllowPaymentBelowThreshold() == null)) {
                LOGGER.info("_message=PI info is not collected");
                return false;
            }
        }

        return true;
    }

    public PaymentEligibility getPaymentEligibility(String sellerGuid) {
        PaymentEligibility paymentEligibility = null;
        try {
            String piEligibilityApiUrl = getProperty("paymentEligibilityStatusService.internal.api.url", "http://api-int.stubhub.com/i18n/paymenteligibility/v1?sellerGuid=");
            piEligibilityApiUrl = piEligibilityApiUrl + URLEncoder.encode(sellerGuid, "UTF-8").replace("+", "%20");

            WebClient webClient = svcLocator.locate(piEligibilityApiUrl);
            webClient.accept(MediaType.APPLICATION_JSON);
            webClient.type(MediaType.APPLICATION_JSON);

            LOGGER.info("_message=\"call paymenteligibility api\" url={}", piEligibilityApiUrl);

            SHMonitor mon = SHMonitorFactory.getMonitor();
            Response response = null;
            try {
                mon.start();
                response = webClient.get();
            } finally {
                mon.stop();
                LOGGER.info(SHMonitoringContext.get() + " _operation=getPaymentEligibility" + " _message= service call for sellerGuid=" + sellerGuid + "  _respTime=" + mon.getTime());
            }

            LOGGER.info("_message=\"call paymenteligibility api\" response.status={}", response.getStatus());
            if (Response.Status.OK.getStatusCode() == response.getStatus()) {
                InputStream responseStream = (InputStream) response.getEntity();
                byte[] data = new byte[1024];
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                while (true) {
                    int n = responseStream.read(data);
                    if (n == -1) break;
                    bos.write(data, 0, n);
                }
                String responseString = bos.toString();

                JSONObject json = new JSONObject(responseString);
                paymentEligibility = new PaymentEligibility();
                if (json.has("allowPaymentAboveThreshold")) {
                    paymentEligibility.setAllowPaymentAboveThreshold(
                            Boolean.valueOf(json.getBoolean("allowPaymentAboveThreshold")));
                }

                if (json.has("allowPaymentBelowThreshold")) {
                    paymentEligibility.setAllowPaymentBelowThreshold(
                            Boolean.valueOf(json.getBoolean("allowPaymentBelowThreshold")));
                }
            } else {
                LOGGER.error(
                        "error_message=\"BulkListing - call paymenteligibility api failed: status code " + response.getStatus() + "\"");
                InputStream is = (InputStream) response.getEntity();
                if (is != null) {
                    is.close();
                }
            }
        } catch (Exception e) {
            LOGGER.error(
                    "error_message=\"BulkListing - call paymenteligibility api failed: internal exception " + e.getLocalizedMessage() + "\"");
        }

        return paymentEligibility;
    }

    /**
     * Returns property value for the given propertyName. This protected method has been created to get
     * around the static nature of the MasterStubHubProperties' methods for Unit tests. The test classes are expected to
     * override this method with custom implementation.
     *
     * @param propertyName
     * @param defaultValue
     * @return
     */
    protected String getProperty(String propertyName, String defaultValue) {
        return MasterStubHubProperties.getProperty(propertyName, defaultValue);
    }
}
