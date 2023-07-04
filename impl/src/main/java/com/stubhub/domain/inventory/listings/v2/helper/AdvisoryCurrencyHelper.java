package com.stubhub.domain.inventory.listings.v2.helper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.i18n.currencyconversion.v1.util.CurrencyConvertor;
import com.stubhub.domain.inventory.v2.DTO.Fee;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;

/**
 * 
 * @author smohite
 *
 */

@Component("advisoryCurrencyHelper")
public class AdvisoryCurrencyHelper {

	@Autowired
	CurrencyConvertor currencyConvertor;

	/**
	 * This method sets the Foreign exchange values
	 * 
	 * @param listingResponse
	 */
	public ListingResponse setForex(ListingResponse listingResponse, String userCurrency) {

		if (listingResponse == null || userCurrency == null) {
			return listingResponse;
		}

		if (listingResponse != null && currencyConvertor != null) {
			if (listingResponse.getBuyerSeesPerProduct() != null) {
				if (!listingResponse.getBuyerSeesPerProduct().getCurrency().equals(userCurrency)) {
					listingResponse.setBuyerSeesPerProduct(
							currencyConvertor.convertMoney(listingResponse.getBuyerSeesPerProduct(), userCurrency));
				}

			}
			if (listingResponse.getEndPricePerTicket() != null) {
				if (!listingResponse.getEndPricePerTicket().getCurrency().equals(userCurrency)) {
					listingResponse.setEndPricePerTicket(
							currencyConvertor.convertMoney(listingResponse.getEndPricePerTicket(), userCurrency));
				}
			}
			if (listingResponse.getFaceValue() != null) {
				if (!listingResponse.getFaceValue().getCurrency().equals(userCurrency)) {
					listingResponse
							.setFaceValue(currencyConvertor.convertMoney(listingResponse.getFaceValue(), userCurrency));
				}
			}
			if (listingResponse.getPayoutPerProduct() != null) {
				if (!listingResponse.getPayoutPerProduct().getCurrency().equals(userCurrency)) {
					listingResponse.setPayoutPerProduct(
							currencyConvertor.convertMoney(listingResponse.getPayoutPerProduct(), userCurrency));
				}
			}
			if (listingResponse.getPricePerProduct() != null) {
				if (!listingResponse.getPricePerProduct().getCurrency().equals(userCurrency)) {
					listingResponse.setPricePerProduct(
							currencyConvertor.convertMoney(listingResponse.getPricePerProduct(), userCurrency));
				}
			}
			if (listingResponse.getPurchasePrice() != null) {
				if (!listingResponse.getPurchasePrice().getCurrency().equals(userCurrency)) {
					listingResponse.setPurchasePrice(
							currencyConvertor.convertMoney(listingResponse.getPurchasePrice(), userCurrency));
				}
			}
			if (listingResponse.getStartPricePerTicket() != null) {
				if (!listingResponse.getStartPricePerTicket().getCurrency().equals(userCurrency)) {
					listingResponse.setStartPricePerTicket(
							currencyConvertor.convertMoney(listingResponse.getStartPricePerTicket(), userCurrency));
				}
			}
			if (listingResponse.getTotalPayout() != null) {
				if (!listingResponse.getTotalPayout().getCurrency().equals(userCurrency)) {
					listingResponse.setTotalPayout(
							currencyConvertor.convertMoney(listingResponse.getTotalPayout(), userCurrency));
				}
			}

			List<Fee> fees = listingResponse.getFees();
			if (fees != null) {
				for (Fee fee : fees) {
					if (fee.getAmount() != null) {
						if (!fee.getAmount().getCurrency().equals(userCurrency)) {
							fee.setAmount(currencyConvertor.convertMoney(fee.getAmount(), userCurrency));
						}
					}
				}
			}
		}
		return listingResponse;
	}

}