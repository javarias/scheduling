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
 * File ExecBlock.java
 */
 
package alma.scheduling.Define;

import java.io.PrintStream;


/**
 * The ExecBlock class identifies the execution record resulting from 
 * the execution of a scheduling block.  It contains the entity-id,
 * starting and ending time, and the subarray-id of the subarray
 * that executed it. This is a wrapper class for the actual ExecBlock
 * that is defined by the Control system.
 * 
 * @version $Id: ExecBlock.java,v 1.4 2004/11/23 20:41:21 sslucero Exp $
 * @author Allen Farris
 */
public class ExecBlock implements ProjectMember {
	// The archive id of this entity -- the entityId of the Data capture generated ExecBlock.
	private String execId;
    // The statusId associated with this ExecBlock.
    private String execStatusId;
	// The project to which this ExecBlock belongs.
	private Project project;
	// The time this ExecBlock was created. 
	private DateTime timeOfCreation;
	// The time this ExecBlock was last updated.
	private DateTime timeOfUpdate;
	// The status of this ExecBlock.
	protected Status status;
	// The immediate parent of this ExecBlock.
	private SB parent;
	// The subarray used to create this execution record.
	private int subarrayId;
	// The BestUnit object used to execute this SB.
	private BestSB best;

	/**
	 * Create a UnitExec object.
	 */
	public ExecBlock(String id, int subarray) {
		this.execId = id;
		project = null;
		timeOfCreation = null;
		timeOfUpdate = null;
		status = new Status ();
		parent = null;
		this.subarrayId = subarray;
		best = null;
	}

	public String toString() {
		String s = "ExecBlock (" + execId + ", project ";
		s += (project == null ? "null" : project.getId());
		s += ", " + (timeOfCreation == null ? "null" : timeOfCreation.toString());
		s += ", " + (timeOfUpdate == null ? "null" : timeOfUpdate.toString());
		s += ", " + status.getState() + ")";
		return s;
	}

	/**
	 * The printTree method prints a summary of the state of a 
	 * UnitExec to the designated PrintStream.
	 * 
	 * @param out the PrintStream to which the summary is written. 
	 */
	public void printTree(PrintStream out, String indent) {
		out.println(indent + "UnitExec " + execId); 
		out.println(indent + "\tparent " + (parent == null ? "null" : parent.getId()));
		out.println(indent + "\tsubarray " + subarrayId);
		out.println(indent + "\tproject " + (project == null ? "null" : project.getId()));
		out.println(indent + "\ttimeOfCreation " + (timeOfCreation == null ? "null" : timeOfCreation.toString()));
		out.println(indent + "\ttimeOfUpdate " + (timeOfUpdate == null ? "null" : timeOfUpdate.toString()));
		out.println(indent + "\tstatus " + status.getState());
	}
	
	/**
	 * The printTreeLite method prints a simplified summary of the state of a 
	 * ProjectComponent to the designated PrintStream.
	 * 
	 * @param out the PrintStream to which the summary is written. 
	 */
	public void printTreeLite(PrintStream out, String indent) {
		out.println(indent + "UnitExec " + execId + 
				" parent " + (parent == null ? "null" : parent.getId()) + 
				" project " + (project == null ? "null" : project.getId()) + 
				" status " + status.getStatus());		
	}
	
	public void setStartTime(DateTime time) {
		status.setReady(time);
		status.setStarted(time);
	}
	
	public void setEndTime(DateTime time, int state) {
		status.setEnded(time,state);
	}
	
	/**
	 * @return Returns the parent.
	 */
	public SB getParent() {
		return parent;
	}

	/**
	 * @param parent The parent to set.
	 */
	public void setParent(SB parent) {
		this.parent = parent;
	}

	/**
	 * @return Returns the execId.
	 */
	public String getExecId() {
		return execId;
	}

	/**
	 * @return Returns the subarrayId.
	 */
	public int getSubarrayId() {
		return subarrayId;
	}

	/**
	 * @return Returns the best.
	 */
	public BestSB getBest() {
		return best;
	}

	/**
	 * @param best The best to set.
	 */
	public void setBest(BestSB best) {
		this.best = best;
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
		return execId;
	}

	/**
	 * Return the status of this project component.
	 * @return Returns the status of this project component.
	 */
	public Status getStatus() {
		return status;
	}
	
    /**
     * @return Returns the execStatusId.
     */
    public String getExecStatusId() {
        return execStatusId;
    }

    /**
     * @param execStatusId The execStatusId to set.
     */
    public void setExecStatusId(String execStatusId) {
        this.execStatusId = execStatusId;
    }
}
