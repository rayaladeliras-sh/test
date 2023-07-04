/**
 * 
 */
package com.stubhub.domain.inventory.listings.v2.bulk.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.biz.v2.intf.BulkInventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.common.entity.ErrorEnum;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.JsonUtil;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.BulkJob;
import com.stubhub.domain.inventory.datamodel.entity.BulkListingGroup;
import com.stubhub.domain.inventory.datamodel.entity.BulkListingRequest;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.BulkStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.BulkUploadType;
import com.stubhub.domain.inventory.listings.v2.helper.ListingCatalogSolrUtil;
import com.stubhub.domain.inventory.listings.v2.helper.ListingCreateProcess;
import com.stubhub.domain.inventory.listings.v2.util.ListingTextValidatorUtil;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkJobResponse;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkJobStatusRequest;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingInternal;
import com.stubhub.newplatform.common.util.DateUtil;
import com.stubhub.newplatform.property.MasterStubHubProperties;

/**
 * @author sjayaswal
 * 
 */
@Component("bulkListingHelper")
public class BulkListingHelper {

	public static final String ERROR_GROUP = "Error";
	
	private static final int CLOB_SIZE = 4000;

	private static final String HASHTAG_SEPARATOR = "#";

	private static final int DATABASE_LIMIT = 4000;

	private final static Logger log = LoggerFactory
			.getLogger(BulkListingHelper.class);

	private static final String MODULENAME_Bulk_Create_Listing = "BulkCreateListing";
	private static final String MODULENAME_Bulk_Update_Listing = "BulkUpdateListing";

	private static int MAX_ALLOWED_EXTERNAL_ID_LENGTH = 50;
	
	private static final String BULK_LISTING_JOB_STATUS_UPDATE_API = "BulkListingJobStatusUpdate";

	@Autowired
	private InventoryMgr inventoryMgr;

	@Autowired
	private BulkInventoryMgr bulkInventoryMgr;

	@Autowired
	private ListingCreateProcess listingCreateProcess;

	@Autowired
	private ListingCatalogSolrUtil listingCatalogSolrUtil;

	@Autowired
	@Qualifier(value = "bulkInventoryTemplate")
	private JmsTemplate jmsTemplate;

	@Autowired
	private BulkListingJobConsumer bulkListingJobConsumer;
	
	//SELLAPI-1092 7/08/15 START
	@Autowired
	ListingTextValidatorUtil listingTextValidatorUtil;
	//SELLAPI-1092 7/08/15 END

	public Long bulkCreateListing(
			Long sellerId,
			String sellerGuid,
			String subscriber,
			BulkStatus bulkStatus,
			com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingRequest createBulkListingRequest,
			String assertionHeader, HttpHeaders httpHeaders) {

		boolean isTemporaryJob = false;
		// Creating a job
		BulkJob bulkJob = new BulkJob();
		List<BulkJob> allJobs = bulkInventoryMgr.getAllJobsForSeller(sellerId);
		if (allJobs != null) {
			isTemporaryJob = true; // This seller has a job that is already
									// getting processed.
		}
		if (bulkStatus != null) {
			bulkJob.setBulkStatusId(bulkStatus.getId());
		} else if (isTemporaryJob) {
			bulkJob.setBulkStatusId(BulkStatus.TEMPORARY.getId());
		} else {
			bulkJob.setBulkStatusId(BulkStatus.CREATED.getId());
		}
		bulkJob.setUserId(sellerId);
		Calendar utcNow = DateUtil.getNowCalUTC();
		bulkJob.setCreatedDate(utcNow);
		bulkJob.setLastUpdatedDate(utcNow);
		bulkJob.setCreatedBy(MODULENAME_Bulk_Create_Listing);
		bulkJob.setLastUpdatedBy(MODULENAME_Bulk_Create_Listing);
		if (createBulkListingRequest != null
				&& createBulkListingRequest.getListings() != null
				&& !createBulkListingRequest.getListings().isEmpty()) {
			List<ListingRequest> createListingRequestList = createBulkListingRequest
					.getListings();

			bulkInventoryMgr.createJob(bulkJob);
			log.info("bulk job created. jobId:"+bulkJob.getBulkJobId() + " with status: "+bulkJob.getBulkStatusId());

			try {
				createGroups(
						sellerId,
						sellerGuid,
						subscriber,
						groupCreateListingRequestObjects(createListingRequestList,
								true), assertionHeader, bulkJob.getBulkJobId(),
						isTemporaryJob, httpHeaders, BulkUploadType.CREATE);				
			} catch (Exception e) {
				// SELLAPI-959 : Bulk Job ID not created when User send big request
				log.error(
						"Error while creating Groups. Aborting the request. Updating job status to ERROR for sellerId="
								+ sellerId + " jobId=" + bulkJob.getBulkJobId(),
						e);
				bulkJob.setBulkStatusId(BulkStatus.ERROR.getId());
				try {
					bulkInventoryMgr.updateJob(bulkJob);					
				} catch (Exception e2) {
					log.error(
							"Error while creating Groups. Aborting the request. Updating job status to ERROR for sellerId="
									+ sellerId
									+ " jobId="
									+ bulkJob.getBulkJobId(), e);
				}
				ListingError listingError = new ListingError(
						ErrorType.INPUTERROR,
						ErrorCode.INPUT_SIZE_EXCEEDED,
						"One of the listing requests' size exceeded max capacity",
						"inputListingRequestSize");
				throw new ListingBusinessException(listingError);
			}
		} else { // Empty payload
			log.error(
					"Error while creating Bulk Job. Payload has no listings. Aborting the request for sellerId="
							+ sellerId);
			ListingError listingError = new ListingError(
					ErrorType.INPUTERROR,
					ErrorCode.INPUT_LISTING_EMPTY,
					"No listings are present in the request",
					"inputListingsEmpty");
			throw new ListingBusinessException(listingError);
		}
		//TODO lets add a log statement with the job ID and its status
		return bulkJob.getBulkJobId();
	}

	private void createGroups(Long sellerId, String sellerGuid, String subscriber,
			Map<String, List<BulkListingRequest>> groupedRequestMap,
			String assertionHeader, Long bulkJobId, boolean isTemp,
			HttpHeaders httpHeaders, BulkUploadType bulkUploadType) {

		int groupSize = MasterStubHubProperties.getPropertyAsInt(
				"bulk.listing.group.batch.size", 50);
		//TODO the assertion header can be part of the JMS message, and we can store in DB (or generate one using the GW API) for temp jobs
		String combinedAssertionHeader = createAssertionHeader(assertionHeader,
				httpHeaders);
		List<BulkListingRequest> erroredBulkListingRequests = groupedRequestMap
				.get(ERROR_GROUP);
		if (erroredBulkListingRequests != null
				&& !erroredBulkListingRequests.isEmpty()) {
			BulkListingGroup group = createGroup(sellerId, sellerGuid, subscriber,
					combinedAssertionHeader, bulkJobId, bulkUploadType);
			for (BulkListingRequest request : erroredBulkListingRequests) {
				request.setBulkListingGroupId(group.getBulkListingGroupId());
				request.setCreatedBy(MODULENAME_Bulk_Create_Listing);
				request.setLastUpdatedBy(MODULENAME_Bulk_Create_Listing);
			}
			bulkInventoryMgr
					.createBulkListingRequests(erroredBulkListingRequests);
			Calendar utcNow = DateUtil.getNowCalUTC();
			updateBulkListingGroup(group, utcNow, BulkStatus.ERROR);
		}

		groupedRequestMap.remove(ERROR_GROUP);
		Collection<List<BulkListingRequest>> completeRequestList = groupedRequestMap
				.values();
		if (completeRequestList == null || completeRequestList.isEmpty()) {
			// The job has no other requests to be processed, Move the job to
			// error state
			BulkJob bulkJob = bulkInventoryMgr.getJobById(bulkJobId);
			updateJob(sellerId, BulkStatus.ERROR, bulkJob);
		}
		for (List<BulkListingRequest> requestList : completeRequestList) {
			if (requestList.size() > 0 && requestList.size() <= groupSize) {
				BulkListingGroup group = createGroup(sellerId, sellerGuid, subscriber,
						combinedAssertionHeader, bulkJobId, bulkUploadType);
				for (BulkListingRequest request : requestList) {
					request.setBulkListingGroupId(group.getBulkListingGroupId());
					request.setCreatedBy(MODULENAME_Bulk_Create_Listing);
					request.setLastUpdatedBy(MODULENAME_Bulk_Create_Listing);
				}
				log.info("bulkGroup created. groupId :"+group.getBulkListingGroupId() + " and jobId: "+group.getBulkJobId() + ". isTemp? "+isTemp);
				
				
				bulkInventoryMgr.createBulkListingRequests(requestList);
				if (!isTemp) {
					Calendar utcNow = DateUtil.getNowCalUTC();
					//TODO - we can avoid this call by merging temporary/created states and depending on Job status to pick the groups
					updateBulkListingGroup(group, utcNow, BulkStatus.CREATED);
					sendGroupMessage(group.getBulkListingGroupId());
				}
			} else {
				for (int i = 0; i < requestList.size(); i += groupSize) {
					BulkListingGroup group = createGroup(sellerId, sellerGuid, subscriber,
							combinedAssertionHeader, bulkJobId, bulkUploadType);
					List<BulkListingRequest> subRequests = requestList.subList(
							i,
							i + groupSize > requestList.size() ? requestList
									.size() : i + groupSize);
					for (BulkListingRequest request : subRequests) {
						request.setBulkListingGroupId(group
								.getBulkListingGroupId());
						request.setCreatedBy(MODULENAME_Bulk_Create_Listing);
						request.setLastUpdatedBy(MODULENAME_Bulk_Create_Listing);
					}
					bulkInventoryMgr.createBulkListingRequests(subRequests);
					if (!isTemp) {
						Calendar utcNow = DateUtil.getNowCalUTC();
						//TODO - we can avoid this call by merging temporary/created states and depending on Job status to pick the groups
						updateBulkListingGroup(group, utcNow,
								BulkStatus.CREATED);
						sendGroupMessage(group.getBulkListingGroupId());
					}
				}
			}
		}
	}

	private void sendGroupMessage(final long groupId) {
		try {
			boolean enableSyncProcess = MasterStubHubProperties
					.getPropertyAsBoolean(
							"inventory.listings.bulk.enableSyncProcess", false);
			if (!enableSyncProcess) {
				if(log.isDebugEnabled())
					log.debug("Sending group request to queue groupId=" + groupId);
				jmsTemplate.send(new MessageCreator() {
					public javax.jms.Message createMessage(
							javax.jms.Session session) throws JMSException {
						javax.jms.MapMessage message = session
								.createMapMessage();
						message.setLong("groupId", groupId);
						return message;
					}
				});
				if(log.isDebugEnabled())
					log.debug("Sent group request to queue groupId=" + groupId);
			}else{
				invokeInprocessGroupProcess(groupId);
			}
		} catch (Throwable t) {
			log.error("Error while sending group created message groupId="
					+ groupId, t);
			invokeInprocessGroupProcess(groupId);
		}
	}
	
	private void invokeInprocessGroupProcess(Long groupId){
		log.info("in-process handling of the bulk job groups, groupId=" + groupId);
		Map<String, Long> message = new HashMap<String, Long>();
		message.put("groupId", groupId);
		bulkListingJobConsumer.processGroup(message);
	}

	private String createAssertionHeader(String assertionHeader,
			HttpHeaders httpHeaders) {
		StringBuilder combinedAssertionHeader = new StringBuilder();
		combinedAssertionHeader.append(assertionHeader);
		combinedAssertionHeader.append(HASHTAG_SEPARATOR);
		List<String> clientIp = httpHeaders.getRequestHeader("X-FORWARDED-FOR");
		if (clientIp != null && clientIp.size() > 0) {
			combinedAssertionHeader.append(clientIp.get(0));
		} else {
			Message message = PhaseInterceptorChain.getCurrentMessage();
			if (message != null) {
				HttpServletRequest httpRequest = (HttpServletRequest) message
						.get(AbstractHTTPDestination.HTTP_REQUEST);
				combinedAssertionHeader.append(httpRequest.getRemoteAddr());
			}
		}
		combinedAssertionHeader.append(HASHTAG_SEPARATOR);
		MultivaluedMap<String, String> headersMap = httpHeaders
				.getRequestHeaders();
		String userAgent = null;
		if (headersMap != null) {
			userAgent = headersMap.getFirst(HttpHeaders.USER_AGENT);
			if (StringUtils.trimToNull(userAgent) != null) {
				combinedAssertionHeader.append(userAgent);
			}
		}
		String responseAssertionHeader = combinedAssertionHeader.toString();
		if (responseAssertionHeader.length() > DATABASE_LIMIT) {
			responseAssertionHeader = responseAssertionHeader.substring(0,
					DATABASE_LIMIT);
		}

		return responseAssertionHeader;
	}

	/**
	 * @param sellerId
	 * @param sellerGuid
	 * @param assertionHeader
	 * @param bulkJobId
	 * @param bulkUploadType
	 *            TODO
	 */
	public BulkListingGroup createGroup(Long sellerId, String sellerGuid,
			String subscriber, String assertionHeader, Long bulkJobId,
			BulkUploadType bulkUploadType) {
		BulkListingGroup group = new BulkListingGroup();
		group.setAssertion(assertionHeader);
		group.setBulkJobId(bulkJobId);
		group.setBulkStatusId(BulkStatus.TEMPORARY.getId());
		group.setUserGuid(sellerGuid);
		group.setUserId(sellerId);
		Calendar utcNow = DateUtil.getNowCalUTC();
		group.setCreatedDate(utcNow);
		group.setLastUpdatedDate(utcNow);
		group.setCreatedBy(subscriber);
		group.setLastUpdatedBy(MODULENAME_Bulk_Create_Listing);
		group.setBulkUploadTypeId(bulkUploadType.getId());
		bulkInventoryMgr.createBulkListingGroup(group);
		return group;
	}

	private Map<String, List<BulkListingRequest>> groupCreateListingRequestObjects(
			List<ListingRequest> createListingRequestList, boolean isCreate) {
		Map<String, List<BulkListingRequest>> groupedRequestMap = new HashMap<String, List<BulkListingRequest>>();

		// To find out the duplicate External listing Ids
		Set<String> duplicateExtListingIdSet = new HashSet<String>();
		Set<String> tempSet = new HashSet<String>();
		for (ListingRequest request : createListingRequestList) {
			if (request.getExternalListingId() != null) {
				if (!tempSet.add(request.getExternalListingId())) {
					duplicateExtListingIdSet
							.add(request.getExternalListingId());
				}
			}
		}
		for (ListingRequest request : createListingRequestList) {
			if (request.getExternalListingId() != null) {
				String extId = listingTextValidatorUtil
						.removeSpecialCharactersFromRowSeat(request
								.getExternalListingId().trim(), Locale.US);
				request.setExternalListingId(extId);
			}
			if (request.getExternalListingId() != null
					&& request.getExternalListingId().length() > MAX_ALLOWED_EXTERNAL_ID_LENGTH) {
				errorGroupMapping(groupedRequestMap, request,
						ErrorCode.MAX_LENGTH_EXCEEDED);
			} else if (!isCreate && request.getListingId() == null) {
				errorGroupMapping(groupedRequestMap, request,
						ErrorCode.MISSING_LISTING_ID);
			} else if (isCreate && request.getExternalListingId() == null) {
				errorGroupMapping(groupedRequestMap, request,
						ErrorCode.MISSING_EXTERNAL_LISTING_ID);
			} else if (isCreate && request.getEvent() == null
					&& request.getEventId() == null) {
				errorGroupMapping(groupedRequestMap, request,
						ErrorCode.MISSING_EVENT_INFO);
			} else if (request.getExternalListingId() != null
					&& request.getEventId() != null) {
				if (duplicateExtListingIdSet.contains(request
						.getExternalListingId())) {
					errorGroupMapping(groupedRequestMap, request,
							ErrorCode.DUPLICATE_EXTERNAL_LISTING_ID);
				} else {
					if (!groupedRequestMap.containsKey(request.getEventId())) {
						List<BulkListingRequest> listingRequestList = new ArrayList<BulkListingRequest>();
						groupedRequestMap.put(request.getEventId(),
								listingRequestList);
					}
					BulkListingRequest bulkListingRequest = new BulkListingRequest();
					bulkListingRequest.setExternalListingId(request
							.getExternalListingId());
					bulkListingRequest.setListingRequestClob(ensureSize(JsonUtil
							.toJson(request)));
					groupedRequestMap.get(request.getEventId()).add(
							bulkListingRequest);
				}
			} else if (request.getExternalListingId() != null
					&& request.getEvent() != null) {
				if (duplicateExtListingIdSet.contains(request
						.getExternalListingId())) {
					errorGroupMapping(groupedRequestMap, request,
							ErrorCode.DUPLICATE_EXTERNAL_LISTING_ID);
				} else {
					if (!groupedRequestMap.containsKey(request.getEvent()
							.toString())) {
						List<BulkListingRequest> listingRequestList = new ArrayList<BulkListingRequest>();
						groupedRequestMap.put(request.getEvent().toString(),
								listingRequestList);
					}
					BulkListingRequest bulkListingRequest = new BulkListingRequest();
					bulkListingRequest.setExternalListingId(request
							.getExternalListingId());
					bulkListingRequest.setListingRequestClob(ensureSize(JsonUtil
							.toJson(request)));
					groupedRequestMap.get(request.getEvent().toString()).add(
							bulkListingRequest);
				}
			} else if (request.getListingId() != null && request.getEventId() != null) {
				if (!groupedRequestMap.containsKey(request.getEventId())) {
					List<BulkListingRequest> listingRequestList = new ArrayList<BulkListingRequest>();
					groupedRequestMap.put(request.getEventId(),
							listingRequestList);
				}
				BulkListingRequest bulkListingRequest = new BulkListingRequest();
				bulkListingRequest.setListingId(request.getListingId());
				bulkListingRequest.setListingRequestClob(ensureSize(JsonUtil
						.toJson(request)));
				groupedRequestMap.get(request.getEventId()).add(
						bulkListingRequest);
			}
		}

		return groupedRequestMap;
	}

	/**
	 * Checks if the size of inputString is greater than 4000.
	 * 
	 * @param inputString
	 * @return
	 */
	private String ensureSize(String inputString) {

		if (inputString != null && inputString.length() > CLOB_SIZE) {
			throw new ListingBusinessException();
		}
		return inputString;
	}

	/**
	 * @param groupedRequestMap
	 * @param request
	 * @param errorCode
	 */
	private void errorGroupMapping(
			Map<String, List<BulkListingRequest>> groupedRequestMap,
			ListingRequest request, ErrorCode errorCode) {
		if (!groupedRequestMap.containsKey(ERROR_GROUP)) {
			List<BulkListingRequest> listingRequestList = new ArrayList<BulkListingRequest>();
			groupedRequestMap.put(ERROR_GROUP, listingRequestList);
		}
		List<ListingError> errors = new ArrayList<ListingError>();
		switch (errorCode) {
		case DUPLICATE_EXTERNAL_LISTING_ID:
			errors.add(new ListingError(ErrorType.INPUTERROR,
					ErrorCode.DUPLICATE_EXTERNAL_LISTING_ID,
					ErrorEnum.DUPLICATE_EXTERNAL_LISTING_ID.getMessage(),
					"externalListingId"));
			break;
		case MISSING_LISTING_ID:
			errors.add(new ListingError(ErrorType.INPUTERROR,
					ErrorCode.MISSING_LISTING_ID, ErrorEnum.MISSING_LISTING_ID
							.getMessage(), "listingId"));
			break;
		case MISSING_EVENT_INFO:
			errors.add(new ListingError(ErrorType.INPUTERROR,
					ErrorCode.MISSING_EVENT_INFO, ErrorEnum.MISSING_EVENT_INFO
							.getMessage(), "event"));
			break;
		case MISSING_EXTERNAL_LISTING_ID:
			errors.add(new ListingError(ErrorType.INPUTERROR,
					ErrorCode.MISSING_EXTERNAL_LISTING_ID,
					ErrorEnum.MISSING_EXTERNAL_LISTING_ID.getMessage(),
					"externalListingId"));
			break;

		case MAX_LENGTH_EXCEEDED:
			errors.add(new ListingError(ErrorType.INPUTERROR,
					ErrorCode.MAX_LENGTH_EXCEEDED,
					ErrorEnum.MAX_LENGTH_EXCEEDED.getMessage(),
					"externalListingId"));
			break;
		default :
			//SELLAPI-1135 sonar-rules, Switch statements should end with a default case.
			break;
		}
		BulkListingRequest bulkListingRequest = new BulkListingRequest();
		bulkListingRequest.setExternalListingId(request.getExternalListingId());
		bulkListingRequest.setErrorCode(JsonUtil.toJson(errors));
		bulkListingRequest.setListingRequestClob(JsonUtil.toJson(request));
		groupedRequestMap.get(ERROR_GROUP).add(bulkListingRequest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.stubhub.domain.inventory.v2.listings.intf.BulkListingJobController
	 * #getjob(java.lang.String)
	 */
	public void processListingRequestByGroupId(Long bulkListingGroupId,
			String machineId, boolean isRetry) {
		long startTime = System.currentTimeMillis();
		BulkListingGroup bulkListingGroup = bulkInventoryMgr
				.getGroupById(bulkListingGroupId);
		BulkJob bulkJob = bulkInventoryMgr.getJobById(bulkListingGroup
				.getBulkJobId());
		Calendar utcNow = DateUtil.getNowCalUTC();
		Integer elapsedProcessingTime = MasterStubHubProperties
				.getPropertyAsInt("bulk.group.elapsed.processing.time", 10);
		long timeDiff = 0;
		if (bulkListingGroup.getLastUpdatedDate() != null) {
			timeDiff = System.currentTimeMillis()
					- bulkListingGroup.getLastUpdatedDate().getTimeInMillis();
		}
		long reprocessingTime = elapsedProcessingTime.longValue() * 60 * 1000;
		try {
				if(log.isDebugEnabled())
					log.debug("processing GroupId=" + bulkListingGroupId
						+ " : MachineId=" + machineId);
				bulkListingGroup.setMachineNode(machineId);
				bulkListingGroup.setBulkStatusId(BulkStatus.INPROGRESS.getId());
				bulkListingGroup.setLastUpdatedDate(utcNow);
				bulkListingGroup
						.setLastUpdatedBy(MODULENAME_Bulk_Create_Listing);
				bulkInventoryMgr.updateBulkListingGroup(bulkListingGroup);

				// Update bulk job to in progress if not already there.
				if (!bulkJob.getBulkStatusId().equals(
						BulkStatus.INPROGRESS.getId())) {
					bulkJob.setBulkStatusId(BulkStatus.INPROGRESS.getId());
					try {
						bulkInventoryMgr.updateJob(bulkJob);
					} catch (HibernateOptimisticLockingFailureException holfe) {
						bulkJob = bulkInventoryMgr.getJobById(bulkListingGroup
								.getBulkJobId());
						if (!bulkJob.getBulkStatusId().equals(
								BulkStatus.INPROGRESS.getId())) {
							bulkJob.setBulkStatusId(BulkStatus.INPROGRESS
									.getId());
							bulkInventoryMgr.updateJob(bulkJob);
						}
					}
				}
				List<BulkListingRequest> bulkListingRequests = bulkInventoryMgr
						.getBulkListingRequests(bulkListingGroupId);

				String[] tokens = bulkListingGroup.getAssertion().split(
						HASHTAG_SEPARATOR);
				String assertion = tokens[0];
				String clientIp = tokens[1];
				String userAgent = tokens[2];

				Map<String, BulkListingRequest> bulkListingRequestMap = new HashMap<String, BulkListingRequest>();
				BulkListingInternal bulkInternalListings = createBulkListingInternal(
						bulkListingRequests, bulkListingGroup, assertion,
						bulkListingRequestMap);
				// internal call to create bulk listing
				List<ListingResponse> listingResponses = null;
				if (bulkInternalListings.getCreateListingBody() != null
						&& !bulkInternalListings.getCreateListingBody()
								.isEmpty()) {
					if (bulkListingGroup.getBulkUploadTypeId().equals(
							BulkUploadType.CREATE.getId())) {
						listingResponses = listingCreateProcess.createListings(
								bulkInternalListings, clientIp, userAgent);
					} else if (bulkListingGroup.getBulkUploadTypeId().equals(
							BulkUploadType.UPDATE.getId())) {
						listingResponses = listingCreateProcess.updateListings(
								bulkInternalListings, clientIp, userAgent);
					}
				}
				updateListingRequestByGroupId(bulkListingGroup,
						listingResponses, bulkListingRequestMap);
				updateBulkListingGroup(bulkListingGroup, utcNow,
						BulkStatus.COMPLETED);
				updateJob(bulkListingGroup.getUserId(), BulkStatus.COMPLETED,
						bulkJob);
				log.info("Completed processing BulkListingGroupId="
						+ bulkListingGroupId + " : bulkJobId="
						+ bulkJob.getBulkJobId() + " in ProcessingTimeOfGroup="
						+ (System.currentTimeMillis() - startTime));
		} catch (JsonGenerationException jge) {
			log.error(
					"JsonGenerationException Occured while jspon parsing the request. groupId="+bulkListingGroupId+" and jobId="+bulkJob.getBulkJobId(),
					jge);
			updateBulkListingGroup(bulkListingGroup, utcNow, BulkStatus.ERROR);
			updateJob(bulkListingGroup.getUserId(), BulkStatus.ERROR, bulkJob);
		} catch (JsonMappingException e) {
			log.error(
					"JsonMappingException Occured while jspon parsing the request. groupId="+bulkListingGroupId+" and jobId="+bulkJob.getBulkJobId(),
					e);
			updateBulkListingGroup(bulkListingGroup, utcNow, BulkStatus.ERROR);
			updateJob(bulkListingGroup.getUserId(), BulkStatus.ERROR, bulkJob);
		} catch (IOException e) {
			if (!isRetry) { // retrying only Once
				log.debug(
						"IOException Occured while json parsing the request. Retrying the request again. groupId="+bulkListingGroupId+" and jobId="+bulkJob.getBulkJobId(),
						e);
				processListingRequestByGroupId(bulkListingGroupId, machineId,
						true);
			} else {
				updateBulkListingGroup(bulkListingGroup, utcNow, BulkStatus.ERROR);
				updateJob(bulkListingGroup.getUserId(), BulkStatus.ERROR, bulkJob);
				log.error("IOException Occured while processing the request.",
						e);
			}
		}catch (Exception e) {
			log.error(
					"Unknown Exception Occured while processing the request. groupId="+bulkListingGroupId+" and jobId="+bulkJob.getBulkJobId(),
					e);
			updateBulkListingGroup(bulkListingGroup, utcNow, BulkStatus.ERROR);
			updateJob(bulkListingGroup.getUserId(), BulkStatus.ERROR, bulkJob);
		}
	}

	private static final String EXTERNAL_LISTING_ID_PREFIX = "E:";

	private BulkListingInternal createBulkListingInternal(
			List<BulkListingRequest> bulkListingRequests,
			BulkListingGroup bulkListingGroup, String assertion,
			Map<String, BulkListingRequest> bulkListingRequestMap)
			throws JsonGenerationException, JsonMappingException, IOException {

		BulkListingInternal bulkInternalListings = new BulkListingInternal();

		List<ListingRequest> createListingRequests = new ArrayList<ListingRequest>();
		for (BulkListingRequest request : bulkListingRequests) {
			//SELLAPI-1333 09/30/15 START
			if ( request.getErrorCode() == null) {
				ListingRequest listingRequest = (ListingRequest) JsonUtil.toObject(
						request.getListingRequestClob(), ListingRequest.class);
				if (request.getListingId() != null) {
					bulkListingRequestMap.put(request.getListingId().toString(), request);
					listingRequest.setRequestId(request.getListingId().toString());
					log.debug("put bulkListingRequest={} through listingId={}", request, request.getListingId());
				} else {
					bulkListingRequestMap.put(EXTERNAL_LISTING_ID_PREFIX + request.getExternalListingId(), request);
					listingRequest.setRequestId(EXTERNAL_LISTING_ID_PREFIX + request.getExternalListingId());
					log.debug("put bulkListingRequest={} through externalListingId={}", request, request.getExternalListingId());
				}
				createListingRequests.add(listingRequest);
			}
			//SELLAPI-1333 09/30/15 END
		}
		bulkInternalListings.setCreateListingBody(createListingRequests);
		bulkInternalListings.setAssertion(assertion);
		bulkInternalListings.setJobId(bulkListingGroup.getBulkJobId());
		bulkInternalListings.setGroupId(bulkListingGroup.getBulkListingGroupId());
		bulkInternalListings.setSellerGuid(bulkListingGroup.getUserGuid());
		bulkInternalListings.setSellerId(bulkListingGroup.getUserId());
		bulkInternalListings.setSubscriber(bulkListingGroup.getCreatedBy());
		return bulkInternalListings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.stubhub.domain.inventory.v2.listings.intf.BulkListingJobController
	 * #updateJob(java.lang.String, java.util.List)
	 */
	public void updateListingRequestByGroupId(
			BulkListingGroup bulkListingGroup,
			List<ListingResponse> listingResponses,
			Map<String, BulkListingRequest> bulkListingRequestMap) {

		Calendar utcNow = DateUtil.getNowCalUTC();
		if (listingResponses != null) {
			for (ListingResponse listingResponse : listingResponses) {

				if (listingResponse.getId() != null) {

					BulkListingRequest bulkListingRequest = bulkListingRequestMap
							.get(listingResponse.getId());

					if (bulkListingRequest == null) {
						bulkListingRequest = bulkListingRequestMap
								.get(EXTERNAL_LISTING_ID_PREFIX + listingResponse.getExternalListingId());
						log.debug("get bulkListingRequest={} through externalListingId={}, bulkListingRequest.externalListingId={}", bulkListingRequest, listingResponse.getExternalListingId(),
								bulkListingRequest != null ? bulkListingRequest.getExternalListingId() : "null");
					} else {
						log.debug("get bulkListingRequest={} through listingId={}", bulkListingRequest, listingResponse.getId());
					}

					if (bulkListingRequest != null) {
						if(listingResponse.getStatus() != null) {
							bulkListingRequest.setListingId(Long.valueOf(listingResponse.getId()));
							bulkListingRequest.setListingStatus(listingResponse.getStatus().toString());
							log.debug("Setting the listingId=" + listingResponse.getId());
							log.debug("Setting the listingStatus=" + listingResponse.getStatus().toString());
						} else if (listingResponse.getErrors() != null) {
							bulkListingRequest
									.setErrorCode(
											JsonUtil.toJson(listingResponse.getErrors()));
						}
						bulkListingRequest
								.setLastUpdatedDate(utcNow);
						bulkListingRequest
								.setLastUpdatedBy(MODULENAME_Bulk_Create_Listing);
					}
				}

			}
			List<BulkListingRequest> bulkListingRequests = new ArrayList<BulkListingRequest>(
					bulkListingRequestMap.values());
			bulkInventoryMgr.updateBulkListingRequests(bulkListingRequests);
		}
	}

	/**
	 * @param bulkListingGroup
	 * @param utcNow
	 * @param bulkStatus
	 */
	private void updateBulkListingGroup(BulkListingGroup bulkListingGroup,
			Calendar utcNow, BulkStatus bulkStatus) {
		bulkListingGroup.setLastUpdatedDate(utcNow);
		bulkListingGroup.setLastUpdatedBy(MODULENAME_Bulk_Create_Listing);
		if (bulkStatus.equals(BulkStatus.ERROR)) {
			bulkListingGroup.setBulkStatusId(BulkStatus.ERROR.getId());
		} else if (bulkStatus.equals(BulkStatus.CREATED)) {
			bulkListingGroup.setBulkStatusId(BulkStatus.CREATED.getId());
		} else {
			bulkListingGroup
					.setAssertion("some-dummy-value-since-this-column-Cannot-be-null"); // we
																						// have
																						// to
																						// make
																						// the
																						// db
																						// as
																						// Nullable
																						// column.
			bulkListingGroup.setBulkStatusId(BulkStatus.COMPLETED.getId());
		}
		bulkInventoryMgr.updateBulkListingGroup(bulkListingGroup);
	}

	private void updateJob(Long sellerId, BulkStatus bulkStatus, BulkJob bulkJob) {
		Calendar utcNow = DateUtil.getNowCalUTC();
		List<BulkListingGroup> bulkListingGroups = bulkInventoryMgr
				.getGroupsByJobId(bulkJob.getBulkJobId());
		int counter = 0;
		if (bulkListingGroups != null) {
			for (BulkListingGroup group : bulkListingGroups) {
				if (group.getBulkStatusId().equals(BulkStatus.ERROR.getId())
						|| group.getBulkStatusId().equals(
								BulkStatus.COMPLETED.getId())) {
					counter++;
				}
			}
			if (bulkListingGroups.size() == counter) {// means all groups have
														// completed.
				if (bulkStatus.equals(BulkStatus.ERROR)) {
					bulkJob.setBulkStatusId(BulkStatus.ERROR.getId());
				} else {
					bulkJob.setBulkStatusId(BulkStatus.COMPLETED.getId());
				}
				bulkJob.setLastUpdatedDate(utcNow);
				bulkJob.setLastUpdatedBy(MODULENAME_Bulk_Create_Listing);

				try {
					bulkInventoryMgr.updateJob(bulkJob);// put the job to
														// complete
					//TODO - add a log statement as job is complete
				} catch (HibernateOptimisticLockingFailureException holfe) {
					
					//SELLAPI-1135 sonar-rules, avoid reassigning to parameters.
					BulkJob lBulkJob = bulkInventoryMgr.getJobById(bulkJob
							.getBulkJobId());
					if (!(lBulkJob.getBulkStatusId().equals(BulkStatus.COMPLETED
							.getId()))) {
						if (bulkStatus.equals(BulkStatus.ERROR)) {
							lBulkJob.setBulkStatusId(BulkStatus.ERROR.getId());
						} else {
							lBulkJob.setBulkStatusId(BulkStatus.COMPLETED
									.getId());
						}
						lBulkJob.setLastUpdatedDate(utcNow);
						lBulkJob.setLastUpdatedBy(MODULENAME_Bulk_Create_Listing);
						bulkInventoryMgr.updateJob(lBulkJob);
					}
				}
				List<BulkJob> sellerJobs = bulkInventoryMgr
						.getPendingJobsBySellerId(sellerId);// get other jobs of
															// seller and
															// activate one of
															// them.
				if (sellerJobs != null && sellerJobs.size() > 0) {
					for (BulkJob sellerJob : sellerJobs) {
						if (sellerJob.getBulkStatusId().equals(
								BulkStatus.TEMPORARY.getId())) {
							//TODO add a log statement mentioning that a pending JOB is picked-up 
							sellerJob.setBulkStatusId(BulkStatus.CREATED
									.getId());
							sellerJob.setLastUpdatedDate(utcNow);
							sellerJob
									.setLastUpdatedBy(MODULENAME_Bulk_Create_Listing);
							bulkInventoryMgr.updateJob(sellerJob);
							List<BulkListingGroup> sellerBulkListingGroups = bulkInventoryMgr
									.getGroupsByJobId(sellerJob.getBulkJobId());
							if (sellerBulkListingGroups != null) {
								for (BulkListingGroup group : sellerBulkListingGroups) {
									if (group.getBulkStatusId() != null
											&& !(group.getBulkStatusId()
													.equals(BulkStatus.ERROR
															.getId()))) { // activate
																			// only
																			// the
																			// ones
																			// which
																			// are
																			// not
																			// errored
																			// out
										group.setBulkStatusId(BulkStatus.CREATED
												.getId());
										group.setLastUpdatedDate(utcNow);
										group.setLastUpdatedBy(MODULENAME_Bulk_Create_Listing);
									}
								}
								bulkInventoryMgr
										.updateBulkListingGroups(sellerBulkListingGroups);
								for (BulkListingGroup group : sellerBulkListingGroups){
									if (group.getBulkStatusId() != null
											&& !(group.getBulkStatusId()
													.equals(BulkStatus.ERROR
															.getId()))) {
										//TODO - what is the expected behavior if the JMS broker is down
										sendGroupMessage(group
												.getBulkListingGroupId());	
									}
									
								}
							}
							break;
						}
					}
				}
			}
		}
	}

	public BulkJobResponse getJobStatuses(Long sellerId, Long jobGuid) {
		BulkJob bulkJob = bulkInventoryMgr.getJobById(jobGuid);
		if (bulkJob == null || !sellerId.equals(bulkJob.getUserId())) {
			ListingError listingError = new ListingError(ErrorType.NOT_FOUND,
					ErrorCode.BULK_JOB_NOT_FOUND,
					ErrorEnum.BULK_JOB_NOT_FOUND.getMessage(),
					jobGuid.toString());
			throw new ListingBusinessException(listingError);
		}

		List<BulkListingRequest> listingResponses = bulkInventoryMgr
				.getJobStatuses(jobGuid);

		BulkJobResponse response = populateJobStatusResponse(jobGuid, bulkJob,
				listingResponses);
		// TODO, no real meat here yet
		return response;
	}
	
	public BulkJobResponse updateJobStatus(Long sellerId, Long jobGuid, BulkJobStatusRequest jobStatusRequest) {
	    BulkJobResponse response = new BulkJobResponse();
	    BulkJob bulkJob = bulkInventoryMgr.getJobById(jobGuid);
	    if (bulkJob == null || !sellerId.equals(bulkJob.getUserId())) {
	        ListingError listingError = new ListingError(ErrorType.NOT_FOUND,
                ErrorCode.BULK_JOB_NOT_FOUND,
                ErrorEnum.BULK_JOB_NOT_FOUND.getMessage(),
                jobGuid.toString());
	        throw new ListingBusinessException(listingError);
	    }
	    
	    String requestStatus = jobStatusRequest.getJobStatus();
	    if(StringUtils.trimToNull(requestStatus) == null || !requestStatus.equalsIgnoreCase(BulkStatus.ERROR.getDescription())) {
	        ListingError listingError = new ListingError(ErrorType.INPUTERROR,
                ErrorCode.INVALID_JOB_STATUS,
                "Invalid job status",
                jobGuid.toString());
            throw new ListingBusinessException(listingError);
	    }
	    
	    BulkStatus dbStatus = BulkStatus.getBulkJobStatus(bulkJob.getBulkStatusId());
	    
	    if(BulkStatus.COMPLETED.equals(dbStatus)) {
	        ListingError listingError = new ListingError(ErrorType.BUSINESSERROR,
                ErrorCode.BULK_JOB_COMPLETED,
                "Job is Completed",
                jobGuid.toString());
            throw new ListingBusinessException(listingError);
	    }
	    
	    if(!BulkStatus.ERROR.equals(dbStatus)) {
	        bulkJob.setBulkStatusId(BulkStatus.ERROR.getId());
	        bulkInventoryMgr.updateJob(bulkJob);
	        
	        if(StringUtils.trimToNull(jobStatusRequest.getListingStatus()) != null
	            && jobStatusRequest.getListingStatus().equalsIgnoreCase(ListingStatus.DELETED.name())) {
	            List<BulkListingRequest> bulkListingRequests = bulkInventoryMgr.getJobStatuses(jobGuid);
	            if(bulkListingRequests != null) {
	                for(BulkListingRequest bulkListingRequest : bulkListingRequests) {
	                    Listing listing = null;
	                    if(bulkListingRequest.getListingId() != null) {
	                        listing = inventoryMgr.getListing(bulkListingRequest.getListingId());
	                        if(listing == null || ListingStatus.DELETED.name().equalsIgnoreCase(listing.getSystemStatus())) {
	                            continue;
	                        }
	                        
	                        listing.setSystemStatus(ListingStatus.DELETED.name());
	                        listing.setLastUpdatedBy(BULK_LISTING_JOB_STATUS_UPDATE_API);
	                        inventoryMgr.updateListingOnly(listing); //TODO: should we use batch update
	                    }
	                  
	                }
	            }
	        }
	    }
	    
	    response.setJobGuid(jobGuid);
        response.setStatus(BulkStatus.getBulkJobStatus(bulkJob.getBulkStatusId()).getDescription());
        return response;
	}
	    
	private BulkJobResponse populateJobStatusResponse(Long jobGuid,
			BulkJob bulkJob, List<BulkListingRequest> listingResponses) {
		BulkJobResponse response = new BulkJobResponse();
		response.setJobGuid(jobGuid);

		BulkStatus status = BulkStatus.getBulkJobStatus(bulkJob
				.getBulkStatusId());

		switch (status) {
		case TEMPORARY:
		case CREATED:
		case INPROGRESS:
			// above three all maps to INPROGRESS as external status
			response.setStatus(BulkStatus.INPROGRESS.getDescription());
			break;
		default:
			response.setStatus(status.getDescription());
		}

		response.setNumberOfInputListings(listingResponses != null ? listingResponses
				.size() : 0);
		int numProcessedListings = 0;
		List<com.stubhub.domain.inventory.v2.bulk.DTO.ListingResponse> listings = new ArrayList<com.stubhub.domain.inventory.v2.bulk.DTO.ListingResponse>();

		for (BulkListingRequest listingRequest : listingResponses) {
			// processed listing means either listing has been created or it has
			// errored out
			if ((listingRequest.getListingId() != null && listingRequest
					.getListingId() > 0)
					|| (listingRequest.getErrorCode() != null && !listingRequest
							.getErrorCode().isEmpty())) {
				numProcessedListings++;
				com.stubhub.domain.inventory.v2.bulk.DTO.ListingResponse listingResp = new com.stubhub.domain.inventory.v2.bulk.DTO.ListingResponse();
				listingResp.setExternalListingId(listingRequest
						.getExternalListingId());
				if (listingRequest.getListingId() != null)
					listingResp.setListingId(listingRequest.getListingId());
				if (listingRequest.getListingStatus() != null)
					listingResp.setStatus(ListingStatus
							.fromString(listingRequest.getListingStatus()));
				if (listingRequest.getExternalListingId() != null) {
					listingResp.setExternalListingId(listingRequest
							.getExternalListingId());
				}
				if (listingRequest.getErrorCode() != null) {
					ListingError errors[];
					errors = (ListingError[]) JsonUtil
							.toObject(listingRequest.getErrorCode(),
									ListingError[].class);
					listingResp.setErrors(new ArrayList<ListingError>(Arrays
							.asList(errors)));
				}
				listings.add(listingResp);
			}
		}
		response.setNumberOfProcessedListings(numProcessedListings);
		if (listings.size() > 0) {
			response.setListings(listings);
		}

		return response;
	}

	public Long bulkUpdateListing(
			Long sellerId,
			String sellerGuid,
			String subscriber,
			BulkStatus bulkStatus,
			com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingRequest updateListingRequest,
			String assertionHeader, HttpHeaders httpHeaders) {

		boolean isTemporaryJob = false;
		// Creating a job
		BulkJob bulkJob = new BulkJob();
		List<BulkJob> allJobs = bulkInventoryMgr.getAllJobsForSeller(sellerId);
		if (allJobs != null) {
			isTemporaryJob = true; // This seller has a job that is already
									// getting processed.
		}
		if (bulkStatus != null) {
			bulkJob.setBulkStatusId(bulkStatus.getId());
		} else if (isTemporaryJob) {
			bulkJob.setBulkStatusId(BulkStatus.TEMPORARY.getId());
		} else {
			bulkJob.setBulkStatusId(BulkStatus.CREATED.getId());
		}
		bulkJob.setUserId(sellerId);
		Calendar utcNow = DateUtil.getNowCalUTC();
		bulkJob.setCreatedDate(utcNow);
		bulkJob.setLastUpdatedDate(utcNow);
		bulkJob.setCreatedBy(MODULENAME_Bulk_Update_Listing);
		bulkJob.setLastUpdatedBy(MODULENAME_Bulk_Update_Listing);
		if(updateListingRequest != null
                && updateListingRequest.getListings() != null
                && !updateListingRequest.getListings().isEmpty()) {
            List<ListingRequest> updListingRequestList = updateListingRequest.getListings();
            bulkInventoryMgr.createJob(bulkJob);
            if (!bulkJob.getBulkStatusId().equals(BulkStatus.ERROR.getId())) {
                populateEventIds(sellerId, updListingRequestList);
              
                createGroups(
                      sellerId,
                      sellerGuid,
                      subscriber,
                      groupCreateListingRequestObjects(updListingRequestList,
                              false), assertionHeader, bulkJob.getBulkJobId(),
                      isTemporaryJob, httpHeaders, BulkUploadType.UPDATE);
            }
		
		} else { // Empty payload
            log.error(
                "Error while creating Bulk Job. Payload has no listings. Aborting the request for sellerId=" + sellerId);
            ListingError listingError = new ListingError(
                    ErrorType.INPUTERROR,
                    ErrorCode.INPUT_LISTING_EMPTY,
                    "No listings are present in the request",
                    "inputListingsEmpty");
            throw new ListingBusinessException(listingError);
		}
		
		return bulkJob.getBulkJobId();
	}

	//SELLAPI-1181 09/04/15 START
	//the following method is modified to accommodate external listing id
	private void populateEventIds(Long sellerId, List<ListingRequest> updateListingRequestList) {
		
		//splitting request object list into two groups, list with listing ids, and external ids
		List<ListingRequest> listWithListingId = new ArrayList<ListingRequest>();
		List<ListingRequest> listWithExternalId = new ArrayList<ListingRequest>();

		for (ListingRequest lr : updateListingRequestList) {
			if (lr.getListingId() != null) {
				listWithListingId.add(lr);
			} else {
				listWithExternalId.add(lr);
			} 
		}
		
		//populate event ids for objects with listing id
		if (listWithListingId.size() > 0 ) {
			//populate event ids from solr if available
			List<Long> missedListingIds = listingCatalogSolrUtil.getEventByListingId(listWithListingId, sellerId);
			Map<Long, ListingRequest> listingIdEventIdMap = new HashMap<Long, ListingRequest>();
			for(ListingRequest request : listWithListingId){
				listingIdEventIdMap.put(request.getListingId(), request);
			}
			//try to get event ids from DB if missing in solr
			List<Listing> listings = null;
			if (!missedListingIds.isEmpty()) {
				listings = inventoryMgr.getListings(missedListingIds);
				
				//if any found in DB, populate event id and external listing id in request objects
				if(listings!=null){
					for (Listing listing : listings) {
						ListingRequest request = listingIdEventIdMap.get(listing.getId());
						request.setEventId(listing.getEventId().toString());
						request.setExternalListingId(listing.getExternalId());
					}
				}
			}			
		}
		
		//populate event ids for objects with external ids
		if (listWithExternalId.size() > 0 ) {
			//populate event ids from solr if available
			List<String> missedExternalIds = listingCatalogSolrUtil.getEventByExternalId(sellerId, listWithExternalId);
			Map<String, ListingRequest> listingIdExternalIdMap = new HashMap<String, ListingRequest>();
			for(ListingRequest request : listWithExternalId){
				listingIdExternalIdMap.put(request.getExternalListingId(), request);
			}
			//try to get event ids from DB if missing in solr
			List<Listing> externalList = null;
			if (!missedExternalIds.isEmpty()) {
				externalList = inventoryMgr.getListings(sellerId, missedExternalIds);
			
				//if any found in DB, populate event id and listing id in request objects
				if(externalList!=null){
					for (Listing listing : externalList) {
						ListingRequest lr = listingIdExternalIdMap.get(listing.getExternalId());
						lr.setEventId(listing.getEventId().toString());
						lr.setListingId(listing.getId());
					}
				}
			}			
		}		
	}
}
