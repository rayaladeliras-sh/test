package com.stubhub.domain.inventory.listings.v2.helper;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.i18n.currencyconversion.v1.util.CurrencyConvertor;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.datamodel.dao.BulkUploadSellerDAO;
import com.stubhub.domain.inventory.datamodel.entity.BulkUploadSeller;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption;
import com.stubhub.domain.inventory.datamodel.entity.enums.FulfillmentMethod;
import com.stubhub.domain.inventory.datamodel.entity.enums.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.enums.TicketMedium;
import com.stubhub.domain.inventory.listings.v2.util.CurrencyConvertHelper;
import com.stubhub.domain.inventory.listings.v2.util.ListingPayout;
import com.stubhub.domain.inventory.listings.v2.util.ListingPayoutUtil;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.pricing.intf.aip.v1.request.PriceRequest;
import com.stubhub.domain.pricing.intf.aip.v1.request.PriceRequestList;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponse;
import com.stubhub.domain.pricing.intf.aip.v1.response.PriceResponseList;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("listingPriceDetailsHelper")
public class ListingPriceDetailsHelper {

    private static final String PAYOUT = "PAYOUT";

    private static final String LISTING_PRICE = "LISTING_PRICE";

    private static final String DISPLAY_PRICE = "DISPLAY_PRICE";

    private static double MAX_LIST_PRICE = 1000000.00d;
	
	private static String DEF_IGNORE = "IGNORE";
	
	private static final String STUBHUB_PRO = "StubHubPro";
	
	private static final String RELIST = "Relist";

	private static final String INDY = "Indy";

    private final static Logger LOG = LoggerFactory.getLogger(ListingPriceDetailsHelper.class);

    private static final String USD_TO_CAD_CURRENCY_CONVERTION_VALUE = "usd.to.cad.conversion.value";
    
    private static final String USD_TO_CAD_CURRENCY_CONVERTION_DEFAULT = "1.3514";

    
    @Autowired
    private MasterStubhubPropertiesWrapper masterStubhubProperties;

    @Autowired
    private BulkUploadSellerDAO bulkUploadSellerDAO;
    
    @Autowired
    private ListingPriceUtil listingPriceUtil;
	
    @Autowired
    private CurrencyConvertHelper currencyConvertHelper;	
    
    @Autowired
    private CurrencyConvertor currencyConvertor;
	
  /**
   * SHAPE-API call to figure out prices. The responses is a list of either pricing result or
   * ListingBusinessExceptions
   * 
   * @param context
   * @param currentListings currentListingMap
   * @param listings List of Listings
   * @return Object array of mix of ListingError, PriceResponse objects, or null == means Not
   *         Applicable
   * @throws Exception
   */
  public Object[] batchPriceCalculationsAIP(SHAPIContext context, Map<Long, Listing> curListingsMap,
      List<Listing> listings, List<ListingRequest> originalListingRequests) throws Exception {
    Object[] responses = new Object[listings.size()];
    String sellerPriceType = null;
    //Money sellerInputPriceData=null;
    
    PriceRequestList priceReqList = new PriceRequestList();
    List<PriceRequest> reqList = new ArrayList<PriceRequest>(listings.size());

    for (int i = 0; i < listings.size(); i++) {

      Listing listingRequest = listings.get(i);
      ListingRequest originalListingRequest = originalListingRequests.get(i);
      // this is null for create, and should be there for update
      Listing curListing = null;
      if (curListingsMap != null) {
        curListing = curListingsMap.get(listingRequest.getId());
      }

//		//Sales Tax changes
//		if (originalListingRequest.getPurchasePricePerProduct() != null
//			&& originalListingRequest.getPurchasePricePerProduct().getAmount() != null) {
//			if (originalListingRequest.getPurchasePricePerProduct().getAmount().compareTo(BigDecimal.ZERO) < 0) {
//				listingRequest.setPurchasePricePerProduct(null);
//				listingRequest.setPurchasePriceCurrency(null);
//			} else {
//				listingRequest.setPurchasePricePerProduct(originalListingRequest.getPurchasePricePerProduct());
//				listingRequest.setPurchasePriceCurrency(
//					Currency.getInstance(originalListingRequest.getPurchasePricePerProduct().getCurrency()));
//			}
//		}
//
//		if (originalListingRequest.getSalesTaxPaid() != null) {
//			listingRequest.setSalesTaxPaid(originalListingRequest.getSalesTaxPaid());
//		} else {
//			listingRequest.setSalesTaxPaid(true);
//		}

		//CDN ChangesO
		if ("CA".equalsIgnoreCase(listingRequest.getEvent().getCountry())) {
			LOG.info("The event belongs to Canada eventId={}",listingRequest.getEvent().getId());
			sellerPriceType= "pricePerProduct";
			Money sellerInputPrice = listingRequest.getListPrice();
			if (sellerInputPrice == null) {
				if (listingRequest.getSellerPayoutAmountPerTicket()!=null) {
					sellerInputPrice=listingRequest.getSellerPayoutAmountPerTicket();
					sellerPriceType="payoutPerProduct";
				}else if(listingRequest.getDisplayPricePerTicket()!=null) {
					sellerInputPrice=listingRequest.getDisplayPricePerTicket();
					sellerPriceType="buyerSeesPerProduct";
				}
			}
				if (sellerInputPrice != null && "USD".equalsIgnoreCase(sellerInputPrice.getCurrency())) {
					//BigDecimal fxUsdToCadExchnage = new BigDecimal(currencyConvertHelper.convertToCadCurrency("USD","CAD"));
					BigDecimal fxUsdToCadExchnage = new BigDecimal(getUSDToCADConversionValue());
					BigDecimal currentUSDPrice = sellerInputPrice.getAmount();
					BigDecimal newCadListPrice = currentUSDPrice.multiply(fxUsdToCadExchnage);
					BigDecimal sellerInputPriceAmount = currentUSDPrice;
					sellerInputPrice.setAmount(newCadListPrice);
					sellerInputPrice.setCurrency("CAD");
					Money sellerMoney = new Money(currentUSDPrice.toString(), "USD");
					listingRequest.setSellerInputPriceType(sellerPriceType);
					listingRequest.setSellerInputPrice(sellerMoney);
					listingRequest.setSellerInputCurrency(Currency.getInstance(Locale.US));
					listingRequest.setCurrency(Currency.getInstance(Locale.CANADA));
					if (listingRequest.getFaceValue() != null) {
						listingRequest.getFaceValue().setAmount(listingRequest.getFaceValue().getAmount().multiply(fxUsdToCadExchnage));
						listingRequest.getFaceValue().setCurrency(Currency.getInstance(Locale.CANADA).toString());
					}
					LOG.info("setting new CAD price using USD Price for eventId={} USD={} CAD={}", listingRequest.getEvent().getId(), sellerInputPriceAmount, newCadListPrice);
				}
		}


		ListingError error = validatePrices(listingRequest, curListing);
      if (error != null) {
        responses[i] = error;
        continue;
      }

      // if false ignore (i.e. no price calc should take place)
      if (needToCalculatePrices(listingRequest) == false || isZeroPrice(listingRequest.getListPrice())) {
        responses[i] = DEF_IGNORE;
        continue;
      }

      StringBuilder fulfillemntType = new StringBuilder();
      StringBuilder pdelType = new StringBuilder();

      if (curListing != null)
        getFulfillmentAndDeliveryTypes(curListing, fulfillemntType, pdelType);
      else
        getFulfillmentAndDeliveryTypes(listingRequest, fulfillemntType, pdelType);

      PriceRequest preq = new PriceRequest();

      // l.getId() == null for create listing
      preq.setListingId(null);
      preq.setRequestKey(String.valueOf(i));

      preq.setEventId(listingRequest.getEventId());
      preq.setFulfillmentType(fulfillemntType.toString());
      preq.setPredeliveryType(pdelType.toString());

      Money amountPerTcket = listingRequest.getSellerPayoutAmountPerTicket();

      if (amountPerTcket == null) {
        LOG.info("_message=\"Calculating the payout. AmountPerTicket is Null\" eventId={}  ",
            listingRequest.getEventId());
        if (listingRequest.getDisplayPricePerTicket() != null) {
          preq.setAmountPerTicket(listingRequest.getDisplayPricePerTicket());
          preq.setAmountType(DISPLAY_PRICE);
        } else if (listingRequest.getListPrice() != null) {
          preq.setAmountPerTicket(listingRequest.getListPrice());
          preq.setAmountType(LISTING_PRICE);
        }
      } else {
        if (listingRequest.isMarkup()) {
          calculateAmountFromPayout(listingRequest, preq);
        } else {
          preq.setAmountPerTicket(listingRequest.getSellerPayoutAmountPerTicket());
          preq.setAmountType(PAYOUT);

        }
      }
      
      Money faceValue = calculateFaceValue(originalListingRequest, listingRequest.getFaceValue());
      if(faceValue != null && faceValue.getAmount() != null ) {
        preq.setFaceValue(faceValue);
      } else if(curListing != null) {
          Money currentListingFaceValue = calculateFaceValue(originalListingRequest, curListing.getFaceValue());
          if(currentListingFaceValue != null && currentListingFaceValue.getAmount() != null ){
        	  preq.setFaceValue(currentListingFaceValue);
          }
      }

      if (curListing != null)
        preq.setSection(curListing.getSection());
      else
        preq.setSection(listingRequest.getSection());

      if (curListing != null)
        preq.setRow(curListing.getRow());
      else
        preq.setRow(listingRequest.getRow());

      String createdBy = listingRequest.getCreatedBy();
      if(curListing != null) {
    	  createdBy = curListing.getCreatedBy();
      }
      if(StringUtils.trimToEmpty(createdBy).contains(RELIST)) {
    	  preq.setListingSource(RELIST);
      } else if(isListingSourceIndy(createdBy)){
		  preq.setListingSource(INDY);
	  } else {
    	  preq.setListingSource(STUBHUB_PRO);
      }
      
    
      preq.setIncludePayout(true);
      preq.setAdjustToMinListPrice(false);
      //Added to make it work for App token while calling Pricing api.
      preq.setSellerId(listingRequest.getSellerId());
      preq.setSellerGuid(listingRequest.getSellerGuid());
      reqList.add(preq);
    }

    // if there are requests make them
    if (reqList.size() > 0) {
      priceReqList.setPriceRequest(reqList);
      PriceResponseList callPriceResp = listingPriceUtil.getListingPricingsAIP(context, priceReqList);
      List<PriceResponse> priceResponses = callPriceResp.getPriceResponse();

      int idx = 0;
      for (int j = 0; j < responses.length; j++) {
        if (responses[j] == null) {
          PriceResponse pr = priceResponses.get(idx);
          if (pr.getErrors() != null && pr.getErrors().size() > 0) {
            responses[j] = _listingError(pr);
          } else {
            responses[j] = pr;
          }
          idx++;
        } else if(DEF_IGNORE.equals(responses[j])) {
          responses[j] = null;
        }
      }    
    }
    
    for (int i = 0; i < responses.length; i++) {
      if (DEF_IGNORE.equals(responses[i])) {
        responses[i] = null;
      }
    }
    
    return responses;
  }

  private String getUSDToCADConversionValue() {
	  return masterStubhubProperties.getProperty(USD_TO_CAD_CURRENCY_CONVERTION_VALUE,USD_TO_CAD_CURRENCY_CONVERTION_DEFAULT);
  }
  
  private void calculateAmountFromPayout(Listing listingRequest, PriceRequest preq)
      throws Exception {
    LOG.info("_message=\"Calclating the payout. AmountPerTicket is not Null\" eventId={} listingId={} ",
        listingRequest.getEventId(), listingRequest.getId());
      Money amountPerTcket = listingRequest.getSellerPayoutAmountPerTicket();
      long sellerId = listingRequest.getSellerId();
      BulkUploadSeller bulkSeller = bulkUploadSellerDAO.get(sellerId);
      if (bulkSeller != null) {
        calculateAmountBySkipCalc(preq, amountPerTcket, bulkSeller);
      } else {
        preq.setAmountPerTicket(listingRequest.getSellerPayoutAmountPerTicket());
        preq.setAmountType(PAYOUT);
      }
  }


  private void calculateAmountBySkipCalc(PriceRequest preq, Money amountPerTcket,
      BulkUploadSeller bulkSeller) {
    ListingPayout lpayout = null;
    // If SkipDynamicCalculation is True
    if (bulkSeller.isSkipDynamicCalculation()) {
      lpayout = new ListingPayout(amountPerTcket, bulkSeller.getMarkUp());
      preq.setAmountPerTicket(ListingPayoutUtil.calculatePriceAfterMarkUp(lpayout));
      preq.setAmountType(LISTING_PRICE);
    } else {
      lpayout =
          new ListingPayout(amountPerTcket, bulkSeller.getMarkUp(),
              bulkSeller.getAutoBulkDefaultSellFee());
      preq.setAmountPerTicket(ListingPayoutUtil.calculateExpectedPayout(lpayout));
      preq.setAmountType(PAYOUT);
      LOG.info(
          "_message=\"Calculating MarkupPrice\" skipDynamicCal={} seller={} markup={} defaultSellFee={}  amountPerTcket={} calculatedAmount={}",
          bulkSeller.isSkipDynamicCalculation(), bulkSeller.getUserId(), bulkSeller.getMarkUp(),
          bulkSeller.getAutoBulkDefaultSellFee(), amountPerTcket, preq.getAmountPerTicket());
    }
  }
	
	private boolean needToCalculatePrices ( Listing l )
	{
		// if create
		if ( l.getListPrice() != null || l.getSellerPayoutAmountPerTicket() != null || l.getDisplayPricePerTicket() != null) {
			return true;
		}
		// CREATE NODE: start with 0 price as default (this can happen only for INCOMPLETE listings)
		else if ( l.getId() == null ) {	
			l.setListPrice(new Money("0.0"));
		}
		return false;
	}
	
	private boolean isZeroPrice(Money listingPrice) {
	    if(listingPrice != null && listingPrice.getAmount() != null) {
	        if(listingPrice.getAmount().compareTo(BigDecimal.ZERO) == 0) {
	            return true;
	        }
	    }
	    return false;
	}
	
	private ListingError _listingError ( PriceResponse pr )
	{
		com.stubhub.domain.pricing.intf.aip.v1.error.Error pe = pr.getErrors().get(0);
		String message = pe.getMessage();
		if(com.stubhub.domain.pricing.intf.aip.v1.error.ErrorCode.MINIMUM_LIST_PRICE_ERROR.equals(pe.getCode())) {
			message = pe.getMessage() + ":" + pe.getParameter();
		}
		if(com.stubhub.domain.pricing.intf.aip.v1.error.ErrorCode.MAXIMUM_LIST_PRICE_ERROR.equals(pe.getCode())) {
			message = pe.getMessage() + ":" + pe.getParameter();
		}
		ListingError listingError = new ListingError(pe.getType(), convertPricingToListingErrorCode(pe.getCode()), message, pe.getParameter());
		return listingError;
	}
	
	private ErrorCode convertPricingToListingErrorCode(com.stubhub.domain.pricing.intf.aip.v1.error.ErrorCode pricingErrorCode) {
		if(com.stubhub.domain.pricing.intf.aip.v1.error.ErrorCode.MINIMUM_LIST_PRICE_ERROR.equals(pricingErrorCode)) {
			return ErrorCode.LISTING_PRICE_TOO_LOW;
		}else if(com.stubhub.domain.pricing.intf.aip.v1.error.ErrorCode.MAXIMUM_LIST_PRICE_ERROR.equals(pricingErrorCode)) {
			return ErrorCode.LISTING_PRICE_TOO_HIGH;
		}else {
			return ErrorCode.PRICING_API_ERROR;
		}
	}
	
	private ListingError validatePrices ( Listing listing ,Listing curListing) 
	{
		// if CREATE request for ACTIVE listing you need to provide a price
		if (listing.getId() == null ) {
			if(!ListingStatus.INCOMPLETE.toString().equals(listing.getSystemStatus())) {
				if ( listing.getListPrice() == null && listing.getSellerPayoutAmountPerTicket() == null && listing.getDisplayPricePerTicket() == null ) {
					ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_PRICEPERTICKET, 
						"Invalid ticket price", "payoutPerProduct/buyerSeesPerProduct/pricePerProduct");
					return listingError;
				}
			}
		}
	//	listing.getFaceValue()
		Money amountPerTcket = listing.getSellerPayoutAmountPerTicket();
		String priceType = "payoutPerProduct";
		if(amountPerTcket == null) {
			if(listing.getDisplayPricePerTicket() != null) {
				amountPerTcket = listing.getDisplayPricePerTicket();
				priceType = "buyerSeesPerProduct";
			}
			else if(listing.getListPrice() != null) {
				amountPerTcket = listing.getListPrice();
				priceType = "pricePerProduct";
			} 
		}
		if (amountPerTcket != null && amountPerTcket.getCurrency() != null && listing.getCurrency() != null && 
				!(amountPerTcket.getCurrency().equals(listing.getCurrency().getCurrencyCode()))) {
			ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_CURRENCY, "Invalid currency", priceType);
			return listingError;
		}
		
		Currency listingCurrency = listing.getCurrency();
		if (curListing != null) {
			listingCurrency = curListing.getCurrency();
		}
		if (listing.getFaceValue() != null && listing.getFaceValue().getCurrency() != null && listingCurrency != null && 
				!(listing.getFaceValue().getCurrency().equals(listingCurrency.getCurrencyCode()))) {
			ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_CURRENCY, "Invalid currency", priceType);
		 	return listingError;
		}
		
		if(amountPerTcket != null && amountPerTcket.getAmount() != null && amountPerTcket.getCurrency() != null) {
			if (amountPerTcket.getAmount().doubleValue() > MAX_LIST_PRICE) {
				ListingError listingError = new ListingError(ErrorType.INPUTERROR, 
						ErrorCode.LISTPRICE_EXCEEDED_MAXLIMIT, "Price exceeding max allowed limit", priceType);
				return listingError;
			}
		}
		return null;
	}

	/**
	 * Set all pricing values in listing
	 * 
	 * @param listing new listing request
	 * @param curListing != null for updates (need to original listing for update)
	 * @param priceResponse that came out or pricing api
	 */
	public void setPricingValues ( Listing listing, Listing curListing, PriceResponse priceResponse )
	{
		double minPostingPrice = 0;
		
		Integer quantity = listing.getQuantity();
		Currency currency = listing.getCurrency();
		
		// UPDATE ONLY: get some original values 
		if ( curListing != null ) {
			quantity = curListing.getQuantity();
			currency = curListing.getCurrency();
		}
		
		// get values from price response
		if ( priceResponse.getMinListingPrice() != null ) {
			minPostingPrice = priceResponse.getMinListingPrice().getAmount().doubleValue();
		}
		Money displayPricePerTkt = priceResponse.getDisplayPrice();
		Money listingPricePerTkt = priceResponse.getListingPrice();
		Money sellFee = priceResponse.getSellFees().getSellFee();
		
		// set listing money values
		listing.setSellFeeValuePerTicket ( sellFee );
		listing.setSellFeeDescription("Seller Fee");
		
		
		BigDecimal totalSellFee = BigDecimal.valueOf(sellFee.getAmount().doubleValue()).multiply(BigDecimal.valueOf(quantity));
		listing.setTotalSellFeeValue ( new Money (totalSellFee, currency.getCurrencyCode() ));
		
		if (listingPricePerTkt.getAmount().doubleValue() < minPostingPrice) {
			ListingError listingError = new ListingError(ErrorType.INPUTERROR, ErrorCode.LISTING_PRICE_TOO_LOW, 
					"Price lesser than the lower limit", "pricePerTicket");
			throw new ListingBusinessException(listingError);
		}

		listing.setDisplayPricePerTicket(displayPricePerTkt);
		listing.setListPrice(listingPricePerTkt);

		Money totalListingPrice = new Money(listing.getListPrice().getAmount()
				.multiply(new BigDecimal(quantity)), listing
				.getListPrice().getCurrency());
		listing.setTotalListingPrice(totalListingPrice);
		
		// need to figure out payout
		Money payoutPerTicket = priceResponse.getPayout();
		listing.setSellerPayoutAmountPerTicket( payoutPerTicket );			
		
		if ( payoutPerTicket!=null && payoutPerTicket.getAmount().doubleValue() > 0 ) {
			BigDecimal totalPayout = BigDecimal.valueOf(payoutPerTicket.getAmount().doubleValue()).multiply(BigDecimal.valueOf(quantity)); 
			listing.setTotalSellerPayoutAmt ( new Money(totalPayout, currency.getCurrencyCode()) );
		}
		
		// UPDATE ONLY: set back in current listing 
		if ( curListing != null ) {
			curListing.setListPrice(listing.getListPrice());
			curListing.setTotalListingPrice(listing.getTotalListingPrice());
			curListing.setSellerPayoutAmountPerTicket (listing.getSellerPayoutAmountPerTicket() );
			curListing.setDisplayPricePerTicket(listing.getDisplayPricePerTicket());
			curListing.setTotalSellerPayoutAmt ( listing.getTotalSellerPayoutAmt() );
			curListing.setSellFeeValuePerTicket (listing.getSellFeeValuePerTicket() ) ;
			curListing.setSellFeeDescription (listing.getSellFeeDescription() );
			curListing.setTotalSellFeeValue (listing.getTotalSellFeeValue() );
			curListing.setPriceAdjusted(listing.isPriceAdjusted());
		}
	}
	
	private static final Integer MANUAL_CONFIRM = 3;

	private void getFulfillmentAndDeliveryTypes ( Listing listing, StringBuilder retFFType, StringBuilder retDelivType )
	{
		// TODO: What is the default delivery option
		if ( listing.getDeliveryOption()==null ) {
			// For now delivery option is always manual
			listing.setDeliveryOption(com.stubhub.domain.inventory.datamodel.entity.enums.DeliveryOption.MANUAL.getValue());
			listing.setConfirmOption(MANUAL_CONFIRM);
		}
		
		String fulfillmentType = "";
		//String preDeliveryType = "Regular";
		String preDeliveryType = "Manual";	// according to the pricing API it is "manual" 
		if(listing.getDeliveryOption() == DeliveryOption.PREDELIVERY.getValue()){
			preDeliveryType = "Predelivery";
		}
		// WILLCALL support
		if (null == listing.getFulfillmentMethod()) {
			if (listing.getFulfillmentDeliveryMethods() != null && listing.getFulfillmentDeliveryMethods().startsWith("8,10")) {
				listing.setFulfillmentMethod(FulfillmentMethod.OTHERPREDELIVERY);
			}
		}
		//currently SellerPro does not support electronic pre-delivery, need to revisit this logic when we support pre-delivery listings 
		if(FulfillmentMethod.FEDEX.equals(listing.getFulfillmentMethod())){
			fulfillmentType = "FEDEX";
		}else if(FulfillmentMethod.PDF.equals(listing.getFulfillmentMethod())){
			fulfillmentType = "PDF";
		}else if(FulfillmentMethod.BARCODE.equals(listing.getFulfillmentMethod())){
			fulfillmentType = "BARCODE";
		}else if(FulfillmentMethod.LMS.equals(listing.getFulfillmentMethod())){
			fulfillmentType = "LMS";
		}else if(FulfillmentMethod.LMSPREDELIVERY.equals(listing.getFulfillmentMethod())){
			fulfillmentType = "LMS";
			preDeliveryType = "Predelivery";
		}else if(FulfillmentMethod.UPS.equals(listing.getFulfillmentMethod())){
			fulfillmentType = "UPS";
		}else if(FulfillmentMethod.SHIPPING.equals(listing.getFulfillmentMethod())){
			fulfillmentType = "SHIPPING"; 
		}else if(listing.getTicketMedium()  == TicketMedium.BARCODE.getValue()){
			fulfillmentType = "BARCODE";
		}else if(listing.getTicketMedium() == TicketMedium.PDF.getValue()){
			fulfillmentType = "PDF";
		}else if(listing.getTicketMedium() == TicketMedium.FLASHSEAT.getValue()){
			fulfillmentType = "FLASHSEAT";
		}else if(listing.getTicketMedium() == TicketMedium.EXTFLASH.getValue()){
			fulfillmentType = "EXTERNALFLASHTRANSFER";
		}else if(listing.getTicketMedium() == TicketMedium.EXTMOBILE.getValue()){
			fulfillmentType = "EXTERNALMOBILETRANSFER";
		}else if(listing.getTicketMedium() == TicketMedium.MOBILE.getValue()){
			fulfillmentType = "MOBILE";
		} else if (FulfillmentMethod.OTHERPREDELIVERY.equals(listing.getFulfillmentMethod())) {
			fulfillmentType = "OTHER";
			preDeliveryType = "Predelivery";
		} else{
			String fmDMList = listing.getFulfillmentDeliveryMethods()==null?"":listing.getFulfillmentDeliveryMethods();
			if(fmDMList.contains("|7,") || fmDMList.startsWith("7,")){
                fulfillmentType = "LMS";
			}
			else if( isLMSPredelivery(listing, fmDMList)){								
	            fulfillmentType = "LMS";
	            preDeliveryType = "Predelivery";			
			}else if(fmDMList.contains("|10,") || fmDMList.startsWith("10,")){
				fulfillmentType = "UPS";
			}else if(fmDMList.contains("|17,") || fmDMList.startsWith("17,")){
                fulfillmentType = "LOCALDELIVERY";
            }else if(fmDMList.contains("|11,") || fmDMList.contains("|12,") || fmDMList.startsWith("11,") || fmDMList.startsWith("12,")){
				fulfillmentType = "SHIPPING";
			}else{
				fulfillmentType = "LMS";
			}
		}
		retFFType.append(fulfillmentType);
		retDelivType.append(preDeliveryType);
	}

	private boolean isLMSPredelivery(Listing listing, String fmDMList) {
		if(fmDMList.contains("|9,") || fmDMList.startsWith("9,")){
			if(listing.getLmsApprovalStatus() != null && listing.getLmsApprovalStatus() == 2){
				return true;
			}
		}		
		return false;
	}

	private boolean isListingSourceIndy(String createdBy) {
		createdBy = StringUtils.trimToEmpty(createdBy);
		if(StringUtils.isNotEmpty(createdBy)){
			createdBy = createdBy.toLowerCase();
			if (createdBy.contains("access@stubhub.com") ||
					createdBy.contains("corp.ebay.com") ||
					createdBy.contains("siebel") ||
					createdBy.contains("sth")) {
				LOG.info("_message=\"listing source is Indy\" listingsource={} createdBy={}", INDY, createdBy);
				return true;
			}
		}
		return false;
	}
	private Money calculateFaceValue(ListingRequest originalListingRequest, Money listingFaceValue){
		Money result = new Money();
		
		List<Product> products = originalListingRequest.getProducts();
		Money minimumFaceValue = new Money();
		if(products != null && products.size() > 0){
			minimumFaceValue = products.get(0).getFaceValue();
			if(products.size() > 1){
				for(int i=1; i< products.size(); i++){
					Product product = products.get(i);
					if(product.getFaceValue() != null && minimumFaceValue != null){
						if(product.getFaceValue().getAmount() != null && minimumFaceValue.getAmount() != null ){
							if(minimumFaceValue.getAmount().compareTo(product.getFaceValue().getAmount()) > 0){
								minimumFaceValue = product.getFaceValue();
							}
						}
					}
				}
			}
		}
		
		if (minimumFaceValue != null && minimumFaceValue.getAmount() != null){
			result = minimumFaceValue;
		    LOG.info("_message=\"minimum FaceValue at ticket seat \" minimumFaceValue={} ", minimumFaceValue);
		}else if(listingFaceValue != null && listingFaceValue.getAmount() != null){
			result = listingFaceValue ;
		    LOG.info("_message=\"listing FaceValue \" listingFaceValue={} ", listingFaceValue);
		}
		
		return result;
	}
}


