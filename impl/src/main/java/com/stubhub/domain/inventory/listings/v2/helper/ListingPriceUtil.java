package com.stubhub.domain.inventory.listings.v2.helper;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.common.util.JsonUtil;
import com.stubhub.domain.inventory.listings.v2.util.CommonUtils;
import com.stubhub.domain.pricing.intf.aip.v1.request.PriceRequestList;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponseList;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

@Component("listingPriceUtil")
public class ListingPriceUtil {
  
    @Autowired
    private SvcLocator svcLocator;

    private static final Log log = LogFactory.getLog(ListingPriceUtil.class);

    /**
     * Calls new SHAPE-API and get pricing information for listing(s)
     *
     * @param requestList is a PriceRequestList
     * @return PriceResponseList prices response list
     */
    public PriceResponseList getListingPricingsAIP(SHAPIContext apiContext, PriceRequestList requestList) throws Exception {
        PriceResponseList responses = null;
        String requestUrl = null;
        try {
            requestUrl = getProperty("pricing.v1.price.api.url", "http://api-int.stubprod.com/pricing/aip/v1/price");
            log.info("Making getListingPricingsAIP service call = " + requestUrl);
            log.debug("PriceRequestList = " + requestList.toString());
            SHAPIThreadLocal.set(apiContext);
            WebClient webClient = svcLocator.locate(requestUrl);
            webClient.accept(MediaType.APPLICATION_JSON);

            SHMonitor mon = SHMonitorFactory.getMonitor();
            Response response = null;
            try {
                mon.start();
                response = webClient.post(requestList);
            } finally {
                mon.stop();
                log.info(SHMonitoringContext.get() + " _operation=getListingPricingsAIP" + " _message= service call" + "  _respTime=" + mon.getTime());
            }

            if (Response.Status.OK.getStatusCode() == response.getStatus()) {
                String responseJson = CommonUtils.streamToString((InputStream) response.getEntity());
                responses = (PriceResponseList) JsonUtil.toObjectWrapRoot(responseJson, PriceResponseList.class);
            } else {
                throw new IOException("Error encountred calling API. Response code: " + response.getStatus());
            }
        } catch (Exception e) {
            log.error("Error making remote API call to pricing API URL: " + requestUrl, e);
            throw e;
        }
        return responses;
    }

    public String getProperty(String key, String defaultVal) {
        return MasterStubHubProperties.getProperty(key, defaultVal);
    }

}
