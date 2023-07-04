package com.stubhub.domain.inventory.listings.v2.tasks;

import org.apache.log4j.Logger;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.infrastructure.common.core.concurrent.SHThreadLocals;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.util.PaymentHelper;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

public class PaymentTypeUpdateTask implements CallableInventoryTask<Listing>{
	
	
	private UserHelper userHelper;
	private PaymentHelper paymentHelper;
	
	private Listing listing;
	private Listing currentListing;
	private SHAPIContext apiContext;
	private SHServiceContext shServiceContext;
	private boolean isCreate;
	private final Map<String, String> context;
	private static final Map<String, String> EMPTY_CONTEXT = new HashMap<>();
	private final static Logger log = Logger.getLogger(PaymentTypeUpdateTask.class);
	
	public PaymentTypeUpdateTask(Listing listing, Listing currentListing, SHAPIContext apiContext, SHServiceContext shServiceContext, UserHelper userHelper, 
			PaymentHelper paymentHelper ) 
	{
		this (listing, currentListing, apiContext, shServiceContext, userHelper, paymentHelper, false);
	}

	
	public PaymentTypeUpdateTask(Listing listing, Listing currentListing, SHAPIContext apiContext, SHServiceContext shServiceContext, UserHelper userHelper, 
			PaymentHelper paymentHelper, boolean isCreate)
	{
		this.listing= listing;
		this.currentListing = currentListing;
		this.apiContext = apiContext;
		this.shServiceContext = shServiceContext;
		this.userHelper = userHelper;
		this.paymentHelper = paymentHelper;
		this.isCreate = isCreate;
		this.context = MDC.getCopyOfContextMap() != null ? MDC.getCopyOfContextMap() : EMPTY_CONTEXT;
	}
	
	public Listing call() throws Exception {
		MDC.setContextMap(this.context);
		log.debug("START async task");
		SHAPIThreadLocal.set(apiContext);
		SHThreadLocals.set(SHServiceContext.SERVICE_CONTEXT_HEADER, shServiceContext);
		if( !isCreate && listing.getSellerPaymentTypeId() != null){
			if(currentListing.getSellerPaymentTypeId() == null || !listing.getSellerPaymentTypeId().equals(currentListing.getSellerPaymentTypeId())){
				
				if(userHelper.isSellerPaymentTypeValid(currentListing.getSellerId(), currentListing.getEventId(), listing.getSellerPaymentTypeId())){
					currentListing.setSellerPaymentTypeId(listing.getSellerPaymentTypeId());
				}else{
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR, ErrorCode.INVALID_PAYMENT_TYPE, "paymentType not supported by event",
							"sellerPaymentTypeId");
					throw new ListingBusinessException(listingError);
				}
				if(!paymentHelper.isSellerPaymentTypeValidForSeller(listing.getSellerPaymentTypeId(), currentListing.getSellerId())){
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR, ErrorCode.INVALID_PAYMENT_TYPE, "paymentType not supported for seller",
							"sellerPaymentTypeId");
					throw new ListingBusinessException(listingError);
				
				}
			}
		}
		
		if ( isCreate ) {
			log.debug("START populatePaymentDetails task");
			paymentHelper.populatePaymentDetails(currentListing);
			log.debug("END populatePaymentDetails task");						
		}
		
		log.debug("END async task");
		return currentListing;
	 }

	@Override
	public boolean ifNeedToRunTask() {
		return listing.getSellerPaymentTypeId() != null || isCreate;
	}

}
