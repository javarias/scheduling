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
 * File Telescope.java
 */
 
package alma.scheduling.define;

import java.util.ArrayList;

/**
 * The Telescope class is an abstract class that contains basic
 * data about the telescope, including site characteristics, accumulated 
 * weather data, and the current state of antennas and allocated subarrays.
 * 
 * @version 1.30 May 10, 2004
 * @author Allen Farris
 */
public abstract class Telescope {

	private SiteCharacteristics site;
	private Antenna[] antenna;
	private ArrayList subarray;
	private WeatherTrend weather;

	/**
	 * Create a Telescope model.
	 *
	 */
	protected Telescope() {
		subarray = new ArrayList ();
	}

	/**
	 * Set the site characteristics of this telescope.
	 * @param site The site characteristics associated with this telescope.
	 */
	protected void setSite(SiteCharacteristics site) {
		this.site = site;
	}

	/**
	 * Set the list of antennas that belong to this telescope.
	 * @param antenna The list of antennas that belong to this telescope.
	 */
	protected void setAntenna(Antenna[] antenna) {
		this.antenna = antenna;
	}

	/**
	 * Add a subarray to this telescope.
	 * @param array
	 */
	public synchronized void addSubarray(Subarray array) {
		subarray.add(array);
	}
	
	/**
	 * Delete a subarray from this telescope.
	 * @param array
	 */
	public synchronized void deleteSubarray(Subarray array) {
		subarray.remove(array);
	}

	/**
	 * Store weather data related to this telescope.
	 * @param x
	 */
	public synchronized void storeWeather(WeatherData x) {
		weather.store(x);
	}
	
	/**
	 * Get the site characteristics of this telescope.
	 * @return
	 */
	public SiteCharacteristics getSite() {
		return site;
	}
	
	/**
	 * Get all antennas that belong to this telescope.
	 * @return
	 */
	public Antenna[] getAntenna() {
		return antenna;
	}

	/**
	 * Get the antenna with the specified antennaId.
	 * @param antennaId
	 * @return
	 */
	public Antenna getAntenna(int antennaId) {
		for (int i = 0; i < antenna.length; ++i) {
			if (antenna[i].getAntennaId() == antennaId)
				return antenna[i];
		}
		return null;
	}
	
	/**
	 * Get all allocated subarrays.
	 * @return
	 */
	public synchronized Subarray[] getSubarray() {
		Subarray[] x = new Subarray [subarray.size()];
		x = (Subarray[])subarray.toArray(x);
		return x;
	}

	/**
	 * Get the subarray with the specified subarrayId.
	 * @param subarrayId
	 * @return
	 */	
	public synchronized Subarray getSubarray(int subarrayId) {
		Subarray x = null;
		for (int i = 0; i < subarray.size(); ++i) {
			x = (Subarray)subarray.get(i);
			if (x.getSubarrayId() == subarrayId)
				break;
		}
		return x;
	}

	/**
	 * Get all accumulated weather data.
	 * @return
	 */	
	public synchronized WeatherData[] getWeather() {
		return weather.get();
	}
	
	/**
	 * Get all weather data in the specified time interval.
	 * @param t
	 * @return
	 */
	public synchronized WeatherData[] getWeather(TimeInterval t) {
		WeatherData[] data = weather.get();
		ArrayList x = new ArrayList();
		for (int i = 0; i < data.length; ++i) {
			if (data[i].getTime().getEnd().ge(t.getStart()) &&
				data[i].getTime().getStart().le(t.getEnd()))
				x.add(data[i]);
		}
		WeatherData[] y = new WeatherData [x.size()];
		y = (WeatherData[])x.toArray(y);
		return y;
	}

}

