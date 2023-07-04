package com.stubhub.domain.inventory.listings.v2.newflow.task;

import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.PricingHelper;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class PricingTask extends RegularTask {

    @Autowired
    private PricingHelper priceHelper;

    private PriceResponse priceResponse;

    public PricingTask(ListingDTO listingDTO) {
        super(listingDTO);
    }

    @Override
    public void preExecute() {
        /*if (SaleMethod.DECLINING.getValue().equals(listingDTO.getPricingInfo().getSaleMethod())
                && !ListingStatus.INCOMPLETE.toString().equals(listingDTO.getStatus())) {
            if (listingDTO.getPricingInfo().getMinPricePerTicket() == null
                    || listingDTO.getPricingInfo().getMaxPricePerTicket() == null) {
                ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.START_AND_END_PRICE_REQUIRED, "Start and End Price required for Declining Price", "");
                throw new ListingBusinessException(listingError);
            }
            if (!(priceHelper.isValidPriceDiff(listingDTO.getPricingInfo()))) {
                ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_START_END_PRICE, "Invalid start and end price", "");
                throw new ListingBusinessException(listingError);
            }
        }
        if (!priceHelper.isValidCurrency(listingDTO)) {
            ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_CURRENCY, "Invalid currency", "pricePerTicket");
            throw new ListingBusinessException(listingError);
        }
        if (!priceHelper.isValidFaceValue(listingDTO)) {
            ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_FACE_VALUE, "Invalid face value", "faceValue");
            throw new ListingBusinessException(listingError);
        }
        if (!priceHelper.isListingPriceUnderMaxPrice(listingDTO)) {
            ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.LISTPRICE_EXCEEDED_MAXLIMIT, "Price exceeding max allowed limit", "pricePerTicket");
            throw new ListingBusinessException(listingError);
        }*/
    }

    @Override
    public void execute() {
//      priceResponse = priceHelper.getPriceResponse(listingDTO);
    }

    @Override
    public void postExecute() {
//        priceHelper.validate(listingDTO, priceResponse);
//        priceHelper.populate(listingDTO, priceResponse);
    }

}
