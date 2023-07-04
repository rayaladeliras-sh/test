package com.stubhub.domain.inventory.listings.v2.helper;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.infrastructure.config.client.core.SHConfig;

public class ListingControlHelperTest {
  
  @InjectMocks
  private ListingControlHelper listingControlHelper;
  
  @Mock
  private SHConfig shConfig;
  
  @BeforeMethod
  public void setUp() {
      MockitoAnnotations.initMocks(this);
  }
  
  @Test
  public void testIsListingBlock() {
    when(shConfig.getProperty("blockcreateandupdate", "")).thenReturn("0");
    when(shConfig.getProperty("blockcreate", "")).thenReturn("0");
    when(shConfig.getProperty("blockcreate.predelivery", "")).thenReturn("1234");
    when(shConfig.getProperty("blockupdate.predelivery", "")).thenReturn("1234");
    when(shConfig.getProperty("blockdelete", "")).thenReturn("0");
    
    Assert.assertFalse(listingControlHelper.isListingBlock(true, false, false, "1234"));
    Assert.assertFalse(listingControlHelper.isListingBlock(true, false, false, "1234"));
    Assert.assertTrue(listingControlHelper.isListingBlock(true, false, true, "1234"));
    Assert.assertTrue(listingControlHelper.isListingBlock(false, false, true, "1234"));
    Assert.assertFalse(listingControlHelper.isListingBlock(false, true, false, "1234"));
  }
  

}
