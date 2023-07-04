package com.stubhub.domain.inventory.listings.v2.enums;

public enum SellerType {
  BUSINESS,
  PRIVATE,
  TRADER;

  public static SellerType findByName(String name) {
    for (SellerType sellerType : SellerType.values()) {
      if (sellerType.name().equalsIgnoreCase(name)) {
        return sellerType;
      }
    }
    return null;
  }
}

