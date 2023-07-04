package com.stubhub.domain.inventory.v2.DTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.newplatform.common.entity.Money;

public class ListingRequestTest {

	@Test
	 public void testGetSet() {
		ListingRequest listingRequest =new ListingRequest();
		
		listingRequest.setHideSeats(true);
		Assert.assertNotNull(listingRequest.isHideSeats());
		Assert.assertTrue(listingRequest.isHideSeats().booleanValue());

	}
	
	@Test
	 public void testFormatForLog() {
		ListingRequest listingRequest =new ListingRequest();
		
		Product product = new Product();
		product.setRow("12");
		product.setSeat("1");
		List<Product> products = new ArrayList<>();
		products.add(product);
		listingRequest.setProducts(products);
		listingRequest.setEventId("123");
		EventInfo event = new EventInfo();
		event.setName("abcd");
		event.setEventLocalDate("2017-11-04");
		event.setDate("2017-11-04");
		event.setVenue("venue");
		event.setCity("city");
		event.setState("state");		
		listingRequest.setEvent(event);		
		listingRequest.setHideSeats(true);
		listingRequest.setQuantity(1);
		listingRequest.setListingId(123L);
		listingRequest.setExternalListingId("extid");
		listingRequest.setSection("123");
		Money money = new Money();
		money.setAmount(new BigDecimal(10));
		money.setCurrency("USD");
		listingRequest.setPricePerProduct(money);
		listingRequest.setStatus(ListingStatus.ACTIVE);
		listingRequest.setDeliveryOption(DeliveryOption.BARCODE);
		listingRequest.setContactGuid("4x8uX_QvyN_acu_y");
		Assert.assertEquals(listingRequest.getContactGuid(), "4x8uX_QvyN_acu_y");
		Assert.assertNotNull(listingRequest.isHideSeats());
		Assert.assertTrue(listingRequest.isHideSeats().booleanValue());
		Assert.assertNotNull(listingRequest.formatForLog());
		listingRequest.setQuantity(null);
		listingRequest.setEventId(null);
		Assert.assertNotNull(listingRequest.formatForLog());
		

	}
		
	
}
