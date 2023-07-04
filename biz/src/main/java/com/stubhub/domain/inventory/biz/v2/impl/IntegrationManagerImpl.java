package com.stubhub.domain.inventory.biz.v2.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.collections.CollectionUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.biz.v2.intf.IntegrationManager;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.util.PartnerIntegrationConstants;
import com.stubhub.domain.inventory.common.util.StringUtils;
import com.stubhub.domain.inventory.datamodel.dao.ListingDAO;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingType;
import com.stubhub.domain.inventory.v2.enums.ListingStatusEnum;
import com.stubhub.domain.inventory.v2.enums.TicketMedium;
import com.stubhub.domain.inventory.v2.enums.TicketSeatStatusEnum;
import com.stubhub.domain.partnerintegration.datamodel.enums.TicketStatusEnum;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.LockInventoryRequest;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.LockInventoryResponse;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.PartnerListing;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.PartnerProduct;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.ProductType;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.UnlockInventoryRequest;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.UnlockInventoryResponse;
import com.stubhub.domain.user.contactsV2.intf.CustomerContactV2Details;
import com.stubhub.newplatform.common.util.DateUtil;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

@Component("integrationManager")
public class IntegrationManagerImpl implements IntegrationManager {

  private static final Logger logger = LoggerFactory.getLogger(IntegrationManagerImpl.class);

  @Autowired
  private ListingDAO listingDAO;

  @Autowired
  private InventoryMgr inventoryMgr;

  @Autowired
  private TicketSeatMgr ticketSeatMgr;

  @Autowired
  private MasterStubhubPropertiesWrapper masterStubhubPropertiesWrapper;

  @Autowired
  private SvcLocator svcLocator;

  /**
   * {@inheritDoc}
   */
  @Override
  public Listing getListing(final Long listingId) {
    return getListing(listingId, null);
  }

  public Listing getListing(final Long listingId, List<Long> specifiedTicketSeatIds) {
    Listing listing = inventoryMgr.getListing(listingId);
    if (listing != null) {
      List<TicketSeat> ticketSeats = ticketSeatMgr.findAllTicketSeatsByTicketId(listingId);
      if (specifiedTicketSeatIds != null) {
        List<TicketSeat> filteredTicketSeats = new ArrayList<>();

        for (TicketSeat ticketSeat : ticketSeats) {
          if (specifiedTicketSeatIds.contains(ticketSeat.getTicketSeatId())) {
            filteredTicketSeats.add(ticketSeat);
          }
        }
        listing.setTicketSeats(filteredTicketSeats);
      } else {
        List<TicketSeat> unsoldTicketSeats = new ArrayList<>();
        for (TicketSeat ticketSeat : ticketSeats) {
          if (TicketSeatStatusEnum.AVAILABLE.getCode().intValue() == ticketSeat.getSeatStatusId()
                  .intValue()
                  || TicketSeatStatusEnum.REMOVED.getCode().intValue() == ticketSeat.getSeatStatusId()
                  .intValue()) {
            unsoldTicketSeats.add(ticketSeat);
          }
        }
        listing.setTicketSeats(unsoldTicketSeats);
      }
    }
    return listing;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LockInventoryRequest createLockInventoryRequest(final Listing listing,
      final CustomerContactV2Details sellerContact) {
    LockInventoryRequest lockInventoryRequest = new LockInventoryRequest();

    if (listing != null && CollectionUtils.isNotEmpty(listing.getTicketSeats())) {
      lockInventoryRequest.setEventId(listing.getEventId());
      lockInventoryRequest.setListing(createPartnerListing(listing, true));
      lockInventoryRequest.setSellerContact(sellerContact);
    }
    return lockInventoryRequest;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UnlockInventoryRequest createUnlockInventoryRequest(final Listing listing,
                                                             final CustomerContactV2Details sellerContact) {
    return createUnlockInventoryRequest(listing, sellerContact, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public UnlockInventoryRequest createUnlockInventoryRequest(final Listing listing,
                                                             final CustomerContactV2Details sellerContact, List<Long> unlockTicketSeatIds) {
    UnlockInventoryRequest unlockInventoryRequest = new UnlockInventoryRequest();

    if (listing != null && CollectionUtils.isNotEmpty(listing.getTicketSeats())) {
      unlockInventoryRequest.setEventId(listing.getEventId());
      unlockInventoryRequest.setListing(createPartnerListing(listing, false, unlockTicketSeatIds));
      unlockInventoryRequest.setSellerContact(sellerContact);
    }
    return unlockInventoryRequest;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public void updateListingAfterLock(final LockInventoryResponse lockInventoryResponse) {
    final Long listingId = lockInventoryResponse.getListing().getId();
    logger.info("_message=\"Entering updateListingAfterLock method...\" listingId={}", listingId);

    // Step-1: Iterate over each lock response product and populate response seatMap
    Map<Long, PartnerProduct> responseSeatMap = new HashMap<>();
    for (PartnerProduct responseProduct : lockInventoryResponse.getListing().getProducts()) {
      responseSeatMap.put(responseProduct.getSeatId(), responseProduct);
    }

    // Step-2: Fetch all available seats to ensure that we update SRS properly
    List<TicketSeat> ticketSeats = ticketSeatMgr.findActiveTicketSeatsByTicketId(listingId);

    // Step-3: Iterate over each ticketSeat and update SRS info
    for (TicketSeat ticketSeat : ticketSeats) {
      PartnerProduct product = responseSeatMap.get(ticketSeat.getTicketSeatId());
      ticketSeat.setSection(product.getSection());
      ticketSeat.setRow(product.getRow());
      ticketSeat.setSeatNumber(product.getSeat());
      ticketSeat.setGeneralAdmissionInd(product.getGa() == null ? false : product.getGa());
      ticketSeat.setLastUpdatedBy(PartnerIntegrationConstants.LISTING_API_V2);
      ticketSeat.setLastUpdatedDate(Calendar.getInstance());
      ticketSeatMgr.updateTicketSeat(ticketSeat);
    }

    // Step-4: Fetch core listing info
    Listing rootListing = inventoryMgr.getListing(listingId);

    /*
     * if its a FLASH non-instant listing and lock operation is successful, we need to flip
     * deliveryoption to preDelivery
     */
    if (TicketMedium.FLASHSEAT.getId()
        .equals(lockInventoryResponse.getListing().getTicketMediumId())
        && rootListing.getDeliveryOption().intValue() == 2) {
      rootListing.setDeliveryOption(1);
    }

    // set listing status as ACTIVE only if it is PENDING LOCK
    if (rootListing.getSystemStatus().toUpperCase().startsWith("PENDING")) {
      rootListing.setDeferedActivationDate(null);
      rootListing.setSystemStatus(ListingStatusEnum.ACTIVE.name());
    }

    // repopulate SRS info in listing
    setDbTicketSectionRowAndSeat(ticketSeats, rootListing);
    
    if(rootListing.getIsPiggyBack()) {
      rootListing.setSplitOption((short) 0);
      rootListing.setSplitQuantity(rootListing.getQuantityRemain());
    }
    
    //NFL flow this flag is set based lockInventoryResponse isRegistration flag value
    if(lockInventoryResponse.getIsRegistration() != null && lockInventoryResponse.getIsRegistration()){
    	rootListing.setIsETicket(true);
    }
    
    // update listing
    inventoryMgr.updateListingOnly(rootListing);

    logger.info("_message=\"Leaving updateListingAfterLock method...\" listingId={}", listingId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateListingAfterUnlock(final UnlockInventoryResponse unlockInventoryResponse) {
    listingDAO.updateTicketStatus(unlockInventoryResponse.getListing().getId());
  }

  private void setDbTicketSectionRowAndSeat(List<TicketSeat> ticketSeats, Listing rootListing) {
    List<String> lstSeatNumbers = new ArrayList<String>();
    String section = null;
    boolean isGa = false;

    ListingType listingType = ListingType.getListingTypeById(rootListing.getListingType());
    if (listingType == null) {
      listingType = ListingType.TICKETS_ONLY;
    }

    Map<String, List<String>> rowSeats = new HashMap<String, List<String>>();

    for (TicketSeat ticketSeat : ticketSeats) {
      ListingType ticketListingType = ListingType.getListingTypeById(ticketSeat.getTixListTypeId());
      if (ticketListingType.equals(ListingType.GIFT_CERTIFICATE)) {
        section = ticketSeat.getSection();
        isGa = true;
        break;
      } else if (ticketListingType.equals(ListingType.TICKETS_ONLY)) {
        if (listingType.equals(ListingType.GIFT_CERTIFICATE)) {
          isGa = true;
          section = ticketSeat.getSection();
          break;
        } else if (listingType.equals(ListingType.SERVICES)) {
          section = ticketSeat.getSection();
          break;
        } else if (listingType.equals(ListingType.TICKETS_ONLY)
            || listingType.equals(ListingType.TICKETS_WITH_PARKING_PASSES_INCLUDED)) {

          if (!ticketSeat.isGeneralAdmissionInd()) {
            if (ticketSeat.getSeatNumber() != null) {
              lstSeatNumbers.add(ticketSeat.getSeatNumber());
            } else {
              lstSeatNumbers.add("");
            }
          } else {
            isGa = true;
          }
          if (section == null) {
            section = ticketSeat.getSection();
          }

          if (!rowSeats.containsKey(ticketSeat.getRow())) {
            // if a row does not exist as a key add a key with new list for values
            rowSeats.put(ticketSeat.getRow(), new ArrayList<String>());
          }
          // by this time an entry already exists for this key
          List<String> tempSeats = rowSeats.get(ticketSeat.getRow());
          tempSeats.add(ticketSeat.getSeatNumber());
          rowSeats.put(ticketSeat.getRow(), tempSeats);
        }

      } else if (ticketListingType.equals(ListingType.PARKING_PASSES_ONLY)) {
        if (section == null) {
          section = ticketSeat.getSection();
        }
        if (!lstSeatNumbers.contains(ListingType.PARKING_PASSES_ONLY.getStatus())) {
          lstSeatNumbers.add(ListingType.PARKING_PASSES_ONLY.getStatus());
        }
      } else {
        logger.error("_message=\"Invalid Listing Type\" listingType={} ticketSeatId={}",
            ticketListingType, ticketSeat.getTicketId());
      }
    }
    rootListing.setSection(section);

    // ROW
    // Loop thru the bizTicket to get the Row desc
    // need to use a list here vs. a Set to retain the order in which they appear in the listing
    // object
    List<String> lstUniqueRows = new ArrayList<String>();
    rootListing.setRow(getDBTicketRowDescription(ticketSeats, lstUniqueRows));

    // ****** Sort the seat numbers associated with every key ******

    // used to collect row-seatnumeber eg r1-3,r2-3 combos for piggyback, if needed.
    List<String> lstRowSeats = new ArrayList<String>();
    // list of seats for non piggy back case we dont have to do row-seat combination
    List<String> lstSeats = new ArrayList<String>();

    // rowSeats will have more than one key in the case of piggyback
    // rowSeat size will be 1 in the case of non piggyback
    if (rowSeats.size() > 0) {
      // sort the list of only numeric seat numbers and leave the lists containing non numeric stuff
      // as is
      if (!isGa) {
        sortRowSeatHashMap(rowSeats);
      }

      for (Map.Entry<String, List<String>> entry : rowSeats.entrySet()) {
        List<String> seatValuesList = entry.getValue();
        for (int j = 0; j < seatValuesList.size(); j++) {
          if (rowSeats.size() > 1) {
            lstRowSeats.add(entry.getKey() + "-" + seatValuesList.get(j));
          } else {
            lstSeats.add(seatValuesList.get(j));
          }
        }
      }
    }

    String dbTicketSeats = "";
    if (lstUniqueRows.size() > 1 && !listingType.equals(ListingType.PARKING_PASSES_ONLY)) {
      // handle Piggyback (r1-seat1, r2-seat2, ...rn-seatn)
      dbTicketSeats = StringUtils.join(lstRowSeats, PartnerIntegrationConstants.STRING_SEPERATOR);
    } else {
      if (lstSeatNumbers.size() > 0 && !isGa) {
        dbTicketSeats = StringUtils.join(lstSeats, PartnerIntegrationConstants.STRING_SEPERATOR);
      } else if (!lstRowSeats.isEmpty()) {
        dbTicketSeats = PartnerIntegrationConstants.TO_BE_DEFINED;
      } else {
        dbTicketSeats = PartnerIntegrationConstants.GA;
      }
    }

    if (isHiddenSeats(ticketSeats, listingType)) {
      rootListing.setSeats(PartnerIntegrationConstants.GA);
    } else {
      rootListing.setSeats(dbTicketSeats);
    }
  }

  private String getDBTicketRowDescription(List<TicketSeat> ticketSeats,
      List<String> lstUniqueRows) {
    for (TicketSeat ticketSeat : ticketSeats) {
      ListingType ticketListingType = ListingType.getListingTypeById(ticketSeat.getTixListTypeId());
      if (ticketListingType.equals(ListingType.TICKETS_ONLY)
          || ticketListingType.equals(ListingType.SERVICES)) {
        if (!lstUniqueRows.contains(ticketSeat.getRow())) {
          lstUniqueRows.add(ticketSeat.getRow());
        }
      } else if (ticketListingType.equals(ListingType.GIFT_CERTIFICATE)) {
        lstUniqueRows.add(ticketSeat.getRow());
        break;
      }

    }

    String dbTicketRowDesc = com.stubhub.domain.inventory.common.util.StringUtils
        .join(lstUniqueRows, PartnerIntegrationConstants.STRING_SEPERATOR);
    if ((dbTicketRowDesc == null || dbTicketRowDesc.length() == 0)
        && isParkingPassesOnly(ticketSeats)) {
      dbTicketRowDesc = PartnerIntegrationConstants.LOT;
    }
    return dbTicketRowDesc;
  }

  private boolean isParkingPassesOnly(List<TicketSeat> ticketSeats) {
    for (TicketSeat t : ticketSeats) {
      if (t.getTixListTypeId() != 2L) {
        return false;
      }
    }
    return true;
  }



  private boolean isHiddenSeats(List<TicketSeat> lstTickets, ListingType listingType) {
    if (listingType != null && listingType.equals(ListingType.PARKING_PASSES_ONLY)) {
      return false;
    }
    // loop through all seats and collect all the ones that are 'hidden'. Hidden are Non-GA tickets
    // that have empty seat numbers
    List<TicketSeat> lstHiddenSeats = new ArrayList<TicketSeat>();
    int numTicketSeats = 0;
    for (TicketSeat t : lstTickets) {
      if (t.getTixListTypeId() == 1L) {
        TicketSeat ts = (TicketSeat) t;
        if (!ts.getGeneralAdmissionInd() && StringUtils.isEmptyString(ts.getSeatNumber())) {
          lstHiddenSeats.add(ts);
        }
        numTicketSeats++;
      }
    }

    // if the number of hidden seats equals all the ticket seats in the list then this is a hidden
    // listing.
    if (lstHiddenSeats.size() == numTicketSeats) {
      return true;
    } else {
      return false;
    }
  }

  public void sortRowSeatHashMap(Map<String, List<String>> rowSeats) {
    if (rowSeats.size() > 0) {
      for (Map.Entry<String, List<String>> entry : rowSeats.entrySet()) {
        List<String> seatNumberList = entry.getValue();
        if (seatNumberList == null || seatNumberList.size() == 0) {
          return;
        }

        boolean doSorting = true;
        for (String seatNumber : seatNumberList) {
          if (StringUtils.isEmptyString(seatNumber) || !StringUtils.isStringNumeric(seatNumber)) {
            doSorting = false;
            break;
          }
        }

        // now we can sort the sortableSeatNumbers list
        // if there are entries in nonSortableSeatNumbers we need to skip sorting for this list
        // altogether
        if (doSorting) {
          try {
            Collections.sort(seatNumberList, new Comparator<String>() {

              public int compare(String o1, String o2) {
                int seatNumber1 = Integer.parseInt(o1);
                int seatNumber2 = Integer.parseInt(o2);
                return seatNumber1 - seatNumber2;
              }
            });
          } catch (NumberFormatException e) {
            logger.debug("Number format exception", e);
          }
          rowSeats.put(entry.getKey(), seatNumberList);
        }
      }
    }
  }

  @Override
  public String getUserGuid(Long userId) {
    String userGuid = null;

    try {
      SHAPIContext apiContext = SHAPIThreadLocal.getAPIContext();
      if (apiContext == null) {
        apiContext = new SHAPIContext();
        SHAPIThreadLocal.set(apiContext);
      }

      String customerGuidApiUrl = masterStubhubPropertiesWrapper.getProperty(
          PartnerIntegrationConstants.CUSTOMER_GUID_API_URL,
          PartnerIntegrationConstants.DEFAULT_CUSTOMER_GUID_API_URL);
      customerGuidApiUrl = customerGuidApiUrl.replace("{customerId}", String.valueOf(userId));

      WebClient webClient = svcLocator.locate(customerGuidApiUrl);
      String accessToken = masterStubhubPropertiesWrapper.getProperty(
          PartnerIntegrationConstants.NEWAPI_ACCESS_TOKEN_KEY,
          PartnerIntegrationConstants.ACCESS_TOCKEN_DEFAULT_VALUE);
      String authorization = PartnerIntegrationConstants.BEARER + accessToken;
      webClient.header(PartnerIntegrationConstants.AUTHORIZATION, authorization);
      webClient.accept(MediaType.APPLICATION_JSON);
      webClient.header(PartnerIntegrationConstants.XSH_SERVICE_CONTEXT, "{role=R1}");

      logger.info("_message=\"Calling getCustomerGuid endpoint\" customerGuidApiUrl={} sellerId={}",
          customerGuidApiUrl, userId);

      SHMonitor mon = SHMonitorFactory.getMonitor();
      Response response = null;
      try {
        mon.start();
        response = webClient.get();
      } finally {
        mon.stop();
        logger.info(SHMonitoringContext.get() + " _operation=getUserGuid"
            + " _message= service call for userId=" + userId + "  _respTime=" + mon.getTime());
      }

      if (Response.Status.OK.getStatusCode() == response.getStatus()) {
        InputStream is = (InputStream) response.getEntity();
        ObjectMapper userGuidObjectMapper = new ObjectMapper();
        JsonNode rootNode = userGuidObjectMapper.readTree(is);
        if (rootNode != null) {
          JsonNode customerNode = rootNode.get("customer");
          if (customerNode != null) {
            userGuid = (customerNode.get("userCookieGuid")).getTextValue();
          }
        }
      } else {
        logger.error(
            "_message=\"Failed to get userGuid from customerGuid api\" customerGuidApiUrl={} userId={} "
                + "responseCode={}",
            customerGuidApiUrl, userId, response.getStatus());
      }
    } catch (Exception e) {
      logger.error("_message=\"Unknown exception while making getSellerGuid call\" sellerId={}",
          userId, e);
    }
    return userGuid;
  }

  private PartnerListing createPartnerListing(Listing listing, boolean isLock) {
    return createPartnerListing(listing, isLock, null);
  }

  private PartnerListing createPartnerListing(Listing listing, boolean isLock, List<Long> specifiedTicketSeatIds) {
    logger.info(
            "_message=\"Populating tickets in Lock/Unlock inventory request...\" listingId={} isLock={} totalUnsoldTicketSeatCount={} listingStatus={}",
            listing.getId(), isLock, listing.getTicketSeats().size(), listing.getSystemStatus());
    PartnerListing partnerListing = mapListingToListingRequest(listing);
    List<TicketSeat> listingTicketSeats = listing.getTicketSeats();
    List<TicketSeat> ticketSeats = new ArrayList<>();
    if (specifiedTicketSeatIds != null) {
      for (TicketSeat ticketSeat : listingTicketSeats) {
        if (specifiedTicketSeatIds.contains(ticketSeat.getTicketSeatId())) {
          ticketSeats.add(ticketSeat);
        }
      }
    } else {
      String listingStatus = listing.getSystemStatus();
      for (TicketSeat unsoldTicketSeat : listingTicketSeats) {
        // lock only should send seats that are available
        if (isLock && unsoldTicketSeat.getSeatStatusId().intValue() == TicketSeatStatusEnum.AVAILABLE
                .getCode().intValue()) {
          ticketSeats.add(unsoldTicketSeat);
        }
        // unlock only should send seats that are ideally removed and for
        // listing deleted case send everything
        if (!isLock && (
                ("DELETED".equalsIgnoreCase(listingStatus) || "INACTIVE".equalsIgnoreCase(listingStatus)
                        || unsoldTicketSeat.getSeatStatusId().intValue() == TicketSeatStatusEnum.REMOVED.getCode().intValue()
                        || DateUtil.getNowCalUTC().after(listing.getEndDate())) && !"statusNotify".equals(unsoldTicketSeat.getLastUpdatedBy())
        )) {
          logger.info(
                  "_message=\"ticketSeat is eligible for unlock.\" listingId={} ticketSeatId={} listingStatus={} lastUpdatedBy={}",
                  listing.getId(), unsoldTicketSeat.getTicketSeatId(), listingStatus, unsoldTicketSeat.getLastUpdatedBy());
          ticketSeats.add(unsoldTicketSeat);
        }
      }
    }

    partnerListing.setProducts(createProducts(ticketSeats));
    logger.info(
            "_message=\"Populated tickets in Lock/Unlock inventory request...\" listingId={} isLock={} populatedTicketSeatCount={}",
            listing.getId(), isLock, ticketSeats.size());
    return partnerListing;
  }

  private List<PartnerProduct> createProducts(List<TicketSeat> ticketSeats) {
    List<PartnerProduct> products = new ArrayList<>(ticketSeats.size());
    for (TicketSeat ticketSeat : ticketSeats) {
      ListingType listingType = ListingType.getListingTypeById(ticketSeat.getTixListTypeId());
      PartnerProduct product = new PartnerProduct();
      product.setSeatId(ticketSeat.getTicketSeatId());
      product.setSection(ticketSeat.getSection());
      product.setRow(ticketSeat.getRow());
      product.setSeat(ticketSeat.getSeatNumber());
      product.setSeatStatus(
          TicketStatusEnum.getTicketSeatStatusEnumById(ticketSeat.getSeatStatusId().intValue()));
      if (listingType.equals(ListingType.TICKETS_ONLY)) {
        product.setProductType(ProductType.TICKET);
      } else if (listingType.equals(ListingType.PARKING_PASSES_ONLY)) {
        product.setProductType(ProductType.PARKING_PASS);
      }
      product.setGa(ticketSeat.getGeneralAdmissionInd());
      products.add(product);
    }
    return products;
  }

  private PartnerListing mapListingToListingRequest(Listing listing) {
    PartnerListing partnerListing = new PartnerListing();
    partnerListing.setId(listing.getId());
    partnerListing.setEventId(listing.getEventId());
    partnerListing.setSellerId(listing.getSellerId());
    if (listing.getListPrice() != null && listing.getListPrice().getAmount() != null) {
      partnerListing.setPricePerProduct(String.valueOf(
          listing.getListPrice().getAmount().doubleValue() * 100 * listing.getQuantityRemain()));
    }
    partnerListing.setTicketMediumId(listing.getTicketMedium());
    return partnerListing;
  }

  @Override
  @Transactional
  public void updateListingAndSeats(Listing listing, List<TicketSeat> ticketSeats) {
    if(ticketSeats != null && !ticketSeats.isEmpty()) {
      for (TicketSeat ticketSeat : ticketSeats) {
        ticketSeatMgr.updateTicketSeat(ticketSeat);
      }
    }
    inventoryMgr.updateListingOnly(listing); 
  }
  
  @Override
  @Transactional
  public void updateTicketStatus(Long listingId, short s) {
	  listingDAO.updateTicketStatus(listingId, s);
  }

}
