package com.stubhub.domain.inventory.listings.v2.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import com.stubhub.domain.inventory.listings.v2.newflow.helper.api.CheckoutTransferAPIHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.accountmanagement.services.orders.v3.intf.dto.OrderDetailsV3DTO;
import com.stubhub.domain.accountmanagement.services.orders.v3.intf.dto.OrderItem;
import com.stubhub.domain.i18n.infra.soa.core.I18nServiceContext;
import com.stubhub.domain.infrastructure.common.exception.base.SHMappableException;
import com.stubhub.domain.infrastructure.common.exception.base.SHRuntimeException;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHBadRequestException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHForbiddenException;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.adapter.ListingResponseAdapter;
import com.stubhub.domain.inventory.listings.v2.aspects.ExcludeLogParam;
import com.stubhub.domain.inventory.listings.v2.aspects.LogEvent;
import com.stubhub.domain.inventory.listings.v2.controller.helper.IntegrationHelper;
import com.stubhub.domain.inventory.listings.v2.helper.AdvisoryCurrencyHelper;
import com.stubhub.domain.inventory.listings.v2.helper.ListingControlHelper;
import com.stubhub.domain.inventory.listings.v2.helper.ListingCreateProcess;
import com.stubhub.domain.inventory.listings.v2.helper.ListingHelper;
import com.stubhub.domain.inventory.listings.v2.helper.RelistHelper;
import com.stubhub.domain.inventory.listings.v2.helper.TransferValidator;
import com.stubhub.domain.inventory.listings.v2.newflow.orchestrator.ListingOrchestrator;
import com.stubhub.domain.inventory.listings.v2.util.CommonUtils;
import com.stubhub.domain.inventory.listings.v2.util.JMSMessageHelper;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.DTO.RelistListing;
import com.stubhub.domain.inventory.v2.DTO.RelistRequest;
import com.stubhub.domain.inventory.v2.DTO.RelistResponse;
import com.stubhub.domain.inventory.v2.DTO.TicketTrait;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingInternal;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.domain.inventory.v2.enums.ProxyRoleTypeEnum;
import com.stubhub.domain.inventory.v2.enums.TicketMedium;
import com.stubhub.domain.inventory.v2.listings.service.CXFMessageContextSetter;
import com.stubhub.domain.inventory.v2.listings.service.ListingService;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;

@Component("listingServiceV2")
public class ListingServiceImpl implements ListingService, CXFMessageContextSetter{

  private static final String EXTERNAL_LISTING_ID = "externalListingId";

private static final String APPLICATION = "application";

private static final String RELIST = "Relist";

  private final static Logger log = LoggerFactory.getLogger(ListingServiceImpl.class);

  private static String api_domain = "inventory";
  private static String api_resource = "listing";
  static final String ACTION_VALIDATE_BARCODES = "validateBarcodes";

  static final String OPERATOR_URL = "http://stubhub.com/claims/operatorapp";
  static final String SUBSCRIBER_URL = "http://stubhub.com/claims/subscriber";

  static final String RELIST_MARKER= "Relist|V2|";
  
  @Context private MessageContext context;
  
//  @Context
 // private HttpHeaders httpHeaders;

//  @Context
//  private UriInfo uriInfo;

  @Autowired
  private ListingCreateProcess listingCreateProcess;

  @Autowired
  private ListingHelper listingHelper;
 
  @Autowired
  private RelistHelper relistHelper;
	
  @Autowired
  private ListingOrchestrator listingOrchestrator;

  @Autowired
  private CheckoutTransferAPIHelper checkoutTransferAPIHelper;

  @Autowired
  private MasterStubhubPropertiesWrapper masterStubhubProperties;
  
	public ThreadLocal<String> relistFlag = new ThreadLocal<String>() {
        @Override protected String initialValue() {
            return null;
        }
	};
	

	@Autowired
	private JMSMessageHelper jmsMessageHelper;
	
	@Autowired
	private TransferValidator transferValidator;
	
	 @Autowired
		private InventoryMgr inventoryMgr;	 

	 @Autowired
	 private IntegrationHelper integrationHelper;

	 @Autowired
	 private AdvisoryCurrencyHelper advisoryCurrencyHelper;
	 
	 @Autowired
	 private ListingControlHelper listingControlHelper;

	 @Override
	 public void setMessageContext(MessageContext ctx)
	 {
	 this.context=ctx; 
	 }
	 
  /**
   * create a listing
   * 
   * @param request
   * @param securityContext
   * @return ListingResponse
   */
  @LogEvent
  @Override
  public ListingResponse createListing(ListingRequest request, @ExcludeLogParam SHServiceContext shServiceContext, I18nServiceContext i18nServiceContext) {
    int statusCode = 200;
    String errorCode = "";
    String description = "";

    try {
      String sellerId = null;
      String sellerGuid = null;
      SHAPIContext apiContext = SHAPIThreadLocal.getAPIContext();
      clearMDC();
      if(request.getExternalListingId() != null){
        MDC.put(EXTERNAL_LISTING_ID, request.getExternalListingId());
      }
      ExtendedSecurityContext securityContext = getSecurityContext(shServiceContext);

      // Added this check as part of SELLAPI-2480(CSAPIS-895)
      if ((request.getStatus() != null) && ("HIDDEN".equalsIgnoreCase(request.getStatus().toString()))
          && isAuthZRequest(shServiceContext)) {
        sellerId = shServiceContext.getProxiedId();
        if (sellerId != null) {
          if (!StringUtils.isNumeric(sellerId)) {
            log.info("userGuid is passed as ProxiedId value");
            sellerGuid = securityContext.getUserGuid();
            sellerId = securityContext.getUserId();
          } else {
            sellerGuid = integrationHelper.getUserGuidFromUid(Long.valueOf(sellerId));
          }
        }
        log.info("AuthZ request. sellerId={} sellerGuid={}", sellerId, sellerGuid);
      } else if (securityContext != null) {
            sellerId = securityContext.getUserId();
            sellerGuid = securityContext.getUserGuid();
            log.info("Not AuthZ request. sellerId={} sellerGuid={}", sellerId, sellerGuid);
          }

      // Security check, if cannot get seller id return INVALID_CREDENTIAL error
      if (sellerId == null) {
        log.warn("api_domain=" + api_domain + " api_resource=" + api_resource
            + " api_method=createListing status=success_with_error message=\"Authentication error while creating listing\"");

        throw new SHForbiddenException("Invalid security token");
      }
    
      boolean isBlock = validateListingBlock(true, request, sellerId);
      if(isBlock) {
        SHRuntimeException e = new SHRuntimeException("Request temporarily blocked. Please reach out to your StubHub account manager for details");
        e.setErrorCode("inventory.listings.blocked");
        e.setStatusCode(429);
        throw e;
      }
    
      // Get ShStore from service context and check if its a valid integer
      String shStoreStr =
          (shServiceContext != null && shServiceContext.getSHStore() != null) ? shServiceContext.getSHStore() : "1";
      Integer shStore = 1;
      try {
        shStore = Integer.parseInt(shStoreStr);
      } catch (NumberFormatException nfe) {
        log.error("_message=Invalid ShStore value. ShStore={}", new Object[] {shStoreStr});
      }
    
      // Seller id ok, process update listing
      try {
        log.info("_message=\"Creating Listing for\" sellerId={}" , sellerId);
        if (apiContext != null) {
          log.debug("_message=\"Assertion header\"  sellerId={} assertion={}" , sellerId , apiContext.getSignedJWTAssertion());
        }
        String clientIp = CommonUtils.getClientIP(context.getHttpHeaders());
        String userAgent = null;

        MultivaluedMap<String, String> headersMap = context.getHttpHeaders().getRequestHeaders();
        if (headersMap != null) {
          userAgent = headersMap.getFirst(HttpHeaders.USER_AGENT);
        }
        BulkListingInternal bli = new BulkListingInternal();
        List<ListingRequest> requests = new ArrayList<ListingRequest>(1);
        requests.add(request);
        bli.setCreateListingBody(requests);
        bli.setSellerId(Long.valueOf(sellerId));
        bli.setSellerGuid(sellerGuid);
        if (apiContext != null) {
          bli.setAssertion(apiContext.getSignedJWTAssertion());
        }
        bli.setLocale(i18nServiceContext.getLocale());

        bli.setSubscriber(getSubscriber(securityContext, null));
        bli.setSellShStoreId(getSHStoreID(i18nServiceContext));

        List<ListingResponse> responses = listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, true, false);
        StringBuilder sb = new StringBuilder("{\"subscriber\" : \"").append(MDC.get(APPLICATION)).append("\", " +
                "\"externalListingId\" : \"").append(MDC.get(EXTERNAL_LISTING_ID)).append("\", \"message\" : \"")
                .append("success").append("}");

        log.info("api_domain=" + api_domain + " api_resource=" + api_resource
                + " api_method=createListing status=success listing_id={} listing_status={} message={}"
                , responses.get(0).getId(), responses.get(0).getStatus(), sb);

          log.info("api_domain=" + api_domain + " api_resource=" + api_resource
                + " api_method=createListing responses={}", responses);

        return responses.get(0);
      } catch (ListingBusinessException listingException) {
        StringBuilder sb = new StringBuilder("{\"subscriber\" : \"").append(MDC.get(APPLICATION))
                .append("\", \"externalListingId\" : \"").append(MDC.get(EXTERNAL_LISTING_ID))
                .append("\", \"message\" : \"").append(listingException.getMessage()).append("\"}");

          // note errors are logged in createOrUpdateListings call
          log.warn("api_domain=" + api_domain + " api_resource=" + api_resource
                    + " api_method=createListing status=error message={}"
                    , sb);
        // note errors are logged in createOrUpdateListings call
        ListingResponseAdapter.errorMappingThrowException(listingException);
      } catch (SHRuntimeException shException) {
        StringBuilder sb = new StringBuilder("{\"subscriber\" : \"").append(MDC.get(APPLICATION))
                .append("\", \"externalListingId\" : \"").append(MDC.get(EXTERNAL_LISTING_ID))
                .append("\", \"message\" : \"").append(shException.getDescription()).append("\"}");

          // note errors are logged in createOrUpdateListings call
          log.warn("api_domain=" + api_domain + " api_resource=" + api_resource
                    + " api_method=updateListing status=error message={}"
                    , sb);
        throw shException;
      } catch (Throwable e) {
        // note errors are logged in createOrUpdateListings call
        StringBuilder sb = new StringBuilder("{\"subscriber\" : \"").append(MDC.get(APPLICATION))
                .append("\", \"externalListingId\" : \"").append(MDC.get(EXTERNAL_LISTING_ID))
                .append("\", \"message\" : \"").append("System error occured while update listing").append("\"}");
          log.error("api_domain=" + api_domain + " api_resource=" + api_resource
              + " api_method=updateListing status=error error_message=" + sb
              + " sellerId=" + sellerId, e);

      }
      throw new SHSystemException("Unable to create a listing from error, please re-try");
    } catch(Throwable t){
      if (t instanceof SHMappableException) {
        SHMappableException shException = (SHMappableException) t;
        statusCode = shException.getStatusCode();
        errorCode = shException.getErrorCode();
        description = shException.getDescription();
      } else {
        statusCode = 500;
      }

      throw t;
    } finally {
      log.info("_message=\"API Result\" api=\"{}\" statusCode={} errorCode=\"{}\" description=\"{}\"" ,
              "Create Listing", statusCode, errorCode, description);
    }
  }

  private Integer getSHStoreID(I18nServiceContext i18nServiceContext) {
    // Get ShStore from service context and check if its a valid integer
    String shStoreStr =
        (i18nServiceContext != null && i18nServiceContext.getSHStore() != null) ? i18nServiceContext.getSHStore() : "1";
    Integer shStore = 1;
    try {
      shStore = Integer.parseInt(shStoreStr);
    } catch (NumberFormatException nfe) {
      log.error("_message=Invalid ShStore value. ShStore={}", new Object[] {shStoreStr});
    }
    return shStore;
  }

  protected void clearMDC() {
	MDC.remove(APPLICATION);
	MDC.remove(EXTERNAL_LISTING_ID);
  }

  private boolean isNewUpdateListingFlow(String listingId) {
	  String globalSwitch = getNewFlowGlobalProperty();
	  String updateListingSwitch = getNewFlowUpdateListingProperty();

      log.info("api_domain={} api_resource={} api_method=updateListing listingId={} globalSwitch={} updateListingSwitch={} ",
    		  api_domain, api_resource, listingId, globalSwitch, updateListingSwitch);
      
      if("true".equalsIgnoreCase(globalSwitch) &&  "true".equalsIgnoreCase(updateListingSwitch)) {
    	  return true;
      }
      
      return false;
  }

  private String getNewFlowGlobalProperty() {
	  return masterStubhubProperties.getProperty("inventory.newflow.global", "false");
  }

  private String getNewFlowUpdateListingProperty() {
	  return masterStubhubProperties.getProperty("inventory.newflow.update.listing", "false");
  }

	/**
	 * updateListing API call
	 * 
	 * @param securityContext
	 * @param sellerGuid
	 * @param listingId
	 * @param updateListingRequest
	 * 
	 * @return ListingResponse (that can contain errors)
	 */
	@Override
	@LogEvent
	public ListingResponse updateListing( String listingId, ListingRequest listingRequest,
			@ExcludeLogParam SHServiceContext shServiceContext, I18nServiceContext i18nServiceContext) {
	   int statusCode = 200;
	   String errorCode = "";
	   String description = "";
	   
	   String sellerId = null;
       String sellerGuid = null;
       SHAPIContext apiContext = SHAPIThreadLocal.getAPIContext();
       ExtendedSecurityContext securityContext = getSecurityContext(shServiceContext);
       
	  try {
	    String userId = securityContext != null ? securityContext.getUserId() : "";
	    boolean isBlock = validateListingBlock(false, listingRequest, userId);
	    if(isBlock) {
	      SHRuntimeException e = new SHRuntimeException("Request temporarily blocked. Please reach out to your StubHub account manager for details");
	      e.setErrorCode("inventory.listings.blocked");
	      e.setStatusCode(429);
	      throw e;
	    }
	    
		// Check for new flow
		if (isNewUpdateListingFlow(listingId)) {
			ListingResponse newFlowResponse = listingOrchestrator.updateListing(listingId, listingRequest,
					shServiceContext, i18nServiceContext, context);
			log.info("api_domain={} api_resource={} api_method=updateListing listingId={} newFlowResponse={}",
					api_domain, api_resource, listingId, newFlowResponse);

			if (newFlowResponse != null) {
				return newFlowResponse;
			}

			log.info(
					"api_domain={} api_resource={} api_method=updateListing listingId={} message=\"The new flow is not enabled for this request, so proceeding with the old flow\"",
					api_domain, api_resource, listingId);
		} else {
			log.info(
					"api_domain={} api_resource={} api_method=updateListing listingId={} message=\"The new flow is not enabled, so proceeding with the old flow\"",
					api_domain, api_resource, listingId);
		}

		// old flow
		clearMDC();

		if (isAuthZRequest(shServiceContext)) {
			sellerId = shServiceContext.getProxiedId();
			if (sellerId != null) {
				if (!StringUtils.isNumeric(sellerId)) {
					log.info("AuthZ request. userGuid is passed as ProxiedId value");
					sellerGuid = securityContext.getUserGuid();
					sellerId = securityContext.getUserId();
				} else {
					sellerGuid = integrationHelper.getUserGuidFromUid(Long.valueOf(sellerId));
				}
			}
			log.info("AuthZ request. sellerId={} sellerGuid={}", sellerId, sellerGuid);
		} else if (securityContext != null) {
			//apiContext = 
			sellerGuid = securityContext.getUserGuid();
			sellerId = securityContext.getUserId();
		}

		String operatorId = null;
		ProxyRoleTypeEnum role = null;

    // Security check, if cannot get seller id return INVALID_CREDENTIAL error
    if (sellerId == null) {
      log.warn("api_domain=" + api_domain + " api_resource=" + api_resource
          + " api_method=updateListing status=success_with_error message=\"Authentication error while update listing\""
          + " listingId={} _respTime={}", listingId);

      throw new SHForbiddenException("Invalid security token");
    }

    //SHServiceContext shServiceContext = SHServiceContext.get();
    
    if (shServiceContext != null) {
      operatorId = shServiceContext.getOperatorId();
      String strRole = shServiceContext.getRole();
      if (StringUtils.isNotBlank(strRole)) {
        role = ProxyRoleTypeEnum.getProxyRoleTypeEnumByName(strRole);
      }

    }

    // Seller id ok, process update listing
    try {
      log.info("Updating Listing, listingId={} for sellerId={} operatorId={} role={}",
          new Object[] {listingId, sellerId, operatorId, role});
      if (apiContext != null) {
        log.debug("Assertion header ::: listingId=" + listingId + " assertion="
            + apiContext.getSignedJWTAssertion());
      }
      // sets the listingId
      listingRequest.setListingId(Long.valueOf(listingId));

      String clientIp = CommonUtils.getClientIP(context.getHttpHeaders());
      String userAgent = null;

      MultivaluedMap<String, String> headersMap = context.getHttpHeaders().getRequestHeaders();
      if (headersMap != null) {
        userAgent = headersMap.getFirst(HttpHeaders.USER_AGENT);
      }

      // setup the bulk object
      BulkListingInternal bli = new BulkListingInternal();
      List<ListingRequest> requests = new ArrayList<ListingRequest>(1);
      requests.add(listingRequest);
      bli.setCreateListingBody(requests);
      bli.setSellerId(Long.valueOf(sellerId));
      bli.setSellerGuid(sellerGuid);
      if (apiContext != null) {
        bli.setAssertion(apiContext.getSignedJWTAssertion());
      }
      bli.setLocale(i18nServiceContext.getLocale());

      bli.setSubscriber(getSubscriber(securityContext, operatorId));
      bli.setSellShStoreId(getSHStoreID(i18nServiceContext));

      bli.setOperatorId(operatorId);
      bli.setRole(role);

      // actual call update listing
      List<ListingResponse> responses =
          listingCreateProcess.createOrUpdateListings(bli, clientIp, userAgent, false, false);
      StringBuilder sb = new StringBuilder("subscriber = ").append(MDC.get(APPLICATION)).append(", externalListingId = ").append(MDC.get(EXTERNAL_LISTING_ID)).append(", message = ").append("success");
      log.info("api_domain=" + api_domain + " api_resource=" + api_resource
              + " api_method=updateListing status=success message={}"
              , sb);

      return responses.get(0);
    } catch (ListingBusinessException listingException) {
    	StringBuilder sb = new StringBuilder("subscriber = ").append(MDC.get(APPLICATION)).append(", externalListingId = ").append(MDC.get(EXTERNAL_LISTING_ID)).append(", message = ").append(listingException.getMessage());
        
      // note errors are logged in createOrUpdateListings call
    	log.warn("api_domain=" + api_domain + " api_resource=" + api_resource
                + " api_method=updateListing status=error message={}"
                , sb);
      ListingResponseAdapter.errorMappingThrowException(listingException);
    } catch (SHRuntimeException shException) {
    	StringBuilder sb = new StringBuilder("subscriber = ").append(MDC.get(APPLICATION)).append(", externalListingId = ").append(MDC.get(EXTERNAL_LISTING_ID)).append(", message = ").append(shException.getMessage());
        
        // note errors are logged in createOrUpdateListings call
      	log.warn("api_domain=" + api_domain + " api_resource=" + api_resource
                  + " api_method=updateListing status=error message={}"
                  , sb);
      throw shException;
    } catch (Throwable e) {
      // note errors are logged in createOrUpdateListings call
    	StringBuilder sb = new StringBuilder("subscriber = ").append(MDC.get(APPLICATION)).append(", externalListingId = ").append(MDC.get(EXTERNAL_LISTING_ID)).append(", message = ").append("System error occured while update listing");
        
      log.error("api_domain=" + api_domain + " api_resource=" + api_resource
          + " api_method=updateListing status=error error_message=" + sb
          + " sellerId=" + sellerId, e);
    }
    throw new SHSystemException("Unable to update a listing from error, please re-try");
	   }catch(Throwable t){
	      if (t instanceof SHMappableException) {
	        SHMappableException shException = (SHMappableException) t;
	        statusCode = shException.getStatusCode();
	        errorCode = shException.getErrorCode();
	        description = shException.getDescription();
	      } else {
	        statusCode = 500;
	      }
	      
	      throw t;
	   } finally {
	      log.info("_message=\"API Result\" api=\"{}\" statusCode={} errorCode=\"{}\" description=\"{}\"" , "Update Listing", statusCode, errorCode, description);
	   }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.stubhub.domain.inventory.v2.listings.intf.ListingService#getListing(com.stubhub.platform.
   * utilities.webservice.security.ExtendedSecurityContext)
   */
  @Override 
  public com.stubhub.domain.inventory.v2.DTO.ListingResponse getListing(String listingId,
      SHServiceContext shServiceContext, I18nServiceContext i18nServiceContext) {
    int statusCode = 200;
    String errorCode = "";
    String description = "";
    
    try{
    String userId = null;
    SHAPIContext apiContext = null;

    if (shServiceContext == null) {
    	log.error("api_domain=" + api_domain + " api_resource=" + api_resource
                + " api_method=getListing status=success_with_error message=\"shServiceContext null error while get listing\""
                + " listingId={} ", listingId);

            throw new SHForbiddenException("invalid or null shServiceContext");
    }
    
    String status = context.getUriInfo().getQueryParameters().getFirst("status");
    String expand = context.getUriInfo().getQueryParameters().getFirst("expand");

    log.info("_message=\"getListing\" listingId={} status={} expand={}", listingId, status, expand);

    String userCurrency = i18nServiceContext.getUserCurrency();
    log.info("_message=\"getListing\" userCurrency={} ", userCurrency);

    ExtendedSecurityContext securityContext = getSecurityContext(shServiceContext);
    ProxyRoleTypeEnum proxyRoleType = null;
    if (securityContext != null) {
      apiContext = SHAPIThreadLocal.getAPIContext();
      userId = securityContext.getUserId();
    }

	if (shServiceContext.getOperatorRole() != null) {
	  proxyRoleType = ProxyRoleTypeEnum.getProxyRoleTypeEnumByName(shServiceContext.getRole());
	}
	log.info("Proxy Role injected by service context: " + proxyRoleType);
	if (proxyRoleType == null && securityContext == null) {
	  log.warn("api_domain=" + api_domain + " api_resource=" + api_resource
	      + " api_method=getListing status=success_with_error message=\"Authentication error while get listing\""
	      + " listingId={} ", listingId);

	  throw new SHForbiddenException("Invalid security token");
	}

    Locale locale = CommonUtils.getClientLocale(context.getHttpHeaders());
    if (apiContext != null) {
      log.debug("_message=\"Assertion header\"  listingId={} assertion={}"
          , listingId, apiContext.getSignedJWTAssertion());
    }
    
    /* SELLAPI-3234 - Fix - java.lang.NumberFormatException */
    Long listingIdLong = null;
    try {
    	log.debug("ListingId =" + listingId);
    	listingIdLong = Long.parseLong(listingId);
    }
    catch (NumberFormatException e) {
    	SHRuntimeException shException = new SHBadRequestException("invalid listingId");
		shException.setErrorCode("inventory.listings.invalidrequest.listingId");
		throw shException;
    }
    
    try {
      Long userIdLong = null;
      if (userId != null) {
        userIdLong = Long.valueOf(userId);
      }
      ListingResponse listingResponse = listingHelper.populateListingDetails(status,
    		  listingIdLong, locale, userIdLong, shServiceContext, expand);

      removeSeatStatus(listingResponse,status);
      advisoryCurrencyHelper.setForex(listingResponse, userCurrency);
      return listingResponse;
    } catch (ListingBusinessException listingException) {
      ListingResponseAdapter.errorMappingThrowException(listingException);
    } catch (SHRuntimeException shException) {
      throw shException;
    }
    throw new SHSystemException("Unable to update a listing from error, please re-try");
  }catch(Throwable t){
    if (t instanceof SHMappableException) {
      SHMappableException shException = (SHMappableException) t;
      statusCode = shException.getStatusCode();
      errorCode = shException.getErrorCode();
      description = shException.getDescription();
    } else {
      statusCode = 500;
    }
    
    throw t;
 } finally {
    log.info("_message=\"API Result\" api=\"{}\" statusCode={} errorCode=\"{}\" description=\"{}\"" , "Get Listing", statusCode, errorCode, description);
 }
  }

  private void removeSeatStatus(ListingResponse listingResponse,String status){
    
    if(StringUtils.isBlank(status) ){
      List<Product> products = listingResponse.getProducts();
      for (Product product : products) {
        product.setSeatStatus(null);
      }
    }
  }
  private ExtendedSecurityContext getSecurityContext(SHServiceContext shServiceContext) {
    return shServiceContext.getExtendedSecurityContext();
  }

  public com.stubhub.domain.inventory.v2.DTO.ListingResponse listingPing() {
    ListingResponse response = new ListingResponse();

    response.setEventId("10000012");
    response.setProducts(new ArrayList<Product>());
    Product p = new Product();
    p.setSeatId(7823642783l);
    p.setOperation(Operation.ADD);
    p.setProductType(ProductType.TICKET);
    response.getProducts().add(p);
    response.setTicketTraits(new HashSet<TicketTrait>());
    TicketTrait t = new TicketTrait();
    t.setId("1231231");
    t.setOperation(Operation.ADD);;
    response.getTicketTraits().add(t);
    return response;
  }

  private String getSubscriber(ExtendedSecurityContext securityContext, String operatorId) {

    StringBuffer subscriber = new StringBuffer();

    if (StringUtils.isNotBlank(operatorId)) {
      subscriber.append(operatorId).append("|");
    }

    if(RELIST.equals(relistFlag.get())){
    	subscriber.append(RELIST_MARKER);    	
    }else{
     subscriber.append("Single|V2|");
    }

    if (securityContext != null) {
    	if (securityContext.getOperatorApp()!=null&&!securityContext.getOperatorApp().isEmpty()) {
            log.info("Operator is not null for userId={}, operator={}", securityContext.getUserId(), securityContext.getOperatorApp());
            subscriber.append(securityContext.getOperatorApp());
          }
    	else{
      Map<String, Object> extendedInfo = securityContext.getExtendedInfo();
      if (extendedInfo != null) {
         if (extendedInfo.get(SUBSCRIBER_URL) != null) {
          log.info("Operator is null.. Falling back to Subscriber for userId=" + securityContext.getUserId());
          subscriber.append((String) extendedInfo.get(SUBSCRIBER_URL));
        }
      }}

      List<String> companyNameApigee = context.getHttpHeaders().getRequestHeader("x-apigee-company-name");
      if(companyNameApigee != null && !companyNameApigee.isEmpty()) {
          subscriber.append("|" + companyNameApigee.get(0));
      }

      String appName = "";
      appName = securityContext.getApplicationName();
      subscriber.append("|" + appName);

        List<String> listingMode;
        listingMode = context.getHttpHeaders().getRequestHeader("x-bfs-listing-mode");
        if (listingMode != null && !listingMode.isEmpty()) {
            subscriber.append("-"+listingMode.get(0));
        }
    }
      MDC.put(APPLICATION, subscriber.toString());

    return subscriber.toString();
    
  }

    @Override
	  public RelistResponse relist(RelistRequest request, SHServiceContext shServiceContext, I18nServiceContext i18nServiceContext) {
    	
	    String sellerId = null;
	    String sellerGuid = null;
	    SHAPIContext apiContext = null;
	    String userGuid = null;
	    String userEmail = null;
	    ExtendedSecurityContext securityContext = getSecurityContext(shServiceContext);
	    // Get the seller id
	    if (securityContext != null) {
	      sellerId = securityContext.getUserId();
	      sellerGuid = securityContext.getUserGuid();
	      userEmail = securityContext.getUserName();
	    }
	    // Security check, if cannot get seller id return INVALID_CREDENTIAL error
	    if (sellerId == null) {
	      log.warn("api_domain=" + api_domain + " api_resource=" + api_resource
	          + " api_method=relistV2 status=error _message=\"Authentication error while creating listing\"");

	      throw new SHForbiddenException("Invalid security token");
	    }
	    RelistResponse relistResponse = null;
	    // Seller id ok, process relist
	    try {
	      log.info("_message=\"Relist listing for sellerId={}\"", sellerId);
	      
	      Map<Long, OrderDetailsV3DTO> orderDetailsMap = relistHelper.validateRelistListings(request);
	      
	      Map<Long, List<OrderItem>> ordersWithOrderItems =
	          relistHelper.validateListingWithOrderDetails(request, orderDetailsMap, i18nServiceContext.getLocale());
	     
	      List<ListingRequest> listingRequests =
	          relistHelper.createListingRequests(request, ordersWithOrderItems,orderDetailsMap);
	      
	      
	      for (RelistListing relistListing : request.getListings()) {
	    	  if(relistListing.getToEmailId()!=null){
	    		  if(userEmail != null && userEmail.equalsIgnoreCase(relistListing.getToEmailId())) {
	    			  log.error("api_domain=" + api_domain + " api_resource=" + api_resource
	    			          + " api_method=relistV2 status=error _message=\"Error - email same as the buyer email who is transferring\"");
	    			  SHRuntimeException shException = new SHBadRequestException("invalid customer emailId");
					  shException.setErrorCode("inventory.listings.invalidrequest.emailId");
					  throw shException;
	    		  }
	    		  userGuid = transferValidator.getUserGuid(relistListing.getToEmailId());
	    		  if(!StringUtils.isBlank(userGuid) && !userGuid.equals(sellerGuid)){
	    			  relistListing.setToCustomerGUID(userGuid);
	    			  continue; 
	    		  }else{
	    			  log.error("api_domain=" + api_domain + " api_resource=" + api_resource
	    			          + " api_method=relistV2 status=error _message=\"Error occured while validating toEmailId - email does not exist or same as the buyer email who is transferring\"");
	    			  SHRuntimeException shException = new SHBadRequestException("invalid customer emailId");
					  shException.setErrorCode("inventory.listings.invalidrequest.emailId");
					  throw shException;
	    		  }
	    	  }
	      }

	      List<ListingResponse> responses = new ArrayList<ListingResponse>();
	      
	      boolean isTransfer = false;

	      
	      relistFlag.set(RELIST);
	      for (ListingRequest listing : listingRequests) {
	    	  for (RelistListing relistListing : request.getListings()) {
	    		  if(relistListing.getToEmailId()!=null || relistListing.getToCustomerGUID()!=null){
	    			  					listing.setStatus(ListingStatus.HIDDEN);
	    			  					isTransfer = true;
	    			  				}
	    	  }
	        ListingResponse response = createListing(listing, shServiceContext, i18nServiceContext);
				for (RelistListing relistListing : request.getListings()) {
					if (relistListing.getToCustomerGUID() != null || relistListing.getToEmailId() != null) {
						List<OrderItem> orderItemList = ordersWithOrderItems.get(relistListing.getOrderId());
						List<TicketSeat> ticketList = listingHelper.getTicketSeatsInfoByTicketId(response.getId());
						Listing listingInfo = inventoryMgr.getListing(Long.parseLong(response.getId()));
						List<Map<String, String>> orderItemToSeatMap = new ArrayList<>();
							for (OrderItem orderItem : orderItemList) {
								for (TicketSeat ticketSeat : ticketList) {
									if (orderItem.getRow().equals(ticketSeat.getRow())
											&& orderItem.getSeat().equals(ticketSeat.getSeatNumber())) {
										Map<String, String> itemToSeatMap = new LinkedHashMap<>();
										itemToSeatMap.put("itemId", String.valueOf(orderItem.getSeatId()));
										itemToSeatMap.put("seatId", String.valueOf(ticketSeat.getTicketSeatId()));
										orderItemToSeatMap.add(itemToSeatMap);
										break;
									}
								}
							}
							//send to Lock queue if ticket medium is Barcode or Flashseat
							sendLockTransferMessage(listing, response, orderItemToSeatMap);

                        try {
                            //call gcp cloud transfer API
                            checkoutTransferAPIHelper.transferOrderToFriend(relistListing.getOrderId(), orderItemToSeatMap,
                                    relistListing.getToEmailId(), relistListing.getToCustomerGUID(), response.getId(),
                                    String.valueOf(listingInfo.getSellerPaymentTypeId()));
                        } catch (Exception ex) {
                            log.warn("gcp transfer api error, throw transfer message to active MQ: order_to_be_transfered=" + relistListing.getOrderId(), ex);
                            //throw to active mq as a fallback for now
                            jmsMessageHelper.sendShareWithFriendsMessage(relistListing.getOrderId(), orderItemToSeatMap,
                                    relistListing.getToEmailId(), relistListing.getToCustomerGUID(), response.getId(),
                                    String.valueOf(listingInfo.getSellerPaymentTypeId()));

                        }
					}
				}
	        
	        responses.add(response);
	      }
	      
	      relistHelper.cloneFileInfoIds(responses,ordersWithOrderItems,request, sellerGuid);
	      
	      relistHelper.addOriginalTicketSeatIds(responses,ordersWithOrderItems,request);
	      relistResponse = new RelistResponse();
	      for (ListingResponse response : responses) {
	        relistResponse.addListing(response.getId(), response.getStatus());
	      }
	      
	      StringBuilder sb = new StringBuilder("{\"orderId\" : \"").append(request.getListings().get(0).getOrderId()).append("\", \"message\" : \"").append("success").append("}");
	      log.info("api_domain=" + api_domain + " api_resource=" + api_resource
	              + " api_method={} status=success message={} relistResponse={}"
	              , isTransfer ? "transfer" : "relist", sb, relistResponse);

	    } catch (ListingBusinessException listingException) {
	    	StringBuilder sb = new StringBuilder("{\"orderId\" : \"").append(request.getListings().get(0).getOrderId()).append("\", \"message\" : \"").append(listingException.getMessage()).append("}");
		      log.warn("api_domain=" + api_domain + " api_resource=" + api_resource
		              + " api_method=relist/transfer status=error message={}"
		              , sb);

	      ListingResponseAdapter.errorMappingThrowException(listingException);
	    } catch (SHRuntimeException shException) {
	    	StringBuilder sb = new StringBuilder("{\"orderId\" : \"").append(request.getListings().get(0).getOrderId()).append("\", \"message\" : \"").append(shException.getDescription()).append("}");
		      log.warn("api_domain=" + api_domain + " api_resource=" + api_resource
		              + " api_method=relist/transfer status=error message={}"
		              , sb);
	      throw shException;
	    } catch (Exception e) {
	      log.error("api_domain=" + api_domain + " api_resource=" + api_resource
	          + " api_method=relistV2 status=error _message=\"System error occured while relisting listing \""
	          + " sellerId=" + sellerId, e);
	      throw new SHSystemException("Unable to relist a listing please re-try");
	    }finally{
	    	//clean up thread local.
	    	relistFlag.remove();
	    }

	    return relistResponse;

	  }

	private void sendLockTransferMessage(ListingRequest listing, ListingResponse response,
			List<Map<String, String>> orderItemToSeatMap) {
		if (TicketMedium.BARCODE.equals(listing.getTicketMedium())
				|| TicketMedium.FLASHSEAT.equals(listing.getTicketMedium())) {
			jmsMessageHelper.sendLockTransferMessage(response.getId(), true, orderItemToSeatMap);
		}
	}
    
	private boolean isAuthZRequest(SHServiceContext shServiceContext) {
		if (shServiceContext != null) {
			String role = shServiceContext.getRole();
			String operatorId = shServiceContext.getOperatorId();
			String proxiedId = shServiceContext.getProxiedId();
			if (StringUtils.isNotBlank(operatorId) && StringUtils.isNotBlank(proxiedId) && StringUtils.isNotBlank(role))
				return ("R2".compareToIgnoreCase(role) == 0) || ("R3".compareToIgnoreCase(role) == 0);
		}

		return false;
	}
	
	private boolean validateListingBlock(boolean isCreate, ListingRequest request, String sellerId) {
	  try {
	    boolean isDelete = false;
        boolean isPredelivery = false;
        if(request.getProducts() != null && !request.getProducts().isEmpty()) {
          if(StringUtils.trimToNull(request.getProducts().get(0).getFulfillmentArtifact()) != null) {
            isPredelivery = true;
          }
        }
        if(!isCreate) {
          if(ListingStatus.DELETED.equals(request.getStatus())) {
            isDelete = true;
          }
        }
        return listingControlHelper.isListingBlock(isCreate, isDelete, isPredelivery, sellerId);
      } catch (Exception e) {
        log.error("Exception occurred while evaluating listing blocks sellerId={}", sellerId, e);
      }
	  
	  return false;
	}
	
}


