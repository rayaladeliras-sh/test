package com.stubhub.domain.inventory.listings.v2.newflow.helper.api;

import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import com.stubhub.domain.fulfillment.window.v1.intf.EventFulfillmentWindowResponse;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.api.ApiHelper;

public class FulfillmentApiHelper extends ApiHelper {

  private static final String FF_API_URL = "fulfillment.window.v1.shape.api.url";
  private static final String FF_API_URL_DEFAULT =
      "http://api-int.stubprod.com/fulfillment/window/v1/event/{eventId}/?sellerContactId={sellerContactId}";

  public EventFulfillmentWindowResponse callFulfillmentWindowApi(String eventId,
      String sellerContactId) {
    EventFulfillmentWindowResponse efwResponse = null;
    try {
      String ffApiURL = getProperty(FF_API_URL, FF_API_URL_DEFAULT);
      ffApiURL = ffApiURL.replace("{eventId}", eventId);
      ffApiURL = ffApiURL.replace("{sellerContactId}", sellerContactId);
      Response response = get(ffApiURL);
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      if (Response.Status.OK.getStatusCode() == response.getStatus()) {
        InputStream is = (InputStream) response.getEntity();
        efwResponse = objectMapper.readValue(is, EventFulfillmentWindowResponse.class);
      } else {
        throw new SHSystemException();
      }

    } catch (Exception ex) {
      throw new SHSystemException();
    }
    return efwResponse;
  }

}
