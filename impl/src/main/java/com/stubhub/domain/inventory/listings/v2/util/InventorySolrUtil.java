package com.stubhub.domain.inventory.listings.v2.util;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.common.util.SolrJsonUtil;
import com.stubhub.domain.inventory.listings.v2.entity.ListingCheck;
import com.stubhub.newplatform.property.MasterStubHubProperties;

@Component
public class InventorySolrUtil {
  private final static Logger log = LoggerFactory.getLogger(InventorySolrUtil.class);
  private final static String TICKET_SYSTEM_STATUS = "ticketSystemStatus";
  private final static String QUANTITY_REMAIN = "quantityRemain";
  private final static String SALE_END_DATE = "saleEndDate";
  private final static String EVENT_ID ="eventId";
  private final static String LMS_APPROVED_STATUS_ID = "lmsApprovalStatusId";
  private final static String SECTION = "section";
  private final static String SEATS = "seats";
  private final static String INACTIVE = "INACTIVE";
  private final static String ACTIVE = "ACTIVE";
  private final static String PENDING_LOCK = "PENDING LOCK";
  private final static String INCOMPLETE = "INCOMPLETE";
  private final static String PENDING_PDF_REVIEW = "PENDING PDF REVIEW";
  private final static String ROW_DESC = "rowDesc";
  private final static String ID = "id";
  
  @Autowired
  private SolrJsonUtil jsonUtil;


  /**
   * Find out if SRS already been listed for that passed event
   * 
   * @param eventId
   * @param section
   * @param row
   * @param seats
   * @return
   */
  public ListingCheck isListingExists(Long eventId, Long sellerId, String section, String row,
      String seats, Long listingId) {
    ListingCheck listingCheck = new ListingCheck();
    if (seats == null || seats.trim().isEmpty()
        || seats.trim().equalsIgnoreCase(CommonConstants.GA)
        || seats.equalsIgnoreCase(CommonConstants.TO_BE_DEFINED)) {
      return listingCheck;
    } 
    if (section == null || section.trim().equals("")
        || section.trim().equalsIgnoreCase(CommonConstants.GENERAL_ADMISSION)
        || section.trim().equalsIgnoreCase(CommonConstants.GA)) {
      return listingCheck;
    }
    if (row == null || CommonConstants.GA_ROW_DESC.equalsIgnoreCase(row.trim())) {
      return listingCheck;
    }

    String jsonQuery = buildJsonQuery(eventId, section);
    log.info("_message=\"inventory Solr query for \" sellerId={}  query={}" ,sellerId, jsonQuery);

    String seatsInputArray[] = seats.split(",");
    List<String> rowSeatsInputList = new ArrayList<String>();
    if (row.contains(",")) {
      String rows[] = row.split(",");
      for (int i = 0; i < seatsInputArray.length / 2; i++) {
        rowSeatsInputList.add(rows[0] + "-" + seatsInputArray[i]);
      }
      for (int j = seatsInputArray.length / 2; j < seatsInputArray.length; j++) {
        rowSeatsInputList.add(rows[1] + "-" + seatsInputArray[j]);
      }
    } else {
      for (int k = 0; k < seatsInputArray.length; k++) {
        rowSeatsInputList.add(row + "-" + seatsInputArray[k]);
      }
    }


    try {
      String solrApiUrl = getProperty("solr.cloud.api.generic.url", null);
      JsonObject response = jsonUtil.getSolrResponse(solrApiUrl, jsonQuery, sellerId);
      if (response == null) {
        return listingCheck;
      }
      JsonArray docs = response.getJsonObject("response").getJsonArray("docs");

      if (docs == null || docs.size() == 0)
        return listingCheck;

      for (int n = 0; n < docs.size(); n++) {
        int id = docs.getJsonObject(n).getInt(ID);
        if(listingId != null && listingId.intValue() == id) {
          return listingCheck;
        }
        if(!docs.getJsonObject(n).containsKey(ROW_DESC) || !docs.getJsonObject(n).containsKey(SEATS)) {
          continue;
        }
        String rowDesc = docs.getJsonObject(n).getString(ROW_DESC);
        String seatsResp = docs.getJsonObject(n).getString(SEATS);
        String[] seatsRespArray = seatsResp.split(",");
        List<String> rowSeatsRespList = new ArrayList<String>();
        if (rowDesc.contains(",")) {
          if (seatsResp.contains("-")) {
            for (int k = 0; k < seatsRespArray.length; k++) {
              rowSeatsRespList.add(seatsRespArray[k]);
            }
          } else {
            String rowsRespArray[] = rowDesc.split(",");
            for (int i = 0; i < seatsRespArray.length / 2; i++) {
              rowSeatsRespList.add(rowsRespArray[0] + "-" + seatsRespArray[i]);
            }
            for (int j = seatsRespArray.length / 2; j < seatsRespArray.length; j++) {
              rowSeatsRespList.add(rowsRespArray[1] + "-" + seatsRespArray[j]);
            }
          }
        } else {
          for (int k = 0; k < seatsRespArray.length; k++) {
            rowSeatsRespList.add(rowDesc + "-" + seatsRespArray[k]);
          }
        }
        for (String rowSeatResp : rowSeatsRespList) {
          for (String rowSeatInput : rowSeatsInputList) {
            if (rowSeatResp.equalsIgnoreCase(rowSeatInput)) {
              listingCheck.setIsListed(true);
              listingCheck.setMessage(rowSeatResp);
              return listingCheck;
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("_message=\"Error occured while querying MCI message=", e.getMessage(), e);
    }
    return listingCheck;
  }

  private String buildJsonQuery(Long eventId, String section) {
    JsonObject value =
        Json.createObjectBuilder()
            .add("start",
                0)
            .add("rows",
            	500)
            .add("fieldList", Json.createArrayBuilder().add(ROW_DESC).add(SEATS).add(ID))
            .add("filter",
                Json.createObjectBuilder().add("must",
                    Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                            .add("match", Json.createObjectBuilder().add(EVENT_ID, eventId)))
                    .add(Json.createObjectBuilder().add("range",
                        Json.createObjectBuilder().add(SALE_END_DATE,
                            Json.createObjectBuilder().add("from", "NOW"))))
                        .add(
                            Json.createObjectBuilder()
                                .add("range",
                                    Json.createObjectBuilder().add(QUANTITY_REMAIN,
                                        Json.createObjectBuilder().add("from", "1"))))
                        .add(Json.createObjectBuilder().add("should",
                            Json.createArrayBuilder()
                                .add(Json.createObjectBuilder().add("match",
                                    Json.createObjectBuilder().add(TICKET_SYSTEM_STATUS,
                                        ACTIVE)))
                    .add(Json.createObjectBuilder().add("match",
                        Json.createObjectBuilder().add(TICKET_SYSTEM_STATUS, INACTIVE)))
                    .add(Json.createObjectBuilder().add("match",
                        Json.createObjectBuilder().add(TICKET_SYSTEM_STATUS, PENDING_LOCK)))
                    .add(Json.createObjectBuilder().add("match",
                        Json.createObjectBuilder().add(TICKET_SYSTEM_STATUS,
                            PENDING_PDF_REVIEW)))
                    .add(Json.createObjectBuilder().add("must", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                            .add("match", Json.createObjectBuilder().add(TICKET_SYSTEM_STATUS, INCOMPLETE)))
                        .add(Json.createObjectBuilder()
                            .add("match", Json.createObjectBuilder().add(LMS_APPROVED_STATUS_ID, 1)))))))
                .add(Json.createObjectBuilder().add("match",
                    Json.createObjectBuilder().add(SECTION, section)))
                .add(Json.createObjectBuilder().add("not", Json.createObjectBuilder().add("match",
                    Json.createObjectBuilder().add(SEATS, CommonConstants.GENERAL_ADMISSION))))

    ))

            .build();
    return value.toString();
  }
  
  protected String getProperty(String propertyName, String defaultValue) {
    return MasterStubHubProperties.getProperty(propertyName, defaultValue);
  }

}
