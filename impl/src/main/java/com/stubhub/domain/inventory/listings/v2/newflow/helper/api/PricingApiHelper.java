package com.stubhub.domain.inventory.listings.v2.newflow.helper.api;

import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.util.JsonUtil;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.util.CommonUtils;
import com.stubhub.domain.pricing.intf.aip.v1.request.PriceRequestList;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponseList;

public class PricingApiHelper extends ApiHelper {

  private static final String PRICING_API_URL = "pricing.v1.price.api.url";
  private static final String PRICING_API_URL_DEFAULT =
      "http://api-int.stubprod.com/pricing/aip/v1/price";

  private static final Logger log = LoggerFactory.getLogger(PricingApiHelper.class);

  public PriceResponseList getListingAIPPricings(PriceRequestList requestList) {
    String requestUrl = getProperty(PRICING_API_URL, PRICING_API_URL_DEFAULT);
    try {
      log.debug("message=\"Pricing API Request ={}\"", requestList.getPriceRequest().toString());	
      Response response = post(requestUrl, requestList);
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      if (Response.Status.OK.getStatusCode() == response.getStatus()) {
        String responseJson = CommonUtils.streamToString((InputStream) response.getEntity());
        PriceResponseList priceResponseList =
            (PriceResponseList) JsonUtil.toObjectWrapRoot(responseJson, PriceResponseList.class);
        return priceResponseList;
      }

      // Pricing API error
      log.info("message=\"Pricing API returned non success response\" statusCode={}", response.getStatus());
      if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()
          || Response.Status.BAD_REQUEST.getStatusCode() == response.getStatus()) {
        throw new ListingException(ErrorType.NOT_FOUND, ErrorCodeEnum.pricingApiError,
            "Price not found");
      } else {
        throw new ListingException(ErrorType.SYSTEMERROR, ErrorCodeEnum.pricingApiError);
      }
    } catch (Exception e) {
      log.error("message=\"Error while processing pricing api\"", e);
      throw new ListingException(ErrorType.SYSTEMERROR, ErrorCodeEnum.pricingApiError);
    }
  }

}
