package com.stubhub.domain.inventory.listings.v2.util;

import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingTicketMediumXref;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.TicketMediumInfo;
import com.stubhub.domain.inventory.v2.enums.TicketMedium;
import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.*;

public class TicketMediumsManipulatorTest {

    // TODO Test if pre-populated ticket mediums are being replaced.
    Listing listing = null;
    SeatProductsContext seatProdContext = null;
    ListingRequest listingRequest = null;

    Map<TicketMedium, String> inHandDates = new HashMap<TicketMedium, String>() {
        {
            put(TicketMedium.MOBILE, "2019-10-01");
            put(TicketMedium.EXTMOBILE, "2019-10-03T10:00:00");
            put(TicketMedium.PAPER, "2019-10-05T10:00:00");
            put(TicketMedium.PDF, "2019-10-10T22:00:00");
            put(TicketMedium.BARCODE, "10-10-2019");
        }
    };

    private ListingRequest populateListingRequest() {
        ListingRequest listingRequest = new ListingRequest();
        List<TicketMediumInfo> ticketMediums = new ArrayList<>();
        ticketMediums.add(new TicketMediumInfo());
        ticketMediums.add(buildTicketMediumInfo(TicketMedium.MOBILE, inHandDates.get(TicketMedium.MOBILE), true));
        ticketMediums.add(buildTicketMediumInfo(TicketMedium.EXTMOBILE, inHandDates.get(TicketMedium.EXTMOBILE), true));
        ticketMediums.add(buildTicketMediumInfo(TicketMedium.PAPER, inHandDates.get(TicketMedium.PAPER), false));
        ticketMediums.add(buildTicketMediumInfo(TicketMedium.PDF, inHandDates.get(TicketMedium.PDF), false));
        ticketMediums.add(buildTicketMediumInfo(TicketMedium.BARCODE, inHandDates.get(TicketMedium.BARCODE), false));

        listingRequest.setTicketMediums(ticketMediums);
        return listingRequest;
    }

    private TicketMediumInfo buildTicketMediumInfo(TicketMedium ticketMedium, String inHandDate, boolean adjustInHandDate) {
        TicketMediumInfo ticketMediumInfo = new TicketMediumInfo();
        ticketMediumInfo.setAdjustInHandDate(adjustInHandDate);
        ticketMediumInfo.setInHandDate(inHandDate);
        ticketMediumInfo.setTicketMedium(ticketMedium);
        return ticketMediumInfo;
    }

    @BeforeMethod
    public void setUp() {
        listing = new Listing();
        seatProdContext = new SeatProductsContext(new Listing(), null, null, null);
        listingRequest = populateListingRequest();
    }

    @Test
    public void testCreateTicketMediumsHappyPath() {
        TicketMediumsManipulator.processTicketMediums(listing, seatProdContext, listingRequest);
        Assert.assertEquals(4, listing.getTicketMediums().size());
        for (ListingTicketMediumXref listingTicketMediumXref : listing.getTicketMediums()) {
            switch (TicketMedium.getById(listingTicketMediumXref.getTicketMediumId())) {

                case MOBILE:
                    Assert.assertEquals(true, listingTicketMediumXref.getActive());
                    Assert.assertEquals(true, listingTicketMediumXref.getAdjustInhandDateInd());
                    Assert.assertEquals(CommonConstants.LISTING_API_V2, listingTicketMediumXref.getCreatedBy());
                    Assert.assertTrue(Calendar.getInstance().after(listingTicketMediumXref.getCreatedDate()));
                    Assert.assertTrue(Calendar.getInstance().after(listingTicketMediumXref.getLastUpdatedDate()));
                    validateDate(TicketMedium.MOBILE, listingTicketMediumXref.getTicketInhandDate());
                    break;

                case EXTMOBILE:
                    Assert.assertEquals(true, listingTicketMediumXref.getActive());
                    Assert.assertEquals(true, listingTicketMediumXref.getAdjustInhandDateInd());
                    Assert.assertEquals(CommonConstants.LISTING_API_V2, listingTicketMediumXref.getCreatedBy());
                    Assert.assertTrue(Calendar.getInstance().after(listingTicketMediumXref.getCreatedDate()));
                    Assert.assertTrue(Calendar.getInstance().after(listingTicketMediumXref.getLastUpdatedDate()));
                    validateDate(TicketMedium.EXTMOBILE, listingTicketMediumXref.getTicketInhandDate());
                    break;

                case PAPER:
                    Assert.assertEquals(true, listingTicketMediumXref.getActive());
                    Assert.assertEquals(false, listingTicketMediumXref.getAdjustInhandDateInd());
                    Assert.assertEquals(CommonConstants.LISTING_API_V2, listingTicketMediumXref.getCreatedBy());
                    Assert.assertTrue(Calendar.getInstance().after(listingTicketMediumXref.getCreatedDate()));
                    Assert.assertTrue(Calendar.getInstance().after(listingTicketMediumXref.getLastUpdatedDate()));
                    validateDate(TicketMedium.PAPER, listingTicketMediumXref.getTicketInhandDate());
                    break;

                case PDF:
                    Assert.assertEquals(true, listingTicketMediumXref.getActive());
                    Assert.assertEquals(false, listingTicketMediumXref.getAdjustInhandDateInd());
                    Assert.assertEquals(CommonConstants.LISTING_API_V2, listingTicketMediumXref.getCreatedBy());
                    Assert.assertTrue(Calendar.getInstance().after(listingTicketMediumXref.getCreatedDate()));
                    Assert.assertTrue(Calendar.getInstance().after(listingTicketMediumXref.getLastUpdatedDate()));
                    validateDate(TicketMedium.PDF, listingTicketMediumXref.getTicketInhandDate());
                    break;
                default:
                    Assert.fail();
            }
        }

    }

    private void validateDate(TicketMedium ticketMedium, Calendar inHandDate) {
        Date date = inHandDate.getTime();

        String expectedInHandDate = inHandDates.get(ticketMedium);
        String actualInHandDate = null;

        if (expectedInHandDate.contains("T")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            actualInHandDate = sdf.format(date);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            actualInHandDate = sdf.format(date);
        }

        Assert.assertEquals(expectedInHandDate, actualInHandDate);

    }
}
