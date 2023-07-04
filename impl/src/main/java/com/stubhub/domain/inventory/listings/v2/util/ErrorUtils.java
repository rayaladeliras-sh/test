package com.stubhub.domain.inventory.listings.v2.util;

import java.util.ArrayList;
import java.util.List;

import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;

public class ErrorUtils 
{
	/**
	 * Helper method that return a list of responses object with specific error
	 * @param requests
	 * @param error
	 * @return List<ListingResponse>
	 */
	public static List<ListingResponse> responsesFromError (List<ListingRequest> requests, ListingError error)
	{
		List<ListingResponse> responses = new ArrayList<ListingResponse> (requests.size());
		for ( ListingRequest req : requests ) {
			if ( error.getCode() != null ) {
				error.setErrorCode( getFormattedErrorCode(error.getCode()) );
			}
			responses.add ( respFromError (req.getExternalListingId(),  error, req.getListingId()) );
		}
		return responses;
	}
	
	/**
	 * Helper method that return resp object from ONE error
	 * @param listing
	 * @param error
	 * @return ListingResponse
	 */
	public static ListingResponse respFromError (Listing listing, ListingError error)
	{
		ListingResponse resp = new com.stubhub.domain.inventory.v2.DTO.ListingResponse();
		ArrayList<ListingError> el = new ArrayList<ListingError>();
		el.add ( error );
		if ( error.getCode() != null ) {
			error.setErrorCode( getFormattedErrorCode(error.getCode()) );
		}
		resp.setErrors(el);
		if ( listing != null ) {
			resp.setExternalListingId(listing.getExternalId());
			if(listing.getId() !=null)
				resp.setId(listing.getId().toString());
			//SELLAPI-1333 10/1/15 START
			else 
				resp.setId("E:" + listing.getExternalId());
			//SELLAPI-1333 10/1/15 END
		}
		return resp;
	}
	
	/**
	 * Helper method that return resp object from ONE error
	 * @param listing
	 * @param error
	 * @return ListingResponse
	 */
	public static void populateRespWithErrors (ListingResponse resp, String externalListingId,  List<ListingError> errors, Long listingId)
	{
		for ( ListingError err :  errors) {
			if ( err.getCode() != null ) {
				err.setErrorCode( getFormattedErrorCode (err.getCode()) );
			}
		}
		resp.setErrors(errors);
		resp.setExternalListingId(externalListingId);
		if(listingId !=null) {
			resp.setId(listingId.toString());
			//SELLAPI-1333 10/1/15 START
		} else { 
			resp.setId("E:" + externalListingId);
		//SELLAPI-1333 10/1/15 END
		}
	}	
	
	/**
	 * Helper method that return resp object from ONE error
	 * @param listing
	 * @param error
	 * @return ListingResponse
	 */
	private static ListingResponse respFromError (String externalId, ListingError error, Long listingId)
	{
		ListingResponse resp = new com.stubhub.domain.inventory.v2.DTO.ListingResponse();
		ArrayList<ListingError> el = new ArrayList<ListingError>();
		el.add ( error );
		resp.setErrors(el);
		if ( externalId != null ) {
			resp.setExternalListingId(externalId);
		}
		if(listingId !=null) {
			resp.setId(listingId.toString());
		//SELLAPI-1333 10/1/15 START
		} else { 
			resp.setId("E:" + externalId);
		//SELLAPI-1333 10/1/15 END
		}
		return resp;
	}
	
	/**
	 * Gets the formatted error code for error results
	 * @param code
	 * @return error code
	 */
	public static String getFormattedErrorCode (ErrorCode code) 
	{
		String str = code.name();
		int strLen = str.length();
		StringBuffer buffer = new StringBuffer(strLen);
		buffer.append("inventory.listings.");
		boolean capitalizeNext = false;
		for (int i = 0; i < strLen; i++) {
			char ch = str.charAt(i);
			if (ch == '_') {
				capitalizeNext = true;
			} else if (capitalizeNext) {
				buffer.append(Character.toTitleCase(ch));
				capitalizeNext = false;
			} else {
				buffer.append(Character.toLowerCase(ch));
			}
		}
		return buffer.toString();
	}
}
