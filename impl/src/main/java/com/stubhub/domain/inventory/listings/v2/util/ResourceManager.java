package com.stubhub.domain.inventory.listings.v2.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.client.ClientWebApplicationException;
import org.apache.cxf.jaxrs.client.WebClient;
import org.springframework.stereotype.Component;

import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.Node;
import nu.xom.ParsingException;

//SELLAPI-1092 07/08/15 declared as component
@Component("resourceManager")
public class ResourceManager 
{
	private static final Log log = LogFactory.getLog(ResourceManager.class);
	
	// resource keys defined here
	public static final String regex_section_specialChars = "regex_section_specialChars";
	public static final String regex_section_startsWith   = "regex_section_startsWith";	
	public static final String regex_rowseat_specialChars = "regex_rowseat_specialChars";
	
	private static final Locale LOCALE_US = Locale.US;
	private static final Locale LOCALE_GERMANY = Locale.GERMANY;

	
	// resource keys to be passed to server to lookup
	private static final String [] resourceKeys = {
		regex_section_specialChars,
		regex_section_startsWith,
		regex_rowseat_specialChars
	};
	
	private static String INVENTORY_GROUP_NAME = "inventory";

	// The defautls values are defined here (en-US) 
	private static HashMap <String,String> defaultResourcesMap = new HashMap <String,String> ();

	// validation map as loaded from resources server
	private static HashMap <String,String> resourcesMap = new HashMap <String,String> ();
	
	// make en_US default locale 
	private static Locale defaultLocale = Locale.US;
	
	private boolean loadedResources = false;
	
	// These are all the fall-back defaults
	static {
		// default fall back resource maps
		defaultResourcesMap.put( regex_section_specialChars, "[\\p{Cc}{}<>?=&^\\\\]");
		defaultResourcesMap.put( regex_section_startsWith, "[^\\s*]");
		defaultResourcesMap.put( regex_rowseat_specialChars, "[\\p{Cc}{}<>?=&^\\\\]" );
		
		// language specific defaults
	/*	defaultResourcesMap.put( makeKey(regex_section_specialChars, LOCALE_GERMANY), "[\\p{Cc}{}<>?=&^\\\\äüöÄÜÖßé]" );
		defaultResourcesMap.put( makeKey(regex_section_startsWith, LOCALE_GERMANY),   "[a-zA-Z0-9äüöÄÜÖßé.]+.*" );
		defaultResourcesMap.put( makeKey(regex_rowseat_specialChars, LOCALE_GERMANY), "[^a-zA-Z0-9\\s\\_\\-\\,\\/\\äüöÄÜÖßé]" );		*/
	}

	/**
	 * Reset data in instance (will cause reload next time)
	 */
	public void resetInstance ()
	{
		loadedResources = false;
		resourcesMap.clear();
	}
	
	/**
	 * Get resource by key and locale (if resource does not exist on server it will use a fallback value)
	 * @param key
	 * @param locale
	 * @return
	 */
	public String getResource ( String key, Locale pLocale ) 
	{
		//SELLAPI-1135 sonar-rules, avoid reassigning to parameters.
		Locale locale = pLocale;
		// load resources only once
		if ( !loadedResources ) { 
			synchronized ( resourcesMap ) {
				if ( !loadedResources ) {
					loadResouces();
				}
			}
		}
		if ( locale == null ) {
			locale = defaultLocale;
		}
		String keyWithLocale = makeKey ( key, locale );
		String resValue = resourcesMap.get(keyWithLocale);
		
		if ( resValue != null ) {
			if ( log.isDebugEnabled() )
				log.debug("Loaded resource (" + keyWithLocale + ", " + resValue + ")" );
		}
		// if can't find in resource map, lookup in defaults map
		else {
			resValue = defaultResourcesMap.get(keyWithLocale);
			if ( resValue != null ) {
				if ( log.isDebugEnabled() )
					log.debug("Loaded resource default (" + keyWithLocale + ", " + resValue + ")" );
			}
			else {
				resValue = defaultResourcesMap.get(key);
				if ( log.isDebugEnabled() )
					log.warn("Loaded resource fall-back (" + key + ", " + resValue + ")"  );
			}
		}
		return resValue;
	}
	
	/**
	 * Internal load resources method
	 */
	private void loadResouces () 
	{
		try {
			getResourceValues (INVENTORY_GROUP_NAME, 
				resourceKeys,
				LOCALE_US,
				resourcesMap);
			
			getResourceValues (INVENTORY_GROUP_NAME, 
				resourceKeys,
				LOCALE_GERMANY,
				resourcesMap);			
		}
		catch ( ClientWebApplicationException ex ) {
			log.error("Parse error loading I18N resources from resource server", ex );
		}
		catch ( Exception ex ) {
			log.error("Errors loading I18N resources from resource server", ex );
		}
		loadedResources = true;		
	}
	
	/**
	 * Load resource values from resource server
	 * @param group
	 * @param keys
	 * @param locale
	 * @param resources
	 * @throws ParsingException 
	 * @throws IOException 
	 * @throws Exception
	 */
	private void getResourceValues (String group, String [] keys, Locale locale, 
			Map<String,String>resources ) throws IOException, ParsingException {
		String resourceUrl = getProperty("i18n.localization.url", 
				"http://api.stubcloudprod.com/i18n/localization/v1/resourcegroups");

		resourceUrl += "/" + group;
		
		StringBuilder sb = new StringBuilder (512);
		sb.append(resourceUrl).append("?keyList=");
		
		for ( int i=0; i<keys.length; i++ ) {
			sb.append( keys[i] ).append(',');
		}
		sb.setLength(sb.length()-1);
	    
	    if ( log.isDebugEnabled() )
	    	log.debug("getResourceValues URL: " + sb.toString() );
	    
	    String localeStr = locale.getLanguage() + "_" + locale.getCountry();
	    
		SvcLocator svcLocator = getSvcLocator();
	    WebClient webClient = svcLocator.locate(sb.toString());
	    webClient.header("accept-language", localeStr );
	    webClient.accept(MediaType.APPLICATION_XML);

		SHMonitor mon = SHMonitorFactory.getMonitor();
		Response response = null;
		try {
			mon.start();
			response = webClient.get();
		} finally {
			mon.stop();
			log.info(SHMonitoringContext.get() + " _operation=getResourceValues" + " _message= service call for group=" + group + "  _respTime=" + mon.getTime());
		}

	    log.info("getResourceValues resp status: " + response.getStatus() );
	    if(Response.Status.OK.getStatusCode() == response.getStatus()) {
	    	if ( !parseResourceValues ( response, resourcesMap, locale ) ) {
	    		log.warn( "Resources not found on server for locale: " + locale +", URL: " + sb.toString() );
	    	}
		} 
	    else if(Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()){
			log.error("Resource not found URL: " + sb.toString() );
		} 
	    else {
			log.error("Resource loading error URL: " + sb.toString() + ", status: " + response.getStatus() );
		}	
	}

	/**
	 * parse resource values from XML resp
	 * @param response
	 * @param resources
	 * @param locale
	 * @return
	 * @throws IOException 
	 * @throws ParsingException 
	 * @throws Exception
	 * 
	 * TODO: IMPORTANT NOTE: The structure of values of resource keys is still not clear. 
	 * We filled issue# PLATFORM-1998 to solve this problem
	 */
	private boolean parseResourceValues ( Response response, 
			Map<String,String>resources, Locale locale ) throws IOException, ParsingException {
		boolean loadedResources = false;
		Nodes nodes = null;
		InputStream is = (InputStream) response.getEntity();
		String respXML = CommonUtils.streamToString ( is );

		Document doc = stringToDocument (respXML );
		if (doc != null){
			 nodes = doc.query("//resources");
		}
		if ( nodes!=null && nodes.size()> 0 ) {
			for ( int i=0; i<nodes.size(); i++ ) {
				Node node = nodes.get(i);
				Node nkey = node.getChild(0);
				Node vals = node.getChild(2);
				if ( nkey != null && vals != null ) {
					Node val = vals.getChild(0);

					if ( val != null) {
						String skey = nkey.getValue();
						String sval = val.getValue();
						if ( sval!=null && sval.length()>0 ) {
							// key consists of resourceKey + locale
							String key = makeKey (skey, locale);
							resources.put(key, sval);

							loadedResources = true;
						}
					}
				}
			}
		}
		return loadedResources;
	}
	
	/**
	 * Make key from resource and locale
	 * @param resourceKey
	 * @param locale
	 * @return  
	 */
	private static String makeKey ( String resourceKey, Locale locale )
	{
		StringBuilder sb = new StringBuilder(64);
		sb.append(resourceKey).append('.').append(locale.toString());
		return sb.toString();
	}
	
	private Document stringToDocument(String xmlString) throws IOException, ParsingException {
		if (xmlString == null || xmlString.trim().length() == 0) {
			return null;
		}
		Builder builder = new Builder();
		return builder.build(xmlString, "");
	}
	
	// Note the following methods are public so they can be tested (mocked)
	public String getProperty (String propertyName, String defaultValue) {
		return MasterStubHubProperties.getProperty(propertyName, defaultValue);
	}
	
	public SvcLocator getSvcLocator ()
	{
		return new SvcLocator();
	}
}
