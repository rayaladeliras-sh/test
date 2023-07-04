package com.stubhub.domain.inventory.listings.v2.aspects;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.ws.rs.core.Response;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stubhub.domain.infrastructure.common.core.concurrent.SHThreadLocals;
import com.stubhub.domain.infrastructure.common.exception.base.SHMappableException;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.v2.DTO.SplunkFormattedLog;
import com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContext;


/**
 * @author rdeverakonda
 *Aspect intercepts methods annotated with <code>LogEvent</code> annotation and logs the request,response/exception in
 *splunk friendly format.
 */

@Aspect
public class EventLogger {
	protected Logger log = LoggerFactory.getLogger(EventLogger.class);
	private final static String SERVICE_NAME = " action=";
	private final static String ERROR_MESSAGE = "errorMessage='";
	private final static String SINGLE_SPACE = " ";
	private final static String SELLER_ID_HEADER=" sellerId=";
	private final static String APP_NAME_HEADER=" appName=";
	private final static String SINGLE_QUOTE="'";
	private final static String DOUBLE_QUOTE="\"";
	private final static String LISTING_ID = "listingId";
	private static final String PAIR = "=";
	private static final String STATUS_CODE = "statusCode=";
	private static final String ERROR_CODE = "errorCode=";
	private static final String ERROR_DESCRIPTION = "errorDescription=";
	
	
	
	/*
	 * Intercepts method calls annotated with LogEvent annotation and logs the request arguments, response/exception in splunk 
	 * friendly format. Annotate the argument with <code>ExcludeParam</code> annotation, if the argument should not be logged.	
	 */
	@Around("execution(* com.stubhub.domain.inventory..*(..)) " + "&& @annotation(logEvent) ")
	public Object logEvent(ProceedingJoinPoint pjp, LogEvent logEvent) throws Throwable {
		
		Throwable methodException = null;
		String invokedMethod = null;
		StringBuilder logMessage = new StringBuilder();
		StringBuilder requestMessage = new StringBuilder();
		
		//wrap all the request capturing in a try/catch block so even if there is an error, can move forward to the method call.
		try {
			
			logMessage.append(getContextFromHeader()).append(SERVICE_NAME);
			
			invokedMethod = pjp.getSignature().getName();
			logMessage.append(invokedMethod).append(SINGLE_SPACE);
			
			
			
			//log the arguments.
			final Signature signature = pjp.getStaticPart().getSignature();
			if(signature instanceof MethodSignature){
			    final MethodSignature ms = (MethodSignature) signature;
			    final Method method = ms.getMethod();
			    //get the annotations for each of the arguments.
			    final Annotation[][] parameterAnnotations = method.getParameterAnnotations();		    
			    
			    //get all arguments of the method.
				final Object[] methodArguments = pjp.getArgs();
			    int argumentIndex=0;
			    //loop through the annotations for each of the arguments.
			    for(Annotation[] annotations : parameterAnnotations){
			      Object argument = methodArguments[argumentIndex];
			      
			      if(!excludeParameter(annotations)){
			    	  //addArgumentLog(gson, parameters, argument);
			    	  addArgumentToLog(requestMessage,argument, argumentIndex);
			      };
			      argumentIndex++;
			    }			    
			}
			
			logMessage.append(requestMessage.toString());
			log.info(logMessage.toString());
		} catch (Exception e1) {
			log.warn("Error in capturing the request and parameters for logging.");
		}
		
		//setup the outbound message.
		logMessage = new StringBuilder(getContextFromHeader()).append(SERVICE_NAME).append(invokedMethod).append(SINGLE_SPACE);
		logMessage.append("stage=outbound ");
		//add the request message to setup the context for the response/error
		logMessage.append(requestMessage.toString());
		
		final long startTime = System.nanoTime();
		Object retVal = null;
		try {
			retVal = pjp.proceed();
			if(retVal != null){	
				//add the response to the log 
				addArgumentToLog(logMessage,retVal, -1);				
			}
		} catch (Throwable t) {
			methodException = t;
		}
		long endTime = System.nanoTime();
		
		log.info("total time taken for {} - {} in nanoseconds",invokedMethod, (endTime-startTime));
		
		//capture the exception details in the logMessage and rethrow the exception
		if (methodException != null) {
		    if(methodException instanceof SHMappableException) {
		        SHMappableException shException = (SHMappableException) methodException;
		        logMessage.append(STATUS_CODE).append(shException.getStatusCode()).append(SINGLE_SPACE);
		        logMessage.append(ERROR_CODE).append(shException.getErrorCode()).append(SINGLE_SPACE);
		        logMessage.append(ERROR_DESCRIPTION).append(DOUBLE_QUOTE).append(shException.getDescription()).append(DOUBLE_QUOTE).append(SINGLE_SPACE);
		    } else {
		        logMessage.append(STATUS_CODE).append(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).append(SINGLE_SPACE);
		    }
			if(methodException.getCause() != null){
				logMessage.append(ERROR_MESSAGE).append(methodException.getCause().getMessage()).append(SINGLE_QUOTE);
				log.error(logMessage.toString());
			}else{
				logMessage.append(ERROR_MESSAGE).append(methodException.getMessage()).append(SINGLE_QUOTE);
				log.error(logMessage.toString());
			}	
			
			throw methodException;
		}
		
		
		log.info(logMessage.toString());

		return retVal;
	}

	
	/*
	 * get the sellerId and appName from the ServiceContext.
	 */
	protected String getContextFromHeader() {
		SHServiceContext serviceContext = (SHServiceContext) SHThreadLocals
				.get(SHServiceContext.SERVICE_CONTEXT_HEADER);

		StringBuilder logStringFromHeader = new StringBuilder();
		if(serviceContext != null){
			
			ExtendedSecurityContext esc = serviceContext.getExtendedSecurityContext();
			if (esc != null) {
				logStringFromHeader.append(SELLER_ID_HEADER).append(esc.getUserId()).append(APP_NAME_HEADER)
						.append(esc.getApplicationName());
			}

			return logStringFromHeader.toString();
		}
		
		return logStringFromHeader.append(SELLER_ID_HEADER).append("NA").append(APP_NAME_HEADER)
				.append("NA").toString();

	}

	private void addArgumentToLog(StringBuilder arguments, Object argument, int argumentIndex) {
		if(argument instanceof SplunkFormattedLog){
			arguments.append(((SplunkFormattedLog)argument).formatForLog());
		}else{
			if(argumentIndex == 0){
				arguments.append(LISTING_ID).append(PAIR).append(argument.toString()).append(SINGLE_SPACE);
			}else{
				arguments.append(SINGLE_SPACE).append(argument.toString()).append(SINGLE_SPACE);
			}
		}		
		
	}

	/*
	 * is the argument annotated to exclude logging.
	 */
	protected boolean excludeParameter(final Annotation[] annotations) {
		for(Annotation annotation : annotations){
		    if(annotation instanceof ExcludeLogParam){
		    	return true;
		    }
		 }
		
		return false;
	}

}
