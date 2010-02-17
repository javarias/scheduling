package alma.scheduling.dataload;

import java.util.ArrayList;
import java.util.List;

import alma.scheduling.dataload.WeatherDataReader.WeatherData;
import alma.scheduling.datamodel.weather.WindSpeedHistRecord;

public class WindSpeedDataLoader extends WeatherParameterDataLoader {

    public WindSpeedDataLoader() {
        super();
    }

    @Override
    public void load() {
        try {
            List<WindSpeedHistRecord> records = new ArrayList<WindSpeedHistRecord>();
            WeatherData wd;
            while ((wd = getNextWeatherDatum()) != null) {
                WindSpeedHistRecord record = new WindSpeedHistRecord(wd.getTime(),
                        wd.getValue(), wd.getRms(), wd.getSlope());
                records.add(record);
            }
            dao.loadWindSpeedHistory(records);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}