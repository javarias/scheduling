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
 * File Project.java
 */
 
package alma.scheduling.Define;

import java.io.PrintStream;


/**
 * An Project is an observing project as viewed by the
 * scheduling subsystem. 
 * 
 * @version $Id: Project.java,v 1.7 2006/07/17 20:53:49 sslucero Exp $
 * @author Allen Farris
 */
public class Project implements ProjectMember {
	
	// The ObsProject-id that identifies this Project.
	protected String obsProjectId;
    // The ProjectStatus-id that is associated with the Project.
    protected String projectStatusId;
	// The proposal-id that is associated with this Project. 
	protected String proposalId;
	// The name of this Project.
	protected String projectName;
	// The PI that is associated with this Project.
	protected String PI;
	// The time this project component was created. 
	protected DateTime timeOfCreation;
	// The time this project component was last updated.
	protected DateTime timeOfUpdate;
	// The status of this project component.
	protected Status status;
	
	// A reference to the obsProgram that belongs to this Project.
	protected Program program;
		
	// The time at which a breakpoint was entered.  If the Project is not
	// in a breakpoint, then breakpointTime is null.
	protected DateTime breakpointTime;
    protected String projectVersion;

	/**
	 * Construct an Project.
	 */
	public Project(String obsProjectId, String proposalId, 
            String projectName, String projectVersion, String PI) {

		this.obsProjectId  = obsProjectId;
		this.proposalId = proposalId;
		this.projectName = projectName;
		this.PI = PI;
		timeOfCreation = null;
		timeOfUpdate = null;
		status = new Status ();
		program = null;
		breakpointTime = null;
        this.projectVersion = projectVersion;
	}

	public void setMemberLink() {
		program.setTimeOfCreation(getTimeOfCreation());
		program.setTimeOfUpdate(getTimeOfCreation());
		program.setProject(this);
		program.setParent(null);
		// The program's parent is null, because it has no UnitSetMember parent.
		program.setMemberLink(program);
	}

	/**
	 * When a project changes its state from "not-defined" to "defined", it
	 * is designated to be ready to execute and a time is entered to mark this
	 * event: this is the purppose of the "setReady(time)" method.  When 
	 * a project is designated to be "ready", this implies that all its ObsUnitSets 
	 * and their members are ready.  Therefore, this setReady method is recursive,
	 * setting the ready time in all of its members, down to and including all
	 * Units that are part of this project.
	 * 
	 * @param time This time at which this UnitSet is ready.
	 */
	public void setReady(DateTime time) {
		if (time == null || time.isNull())
			throw new UnsupportedOperationException("Cannot set project to ready! Time cannot be null.");
		if (program == null)
			throw new UnsupportedOperationException("Cannot set project to ready! Project's program has not been set.");
		status.setReady(time);
		program.setReady(time);
		program.setTotals();
	}

	/**
	 * When a project enters a breakpoint, all of its ObsUnitSets and their 
	 * members enter a wait state.  Therefore, the setBreakpoint method
	 * is recursive, placing all of its members, down to and including all
	 * Units that are part of this project, in a wait state. 
	 * @param time This time at which this project entered the breakpoint.
	 */
	public void setBreakpoint(DateTime time) {
		if (time == null || time.isNull())
			throw new UnsupportedOperationException("Cannot set project breakpoint! Time cannot be null.");
		if (program == null)
			throw new UnsupportedOperationException("Cannot set project breakpoint! Project's program has not been set.");
		if (!status.isDefined())
			throw new UnsupportedOperationException("Cannot set project breakpoint! Project is not defined.");
		if (!status.isStarted())
			throw new UnsupportedOperationException("Cannot set project breakpoint! Project has not been started.");
		breakpointTime = time;
		status.setWaiting();
		program.setWaiting();
	}

	/**
	 * When a project breakpoint is cleared, all of its ObsUnitSets and their 
	 * members enter the ready state.  Therefore, the clearBreakpoint method
	 * is recursive, placing all of its members, down to and including all
	 * Units that are part of this project, in a ready state. 
	 */
	public void clearBreakpoint() {
		if (breakpointTime != null)
			throw new UnsupportedOperationException("Cannot clear project breakpoint! There is no breakpoint.");
		breakpointTime = null;
		status.setReady();
		program.setReady();
		// TODO At this point we must also re-compute some of the counters, including
		// the total time required, number of units, and sets, because new scheduling
		// blocks may have been added and other scheduling blocks (that haven't been
		// executed) may have been deleted.  We will do this when we have implemented
		// storing the state information in the archive.
	}
		
	public boolean isBreakpoint() {
		return breakpointTime != null;
	}

	public String toString() {
		String s = "Project (" + getId() + "," + getTimeOfCreation() + "," + getTimeOfUpdate() + ")" +
			"\n\t" + getProjectName() + " " + getObsProjectId() + " " +
				getProposalId() + " " + getPI() + "\nProgram" + "\n" + program;
		return s;
	}
	
	/**
	 * The printTree method prints a summary of the state of a 
	 * project to the designated PrintStream.
	 * 
	 * @param out the PrintStream to which the summary is written. 
	 */
	public void printTree(PrintStream out, String indent) {
		out.println("Project " + projectName);
		out.println("\tobsProjectId " + obsProjectId);
		out.println("\tproposalId " + proposalId);
		out.println("\tprojectName " + projectName);
		out.println("\tPI " + PI);
		out.println("\tbreakpointTime " + (breakpointTime == null ? "null" : breakpointTime.toString()));
		out.println("\ttimeOfCreation " + (timeOfCreation == null ? "null" : timeOfCreation.toString()));
		out.println("\ttimeOfUpdate " + (timeOfUpdate == null ? "null" : timeOfUpdate.toString()));
		out.println("\tstatus " + status.getState());
		program.printTree(out,"\t");
	}
	
	/**
	 * The printTreeLite method prints a simplified summary of the state of a 
	 * ProjectComponent to the designated PrintStream.
	 * 
	 * @param out the PrintStream to which the summary is written. 
	 */
	public void printTreeLite(PrintStream out, String indent) {
		out.println("Project " + projectName + " (" + obsProjectId + ") proposal " + proposalId + 
			" PI " + PI + " " + " status " + status.getStatus());
		program.printTreeLite(out,"\t");
	}
	
	/**
	 * @return Returns the project.
	 */
	public Project getProject() {
		return this;
	}

	/**
	 * @param project The project to set.
	 */
	public void setProject(Project project) {
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
		return obsProjectId;
	}

	/**
	 * Return the status of this project component.
	 * @return Returns the status of this project component.
	 */
	public Status getStatus() {
		return status;
	}

	///////////////////////////////
	// Getter and Setter methods //
	///////////////////////////////
	
	/**
	 * @return Returns the scientificPriority.
	 */
	public Priority getScientificPriority() {
		return program.getScientificPriority();
	}

	/**
	 * Set the program that belongs to this Project.
	 * @param program the UnitSet program that belongs to this Project.
	 */
	public void setProgram(Program program) {
		this.program = program;
	}
	
	/**
	 * @return Returns the breakpointTime.
	 */
	public DateTime getBreakpointTime() {
		return breakpointTime;
	}

	/**
	 * @return Returns the obsProjectId.
	 */
	public String getObsProjectId() {
		return obsProjectId;
	}

	/**
	 * @return Returns the pI.
	 */
	public String getPI() {
		return PI;
	}

	/**
	 * @return Returns the program.
	 */
	public Program getProgram() {
		return program;
	}

	/**
	 * @return Returns the projectName.
	 */
	public String getProjectName() {
		return projectName;
	}

    public String getProjectVersion() {
        return projectVersion;
    }

	/**
	 * @return Returns the proposalId.
	 */
	public String getProposalId() {
		return proposalId;
	}

	/**
	 * @return Returns the totalRequiredTimeInSeconds.
	 */
	public int getTotalRequiredTimeInSeconds() {
		return program.getTotalRequiredTimeInSeconds();
	}

	/**
	 * @return Returns the totalUsedTimeInSeconds.
	 */
	public int getTotalUsedTimeInSeconds() {
		return program.getTotalUsedTimeInSeconds();
	}

	/**
	 * @return Returns the totalUnitSets.
	 */
	public int getTotalPrograms() {
		return program.getTotalPrograms();
	}

	/**
	 * @return Returns the numberUnitSetsCompleted.
	 */
	public int getNumberProgramsCompleted() {
		return program.getNumberProgramsCompleted();
	}

	/**
	 * @return Returns the numberUnitSetsFailed.
	 */
	public int getNumberProgramsFailed() {
		return program.getNumberProgramsFailed();
	}

	/**
	 * @return Returns the totalUnits.
	 */
	public int getTotalSBs() {
		return program.getTotalSBs();
	}

	/**
	 * @return Returns the numberUnitsCompleted.
	 */
	public int getNumberSBsCompleted() {
		return program.getNumberSBsCompleted();
	}

	/**
	 * @return Returns the numberUnitsFailed.
	 */
	public int getNumberSBsFailed() {
		return program.getNumberSBsFailed();
	}
	
	/**
	 * Return all Units that belong to this Project.
	 */
	public SB[] getAllSBs() {
		return program.getAllSBs();
	}
		
	/**
	 * Return all UnitSets that belong to this Project.
	 */
	public Program[] getAllPrograms() {
		return program.getAllPrograms();
	}
	
    /**
     * @return Returns the projectStatusId.
     */
    public String getProjectStatusId() {
        return projectStatusId;
    }

    /**
     * @param projectStatusId The projectStatusId to set.
     */
    public void setProjectStatusId(String projectStatusId) {
        this.projectStatusId = projectStatusId;
    }

}
