package com.stubhub.domain.inventory.listings.v2.newflow.orchestrator;

import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.handler.BusinessFlowHandler;

public interface BusinessFlowRouter {
	public BusinessFlowHandler getBusinessFlowHandler(ListingDTO listingDTO);
}
