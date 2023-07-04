package com.stubhub.domain.inventory.listings.v2.newflow.task;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.List;

import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;

public class TestUtil {

    public static Listing getDBListing(){
        Listing dbListing = new Listing();
        dbListing.setComments("comments");
        dbListing.setTicketMedium(TicketMedium.BARCODE.getValue());
        dbListing.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
        dbListing.setConfirmOption(new Integer(1));
        dbListing.setCreatedDate(Calendar.getInstance());
        dbListing.setCurrency(Currency.getInstance("USD"));
        dbListing.setDeclaredInhandDate(Calendar.getInstance());
        dbListing.setDeferedActivationDate(Calendar.getInstance());
        dbListing.setDeliveryOption(new Integer(1));
        dbListing.setSellerId(982743987234L);
        dbListing.setExternalId("872348723");
        dbListing.setSellerCCId(987687687L);
        dbListing.setSellerContactId(872387487324L);
        dbListing.setSellerPaymentTypeId(4L);
        dbListing.setLmsApprovalStatus(1);
        dbListing.setScrubbedSectionName("test section");
        com.stubhub.newplatform.common.entity.Money ticketCost = new com.stubhub.newplatform.common.entity.Money();
        ticketCost.setAmount(new BigDecimal(10.0));
        dbListing.setFaceValue(ticketCost);
        dbListing.setTicketCost(ticketCost);

        Calendar endDate = Calendar.getInstance();
        endDate.roll(Calendar.MONTH, true);
        if (endDate.getTime().getMonth() == 0) {// an edge case that can happen in December where the roll method sets the MONTH as 0
            endDate.roll(Calendar.YEAR, 1);
            endDate.roll(Calendar.MONTH, 1);
        }
        Long eventId = 11111L;
        Long listingId = 123456L;
        Long venueConfigSectionId = 2222211L;
        dbListing.setEndDate(endDate);
        dbListing.setEventId(eventId);
        dbListing.setId(listingId);
        dbListing.setInhandDate(Calendar.getInstance());
        dbListing.setQuantity(new Integer(1));
        dbListing.setQuantityRemain(new Integer(1));
        dbListing.setRow("Row 13");
        dbListing.setSaleMethod(1L);
        dbListing.setSeats("1");
        dbListing.setSection("section Name");
        dbListing.setSplitOption(new Short("1"));
        dbListing.setSplitQuantity(new Integer(2));
        dbListing.setVenueConfigSectionsId(venueConfigSectionId);
        dbListing.setSystemStatus("ACTIVE");
        dbListing.setDisplayPricePerTicket(new com.stubhub.newplatform.common.entity.Money());

        com.stubhub.newplatform.common.entity.Money listPrice = new com.stubhub.newplatform.common.entity.Money();
        listPrice.setAmount(new BigDecimal(10.0));
        listPrice.setCurrency("USD");
        dbListing.setListPrice(listPrice);

        com.stubhub.newplatform.common.entity.Money faceValue = new com.stubhub.newplatform.common.entity.Money();
        faceValue.setAmount(new BigDecimal(20.0));
        dbListing.setFaceValue(faceValue);
        List<ListingSeatTrait> seatTraits = new ArrayList<ListingSeatTrait>();
        ListingSeatTrait seatTrait = new ListingSeatTrait();
        seatTrait.setSupplementSeatTraitId(1L);
        seatTraits.add(seatTrait);
        dbListing.setSeatTraits(seatTraits);

        return dbListing;
    }
}
