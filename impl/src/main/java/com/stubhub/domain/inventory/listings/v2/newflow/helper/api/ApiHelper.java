package com.stubhub.domain.inventory.listings.v2.newflow.helper.api;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.ResponseReader;
import org.apache.cxf.jaxrs.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;

import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

public abstract class ApiHelper {

    @Autowired
    protected SvcLocator svcLocator;

    // GET
    protected Response get(String url) {
        WebClient webClient = svcLocator.locate(url);
        webClient.accept(MediaType.APPLICATION_JSON);
        Response response = webClient.get();
        return response;
    }

    // GET with Response Headers
    /*protected Response get(String url, List<ResponseReader> responseReaders) {
        WebClient webClient = svcLocator.locate(url, responseReaders);
        webClient.accept(MediaType.APPLICATION_JSON);
        Response response = webClient.get();
        return response;
    }*/

    // GET with Response and Request Headers
   /* protected Response get(String url, List<ResponseReader> responseReader, Map requestHeaders) {
        WebClient webClient = svcLocator.locate(url, responseReader);
        webClient.accept(MediaType.APPLICATION_JSON);
        Set<Map.Entry<String, String>> entrySet = requestHeaders.entrySet();
        for (Map.Entry entry : entrySet) {
            webClient.header((String)entry.getKey(), (String)entry.getValue());
        }
        Response response = webClient.get();
        return response;
    }*/

   /* protected Response get(String url, Map requestHeaders) {
        WebClient webClient = svcLocator.locate(url);
        webClient.accept(MediaType.APPLICATION_JSON);
        Set<Map.Entry<String, String>> entrySet = requestHeaders.entrySet();
        for (Map.Entry entry : entrySet) {
            webClient.header((String)entry.getKey(), (String)entry.getValue());
        }
        Response response = webClient.get();
        return response;
    }*/

    /*protected Response get(String url, Locale locale) {
        WebClient webClient = svcLocator.locate(url);
        webClient.accept(MediaType.APPLICATION_JSON);
        webClient.acceptLanguage(getWellFormedLocaleString(locale));
        Response response = webClient.get();
        return response;
    }*/

   /* protected Response get(String url, Map requestHeaders, Locale locale) {
        WebClient webClient = svcLocator.locate(url);
        webClient.accept(MediaType.APPLICATION_JSON);
        Set<Map.Entry<String, String>> entrySet = requestHeaders.entrySet();
        for (Map.Entry entry : entrySet) {
            webClient.header((String)entry.getKey(), (String)entry.getValue());
        }
        webClient.acceptLanguage(getWellFormedLocaleString(locale));
        Response response = webClient.get();
        return response;
    }*/

   /* protected Response get(String url, String name, Object... values) {
        WebClient webClient = svcLocator.locate(url);
        webClient.accept(MediaType.APPLICATION_JSON);
        Response response = webClient.get();
        return response;
    }*/

    // POST
    protected Response post(String url, Object body) {
        WebClient webClient = svcLocator.locate(url);
        webClient.accept(MediaType.APPLICATION_JSON);
        Response response = webClient.post(body);
        return response;
    }

    // Properties
    protected String getProperty(String propertyName, String defaultValue) {
        return MasterStubHubProperties.getProperty(propertyName, defaultValue);
    }
    
   /*protected int getPropertyAsInt(String propertyName, int defaultValue) {
      return MasterStubHubProperties.getPropertyAsInt(propertyName, defaultValue);
    }*/

    /*private static String getWellFormedLocaleString(Locale locale) {
        return locale.getLanguage() + "-" + locale.getCountry();
    }*/
    
}
