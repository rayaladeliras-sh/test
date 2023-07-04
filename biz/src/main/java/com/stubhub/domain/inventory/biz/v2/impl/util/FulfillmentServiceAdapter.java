package com.stubhub.domain.inventory.biz.v2.impl.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.stubhub.domain.fulfillment.pdf.v1.intf.request.CloneAndLinkTicketRequest;
import com.stubhub.domain.fulfillment.pdf.v1.intf.request.CopyTicketSeat;
import com.stubhub.domain.fulfillment.pdf.v1.intf.response.CloneAndLinkTicketResponse;
import org.apache.cxf.jaxrs.client.ResponseReader;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.stubhub.domain.fulfillment.pdf.v1.intf.request.CloneFileInfoRequest;
import com.stubhub.domain.fulfillment.pdf.v1.intf.response.CloneFileInfoResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.EventFulfillmentWindowResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.FulfillmentWindowResponse;
import com.stubhub.domain.fulfillment.window.v1.intf.ListingFulfillmentWindowResponse;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.infrastructure.common.exception.base.SHSystemException;
import com.stubhub.domain.inventory.datamodel.entity.FulfillmentWindow;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

@Component("fulfillmentServiceAdapter")
public class FulfillmentServiceAdapter {

    @Autowired
    private SvcLocator svcLocator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MasterStubhubPropertiesWrapper masterStubhubProperties;

    private static final String FF_API_URL = "fulfillment.window.v1.shape.api.url";
    private static final String FF_API_URL_DEFAULT =
            "https://api.stubcloudprod.com/fulfillment/window/v1/event/{eventId}/?sellerContactId={sellerContactId}";
    private static final String PDF_API_URL = "pdf.clone.v1.shape.api.url";
    private static final String FF_API_CLONE_URL_DEFAULT =
            "https://api.stubcloudprod.com/fulfillment/pdf/v1/cloneticket";

    // log identifiers
    private static final String LOG_DOMAIN_RESOURCE_METHOD_PREFIX =
            "api_domain=inventory api_resource=" + FulfillmentServiceAdapter.class.getSimpleName()
                    + " api_method={} ";
    private static final String LOG_URI_PREFIX = LOG_DOMAIN_RESOURCE_METHOD_PREFIX + "api_uri={}";
    private static final String LOG_INFO_PREFIX =
            LOG_DOMAIN_RESOURCE_METHOD_PREFIX + "input_param={} time_usec={}";
    private static final String LOG_ERROR_NO_MESSAGE_PREFIX =
            LOG_DOMAIN_RESOURCE_METHOD_PREFIX + "input_param={} status=success_with_error time_usec={}";
    private static final String LOG_ERROR_PREFIX = LOG_DOMAIN_RESOURCE_METHOD_PREFIX
            + "input_param={} status=success_with_error error_message={} time_msec={}";

    private final static Logger log = LoggerFactory.getLogger(FulfillmentServiceAdapter.class);

    // SELLAPI-1172 08/03/15 START
    public EventFulfillmentWindowResponse getFulfillmentWindowsShape(Long eventId, Long sellerContactId) {

        // api response objects
        EventFulfillmentWindowResponse efwResponse = null;

        SHMonitor mon = SHMonitorFactory.getMonitor();
        String apiMethod = "getFulfillmentMethods";

        try {
            String ffWindowURL = masterStubhubProperties.getProperty(FF_API_URL, FF_API_URL_DEFAULT);
            ffWindowURL = ffWindowURL.replace("{eventId}", Long.toString(eventId));
            ffWindowURL = ffWindowURL.replace("{sellerContactId}", Long.toString(sellerContactId));

            log.info(LOG_URI_PREFIX, apiMethod, ffWindowURL);
            objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            WebClient webClient = svcLocator.locate(ffWindowURL);
            webClient.accept(MediaType.APPLICATION_JSON);

            Response response;
            try {
                mon.start();
                response = webClient.get();
            } finally {
                mon.stop();
                log.info(SHMonitoringContext.get() + " _operation=getFulfillmentWindowsShape" +
                        " _message= service call for eventId=" + eventId + " sellerContactId" + sellerContactId + "  _respTime=" + mon.getTime());
            }

            if (Response.Status.OK.getStatusCode() == response.getStatus()) {
                log.info("_message=\"fulfillment api response successful\" eventId={} sellerContactId={}",
                        eventId, sellerContactId);

                InputStream is = (InputStream) response.getEntity();
                efwResponse = objectMapper.readValue(is, EventFulfillmentWindowResponse.class);
            } else {
                log.error(LOG_ERROR_PREFIX, apiMethod, eventId, response.getStatus(), mon.getTime());
                throw new SHSystemException(); // 500
            }

        } catch (Exception ex) {
            log.error(LOG_ERROR_NO_MESSAGE_PREFIX, apiMethod, eventId, mon.getTime(), ex);
            throw new SHSystemException(); // 500
        } finally {
            mon.stop();
            log.info(LOG_INFO_PREFIX, apiMethod, eventId, mon.getTime());
        }
        return efwResponse;
    }

    public ListingFulfillmentWindowResponse getFulfillmentWindowsShapeForListing(Long listingId,
                                                                                 Long buyerContactId) {
        final String apiMethod = "getFulfillmentWindowsShapeForListing";
        ListingFulfillmentWindowResponse lfwResponse = null;
        try {
            String fulfilmentApiUrl = getFulfillmentServiceApiUrl(listingId, buyerContactId);
            WebClient webClient = svcLocator.locate(fulfilmentApiUrl);
            webClient.accept(MediaType.APPLICATION_JSON);
            objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            SHMonitor mon = SHMonitorFactory.getMonitor();
            Response response;
            try {
                mon.start();
                response = webClient.get();
            } finally {
                mon.stop();
                log.info(SHMonitoringContext.get() + " _operation=getFulfillmentWindowsShapeForListing" + " _message= service call for listingId=" + listingId + " buyerContactId" + buyerContactId +
                        "  _respTime=" + mon.getTime());
            }

            if (Response.Status.OK.getStatusCode() == response.getStatus()) {
                log.info(
                        LOG_DOMAIN_RESOURCE_METHOD_PREFIX
                                + " _message=\"fulfillment api call successful\" responseStatus={}  listingId={}",
                        apiMethod, response.getStatus(), listingId);
                InputStream is = (InputStream) response.getEntity();
                lfwResponse = objectMapper.readValue(is, ListingFulfillmentWindowResponse.class);
            } else {
                log.error(
                        LOG_DOMAIN_RESOURCE_METHOD_PREFIX
                                + " status=success_with_error _message=\"fulfillment api call failed\" responseStatus={} listingId={}",
                        apiMethod, response.getStatus(), listingId);
            }

        } catch (Exception e) {
            log.error(
                    LOG_DOMAIN_RESOURCE_METHOD_PREFIX
                            + " status=success_with_error _message=\"fulfillment api call failed\" listingId={}",
                    apiMethod, listingId, e);
        }
        return lfwResponse;
    }

    private String getFulfillmentServiceApiUrl(Long listingId, Long buyerContactId) {
        final String apiMethod = "getFulfillmentServiceApiUrl";
        String fulfillmentApiUrl = null;
        if (buyerContactId != null) {
            log.info(
                    LOG_DOMAIN_RESOURCE_METHOD_PREFIX
                            + " message=\"passed in for\" BuyerContactId={} listing={}",
                    apiMethod, buyerContactId, listingId);
            fulfillmentApiUrl =
                    masterStubhubProperties.getProperty("fulfillment.listing.withbuyercontact.api.url", "");
            fulfillmentApiUrl = fulfillmentApiUrl.replace("{buyerContactId}", buyerContactId.toString());
        } else {
            log.info(LOG_DOMAIN_RESOURCE_METHOD_PREFIX
                    + " message=\"No buyerContactId passed in for\" listing={}", apiMethod, listingId);
            fulfillmentApiUrl = masterStubhubProperties.getProperty("fulfillment.listing.api.url", "");
        }
        fulfillmentApiUrl = fulfillmentApiUrl.replace("{listingId}", listingId.toString());
        log.info(
                LOG_DOMAIN_RESOURCE_METHOD_PREFIX
                        + " message=\"Url for the fulfillmentService call\" fulfillmentApiUrl={}",
                apiMethod, fulfillmentApiUrl);
        return fulfillmentApiUrl;
    }

    private List<FulfillmentWindow> processWindowResponse(FulfillmentWindowResponse[] windows) {
        List<FulfillmentWindow> fulfillmentWindows = new ArrayList<FulfillmentWindow>();
        if (windows != null && windows.length > 0) {
            for (FulfillmentWindowResponse window : windows) {
                FulfillmentWindow ffWindow = new FulfillmentWindow();

                ffWindow.setFulfillmentMethodId(window.getFulfillmentMethod().getId());
                ffWindow.setFulfillmentTypeName(window.getFulfillmentMethod().getFulfillmentTypeName());
                ffWindow.setFulfillmentMethodName(window.getFulfillmentMethod().getName());
                ffWindow.setDeliveryMethodId(window.getDeliveryMethod().getId());
                ffWindow.setStartTime(window.getStartTime());
                ffWindow.setEndTime(window.getEndTime());
                ffWindow.setBaseCost(window.getBaseCost().getAmount().doubleValue());
                ffWindow.setTicketMedium(window.getFulfillmentMethod().getTicketMedium());
                fulfillmentWindows.add(ffWindow);
            }
        }
        return fulfillmentWindows;
    }

    public List<FulfillmentWindow> getFulfillmentWindows(EventFulfillmentWindowResponse efwResponse) {
    	List<FulfillmentWindow> fulfillmentWindows = new ArrayList<>();
        if (efwResponse == null) {
            return fulfillmentWindows;
        }
        // EventFulfillmentWindowResponse.getFulfillmentWindows() returns collection object
        Collection<FulfillmentWindowResponse> fulfillmentWindowColl =
                efwResponse.getFulfillmentWindows();
        if (fulfillmentWindowColl == null)
            return fulfillmentWindows;
        FulfillmentWindowResponse[] fwResponse =
                fulfillmentWindowColl.toArray(new FulfillmentWindowResponse[0]);
        fulfillmentWindows = processWindowResponse(fwResponse);
        return fulfillmentWindows;
    }

    public List<Long> cloneFileInfo(final Long listingId, final Long stubTransId, List<CopyTicketSeat> copyTicketSeatList) {
        String apiMethod = "cloneFileInfo";
        String pdfCloneURL = masterStubhubProperties.getProperty(PDF_API_URL, FF_API_CLONE_URL_DEFAULT);
        final SHMonitor mon = SHMonitorFactory.getMonitor();
        final CloneAndLinkTicketResponse response;

        try {
            log.info(LOG_URI_PREFIX, apiMethod, pdfCloneURL);

            List<ResponseReader> repsonseReaderList = new ArrayList<ResponseReader>();
            repsonseReaderList.add(new ResponseReader(CloneAndLinkTicketResponse.class));

            WebClient webClient = svcLocator.locate(pdfCloneURL, repsonseReaderList);
            webClient.type(MediaType.APPLICATION_JSON);
            webClient.accept(MediaType.APPLICATION_JSON);

            final CloneAndLinkTicketRequest postBody = new CloneAndLinkTicketRequest();
            postBody.setListingId(listingId);
            postBody.setStubTransId(stubTransId);
            postBody.setCopyTicketSeatList(copyTicketSeatList);
            final ObjectMapper mapper = new ObjectMapper().configure(Feature.WRAP_ROOT_VALUE, true);
            final String jsonPostBody = mapper.writeValueAsString(postBody);

            Response rawResponse;
            try {
                mon.start();
                rawResponse = webClient.post(jsonPostBody);
            } finally {
                mon.stop();
                log.info(SHMonitoringContext.get() + " _operation=cloneFileInfo" + " _message= service call for listingId=" + listingId + " stubTransId" + stubTransId +
                        "  _respTime=" + mon.getTime());
            }

            if (Response.Status.OK.getStatusCode() == rawResponse.getStatus()
                    && null != ((CloneAndLinkTicketResponse) rawResponse.getEntity()).getFileInfoIdList()) {
                response = (CloneAndLinkTicketResponse) rawResponse.getEntity();
            } else {
                log.error(LOG_ERROR_PREFIX, apiMethod, new Long[]{listingId, stubTransId},
                        rawResponse.getStatus(), mon.getTime());
                throw new SHSystemException();
            }
        } catch (Exception ex) {
           log.error(LOG_ERROR_NO_MESSAGE_PREFIX, apiMethod, new Long[]{listingId, stubTransId},
                    mon.getTime(), ex);
            throw new SHSystemException(ex);
        }
        return response.getFileInfoIdList();
    }
}
