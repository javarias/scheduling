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
 * "@(#) $Id: SystemTemperatureCalculator.java,v 1.13 2012/02/22 16:28:14 javarias Exp $"
 */
package alma.scheduling.algorithm.astro;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.utils.Constants;
import alma.scheduling.utils.CoordinatesUtil;
import alma.scheduling.utils.ErrorHandling;
/**
 * Calculates the system temperature (Tsys).
 * 
 */
public class SystemTemperatureCalculator {
	private static Logger logger = LoggerFactory.getLogger(SystemTemperatureCalculator.class);
	/**
	 * 
	 * @param tau_zero opacity at the zenith
     * @param ra Right Ascension (degrees)
     * @param declination Declination (degrees)
     * @param latitude Latitude (degrees)
     * @param ut Time in Universal Time system
	 * @return the opacity for the given coordinate
	 */
	public static double getOpacity (double tau_zero, double ra, double declination,
            double latitude, Date ut) {
        double latitudeRad = Math.toRadians(latitude);
        double decRad = Math.toRadians(declination);

        logger.debug("getOpacity: lat: " + latitude + "; dec: " + declination);
        double sinDec = Math.sin(decRad);
        double sinLat = Math.sin(latitudeRad);
        double cosDec = Math.cos(decRad);
        double cosLat = Math.cos(latitudeRad);
        double haHours = CoordinatesUtil.getHourAngle(ut, ra/15.0, Constants.CHAJNANTOR_LONGITUDE);
        double ha = Math.PI * haHours / 12;
        logger.debug("getOpacity: ha = " + haHours);
        double cosHa = Math.cos(ha);
		double sinAltitude = sinDec * sinLat + cosDec * cosLat * cosHa;
		logger.debug("getOpacity: sinDec=" + sinDec + "; sinLat=" + sinLat + "; cosDec=" + cosDec + "; cosLat=" + cosLat + "; cosHa=" + cosHa);
		logger.debug("getOpacity: sinAlt = " + sinAltitude);
		double tau = tau_zero / sinAltitude;
		return tau;
	}
	
    /**
     * Get system temperature (K).
     * @param ra Right Ascension (degrees)
     * @param declination Declination (degrees)
     * @param latitude Latitude (degrees)
     * @param frequency Frequency (GHz)
     * @param opacity Opacity or optical depth (neper)
     * @param atmBrightnessTemperature Atmospheric brightness temperature (K)
     * @param ut Time in Universal Time system
     * @return System temperature (K)
     */
    public static double getTsys(double ra, double declination,
            double latitude, double frequency, double opacity,
            double atmBrightnessTemperature, Date ut) {
    	return getTsys(ra, declination, latitude, frequency, opacity, atmBrightnessTemperature, ut, 270.0);
    }
	
    /**
     * Get system temperature (K).
     * @param ra Right Ascension (degrees)
     * @param declination Declination (degrees)
     * @param latitude Latitude (degrees)
     * @param frequency Frequency (GHz)
     * @param opacity Opacity or optical depth (neper)
     * @param atmBrightnessTemperature Atmospheric brightness temperature (K)
     * @param ut Time in Universal Time system
     * @param Tamb ambient temperature (K)
     * @return System temperature (K)
     */
    public static double getTsys(double ra, double declination,
            double latitude, double frequency, double opacity,
            double atmBrightnessTemperature, Date ut, double Tamb) {
        
    	// sinAltitud Calculation
        double latitudeRad = Math.toRadians(latitude);
        double decRad = Math.toRadians(declination);

        double sinDec = Math.sin(decRad);
        double sinLat = Math.sin(latitudeRad);
        double cosDec = Math.cos(decRad);
        double cosLat = Math.cos(latitudeRad);
        double haHours = CoordinatesUtil.getHourAngle(ut, ra/15.0, Constants.CHAJNANTOR_LONGITUDE);
        double ha = Math.PI * haHours / 12;
        logger.debug("getTsys: ha = " + haHours);
        double cosHa = Math.cos(ha);

        double sinAltitude = sinDec * sinLat + cosDec * cosLat * cosHa;
        
        // Airmass
        double Airmass = 1.0 / sinAltitude;

        double etaFeed = 0.95; // forward efficiency
        double Trx = getReceiverTemperature(frequency);
        
        double tauZero = opacity;
        double Tatm = atmBrightnessTemperature;
        double tau = tauZero * Airmass;
        ErrorHandling.getInstance().debug("Opacity at source: " + tau);
        
        double Tant = getAntennaTemperature(frequency, etaFeed, Tamb, Tatm, tau);
        
        double Tsys = (Trx + Tant) * Math.exp(tau);
        Tsys = Tsys / etaFeed;
//        double f = Math.exp(tauZero * Airmass);
//        double Tcmb = 2.725; // [K]
//        
//        Trx  = planck(frequency, Trx);
//        Tatm = planck(frequency, Tatm);
//        Tamb = planck(frequency, Tamb);
//
//        double Tsys = (Trx + Tatm * etaFeed * (1.0 - 1 / f)
//                    + Tamb * (1.0 - etaFeed));
//        // GHz, K
//        Tsys = f * Tsys + Tcmb;
        return Tsys;
    }
    
    /**
     * Get zenith system temperature (K)
     * 
     * @param frequency Frequency (GHz)
     * @param opacity Opacity or optical depth (neper)
     * @param atmBrightnessTemperature Atmospheric brightness temperature (K)
     * @return System temperature (K)
     */
    
    public static double getZenithTsys(double frequency, double opacity,
            double atmBrightnessTemperature) {
    	return getZenithTsys(frequency, opacity, atmBrightnessTemperature, 270);
    }
    
    /**
     * Get zenith system temperature (K)
     * 
     * @param frequency Frequency (GHz)
     * @param opacity Opacity or optical depth (neper)
     * @param atmBrightnessTemperature Atmospheric brightness temperature (K)
     * @param Tamb ambient temperature (K)
     * @return System temperature (K)
     */
    
    public static double getZenithTsys(double frequency, double opacity,
            double atmBrightnessTemperature, double Tamb) {
    	double etaFeed = 0.95;
    	double Trx = getReceiverTemperature(frequency);
        double tauZero = opacity;
        double Tatm = atmBrightnessTemperature;
    	double Tant = getAntennaTemperature(frequency, etaFeed, Tamb, Tatm, tauZero);
    	
    	double Tsys = (Trx + Tant) * Math.exp(tauZero);
    	Tsys = Tsys / etaFeed;
    	return Tsys;
    }
    /**
     * Return the receiver temperature
     * 
     * @param frequency (GHz)
     * @return receiver temperature in deg. K
     * @author ab
     * 
     * TODO AB Replace this quick and dirty impl. by one using the Receiver class.
     *
     */
    protected static double getReceiverTemperature (double frequency) {
        if (frequency >=31.3 && frequency <= 45.0)
            return 17.0;
        else if (frequency >=67.0 && frequency <= 90.0)
            return 30.0;
        else if (frequency >=84.0 && frequency <= 116.0)
            return 37.0;
        else if (frequency >=125.0 && frequency < 163.0)
            return 51.0;
        else if (frequency >=163.0 && frequency < 211.0)
            return 65.0;
        else if (frequency >=211.0 && frequency < 275.0)
            return 83.0;
        else if (frequency >=275.0 && frequency <= 373.0)
            return 147.0;
        else if (frequency >=385.0 && frequency <= 500.0)
            return 196.0;
        else if (frequency >=602.0 && frequency <= 720.0)
            return 175.0;
        else if (frequency >=787.0 && frequency <= 950.0)
            return 230.0;
        else
            return -1.0;
    }
    
    /**
     * Apply the Planck correction to a temperature for a given frequency.
     * 
     * @param frequency Frequency (GHz)
     * @param temperature Temperature to correct (K)
     * @return corrected temperature (K)
     */
    protected static double planck(double frequency, double temperature) {
        final double k = 1.38E-23;
        double freqHz = frequency * 1.0E9; // [Hz]
        double tmp = Constants.PLANCK * freqHz / k;
        double ret = tmp / (Math.exp(tmp / temperature) - 1.0);
        return ret;
    }
    
    /**
     * Calculate Antenna temperature
     * 
     * @param frequency (GHz)
     * @param etaFeed Forward efficiency
     * @param Tamb Ambient temperature (from weather station)
     * @param Tatm Atmospheric temperature (interpolated)
     * @param tau tau_zero / Math.sin(el)
     * @return Antenna temperature
     */
    protected static double getAntennaTemperature(double frequency, double etaFeed,
    		double Tamb, double Tatm, double tau) {
    	double Tsky = Tatm * (1 - Math.exp(-tau));
    	double TSpill = 0.95 * Tamb;
    	double Tbg = 2.76 * Math.exp(-tau);
    	double Tant = etaFeed * Tsky + (1 - etaFeed) * TSpill + etaFeed * Tbg; 
    	return Tant;
    }
}
