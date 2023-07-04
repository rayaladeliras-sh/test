/**
 * 
 */
package com.stubhub.domain.inventory.listings.v2.helper;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.stubhub.domain.inventory.common.util.SolrJsonUtil;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

import junit.framework.Assert;

/**
 * @author sjayaswal
 *
 */
public class ListingCatalogSolrUtilTest {

	List<ListingRequest> listings;
	private SvcLocator svcLocator;
	private WebClient webClient;
	private ListingCatalogSolrUtil listingCatalogSolrUtil;
	private SolrJsonUtil solrJsonUtil;
	private MasterStubhubPropertiesWrapper masterStubhubProperties;

	@BeforeTest
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		svcLocator = Mockito.mock(SvcLocator.class);
		webClient = Mockito.mock(WebClient.class);
		listingCatalogSolrUtil = new ListingCatalogSolrUtil();
		listingCatalogSolrUtil = new ListingCatalogSolrUtil() {
			protected String getProperty(String propertyName, String defaultValue) {
				return "";
			}
		};

		solrJsonUtil = new SolrJsonUtil() {
			protected String getProperty(String propertyName, String defaultValue) {
				return "";
			}
		};
		Mockito.when(svcLocator.locate(Mockito.anyString())).thenReturn(webClient);
		ReflectionTestUtils.setField(solrJsonUtil, "svcLocator", svcLocator);
		ReflectionTestUtils.setField(listingCatalogSolrUtil, "svcLocator", svcLocator);
		ReflectionTestUtils.setField(listingCatalogSolrUtil, "jsonUtil", solrJsonUtil);

		listings = new ArrayList<ListingRequest>();
		ListingRequest listing = new ListingRequest();
		listing.setEventId("123456789");
		listing.setExternalListingId("98765");
		listing.setListingId(123456L);
		listing.setPricePerProduct(new Money("100"));
		listing.setSection("section");
		List<Product> products = new ArrayList<Product>();
		Product product = new Product();
		product.setRow("row1");
		product.setSeat("seat1");
		products.add(product);
		product = new Product();
		product.setRow("row1");
		product.setSeat("seat2");
		products.add(product);
		listing.setProducts(products);
		listings.add(listing);

		listing = new ListingRequest();
		listing.setEventId("123456789");
		listing.setExternalListingId("987651");
		listing.setListingId(1234567L);
		listing.setPricePerProduct(new Money("100"));
		listing.setSection("section");
		products = new ArrayList<Product>();
		product = new Product();
		product.setRow("row2");
		product.setSeat("seat2");
		products.add(product);
		product = new Product();
		product.setRow("row3");
		product.setSeat("seat3");
		products.add(product);
		listing.setProducts(products);
		listings.add(listing);

	}

	private List<ListingRequest> getListingRequests(int count) {
		List<ListingRequest> listingRequests = new ArrayList<ListingRequest>();

		for (int i = 0; i < count; i++) {
			ListingRequest listing = new ListingRequest();
			listing.setEventId("123456789");
			listing.setExternalListingId("98765" + i);
			listing.setListingId(new Long(123456 + i));
			listing.setPricePerProduct(new Money("100"));
			listing.setSection("section");
			List<Product> products = new ArrayList<Product>();
			Product product = new Product();
			product.setRow("row1" + i);
			product.setSeat("seat1" + i);
			products.add(product);
			product = new Product();
			product.setRow("row1" + i);
			product.setSeat("seat2" + i);
			products.add(product);
			listing.setProducts(products);
			listingRequests.add(listing);
		}

		return listingRequests;
	}

	@Test
	public void testGetEventByListingId() throws Exception {
		Long sellerId = 19L;
		Response response = new Response() {

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
				String response = "{\"response\": {\"numFound\": 5,\"docs\": [{\"id\" : \"123456\" , \"eventId\":\"23423423\", \"externalListingId\":\"98765\"}, "
						+ " {\"ticketId\" : \"10\" , \"eventId\":\"23423423\", \"externalListingId\":\"98710\"}]}}";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		Mockito.when(webClient.post(Mockito.anyObject())).thenReturn(response);
		masterStubhubProperties = Mockito.mock(MasterStubhubPropertiesWrapper.class);
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("solr.cloud.api.generic.url");

		List<Long> missingIds = listingCatalogSolrUtil.getEventByListingId(listings, sellerId);
		Assert.assertNotNull(missingIds);
	}

	@Test
	public void testGetEventByListingIdBatch600() throws Exception {
		Long sellerId = 19L;
		Response response = new Response() {

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
				String response = "{\"response\": {\"numFound\": 5,\"docs\": [{\"id\" : \"123456\" , \"eventId\":\"23423423\", \"externalListingId\":\"98765\"}, "
						+ " {\"ticketId\" : \"10\" , \"eventId\":\"23423423\", \"externalListingId\":\"98710\"}]}}";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		Mockito.when(webClient.post(Mockito.anyObject())).thenReturn(response);
		masterStubhubProperties = Mockito.mock(MasterStubhubPropertiesWrapper.class);
		List<ListingRequest> listings = getListingRequests(600);
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("solr.cloud.api.generic.url");

		List<Long> missingIds = listingCatalogSolrUtil.getEventByListingId(listings, sellerId);
		Assert.assertNotNull(missingIds);
	}

	@Test
	public void testGetEventByListingIdEmptyDocs() throws Exception {
		Long sellerId = 19L;
		Response response = new Response() {

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
				String response = "{\"response\": {\"numFound\": 5,\"docs\": []}}";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		Mockito.when(webClient.post(Mockito.anyObject())).thenReturn(response);
		masterStubhubProperties = Mockito.mock(MasterStubhubPropertiesWrapper.class);
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("solr.cloud.api.generic.url");

		List<Long> missingIds = listingCatalogSolrUtil.getEventByListingId(listings, sellerId);
		Assert.assertNotNull(missingIds);
	}

	@Test
	public void testGetEventByListingIdNullResponse() throws Exception {
		Long sellerId = 19L;
		Response response = new Response() {

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
				String response = null;
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		Mockito.when(webClient.post(Mockito.anyObject())).thenReturn(response);
		masterStubhubProperties = Mockito.mock(MasterStubhubPropertiesWrapper.class);
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("solr.cloud.api.generic.url");

		List<Long> missingIds = listingCatalogSolrUtil.getEventByListingId(listings, sellerId);
		Assert.assertNotNull(missingIds);
	}

	// SELLAPI-1181 09/10/15 START
	@Test
	public void testGetEventByExternalId() throws Exception {
		Long sellerId = 19L;
		Response response = new Response() {

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
				String response = "{\"response\": {\"numFound\": 5,\"docs\": [{\"id\" : \"123456\" , \"eventId\":\"23423423\", \"externalListingId\":\"98765\"}, "
						+ " {\"ticketId\" : \"10\" , \"eventId\":\"23423423\", \"externalListingId\":\"98710\"}]}}";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		Mockito.when(webClient.post(Mockito.anyObject())).thenReturn(response);

		List<String> missingIds = listingCatalogSolrUtil.getEventByExternalId(sellerId, listings);
		Assert.assertNotNull(missingIds);
	}

	@Test
	public void testGetEventByExternalIdBatch600() throws Exception {
		Long sellerId = 19L;
		Response response = new Response() {

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
				String response = "{\"response\": {\"numFound\": 5,\"docs\": [{\"ticketId\" : \"123456\" , \"eventId\":\"23423423\", \"externalListingId\":\"98765\"}, "
						+ " {\"ticketId\" : \"10\" , \"eventId\":\"23423423\", \"externalListingId\":\"98710\"}]}}";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		Mockito.when(webClient.post(Mockito.anyObject())).thenReturn(response);
		List<ListingRequest> listings = get600ExternalIdList();

		List<String> missingIds = listingCatalogSolrUtil.getEventByExternalId(sellerId, listings);
		Assert.assertNotNull(missingIds);
	}

	@Test
	public void testGetEventByExternalIdThrowException() throws Exception {
		Long sellerId = 19L;
		Response response = new Response() {

			@Override
			public int getStatus() {
				return 404;
			}

			@Override
			public MultivaluedMap<String, Object> getMetadata() {
				return null;
			}

			@Override
			public Object getEntity() {
				String response = "{\"response\": {\"numFound\": 2,\"docs\": []}}";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		Mockito.when(webClient.post(Mockito.anyObject())).thenReturn(response);

		try {
			List<String> missingIds = listingCatalogSolrUtil.getEventByExternalId(sellerId, listings);
			Assert.assertNotNull(missingIds);
		} catch (Throwable th) {
		}
	}

	@Test
	public void testGetEventByListingIdThrowException() throws Exception {
		Long sellerId = 19L;
		Response response = new Response() {

			@Override
			public int getStatus() {
				return 404;
			}

			@Override
			public MultivaluedMap<String, Object> getMetadata() {
				return null;
			}

			@Override
			public Object getEntity() {
				String response = "{\"response\": {\"numFound\": 2,\"docs\": []}}";
				return new ByteArrayInputStream(response.getBytes());
			}
		};
		Mockito.when(webClient.post(Mockito.anyObject())).thenReturn(response);

		try {
			List<Long> missingIds = listingCatalogSolrUtil.getEventByListingId(listings, sellerId);
			Assert.assertNotNull(missingIds);
		} catch (Throwable th) {
		}
	}

	private List<ListingRequest> get600ExternalIdList() {
		List<ListingRequest> externalIdList = new ArrayList<ListingRequest>();
		for (Long i = 0L; i <= 600; i++) {
			ListingRequest lr = new ListingRequest();
			lr.setListingId(i);
			lr.setEventId(i + "Event");
			lr.setExternalListingId(i + "");
			externalIdList.add(lr);

		}
		return externalIdList;
	}
	// SELLAPI-1181 09/10/15 END

	@Test
	public void testGetProperty() throws Exception {
		masterStubhubProperties = Mockito.mock(MasterStubhubPropertiesWrapper.class);
		Mockito.when(masterStubhubProperties.getProperty(Mockito.anyString(), Mockito.anyString()))
				.thenReturn("solr.cloud.api.generic.url");
		String property = listingCatalogSolrUtil.getProperty("solr.cloud.api.generic.url", "");
		Assert.assertNotNull(property);
	}
}
