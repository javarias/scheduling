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
 
package alma.scheduling.Define;

/**
 * The SiteCharacteristics class defines characteristics of the telescope
 * that are used by various classes.  It includes the geographical 
 * location, altitude, number of antennas, properties of those antennas,
 * minimum angle of elevation and supported frequency bands. 
 * 
 * @version 1.30 May 10, 2004
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
	private Antenna[] antenna;
	private FrequencyBand[] band;

	/**
	 * Create a SiteCharacteristics object.
	 * @param longitude The longitude, in degrees, of the location this telescope.
	 * @param latitude The latitude, in degrees, of the location of this telescope.
	 * @param timeZone The local time zone relative to UT of this telescope, i.e., 
	 * the number of hours between the local time zone and UT, i.e., localTime - UT.
	 * If the local time zone is ahead of Greenwich mean time, then zone should be 
	 * positive.  If the local time zone is behind Greenwich mean time, then zone 
	 * should be negative.  For example, 9:00 Mountain Daylight Time is 15:00 UT, 
	 * so if the local time is MDT, timeZone is -6;
	 * @param altitude The altitude of this site in meters.
	 * @param minimumElevationAngle The minimum angle of elevation in degrees.
	 * @param numberAntennas The number of antennas taht belong to this telescope.
	 * @param band The array of frequency bands supported by this site.
	 */
	public SiteCharacteristics(double longitude, double latitude, int timeZone, 
			double altitude, double minimumElevationAngle, int numberAntennas, 
			FrequencyBand[] band) {
		this.longitude = longitude * degToRad;
		this.latitude = latitude * degToRad;
		sinLatitude = Math.sin(this.latitude);
		cosLatitude = Math.cos(this.latitude);
		this.timeZone = timeZone;
		this.altitude = altitude;
		this.minimumElevationAngle = minimumElevationAngle * degToRad;
		sinMinEl = Math.sin(this.minimumElevationAngle);
		this.numberAntennas = numberAntennas;
		antenna = new Antenna [numberAntennas];
		// TODO Until site characteristics are really defined, we will just generate these.
		// This should be changed in favor of a setter method.
		for (int i = 0; i < antenna.length; ++i) {
			antenna[i] = new Antenna ((short)i, i, false);
		}
		this.band = band;
	}
	
	// TODO Add a method to get the number of antennas on-line.

	/**
	 * Get the altitude of this site in meters.
	 * @return The altitude of this site in meters.
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * Get the antennas that are at this site.
	 * @return The antennas that are at this site.
	 */
	public Antenna[] getAntenna() {
		return antenna;
	}

	/**
	 * Get the antenna with the specified antenna-id. 
	 * @return The antenna with the specified antenna-id.  Null is returned
	 * if there is no such antenna.
	 */
	public Antenna getAntenna(int antennaId) {
		for (int i = 0; i < antenna.length; ++i) {
			if (antenna[i].getAntennaId() == antennaId)
				return antenna[i];
		}
		return null;
	}

	/**
	 * Get the frequency bands supported by this site. 
	 * @return The frequency bands supported by this site.
	 */
	public FrequencyBand[] getBand() {
		return band;
	}

	/**
	 * Get the frequency band with the specified name.
	 * @return The frequency band with the specified name.
	 */
	public FrequencyBand getBand(String bandName) {
		for (int i = 0; i < band.length; ++i) {
			if (band[i].getName().equals(bandName))
				return band[i];
		}
		return null;
	}

	/**
	 * Get the cosine of the latitude of this site. 
	 * @return The cosine of the latitude of this site. 
	 */
	public double getCosLatitude() {
		return cosLatitude;
	}

	/**
	 * Get the latitude of this site in radians. 
	 * @return The latitude of this site in radians.
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Get the longitude of this site in radians.
	 * @return The longitude of this site in radians.
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Get the minimum angle of elevation in radians.
	 * @return The minimum angle of elevation in radians.
	 */
	public double getMinimumElevationAngle() {
		return minimumElevationAngle;
	}

	/**
	 * Get the number of antennas at this site.
	 * @return The number of antennas at this site.
	 */
	public int getNumberAntennas() {
		return numberAntennas;
	}

	/**
	 * Get the sine of the latitude of the this site. 
	 * @return The sine of the latitude of the this site.
	 */
	public double getSinLatitude() {
		return sinLatitude;
	}

	/**
	 * Get The sine of the minimum angle of elevation of this site. 
	 * @return The sine of the minimum angle of elevation of this site.
	 */
	public double getSinMinEl() {
		return sinMinEl;
	}

	/**
	 * Get the time zone of this site. 
	 * The local time zone is relative to UT, i.e., 
	 * the number of hours between the local time zone and UT, i.e., localTime - UT.
	 * If the local time zone is ahead of Greenwich mean time, then zone should be 
	 * positive.  If the local time zone is behind Greenwich mean time, then zone 
	 * should be negative.  For example, 9:00 Mountain Daylight Time is 15:00 UT, 
	 * so if the local time is MDT, timeZone is -6;
	 * @return The time zone of this site.
	 */
	public int getTimeZone() {
		return timeZone;
	}

}

