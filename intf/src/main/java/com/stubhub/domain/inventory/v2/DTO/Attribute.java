package com.stubhub.domain.inventory.v2.DTO;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonRootName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "attribute")
@JsonRootName(value = "attribute")
@XmlType(name = "", propOrder = {"key", "value"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attribute implements Serializable {

  private static final long serialVersionUID = -5951865546502983465L;

  @XmlElement(name = "key", required = false)
  private String key;

  @XmlElement(name = "value", required = false)
  private String value;
  
  @XmlElement(name = "type", required = false)
  private String type;

  public Attribute(String key, String value, String type) {
    this.key = key;
    this.value = value;
    this.type = type;
  }

  public Attribute() {

  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getType() {
	return type;
  }

  public void setType(String type) {
	this.type = type;
  }

}

