package com.stubhub.domain.inventory.listings.v2.adapter;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.stubhub.domain.inventory.datamodel.entity.TicketSeat;

public class TicketSeatComparatorTest {

    @Test
    public void testCompare() {
        TicketSeat ts1 = new TicketSeat();
        ts1.setTicketSeatId(1L);
        TicketSeat ts2 = new TicketSeat();
        ts2.setTicketSeatId(2L);

        TicketSeatComparator tsc = new TicketSeatComparator();
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);

        ts1.setSeatNumber("GA");
        ts2.setSeatNumber("GA");
        result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);

        ts1.setSeatNumber("Parking Pass");
        result = tsc.compare(ts1, ts2);
        Assert.assertEquals(1, result);

        ts2.setSeatNumber("Parking Pass");
        result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);

        ts1.setSeatNumber("");
        ts2.setSeatNumber("Parking Pass");
        result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);

        ts1.setSeatNumber(null);
        ts2.setSeatNumber("1");
        result = tsc.compare(ts1, ts2);
        Assert.assertEquals(1, result);

        ts1.setSeatNumber("1");
        ts2.setSeatNumber(null);
        result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);

        ts1.setSeatNumber("2");
        ts2.setSeatNumber("1");
        result = tsc.compare(ts1, ts2);
        Assert.assertEquals(1, result);

        ts1.setSeatNumber("1");
        ts2.setSeatNumber("2");
        result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);

        ts1.setSeatNumber("a");
        ts2.setSeatNumber("b");
        result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);

        ts1.setSeatNumber("a9");
        ts2.setSeatNumber("a10");
        result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);
    }

    @Test
    public void testSeatNumbersIntegerMaxValue() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        //2^31 - 1  (2147483647)
        ts1.setSeatNumber("2147483647");
        ts2.setSeatNumber("2147483647");
        ts1.setTicketSeatId(1L);
        ts2.setTicketSeatId(2L);
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);
    }

    @Test
    public void testSeatNumbersIntegerMaxValues() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        //2^63 - 1 (Integer MAX_VALUE)
        ts1.setSeatNumber(String.valueOf(Integer.MAX_VALUE));
        ts2.setSeatNumber(String.valueOf(Integer.MAX_VALUE));
        ts1.setTicketSeatId(2L);
        ts2.setTicketSeatId(1L);
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(1, result);
    }

    @Test
    public void testSeatNumbersLongMaxValue() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        //2^63 - 1  (9223372036854775807)
        ts1.setSeatNumber("9223372036854775807");
        ts2.setSeatNumber("9223372036854775807");
        ts1.setTicketSeatId(1L);
        ts2.setTicketSeatId(2L);
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);
    }

    @Test
    public void testSeatNumbersLongMaxValues() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        //2^63 - 1 (Long MAX_VALUE)
        ts1.setSeatNumber(String.valueOf(Long.MAX_VALUE));
        ts2.setSeatNumber(String.valueOf(Long.MAX_VALUE));
        ts1.setTicketSeatId(2L);
        ts2.setTicketSeatId(1L);
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(1, result);
    }

    @Test
    public void testReverse() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        //Seat Number
        ts1.setSeatNumber("123456789");
        ts2.setSeatNumber("987654321");
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);
    }

    @Test
    public void testSeatNumberUnderIntegerMax() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        //2^31 - 2 (2147483646)
        ts1.setSeatNumber("2147483645");
        ts2.setSeatNumber("2147483646");
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);
    }

    @Test
    public void testSeatNumberBelowIntegerMax() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        //2^31 - 2 (Integer MAX_VALUE - 1)
        ts1.setSeatNumber(String.valueOf(Integer.MAX_VALUE - 1));
        ts2.setSeatNumber(String.valueOf(Integer.MAX_VALUE - 2));
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(1, result);
    }

    @Test
    public void testSeatNumberOverIntegerMax() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        //2^31  (9223372036854775808)
        ts1.setSeatNumber("2147483647");
        ts2.setSeatNumber("2147483648");
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);
    }

    @Test
    public void testSeatNumberAboveIntergerMax() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        //2^31 (Integer MAX_VALUE + 1)
        ts1.setSeatNumber(String.valueOf(new Long("2147483648")));
        ts2.setSeatNumber(String.valueOf(Integer.MAX_VALUE));
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(1, result);
    }

    @Test
    public void testSeatNumberUnderLongMax() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        //2^63 - 2 (9223372036854775806)
        ts1.setSeatNumber("9223372036854775805");
        ts2.setSeatNumber("9223372036854775806");
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);
    }

    @Test
    public void testSeatNumberBelowLongMax() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        //2^63 - 2 (Max Long Value - 1)
        ts1.setSeatNumber(String.valueOf(Long.MAX_VALUE - 1));
        ts2.setSeatNumber(String.valueOf(Long.MAX_VALUE - 2));
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(1, result);
    }

    @Test
    public void testSeatNumberOverLongMax() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        //2^63 (9223372036854775808)
        ts1.setSeatNumber("9223372036854775808");
        ts2.setSeatNumber("9223372036854775807");
        ts1.setTicketSeatId(2L);
        ts2.setTicketSeatId(1L);
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(1, result);
    }

    @Test
    public void testSeatNumberAboveLongMax() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        //2^63 (Long MAX_VALUE + 1)
        ts1.setSeatNumber(String.valueOf("9223372036854775808"));
        ts2.setSeatNumber(String.valueOf(Long.MAX_VALUE));
        ts1.setTicketSeatId(1L);
        ts2.setTicketSeatId(2L);
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);
    }

    @Test
    public void testMaxSeatNumberAllowed() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        //Seat Number Max Length of 25 digits
        ts1.setSeatNumber("9999999999999999999999999");
        ts2.setSeatNumber("9999999999999999999999998");
        ts1.setTicketSeatId(2L);
        ts2.setTicketSeatId(1L);
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(1, result);
    }

    @Test
    public void testSellApi2295_1() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        //Mentioned in SELLAPI-2295
        ts1.setSeatNumber("2188795729587449");
        ts2.setSeatNumber("2188795729587450");
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);
    }

    @Test
    public void testSellApi2295_2() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        ts1.setSeatNumber("2193495290830732");
        //Mentioned in SELLAPI-2295
        ts2.setSeatNumber("2193495290830731");
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(1, result);
    }

    @Test
    public void testSellApi2295_3() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        //Mentioned in SELLAPI-2295
        ts1.setSeatNumber("2656785218346103");
        ts2.setSeatNumber("2656785218346104");
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);
    }

    @Test
    public void testSellApi2295_4() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        ts1.setSeatNumber("2699343502432757");
        //Mentioned in SELLAPI-2295
        ts2.setSeatNumber("2699343502432756");
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(1, result);
    }

    @Test
    public void testSellApi2295_5() {
        TicketSeat ts1 = new TicketSeat();
        TicketSeat ts2 = new TicketSeat();
        TicketSeatComparator tsc = new TicketSeatComparator();

        //Mentioned in SELLAPI-2295
        ts1.setSeatNumber("2822888187472766");
        ts2.setSeatNumber("2822888187472767");
        int result = tsc.compare(ts1, ts2);
        Assert.assertEquals(-1, result);
    }
}
