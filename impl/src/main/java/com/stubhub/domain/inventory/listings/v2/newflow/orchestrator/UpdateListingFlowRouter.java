package com.stubhub.domain.inventory.listings.v2.newflow.orchestrator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.entity.SplitOption;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.EventInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ProductInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.UpdateListingInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.UpdateListingEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.handler.BusinessFlowHandler;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.EventHelper;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.newplatform.common.util.DateUtil;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;

@Component("updateListingFlowRouter")
public class UpdateListingFlowRouter implements BusinessFlowRouter {

  private final static Logger log = LoggerFactory.getLogger(UpdateListingFlowRouter.class);

  @Autowired
  private InventoryMgr inventoryMgr;

  @Autowired
  private ListingSeatTraitMgr listingSeatTraitMgr;

  @Autowired
  private BeanFactory beanFactory;

  @Autowired
  private MasterStubhubPropertiesWrapper masterStubhubProperties;

  @Autowired
  private TicketSeatMgr ticketSeatMgr;

  @Autowired
  private EventHelper eventHelper;

  @Override
  public BusinessFlowHandler getBusinessFlowHandler(ListingDTO listingDTO) {
    ListingRequest request = listingDTO.getListingRequest();
    Listing dbListing = inventoryMgr.getListing(request.getListingId());

    // Validate DB Listing
    validateDbListing(dbListing, listingDTO.getSellerInfo().getSellerId(),listingDTO.getListingRequest().getStatus());
    listingDTO.setDbListing(dbListing);

    List<UpdateListingEnum> updateAttributeList = new ArrayList<UpdateListingEnum>();
    if (request.getContactId() != null
        && !request.getContactId().equals(dbListing.getSellerContactId())) {
      updateAttributeList.add(UpdateListingEnum.CONTACT_ID);
    }
    
    if (request.getContactGuid() != null
        && !request.getContactGuid().equals(dbListing.getSellerContactGuid())) {
      updateAttributeList.add(UpdateListingEnum.CONTACT_GUID);
    }

    if (request.getPaymentType() != null
        && !request.getPaymentType().equals(dbListing.getSellerPaymentTypeId())) {
      updateAttributeList.add(UpdateListingEnum.PAYMENT_TYPE);
    }

    if (request.getCcId() != null) {
      updateAttributeList.add(UpdateListingEnum.CC_ID);
    }

    if (request.getTicketMedium() != null
			&& request.getTicketMedium().getId() != dbListing.getTicketMedium().intValue())
		updateAttributeList.add(UpdateListingEnum.DELIVERY_OPTION);
	else if (request.getTicketMedium() == null && request.getDeliveryOption() != null) {
		DeliveryOption deliveryOption = ticketMediumToDeliveryOption(dbListing);
		if (deliveryOption == null || !deliveryOption.equals(request.getDeliveryOption())) {
			updateAttributeList.add(UpdateListingEnum.DELIVERY_OPTION);
		}
	}

    // TODO: Revisit this
    if (request.getSaleEndDate() != null) {
      updateAttributeList.add(UpdateListingEnum.SALE_END_DATE);
    }

    if (request.getInhandDate() != null) {
      if (dbListing
          .getDeliveryOption() == com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.MANUAL
              .getValue()) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar inhandDate = DateUtil.getTodayCalUTC();
        try {
          inhandDate.setTime(sdf.parse(request.getInhandDate()));
          if (!DateUtil.calendarsEqualToDate(inhandDate, dbListing.getInhandDate())) {
            updateAttributeList.add(UpdateListingEnum.INHAND_DATE);
          }
        } catch (ParseException e) {
          log.error("message=\"Invalid date format in inhandDate\" inhandDate={} listingId={}",
              request.getInhandDate(), request.getListingId());
          throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidDateFormat);
        }
      }
      setSellerContact(listingDTO);
      setEventInfo(listingDTO, true);
    }

    if (Boolean.TRUE.equals(request.getLmsExtension())) {
      updateAttributeList.add(UpdateListingEnum.LMS_EXTENSION);
    }

    if (request.getLmsApprovalStatus() != null
        && !request.getLmsApprovalStatus().equals(dbListing.getLmsApprovalStatus())) {
      updateAttributeList.add(UpdateListingEnum.LMS_APPROVAL_STATUS);
    }

    if (request.getSection() != null && !request.getSection().equals(dbListing.getSection())) {
      updateAttributeList.add(UpdateListingEnum.SECTION);
    }

    if (request.getProducts() != null && !request.getProducts().isEmpty()) {
      boolean isPredelivery = false;
      for (Product product : request.getProducts()) {
        if (Operation.UPDATE.equalsEnum(product.getOperation())
            && StringUtils.trimToNull(product.getFulfillmentArtifact()) != null) {
          isPredelivery = true;
          break;
        }
      }
      if (isPredelivery) {
        if (dbListing.getTicketMedium().equals(TicketMedium.BARCODE.getValue())) {
          updateAttributeList.add(UpdateListingEnum.PREDELIVERY_BARCODE);
        } else if (dbListing.getTicketMedium().equals(TicketMedium.PDF.getValue())) {
          updateAttributeList.add(UpdateListingEnum.PREDELIVERY_PDF);
        } else {
          updateAttributeList.add(UpdateListingEnum.PREDELIVERY);
        }
      } else {
        // populate product info
        populateProductInfo(listingDTO);
        ProductInfo productInfo = listingDTO.getUpdateListingInfo().getProductInfo();
        setSeatsAndTraits(dbListing);
        if (productInfo.isAddProduct()) {
          updateAttributeList.add(UpdateListingEnum.ADD_SEATS);
          setEventInfo(listingDTO, true);
        }
        if (productInfo.isUpdateProduct()) {
          updateAttributeList.add(UpdateListingEnum.UPDATE_SEATS);
          setEventInfo(listingDTO, false);
        }
        if (productInfo.isDeleteProduct()) {
          boolean hasParkingPass = false;
          for(Product product : productInfo.getDeleteProducts()) {
            if(ProductType.PARKING_PASS.equalsEnum(product.getProductType())) {
              hasParkingPass = true;
              break;
            }
          }
          if(productInfo.getDeleteProducts().size() == dbListing.getQuantityRemain() && !productInfo.isAddProduct() && !hasParkingPass) {
            // Delete the listing if the user is trying to delete all the seats
            log.info("message=\"Listing will  be deleted as the user is trying to delete all the seats\" listingId={}", dbListing.getId());
            updateAttributeList.add(UpdateListingEnum.DELETE_LISTING);
          } else {
            updateAttributeList.add(UpdateListingEnum.DELETE_SEATS);
          } 
        }
      }
    }

    if (request.getQuantity() != null
        && !request.getQuantity().equals(dbListing.getQuantityRemain())) {
      updateAttributeList.add(UpdateListingEnum.QUANTITY);
    }

    if (request.getTicketTraits() != null && !request.getTicketTraits().isEmpty()) {
      setEventInfo(listingDTO, true);
      setSeatsAndTraits(dbListing);
      updateAttributeList.add(UpdateListingEnum.TICKET_TRAITS);
    }

    if (StringUtils.trimToNull(request.getInternalNotes()) != null
        || StringUtils.trimToNull(request.getComments()) != null) {
      updateAttributeList.add(UpdateListingEnum.COMMENTS);
    }

    if (request.getSplitOption() != null) {
      SplitOption dbSplitOption = SplitOption.fromString(dbListing.getSplitOption().toString());
      if (!request.getSplitOption().equals(dbSplitOption)) {
        setSeatsAndTraits(dbListing);
        updateAttributeList.add(UpdateListingEnum.SPLIT_OPTION);
      }
    }

    if (dbListing.getSplitOption() != null && dbListing.getSplitOption() == 1) {
      if (request.getSplitQuantity() != null
          && !request.getSplitQuantity().equals(dbListing.getSplitQuantity())) {
        updateAttributeList.add(UpdateListingEnum.SPLIT_QUANTITY);
      }
    }

    if (request.isHideSeats() != null
        && !request.isHideSeats().equals(dbListing.isHideSeatInfoInd())) {
      updateAttributeList.add(UpdateListingEnum.HIDESEATS_INDICATOR);
      if (request.isHideSeats()) {
        setEventInfo(listingDTO, false);
      }
    }
    
    if (request.isAutoPricingEnabledInd() != null
        && !request.isAutoPricingEnabledInd().equals(dbListing.isAutoPricingEnabledInd())) {
      updateAttributeList.add(UpdateListingEnum.AUTO_PRICING_ENABLED_IND);
    }
    
    if (request.getFaceValue() != null && request.getFaceValue().getAmount() != null) {
      if (dbListing.getFaceValue() == null || dbListing.getFaceValue().getAmount() == null
          || request.getFaceValue().getAmount()
              .compareTo(dbListing.getFaceValue().getAmount()) != 0) {
        updateAttributeList.add(UpdateListingEnum.FACE_VALUE);
        setEventInfo(listingDTO, false);
        setSeatsAndTraits(dbListing);
      }
    }
    
    if (request.getPayoutPerProduct() != null && (request.getPayoutPerProduct().getAmount()
        .compareTo(dbListing.getSellerPayoutAmountPerTicket().getAmount()) != 0)) {
      updateAttributeList.add(UpdateListingEnum.PRICE);
    } else if (request.getPricePerProduct() != null && (request.getPricePerProduct().getAmount()
        .compareTo(dbListing.getListPrice().getAmount()) != 0)) {
      updateAttributeList.add(UpdateListingEnum.PRICE);
    } else if (request.getBuyerSeesPerProduct() != null && (request.getBuyerSeesPerProduct()
        .getAmount().compareTo(dbListing.getDisplayPricePerTicket().getAmount()) != 0)) {
      updateAttributeList.add(UpdateListingEnum.PRICE);
    }

    if (request.getPurchasePricePerProduct() != null && request.getPurchasePricePerProduct().getAmount() != null){
      if(dbListing.getPurchasePricePerProduct() == null || dbListing.getPurchasePricePerProduct().getAmount() == null || request.getPurchasePricePerProduct().getAmount().compareTo(dbListing.getPurchasePricePerProduct().getAmount()) != 0)
      updateAttributeList.add(UpdateListingEnum.PURCHASE_PRICE);
    }

    if(request.getSalesTaxPaid() != null){
       if(dbListing.getSalesTaxPaid() == null || !request.getSalesTaxPaid().equals(dbListing.getSalesTaxPaid()))
      updateAttributeList.add(UpdateListingEnum.SALES_TAX_PAID);
    }

    if (request.getStatus() != null
        && !request.getStatus().name().equalsIgnoreCase(dbListing.getSystemStatus())) {
      if (request.getStatus().equals(ListingStatus.DELETED)) {
        updateAttributeList.add(UpdateListingEnum.DELETE_LISTING);
      } else {
        updateAttributeList.add(UpdateListingEnum.STATUS);
      }
    }

    // Except for DELETE listing request, check if listing is already
    // expired
    if (!updateAttributeList.contains(UpdateListingEnum.DELETE_LISTING)) {
      validateDbListingForExpiration(dbListing);
    }
    validateUpdate(dbListing, updateAttributeList);
    
    log.info(
        "message=\"Update listing - identified update list\" listingId={} updateAttributeList={}",
        dbListing.getId(), updateAttributeList);

    // Check if the new flow is enabled for identified updates
    if (!isNewFlowEnabled(updateAttributeList))
      return null;

    // Sort the list in the enum sequence order
    Collections.sort(updateAttributeList);

    return (BusinessFlowHandler) beanFactory.getBean("updateListingHandler", listingDTO,
        updateAttributeList);

  }

  // Checks for all released features; returns false if all the features are
  // not enabled for new flow
  private boolean isNewFlowEnabled(List<UpdateListingEnum> updateAttributeList) {
    List<UpdateListingEnum> availableAttributeList = new ArrayList<UpdateListingEnum>();

    // Release 1 attributes
    if (isRelease1Enabled()) {
      availableAttributeList.addAll(releaseAttributesMap.get("Release1"));
    }

    // Release 2 attributes
    if (isRelease2Enabled()) {
      availableAttributeList.addAll(releaseAttributesMap.get("Release2"));
    }
    
    // Release 3 attributes
    if (isRelease3Enabled()) {
      availableAttributeList.addAll(releaseAttributesMap.get("Release3"));
    }
    
    // Release 4 attributes
    if (isRelease4Enabled()) {
      availableAttributeList.addAll(releaseAttributesMap.get("Release4"));
    }

    // If no updates identified, return false
    if (updateAttributeList.isEmpty())
      return true;

    // Check if they are all available
    for (UpdateListingEnum updateListingEnum : updateAttributeList) {
      if (!availableAttributeList.contains(updateListingEnum)) {
        return false;
      }
    }

    return true;
  }

  private boolean isRelease1Enabled() {
    String updateListingRelease1Switch = getNewFlowRelease1Property();
    log.info("updateListingRelease1Switch={}", updateListingRelease1Switch);

    if ("true".equalsIgnoreCase(updateListingRelease1Switch)) {
      return true;
    }

    return false;
  }

  private boolean isRelease2Enabled() {
    String updateListingRelease2Switch = getNewFlowRelease2Property();
    log.info("updateListingRelease2Switch={}", updateListingRelease2Switch);

    if ("true".equalsIgnoreCase(updateListingRelease2Switch)) {
      return true;
    }

    return false;
  }

  private boolean isRelease3Enabled() {
    String updateListingRelease3Switch = getNewFlowRelease3Property();
    log.info("updateListingRelease3Switch={}", updateListingRelease3Switch);

    if ("true".equalsIgnoreCase(updateListingRelease3Switch)) {
      return true;
    }

    return false;
  }
  
  private boolean isRelease4Enabled() {
    String updateListingRelease4Switch = getNewFlowRelease4Property();
    log.info("updateListingRelease4Switch={}", updateListingRelease4Switch);

    if ("true".equalsIgnoreCase(updateListingRelease4Switch)) {
      return true;
    }

    return false;
  }

  private String getNewFlowRelease1Property() {
    return masterStubhubProperties.getProperty("inventory.newflow.update.listing.release1",
        "false");
  }

  private String getNewFlowRelease2Property() {
    return masterStubhubProperties.getProperty("inventory.newflow.update.listing.release2",
        "false");
  }

  private String getNewFlowRelease3Property() {
    return masterStubhubProperties.getProperty("inventory.newflow.update.listing.release3",
        "false");
  }
  
  private String getNewFlowRelease4Property() {
    return masterStubhubProperties.getProperty("inventory.newflow.update.listing.release4",
        "false");
  }

  private void validateDbListing(Listing dbListing, Long sellerId, ListingStatus status ) {
    if (dbListing == null) {
      log.error("message=\"No listing was found\"");
      throw new ListingException(ErrorType.NOT_FOUND, ErrorCodeEnum.listingNotFound);
    } else if ((!dbListing.getSellerId().equals(sellerId))) {
      log.error("message=\"User other than the seller trying to update the listing\" listingId={}",
          dbListing.getId());
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingActionNotallowed,
          "Listing does not belong to seller");
    } else if (ListingStatus.DELETED.toString().equals(dbListing.getSystemStatus())) {
      if (!(status!=null && ListingStatus.DELETED.toString().equalsIgnoreCase(status.toString()))) {
        log.warn("message=\"Listing is DELETED\" listingId={}", dbListing.getId());
        throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingNotActive);
      }
    } else if (dbListing.getQuantityRemain() == 0) {
      log.error("message=\"Listing has been sold already\" listingId={}", dbListing.getId());
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingAlreadySold);
    }
  }

  private void validateDbListingForExpiration(Listing dbListing) {
    if (dbListing.getEndDate() != null && dbListing.getEndDate().before(DateUtil.getNowCalUTC())) {
      log.error("Listing has expired listingId={}", dbListing.getId());
      throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingExpired);
    }
  }

  private static Map<String, List<UpdateListingEnum>> releaseAttributesMap;
  static {
    List<UpdateListingEnum> release1AttributesList = new ArrayList<UpdateListingEnum>();
    release1AttributesList.add(UpdateListingEnum.QUANTITY);
    release1AttributesList.add(UpdateListingEnum.SPLIT_OPTION);
    release1AttributesList.add(UpdateListingEnum.SPLIT_QUANTITY);
    release1AttributesList.add(UpdateListingEnum.PRICE);

    List<UpdateListingEnum> release2AttributesList = new ArrayList<UpdateListingEnum>();
    release2AttributesList.add(UpdateListingEnum.HIDESEATS_INDICATOR);
    release2AttributesList.add(UpdateListingEnum.TICKET_TRAITS);
    release2AttributesList.add(UpdateListingEnum.STATUS);
    release2AttributesList.add(UpdateListingEnum.DELETE_LISTING);

    List<UpdateListingEnum> release3AttributesList = new ArrayList<UpdateListingEnum>();
    release3AttributesList.add(UpdateListingEnum.INHAND_DATE);
    release3AttributesList.add(UpdateListingEnum.ADD_SEATS);
    release3AttributesList.add(UpdateListingEnum.UPDATE_SEATS);
    release3AttributesList.add(UpdateListingEnum.DELETE_SEATS);
    
    List<UpdateListingEnum> release4AttributesList = new ArrayList<UpdateListingEnum>();
    release4AttributesList.add(UpdateListingEnum.FACE_VALUE);

    releaseAttributesMap = new HashMap<String, List<UpdateListingEnum>>();
    releaseAttributesMap.put("Release1", release1AttributesList);
    releaseAttributesMap.put("Release2", release2AttributesList);
    releaseAttributesMap.put("Release3", release3AttributesList);
    releaseAttributesMap.put("Release4", release4AttributesList);
  }

  private DeliveryOption ticketMediumToDeliveryOption(Listing listing) {
    if (listing.getTicketMedium() != null) {
      if (TicketMedium.BARCODE.getValue() == (listing.getTicketMedium().intValue())) {
        return DeliveryOption.BARCODE;
      } else if (TicketMedium.PDF.getValue() == (listing.getTicketMedium().intValue())) {
        return DeliveryOption.PDF;
      } else if (TicketMedium.FLASHSEAT.getValue() == (listing.getTicketMedium().intValue()) || TicketMedium.EXTFLASH.getValue() == listing.getTicketMedium().intValue()) {
        return DeliveryOption.FLASHSEAT;
      } else if (TicketMedium.MOBILE.getValue() == (listing.getTicketMedium().intValue())) {
          return DeliveryOption.MOBILE;
      } else if (TicketMedium.EXTMOBILE.getValue() == (listing.getTicketMedium().intValue())) {
          return DeliveryOption.MOBILETRANSFER;
      }
    }
    if (listing.getFulfillmentDeliveryMethods() != null) {
      String fmDMList = listing.getFulfillmentDeliveryMethods();
      if (fmDMList.contains("|10,") || fmDMList.startsWith("10,")) {
        return DeliveryOption.UPS;
      } else if (fmDMList.contains("|11,") || fmDMList.contains("|12,") || fmDMList.contains("|15,")
          || fmDMList.startsWith("11,") || fmDMList.startsWith("12,") || fmDMList.startsWith("15,")) {
        return DeliveryOption.SHIPPING;
      } else if (fmDMList.contains("|9,") || fmDMList.contains("|7,") || fmDMList.startsWith("9,")
          || fmDMList.startsWith("7,")) {
        return DeliveryOption.LMS;
      } else if (fmDMList.contains("|17,") || fmDMList.startsWith("17,")) {
          return DeliveryOption.LOCALDELIVERY;
      } else if (fmDMList.contains("|8,") || fmDMList.startsWith("8,")) {
    	  return DeliveryOption.WILLCALL;
      }
    }
    return null;
  }

  private void populateProductInfo(ListingDTO listingDTO) {
    List<Product> products = listingDTO.getListingRequest().getProducts();

    if (products != null && !products.isEmpty()) {
      UpdateListingInfo updateListingInfo = new UpdateListingInfo();
      ProductInfo productInfo = new ProductInfo();
      List<Product> addList = new ArrayList<Product>();
      List<Product> updateList = new ArrayList<Product>();
      List<Product> deleteList = new ArrayList<Product>();
      for (Product product : products) {
        if (Operation.ADD.equals(product.getOperation())) {
          addList.add(product);
        } else if (Operation.UPDATE.equals(product.getOperation())) {
          updateList.add(product);
        } else if (Operation.DELETE.equals(product.getOperation())) {
          deleteList.add(product);
        }
      }
      productInfo.setAddProducts(addList);
      productInfo.setUpdateProducts(updateList);
      productInfo.setDeleteProducts(deleteList);
      updateListingInfo.setProductInfo(productInfo);
      listingDTO.setUpdateListingInfo(updateListingInfo);

      // validate the products
      validateProducts(listingDTO);
    }
  }

  private void setSeatsAndTraits(Listing listing) {
    if (listing.getTicketSeats() == null) {
      listing.setTicketSeats(ticketSeatMgr.findTicketSeatsByTicketId(listing.getId()));
    }
    if (listing.getSeatTraits() == null) {
      listing.setSeatTraits(listingSeatTraitMgr.findSeatTraits(listing.getId()));
    }
  }

  private void setEventInfo(ListingDTO listingDTO, boolean isSeatTraits) {
    Listing listing = listingDTO.getDbListing();
    if (listing.getEvent() == null) {
      EventInfo eventInfo = new EventInfo();
      com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event eventV3 =
          eventHelper.getEvent(listing.getEventId(), isSeatTraits);
      if (eventV3 == null) {
        log.error("message=\"Event ID is invalid OR not active\" listingId={} eventId={}",
            listing.getId(), listing.getEventId());
        throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.invalidEventid,
            "Event ID is invalid");
      }
      Event event = eventHelper.convert(eventV3);
      listing.setEvent(event);
      eventInfo.setEventId(event.getId());
      eventInfo.setEventDate(event.getEventDate());
      eventInfo.setTimeZone(event.getJdkTimeZone());
      listingDTO.setEventInfo(eventInfo);
    }

  }

  private void validateProducts(ListingDTO listingDTO) {
    Listing dbListing = listingDTO.getDbListing();
    if (listingDTO.getUpdateListingInfo() != null
        && listingDTO.getUpdateListingInfo().getProductInfo() != null) {
      ProductInfo productInfo = listingDTO.getUpdateListingInfo().getProductInfo();
      if (com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.PREDELIVERY
          .getValue() == dbListing.getDeliveryOption() && productInfo.isAddProduct()
          && productInfo.isDeleteProduct()) {
        log.error("message=\"Cannot add and delete seats in the same request\" listingId={}",
            dbListing.getId());
        throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingActionNotallowed,
            "Cannot add and delete seats in the same request");
      }
    }
  }

  private void setSellerContact(ListingDTO listingDTO) {
    if (listingDTO.getSellerInfo().getSellerContactId() == null) {
      Listing dbListing = listingDTO.getDbListing();
      listingDTO.getSellerInfo().setSellerContactId(dbListing.getSellerContactId());
    }
  }
  
  private void validateUpdate(Listing dbListing, List<UpdateListingEnum> updateAttributeList) {
    boolean isSTHListing = false;    
	if(dbListing.getListingSource() != null && dbListing.getListingSource() == 8) {
	  isSTHListing = true ;    
	}
	if(com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.PREDELIVERY.getValue() == dbListing.getDeliveryOption() || isSTHListing) {
	  if (updateAttributeList.contains(UpdateListingEnum.SECTION) || updateAttributeList.contains(UpdateListingEnum.ADD_SEATS)) {
	    log.error("Section/Seats cannot be updated for a predelivered OR STH Listing listingId={}", dbListing.getId());
	    throw new ListingException(ErrorType.BUSINESSERROR, ErrorCodeEnum.listingActionNotallowed, "SRS cannot be updated for predelivered listing");
	  }       
	}  
  }

}
