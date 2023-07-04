package com.stubhub.domain.inventory.biz.v2.impl.util;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;

@Configuration
//@EnableScheduling
@Component
public class InventoryServiceMonitor {
	private final static Logger log = Logger
			.getLogger(InventoryServiceMonitor.class);
	public boolean inventoryServiceHealth = true;

	//@Scheduled(cron = "0 0/5 * * * ?")
	public void execute() {
		boolean status = true;
		
		status = getClient(getProperty("CATALOG_DOMAIN_WADL",
				"http://api-int.stubprod.com/catalog/events/v1?_wadl"));

		status = status
				&& getClient(getProperty("PAYMENT_DOMAIN_WADL",
						"http://api-int.stubprod.com/payment/payableInstrumentType/v1?_wadl"));
		status = status
				&& getClient(getProperty("USER_DOMAIN_WADL",
						"http://api-int.stubprod.com/user/customers/v1?_wadl"));
		status = status
				&& getClient(getProperty("PRICING_DOMAIN_WADL",
						"http://api-int.stubprod.com/sinventory/pricingengine/v1?_wadl"));
		status = status
				&& getClient(getProperty("FULFILMENT_GEN31_DOMAIN_WADL",
						"http://api-int.stubprod.com/fulfillmentapi/service?_wadl"));
		status = status
				&& getClient(getProperty("SEARCH_DOMAIN_WADL",
						"http://api-int.stubprod.com/search/catalog/events/v1?_wadl"));

		inventoryServiceHealth = status;
		log.info("InventoryServiceHealth status=" + status);
	}

	protected String getProperty(String property, String defaultValue) {
		return MasterStubHubProperties.getProperty(property, defaultValue);
	}

	protected WebClient getWebClient(String uri) {
		try{
			SHAPIThreadLocal shapithreadLocal = new SHAPIThreadLocal();
			SHAPIContext apiContext = new SHAPIContext();
			shapithreadLocal.set(apiContext);
			WebClient webClient = WebClient.create(uri);
			return webClient;
		}catch(Exception e){
			return null;
		}
	}

	private boolean getClient(String uri) {
		WebClient webClient = getWebClient(uri);
		if(webClient == null) return false;
		webClient.accept(MediaType.APPLICATION_XML);
		Response response = webClient.get();
		if (response == null)
			return false;
		try {
			((InputStream) response.getEntity()).close();
		} catch (IOException e) {
			log.error("InventoryServiceHealth error", e);
		}
		log.info("InventoryServiceHealth check status=" + response.getStatus()
				+ "  URI" + uri);
		if (response.getStatus() != 200) {
			return false;
		}
		return true;
	}

}
