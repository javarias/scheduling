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
 * File ArchiveSimulator.java
 */
 
package alma.scheduling.planning_mode_sim.simulator;

import alma.scheduling.planning_mode_sim.define.BasicComponent;
import alma.scheduling.planning_mode_sim.define.SProject;
import alma.scheduling.planning_mode_sim.define.SUnit;
import alma.scheduling.planning_mode_sim.define.SUnitSet;
import alma.scheduling.planning_mode_sim.define.DateTime;
import alma.scheduling.planning_mode_sim.define.SPolicy;
import alma.scheduling.planning_mode_sim.define.SExec;
import alma.scheduling.planning_mode_sim.define.ComponentState;
import alma.scheduling.planning_mode_sim.master_scheduler.ArchiveProxy;
import alma.scheduling.planning_mode_sim.master_scheduler.SchedulingException;

import alma.scheduling.planning_mode_sim.define.acs.component.ComponentLifecycleException;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * The ArchiveSimulator stores the relevant entities needed for the
 * simulation.  It stores them in memory and  provides the same interface
 * that one uses in getting them from the real archive. 
 * 
 * @version 1.00  Jul 18, 2003
 * @author Allen Farris
 */
public class ArchiveSimulator extends BasicComponent implements ArchiveProxy {

	private static int idGenerator = 0;
	private static String generateId() {
		++idGenerator;
		return Integer.toString(idGenerator);
	}

	private ClockSimulator clock;
	private SProject[] project;
	private SUnitSet[] set;
	private SUnit[] unit;
	private SPolicy[] policy;
	private ArrayList ppr;
	private ArrayList sExec;
	private boolean initialLoad;

	public ArchiveSimulator(SimulationInput data) {
		super(data);
		initialLoad = false;
	}

	/**
	 * An internal method used in the event an error is found in the simulation.
	 * A severe message is entered into the log and an exception is thrown.
	 * @param message The text of the error message.
	 * @throws SimulationException
	 */
	private void error(String message) throws SchedulingException {
		m_state.setState(ComponentState.ERROR);
		m_logger.severe("Archive.error " + message);
		throw new SchedulingException("Archive " + Level.SEVERE + " " + message);
	}

	public void initialize() throws ComponentLifecycleException {
		super.initialize();
		try {
			clock = (ClockSimulator)m_containerServices.getComponent(Container.CLOCK);
			ppr = new ArrayList ();
			sExec = new ArrayList ();
		} catch (Exception err) {
			m_state.setState(ComponentState.ERROR);
			m_logger.severe("Archive.error " + err.toString());
			throw new ComponentLifecycleException("Archive " + Level.SEVERE + " " + err.toString());
		}
	}

	public void execute() throws ComponentLifecycleException {
		super.execute();
		if (!initialLoad)
			throw new ComponentLifecycleException ("Initial data has not been loaded.");
	}
	
	// Initial load.
	
	public void initialLoad() {
		loadSPolicy();
		loadSUnit();
		loadSUnitSet();
		loadProject();
		initialLoad = true;
	}
	
	private void loadProject() {
		SProject[] x = data.getProject();
		for (int i =0; i < x.length; ++i) {
			x[i].setId(generateId());
			x[i].setTimeCreated(clock.getDateTime());
			x[i].setTimeUpdated(clock.getDateTime());
			x[i].setMemberLink();
		}
		this.project = x;
		m_logger.info("Archive.load Project data loaded.");
	}

	private void loadSUnitSet() {
		SUnitSet[] x = data.getSUnitSet();
		for (int i =0; i < x.length; ++i) {
			x[i].setId(generateId());
			x[i].setTimeCreated(clock.getDateTime());
			x[i].setTimeUpdated(clock.getDateTime());
		}
		this.set = x;
		m_logger.info("Archive.load ObsUnitSet data loaded.");
	}

	private void loadSUnit() {
		SUnit[] x = data.getSUnit();
		for (int i =0; i < x.length; ++i) {
			x[i].setId(generateId());
			x[i].setTimeCreated(clock.getDateTime());
			x[i].setTimeUpdated(clock.getDateTime());
		}
		this.unit = x;
		m_logger.info("Archive.load Scheduling block data loaded.");
	}

	private void loadSPolicy() {
		SPolicy[] x = null;
		try {
			x = data.getSPolicy();
		} catch (SimulationException err) {
			m_logger.severe("Error loading scheduling policy from input.");
			System.out.println(err.toString());
			System.exit(0);
		}
		for (int i =0; i < x.length; ++i) {
			x[i].setId(generateId());
			x[i].setTimeCreated(clock.getDateTime());
			x[i].setTimeUpdated(clock.getDateTime());
		}
		this.policy = x;
		m_logger.info("Archive.load Scheduling policy data loaded.");
	}

	// Projects.

	public SProject[] getAllProject() throws SchedulingException {
		return this.project;
	}

	public SProject[] getNewProject(DateTime t) throws SchedulingException {
		return new SProject [0];
	}

	public SProject getProject(String id) throws SchedulingException {
		for (int i = 0; i < project.length; ++i) {
			if (project[i].getId().equals(id))
				return project[i];
		}
		String message = "Archive.notFound: Project " + id + " was not found.";
		m_logger.fine(message);
		throw new SchedulingException(message);
	}

	public void updateProject(SProject p) throws SchedulingException {
		for (int i = 0; i < project.length; ++i) {
			if (project[i] == p) {
				return;
			}
		}
		String message = "Archive.notFound: Project " + p.getId() + " was not found.";
		m_logger.fine(message);
		throw new SchedulingException(message);
	}

	// SUnitSet
	
	public SUnitSet getSUnitSet(String id) throws SchedulingException {
		for (int i = 0; i < set.length; ++i) {
			if (set[i].getId().equals(id))
				return set[i];
		}
		String message = "Archive.notFound: UnitSet " + id + " was not found.";
		m_logger.fine(message);
		throw new SchedulingException(message);
	}

	public void updateSUnitSet(SUnitSet s) throws SchedulingException {
		for (int i = 0; i < set.length; ++i) {
			if (set[i] == s) {
				return;
			}
		}
		String message = "Archive.notFound: UnitSet " + s.getId() + " was not found.";
		m_logger.fine(message);
		throw new SchedulingException(message);
	}

	// SUnits.
	
	public SUnit[] getAllSUnit() throws SchedulingException {
		return this.unit;
	}

	public SUnit[] getNewSUnit(DateTime t) throws SchedulingException {
		return new SUnit [0];
	}

	public SUnit getSUnit(String id) throws SchedulingException {
		for (int i = 0; i < unit.length; ++i) {
			if (unit[i].getId().equals(id))
			return unit[i];
		}
		String message = "Archive.notFound: Scheduling block " + id + " was not found.";
		m_logger.fine(message);
		throw new SchedulingException(message);
	}

	public void updateSUnit(SUnit s) throws SchedulingException {
		for (int i = 0; i < unit.length; ++i) {
			if (unit[i] == s) {
				return;
			}
		}
		String message = "Archive.notFound: Scheduling block " + s.getId() + " was not found.";
		m_logger.fine(message);
		throw new SchedulingException(message);
	}

	// Policies.
	
	public SPolicy[] getSPolicy() throws SchedulingException {
		return policy;
	}
	
	// SExec.
	
	public void newSExec(SExec x) throws SchedulingException {
			x.setId(generateId());
			x.setTimeCreated(clock.getDateTime());
			x.setTimeUpdated(clock.getDateTime());
			this.sExec.add(x);
	}

	public void updateSExec(SExec x) throws SchedulingException {
			x.setTimeUpdated(clock.getDateTime());
	}

	public SExec[] getAllSExec() throws SchedulingException {
		SExec[] x = new SExec [ppr.size()];
		x = (SExec[])ppr.toArray(x);
		return x;
	}
	
	public SExec getSExec(String id) throws SchedulingException {
		SExec x = null;
		for (int i = 0; i < ppr.size(); ++i) {
			x = (SExec)ppr.get(i);
			if (x.getId().equals(id))
				return x;
		}
		String message = "Archive.notFound: SExec " + id + " was not found.";
		m_logger.fine(message);
		throw new SchedulingException(message);
	}
	
	// PipelineProcessingRequests.

	/*public void newSppr(Sppr ppr) throws SchedulingException {
		ppr.setId(generateId());
		ppr.setTimeCreated(clock.getDateTime());
		ppr.setTimeUpdated(clock.getDateTime());
		this.ppr.add(ppr);
	}
	
	public Sppr[] getAllSppr() throws SchedulingException {
		Sppr[] x = new Sppr [ppr.size()];
		x = (Sppr[])ppr.toArray(x);
		return x;
	}
	
	public Sppr getSppr(String id) throws SchedulingException {
		Sppr x = null;
		for (int i = 0; i < ppr.size(); ++i) {
			x = (Sppr)ppr.get(i);
			if (x.getId().equals(id))
				return x;
		}
		String message = "Archive.notFound: PipelineProcessingRequest " + id + " was not found.";
		m_logger.fine(message);
		throw new SchedulingException(message);
	}
	
	public void updateSppr(Sppr p) throws SchedulingException {
		if (ppr.contains(p))
			return;
		String message = "Archive.notFound: PipelineProcessingRequest " + p.getId() + " was not found.";
		m_logger.fine(message);
		throw new SchedulingException(message);
	}*/

}
