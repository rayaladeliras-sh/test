package com.stubhub.domain.inventory.listings.v2.newflow.task;

import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.listings.v2.newflow.task.UpdateQuantityTask;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;
import org.junit.Assert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;


public class UpdateQuantityTaskTest {


    @Mock
    private TicketSeatMgr ticketSeatMgr;
    @Mock
    private ListingDTO listingDTO;
    @InjectMocks
    UpdateQuantityTask updateQuantityTask = new UpdateQuantityTask(listingDTO);

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecuteWithNonGATicket() throws Exception {
        Listing ls = TestUtil.getDBListing();
        ls.setSection("General Admission");
        ls.setQuantity(1);
        ls.setQuantityRemain(1);
        when(listingDTO.getDbListing()).thenReturn(ls);
        ListingRequest lr = new ListingRequest();
        lr.setQuantity(3);
        when(listingDTO.getListingRequest()).thenReturn(lr);
        try {
            updateQuantityTask.call();
        } catch (ListingException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(e.getErrorCodeEnum().getDescription(), "Cannot increase quantity to add seats for non GA listing");
        }
        verify(listingDTO, atLeast(1)).getDbListing();
        verify(listingDTO, atLeast(1)).getListingRequest();

    }

    @Test
    public void testExecuteIncreaseGATicketSuccess() throws Exception {
        Listing ls = TestUtil.getDBListing();
        ls.setSection("General Admission");
        ls.setQuantity(1);
        ls.setQuantityRemain(1);
        when(listingDTO.getDbListing()).thenReturn(ls);
        ListingRequest lr = new ListingRequest();
        lr.setQuantity(3);
        when(listingDTO.getListingRequest()).thenReturn(lr);
        List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
        TicketSeat ts = new TicketSeat();
        ts.setSection("General Admission");
        ticketSeats.add(ts);
        when(ticketSeatMgr.findActiveTicketSeatsByTicketId(anyLong())).thenReturn(ticketSeats);
        updateQuantityTask.call();
        Assert.assertEquals((Integer) 3, ls.getQuantity());
        verify(listingDTO, atLeast(1)).getDbListing();
        verify(listingDTO, atLeast(1)).getListingRequest();
    }

    @Test
    public void testExecuteDecreaseGATicketSuccess() throws Exception {
        Listing ls = TestUtil.getDBListing();
        ls.setSection("General Admission");
        ls.setSeats("1,2,3");
        ls.setQuantity(3);
        ls.setQuantityRemain(3);
        when(listingDTO.getDbListing()).thenReturn(ls);
        ListingRequest lr = new ListingRequest();
        lr.setQuantity(1);
        when(listingDTO.getListingRequest()).thenReturn(lr);
        List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
        TicketSeat ts1 = new TicketSeat();
        ts1.setSection("General Admission");
        ts1.setSeatNumber("1");
        ts1.setSeatStatusId(1L);
        ts1.setTixListTypeId(1L);
        TicketSeat ts2 = new TicketSeat();
        ts2.setSection("General Admission");
        ts2.setSeatNumber("2");
        ts2.setSeatStatusId(1L);
        ts2.setTixListTypeId(1L);
        TicketSeat ts3 = new TicketSeat();
        ts3.setSection("General Admission");
        ts3.setSeatNumber("3");
        ts3.setSeatStatusId(1L);
        ts3.setTixListTypeId(1L);
        ticketSeats.add(ts1);
        ticketSeats.add(ts2);
        ticketSeats.add(ts3);
        when(ticketSeatMgr.findActiveTicketSeatsOnlyByTicketId(anyLong())).thenReturn(ticketSeats);
        updateQuantityTask.call();
        Assert.assertEquals((Integer) 1, ls.getQuantity());
        List<TicketSeat> seats = ls.getTicketSeats();
        Assert.assertEquals((Long) 4L, seats.get(2).getSeatStatusId());
        Assert.assertEquals((Long) 4L, seats.get(1).getSeatStatusId());
        verify(listingDTO, atLeast(1)).getDbListing();
        verify(listingDTO).getListingRequest();
    }
    //TODO check status

    @Test
    public void testExecuteDecreaseGATicketSuccessNoMatchSeatNumber() throws Exception {
        Listing ls = TestUtil.getDBListing();
        ls.setSection("General Admission");
        ls.setSeats("1,2,3");
        ls.setQuantity(3);
        ls.setQuantityRemain(3);
        when(listingDTO.getDbListing()).thenReturn(ls);
        ListingRequest lr = new ListingRequest();
        lr.setQuantity(1);
        when(listingDTO.getListingRequest()).thenReturn(lr);
        List<TicketSeat> ticketSeats = new ArrayList<TicketSeat>();
        TicketSeat ts1 = new TicketSeat();
        ts1.setSection("General Admission");
        ts1.setSeatNumber("1");
        ticketSeats.add(ts1);
        ticketSeats.add(ts1);
        ticketSeats.add(ts1);
        when(ticketSeatMgr.findActiveTicketSeatsOnlyByTicketId(anyLong())).thenReturn(ticketSeats);
        updateQuantityTask.call();
        Assert.assertEquals((Integer) 1, ls.getQuantity());
        Assert.assertEquals((Integer) 1, ls.getQuantity());
        List<TicketSeat> seats = ls.getTicketSeats();
        Assert.assertEquals((Long) 4L, seats.get(2).getSeatStatusId());
        Assert.assertEquals((Long) 4L, seats.get(1).getSeatStatusId());
        verify(listingDTO, atLeast(1)).getDbListing();
        verify(listingDTO, atLeast(1)).getListingRequest();
    }
    
    @Test
    public void testExecuteWithPiggyback() throws Exception {
      Listing ls = TestUtil.getDBListing();
      ls.setSection("General Admission");
      ls.setQuantity(4);
      ls.setQuantityRemain(4);
      ls.setIsPiggyBack(true);
      when(listingDTO.getDbListing()).thenReturn(ls);
      ListingRequest lr = new ListingRequest();
      lr.setQuantity(1);
      when(listingDTO.getListingRequest()).thenReturn(lr);
      try {
          updateQuantityTask.call();
      } catch (ListingException e) {
          Assert.assertNotNull(e);
          Assert.assertEquals(e.getErrorCodeEnum(), ErrorCodeEnum.invalidPiggybackRow);
      }
    }
}
