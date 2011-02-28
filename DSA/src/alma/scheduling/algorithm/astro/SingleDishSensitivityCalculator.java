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
 * "@(#) $Id: SingleDishSensitivityCalculator.java,v 1.3 2011/02/28 17:35:06 ahoffsta Exp $"
 */
package alma.scheduling.algorithm.astro;

import java.util.Date;

/**
 * Sensitivity calculator for single dish observations.
 * 
 * This code has been taken from OBSPREP alma.obsprep.services.etc.ExposureTimeCalculator class,
 * by Hiroshi Yatagai. It was reorganized to make it more reusable and avoid unnecessary
 * dependencies with OBSPREP.
 */
public class SingleDishSensitivityCalculator extends SensitivityCalculatorBase {

    /**
     * Returns the sensitivity for a point source observation, given an
     * exposure time.
     * 
     * @param exposureTime Exposure time (seconds)
     * @param frequency Observation frequency (GHz)
     * @param bandwidth Bandwidth (GHz)
     * @param declination Source declination (degrees)
     * @param numberAntennas Number of antennas
     * @param antennaDiameter Antenna diameter (m)
     * @param latitude Geographic latitude (degrees)
     * @param opacity Opacity or optical depth (nepers)
     * @param atmBrightnessTemperature Atmospheric brightness temperature (K)
     * @param ut Date in Universal Time
     * @return sensitivity (Jy)
     */
    public static double pointSourceSensitivity(double exposureTime,
            double frequency, double bandwidth, double declination,
            int numberAntennas, double antennaDiameter, double latitude,
            double opacity, double atmBrightnessTemperature, Date ut) {
        double rho_e = antennaEfficiency(antennaDiameter, frequency);
        double tsys = SystemTemperatureCalculator.getTsys(declination,
                latitude, frequency, opacity, atmBrightnessTemperature, ut);
        double bandwidthHz = bandwidth * 1.0e9;
        // only valid for switch mode
        // for on-the-fly use 1.0 instead of sqrt(2)
        // other modes have other factors
        return rho_e * Math.sqrt(2) * tsys
                / Math.sqrt(numberAntennas * bandwidthHz * exposureTime);
    }
    
    /**
     * Returns the faintest source brightness temperature that is possible to
     * observe given an exposure time, for an extended source observation with a given
     * resolution.
     * 
     * @param exposureTime Exposure time (seconds)
     * @param resolution Resolution (arcsec)
     * @param frequency Observation frequency (GHz)
     * @param bandwidth Bandwidth (GHz)
     * @param declination Source declination (degrees)
     * @param numberAntennas Number of antennas
     * @param antennaDiameter Antenna diameter (m)
     * @param latitude Geographic latitude (degrees)
     * @param opacity Opacity (nepers)
     * @param atmBrightnessTemperature Atmospheric brightness temperature (K)
     * @param ut Date in Universal Time
     * @return source brightness temperature (K)
     */
    public static double extendedSourceBrightnessTemp(double exposureTime,
            double resolution, double frequency, double bandwidth,
            double declination, int numberAntennas, double antennaDiameter,
            double latitude, double opacity, double atmBrightnessTemperature, Date ut) {
        double sensitivity = pointSourceSensitivity(exposureTime, frequency, bandwidth,
                declination, numberAntennas, antennaDiameter,
                latitude, opacity, atmBrightnessTemperature, ut);
        return toBrightnessTemp(sensitivity, frequency, antennaDiameter);
    }

    /**
     * Returns the exposure time required to achieve a given sensitivity.
     * 
     * @param sensitivity Sensitivity (Jy)
     * @param frequency Frequency (GHz)
     * @param bandwidth Bandwidth (GHz)
     * @param declination Declination (degrees)
     * @param numberAntennas Number of antennas
     * @param antennaDiameter Antenna diameter (m)
     * @param latitude Geographic latitude (degrees)
     * @param opacity Opacity (nepers)
     * @param atmBrightnessTemperature Atmospheric brightness temperature (K)
     * @param ut Date in Universal Time
     * @return exposure time (seconds)
     */
    public static double pointSourceExposureTime(double sensitivity,
            double frequency, double bandwidth, double declination,
            int numberAntennas, double antennaDiameter, double latitude,
            double opacity, double atmBrightnessTemperature, Date ut) {
        double rho_e = antennaEfficiency(antennaDiameter, frequency);
        double tsys  =
            SystemTemperatureCalculator.getTsys(declination, latitude, frequency,
            		opacity, atmBrightnessTemperature, ut);
        double bandwidthHz = bandwidth * 1.0e9;
        double tmp = rho_e * Math.sqrt(2) * tsys / sensitivity;
        return tmp * tmp / (numberAntennas * bandwidthHz);
    }

    
    /**
     * Converts the sensitivity, i.e., the faintest source that it is possible
     * to observe in Jy, to its equivalent brightness temperature.
     * 
     * @param sensitivity Sensitivity (Jy)
     * @param frequency Frequency (GHz)
     * @param diameter Antenna diameter (m)
     * @return equivalent brightness temperature
     */
    public static double toBrightnessTemp(double sensitivity, 
            double frequency, double diameter) {
        double ae = antennaEfficiency(diameter, frequency);
        double bme = mainBeamEfficiency(frequency);
        double result = sensitivity / ae / bme;
        return result;
    }
    
    /**
     * Converts a brightness temperature to flux density. 
     * 
     * @param brightnessTemp Brightness temperature (K)
     * @param frequency Frequency (GHz)
     * @param diameter Antenna diameter (m)
     * @return flux density
     */
    public static double toFluxDensity(double brightnessTemp, 
            double frequency, double diameter) {
        double ae = antennaEfficiency(diameter, frequency);
        double bme = mainBeamEfficiency(frequency);
        double result = brightnessTemp * ae * bme;
        return result;
    }
}
