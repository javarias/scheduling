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
package alma.scheduling.algorithm;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.algorithm.astro.InterferometrySensitivityCalculator;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.executive.ExecutivePercentage;
import alma.scheduling.datamodel.executive.ExecutiveTimeSpent;
import alma.scheduling.datamodel.executive.dao.ExecutiveDAO;
import alma.scheduling.datamodel.observation.ExecBlock;
import alma.scheduling.datamodel.observation.ExecStatus;
import alma.scheduling.datamodel.observation.dao.ObservationDao;
import alma.scheduling.datamodel.observatory.ArrayConfiguration;
import alma.scheduling.datamodel.obsproject.ObservationStatus;
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
import alma.scheduling.weather.OpacityInterpolator;

public class SchedBlockExecutorImpl implements SchedBlockExecutor {

    private static Logger logger = LoggerFactory.getLogger(SchedBlockExecutorImpl.class);
    private Map<String, Double> accumSensJyCache;
    /**In milliseconds*/
//    private final long executionTime = 60 * 60 * 1000 + 20 * 60 * 1000; //1 hr 20 min.
    private final ConcurrentNavigableMap<String, ExecBlock> activeExecBlocks;
    private ConfigurationDao configDao;
    private ExecutiveDAO execDao;
    private SchedBlockDao schedBlockDao;
    private OpacityInterpolator opacityInterpolator;
    private WeatherHistoryDAO weatherDao;
    private ObservationDao obsDao;
    
    public SchedBlockExecutorImpl() {
    	accumSensJyCache = new TreeMap<String, Double>();
    	activeExecBlocks = new ConcurrentSkipListMap<String, ExecBlock>();
	}

    public void setConfigDao(ConfigurationDao configDao) {
        this.configDao = configDao;
    }
    
    public void setExecDao(ExecutiveDAO execDao) {
        this.execDao = execDao;
    }
    
    public void setSchedBlockDao(SchedBlockDao schedBlockDao) {
        this.schedBlockDao = schedBlockDao;
    }
    
    public void setOpacityInterpolator(OpacityInterpolator opacityInterpolator) {
        this.opacityInterpolator = opacityInterpolator;
    }
    
    public void setWeatherDao(WeatherHistoryDAO weatherDao) {
        this.weatherDao = weatherDao;
    }
    
    public void setObsDao(ObservationDao obsDao) {
		this.obsDao = obsDao;
	}
    
	@Override
    public Date execute(SchedBlock schedBlock, ArrayConfiguration arrCnf, Date ut) {
		double accumTime = obsDao.getAccumulatedObservingTimeForSb(schedBlock.getUid()) / 3600.0;
        int numRep = obsDao.getNumberOfExecutionsForSb(schedBlock.getUid());
		
        long executionTime = (long)(schedBlock.getSchedBlockControl().getSbMaximumTime() * 60 * 60 * 1000);
        
		double expTimeHr = executionTime;
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
        //Check if the antennas has been declared in the configuration
        //Otherwise use the value put in the array configuration
        int numAnt = arrCnf.getAntennaInstallations().size();
        if (numAnt == 0)
        	numAnt = arrCnf.getNumberOfAntennas();
        double antDiamMtr = arrCnf.getAntennaDiameter();
        
//        Set<AntennaInstallation> antInst = arrCnf.getAntennaInstallations();
//        for (Iterator<AntennaInstallation> iter = antInst.iterator(); iter.hasNext();) {
//            AntennaInstallation ai = iter.next();
//            antDiamMtr = ai.getAntenna().getDiameter(); // just pick the first one
//        }
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
			String msg = new String(" High Sensitivity detected in SchedBlock ID: " + schedBlock.getId() + 
					"(" + schedBlock.getUid() + ", " + schedBlock.getName() +")\n" +  
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
		
        double accumSens = 0;
        if(!accumSensJyCache.containsKey(schedBlock.getUid()))
            accumSens = sensJy;
        else{
        	double previousSum = accumSensJyCache.get(schedBlock.getUid()) * numRep;
        	previousSum = Math.pow(previousSum, 2); 
        	accumSens = Math.sqrt( previousSum + Math.pow(sensJy, 2) ) / 
        				schedBlock.getSchedBlockControl().getNumberOfExecutions();
        }
        //Since hibernate 3 doesn't support nested subqueries criterias and projections
        //A cache is necessary to save the accumulated sensitivity
        accumSensJyCache.put(schedBlock.getUid(), accumSens);
        
        //TODO: Missing max number of repetitions in SB
//        if ((accumSens * FUDGE_FACTOR <= sensGoalJy) || (accumTime >= schedBlock.getObsUnitControl().getMaximumTime())) {
        if ( (numRep + 1) == schedBlock.getSchedBlockControl().getExecutionCount()) {
            schedBlock.getSchedBlockControl().setState(SchedBlockState.FULLY_OBSERVED);
        }
        else{
            schedBlock.getSchedBlockControl().setState(SchedBlockState.RUNNING);
        }
        schedBlockDao.saveOrUpdate(schedBlock);
        
        Date nextExecutionTime = new Date(ut.getTime() + executionTime);
        
        ExecBlock eb = new ExecBlock();
        eb.setExecBlockUid(UUID.randomUUID().toString());
        eb.setSchedBlockUid(schedBlock.getUid());
        eb.setStatus(ExecStatus.SUCCESS);
        eb.setStartTime(ut);
        eb.setEndTime(nextExecutionTime);
        eb.setSensitivityAchieved(sensJy);
        //TODO: Assuming all the time we were on source 
        eb.setTimeOnSource(executionTime / 1000.0);
        activeExecBlocks.put(schedBlock.getUid(), eb);
        
        ExecutiveTimeSpent ets = new ExecutiveTimeSpent();
        ets.setExecutive(execDao.getExecutive(schedBlock.getPiName()));
        ets.setObservingSeason(execDao.getCurrentSeason());
        ets.setSbUid(schedBlock.getUid());
        ets.setTimeSpent(executionTime / 3600000.0F);
        ExecutivePercentage ep = execDao.getExecutivePercentage(schedBlock.getExecutive(), execDao.getCurrentSeason());
        ep.setRemainingObsTime(ep.getRemainingObsTime() - schedBlock.getSchedBlockControl().getSbMaximumTime().floatValue());
        execDao.saveOrUpdate(ets); // TODO fix interfaces instead
        
        
        return nextExecutionTime;
    }

    @Override
    public ExecBlock finishSbExecution(SchedBlock sb, ArrayConfiguration arrCnf,
            Date ut) {
        if (sb.getSchedBlockControl().getState() == SchedBlockState.RUNNING){
            sb.getSchedBlockControl().setState(SchedBlockState.READY);
            sb.getProject().setStatus(ObservationStatus.IN_PROGRESS);
            schedBlockDao.saveOrUpdate(sb);
        }
        
        ExecBlock eb = activeExecBlocks.remove(sb.getUid());
        eb.setEndTime(ut);
        obsDao.save(eb);
        
        return eb;
    }
}
