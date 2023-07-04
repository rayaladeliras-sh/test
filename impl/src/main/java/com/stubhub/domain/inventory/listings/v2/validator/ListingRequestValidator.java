package com.stubhub.domain.inventory.listings.v2.validator;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.entity.DeliveryOption;
import com.stubhub.domain.inventory.common.entity.ErrorEnum;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.common.util.ListingError;
import com.stubhub.domain.inventory.listings.v2.util.CmaValidator;
import com.stubhub.domain.inventory.listings.v2.util.ListingTextValidatorUtil;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.Product;
import com.stubhub.domain.inventory.v2.DTO.TicketTrait;
import com.stubhub.domain.inventory.v2.enums.LMSApprovalStatus;
import com.stubhub.domain.inventory.v2.enums.Operation;
import com.stubhub.domain.inventory.v2.enums.ProductType;
import com.stubhub.domain.inventory.v2.enums.ProxyRoleTypeEnum;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.newplatform.common.util.SecurityUtil;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("listingRequestValidator")
public class ListingRequestValidator {

	private static final Logger logger = LoggerFactory.getLogger(ListingRequestValidator.class);

	private static int MAX_ALLOWED_COMMENTS_LENGTH = 1024;
	private static int MAX_ALLOWED_QUANTITY = 150;
	private static int MAX_ALLOWED_SECTION_LENGTH = 100;
	private static int MAX_ALLOWED_ROW_LENGTH = 20;
	private static int MAX_ALLOWED_SEAT_LENGTH = 25;
	private static int MAX_ALLOWED_EXTERNAL_ID_LENGTH = 50;
	private static int MAX_ALLOWED_TEALEAF_SESSION_ID_LENGTH = 32;
	private static int MAX_ALLOWED_THREAT_MATRIX_REFID_LENGTH = 32;
	private static int MAX_ALLOWED_BARCODE_LENGTH = 50;
	private static int MAX_ALLOWED_EXTERNAL_SEAT_ID = 32;
	private static int MAX_ALLOWED_TRAIT_NAME = 1000;

	private static final String FACE_VALUE_COUNTRIES = "GB,LU,AT,DE";

	private static final String SEAT_MANDATORY_COUNTRIES = "GB";

	private static final String UNIQUE_TKT_NUM_MANDATORY_COUNTRIES = "GB";

	private static final String SEAT_TRAIT_ID_MEMBER = "15880";
	private static final String SEAT_TRAIT_NAME_MEMBER = "You need to be a member to buy a ticket in this section";

  static final String RELIST_MARKER= "Relist|V2|";

  @Autowired
	private ListingTextValidatorUtil listingTextValidatorUtil;

	@Autowired
	private MasterStubhubPropertiesWrapper masterStubhubProperties;

	@Autowired
	private CmaValidator cmaValidator;

	public List<ListingError> validate(ListingRequest listingRequest, Long eventId, boolean isCreate, boolean isBulk, Locale locale, String subscriber,
																		 String operatorId, ProxyRoleTypeEnum role, String eventCountry, Currency eventCurrency) {
	    String faceValueCountries = masterStubhubProperties.getProperty("facevalue.required.countries", FACE_VALUE_COUNTRIES);
	    boolean seatNumberValidationUKEnabled = Boolean.parseBoolean(masterStubhubProperties.getProperty("seatnumber.validation.UK.enabled", "false"));
	    boolean uniqueTicketNumberValidationUKEnabled = Boolean.parseBoolean(masterStubhubProperties.getProperty("uniqueTicket.Number.validation.UK.enabled", "false"));

	    List<String> faceValueCountriesList = Arrays.asList(faceValueCountries.split(","));
	    List<String> seatMandatoryCountriesList = Arrays.asList(SEAT_MANDATORY_COUNTRIES.split(","));
	    List<String> uniqueTktNumMandatCountryList = Arrays.asList(UNIQUE_TKT_NUM_MANDATORY_COUNTRIES.split(","));

		List<ListingError> errors = new ArrayList<ListingError>();
		if (listingRequest == null) {
			return errors;
		}
		// only for create (and if part of bulk listing)
		if ( isBulk && isCreate ) {
			// require externalListingId
			if ( StringUtils.isEmpty(listingRequest.getExternalListingId()) ) {
				errors.add(new ListingError(ErrorType.INPUTERROR,
						ErrorCode.INPUT_ERROR, "ExternalListingId is needed for Bulk Listing Request", "externalListingId"));
			}
		}

		if ( !_isAmountValid (listingRequest.getPayoutPerProduct()) ) {
			errors.add(new ListingError(ErrorType.INPUTERROR,
					ErrorCode.INVALID_PRICEPERTICKET, "Invalid price", "payoutPerProduct"));
		}
		if(isCreate) {
			if(DeliveryOption.STH == listingRequest.getDeliveryOption() && listingRequest.getPricePerProduct() == null) {
				errors.add(new ListingError(ErrorType.INPUTERROR,
						ErrorCode.INVALID_PRICEPERTICKET, "Invalid price", "pricePerProduct"));
			}
		}
		if ( !_isAmountValid (listingRequest.getPurchasePrice()) ) {
			errors.add(new ListingError(ErrorType.INPUTERROR,
					ErrorCode.INVALID_PRICEPERTICKET, "Invalid price", "purchasePrice"));
		}
		if ( !_isAmountValid (listingRequest.getPricePerProduct()) ) {
			ListingStatus listingStatus = listingRequest.getStatus();
			 			if(!ListingStatus.HIDDEN.equals(listingStatus)){
			errors.add(new ListingError(ErrorType.INPUTERROR,
					ErrorCode.INVALID_PRICEPERTICKET, "Invalid price", "pricePerProduct"));
			 			}
		}

		if(!_isPurchasePricePerProductValid(listingRequest.getPurchasePricePerProduct(), isCreate)) {
			errors.add(new ListingError(ErrorType.INPUTERROR,
				ErrorCode.INVALID_PURCHASE_PRICE_PER_PRODUCT, "Invalid price", "purchasePricePerProduct"));
		}

		if ( !_isAmountValid (listingRequest.getBuyerSeesPerProduct()) ) {
			errors.add(new ListingError(ErrorType.INPUTERROR,
					ErrorCode.INVALID_PRICEPERTICKET, "Invalid price", "buyerSeesPerProduct"));
		}

		boolean isFaceValueCheckRequired = _isFaceValueCheckRequired(faceValueCountriesList, eventCountry);
		boolean isFaceValueAtListingLevelValid = true;
		if (isFaceValueCheckRequired && !isReList(subscriber)) {
			if (isCreate) {
				isFaceValueAtListingLevelValid = _isFaceValueAtListingLevelValid(listingRequest) && _isAmountValid (listingRequest.getFaceValue());
			}
			if (!_isAmountValid (listingRequest.getFaceValue())) {
			        errors.add(new ListingError(ErrorType.INPUTERROR,
			            ErrorCode.INVALID_FACE_VALUE, ErrorEnum.INVALID_FACE_VALUE.getMessage(), "faceValue"));
			}
		}


		if ( !_isQuantityValid (listingRequest.getQuantity()) ) {
			errors.add(new ListingError(ErrorType.INPUTERROR,
					ErrorCode.INVALID_QUANTITY, ErrorEnum.INVALID_QUANTITY.getMessage(), "quantity"));
		}
		if (listingRequest.getQuantity() != null
				&& listingRequest.getQuantity() > MAX_ALLOWED_QUANTITY) {
			errors.add(new ListingError(ErrorType.INPUTERROR,
					ErrorCode.MAX_ALLOWED_EXCEEDED, ErrorEnum.MAX_ALLOWED_EXCEEDED.getMessage(), "quantity"));
		}

		if (listingRequest.getInternalNotes() != null
				&& listingRequest.getInternalNotes().length() > MAX_ALLOWED_COMMENTS_LENGTH) {
			errors.add(new ListingError(ErrorType.INPUTERROR,
					ErrorCode.MAX_LENGTH_EXCEEDED, ErrorEnum.MAX_LENGTH_EXCEEDED.getMessage(), "comments"));
		}
		if( StringUtils.trimToNull(listingRequest.getInternalNotes()) != null ){
			listingRequest.setInternalNotes(SecurityUtil.makeTextSafe(listingRequest.getInternalNotes()));
		}
		if (listingRequest.getSection() != null){
			if(listingRequest.getSection().trim().length() > MAX_ALLOWED_SECTION_LENGTH) {
				errors.add(new ListingError(ErrorType.INPUTERROR,
						ErrorCode.MAX_LENGTH_EXCEEDED, ErrorEnum.MAX_LENGTH_EXCEEDED.getMessage(), "section"));
			}else if(!"".equals(listingRequest.getSection().trim())){
				// Remove special characters from section
				listingRequest.setSection(listingTextValidatorUtil.removeSpecialCharactersFromSection(listingRequest.getSection(), locale) );
			}
		}

		if (listingRequest.getTealeafSessionId() != null &&
				listingRequest.getTealeafSessionId().length() > MAX_ALLOWED_TEALEAF_SESSION_ID_LENGTH) {
			errors.add(new ListingError(ErrorType.INPUTERROR, ErrorCode.MAX_LENGTH_EXCEEDED, "Invalid Tealeaf session ID", "tealeafSessionId"));
		}
		if (listingRequest.getThreatMatrixSessionId() != null &&
				listingRequest.getThreatMatrixSessionId().length() > MAX_ALLOWED_THREAT_MATRIX_REFID_LENGTH) {
			errors.add(new ListingError(ErrorType.INPUTERROR, ErrorCode.MAX_LENGTH_EXCEEDED, "Invalid ThreatMatrix Session ID", "threatMatrixSessionId"));
		}
		if (StringUtils.trimToNull(listingRequest.getExternalListingId()) != null) {
			if(listingRequest.getExternalListingId().length() > MAX_ALLOWED_EXTERNAL_ID_LENGTH) {
				errors.add(new ListingError(ErrorType.INPUTERROR, ErrorCode.MAX_LENGTH_EXCEEDED, "External Listing ID length is greater than 50 characters", "externalListingId"));
			} else {
				listingRequest.setExternalListingId(listingTextValidatorUtil.removeSpecialCharactersFromRowSeat(listingRequest.getExternalListingId().trim(), locale));
			}
		}
		errors.addAll(validateLMSApprovalStatusAndCheckAccess(listingRequest, operatorId, role));


		// if passed products
		if (listingRequest.getProducts() != null && !listingRequest.getProducts().isEmpty()) {
			if ( listingRequest.getQuantity() != null ) {
				if ( !isCreate ) {	// only for update
					errors.add(new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_QUANTITY,
							"Error cannot pass quantity and products array (you can pass either but not both)", "quantity"));
					return errors;
				}
			}

			for ( Product product : listingRequest.getProducts() ) {

				if ( StringUtils.trimToNull(product.getExternalId()) != null ) {
					if ( product.getExternalId().length() > MAX_ALLOWED_EXTERNAL_SEAT_ID ) {
						errors.add(new ListingError(ErrorType.INPUTERROR, ErrorCode.MAX_LENGTH_EXCEEDED,
								"Invalid externalId in product object", "externalId"));
					}
					String newExtId = ListingTextValidatorUtil.stripSpecialCharactersForId(product.getExternalId());
					product.setExternalId ( newExtId );
				}

				// get some of the values (and eliminate nulls)
				String row = product.getRow()==null? "" :
					listingTextValidatorUtil.removeSpecialCharactersFromRowSeat (product.getRow(), locale);
				String seat = product.getSeat()==null? "" :
					listingTextValidatorUtil.removeSpecialCharactersFromRowSeat (product.getSeat(), locale);

				String fullArtifact = product.getFulfillmentArtifact()==null? "" :
					product.getFulfillmentArtifact();


				if ( row.indexOf(',') >= 0 ) {
					errors.add(new ListingError(ErrorType.INPUTERROR,
							ErrorCode.INVALID_ROW, "Comma seperated values for row are not allowed", "row"));
				}
				if ( seat.indexOf(',') >= 0 ) {
					errors.add(new ListingError(ErrorType.INPUTERROR,
							ErrorCode.INVALID_SEAT_NUMBER, "Comma seperated values for seat are not allowed", "seat"));
				}


				if ( !_isValidArtifact(fullArtifact) ) {
					errors.add(new ListingError(ErrorType.INPUTERROR,
							ErrorCode.MAX_LENGTH_EXCEEDED, "FulfillmentArtifact cannot be null or greater than " + MAX_ALLOWED_BARCODE_LENGTH +
							" characters in ticket objects", "barcode"));
				}

				if(uniqueTicketNumberValidationUKEnabled && uniqueTktNumMandatCountryList.contains(eventCountry) && !StringUtils.isBlank(fullArtifact) ){
					if(StringUtils.isBlank(product.getUniqueTicketNumber())){
						errors.add(new ListingError(ErrorType.INPUTERROR,
								ErrorCode.UNIQUE_TICKET_NUMBER_CANNOT_BE_NULL_OR_EMPTY , "Unique Ticket Number cannot be null or Empty" , "seat"));
					}
				}

				if(isCreate){
					if ( StringUtils.isBlank(row) ) {
						errors.add(new ListingError(ErrorType.INPUTERROR,
								ErrorCode.ROW_CANNOT_BE_NULL_OR_EMPTY, "Row cannot be null or Empty" , "row"));
					}

					if(seatNumberValidationUKEnabled){
						if ( StringUtils.isBlank(seat) && seatMandatoryCountriesList.contains(eventCountry) && ! _isGASection(listingRequest.getSection())) {
						errors.add(new ListingError(ErrorType.INPUTERROR,
								ErrorCode.SEAT_CANNOT_BE_NULL_OR_EMPTY , "Seat cannot be null or Empty" , "seat"));
						}
					}

				}
				if (isCmaEvent(listingRequest, eventCountry, eventCurrency)) {
					if (!cmaValidator.isValidRow(eventId, product, listingRequest.getSection())) {
						errors.add(new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_ROW_WORDS,
								"Row/Seat contains words that are not allowed", "row"));
					}
					if (!StringUtils.isBlank(seat) && !cmaValidator.isValidSeat(seat)) {
						errors.add(new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_SEAT_NUMBER,
								"Row/Seat contains words that are not allowed", "seat"));
					}
				}
				if ( !_isValidSeat(seat) ) {
					errors.add(new ListingError(ErrorType.INPUTERROR,
							ErrorCode.MAX_LENGTH_EXCEEDED, "Seat cannot be null or greater than " + MAX_ALLOWED_SEAT_LENGTH +
							" characters in ticket objects", "seat"));
				}
				if ( !_isValidRow(row) ) {
					errors.add(new ListingError(ErrorType.INPUTERROR,
							ErrorCode.MAX_LENGTH_EXCEEDED, "Row cannot be null or greater than " + MAX_ALLOWED_ROW_LENGTH +
							" characters in ticket objects", "row"));
				}
				if ( !_isValidOperation(product.getOperation()) ) {
					errors.add(new ListingError(ErrorType.INPUTERROR,
							ErrorCode.TICKET_OPERATION_INVALID, "Invalid product operation passed", "operation"));
				}

				if(isCreate && !isFaceValueAtListingLevelValid) {
				    if(product.getFaceValue() == null || product.getFaceValue().getAmount() == null) {
				        errors.add(new ListingError(ErrorType.INPUTERROR,
                            ErrorCode.INVALID_FACE_VALUE, "Facevalue cannot be Null or Empty", "faceValue"));
                    }

				    if (!_isAmountValid (product.getFaceValue())) {
				        errors.add(new ListingError(ErrorType.INPUTERROR,
                            ErrorCode.INVALID_FACE_VALUE, ErrorEnum.INVALID_FACE_VALUE.getMessage(), "faceValue"));
                    }
				}

				// set new values anyway
				product.setRow( row );
				product.setSeat( seat );
				product.setFulfillmentArtifact(fullArtifact);
			}

			//Validate listing requests with duplicate row and seat numbers
			boolean dupsForSeatsAndRows = validateDupsForSeatsAndRows(listingRequest.getProducts());
			if (dupsForSeatsAndRows) {
			  errors.add(new ListingError(ErrorType.INPUTERROR,
                  ErrorCode.INVALID_SEAT_NUMBER, "Duplicate seat numbers not allowed", "seat"));
			}

			boolean duplicateExternalSeatId = validateDupsForExternalSeatId(listingRequest.getProducts());
			if(duplicateExternalSeatId) {
			  errors.add(new ListingError(ErrorType.INPUTERROR,
                  ErrorCode.INVALID_SEAT_NUMBER, "Duplicate external seat IDs not allowed", "externalId"));
			}

			if (isCreate) {
			  int productsWithFaceValue = 0;
			  int productsWithoutFaceValue = 0;
			  for ( Product p : listingRequest.getProducts() ) {
			    if(p.getFaceValue() != null && p.getFaceValue().getAmount() != null && p.getFaceValue().getAmount().doubleValue() > 0) {
			      productsWithFaceValue++;
			    } else {
			      productsWithoutFaceValue++;
			    }
			  }
			  if (listingRequest.getProducts().size() != productsWithFaceValue && listingRequest.getProducts().size() != productsWithoutFaceValue && !isReList(subscriber)) {
			    errors.add(new ListingError(ErrorType.INPUTERROR,
			        ErrorCode.INVALID_FACE_VALUE, "Inconsistent face values", "faceValue"));
			  }
			}

			if(!isCreate) {
			  boolean isPredelivery = false;
			  boolean isAdd = false;
			  for ( Product p : listingRequest.getProducts()) {
                if(Operation.ADD.equalsEnum(p.getOperation())) {
                  isAdd = true;
                } else if(Operation.UPDATE.equalsEnum(p.getOperation()) && StringUtils.trimToNull(p.getFulfillmentArtifact()) != null) {
                  isPredelivery = true;
                }
              }

			  if(isPredelivery && isAdd) {
			    errors.add(new ListingError(ErrorType.INPUTERROR,
                    ErrorCode.LISTING_ACTION_NOTALLOWED, "Cannot add seats during predelivey", ""));
			  }
			}
		}
		else if ( isCreate && (listingRequest.getQuantity()==null || listingRequest.getQuantity()==0) ) {
			errors.add(new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_QUANTITY,
					"Cannot create listing without quantity or products array", "quantity") );
		}
		else if (isCreate && !isFaceValueAtListingLevelValid) {
		    errors.add(new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_FACE_VALUE,
		            ErrorEnum.INVALID_FACE_VALUE.getMessage(), "faceValue"));
		}

		if ( listingRequest.getTicketTraits() != null ) {
			for ( TicketTrait trait : listingRequest.getTicketTraits() )
			{
				String id = trait.getId();
				String name = trait.getName();
				Operation op = trait.getOperation();

				if ( StringUtils.trimToNull(id)==null && StringUtils.trimToNull(name)==null ) {
					errors.add(new ListingError(ErrorType.INPUTERROR,
							ErrorCode.INVALID_TICKET_TRAIT, "Need to provide either id or name for Ticket Trait", "ticketTrait"));
				}
				if ( StringUtils.trimToNull(name)!=null ) {
					if ( name.length() > MAX_ALLOWED_TRAIT_NAME ) {
						errors.add(new ListingError(ErrorType.INPUTERROR,
								ErrorCode.INVALID_TICKET_TRAIT, "Invalid Ticket Trait name length", "name"));
					}
				}
				if ( StringUtils.trimToNull(id)!=null ) {
					if ( !_isValidInteger (id) ) {
						errors.add(new ListingError(ErrorType.INPUTERROR,
								ErrorCode.INVALID_TICKET_TRAIT, "Invalid integer for Seat Trait id", "id"));
					}
				}
				if ( !_isValidOperation( op ) ) {
					errors.add(new ListingError(ErrorType.INPUTERROR,
							ErrorCode.INVALID_TICKET_TRAIT, "Invalid Ticket Trait operation passed", "operation"));
				}
				if ( op != null && op.equals(Operation.UPDATE) ) {
					errors.add(new ListingError(ErrorType.INPUTERROR,
							ErrorCode.INVALID_TICKET_TRAIT, "Ticket Trait operation UPDATE is not allowed", "operation"));
				}
				if ( isPartnerTicketsCategory (id, name) ) {
				    errors.add(new ListingError(ErrorType.INPUTERROR,
                            ErrorCode.INVALID_TICKET_TRAIT, "Invalid ticket traits in the request", ""));
				}
			}
		}

		return errors;
	}

    private boolean isCmaEvent(ListingRequest listingRequest, String eventCountry, Currency eventCurrency) {
		if (!"GB".equalsIgnoreCase(eventCountry)) {
			return false;
		}
		if (listingRequest.getPricePerProduct() != null) {
			return "GBP".equalsIgnoreCase(listingRequest.getPricePerProduct().getCurrency());
		}
		if (eventCurrency != null) {
			return "GBP".equalsIgnoreCase(eventCurrency.getCurrencyCode());
		}
		return false;
    }

	List<ListingError> validateLMSApprovalStatusAndCheckAccess(ListingRequest listingRequest, String operatorId, ProxyRoleTypeEnum role) {

		Integer lmsApprovalStatus = listingRequest.getLmsApprovalStatus();

		if (lmsApprovalStatus != null) {
			if (StringUtils.isBlank(operatorId) || role == null || (role != ProxyRoleTypeEnum.Super)) {
				ListingError listingError = new ListingError(ErrorType.AUTHORIZATIONERROR, ErrorCode.ACCESS_TO_LMSAPPROVAL_STATUS_DENIED, "Access denied", "lmsApprovalStatus");
				throw new ListingBusinessException(listingError);
			}
			if (LMSApprovalStatus.getById(lmsApprovalStatus) == null) {
				List<ListingError> result = new ArrayList<ListingError>();
				result.add(new ListingError(ErrorType.INPUTERROR, ErrorCode.INVALID_LMS_STATUS, "Wrong value of LMS Approval Status", "lmsApprovalStatus"));
				return result;
			}
		}

		return Collections.emptyList();
	}
	
	private boolean validateDupsForSeatsAndRows(List<Product> products) {
		HashMap<String, Set<String>> rowSeat = new HashMap<String, Set<String>>();
		Set<String> seatsSetAdded = new HashSet<String>();
		if (products != null && !products.isEmpty()) {
			for (Product product : products) {
				if (null != product && ProductType.TICKET.equalsEnum(product.getProductType())
						&& null != product.getSeat()) {
					String seat = product.getSeat().replaceAll("[^a-zA-Z0-9]", "").trim();
					if (null != seat && seat.length() > 0) {
						if (rowSeat.get(StringUtils.trimToNull(product.getRow())) == null) {
							Set<String> seatsSet = new HashSet<String>();
							seatsSet.add(seat.toLowerCase());
							rowSeat.put(StringUtils.trimToNull(product.getRow()), seatsSet);
						} else {
						    seatsSetAdded = rowSeat.get(StringUtils.trimToNull(product.getRow()));
							Boolean isAdded = seatsSetAdded.add(seat.toLowerCase());
							if (!isAdded) {
								return true;
							}
							else
								rowSeat.put(StringUtils.trimToNull(product.getRow()), seatsSetAdded);
						}
					}
				}
			}
		}
		return false;
	}

	private boolean validateDupsForExternalSeatId(List<Product> products) {
	  Set<String> externalSeatIds = new HashSet<String>();
	  for(Product product : products) {
	    if(StringUtils.trimToNull(product.getExternalId()) != null) {
	      boolean isAdded = externalSeatIds.add(product.getExternalId().trim().toLowerCase());
	      if(!isAdded) {
	        return true;
	      }
	    }
	  }
	  return false;
	}

	private boolean _isValidInteger ( String intValue )
	{
		boolean valid = false;
		try {
			Integer.valueOf(intValue);
			valid = true;
		}
		catch ( NumberFormatException nfe) {}
		return valid;
	}

	private boolean _isQuantityValid ( Integer qty )
	{
		boolean valid = true;
		if ( qty != null ) {
			valid = qty.intValue()>0;
		}
		return valid;
	}

	private boolean _isAmountValid(Money money) {
		boolean valid = true;
		if (money != null) {
			valid = money.getAmount() != null && money.getAmount().doubleValue() > 0;
		}
		return valid;
	}

	private boolean _isPurchasePricePerProductValid ( Money money, boolean isCreate )
	{
		boolean valid = true;
		if ( money!=null ) {
			if(isCreate)
				valid = money.getAmount()!=null && money.getAmount().doubleValue() >= 0;
			else
				valid = money.getAmount()!=null;
		}
		return valid;
	}


	private boolean _isValidOperation( Operation op )
	{
		return op != null && op.name() != null ;
	}

	private boolean _isValidSeat ( String seat )
	{
		boolean valid = true;
		if ( !_isEmpty(seat) )
			valid = seat.length() <= MAX_ALLOWED_SEAT_LENGTH;
		return valid;
	}

	private boolean _isValidRow ( String row )
	{
		boolean valid = true;
		if ( !_isEmpty(row) )
			valid = row.length() <= MAX_ALLOWED_ROW_LENGTH;
		return valid;
	}

	private boolean _isValidArtifact ( String artifact )
	{
		boolean valid = true;
		if ( !_isEmpty(artifact) )
			valid = artifact.length() <= MAX_ALLOWED_BARCODE_LENGTH;
		return valid;
	}

	private boolean _isEmpty ( String val )
	{
		return ( val == null || val.trim().length()==0 );
	}

    private boolean isPartnerTicketsCategory (String id, String name) {
        if (id != null) {
            if(id.equals(SEAT_TRAIT_ID_MEMBER)) {
	            return true;
	        }
	    }
        if (name != null) {
            if(name.equalsIgnoreCase(SEAT_TRAIT_NAME_MEMBER)) {
                return true;
            }
        }
        return false;
	}

    private boolean _isGASection(String section)
	{
		return ( section != null ) && ( section.equalsIgnoreCase(CommonConstants.GENERAL_ADMISSION) );
	}

    private boolean _isFaceValueAtListingLevelValid(ListingRequest listingRequest){
    	boolean result = true;
    	if (listingRequest.getFaceValue() == null || listingRequest.getFaceValue().getAmount() == null) {
	        	result = false;
	     }
    	return result;
    }


    private boolean _isFaceValueCheckRequired(List<String> faceValueCountriesList, String eventCountry){
    	boolean result = false;
    	if(eventCountry != null && faceValueCountriesList.contains(eventCountry)) {
	        	result = true;
	     }
    	return result;
    }

  private boolean isReList(String subscriber) {
    return subscriber != null && subscriber.contains(RELIST_MARKER);
  }

}