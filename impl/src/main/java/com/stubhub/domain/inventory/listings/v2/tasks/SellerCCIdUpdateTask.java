package com.stubhub.domain.inventory.listings.v2.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.domain.user.payments.intf.CustomerPaymentInstrumentDetails;
import com.stubhub.domain.user.payments.v2.intf.CustomerPaymentInstrumentDetailsV2;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;
import org.slf4j.MDC;

public class SellerCCIdUpdateTask implements CallableInventoryTask<Listing> 
{
	private UserHelper userHelper;	
	private Listing listing;
	private Listing currentListing;
	private SHAPIContext apiContext;
	private final Map<String, String> context;
	private static final Map<String, String> EMPTY_CONTEXT = new HashMap<>();
	private final static Logger log = Logger.getLogger(SellerCCIdUpdateTask.class);
	public SellerCCIdUpdateTask(Listing listing, Listing currentListing, SHAPIContext apiContext, UserHelper userHelper){
		this.listing= listing;
		this.currentListing = currentListing;
		this.userHelper = userHelper;
		this.apiContext = apiContext;
		this.context = MDC.getCopyOfContextMap() != null ? MDC.getCopyOfContextMap() : EMPTY_CONTEXT;
	}	
	
	public Listing call() throws ListingBusinessException {
		MDC.setContextMap(this.context);
		log.debug("START async task");
		if(listing.getCcGuid() != null){			
			SHAPIThreadLocal.set(apiContext);
			List<CustomerPaymentInstrumentDetailsV2> sellerPaymentInstruments = currentListing.getAllsellerPaymentInstrumentsV2();
			if(sellerPaymentInstruments == null){
				sellerPaymentInstruments= userHelper.getAllSellerPaymentInstrumentV2(listing.getSellerGuid());
				currentListing.setAllsellerPaymentInstrumentsV2(sellerPaymentInstruments);
			}
			Long mappedCCId = userHelper.getMappedValidSellerCCId(listing.getSellerGuid(), listing.getCcGuid(),sellerPaymentInstruments,listing.getSystemStatus());			
			if(mappedCCId != null){
				currentListing.setSellerCCId(mappedCCId);
			}else{
				ListingError listingError = new ListingError(
						ErrorType.BUSINESSERROR, ErrorCode.INVALID_CCID, "Listing ccGuid not matched with any paymentInstruments",
						"sellerCCId");
				throw new ListingBusinessException(listingError);
			}			
		}
		log.debug("END async task");
		return currentListing;
	 }

	@Override
	public boolean ifNeedToRunTask() {
		return listing.getSellerCCId() != null || listing.getCcGuid() != null;
	}

}
