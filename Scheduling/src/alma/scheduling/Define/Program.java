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
 * File Program.java
 */
 
package alma.scheduling.Define;

import java.util.ArrayList;
import java.io.PrintStream;

/**
 * A Program is a hierarchical tree whose leaves are SB objects. 
 * It is an ObsProgram as viewed by the scheduling subsystem.
 * 
 * @version $Id: Program.java,v 1.7 2005/03/29 20:49:16 sslucero Exp $
 * @author Allen Farris
 */
public class Program implements ProgramMember {
	// The unique id of this entity -- which is the ObsUnitSet part-id.
	private String programId;
	// The statusId assoiciated with this Program.
	private String obsUnitSetStatusId;
	// The project to which this Program belongs.
	private Project project;
	// The time this Program was created. 
	private DateTime timeOfCreation;
	// The time this Program was last updated.
	private DateTime timeOfUpdate;
	// The status of this Program.
	protected Status status;
	// The immediate parent of this Program.
	private Program parent;
	
	// The members of this set are either Program or SB objects.
	private ArrayList member;
	
	// The ObservedSessions that belong to this Program.
	private ArrayList session;
	
	// The science pipeline processing request tha belong to this program,
	// which may be null.
	private SciPipelineRequest sciPipelineRequest;
	
	// The total time required to execute this Program.
	private int totalRequiredTimeInSeconds;
	// The total amount of time used by this Program so far.
	private int totalUsedTimeInSeconds;
	// The total number of Programs that belong to this Program.
	private int totalPrograms;
	// The total number of Programs belonging to this set that have been successfully completed. 
	private int numberProgramsCompleted;
	// The total number of Programs belonging to this set that have failed.
	private int numberProgramsFailed;
	// The total number of SBs that belong to this set.
	private int totalSBs;
	// The total number of SBs belonging to this set that have been successfully completed. 
	private int numberSBsCompleted;
	// The total number of SBs belonging to this set that have failed.
	private int numberSBsFailed;
	
	private Priority scientificPriority;
	private Priority userPriority;
	private String dataReductionProcedureName;
	private Object[] dataReductionParameters;
	private Status pipelineStatus;
	private FlowControl[] flowControl;
	private NotifyPI notify;
	private Expression scienceGoal;
	private WeatherCondition weatherConstraint;
	private double centerFrequency;
	private FrequencyBand frequencyBand;
	private SBSetup requiredInitialSetup;
	private int maximumTimeInSeconds;
	
	/**
	 * Create an Program object.
	 */
	public Program(String id) {
		programId = id;
        obsUnitSetStatusId = id;
		project = null;
		timeOfCreation = null;
		timeOfUpdate = null;
		status = new Status ();
		member = new ArrayList ();
		session = new ArrayList();

		parent = null;
		totalRequiredTimeInSeconds = 0;
		totalUsedTimeInSeconds = 0;
		totalPrograms = 0;
		numberProgramsCompleted = 0;
		numberProgramsFailed = 0;
		totalSBs = 0;
		numberSBsCompleted = 0;
		numberSBsFailed = 0;
		scientificPriority = new Priority (Priority.BACKGROUND);
		userPriority = new Priority (Priority.BACKGROUND);
		dataReductionProcedureName = "";
		dataReductionParameters = new Object [0];
		pipelineStatus = new Status ();
		flowControl = null;
		notify = null;
		scienceGoal = null;
		weatherConstraint = null;
		centerFrequency = 0.0;
		frequencyBand = null;
		requiredInitialSetup = null;
		maximumTimeInSeconds = 0;
	}

	public void setMemberLink(Program parent) {
		ProgramMember x = null;
		Program u = null;
		SB s = null;
		for (int i = 0; i < member.size(); ++i) {
			x = (ProgramMember)member.get(i);
			if (x instanceof Program) {
				u = (Program)x;
				u.setTimeOfCreation(parent.getTimeOfCreation());
				u.setTimeOfUpdate(parent.getTimeOfUpdate());
				u.setProject(parent.getProject());
				u.setParent((Program)parent);
				u.setMemberLink(u);
			} else {
				s = (SB)x;
				s.setTimeOfCreation(parent.getTimeOfCreation());
				s.setTimeOfUpdate(parent.getTimeOfUpdate());
				s.setProject(parent.getProject());
				s.setParent((Program)parent);
			}
				
		}
	}
	
	/**
	 * Return the internal information about this Program as a string.
	 */
	public String toString() {
		StringBuffer s = new StringBuffer ();
		s.append("Program (" + getId() + "," + getTimeOfCreation() + "," + getTimeOfUpdate() + ") [" +
				getProject().getId() + "," + getParent().getId() + "] ");
		Object[] m = getMember();
		for (int i = 0; i < m.length; ++i)
			s.append("\n\t" + m[i].toString());
		return s.toString();
	}

	/**
	 * The printTree method prints a summary of the state of a 
	 * Program to the designated PrintStream.
	 * 
	 * @param out the PrintStream to which the summary is written. 
	 */
	public void printTree(PrintStream out, String indent) {
		// Do the printing for this unit set.
		out.println(indent + "Program " + programId);
		out.println(indent + "\tparent " + (parent == null ? "null" : parent.getId()));
		out.println(indent + "\ttotalRequiredTimeInSeconds " + totalRequiredTimeInSeconds);
		out.println(indent + "\ttotalUsedTimeInSeconds " + totalUsedTimeInSeconds);
		out.println(indent + "\ttotalPrograms " + totalPrograms);
		out.println(indent + "\tnumberProgramsCompleted " + numberProgramsCompleted);
		out.println(indent + "\tnumberProgramsFailed " + numberProgramsFailed);
		out.println(indent + "\ttotalSBs " + totalSBs);
		out.println(indent + "\tnumberSBsCompleted " + numberSBsCompleted);
		out.println(indent + "\tnumberSBsFailed " + numberSBsFailed);
		out.println(indent + "\tscientificPriority " + (scientificPriority == null ? "null" : scientificPriority.toString()));
		out.println(indent + "\tuserPriority " + (userPriority == null ? "null" : userPriority.toString()));
		out.println(indent + "\tdataReductionProcedureName " + dataReductionProcedureName);
		out.println(indent + "\tpipelineStatus " + pipelineStatus);
		out.print(indent + "\tflowControl ");
		if (flowControl == null)
			out.println("null");
		else {
			for (int i = 0; i < flowControl.length; ++i)
				out.println(indent + "\t\t" + flowControl[i].toString());
		}
		out.println(indent + "\tnotify " + (notify == null ? "null" : notify.toString()));
		out.println(indent + "\tscienceGoal " + (scienceGoal == null ? "null" : scienceGoal.toString()));
		out.println(indent + "\tweatherConstraint " + (weatherConstraint == null ? "null" : weatherConstraint.toString()));
		out.println(indent + "\trequiredInitialSetup " + (requiredInitialSetup == null ? "null" : requiredInitialSetup.toString()));
		out.println(indent + "\tmaximumTimeInSeconds " + maximumTimeInSeconds);
		out.println(indent + "\tproject " + (project == null ? "null" : project.getId()));
		out.println(indent + "\ttimeOfCreation " + (timeOfCreation == null ? "null" : timeOfCreation.toString()));
		out.println(indent + "\ttimeOfUpdate " + (timeOfUpdate == null ? "null" : timeOfUpdate.toString()));
		out.println(indent + "\tstatus " + status.getState());
		// Do the members of this set.
		ProgramMember x = null;
		Program u = null;
		SB s = null;
		for (int i = 0; i < member.size(); ++i) {
			x = (ProgramMember)member.get(i);
			if (x instanceof Program) {
				u = (Program)x;
				u.printTree(out,indent + "\t");
			} else {
				s = (SB)x;
				s.printTree(out,indent + "\t");
			}
			
		}
	}
	
	/**
	 * The printTreeLite method prints a simplified summary of the state of a 
	 * ProjectComponent to the designated PrintStream.
	 * 
	 * @param out the PrintStream to which the summary is written. 
	 */
	public void printTreeLite(PrintStream out, String indent) {
		// Do the printing for this unit set.
		out.println(indent + "Program " + programId +  
				" parent " + (parent == null ? "null" : parent.getId()) + 
				" project " + (project == null ? "null" : project.getId()) + 
				" status " + status.getStatus());
		// Do the members of this set.
		ProgramMember x = null;
		Program u = null;
		SB s = null;
		for (int i = 0; i < member.size(); ++i) {
			x = (ProgramMember)member.get(i);
			if (x instanceof Program) {
				u = (Program)x;
				u.printTreeLite(out,indent + "\t");
			} else {
				s = (SB)x;
				s.printTreeLite(out,indent + "\t");
			}
			
		}
	}
	
	// Methods for manipulating state information.
	
	/**
	 * When a project changes its state from "not-defined" to "defined", it
	 * is designated to be ready to execute and a time is entered to mark this
	 * event: this is the purppose of the "setReady(time)" method.  When 
	 * a project is designated to be "ready", this implies that all its Programs 
	 * and their members are ready.  Therefore, this setReady method is recursive,
	 * setting the ready time in all of its members, down to and including all
	 * SBs that are part of this project.
	 * 
	 * @param time This time at which this Program is ready.
	 */
	public void setReady(DateTime time) {
		// Set the ready time.
		status.setReady(time);
		ProgramMember[] x = getMember();
		for (int i = 0; i < x.length; ++i) {
			if (x[i] instanceof Program)
				((Program)x[i]).setReady(time);
			else
				((SB)x[i]).setReady(time);
		}
	}
	public void setTotals() {
		ProgramMember[] x = getMember();
		for (int i = 0; i < x.length; ++i) {
			if (x[i] instanceof Program) {
				((Program)x[i]).setTotals();
				++totalPrograms;
				totalPrograms +=((Program)x[i]).getTotalPrograms(); 
				totalSBs +=((Program)x[i]).getTotalSBs(); 
				totalRequiredTimeInSeconds += ((Program)x[i]).getTotalRequiredTimeInSeconds();
			} else {
				++totalSBs;
				totalRequiredTimeInSeconds += ((SB)x[i]).getTotalRequiredTimeInSeconds();
			}
		}
	}
	
	/**
	 * When a project breakpoint is cleared, all of its Programs and their 
	 * members enter the ready state.  Therefore, the setReady method
	 * is recursive, placing all of its members, down to and including all
	 * SBs that are part of this project, in a ready state. 
	 */
	public void setReady() {
		status.setReady();
		ProgramMember[] x = getMember();
		for (int i = 0; i < x.length; ++i) {
			if (x[i] instanceof Program)
				((Program)x[i]).setReady();
			else
				((SB)x[i]).getStatus().setReady();
		}
		
	}
	
	/**
	 * When a project enters a breakpoint, all of its Programs and their 
	 * members enter a wait state.  Therefore, the setWaiting method
	 * is recursive, placing all of its members, down to and including all
	 * SBs that are part of this project, in a wait state. 
	 */
	public void setWaiting() {
		status.setWaiting();
		ProgramMember[] x = getMember();
		for (int i = 0; i < x.length; ++i) {
			if (x[i] instanceof Program)
				((Program)x[i]).getStatus().setWaiting();
			else
				((SB)x[i]).getStatus().setWaiting();
		}
	}
	
	/**
	 * The unitStarted method marks the start of the first time
	 * the specified member was executed.  This Program considers the event and
	 * if this is the first time this Program has been executed, its
	 * own start time is set and its parent is informed. The information
	 * flow is upwards -- if the parent has not started, then its start
	 * time is set and its parent is informed -- all the way up to the
	 * project level.
	 * 
	 * @param unit The SProgramMember that was executed for the first time.
	 * @param time The time at which this SB started.
	 */
	public void unitStarted(ProgramMember unit, DateTime time) {
		if (!status.isStarted()) {
			status.setStarted(time);
			// Inform either the parent or the project.
			if (parent != null)
				parent.unitStarted(this,time);
			else
				getProject().getStatus().setStarted(time);
		} else {
			setAllRunning();
		}
	}

	public void setAllRunning() {
		if (parent == null)
			getProject().getStatus().setRunning();
		else
			parent.setAllRunning();
		status.setRunning();
	}
	private void setAllReady() {
		if (parent == null)
			getProject().getStatus().setReady();
		else
			parent.setAllReady();
		status.setReady();
	}
	private void updateAllTimeUsed(int t) {
		totalUsedTimeInSeconds += t;
		if (parent != null)
			parent.updateAllTimeUsed(t);
	}
	private void updateAllNumberSBsCompleted() {
		++numberSBsCompleted;
		if (parent != null)
			parent.updateAllNumberSBsCompleted();
	}
	private void updateAllNumberSBsFailed() {
		++numberSBsFailed;
		if (parent != null)
			parent.updateAllNumberSBsFailed();
	}
	private void updateAllNumberProgramsCompleted() {
		++numberProgramsCompleted;
		if (parent != null)
			parent.updateAllNumberProgramsCompleted();
	}
	private void updateAllNumberProgramsFailed() {
		++numberProgramsFailed;
		if (parent != null)
			parent.updateAllNumberProgramsFailed();
	}
	private void checkParent(SB x, DateTime now) {
		// Check for completeness of Programs.
		Program s = x.getParent();
		ProgramMember[] m = null;
		while (s != null) {
			// First, get all members;
			m = s.getMember();
			boolean incomplete = false;
			boolean aborted = false;
			// Go through the list and check their status.
			for (int j = 0; j < m.length; ++j) {
				if (!m[j].getStatus().isEnded()) {
					incomplete = true;
					break;
				}
				if (m[j].getStatus().isAborted())
					aborted = true;
			}
			// If any are incomplete, then this unit set is incomplete.
			if (incomplete)
				break;
			// Otherwise, this Program is complete.  Let the parent know.
			if (aborted) {
				s.getStatus().setEnded(now,Status.ABORTED);
				if (s.parent != null)
					s.parent.updateAllNumberProgramsFailed();
			} else {
				s.getStatus().setEnded(now,Status.COMPLETE);
				if (s.parent != null)
					s.parent.updateAllNumberProgramsCompleted();
			}
			if (s.getParent() == null) {
				Project prj = s.getProject();
				Program pgm = prj.getProgram();
				if (pgm.getStatus().isEnded())
					prj.getStatus().setEnded(pgm.getStatus().getEndTime(),pgm.getStatus().getStatusAsInt());
			}
			s = s.getParent();
		}
	}
	public void execEnd(SB unit, DateTime time, int timeUsedInSeconds, int state) {
		// 1. If the parent is not in the running state, something is wrong.
		if (!status.isRunning())
			throw new IllegalStateException(" The execEnd method was called but Program is not in the running state!" +
					"Program: " + getId() + " SB: " + getId() + " Time: " + time);
		// 2. Change the state to ready.
		setAllReady();
		// 3. If time used in seconds is not 0, update the timeUsed.
		updateAllTimeUsed(timeUsedInSeconds);
		// 4. If unit has ended, update number completed or failed.
		if (unit.getStatus().isEnded()) {
			if (unit.getStatus().isComplete())
				updateAllNumberSBsCompleted();
			else
				updateAllNumberSBsFailed();
			checkParent(unit,time);
		}
	}
	
	
	// Methods for manipulating members of this Program.
	
	/**
	 * Get the number of members in this set.
	 * @return The number of members in this set.
	 */	
	public int getNumberMembers() {
		return member.size();
	}
	
	/**
	 * Get a member of this set by specifying its index.
	 * @param index The index of the member to be returned.
	 * @return The member with the specified index
	 * or null, if there was no such object.
	 */
	public ProgramMember getMember(int index) {
		if (index < 0 || index >= member.size())
			return null; 
		return (ProgramMember)(member.get(index));
	}

	/**
	 * The member with the specified id.
	 * @param id The id of the member to be returned.
	 * @return The member with the specified id
	 * or null, if there was no such object.
	 */
	public ProgramMember getMember(String id) {
		ProgramMember x = null;
		for (int i = 0; i < member.size(); ++i) {
			x = (ProgramMember)member.get(i);
			if (x.getId().equals(id))
				return x;
		}
		return null;
	}

    /**
      * Replace the given member with the newly updated one.
      * @param
      */ 
    public void updateMember(ProgramMember mem) {
        ProgramMember x=null;
        for(int i=0; i < member.size(); i++){
            x=(ProgramMember)member.get(i);
            if(x.getId().equals(mem.getId())){
                member.set(i, mem);
            }
        }
    }

	/**
	 * Get all the members of this set.
	 * @return The members of this set as an array of Objects.
	 */
	public ProgramMember[] getMember() {
		ProgramMember[] x = new ProgramMember [member.size()];
		x = (ProgramMember[])member.toArray(x);
		return x;
	}
	
	/**
	 * Return all SBs that belong to this Program.
	 */
	public SB[] getAllSBs() {
		ArrayList list = new ArrayList ();
		getAllSBs(list);
		SB[] u = new SB [list.size()];
		return (SB[])list.toArray(u);
	}
	private void getAllSBs(ArrayList list) {
		ProgramMember[] member = getMember();
		for (int i = 0; i < member.length; ++i) {
			if (member[i] instanceof SB)
				list.add(member[i]);
			else
				((Program)member[i]).getAllSBs(list);
		}
	}
	
	/**
	 * Return all Programs that belong to this Program.
	 */
	public Program[] getAllPrograms() {
		ArrayList list = new ArrayList ();
		getAllPrograms(list);
		Program[] u = new Program [list.size()];
		return (Program[])list.toArray(u);
	}
	private void getAllPrograms(ArrayList list) {
		ProgramMember[] member = getMember();
		for (int i = 0; i < member.length; ++i) {
			if (member[i] instanceof Program)
				list.add((Program)member[i]);
		}
	}
	/**
	 * Add a member to this set.
	 * @param x The member to be added.
	 */
	public void addMember(Program x) {
		member.add(x);
	}

	/**
	 * Add a member to this set.
	 * @param x The member to be added.
	 */
	public void addMember(SB x) {
		member.add(x);
	}
	
	// Methods for adding and accessing observed sessions.
	
	public int getNumberSession() {
		return session.size();
	}
	
	public void addObservedSession(ObservedSession s) {
		session.add(s);
	}

	public ObservedSession[] getAllSession() {
		ObservedSession[] x = new ObservedSession [session.size()];
		x = (ObservedSession[])session.toArray(x);
		return x;
	}
	
	public ObservedSession getSession(int index) {
		if (index < 0 || index >= session.size())
			return null;
		return (ObservedSession)(session.get(index));
	}
	
	/**
	 * Increment the time used by the specified number of seconds.
	 * @param totalUsedTimeInSeconds The timeInSeconds to be added to the total time used.
	 */
	public void incrTotalUsedTimeInSeconds(int timeInSeconds) {
		this.totalUsedTimeInSeconds += timeInSeconds;
		if (parent != null)
			parent.incrTotalUsedTimeInSeconds(timeInSeconds);
	}

	////////////////////////////////////////////////////////////////////////
	// Routine getters and setters
	////////////////////////////////////////////////////////////////////////
	
	/**
	 * @return Returns the centerFrequency.
	 */
	public double getCenterFrequency() {
		return centerFrequency;
	}

	/**
	 * @param centerFrequency The center frequency to set.
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
	 * @return Returns the dataReductionProcedureName.
	 */
	public String getDataReductionProcedureName() {
		return dataReductionProcedureName;
	}

	/**
	 * @param dataReductionProcedureName The dataReductionProcedureName to set.
	 */
	public void setDataReductionProcedureName(String dataReductionProcedureName) {
		this.dataReductionProcedureName = dataReductionProcedureName;
	}

	/**
	 * @return Returns the flowControl.
	 */
	public FlowControl[] getFlowControl() {
		return flowControl;
	}

	/**
	 * @param flowControl The flowControl to set.
	 */
	public void setFlowControl(FlowControl[] flowControl) {
		this.flowControl = flowControl;
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
	 * @return Returns the notify.
	 */
	public NotifyPI getNotify() {
		return notify;
	}

	/**
	 * @param notify The notify to set.
	 */
	public void setNotify(NotifyPI notify) {
		this.notify = notify;
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
	 * @return Returns the pipelineStatus.
	 */
	public Status getPipelineStatus() {
		return pipelineStatus;
	}

	/**
	 * @param pipelineStatus The pipelineStatus to set.
	 */
	public void setPipelineStatus(Status pipelineStatus) {
		this.pipelineStatus = pipelineStatus;
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
	 * @return Returns the numberSBsCompleted.
	 */
	public int getNumberSBsCompleted() {
		return numberSBsCompleted;
	}

	/**
	 * @return Returns the numberProgramsCompleted.
	 */
	public int getNumberProgramsCompleted() {
		return numberProgramsCompleted;
	}

	/**
	 * @return Returns the numberProgramsFailed.
	 */
	public int getNumberProgramsFailed() {
		return numberProgramsFailed;
	}

	/**
	 * @return Returns the numberSBsFailed.
	 */
	public int getNumberSBsFailed() {
		return numberSBsFailed;
	}

	/**
	 * @return Returns the totalRequiredTimeInSeconds.
	 */
	public int getTotalRequiredTimeInSeconds() {
		return totalRequiredTimeInSeconds;
	}

	/**
	 * @return Returns the totalSBs.
	 */
	public int getTotalSBs() {
		return totalSBs;
	}

	/**
	 * @return Returns the totalPrograms.
	 */
	public int getTotalPrograms() {
		return totalPrograms;
	}

	/**
	 * @return Returns the totalUsedTimeInSeconds.
	 */
	public int getTotalUsedTimeInSeconds() {
		return totalUsedTimeInSeconds;
	}

	/**
	 * @return Returns the unitSetId.
	 */
	public String getProgramId() {
		return programId;
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
		return programId;
	}

	/**
	 * Return the status of this project component.
	 * @return Returns the status of this project component.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @return Returns the dataReductionParameters.
	 */
	public Object[] getDataReductionParameters() {
		return dataReductionParameters;
	}

	/**
	 * @param dataReductionParameters The dataReductionParameters to set.
	 */
	public void setDataReductonParameters(Object[] dataReductionParameters) {
		this.dataReductionParameters = dataReductionParameters;
	}

	/**
	 * @return Returns the sciPipelineRequest.
	 */
	public SciPipelineRequest getSciPipelineRequest() {
		return sciPipelineRequest;
	}

	/**
	 * @param sciPipelineRequest The sciPipelineRequest to set.
	 */
	public void setSciPipelineRequest(SciPipelineRequest sciPipelineRequest) {
		this.sciPipelineRequest = sciPipelineRequest;
	}

	/**
	 * @return Returns the obsUnitSetStatusId.
	 */
	public String getObsUnitSetStatusId() {
		return obsUnitSetStatusId;
	}

	/**
	 * @param obsUnitSetStatusId The obsUnitSetStatusId to set.
	 */
	public void setObsUnitSetStatusId(String obsUnitSetStatusId) {
		this.obsUnitSetStatusId = obsUnitSetStatusId;
	}

}
