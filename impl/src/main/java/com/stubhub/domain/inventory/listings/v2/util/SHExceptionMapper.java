package com.stubhub.domain.inventory.listings.v2.util;

import com.stubhub.domain.infrastructure.common.core.context.SHDyeContext;
import com.stubhub.domain.infrastructure.common.exception.base.SHMappableException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHValidationErrorException;
import com.stubhub.domain.infrastructure.soa.intf.exception.ErrorEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Provider
public class SHExceptionMapper<E extends Throwable> implements ExceptionMapper<E> {
    private static final String DEFAULT_DESCRIPTION = "";
    private static final int DEFAULT_STATUS_CODE = 500;
    private static final String DEFAULT_REQUEST_ID = "";
    private static final Logger logger = LoggerFactory.getLogger(SHExceptionMapper.class);

    public SHExceptionMapper() {
    }

    public Response toResponse(E t) {
        //Copied from com.stubhub.domain.infrastructure.soa.server.exception.SHExceptionMapper
        //only modify this line to avoid print stack track
        logger.error("_message=\"mapping an exception to jaxrs response\" errorClass={} errorMessage={}", t.getClass().toString(), t.getMessage());
        ErrorEntity error = new ErrorEntity();
        error.setCode(this.getErrorCodeFromThrowable(t));
        error.setDescription(this.getDescriptionFromThrowable(t));
        error.setRequestId(this.getDyeIdFromRequest());
        if (t instanceof SHMappableException) {
            error.setData(((SHMappableException)t).getData());
        }

        if (t instanceof SHValidationErrorException) {
            error.setValidationErrors(((SHValidationErrorException)t).getErrors());
        }

        return Response.status(this.getStatusCodeFromThrowable(t)).entity(error).build();
    }

    protected String getErrorCodeFromThrowable(Throwable t) {
        if (t instanceof SHMappableException) {
            return ((SHMappableException)t).getErrorCode();
        } else {
            String cName = t.getClass().getName();
            int start = cName.indexOf("domain.");
            if (start == -1) {
                return cName;
            } else {
                start += 7;
                String trimmed = cName.substring(start);
                int firstEnd = trimmed.indexOf(46);
                int secondEnd = trimmed.indexOf(46, firstEnd + 1);
                return trimmed.substring(0, secondEnd + 1) + t.getClass().getSimpleName();
            }
        }
    }

    protected String getDescriptionFromThrowable(Throwable t) {
        if (t instanceof SHMappableException) {
            return ((SHMappableException)t).getDescription();
        } else {
            String message = t.getMessage();
            return message != null ? message : "";
        }
    }

    protected int getStatusCodeFromThrowable(Throwable t) {
        return t instanceof SHMappableException ? ((SHMappableException)t).getStatusCode() : 500;
    }

    private String getDyeIdFromRequest() {
        String dyeId = null;
        SHDyeContext dyeContext = SHDyeContext.get();
        if (dyeContext != null) {
            dyeId = dyeContext.getDye();
        }

        return dyeId != null ? dyeId : "";
    }
}
