package com.stubhub.domain.inventory.listings.v2.helper;

import java.io.ByteArrayInputStream;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import junit.framework.Assert;

import org.apache.cxf.jaxrs.client.WebClient;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.util.SolrJsonUtil;
import com.stubhub.domain.inventory.listings.v2.entity.ListingCheck;
import com.stubhub.domain.inventory.listings.v2.util.InventorySolrUtil;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

public class InventorySolrUtilTest {

  private SvcLocator svcLocator;
  private WebClient webClient;
  private InventorySolrUtil inventorySolrUtil;
  private SolrJsonUtil solrJsonUtil;

  @BeforeTest
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    svcLocator = Mockito.mock(SvcLocator.class);
    webClient = Mockito.mock(WebClient.class);
    inventorySolrUtil = new InventorySolrUtil() {
      protected String getProperty(String propertyName, String defaultValue) {
        return "";
      }
    };
    solrJsonUtil = new SolrJsonUtil() {
      protected String getProperty(String propertyName, String defaultValue) {
        return "";
      }
    };
    ReflectionTestUtils.setField(solrJsonUtil, "svcLocator", svcLocator);
    ReflectionTestUtils.setField(inventorySolrUtil, "jsonUtil", solrJsonUtil);
  }

  @Test
  public void testIsListingExist() {

    Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
    Mockito.when(webClient.post(Mockito.anyObject())).thenReturn(getSolrResponse());

    ListingCheck listingCheck =
        inventorySolrUtil.isListingExists(23456L, 12345L, "3AD", "121, 122", "1,16", null);
    Assert.assertFalse(listingCheck.getIsListed());

    listingCheck = inventorySolrUtil.isListingExists(23456L, 12345L, "3AD", "11,12", "1,16", null);
    Assert.assertTrue(listingCheck.getIsListed());
    
    listingCheck = inventorySolrUtil.isListingExists(23456L, 12345L, "3AD", "11,12", "1,16", 12345L);
    Assert.assertFalse(listingCheck.getIsListed());
    
    listingCheck = inventorySolrUtil.isListingExists(23456L, 12345L, "3AD", "11,12", "1,16", 12L);
    Assert.assertTrue(listingCheck.getIsListed());

  }
  
  @Test
  public void testIsListingExistNoSeats() {
    Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
    Mockito.when(webClient.post(Mockito.anyObject())).thenReturn(getSolrResponseNoSeats());
    ListingCheck listingCheck = inventorySolrUtil.isListingExists(23456L, 12345L, "3AD", "11,12", "1,16", 12L);
    Assert.assertTrue(listingCheck.getIsListed());
  }
  
  @Test
  public void testGetProperty(){
    
    class InventorySolrUtil1 extends InventorySolrUtil{
      @Override
      public String getProperty(String propertyName, String defaultValue) {
        return super.getProperty(propertyName, defaultValue);
      }
    };
    InventorySolrUtil1 isu = new InventorySolrUtil1() ;
    try{
      isu.getProperty("test", "testValue");
      Assert.fail("should fail accessing the MasterStubhubProperties");
    }catch(Exception e){
      Assert.assertTrue(true);
    }
    
  }

  @Test
  public void testIsListingExistThrowException() throws Exception {
    Response response = new Response() {

      @Override
      public int getStatus() {
        return 404;
      }

      @Override
      public MultivaluedMap<String, Object> getMetadata() {
        return null;
      }

      @Override
      public Object getEntity() {
        String response = "{\"response\": {\"numFound\": 2,\"docs\": []}}";
        return new ByteArrayInputStream(response.getBytes());
      }
    };
    Mockito.when(webClient.post(Mockito.anyObject())).thenReturn(response);

    try {
      ListingCheck listingCheck =
          inventorySolrUtil.isListingExists(23456L, 12345L, "3AD", "121", "1,16", null);
      Assert.fail("An exception should have been thrown!");
    } catch (Throwable th) {
    }
  }

  private Response getSolrResponse() {
    Response response = new Response() {

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
        String response =
            "{\"response\": {\"numFound\": 2,\"docs\": [{\"id\": 12345, \"seats\": \"9,10\",\"rowDesc\": \"13, 14\""
                + "},{\"id\": 12345, \"seats\": \"1,16\", \"rowDesc\": \"11, 12\"}]}}";
        return new ByteArrayInputStream(response.getBytes());
      }
    };
    return response;
  }
  
  private Response getSolrResponseNoSeats() {
    Response response = new Response() {

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
        String response =
            "{\"response\": {\"numFound\": 2,\"docs\": [{\"id\": 12345, \"rowDesc\": \"13, 14\""
                + "},{\"id\": 12345, \"seats\": \"1,16\", \"rowDesc\": \"11, 12\"}]}}";
        return new ByteArrayInputStream(response.getBytes());
      }
    };
    return response;
  }
}
