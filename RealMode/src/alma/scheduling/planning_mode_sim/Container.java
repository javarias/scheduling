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
 * File Container.java
 */
 
package alma.scheduling.planning_mode_sim;

import alma.scheduling.planning_mode_sim.define.ContainerServices;
import alma.scheduling.planning_mode_sim.master_scheduler.MasterScheduler;

import java.util.logging.Logger;

/**
 * The Container class implements a very simple container concept for use
 * within the plamming mode simulator.  It manages only a handful of components.
 * The Simulator class is in charge of initializing and executing these
 * components within the simulator. 
 * 
 * @version 1.10  December 12, 2003
 * @author Allen Farris
 */
public class Container implements ContainerServices {

	// These strings are the names by which the various components are known.
	static public final String REPORTER = 			"reporter";
	static public final String SIMULATION_INPUT = 	"input";
	static public final String CLOCK = 				"clock";
	static public final String WEATHER_MODEL = 		"weatherModel";
	static public final String MASTER_SCHEDULER = 	"master_scheduler";
	static public final String CONTROL = 			"control";
	static public final String ARCHIVE = 			"archive";
	static public final String TELESCOPE = 			"telescope";
	static public final String OPERATOR = 			"operator";
	static public final String PROJECT_MANAGER = 	"projectManager";

	// This static id-generator function is used to implement the getEntityId method.
	private static int idGenerator = 0;
	private static String generateId() {
		++idGenerator;
		return Integer.toString(idGenerator);
	}

	// The components.
	private Reporter reporter;
	private SimulationInput input;	
	private ClockSimulator clock;
	private WeatherModel weatherModel;
	private MasterScheduler masterScheduler;
	private ControlSimulator control;
	private ArchiveSimulator archive;
	private TelescopeSimulator telescope;
	private OperatorSimulator operator;
	private ProjectManagerSimulator projectManager;
	
	
	// The Logger.
	private Logger logger;
	
	/**
	 * Construct a container.
	 * @param logger The Java logger associated witb this container.
	 */
	public Container(Logger logger) {
		this.logger = logger;
		
		// Create the components.
		reporter = new Reporter ();
		input = new SimulationInput();
		clock = new ClockSimulator ();
		weatherModel = new WeatherModel();
		masterScheduler = new MasterScheduler ();
		control = new ControlSimulator ();
		archive = new ArchiveSimulator ();
		telescope = new TelescopeSimulator ();
		operator = new OperatorSimulator();
		projectManager = new ProjectManagerSimulator();
		logger.info("Components have been constructed.");
		
		// Set the component names.
		reporter.setComponentName(REPORTER);
		input.setComponentName(SIMULATION_INPUT);
		clock.setComponentName(CLOCK);
		weatherModel.setComponentName(WEATHER_MODEL);
		masterScheduler.setComponentName(MASTER_SCHEDULER);
		control.setComponentName(CONTROL);
		archive.setComponentName(ARCHIVE);
		telescope.setComponentName(TELESCOPE);
		operator.setComponentName(OPERATOR);
		projectManager.setComponentName(PROJECT_MANAGER);
		
		// Set the component's container services.
		reporter.setContainerServices(this);
		input.setContainerServices(this);
		clock.setContainerServices(this);
		weatherModel.setContainerServices(this);
		masterScheduler.setContainerServices(this);
		control.setContainerServices(this);
		archive.setContainerServices(this);
		telescope.setContainerServices(this);
		operator.setContainerServices(this);
		projectManager.setContainerServices(this);
		logger.info("Component names and services have been set.");
			
	}

	/** 
	 * Get a unique id that can tag an object in the archive.
	 * @return String
	 */
	public String getEntityId() {
		return generateId();
	}
	

	/** 
	 * Get the Logger.
	 * @return Logger
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * Get the component with the specified name.
	 * @return the component as a Java object.
	 */
	public Object getComponent(String name) {
		if (name.equals(REPORTER))
			return reporter;
		else if (name.equals(SIMULATION_INPUT))
			return input;
		else if (name.equals(CLOCK))
			return clock;
		else if (name.equals(WEATHER_MODEL))
			return weatherModel;
		else if (name.equals(MASTER_SCHEDULER))
			return masterScheduler;
		else if (name.equals(CONTROL))
			return control;
		else if (name.equals(ARCHIVE))
			return archive;
		else if (name.equals(TELESCOPE))
			return telescope;
		else if (name.equals(OPERATOR))
			return operator;
		else if (name.equals(PROJECT_MANAGER))
			return projectManager;
		return null;
	}

}
