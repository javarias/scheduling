package alma.scheduling.dataload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import alma.scheduling.dataload.WeatherDataReader.WeatherData;
import alma.scheduling.datamodel.weather.dao.WeatherHistoryDAO;

public abstract class WeatherParameterDataLoader implements DataLoader {

    protected String file;
    protected WeatherHistoryDAO dao;
    protected int maxNumRecords;
    private WeatherDataReader reader;
    private int count = 0;

    public WeatherParameterDataLoader() {
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setDao(WeatherHistoryDAO dao) {
        this.dao = dao;
    }

    public void setMaxNumRecords(int maxNumRecords) {
        this.maxNumRecords = maxNumRecords;
    }

    private void createDataReader() throws FileNotFoundException {
        File file = new File(this.file);
        FileReader fr = new FileReader(file);
        reader = new WeatherDataReader(fr);                
    }

    protected WeatherData getNextWeatherDatum() throws NumberFormatException,
            IOException {
        if (reader == null)
            createDataReader();
        if ((maxNumRecords >= 0) && (count++ > maxNumRecords)) {
            return null;
        }
        return reader.getWeatherData();
    }
}