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
package alma.scheduling.planning_mode_sim.master_scheduler;

import alma.scheduling.planning_mode_sim.define.BasicComponent;
import alma.scheduling.planning_mode_sim.define.ComponentState;
import alma.scheduling.planning_mode_sim.define.Clock;
import alma.scheduling.planning_mode_sim.define.acs.component.ComponentLifecycleException;
import alma.scheduling.planning_mode_sim.define.SiteCharacteristics;

import alma.scheduling.planning_mode_sim.scheduler.Scheduler;
import alma.scheduling.planning_mode_sim.define.SPolicy;
import alma.scheduling.planning_mode_sim.define.SUnit;

import alma.scheduling.planning_mode_sim.simulator.Container;
import alma.scheduling.planning_mode_sim.simulator.ArchiveSimulator;
import alma.scheduling.planning_mode_sim.simulator.ControlSimulator;
import alma.scheduling.planning_mode_sim.simulator.ClockSimulator;
import alma.scheduling.planning_mode_sim.simulator.SimulationInput;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The MasterScheduler class is the major controlling class in the Scheduling
 * Subsystem.  See Scheduling Subsystem Design document, section 3.2.1.
 * 
 * @version 1.00 May 1, 2003
 * @author Allen Farris
 */
public class MasterScheduler extends BasicComponent {
	
	private ArchiveSimulator archive;
	private ControlSimulator control;
	private ClockSimulator clock;
	private Scheduler scheduler;
	private SPolicy policy;
	private SUnit[] sbList;
	private int advanceTheClock;
	private SiteCharacteristics site;
	
	public MasterScheduler(SimulationInput data) {
		super(data);
		advanceTheClock = data.getAdvanceClock();
		site = data.getSite();
	}

	public void execute() throws ComponentLifecycleException {
		super.execute();
		try {
			// Get the archive component.
			archive = (ArchiveSimulator)m_containerServices.getComponent(Container.ARCHIVE);
			// Get the control component.
			control = (ControlSimulator)m_containerServices.getComponent(Container.CONTROL);
			// Get the clock component.
			clock = (ClockSimulator)m_containerServices.getComponent(Container.CLOCK);
			// Get the scheduling policy from the archive. (We'll just use the first one.)
			SPolicy[] x = archive.getSPolicy();
			if (x == null || x.length == 0)
				throw new ComponentLifecycleException("MasterScheduler " + Level.SEVERE + "There is no scheduling policy.");
			policy = x[0];
			// Get the scheduling blocks from the archive.
			sbList = archive.getAllSUnit();
		} catch (Exception err) {
			m_state.setState(ComponentState.ERROR);
			m_logger.severe("MasterScheduler.error " + err.toString());
			throw new ComponentLifecycleException("MasterScheduler " + Level.SEVERE + " " + err.toString());
		}
	}

	public void runSimulation() {
		System.out.println("The simulation is running now.");
		try {

			// Create a subarray.
			short[] ant = control.getIdleAntennas();
			short subarrayId = control.createSubarray(ant);
			
			// Create a scheduler.
			scheduler = new Scheduler(subarrayId, sbList, policy, data.getEnd(), this);		
			
			// Set the scheduler's output file.
			scheduler.setOut(data.getOut());
			
			// Set control's callback.
			control.setScheduler(scheduler);
			
			// Run the scheduler.
			scheduler.run();
		
		} catch (SchedulingException err) {
			System.out.println(err.toString());
		}
		
		System.out.println("The simulation has ended.");
	}

	public ArchiveProxy getArchive() {
		return archive;
	}

	public ControlProxy getControl() {
		return control;
	}
	
	public Clock getClock() {
		return clock;
	}
	
	public void cannotScheduleAnything() {
		// This is called by the Scheduler whenever it cannot schedule anything.
		// We will just advance the clock and continue.
		//m_logger.info("Nothing can be scheduled at " + clock.getDateTime());
		clock.advance(advanceTheClock);
	}
	
	public Logger getLogger() {
		return m_logger;
	}

	/**
	 * @return
	 */
	public SiteCharacteristics getSite() {
		return site;
	}

}

