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
 * File SiteCharacteristics.java
 */
 
package alma.scheduling.planning_mode_sim.define;

import alma.scheduling.planning_mode_sim.simulator.SimulationInput;
import alma.scheduling.planning_mode_sim.simulator.SimulationException;
import alma.scheduling.planning_mode_sim.simulator.Tag;

import java.util.StringTokenizer;
import java.util.NoSuchElementException;

/**
 * The SiteCharacteristics class defines characteristics of the telescope
 * used that are used by various classes.  It includes the geographical 
 * location, altitude, number of antennas, and minimum angle of elevation. 
 * 
 * @version 1.00  Jun 6, 2003
 * @author Allen Farris
 */
public class SiteCharacteristics {

	static private final double radToDeg = 180.0 / Math.PI;
	static private final double radToHour =  12.0 / Math.PI;
	static private final double degToRad = Math.PI / 180.0;
	static private final double hourToRad =  Math.PI / 12.0;
	
	private double longitude;
	private double latitude;
	private double sinLatitude;
	private double cosLatitude;
	private int timeZone;
	private double altitude;
	private double minimumElevationAngle;
	private double sinMinEl;
	private int numberAntennas;
	private AntennaProperty[] antenna;
	private FrequencyBand[] band;

	/**
	 * Create a default SiteCharacteristics object.
	 */
	public SiteCharacteristics() {
	}

	/**
	 * Create a SiteCharacteristics object from a simulation data object.
	 * The structure of a property file is given below.
	 * 
	 * 	# Site longitude in degrees.
	 * 	Site.longitude = 107.6177275;
	 * 	# Site latitude in degrees.
	 * 	Site.latitude = 34.0787491666667
	 *  # The time zone of the site.
	 *  Site.timeZone = -6
	 * 	# Site altitude in meters.
	 * 	Site.altitude = 2124.0
	 * 	# Site minimum elevation angle above the horizon.
	 * 	Site.minimumElevationAngle = 8.0
	 * 	# Number of antennas.
	 * 	Site.numberAntennas = 64
	 */
	public SiteCharacteristics(SimulationInput property) throws SimulationException {
		longitude = property.getDouble(Tag.longitude) * degToRad;
		latitude = property.getDouble(Tag.latitude) * degToRad;
		sinLatitude = Math.sin(latitude);
		cosLatitude = Math.cos(latitude);
		timeZone = property.getInt(Tag.timeZone);
		altitude = property.getDouble(Tag.altitude);
		minimumElevationAngle = property.getDouble(Tag.minimumElevationAngle) * degToRad;
		sinMinEl = Math.sin(minimumElevationAngle);
		numberAntennas = property.getInt(Tag.numberAntennas);
		if (numberAntennas < 0)
			throw new SimulationException("SiteCharacteristics","Number of antennas cannot be negative.");
		antenna = new AntennaProperty [numberAntennas];
		for (int i = 0; i < numberAntennas; ++i)
			antenna[i] = new AntennaProperty((short)i,(short)i);
		getBand(property);
	}

	private void getBand(SimulationInput property) throws SimulationException {
		// Get the frequency bands form the input.
		// Syntax:
		// 		FrequencyBand.numberOfBands = N
		//		FrequencyBand.<i> = nameOfBand; minimumFrequency; maximumFrequency
		// where 0 <= i < N, and N is the number of frequency bands. Frequencies are in GHz.
		band = new FrequencyBand [property.getInt(Tag.numberOfBands)];
		StringTokenizer token = null;
		String value = null;
		String name = null;
		double min = 0.0;
		double max = 0.0;
		String tmp = null;
		for (int i = 0; i < band.length; ++i) {
			value = property.getString(Tag.band + "." + i);
			token = new StringTokenizer(value,";");
			try {
				name = token.nextToken().trim();
				tmp = token.nextToken().trim();
				min = Double.parseDouble(tmp);
				tmp = token.nextToken().trim();
				max = Double.parseDouble(tmp);
			} catch (NumberFormatException err) {
				throw new SimulationException("SiteCharacteristics","Invalid number format in " + tmp);
			} catch (NoSuchElementException err) {
				throw new SimulationException("SiteCharacteristics","Missing element in project string: " + value);
			}
			band[i] = new FrequencyBand(name,min,max);
		}
	}

	/**
	 * @return
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * @return
	 */
	public AntennaProperty[] getAntennas() {
		return antenna;
	}

	/**
	 * @return
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @return
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @return
	 */
	public double getMinimumElevationAngle() {
		return minimumElevationAngle;
	}

	/**
	 * @return
	 */
	public int getNumberAntennas() {
		return numberAntennas;
	}

	/**
	 * @return
	 */
	public int getTimeZone() {
		return timeZone;
	}

	/**
	 * @return
	 */
	public FrequencyBand[] getFrequencyBand() {
		return band;
	}

	/**
	 * @return
	 */
	public double getCosLatitude() {
		return cosLatitude;
	}

	/**
	 * @return
	 */
	public double getSinLatitude() {
		return sinLatitude;
	}

	/**
	 * @return
	 */
	public double getSinMinEl() {
		return sinMinEl;
	}

}

