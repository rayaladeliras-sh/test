package com.stubhub.domain.inventory.v2.enums;

public enum ListingStatusEnum {
	ACTIVE("1"),
	INACTIVE("2"),
	DELETED("3"),
	HIDDEN("4"),
	PENDING_LOCK("5"),
	INCOMPLETE("6"),
	PENDING_PDF_REVIEW("7");
	
	private String code;
	
	private ListingStatusEnum(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	/**
	 * Returns ListingStatusEnum by listing system status code
	 * @param code
	 * @return ListingStatusEnum
	 */
	public static ListingStatusEnum getListingStatusEnumByCode(String code) {
		ListingStatusEnum[] values = values();

		for (ListingStatusEnum type : values) {
			if(type.getCode().equalsIgnoreCase(code)) {
				return type;
			}
		}
		return null;
	}
	
	/**
	 * Returns ListingStatusEnum based on given name
	 * @param option
	 * @return
	 */
	public static ListingStatusEnum getListingStatusEnumByName(String name) {
		ListingStatusEnum[] names = ListingStatusEnum.class.getEnumConstants();

		for (ListingStatusEnum type : names) {
			if(type.name().equalsIgnoreCase(name)) {
				return type;
			}
		}
		return null;
	}
}