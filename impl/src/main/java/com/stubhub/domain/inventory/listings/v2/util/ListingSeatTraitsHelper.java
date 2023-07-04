package com.stubhub.domain.inventory.listings.v2.util;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.entity.VenueConfigSectionOrZone;
import com.stubhub.domain.inventory.listings.v2.helper.GlobalRegistryServiceHelper;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.TicketTrait;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.newplatform.common.util.DateUtil;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component("listingSeatTraitsHelper")
public class ListingSeatTraitsHelper {
    private final static Long AISLE = 101l;
    private final static Long PARKING_PASS = 102l;
    private final static Long TRADITIONAL_HARD_TICKET = 311l;
    private final static Long PIGGYBACK_SEATING = 501l;
    private final static Long FULL_SUITE = 2566l;
    private static final int LISTING_SOURCE_ID_STH = 8;
    private static final String TICKET_FEATURE = "Ticket Feature";

    @Autowired
    ListingSeatTraitMgr listingSeatTraitMgr;

    private static final String GET_VENUE_CONFIG_METADATA_API = "get_venue_config_metadata_api";

    @Autowired
    private SvcLocator svcLocator;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private GlobalRegistryServiceHelper globalRegServiceHelper;

    private static final Logger logger = LoggerFactory.getLogger(ListingSeatTraitsHelper.class);

    public void processSeatTraits(SeatProductsContext seatProdContext) {
        Listing currentListing = seatProdContext.getCurrentListing();
        ListingRequest updateListingRequest = seatProdContext.getListingRequest();
        

        List<ListingSeatTrait> listingTraitsfromDB = currentListing.getSeatTraits();
        // if update, try load old traits from DB
        if (!seatProdContext.isCreate() && listingTraitsfromDB == null) {
            listingTraitsfromDB = listingSeatTraitMgr.findSeatTraits(currentListing.getId());
        }

        // if no traits found, create an empty list
        if (listingTraitsfromDB == null) {
            listingTraitsfromDB = new ArrayList<ListingSeatTrait>();
        }
        boolean seatTraitChanged = false;
        // passed traits processing
        if (updateListingRequest.getTicketTraits() != null && updateListingRequest.getTicketTraits().size() > 0) {
            // get all passed
            StringBuilder structuredComments = new StringBuilder();
            List<Long> seatTraitIds = new ArrayList<Long>();
            List<String> seatTraitNames = new ArrayList<String>();

            // seatTraitIds hold the to be updated status = current + request
            for (ListingSeatTrait trait : listingTraitsfromDB) {
                seatTraitIds.add(trait.getSupplementSeatTraitId());
            }

            logger.debug("load from DB seatTraitIds={} listingId={}", seatTraitIds, currentListing.getId());

            for (TicketTrait trait : updateListingRequest.getTicketTraits()) {
                if (trait.getName() != null) {
                    structuredComments.append(trait.getName());
                    structuredComments.append(',');
                    seatTraitNames.add(trait.getName());
                }
                if (trait.getId() != null) {
                    // also consider the operation type for seat trait
                    Long id = new Long(trait.getId());
                    if (trait.getOperation() == Operation.ADD) {
                        if (!seatTraitIds.contains(id)) {
                            seatTraitIds.add(id);
                        }
                    } else if (trait.getOperation() == Operation.DELETE) {
                        seatTraitIds.remove(id);
                    }
                }
            }
            String allComments = structuredComments.toString();
            if (allComments.length() > 0) {
                allComments = allComments.substring(0, structuredComments.length() - 1);
            }

            // now figure out all seat traits ids
            seatTraitIds = validateAllSeatTraits(currentListing, seatTraitIds, seatTraitNames,
                    allComments, listingTraitsfromDB, updateListingRequest.getTicketTraits(),
                    seatProdContext.getListingRequest().getSplitOption() != null);

            // add the new traits
            for (TicketTrait ticketTrait : updateListingRequest.getTicketTraits()) {

                Long id = ticketTrait.getId() == null ? -1 : Long.valueOf(ticketTrait.getId());

                if (Operation.ADD.equalsEnum(ticketTrait.getOperation())) {

                    if (!seatTraitIds.contains(id))
                        continue;

                    ListingSeatTrait dbtrait = getTraitAlreadyExist(ticketTrait, listingTraitsfromDB);
                    if (dbtrait == null) {
                        if (id == PARKING_PASS && !seatProdContext.isCreate()) {
                          if (DeliveryOption.PREDELIVERY.getValue() == currentListing.getDeliveryOption()
                              && ListingStatus.ACTIVE.name().equals(currentListing.getSystemStatus())) {
                            logger.error(
                                "message=\"BAD Request to ADD parking pass to a predelivered listing\", listingId={} ",
                                currentListing.getId());
                            throw new ListingException(ErrorType.BUSINESSERROR,
                                ErrorCodeEnum.listingActionNotallowed,
                                "Cannot add parking pass to a predelivered listing");
                          }
                        }
                        ListingSeatTrait seatTrait = new ListingSeatTrait();
                        seatTrait.setActive(true);
                        seatTrait.setSellerSpecifiedInd(true);
                        seatTrait.setExtSystemSpecifiedInd(false);
                        seatTrait.setSupplementSeatTraitId(id);
                        seatTrait.setTicketId(currentListing.getId());
                        Calendar utcNow = DateUtil.getNowCalUTC();
                        seatTrait.setCreatedDate(utcNow);
                        seatTrait.setLastUpdatedDate(utcNow);
                        seatTrait.setCreatedBy(CommonConstants.LISTING_API_V2);
                        seatTrait.setLastUpdatedBy(CommonConstants.LISTING_API_V2);
                        seatTrait.setMarkForDelete(false);
                        listingTraitsfromDB.add(seatTrait);

                        seatTraitChanged = true;
                        logger.debug("mark add supplementSeatTraitId={}", id);
                    }

                    if (id == PARKING_PASS) {
                        // add parking pass if does not exist
                        _manageParkingPassIfExist(seatProdContext, true);
                    }
                } else if (Operation.DELETE.equalsEnum(ticketTrait.getOperation())) {
                    ListingSeatTrait dbtrait = getTraitAlreadyExist(ticketTrait, listingTraitsfromDB);
                    if (dbtrait != null) {
                        dbtrait.setMarkForDelete(true);
                        seatTraitChanged = true;
                        logger.debug("mark delete supplementSeatTraitId={}", dbtrait.getSupplementSeatTraitId());
                        if (id == PARKING_PASS) {    // if parking pass, delete
                            _manageParkingPassIfExist(seatProdContext, false);
                        }
                    }
                }
            }
        }

        if (StringUtils.trimToNull(updateListingRequest.getComments()) != null && (updateListingRequest.getTicketTraits() == null || updateListingRequest.getTicketTraits().isEmpty())) { 
            List<Long> seatTraitIds = listingSeatTraitMgr.parseComments(currentListing.getEventId(), updateListingRequest.getComments());
            if (!seatProdContext.isCreate() && !listingTraitsfromDB.isEmpty()) {
                for (ListingSeatTrait dbTrait : listingTraitsfromDB) {
                    if (!seatTraitIds.contains(dbTrait.getSupplementSeatTraitId())) {
                        dbTrait.setMarkForDelete(true);
                        seatTraitChanged = true;
                        logger.debug("mark for delete supplementSeatTraitId={}", dbTrait.getSupplementSeatTraitId());
                        if (dbTrait.getSupplementSeatTraitId() == PARKING_PASS) {    // if parking pass, delete
                            _manageParkingPassIfExist(seatProdContext, false);
                        }
                    }
                }
            }
            if (!seatTraitIds.isEmpty()) {
                seatTraitIds = validateSeatTraitIds(currentListing, seatTraitIds, seatProdContext.getListingRequest().getSplitOption() != null);
                // add the new traits
                for (Long seatTraitId : seatTraitIds) {
                    ListingSeatTrait dbTrait = getAlreadyExistingSeatTrait(seatTraitId, listingTraitsfromDB);
                    if (dbTrait == null) {
                        if (seatTraitId == PARKING_PASS && !seatProdContext.isCreate()) {
                          if (DeliveryOption.PREDELIVERY.getValue() == currentListing.getDeliveryOption()
                              && ListingStatus.ACTIVE.name().equals(currentListing.getSystemStatus())) {
                            logger.error(
                                "message=\"BAD Request to ADD parking pass to a predelivered listing\", listingId={} ",
                                currentListing.getId());
                            throw new ListingException(ErrorType.BUSINESSERROR,
                                ErrorCodeEnum.listingActionNotallowed,
                                "Cannot add parking pass to a predelivered listing");
                          }
                        }
                      
                        ListingSeatTrait seatTrait = new ListingSeatTrait();
                        seatTrait.setActive(true);
                        seatTrait.setSellerSpecifiedInd(true);
                        seatTrait.setExtSystemSpecifiedInd(false);
                        seatTrait.setSupplementSeatTraitId(seatTraitId);
                        seatTrait.setTicketId(currentListing.getId());
                        Calendar utcNow = DateUtil.getNowCalUTC();
                        seatTrait.setCreatedDate(utcNow);
                        seatTrait.setLastUpdatedDate(utcNow);
                        seatTrait.setCreatedBy(CommonConstants.LISTING_API_V2);
                        seatTrait.setLastUpdatedBy(CommonConstants.LISTING_API_V2);
                        seatTrait.setMarkForDelete(false);
                        listingTraitsfromDB.add(seatTrait);
                        seatTraitChanged = true;
                        logger.debug("mark for add supplementSeatTraitId={}", seatTraitId);
                        if (seatTraitId == PARKING_PASS) {
                            // add parking pass if does not exist
                            _manageParkingPassIfExist(seatProdContext, true);
                        }
                    }
                }
            }
        }

        // if changes, set back in listing
        if (seatTraitChanged) {
            logger.debug("seatTraitChanged, listingTraitsfromDB.size={}", listingTraitsfromDB.size());
            logger.info("*************************************************** seatTraitChanged, listingTraitsfromDB.size={}", listingTraitsfromDB.size());
            currentListing.setSeatTraits(listingTraitsfromDB);
        }

        // set seats back if there are any ticket changes (as result of adding/removing parking pass)
        if (seatProdContext.getIsDBTicketsChanged()) {
            seatProdContext.setTicketSeatsBackInCurrentListing();
        }

        // check new seat traits related to passed parking pass and piggyback logic and set it currentListing is applicable
        updateTraitsInferredByPassedListing(seatProdContext);
    }

    /**
     * _addParkingPassIfNotExist
     *
     * @param seatProdContext
     * @param if              add == true then add, otherwise delete
     */
    private void _manageParkingPassIfExist(SeatProductsContext seatProdContext, boolean add) {
        List<TicketSeat> seats = seatProdContext.getTicketSeatsFromCache();

        TicketSeat parkingPass = seatProdContext.getCurrentParkingPass();

        // add parking pass if not there
        if (parkingPass != null) {    // if there
            if (!add) {                // if delete
                seatProdContext.deleteTicketSeat(parkingPass);
                setToPendingLock(seatProdContext.getCurrentListing());
            }
        } else if (add) {// if not there and add, then add
            parkingPass = TicketSeatUtils.createParkingTicketSeat(seatProdContext.getCurrentListing());

            // TODO: note these two calls need to be consolidated (as well ad dbicketsChanged indicator)
            seatProdContext.addTicketSeat(parkingPass);
            seatProdContext.addToNewlyAddedTicketSeatList(parkingPass);
        }
    }

    /**
     * _doesTraitAlreadyExist
     *
     * @param ticketTrait
     * @param listingTraitsfromDB
     * @return
     */
    private ListingSeatTrait getTraitAlreadyExist(TicketTrait trait, List<ListingSeatTrait> traitsfromDB) {
        if (trait.getId() != null) {
            Long id = Long.valueOf(trait.getId());

            for (ListingSeatTrait lt : traitsfromDB) {
                if (lt.getSupplementSeatTraitId() != null && lt.getSupplementSeatTraitId().equals(id))
                    return lt;
            }
        }
        return null;
    }

    private ListingSeatTrait getAlreadyExistingSeatTrait(Long seatTraitId, List<ListingSeatTrait> traitsfromDB) {
        for (ListingSeatTrait lt : traitsfromDB) {
            if (lt.getSupplementSeatTraitId() != null && lt.getSupplementSeatTraitId().equals(seatTraitId)) {
                return lt;
            }
        }
        return null;
    }

    /**
     * validateAllSeatTraits and return the new processed
     *
     * @param currentListing
     * @param seatTraitIds   - is supplement_seat_trait_id rather than listing_seat_trait_id
     * @param seatTraitNames
     * @param allComments
     * @return all validated IDs
     */
    private List<Long> validateAllSeatTraits(Listing currentListing, List<Long> seatTraitIds,
                                             List<String> seatTraitNames, String allComments, List<ListingSeatTrait> listingTraitsfromDB,
                                             List<TicketTrait> passedTraits, boolean splitOptionPassed) {
        logger.debug("validateAllSeatTraits input seatTraitIds={}", seatTraitIds);

        List<Long> processedIds = new ArrayList<Long>();

        if (seatTraitIds != null && seatTraitIds.size() > 0) {
            if (currentListing.getEvent() != null && currentListing.getEvent().getTicketTraitId() != null) {
                List<Long> applicableSeatTraitList = currentListing.getEvent().getTicketTraitId();
                if (applicableSeatTraitList != null && applicableSeatTraitList.size() > 0) {
                    for (Long structuredCommentId : seatTraitIds) {
                        if (applicableSeatTraitList.contains(structuredCommentId)) {
                            processedIds.add(structuredCommentId);
                        }
                    }
                }
            }
        }

        // if ticket trait names are passed (note: also allComments != null is this case)
        if (seatTraitNames != null && seatTraitNames.size() > 0) {
            if (currentListing.getEvent() != null && currentListing.getEvent().getTicketTraitId() != null) {
                List<com.stubhub.domain.catalog.events.intf.TicketTrait> applicableSeatTraitList =
                        currentListing.getEvent().getTicketTrait();

                // get all event traits
                if (applicableSeatTraitList != null && applicableSeatTraitList.size() > 0) {
                    for (String seatTraitName : seatTraitNames) {
                        for (com.stubhub.domain.catalog.events.intf.TicketTrait ticketTrait : applicableSeatTraitList) {

                            // find a trait by name and get its id
                            if (seatTraitName.equalsIgnoreCase(ticketTrait.getName())) {
                                if (!processedIds.contains(ticketTrait.getId())) {
                                    processedIds.add(ticketTrait.getId());
                                }

                                // set the id back in passed ticketTrait
                                TicketTrait tt = _findTicketTrait(seatTraitName, ticketTrait.getId(), passedTraits);
                                if (tt != null) {
                                    tt.setName(seatTraitName);
                                    tt.setId(String.valueOf(ticketTrait.getId()));
                                }

                                break;
                            }
                        }
                    }
                }
            } else {    // issue a query to get all trait IDS from comments
                processedIds = listingSeatTraitMgr.parseComments(
                        currentListing.getEventId(), allComments);
            }
        }

        // IGNORE Piggyback because it is handled in ticket operation section
        if (processedIds.contains(PIGGYBACK_SEATING)) {
            processedIds.remove(PIGGYBACK_SEATING);
        }
        
        //remove parking pass ticket traits for Parking pass only Event
        if (processedIds.contains(PARKING_PASS) &&
                currentListing.getEvent().isParkingOnlyEvent() != null && Boolean.TRUE.equals(currentListing.getEvent().isParkingOnlyEvent())){
            logger.info("_message=\"removing ParkingPass traits for\" eventId={}", currentListing.getEvent().getId());
            processedIds.remove(PARKING_PASS);            
        }
        
        boolean isFeature = hasFeature(currentListing, processedIds);
        
        // note: INVALID_SPLIT_TICKETTRAIT_COMBINATION should not be thrown
        // Set split option to NO_SPLITS and set the split quantity as the same as the quantity requested
        // When there is a Parking Pass, Aisle or Piggyback in the request or identified
        if (processedIds.contains(AISLE) || processedIds.contains(FULL_SUITE) || processedIds.contains(PARKING_PASS) || currentListing.getIsPiggyBack() || isFeature) {
                currentListing.setSplitOption((short) 0);
                currentListing.setSplitQuantity(currentListing.getQuantity());
        }

        if (processedIds.contains(PARKING_PASS)) {
            if (!_isParkingPassSupported(currentListing)) {
                ListingError listingError = new ListingError(ErrorType.INPUTERROR,
                        ErrorCode.INVALID_TICKET_TRAIT, "Parking Pass is not supported", "ticketTraits");
                throw new ListingBusinessException(listingError);
            }
        }
        if (currentListing.getTicketMedium() != null && currentListing.getTicketMedium() != TicketMedium.PAPER.getValue()) {
            if (processedIds.contains(TRADITIONAL_HARD_TICKET)) {
                processedIds.remove(TRADITIONAL_HARD_TICKET);
            }
        }

        logger.debug("validateAllSeatTraits output processedIds={}", processedIds);

        return processedIds;
    }

    private List<Long> validateSeatTraitIds(Listing currentListing, List<Long> seatTraitIds, boolean splitOptionPassed) {
        List<Long> processedIds = seatTraitIds;

        // IGNORE Piggyback because it is handled in ticket operation section
        if (processedIds.contains(PIGGYBACK_SEATING)) {
            processedIds.remove(PIGGYBACK_SEATING);
        }
        
        //remove parking pass ticket traits for Parking pass only Event
        if (processedIds.contains(PARKING_PASS) &&
                currentListing.getEvent().isParkingOnlyEvent() != null && Boolean.TRUE.equals(currentListing.getEvent().isParkingOnlyEvent())){
            logger.info("_message=\"removing ParkingPass traits for\" eventId={}", currentListing.getEvent().getId());
            processedIds.remove(PARKING_PASS);            
        }
        
        boolean isFeature = hasFeature(currentListing, processedIds);

        // Set split option to NO_SPLITS and set the split quantity as the same as the quantity requested
        // When there is a Parking Pass, Aisle or Piggyback in the request or identified
        if (processedIds.contains(AISLE) || processedIds.contains(PARKING_PASS) || processedIds.contains(FULL_SUITE) || currentListing.getIsPiggyBack() || isFeature) {
                currentListing.setSplitOption((short) 0);
                currentListing.setSplitQuantity(currentListing.getQuantity());
        }

        if (processedIds.contains(PARKING_PASS)) {
            if (!_isParkingPassSupported(currentListing)) {
                ListingError listingError = new ListingError(ErrorType.INPUTERROR,
                        ErrorCode.INVALID_TICKET_TRAIT, "Parking Pass is not supported", "ticketTraits");
                throw new ListingBusinessException(listingError);
            }
        }
        if (currentListing.getTicketMedium() != null && currentListing.getTicketMedium() != TicketMedium.PAPER.getValue()) {
            if (processedIds.contains(TRADITIONAL_HARD_TICKET)) {
                processedIds.remove(TRADITIONAL_HARD_TICKET);
            }
        }

        logger.debug("validateSeatTraitIds output processedIds={}", processedIds);

        return processedIds;
    }

    private TicketTrait _findTicketTrait(String comment, Long id, List<TicketTrait> passedTraits) {
        String strId = String.valueOf(id);
        for (TicketTrait tt : passedTraits) {
            if (tt.getId() != null && strId.equals(tt.getId())) {
                return tt;
            } else if (tt.getName() != null && comment.equalsIgnoreCase(tt.getName())) {
                return tt;
            }
        }
        return null;
    }

    private TicketTrait _findTicketTraitById(Long id, List<TicketTrait> passedTraits) {
        String strId = String.valueOf(id);
        for (TicketTrait tt : passedTraits) {
            if (tt.getId() != null && strId.equals(tt.getId()))
                return tt;
        }
        return null;
    }
    
    private boolean hasFeature(Listing listing, List<Long> seatTraitIds) {
        boolean isFeature = false;
        if(listing.getEvent() != null && listing.getEvent().getTicketTrait() != null) {
            for(com.stubhub.domain.catalog.events.intf.TicketTrait trait : listing.getEvent().getTicketTrait()) {
                if(seatTraitIds.contains(trait.getId()) && TICKET_FEATURE.equalsIgnoreCase(trait.getType())) {
                    isFeature = true;
                    break;
                }
            }
        }
        return isFeature;
    }


    /**
     * Update seat traits in current listing as inferred by passed parking pass and piggyback logic
     *
     * @param listing
     * @param barcodeSeats
     * @return new ListingSeatTrait list if any new traits added, otherwise null
     */
    public void updateTraitsInferredByPassedListing(SeatProductsContext seatProdContext) {
        boolean parkingPassTraitExits = false;
        boolean piggybackTraitEsists = false;
        Listing currentListing = seatProdContext.getCurrentListing();

        List<ListingSeatTrait> listingTraits = currentListing.getSeatTraits();

        if (listingTraits == null) {
            if (seatProdContext.isCreate()) {
                listingTraits = new ArrayList<ListingSeatTrait>();
            } else {
                listingTraits = listingSeatTraitMgr.findSeatTraits(currentListing.getId());
            }
        }

        boolean isCurrentPiggyBack = currentListing.getIsPiggyBack();
        boolean isCurrentParkingPass = false;
        if (currentListing.getTicketSeats() != null && !currentListing.getTicketSeats().isEmpty()) {
            for (TicketSeat ticketSeat : currentListing.getTicketSeats()) {
                if (ticketSeat.getTixListTypeId().equals(2L) && !ticketSeat.getSeatStatusId().equals(4L)) {
                    isCurrentParkingPass = true;
                    break;
                }
            }
        }
        boolean traitsChanged = false;

        for (Iterator<ListingSeatTrait> it = listingTraits.iterator(); it.hasNext(); ) {

            ListingSeatTrait trait = it.next();
            if (trait.getSupplementSeatTraitId().longValue() == PARKING_PASS) { // PARKING_PASS)
                parkingPassTraitExits = true;
                if (!isCurrentParkingPass && currentListing.getTicketSeats() != null && !currentListing.getTicketSeats().isEmpty()) {
                    trait.setMarkForDelete(true);
                    traitsChanged = true;
                }
            } else if (trait.getSupplementSeatTraitId().longValue() == PIGGYBACK_SEATING) {
                piggybackTraitEsists = true;
                // no longer PG seats remove trait
                if (!isCurrentPiggyBack) {
                    trait.setMarkForDelete(true);
                    traitsChanged = true;
                }
            }
        }

        // if parking pass passed and not found in DB or listing
        if (seatProdContext.isNewlyAddedParkingPass()) {
            if (!parkingPassTraitExits) {
                long id = (currentListing.getId() == null) ? 0l : currentListing.getId();
                ListingSeatTrait seatTrait = TicketSeatUtils.makeListingSeatTrait(id, PARKING_PASS,
                        CommonConstants.LISTING_API_V2, CommonConstants.LISTING_API_V2);
                listingTraits.add(seatTrait);
                traitsChanged = true;
            }

            // figure out manually how many tickets available and how many parking pass
            // note: qty can be == 1 and no tickets available, or one ticket and parking pass can also have qty == 1
            int qtyCount = 0;
            boolean parkingPass = false;
            for (int i = 0; i < seatProdContext.getTicketSeatsFromCache().size(); i++) {
                TicketSeat tic = seatProdContext.getTicketSeatsFromCache().get(i);
                if (tic.getSeatStatusId().intValue() == 1) {    // if not deleted
                    qtyCount++;
                }
                if (tic.getTixListTypeId().intValue() == 2) {
                    parkingPass = true;
                }
            }

            // parking pass ONLY listing (no other tickets)
            if (qtyCount == 1 && parkingPass) {
                currentListing.setListingType(2L);
                currentListing.setQuantity(1);
                currentListing.setQuantityRemain(1);
                currentListing.setSplitQuantity(1);
            } else {
                currentListing.setListingType(3L);    // has parking pass (quantity is set earlier)
            }
        }

        // Check for piggyback
        if (!piggybackTraitEsists && isCurrentPiggyBack) {
            long id = (currentListing.getId() == null) ? 0l : currentListing.getId();
            ListingSeatTrait seatTrait = TicketSeatUtils.makeListingSeatTrait(id, PIGGYBACK_SEATING,
                    CommonConstants.LISTING_API_V2, CommonConstants.LISTING_API_V2);
            listingTraits.add(seatTrait);
            traitsChanged = true;
        }

        // added new traits set in currentListing object
        if (traitsChanged) {
            logger.debug("traitsChanged, listingTraits.size={}", listingTraits.size());
            currentListing.setSeatTraits(listingTraits);
            currentListing.setLastModifiedDate(DateUtil.getNowCalUTC());
        }
    }

    /**
     * This method will check if parking pass is supported or not if event is integrated
     *
     * @param listing
     * @return
     */
    private boolean _isParkingPassSupported(Listing listing) {
        // SELLFLOW-67
        // Parking pass is allowed for STH irrespective of Event config.
        if (listing != null
                && listing.getListingSource() != null
                && listing.getListingSource().intValue() == LISTING_SOURCE_ID_STH) {
            return true;
        }
		if (listing != null) {
			Event event = listing.getEvent();
			if (event != null && event.getIsIntegrated()) {
				return listingSeatTraitMgr.isParkingSupportedForEvent(event.getId());
			}
		}
        return true;
    }

    public VenueConfigSectionOrZone getVenueConfigSectionOrZoneId(Long venueConfigId, String sectionDesc, String rowDesc,
                                                      String piggyBackRowDesc, String eventCountry, Boolean enableHybridMapAtEvent) throws UnsupportedEncodingException {
        VenueConfigSectionOrZone venueConfigSectionOrZone = new VenueConfigSectionOrZone();
        Long venueConfigSectionsId = 0l;
        Long venueConfigZoneId = 0l;
        JSONObject jsonObjectList = null;
        JSONArray venueConfigurationsArray = null;
        String id = null;
        boolean isGA = false;
        JSONObject jSONObject = null;
        
        Boolean globalRegSectionZoneToggle = globalRegServiceHelper.getSectionZoneToggleByCountry(eventCountry);
        
        String getVenueConfigMetadataAPIUrl = getProperty(GET_VENUE_CONFIG_METADATA_API,
                "http://api-int.stubprod.com/catalog-read/v3/venues/venueConfigMetadata?rows={rowDesc},{piggyBackRowDesc}&venueConfigId={venueConfigId}&sectionOrZoneName={sectionName}&isSectionStemmingRequired=true&isSectionNameExactMatch=true&source=sell");
        getVenueConfigMetadataAPIUrl = getVenueConfigMetadataAPIUrl.replace("{venueConfigId}",
                venueConfigId.toString());
        getVenueConfigMetadataAPIUrl = getVenueConfigMetadataAPIUrl.replace("{sectionOrZoneName}", URLEncoder.encode(sectionDesc, "UTF-8").replace("+", "%20"));
        if (rowDesc != null && piggyBackRowDesc != null) {
            getVenueConfigMetadataAPIUrl = getVenueConfigMetadataAPIUrl.replace("{rowDesc}", URLEncoder.encode(rowDesc, "UTF-8").replace("+", "%20"));
            getVenueConfigMetadataAPIUrl = getVenueConfigMetadataAPIUrl.replace("{piggyBackRowDesc}", URLEncoder.encode(piggyBackRowDesc, "UTF-8").replace("+", "%20"));
        } else {
            if (rowDesc != null) {
                getVenueConfigMetadataAPIUrl = getVenueConfigMetadataAPIUrl.replace("{rowDesc},{piggyBackRowDesc}", URLEncoder.encode(rowDesc, "UTF-8").replace("+", "%20"));
            }
        }
        logger.info("_message=\"get venue config metadata API\" venueConfigMetadataAPIUrl={}", getVenueConfigMetadataAPIUrl);
        try {
            objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            WebClient webClient = svcLocator.locate(getVenueConfigMetadataAPIUrl);
            webClient.accept(MediaType.APPLICATION_JSON);

            SHMonitor mon = SHMonitorFactory.getMonitor();
            Response response = null;
            try {
                mon.start();
                response = webClient.get();
            } finally {
                mon.stop();
                logger.info(SHMonitoringContext.get() + " _operation=getVenueConfigSectionId" + " _message= service call for venueConfigId=" + venueConfigId + "  _respTime=" + mon.getTime());
            }

            if (Response.Status.OK.getStatusCode() == response.getStatus()) {
                logger.info("_message=\"venue config metadata api call successful\" venueConfigId={}", venueConfigId);
                InputStream is = (InputStream) response.getEntity();
                if (is != null) {
                    jsonObjectList = JSONObject.fromObject(IOUtils.toString(is));
                }
                if (jsonObjectList != null) {
                    venueConfigurationsArray = jsonObjectList.getJSONArray("venueConfigurations");
                    logger.info("_message=\"venue config metadata api response\" jsonResponse={}", venueConfigurationsArray.toString());
                    JSONObject jasonObjectForseatingZones = (JSONObject) venueConfigurationsArray.get(0);
                    JSONArray jsonArrayForSeatingZones = jasonObjectForseatingZones.getJSONArray("seatingZones");
                    //check if the seatingZones Array is empty
                    if(!jsonArrayForSeatingZones.isEmpty()){
	                    JSONObject jasonObjectForSeatingSections = (JSONObject) jsonArrayForSeatingZones.get(0);
	                    //check if the jsonObject contains key seatingSections, in case of section listing
	                    if(jasonObjectForSeatingSections.containsKey("seatingSections")){
	                    	JSONArray jsonArrayForSeatingSections = jasonObjectForSeatingSections.getJSONArray("seatingSections");
		                    if (jsonArrayForSeatingSections.size() > 0) {
		                        jSONObject = (JSONObject) jsonArrayForSeatingSections.get(0);
			                    if (jSONObject != null) {
			                        id = jSONObject.getString("id");
			                        venueConfigSectionsId = Long.parseLong(id);
			                        if (jSONObject.has("generalAdmission")) {
			                            isGA = jSONObject.getBoolean("generalAdmission");
			                        }
			                    }
		                    }
	                    }else{
	                    	//else its zone listing , look for zone id and name
	                        jSONObject = (JSONObject) jsonArrayForSeatingZones.get(0);
	                        if(jSONObject != null){
		                        id = jSONObject.getString("id");
		                        venueConfigZoneId = Long.parseLong(id);
		                        logger.info("_message=\"venue config metadata api, value of\" venueConfigId={} venueConfigZoneId={}", venueConfigId, venueConfigZoneId);
	                        }
		                 }
	                    	
                    }
                }
            } else {
                logger.error("_message=\"get venue config metadata api call failed\" responseCode={}", response.getStatus());
            }
        } catch (Exception e) {
            logger.error("_message=\"Exception occured while call to venue configMetadata API and the " + " Exception=", e.getMessage(), e);
        }
        venueConfigSectionOrZone.setVenueConfigSectionId(venueConfigSectionsId);
        venueConfigSectionOrZone.setGeneralAdmission(isGA);
        
        if((enableHybridMapAtEvent != null && enableHybridMapAtEvent) || (enableHybridMapAtEvent == null && !globalRegSectionZoneToggle)){
            logger.info("_message=\"Persisting the ZoneId\" venueConfigId={} venueConfigZoneId={}", venueConfigId, venueConfigZoneId);
        	venueConfigSectionOrZone.setVenueConfigZoneId(venueConfigZoneId);
        }
        
        return venueConfigSectionOrZone;
    }

    protected String getProperty(String propertyName, String defaultValue) {
        return MasterStubHubProperties.getProperty(propertyName, defaultValue);
    }
    
    private void setToPendingLock(Listing currentListing) {
        if ((TicketMedium.BARCODE.getValue() == currentListing.getTicketMedium() || TicketMedium.FLASHSEAT.getValue() == currentListing.getTicketMedium())
                && DeliveryOption.PREDELIVERY.getValue() == currentListing.getDeliveryOption()) {
            if (!currentListing.getSystemStatus().equals(ListingStatus.INCOMPLETE.toString())) {
                currentListing.setSystemStatus(ListingStatus.PENDING_LOCK.toString());
                /*SELLAPI-3243*/
                currentListing.setIsLockMessageRequired(true);
            }
        }
    }


}
