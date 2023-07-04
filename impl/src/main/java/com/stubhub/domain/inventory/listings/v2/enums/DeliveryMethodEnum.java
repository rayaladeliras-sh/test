package com.stubhub.domain.inventory.listings.v2.enums;


public enum DeliveryMethodEnum {
	
	ElectronicDownload("Electronic - Download"),
	ElectronicInstantDownload("Electronic - Instant Download"),
	WillCall("WillCall"),
	Kiosk("Kiosk"),	
//	FedExSaturday("FedEx Saturday"),
//	FedExTwoDay("FedEx Two Day"),
//	FedExStandardOvernight("FedEx Standard Overnight"),
//	FedExPriorityOvernightUS("FedEx Priority Overnight - Continental US"),
//	FedExPriorityOvernightAKAndHI("FedEx Priority Overnight - AK and HI"),
//	FedExIntraCanadaOvernight("FedEx Intra Canada Overnight"),
//	FedExInternationalPriorityPuertoRico("FedEx International Priority Puerto Rico"),
//	FedExInternationalPriorityCanada("FedEx International Priority Canada"),
//	FedExInternationalEconomyIntraCanada("FedEx International Economy - Intra Canada"),
	Pickup("Pickup"),
	PickupEventDay("Pickup - Event Day"),
	OffSitePickup("Off-site Pickup"),
	Hospitality("Hospitality"),
	Email("Email"),
	Courier("Courier"),
	UpsWorldwideSaverFromUS("UPS Worldwide Saver - From US"),
	UpsWorldwideSaverFromCA("UPS Worldwide Saver - From CA"),
	UpsWorldwideSaverFromUK("UPS Worldwide Saver - From UK"),
	UpsNextBusinessDayPMIntraUSA("UPS Next Business Day PM - Intra-USA"),
	Ups2ndDayAirIntraUSA("UPS 2nd Day Air - Intra-USA"),
	UpsNextBusinessDayAMIntraUSA("UPS Next Business Day AM - Intra USA"),
	
	UpsWorldwideSaverUKToPR("UPS Worldwide Saver - UK-PR"),
	UpsWorldwideSaverUKToAKAndHI("UPS Worldwide Saver - UK-AK/HI"),
	UpsWorldwideSaverCAToAKAndHI("UPS Worldwide Saver - CA-AK/HI"),
	UpsWorldwideSaverCAToUK("UPS Worldwide Saver - CA-UK"),
	UpsExpressSaverIntraCA("UPS Express Saver - Intra-CA"),
	UpsStandardIntraCA("UPS Standard - Intra-CA"),
	UpsWorldwideSaverCAToPR("UPS Worldwide Saver - CA-PR"),
	UpsNextBusinessDayAir("UPS Next Business Day Air"),
	UpsNextBusinessDaySaverIntraUSA("UPS Next Business Day Saver - Intra-USA"),
	UpsNextBusinessDayAirIntraUSA("UPS Next Business Day Air - Intra USA"),
	UpsNextBusinessDayAMUSAToAKAndHI("UPS Next Business Day AM -USA-AK/HI"),
	UpsWorldwideSaverUSAToPR("UPS Worldwide Saver - USA-PR");
		
	private String name;
	
	DeliveryMethodEnum(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static DeliveryMethodEnum getDeliveryMethodEnumByName(String name) {
		if (name != null) {
			DeliveryMethodEnum list[] = DeliveryMethodEnum.class.getEnumConstants();
			for (int i=0; i<list.length; i++) {
				DeliveryMethodEnum obj = list[i];
				if (obj.getName().equalsIgnoreCase(name.trim())) {
					return obj;
				}
			}
		}
		
		return null;
	}


}

