/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File STime.java
 */
 
package ALMA.scheduling.define;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * The STime class provides for a moment in time accurate to the nearest second.
 * String input and output are in the FITS date-time format.  The FITS format 
 * is of the form:	
 * 			YYYY-MM-DDThh:mm:ss
 * Leading zeros are required if months, days, hours, minutes, or seconds are 
 * single digits.  The value for months ranges from "01" to "12".
 * 
 * The only departure from the FITS standard is to allow the "T" between the 
 * data and time values to be optional in constructing a Time from a string.
 * 
 * @version 1.00  Jun 3, 2003
 * @author Allen Farris
 */
public class STime {

	long datetime;
	
	/**
	 * Construct a Time whose value is 0.
	 */
	public STime() {
		datetime = 0L;
	}
	
	/**
	 * Construct a STime from a FITS-formatted string.  The format must be of 
	 * the form:	
	 * 			YYYY-MM-DDThh:mm:ss
	 * Leading zeros are required if months, days, hours, minutes, or seconds 
	 * are single digits.  The value for months ranges from "01" to "12".  
	 * The "T" separting the data and time values is optinal.  If the "T" is 
	 * not present, then a space must be present.
	 * 
	 * An IllegalArgumentException is thrown is the string is not a valid 
	 * date-time.
	 */
	public STime(String t) {
		if (t.length() < 19 || t.charAt(4) != '-' || t.charAt(7) != '-' || 
			(t.charAt(10) != 'T' && t.charAt(10) != ' ') || 
			t.charAt(13) != ':' || t.charAt(16) != ':')
			throw new IllegalArgumentException("Invalid time format: " + t);
		int yyyy = 0;
		int mm = 0;
		int dd = 0;
		int hh = 0;
		int min = 0;
		int sec = 0;
		try {
			yyyy = Integer.parseInt(t.substring(0,4));
			mm   = Integer.parseInt(t.substring(5,7));
			dd   = Integer.parseInt(t.substring(8,10));
			hh   = Integer.parseInt(t.substring(11,13));
			min  = Integer.parseInt(t.substring(14,16));
			sec  = Integer.parseInt(t.substring(17));
		} catch (NumberFormatException err) {
			throw new IllegalArgumentException("Invalid time format: " + t);
		}
		if (yyyy < 1970 || mm < 1 || mm > 12 || dd < 1 || dd > 31 || 
			hh < 0 || hh >= 24 || min < 0 || min >= 60 || sec < 0 || 
			sec >= 60) {
			throw new IllegalArgumentException("Invalid time format: " + t);
		}
		datetime = new GregorianCalendar(yyyy,(mm - 1),dd,hh,min,
										 sec).getTime().getTime();
	}
	
	/**
	 * Construct a time to the nearest minute.  The value of seconds is 0.
	 * The value for months ranges from 1 to 12.
	 */
	public STime(int year, int month, int day, int hour, int minute) {
		if (year < 1970 || month < 1 || month > 12 || day < 1 || day > 31 || 
		hour < 0 || hour >= 24 || minute < 0 || minute >= 60) {
			throw new IllegalArgumentException("Invalid time format: " + 
			year + "-" + month + "-" + day + " " + hour + ":" + minute);
		}
		datetime = new GregorianCalendar(year,(month - 1),day,hour,
										 minute).getTime().getTime();
	}
	
	/**
	 * Construct a time to the nearest second.
	 * The value for months ranges from 1 to 12.
	 */
	public STime(int year, int month, int day, int hour, int minute, 
				 int second) {
		if (year < 1970 || month < 1 || month > 12 || day < 1 || day > 31 || 
			hour < 0 || hour >= 24 || minute < 0 || minute >= 60 || second < 0 || 
			second >= 60) {
			throw new IllegalArgumentException("Invalid time format: " +
			year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second);
		}
		datetime = new GregorianCalendar(year,(month - 1),day,hour,minute,
										 second).getTime().getTime();
	}
	
	/**
	 * Construct a STime whose value is initialized to the specified time.
	 */
	public STime(STime t) {
		datetime = t.datetime;
	}
	
	/**
	 * Return the internal value of the time, which is in milliseconds 
	 * since Jan. 1, 1970.
	 */
	public long getTime() {
		return datetime;
	}
	
	/**
	 * Add an interval to this time.
	 */
	public void add(TimeInterval interval) {
		datetime += interval.getLength() * 1000L;
	}
	
	/**
	 * Convert the time to a string in FITS format.
	 */
	public String toString() {
		GregorianCalendar x = convert(this);
		StringBuffer s = new StringBuffer(x.get(Calendar.YEAR) + "-");
		int mm = x.get(Calendar.MONTH) + 1;
		int dd = x.get(Calendar.DAY_OF_MONTH);
		int hh = x.get(Calendar.HOUR_OF_DAY);
		int min = x.get(Calendar.MINUTE);
		int sec = x.get(Calendar.SECOND);
		if (mm < 10) s.append('0'); s.append(mm); s.append('-');
		if (dd < 10) s.append('0'); s.append(dd); s.append('T');
		if (hh < 10) s.append('0'); s.append(hh); s.append(':');
		if (min < 10) s.append('0'); s.append(min);  s.append(':');
		if (sec < 10) s.append('0'); s.append(sec);
		return s.toString();
	}
	
	/**
	 * Generate a new Time by adding a specified number of seconds to a 
	 * particular STime.
	 * @param t The particular time.
	 * @param seconds The number of seconds to be added to time t.
	 * @return STime
	 */
	static public STime add(STime t, int seconds) {
		STime d = new STime ();
		d.datetime = t.datetime + seconds * 1000L;
		return d;
	}

	/**
	 * Convert the STime object to a proper Gregorian Date.
	 */
	static public GregorianCalendar convert(STime x) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(x.getTime());
		return calendar;
	}

	/**
	 * Provide for a unit test.
	 */
	public static void main(String[] args) {
		STime t1 = new STime(2002,8,9,20,0,0);
		System.out.println("SCHEDULING: t1 = " + t1);
		STime t2 = new STime();
		System.out.println("SCHEDULING: t2 = " + t2);
		
		long now = new GregorianCalendar(2002,8,8).getTime().getTime();
		System.out.println("SCHEDULING: 2002-8-8 = " + now);
		STime x = new STime(2002,8,8,8,30,15);
		System.out.println("SCHEDULING: x = " + x.getTime());
		
		System.out.println("SCHEDULING: x := " + STime.convert(x).getTime());
		System.out.println("SCHEDULING: x as FITS is " + x);
		
		STime x2 = STime.add(x,1845);
		System.out.println("SCHEDULING: x2 is " + x2);
		
		STime y1 = new STime ("2002-08-19T09:42:56");
		System.out.println("SCHEDULING: y1 = " + y1);

		STime y2 = new STime ("2002-08-19 09:42:56");
		System.out.println("SCHEDULING: y2 = " + y2);

		try {
			STime y3 = new STime ("2002-8-19T9:42");
		} catch (IllegalArgumentException err) {
			System.out.println("SCHEDULING: "+err.toString());
		}
	}
}
