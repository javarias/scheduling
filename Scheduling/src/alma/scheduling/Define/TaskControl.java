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
 * File TaskControl.java
 */
 
package alma.scheduling.Define;

/**
 * TaskControl is an informational object that is used by the
 * MasterScheduler to monitor and control a subordinate thread.  
 * This TaskControl object is shared between the MasterScheduler
 * and the subordinate thread. The following data items are included:
 * <ul>
 * <li> The master scheduler thread
 * <li> The subordinate task thread
 * <li> The state of the subordinate thread: operational or not
 * <li> The time the task started
 * <li> A hard deadline time at which the subordinate task is to end
 * <li> The estimated time that the task will end
 * <li> The time the task actually ended
 * <li> A flag used to tell the task to stop
 * <li> An error message stating why the task has entered an error state
 * <li> The time the task entered an error state
 * </ul>
 * The subordinate task can be stopped either by setting a hard stop time
 * or by setting the stop flag. All of the indicated times may be null.
 * 
 * @version 1.30 May 10, 2004
 * @author Allen Farris
 */
public class TaskControl {

	/**
	 * The state of the subordinate thread: operational or not.
	 */
	private boolean operational;
	/**
	 * An error message stating why the task has entered an error state.
	 */
	private String errMsg;
	/**
	 * A flag used to tell the task to stop.
	 */
	private boolean stopFlag;
	/**
	 * The master scheduler thread.
	 */
	private Thread masterScheduler;
	/**
	 * The subordinate task thread.
	 */
	private Thread task;
	/**
	 * The time the task is commanded to start.
	 */
	private DateTime commandedStartTime;
	/**
	 * The time the task actually started.
	 */
	private DateTime actualStartTime;
	/**
	 * A hard deadline at which the subordinate task is to end.
	 */
	private DateTime commandedEndTime;
	/**
	 * The estimated time that the task will end.
	 */
	private DateTime estimatedEndTime;
	/**
	 * The time the task actually ended.
	 */
	private DateTime actualEndTime;
	/**
	 * The time the task entered an error state. 
	 */
	private DateTime errorTime;

	/**
	 * Create an information object to control a subordinate thread.
	 * This object is shared between the MasterScheduler thread and the
	 * subordinate thread.
	 * @param masterScheduler the MasterSheduler that creates and monitors
	 * the subordinate thread.
	 */
	public TaskControl(Thread masterScheduler) {
		operational = false;
		this.errMsg = new String ("");
		this.stopFlag = false;
		this.masterScheduler = masterScheduler;
		this.task = null;
		this.commandedStartTime = null;
		this.actualStartTime = null;
		this.commandedEndTime = null;
		this.estimatedEndTime = null;
		this.actualEndTime = null;
		this.errorTime = null;
	}
	
	/**
	 * Set the subordinate thread.
	 * @param task The subordinate thread 
	 */
	public synchronized void setTask(Thread task) {
		this.task = task;
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
	}

	/**
	 * Check for a null or zero time.
	 * @param time The time to be checked.
	 * @exception An IllegalArgumentException exception is thrown if 
	 * the time is null or has a zero value. 
	 */
	private void checkTime(DateTime time) {
		if (time == null || time.isNull())
			throw new IllegalArgumentException("Time cannot be null.");
	}
	
	/**
	 * Indicatethe subordinate thread ended normally.
	 * @param time the time at which the subordinate thread ended.
	 */
	public synchronized void normalEnd(DateTime time) {
		checkTime(time);
		actualEndTime = time;
		operational = false;
	}

	/**
	 * Indicate the subordinate thread aborted.
	 * @param err A message indicating the error.
	 * @param time The time at which this condition occurred.
	 */
	public synchronized void errorEnd(String err, DateTime time) {
		checkTime(time);
		errMsg = err;
		errorTime = time;
		actualEndTime = time;
		operational = false;
	}

	/**
	 * Set the estimated time when the subordinate thread will end.
	 * @param time the estimated time when the subordinate thread will end.
	 */
	public synchronized void setEstimatedEndTime(DateTime time) {
		checkTime(time);
		estimatedEndTime = time;
	}

	/**
	 * Set the commanded time when the subordinate thread will end.
	 * @param time the commanded time when the subordinate thread will end.
	 */
	public synchronized void setComamndedEndTime(DateTime time) {
		checkTime(time);
		commandedEndTime = time;
	}

	/**
	 * Set the commanded time when the subordinate thread will end.
	 * @param time the commanded time when the subordinate thread will end.
	 */
	public synchronized void setComamndedStartTime(DateTime time) {
		checkTime(time);
		commandedStartTime = time;
	}

	/**
	 * Set the time the subordinate thread was started.
	 * @param start the time the subordinate thread was started.
	 * @param end the estimated time the subordinate thread will end.
	 * If the estimated end time is unknown, this time should be null.
	 */
	public synchronized void start(DateTime start, DateTime end) {
		checkTime(start);
		actualStartTime = start;
		if (end != null)
			estimatedEndTime = end.isNull() ? null : end;
		operational = true;
	}

	// Getters
	
	/**
	 * return the actual time the subordiante thread stopped.
	 * @return the actual time the subordiante thread stopped
	 * or null if this time has not been set.
	 */
	public synchronized DateTime getActualEndTime() {
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
	 * @return the time when the subordinate aborted or entered the error state
	 * or null if this time has not been set.
	 */
	public synchronized DateTime getErrorTime() {
		return errorTime;
	}

	/**
	 * return the estimated time when the subordinate thread will end
	 * @return the estimated time when the subordinate thread will end
	 * or null if this time has not been set.
	 */
	public synchronized DateTime getEstimatedEndTime() {
		return estimatedEndTime;
	}

	/**
	 * return the MasterScheduler thread.
	 * @return the MasterScheduler thread.
	 */
	public Thread getMasterScheduler() {
		return masterScheduler;
	}

	/**
	 * return the time the subordinate thread is commanded to stop.
	 * @return the time the subordinate thread is commanded to stop 
	 * or null if this time has not been set.
	 */
	public synchronized DateTime getCommandedEndTime() {
		return commandedEndTime;
	}

	/**
	 * return the time the subordinate thread was commanded to start.
	 * @return the time the subordinate thread was started
	 * or null if this time has not been set.
	 */
	public synchronized DateTime getCommandedStartTime() {
		return commandedStartTime;
	}

	/**
	 * return the time the subordinate thread actaully started.
	 * @return the time the subordinate thread was started
	 * or null if this time has not been set.
	 */
	public synchronized DateTime getActualStartTime() {
		return actualStartTime;
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
	public Thread getTask() {
		return task;
	}

	/**
	 * return the state of the subordinate thread: operational or not.
	 * @return the state of the subordinate thread: operational or not.
	 */
	public synchronized boolean isOperational() {
		return operational;
	}

}
