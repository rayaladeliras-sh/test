package com.stubhub.domain.inventory.listings.v2.helper;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.common.exception.RecordNotFoundException;
import com.stubhub.domain.fulfillment.window.v1.intf.EventFulfillmentWindowResponse;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.infrastructure.config.client.core.SHConfig;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.biz.v2.impl.util.FulfillmentServiceAdapter;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.biz.v2.intf.PDFTicketMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.entity.ErrorEnum;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.common.util.ListingException;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.FulfillmentMethod;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TaxpayerStatusEnum;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.adapter.ListingRequestAdapter;
import com.stubhub.domain.inventory.listings.v2.adapter.ListingResponseAdapter;
import com.stubhub.domain.inventory.listings.v2.entity.ListingCheck;
import com.stubhub.domain.inventory.listings.v2.entity.SeatProduct;
import com.stubhub.domain.inventory.listings.v2.enums.ConfirmOptionEnum;
import com.stubhub.domain.inventory.listings.v2.enums.GeneralAdmissionI18nEnum;
import com.stubhub.domain.inventory.listings.v2.nlproc.CommonTasks;
import com.stubhub.domain.inventory.listings.v2.nlproc.ListingData;
import com.stubhub.domain.inventory.listings.v2.tasks.BusinessInfoUpdateTask;
import com.stubhub.domain.inventory.listings.v2.tasks.ContactUpdateTask;
import com.stubhub.domain.inventory.listings.v2.tasks.DeliveryAndFullfilmentOptionsTask;
import com.stubhub.domain.inventory.listings.v2.tasks.LookupExternalIdTask;
import com.stubhub.domain.inventory.listings.v2.tasks.PaymentTypeUpdateTask;
import com.stubhub.domain.inventory.listings.v2.tasks.SellerCCIdUpdateTask;
import com.stubhub.domain.inventory.listings.v2.tasks.UpdateBarcodeSeatsTask;
import com.stubhub.domain.inventory.listings.v2.tasks.VerifySthInventoryTask;
import com.stubhub.domain.inventory.listings.v2.util.ErrorUtils;
import com.stubhub.domain.inventory.listings.v2.util.EventHelper;
import com.stubhub.domain.inventory.listings.v2.util.FulfillmentServiceHelper;
import com.stubhub.domain.inventory.listings.v2.util.InventorySolrUtil;
import com.stubhub.domain.inventory.listings.v2.util.JMSMessageHelper;
import com.stubhub.domain.inventory.listings.v2.util.ListingSeatTraitsHelper;
import com.stubhub.domain.inventory.listings.v2.util.ListingWrapper;
import com.stubhub.domain.inventory.listings.v2.util.PaymentHelper;
import com.stubhub.domain.inventory.listings.v2.util.PrimaryIntegrationUtil;
import com.stubhub.domain.inventory.listings.v2.util.SeatProductsContext;
import com.stubhub.domain.inventory.listings.v2.util.SeatProductsManipulator;
import com.stubhub.domain.inventory.listings.v2.util.SellerHelper;
import com.stubhub.domain.inventory.listings.v2.util.TicketMediumsManipulator;
import com.stubhub.domain.inventory.listings.v2.util.TicketSeatUtils;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.domain.inventory.listings.v2.validator.ListingRequestValidator;
import com.stubhub.domain.inventory.metadata.v1.event.util.SellerPaymentUtil;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.domain.inventory.v2.enums.ProxyRoleTypeEnum;
import com.stubhub.domain.pricing.intf.aip.v1.enums.ListingSourceEnum;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponse;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.common.util.DateUtil;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component("updateListingHelperTaskMgr2")
public class UpdateListingAsyncHelper2 {

	private final static Logger log = LoggerFactory
			.getLogger(UpdateListingAsyncHelper2.class);
	
	@Autowired
	private InventoryMgr inventoryMgr;

	@Autowired
	private EventHelper eventHelper;

	@Autowired
	private JMSMessageHelper jmsMessageHelper;

	@Autowired
	private UserHelper userHelper;

	@Autowired
	private PaymentHelper paymentHelper;

	@Autowired
	private ListingPriceDetailsHelper listingPriceDetailsHelper;

	@Autowired
	private ListingSeatTraitsHelper listingSeatTraitsHelper;

	@Autowired  
	private TicketSeatMgr ticketSeatMgr; 

	@Autowired  
	private PDFTicketMgr pdfTicketMgr;

	@Autowired
	private FulfillmentServiceHelper fulfillmentServiceHelper;
	
	@Autowired
	private FulfillmentServiceAdapter fulfillmentServiceAdapter;

	@Autowired
	private ListingPriceUtil listingPriceUtil;

	@Autowired
	private InventorySolrUtil inventorySolrUtil;
	
	@Autowired
	private SellerHelper sellerHelper;
	
	@Autowired
	private PrimaryIntegrationUtil primaryIntegrationUtil;
	
	@Autowired
	private ListingSeatTraitMgr listingSeatTicketManager;

	@Autowired
	private ListingFulfilmentHelper listingFulfilHelper;
	
	@Autowired
	private ListingRequestValidator listingRequestValidator;
	
	@Autowired
	private SHConfig shConfig;
	
	private static final Integer LMS_PENDING_APPROVAL = 1;
	private static final Integer SELL_IT_NOW = 1;
	private static final Integer LMS_APPROVED = 2;
	public final static Integer CONFIRM_OPTION_MANUAL = 3;
	private static final Long FM_BARCODE_PRE = 2L;
	private static final String SHIP_ABL = "|SHIP_ABL|";
	private static final String RELIST = "RELIST";
	private static final String PENDING_LOCK = "PENDING LOCK";

	private static final int MAX_CHAR = 1000;

	private ThreadPoolTaskExecutor threadPoolTaskExecutor;

	@PostConstruct
	public void setup() {
		threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setMaxPoolSize(MasterStubHubProperties
				.getPropertyAsInt("update.listing.max.poolsize", 50));
		threadPoolTaskExecutor.setKeepAliveSeconds(MasterStubHubProperties
				.getPropertyAsInt("update.listing.threadpool.timeout", 15));
		threadPoolTaskExecutor.setCorePoolSize( 20 );
		threadPoolTaskExecutor.setQueueCapacity( 20 );
		threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
		threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
		threadPoolTaskExecutor.setThreadGroup(Thread.currentThread()
				.getThreadGroup());
		threadPoolTaskExecutor.afterPropertiesSet();
		threadPoolTaskExecutor.initialize();
	}
	
	/**
	 * INTERNAL METHOD ONLY for creating / updating a listing from listingData (this is NOT exposed as an end-point for now)
	 * @param ldata
	 * @param clientIp TODO
	 * @param userAgent TODO
	 * 
	 * @return list of ListingResponse objects
	 */
	public List<ListingResponse> createOrUpdateListingData ( ListingData ldata, String clientIp, String userAgent) 
			throws Exception
	{
		// start monitor
		SHMonitor mon = SHMonitorFactory.getMonitor().start();	

		Integer currentLmsApprovalStatus = null;
		if (ldata.getCurSingleListing() != null ) {
			currentLmsApprovalStatus = ldata.getCurSingleListing().getLmsApprovalStatus();
		}
		// now process the passed requests 
		List<ListingRequest> reqList = ldata.getRequestBodies();
		
		// perform common async tasks (only once)
		performCommonAsyncTasks ( ldata );

		// create number of responses in list
		ListingResponse[] respArray = new  ListingResponse[ reqList.size() ];
		
		// batch lists
		int batchSize = ldata.getBatchSize();
		ArrayList<Listing> batchListingList = new ArrayList<Listing>(batchSize);
		
		// list for all verify barcode requests
		ArrayList<SeatProductsContext> verifyBarcodesList = new ArrayList<SeatProductsContext>();
		ArrayList<Integer> verifyBarcodesIdxs = new ArrayList<Integer>(); 
		
		// list for all verify STH inventory requests
		ArrayList<SeatProductsContext> verifySthInventoryList = new ArrayList<SeatProductsContext>();
		ArrayList<Integer> verifySthInventoryIdxs = new ArrayList<Integer>();
		
		//list for all pdf tickets 
		ArrayList<SeatProductsContext> verifyPdfInventoryList = new ArrayList<SeatProductsContext>();
		ArrayList<Integer> verifyPdfFileInfoIds= new ArrayList<Integer>();
		
		Set<String> flashMemberIds = null;
		
		// listings db updates  
		int[] batchListingListIdx = new int [batchSize];
		
		// listings db updates list
		List<Listing> dbListingToUpdateList = new ArrayList<Listing>(batchSize);
		
		boolean isCreate = ldata.isCreateRequest();
		int idx = 0;
		for ( idx=0; idx<reqList.size(); idx++ ) {
			
			ListingRequest req = reqList.get( idx );

			if(req.getListingId() != null) { //this is an update request
				checkForLMSlisting(req, ldata.getCurListing(req.getListingId()));
			}
			
			boolean isEticket = false;
			
			/*SELLAPI-3300*/

			if (isCreate && !(ldata.getSubscriber().toUpperCase().contains(SHIP_ABL) || ldata.getSubscriber().toUpperCase().contains(RELIST))) {
				isEticket = validateforSeats(req.getProducts(), ldata.getHeaderListing().getEvent().getId());
			}
			/* SELLAPI-3271 */
			if(req.getProducts() != null)
			{   
				long quantity = 0;
				if (isCreate)
					quantity = req.getProducts().size();
				else {
					Listing dbListing = ldata.getCurListing(req.getListingId());
					if (dbListing != null && dbListing.getQuantityRemain() != null) {
						quantity = dbListing.getQuantityRemain();
						// If parking Pass Increase the quantity 
						if (dbListing.getListingType() != null && dbListing.getListingType().intValue() == 3)
							quantity++;
					}
				}
				validateforProducts(req.getProducts(),quantity);
				// Reject a listing with fulfillmentArtifact and without seat number
				validateSeatsAndExternalIDForProducts(req.getProducts());
				
			}
			
			// convert req to listing (and check for any errors)
			ListingResponse errorResponse = new ListingResponse();
			if (req.getListingId() != null) {
				errorResponse.setId(req.getListingId().toString());
			} else {
				errorResponse.setId("E:" + req.getExternalListingId());
			}
			
			Listing listing = null;
            try{
                  listing = getListingFromRequest (ldata, req, errorResponse, clientIp, userAgent );
            }catch(ListingBusinessException lbe){
                  if(reqList.size() == 1)
                                throw lbe;
                 
                  ArrayList<ListingError> el = new ArrayList<ListingError>();
                  el.add ( lbe.getListingError() );
                  errorResponse.setErrors(el);
            }
			
			// NOTE: If listing == null, the errorResponse will contain validation errors response
			if ( listing == null ) {
				respArray [idx] = errorResponse;
			}
			else {
				if(ldata.isCreateRequest() && !listing.isPredeliveryAvailable()) {
					if(req.getProducts() != null && !req.getProducts().isEmpty()) {
						for(Product product : req.getProducts()) {
							product.setFulfillmentArtifact(null);
						}
					}
					listing.setDeliveryOption(DeliveryOption.MANUAL.getValue());
				}
				// add listing to batch
				batchListingList.add( listing );
				
				// save idx and get back to know the position (because batch size might be < #requests)
				batchListingListIdx [ batchListingList.size()-1 ] = idx;				
			}
	
			long aipTotalTime=0;
			
			// perform the batch pricing work for all the batchListingList group
			if ( batchListingList.size()>0 && ( batchListingList.size() == batchSize || idx == reqList.size()-1 )) {
				long aipStartTime = System.currentTimeMillis();
				
				// call pricing for whole batch and return errors (if any)
				ListingError [] results = performPricingRequests (ldata.getApiContext(), 
						ldata.getCurListingsMap(), batchListingList, reqList);
				aipTotalTime=aipTotalTime+(System.currentTimeMillis()- aipStartTime);
				
				// get as many results.length as batchSize
				for ( int i=0; i<results.length; i++ ) {
					Listing listingNew = batchListingList.get(i);
					Listing currentListing =ldata.getCurListing(listingNew.getId());
					boolean isPDF = false;
					boolean isMobile = false;
					boolean isArtifactExists = hasFulfillmentArtifact(req);
					
					Integer ticketMedium = listingNew.getTicketMedium();
					if(ticketMedium == null){
						ticketMedium = currentListing.getTicketMedium();
					}
					if(ticketMedium != null && ticketMedium.equals(TicketMedium.PDF.getValue())){
						isPDF = true;
					}
					if(ticketMedium != null && ticketMedium.equals(TicketMedium.MOBILE.getValue())){
						isMobile = true;
					}
					
					// if no error (everything ok create listing)
					if ( results[ i ] == null ) {

						// actual create listing
						try {
							//ListingRequest nextReq = reqList.get( (idx+1-batchListingList.size()) + i );
						    ListingRequest nextReq;
						    if(reqList.size() == 1) {
						        nextReq = reqList.get(0);
						    } else {
						        nextReq = ldata.getRequestFromMap(listingNew.getExternalId());
								if (nextReq == null)
									nextReq = reqList.get(batchListingListIdx[i]);
						    }
							listingNew.setSubscriber(ldata.getSubscriber());
							ListingWrapper listingWrapper = updateOrCreateSingleListing (listingNew, 
									ldata.getCurListing(nextReq.getListingId()), nextReq,  
									ldata.getApiContext(), null );

							// batch DB update
							listingNew = listingWrapper.getListing();
							
							if(listingNew.getLmsApprovalStatus() != null && listingNew.getLmsApprovalStatus() == 1 && ListingStatus.ACTIVE.toString().equalsIgnoreCase(listingNew.getSellerRequestedStatus())) {
							    listingNew.setSendLmsMessage(true);
							}

							if (isCreate && ldata.getHeaderRequest().getEvent()!=null) {
								String description = "eventInfo:";
								if (ldata.getHeaderRequest().getEvent().getName()!=null && ldata.getHeaderRequest().getEvent().getVenue()!=null) {
									description=description+ldata.getHeaderRequest().getEvent().getName()+"|"+ldata.getHeaderRequest().getEvent().getVenue();
								}
								if (ldata.getHeaderRequest().getEvent().getEventLocalDate()!=null) {
									description = description+"|" + ldata.getHeaderRequest().getEvent().getEventLocalDate();
								}else if (ldata.getHeaderRequest().getEvent().getDate()!=null) {
									description = description+"|"+ ldata.getHeaderRequest().getEvent().getDate();
								}				
								int maxLength = (description.length() < MAX_CHAR)?description.length():MAX_CHAR;
								listingNew.setDescription(description.substring(0,maxLength));
							}
							
							if(isCreate && listingNew.getTicketMedium()!=null && listingNew.getTicketMedium().equals(TicketMedium.FLASHSEAT.getValue())) {
								if(listingNew.getDeliveryOption() == DeliveryOption.PREDELIVERY.getValue()) {
									listingNew.setInhandDate(DateUtil.getNowCalUTC());
									if (ListingStatus.ACTIVE.toString().equalsIgnoreCase(listingNew.getSellerRequestedStatus())
											|| ListingStatus.INACTIVE.toString().equalsIgnoreCase(listingNew.getSellerRequestedStatus())) {
										listingNew.setSystemStatus(ListingStatus.PENDING_LOCK.toString());
									}else if(ListingStatus.HIDDEN.toString().equalsIgnoreCase(listingNew.getSellerRequestedStatus())){
										listingNew.setSystemStatus(ListingStatus.HIDDEN.toString());
										
									}
								}
							}
							
							if(listingNew.getId() == null) {
								listingNew.setCreatedBy(ldata.getSubscriber());
							} else {
								listingNew.setLastUpdatedBy(ldata.getSubscriber());
							}

							log.debug("businessId=" + listingNew.getBusinessId() + " businessGuid=" + listingNew.getBusinessGuid());
							dbListingToUpdateList.add ( listingNew );
							
							if(com.stubhub.domain.inventory.common.entity.DeliveryOption.STH == nextReq.getDeliveryOption()) {
								verifySthInventoryList.add(listingWrapper.getContext());
								verifySthInventoryIdxs.add( new Integer(i) );
							}
							// add to verify barcode list (if applicable)
							else if ( listingWrapper.getContext().getBarcodeSeatProductList()!=null && 
									listingWrapper.getContext().getBarcodeSeatProductList().size()> 0 && !isPDF) {
								verifyBarcodesList.add( listingWrapper.getContext() );
								verifyBarcodesIdxs.add( new Integer(i) );
								
								// see if we need to change listing status (before we store in DB)
								listingWrapper.getContext().checkSetListingStatus ();
							}
							//if the ticket medium is pdf and input products contains fulfilment artifact
							else if(isArtifactExists && (isPDF || isMobile)){
								verifyPdfInventoryList.add(listingWrapper.getContext());
								verifyPdfFileInfoIds.add(new Integer(i));
							}
							
							flashMemberIds = listingWrapper.getContext().getFlashFulfillmentArtifactIds();
						
						}
						catch (Exception ex) { 
							mon.stop();
							log.error("exception", ex);
							int j = batchListingListIdx [i];
							handleError ( ex, listingNew, ldata.getSellerId(), j, mon.getTime(), respArray );
						}
					}
					else { // get the actual index and set error
						int j = batchListingListIdx [i];
						respArray [ j ] = ErrorUtils.respFromError(listingNew, results[i] );
					}
				}
				boolean isValidFileInfoIds =false;
				// commit and return responses
				boolean batchCommitFailure = false;
				List<Listing> updatedListings = null;
				try {
					
					if(isCreate) {
						for(Listing l : dbListingToUpdateList) {
							if(ListingStatus.ACTIVE.toString().equalsIgnoreCase(l.getSystemStatus())) {
								l.setSystemStatus(ListingStatus.INCOMPLETE.toString());
								l.setStatusUpdated(true);
							}
						}
					}
					
					// DO DB batch Updates (had to do the create listing first before adding the seats)
					//Superfluous updates on tickets table - Avoid DB Update during PUT + Barcodes
					if(!isCreate && verifyBarcodesIdxs.size() > 0)	
						updatedListings = dbListingToUpdateList;
					else
						updatedListings = updateDatabaseForBatch ( ldata.isCreateRequest(), dbListingToUpdateList);
					
					
				}
				catch ( Throwable th ) {
					batchCommitFailure = true;
					log.error("Batch create commit failure", th );
				}

				/* SELLAPI-3243 - Moved the lock call after DB Update */ 
				for(Listing dbListing:dbListingToUpdateList)
				{
					if (ListingStatus.PENDING_LOCK.toString().equals(dbListing.getSystemStatus()) &&
							dbListing.isLockMessageRequired()) {
						jmsMessageHelper.sendLockInventoryMessage(dbListing.getId());
					}
				}
				boolean isValidateBarcode=true;
				//have another loop 
				//call getBarcodeSeatProductLid 
				//call getTicketseats 
				// do verify barcodes for relevant listings (if no db failures)
				List<FulfillmentWindow> fulfillmentWindows = null;
				if ( !batchCommitFailure && dbListingToUpdateList.size()>0 ) {
					boolean isBarcodeListing = false;
					boolean barcodeUpdateSuccess = true;
					if(verifyBarcodesList.size() > 0 && !isCreate) {
						EventFulfillmentWindowResponse efwResponse = fulfillmentServiceAdapter.getFulfillmentWindowsShape(listing.getEventId(), 
								listing.getSellerContactId());
						fulfillmentWindows = fulfillmentServiceAdapter.getFulfillmentWindows(efwResponse);
						List<Long> fulfillmentMethodIds = new ArrayList<Long>();
						if(fulfillmentWindows != null) {
							for (FulfillmentWindow fw : fulfillmentWindows) {
								fulfillmentMethodIds.add(fw.getFulfillmentMethodId());
							}
						}
						if(!fulfillmentMethodIds.contains(FM_BARCODE_PRE)) {
							//SELLAPI-3653 No Pre-delivery Window, persist Barcode to use later.
							String isPersistBarcodeWithoutWindow = shConfig.getProperty("persistBarcodeWithoutWindow", "true");
							if (Boolean.parseBoolean(isPersistBarcodeWithoutWindow)){
								isValidateBarcode=false;
							}else
							{
								ListingError listingError = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.DELIVERY_OPTION_NOT_SUPPORTED, ErrorEnum.DELIVERY_OPTION_NOT_SUPPORTED.getMessage(), "deliveryOption");
								throw new ListingBusinessException(listingError);
							}
							log.info("message=\"Persisting Barcode without preDelivery Window\" createdBy={} listingId={} eventId={} sellerId={} persistBarcodeWithoutWindow={} validateBarcode={}",
									dbListingToUpdateList.get(0).getCreatedBy(),listing.getId(),listing.getEventId(),listing.getSellerId(),isPersistBarcodeWithoutWindow,isValidateBarcode);
						}
					}
					for ( int k=0; k<verifyBarcodesList.size(); k++ ) {
						isBarcodeListing=true;
						SeatProductsContext seatProdCtx = verifyBarcodesList.get(k);
						try {
							//verify barcode if no pre-delivery window
							seatProdCtx.setValidateBarcode(isValidateBarcode);
							// resolve all ticketSeatIds 
							seatProdCtx.resolveBarcodeSeatProductsList();
						 
							// call verify barcode task
							UpdateBarcodeSeatsTask barcodeTask = new UpdateBarcodeSeatsTask(seatProdCtx, 
									ldata.getApiContext(), primaryIntegrationUtil);
							
							barcodeTask.call();
						}
						catch ( Exception ex ) {
							mon.stop();
							log.error("exception", ex);
							barcodeUpdateSuccess = false;
							int j = batchListingListIdx [ verifyBarcodesIdxs.get(k) ];
							handleError ( ex, seatProdCtx.getCurrentListing(), ldata.getSellerId(), j, mon.getTime(), respArray );
							// rollback
							if ( !seatProdCtx.isCreate() )
								seatProdCtx.rollbackAllNewlyAddedTicketSeats();
							else 
								seatProdCtx.rollbackCurrentListing (inventoryMgr);
						}
					}

					//verify fileinfo ids for pdf listings
					boolean fileInfoValidationFailed = false; //SELLAPI-956 10/13/15
					for ( int k=0; k<verifyPdfInventoryList.size(); k++ ) {
						SeatProductsContext seatProdCtx = verifyPdfInventoryList.get(k);

						try {
							//verify fileinfo helper
							isValidFileInfoIds = listingFulfilHelper.validateFileInfoIds(seatProdCtx, listing);
							if (!isValidFileInfoIds) {
								fileInfoValidationFailed = true; //SELLAPI-956 10/13/15
								log.error("error_message=\"Error Validating FileInfo ids from Fulfillment api\"" + " listingId={}", listing.getId());
								ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_PDF_FILE, ErrorEnum.INVALID_PDF_FILE.getMessage(), "");
								throw new ListingBusinessException(listingError);
							}
						}
						catch ( Exception ex ) {
							mon.stop();
							log.error("exception", ex);
							int j = batchListingListIdx [ verifyPdfFileInfoIds.get(k) ];
							handleError ( ex, seatProdCtx.getCurrentListing(), ldata.getSellerId(), j, mon.getTime(), respArray );
							// rollback
							if ( !seatProdCtx.isCreate() )
								seatProdCtx.rollbackAllNewlyAddedTicketSeats();
							else 
								seatProdCtx.rollbackCurrentListing (inventoryMgr);
						}
					}
					
					if (fileInfoValidationFailed) {
						continue; //if current listing file info process fails, continue to process next listing
					}
					if(isValidFileInfoIds || (isBarcodeListing && barcodeUpdateSuccess && isValidateBarcode)) {
						for(Listing listingToUpdate : updatedListings) {
							listingToUpdate.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
							listingToUpdate.setConfirmOption(ConfirmOptionEnum.AUTO_CONFIRM.getConfirmStatus());
							if (isBarcodeListing) {
								listingToUpdate.setFulfillmentMethod(FulfillmentMethod.BARCODE);
							} else if (listingToUpdate.getTicketMedium() != null && listingToUpdate.getTicketMedium().equals(TicketMedium.MOBILE.getValue())) {
								listingToUpdate.setFulfillmentMethod(FulfillmentMethod.MOBILE);
							} else {
								listingToUpdate.setFulfillmentMethod(FulfillmentMethod.PDF);
							}
							listingToUpdate.setInhandDate(null);
							listingToUpdate.setEndDate(null);
							CommonTasks commonTasks = new CommonTasks();
							DeliveryAndFullfilmentOptionsTask fmDmTask = new DeliveryAndFullfilmentOptionsTask(listingToUpdate,
							listingToUpdate, ldata.getApiContext(), fulfillmentServiceHelper, fulfillmentServiceAdapter, ldata.isCreateRequest(), ldata.isBulkRequest(), fulfillmentWindows);
							commonTasks.addRecalllableTask(fmDmTask);
							commonTasks.setListingValues(listingToUpdate, listingToUpdate);
							
							listingToUpdate.setInhandDate(DateUtil.getNowCalUTC());

							if (listingToUpdate.getSellerRequestedStatus() == null
									|| ListingStatus.ACTIVE.toString().equalsIgnoreCase(listingToUpdate.getSellerRequestedStatus())
									|| ListingStatus.INACTIVE.toString().equalsIgnoreCase(listingToUpdate.getSellerRequestedStatus())) {
								if (isBarcodeListing) {
									//SELLAPI-4209
									if(isEticket)
										listingToUpdate.setIsETicket(isEticket);
									listingToUpdate.setSystemStatus(ListingStatus.PENDING_LOCK.toString());
								} else {
									listingToUpdate.setSystemStatus(ListingStatus.ACTIVE.toString());
								}
							}
						}
					}
					if(!isValidateBarcode){ //SELLAPI-3653 - Persisting Barcode without Pre-delivery window, updating tickets table status column with value=3
						int status = 3;
						for(Listing listingToUpdate : updatedListings){
							listingToUpdate.setStatus((short) status);
						}
					}
                    for (Listing updatedlisting : updatedListings) {
                      try {
                        if (ListingStatus.PENDING_LOCK.toString().equals(updatedlisting.getSystemStatus())
                            && updatedlisting.getTicketMedium()
                                .intValue() == com.stubhub.domain.inventory.v2.enums.TicketMedium.FLASHSEAT
                                    .getId()) {
                          String memberIds = null;
                          if(flashMemberIds != null) {
                            memberIds = org.springframework.util.StringUtils.collectionToCommaDelimitedString(flashMemberIds);
                          }
                          jmsMessageHelper.sendLockInventoryMessage(updatedlisting.getId(), memberIds);
                        }
                      } catch (Exception e) {
                        log.error(
                            "error_message=\"Unable to push pending lock message for flashseat listing\""
                                + " listingId=" +
                            listing.getId(), e);
          
                      }
                    }
                    
                    for (Listing updatedlisting : updatedListings) {
                      try {
                        if (ListingStatus.DELETED.toString().equals(updatedlisting.getSystemStatus())) {
                          if ((TicketMedium.BARCODE.getValue() == updatedlisting.getTicketMedium() || TicketMedium.FLASHSEAT.getValue() == updatedlisting.getTicketMedium())
                              && DeliveryOption.PREDELIVERY.getValue() == updatedlisting.getDeliveryOption()) {
                            log.debug("Barcode/Flashseat predelivered listing, sending unlock inventory message for listingId="+ updatedlisting.getId());
                            jmsMessageHelper.sendUnlockInventoryMessage(updatedlisting.getId());
                          }
                        }
                      } catch (Exception e) {
                        log.error(
                            "error_message=\"Unable to send unlock inventory message for deleted listing\""
                                + " listingId=" +
                            updatedlisting.getId(), e);
          
                      }
                    }
					
					try {
					  

			            
						for (int k = 0; k < verifySthInventoryList.size(); k++) {
							SeatProductsContext seatProdCtx = verifySthInventoryList.get(k);

							// resolve all ticketSeatIds
							seatProdCtx.resolveBarcodeSeatProductsList();
						}

                        if(verifySthInventoryList!=null && verifySthInventoryList.size()>0){
							// call verify barcode task
							VerifySthInventoryTask sthTask = new VerifySthInventoryTask(
									verifySthInventoryList, ldata.getApiContext(),
									primaryIntegrationUtil);
	
							sthTask.call();
	
							for(Listing listingToUpdate : updatedListings) {
								listingToUpdate.setDeliveryOption(DeliveryOption.PREDELIVERY.getValue());
								listingToUpdate.setInhandDate(DateUtil.getNowCalUTC());
								listingToUpdate.setSystemStatus(ListingStatus.PENDING_LOCK.toString());
								listingToUpdate.setConfirmOption(ConfirmOptionEnum.AUTO_CONFIRM.getConfirmStatus());
							}
                        }
                        for(Listing l : updatedListings) {
    						if(ListingStatus.INCOMPLETE.toString().equalsIgnoreCase(l.getSystemStatus()) && l.isStatusUpdated()) {
    							l.setSystemStatus(ListingStatus.ACTIVE.toString());
    						}
    					}
						try {
							if(barcodeUpdateSuccess) {
								// DO DB batch Updates for above system status and delivery option change
								updateDatabaseForBatch(false, updatedListings);
								
								for(Listing currentListing : dbListingToUpdateList) {
									for(Listing updatedListing : updatedListings) {
										if(currentListing.getId().equals(updatedListing.getId())) {
											currentListing.setDeliveryOption(updatedListing.getDeliveryOption());
											currentListing.setInhandDate(updatedListing.getInhandDate());
											currentListing.setSystemStatus(updatedListing.getSystemStatus());
											currentListing.setConfirmOption(updatedListing.getConfirmOption());
											currentListing.setEndDate(updatedListing.getEndDate());
											currentListing.setFulfillmentDeliveryMethods(updatedListing.getFulfillmentDeliveryMethods());
											break;
										}
									}
								}
								
								for (int k = 0; k < verifySthInventoryList.size(); k++) {
									SeatProductsContext seatProdCtx = verifySthInventoryList.get(k);
									// Need to lock listing if active
									seatProdCtx.handleBarcodeListingLocks(jmsMessageHelper);
								}
								
								for (int j = 0; j < verifyBarcodesList.size(); j++) {
									SeatProductsContext seatProdCtx = verifyBarcodesList.get(j);
									// Need to lock listing if active
									seatProdCtx.handleBarcodeListingLocks(jmsMessageHelper);
								}
							}
						} catch (Throwable th) {
							batchCommitFailure = true;
							log.error("Batch update for delivery option and system status failed", th);
						}												
						
					} catch (Exception ex) {
						mon.stop();
						log.error("exception", ex);
						for (int k = 0; k < verifySthInventoryList.size(); k++) {
							SeatProductsContext seatProdCtx = verifySthInventoryList.get(k);
							int j = batchListingListIdx [verifySthInventoryIdxs.get(k)];
							handleError(ex, seatProdCtx.getCurrentListing(), ldata.getSellerId(), j, mon.getTime(), respArray);
							if (!seatProdCtx.isCreate())
								seatProdCtx.rollbackAllNewlyAddedTicketSeats();
							else
								seatProdCtx.rollbackCurrentListing(inventoryMgr);
						}
					}
					
			}
				
				// return responses
				int respIdx = 0;
				for ( int i=0; i<results.length; i++ ) {
					
					int j = batchListingListIdx [ i ];
					
					// if no error already set at that position, then set response
					if ( respArray [j ] == null ) { 
						// set response in proper order
						
						Listing l = dbListingToUpdateList.get(respIdx);
						
						if ( batchCommitFailure ) {
							respArray [ j ] = ErrorUtils.respFromError(l, new ListingError(ErrorType.SYSTEMERROR, 
									ErrorCode.SYSTEM_ERROR, "Internal System Error", "") );
						}
						else {
							respArray [ j ] = ListingResponseAdapter.getListingRespWithData ( 
									l, ldata.isCreateRequest() );
						}
						respIdx++;
					}
				}
				
				// clear batch requests
				batchListingList.clear();
				dbListingToUpdateList.clear();
				verifyBarcodesList.clear();
				verifyBarcodesIdxs.clear();
				verifyPdfInventoryList.clear();
				verifyPdfFileInfoIds.clear();
				Arrays.fill (batchListingListIdx, 0);
			}
			if(aipTotalTime>0){
				log.info("Time taken by AIP="+aipTotalTime);
			}
		}

		return new ArrayList<com.stubhub.domain.inventory.v2.DTO.ListingResponse>(
				Arrays.asList(respArray));
	}
/**
 * A helper method to check approved LMS listings. Once a listing is 
 * approved for LMS, we do not allow any changes to the listing 
 * except for reducing the quantity.
 * @param lReq
 */
	private void checkForLMSlisting(ListingRequest lReq, Listing existingListing) {
		Integer currentLmsApprovalStatus = existingListing.getLmsApprovalStatus();
		boolean isTrustedSeller = false;
		if(existingListing.getFulfillmentDeliveryMethods() != null){
			isTrustedSeller = existingListing.getFulfillmentDeliveryMethods().contains("|7,") ||
				existingListing.getFulfillmentDeliveryMethods().startsWith("7,");
		}
		if(currentLmsApprovalStatus ==  LMS_APPROVED && !isTrustedSeller) {
			if(lReq.getProducts() != null){
			List<Product> products = lReq.getProducts();
			for ( Product product : products) {
				if (product.getOperation().equals(Operation.ADD) || product.getOperation().equals(Operation.UPDATE)) {
					
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR, ErrorCode.LISTING_ACTION_NOTALLOWED, "Can not add new seats to already approved LMS listing id= " + lReq.getListingId(),
							"listingId");
					log.error ("Can not add new seats to already approved LMS listing id=" + lReq.getListingId());
					throw new ListingBusinessException(listingError);				}
			}
			} else {
				if(lReq.getQuantity() != null &&  lReq.getQuantity() > existingListing.getQuantityRemain()){
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR, ErrorCode.LISTING_ACTION_NOTALLOWED, "Can not increase seat quantity for an already approved LMS listing id= " + lReq.getListingId(),
							"listingId");
					log.error ("Can not increase seat quantity for an already approved LMS listing id=" + lReq.getListingId());
					throw new ListingBusinessException(listingError);				
					}
			
				}
			}
	}
	private void handleError ( Exception ex, Listing l, Long sellerId, int respIdx, long respTime, ListingResponse[] respArray  )
	{
		if ( ex instanceof ListingBusinessException ) {
			ListingError lerror = ((ListingBusinessException)ex).getListingError();
			respArray [ respIdx ] = ErrorUtils.respFromError(l, lerror);
			log.warn("api_domain=inventory api_resource=bulkListing"
					+ " api_method=updateListing status=success_with_error _message=\"ListingBusinessException occured while creating listing\"" 
					+ " cause: " + lerror.getCode() +", " + lerror.getMessage()
					+ " sellerId={}  _respTime={}", sellerId, respTime);
		}
		else { 
			respArray [ respIdx ] = ErrorUtils.respFromError(l, new ListingError(ErrorType.SYSTEMERROR, 
					ErrorCode.SYSTEM_ERROR, ex.getLocalizedMessage(), "") );
			log.error("api_domain=inventory api_resource=bulkListing"
					+ " api_method=updateListing status=success_with_error _message=\"ListingBusinessException occured while creating listing\""
					+ " sellerId={}  _respTime={}", sellerId, respTime);								
		}
	}
	
	private void performCommonAsyncTasks (ListingData ldata ) throws ListingBusinessException, ListingException, ExecutionException, InterruptedException
	{
		List<Future<Listing>> futures = new ArrayList<Future<Listing>>();
		
		Listing listing = ldata.getHeaderListing();	
		
		com.stubhub.domain.inventory.common.entity.DeliveryOption dOption = ldata.getHeaderRequest().getDeliveryOption();
		if(dOption != null && dOption.equals(com.stubhub.domain.inventory.common.entity.DeliveryOption.STH)) {
			listing.setListingSource(8); //STHGen3
		}
		Listing currentListing = null;
		
		// if not bulk and update mode (there should be one cur listing)
		if ( !ldata.isCreateRequest() && !ldata.isBulkRequest() )
			currentListing = ldata.getCurSingleListing();
		else 
			currentListing = ldata.getHeaderListing();
		
		SHAPIContext apiContext = ldata.getApiContext();
		SHServiceContext shServiceContext = ldata.getShServiceContext();
		
		// seller contact 
		ContactUpdateTask sellerContactTask = new ContactUpdateTask(
				listing, currentListing, apiContext, shServiceContext, userHelper, sellerHelper, ldata.isCreateRequest());
		if( sellerContactTask.ifNeedToRunTask() ) {
			futures.add(threadPoolTaskExecutor.submit(sellerContactTask));
		}
		
		// seller business info
		BusinessInfoUpdateTask businessInfoUpdateTask = new BusinessInfoUpdateTask(
				listing, currentListing, sellerHelper, apiContext, ldata.isCreateRequest());
		if ( businessInfoUpdateTask.ifNeedToRunTask() ) {
			futures.add(threadPoolTaskExecutor.submit(businessInfoUpdateTask));
		}
		
		// sellerCCID task (NEED to run automically before the PaymentTypeUpdateTask task)
		SellerCCIdUpdateTask sellerCCIDTask = new SellerCCIdUpdateTask(
				listing, currentListing, apiContext, userHelper);
		if( sellerCCIDTask.ifNeedToRunTask() ) {	
			futures.add(threadPoolTaskExecutor.submit(sellerCCIDTask));
		}
		
		// Wait here until all future tasks get methods return
		for (Future<Listing> future : futures) {
			future.get();
		}
		
		futures.clear();
		
		// PaymentTypeUpdateTask payment type update (NEED to run after the sellerCCID task)
		PaymentTypeUpdateTask paymentTask = new PaymentTypeUpdateTask(listing, currentListing, apiContext, shServiceContext, userHelper, 
				paymentHelper, ldata.isCreateRequest());
		if ( paymentTask.ifNeedToRunTask() ) {
			futures.add(threadPoolTaskExecutor.submit(paymentTask) );
		}
		
		// Wait here until all future tasks get methods return
		for (Future<Listing> future : futures) {
			future.get();
		}
		
		// after finishing all tasks execution, add re-callable tasks (so we can figure out values for individual listings)
		DeliveryAndFullfilmentOptionsTask fmDmTask = new DeliveryAndFullfilmentOptionsTask(listing,
				currentListing, apiContext, fulfillmentServiceHelper, fulfillmentServiceAdapter,
				ldata.isCreateRequest(), ldata.isBulkRequest(), null );

		ldata.addRecallableTask(fmDmTask);
	}
	
	/**
	 * Call new SHAPE-API to get batch pricing request 
	 * @param apiContext
	 * @param batchRequestList
	 * @return array of ListingError for the request or null for no error
	 * @throws Exception
	 */
	public ListingError [] performPricingRequests ( SHAPIContext apiContext, 
			Map<Long,Listing> curListingsMap, List<Listing> batchRequestList, List<ListingRequest> originalListingRequests ) throws Exception
	{		
		Object [] responses = listingPriceDetailsHelper.batchPriceCalculationsAIP (apiContext, curListingsMap, batchRequestList , originalListingRequests);
		ListingError [] errors = new ListingError [responses.length];
		
		for ( int i=0; i<responses.length; i++ ) {
			
			// if responses[i] == null (means pricing is N/A to listing, simply ignore) 
			if ( responses[i] != null ) {
				if ( responses[i] instanceof PriceResponse ) {
					PriceResponse priceResp = (PriceResponse) responses[i];
					errors[i] = null;
					Listing l = batchRequestList.get(i);
					// set all prices in listing (get cur listing if available)
					Listing curListing = null;
					if ( curListingsMap != null )
						curListing = curListingsMap.get(l.getId());
					listingPriceDetailsHelper.setPricingValues (l, curListing, priceResp);
					
				}
				else {
					ListingError le = (ListingError)responses [i];
					
					Listing listing = batchRequestList.get(i);
					if(ErrorCode.LISTING_PRICE_TOO_LOW.equals(le.getCode()) && listing.isAdjustPrice()) {
						List<Listing> requestList = new ArrayList<Listing>();
						String errorParam = le.getParameter();
						int startIndexAmt = errorParam.indexOf("amount=");
						int endIndexAmt = errorParam.indexOf(", currency");
						String amount = errorParam.substring(startIndexAmt+7, endIndexAmt);
						int startIndexCur = errorParam.indexOf("currency=");
						int endIndexCur = errorParam.indexOf("]");
						String currency = errorParam.substring(startIndexCur+9, endIndexCur);
						Money minPrice = new Money(amount, currency);
						listing.setListPrice(minPrice);
						listing.setSellerPayoutAmountPerTicket(null);
						requestList.add(listing);
						Object [] responseList = listingPriceDetailsHelper.batchPriceCalculationsAIP (apiContext, curListingsMap, requestList, originalListingRequests);
						if(responseList[0] != null) {
							if(responseList[0] instanceof PriceResponse) {
								PriceResponse priceResp = (PriceResponse) responseList[0];
								errors[i] = null;
								Listing curListing = null;
								if (curListingsMap != null) {
									curListing = curListingsMap.get(listing.getId());
								}
								listing.setPriceAdjusted(true);
								listingPriceDetailsHelper.setPricingValues (listing, curListing, priceResp);
								
							} else {
								ListingError error = (ListingError) responseList[0];
								errors [i] = error;
								log.error("Pricing API Error encountered, code=" + le.getErrorCode() + ", msg=" + le.getMessage() + ", param=" + le.getParameter() );
							}
						}
					} else {
						errors [i] = le;
						log.error("Pricing API Error encountered, code=" + le.getErrorCode() + ", msg=" + le.getMessage() + ", param=" + le.getParameter() );
					}
				}
			}
		}
		return errors;
	}
	
	/**
	 * convert request to a listing. If there are errors, null is returned and error is populated in 
	 * ListingResponse
	 * @param request
	 * @param clientIp TODO
	 * @param userAgent TODO
	 * @param securityContext
	 * 
	 * @return ListingResponse
	 */
	protected Listing getListingFromRequest ( ListingData ldata, ListingRequest request,
			com.stubhub.domain.inventory.v2.DTO.ListingResponse abortResp, String clientIp, String userAgent ) 
	{
		Long sellerId = ldata.getSellerId();
		String sellerGuid = ldata.getSellerGuid();
		
		String operatorId = ldata.getOperatorId();
		ProxyRoleTypeEnum role = ldata.getRole();
		
		com.stubhub.domain.inventory.datamodel.entity.Listing listing = null;
		Event event = ldata.getHeaderListing().getEvent();
		String eventCountry = event != null ?  event.getCountry() : null;

		Currency eventCurrency = event != null ? event.getCurrency() : null;

		// Validate the listing request
		List<ListingError> errors = listingRequestValidator.validate(request, Long.valueOf(ldata.getHeaderRequest().getEventId()),
				ldata.isCreateRequest(), ldata.isBulkRequest(), ldata.getLocale(), ldata.getSubscriber(), operatorId, role, eventCountry, eventCurrency);
		
		// return errors in response if validation fails
		if (errors.size() > 0) {	
			ErrorUtils.populateRespWithErrors(abortResp, 
					request.getExternalListingId(), errors, request.getListingId());
			if ( log.isDebugEnabled() ) {
				ListingError err = errors.get(0);
				log.debug( "Listing validation error: " + err.getMessage() );
			}
			return null;
		}
		
		listing = ListingRequestAdapter.convert(request, ldata.isCreateRequest(), event);
		listing.setSellerId( sellerId );
		listing.setSellerGuid(sellerGuid);
		
		if(ldata.getSellShStoreId() != null) {
			listing.setSellShStoreId(ldata.getSellShStoreId());
		}
     
		// set event in listing (ALWAYS!! for create or update)
		listing.setEvent(event);
		if ( request.getEventId() != null ) {
			listing.setEventId( Long.valueOf(request.getEventId()) );
		}

		// add some common values (Important! do this after listing mapping from request)
		listing = ldata.addCommonListingHeaderValues(listing);

		listing.setIpAddress(clientIp);
		Listing currentListing =null;
		if ( !ldata.isCreateRequest() && !ldata.isBulkRequest() )
			currentListing = ldata.getCurSingleListing();
		else 
			currentListing = ldata.getHeaderListing();

		if(!ldata.isCreateRequest() && ldata.isBulkRequest()) {
			Long id = ldata.getRequestBodies().get(0).getListingId();
			Listing curListing = ldata.getCurListing(id);
			if(curListing != null) {
				currentListing.setTicketMedium(curListing.getTicketMedium());
			}
		}
		
		if(!ldata.isCreateRequest() && listing.getFulfillmentMethod() != null) {
            if(currentListing.getTicketMedium().equals(listing.getTicketMedium())) {
                listing.setFulfillmentMethod(null);
            }
            
            if(currentListing.getDeliveryOption() != null && currentListing.getDeliveryOption().intValue() == DeliveryOption.PREDELIVERY.getValue()
                && ListingStatus.ACTIVE.toString().equalsIgnoreCase(currentListing.getSystemStatus())) {
              listing.setFulfillmentMethod(null);
            }
        }
		
		// Set listing values with task values that were executed once for the whole listings batch
		if ( ldata.getCommonTasks() != null ) {
			 ldata.getCommonTasks().setListingValues(listing, currentListing);
		}

		if (currentListing != null) {
			log.debug("BusinessGuid={}, BusinessId={}", currentListing.getBusinessGuid(), currentListing.getBusinessId());
			listing.setBusinessGuid(currentListing.getBusinessGuid());
			listing.setBusinessId(currentListing.getBusinessId());
			
		}

		if ( StringUtils.trimToNull(userAgent) != null ) {
			if ( ldata.isCreateRequest() ) 
				listing.setCreatedByUserAgentId(inventoryMgr.getUserAgentID(userAgent));
			else 
				listing.setUpdatedByUserAgentId(inventoryMgr.getUserAgentID(userAgent));
		}

		if (ldata.isCreateRequest()) {
			log.debug("checkpoint=pre_indy_check listingSource=" + listing.getListingSource()
				+ " createdBy=" + ldata.getSubscriber());
			listing.setCreatedBy(ldata.getSubscriber());
			if (!(listing.getListingSource() != null && listing.getListingSource().equals(
				Integer.valueOf(ListingSourceEnum.STHGen3.getListingSource())))
				&& isListingSourceIndy(ldata.getSubscriber())) {
				listing.setListingSource(Integer.valueOf(ListingSourceEnum.IndyGen3.getListingSource()));
				log.info("checkpoint=set_listing_source listingSource=" + ListingSourceEnum.IndyGen3.getListingSource());

			}
			log.debug("checkpoint=post_indy_check listingSource=" + listing.getListingSource());
		}
		return listing;
	}
	
	/**
	 * Create single listing helper method
	 */
	public Listing createSingleListing (Listing listing, ListingRequest request, SHAPIContext apiContext) 
			throws RecordNotFoundException, InterruptedException, ExecutionException, ListingException 
	{
		ListingWrapper lw = updateOrCreateSingleListing(listing, null, request, apiContext, new CommonTasks() );
		return lw.getListing();
	}
	
	/**
	 * Update single listing helper method
	 */
	public Listing updateSingleListing (Listing listing, Listing curListing, ListingRequest request, SHAPIContext apiContext) 
			throws RecordNotFoundException, InterruptedException, ExecutionException, ListingException 
	{
		ListingWrapper lw = updateOrCreateSingleListing(listing, curListing, request, apiContext, new CommonTasks() );
		return lw.getListing();
	}
	
	/**
	 * @param listing object that comes from request
	 * @param request create or update request
	 * @param isCreate true if intention is create otherwise update
	 * @param apiContext api context
	 * @return Listing object to be stored in DB
	 * @throws RecordNotFoundException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws ListingException
	 */
	public ListingWrapper updateOrCreateSingleListing (Listing listing, Listing pCurrentListing, ListingRequest request, 
			SHAPIContext apiContext, CommonTasks commonTasks) 
			throws RecordNotFoundException, InterruptedException, ExecutionException, ListingException 
	{
		boolean isCreate = listing.getId() == null;
		
		Listing currentListing = pCurrentListing;
		if ( currentListing == null ) {	// if update just get listing from DB
			currentListing = listing;

			if ( !isCreate ) {
				// event is ALWAYS! set in listing (because it is lookup earlier)
				currentListing = inventoryMgr.getListing( listing.getId() );
				
				if ( listing.getEvent() != null ) {
					if ( currentListing.getEventId().equals(listing.getEvent().getId()) ) {
						currentListing.setEvent(listing.getEvent());					
					}
					else {
						ListingError listingError = new ListingError(
								ErrorType.INPUTERROR,
								ErrorCode.INVALID_EVENTID,
								"Invalid eventId passed in request not equal listing eventId",
								"eventId");
						throw new ListingBusinessException(listingError);
					}
				}
			}
		}

		log.info("create/update={} listing.Id={} currentListing.Status={} requestListing.Status={}", isCreate, currentListing.getId(),
				currentListing.getSystemStatus(), listing.getSystemStatus());

		// UPDATE: verify listing with curListing
		if ( !isCreate ) {
			verifyExistingListing(currentListing, listing);
		}
		
		// Context that contains information about Seat Product processing populated by various objects
		SeatProductsContext seatProdContext = new SeatProductsContext(currentListing, request,
				ticketSeatMgr, listingSeatTicketManager, false );

		// if update mode then handle the changes in quantities
		handleQuantityUpdates( listing, seatProdContext);
		
		// handle common listing updates (these are data items resolved at common processing time)
		if ( listing.getSellerCCId() != null )
			currentListing.setSellerCCId( listing.getSellerCCId() );
		if ( listing.getSellerContactId() != null )
			currentListing.setSellerContactId ( listing.getSellerContactId() );
		if ( listing.getSellerContactGuid() != null )
			currentListing.setSellerContactGuid(listing.getSellerContactGuid());
		if ( listing.getInhandDate() != null ){
			currentListing.setInhandDate ( listing.getInhandDate() );
			currentListing.setAdjustInhandDate(listing.isAdjustInhandDate());
			currentListing.setInHandDateAdjusted(listing.isInHandDateAdjusted());
			currentListing.setInhandDateValidated(listing.isInhandDateValidated());
			currentListing.setDeclaredInhandDate(listing.getDeclaredInhandDate());
		}
		if (log.isDebugEnabled()) {
			log.debug("message=\"Starting async tasks\""+"listingId=" + listing.getId());
		}

		try {
			// Set listing values with task values that were executed once for the whole listings batch
			if ( commonTasks != null )
				commonTasks.setListingValues(listing, currentListing);
			
			// process product manipulations 
			SeatProductsManipulator.processTicketProducts(listing, seatProdContext, request);

			// set a face value for currentListing if it has not value, after process ticketProducts
			if (currentListing.getFaceValue() == null || currentListing.getFaceValue().getAmount() == null) {
				currentListing.setFaceValue(calculateFaceValue(seatProdContext.getPassedSeatProductList(false)));
			}
			
			// process seats and traits operations
			SeatsAndTraitsManipulator.processSeatsTraits(listing,  currentListing, apiContext, seatProdContext,
					listingSeatTraitsHelper, ticketSeatMgr, inventoryMgr, pdfTicketMgr, inventorySolrUtil );
			
			// ticket traits task
			LookupExternalIdTask traitsTask = new LookupExternalIdTask(listing,  
					currentListing, apiContext, seatProdContext,
					listingSeatTraitsHelper, ticketSeatMgr,
					inventoryMgr, pdfTicketMgr, inventorySolrUtil );
			if ( traitsTask.ifNeedToRunTask() ) {
				traitsTask.call();
			}

			// add/update/delete ticket mediums
			// SELLAPI-3877 - Store the StockTypes only for Non Barcode and If more than one stockType is provided
			if (listing.getTicketMedium() != null && TicketMedium.BARCODE.getValue() != listing.getTicketMedium() && null != request.getTicketMediums()
					&& request.getTicketMediums().size() > 1)
				TicketMediumsManipulator.processTicketMediums(listing, seatProdContext, request);
			
			// Move some misc values if exist in newListing to curListing
			seatProdContext.setMiscValuesInCurrentListing ( listing );
		} 
		catch (ListingBusinessException lbe) {
			throw lbe;
		} 
		catch (Exception e) {
			log.error("error_message=\"unexpected exception\""+"listingId=" + listing.getId(), e);
			Throwable t = e.getCause();
			if (t instanceof ListingBusinessException) {
				ListingBusinessException lbe = (ListingBusinessException) t;
				throw lbe;
			} else {
				ListingError listingError = new ListingError(
						ErrorType.SYSTEMERROR, ErrorCode.SYSTEM_ERROR, "","listingId");
				throw new ListingBusinessException(listingError);
			}
		}
		// update comments
		if (listing.getComments() != null) {
			currentListing.setComments(listing.getComments());
		}
		if (listing.getSellerInputPrice()!=null) {
			currentListing.setSellerInputPrice(listing.getSellerInputPrice());
			currentListing.setSellerInputCurrency(listing.getSellerInputCurrency());
			currentListing.setSellerInputPriceType(listing.getSellerInputPriceType());
		}

		// SELLAPI-4345 Sales Tax Update
		if(listing.getPurchasePricePerProduct() != null) {
			if(listing.getPurchasePricePerProduct().getAmount().compareTo(BigDecimal.ZERO) < 0 ) {
				currentListing.setPurchasePricePerProduct(null);
				currentListing.setPurchasePriceCurrency(null);
			}
			else {
				currentListing.setPurchasePricePerProduct(listing.getPurchasePricePerProduct());
				currentListing.setPurchasePriceCurrency(listing.getPurchasePriceCurrency());
			}
		}

		if (listing.getSalesTaxPaid() != null) {
			currentListing.setSalesTaxPaid(listing.getSalesTaxPaid());
		} else {
			currentListing.setSalesTaxPaid(true);
		}

		// update InternalNote
		if (listing.getSellerInternalNote() != null) {
			currentListing.setSellerInternalNote(listing.getSellerInternalNote());
		}

		// activate or deactivate a listing START
		if (com.stubhub.domain.inventory.common.entity.ListingStatus.INACTIVE.toString().equalsIgnoreCase(listing.getSystemStatus())) {
			if (ListingStatus.DELETED.toString().equals(
					currentListing.getSystemStatus())
					|| ListingStatus.PENDING_LOCK.toString().equals(
							currentListing.getSystemStatus())
					|| ListingStatus.INCOMPLETE.toString().equals(
							currentListing.getSystemStatus())
					|| ListingStatus.PENDING_PDF_REVIEW.toString().equals(
							currentListing.getSystemStatus())) {
				ListingError listingError = new ListingError(
						ErrorType.BUSINESSERROR,
						ErrorCode.LISTING_ACTION_NOTALLOWED, "", "listingId");
				throw new ListingBusinessException(listingError);
			}
			if (!(currentListing.getSystemStatus()
					.equals(ListingStatus.INACTIVE.toString()))) {
				currentListing.setSystemStatus(ListingStatus.INACTIVE
						.toString());
			}
			if (isCreate) {
                checkForDuplicateSRS(currentListing);
            }
		} else if (com.stubhub.domain.inventory.common.entity.ListingStatus.ACTIVE
				.toString().equalsIgnoreCase(listing.getSystemStatus())) {
			// If try to active and current listing was not active
			if (!(currentListing.getSystemStatus().equals(ListingStatus.ACTIVE
					.toString()))) {
				if (ListingStatus.DELETED.toString().equals(
						currentListing.getSystemStatus())
						|| ListingStatus.PENDING_LOCK.toString().equals(
								currentListing.getSystemStatus())
						|| ListingStatus.PENDING_PDF_REVIEW.toString().equals(
								currentListing.getSystemStatus())) {
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR,
							ErrorCode.LISTING_ACTION_NOTALLOWED, "",
							"listingId");
					throw new ListingBusinessException(listingError);
				}
				
				// SELLAPI-1773
		        boolean paymentMethodRequired =
		            SellerPaymentUtil.isPaymentMethodRequired(listing.getSellerId().toString());

		        if (paymentMethodRequired) {
		          if (currentListing.getSellerPaymentTypeId() == null) {
		            ListingError listingError = new ListingError(ErrorType.BUSINESSERROR,
		                ErrorCode.LISTING_ACTION_NOTALLOWED, "", "SellerPaymentTypeId");
		            throw new ListingBusinessException(listingError);
		          }
		        }

				if (currentListing.getSellerContactId() == null) {
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR,
							ErrorCode.LISTING_ACTION_NOTALLOWED, "",
							"SellerContactId");
					throw new ListingBusinessException(listingError);
				}
				verifyTaxPayerStatus(listing);
				
				// SELLAPI-1789 if fraud check status is approved, don't check CC
        		Boolean isFraudCheckStatus = currentListing.getFraudCheckStatusId() == null
            		|| currentListing.getFraudCheckStatusId() == 500L;
        		if (!isFraudCheckStatus) {
				  if (paymentMethodRequired && !(userHelper.isSellerPaymentContactIdPopulated(
						listing.getSellerGuid(),
						currentListing.getSellerPaymentTypeId(),
						currentListing.getAllsellerPaymentInstrumentsV2()))) {
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR,
							ErrorCode.LISTING_ACTION_NOTALLOWED, "",
							"SellerPaymentContactId");
					throw new ListingBusinessException(listingError);
				  }
				  if ((currentListing.getSellerCCId() == null)
						|| (currentListing.getSellerCCId().longValue() == 48411)
						|| (currentListing.getAllsellerPaymentInstruments() == null && !userHelper
								.isSellerCCIdValid(listing.getSellerGuid(),
										currentListing.getSellerCCId()))) {
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR,
							ErrorCode.LISTING_ACTION_NOTALLOWED, "",
							"SellerCCId");
					throw new ListingBusinessException(listingError);
				  }
				}
				if ((currentListing.getListPrice() == null)
						|| (currentListing.getListPrice().getAmount() == null)
						|| (currentListing.getListPrice().getAmount()
								.doubleValue() <= 0)) {
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR,
							ErrorCode.LISTING_ACTION_NOTALLOWED, "",
							"TicketPrice");
					throw new ListingBusinessException(listingError);
				}
				if ((currentListing.getSplitOption() == null)
						|| (currentListing.getSplitQuantity() == null)) {
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR,
							ErrorCode.LISTING_ACTION_NOTALLOWED, "", "split");
					throw new ListingBusinessException(listingError);
				}
				
				if(currentListing.getFraudCheckStatusId() != null && currentListing.getFraudCheckStatusId() != 500L) {
					ListingError listingError = new ListingError(
							ErrorType.BUSINESSERROR,
							ErrorCode.LISTING_ACTION_NOTALLOWED, "",
							"listingId");
					throw new ListingBusinessException(listingError);
				}
				
				if (currentListing.getSectionScrubSchedule() == null) {
					currentListing.setSectionScrubSchedule(true);
				}
				if (currentListing.getSectionScrubExcluded() == null) {
					currentListing.setSectionScrubSchedule(false);
				} 
				if (currentListing.getConfirmOption() == null) {
					currentListing.setConfirmOption(CONFIRM_OPTION_MANUAL);
				} 
				
				// Check for SRS if listing being activated
				checkForDuplicateSRS(currentListing);
				
				boolean isArtifactExists = hasFulfillmentArtifact(request);

				if (ListingStatus.INCOMPLETE.toString().equals(
						currentListing.getSystemStatus())
						&& (currentListing.getTicketMedium() == TicketMedium.BARCODE.getValue()
							|| currentListing.getTicketMedium() == TicketMedium.FLASHSEAT.getValue())
						&& currentListing.getDeliveryOption() == DeliveryOption.PREDELIVERY.getValue()) {
					currentListing.setInhandDate(DateUtil.getNowCalUTC());
					currentListing.setSystemStatus(ListingStatus.PENDING_LOCK
							.toString());
					currentListing.setConfirmOption(ConfirmOptionEnum.AUTO_CONFIRM.getConfirmStatus());
				} else if (LMS_PENDING_APPROVAL == currentListing
						.getLmsApprovalStatus()
						&& FulfillmentMethod.LMS.equals(listing
								.getFulfillmentMethod())) {
					currentListing.setSystemStatus(ListingStatus.INCOMPLETE
							.toString());
					log.info("set listing to INCOMPLETE listingId={} due to getLmsApprovalStatus={} is pending and FulfillmentMethod is lms", currentListing.getId(),
							currentListing.getLmsApprovalStatus());
				} else if (ListingStatus.INCOMPLETE.toString().equals(currentListing.getSystemStatus())
                        && StringUtils.trimToNull(currentListing.getFulfillmentDeliveryMethods()) == null
                        && !isArtifactExists) {
                    log.error("message=\"Listing cannot be activated without fulfillment\" listingId={}", currentListing.getId());
                    ListingError listingError = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.LISTING_ACTION_NOTALLOWED, "Listing cannot be activated until fulfilled", "status");
                    throw new ListingBusinessException(listingError);
                } else {
					currentListing.setSystemStatus(ListingStatus.ACTIVE
							.toString());
				}
				if(currentListing.getLmsApprovalStatus() != null && currentListing.getLmsApprovalStatus() != LMS_APPROVED) {
					boolean isShipping = false;
					String fmDMList = currentListing.getFulfillmentDeliveryMethods();
					if(fmDMList != null) {
						isShipping = deliveryMethodsContainShipping(fmDMList); ////SELLAPI-1135 - Boolean expression complexity is 5 (max allowed is 3)
					}
					if(!isShipping) {
						currentListing.setSystemStatus(ListingStatus.INCOMPLETE.toString());
						log.info("set listing to INCOMPLETE listingId={} due to getLmsApprovalStatus={} is not approved and not shipping", currentListing.getId(),
								currentListing.getLmsApprovalStatus());
					}
				}
			}
			// For isCreate and ACTIVE and NON-GA listing, check for duplicate SRS always if listing is being activated 
			if (isCreate) {
			    checkForDuplicateSRS(currentListing);
			}
		}
		// activate or deactivate a listing END
		else if(com.stubhub.domain.inventory.common.entity.ListingStatus.PENDING.toString().equalsIgnoreCase(listing.getSystemStatus())) {
			if(currentListing.getTicketMedium() == TicketMedium.PDF.getValue()) {
				if(inventoryMgr.isPDFPendingReviewAllowed(currentListing.getId())){
					currentListing.setSystemStatus(ListingStatus.PENDING_PDF_REVIEW.toString());
				}else{
					ListingError listingError = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.LISTING_ACTION_NOTALLOWED, "Invalid listing status", "status");
					throw new ListingBusinessException(listingError);
				}
			} else {
				ListingError listingError = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.LISTING_ACTION_NOTALLOWED, "Invalid listing status", "status");
				throw new ListingBusinessException(listingError);
			}
			verifyTaxPayerStatus(listing);
		}else if (com.stubhub.domain.inventory.common.entity.ListingStatus.DELETED
				.toString().equalsIgnoreCase(listing.getSystemStatus())) {
			currentListing.setSystemStatus(ListingStatus.DELETED
					.toString());
		} 
		//SELLAPI-1180 doesn't check duplicate listing when create Incomplete status Listing
		else if(isCreate && (com.stubhub.domain.inventory.common.entity.ListingStatus.INCOMPLETE.toString().equalsIgnoreCase(currentListing.getSystemStatus())
		    || PENDING_LOCK.equalsIgnoreCase(currentListing.getSystemStatus()))){
		    checkForDuplicateSRS(currentListing);
		}
		else if(!isCreate && 
				com.stubhub.domain.inventory.common.entity.ListingStatus.INCOMPLETE.toString().equalsIgnoreCase(listing.getSystemStatus())){
			currentListing.setSystemStatus(ListingStatus.INCOMPLETE.toString());
		}
		//LDD-430 Listings Fulfillment Extension issues		
		if(!isCreate && request.getSaleEndDate() != null ){
			currentListing.setSaleEndDateIndicator(Boolean.TRUE);
		}
		//hide seats
		if(!isCreate && request.isHideSeats() != null ){
			currentListing.setHideSeatInfoInd(request.isHideSeats());
		}
		
		// Sell It Now
		if (isCreate && request.getAttributes() != null && request.getAttributes().getKey() != null
				&& request.getAttributes().getKey().equalsIgnoreCase("SELLITNOW")
				&& request.getAttributes().getValue().equalsIgnoreCase("TRUE")) {
			currentListing.setSellItNow(SELL_IT_NOW);
		}
		
		//auto pricing
		Boolean apEnabled = Boolean.FALSE;
		if(!isCreate  ){
			if(request.isAutoPricingEnabledInd() != null) {				
				apEnabled = request.isAutoPricingEnabledInd();
			}
			currentListing.setAutoPricingEnabledInd(apEnabled);
		}
		// Don't Update IP Address during Update flow - SELLAPI-4192
		if (listing.getIpAddress() != null && isCreate)
			currentListing.setIpAddress(listing.getIpAddress());
		if (listing.getTealeafSessionGuid() != null)
			currentListing.setTealeafSessionGuid(listing
					.getTealeafSessionGuid());
		if (listing.getThreatMatrixRefId() != null)
			currentListing.setThreatMatrixRefId(listing.getThreatMatrixRefId());
		
		log.debug("businessId=" + listing.getBusinessId() + " businessGuid=" + listing.getBusinessGuid());
		if (listing.getBusinessId() != null && listing.getBusinessGuid() != null) {
			currentListing.setBusinessId(listing.getBusinessId());
			currentListing.setBusinessGuid(listing.getBusinessGuid());
		}

		log.info("after create/update={} listing.Id={} currentListing.Status={}", isCreate, currentListing.getId(),
				currentListing.getSystemStatus());

		if(listing.getSellerRequestedStatus() != null) {
		    currentListing.setSellerRequestedStatus(listing.getSellerRequestedStatus());
		}
		
		return new ListingWrapper(currentListing, seatProdContext );
	}

	private Money calculateFaceValue(List<SeatProduct> passedProductsList){
		Money result = null;

		Money minimumFaceValue = null;
		if (passedProductsList != null) {
			for (SeatProduct seatProduct : passedProductsList) {
				if (seatProduct.getFaceValue() != null && seatProduct.getFaceValue().getAmount() != null) {
					if (minimumFaceValue == null || (minimumFaceValue.getAmount().compareTo(seatProduct.getFaceValue().getAmount()) > 0)) {
						minimumFaceValue = seatProduct.getFaceValue();
					}
				}
			}
		}

		if (minimumFaceValue != null && minimumFaceValue.getAmount() != null){
			result = minimumFaceValue;
		}

		return result;
	}

	private boolean isGA(Listing currentListing) {
		
		boolean isGA = false;
		if(GeneralAdmissionI18nEnum.isGA(currentListing.getSection())){
			isGA = true;
		}else if(!StringUtils.isBlank(currentListing.getSeats())){
			String patternString = "^GA\\d+.*";			
			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(currentListing.getSeats());
			if(matcher.matches()){
				isGA = true;
			}
		}
		return isGA;
	}

	private boolean deliveryMethodsContainShipping(String fmDMList) {
		if(fmDMList.contains("|10,") || fmDMList.contains("|11,") || fmDMList.contains("|12,")){
			return true;
		}else if(fmDMList.startsWith("10,") || fmDMList.startsWith("11,") || fmDMList.startsWith("12,")){
			return true;
			
		}else {
			return false;
		}
	}
	
	public List<Listing> updateDatabaseForBatch ( boolean isCreate, List<Listing> listings ) 
	{
		//SELLAPI-1135 sonar-rules, avoid reassigning to parameters.
		List<Listing> upsertListings = null;

		// Database create batch listings
		if ( isCreate ) {
			upsertListings = inventoryMgr.addListings(listings);
		}
		else { // update batch listings
			upsertListings = inventoryMgr.updateListings(listings);
		}
		
		for (int i=0; i<upsertListings.size(); i++ ) {
			if(upsertListings.get(i).getSendLmsMessage()) {
				jmsMessageHelper.sendCreateLMSListingMessage(upsertListings.get(i));
				//SELLFLOW-6855 change , avoid duplicate DOS instances
				upsertListings.get(i).setSendLmsMessage(false);
			}
		}
		
		return upsertListings;
	}
	
	private void verifyExistingListing(Listing currentListing, Listing listingInput){
		if (currentListing != null
				&& (listingInput.getSellerId().longValue() != currentListing.getSellerId().longValue())) {
			ListingError listingError = new ListingError(
					ErrorType.BUSINESSERROR, ErrorCode.INVALID_LISTINGID, "Listing was created by different seller",
					"listingId");
			throw new ListingBusinessException(listingError);
		}

		if (currentListing == null) {
			ListingError listingError = new ListingError(ErrorType.NOT_FOUND,
					ErrorCode.LISTING_NOT_FOUND, "Cannot find listing id", "listingId");
			throw new ListingBusinessException(listingError);
		}

		// SELLAPI-2426 Added logic to avoid any updates to a deleted listing
		if (currentListing.getSystemStatus() != null
				&& currentListing.getSystemStatus().equals(ListingStatus.DELETED.toString())) {
			ListingError listingError = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.LISTING_NOT_ACTIVE,
					"Listing has been deleted", "listingId");
			throw new ListingBusinessException(listingError);
		}
		// SELLAPI-1550
		// if the request is to delete the listing, we don't need to do any
		// further
		// checking; just delete it
		if (listingInput.getSystemStatus() != null
				&& !listingInput.getSystemStatus().equals(ListingStatus.DELETED.toString())) {

			if (listingInput.getIsLmsApproval() == false) {
				if (currentListing.getEndDate() != null
						&& currentListing.getEndDate().before(DateUtil.getNowCalUTC())) {
					ListingError listingError = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.LISTING_EXPIRED,
							"The listing has expired ", "listingId");
					throw new ListingBusinessException(listingError);
				}
			}
		}

		if (currentListing.getQuantityRemain() == 0) {
			ListingError listingError = new ListingError(
					ErrorType.BUSINESSERROR, ErrorCode.LISTING_ALREADY_SOLD,
					"", "listingId");
			throw new ListingBusinessException(listingError);
		}		
	}
	
	private void handleQuantityUpdates ( Listing listing, SeatProductsContext seatProdContext)
	{
		int modifiedQuantityCount = 0;
		Listing currentListing = seatProdContext.getCurrentListing();
		
		// update quantity
		if (listing.getQuantity() != null ) {
			
			int passedQuantity = listing.getQuantity().intValue();
			
			// 1: Update mode and quantity changed case (only for update)
			if ( !seatProdContext.isCreate() && passedQuantity != currentListing.getQuantityRemain().intValue()) {
				
				// Get db ticket seats and parse the csv seats into a list 
				List<TicketSeat> curTicketSeats = seatProdContext.getTicketSeatsFromCache();
				
				// Modify quantity and split: modifiedQty = currentRemainQty - passedQty;
				modifiedQuantityCount = currentListing.getQuantityRemain() - listing.getQuantity();

				// reduce quantity
				if ( modifiedQuantityCount > 0 ) {
					
					// delete seats
					_deleteSeatsFromBottom ( modifiedQuantityCount, currentListing, curTicketSeats );
				}
				// increase seats (if not GA issue error)
				else if ( !TicketSeatUtils.isGASection(currentListing.getSection()) && 
						!TicketSeatUtils.isGASection (listing.getSection()) ) {
					
					ListingError listingError = new ListingError(
							ErrorType.INPUTERROR,
							ErrorCode.INVALID_QUANTITY,
							"Cannot increase quantity to add seats for non GA listing",
							"quantity");
					throw new ListingBusinessException(listingError);
				}	
				else { // increase qty and GA listing
					_addDummyGASeats ( modifiedQuantityCount, currentListing, seatProdContext );
				}
				
				// Takes care of updating quantity and split quantity in currentListing object
				seatProdContext.updateQuantityInCurrentListing( -modifiedQuantityCount );
				
			}
			// 2: Create mode and GA and no products passed 
			else if (seatProdContext.isCreate() && (StringUtils.trimToNull(listing.getSection())==null || TicketSeatUtils.isGASection(listing.getSection()) ) &&
					seatProdContext.getListingRequest().getProducts() == null )
			{
				List<TicketSeat> curTicketSeats = seatProdContext.getTicketSeatsFromCache(); 
				_addDummyGASeats ( -listing.getQuantity(), currentListing, seatProdContext );
			}
		}

		if (modifiedQuantityCount > 0 && currentListing.getTicketMedium() != null
				&& (currentListing.getTicketMedium() == TicketMedium.BARCODE.getValue()
					|| currentListing.getTicketMedium() == TicketMedium.FLASHSEAT.getValue())
				&& currentListing.getDeliveryOption() == DeliveryOption.PREDELIVERY.getValue()) {
			
			//We might need to see if need to call verifyBarcode again to cleanup after deleting barcode (the PTV_TICKET) tables now have orphan ticket seat
			//records
			if ( !currentListing.getSystemStatus().equals(ListingStatus.INCOMPLETE.name() ) ) {

				currentListing.setInhandDate(DateUtil.getNowCalUTC());

				currentListing.setSystemStatus(ListingStatus.PENDING_LOCK.toString());
				currentListing.setConfirmOption(ConfirmOptionEnum.AUTO_CONFIRM.getConfirmStatus());
				/*SELLAPI-3243*/
				currentListing.setIsLockMessageRequired(true);
			}
		}
	}
	
	/**
	 * Add dummy GA seats to TicketSeat table
	 * @param changeQty
	 * @param currentListing
	 * @param curTicketSeats
	 */
	private void _addDummyGASeats ( int changeQty, Listing currentListing, SeatProductsContext seatProdContext )
	{
		List<TicketSeat> curTicketSeats = seatProdContext.getTicketSeatsFromCache();
		
		TicketSeat likeSeat = null;	
		
		if ( curTicketSeats.size() > 0 ) {
			likeSeat = curTicketSeats.get(0);
		}
		
		for ( int i=0; i<-changeQty; i++ ) {
			TicketSeat ts = TicketSeatUtils.makeTicketSeatLike(currentListing, likeSeat, null);
			ts.setTixListTypeId(1L);
			ts.setSeatStatusId(1L);
			ts.setGeneralAdmissionInd(true);
			seatProdContext.addTicketSeat( ts );
		}
	}
	
	/**
	 * _deleteSeatsFromBottom
	 * @param currentListing
	 * @throws ListingBusinessException
	 */
	private void _deleteSeatsFromBottom ( int changeQty, Listing currentListing, List<TicketSeat> curTicketSeats  ) 
			throws ListingBusinessException
	{
		String seats = currentListing.getSeats();
		for ( int j=0; j<changeQty; j++ ) {
			
			String removedSeat= null;
			int idx = curTicketSeats.size()-j-1;

			if (seats != null && seats.contains(",")) {
				removedSeat = seats.substring(seats.lastIndexOf(",")+1);
				seats = seats.substring(0, seats.lastIndexOf(","));
			}
			boolean deleted = false;
			if ( removedSeat != null ) {
				for ( int i=curTicketSeats.size()-1; i>=0; i--) {	
					TicketSeat ts = curTicketSeats.get (i);
					if ( removedSeat.equalsIgnoreCase(ts.getSeatNumber()) && 
							ts.getSeatStatusId().longValue()== 1L && 
							ts.getTixListTypeId().longValue()== 1L ) {
						ts.setSeatStatusId(4L); // 4-deleted
						deleted = true;
						break;
					}
				}
			}
			if ( !deleted ) {
				TicketSeat ts = curTicketSeats.get(idx);
				ts.setSeatStatusId(4l); // 4-deleted
			}
		}
		currentListing.setSeats ( seats );
		currentListing.setTicketSeats(curTicketSeats);
	}
		
	/**
	 * verifyTaxPayerStatus
	 * @param listing
	 * @throws ListingException
	 */
	private void verifyTaxPayerStatus(Listing listing) throws ListingException{
		sellerHelper.populateSellerDetails(listing);
		if(listing.getTaxpayerStatus()!=null && TaxpayerStatusEnum.TINRequired.getStatus().equalsIgnoreCase(listing.getTaxpayerStatus()) ||
				TaxpayerStatusEnum.TINInvalid.getStatus().equalsIgnoreCase(listing.getTaxpayerStatus())){
				ListingError listingError = new ListingError
					(ErrorType.BUSINESSERROR, 
					ErrorCode.TAXPAYER_ERROR, "TIN is either not on file or Invalid", "");
				throw new ListingBusinessException(listingError);
		}
	}
	
    private boolean validateforSeats(List<Product> products, Long eventId) {
      boolean hasSeatNumbers = true;
      boolean isEticket = false;
      boolean isSeatsRequired = false;
      if (products != null && !products.isEmpty()) {
        for (Product product : products) {
          if (StringUtils.trimToNull(product.getSeat()) == null) {
            hasSeatNumbers = false;
            break;
          }
        }
      } else {
        hasSeatNumbers = false;
      }
     
      HashMap<String,Boolean> booleanValues = inventoryMgr.isSeatsRequired(eventId);
		if (null != booleanValues) {
			isSeatsRequired = booleanValues.get("isSeatRequired");
			isEticket = booleanValues.get("isEticket");
		}

      if (!hasSeatNumbers && isSeatsRequired) {
        log.error("message=\"Seat numbers are mandatory\" eventId={}", eventId);
        ListingError listingError = new ListingError(ErrorType.BUSINESSERROR,
            ErrorCode.SEAT_CANNOT_BE_NULL, "Seat numbers are required", "");
        throw new ListingBusinessException(listingError);
      }
      
      return isEticket;
    }
    
	private void validateforProducts(List<Product> products,long quantity) {
		int fulfillmentArtifactCount = 0;
		if (products != null && !products.isEmpty()) {
			for (Product product : products) {
				if (StringUtils.trimToNull(product.getFulfillmentArtifact()) != null) {
					fulfillmentArtifactCount++;

				}
			}
		}
		if (fulfillmentArtifactCount > 0 && fulfillmentArtifactCount != quantity) {
			log.error("message=\"Fulfillment artifacts do not match the quantity\"");
			ListingError listingError = new ListingError(ErrorType.BUSINESSERROR, ErrorCode.INCORRECT_QUANTITY_OF_FULFILLMENT_ARTIFACTS,
					"Fulfillment artifacts do not match the quantity", "");
			throw new ListingBusinessException(listingError);

		}
	}

	private void validateSeatsAndExternalIDForProducts(List<Product> products) {
		if (products != null && !products.isEmpty()) {
			for (Product product : products) {
				//Validate Only for Tickets
				if(null != product && ProductType.TICKET.equalsEnum(product.getProductType()))
				{
					if (StringUtils.trimToNull(product.getFulfillmentArtifact()) != null) {
						if (StringUtils.trimToNull(product.getSeat()) == null
							&& StringUtils.trimToNull(product.getExternalId()) == null && product.getSeatId() == null) {
							log.error("message=\"Seats not provided for the Fulfillment artifacts\"");
							ListingError listingError = new ListingError(ErrorType.BUSINESSERROR,
									ErrorCode.INCORRECT_SEATS_PREDELIVERY,
									"Seats not provided for the Fulfillment artifacts", "");
							throw new ListingBusinessException(listingError);
						}
					}
				}
			}
		}
	}
	
  private void checkForDuplicateSRS(Listing listing) {
    if (!isGA(listing)) {
      ListingCheck listingCheck =
          inventorySolrUtil.isListingExists(listing.getEventId(), listing.getSellerId(),
              listing.getSection(), listing.getRow(), listing.getSeats(), listing.getId());
      if (listingCheck.getIsListed()) {
        ListingError listingError =
            new ListingError(ErrorType.BUSINESSERROR, ErrorCode.DUPLICATE_SECTION_ROW_SEAT,
                ErrorEnum.DUPLICATE_SECTION_ROW_SEAT.getMessage(), listingCheck.getMessage());
        throw new ListingBusinessException(listingError);
      }
    }
  }  	

  private boolean hasFulfillmentArtifact(ListingRequest request) {
    List<Product> products = request.getProducts();
    if (products != null && products.size() > 0) {
      for (Product product : products) {
        if (product.getFulfillmentArtifact() != null
            && !product.getFulfillmentArtifact().trim().equals("")) {
          return true;
        }
      }
    }
    return false;
  }

	private boolean isListingSourceIndy(String createdBy) {
		createdBy = StringUtils.trimToEmpty(createdBy);
		if (StringUtils.isNotEmpty(createdBy)) {
			createdBy = createdBy.toLowerCase();
			if (createdBy.contains("api_uk_sell_buyer20") || createdBy
				.contains("access@stubhub.com") ||
				createdBy.contains("corp.ebay.com") ||
				createdBy.contains("siebel") ||
				createdBy.contains("sth")) {
				log.debug("_message=\"listing source is Indy\"createdBy={}",
					createdBy);
				return true;
			}
		}
		return false;
	}
	

}
