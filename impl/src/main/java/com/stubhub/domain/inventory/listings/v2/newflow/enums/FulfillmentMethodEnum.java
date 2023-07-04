package com.stubhub.domain.inventory.listings.v2.newflow.enums;

public enum FulfillmentMethodEnum {
	BarcodePreDeliverySTH(1L, "Barcode - PreDelivery (STH)"),
	BarcodePreDeliveryNonSTH(2L, "Barcode - PreDelivery (Non-STH)"),
	Barcode(3L, "Barcode"),
	PDFPreDelivery(4L, "PDF - PreDelivery"),
	PDF(5L, "PDF"),
	FedEx(6L, "FedEx"),
	LMS(7L, "LMS"),
	OtherPreDelivery(8L, "Other - PreDelivery"),
	LMSPreDelivery(9L, "LMS - PreDelivery"),
	UPS(10L, "UPS"),
	RoyalMail(11L, "Royal Mail"),
	DeutschePost(12L, "Deutsche Post"),
	FlashSeatPreDelivery(13L, "FlashSeat - PreDelivery"),
	FlashSeat(14L, "FlashSeat"),
	Courier(15L, "Courier"),
	CourierSeasonCard(16L, "Courier - Season Card");
	
	private Long id;
	
	private String name;
	
	FulfillmentMethodEnum(Long id, String name) {
		this.id = id;
		this.name= name;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static FulfillmentMethodEnum getFulfillmentMethod(Long id) {
		FulfillmentMethodEnum list[] = FulfillmentMethodEnum.class.getEnumConstants();
		for (int i = 0; i < list.length; i++) {
			FulfillmentMethodEnum obj = list[i];
			if (obj.getId().equals(id)) {
				return obj;
			}
		}

		return null;
	}
	
	public static FulfillmentMethodEnum getFulfillmentMethod(String name) {
		FulfillmentMethodEnum list[] = FulfillmentMethodEnum.class.getEnumConstants();
		for (int i = 0; i < list.length; i++) {
			FulfillmentMethodEnum obj = list[i];
			if (obj.getName().equalsIgnoreCase(name)) {
				return obj;
			}
		}

		return null;
	}

}
