package com.stubhub.domain.inventory.listings.v2.newflow.validator;

import com.stubhub.domain.inventory.v2.DTO.ListingRequest;

public abstract class RequestValidator implements Validator {

	protected ListingRequest listingRequest;

	public RequestValidator(ListingRequest listingRequest) {
		this.listingRequest = listingRequest;
	}
}
