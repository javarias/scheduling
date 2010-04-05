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
 * "@(#) $Id: SystemTemperatureCalculator.java,v 1.2 2010/04/05 19:54:39 rhiriart Exp $"
 */
package alma.scheduling.algorithm.astro;

/**
 * Calculates the system temperature (Tsys).
 * 
 */
public class SystemTemperatureCalculator {
    
    /**
     * Get system temperature (K).
     * 
     * @param declination Declination (degrees)
     * @param latitude Latitude (degrees)
     * @param frequency Frequency (GHz)
     * @param opacity Opacity or optical depth (neper)
     * @param atmBrightnessTemperature Atmospheric brightness temperature (K)
     * @return System temperature (K)
     */
    public static double getTsys(double declination, double latitude,
            double frequency, double opacity, double atmBrightnessTemperature) {
        
        double latitudeRad = Math.toRadians(latitude);
        double decRad = Math.toRadians(declination);

        double sinDec = Math.sin(decRad);
        double sinLat = Math.sin(latitudeRad);
        double cosDec = Math.cos(decRad);
        double cosLat = Math.cos(latitudeRad);
        double sinAltitude = sinDec * sinLat + cosDec * cosLat; // missing cosH?
        
        double Airmass = 1.0 / sinAltitude;

        // TODO Shouldn't this be the current temperature?
        // TODO Replace for weather temperature
        double Tamb = 270; // Ambient temperature (260 - 280 K) 
        double etaFeed = 0.95; // forward efficiency
        double Trx = getReceiverTemperature(frequency);
        
        double tauZero = opacity;
        double Tatm = atmBrightnessTemperature;
        
        double f = Math.exp(tauZero * Airmass);
        double Tcmb = 2.725; // [K]
        
        Trx  = planck(frequency, Trx);
        Tatm = planck(frequency, Tatm);
        Tamb = planck(frequency, Tamb);

        double Tsys = (Trx + Tatm * etaFeed * (1.0 - 1 / f)
                    + Tamb * (1.0 - etaFeed));
        // GHz, K
        Tsys = f * Tsys + Tcmb;
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
}
