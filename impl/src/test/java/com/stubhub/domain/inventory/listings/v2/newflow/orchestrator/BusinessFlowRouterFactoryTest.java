package com.stubhub.domain.inventory.listings.v2.newflow.orchestrator;

import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingType;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.OperationTypeEnum;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

public class BusinessFlowRouterFactoryTest {

//    @Mock
//    private CreateListingFlowRouter createListingFlowRouter;

    @Mock
    private UpdateListingFlowRouter updateListingFlowRouter;

    @InjectMocks
    private BusinessFlowRouterFactory businessFlowRouterFactory;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

//    @Test
//    public void getBusinessFlowRouterForCreateTest() {
//        ListingType listingType = new ListingType();
//        listingType.setOperationType(OperationTypeEnum.CREATE);
//        BusinessFlowRouter businessFlowRouter = businessFlowRouterFactory.getBusinessFlowRouter(listingType);
//        Assert.assertNotNull(businessFlowRouter);
//        Assert.assertEquals(mock(CreateListingFlowRouter.class).getClass(),businessFlowRouter.getClass());
//    }

    @Test
    public void getBusinessFlowRouterForUpdateTest() {
        ListingType listingType = new ListingType();
        listingType.setOperationType(OperationTypeEnum.UPDATE);
        BusinessFlowRouter businessFlowRouter = businessFlowRouterFactory.getBusinessFlowRouter(listingType);
        Assert.assertNotNull(businessFlowRouter);
        Assert.assertEquals(mock(UpdateListingFlowRouter.class).getClass(),businessFlowRouter.getClass());
    }

    @Test
    public void getBusinessFlowRouterForDefaultTest() {
        ListingType listingType = new ListingType();
        listingType.setOperationType(OperationTypeEnum.DELETE);
        BusinessFlowRouter businessFlowRouter = businessFlowRouterFactory.getBusinessFlowRouter(listingType);
        Assert.assertNotNull(businessFlowRouter);
        Assert.assertEquals(mock(UpdateListingFlowRouter.class).getClass(),businessFlowRouter.getClass());
    }
}
