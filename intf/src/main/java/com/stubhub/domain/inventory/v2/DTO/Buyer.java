package com.stubhub.domain.inventory.v2.DTO;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonRootName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "buyer")
@XmlType(name = "", propOrder = {"id", "firstName", "lastName", "email"})
@JsonRootName(value = "buyer")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Buyer {
	
	@XmlElement(name = "id")
	private Long id;
	
	@XmlElement(name = "firstName")
	private String firstName;
	
	@XmlElement(name = "lastName")
	private String lastName;
	
	@XmlElement(name = "email")
	private String email;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
}
