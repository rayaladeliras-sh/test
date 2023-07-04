package com.stubhub.domain.inventory.listings.v2.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.HttpHeaders;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CommonUtilsTest 
{
	@Test
	public void testCommonUtils () throws Exception
	{
		String localeStr = CommonUtils.getWellFormedLocaleString(Locale.US);
		Assert.assertEquals(localeStr, "en-US");  
		
		String someText = "Create and Update Listing V2 and V1 use the localization services (in limited form) documented at: i18n/localization1 " + 
				"Create and Update Listing V2 and V1 use the localization services (in limited form) documented at: i18n/localization2 " + 
				"Create and Update Listing V2 and V1 use the localization services (in limited form) documented at: i18n/localization3";
		
		InputStream is = new ByteArrayInputStream(someText.getBytes("UTF-8") );
		String result = CommonUtils.streamToString( is );  
		
		Assert.assertEquals(result, someText );
	}
	
	@Test
	public void testGetLocale ()
	{
		Locale locale = CommonUtils.localeFromLangString( "us-US" );
		Assert.assertEquals(locale, Locale.US);
		
		locale = CommonUtils.localeFromLangString( "us_US" );
		Assert.assertEquals(locale, Locale.US);
		
		locale = CommonUtils.localeFromLangString( "de-DE" );
		Assert.assertEquals(locale, Locale.GERMANY);

		locale = CommonUtils.localeFromLangString( "de_DE" );
		Assert.assertEquals(locale, Locale.GERMANY);
		
		// set default for bad locale
		locale = CommonUtils.localeFromLangString( "baa-BAD-LOCALE" );
		Assert.assertEquals(locale, Locale.US);
		
		// set default for null locale
		locale = CommonUtils.localeFromLangString( null );
		Assert.assertEquals(locale, Locale.US);
	}
	
	@Test
	public void testLocaleFromHeaderGood () 
	{	
		HttpHeaders headers = Mockito.mock(HttpHeaders.class);
		
		List<String> headerList = new ArrayList<String>();
		headerList.add("de-DE");
		Mockito.when(headers.getRequestHeader(Mockito.anyString())).thenReturn(headerList);
		
		Locale locale = CommonUtils.getClientLocale (headers);
		Assert.assertEquals(Locale.GERMANY, locale);
		
		headerList = new ArrayList<String>();
		headerList.add("");
		Mockito.when(headers.getRequestHeader(Mockito.anyString())).thenReturn(headerList);
		
		locale = CommonUtils.getClientLocale (headers);
		Assert.assertEquals(Locale.US, locale);
	}
	
	@Test
	public void testDelimitedStringToURI() {
		
		List<URI> uriList = CommonUtils.delimitedStringToURI("abc:1234 abcd:123456", " ", "http://", "/ping");
		Assert.assertNotNull(uriList);
		Assert.assertEquals(uriList.size(), 2);
		
		uriList = CommonUtils.delimitedStringToURI(" abc:1234 abcd:123456 ", " ", "http://", "/ping");
		Assert.assertNotNull(uriList);
		Assert.assertEquals(uriList.size(), 2);
		
		uriList = CommonUtils.delimitedStringToURI("abc abcd", " ", "http://", "/ping");
		Assert.assertNotNull(uriList);
		Assert.assertEquals(uriList.size(), 2);
		
		uriList = CommonUtils.delimitedStringToURI(null, " ", "http://", "/ping");
		Assert.assertNull(uriList);
		try {
			uriList = CommonUtils.delimitedStringToURI("abc:1234 abcd:123456", ":", "http://", "/ping");
			Assert.fail();
		}catch(Exception e) {
			
		}
	}
}


