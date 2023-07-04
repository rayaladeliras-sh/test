package com.stubhub.domain.inventory.listings.v2.newflow.task;

import java.util.concurrent.Callable;

import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;

public abstract class ParallelTask extends RegularTask implements Callable<ListingDTO>{
	protected SHAPIContext apiContext;

	public ParallelTask(ListingDTO dto, SHAPIContext apiContext) {
		super(dto);
		this.apiContext = apiContext;
	}
	
	@Override
	public ListingDTO call(){
		SHAPIThreadLocal.set(apiContext);
		return super.call();
	}
}
