package com.stubhub.domain.inventory.listings.v2.util;

import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingTicketMediumXref;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.TicketMediumInfo;
import com.stubhub.newplatform.common.util.DateUtil;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TicketMediumsManipulator {

    private final static Logger log = Logger.getLogger(TicketMediumsManipulator.class);


    private static List<ListingTicketMediumXref> createTicketMediums(ListingRequest request) {

        List<ListingTicketMediumXref> ticketMediumXrefs = new ArrayList<>();

        if(request.getTicketMediums() != null) {
            for(TicketMediumInfo tm : request.getTicketMediums()) {

                if(tm.getTicketMedium() != null && tm.getInHandDate() !=null) {
                    // Validate inHandDate string and fetch the inHandDate
                    Calendar inHandDate = getInHandDate(tm.getInHandDate());

                    if (inHandDate != null) {
                        ListingTicketMediumXref ltmx = new ListingTicketMediumXref();
                        ltmx.setTicketInhandDate(inHandDate);
                        ltmx.setActive(true);
                        ltmx.setAdjustInhandDateInd(tm.getAdjustInHandDate());
                        ltmx.setTicketMediumId(tm.getTicketMedium().getId());

                        Calendar utcNow = DateUtil.getNowCalUTC();
                        ltmx.setCreatedDate(utcNow);
                        ltmx.setLastUpdatedDate(utcNow);

                        ltmx.setCreatedBy(CommonConstants.LISTING_API_V2);
                        ltmx.setLastUpdatedBy(CommonConstants.LISTING_API_V2);
                        ticketMediumXrefs.add(ltmx);
                    }
                }
            }
        }
        return ticketMediumXrefs;
    }

    private static Calendar getInHandDate(String inHandDateStr) {
        try {
            // TODO Use joda utils to comply with ISO 8601 date format
            SimpleDateFormat sdf = null;
            if(inHandDateStr.contains("T")) {
                sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            } else {
                sdf = new SimpleDateFormat("yyyy-MM-dd");
            }
            sdf.setLenient(false);

            Calendar inHandDate = Calendar.getInstance();
            Date dInHandDate = sdf.parse(inHandDateStr);
            inHandDate.setTime(dInHandDate);

            return inHandDate;
        } catch(ParseException ex) {
            // NOTE: This logic is only to persist the provided inhand dates. We don't want to block the listing from getting
            // created if there is a parse exception here. The actual inHand date and the ticketMedium (deliveryOption)
            // are provided as different parameters. If this is the source-of-truth for determining the deliveryOption
            // then we need to block the listing creation by throwing an exception.
            log.error("errorCode=TICKET_MEDIUM_IN_HAND_DATE_PARSE_ERROR _message=\"Error parsing in-hand date\"");
            return null;
        }
    }


    /**
     * processTicketMediums
     *
     * @param listing
     * @param seatProdContext
     * @param listingRequest
     *
     */
    public static void processTicketMediums(Listing listing, SeatProductsContext seatProdContext,
                                             ListingRequest listingRequest) {
        // SELLAPI-3608
        if ( seatProdContext.isCreate() ) {
            listing.setTicketMediums(createTicketMediums(listingRequest));
        }
    }
}