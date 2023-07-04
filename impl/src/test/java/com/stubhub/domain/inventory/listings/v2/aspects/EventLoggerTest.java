package com.stubhub.domain.inventory.listings.v2.aspects;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.lang.reflect.Method;

import com.stubhub.domain.i18n.infra.soa.core.I18nServiceContext;

import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Assert;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.infrastructure.common.core.concurrent.SHThreadLocals;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.listings.v2.impl.ListingServiceImpl;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContextImpl;

public class EventLoggerTest {
	
	StringWriter writer = new StringWriter();
	
	ProceedingJoinPoint joinPoint = null;
    MethodSignature signature = null;
    StaticPart staticpart = null;
	
	
	
	@BeforeMethod
	public void setUp() throws Throwable 
	{
		MockitoAnnotations.initMocks(this);
		
		joinPoint = mock(ProceedingJoinPoint.class);
        signature = mock(MethodSignature.class);
        staticpart = mock(StaticPart.class);
        ListingServiceImpl listingService = new ListingServiceImpl();
        Method method = listingService.getClass().getMethod("createListing",ListingRequest.class,SHServiceContext.class, I18nServiceContext.class);
        Method updateMethod = listingService.getClass().getMethod("updateListing",String.class, ListingRequest.class,SHServiceContext.class, I18nServiceContext.class);
           
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getStaticPart()).thenReturn(staticpart);
        when(signature.getName()).thenReturn("createListing");
        when(staticpart.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenReturn(new ListingResponse());
        when(joinPoint.getArgs()).thenReturn(new Object[]{new ListingRequest(),new SHServiceContext()});
        
        SHServiceContext serviceContext = new SHServiceContext();
        		SHThreadLocals
				.set(SHServiceContext.SERVICE_CONTEXT_HEADER,serviceContext);
        ExtendedSecurityContextImpl esc = new ExtendedSecurityContextImpl();
        esc.setUserId("1234");
        esc.setApplicationName("appName");
        serviceContext.setExtendedSecurityContext(esc);
	}
	
	
	
	@Test
	public void testEventLoggerSuccess() throws Throwable
	{
		writer = new StringWriter();
		EventLogger ev = new EventLogger();
		ev.log = log;
        ev.logEvent(joinPoint, null);
        Assert.assertTrue(writer.toString().contains("createListing"));
      
		
	}
	@Test
	public void testEventLoggerUpdateListingSuccess() throws Throwable
	{
		ListingServiceImpl listingService = new ListingServiceImpl();
		Method updateMethod = listingService.getClass().getMethod("updateListing",String.class, ListingRequest.class,SHServiceContext.class, I18nServiceContext.class);
		when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getStaticPart()).thenReturn(staticpart);
        when(signature.getName()).thenReturn("updateListing");
        when(staticpart.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(updateMethod);
        when(joinPoint.proceed()).thenReturn(new ListingResponse());
        when(joinPoint.getArgs()).thenReturn(new Object[]{new ListingRequest(),new SHServiceContext()});
		writer = new StringWriter();
		EventLogger ev = new EventLogger();
		ev.log = log;
        ev.logEvent(joinPoint, null);
        Assert.assertTrue(writer.toString().contains("updateListing"));
	}
	
	@Test
	public void testUpdateListingEventLoggerWithExceptionWithCause()  throws Throwable
	{
		ListingServiceImpl listingService = new ListingServiceImpl();
		ListingRequest listingReq = new ListingRequest();
		listingReq.setListingId(1023456789L);
		Method updateMethod = listingService.getClass().getMethod("updateListing",String.class, ListingRequest.class,SHServiceContext.class, I18nServiceContext.class);
		when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getStaticPart()).thenReturn(staticpart);
        when(signature.getName()).thenReturn("updateListing");
        when(staticpart.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(updateMethod);
        when(joinPoint.proceed()).thenReturn(new ListingResponse());
        when(joinPoint.getArgs()).thenReturn(new Object[]{listingReq,new SHServiceContext()});
		writer = new StringWriter();
		EventLogger ev = new EventLogger();
		ev.log = log;
		Exception e = new RuntimeException(new ListingBusinessException(new ListingError()));
		when(joinPoint.proceed()).thenThrow(e);
        try {
			ev.logEvent(joinPoint, null);
			Assert.fail("should have failed");
		} catch (Throwable t) {
			Assert.assertTrue(t.getCause() instanceof ListingBusinessException);
			Assert.assertTrue(writer.toString().contains("errorMessage"));
		}
		
	}
	
	@Test
	public void testUpdateListingEventLoggerWithExceptionWithCauseLid()  throws Throwable
	{
		ListingServiceImpl listingService = new ListingServiceImpl();
		ListingRequest listingReq = new ListingRequest();
		listingReq.setListingId(1023456789L);
		String lId = "1023456789";
		Method updateMethod = listingService.getClass().getMethod("updateListing",String.class, ListingRequest.class,SHServiceContext.class, I18nServiceContext.class);
		when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getStaticPart()).thenReturn(staticpart);
        when(signature.getName()).thenReturn("updateListing");
        when(staticpart.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(updateMethod);
        when(joinPoint.proceed()).thenReturn(new ListingResponse());
        when(joinPoint.getArgs()).thenReturn(new Object[]{lId, listingReq,new SHServiceContext()});
		writer = new StringWriter();
		EventLogger ev = new EventLogger();
		ev.log = log;
		Exception e = new RuntimeException(new ListingBusinessException(new ListingError()));
		when(joinPoint.proceed()).thenThrow(e);
        try {
			ev.logEvent(joinPoint, null);
			Assert.fail("should have failed");
		} catch (Throwable t) {
			Assert.assertTrue(t.getCause() instanceof ListingBusinessException);
			Assert.assertTrue(writer.toString().contains("errorMessage"));
		}
		
	}
	
	@Test
	public void testEventLoggerNullContext() throws Throwable
	{
		SHThreadLocals.set(SHServiceContext.SERVICE_CONTEXT_HEADER,null);
		writer = new StringWriter();
		EventLogger ev = new EventLogger();
		ev.log = log;
        ev.logEvent(joinPoint, null);
        Assert.assertTrue(writer.toString().contains("createListing"));
	}
	
	@Test
	public void testEventLoggerWithException()  throws Throwable
	{
		writer = new StringWriter();
		EventLogger ev = new EventLogger();
		ev.log = log;
		when(joinPoint.proceed()).thenThrow(new ListingBusinessException(new ListingError()));
        try {
			ev.logEvent(joinPoint, null);
			Assert.fail("should have failed");
		} catch (Throwable e) {
			Assert.assertTrue(e instanceof ListingBusinessException);
			Assert.assertTrue(writer.toString().contains("errorMessage"));
		}
		
	}
	
	@Test
	public void testEventLoggerWithExceptionWithCause()  throws Throwable
	{
		writer = new StringWriter();
		EventLogger ev = new EventLogger();
		ev.log = log;
		Exception e = new RuntimeException(new ListingBusinessException(new ListingError()));
		when(joinPoint.proceed()).thenThrow(e);
        try {
			ev.logEvent(joinPoint, null);
			Assert.fail("should have failed");
		} catch (Throwable t) {
			Assert.assertTrue(t.getCause() instanceof ListingBusinessException);
			Assert.assertTrue(writer.toString().contains("errorMessage"));
		}
		
	}
	
	Logger log = new Logger() {		
		
		
		@Override
		public void warn(Marker marker, String format, Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void warn(Marker marker, String msg, Throwable t) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void warn(Marker marker, String format, Object... arguments) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void warn(Marker marker, String format, Object arg) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void warn(String format, Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void warn(Marker marker, String msg) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void warn(String msg, Throwable t) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void warn(String format, Object... arguments) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void warn(String format, Object arg) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void warn(String msg) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void trace(Marker marker, String format, Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void trace(Marker marker, String msg, Throwable t) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void trace(Marker marker, String format, Object... argArray) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void trace(Marker marker, String format, Object arg) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void trace(String format, Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void trace(Marker marker, String msg) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void trace(String msg, Throwable t) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void trace(String format, Object... arguments) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void trace(String format, Object arg) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void trace(String msg) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean isWarnEnabled(Marker marker) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isWarnEnabled() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isTraceEnabled(Marker marker) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isTraceEnabled() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isInfoEnabled(Marker marker) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isInfoEnabled() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isErrorEnabled(Marker marker) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isErrorEnabled() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isDebugEnabled(Marker marker) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean isDebugEnabled() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public void info(Marker marker, String format, Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void info(Marker marker, String msg, Throwable t) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void info(Marker marker, String format, Object... arguments) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void info(Marker marker, String format, Object arg) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void info(String format, Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void info(Marker marker, String msg) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void info(String msg, Throwable t) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void info(String format, Object... arguments) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void info(String format, Object arg) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void info(String msg) {
			writer.write(msg);
			
		}
		
		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void error(Marker marker, String format, Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void error(Marker marker, String msg, Throwable t) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void error(Marker marker, String format, Object... arguments) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void error(Marker marker, String format, Object arg) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void error(String format, Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void error(Marker marker, String msg) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void error(String msg, Throwable t) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void error(String format, Object... arguments) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void error(String format, Object arg) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void error(String msg) {
			writer.write(msg);
			
		}
		
		@Override
		public void debug(Marker marker, String format, Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void debug(Marker marker, String msg, Throwable t) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void debug(Marker marker, String format, Object... arguments) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void debug(Marker marker, String format, Object arg) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void debug(String format, Object arg1, Object arg2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void debug(Marker marker, String msg) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void debug(String msg, Throwable t) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void debug(String format, Object... arguments) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void debug(String format, Object arg) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void debug(String msg) {
			// TODO Auto-generated method stub
			
		}
	};
	
	
}
