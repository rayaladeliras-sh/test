package com.stubhub.domain.inventory.listings.v2.helper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.i18n.currencyconversion.v1.util.CurrencyConvertor;
import com.stubhub.domain.i18n.currencyconversion.v1.util.entity.XMoney;
import com.stubhub.domain.inventory.v2.DTO.Fee;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.newplatform.common.entity.Money;

import junit.framework.Assert;

public class AdvisoryCurrencyHelperTest {
	
	@InjectMocks
  	private AdvisoryCurrencyHelper advisoryCurrencyHelper;

	@Mock
	CurrencyConvertor currencyConvertor;
	
	ListingResponse listingResponse;

	@BeforeMethod
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		listingResponse = getListingResponse();
	}

	@Test
	public void testSetForex() {
		String userCurrency = "GBP";
		Mockito.when(currencyConvertor.convertMoney((Money)Mockito.anyObject(), Mockito.anyString())).thenReturn(new XMoney());
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		
		Assert.assertNotNull(listingResponse.getBuyerSeesPerProduct());
		
		//null user Currency
		advisoryCurrencyHelper.setForex(listingResponse,null);
		Assert.assertNotNull(listingResponse.getBuyerSeesPerProduct());
	}
	
	@Test 
	void testSetForexNullListingResponse(){
		String userCurrency = "GBP";
		advisoryCurrencyHelper.setForex(null,userCurrency);
		advisoryCurrencyHelper.setForex(null,null);
		
	}
	
	@Test 
	void testSetForexNullBuyerSeesPerProduct(){
		String userCurrency = "GBP";
		Mockito.when(currencyConvertor.convertMoney((Money)Mockito.anyObject(), Mockito.anyString())).thenReturn(new XMoney());
		listingResponse.setBuyerSeesPerProduct(null);
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		
	}
	
	@Test
	void testSetForexNullgetEndPricePerTicket(){
		String userCurrency = "GBP";
		listingResponse.getEndPricePerTicket().setCurrency("USD");
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		Assert.assertNotNull(listingResponse);
		
		listingResponse.setEndPricePerTicket(null);
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		Assert.assertNotNull(listingResponse);
		
	}
	
	@Test
	void testSetForexNullgetFaceValue(){
		String userCurrency = "GBP";
		listingResponse.setFaceValue(null);
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		Assert.assertNotNull(listingResponse);
	}
	
	@Test
	void testSetForexNullPayoutPerProduct(){
		String userCurrency = "GBP";
		listingResponse.setPayoutPerProduct(null);;
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		Assert.assertNotNull(listingResponse);
		
	}
	
	@Test
	void testSetForexNullPricePerProduct(){
		String userCurrency = "GBP";
		listingResponse.setPricePerProduct(null);;
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		Assert.assertNotNull(listingResponse);
		
	}
	
	@Test
	void testSetForexNullPurchasePrice(){
		String userCurrency = "GBP";
		listingResponse.setPurchasePrice(null);;
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		Assert.assertNotNull(listingResponse);
		
	}
	
	@Test
	void testSetForexNullStartPricePerTicket(){
		String userCurrency = "GBP";
		listingResponse.setStartPricePerTicket(null);;
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		Assert.assertNotNull(listingResponse);
		
	}
	
	@Test
	void testSetForexNullTotalPayout(){
		String userCurrency = "GBP";
		listingResponse.setTotalPayout(null);;
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		Assert.assertNotNull(listingResponse);
		
	}
	
	@Test
	void testSetForexNullFees(){
		String userCurrency = "GBP";
		listingResponse.setFees(null);;
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		Assert.assertNotNull(listingResponse);
		
	}
	
	@Test void testUserCurrencyNotEqualsTransCurrency(){
		String userCurrency = "GBP";
		ListingResponse listingResp = getListingResponse();

		listingResp.getEndPricePerTicket().setCurrency("USD");
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		Assert.assertNotNull(listingResponse);

		listingResp.getEndPricePerTicket().setCurrency("USD");
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		Assert.assertNotNull(listingResponse);

		listingResp.getFaceValue().setCurrency("USD");
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		Assert.assertNotNull(listingResponse);

		listingResp.getPayoutPerProduct().setCurrency("USD");
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		Assert.assertNotNull(listingResponse);

		listingResp.getPricePerProduct().setCurrency("USD");
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		Assert.assertNotNull(listingResponse);

		listingResp.getPurchasePrice().setCurrency("USD");
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		Assert.assertNotNull(listingResponse);

		listingResp.getStartPricePerTicket().setCurrency("USD");
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		Assert.assertNotNull(listingResponse);

		listingResp.getTotalPayout().setCurrency("USD");
		advisoryCurrencyHelper.setForex(listingResponse,userCurrency);
		Assert.assertNotNull(listingResponse);
		
		
	}
	
	
	
	private ListingResponse getListingResponse(){
		ListingResponse listingResponse = new ListingResponse();
		Money money = new Money();
		money.setCurrency("GBP");
		money.setAmount(new BigDecimal("100"));
		listingResponse.setBuyerSeesPerProduct(money);
		listingResponse.setEndPricePerTicket(money);
		listingResponse.setFaceValue(money);
		listingResponse.setPayoutPerProduct(money);
		listingResponse.setPricePerProduct(money);
		listingResponse.setPurchasePrice(money);
		listingResponse.setStartPricePerTicket(money);
		listingResponse.setTotalPayout(money);	

		List<Fee> fees = new ArrayList<Fee>();
		Fee fee = new Fee();
		fee.setAmount(money);
		fees.add(fee);
		listingResponse.setFees(fees);
		return listingResponse;
	}

}