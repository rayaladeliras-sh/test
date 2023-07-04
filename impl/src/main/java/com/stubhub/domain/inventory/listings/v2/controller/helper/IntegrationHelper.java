package com.stubhub.domain.inventory.listings.v2.controller.helper;


import com.stubhub.common.exception.StubHubSystemException;
import com.stubhub.domain.infrastructure.common.core.concurrent.SHThreadLocals;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.infrastructure.common.exception.base.SHRuntimeException;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.infrastructure.common.exception.derived.SHBadRequestException;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.biz.v2.intf.IntegrationManager;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TTOrder;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.util.JMSMessageHelper;
import com.stubhub.domain.inventory.v2.DTO.Buyer;
import com.stubhub.domain.inventory.v2.DTO.ExternalOrderRequest;
import com.stubhub.domain.inventory.v2.DTO.ExternalOrderResponse;
import com.stubhub.domain.inventory.v2.DTO.Ticket;
import com.stubhub.domain.inventory.v2.enums.TicketSeatStatusEnum;
import com.stubhub.domain.partnerintegration.common.util.StringUtils;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.BarcodesRequest;
import com.stubhub.domain.partnerintegration.services.tickets.v1.intf.dto.Product;
import com.stubhub.domain.user.services.customers.v2.intf.GetCustomerResponse;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


@Component("integrationHelper")
public class IntegrationHelper {

    @Autowired
    private ObjectMapper objectMapper;

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @Autowired
    private TicketSeatMgr ticketSeatMgr;

    @Autowired
    private InventoryMgr inventoryMgr;
    
    @Autowired
    private IntegrationManager integrationManager;
    
    @Autowired
	private JMSMessageHelper jmsMessageHelper;
    
    @Autowired
    private SvcLocator svcLocator;
    
    private final static Logger log = LoggerFactory.getLogger(IntegrationHelper.class);

    private final static String TT_ORDER_UPDATE_IDENTIFIER = "ListingController";

    private final long TICKET_SEAT_STATUS_AVAILABLE = 1L;

    private final long TICKET_SEAT_STATUS_REMOVED = 4L;
    
    private static final String NEWAPI_ACCESS_TOKEN_KEY = "newapi.accessToken";
    private static final String ACCESS_TOCKEN_DEFAULT_VALUE = "JYf0azPrf1RAvhUhpGZudVU9bBEa";


    public boolean reserveInventory(ListingHolder listingHolder, GetCustomerResponse customerDetails) {
        ExternalOrderResponse reserveResponse = null;
        final int maxRetryCount = getPropertyAsInt("ship.partner.integration.retryCount", 1);

        List<Object> providers = new ArrayList<>();
        ObjectMapper reserveObjectMapper = new ObjectMapper();
        reserveObjectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        reserveObjectMapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, false);
        JacksonJaxbJsonProvider j = new JacksonJaxbJsonProvider();
        j.setMapper(reserveObjectMapper);
        providers.add(j);
        SHAPIContext apiContext=SHAPIThreadLocal.getAPIContext();

        for (int i = 0; i <= maxRetryCount; i++) {
        	SHAPIThreadLocal.set(apiContext);
            try {
                ExternalOrderRequest request = getReserveRequest(listingHolder.getListing().getSaleId(), listingHolder.getListing(), listingHolder.getSoldSeats(),
                        listingHolder.getFulfillmentType(), customerDetails, listingHolder.getBuyer());
                final String reserveInventoryURL = getProperty("ship.partner.integration.hold.inventory.v2.api.url", "https://api-int.stubprod.com/integration/holdinventory/v2");
                log.info("_message=\"calling reserveInventory on SHIP \" url={}", reserveInventoryURL);

                WebClient webClient = createWebClient(reserveInventoryURL, providers);
                webClient.accept(MediaType.APPLICATION_JSON);
                webClient.header("Content-Type", MediaType.APPLICATION_JSON);
                ClientConfiguration config = WebClient.getConfig(webClient);
                config.getInInterceptors().add(new LoggingInInterceptor());
                config.getOutInterceptors().add(new LoggingOutInterceptor());

                SHMonitor mon = SHMonitorFactory.getMonitor();
                Response response = null;
                try {
                    mon.start();
                    response = webClient.post(request);
                } finally {
                    mon.stop();
                    log.info(SHMonitoringContext.get() + " _operation=reserveInventory" + " _message= service call for saleId={}" + listingHolder.getListing().getSaleId() + "  _respTime=" + mon.getTime());
                }

                if (Response.Status.OK.getStatusCode() == response.getStatus()) {
                    log.info("_message=\"reserve call successful and inventory is available\" responseStatus={} saleId={}", response.getStatus(), listingHolder.getListing().getSaleId());
                    InputStream is = (InputStream) response.getEntity();
                    reserveResponse = reserveObjectMapper.readValue(is, ExternalOrderResponse.class);
                    log.info(reserveResponse.toString());
                    if (!StringUtils.isBlank(reserveResponse.getShoppingCartGuid())) {
                        processTTOrder(reserveResponse);
                    }
                    return true;
                } else if (Response.Status.BAD_REQUEST.getStatusCode() == response.getStatus()) {
                    log.error("_message=\"reserve call FAILED and inventory is not available\" responseStatus={} saleId={}", response.getStatus(), listingHolder.getListing().getSaleId());
                    if (listingHolder.getListing().getIsETicket()!=null && listingHolder.getListing().getIsETicket()){
                    	reserveResponse = new ExternalOrderResponse();
                    	reserveResponse.setDeleteInventory(true);
                    }else{
                    	InputStream is = (InputStream) response.getEntity();
                    	reserveResponse = reserveObjectMapper.readValue(is, ExternalOrderResponse.class);
                    }
                    processReserveResponse(reserveResponse, listingHolder.getSoldSeats(), listingHolder.getListing());
                    return false;
                } else {
                    if (i == maxRetryCount) {
                        log.error("_message=\"reserve call FAILED with NOT_FOUND error\" responseStatus={} saleId={}", response.getStatus(), listingHolder.getListing().getSaleId());
                        if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
                            Listing listing = listingHolder.getListing();
                            listing.setSystemStatus(ListingStatus.DELETED.toString());
                            inventoryMgr.updateListingOnly(listing);
                            unlockArtifact(listing);                    
                            return false;
                        }

                        log.error("_message=\"reserve call FAILED with unknown error\" responseStatus={} saleId={}", response.getStatus(), listingHolder.getListing().getSaleId());
                        SHRuntimeException shException = new SHSystemException("An internal processing error occurred in the system");
                        shException.setErrorCode("inventory.listings.systemError");
                        throw shException;
                    }
                }
            } catch (SocketTimeoutException se) {
                if (i == maxRetryCount) {
                    log.error("_message=\"socket timeout error while making reserve call\" orderId={}", listingHolder.getListing().getSaleId(), se);
                    SHRuntimeException shException = new SHSystemException("An internal processing error occurred in the system");
                    shException.setErrorCode("inventory.listings.timeoutError");
                    throw shException;
                }
            } catch (Exception e) {
                if (i == maxRetryCount) {
                    log.error("_message=\"unknown exception while making reserve call\" orderId={}", listingHolder.getListing().getSaleId(), e);
                    SHRuntimeException shException = new SHSystemException("An internal processing error occurred in the system");
                    shException.setErrorCode("inventory.listings.systemError");
                    throw shException;
                }
            } 
        }
        return false;
    }

    private void processTTOrder(ExternalOrderResponse reserveResponse) {

        TTOrder ttOrder = new TTOrder();
        ttOrder.setTid(reserveResponse.getOrderId());
        if (reserveResponse.getAutoConfirm() != null) {
            ttOrder.setAutoConfirm(reserveResponse.getAutoConfirm());
        }
        if (reserveResponse.getSaleType() != null) {
            ttOrder.setSaleType(reserveResponse.getSaleType());
        }
        if (!StringUtils.isBlank(reserveResponse.getShoppingCartGuid())) {
            ttOrder.setShoppingCartGuid(reserveResponse.getShoppingCartGuid());
        }
        Calendar cal = Calendar.getInstance();
        ttOrder.setCreatedDate(cal);
        ttOrder.setLastUpdatedDate(cal);
        ttOrder.setCreatedBy(TT_ORDER_UPDATE_IDENTIFIER);
        ttOrder.setLastUpdatedBy(TT_ORDER_UPDATE_IDENTIFIER);

        inventoryMgr.addTTOrder(ttOrder);

    }

    protected WebClient createWebClient(final String reserveInventoryURL, List<Object> providers) {
        WebClient webClient = WebClient.create(reserveInventoryURL, providers);
        return webClient;
    }

    protected String getProperty(String propertyName, String defaultValue) {
        return MasterStubHubProperties.getProperty(propertyName, defaultValue);
    }

    protected int getPropertyAsInt(String propertyName, int defaultValue) {
        return MasterStubHubProperties.getPropertyAsInt(propertyName, defaultValue);
    }

    private void processReserveResponse(ExternalOrderResponse reserveResponse, List<TicketSeat> requestedTicketSeats, Listing listing) {
        try {
          if (reserveResponse != null && reserveResponse.getDeleteInventory()) {
              List<TicketSeat> removedTicketSeats = new ArrayList<TicketSeat>();
              Integer qtyRemain = listing.getQuantityRemain() - requestedTicketSeats.size();
              Integer qty = listing.getQuantity() - requestedTicketSeats.size();
              //Add 1 to qtyRemain in case if tickets with parking pass as parking pass is not included in the original quantity
              if (listing.getListingType() != null && listing.getListingType() == 3L) {
                  qtyRemain = qtyRemain + 1;
                  qty = qty + 1;
              }
              if (qtyRemain == 0) {
                  listing.setSystemStatus(ListingStatus.DELETED.toString());
                  //Fix for SELLAPI-3510
                  for (TicketSeat ticketSeat : requestedTicketSeats) {
                      if (ticketSeat.getSeatStatusId() == TICKET_SEAT_STATUS_AVAILABLE) {
                          ticketSeat.setSeatStatusId(TICKET_SEAT_STATUS_REMOVED);
                          removedTicketSeats.add(ticketSeat);
                      }
                  }
              } else {
                  for (TicketSeat ticketSeat : requestedTicketSeats) {
                      if (ticketSeat.getSeatStatusId() == TICKET_SEAT_STATUS_AVAILABLE) {
                          ticketSeat.setSeatStatusId(TICKET_SEAT_STATUS_REMOVED);
                          removedTicketSeats.add(ticketSeat);
                      }
                  }
  
                  listing.setQuantityRemain(qtyRemain);
                  listing.setQuantity(qty);
                  listing.getTicketSeats().removeAll(requestedTicketSeats);
                  if (!CommonConstants.GENERAL_ADMISSION.equalsIgnoreCase(listing.getSeats())) {
                      if (listing.getQuantityRemain() == 0) {
                          listing.setSeats(null);
                      } else if (listing.getQuantityRemain() > 0) {
                          StringBuilder seats = new StringBuilder();
                          for (TicketSeat ts : listing.getTicketSeats()) {
                              seats.append(ts.getSeatNumber() + ",");
                          }
                          listing.setSeats(seats.substring(0, seats.length() - 1));
                      }
                  }
              }
              if(isPredelivery(listing) && !(ListingStatus.DELETED.toString().equalsIgnoreCase(listing.getSystemStatus()))) {
                  listing.setSystemStatus(ListingStatus.PENDING_LOCK.toString());
              }
              integrationManager.updateListingAndSeats(listing, removedTicketSeats);
              unlockArtifact(listing);
          }
        } catch (Exception e) {
          log.error("message=\"Exception while deleting seats/listing after 400 from holdInventory\" listingId={}", listing.getId(), e);
        }
    }
    
    private void unlockArtifact(Listing listing) {
		if(isPredelivery(listing)) {		
			//if the entire listing is deleted, send an unlock message
			//else set the system status to PENDING_LOCK and send a lock message. The downstream system will
			//process the unlock and sets the system_status to ACTIVE.
			if(ListingStatus.DELETED.toString().equalsIgnoreCase(listing.getSystemStatus())){
				log.debug("Barcode/Flashseat predelivered listing, sending unlock inventory message for listingId="+ listing.getId());
				jmsMessageHelper.sendUnlockInventoryMessage(listing.getId());
			}else{
				//listing.setSystemStatus(ListingStatus.PENDING_LOCK.toString());
				jmsMessageHelper.sendLockInventoryMessage(listing.getId());
			}
		}
	}
    
    private boolean isPredelivery(Listing listing) {
      if (listing.getTicketMedium() != null && listing.getDeliveryOption() != null) {
        if ((TicketMedium.BARCODE.getValue() == listing.getTicketMedium()
            || TicketMedium.FLASHSEAT.getValue() == listing.getTicketMedium())
            && DeliveryOption.PREDELIVERY.getValue() == listing.getDeliveryOption()) {
          return true;
        }
      }
      return false;
    }

    private ExternalOrderRequest getReserveRequest(Long orderId, Listing listing, List<TicketSeat> requestedTicketSeats, String fulfillmentType, GetCustomerResponse customerDetails, Buyer buyer) {
        
    	
    	ExternalOrderRequest request = new ExternalOrderRequest();
    	BarcodesRequest response = null;
    	
		if (listing.getIsETicket() != null && listing.getIsETicket()) {
			BarcodesRequest barcodesRequest = getSellerBarcodesRequest(listing, requestedTicketSeats);
			response = getSellerBaracodeResponse(barcodesRequest);
			if (response != null && (response.getProducts() != null && !response.getProducts().isEmpty())) {
				request.setVenueConfigSectionId(response.getVenueConfigSectionId());
				request.setVendorEventId(response.getVendorEventId());
			} else {
				log.info("_message=\"failed to  get sellerbarcodes on partnerintegration  \" listingId={}", listing.getId());
			}
		}
    	
        request.setOrderId(listing.getSaleId());
        request.setListingId(listing.getId());
        request.setEventId(listing.getEventId());
        request.setExternalListingId(listing.getExternalId());
        request.setFulfillmentType(fulfillmentType);
        Money sellerPayout = new Money();
        sellerPayout.setAmount(new BigDecimal(0));
        sellerPayout.setCurrency(listing.getListPrice().getCurrency());
        request.setSellerPayout(sellerPayout);
        Integer requestedQuantity = requestedTicketSeats.size();
        if (listing.getListingType() != null && listing.getListingType() == 3L) {
            requestedQuantity = requestedQuantity - 1;
        }
        BigDecimal orderTotal = listing.getListPrice().getAmount().multiply(new BigDecimal(requestedQuantity));
        request.setOrderTotal(orderTotal);
        request.setSellerId(listing.getSellerId());
        request.setSellerGuid(customerDetails.getUserCookieGuid());
        request.setBrokerId(customerDetails.getTicTechBrokerId());
        request.setSellerEmail(customerDetails.getEmailAddress());
        if (buyer != null) {
            request.setBuyerFirstName(buyer.getFirstName());
            request.setBuyerLastName(buyer.getLastName());
            request.setBuyerEmailAddress(buyer.getEmail());
        }
        if (!requestedTicketSeats.isEmpty()) {
            List<Ticket> tickets = new ArrayList<Ticket>();
            for (TicketSeat ticketSeat : requestedTicketSeats) {
                Ticket ticket = new Ticket();
                ticket.setId(ticketSeat.getTicketSeatId());
                ticket.setExternalTicketSeatId(ticketSeat.getExternalSeatId());
                ticket.setSection(ticketSeat.getSection());
                ticket.setRow(ticketSeat.getRow());
                ticket.setSeat(ticketSeat.getSeatNumber());
                ticket.setPrice(listing.getListPrice());
                ticket.setStatus(TicketSeatStatusEnum.getTicketSeatStatusEnumByCode(ticketSeat.getSeatStatusId().intValue()).toString());
                if(listing.getIsETicket() != null && listing.getIsETicket() && response != null) {
                	ticket.setFulfillmentArtifact(getFulfillmentArtifact(ticket, response.getProducts()));
                }
                tickets.add(ticket);
            }
            request.setTickets(tickets);
            
            //flag for NFL tickets
            if(listing.getIsETicket() != null) {
            	request.setIsRegisteredListing(listing.getIsETicket());
            }
        }
        return request;
    }
    
    private String getFulfillmentArtifact(Ticket ticket, List<Product> products) { 
    	for(Product p : products) {
    		if(p.getSeatId().equals(ticket.getId())) {
    			return p.getFulfillmentArtifact();	
    		}
    	}
    	return null;
    }
    
	private BarcodesRequest getSellerBaracodeResponse(BarcodesRequest request) {
		String url = getProperty("partnerintegration.seller.barcodes.api.url",
				"https://intsvc.api.stubprod.com/partnerintegration/tickets/v1");
		url = url + "/sellerbarcodes";
		log.info("_message=\"calling get sellerbarcodes on partnerintegration  \" url={}", url);
		BarcodesRequest barcodesResponse = null;

		WebClient webClient = svcLocator.locate(url);
		webClient.accept(MediaType.APPLICATION_JSON);
		webClient.header("Content-Type", "application/json");

		ObjectMapper mapper = new ObjectMapper();

		try {
			String json = mapper.writeValueAsString(request);
			log.info("_message=\"get sellerbarcodes with payload  \" requestBody={}", json);
			Response response = webClient.post(json);

			if (response != null) {
				log.info("_message=\"get sellerbarcodes response  \" status={}", response.getStatus());
				if (Response.Status.OK.getStatusCode() == response.getStatus()) {
					InputStream in = (InputStream) response.getEntity();

					try {
						mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
						barcodesResponse = mapper.readValue(in, BarcodesRequest.class);
					} catch (IOException e) {
						log.error("IO Exception ", e);
					}
				} else if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
					log.error("unable to get seller barcodes for  listingId {} =", request.getListingId());
				}
			} else {
				log.error("_message=\"Error occured wihile getting sellerbarcodes for  \" listingId={}\"",
						request.getListingId());
			}
		} catch (StubHubSystemException | IOException e) {
			log.error("_message=\"unknown exception while making getSellerBarcodes call\" listingId={}", request.getListingId(), e, e);
		}

		return barcodesResponse;

	}
	
	private BarcodesRequest getSellerBarcodesRequest(Listing listing, List<TicketSeat> requestedTicketSeats) {

		BarcodesRequest sellerBarcodes = new BarcodesRequest();
		sellerBarcodes.setEventId(listing.getEventId());
		sellerBarcodes.setListingId(listing.getId());
		sellerBarcodes.setOrderId(listing.getSaleId());
		List<Product> products = new ArrayList<>();
		for (TicketSeat ts : requestedTicketSeats) {
			Product p = new Product();
			p.setSeatId(ts.getTicketSeatId());
			products.add(p);
		}

		sellerBarcodes.setProducts(products);

		return sellerBarcodes;
	}




    public GetCustomerResponse getShipCustomer(Long customerId) {
        String customerGUID = getUserGuidFromUid(customerId);
        if (customerGUID == null) {
            log.error("_message=\"getCustomer with customerId returned null\" customerId={}", customerId);
            return null;
        }

        return getShipOrderIntegrationOptinUser(customerGUID);
    }


    public String getUserGuidFromUid(Long customerId) {
        String customerGUID = null;
        final int maxRetryCount = getPropertyAsInt("customer.guid.retryCount", 1);

        SHAPIContext context = SHAPIThreadLocal.getAPIContext();

        context.setSignedJWTAssertion(context.getSignedJWTAssertion());


        List<Object> providers = new ArrayList<>();
        ObjectMapper userGuidObjectMapper = new ObjectMapper();
        userGuidObjectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        userGuidObjectMapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, false);

        JacksonJaxbJsonProvider j = new JacksonJaxbJsonProvider();
        j.setMapper(userGuidObjectMapper);
        providers.add(j);


        for (int i = 0; i <= maxRetryCount; i++) {
            try {

                String customerGUIDUrl = getProperty("customer.guid.api.url", "https://api-int.stubprod.com/user/customers/v2/{customerId}/guid");
                if (!StringUtils.isBlank(customerGUIDUrl))
                    customerGUIDUrl = customerGUIDUrl.replaceAll("\\{customerId\\}", String.valueOf(customerId));
                log.info("_message=\"calling getCustomerGuid \" url={}", customerGUIDUrl);

                WebClient webClient = getWebClient(customerGUIDUrl, providers);

                SHMonitor mon = SHMonitorFactory.getMonitor();
                Response response = null;
                try {
                    mon.start();
                    response = webClient.get();
                } finally {
                    mon.stop();
                    log.info(SHMonitoringContext.get() + " _operation=getUserGuidFromUid" + " _message= service call for customerId=" + customerId + "  _respTime=" + mon.getTime());
                }

                if (Response.Status.OK.getStatusCode() == response.getStatus()) {
                    log.info("_message=\"getCustomer with customerId succeeded\" customerId={}", customerId);
                    InputStream is = (InputStream) response.getEntity();
                    JsonNode json = userGuidObjectMapper.readTree(is);
                    if (json != null) {
                        JsonNode customerNode = json.get("customer");
                        if (customerNode != null)
                            customerGUID = (customerNode.get("userCookieGuid")).getTextValue();
                    }
                }
                break;
            } catch (Exception e) {
                SHAPIThreadLocal.set(context);
                if (i == maxRetryCount) {
                    log.error("_message=\"unknown exception while making getCustomer call\" customerId={}", customerId, e);
                    SHRuntimeException shException = new SHSystemException("An internal processing error occurred in the system");
                    shException.setErrorCode("inventory.listings.systemError");
                    throw shException;
                }

            }
        }
        if (StringUtils.isBlank(customerGUID)) {
            log.error("_message=\"unknown exception while making getCustomer call\" customerId={}", customerId);
            SHBadRequestException shException = new SHBadRequestException("Unable to find GUID for " + customerId);
            shException.setErrorCode("inventory.listings.badRequest");
            throw shException;
        }
        return customerGUID;
    }

    private WebClient getWebClient(String customerGUIDUrl, List<Object> providers) {

        WebClient webClient = createWebClient(customerGUIDUrl, providers);
        webClient.accept(MediaType.APPLICATION_JSON);
        webClient.header("Content-Type", MediaType.APPLICATION_JSON);
        ClientConfiguration config = webClient.getConfig(webClient);
        config.getInInterceptors().add(new LoggingInInterceptor());
        config.getOutInterceptors().add(new LoggingOutInterceptor());

        SHServiceContext shServiceContext = (SHServiceContext) SHThreadLocals.get(SHServiceContext.SERVICE_CONTEXT_HEADER);

        if (shServiceContext != null && shServiceContext.getAttributeMap() != null) {
            String contextAsString = shServiceContext.getAttributeMap().toString();
            if (contextAsString != null) {
                webClient.header("X-SH-Service-Context", contextAsString);
            }

        }
        return webClient;
    }


    public GetCustomerResponse getShipOrderIntegrationOptinUser(String customerGUID) {

        GetCustomerResponse customer = null;

        SHAPIContext context = SHAPIThreadLocal.getAPIContext();
        final int maxRetryCount = getPropertyAsInt("customer.guid.retryCount", 1);
        List<Object> providers = new ArrayList<>();
        ObjectMapper customerGUIDObjectMapper = new ObjectMapper();

        customerGUIDObjectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        customerGUIDObjectMapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, true);
        JacksonJaxbJsonProvider j = new JacksonJaxbJsonProvider();
        j.setMapper(customerGUIDObjectMapper);
        providers.add(j);

        for (int i = 0; i <= maxRetryCount; i++) {
            try {

                String customerGUIDUrl = getProperty("customer.details.api.url", "https://api-int.stubprod.com/user/customers/v2/{customerGUID}");
                if (!StringUtils.isBlank(customerGUIDUrl))
                    customerGUIDUrl = customerGUIDUrl.replaceAll("\\{customerGUID}", customerGUID);
                log.info("_message=\"calling getCustomerDetails for isShipOptin \" url={}", customerGUIDUrl);


                WebClient webClient = getWebClient(customerGUIDUrl, providers);
                webClient.accept(MediaType.APPLICATION_JSON);

                SHMonitor mon = SHMonitorFactory.getMonitor();
                Response response = null;
                try {
                    mon.start();
                    response = webClient.get();
                } finally {
                    mon.stop();
                    log.info(SHMonitoringContext.get() + " _operation=getShipOrderIntegrationOptinUser" + " _message= service call for customerGUID=" + customerGUID + "  _respTime=" + mon.getTime());
                }

                if (Response.Status.OK.getStatusCode() == response.getStatus()) {
                    log.info("_message=\"getCustomerV2 with customerGUID succeeded\" customerGUID={}", customerGUID);
                    InputStream is = (InputStream) response.getEntity();
                    customer = customerGUIDObjectMapper.readValue(is, GetCustomerResponse.class);
                }
                break;
            } catch (Exception e) {
                SHAPIThreadLocal.set(context);
                if (i == maxRetryCount) {
                    log.error("_message=\"unknown exception while making getCustomerV2 call\" customerGUID={}", customerGUID, e);
                    SHRuntimeException shException = new SHSystemException("An internal processing error occurred in the system");
                    shException.setErrorCode("inventory.listings.systemError");
                    throw shException;
                }
            }
        }

        if (customer == null) {
            log.error("_message=\"unknown exception while making user/customers/v2/{} call\"", customerGUID);
            SHBadRequestException shException = new SHBadRequestException("An internal processing error occurred in the system");
            shException.setErrorCode("inventory.listings.badRequest");
            throw shException;
        }
        return customer;
    }

}