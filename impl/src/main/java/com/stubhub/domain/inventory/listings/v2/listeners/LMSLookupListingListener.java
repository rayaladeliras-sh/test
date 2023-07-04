package com.stubhub.domain.inventory.listings.v2.listeners;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.datamodel.dao.ListingDAO;
import com.stubhub.domain.inventory.datamodel.entity.Listing;

@Component("lmsLookupListener")
public class LMSLookupListingListener implements MessageListener {

  private final static Logger logger = LoggerFactory.getLogger(LMSLookupListingListener.class);

  private static final String LOG_PREFIX = "_domain=domaininventory";

  @Autowired
  private InventoryMgr inventoryManager;
  
  @Autowired
  @Qualifier("listingDAO")
  private ListingDAO listingDAO;
  
  @Autowired
  @Qualifier(value = "lmsFormMessageTemplate")
  private JmsTemplate lmsFormMessageTemplate;  
  
  @Override
  @Transactional
  public void onMessage(Message message) {

    SHMonitor monitor = SHMonitorFactory.getMonitor().start();
    Long listingId = null;
    String externalListingId = null;
    String section = null;
    String row = null;
    String seats = null;
    Integer quantity = null;
    Long sellerId = null;
    Long eventId = null;
    try {
        MapMessage mapMessage = (MapMessage) message;
        listingId = mapMessage.getLong("listingId");
        String lmsContentXml= mapMessage.getString("lmsContentXml");
        logger.info("{} {} _operation=lmsLookup _status=START listingId={}",
                new Object[] {SHMonitoringContext.get(), LOG_PREFIX, listingId});
            Listing listing = inventoryManager.getListing(listingId);
            if (listing != null) {
            	section=listing.getSection();
            	quantity=listing.getQuantity();
            	sellerId=listing.getSellerId();
            	eventId=listing.getEventId();
            	row=listing.getRow();
            	seats=listing.getSeats();
            	if (listing.getExternalId()!=null){
                  	externalListingId=listing.getExternalId();	            	
	          	  	List<Listing> getOldLMSListing = listingDAO.findLMSListing(eventId, sellerId,section,row);
	          	  	if(!getOldLMSListing.isEmpty()){
		          	  	logger.info("The number of Listings was approved earleir for this Seller & this SRS, Size={} ",getOldLMSListing.size());
	          	  		for (Listing ticket :getOldLMSListing){
	          	  			if (compareLMSListing(ticket,quantity,section,row,seats)){
	    	          	  		logger.info("_message=\"There is existing LMS listing was approved earlier for the externalListingId={}, sellerId={}, eventId={}, section={}, row={}"
	    		        		   		+ " so updating new LMS listing listingId={} to Approve\"",externalListingId,sellerId,eventId,section,row,listingId);
	    		          	  		listingDAO.updateTicketLMSApprovalStatus(listingId);
	    		          	  	monitor.stop();
	    		          	  	return;
	          	  			}
	          	  		}
	          	  		sendCreateLMSListingMessageToOldQueue(listingId, externalListingId, section, row, sellerId, eventId, lmsContentXml);

	          	  	}else{
	          	  		sendCreateLMSListingMessageToOldQueue(listingId, externalListingId, section, row, sellerId, eventId, lmsContentXml);
	          	  	}
	          	  	monitor.stop();
            	}else{
          	  		monitor.stop();
          	  		logger.info("_message=\"There is no externalListingId in current Listing listingId={}, sellerId={}, eventId={} "
  	    		   		+ "hence not checking, sending message directly to old LMS queue\"",listingId,sellerId,eventId);
          	  		sendCreateLMSListingMessageToOldQueue(lmsContentXml, listingId);
          	  	}
            }else{
              monitor.stop();
              logger.error(
                  "{} {} _operation=lmsLookup _message=\"{}\" _status=BAD_REQUEST listingId={} _respTime={}",
                  new Object[] {SHMonitoringContext.get(), LOG_PREFIX,
                      "Either the listing is invalid or no tickets available in this listing to perform LMS lookup opertion",
                      listingId, monitor.getTime()});
              return;
            }
     } catch (Exception e) {
      monitor.stop();
      logger.error(
          "{} {} _operation=LMSListingLookup _message=\"{}\" _status=ERROR listingId={} _respTime={}",
          new Object[] {SHMonitoringContext.get(), LOG_PREFIX, e, listingId,
              monitor.getTime()});
    }
  }

private void sendCreateLMSListingMessageToOldQueue(Long listingId, String externalListingId, String section, String row, Long sellerId, Long eventId,
		String lmsContentXml) {
	logger.info("_message=\"There is no existing LMS listing for the externalListingId={}, sellerId={}, eventId={}, section={}, row={} "
		+ "so sending message to old LMS queue for listingId={}\"",externalListingId,sellerId,eventId,section,row,listingId);
	sendCreateLMSListingMessageToOldQueue(lmsContentXml, listingId);
}
  
  public void sendCreateLMSListingMessageToOldQueue(final String lmsContentXml, final Long listingId) {
    try {
    	logger.debug("Sending LMS listing message to lmsform_message listingId={} lmsContentXml={}", listingId, lmsContentXml);
        lmsFormMessageTemplate.send(new MessageCreator() {
        	public Message createMessage(javax.jms.Session session) throws JMSException {        		
        		Message message = session.createTextMessage(lmsContentXml);
        		return message;
        	}
        });
        logger.debug("Sent LMS listing message to lmsform_message listingId={} lmsContentXml={}", listingId, lmsContentXml );    
    }catch(Exception e) {
    	logger.error("Error while sending LMS listing  message listingId ={} error={}" + listingId, e);
      }
  }
  
  public boolean compareLMSListing(Listing oldLMSListing, Integer quantity, String section, String row, String seats){
	  boolean flag = false;
		  if (oldLMSListing.getQuantity()>=quantity){
			  if (oldLMSListing.getSection()!=null && oldLMSListing.getSection().toString().equalsIgnoreCase(section)){
				  if (oldLMSListing.getRow()!=null && oldLMSListing.getRow().toString().equalsIgnoreCase(row)){
					  if (oldLMSListing.getSeats()!=null && oldLMSListing.getSeats().toString().contains(seats)){
						  flag = true;
					  }
				  }
			  }
		  }
	return flag;
  }
}
