package com.stubhub.domain.inventory.listings.v2.newflow.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.UpdateListingEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.task.Task;

@Component
public class UpdateListingTaskFactory {
  
    private final static Logger log = LoggerFactory.getLogger(UpdateListingTaskFactory.class);
  
	@Autowired
	BeanFactory beanFactory;

	// Get the tasks for each update type
	@SuppressWarnings("unchecked")
	public List<Task<ListingDTO>> getTasks(UpdateListingEnum updateListingEnum, ListingDTO listingDTO) {
		List<Task<ListingDTO>> tasks =  new ArrayList<Task<ListingDTO>>();
		log.info("List of tasks for updating listing listingID={} taskList={}", listingDTO.getDbListing().getId(), updateListingTaskMap.get(updateListingEnum));
		for(String taskName : updateListingTaskMap.get(updateListingEnum)){
			tasks.add((Task<ListingDTO>) beanFactory.getBean(taskName, listingDTO));
		}
		
		return tasks;
	}

	
	// Enum -> List of Tasks Map
	private static Map<UpdateListingEnum, List<String>> updateListingTaskMap = new HashMap<UpdateListingEnum, List<String>>();
	static {
		updateListingTaskMap.put(UpdateListingEnum.DELETE_LISTING, getTaskNames(UpdateListingEnum.DELETE_LISTING, "deleteListingTask"));
		updateListingTaskMap.put(UpdateListingEnum.CONTACT_ID, getTaskNames(UpdateListingEnum.CONTACT_ID, "updateContactTask"));
		updateListingTaskMap.put(UpdateListingEnum.PAYMENT_TYPE, getTaskNames(UpdateListingEnum.PAYMENT_TYPE, "updatePaymentTypeTask"));
		updateListingTaskMap.put(UpdateListingEnum.CC_ID, getTaskNames(UpdateListingEnum.CC_ID, "updateCCTask"));
		updateListingTaskMap.put(UpdateListingEnum.DELIVERY_OPTION, getTaskNames(UpdateListingEnum.DELIVERY_OPTION, "updateDeliveryOptionTask"));
		updateListingTaskMap.put(UpdateListingEnum.SALE_END_DATE, getTaskNames(UpdateListingEnum.SALE_END_DATE, "updateSaleEndDateTask"));
		updateListingTaskMap.put(UpdateListingEnum.INHAND_DATE, getTaskNames(UpdateListingEnum.INHAND_DATE, "fulfillmentTask","updateInhandDateTask"));
		updateListingTaskMap.put(UpdateListingEnum.LMS_EXTENSION, getTaskNames(UpdateListingEnum.LMS_EXTENSION, "lmsExtensionTask"));
		updateListingTaskMap.put(UpdateListingEnum.LMS_APPROVAL_STATUS, getTaskNames(UpdateListingEnum.LMS_APPROVAL_STATUS, "updateLmsApprovalStatusTask"));
		updateListingTaskMap.put(UpdateListingEnum.QUANTITY, getTaskNames(UpdateListingEnum.QUANTITY, "updateQuantityTask"));
		updateListingTaskMap.put(UpdateListingEnum.SECTION, getTaskNames(UpdateListingEnum.SECTION, "updateSectionTask"));
		updateListingTaskMap.put(UpdateListingEnum.PREDELIVERY_BARCODE, getTaskNames(UpdateListingEnum.PREDELIVERY_BARCODE, "predeliverBarcodeListingTask"));
		updateListingTaskMap.put(UpdateListingEnum.PREDELIVERY_PDF, getTaskNames(UpdateListingEnum.PREDELIVERY_PDF, "predeliverPdfListingTask"));
		updateListingTaskMap.put(UpdateListingEnum.ADD_SEATS, getTaskNames(UpdateListingEnum.ADD_SEATS, "addSeatsTask"));
		updateListingTaskMap.put(UpdateListingEnum.UPDATE_SEATS, getTaskNames(UpdateListingEnum.UPDATE_SEATS, "updateSeatsTask"));
		updateListingTaskMap.put(UpdateListingEnum.DELETE_SEATS, getTaskNames(UpdateListingEnum.DELETE_SEATS, "deleteSeatsTask"));
		updateListingTaskMap.put(UpdateListingEnum.SPLIT_OPTION, getTaskNames(UpdateListingEnum.SPLIT_OPTION, "updateSplitTask"));
		updateListingTaskMap.put(UpdateListingEnum.SPLIT_QUANTITY, getTaskNames(UpdateListingEnum.SPLIT_QUANTITY, "updateSplitTask"));
		updateListingTaskMap.put(UpdateListingEnum.HIDESEATS_INDICATOR, getTaskNames(UpdateListingEnum.HIDESEATS_INDICATOR, "updateHideSeatsIndTask"));
		updateListingTaskMap.put(UpdateListingEnum.TICKET_TRAITS, getTaskNames(UpdateListingEnum.TICKET_TRAITS, "updateTicketTraitsTask"));
		updateListingTaskMap.put(UpdateListingEnum.INTERNAL_NOTES, getTaskNames(UpdateListingEnum.INTERNAL_NOTES, "updateTicketTraitsTask"));
		updateListingTaskMap.put(UpdateListingEnum.COMMENTS, getTaskNames(UpdateListingEnum.COMMENTS, "updateTicketTraitsTask"));
		updateListingTaskMap.put(UpdateListingEnum.PRICE, getTaskNames(UpdateListingEnum.PRICE, "updatePricingTask"));
		updateListingTaskMap.put(UpdateListingEnum.FACE_VALUE, getTaskNames(UpdateListingEnum.FACE_VALUE, "updateFaceValueTask"));
		updateListingTaskMap.put(UpdateListingEnum.STATUS, getTaskNames(UpdateListingEnum.STATUS, "updateStatusTask"));
	}
	
	// Task names for each Enum
	private static List<String> getTaskNames(UpdateListingEnum updateListingEnum, String... taskNames) {
		return Arrays.asList(taskNames);
	}
}
