package com.stubhub.domain.inventory.listings.v2.validator;

import java.util.List;

import com.stubhub.domain.inventory.common.util.ListingError;

public interface Validator {

	public void validate();
	
	public List<ListingError> getErrors();
}
