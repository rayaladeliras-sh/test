package com.stubhub.domain.inventory.biz.v2.intf;

import java.util.List;

import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;



public interface TicketSeatMgr {

  public List<TicketSeat> findAllTicketSeatsByTicketId(long ticketId);

  public List<TicketSeat> findTicketSeatsByTicketId(long ticketId);

  public List<TicketSeat> findActiveTicketSeatsByTicketId(long ticketId);

  public void addTicketSeat(TicketSeat seat);

  public void updateTicketSeat(TicketSeat seat);

  public void deleteTicketSeat(TicketSeat seat);
  
  public List<Long> findActiveTicketSeatsByOriginalSeatIds(List<Long> ticketSeatIds);
  
  public List<TicketSeat> findActiveTicketSeatsOnlyByTicketId(long ticketId);
  
  public List<TicketSeat> findTicketSeatByTicketSeatId(Long ticketId, Long ticketSeatId);

}
