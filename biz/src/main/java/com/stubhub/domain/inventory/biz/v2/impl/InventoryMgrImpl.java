package com.stubhub.domain.inventory.biz.v2.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.biz.v2.intf.ListingTicketMediumMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.entity.ErrorEnum;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.common.util.ListingSearchCriteria;
import com.stubhub.domain.inventory.datamodel.dao.ExternalSystemDAO;
import com.stubhub.domain.inventory.datamodel.dao.ListingDAO;
import com.stubhub.domain.inventory.datamodel.dao.PDFParseDataRsnXrefDAO;
import com.stubhub.domain.inventory.datamodel.dao.TTOrderDAO;
import com.stubhub.domain.inventory.datamodel.dao.UserAgentDAO;
import com.stubhub.domain.inventory.datamodel.dao.VendorStubEventXrefDAO;
import com.stubhub.domain.inventory.datamodel.entity.ExternalSystem;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.ListingTicketMediumXref;
import com.stubhub.domain.inventory.datamodel.entity.PDFParseDataRsnXref;
import com.stubhub.domain.inventory.datamodel.entity.TTOrder;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.UserAgent;
import com.stubhub.domain.inventory.datamodel.entity.VendorStubEventXref;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;

/**
 * @author vichalasani
 */
@Component("inventoryMgr")
public class InventoryMgrImpl implements InventoryMgr {

    private static final Logger log = LoggerFactory.getLogger(InventoryMgrImpl.class);

    private static final String MODULENAME_CREATE_LISTING = "CreateListingAPI";

    private static final int MAX_GROUP_SIZE = 500;


    @Autowired
    private ListingDAO listingDAO;

    @Autowired
    private TicketSeatMgr ticketSeatMgr;

    @Autowired
    private ListingSeatTraitMgr listingSeatTraitMgr;

    @Autowired
    private ListingTicketMediumMgr listingTicketMediumMgr;

    @Autowired
    private UserAgentDAO userAgentDAO;

    @Autowired
    private PDFParseDataRsnXrefDAO pdfParseDataRsnXrefDAO;

    @Autowired
    private TTOrderDAO ttOrderDAO;

    @Autowired
    @Qualifier("inventoryExternalSystemDAO")
    private ExternalSystemDAO externalSystemDAO;

    @Autowired
    @Qualifier("inventoryVendorStubEventXrefDAO")
    private VendorStubEventXrefDAO vendorStubEventXrefDAO;


    public TTOrderDAO getTtOrderDAO() {
        return ttOrderDAO;
    }

    public void setTtOrderDAO(TTOrderDAO ttOrderDAO) {
        this.ttOrderDAO = ttOrderDAO;
    }

    @Override
    public QueryResponse getListings(ListingSearchCriteria criteria) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Listing getListing(Long listingId) {
        Listing listing = listingDAO.getListingById(listingId);
        return listing;
    }

    @Override
    @Transactional
    public Listing getListing(Long listingId, Locale locale) {
        Listing listing = listingDAO.getListingById(listingId);

        return listing;
    }

    @Override
    @Transactional
    public List<Listing> getActiveListingsBySellerId(Long sellerId) {
        List<Listing> listing = listingDAO.getActiveListingBySellerId(sellerId);
        return listing != null ? listing : Collections.EMPTY_LIST;
    }

    @Override
    @Transactional
    public List<Listing> addListings(List<Listing> listings) {
        if (listings != null && listings.size() > 0) {

            for (Listing l : listings) {
                addListing(l);
            }
        }
        return listings;
    }

    @Override
    @Transactional
    public Listing addListing(Listing listing) {
        try {
            if (listing.getIsPreDelivery() == Boolean.TRUE) {
                listing.setSystemStatus(ListingStatus.INCOMPLETE.toString());
                log.info("set listing to INCOMPLETE listingId={} due to isPreDelivery", listing.getId());
            }
            Listing newListing = listingDAO.addListing(listing);
            if (listing.getTicketSeats() != null) {
                for (TicketSeat seat : listing.getTicketSeats()) {
                    seat.setTicketId(newListing.getId());
                    ticketSeatMgr.addTicketSeat(seat);
                }
            }
            if (listing.getSeatTraits() != null) {
                for (ListingSeatTrait seatTrait : listing.getSeatTraits()) {
                    seatTrait.setTicketId(newListing.getId());
                    listingSeatTraitMgr.addSeatTrait(seatTrait);
                }
            }

            if (listing.getTicketMediums() != null) {
                for (ListingTicketMediumXref ltmx : listing.getTicketMediums()) {
                    ltmx.setTicketId(newListing.getId());
                    listingTicketMediumMgr.addTicketMedium(ltmx);
                }
            }
            return newListing;
        } catch (Exception e) {
            log.error("error_message=\"Error creating a listing\"" + "sellerId={}", listing.getSellerId(),
                    e);
            ListingError listingError = new ListingError(ErrorType.SYSTEMERROR, ErrorCode.SYSTEM_ERROR,
                    ErrorEnum.SYSTEM_ERROR.getMessage(), "");
            throw new ListingBusinessException(listingError);
        }
    }

    @Override
    @Transactional
    public List<Listing> updateListings(List<Listing> listings) {
        List<Listing> retListing = new ArrayList<Listing>(listings.size());
        if (listings.size() > 0) {

            for (Listing l : listings) {
                retListing.add(updateListing(l));
            }
        }
        return retListing;
    }

    @Override
    @Transactional
    public Listing updateListing(Listing listing) {
        try {
            Listing listingOut = listingDAO.updateListing(listing);
            // make sure you set the listing ID only
            listing.setId(listingOut.getId());
            listingOut.setSendLmsMessage(listing.getSendLmsMessage());
            listingOut.setSellerRequestedStatus(listing.getSellerRequestedStatus());
            listingOut.setStatusUpdated(listing.isStatusUpdated());
            listingOut.setPriceAdjusted(listing.isPriceAdjusted());
            listingOut.setInHandDateAdjusted(listing.isInHandDateAdjusted());
            if (listing.getTicketSeats() != null) {
                for (TicketSeat seat : listing.getTicketSeats()) {
                    if (seat.getTicketSeatId() == null) { // create
                        seat.setTicketId(listing.getId());
                        ticketSeatMgr.addTicketSeat(seat);
                    } else { // update
                        ticketSeatMgr.updateTicketSeat(seat);
                    }
                }
            }
            if (listing.getSeatTraits() != null) {
                for (ListingSeatTrait seatTrait : listing.getSeatTraits()) {
                    // always seatTrait.ticketId should == listing.id (or ticketId)
                    seatTrait.setTicketId(listing.getId());

                    if (seatTrait.isMarkForDelete()) {
                        listingSeatTraitMgr.deleteListingSeatTrait(seatTrait);
                    } else {
                        listingSeatTraitMgr.addSeatTrait(seatTrait);
                    }
                }
            }
            return listingOut;
        } catch (Exception be) {
            log.error("error_message=\"Error updating a listing\"" + " sellerId=" + listing.getSellerId(),
                    be);
            ListingError listingError =
                    new ListingError(ErrorType.SYSTEMERROR, ErrorCode.SYSTEM_ERROR, "", "eventId");
            throw new ListingBusinessException(listingError);
        }
    }

    @Override
    @Transactional
    public Listing updateListingOnly(Listing listing) {
        return listingDAO.updateListing(listing);
    }

    @Override
    @Transactional
    public Listing getListingBySellerIdExternalIdAndStatus(Long sellerId, String externalId) {
        List<Listing> listings = listingDAO.getListingBySellerIdAndExternalId(sellerId, externalId);
        if (listings != null) {
            for (Listing listing : listings) {
                if (listing.getSystemStatus().equals(ListingStatus.ACTIVE.toString())
                        || listing.getSystemStatus().equals(ListingStatus.INACTIVE.toString())) {
                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    if (!calendar.after(listing.getEndDate())
                            && (listing.getQuantityRemain() != null && listing.getQuantityRemain() != 0)) {
                        return listing;
                    }
                }
            }
        }
        return null;
    }

    @Override
    @Transactional
    public Listing getListingBySellerIdAndExternalId(Long sellerId, String externalId) {
        List<Listing> listings = listingDAO.getListingBySellerIdAndExternalId(sellerId, externalId);
        if (listings != null) {
            for (Listing listing : listings) {
                if (ListingStatus.ACTIVE.toString().equals(listing.getSystemStatus())
                        || ListingStatus.INCOMPLETE.toString().equals(listing.getSystemStatus())) {
                    return listing;
                }
            }
            return listings.get(0);
        }
        return null;
    }

    @Override
    @Transactional
    public Listing findListing(Long eventId, String section, String row, String seats) {
        return listingDAO.findListing(eventId, section, row, seats);
    }

    @Override
    @Transactional
    public Listing findListingBySectionRow(Long eventId, String section, String row) {
        return listingDAO.findListingBySectionRow(eventId, section, row);
    }

    @Override
    @Transactional
    public boolean hasSectionHadBadTerms(String section) {
        return listingDAO.hasSectionHadBadTerms(section);
    }

    @Override
    @Transactional
    public boolean hasRowHadBadTerms(String row) {
        return listingDAO.hasRowHadBadTerms(row);
    }

    @Override
    @Transactional
    public Long getSectionId(Long venueConfigId, String sectionDesc, String rowDesc,
                             String piggyBackRowDesc, int validate) {
        return listingDAO.getSectionId(venueConfigId, sectionDesc, rowDesc, piggyBackRowDesc, validate);
    }

    @Override
    @Transactional
    public Long getUserAgentID(String userAgentString) {
        Long hashVal = Long.valueOf(userAgentString.hashCode());
        UserAgent userAgent = userAgentDAO.findByHashId(hashVal);
        if (userAgent == null) {
            userAgent = new UserAgent();
            userAgent.setUserAgentHashId(hashVal);
            userAgent.setUserAgentStr(userAgentString);
            userAgent = userAgentDAO.persist(userAgent);
        }

        return userAgent.getUserAgentId();
    }

    @Override
    @Transactional
    public boolean isPDFPendingReviewAllowed(Long listingId) {
        if (listingId != null) {
            PDFParseDataRsnXref pdfParseDataRsnXref = pdfParseDataRsnXrefDAO.getByListingId(listingId);
            if (pdfParseDataRsnXref != null) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr#getListings(java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Listing> getListings(List<Long> listingIds) {
        List<Listing> dbListings = new ArrayList<Listing>();
        if (listingIds.size() <= MAX_GROUP_SIZE) {
            dbListings = listingDAO.getListings(listingIds);
        } else {
            for (int i = 0; i < listingIds.size(); i += MAX_GROUP_SIZE) {
                List<Long> sublist = new LinkedList<Long>(listingIds).subList(i,
                        i + MAX_GROUP_SIZE > listingIds.size() ? listingIds.size() : i + MAX_GROUP_SIZE);
                dbListings.addAll(listingDAO.getListings(sublist));
            }
        }
        return dbListings;
    }

    // SELLAPI-1181 09/02/15 START
    @Override
    @Transactional(readOnly = true)
    public List<Listing> getListings(Long sellerId, List<String> externalIds) {
        List<Listing> dbListings = new ArrayList<Listing>();
        if (externalIds.size() <= MAX_GROUP_SIZE) {
            dbListings = listingDAO.getListings(sellerId, externalIds);
        } else {
            for (int i = 0; i < externalIds.size(); i += MAX_GROUP_SIZE) {
                List<String> sublist = new LinkedList<String>(externalIds).subList(i,
                        i + MAX_GROUP_SIZE > externalIds.size() ? externalIds.size() : i + MAX_GROUP_SIZE);
                dbListings.addAll(listingDAO.getListings(sellerId, sublist));
            }
        }
        return dbListings;
    }
    // SELLAPI-1181 09/02/15 END

    @Override
    @Transactional
    public TTOrder addTTOrder(TTOrder ttOrder) {
        return ttOrderDAO.addTTOrder(ttOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<Listing> getAllPendingFlashListings(final Long sellerId) {
        return listingDAO.getAllPendingFlashListings(sellerId);
    }

    @Override
    @Transactional
    public HashMap<String, Boolean> isSeatsRequired(Long eventId) {
        HashMap<String, Boolean> booleanValues = new HashMap<String, Boolean>();
        boolean isSeatRequired = false;
        boolean isEticket = false;
        if (eventId != null) {
            VendorStubEventXref vendorStubEventXref = vendorStubEventXrefDAO.getByEventId(eventId);
            if (vendorStubEventXref != null && vendorStubEventXref.getExtSystemId() != null) {
                ExternalSystem extSystem = externalSystemDAO.findById(vendorStubEventXref.getExtSystemId());
                if (extSystem != null && extSystem.getSeatsRequiredInd() != null && extSystem.getSeatsRequiredInd() == 1L) {
                    isSeatRequired = true;
                }
                if (extSystem != null && extSystem.getPrimaryTicketVendorId() != null && (extSystem.getPrimaryTicketVendorId() == 9L || extSystem.getPrimaryTicketVendorId() == 10L)) {
                    isEticket = true;
                }
            }
        }
        booleanValues.put("isSeatRequired", isSeatRequired);
        booleanValues.put("isEticket", isEticket);
        return booleanValues;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Listing updateSystemStatus(Listing listing) {
        listingDAO.updateSystemStatus(listing.getId(), listing.getSystemStatus(),
                listing.getListingDeactivationReasonId(), listing.getLastUpdatedBy());
        return this.getListing(listing.getId());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Listing updateListingFraudStatus(Listing listing) {
        return this.updateListingOnly(listing);
    }

}
