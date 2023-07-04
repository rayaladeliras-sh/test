package com.stubhub.domain.inventory.listings.v2.newflow.helper;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingType;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.OperationTypeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.validator.PriceRequestValidator;
import com.stubhub.domain.inventory.listings.v2.newflow.validator.QuantityRequestValidator;
import com.stubhub.domain.inventory.listings.v2.newflow.validator.SplitsRequestValidator;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.newplatform.common.entity.Money;

public class RequestValidatorHelperTest {
  @Mock
  private BeanFactory beanFactory;
  @InjectMocks
  RequestValidatorHelper requestValidatorHelper;

  @BeforeMethod
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testValidateRequestCreateSuccess() {
    ListingType listingType = new ListingType();
    listingType.setOperationType(OperationTypeEnum.CREATE);
    ListingRequest listingRequest = new ListingRequest();
    try {
      requestValidatorHelper.validateRequest(listingType, listingRequest);
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }

  }

  @Test
  public void testValidateRequestUpdateSuccess() {
    ListingType listingType = new ListingType();
    listingType.setOperationType(OperationTypeEnum.UPDATE);
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setPricePerProduct(new Money("123"));
    listingRequest.setSplitQuantity(2);
    listingRequest.setQuantity(4);
    PriceRequestValidator priceRequestValidator = new PriceRequestValidator(listingRequest);
    when(beanFactory.getBean("priceRequestValidator", listingRequest))
        .thenReturn(priceRequestValidator);
    SplitsRequestValidator splitsRequestValidator = new SplitsRequestValidator(listingRequest);
    when(beanFactory.getBean("splitsRequestValidator", listingRequest))
        .thenReturn(splitsRequestValidator);
    QuantityRequestValidator quantityRequestValidator =
        new QuantityRequestValidator(listingRequest);
    when(beanFactory.getBean("quantityRequestValidator", listingRequest))
        .thenReturn(quantityRequestValidator);
    try {
      requestValidatorHelper.validateRequest(listingType, listingRequest);
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }

  }

  @Test
  public void testValidateRequestDefault() {
    ListingType listingType = new ListingType();
    listingType.setOperationType(OperationTypeEnum.DELETE);
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setPricePerProduct(new Money("123"));
    listingRequest.setSplitQuantity(2);
    listingRequest.setQuantity(4);
    PriceRequestValidator priceRequestValidator = new PriceRequestValidator(listingRequest);
    when(beanFactory.getBean("priceRequestValidator", listingRequest))
        .thenReturn(priceRequestValidator);
    SplitsRequestValidator splitsRequestValidator = new SplitsRequestValidator(listingRequest);
    when(beanFactory.getBean("splitsRequestValidator", listingRequest))
        .thenReturn(splitsRequestValidator);
    QuantityRequestValidator quantityRequestValidator =
        new QuantityRequestValidator(listingRequest);
    when(beanFactory.getBean("quantityRequestValidator", listingRequest))
        .thenReturn(quantityRequestValidator);
    try {
      requestValidatorHelper.validateRequest(listingType, listingRequest);
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }

  }

  @Test
  public void testValidateRequestUpdateFailOnPricePerProduct() {
    ListingType listingType = new ListingType();
    listingType.setOperationType(OperationTypeEnum.UPDATE);
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setPricePerProduct(new Money("-1"));
    PriceRequestValidator priceRequestValidator = new PriceRequestValidator(listingRequest);
    when(beanFactory.getBean("priceRequestValidator", listingRequest))
        .thenReturn(priceRequestValidator);
    try {
      requestValidatorHelper.validateRequest(listingType, listingRequest);
    } catch (ListingException e) {
      Assert.assertNotNull(e);
      Assert.assertEquals(e.getType(), ErrorType.INPUTERROR);
      Assert.assertEquals(e.getErrorCodeEnum().getDescription(), "Invalid price");
    }
  }

  @Test
  public void testValidateRequestUpdateFailOnBuyerSeesPerProduct() {
    ListingType listingType = new ListingType();
    listingType.setOperationType(OperationTypeEnum.UPDATE);
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setBuyerSeesPerProduct(new Money("-1"));
    PriceRequestValidator priceRequestValidator = new PriceRequestValidator(listingRequest);
    when(beanFactory.getBean("priceRequestValidator", listingRequest))
        .thenReturn(priceRequestValidator);
    try {
      requestValidatorHelper.validateRequest(listingType, listingRequest);
    } catch (ListingException e) {
      Assert.assertNotNull(e);
      Assert.assertEquals(e.getType(), ErrorType.INPUTERROR);
      Assert.assertEquals(e.getErrorCodeEnum().getDescription(), "Invalid price");
    }
  }

  @Test
  public void testValidateRequestUpdateFailOnSplitQuantity() {
    ListingType listingType = new ListingType();
    listingType.setOperationType(OperationTypeEnum.UPDATE);
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setSplitQuantity(0);
    SplitsRequestValidator splitsRequestValidator = new SplitsRequestValidator(listingRequest);
    when(beanFactory.getBean("splitsRequestValidator", listingRequest))
        .thenReturn(splitsRequestValidator);
    try {
      requestValidatorHelper.validateRequest(listingType, listingRequest);
    } catch (ListingException e) {
      Assert.assertNotNull(e);
      Assert.assertEquals(e.getType(), ErrorType.INPUTERROR);
      Assert.assertEquals(e.getErrorCodeEnum().getDescription(), "Invalid split quantity");
    }
  }

  @Test
  public void testValidateRequestUpdateFailOnInvalidQuantity() {
    ListingType listingType = new ListingType();
    listingType.setOperationType(OperationTypeEnum.UPDATE);
    ListingRequest listingRequest = new ListingRequest();
    listingRequest.setQuantity(-1);
    QuantityRequestValidator quantityRequestValidator =
        new QuantityRequestValidator(listingRequest);
    when(beanFactory.getBean("quantityRequestValidator", listingRequest))
        .thenReturn(quantityRequestValidator);
    try {
      requestValidatorHelper.validateRequest(listingType, listingRequest);
    } catch (ListingException e) {
      Assert.assertNotNull(e);
      Assert.assertEquals(e.getType(), ErrorType.BUSINESSERROR);
      Assert.assertEquals(e.getErrorCodeEnum().getDescription(), "The quantity is less than 1");
    }
  }
}
