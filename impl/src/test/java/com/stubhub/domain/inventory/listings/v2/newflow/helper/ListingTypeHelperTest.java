package com.stubhub.domain.inventory.listings.v2.newflow.helper;

import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingType;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.OperationTypeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.SizeTypeEnum;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ListingTypeHelperTest {
    @Test
    public void testGetCreateListingType() {
        ListingTypeHelper listingTypeHelper = new ListingTypeHelper();
        ListingType listingType = listingTypeHelper.getCreateListingType();
        Assert.assertEquals(listingType.getOperationType(), OperationTypeEnum.CREATE);
        Assert.assertEquals(listingType.getSizeType(), SizeTypeEnum.SINGLE);
    }

    @Test
    public void testGetUpdateListingType() {
        ListingTypeHelper listingTypeHelper = new ListingTypeHelper();
        ListingType listingType = listingTypeHelper.getUpdateListingType();
        Assert.assertEquals(listingType.getOperationType(), OperationTypeEnum.UPDATE);
        Assert.assertEquals(listingType.getSizeType(), SizeTypeEnum.SINGLE);
    }
    
    @Test
    public void testGetUpdateListingBulkType() {
        ListingTypeHelper listingTypeHelper = new ListingTypeHelper();
        ListingType listingType = listingTypeHelper.getListingType(false, true);
        Assert.assertEquals(listingType.getOperationType(), OperationTypeEnum.UPDATE);
        Assert.assertEquals(listingType.getSizeType(), SizeTypeEnum.BULK);
    }
}
