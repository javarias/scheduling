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
 * File ProjectManagerTaskControl.java
 */
 
package ALMA.scheduling.project_manager;

import ALMA.scheduling.master_scheduler.TaskControlInfo;
//import ALMA.Control.ExecBlockEndEvent;
import ALMA.pipelinescience.ScienceProcessingRequestEnd;

/**
 * Description 
 * 
 * @version 1.00  Jul 17, 2003
 * @author Allen Farris
 */
public class ProjectManagerTaskControl extends TaskControlInfo {
	private int projectsCompleted;
	private int projectsInProgress;
	private int projectsNotStarted;
	private int projectsWaitingOnBreakpointResponses;
	private int pipelineRequestsCompleted;
	private int pipelineRequestsInProgress;
	private int sbCompleted;
	private int sbFailed;
	private int piMessagesSent;
	private int breakpointMessagesSent;
	private int piBreakpointResponses;

    private boolean control_event=false; //true if an event gotten from control
    private boolean pipeline_event=false; //true if event gotten from pipeline

    //private ExecBlockEndEvent c_end_event;
    //private ScienceProcessingRequestEnd p_end_event;

	/**
	 * @param masterScheduler
	 * @param task
	 */
	public ProjectManagerTaskControl(Thread masterScheduler, Thread task) {
		super(masterScheduler, task);
	}

	/**
	 * Increment the number of projects completed.
	 */
	public void incrementProjectsCompleted() {
		++projectsCompleted;
	}
	/**
	 * Decrement the number of projects completed.
	 */
	public void decrementProjectsCompleted() {
		--projectsCompleted;
	}

	/**
	 * Increment the number of projects in progress.
	 */
	public void incrementProjectsInProgress() {
		++projectsInProgress;
	}
	/**
	 * Decrement the number of projects in progress.
	 */
	public void decrementProjectsInProgress() {
		--projectsInProgress;
	}

	/**
	 * Increment the number of projects not started.
	 */
	public void incrementProjectsNotStarted() {
		++projectsNotStarted;
	}
	/**
	 * Decrement the number of projects not started.
	 */
	public void decrementProjectsNotStarted() {
		--projectsNotStarted;
	}
	
	/**
	 * Increment the number of projects waiting on breakpoint responses.
	 */
	public void incrementProjectsWaitingOnBreakpointResponses() {
		++projectsWaitingOnBreakpointResponses;
	}
	/**
	 * Decrement the number of projects waiting on breakpoint responses.
	 */
	public void decrementProjectsWaitingOnBreakpointResponses() {
		--projectsWaitingOnBreakpointResponses;
	}
	
	/**
	 * Increment the number of pipeline requests completed.
	 */
	public void incrementPipelineRequestsCompleted() {
		++pipelineRequestsCompleted;
	}
	/**
	 * Decrement the number of pipeline requests completed.
	 */
	public void decrementPipelineRequestsCompleted() {
		--pipelineRequestsCompleted;
	}

	/**
	 * Increment the number of pipeline requests in progress.
	 */
	public void incrementPipelineRequestsInProgress() {
		++pipelineRequestsInProgress;
	}
	/**
	 * Decrement the number of pipeline requests in progress.
	 */
	public void decrementPipelineRequestsInProgress() {
		--pipelineRequestsInProgress;
	}

	/**
	 * Increment the number of scheduling blocks completed.
	 */
	public void incrementSBCompleted() {
		++sbCompleted;
	}
	/**
	 * Decrement the number of scheduling blocks completed.
	 */
	public void decrementSBCompleted() {
		--sbCompleted;
	}

	/**
	 * Increment the number of scheduling blocks failed.
	 */
	public void incrementSBFailed() {
		++sbFailed;
	}
	/**
	 * Decrement the number of scheduling blocks failed.
	 */
	public void decrementSBFailed() {
		--sbFailed;
	}

	/**
	 * Increment the number of messages sent to the PI.
	 */
	public void incrementPIMessagesSent() {
		++piMessagesSent;
	}
	/**
	 * Decrement the number of messages sent to the PI.
	 */
	public void decrementPIMessagesSent() {
		--piMessagesSent;
	}

	/**
	 * Increment the number of breakpoint messages sent.
	 */
	public void incrementBreakpointMessagesSent() {
		++breakpointMessagesSent;
	}
	/**
	 * Decrement the number of breakpoint messages sent.
	 */
	public void decrementBreakpointMessagesSent() {
		--breakpointMessagesSent;
	}

	/**
	 * Increment the number of PI responses to breakpoints.
	 */
	public void incrementPIBreakpointResponses() {
		++piBreakpointResponses;
	}
	/**
	 * Decrement the number of PI responses to breakpoints.
	 */
	public void decrementPIBreakpointResponses() {
		--piBreakpointResponses;
	}

	/**
	 * Return the number of breakpoint messages sent.
	 * @return the number of breakpoint messages sent.
	 */
	public int getBreakpointMessagesSent() {
		return breakpointMessagesSent;
	}

	/**
	 * Return the number of PI responses to breakpoints.
	 * @return the number of PI responses to breakpoints.
	 */
	public int getPiBreakpointResponses() {
		return piBreakpointResponses;
	}

	/**
	 * Return the number of messages sent to the PI.
	 * @return the number of messages sent to the PI.
	 */
	public int getPiMessagesSent() {
		return piMessagesSent;
	}

	/**
	 * Return the number of pipeline requests completed.
	 * @return the number of pipeline requests completed.
	 */
	public int getPipelineRequestsCompleted() {
		return pipelineRequestsCompleted;
	}

	/**
	 * Return the number of pipeline requests in progress.
	 * @return return the number of pipeline requests in progress.
	 */
	public int getPipelineRequestsInProgress() {
		return pipelineRequestsInProgress;
	}

	/**
	 * Return the number of projects completed.
	 * @return the number of projects completed.
	 */
	public int getProjectsCompleted() {
		return projectsCompleted;
	}

	/**
	 * Return the number of projects in progress.
	 * @return the number of projects in progress.
	 */
	public int getProjectsInProgress() {
		return projectsInProgress;
	}

	/**
	 * Return the number of projects not started.
	 * @return the number of projects not started.
	 */
	public int getProjectsNotStarted() {
		return projectsNotStarted;
	}

	/**
	 * Return the number of projects waiting on breakpoint responses.
	 * @return the number of projects waiting on breakpoint responses.
	 */
	public int getProjectsWaitingOnBreakpointResponses() {
		return projectsWaitingOnBreakpointResponses;
	}

	/**
	 * Return the number of scheduling blocks completed.
	 * @return the number of scheduling blocks completed.
	 */
	public int getSbCompleted() {
		return sbCompleted;
	}

	/**
	 * Return the number of scheduling blocks failed.
	 * @return the number of scheduling blocks failed.
	 */
	public int getSbFailed() {
		return sbFailed;
	}

	/**
	 * Return the number of projects in progress.
	 * @param i the number of projects in progress.
	 */
	public void setProjectsInProgress(int i) {
		projectsInProgress = i;
	}

	/**
	 * Return the number of projects not started.
	 * @param i the number of projects not started.
	 */
	public void setProjectsNotStarted(int i) {
		projectsNotStarted = i;
	}

	public static void main(String[] args) {
	}

}
