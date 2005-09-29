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
 * File SimulationInput.java
 */
package alma.scheduling.PlanningModeSim;

import alma.scheduling.PlanningModeSim.Define.ComponentLifecycle;
import alma.scheduling.PlanningModeSim.Define.ContainerServices;
import alma.scheduling.PlanningModeSim.Define.SimulationException;

import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.FrequencyBand;
import alma.scheduling.Define.SiteCharacteristics;
import alma.scheduling.Define.Project;
import alma.scheduling.Define.Program;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.Policy;
import alma.scheduling.Define.PolicyFactor;
import alma.scheduling.Define.Priority;
import alma.scheduling.Define.WeatherCondition;
import alma.scheduling.Define.Target;
import alma.scheduling.Define.Equatorial;
import alma.scheduling.Define.Antenna;

import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * The SimulationInput class is a component that is an extension of 
 * the Properties class.  It is used to house all the input values 
 * in the form of a properties file.  This includes the simulation mode, 
 * minimum logging level, beginning and ending time, site characteristics,
 * as well as basic data for constructing projects and scheduling units.
 * In addition to being a list of properties, this class contains 
 * methods for getting strings, ints, doubles, and times from the 
 * list of properties, throwing exceptions if these are not found. 
 * This class takes no action itself.  It merely gather input data and 
 * provides services for other objects to extract parameters.
 * 
 * @version 1.10  Dec. 12, 2003
 * @author Allen Farris
 */
public class SimulationInput extends Properties implements ComponentLifecycle {
	
	/**
	 * The instance name of this component.
	 */
	private String instanceName;
	
	/**
	 * The container services provided by the container.
	 */
	private ContainerServices containerServices;
	
	/**
	 * The Java logger used in this simulation.
	 */
	private Logger logger;
	
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
	 * The simulation mode.
	 */
	private Mode mode;
	
	/**
	 * The mimimum logging level. 
	 */
	private Level mimimumLogLevel;

	/**
	 * The beginning local, civil time of the simulation.
	 */	
	private DateTime beginCivilTime;
	
	/**
	 * The ending local, civil time of the simulation.
	 */	
	private DateTime endCivilTime;
	
	/**
	 * The beginning time of the simulation which may not be the same as the civil time.
	 */	
	private DateTime beginTime;
	
	/**
	 * The ending time of the simulation which may not be the same as the civil time.
	 */	
	private DateTime endTime;
	
	/**
	 * The site characteristics.
	 */
	private SiteCharacteristics site;

	/**
	 * The frequency bands associated with this simulation.
	 */
	private FrequencyBand[] band;

	/**
	 * The antennas associated with this simulation
	 */
	private Antenna[] antenna;
	
	/**
	 * The projects as constructed from the input properties file.
	 */
	private Project[] project;

	/**
	 * The unit sets as constructed from the input properties file.
	 */
	private Program[] set;

	/**
	 * The units as constructed from the input properties file.
	 */
	private SB[] unit;

	/**
	 * The scheduling policies as constructed from the input properties file.
	 */
	private Policy[] policy;
	
	/**
	 * The time in seconds that is required to change to a different
	 * observing setup, for example to change frequency bands.
	 */
	private int setUpTimeInSec;
	
	/**
	 * The time in seconds that is required to change from one project to a
	 * new project.
	 */
	private int changeProjectTimeInSec;
	
	/**
	 * The time in seconds to advance the clock when nothing can be scheduled.
	 */
	private int advanceClock;
	
	/**
	 * Create a SimulationInput object.
	 */
	public SimulationInput() {
	}

	/* 
	 * Set this component's name -- done by the container.
	 */
	public void setComponentName(String instanceName)  {
		if (instanceName == null || instanceName.length() == 0)
			throw new IllegalArgumentException (
				"Component name cannot be a null string.");
		if (this.instanceName != null)
			throw new UnsupportedOperationException (
				"Cannot change the name of a component that has already been named.");
		this.instanceName = instanceName;
	}

	/* 
	 * Set the object that provides container services -- done by the container.
	 */
	public void setContainerServices(ContainerServices containerServices) {
		if (containerServices == null)
			throw new IllegalArgumentException (
				"ContainerServices object cannot be a null.");
		if (this.containerServices != null)
			throw new UnsupportedOperationException (
				"Cannot change container services that have already been set.");
		this.containerServices = containerServices;
		this.logger = this.containerServices.getLogger();
	}

	/**
	 * Set the filename to be used in loading the properties files -- done by the Simulator.
	 * @param filename
	 */
	public void setFiles (File in, File out, File log) {
		this.inputFile = in;
		this.outFile = out;
		this.logFile = log;
	} 
	
	public void initialize() throws SimulationException {
		// 0. filename, container services and component name must be set.
		if (inputFile == null)
			error("Cannot initialize component!  Filename has not been set.");
		if (instanceName == null || containerServices == null)
			error("Cannot initialize component!  Either instanceName or containerServices has not been set.");
		
		// 1. load properties file
		try {
			logger.info("SimulationInput.filename Using file " + inputFile.getAbsolutePath() + " for simultation data.");
			FileInputStream file = new FileInputStream(inputFile);
			super.load(file);
			logger.fine("SimulationInput.filename " + inputFile.getAbsolutePath() + " loaded.");
		} catch (IOException err) {
			error("Could not open file " + inputFile.getAbsolutePath());
		}
		
		// 2. get simulation mode
		String s = getString(Tag.mode);
		if (s.equals("FULL"))
			mode = Mode.FULL;
		else if (s.equals("PLANNING"))
			mode = Mode.PLANNING;
		else if (s.equals("CONFLICT"))
			mode = Mode.CONFLICT;
		else
			error("Invalid value (" + s + ") for Simulation.mode."); 

		
		// 3. get mimimum logging level
		s = getString(Tag.logLevel);
		if (s.equals("CONFIG"))
			mimimumLogLevel = Level.CONFIG;
		else if (s.equals("FINE"))
			mimimumLogLevel = Level.FINE;
		else if (s.equals("FINER"))
			mimimumLogLevel = Level.FINER;
		else if (s.equals("FINEST"))
			mimimumLogLevel = Level.FINEST;
		else
			error("Invalid value (" + s + ") for Simulation.logLevel.");
		
		// 4. Get the frequency bands from the input.
		// Syntax:
		// 		FrequencyBand.numberOfBands = N
		//		FrequencyBand.<i> = nameOfBand; minimumFrequency; maximumFrequency
		// where 0 <= i < N, and N is the number of frequency bands. Frequencies are in GHz.
		band = new FrequencyBand [getInt(Tag.numberOfBands)];
		String value = null;
		String name = null;
		double min = 0.0;
		double max = 0.0;
		String[] x = null;
		for (int i = 0; i < band.length; ++i) {
			value = getString(Tag.band + "." + i);
			x = value.split(";",-1);
			if (x.length < 3) {
				error("Invalid number of frequency band parameters in " + value);
			}
			try {
				name = x[0].trim();
				min = Double.parseDouble(x[1]);
				max = Double.parseDouble(x[2]);
			} catch (NumberFormatException err) {
				error("Invalid number format in " + value);
			}
			band[i] = new FrequencyBand(name,min,max);
		}
		
		// 5. get site characteristics
		// Create a SiteCharacteristics object from a simulation data object.
		// The structure of a property file is given below.
		// 
		// 	# Site longitude in degrees.
		// 	Site.longitude = 107.6177275;
		// 	# Site latitude in degrees.
		// 	Site.latitude = 34.0787491666667
		//  # The time zone of the site.
		//  Site.timeZone = -6
		// 	# Site altitude in meters.
		// 	Site.altitude = 2124.0
		// 	# Site minimum elevation angle above the horizon.
		// 	Site.minimumElevationAngle = 8.0
		//	# Number of antennas.
		// 	Site.numberAntennas = 64
		double longitude = getDouble(Tag.longitude);
		double latitude = getDouble(Tag.latitude);
		int timeZone = getInt(Tag.timeZone);
		double altitude = getDouble(Tag.altitude);
		double minimumElevationAngle = getDouble(Tag.minimumElevationAngle);
		int numberAntennas = getInt(Tag.numberAntennas);
		if (numberAntennas < 1)
			error("Number of antennas cannot be less than 1.");

		site = new SiteCharacteristics (longitude, latitude, timeZone, 
					altitude, minimumElevationAngle, numberAntennas, band);
		// Get the Antenna configuration
        antenna = new Antenna[numberAntennas];
        String antDesc = null;
        String antName = null;
        String antPad = null;
        int antLoc = -1;
        boolean hasNutator = false;
        String[] vals = null;
        for(int i=0; i < numberAntennas; i++){
            antDesc = getString(Tag.antenna +"."+ i);
            vals = antDesc.split(";", -1);
            if(vals.length < 4) {
				error("Invalid number of antenna config parameters in " + antDesc);
			}
            try {
                antName = vals[0].trim();
                antLoc = Integer.parseInt(vals[1].trim());
                antPad = vals[2].trim();
                hasNutator = Boolean.getBoolean(vals[3].trim());
            } catch(Exception e) {
                error("Invalid format for antenna.");
            }
            antenna[i] = new Antenna(antName, antLoc, antPad, hasNutator);
        }
		// 6. Get setup time parameter -- 
		// The time in seconds to change to a different observing setup.
		setUpTimeInSec = getInt(Tag.setUpTime);
		
		// 7. get change project time parameter -- 
		// The time in seconds to change from one project to a new project.
		changeProjectTimeInSec = getInt(Tag.changeProjectTime);

		// 8. get advance the clock parameter -- 
		// The time in seconds to advance the clock when nothing can be scheduled.
		advanceClock = getInt(Tag.advanceClock);

		logger.info(instanceName + ".initialized");
	}

	public void execute() throws SimulationException {
		getSchedulingPolicy();
		getProjectSource();
		getProjectData();
		logger.info(instanceName + ".execute complete");
	}
	
	private void getProjectSource() throws SimulationException {
		String type = getString(Tag.projectSourceType);
		if (type.equals("JavaProperties")) {
			String projectFilename = getString(Tag.projectSource);
			int n = inputFile.getAbsolutePath().lastIndexOf(File.separator);
			String dir = inputFile.getAbsolutePath().substring(0,n);
			File projectFile = new File (dir,projectFilename);
			try {
				logger.info("SimulationInput.projectFilename Using file " + projectFile.getAbsoluteFile() + " for project data.");
				FileInputStream file = new FileInputStream(projectFile);
				super.load(file);
				logger.fine("SimulationInput.projectFilename " + projectFile.getAbsolutePath() + " loaded.");
			} catch (IOException err) {
				error("Could not open file " + projectFile.getAbsolutePath());
			}
		} else {
			error("ProjectSourceType must be JavaProperties at this time.");
		}
	}

	public void cleanUp() {
		logger.info(instanceName + ".stopped");
	}

	public void aboutToAbort() {
		cleanUp();
	}

	/**
	 * An internal method used in the event an error is found in the simulation input.
	 * A severe message is entered into the log and an exception is thrown.
	 * @param message The text of the error message.
	 * @throws SimulationException
	 */
	private void error(String message) throws SimulationException {
		logger.severe("SimulationInput.error " + message);
		throw new SimulationException("SimulationInput", Level.SEVERE, message);
	}

	/**
	 * Get a value of type String from a named property.
	 */
	public String getString(String name) throws SimulationException {
		String s = getProperty(name);
		if (s == null) {
			error("Property " + name + " was not found.");
		}
		logger.finer("SimulationInput: Got string property " + name + " value " + s);
		return s;
	}

	/**
	 * Get a value of type int from a named property.
	 */
	public int getInt(String name) throws SimulationException {
		String s = getProperty(name);
		if (s == null) {
			error("Property " + name + " was not found.");
		}
		logger.finer("SimulationInput: Got int property " + name + " value " + s);
		int n = 0;
		try {
			n = Integer.parseInt(s);
		} catch (NumberFormatException err) {
			error("Integer expected as value for " + name + " (" + s + " was found).");
		}
		return n;
	}

	/**
	 * Get a value of type int from a named property.
	 */
	public double getDouble(String name) throws SimulationException {
		String s = getProperty(name);
		if (s == null) {
			error("Property " + name + " was not found.");
		}
		logger.finer("SimulationInput: Got double property " + name + " value " + s);
		double d = 0.0;
		try {
			d = Double.parseDouble(s);
		} catch (NumberFormatException err) {
			error("Double expected as value for " + name + " (" + s + " was found).");
		}
		return d;
	}

	/**
	 * Get a value of type DateTime from a named property.  The time must be a
	 * FITS formatted string (except that the separating 'T' is optional).
	 */
	public DateTime getTime(String name) throws SimulationException {
		String s = getProperty(name);
		if (s == null) {
			error("Property " + name + " was not found.");
		}
		logger.finer("SimulationInput: Got time property " + name + " value " + s);
		DateTime t = null;
		try {
			t = new DateTime(s);
		} catch (IllegalArgumentException err) {
			error("Property " + name + ": " + err.toString());
		}
		return t;
	}

	public void getSchedulingPolicy() throws SimulationException {
		Policy[] x = new Policy [1];
		x[0] = new Policy ();
		//x[0].setName("R1Policy");
		x[0].setName("R3.0Policy");
		x[0].setVersion("V1.0");
		x[0].setDescription("R3 Release -- 1 Oct, 2005");
		x[0].setScoreCalculation("RankingCalculation * SuccessCalculation");
		x[0].setSuccessCalculation("(w0*positionElevation + w1*positionMaximum  + w2*weather) / (w0 + w1 + w2)");
			// Provided position > minimumElevation and weather > 0
		x[0].setRankingCalculation("w2 * priority + w3 * sameProjectSameBand + " +
			"w4 * sameProjectDifferentBand + w5 * differentProjectSameBand + " +
			"w6 * differentProjectDifferentBand + w7 * newProject + w8 * OneSBRemaining");
		PolicyFactor[] factor = new PolicyFactor [10];
		for (int i = 0; i < factor.length; ++i)
			factor[i] = new PolicyFactor ();
		factor[0].setName("positionElevation");
		factor[0].setDefinition("sin of the current elevation or 0.0 if the source is not visible, max = 1.0 min = 0.0");
		factor[1].setName("positionMaximum");
		factor[1].setDefinition("cos of the difference between the current elevation and the maximum elelvation or 0.0 if the source is not visible, max = 1.0 min = 0.0");
		factor[2].setName("weather");
		factor[2].setDefinition("weather expression evaluation, max = 1.0 min = 0.0");
		factor[3].setName("priority");
		factor[3].setDefinition("the scientific priority, value: 10/9/8/7/6/5/4/3/2/1");
		factor[4].setName("sameProjectSameBand");
		factor[4].setDefinition("SB belongs to same project and frequency band as current, value: 1/0");
		factor[5].setName("sameProjectDifferentBand");
		factor[5].setDefinition("SB belongs to same project as current but has a different frequency band, value: 1/0");
		factor[6].setName("differentProjectSameBand");
		factor[6].setDefinition("SB belongs to a different project but has same frequency band as current, value: 1/0");
		factor[7].setName("differentProjectDifferentBand");
		factor[7].setDefinition("SB belongs to a different project and has different frequency band from current, value: 1/0");
		factor[8].setName("newProject");
		factor[8].setDefinition("the cost of starting a new project, value: 1/0");
		factor[9].setName("oneSBRemaining");
		factor[9].setDefinition("only one SB remains in the project, value: 1/0");
		// Get the weights from the intput.
		factor[0].setWeight(getDouble(Tag.weightPositionElevation));
		factor[1].setWeight(getDouble(Tag.weightPositionMaximum));
		factor[2].setWeight(getDouble(Tag.weightWeather));
		factor[3].setWeight(getDouble(Tag.weightPriority));
		factor[4].setWeight(getDouble(Tag.weightSameProjectSameBand));
		factor[5].setWeight(getDouble(Tag.weightSameProjectDifferentBand));
		factor[6].setWeight(getDouble(Tag.weightDifferentProjectSameBand));
		factor[7].setWeight(getDouble(Tag.weightDifferentProjectDifferentBand));
		factor[8].setWeight(getDouble(Tag.weightNewProject));
		factor[9].setWeight(getDouble(Tag.weightOneSBRemaining));
		x[0].setFactor(factor);
		// All ranking factors cannot be 0.
		if (factor[3].getWeight() == 0.0 && factor[4].getWeight() == 0.0 && 
			factor[5].getWeight() == 0.0 && factor[6].getWeight() == 0.0 && 
			factor[7].getWeight() == 0.0 && factor[8].getWeight() == 0.0 && 
			factor[9].getWeight() == 0.0)
			error("All ranking factors cannot be 0");
		// All success factors cannot be 0.
		if (factor[0].getWeight() == 0.0 && factor[1].getWeight() == 0.0 && 
			factor[2].getWeight() == 0.0)
			error("All success factors cannot be 0");
		// All position factors cannot be 0.
		if (factor[0].getWeight() == 0.0 && factor[1].getWeight() == 0.0)
			error("All position factors cannot be 0");
		policy = x;
	}

	/**
	 * Get the source data from the input properties and construct projects,
	 * unit sets and units.
	 *
	 * The input data in the properties file is of the following form. 
	 * 		numberProjects = N
	 * 		project.<i> = projectName; PI; priority; numberTargets								
	 * 		target.<i>.<j> = targetName; ra; dec; frequency; total-time; weather-condition
	 * where 0 <= i < N and 0 <= j <= numberTargets-i.
	 * 
	 * As an example,
	 * 		numberProjects = 15
	 * 		project.0 = MyProject0; Allen Farris; high; 5
	 * 		target.0.0 = c312; 9.45; 54.9; 31.0; 30.0; excellent
	 * 		target.0.1 = c312; 9.45; 54.9; 31.0; 25.4; good
	 * 		target.0.2 = c312; 9.45; 54.9; 31.0; 28.2; average
	 * 		target.0.3 = c312; 9.45; 54.9; 31.0; 31.5; poor
	 * 		target.0.4 = c312; 9.45; 54.9; 31.0; 30.0; whatever
	 * 		project.1 = etc.
	 * 
	 * The units are as follows.
	 * 		ra 			hours
	 * 		dec			degrees
	 * 		frequency 	GHz
	 * 		total-time	minutes
	 * 
	 * Words enumerating priority are as follows.
	 * 		highest
	 * 		higher
	 * 		high
	 *		mediumPlus
	 * 		medium
	 * 		mediumMinus
	 * 		low
	 * 		lower
	 * 		lowest
	 * 		background
	 * 
	 * Weather condition words are: 
	 * 		words	   occurs in  	   #30min		quality
	 * 				   % of time       periods		function
	 * 		exceptional	 10 			4.8			0.9
	 * 		excellent	 20				9.6			0.8
	 * 		good		 40				19.2		0.7
	 * 		average		 60				28.8		0.6
	 * 		belowAverage 70				33.6		0.5
	 * 		poor		 80				38.4		0.4
	 * 		dismal		 90				43.2		0.3
	 * 		whatever	 100			48			0.2
	 * Make this into a weather quality sinusiodal function using the DiurnalModel.
	 * Maybe a "quality" function -> .1 is dismal and .9 is exceptional.
	 * 
	 * The following strategy is used in creating projects, unitsets, and units.
	 * 	1. Create one project for each project entry.
	 * 	2. Each target is assumed to be the center of a two-degree square box.
	 *  3. Each target is used to construct one scheduling unit.
	 * 	4. Each scheduling unit is placed in the project's program.
	 * 
	 * @throws SimulationException
	 */
	public void getProjectData() throws SimulationException {
		// The beginning and ending time of the simulation have been set by the simulator.
		
		// First, create the project array and get all the project value strings.
		// The form is: numberProjects = N
		// 				project.<i> = projectName; PI; priority; numberSets
		int n = getInt(Tag.numberProjects);
		project = new Project [n];
		String[] projectValue = new String [n];
		
		// We will accumulate all the unit sets and units in array lists.
		ArrayList setList = new ArrayList ();
		ArrayList unitList = new ArrayList ();
		
		String name = null;
		String pi = null;
		String priority = null;
		int numberSets = 0;
		String[] s = null;
		// Go through all the projects.
		for (int i = 0; i < project.length; ++i) {
			// Get the project data.
			projectValue[i] = getString(Tag.project + "." + i);
			// The data format is: projectName; PI; priority; numberTargets
			s = projectValue[i].split(";",-1);
			if (s.length < 4)
				error("Invalid number of project parameters: " + projectValue[i]);
			name = s[0].trim();
			pi = s[1].trim();
			priority = s[2].trim();
			try {
				numberSets = Integer.parseInt(s[3].trim());
			} catch (NumberFormatException err) {
				error("Invalid number format in " + s[3]);
			}
			if (numberSets < 0)
				error("Number of sets cannot be 0 in " + s);
			// Create the project.
			project[i] = new Project(name + "_project", name + "_proposal", name, "v01", pi);
				
			// Create the program and add it to the project.
			Program program = new Program (name + "_set_program");
			setList.add(program);
			project[i].setProgram(program);
			// Set the priority of the program.
			try {
				program.setScientificPriority(new Priority (priority));
			} catch (IllegalArgumentException err) {
				error(err.toString());
			}
			
			// Create the value array for the sets ...
			String[] setValue = new String [numberSets];
			// ... and, get the values for each of the Sets.
			for (int j = 0; j < numberSets; ++j) {
				setValue[j] = getString(Tag.set + "." + i + "." + j);
			}
			doSets(program,i,setValue,setList,unitList);
		}
		
		// Save the unit sets and the units.
		set = new Program [setList.size()];
		set = (Program[])setList.toArray(set);
		unit = new SB [unitList.size()];
		unit = (SB[])unitList.toArray(unit);
	}
	
	private void doSets(Program program, int projectNumber, String[] value, ArrayList setList, ArrayList unitList) 
		throws SimulationException {
		// Syntax is: setName; frequency-band; frequency; weather-condition; numberTargets
		// There are no optional parameters.
				
		String setName = null;
		String frequencyBand = null;
		double frequency = 0.0;
		String weatherCondition = null;
		int numberTargets = 0;
		WeatherCondition w = null;
		FrequencyBand b = null;
		
		// Now, get the data values.
		String[] s = null;
		Program set = null;
		for (int i = 0; i < value.length; ++i) {
			s = value[i].split(";",-1);
			if (s.length != 5)
				error("Invalid number of set parameters: " + value[i]);
			setName = s[0].trim();
			frequencyBand = s[1].trim();
			weatherCondition = s[3].trim();
			try {
				frequency = Double.parseDouble(s[2].trim());
				numberTargets = Integer.parseInt(s[4].trim());
			} catch (NumberFormatException err) {
				error("Invalid number format in " + value[i]);
			}

			// Validate frequency band.
			b = validateFrequencyBand(frequencyBand);
			
			// Validate frequency.
			validateFrequency(b,frequency);
			
			// Validate weather condition.
			w = validateWeatherCondition(weatherCondition);
			
			// Validate number of targets.
			if (numberTargets < 0)
				error("Number of targets cannot be 0 in " + value[i]);
			
			set = new Program (setName + "_set_" + i);
			// Set the scientific priority.
			set.setScientificPriority(program.getScientificPriority());
			// Set the frequency and frequency band.
			set.setCenterFrequency(frequency);
			set.setFrequencyBand(b);
			// Set the weather condition.
			set.setWeatherConstraint(w);
			
			// Add the set to the setList and the program.
			setList.add(set);
			program.addMember(set);
			
			// Do the targets.
			// Create the value array for the targets ...
			String[] targetValue = new String [numberTargets];
			// ... and, get the values for each of the targets.
			for (int j = 0; j < numberTargets; ++j) {
				targetValue[j] = getString(Tag.target + "." + projectNumber + "." + i + "." + j);
			}
			doTargets(set,weatherCondition,targetValue,unitList);
		}
	}
	
	private FrequencyBand validateFrequencyBand(String word)
		throws SimulationException {
		// We will need the frequency bands.
		FrequencyBand[] band = site.getBand();
		
		int i = 0;
		for (; i < band.length; ++i) {
			if (band[i].getName().equals(word))
				break;
		}
		if (i == band.length)
			error("Invalid frequency band name: " + word);
		return band[i];
	}
		
	private void validateFrequency(FrequencyBand band, double f) throws SimulationException {
		if (f < band.getLowFrequency() || f > band.getHighFrequency())
			error("Frequecncy " + f + " is outside the valid range of band " + band.getName());
	}
	
	private WeatherCondition validateWeatherCondition(String word)
		throws SimulationException {
		// Validate the weather word.
		WeatherCondition w = null;
		String[] cond = null;
		// Set the weather condition.
		if (word.equals("exceptional")) {
			cond = new String [3];
			cond[0] = "quality >= 0.9 -> 1.0";
			cond[1] = "quality >= 0.8 -> 0.8";
			cond[2] = "quality <  0.8 -> 0.0";
			w = new WeatherCondition (cond);
		} else if (word.equals("excellent")) {
			cond = new String [3];
			cond[0] = "quality >= 0.8 -> 1.0";
			cond[1] = "quality >= 0.7 -> 0.8";
			cond[2] = "quality <  0.7 -> 0.0";
			w = new WeatherCondition (cond);
		}
		else if (word.equals("good")) {
			cond = new String [3];
			cond[0] = "quality >= 0.7 -> 1.0";
			cond[1] = "quality >= 0.6 -> 0.8";
			cond[2] = "quality <  0.6 -> 0.0";
			w = new WeatherCondition (cond);
		}
		else if (word.equals("average")) {
			cond = new String [3];
			cond[0] = "quality >= 0.6 -> 1.0";
			cond[1] = "quality >= 0.5 -> 0.8";
			cond[2] = "quality <  0.5 -> 0.0";
			w = new WeatherCondition (cond);
		}
		else if (word.equals("belowAverage")) {
			cond = new String [3];
			cond[0] = "quality >= 0.5 -> 1.0";
			cond[1] = "quality >= 0.4 -> 0.8";
			cond[2] = "quality <  0.4 -> 0.0";
			w = new WeatherCondition (cond);
		}
		else if (word.equals("poor")) {
			cond = new String [3];
			cond[0] = "quality >= 0.4 -> 1.0";
			cond[1] = "quality >= 0.3 -> 0.8";
			cond[2] = "quality <  0.3 -> 0.0";
			w = new WeatherCondition (cond);
		}
		else if (word.equals("dismal")) {
			cond = new String [3];
			cond[0] = "quality >= 0.3 -> 1.0";
			cond[1] = "quality >= 0.2 -> 0.8";
			cond[2] = "quality <  0.2 -> 0.0";
			w = new WeatherCondition (cond);
		}
		else if (word.equals("any")) {
			cond = new String [1];
			cond[0] = "quality >= 0.0 -> 1.0";
			w = new WeatherCondition (cond);
		} else
			error("Invalid weather condition " + word);
		return w;
	}

	private void doTargets(Program set, String weatherWord, String[] value, ArrayList unitList) 
		throws SimulationException {
		// Syntax is: targetName; ra; dec; frequency; total-time; weather-condition; repeat-count; lstBegin; lstEnd 
		// The repeat-count, lstBegin, and lstEnd are all optional.
		// The default repeat-count is 0.
		// If lstBegin is specified, repeat-count and lstEnd must also be specified.
		// Repeat-count may be specified without lstBegin and lstEnd being specified. 
		// Frequency and weather-condition are optional; default comes from set.
		
		// If repeat-count is specified without an LST range, this indicates increased signal to noise.
		// If repeat-count and an LST range is specified, this indicates increased UV coverage.
		
		// Get the proper frequency bands.
		FrequencyBand[] band = site.getBand();

		// These are the variables we will need. 
		String targetName = null;
		double ra = 0.0;
		double dec = 0.0;
		double frequency = 0.0;
		int totalTime = 0;
		String weatherCondition = null;
		int repeatCount = 0;
		double b = -1.0;
		double e = -1.0;

		double sizeTargetBox = 7200.0;
		SB u = null;
		Target t = null;
		WeatherCondition w = null;
		
		// Now, get the data values.
		String[] s = null;
		for (int i = 0; i < value.length; ++i) {
			s = value[i].split(";",-1);
			if (s.length < 6)
				error("Invalid number of target parameters: " + value[i]);
			targetName = s[0].trim();
			try {
				ra = Double.parseDouble(s[1].trim());
				dec = Double.parseDouble(s[2].trim());
				if (s[3].trim().length() == 0)
					frequency = set.getCenterFrequency();
				else
					frequency = Double.parseDouble(s[3].trim());
				// Convert time to seconds.
				totalTime = (int)((Double.parseDouble(s[4].trim())) * 60.0 + 0.5);
			} catch (NumberFormatException err) {
				error("Invalid number format in " + value[i]);
			}
			if (s[5].trim().length() == 0)
				weatherCondition = weatherWord;
			else
				weatherCondition = s[5].trim();
			if (s.length == 7) {
				try {
					repeatCount = Integer.parseInt(s[6].trim());
				} catch (NumberFormatException err) {
					error("Invalid number format in " + s[6]);
				}	
			} else if (s.length == 9) {
				try {
					b = Double.parseDouble(s[7].trim());
					e = Double.parseDouble(s[8].trim());
				} catch (NumberFormatException err) {
					error("Invalid number format in " + value[i]);
				}
			} else if (s.length == 8) {
				error("Invalid number of target parameters: " + value[i]);
			} else
				repeatCount = 0;
			
			// Validate the weather word.
			w = validateWeatherCondition(weatherCondition); 
			
			// OK, we've got the data.
			
			// We will create one scheduling unit per target wirh the coordinates
			// in the center of a 2-degree square box.
			// We will place each sheduling unit in the program unit set.
		
			u = new SB(targetName);
			t = new Target (new Equatorial(ra,dec),sizeTargetBox,sizeTargetBox);
			u.setTarget(t);
			u.setCenterFrequency(frequency);
			u.setMaximumTimeInSeconds(totalTime);
			u.setScientificPriority(set.getScientificPriority());
			if (b > -1.0) {
				try {
					u.setLSTRange(b,e);
				} catch (IllegalArgumentException err) {
					error(err.toString());
				}
			}
			// Set the weather condition.
			u.setWeatherConstraint(w);
			
			// Set the repeat count.
			u.setMaximumNumberOfRepeats(repeatCount);
			
			// Set the frequency band.
			int j = 0;
			for (; j < band.length; ++j) {
				if (u.getCenterFrequency() >= band[j].getLowFrequency() && u.getCenterFrequency() <= band[j].getHighFrequency()) {
					u.setFrequencyBand(band[j]);
					break;
				}
			}
			if (j == band.length)
				error("Frequency " + u.getCenterFrequency() + " is out of the range of valid frequency bands.");
			
			// Anything else we might want to set?
			
			// Add the unit to the unitList and to the set. 
			unitList.add(u);
			set.addMember(u);
		}

	}
	

	/**
	 * @return
	 */
	public int getAdvanceClock() {
		return advanceClock;
	}

	/**
	 * @return
	 */
	public FrequencyBand[] getBand() {
		return band;
	}

    /**
      * @return
      */
    public Antenna[] getAntennas() {
        return antenna;
    }

	/**
	 * @return
	 */
	public DateTime getBeginTime() {
		return beginTime;
	}

	/**
	 * @return
	 */
	public DateTime getBeginCivilTime() {
		return beginCivilTime;
	}

	/**
	 * @return
	 */
	public int getChangeProjectTimeInSec() {
		return changeProjectTimeInSec;
	}

	/**
	 * @return
	 */
	public ContainerServices getContainerServices() {
		return containerServices;
	}

	/**
	 * @return
	 */
	public DateTime getEndTime() {
		return endTime;
	}

	/**
	 * @return
	 */
	public DateTime getEndCivilTime() {
		return endCivilTime;
	}

	/**
	 * @return
	 */
	public File getInputFile() {
		return inputFile;
	}

	/**
	 * @return
	 */
	public File getOutFile() {
		return outFile;
	}

	/**
	 * @return
	 */
	public File getLogFile() {
		return logFile;
	}

	/**
	 * @return
	 */
	public String getInstanceName() {
		return instanceName;
	}

	/**
	 * @return
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * @return
	 */
	public Level getMimimumLogLevel() {
		return mimimumLogLevel;
	}

	/**
	 * @return
	 */
	public Mode getMode() {
		return mode;
	}

	/**
	 * @return
	 */
	public Policy[] getPolicy() {
		return policy;
	}

	/**
	 * @return
	 */
	public Project[] getProject() {
		return project;
	}

	/**
	 * @return
	 */
	public int getSetUpTimeInSec() {
		return setUpTimeInSec;
	}

	/**
	 * @return
	 */
	public SiteCharacteristics getSite() {
		return site;
	}

	/**
	 * @return
	 */
	public SB[] getSB() {
		return unit;
	}

	/**
	 * @return
	 */
	public Program[] getProgram() {
		return set;
	}

	// The following methods have package access because they are used by
	// the simulator to set beginning and ending times of the simulation.
	// The site coordinates in the DateTime class must be set prior to creating 
	// any DateTime object.
	
	void setCivilTime() throws SimulationException {
		// Get the beginning and ending times.  
		// The beginning time of the simulation.
		beginCivilTime = getTime(Tag.beginTime);
		// The ending time of the simulation.
		endCivilTime = getTime(Tag.endTime);
		if (endCivilTime.le(beginCivilTime))
			error("The ending simulation time (" + endCivilTime + 
				") must be greater than the beginning simulation time(" + 
				beginCivilTime + ").");
	}

	/**
	 * @param time
	 */
	void setBeginTime(DateTime time) {
		beginTime = new DateTime(time);
	}

	/**
	 * @param time
	 */
	void setEndTime(DateTime time) {
		endTime = new DateTime(time);
	}


}
