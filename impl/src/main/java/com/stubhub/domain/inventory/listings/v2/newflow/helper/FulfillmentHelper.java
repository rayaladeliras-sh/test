package com.stubhub.domain.inventory.listings.v2.newflow.helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import org.springframework.stereotype.Component;

import com.stubhub.domain.fulfillment.window.v1.intf.EventFulfillmentWindowResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.FulfillmentWindowResponse;
import com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.api.FulfillmentApiHelper;

@Component
public class FulfillmentHelper extends FulfillmentApiHelper {

  public List<FulfillmentWindow> getFulfillmentWindows(String eventId, String sellerContactId) {
    EventFulfillmentWindowResponse efwResponse = callFulfillmentWindowApi(eventId, sellerContactId);
    List<FulfillmentWindow> fulfillmentWindows = getFulfillmentWindowsFromResponse(efwResponse);
    return fulfillmentWindows;
  }

  public List<FulfillmentWindow> getFulfillmentWindowsFromResponse(
      EventFulfillmentWindowResponse efwResponse) {
    if (efwResponse == null) {
      return Collections.emptyList();
    }
    List<FulfillmentWindow> fulfillmentWindows = null;
    Collection<FulfillmentWindowResponse> fulfillmentWindowColl =
        efwResponse.getFulfillmentWindows();
    if (fulfillmentWindowColl == null) {
      return Collections.emptyList();
    }

    FulfillmentWindowResponse[] fwResponse =
        fulfillmentWindowColl.toArray(new FulfillmentWindowResponse[0]);
    fulfillmentWindows = processWindowResponse(fwResponse);
    return fulfillmentWindows;
  }

  private List<FulfillmentWindow> processWindowResponse(FulfillmentWindowResponse[] windows) {
    List<FulfillmentWindow> fulfillmentWindows = new ArrayList<FulfillmentWindow>();
    if (windows != null && windows.length > 0) {
      for (FulfillmentWindowResponse window : windows) {
        FulfillmentWindow ffWindow = new FulfillmentWindow();

        ffWindow.setFulfillmentMethodId(window.getFulfillmentMethod().getId());
        ffWindow.setFulfillmentTypeName(window.getFulfillmentMethod().getFulfillmentTypeName());
        ffWindow.setFulfillmentMethodName(window.getFulfillmentMethod().getName());
        ffWindow.setDeliveryMethodId(window.getDeliveryMethod().getId());
        ffWindow.setStartTime(window.getStartTime());
        ffWindow.setEndTime(window.getEndTime());
        ffWindow.setBaseCost(window.getBaseCost().getAmount().doubleValue());
        ffWindow.setTicketMedium(window.getFulfillmentMethod().getTicketMedium());
        fulfillmentWindows.add(ffWindow);
      }
    }
    return fulfillmentWindows;
  }

  public void fillFmDmList(StringBuffer fmDmList, FulfillmentWindow window) {
    fmDmList.append(window.getFulfillmentMethodId());
    fmDmList.append(",");
    fmDmList.append(window.getDeliveryMethodId());
    fmDmList.append(",");
    fmDmList.append(window.getBaseCost());
    fmDmList.append(",");
    fmDmList.append(",");
    fmDmList.append(getDateFormat().format(window.getEndTime().getTime()));
    fmDmList.append("|");
  }

  private static SimpleDateFormat getDateFormat() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    sdf.setLenient(false);
    return sdf;
  }

  public EventFulfillmentWindowResponse getFulfillmentWindowApi(String eventId,
      String sellerContactId) {
    EventFulfillmentWindowResponse efwResponse = callFulfillmentWindowApi(eventId, sellerContactId);
    
    return efwResponse;
    
  }


}
