package com.stubhub.domain.inventory.listings.v2.enums;

public enum ConfirmOptionEnum {
	PRE_CONFIRM(1),AUTO_CONFIRM(2),MANUAL_CONFIRM(3);
	private int confirmStatus;

	public int getConfirmStatus() {
		return confirmStatus;
	}



	private ConfirmOptionEnum(int confirmStatus) {
		this.confirmStatus = confirmStatus;
	}
	
	

}
