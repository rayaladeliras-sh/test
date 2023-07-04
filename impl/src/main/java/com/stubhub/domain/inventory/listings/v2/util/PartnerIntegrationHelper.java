package com.stubhub.domain.inventory.listings.v2.util;

import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Splitter;
import com.stubhub.domain.inventory.common.util.PartnerIntegrationConstants;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

/**
 * Helper or Utility class to handle partner integration related calls.
 * 
 * @author rkesara
 *
 */
@Component("partnerIntegrationHelper")
public class PartnerIntegrationHelper {

  private final static Logger logger = LoggerFactory.getLogger(PartnerIntegrationHelper.class);

  @Autowired
  private MasterStubhubPropertiesWrapper masterStubhubPropertiesWrapper;

  @Autowired
  private SvcLocator svcLocator;


  /**
   * Helper method to check if primary associated to an event is SHIP integrated or not.
   * 
   * @param eventId - StubHub eventId
   * @return true/false
   */
  public boolean isPartnerIntegratedOnSHIP(final Long eventId) {
    logger.debug("_message=\"Entering isPartnerIntegratedOnSHIP for event\" eventId={} ", eventId);
    boolean isPartnerIntegratedOnSHIP = true;

    String integrationDisabledPartners = masterStubhubPropertiesWrapper
        .getProperty(PartnerIntegrationConstants.SECONDRY_INTEGRATION_DISABLED_PARTNERS);

    if (StringUtils.isNotBlank(integrationDisabledPartners)) {
      // fetch ptvId from stubhub eventId
      Long ptvId = getPrimaryTicketVendorId(eventId);

      if (ptvId != null) {
        // extract iterable collection of disabled partners
        Iterable<String> disabledPartnerIds =
            Splitter.on(',').omitEmptyStrings().split(integrationDisabledPartners);
        // iterate over each disabled partnerId and check
        for (String disabledPartnerId : disabledPartnerIds) {
          if (Long.valueOf(disabledPartnerId).longValue() == ptvId.longValue()) {
            isPartnerIntegratedOnSHIP = false;
            break;
          }
        }
      } else {
    	  isPartnerIntegratedOnSHIP = false;
      }
    }

    logger.debug(
        "_message=\"Leaving isPartnerIntegratedOnSHIP for event\" eventId={} isPartnerIntegratedOnSHIP={}",
        eventId, isPartnerIntegratedOnSHIP);
    return isPartnerIntegratedOnSHIP;
  }

  /**
   * Helper method to invoke <b>PartnerEvents</b> API and find out if a given event is integrated or
   * not and if it is integrated, it fetches the primary information from that.
   * 
   * @param eventId - Stubhub EventId
   * @return - PrimaryTicketVendor Id
   */
  public Long getPrimaryTicketVendorId(final Long eventId) {
    logger.debug("_message=\"Entering getPrimaryTicketVendorId for event\" eventId={} ", eventId);
    Long primaryTicketVendorId = null;
    
    SHAPIContext apiContext = SHAPIThreadLocal.getAPIContext();
    
    try {
    	
    	if(apiContext == null) {
        	apiContext = new SHAPIContext();
        	SHAPIThreadLocal.set(apiContext);
       			
        }
      String serviceUrl = masterStubhubPropertiesWrapper.getProperty(
          PartnerIntegrationConstants.PARTNEREVENTS_API_URL,
          PartnerIntegrationConstants.DEFAULT_PARTNEREVENTS_API_URL);
      serviceUrl = serviceUrl.replace("{eventId}", String.valueOf(eventId));

      // lookup service client
      WebClient client = getWebClient(serviceUrl);
      client.accept(MediaType.APPLICATION_JSON);
      Response response = client.get();

      if (response != null && Response.Status.OK.getStatusCode() == response.getStatus()) {
        ObjectMapper mapper = new ObjectMapper();
        InputStream responseStream = (InputStream) response.getEntity();
        JsonNode rootNode = mapper.readTree(responseStream);
        if (rootNode != null && rootNode.get("ptvId") != null) {
          primaryTicketVendorId = rootNode.get("ptvId").asLong();
        }
      }

    } catch (Exception e) {
      logger.error(
          "_message=\"Error occured while fetching PrimaryTicketVendorId for event\" eventId={}",
          eventId, e);
    }

    logger.debug(
        "_message=\"Leaving getPrimaryTicketVendorId for event\" eventId={} primaryTicketVendorId={}",
        eventId, primaryTicketVendorId);
    return primaryTicketVendorId;
  }


  /**
   * Helper method to get handle to WebClient.
   * 
   * @param serviceUrl - Service URL
   * @return {@link WebClient}
   */
  public WebClient getWebClient(final String serviceUrl) {
    WebClient webClient = svcLocator.locate(serviceUrl);
    String accessToken = masterStubhubPropertiesWrapper.getProperty(
        PartnerIntegrationConstants.NEWAPI_ACCESS_TOKEN_KEY,
        PartnerIntegrationConstants.ACCESS_TOCKEN_DEFAULT_VALUE);
    String authorization = PartnerIntegrationConstants.BEARER + accessToken;
    webClient.header(PartnerIntegrationConstants.AUTHORIZATION, authorization);
    return webClient;
  }


}
