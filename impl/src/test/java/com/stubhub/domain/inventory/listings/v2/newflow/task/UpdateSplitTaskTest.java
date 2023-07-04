package com.stubhub.domain.inventory.listings.v2.newflow.task;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.common.exception.ErrorType;
import com.stubhub.domain.inventory.common.entity.SplitOption;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.ListingSeatTrait;
import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingDTO;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ErrorCodeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.exception.ListingException;
import com.stubhub.domain.inventory.v2.DTO.ListingRequest;

public class UpdateSplitTaskTest {

    @Mock
    private ListingDTO listingDTO;

    @InjectMocks
    private UpdateSplitTask updateSplitTask=new UpdateSplitTask(listingDTO);

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecuteNoneSplitOptionSuccess(){
        Listing listing = TestUtil.getDBListing();
        listing.setQuantity(3);
        listing.setQuantityRemain(3);
        listing.setSection("General Admission");
        when(listingDTO.getDbListing()).thenReturn(listing);
        ListingRequest listingRequest = new ListingRequest();
        listingRequest.setSplitOption(SplitOption.NONE);
        when(listingDTO.getListingRequest()).thenReturn(listingRequest);
        updateSplitTask.call();

        Assert.assertEquals(listing.getSplitOption(),new Short("0"));
        Assert.assertEquals(listing.getSplitQuantity(),(Integer)3);
    }

    @Test
    public void testExecuteMultiplesSplitOptionSuccess(){
        Listing listing = TestUtil.getDBListing();
        listing.setQuantity(3);
        listing.setQuantityRemain(3);
        listing.setSection("General Admission");
        when(listingDTO.getDbListing()).thenReturn(listing);
        ListingRequest listingRequest = new ListingRequest();
        listingRequest.setSplitQuantity(2);
        listingRequest.setSplitOption(SplitOption.MULTIPLES);
        when(listingDTO.getListingRequest()).thenReturn(listingRequest);
        updateSplitTask.call();

        Assert.assertEquals(listing.getSplitOption(),new Short("1"));
        Assert.assertEquals(listing.getSplitQuantity(),(Integer)2);
    }

    @Test
    public void testExecuteNosinglesSplitOptionSuccess(){
        Listing listing = TestUtil.getDBListing();
        listing.setQuantity(3);
        listing.setQuantityRemain(3);
        listing.setSection("General Admission");
        when(listingDTO.getDbListing()).thenReturn(listing);
        ListingRequest listingRequest = new ListingRequest();
        listingRequest.setSplitOption(SplitOption.NOSINGLES);
        when(listingDTO.getListingRequest()).thenReturn(listingRequest);
        updateSplitTask.call();

        Assert.assertEquals(listing.getSplitOption(),new Short("2"));
        Assert.assertEquals(listing.getSplitQuantity(),(Integer)1);
    }

    @Test
    public void testExecuteNoSplitOptionSuccess(){
        Listing listing = TestUtil.getDBListing();
        listing.setQuantity(3);
        listing.setQuantityRemain(3);
        listing.setSplitOption(new Short("1"));
        listing.setSection("General Admission");
        when(listingDTO.getDbListing()).thenReturn(listing);
        ListingRequest listingRequest = new ListingRequest();
        listingRequest.setSplitQuantity(1);
        when(listingDTO.getListingRequest()).thenReturn(listingRequest);
        updateSplitTask.call();

        Assert.assertEquals(listing.getSplitOption(),new Short("1"));
        Assert.assertEquals(listing.getSplitQuantity(),(Integer)1);
    }

    @Test
    public void testExecuteNoSplitOptionFailOnInvalidSplitValue(){
        Listing listing = TestUtil.getDBListing();
        listing.setQuantity(3);
        listing.setQuantityRemain(3);
        listing.setSplitOption(new Short("1"));
        listing.setSplitQuantity(4);
        listing.setSection("General Admission");
        when(listingDTO.getDbListing()).thenReturn(listing);
        ListingRequest listingRequest = new ListingRequest();
        when(listingDTO.getListingRequest()).thenReturn(listingRequest);
        try {
            updateSplitTask.call();
        }catch (ListingException e){
            Assert.assertEquals(e.getType(), ErrorType.INPUTERROR);
            Assert.assertEquals(e.getErrorCodeEnum(), ErrorCodeEnum.invalidSplitValue);
        }
    }
    
    @Test
    public void testExecuteNoSplitOptionPiggyback(){
        Listing listing = TestUtil.getDBListing();
        listing.setQuantity(4);
        listing.setQuantityRemain(4);
        listing.setSplitOption(new Short("1"));
        listing.setSplitQuantity(3);
        listing.setSection("Orchestra Center");
        listing.setRow("A,B");
        List<ListingSeatTrait> seatTraits = new ArrayList<ListingSeatTrait>();
        ListingSeatTrait seatTrait = new ListingSeatTrait();
        seatTrait.setSupplementSeatTraitId(101L);
        seatTraits.add(seatTrait);
        listing.setSeatTraits(seatTraits);
        when(listingDTO.getDbListing()).thenReturn(listing);
        ListingRequest listingRequest = new ListingRequest();
        when(listingDTO.getListingRequest()).thenReturn(listingRequest);
        try {
          ListingDTO dto =   updateSplitTask.call();
          Assert.assertNotNull(dto);
        }catch (ListingException e){
            Assert.fail("should not reach here");
        }
    }
    
    @Test
    public void testExecuteResetSplitTrue() {
      Listing listing = TestUtil.getDBListing();
      ListingSeatTrait pp = new ListingSeatTrait();
      pp.setSupplementSeatTraitId(102L);
      listing.getSeatTraits().add(pp);
      
      when(listingDTO.getDbListing()).thenReturn(listing);
      ListingRequest listingRequest = new ListingRequest();
      listingRequest.setSplitOption(SplitOption.NOSINGLES);
      when(listingDTO.getListingRequest()).thenReturn(listingRequest);
      try {
        ListingDTO dto =   updateSplitTask.call();
        Assert.assertNotNull(dto);
        Assert.assertEquals(listing.getSplitOption(), new Short((short) 0));
      }catch (ListingException e){
          Assert.fail("should not reach here");
      }
      
    }
    
    @Test
    public void testExecuteResetSplitFalse() {
      Listing listing = TestUtil.getDBListing();
      ListingSeatTrait pp = new ListingSeatTrait();
      pp.setSupplementSeatTraitId(102L);
      pp.setMarkForDelete(true);
      listing.getSeatTraits().add(pp);
      
      when(listingDTO.getDbListing()).thenReturn(listing);
      ListingRequest listingRequest = new ListingRequest();
      listingRequest.setSplitOption(SplitOption.NOSINGLES);
      when(listingDTO.getListingRequest()).thenReturn(listingRequest);
      try {
        ListingDTO dto =   updateSplitTask.call();
        Assert.assertNotNull(dto);
        Assert.assertEquals(listing.getSplitOption(), new Short((short) 2));
      }catch (ListingException e){
          Assert.fail("should not reach here");
      }
      
    }
    
    
}
