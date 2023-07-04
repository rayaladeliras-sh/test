package com.stubhub.domain.inventory.listings.v2.helper;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stubhub.domain.i18n.globalregistry.v2.intf.dto.response.EventCountry;
import com.stubhub.domain.i18n.globalregistry.v2.intf.dto.response.Feature;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

@Component("globalRegistryServiceHelper")
public class GlobalRegistryServiceHelper {

	private final static Logger log = LoggerFactory
			.getLogger(GlobalRegistryServiceHelper.class);

	private static final String GLOBALREGISTRY_API_URL = "global.registry.v2.api.url";
	private static final String GLOBALREGISTRY_API_URL_DEFAULT = "http://api-int.stubprod.com/i18n/globalregistry/v2/countries/{countrycode}";
	private static final String SECTIONZONE_TOGGLE ="event.sectionZoneToggle.show";
    private static final String NEWAPI_ACCESS_TOKEN_KEY = "newapi.accessToken";
    private static final String ACCESS_TOCKEN_DEFAULT_VALUE = "gWwh4zP4l90Cj4wQCslKHpB67_8a";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

	
	@Autowired
	private SvcLocator svcLocator;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	

	public EventCountry getEventCountryFeatures(String countryCode) {
		String globalRegApiUrl = getProperty(GLOBALREGISTRY_API_URL,GLOBALREGISTRY_API_URL_DEFAULT);
		globalRegApiUrl = globalRegApiUrl.replace("{countrycode}", countryCode);
		
		try{
	        String accessToken = getProperty(NEWAPI_ACCESS_TOKEN_KEY, ACCESS_TOCKEN_DEFAULT_VALUE);
	        String authorization = BEARER + accessToken;
	        
			WebClient webClient = svcLocator.locate(globalRegApiUrl);
			webClient.accept(MediaType.APPLICATION_JSON);
			webClient.header("Content-Type", MediaType.APPLICATION_JSON);
	        webClient.header(AUTHORIZATION, authorization);
	
			SHMonitor mon = SHMonitorFactory.getMonitor();
			Response response = null;
			try {
				mon.start();
				response = webClient.get();
			} finally {
				mon.stop();
				log.info(SHMonitoringContext.get() + " _operation=getSectionZoneToggleByCountry"
						+ " _message= service call for global registry countryCode= "+countryCode + "  _respTime=" + mon.getTime());
			}
	
			if (Response.Status.OK.getStatusCode() == response.getStatus()) {
				log.info("_message=\" Global Registry api call successful for \" countryCode={}", countryCode);
				InputStream is = (InputStream) response.getEntity();
				return objectMapper.readValue(is, EventCountry.class);
			} else if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
				log.error("_message=\"Not found\", responseCode={} countryCode={}",response.getStatus(), countryCode);
			} else {
				log.error("_message=\"System error occured while calling global registry api\" responseCode={} countryCode={}",response.getStatus(), countryCode);
			}
		} catch (Exception e) {
			log.error("_message=\"Exception occured while calling global registery  api\", countryCode=" + countryCode, e);
		}
		return null;
	}

	protected String getProperty(String propertyName, String defaultValue) {
		return MasterStubHubProperties.getProperty(propertyName, defaultValue);
	}
	
	public Boolean getSectionZoneToggleByCountry(String countryCode) {
		EventCountry eventCountryFeatures =  getEventCountryFeatures(countryCode) ;
		if(eventCountryFeatures != null){
			List<Feature> eventCountryFeatureList = eventCountryFeatures.getFeatureList();
			if(eventCountryFeatureList != null){
				for(Feature feature : eventCountryFeatureList ){
					if(SECTIONZONE_TOGGLE.equalsIgnoreCase(feature.getFeatureName()))
						return feature.getFeatureValue();
				}
			}	
		}
		log.info("_message=\" Global Registry api SectionZone feature toggle value is ON by default\" featureValue={}", false);
		return false;
	}
	

}
