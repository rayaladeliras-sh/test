package com.stubhub.domain.inventory.listings.v2.newflow.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.newflow.adapter.ListingResponseAdapter;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.HeaderInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingType;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.SellerInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.OperationTypeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.SizeTypeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.UpdateListingEnum;
import com.stubhub.domain.inventory.listings.v2.util.JMSMessageHelper;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.DTO.ListingResponse;
import com.stubhub.domain.inventory.v2.enums.TicketMedium;
import com.stubhub.newplatform.common.entity.Money;

public class UpdateListingHandlerTest {

  @Mock
  UpdateListingTaskFactory updateListingTaskFactory;

  @Mock
  private InventoryMgr inventoryMgr;

  @Mock
  private ListingResponseAdapter listingResponseAdapter;
  
  @Mock
  private JMSMessageHelper jmsMessageHelper;

  private ListingDTO listingDTO = getListingDTO();

  private List<UpdateListingEnum> updateAttributeList = getUpdateAttrList();


  @InjectMocks
  UpdateListingHandler updateListingHandler =
      new UpdateListingHandler(listingDTO, updateAttributeList);

  @BeforeMethod
  public void setUp() {
    listingDTO = getListingDTO();
    updateAttributeList = getUpdateAttrList();
    initMocks(this);
  }

  @Test
  public void testExecute() {
    ListingType listingType = new ListingType();
    listingType.setOperationType(OperationTypeEnum.UPDATE);
    listingDTO.setListingType(listingType);
    Listing dbListing = new Listing();
    dbListing.setSystemStatus("ACTIVE");
    when(inventoryMgr.updateListing(Mockito.any(Listing.class))).thenReturn(dbListing);

    when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(dbListing);
    when(listingResponseAdapter.convertToListingResponse(Mockito.any(Listing.class)))
        .thenReturn(getListingResponse());
    ListingResponse response = updateListingHandler.execute();
    assertNotNull(response);
    assertEquals(response.getId(), "1271578716");

  }

  @Test
  public void testExecuteBarcodePredeliveryUpdateQty() {
    Listing dbListing = getListingDTO().getDbListing();
    dbListing.setSystemStatus("PENDING LOCK");
    when(inventoryMgr.getListing(Mockito.anyLong())).thenReturn(dbListing);
    when(inventoryMgr.updateListing(Mockito.any(Listing.class))).thenReturn(dbListing);
    when(listingResponseAdapter.convertToListingResponse(Mockito.any(Listing.class)))
        .thenReturn(getListingResponse());
    ListingResponse response = updateListingHandler.execute();
    assertNotNull(response);
    assertEquals(response.getId(), "1271578716");
  }


  @Test
  public void testExecuteDeleteListingSuccess() {
    List<UpdateListingEnum> updateAttrs = new ArrayList<>();
    updateAttrs.add(UpdateListingEnum.DELETE_LISTING);
    ReflectionTestUtils.setField(updateListingHandler, "updateAttributeList", updateAttrs);
    ListingType listingType = new ListingType();
    listingType.setOperationType(OperationTypeEnum.UPDATE);
    listingDTO.setListingType(listingType);

    Listing dbListing = new Listing();
    dbListing.setSystemStatus("DELETED");
    dbListing.setTicketMedium(4);
    dbListing.setDeliveryOption(1);
    when(inventoryMgr.updateListing(Mockito.any(Listing.class))).thenReturn(dbListing);
    doNothing().when(jmsMessageHelper).sendUnlockInventoryMessage(null);

    ListingResponse listingResponse = new ListingResponse();
    listingResponse.setId("1271578716");
    listingResponse.setStatus(ListingStatus.DELETED);
    when(listingResponseAdapter.convertToListingResponse(any(Listing.class)))
        .thenReturn(listingResponse);
    ListingResponse response = updateListingHandler.execute();
    assertNotNull(response);
    assertEquals(response.getId(), "1271578716");
    assertEquals(response.getStatus(), ListingStatus.DELETED);
  }

  @Test
  public void testExecuteUpdatePriceListingSuccess() {
    List<UpdateListingEnum> updateAttrs = new ArrayList<>();
    updateAttrs.add(UpdateListingEnum.PRICE);
    ReflectionTestUtils.setField(updateListingHandler, "updateAttributeList", updateAttrs);
    ListingType listingType = new ListingType();
    listingType.setOperationType(OperationTypeEnum.UPDATE);
    listingDTO.setListingType(listingType);

    Listing dbListing = new Listing();
    dbListing.setListPrice(new Money("79.99", "USD"));
    dbListing.setSystemStatus("PENDING LOCK");
    when(inventoryMgr.updateListing(Mockito.any(Listing.class))).thenReturn(dbListing);
    doNothing().when(jmsMessageHelper).sendUnlockInventoryMessage(null);

    ListingResponse listingResponse = new ListingResponse();
    listingResponse.setId("1271578716");
    when(listingResponseAdapter.convertToListingResponse(any(Listing.class)))
        .thenReturn(listingResponse);
    ListingResponse response = updateListingHandler.execute();
    assertNotNull(response);
    assertEquals(response.getId(), "1271578716");
  }
  private ListingDTO getListingDTO() {

    ListingRequest request = new ListingRequest();
    request.setPricePerProduct(new Money("49.99", "USD"));
    request.setTicketMedium(TicketMedium.BARCODE);
    request.setQuantity(2);

    Listing dbListing = new Listing();
    ListingDTO dto = new ListingDTO(request);

    ListingType listingType = new ListingType();
    listingType.setOperationType(OperationTypeEnum.UPDATE);
    listingType.setSizeType(SizeTypeEnum.SINGLE);
    dto.setListingType(listingType);

    dbListing.setId(1271578716L);
    dbListing.setEventId(1271578799L);
    dbListing.setSection("Block A");
    dbListing.setRow("22");
    dbListing.setQuantity(1);
    dbListing.setCurrency(Currency.getInstance(Locale.US));
    dbListing.setDeliveryOption(2);
    dbListing.setFulfillmentDeliveryMethods(
        "10,22,5.25,,2017-09-19T19:00:00Z|10,23,5.25,,2017-09-19T19:00:00Z|10,24,5.25,,2017-09-14T19:00:00Z");
    dbListing.setSystemStatus("ACTIVE");
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    dto.setSellerInfo(sellerInfo);
    dto.setDbListing(dbListing);
    HeaderInfo headerInfo = new HeaderInfo();
    headerInfo.setSubscriber("Single|V2|Api_UK_sell_buyer20|DefaultApplication");
    headerInfo.setClientIp("10.10.10.10");
    dto.setHeaderInfo(headerInfo);
    return dto;
  }

  private List<UpdateListingEnum> getUpdateAttrList() {
    List<UpdateListingEnum> updateAttrs = new ArrayList<>();
    updateAttrs.add(UpdateListingEnum.PRICE);
    updateAttrs.add(UpdateListingEnum.QUANTITY);
    return updateAttrs;

  }

  private ListingResponse getListingResponse() {
    ListingResponse listingResponse = new ListingResponse();
    listingResponse.setId("1271578716");
    listingResponse.setExternalListingId("1271578716L");
    listingResponse.setStatus(ListingStatus.ACTIVE);

    return listingResponse;
  }

}
