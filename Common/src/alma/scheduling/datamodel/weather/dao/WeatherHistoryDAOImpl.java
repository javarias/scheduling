package alma.scheduling.datamodel.weather.dao;

import java.util.List;

import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.weather.HumidityHistRecord;
import alma.scheduling.datamodel.weather.OpacityHistRecord;
import alma.scheduling.datamodel.weather.TemperatureHistRecord;
import alma.scheduling.datamodel.weather.WindSpeedHistRecord;

public class WeatherHistoryDAOImpl extends GenericDaoImpl implements WeatherHistoryDAO {

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

}
