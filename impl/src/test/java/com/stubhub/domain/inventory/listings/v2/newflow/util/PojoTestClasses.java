package com.stubhub.domain.inventory.listings.v2.newflow.util;

import com.stubhub.domain.inventory.listings.v2.newflow.dto.EventInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.FulfillmentInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ProductInfo;

public abstract class PojoTestClasses {
  @SuppressWarnings("rawtypes")
  protected Class[] classes = {EventInfo.class, FulfillmentInfo.class, ListingInfo.class, ProductInfo.class};
}
