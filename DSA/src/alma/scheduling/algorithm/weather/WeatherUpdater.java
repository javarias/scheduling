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
 * "@(#) $Id: WeatherUpdater.java,v 1.9 2010/04/16 20:59:49 javarias Exp $"
 */
package alma.scheduling.algorithm.weather;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import alma.scheduling.algorithm.AlgorithmPart;
import alma.scheduling.algorithm.astro.SystemTemperatureCalculator;
import alma.scheduling.algorithm.modelupd.ModelUpdater;
import alma.scheduling.algorithm.sbselection.NoSbSelectedException;
import alma.scheduling.algorithm.sbselection.SchedBlockSelector;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.obsproject.FieldSource;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.Target;
import alma.scheduling.datamodel.obsproject.WeatherDependentVariables;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.datamodel.weather.AtmParameters;
import alma.scheduling.datamodel.weather.HumidityHistRecord;
import alma.scheduling.datamodel.weather.TemperatureHistRecord;
import alma.scheduling.datamodel.weather.dao.AtmParametersDao;
import alma.scheduling.datamodel.weather.dao.WeatherHistoryDAO;

/**
 * The WeatherUpdater calculates the current and projected Tsys (for a given
 * delta T) and updates the proper columns in the database. These values are used
 * in a subsequent selector, where the criteria is that a SchedBlock cannot be
 * selected if the projected Tsys is different that the current Tsys for more that
 * a given percentage.
 * 
 */
public class WeatherUpdater implements ModelUpdater, AlgorithmPart {

    private static Logger logger = LoggerFactory.getLogger(WeatherUpdater.class);
    
    // --- Spring set properties and accessors ---
    
    private ConfigurationDao configDao;
    public void setConfigDao(ConfigurationDao configDao) {
        this.configDao = configDao;
    }
    
    private AtmParametersDao dao;
    public void setDao(AtmParametersDao dao) {
        this.dao = dao;
    }

    private SchedBlockDao schedBlockDao;
    public void setSchedBlockDao(SchedBlockDao schedBlockDao) {
        this.schedBlockDao = schedBlockDao;
    }
    
    private SchedBlockSelector selector;
    public void setSelector(SchedBlockSelector selector) {
        this.selector = selector;
    }
    
    private WeatherHistoryDAO weatherDao;
    public void setWeatherDao(WeatherHistoryDAO weatherDao) {
        this.weatherDao = weatherDao;
    }
    
    private Double projTimeIncr;
    public void setProjTimeIncr(Double projTimeIncr) {
        this.projTimeIncr = projTimeIncr;
    }

    private List<AlgorithmPart> dependencies;
    public void setAlgorithmPart(List<AlgorithmPart> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * Zero-args constructor.
     */
    public WeatherUpdater() {}
    
    // --- AlgorithmPart interface implementation ---
    
    @Override
    public List<AlgorithmPart> getAlgorithmDependencies() {
        return dependencies;
    }

    @Override
    public void execute(Date ut) {
        if (dependencies != null) {
            for (Iterator<AlgorithmPart> iter = dependencies.iterator(); iter.hasNext();) {
                iter.next().execute(ut);
            }
        }
        update(ut);
    }    
    
    // --- ModelUpdater interface implementation ---
    
    @Override
    public boolean needsToUpdate(Date date) {
        return true;
    }

    @Override
    @Transactional
    public void update(Date date) {
        Collection<SchedBlock> sbs;
        try {
            sbs = selector.select();
        } catch (NoSbSelectedException e) {
            return;
        }
        update(date,sbs);
    }

    @Override
    public void update(Date date, Collection<SchedBlock> sbs) {
        logger.trace("entering");
        logger.debug("updating for time " + date);

        double latitude = configDao.getConfiguration().getArrayCenterLatitude();

        // get current PWV
        TemperatureHistRecord tr = weatherDao.getTemperatureForTime(date);
        logger.info("temperature record: time = " + tr.getTime() + "; value = "
                + tr.getValue());
        HumidityHistRecord hr = weatherDao.getHumidityForTime(date);
        logger.info("humidity record: time = " + hr.getTime() + "; value = "
                + hr.getValue());
        double pwv = estimatePWV(hr.getValue(), tr.getValue()); // mm

        long deltaT = (long) (projTimeIncr * 3600.0 * 1000.0); // delta T in
                                                               // milliseconds
        Date projDate = new Date(date.getTime() + deltaT);
        TemperatureHistRecord ptr = weatherDao.getTemperatureForTime(projDate);
        HumidityHistRecord phr = weatherDao.getHumidityForTime(projDate);
        double ppwv = estimatePWV(phr.getValue(), ptr.getValue()); // projected
                                                                   // PWV, in mm

        for (Iterator<SchedBlock> iter = sbs.iterator(); iter.hasNext();) {
            SchedBlock sb = iter.next();
            double frequency = sb.getSchedulingConstraints()
                    .getRepresentativeFrequency(); // GHz
            Target target = sb.getSchedulingConstraints()
                    .getRepresentativeTarget();
            FieldSource src = target.getSource();
            double decl = src.getCoordinates().getDec(); // degrees

            double[] tmp = interpolateOpacityAndTemperature(pwv, frequency);
            double tau_zero = tmp[0];
            double Tatm = tmp[1];
            double tsys = SystemTemperatureCalculator.getTsys(decl, latitude,
                    frequency, tau_zero, Tatm);
            logger.info("tsys: " + tsys);

            tmp = interpolateOpacityAndTemperature(ppwv, frequency);
            tau_zero = tmp[0];
            Tatm = tmp[1];
            double ptsys = SystemTemperatureCalculator.getTsys(decl, latitude,
                    frequency, tau_zero, Tatm);

            WeatherDependentVariables vars = new WeatherDependentVariables();
            vars.setTsys(tsys);
            vars.setProjectedTsys(ptsys);
            vars.setProjectionTimeIncr(projTimeIncr);
            sb.setWeatherDependentVariables(vars);
            schedBlockDao.saveOrUpdate(sb);
        }
    }

    // --- Internal functions ---
    
    /**
     * Estimate Precipitable Water Vapor (PWV)
     * 
     * @param humidity Humidty [0/1]
     * @param temperature Temperature [C]
     * @return PWV in mm
     */
    private double estimatePWV(double humidity, double temperature) {
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
        logger.debug("P_0 = " + P_0);
        
        h = ( m_w * P_0 * H ) / ( rho_l * k * T_0 );
        logger.debug("h = " + h);
        return h * 1E3; // in mm
    }

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
    private double[] interpolateOpacityAndTemperature(double pwv, double freq) {
        double[] retVal = new double[2];
        logger.debug("pwv: " + pwv);
        logger.debug("freq: " + freq);
        // First get the PWV interval
        Double[] pwvInterval = dao.getEnclosingPwvInterval(pwv);
        logger.debug("pwv lower bound: " + pwvInterval[0]);
        logger.debug("pwv upper bound: " + pwvInterval[1]);
        // For the PWV lower bound, interpolate opacity and temperature as functions of frequency
        AtmParameters[] atm;
        atm = dao.getEnclosingIntervalForPwvAndFreq(pwvInterval[0], freq);
        logger.debug("freq lower bound: " + atm[0].getFreq());
        logger.debug("freq upper bound: " + atm[1].getFreq());
        
        double interpOpacity1 = interpolate(freq, atm[0].getFreq(), atm[1].getFreq(),
                atm[0].getOpacity(), atm[1].getOpacity());
        logger.debug("interpolated opacity 1: " + interpOpacity1);
        double interpTemp1 = interpolate(freq, atm[0].getFreq(), atm[1].getFreq(),
                atm[0].getAtmBrightnessTemp(), atm[1].getAtmBrightnessTemp());
        logger.debug("interpolated temperature 1: " + interpTemp1);
        
        // For the PWV upper bound, interpolate opacity and temperature as functions of frequency
        atm = dao.getEnclosingIntervalForPwvAndFreq(pwvInterval[1], freq);
        double interpOpacity2 = interpolate(freq, atm[0].getFreq(), atm[1].getFreq(),
                atm[0].getOpacity(), atm[1].getOpacity());
        logger.debug("interpolated opacity 2: " + interpOpacity2);
        double interpTemp2 = interpolate(freq, atm[0].getFreq(), atm[1].getFreq(),
                atm[0].getAtmBrightnessTemp(), atm[1].getAtmBrightnessTemp());
        logger.debug("interpolated temperature 2: " + interpTemp2);
        
        // Finally, interpolate opacity and temperature again as functions of PWV.
        // Do this only if the PWV's are different, if not just return the first interpolated
        // values.
        if (pwvInterval[0] != pwvInterval[1]) {
            double finalOpacity = interpolate(pwv, pwvInterval[0], pwvInterval[1],
                    interpOpacity1, interpOpacity2);
            logger.debug("final opacity: " + finalOpacity);
            double finalTemp = interpolate(pwv, pwvInterval[0], pwvInterval[1],
                    interpTemp1, interpTemp2);
            logger.debug("final temperature: " + finalTemp);
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
