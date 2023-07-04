package com.stubhub.domain.inventory.v2.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;

@XmlType(name = "ticketMedium")
@XmlEnum(String.class)
public enum TicketMedium {

	@XmlEnumValue("PAPER")
	PAPER(1, "PAPER"),

	@XmlEnumValue("PDF")
	PDF(2, "PDF"),

	@XmlEnumValue("BARCODE")
	BARCODE(3, "BARCODE"),
	
	@XmlEnumValue("FLASHSEAT")
	FLASHSEAT(4, "FLASHSEAT"),
	
	@XmlEnumValue("SEASONCARD")
	SEASONCARD(5, "SEASONCARD"),
	
	@XmlEnumValue("EVENTCARD")
	EVENTCARD(6, "EVENTCARD"),
	
	@XmlEnumValue("EXTMOBILE")
	EXTMOBILE(7, "EXTMOBILE"),
	
	@XmlEnumValue("EXTFLASH")
	EXTFLASH(8, "EXTFLASH"),
	
	@XmlEnumValue("MOBILE")
	MOBILE(9, "MOBILE"),
	
	@XmlEnumValue("WRISTBAND")
	WRISTBAND(10, "WRISTBAND"),
	
	@XmlEnumValue("RFID")
	RFID(11, "RFID"),
	
	@XmlEnumValue("GUESTLIST")
	GUESTLIST(12, "GUESTLIST");

	private Integer id;
	private String description;

	private TicketMedium(Integer id, String desc) {
		this.id = id;
		this.description = desc;
	}

	public Integer getId() {
		return this.id;
	}

	public String getDescription() {
		return this.description;
	}

	public static TicketMedium getByName(String name) {
		if (StringUtils.isNotBlank(name)) {
			TicketMedium[] values = TicketMedium.values();
			for (TicketMedium value : values) {
				if (value.getDescription().equalsIgnoreCase(name))
					return value;
			}
		}
		return null;
	}
	
	public static TicketMedium getById(Integer id) {
		if (id != null) {
			TicketMedium[] values = TicketMedium.values();
			for (TicketMedium value : values) {
				if (value.getId().equals(id))
					return value;
			}
		}
		return null;
	}
}
