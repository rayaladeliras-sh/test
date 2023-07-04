package com.stubhub.domain.inventory.listings.v2.enums;

import com.stubhub.domain.inventory.v2.enums.ProductMediumEnum;
import org.junit.Assert;
import org.junit.Test;

public class DMEnumTest {

    @Test
    public void testEnum(){
        Assert.assertEquals(DMEnum.Electronic_Download,DMEnum.getDMEnumByCode(1));
    }
}