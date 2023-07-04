package com.stubhub.domain.inventory.listings.v2.helper.tasks;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.PaymentType;
import com.stubhub.domain.inventory.listings.v2.tasks.PaymentTypeUpdateTask;
import com.stubhub.domain.inventory.listings.v2.util.PaymentHelper;
import com.stubhub.domain.inventory.listings.v2.util.SHInventoryTest;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;

public class PaymentTypeUpdateTaskTest extends SHInventoryTest
{
	@BeforeTest
	public void setup() throws Exception
	{
		MockitoAnnotations.initMocks(this);
	}
		
	@Test
	public void test_validSellerWithInvalidPayment () throws Exception
	{
		Listing dblisting = getListing(1000L, "section-10", "R1", "1,2,3,4,5", 5, 5);
		
		Listing newlisting = new Listing();
		newlisting.setId(1000l);
		newlisting.setSellerId(1000l);		
		newlisting.setSellerPaymentTypeId(PaymentType.Paypal.getType());
		
		PaymentHelper paymentHelper = Mockito.mock (PaymentHelper.class );
		when(paymentHelper.isSellerPaymentTypeValidForSeller(Mockito.anyLong(), Mockito.anyLong())).thenReturn(false);
		
		UserHelper userHelper =  Mockito.mock (UserHelper.class );
		when(userHelper.isSellerPaymentTypeValid(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		
		SHAPIContext shapiContext = new SHAPIContext();
		SHServiceContext shServiceContext = new SHServiceContext();
		PaymentTypeUpdateTask task = new PaymentTypeUpdateTask (newlisting, dblisting, shapiContext, shServiceContext, userHelper, paymentHelper);
		
		try {
			task.call();
			Assert.fail("Should not allow Valid Seller with an Invalid Payment Type");
		}
		catch ( ListingBusinessException ex ) {
		}
	}	
	
	@Test
	public void test_invalidSellerWithValidPayment () throws Exception
	{
		Listing dblisting = getListing(1000L, "section-10", "R1", "1,2,3,4,5", 5, 5);
		
		Listing newlisting = new Listing();
		newlisting.setId(1000l);
		newlisting.setSellerId(1000l);		
		newlisting.setSellerPaymentTypeId(PaymentType.Paypal.getType());
		
		PaymentHelper paymentHelper = Mockito.mock (PaymentHelper.class );
		when(paymentHelper.isSellerPaymentTypeValidForSeller(Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		
		UserHelper userHelper =  Mockito.mock (UserHelper.class );
		when(userHelper.isSellerPaymentTypeValid(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong())).thenReturn(false);
		
		SHAPIContext shapiContext = new SHAPIContext();
		SHServiceContext shServiceContext = new SHServiceContext();
		PaymentTypeUpdateTask task = new PaymentTypeUpdateTask (newlisting, dblisting, shapiContext, shServiceContext, userHelper, paymentHelper);
		
		try {
			task.call();
			Assert.fail("Should not allow Invalid Seller with an Valid Payment Type");
		}
		catch ( ListingBusinessException ex ) {
		}
	}	
	
	@Test
	public void test_validPaymentAndSeller () throws Exception
	{
		Listing dblisting = getListing(1000L, "section-10", "R1", "1,2,3,4,5", 5, 5);
		
		Listing newlisting = new Listing();
		newlisting.setId(1000l);
		newlisting.setSellerId(1000l);		
		
		PaymentHelper paymentHelper = Mockito.mock (PaymentHelper.class );
		when(paymentHelper.isSellerPaymentTypeValidForSeller(Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		
		UserHelper userHelper =  Mockito.mock (UserHelper.class );
		when(userHelper.isSellerPaymentTypeValid(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong())).thenReturn(true);
		
		SHAPIContext shapiContext = new SHAPIContext();
		SHServiceContext shServiceContext = new SHServiceContext();
		PaymentTypeUpdateTask task = new PaymentTypeUpdateTask (newlisting, dblisting, shapiContext, shServiceContext, userHelper, paymentHelper);
		
		try {
			task.call();
		}
		catch ( ListingBusinessException ex ) {
			Assert.fail("Should allow valid payment with valid seller");
		}
	}	
}

