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

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.weather.PathFluctHistRecord;
import alma.scheduling.datamodel.weather.TemperatureHistRecord;
import alma.scheduling.datamodel.weather.WeatherHistRecord;

/**
 * Cached Weather DAO. This class load  all the data from DB and store them in the memory
 * during all the execution of the simulation.
 * 
 * @author javarias
 *
 */
public class MemoryWeatherHistoryDAOImpl extends WeatherHistoryDAOImpl
        implements WeatherHistoryDAO {

    private static Logger logger = LoggerFactory.getLogger(MemoryWeatherHistoryDAOImpl.class);
    
    private TemperatureHistRecord tempCache[] = null;
    private PathFluctHistRecord phaseCache[] = null;
    private Long startTime = null;
    private Double maxTime = null;

    
    public TemperatureHistRecord getTemperatureForTime(Date ut) {
        if(tempCache == null)
            fillCache();
        return getWeatherRecordForTime(ut, tempCache);
    }
    
    public PathFluctHistRecord getPathFluctForTime(Date ut) {
        if(phaseCache == null)
            fillCache();
        return getWeatherRecordForTime(ut, phaseCache);
    }
    
    
    /**
     * 
     * @param ut the time
     * @param cache the cache where to extract ten weather data
     * @return the weather data for the given time 
     */
    @SuppressWarnings("unchecked")
	private <T extends WeatherHistRecord> T getWeatherRecordForTime(Date ut, T[] cache) {
    	if(startTime == null)
            startTime = getSimulationStartTime().getTime();
        if(maxTime == null){
            Query query;
            query = getSession().getNamedQuery("TemperatureHistRecord.getMaxTime");
            maxTime = (Double) query.uniqueResult();
            logger.debug("max time in temperature historical records: " + maxTime);
        }
        double dt = ( ut.getTime() - getSimulationStartTime().getTime() ) / ( 3600000.0); // difference in time (hours)
        dt = dt % maxTime;
        int pos = (int) (dt / 0.010416667 + 0.1);
        logger.debug("Time to get" + dt + " Position to get: " + pos );
        
        Double temperature = tempCache[pos].getValue();
        if (temperature < -500) {
            logger.info("lower bound not a valid value, looking at the next 5 values");
            for (int i = 0; i < 5 ; i++) {
                WeatherHistRecord t = cache[pos + i];
                if (t.getValue() > -500) {
                    return (T) t;
                }
            }
        }
        return cache[pos];
    }
    
	private void fillCache() {
		{
			tempCache = new TemperatureHistRecord[35065];
			List<TemperatureHistRecord> tmp = findAllOrdered(TemperatureHistRecord.class);
			for (int i = 0; i < tempCache.length; i++) {
				tempCache[i] = tmp.get(i);
			}
		}
		{
			phaseCache = new PathFluctHistRecord[35065];
			List<PathFluctHistRecord> tmp = findAllOrdered(PathFluctHistRecord.class);
			for (int i = 0; i < phaseCache.length; i++) {
				phaseCache[i] = tmp.get(i);
			}
		}
	}
}
