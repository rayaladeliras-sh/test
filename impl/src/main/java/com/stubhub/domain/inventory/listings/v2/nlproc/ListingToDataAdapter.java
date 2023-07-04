package com.stubhub.domain.inventory.listings.v2.nlproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.adapter.ListingRequestAdapter;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.DTO.TicketTrait;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;

public class ListingToDataAdapter 
{
	private final static Logger log = LoggerFactory.getLogger(ListingToDataAdapter.class);
	
	private static final List<String> FLS_TRAIT_IDS = Arrays.asList("13688", "13701", "14911", "14907");
	
	/**
	 * Create ListingData for update listing (either bulk or single)
	 * @param inventoryMgr
	 * @param sellerId
	 * @param sellerGuid
	 * @param ctx
	 * @param requests
	 * @param isBulk
	 * @return
	 */
	public ListingData listingDataFromUpdateRequests (InventoryMgr inventoryMgr, Long sellerId, String sellerGuid, SHAPIContext ctx, SHServiceContext shServiceContext,
	        List <ListingRequest> requests, boolean isBulk )
	{
		return listingDataFromRequests(inventoryMgr, sellerId, sellerGuid, ctx, shServiceContext, requests, false, isBulk);
	}
	
	/**
	 * Create ListingData for create listing (either bulk or single)
	 * @param sellerId
	 * @param sellerGuid
	 * @param ctx
	 * @param requests
	 * @param isBulk
	 * @return
	 */
	public ListingData listingDataFromCreateRequests (Long sellerId, String sellerGuid, SHAPIContext ctx, SHServiceContext shServiceContext,
			List <ListingRequest> requests, boolean isBulk )
	{
		return listingDataFromRequests(null, sellerId, sellerGuid, ctx, shServiceContext, requests, true, isBulk);
	}
	
	/**
	 * listingDataFromRequests fpr update, create or bulk 
	 * @param sellerId
	 * @param sellerGuid
	 * @param ctx
	 * @param requests
	 * @param isCreate
	 * @param isBulk
	 * @return ListingData
	 */
	private ListingData listingDataFromRequests (InventoryMgr inventoryMgr, Long sellerId, String sellerGuid, SHAPIContext ctx, SHServiceContext shServiceContext,
			List <ListingRequest> requests, boolean isCreate, boolean isBulk)
	{
		if ( requests == null || requests.size() == 0 )
			return null;
		
		// create a request and use as header for common values
		ListingRequest requestHeader = new ListingRequest();

		// listings body
		List<ListingRequest> listingRequests = requests;
		
		// listing mdata object
		ListingData ldata = new ListingData (isCreate, isBulk, sellerId, sellerGuid);
		 
		// set context
		ldata.setApiContext( ctx );
		ldata.setShServiceContext(shServiceContext);
		
		for ( int i=0; i<listingRequests.size(); i++ ) {
			
			ListingRequest req = listingRequests.get(i);
			process ( ldata, req, requestHeader);
			
			// add evey req to ldata object
			ldata.addRequest(req);
		}
		
		// get hdr listing
		Listing headerListing = getListingFromHeader ( requestHeader, isCreate);
		Long sellerContactId = null;
		String sellerContactGuid  = null;
		
		// only for bulk and create make sure the event is there!
		if ( headerListing.getEventId()==null && requestHeader.getEvent()==null &&  isCreate && isBulk) {
			ldata.addListingError(new ListingError(ErrorType.INPUTERROR, 
					ErrorCode.INPUT_ERROR, "Event Id or information missing from all requests", "eventId"));
		}
		else {
			headerListing.setSellerId(sellerId);
			headerListing.setSellerGuid(sellerGuid);
					
			// sets the header request and listing
			ldata.setHeaderListing(headerListing);
			ldata.setHeaderRequest(requestHeader);
			
			// UPDATE ONLY: load all current listings from DB (done early on)
			// TODO: need to find more efficient way to do this
			if ( !isCreate ) {
				Map<Long,Listing>curListings = new HashMap<Long,Listing>(listingRequests.size());
				Long sampleEventId = null;
				
				for ( int i=0; i<listingRequests.size(); i++  ) {
					ListingRequest req = listingRequests.get(i);
					try {
						// this can be null if not found
						Listing curListing = inventoryMgr.getListing( req.getListingId() );
						if ( curListing == null ) {
							ldata.addListingError(new ListingError(ErrorType.INPUTERROR, 
								ErrorCode.LISTING_NOT_FOUND, 
								"Can't find listing to update listingId=" + req.getListingId(), "listingId"));
						}
						else if ( !curListing.getSellerId().equals(sellerId) ) {
							ldata.addListingError(new ListingError(ErrorType.INPUTERROR, 
								ErrorCode.LISTING_ACTION_NOTALLOWED, 
								"Listing does not belong to seller listingId=" + req.getListingId(), "listingId"));
						}
						else {
							if(curListing.getExternalId() != null){
								  MDC.put("externalListingId", curListing.getExternalId());
							}							
							if ( sampleEventId == null ) {
							  sampleEventId = curListing.getEventId();
							}
							if ( sellerContactId == null ) {
	                          sellerContactId = curListing.getSellerContactId();
	                        }
							if( sellerContactGuid == null) {
								sellerContactGuid = curListing.getSellerContactGuid();
							}
							curListing.setSellerGuid(sellerGuid);
							curListings.put(req.getListingId(), curListing);
						}
					}
					catch ( Exception ex ) {
						ldata.addListingError(new ListingError(ErrorType.SYSTEMERROR, 
								ErrorCode.LISTING_NOT_FOUND, "Can't find listing to update id=" + req.getListingId(), "listingId"));
						log.error("Can't load listing to update id=" + req.getListingId(), ex );
					}
				}
				ldata.setCurListingsMap(sampleEventId, curListings);
			}
		}
		
        for (int index = 0; index < listingRequests.size(); index++) {
          ListingRequest req = listingRequests.get(index);
          if (req.getProducts() != null && !isFlash(req, ldata.getCurListing(req.getListingId()))) {
            Set<String> fulfillmentArtifacts = new HashSet<String>();
            for (Product p : req.getProducts()) {
              String fulfillmentArtifact = p.getFulfillmentArtifact();
              if (StringUtils.trimToNull(fulfillmentArtifact) != null) {
                fulfillmentArtifact = fulfillmentArtifact.trim();
                if (fulfillmentArtifacts.contains(fulfillmentArtifact)) {
                  ldata.addListingError(new ListingError(ErrorType.INPUTERROR,
                      ErrorCode.DUPLICATE_FULFILLMENT_ARTIFACT_ERROR, "Duplicate fulfillmentArtifact", "fulfillmentArtifact"));
                } else {
                  fulfillmentArtifacts.add(fulfillmentArtifact);
                }
              }
            }
          }
        }
		
		if(headerListing.getSellerContactId() == null) {
           headerListing.setSellerContactId(sellerContactId);
        }
		if(headerListing.getSellerContactGuid() == null) {
			headerListing.setSellerContactGuid(sellerContactGuid);
		}


		return ldata;
	}
	
	private Listing getListingFromHeader ( ListingRequest header, boolean isCreate )
	{
		return ListingRequestAdapter.convert(header, isCreate);
	}
	
	private ListingError process ( ListingData lData, ListingRequest req, ListingRequest header ) 
	{
		// HEADER: Put all common stuff in header and then clear from request (also make sure all common values are equal)
		if ( req.getCcId() != null ) {
			if ( !validUniqueValues( req.getCcId(), header.getCcId() ) ) {
				lData.addListingError(new ListingError (ErrorType.INPUTERROR, ErrorCode.INVALID_CCID, 
						"Inconsistent CC IDs in listings", "ccid"));
			}
			header.setCcId(req.getCcId());
			req.setCcId(null);
		}
		if ( req.getContactGuid() != null ) {
			if ( !validUniqueValues( req.getContactGuid(), header.getContactGuid() ) ) {
				lData.addListingError(new ListingError (ErrorType.INPUTERROR, ErrorCode.INVALID_CONTACT_GUID, 
						"Inconsistent contactGuid in listings", "contactGuid" ));
			}
			header.setContactGuid(req.getContactGuid());
			req.setContactGuid(null);
		}
		if ( req.getContactId() != null ) {
			if ( !validUniqueValues( req.getContactId(), header.getContactId() ) ) {
				lData.addListingError(new ListingError (ErrorType.INPUTERROR, ErrorCode.INVALID_CONTACT_ID, 
						"Inconsistent contactId in listings", "contactId" ));
			}
			header.setContactId(req.getContactId());
			req.setContactId(null);
		}
		if ( req.getPaymentType() != null ) {
			if ( !validUniqueValues( req.getPaymentType(), header.getPaymentType() ) ) {
				lData.addListingError(new ListingError (ErrorType.INPUTERROR, ErrorCode.INVALID_PAYMENT_TYPE, 
						"Inconsistent paymentType in listings", "paymentType" ));
			}
			header.setPaymentType(req.getPaymentType());
			req.setPaymentType(null);
		}		
		if ( req.getEventId() != null ) {
			if ( !validUniqueValues( req.getEventId(), header.getEventId() ) ) {
				lData.addListingError(new ListingError (ErrorType.INPUTERROR, ErrorCode.INVALID_EVENTID, 
						"Inconsistent eventId in listings", "eventId" ));
			}
			header.setEventId(req.getEventId());
			req.setEventId(null);
		}
		if ( req.getEvent() != null ) {
			if ( !validUniqueValues ( req.getEvent(), header.getEvent() ) ) {
				lData.addListingError(new ListingError (ErrorType.INPUTERROR, ErrorCode.EVENT_NOT_MAPPED, 
						"Inconsistent eventInfo in listings", "eventInfo" ));
			}
			header.setEvent(req.getEvent());
			req.setEvent(null);
		}
		if ( !lData.isCreateRequest() && req.getListingId()==null ) {
			lData.addListingError(new ListingError (ErrorType.INPUTERROR, ErrorCode.INVALID_LISTINGID, 
					"Listing Id is not found for update", "listingId" ));
		}

		// SELLAPI-1193 - Set Status from the Request (covers Single Listing)
		header.setStatus(req.getStatus());
		log.info("Listing Status after setting fromRequest="+header.getStatus());
		
		header.setLmsExtension(req.getLmsExtension());
		
		//markup
		header.setMarkup(req.isMarkup());
		
		return null;
	}
	
	private boolean validUniqueValues ( Object val1, Object val2 )
	{
		if ( val1 != null && val2 != null ) 
			return val1.equals(val2);
		
		return true;
	}
	
	private boolean isFlash(ListingRequest request, Listing currentListing) {
	    if(request.getDeliveryOption() != null && request.getDeliveryOption().equals(DeliveryOption.FLASHSEAT)) {
	        return true;
	    }
        if(StringUtils.trimToNull(request.getComments()) != null) {
            if(request.getComments().toLowerCase().contains("fls")) {
                return true;
            }
        }
        
        List<String> ticketTraitIds = new ArrayList<String>();
        if(request.getTicketTraits() != null && !request.getTicketTraits().isEmpty()) {
            for(TicketTrait tt : request.getTicketTraits()) {
                if(StringUtils.trimToNull(tt.getId()) != null) {
                    ticketTraitIds.add(tt.getId());
                }
            }
        }
        if(!ticketTraitIds.isEmpty() && !Collections.disjoint(ticketTraitIds, FLS_TRAIT_IDS)) {
            return true;
        }
        
        if(currentListing != null && currentListing.getTicketMedium() != null
              && TicketMedium.FLASHSEAT.getValue() == currentListing.getTicketMedium().intValue()) {
            return true;
        }
        
        return false;
    }
	
	
}
