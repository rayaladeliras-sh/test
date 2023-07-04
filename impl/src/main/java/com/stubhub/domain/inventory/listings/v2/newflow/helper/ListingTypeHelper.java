package com.stubhub.domain.inventory.listings.v2.newflow.helper;

import org.springframework.stereotype.Component;

import com.stubhub.domain.inventory.listings.v2.newflow.dto.ListingType;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.OperationTypeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.SizeTypeEnum;

@Component("listingTypeHelper")
public class ListingTypeHelper {

	public ListingType getCreateListingType() {
		return getListingType(true, false);
	}

	public ListingType getUpdateListingType() {
		return getListingType(false, false);
	}

	public ListingType getListingType(boolean isCreate, boolean isBulk) {
		ListingType listingType = new ListingType();
		listingType.setOperationType(isCreate?OperationTypeEnum.CREATE:OperationTypeEnum.UPDATE);
		listingType.setSizeType(isBulk?SizeTypeEnum.BULK:SizeTypeEnum.SINGLE);
		
		return listingType;
	}
}
