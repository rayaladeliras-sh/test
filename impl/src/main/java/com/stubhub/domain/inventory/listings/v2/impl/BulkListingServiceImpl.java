/**
 * 
 */
package com.stubhub.domain.inventory.listings.v2.impl;

import java.util.Map;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHBadRequestException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHForbiddenException;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.enums.BulkStatus;
import com.stubhub.domain.inventory.listings.v2.adapter.ListingResponseAdapter;
import com.stubhub.domain.inventory.listings.v2.bulk.util.BulkListingHelper;
import com.stubhub.domain.inventory.listings.v2.helper.ListingCreateProcess;
import com.stubhub.domain.inventory.listings.v2.helper.PaymentEligibilityHelper;
import com.stubhub.domain.inventory.listings.v2.util.SellerHelper;
import com.stubhub.domain.inventory.listings.v2.validator.BulkCreateListingValidator;
import com.stubhub.domain.inventory.listings.v2.validator.InputValidator;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkJobResponse;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkJobStatusRequest;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingRequest;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingResponse;
import com.stubhub.domain.inventory.v2.listings.service.BulkListingService;
import com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;

/**
 * @author sjayaswal
 *
 */

@Component("bulkListing")
public class BulkListingServiceImpl implements BulkListingService {

	@Autowired
	private BulkListingHelper bulkListingHelper;
	
	@Context  
	private HttpHeaders httpHeaders;
	
	@Autowired
	private SellerHelper sellerHelper;

	@Autowired
	private PaymentEligibilityHelper paymentEligibilityHelper;
	
	@Autowired
	private ListingCreateProcess listingCreateProcess;
	
	private final static Logger log = LoggerFactory.getLogger(BulkListingServiceImpl.class);
	private static String api_domain = "inventory";
	private static String api_resource = "listing";
	
	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.v2.listings.intf.BulkListingService#createBulkListing(com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContext, com.stubhub.domain.inventory.v2.bulk.DTO.CreateBulkListingRequest)
	 */
	@Override
	public BulkListingResponse createBulkListing(
			BulkListingRequest createBulkListingRequest,
			SHServiceContext shServiceContext) {
		
		SHMonitor mon = SHMonitorFactory.getMonitor().start();

		String sellerId = null;
		SHAPIContext apiContext = null;
		String sellerGUID = null;
		ExtendedSecurityContext securityContext = getSecurityContext(shServiceContext);

		// Get the seller id
		if (securityContext != null) {
			apiContext = SHAPIThreadLocal.getAPIContext();
			sellerId = securityContext.getUserId();
			sellerGUID = securityContext.getUserGuid();
		}
			
		// Security check, if cannot get seller id return INVALID_CREDENTIAL error
		if ( sellerId == null ) {
			mon.stop();
			log.error(
					"{} _operation=createBulkListing _message=\"Bulk Job creation failed, invalid sellerID\" _status=CLIENT_ERROR _respTime={} ",
					SHMonitoringContext.get(), mon.getTime());
			throw new SHForbiddenException("Invalid security token");
		}

		BulkListingResponse bulkListingResponse = new BulkListingResponse();
		Long sellerIdLong = Long.valueOf(sellerId);
		String subscriber = getSubscriber(securityContext);
		try {

			// Check Seller Eligibility
			if (!paymentEligibilityHelper.isValidPaymentEligibility(sellerGUID, httpHeaders)) {
				ListingError listingError = new ListingError(
						ErrorType.BUSINESSERROR,
						ErrorCode.SELLER_NO_PI_INFO,
						"PI info is missing",
						"PI info is missing");
				throw new ListingBusinessException(listingError);
			}

			InputValidator validator = new InputValidator();
			validator.addValidator(new BulkCreateListingValidator(sellerIdLong,sellerHelper));
			validator.validate();
			
			if (validator.getErrors()!=null && validator.getErrors().size() > 0) {
				Long bulkJobId=bulkListingHelper.bulkCreateListing(sellerIdLong, sellerGUID, subscriber, BulkStatus.ERROR, createBulkListingRequest, "assertion", httpHeaders);
				bulkListingResponse.setJobGuid(bulkJobId); 
				bulkListingResponse.setErrors(validator.getErrors());
				mon.stop();
				log.error(
						"{} _operation=createBulkListing _message=\"Bulk Job creation failed, input validations failed \" _status=CLIENT_ERROR _respTime={} sellerId={} ",
						SHMonitoringContext.get(), mon.getTime(), securityContext.getUserId());
			}else{
				// real call
				Long bulkJobId=bulkListingHelper.bulkCreateListing(sellerIdLong, sellerGUID, subscriber, null, createBulkListingRequest, apiContext.getSignedJWTAssertion(), httpHeaders);
				bulkListingResponse.setJobGuid(bulkJobId); 
				mon.stop();
				log.info(
						"{} _operation=createBulkListing _message=\"Job created successfully \" _status=OK _respTime={} sellerId={} jobId={}",
						SHMonitoringContext.get(), mon.getTime(),
						securityContext.getUserId(), bulkJobId);
				
			}
		}catch(ListingBusinessException ex){
			mon.stop();
			log.error(
					"{} _operation=createBulkListing _message=\"Bulk Job creation failed, listing business exceptions \" _status=CLIENT_ERROR _respTime={} sellerId={} ",
					SHMonitoringContext.get(), mon.getTime(), securityContext.getUserId());
			ListingResponseAdapter.errorMappingThrowException(ex);
		}catch(Exception ex){
			mon.stop();
			log.error(
					"{} _operation=createBulkListing _message=\"Bulk Job creation failed, unknown system exceptions \" _status=SYSTEM_ERROR _respTime={} sellerId={} ",
					SHMonitoringContext.get(), mon.getTime(), securityContext.getUserId());
		}

		return bulkListingResponse;
	}

	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.v2.listings.intf.BulkListingService#getJobStatus(com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContext, java.lang.String)
	 */
	@Override
	public BulkJobResponse getJobStatus(String jobGuid, SHServiceContext shServiceContext) {
		SHMonitor mon = SHMonitorFactory.getMonitor().start();

		String sellerId = null;
		SHAPIContext apiContext = null;
		ExtendedSecurityContext securityContext = getSecurityContext(shServiceContext);

		// Get the seller id
		if (securityContext != null) {
			apiContext = SHAPIThreadLocal.getAPIContext();
			sellerId = securityContext.getUserId();
		}
			
		// Security check, if cannot get seller id return INVALID_CREDENTIAL error
		if ( sellerId == null ) {
			mon.stop();
			log.error("api_domain="
					+ api_domain
					+ " api_resource="
					+ api_resource
					+ " api_method=getJobStatus status=success_with_error message=\"Authentication error while geting job status\""
					+ " respTime={}",mon.getTime());
			
			throw new SHForbiddenException("Invalid security token");
		}
		
		BulkJobResponse response = new BulkJobResponse();
		
		Long jobId = null;
		try{
			jobId = Long.valueOf(jobGuid);
		}
		catch(NumberFormatException e){
			SHBadRequestException sbe = new SHBadRequestException();
			sbe.setErrorCode("inventory.listings.invalidJobGuid");
			sbe.setDescription("jobGuid passed in is not a valid number");
			throw sbe;
		}
		
		Long sellerIdLong = Long.valueOf(sellerId);
		try{
			response = bulkListingHelper.getJobStatuses(sellerIdLong, jobId);
		}
		catch(ListingBusinessException ex){
			mon.stop();
			log.error(
					"api_domain="
							+ api_domain
							+ " api_resource="
							+ api_resource
							+ " api_method=getJobStatuses status=success_with_error _message=\"ListingBusinessException occured while getting job statuses\""
							+ " sellerId={} respTime=", sellerIdLong , mon.getTime(), ex);
			ListingResponseAdapter.errorMappingThrowException(ex);
		}
		
		mon.stop();
		return response;
	}

	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.v2.listings.intf.BulkListingService#updateBulkListing(com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContext, com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingRequest)
	 */
	@Override
	public BulkListingResponse updateBulkListing(
			BulkListingRequest updateListingRequest,
			SHServiceContext shServiceContext) {
		SHMonitor mon = SHMonitorFactory.getMonitor().start();

		String sellerId = null;
		SHAPIContext apiContext = null;
		String sellerGUID = null;
		ExtendedSecurityContext securityContext = getSecurityContext(shServiceContext);

		// Get the seller id
		if (securityContext != null) {
			apiContext = SHAPIThreadLocal.getAPIContext();
			sellerId = securityContext.getUserId();
			sellerGUID = securityContext.getUserGuid();
		}
			
		// Security check, if cannot get seller id return INVALID_CREDENTIAL error
		if ( sellerId == null ) {
			mon.stop();
			log.error("api_domain="
					+ api_domain
					+ " api_resource="
					+ api_resource
					+ " api_method=updateBulkListing status=success_with_error message=\"Authentication error while update bulk listing\""
					+ " respTime={}",mon.getTime());
			
			throw new SHForbiddenException("Invalid security token");
		}
	
		BulkListingResponse bulkListingResponse = new BulkListingResponse();
		Long sellerIdLong = Long.valueOf(sellerId);
		String subscriber = getSubscriber(securityContext);
		try {
			InputValidator validator = new InputValidator();
			validator.addValidator(new BulkCreateListingValidator(sellerIdLong,sellerHelper));
			validator.validate();
			
			if (validator.getErrors()!=null && validator.getErrors().size() > 0) {
				Long bulkJobId=bulkListingHelper.bulkUpdateListing(sellerIdLong, sellerGUID, subscriber, BulkStatus.ERROR, updateListingRequest, apiContext.getSignedJWTAssertion(), httpHeaders);
				bulkListingResponse.setJobGuid(bulkJobId); 
				bulkListingResponse.setErrors(validator.getErrors());
			}else{
				Long bulkJobId=bulkListingHelper.bulkUpdateListing(sellerIdLong, sellerGUID, subscriber, null, updateListingRequest, apiContext.getSignedJWTAssertion(), httpHeaders);
				bulkListingResponse.setJobGuid(bulkJobId);
				mon.stop();
				log.info(
						"{} _operation=updateBulkListing _message=\"Job created successfully \" _status=OK _respTime={} sellerId={} jobId={}",
						SHMonitoringContext.get(), mon.getTime(),
						securityContext.getUserId(), bulkJobId);
				
			}
		}catch(ListingBusinessException ex){
			mon.stop();
			log.error(
					"api_domain="
							+ api_domain
							+ " api_resource="
							+ api_resource
							+ " api_method=updateBulkListing status=success_with_error _message=\"ListingBusinessException occured while update bulk listing\""
							+ " sellerId={} respTime=", sellerIdLong , mon.getTime(), ex);
			ListingResponseAdapter.errorMappingThrowException(ex);
		}
		
		mon.stop();
		return bulkListingResponse;
	}
	
	@Override
	public BulkJobResponse updateJobStatus(String jobGuid, BulkJobStatusRequest jobStatusRequest, SHServiceContext shServiceContext) {
	  SHMonitor mon = SHMonitorFactory.getMonitor().start();
	  String sellerId = null;
	  ExtendedSecurityContext securityContext = getSecurityContext(shServiceContext);
	  if(securityContext != null) {
	    sellerId = securityContext.getUserId();
	  }
	  
	  if (sellerId == null) {
	    mon.stop();
        log.error("api_domain=" + api_domain + " api_resource=" + api_resource + " api_method=updateJobStatus"
                + " status=error message=\"Authentication error while updating job status\""
                + " respTime={}", mon.getTime());
        
        throw new SHForbiddenException("Invalid security token");
      }
	  
	  Long jobId = null;
      try {
        jobId = Long.valueOf(jobGuid);
      }
      catch(NumberFormatException e) {
        SHBadRequestException sbe = new SHBadRequestException();
        sbe.setErrorCode("inventory.listings.invalidJobGuid");
        sbe.setDescription("jobGuid is not a valid number");
        throw sbe;
      }
      BulkJobResponse response = new BulkJobResponse();
      try {
        response = bulkListingHelper.updateJobStatus(Long.valueOf(sellerId), jobId, jobStatusRequest);
      } catch (ListingBusinessException ex) {
        mon.stop();
        log.error("api_domain=" + api_domain + " api_resource=" + api_resource + " api_method=updateJobStatus status=success_with_error"
            + " message=\"ListingBusinessException occurred while updating job statuses\""
            + " sellerId={} respTime={}", sellerId , mon.getTime(), ex);
        ListingResponseAdapter.errorMappingThrowException(ex);
      } catch (Exception e) {
        mon.stop();
        log.error("api_domain=" + api_domain + " api_resource=" + api_resource + " api_method=updateJobStatus status=error"
            + " message=\"Exception occurred while updating job statuses\""
            + " sellerId={} respTime={}", sellerId , mon.getTime(), e);
        throw new SHSystemException("System error occurred");
      }
	  
      mon.stop();
      return response;

	}
	
	private ExtendedSecurityContext getSecurityContext(SHServiceContext shServiceContext){
		return shServiceContext.getExtendedSecurityContext();
	}
	
	private String getSubscriber(ExtendedSecurityContext securityContext) {
		StringBuffer subscriber = new StringBuffer("Bulk|V2|");
		if (securityContext != null) {
			if (securityContext.getOperatorApp() != null && !securityContext.getOperatorApp().isEmpty()) {
				log.info("Operator is not null for userId={}, operator={}", securityContext.getUserId(),
						securityContext.getOperatorApp());
				subscriber.append(securityContext.getOperatorApp());
			} else {
				Map<String, Object> extendedInfo = securityContext.getExtendedInfo();
				if (extendedInfo != null) {
					if (extendedInfo.get("http://stubhub.com/claims/subscriber") != null) {
						subscriber.append((String) extendedInfo.get("http://stubhub.com/claims/subscriber"));
					}
				}
			}
			String appName = "";
			appName = securityContext.getApplicationName();
			subscriber.append("|" + appName);
		}
		return subscriber.toString();
	}

}
