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
 
package alma.scheduling.planning_mode_sim.simulator;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.io.IOException;
import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;

import alma.scheduling.planning_mode_sim.define.Expression;
import alma.scheduling.planning_mode_sim.define.DateTime;
import alma.scheduling.planning_mode_sim.define.SiteCharacteristics;
import alma.scheduling.planning_mode_sim.define.acs.container.ContainerException;
import alma.scheduling.planning_mode_sim.define.acs.component.ComponentLifecycleException;
import alma.scheduling.planning_mode_sim.master_scheduler.MasterScheduler;

/**
 * The Simulator class gathers and verifies all input data to the simulation
 * and controls the running of the simulation. 
 * 
 * @version 1.00  May 21, 2003
 * @author Allen Farris
 */
public class Simulator implements Runnable {

	static private final double radToDeg = 180.0 / Math.PI;
	static private final double radToHour =  12.0 / Math.PI;
	static private final double degToRad = Math.PI / 180.0;
	static private final double hourToRad =  Math.PI / 12.0;
	
	/**
	 * The container that controls the components.
	 */
	private Container container;

	/**
	 * The logger used by the simulator.
	 */
	private Logger log;
	
	/**
	 * The file handler used by the logger.
	 */
	private FileHandler logfile;

	/**
	 * The simulation input data.
	 */
	private SimulationInput data;

	/**
	 * The directory in which the input, log, and output are located.
	 */
	private String directory;
	/**
	 * The input file.
	 */
	private File inputFile;
	/**
	 * The log file.
	 */
	private File logFile;
	/**
	 * The name of the output file.
	 */
	private File outFile;
		
	/**
	 * Report a severe error to the log and throw an exception.
	 * @param message
	 * @throws SimulationException
	 */
	private void error(String message) throws SimulationException {
		log.severe("Simulator.error " + message);
		throw new SimulationException("Simulator", Level.SEVERE, message);
	}

	/**
	 * Create a simulator.
	 * 
	 * @param inputFilename The name of the file containing the input data to the simulation.
	 * @param logFilename The name of the output log file.
	 */
	public Simulator(String directory, String inputFilename, String outputFilename, String logFilename) {
		this.directory = directory;
		inputFile = new File (directory,inputFilename);
		logFile = new File (directory,logFilename);
		outFile = new File (directory,outputFilename);
		try {
			
			// Make sure the input file exists, and
			// create the logger and the log file.
			log = Logger.getLogger("Scheduling.Simulator");
			try {
				if (!inputFile.exists()) {
					System.out.println("There is no such input file as " + inputFile.getAbsolutePath());
					System.exit(0);
				}
				logfile = new FileHandler(logFile.getAbsolutePath());
			} catch (IOException ioerr) {
				 ioerr.printStackTrace();
				 System.exit(0);
			}
			
			// The log file will be a text file.
			SimpleFormatter sformatter = new SimpleFormatter ();
			logfile.setFormatter(sformatter);
			log.setLevel(Level.CONFIG);
			log.addHandler(logfile);
		
			// Create the SimulationInput object.
			data = new SimulationInput(inputFile.getAbsolutePath(),log);
			
			// Check the mode.  We're only doing PLANNING for now.
			if (data.getMode() != Mode.PLANNING) {
				System.out.println("The R1 simulator only runs in the PLANNING mode.");
				System.exit(0);
			}
					
			// Create the container.
			container = new Container(data,log);
			
			// OK, we're done for now.  Record the basic data in the log.
			DateTime t = new DateTime ();
			t.add((int)(System.currentTimeMillis() / 1000));
			log.info(Tag.title + ": Scheduling simulation run at " + t + ".");
			log.info(Tag.mode + ": " + data.getMode());
			log.info(Tag.logLevel + ": " + data.getMimimumLogLevel());
			log.info(Tag.beginTime + ": " + data.getBegin());
			log.info(Tag.endTime + ": " + data.getEnd());
			SiteCharacteristics site = data.getSite();
			log.info(Tag.longitude + ": " + site.getLongitude());
			log.info(Tag.latitude + ": " + site.getLatitude());
			log.info(Tag.altitude + ": " + site.getAltitude());
			log.info(Tag.timeZone + ": " + site.getTimeZone());
			log.info(Tag.minimumElevationAngle + ": " + site.getMinimumElevationAngle());
			log.info(Tag.numberAntennas + ": " + site.getNumberAntennas());

		} catch (SimulationException err) {
			err.printStackTrace();
			System.exit(0);
		}
	}

	public void run() {
		
		// The components we will need.
		ArchiveSimulator archive = null;
		ControlSimulator control = null;
		ClockSimulator clock = null;
		MasterScheduler masterScheduler = null;
		WeatherModel weather = null;
		
		// Create the output file.
		PrintStream out = null;
		try {
			// Create the output text file.
			out = new PrintStream (new FileOutputStream (outFile));
		} catch (IOException ioerr) {
			 ioerr.printStackTrace();
			 System.exit(0);
		}
		// Set the output file in the simlation input object.
		data.setOut(out);

		// Create and initialize the components.
		DateTime beginCivil = null;
		DateTime endCivil = null;
		try {
			
			// Activate the clock.
			clock = (ClockSimulator)container.getComponent(Container.CLOCK);
			clock.initialize();
			clock.execute();
			
			// Configure the DateTime coordinates.
			DateTime.setClockCoordinates(clock.getLongitudeInDegrees(),clock.getLatitudeInDegrees(),clock.getTimeZone());

			// Set the beginning and ending time.
			data.setBeginEndTime(); // These times are in local civil time.
			
			// We want to make the clock an LST clock.
			// Form the beginning time.
			beginCivil = data.getBegin();
			endCivil = data.getEnd();
			// Compute the corresponding LST in hours.
			double lstHrs = beginCivil.getLocalSiderealTime();
			// Form the DateTime for the LST.
			DateTime lstBegin = DateTime.add(beginCivil,(lstHrs / 24.0));
			clock.setTime(lstBegin);
			// This clock is now an LST clock.
			
			// Now we need to reset the beginning and ending times to LST times.
			DateTime end = data.getEnd();
			lstHrs = end.getLocalSiderealTime();
			DateTime lstEnd = DateTime.add(end,(lstHrs / 24.0));
			data.setBegin(lstBegin);
			data.setEnd(lstEnd);
					
			
			// Activate the weather model.
			weather = (WeatherModel)container.getComponent(Container.WEATHER_MODEL);
			weather.initialize();
			weather.execute();
			
			// Configure the Expression class.
			Expression.setFunctionNames(weather.getFunctionNames());
			Expression.setMethods(weather.getObjects());
			
			// Set the Source data.
			data.setSourceData();
			
			// Activate the archive.
			archive = (ArchiveSimulator)container.getComponent(Container.ARCHIVE);
			archive.initialize();
			archive.initialLoad();
			archive.execute();
			
			// Activate control.
			control = (ControlSimulator)container.getComponent(Container.CONTROL);
			control.initialize();
			control.execute();
			
			// Activate the MasterScheduler;
			masterScheduler = (MasterScheduler)container.getComponent(Container.MASTER_SCHEDULER);
			masterScheduler.initialize();
			masterScheduler.execute();
		
		} catch (ContainerException err) {
			log.severe("Simulator.error " + err.toString());
			System.out.println(err.toString());
			System.exit(0);
		} catch (ComponentLifecycleException err) {
			log.severe("Simulator.error " + err.toString());
			System.out.println(err.toString());
			System.exit(0);
		} catch (SimulationException err) {
			log.severe("Simulator.error " + err.toString());
			System.out.println(err.toString());
			System.exit(0);
		}		
		System.out.println(">>> Components have been created, initialized, and are ready.");
		

		// Write the initial data in the output file.
		SiteCharacteristics site = data.getSite();
		out.println("ALMA Simulator Release R1 - October 7, 2003");
		out.println("Beginning simulation run.  System time: " + DateTime.currentSystemTime());
		out.println("Input properties from file: " + inputFile.getAbsolutePath());
		out.println("Site: " + site.getLongitude() * radToDeg + " deg longitude " + 
					site.getLatitude() * radToDeg + " deg latitude");
		out.println("Number of antennas: " + site.getNumberAntennas());
		out.println("Starting time: " + beginCivil);
		out.println("Ending time: " + endCivil);
		out.println("All subsequent times are LST");
		out.println("Starting time: " + data.getBegin() + " LST");
		out.println("Ending time: " + data.getEnd() + " LST");
		out.println();
		out.println("Schedule");
		out.println();

		// Run the simulation.
		masterScheduler.runSimulation();
		
	}
	
	/**
	 * @return
	 */
	public Container getContainer() {
		return container;
	}

	/**
	 * @return
	 */
	public SimulationInput getData() {
		return data;
	}

	/**
	 * @return
	 */
	public Logger getLog() {
		return log;
	}

	/**
	 * @return
	 */
	public FileHandler getLogfile() {
		return logfile;
	}

}
