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
 * File SUnitSet.java
 */
 
package ALMA.scheduling.define;

import java.util.ArrayList;

/**
 * A SUnitSet is a hierarchical tree whose leaves are SUnit objects. 
 * 
 * @version 1.00  Jun 4, 2003
 * @author Allen Farris
 */
public class SUnitSet {

	// The members of this set are either SUnitSet or Sunit objects.
	private ArrayList member;
	private int name;
	
	private Priority scientificPriority;
	private Priority userPriority;
	private String dataReductionProcedureName;
	private Status pipelineStatus;
	private FlowControlExpression[] flowControl;
	private NotifyPI notify;
	private Expression scienceGoal;
	private WeatherCondition weatherConstraint;
	private SystemSetup requiredInitialSetup;
	private int maximumTimeInSeconds;
	private Status unitSetStatus;

	/**
	 * Create an SUnitSet object.
	 */
	public SUnitSet() {
		member = new ArrayList ();
	}

	/**
	 * Add an SUnitSet object as a member of this set.
	 */
	public void addMember(SUnitSet set) {
		member.add(set);
	}

	/**
	 * Add an SUnit object as a member of this set.
	 */
	public void addMember(SUnit unit) {
		member.add(unit);
	}


	/**
	 * @return
	 */
	public String getDataReductionProcedureName() {
		return dataReductionProcedureName;
	}

	/**
	 * @return
	 */
	public FlowControlExpression[] getFlowControl() {
		return flowControl;
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
	public ArrayList getMember() {
		return member;
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
	public NotifyPI getNotify() {
		return notify;
	}

	/**
	 * @return
	 */
	public Status getPipelineStatus() {
		return pipelineStatus;
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
	public Status getUnitSetStatus() {
		return unitSetStatus;
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
	 * @param string
	 */
	public void setDataReductionProcedureName(String string) {
		dataReductionProcedureName = string;
	}

	/**
	 * @param expressions
	 */
	public void setFlowControl(FlowControlExpression[] expressions) {
		flowControl = expressions;
	}

	/**
	 * @param i
	 */
	public void setMaximumTimeInSeconds(int i) {
		maximumTimeInSeconds = i;
	}

	/**
	 * @param list
	 */
	public void setMember(ArrayList list) {
		member = list;
	}

	/**
	 * @param i
	 */
	public void setName(int i) {
		name = i;
	}

	/**
	 * @param notifyPI
	 */
	public void setNotify(NotifyPI notifyPI) {
		notify = notifyPI;
	}

	/**
	 * @param status
	 */
	public void setPipelineStatus(Status status) {
		pipelineStatus = status;
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
	public void setUnitSetStatus(Status status) {
		unitSetStatus = status;
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
