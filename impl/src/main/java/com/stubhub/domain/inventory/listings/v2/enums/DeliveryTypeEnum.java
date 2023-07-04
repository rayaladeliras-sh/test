package com.stubhub.domain.inventory.listings.v2.enums;

public enum DeliveryTypeEnum {
	Electronic("Electronic"),
	ElectronicInstantDownload("Electronic Instant Download"),
	Pickup("Pickup"),
	UPS("UPS");

	private String name;

	DeliveryTypeEnum(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static DeliveryTypeEnum getDeliveryTypeEnumByName(String name) {
		DeliveryTypeEnum list[] = DeliveryTypeEnum.class.getEnumConstants();
		for (int i=0; i<list.length; i++) {
			DeliveryTypeEnum obj = list[i];
			if (obj.getName().equalsIgnoreCase(name)) {
				return obj;
			}
		}

		return null;
	}
}
