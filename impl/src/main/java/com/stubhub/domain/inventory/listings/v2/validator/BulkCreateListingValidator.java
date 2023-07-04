package com.stubhub.domain.inventory.listings.v2.validator;

import java.util.ArrayList;
import java.util.List;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.entity.ErrorEnum;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.common.util.ListingException;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.TaxpayerStatusEnum;
import com.stubhub.domain.inventory.listings.v2.util.SellerHelper;

public class BulkCreateListingValidator implements Validator {

	private SellerHelper sellerHelper;
	
	private Long sellerId; 
	private List<ListingError> errors = new ArrayList<ListingError>();
	
	public BulkCreateListingValidator(Long sellerId, SellerHelper sellerHelper){
		this.sellerId = sellerId;	
		this.sellerHelper=sellerHelper;
	}
	
	
	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.listings.v2.validator.Validator#validate()
	 */
	@Override
	public void validate(){
		Listing tempListing = new Listing();
		tempListing.setSellerId(sellerId);
		try {
			sellerHelper.populateSellerDetails(tempListing);
			if(tempListing.getTaxpayerStatus()!=null && TaxpayerStatusEnum.TINRequired.getStatus().equalsIgnoreCase(tempListing.getTaxpayerStatus()) ||
					TaxpayerStatusEnum.TINInvalid.getStatus().equalsIgnoreCase(tempListing.getTaxpayerStatus())){
				errors.add(new ListingError
						(ErrorType.BUSINESSERROR, 
								ErrorCode.TAXPAYER_ERROR, "TIN is either not on file or Invalid", ""));
			}
			if(tempListing.getSellerContactId()==null){
				errors.add(new ListingError
						(ErrorType.BUSINESSERROR, 
								ErrorCode.INVALID_CONTACT_ID, "Default contact missing", ""));
			}
			//TODO return error if the input listings array is empty or null
		} catch (ListingException e) {
			if(ErrorEnum.INVALID_SELLER_GUID.getCode().equals(e.getListingError().getCode())){
				errors.add(new ListingError
						(ErrorType.BUSINESSERROR, 
								ErrorCode.INVALID_SELLERID, ErrorEnum.INVALID_SELLER_GUID.getMessage(), ""));
			}else{
				errors.add(new ListingError
						(ErrorType.SYSTEMERROR, 
								ErrorCode.SYSTEM_ERROR, ErrorEnum.SYSTEM_ERROR.getMessage(), ""));
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see com.stubhub.domain.inventory.listings.v2.validator.Validator#getErrors()
	 */
	@Override
	public List<ListingError> getErrors() {
		return errors;
	}

}
