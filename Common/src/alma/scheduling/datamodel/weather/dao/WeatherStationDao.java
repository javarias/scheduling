/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2011
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/
package alma.scheduling.datamodel.weather.dao;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.Control.CurrentWeather;
import alma.Control.CurrentWeatherPackage.Humidity;
import alma.Control.CurrentWeatherPackage.Temperature;
import alma.ControlExceptions.IllegalParameterErrorEx;
import alma.acs.exceptions.CorbaExceptionConverter;
import alma.scheduling.datamodel.GenericDaoImpl;
import alma.scheduling.datamodel.weather.HumidityHistRecord;
import alma.scheduling.datamodel.weather.OpacityHistRecord;
import alma.scheduling.datamodel.weather.PathFluctHistRecord;
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
public class WeatherStationDao extends GenericDaoImpl implements WeatherHistoryDAO {

	private static Logger logger = LoggerFactory.getLogger(WeatherHistoryDAO.class);
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
		if (weatherStationComponent == null) {
			logger.warn("Weather station reference is not available for DSA. Using default values for humidity: 0.0");
			return new HumidityHistRecord(0.0, 0.0, 0.0 ,0.0);
		}
		try {
			hum = weatherStationComponent.getHumidity(WS_NAMES[1]);
		} catch (IllegalParameterErrorEx e) {
			e.printStackTrace();
		} catch (org.omg.CORBA.SystemException ex) {
			logger.warn("CORBA Problem when trying to access weather station reference: " +
					"Weather station is not available for DSA. Using default values for humidity: 0.0");
			ex.printStackTrace();
			new HumidityHistRecord(0.0, 0.0, 0.0, 0.0);
		}
		if (hum == null) {
			logger.warn("Invalid return value for weather station. Using default values for humidity: 0.0");
			return new HumidityHistRecord(0.0, 0.0, 0.0, 0.0);
		}
		return new HumidityHistRecord((double)hum.timestamp, hum.value, 0.0, 0.0);
	}

	/**
	 * Get the current temperature only
	 */
	@Override
	public TemperatureHistRecord getTemperatureForTime(Date ut) {
		Temperature temp  = null;
		if (weatherStationComponent == null) {
			logger.warn("Weather station reference is not available for DSA. Using default values for temp: 270.0 K");
			return new TemperatureHistRecord(0.0, 270.0, 0.0 ,0.0);
		}
		try {
			temp = weatherStationComponent.getTemperature(WS_NAMES[1]);
		} catch (IllegalParameterErrorEx e) {
			e.printStackTrace();
		} catch (org.omg.CORBA.SystemException ex) {
			logger.warn("CORBA Problem when trying to access weather station reference: " +
					"Weather station is not available for DSA. Using default values for temp: 270.0 K");
			ex.printStackTrace();
			return new TemperatureHistRecord(0.0, 270.0, 0.0 ,0.0);
		}
		if (temp == null) {
			logger.warn("Invalid return value for weather station. Using default values for temp: 270.0 K");
			return new TemperatureHistRecord(0.0, 270.0, 0.0 ,0.0);
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

	@Override
	public void loadPathFluctHistory(List<PathFluctHistRecord> records) {
		// TODO Auto-generated method stub
		
	}

}
