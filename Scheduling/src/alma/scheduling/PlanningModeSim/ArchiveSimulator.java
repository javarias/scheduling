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
 
package alma.scheduling.PlanningModeSim;

import alma.scheduling.Define.Session;
import alma.scheduling.PlanningModeSim.Define.BasicComponent;
import alma.scheduling.PlanningModeSim.Define.SimulationException;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.Program;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Policy;
import alma.scheduling.Define.ExecBlock;
import alma.scheduling.Define.Archive;
import alma.scheduling.Define.SciPipelineRequest;
import alma.scheduling.Define.SchedulingException;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * The ArchiveSimulator stores the relevant entities needed for the
 * simulation.  It stores them in memory and  provides the same interface
 * that one uses in getting them from the real archive. 
 * 
 * @version 1.10  Dec. 12, 2003
 * @author Allen Farris
 */
public class ArchiveSimulator extends BasicComponent implements Archive {

	private ClockSimulator clock;
	private Project[] project;
	private Program[] set;
	private SB[] unit;
	private Policy[] policy;
	private ArrayList sExec;
	private boolean initialLoad;

	public ArchiveSimulator() {
		initialLoad = false;
	}

	/**
	 * An internal method used in the event an error is found in the simulation.
	 * A severe message is entered into the log and an exception is thrown.
	 * @param message The text of the error message.
	 * @throws SimulationException
	 */
	private void error(String message) throws SimulationException {
		logger.severe("Archive.error " + message);
		throw new SimulationException("ArchiveSimulator","Archive " + Level.SEVERE + " " + message);
	}

	public void initialize() throws SimulationException {
		clock = (ClockSimulator)containerServices.getComponent(Container.CLOCK);
		sExec = new ArrayList ();
		logger.info(instanceName + ".initialized");
	}

	public void execute() throws SimulationException {
		SimulationInput input = (SimulationInput)containerServices.getComponent(Container.SIMULATION_INPUT);
		initialLoad(input);
		logger.info(instanceName + ".execute complete");
	}

	// Initial load.
	
	private void initialLoad(SimulationInput data) {
		loadPolicy(data);
		loadSB(data);
		loadProgram(data);
		loadProject(data);
		initialLoad = true;
	}
	
	private void loadProject(SimulationInput data) {
		Project[] x = data.getProject();
		for (int i =0; i < x.length; ++i) {
			x[i].setTimeOfCreation(clock.getDateTime());
			x[i].setTimeOfUpdate(clock.getDateTime());
			x[i].setMemberLink();
		}
		this.project = x;
		logger.info("Archive.load Project data loaded.");
	}

	private void loadProgram(SimulationInput data) {
		Program[] x = data.getProgram();
		for (int i =0; i < x.length; ++i) {
			x[i].setTimeOfCreation(clock.getDateTime());
			x[i].setTimeOfUpdate(clock.getDateTime());
		}
		this.set = x;
		logger.info("Archive.load ObsProgram data loaded.");
	}

	private void loadSB(SimulationInput data) {
		SB[] x = data.getSB();
		for (int i =0; i < x.length; ++i) {
			x[i].setTimeOfCreation(clock.getDateTime());
			x[i].setTimeOfUpdate(clock.getDateTime());
		}
		this.unit = x;
		logger.info("Archive.load Scheduling block data loaded.");
	}

	private void loadPolicy(SimulationInput data) {
		Policy[] x = data.getPolicy();
		for (int i =0; i < x.length; ++i) {
			x[i].setId(containerServices.getEntityId());
			x[i].setTimeOfCreation(clock.getDateTime());
			x[i].setTimeOfUpdate(clock.getDateTime());
		}
		this.policy = x;
		logger.info("Archive.load Scheduling policy data loaded.");
	}

	// Projects.

	public Project[] getAllProject() throws SchedulingException {
		return this.project;
	}

	public Project[] getNewProject(DateTime t) throws SchedulingException {
		return new Project [0];
	}

	public Project getProject(String id) throws SchedulingException {
		for (int i = 0; i < project.length; ++i) {
			if (project[i].getId().equals(id))
				return project[i];
		}
		String message = "Archive.notFound: Project " + id + " was not found.";
		logger.fine(message);
		throw new SchedulingException(message);
	}

	public void updateProject(Project p) throws SchedulingException {
		for (int i = 0; i < project.length; ++i) {
			if (project[i] == p) {
				return;
			}
		}
		String message = "Archive.notFound: Project " + p.getId() + " was not found.";
		logger.fine(message);
		throw new SchedulingException(message);
	}

	// Program
	
	public Program getProgram(String id) throws SchedulingException {
		for (int i = 0; i < set.length; ++i) {
			if (set[i].getId().equals(id))
				return set[i];
		}
		String message = "Archive.notFound: Program " + id + " was not found.";
		logger.fine(message);
		throw new SchedulingException(message);
	}

	public void updateProgram(Program s) throws SchedulingException {
		for (int i = 0; i < set.length; ++i) {
			if (set[i] == s) {
				return;
			}
		}
		String message = "Archive.notFound: Program " + s.getId() + " was not found.";
		logger.fine(message);
		throw new SchedulingException(message);
	}

	// SBs.
	
	public SB[] getAllSB() throws SchedulingException {
		return this.unit;
	}

	public SB[] getNewSB(DateTime t) throws SchedulingException {
		return new SB [0];
	}

	public SB getSB(String id) throws SchedulingException {
		for (int i = 0; i < unit.length; ++i) {
			if (unit[i].getId().equals(id))
			return unit[i];
		}
		String message = "Archive.notFound: Scheduling block " + id + " was not found.";
		logger.fine(message);
		throw new SchedulingException(message);
	}

	public void updateSB(SB s) throws SchedulingException {
		for (int i = 0; i < unit.length; ++i) {
			if (unit[i] == s) {
				return;
			}
		}
		String message = "Archive.notFound: Scheduling block " + s.getId() + " was not found.";
		logger.fine(message);
		throw new SchedulingException(message);
	}

	// Policies.
	
	public Policy[] getPolicy() throws SchedulingException {
		return policy;
	}
	
	// ExecBlock.
	
	public void newExec(ExecBlock x) throws SchedulingException {
			//??x.setId(containerServices.getEntityId());
			x.setTimeOfCreation(clock.getDateTime());
			x.setTimeOfUpdate(clock.getDateTime());
			this.sExec.add(x);
	}

	public void updateExec(ExecBlock x) throws SchedulingException {
			x.setTimeOfUpdate(clock.getDateTime());
	}

	public ExecBlock[] getAllExec() throws SchedulingException {
		ExecBlock[] x = new ExecBlock [sExec.size()];
		x = (ExecBlock[])sExec.toArray(x);
		return x;
	}
	
	public ExecBlock getExec(String id) throws SchedulingException {
		ExecBlock x = null;
		for (int i = 0; i < sExec.size(); ++i) {
			x = (ExecBlock)sExec.get(i);
			if (x.getId().equals(id))
				return x;
		}
		String message = "Archive.notFound: SExec " + id + " was not found.";
		logger.fine(message);
		throw new SchedulingException(message);
	}

	/**
	 * Get all ExecBlocks that belong to the specified SB.  
	 */
	public ExecBlock[] getExec(SB u) throws SchedulingException {
		if (u == null) {
			throw new IllegalArgumentException ("SB cannot be null!");
		}
		//String[] id = u.getExec();
		//ExecBlock[] x = new ExecBlock [id.length];
        ExecBlock[] x = u.getExec();
        /*
		for (int i = 0; i <x.length; ++i) {
			x[i] = getExec(id[i]);
		}*/
		return x;
	}
	
    public void storePipelineProcessingRequest(SciPipelineRequest ppr) {}
    public String storeSession(Session s) { return null; }
    public void updateSession(String id) {}
    public Project checkProjectForUpdates(String id) throws SchedulingException{
        return null;
    }
    
}
