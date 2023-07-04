package com.stubhub.domain.inventory.listings.v2.helper;

import java.io.InputStream;
import java.net.URLEncoder;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.infrastructure.caching.client.core.L2Cache;
import com.stubhub.domain.infrastructure.caching.client.core.L2CacheManager;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.eligibility.dto.response.GetSellerEligibilityResponse;
import com.stubhub.domain.inventory.eligibility.dto.response.Reason;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

@Component("sellerEligibilityHelper")
public class SellerEligibilityHelper {

  private final static org.slf4j.Logger log = LoggerFactory.getLogger(SellerEligibilityHelper.class);
  private static final String SELLER_ELIGIBILITY_CACHE = "sellerEligibility";
  private static final String DELIMITER = "_";
  public static final String YES = "YES";
  public static final String NO = "NO";

  @Autowired
  private SvcLocator svcLocator;

  @Autowired
  @Qualifier("L2CacheManager")
  L2CacheManager cacheManager;

  @Value("${SellerEligibility.check.enabled:true}")
  private boolean enabled = true;
    
  public boolean checkSellerEligibility(String sellerGuid, String listingSource, Long eventId) {
    if (!enabled) {
      log.warn("disabled SellerEligibility, always allow to sell");
      return true;
    }
    try {
      String userSpecificKey = sellerGuid + DELIMITER + eventId;
      L2Cache<Object> cache = cacheManager.getCache(SELLER_ELIGIBILITY_CACHE);
      Object isSellerEligible = cache.get(userSpecificKey);
      if (isSellerEligible != null) {
        log.info(
            "_message=\"get from couchbase cache\"  isSellerEligible={},sellerGuid={},eventId={}",
            isSellerEligible, sellerGuid, eventId);
        return (Boolean) isSellerEligible;
      } else {
        GetSellerEligibilityResponse sellerEligibilityResponse = null;
        String sellerEligibilityApiUrl = getProperty("seller.eligibility.api.url",
            "http://api-int.stubprod.com/inventorynew/eligibility/v1/sellereligibility");
        sellerEligibilityApiUrl = sellerEligibilityApiUrl + "?sellerGuid="
            + URLEncoder.encode(sellerGuid, "UTF-8").replace("+", "%20");
        sellerEligibilityApiUrl = sellerEligibilityApiUrl + "&eventId="
            + URLEncoder.encode(eventId.toString(), "UTF-8").replace("+", "%20");
        sellerEligibilityApiUrl = sellerEligibilityApiUrl + "&listingSource="
            + URLEncoder.encode(listingSource, "UTF-8").replace("+", "%20");
        log.info(
            "_message=\"seller eligibility api url\" sellerEligibilityAPIUrl={},sellerGuid={},eventId={}",
            sellerEligibilityApiUrl, sellerGuid, eventId);
        WebClient webClient = svcLocator.locate(sellerEligibilityApiUrl);
        webClient.accept(MediaType.APPLICATION_JSON);

        SHMonitor mon = SHMonitorFactory.getMonitor();
        Response response = null;
        try {
          mon.start();
          response = webClient.get();
        } finally {
          mon.stop();
          log.info(SHMonitoringContext.get() + " _operation=checkSellerEligibility"
              + " _message= service call for sellerGuid=" + sellerGuid + " listingSource="
              + listingSource + " eventId=" + eventId + "  _respTime=" + mon.getTime());
        }

        log.info("_message=\"response status is\" responseStatus={}", response.getStatus());
        if (Response.Status.OK.getStatusCode() == response.getStatus()) {
          InputStream responseStream = (InputStream) response.getEntity();
          ObjectMapper mapper = new ObjectMapper();
          sellerEligibilityResponse =
              mapper.readValue(responseStream, GetSellerEligibilityResponse.class);
          if (sellerEligibilityResponse != null) {
            if (YES.equalsIgnoreCase(sellerEligibilityResponse.getAllowedToSell())) {
              log.info("sellerEligibility check is success.");
              setSellerEligibilityCache(userSpecificKey, true);
              return true;
            } else if (NO.equalsIgnoreCase(sellerEligibilityResponse.getAllowedToSell())) {
              // BSF does not make SellerEligibility check fail
              if (sellerEligibilityResponse.getReasons() != null
                  && sellerEligibilityResponse.getReasons().size() > 0) {
                for (Reason reason : sellerEligibilityResponse.getReasons()) {
                  if (!reason.getReasonCode().equalsIgnoreCase("SellerInfoBlock") && !reason
                      .getReasonCode().equalsIgnoreCase("SellerInfoForCurrCountryBlock")) {
                    log.info("_message=\"sellerEligibility check is fail with\" reasonCode={}",
                        reason.getReasonCode());
                    setSellerEligibilityCache(userSpecificKey, false);
                    return false;
                  }
                }
                setSellerEligibilityCache(userSpecificKey, true);
                return true;
              }
              log.debug("sellerEligibility check is fail.");
            }
          }

        } else {
          InputStream is = (InputStream) response.getEntity();
          if (is != null) {
            if (log.isDebugEnabled()) {
              log.debug(String.format("[%d]  [%s]", response.getStatus(), IOUtils.toString(is)));
            }
            is.close();
          }
          ListingError listingError = new ListingError(ErrorType.SYSTEMERROR,
              ErrorCode.SYSTEM_ERROR, "System error occured", null);
          throw new ListingBusinessException(listingError);
        }
        setSellerEligibilityCache(userSpecificKey, false);
        return false;
      }
    } catch (Exception e) {
      ListingError listingError = new ListingError(ErrorType.SYSTEMERROR, ErrorCode.SYSTEM_ERROR,
          "System error occured", null);
      throw new ListingBusinessException(listingError);
    }
  }

  /**
   * Returns property value for the given propertyName. This protected method has been created to
   * get around the static nature of the MasterStubHubProperties' methods for Unit tests. The test
   * classes are expected to override this method with custom implementation.
   *
   * @param propertyName
   * @param defaultValue
   * @return
   */
  protected String getProperty(String propertyName, String defaultValue) {
      return MasterStubHubProperties.getProperty(propertyName, defaultValue);
  }

  /**
   * Setting Values to the SellerEligibility Cache, key is SellerGuid and EventId
   * @param userSpecificKey
   * @param sellerEligibilityStatus
   */
  private void setSellerEligibilityCache(String userSpecificKey, boolean sellerEligibilityStatus){
    try {
      SHMonitor mon = SHMonitorFactory.getMonitor();
      mon.start();
      L2Cache<Object> cache = cacheManager.getCache(SELLER_ELIGIBILITY_CACHE);
      cache.put(userSpecificKey, sellerEligibilityStatus);
      mon.stop();
      log.info("Saved in sellerEligibility couchbase cache. _respTime=" + mon.getTime());
    } catch (Exception e) {
      log.error(
        "_message=\"exception while setting key in couchbase\" _key={} _error={}",
        userSpecificKey,
        e.getMessage());
    }
  }
}