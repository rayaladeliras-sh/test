package com.stubhub.domain.inventory.listings.v2.helper;


import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.accountmanagement.services.orders.v3.intf.dto.OrderDetailsV3DTO;
import com.stubhub.domain.accountmanagement.services.orders.v3.intf.dto.OrderItem;
import com.stubhub.domain.catalog.read.v3.intf.events.dto.response.Event;
import com.stubhub.domain.fulfillment.pdf.v1.intf.request.CopyTicketSeat;
import com.stubhub.domain.fulfillment.window.v1.intf.EventFulfillmentWindowResponse;
import com.stubhub.domain.inventory.biz.v2.impl.util.FulfillmentServiceAdapter;
import com.stubhub.domain.inventory.biz.v2.intf.FulfillmentArtifactMgr;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.common.util.ListingException;
import com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeatDetails;
import com.stubhub.domain.inventory.datamodel.entity.enums.FulfillmentMethod;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TaxpayerStatusEnum;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.util.EventHelper;
import com.stubhub.domain.inventory.listings.v2.util.FulfillmentServiceHelper;
import com.stubhub.domain.inventory.listings.v2.util.SellerHelper;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.domain.inventory.metadata.v1.event.util.SellerPaymentUtil;
import com.stubhub.domain.inventory.v2.DTO.*;
import com.stubhub.domain.inventory.v2.enums.InventoryType;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.domain.partnerintegration.common.util.StringUtils;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Component("relistHelper")
public class RelistHelper {
  private final static Logger log = LoggerFactory.getLogger(RelistHelper.class);
  private static final int AUTO_CONFIRM = 2;
  private static final long BARCODE = 3L;
  private static final long FM_PDF_PRE = 4L;
  private static final long FM_MOBILE_PRE = 20L;
  private static final String SELLITNOW_ORDER_SOURCE="SellItNow(Bot-flow)";

  @Autowired
  private OrderDetailsHelper orderHelper;
  
  @Autowired
  private FulfillmentArtifactMgr fulfillmentArtifactMgr;

  @Autowired
  private FulfillmentServiceAdapter fulfillmentServiceAdapter;

  @Autowired
  private TicketSeatMgr ticketSeatMgr;
  
  @Autowired
  private InventoryMgr inventoryMgr;
  
  @Autowired
  private FulfillmentServiceHelper fulfillmentServiceHelper;
  
  @Autowired
  private UserHelper userHelper;
  
  @Autowired
  private SellerHelper sellerHelper;

  @Autowired
  private EventHelper eventHelper;

  @Value("${inventory.relist.setPurchasePricePerProduct.enabled:false}")
  private boolean enabledSetPurchasePricePerProduct = true;
  
  public OrderDetailsHelper getOrderHelper() {
    return orderHelper;
  }

  public void setOrderHelper(OrderDetailsHelper orderHelper) {
    this.orderHelper = orderHelper;
  }

  /**
   * helper method to validate listing With order details
   * 
   * @param request
   * @return Map<Long, List<OrderItem>>
   */
  public Map<Long, List<OrderItem>> validateListingWithOrderDetails(RelistRequest request, Map<Long, OrderDetailsV3DTO> orderDetailsMap, Locale locale) {
    Map<Long, List<OrderItem>> ordersWithOrderItems = new HashMap<Long, List<OrderItem>>();


    // validate each listing
    for (RelistListing requestListing : request.getListings()) {
      OrderDetailsV3DTO orderDetails = orderDetailsMap.get(requestListing.getOrderId());
      
      List<OrderItem> requestedItemsFromOrder =
          getRequestedItemsFromOrder(requestListing, orderDetails.getItems());
      // validate if all them items in the listing have the same deliveryOption, ticketMedium and
      // section
      validateGrouping(requestedItemsFromOrder);
      List<Long> ticketSeatIds=new ArrayList<Long>();
      for(OrderItem orderItem:requestedItemsFromOrder)
      {
    	  ticketSeatIds.add(orderItem.getSeatId());
    	  
      }
      if(!isTransferUsecase(requestListing))
      	{
    	  validateRelistWithTicketFeatures(requestedItemsFromOrder,orderDetails.getItems());

          List<Long> seats=ticketSeatMgr.findActiveTicketSeatsByOriginalSeatIds(ticketSeatIds);
          
          if(!CollectionUtils.isEmpty(seats))
          { 	  
        		  log.error("_message=\"The order has one or more items that were already relisted  orderId={}\"",orderDetails.getId());
    	            throw new ListingBusinessException(
    	                new ListingError(ErrorType.INPUTERROR, ErrorCode.ALREADY_LISTED,
    	                    "The order has one or more items that were already relisted", "orderId"));  
          }

            validateForLiabilityWaiver(requestedItemsFromOrder, orderDetails.getItems(), locale);

        }else {
      		// Check if seat is already transfered from the existing order(TransferredInd ==null or 0= NOT Transferred)
      		for(OrderItem oldOrderItem:orderDetails.getItems())
            {
      			if(ticketSeatIds.contains(oldOrderItem.getSeatId())&&!(oldOrderItem.getTransferredInd()==null || oldOrderItem.getTransferredInd().intValue()==0) ) {
      				 log.error("_message=\"The order has one or more items that were already transferred\"  orderId={} seatId={}",orderDetails.getId(),oldOrderItem.getSeatId());
     	            throw new ListingBusinessException(
     	                new ListingError(ErrorType.INPUTERROR, ErrorCode.BARCODE_TRANSFERRED,
     	                    "The order has one or more items that were already transferred", "orderId"));
      			}
            }
      	}
     

      ordersWithOrderItems.put(requestListing.getOrderId(), requestedItemsFromOrder);
     
    }
    return ordersWithOrderItems;
  }

    private void validateForLiabilityWaiver(List<OrderItem> requestedItemsFromOrder, List<OrderItem> itemsFromOrder, Locale locale) {

        //no need validate in case of relist all of them
        if (requestedItemsFromOrder.size() == itemsFromOrder.size()) {
            return;
        }

        // group order item by event id
        Map<Long, List<OrderItem>> requestSaleOrderItems = groupOrderItemBySaleId(requestedItemsFromOrder);

        Map<Long, List<OrderItem>> existSaleToOrderItems = groupOrderItemBySaleId(itemsFromOrder);

        for (Map.Entry<Long, List<OrderItem>> entry : requestSaleOrderItems.entrySet()) {
            Long saleId = entry.getKey();

            int requestItemSize = entry.getValue().size();
            int orderItemSize = existSaleToOrderItems.get(saleId).size();

            if (requestItemSize < orderItemSize) {
                Long eventId = entry.getValue().get(0).getEventId();
                try {
                    Event eventV3ById = eventHelper.getEventV3ById(eventId, locale, false);

                    if (eventV3ById == null) {
                        log.error("eventV3ById is null, eventId={}", eventId);
                        continue;
                    }

                    if (EventHelper.isLiabilityWaiver(eventV3ById)) {

                        log.error("LiabilityWaiverEvent not support split relist eventId={} requestItemSize={} < orderItemSize={}", eventId, requestItemSize, orderItemSize);
                        throw new ListingBusinessException(
                                new ListingError(ErrorType.INPUTERROR, ErrorCode.NOT_ALLOWED_TO_LIST,
                                        "Event is LiabilityWaiver, not support split relist", "orderId"));

                    }
                } catch (ListingBusinessException e) {
                    throw e;
                } catch (Exception e) {
                    //for other exceptions, just log it
                    log.warn("unexpect error when relist check against LiabilityWaiver", e);
                }
            }
        }


    }

    private Map<Long, List<OrderItem>> groupOrderItemBySaleId(List<OrderItem> itemsFromOrder) {
        Map<Long, List<OrderItem>> itemMap = new HashMap<>();

        for (OrderItem orderItem : itemsFromOrder) {
            Long id = orderItem.getSaleId();

            List<OrderItem> orderItems = itemMap.get(id);
            if (orderItems == null) {
                orderItems = new ArrayList<>();
                itemMap.put(id, orderItems);
            }

            orderItems.add(orderItem);
        }

        return itemMap;
    }

    /**
   * helper method to validate the Relist request
   * @param request
   * @return Map<Long, OrderDetailsV3DTO>
   */

	public Map<Long, OrderDetailsV3DTO> validateRelistListings(RelistRequest request) {
		// validate the relist request
		validateRequest(request);
		String orderStatus = null;
		// fetch the orderDetails for all orders in the request
		Map<Long, OrderDetailsV3DTO> orderDetailsMap = new HashMap<Long, OrderDetailsV3DTO>();
		for (RelistListing requestListing : request.getListings()) {
			if (orderDetailsMap.get(requestListing.getOrderId()) == null) {
				OrderDetailsV3DTO orderDetails = orderHelper.getOrderDetails(requestListing.getOrderId());
				if (orderDetails != null && orderDetails.getOrderStatus() != null) {
					orderStatus = orderDetails.getOrderStatus();
					if (orderStatus.contains("Purchased") || orderStatus.contains("Subsoffered")
							|| orderStatus.contains("Cancelled")) {
						log.error("_message=\"The order is in invalid status to relist orderStatus={}\"",
								orderDetails.getOrderStatus());
						throw new ListingBusinessException(new ListingError(ErrorType.INPUTERROR,
								ErrorCode.ORDER_CANCELLED,
								"The order is in either purchased, suboffered or cancelled status", "seatDetail"));
					}
				}
				orderDetailsMap.put(requestListing.getOrderId(), orderDetails);
			}
		}
		return orderDetailsMap;
	}
  
  public boolean isTransferUsecase(RelistListing relistListing)
  {
	  if(relistListing.getToEmailId()!=null || relistListing.getToCustomerGUID()!=null){
			return true;
		}
	  return false;
  }
  
  public void validateRelistWithTicketFeatures(List<OrderItem>requestedItemsFromOrder,List<OrderItem> itemsFromOrderDetails)
  {
	    Map<Long,List<Long>> listingItemMapOrder=new HashMap<Long,List<Long>>();
	    Map<Long,List<Long>> listingItemMapRequest=new HashMap<Long,List<Long>>();
	  for(OrderItem itemInOrder:requestedItemsFromOrder)
      {
    	  List<com.stubhub.domain.accountmanagement.services.orders.v3.intf.dto.TicketTrait> ticketTraits=itemInOrder.getTicketTraits();
    	  if(ticketTraits!=null)
    	  {
    		  for(com.stubhub.domain.accountmanagement.services.orders.v3.intf.dto.TicketTrait ticketTrait:ticketTraits)
    		  {
    		  if(ticketTrait.getId()==101)
    		  {
    			 if(listingItemMapRequest.get(itemInOrder.getListingId())==null)
    			 {
    				 List<Long> listOfItem=new ArrayList<Long>();
    				 listOfItem.add(itemInOrder.getSeatId());
    				 listingItemMapRequest.put(itemInOrder.getListingId(),listOfItem);
    			 }
    			 else
    			 {
    				 listingItemMapRequest.get(itemInOrder.getListingId()).add(itemInOrder.getSeatId());
    			 }
    		  }
    		  }
    	  }
      }
      for(OrderItem itemInOrder:itemsFromOrderDetails)
      {
    	  List<com.stubhub.domain.accountmanagement.services.orders.v3.intf.dto.TicketTrait> ticketTraits=itemInOrder.getTicketTraits();
    	  if(ticketTraits!=null)
    	  {
    	  for(com.stubhub.domain.accountmanagement.services.orders.v3.intf.dto.TicketTrait ticketTrait:ticketTraits)
    	  {
    		  if(ticketTrait.getId()==101)
    		  {
    			 if(listingItemMapOrder.get(itemInOrder.getListingId())==null)
    			 {
    				 List<Long> listOfItem=new ArrayList<Long>();
    				 listOfItem.add(itemInOrder.getSeatId());
    				 listingItemMapOrder.put(itemInOrder.getListingId(),listOfItem);
    			 }
    			 else
    			 {
    				 listingItemMapOrder.get(itemInOrder.getListingId()).add(itemInOrder.getSeatId());
    			 }
    		  }
    	  }}
      }
      for(Long listingId:listingItemMapOrder.keySet())
      {
    	  if(listingItemMapRequest.get(listingId)!=null)
    	  {
    		  if(listingItemMapRequest.get(listingId).size()!=listingItemMapOrder.get(listingId).size())
    		  {
    			  log.error("_message=\"Invalid spilt option with Aisle, listingId={}\"",listingId);
    			  ListingError listingError = new ListingError(
  						ErrorType.INPUTERROR,
  						ErrorCode.INVALID_SPLIT_TICKETTRAIT_COMBINATION, 
  						"Invalid splitOption with AISLE ticket traits",
  						"splitOption");
  				throw new ListingBusinessException(listingError);
    		  }
    	  }
      }

  }
  
  public void cloneFileInfoIds(List<ListingResponse> responses,Map<Long,List<OrderItem>> ordersWithOrderItems,RelistRequest request, String sellerGuid)
  {
	cloneFileInfoIdHelper(responses,ordersWithOrderItems,request, sellerGuid);
  }
	  
   /* Helper method to clone file info ids
   * 
   * @param Map<Long, List<OrderItem>>
   * @return Map<Long,Long>
   * @throws ListingBusinessException
   */
  public void cloneFileInfoIdHelper(List<ListingResponse> responses,
      Map<Long, List<OrderItem>> ordersWithOrderItems, RelistRequest request, String sellerGuid) {
    int index = 0;
    for (RelistListing relistListing : request.getListings()) {
      ListingResponse response = responses.get(index);
      final Long orderId = relistListing.getOrderId();
      List<OrderItem> listOfOrderItems = ordersWithOrderItems.get(orderId);
      OrderDetailsV3DTO orderDetails = orderHelper.getOrderDetails(orderId);
      Listing listing = inventoryMgr.getListing(Long.valueOf(response.getId()));
      String upperCaseStatus = orderDetails.getOrderStatus().toUpperCase();
      if (listing != null && listing.getTicketMedium() != null
          && (listing.getTicketMedium() == TicketMedium.PDF.getValue()
              || listing.getTicketMedium() == TicketMedium.MOBILE.getValue())) {
        if (upperCaseStatus.contains("DELIVERED") || upperCaseStatus.contains("VIEWED")) {
          List<FulfillmentWindow> fulfillmentWindows = null;
          EventFulfillmentWindowResponse efwResponse = fulfillmentServiceAdapter
              .getFulfillmentWindowsShape(listing.getEventId(), listing.getSellerContactId());
          fulfillmentWindows = fulfillmentServiceAdapter.getFulfillmentWindows(efwResponse);
          boolean isPredeliveryAvailable = false;
          if (fulfillmentWindows != null) {
            for (FulfillmentWindow window : fulfillmentWindows) {
              if (window.getFulfillmentMethodId().equals(FM_PDF_PRE)
                  || window.getFulfillmentMethodId().equals(FM_MOBILE_PRE)) {
                isPredeliveryAvailable = true;
                break;
              }
            }
          }

          if (isPredeliveryAvailable) {
            boolean isFileCopied = true;
            List<TicketSeat> listOfTicketSeats =
                ticketSeatMgr.findTicketSeatsByTicketId(Long.valueOf(response.getId()));
            List<CopyTicketSeat> copyTicketSeatList = new ArrayList<CopyTicketSeat>();
            for (int j = 0; j < listOfTicketSeats.size(); j++) {
              OrderItem orderItem = listOfOrderItems.get(j);
              TicketSeat ticketSeat = listOfTicketSeats.get(j);
              if (TicketMedium.PDF.getValue() == orderItem.getTicketMediumId().intValue()
                  || TicketMedium.MOBILE.getValue() == orderItem.getTicketMediumId().intValue()) {
                  CopyTicketSeat copyTicketSeat = new CopyTicketSeat();
                  copyTicketSeat.setSaleSeatId(orderItem.getSeatId());
                  copyTicketSeat.setListingSeatId(ticketSeat.getTicketSeatId());
                  copyTicketSeat.setListingSeatNum(ticketSeat.getSeatNumber());
                  copyTicketSeatList.add(copyTicketSeat);
                } else {
                  log.info("message=\"There are no files to be copied\" orderId={} relistedListingId={}", orderId, response.getId());
                  isFileCopied = false;
                  break;
                }
              }

            if(copyTicketSeatList!=null && !copyTicketSeatList.isEmpty()){
                try {
                    List<Long> fileInfoIdList = fulfillmentServiceAdapter
                            .cloneFileInfo(Long.valueOf(response.getId()), listOfOrderItems.get(0).getSaleId(),copyTicketSeatList);
                    if(fileInfoIdList==null ||fileInfoIdList.isEmpty() ||fileInfoIdList.size()<copyTicketSeatList.size()){
                        log.error("message=\"file copy failed, relisted listing will not be predelivered\" orderId={} relistedListingId={}", orderId, response.getId());
                        isFileCopied =false;
                    }
                } catch (Exception e) {
                    log.error("message=\"file copy failed, relisted listing will not be predelivered\" orderId={} relistedListingId={}", orderId, response.getId(), e);
                    isFileCopied = false;
                }
            }else{
                log.info("message=\"There are no files to be copied\" orderId={} relistedListingId={}", orderId, response.getId());
                isFileCopied = false;
            }

            ++index;
            if (isFileCopied) {
              try {
                updatePdfPredeliveredListing(listing, sellerGuid, fulfillmentWindows);
              } catch (ListingBusinessException lbe) {
                log.error("message=\"predelivery failed due to business error, relisted listing will not be predelivered\" orderId={} relistedListingId={}", orderId, response.getId(), lbe);
              } catch (Exception e) {
                log.error("message=\"exception occured during predelivery, relisted listing will not be predelivered\" orderId={} relistedListingId={}", orderId, response.getId(), e);
              }
            }
            response.setStatus(com.stubhub.domain.inventory.common.entity.ListingStatus
                .fromString(listing.getSystemStatus()));
          }

        }
      }
    }
  }

  public void updatePdfPredeliveredListing(Listing listing, String sellerGuid, List<FulfillmentWindow> fulfillmentWindows) throws ListingBusinessException, ListingException {
    listing.setSellerGuid(sellerGuid);
    listing.setDeliveryOption(com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.PREDELIVERY.getValue());
    listing.setConfirmOption(AUTO_CONFIRM); // 2 for AutoConfirm
    
    if (listing.getTicketMedium() != null) {
      if (listing.getTicketMedium() == TicketMedium.MOBILE.getValue()) {
        listing.setFulfillmentMethod(FulfillmentMethod.MOBILE);
      } else {
        listing.setFulfillmentMethod(FulfillmentMethod.PDF);
      }
    }

    Calendar saleEndDate = fulfillmentServiceHelper.calculateSaleEndDate(listing, fulfillmentWindows);
    if (saleEndDate == null) {
      log.error("Sale end date came as Null from fulfillment for listingId=" + listing.getId());
      ListingError listingError =
          new ListingError(ErrorType.BUSINESSERROR, ErrorCode.DELIVERY_OPTION_NOT_SUPPORTED,
              "Sale end date is null", "listingId");
      throw new ListingBusinessException(listingError);
    }
    listing.setEndDate(saleEndDate);

    // set in hand date to NOW
    listing.setInhandDate(Calendar.getInstance());
    
    String fmDmList = fulfillmentServiceHelper.calculateFmDmList(listing, fulfillmentWindows);
    listing.setFulfillmentDeliveryMethods(fmDmList);

    String status = listing.getSystemStatus();
    try {
      if (!status.equals(ListingStatus.ACTIVE.toString())) {
        listing.setSystemStatus(ListingStatus.ACTIVE.toString());
        this.validateListingActivation(listing);
      }
    } catch (ListingBusinessException lbe) {
      log.error("ListingBusinessException occured while activating the listing listingId=" + listing.getId(), lbe);
      listing.setSystemStatus(status);
    } catch (Exception e) {
      log.error("Exception occured while activating the listing listingId=" + listing.getId(), e);
      listing.setSystemStatus(status);
    }
    
    Listing dbListing = inventoryMgr.getListing(listing.getId());
    setPredeliveryAttributes(dbListing, listing);
    inventoryMgr.updateListing(dbListing);
  }
  
  private void setPredeliveryAttributes(Listing dbListing, Listing newListing) {
    dbListing.setDeliveryOption(newListing.getDeliveryOption());
    dbListing.setConfirmOption(newListing.getConfirmOption());
    dbListing.setFulfillmentMethod(newListing.getFulfillmentMethod());
    dbListing.setEndDate(newListing.getEndDate());
    dbListing.setInhandDate(newListing.getInhandDate());
    dbListing.setFulfillmentDeliveryMethods(newListing.getFulfillmentDeliveryMethods());
    dbListing.setSystemStatus(newListing.getSystemStatus());
  }

  /**
   * Helper method to validate if the order items have the same attributes like
   * ticketMedium,deliveryOption, deliveryType,deliveryMethod,fulfillmentMethod,section. This method
   * also provide validation for one or more parking passes
   * 
   * @param List<OrderItem>
   * @return None
   */
  private void validateGrouping(List<OrderItem> requestedItemsFromOrder) {

    Long eventId= requestedItemsFromOrder.get(0).getEventId();
    Long ticketMediumId = requestedItemsFromOrder.get(0).getTicketMediumId();
    Long deliveryOptionId = requestedItemsFromOrder.get(0).getDeliveryOptionId();
    Long deliveryTypeId = requestedItemsFromOrder.get(0).getDeliveryTypeId();
    Long deliveryMethodId = requestedItemsFromOrder.get(0).getDeliveryMethodId();
    Long fulfillmentMethodId = requestedItemsFromOrder.get(0).getFulfillmentMethodId();
    String section = requestedItemsFromOrder.get(0).getSection();
    ListingError error = null;

    //Set<Long> parkingPasses = null;

    for (OrderItem item : requestedItemsFromOrder) {

      if (eventId != null && !eventId.equals(item.getEventId())) {
        log.debug("_message=\"Items in the listing have different event ids\"");
        error = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_REQUEST,
            "All items in the listing should have the same EventId", "items");
        break;

      }
      
      if (ticketMediumId != null && !ticketMediumId.equals(item.getTicketMediumId())) {
        log.debug("_message=\"Items in the listing have different ticket mediums\"");
        error = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_REQUEST,
            "All items in the listing should have the same TicketMedium", "items");
        break;

      }

      /*if (deliveryOptionId != null && !deliveryOptionId.equals(item.getDeliveryOptionId())) {
        log.debug("_message=\"Items in the listing have different delivery options\"");
        error = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_REQUEST,
            "All items in the listing should have the same Delivery Option", "items");
        break;
      }

      if (deliveryTypeId != null && !deliveryTypeId.equals(item.getDeliveryTypeId())) {
        log.debug("_message=\"Items in the listing have different delivery types\"");
        error = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_REQUEST,
            "All items in the listing should have the same Delivery Type", "items");
        break;
      }

      if (deliveryMethodId != null && !deliveryMethodId.equals(item.getDeliveryMethodId())) {
        log.debug("_message=\"Items in the listing have different delivery methods\"");
        error = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_REQUEST,
            "All items in the listing should have the same Delivery Method", "items");
        break;
      }*/

      /*if (fulfillmentMethodId != null
          && !fulfillmentMethodId.equals(item.getFulfillmentMethodId())) {
        log.debug("_message=\"Items in the listing have different fulfillment Methods\"");
        error = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_REQUEST,
            "All items in the listing should have the same Fulfillment Method", "items");
        break;
      }
*/
      if (section != null && !section.equals(item.getSection())) {
        log.debug("_message=\"Items in the listing have different sections\"");
        error = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_REQUEST,
            "All items in the listing should have the same Section", "items");
        break;
      }

      /*if ("Parking Pass".equalsIgnoreCase(item.getType())) {
        if (parkingPasses == null) {
          parkingPasses = new HashSet<Long>();
        }
        parkingPasses.add(item.getId());
      }*/

    }
    /*if (parkingPasses != null && parkingPasses.size() > 1) {
      log.debug("_message=\"Items in the listing have multiple parking passes\"");
      error = new ListingError(ErrorType.INPUTERROR, ErrorCode.MULTIPLE_PARKING_PASSES,
          "Multiple Parking passes in the same listing not allowed", "items");
    }*/


    if (error != null) {
      throw new ListingBusinessException(error);
    }

  }

  /**
   * Helper method to get the requested orderItems from the order by validating if they are present
   * in the orderDetails
   * 
   * @param RelistListing
   * @param List<OrderItem>
   * @return List<OrderItem>
   */

  private List<OrderItem> getRequestedItemsFromOrder(RelistListing requestListing,
      List<OrderItem> items) {
    List<OrderItem> requestedItems = null;

    if (requestListing.getItems() == null || requestListing.getItems().size() < 1) {
      requestedItems = items;
    } else {
      requestedItems = new ArrayList<OrderItem>();
      for (RelistItem requestItem : requestListing.getItems()) {
        for (OrderItem orderItem : items) {
            //ignore orderItem with "saleStatus": "Cancelled",
            if ("Cancelled".equals(orderItem.getSaleStatus())) {
                log.warn("skip cancelled sale, saleId={}", orderItem.getSaleId());
                continue;
            }
            if (orderItem.getSeatId().toString().equals(requestItem.getItemId())) {
                requestedItems.add(orderItem);
            }
        }
      }
      if (requestedItems.size() != requestListing.getItems().size()) {
        log.debug("_message=\"Items in the request listing not found in the order\"");
        throw new ListingBusinessException(
            new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_ITEM,
                String.format("one or more of the requested items is not found in the order: {%1s}",
                    requestListing.getOrderId()),
                "items"));
      }
    }
    return requestedItems;
  }
  
  /**
   * Helper method to validate the RelistRequest
   * 
   * @param RelistRequest
   * @return None
   * @throws ListingBusinessException
   */

  private void validateRequest(RelistRequest request) {

    if (request.getListings() == null || request.getListings().size() < 1) {
      log.debug("_message=\"The request does not have any requests\"");
      throw new ListingBusinessException(new ListingError(ErrorType.INPUTERROR,
          ErrorCode.INVALID_LISTINGS, "No listings found in the request", "listings"));
    }
    Map<Long, List<RelistItem>> orders = new HashMap<Long, List<RelistItem>>();

    for (RelistListing current : request.getListings()) {
      validateRelistListing(current);

      // Validations for multiple listings with same orderIds to have items and they should be
      // unique
      List<RelistItem> itemsInPreviousListing = orders.get(current.getOrderId());

      if (itemsInPreviousListing != null && !itemsInPreviousListing.isEmpty()) {
        List<RelistItem> items = current.getItems();
        if (items == null || items.isEmpty()) {
          log.debug(
              "_message=\"Some of the listings with the same order id do not have any items\" orderId={}",
              current.getOrderId());
          throw new ListingBusinessException(
              new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_ITEM,
                  "All the listings with the same orderId should have items", "items"));
        }
        for (RelistItem item : itemsInPreviousListing) {
          for (RelistItem currentItem : current.getItems()) {
            if (currentItem.getItemId().equals(item.getItemId())) {
              log.debug(
                  "_message=\"Some of the listings with the same order id have duplicate items\" orderId={}",
                  current.getOrderId());
              throw new ListingBusinessException(
                  new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_ITEM,
                      "Multiple listings with the same orderId cannot have same items", "items"));
            }
          }
        }

      } else if (orders.keySet().contains(current.getOrderId())) {
        log.debug(
            "_message=\"Multiple listings with the same order id do not have items\" orderId={}",
            current.getOrderId());
        throw new ListingBusinessException(
            new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_ITEM,
                "Multiple listings with the same orderId should have items", "items"));

      } else {
        orders.put(current.getOrderId(), current.getItems());
      }
    }

  }


  /**
   * Helper method to validate the RelistListing of the RelistRequest
   * 
   * @param RelistListing
   * @return None
   * @throws ListingBusinessException
   */
  private void validateRelistListing(RelistListing current) {
    if (current.getOrderId() == null || current.getOrderId() < 1L) {
      log.debug("_message=\"The order id is invalid\" orderId={}", current.getOrderId());
      throw new ListingBusinessException(new ListingError(ErrorType.INPUTERROR,
          ErrorCode.INVALID_ORDER_ID, "Invalid Order Id", "orderId"));
    }
    //if transfer case and price is null then we are setting it to zero
    if (current.getPricePerItem() == null && (current.getToEmailId()!=null || current.getToCustomerGUID()!=null)) {
    	current.setPricePerItem(BigDecimal.ZERO);
    }
    if (current.getPricePerItem() == null) {
      log.debug("_message=\"The price per item is invalid\" priceItem={}",
          current.getPricePerItem());
      throw new ListingBusinessException(new ListingError(ErrorType.INPUTERROR,
          ErrorCode.INVALID_PRICE, "Invalid pricePerItem", "pricePerItem"));
    }
    if (current.getItems() != null && current.getItems().size() < 1) {
      log.debug("_message=\"The items in the listing are invalid\" items={}", current.getItems());
      throw new ListingBusinessException(
          new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_ITEM, "Invalid items", "items"));
    }

  }

  /**
   * Helper method to create listings from the RelistRequest and the order items
 * @param orderDetailsMap 
   * 
   * @param RelistRequest
   * @param Map<Long, List<OrderItem>>
   * @return List<ListingRequest>
   */

  public List<ListingRequest> createListingRequests(RelistRequest request,
	      Map<Long, List<OrderItem>> ordersWithItems, Map<Long, OrderDetailsV3DTO> orderDetailsMap) {
	  	String orderSource = null;
	    List<ListingRequest> listingRequests = new ArrayList<ListingRequest>();
	    for (RelistListing listingFromRequest : request.getListings()) {
		  	if (orderDetailsMap!=null && orderDetailsMap.get(listingFromRequest.getOrderId()).getOrderSource() != null) {
		  		orderSource = orderDetailsMap.get(listingFromRequest.getOrderId()).getOrderSource();
		  	}

	      ListingRequest listingRequest = createListingRequest(listingFromRequest,
	          ordersWithItems.get(listingFromRequest.getOrderId()),orderSource);
	      
	      setTypeForParkingPassOnlyEvent(listingRequest);
          setFaceValueToListingRequest(listingRequest, ordersWithItems.get(listingFromRequest.getOrderId()).get(0).getListingId());
	      
	      log.info("_message=\"The listingRequest created from relist request \" request={} , orderId={}", listingRequest,listingFromRequest.getOrderId());
	      listingRequests.add(listingRequest);
	    }

	    return listingRequests;
	  }

  private void setFaceValueToListingRequest(ListingRequest listingRequest, Long originalListingId) {
    Listing originalListing = inventoryMgr.getListing(originalListingId);
    Money originalFaceValue = (originalListing != null) ? originalListing.getFaceValue() : null;
    listingRequest.setFaceValue(originalFaceValue == null || originalFaceValue.getAmount() == null ? null : originalFaceValue);
    if (originalFaceValue == null || originalFaceValue.getAmount() == null) {
      log.info("_message=\"Original Face Value is null on relist process\" originalListing={} listingId={} country={} originalSellerId={}",
              (originalListing != null) ? originalListing.getId() : null,
              listingRequest.getListingId(),
              (originalListing != null) ? ((originalListing.getEvent() != null) ? originalListing.getEvent().getCountry() : null) : null,
              (originalListing != null) ? originalListing.getSellerId() : null);
    }
  }

  /*
   * a hack to get around the OrderDetailsV3 call returning OrderItemType as 'Parking Pass' for parking pass only listings. Inventory
   * creates listing/seats as 'Ticket' type for PP ONly events. The expectation is- the relist request block contains PP type seats 
   */
  private void setTypeForParkingPassOnlyEvent(ListingRequest listingRequest) {
    List<Product> products = listingRequest.getProducts();
    boolean isAllParkingPass = true;
    for(Product product: products){
      if(ProductType.PARKING_PASS != product.getProductType()){
        isAllParkingPass = false;
      }
    }
    if(isAllParkingPass){
      for(Product product: products){
        product.setProductType(ProductType.TICKET);
      }
    }
    
  }

  /**
   * Helper method to createListingRequests(RelistRequest,Map<Long, List<OrderItem>>)
   *
   * @return ListingRequest
   */

  private ListingRequest createListingRequest(RelistListing listingFromRequest,
      List<OrderItem> itemsFromOrder, String orderSource) {
    ListingRequest request = new ListingRequest();
    OrderItem orderItem = itemsFromOrder.get(0);
    Money pricePerTicket = orderItem.getPricePerTicket();
    log.info("_message=\"The relist request \" request={} , orderId={}, saleId={}, pricePerTicket={}, orderSource={}"
    		, listingFromRequest, listingFromRequest.getOrderId(), orderItem.getSaleId(), pricePerTicket, orderSource);
    Money pricePerItem = new Money();
    pricePerItem.setAmount(listingFromRequest.getPricePerItem());
    pricePerItem.setCurrency(pricePerTicket.getCurrency());
    request.setPricePerProduct(pricePerItem);
    request.setEventId(orderItem.getEventId().toString());
    request.setSection(orderItem.getSection());
    //Relist not for friend to friend transfer, capturing original pricePerTicket as Purchase Price
    if (listingFromRequest.getPricePerItem() != null && listingFromRequest.getPricePerItem().signum() > 0 ) {
        if (enabledSetPurchasePricePerProduct) {
            if (orderItem.getCostPerTicket() != null) {
                log.info("set PurchasePricePerProduct={}", orderItem.getCostPerTicket());

                request.setPurchasePricePerProduct(orderItem.getCostPerTicket());
            } else {
                request.setPurchasePricePerProduct(pricePerTicket);
                // in case of costPerTicket is not returned
            }
            request.setSalesTaxPaid(true);
        }
    }
    if (listingFromRequest.isAutoPricingEnabledInd()!=null && listingFromRequest.isAutoPricingEnabledInd()) {
    	request.setAutoPricingEnabledInd(true);
    }
    List<Product> products = new ArrayList<Product>();
    for(FulfillmentMethod fulfillmentMethod:FulfillmentMethod.values())
    {
    	if(fulfillmentMethod.getCode().equals(orderItem.getFulfillmentMethodId()))
    	{
    		if (fulfillmentMethod.equals(FulfillmentMethod.UPS)) {
    			request.setDeliveryOption(DeliveryOption.UPS);
    		}
    		else if (fulfillmentMethod.equals(FulfillmentMethod.SHIPPING)) {
    			request.setDeliveryOption(DeliveryOption.SHIPPING);
    		}
            else if (fulfillmentMethod.equals(FulfillmentMethod.ROYALMAIL)) {
              request.setDeliveryOption(DeliveryOption.SHIPPING);
            }
            else if (fulfillmentMethod.equals(FulfillmentMethod.DEUTSCHEPOST)) {
              request.setDeliveryOption(DeliveryOption.SHIPPING);
            }
    		else if (fulfillmentMethod.equals(FulfillmentMethod.PDF) || fulfillmentMethod.equals(FulfillmentMethod.PDFPREDELIVERY)) {
    			request.setDeliveryOption(DeliveryOption.PDF);
    		} 
    		else if (fulfillmentMethod.equals(FulfillmentMethod.BARCODE) || fulfillmentMethod.equals(FulfillmentMethod.BARCODEPREDELIVERYNONSTH) || fulfillmentMethod.equals(FulfillmentMethod.BARCODEPREDELIVERYSTH) ) {
    			request.setDeliveryOption(DeliveryOption.BARCODE);
    		} 
    		else if (fulfillmentMethod.equals(FulfillmentMethod.LMS) || fulfillmentMethod.equals(FulfillmentMethod.LMSPREDELIVERY)) {
    			request.setDeliveryOption(DeliveryOption.LMS);
    		}
    		else if (fulfillmentMethod.equals(FulfillmentMethod.FLASHSEAT) || fulfillmentMethod.equals(FulfillmentMethod.FLASHSEATNONINSTANT) || fulfillmentMethod.equals(FulfillmentMethod.FLASHTRANSFER)) {
    			request.setDeliveryOption(DeliveryOption.FLASHSEAT);
    		}
    		else if (fulfillmentMethod.equals(FulfillmentMethod.OTHERPREDELIVERY)) {
    			request.setDeliveryOption(DeliveryOption.WILLCALL);
    		}
    		else if (fulfillmentMethod.equals(FulfillmentMethod.LOCALDELIVERY)) {
    			request.setDeliveryOption(DeliveryOption.LOCALDELIVERY);
    		}
    		else if (fulfillmentMethod.equals(FulfillmentMethod.MOBILETRANSFER)) {
    			request.setDeliveryOption(DeliveryOption.MOBILETRANSFER);
    		}
    		else if (fulfillmentMethod.equals(FulfillmentMethod.MOBILEINSTANT) || fulfillmentMethod.equals(FulfillmentMethod.MOBILE)) {
    			request.setDeliveryOption(DeliveryOption.MOBILE);
    		}
    	}
    }
    if(orderItem.getTicketMediumId() != null) {
    	request.setTicketMedium(com.stubhub.domain.inventory.v2.enums.TicketMedium.getById(orderItem.getTicketMediumId().intValue())); 
    }
    
    request.setProducts(products);
    List<TicketTrait> seatTraits = null;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
    TimeZone tz = TimeZone.getTimeZone("UTC");
    sdf.setTimeZone(tz);
    Date listingIHDate = orderItem.getTicketInhandDate();
    boolean isPreDelivered=verifyIfPreDelivered(itemsFromOrder);
    for (OrderItem item : itemsFromOrder) {
      if (item.getTicketInhandDate() != null
          && item.getTicketInhandDate().compareTo(listingIHDate) != 0) {
        if (item.getTicketInhandDate().after(listingIHDate)) {
          listingIHDate = item.getTicketInhandDate();
        }
      }
      Product product = createProductFromOrderItem(item, orderSource);
      if(!isPreDelivered)
      {
    	  product.setFulfillmentArtifact(null);
      }
      products.add(product);

      List<TicketTrait> itemTraits = createTicketTraits(item);
      if (itemTraits != null && seatTraits == null) {
        seatTraits = new ArrayList<TicketTrait>();
      }
      if (itemTraits != null) {
        seatTraits.addAll(itemTraits);
      }
    }
    request.setTicketTraits(seatTraits);
    return request;

  }
  
  /**
   * Helper method to check of all the items to be created as one listing have the same or different delivery option
   * 
   * @param OrderItem
   * @return boolean
   */
  private boolean verifyIfPreDelivered(List<OrderItem> itemsFromOrder) {
	for(OrderItem item:itemsFromOrder)
	{
		if ((StringUtils.trimToNull(item.getBarcodeText()) == null) && (StringUtils.trimToNull(item.getSecureRenderBarcode()) == null))
			return false;
	}
	return true;
}

/**
   * Helper method to create ticket traits from the order item
   * 
   * @param OrderItem
   * @return List<TicketTrait>
   */

  private List<TicketTrait> createTicketTraits(OrderItem item) {

    Map<Long, TicketTrait> seatTraits = null;
    if (item.getTicketTraits() != null && item.getTicketTraits().size() > 0) {
      seatTraits = new HashMap<Long, TicketTrait>();

      for (com.stubhub.domain.accountmanagement.services.orders.v3.intf.dto.TicketTrait orderItemTrait : item
          .getTicketTraits()) {
        if (seatTraits.get(orderItemTrait.getId()) == null) {
          TicketTrait trait = new TicketTrait();
          trait.setId(orderItemTrait.getId().toString());
          trait.setName(orderItemTrait.getName());
          trait.setType(orderItemTrait.getType().toString());
          trait.setOperation(Operation.ADD);
          seatTraits.put(orderItemTrait.getId(), trait);
        }
      }

    }
    return seatTraits != null ? new ArrayList(seatTraits.values()) : null;
  }

  /**
   * Helper method to create product from the order item
   * 
   * @param OrderItem
   * @return Product
   */

  private Product createProductFromOrderItem(OrderItem item, String orderSource) {
      Product product = new Product();
      product.setOperation(Operation.ADD);
      product.setRow(item.getRow());
      product.setSeat(item.getSeat());
      log.debug("_message=\"Create Products from Order \" orderSource={}", orderSource);
      if (SELLITNOW_ORDER_SOURCE.equalsIgnoreCase(orderSource)) {
          product.setInventoryType(InventoryType.SELLITNOW);
      }
      ProductType itemType = ProductType.fromString(item.getType());
      if (itemType != null) {
          product.setProductType(itemType);
      }

      if (Objects.equals(item.getTicketMediumId(), BARCODE)) {
          //TODO comment it out for now, as NFL will be impacted by this too, SELLAPI-4455
//          if (StringUtils.isNotBlank(item.getPartnerUniqueTixNo())) {
//              product.setFulfillmentArtifact(item.getPartnerUniqueTixNo());
//              log.info("set FulfillmentArtifact from PartnerUniqueTixNo={} seatId={}", item.getPartnerUniqueTixNo(), item.getSeatId());
//          } else
          if (StringUtils.isNotBlank(item.getBarcodeText())) {
              product.setFulfillmentArtifact(item.getBarcodeText());
          } else if (StringUtils.isNotBlank(item.getSecureRenderBarcode())) {
              product.setFulfillmentArtifact(item.getSecureRenderBarcode());
          }
      }
      product.setGa(item.getGeneralAdmission());

      return product;
  }

  /**
   * Returns property value for the given propertyName. This protected method has been created to
   * get around the static nature of the MasterStubHubProperties' methods for Unit tests. The test
   * classes are expected to override this method with custom implementation.
   * 
   * @param propertyName
   * @param defaultValue
   * @return
   */
  protected String getProperty(String propertyName, String defaultValue) {
    return MasterStubHubProperties.getProperty(propertyName, defaultValue);
  }
  
  public void validateListingActivation(Listing listing) throws ListingException {
    // SELLAPI-1773
    boolean paymentMethodRequired =
        SellerPaymentUtil.isPaymentMethodRequired(listing.getSellerId().toString());

    if (paymentMethodRequired) {
      if (listing.getSellerPaymentTypeId() == null) {
        ListingError listingError =
            new ListingError(ErrorType.BUSINESSERROR, ErrorCode.LISTING_ACTION_NOTALLOWED, "",
                "SellerPaymentTypeId");
        throw new ListingBusinessException(listingError);
      }
    }

    if (listing.getSellerContactId() == null) {
      ListingError listingError =
          new ListingError(ErrorType.BUSINESSERROR, ErrorCode.LISTING_ACTION_NOTALLOWED, "",
              "SellerContactId");
      throw new ListingBusinessException(listingError);
    }

    sellerHelper.populateSellerDetails(listing);
    if (listing.getTaxpayerStatus() != null
        && TaxpayerStatusEnum.TINRequired.getStatus().equalsIgnoreCase(listing.getTaxpayerStatus())
        || TaxpayerStatusEnum.TINInvalid.getStatus().equalsIgnoreCase(listing.getTaxpayerStatus())) {
      ListingError listingError =
          new ListingError(ErrorType.BUSINESSERROR, ErrorCode.TAXPAYER_ERROR,
              "TIN is either not on file or Invalid", "");
      throw new ListingBusinessException(listingError);
    }

    Boolean isFraudCheckStatus =
        listing.getFraudCheckStatusId() == null || listing.getFraudCheckStatusId() == 500L;
    if (!isFraudCheckStatus) {
      if (paymentMethodRequired
          && !(userHelper.isSellerPaymentContactIdPopulated(listing.getSellerGuid(),
              listing.getSellerPaymentTypeId(), listing.getAllsellerPaymentInstrumentsV2()))) {
        ListingError listingError =
            new ListingError(ErrorType.BUSINESSERROR, ErrorCode.LISTING_ACTION_NOTALLOWED, "",
                "SellerPaymentContactId");
        throw new ListingBusinessException(listingError);
      }
      if ((listing.getSellerCCId() == null)
          || (listing.getSellerCCId().longValue() == 48411)
          || (listing.getAllsellerPaymentInstruments() == null && !userHelper.isSellerCCIdValid(
              listing.getSellerGuid(), listing.getSellerCCId()))) {
        ListingError listingError =
            new ListingError(ErrorType.BUSINESSERROR, ErrorCode.LISTING_ACTION_NOTALLOWED, "",
                "SellerCCId");
        throw new ListingBusinessException(listingError);
      }
    }
    if ((listing.getListPrice() == null) || (listing.getListPrice().getAmount() == null)
        || (listing.getListPrice().getAmount().doubleValue() <= 0)) {
      ListingError listingError =
          new ListingError(ErrorType.BUSINESSERROR, ErrorCode.LISTING_ACTION_NOTALLOWED, "",
              "TicketPrice");
      throw new ListingBusinessException(listingError);
    }
    if ((listing.getSplitOption() == null) || (listing.getSplitQuantity() == null)) {
      ListingError listingError =
          new ListingError(ErrorType.BUSINESSERROR, ErrorCode.LISTING_ACTION_NOTALLOWED, "",
              "split");
      throw new ListingBusinessException(listingError);
    }

    if (listing.getFraudCheckStatusId() != null && listing.getFraudCheckStatusId() != 500L) {
      ListingError listingError =
          new ListingError(ErrorType.BUSINESSERROR, ErrorCode.LISTING_ACTION_NOTALLOWED, "",
              "listingId");
      throw new ListingBusinessException(listingError);
    }
  }
  public void addOriginalTicketSeatIds(List<ListingResponse> responses, Map<Long, List<OrderItem>> ordersWithOrderItems,
			RelistRequest request)  {
		// TODO Auto-generated method stub
		  List<RelistListing> relistlistings=request.getListings();
		  //Retrieve the ticket seats from the listing
		  for(int i=0;i<responses.size();i++)
		  {
			List<TicketSeat> ticketSeats=  ticketSeatMgr.findAllTicketSeatsByTicketId(Long.parseLong(responses.get(i).getId()));
			RelistListing relistListing=relistlistings.get(i);
			List<OrderItem> listOfOrderItems=ordersWithOrderItems.get(relistListing.getOrderId());
			List<Long> ticketSeatIds=new ArrayList<Long>();
			String section="";
			if(relistListing.getItems()!=null)
			{
				for(RelistItem item:relistListing.getItems())
				{
					ticketSeatIds.add(Long.parseLong(item.getItemId()));
				}
			}
			else 
			{
				for(OrderItem item:listOfOrderItems)
				{
					ticketSeatIds.add(item.getSeatId());
				}
			}
			if(ticketSeats!=null)
				{
				if(ticketSeats.get(0).getSection().equals("General Admission"))
				{
				
					for(int j=0;j<ticketSeats.size();j++)
					{
						ticketSeats.get(j).setOriginalTicketSeatId(ticketSeatIds.get(j));
						ticketSeatMgr.updateTicketSeat(ticketSeats.get(j));
					}
				}
				else
				{
					HashMap<String,List<Long>> map=new HashMap<String,List<Long>>();
					String hashKey="";
						for(OrderItem item:listOfOrderItems)
							{
							for(Long ticketSeatId:ticketSeatIds)
								{
									if(ticketSeatId.equals(item.getSeatId()))
									{
										if(item.getSection()!=null)
										{
											hashKey=item.getSection();
												if(item.getRow()!=null)
												{
													hashKey=hashKey.concat(item.getRow());
														{
															if(item.getSeat()!=null)
															{
																hashKey=hashKey.concat(item.getSeat());
															}
														}
												}
										}
								if (hashKey != null) {
									hashKey = hashKey.replace(" ", "");
									if (map.get(hashKey) != null) {
										map.get(hashKey).add(ticketSeatId);
									} else {
										List<Long> list = new ArrayList<Long>();
										list.add(ticketSeatId);
										map.put(hashKey, list);
									}
								}
									}
								}
							}
						Long origSeatId=0L;
						List<Long> list=new ArrayList<Long>();
						for(TicketSeat ticketSeat:ticketSeats)
						{
							if(ticketSeat.getSection()!=null)
							{
								hashKey=ticketSeat.getSection();
									if(ticketSeat.getRow()!=null)
									{
										hashKey=hashKey.concat(ticketSeat.getRow());
											{
												if(ticketSeat.getSeatNumber()!=null)
												{
													hashKey=hashKey.concat(ticketSeat.getSeatNumber());
												}
											}
									}
							}
							hashKey=hashKey.replace(" ", "");
							if(map.containsKey(hashKey))
							{
								list=map.get(hashKey);
								origSeatId=list.get(0);
								list.remove(0);
								ticketSeat.setOriginalTicketSeatId(origSeatId);
								ticketSeatMgr.updateTicketSeat(ticketSeat);
							}
						}
			
		  }
	   }

	}}
}
