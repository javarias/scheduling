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
 * File TaskControlInfo.java
 */
 
package ALMA.scheduling.master_scheduler;

import ALMA.scheduling.define.ComponentState;
import ALMA.scheduling.define.STime;

/**
 * TaskControlInfo is an informational object that is used by the
 * MasterScheduler to monitor and control a subordinate component
 * that is a thread.  Such an object is shared between the MasterScheduler
 * and the subordinate thread. 
 * 
 * @version 1.00  Jul 16, 2003
 * @author Allen Farris
 */
public class TaskControlInfo {
	
	private ComponentState taskState;
	private String errMsg;
	private boolean stopFlag;
	private Thread masterScheduler;
	private Thread task;
	private STime startTime;
	private STime estimatedEndTime;
	private STime actualEndTime;
	private STime errorTime;

	/**
	 * Create an information object to control a subordinate thread.
	 * This object is shared between the MasterScheduler thread and the
	 * subordinate thread.
	 * @param masterScheduler the MasterSheduler that creates and monitors
	 * the subordinate thread.
	 * @param task The subordinate thread.
	 */
	public TaskControlInfo(Thread masterScheduler, Thread task) {
		this.taskState = new ComponentState(ComponentState.NEW);
		this.errMsg = new String ("");
		this.stopFlag = false;
		this.masterScheduler = masterScheduler;
		this.task = task;
		this.startTime = null;
		this.estimatedEndTime = null;
		this.actualEndTime = null;
		this.errorTime = null;
	}

	/**
	 * Interrupt the MasterScheduler thread.
	 */
	public synchronized void interruptMasterScheduler() {
		masterScheduler.interrupt();
	}
	
	/**
	 * Interrupt the subordinate thread.
	 */
	public synchronized void interruptTask() {
		task.interrupt();
	}

	/**
	 * Tell the subordinate thread to stop. This method merely posts a
	 * message requesting the subordiante thread to stop.  It does
	 * not guarantee when or under what circumstances the subordinate
	 * task will actually stop.
	 */
	public synchronized void stopTask() {
		stopFlag = true;
		interruptTask();
	}

	/**
	 * return the actual time the subordiante thread stopped.
	 * @return the actual time the subordiante thread stopped.
	 */
	public synchronized STime getActualEndTime() {
		return actualEndTime;
	}

	/**
	 * return an error message detailing why the subordinate aborted or 
	 * entered the error state.
	 * @return an error message detailing why the subordinate aborted or 
	 * entered the error state.
	 */
	public synchronized String getErrMsg() {
		return errMsg;
	}

	/**
	 * return the time when the subordinate aborted or entered the error state.
	 * @return the time when the subordinate aborted or entered the error state.
	 */
	public synchronized STime getErrorTime() {
		return errorTime;
	}

	/**
	 * return the estimated time when the subordinate thread will end.  If this time
	 * is unknown or not yet determined, null is returned.
	 * @return the estimated time when the subordinate thread will end.  If this time
	 * is unknown or not yet determined, null is returned.
	 */
	public synchronized STime getEstimatedEndTime() {
		return estimatedEndTime;
	}

	/**
	 * return the MasterScheduler thread.
	 * @return the MasterScheduler thread.
	 */
	public synchronized Thread getMasterScheduler() {
		return masterScheduler;
	}

	/**
	 * return the time the subordinate thread was started.
	 * @return the time the subordinate thread was started.
	 */
	public synchronized STime getStartTime() {
		return startTime;
	}

	/**
	 * Has there been a request for the subordinate thread to stop?
	 * @return the stop flag; "true" is returned if and only if the stop
	 * flag is set, i.e., there has been a request for the subordinate
	 * thread to stop.
	 */
	public synchronized boolean isStopFlag() {
		return stopFlag;
	}

	/**
	 * return the subordinate thread.
	 * @return the subordinate thread.
	 */
	public synchronized Thread getTask() {
		return task;
	}

	/**
	 * return the state of the subordinate thread.
	 * @return the state of the subordinate thread.
	 */
	public synchronized ComponentState getTaskState() {
		return taskState;
	}

	/**
	 * Set the time the subordinate thread started.
	 * @param time the time the subordinate thread started.
	 */
	public synchronized void setActualEndTime(STime time) {
		actualEndTime = time;
	}

	/**
	 * Set the error message that indicates why the subordinate thread
	 * aborted or entered the error state.
	 * @param string Set the error message
	 */
	public synchronized void setErrMsg(String string) {
		errMsg = string;
	}

	/**
	 * Set the time when the subordinate aborted or entered the error state.
	 * @param time the time when the subordinate aborted or entered the error state.
	 */
	public synchronized void setErrorTime(STime time) {
		errorTime = time;
	}

	/**
	 * Set the estimated time when the subordinate thread will end.
	 * @param time the estimated time when the subordinate thread will end.
	 */
	public synchronized void setEstimatedEndTime(STime time) {
		estimatedEndTime = time;
	}

	/**
	 * Set the time the subordinate thread was started.
	 * @param time the time the subordinate thread was started.
	 */
	public synchronized void setStartTime(STime time) {
		startTime = time;
	}

	/**
	 * Set the state of the subordinate thread.
	 * @param state the state of the subordinate thread.
	 */
	public synchronized void setTaskState(ComponentState state) {
		taskState = state;
	}

}
