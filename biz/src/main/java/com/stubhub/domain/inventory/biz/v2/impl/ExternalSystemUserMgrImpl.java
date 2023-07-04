package com.stubhub.domain.inventory.biz.v2.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.stubhub.domain.inventory.biz.v2.intf.ExternalSystemUserMgr;
import com.stubhub.domain.inventory.datamodel.dao.ExternalSystemUserDAO;
import com.stubhub.domain.inventory.datamodel.entity.ExternalSystemUser;

@Component("externalSystemUserMgr")
public class ExternalSystemUserMgrImpl implements ExternalSystemUserMgr {

	@Autowired
	private ExternalSystemUserDAO externalSystemUserDAO;
	
	@Override
	@Transactional
	public ExternalSystemUser getExternalSystemUserByUserId(Long userId) {
		if(userId != null){
			return externalSystemUserDAO.getExternalSystemUserByUserId(userId);
		}
		return null;
	}
}
