package com.stubhub.domain.inventory.listings.v2.util;

import java.io.IOException;
import java.math.BigDecimal;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.stubhub.newplatform.common.entity.Money;

public class ListingPayoutUtilTest {

  @BeforeMethod
  public void setUp() {}

  @Test
  public void payoutSkipMarkupTest_10() throws IOException {
    Money totalPayout = new Money();
    totalPayout.setAmount(new BigDecimal(100));

    double markUp = 10;
    double defaultSellerFee = 0;
    ListingPayout lpayout = new ListingPayout(totalPayout, markUp, defaultSellerFee);

    Money expectedPayout = ListingPayoutUtil.calculatePriceAfterMarkUp(lpayout);
    BigDecimal rpayout = expectedPayout.getAmount();
    Double dpayout = rpayout.doubleValue();
    BigDecimal expectedAmount = new BigDecimal(111.11);
    Double dexpectedAmount = expectedAmount.doubleValue();
    Assert.assertTrue(dexpectedAmount.equals(dpayout));

  }

  @Test
  public void payoutSkipMarkupTest_5() throws IOException {
    Money totalPayout = new Money();
    totalPayout.setAmount(new BigDecimal(100));

    double markUp = 7;
    double defaultSellerFee = 0;
    ListingPayout lpayout = new ListingPayout(totalPayout, markUp, defaultSellerFee);

    Money expectedPayout = ListingPayoutUtil.calculatePriceAfterMarkUp(lpayout);
    BigDecimal rpayout = expectedPayout.getAmount();
    Double dpayout = rpayout.doubleValue();
    BigDecimal expectedAmount = new BigDecimal(107.53);
    Double dexpectedAmount = expectedAmount.doubleValue();
    Assert.assertTrue(dexpectedAmount.equals(dpayout));

  }

  @Test
  public void payoutSkipMarkupTest_0() throws IOException {
    Money totalPayout = new Money();
    totalPayout.setAmount(new BigDecimal(100));

    double markUp = 0;
    double defaultSellerFee = 0;
    ListingPayout lpayout = new ListingPayout(totalPayout, markUp, defaultSellerFee);

    Money expectedPayout = ListingPayoutUtil.calculatePriceAfterMarkUp(lpayout);
    BigDecimal rpayout = expectedPayout.getAmount();
    Double dpayout = rpayout.doubleValue();
    BigDecimal expectedAmount = new BigDecimal(100.0);
    Double dexpectedAmount = expectedAmount.doubleValue();
    Assert.assertTrue(dexpectedAmount.equals(dpayout));

  }

  @Test
  public void payoutMarkupTest_100_5_5() throws IOException {
    Money totalPayout = new Money();
    totalPayout.setAmount(new BigDecimal(100));

    double markUp = 5;
    double defaultSellerFee = 5;
    ListingPayout lpayout = new ListingPayout(totalPayout, markUp, defaultSellerFee);

    Money expectedPayout = ListingPayoutUtil.calculateExpectedPayout(lpayout);
    BigDecimal rpayout = expectedPayout.getAmount();
    BigDecimal expectedAmount = new BigDecimal("100.7000");

    Assert.assertTrue(expectedAmount.equals(rpayout));

  }


  @Test
  public void payoutMarkupTest_100_5_3() throws IOException {
    Money totalPayout = new Money();
    totalPayout.setAmount(new BigDecimal(100));

    double markUp = 3;
    double defaultSellerFee = 5;
    ListingPayout lpayout = new ListingPayout(totalPayout, markUp, defaultSellerFee);

    Money expectedPayout = ListingPayoutUtil.calculateExpectedPayout(lpayout);
    BigDecimal rpayout = expectedPayout.getAmount();

    BigDecimal expectedAmount = new BigDecimal("98.8000");

    Assert.assertTrue(expectedAmount.equals(rpayout));

  }

  @Test
  public void payoutMarkupTest_100_5_0() throws IOException {
    Money totalPayout = new Money();
    totalPayout.setAmount(new BigDecimal(100));

    double markUp = 0;
    double defaultSellerFee = 5;
    ListingPayout lpayout = new ListingPayout(totalPayout, markUp, defaultSellerFee);

    Money expectedPayout = ListingPayoutUtil.calculateExpectedPayout(lpayout);
    BigDecimal rpayout = expectedPayout.getAmount();

    BigDecimal expectedAmount = new BigDecimal("95.0000");

    Assert.assertTrue(expectedAmount.equals(rpayout));

  }

  @Test
  public void payoutMarkupTest_100Point2_5_5() throws IOException {
    Money totalPayout = new Money();
    totalPayout.setAmount(new BigDecimal(100.2));

    double markUp = 5;
    double defaultSellerFee = 5;
    ListingPayout lpayout = new ListingPayout(totalPayout, markUp, defaultSellerFee);

    Money expectedPayout = ListingPayoutUtil.calculateExpectedPayout(lpayout);
    BigDecimal rpayout = expectedPayout.getAmount();
    BigDecimal expectedAmount = new BigDecimal("100.7000");
    Assert.assertTrue(expectedAmount.equals(rpayout));

  }


  @Test
  public void payoutMarkupTest_10_5_10() throws IOException {
    Money totalPayout = new Money();
    totalPayout.setAmount(new BigDecimal(10));

    double markUp = 5;
    double defaultSellerFee = 10;
    ListingPayout lpayout = new ListingPayout(totalPayout, markUp, defaultSellerFee);

    Money expectedPayout = ListingPayoutUtil.calculateExpectedPayout(lpayout);
    BigDecimal rpayout = expectedPayout.getAmount();
    BigDecimal expectedAmount = new BigDecimal("9.9000");
    Assert.assertTrue(expectedAmount.equals(rpayout));

  }


  @Test
  public void payoutMarkupTest_100Point7_5_5() throws IOException {
    Money totalPayout = new Money();
    totalPayout.setAmount(new BigDecimal(100.70));

    double markUp = 5;
    double defaultSellerFee = 5;
    ListingPayout lpayout = new ListingPayout(totalPayout, markUp, defaultSellerFee);

    Money expectedPayout = ListingPayoutUtil.calculateExpectedPayout(lpayout);
    BigDecimal rpayout = expectedPayout.getAmount();

    BigDecimal expectedAmount = new BigDecimal("100.7000");
    Assert.assertTrue(expectedAmount.equals(rpayout));

  }


  @Test
  public void payoutMarkupTest_100Point8_5_5() throws IOException {
    Money totalPayout = new Money();
    totalPayout.setAmount(new BigDecimal(100.80));

    double markUp = 5;
    double defaultSellerFee = 5;
    ListingPayout lpayout = new ListingPayout(totalPayout, markUp, defaultSellerFee);

    Money expectedPayout = ListingPayoutUtil.calculateExpectedPayout(lpayout);
    BigDecimal rpayout = expectedPayout.getAmount();

    BigDecimal expectedAmount = new BigDecimal("101.6500");
    Assert.assertTrue(expectedAmount.equals(rpayout));

  }


}
