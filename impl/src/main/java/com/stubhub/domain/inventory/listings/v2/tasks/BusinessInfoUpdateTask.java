package com.stubhub.domain.inventory.listings.v2.tasks;

import org.apache.log4j.Logger;

import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.util.SellerHelper;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

public class BusinessInfoUpdateTask implements CallableInventoryTask<Listing> {
	
	private Listing listing;
	private Listing currentListing;
	private SHAPIContext apiContext;
	private boolean isCreate = false;
	private SellerHelper sellerHelper;
	private final Map<String, String> context;

	private static final Map<String, String> EMPTY_CONTEXT = new HashMap<>();

	private final static Logger log = Logger.getLogger(BusinessInfoUpdateTask.class);
	
	public BusinessInfoUpdateTask(Listing listing, Listing currentListing, SellerHelper sellerHelper, SHAPIContext apiContext)
	{
		this(listing, currentListing, sellerHelper, apiContext, false);
	}
	
	public BusinessInfoUpdateTask(Listing listing, Listing currentListing, SellerHelper sellerHelper, SHAPIContext apiContext, boolean isCreate)
	{
		this.listing = listing;
		this.currentListing = currentListing;
		this.sellerHelper = sellerHelper;
		this.apiContext = apiContext;
		this.isCreate = isCreate;
		this.context = MDC.getCopyOfContextMap() != null ? MDC.getCopyOfContextMap() : EMPTY_CONTEXT;
	}

	@Override
	public Listing call() throws Exception {
		if (isCreate) {
			MDC.setContextMap(this.context);
			SHMonitor mon = SHMonitorFactory.getMonitor().start();
			
			SHAPIThreadLocal.set(apiContext);
			try {
				sellerHelper.addBusinessDetails(listing);
				
				log.debug("businessId=" + listing.getBusinessId() + " businessGuid=" + listing.getBusinessGuid());
				if (listing.getBusinessId() != null && listing.getBusinessGuid() != null) {
					currentListing.setBusinessId(listing.getBusinessId());
					currentListing.setBusinessGuid(listing.getBusinessGuid());
				}
				
				mon.stop();
				log.info(SHMonitoringContext.get() + " _operation=addBusinessDetails" + " _message=\"Customer Business Statuses Call Successful \" _status=OK" 
						+ " _respTime=" + mon.getTime() + " sellerGuid=" + listing.getSellerGuid());
				
			} catch (Exception e) {
				// Swallow the exception to allow the listing created
				mon.stop();
				log.warn(SHMonitoringContext.get() + " _operation=addBusinessDetails" + " _message=\"Exception occured while calling userBusinessStatus api.\" sellerGuid=" + listing.getSellerGuid());
			}
		}
		
		return currentListing;
	}

	@Override
	public boolean ifNeedToRunTask() {
		return isCreate;
	}

}
