package com.stubhub.domain.inventory.listings.v2.newflow.dto;

import com.stubhub.domain.inventory.listings.v2.newflow.enums.OperationTypeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.SizeTypeEnum;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ListingTypeTest {

    @Test
    public void testGetterSetterOperationType() {
        ListingType listingType = new ListingType();
        listingType.setSizeType(SizeTypeEnum.SINGLE);
        Assert.assertEquals(listingType.getSizeType(), SizeTypeEnum.SINGLE);
        listingType.setOperationType(OperationTypeEnum.UPDATE);
        Assert.assertEquals(listingType.getOperationType(), OperationTypeEnum.UPDATE);

    }

}
