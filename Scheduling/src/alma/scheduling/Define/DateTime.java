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
 
package alma.scheduling.Define;


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
 * the timezone of the clock that provides the current time.  
 * One specifies the geographical coordinates of the clock, as well 
 * as the local timezone, via the "setClockCoordinates" static method. 
 * The default is zero degrees longitude and latitude.
 *  
 * <p>
 * The main reference used in crafting these methods is 
 * Astronomical Algorithms by Jean Meeus, second edition,
 * 2000, Willmann-Bell, Inc., ISBN 0-943396-61-1.  See
 * chapter 7, "Julian day", and chapter 12, "Sideral Time".
 * 
 * @version $Id: DateTime.java,v 1.10 2006/09/25 16:08:32 sslucero Exp $
 * @author Allen Farris
 */
public class DateTime {
	
    /**
      *
      */
    private static final long CONVERSION = 12219292800L;
    /**
      * The longitude in degrees West (i.e., degrees west of Greenwich are positive).
      */
	static private double longitudeInDegrees = 0.0;
    /**
      *
      */
	static private double longitudeInHours = 0.0;
    /**
      *
      */
	static private double longitude = 0.0;
    /**
	  * The latitude in degrees North.
      */
	static private double latitudeInDegrees = 0.0;
    /**
      *
      */
	static private double latitude = 0.0;
	/**
      * The number of hours between the local time zone and UT, i.e., localTime - UT.
      */
	static private int timeZone = 0;
	/**
      * The factor, in units of fractions of a day, that must be added to local time to get UT.
      */
	static private double convertToUT = 0.0;

	/**
	 * Set the coordinates of the clock associated with all DateTimes, by specifying the 
	 * geographical coordinates of the clock and the timezone.
	 * @param lng The longitude in degrees West (i.e., degrees west of Greenwich are positive).
	 * @param lat The latitude in degrees North.
	 * @param zone The local time zone relative to UT, i.e., the number of hours 
	 * between the local time zone and UT, i.e., localTime - UT.  If the local time zone
	 * is ahead of Greenwich mean time, then zone should be positive.  If the local 
	 * time zone is behind Greenwich mean time, then zone should be negative.  
	 * For example, 9:00 Mountain Daylight Time is 15:00 UT, so if the local time is 
	 * MDT, zone is -6;
	 */
	static public void setClockCoordinates(double lng, double lat, int zone) {
		longitudeInDegrees = lng;
		longitudeInHours = lng / 15.0;
		longitude = Math.toRadians(lng);
		latitudeInDegrees = lat;
		latitude = Math.toRadians(lat);
		timeZone = zone;
		convertToUT = -timeZone / 24.0;
	}


	/**
	 * Return true if the specified year is a leap year.
	 * @param year the year in the Gregorian calendar.
	 * @return true if the specified year is a leap year.
	 */
	static public boolean isLeapYear(int year) {
		if (year % 4 != 0) {
			return false;
        }
		if (year % 100 == 0 && year % 400 != 0) {
			return false;
        }
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
		dt.add(days);
		return dt;
	}

	/**
	 * Add the specified number of seconds to this DateTime.
	 * @param days the number of seconds to be added.
	 */
	static public DateTime add(DateTime t, int seconds) {
		DateTime dt = new DateTime(t);
		dt.add(seconds);
		return dt;
	}
	
	/**
	 * Add the specified number of seconds to this DateTime.
	 * @param days the number of seconds to be added.
	 */
	static public DateTime add(DateTime t, TimeInterval interval) {
		DateTime dt = new DateTime(t);
		dt.add(interval);
		return dt;
	}
	
	/**
	 * Create a DateTime from the current system time, as obtained from 
	 * the Java System.currentTimeMillis().
	 * @return The current system time.
	 */
	static public DateTime currentSystemTime() {
		return new DateTime(System.currentTimeMillis());
	}

    /**
     * Given a unix time as a long, converts it to ACS Time using
     * the constant '12219292800'.
     *
     * @param long The unix time to convert.
     * @return long The acs time.
     */
    static public long unixToAcs(long unixTime) {
        long acsTime = (unixTime + (CONVERSION * 1000) * 10000);
        System.out.println("SCHEDULING: ACS TIME = "+acsTime);
        return acsTime;
    }

    /*
    static public long acsToUnix(long acsTime) {
        //long unixTime = acsTime / ((CONVERSION * 1000) * 10000);
        long unixTime = acsTime - (CONVERSION / 10000000);
        return unixTime;
    }*/
	
	/**
	 * Internally, time is kept in the form of the Julian day, 
	 * including fractions thereof.  the Julian day is the continuous 
	 * time since the beginning of the year -4712.  By tradition the
	 * Julian day starts at 12 hours UT.
	 */
	private double jd;

    /**
	 * Create a DateTime by specifying the number of milliseconds since
	 * midnight, January 1, 1970 UTC (which is what the Java System
	 * method "long currentTimeMillis()" returns.
	 * @param millisec The number of milliseconds since
	 * midnight, January 1, 1970 UTC.
	 */
	public DateTime(long millisec) {
		// 2440587.5 is the Julian date of Jan. 1, 1970, midnight UTC.
		this(millisec / 86400000.0 + 2440587.5);	
	}

	/**
	 * Create a DateTime by specifying the Julian day.
	 * @param julianDay the Julian day, including fractions thereof.
	 */
	public DateTime(double julianDay) {
		jd = julianDay;
	}

	// The two "init" methods are used by constructors.
	private void init(int year, int month, double day) {
		// For this algorithm see Meeus, chapter 7, p. 61.
		int iday = (int)day;
        if (day > 0.0 && day < 1.0) {
            iday = 1;
        }
		if (month < 1 || month > 12) {
			throw new IllegalArgumentException ("Illegal value of month: " 
				+ year + "-" + month + "-" + day);
        }
		if ( (iday < 1 || iday > 31) ||
			 ((month == 4 || month == 6 || month == 9 || month == 11) && iday > 30) ||
			 (month == 2 && (iday > ((isLeapYear(year) ? 29 : 28)))) ) {
			throw new IllegalArgumentException ("Illegal value of day: "
				+ year + "-" + month + "-" + day);
        }
		if (month <= 2) {
			--year;
			month += 12;
		}
		int A = year / 100;
		int B = 2 - A + (A / 4);
		// Apply the time zone correction.
		day += convertToUT;
		jd = (int)(365.25 * (year + 4716)) + (int)(30.6001 * (month + 1)) + day + B - 1524.5;
	}

	private void init(int year, int month, int day, int hour, int minute, double second) {
		if (hour < 0 || hour > 23 || minute < 0 
                || minute > 59 || second < 0.0 || second >= 60.0) {
			throw new IllegalArgumentException("Invalid time: " + hour + ":" + minute + ":" + second);
		}
		init(year,month,(double)(day + (((((second / 60.0) + minute) / 60.0) + hour) / 24.0)));
	}

	/**
	 * Create a default DateTime, initialized to a Julian date of 0.0.
	 *
	 */
	public DateTime() {
		jd = 0.0;
	}
	
	/**
	 * Create a DateTime by specifying the year, month, and day plus the fraction of a day.
	 * @param year
	 * @param month
	 * @param day the day (and time in the local time zone)
	 */
	public DateTime(int year, int month, double day) {
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
		if (t.length() < 19 || t.charAt(4) != '-' || t.charAt(7) != '-' || 
			(t.charAt(10) != 'T' && t.charAt(10) != ' ') || 
			t.charAt(13) != ':' || t.charAt(16) != ':') {

			throw new IllegalArgumentException("Invalid time format: " + t);
        }
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
		jd = dt.jd;
	}

	/**
	 * Create a DateTime by specifying the Date and the number of hours,
	 * including fractions.
	 * @param d The date.
	 * @param hours The number of hours.
	 */
	public DateTime(Date d, double hours) {
		init(d.getYear(),d.getMonth(),(d.getDay() + (hours / 24.0)));
	}

	/**
	 * Return the Julian day.
	 * @return The Julian day as a double.
	 */
	public double getJD() {
		return jd;
	}
	
	/**
	 * Get the number of milliseconds since
	 * midnight, January 1, 1970 UTC (which is what the Java System
	 * method "long currentTimeMillis()" returns.
	 * @return The number of milliseconds since
	 * midnight, January 1, 1970 UTC
	 */
	public long getMillisec() {
		// 2440587.5 is the Julian date of Jan. 1, 1970, midnight UTC.
		return (long)((jd - 2440587.5) * 86400000.0 + 0.5);
	}
	
	/**
	 * Compare this DateTime to the specified DateTime, returning -1, 0,
	 * or +1 if this time is less than, equal to, or greater than the
	 * specified time.
	 * @param dt The DateTime to which this time is being compared.
	 * @return -1, 0, or +1 if this time is less than, equal to, or 
	 * greater than the specified time.
	 */
	public int compareTo(DateTime dt) {
		if (jd < dt.jd) {
			return -1;
        }
		if (jd > dt.jd) {
			return 1;
        }
		return 0;
	}
	
	/**
	 * Return true if and only if this time is equal to the specified time.
	 * @param dt The DateTime to which this time is being compared.
	 * @return True, if and only if this time is equal to the specified time.
	 */
	public boolean eq(DateTime dt) {
		return jd == dt.jd ? true : false; 
	}
	/**
	 * Return true if and only if this time is not equal to the specified time.
	 * @param dt The DateTime to which this time is being compared.
	 * @return True, if and only if this time is not equal to the specified time.
	 */
	public boolean ne(DateTime dt) {
		return jd == dt.jd ? true : false; 
	}
	/**
	 * Return true if and only if this time is less than the specified time.
	 * @param dt The DateTime to which this time is being compared.
	 * @return True, if and only if this time is less than the specified time.
	 */
	public boolean lt(DateTime dt) {
		return jd < dt.jd ? true : false; 
	}
	/**
	 * Return true if and only if this time is less than or equal to the specified time.
	 * @param dt The DateTime to which this time is being compared.
	 * @return True, if and only if this time is less than or equal to the specified time.
	 */
	public boolean le(DateTime dt) {
		return jd <= dt.jd ? true : false; 
	}
	/**
	 * Return true if and only if this time is greater than the specified time.
	 * @param dt The DateTime to which this time is being compared.
	 * @return True, if and only if this time is greater than the specified time.
	 */
	public boolean gt(DateTime dt) {
		return jd > dt.jd ? true : false; 
	}
	/**
	 * Return true if and only if this time is greater than or equal to the specified time.
	 * @param dt The DateTime to which this time is being compared.
	 * @return True, if and only if this time is greater than or equal to the specified time.
	 */
	public boolean ge(DateTime dt) {
		return jd >= dt.jd ? true : false; 
	}
	
	/**
	 * return true if the value of this DateTime is zero.
	 * @return true if the value of this DateTime is zero
	 */
	public boolean isNull() {
		return jd == 0.0;
	}
	
	/**
	 * Add the specified number of days (and fractions thereof) to
	 * this DateTime.
	 * @param days the number of days to be added.
	 */
	public void add(double days) {
		jd += days;
	}
	
	/**
	 * Add the specified number of seconds to this DateTime.
	 * @param days the number of seconds to be added.
	 */
	public void add(int seconds) {
		jd += (double)seconds / 86400.0;
	}
	
	/**
	 * Add the specified number of seconds to this DateTime.
	 * @param days the number of seconds to be added.
	 */
	public void add(TimeInterval interval) {
		jd += (double)(interval.getLength()) / 86400.0;
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
		// For this algorithm see Meeus, chapter 7, p. 63.
		double x = jd - convertToUT + 0.5; // Make the timezone and 12h UT adjustment.
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
		double x = jd - convertToUT + 0.5; // Make the timezone and 12h UT adjustment.
		return new Time ((x - (int)x) * 24.0);
	}

	/**
	 * Get the local time in hours.
	 * @return
	 */
	public double getTimeOfDay() {
		double x = jd - convertToUT + 0.5; // Make the timezone and 12h UT adjustment.
		return (x - (int)x) * 24.0;
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
		int yy = d.getYear();
		int mm = d.getMonth();
		int dd = d.getDay();
		int hh = t.getHours();
		int min = t.getMinutes();
		int sec = (int)(t.getSeconds() + 0.5);
		if (sec == 60) {
			sec = 0;
			++min;
			if (min == 60) {
				min = 0;
				++hh;
				if (hh == 24) {
					hh = 0;
					++dd;
					if ( ((mm == 4 || mm == 6 || mm == 9 || mm == 11) && dd > 30) ||
						 (mm == 2 && (dd > (DateTime.isLeapYear(yy) ? 29 : 28))) ||
						 (dd > 31) ) {
						dd = 1;
						++mm;
						if (mm > 12) {
							mm = 1;
							++yy;
						}
					}
				}
			}
		}
		StringBuffer s = new StringBuffer(yy + "-");
		if (mm < 10)  {
            s.append('0'); 
        }
        s.append(mm); 
        s.append('-');
		if (dd < 10) {
            s.append('0'); 
        }
        s.append(dd); 
        s.append('T');
		if (hh < 10) { 
            s.append('0'); 
        }
        s.append(hh); 
        s.append(':');
		if (min < 10) { 
            s.append('0'); 
        }
        s.append(min);  
        s.append(':');
		if (sec < 10.0) { 
            s.append('0'); 
        }
        s.append(sec);
		return s.toString();
	}

	/**
	 * Return the time of day as a string.
	 */
	public String timeOfDayToString() {
		Time t = getTime();
		StringBuffer s = new StringBuffer();
		int hh = t.getHours();
		int min = t.getMinutes();
		int sec = (int)(t.getSeconds()); 
		if (sec == 60) {
			sec = 0;
			++min;
			if (min == 60) {
				min = 0;
				++hh;
				if (hh == 24) {
					hh = 0;
				}
			}
		}
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
        /*
		double x = getGreenwichMeanSiderealTime() + longitudeInHours;
            */
        double x = getGreenwichMeanSiderealTime() + longitudeInDegrees/15.0;
        if(x<0)
                x+= 24.0;
        if(x> 24.0)
            x-= 24.0;
        return x;
                
	}

	/**
	 * Return the Greenwich mean sideral time for this DateTime
	 * in hours and fractions of an hour.
	 * @return The Greenwich mean sideral time not in hours.
	 */
	public double getGreenwichMeanSiderealTime() {
        //convert jd to jd in UT
        /*
        System.out.println("time zone = "+timeZone);
        double jdut = jd + convertToUT; //- (timeZone/24.0);
		double t0 = jdut - 2451545.0;
		double t = t0 / 36525.0;
        //double t = (jd - 2451545.0) / 36525.0;
		double tt = t * t;
		double ttt = t * t * t;
		double x = (280.46061837 + 
			       360.98564736629 * t0 + 
			       tt * (0.000387933 - (ttt / 38710000.0)));// / 15.0 ;
        //double y = (x * 24.0) / 360.0;
        */
        double MJD = getMJD();
        double MJD_0 = Math.floor(MJD);
        double UT = 86400.0 * (MJD - MJD_0);
        double T_0 = (MJD_0 -51544.5)/36525.0;
        double T = (MJD - 51544.5)/36525.0;
        double x = 24110.54841+8640184.812866* T_0 + 1.0027379093 * UT +
            (0.093104 - 0.0000062 * T)*T*T;
		double y =Math.IEEEremainder(x,86400.0);
        if(y <0) {
            y+=86400.0;
        }
        double hours = y / 3600;
        return hours;
        /*
		double y = Math.IEEEremainder(x,24.0);
		if (y < 0)
			y = 24.0 + y;	   
        return y;
        */
	}
	
	//*/
	// * Given a local sideral time, a date and a local clock, return the 
	// * local time that corresponds to this sideral time.
	// * @param lst local sideral time in hours
	// * @param d	the current date
	// * @param c the local clock
	// * @return
	//*/
	static public DateTime lstToLocalTime(double lst, Date d) {
		// (1) Find the Greenwich mean sideral time.
		double gmst = lst - longitudeInHours;

		// (2) For this date, calculate the sideral time at 0h UT.
		DateTime x = new DateTime(d.getYear(),d.getMonth(),(double)d.getDay());
		double t0 = x.getJD() - 2451545.0;
		double T = (t0) / 36525.0;
		double TT = T * T;
		//double y = (100.46061837 + T * (36000.770053608 + T * (0.000387933 - T / 38710000.0))) / 15.0;
		double y = (280.46061837 + 
			360.98564736629 * t0 + 
			TT * (0.000387933 - (T / 38710000.0))) / 15.0 ; 
		double theta = Math.IEEEremainder(y,24.0);
		if (theta < 0)
		theta = 24.0 + theta;	   
		// (3) Then calulate the Greenwich mean time for this Greenwich mean sideral time.
		double ut = (gmst - theta) / 1.00273790935;
		// (4) Finally, apply the time zone correction.
        //System.out.println("Converting to UT with "+convertToUT);
		//ut -= convertToUT;
		x = new DateTime(d,ut);
		return x;
	}
    
	static public void main(String[] args) {
		//DateTime.setClockCoordinates(-106.905917, 34.068693, -6);
		//DateTime.setClockCoordinates(107.6177275, 34.0787491666667,-7);
		//DateTime.setClockCoordinates(-60.0, -23.02271113, -1);
		//DateTime.setClockCoordinates(-67.7543628, -23.02271113, -6); //2 hours away from CHILE..
		DateTime.setClockCoordinates(-67.7543628, -23.02271113, -4); //CHILE..

        DateTime start = new DateTime("2006-09-21T15:56:00");
        System.out.println("Original DT = "+start.toString());
        System.out.println();
        System.out.println("DT's JD = "+start.getJD());
        System.out.println("DT's MJD = "+start.getMJD());
        double lst = start.getLocalSiderealTime();
        System.out.println();
        System.out.println("Lst: "+lst+ ": should be about 15:27"); 
        Date d = start.getDate();
        System.out.println("Orig's Date: "+d.toString());
        DateTime newDT = DateTime.lstToLocalTime(lst, d);
        System.out.println("New DT = "+newDT.toString());
        /*
        DateTime end = new DateTime("2006-01-02T22:18:42");
        System.out.println("Start: "+start.toString());
        System.out.println("End: "+end.toString());
        double diff = DateTime.difference(end, start);
        System.out.println("Difference between two (in JD) = "+diff);
        System.out.println("In hours = "+ diff*24.0);
        System.out.println("In minutes = "+ diff*24.0*60.0);
        System.out.println("In seconds = "+ diff*24.0*60.0*60.0);
        */
        /*
		long currentSystemTime = System.currentTimeMillis();
		DateTime current  = new DateTime(currentSystemTime);// / 86400000.0 + 2440587.5);
		System.out.println("The current time, as a DateTime is " + current.toString());
		System.out.println("The currrent system time is " + currentSystemTime);
        
		
		double lst = current.getLocalSiderealTime();
		System.out.println("lst = " + lst);
		
		Date day = new Date (2006,8,14);
		DateTime x = DateTime.lstToLocalTime(lst,day);
		System.out.println("Lst to local = " + x);
        */
	}
    
	/**
	 * Get the conversion factor that converts local time to UT, i.e.
	 * the factor, in units of fractions of a day, that must be added 
	 * to local time to get UT. 
	 * @return The factor, in units of fractions of a day, that must be added 
	 * to local time to get UT.
	 */
	public static double getConvertToUT() {
		return convertToUT;
	}

	/**
	 * Get the local latitude in radians.
	 * @return The local latitude in radians.
	 */
	public static double getLatitude() {
		return latitude;
	}

	/**
	 * Get the local latitude in degrees.
	 * @return The local latitude in degrees.
	 */
	public static double getLatitudeInDegrees() {
		return latitudeInDegrees;
	}

	/**
	 * Get the local longitude in radians.
	 * @return The local longitude in radians.
	 */
	public static double getLongitude() {
		return longitude;
	}

	/**
	 * Get the local longitude in degrees.
	 * @return The local longitude in degrees.
	 */
	public static double getLongitudeInDegrees() {
		return longitudeInDegrees;
	}

	/**
	 * Get the local longitude in hours.
	 * @return The local longitude in lours.
	 */
	public static double getLongitudeInHours() {
		return longitudeInHours;
	}

	/**
	 * Get the local timezone, i.e., the number of hours between the 
	 * local time zone and UT, i.e., localTime - UT.
	 * @return The number of hours between the local time zone and 
	 * UT, i.e., localTime - UT.
	 */
	public static int getTimeZone() {
		return timeZone;
	}

}
