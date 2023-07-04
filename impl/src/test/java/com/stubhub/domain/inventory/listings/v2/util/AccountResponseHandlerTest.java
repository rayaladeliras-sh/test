package com.stubhub.domain.inventory.listings.v2.util;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.listings.v2.util.InventoryResponseHandler;

public class AccountResponseHandlerTest {
	@Test
	 public void testHandleResponse_InputErr() {
		InventoryResponseHandler inventoryResponseHandler = new InventoryResponseHandler();
		com.stubhub.domain.inventory.common.util.Response responseErr = new com.stubhub.domain.inventory.common.util.Response();
		List<com.stubhub.domain.inventory.common.util.ListingError> errors = new ArrayList<com.stubhub.domain.inventory.common.util.ListingError>();
		com.stubhub.domain.inventory.common.util.ListingError error = new com.stubhub.domain.inventory.common.util.ListingError();
		error.setType(ErrorType.INPUTERROR);
		errors.add(error);
		responseErr.setErrors(errors);		
		Response resp = Response.status(Response.Status.OK).entity(responseErr).build();
		Response retResp = inventoryResponseHandler.handleResponse(null, null, resp);
		Assert.assertNotNull(retResp);
	}
	
	@Test
	 public void testHandleResponse_BusinessErr() {
		InventoryResponseHandler inventoryResponseHandler = new InventoryResponseHandler();
		com.stubhub.domain.inventory.common.util.Response responseErr = new com.stubhub.domain.inventory.common.util.Response();
		List<com.stubhub.domain.inventory.common.util.ListingError> errors = new ArrayList<com.stubhub.domain.inventory.common.util.ListingError>();
		com.stubhub.domain.inventory.common.util.ListingError error = new com.stubhub.domain.inventory.common.util.ListingError();
		error.setType(ErrorType.BUSINESSERROR);
		errors.add(error);
		responseErr.setErrors(errors);		
		Response resp = Response.status(Response.Status.OK).entity(responseErr).build();
		Response retResp = inventoryResponseHandler.handleResponse(null, null, resp);
		Assert.assertNotNull(retResp);
	}
	
	@Test
	 public void testHandleResponse_AuthErr() {
		InventoryResponseHandler inventoryResponseHandler = new InventoryResponseHandler();
		com.stubhub.domain.inventory.common.util.Response responseErr = new com.stubhub.domain.inventory.common.util.Response();
		List<com.stubhub.domain.inventory.common.util.ListingError> errors = new ArrayList<com.stubhub.domain.inventory.common.util.ListingError>();
		com.stubhub.domain.inventory.common.util.ListingError error = new com.stubhub.domain.inventory.common.util.ListingError();
		error.setType(ErrorType.AUTHENTICATIONERROR);
		errors.add(error);
		responseErr.setErrors(errors);		
		Response resp = Response.status(Response.Status.OK).entity(responseErr).build();
		Response retResp = inventoryResponseHandler.handleResponse(null, null, resp);
		Assert.assertNotNull(retResp);
	}
	
	@Test
	 public void testHandleResponse_SysErr() {
		InventoryResponseHandler inventoryResponseHandler = new InventoryResponseHandler();
		com.stubhub.domain.inventory.common.util.Response responseErr = new com.stubhub.domain.inventory.common.util.Response();
		List<com.stubhub.domain.inventory.common.util.ListingError> errors = new ArrayList<com.stubhub.domain.inventory.common.util.ListingError>();
		com.stubhub.domain.inventory.common.util.ListingError error = new com.stubhub.domain.inventory.common.util.ListingError();
		error.setType(ErrorType.SYSTEMERROR);
		errors.add(error);
		responseErr.setErrors(errors);		
		Response resp = Response.status(Response.Status.OK).entity(responseErr).build();
		Response retResp = inventoryResponseHandler.handleResponse(null, null, resp);
		Assert.assertNotNull(retResp);
	}

}
