package com.stubhub.domain.inventory.v2.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;

@XmlType(name = "inventoryType")
@XmlEnum(String.class)

public enum InventoryType {

  @XmlEnumValue("CONSIGNMENT")
  CONSIGNMENT(1L, "CONSIGNMENT"),

  @XmlEnumValue("OWNED")
  OWNED(2L, "OWNED"),

  @XmlEnumValue("SELLITNOW")
  SELLITNOW(3L, "SELLITNOW");

  private Long id;
  private String description;

  private InventoryType(Long id, String desc) {
    this.id = id;
    this.description = desc;
  }

  public Long getId() {
    return this.id;
  }

  public String getDescription() {
    return this.description;
  }

  public static InventoryType getByName(String name) {
    if (StringUtils.isNotBlank(name)) {
      InventoryType[] values = InventoryType.values();
      for (InventoryType value : values) {
        if (value.getDescription().equalsIgnoreCase(name))
          return value;
      }
    }
    return null;
  }

  public static InventoryType getById(Long id) {
    if (id != null) {
      InventoryType[] values = InventoryType.values();
      for (InventoryType value : values) {
        if (value.getId().equals(id))
          return value;
      }
    }
    return null;
  }

}
