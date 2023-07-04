package com.stubhub.domain.inventory.listings.v2.util;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Locale;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.stubhub.domain.inventory.listings.v2.util.ResourceManager;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

public class ResourceManagerTest 
{
	private ResourceManager resourceManager ;
	private SvcLocator svcLocator ;
	private WebClient webClient;
	
	@BeforeMethod
	public void setUp () throws Exception {
		
		resourceManager = Mockito.mock(ResourceManager.class);
		
		svcLocator = Mockito.mock(SvcLocator.class);
		webClient = Mockito.mock(WebClient.class);
		
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn (webClient);
		
		Mockito.when(webClient.get()).thenReturn(getResponse(200));
		
		Mockito.when(resourceManager.getResource(Mockito.anyString(), Mockito.any(Locale.class))).thenCallRealMethod();
		
		when ( resourceManager.getProperty(Mockito.anyString(), 
				Mockito.anyString())).thenReturn ("http://someUrl");
		when ( resourceManager.getSvcLocator() ).thenReturn(svcLocator);
	}
	
	@AfterMethod 
	public void tearDown () throws Exception {
		Mockito.reset ( resourceManager );
		//SELLAPI-1092 07/08/15
		resourceManager.resetInstance ();	
	}
		
	@Test
	public void resourceManagerTest ()
	{
		Mockito.when(webClient.get()).thenReturn(getResponse(200));
		
		// Supported locale
		String res1 = resourceManager.getResource(ResourceManager.regex_rowseat_specialChars, Locale.GERMANY);
		String res2 = resourceManager.getResource(ResourceManager.regex_section_specialChars, Locale.GERMANY);
		String res3 = resourceManager.getResource(ResourceManager.regex_section_startsWith, Locale.GERMANY);
		
		Assert.assertTrue(res1!=null && res2!=null && res3!=null);

		// Default locale is US
		String us_res1 = resourceManager.getResource(ResourceManager.regex_rowseat_specialChars, Locale.US);
		String us_res2 = resourceManager.getResource(ResourceManager.regex_section_specialChars, Locale.US);
		String us_res3 = resourceManager.getResource(ResourceManager.regex_section_startsWith, Locale.US);
		
		Assert.assertTrue(us_res1!=null && us_res2!=null && us_res3!=null);
		
		// un-supported locale should return default
		String def_res1 = resourceManager.getResource(ResourceManager.regex_rowseat_specialChars, Locale.ITALY);
		String def_res2 = resourceManager.getResource(ResourceManager.regex_section_specialChars, Locale.ITALY);
		String def_res3 = resourceManager.getResource(ResourceManager.regex_section_startsWith, Locale.ITALY);
		
		Assert.assertTrue (def_res1!=null && def_res2!=null && def_res3!=null);
	}	
	
	@Test
	public void getResourcesOkDeepTest ()
	{
		Mockito.when(webClient.get()).thenReturn(getResponse(200));
		
		Mockito.when(resourceManager.getResource(Mockito.anyString(), Mockito.any(Locale.class))).thenCallRealMethod();
		
		String res1 = resourceManager.getResource(ResourceManager.regex_rowseat_specialChars, Locale.GERMANY);
		Assert.assertTrue(res1.equals("[regex-rowseat-chars]") );
		
		String res2 = resourceManager.getResource(ResourceManager.regex_section_specialChars, Locale.US);
		Assert.assertTrue(res2.equals("[regex-section-chars]") );
	}
	
	@Test
	public void getResourcesErrorDeepTest ()
	{
		Mockito.when(webClient.get()).thenReturn(getResponse(400));
		
		Mockito.when(resourceManager.getResource(Mockito.anyString(), Mockito.any(Locale.class))).thenCallRealMethod();
		
		String res1 = resourceManager.getResource(ResourceManager.regex_rowseat_specialChars, Locale.GERMANY);
		Assert.assertFalse(res1.equals("[regex-rowseat-chars]") );
		
		String res2 = resourceManager.getResource(ResourceManager.regex_rowseat_specialChars, Locale.US);
		Assert.assertFalse(res1.equals("[regex-rowseat-chars]") );
	}
	
	
	private int respStatus = 0;
	
	private Response getResponse( int status) {
		respStatus = status;
		Response response =  new Response() {
		
			@Override
			public int getStatus() {
				return respStatus;
			}
			
			@Override
			public MultivaluedMap<String, Object> getMetadata() {				
				return null;
			}
			
			@Override
			public Object getEntity() {
				String resp = "<resourceGroup><groupName>inventory</groupName><locale>en_US</locale><resources><key>regex_rowseat_specialChars</key>"+
					"<version>1</version><values><value>[regex-rowseat-chars]</value><EffectiveDate><StartDate>2014-10-01</StartDate>"+
					"</EffectiveDate></values></resources><resources><key>regex_section_startsWith</key>"+
					"<version>1</version><values><value>[regex-startswith-chars]</value><EffectiveDate><StartDate>2014-10-01</StartDate>"+
					"</EffectiveDate></values></resources><resources><key>regex_section_specialChars</key><version>1</version>"+
					"<values><value>[regex-section-chars]</value><EffectiveDate><StartDate>2014-10-01</StartDate>"+
					"</EffectiveDate></values></resources></resourceGroup>";
				return new ByteArrayInputStream ( resp.getBytes() );
			}
		};
		return response;
	}	
}

