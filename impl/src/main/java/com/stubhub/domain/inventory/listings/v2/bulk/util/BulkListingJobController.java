/**
 * 
 */
package com.stubhub.domain.inventory.listings.v2.bulk.util;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.newplatform.property.MasterStubHubProperties;


/**
 * @author sjayaswal
 *
 */
@Component("bulkListingController")
public class BulkListingJobController  {

	@Autowired
	BulkListingJobProducer producer;
	
	@Autowired
	BulkListingJobConsumer consumer;
	
	private final static Logger log = Logger.getLogger(BulkListingJobController.class);
	
//	@PostConstruct
	public void init() {
		Thread t = new Thread(producer);
		t.start();
		
		int numberOfConsumers = MasterStubHubProperties.getPropertyAsInt("bulk.listing.consumer.size", 5);
		for(int i=1;i<=numberOfConsumers;i++){
			Thread consumerThread = new Thread(consumer);
			log.info("Starting BulkListingJobConsumer ThreadNo="+i);
			consumerThread.start();
		}
		
	}	
	
}
