package com.stubhub.domain.inventory.v2.DTO;

import org.junit.Assert;
import org.testng.annotations.Test;

public class RelistListingTest {
	
	private static final String emailId = "70@testmail.com";
	private static final String customerGUID = "6C21FF95408F3BC0E04400144FB7AAA6";
	
  @Test
  public void  testGetSet(){
	  
	  RelistListing relistListing = new RelistListing();
	  relistListing.setToEmailId(emailId);
	  relistListing.setToCustomerGUID(customerGUID);
	  Assert.assertNotNull(relistListing.getToEmailId());
	  Assert.assertNotNull(relistListing.getToCustomerGUID());
  }
}
