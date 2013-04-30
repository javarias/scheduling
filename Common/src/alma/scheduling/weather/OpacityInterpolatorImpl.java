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
package alma.scheduling.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.weather.AtmParameters;
import alma.scheduling.datamodel.weather.dao.AtmParametersDao;

public class OpacityInterpolatorImpl implements OpacityInterpolator {

    private static Logger logger = LoggerFactory.getLogger(OpacityInterpolatorImpl.class);
    
    private AtmParametersDao atmDao;
    public void setAtmDao(AtmParametersDao atmDao) {
        this.atmDao = atmDao;
    }
    
    @Override
    public double estimatePWV(double humidity, double temperature) {
        double h; // PWV
        double P_0; // water vapor partial pressure
        double theta; // inverse temperature [K]
        double m_w = 18 * 1.660538782E-27; // mass of a water molecule (18 amu in Kg)
        double H = 1.5E3; // scale height of water vapor distribution
        double rho_l = 1e3; // desity of water [Kg/m^3]
        double k = 1.3806503E-23; // Boltzmann constant [m^2 Kg s^-2 K^-1]
        double T_0; // ground temperature in Kelvins
        
        // convert temperature to degrees Kelvin
        T_0 = temperature + 273.15;
        theta = 300.0/T_0;
        P_0 = 2.409E12 * humidity * Math.pow(theta, 4) * Math.exp(-22.64 * theta);
        
        h = ( m_w * P_0 * H ) / ( rho_l * k * T_0 );
        return h * 1E3; // in mm
    }

    @Override
    public double[] interpolateOpacityAndTemperature(double pwv, double freq) {
        double[] retVal = new double[2];
        // First get the PWV interval
        Double[] pwvInterval = atmDao.getEnclosingPwvInterval(pwv);
        // For the PWV lower bound, interpolate opacity and temperature as functions of frequency
        AtmParameters[] atm;
        atm = atmDao.getEnclosingIntervalForPwvAndFreq(pwvInterval[0], freq);
        
        double interpOpacity1 = interpolate(freq, atm[0].getFreq(), atm[1].getFreq(),
                atm[0].getOpacity(), atm[1].getOpacity());
        double interpTemp1 = interpolate(freq, atm[0].getFreq(), atm[1].getFreq(),
                atm[0].getAtmBrightnessTemp(), atm[1].getAtmBrightnessTemp());
        
        // For the PWV upper bound, interpolate opacity and temperature as functions of frequency
        atm = atmDao.getEnclosingIntervalForPwvAndFreq(pwvInterval[1], freq);
        double interpOpacity2 = interpolate(freq, atm[0].getFreq(), atm[1].getFreq(),
                atm[0].getOpacity(), atm[1].getOpacity());
        double interpTemp2 = interpolate(freq, atm[0].getFreq(), atm[1].getFreq(),
                atm[0].getAtmBrightnessTemp(), atm[1].getAtmBrightnessTemp());
        
        // Finally, interpolate opacity and temperature again as functions of PWV.
        // Do this only if the PWV's are different, if not just return the first interpolated
        // values.
        if (pwvInterval[0] != pwvInterval[1]) {
            double finalOpacity = interpolate(pwv, pwvInterval[0], pwvInterval[1],
                    interpOpacity1, interpOpacity2);
            double finalTemp = interpolate(pwv, pwvInterval[0], pwvInterval[1],
                    interpTemp1, interpTemp2);
            retVal[0] = finalOpacity;
            retVal[1] = finalTemp;
        } else {
            retVal[0] = interpOpacity1;
            retVal[1] = interpTemp1;
        }
        return retVal;
    }

    /**
     * A simple linear interpolation routine.
     * @param x independent variable to interpolate, should be between x1 and x2
     * @param x1 independent variable value 1
     * @param x2 independent variable value 2
     * @param y1 dependent variable value for x1
     * @param y2 dependent variable value for x2
     * @return interpolation for the dependent variable, for the value x
     */
    private double interpolate(double x, double x1, double x2, double y1, double y2) {
        return y1 + ( y2 - y1 ) * ( x - x1 ) / ( x2 - x1 );
    }    
    
}