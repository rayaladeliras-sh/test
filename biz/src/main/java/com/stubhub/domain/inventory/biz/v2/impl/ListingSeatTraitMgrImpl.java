package com.stubhub.domain.inventory.biz.v2.impl;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.stubhub.domain.catalog.read.v3.intf.seatTraits.dto.response.SeatTrait;
import com.stubhub.domain.catalog.read.v3.intf.seatTraits.dto.response.SeatTraits;
import com.stubhub.domain.infrastructure.common.core.context.SHMonitoringContext;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitor;
import com.stubhub.domain.infrastructure.common.core.monitor.SHMonitorFactory;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.datamodel.dao.ExternalSystemDAO;
import com.stubhub.domain.inventory.datamodel.dao.ListingSeatTraitDAO;
import com.stubhub.domain.inventory.datamodel.dao.SupplementSeatTraitDAO;
import com.stubhub.domain.inventory.datamodel.dao.VendorStubEventXrefDAO;
import com.stubhub.domain.inventory.datamodel.entity.ExternalSystem;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.SupplementSeatTrait;
import com.stubhub.domain.inventory.datamodel.entity.VendorStubEventXref;
import com.stubhub.newplatform.common.cache.Cacheable;
import com.stubhub.newplatform.property.MasterStubhubPropertiesWrapper;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

@Component("listingSeatTraitMgr")
public class ListingSeatTraitMgrImpl implements ListingSeatTraitMgr {

    @Autowired
    @Qualifier("inventoryExternalSystemDAO")
    private ExternalSystemDAO externalSystemDAO;

    @Autowired
    @Qualifier("inventoryVendorStubEventXrefDAO")
    private VendorStubEventXrefDAO vendorStubEventXrefDAO;

    @Autowired
    private ListingSeatTraitDAO listingSeatTraitDAO;

    @Autowired
    private SupplementSeatTraitDAO supplementSeatTraitDAO;

    @Autowired
    private MasterStubhubPropertiesWrapper masterStubhubProperties;

    @Autowired
    private SvcLocator svcLocator;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long PARKING_SUPPORTED = new Long("1");

    private final static Logger log = LoggerFactory.getLogger(ListingSeatTraitMgrImpl.class);

    @Override
    @Transactional
    public List<Long> getSeatTraitsFromComments(long eventId,
                                                String structuredComments) {
        return listingSeatTraitDAO
                .getSeatTraitsFromComments(eventId,
                        structuredComments);
    }


    @Override
    @Transactional
    public List<ListingSeatTrait> findSeatTraits(long ticketId) {
        return listingSeatTraitDAO.findSeatTraits(ticketId);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void addSeatTrait(ListingSeatTrait seatTrait) {
        listingSeatTraitDAO.addSeatTrait(seatTrait);
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteListingSeatTrait(ListingSeatTrait seatTrait) {
        listingSeatTraitDAO.deleteListingSeatTrait(seatTrait);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @Cacheable
    public SupplementSeatTrait getSupplementSeatTrait(Long supplementSeatTraitId) {
        return supplementSeatTraitDAO.findById(supplementSeatTraitId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @Cacheable
    public List<SupplementSeatTrait> getSupplementSeatTraitsForListing(Long listingId) {
        return supplementSeatTraitDAO.getSupplementSeatTraitsForListing(listingId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    @Cacheable
    public List<SupplementSeatTrait> getSupplementSeatTraitsBySeatTraitIds(List<Long> supplementSeatTraitIds) {
        return supplementSeatTraitDAO.getSupplementSeatTraitsBySeatTraitIds(supplementSeatTraitIds);
    }

    @Override
    @Transactional
    public boolean isParkingSupportedForEvent(Long eventId) {
        if (eventId != null) {
            VendorStubEventXref vendorStubEventXref = vendorStubEventXrefDAO.getByEventId(eventId);
            if (vendorStubEventXref != null && vendorStubEventXref.getExtSystemId() != null) {
                ExternalSystem extSystem = externalSystemDAO.findById(vendorStubEventXref.getExtSystemId());
                if (extSystem != null && PARKING_SUPPORTED.equals(extSystem.getParkingPassBarcodeInd())) {
                    return true;
                }

            }
        }
        return false;
    }

    @Override
    public List<Long> parseComments(Long eventId, String comments) {
        List<Long> seatTraitIds = new ArrayList<Long>();
        try {
            String commentsParserUrl = masterStubhubProperties.getProperty("catalog.commentsparser.api.url", "http://api-int.stubprod.com/catalog-read/v3/seatTraits/?eventId={eventId}&seatingComment={seatingComment}&source=sell");
            commentsParserUrl = commentsParserUrl.replace("{eventId}", eventId.toString());
            commentsParserUrl = commentsParserUrl.replace("{seatingComment}", URLEncoder.encode(comments, "UTF-8").replace("+", "%20"));

            objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            log.info("Calling commentsparser API for eventId=" + eventId + " url=" + commentsParserUrl);
            WebClient webClient = svcLocator.locate(commentsParserUrl);
            webClient.accept(MediaType.APPLICATION_JSON);

            SHMonitor mon = SHMonitorFactory.getMonitor();
            Response response = null;
            try {
                mon.start();
                response = webClient.get();
            } finally {
                mon.stop();
                log.info(SHMonitoringContext.get() + " _operation=parseComments" + " _message= service call for eventId=" + eventId + "  _respTime=" + mon.getTime());
            }

            if (response != null && Response.Status.OK.getStatusCode() == response.getStatus()) {
                log.info("_message=\"comments parser api call successful\" eventId={}", eventId);
                InputStream is = (InputStream) response.getEntity();
                SeatTraits seatTraitsResponse = objectMapper.readValue(is, SeatTraits.class);
                if (seatTraitsResponse != null) {
                    List<SeatTrait> seatTraits = seatTraitsResponse.getSeatTraits();
                    if (seatTraits != null) {
                        for (SeatTrait seatTrait : seatTraits) {
                            seatTraitIds.add(seatTrait.getId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error occured while calling comment parser for eventId=" + eventId);
        }
        return seatTraitIds;
    }

}
