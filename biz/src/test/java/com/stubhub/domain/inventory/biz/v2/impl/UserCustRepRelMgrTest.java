package com.stubhub.domain.inventory.biz.v2.impl;

import static org.mockito.Mockito.mock;
import junit.framework.Assert;

import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.datamodel.dao.UserCustRepRelDAO;
import com.stubhub.domain.inventory.datamodel.entity.UserCustRepRel;

public class UserCustRepRelMgrTest {
	
	private UserCustRepRelMgrImpl userCustRepRelMgrImpl;
	private UserCustRepRelDAO userCustRepRelDAO;
	
	@BeforeMethod
	public void setUp(){
		userCustRepRelMgrImpl = new UserCustRepRelMgrImpl();
		userCustRepRelDAO = mock(UserCustRepRelDAO.class);
		ReflectionTestUtils.setField(userCustRepRelMgrImpl, "userCustRepRelDAO", userCustRepRelDAO);
	}

	@Test
	public void getByUserIdAndTypeTest(){
		Long userId = 123456L;
		Long type = 2L;
		UserCustRepRel userCustRepRel =  new UserCustRepRel();
		Mockito.when(userCustRepRelDAO.getByUserIdAndType(Mockito.anyLong(), Mockito.anyLong())).thenReturn(userCustRepRel);
		UserCustRepRel returned = userCustRepRelMgrImpl.getByUserIdAndType(userId, type);
		Assert.assertNotNull(returned);
	}
	
	@Test
	public void getByUserIdAndTypeTest_Null(){
		Long userId = 123456L;
		Long type = 2L;		
		Mockito.when(userCustRepRelDAO.getByUserIdAndType(Mockito.anyLong(), Mockito.anyLong())).thenReturn(null);
		UserCustRepRel returned = userCustRepRelMgrImpl.getByUserIdAndType(userId, type);
		Assert.assertNull(returned);
	}
	
	@Test
	public void getByUserIdAndTypeTest_NullInput(){
		Long userId = null;
		Long type = 2L;		
		UserCustRepRel returned = userCustRepRelMgrImpl.getByUserIdAndType(userId, type);
		Assert.assertNull(returned);
	}
}
