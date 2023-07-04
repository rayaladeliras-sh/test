package com.stubhub.domain.inventory.listings.v2.newflow.handler;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.infrastructure.common.exception.base.SHRuntimeException;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExceptionHandlerTest {
    @InjectMocks
    private ExceptionHandler exceptionHandler;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHandlehandleListingExceptionCustomMessage() {
        ListingException t = new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidQuantity);
        t.setCustomMessage("Custom msg");
        try {
            exceptionHandler.handle(t);
        } catch (SHRuntimeException e) {
            Assert.assertEquals(e.getErrorCode(), "inventory.listings.invalidQuantity");
            Assert.assertEquals(e.getDescription(),"Custom msg");
        }
    }

    @Test
    public void testHandlehandleListingExceptionINPUTERROR() {
        Throwable t = new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidQuantity);
        try {
            exceptionHandler.handle(t);
        } catch (SHRuntimeException e) {
            Assert.assertEquals(e.getErrorCode(), "inventory.listings.invalidQuantity");
            Assert.assertEquals(e.getDescription(), "The quantity is less than 1");
        }
    }

    @Test
    public void testHandlehandleListingExceptionAUTHENTICATIONERROR() {
        Throwable t = new ListingException(ErrorType.AUTHENTICATIONERROR, ErrorCodeEnum.unknownError);
        try {
            exceptionHandler.handle(t);
        } catch (SHRuntimeException e) {
            Assert.assertEquals(e.getErrorCode(), "inventory.listings.unknownError");
            Assert.assertEquals(e.getDescription(), "");
        }
    }

    @Test
    public void testHandlehandleListingExceptionNOTFOUND() {
        Throwable t = new ListingException(ErrorType.NOT_FOUND, ErrorCodeEnum.invalidListingid);
        try {
            exceptionHandler.handle(t);
        } catch (SHRuntimeException e) {
            Assert.assertEquals(e.getErrorCode(), "inventory.listings.invalidListingid");
            Assert.assertEquals(e.getDescription(), "Listing was created by different seller");
        }
    }

    @Test
    public void testHandlehandleListingExceptionSYSTEMERROR() {
        Throwable t = new ListingException(ErrorType.SYSTEMERROR, ErrorCodeEnum.systemError);
        try {
            exceptionHandler.handle(t);
        } catch (SHRuntimeException e) {
            Assert.assertEquals(e.getErrorCode(), "inventory.listings.systemError");
            Assert.assertEquals(e.getDescription(), "System error happened while processing");
        }
    }

    @Test
    public void testHandlehandleSHSystemExceptionSYSTEMERROR() {
        Throwable t = new SHSystemException();
        try {
            exceptionHandler.handle(t);
        } catch (SHRuntimeException e) {
            Assert.assertEquals(e.getDescription(), "Unable to process the request, please re-try");
        }
    }
}
