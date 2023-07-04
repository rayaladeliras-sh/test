package com.stubhub.domain.inventory.biz.v2.impl;

import junit.framework.Assert;

import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.datamodel.dao.ExternalSystemUserDAO;
import com.stubhub.domain.inventory.datamodel.entity.ExternalSystemUser;

public class ExternalSystemUserMgrTest {
	
	private ExternalSystemUserMgrImpl externalSystemUserMgrImpl;
	
	private ExternalSystemUserDAO externalSystemUserDAO;
	
	@BeforeMethod
	public void setUp(){
		externalSystemUserMgrImpl = new ExternalSystemUserMgrImpl();
		externalSystemUserDAO = Mockito.mock(ExternalSystemUserDAO.class);
		ReflectionTestUtils.setField(externalSystemUserMgrImpl, "externalSystemUserDAO", externalSystemUserDAO);
	}
	
	@Test
	public void testGetExternalSystemUserByUserId(){
		Mockito.when(externalSystemUserDAO.getExternalSystemUserByUserId(Mockito.anyLong())).thenReturn(new ExternalSystemUser());
		ExternalSystemUser externalSystemUser = externalSystemUserMgrImpl.getExternalSystemUserByUserId(1234L);
		Assert.assertNotNull(externalSystemUser);
	}
	 
	@Test
	public void testGetExternalSystemUserByUserIdNull(){
		Mockito.when(externalSystemUserDAO.getExternalSystemUserByUserId(Mockito.anyLong())).thenReturn(new ExternalSystemUser());
		ExternalSystemUser externalSystemUser = externalSystemUserMgrImpl.getExternalSystemUserByUserId(null);
		Assert.assertNull(externalSystemUser);
	}
	

}
