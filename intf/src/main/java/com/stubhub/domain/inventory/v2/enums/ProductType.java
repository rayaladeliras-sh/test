package com.stubhub.domain.inventory.v2.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Operations to be performed on Tickets / Products
 *   
 * @author sadranly
 */
@XmlType
@XmlEnum(String.class)
public enum ProductType   
{
	@XmlEnumValue("TICKET")
	TICKET,
	@XmlEnumValue("PARKING_PASS")
	PARKING_PASS;
	
	private static final String PARKING_PASS_SPACE_SEPARATED = "PARKING PASS";
	
	public static ProductType fromString(String type)
	{
		// default is TICKET
		if ( type==null || type.length()==0 )
			return TICKET;
		
		else if(TICKET.name().equalsIgnoreCase(type))
			return TICKET;
		else if(PARKING_PASS.name().equalsIgnoreCase(type) || PARKING_PASS_SPACE_SEPARATED.equalsIgnoreCase(type))
			return PARKING_PASS;
		else
			return null;  
	}
	
	public boolean equalsEnum ( ProductType anotherOp )
	{
		boolean ret = false;
		if ( anotherOp!=null && anotherOp.name()!=null ) {
			ret = this.name().equalsIgnoreCase( anotherOp.name() );
		}
		return ret;
	}
}  
  