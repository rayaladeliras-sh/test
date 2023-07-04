package com.stubhub.domain.inventory.listings.v2.newflow.validator;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.RequestValidationException;
import com.stubhub.domain.inventory.listings.v2.newflow.validator.QuantityRequestValidator;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.newplatform.common.entity.Money;
import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

public class QuantityRequestValidatorTest {
    @Mock
    ListingRequest listingRequest;
    @InjectMocks
    private QuantityRequestValidator quantityRequestValidator;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void validateTestSucceed() {
        when(listingRequest.getQuantity()).thenReturn(100);
        try {
            quantityRequestValidator.validate();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void validateTestFailOnLowQuantity() {
        when(listingRequest.getQuantity()).thenReturn(0);
        try {
            quantityRequestValidator.validate();
        } catch (RequestValidationException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(e.getType(), ErrorType.BUSINESSERROR);
            Assert.assertEquals(e.getErrorCodeEnum().getDescription(), "The quantity is less than 1");
        }
    }

    @Test
    public void validateTestFailOnHighQuantity() {
        when(listingRequest.getQuantity()).thenReturn(200);
        try {
            quantityRequestValidator.validate();
        } catch (RequestValidationException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(e.getType(), ErrorType.BUSINESSERROR);
            Assert.assertEquals(e.getErrorCodeEnum().getDescription(), "The quantity is more than 150");
        }
    }
}
