package com.stubhub.domain.inventory.biz.v2.impl;

import java.io.ByteArrayInputStream;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.biz.v2.impl.util.InventoryServiceMonitor;

public class InventoryServiceMonitorTest {
	
	@Test
	public void testExecute(){
		InventoryServiceMonitor monitor = new InventoryServiceMonitor(){
			protected String getProperty(String propertyName, String defaultValue) {
				return defaultValue;
			}
			protected WebClient getWebClient(String uri){
				WebClient client = Mockito.mock(WebClient.class);
				Mockito.when(client.get()).thenReturn(getResponse());
				return client;
			}
		};
		monitor .execute();
		
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
				byte[] b = new byte[100];
				return new ByteArrayInputStream(b);
			}
		};
		return response;
	}

}
