package com.stubhub.domain.inventory.listings.v2.newflow.helper;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.infrastructure.common.exception.derived.SHForbiddenException;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.listings.v2.controller.helper.IntegrationHelper;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.HeaderInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingType;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.SellerInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContext;

@Component("requestHelper")
public class RequestHelper {

  private static final String RELIST = "Relist";
  static final String RELIST_MARKER = "Relist|V2|";
  static final String SUBSCRIBER_URL = "http://stubhub.com/claims/subscriber";
  private static final Logger log = LoggerFactory.getLogger(RequestHelper.class);

  @Autowired
  private IntegrationHelper integrationHelper;

  public ThreadLocal<String> relistFlag = new ThreadLocal<String>() {
    @Override
    protected String initialValue() {
      return null;
    }
  };

  public ListingDTO getListingDTO(ListingType listingType, ListingRequest listingRequest,
      MessageContext context, SHServiceContext shServiceContext) {
    ListingDTO listingDTO = new ListingDTO(listingRequest);
    listingDTO.setListingType(listingType);
    listingDTO.setSellerInfo(getSellerInfo(shServiceContext));
    listingDTO.setHeaderInfo(getHeaderInfo(context, shServiceContext));

    return listingDTO;
  }

  private SellerInfo getSellerInfo(SHServiceContext shServiceContext) {
    SellerInfo sellerInfo = new SellerInfo();
    String sellerId = null;
    String sellerGuid = null;
    if (isAuthZRequest(shServiceContext)) {
      sellerId = shServiceContext.getProxiedId();
      if (sellerId != null) {
        if (!StringUtils.isNumeric(sellerId)) {
          log.info("AuthZ request. userGuid is passed as ProxiedId value");
          sellerId = shServiceContext.getExtendedSecurityContext().getUserId();
          sellerGuid = shServiceContext.getExtendedSecurityContext().getUserGuid();
        } else {
          sellerGuid = integrationHelper.getUserGuidFromUid(Long.valueOf(sellerId));
        }
      }
      log.info("AuthZ request. sellerId={} sellerGuid={}", sellerId, sellerGuid);
    } else {
			if (shServiceContext != null) {
				sellerId = shServiceContext.getExtendedSecurityContext().getUserId();
				sellerGuid = shServiceContext.getExtendedSecurityContext().getUserGuid();
			}
    }
    
    if (sellerId == null) {
      log.error("message=\"Authentication error - No seller ID in the context\"");
      throw new ListingException(ErrorType.AUTHENTICATIONERROR, ErrorCodeEnum.invalidSellerid, "Invalid security token");
    }

    sellerInfo.setSellerId(Long.valueOf(sellerId));
    sellerInfo.setSellerGuid(sellerGuid);

    return sellerInfo;
  }

  private HeaderInfo getHeaderInfo(MessageContext context, SHServiceContext shServiceContext) {
    HeaderInfo headerInfo = new HeaderInfo();
    headerInfo.setUserAgent(getUserAgent(context));
    headerInfo.setClientIp(getClientIP(context.getHttpHeaders()));
    final String operatorId = shServiceContext.getOperatorId();
    headerInfo
        .setSubscriber(getSubscriber(shServiceContext.getExtendedSecurityContext(), operatorId));

    return headerInfo;
  }

  private String getUserAgent(MessageContext context) {
    MultivaluedMap<String, String> headersMap = context.getHttpHeaders().getRequestHeaders();
    if (headersMap != null) {
      return headersMap.getFirst(HttpHeaders.USER_AGENT);
    } else {
      return null;
    }
  }

  public static String getClientIP(HttpHeaders httpHeaders) {
    List<String> clientIp = httpHeaders.getRequestHeader("X-FORWARDED-FOR");
    if (clientIp != null && clientIp.size() > 0) {
      return clientIp.get(0);
    } else {
      Message message = PhaseInterceptorChain.getCurrentMessage();
      if (message != null) {
        HttpServletRequest httpRequest =
            (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
        return httpRequest.getRemoteAddr();
      }
    }
    return null;
  }

  private String getSubscriber(ExtendedSecurityContext securityContext, String operatorId) {

    StringBuffer subscriber = new StringBuffer();

    if (StringUtils.isNotBlank(operatorId)) {
      subscriber.append(operatorId).append("|");
    }

    if (RELIST.equals(relistFlag.get())) {
      subscriber.append(RELIST_MARKER);
    } else {
      subscriber.append("Single|V2|");
    }

    if (securityContext != null) {
      if (securityContext.getOperatorApp() != null && !securityContext.getOperatorApp().isEmpty()) {
        log.info("message=\"Operator is not null for this user\" userId={} operator={}",
            securityContext.getUserId(), securityContext.getOperatorApp());
        subscriber.append(securityContext.getOperatorApp());
      } else {
        Map<String, Object> extendedInfo = securityContext.getExtendedInfo();
        if (extendedInfo != null) {
          if (extendedInfo.get(SUBSCRIBER_URL) != null) {
            log.info(
                "message=\"Operator is null.. Falling back to Subscriber for this user\" userId={}",
                securityContext.getUserId());
            subscriber.append((String) extendedInfo.get(SUBSCRIBER_URL));
          }
        }
      }
      String appName = "";
      appName = securityContext.getApplicationName();
      subscriber.append("|" + appName);
    }

    return subscriber.toString();

  }

  private boolean isAuthZRequest(SHServiceContext shServiceContext) {
    if (shServiceContext != null) {
      String role = shServiceContext.getRole();
      String operatorId = shServiceContext.getOperatorId();
      String proxiedId = shServiceContext.getProxiedId();
      if (StringUtils.isNotBlank(operatorId) && StringUtils.isNotBlank(proxiedId)
          && StringUtils.isNotBlank(role))
        return ("R2".compareToIgnoreCase(role) == 0) || ("R3".compareToIgnoreCase(role) == 0);
    }

    return false;
  }


}
