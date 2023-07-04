package com.stubhub.domain.inventory.listings.v2.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.activemq.util.ByteArrayInputStream;
import org.apache.cxf.jaxrs.client.WebClient;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.pricing.intf.aip.v1.request.PriceRequest;
import com.stubhub.domain.pricing.intf.aip.v1.request.PriceRequestList;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

public class ListingPriceUtilTest {
	
	@InjectMocks
	private ListingPriceUtil listPriceUtil;
	@Mock
	private SvcLocator svcLocator;	
	@Mock
	private WebClient webClient;
	@Mock
	private SHAPIThreadLocal shapiThreadLocal;
	@Mock
	private MediaType mediaType;
	@Mock
	private Response response;
	@Mock
	private SHAPIContext ctx ;
	@BeforeMethod
	public void setUp() {
		listPriceUtil = new ListingPriceUtil() {
			public String getProperty(String propertyName, String defaultValue) {
				if ("pricing.v1.price.api.url".equals(propertyName)){
					return "http://api-int.slcq015.com/pricing/aip/v1/price";
				}
				return "";
			}
		};
		MockitoAnnotations.initMocks(this);

		Mockito.when(svcLocator.locate(Mockito.anyString(), Mockito.anyList())).thenReturn(webClient);
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
	}
	
	
	@Test
	public void getListingPricingsAIP() throws IOException{
		PriceRequestList priceReqList = new PriceRequestList();
		List<PriceRequest> reqList = new ArrayList<PriceRequest>(1);
		PriceRequest preq = new PriceRequest();
		preq.setListingId( 123456l );
		preq.setRequestKey( "1" );
		preq.setSection("SECTION240");
		preq.setRow ("row11" );
		preq.setEventId(8993478L);
		String priceType = "PAYOUT";
	
		preq.setAmountType (priceType);
					
		preq.setListingSource("StubHubPro");
		
		preq.setIncludePayout(true);
		preq.setAdjustToMinListPrice(false);
		reqList.add(preq);
		
		
		priceReqList.setPriceRequest(reqList);
		
		
	     Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
	     webClient.accept(MediaType.APPLICATION_JSON);
	     Mockito.when(webClient.post(Mockito.any())).thenReturn(getResponse());
		 try {
			 	listPriceUtil.getListingPricingsAIP(ctx, priceReqList);
			} catch (Exception e1) {
				Assert.assertTrue(true); 
			}
     

	}

	private Response getResponse() {
		Response response =  new Response() {
			@Override
			public int getStatus() {
				return 200;
			}
			@Override
			public MultivaluedMap<String, Object> getMetadata() {				
				return null;
			}
			@Override
			public Object getEntity() {
				String resp = "{\"priceResponseList\":{\"priceResponse\":[{\"requestKey\":\"1\",\"displayPrice\":{\"amount\":20.46,\"currency\":\"USD\"},\"listingPrice\":{\"amount\":13,\"currency\":\"USD\"},\"payout\":{\"amount\":11.70,\"currency\":\"USD\"}}]}}";
				return new ByteArrayInputStream(resp.getBytes());
			}
		};
		return response;
	}
	

}
