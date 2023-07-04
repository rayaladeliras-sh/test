package com.stubhub.domain.inventory.listings.v2.adapter;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

import com.stubhub.domain.inventory.common.util.CommonConstants;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TicketSeatComparator implements Comparator<TicketSeat> {

    private final static Logger log = LoggerFactory.getLogger(TicketSeatComparator.class);

    @Override
    public int compare(TicketSeat ts1, TicketSeat ts2) {
        if (StringUtils.trimToNull(ts1.getSeatNumber()) == null && StringUtils.trimToNull(ts2.getSeatNumber()) == null) {
            return ts1.getTicketSeatId().compareTo(ts2.getTicketSeatId());
        }
        if (CommonConstants.GA.equalsIgnoreCase(ts1.getSeatNumber()) && CommonConstants.GA.equalsIgnoreCase(ts2.getSeatNumber())) {
            return ts1.getTicketSeatId().compareTo(ts2.getTicketSeatId());
        }
        if (StringUtils.trimToNull(ts1.getSeatNumber()) != null && StringUtils.trimToNull(ts2.getSeatNumber()) != null
                && ts1.getSeatNumber().equalsIgnoreCase(ts2.getSeatNumber())) {
            return ts1.getTicketSeatId().compareTo(ts2.getTicketSeatId());
        }
        if (CommonConstants.PARKING_PASS.equalsIgnoreCase(ts1.getSeatNumber())) {
            return 1;
        }
        if (CommonConstants.PARKING_PASS.equalsIgnoreCase(ts2.getSeatNumber())) {
            return -1;
        }
        if (StringUtils.trimToNull(ts1.getSeatNumber()) == null || CommonConstants.GA.equalsIgnoreCase(ts1.getSeatNumber())) {
            return 1;
        }
        if (StringUtils.trimToNull(ts2.getSeatNumber()) == null || CommonConstants.GA.equalsIgnoreCase(ts2.getSeatNumber())) {
            return -1;
        }
        if (StringUtils.trimToNull(ts1.getSeatNumber()) != null && StringUtils.trimToNull(ts2.getSeatNumber()) != null) {
            if (StringUtils.isNumeric(ts1.getSeatNumber()) && StringUtils.isNumeric(ts2.getSeatNumber())) {
                try {
                    return Long.valueOf(ts1.getSeatNumber()).compareTo(Long.valueOf(ts2.getSeatNumber()));
                } catch (NumberFormatException nfe) {
                    log.warn(nfe.getMessage(), "SeatNumber value is greater than java.lang.Long MAX_VALUE {}. Still continuing  with alternate.", Long.MAX_VALUE);
                    return ts1.getTicketSeatId().compareTo(ts2.getTicketSeatId());
                }
            } else {
                return ts1.getTicketSeatId().compareTo(ts2.getTicketSeatId());
            }
        }
        return 0;
    }

}
