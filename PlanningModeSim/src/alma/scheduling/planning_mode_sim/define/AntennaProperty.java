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
 * File AntennaProperty.java
 */
 
package alma.scheduling.planning_mode_sim.define;

/**
 * The AntennaProperty class denotes the properties of an antenna
 * that are relevant to the scheduling subsystem.  At the present
 * time, these include the antennaId and their locations.  Other
 * relevant information will be added later.
 * 
 * @version 1.00  Aug 20, 2003
 * @author Allen Farris
 */
public class AntennaProperty {
	private short antennaId;
	private short locationId;
	// Other properties will be added later.

	/**
	 * Create the properties of an antenna.
	 * @param antennaId A number <= 0 and <= 32768 that represents
	 * 			how this antenna is identified.
	 * @param locationId A number <= 0 and <= 32768 that represents
	 * 			how the location on which this antenna presently resides
	 * 			is identified.
	 */
	public AntennaProperty(short antennaId, short locationId) {
		this.antennaId = antennaId;
		this.locationId = locationId; 
	} 
	                                                        
	/**
	 * Get the number that identifies this antenna.
	 * @return A number <= 0 and <= 32768 that represents
	 * 			how this antenna is identified.
	 */
	public short getAntennaId() {                                                                                                              
		return antennaId;
	}

	/**
	 * Get the number that represents the location of this antenna.
	 * @return A number <= 0 and <= 32768 that represents
	 * 			how the location on which this antenna presently resides
	 * 			is identified.
	 */
	public short getLocationId() {
		return locationId;
	}

}                                                                                           
