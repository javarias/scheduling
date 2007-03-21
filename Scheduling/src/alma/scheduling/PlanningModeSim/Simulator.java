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
 * File Simulator.java
 */
 
package alma.scheduling.PlanningModeSim;

import alma.scheduling.PlanningModeSim.MasterScheduler.MasterScheduler;
import alma.scheduling.PlanningModeSim.Define.SimulationException;

import alma.scheduling.Define.*;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.File;

/**
 * The Simulator class gathers and verifies all input data to the simulation
 * and controls the running of the simulation. 
 * 
 * @version 1.10  Dec. 12, 2003
 * @author Allen Farris
 */
public class Simulator implements Runnable {

	/**
	 * The logger used by the simulator.
	 */
	private Logger logger;
	
	/**
	 * The container that controls the components.
	 */
	private Container container;

    private String outputResultsFilename;

	/**
	 * Report a severe error to the log and throw an exception.
	 * @param message
	 * @throws SimulationException
	 */
	private void error(String message) throws SimulationException {
		logger.severe("Simulator.error " + message);
		throw new SimulationException("Simulator", Level.SEVERE, message);
	}

	/**
	 * Create a simulator.
	 */
	public Simulator() {
	}
	
	/**
	 * Initialize the simulator.
	 * 
	 * @param directory The directory in which the input, output and log files reside.
	 * @param inputFilename The name of the file containing the input data to the simulation.
	 * @param outputFilename The name of the file containing the input data to the simulation.
	 * @param logFilename The name of the output log file.
	 */
	public void initialize(String directory, String inputFilename, String outputFilename, String logFilename) 
		throws SimulationException {
		File inputFile = new File (directory,inputFilename);
		File logFile = new File (directory,logFilename);
		File outFile = new File (directory,outputFilename);
        File graphFile = new File(directory, "graph_"+outputFilename);
        File statsFile = new File(directory, "stats_"+outputFilename);
			
		// Make sure the input file exists.
		if (!inputFile.exists()) {
			String s = "There is no such input file as " + inputFile.getAbsolutePath();
			System.out.println(s);
			throw new SimulationException("Simulator", Level.SEVERE, s);
		}
		// Create the logger and the log file.
		FileHandler logText = null;
		this.logger = Logger.getLogger("Scheduling.Simulator");
		try {
			logText = new FileHandler(logFile.getAbsolutePath());
		} catch (IOException ioerr) {
			error(ioerr.toString());
		}
		
		// The log file will be a text file.
		SimpleFormatter sformatter = new SimpleFormatter ();
		logText.setFormatter(sformatter);
		logger.setLevel(Level.CONFIG);
		logger.addHandler(logText);
		
		logger.info("Simulator:  Log file created.");
	
		// Create the container.
		container = new Container(logger);
		logger.info("Simulator:  Container created.");
		
		// Create the components
		
		Reporter reporter = (Reporter)container.getComponent(Container.REPORTER);
		SimulationInput input = (SimulationInput)container.getComponent(Container.SIMULATION_INPUT);
		TelescopeSimulator telescope = (TelescopeSimulator)container.getComponent(Container.TELESCOPE);
		OperatorSimulator operator = (OperatorSimulator)container.getComponent(Container.OPERATOR);
		ClockSimulator clock = (ClockSimulator)container.getComponent(Container.CLOCK);
		WeatherModel weatherModel = (WeatherModel)container.getComponent(Container.WEATHER_MODEL);
		MasterScheduler masterScheduler = (MasterScheduler)container.getComponent(Container.MASTER_SCHEDULER);
		ControlSimulator control = (ControlSimulator)container.getComponent(Container.CONTROL);
		ArchiveSimulator archive = (ArchiveSimulator)container.getComponent(Container.ARCHIVE);
		ProjectManagerSimulator projectManager = (ProjectManagerSimulator)container.getComponent(Container.PROJECT_MANAGER);
		
		// Set the filename for the simulator input.
		input.setFiles(inputFile, outFile, logFile, graphFile, statsFile);
		
		// Initialize the components
		
		// Reporter.initialize: nothing
		reporter.initialize();
		// SimulationInput.initialize: build properties, get basic input data
		input.initialize();
		// TelescopeSimulator: set site characteristics and create list of antennas.
		telescope.initialize();
		// OperatorSimulator: get telescope.
		operator.initialize();
		// ClockSimulator.initialize: set coordinates
		clock.initialize();
		// WeatherModel.initialize: get parameters from input data
		weatherModel.initialize();
		// ArchiveSimulator.initialize: create empty archive
		archive.initialize();
		// ControlSimulator.initialize: get clock and other components
		control.initialize();
		// MasterScheduler.initialize: nothing
		masterScheduler.initialize();
		// ???
		projectManager.initialize();
		logger.info("Simulator:  All components initialized.");

		// The following are housekeeping chores that must be done prior to executing the components.

		// Set the coordinates of the clock in the DateTime class.
		DateTime.setClockCoordinates(clock.getLongitudeInDegrees(),clock.getLatitudeInDegrees(),clock.getTimeZone());

		// Get the beginning and ending local civil time.
		input.setCivilTime();
		
		// Change the clock to an LST clock and set the beginning and ending times.
		// Form the beginning time.
		DateTime beginCivil = input.getBeginCivilTime();
        System.out.println("CIVIL TIME: "+beginCivil.toString());
		// Compute the corresponding LST in hours.
		double lstHrs = beginCivil.getLocalSiderealTime();
        System.out.println("LST hours: "+lstHrs);
		// Form the DateTime for the LST.
		DateTime lstBegin = DateTime.add(beginCivil,(lstHrs / 24.0));
        System.out.println("LST begin TIME: "+lstBegin.toString());
		clock.setTime(lstBegin);
        //System.out.println("@@@@@@@@@@@@@@@@@@@@@@");
        //System.out.println(clock.getDateTime());
		// This clock is now an LST clock.
		// Now we need to set the beginning and ending times to LST times.
        //clock.setTime(beginCivil);
		DateTime endCivil = input.getEndCivilTime();
		lstHrs = endCivil.getLocalSiderealTime();
		DateTime lstEnd = DateTime.add(endCivil,(lstHrs / 24.0));
		input.setBeginTime(lstBegin);
		input.setEndTime(lstEnd);

		// Configure the Expression class.
		//Expression.setFunctionNames(weatherModel.getFunctionNames());
		//Expression.setMethods(weatherModel.getObjects());
		PreConditions.setFunctionNames(weatherModel.getFunctionNames());
		PreConditions.setMethods(weatherModel.getObjects());

		// Execute the components.

		// Reporter.execute: nothing
		reporter.execute();
		// SimulationInput.execute: build project and policy data
		input.execute();
		// TelescopeSimulator.execute: nothing
		telescope.execute();
		// OperatorSimulator: nothing.
		operator.execute();
		// WeatherModel.execute:
		weatherModel.execute();
		// ClockSimulator.execute:
		clock.execute(); 
		// ArchiveSimulator.execute: load initial data
		archive.execute();
		// ControlSimulator.execute: nothing
		control.execute();
		// MasterScheduler.execute: get other components and scheduling policy
		masterScheduler.execute();
		// ...
		projectManager.execute();
		// TODO Add a validation step that verifies the values of all input data.
		
		// Ok, everything is ready.
		logger.info("Simulator:  All components ready.");
	}

	public void run() {
		try {
			
			Reporter reporter = (Reporter)container.getComponent(Container.REPORTER);
			MasterScheduler masterScheduler = (MasterScheduler)container.getComponent(Container.MASTER_SCHEDULER);

			ClockSimulator clock = (ClockSimulator)container.getComponent(Container.CLOCK);
			
			// Let the reporter know we are starting.
			reporter.schedulingIsBeginning(clock.getDateTime());
			
			// Run the simulation.
			masterScheduler.runSimulation();
			
			// Let the reporter know we have finished.
			reporter.schedulingIsComplete(clock.getDateTime(),0,"");
			
            outputResultsFilename = reporter.getOutputFilename();
		} catch (Exception err) {
			err.printStackTrace(System.out);
			System.out.println("The simulation run has aborted.");
		}
	}

    public String getReportFilename() {
        return outputResultsFilename;
    }

}
