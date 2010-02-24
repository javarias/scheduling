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
 * "@(#) $Id: TimeUtil.java,v 1.1 2010/02/24 20:58:05 rhiriart Exp $"
 */
package alma.scheduling.algorithm.astro;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeUtil {

    private static Logger logger = LoggerFactory.getLogger(TimeUtil.class);

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
     * Get the Greenwich Mean sidereal time (GST).
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
     * Get Local Sidereal Time (LST).
     * @param gst Greenwich Sidereal Time, in decimal hours
     * @param longitude Longitude, in degrees
     * @param longDir Longitude direction, either 'E' or 'W'
     * @return LST, in hours
     */
    public static double getLocalSiderealTime(double gst, double longitude, char longDir) {
        double lst = gst;
        double tdiff = longitude / 15.0;
        if (longDir == 'W') {
            lst = lst - tdiff;
        } else if (longDir == 'E') {
            lst = lst + tdiff;
        }
        if (lst > 24.0) {
            lst = lst - 24.0;
        }
        if (lst < 0) {
            lst = lst + 24.0;
        }
        return lst;
    }
    
    public static double toDecimalHours(int hours, int minutes, double seconds) {
        return hours + ( minutes + ( seconds / 60.0 ) ) / 60.0;
    }
    
}
