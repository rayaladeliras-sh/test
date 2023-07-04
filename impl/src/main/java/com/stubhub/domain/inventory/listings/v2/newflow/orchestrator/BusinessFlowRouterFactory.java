package com.stubhub.domain.inventory.listings.v2.newflow.orchestrator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingType;

@Component("businessFlowRouterFactory")
public class BusinessFlowRouterFactory {
  
//	@Autowired()
//	private CreateListingFlowRouter createListingFlowRouter;

	@Autowired
	private UpdateListingFlowRouter updateListingFlowRouter;

	public BusinessFlowRouter getBusinessFlowRouter(ListingType listingType) {
		switch (listingType.getOperationType()) {
		// By Default send Create Listing Router
//		case CREATE:
//			return createListingFlowRouter;
		case UPDATE:
        default:
			return updateListingFlowRouter;
		}
	}
}
