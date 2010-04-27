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
 * "@(#) $Id: CoordinatesUtil.java,v 1.7 2010/04/27 22:10:04 javarias Exp $"
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
     * Get the right ascencion of a given sky coordinate.
     * 
     * @param ut Time (UT)
     * @param ha Hour Angle (decimal hours)
     * @param longitude Geographic longitude (degrees, 'E' is positive, 'W' negative)
     * @return right ascencion (decimal hours)
     */
    public static double getRA(Date ut, double ha, double longitude) {
    	Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UT"));
    	cal.setTime(ut);
    	double hours = TimeUtil.toDecimalHours(cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND) / 1000.0);
    	double gst = TimeUtil.getGreenwichMeanSiderealTime(cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), hours);
    	double lst = TimeUtil.getLocalSiderealTime(gst, longitude);
    	double ra = lst - ha;
    	if ( ra < 0 ) ra +=24;
    	return ra;
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
    
    public static SunAstroData getRADecSun(Date ut){
        double days = TimeUtil.getDaysFrom1990(ut);
        double n = 360.0/365.242191 * days;
        double Eg = 279.403303;
        if(n < 0)
            n = n + (((int)((-n) / 360) + 1) * 360.0);
        else if(n > 360)
            n = n - ((int)( n / 360) * 360.0);
        double m = n + Eg - 282.768422;
        if(m < 0)
            m += 360;
        double ec = 360.0/Math.PI * 0.016713 * Math.sin(m * Math.PI / 180.0);
        double Lg = n + ec + Eg;
        if(Lg > 360)
            Lg -= 360;
        double T = (TimeUtil.getJulianDate(ut) - 2451545.0) / 36525.0;
        double dE = 46.815 * T + 0.0006 * Math.pow(T, 2) + 0.00181 * Math.pow(T, 3);
        dE = Math.abs(dE);
        dE = dE / 3600;
        double E = 23.439292 - dE;
        double Bg = 0.0;
        double x = Math.cos(Lg * Math.PI / 180.0);
        double y = Math.sin(Lg * Math.PI / 180.0) * Math.cos(E * Math.PI / 180.0) 
        - Math.tan(Bg * Math.PI / 180.0) * Math.sin(Eg * Math.PI / 180.0);
        double ra = Math.atan(y/x);
        double dec = Math.sin(Bg * Math.PI / 180.0) * Math.cos(E * Math.PI / 180.0);
        dec += Math.cos(Bg * Math.PI / 180.0) * Math.sin(E * Math.PI / 180.0) * Math.sin(Lg * Math.PI / 180.0);
        dec = Math.asin(dec);
        ra = ra * 180 / Math.PI;
        if( ( x < 0 && y > 0 ) ||  (x < 0 && y < 0) )
            ra += 180;
        else if ( x > 0 && y < 0)
            ra += 360;
        SunAstroData sunData = new SunAstroData(ra ,dec * 180 / Math.PI);
        sunData.setTrueAnomaly(m + ec);
        return sunData;
    }
    
    public static SunAstroData getSunAstroData(Date ut) {
        SunAstroData sunData = getRADecSun(ut);
        double f = (1 + 0.016713 * Math.cos(sunData.getTrueAnomaly() * Math.PI / 180.0))
                / (1 - Math.pow(0.0161713, 2));
        sunData.setAngularDiameter(f * 0.533128);
        return sunData;
    }
    
    public static MoonAstroData getRaDecMoon(Date ut) {
        double days = TimeUtil.getDaysFrom1990(ut);
        double n = 360.0/365.242191 * days;
        double Eg = 279.403303;
        if(n < 0)
            n = n + (((int)((-n) / 360) + 1) * 360.0);
        else if(n > 360)
            n = n - ((int)( n / 360) * 360.0);
        double m = n + Eg - 282.768422;
        if(m < 0)
            m += 360;
        double ec = 360.0/Math.PI * 0.016713 * Math.sin(m * Math.PI / 180.0);
        double Lg = n + ec + Eg;
        if(Lg > 360)
            Lg -= 360;
        
        double l  = 13.1763966 * days + 318.351648;
        if(l < 0)
            l = l + (((int)((-l) / 360) + 1) * 360.0);
        else if(l > 360)
            l = l - ((int)( l / 360) * 360.0);
        
        double Mm = l - 0.1114041 * days - 36.340410;
        if(Mm < 0)
            Mm = Mm + (((int)((-Mm) / 360) + 1) * 360.0);
        else if(Mm > 360)
            Mm = Mm - ((int)( Mm / 360) * 360.0);
        
        double N = 318.510107 - 0.0529539 * days;
        if(N < 0)
            N = N + (((int)((-N) / 360) + 1) * 360.0);
        else if(N > 360)
            N = N - ((int)( N / 360) * 360.0);
        
        double Ev = 1.2739 * Math.sin((2 * (l - Lg) - Mm) * Math.PI / 180.0);
        double Ae = 0.1852 * Math.sin(m * Math.PI / 180.0);
        double A3 = 0.37 * Math.sin(m * Math.PI / 180.0);
        double Mmp = Mm + Ev - Ae - A3;
        double Ec = 6.2886 * Math.sin(Mmp * Math.PI / 180.0);
        double A4 = 0.214 * Math.sin( 2 * Mmp * Math.PI / 180.0);
        double lp = l + Ev + Ec - Ae + A4;
        double V = 0.6583 * Math.sin(2 * (lp - Lg) * Math.PI / 180.0);
        double lpp = lp + V;
        double Np = N - 0.16 * Math.sin(m * Math.PI / 180.0);
        double y = Math.sin((lpp - Np) * Math.PI / 180.0) * Math.cos(5.145396 * Math.PI / 180.0);
        double x = Math.cos((lpp - Np) * Math.PI / 180.0);
        double Lm = Math.atan(y/x);
        Lm = Lm * 180 / Math.PI;
        if( ( x < 0 && y > 0 ) ||  (x > 0 && y < 0) )
            Lm += 180;
        else if ( x > 0 && y < 0)
            Lm += 360;
        Lm += Np;
        double Bm = Math.asin(Math.sin((lpp - Np) * Math.PI / 180.0) * Math.sin(5.145396 * Math.PI / 180.0)) * 180.0 / Math.PI;
        
        // mean obliquity of the eclipstic
        double T = (TimeUtil.getJulianDate(ut) - 2451545.0) / 36525.0;
        double dE = 46.815 * T + 0.0006 * Math.pow(T, 2) + 0.00181 * Math.pow(T, 3);
        dE = Math.abs(dE);
        dE = dE / 3600;
        double E = 23.439292 - dE;
        
        x = Math.cos(Lm * Math.PI / 180.0);
        y = Math.sin(Lm * Math.PI / 180.0) * Math.cos(E * Math.PI / 180.0) 
        - Math.tan(Bm * Math.PI / 180.0) * Math.sin(E * Math.PI / 180.0);
        double ra = Math.atan(y/x);
        double dec = Math.sin(Bm * Math.PI / 180.0) * Math.cos(E * Math.PI / 180.0);
        dec += Math.cos(Bm * Math.PI / 180.0) * Math.sin(E * Math.PI / 180.0) * Math.sin(Lg * Math.PI / 180.0);
        dec = Math.asin(dec);
        ra = ra * 180 / Math.PI;
        if( ( x < 0 && y > 0 ) ||  (x < 0 && y < 0) )
            ra += 180;
        else if ( x > 0 && y < 0)
            ra += 360;
        dec = dec * 180 / Math.PI;
        MoonAstroData moonData = new MoonAstroData(ra, dec);
        moonData.setMmp(Mmp);
        moonData.setEc(Ec);
        return moonData;
    }
    
    public static MoonAstroData getMoonAstroData(Date ut){
        MoonAstroData moonData = getRaDecMoon(ut);
        double pp =  (1 - Math.pow(0.054900, 2)) / (1 + 0.054900 * Math.cos((moonData.getMmp() + moonData.getEc()) * Math.PI / 180.0));
        moonData.setAngularDiameter(0.5181/ pp);
        return moonData;
    }
}
