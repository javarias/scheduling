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
 * "@(#) $Id: SchedBlockExecutorImpl.java,v 1.2 2010/03/18 06:27:15 rhiriart Exp $"
 */
package alma.scheduling.algorithm;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.astro.InterferometrySensitivityCalculator;
import alma.scheduling.algorithm.weather.OpacityInterpolator;
import alma.scheduling.datamodel.GenericDao;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.observatory.AntennaInstallation;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.ObservingParameters;
import alma.scheduling.datamodel.obsproject.SchedBlock;
import alma.scheduling.datamodel.obsproject.SchedBlockState;
import alma.scheduling.datamodel.obsproject.ScienceParameters;
import alma.scheduling.datamodel.obsproject.dao.SchedBlockDao;
import alma.scheduling.datamodel.weather.HumidityHistRecord;
import alma.scheduling.datamodel.weather.TemperatureHistRecord;
import alma.scheduling.datamodel.weather.dao.WeatherHistoryDAO;

public class SchedBlockExecutorImpl implements SchedBlockExecutor {

    private static Logger logger = LoggerFactory.getLogger(SchedBlockExecutorImpl.class);

    private ConfigurationDao configDao;
    public void setConfigDao(ConfigurationDao configDao) {
        this.configDao = configDao;
    }
    
    private ExecutiveDAO execDao;
    public void setExecDao(ExecutiveDAO execDao) {
        this.execDao = execDao;
    }
    
    private SchedBlockDao schedBlockDao;
    public void setSchedBlockDao(SchedBlockDao schedBlockDao) {
        this.schedBlockDao = schedBlockDao;
    }
    
    private OpacityInterpolator opacityInterpolator;
    public void setOpacityInterpolator(OpacityInterpolator opacityInterpolator) {
        this.opacityInterpolator = opacityInterpolator;
    }
    
    private WeatherHistoryDAO weatherDao;
    public void setWeatherDao(WeatherHistoryDAO weatherDao) {
        this.weatherDao = weatherDao;
    }
    
    @Override
    public Date execute(SchedBlock schedBlock, ArrayConfiguration arrCnf, Date ut) {
        ExecutiveTimeSpent ets = new ExecutiveTimeSpent();
        ets.setExecutive(execDao.getExecutive(schedBlock.getPiName()));
        ets.setObservingSeason(execDao.getCurrentSeason());
        ets.setSbId(schedBlock.getId());
        ets.setTimeSpent(schedBlock.getObsUnitControl().getEstimatedExecutionTime().floatValue());
        ((GenericDao) execDao).saveOrUpdate(ets); // TODO fix interfaces instead
        
        double execTime = schedBlock.getObsUnitControl().getEstimatedExecutionTime().floatValue();
        double accumTime = 0.0;
        if (schedBlock.getSchedBlockControl().getAccumulatedExecutionTime() != null) {
            accumTime = schedBlock.getSchedBlockControl().getAccumulatedExecutionTime();            
        }
        accumTime += execTime;
        schedBlock.getSchedBlockControl().setAccumulatedExecutionTime(accumTime);
        
        double expTimeHr = accumTime;
        double freqGHz = schedBlock.getSchedulingConstraints().getRepresentativeFrequency();
        schedBlockDao.hydrateSchedBlockObsParams(schedBlock);
        Set<ObservingParameters> ops = schedBlock.getObservingParameters();
        double bwGHz = 2.0;
        double sensGoalJy = 10.0;
        for (Iterator<ObservingParameters> iter = ops.iterator(); iter.hasNext();) {
            ObservingParameters params = iter.next();
            if (params instanceof ScienceParameters) {
                bwGHz = ((ScienceParameters) params).getRepresentativeBandwidth();
                sensGoalJy = ((ScienceParameters) params).getSensitivityGoal();
            }
        }
        double declDeg = 
            schedBlock.getSchedulingConstraints()
                      .getRepresentativeTarget()
                      .getSource()
                      .getCoordinates()
                      .getDec();
        int numAnt = arrCnf.getAntennaInstallations().size();
        double antDiamMtr = 4.0;
        Set<AntennaInstallation> antInst = arrCnf.getAntennaInstallations();
        for (Iterator<AntennaInstallation> iter = antInst.iterator(); iter.hasNext();) {
            AntennaInstallation ai = iter.next();
            antDiamMtr = ai.getAntenna().getDiameter(); // just pick the first one
        }
        double latitudeDeg = configDao.getConfiguration().getArrayCenterLatitude();
        TemperatureHistRecord tr = weatherDao.getTemperatureForTime(ut);
        HumidityHistRecord hr = weatherDao.getHumidityForTime(ut);
        double pwv = opacityInterpolator.estimatePWV(hr.getValue(), tr.getValue());
        double[] tmp = opacityInterpolator.interpolateOpacityAndTemperature(pwv, freqGHz);
        double opacity = tmp[0];
        double atmBrightnessTemp = tmp[1];
        double sensJy =
            InterferometrySensitivityCalculator.pointSourceSensitivity(expTimeHr,
                    freqGHz, bwGHz, declDeg, numAnt, antDiamMtr, latitudeDeg,
                    opacity, atmBrightnessTemp);
        schedBlock.getSchedBlockControl().setAchievedSensitivity(sensJy);
        if (sensJy >= sensGoalJy) {
            schedBlock.getSchedBlockControl().setState(SchedBlockState.FULLY_OBSERVED);
        }
        schedBlockDao.saveOrUpdate(schedBlock);
        
        long executionTime = (long) schedBlock.getObsUnitControl().getEstimatedExecutionTime().floatValue()
            * 3600 * 1000;
        Date nextExecutionTime = new Date(ut.getTime() + executionTime);
        return nextExecutionTime;
    }
}