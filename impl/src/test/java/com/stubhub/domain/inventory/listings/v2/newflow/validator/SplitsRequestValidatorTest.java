package com.stubhub.domain.inventory.listings.v2.newflow.validator;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;

public class SplitsRequestValidatorTest {
  @Mock
  ListingRequest listingRequest;
  @InjectMocks
  private SplitsRequestValidator SplitsRequestValidator;

  @BeforeMethod
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void validateTestSucceed() {
    when(listingRequest.getSplitQuantity()).thenReturn(123);
    try {
      SplitsRequestValidator.validate();
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void validateTestFail() {
    when(listingRequest.getSplitQuantity()).thenReturn(0);
    try {
      SplitsRequestValidator.validate();
    } catch (ListingException e) {
      Assert.assertNotNull(e);
      Assert.assertEquals(e.getType(), ErrorType.INPUTERROR);
      Assert.assertEquals(e.getErrorCodeEnum().getDescription(), "Invalid split quantity");
    }
  }
}
