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

import com.stubhub.domain.i18n.infra.soa.core.I18nServiceContext;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.v2.DTO.RelistRequest;
import com.stubhub.domain.inventory.v2.DTO.RelistResponse;

@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public interface ListingService {

  /**
   * update listing
   */
  @PUT
  @Path("/{listingId}")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  ListingResponse updateListing(@PathParam("listingId") String listingId,
      ListingRequest listingRequest, @Context SHServiceContext shServiceContext, @Context I18nServiceContext i18nServiceContext);

  /**
   * create a listing
   */
  @POST
  @Path("/")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  ListingResponse createListing(ListingRequest request, @Context SHServiceContext shServiceContext, @Context I18nServiceContext i18nServiceContext);


  /**TODO update the wiki
   * https://wiki.stubcorp.com/display/api/Re-List+tickets+from+order+-+V2%2C+support+for+multiple+listings+-+WIP
   * 
   * Accepts multiple listings and creates listings querying information from the order.
   * @param <RelistRquest>request with multiple listings
   * @param securityContext
   * @return list of Listings with id and status.
   */
  @POST
  @Path("/relist")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  public RelistResponse relist(RelistRequest request, @Context SHServiceContext securityContext, @Context I18nServiceContext i18nServiceContext);

  /**
   * Get listing
   */
  @GET
  @Path("/{listingId}")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  ListingResponse getListing(@PathParam("listingId") String listingId,
      @Context SHServiceContext shServiceContext, @Context I18nServiceContext i18nServiceContext);

  @GET
  @Path("/ping")
  @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  ListingResponse listingPing();
  
}
