package com.stubhub.domain.inventory.listings.v2.tns.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import com.stubhub.domain.catalog.read.v3.intf.common.dto.response.Grouping;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.EventPerformer;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.tasks.DeliveryAndFullfilmentOptionsTask;
import com.stubhub.domain.inventory.listings.v2.tns.dto.EventMessageHub;
import com.stubhub.domain.inventory.listings.v2.tns.dto.FraudCheckStatus;
import com.stubhub.domain.inventory.listings.v2.tns.dto.FraudStatusUpdateRequest;
import com.stubhub.domain.inventory.listings.v2.tns.dto.ListingMessageHub;
import com.stubhub.domain.inventory.listings.v2.util.MessageHubAPIHelper;
import com.stubhub.domain.user.services.customers.v2.intf.CustomerContactInfo;
import com.stubhub.domain.user.services.customers.v2.intf.GetCustomerResponse;
import com.stubhub.platform.messagehub.client.MessageHubRequest;

@Component
public class FraudEvaluationHelper extends AbstractFraudEvaluationHelper {

	@Autowired
	private MessageHubAPIHelper messageHubHelper;

	@Autowired
	private InventoryMgr inventoryMgr;

	private static final Logger log = LoggerFactory.getLogger(FraudEvaluationHelper.class);
	private static final int SELL_IT_NOW = 1;

	public boolean submitFraudListingEmailRequest(String listingId, String sellerId, Long fraudCheckStatusId) {
		return fraudAPIHelper.submitFraudListingEmailRequest(listingId, sellerId, fraudCheckStatusId);
	}

	public void sendListingAcceptEmail(Long listingId) {
		log.info("api_domain={}, api_method={}, calling, listingId={}, ", DOMAIN, "sendListingAcceptEmail", listingId);
		try {
			Listing listing = inventoryMgr.getListing(listingId);
			if (listing == null) {
				log.error("api_domain={} api_method={} message=\"No listing found \" listingId={} ", DOMAIN,
						"sendListingAcceptEmail", listingId);
				SHSystemException shException = new SHSystemException(
						"No listing found  for listingId  : " + listingId);
				shException.setErrorCode("inventory.listings.systemError");
				throw shException;
			}
			String userGUID = null;
			userGUID = fraudAPIHelper.getUserGuidFromUid(listing.getSellerId());
			GetCustomerResponse userInfo = fraudAPIHelper.getCustomer(userGUID);
			if (userInfo == null) {
				log.error("api_domain={} api_method={} message=\"No User found \" listingId={} sellerId={} ", DOMAIN,
						"sendListingAcceptEmail", listingId, listing.getSellerId());
				SHSystemException shException = new SHSystemException(
						"No User found for sellerId : " + listing.getSellerId() + "  listingId : " + listingId);
				shException.setErrorCode("inventory.listings.systemError");
				throw shException;
			}
			Event event = fraudAPIHelper.getEventById(listing.getEventId());
			DeliveryAndFullfilmentOptionsTask.ticketMediumToFulfillmentMethod(listing);
			log.info(
					"api_domain={}, api_method={}, message=\"Listing, UserInfo and Event info fetched\", listingId={}, ",
					DOMAIN, "sendListingAcceptEmail", listingId);
			MessageHubRequest.MessageHubRequestBuilder messageHubRequestBuilder = MessageHubRequest.builder();
			messageHubRequestBuilder.messageName("TES_LISTING_CREATED_MHV2");
			messageHubRequestBuilder.operatorId("sell");
			messageHubRequestBuilder.role("R2");
			messageHubRequestBuilder.userId(listing.getSellerId());
			log.info("api_domain={}, api_method={}, message=\"Building Message Hub request\", listingId={}, ", DOMAIN,
					"sendListingAcceptEmail", listingId);
			populateUserInfo(userInfo, messageHubRequestBuilder);
			messageHubRequestBuilder.data("ticketInhand", listing.getInhandInd());
			messageHubRequestBuilder.data("expectedInhandDate", getFormattedDate(listing.getInhandDate()));
			messageHubRequestBuilder.data("listingId", listing.getId());
			EventMessageHub eventMessageHub = new EventMessageHub();
			populateEventMH(event, eventMessageHub);
			ListingMessageHub listingMessageHub = new ListingMessageHub();
			if (listing.getFulfillmentMethod() != null && listing.getFulfillmentMethod().getCode() != null) {
				listingMessageHub.setFulfillmentMethodId(listing.getFulfillmentMethod().getCode().toString());
			}
			if (listing.getListingType() != null) {
				listingMessageHub.setType(listing.getListingType().toString());
			}
			if (listing.getTicketMedium() != null) {
				listingMessageHub.setMedium(listing.getTicketMedium().toString());
			}
			listingMessageHub.setEvent(eventMessageHub);
			listingMessageHub.setQuantity(listing.getQuantity());
			//SELLAPI-4213
			listingMessageHub.setSection(listing.getSection()); 
			if (listing.getRow() != null)
				listingMessageHub.setRows(listing.getRow()); 
			if(listing.getSeats() != null)
				listingMessageHub.setSeats(listing.getSeats());
			
			messageHubRequestBuilder.data("listing", listingMessageHub);
			if(listing.getSellItNow() != null && listing.getSellItNow() == SELL_IT_NOW) {
				log.info("api_domain={}, api_method={}, message=\"Not Sending Email for SNOW Listing\", listingId={}, ", DOMAIN,
						"sendListingAcceptEmail", listingId);
				return;
			}else{
				log.info("api_domain={}, api_method={}, message=\"Submitting Message Hub request\", listingId={}, ", DOMAIN,
						"sendListingAcceptEmail", listingId);
				messageHubHelper.send(messageHubRequestBuilder);
			}
			log.info(
					"api_domain={}, api_method={}, message=\"Submission complete for Message Hub request\", listingId={}, ",
					DOMAIN, "sendListingAcceptEmail", listingId);
		} catch (Exception e) {
			log.error(
					"api_domain={} api_method={} message=\"Exception while submitting accept email request to Message Hub \" listingId={},  exception=\"{}\" ",
					DOMAIN, "submitFraudListingEmailRequest", listingId, ExceptionUtils.getFullStackTrace(e), e);
		} finally {
			log.info("api_domain={}, api_method={}, complete, listingId={}, ", DOMAIN, "sendListingAcceptEmail",
					listingId);
		}

	}

	private void populateUserInfo(GetCustomerResponse userInfo,
			MessageHubRequest.MessageHubRequestBuilder messageHubRequestBuilder) {
		if (userInfo != null) {
			messageHubRequestBuilder.shStoreId(userInfo.getPreferredStoreID());
			messageHubRequestBuilder.locale(getUserLocale(userInfo.getPreferredLocale()));
			messageHubRequestBuilder.userGuid(userInfo.getUserCookieGuid());

			CustomerContactInfo userContact = userInfo.getDefaultContact();
			if (userContact != null && userContact.getName() != null) {
				messageHubRequestBuilder.data("firstname",
						StringUtils.isNotEmpty(userContact.getName().getFirstName())
								? userContact.getName().getFirstName()
								: "");
				messageHubRequestBuilder.data("lastName",
						StringUtils.isNotEmpty(userContact.getName().getLastName())
								? userContact.getName().getLastName()
								: "");
			} else {
				messageHubRequestBuilder.data("firstname", "");
				messageHubRequestBuilder.data("lastName", "");
			}
		}
	}

	private void populateEventMH(Event event, EventMessageHub eventMessageHub) {
		if (event != null) {
			eventMessageHub.setId(event.getId().toString());
			eventMessageHub.setDescription(event.getName());
			eventMessageHub.setDateLocal(event.getEventDateLocal());

			eventMessageHub.setVenue(event.getVenue().getName());
			eventMessageHub.setCountryCode(event.getVenue().getCountry());
			eventMessageHub.setCity(event.getVenue().getLocality());
			eventMessageHub.setState(event.getVenue().getState());
			List<EventPerformer> performers = event.getPerformers();
			if (performers != null && !performers.isEmpty()) {
				EventPerformer performer = performers.get(0);
				if (performer != null) {
					if (performer.getId() != null) {
						eventMessageHub.setPerformerId(performer.getId().toString());
					}
					eventMessageHub.setPerformerName(performer.getCleanPrimaryName());
				}
				if (performers.size() >= 2) {
					performer = performers.get(1);
					if (performer != null) {
						eventMessageHub.setSecondaryPerformerId(performer.getId().toString());
						eventMessageHub.setSecondaryPerformerName(performer.getCleanPrimaryName());
					}
				}
			}
			if (event.getGroupings() != null) {
				eventMessageHub.setGroupingsIds(getGroupingsIds(event.getGroupings()));
			}
		}
	}

	private List<String> getGroupingsIds(List<Grouping> groupings) {
		List<String> groupingList = new ArrayList<>();
		for (Grouping grouping : groupings) {
			groupingList.add(grouping.getId().toString());
		}
		return groupingList;
	}

	public void processListingUpdate(FraudStatusUpdateRequest request, JmsTemplate fraudListingDeactivationMsgProducer) {
		final Long listingId = request.getListingId();
		Long fraudCheckStatusId = request.getFraudCheckStatusId();
		String fraudCheckStatus = request.getFraudCheckStatus();
		final Long sellerId = request.getSellerId();
		final Long fraudResolutionId = request.getFraudResolutionId();
		boolean isSellerDeactivated = request.isIsSellerDeactivated();
		boolean successFlag = true;
		try {
			updateListingStatus(listingId, fraudCheckStatusId, fraudCheckStatus, sellerId, fraudResolutionId,
					isSellerDeactivated);
			if (FraudCheckStatus.REJECTED.getId() == fraudCheckStatusId && isSellerDeactivated) {
				submitToListingDeactivationQueue(fraudListingDeactivationMsgProducer, request);
			}
		} catch (Throwable e) {
			log.error(
					"api_domain={}, api_method={}, message=\"Exception while calling Listing update\", listingId={}, fraudCheckStatusId={}, fraudCheckStatus={}, sellerId={}, fraudResolutionId={}, isSellerDeactivated={}, exception={}",
					DOMAIN, "processListingUpdate", listingId, fraudCheckStatusId, fraudCheckStatus, sellerId,
					fraudResolutionId, isSellerDeactivated, ExceptionUtils.getFullStackTrace(e),
					e);
			SHSystemException shException = new SHSystemException(
					"Exception while calling Listing update for listingId : " + listingId, e);
			shException.setErrorCode("inventory.listings.systemError");
			successFlag = false;
			throw shException;
		} finally {
			log.info(
					"api_domain={}, api_method={}, message=\"Complete calling Listing update\", listingId={}, fraudCheckStatusId={}, fraudCheckStatus={}, sellerId={}, fraudResolutionId={}, isSellerDeactivated={}, success={}",
					DOMAIN, "processListingUpdate", listingId, fraudCheckStatusId, fraudCheckStatus, sellerId,
					fraudResolutionId, isSellerDeactivated,  successFlag);
		}
	}

	private void updateListingStatus(final Long listingId, Long fraudCheckStatusId, String fraudCheckStatus,
			final Long sellerId, final Long fraudResolutionId, boolean isSellerDeactivated) {
		log.info(
				"api_domain={}, api_method={}, message=\"Calling Listing update\", listingId={}, fraudCheckStatusId={}, fraudCheckStatus={}, sellerId={}, fraudResolutionId={}, isSellerDeactivated={} ",
				DOMAIN, "processListingUpdate", listingId, fraudCheckStatusId, fraudCheckStatus, sellerId,
				fraudResolutionId, isSellerDeactivated );
		Listing listing = inventoryMgr.getListing(listingId);
		listing.setFraudCheckStatusId(fraudCheckStatusId);
		if (fraudResolutionId != null) {
			listing.setFraudResolutionId(fraudResolutionId);
		}
		if (FraudCheckStatus.REJECTED.getId() == fraudCheckStatusId) {
			listing.setSystemStatus(CommonConstants.INACTIVE);
			listing.setListingDeactivationReasonId(CommonConstants.AUTOMATED_FRAUD_REJECT_REASON);
		}
		inventoryMgr.updateListingOnly(listing);
		log.info(
				"api_domain={}, api_method={}, message=\"Listing update successful\", listingId={}, sellerId={} isSellerDeactivated={}",
				DOMAIN, "processListingUpdate", listingId, sellerId, isSellerDeactivated);
	}

	public void submitToListingDeactivationQueue(JmsTemplate fraudListingDeactivationMsgProducer,
			FraudStatusUpdateRequest request) {
		final Long listingId = request.getListingId();
		final Long sellerId = request.getSellerId();
		final Long fraudResolutionId = request.getFraudResolutionId();
		final int retryCount = request.getRetryCount();
		boolean isSellerDeactivated = request.isIsSellerDeactivated();
		try {
			log.info(
					"api_domain={}, api_method={}, message=\"Submitting to Listing Deactivation Queue\", listingId={}, sellerId={}, fraudResolutionId={}, isSellerDeactivated={}, retryCount={}",
					DOMAIN, "processListingUpdate", listingId, sellerId, fraudResolutionId, isSellerDeactivated);
			if (sellerId != null && isSellerDeactivated) {
				MessageCreator creator = new MessageCreator() {
					public javax.jms.Message createMessage(javax.jms.Session session) throws JMSException {
						javax.jms.MapMessage message = session.createMapMessage();
						message.setString("listingId", listingId.toString());
						message.setString("sellerId", sellerId.toString());
						message.setInt("retryCount", retryCount);
						if(fraudResolutionId!=null) {
							message.setString("fraudResolutionId", fraudResolutionId.toString());
						}
						return message;
					}
				};
				fraudListingDeactivationMsgProducer.send(creator);
				log.info(
						"api_domain={}, api_method={}, message=\"Submitting to Listing Deactivation Queue Complete\", listingId={}, sellerId={}, fraudResolutionId={} ",
						DOMAIN, "processListingUpdate", listingId, sellerId, fraudResolutionId );
			}
		} catch (Throwable t) {
			log.error(
					"api_domain={}, api_method={}, message=\"Error while Submitting to Listing Deactivation Queue\", listingId={}, sellerId={}, fraudResolutionId={}, exception={}",
					DOMAIN, "processListingUpdate", listingId, sellerId, fraudResolutionId, ExceptionUtils.getFullStackTrace(t), t);
		}
	}

	public boolean processLisitngDeactivationForSeller(FraudStatusUpdateRequest request) {
		Long listingId = request.getListingId();
		Long sellerId = request.getSellerId();
		boolean isSellerDeactivated = request.isIsSellerDeactivated();
		String key = this.getKey(sellerId.toString());
		boolean isSuccess = true;
		if (addSellerIdToCache(sellerId, key)) {
			String cacheCheck = (String) cacheStore.get(key);
			if (null == cacheCheck) {
				log.warn(
						"api_domain={}, api_method={}, message=\"FAILED adding to cache\", listingId={}, sellerId={} isSellerDeactivated={}",
						DOMAIN, "processListingDeactivation", listingId, sellerId, isSellerDeactivated);
			}
			log.info(
					"api_domain={}, api_method={}, message=\"Starting listing deactivation process\", listingId={}, sellerId={}",
					DOMAIN, "processListingDeactivation", listingId, sellerId);
			long startTime = System.currentTimeMillis();
			List<Listing> activeListings = inventoryMgr.getActiveListingsBySellerId(sellerId);
			long duration = System.currentTimeMillis() - startTime;
			log.info(
					"api_domain={}, api_method={}, message=\"Deactivating Active Listings \", listingId={}, sellerId={} listingCount={} dbCallDuration={}",
					DOMAIN, "processListingDeactivation", listingId, sellerId, activeListings.size(), duration);
			for (Listing activeListing : activeListings) {
				try {
					if(listingId.equals(activeListing.getId())) {
						log.info(
								"api_domain={}, api_method={}, message=\"Ignored deactivating the listing \", listingId={}, sellerId={} activeListing={}",
								DOMAIN, "processListingDeactivation", listingId, sellerId, activeListing.getId());
						continue;
					}
					updateListingSystemStatus(listingId, sellerId, activeListing);
				}catch(Throwable t) {
					log.error(
							"api_domain={}, api_method={}, message=\"Error while updating system status\", listingId={}, sellerId={} isSellerDeactivated={} activeListing={} exception={}",
							DOMAIN, "processListingDeactivation", listingId, sellerId, isSellerDeactivated, activeListing, ExceptionUtils.getFullStackTrace(t),t);
					isSuccess = false;
				}
			}
			cacheStore.remove(key);
			Serializable cacheRemoveCheck = cacheStore.get(key);
			if (null != cacheRemoveCheck) {
				log.warn(
						"api_domain={}, api_method={}, message=\"FAILED removing from cache\", listingId={}, sellerId={} isSellerDeactivated={}",
						DOMAIN, "processListingDeactivation", listingId, sellerId, isSellerDeactivated);
			}
		} else {
			log.warn(
					"api_domain={}, api_method={}, message=\"Found sellerId in cache\", listingId={}, sellerId={} isSellerDeactivated={}",
					DOMAIN, "processListingDeactivation", listingId, sellerId, isSellerDeactivated);
		}
		return isSuccess; 
	}

	private void updateListingSystemStatus(Long listingId, Long sellerId, Listing activeListing) {
		activeListing.setSystemStatus(CommonConstants.INACTIVE);
		activeListing.setListingDeactivationReasonId(CommonConstants.SELLER_DEACTIVATED_REASON);
		log.info(
				"api_domain={}, api_method={}, message=\"Updating Active Listing for deactivation\", listingId={}, sellerId={}, activeListing={}",
				DOMAIN, "processListingDeactivation", listingId, sellerId, activeListing.getId());
		inventoryMgr.updateSystemStatus(activeListing);
		log.info(
				"api_domain={}, api_method={}, message=\"Updating Active Listing for deactivation successful\", listingId={}, sellerId={}, activeListing={}",
				DOMAIN, "processListingDeactivation", listingId, sellerId, activeListing.getId());
	}
	

}
