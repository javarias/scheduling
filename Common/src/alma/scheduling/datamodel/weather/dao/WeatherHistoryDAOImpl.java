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
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.config.Configuration;
import alma.scheduling.datamodel.config.dao.ConfigurationDao;
import alma.scheduling.datamodel.weather.HumidityHistRecord;
import alma.scheduling.datamodel.weather.OpacityHistRecord;
import alma.scheduling.datamodel.weather.PathFluctHistRecord;
import alma.scheduling.datamodel.weather.TemperatureHistRecord;
import alma.scheduling.datamodel.weather.WeatherHistRecord;
import alma.scheduling.datamodel.weather.WindSpeedHistRecord;

public abstract class WeatherHistoryDAOImpl extends GenericDaoImpl implements WeatherHistoryDAO {

    private static Logger logger = LoggerFactory.getLogger(WeatherHistoryDAOImpl.class);

    // --- Spring managed properties ---
    
    protected ConfigurationDao configurationDao;
    public void setConfigurationDao(ConfigurationDao configurationDao) {
        this.configurationDao = configurationDao;
    }
    
    private Date simulationStartTime;
    
    public void setSimulationStartTime(Date simulationStartTime) {
        this.simulationStartTime = simulationStartTime;
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
        saveOrUpdate(records);
    }

    @Override
    public void loadHumidityHistory(List<HumidityHistRecord> records) {
        saveOrUpdate(records);
    }

    @Override
    public void loadOpacityHistory(List<OpacityHistRecord> records) {
        saveOrUpdate(records);
    }

    @Override
    public void loadWindSpeedHistory(List<WindSpeedHistRecord> records) {
        saveOrUpdate(records);
    }
    
	@Override
	public void loadPathFluctHistory(List<PathFluctHistRecord> records) {
		saveOrUpdate(records);
		
	}

    @Override
    public HumidityHistRecord getHumidityForTime(Date ut) {
    	double h = 0.5 + Math.random() * 4.0;
        return new HumidityHistRecord(0.0, h, 0.1, 0.1);
    }

    /**
     * @deprecated As of release ALMA-9.1. Replaced by {@link MemoryWeatherHistoryDAOImpl#getTemperatureForTime(Date)}
     * 
     */
    @Override
    @Deprecated 
    public TemperatureHistRecord getTemperatureForTime(Date ut) {
    	Calendar cal = Calendar.getInstance();
		cal.setTime(ut);
		//This calculate the number of days of the current year
		cal.set(cal.get(Calendar.YEAR), 0, 0, 0, 0, 0);
		Date b_y = cal.getTime(); //first instant of the current year
		long diff = ut.getTime() - b_y.getTime();
		double days = diff /1000.0 /60.0 /60.0 /24.0;
        double dt = (long)(Math.round(days * 1000000.0))/1000000.0; // time (days) since the begin of the current year
        //approximated at 6 decimal places
        cal.setTime(ut);
        logger.debug("dt = " + dt);
        Query query;
        query = getSession().getNamedQuery("TemperatureHistRecord.getMaxTime");
        Double maxTime = (Double) query.uniqueResult();
        logger.info("max time in temperature historical records: " + maxTime);
        dt = dt % maxTime;
        query = getSession().getNamedQuery("TemperatureHistRecord.getIntervalLowerBound");
        query.setParameter(0, dt);
        query.setMaxResults(1);
        List<TemperatureHistRecord> temps = (List<TemperatureHistRecord>) query.list();
        logger.info("retrieved # of temperature records: " + temps.size());
        
        Double temperature = temps.get(0).getValue();        
        if (temperature < -500) {
            logger.info("lower bound not a valid value, looking at the next 5 values");
            query = getSession().getNamedQuery("TemperatureHistRecord.getIntervalUpperBound");
            query.setParameter(0, temps.get(0).getTime());
            query.setMaxResults(5);
            temps = (List<TemperatureHistRecord>) query.list();
            for (Iterator<TemperatureHistRecord> iter = temps.iterator(); iter.hasNext();) {
                TemperatureHistRecord t = iter.next();
                if (t.getValue() > -500) {
                    return t;
                }
            }
        }
        
        logger.info("first record: " + temps.get(0).getTime() + ", " + temps.get(0).getValue());
        return temps.get(0);
         //return new TemperatureHistRecord(0.0, 0.20, 0.1, 0.1);
    }

    /**
     * 
     * @param t the class to do the query it must be a subclass of {@link WeatherHistRecord}
     * @return All the {@link WeatherHistRecords} og type t found in Database ordered by time ascending
     */
    @SuppressWarnings("unchecked")
    public <T extends WeatherHistRecord> List<T> findAllOrdered(Class<T> t) {
        Query q = this.getSession().createQuery("from " + t.getName() +" wp order by wp.time asc");
        return q.list();
    }

}
