package com.stubhub.domain.inventory.listings.v2.newflow.task;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import org.apache.cxf.jaxrs.client.WebClient;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.stubhub.domain.inventory.biz.v2.impl.InventoryMgrImpl;
import com.stubhub.domain.inventory.common.entity.ListingStatus;
import com.stubhub.domain.inventory.common.util.SolrJsonUtil;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.listings.v2.entity.ListingCheck;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.HeaderInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.SellerInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.util.InventorySolrUtil;
import com.stubhub.domain.inventory.listings.v2.util.SellerHelper;
import com.stubhub.domain.inventory.listings.v2.util.UserHelper;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import com.stubhub.domain.inventory.v2.enums.TicketMedium;
import com.stubhub.newplatform.common.entity.Money;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

public class UpdateStatusTaskTest {

  @Mock
  private UserHelper userHelper;

  @Mock
  private SellerHelper sellerHelper;

  @Mock
  private InventoryMgrImpl inventoryMgr;

  @Mock
  private ListingDTO listingDTO;


  @InjectMocks
  private UpdateStatusTask updateStatusTask = new UpdateStatusTask(listingDTO);

  private SvcLocator svcLocator;
  private WebClient webClient;
  private InventorySolrUtil inventorySolrUtil;
  private SolrJsonUtil solrJsonUtil;

  @BeforeMethod
  public void setup() {
    MockitoAnnotations.initMocks(this);
    svcLocator = Mockito.mock(SvcLocator.class);
    webClient = Mockito.mock(WebClient.class);
    inventorySolrUtil = new InventorySolrUtil() {
      protected String getProperty(String propertyName, String defaultValue) {
        return "";
      }
    };
    solrJsonUtil = new SolrJsonUtil() {
      protected String getProperty(String propertyName, String defaultValue) {
        return "";
      }
    };
    ReflectionTestUtils.setField(solrJsonUtil, "svcLocator", svcLocator);
    ReflectionTestUtils.setField(inventorySolrUtil, "jsonUtil", solrJsonUtil);
  }

  @Test
  public void testDeactivateListingSuccess()
      throws JsonParseException, JsonMappingException, IOException {
    ListingRequest request = new ListingRequest();
    request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = new Listing();
    dbListing = getListingDTO().getDbListing();
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    updateStatusTask.call();
    assertEquals(dbListing.getSystemStatus(), "INACTIVE");
  }

  @Test
  public void testDefaultUpdate() throws JsonParseException, JsonMappingException, IOException {
    ListingRequest request = new ListingRequest();
    request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.DELETED);
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = new Listing();
    dbListing = getListingDTO().getDbListing();
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    updateStatusTask.call();
    assertEquals(dbListing.getSystemStatus(), null);
  }

  @Test
  public void testDeactivatePendingLockListing()
      throws JsonParseException, JsonMappingException, IOException {
    ListingRequest request = new ListingRequest();
    request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = new Listing();
    dbListing = getListingDTO().getDbListing();
    dbListing.setSystemStatus("PENDING LOCK");
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    try {
      updateStatusTask.call();
    } catch (ListingException le) {
      assertEquals(le.getCustomMessage(), "Invalid listing status");
    }
  }

  @Test
  public void testActivateListNullPrice()
      throws JsonParseException, JsonMappingException, IOException {
    ListingRequest request = getRequest();
    when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.ACTIVE);
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = getListingDTO().getDbListing();
    dbListing.setSystemStatus("INCOMPLETE");
    dbListing.setListPrice(null);
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    when(listingDTO.getSellerInfo()).thenReturn(getListingDTO().getSellerInfo());
    try {
      updateStatusTask.call();
    } catch (ListingException le) {
      assertEquals(le.getCustomMessage(), "Invalid listing price");
    }
  }

  @Test
  public void testActivateListInvalidSplit()
      throws JsonParseException, JsonMappingException, IOException {
    when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    ListingRequest request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.ACTIVE);
    when(listingDTO.getListingRequest()).thenReturn(request);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    Listing dbListing = getListingDTO().getDbListing();
    dbListing.setSystemStatus("INCOMPLETE");
    dbListing.setSplitOption(null);
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    when(listingDTO.getSellerInfo()).thenReturn(getListingDTO().getSellerInfo());
    try {
      updateStatusTask.call();
    } catch (ListingException le) {
      assertEquals(le.getCustomMessage(), "Missing split option or quantity");
    }
  }

  @Test
  public void testActivateListInvalidSplitQty()
      throws JsonParseException, JsonMappingException, IOException {
    when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    ListingRequest request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.ACTIVE);
    when(listingDTO.getListingRequest()).thenReturn(request);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    Listing dbListing = getListingDTO().getDbListing();
    dbListing.setSystemStatus("INCOMPLETE");
    dbListing.setSplitQuantity(null);
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    when(listingDTO.getSellerInfo()).thenReturn(getListingDTO().getSellerInfo());
    try {
      updateStatusTask.call();
    } catch (ListingException le) {
      assertEquals(le.getCustomMessage(), "Missing split option or quantity");
    }
  }

  @Test
  public void testDeactivatePendingPdfListing()
      throws JsonParseException, JsonMappingException, IOException {
    ListingRequest request = new ListingRequest();
    request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = new Listing();
    dbListing = getListingDTO().getDbListing();
    dbListing.setSystemStatus("PENDING PDF REVIEW");
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    try {
      updateStatusTask.call();
    } catch (ListingException le) {
      assertEquals(le.getCustomMessage(), "Invalid listing status");
    }
  }

  @Test
  public void testDeactivateIncompleteListing()
      throws JsonParseException, JsonMappingException, IOException {
    ListingRequest request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = getListingDTO().getDbListing();
    dbListing.setSystemStatus("INCOMPLETE");
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    try {
      updateStatusTask.call();
    } catch (ListingException le) {
      assertEquals(le.getCustomMessage(), "Invalid listing status");
    }
  }

  @Test
  public void testActivatePendingLockListing()
      throws JsonParseException, JsonMappingException, IOException {
    ListingRequest request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.ACTIVE);
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = getListingDTO().getDbListing();
    dbListing.setSystemStatus("PENDING LOCK");
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    try {
      updateStatusTask.call();
    } catch (ListingException le) {
      assertEquals(le.getCustomMessage(), "Invalid listing status");
    }
  }

  @Test
  public void testActivatePendingPdfListing()
      throws JsonParseException, JsonMappingException, IOException {
    ListingRequest request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.ACTIVE);
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = getListingDTO().getDbListing();
    dbListing.setSystemStatus("PENDING PDF REVIEW");
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    try {
      updateStatusTask.call();
    } catch (ListingException le) {
      assertEquals(le.getCustomMessage(), "Invalid listing status");
    }
  }

  @Test
  public void testActivateListingNoPaymentType()
      throws JsonParseException, JsonMappingException, IOException {
    ListingRequest request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.ACTIVE);
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = getListingDTO().getDbListing();
    dbListing.setSystemStatus("INCOMPLETE");
    dbListing.setSellerPaymentTypeId(null);
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    try {
      updateStatusTask.call();
    } catch (ListingException le) {
      assertEquals(le.getCustomMessage(), "Missing seller payment type id");
    }
  }

  @Test
  public void testActivateListingNoContactId()
      throws JsonParseException, JsonMappingException, IOException {
    ListingRequest request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.ACTIVE);
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = getListingDTO().getDbListing();
    dbListing.setSellerContactId(null);
    dbListing.setSystemStatus("INCOMPLETE");
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    try {
      updateStatusTask.call();
    } catch (ListingException le) {
      assertEquals(le.getCustomMessage(), "Missing seller contact id");
    }
  }

  @Test
  public void testActivateListingNullCcId()
      throws JsonParseException, JsonMappingException, IOException {
    ListingRequest request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.ACTIVE);
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = getListingDTO().getDbListing();
    dbListing.setSellerCCId(null);
    dbListing.setSystemStatus("INCOMPLETE");
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    try {
      updateStatusTask.call();
    } catch (ListingException le) {
      assertEquals(le.getCustomMessage(), "Invalid seller cc id");
    }
  }

  @Test
  public void testActivateListingDefaultCcId()
      throws JsonParseException, JsonMappingException, IOException {
    ListingRequest request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.ACTIVE);
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = getListingDTO().getDbListing();
    dbListing.setSellerCCId(48411L);
    dbListing.setSystemStatus("INCOMPLETE");
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    try {
      updateStatusTask.call();
    } catch (ListingException le) {
      assertEquals(le.getCustomMessage(), "Invalid seller cc id");
    }
  }

  @Test
  public void testActivateListingNullPayInst()
      throws JsonParseException, JsonMappingException, IOException {
    when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(false);
    ListingRequest request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.ACTIVE);
    when(listingDTO.getListingRequest()).thenReturn(request);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    Listing dbListing = getListingDTO().getDbListing();
    dbListing.setAllsellerPaymentInstruments(null);
    dbListing.setSystemStatus("INCOMPLETE");
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    when(listingDTO.getSellerInfo()).thenReturn(getListingDTO().getSellerInfo());
    try {
      updateStatusTask.call();
    } catch (ListingException le) {
      assertEquals(le.getCustomMessage(), "Invalid seller cc id");
    }
  }

  @Test
  public void testActivateListValidCcId()
      throws JsonParseException, JsonMappingException, IOException {
    when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    ListingCheck check = new ListingCheck();
    check.setIsListed(false);
    check.setMessage(" ");
    InventorySolrUtil inventorySolrUtil = Mockito.mock(InventorySolrUtil.class);
    ReflectionTestUtils.setField(updateStatusTask, "inventorySolrUtil", inventorySolrUtil);
    Mockito
        .when(inventorySolrUtil.isListingExists(Mockito.anyLong(), Mockito.anyLong(),
            Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(check);
    ListingRequest request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.ACTIVE);
    when(listingDTO.getListingRequest()).thenReturn(request);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    Listing dbListing = getListingDTO().getDbListing();
    dbListing.setAllsellerPaymentInstruments(null);
    dbListing.setSystemStatus("INCOMPLETE");
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    when(listingDTO.getSellerInfo()).thenReturn(getListingDTO().getSellerInfo());
    updateStatusTask.call();
    assertEquals(dbListing.getSystemStatus(), "ACTIVE");
  }

  @Test
  public void testActivateListDuplicateListing()
      throws JsonParseException, JsonMappingException, IOException {
    when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    ListingCheck check = new ListingCheck();
    check.setIsListed(true);
    check.setMessage(" ");
    InventorySolrUtil inventorySolrUtil = Mockito.mock(InventorySolrUtil.class);
    ReflectionTestUtils.setField(updateStatusTask, "inventorySolrUtil", inventorySolrUtil);
    Mockito
        .when(inventorySolrUtil.isListingExists(Mockito.anyLong(), Mockito.anyLong(),
            Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(check);
    ListingRequest request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.ACTIVE);
    when(listingDTO.getListingRequest()).thenReturn(request);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    Listing dbListing = getListingDTO().getDbListing();
    dbListing.setAllsellerPaymentInstruments(null);
    dbListing.setSystemStatus("INCOMPLETE");
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    when(listingDTO.getSellerInfo()).thenReturn(getListingDTO().getSellerInfo());
    try {
      updateStatusTask.call();
    } catch (ListingException le) {
      assertEquals(le.getErrorCodeEnum(), ErrorCodeEnum.duplicateSectionRowSeat);
    }
  }

  @Test
  public void testActivateListValidBarcodPredelivery()
      throws JsonParseException, JsonMappingException, IOException {
    when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    ListingCheck check = new ListingCheck();
    check.setIsListed(false);
    check.setMessage(" ");
    InventorySolrUtil inventorySolrUtil = Mockito.mock(InventorySolrUtil.class);
    ReflectionTestUtils.setField(updateStatusTask, "inventorySolrUtil", inventorySolrUtil);
    Mockito
        .when(inventorySolrUtil.isListingExists(Mockito.anyLong(), Mockito.anyLong(),
            Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(check);
    ListingRequest request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.ACTIVE);
    when(listingDTO.getListingRequest()).thenReturn(request);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    Listing dbListing = getListingDTO().getDbListing();
    dbListing.setAllsellerPaymentInstruments(null);
    dbListing.setSystemStatus("INCOMPLETE");
    dbListing.setTicketMedium(3);
    dbListing.setDeliveryOption(1);
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    when(listingDTO.getSellerInfo()).thenReturn(getListingDTO().getSellerInfo());
    updateStatusTask.call();
    assertEquals(dbListing.getSystemStatus(), "PENDING LOCK");
  }

  @Test
  public void testActivateListValidLms()
      throws JsonParseException, JsonMappingException, IOException {
    when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    ListingCheck check = new ListingCheck();
    check.setIsListed(false);
    check.setMessage(" ");
    InventorySolrUtil inventorySolrUtil = Mockito.mock(InventorySolrUtil.class);
    ReflectionTestUtils.setField(updateStatusTask, "inventorySolrUtil", inventorySolrUtil);
    Mockito
        .when(inventorySolrUtil.isListingExists(Mockito.anyLong(), Mockito.anyLong(),
            Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong()))
        .thenReturn(check);
    ListingRequest request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.ACTIVE);
    when(listingDTO.getListingRequest()).thenReturn(request);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    Listing dbListing = getListingDTO().getDbListing();
    dbListing.setAllsellerPaymentInstruments(null);
    dbListing.setSystemStatus("INCOMPLETE");
    dbListing.setLmsApprovalStatus(1);
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    when(listingDTO.getSellerInfo()).thenReturn(getListingDTO().getSellerInfo());
    updateStatusTask.call();
    assertEquals(dbListing.getSystemStatus(), "ACTIVE");
    
    dbListing.setFulfillmentDeliveryMethods("5,1,5.25,,2017-09-19T19:00:00Z");
    dbListing.setSystemStatus("INCOMPLETE");
    updateStatusTask.call();
    assertEquals(dbListing.getSystemStatus(), "INCOMPLETE");
  }

  @Test
  public void testActivateListInvalidFraud()
      throws JsonParseException, JsonMappingException, IOException {
    when(userHelper.isSellerCCIdValid(Mockito.anyString(), Mockito.anyLong())).thenReturn(true);
    ListingRequest request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.ACTIVE);
    when(listingDTO.getListingRequest()).thenReturn(request);
    SellerInfo sellerInfo = new SellerInfo();
    sellerInfo.setSellerId(1000000008L);
    sellerInfo.setSellerGuid("C77991557A035E14E04400212861B256");
    Listing dbListing = getListingDTO().getDbListing();
    dbListing.setSystemStatus("INCOMPLETE");
    dbListing.setFraudCheckStatusId(200L);
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    when(listingDTO.getSellerInfo()).thenReturn(getListingDTO().getSellerInfo());
    try {
      updateStatusTask.call();
    } catch (ListingException le) {
      assertEquals(le.getCustomMessage(), "Listing cannot be activated");
    }
  }

  @Test
  public void testMakePendingListingPdfSuccess()
      throws JsonParseException, JsonMappingException, IOException {
    ListingRequest request = new ListingRequest();
    Mockito.when(inventoryMgr.isPDFPendingReviewAllowed(Mockito.anyLong())).thenReturn(true);
    try {
      Mockito.when(sellerHelper.populateSellerDetails(Mockito.any(Listing.class))).thenReturn(true);
    } catch (com.stubhub.domain.inventory.common.util.ListingException e) {
      e.printStackTrace();
    }
    request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.PENDING);
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = new Listing();
    dbListing = getListingDTO().getDbListing();
    dbListing.setSystemStatus("PENDING PDF REVIEW");
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    updateStatusTask.call();
    assertEquals(dbListing.getSystemStatus(), "PENDING PDF REVIEW");
  }

  @Test
  public void testMakePendingListingPdfError()
      throws JsonParseException, JsonMappingException, IOException {
    ListingRequest request = new ListingRequest();
    Mockito.when(inventoryMgr.isPDFPendingReviewAllowed(Mockito.anyLong())).thenReturn(false);
    try {
      Mockito.when(sellerHelper.populateSellerDetails(Mockito.any(Listing.class)))
          .thenReturn(false);
    } catch (com.stubhub.domain.inventory.common.util.ListingException e) {
      e.printStackTrace();
    }
    request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.PENDING);
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = new Listing();
    dbListing = getListingDTO().getDbListing();
    dbListing.setSystemStatus("PENDING PDF REVIEW");
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    try {
      updateStatusTask.call();
    } catch (ListingException le) {
      assertEquals(le.getCustomMessage(), "Invalid listing status");
    }
  }

  @Test
  public void testMakePendingListingNotPdfError()
      throws JsonParseException, JsonMappingException, IOException {
    ListingRequest request = new ListingRequest();
    Mockito.when(inventoryMgr.isPDFPendingReviewAllowed(Mockito.anyLong())).thenReturn(true);
    try {
      Mockito.when(sellerHelper.populateSellerDetails(Mockito.any(Listing.class)))
          .thenReturn(false);
    } catch (com.stubhub.domain.inventory.common.util.ListingException e) {
      e.printStackTrace();
    }
    request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.PENDING);
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = new Listing();
    dbListing = getListingDTO().getDbListing();
    dbListing.setSystemStatus("PENDING PDF REVIEW");
    dbListing.setTicketMedium(1);
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    try {
      updateStatusTask.call();
    } catch (ListingException le) {
      assertEquals(le.getCustomMessage(), "Invalid listing status");
    }
  }

  @Test
  public void testMakeIncompleteListingPdfSuccess()
      throws JsonParseException, JsonMappingException, IOException {
    ListingRequest request = new ListingRequest();
    Mockito.when(inventoryMgr.isPDFPendingReviewAllowed(Mockito.anyLong())).thenReturn(true);
    try {
      Mockito.when(sellerHelper.populateSellerDetails(Mockito.any(Listing.class))).thenReturn(true);
    } catch (com.stubhub.domain.inventory.common.util.ListingException e) {
      e.printStackTrace();
    }
    request = getRequest();
    request.setPayoutPerProduct(new Money("49.99", "USD"));
    request.setPricePerProduct(null);
    request.setStatus(ListingStatus.INCOMPLETE);
    when(listingDTO.getListingRequest()).thenReturn(request);
    Listing dbListing = new Listing();
    dbListing = getListingDTO().getDbListing();
    dbListing.setSystemStatus("ACTIVE");
    when(listingDTO.getDbListing()).thenReturn(dbListing);
    HeaderInfo headerInfo = getListingDTO().getHeaderInfo();
    when(listingDTO.getHeaderInfo()).thenReturn(headerInfo);
    updateStatusTask.call();
    assertEquals(dbListing.getSystemStatus(), "INCOMPLETE");
  }

  private ListingDTO getListingDTO() {

    ListingRequest request = new ListingRequest();
    request.setPricePerProduct(new Money("49.99", "USD"));
    request.setTicketMedium(TicketMedium.BARCODE);

    Listing dbListing = new Listing();
    ListingDTO dto = new ListingDTO(request);
    dbListing.setId(1271578716L);
    dbListing.setEventId(1271578799L);
    dbListing.setSection("Block A");
    dbListing.setRow("22");
    dbListing.setQuantity(1);
    dbListing.setCurrency(Currency.getInstance(Locale.US));
    dbListing.setDeliveryOption(2);
    dbListing.setSplitOption((short) 2);
    dbListing.setSplitQuantity(2);
    dbListing.setSellerPaymentTypeId(1L);
    dbListing.setSellerCCId(1L);
    dbListing.setSellerContactId(1L);
    dbListing.setEventId(1L);
    dbListing.setSection("Test");
    dbListing.setRow("Test");
    dbListing.setSeats("Test");
    dbListing.setSellerId(1L);
    dbListing.setId(1L);
    dbListing.setTicketMedium(2);
    Money money = new Money();
    money.setAmount(new BigDecimal("23.00"));
    money.setCurrency("USD");
    dbListing.setListPrice(money);
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
    return dto;
  }


  private ListingRequest getRequest() {
    ListingRequest request = new ListingRequest();
    request.setPricePerProduct(new Money("49.99", "USD"));
    request.setTicketMedium(TicketMedium.BARCODE);
    request.setStatus(ListingStatus.INACTIVE);
    return request;
  }

}
