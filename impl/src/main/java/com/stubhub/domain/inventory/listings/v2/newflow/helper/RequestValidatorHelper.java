package com.stubhub.domain.inventory.listings.v2.newflow.helper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingType;
import com.stubhub.domain.inventory.listings.v2.newflow.validator.PriceRequestValidator;
import com.stubhub.domain.inventory.listings.v2.newflow.validator.QuantityRequestValidator;
import com.stubhub.domain.inventory.listings.v2.newflow.validator.RequestValidator;
import com.stubhub.domain.inventory.listings.v2.newflow.validator.SplitsRequestValidator;
import com.stubhub.domain.inventory.listings.v2.newflow.validator.TicketTraitsRequestValidator;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;

@Component("requestValidatorHelper")
public class RequestValidatorHelper {

	@Autowired
	private BeanFactory beanFactory;

	public void validateRequest(ListingType listingType, ListingRequest listingRequest) {
		for (RequestValidator requestValidator : getRequestValidators(listingType, listingRequest)) {
			requestValidator.validate();
		}
	}

	private List<RequestValidator> getRequestValidators(ListingType listingType, ListingRequest listingRequest) {
		switch (listingType.getOperationType()) {
		// Create Listing validation by default
	    case UPDATE:
	        return getUpdateListingRequestValidators(listingRequest);
		case CREATE:
        default:
			return getCreateListingRequestValidators(listingRequest);
		}
	}

	private List<RequestValidator> getCreateListingRequestValidators(ListingRequest listingRequest) {
		List<RequestValidator> validatorList = new ArrayList<RequestValidator>();
		// validatorList.add(new QuantityRequestValidator(listingRequest));

		return validatorList;
	}

	private List<RequestValidator> getUpdateListingRequestValidators(ListingRequest listingRequest) {
		List<RequestValidator> validatorList = new ArrayList<RequestValidator>();
		// Price
		if (listingRequest.getPricePerProduct() != null || listingRequest.getPayoutPerProduct() != null
				|| listingRequest.getFaceValue() != null || listingRequest.getBuyerSeesPerProduct() != null) {
			validatorList.add((PriceRequestValidator)beanFactory.getBean("priceRequestValidator",listingRequest));
		}

		// Split Option and Quantity
		if (listingRequest.getSplitQuantity() != null) {
			validatorList.add((SplitsRequestValidator)beanFactory.getBean("splitsRequestValidator",listingRequest));
		}

		// Quantity
		if (listingRequest.getQuantity() != null) {
			validatorList.add((QuantityRequestValidator)beanFactory.getBean("quantityRequestValidator",listingRequest));
		}
		
		//Ticket Traits
		if (listingRequest.getTicketTraits() != null && !listingRequest.getTicketTraits().isEmpty()) {
		    validatorList.add((TicketTraitsRequestValidator)beanFactory.getBean("ticketTraitsRequestValidator",listingRequest));
		}

		return validatorList;
	}

}
