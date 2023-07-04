package com.stubhub.domain.inventory.listings.v2.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class CommonUtils 
{
	
	private static final Logger log = LoggerFactory.getLogger(CommonUtils.class);

	/**
	 * set client ip in request
	 * @param listing
	 */
	public static void setClientIP (HttpHeaders httpHeaders, com.stubhub.domain.inventory.datamodel.entity.Listing listing) 
	{
		listing.setIpAddress(  getClientIP (httpHeaders) );
	}	
	
	/**
	 * set client ip in request
	 */
	public static String getClientIP (HttpHeaders httpHeaders) 
	{
		List<String> clientIp = httpHeaders.getRequestHeader("X-FORWARDED-FOR");
		if (clientIp != null && clientIp.size() > 0) {
			return clientIp.get(0);
		} 
		else {
			Message message = PhaseInterceptorChain.getCurrentMessage();
			if ( message != null ) {
				HttpServletRequest httpRequest = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
				return httpRequest.getRemoteAddr();
			}
		}
		return null;
	}
	
	/**
	 * getClientLocale from header
	 * @param httpHeaders
	 * @return
	 */
	public static Locale getClientLocale ( HttpHeaders httpHeaders) 
	{
		List<String> langs = httpHeaders.getRequestHeader ( "accept-language" );
		String language = null;
		
		if ( langs != null && langs.size() > 0 ) {
			language = langs.get(0);
		}
		return localeFromLangString ( language );
	}
	
	/**
	 * localeFromLangString
	 * @param acceptLanguage
	 * @return Locale
	 */
    public static Locale localeFromLangString(String pAcceptLanguage) 
    {
    	//SELLAPI-1135 sonar-rules, avoid reassigning to parameters.
    	String acceptLanguage = pAcceptLanguage;
        if (acceptLanguage == null || acceptLanguage.trim().isEmpty())
            return Locale.US;

        // Be forgiving with accepting '_' as locale lang_country seperator 
        acceptLanguage = acceptLanguage.replace('_', '-');
        
        Locale locale = null;
        acceptLanguage = acceptLanguage.split(",")[0].trim();
        String[] lcv = acceptLanguage.split("-");//according to rfc2616, 14.4 and 3.10 we expect like "en-US" or "en", etc
        if (lcv.length == 1)
            locale = new Locale(lcv[0]);
        else if (lcv.length == 2)
            locale = new Locale(lcv[0], lcv[1]);
        else if (lcv.length == 3)
            locale = new Locale(lcv[0], lcv[1], lcv[2]);
        else
            locale = Locale.US;

        if (!Arrays.asList(Locale.getAvailableLocales()).contains(locale))
            locale = Locale.US;

        return locale;
    }
    
	/**
	 * Efficient way to convert a string to String
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public static String streamToString ( InputStream is ) throws IOException
	{
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, "UTF-8");
		return writer.toString();
	}
	
	/**
	 * Gets the  well-formed IETF BCP 47 locale String
	 * @param locale
	 */
	public static String getWellFormedLocaleString ( Locale locale ) 
	{
		return locale.getLanguage() + "-" + locale.getCountry() ;
	}	
	
	public static List<URI> delimitedStringToURI(String delimittedString, String delimiter, String prefix, String suffix) {
		List<URI> uris = null;
		String[] delimittedArray = StringUtils.delimitedListToStringArray(delimittedString, delimiter);
		int arrayLength = delimittedArray.length;
		if(arrayLength>0) {
			uris = new LinkedList<URI>();
			for (int i = 0; i < arrayLength; ++i) {
				if(org.apache.commons.lang3.StringUtils.isNotBlank(delimittedArray[i])) {
					String host = delimittedArray[i];
					uris.add(URI.create(prefix + host + suffix));
				}
			}
		}
		log.info("Couch Base Hosts List: delimiter:{} delimittedString:{}",delimiter, delimittedString );

		log.info("Couch Base Hosts List: {}",uris);
		return uris;
	}
}
