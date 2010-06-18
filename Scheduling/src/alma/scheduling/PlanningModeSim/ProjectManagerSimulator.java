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
 * File ProjectManagerSimulator.java
 */
 
package alma.scheduling.PlanningModeSim;
import java.util.logging.Level;

import alma.scheduling.NothingCanBeScheduledEnum;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.ExecBlock;
import alma.scheduling.Define.ObservedSession;
import alma.scheduling.Define.Program;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.ProjectQueue;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.Define.Status;
import alma.scheduling.Define.TaskControl;
import alma.scheduling.PlanningModeSim.Define.BasicComponent;
import alma.scheduling.PlanningModeSim.Define.SimulationException;
import alma.scheduling.Scheduler.DSA.SchedulerStats;

/**
 * Description 
 * 
 * @version 1.00  Jan 9, 2004
 * @author Allen Farris
 */
public class ProjectManagerSimulator
	extends BasicComponent
	implements alma.scheduling.Define.ProjectManager {
		
	private ArchiveSimulator archive;
	private Reporter reporter;
	private Project[] project;
    private TaskControl taskControl;

	/**
	 * An internal method used in the event an error is found in the simulation.
	 * A severe message is entered into the log and an exception is thrown.
	 * @param message The text of the error message.
	 * @throws SimulationException
	 */
	private void error(String message) throws SimulationException {
		logger.severe("ProjectManager.error " + message);
		throw new SimulationException("ProjectManager","ProjectManager " + Level.SEVERE + " " + message);
	}
	
	public ProjectManagerSimulator() {
	}

	public void initialize() throws SimulationException {
		archive = (ArchiveSimulator)containerServices.getComponent(Container.ARCHIVE);
		reporter = (Reporter)containerServices.getComponent(Container.REPORTER);
		logger.info(instanceName + ".initialized");
	} 

	public void execute() throws SimulationException {
		try {
			// Get the projects from the archive.
			project = archive.getAllProject();
		} catch (SchedulingException err) {
			error(err.toString());
		}
		logger.info(instanceName + ".execute complete");
	}
	
	/**
	 * Is the project to which the specified SB belongs a new project?
	 */
	public boolean newProject(SB unit) {
		return unit.getProject().getStatus().isStarted();
	}

	/**
	 * Return the number of SUnits remaining in the project to which
	 * the specified SUnit belongs.
	 */
	public int numberRemaining(SB unit) {
		Project prj = unit.getProject();
		SB[] sb = prj.getAllSBs();
		int count = 0;
		for (int i = 0; i < sb.length; ++i) {
			if (!(sb[i].getStatus().isEnded()))
				count++;
		}
		return count;
	}

	public void execStart(String arrayname, String sbId, DateTime time) throws SimulationException {
		try {
			// Get the SB.
			SB sb = archive.getSB(sbId);
			if (sb == null)
				error("Scheding block " + sbId + " was not found.");
			// Set its start time, if it hasn't been set.
			if (!sb.getStatus().isStarted())
				sb.setStartTime(time);
			else // Otherwise, set its state to running. 
				sb.setRunning();
			// Inform the reporter.
			reporter.execStart(arrayname,sbId,time);
		} catch (SchedulingException err) {
			error(err.toString());
		}
	}
	
	public void execEnd(String arrayname, String sbId, String execId, DateTime time) throws SimulationException {
		try {
			// Get the SB.
			SB sb = archive.getSB(sbId);
			if (sb == null)
				error("Scheding block " + sbId + " was not found.");
			if (!(sb.getStatus().isRunning()))
				error("Scheduling block " + sbId + " was not running!");
			// Make sure the execution block is there.
			ExecBlock ex = archive.getExec(execId);
			if (ex == null)
				error("Execution block " + execId + " was not found.");
			// Let the unit know it has ended.
            //ex.setParent(sb);
			//sb.execEnd(ex,time,Status.COMPLETE);
            logger.info(sbId +" about to set READY");
            sb.execEnd(ex,time,Status.READY);
            logger.info(sb.getStatus().getStatus());
			// Inform the reporter.
			reporter.execEnd(arrayname,sbId,execId,time);
		} catch (SchedulingException err) {
			error(err.toString());
		}
	}

    public void setProjectManagerTaskControl(TaskControl tc) {
        taskControl = tc;
    }
    public TaskControl getProjectManagerTaskControl() {
        return taskControl;
    }
	
    public ObservedSession createObservedSession(Program p) {
        return null;
    }

    public void sendStartSessionEvent(ObservedSession session){}
    public void sendEndSessionEvent(ObservedSession session){}
    public Project getProject(String projId) throws SchedulingException{
        return null;
    }
    public String[] archiveQuery(String query, String schema) throws SchedulingException {
        return null;
    }
    public Object archiveRetrieve(String uid) throws SchedulingException{
        return null;
    }

    public SBQueue getSBQueue() {
	return null;
    }

    public void archiveReleaseComponents() throws SchedulingException {
    }
    public SB[] getSBsForProject(String projId) throws SchedulingException{
        return null;
    }

    public ProjectQueue getProjectQueue() {
        return null;
    }
    
    public void getUpdates() throws SchedulingException {}
    public void publishNothingCanBeScheduled(NothingCanBeScheduledEnum reason){}
    
    public void addSchedulerStatsToArchive(SchedulerStats s){
        archive.addSchedulerStats(s);
    }
    public SchedulerStats[] getSchedulerStatsFromArchive() {
        return archive.getSchedulerStats();
    }

	@Override
	public void verifyRunnable(String sbId) throws SchedulingException { }

}
