package com.stubhub.domain.inventory.v2.enums;

public enum TicketSeatStatusEnum {
	AVAILABLE(1),
	UNCONFIRMED(2),
	SOLD(3),
	REMOVED(4),
	INCOMPLETE(5);
	
	private Integer code;
	
	private TicketSeatStatusEnum(Integer code) {
		this.code = code;
	}

	public Integer getCode() {
		return code;
	}

	/**
	 * Returns TicketSeatStatusEnum by listing system status code
	 * @param code
	 * @return TicketSeatStatusEnum
	 */
	public static TicketSeatStatusEnum getTicketSeatStatusEnumByCode(Integer code) {
		TicketSeatStatusEnum[] values = values();

		for (TicketSeatStatusEnum type : values) {
			if(type.getCode().equals(code)) {
				return type;
			}
		}
		return null;
	}
	
	/**
	 * Returns TicketSeatStatusEnum based on given name
	 * @param option
	 * @return
	 */
	public static TicketSeatStatusEnum getTicketSeatStatusEnumByName(String name) {
		TicketSeatStatusEnum[] names = TicketSeatStatusEnum.class.getEnumConstants();

		for (TicketSeatStatusEnum type : names) {
			if(type.name().equalsIgnoreCase(name)) {
				return type;
			}
		}
		return null;
	}
}