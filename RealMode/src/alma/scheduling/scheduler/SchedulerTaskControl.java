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
 * File SchedulerTaskControl.java
 */
 
package alma.scheduling.scheduler;

import alma.scheduling.master_scheduler.TaskControlInfo;

/**
 * Description 
 * 
 * @version 1.00  Jul 17, 2003
 * @author Allen Farris
 */
public class SchedulerTaskControl extends TaskControlInfo {
	private int sbsCompleted;
	private int sbsNotStarted;
	private int sbsFailed;

	/**
	 * @param masterScheduler
	 * @param task
	 */
	public SchedulerTaskControl(Thread masterScheduler, Thread task) {
		super(masterScheduler, task);
	}

	/**
	 * Increment the number of schedblocks completed.
	 */
	public synchronized void incrementSbsCompleted() {
		++sbsCompleted;
	}
	/**
	 * Decrement the number of schedblocks completed.
	 */
	public synchronized void decrementSbsCompleted() {
		--sbsCompleted;
	}

	/**
	 * Increment the number of schedblocks not started.
	 */
	public synchronized void incrementSbsNotStarted() {
		++sbsNotStarted;
	}
	/**
	 * Decrement the number of schedblocks not started.
	 */
	public synchronized void decrementSbsNotStarted() {
		--sbsNotStarted;
	}
	
	/**
	 * Increment the number of scheduling blocks failed.
	 */
	public synchronized void incrementSbsFailed() {
		++sbsFailed;
	}
	/**
	 * Decrement the number of scheduling blocks failed.
	 */
	public synchronized void decrementSbsFailed() {
		--sbsFailed;
	}

	/**
	 * Return the number of scheduling blocks completed.
	 * @return the number of scheduling blocks completed.
	 */
	public synchronized int getSbsCompleted() {
		return sbsCompleted;
	}

	/**
	 * Return the number of scheduling blocks failed.
	 * @return the number of scheduling blocks failed.
	 */
	public synchronized int getSbsFailed() {
		return sbsFailed;
	}

	public synchronized static void main(String[] args) {
	}

}
