package com.stubhub.domain.inventory.biz.v2.impl;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.Assert;

import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.datamodel.dao.SpiDefaultDAO;
import com.stubhub.domain.inventory.datamodel.entity.SpiDefault;

public class SpiDefaultMgrTest {

	private SpiDefaultMgrImpl spiDefaultMgrImpl;
	
	private SpiDefaultDAO spiDefaultDAO;
	
	@BeforeMethod
	public void setUp(){
		spiDefaultMgrImpl = new SpiDefaultMgrImpl();
		spiDefaultDAO = mock(SpiDefaultDAO.class);
		ReflectionTestUtils.setField(spiDefaultMgrImpl, "spiDefaultDAO", spiDefaultDAO);
	}
	
	@Test
	public void getDefaultSpiBySellerIdBobIdTest(){
		SpiDefault spiDefault =  new SpiDefault();
		
		when(spiDefaultDAO.getDefaultSpiBySellerIdBobId(anyLong(), anyLong())).thenReturn(spiDefault);
		SpiDefault spiDefault2 = spiDefaultMgrImpl.getDefaultSpiBySellerIdBobId(12345L, 1L);
		Assert.assertNotNull(spiDefault2);
	}
}
