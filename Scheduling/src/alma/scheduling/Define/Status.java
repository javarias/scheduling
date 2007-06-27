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
 * File Status.java
 */
 
package alma.scheduling.Define;

/**
 * The Status class defines the state model for components
 * within a project.  Components may be any of the following:
 * Project, Unit, UnitSet, or UnitExec.  There are six states and
 * three significant times.  The six states are
 * <ul>
 * <li> NOTDEFINED the component is not defined
 * <li>	WAITING the component is waiting
 * <li>	READY the component is ready to be executed
 * <li>	RUNNING the component is currently running
 * <li>	ABORTED the component has aborted
 * <li>	COMPLETE the component has completed execution
 * </ul> 
 * The three times are
 * <ul>
 * <li> when the component was first declared to be ready to execute,
 * <li> when the component first began execution,
 * <li> when the component ended execution. 
 * </ul>
 * The three times may be considered to designate super-states.
 * In its intial state a component is not defined
 * and its ready, start, and end times are null.  The state progression
 * is from "ready" to "started" to "ended".  Checks insure these times
 * are in the proper order.  The ready time must be set before any
 * unit can be started.  If the unit has been started, it may be in either
 * the "ready", "waiting", or "running" state.  If the unit's end time
 * has been set the state is either "complete" or "aborted".  
 * 
 * @version $Id: Status.java,v 1.7 2007/06/27 22:24:10 sslucero Exp $
 * @author Allen Farris
 */
public class Status {
	static public final int NOTDEFINED 	= 0;
	static public final int WAITING		= 1;
	static public final int READY	  	= 2;
	static public final int RUNNING		= 3;
	static public final int ABORTED		= 4;
	static public final int COMPLETE	= 5;
	
	private int status;
	private DateTime readyTime;
	private DateTime startTime;
	private DateTime endTime;
	
/*
    public Status(int s) {
        this.status = s;
        
    }
    */


	/**
	 * Create a Status object with an initial state of NOT-DEFINED.
	 */
	public Status () {
		this.status = NOTDEFINED;
		readyTime = null;
		startTime = null;
		endTime = null;
	}
	
	/**
	 * Change the state of this Status object to RUNNING.
	 */
	public void setRunning() {
		if (startTime == null)
			throw new UnsupportedOperationException("Cannot set state to RUNNING!  The start time must be set.");
		if (endTime != null)
			throw new UnsupportedOperationException("Cannot set state to RUNNING!  The component has ended.");
		this.status = RUNNING;
	}

	/**
	 * Change the state of this Status object to WAITING.
	 */
	public void setWaiting() {
		if (startTime == null)
			throw new UnsupportedOperationException("Cannot set state to WAITING!  The start time must be set.");
		if (endTime != null)
			throw new UnsupportedOperationException("Cannot set state to WAITING!  The component has ended.");
		this.status = WAITING;
	}
	/**
	 * Change the state of this Status object to READY.
	 */
	public void setReady() {
		if (startTime == null) 
			throw new UnsupportedOperationException("Cannot set state to READY!  The start time must be set.");
		if (endTime != null)
			throw new UnsupportedOperationException("Cannot set state to READY!  The component has ended.");
		this.status = READY;
	}
	
	/**
	 * Set the ready time, to indicate this unit is ready for execution.
	 */
	public void setReady(DateTime time) {
		if (readyTime != null )
			throw new UnsupportedOperationException("Cannot set ready time!  Component has already been defined.");
		if (time == null || time.isNull())
			throw new UnsupportedOperationException("Time cannot be null!");
		status = READY;
		readyTime = new DateTime (time);
	}
	
	/**
	 * Set the start time, to indicate this unit has begun execution.
	 */
	public void setStarted(DateTime time) {
        ///
		if (startTime != null )
			throw new UnsupportedOperationException("Cannot set start time!  Component has already been started.");
        if(readyTime == null) {
            System.out.println("SCHED: in status, readyTime == null");
        }
        if(readyTime.gt(time)){
            System.out.println("SCHED: in status, readyTime > time");
        }
		//if (readyTime == null )  //|| readyTime.gt(time))
		//	throw new UnsupportedOperationException("Cannot set 'started' before setting 'ready'.");
            ////
		startTime = new DateTime (time);
		status = RUNNING;
	}
	
	/**
	 * Set the end time, to indicate this unit has ended execution.
	 */
	public void setEnded(DateTime time, int status) {
		if (endTime != null )
			throw new UnsupportedOperationException("Cannot set end time!  Component has already been defined.");
        //System.out.println("Status start = "+startTime.toString());
        //System.out.println("Status end = "+time.toString());
		if (startTime == null || startTime.gt(time)) 
			throw new UnsupportedOperationException("Cannot set 'ended' before setting 'started'.");
		if (!(status == COMPLETE || status == ABORTED ))
			throw new UnsupportedOperationException("If component has ended, status must be either COMPLETE or ABORTED.");
		this.status = status;
		this.endTime = new DateTime (time);
	}
	
	/**
	 * Return the current state of this object as a string.
	 */
	public String getStatus() {
		switch (status) {
			case 0: return "notdefined";
			case 1: return "waiting";
			case 2: return "ready";
			case 3: return "running";
			case 4: return "aborted";
			case 5: return "complete";
			default: return "***";
		}
	}
	
	/**
	 * Return the current state of this object as an int.
	 */
	public int getStatusAsInt() {
		return status;
	}
	/**
	 * return true if the status is not defined
	 * @return true if the status is not defined
	 */
	public boolean isDefined() {
		return status != NOTDEFINED;
	}
	
	/**
	 * return true if the status is waiting
	 * @return true if the status is waiting
	 */
	public boolean isWaiting() {
		return status == WAITING;
	}
	
	/**
	 * return true if the status is ready
	 * @return true if the status is ready
	 */
	public boolean isReady() {
		return status == READY;
	}
	
	/**
	 * return true if the status is running
	 * @return true if the status is running
	 */
	public boolean isRunning() {
		return status == RUNNING;
	}
	
	/**
	 * return true if the status is complete
	 * @return true if the status is complete
	 */
	public boolean isComplete() {
		return status == COMPLETE;
	}
	
	/**
	 * return true if the status is aborted
	 * @return true if the status is aborted
	 */
	public boolean isAborted() {
		return status == ABORTED;
	}
	
	/**
	 * Return true if and only if started is true.
	 * @return true if and only if started is true.
	 */
	public boolean isStarted() {
		return startTime != null;
	}
	
	/**
	 * Return true if and only if started is true.
	 * @return true if and only if started is true.
	 */
	public boolean isEnded() {
		return endTime != null;
	}
	
	/**
	 * Get the time this entity was ready.
	 * @return The time this entity was ready.
	 */
	public DateTime getReadyTime() {
		return readyTime;
	}

	/**
	 * Get the time this entity started.
	 * @return The time this entity started.
	 */
	public DateTime getStartTime() {
		return startTime;
	}

	/**
	 * Get the time this entity ended.
	 * @return The time this entity ended.
	 */
	public DateTime getEndTime() {
		return endTime;
	}

	/**
	 * Return this status object as a string.
	 */
	public String toString() {
		return getStatus();
	}
	
	/**
	 * Return this status object as a string, including the times. 
	 */
	public String getState() {
		String s = getStatus();
		if (readyTime != null) {
			s += " ready " + readyTime;
		}
		if (startTime != null) {
			s += " start " + startTime;
		}
		if (endTime != null) {
			s += " end " + endTime;
		}
		return s;
	}
	
}


