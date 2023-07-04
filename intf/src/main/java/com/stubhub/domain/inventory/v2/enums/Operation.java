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
public enum Operation 
{
	@XmlEnumValue("UPDATE")
	UPDATE,
	@XmlEnumValue("ADD")
	ADD,
	@XmlEnumValue("DELETE")
	DELETE;  
	
	public static Operation fromString(String type)
	{
		if ( type==null || type.length()==0 )
			return ADD;
		
		else if(UPDATE.name().equalsIgnoreCase(type)) {
			return UPDATE;
		}
		else if(DELETE.name().equalsIgnoreCase(type)) {
			return DELETE;
		}
		else if(ADD.name().equalsIgnoreCase(type)) {
			return ADD;
		}
		return null;  
	}
	
	public boolean equalsEnum ( Operation anotherOp )
	{
		boolean ret = false;
		if ( anotherOp!=null && anotherOp.name()!=null ) {
			ret = this.name().equalsIgnoreCase( anotherOp.name() );
		}
		return ret;
	}
}  
