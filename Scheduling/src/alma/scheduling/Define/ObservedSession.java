/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by AUI (in the framework of the ALMA collaboration),
 * All rights reserved
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 * 
 * File Session.java
 * 
 */

package alma.scheduling.Define;

import java.util.ArrayList;

/**
 * description
 * 
 * @version 1.5 September 16, 2004
 * @author Allen Farris
 */
public class ObservedSession {
	// The entity-ID that identifies this observed session.
    private String sessionId;
    // The program to which this observed session belongs. 
    private Program program;
    // The time at which this observed session started.
    private DateTime startTime;
    // The time at which this observed session ended.
    private DateTime endTime;
    // The list of ExecBlocks that are in this observed session.
    private ArrayList exec;
    
    /**
     * Create an ObservedSession object.
     */
    public ObservedSession() {
        exec = new ArrayList ();
    }

    /**
     * Get the number of ExecBlocks that belong to this ObservedSession.
     * @return The number of ExecBlocks that belong to this ObservedSession.
     */	
    public int getNumberExec() {
    	return exec.size();
    }
    
    /**
     * Add an ExecBlock to this ObservedSession.
     * @param x The ExecBlock to be added.
     */
    public void addExec(ExecBlock x) {
    	exec.add(x);
    }

    /**
     * Return the ExecBlocks that belong to this ObservedSession.
     * @return the ExecBlocks that belong to this ObservedSession.
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

    // Routine getters and setters.

    
	/**
	 * @return Returns the endTime.
	 */
	public DateTime getEndTime() {
		return endTime;
	}

	/**
	 * @param endTime The endTime to set.
	 */
	public void setEndTime(DateTime endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return Returns the program.
	 */
	public Program getProgram() {
		return program;
	}

	/**
	 * @param program The program to set.
	 */
	public void setProgram(Program program) {
		this.program = program;
	}

	/**
	 * @return Returns the sessionId.
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * @param sessionId The sessionId to set.
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * @return Returns the startTime.
	 */
	public DateTime getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime The startTime to set.
	 */
	public void setStartTime(DateTime startTime) {
		this.startTime = startTime;
	}

    public boolean execExists(String id) {
        return false;
    }
 }
