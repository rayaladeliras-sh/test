package com.stubhub.domain.inventory.biz.v2.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.datamodel.dao.TicketSeatDAO;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;

@Component("ticketSeatMgr")
public class TicketSeatMgrImpl implements TicketSeatMgr {

  @Autowired
  private TicketSeatDAO ticketSeatDAO;

  @Override
  @Transactional
  public List<TicketSeat> findAllTicketSeatsByTicketId(long ticketId) {
    return ticketSeatDAO.findAllByTicketId(ticketId);
  }

  @Override
  @Transactional
  public List<TicketSeat> findTicketSeatsByTicketId(long ticketId) {
    return ticketSeatDAO.findByTicketId(ticketId);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void addTicketSeat(TicketSeat seat) {
    ticketSeatDAO.addTicketSeat(seat);

  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void updateTicketSeat(TicketSeat seat) {
    ticketSeatDAO.updateTicketSeat(seat);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void deleteTicketSeat(TicketSeat seat) {
    ticketSeatDAO.deleteTicketSeat(seat);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr#findActiveTicketSeatsByTicketId(long)
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public List<TicketSeat> findActiveTicketSeatsByTicketId(long ticketId) {
    return ticketSeatDAO.findActiveSeatsByTicketId(ticketId);
  }
  
  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public List<Long> findActiveTicketSeatsByOriginalSeatIds(List<Long> ticketSeatIds)
  {
	return ticketSeatDAO.findActiveTicketSeatsByOriginalSeatIds(ticketSeatIds);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public List<TicketSeat> findActiveTicketSeatsOnlyByTicketId(long ticketId) {
    return ticketSeatDAO.findActiveSeatsOnlyByTicketId(ticketId);
  }

	@Override
	@Transactional
	public List<TicketSeat> findTicketSeatByTicketSeatId(Long ticketId, Long ticketSeatId) {
		return ticketSeatDAO.findTicketSeatByTicketSeatId(ticketId, ticketSeatId);
	}

}
