package com.stubhub.domain.inventory.listings.v2.helper;

/**
 * Created by ryang1 on 8/8/2016.
 */
public class PaymentEligibility {

    private Boolean allowPaymentAboveThreshold;
    private Boolean allowPaymentBelowThreshold;

    public PaymentEligibility() {}

    public Boolean getAllowPaymentAboveThreshold() {
        return this.allowPaymentAboveThreshold;
    }

    public void setAllowPaymentAboveThreshold(Boolean allowPaymentAboveThreshold) {
        this.allowPaymentAboveThreshold = allowPaymentAboveThreshold;
    }

    public Boolean getAllowPaymentBelowThreshold() {
        return this.allowPaymentBelowThreshold;
    }

    public void setAllowPaymentBelowThreshold(Boolean allowPaymentBelowThreshold) {
        this.allowPaymentBelowThreshold = allowPaymentBelowThreshold;
    }
}