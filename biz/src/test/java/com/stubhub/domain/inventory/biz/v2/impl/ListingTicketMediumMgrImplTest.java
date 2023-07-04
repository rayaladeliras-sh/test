package com.stubhub.domain.inventory.biz.v2.impl;


import com.stubhub.domain.inventory.datamodel.dao.ListingTicketMediumDAO;
import com.stubhub.domain.inventory.datamodel.entity.ListingTicketMediumXref;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.times;

public class ListingTicketMediumMgrImplTest {

    @InjectMocks
    private ListingTicketMediumMgrImpl listingTicketMediumMgrImpl;


    @Mock
    private ListingTicketMediumDAO listingTicketMediumDAO;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddTicketMedium() {
        ListingTicketMediumXref ltmx = new ListingTicketMediumXref();
        listingTicketMediumMgrImpl.addTicketMedium(ltmx);
        Mockito.verify(listingTicketMediumDAO, times(1)).addTicketMedium(ltmx);
    }

}
