package com.stubhub.domain.inventory.biz.v2.intf;

import java.util.List;

import com.stubhub.domain.inventory.datamodel.entity.PTVTicket;

public interface PTVTicketMgr {
    public List<PTVTicket> findPTVTickets(long ticketId);

    public PTVTicket findPTVTicketsBySeatId(long ticketId, long ticketSeatId);

}
