/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 *
 * File WeatherData.java
 */
 
package alma.scheduling.define;

/**
 * The WeatherData class captures data related to weather conditions averaged
 * over some time interval.  These quantities include:
 * <ul>
 * <li> the time interval,
 * <li> a measure of weather quality as a function of frequency band,
 * <li> the minimum observable flux density as a function of frequency band,
 * <li> the atmospheric pressure,
 * <li> the relative humidity,
 * <li> the temperature,
 * <li> the dew point temperature,
 * <li> the wind direction and speed.
 * </ul> 
 * 
 * @version 1.30 May 10, 2004
 * @author Allen Farris
 */
public class WeatherData {

	// Weather quantities are averaged over a time interval.
	private TimeInterval time; 
	// For each freqency band quality answers the question:
	//		for the specified frequency band, what is the current weather quality?
	//			where quality is: exceptional, excellent, good, average, belowAverage, poor, dismal
	private String[] quality;
	// For each freqency band minimumFlux answers the question:
	// 		for the specified frequency band and under current weather conditions, 
	//		what is the minimum observable flux?
	private double[] minimumFlux;
	private double atmosphericPressure;
	private double relativeHumidity;
	private double temperature;
	private double dewPointTemperature;
	private double windDirection;
	private double windSpeed;

	public WeatherData() {
	}

	/**
	 * @return
	 */
	public double getAtmosphericPressure() {
		return atmosphericPressure;
	}

	/**
	 * @return
	 */
	public double getDewPointTemperature() {
		return dewPointTemperature;
	}

	/**
	 * @return
	 */
	public double[] getMinimumFlux() {
		return minimumFlux;
	}

	/**
	 * @param band
	 * @return
	 */
	public double getMinimumFlux(int band) {
		return minimumFlux[band];
	}

	/**
	 * @return
	 */
	public String[] getQuality() {
		return quality;
	}

	/**
	 * @param band
	 * @return
	 */
	public String getQuality(int band) {
		return quality[band];
	}

	/**
	 * @return
	 */
	public double getRelativeHumidity() {
		return relativeHumidity;
	}

	/**
	 * @return
	 */
	public double getTemperature() {
		return temperature;
	}

	/**
	 * @return
	 */
	public TimeInterval getTime() {
		return time;
	}

	/**
	 * @return
	 */
	public double getWindDirection() {
		return windDirection;
	}

	/**
	 * @return
	 */
	public double getWindSpeed() {
		return windSpeed;
	}

	/**
	 * @param d
	 */
	public void setAtmosphericPressure(double d) {
		atmosphericPressure = d;
	}

	/**
	 * @param d
	 */
	public void setDewPointTemperature(double d) {
		dewPointTemperature = d;
	}

	/**
	 * @param ds
	 */
	public void setMinimumFlux(double[] ds) {
		minimumFlux = ds;
	}

	/**
	 * @param strings
	 */
	public void setQuality(String[] strings) {
		quality = strings;
	}

	/**
	 * @param d
	 */
	public void setRelativeHumidity(double d) {
		relativeHumidity = d;
	}

	/**
	 * @param d
	 */
	public void setTemperature(double d) {
		temperature = d;
	}

	/**
	 * @param interval
	 */
	public void setTime(TimeInterval interval) {
		time = interval;
	}

	/**
	 * @param d
	 */
	public void setWindDirection(double d) {
		windDirection = d;
	}

	/**
	 * @param d
	 */
	public void setWindSpeed(double d) {
		windSpeed = d;
	}

}
