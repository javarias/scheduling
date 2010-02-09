package alma.scheduling.datamodel.weather.dao;

import java.util.List;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.weather.TemperatureHistRecord;

public class WeatherHistoryDAOImpl extends GenericDaoImpl implements WeatherHistoryDAO {

    @Override
    public void loadTemperatureHistory(List<TemperatureHistRecord> records) {
        saveOrUpdate(records);
    }

}
