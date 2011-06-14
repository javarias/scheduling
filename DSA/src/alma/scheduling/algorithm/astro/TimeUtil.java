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
 * "@(#) $Id: TimeUtil.java,v 1.7 2011/06/14 16:32:15 javarias Exp $"
 */
package alma.scheduling.algorithm.astro;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.common.gui.components.astrotime.AstroTime;

public class TimeUtil {

    private static Logger logger = LoggerFactory.getLogger(TimeUtil.class);
    
    private final static DateFormat format = 
        new SimpleDateFormat("'['yyyy-MM-dd'T'HH:mm:ss'] '");
    
    public static double getJulianDate(Date ut) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UT"));
        cal.setTime(ut);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        double day = cal.get(Calendar.DAY_OF_MONTH);
        day = day + ( cal.get(Calendar.HOUR_OF_DAY) / 24.0 );
        day = day + ( cal.get(Calendar.MINUTE ) / ( 24.0 * 60.0 ) );
        day = day + ( cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND) / 1000.0 ) / ( 24.0 * 3600.0 );
        logger.debug("year = " + year);
        logger.debug("month = " + month);
        logger.debug("day = " + day);
        return getJulianDate(year, month, day);
    }
    
    public static double getJulianDate(int year, int month, double day) {
        if ( month == 1 || month == 2 ) {
            year = year - 1;
            month = month + 12;
        }
        logger.debug("year = " + year);
        logger.debug("month = " + month);
        int B = 0;
        if ( year >= 1582 || month >= 10 || day >= 15) {
            int A = year / 100;
            B = 2 - A + (int) (A/4);
            logger.debug("A = " + A);
        }
        logger.debug("B = " + B);
        int C;
        if (year < 0) {
            C = (int) ( ( 365.25 * year ) - 0.75 );
        } else {
            C = (int) ( 365.25 * year );
        }
        logger.debug("C = " + C);
        int D = (int) ( 30.6001 * (month + 1) );
        logger.debug("D = " + D);
        double JD = B + C + D + day + 1720994.5;
        logger.debug("JD = " + JD);
        return JD;
    }
    
    /**
     * 
     * @deprecated Instead use {@link AstroTime#getGMST(Date)}
     * 
     * Get the Greenwich Mean sidereal time (GST) from UT.
     * 
     * @param year Year
     * @param month Month (1-12)
     * @param day Day of month, starting from 1
     * @param hour Decimal hours
     * @return GST, as decimal hours
     */
    public static double getGreenwichMeanSiderealTime(int year, int month, int day, double hour) {
        double jd = getJulianDate(year, month, day);
        logger.debug("JD = " + jd);
        double s = jd - 2451545.0;
        double t = s / 36525.0;
        double t0 = 6.697374558 + 2400.051336 * t + 0.000025862 * t * t;
        logger.debug("t0 = " + t0);
        t0 = t0 % 24.0;
        logger.debug("t0 = " + t0);
        double ut = hour; // already in decimal hours
        ut = ut * 1.002737909;
        double gst = ( ut + t0 ) % 24.0;
        return gst;
    }
    
    /**
     * 
     * @param date Time to transform
     * @param longitude  Longitude (degrees, 'E' is positive and 'W' is negative)
     * @return LST, in hours
     */
    
    public static double getLocalSiderealTime(Date date, double longitude) {
    	//The AstroUtils already consider the longitude as negative
    	return AstroTime.getLST(date, -longitude);
    }
    
    /** 
     * @deprecated instead use {@link #getLocalSiderealTime(Date, double) }
     * Get Local Sidereal Time (LST).
     * @param gst Greenwich Sidereal Time, in decimal hours
     * @param longitude Longitude (degrees, 'E' is positive and 'W' is negative)
     * @return LST, in hours
     */
    public static double getLocalSiderealTime(double gst, double longitude) {
        double lst = gst;
        double tdiff = longitude / 15.0;
        lst = lst + tdiff;
        if (lst > 24.0) {
            lst = lst - 24.0;
        }
        if (lst < 0) {
            lst = lst + 24.0;
        }
        return lst;
    }

    // TODO change names

    public static double gstToLST(double gst, double longitude) {
        return getLocalSiderealTime(gst, longitude);
    }
    
    public static double lstToGST(double lst, double longitude) {
        double gst = lst;
        double longHours = longitude / 15.0;
        gst = gst - longHours;
        if (gst > 24.0) {
            gst -= 24.0;
        }
        if (gst < 0) {
            gst += 24.0;
        }
        return gst;
    }
    
    /**
     * @deprecated Instead use {@link AstroTime#getGMST(Date)}
     */
    public static double utToGST(Date ut) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UT"));
        cal.setTime(ut);
        double hour = cal.get(Calendar.HOUR_OF_DAY) +
            cal.get(Calendar.MINUTE) / 60.0 + cal.get(Calendar.SECOND) / 3600.0 +
            cal.get(Calendar.MILLISECOND) / 3600000.0;
        return getGreenwichMeanSiderealTime(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH), hour);
    }
    
    public static Date gstToUT(double gst, int year, int month, int day) {
        double jd = getJulianDate(year, month, day);
        logger.debug("JD = " + jd);
        double s = jd - 2451545.0;
        double t = s / 36525.0;
        double t0 = 6.697374558 + 2400.051336 * t + 0.000025862 * t * t;
        logger.debug("t0 = " + t0);
        t0 = t0 % 24;
        gst = ( gst - t0 ) % 24;
        double ut = gst * 0.9972695663;
        logger.debug("UT = " + ut);
        Calendar cal= Calendar.getInstance(TimeZone.getTimeZone("UT"));
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        int hours = (int) ut;
        double tmp = (ut - hours) * 60.0;
        int minutes = (int) tmp;
        tmp = (tmp - minutes) * 60.0;
        int seconds = (int) tmp;
        tmp = (tmp - seconds) * 1000.0;
        int milliseconds = (int) tmp;
        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, seconds);
        cal.set(Calendar.MILLISECOND, milliseconds);
        return cal.getTime();
    }
    
    public static double toDecimalHours(int hours, int minutes, double seconds) {
        return hours + ( minutes + ( seconds / 60.0 ) ) / 60.0;
    }
    
    public static String getUTString(Date ut) {
        format.setTimeZone(TimeZone.getTimeZone("UT"));
        return format.format(ut);
    }
    
    public static double getDaysFrom1990(Date date){
        Calendar cal= Calendar.getInstance(TimeZone.getTimeZone("UT"));
        cal.setTime(date);
        double days = 0;
        if(cal.get(Calendar.YEAR) >= 1990)
            days = cal.get(Calendar.YEAR) - 1990;
        else
            days = 1990 - cal.get(Calendar.YEAR) - 1;
        days = days * 365;
        if(cal.get(Calendar.YEAR) >= 1990) {
            days += cal.get(Calendar.DAY_OF_YEAR) - 1;
            days += cal.get(Calendar.HOUR_OF_DAY)/24.0 + cal.get(Calendar.MINUTE)/1440.0 + cal.get(Calendar.SECOND)/86400.0; 
        }
        else{
            if (cal.get(Calendar.YEAR) % 4 == 0)
                days += 366.0 - (cal.get(Calendar.DAY_OF_YEAR) + cal.get(Calendar.HOUR_OF_DAY)/24.0 + cal.get(Calendar.MINUTE)/1440.0 + cal.get(Calendar.SECOND)/86400.0);
            else
                days += 365.0 - (cal.get(Calendar.DAY_OF_YEAR) + cal.get(Calendar.HOUR_OF_DAY)/24.0 + cal.get(Calendar.MINUTE)/1440.0 + cal.get(Calendar.SECOND)/86400.0);
        }
        days += getLeapYearDaysfrom1990(date);
        if(cal.get(Calendar.YEAR) % 4 == 0 && cal.get(Calendar.MONTH) < Calendar.MARCH)
            days--;
        if (! (cal.get(Calendar.YEAR) >= 1990))
            return -days;
        return days;
    }
    
    public static int getLeapYearDaysfrom1990(Date date){
        Calendar cal= Calendar.getInstance(TimeZone.getTimeZone("UT"));
        cal.setTime(date);
        double years = 0.0;
        if(cal.get(Calendar.YEAR) > 1990)
            years = cal.get(Calendar.YEAR) - 1990;
        else
            years = 1990 - cal.get(Calendar.YEAR);
        years = years / 4.0;
        if((years - (int)years) >= 0.5 )
            if(!(cal.get(Calendar.YEAR) % 4 == 0 && cal.get(Calendar.MONTH) > Calendar.FEBRUARY))
                years++;
        return (int)years;
    }
}
