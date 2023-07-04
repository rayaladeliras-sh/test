package com.stubhub.domain.inventory.listings.v2.util;

import java.util.Calendar;
import java.util.Date;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.chrono.ISOChronology;

public class TimeUtils {
	
	public static String getDateFormatISO8601 ( java.util.Calendar cal ) {
		
		Chronology chrono = ISOChronology.getInstance();
		
		// date
		int year = cal.get(Calendar.YEAR);
		int mon =  cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		
		// time
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);
		int msec = cal.get(Calendar.MILLISECOND);
		

		DateTime dt = new DateTime(year, mon, day, hour, min, sec, msec, chrono );
		return dt.toString();
	}
	
	/**
	 * Returns true if the new value of sale end date is in between current date
	 * and event date + 2 days.
	 * 
	 * @param cEventDate
	 *            an event date.
	 * @param cSaleEndDate
	 *            - value of sale end date
	 * @return true/false
	 */
	public static boolean isValidSaleEndDate(Date dEventDate, Date dSaleEndDate) {
		
		if (dEventDate == null || dSaleEndDate == null)
			return false;

		DateTime to = new DateTime(dEventDate).plusDays(2);
		DateTime saleEndDate = new DateTime(dSaleEndDate);
		DateTime from = new DateTime();

		return new Interval(from, to).contains(saleEndDate);
	}
	
}
