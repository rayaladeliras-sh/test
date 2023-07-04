package com.stubhub.domain.inventory.biz.v2.intf;

import com.stubhub.domain.inventory.datamodel.entity.SpiDefault;

public interface SpiDefaultMgr {
	public SpiDefault getDefaultSpiBySellerIdBobId(long sellerId, long bobId);
}
 