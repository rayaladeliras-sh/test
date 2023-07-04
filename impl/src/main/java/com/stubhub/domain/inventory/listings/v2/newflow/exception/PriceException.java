package com.stubhub.domain.inventory.listings.v2.newflow.exception;

import com.stubhub.common.exception.ErrorType;

@SuppressWarnings("serial")
public class PriceException extends ListingException {

	public PriceException(ErrorType type, ErrorCodeEnum errorCodeEnum) {
		super(type, errorCodeEnum);
	}

	public PriceException(ErrorType type, ErrorCodeEnum errorCodeEnum, String customMessage){
		super(type, errorCodeEnum, customMessage);
	}
}
