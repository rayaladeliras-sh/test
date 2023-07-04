package com.stubhub.domain.inventory.listings.v2.newflow.validator;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.validator.PriceRequestValidator;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.newplatform.common.entity.Money;
import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

public class PriceRequestValidatorTest {
    @Mock
    ListingRequest listingRequest;
    @InjectMocks
    private PriceRequestValidator priceRequestValidator;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void validateTestSucceed() {
        when(listingRequest.getPricePerProduct()).thenReturn(new Money("123"));
        when(listingRequest.getPayoutPerProduct()).thenReturn(new Money("123"));
        when(listingRequest.getFaceValue()).thenReturn(new Money("123"));
        try {
            priceRequestValidator.validate();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void validateTestFailOnPricePerProduct() {
        when(listingRequest.getPricePerProduct()).thenReturn(new Money("-123"));
        try {
            priceRequestValidator.validate();
        } catch (ListingException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(e.getType(), ErrorType.INPUTERROR);
            Assert.assertEquals(e.getErrorCodeEnum().getDescription(), "Invalid price");
        }
    }
    @Test
    public void validateTestFailOnPayoutPerProduct() {
        when(listingRequest.getPricePerProduct()).thenReturn(new Money("123"));
        when(listingRequest.getPayoutPerProduct()).thenReturn(new Money("-123"));
        try {
            priceRequestValidator.validate();
        } catch (ListingException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(e.getType(), ErrorType.INPUTERROR);
            Assert.assertEquals(e.getErrorCodeEnum().getDescription(), "Invalid price");
        }
    }

    @Test
    public void validateTestFailOnFaceValue() {
        when(listingRequest.getPricePerProduct()).thenReturn(new Money("123"));
        when(listingRequest.getPayoutPerProduct()).thenReturn(new Money("123"));
        when(listingRequest.getFaceValue()).thenReturn(new Money("-123"));
        try {
            priceRequestValidator.validate();
        } catch (ListingException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(e.getType(), ErrorType.INPUTERROR);
            Assert.assertEquals(e.getErrorCodeEnum().getDescription(), "Invalid Face value");
        }
    }
}
