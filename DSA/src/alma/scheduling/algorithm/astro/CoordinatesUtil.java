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
 * "@(#) $Id: CoordinatesUtil.java,v 1.2 2010/03/10 00:16:02 rhiriart Exp $"
 */
package alma.scheduling.algorithm.astro;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.obsproject.FieldSourceObservability;
import alma.scheduling.datamodel.obsproject.HorizonCoordinates;
import alma.scheduling.datamodel.obsproject.SkyCoordinates;

/**
 * Coordinate transformation utilities.
 * 
 */
public class CoordinatesUtil {

    private static Logger logger = LoggerFactory.getLogger(CoordinatesUtil.class);
    
    /**
     * Get the hour angle of a given sky coordinate.
     * 
     * @param ut Time (UT)
     * @param ra Right ascension (decimal hours)
     * @param longitude Geographic longitude (degrees, 'E' is positive, 'W' negative)
     * @return hour angle (decimal hours)
     */
    public static double getHourAngle(Date ut, double ra, double longitude) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UT"));
        cal.setTime(ut);
        double hours = TimeUtil.toDecimalHours(cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND) / 1000.0);
        double gst = TimeUtil.getGreenwichMeanSiderealTime(cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), hours);
        double lst = TimeUtil.getLocalSiderealTime(gst, longitude);
        double ha = lst - ra;
        if (ha < 0) ha += 24;
        return ha;
    }
    
    /**
     * Transform equatorial coordinates to horizon.
     * @param eq Equatorial coordinates
     * @param ut Time (UT)
     * @param latitude Latitude (degrees)
     * @param longitude Longitude (degrees)
     * @return horizon coordinates
     */
    public static HorizonCoordinates equatorialToHorizon(SkyCoordinates eq, Date ut, double latitude,
            double longitude) {
        double ha = getHourAngle(ut, eq.getRA() / 15.0, longitude);
        logger.debug("hour angle = " + ha + " (hours)");
        ha = ha * 15.0; // from decimal hours to degrees
        logger.debug("hour angle = " + ha + " (degrees)");
        double sina = Math.sin(Math.toRadians(eq.getDec())) * Math.sin(Math.toRadians(latitude)) +
            Math.cos(Math.toRadians(eq.getDec())) * Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(ha));
        logger.debug("sina = " + sina);
        double a = Math.toDegrees(Math.asin(sina)); // altitude, in degrees
        logger.debug("a = " + a + " (degrees)");
        double cosA = ( Math.sin(Math.toRadians(eq.getDec())) - Math.sin(Math.toRadians(latitude)) * sina ) /
            ( Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(a)) );
        logger.debug("cosA = " + cosA);
        double A = Math.toDegrees(Math.acos(cosA));
        logger.debug("A' = " + A + " (degrees)");
        double sinH = Math.sin(Math.toRadians(ha));
        if (sinH >= 0) {
            A = 360.0 - A;
        }
        logger.debug("A = " + A + " (degrees)");
        return new HorizonCoordinates(A, a);
    }
    
    /**
     * Get rising and setting times and angles.
     * 
     * @param coords Equatorial coordinates
     * @param latitude Latitude (degrees, North is positive, South negative)
     * @param longitude Longitude (degrees)
     * @param year Year
     * @param month Month (1-12)
     * @param day Day of month (1-31)
     * @return field source observability parameters
     */
    public static FieldSourceObservability getRisingAndSettingParameters(SkyCoordinates coords,
            double latitude, double longitude) {
        
        FieldSourceObservability fso = new FieldSourceObservability();
        fso.setAlwaysHidden(false);
        fso.setAlwaysVisible(false);
        if (latitude > 0) {
            // north hemisphere
            if (coords.getDec() > (90.0 - latitude)) {
                // source is always visible
                fso.setAlwaysVisible(true);
                return fso;
            }
            if (coords.getDec() < -(90.0 - latitude)) {
                // source is always hidden
                fso.setAlwaysHidden(true);
                return fso;
            }
        } else {
            // south hemisphere
            if (coords.getDec() < -(90.0 + latitude)) {
                // source is always visible
                fso.setAlwaysVisible(true);
                return fso;
            }
            if (coords.getDec() > (90.0 + latitude)) {
                // source is always hidden
                fso.setAlwaysHidden(true);
                return fso;
            }
        }
        
        double cosAr = Math.sin(Math.toRadians(coords.getDec())) /
                       Math.cos(Math.toRadians(latitude));
        // if (cosAr > 1 || cosAr < -1)
        //     return null; // should never happen
        double Ar = Math.toDegrees(Math.acos(cosAr));
        double As = 360.0 - Ar;
        
        double raHours = coords.getRA() / 15.0;
        double tmp = - Math.tan(Math.toRadians(coords.getDec())) * Math.tan(Math.toRadians(latitude));
        // if (tmp > 1 || tmp < -1)
        //     return null; // should never happen
        double H = Math.toDegrees(Math.acos(tmp)) / 15.0;
        double LSTr = 24.0 + raHours - H;
        if (LSTr > 24.0 ) {
            LSTr -= 24.0;
        }
        double LSTs = raHours + H;
        if (LSTs > 24.0) {
            LSTs -= 24.0;
        }
        double r = TimeUtil.lstToGST(LSTr, longitude);
        double s = TimeUtil.lstToGST(LSTs, longitude);
        
//        Date utRising = TimeUtil.gstToUT(r, year, month, day);
//        Date utSetting = TimeUtil.gstToUT(s, year, month, day);
//        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UT"));
//        cal.setTime(utRising);
//        double risingHour = TimeUtil.toDecimalHours(cal.get(Calendar.HOUR_OF_DAY),
//                cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND) / 100.0); 
//        cal.setTime(utSetting);
//        double settingHour = TimeUtil.toDecimalHours(cal.get(Calendar.HOUR_OF_DAY),
//                cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND) / 100.0); 
        
        fso.setAzimuthRising(Ar);
        fso.setAzimuthSetting(As);
        fso.setRisingTime(r);
        fso.setSettingTime(s);
        return fso;
    }
}
