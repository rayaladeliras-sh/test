package com.stubhub.domain.inventory.listings.v2.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.listings.v2.util.PrimaryIntegrationUtil;
import com.stubhub.domain.inventory.listings.v2.util.SeatProductsContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import org.slf4j.MDC;

public class VerifySthInventoryTask implements CallableInventoryTask<List<SeatProductsContext>> {
	private SHAPIContext apiContext;
	private PrimaryIntegrationUtil primaryIntegrationUtil;
	private List<SeatProductsContext> seatProdContexts;
	private final Map<String, String> context;
	private static final Map<String, String> EMPTY_CONTEXT = new HashMap<>();

	private final static Logger log = Logger.getLogger(VerifySthInventoryTask.class);
  
	public VerifySthInventoryTask(List<SeatProductsContext> seatProdContexts, SHAPIContext apiContext, PrimaryIntegrationUtil primaryIntegrationUtil) {
		this.apiContext = apiContext;
		this.primaryIntegrationUtil = primaryIntegrationUtil;
		this.seatProdContexts = seatProdContexts;
		this.context = MDC.getCopyOfContextMap() != null ? MDC.getCopyOfContextMap() : EMPTY_CONTEXT;
	}

	@Override
	public List<SeatProductsContext> call() throws Exception {
		MDC.setContextMap(this.context);
		log.debug("START verifySthInventory task");
		
		String error = primaryIntegrationUtil.verifySthInventory(seatProdContexts);
		if (error != null) {					
			ListingError listingError = new ListingError(ErrorType.INPUTERROR,
					ErrorCode.INVALID_STH_INVENTORY, error, "FulfillmentArtifact");
			log.debug("END verifySthInventory task with exception");
			throw new ListingBusinessException(listingError);
		}
		
		log.debug("END verifySthInventory task");
		return seatProdContexts;
	}

	@Override
	public boolean ifNeedToRunTask() {

		return seatProdContexts != null && seatProdContexts.size() > 0;
	}
}
