package com.stubhub.domain.inventory.listings.v2.newflow.orchestrator;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.i18n.infra.soa.core.I18nServiceContext;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingType;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.OperationTypeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.SizeTypeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.handler.BusinessFlowHandler;
import com.stubhub.domain.inventory.listings.v2.newflow.handler.ExceptionHandler;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.ListingTypeHelper;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.RequestHelper;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.RequestValidatorHelper;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContext;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ListingOrchestratorTest {

    @Mock
    private ListingTypeHelper listingTypeHelper;

    @Mock
    private RequestHelper requestHelper;

    @Mock
    private RequestValidatorHelper requestValidatorHelper;

    @Mock
    private BusinessFlowRouterFactory businessFlowRouterFactory;

    @Mock
    ExceptionHandler exceptionHandler;

    @InjectMocks
    private ListingOrchestrator listingOrchestrator;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateListingSuccess() {
        ListingResponse listingResponse = new ListingResponse();
        listingResponse.setId("1");
        ListingRequest listingRequest = new ListingRequest();
        SHServiceContext sHServiceContext = mock(SHServiceContext.class);
        ExtendedSecurityContext extendedSecurityContext = mock(ExtendedSecurityContext.class);
        when(extendedSecurityContext.getUserId()).thenReturn("123");
        when(sHServiceContext.getExtendedSecurityContext()).thenReturn(extendedSecurityContext);
        I18nServiceContext i18nServiceContext = mock(I18nServiceContext.class);
        MessageContext messageContext = mock(MessageContext.class);

        ListingType listingType = new ListingType();
        listingType.setOperationType(OperationTypeEnum.CREATE);
        listingType.setSizeType(SizeTypeEnum.SINGLE);
        when(listingTypeHelper.getCreateListingType()).thenReturn(listingType);

        ListingDTO listingDTO = mock(ListingDTO.class);
        when(requestHelper.getListingDTO(listingType, listingRequest, messageContext, sHServiceContext)).thenReturn(listingDTO);

        BusinessFlowRouter businessFlowRouter = mock(BusinessFlowRouter.class);
        BusinessFlowHandler businessFlowHandler = mock(BusinessFlowHandler.class);

        try {
            when(businessFlowHandler.execute()).thenReturn(listingResponse);
            when(businessFlowRouter.getBusinessFlowHandler(listingDTO)).thenReturn(businessFlowHandler);
        } catch (Exception e) {
            Assert.fail("Should not reach here");
        }
        when(businessFlowRouterFactory.getBusinessFlowRouter(listingType)).thenReturn(businessFlowRouter);
        ListingResponse resp = listingOrchestrator.createListing(listingRequest, sHServiceContext, i18nServiceContext, messageContext);

        Assert.assertNotNull(resp);
        Assert.assertEquals(resp.getId(),"1");

    }

    @Test
    public void testCreateListingFailOnExecute() {
        ListingResponse listingResponse = new ListingResponse();
        listingResponse.setId("1");
        ListingRequest listingRequest = new ListingRequest();
        SHServiceContext sHServiceContext = mock(SHServiceContext.class);
        ExtendedSecurityContext extendedSecurityContext = mock(ExtendedSecurityContext.class);
        when(extendedSecurityContext.getUserId()).thenReturn("123");
        when(sHServiceContext.getExtendedSecurityContext()).thenReturn(extendedSecurityContext);
        I18nServiceContext i18nServiceContext = mock(I18nServiceContext.class);
        MessageContext messageContext = mock(MessageContext.class);

        ListingType listingType = new ListingType();
        listingType.setOperationType(OperationTypeEnum.CREATE);
        listingType.setSizeType(SizeTypeEnum.SINGLE);
        when(listingTypeHelper.getCreateListingType()).thenReturn(listingType);

        ListingDTO listingDTO = mock(ListingDTO.class);
        when(requestHelper.getListingDTO(listingType, listingRequest, messageContext, sHServiceContext)).thenReturn(listingDTO);

        BusinessFlowRouter businessFlowRouter = mock(BusinessFlowRouter.class);
        BusinessFlowHandler businessFlowHandler = mock(BusinessFlowHandler.class);
        ListingException listingException = new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidQuantity);
        when(exceptionHandler.handle(listingException)).thenReturn(listingResponse);
        ListingResponse resp = null;
        try {
            when(businessFlowHandler.execute()).thenThrow(listingException);
            when(businessFlowRouter.getBusinessFlowHandler(listingDTO)).thenReturn(businessFlowHandler);
            when(businessFlowRouterFactory.getBusinessFlowRouter(listingType)).thenReturn(businessFlowRouter);
            resp = listingOrchestrator.createListing(listingRequest, sHServiceContext, i18nServiceContext, messageContext);
        }  catch (Exception e) {
            Assert.fail("Should not reach here");
        }
        Assert.assertNotNull(resp);
        Assert.assertEquals(resp.getId(),"1");

    }

    @Test
    public void testUpdateListingSuccess() {
        ListingResponse listingResponse = new ListingResponse();
        listingResponse.setId("1");
        ListingRequest listingRequest = new ListingRequest();
        SHServiceContext sHServiceContext = mock(SHServiceContext.class);
        ExtendedSecurityContext extendedSecurityContext = mock(ExtendedSecurityContext.class);
        when(extendedSecurityContext.getUserId()).thenReturn("123");
        when(sHServiceContext.getExtendedSecurityContext()).thenReturn(extendedSecurityContext);
        I18nServiceContext i18nServiceContext = mock(I18nServiceContext.class);
        MessageContext messageContext = mock(MessageContext.class);

        ListingType listingType = new ListingType();
        listingType.setOperationType(OperationTypeEnum.UPDATE);
        listingType.setSizeType(SizeTypeEnum.SINGLE);
        when(listingTypeHelper.getUpdateListingType()).thenReturn(listingType);

        ListingDTO listingDTO = mock(ListingDTO.class);
        when(requestHelper.getListingDTO(listingType, listingRequest, messageContext, sHServiceContext)).thenReturn(listingDTO);

        BusinessFlowRouter businessFlowRouter = mock(BusinessFlowRouter.class);
        BusinessFlowHandler businessFlowHandler = mock(BusinessFlowHandler.class);

        try {
            when(businessFlowHandler.execute()).thenReturn(listingResponse);
            when(businessFlowRouter.getBusinessFlowHandler(listingDTO)).thenReturn(businessFlowHandler);
        } catch (Exception e) {
            Assert.fail("Should not reach here");
        }
        when(businessFlowRouterFactory.getBusinessFlowRouter(listingType)).thenReturn(businessFlowRouter);
        ListingResponse resp = listingOrchestrator.updateListing("123", listingRequest, sHServiceContext, i18nServiceContext, messageContext);

        Assert.assertNotNull(resp);
        Assert.assertEquals(resp.getId(),"1");

    }

}
