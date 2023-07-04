package com.stubhub.domain.inventory.v2.enums;

/**
 * @author sjayaswal
 *
 */
public enum ProxyRoleTypeEnum {
	Pricing("R1"),
	Fulfillment("R2"),
	Super("R3");

	private String name;
	
	ProxyRoleTypeEnum(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static ProxyRoleTypeEnum getProxyRoleTypeEnumByName(String name) {
		ProxyRoleTypeEnum list[] = ProxyRoleTypeEnum.class.getEnumConstants();
		for (int i=0; i<list.length; i++) {
			ProxyRoleTypeEnum obj = list[i];
			if (obj.getName().equalsIgnoreCase(name)) {
				return obj;
			}
		}
		
		return null;
	}
}
