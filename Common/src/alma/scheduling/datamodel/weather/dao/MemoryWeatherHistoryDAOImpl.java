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

import alma.scheduling.datamodel.weather.TemperatureHistRecord;

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
    
    private TemperatureHistRecord cache[] = null;
    private Long startTime = null;
    private Double maxTime = null;

    @Override
    public TemperatureHistRecord getTemperatureForTime(Date ut) {
        //Cache init
        if(cache == null)
            fillCache();
        if(startTime == null)
            startTime = getSimulationStartTime().getTime();
        if(maxTime == null){
            Query query;
            query = getSession().getNamedQuery("TemperatureHistRecord.getMaxTime");
            maxTime = (Double) query.uniqueResult();
            logger.info("max time in temperature historical records: " + maxTime);
        }
        double dt = ( ut.getTime() - getSimulationStartTime().getTime() ) / ( 3600000.0); // difference in time (hours)
        dt = dt % maxTime;
        int pos = (int) (dt / 0.010416667 + 0.1);
        System.out.println(pos);
        
        Double temperature = cache[pos].getValue();
        if (temperature < -500) {
            logger.info("lower bound not a valid value, looking at the next 5 values");
            for (int i = 0; i < 5 ; i++) {
                TemperatureHistRecord t = cache[pos + i];
                if (t.getValue() > -500) {
                    return t;
                }
            }
        }
        return cache[pos];
    }
    
    private void fillCache(){
        cache =  new TemperatureHistRecord[35065];
        List<TemperatureHistRecord> tmp = findAllOrdered();
        for(int i = 0; i < cache.length; i++){
            cache[i] = tmp.get(i);
        }
    }
}
