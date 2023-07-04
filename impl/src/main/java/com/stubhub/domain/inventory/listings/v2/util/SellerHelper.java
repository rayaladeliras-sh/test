package com.stubhub.domain.inventory.listings.v2.util;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.client.ResponseReader;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.infrastructure.common.core.concurrent.SHThreadLocals;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContext;
import com.stubhub.domain.infrastructure.soa.core.context.SHServiceContextSerializer;
import com.stubhub.domain.inventory.common.entity.ErrorEnum;
import com.stubhub.domain.inventory.common.util.Error;
import com.stubhub.domain.inventory.common.util.ListingException;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.user.business.intf.UserBusinessStatus;
import com.stubhub.domain.user.intf.GetCustomerResponse;
import com.stubhub.newplatform.property.MasterStubHubProperties;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

@Component("sellerHelper")
public class SellerHelper {

    private final static Logger log = Logger.getLogger(SellerHelper.class);

    @Autowired
    private SvcLocator svcLocator;
    
    @Autowired
    private UserHelper userHelper;

    /**
     * helper method to call getCustomer api
     *
     * @param listing
     * @throws ListingException
     */
    public boolean populateSellerDetails(Listing listing) throws ListingException {
        String customerContactsApiUrl = getProperty("userdefaultcontact.api.url", "https://api.stubcloudprod.com/user/customers/v1/");
        customerContactsApiUrl = customerContactsApiUrl + listing.getSellerId() + "?action=defaultContact";

        log.debug("customerContactsApiUrl=" + customerContactsApiUrl + " :: SellerId=" + listing.getSellerId());
        ResponseReader reader = new ResponseReader();
        reader.setEntityClass(GetCustomerResponse.class);
        List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
        responseReader.add(reader);

        WebClient webClient = svcLocator.locate(customerContactsApiUrl, responseReader);
        
        if(userHelper.isAuthZRequest()) {
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
            log.info(SHMonitoringContext.get() + " _operation=populateSellerDetails" + " _message= service call for SellerId=" + listing.getSellerId() + "  _respTime=" + mon.getTime());
        }

        int statusCode = response.getStatus();
        log.info("customerContactsApiUrl=" + customerContactsApiUrl + " :: responseStatus=" + statusCode);
        if (Response.Status.OK.getStatusCode() == statusCode) {
            GetCustomerResponse getCustomerResponse = (GetCustomerResponse) response.getEntity();
            if (listing.getSellerContactId() == null && getCustomerResponse != null && getCustomerResponse.getCustomerContact() != null) {
                listing.setSellerContactId(getCustomerResponse.getCustomerContact().getId());
            }
			if (getCustomerResponse != null) {
				listing.setTaxpayerStatus(getCustomerResponse.getCustomerContact().getTaxpayerStatus());
			}
        } else if (Response.Status.NOT_FOUND.getStatusCode() == statusCode) {
            log.error("Invalid seller id=" + listing.getSellerId());
            Error error = new Error(ErrorEnum.INVALID_SELLER_GUID.getCode(), ErrorEnum.INVALID_SELLER_GUID.getMessage());
            throw new ListingException(listing.getCorrelationId(), error);
        } else{
            log.error("System error occured while calling getCustomer api  sellerId={}" + listing.getSellerId() + " responseStatus=" + statusCode);
            Error error = new Error(ErrorEnum.SYSTEM_ERROR.getCode(), ErrorEnum.SYSTEM_ERROR.getMessage());
            throw new ListingException(listing.getCorrelationId(), error);
        }
        return true;
    }

    /**
     * Returns property value for the given propertyName. This protected method has been created to get
     * around the static nature of the MasterStubHubProperties' methods for Unit tests. The test classes are expected to
     * override this method with custom implementation.
     *
     * @param propertyName
     * @param defaultValue
     * @return
     */
    protected String getProperty(String propertyName, String defaultValue) {
        return MasterStubHubProperties.getProperty(propertyName, defaultValue);
    }

    /**
     * @param svcLocator the svcLocator to set
     */
    public void setSvcLocator(SvcLocator svcLocator) {
        this.svcLocator = svcLocator;
    }

    /**
     * helper method to call getCustomerStatuses api
     *
     * @param listing
     * @throws ListingException
     */
    public boolean addBusinessDetails(Listing listing) throws ListingException {
        String userBusinessStatusApiUrl = getProperty("userbusinessstatus.api.url", "https://api.stubcloudprod.com/user/customers/v1/{customerGuid}/statuses");
        if (StringUtils.isEmpty(listing.getSellerGuid())) {
            return false;
        }
        userBusinessStatusApiUrl = userBusinessStatusApiUrl.replace("{customerGuid}", String.valueOf(listing.getSellerGuid()));

        log.debug("userBusinessStatusApiUrl=" + userBusinessStatusApiUrl + " :: SellerGuid=" + listing.getSellerGuid());
        ResponseReader reader = new ResponseReader();
        reader.setEntityClass(UserBusinessStatus.class);
        List<ResponseReader> responseReader = new ArrayList<ResponseReader>();
        responseReader.add(reader);

        WebClient webClient = svcLocator.locate(userBusinessStatusApiUrl, responseReader);

        SHMonitor mon = SHMonitorFactory.getMonitor();
        Response response = null;
        try {
            mon.start();
            response = webClient.get();
        } finally {
            mon.stop();
            log.info(SHMonitoringContext.get() + " _operation=addBusinessDetails" + " _message= service call for sellerGuid=" + listing.getSellerGuid() + "  _respTime=" + mon.getTime());
        }

        log.debug("userBusinessStatusApiUrl=" + userBusinessStatusApiUrl + " :: responseStatus=" + response.getStatus());
        if (Response.Status.OK.getStatusCode() == response.getStatus()) {
            UserBusinessStatus userBusinessStatus = (UserBusinessStatus) response.getEntity();
            log.debug("userBusinessStatus=" + userBusinessStatus);
            if (userBusinessStatus != null) {
                log.debug("businessId=" + userBusinessStatus.getBusinessId() + " businessGuid=" + userBusinessStatus.getBusinessGuid());
                listing.setBusinessId(userBusinessStatus.getBusinessId());
                listing.setBusinessGuid(userBusinessStatus.getBusinessGuid());
            }
            log.info("_message=\"Customer Business Statuses Call Successful \" _status=OK" + " sellerGuid=" + listing.getSellerGuid());
        } else if (Response.Status.BAD_REQUEST.getStatusCode() == response.getStatus()) {
            log.error("_message=\"Bad Request. \" _status=" + response.getStatus() + " sellerGuid=" + listing.getSellerGuid());
            return false;
        } else if (Response.Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
            log.error("_message=\"Invalid sellerGuid. \" _status=" + response.getStatus() + " sellerGuid=" + listing.getSellerGuid());
            return false;
        } else if (Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() == response.getStatus()) {
            log.error("_message=\"System error occured while calling userBusinessStatusApiUrl api. \" _status=" + response.getStatus()
                    + " sellerGuid=" + listing.getSellerGuid());
            return false;
        } else {
            log.error("_message=\"Unknown error. \" _status=" + response.getStatus() + " sellerGuid=" + listing.getSellerGuid());
            return false;
        }
        return true;
    }
}
