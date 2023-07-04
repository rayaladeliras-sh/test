package com.stubhub.domain.inventory.listings.v2.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.infrastructure.common.core.concurrent.SHThreadLocals;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.common.entity.ErrorEnum;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.EventError;
import com.stubhub.domain.inventory.common.util.EventMappingException;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingSource;
import com.stubhub.domain.inventory.listings.v2.adapter.ListingResponseAdapter;
import com.stubhub.domain.inventory.listings.v2.nlproc.ListingData;
import com.stubhub.domain.inventory.listings.v2.nlproc.ListingToDataAdapter;
import com.stubhub.domain.inventory.listings.v2.tns.FraudEvaluationService;
import com.stubhub.domain.inventory.listings.v2.util.ErrorUtils;
import com.stubhub.domain.inventory.listings.v2.util.EventHelper;
import com.stubhub.domain.inventory.v2.DTO.EventInfo;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.DTO.TicketTrait;
import com.stubhub.domain.inventory.v2.bulk.DTO.BulkListingInternal;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.domain.inventory.v2.listings.eventmapper.EventMapperAdaptor;
import com.stubhub.domain.search.catalog.v3.intf.dto.response.event.ShipEvent;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;

@Component("ListingCreateProcess")
public class ListingCreateProcess {
  private final static Logger log = LoggerFactory.getLogger(ListingCreateProcess.class);

  private static String api_domain = "inventory";
  private static String api_resource = "listing";
  private static final String COUNTRIES_HIDESEATS_PROHIBITED = "listing.country.hideseats.prohibited";
  private static final String CHP_DEFAULT = "GB,DE,FR";

  @Value("#{'${inventory.create.listing.event.countries.whitelist:*}'.trim().toUpperCase().split(',')}")
  private Set<String> countryWhiteList;

  @Autowired
  private UpdateListingAsyncHelper2 updateListingAsyncHelper2;

  @Autowired
  private EventHelper eventHelper;

  @Autowired
  private InventoryMgr inventoryMgr;

  @Autowired
  @Qualifier("eventMapperAdaptor")
  private EventMapperAdaptor eventMapperAdaptor;

  @Autowired
  private SellerEligibilityHelper sellerEligibilityHelper;

  @Autowired
  private MasterStubhubPropertiesWrapper masterStubhubProperties;

  @Autowired
  private FraudEvaluationService fraudEvaluationService;

  /**
   * Backward compatible call to create bulk listing
   * @param bli
   * @param clientIp
   * @param userAgent
   * @return list of ListingResponse objects (that might include errors)
   */
  public List<ListingResponse> createListings (BulkListingInternal bli, String clientIp, String userAgent )
  {
    return createOrUpdateListings (bli, clientIp, userAgent, true, true);
  }

  public List<ListingResponse> updateListings (BulkListingInternal bli, String clientIp, String userAgent )
  {
    return createOrUpdateListings (bli, clientIp, userAgent, false, true);
  }

  /**
   * INTERNAL METHOD for creating batch listings (this is NOT exposed as an end-point). NOTE: this method will never throw an Exception
   * @param clientIp TODO
   * @param userAgent TODO
   * @param bli bulk listing data object (never exposed to presentation layer)
   * @param isCreate if true create else update
   * @param isBulk if true means intended as bulk call (even if there is only one request), false means not intended to be bulk
   * @return list of ListingResponse objects (that might include errors)
   */
  public List<ListingResponse> createOrUpdateListings (BulkListingInternal bli, String clientIp, String userAgent,
                                                       boolean isCreate, boolean isBulk)
  {
    // start monitor
    SHMonitor mon = SHMonitorFactory.getMonitor().start();
    if(isBulk) {
      log.info("message=\"Processing bulk listing requests for jobId={} groupId={} sellerId={} isCreate={}\"",
              bli.getJobId(), bli.getGroupId(), bli.getSellerId(), isCreate);
    }
    ListingToDataAdapter adapter = new ListingToDataAdapter();

    SHAPIContext apiContext = new SHAPIContext();
    apiContext.setSignedJWTAssertion(bli.getAssertion());
    SHAPIThreadLocal.set(apiContext);

    SHServiceContext shServiceContext = (SHServiceContext) SHThreadLocals.get(SHServiceContext.SERVICE_CONTEXT_HEADER);

    List<ListingResponse> responses = null;
    try {
      ListingData listingData = null;

      if ( isCreate ) {
        listingData = adapter.listingDataFromCreateRequests(bli.getSellerId(),
                bli.getSellerGuid(), apiContext, shServiceContext, bli.getCreateListingBody(), isBulk);
      }
      else {
        listingData = adapter.listingDataFromUpdateRequests(inventoryMgr, bli.getSellerId(),
                bli.getSellerGuid(), apiContext, shServiceContext, bli.getCreateListingBody(), isBulk);
      }

      if ( listingData == null ) {
        ListingError error = new ListingError(ErrorType.INPUTERROR, ErrorCode.INPUT_ERROR, "Invalid Request", "");
        throw new ListingBusinessException(error);
      }
      else if ( listingData.getListingErrors()!=null && listingData.getListingErrors().size()>0 ) {
        throw new ListingBusinessException (listingData.getListingErrors().get(0) );
      }

      // figure out the event stuff once
      Event event = checkGetEventDetails ( bli.getLocale(), listingData.getHeaderListing(),
              listingData.getHeaderRequest(), listingData.getSellerId(),
              isCreate, listingData.needDeepEventLookup());
      if (event==null){
        log.error("Event is null for eventId={}",listingData.getHeaderRequest().getEventId());
        ListingError listingError = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.INVALID_EVENTID,
                "Event ID is invalid", "eventId");
        throw new ListingBusinessException(listingError);
      }

      String eventCountry = event.getCountry();

      if (isCreate && !countryWhiteList.contains("*") && !countryWhiteList.contains(eventCountry)) {
        log.error("not allowed created event from country={} whitelist={}", eventCountry, countryWhiteList);
        ListingError listingError = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.INVALID_EVENTID,
                "Not allow create event from country=" + eventCountry, null);
        throw new ListingBusinessException(listingError);
      }

      //validate seat hiding for permitted countries
      String countriesHideSeatsProhibited = masterStubhubProperties.getProperty(COUNTRIES_HIDESEATS_PROHIBITED, CHP_DEFAULT);
      if (countriesHideSeatsProhibited.contains(eventCountry)) {
        boolean hideSeatsValidationFailed = false;
        hideSeatsValidationFailed = hideSeatsValidation(listingData);
        if (hideSeatsValidationFailed) {
          ListingError listingError = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.HIDE_SEATS_NOT_ALLOWED,
                  "Seat hiding is prohibited in " + eventCountry, null);
          throw new ListingBusinessException(listingError);
        }
      }



      if(isCreate && !isHiddenListing(bli.getCreateListingBody().get(0))){
        Integer listingSourceNumber = getListingSourceBasedOnDeliveryOptionAndCreateListing(listingData);
        boolean isAllowedToSell = sellerEligibilityHelper.checkSellerEligibility(bli.getSellerGuid(), ListingSource.getListingSource(listingSourceNumber).getDescription(), event.getId());
        if(!isAllowedToSell) {
          ListingError listingError = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.NOT_ALLOWED_TO_LIST, "Seller not allowed to list", null);
          throw new ListingBusinessException(listingError);
        }
      }

      listingData.updateEventInfo ( event );
      if(bli.getLocale() != null){
        listingData.setLocale( bli.getLocale() );
      }else if (event.getLocale() != null){
        listingData.setLocale(new Locale(event.getLocale()));
      }
      listingData.setSubscriber(bli.getSubscriber());
      listingData.setOperatorId(bli.getOperatorId());
      listingData.setRole(bli.getRole());

      listingData.setSellShStoreId(bli.getSellShStoreId());


      //if parking only event
      if (event.isParkingOnlyEvent() != null && Boolean.TRUE.equals(event.isParkingOnlyEvent())) {
        List<ListingRequest> requestBodyList = 	listingData.getRequestBodies();

        if (requestBodyList != null) {
          for (ListingRequest lr : requestBodyList) {
            //set product type as 'ticket'
            List<Product> productList = lr.getProducts();
            if (productList != null) {
              for (Product prod : productList) {
                prod.setProductType(ProductType.TICKET);
              }
            }
            //remove seat trait 'parking pass' from seat trait list
            List<TicketTrait> ticketTraitsReq = lr.getTicketTraits();
            List<TicketTrait> ticketTraitsModified = new ArrayList<TicketTrait> ();
            if (ticketTraitsReq != null) {
              for (TicketTrait tt: ticketTraitsReq) {
                if ("Parking pass".equalsIgnoreCase(tt.getName()) || "102".equals(tt.getId())) {
                  continue;
                } else {
                  ticketTraitsModified.add(tt);
                }
              }
              lr.setTicketTraits(ticketTraitsModified);
            }
          }
        }
      }

      // do the batch processing of all requests
      responses = updateListingAsyncHelper2.createOrUpdateListingData( listingData, clientIp, userAgent );

      // info message about end of run
      mon.stop();
      log.info("{} _operation="+(isCreate?"createListing":"updateListing")+" _message=\"Listings created successfully \" _status=OK _respTime={} sellerId={} no.of.Listings={}",
              SHMonitoringContext.get(), mon.getTime(),
              bli.getSellerId(), bli.getCreateListingBody().size());

      // For single request make sure errors will report the correct status
      if ( !isBulk && responses.size()>0 ) {
        List<ListingError> errors = responses.get(0).getErrors();
        if ( errors!=null && errors.size()>0 ) {
          throw new ListingBusinessException (errors.get(0) );
        }
      }
    }
    catch (ListingBusinessException listingException) {
      mon.stop();
      log.warn(
              "{} _operation="+(isCreate?"createListing":"updateListing")+" _message=\"Listings creation failed \" _status=CLIENT_ERROR _respTime={} sellerId={} no.of.Listings={} error={}",
              SHMonitoringContext.get(), mon.getTime(),
              bli.getSellerId(), bli.getCreateListingBody().size(), listingException.getListingError().getMessage());
      // set error in responses (only for bulk requests)
      if ( isBulk )
        responses = ErrorUtils.responsesFromError(bli.getCreateListingBody(), listingException.getListingError());
      else
        ListingResponseAdapter.errorMappingThrowException(listingException);
    }
    catch (Exception e) {
      mon.stop();
      log.error("exception", e);
      if ( e.getCause()!=null && e.getCause() instanceof ListingBusinessException) {
        ListingBusinessException listingException = (ListingBusinessException)e.getCause();
        log.warn(
                "{} _operation="+(isCreate?"createListing":"updateListing")+" _message=\"Listings creation failed \" _status=CLIENT_ERROR _respTime={} sellerId={} no.of.Listings={} error={}",
                SHMonitoringContext.get(), mon.getTime(),
                bli.getSellerId(), bli.getCreateListingBody().size(), listingException.getListingError().getMessage());


        // set error in responses (only for bulk requests)
        if ( isBulk )
          responses = ErrorUtils.responsesFromError(bli.getCreateListingBody(), listingException.getListingError());
        else
          ListingResponseAdapter.errorMappingThrowException(listingException);
      }
      else {
        log.error(
                "{} _operation="+(isCreate?"createListing":"updateListing")+" _message=\"Listings creation failed \" _status=SYSTEM_ERROR _respTime={} sellerId={} no.of.Listings={} error={}",
                SHMonitoringContext.get(), mon.getTime(),
                bli.getSellerId(), bli.getCreateListingBody().size(), e.getMessage());
        ListingError sysError = new ListingError(ErrorType.SYSTEMERROR, ErrorCode.SYSTEM_ERROR, "System error please retry", "" );

        if ( isBulk )
          responses = ErrorUtils.responsesFromError(bli.getCreateListingBody(), sysError);
        else
          throw new SHSystemException ( "Unable to process the request from error, please re-try");
      }
    }
    if(CollectionUtils.isNotEmpty(responses)) {
      if ( isCreate && !isBulk ) {
        submitForFraudEvaluation(responses);
      }else {
        log.info("message=\"Ignoring fraud evaluation submission jobId={} groupId={} sellerId={} isCreate={} isBulk={} \"",
                bli.getJobId(), bli.getGroupId(), bli.getSellerId(), isCreate, isBulk);
      }
      //Setting Event Id to null to honor existing response specification
      for (ListingResponse listingResponse : responses) {
        listingResponse.setEventId(null);
        listingResponse.setSellerId(null);
      }
    }
    return responses;
  }

  private void submitForFraudEvaluation(List<ListingResponse> responses) {
    ListingResponse listingResponse = responses.get(0);
    fraudEvaluationService.submitToCloudConfluentKafka(listingResponse.getId());
  }

  private boolean isHiddenListing(ListingRequest listingRequest) {
    if(listingRequest != null && listingRequest.getStatus() != null && listingRequest.getStatus() == ListingStatus.HIDDEN) {
      return true;
    } else {
      return false;
    }
  }

  private boolean hideSeatsValidation (ListingData listingData) {
    boolean validationFailed = false;
    if (listingData.getRequestBodies() != null) {
      for (ListingRequest listingReq : listingData.getRequestBodies()) {
        if (listingReq.isHideSeats() != null && listingReq.isHideSeats()) {
          validationFailed = true;
          break;
        }
      }
    }
    return validationFailed;
  }

  private Integer getListingSourceBasedOnDeliveryOptionAndCreateListing(ListingData listingData) {
    Integer listingSourceNumber = null;
    List<ListingRequest> requestsBody=listingData.getRequestBodies();
    for (ListingRequest listingRequest : requestsBody) {
      com.stubhub.domain.inventory.common.entity.DeliveryOption dOption = listingRequest.getDeliveryOption();
      if(dOption != null && dOption.equals(com.stubhub.domain.inventory.common.entity.DeliveryOption.STH)) {
        listingSourceNumber = 8;
      }
      else{
        listingSourceNumber=10;
      }
    }
    return listingSourceNumber;
  }


  /**
   * Check event details for create listing
   * @param listing
   * @param request
   */
  private Event checkGetEventDetails (Locale locale, Listing listing, ListingRequest request, Long sellerId,
                                      boolean isCreate, boolean getTraits)
  {
    Long eventId = null;
    Event event = null;
    // Check the passed event and if invalid throw an exception
    if (StringUtils.trimToNull(request.getEventId()) == null ) {
      if ( request.getEvent() != null) {
        EventInfo eventInfo = request.getEvent();
        if (StringUtils.trimToNull(eventInfo.getVenue()) == null || StringUtils.trimToNull(eventInfo.getName()) == null
                || (StringUtils.trimToNull(eventInfo.getDate()) == null && StringUtils.trimToNull(eventInfo.getEventLocalDate()) == null)) {
          ListingError error = new ListingError(ErrorType.INPUTERROR,
                  ErrorCode.MISSING_EVENT_INFO,
                  "Missing event info details", "event");
          throw new ListingBusinessException (error);
        }
      } else if ( isCreate ) {
        ListingError error = new ListingError(ErrorType.INPUTERROR,
                ErrorCode.MISSING_EVENT_INFO,
                "Need to pass either eventId or eventInfo related to this listing", "event");
        throw new ListingBusinessException (error);
      }

      try {
        ShipEvent shipEvent = eventMapperAdaptor.mapEvent(locale, request.getEvent(), String.valueOf(sellerId));
        if(shipEvent == null || shipEvent.getId() == null) {
          log.warn("No event found for the input event info");
          ListingError error = new ListingError(ErrorType.INPUTERROR, ErrorCode.EVENT_NOT_MAPPED, ErrorEnum.EVENT_NOT_MAPPED.getMessage(), "event");
          throw new ListingBusinessException(error);
        }
        eventId = Long.valueOf(shipEvent.getId());
      } catch (EventMappingException e) {
        EventError eventError = e.getEventError();
        ListingError error = new ListingError(eventError.getType(), eventError.getCode(), eventError.getMessage(), "event");
        log.error("EventMappingException occured while trying to map the event=" + request.getEvent() + ", error=" + eventError.getMessage());
        throw new ListingBusinessException(error);
      }

    }
    else {
      eventId = Long.valueOf(request.getEventId());
    }
    if ( eventId != null ) {
      event = eventHelper.getEventObject (locale, listing, eventId, getTraits );
    }
    return event;
  }
}
