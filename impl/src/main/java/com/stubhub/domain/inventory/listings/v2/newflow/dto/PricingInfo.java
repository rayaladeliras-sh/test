package com.stubhub.domain.inventory.listings.v2.newflow.dto;

import com.stubhub.newplatform.common.entity.Money;

import java.util.Currency;

public class PricingInfo {

	private Money listingPrice;

	private Money totalListingPrice;

	private Money displayPrice;

	private Money sellerPayoutPerTicket;

	private Money totalSellerPayout;

	private Money sellFeePerTicket;

	private Money totalSellFee;

	private String sellFeeDescription;

	private Money faceValue;

	private Money minPricePerTicket;

	private Money maxPricePerTicket;

    private Currency currency;

	private Long saleMethod;

	public Money getListingPrice() {
		return listingPrice;
	}

	public void setListingPrice(Money listingPrice) {
		this.listingPrice = listingPrice;
	}

	public Money getTotalListingPrice() {
		return totalListingPrice;
	}

	public void setTotalListingPrice(Money totalListingPrice) {
		this.totalListingPrice = totalListingPrice;
	}

	public Money getDisplayPrice() {
		return displayPrice;
	}

	public void setDisplayPrice(Money displayPrice) {
		this.displayPrice = displayPrice;
	}

	public Money getSellerPayoutPerTicket() {
		return sellerPayoutPerTicket;
	}

	public void setSellerPayoutPerTicket(Money sellerPayoutPerTicket) {
		this.sellerPayoutPerTicket = sellerPayoutPerTicket;
	}

	public Money getTotalSellerPayout() {
		return totalSellerPayout;
	}

	public void setTotalSellerPayout(Money totalSellerPayout) {
		this.totalSellerPayout = totalSellerPayout;
	}

	public Money getSellFeePerTicket() {
		return sellFeePerTicket;
	}

	public void setSellFeePerTicket(Money sellFeePerTicket) {
		this.sellFeePerTicket = sellFeePerTicket;
	}

	public Money getTotalSellFee() {
		return totalSellFee;
	}

	public void setTotalSellFee(Money totalSellFee) {
		this.totalSellFee = totalSellFee;
	}

	public String getSellFeeDescription() {
		return sellFeeDescription;
	}

	public void setSellFeeDescription(String sellFeeDescription) {
		this.sellFeeDescription = sellFeeDescription;
	}

	public Money getFaceValue() {
		return faceValue;
	}

	public void setFaceValue(Money faceValue) {
		this.faceValue = faceValue;
	}

	public Money getMinPricePerTicket() {
		return minPricePerTicket;
	}

	public void setMinPricePerTicket(Money minPricePerTicket) {
		this.minPricePerTicket = minPricePerTicket;
	}

    public Money getMaxPricePerTicket() {
        return maxPricePerTicket;
    }

    public void setMaxPricePerTicket(Money maxPricePerTicket) {
        this.maxPricePerTicket = maxPricePerTicket;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

	public Long getSaleMethod() {
		return saleMethod;
	}

	public void setSaleMethod(Long saleMethod) {
		this.saleMethod = saleMethod;
	}

}
