package com.stubhub.domain.inventory.listings.v2.util;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.ext.ResponseHandler;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Message;
import org.springframework.stereotype.Component;


import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.util.ListingError;

@Component("inventoryResponseHandler")
public class InventoryResponseHandler implements ResponseHandler {
	
	@Override
	public Response handleResponse(Message message, OperationResourceInfo arg1, Response response) {
		Object retEntity = response.getEntity();
		if (retEntity != null) {
			if (retEntity instanceof com.stubhub.domain.inventory.common.util.Response) {
				if ((((com.stubhub.domain.inventory.common.util.Response) retEntity).getErrors() != null)
						&& (((com.stubhub.domain.inventory.common.util.Response) retEntity).getErrors().size() > 0)) {
					ListingError error = ((com.stubhub.domain.inventory.common.util.Response) retEntity).getErrors().get(0);
					ResponseBuilder responseBuilder = Response.fromResponse(response);
					if(error.getType()== ErrorType.INPUTERROR){
						responseBuilder.status(Status.BAD_REQUEST);
					}else if(error.getType()== ErrorType.NOT_FOUND){
						responseBuilder.status(Status.NOT_FOUND);
					}else if(error.getType()== ErrorType.BUSINESSERROR){
						responseBuilder.status(Status.CONFLICT);
					}else if(error.getType()== ErrorType.AUTHENTICATIONERROR){
						responseBuilder.status(Status.FORBIDDEN);
					}else if(error.getType()== ErrorType.AUTHORIZATIONERROR){
						responseBuilder.status(Status.UNAUTHORIZED);
					}
					else{
						responseBuilder.status(Status.INTERNAL_SERVER_ERROR);
					}
					return responseBuilder.build();
				}
			}
		}
		return response;
	}
	
}
