package alma.scheduling.datamodel.weather.dao;

import java.util.Date;
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
    
    private Date initialTime;
    
    public void setInitialTime(Date initialTime) {
        this.initialTime = initialTime;
    }
    
    public Date getInitialTime() {
        return this.initialTime;
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
        return new HumidityHistRecord(0.0, 0.20, 0.1, 0.1);
    }

    @Override
    public TemperatureHistRecord getTemperatureForTime(Date ut) {
        // double dt = ( ut.getTime() - initialTime.getTime() ) / ( 3600000.0); // difference in time (hours)
        // mod max time in table
        Query query;
        query = getSession().getNamedQuery("TemperatureHistRecord.getMaxTime");
        Double maxTime = (Double) query.uniqueResult();
        logger.info("max time in temperature historical records: " + maxTime);
        // dt = dt % maxTime;
        double dt = 0.5; // for now
        query = getSession().getNamedQuery("TemperatureHistRecord.getIntervalLowerBound");
        query.setParameter(0, dt);
        query.setMaxResults(1);
        List<TemperatureHistRecord> temps = (List<TemperatureHistRecord>) query.list();
        logger.info("retrieved # of temperature records: " + temps.size());
        return new TemperatureHistRecord(0.0, 0.20, 0.1, 0.1);
    }

}
