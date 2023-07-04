package com.stubhub.domain.inventory.listings.v2.newflow.helper.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Slf4j
@Component
public class ProxyHelper {

    @Value("${stubhub.eproxy.host}")
    private String eProxyHost;

    @Value("${stubhub.eproxy.port}")
    private int eProxyPort;

    private final static int TIME_OUT_VALUE = 15_000;

// this not works sometimes
//    public RestTemplate createProxyRestTemplate2() {
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(eProxyHost, eProxyPort));
//        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
//        requestFactory.setProxy(proxy);
//        RestTemplate restTemplate = new RestTemplate(requestFactory);
//        return restTemplate;
//    }

    public RestTemplate createProxyRestTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        requestFactory.setConnectTimeout(TIME_OUT_VALUE);
        requestFactory.setReadTimeout(TIME_OUT_VALUE);

        requestFactory.setHttpClient(getCloseableHttpClient());

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;
    }

    private CloseableHttpClient getCloseableHttpClient() {
        RequestConfig config = RequestConfig
                .custom()
                .setConnectionRequestTimeout(TIME_OUT_VALUE)
                .setConnectTimeout(TIME_OUT_VALUE)
                .setSocketTimeout(TIME_OUT_VALUE)
                .build();

        HttpClientBuilder httpClientBuilder = HttpClients
                .custom()
                .setDefaultRequestConfig(config)
                .disableCookieManagement()
                .disableRedirectHandling();

        httpClientBuilder.setRoutePlanner(new DefaultProxyRoutePlanner(new HttpHost(eProxyHost, eProxyPort, "http")));
        httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);

        return httpClientBuilder.build();
    }

}