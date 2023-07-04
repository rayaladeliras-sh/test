package com.stubhub.domain.inventory.v2.enums;

public enum ProductMediumEnum {
	
	PAPER(1),
	PDF(2),
	BARCODE(3),
	FLASHSEAT(4),
	SEASONCARD(5),
	EVENTCARD(6),
	EXTMOBILE(7),
	EXTFLASH(8),
	MOBILE(9),
	WRISTBAND(10),
	RFID(11),
	GUESTLIST(12);
	

	Integer code;

	ProductMediumEnum(Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

	/**
	 * Returns ProductMediumEnum by given code
	 * @param code
	 * @return
	 */
	public static ProductMediumEnum getProductMediumEnumByCode(Integer code) {
		ProductMediumEnum list[] = ProductMediumEnum.class.getEnumConstants();
		for (ProductMediumEnum obj : list) {
			if (obj.getCode().equals(code)) {
				return obj;
			}
		}
		return null;
	}
	
	/**
	 * Returns ProductMediumEnum by given name
	 * @param name
	 * @return
	 */
	public static ProductMediumEnum getProductMediumEnumByName(String name) {
		ProductMediumEnum[] names = ProductMediumEnum.class.getEnumConstants();

		for (ProductMediumEnum type : names) {
			if(type.name().equalsIgnoreCase(name)) {
				return type;
			}
		}
		return null;
	}
	
}
