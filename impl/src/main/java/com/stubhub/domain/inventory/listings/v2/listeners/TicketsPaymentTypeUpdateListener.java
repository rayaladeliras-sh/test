package com.stubhub.domain.inventory.listings.v2.listeners;

import java.util.List;
import java.util.Map;

import com.stubhub.domain.inventory.listings.v2.seller.SpiConvertTrackingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import com.stubhub.domain.inventory.datamodel.entity.SellerSpiConvertTrackingDO;
import com.stubhub.domain.inventory.datamodel.dao.SellerSpiConvertTrackingDAO;
import com.stubhub.domain.inventory.datamodel.dao.ListingDAO;

@Component("ticketPaymentTypeUpdateListener")
public class TicketsPaymentTypeUpdateListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(TicketsPaymentTypeUpdateListener.class);
	private static final Long DEFAULT_BATCH_PROCESS_SIZE = Long.valueOf(10L);

	private static final String CONVERT_TRACKING_ID = "convertTrackingId";
	private static final String BATCH_PROCESS_SIZE = "batchSize";

	@Autowired
	@Qualifier("inventorySellerSpiConvertTrackingDAOImpl")
	private SellerSpiConvertTrackingDAO sellerSpiConvertTrackingDAO;

	@Autowired
	@Qualifier("listingDAO")
	private ListingDAO listingDAO;

	@Transactional
	public void onMessage(Map<String, Long> messageMap) {
		Long convertTrackingId = messageMap.get(CONVERT_TRACKING_ID);
		Long batchProcessSize = messageMap.get(BATCH_PROCESS_SIZE);
		LOGGER.info(
				"_message=\"start to consume the ticket payment type update message for convert_tracking_id:{}, batchProcessSize:{}\"",
				convertTrackingId, batchProcessSize);
	
		Assert.notNull(convertTrackingId);
		Assert.isTrue(convertTrackingId.compareTo(Long.valueOf(0)) > 0);
		
		batchProcessSize = (batchProcessSize == null || batchProcessSize.compareTo(Long.valueOf(0L)) < 0) ? DEFAULT_BATCH_PROCESS_SIZE : batchProcessSize;

		SellerSpiConvertTrackingDO sellerSpiConvertTrackingDO = sellerSpiConvertTrackingDAO
				.loadSellerSpiConvertTrackingDOById(convertTrackingId);
		
		Assert.notNull(sellerSpiConvertTrackingDO.getSellerId());
		Assert.isTrue(sellerSpiConvertTrackingDO.getSellerId().compareTo(Long.valueOf(0))>0);
		Assert.notNull(sellerSpiConvertTrackingDO.getSellerPaymentTypeOldId());
		Assert.isTrue(sellerSpiConvertTrackingDO.getSellerPaymentTypeOldId().compareTo(Long.valueOf(0)) > 0);
		Assert.notNull(sellerSpiConvertTrackingDO.getSellerPaymentTypeNewId());
		Assert.isTrue(sellerSpiConvertTrackingDO.getSellerPaymentTypeNewId().compareTo(Long.valueOf(0)) > 0);
		
		List<Long> listingIdsToBeProcessed = 
				listingDAO.getListingIdBySellerId(sellerSpiConvertTrackingDO.getSellerId(), sellerSpiConvertTrackingDO.getSellerPaymentTypeOldId());

		if( null == listingIdsToBeProcessed || listingIdsToBeProcessed.isEmpty() )
		{
			LOGGER.info(
					"_message=\"There are no listing tickets for the sellerId:{}, sellerPaymentTypeOldId:{}, convert_tracking_id:{}\"",
					sellerSpiConvertTrackingDO.getSellerId(), sellerSpiConvertTrackingDO.getSellerPaymentTypeOldId(), convertTrackingId);
			sellerSpiConvertTrackingDAO.updateOrderPaymentTypeProcessStatus(Long.valueOf(5L), convertTrackingId);
			return;
		}
		
		int batchTimes = (int) (listingIdsToBeProcessed.size() / batchProcessSize) + 1;
		int exceptionTimes = 0;
		boolean isLast = false;
		for (int times = 0; times < batchTimes; times++) {
			try {
				int startIndex = (int) (times * batchProcessSize);
				// the case that the listingIdsToBeProcessed.size() can be
				// divided
				// by batchProcessSize
				if (startIndex == listingIdsToBeProcessed.size()) {
					break;
				}
				int toIndex = (int) ((times + 1) * batchProcessSize);
				// the case that process the final batch which range less than
				// the
				// batchProcessSize;
				if (toIndex > listingIdsToBeProcessed.size()) {
					toIndex = listingIdsToBeProcessed.size();
					isLast = true;
				}
				List<Long> subListToBeProcessed = listingIdsToBeProcessed.subList(startIndex, toIndex);
				LOGGER.info("_message=\"start to process batch for listingIds:{}\"", subListToBeProcessed);
				int processedSize = listingDAO.updateListingPaymentTypeByListingIds(sellerSpiConvertTrackingDO.getSellerPaymentTypeNewId(),
						sellerSpiConvertTrackingDO.getSellerPaymentTypeOldId(), subListToBeProcessed);

				// verify the result
				if ((!isLast && (processedSize == batchProcessSize))
						|| (isLast && processedSize == (toIndex - startIndex))) {
					LOGGER.info("_message=\"all the payment type id has been updated in this batch.\"");
					continue;
				}
				LOGGER.info("_message=\"there is some payment type id not been updated in this batch.\"");
			} catch (Throwable t) {
				LOGGER.info("_message=\"exception:{} caught for this batch.\"", t.getMessage());
				exceptionTimes ++;
			}
		}
		Long processStatus = Long.valueOf(2L);
		
		//if all the batch has an exception thrown then no records has been affected.
		if( (!isLast && exceptionTimes == (batchTimes - 1) ) || (isLast && exceptionTimes == batchTimes))
		{
			LOGGER.info("_message=\"All the batch update failed for this message.\"");
			processStatus = Long.valueOf(4L);
		} else 
		{
			processStatus = Long.valueOf(3L);
		}
		
		sellerSpiConvertTrackingDAO.updateOrderPaymentTypeProcessStatus(processStatus, convertTrackingId);

	}
  
  @Transactional
  public void taskAfterKafkaMessage(SpiConvertTrackingMessage spiConvertTrackingMessage) {
    Long convertTrackingId = spiConvertTrackingMessage.getConvertTrackingId();
    Long batchProcessSize = spiConvertTrackingMessage.getBatchSize();
    
    LOGGER.info(
        "_message=\"start to consume the ticket payment type update message for convert_tracking_id:{}, batchProcessSize:{}\"",
        convertTrackingId, batchProcessSize);
    
    Assert.notNull(convertTrackingId);
    Assert.isTrue(convertTrackingId.compareTo(Long.valueOf(0)) > 0);
    
    batchProcessSize = (batchProcessSize == null || batchProcessSize.compareTo(Long.valueOf(0L)) < 0) ? DEFAULT_BATCH_PROCESS_SIZE : batchProcessSize;
    
    SellerSpiConvertTrackingDO sellerSpiConvertTrackingDO = sellerSpiConvertTrackingDAO
        .loadSellerSpiConvertTrackingDOById(convertTrackingId);
    LOGGER.info("Seller spi convert tracking, load spi convert tracking={}", sellerSpiConvertTrackingDO.toString());
    
    Assert.notNull(sellerSpiConvertTrackingDO.getSellerId());
    Assert.isTrue(sellerSpiConvertTrackingDO.getSellerId().compareTo(Long.valueOf(0))>0);
    Assert.notNull(sellerSpiConvertTrackingDO.getSellerPaymentTypeOldId());
    Assert.isTrue(sellerSpiConvertTrackingDO.getSellerPaymentTypeOldId().compareTo(Long.valueOf(0)) > 0);
    Assert.notNull(sellerSpiConvertTrackingDO.getSellerPaymentTypeNewId());
    Assert.isTrue(sellerSpiConvertTrackingDO.getSellerPaymentTypeNewId().compareTo(Long.valueOf(0)) > 0);
    
    List<Long> listingIdsToBeProcessed =
        listingDAO.getListingIdBySellerId(sellerSpiConvertTrackingDO.getSellerId(), sellerSpiConvertTrackingDO.getSellerPaymentTypeOldId());
    LOGGER.info("Seller spi convert tracking, get listing ids={}", listingIdsToBeProcessed);
    if( null == listingIdsToBeProcessed || listingIdsToBeProcessed.isEmpty() )
    {
      LOGGER.info(
          "_message=\"There are no listing tickets for the sellerId:{}, sellerPaymentTypeOldId:{}, convert_tracking_id:{}\"",
          sellerSpiConvertTrackingDO.getSellerId(), sellerSpiConvertTrackingDO.getSellerPaymentTypeOldId(), convertTrackingId);
      sellerSpiConvertTrackingDAO.updateOrderPaymentTypeProcessStatus(Long.valueOf(5L), convertTrackingId);
      return;
    }
    
    int batchTimes = (int) (listingIdsToBeProcessed.size() / batchProcessSize) + 1;
    int exceptionTimes = 0;
    boolean isLast = false;
    for (int times = 0; times < batchTimes; times++) {
      try {
        int startIndex = (int) (times * batchProcessSize);
        // the case that the listingIdsToBeProcessed.size() can be
        // divided
        // by batchProcessSize
        if (startIndex == listingIdsToBeProcessed.size()) {
          break;
        }
        int toIndex = (int) ((times + 1) * batchProcessSize);
        // the case that process the final batch which range less than
        // the
        // batchProcessSize;
        if (toIndex > listingIdsToBeProcessed.size()) {
          toIndex = listingIdsToBeProcessed.size();
          isLast = true;
        }
        List<Long> subListToBeProcessed = listingIdsToBeProcessed.subList(startIndex, toIndex);
        LOGGER.info("_message=\"start to process batch for listingIds:{}\"", subListToBeProcessed);
        int processedSize = listingDAO.updateListingPaymentTypeByListingIds(sellerSpiConvertTrackingDO.getSellerPaymentTypeNewId(),
            sellerSpiConvertTrackingDO.getSellerPaymentTypeOldId(), subListToBeProcessed);
        
        // verify the result
        if ((!isLast && (processedSize == batchProcessSize))
            || (isLast && processedSize == (toIndex - startIndex))) {
          LOGGER.info("_message=\"all the payment type id has been updated in this batch.\"");
          continue;
        }
        LOGGER.info("_message=\"there is some payment type id not been updated in this batch.\"");
      } catch (Throwable t) {
        LOGGER.info("_message=\"exception:{} caught for this batch.\"", t.getMessage());
        exceptionTimes ++;
      }
    }
    Long processStatus = Long.valueOf(2L);
    
    //if all the batch has an exception thrown then no records has been affected.
    if( (!isLast && exceptionTimes == (batchTimes - 1) ) || (isLast && exceptionTimes == batchTimes))
    {
      LOGGER.info("_message=\"All the batch update failed for this message.\"");
      processStatus = Long.valueOf(4L);
    } else
    {
      processStatus = Long.valueOf(3L);
    }
    LOGGER.info("Update spi convert tracking, processStatus={}", processStatus);
    sellerSpiConvertTrackingDAO.updateOrderPaymentTypeProcessStatus(processStatus, convertTrackingId);
  }
}
