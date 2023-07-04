package com.stubhub.domain.inventory.listings.v2.newflow.dto;

import com.stubhub.domain.inventory.listings.v2.newflow.enums.OperationTypeEnum;
import com.stubhub.domain.inventory.listings.v2.newflow.enums.SizeTypeEnum;

public class ListingType {
	protected OperationTypeEnum operationType;
	protected SizeTypeEnum sizeType;

	public OperationTypeEnum getOperationType() {
		return operationType;
	}

	public void setOperationType(OperationTypeEnum operationType) {
		this.operationType = operationType;
	}

	public SizeTypeEnum getSizeType() {
		return sizeType;
	}

	public void setSizeType(SizeTypeEnum sizeType) {
		this.sizeType = sizeType;
	}
}
