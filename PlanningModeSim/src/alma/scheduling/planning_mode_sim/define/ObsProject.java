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
 * File ObsProject.java
 */
 
package alma.scheduling.planning_mode_sim.define;

/**
 * This is a substitute for the ObsProject class that is really defined
 * in the ObsPrep subsystem. 
 * 
 * @version 1.00  Sep 3, 2003
 * @author Allen Farris
 */
public class ObsProject {
	
	private String id;
	private DateTime timeSubmitted;
	private String proposalRef;
	private String name;
	private String version;
	private String PI;
	private ObsUnitSet program;

	/**
	 * Create an ObsProject.
	 */
	public ObsProject() {
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public String getPI() {
		return PI;
	}

	/**
	 * @return
	 */
	public ObsUnitSet getProgram() {
		return program;
	}

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return
	 */
	public String getProposalRef() {
		return proposalRef;
	}

	/**
	 * @return
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param string
	 */
	public void setPI(String string) {
		PI = string;
	}

	/**
	 * @param set
	 */
	public void setProgram(ObsUnitSet set) {
		program = set;
	}

	/**
	 * @param string
	 */
	public void setProposalRef(String string) {
		proposalRef = string;
	}

	/**
	 * @param string
	 */
	public void setVersion(String string) {
		version = string;
	}

	/**
	 * @return
	 */
	public DateTime getTimeSubmitted() {
		return timeSubmitted;
	}

	/**
	 * @param string
	 */
	public void setId(String string) {
		id = string;
	}

	/**
	 * @param time
	 */
	public void setTimeSubmitted(DateTime time) {
		timeSubmitted = time;
	}

}
