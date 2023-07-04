package com.stubhub.domain.inventory.listings.v2.util;

import com.stubhub.newplatform.common.entity.Money;

public class ListingPayout {

  private final Money totalPayout;
  private final double markUp;
  private double defaultSellerFee = 0.0d;

  public ListingPayout(Money totalPayout, double markUp, double defaultSellerFee) {
    super();
    this.totalPayout = totalPayout;
    this.markUp = markUp;
    this.defaultSellerFee = defaultSellerFee;
  }

  public ListingPayout(Money totalPayout, double markUp) {
    super();
    this.totalPayout = totalPayout;
    this.markUp = markUp;
  }

  /**
   * @return the totalPayout
   */
  public Money getTotalPayout() {
    return totalPayout;
  }

  /**
   * @return the markUp
   */
  public double getMarkUp() {
    return markUp;
  }

  /**
   * @return the defaultSellerFee
   */
  public double getDefaultSellerFee() {
    return defaultSellerFee;
  }


}
