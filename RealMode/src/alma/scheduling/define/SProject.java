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
 * File SProject.java
 */
 
package ALMA.scheduling.define;


// Only to get this to compile.
class ObsProject { String dummy; }

/**
 * An SProject is an observing project as viewed by the
 * scheduling subsystem. 
 * 
 * @version 1.00  Jun 4, 2003
 * @author Allen Farris
 */
public class SProject {
	
	private String obsProjectId;
	private String projectName;
	private String PI;
	private STime dateOfSubmission;
	private STime startTime;
	private STime endTime;
	private int totalRequiredTimeInSeconds;
	private int totalUsedTimeInSeconds;
	private int totalUnits;
	private int numberUnitsCompleted;
	private int numberUnitsFailed;
	private Status projectStatus;
	private boolean breakpoint;
	private SUnitSet unitSet;

	/**
	 * Construct an SProject from an ObsProject object.
	 */
	public SProject(ObsProject project) {
		// Not implemented at this time.
	}



	/**
	 * @return
	 */
	public boolean isBreakpoint() {
		return breakpoint;
	}

	/**
	 * @return
	 */
	public STime getDateOfSubmission() {
		return dateOfSubmission;
	}

	/**
	 * @return
	 */
	public STime getEndTime() {
		return endTime;
	}

	/**
	 * @return
	 */
	public int getNumberUnitsCompleted() {
		return numberUnitsCompleted;
	}

	/**
	 * @return
	 */
	public int getNumberUnitsFailed() {
		return numberUnitsFailed;
	}

	/**
	 * @return
	 */
	public String getObsProjectId() {
		return obsProjectId;
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
	public String getProjectName() {
		return projectName;
	}

	/**
	 * @return
	 */
	public Status getProjectStatus() {
		return projectStatus;
	}

	/**
	 * @return
	 */
	public STime getStartTime() {
		return startTime;
	}

	/**
	 * @return
	 */
	public int getTotalRequiredTimeInSeconds() {
		return totalRequiredTimeInSeconds;
	}

	/**
	 * @return
	 */
	public int getTotalUnits() {
		return totalUnits;
	}

	/**
	 * @return
	 */
	public int getTotalUsedTimeInSeconds() {
		return totalUsedTimeInSeconds;
	}

	/**
	 * @return
	 */
	public SUnitSet getUnitSet() {
		return unitSet;
	}

	/**
	 * @param b
	 */
	public void setBreakpoint(boolean b) {
		breakpoint = b;
	}

	/**
	 * @param time
	 */
	public void setDateOfSubmission(STime time) {
		dateOfSubmission = time;
	}

	/**
	 * @param time
	 */
	public void setEndTime(STime time) {
		endTime = time;
	}

	/**
	 * @param i
	 */
	public void setNumberUnitsCompleted(int i) {
		numberUnitsCompleted = i;
	}

	/**
	 * @param i
	 */
	public void setNumberUnitsFailed(int i) {
		numberUnitsFailed = i;
	}

	/**
	 * @param string
	 */
	public void setObsProjectId(String string) {
		obsProjectId = string;
	}

	/**
	 * @param string
	 */
	public void setPI(String string) {
		PI = string;
	}

	/**
	 * @param string
	 */
	public void setProjectName(String string) {
		projectName = string;
	}

	/**
	 * @param status
	 */
	public void setProjectStatus(Status status) {
		projectStatus = status;
	}

	/**
	 * @param time
	 */
	public void setStartTime(STime time) {
		startTime = time;
	}

	/**
	 * @param i
	 */
	public void setTotalRequiredTimeInSeconds(int i) {
		totalRequiredTimeInSeconds = i;
	}

	/**
	 * @param i
	 */
	public void setTotalUnits(int i) {
		totalUnits = i;
	}

	/**
	 * @param i
	 */
	public void setTotalUsedTimeInSeconds(int i) {
		totalUsedTimeInSeconds = i;
	}

	/**
	 * @param set
	 */
	public void setUnitSet(SUnitSet set) {
		unitSet = set;
	}

}
