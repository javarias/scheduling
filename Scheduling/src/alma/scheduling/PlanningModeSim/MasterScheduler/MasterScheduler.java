/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
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
 * File MasterScheduler.java
 * 
 */
package alma.scheduling.PlanningModeSim.MasterScheduler;

import alma.scheduling.Scheduler.*;

import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.Policy;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.NothingCanBeScheduled;
import alma.scheduling.Define.TaskControl;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.SchedulingException;

import alma.scheduling.PlanningModeSim.TelescopeSimulator;
import alma.scheduling.PlanningModeSim.OperatorSimulator;
import alma.scheduling.PlanningModeSim.Define.BasicComponent;
import alma.scheduling.PlanningModeSim.Define.SimulationException;
import alma.scheduling.PlanningModeSim.Container;
import alma.scheduling.PlanningModeSim.ArchiveSimulator;
import alma.scheduling.PlanningModeSim.ControlSimulator;
import alma.scheduling.PlanningModeSim.ProjectManagerSimulator;
import alma.scheduling.PlanningModeSim.Reporter;
import alma.scheduling.PlanningModeSim.ClockSimulator;
import alma.scheduling.PlanningModeSim.SimulationInput;

import java.util.logging.Level;

/**
 * The MasterScheduler class is the major controlling class in the Scheduling
 * Subsystem.  See Scheduling Subsystem Design document, section 3.2.1.
 * 
 * @version 1.10 Dec. 18, 2003
 * @author Allen Farris
 */
public class MasterScheduler extends BasicComponent {
	
	private ArchiveSimulator archive;
	private ControlSimulator control;
	private ClockSimulator clock;
	private DynamicScheduler scheduler;
	private Policy policy;
	private SB[] sbList;
	private int advanceTheClock;
	private TelescopeSimulator telescope;
	private OperatorSimulator operator;
	private ProjectManagerSimulator projectManager;
	private Reporter reporter;
	private SimulationInput data;
	
	public MasterScheduler() {
	}

	/**
	 * An internal method used in the event an error is found in the simulation.
	 * A severe message is entered into the log and an exception is thrown.
	 * @param message The text of the error message.
	 * @throws SimulationException
	 */
	private void error(String message) throws SimulationException {
		logger.severe("MasterScheduler.error " + message);
		throw new SimulationException("MasterScheduler","MasterScheduler " + Level.SEVERE + " " + message);
	}

	public void initialize() throws SimulationException {
		data = (SimulationInput)containerServices.getComponent(Container.SIMULATION_INPUT);
		advanceTheClock = data.getAdvanceClock();
		logger.info(instanceName + ".initialized");
	}

	public void execute() throws SimulationException {
		// Get the archive component.
		archive = (ArchiveSimulator)containerServices.getComponent(Container.ARCHIVE);
		// Get the control component.
		control = (ControlSimulator)containerServices.getComponent(Container.CONTROL);
		// Get the clock component.
		clock = (ClockSimulator)containerServices.getComponent(Container.CLOCK);
		// Get the telescope component.
		telescope = (TelescopeSimulator)containerServices.getComponent(Container.TELESCOPE);
		// Get the operator component.
		operator = (OperatorSimulator)containerServices.getComponent(Container.OPERATOR);
		// Get the project manager component.
		projectManager = (ProjectManagerSimulator)containerServices.getComponent(Container.PROJECT_MANAGER);
		// Get the Reporter.
		reporter = (Reporter)containerServices.getComponent(Container.REPORTER);
		try {
			// Get the scheduling policy from the archive. (We'll just use the first one.)
			Policy[] x = archive.getPolicy();
			if (x == null || x.length == 0)
				error("There is no scheduling policy.");
			policy = x[0];
			// Get the scheduling blocks from the archive.
			sbList = archive.getAllSB();
		} catch (SchedulingException err) {
			error(err.toString());
		}
		logger.info(instanceName + ".execute complete");
	}

	public void runSimulation() {
		logger.info("The simulation is running now.");
		try {
			// Mark all the projects ready.
			Project[] p = archive.getAllProject();
			DateTime now = clock.getDateTime();
			for (int i = 0; i < p.length; ++i)
				p[i].setReady(now);

			// Create an array.
			String[] ant = control.getIdleAntennas();
			String arrayName = control.createArray(ant);
			
			// Create a scheduler configuration.
			SBQueue queue = new SBQueue ();
			queue.add(sbList);
            projectManager.setProjectManagerTaskControl(
                    new TaskControl(Thread.currentThread()));
			SchedulerConfiguration config = new SchedulerConfiguration (
					Thread.currentThread(),
					true,true,queue,5,0,arrayName,clock,control,operator,
                    telescope, projectManager,policy,logger);			
			
			// Create a scheduler.
            System.out.println("in MS: "+config.getArrayName());
			scheduler = new DynamicScheduler(config);		
			
			// Create a thread for this scheduler
			Thread task = new Thread (scheduler);
            projectManager.getProjectManagerTaskControl().setTask(task);
			
			// Set paramaters in the config object.
			config.setTask(task);
			config.setCommandedStartTime(data.getBeginTime());
			config.setCommandedEndTime(data.getEndTime());
			
			// Start the scheduler.
			task.start();
			
			// Wait for the task to complete, except that we will
			// take action in case nothing can be scheduled.
			while (true) {
				try {
					task.join();
					break;
				} catch (InterruptedException ex) {
					if (config.isNothingToSchedule()) {
						NothingCanBeScheduled r = config.getNothingToSchedule();
						logger.info(r.toString());
						// Let the reporter know.
						reporter.nothingCouldBeScheduled(r);
						config.clearMessage();
						clock.advance(advanceTheClock);
						config.respondContinue();
					} else {
						ex.printStackTrace(System.out);
						throw new IllegalStateException("Why are we being interrupted?");
					}
				}
			}
			
			if (!config.isOperational()) {
				logger.info("Scheduler has ended at " + config.getActualEndTime());
			}
		
		} catch (SchedulingException err) {
			System.out.println(err.toString());
		}
		
		logger.info("The simulation has ended.");
	}

}

