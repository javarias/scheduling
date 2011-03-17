package alma.scheduling.datamodel.weather.dao;

import java.util.Date;
import java.util.List;

import alma.Control.CurrentWeather;
import alma.Control.CurrentWeatherPackage.Humidity;
import alma.Control.CurrentWeatherPackage.Temperature;
import alma.ControlExceptions.IllegalParameterErrorEx;
import alma.scheduling.datamodel.weather.HumidityHistRecord;
import alma.scheduling.datamodel.weather.OpacityHistRecord;
import alma.scheduling.datamodel.weather.TemperatureHistRecord;
import alma.scheduling.datamodel.weather.WindSpeedHistRecord;

/**
 * The weather Station DAO will return just the weather values for the current time
 * parameters, all the method who try to load for history will throw a RuntimeException.
 * 
 * @author javarias
 * 
 * @since ALMA-8.1
 *
 */
public class WeatherStationDao implements WeatherHistoryDAO {

	/**
	 * This component reference should be set before to run any DSA in the online system
	 * @see WeatherStationDao.setWeatherStation
	 */
	static private CurrentWeather weatherStationComponent;
	
	/**
	 * Weather Station names
	 */
	static private final String[] WS_NAMES = {"WSOSF", "WSTB1", "WSTB2"};
	/**
	 * Nothing to load
	 */
	@Override
	public void loadTemperatureHistory(List<TemperatureHistRecord> records) {
		throw new RuntimeException("The weather station doesn't handle history for the weather parameters");
	}

	/**
	 * Nothing to load
	 */
	@Override
	public void loadHumidityHistory(List<HumidityHistRecord> records) {
		throw new RuntimeException("The weather station doesn't handle history for the weather parameters");
	}

	/**
	 * Nothing to load
	 */
	@Override
	public void loadOpacityHistory(List<OpacityHistRecord> records) {
		throw new RuntimeException("The weather station doesn't handle history for the weather parameters");
	}

	/**
	 * Nothing to load
	 */
	@Override
	public void loadWindSpeedHistory(List<WindSpeedHistRecord> records) {
		throw new RuntimeException("The weather station doesn't handle history for the weather parameters");
	}

	/**
	 * Get the current humidity only
	 */
	@Override
	public HumidityHistRecord getHumidityForTime(Date ut) {
		Humidity hum = null;
		try {
			hum = weatherStationComponent.getHumidity(WS_NAMES[1]);
		} catch (IllegalParameterErrorEx e) {
			e.printStackTrace();
		}
		if (hum == null)
			return new HumidityHistRecord(0.0, 0.0, 0.0, 0.0);
		
		return new HumidityHistRecord((double)hum.timestamp, hum.value, 0.0, 0.0);
	}

	/**
	 * Get the current temperature only
	 */
	@Override
	public TemperatureHistRecord getTemperatureForTime(Date ut) {
		Temperature temp  = null;
		try {
			temp = weatherStationComponent.getTemperature(WS_NAMES[1]);
		} catch (IllegalParameterErrorEx e) {
			e.printStackTrace();
		}
		
		if (temp == null) {
			return new TemperatureHistRecord(0.0, 0.0, 0.0 ,0.0);
		}
		return new TemperatureHistRecord((double)temp.timestamp, temp.value, 0.0 ,0.0);
	}

	/**
	 * This method doesn't do nothing
	 */
	@Override
	public void setSimulationStartTime(Date ut) {

	}
	
	public static void setWeatherStation(CurrentWeather comp) {
		WeatherStationDao.weatherStationComponent = comp;
	}

}
