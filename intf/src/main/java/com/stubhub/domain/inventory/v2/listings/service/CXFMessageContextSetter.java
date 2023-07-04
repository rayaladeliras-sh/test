package com.stubhub.domain.inventory.v2.listings.service;

import javax.ws.rs.core.Context;

import org.apache.cxf.jaxrs.ext.MessageContext;

public interface CXFMessageContextSetter {
	@Context
	 void setMessageContext(MessageContext ctx);
	 

}


 
	 