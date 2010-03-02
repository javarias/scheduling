package alma.scheduling.datamodel.weather.dao;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.weather.HumidityHistRecord;
import alma.scheduling.datamodel.weather.OpacityHistRecord;
import alma.scheduling.datamodel.weather.TemperatureHistRecord;
import alma.scheduling.datamodel.weather.WindSpeedHistRecord;

public class WeatherHistoryDAOImpl extends GenericDaoImpl implements WeatherHistoryDAO {

    private static Logger logger = LoggerFactory.getLogger(WeatherHistoryDAOImpl.class);
    
    private Date simulationStartTime;
    
    public void setSimulationStartTime(Date simulationStartTime) {
        this.simulationStartTime = simulationStartTime;
    }
    
    public Date getSimulationStartTime() {
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
    public HumidityHistRecord getHumidityForTime(Date ut) {
        return new HumidityHistRecord(0.0, 0.50, 0.1, 0.1);
    }

    @Override
    public TemperatureHistRecord getTemperatureForTime(Date ut) {
        double dt = ( ut.getTime() - simulationStartTime.getTime() ) / ( 3600000.0); // difference in time (hours)
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
        // return new TemperatureHistRecord(0.0, 0.20, 0.1, 0.1);
    }

}
