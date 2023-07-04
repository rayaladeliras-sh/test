package com.stubhub.domain.inventory.listings.v2.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.ResponseReader;
import org.apache.cxf.jaxrs.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.stubhub.domain.infrastructure.common.core.concurrent.SHThreadLocals;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContextSerializer;
import com.stubhub.domain.inventory.biz.v2.intf.IntegrationManager;
import com.stubhub.domain.inventory.common.util.JsonUtil;
import com.stubhub.domain.inventory.datamodel.entity.enums.PaymentType;
import com.stubhub.domain.inventory.listings.v2.entity.Address;
import com.stubhub.domain.inventory.listings.v2.entity.UserContact;
import com.stubhub.domain.inventory.listings.v2.enums.SellerType;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.BusinessInfo;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.SellerInfo;
import com.stubhub.domain.inventory.v2.DTO.ProSellerAddress;
import com.stubhub.domain.inventory.v2.DTO.ProSellerInfo;
import com.stubhub.domain.payment.intf.payableinstrument.v1.PayableInstrumentType;
import com.stubhub.domain.payment.intf.payableinstrument.v1.PayableInstrumentTypeResponse;
import com.stubhub.domain.user.contacts.intf.CustomerContactDetails;
import com.stubhub.domain.user.contacts.intf.CustomerContactMappingResponse;
import com.stubhub.domain.user.contactsV2.intf.CustomerContactV2Details;
import com.stubhub.domain.user.contactsV2.intf.CustomerContactsV2Response;
import com.stubhub.domain.user.intf.GetCustomerResponse;
import com.stubhub.domain.user.intf.SellerTypeResponse;
import com.stubhub.domain.user.payments.intf.CreditCardDetails;
import com.stubhub.domain.user.payments.intf.CreditCardDetails.ExpDate;
import com.stubhub.domain.user.payments.intf.CustomerPaymentInstrumentDetails;
import com.stubhub.domain.user.payments.intf.CustomerPaymentInstrumentMappingsResponse;
import com.stubhub.domain.user.payments.v2.intf.CustomerPaymentInstrumentDetailsV2;
import com.stubhub.domain.user.payments.v2.intf.CustomerPaymentInstrumentMappingsResponseV2;
import com.stubhub.domain.user.payments.v2.intf.CustomerPaymentInstrumentsResponseV2;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIThreadLocal;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

/**
 * Helper class to provide user related functionality like returning a given user's defaultContact address etc.
 *
 * @author subpadmanabhan
 */
@Component("userHelper")
public class UserHelper {

  private static final String CUSTOMER_CONTACT_API_URL = "stubhub.user.customer.contactv2.api.url";
  private static final String NEWAPI_ACCESS_TOKEN_KEY = "newapi.accessToken";
  private static final String ACCESS_TOCKEN_DEFAULT_VALUE = "JYf0azPrf1RAvhUhpGZudVU9bBEa";
  private static final String AUTHORIZATION = "Authorization";
  private static final String BEARER = "Bearer ";
  private static final String XSH_SERVICE_CONTEXT = "X-SH-Service-Context";
  private static final String XSH_SERVICE_CONTEXT_HEADER = "{proxiedId={customerGuid}}";
  private static final String DEFAULT_INDICATOR = "Y";
  private static final String CUSTOMER_CONTACT_V2_GET_API_URL = "usercontact.v2.get.api.url";
  private static final String CUSTOMER_CONTACT_V2_GET_API_URL_DEFAULT = "https://api.stubcloudprod.com/user/customers/v1/{userGuid}/contactsV2/{contactGuid}";
  private static final String CONTACT_MAPPING_API_URL = "usercontact.v1.api.getcontactId.url";
  private static final String CONTACT_MAPPING_API_URL_DEFAULT = "http://api-int.stubprod.com/user/customers/v1/{userGuid}/contactMapping/{contactGuid}";
  private static final String CONTACT_MAPPING_INVERSE_API_URL = "usercontact.v1.api.getcontactGuid.url";
  private static final String CONTACT_MAPPING_INVERSE_API_URL_DEFAULT = "http://api-int.stubprod.com/user/customers/v1/{userGuid}/contactMapping?internalId={internalId}";

  private static final String DOMAIN = "inventory";


  @Autowired
  private SvcLocator svcLocator;

  @Autowired
  MasterStubhubPropertiesWrapper masterStubhubProperties;

  @Autowired
  IntegrationManager integrationManager;

  @Autowired
  BusinessHelper businessHelper;

  @Value("${newapi.accessToken:gWwh4zP4l90Cj4wQCslKHpB67_8a}")
  private String appToken;


  private static final Logger log = LoggerFactory.getLogger(UserHelper.class);

  /**
   * Utility method to return the user's default contact address given the user id.
   *
   * @param userId
   */
  public UserContact getDefaultUserContact(Long userId) {
    String customerContactsApiUrl = getProperty("userdefaultcontact.api.url", "https://api.stubcloudprod.com/user/customers/v1/");
    customerContactsApiUrl = customerContactsApiUrl + userId + "?action=defaultContact";

    ResponseReader reader = new ResponseReader();
    reader.setEntityClass(GetCustomerResponse.class);
    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
    responseReader.add(reader);

    log.info("making getDefaultUserContact service call = " + customerContactsApiUrl);

    WebClient webClient = svcLocator.locate(customerContactsApiUrl, responseReader);
    webClient.accept(MediaType.APPLICATION_JSON);
    setSHServiceContext(webClient);

    SHMonitor mon = SHMonitorFactory.getMonitor();
    Response response = null;
    try {
      mon.start();
      response = webClient.get();
    } finally {
      mon.stop();
      log.info(
          SHMonitoringContext.get() + " _operation=getDefaultUserContact" + " _message= service call for userId=" + userId + "  _respTime="
              + mon.getTime());
    }

    if (Response.Status.OK.getStatusCode() == response.getStatus()) {
      GetCustomerResponse getCustomerResponse = (GetCustomerResponse) response.getEntity();
      if (getCustomerResponse != null) {
        CustomerContactDetails contactDetails = getCustomerResponse.getCustomerContact();
        if (contactDetails != null) {
          UserContact userContact = new UserContact();
          userContact.setId(contactDetails.getId());
          userContact.setIsDefault(contactDetails.getDefaultInd().equalsIgnoreCase("Y"));
          if (contactDetails.getName() != null) {
            userContact.setFirstName(contactDetails.getName().firstName);
            userContact.setLastName(contactDetails.getName().lastName);
          }
          if (contactDetails.getAddress() != null) {
            Address userAddress = new Address();
            userAddress.setStreetAddress1(contactDetails.getAddress().getLine1());
            userAddress.setStreetAddress2(contactDetails.getAddress().getLine2());
            userAddress.setCity(contactDetails.getAddress().getCity());
            userAddress.setState(contactDetails.getAddress().getState());
            userAddress.setCountry(contactDetails.getAddress().getCountry());
            userAddress.setZip(contactDetails.getAddress().getZipOrPostalCode());
            userContact.setAddress(userAddress);
          }
          userContact.setPhoneNumber(contactDetails.getPhoneNumber());
          userContact.setPhoneExt(contactDetails.getPhoneExt());
          userContact.setEmail(contactDetails.getEmail());
          return userContact;
        }
      }
    } else if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
      log.error("Invalid UserID=" + userId);
    } else {
      log.error("System error occured while calling getUserContact api  userId=" + userId + " responseCode=" + response.getStatus());
    }
    return null;
  }

  /**
   * Returns property value for the given propertyName. This protected method has been created to get around the static nature of the
   * MasterStubHubProperties' methods for Unit tests. The test classes are expected to override this method with custom implementation.
   *
   * @param propertyName
   * @param defaultValue
   * @return
   */
  protected String getProperty(String propertyName, String defaultValue) {
    return MasterStubHubProperties.getProperty(propertyName, defaultValue);
  }

  public CustomerContactDetails getUserContact(String userGuid, Long contactId) {

    ResponseReader reader = new ResponseReader();
    reader.setEntityClass(CustomerContactDetails.class);
    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
    responseReader.add(reader);

    WebClient webClient = null;
    if (isAuthZRequest()) {
      String customerContactsApiUrl = getProperty("usercontact.api.url2",
          "https://api.stubcloudprod.com/user/customers/v1/{userId}/contacts/{contactId}");
      customerContactsApiUrl = customerContactsApiUrl.replace("{userId}", userGuid);
      customerContactsApiUrl = customerContactsApiUrl.replace("{contactId}", Long.toString(contactId));

      SHServiceContext shServiceContext = (SHServiceContext) SHThreadLocals.get(SHServiceContext.SERVICE_CONTEXT_HEADER);

      SHServiceContextSerializer serializer = new SHServiceContextSerializer();
      String serializedServiceContext = serializer.serialize(shServiceContext);

      webClient = svcLocator.locate(customerContactsApiUrl, responseReader);

      webClient.header("Authorization", "Bearer " + masterStubhubProperties.getProperty("newapi.accessToken", appToken));
      webClient.header(SHServiceContext.SERVICE_CONTEXT_HEADER, serializedServiceContext);

    } else {

      String customerContactsApiUrl = getProperty("usercontact.api.url",
          "https://api.stubcloudprod.com/user/customers/v1/{userId}/contacts/{contactId}");
      customerContactsApiUrl = customerContactsApiUrl.replace("{userId}", userGuid);
      customerContactsApiUrl = customerContactsApiUrl.replace("{contactId}", Long.toString(contactId));

      webClient = svcLocator.locate(customerContactsApiUrl, responseReader);
    }

    webClient.accept(MediaType.APPLICATION_JSON);

    SHMonitor mon = SHMonitorFactory.getMonitor();
    Response response = null;
    try {
      mon.start();
      response = webClient.get();
    } finally {
      mon.stop();
      log.info(SHMonitoringContext.get() + " _operation=getUserContact _message= service call for userGuid=" + userGuid + " contactId="
          + contactId + "  _respTime=" + mon.getTime());
    }

    if (Response.Status.OK.getStatusCode() == response.getStatus()) {
      log.info("getUserContact api call successful for userGuid=" + userGuid + " contactId=" + contactId);

      return (CustomerContactDetails) response.getEntity();
    } else if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
      log.error("Invalid UserGUID=" + userGuid);
    } else {
      log.error("System error occured while calling getUserContact api  userGuid=" + userGuid + " contactId=" + contactId + " responseCode="
          + response.getStatus());
    }
    return null;
  }

  public CustomerPaymentInstrumentDetails getUserCC(String userId, Long ccId) {
    String customerCCApiUrl = getProperty("usercc.api.url",
        "https://payments.api.stubcloudprod.com/user/customers/v1/{userId}/paymentInstruments/{paymentinstrumentid}");
    customerCCApiUrl = customerCCApiUrl.replace("{userId}", userId);
    customerCCApiUrl = customerCCApiUrl.replace("{paymentinstrumentid}", Long.toString(ccId));
    ResponseReader reader = new ResponseReader();

    reader.setEntityClass(CustomerPaymentInstrumentDetails.class);
    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
    responseReader.add(reader);
    log.info("customerCCApiUrl **** = " + customerCCApiUrl);
    WebClient webClient = svcLocator.locate(customerCCApiUrl, responseReader);
    webClient.accept(MediaType.APPLICATION_JSON);

    setSHServiceContext(webClient);

    SHMonitor mon = SHMonitorFactory.getMonitor();
    Response response = null;
    try {
      mon.start();
      response = webClient.get();
    } finally {
      mon.stop();
      log.info(
          SHMonitoringContext.get() + " _operation=getUserCC" + " _message= Success for userId=" + userId + " ccId=" + ccId + "  _respTime="
              + mon.getTime());
    }

    if (Response.Status.OK.getStatusCode() == response.getStatus()) {
      log.info("getUserCC api call successful for userId=" + userId);
      return (CustomerPaymentInstrumentDetails) response.getEntity();
    } else if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
      log.error("Invalid User Id=" + userId);
    } else {
      log.error(
          "System error occured while calling getUserCC api  userId=" + userId + " ccId=" + ccId + " responseCode=" + response.getStatus());
    }
    return null;
  }

  private void setSHServiceContext(WebClient webClient) {
    SHServiceContext shServiceContext = (SHServiceContext) SHThreadLocals.get(SHServiceContext.SERVICE_CONTEXT_HEADER);

    if (shServiceContext != null && shServiceContext.getAttributeMap() != null) {
      String contextAsString = shServiceContext.getAttributeMap().toString();
      if (contextAsString != null) {
        webClient.header(XSH_SERVICE_CONTEXT, contextAsString);
      }
    }
  }

  public CustomerPaymentInstrumentMappingsResponse getPid(String userId, Long ccId, String mode) {
    String userPidMappingApiUrl = getProperty("userpid.api.url",
        "https://payments.api.stubcloudprod.com/user/customers/v1/{customerid}/paymentInstrumentMappings");
    userPidMappingApiUrl = userPidMappingApiUrl.replace("{customerid}", userId);
    ResponseReader reader = new ResponseReader();

    reader.setEntityClass(CustomerPaymentInstrumentMappingsResponse.class);
    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
    responseReader.add(reader);
    log.debug("customerCCApiUrl= " + userPidMappingApiUrl);
    WebClient webClient = svcLocator.locate(userPidMappingApiUrl, responseReader);
    webClient.query("internalId", ccId);
    webClient.query("mode", mode);
    webClient.accept(MediaType.APPLICATION_JSON);
    setSHServiceContext(webClient);

    SHMonitor mon = SHMonitorFactory.getMonitor();
    Response response = null;
    try {
      mon.start();
      response = webClient.get();
    } finally {
      mon.stop();
      log.info(SHMonitoringContext.get() + " _operation=getPid" + " _message= service call for userId=" + userId + " ccId=" + ccId
          + "  _respTime=" + mon.getTime());
    }

    if (Response.Status.OK.getStatusCode() == response.getStatus()) {
      log.info("getPid api call successful for userId=" + userId);
      return (CustomerPaymentInstrumentMappingsResponse) response.getEntity();
    } else if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
      log.error("Invalid CCId=" + ccId);
    } else {
      log.error(
          "System error occured while calling getUserCC api  userId=" + userId + " ccId=" + ccId + " responseCode=" + response.getStatus());
    }
    return null;
  }

  public CustomerPaymentInstrumentMappingsResponseV2 getCCid(String userId, String ccGuid) {
    String userCCidMappingApiUrl = getProperty("userccid.api.url.v2",
        "https://payments.api.stubcloudprod.com/user/customers/v1/{customerid}/paymentInstrumentMappingsV2/{paymentinstrumentmappingsid}");
    userCCidMappingApiUrl = userCCidMappingApiUrl.replace("{customerid}", userId);
    userCCidMappingApiUrl = userCCidMappingApiUrl.replace("{paymentinstrumentmappingsid}", ccGuid);
    ResponseReader reader = new ResponseReader();
    reader.setEntityClass(CustomerPaymentInstrumentMappingsResponseV2.class);
    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
    responseReader.add(reader);
    log.info("userCCidMappingApiUrl= " + userCCidMappingApiUrl);
    WebClient webClient = svcLocator.locate(userCCidMappingApiUrl, responseReader);
    webClient.accept(MediaType.APPLICATION_JSON);
    setSHServiceContext(webClient);

    SHMonitor mon = SHMonitorFactory.getMonitor();
    Response response = null;
    try {
      mon.start();
      response = webClient.get();
    } finally {
      mon.stop();
      log.info(SHMonitoringContext.get() + " _operation=getCCid" + " _message= service call for userId=" + userId + " ccGuid" + ccGuid
          + "  _respTime=" + mon.getTime());
    }
    if (Response.Status.OK.getStatusCode() == response.getStatus()) {
      log.info("getCCid api call successful for ccGuid=" + ccGuid + " userId=" + userId);
      return (CustomerPaymentInstrumentMappingsResponseV2) response.getEntity();
    } else if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
      log.error("Invalid ccGuid=" + ccGuid + " userId=" + userId);
    } else {
      String errorResponse = null;
      if (response.getEntity() instanceof InputStream) {
        try {
          errorResponse = IOUtils.toString((InputStream) response.getEntity(), "UTF-8");
        } catch (IOException e) {
          log.error("", e);
        }
      }
      log.error("System error occured while calling getUserCC api  userId=" + userId + " ccGuid=" + ccGuid + " responseCode="
          + response.getStatus() + " errorResponse=" + errorResponse);
    }
    return null;
  }

  public CustomerPaymentInstrumentMappingsResponseV2 getHiddenListingCCid(String userId, String ccGuid) {
    String userCCidMappingApiUrl = getProperty("userccid.hiddenlisting.api.url.v2",
        "https://payments.api.stubcloudprod.com/user/customers/v1/{customerid}/paymentInstrumentMappingsV2/{paymentinstrumentmappingsid}");
    userCCidMappingApiUrl = userCCidMappingApiUrl.replace("{customerid}", userId);
    userCCidMappingApiUrl = userCCidMappingApiUrl.replace("{paymentinstrumentmappingsid}", ccGuid);
    ResponseReader reader = new ResponseReader();
    reader.setEntityClass(CustomerPaymentInstrumentMappingsResponseV2.class);
    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
    responseReader.add(reader);
    log.info("userCCidMappingApiUrl = " + userCCidMappingApiUrl);
    WebClient webClient = svcLocator.locate(userCCidMappingApiUrl, responseReader);
    SHServiceContext shServiceContext = (SHServiceContext) SHThreadLocals.get(SHServiceContext.SERVICE_CONTEXT_HEADER);
    if (shServiceContext != null && shServiceContext.getAttributeMap() != null) {
      SHServiceContextSerializer serializer = new SHServiceContextSerializer();
      String serializedServiceContext = serializer.serialize(shServiceContext);
      String contextAsString = shServiceContext.getAttributeMap().toString();
      if (contextAsString != null) {
        webClient.header("Authorization", "Bearer " + masterStubhubProperties.getProperty("newapi.accessToken", appToken));
        webClient.header(SHServiceContext.SERVICE_CONTEXT_HEADER, serializedServiceContext);
        webClient.accept(MediaType.APPLICATION_JSON);
        webClient.header("Content-Type", MediaType.APPLICATION_JSON);
      }
    }

    SHMonitor mon = SHMonitorFactory.getMonitor();
    Response response = null;
    try {
      mon.start();
      response = webClient.get();
    } finally {
      mon.stop();
      log.info(SHMonitoringContext.get() + " _operation=getHiddenListingCCid" + " _message= service call for ccGuid=" + ccGuid + " userId="
          + userId + "  _respTime=" + mon.getTime());
    }

    if (Response.Status.OK.getStatusCode() == response.getStatus()) {
      log.info("getHiddenListingCCid api call successful for ccGuid=" + ccGuid + " userId=" + userId);
      return (CustomerPaymentInstrumentMappingsResponseV2) response.getEntity();
    } else if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
      log.error("Invalid ccGuid=" + ccGuid + " userId=" + userId);
    } else {
      log.error("System error occured while calling getHiddenListingCCid api  userId=" + userId + " ccGuid=" + ccGuid + " responseCode="
          + response.getStatus());
    }
    return null;
  }

  public boolean isSellerPaymentContactIdPopulated(String sellerGuId, Long paymentTypeId,
      List<CustomerPaymentInstrumentDetailsV2> sellerPaymentInstruments) {
    boolean isSellerPaymentContactPopulated = false;
    String paymentType = "";
    if (paymentTypeId != null) {
      if (paymentTypeId.equals(PaymentType.Check.getType())) {
        paymentType = "check";
      } else if (paymentTypeId.equals(PaymentType.Paypal.getType())) {
        paymentType = "paypal";
      } else if (paymentTypeId.equals(PaymentType.LargeSellerCheck.getType())) {
        paymentType = "largeSellerCheck";
      } else if (paymentTypeId.equals(PaymentType.ACH.getType())) {
        paymentType = "ACH";
      } else {
        return true;
      }
      CustomerPaymentInstrumentDetailsV2 paymentDetails = getSellerPaymentInstrumentByPaymentType(sellerGuId, paymentType,
          sellerPaymentInstruments);
      if (paymentDetails != null) {
        isSellerPaymentContactPopulated = true;
      }
    }
    return isSellerPaymentContactPopulated;
  }

  public boolean isSellerContactValid(String sellerGuid, Long contactId) {
    CustomerContactDetails userContact = getUserContact(sellerGuid, contactId);
    if (userContact != null) {
      return true;
    }
    return false;
  }

  public boolean isUserContactGuidValid(String userGuid, String contactGuid) {
    CustomerContactV2Details userContact = getCustomerContactByContactGuidV2(userGuid, contactGuid);
    if (userContact != null) {
      return true;
    }
    return false;
  }

  public boolean isSellerCCIdValid(String sellerId, Long ccId) {
    CustomerPaymentInstrumentMappingsResponse customerPIMappingsResponse = getPid(sellerId, ccId, "RECEIVABLE");
    if (customerPIMappingsResponse != null && customerPIMappingsResponse.getId() != null) {
      if (new Long(customerPIMappingsResponse.getInternalId()).equals(ccId)) {
        log.debug("Requested ccId is valid for sellerId=" + sellerId);
        CustomerPaymentInstrumentDetails userPayment = getUserCC(sellerId, new Long(customerPIMappingsResponse.getId()));
        if ((userPayment != null) && (userPayment.getCardDetails() != null) && (userPayment.getActive() != null && (userPayment.getActive()
            .equalsIgnoreCase("Y")))) {
          CreditCardDetails cardDetails = userPayment.getCardDetails();
          try {
            return isCCValid(cardDetails.getExpirationDate());
          } catch (NumberFormatException e) {
            log.error(
                "Parsing Exception while parsing the Credit Card Expiration Date for sellerId" + sellerId + " Exception=" + e.getMessage());
          }
        }
      }
    }
    return false;
  }

  public Boolean isSellerPaymentTypeValid(Long sellerId, Long eventId, Long paymentTypeId) {
    List<PayableInstrumentType> payableInstrTypes = getPaymentInstrTypes(eventId);
    if (payableInstrTypes != null && !payableInstrTypes.isEmpty()) {
      for (PayableInstrumentType payableInstrumentType : payableInstrTypes) {
        if (payableInstrumentType.getId().equals(paymentTypeId)) {
          log.debug("PaymentType= " + payableInstrumentType.getDescription() + " is supported for event=" + eventId);
          return true;
        }
      }
    }
    return false;
  }

  private List<PayableInstrumentType> getPaymentInstrTypes(Long eventId) {
    String payableInstrTypesApiUrl = getProperty("payableinstrtypes.api.url",
        "https://api.stubcloudprod.com/payment/payableInstrumentType/v1");
    ResponseReader reader = new ResponseReader();
    reader.setEntityClass(PayableInstrumentTypeResponse.class);
    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
    responseReader.add(reader);
    log.debug("payableInstrTypesApiUrl= " + payableInstrTypesApiUrl);
    WebClient webClient = svcLocator.locate(payableInstrTypesApiUrl, responseReader);
    webClient.query("eventId", eventId);
    webClient.accept(MediaType.APPLICATION_JSON);
    setSHServiceContext(webClient);

    SHMonitor mon = SHMonitorFactory.getMonitor();
    Response response = null;
    try {
      mon.start();
      response = webClient.get();
    } finally {
      mon.stop();
      log.info(
          SHMonitoringContext.get() + " _operation=getPaymentInstrTypes" + " _message= service call for eventId=" + eventId + "  _respTime="
              + mon.getTime());
    }

    if (Response.Status.OK.getStatusCode() == response.getStatus()) {
      log.info("GetPayableInstruments api call successful for eventId=" + eventId);
      PayableInstrumentTypeResponse paymentInstrTypeResponse = (PayableInstrumentTypeResponse) response.getEntity();
      if (paymentInstrTypeResponse != null) {
        return paymentInstrTypeResponse.getPayableInstrumentTypes();
      }
    } else if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
      log.error("Invalid event Id=" + eventId);
    } else {
      log.error(
          "System error occured while calling getPaymentInstrTypes api  eventId=" + eventId + " responseCode=" + response.getStatus());
    }
    return new ArrayList<PayableInstrumentType>();
  }

  public List<CustomerPaymentInstrumentDetailsV2> getAllSellerPaymentInstrumentV2(String sellerGuId) {
    String getAllPaymentInstrApiUrl = getProperty("getallsellerpayment.api.url.v2",
        "https://payments.api.stubcloudprod.com/user/customers/v1/{sellerId}/paymentInstrumentsV2");
    getAllPaymentInstrApiUrl = getAllPaymentInstrApiUrl.replace("{sellerId}", sellerGuId);
    WebClient webClient = svcLocator.locate(getAllPaymentInstrApiUrl);
    SHServiceContext shServiceContext = (SHServiceContext) SHThreadLocals.get(SHServiceContext.SERVICE_CONTEXT_HEADER);
    if (shServiceContext != null && shServiceContext.getAttributeMap() != null) {
      String contextAsString = shServiceContext.getAttributeMap().toString();
      if (contextAsString != null) {
        webClient.header(SHServiceContext.SERVICE_CONTEXT_HEADER, contextAsString);
      }
    }
    webClient.accept(MediaType.APPLICATION_JSON);

    SHMonitor mon = SHMonitorFactory.getMonitor();
    Response response = null;
    try {
      mon.start();
      response = webClient.get();
    } finally {
      mon.stop();
      log.info(
          SHMonitoringContext.get() + " _operation=getAllSellerPaymentInstrumentV2" + " _message= service call for sellerGuid=" + sellerGuId
              + "  _respTime=" + mon.getTime());
    }

    if (Response.Status.OK.getStatusCode() == response.getStatus()) {
      log.info("getAllSellerPaymentInstrumentV2 api call successful for sellerGuid=" + sellerGuId);
      CustomerPaymentInstrumentsResponseV2 customerPaymentInstrumentsResponse = getResponse(response);
      if (customerPaymentInstrumentsResponse != null) {
        return customerPaymentInstrumentsResponse.getPaymentInstruments();
      }
    } else {
      log.error("Error occured while calling getAllSellerPaymentInstrumentV2 api for  sellerGuId=" + sellerGuId + " responseCode="
          + response.getStatus());
    }
    return new ArrayList<CustomerPaymentInstrumentDetailsV2>();
  }

  private CustomerPaymentInstrumentsResponseV2 getResponse(Response res) {
    Object respEntity = res.getEntity();
    InputStream responseStream = (InputStream) respEntity;
    byte[] data = new byte[1024];
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      while (true) {
        int n;
        n = responseStream.read(data);
          if (n == -1) {
              break;
          }
        bos.write(data, 0, n);
      }

      String responseString = bos.toString();
      log.debug("responseString: " + responseString);
      return (CustomerPaymentInstrumentsResponseV2) JsonUtil.toObjectWrapRoot(responseString, CustomerPaymentInstrumentsResponseV2.class);
    } catch (IOException e) {
      log.error("get CustomerPaymentInstrumentsResponseV2 error: " + e.getMessage());
    }
    return null;

  }

  public CustomerPaymentInstrumentDetailsV2 getSellerPaymentInstrumentByPaymentType(String sellerGuId, String paymentType,
      List<CustomerPaymentInstrumentDetailsV2> pCustomerpaymentInstrumentDetailsList) {
    //SELLAPI-1135 sonar-rules, avoid reassigning to parameters.
    List<CustomerPaymentInstrumentDetailsV2> customerpaymentInstrumentDetailsList = pCustomerpaymentInstrumentDetailsList;
    if (customerpaymentInstrumentDetailsList == null) {
      customerpaymentInstrumentDetailsList = getAllSellerPaymentInstrumentV2(sellerGuId);
    }
    if (customerpaymentInstrumentDetailsList != null && customerpaymentInstrumentDetailsList.size() > 0) {
      for (CustomerPaymentInstrumentDetailsV2 customerpaymentInstrumentDetail : customerpaymentInstrumentDetailsList) {
        if (customerpaymentInstrumentDetail.getPaymentType().equalsIgnoreCase(paymentType)) {
          return customerpaymentInstrumentDetail;
        }
      }
    }
    return null;
  }

  public Long getMappedValidSellerCCId(String sellerGuid, String pId,
      List<CustomerPaymentInstrumentDetailsV2> pCustomerPaymentInstrumentDetailsList,
      String listingStatus) {
    Long validMappedSellerCCId = null;
    //SELLAPI-1135 sonar-rules, avoid reassigning to parameters.
    List<CustomerPaymentInstrumentDetailsV2> customerPaymentInstrumentDetailsList = pCustomerPaymentInstrumentDetailsList;
    if (customerPaymentInstrumentDetailsList == null) {
      customerPaymentInstrumentDetailsList = getAllSellerPaymentInstrumentV2(sellerGuid);
    }

    if (customerPaymentInstrumentDetailsList != null && customerPaymentInstrumentDetailsList.size() > 0) {
      for (CustomerPaymentInstrumentDetailsV2 customerpaymentInstrumentDetail : customerPaymentInstrumentDetailsList) {
        if ((customerpaymentInstrumentDetail != null) && (customerpaymentInstrumentDetail.getCardDetails() != null)) {
          if (customerpaymentInstrumentDetail.getId().equals(pId)) {
            log.debug("Requested pId=" + pId + " is valid for sellerGuid=" + sellerGuid);
            CreditCardDetails cardDetails = customerpaymentInstrumentDetail.getCardDetails();
            try {
              if (isCCValid(cardDetails.getExpirationDate())) {
                CustomerPaymentInstrumentMappingsResponseV2 customerPaymentInstrumentMappingsResponse = null;
                if ("HIDDEN".equalsIgnoreCase(listingStatus) && isAuthZRequest()) {
                  customerPaymentInstrumentMappingsResponse = getHiddenListingCCid(sellerGuid, pId);
                } else {
                  customerPaymentInstrumentMappingsResponse = getCCid(sellerGuid, pId);
                }
                if (customerPaymentInstrumentMappingsResponse != null) {
                  validMappedSellerCCId = Long.parseLong(customerPaymentInstrumentMappingsResponse.getInternalId());
                  return validMappedSellerCCId;
                }
              }
            } catch (NumberFormatException e) {
              log.error(
                  "Parsing Exception while parsing the Credit Card Expiration Date for sellerGuId"
                      + sellerGuid + " Exception=" + e.getMessage());
            }
          }
        }
      }
    }
    return validMappedSellerCCId;

  }

  private boolean isCCValid(ExpDate expirationDate) {
    Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    if (today.get(Calendar.YEAR) < Integer.parseInt(expirationDate.getYear())) {
      return true;
    } else if (today.get(Calendar.YEAR) == Integer.parseInt(expirationDate.getYear())
        && (today.get(Calendar.MONTH) + 1) <= Integer.parseInt(expirationDate.getMonth())) {
      return true;
    }
    return false;
  }


  public CustomerContactV2Details getDefaultCustomerContactV2(String customerGuid,
      boolean createApiContext) {
    String customerContactsApiUrl = getProperty(CUSTOMER_CONTACT_API_URL,
        "https://api.stubcloudprod.com/user/customers/v1/{customerGuid}/contactsV2");
    customerContactsApiUrl = customerContactsApiUrl.replace("{customerGuid}", customerGuid);

    ResponseReader reader = new ResponseReader();
    reader.setEntityClass(CustomerContactsV2Response.class);
    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
    responseReader.add(reader);

    try {
      WebClient webClient =
          getWebClientFromSveLocator(customerContactsApiUrl, responseReader, createApiContext);

      webClient.accept(MediaType.APPLICATION_JSON);
      String xshServiceContextValue =
          XSH_SERVICE_CONTEXT_HEADER.replace("{customerGuid}", customerGuid);
      webClient.header(XSH_SERVICE_CONTEXT, xshServiceContextValue);
      log.info("_message= \"Making call to CustomerContactV2 API\" apiUrl={} customerGuid={}",
          customerContactsApiUrl, customerGuid);

      SHMonitor mon = SHMonitorFactory.getMonitor();
      Response response = null;
      try {
        mon.start();
        response = webClient.get();
      } finally {
        mon.stop();
        log.info(SHMonitoringContext.get() + " _operation=getDefaultCustomerContactV2" + " _message= service call for customerGuid="
            + customerGuid + "  _respTime=" + mon.getTime());
      }

      if (Response.Status.OK.getStatusCode() == response.getStatus()) {
        CustomerContactsV2Response getCustomerResponse =
            (CustomerContactsV2Response) response.getEntity();
        if (getCustomerResponse != null && getCustomerResponse.getContact() != null
            && getCustomerResponse.getContact().size() > 0) {
          for (CustomerContactV2Details contactV2Details : getCustomerResponse.getContact()) {
            if (DEFAULT_INDICATOR.equalsIgnoreCase(contactV2Details.getDefaultInd())) {
              return contactV2Details;
            }
          }
        }
      } else if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
        log.error("_message=\"Invalid customerGuid\" customerGuid={}", customerGuid);
      } else {
        log.error(
            "_message=\"System error occured while calling getUserContact api\"  customerGuid={} "
                + "responseCode={}",
            customerGuid, response.getStatus());
      }
    } catch (Exception e) {
      log.error(
          "_message=\"Unknown exception while making CustomerContactV2 API call\" customerGuid={}",
          customerGuid, e.getMessage());
    }
    return null;
  }

  public CustomerContactV2Details getCustomerContactByContactGuidV2(String userGuid, String contactGuid) {
    String customerContactsApiUrl = getProperty(CUSTOMER_CONTACT_V2_GET_API_URL, CUSTOMER_CONTACT_V2_GET_API_URL_DEFAULT);
    customerContactsApiUrl = customerContactsApiUrl.replace("{userGuid}", userGuid);
    customerContactsApiUrl = customerContactsApiUrl.replace("{contactGuid}", contactGuid);

    log.info(
        "api_domain={} api_method={} _message=\"Calling getCustomerContactV2 api\", customerContactsV2ApiUrl={}, userGuid={},  contactGuid={}",
        DOMAIN, "getCustomerContactByContactGuidV2", customerContactsApiUrl, userGuid, contactGuid);

    ResponseReader reader = new ResponseReader();
    reader.setEntityClass(CustomerContactV2Details.class);
    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
    responseReader.add(reader);

    try {
      WebClient webClient = svcLocator.locate(customerContactsApiUrl, responseReader);
      webClient.accept(MediaType.APPLICATION_JSON);

      setSHServiceContext(webClient);

      SHMonitor mon = SHMonitorFactory.getMonitor();
      Response response = null;
      try {
        mon.start();
        response = webClient.get();
      } finally {
        mon.stop();
        log.info(SHMonitoringContext.get()
                + "api_domain={} api_method={} _message=\"Called getCustomerContactV2 api\", customerContactsV2ApiUrl={}, userGuid={},  contactGuid={} _respTime={}",
            DOMAIN, "getCustomerContactByContactGuidV2", customerContactsApiUrl, userGuid, contactGuid, mon.getTime());
      }

      if (Response.Status.OK.getStatusCode() == response.getStatus()) {
        CustomerContactV2Details contactV2Details = (CustomerContactV2Details) response.getEntity();
        if (contactV2Details != null) {
          return contactV2Details;
        }

      } else if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
        log.error("api_domain={} api_method={} message=\"resource not found\" userGuid={} contactId={} respCode={}",
            DOMAIN, "getCustomerContactByContactGuidV2", userGuid, contactGuid, response.getStatus());
      } else {
        log.error(
            "api_domain={} api_method={} message=\"System error occured while calling getUserContact api\" userGuid={} contactId={} respCode={}",
            DOMAIN, "getCustomerContactByContactGuidV2", userGuid, contactGuid, response.getStatus());

      }
    } catch (Exception e) {
      log.error("api_domain={} api_method={} message=\"Unknown exception while making CustomerContactV2 API call\" userGuid={}",
          DOMAIN, "getCustomerContactByContactGuidV2", userGuid, e.getMessage());
    }
    return null;
  }

  public CustomerContactMappingResponse getCustomerContactId(String userGuid, String contactGuid) {

    String contactMappingUrl = getProperty(CONTACT_MAPPING_API_URL, CONTACT_MAPPING_API_URL_DEFAULT);
    contactMappingUrl = contactMappingUrl.replace("{userGuid}", userGuid);
    contactMappingUrl = contactMappingUrl.replace("{contactGuid}", contactGuid);

    log.info("api_domain={}, api_method={}, calling, apiURI={}, userGuid={}, contactGuid={}  ", DOMAIN, "getCustomerContactId",
        contactMappingUrl, userGuid, contactGuid);
    boolean isAuthZRequest = isAuthZRequest();
    String contactIdFromProxiedId = null;

    if (isAuthZRequest) {
      contactIdFromProxiedId = getProxiedId();
      changeProxiedIdToUserIdOrUserGuid(userGuid);
      log.info("api_domain={}, api_method={}, AuthZ request, apiURI={}, userGuid={}, contactGuid={} proxiedId={} ", DOMAIN,
          "getCustomerContactGuid", contactMappingUrl, userGuid, contactGuid, contactIdFromProxiedId);
    }
    ResponseReader reader = new ResponseReader();
    reader.setEntityClass(CustomerContactMappingResponse.class);
    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
    responseReader.add(reader);
    try {
      WebClient webClient = svcLocator.locate(contactMappingUrl, responseReader);
      webClient.accept(MediaType.APPLICATION_JSON);

      if (isAuthZRequest) {
        SHServiceContext shServiceContext = (SHServiceContext) SHThreadLocals.get(SHServiceContext.SERVICE_CONTEXT_HEADER);
        SHServiceContextSerializer serializer = new SHServiceContextSerializer();
        String serializedServiceContext = serializer.serialize(shServiceContext);
        webClient.header(SHServiceContext.SERVICE_CONTEXT_HEADER, serializedServiceContext);
      }

      SHMonitor mon = SHMonitorFactory.getMonitor();
      Response response = null;
      try {
        mon.start();
        response = webClient.get();
      } finally {
        mon.stop();
        log.info(SHMonitoringContext.get()
                + "api_domain={} api_method={} _message=\"Called getCustomerContactId api\", contactMappingUrl={}, userGuid={},  contactGuid={} _respTime={}",
            DOMAIN, "getCustomerContactGuid", contactMappingUrl, userGuid, contactGuid, mon.getTime());
      }
      if (isAuthZRequest) {
        changeProxiedIdToUserIdOrUserGuid(contactIdFromProxiedId);
      }
      if (Response.Status.OK.getStatusCode() == response.getStatus()) {
        log.info("api_domain={} api_method={} message=\" contactMapping api call successful for \" userGuid={} contactId={} respCode={}",
            DOMAIN, "getUserContactId", userGuid, contactGuid, response.getStatus());
        return (CustomerContactMappingResponse) response.getEntity();
      } else if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
        log.error("api_domain={} api_method={} message=\"resource not found\" userGuid={} contactId={} respCode", DOMAIN,
            "getCustomerContactId", userGuid, contactGuid, response.getStatus());
      } else {
        log.error(
            "api_domain={} api_method={} message=\"System error occured while calling ContactMapping api\" userGuid={} contactId={} respCode={}",
            DOMAIN, "getCustomerContactId", userGuid, contactGuid, response.getStatus());
      }
    } catch (Exception e) {
      log.error("api_domain={} api_method={} message=\"Unknown exception while making ContactMapping API call\" userGuid={}",
          DOMAIN, "getCustomerContactId", userGuid, e.getMessage());
    }
    return null;
  }

  public CustomerContactMappingResponse getCustomerContactGuid(String userGuid, String contactId) {

    String contactMappingInverseUrl = getProperty(CONTACT_MAPPING_INVERSE_API_URL, CONTACT_MAPPING_INVERSE_API_URL_DEFAULT);
    contactMappingInverseUrl = contactMappingInverseUrl.replace("{userGuid}", userGuid);
    contactMappingInverseUrl = contactMappingInverseUrl.replace("{internalId}", contactId);

    log.info("api_domain={}, api_method={}, calling, apiURI={}, userGuid={}, contactGuid={}  ", DOMAIN, "getCustomerContactGuid",
        contactMappingInverseUrl, userGuid, contactId);
    boolean isAuthZRequest = isAuthZRequest();
    String contactIdFromProxiedId = null;

    if (isAuthZRequest) {
      contactIdFromProxiedId = getProxiedId();
      changeProxiedIdToUserIdOrUserGuid(userGuid);
      log.info("api_domain={}, api_method={}, AuthZ request, apiURI={}, userGuid={}, contactId={} proxiedId={} ", DOMAIN,
          "getCustomerContactGuid", contactMappingInverseUrl, userGuid, contactId, contactIdFromProxiedId);
    }

    ResponseReader reader = new ResponseReader();
    reader.setEntityClass(CustomerContactMappingResponse.class);
    List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
    responseReader.add(reader);
    try {
      WebClient webClient = svcLocator.locate(contactMappingInverseUrl, responseReader);
      webClient.accept(MediaType.APPLICATION_JSON);

      if (isAuthZRequest) {
        SHServiceContext shServiceContext = (SHServiceContext) SHThreadLocals.get(SHServiceContext.SERVICE_CONTEXT_HEADER);
        SHServiceContextSerializer serializer = new SHServiceContextSerializer();
        String serializedServiceContext = serializer.serialize(shServiceContext);
        webClient.header(SHServiceContext.SERVICE_CONTEXT_HEADER, serializedServiceContext);
      }

      SHMonitor mon = SHMonitorFactory.getMonitor();
      Response response = null;
      try {
        mon.start();
        response = webClient.get();
      } finally {
        mon.stop();
        log.info(SHMonitoringContext.get()
                + "api_domain={} api_method={} _message=\"Called getCustomerContactGuid api\", contactMappingInverseUrl={}, userGuid={},  contactId={} _respTime={}",
            DOMAIN, "getCustomerContactGuid", contactMappingInverseUrl, userGuid, contactId, mon.getTime());
      }
      if (isAuthZRequest) {
        changeProxiedIdToUserIdOrUserGuid(contactIdFromProxiedId);
      }

      if (Response.Status.OK.getStatusCode() == response.getStatus()) {
        log.info(
            "api_domain={} api_method={} message=\" contactMapping inverse api call successful for \" userGuid={} contactId={} respCode={}",
            DOMAIN, "getCustomerContactGuid", userGuid, contactId, response.getStatus());
        return (CustomerContactMappingResponse) response.getEntity();
      } else if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
        log.error("api_domain={} api_method={} message=\"resource not found\" userGuid={} contactId={} respCode", DOMAIN,
            "getCustomerContactGuid", userGuid, contactId, response.getStatus());
      } else {
        log.error(
            "api_domain={} api_method={} message=\"System error occured while calling contactMapping inverse  api\" userGuid={} contactId={} respCode={}",
            DOMAIN, "getCustomerContactGuid", userGuid, contactId, response.getStatus());
      }
    } catch (Exception e) {
      log.error(
          "api_domain={} api_method={} message=\"Unknown exception while making ContactMapping inverse API call\" userGuid={} contactId={}",
          DOMAIN, "getCustomerContactGuid", userGuid, contactId, e.getMessage());
    }
    return null;
  }


  public WebClient getWebClientFromSveLocator(String apiUrl, List<ResponseReader> responseReader,
      boolean createApiContext) {
    WebClient webClient = svcLocator.locate(apiUrl, responseReader);
    if (createApiContext) {
      SHAPIContext apiContext = SHAPIThreadLocal.getAPIContext();
      if (apiContext == null) {
        apiContext = new SHAPIContext();
        SHAPIThreadLocal.set(apiContext);
      }
      String accessToken = getProperty(NEWAPI_ACCESS_TOKEN_KEY, ACCESS_TOCKEN_DEFAULT_VALUE);
      String authorization = BEARER + accessToken;
      webClient.header(AUTHORIZATION, authorization);
    }
    return webClient;
  }

  public boolean isAuthZRequest() {
    SHServiceContext shServiceContext = (SHServiceContext) SHThreadLocals.get(SHServiceContext.SERVICE_CONTEXT_HEADER);
    if (shServiceContext != null) {
      String role = shServiceContext.getRole();
      String operatorId = shServiceContext.getOperatorId();
      String proxiedId = shServiceContext.getProxiedId();
        if (StringUtils.isNotBlank(operatorId)
            && StringUtils.isNotBlank(proxiedId)
            && StringUtils.isNotBlank(role)) {
            return ("R2".compareToIgnoreCase(role.trim()) == 0)
                || ("R3".compareToIgnoreCase(role.trim()) == 0);
        }
    }

    return false;
  }

  public ProSellerInfo getSellerInfo(Long sellerId) {
    ProSellerInfo sellerInfo = null;

    SellerType sellerType = getSellerType(sellerId);
    if (sellerType == null) {
      log.warn("api_domain={} api_method={} sellerId={} message=\"No seller type found\"", DOMAIN, "getSellerInfo", sellerId);
      return null;
    }
    log.info("api_domain={} api_method={} sellerId={} sellerType={}", DOMAIN, "getSellerInfo", sellerId, sellerType.name());
    switch (sellerType) {
      case TRADER:
        String sellerGuid = integrationManager.getUserGuid(sellerId);
        CustomerContactV2Details contactDetails = this.getDefaultCustomerContactV2(sellerGuid, true);
        if (contactDetails == null) {
          log.error("api_domain={} api_method={} sellerId={} message=\"No contact details found for the given seller\"", DOMAIN, "getSellerInfo", sellerId);
          return null;
        }
        String sellerFullName = contactDetails.getName().getFirstName() + " " + contactDetails.getName().getLastName();
        log.info("api_domain={} api_method={} sellerId={} sellerType={} sellerName={}", DOMAIN, "getSellerInfo", sellerId, sellerType.name(), sellerFullName);
        ProSellerAddress traderAddress = null;
        if (contactDetails.getAddress() != null) {
          traderAddress = new ProSellerAddress(
              contactDetails.getAddress().getLine1(),
              contactDetails.getAddress().getLine2(),
              contactDetails.getAddress().getCity(),
              contactDetails.getAddress().getState(),
              contactDetails.getAddress().getCountry(),
              contactDetails.getAddress().getZipOrPostalCode()
          );
        }
        sellerInfo = new ProSellerInfo();
        sellerInfo.setSellerType(sellerType.name());
        sellerInfo.setSellerName(sellerFullName);
        sellerInfo.setAddress(traderAddress);
        break;
      case BUSINESS:
        BusinessInfo businessInfo = businessHelper.getBusinessInfo(sellerId);
        if (businessInfo == null) {
          log.error("api_domain={} api_method={} sellerId={} message=\"No business info found for the given seller\"", DOMAIN,
              "getSellerInfo", sellerId);
          return null;
        }
        log.info("api_domain={} api_method={} sellerId={} sellerType={} businessInfo={}", DOMAIN, "getSellerInfo", sellerId, sellerType.name(), businessInfo);
        ProSellerAddress businessAddress = null;
        if (businessInfo.getAddress() != null) {
          businessAddress = new ProSellerAddress(
              businessInfo.getAddress().getLineAddress1(),
              businessInfo.getAddress().getLineAddress2(),
              businessInfo.getAddress().getCity(),
              businessInfo.getAddress().getState(),
              businessInfo.getAddress().getCountry(),
              businessInfo.getAddress().getPostCode()
          );
        }
        sellerInfo = new ProSellerInfo();
        sellerInfo.setSellerType(sellerType.name());
        sellerInfo.setSellerName(businessInfo.getCompanyName());
        sellerInfo.setAddress(businessAddress);
        break;
    }
    log.info("api_domain={} api_method={} sellerId={} sellerInfo={}", DOMAIN, "getSellerInfo", sellerId, sellerInfo);
    return sellerInfo;
  }

  private SellerType getSellerType(Long sellerId) {
    String sellerTypeApiUrl = getProperty("domain.user.sellertype.api.url", "https://api.stubcloudprod.com/user/customers/v1/{sellerId}/sellerType");
    sellerTypeApiUrl = sellerTypeApiUrl.replace("{sellerId}", sellerId.toString());

    log.info(
        "api_domain={} api_method={} message=\"Calling getSellerType api\", sellerTypeApiUrl={}, sellerId={}",
        DOMAIN, "getSellerType", sellerTypeApiUrl, sellerId);

    ResponseReader reader = new ResponseReader();
    reader.setEntityClass(SellerTypeResponse.class);
    List<ResponseReader> responseReader = new ArrayList<>();
    responseReader.add(reader);

    try {
      WebClient webClient = svcLocator.locate(sellerTypeApiUrl, responseReader);
      webClient.accept(MediaType.APPLICATION_JSON);
      webClient.type(MediaType.APPLICATION_JSON);

      setSHServiceContext(webClient);

      SHMonitor mon = SHMonitorFactory.getMonitor();
      Response response;
      try {
        mon.start();
        response = webClient.get();
      } finally {
        mon.stop();
        log.info(SHMonitoringContext.get()
                + "api_domain={} api_method={} message=\"Called getSellerType api\", sellerTypeApiUrl={}, sellerId={}, _respTime={}",
            DOMAIN, "getSellerType", sellerTypeApiUrl, sellerId, mon.getTime());
      }

      if (Response.Status.OK.getStatusCode() == response.getStatus()) {
        SellerTypeResponse sellerTypeResponse = (SellerTypeResponse) response.getEntity();
        if (sellerTypeResponse != null) {
          log.info(SHMonitoringContext.get()
                  + "api_domain={} api_method={} message=\"getSellerType api response\" sellerId={} sellerType={}",
              DOMAIN, "getSellerType", sellerId, sellerTypeResponse.getSellerType());
          return SellerType.findByName(sellerTypeResponse.getSellerType());
        }

      } else if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
        log.warn("api_domain={} api_method={} message=\"No seller type found\" sellerId={} respCode={}",
            DOMAIN, "getSellerType", sellerId, response.getStatus());
      } else {
        log.error(
            "api_domain={} api_method={} message=\"System error occurred while calling getSellerType api\" sellerId={} respCode={}",
            DOMAIN, "getSellerType", sellerId, response.getStatus());

      }
    } catch (Exception ex) {
      log.error("api_domain={} api_method={} message=\"Unknown exception while making getSellerType API call\" sellerId={} error=\"{}\"",
          DOMAIN, "getSellerType", sellerId, ex.getMessage());
    }
    return null;
  }

  private String getProxiedId() {
    SHServiceContext shServiceContext = (SHServiceContext) SHThreadLocals.get(SHServiceContext.SERVICE_CONTEXT_HEADER);
    if (shServiceContext != null) {
      return shServiceContext.getProxiedId();
    }
    return null;
  }


  private void changeProxiedIdToUserIdOrUserGuid(String contactIdOrGuid) {
    SHServiceContext shServiceContext = (SHServiceContext) SHThreadLocals.get(SHServiceContext.SERVICE_CONTEXT_HEADER);
    if (shServiceContext != null) {
      shServiceContext.setAttribute("proxiedId", contactIdOrGuid);
    }
    SHThreadLocals.set(SHServiceContext.SERVICE_CONTEXT_HEADER, shServiceContext);
  }


}