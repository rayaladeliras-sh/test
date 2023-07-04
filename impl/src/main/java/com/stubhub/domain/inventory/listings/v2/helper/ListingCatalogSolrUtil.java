/**
 * 
 */
package com.stubhub.domain.inventory.listings.v2.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.inventory.common.util.SolrJsonUtil;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

/**
 * @author sjayaswal
 *
 */
@Component
public class ListingCatalogSolrUtil {

  private final static Logger log = LoggerFactory.getLogger(ListingCatalogSolrUtil.class);
  private static final int MAX_GROUP_SIZE = 500;
  private static final String TICKET_ID = "id";
  private static final String EVENT_ID = "eventId";
  private static final String EXTERNAL_LISTING_ID = "externalListingId";
  private static final String SELLER_ID = "sellerId";
  private final static String TICKET_SYSTEM_STATUS = "ticketSystemStatus";
  private final static String ACTIVE = "ACTIVE";
  private final static String SALE_END_DATE = "saleEndDate";
  private final static String QUANTITY_REMAIN = "quantityRemain";

  @Autowired
  private SolrJsonUtil jsonUtil;
  
  @Autowired
  private SvcLocator svcLocator;
  
  public List<Long> getEventByListingId(List<ListingRequest> updateListingRequestList,
      Long sellerId) {
    List<Long> missedListingIds = new ArrayList<>();
    int batchCounter = 0;
    int itemsProcessed = 0;
    StringBuilder sb = new StringBuilder();
    List<ListingRequest> updateList = new ArrayList<>();
    for (ListingRequest request : updateListingRequestList) {
      sb.append(request.getListingId());
      updateList.add(request);
      batchCounter++;
      itemsProcessed++;
      if (batchCounter < MAX_GROUP_SIZE && itemsProcessed < updateListingRequestList.size()) {
        sb.append(",");
      }
      if (batchCounter >= MAX_GROUP_SIZE) {
        missedListingIds.addAll(populateEventId(sb.toString(), updateList, sellerId));
        sb = new StringBuilder();
        updateList = new ArrayList<ListingRequest>();
        batchCounter = 0;
      }
    }
    if (batchCounter > 0) {
      missedListingIds.addAll(populateEventId(sb.toString(), updateList, sellerId));
    }

    return missedListingIds;
  }

	private Set<Long> populateEventId(String listingIds, List<ListingRequest> updateList, Long sellerId) {
		Map<Long, ListingRequest> listingIdEventIdMap = new HashMap<>();
		for (ListingRequest request : updateList) {
			listingIdEventIdMap.put(request.getListingId(), request);
		}
		String jsonQuery = buildJsonEventId(listingIds);

		log.debug("_message=\"Solr query for \" sellerId={} {}", sellerId, jsonQuery);

		try {
			String solrApiUrl = getProperty("solr.cloud.api.generic.url", null);
			JsonObject response = jsonUtil.getSolrResponse(solrApiUrl, jsonQuery, sellerId);
			if (response == null) {
				return listingIdEventIdMap.keySet();
			}
			JsonArray docs = response.getJsonObject("response").getJsonArray("docs");
			if (docs == null || docs.size() == 0) {
				return listingIdEventIdMap.keySet();
			}
			for (int i = 0; i < docs.size(); i++) {
				Integer listingId = null, eventId = null;
				String externalListingId = null;
				if (docs.getJsonObject(i).containsKey(TICKET_ID)) {
					listingId = docs.getJsonObject(i).getInt(TICKET_ID);
				}
				if (docs.getJsonObject(i).containsKey(EVENT_ID)) {
				    eventId = docs.getJsonObject(i).getInt(EVENT_ID);
				}
				// SELLAPI 2284
				if (docs.getJsonObject(i).containsKey(EXTERNAL_LISTING_ID)) {
					externalListingId = docs.getJsonObject(i).getString(EXTERNAL_LISTING_ID);
				}
				if (listingId != null) {
					ListingRequest request = listingIdEventIdMap.remove(listingId.longValue());
					if (eventId != null) {
						request.setEventId(eventId.toString());
					}
					if (externalListingId != null) {
						request.setExternalListingId(externalListingId);
					}
				}
			}

		} catch (Exception e) {
			log.error("_message=\"Error occured while querying MCI message={}", e.getMessage(), e);
		}

		return listingIdEventIdMap.keySet();
	}



  public List<String> getEventByExternalId(Long sellerId, List<ListingRequest> updateExternalList) {
    List<String> missedExternalIds = new ArrayList<>();
    int batchCounter = 0;
    int itemsProcessed = 0;
    StringBuilder sb = new StringBuilder();
    List<ListingRequest> updateList = new ArrayList<>();
    for (ListingRequest request : updateExternalList) {
      sb.append(request.getExternalListingId());
      updateList.add(request);
      batchCounter++;
      itemsProcessed++;
      if (batchCounter < MAX_GROUP_SIZE && itemsProcessed < updateExternalList.size()) {
        sb.append(",");
      }
      if (batchCounter >= MAX_GROUP_SIZE) {
        missedExternalIds
            .addAll(populateEventIdForExternalIds(sellerId, sb.toString(), updateList));
        sb = new StringBuilder();
        updateList = new ArrayList<ListingRequest>();
        batchCounter = 0;
      }
    }
    if (batchCounter > 0) {
      missedExternalIds.addAll(populateEventIdForExternalIds(sellerId, sb.toString(), updateList));
    }
    return missedExternalIds;
  }

	private Set<String> populateEventIdForExternalIds(Long sellerId, String externalIds,
			List<ListingRequest> updateList) {

		// convert the given list into map to facilitate object access for a
		// given key
		Map<String, ListingRequest> externalIdEventIdMap = new HashMap<String, ListingRequest>();
		for (ListingRequest request : updateList) {
			externalIdEventIdMap.put(request.getExternalListingId(), request);
		}
		String jsonQuery = buildJsonEventIdForExternalIds(externalIds, sellerId);

		log.debug("_message=\"Listing catalog Solr query for\" sellerId={} {}", sellerId, jsonQuery);

		// trigger solr query and return missing external id list
		try {
			String solrApiUrl = getProperty("solr.cloud.api.generic.url", null);
			JsonObject response = jsonUtil.getSolrResponse(solrApiUrl, jsonQuery, sellerId);
			if (response == null) {
				return externalIdEventIdMap.keySet();
			}
			JsonArray docs = response.getJsonObject("response").getJsonArray("docs");
			if (docs == null || docs.size() == 0) {
				return externalIdEventIdMap.keySet();
			}
			for (int i = 0; i < docs.size(); i++) {
				Integer listingId = null, eventId = null;
				String externalListingId = null;
				if (docs.getJsonObject(i).containsKey(TICKET_ID)) {
				    listingId = docs.getJsonObject(i).getInt(TICKET_ID);
				}
				if (docs.getJsonObject(i).containsKey(EVENT_ID)) {
				    eventId = docs.getJsonObject(i).getInt(EVENT_ID);
				}
				// SELLAPI 2284
				if (docs.getJsonObject(i).containsKey(EXTERNAL_LISTING_ID)) {
					externalListingId = docs.getJsonObject(i).getString(EXTERNAL_LISTING_ID);
				}
				if (externalListingId != null) {
					ListingRequest request = externalIdEventIdMap.remove(externalListingId);
					if (eventId != null) {
						request.setEventId(eventId.toString());
					}
					if (listingId != null) {
						request.setListingId(listingId.longValue());
					}
				}
			}

		} catch (Exception e) {
			log.error("_message=\"Error occured while querying MCI message=", e.getMessage(), e);
		}

		return externalIdEventIdMap.keySet();
	}

  public String buildJsonEventIdForExternalIds(String externalListingIds, Long sellerId) {
    String[] ids = externalListingIds.split(",");
    JsonArrayBuilder builder = Json.createArrayBuilder();
    for (int i = 0; i < ids.length; i++) {
      builder.add(ids[i]);
    }
    JsonArray parameters = builder.build();

    JsonObject value = Json.createObjectBuilder().add("start", 0)
        .add("fieldList",
            Json.createArrayBuilder().add(TICKET_ID).add(EVENT_ID).add(EXTERNAL_LISTING_ID))
        .add("filter",
            Json.createObjectBuilder().add("must", Json.createArrayBuilder()
                .add(Json.createObjectBuilder().add("match",
                    Json.createObjectBuilder().add(SELLER_ID, sellerId)))
                .add(Json.createObjectBuilder().add("match",
                    Json.createObjectBuilder().add(TICKET_SYSTEM_STATUS, ACTIVE)))
                .add(Json.createObjectBuilder().add("range",
                        Json.createObjectBuilder().add(SALE_END_DATE,
                            Json.createObjectBuilder().add("from", "NOW"))))
                .add(Json.createObjectBuilder().add("range",
                        Json.createObjectBuilder().add(QUANTITY_REMAIN,
                            Json.createObjectBuilder().add("from", "1"))))
                .add(Json.createObjectBuilder().add("in", Json.createObjectBuilder().add(
                    EXTERNAL_LISTING_ID, Json.createObjectBuilder().add("values", parameters))))))
        .build();
    return value.toString();
  }

  public String buildJsonEventId(String listingIds) {
    String[] ids = listingIds.split(",");
    JsonArrayBuilder builder = Json.createArrayBuilder();
    for (int i = 0; i < ids.length; i++) {
      builder.add(ids[i]);
    }
    JsonArray parameters = builder.build();
    JsonObject value =
        Json.createObjectBuilder().add("start", 0)
            .add("fieldList",
                Json.createArrayBuilder().add(TICKET_ID).add(EVENT_ID)
                    .add(EXTERNAL_LISTING_ID))
            .add("filter",
                Json.createObjectBuilder()
                    .add("must",
                        Json.createArrayBuilder()
                            .add(Json.createObjectBuilder().add("in",
                                Json.createObjectBuilder().add(TICKET_ID,
                                    Json.createObjectBuilder().add("values", parameters))))))
        .build();
    return value.toString();
  }
  
  protected String getProperty(String propertyName, String defaultValue) {
    return MasterStubHubProperties.getProperty(propertyName, defaultValue);
  }

}
