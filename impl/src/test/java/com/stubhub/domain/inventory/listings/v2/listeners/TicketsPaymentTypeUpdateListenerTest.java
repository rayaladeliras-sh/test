package com.stubhub.domain.inventory.listings.v2.listeners;

import org.testng.annotations.Test;

import com.stubhub.domain.inventory.datamodel.dao.ListingDAO;
import com.stubhub.domain.inventory.datamodel.dao.SellerSpiConvertTrackingDAO;
import com.stubhub.domain.inventory.datamodel.entity.SellerSpiConvertTrackingDO;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

public class TicketsPaymentTypeUpdateListenerTest {

	
	@Mock
	ListingDAO listingDAO;
	
	@Mock
	SellerSpiConvertTrackingDAO sellerSpiConvertTrackingDAOImpl;
	
	TicketsPaymentTypeUpdateListener target;
	
	
	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		target = new TicketsPaymentTypeUpdateListener();
		ReflectionTestUtils.setField(target, "listingDAO", listingDAO);
		ReflectionTestUtils.setField(target, "sellerSpiConvertTrackingDAO", sellerSpiConvertTrackingDAOImpl);
	}
  
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testConvertTrackingIdIsNull() {
		Map<String, Long> messageMap = new HashMap<String, Long>();
		messageMap.put("convertTrackingId", null);
		target.onMessage(messageMap);
		Assert.fail("testConvertTrackingIdIsNull fails.");
	}
	
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testConvertTrackingIdIsLessThan0() {
		Map<String, Long> messageMap = new HashMap<String, Long>();
		messageMap.put("convertTrackingId", -1L);
		target.onMessage(messageMap);
		Assert.fail("testConvertTrackingIdIsNull fails.");
	}
	
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testSellerIdOfSellerSpiConvertTrackingDOIsNull()
	{
		Map<String, Long> messageMap = new HashMap<String, Long>();
		messageMap.put("convertTrackingId", 1L);
		messageMap.put("batchSize", 1L);
		
		SellerSpiConvertTrackingDO mockConvertTrackingDO = Mockito.mock(SellerSpiConvertTrackingDO.class);
		
	    when(mockConvertTrackingDO.getSellerId()).thenReturn(null);
		when(sellerSpiConvertTrackingDAOImpl.loadSellerSpiConvertTrackingDOById(Mockito.anyLong())).thenReturn(mockConvertTrackingDO);
		
		target.onMessage(messageMap);
		
		Assert.fail("testSellerIdOfSellerSpiConvertTrackingDOIsNull fails.");

	}
	
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testSellerIdOfSellerSpiConvertTrackingDOIsLowerThan0()
	{
		Map<String, Long> messageMap = new HashMap<String, Long>();
		messageMap.put("convertTrackingId", 1L);
		messageMap.put("batchSize", 1L);
		
		SellerSpiConvertTrackingDO mockConvertTrackingDO = Mockito.mock(SellerSpiConvertTrackingDO.class);
		
	    when(mockConvertTrackingDO.getSellerId()).thenReturn(-1L);
		when(sellerSpiConvertTrackingDAOImpl.loadSellerSpiConvertTrackingDOById(Mockito.anyLong())).thenReturn(mockConvertTrackingDO);
		
		target.onMessage(messageMap);
		
		Assert.fail("testSellerIdOfSellerSpiConvertTrackingDOIsNull fails.");

	}

	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testPaymentTypeOldIdOfSellerSpiConvertTrackingDOIsNull()
	{
		Map<String, Long> messageMap = new HashMap<String, Long>();
		messageMap.put("convertTrackingId", 1L);
		messageMap.put("batchSize", 1L);
		
		SellerSpiConvertTrackingDO mockConvertTrackingDO = Mockito.mock(SellerSpiConvertTrackingDO.class);
		
	    when(mockConvertTrackingDO.getSellerId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeOldId()).thenReturn(null);
		when(sellerSpiConvertTrackingDAOImpl.loadSellerSpiConvertTrackingDOById(Mockito.anyLong())).thenReturn(mockConvertTrackingDO);
		
		target.onMessage(messageMap);
		
		Assert.fail("testSellerIdOfSellerSpiConvertTrackingDOIsNull fails.");

	}
	
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testPaymentTypeOldIdOfSellerSpiConvertTrackingDOIsLowerThan0()
	{
		Map<String, Long> messageMap = new HashMap<String, Long>();
		messageMap.put("convertTrackingId", 1L);
		messageMap.put("batchSize", 1L);
		
		SellerSpiConvertTrackingDO mockConvertTrackingDO = Mockito.mock(SellerSpiConvertTrackingDO.class);
		
	    when(mockConvertTrackingDO.getSellerId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeOldId()).thenReturn(-1L);
		when(sellerSpiConvertTrackingDAOImpl.loadSellerSpiConvertTrackingDOById(Mockito.anyLong())).thenReturn(mockConvertTrackingDO);
		
		target.onMessage(messageMap);
		
		Assert.fail("testSellerIdOfSellerSpiConvertTrackingDOIsNull fails.");

	}
	
	@Test(expectedExceptions={IllegalArgumentException.class})
	public void testPaymentTypeNewIdOfSellerSpiConvertTrackingDOIsLowerThan0()
	{
		Map<String, Long> messageMap = new HashMap<String, Long>();
		messageMap.put("convertTrackingId", 1L);
		messageMap.put("batchSize", 1L);
		
		SellerSpiConvertTrackingDO mockConvertTrackingDO = Mockito.mock(SellerSpiConvertTrackingDO.class);
		
	    when(mockConvertTrackingDO.getSellerId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeOldId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeNewId()).thenReturn(-1L);
		when(sellerSpiConvertTrackingDAOImpl.loadSellerSpiConvertTrackingDOById(Mockito.anyLong())).thenReturn(mockConvertTrackingDO);
		
		target.onMessage(messageMap);
		
		Assert.fail("testSellerIdOfSellerSpiConvertTrackingDOIsNull fails.");

	}
	
	@Test
	public void testNullOrdersReturned()
	{
		Map<String, Long> messageMap = new HashMap<String, Long>();
		messageMap.put("convertTrackingId", 1L);
		messageMap.put("batchSize", 1L);
		
		SellerSpiConvertTrackingDO mockConvertTrackingDO = Mockito.mock(SellerSpiConvertTrackingDO.class);
		
	    when(mockConvertTrackingDO.getSellerId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeOldId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeNewId()).thenReturn(2L);
		when(sellerSpiConvertTrackingDAOImpl.loadSellerSpiConvertTrackingDOById(Mockito.anyLong())).thenReturn(mockConvertTrackingDO);
		when(listingDAO.getListingIdBySellerId(Mockito.anyLong(), Mockito.anyLong())).thenReturn(null);
		
		target.onMessage(messageMap);
		
		verify(sellerSpiConvertTrackingDAOImpl, times(1)).updateOrderPaymentTypeProcessStatus(Mockito.anyLong(), Mockito.anyLong());
	}
	
	@Test
	public void testEmptyListingReturned()
	{
		Map<String, Long> messageMap = new HashMap<String, Long>();
		messageMap.put("convertTrackingId", 1L);
		messageMap.put("batchSize", 10L);
		
		SellerSpiConvertTrackingDO mockConvertTrackingDO = Mockito.mock(SellerSpiConvertTrackingDO.class);
		
	    when(mockConvertTrackingDO.getSellerId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeOldId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeNewId()).thenReturn(2L);
		when(sellerSpiConvertTrackingDAOImpl.loadSellerSpiConvertTrackingDOById(Mockito.anyLong())).thenReturn(mockConvertTrackingDO);
		when(listingDAO.getListingIdBySellerId(Mockito.anyLong(), Mockito.anyLong())).thenReturn(new ArrayList<Long>());
		
		target.onMessage(messageMap);
		
		verify(sellerSpiConvertTrackingDAOImpl, times(1)).updateOrderPaymentTypeProcessStatus(Mockito.anyLong(), Mockito.anyLong());
	}
	
	
	private void populateIds(List<Long> orderIdsToBeUpdated, Long count)
	{
		for(Long i=0L;i<count;i++)
		{
			orderIdsToBeUpdated.add(i);
		}
	}
	
	@Test
	public void testExecuteWithNumberOfOrdersIs50()
	{
		Map<String, Long> messageMap = new HashMap<String, Long>();
		messageMap.put("convertTrackingId", 1L);
		messageMap.put("batchSize", 10L);
		
		SellerSpiConvertTrackingDO mockConvertTrackingDO = Mockito.mock(SellerSpiConvertTrackingDO.class);
		
	    when(mockConvertTrackingDO.getSellerId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeOldId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeNewId()).thenReturn(2L);
	    when(sellerSpiConvertTrackingDAOImpl.loadSellerSpiConvertTrackingDOById(Mockito.anyLong())).thenReturn(mockConvertTrackingDO);
	    
	    List<Long> listingIdsToBeUpdated = new ArrayList<Long>(50);
	    
	    populateIds(listingIdsToBeUpdated, 50L);
	    
	    when(listingDAO.getListingIdBySellerId(Mockito.anyLong(), Mockito.anyLong())).thenReturn(listingIdsToBeUpdated);
	    
	    when(listingDAO.updateListingPaymentTypeByListingIds(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyListOf(Long.class))).thenReturn(10);
	    
	    doNothing().when(sellerSpiConvertTrackingDAOImpl).persist(Mockito.any(SellerSpiConvertTrackingDO.class));
	    
	    target.onMessage(messageMap);
	    
	    verify(sellerSpiConvertTrackingDAOImpl, times(1)).loadSellerSpiConvertTrackingDOById(Mockito.anyLong());
	    verify(listingDAO, times(1)).getListingIdBySellerId(Mockito.anyLong(), Mockito.anyLong());
	    verify(listingDAO, times(5)).updateListingPaymentTypeByListingIds(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyListOf(Long.class));
	    verify(sellerSpiConvertTrackingDAOImpl, times(1)).updateOrderPaymentTypeProcessStatus(Mockito.anyLong(), Mockito.anyLong());
		
	}
	
	@Test
	public void testExecuteWithNumberOfOrdersIs50WithUpdateThrowException()
	{
		Map<String, Long> messageMap = new HashMap<String, Long>();
		messageMap.put("convertTrackingId", 1L);
		messageMap.put("batchSize", 10L);
		
		SellerSpiConvertTrackingDO mockConvertTrackingDO = Mockito.mock(SellerSpiConvertTrackingDO.class);
		
	    when(mockConvertTrackingDO.getSellerId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeOldId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeNewId()).thenReturn(2L);
	    when(sellerSpiConvertTrackingDAOImpl.loadSellerSpiConvertTrackingDOById(Mockito.anyLong())).thenReturn(mockConvertTrackingDO);
	    
	    List<Long> listingIdsToBeUpdated = new ArrayList<Long>(50);
	    
	    populateIds(listingIdsToBeUpdated, 50L);
	    
	    when(listingDAO.getListingIdBySellerId(Mockito.anyLong(), Mockito.anyLong())).thenReturn(listingIdsToBeUpdated);
	    
	    when(listingDAO.updateListingPaymentTypeByListingIds(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyListOf(Long.class))).thenThrow(RuntimeException.class);
	    
	    doNothing().when(sellerSpiConvertTrackingDAOImpl).persist(Mockito.any(SellerSpiConvertTrackingDO.class));
	    
	    target.onMessage(messageMap);
	    
	    verify(sellerSpiConvertTrackingDAOImpl, times(1)).loadSellerSpiConvertTrackingDOById(Mockito.anyLong());
	    verify(listingDAO, times(1)).getListingIdBySellerId(Mockito.anyLong(), Mockito.anyLong());
	    verify(listingDAO, times(5)).updateListingPaymentTypeByListingIds(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyListOf(Long.class));
	    verify(sellerSpiConvertTrackingDAOImpl, times(1)).updateOrderPaymentTypeProcessStatus(Mockito.anyLong(), Mockito.anyLong());
		
	}
	
	@Test
	public void testExecuteWithNumberOfOrdersIs58()
	{
		Map<String, Long> messageMap = new HashMap<String, Long>();
		messageMap.put("convertTrackingId", 1L);
		messageMap.put("batchSize", 10L);
		
		SellerSpiConvertTrackingDO mockConvertTrackingDO = Mockito.mock(SellerSpiConvertTrackingDO.class);
		
	    when(mockConvertTrackingDO.getSellerId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeOldId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeNewId()).thenReturn(2L);
	    when(sellerSpiConvertTrackingDAOImpl.loadSellerSpiConvertTrackingDOById(Mockito.anyLong())).thenReturn(mockConvertTrackingDO);
	    
	    List<Long> listingIdsToBeUpdated = new ArrayList<Long>(50);
	    
	    populateIds(listingIdsToBeUpdated, 58L);
	    
	    when(listingDAO.getListingIdBySellerId(Mockito.anyLong(), Mockito.anyLong())).thenReturn(listingIdsToBeUpdated);
	    
	    when(listingDAO.updateListingPaymentTypeByListingIds(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyListOf(Long.class))).thenReturn(10);
	    
	    doNothing().when(sellerSpiConvertTrackingDAOImpl).persist(Mockito.any(SellerSpiConvertTrackingDO.class));
	    
	    target.onMessage(messageMap);
	    
	    verify(sellerSpiConvertTrackingDAOImpl, times(1)).loadSellerSpiConvertTrackingDOById(Mockito.anyLong());
	    verify(listingDAO, times(1)).getListingIdBySellerId(Mockito.anyLong(), Mockito.anyLong());
	    verify(listingDAO, times(6)).updateListingPaymentTypeByListingIds(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyListOf(Long.class));
	    verify(sellerSpiConvertTrackingDAOImpl, times(1)).updateOrderPaymentTypeProcessStatus(Mockito.anyLong(), Mockito.anyLong());
		
	}
	
	@Test
	public void testExecuteWithBatchSizeIsNullNumberOfOrdersIs58()
	{
		Map<String, Long> messageMap = new HashMap<String, Long>();
		messageMap.put("convertTrackingId", 1L);
		messageMap.put("batchSize", null);
		
		SellerSpiConvertTrackingDO mockConvertTrackingDO = Mockito.mock(SellerSpiConvertTrackingDO.class);
		
	    when(mockConvertTrackingDO.getSellerId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeOldId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeNewId()).thenReturn(2L);
	    when(sellerSpiConvertTrackingDAOImpl.loadSellerSpiConvertTrackingDOById(Mockito.anyLong())).thenReturn(mockConvertTrackingDO);
	    
	    List<Long> listingIdsToBeUpdated = new ArrayList<Long>(50);
	    
	    populateIds(listingIdsToBeUpdated, 58L);
	    
	    when(listingDAO.getListingIdBySellerId(Mockito.anyLong(), Mockito.anyLong())).thenReturn(listingIdsToBeUpdated);
	    
	    when(listingDAO.updateListingPaymentTypeByListingIds(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyListOf(Long.class))).thenReturn(10);
	    
	    doNothing().when(sellerSpiConvertTrackingDAOImpl).persist(Mockito.any(SellerSpiConvertTrackingDO.class));
	    
	    target.onMessage(messageMap);
	    
	    verify(sellerSpiConvertTrackingDAOImpl, times(1)).loadSellerSpiConvertTrackingDOById(Mockito.anyLong());
	    verify(listingDAO, times(1)).getListingIdBySellerId(Mockito.anyLong(), Mockito.anyLong());
	    verify(listingDAO, times(6)).updateListingPaymentTypeByListingIds(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyListOf(Long.class));
	    verify(sellerSpiConvertTrackingDAOImpl, times(1)).updateOrderPaymentTypeProcessStatus(Mockito.anyLong(), Mockito.anyLong());
		
	}
	
	@Test
	public void testExecuteWithBatchSizeIsLessThen0NumberOfOrdersIs58()
	{
		Map<String, Long> messageMap = new HashMap<String, Long>();
		messageMap.put("convertTrackingId", 1L);
		messageMap.put("batchSize", -10L);
		
		SellerSpiConvertTrackingDO mockConvertTrackingDO = Mockito.mock(SellerSpiConvertTrackingDO.class);
		
	    when(mockConvertTrackingDO.getSellerId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeOldId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeNewId()).thenReturn(2L);
	    when(sellerSpiConvertTrackingDAOImpl.loadSellerSpiConvertTrackingDOById(Mockito.anyLong())).thenReturn(mockConvertTrackingDO);
	    
	    List<Long> listingIdsToBeUpdated = new ArrayList<Long>(58);
	    
	    populateIds(listingIdsToBeUpdated, 58L);
	    
	    List<Long> listingIds1 = listingIdsToBeUpdated.subList(0, 10);	    

	    List<Long> listingIds2 = listingIdsToBeUpdated.subList(10, 20);	    

	    List<Long> listingIds3 = listingIdsToBeUpdated.subList(20, 30);	    

	    List<Long> listingIds4 = listingIdsToBeUpdated.subList(30, 40);	    

	    List<Long> listingIds5 = listingIdsToBeUpdated.subList(40, 50);	    
	    
	    List<Long> listingIds6 = listingIdsToBeUpdated.subList(50, 58);	    

	    
	    when(listingDAO.getListingIdBySellerId(Mockito.anyLong(), Mockito.anyLong())).thenReturn(listingIdsToBeUpdated);
	    
	    when(listingDAO.updateListingPaymentTypeByListingIds(1L, 2L, listingIds1)).thenReturn(10);
	    when(listingDAO.updateListingPaymentTypeByListingIds(1L, 2L, listingIds2)).thenReturn(10);

	    when(listingDAO.updateListingPaymentTypeByListingIds(1L, 2L, listingIds3)).thenReturn(10);

	    when(listingDAO.updateListingPaymentTypeByListingIds(1L, 2L, listingIds4)).thenReturn(10);

	    when(listingDAO.updateListingPaymentTypeByListingIds(1L, 2L, listingIds5)).thenReturn(10);

	    when(listingDAO.updateListingPaymentTypeByListingIds(1L, 2L, listingIds6)).thenReturn(8);

	    
	    doNothing().when(sellerSpiConvertTrackingDAOImpl).persist(Mockito.any(SellerSpiConvertTrackingDO.class));
	    
	    target.onMessage(messageMap);
	    
	    verify(sellerSpiConvertTrackingDAOImpl, times(1)).loadSellerSpiConvertTrackingDOById(Mockito.anyLong());
	    verify(listingDAO, times(1)).getListingIdBySellerId(Mockito.anyLong(), Mockito.anyLong());
	    verify(listingDAO, times(6)).updateListingPaymentTypeByListingIds(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyListOf(Long.class));
	    verify(sellerSpiConvertTrackingDAOImpl, times(1)).updateOrderPaymentTypeProcessStatus(Mockito.anyLong(), Mockito.anyLong());
		
	}
	
	@Test
	public void testExecuteWithNumberOfOrdersIs58AndUpdateThrowException()
	{
		Map<String, Long> messageMap = new HashMap<String, Long>();
		messageMap.put("convertTrackingId", 1L);
		messageMap.put("batchSize", 10L);
		
		SellerSpiConvertTrackingDO mockConvertTrackingDO = Mockito.mock(SellerSpiConvertTrackingDO.class);
		
	    when(mockConvertTrackingDO.getSellerId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeOldId()).thenReturn(1L);
	    when(mockConvertTrackingDO.getSellerPaymentTypeNewId()).thenReturn(2L);
	    when(sellerSpiConvertTrackingDAOImpl.loadSellerSpiConvertTrackingDOById(Mockito.anyLong())).thenReturn(mockConvertTrackingDO);
	    
	    List<Long> listingIdsToBeUpdated = new ArrayList<Long>(50);
	    
	    populateIds(listingIdsToBeUpdated, 58L);
	    
	    when(listingDAO.getListingIdBySellerId(Mockito.anyLong(), Mockito.anyLong())).thenReturn(listingIdsToBeUpdated);
	    
	    when(listingDAO.updateListingPaymentTypeByListingIds(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyListOf(Long.class))).thenThrow(RuntimeException.class);
	    
	    doNothing().when(sellerSpiConvertTrackingDAOImpl).persist(Mockito.any(SellerSpiConvertTrackingDO.class));
	    
	    target.onMessage(messageMap);
	    
	    verify(sellerSpiConvertTrackingDAOImpl, times(1)).loadSellerSpiConvertTrackingDOById(Mockito.anyLong());
	    verify(listingDAO, times(1)).getListingIdBySellerId(Mockito.anyLong(), Mockito.anyLong());
	    verify(listingDAO, times(6)).updateListingPaymentTypeByListingIds(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyListOf(Long.class));
	    verify(sellerSpiConvertTrackingDAOImpl, times(1)).updateOrderPaymentTypeProcessStatus(Mockito.anyLong(), Mockito.anyLong());
		
	}
	
	


}
