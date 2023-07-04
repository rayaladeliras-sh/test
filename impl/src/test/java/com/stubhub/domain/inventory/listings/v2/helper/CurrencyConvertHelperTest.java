package com.stubhub.domain.inventory.listings.v2.helper;

import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.Map;
import com.stubhub.domain.infrastructure.caching.client.core.L2Cache;
import com.stubhub.domain.infrastructure.caching.client.core.L2CacheManager;
import com.stubhub.domain.inventory.listings.v2.entity.CadCurrency;
import com.stubhub.domain.inventory.listings.v2.util.CurrencyConvertHelper;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class CurrencyConvertHelperTest {

    private CurrencyConvertHelper currencyConvertHelper;
    private SvcLocator svcLocator;
    private WebClient webClient;
    private ObjectMapper objectMapper;
    private L2CacheManager cacheManager;
    private L2Cache<Object> mockCache;


    @BeforeMethod
    public void setUp(){
        currencyConvertHelper = new CurrencyConvertHelper() {
            protected String getProperty(String propertyName, String defaultValue) {
                if ("currency.convert.api.url".equals(propertyName)) {
                    return "https://api-int.slcq063.com/fx/exchange/v1/daily?from=USD&to=CAD";
                }
                return "";
            }
        };
        svcLocator = Mockito.mock(SvcLocator.class);
        webClient  = Mockito.mock(WebClient.class);
        objectMapper  = Mockito.mock(ObjectMapper.class);
        cacheManager = Mockito.mock(L2CacheManager.class);
        mockCache = Mockito.mock(L2Cache.class);
        ReflectionTestUtils.setField(currencyConvertHelper, "svcLocator", svcLocator);
        ReflectionTestUtils.setField(currencyConvertHelper, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(currencyConvertHelper, "cacheManager", cacheManager);
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testGetCadCurrency() throws Exception{
        Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
        Mockito.when(webClient.get()).thenReturn(getCurrencyConvertResponse());
        CadCurrency cadCurrency = getCurrencyData();
        Mockito.when(cacheManager.getCache(Mockito.anyString())).thenReturn(mockCache);
        Mockito.when(mockCache.get(Mockito.anyString())).thenReturn(null);
        Mockito.when(objectMapper.readValue((InputStream) Mockito.anyObject(), Mockito.eq(CadCurrency.class))).thenReturn(cadCurrency);
        String cadCurrencyReturnValue = currencyConvertHelper.convertToCadCurrency("USD", "CAD");
        assertNotNull(cadCurrencyReturnValue);
        assertEquals(cadCurrency.getAmount(),new BigDecimal(cadCurrencyReturnValue));
    }

    private Response getCurrencyConvertResponse() {
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
                Date convertionDate = new Date();
                String response = "{\"id\": 540052,\"from\": \"USD\",\"to\": CAD,\"amount\": 1.3426,\"date\": convertionDate, \"convertionValue\": 1.34}";
                return new ByteArrayInputStream(response.getBytes());
            }
        };
        return response;
    }


    private CadCurrency getCurrencyData() {
        CadCurrency cadCurrency = new CadCurrency();
        List<CadCurrency> cadCurrencyList = new ArrayList<>();
        cadCurrency.setId(540052l);
        cadCurrency.setAmount(new BigDecimal("1.34").round(new MathContext(4, RoundingMode.HALF_UP)));
        return cadCurrency;
    }
}
