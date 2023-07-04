package com.stubhub.domain.inventory.biz.v2.impl;

import com.stubhub.domain.inventory.biz.v2.intf.ListingTicketMediumMgr;
import com.stubhub.domain.inventory.datamodel.dao.ListingTicketMediumDAO;
import com.stubhub.domain.inventory.datamodel.entity.ListingTicketMediumXref;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("listingTicketMediumMgr")
public class ListingTicketMediumMgrImpl implements ListingTicketMediumMgr {

  @Autowired
  private ListingTicketMediumDAO listingTicketMediumDAO;

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void addTicketMedium(ListingTicketMediumXref listingTicketMediumXref) {
    listingTicketMediumDAO.addTicketMedium(listingTicketMediumXref);
  }

}
