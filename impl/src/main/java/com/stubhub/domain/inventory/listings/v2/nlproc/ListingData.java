package com.stubhub.domain.inventory.listings.v2.nlproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.tasks.RecallableInventoryTask;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.enums.ProxyRoleTypeEnum;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;

public class ListingData 
{
	// listing errors
	private List<ListingError> listingErrors = null;
	
	private List<ListingRequest> requestsBody = new ArrayList<ListingRequest> ();
	
	private Map<String, ListingRequest> requestMap = new HashMap<String, ListingRequest>();
	
	// listing to store in DB (this should be == current listing for update)
	//private Listing toStoreListing = new Listing ();
	
	// listing header (only common fields converted from common request)
	private Listing headerListing  = null;
	
	// passed common request values covered to headerListing
	private ListingRequest headerRequest = null;
	
	// remember the fmdmtask instance per request to provide fmdm per listing based on parameters 
	private CommonTasks commonTasks = new CommonTasks();
	
	private SHAPIContext apiContext = null;
	private SHServiceContext shServiceContext = null;
	
	private boolean isCreate = false;
	private boolean isBulk = false;
	
	private Long sellerId = null;
	private String sellerGuid = null;
	
	private String operatorId;
	
	private ProxyRoleTypeEnum role;
	
	private int DEF_BATCH_SIZE = 20;
	private int batchSize = DEF_BATCH_SIZE;
	
	// only if there is one listing
	//private Listing singleCurrentListing = null;
	private Map<Long,Listing>curListings = null;
	
	// Locale defaults to US
	private Locale locale = Locale.US;
	
	private String subscriber;
	
	private Integer sellShStoreId;
	
	public Integer getSellShStoreId() {
		return sellShStoreId;
	}

	public void setSellShStoreId(Integer sellShStoreId) {
		this.sellShStoreId = sellShStoreId;
	}
		
	public ListingData ( boolean isCreate, Long sellerId, String sellerGuid )
	{
		this ( isCreate, false, sellerId, sellerGuid );
	}
	
	public ListingData ( boolean isCreate, boolean isBulk, Long sellerId, String sellerGuid )
	{
		this.isCreate = isCreate;
		this.isBulk = isBulk;
		
		this.sellerGuid = sellerGuid;
		this.sellerId = sellerId;
		
		this.batchSize = _getBatchSize();
	}
	
	public void addRecallableTask ( RecallableInventoryTask recallableTask )
	{
		this.commonTasks.addRecalllableTask( recallableTask );
	}
	
	public CommonTasks getCommonTasks ()
	{
		return this.commonTasks;
	}
	
	public boolean needDeepEventLookup ()
	{
		if ( !isBulk ) {
			if ( requestsBody.size()==1 ) {
				ListingRequest lr = requestsBody.get(0);
				return ((lr.getTicketTraits()!=null && lr.getTicketTraits().size()>0) || StringUtils.trimToNull(lr.getComments()) != null) ;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Add common values found in specific listing 
	 */
	public Listing addCommonListingHeaderValues ( Listing listing )
	{
		listing.setSellerCCId( headerListing.getSellerCCId() );
		listing.setSellerContactId(headerListing.getSellerContactId());
		listing.setSellerContactGuid(headerListing.getSellerContactGuid());
		listing.setSellerId(sellerId);
		listing.setSellerGuid(sellerGuid);
		listing.setSellerPaymentTypeId(headerListing.getSellerPaymentTypeId());
		listing.setEvent(headerListing.getEvent());
		listing.setEventId(headerListing.getEventId());
		listing.setCurrency(headerListing.getCurrency());
		if(listing.getListingSource() == null) {
			listing.setListingSource(10);	// StubhubPro
		}
		
		// for create, do initial
		/*
		if ( isCreate ) {
			listing.setFulfillmentDeliveryMethods(headerListing.getFulfillmentDeliveryMethods());
			if ( listing.getFulfillmentMethod() == null )
				listing.setFulfillmentMethod(headerListing.getFulfillmentMethod());
		}
		*/
		return listing;
	}

	public boolean isCreateRequest () {
		return isCreate;
	}
	
	public boolean isBulkRequest () {
		return isBulk;
	}
	
	public List<ListingError> getListingErrors() {
		return listingErrors;
	}

	public void addListingError( ListingError error) {
		if ( listingErrors == null )
			listingErrors = new ArrayList<ListingError> ();
		
		this.listingErrors.add(error);
	}

	public Listing getHeaderListing() {
		return headerListing;
	}
	
	public void setHeaderListing ( Listing headerListing )
	{
		this.headerListing = headerListing;
	}

	public ListingRequest getHeaderRequest() {
		return headerRequest;
	}

	public void setHeaderRequest(ListingRequest headerRequest) {
		this.headerRequest = headerRequest;
	}	

	public List<ListingRequest> getRequestBodies()
	{
		return requestsBody;
	}
	
	public void updateEventInfo ( Event e)
	{
		headerListing.setEvent(e);
		headerListing.setEventId(e.getId());
		
		if ( isCreate ) {
			headerListing.setCurrency(e.getCurrency());
		}
		
		// set Event object in all current listings (if any)
		if ( curListings != null && curListings.size()> 0 ) {
			for (Iterator<Entry<Long, Listing>> it = curListings.entrySet().iterator(); it.hasNext(); ) {
				Entry<Long, Listing> entry = it.next();
				entry.getValue().setEvent(e);
			}
		}
	}
	
	public void addRequest ( ListingRequest request) 
	{
		// add all requests to map
		requestsBody.add( request );
		
		// get item index
		int idx = requestsBody.size() - 1;
		
		if(StringUtils.trimToNull(request.getExternalListingId()) != null) {
		    requestMap.put(request.getExternalListingId(), request);
		}
	}
	
	public ListingRequest getRequestFromMap(String externalListingId) {
	    return requestMap.get(externalListingId);
	}
	
	public SHAPIContext getApiContext() {
		return apiContext;
	}

	public void setApiContext(SHAPIContext apiContext) {
		this.apiContext = apiContext;
	}
	
    public SHServiceContext getShServiceContext() {
        return shServiceContext;
    }

    public void setShServiceContext(SHServiceContext shServiceContext) {
        this.shServiceContext = shServiceContext;
    }

    public String getSellerGuid() {
		return sellerGuid;
	}

	public Long getSellerId() {
		return sellerId;
	}
	
	private int _getBatchSize ()
	{
		try {
			String val = MasterStubHubProperties.getProperty("bulk.listing.price.api.batch.size", 
					String.valueOf(DEF_BATCH_SIZE));
			return Integer.parseInt(val);
		}
		catch ( Exception ex ) {
			return DEF_BATCH_SIZE;
		}
	}

	public int getBatchSize() {
		return batchSize;
	}

	public Long getSingleListingId() {
		if ( requestsBody != null && requestsBody.size()==1 ) {
			return requestsBody.get(0).getListingId();
		}
		return null;
	}
	
	/*
	public void setSingleCurrentListing (Listing currentListing) 
	{
		this.singleCurrentListing = currentListing;
		if ( currentListing.getEventId() != null ) {
			this.headerRequest.setEventId( String.valueOf( currentListing.getEventId() ));
		}
	}
	
	public Listing getSingleCurrentListing ()
	{
		return this.singleCurrentListing;
	}
	*/
	
	/**
	 * Return current db listing associated with request listingId, or null if create listing 
	 * @param requestIdx
	 * @return
	 */
	public Listing getCurListing ( Long listingId )
	{
		if ( listingId!=null && curListings != null ) {
			return curListings.get(listingId);
		}
		return null;
	}
	
	public Listing getCurSingleListing ()
	{
		Long id = getSingleListingId ();
		if ( id!=null && curListings!=null && curListings.size()==1 ) {
			return curListings.get(id);
		}
		return null;
	}
	
	/**
	 * Return current db listings associated with all request idx, or null if create listing 
	 * @param requestIdx
	 * @return
	 */
	public Map<Long,Listing> getCurListingsMap ()
	{
		return curListings;
	}
	
	/**
	 * Sets the current listings map
	 * @param eventId
	 * @param curListings
	 */
	public void setCurListingsMap(Long eventId, Map<Long,Listing> curListings) {
		this.curListings = curListings;
		this.headerRequest.setEventId (  String.valueOf(eventId) );
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		if ( locale != null )
			this.locale = locale;
	}

	public String getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(String subscriber) {
		this.subscriber = subscriber;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public ProxyRoleTypeEnum getRole() {
		return role;
	}

	public void setRole(ProxyRoleTypeEnum role) {
		this.role = role;
	}
}

