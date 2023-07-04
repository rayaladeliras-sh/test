package com.stubhub.domain.inventory.listings.v2.helper;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.catalog.datamodel.entity.VenueConfigSection;
import com.stubhub.domain.catalog.events.biz.intf.VenueConfigSectionsBO;
import com.stubhub.domain.catalog.read.v3.intf.venues.dto.response.VenueConfiguration;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.adapter.ListingResponseAdapter;
import com.stubhub.domain.inventory.listings.v2.entity.DM;
import com.stubhub.domain.inventory.listings.v2.entity.DeliveryMethod;
import com.stubhub.domain.inventory.listings.v2.entity.ExpectedDeliveryDate;
import com.stubhub.domain.inventory.listings.v2.entity.UserContact;
import com.stubhub.domain.inventory.listings.v2.enums.DMEnum;
import com.stubhub.domain.inventory.listings.v2.util.EventHelper;
import com.stubhub.domain.inventory.listings.v2.util.FulfillmentServiceHelper;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.v2.DTO.ProSellerInfo;
import com.stubhub.domain.inventory.v2.enums.ProxyRoleTypeEnum;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component("listingHelper")
public class ListingHelper {

  private final static Logger log = LoggerFactory.getLogger(ListingHelper.class);

  @Autowired
  private InventoryMgr inventoryMgr;

  @Autowired
  private TicketSeatMgr ticketSeatMgr;

  @Autowired
  private VenueConfigSectionsBO venueConfigSectionsBO;

  @Autowired
  private EventHelper eventHelper;

  @Autowired
  private MasterStubhubPropertiesWrapper masterStubhubProperties;

  @Autowired
  private FulfillmentServiceHelper fulfillmentServiceHelper;

  @Autowired
  private UserHelper userHelper;

  @Autowired
  private ListingSeatTraitMgr listingSeatTraitMgr;

  public ListingResponse populateListingDetails(String status, Long listingId, Locale locale,
      Long userId, SHServiceContext shServiceContext, String expand) {

    ProxyRoleTypeEnum proxyRoleType = ProxyRoleTypeEnum.getProxyRoleTypeEnumByName(shServiceContext.getRole());
    Listing listing = inventoryMgr.getListing(listingId, locale);
    boolean isSeller = false;
    if (listing == null) {
      ListingError listingError =
          new ListingError(ErrorType.NOT_FOUND, ErrorCode.INVALID_LISTINGID,
              "There is no listing exist in the system with this ID", "");
      throw new ListingBusinessException(listingError);
    }
    if (userId != null && userId.equals(listing.getSellerId())) {
      isSeller = true;
    }
    if (StringUtils.isBlank(status)) {
      if (((proxyRoleType == null && !isSeller))
          && !("ACTIVE".equals(listing.getSystemStatus()) || "HIDDEN".equals(listing
          .getSystemStatus()))) {
        ListingError listingError =
            new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_LISTINGID,
                "There is no listing exist in the system with this ID", "");
        throw new ListingBusinessException(listingError);
      } else if (Calendar.getInstance().after(listing.getEndDate())) {
        ListingError listingError =
            new ListingError(ErrorType.INPUTERROR, ErrorCode.LISTING_EXPIRED,
                "Either the event is expired or the listing sale end has passed", "");
        throw new ListingBusinessException(listingError);
      } else if (listing.getQuantityRemain() != null && listing.getQuantityRemain().intValue() == 0) {
        ListingError listingError =
            new ListingError(ErrorType.INPUTERROR, ErrorCode.LISTING_ALREADY_SOLD,
                "All seats of this listing are sold", "");
        throw new ListingBusinessException(listingError);
      }
    }

    boolean isScrubbingEnabled = false;
    boolean sectionMappingRequired = false;
    if (listing.getVenueConfigSectionsId() == null) {
      VenueConfiguration venueConfig = eventHelper.getVenueDetails(listing.getEventId());
      if (venueConfig != null && venueConfig.getMap() != null && venueConfig.getMap().getSectionScrubbing() != null) {
        if (venueConfig.getMap().getSectionScrubbing()) {
          isScrubbingEnabled = true;
          if (listing.getSectionScrubSchedule() == null || !listing.getSectionScrubSchedule()) {
            sectionMappingRequired = true;
          }
        }
      }
    } else {
      isScrubbingEnabled = true;
    }

    String faceValueNeededCountries =
        masterStubhubProperties.getProperty("listing.country.facevalue.required", "UK,GB,DE");
    boolean faceValueRequired = false;
    String countryCode = "NONE";
    com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 = null;
    try {
      if (SHAPIThreadLocal.getAPIContext() != null) {
        eventV3 = eventHelper.getEventV3ById(listing.getEventId(), locale, true, true);

        if (eventV3 != null) {
          Event event = new Event();
          event.setId(eventV3.getId());
          event.setDescription(eventV3.getName());
          listing.setEvent(event);
        }

        if (eventV3 != null && eventV3.getVenue() != null
            && eventV3.getVenue().getCountry() != null) {
          countryCode = eventV3.getVenue().getCountry();
        }
      }

      if (faceValueNeededCountries.contains(countryCode)) {
        faceValueRequired = true;
      }
    } catch (Exception e) {
      log.error(
          "api_domain=inventory api_resource=getListing api_method=getListing status=error error_message=\"System error occured while get listing \"",
          e);
      ListingError listingError =
          new ListingError(ErrorType.INPUTERROR, ErrorCode.LISTING_EXPIRED,
              "Either the event is expired or the listing sale end has passed", "");
      throw new ListingBusinessException(listingError);
    }

    try {
      if (proxyRoleType == null) {
        listing.setStubhubMobileTicket(getStubHubMobileTicket(listing, locale, eventV3));
      }

      if (StringUtils.isBlank(status)) {
        listing.setTicketSeats(ticketSeatMgr.findActiveTicketSeatsByTicketId(listing.getId()));
      } else {
        listing.setTicketSeats(ticketSeatMgr.findAllTicketSeatsByTicketId(listing.getId()));
      }

      List<DeliveryMethod> deliveryMethodList = null;
      //SELLAPI-4259 - if we get request parame expand=deliveryMethods then will make FFWindowcall during getListing.
      if (!isSeller && (!StringUtils.isBlank(expand) && expand.equalsIgnoreCase("deliveryMethods"))) {
        deliveryMethodList = getDeliveryMethods(listing, userId, eventV3);
      } else if (!isSeller) {
        deliveryMethodList = getDeliveryMethodsWithOutQueryParam(listing);
      }
      listing.setScrubbedSectionName(getScrubbedSectionName(listing));

      listing.setSeatTraits(populateListingSeatTraits(listingId));

      ListingResponse response =
          ListingResponseAdapter.convert(listing, isSeller, deliveryMethodList, proxyRoleType,
              faceValueRequired, sectionMappingRequired, isScrubbingEnabled, eventV3, shServiceContext.getOperatorId());
      if (response == null) {
        return response;
      }
      //removed Commented code from here- this as updating just LISTING_SEAT_TRAIT will not re-index the listings in mci

      if (listing != null && listing.getVenueConfigSectionsId() != null) {
        String localizedSectionName = eventHelper.getLocalizedSeatingSectionName(listing.getVenueConfigSectionsId(),
            shServiceContext.getLocale());
        if (localizedSectionName != null) {
          response.setLocalizedSectionName(localizedSectionName);
        }
      }
      if (response.getLocalizedSectionName() == null) {
        response.setLocalizedSectionName(response.getSection());
      }

      ProSellerInfo sellerInfo = userHelper.getSellerInfo(listing.getSellerId());
      if (sellerInfo != null) {
        response.setProSellerInfo(sellerInfo);
      }

      response.setEventCountry(countryCode);

      return response;

    } catch (Exception e) {
      log.error(
          "api_domain=inventory api_resource=getListing api_method=getListing status=error error_message=\"System error\"",
          e);
      ListingError listingError =
          new ListingError(ErrorType.SYSTEMERROR, ErrorCode.UNKNOWN_ERROR, e.getMessage(), "");
      throw new ListingBusinessException(listingError);
    }
  }

  private List<ListingSeatTrait> populateListingSeatTraits(Long listingId) {
    return listingSeatTraitMgr.findSeatTraits(listingId);
  }

  private Integer getStubHubMobileTicket(Listing listing, Locale locale,
      com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3) {
    Integer stubhubMobileTicket = 0;

    if (listing.getTicketMedium().equals(TicketMedium.BARCODE.getValue())
        && listing.getDeliveryOption().intValue() == com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.PREDELIVERY
        .getValue()) {
      if (eventV3 != null) {
        if (eventV3.getMobileAttributes() != null
            && eventV3.getMobileAttributes().getStubhubMobileTicket() != null) {
          stubhubMobileTicket =
              eventV3.getMobileAttributes().getStubhubMobileTicket() == true ? 1 : 0;
        }
      }
    }
    return stubhubMobileTicket;
  }

  private List<DeliveryMethod> getDeliveryMethods(Listing listing, Long buyerId,
      com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3) {
    List<DeliveryMethod> deliveryMethodsList = null;
    try {
      Long buyerDefaultContactId = null;
      if (buyerId != null) {
        buyerDefaultContactId = getDefaultBuyerContactId(buyerId);
      }

      Event event = new Event();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
      Calendar eventDate = Calendar.getInstance();
      eventDate.setTime(sdf.parse(eventV3.getEventDateUTC()));
      event.setEventDate(eventDate);
      event.setJdkTimeZone(TimeZone.getTimeZone(eventV3.getTimezone()));

      deliveryMethodsList =
          fulfillmentServiceHelper.getDeliveryMethodsForListingId(listing.getId(),
              buyerDefaultContactId, listing.getInhandDate(),
              isTicketInHand(listing.getDeclaredInhandDate()), event);
    } catch (Exception e) {
      log.error(
          "api_domain=inventory"
              + " api_resource="
              + " api_method=getListingById status=success_with_error message=\"Exception occured in getting the fulfillmentWindows\""
              + "listingId={}", listing.getId(), e);
    }
    return deliveryMethodsList;
  }

  /**
   * @param listing
   * @return List<DeliveryMethod>
   * <p>
   * SELLAPI-4259: Inventory v2 Get Listings -  delivery methods most of the teams not consuming, for the backward compatability populating
   * delivery method response from fmdmlist values from tickets table, if delivery methods unavailable it returns default as electronic.
   */
  private List<DeliveryMethod> getDeliveryMethodsWithOutQueryParam(Listing listing) {
    List<DeliveryMethod> deliveryMethodList = new ArrayList<>();
    List<String> dmList = new ArrayList();
    DeliveryMethod deliveryMethod = new DeliveryMethod();
    ExpectedDeliveryDate ed = new ExpectedDeliveryDate();
    int dmEnumOrdinal = 1;
    DMEnum dmEnum = null;
    DM dmObj = null;

    String fmdmListString = listing.getFulfillmentDeliveryMethods();

    if (StringUtils.isNotBlank(fmdmListString)) {
      String[] fmdmListArray = fmdmListString.split("\\|");
      String[] deliveryMethodArray = fmdmListArray[0].split(",");
      if (deliveryMethodArray.length >= 5) {
        dmList.add(deliveryMethodArray[1].trim());
        dmList.add(deliveryMethodArray[4].trim());

        if (StringUtils.isNotBlank(dmList.get(0)) && StringUtils.isNumeric(dmList.get(0))) {
          dmEnumOrdinal = Integer.parseInt(dmList.get(0));
          dmEnum = DMEnum.getDMEnumByCode(dmEnumOrdinal);
        } else {
          dmEnum = DMEnum.getDMEnumByCode(1);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Calendar deliveryDate = Calendar.getInstance();

        try {
          if (StringUtils.isNotBlank(dmList.get(1))) {
            deliveryDate.setTime(sdf.parse(dmList.get(1)));
          } else {
            deliveryDate.add(Calendar.DATE, 5);
          }
        } catch (ParseException e) {
          log.error("message=\"Error occured in converting while setting deliveryDate\"", e);
        }

        if (dmEnum != null) {
          dmObj = new DM(dmEnumOrdinal, dmEnum.getName(), deliveryDate);
        } else {
          dmObj = new DM(1, "Electronic", deliveryDate);
        }

        ed.setExpectedDate(dmObj.getEstimatedDeliveryTime());

        deliveryMethod.setId(Long.valueOf(dmObj.getId()));
        deliveryMethod.setName(dmObj.getName());
        deliveryMethod.setExpectedDeliveryDate(ed);

        deliveryMethodList.add(deliveryMethod);
      }
    }
    if (deliveryMethodList.isEmpty()) {
      setDefaultDeliveryMethod(deliveryMethodList, deliveryMethod, ed);
    }
    return deliveryMethodList;
  }

  private void setDefaultDeliveryMethod(List<DeliveryMethod> deliveryMethodList, DeliveryMethod deliveryMethod, ExpectedDeliveryDate ed) {
    deliveryMethod.setId(1l);
    deliveryMethod.setName("Electronic");
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DATE, 5);
    ed.setExpectedDate(calendar);
    deliveryMethod.setExpectedDeliveryDate(ed);

    deliveryMethodList.add(deliveryMethod);
  }

  private boolean isTicketInHand(Calendar declaredInHandDate) {
    return (declaredInHandDate != null) ? true : false;
  }

  private Long getDefaultBuyerContactId(Long buyerId) {
    Long defaultBuyerContactId = null;
    UserContact userContact = userHelper.getDefaultUserContact(buyerId);
    if (userContact != null) {
      defaultBuyerContactId = userContact.getId();
    }
    return defaultBuyerContactId;
  }

  public List<TicketSeat> getTicketSeatsInfoByTicketId(String ticketId) {
    List<TicketSeat> ticketSeatList = ticketSeatMgr.findTicketSeatsByTicketId(Long.parseLong(ticketId));
    return ticketSeatList;

  }

  /**
   * Returns the section Alias for the given listing
   *
   * @param dbListing
   * @return venueConfigSectionAlias
   */
  public String getScrubbedSectionName(
      com.stubhub.domain.inventory.datamodel.entity.Listing dbListing) {
    VenueConfigSection venueConfigSection =
        venueConfigSectionsBO.getVenueConfigSectionAliasBySectionId(dbListing
            .getVenueConfigSectionsId());
    if (venueConfigSection != null) {
      if (log.isDebugEnabled()) {
        log.debug("_message=\"venue configuration section alias is : {} \"",
            venueConfigSection.getVenueConfigSectionAlias());
      }
      return venueConfigSection.getVenueConfigSectionAlias();
    } else {
      log.info("_message=\"No venueConfigSection instance returned for sectionId {}\"",
          dbListing.getVenueConfigSectionsId());
      return dbListing.getSection();
    }
  }

}