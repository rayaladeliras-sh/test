package com.stubhub.domain.inventory.listings.v2.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.common.util.ListingException;
import com.stubhub.domain.inventory.datamodel.entity.Event;
import com.stubhub.domain.inventory.datamodel.entity.FulfillmentMethod;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOptions;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.entity.ListingCheck;
import com.stubhub.domain.inventory.listings.v2.entity.SeatProduct;
import com.stubhub.domain.inventory.listings.v2.nlproc.ListingData;
import com.stubhub.domain.inventory.listings.v2.nlproc.ListingToDataAdapter;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.DTO.TicketTrait;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.domain.inventory.v2.enums.ProxyRoleTypeEnum;
import com.stubhub.domain.pricing.intf.aip.v1.error.ErrorCode;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponse;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponseList;
import com.stubhub.domain.pricing.intf.aip.v1.response.SellFees;
import com.stubhub.domain.user.payments.intf.CheckDetails;
import com.stubhub.domain.user.payments.intf.CustomerPaymentInstrumentDetails;
import com.stubhub.domain.user.payments.intf.PayPalDetails;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.common.util.DateUtil;
import com.stubhub.platform.utilities.webservice.security.ExtendedSecurityContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;

public abstract class SHInventoryTest {
	// Scenario class 
  protected static class Scenario {
		String methodName;
		String desc;
		boolean testForSuccess;
		
    public Scenario(String methodName, String desc) {
			this.methodName = methodName;
			this.testForSuccess = true;  
			this.desc = desc;
		}
	}
	
	// Test result failure class 
  protected static class Result {
		String message;
		Throwable cause;
		boolean success;
		public static final Result SUCCESS = new Result();

    public static Result FAILURE(String errorMsg) {
			return new Result(errorMsg);
		}

    public static Result FAILURE(String errorMsg, Throwable cause) {
			return new Result(errorMsg, cause);
		}

		public Result () {
			success = true;
		}

		public Result (String errorMsg ){
			this.message = errorMsg;
			success = false;
		}

		public Result (String errorMsg, Throwable cause ){
			this.message = errorMsg;
			this.cause = cause;
			success = false;
		}
	}
			
	//// COMMON TEST VARIABLES
	/*
   * protected ExtendedSecurityContext securityContext; protected
   * com.stubhub.domain.inventory.datamodel.entity.Event event; protected EventHelper eventHelper;
   * 
   * protected HttpHeaders headers;
   * 
   * protected UpdateListingRequestValidator updatevalidatorMock; protected UpdateListingAsyncHelper
   * updateListingHelperMock;
   * 
   * protected SellerHelper sellerHelper;
   * 
   * protected TicketSeatMgr ticketSeatMgr; protected TicketSeatMgr seatDAO;
   * 
   * protected ListingSeatTraitsHelper listingSeatTraitsHelper; protected UserHelper userHelper;
   * protected ListingPriceDetailsHelper listingPriceDetailsHelper; protected InventoryMgr
   * inventoryMgr; protected JMSMessageHelper barcodeMessageHelper; protected PDFTicketMgr
   * pdfTicketMgr; protected FulfillmentServiceHelper fulfillmentServiceHelper; protected
   * ListingPriceUtil listingPriceUtil; protected InventorySolrUtil inventorySolrUtil; protected
   * PrimaryIntegrationUtil primaryIntegrationUtil; protected ListingSeatTraitMgr
   * listingSeatTraitMgr;
	*/
	
  public void testAggregate(Scenario[] testingTruthTable) {
		// list of failures
		ArrayList<Exception> failures = new ArrayList<Exception>();
		
		System.out.println ( "\nRUNNING TESTS .........\n");
		for ( int i=0; i<testingTruthTable.length; i++ ) {
			
			Scenario testScenario = testingTruthTable [ i ];
      System.out.println("[" + String.valueOf(i + 1) + "] " + testScenario.methodName + " -- "
          + testScenario.desc);
			
			Exception result = _executeTestMethod ( testScenario );
			if ( result != null ) {
				failures.add ( result );
			}
		}
		
		// show failures if any
		if ( failures.size() > 0 ) {

			System.out.println ( "\nTEST FAILURES ENCOUNTERED SEE BELOW .........\n");
			for  ( int i=0; i<failures.size(); i++ ) {
				
				Exception ex = failures.get(i);
				System.out.println ( "[" + String.valueOf(i+1) +"] "  + ex.getMessage() );
				if ( ex.getCause() != null ) {
					ex.getCause().printStackTrace();
				}
				System.out.println ( "");
			}
			
			Assert.fail("\nTEST FAILURES COUNT .......... " + failures.size() + "\n");
		}
	}
	
	// common methods
  private Exception _executeTestMethod(Scenario testScenario) {
		try {
			if ( testScenario.methodName == null ) {
				return new Exception ( "Invalid test method name: " + testScenario.methodName );
			}
			Method method = getClass().getMethod(testScenario.methodName);
			Object result = method.invoke(this);
			if ( result instanceof Result ) {
				Result successResult = (Result)result;
				if ( successResult.message==null || successResult.message.length()==0 ) {
					successResult.message = "none provided";
				}
				if ( testScenario.testForSuccess ) {
					// test failed
					if ( successResult.success == false ) {
            return new Exception(
                "Failed Test: " + testScenario.methodName
                    + ", AssertionError: expected success. Reason: " + successResult.message,
								successResult.cause);
					}
        } else if (successResult.success == true) {
          return new Exception(
              "Failed Test: " + testScenario.methodName
                  + ", AssertionError: expected failure. Reason: " + successResult.message,
							successResult.cause  );
				}
      } else {
        return new Exception(
            "Invalid Test: " + testScenario.methodName + ", test need to return Result object.");
			}
    } catch (java.lang.NoSuchMethodException nsm) {
      return new Exception(
          "Invalid Test: " + testScenario.methodName + ", test method is not found.");
    } catch (Throwable th) {
      return new Exception("Failed Test: " + testScenario.methodName + ", exception encountered",
          th);
		}
		return null;
	}
	
	
	/////////////////// MOCKITO UTILITIES ////////////////
	
  protected Object mockClass(Class cls, Object bindToObject, String bindFieldName) {
		Object mockedObj = mock ( cls );
		if ( bindToObject != null && bindFieldName != null ) {
			ReflectionTestUtils.setField(bindToObject, bindFieldName, mockedObj);
		}
		return mockedObj;
	}
	
  protected SellerHelper mockSellerHelper(Object obj, String fieldName) throws ListingException {
		SellerHelper sellerHelper = mock(SellerHelper.class);
		if ( fieldName != null )
			ReflectionTestUtils.setField(obj, fieldName, sellerHelper);
		
		Mockito.doAnswer(new Answer<Void>() {
	        @Override
	        public Void answer(InvocationOnMock invocation) throws Throwable {
	            return null;
	        }
	    }).when(sellerHelper).populateSellerDetails(Mockito.any(Listing.class));
		
		return sellerHelper;
	}	
	
  protected HttpHeaders mockHeaders(Object obj, String fieldName) {
		HttpHeaders headers = Mockito.mock(HttpHeaders.class);
		
		List<String> headerList = new ArrayList<String>();
		headerList.add("test value");
		Mockito.when(headers.getRequestHeader(Mockito.anyString())).thenReturn(headerList);
		
		if ( fieldName != null )
			ReflectionTestUtils.setField(obj, fieldName, headers);
		return headers;
	}

  public static UriInfo mockUriInfo() {
    MultivaluedMap<String, String> mvMap = mock(MultivaluedMap.class);
    Mockito.when(mvMap.getFirst("status")).thenReturn("ALL");

    UriInfo uriInfo = mock(UriInfo.class);
    Mockito.when(uriInfo.getQueryParameters()).thenReturn(mvMap);

    return uriInfo;
  }

  protected PrimaryIntegrationUtil mockPrimaryIntegrationUtil(Object obj, String fieldName) {
		PrimaryIntegrationUtil primaryIntegrationUtil = mock(PrimaryIntegrationUtil.class);
		if ( fieldName != null )
			ReflectionTestUtils.setField(obj, fieldName, primaryIntegrationUtil);	
		return primaryIntegrationUtil;
	}
	
  protected InventorySolrUtil mockInventorySolrUtil(Object obj, String fieldName) {
		InventorySolrUtil inventorySolrUtil = Mockito.mock(InventorySolrUtil.class);
		ListingCheck listingCheck =  new ListingCheck();
		Mockito.when(inventorySolrUtil.isListingExists(Matchers.anyLong(),Matchers.anyLong(),  Matchers.anyString(), 
				Matchers.anyString(), Matchers.anyString(), Mockito.anyLong())).thenReturn(listingCheck);
		if ( fieldName != null )
			ReflectionTestUtils.setField(obj, fieldName, inventorySolrUtil);	
		return inventorySolrUtil;
	}
	
	
  protected EventHelper mockEventHelper(Object obj, String fieldName) throws Exception {
		EventHelper eventHelper = Mockito.mock(EventHelper.class);
		com.stubhub.domain.inventory.datamodel.entity.Event dEvent= 
				new com.stubhub.domain.inventory.datamodel.entity.Event();
		dEvent.setId(10001l);
		dEvent.setActive(true);
		dEvent.setCurrency(Currency.getInstance("USD"));
		dEvent.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
		dEvent.setDescription("Event description");
		dEvent.setEventDate(new GregorianCalendar(2012, 10, 1));
		dEvent.setEarliestPossibleInhandDate(new GregorianCalendar(2012, 9, 1));
		dEvent.setLatestPossibleInhandDate(new GregorianCalendar(2012, 9, 25));
		List<FulfillmentMethod> fulfillmentMethods = new ArrayList<FulfillmentMethod>();
		FulfillmentMethod fulfillmentMethod  = new FulfillmentMethod();
		fulfillmentMethod.setName(DeliveryOptions.PDF);
		Calendar date = Calendar.getInstance();
		date.add(Calendar.DATE, 2);
		fulfillmentMethod.setEndDate(date);
		fulfillmentMethods.add(fulfillmentMethod);
		dEvent.setFulfillmentMethods(fulfillmentMethods);
    when(eventHelper.getEventById(Mockito.anyLong(), Mockito.anyString(), Mockito.any(Locale.class),
        Mockito.any(Boolean.class))).thenReturn(dEvent);
		
		if ( fieldName != null )
			ReflectionTestUtils.setField(obj, fieldName, eventHelper);

		return eventHelper;

	}
	
  protected ExtendedSecurityContext mockSecurityContext(String sellerId) {
		ExtendedSecurityContext securityContext = Mockito.mock(ExtendedSecurityContext.class);
		Mockito.when(securityContext.getUserId()).thenReturn(sellerId);
		Mockito.when(securityContext.getUserGuid()).thenReturn("AB12345");
		Mockito.when(securityContext.getUserName()).thenReturn("User");
		return securityContext;
	}
  
  protected ExtendedSecurityContext mockSecurityContextWithAppName(String sellerId) {
		ExtendedSecurityContext securityContext = Mockito.mock(ExtendedSecurityContext.class);
		Mockito.when(securityContext.getUserId()).thenReturn(sellerId);
		Mockito.when(securityContext.getUserGuid()).thenReturn("AB12345");
		Mockito.when(securityContext.getUserName()).thenReturn("User");
		Mockito.when(securityContext.getOperatorApp()).thenReturn("SHIP_POS");
		return securityContext;
	}
  
  protected MessageContext mockMessageContext(){
	  return new MessageContext() {
			
			@Override
			public void put(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public UriInfo getUriInfo() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public ServletContext getServletContext() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public ServletConfig getServletConfig() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public SecurityContext getSecurityContext() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public <T, E> T getResolver(Class<T> arg0, Class<E> arg1) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Request getRequest() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Providers getProviders() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public HttpServletResponse getHttpServletResponse() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public HttpServletRequest getHttpServletRequest() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public HttpHeaders getHttpHeaders() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object getContextualProperty(Object arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public <T> T getContext(Class<T> arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public <T> T getContent(Class<T> arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Object get(Object arg0) {
				// TODO Auto-generated method stub
				return null;
			}
		};
  }
	
  protected SHServiceContext mockServiceContext(String sellerId) {
    SHServiceContext shServiceContext = new SHServiceContext();
    ExtendedSecurityContext securityContext = mockSecurityContext();
    shServiceContext.setExtendedSecurityContext(securityContext);
    shServiceContext.setAttribute(SHServiceContext.ATTR_ROLE, ProxyRoleTypeEnum.Super.getName());
    return shServiceContext;
  }

  protected ExtendedSecurityContext mockSecurityContext() {
		ExtendedSecurityContext securityContext = Mockito.mock(ExtendedSecurityContext.class);
		Mockito.when(securityContext.getUserId()).thenReturn("12345");
		Mockito.when(securityContext.getUserGuid()).thenReturn("AB12345");
		Map<String, Object> extendedInfo = new HashMap<String, Object>();
		extendedInfo.put("http://stubhub.com/claims/subscriber", "api_us_sell_indy03@testmail.com");
		Mockito.when(securityContext.getExtendedInfo()).thenReturn(extendedInfo);
		return securityContext;
	}
		
  private BaseMatcher getMatcher() {
        BaseMatcher matcher=new BaseMatcher() {
              @Override
              public boolean matches(Object item) {
                    return true;
              }

              @Override
      public void describeTo(Description description) {}

        };
        return matcher;
    }
    
    /////////// Other helper methods //////////

    /*
     * create getTicketTrait
   * 
     * @param id
   * 
     * @param name
   * 
     * @param op
   * 
     * @return
     */
  protected TicketTrait getTicketTrait(String id, String name, Operation op) {
		TicketTrait trait = new TicketTrait();
		trait.setId(id);
		trait.setName(name);
		trait.setOperation(op);
		return trait;
	}
	
    /**
     * Gets a list of ticket seats. The rows will be ordered starting with 1 .. count
   * 
     * @param section
     * @param row
     * @param count
     * @param noSeatNumber if true do not generate seat sumbers
     * @return
     */
  protected List<TicketSeat> getTicketSeats(long ticketId, String section, String row, int count,
      boolean noSeatNumber, boolean gaInd) {
		List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
		for(int i=0; i<count; i++){
			TicketSeat ts = new TicketSeat();
			ts.setTicketId ( ticketId );
			ts.setSeatStatusId(1L); 
			ts.setTixListTypeId(1L);
			ts.setGeneralAdmissionInd(gaInd);
			ts.setRow(row);
			ts.setSection(section);
			if ( !noSeatNumber )
				ts.setSeatNumber (String.valueOf(i+1) );
			ticketSeats.add(ts);
		}
		return ticketSeats;
	}
	
    /**
     * Gets a list of ticket seats. The rows will be ordered starting with 1 .. count
   * 
     * @param section
     * @param row
     * @param count
     * @param noSeatNumber if true do not generate seat sumbers
     * @return
     */
  protected List<TicketSeat> getTicketSeats(long ticketId, String section, String row, int count,
      boolean noSeatNumber) {
		return getTicketSeats (ticketId, section, row, count, noSeatNumber, false );
	}
		
    /**
     * Gets a list of ticket seats. The rows will be ordered starting with 1 .. count
   * 
     * @param section
     * @param row
     * @param count
     * @return
     */
  protected List<TicketSeat> getTicketSeats(long ticketId, String section, String row, int count) {
		return getTicketSeats ( ticketId, section, row, count, false);
	}    	
	
	/**
	 * Make a test parking pass
   * 
	 * @param ticketId
	 * @return
	 */
  protected TicketSeat getParkingPass(long ticketId) {
		TicketSeat seat = new TicketSeat();
		seat.setSection("Lot");
		seat.setRow("LOT");
		seat.setTicketId(ticketId);
		seat.setSeatNumber("Parking Pass");
		seat.setSeatDesc("Parking");
		seat.setGeneralAdmissionInd(false);
		seat.setTixListTypeId(2l);
		seat.setSeatStatusId(1l);
		Calendar utcNow = DateUtil.getNowCalUTC();
		seat.setCreatedDate(utcNow);
		seat.setLastUpdatedDate(utcNow);
		seat.setCreatedBy("UpdateListingAPIv2");
		seat.setLastUpdatedBy("UpdateListingAPIv2");
		return seat;		
	}
	
	
	/**
   * Gets Listing as it would be loaded from DB. The row can contain multiple csv rows. The seats
   * can also contain multiple csv values
   * 
	 * @param id
	 * @param section
	 * @param row
	 * @param seats
	 * @param quantity
	 * @param quantityRemain
	 * @return Listing
	 */
  protected Listing getListing(long id, String section, String row, String seats, int quantity,
      int quantityRemain) {
		Listing listing = new Listing();
		listing.setId(id);
		listing.setEventId(1000l);
		listing.setSellerId(1000l);
		listing.setListPrice(new Money(new BigDecimal(100d), "USD"));
		listing.setQuantity(quantity); 
		listing.setSplitOption((short)0);
		listing.setSplitQuantity(quantity);
		listing.setSection(section);
		listing.setRow( row );
		listing.setSeats( seats );
		listing.setSystemStatus(ListingStatus.ACTIVE.name());
		listing.setTicketMedium(TicketMedium.BARCODE.getValue());
		listing.setDeliveryOption(DeliveryOption.MANUAL.getValue());
		listing.setQuantityRemain(quantityRemain);
		listing.setListingType(1l);
		listing.setSaleMethod(1L);
		listing.setInhandDate(new GregorianCalendar());
		listing.setInhandInd(true);
		listing.setMinPricePerTicket(new Money(new BigDecimal(100d), "USD"));		
		listing.setMaxPricePerTicket(new Money(new BigDecimal(100d), "USD"));
		listing.setTicketCost((new Money(new BigDecimal(20d), "USD")));
		GregorianCalendar endDate = new GregorianCalendar();
		endDate.roll(Calendar.YEAR, true);
		listing.setEndDate(endDate);
		listing.setCurrency(Currency.getInstance("USD"));
		listing.setFaceValue(new Money(new BigDecimal(5d), "USD"));
		listing.setListingSource(10);
		return listing;
	}

	/**
	 * getPaymentInstruments
   * 
	 * @return a list of CustomerPaymentInstrumentDetails
	 */
  protected List<CustomerPaymentInstrumentDetails> getPaymentInstruments() {
    List<CustomerPaymentInstrumentDetails> paymentInstruments =
        new ArrayList<CustomerPaymentInstrumentDetails>();
		CustomerPaymentInstrumentDetails details = new CustomerPaymentInstrumentDetails();
		details.setBookOfBusinessId("1");
		details.setId("1002");
		details.setPaymentType("check");
		details.setDefaultPaymentInd("y");
		CheckDetails check = new CheckDetails();
		check.setPayByCompanyName("Stubhub");
		details.setCheckDetails(check);
		paymentInstruments.add(details);
		
		details = new CustomerPaymentInstrumentDetails();
		details.setBookOfBusinessId("1");
		details.setId("1001");
		details.setPaymentType("largeSellerCheck");
		details.setDefaultPaymentInd("n");
		check = new CheckDetails();
		check.setPayByCompanyName("Stubhub");
		details.setCheckDetails(check);
		paymentInstruments.add(details);
		
		details = new CustomerPaymentInstrumentDetails();
		details.setBookOfBusinessId("1");
		details.setId("1003");
		details.setPaymentType("Paypal");
		details.setDefaultPaymentInd("n");
		PayPalDetails paypalDetails = new PayPalDetails();
		paypalDetails.setMode("Payable");
		paypalDetails.setEmailAddress("test@test.com");
		details.setPaypalDetails(paypalDetails);
		paymentInstruments.add(details);		
		return paymentInstruments;
	}
	
	/**
   * Gets an event object that will expire 3 months from now. The event id is set == 1000L. Also
   * allow it to have supported trait comments
   * 
	 * @return
	 */
  protected Event getEvent(String[] traitComments, Long[] traitIds) {
    	Event event = getEvent();
    	List <Long> idsList = new ArrayList<Long>();
    	
    	if ( traitComments != null || traitIds != null ) {
      int len = (traitComments != null && traitComments.length > 0) ? traitComments.length
          : traitIds.length;
    		List<com.stubhub.domain.catalog.events.intf.TicketTrait> traitsList = 
    				new ArrayList<com.stubhub.domain.catalog.events.intf.TicketTrait>( len );

    		for ( int i=0; i<len; i++ ) {

    			com.stubhub.domain.catalog.events.intf.TicketTrait tt = 
    					new com.stubhub.domain.catalog.events.intf.TicketTrait();
    			if ( traitComments != null ) {
    				tt.setName(traitComments[i]);
    			}
    			if ( traitIds != null ) {
    				tt.setId( traitIds[i]  );
    				idsList.add( traitIds[i] );
    			}
    			traitsList.add( tt );
    		}
    		
    		event.setTicketTraitId(idsList);
    		event.setTicketTrait( traitsList );
    	}
    	return event;
    }
    
	/**
	 * Gets an event object that will expire 3 months from now. The event id is set == 1000L
   * 
	 * @return
	 */
  protected Event getEvent(DeliveryOptions doption) {
    	Event event = getEvent ();
    	
		List<FulfillmentMethod> fulfillmentMethods = new ArrayList<FulfillmentMethod>();
		FulfillmentMethod fulfillmentMethod  = new FulfillmentMethod();
		fulfillmentMethod.setName(doption);
		event.setFulfillmentMethods(fulfillmentMethods);

		return event;
    }
	
	/**
	 * Gets an event object that will expire 3 months from now. The event id is set == 1000L
   * 
	 * @return
	 */
  protected Event getEvent() {
		Event event = new Event();
		event.setId(1000l);
		event.setActive(true);
		event.setDescription("Event description");
		
		// Event date is: 1 month from TODAY
		GregorianCalendar eventDate = new GregorianCalendar();
		eventDate.add(Calendar.DAY_OF_MONTH, 15);
		event.setEventDate(eventDate);
		
		// Early inhand date: is eventDate - 15 days
		GregorianCalendar eih = new GregorianCalendar();
		eih.setTimeInMillis(eventDate.getTimeInMillis());
		eih.add(Calendar.DAY_OF_MONTH, -20);
		event.setEarliestPossibleInhandDate(eih);

		// Latest inhand date: is eventDate - 2 days
		GregorianCalendar lih = new GregorianCalendar();
		lih.setTimeInMillis(eventDate.getTimeInMillis());
		lih.add(Calendar.DAY_OF_MONTH, -2);		
		event.setLatestPossibleInhandDate(lih);
		
		event.setCurrency(Currency.getInstance("USD"));
		event.setJdkTimeZone(TimeZone.getTimeZone("US/Pacific"));
		return event;
	}

    /**
     * setEventDates 
   * 
     * @param event
     * @param edateDaysFromNow Event Date: positive or negative days from today's date
     * @param offDaysEIHD Event date offset positive or negative for EIHD
     * @param offDaysLIHD Event date offset positive or negative for LIHD
     */
  protected void setEventDates(Event event, int edateDaysFromNow, int offDaysEIHD,
      int offDaysLIHD) {
    	GregorianCalendar eventDate = new GregorianCalendar();
    	eventDate.add(Calendar.DAY_OF_MONTH, edateDaysFromNow);
		event.setEventDate(eventDate);
		
		// Early inhand date: is eventDate - 15 days
		GregorianCalendar eih = new GregorianCalendar();
		eih.setTimeInMillis(eventDate.getTimeInMillis());
		eih.add(Calendar.DAY_OF_MONTH, offDaysEIHD);
		event.setEarliestPossibleInhandDate(eih);

		// Latest inhand date: is eventDate - 2 days
		GregorianCalendar lih = new GregorianCalendar();
		lih.setTimeInMillis(eventDate.getTimeInMillis());
		lih.add(Calendar.DAY_OF_MONTH, offDaysLIHD);		
		event.setLatestPossibleInhandDate(lih);
    }
    
    /**
     * Create and return a TicketTrait
   * 
     * @param id
     * @param name
     * @param op
     * @return
     */
  protected TicketTrait getTicketTrail(String id, String name, Operation op) {
    	TicketTrait tt = new TicketTrait();
    	tt.setId(id);
    	tt.setName(name);
    	tt.setOperation(op);
    	return tt;
    }
    
    /**
     * getProduct creates a product object
   * 
     * @param op
     * @param row
     * @param seat
     * @param type
     * @param fulfillmentArtifact
     * @return
     */
  protected Product getProduct(Operation op, String row, String seat, ProductType type,
      String fulfillmentArtifact, String externalId) {
    	Product prod = new Product ();
    	prod.setOperation(op);
    	prod.setRow(row);
    	prod.setSeat(seat);
    	prod.setProductType(type);
    	prod.setFulfillmentArtifact(fulfillmentArtifact);
    	prod.setExternalId(externalId);
    	return prod;
    }
    
    /**
     * setBeanProperty
   * 
     * @param objInstance
     * @param propertyName
     * @param newVal
     * @throws Exception
     */
  protected void setBeanProperty(Object objInstance, String propertyName, Object newVal)
      throws Exception {
		Field[] fields = objInstance.getClass().getDeclaredFields();
		objInstance.getClass().getDeclaredMethods();

		if (fields != null) {
			for (Field field : fields) {
				if (field.getName().equalsIgnoreCase(propertyName)) {
					field.setAccessible(true);
					field.set(objInstance, newVal);
				}
			}
		}
	}
    
	/**
	 * Get PriceResponse object
   * 
	 * @param dispPrice
	 * @param sellFee
	 * @return
	 */
  protected PriceResponse getPriceResponse(String dispPrice, String sellFee) {
		PriceResponse pr = new PriceResponse();
		pr.setDisplayPrice(new Money(dispPrice, "USD"));
		
		SellFees sf = new SellFees();
		sf.setSellFee(new Money(sellFee, "USD"));
		pr.setSellFees(sf);
		return pr;
	}
    
	/**
	 * Get PriceResponseList sample object seeded by base prices and incremented by 5.0, 2.0
   * 
	 * @param baseDispPrice
	 * @param baseDispPrice
	 * @param countSuccess how many successful 
	 * @param countError how many errors in the response
	 * @return
	 */
  protected PriceResponseList getPriceResponseList(Float baseDispPrice, Float baseSellFee,
      int countSuccess, int countError) {
    	PriceResponseList rlist = new PriceResponseList();
    	List<PriceResponse>list = new ArrayList<PriceResponse>();	
    	
    	for ( int i=0; i<countSuccess; i++ ) {
    		baseDispPrice += 5.0f;
    		baseSellFee += 2.0f;
    		
    		list.add( getPriceResponse(String.valueOf(baseDispPrice), String.valueOf(baseSellFee ) ) );
    	}
    	
    	if ( countError > 0 ) {
    		
        	PriceResponse error = new PriceResponse();
      com.stubhub.domain.pricing.intf.aip.v1.error.Error e =
          new com.stubhub.domain.pricing.intf.aip.v1.error.Error();
        	e.setCode(ErrorCode.MINIMUM_LIST_PRICE_ERROR);
        	e.setType(ErrorType.BUSINESSERROR);
        	e.setMessage("Minimum listing price error");
        	e.setParameter("expected minimum listing price = Money [amount=6, currency=USD]");
      List<com.stubhub.domain.pricing.intf.aip.v1.error.Error> erl =
          new ArrayList<com.stubhub.domain.pricing.intf.aip.v1.error.Error>();
        	erl.add(e);
        	error.setErrors(erl);
        	
        	// add errors but in random places
        	for ( int i=0; i<countError; i++ ) {
        		if ( i==0 )   
        			list.add(i, error);
        		else
        			list.add( error );
        	}
    	}
    	
    	rlist.setPriceResponse( list );
    	return rlist;
	}
    
    /**
     * Helper to make listingData object from requests
   * 
     * @param sellerId
     * @param ctx
     * @param isCreate
     * @param createOrUpdateReq
     * @return  
     */
  protected ListingData listingDataFromCreateRequest(long sellerId, SHAPIContext ctx, SHServiceContext shServiceContext,
      boolean isBulk, ListingRequest req) {
		ListingToDataAdapter ldAdapter = new ListingToDataAdapter();
		List<ListingRequest> reqs = new ArrayList<ListingRequest>();
		reqs.add(req);
		return ldAdapter.listingDataFromCreateRequests(sellerId, "SELLER-GUID", ctx, shServiceContext, reqs, isBulk);
    }
    
    /**
     * Helper to make listingData object from requests
   * 
     * @param sellerId
     * @param ctx
     * @param isCreate
     * @param createOrUpdateReq
     * @return  
     */
  protected ListingData listingDataFromUpdateRequest(InventoryMgr inventoryMgr, long sellerId,
      SHAPIContext ctx, SHServiceContext shServiceContext, boolean isBulk, ListingRequest req) {
		ListingToDataAdapter ldAdapter = new ListingToDataAdapter();
		List<ListingRequest> reqs = new ArrayList<ListingRequest>();
		reqs.add(req);
    return ldAdapter.listingDataFromUpdateRequests(inventoryMgr, sellerId, "SELLER-GUID", ctx, shServiceContext, reqs,
        isBulk);
    }
    
    /**
     * makeListingResponse from listingId and system status
   * 
     * @param listingId
     * @param status
     * @return
     */
  protected ListingResponse makeListingResponse(String listingId, String status) {
    	ListingResponse resp = new ListingResponse();
    	resp.setId(listingId);
    	resp.setStatus (com.stubhub.domain.inventory.common.entity.ListingStatus.fromString(status)  );
    	return resp;
    }
    
    /**
     * Helper to make listingData object from requests
   * 
     * @param sellerId
     * @param ctx
     * @param isCreate
     * @param createOrUpdateReq
   * @return protected ListingData makeSingleListingDataFromRequest (InventoryMgr inventoryMgr, long
   *         sellerId, SHAPIContext ctx, boolean isCreate, ListingRequest req ) {
   *         ListingToDataAdapter ldAdapter = new ListingToDataAdapter(); return
   *         ldAdapter.listingDataFromSingleRequest(inventoryMgr, sellerId, "SELLER-GUID", ctx, req,
   *         isCreate); }
    */
}
