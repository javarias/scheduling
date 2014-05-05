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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.weather.HumidityHistRecord;
import alma.scheduling.datamodel.weather.OpacityHistRecord;
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
    private HumidityHistRecord humidityCache[] = null;
    private Long startTime = null;
    private Double maxTime = null;

    @Override
    public TemperatureHistRecord getTemperatureForTime(Date ut) {
        if(tempCache == null)
            fillCache();
        return getWeatherRecordForTime(ut, tempCache);
    }
    
    @Override
    public PathFluctHistRecord getPathFluctForTime(Date ut) {
        if(phaseCache == null)
            fillCache();
        return getWeatherRecordForTime(ut, phaseCache);
    }
    
    @Override
	public HumidityHistRecord getHumidityForTime(Date ut) {
    	if (humidityCache == null)
    		fillCache();
    	return getWeatherRecordForTime(ut, humidityCache);
	}

	/**
     * 
     * @param ut the time
     * @param cache the cache where to extract ten weather data
     * @return the weather data for the given time 
     */
    @SuppressWarnings("unchecked")
	private <T extends WeatherHistRecord> T getWeatherRecordForTime(Date ut, T[] cache) {
    	if (maxTime == null) {
    		maxTime = tempHistRecords.lastKey();
    	}
        //Get time in hours relative to the beginning of current year given by the date
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ut.getTime());
        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.AM_PM, Calendar.AM);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        double dt = ( ut.getTime() - cal.getTimeInMillis()) / ( 86400000.0); // difference in time (days)
        dt = dt % maxTime;
        int pos = (int) (dt / 0.010416667 + 0.1);
        logger.debug("Time to get " + dt + " Position to get: " + pos );
        
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
		{
			humidityCache = new HumidityHistRecord[35065];
			List<OpacityHistRecord> tmp = findAllOrdered(OpacityHistRecord.class);
			for (int i = 0; i < humidityCache.length; i++) {
				double h = -4.0757E-2 + Math.sqrt(Math.pow(4.0757E-2, 2) - 4 * 9.59E-4 * (6.7787E-3 - tmp.get(i).getValue()));
				h = h / (2 * 9.59E-4);
				humidityCache[i] = new HumidityHistRecord(tmp.get(i).getTime(), h, 0.1, 0.1);
			}
		}
	}

	@Override
	public boolean hasPWV() {
		return false;
	}

	@Override
	public double getPwvForTime(Date ut) throws UnsupportedOperationException {
		throw new UnsupportedOperationException(this.getClass().getCanonicalName() + " does not have the pwv value. You should ask first!");
	}
}
