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
 * File Ecliptical.java
 */
package alma.scheduling.planning_mode_sim.define;

/**
 * Ecliptical coordinates -- clestial longitude, latitude, and the equinox.
 * 
 * @version 1.00  Jul 1, 2003
 * @author Allen Farris
 */
public class Ecliptical {
	/**
	 * The clestial longitude in radians.
	 */
	private double longitude;
	/**
	 * The clestial latitude in radians.
	 */
	private double latitude;
	/**
	 * The equinox.
	 */
	private DateTime equinox;

	/**
	 * Create  Ecliptical coordinates.
	 * @param longitude The clestial longitude in radians.
	 * @param latitude The clestial latitude in radians.
	 * @param equinox The equinox.
	 */
	public  Ecliptical(double longitude, double latitude, DateTime equinox) {
		this.longitude = longitude;
		this.latitude = latitude;
		this.equinox = equinox;
	}

	/**
	 * Get the equinox associated with this coordinate.
	 * @return The equinox.
	 */
	public DateTime getEquinox() {
		return equinox;
	}

	/**
	 * Get the clestial latitude in radians.
	 * @return The clestial latitude in radians.
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Get the clestial longitude in radians.
	 * @return The clestial longitude in radians.
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Get the clestial latitude in degrees.
	 * @return The clestial latitude in degrees.
	 */
	public double getLatitudeInDegrees() {
		return Math.toDegrees(latitude);
	}

	/**
	 * Get the clestial longitude in degrees.
	 * @return The clestial longitude in degrees.
	 */
	public double getLongitudeInDegrees() {
		return Math.toDegrees(longitude);
	}
}
