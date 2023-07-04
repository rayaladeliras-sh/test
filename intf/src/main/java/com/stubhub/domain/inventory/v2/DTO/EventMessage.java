package com.stubhub.domain.inventory.v2.DTO;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
	"header",
	"message",
	"type"
})
@XmlRootElement(name = "eventMessage")
public class EventMessage implements Serializable {
	private static final long serialVersionUID = 4635319120183702769L;

	@XmlElement(name = "header", required = true)
    private String header;
	@XmlElement(name = "message", required = true)
    private String message;
	@XmlElement(name = "type", required = true)
    private int type;
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
}
