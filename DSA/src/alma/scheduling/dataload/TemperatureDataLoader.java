package alma.scheduling.dataload;

import java.util.ArrayList;
import java.util.List;

import alma.scheduling.dataload.WeatherDataReader.WeatherData;
import alma.scheduling.datamodel.weather.TemperatureHistRecord;

public class TemperatureDataLoader extends WeatherParameterDataLoader {

    public TemperatureDataLoader() {
        super();
    }

    @Override
    public void load() {
        try {
            List<TemperatureHistRecord> records = new ArrayList<TemperatureHistRecord>();
            WeatherData wd;
            while ((wd = getNextWeatherDatum()) != null) {
                TemperatureHistRecord record = new TemperatureHistRecord(wd.getTime(),
                        wd.getValue(), wd.getRms(), wd.getSlope());
                records.add(record);
            }
            dao.loadTemperatureHistory(records);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}