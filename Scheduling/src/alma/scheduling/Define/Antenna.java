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
 * File Antenna.java
 */
 
package alma.scheduling.Define;

/**
 * The Antenna class captures information about the current state of
 * an antenna.  Properties of antennnas include both static properties
 * (those that never change or change only infrequently), and dynamic 
 * properties.  These include
 * <ul>
 * <li> the antenna id
 * <li> its location id
 * <li> whether or not it has nutator capabilities
 * <li> whether it is allocated to a subarray or not,
 * <li> whether it is idle or busy,
 * <li> whether it is in manual or automatic mode,
 * <li> whether it is on-line or off-line,
 * <li> its current frequency band and frequency,
 * <li> its standby frequency band and standby frequency.
 * </ul>
 * 
 * @version 1.30 May 10, 2004
 * @author Allen Farris
 */
public class Antenna {

	private short antennaId;
	private int locationId;
	private boolean nutator;

	private short subarrayId;
	private boolean allocated;
	private boolean idle;
	private boolean manual;
	private boolean online;
	private FrequencyBand currentFrequencyBand;
	private double currentFrequency;
	private FrequencyBand standbyFrequencyBand;
	private double standbyFrequency;

	/**
	 * Create an Antenna.
	 * 
	 * @param antennaId A number <= 0 and <= 32768 that represents
	 * 			how this antenna is identified.
	 * @param locationId A number <= 0 and <= 32768 that represents
	 * 			how the location on which this antenna presently resides
	 * 			is identified.
	 * @param nutator whether or not this antenna has nutator capabilities.
	 */
	public Antenna(short antennaId, int locationId, boolean nutator) {
		this.antennaId = antennaId;
		this.locationId = locationId;
		this.nutator = nutator; 
		setOffline();
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
	public synchronized int getLocationId() {
		return locationId;
	}

	/**
	 * Get whether or not this antenna has nutator capabilities.
	 * @return true if and only if this antenna has nutator capabilities
	 */
	public boolean isNutator() {
		return nutator;
	}

	/**
	 * Update the location of an antenna.
	 * @param locationId The new location of this antenna.
	 */
	public synchronized void moveTo(int locationId) {
		this.locationId = locationId;
	}

	/**
	 * Is this antenna allocated to a subarray?
	 * @return true if and only if this antenna is allocated to a subarray.
	 */
	public synchronized boolean isAllocated() {
		return allocated;
	}

	/**
	 * Get the current frequency in GHz associated with this antenna.
	 * @return The current frequency in GHz.
	 */
	public synchronized double getCurrentFrequency() {
		return currentFrequency;
	}

	/**
	 * Get the current frequency band associated with this antenna.
	 * @return  The current frequency band.
	 */
	public synchronized FrequencyBand getCurrentFrequencyBand() {
		return currentFrequencyBand;
	}

	/**
	 * Is this antenna idle?
	 * @return true if and only if this antenna is idle.
	 */
	public synchronized boolean isIdle() {
		return idle;
	}

	/**
	 * Is this antenna in manual mode?
	 * @return true if and only if this antenna is in manual mode.
	 */
	public synchronized boolean isManual() {
		return manual;
	}

	/**
	 * Is this antenna on-line?
	 * @return true if and only if this antenna is on-line.
	 */
	public synchronized boolean isOnline() {
		return online;
	}

	/**
	 * Get the standby frequency in GHz associated with this antenna.
	 * @return The standby frequency in GHz associated with this antenna.  
	 * If there is no standby frequency, 0.0 is returned.
	 */
	public synchronized double getStandbyFrequency() {
		return standbyFrequency;
	}

	/**
	 * Get the standby frequency band associated with this antenna.
	 * @return The standby frequency band associated with this antenna.
	 * If there is no standby frequency band, null is returned.
	 */
	public synchronized FrequencyBand getStandbyFrequencyBand() {
		return standbyFrequencyBand;
	}

	/**
	 * Get the subarray-id associated with the antenna.
	 * @return The subarray-id associated with the antenna.  If this
	 * antenna is not a member of a subarray, -1 is returned.
	 */
	public synchronized short getSubarrayId() {
		return subarrayId;
	}

	/**
	 * Make this antenna a member of a subarray by setting its subarray-id.
	 * @param b The subarray-id of the subarray of which this antenna is a member. 
	 */
	public synchronized void setAllocated(short subarrayId) {
		allocated = true;
		this.subarrayId = subarrayId;
	}

	/**
	 * Mark this antenna as unallocated to any subarray.
	 */
	public synchronized void unAllocated() {
		allocated = false;
		this.subarrayId = -1;
		setIdle();
	}

	/**
	 * Set the current frequency of this antenna.
	 * @param d The current frequency of this antenna in GHz.
	 */
	public synchronized void setCurrentFrequency(double d) {
		currentFrequency = d;
	}

	/**
	 * Set the current frequency band of this antenna.
	 * @param band The current frequency band of this antenna in GHz.
	 */
	public synchronized void setCurrentFrequencyBand(FrequencyBand band) {
		currentFrequencyBand = band;
	}

	/**
	 * Mark this antenna as idle.
	 */
	public synchronized void setIdle() {
		idle = true;
	}

	/**
	 * Mark this antenna as busy.
	 */
	public synchronized void setBusy() {
		idle = false;
	}

	/**
	 * Mark this antenna as being in manual mode.
	 */
	public synchronized void setManual() {
		manual = true;
		unAllocated();
	}

	/**
	 * Mark this antenna as being in automatic mode (not in manual mode).
	 */
	public synchronized void setAutomatic() {
		manual = false;
	}

	/**
	 * Mark this antenna as being on-line.
	 */
	public synchronized void setOnline() {
		online = true;
	}

	/**
	 * Mark this antenna as being off-line.
	 */
	public synchronized void setOffline() {
		online = false;
		unAllocated();
	}

	/**
	 * Set the standby frequency of this antenna.
	 * @param d The standby frequency of this antenna in GHz.
	 */
	public synchronized void setStandbyFrequency(double d) {
		standbyFrequency = d;
	}

	/**
	 * Set the standby frequency band of this antenna.
	 * @param band The standby frequency band of this antenna in GHz.
	 */
	public synchronized void setStandbyFrequencyBand(FrequencyBand band) {
		standbyFrequencyBand = band;
	}

}
