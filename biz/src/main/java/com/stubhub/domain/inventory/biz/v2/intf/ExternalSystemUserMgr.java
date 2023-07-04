package com.stubhub.domain.inventory.biz.v2.intf;

import com.stubhub.domain.inventory.datamodel.entity.ExternalSystemUser;

public interface ExternalSystemUserMgr {
	
	public ExternalSystemUser getExternalSystemUserByUserId(Long userId);

}
