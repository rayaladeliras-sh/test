package com.stubhub.domain.inventory.listings.v2.tns.dto;

public enum FraudCheckStatus {

	ACCEPTED(500l), REJECTED(1000l);

	private long id;

	private FraudCheckStatus(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public static FraudCheckStatus getById(long id) {
		for (FraudCheckStatus status : values()) {
			if (status.id == id) {
				return status;
			}
		}
		return null;
	}
	
	public String toString() {
		return Long.toString(id);
	}

}
