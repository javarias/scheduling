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
 * File SB.java
 */
 
package alma.scheduling.Define;

import java.util.ArrayList;
import java.io.PrintStream;

/**
 * An SB is the lowest-level, atomic scheduling unit. 
 * It is a SchedBlock as viewed by the scheduling subsystem.
 * 
 * @version $Id: SB.java,v 1.9 2004/11/23 20:41:21 sslucero Exp $
 * @author Allen Farris
 */
public class SB implements ProgramMember {
	
	// Types of scheduling blocks.
	public static final int NORMAL = 0;
	public static final int INTERACTIVE = 1;
	public static final int MAINTENANCE = 2;
	public static final int TEST = 3;
	public static final int VLBI = 4;
	public static final int OTHER = 9;
	
	// The type of scheduling block.
	private int type;
	// Required starting time.
	private TimeInterval requiredStart;
	// List of relevant antennas.
	private ArrayList antennaList;
	
	// The scheduling block id that identifies this SB.
	private String schedBlockId;
	// The statusId associated with this SB.
	private String sbStatusId;
	// The project to which this SB belongs.
	private Project project;
	// The time this SB was created. 
	private DateTime timeOfCreation;
	// The time this SB was last updated.
	private DateTime timeOfUpdate;
	// The status of this SB.
	protected Status status;
	// The immediate parent of this SB.
	private Program parent;

	// The total time required to execute this SB.
	private int totalRequiredTimeInSeconds;
	// The total amount of time used by this SB so far.
	private int totalUsedTimeInSeconds;
	// Note: The total required time is:
	//			maximumTimeInSeconds * (maximumNumberOfRepeats + 1)
	
	// The members of this set are ExecBlocks.
	private ArrayList exec;
		
	private Priority scientificPriority;
	private Priority userPriority;
	private Expression scienceGoal;
	private WeatherCondition weatherConstraint;
	private SBSetup requiredInitialSetup;
	private int maximumTimeInSeconds;
	private int maximumNumberOfRepeats;
	private String imagingScript;
	private String observingScript;
	private boolean standardScript;
	private Target target;
	private double centerFrequency;
	private FrequencyBand frequencyBand;
	
	// LST range in hours -- used to support increased UV coverage.
	private double lstBegin;
	private double lstEnd;

	/**
	 * Create an SB object.
	 * @param id The archive-id of the scheduling block that this SB represents.
	 */
	public SB(String id) {
		type = NORMAL;
		requiredStart = null;
		antennaList = new ArrayList ();
		schedBlockId = id;
		project = null;
		timeOfCreation = null;
		timeOfUpdate = null;
		status = new Status ();
		parent = null;
		totalRequiredTimeInSeconds = 0;
		totalUsedTimeInSeconds = 0;
		scientificPriority = null;
		userPriority = null;
		scienceGoal = null;
		weatherConstraint = null;
		requiredInitialSetup = null;
		maximumTimeInSeconds = 0;		
		maximumNumberOfRepeats = 0;
		imagingScript = null;
		observingScript = null;
		standardScript = true;
		target = null;
		centerFrequency = 0.0;
		frequencyBand = null;
		lstBegin = -1.0;
		lstEnd = -1.0;
		exec = new ArrayList ();
	}
	
	/**
	 * Set the type of scheduling block.  The default is NORMAL.
	 * @param type
	 */
	public void setType(int type) {
		if (type < 0 || type > OTHER)
			throw new IllegalArgumentException(type + " is not a valid type of SB.");
		this.type = type;
	}
	
	/**
	 * Get the type of scheduling block.
	 * @return
	 */
	public int getType() {
		return type;
	}

	/**
	 * Set the required starting time for fixed-time SBs.
	 * @param start
	 */
	public void setRequiredStart(TimeInterval start) {
		this.requiredStart = start;
	}
	
	/**
	 * Get the required starting interval.  If the starting interval
	 * is nor specified, null is returned.
	 * @return
	 */
	public TimeInterval getRequiredStart() {
		return requiredStart;
	}
	
	/**
	 * Add an antenna-is to the list of relevant antennas.
	 * @param antennaId
	 */
	public void addAntenna(String antennaId) {
		antennaList.add(antennaId);
	}
	
	/**
	 * Get the list of antennas associated with this SB.
	 * @return
	 */
	public String[] getAntennaList() {
		String[] s = new String [antennaList.size()];
		s = (String[])antennaList.toArray(s);
		return s;
	}
	
	/**
	 * Return the internal information about this SUnit as a string.
	 */
	public String toString() {
		return "\tSUnit (" + getId() + "," + getTimeOfCreation() + "," + getTimeOfUpdate() + ") [" +
		getProject().getId() + "," + getParent().getId() + "] " +
		getSchedBlockId() + " " + getScientificPriority() + " " + getTarget() + " " +
		getCenterFrequency() + " " + getStatus();
	}

	/**
	 * The printTree method prints a summary of the state of a 
	 * Unit to the designated PrintStream.
	 * 
	 * @param out the PrintStream to which the summary is written. 
	 */
	public void printTree(PrintStream out, String indent) {
		out.println(indent + "Unit " + schedBlockId);
		out.println(indent + "\tparent " + (parent == null ? "null" : parent.getId()));
		out.println(indent + "\ttotalRequiredTimeInSeconds " + totalRequiredTimeInSeconds); 
		out.println(indent + "\ttotalUsedTimeInSeconds " + totalUsedTimeInSeconds);
		out.println(indent + "\tscientificPriority " + (scientificPriority == null ? "null" : scientificPriority.toString()));
		out.println(indent + "\tuserPriority " + (userPriority == null ? "null" : userPriority.toString()));
		out.println(indent + "\tscienceGoal " + (scienceGoal == null ? "null" : scienceGoal.toString()));
		out.println(indent + "\tweatherConstraint " + (weatherConstraint == null ? "null" : weatherConstraint.toString()));
		out.println(indent + "\trequiredInitialSetup " + (requiredInitialSetup == null ? "null" : requiredInitialSetup.toString()));
		out.println(indent + "\tmaximumTimeInSeconds " + maximumTimeInSeconds);
		out.println(indent + "\tmaximumNumberOfRepeats " + maximumNumberOfRepeats);
		out.println(indent + "\timagingScript " + imagingScript);
		out.println(indent + "\tobservingScript " + observingScript);
		out.println(indent + "\tstandardScript " + standardScript);
		out.println(indent + "\ttarget " + target);
		out.println(indent + "\tcenterFrequency " + centerFrequency);
		out.println(indent + "\tfrequencyBand " + (frequencyBand == null ? "null" : frequencyBand.toString()));
		out.println(indent + "\tproject " + (project == null ? "null" : project.getId()));
		out.println(indent + "\ttimeOfCreation " + (timeOfCreation == null ? "null" : timeOfCreation.toString()));
		out.println(indent + "\ttimeOfUpdate " + (timeOfUpdate == null ? "null" : timeOfUpdate.toString()));
		out.println(indent + "\tstatus " + status.getState());
		out.println(indent + "\tnumber of executions " + exec.size());
		ExecBlock x = null;
		for (int i = 0; i < exec.size(); ++i) {
			x = (ExecBlock)exec.get(i);
			out.println(indent + "\t" + x.getExecId());
		}
	}

	/**
	 * The printTreeLite method prints a simplified summary of the state of a 
	 * ProjectComponent to the designated PrintStream.
	 * 
	 * @param out the PrintStream to which the summary is written. 
	 */
	public void printTreeLite(PrintStream out, String indent) {
		out.println(indent + "Unit " + schedBlockId + 
				" parent " + (parent == null ? "null" : parent.getId()) + 
				" project " + (project == null ? "null" : project.getId()) + 
				" status " + status.getStatus());
		ExecBlock x = null;
		for (int i = 0; i < exec.size(); ++i) {
			x = (ExecBlock)exec.get(i);
			out.println(indent + "\t" + x.getExecId());
		}
		
	}
	
	// Methods related to manipulating execution blocks.
	
	/**
	 * Get the number of ExecBlocks that belong to this SB.
	 * @return The number of ExecBlocks that belong to this SB.
	 */	
	public int getNumberExec() {
		return exec.size();
	}
	
	/**
	 * Add an ExecBlock to this SB.
	 * @param x The ExecBlock to be added.
	 */
	private void addExec(ExecBlock x) {
		x.setProject(getProject());
		x.setParent(this);
		exec.add(x);
	}

	/**
	 * Return the ExecBlocks that belong to this SB.
	 * @return the ExecBlocks that belong to this SB.
	 */
	public ExecBlock[] getExec() {
		ExecBlock[] list = new ExecBlock [exec.size()];
		return (ExecBlock[])exec.toArray(list);
	}

	/**
	 * Get an ExecBlock by specifying its index.
	 * @param index The index of the ExecBlock to be returned.
	 * @return The ExecBlock with the specified index
	 * or null, if there was no such object.
	 */
	public ExecBlock getExec(int index) {
		if (index < 0 || index >= exec.size())
			return null; 
		return (ExecBlock)(exec.get(index));
	}

	// Methods for manipulating state.
	
	/**
	 * Set the time when this SB is ready.  At this point the total
	 * required time is also set.
	 * 
	 * @param time This time at which this SB is ready.
	 */
	public void setReady(DateTime time) {
		status.setReady(time);
		// Set the total time required.
		totalRequiredTimeInSeconds = maximumTimeInSeconds * (maximumNumberOfRepeats + 1);
	}
	
	/**
	 * The setStartTime method marks the start of the first time
	 * this SB is executed.  The method sets its own start time
	 * and informs its parent that this event has occurred.  The information
	 * flow is upwards -- if the parent has not started, then its start
	 * time is set and its parent is informed -- all the way up to the
	 * project level.
	 * 
	 * @param time The time at which this SB started.
	 */
	public void setStartTime(DateTime time) {
		status.setStarted(time);
		parent.unitStarted(this,time);
	}
	
	/**
	 * The setStartTime method marks the start of the first time
	 * this SB is executed.  The method sets its own start time
	 * and informs its parent that this event has occurred.  The information
	 * flow is upwards -- if the parent has not started, then its start
	 * time is set and its parent is informed -- all the way up to the
	 * project level.
	 * 
	 * @param time The time at which this SB started.
	 */
	public void setRunning() {
		status.setRunning();
		parent.setAllRunning();
	}
	
	/**
	 * The execEnd method marks when this Unit has finished execution.
	 * The analysis of the circumstances surrounding the execution is
	 * carries out by the project manager.  The project manager figures
	 * out what to do and changes the state of the Unit.  The time used
	 * is updated only if the execution block is not null and its state 
	 * is COMPLETE. Appropriate actions are taken to update and change
	 * the state of the unit's parent; and, this information flow
	 * continues upward all the way to the project level.
	 *   
	 * @param ex The UnitExec object that was produced as a result of its execution.
	 * 			 This object may be null, which means that the execution produced no
	 * 			 results that should be attached to this unit.
	 * @param time The time at which this Unit ended.
	 * @param int The state to which this unit should be changed.
	 */
	public void execEnd(ExecBlock ex, DateTime time, int state) {
			if (ex != null) {
				// Add the exec block reference to the SB.
				addExec(ex);
				if (ex.getStatus().isComplete()) {
                    System.out.println("Exec is complete");
					// This is the "normal" case; the execution was successful.
					// Increment the amount of time used in this Unit ...
					totalUsedTimeInSeconds += maximumTimeInSeconds;
					// If the maximum number of repeats has been reached ...
				//Sohaila: removed temporarily if (getNumberExec() > maximumNumberOfRepeats) {
						// ... then mark it complete and set its end time.
						status.setEnded(time,Status.COMPLETE);
						// ... and inform the parent.
						parent.execEnd(this,time,maximumTimeInSeconds,Status.COMPLETE);
                        //System.out.println("sb repeat count met.");
				//Sohaila: removed temporarily 	} else {
						// The only possible state change in this case is to "ready".
                        //System.out.println("sb repeat count not met.");
				//Sohaila: removed temporarily 		parent.execEnd(this,time,maximumTimeInSeconds,Status.READY);
				//Sohaila: removed temporarily 		status.setReady();
				//Sohaila: removed temporarily 	}
				} else {
					// In this case, the execution failed; however, the execution did
					// produce some results that are attached to the unit.  The time 
					// used is not updated.  We still have to inform the parent.
					// The ony possible state change is to "ready".
					parent.execEnd(this,time,0,Status.READY);
					status.setReady();
				}
			} else {
				// In this case the execution produced no useful results.
				// The time used is not updated.
				// The state change could be either to "aborted", or "ready",
				// or to "waiting".
				if (state == Status.ABORTED) {
					status.setEnded(time,state);
					parent.execEnd(this,time,0,Status.ABORTED);
				} else if (state == Status.WAITING) {
					status.setWaiting();
					parent.execEnd(this,time,0,Status.WAITING);
				} else if (state == Status.READY) {
					parent.execEnd(this,time,0,Status.READY);
					status.setReady();
				} else 
					throw new IllegalStateException("Invalid state change! Exec block is null. Cannot change state to COMPLETE!");
			}
	}

	////////////////////////////////////////////////////////////////////////
	// Routine getters and setters
	////////////////////////////////////////////////////////////////////////

	/**
	 * @return true if an LST range has been defined.
	 */
	public boolean isLSTRange() {
		if (lstBegin < 0.0)
			return false;
		return true;
	}
	
	/**
	 * @return the beginning LST in hours.  If there is no such time, -1.0 is returned.
	 */
	public double getBeginLST() {
		return lstBegin;
	}
	
	/**
	 * @return the ending LST in hours.  If there is no such time, -1.0 is returned.
	 */
	public double getEndLST() {
		return lstEnd;
	}
	
	/**
	 * Set the LST range.
	 * @param begin The beginning LST in hours. 
	 * @param end The ending LST in hours.
	 */
	public void setLSTRange(double begin, double end) {
		if (begin < 0.0 || begin >= 24.0 || end < 0.0 || end >= 24.0 || end <= begin)
			throw new IllegalArgumentException("invalid LST range " + begin + " " + end);
		lstBegin = begin;
		lstEnd = end;
	}
	
	/**
	 * @return Returns the frequency.
	 */
	public double getCenterFrequency() {
		return centerFrequency;
	}

	/**
	 * @param frequency The frequency to set.
	 */
	public void setCenterFrequency(double centerFrequency) {
		this.centerFrequency = centerFrequency;
	}

	/**
	 * @return Returns the frequencyBand.
	 */
	public FrequencyBand getFrequencyBand() {
		return frequencyBand;
	}

	/**
	 * @param frequencyBand The frequencyBand to set.
	 */
	public void setFrequencyBand(FrequencyBand frequencyBand) {
		this.frequencyBand = frequencyBand;
	}

	/**
	 * @return Returns the imagingScript.
	 */
	public String getImagingScript() {
		return imagingScript;
	}

	/**
	 * @param imagingScript The imagingScript to set.
	 */
	public void setImagingScript(String imagingScript) {
		this.imagingScript = imagingScript;
	}

	/**
	 * @return Returns the maximumNumberOfRepeats.
	 */
	public int getMaximumNumberOfRepeats() {
		return maximumNumberOfRepeats;
	}

	/**
	 * @param maximumNumberOfRepeats The maximumNumberOfRepeats to set.
	 */
	public void setMaximumNumberOfRepeats(int maximumNumberOfRepeats) {
		this.maximumNumberOfRepeats = maximumNumberOfRepeats;
	}

	/**
	 * @return Returns the maximumTimeInSeconds.
	 */
	public int getMaximumTimeInSeconds() {
		return maximumTimeInSeconds;
	}

	/**
	 * @param maximumTimeInSeconds The maximumTimeInSeconds to set.
	 */
	public void setMaximumTimeInSeconds(int maximumTimeInSeconds) {
		this.maximumTimeInSeconds = maximumTimeInSeconds;
	}

	/**
	 * @return Returns the observingScript.
	 */
	public String getObservingScript() {
		return observingScript;
	}

	/**
	 * @param observingScript The observingScript to set.
	 */
	public void setObservingScript(String observingScript) {
		this.observingScript = observingScript;
	}

	/**
	 * @return Returns the parent.
	 */
	public Program getParent() {
		return parent;
	}

	/**
	 * @param parent The parent to set.
	 */
	public void setParent(Program parent) {
		this.parent = parent;
	}

	/**
	 * @return Returns the requiredInitialSetup.
	 */
	public SBSetup getRequiredInitialSetup() {
		return requiredInitialSetup;
	}

	/**
	 * @param requiredInitialSetup The requiredInitialSetup to set.
	 */
	public void setRequiredInitialSetup(SBSetup requiredInitialSetup) {
		this.requiredInitialSetup = requiredInitialSetup;
	}

	/**
	 * @return Returns the scienceGoal.
	 */
	public Expression getScienceGoal() {
		return scienceGoal;
	}

	/**
	 * @param scienceGoal The scienceGoal to set.
	 */
	public void setScienceGoal(Expression scienceGoal) {
		this.scienceGoal = scienceGoal;
	}

	/**
	 * @return Returns the scientificPriority.
	 */
	public Priority getScientificPriority() {
		return scientificPriority;
	}

	/**
	 * @param scientificPriority The scientificPriority to set.
	 */
	public void setScientificPriority(Priority scientificPriority) {
		this.scientificPriority = scientificPriority;
	}

	/**
	 * @return Returns the standardScript.
	 */
	public boolean isStandardScript() {
		return standardScript;
	}

	/**
	 * @param standardScript The standardScript to set.
	 */
	public void setStandardScript(boolean standardScript) {
		this.standardScript = standardScript;
	}

	/**
	 * @return Returns the target.
	 */
	public Target getTarget() {
		return target;
	}

	/**
	 * @param target The target to set.
	 */
	public void setTarget(Target target) {
		this.target = target;
	}

	/**
	 * @return Returns the userPriority.
	 */
	public Priority getUserPriority() {
		return userPriority;
	}

	/**
	 * @param userPriority The userPriority to set.
	 */
	public void setUserPriority(Priority userPriority) {
		this.userPriority = userPriority;
	}

	/**
	 * @return Returns the weatherConstraint.
	 */
	public WeatherCondition getWeatherConstraint() {
		return weatherConstraint;
	}

	/**
	 * @param weatherConstraint The weatherConstraint to set.
	 */
	public void setWeatherConstraint(WeatherCondition weatherConstraint) {
		this.weatherConstraint = weatherConstraint;
	}

	/**
	 * @return Returns the schedBlockId.
	 */
	public String getSchedBlockId() {
		return schedBlockId;
	}

	/**
	 * @return Returns the totalRequiredTimeInSeconds.
	 */
	public int getTotalRequiredTimeInSeconds() {
		return totalRequiredTimeInSeconds;
	}

	/**
	 * @return Returns the totalUsedTimeInSeconds.
	 */
	public int getTotalUsedTimeInSeconds() {
		return totalUsedTimeInSeconds;
	}
	
	/**
	 * @return Returns the project.
	 */
	public Project getProject() {
		return project;
	}

	/**
	 * @param project The project to set.
	 */
	public void setProject(Project project) {
		this.project = project;
	}

	/**
	 * Return the time this project component was created.
	 * @return Returns the time this project component was created.
	 */
	public DateTime getTimeOfCreation() {
		return timeOfCreation;
	}

	/**
	 * Set the time this project component was created.
	 * @param t The time this project component was created.
	 */
	public void setTimeOfCreation(DateTime timeOfCreation) {
		this.timeOfCreation = timeOfCreation;
	}

	/**
	 * Return the time this project component was updated.
	 * @return Returns the time this project component was updated.
	 */
	public DateTime getTimeOfUpdate() {
		return timeOfUpdate;
	}

	/**
	 * Set the time this project component was updated.
	 * @param t The time this project component was updated.
	 */
	public void setTimeOfUpdate(DateTime timeOfUpdate) {
		this.timeOfUpdate = timeOfUpdate;
	}

	/**
	 * Return the unique identifier of this project component.
	 * @return Returns the unique identifier of this project component.
	 */
	public String getId() {
		return schedBlockId;
	}

	/**
	 * Return the status of this project component.
	 * @return Returns the status of this project component.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @return Returns the sbStatusId.
	 */
	public String getSbStatusId() {
		return sbStatusId;
	}

	/**
	 * @param sbStatusId The sbStatusId to set.
	 */
	public void setSbStatusId(String sbStatusId) {
		this.sbStatusId = sbStatusId;
	}

}

