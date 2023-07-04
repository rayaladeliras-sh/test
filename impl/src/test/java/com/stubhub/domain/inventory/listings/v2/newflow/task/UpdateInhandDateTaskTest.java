package com.stubhub.domain.inventory.listings.v2.newflow.task;

import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.FulfillmentInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.HeaderInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.SellerInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.helper.InhandDateHelper;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;

public class UpdateInhandDateTaskTest {

  @Mock
  private InhandDateHelper inhandDateHelper;
  
  ListingDTO listingDTO = getListingDTO();
  private static final DateFormat df = new SimpleDateFormat("MM/dd/yyyy");

  @InjectMocks
  private UpdateInhandDateTask updateInhandDateTask =  new UpdateInhandDateTask(listingDTO);
  
  @BeforeMethod
  public void setup() {
    initMocks(this);
  }
  
  @Test
  public void testUpdateInhandDate() {
    ListingDTO dto = updateInhandDateTask.call();
    assertNotNull(dto);
    
  }
  
  private ListingDTO getListingDTO() {

    ListingRequest request = new ListingRequest();
    
    request.setInhandDate(df.format(getDate(10).getTime()));
    
    Listing dbListing = new Listing();
    ListingDTO dto = new ListingDTO(request);
    dbListing.setId(1271578716L);
    dbListing.setEventId(1271578799L);
    dbListing.setSection("Block A");
    dbListing.setRow("22");
    dbListing.setQuantity(1);
    dbListing.setCurrency(Currency.getInstance(Locale.US));
    dbListing.setDeliveryOption(2);
    dbListing.setTicketMedium(1);
    dbListing.setFulfillmentDeliveryMethods(
        "10,22,5.25,,2017-09-19T19:00:00Z|10,23,5.25,,2017-09-19T19:00:00Z|10,24,5.25,,2017-09-14T19:00:00Z");
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    dto.setSellerInfo(sellerInfo);
    dto.setDbListing(dbListing);
    HeaderInfo headerInfo = new HeaderInfo();
    headerInfo.setSubscriber("Single|V2|Api_UK_sell_buyer20|DefaultApplication");
    headerInfo.setClientIp("10.10.10.10");
    dto.setHeaderInfo(headerInfo);
    FulfillmentInfo fulfillmentInfo = new FulfillmentInfo();
    fulfillmentInfo.setInHandDate(getDate(10));
    fulfillmentInfo.setDeclaredInhandDate(getDate(9));
    dto.setFulfillmentInfo(fulfillmentInfo);
    return dto;
  }

  private Calendar getDate(int days) {
    
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, days);   
    return cal;
  }


}
