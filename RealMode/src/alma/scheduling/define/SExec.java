/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
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
 * File SExec.java
 */
 
package alma.scheduling.define;


/**
 * Description 
 * 
 * @version 1.00  Sep 22, 2003
 * @author Allen Farris
 */
public class SExec implements MemberOf {
	// The archive id of this entity.
	private String id;
	// The time this archive entity was created. 
	private STime timeOfCreation;
	// The time this archive entity was last updated.
	private STime timeOfUpdate;
	// The index of this member.
	private int index;
	// The project to which this SExec belongs.
	private SProject project;
	// The id of the project to which this SExec belongs.
	private String projectId;
	// The immediate parent of this SExec.
	private SUnit parent;
	// The id of the immediate parent of this SUnit.
	private String parentId;

	private STime startTime;
	private STime endTime;
	private short subarrayId;

	/**
	 * Create an SExec object.
	 */
	public SExec() {
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
	public SUnit getParent() {
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
	public void setParent(SUnit set) {
		parent = set;
	}

	///////////////////////////////
	// Getter and Setter Methods //
	///////////////////////////////

	/**
	 * @return
	 */
	public STime getEndTime() {
		return endTime;
	}

	/**
	 * @return
	 */
	public STime getStartTime() {
		return startTime;
	}

	/**
	 * @param time
	 */
	public void setEndTime(STime time) {
		endTime = time;
	}

	/**
	 * @param time
	 */
	public void setStartTime(STime time) {
		startTime = time;
	}

	/**
	 * @return
	 */
	public short getSubarrayId() {
		return subarrayId;
	}

	/**
	 * @param s
	 */
	public void setSubarrayId(short s) {
		subarrayId = s;
	}

}
