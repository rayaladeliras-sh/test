package com.stubhub.domain.inventory.listings.v2.newflow.adapter;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;

@Component("listingDbAdapter")
public class ListingDbAdapter {

  @Autowired
  private InventoryMgr inventoryMgr;

 /* public Listing convertToDBListing(ListingDTO listingDTO) {
    Listing listing = new Listing();
    listing.setEventId(listingDTO.getEventInfo().getEventId());
    listing.setSellerId(listingDTO.getSellerInfo().getSellerId());
    listing.setSystemStatus(listingDTO.getStatus().toString());
    // listing.setSystemStatus("ACTIVE");
    listing.setSection(listingDTO.getListingInfo().getSection());
    listing.setRow(listingDTO.getListingInfo().getRow());
    listing.setSeats(listingDTO.getListingInfo().getSeats());
    listing.setHideSeatInfoInd(listingDTO.getListingRequest().isHideSeats());
    listing.setQuantity(listingDTO.getListingInfo().getQuantity());
    listing.setQuantityRemain(listingDTO.getListingInfo().getQuantityRemain());
    listing.setSplitOption(listingDTO.getListingInfo().getSplitOption());
    listing.setSplitQuantity(listingDTO.getListingInfo().getSplitQuantity());
    listing.setSaleMethod(1L);
    listing.setEndDate(listingDTO.getFulfillmentInfo().getSaleEndDate());
    listing.setIpAddress("ip");
    listing.setTealeafSessionGuid(listingDTO.getListingRequest().getTealeafSessionId());
    listing.setThreatMatrixRefId(listingDTO.getListingRequest().getThreatMatrixSessionId());
    listing.setTicketClass(null);
    listing.setSellShStoreId(1);
    listing.setDisplayPricePerTicket(listingDTO.getPricingInfo().getDisplayPrice());
    listing.setListPrice(listingDTO.getPricingInfo().getListingPrice());
    listing.setTotalListingPrice(listingDTO.getPricingInfo().getTotalListingPrice());
    listing.setSellFeeValuePerTicket(listingDTO.getPricingInfo().getSellFeePerTicket());
    listing.setSellFeeDescription("sell fee");
    listing.setTotalSellFeeValue(listingDTO.getPricingInfo().getTotalSellFee());
    listing.setTotalSellerPayoutAmt(listingDTO.getPricingInfo().getTotalSellerPayout());
    listing.setSellerPayoutAmountPerTicket(listingDTO.getPricingInfo().getSellerPayoutPerTicket());
    listing.setFaceValue(listingDTO.getPricingInfo().getFaceValue());
    listing.setTicketCost(null);
    listing.setMinPricePerTicket(null);
    listing.setMaxPricePerTicket(null);
    listing.setComments("");
    listing.setSellerCCId(listingDTO.getPaymentInfo().getSellerCCId());
    listing.setSellerContactId(listingDTO.getSellerInfo().getSellerContactId());
    listing.setSellerPaymentTypeId(listingDTO.getPaymentInfo().getPaymentTypeId());
    listing.setConfirmOption(listingDTO.getFulfillmentInfo().getConfirmOptionId());
    listing.setDeliveryOption(listingDTO.getFulfillmentInfo().getDeliveryOptionId());
    listing.setListingSource(10);
    listing.setTicketMedium(listingDTO.getFulfillmentInfo().getTicketMediumId());
    listing.setLmsApprovalStatus(null);
    listing.setInhandDate(Calendar.getInstance());
    // listing.setDeclaredInhandDate(declaredInhandDate);
    listing.setCurrency(listingDTO.getEventInfo().getCurrency());
    listing.setExternalId(listingDTO.getListingRequest().getExternalListingId());
    listing.setCreatedDate(Calendar.getInstance());
    listing.setLastModifiedDate(Calendar.getInstance());
    listing.setDeferedActivationDate(null);
    listing.setListingType(1L);
    listing.setSectionScrubExcluded(false);
    listing.setSectionScrubSchedule(false);
    listing.setIsETicket(true);
    listing.setSaleEndDateIndicator(false);
    listing.setSellerCobrand("www");
    listing.setVenueConfigSectionsId(1L);
    listing.setFulfillmentDeliveryMethods(listingDTO.getFulfillmentInfo().getFmDmList());
    // listing.setContentApprovalStatusId(contentApprovalStatusId);
    // listing.setDomainId(domainId);
    listing.setCreatedByUserAgentId(
        inventoryMgr.getUserAgentID(listingDTO.getHeaderInfo().getUserAgent()));
    listing.setBusinessId(listingDTO.getSellerInfo().getBusinessId());
    listing.setBusinessGuid(listingDTO.getSellerInfo().getBusinessGuid());
    listing.setCreatedBy("inventoryv2");
    listing.setLastUpdatedBy("inventoryv2");
    listing.setStatus((short) 0);
    listing.setFraudCheckStatusId(null);

    listing.setTicketSeats(listingDTO.getListingInfo().getTicketSeats());
    listing.setSeatTraits(listingDTO.getListingInfo().getTicketTraits());
    return listing;
  }*/
}
