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
 * File Subarray.java
 */
 
package alma.scheduling.Define;

/**
 * The Subarray class identifies a subarray of antennas.  Included in
 * the data associated with a subarray are:
 * <ul>
 * <li> its subarrayId,
 * <li> the list of antennas allocated to this subarray,
 * <li> its current frequency band and frequency,
 * <li> its standby frequency band and standby frequency,
 * <li> its current project, unit set, and scheduling unit.
 * </ul>
 * 
 * @version $Id: Subarray.java,v 1.4 2005/06/20 20:58:09 sslucero Exp $
 * @author Allen Farris
 */
public class Subarray {

	private String arrayName;
	boolean idle;
	private Antenna[] antenna;
	private FrequencyBand currentFrequencyBand;
	private double currentFrequency;
	private FrequencyBand standbyFrequencyBand;
	private double standbyFrequency;
	private String currentProject;
	private String currentProgram;
	private String currentSB;
	
	public Subarray (String name, Antenna[] antenna) {
		this.arrayName = name;
		this.antenna = antenna;
		setBusy();
		this.currentFrequencyBand = new FrequencyBand ("",0.0,0.0);
		this.currentFrequency = 0.0;
		this.standbyFrequencyBand = this.currentFrequencyBand;
		this.standbyFrequency = 0.0;
		this.currentProject = "";
		this.currentProgram = "";
		this.currentSB = "";
	}

	public synchronized Antenna[] getAntenna() {
		return antenna;
	}
	public synchronized boolean isIdle() {
		return idle;
	}
	public String getArrayName() {
		return arrayName;
	}

	public synchronized void setIdle() {
		idle = true;
		for (int i = 0; i < antenna.length; ++i)
			antenna[i].setIdle();
	}
	public synchronized void setBusy() {
		idle = false;
		for (int i = 0; i < antenna.length; ++i)
			antenna[i].setBusy();
	}
	/**
	 * @return
	 */
	public synchronized double getCurrentFrequency() {
		return currentFrequency;
	}

	/**
	 * @return
	 */
	public synchronized FrequencyBand getCurrentFrequencyBand() {
		return currentFrequencyBand;
	}

	/**
	 * @return
	 */
	public synchronized String getCurrentProject() {
		return currentProject;
	}

	/**
	 * @return
	 */
	public synchronized String getCurrentSB() {
		return currentSB;
	}

	/**
	 * @return
	 */
	public synchronized String getCurrentProgram() {
		return currentProgram;
	}

	/**
	 * @return
	 */
	public synchronized double getStandbyFrequency() {
		return standbyFrequency;
	}

	/**
	 * @return
	 */
	public synchronized FrequencyBand getStandbyFrequencyBand() {
		return standbyFrequencyBand;
	}

	/**
	 * @param band
	 * @param d
	 */
	public synchronized void setCurrentFrequency(FrequencyBand band, double d) {
		currentFrequencyBand = band;
		currentFrequency = d;
	}

	/**
	 * @param string
	 */
	public synchronized void setCurrentSB(String project, String unitSet, String unit) {
		currentProject = project;
		currentProgram = unitSet;
		currentSB = unit;
	}

	/**
	 * @param band
	 * @param d
	 */
	public void setStandbyFrequency(FrequencyBand band, double d) {
		standbyFrequencyBand = band;
		standbyFrequency = d;
	}

}
