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
 * File Horizontal.java
 */
package alma.scheduling.Define;

/**
 * Horizontal coordinates -- altitude, azimuth, and the equinox
 * 
 * @version $Id: Horizontal.java,v 1.2 2004/11/23 20:41:21 sslucero Exp $
 * @author Allen Farris
 */
public class Horizontal {
	/**
	 * The altitude in radians.
	 */
	private double altitude;
	/**
	 * The azimuth in radians.
	 */
	private double azimuth;
	/**
	 * The equinox.
	 */
	private DateTime equinox;

	/**
	 * Create Horizontal coordinates.
	 * @param altitude The altitude, in radians, positive above the horizon, negative below.
	 * @param azimuth The azimuth, in radians, measured westward from the South.
	 * @param equinox The equinox.
	 */
	public  Horizontal(double altitude, double azimuth, DateTime equinox) {
		this.altitude = altitude;
		this.azimuth = azimuth;
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
	 * Get the altitude in radians.
	 * @return The altitude in radians.
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * Get the azimuth in radians.
	 * @return The azimuth in radians.
	 */
	public double getAzimuth() {
		return azimuth;
	}

	/**
	 * Get the altitude in degrees.
	 * @return The altitude in degrees.
	 */
	public double getAltitudeInDegrees() {
		return Math.toDegrees(altitude);
	}

	/**
	 * Get the azimuth in degrees.
	 * @return The azimuth in degrees.
	 */
	public double getAzimuthInDegrees() {
		return Math.toDegrees(azimuth);
	}

}
