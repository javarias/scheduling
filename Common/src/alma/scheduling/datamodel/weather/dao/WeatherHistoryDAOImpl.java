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
package alma.scheduling.datamodel.weather.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.weather.HumidityHistRecord;
import alma.scheduling.datamodel.weather.OpacityHistRecord;
import alma.scheduling.datamodel.weather.PathFluctHistRecord;
import alma.scheduling.datamodel.weather.TemperatureHistRecord;
import alma.scheduling.datamodel.weather.WeatherHistRecord;
import alma.scheduling.datamodel.weather.WindSpeedHistRecord;

public abstract class WeatherHistoryDAOImpl implements WeatherHistoryDAO {

    private static Logger logger = LoggerFactory.getLogger(WeatherHistoryDAOImpl.class);

    // --- Spring managed properties ---
    
    protected ConfigurationDao configurationDao;
    
    protected SortedMap<Double, TemperatureHistRecord> tempHistRecords;
    protected SortedMap<Double, HumidityHistRecord> humidityHistRecords;
    protected SortedMap<Double, OpacityHistRecord> opacityHistRecords;
    protected SortedMap<Double, WindSpeedHistRecord> windSpdHistRecords;
    protected SortedMap<Double, PathFluctHistRecord> pathFluctHistRecords;
    
    public void setConfigurationDao(ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }
    
    private Date simulationStartTime;
    
    public void setSimulationStartTime(Date simulationStartTime) {
        this.simulationStartTime = simulationStartTime;
    }
    
    public WeatherHistoryDAOImpl() {
    	tempHistRecords = new TreeMap<>();
    	humidityHistRecords = new TreeMap<>();
    	opacityHistRecords = new TreeMap<>();
    	windSpdHistRecords = new TreeMap<>();
    	pathFluctHistRecords = new TreeMap<>();
    }
    
    public Date getSimulationStartTime() {
        if (simulationStartTime == null) {
            Configuration cnf = configurationDao.getConfiguration();
            if (cnf == null) {
                logger.error("Configuration is null");
            }
            Date startTime = configurationDao.getConfiguration().getSimulationStartTime();
            logger.debug("setting simulation start time: " + startTime);
            this.simulationStartTime = startTime;
        }
        return this.simulationStartTime;
    }
    
    @Override
    public void loadTemperatureHistory(List<TemperatureHistRecord> records) {
        for(TemperatureHistRecord r : records) {
        	tempHistRecords.put(r.getTime(), r);
        }
    }

    @Override
    public void loadHumidityHistory(List<HumidityHistRecord> records) {
    	for(HumidityHistRecord r: records) {
    		humidityHistRecords.put(r.getTime(), r);
    	}
    }

    @Override
    public void loadOpacityHistory(List<OpacityHistRecord> records) {
    	for(OpacityHistRecord r: records) {
    		opacityHistRecords.put(r.getTime(), r);
    	}
    }

    @Override
    public void loadWindSpeedHistory(List<WindSpeedHistRecord> records) {
    	for(WindSpeedHistRecord r: records) {
    		windSpdHistRecords.put(r.getTime(), r);
    	}
    }
    
	@Override
	public void loadPathFluctHistory(List<PathFluctHistRecord> records) {
		for(PathFluctHistRecord r: records) {
			pathFluctHistRecords.put(r.getTime(), r);
		}
	}

    @Override
    public HumidityHistRecord getHumidityForTime(Date ut) {
    	double h = 0.5 + Math.random() * 4.0;
        return new HumidityHistRecord(0.0, h, 0.1, 0.1);
    }

    /**
     * 
     * @param t the class to do the query it must be a subclass of {@link WeatherHistRecord}
     * @return All the {@link WeatherHistRecords} of type t found in Database ordered by time ascending
     */
    @SuppressWarnings("unchecked")
    public <T extends WeatherHistRecord> List<T> findAllOrdered(Class<T> t) {
    	if (t.getCanonicalName().equals(TemperatureHistRecord.class.getCanonicalName())) {
    		return (List<T>) new ArrayList<TemperatureHistRecord>(tempHistRecords.values());
    	} else if (t.getCanonicalName().equals(HumidityHistRecord.class.getCanonicalName())) {
    		return (List<T>) new ArrayList<HumidityHistRecord>(humidityHistRecords.values());
    	} else if (t.getCanonicalName().equals(OpacityHistRecord.class.getCanonicalName())) {
    		return (List<T>) new ArrayList<OpacityHistRecord>(opacityHistRecords.values());
    	} else if (t.getCanonicalName().equals(PathFluctHistRecord.class.getCanonicalName())) {
    		return (List<T>) new ArrayList<PathFluctHistRecord>(pathFluctHistRecords.values());
    	} else 
    		return null;
    }
    
    

}
