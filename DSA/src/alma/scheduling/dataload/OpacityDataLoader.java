package alma.scheduling.dataload;

import java.util.ArrayList;
import java.util.List;

import alma.scheduling.dataload.WeatherDataReader.WeatherData;
import alma.scheduling.datamodel.weather.OpacityHistRecord;

public class OpacityDataLoader extends WeatherParameterDataLoader {

	@Override
	public void load() throws Exception {
		List<OpacityHistRecord> records = new ArrayList<>();
		WeatherData wd;
        while ((wd = getNextWeatherDatum()) != null) {
        	OpacityHistRecord record = new OpacityHistRecord(wd.getTime(),
                        wd.getValue(), wd.getRms(), wd.getSlope());
        	 records.add(record);
        }
        dao.loadOpacityHistory(records);

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

}
