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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alma.Control.CurrentWeather;
import alma.Control.CurrentWeatherPackage.Humidity;
import alma.Control.CurrentWeatherPackage.Temperature;
import alma.ControlExceptions.IllegalParameterErrorEx;
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
 * The PWV value is based on a forecast taken from APEX website: 'http://www.eso.org/gen-fac/pubs/astclim/forecast/gfs/APEX/'
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
	
	
	private TreeMap<Date, Double> cachedPWVValues = null;
	private String lastChecksumForPage = null;
	private long lastCheckPWV = 0;
	/*
	 * Nothing to load
	 */
	@Override
	public void loadTemperatureHistory(List<TemperatureHistRecord> records) {
		throw new RuntimeException("The weather station doesn't handle history for the weather parameters");
	}

	/*
	 * Nothing to load
	 */
	@Override
	public void loadHumidityHistory(List<HumidityHistRecord> records) {
		throw new RuntimeException("The weather station doesn't handle history for the weather parameters");
	}

	/*
	 * Nothing to load
	 */
	@Override
	public void loadOpacityHistory(List<OpacityHistRecord> records) {
		throw new RuntimeException("The weather station doesn't handle history for the weather parameters");
	}

	/*
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

	@Override
	public PathFluctHistRecord getPathFluctForTime(Date ut) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean hasPWV() {
		return true;
	}

	@Override
	public double getPwvForTime(Date ut) throws UnsupportedOperationException {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(ut);
		calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
		int year = calendar.get(Calendar.YEAR);
		
		if (cachedPWVValues == null)
			try {
				if ((System.currentTimeMillis() - lastCheckPWV) > (2 * 60 * 60 * 1000)) { //Check every 2 hours
					cachedPWVValues = refreshPWVforecast(year);
					lastCheckPWV = System.currentTimeMillis();
				}
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("Problem trying to retrieve PWV forecast. Error: " + e.getMessage());
				if (cachedPWVValues != null) {
					logger.error("Returning already stored value...");
				}
			}
		
		if (cachedPWVValues != null)
			return getInterpolatedPWVValue(ut);
		
		return 0;
	}
	
	/*
	 * The ascii file contains 4 columns of data that represent: 
	 * [Date of forecast initiation, Hour of forecast initiation, Forecast hour, PWV forecast]
	 */
	private TreeMap<Date, Double> refreshPWVforecast(long year) throws IOException {
		final TreeMap<Date, Double> retVal = new TreeMap<Date, Double>();
		final String urlStr = new String("http://www.eso.org/gen-fac/pubs/astclim/forecast/gfs/APEX/forecast/text/"+ year +"/gfs_pwv_for.txt");
		URL url = null;
		InputStream is = null;
		URLConnection connection = null;
		BufferedReader reader = null;
		try {
			url = new URL(urlStr);
			connection = url.openConnection();
			//Check if it is necessary to refresh the data
			String currentChecksum = getPageChecksum(connection.getInputStream());
			if (lastChecksumForPage != null && cachedPWVValues != null &&
					lastChecksumForPage.compareTo(currentChecksum) == 0) {
				logger.debug("Ignoring values get from forecast file. Same values stored in cache ");
				return cachedPWVValues;
			}
			connection = url.openConnection();
			is = connection.getInputStream();
			reader =  new BufferedReader(new InputStreamReader(is));
			String line = reader.readLine();
			String[] s = line.split("\\s+");
			Calendar initialCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			initialCal.set(
					Integer.valueOf(s[1].substring(0,4)),
					Integer.valueOf(s[1].substring(4,6)) - 1,
					Integer.valueOf(s[1].substring(6,8)),
					Integer.valueOf(s[2]), 0);
			do {
				s = line.split("\\s+");
				Date forecastDate = new Date(initialCal.getTimeInMillis() + Integer.valueOf(s[3]) * 3600000);
				retVal.put(forecastDate, new Double(s[4]));
			} while ((line = reader.readLine()) != null);
			lastChecksumForPage = currentChecksum;
		} catch (MalformedURLException e) {
			logger.error("URL: '" + urlStr + "' could be invalid.");
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					//It is not possible to fix the things ant this point
				}
		}
		
		return retVal;
	}
	
	private String getPageChecksum(InputStream is) throws IOException {
		try {
			Formatter formatter = new Formatter();
			MessageDigest digest = MessageDigest.getInstance("SHA");
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder builder = new StringBuilder();
			int c;
			while ((c = reader.read()) != -1) {
				builder.append((char)c);
			}
			for (byte b: digest.digest(builder.toString().getBytes())) {
				formatter.format("%02x", b);
			}
			return formatter.toString();
		} catch (NoSuchAlgorithmException ex) {
			//If something goes wrong the value of the hash will be always different
			return String.valueOf(System.currentTimeMillis());
		} finally {
			is.close();
		}
	}
	
	private double getInterpolatedPWVValue(Date ut) {
		Date pd = null;
		for (Date d: cachedPWVValues.keySet()) {
			if (ut.compareTo(d) > 0) {
				pd = d;
			} else {
				if (pd == null) {
					logger.warn("Requested date: " + ut.toString() + 
							" to get PWV value is outside of the range. Returning initial value got from forecast");
					return cachedPWVValues.get(d);
				}
//				double pwv_low, pwv_high;
//				pwv_low = cachedPWVValues.get(pd);
//				pwv_high = cachedPWVValues.get(d);
				double m = (cachedPWVValues.get(d) - cachedPWVValues.get(pd)) / (d.getTime() - pd.getTime());
				double i = cachedPWVValues.get(pd) - m * pd.getTime() ;
				return m * ut.getTime() + i;
			}
		}
		
		logger.warn("Requested date: " + ut.toString() + 
							" to get PWV value is outside of the range. Returning last value got from forecast.");
		return cachedPWVValues.get(pd);
		
	}

}
