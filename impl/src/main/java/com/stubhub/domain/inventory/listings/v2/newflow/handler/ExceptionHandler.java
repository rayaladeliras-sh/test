package com.stubhub.domain.inventory.listings.v2.newflow.handler;

import org.springframework.stereotype.Component;

import com.stubhub.domain.infrastructure.common.exception.base.SHRuntimeException;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHBadRequestException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHForbiddenException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHResourceNotFoundException;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;

@Component
public class ExceptionHandler {

	private static final String ERROR_CODE_PREFIX = "inventory.listings.";

	public ListingResponse handle(Throwable t) {
		if (t instanceof ListingException) {
			throw handleListingException((ListingException) t);
		} else {
			throw new SHSystemException("Unable to process the request, please re-try", t);
		}
	}

	private SHRuntimeException handleListingException(ListingException listingException) {
		return getErrorMapping(listingException);
	}

	private SHRuntimeException getErrorMapping(ListingException listingException) {
		SHRuntimeException runtimeException = getMappedException(listingException);
		populateErrorDetails(runtimeException, listingException);
		return runtimeException;
	}

	private SHRuntimeException getMappedException(ListingException listingException) {
		switch (listingException.getType()) {
		case BUSINESSERROR:
		case INPUTERROR:
			return new SHBadRequestException(listingException);
		case AUTHENTICATIONERROR:
		case AUTHORIZATIONERROR:
			return new SHForbiddenException(listingException);
		case NOT_FOUND:
			return new SHResourceNotFoundException(listingException);
		case SYSTEMERROR:
		default:
			return new SHSystemException(listingException);
		}
	}

	private void populateErrorDetails(SHRuntimeException exception, ListingException listingException) {
		exception.setErrorCode(ERROR_CODE_PREFIX + listingException.getErrorCodeEnum().toString());

		// send custom message if available, otherwise standard message from enum 
		if(listingException.getCustomMessage() != null) {
			exception.setDescription(listingException.getCustomMessage());
		} else {
			exception.setDescription(listingException.getErrorCodeEnum().getDescription());
		}
	}
}
