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
 * File DateTime.java
 */
 
package alma.scheduling.define;


/**
 * Create an instant in time.  All dates are assumed to be in the
 * Gregorian calendar, including those prior to October 15, 1582.
 * So, if you are interested in very old dates, this isn't the most
 * convenient class to use.
 * <p>
 * Internally, time is kept in the form of the Julian day, 
 * including fractions thereof.  The Julian day is the continuous 
 * time since the beginning of the year -4712.  By tradition the
 * Julian day starts at 12 hours UT.  
 * <p>
 * When forming an instant of time, that time is the local time in 
 * the timezone of the clock that provides the current time.  This 
 * clock is configurable via the "setClock" static method and the 
 * default is the system clock.  One also specifies the geographical 
 * coordinates of the clock, as well as the local timezone. 
 * <p>
 * The main reference used in crafting these methods is 
 * Astronomical Algorithms by Jean Meeus, second edition,
 * 2000, Willmann-Bell, Inc., ISBN 0-943396-61-1.  See
 * chapter 7, "Julian day", and chapter 12, "Sideral Time".
 * 
 * @version 1.00  Jun 11, 2003
 * @author Allen Farris
 */
public class DateTime {
	
	//static private Clock defaultClock = Clock.DEFAULT;
	static private Clock defaultClock = null;
	
	/**
	 * Configure the clock used to get the current local time.
	 * The default clock is the system clock.
	 */
	static public void setDefaultClock(Clock c) {
		defaultClock = c;
	}
	
	/**
	 * Return true if the specified year is a leap year.
	 * @param year the year in the Gregorian calendar.
	 * @return true if the specified year is a leap year.
	 */
	static public boolean isLeapYear(int year) {
		if (year % 4 != 0)
			return false;
		if (year % 100 == 0 && year % 400 != 0)
			return false;
		return true;
	}

	/**
	 * Return the Modified Julian day, given the Julian day.
	 * @param jd
	 * @return
	 */
	static public double getMJD(double jd) {
		return jd - 2400000.5;
	}

	/**
	 * Return the Julian day, given the Modified Julian day.
	 * @param mjd
	 * @return
	 */
	static public double getJD(double mjd) {
		return mjd + 2400000.5;
	}

	/**
	 * Return the difference in days, and fractions thereof,
	 * between two DateTimes.
	 * @param t1
	 * @param t2
	 * @return The number of days in t1 - t2.
	 */
	static public double difference(DateTime t1, DateTime t2) {
		return t1.jd - t2.jd;
	}

	/**
	 * Return the predicted difference, in seconds, between dynamical
	 * time and UT, i.e., TD - UT, for the specified year.  This
	 * calculation is only valid from 2000 to 2100.
	 * @return
	 */
	public static double getDTDiff(int year) {
		double t = (year - 2000.0) / 100.0;
		return 102.0 + t * (102.0  + 25.3 * t) + (0.37 * (year - 2100.0));
	}

	/**
	 * Generate a new DateTime by adding a specified number of days
	 * to the specified DateTime.
	 * @param t
	 * @param days
	 * @return
	 */
	static public DateTime add(DateTime t, double days) {
		DateTime dt = new DateTime(t);
		dt.jd += days;
		return dt;
	}

	/**
	 * Internally, time is kept in the form of the Julian day, 
	 * including fractions thereof.  the Julian day is the continuous 
	 * time since the beginning of the year -4712.  By tradition the
	 * Julian day starts at 12 hours UT.
	 */
	private double jd;
	private Clock clock;

	/**
	 * Create a DateTime by specifying the Julian day.
	 * @param julianDay the Julian day, including fractions thereof.
	 */
	public DateTime(double julianDay) {
		clock = defaultClock;
		jd = julianDay;
	}

	// The two "init" methods are used by constructors.
	private void init(int year, int month, double day) {
		if (month < 1 || month > 12)
			throw new IllegalArgumentException ("Illegal value of month: " 
				+ year + "-" + month + "-" + day);
		if ( (day < 1.0 || day > 31.0) ||
			 ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30.0) ||
			 (month == 2 && (day > ((isLeapYear(year) ? 29.0 : 28.0)))) )
			throw new IllegalArgumentException ("Illegal value of day: "
				+ year + "-" + month + "-" + day);
		if (month <= 2) {
			--year;
			month += 12;
		}
		int A = year / 100;
		int B = 2 - A + (A / 4);
		// Apply the time zone correction.
		day += clock.getConvertToUT();
		jd = (int)(365.25 * (year + 4716)) + (int)(30.6001 * (month + 1)) + day + B - 1524.5;
	}
	private void init(int year, int month, int day, int hour, int minute, double second) {
		if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0.0 || second >= 60.0) {
			throw new IllegalArgumentException("Invalid time: " + hour + ":" + minute + ":" + second);
		}
		init(year,month,(double)(day + (((((second / 60.0) + minute) / 60.0) + hour) / 24.0)));
	}

	/**
	 * Create a DateTime that is initialized to the current local time.
	 *
	 */
	public DateTime() {
		clock = defaultClock;
		long currentTime = clock.currentTimeMillis();
		// The current time is in milliseconds, so we convert it to 
		// days and fractions of a day.  The JD for the reference period,
		// January 1, 1970 UT is 2440587.5.
		jd = 2440587.5 + currentTime / 86400000.0;
	}

	/**
	 * Create a DateTime by specifying the year, month, and day plus the fraction of a day.
	 * @param year
	 * @param month
	 * @param day the day (and time in the local time zone)
	 */
	public DateTime(int year, int month, double day) {
		clock = defaultClock;
		init(year,month,day);
	}

	/**
	 * Create a DataTime by specifying the calendar date and the local time.
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param minute
	 * @param second
	 */
	public DateTime(int year, int month, int day, int hour, int minute, double second) {
		clock = defaultClock;
		if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0.0 || second >= 60.0) {
			throw new IllegalArgumentException("Invalid time: " + hour + ":" + minute + ":" + second);
		}
		init(year,month,(double)(day + (((((second / 60.0) + minute) / 60.0) + hour) / 24.0)));
	}
	
	/**
	 * Create a DateTime by specfying a Date and a local Time.
	 * @param date
	 * @param time
	 */
	public DateTime(Date date, Time time) {
		clock = defaultClock;
		init(date.getYear(),date.getMonth(),((time.getTime() / 24.0) + date.getDay()));
	}

	/**
	 * Construct a DateTime from a FITS-formatted string, specifying the local time.  
	 * The format must be of the form:	
	 * 			YYYY-MM-DDThh:mm:ss.ssss
	 * Leading zeros are required if months, days, hours, minutes, or seconds 
	 * are single digits.  The value for months ranges from "01" to "12".  
	 * The "T" separting the data and time values is optinal.  If the "T" is 
	 * not present, then a space MUST be present.
	 * 
	 * An IllegalArgumentException is thrown is the string is not a valid 
	 * date-time.
	 */
	public DateTime(String t) {
		clock = defaultClock;
		if (t.length() < 19 || t.charAt(4) != '-' || t.charAt(7) != '-' || 
			(t.charAt(10) != 'T' && t.charAt(10) != ' ') || 
			t.charAt(13) != ':' || t.charAt(16) != ':')
			throw new IllegalArgumentException("Invalid time format: " + t);
		int yyyy = 0;
		int mm = 0;
		int dd = 0;
		int hh = 0;
		int min = 0;
		double sec = 0.0;
		try {
			yyyy = Integer.parseInt(t.substring(0,4));
			mm   = Integer.parseInt(t.substring(5,7));
			dd   = Integer.parseInt(t.substring(8,10));
			hh   = Integer.parseInt(t.substring(11,13));
			min  = Integer.parseInt(t.substring(14,16));
			sec  = Double.parseDouble(t.substring(17));
		} catch (NumberFormatException err) {
			throw new IllegalArgumentException("Invalid time format: " + t);
		}
		init(yyyy,mm,dd,hh,min,sec);
	}
	
	/**
	 * Create a DateTime that is initialized to a specified DateTime.
	 * @param dt
	 */
	public DateTime(DateTime dt) {
		clock = dt.clock;
		jd = dt.jd;
	}

	/**
	 * Create a DateTime by specifying the clock, year, month, and day plus the fraction of a day.
	 * This is most useful in specifying a UT time, e.g.,
	 * new DateTime(Clock.UT,2028,11,13.19).
	 * @param c The clock associated with this time.
	 * @param year The year.
	 * @param month The month.
	 * @param day the day (and time in the local time zone)
	 */
	public DateTime(Clock c, int year, int month, double day) {
		clock = c;
		init(year,month,day);
	}

	public DateTime(Clock c, Date d, double hours) {
		clock = c;
		init(d.getYear(),d.getMonth(),(d.getDay() + (hours / 24.0)));
	}

	/**
	 * Create a DataTime by specifying the clock, calendar date and the local time.
	 * @param c
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param minute
	 * @param second
	 */
	public DateTime(Clock c, int year, int month, int day, int hour, int minute, double second) {
		clock = c;
		if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0.0 || second >= 60.0) {
			throw new IllegalArgumentException("Invalid time: " + hour + ":" + minute + ":" + second);
		}
		init(year,month,(double)(day + (((((second / 60.0) + minute) / 60.0) + hour) / 24.0)));
	}
	
	/**
	 * Return the Julian day.
	 * @return The Julian day as a double.
	 */
	public double getJD() {
		return jd;
	}
	
	/**
	 * Return the clock associated with this DateTime.
	 * @return The clock associated with this DateTime.
	 */
	public Clock getClock() {
		return clock;
	}

	/**
	 * Return the Modified Julian day.
	 * @return The Modified Julian day as a double.
	 */
	public double getMJD() {
		return getMJD(jd);
	}

	/**
	 * Get the local date.
	 * @return
	 */
	public Date getDate() {
		double x = jd - clock.getConvertToUT() + 0.5; // Make the timezone and 12h UT adjustment.
		int Z = (int)x;
		double F = x - Z;
		int A = Z;
		int alpha = 0;
		if (Z >= 2299161) {
			alpha = (int)((Z - 1867216.25) / 36524.25);
			A = Z + 1 + alpha - (int)(alpha / 4);
		}
		int B = A + 1524;
		int C = (int)((B - 122.1) / 365.25);
		int D = (int)(365.25 * C);
		int E = (int)((B - D) / 30.6001);
		double day = B - D - (int)(30.6001 * E) + F;
		int month = (E < 14) ? E - 1 : E - 13;
		int year = (month > 2) ? C - 4716 : C - 4715;
		return new Date (year,month,(int)day);
	}
	
	/**
	 * Get the local time.
	 * @return
	 */
	public Time getTime() {
		double x = jd - clock.getConvertToUT() + 0.5; // Make the timezone and 12h UT adjustment.
		return new Time ((x - (int)x) * 24.0);
	}

	/**
	 * Return the day number of the week of this DateTime.
	 * The day numbers are 0-Sunday, 1-Monday, 2-Tuesday,
	 * 3-Wednesday, 4-Thursday, 5-Friday, and 6-Saturday.
	 */
	public int getDayOfWeek() {
		return ((int)(jd + 1.5)) % 7;
	}

	/**
	 * Return the day number of the year of this DateTime.
	 */
	public int getDayOfYear() {
		Date d = getDate();
		int year = d.getYear();
		int month = d.getMonth();
		int day = d.getDay();
		return ((275 * month) / 9) - 
			   ((isLeapYear(year) ? 1 : 2) * ((month + 9) / 12)) + 
			   day - 30;
	}

	/**
	 * Convert this DateTime to a string in FITS format that
	 * corresponds to the local time.
	 */
	public String toString() {
		Date d = getDate();
		Time t = getTime();
		StringBuffer s = new StringBuffer(d.getYear() + "-");
		int mm = d.getMonth();
		int dd = d.getDay();
		int hh = t.getHours();
		int min = t.getMinutes();
		double sec = t.getSeconds(); 
		if (mm < 10) s.append('0'); s.append(mm); s.append('-');
		if (dd < 10) s.append('0'); s.append(dd); s.append('T');
		if (hh < 10) s.append('0'); s.append(hh); s.append(':');
		if (min < 10) s.append('0'); s.append(min);  s.append(':');
		if (sec < 10.0) s.append('0'); s.append(sec);
		return s.toString();
	}

	/**
	 * Get the Universal Time.
	 * @return UT as a Time.
	 */
	public Time getUT() {
		double x = jd + 0.5;
		return new Time ((x - (int)x) * 24.0);
	}

	/**
	 * Return the local sideral time for this DateTime
	 * in hours and fractions of an hour.
	 * @return The local sideral time in hours.
	 */
	public double getLocalSiderealTime() {
		return getGreenwichMeanSiderealTime() - clock.getLongitudeInHours();
	}

	/**
	 * Return the Greenwich mean sideral time for this DateTime
	 * in hours and fractions of an hour.
	 * @return The Greenwich mean sideral time in hours.
	 */
	public double getGreenwichMeanSiderealTime() {
		double t0 = jd - 2451545.0;
		double t = t0 / 36525.0;
		double tt = t * t;
		double x = (280.46061837 + 
			       360.98564736629 * t0 + 
			       tt * (0.000387933 - (t / 38710000.0))) / 15.0 ;
		double y = Math.IEEEremainder(x,24.0);
		if (y < 0)
			y = 24.0 + y;	   
		return y;
	}
	
	/**
	 * Given a local sideral time, a date and a local clock, return the 
	 * local time that corresponds to this sideral time.
	 * @param lst local sideral time in hours
	 * @param d	the current date
	 * @param c the local clock
	 * @return
	 */
	static public DateTime lstToLocalTime(double lst, Date d, Clock c) {
		// (1) Find the Greenwich mean sideral time.
		double gmst = lst + c.getLongitudeInHours();
		// (2) For this date, calculate the sideral time at 0h UT.
		DateTime x = new DateTime(c,d.getYear(),d.getMonth(),(double)d.getDay());
		double T = (x.getJD() - 2451545.0) / 36525.0;
		double y = (100.46061837 + T * (36000.770053608 + T * (0.000387933 + T / 38710000))) / 15.0; 
		double theta = Math.IEEEremainder(y,24.0);
		if (theta < 0)
		theta = 24.0 + theta;	   
		// (3) Then calulate the Greenwich mean time for this Greenwich mean sideral time.
		double ut = (gmst - theta) / 1.00273790935;
		// (4) Finally, apply the time zone correction.
		ut -= c.getConvertToUT();
		System.out.println(">>> gmst " + gmst);
		System.out.println(">>> x " + x);
		System.out.println(">>> T " + T);
		System.out.println(">>> y " + y);
		System.out.println(">>> theta " + theta);
		System.out.println(">>> ut " + ut);
		x = new DateTime(c,d,ut);
		return x;
	}


	//////////////////////////////////////////////////////////////////	
	// All of the following static methods are used in the unit tests.
	//////////////////////////////////////////////////////////////////	

	private static void testLeap(int year) {
		System.out.println("The year " + year + " is " + (isLeapYear(year) ? "" : " not ") + " a leap year.");
	}
	private static void testJD(int y, int m, double d) {
		DateTime t = new DateTime(y,m,d);
		System.out.println("date " + y + "-" + m + "-" + d + "  \tjd = " + t.getJD());
	}
	private static void testJD(int y, int m, int d, int h, int mm, double s) {
		DateTime t = new DateTime(y,m,d,h,mm,s);
		System.out.println("date " + y + "-" + m + "-" + d +
			"T" + h + ":" + mm + ":" + s +  
			"  \tjd = " + t.getJD());
	}
	private static void testJD(Date d, Time t) {
		DateTime x = new DateTime(d,t);
		System.out.println("date " + d + " time " + t +
			"  \tjd = " + x.getJD());
	}
	private static void testDT(double jd) {
		DateTime x = new DateTime(jd);
		System.out.println("jd = " + jd + " date = " + x);
	}
	private static void testDN(int year, int month, int day) {
		String[] dayName = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
		};
		DateTime x = new DateTime(year,month,day);
		System.out.println("The day number of " + x + " is " + x.getDayOfYear() +
			" and it is " + dayName[x.getDayOfWeek()]);
	}
	public static void main(String[] args) {
		
		/*
		// For this test we'll use UT.
		DateTime.setDefaultClock(new Clock(0.0,0.0,0));
		System.out.println("Timezone is UT.");
		
		DateTime d = new DateTime (1987,4,10,19,21,0.0);
		System.out.println("Mean sideral time at Greenwich on " + d + " is " + 
			d.getGreenwichMeanSiderealTime() + " hours.");

		DateTime x = DateTime.lstToLocalTime(d.getGreenwichMeanSiderealTime(),d.getDate(),Clock.UT);
		System.out.println("x = " + x);

		d = new DateTime (1987,4,10.0);
		System.out.println("Mean sideral time at Greenwich on " + d + " is " + 
			d.getGreenwichMeanSiderealTime() + " hours.");		
		*/
		
		/*
		testLeap(2003);
		testLeap(2000);
		testLeap(2001);
		testLeap(2004);
		testLeap(1900);
		testLeap(2100);
		
		testJD(1957,10,4.81);
		testJD(2000,1,1.5);		
		testJD(1999,1,1.0);		
		testJD(1987,1,27.0);		
		testJD(1987,6,19.5);		
		testJD(1988,1,27.0);		
		testJD(1988,6,19.5);		
		testJD(1900,1,1.0);		
		testJD(1600,1,1.0);		
		testJD(1600,12,31.0);
		
		testJD(1970,1,1.0);
				
		testJD(1957,10,4,19,26,24.0);
		testJD(2000,1,1,12,0,0.0);		

		testJD(new Date(1957,10,4),new Time(19,26,24.0));
		testJD(new Date(2000,1,1),new Time(12,0,0.0));
		
		testJD(1858,11,17.0);		

		testDT(2436116.31);
		testDT(2451545.0);
		testDT(2451179.5);
		testDT(2446822.5);
		testDT(2446966.0);
		testDT(2447187.5);
		testDT(2447332.0);
		testDT(2415020.5);
		testDT(2305447.5);
		testDT(2305812.5);
		
		// Now use MDT.
		DateTime.setDefaultClock(Clock.DEFAULT);
		System.out.println("Timezone is MDT.");
		testJD(1957,10,4.81);
		testJD(2000,1,1.5);		
		testJD(1999,1,1.0);		
		testJD(1987,1,27.0);		
		testJD(1987,6,19.5);		
		testJD(1988,1,27.0);		
		testJD(1988,6,19.5);		
		testJD(1900,1,1.0);		
		testJD(1600,1,1.0);		
		testJD(1600,12,31.0);

		testDT(2436116.31);
		testDT(2451545.0);
		testDT(2451179.5);
		testDT(2446822.5);
		testDT(2446966.0);
		testDT(2447187.5);
		testDT(2447332.0);
		testDT(2415020.5);
		testDT(2305447.5);
		testDT(2305812.5);

		DateTime now = new DateTime();
		System.out.println("The current time is " + now);
		System.out.println("The UT is " + now.getUT());
		System.out.println("The day number is " + now.getDayOfYear());
		System.out.println("The day of the week is " + now.getDayOfWeek());
		
		testDN(2003,1,1);
		testDN(2003,3,1);
		testDN(2003,12,31);
		testDN(2004,3,1);
		testDN(2004,12,31);
		
		int y = 2003; System.out.println("TD for " + y + " is " + DateTime.getDTDiff(y));
		y = 2000; System.out.println("TD for " + y + " is " + DateTime.getDTDiff(y));
		y = 2005; System.out.println("TD for " + y + " is " + DateTime.getDTDiff(y));
		y = 2015; System.out.println("TD for " + y + " is " + DateTime.getDTDiff(y));
		*/
	}
}
