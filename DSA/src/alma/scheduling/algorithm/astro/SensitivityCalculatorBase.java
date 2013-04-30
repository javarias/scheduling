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
 */
package alma.scheduling.algorithm.astro;

import alma.scheduling.utils.Constants;

/**
 * Base class for sensitivity calculators. Contains common functions necessary for
 * these computations.
 * 
 * This code has been taken from OBSPREP alma.obsprep.services.etc.ExposureTimeCalculator class,
 * by Hiroshi Yatagai. It was reorganized to make it more reusable and avoid unnecessary
 * dependencies with OBSPREP.
 */
public abstract class SensitivityCalculatorBase {
    
    /**
     * Calculate antenna efficiency
     * 
     * @param Antenna diameter (m)
     * @param Frequency (GHz)
     * @return antenna efficiency
     */
    protected static double antennaEfficiency(double diameter, double frequency) {
        double antRadius = diameter * 100.0 / 2.0;
        double antArea = Math.PI * antRadius * antRadius * illuminationEfficiency();
        double ae = 2.0 * Constants.BOLTZMANN / antArea;
        double appertureEfficiency = appertureEfficiency(frequency);
        return ae / appertureEfficiency;
    }
    
    /**
     * Calculate apperture efficiency
     * 
     * @param Frequency (GHz)
     * @return apperture efficiency
     */
    protected static double appertureEfficiency(double frequency) {
        double eps_zero = 0.80;
        double sigma    = 25; // [microns]
        double lambda   = 2.998E5 / frequency;
        double arg = ( 4 * Math.PI * sigma / lambda );
        return eps_zero * Math.exp(-arg*arg);
    }
    
    /**
     * Returns the atmospheric decorrelation coefficient, related to the
     * atmospheric phase noise.
     * 
     * @return atmospheric decorrelation coefficient
     */
    protected static double atmosphericDecorrelationCoeff() {
        return 1.0;
    }
    
    /**
     * Returns the correlator efficiency.
     * 
     * @return correlator efficiency
     */
    protected static double correlatorEfficiency() {
         // 0.88 for 2-bit orrelator modes
         // 0.95 for 3-bit orrelator modes
         // 0.99 for 4-bit orrelator modes
        return 0.88; // PdB number
        // TODO ACA: 3bit, BL: 2bits
    }
        
    /**
     * Returns the instrumental decorrelation coefficient, related
     * to the oscillator phase jitter.
     * 
     * @return instrumental decorrelation coefficient
     */
    protected static double instrumentalDecorrelationCoeff() {
        return 1.0;
    }
    
    /**
     * Returns the main beam efficiency. 
     *
     * @param frequency Frequency (GHz)
     * @return main beam efficiency
     */
    protected static double mainBeamEfficiency(double frequency) {
        return 1.0 * appertureEfficiency(frequency); // for the moment
    }
    
    /**
     * Returns the the parameter for untapered maps with natural weighting.
     * Used in calculating brightness temperature.
     * 
     * @return untapered mapping, natural weighting parameter
     */
    protected static double untaparedMapsParameter() {
        return 15.0;
    }
    
    /**
     * Returns the illumination efficiency. 
     *
     * @return illumination efficiency
     */
    private static double illuminationEfficiency() {
        return 0.8;
    }
}
