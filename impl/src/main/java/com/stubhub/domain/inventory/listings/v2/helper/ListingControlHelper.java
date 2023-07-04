package com.stubhub.domain.inventory.listings.v2.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.common.util.StringUtils;
import com.stubhub.domain.infrastructure.config.client.core.SHConfig;

@Component("listingControlHelper")
public class ListingControlHelper {
  
  private static final Logger log = LoggerFactory.getLogger(ListingControlHelper.class);
  
  @Autowired
  private SHConfig shConfig;
  
  public boolean isListingBlock(boolean isCreate, boolean isDelete, boolean isPredelivery, String sellerId) {
    
    if(StringUtils.trimToNull(sellerId) == null) {
      return false;
    }
    
    String blockAllSellerIds = shConfig.getProperty("blockcreateandupdate", "");
    if(blockAllSellerIds.contains(sellerId)) {
      log.error("Blocking create and update sellerId={}", sellerId);
      return true;
    }
    
    if(isCreate) {
      return isListingBlockCreate(isPredelivery, sellerId);
    } else { //update listing
      return isListingBlockUpdate(isDelete, isPredelivery, sellerId);
    }

  }
  
  private boolean isListingBlockCreate(boolean isPredelivery, String sellerId) {
    String blockCreateSellerIds = shConfig.getProperty("blockcreate", "");
    if(blockCreateSellerIds.contains(sellerId)) {
      log.error("Blocking create sellerId={}", sellerId);
      return true;
    }
    
    if(isPredelivery) {
      String blockCreateBarcodeSellerIds = shConfig.getProperty("blockcreate.predelivery", "");
      if(blockCreateBarcodeSellerIds.contains(sellerId)) {
        log.error("Blocking create predelivery sellerId={}", sellerId);
        return true;
      }
    }
    
    return false;
  }
  
  private boolean isListingBlockUpdate(boolean isDelete, boolean isPredelivery, String sellerId) {
    if(isPredelivery) {
      String blockUpdateBarcodeSellerIds = shConfig.getProperty("blockupdate.predelivery", "");
      if(blockUpdateBarcodeSellerIds.contains(sellerId)) {
        log.error("Blocking update - predelivery sellerId={}", sellerId);
        return true;
      }
    } else if(isDelete) {
      String blockDeleteSellerIds = shConfig.getProperty("blockdelete", "");
      if(blockDeleteSellerIds.contains(sellerId)) {
        log.error("Blocking delete sellerId={}", sellerId);
        return true;
      }
    }
    
    return false;
  }
  

}
