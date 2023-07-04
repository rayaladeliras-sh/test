package com.stubhub.domain.inventory.listings.v2.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.common.util.ListingException;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.entity.SeatProduct;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.domain.inventory.v2.enums.InventoryType;


public class SeatProductsContext {

    private final static Logger log = LoggerFactory.getLogger(SeatProductsContext.class);

    private Listing currentListing;
    private TicketSeatMgr ticketSeatMgr;
    private ListingSeatTraitMgr listingSeatTraitMgr;

    private ArrayList<TicketSeat> newlyAddedTicketSeatList;
    private boolean newlyAddedParkingPasss = false;
    private boolean validateBarcode = false;

    public boolean isValidateBarcode() {
		return validateBarcode;
	}

	public void setValidateBarcode(boolean validateBarcode) {
		this.validateBarcode = validateBarcode;
	}

	// cache
    private List<TicketSeat> cachedTicketSeats = null;

    // artifact id duplicate check
    private HashMap<String, String> duplicateCheckArtifactId;

    // seat products list (this is passed rto verify barcode call)
    private ArrayList<SeatProduct> barcodeSeatProductList;
    
    private Set<String> flashFulfillmentArtifactIds;

    // request from client
    private ListingRequest updateListReq;

    private boolean hasDelOperation = false;
    private boolean hasUpdOperation = false;
    private boolean hasAddOperation = false;

    // flag to indicate that DB tickets has changed
    private boolean isDBTicketsChanged = false;

    private boolean isCreate = false;
    private boolean immediateSave = true;

    // parking pass in current listing
    private TicketSeat currentParkingPass = null;

    public SeatProductsContext(Listing currentListing, ListingRequest updateListReq,
                               TicketSeatMgr ticketSeatMgr, ListingSeatTraitMgr listingSeatTraitMgr) {
        this(currentListing, updateListReq, ticketSeatMgr, listingSeatTraitMgr, true);
    }

    public SeatProductsContext(Listing currentListing, ListingRequest updateListReq,
                               TicketSeatMgr ticketSeatMgr, ListingSeatTraitMgr listingSeatTraitMgr, boolean immediateSave) {
        this.currentListing = currentListing;
        this.ticketSeatMgr = ticketSeatMgr;
        this.listingSeatTraitMgr = listingSeatTraitMgr;
        this.updateListReq = updateListReq;
        this.immediateSave = immediateSave;

        isCreate = currentListing.getId() == null;
    }

    public boolean isCreate() {
        return isCreate;
    }

    public boolean isPassedTraits() {
        return updateListReq.getTicketTraits() != null && updateListReq.getTicketTraits().size() > 0;
    }

    public boolean isSTHListing() {
        return currentListing.getListingSource() != null && currentListing.getListingSource().equals(8);
    }

    public ListingRequest getListingRequest() {
        return updateListReq;
    }

    public boolean getHasDelOperation() {
        return hasDelOperation;
    }

    public boolean getHasUpdOperation() {
        return hasUpdOperation;
    }

    public boolean getHasAddOperation() {
        return hasAddOperation;
    }

    public void setIsDBTicketsChanged() {
        isDBTicketsChanged = true;
    }

    public boolean getIsDBTicketsChanged() {
        return isDBTicketsChanged;
    }

    public boolean isPiggyBackRows() {
        return currentListing.getIsPiggyBack();
    }

    public boolean isNewlyAddedParkingPass() {
        return newlyAddedParkingPasss;
    }

    public TicketSeat getCurrentParkingPass() {
        if (currentParkingPass == null) {
            List<TicketSeat> ticketsFromDb = getTicketSeatsFromCache();

            TicketSeat ts = null;
            // find if there is one already existing
            for (TicketSeat ticket : ticketsFromDb) {
                if (ticket.getSeatStatusId().intValue() == 1 && ticket.getTixListTypeId().intValue() == 2) {// parking
                    // pass

                    currentParkingPass = ticket;
                    break;
                }
            }
        }
        return currentParkingPass;
    }

    /**
     * Check if we need to change listingStatus based on delivery and medium
     */
    public void checkSetListingStatus() {
        if (ListingStatus.ACTIVE.toString().equalsIgnoreCase(currentListing.getSystemStatus())) {
            if (currentListing.getDeliveryOption().equals(DeliveryOption.PREDELIVERY.getValue())
                    && currentListing.getTicketMedium().intValue() == TicketMedium.BARCODE.getValue()) {

                currentListing.setSystemStatus(ListingStatus.PENDING_LOCK.toString());
            }
        }
        // Note we may need to send this message in some cases to set Admin End Date forlisting
        // jmsMessageHelper.sendCreateLMSListingMessage(listing);
    }

    /**
     * Handles the locking of barcodes for active barcode predelivery listing
     */
    public void handleBarcodeListingLocks(JMSMessageHelper jmsMessageHelper) {

        // if active status
        if (ListingStatus.PENDING_LOCK.toString().equalsIgnoreCase(currentListing.getSystemStatus())) {
            jmsMessageHelper.sendLockInventoryMessage(currentListing.getId());
        }
    }

    /**
     * Updates quantity, quantityRemain and split quantity in currentListing in memory
     *
     * @param deltaValue positive or negative difference value
     */
    public void updateQuantityInCurrentListing(int deltaValue) {
        currentListing.setQuantity(currentListing.getQuantity() + deltaValue);
        currentListing.setQuantityRemain(currentListing.getQuantityRemain() + deltaValue);

        if (currentListing.getSplitOption() != null && currentListing.getSplitOption() == 0) {
            // its a no-split ticket, so update the split quantity to
            // same as the quantity
            currentListing.setSplitQuantity(currentListing.getQuantityRemain());
        }
    }

    /**
     * This method sets all manipulated seats back in current listing ready to be stored in db NOTE:
     * No seat manipulation should happen after this
     */
    public void setTicketSeatsBackInCurrentListing() {
        if (cachedTicketSeats != null) {
            currentListing.setTicketSeats(cachedTicketSeats);

            if (currentListing.getListingType() == null || currentListing.getListingType() == 0L) {
                // one mean no parking pass set
                currentListing.setListingType(1L);// Changing the tix_list_type_id from 3 to 1
            }
        }
    }

    /**
     * Move some params passed in newlisting to current listing
     *
     * @param newListing
     */
    public void setMiscValuesInCurrentListing(Listing newListing) {
        if (newListing.getFaceValue() != null && newListing.getFaceValue().getAmount() != null) {
            if(newListing.getFaceValue().getAmount().doubleValue() > 0) {
                this.currentListing.setFaceValue(newListing.getFaceValue());
            } else {
                this.currentListing.setFaceValue(null);
            }
        }
        if (newListing.getTicketCost() != null) {
            this.currentListing.setTicketCost(newListing.getTicketCost());
        }
    }

    /**
     * validateAllSeatsAndRows. check row and seat consistency (has to be either all empty or all have
     * value). Also check rows for piggyback
     */
    public void validateAllSeatsAndRows() throws ListingException {
        List<TicketSeat> seats = getTicketSeatsFromCache();
        HashMap<String, Integer> rowsMap = new HashMap<String, Integer>();
        ArrayList<String> rowList = new ArrayList<String>();
        ArrayList<String> seatList = new ArrayList<String>();
        int rowEmpty = -1;
        int seatEmpty = -1;
        boolean rowError = false;
        boolean seatError = false;

        for (TicketSeat ts : seats) {

            if (ts.getSeatStatusId().intValue() != 1 || ts.getTixListTypeId().intValue() != 1)
                continue;

            // check row consistency (has to be either all empty or all have value) and piggyback
            String row = ts.getRow();
            if (StringUtils.trimToNull(row) != null) {
                Integer count = rowsMap.get(row);
                if (count == null) {
                    rowsMap.put(row, new Integer(1));
                    rowList.add(row);
                } else {
                    rowsMap.put(row, new Integer(count + 1));
                }

                // check if row was empty and become not
                if (rowEmpty < 0)
                    rowEmpty = 0;
                else if (rowEmpty == 1) {
                    rowError = true;
                    break;
                }
            } else {
                if (rowEmpty < 0)
                    rowEmpty = 1;
                else if (rowEmpty == 0) {
                    rowError = true;
                    break;
                }
            }

            // check seat consistency (has to be either all empty or all have value)
            String seat = ts.getSeatNumber();
            if (StringUtils.trimToNull(seat) != null) {
                seatList.add(seat);
                if (seatEmpty < 0)
                    seatEmpty = 0;
                else if (seatEmpty == 1) {
                    seatError = true;
                    break;
                }
            } else {
                if (seatEmpty < 0)
                    seatEmpty = 1;
                else if (seatEmpty == 0) {
                    seatError = true;
                    break;
                }
            }
        }
        // check for errors
        if (rowError) {
            ListingError listingError =
                    new ListingError(ErrorType.INPUTERROR, ErrorCode.INCORRECT_QUANTITY_OF_SEATS,
                            "Update operatios seats resulted in inconsistent rows with some having values and others empty",
                            "row");
            throw new ListingBusinessException(listingError);
        } else if (seatError) {
            ListingError listingError =
                    new ListingError(ErrorType.INPUTERROR, ErrorCode.INCORRECT_QUANTITY_OF_SEATS,
                            "Update operatios seats resulted in inconsistent seats with some having values and others empty",
                            "row");
            throw new ListingBusinessException(listingError);
        }

        // Figure out the rows (and check for piggyBack)
        if (rowsMap.size() > 0) {
            int lastVal = -1;
            for (Iterator<Integer> it = rowsMap.values().iterator(); it.hasNext(); ) {
                Integer val = it.next();
                if (lastVal < 0) {
                    lastVal = val;
                } else if (lastVal != val) {
                    ListingError listingError =
                            new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_PIGGYBACK_ROW,
                                    "Unbalanced piggyback rows seats. Numbers (" + lastVal + ", " + val + ")", "row");
                    throw new ListingBusinessException(listingError);
                }
            }

            if (rowList.get(0).equals(CommonConstants.GA_ROW_DESC)) {
                currentListing.setRow(CommonConstants.GA_ROW_DESC);
                currentListing.setSeats(CommonConstants.GENERAL_ADMISSION);
            } else {
                currentListing.setRow(TicketSeatUtils.makeCSVStringFromList(rowList));
                if (seatList.isEmpty()) {
                    currentListing.setSeats(CommonConstants.TO_BE_DEFINED);
                } else {
                    currentListing.setSeats(TicketSeatUtils.makeCSVStringFromList(seatList));
                }
            }

            // piggyback validations
            if (currentListing.getIsPiggyBack() && currentListing.getQuantityRemain() < 2) {
                ListingError listingError =
                        new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_PIGGYBACK_ROW,
                                "Invalid piggyback number of seats. Minimum of 2 is required.", "products");
                throw new ListingBusinessException(listingError);
            }
        }
    }

    /**
     * Convert DTO SeatProduct ready to process
     *
     * @param onlyBarcode
     * @return List<BarcodeSeat>
     */
    public List<SeatProduct> getPassedSeatProductList(boolean onlyBarcode) {
        ArrayList<SeatProduct> seatList = null;

        if (updateListReq.getProducts() != null) {
            seatList = new ArrayList<SeatProduct>();

            for (com.stubhub.domain.inventory.v2.DTO.Product prod : updateListReq.getProducts()) {
                SeatProduct barSeat = new SeatProduct();

                barSeat.setRow(prod.getRow());
                barSeat.setSeat(prod.getSeat());
                barSeat.setOperation(prod.getOperation());
                barSeat.setProductType(prod.getProductType());
                barSeat.setExternalId(prod.getExternalId());
                barSeat.setUniqueTicketNumber(prod.getUniqueTicketNumber());
                barSeat.setFaceValue(prod.getFaceValue());
                barSeat.setFulfillmentArtifact(prod.getFulfillmentArtifact());
                
                if (prod.getInventoryType() != null) {
                    if (prod.getInventoryType() == InventoryType.CONSIGNMENT) {
                        barSeat.setInventoryTypeId(com.stubhub.domain.inventory.datamodel.entity.enums.InventoryType.CONSIGNMENT.getId());
                    } else if (prod.getInventoryType() == InventoryType.OWNED) {
                        barSeat.setInventoryTypeId(com.stubhub.domain.inventory.datamodel.entity.enums.InventoryType.OWNED.getId());
                    } else if (prod.getInventoryType() == InventoryType.SELLITNOW) {
                        barSeat.setInventoryTypeId(com.stubhub.domain.inventory.datamodel.entity.enums.InventoryType.SELLITNOW.getId());
                    }
                    
                }
                
                seatList.add(barSeat);

                if (Operation.ADD.equalsEnum(prod.getOperation())) {
                    hasAddOperation = true;
                } else if (Operation.DELETE.equalsEnum(prod.getOperation())) {
                    hasDelOperation = true;
                } else if (Operation.UPDATE.equalsEnum(prod.getOperation())) {
                    hasUpdOperation = true;
                }
            }
        }
        return seatList;
    }

    /**
     * getTicketSeatMgr
     *
     * @return
     */
    public TicketSeatMgr getTicketSeatMgr() {
        return ticketSeatMgr;
    }

    /**
     * getListingSeatTraitMgr
     *
     * @return
     */
    public ListingSeatTraitMgr getListingSeatTraitMgr() {
        return listingSeatTraitMgr;
    }

    /**
     * getTicketSeatsFromCache
     *
     * @return
     */
    public List<TicketSeat> getTicketSeatsFromCache() {
        if (cachedTicketSeats == null) {
            if (isCreate) { // no existing seats
                cachedTicketSeats = new ArrayList<TicketSeat>();
            } else {
                cachedTicketSeats = ticketSeatMgr.findTicketSeatsByTicketId(currentListing.getId());
            }
        }
        return cachedTicketSeats;
    }

    /**
     * Adds ticket seat to seat cache and then it will all be stored in the DB in batch
     *
     * @param ticket
     */
    public void addTicketSeat(TicketSeat ticket) {
        // old behaviors to save immediately to DB
        if (immediateSave) {
            ticketSeatMgr.addTicketSeat(ticket);
            cachedTicketSeats.add(ticket);
        } else { // new behavior is to batch in memory and then save to db
            cachedTicketSeats.add(ticket);
        }
        setIsDBTicketsChanged();
    }

    /**
     * Delete ticket seat and if parking pass set the correct indicator in currentListing
     *
     * @param ticket
     */
    public void deleteTicketSeat(TicketSeat ticket) {
        ticket.setSeatStatusId(4L);

        if (ticket.getTixListTypeId().intValue() == 2) {
            currentListing.setListingType(1L);
        }

        // set db tickets changes indicator
        setIsDBTicketsChanged();
    }

    /**
     * @return CurrentListing from (as read and cached from db)
     */
    public Listing getCurrentListing() {
        return currentListing;
    }

    /**
     * getBarcodeSeatProductList will return barcodes only that resulted from product manipulation
     * (add, update). This only pretains to Barcode listing
     *
     * @return getBarcodeSeatProductList
     */
    public ArrayList<SeatProduct> getBarcodeSeatProductList() {
        return barcodeSeatProductList;
    }

    /**
     * Gets the list updates with all seatId (after saving to the DB)
     */
    public void resolveBarcodeSeatProductsList() {
        if (barcodeSeatProductList != null) {
            List<TicketSeat> cachedSeats = getTicketSeatsFromCache();

            for (SeatProduct prodSeat : barcodeSeatProductList) {

                if (prodSeat.getProductType().equals(ProductType.TICKET)) {
                    TicketSeat seat = SeatProductsManipulator.findTicketSeatEqSeatProduct(prodSeat, cachedSeats);
                    if (seat != null) {
                        prodSeat.setSeatId(seat.getTicketSeatId());
                        if(StringUtils.trimToNull(prodSeat.getRow()) == null) {
                            prodSeat.setRow(seat.getRow());
                        }
                        if(StringUtils.trimToNull(prodSeat.getSeat()) == null) {
                            prodSeat.setSeat(seat.getSeatNumber());
                        }
                        if(prodSeat.getFaceValue() == null){
                        	prodSeat.setFaceValue(seat.getFaceValue());
                        }
                    }
                } else { // parking pass
                    // get current pass from DB
                    TicketSeat parkingTicket = getCurrentParkingPass();
                    if (parkingTicket != null) {
                        prodSeat.setSeatId(parkingTicket.getTicketSeatId());
                        if(StringUtils.trimToNull(prodSeat.getRow()) == null) {
                            prodSeat.setRow(parkingTicket.getRow());
                        }
                        if(StringUtils.trimToNull(prodSeat.getSeat()) == null) {
                            prodSeat.setSeat(parkingTicket.getSeatNumber());
                        }
                        if(prodSeat.getFaceValue() == null){
                        	prodSeat.setFaceValue(parkingTicket.getFaceValue());
                        }
                    }
                }
            }
        }
    }

    /**
     * Add SeatProduct to list (if no duplicates found). the main purpose of this list is to pass on
     * to validate artifact asso. with seat to be updated. If seat does not have artifact it is
     * ignored and return true
     *
     * @param prod
     * @return false not added for duplicate artifact found, else true
     */
    public boolean addArtifactSeatProductToList(SeatProduct prod) {
        if (StringUtils.trimToNull(prod.getFulfillmentArtifact()) == null) {
            return true;
        }

        if (barcodeSeatProductList == null)
            barcodeSeatProductList = new ArrayList<SeatProduct>();

        if (!_isArtifactIdDuplicate(prod.getFulfillmentArtifact())) {
            barcodeSeatProductList.add(prod);
            return true;
        }
        return false;
    }

    /**
     * getNewlyAddedTiketSeatList
     *
     * @return
     */
    public ArrayList<TicketSeat> getNewlyAddedTiketSeatList() {
        return newlyAddedTicketSeatList;
    }

    /**
     * Call this method to backout all added ticket seats
     */
    public void rollbackAllNewlyAddedTicketSeats() {
        resolveBarcodeSeatProductsList();

        if (getNewlyAddedTiketSeatList() != null) {
            for (TicketSeat seat : getNewlyAddedTiketSeatList()) {
                try {
                    seat.setSeatStatusId(CommonConstants.SEAT_STATUS_DELETED);
                    getTicketSeatMgr().updateTicketSeat(seat);
                } catch (Exception ex) {
                    log.error("Error encountered while rolling back (soft delete) added TicketSeat from DB",
                            ex);
                }
            }
            newlyAddedTicketSeatList = null;
        }
    }

    /**
     * Call this method to backout created listing
     */
    public void rollbackCurrentListing(InventoryMgr inventoryMgr) {
        currentListing.setSystemStatus(ListingStatus.DELETED.toString());
        try {
            // SEA: I had to do this otherwise U get a transaction exception
            Listing newListing = inventoryMgr.getListing(currentListing.getId());
            newListing.setSystemStatus(ListingStatus.DELETED.toString());
            newListing.setStatus((short) 1); // set status to 1 so that Unlock job does not pick up this listing
            inventoryMgr.updateListing(newListing);
        } catch (Exception ex) {
            log.error("Error encountered while rolling back listing record (set systemStatus = DELETE)",
                    ex);
        }
    }

    /**
     * addToNewlyAddedTicketSeatList
     *
     * @param ticket
     */
    public synchronized void addToNewlyAddedTicketSeatList(TicketSeat ticket) {
        if (newlyAddedTicketSeatList == null)
            newlyAddedTicketSeatList = new ArrayList<TicketSeat>();

        newlyAddedTicketSeatList.add(ticket);

        // update listing with parking pass status and update flag
        if (ticket.getTixListTypeId().intValue() == 2) {
            currentListing.setListingType(3L);
            newlyAddedParkingPasss = true;
        }
    }
    
    public Set<String> getFlashFulfillmentArtifactIds() {
        return flashFulfillmentArtifactIds;
    }

    public void addToFlashFulfillmentArtifactIds(String flashFulfillmentArtifactId) {
        if (flashFulfillmentArtifactIds == null) {
            flashFulfillmentArtifactIds = new HashSet<String>();
        }
        
        flashFulfillmentArtifactIds.add(flashFulfillmentArtifactId);
    }

    /**
     * _isArtifactIdDuplicate
     *
     * @param artifactId
     * @return
     */
    private boolean _isArtifactIdDuplicate(String artifactId) {
        boolean dup = false;

        if (duplicateCheckArtifactId == null)
            duplicateCheckArtifactId = new HashMap<String, String>(10);

        if (duplicateCheckArtifactId.get(artifactId) != null)
            dup = true;
        else
            duplicateCheckArtifactId.put(artifactId, "");

        return dup;
    }
    
}

