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
 
package alma.scheduling.define;

import java.util.ArrayList;
import alma.entity.xmlbinding.obsproject.*;
import alma.entity.xmlbinding.schedblock.*;
/**
 * A SUnitSet is a hierarchical tree whose leaves are SUnit objects. 
 * 
 * @version 1.00  Jun 4, 2003
 * @author Allen Farris
 */
public class SUnitSet implements HasMembers, MemberOf {
	// The archive id of this entity.
	private String id;
	// The time this archive entity was created. 
	private STime timeOfCreation;
	// The time this archive entity was last updated.
	private STime timeOfUpdate;
	// The index of this member.
	private int index;
	// The project to which this SUnitSet belongs.
	private SProject project;
	// The id of the project to which this SUnitSet belongs.
	private String projectId;
	// The immediate parent of this SUnitSet.
	private SUnitSet parent;
	// The id of the immediate parent of this SUnitSet.
	private String parentId;
	// The members of this set are either SUnitSet or SUnit objects.
	private ArrayList member;
	
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
    private ObsUnitSetT[] obsUnitSet;

	/**
	 * Create an SUnitSet object.
	 */
	public SUnitSet() {
		member = new ArrayList ();

		project = null;
		projectId = "";
		parent = null;
		parentId = "";
		scientificPriority = new Priority (Priority.BACKGROUND);
		userPriority = new Priority (Priority.BACKGROUND);
		dataReductionProcedureName = "";
		pipelineStatus = new Status (Status.NOTDEFINED);
		flowControl = null;
		notify = null;
		scienceGoal = null;
		weatherConstraint = null;
		requiredInitialSetup = null;
		maximumTimeInSeconds = 0;
		unitSetStatus = new Status (Status.NOTDEFINED);
	}

    public SUnitSet(ObsUnitSetT[] ous) {
        this();
        this.obsUnitSet = ous;
        /*
        ObsUnitSetTChoice tmp = ous.getObsUnitSetTChoice();
        SchedBlockRefT[] tmp1 = tmp.getSchedBlockRef();
        ObsUnitSetT[] tmp2 = tmp.getObsUnitSet();
        */
        /*
        for(int i=0; i< obsUnitSet.length; i++) {
            //this will have to change!!!!
            if(obsUnitSet[i] instanceof ObsUnitSetT) {
                for(inf j=0; j<obsUnitSet[i].length(
            }
            for(int j=0; j<obsUnitSet[i].
            members.add(obsUnitSet[i]);
        }
        */
    }

	public void setMemberLink(SUnitSet parent) {
		MemberOf x = null;
		SUnitSet u = null;
		SUnit s = null;
		for (int i = 0; i < member.size(); ++i) {
			x = (MemberOf)member.get(i);
			x.setId(parent.getId() + "." + Integer.toString(x.getMemberIndex()));
			x.setTimeCreated(parent.getTimeCreated());
			x.setTimeUpdated(parent.getTimeUpdated());
			x.setProjectId(parent.getProjectId());
			x.setParentId(parent.getId());
			if (x instanceof SUnitSet) {
				u = (SUnitSet)x;
				u.setProject(parent.getProject());
				u.setParent((SUnitSet)parent);
				u.setMemberLink(u);
			} else {
				s = (SUnit)x;
				s.setProject(parent.getProject());
				s.setParent((SUnitSet)parent);
			}
				
		}
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
		return member.size();
	}
	
	/**
	 * Get a member of this set by specifying its name.
	 * @param index The index of the member to be returned.
	 * @return The member with the specified index
	 * or null, if there was no such object.
	 */
	public MemberOf getMember(int index) {
		if (index < 0 || index >= member.size())
			return null; 
		return (MemberOf)(member.get(index));
	}

	/**
	 * The member with the specified id.
	 * @param id The id of the member to be returned.
	 * @return The member with the specified id
	 * or null, if there was no such object.
	 */
	public MemberOf getMember(String id) {
		MemberOf x = null;
		for (int i = 0; i < member.size(); ++i) {
			x = (MemberOf)member.get(i);
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
		MemberOf[] x = new MemberOf [member.size()];
		x = (MemberOf[])member.toArray(x);
		return x;
	}
	
	/**
	 * Add a member to this set.
	 * @param x The member to be added.
	 */
	public void addMember(SUnitSet x) {
		x.setMemberIndex(member.size());
		member.add(x);
	}

	/**
	 * Add a member to this set.
	 * @param x The member to be added.
	 */
	public void addMember(SUnit x) {
		x.setMemberIndex(member.size());
		member.add(x);
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

	////////////////////
	// Setter methods //
	////////////////////

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

	//////////////////////////
	// The toString method. //
	//////////////////////////

	/**
	 * Return the internal information about this SUnitSet as a string.
	 */
	public String toString() {
		StringBuffer s = new StringBuffer ();
		s.append("SUnitSet (" + getId() + "," + getTimeCreated() + "," + getTimeUpdated() + ") [" +
				   getMemberIndex() + ", " + getProjectId() + "," + getParentId() + "] ");
		Object[] m = getMember();
		for (int i = 0; i < m.length; ++i)
			s.append("\n\t" + m[i].toString());
		return s.toString();
	}

}
