package com.stubhub.domain.inventory.listings.v2.newflow.task;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;

@Component
@Scope("prototype")
public class UpdateFaceValueTask extends RegularTask {

  private static final Logger log = LoggerFactory.getLogger(UpdateFaceValueTask.class);
  
  private static final String FACE_VALUE_COUNTRIES = "GB,LU,AT,DE";
  
  private static final String USD_TO_CAD_CURRENCY_CONVERTION_VALUE = "usd.to.cad.conversion.value";
  
  private static final String USD_TO_CAD_CURRENCY_CONVERTION_DEFAULT = "1.3514";
  
  @Autowired
  private MasterStubhubPropertiesWrapper masterStubhubProperties;

  public UpdateFaceValueTask(ListingDTO dto) {
    super(dto);
  }

  private Listing dbListing;
  private ListingRequest request;

  @Override
  protected void preExecute() {
    dbListing = listingDTO.getDbListing();
    request = listingDTO.getListingRequest();
    String faceValueCountries = masterStubhubProperties.getProperty("facevalue.required.countries", FACE_VALUE_COUNTRIES);
    List<String> faceValueCountriesList = Arrays.asList(faceValueCountries.split(","));
    if (dbListing.getEvent() != null && dbListing.getEvent().getCountry() != null) {
			if (faceValueCountriesList.contains(dbListing.getEvent().getCountry())) {
				if (request.getFaceValue().getAmount().doubleValue() <= 0) {
					log.error("message=\"Face value is required\" listingId={} eventId={}", dbListing.getId(),
							dbListing.getEventId());
					throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidFaceValue);
				}
			}
    }

    if (dbListing.getCurrency() != null && request.getFaceValue().getCurrency() != null 
    		&& !(dbListing.getEvent() != null && dbListing.getEvent().getCountry() != null &&  
    		"CA".equalsIgnoreCase(dbListing.getEvent().getCountry()) && "USD".equalsIgnoreCase(request.getFaceValue().getCurrency())) 
    		&& !(request.getFaceValue().getCurrency().equals(dbListing.getCurrency().getCurrencyCode()))) {
      log.error(
          "message=\"Invalid currency in the request\" listingId={} inputCurrency={} eventCurrency={}",
          dbListing.getId(), request.getFaceValue().getCurrency(), dbListing.getCurrency());
      throw new ListingException(ErrorType.INPUTERROR, ErrorCodeEnum.invalidCurrency);
    }
  }

	@Override
	protected void execute() {
		BigDecimal fxUsdToCadExchnage;
		if (dbListing.getEvent() != null && dbListing.getEvent().getCountry() != null 
				&& "CA".equalsIgnoreCase(dbListing.getEvent().getCountry())
				&& "USD".equalsIgnoreCase(request.getFaceValue().getCurrency())) {
			fxUsdToCadExchnage = new BigDecimal(getUSDToCADConversionValue());
			dbListing.getFaceValue().setAmount(request.getFaceValue().getAmount().multiply(fxUsdToCadExchnage));
			dbListing.getFaceValue().setCurrency(Currency.getInstance(Locale.CANADA).toString());
		} else {
			dbListing.setFaceValue(request.getFaceValue());
		}
		List<TicketSeat> dbTicketSeats = dbListing.getTicketSeats();
		if (dbTicketSeats != null) {
			for (TicketSeat seat : dbTicketSeats) {
				if (dbListing.getEvent() != null && dbListing.getEvent().getCountry() != null 
						&& "CA".equalsIgnoreCase(dbListing.getEvent().getCountry())
						&& "USD".equalsIgnoreCase(request.getFaceValue().getCurrency())) {
					fxUsdToCadExchnage = new BigDecimal(getUSDToCADConversionValue());
					seat.getFaceValue().setAmount(request.getFaceValue().getAmount().multiply(fxUsdToCadExchnage));
					seat.getFaceValue().setCurrency(Currency.getInstance(Locale.CANADA).toString());
				} else {
					seat.setFaceValue(request.getFaceValue());
					seat.setCurrency(Currency.getInstance(request.getFaceValue().getCurrency()));
				}
			}
		}
	}

  private String getUSDToCADConversionValue() {
	  return masterStubhubProperties.getProperty(USD_TO_CAD_CURRENCY_CONVERTION_VALUE,USD_TO_CAD_CURRENCY_CONVERTION_DEFAULT);
  }
  
  @Override
  protected void postExecute() {
    if (dbListing.getFaceValue() != null
        && dbListing.getFaceValue().getAmount().doubleValue() == 0) {
      log.debug("message=\"Clearing the face value\" listingId={}", dbListing.getId());
      dbListing.setFaceValue(null);
      
      List<TicketSeat> dbTicketSeats = dbListing.getTicketSeats();
      if(dbTicketSeats != null) {
        for(TicketSeat seat : dbTicketSeats) {
          seat.setFaceValue(null);
          seat.setCurrency(null);
        }
      }
    }
  }
  
	/*private boolean isFacevalueRequiredForStates(String eventCountry, String eventState, String eventId) {
		String canadaSwitch = masterStubhubProperties.getProperty("canada.site.switch", "false");
		boolean isCanadaSwitch = Boolean.parseBoolean(canadaSwitch);
		log.info("property value of canadaSwith={}",isCanadaSwitch);
		//start code added for PROD testing of canada events to be removed once Canada is live
		String caEvenIids = masterStubhubProperties.getProperty("canada.event.switch.v2", " ");
		log.info("property value of canada.event.switch.v2={}",caEvenIids);
		if(StringUtils.isNotBlank(caEvenIids)) {
			List<String> caEvents = new ArrayList<String>(Arrays.asList(caEvenIids.split("\\s*,\\s*")));
			if(caEvents.contains(eventId)) {
				isCanadaSwitch = true;
			}			
		}
		log.info("value of canadaSwitch after validating envents canadaSwith={}",isCanadaSwitch);
		//end code added for PROD testing of canada events
		
		String faceValueStates = masterStubhubProperties.getProperty("listing.facevalue.required.states." + eventCountry, null);
		log.info("message=\"faceValue required  for \" isCanadaSwitch={} eventCountry={} eventStates={} ",isCanadaSwitch, eventCountry, faceValueStates);
		List<String> states = null;
		if( null != faceValueStates) {
			states = Arrays.asList(faceValueStates.split(","));
		}
		return (states != null && states.contains(eventState) && isCanadaSwitch) ;
		
	}*/

}
