package com.stubhub.domain.inventory.listings.v2.newflow.task;

import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

public abstract class RegularTask implements Task<ListingDTO> {

	protected ListingDTO listingDTO;

	private final Map<String, String> context;
	private static final Map<String, String> EMPTY_CONTEXT = new HashMap<>();

	public RegularTask(ListingDTO dto) {
		this.listingDTO = dto;
		this.context = MDC.getCopyOfContextMap() != null ? MDC.getCopyOfContextMap() : EMPTY_CONTEXT;
	}

	@Override
	public ListingDTO call() {
		MDC.setContextMap(this.context);
		preExecute();
		execute();
		postExecute();

		return listingDTO;
	}

	protected abstract void preExecute();

	protected abstract void execute();

	protected abstract void postExecute();

	@Override
	public int hashCode() {
		return this.getClass().getName().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return (object != null && this.getClass().getName() == object.getClass().getName());
	}
}
