package com.stubhub.domain.inventory.listings.v2.helper;

import java.io.ByteArrayInputStream;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

public class TransferValidatorTest {

	private static final Log log = LogFactory.getLog(TransferValidatorTest.class);

	private TransferValidator transferValidator;
	private SvcLocator svcLocator;
	private WebClient webClient;
	private ObjectMapper objectMapper;
	private UserHelper userHelper;

	@BeforeMethod
	public void setUp() {
		transferValidator = new TransferValidator() {
			protected String getProperty(String propertyName, String defaultValue) {
				if (TransferValidator.CUSTOMER_END_POINT.equals(propertyName))
					return "https://api.stubcloudprod.com/user/customers/v1/?action=checkEmail&emailAddress={emailId}";
				return "";
			}
		};
		svcLocator = Mockito.mock(SvcLocator.class);
		webClient = Mockito.mock(WebClient.class);
		objectMapper = Mockito.mock(ObjectMapper.class);
		userHelper = Mockito.mock(UserHelper.class);
		ReflectionTestUtils.setField(transferValidator, "svcLocator", svcLocator);
		ReflectionTestUtils.setField(transferValidator, "objectMapper", objectMapper);
		ReflectionTestUtils.setField(transferValidator, "userHelper", userHelper);
	}

	@Test
	public void populateCustomerDetails() {
		
		Mockito.when(userHelper.getWebClientFromSveLocator(Mockito.anyString(), Mockito.anyList(), Mockito.anyBoolean())).thenReturn(webClient);
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
		Mockito.when(webClient.get()).thenReturn(getResponse200());
		
		String userGuid = transferValidator
				.getUserGuid("testing@testmail.com");
		Assert.assertNotNull(userGuid);
	}
	
	

	@Test
	public void populateCustomerDetails_notFound() {
		Mockito.when(userHelper.getWebClientFromSveLocator(Mockito.anyString(), Mockito.anyList(), Mockito.anyBoolean())).thenReturn(webClient);
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
		
		Mockito.when(webClient.get()).thenReturn(getResponse(404));
		
		String userGuid = transferValidator
				.getUserGuid("testing@testmail.com");
		Assert.assertNull(userGuid);
	}

	@Test
	public void populateCustomerDetails_internalServerError() {
		Mockito.when(userHelper.getWebClientFromSveLocator(Mockito.anyString(), Mockito.anyList(), Mockito.anyBoolean())).thenReturn(webClient);
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
		Mockito.when(webClient.get()).thenReturn(getResponse(500));
		
		String userGuid = transferValidator
				.getUserGuid("testing@testmail.com");
		Assert.assertNull(userGuid);
	}
	
	@Test
	public void populateCustomerDetails_exceptionCase() {
		Mockito.when(userHelper.getWebClientFromSveLocator(Mockito.anyString(), Mockito.anyList(), Mockito.anyBoolean())).thenReturn(webClient);
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		Mockito.when(webClient.getConfig(webClient)).thenReturn(clientConfiguration);
		Mockito.doThrow(Exception.class).when(webClient).get();
		String userGuid = transferValidator
				.getUserGuid("testing@testmail.com");
		Assert.assertNull(userGuid);
	}
	

	private Response getResponse(final int status) {
		Response response = new Response() {

			@Override
			public int getStatus() {
				return status;
			}

			@Override
			public MultivaluedMap<String, Object> getMetadata() {
				return null;
			}

			@Override
			public Object getEntity() {
				return null;

			}
		};
		return response;
	}
	
	
	
	private Response getResponse200() {
		Response response = new Response() {

			@Override
			public Object getEntity() {
				String response = "{\"customer\":{\"userCookieGuid\":\"C77991557A355E14E04400212861B256\"}}";
				return new ByteArrayInputStream(response.getBytes());
			}

			@Override
			public int getStatus() {
				return 200;
			}

			@Override
			public MultivaluedMap<String, Object> getMetadata() {
				return null;
			}
		};
		return response;
	}
}