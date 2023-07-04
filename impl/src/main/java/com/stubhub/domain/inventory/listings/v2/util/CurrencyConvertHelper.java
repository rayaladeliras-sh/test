package com.stubhub.domain.inventory.listings.v2.util;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.stubhub.domain.infrastructure.caching.client.core.L2Cache;
import com.stubhub.domain.infrastructure.caching.client.core.L2CacheManager;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.listings.v2.entity.CadCurrency;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

@Component("currencyConvertHelper")
public class CurrencyConvertHelper {

    private final static Logger log = LoggerFactory.getLogger(CurrencyConvertHelper.class);
    private static final String CURRENCY_CONVERT_API_URL = "currency.convert.api.url";
    private static final String CURRENCY_CONVERT_API_URL_DEFAULT = "https://api-int.stubprod.com/fx/exchange/v1/daily?fromCurrency={fromCurrency}&toCurrency={toCurrency}";

    private static final String USD_TO_CAD_CACHE = "usdToCadCache";

    @Autowired
    private SvcLocator svcLocator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("L2CacheManager")
    L2CacheManager cacheManager;
    String cadCurrencyValue = null;

	public String convertToCadCurrency(String fromCurrency, String toCurrency) {
		String cadCurrencyValue = null;
		try {
			L2Cache<String> cache = cacheManager.getCache(USD_TO_CAD_CACHE);
			cadCurrencyValue = cache.get("USDToCADValue");
			if (StringUtils.isBlank(cadCurrencyValue)) {
				CadCurrency cadCurrency = getCurrencyConversionValue(fromCurrency, toCurrency);
				if (cadCurrency != null && cadCurrency.getAmount() != null) {
					cadCurrencyValue = cadCurrency.getAmount().toString();
					cache.put("USDToCADValue", cadCurrencyValue);
					log.info(
							"_message=\"Getting currency coversion value and added to the Cache\", fromCurrency={}, toCurrency={}, cadCurrencyValue={}",
							fromCurrency, toCurrency, cadCurrencyValue);
				}
			}
		} catch (Exception e) {
			log.error("_message=\"Convert Currency conversion to CAD from cache failed\"", e);
		}
		return cadCurrencyValue;
	}

    private CadCurrency getCurrencyConversionValue(String fromCurrency, String toCurrency) {
        CadCurrency cadCurrency = null;

        String currencyConvertURL = getProperty(CURRENCY_CONVERT_API_URL, CURRENCY_CONVERT_API_URL_DEFAULT);
        currencyConvertURL = currencyConvertURL.replace("{fromCurrency}", fromCurrency.toString());
        currencyConvertURL = currencyConvertURL.replace("{toCurrency}", toCurrency.toString());

        log.info("_message=\"getCurrencyConversionValue information\" fromCurrency={} toCurrency={} currencyConversionUrl={}", fromCurrency, toCurrency, currencyConvertURL);

        try {
            objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            WebClient webClient = svcLocator.locate(currencyConvertURL);
            webClient.accept(MediaType.APPLICATION_JSON);

            SHMonitor mon = SHMonitorFactory.getMonitor();
            Response response = null;

            try {
                mon.start();
                response = webClient.get();
            } finally {
                mon.stop();
                log.info(SHMonitoringContext.get() + " _operation=getCadCurrency" + " _message= service call _respTime=" + mon.getTime());
            }

            if (Response.Status.OK.getStatusCode() == response.getStatus()) {
                InputStream is = (InputStream) response.getEntity();
                cadCurrency = objectMapper.readValue(is, CadCurrency.class);
            } else {
                log.error("_message=\"getCurrencyConversionValue api call failed\" responseCode={}", response.getStatus());
            }

        } catch (Exception e) {

        }
        if (cadCurrency != null) {
            return cadCurrency;
        }
        return null;
    }

    protected String getProperty(String propertyName, String defaultValue) {
        return MasterStubHubProperties.getProperty(propertyName, defaultValue);
    }
}
