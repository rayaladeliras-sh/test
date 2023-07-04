/**
 * 
 */
package com.stubhub.domain.inventory.listings.v2.bulk.util;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr;
import com.stubhub.domain.inventory.datamodel.entity.BulkListingGroup;

/**
 * @author sjayaswal
 *
 */
@Component("bulkListingJobProducer")
@Configurable
public class BulkListingJobProducer implements Runnable{

	@Autowired
	private BulkInventoryMgr bulkInventoryMgr;
	
	
	private final static Logger log = Logger.getLogger(BulkListingJobProducer.class);
	private static final int MAXQUEUE=3;
	
	private Vector<Long> groups = new Vector<Long>();
	
    public void run() {
		log.info("BulkListingJobProducer STARTED");
		while(true){
	        try {
    			log.debug("################## Polling BulkListingJobProducer ##################");
    			List<BulkListingGroup> allAvailableGroups =bulkInventoryMgr.getAvailableGroups();
    			
    			if(allAvailableGroups!=null){
    				log.info("available bulkListingGroup.size()="+allAvailableGroups.size());
    				for(BulkListingGroup group:allAvailableGroups){
    					putGroup(group.getBulkListingGroupId());
    				}
    			}
    			Thread.sleep(1000);
	    		
	        } catch (InterruptedException e) {
	        	log.error("InterruptedException on BulkListingJobProducer.",e);
	        } catch (Throwable t){
				log.error("Unknown system error on BulkListingJobProducer.",t);
			}
		}
    }
 
    private synchronized void putGroup(Long groupId) throws InterruptedException {
        while (groups.size() == MAXQUEUE) {
            wait();
        }
        log.debug("adding to BulkListingJobProducer groupId"+groupId);
        groups.addElement(groupId);
        notifyAll();
        
    }
 

    public synchronized Long getGroup() throws InterruptedException {
    	
    	while (groups.size() == 0) {
        	 log.debug("waiting to get the Group");
            wait();
        }
        Long message = (Long) groups.firstElement();
        log.info("Processing groupId="+message);
        groups.removeElement(message);
        notifyAll();
        return message;
    }
}
