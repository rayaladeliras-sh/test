package com.stubhub.domain.inventory.listings.v2.newflow.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.fulfillment.window.v1.intf.EventFulfillmentWindowResponse;
import com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.FulfillmentInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.FulfillmentWindowsException;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.FulfillmentHelper;

@Component
@Scope("prototype")
public class FulfillmentTask extends RegularTask {

  private static final Logger logger = LoggerFactory.getLogger(FulfillmentTask.class);

  // Helpers
  @Autowired
  private FulfillmentHelper fulfillmentHelper;

  public FulfillmentTask(ListingDTO dto) {
    super(dto);
  }

  private EventFulfillmentWindowResponse efwResponse = null;

  private List<FulfillmentWindow> fulfillmentWindows = null;

  @Override
  public void execute() {
    efwResponse =
        fulfillmentHelper.getFulfillmentWindowApi(listingDTO.getEventInfo().getEventId().toString(),
            listingDTO.getSellerInfo().getSellerContactId().toString());
    if (efwResponse == null) {
      logger.error("message=\"No fulfillment windows are available\" eventId={}",
          listingDTO.getEventInfo().getEventId());
      throw new FulfillmentWindowsException(ErrorType.INPUTERROR,
          ErrorCodeEnum.noFulfillmentWindowsAvailable);
    }
    fulfillmentWindows = fulfillmentHelper.getFulfillmentWindowsFromResponse(efwResponse);
  }

  @Override
  protected void preExecute() {}

  @Override
  protected void postExecute() {
    
    FulfillmentInfo fulfillmentInfo = new FulfillmentInfo();
    listingDTO.setFulfillmentInfo(fulfillmentInfo);
    fulfillmentInfo.setFulfillmentWindows(fulfillmentWindows);

    Map<Long, FulfillmentWindow> fulfillmentWindowMap = new HashMap<Long, FulfillmentWindow>();
    for (FulfillmentWindow fw : fulfillmentWindows) {
      fulfillmentWindowMap.put(fw.getFulfillmentMethodId(), fw);
    }
    fulfillmentInfo.setFulfillmentWindowMap(fulfillmentWindowMap);
    fulfillmentInfo.setInHandDateSettings(efwResponse.getInHandDateSettings());

  }

}
