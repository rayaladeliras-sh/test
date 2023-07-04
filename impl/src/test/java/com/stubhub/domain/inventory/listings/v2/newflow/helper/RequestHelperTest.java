package com.stubhub.domain.inventory.listings.v2.newflow.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.listings.v2.controller.helper.IntegrationHelper;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingType;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContext;

import mockit.Mocked;
import mockit.NonStrictExpectations;

public class RequestHelperTest {
  
    @Mock
    IntegrationHelper integrationHelper;
    
    @InjectMocks
    RequestHelper requestHelper;
    @Mocked
    PhaseInterceptorChain phaseInterceptorChain;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStaticClasses();
    }

    @Test
    public void testGetListingDTOSuccess1() {
        ListingType listingType = new ListingType();
        ListingRequest listingRequest = new ListingRequest();
        MessageContext messageContext = mock(MessageContext.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        MultivaluedMap map = mock(MultivaluedMap.class);
        when(map.getFirst(HttpHeaders.USER_AGENT)).thenReturn("userAgent");
        when(headers.getRequestHeaders()).thenReturn(map);
        List<String> ip = new ArrayList<>();
        ip.add("192.168.1.1");
        when(headers.getRequestHeader("X-FORWARDED-FOR")).thenReturn(ip);
        when(messageContext.getHttpHeaders()).thenReturn(headers);
        SHServiceContext shs = mock(SHServiceContext.class);
        ExtendedSecurityContext esc = mock(ExtendedSecurityContext.class);
        when(esc.getUserId()).thenReturn("1");
        when(esc.getUserGuid()).thenReturn("2");
        when(esc.getOperatorApp()).thenReturn("operator");
        when(shs.getExtendedSecurityContext()).thenReturn(esc);
        when(shs.getOperatorId()).thenReturn("OperatorId");

        ListingDTO listingDTO = requestHelper.getListingDTO(listingType, listingRequest, messageContext, shs);

        Assert.assertNotNull(listingDTO);
        Assert.assertEquals(listingDTO.getListingType(), listingType);
        Assert.assertEquals(listingDTO.getSellerInfo().getSellerId(), (Long) 1L);
        Assert.assertEquals(listingDTO.getSellerInfo().getSellerGuid(), "2");
        Assert.assertEquals(listingDTO.getHeaderInfo().getClientIp(), "192.168.1.1");
    }

    @Test
    public void testGetListingDTOSuccess2() {
        ListingType listingType = new ListingType();
        ListingRequest listingRequest = new ListingRequest();
        MessageContext messageContext = mock(MessageContext.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        MultivaluedMap map = mock(MultivaluedMap.class);
        when(map.getFirst(HttpHeaders.USER_AGENT)).thenReturn("userAgent");
        when(headers.getRequestHeaders()).thenReturn(map);
        List<String> ip = new ArrayList<>();
        ip.add("192.168.1.1");
        when(headers.getRequestHeader("X-FORWARDED-FOR")).thenReturn(ip);
        when(messageContext.getHttpHeaders()).thenReturn(headers);
        SHServiceContext shs = mock(SHServiceContext.class);
        ExtendedSecurityContext esc = mock(ExtendedSecurityContext.class);
        when(esc.getUserId()).thenReturn("1");
        when(esc.getUserGuid()).thenReturn("2");
        when(esc.getOperatorApp()).thenReturn(null);
        Map<String, Object> extendedInfo = new HashMap<>();
        extendedInfo.put("http://stubhub.com/claims/subscriber", "url");
        when(esc.getExtendedInfo()).thenReturn(extendedInfo);
        when(shs.getExtendedSecurityContext()).thenReturn(esc);
        when(shs.getOperatorId()).thenReturn("OperatorId");
        when(shs.getRole()).thenReturn("R2");
        when(shs.getProxiedId()).thenReturn("12345");
        when(integrationHelper.getUserGuidFromUid(Mockito.anyLong())).thenReturn("ABC12345");
        
        ListingDTO listingDTO = requestHelper.getListingDTO(listingType, listingRequest, messageContext, shs);

        Assert.assertNotNull(listingDTO);
        Assert.assertEquals(listingDTO.getListingType(), listingType);
        Assert.assertEquals(listingDTO.getSellerInfo().getSellerId(), (Long) 12345L);
        Assert.assertEquals(listingDTO.getSellerInfo().getSellerGuid(), "ABC12345");
        Assert.assertEquals(listingDTO.getHeaderInfo().getClientIp(), "192.168.1.1");
    }
    
    @Test
    public void testGetListingDTOSuccess3() {
        ListingType listingType = new ListingType();
        ListingRequest listingRequest = new ListingRequest();
        MessageContext messageContext = mock(MessageContext.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        MultivaluedMap map = mock(MultivaluedMap.class);
        when(map.getFirst(HttpHeaders.USER_AGENT)).thenReturn("userAgent");
        when(headers.getRequestHeaders()).thenReturn(map);
        List<String> ip = new ArrayList<>();
        ip.add("192.168.1.1");
        when(headers.getRequestHeader("X-FORWARDED-FOR")).thenReturn(ip);
        when(messageContext.getHttpHeaders()).thenReturn(headers);
        SHServiceContext shs = mock(SHServiceContext.class);
        ExtendedSecurityContext esc = mock(ExtendedSecurityContext.class);
        when(esc.getUserId()).thenReturn("1");
        when(esc.getUserGuid()).thenReturn("2");
        when(esc.getOperatorApp()).thenReturn(null);
        Map<String, Object> extendedInfo = new HashMap<>();
        extendedInfo.put("http://stubhub.com/claims/subscriber", "url");
        when(esc.getExtendedInfo()).thenReturn(extendedInfo);
        when(shs.getExtendedSecurityContext()).thenReturn(esc);
        when(shs.getOperatorId()).thenReturn("OperatorId");
        when(shs.getRole()).thenReturn("R2");
        when(shs.getProxiedId()).thenReturn("AB12345");
        when(integrationHelper.getUserGuidFromUid(Mockito.anyLong())).thenReturn("ABC12345");
        
        ListingDTO listingDTO = requestHelper.getListingDTO(listingType, listingRequest, messageContext, shs);

        Assert.assertNotNull(listingDTO);
        Assert.assertEquals(listingDTO.getListingType(), listingType);
        Assert.assertEquals(listingDTO.getSellerInfo().getSellerId(), (Long) 1L);
        Assert.assertEquals(listingDTO.getSellerInfo().getSellerGuid(), "2");
        Assert.assertEquals(listingDTO.getHeaderInfo().getClientIp(), "192.168.1.1");
    }

    @Test(expectedExceptions = ListingException.class)
    public void testGetListingDTOFailNoSHServiceContext() {
        MessageContext messageContext = mock(MessageContext.class);
        requestHelper.getListingDTO(new ListingType(), new ListingRequest(), messageContext, null);
    }

    @Test
    public void testGetClientIPSuccess() {
        HttpHeaders headers = mock(HttpHeaders.class);
        when(headers.getRequestHeader("X-FORWARDED-FOR")).thenReturn(null);
        String ip = requestHelper.getClientIP(headers);
        Assert.assertEquals(ip, "123.123.1.1");
    }

    private void mockStaticClasses() {
        try {
            new NonStrictExpectations() {
                {
                    phaseInterceptorChain.getCurrentMessage();
                    Message message = mock(Message.class);
                    HttpServletRequest req = mock(HttpServletRequest.class);
                    when(req.getRemoteAddr()).thenReturn("123.123.1.1");
                    when(message.get(AbstractHTTPDestination.HTTP_REQUEST)).thenReturn(req);
                    returns(message);
                }
            };
        } catch (Exception e) {
        }
    }
}
