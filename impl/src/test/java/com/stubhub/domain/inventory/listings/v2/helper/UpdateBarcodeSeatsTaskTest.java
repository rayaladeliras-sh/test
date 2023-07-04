package com.stubhub.domain.inventory.listings.v2.helper;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.cxf.jaxrs.client.WebClient;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.domain.inventory.biz.v2.impl.InventoryMgrImpl;
import com.stubhub.domain.inventory.biz.v2.impl.TicketSeatMgrImpl;
import com.stubhub.domain.inventory.biz.v2.intf.InventoryMgr;
import com.stubhub.domain.inventory.biz.v2.intf.ListingSeatTraitMgr;
import com.stubhub.domain.inventory.biz.v2.intf.TicketSeatMgr;
import com.stubhub.domain.inventory.common.entity.TicketOperation;
import com.stubhub.domain.inventory.common.util.ErrorCode;
import com.stubhub.domain.inventory.common.util.ListingBusinessException;
import com.stubhub.domain.inventory.datamodel.entity.Listing;
import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;
import com.stubhub.domain.inventory.listings.v2.entity.SeatProduct;
import com.stubhub.domain.inventory.listings.v2.tasks.UpdateBarcodeSeatsTask;
import com.stubhub.platform.utilities.webservice.shcontext.SHAPIContext;
import com.stubhub.platform.utilities.webservice.svclocator.SvcLocator;

public class UpdateBarcodeSeatsTaskTest {
	
	private InventoryMgrImpl inventoryMgr;
	
	@BeforeMethod
	public void setUp(){
		inventoryMgr = mock(InventoryMgrImpl.class);
	}

}

