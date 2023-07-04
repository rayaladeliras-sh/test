package com.stubhub.domain.inventory.biz.v2.intf;

import com.stubhub.domain.inventory.datamodel.entity.UserCustRepRel;

public interface UserCustRepRelMgr {
	
	public UserCustRepRel getByUserIdAndType(Long userId , Long type);

}
