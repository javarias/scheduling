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
 
package alma.scheduling.define;

import java.util.ArrayList;
import alma.entity.xmlbinding.schedblock.*;
/**
 * An SUnit is the lowest-level, atomic scheduling unit. 
 * 
 * @version 1.00  Jun 4, 2003
 * @author Allen Farris
 */
public class SUnit implements HasMembers, MemberOf {
	// The archive id of this entity.
	private String id;
	// The time this archive entity was created. 
	private STime timeOfCreation;
	// The time this archive entity was last updated.
	private STime timeOfUpdate;
	// The index of this member.
	private int index;
	// The project to which this SUnit belongs.
	private SProject project;
	// The id of the project to which this SUnit belongs.
	private String projectId;
	// The immediate parent of this SUnit.
	private SUnitSet parent;
	// The id of the immediate parent of this SUnit.
	private String parentId;
	// The members of this set are execution records.
	private ArrayList executionRec;
	
	// The scheduling block id that identifies this SUnit.
	private String schedBlockId;
		
	private Priority scientificPriority;
	private Priority userPriority;
	private Expression scienceGoal;
	private WeatherCondition weatherConstraint;
	private SystemSetup requiredInitialSetup;
	private int maximumTimeInSeconds;
	private int maximumNumberOfRepeats;
	private String imagingScript;
	private String observingScript;
	private boolean isStandardScript;

	//private Target target;
	private double frequency;
	
	private Status unitStatus;
    private SchedBlock schedBlock;
	
	/**
	 * Create an SUnit object.
	 */
	public SUnit() {
		executionRec = new ArrayList ();
		unitStatus = new Status (Status.NOTDEFINED);

		project = null;
		projectId = "";
		parent = null;
		parentId = "";
		schedBlockId = "";
		scientificPriority = new Priority (Priority.BACKGROUND);
		userPriority = new Priority (Priority.BACKGROUND);
		scienceGoal = null;
		weatherConstraint = null;
		requiredInitialSetup = null;
		maximumTimeInSeconds = 0;		
		maximumNumberOfRepeats = 0;
		imagingScript = "";
		observingScript = "";
		isStandardScript = true;
		//Equatorial tmp = new Equatorial (0,0,0.0,0,0,0.0);
		//target = new Target (tmp,0.0);
		frequency = 0.0;
		unitStatus = new Status (Status.WAITING);
	}
    public SUnit(SchedBlock sb) {
        this();
        this.schedBlock = sb;
        this.schedBlockId = sb.getSchedBlockEntity().getEntityId();
        //this.projectId = sb.getObsProjectRef().getEntityId();
    }

	public void setMemberLink(SUnitSet parent) {
	}

    public void updateSB(SchedBlock sb) {
        this.schedBlock = sb;
    }

	////////////////////////////////////////////////////
	// Implementation of the ArchiveEntity interface. //
	////////////////////////////////////////////////////

	/**
	 * Get the archive identifier.
	 * @return The archive identifier as a String.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Get the time this archive entry was created.
	 * @return The time this archive entry was created as an STime.
	 */
	public STime getTimeCreated() {
		return timeOfCreation;
	}
	
	/**
	 * Get the time this archive entry was last updated.
	 * @return The time this archive entry was last updated as an STime.
	 */
	public STime getTimeUpdated() {
		return timeOfUpdate;
	}
	
	/**
	 * Set the archive identifier.
	 * @param id The id of this archive entity.
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Set the time this archive entry was created.
	 * @param t The time this archive entry was created.
	 */
	public void setTimeCreated(STime t) {
		this.timeOfCreation = t;
	}
	
	/**
	 * Set the time this archive entry was last updated.
	 * @param t The time this archive entry was last updated.
	 */
	public void setTimeUpdated(STime t) {
		this.timeOfUpdate = t;
	}
	
	/////////////////////////////////////////////////
	// Implementation of the HasMembers interface. //
	/////////////////////////////////////////////////

	/**
	 * Get the number of members in this set.
	 * @return The number of members in this set.
	 */	
	public int getNumberMembers() {
		return executionRec.size();
	}
	
	/**
	 * Get a member of this set by specifying its name.
	 * @param index The index of the member to be returned.
	 * @return The member with the specified index
	 * or null, if there was no such object.
	 */
	public MemberOf getMember(int index) {
		if (index < 0 || index >= executionRec.size())
			return null; 
		return (MemberOf)(executionRec.get(index));
	}

	/**
	 * The member with the specified id.
	 * @param id The id of the member to be returned.
	 * @return The member with the specified id
	 * or null, if there was no such object.
	 */
	public MemberOf getMember(String id) {
		MemberOf x = null;
		for (int i = 0; i < executionRec.size(); ++i) {
			x = (MemberOf)executionRec.get(i);
			if (x.getId().equals(id))
				return x;
		}
		return null;
	}

	/**
	 * Get all the members of this set.
	 * @return The members of this set as an array of Objects.
	 */
	public MemberOf[] getMember() {
		MemberOf[] x = new MemberOf [executionRec.size()];
		x = (MemberOf[])executionRec.toArray(x);
		return x;
	}
	
	/**
	 * Add a member to this set.
	 * @param x The member to be added.
	 */
	public void addMember(SExec x) {
		x.setMemberIndex(executionRec.size());
		x.setProjectId(getProjectId());
		x.setProject(getProject());
		x.setParentId(getId());
		x.setParent(this);
		executionRec.add(x);
	}

	/**
	 * @return
	 */
	public SExec[] getSExec() {
		SExec[] s = new SExec [executionRec.size()];
		s = (SExec[])executionRec.toArray(s);
		return s;
	}

	/**
	 * Get a member of this set by specifying its index.
	 * @param index The index of the member to be returned.
	 * @return The member with the specified index
	 * or null, if there was no such object.
	 */
	public SExec getSExec(int index) {
		if (index < 0 || index >= executionRec.size())
			return null; 
		return (SExec)(executionRec.get(index));
	}

	/**
	 * Get the member of this set with the specified id.
	 * @param id The id of the member to be returned.
	 * @return The member with the specified id
	 * or null, if there was no such object.
	 */
	public SExec getSExec(String id) {
		SExec x = null;
		for (int i = 0; i < executionRec.size(); ++i) {
			x = (SExec)executionRec.get(i);
			if (x.getId().equals(id))
				return x;
		}
		return null;
	}
	
	/////////////////////////////////////////////// 
	// Implementation of the MemberOf interface. // 
	///////////////////////////////////////////////
	
	/**
	 * Get the index of this member;
	 * @return The index of the member as an int.
	 */
	public int getMemberIndex() {
		return index;
	}

	/**
	 * Set the index of this member;
	 * @parm index The index of this member.
	 */
	public void setMemberIndex(int index) {
		this.index = index;
	}

	/**
	 * @return
	 */
	public SUnitSet getParent() {
		return parent;
	}

	/**
	 * @return
	 */
	public String getParentId() {
		return parentId;
	}

	/**
	 * @return
	 */
	public SProject getProject() {
		return project;
	}

	/**
	 * @return
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * @param string
	 */
	public void setParentId(String string) {
		parentId = string;
	}

	/**
	 * @param string
	 */
	public void setProjectId(String string) {
		projectId = string;
	}

	/**
	 * @param project
	 */
	public void setProject(SProject project) {
		this.project = project;
	}

	/**
	 * @param set
	 */
	public void setParent(SUnitSet set) {
		parent = set;
	}

	////////////////////
	// Getter methods //
	////////////////////

    public SchedBlock getSchedBlock() {
        return schedBlock;
    }

	/**
	 * @return
	 */
	public int getMaximumNumberOfRepeats() {
		return maximumNumberOfRepeats;
	}

	/**
	 * @return
	 */
	public int getNumberExecutions() {
		return executionRec.size();
	}

	/**
	 * @return
	 */
	public String getImagingScript() {
		return imagingScript;
	}

	/**
	 * @return
	 */
	public String getObservingScript() {
		return observingScript;
	}

	/**
	 * @return
	 */
    /*
	public Target getTarget() {
		return target;
	}
    */
	/**
	 * @return
	 */
	public String getSchedBlockId() {
		return schedBlockId;
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

	////////////////////
	// Setter methods //
	////////////////////

    public void setSchedBlock(SchedBlock sb) {
        this.schedBlock = sb;
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

	/**
	 * @param string
	 */
	public void setSchedBlockId(String string) {
		schedBlockId = string;
	}

	public void addExecutionRec(String id) {
		executionRec.add(id);
	}

	/**
	 * @param string
	 */
	public void setImagingScript(String string) {
		imagingScript = string;
	}

	/**
	 * @param string
	 */
	public void setObservingScript(String string) {
		observingScript = string;
	}

	/**
	 * @param target
	 */
    /*
	public void setTarget(Target target) {
		this.target = target;
	}
    */

	/**
	 * @param i
	 */
	public void setMaximumNumberOfRepeats(int i) {
		maximumNumberOfRepeats = i;
	}

	/**
	 * @return
	 */
	public double getFrequency() {
		return frequency;
	}

	/**
	 * @param d
	 */
	public void setFrequency(double d) {
		frequency = d;
	}
	
	/**
	 * Get the earliest time at which this SUnit can be scheduled on a specified date.
	 * @param d The date on interest.
	 * @return The earliest time at which this SUnit can be scheduled on the specified date.
	 */
	public STime getEarliest(Date d) {
		return null; 
	}
	/**
	 * Get the latest time at which this SUnit can be scheduled on a specified date.
	 * @param d The date on interest.
	 * @return The latest time at which this SUnit can be scheduled on the specified date.
	 */
	public STime getLatest(Date d) {
		return null;
	}
	/**
	 * Get the elevation of the target at the specified time.
	 * @param t The time of interest.
	 * @return The elevation of the target at the specified time.
	 */
	public double getElevation(STime t) {
		return 0.0;
	}

	//////////////////////////
	// The toString method. //
	//////////////////////////

	/**
	 * Return the internal information about this SUnit as a string.
	 */
	public String toString() {
		return "\tSUnit (" + getId() + "," + getTimeCreated() + "," + getTimeUpdated() + ") [" +
				   getMemberIndex() + "," + getProjectId() + "," + getParentId() + "] " +
				   getSchedBlockId() + " " + getScientificPriority() + " " /*+ getTarget() + " " */+
				   getFrequency() + " " + getUnitStatus();
	}

}

