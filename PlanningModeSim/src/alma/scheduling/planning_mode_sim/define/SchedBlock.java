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
 * File SchedBlock.java
 */
 
package alma.scheduling.planning_mode_sim.define;

import java.util.ArrayList;

/**
 * This is a substitute for the SchedBlock class that is really defined
 * in the ObsPrep subsystem. 
 * 
 * @version 1.00  Sep 3, 2003
 * @author Allen Farris
 */
public class SchedBlock {
	
	private String id;
	private DateTime timeSubmitted;
	private ArrayList execList;
	private String projectRef;
	private int repeatcount;
	private int maximumTime;
	private String imageScript;
	private String procedureScript;
	private Priority priority;
	private Status status;
	private String obsUnitId;
	private Equatorial[] target;

	/**
	 * Create a SchedBlock.
	 */
	public SchedBlock(String sbId) {
		execList = new ArrayList ();
	}

	public void addExecRecord(String s) {
		execList.add(s);
	}

	/**
	 * @return
	 */
	public String[] getExecList() {
		String[] x = new String [execList.size()];
		x = (String[])execList.toArray(x);
		return x;
	}

	/**
	 * @return
	 */
	public String getImageScript() {
		return imageScript;
	}

	/**
	 * @return
	 */
	public int getMaximumTime() {
		return maximumTime;
	}

	/**
	 * @return
	 */
	public String getObsUnitId() {
		return obsUnitId;
	}

	/**
	 * @return
	 */
	public Priority getPriority() {
		return priority;
	}

	/**
	 * @return
	 */
	public String getProcedureScript() {
		return procedureScript;
	}

	/**
	 * @return
	 */
	public String getProjectRef() {
		return projectRef;
	}

	/**
	 * @return
	 */
	public int getRepeatcount() {
		return repeatcount;
	}

	/**
	 * @return
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @return
	 */
	public Equatorial[] getTarget() {
		return target;
	}

	/**
	 * @param string
	 */
	public void setImageScript(String string) {
		imageScript = string;
	}

	/**
	 * @param i
	 */
	public void setMaximumTime(int i) {
		maximumTime = i;
	}

	/**
	 * @param string
	 */
	public void setObsUnitId(String string) {
		obsUnitId = string;
	}

	/**
	 * @param priority
	 */
	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	/**
	 * @param string
	 */
	public void setProcedureScript(String string) {
		procedureScript = string;
	}

	/**
	 * @param string
	 */
	public void setProjectRef(String string) {
		projectRef = string;
	}

	/**
	 * @param i
	 */
	public void setRepeatcount(int i) {
		repeatcount = i;
	}

	/**
	 * @param status
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * @param equatorials
	 */
	public void setTarget(Equatorial[] equatorials) {
		target = equatorials;
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
