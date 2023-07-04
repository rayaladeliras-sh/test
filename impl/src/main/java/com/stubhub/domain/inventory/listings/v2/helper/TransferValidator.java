/**
 *
 */
package com.stubhub.domain.inventory.listings.v2.helper;

import java.io.InputStream;
import java.io.StringWriter;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

/**
 * @author sparida
 */
@Component("transferValidator")
public class TransferValidator {

    private static final String USER_COOKIE_GUID = "userCookieGuid";
    private static final String CUSTOMER = "customer";
    private static final String XSH_SERVICE_CONTEXT = "X-SH-Service-Context";
    private static final String XSH_SERVICE_CONTEXT_HEADER = "{role=R1}";

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(TransferValidator.class);

    protected final static String CUSTOMER_END_POINT = "customer.api.url";

    @Autowired
    private SvcLocator svcLocator;

    @Autowired
    private UserHelper userHelper;

    // @Autowired
    private ObjectMapper objectMapper = null;

    /**
     * helper method to call getCustomer api
     */
    public String getUserGuid(String emailId) {

        String customerV2ApiUrl = getProperty(CUSTOMER_END_POINT,
                "https://api.stubcloudprod.com/user/customers/v1/?action=checkEmail&emailAddress={emailId}");
        customerV2ApiUrl = customerV2ApiUrl.replace("{emailId}", emailId);
        log.info("_message=\"get user account information\" emailId={} userAccountInfoApiUrl={}", emailId,
                customerV2ApiUrl);
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        WebClient webClient = userHelper.getWebClientFromSveLocator(customerV2ApiUrl, null, true);
        webClient.header(XSH_SERVICE_CONTEXT, XSH_SERVICE_CONTEXT_HEADER);
        webClient.accept(MediaType.APPLICATION_JSON);
        ClientConfiguration config = webClient.getConfig(webClient);
        config.getInInterceptors().add(new LoggingInInterceptor());
        config.getOutInterceptors().add(new LoggingOutInterceptor());

        try {

            SHMonitor mon = SHMonitorFactory.getMonitor();
            Response response = null;
            try {
                mon.start();
                response = webClient.get();
            } finally {
                mon.stop();
                log.info(SHMonitoringContext.get() + " _operation=getUserGuid" + " _message= service call for emailId=" + emailId + "  _respTime=" + mon.getTime());
            }

            if (response.getStatus() == 200) {
                InputStream is = (InputStream) response.getEntity();
                StringWriter sw = new StringWriter();
                IOUtils.copy(is, sw);
                JSONObject jsonObject = new JSONObject(sw.toString());
                JSONObject customer = (JSONObject) jsonObject.get(CUSTOMER);
                String userGuid = customer.getString(USER_COOKIE_GUID);
                return userGuid;
            }

        } catch (Exception e) {
            log.error("System error occured while populating CustomerDetails of emailId=" + emailId, e);
        }
        return null;
    }

    protected String getProperty(String propertyName, String defaultValue) {
        return MasterStubHubProperties.getProperty(propertyName, defaultValue);
    }
}
