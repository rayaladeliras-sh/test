package com.stubhub.domain.inventory.biz.v2.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.stubhub.domain.inventory.biz.v2.intf.UserCustRepRelMgr;
import com.stubhub.domain.inventory.datamodel.dao.UserCustRepRelDAO;
import com.stubhub.domain.inventory.datamodel.entity.UserCustRepRel;

@Component("userCustRepRelMgr")
public class UserCustRepRelMgrImpl implements UserCustRepRelMgr {

	@Autowired
	private UserCustRepRelDAO userCustRepRelDAO;
	
	@Override
	@Transactional
	public UserCustRepRel getByUserIdAndType(Long userId , Long type) {
		if(userId != null){
			 return userCustRepRelDAO.getByUserIdAndType(userId, type);
		}
		return null;
	} 

}
