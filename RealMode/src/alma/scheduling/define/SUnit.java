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
 * File SUnit.java
 */
 
package ALMA.scheduling.define;

// Only to get this to compile.
class SchedBlock { String dummy; }

/**
 * An SUnit is the lowest-level, atomic scheduling unit. 
 * 
 * @version 1.00  Jun 4, 2003
 * @author Allen Farris
 */
public class SUnit {

	private String schedBlockId;
	private int name;
		
	private Priority scientificPriority;
	private Priority userPriority;
	private Expression scienceGoal;
	private WeatherCondition weatherConstraint;
	private SystemSetup requiredInitialSetup;
	private int maximumTimeInSeconds;

	private SkyCoordinates coordinates;
	private boolean isStandardScript;
	private Status unitStatus;
	
	/**
	 * Create an SUnit object from a SchedBlock object.
	 */
	public SUnit(SchedBlock sb) {
	}


	/**
	 * @return
	 */
	public SkyCoordinates getCoordinates() {
		return coordinates;
	}

	/**
	 * @return
	 */
	public boolean isStandardScript() {
		return isStandardScript;
	}

	/**
	 * @return
	 */
	public int getMaximumTimeInSeconds() {
		return maximumTimeInSeconds;
	}

	/**
	 * @return
	 */
	public int getName() {
		return name;
	}

	/**
	 * @return
	 */
	public SystemSetup getRequiredInitialSetup() {
		return requiredInitialSetup;
	}

	/**
	 * @return
	 */
	public Expression getScienceGoal() {
		return scienceGoal;
	}

	/**
	 * @return
	 */
	public Priority getScientificPriority() {
		return scientificPriority;
	}

	/**
	 * @return
	 */
	public Status getUnitStatus() {
		return unitStatus;
	}

	/**
	 * @return
	 */
	public Priority getUserPriority() {
		return userPriority;
	}

	/**
	 * @return
	 */
	public WeatherCondition getWeatherConstraint() {
		return weatherConstraint;
	}

	/**
	 * @param coordinates
	 */
	public void setCoordinates(SkyCoordinates coordinates) {
		this.coordinates = coordinates;
	}

	/**
	 * @param b
	 */
	public void setStandardScript(boolean b) {
		isStandardScript = b;
	}

	/**
	 * @param i
	 */
	public void setMaximumTimeInSeconds(int i) {
		maximumTimeInSeconds = i;
	}

	/**
	 * @param i
	 */
	public void setName(int i) {
		name = i;
	}

	/**
	 * @param setup
	 */
	public void setRequiredInitialSetup(SystemSetup setup) {
		requiredInitialSetup = setup;
	}

	/**
	 * @param expression
	 */
	public void setScienceGoal(Expression expression) {
		scienceGoal = expression;
	}

	/**
	 * @param priority
	 */
	public void setScientificPriority(Priority priority) {
		scientificPriority = priority;
	}

	/**
	 * @param status
	 */
	public void setUnitStatus(Status status) {
		unitStatus = status;
	}

	/**
	 * @param priority
	 */
	public void setUserPriority(Priority priority) {
		userPriority = priority;
	}

	/**
	 * @param condition
	 */
	public void setWeatherConstraint(WeatherCondition condition) {
		weatherConstraint = condition;
	}

}
