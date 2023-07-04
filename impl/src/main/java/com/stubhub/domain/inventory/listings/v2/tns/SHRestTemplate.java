package com.stubhub.domain.inventory.listings.v2.tns;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public final class SHRestTemplate {

    public static final int SOCKET_TIME_OUT = 2000;
    public static final int CONN_TIMEOUT = 2000;

    private SHRestTemplate () {

    }

    public static RestTemplate createRestTemplate() {
        return createRestTemplate(SOCKET_TIME_OUT, CONN_TIMEOUT) ;
    }

    private static RestTemplate createRestTemplate(final int socketTimeOut, final int connTimeout) {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(3,false))
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(socketTimeOut)
                        .setSocketTimeout(connTimeout)
                        .build())
                .setMaxConnTotal(250)
                .setMaxConnPerRoute(25)
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }


    public static RestTemplate createRestTemplate(int timeout){
        return  createRestTemplate(timeout, timeout);
    }

}
