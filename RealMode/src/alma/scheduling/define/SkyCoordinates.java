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
 * File SkyCoordinates.java
 */
 
package alma.scheduling.define;

/**
 * Description 
 * 
 * @version 1.00  Jun 4, 2003
 * @author Allen Farris
 */
public class SkyCoordinates {

	private double longitude;
	private double latitude;
	private String epoch;

	public SkyCoordinates(double longitude, double latitude, String epoch) {
		this.longitude = longitude;
		this.latitude = latitude;
		this.epoch = epoch;
	}

	/**
	 * @return
	 */
	public String getEpoch() {
		return epoch;
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

}
