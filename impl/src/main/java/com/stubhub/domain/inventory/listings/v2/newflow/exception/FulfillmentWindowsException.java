package com.stubhub.domain.inventory.listings.v2.newflow.exception;

import com.stubhub.common.exception.ErrorType;

@SuppressWarnings("serial")
public class FulfillmentWindowsException extends ListingException {

	public FulfillmentWindowsException(ErrorType type, ErrorCodeEnum errorCodeEnum) {
		super(type, errorCodeEnum);
	}
}
