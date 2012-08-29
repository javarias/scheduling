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
 * "@(#) $Id: OpacityInterpolator.java,v 1.1 2012/08/29 20:23:31 javarias Exp $"
 */
package alma.scheduling.weather;

public interface OpacityInterpolator {
    
    /**
     * Estimate Precipitable Water Vapor (PWV)
     * 
     * @param humidity Humidty [0/1]
     * @param temperature Temperature [C]
     * @return PWV in mm
     */
    double estimatePWV(double humidity, double temperature);
    
    /**
     * Interpolates the opacity and the atmospheric brightness temperature.
     * 
     * The ATM tables, stored in the database, can be seen as two surface maps. One
     * gives the opacity for (pwv, freq), and the other the atmospheric brightness temperature
     * for (pwv, freq). This routine interpolates these surface maps.
     * 
     * @param pwv Precipitable water vapor (mm)
     * @param freq Frequency (GHz)
     * @return an array with two values, the first one is the opacity (nepers) and the second
     * the atmospheric brightness temperature (K)
     */
    double[] interpolateOpacityAndTemperature(double pwv, double freq);
}
