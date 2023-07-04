package com.stubhub.domain.inventory.listings.v2.tasks;


import org.apache.log4j.Logger;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.infrastructure.common.core.concurrent.SHThreadLocals;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.common.util.ListingException;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.TaxpayerStatusEnum;
import com.stubhub.domain.inventory.listings.v2.util.SellerHelper;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.domain.partnerintegration.common.util.StringUtils;
import com.stubhub.domain.user.contacts.intf.CustomerContactMappingResponse;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

public class ContactUpdateTask implements CallableInventoryTask<Listing>
{
	private UserHelper userHelper;
	private SellerHelper sellerHelper;
	
	private Listing listing;
	private Listing currentListing;
	private SHAPIContext apiContext;
	SHServiceContext shServiceContext;
	private boolean isCreate = false;

	private final Map<String, String> context;
	private static final Map<String, String> EMPTY_CONTEXT = new HashMap<>();
	private final static Logger log = Logger.getLogger(ContactUpdateTask.class);
	
	public ContactUpdateTask(Listing listing, Listing currentListing, SHAPIContext apiContext, SHServiceContext shServiceContext, UserHelper userHelper, SellerHelper sellerHelper)
	{
		this (listing, currentListing, apiContext, shServiceContext, userHelper, sellerHelper, false);
	}
	
	public ContactUpdateTask(Listing listing, Listing currentListing, SHAPIContext apiContext, SHServiceContext shServiceContext, UserHelper userHelper, 
			SellerHelper sellerHelper, boolean isCreate)
	{
		this.listing = listing;
		this.currentListing = currentListing;
		this.apiContext = apiContext;
		this.shServiceContext = shServiceContext;
		this.userHelper = userHelper;
		this.sellerHelper = sellerHelper;
		this.isCreate = isCreate;
		this.context = MDC.getCopyOfContextMap() != null ? MDC.getCopyOfContextMap() : EMPTY_CONTEXT;
	}
	
	
	public Listing call() throws ListingException {
		MDC.setContextMap(this.context);
		log.debug("START async task");
		SHAPIThreadLocal.set(apiContext);
		SHThreadLocals.set(SHServiceContext.SERVICE_CONTEXT_HEADER, shServiceContext);

		//update contactGuid
		if(!isCreate){
			currentListing.setSellerContactGuidOld(currentListing.getSellerContactGuid()) ;

			if(listing.getSellerContactGuid() != null && ! listing.getSellerContactGuid().equals(currentListing.getSellerContactGuid())){
				//validate if the contactGuid is valid
				if (userHelper.isUserContactGuidValid(currentListing.getSellerGuid(), listing.getSellerContactGuid() )){
					currentListing.setSellerContactGuid(listing.getSellerContactGuid()) ;
					//get the contactId for the contactGuid
					CustomerContactMappingResponse contacMappingResponse = userHelper.getCustomerContactId(currentListing.getSellerGuid(), listing.getSellerContactGuid());
					if (contacMappingResponse != null && contacMappingResponse.internalId != null){
						currentListing.setSellerContactId(Long.parseLong(contacMappingResponse.internalId));
						listing.setSellerContactId(Long.parseLong(contacMappingResponse.internalId));
					}else{
						ListingError listingError = new ListingError(
								ErrorType.BUSINESSERROR, ErrorCode.CONFIGURATION_ERROR, "Error while updating contactGuid",
								"contactGuid");
						throw new ListingBusinessException(listingError);
					}
					
				}
				else{
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR, ErrorCode.INVALID_CONTACT_GUID, "",
							"contactGuid");
					throw new ListingBusinessException(listingError);
				}
			//	update contactId
			}else if(listing.getSellerContactId() != null && !listing.getSellerContactId().equals(currentListing.getSellerContactId())) {	
				SHAPIThreadLocal.set(apiContext);
				if (userHelper.isSellerContactValid(currentListing.getSellerGuid(), listing.getSellerContactId())){
					currentListing.setSellerContactId(listing.getSellerContactId());
					//contactMapping  inverse call to get the contactGuid
					CustomerContactMappingResponse response= userHelper.getCustomerContactGuid(currentListing.getSellerGuid(), listing.getSellerContactId().toString());
					if(response != null && response.id != null){
						currentListing.setSellerContactGuid(response.id);
						listing.setSellerContactGuid(response.id);
					}else{
						ListingError listingError = new ListingError(
								ErrorType.BUSINESSERROR, ErrorCode.CONFIGURATION_ERROR, "Error while updating contactId",
								"contactId");
						throw new ListingBusinessException(listingError);
					}
	
				}
				else{
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR, ErrorCode.INVALID_CONTACT_ID, "",
							"contactId");
					throw new ListingBusinessException(listingError);
				}
			}
		}
		
		
		// if create listing do this
		if ( isCreate ) {
			if(listing.getSellerContactId() == null && listing.getSellerContactGuid() == null){
				sellerHelper.populateSellerDetails(listing);
			}
			
			if(listing.getSellerContactId() != null){
				//contactMapping  inverse call to get the contactGuid
				CustomerContactMappingResponse response= userHelper.getCustomerContactGuid(listing.getSellerGuid(), listing.getSellerContactId().toString());
				if(response != null && response.id != null){
					listing.setSellerContactGuid(response.id);
				}else{
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR, ErrorCode.CONFIGURATION_ERROR, "Error while getting contact details ",
							"contactGuid");
					throw new ListingBusinessException(listingError);
				}
			}else if(listing.getSellerContactGuid() != null){
				if (userHelper.isUserContactGuidValid(listing.getSellerGuid(), listing.getSellerContactGuid() )){
					CustomerContactMappingResponse contacMappingResponse = userHelper.getCustomerContactId(listing.getSellerGuid(), listing.getSellerContactGuid());
					if (contacMappingResponse != null && contacMappingResponse.internalId != null && StringUtils.isNumeric(contacMappingResponse.internalId) ){
						listing.setSellerContactId(Long.parseLong(contacMappingResponse.internalId));
					}else{
						ListingError listingError = new ListingError(
								ErrorType.BUSINESSERROR, ErrorCode.CONFIGURATION_ERROR, "Error while getting contact details ",
								"contactId");
						throw new ListingBusinessException(listingError);
					}
				}else{
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR, ErrorCode.INVALID_CONTACT_GUID, "","contactGuid");
					throw new ListingBusinessException(listingError);
				}
			}
			
			if(TaxpayerStatusEnum.TINRequired.getStatus().equalsIgnoreCase(listing.getTaxpayerStatus()) ||
					TaxpayerStatusEnum.TINInvalid.getStatus().equalsIgnoreCase(listing.getTaxpayerStatus())){
				ListingError listingError = new ListingError
				(ErrorType.BUSINESSERROR, 
						ErrorCode.TAXPAYER_ERROR, "TIN is either not on file or Invalid", "");
				throw new ListingBusinessException(listingError);
			}
			currentListing.setSellerContactId(listing.getSellerContactId());
			currentListing.setSellerContactGuid(listing.getSellerContactGuid());
			currentListing.setTaxpayerStatus(listing.getTaxpayerStatus() );
		}		
		log.debug("END async task");
		return currentListing;
	 }

	@Override
	public boolean ifNeedToRunTask() {
		return listing.getSellerContactId() != null || isCreate;
	}
}