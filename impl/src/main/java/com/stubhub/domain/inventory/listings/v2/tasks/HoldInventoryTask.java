package com.stubhub.domain.inventory.listings.v2.tasks;

import org.apache.log4j.Logger;

import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.listings.v2.controller.helper.IntegrationHelper;
import com.stubhub.domain.inventory.listings.v2.controller.helper.ListingHolder;
import com.stubhub.domain.user.services.customers.v2.intf.GetCustomerResponse;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

public class HoldInventoryTask implements CallableInventoryTask<ListingHolder> {
	private ListingHolder listingHolder;
	private IntegrationHelper integrationHelper;
	private SHAPIContext shapiContext;

	private final Map<String, String> context;
	private final static Logger log = Logger.getLogger(HoldInventoryTask.class);
	private static final Map<String, String> EMPTY_CONTEXT = new HashMap<>();

	public HoldInventoryTask(ListingHolder listing, IntegrationHelper integrationHelper,SHAPIContext shapiContext)
	{
		this.listingHolder = listing;		
		this.integrationHelper = integrationHelper;
		this.shapiContext = shapiContext;
		this.context = MDC.getCopyOfContextMap() != null ? MDC.getCopyOfContextMap() : EMPTY_CONTEXT;
	}

	@Override
	public ListingHolder call() throws Exception {
		MDC.setContextMap(this.context);
		SHAPIThreadLocal.set(shapiContext);
		SHMonitor getShipCustomerPerfMonitor = SHMonitorFactory.getMonitor().start();
		GetCustomerResponse	customerDetails = integrationHelper.getShipCustomer(listingHolder.getListing().getSellerId());
		getShipCustomerPerfMonitor.stop();

		if((customerDetails != null && customerDetails.getShipOrderINTOptIn() != null && customerDetails.getShipOrderINTOptIn().longValue() == 1L) 
				|| (listingHolder.getListing().getIsETicket() != null && listingHolder.getListing().getIsETicket())) {
			listingHolder.setExternal(true);
			
			//TODO change the orderid and requestkey to pick from listing.
			SHMonitor reserveInventoryPerfMonitor = SHMonitorFactory.getMonitor().start();

			boolean shipResponse = integrationHelper.reserveInventory(listingHolder, customerDetails);
			listingHolder.setExternalCallSuccess(shipResponse);
			
			reserveInventoryPerfMonitor.stop();
		}
		
		return listingHolder;
	}

	@Override
	public boolean ifNeedToRunTask() {
		return true;
	}
}
