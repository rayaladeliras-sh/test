/**
 * 
 */
package com.stubhub.domain.inventory.v2.listings.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkJobResponse;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkJobStatusRequest;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingRequest;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingResponse;

/**
 * @author sjayaswal
 *
 */
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Path("/bulk")
public interface BulkListingService {

	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@POST
	public BulkListingResponse createBulkListing(
			BulkListingRequest createBulkListingRequest,
			@Context SHServiceContext shServiceContext);
	
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@PUT
	public BulkListingResponse updateBulkListing(
	        BulkListingRequest updateListingRequest,
	        @Context SHServiceContext shServiceContext);
	

	@Path("/{jobGuid}")
	@GET
	public BulkJobResponse getJobStatus(@PathParam("jobGuid") String jobGuid,
			@Context SHServiceContext shServiceContext);
	
	@Path("/{jobGuid}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @PUT
	public BulkJobResponse updateJobStatus(@PathParam("jobGuid") String jobGuid,
	        BulkJobStatusRequest jobStatusRequest,
	        @Context SHServiceContext shServiceContext);

}
