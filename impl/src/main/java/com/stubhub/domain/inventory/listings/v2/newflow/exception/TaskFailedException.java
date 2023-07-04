package com.stubhub.domain.inventory.listings.v2.newflow.exception;

import com.stubhub.common.exception.ErrorType;

@SuppressWarnings("serial")
public class TaskFailedException extends ListingException {

	public TaskFailedException(ErrorType type, ErrorCodeEnum errorCodeEnum) {
		super(type, errorCodeEnum);
	}
}
