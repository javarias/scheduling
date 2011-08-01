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
 * "@(#) $Id: SchedBlockExecutorImpl.java,v 1.16 2011/08/01 15:47:26 dclarke Exp $"
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
import alma.scheduling.datamodel.executive.ExecutivePercentage;
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
import alma.scheduling.utils.Constants;
import alma.scheduling.utils.CoordinatesUtil;

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
        ets.setTimeSpent(schedBlock.getSchedBlockControl().getSbMaximumTime().floatValue());
        ExecutivePercentage ep = execDao.getExecutivePercentage(schedBlock.getExecutive(), execDao.getCurrentSeason());
        ep.setRemainingObsTime(ep.getRemainingObsTime() - schedBlock.getSchedBlockControl().getSbMaximumTime().floatValue());
        ((GenericDao) execDao).saveOrUpdate(ets); // TODO fix interfaces instead
        ((GenericDao) execDao).saveOrUpdate(ep); // TODO fix interfaces instead
        
        double execTime = schedBlock.getSchedBlockControl().getSbMaximumTime();
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
        double raDeg = 
            schedBlock.getSchedulingConstraints()
                      .getRepresentativeTarget()
                      .getSource()
                      .getCoordinates()
                      .getRA();
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
                    freqGHz, bwGHz, raDeg, declDeg, numAnt, antDiamMtr, latitudeDeg,
                    opacity, atmBrightnessTemp, ut);
        
		//TODO: For Warning purposes. Sensitivity gets high values when observing to horizon.
        //      See http://jira.alma.cl/browse/COMP-5048 for more information.
		if( sensJy > 1.0 ){
			String msg = new String(" High Sensitivity detected in SchedBlock ID: " + schedBlock.getId() + "\n" +  
					"  Temp and Humi: " + hr.getValue() + ", " + tr.getValue() + "\n" + 
					"  opacityInterpolator.estimatePWV(): " + pwv + "\n" + 
					"  opacityInterpolator.interpolateOpacityAndTemperature().opacity: " + opacity + "\n" + 
					"  opacityInterpolator.interpolateOpacityAndTemperature().atmBrightnessTemp: " + atmBrightnessTemp + "\n" + 
					"  InterferometrySensitivityCalculator.pointSourceSensitivity(): " + sensJy + "\n" + 
					"  RA: " + raDeg + "     Dec: " + declDeg + "\n" + 
					"  Hour Angle at the given time: " + 
					CoordinatesUtil.getHourAngle(ut, schedBlock.getSchedulingConstraints()
	                      .getRepresentativeTarget()
	                      .getSource()
	                      .getCoordinates().getRA() / 15, 
	                Constants.CHAJNANTOR_LONGITUDE) );
			logger.warn(msg);
			
		}
		// END Warning code 
		
		
        schedBlock.getSchedBlockControl().setNumberOfExecutions(
                schedBlock.getSchedBlockControl().getNumberOfExecutions() + 1);
        double accumSens = 0;
        if(schedBlock.getSchedBlockControl().getAchievedSensitivity() == null)
            accumSens = Math.sqrt(sensJy/sensJy);
        else{
        	double previousSum = schedBlock.getSchedBlockControl().getAchievedSensitivity() * 
        							(schedBlock.getSchedBlockControl().getNumberOfExecutions() 
        							- 1);
        	previousSum = previousSum * previousSum; 
        	accumSens = Math.sqrt( previousSum + (sensJy * sensJy) ) / 
        				schedBlock.getSchedBlockControl().getNumberOfExecutions();
        }
        schedBlock.getSchedBlockControl().setAchievedSensitivity(accumSens);
        if ((accumSens * FUDGE_FACTOR <= sensGoalJy) || (accumTime >= schedBlock.getObsUnitControl().getMaximumTime())) {
            schedBlock.getSchedBlockControl().setState(SchedBlockState.FULLY_OBSERVED);
        }
        else{
            schedBlock.getSchedBlockControl().setState(SchedBlockState.RUNNING);
        }
        schedBlockDao.saveOrUpdate(schedBlock);
        
        long executionTime = (long) (schedBlock.getSchedBlockControl().getSbMaximumTime().doubleValue()
            * 1000 * 3600);
        Date nextExecutionTime = new Date(ut.getTime() + executionTime);
        return nextExecutionTime;
    }

    @Override
    public void finishSbExecution(SchedBlock sb, ArrayConfiguration arrCnf,
            Date ut) {
        if (sb.getSchedBlockControl().getState() == SchedBlockState.RUNNING){
            sb.getSchedBlockControl().setState(SchedBlockState.READY);
            schedBlockDao.saveOrUpdate(sb);
        }
    }
}
