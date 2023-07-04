package com.stubhub.domain.inventory.listings.v2.listeners;

import com.stubhub.domain.inventory.datamodel.dao.BusinessDAO;
import com.stubhub.domain.inventory.datamodel.dao.ListingDAO;
import com.stubhub.domain.inventory.datamodel.entity.Business;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

public class updateListingBusinessListener implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(updateListingBusinessListener.class);
    public static final String ACTION_RESET = "RESET";
    public static final String ACTION_ADD = "ADD";

    @Autowired
    private ListingDAO listingDAO;

    @Autowired
    private BusinessDAO businessDAO;

    private int batchSize=MasterStubHubProperties.getPropertyAsInt("inventory.update.business.batchSize", 500);

    @Transactional
    public void onMessage(Message msg) {
        LOGGER.info("Update listing business message received");
        StopWatch stopWatch=new StopWatch();
        String userId=null;
        try {
            MapMessage mapMessage = (MapMessage) msg;
            userId= mapMessage.getString("userId");
            String action = mapMessage.getString("action");
            LOGGER.info("Processing update listing business message userId={} , action={}",userId,action);
            if (null == userId || null == action) {
                return;
            }

            stopWatch.start();
            Long sellerId=Long.valueOf(userId);
            int maxCount=listingDAO.checkUpdateCountForBusiness(sellerId);
            LOGGER.info("Seller active listing count ={},userId={}",maxCount,userId);
            int recordUpdated = 0;
            int batchUpdated=0;
            int batchCount=0;
            if (ACTION_RESET.equalsIgnoreCase(action)) {
                do {
                    batchUpdated = listingDAO.resetBusinessInfo(sellerId,batchSize);
                    recordUpdated+=batchUpdated;
                    batchCount++;
                }while(batchUpdated!=0&&recordUpdated<maxCount);
            } else if (ACTION_ADD.equalsIgnoreCase(action)) {
                Business business = businessDAO.findByOwnerId(sellerId);
                if (null != business) {
                    Long businessId = business.getId();
                    String bussinessGuid = business.getBusinessGuid();
                    do {
                        batchUpdated = listingDAO.updateBusinessInfo(sellerId, businessId, bussinessGuid,batchSize);
                        recordUpdated+=batchUpdated;
                        batchCount++;
                    }while (batchUpdated!=0&&recordUpdated<maxCount);
                }
            } else {
                stopWatch.stop();
                LOGGER.info("Update listing business info message ignored due to incorrect action={} for userId={}", action, userId);
                return;
            }

            stopWatch.stop();
            LOGGER.info("Updated user listing business info successfully for userId={},recordUpdated={},batchCount={},timeElapsed={}", userId, recordUpdated,batchCount, stopWatch.getLastTaskTimeMillis());
        }catch (Exception e){
            LOGGER.error("Got exception when processing updateListingBusinessListener for user:{} exception:-{}",userId,e);
        }
    }
}
