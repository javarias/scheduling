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
