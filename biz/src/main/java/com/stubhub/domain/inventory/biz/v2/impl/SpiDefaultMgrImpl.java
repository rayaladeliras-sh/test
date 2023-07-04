package com.stubhub.domain.inventory.biz.v2.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.stubhub.domain.inventory.biz.v2.intf.SpiDefaultMgr; 
import com.stubhub.domain.inventory.datamodel.dao.SpiDefaultDAO;
import com.stubhub.domain.inventory.datamodel.entity.SpiDefault;

@Component("spiDefaultMgr")
public class SpiDefaultMgrImpl implements SpiDefaultMgr {
	@Autowired
	private SpiDefaultDAO spiDefaultDAO;

	@Override
	@Transactional
	public SpiDefault getDefaultSpiBySellerIdBobId(long sellerId, long bobId) {
		return spiDefaultDAO.getDefaultSpiBySellerIdBobId(sellerId, bobId);
	}
}
