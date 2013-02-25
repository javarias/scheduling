/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.algorithm.weather;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import alma.scheduling.algorithm.astro.SystemTemperatureCalculator;
import alma.scheduling.algorithm.modelupd.ModelUpdater;
import alma.scheduling.datamodel.obsproject.FieldSource;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.Target;
import alma.scheduling.datamodel.obsproject.WeatherDependentVariables;
import alma.scheduling.datamodel.weather.HumidityHistRecord;
import alma.scheduling.datamodel.weather.TemperatureHistRecord;
import alma.scheduling.utils.ErrorHandling;

public class MemoryWeatherUpdater extends WeatherUpdater implements
        ModelUpdater {

    private HashMap<Date, PWV> cache;
    
    public MemoryWeatherUpdater(){
        super();
        cache = new HashMap<Date, PWV>();
    }
    
    @Override
    public synchronized void update(Date date, Collection<SchedBlock> sbs) {
        /*this is to assure of the atomicity of the update operation*/
        if(needsToUpdate(date) == false)
            return;
        lastUpdate = date;
        
        ErrorHandling.getInstance().trace("entering");
        ErrorHandling.getInstance().debug("updating for time " + date);
        
        if(latitude == null)
            latitude = configDao.getConfiguration().getArrayCenterLatitude();

        // get current PWV
        double pwv;
        //Check if the time to get the PWV is current of future
        //check if dao supports get value of PWV first
        //In practical terms the current time is now plus half an hour in the future
        if (weatherDao.hasPWV() && date.getTime() > System.currentTimeMillis() + 29 * 60 * 1000) {
        	TemperatureHistRecord tr = weatherDao.getTemperatureForTime(date);
        	ErrorHandling.getInstance().info("temperature record: time = " + tr.getTime() + "; value = "
        			+ tr.getValue());
        	HumidityHistRecord hr = weatherDao.getHumidityForTime(date);
        	ErrorHandling.getInstance().info("humidity record: time = " + hr.getTime() + "; value = "
        			+ hr.getValue());
        	pwv = estimatePWV(hr.getValue(), tr.getValue()); // mm
        } else {
        	pwv = weatherDao.getPwvForTime(date);
        }
        long deltaT = (long) (projTimeIncr * 3600.0 * 1000.0); // delta T in
                                                               // milliseconds
        Date projDate = new Date(date.getTime() + deltaT);
        TemperatureHistRecord ptr = weatherDao.getTemperatureForTime(projDate);
        HumidityHistRecord phr = weatherDao.getHumidityForTime(projDate);
        double ppwv = estimatePWV(phr.getValue(), ptr.getValue()); // projected
                                                                   // PWV, in mm

        for (SchedBlock sb: sbs) {
            ErrorHandling.getInstance().debug("Calculations for SchedBlock: " + sb.getUid());
            double frequency = sb.getSchedulingConstraints()
                    .getRepresentativeFrequency(); // GHz
            Target target = sb.getSchedulingConstraints()
                    .getRepresentativeTarget();
            FieldSource src = target.getSource();
            double ra = src.getCoordinates().getRA(); // degrees
            double decl = src.getCoordinates().getDec(); // degrees

            double[] tmp = interpolateOpacityAndTemperature(pwv, frequency);
            double tau_zero = tmp[0];
            ErrorHandling.getInstance().debug("Opacity at zenith: " + tau_zero);
            double Tatm = tmp[1];
            double tsys = SystemTemperatureCalculator.getTsys(ra, decl,
                    latitude, frequency, tau_zero, Tatm, date);
            ErrorHandling.getInstance().info("tsys: " + tsys);

            double zenithTsys = SystemTemperatureCalculator.getZenithTsys(frequency,
            		tau_zero, Tatm);
            ErrorHandling.getInstance().info("curr tsys: " + zenithTsys);
            
            double tau = SystemTemperatureCalculator.getOpacity(tau_zero, ra, decl, latitude, date);
            tmp = interpolateOpacityAndTemperature(ppwv, frequency);
            tau_zero = tmp[0];
            Tatm = tmp[1];
            double ptsys = SystemTemperatureCalculator.getTsys(ra, decl,
                    latitude, frequency, tau_zero, Tatm, date);

            
            WeatherDependentVariables vars = new WeatherDependentVariables();
            vars.setTsys(tsys);
            vars.setProjectedTsys(ptsys);
            vars.setProjectionTimeIncr(projTimeIncr);
            vars.setOpacity(tau);
            vars.setZenithTsys(zenithTsys);
            sb.setWeatherDependentVariables(vars);
            //schedBlockDao.saveOrUpdate(sb); //TODO: Remove this? This should not be here, degrade a lot the performance of the simulator
        }
    }

    @Override
    public void update(Date date, SchedBlock sb) {
        if(latitude == null)
            latitude = configDao.getConfiguration().getArrayCenterLatitude();
        if(cache.keySet().size() > 100)
            cache.clear();
        if (cache.get(date) == null) {
            // get current PWV
            System.out.println("Start Calculations");
            Date t1 = new Date();
            TemperatureHistRecord tr = weatherDao.getTemperatureForTime(date);
            ErrorHandling.getInstance().debug("temperature record: time = " + tr.getTime()
                    + "; value = " + tr.getValue());
            HumidityHistRecord hr = weatherDao.getHumidityForTime(date);
            ErrorHandling.getInstance().debug("humidity record: time = " + hr.getTime()
                    + "; value = " + hr.getValue());
            double pwv = estimatePWV(hr.getValue(), tr.getValue()); // mm

            long deltaT = (long) (projTimeIncr * 3600.0 * 1000.0); // delta T in
            // milliseconds
            Date projDate = new Date(date.getTime() + deltaT);
            TemperatureHistRecord ptr = weatherDao
                    .getTemperatureForTime(projDate);
            HumidityHistRecord phr = weatherDao.getHumidityForTime(projDate);
            double ppwv = estimatePWV(phr.getValue(), ptr.getValue()); // projected
            // PWV, in mm
            PWV tmp = new PWV();
            tmp.setPwv(pwv);
            tmp.setPpwv(ppwv);
            cache.put(date, tmp);
            Date t2 = new Date();
            System.out.println("Weather Calculations takes: " + (t2.getTime() - t1.getTime()) + " ms");
        }
        double pwv = cache.get(date).getPwv();
        double ppwv = cache.get(date).getPpwv();
        
        // inside the for of method of above
        double frequency = sb.getSchedulingConstraints()
                .getRepresentativeFrequency(); // GHz
        Target target = sb.getSchedulingConstraints().getRepresentativeTarget();
        FieldSource src = target.getSource();
        double ra = src.getCoordinates().getRA(); // degrees
        double decl = src.getCoordinates().getDec(); // degrees

        double[] tmp = interpolateOpacityAndTemperature(pwv, frequency);
        double tau_zero = tmp[0];
        double Tatm = tmp[1];
        double tsys = SystemTemperatureCalculator.getTsys(ra, decl,
                latitude, frequency, tau_zero, Tatm, date);
        ErrorHandling.getInstance().debug("tsys: " + tsys);

        double currTsys = SystemTemperatureCalculator.getZenithTsys(frequency,
        		tau_zero, Tatm);
        ErrorHandling.getInstance().debug("curr tsys: " + currTsys);
        
        double tau = SystemTemperatureCalculator.getOpacity(tau_zero, ra, decl, latitude, date);
        ErrorHandling.getInstance().debug("tau_zero:" + tau_zero + "; " + "tau: " + tau);
        tmp = interpolateOpacityAndTemperature(ppwv, frequency);
        tau_zero = tmp[0];
        Tatm = tmp[1];
        double ptsys = SystemTemperatureCalculator.getTsys(ra, decl,
                latitude, frequency, tau_zero, Tatm, date);

        
        WeatherDependentVariables vars = new WeatherDependentVariables();
        vars.setTsys(tsys);
        vars.setProjectedTsys(ptsys);
        vars.setProjectionTimeIncr(projTimeIncr);
        vars.setOpacity(tau);
        vars.setZenithOpacity(tau_zero);
        vars.setZenithTsys(currTsys);
        sb.setWeatherDependentVariables(vars);
        //schedBlockDao.saveOrUpdate(sb); //TODO: Remove this? This should not be here, degrade a lot the performance of the simulator
    }

    private class PWV{
        private double pwv;
        private double ppwv;
        
        public double getPwv() {
            return pwv;
        }
        
        public void setPwv(double pwv) {
            this.pwv = pwv;
        }
        
        public double getPpwv() {
            return ppwv;
        }
        
        public void setPpwv(double ppwv) {
            this.ppwv = ppwv;
        }
        
    }
}
