package com.stubhub.domain.inventory.listings.v2.newflow.exception;

import com.stubhub.common.exception.ErrorType;

@SuppressWarnings("serial")
public class RequestValidationException extends ListingException {

	public RequestValidationException(ErrorType type, ErrorCodeEnum errorCodeEnum) {
		super(type, errorCodeEnum);
	}
	
	public RequestValidationException(ErrorType type, ErrorCodeEnum errorCodeEnum, String customMessage) {
      super(type, errorCodeEnum, customMessage) ;
  }
}
