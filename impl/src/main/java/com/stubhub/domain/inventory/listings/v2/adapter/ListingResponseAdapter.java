package com.stubhub.domain.inventory.listings.v2.adapter;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.catalog.events.biz.intf.VenueConfigSectionsBO;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.SeatTrait;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHBadRequestException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHForbiddenException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHResourceNotFoundException;
import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.entity.SaleMethod;
import com.stubhub.domain.inventory.common.entity.SplitOption;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.util.ErrorUtils;
import com.stubhub.domain.inventory.listings.v2.util.EventHelper;
import com.stubhub.domain.inventory.listings.v2.util.TimeUtils;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.DTO.ProductDetail;
import com.stubhub.domain.inventory.v2.DTO.TicketTrait;
import com.stubhub.domain.inventory.v2.enums.*;
import com.stubhub.domain.inventory.v2.listings.intf.DeliveryMethod;
import com.stubhub.newplatform.common.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component("listingResponseAdapterV2")
public class ListingResponseAdapter {

  private static final Logger log = LoggerFactory.getLogger(ListingResponseAdapter.class);

  private static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ";

  private static final long SEAT_TRAIT_PARTNER_TICKET = 13372L; // SALLAPI-1041 07/17/15

  private static final String PARKING = "Parking";

  private static final String EVENT_RESTRICTIONS= "Event Restrictions";

  private static final String PENDING_LOCK = "PENDING LOCK";

  private static final String PENDING_PDF_REVIEW = "PENDING PDF REVIEW";

  private static final String RELIST = "Relist";

  private static final Integer SNOWIND = 1;

  @Autowired
  private VenueConfigSectionsBO venueConfigSectionsBO;

  public VenueConfigSectionsBO getVenueConfigSectionsBO() {
    return venueConfigSectionsBO;
  }

  public void setVenueConfigSectionsBO(VenueConfigSectionsBO venueConfigSectionsBO) {
    this.venueConfigSectionsBO = venueConfigSectionsBO;
  }

  public static ListingResponse convert(Listing listing, boolean isSeller,
      List<com.stubhub.domain.inventory.listings.v2.entity.DeliveryMethod> deliveryMethodList,
      ProxyRoleTypeEnum proxyRoleType, boolean faceValueRequired, boolean sectionMappingRequired,
      boolean isScrubbingEnabled, com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3, String operatorId) {
    ListingResponse response = new ListingResponse();
    if (proxyRoleType != null) {
      response.setSellerIpAddress(listing.getIpAddress());
    }
    if (proxyRoleType != null || isSeller) {
      if (listing.getTicketCost() != null && listing.getTicketCost().getAmount() != null) {
        response.setPurchasePrice(listing.getTicketCost());
      }
      if (listing.getFaceValue() != null && listing.getFaceValue().getAmount() != null) {
        response.setFaceValue(listing.getFaceValue());
      }
      //CDN Changes
      if (listing.getSellerInputPrice()!=null && listing.getSellerInputPrice().getAmount()!=null) {
        response.setSellerInputPrice(listing.getSellerInputPrice());
      }

      if (listing.getSellerInputPriceType()!=null) {
        response.setSellerInputPriceType(listing.getSellerInputPriceType());
      }

	// EXTSELL-155 populate payoutperTicket and totalPayout
     if(listing.getSellerPayoutAmountPerTicket()!= null)
      	response.setPayoutPerProduct(listing.getSellerPayoutAmountPerTicket());
     if(listing.getTotalSellerPayoutAmt() != null)
	response.setTotalPayout(listing.getTotalSellerPayoutAmt());
     if (listing.getExternalId() != null) {
        response.setExternalListingId(listing.getExternalId());
      }
      response.setInternalNotes(listing.getSellerInternalNote());
      if (listing.getSellerCCId() != null) {
        response.setCcId(listing.getSellerCCId());
      }
      if (listing.getSellerContactId() != null) {
        response.setContactId(listing.getSellerContactId());
      }
      if (listing.getSellerContactGuid() != null) {
          response.setContactGuid(listing.getSellerContactGuid());
       }
      if (listing.getSellerPaymentTypeId() != null) {
        response.setPaymentType(listing.getSellerPaymentTypeId().toString());
      }
      if (listing.getLmsApprovalStatus() != null) {
        response.setLmsApprovalStatus(listing.getLmsApprovalStatus());
      }
      if(listing.getConfirmOption() != null)
    	  response.setConfirmOptionId(listing.getConfirmOption());
      response.setSellerId(listing.getSellerId());
    }

    if (listing.isHideSeatInfoInd() != null && listing.isHideSeatInfoInd().booleanValue()) {
        response.setHideSeats(true);
    } else {
    	response.setHideSeats(false);
    }

    // SLAM-1801 auto-pricing services
    if (listing.isAutoPricingEnabledInd() != null && listing.isAutoPricingEnabledInd().booleanValue()) {
    	    response.setAutoPricingEnabledInd(true);
    } else {
    	    response.setAutoPricingEnabledInd(false);
    }

    if (faceValueRequired) {
      if (listing.getFaceValue() != null && listing.getFaceValue().getAmount() != null) {
        response.setFaceValue(listing.getFaceValue());
      }
    }

    if (listing.getSeatTraits() != null) {
      for (ListingSeatTrait seatTrait : listing.getSeatTraits()) {
        if (SEAT_TRAIT_PARTNER_TICKET == seatTrait.getSupplementSeatTraitId().longValue()) {
          response.setPrimaryTicket(Boolean.TRUE);
        }
      }
    }

    response.setTicketTraits(buildListingTraits(listing, eventV3));

    response.setStubhubMobileTicket(listing.getStubhubMobileTicket());
    if (listing.getTicketSeats() != null) {
      boolean isCSCall = (proxyRoleType != null && StringUtils.isNotBlank(operatorId));
      List<Product> products = new ArrayList<Product>();
      for (TicketSeat ticketSeat : listing.getTicketSeats()) {
        Product product = new Product();
        Boolean generalAdmission = ticketSeat.getGeneralAdmissionInd();

        if (generalAdmission != null && generalAdmission.booleanValue()) {
          product.setGa(true);
        } else {
          product.setGa(false);
        }
        if (CommonConstants.GENERAL_ADMISSION.equalsIgnoreCase(listing.getSection())) {
          product.setGa(true);
        }
        if (proxyRoleType != null || isSeller) {
          if (ticketSeat.getExternalSeatId() != null) {
            product.setExternalId(ticketSeat.getExternalSeatId());
          }
          product.setInventoryTypeId(ticketSeat.getInventoryTypeId());
          product.setSeatId(ticketSeat.getTicketSeatId());
        }
        product.setOperation(null);
        if (listing.isHideSeatInfoInd() != null) {
	        if (!listing.isHideSeatInfoInd().booleanValue() || isSeller || isCSCall) {
	            product.setSeat(ticketSeat.getSeatNumber());
	        }
        }
    	product.setRow(ticketSeat.getRow());
        if (ticketSeat.getTixListTypeId().longValue() == 2) {
          product.setProductType(ProductType.PARKING_PASS);
        } else if (ticketSeat.getTixListTypeId().longValue() == 1) {
          product.setProductType(ProductType.TICKET);
        }
        product.setSeatStatus(convertToSeatStatusString(ticketSeat.getSeatStatusId()));
        product.setFaceValue(ticketSeat.getFaceValue());
        product.setUniqueTicketNumber(ticketSeat.getUniqueTicketNumber());

        products.add(product);
      }
      response.setProducts(products);

    }

    if(listing.getPurchasePricePerProduct() != null) {
      response.setPurchasePricePerProduct(listing.getPurchasePricePerProduct());
      if (listing.getSalesTaxPaid() != null) {
        response.setSalesTaxPaid(listing.getSalesTaxPaid());
      }else{
        response.setSalesTaxPaid(true);
      }
    }
    //CSAPIS-2098
    if(listing.getCreatedBy() != null){
      response.setCreatedBy(listing.getCreatedBy());
    }

    response.setPricePerProduct(listing.getListPrice());
    response.setBuyerSeesPerProduct(listing.getDisplayPricePerTicket());
    response.setTicketMedium(listing.getTicketMedium());
    response.setEventId(listing.getEventId().toString());
    response.setSection(listing.getSection());
    response.setRows(listing.getRow());
    if (listing.getVenueConfigSectionsId() != null) {
      response.setVenueConfigSectionId(listing.getVenueConfigSectionsId());
    }
    response.setQuantity(listing.getQuantity());
    response.setQuantityRemain(listing.getQuantityRemain());
    response.setSplitOption(SplitOption.fromString(String.valueOf(listing.getSplitOption())));
    response.setSplitQuantity(listing.getSplitQuantity());
    if (listing.getBusinessGuid() != null) {
      response.setBusinessGUID(listing.getBusinessGuid());
    }
    //fix for SELLAPI-4059
    if (listing.getSellItNow()!=null && listing.getSellItNow()==1) {
    	response.setSnowInd(SNOWIND);
    }
    if (listing
        .getDeliveryOption() == com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.PREDELIVERY
            .getValue()) {
      response.setPreDelivered(true);
    } else {
      response.setPreDelivered(false);
    }
    if (!isSeller && deliveryMethodList != null && !deliveryMethodList.isEmpty()) {
      List<DeliveryMethod> deliveryMethods = new ArrayList<DeliveryMethod>();
      for (com.stubhub.domain.inventory.listings.v2.entity.DeliveryMethod deliveryMethodObj : deliveryMethodList) {
        DeliveryMethod deliveryMethod = new DeliveryMethod();
        deliveryMethod.setId(deliveryMethodObj.getId());
        deliveryMethod.setName(deliveryMethodObj.getName());
        deliveryMethod.setEstimatedDeliveryTime(TimeUtils
            .getDateFormatISO8601(deliveryMethodObj.getExpectedDeliveryDate().getExpectedDate()));
        deliveryMethods.add(deliveryMethod);
      }
      response.setDeliveryMethods(deliveryMethods);
    }
    response.setTicketClass(listing.getTicketClass());

    if (PENDING_PDF_REVIEW.equalsIgnoreCase(listing.getSystemStatus())
        || PENDING_LOCK.equalsIgnoreCase(listing.getSystemStatus())) {
      response.setStatus(ListingStatus.PENDING);
    } else {
      response.setStatus(ListingStatus.fromString(listing.getSystemStatus()));
    }

    response.setId(listing.getId().toString());
    response.setScrubbedSectionName(listing.getScrubbedSectionName());
    response.setListingSource(listing.getListingSource());

    if(listing.getCreatedDate() != null){
	    String timeZone = "UTC";
		response.setCreatedDate(convertToNewTimeZoneXMLCalendar(
					listing.getCreatedDate(), timeZone));
    }

    // convert listing ticket medium to response delivery option
    ticketMediumToDeliveryOption(listing, response);

    boolean isLiabilityWaiver=false;
    try {
      if (eventV3 != null) {
        response.setEventTimezone(eventV3.getTimezone());
        response.setEventDescription(eventV3.getName());
        response.setEventDate(eventV3.getEventDateLocal());
        response.setIsParkingPassOnlyEvent(isParkingPassEvent(eventV3));
        if (listing.getInhandDate() != null) {
          response.setInhandDate(
              convertToNewTimeZoneXMLCalendar(listing.getInhandDate(), eventV3.getTimezone()));
        }
        if (listing.getEndDate() != null && (proxyRoleType != null || isSeller)) {
          response.setSaleEndDate(
              convertToNewTimeZoneXMLCalendar(listing.getEndDate(), eventV3.getTimezone()));
        }
        //DISTCOMM-69
        if (eventV3.getVenue() !=null) {
          response.setVenueId(eventV3.getVenue().getId());
          response.setVenueConfigId(eventV3.getVenue().getConfigurationId());
        }
        isLiabilityWaiver = EventHelper.isLiabilityWaiver(eventV3);


      }

    } catch (Exception e) {
      log.error("unexpected parse exception, listingId=" + listing.getId() + " inhandDate="
          + listing.getInhandDate() + " endDate=" + listing.getEndDate(), e);
    }

    if (listing.getSaleMethod() != null)
      response.setSaleMethod(SaleMethod.getSaleMethod(listing.getSaleMethod()));

    response.setSplitVector(constructSplitVectorWithLiabilityWaiver(listing,isLiabilityWaiver));

    response.setSectionMappingRequired(sectionMappingRequired); //FAN-237 06/27/16

    response.setIsScrubbingEnabled(isScrubbingEnabled); //FAN-357 069/20


    if(StringUtils.trimToEmpty(listing.getCreatedBy()).contains(RELIST)) {
      response.setRelist(true);
    } else {
      response.setRelist(false);
    }

    return response;
  }

  private static TicketSeatStatusEnum convertToSeatStatusString(Long seatStatusId) {
    TicketSeatStatusEnum seatStatus = null;
    if (seatStatusId != null) {
      seatStatus = TicketSeatStatusEnum.getTicketSeatStatusEnumByCode(seatStatusId.intValue());

    }
    return seatStatus;
  }
  // private static String getTicketClassValue(Integer listingSourceId, String comments) {
  // if (listingSourceId != null && listingSourceId == 8
  // && !StringUtils.isEmpty(comments)) {
  // return comments;
  // }
  //
  // return null;
  // }

  /**
   * Convert listingTicketMedium back to response deliveryOption
   *
   * @param listing
   * @param response
   */
	public static void ticketMediumToDeliveryOption(Listing listing, ListingResponse response) {
		if (listing.getTicketMedium() != null
				&& TicketMedium.BARCODE.getValue() == listing.getTicketMedium().intValue()) {
			response.setDeliveryOption(DeliveryOption.BARCODE);
		} else if (listing.getTicketMedium() != null
				&& TicketMedium.PDF.getValue() == listing.getTicketMedium().intValue()) {
			response.setDeliveryOption(DeliveryOption.PDF);
		} else if (listing.getTicketMedium() != null
				&& (TicketMedium.FLASHSEAT.getValue() == listing.getTicketMedium().intValue() || TicketMedium.EXTFLASH.getValue() == listing.getTicketMedium().intValue())) {
			response.setDeliveryOption(DeliveryOption.FLASHSEAT);
		} else if (listing.getTicketMedium() != null
				&& TicketMedium.MOBILE.getValue() == listing.getTicketMedium().intValue()) {
			response.setDeliveryOption(DeliveryOption.MOBILE);
		} else if (listing.getTicketMedium() != null
				&& TicketMedium.EXTMOBILE.getValue() == listing.getTicketMedium().intValue()) {
			response.setDeliveryOption(DeliveryOption.MOBILETRANSFER);
		} else if (listing.getFulfillmentDeliveryMethods() != null) {
			String fmDMList = listing.getFulfillmentDeliveryMethods();
			if (fmDMList.contains("|11,") || fmDMList.contains("|12,") || fmDMList.contains("|15,") || fmDMList.startsWith("11,")
					|| fmDMList.startsWith("12,") || fmDMList.startsWith("15,")) {
				response.setDeliveryOption(DeliveryOption.SHIPPING);
			} else if (fmDMList.contains("|10,") || fmDMList.startsWith("10,")) {
				response.setDeliveryOption(DeliveryOption.UPS);
			} else if (fmDMList.contains("|9,") || fmDMList.contains("|7,") || fmDMList.startsWith("9,")
					|| fmDMList.startsWith("7,")) {
				response.setDeliveryOption(DeliveryOption.LMS);
			} else if (fmDMList.contains("|17,") || fmDMList.startsWith("17,")) {
				response.setDeliveryOption(DeliveryOption.LOCALDELIVERY);
			} else if (fmDMList.contains("|8,") || fmDMList.startsWith("8,")) {
				response.setDeliveryOption(DeliveryOption.WILLCALL);
			}

		}
	}

  /**
   * Convert to new timezone XML Calendar.
   *
   * @param pCal
   * @param timeZoneStr
   * @return XMLGregorianCalendar
   */
  private static String convertToNewTimeZoneXMLCalendar(Calendar pCal, String timeZoneStr) {
    Calendar cal = pCal;
    cal = (GregorianCalendar) DateUtil.convertCalendarToNewTimeZone(cal,
        TimeZone.getTimeZone(timeZoneStr));
    cal.setTimeZone(TimeZone.getTimeZone(timeZoneStr));
    SimpleDateFormat sf = new SimpleDateFormat(dateFormat);
    sf.setTimeZone(TimeZone.getTimeZone(timeZoneStr));
    return sf.format(cal.getTime());

  }

  /**
   * Get response and add some optional data (such as deliveryOption)
   *
   * @param listing
   * @param isCreate
   * @return
   */
  public static ListingResponse getListingRespWithData(Listing listing, boolean isCreate) {
    ListingResponse resp = getListingResponse(listing);
    if (isCreate) {
      ticketMediumToDeliveryOption(listing, resp);
    }
    if (listing.isPriceAdjusted()) {
      resp.setPricePerProduct(listing.getListPrice());
    }
    if (listing.isInHandDateAdjusted()) {
    	StringBuilder ihDate = new StringBuilder();
		ihDate.append(listing.getInhandDate().get(Calendar.MONTH)+1).append("/").append(listing.getInhandDate().get(Calendar.DAY_OF_MONTH)).append("/").append(listing.getInhandDate().get(Calendar.YEAR));
        resp.setInhandDate(ihDate.toString());
      }
    if (listing.isSaleEndDateAdjusted()) {
    	StringBuilder saleEndDate = new StringBuilder();
    	saleEndDate.append(listing.getEndDate().get(Calendar.MONTH)+1).append("/").append(listing.getEndDate().get(Calendar.DAY_OF_MONTH)).append("/").append(listing.getEndDate().get(Calendar.YEAR));
        resp.setSaleEndDate(saleEndDate.toString());
      }
    return resp;
  }

  /**
   * Get simple listing response
   *
   * @param listing
   * @return
   */
  public static ListingResponse getListingResponse(Listing listing) {
    ListingResponse response = new ListingResponse();
    if (listing != null && listing.getId() != null) {
      response.setId(listing.getId().toString());
      response.setExternalListingId(listing.getExternalId());
      //This is added for Fraud Evaluation And set it back to null before committing the response.
      if(listing.getEventId()!=null) {
    	  response.setEventId(Long.toString(listing.getEventId()));
      }
      response.setSellerId(listing.getSellerId());
      if (listing.getSystemStatus() != null) {
        if ("PENDING PDF REVIEW".equalsIgnoreCase(listing.getSystemStatus())
            || "PENDING LOCK".equalsIgnoreCase(listing.getSystemStatus())) {
          response.setStatus(ListingStatus.PENDING);
        } else {
          response.setStatus(ListingStatus.fromString(listing.getSystemStatus()));
        }
      }
    }
    return response;
  }

  public static void errorMappingThrowException(ListingBusinessException listingException) {
    RuntimeException ex = getErrorMapping(listingException);
    throw ex;
  }

  public static RuntimeException getErrorMapping(ListingBusinessException listingException) {
    if (listingException.getListingError().getType().equals(ErrorType.BUSINESSERROR)
        || listingException.getListingError().getType().equals(ErrorType.INPUTERROR)) {
      SHBadRequestException sbe = new SHBadRequestException(listingException);
      sbe.setErrorCode(
          ErrorUtils.getFormattedErrorCode(listingException.getListingError().getCode()));
      sbe.setDescription(listingException.getListingError().getMessage());
      if (listingException.getListingError().getParameter() != null) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("parameter", listingException.getListingError().getParameter());
      }
      return sbe;
    } else if (listingException.getListingError().getType().equals(ErrorType.AUTHENTICATIONERROR)
        || listingException.getListingError().getType().equals(ErrorType.AUTHORIZATIONERROR)) {
      SHForbiddenException sbe = new SHForbiddenException(listingException);
      sbe.setErrorCode(
          ErrorUtils.getFormattedErrorCode(listingException.getListingError().getCode()));
      sbe.setDescription(listingException.getListingError().getMessage());
      if (listingException.getListingError().getParameter() != null) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("parameter", listingException.getListingError().getParameter());
      }
      return sbe;
    } else if (listingException.getListingError().getType().equals(ErrorType.NOT_FOUND)) {
      SHResourceNotFoundException sbe = new SHResourceNotFoundException(listingException);
      sbe.setErrorCode(
          ErrorUtils.getFormattedErrorCode(listingException.getListingError().getCode()));
      sbe.setDescription(listingException.getListingError().getMessage());
      if (listingException.getListingError().getParameter() != null) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("parameter", listingException.getListingError().getParameter());
      }
      throw sbe;
    } else {
      throw new SHSystemException("System error, please re-try",listingException);
    }
  }


  /**
   * Utility method to build the splitVector based on the splitOption and the splitQuantity
   * mentioned by the seller
   *
   * @param listing
   * @return
   */

  public static String constructSplitVectorWithLiabilityWaiver(com.stubhub.domain.inventory.datamodel.entity.Listing listing, boolean isLiabilityWaiver) {
    String splitVector = constructSplitVector(listing);
    if (isLiabilityWaiver) {
      log.info("before LiabilityWaiver ticket_id={} split_vector={}", listing.getId(), splitVector);
      if (splitVector.contains(",")) {
        splitVector = splitVector.substring(splitVector.lastIndexOf(",") + 1);
      }
      log.info("after LiabilityWaiver ticket_id={} split_vector={}", listing.getId(), splitVector);
    }

    return splitVector;
  }

  public static String constructSplitVector(
      com.stubhub.domain.inventory.datamodel.entity.Listing listing) {
    String splitVector = "";
    if(listing.getQuantityRemain() == 0) {
      return splitVector;
    }

    StringBuilder splitVectorSB = new StringBuilder();
    Short allQuantityExceptOne = 2;
    Short splitOption = listing.getSplitOption();
    if (splitOption != null) {
      if (allQuantityExceptOne.equals(splitOption)) {
        for (int i = 1; i <= listing.getQuantityRemain().intValue(); i++) {
          if (i != (listing.getQuantityRemain().intValue() - 1)) {
            splitVectorSB.append(i).append(",");
          }
        }
        splitVector = splitVectorSB.toString();
        if (!"".equals(splitVector)) {
          return splitVector.substring(0, splitVector.lastIndexOf(","));
        } else {
          return splitVector;
        }
      }
    }

    int remainder = 0;
    int split = 0;
    if (listing.getSplitQuantity() != null && listing.getSplitQuantity() > 0) {
      remainder = listing.getQuantityRemain().intValue() % listing.getSplitQuantity().intValue();
      split = listing.getSplitQuantity().intValue();
    } else {
      split = listing.getQuantityRemain().intValue();
    }

    for (int i = 1; i <= listing.getQuantityRemain(); i++) {
      if (split == 1) {
        splitVectorSB.append(i).append(",");
      } else if (listing.getQuantityRemain().intValue() - i != 1 && i % split == 0) {
        splitVectorSB.append(i).append(",");
      } else if (listing.getQuantityRemain().intValue() - i != 1 && (i - remainder) % split == 0) {
        splitVectorSB.append(i).append(",");
      }
    }
    splitVector = splitVectorSB.toString();
    if (!"".equals(splitVector)) {
      return splitVector.substring(0, splitVector.lastIndexOf(","));
    } else {
      return splitVector;
    }
  }



  private static Boolean isParkingPassEvent(Event event) {
    if (event.getEventAttributes() != null
        && PARKING.equalsIgnoreCase(event.getEventAttributes().getEventType())) {
      return Boolean.TRUE;
    }

    return Boolean.FALSE;
  }


  /**
   * Returns active ticketseats from a listing
   *
   * @param listing
   * @return
   */
  private List<TicketSeat> getAllActiveTicketSeats(Listing listing) {
    List<TicketSeat> ticketSeats = listing.getTicketSeats();
    // activeTicketSeats are modeled as list so that the seat ids are in sequence
    List<TicketSeat> activeTicketSeats = new ArrayList<TicketSeat>();
    for (TicketSeat ticketSeat : ticketSeats) {
      if (TicketSeatStatusEnum.AVAILABLE == TicketSeatStatusEnum
          .getTicketSeatStatusEnumByCode(ticketSeat.getSeatStatusId().intValue())) {
        activeTicketSeats.add(ticketSeat);
      }
    }
    log.info("_message=\"listingId={} ticketSeats={} activeTicketSeats={}\"", listing.getId(),
        ticketSeats.size(), activeTicketSeats.size());
    return activeTicketSeats;
  }


  /**
   * Returns active ticketseatIds from a listing
   *
   * @param listing
   * @return
   */
  private List<Long> getAllActiveTicketSeatIds(Listing listing) {
    List<TicketSeat> ticketSeats = listing.getTicketSeats();
    // activeTicketSeats are modeled as list so that the seat ids are in sequence
    List<Long> activeTicketSeats = new ArrayList<Long>();
    for (TicketSeat ticketSeat : ticketSeats) {
      if (TicketSeatStatusEnum.AVAILABLE == TicketSeatStatusEnum
          .getTicketSeatStatusEnumByCode(ticketSeat.getSeatStatusId().intValue())) {
        activeTicketSeats.add(ticketSeat.getTicketSeatId());
      }
    }
    log.info("_message=\"listingId={} ticketSeats={} activeTicketSeatIds={}\"", listing.getId(),
        ticketSeats.size(), activeTicketSeats.size());
    return activeTicketSeats;
  }


  /**
   * Build product from a listing and a given ticketseat
   *
   * @param listing
   * @param ticketSeat
   * @return a product
   */
  private ProductDetail buildProductDetail(Listing listing, TicketSeat ticketSeat) {
    ProductDetail product = new ProductDetail();
    product.setProductId(ticketSeat.getTicketSeatId());
    product.setSection(ticketSeat.getSection());
    product.setRow(ticketSeat.getRow());
    product.setSeat(ticketSeat.getSeatNumber());
    product.setProductStatus(ProductAvailabilityStatusEnum.AVAILABLE.toString());
    product.setFaceValue(ticketSeat.getFaceValue());
    product.setUniqueTicketNumber(ticketSeat.getUniqueTicketNumber());
    product.setInventoryTypeId(ticketSeat.getInventoryTypeId());
    product.setExternalId(ticketSeat.getExternalSeatId());
    // general admission
    Boolean generalAdmission = ticketSeat.getGeneralAdmissionInd();
    if ((null != generalAdmission && generalAdmission.booleanValue())
        || CommonConstants.GENERAL_ADMISSION.equalsIgnoreCase(listing.getSection())) {
      product.setGa(true);
    } else {
      product.setGa(false);
    }

    // product type
    if (ticketSeat.getTixListTypeId() == 2L) {
      product.setProductType(ProductType.PARKING_PASS.toString());
    } else if (ticketSeat.getTixListTypeId() == 1L) {
      product.setProductType(ProductType.TICKET.toString());
    }

    // product medium
    if (null != listing.getTicketMedium()
        && null != ProductMediumEnum.getProductMediumEnumByCode(listing.getTicketMedium())) {
      product.setMedium(
          ProductMediumEnum.getProductMediumEnumByCode(listing.getTicketMedium()).toString());
    }

    // seat traits
    return product;
  }


  /**
   * Builds traits of a given listing
   *
   * @param listing
   * @return a set of traits
   */
  private static Set<TicketTrait> buildListingTraits(Listing listing, Event event) {
    Set<TicketTrait> ticketTraits = null;
    if (event != null && event.getSeatTraits() != null ) {
      ticketTraits = new HashSet<TicketTrait>();
      if(null != listing.getSeatTraits()){
	      for (ListingSeatTrait listingTrait : listing.getSeatTraits()) {
	        for (SeatTrait eventTrait : event.getSeatTraits()) {
	          if (eventTrait.getId().equals(listingTrait.getSupplementSeatTraitId())) {
	            TicketTrait trait = new TicketTrait();
	            trait.setId(eventTrait.getId().toString());
	            trait.setName(eventTrait.getName());
	            trait.setType(eventTrait.getType());
	            trait.setCategoryId(eventTrait.getCategoryId());
	            trait.setCategoryName(eventTrait.getCategory());
	            trait.setOperation(null);
	            ticketTraits.add(trait);
	          }
	        }
	      }
    	}

      for (SeatTrait eventTrait : event.getSeatTraits()) {
      	if (eventTrait.getType() != null && EVENT_RESTRICTIONS.equalsIgnoreCase(eventTrait.getType())) {
            TicketTrait trait = new TicketTrait();
            trait.setId(eventTrait.getId().toString());
            trait.setName(eventTrait.getName());
            trait.setType(eventTrait.getType());
            trait.setCategoryId(eventTrait.getCategoryId());
            trait.setCategoryName(eventTrait.getCategory());
            trait.setOperation(null);
            ticketTraits.add(trait);
      	}

      }

    }

    return ticketTraits;
  }
}
