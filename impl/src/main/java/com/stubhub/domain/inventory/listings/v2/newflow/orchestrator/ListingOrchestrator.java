package com.stubhub.domain.inventory.listings.v2.newflow.orchestrator;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.i18n.infra.soa.core.I18nServiceContext;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingType;
import com.stubhub.domain.inventory.listings.v2.newflow.handler.BusinessFlowHandler;
import com.stubhub.domain.inventory.listings.v2.newflow.handler.ExceptionHandler;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.ListingTypeHelper;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.RequestHelper;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.RequestValidatorHelper;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;

@Component("listingOrchestrator")
public class ListingOrchestrator {

  private final static Logger log = LoggerFactory.getLogger(ListingOrchestrator.class);

  @Autowired
  private ListingTypeHelper listingTypeHelper;

  @Autowired
  private RequestHelper requestHelper;

  @Autowired
  private RequestValidatorHelper requestValidatorHelper;

  @Autowired
  private BusinessFlowRouterFactory businessFlowRouterFactory;

  @Autowired
  ExceptionHandler exceptionHandler;

  // Create Single Listing
  public ListingResponse createListing(ListingRequest listingRequest,
      SHServiceContext shServiceContext, I18nServiceContext i18nServiceContext,
      MessageContext context) {
    log.info("message=\"START create listing\" sellerId={}", "",
        shServiceContext.getExtendedSecurityContext().getUserId());
    // Listing Type for create listing
    ListingType listingType = listingTypeHelper.getCreateListingType();

    return processListing(listingType, listingRequest, shServiceContext, i18nServiceContext,
        context);
  }

  // Update Single Listing
  public ListingResponse updateListing(String listingId, ListingRequest listingRequest,
      SHServiceContext shServiceContext, I18nServiceContext i18nServiceContext,
      MessageContext context) {
    log.info("message=\"START update listing\" listingId={} sellerId={}", listingId,
        shServiceContext.getExtendedSecurityContext().getUserId());
    // Listing Type for update listing
    ListingType listingType = listingTypeHelper.getUpdateListingType();

    listingRequest.setListingId(new Long(listingId));

    return processListing(listingType, listingRequest, shServiceContext, i18nServiceContext,
        context);
  }

  // Process all type of listings
  private ListingResponse processListing(ListingType listingType, ListingRequest listingRequest,
      SHServiceContext shServiceContext, I18nServiceContext i18nServiceContext,
      MessageContext context) {
    try {
      // Validate Request
      requestValidatorHelper.validateRequest(listingType, listingRequest);

      // Create Listing Data Transfer Object
      ListingDTO listingDTO =
          requestHelper.getListingDTO(listingType, listingRequest, context, shServiceContext);

      // Call Business Flow Router
      BusinessFlowHandler businessFlowHandler = businessFlowRouterFactory
          .getBusinessFlowRouter(listingType).getBusinessFlowHandler(listingDTO);

      // If handler is null, skip to the old flow
      if (businessFlowHandler == null) {
        return null;
      }

      // Execute the handler
      ListingResponse listingResponse = businessFlowHandler.execute();

      return listingResponse;
    } catch (Throwable t) {
      return exceptionHandler.handle(t);
    }
  }

}
