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
 
package alma.scheduling.planning_mode_sim.simulator;

import java.util.Properties;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import alma.scheduling.planning_mode_sim.define.DateTime;
import alma.scheduling.planning_mode_sim.define.SiteCharacteristics;
import alma.scheduling.planning_mode_sim.define.SProject;
import alma.scheduling.planning_mode_sim.define.SUnitSet;
import alma.scheduling.planning_mode_sim.define.MemberOf;
import alma.scheduling.planning_mode_sim.define.SUnit;
import alma.scheduling.planning_mode_sim.define.SPolicy;
import alma.scheduling.planning_mode_sim.define.SPolicyFactor;
import alma.scheduling.planning_mode_sim.define.Priority;
import alma.scheduling.planning_mode_sim.define.Equatorial;
import alma.scheduling.planning_mode_sim.define.Target;
import alma.scheduling.planning_mode_sim.define.WeatherCondition;
import alma.scheduling.planning_mode_sim.define.Status;
import alma.scheduling.planning_mode_sim.define.FrequencyBand;

/**
 * The SimulationData class is an extension of the Properties
 * class.  It is used to house all the input values in the form
 * of a properties file.  This includes the simulation mode, minimum 
 * logging level, beginning and ending time, site characteristics,
 * as well as basic data for constructing projects ans scheduling units.
 * In addition to being a list of properties, this class contains 
 * methods for getting strings, ints, doubles, and times from the 
 * list of properties, throwing exceptions if these are not found.
 * 
 * @version 1.00  Sept. 30, 2003  For ALMA software release R1.
 * @author Allen Farris
 */
public class SimulationInput extends Properties {

	/**
	 * The Java logger used in the simulation.
	 */
	Logger log;

	/**
	 * The simulation mode.
	 */
	private Mode mode;
	
	/**
	 * The mimimum logging level. 
	 */
	private Level mimimumLogLevel;

	/**
	 * The beginning time of the simulation.
	 */	
	private DateTime begin;
	
	/**
	 * The ending time of the simulation.
	 */	
	private DateTime end;
	
	/**
	 * The site characteristics.
	 */
	private SiteCharacteristics site;

	/**
	 * This list of projects as constructed from the input properties file.
	 */
	private SProject[] project;

	/**
	 * This list of unit sets as constructed from the input properties file.
	 */
	private SUnitSet[] unitSet;

	/**
	 * This list of units as constructed from the input properties file.
	 */
	private SUnit[] sUnit;

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
	 * The simulation output file.
	 */
	private PrintStream out;
	
	/**
	 * An internal method used in the event an error is found in the simulation input.
	 * A severe message is entered into the log and an exception is thrown.
	 * @param message The text of the error message.
	 * @throws SimulationException
	 */
	private void error(String message) throws SimulationException {
		log.severe("SimulationInput.error " + message);
		throw new SimulationException("SimulationInput", Level.SEVERE, message);
	}

	/**
	 * Create a SimulationInput object.
	 * @param filename The full-path name of the properties file containing the input data.
	 * @param log The Java logger to be used in the simulation.
	 * @throws SimulationException Thrown if there is any error in accessing the properties file.
	 */
	public SimulationInput(String filename, Logger log) throws SimulationException {
		super();
		this.log = log;
		// First, load the properties from the specified file.
		try {
			log.info("SimulationInput.filename Using file " + filename + " for simultation data.");
			FileInputStream file = new FileInputStream(filename);
			super.load(file);
			log.fine("SimulationInput.filename " + filename + " loaded.");
		} catch (IOException err) {
			error("Could not open file " + filename);
		}

		// Get the basic simulation data.
		
		// The simulation mode.
		String s = getString(Tag.mode);
		if (s.equals("FULL"))
			mode = Mode.FULL;
		else if (s.equals("PLANNING"))
			mode = Mode.PLANNING;
		else if (s.equals("CONFLICT"))
			mode = Mode.CONFLICT;
		else
			error("Invalid value (" + s + ") for Simulation.mode."); 
		// The mimimum logging level.
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
		// Reset the logging level.
		log.setLevel(mimimumLogLevel);		
		// The site characteristics.
		site = new SiteCharacteristics(this);
	 	// The time in seconds to change to a different observing setup.
		setUpTimeInSec = getInt(Tag.setUpTime);
	 	// The time in seconds to change from one project to a new project.
		changeProjectTimeInSec = getInt(Tag.changeProjectTime);
	 	// The time in seconds to advance the clock when nothing can be scheduled.
		advanceClock = getInt(Tag.advanceClock);
			
	}

	/**
	 * Get a value of type String from a named property.
	 */
	public String getString(String name) throws SimulationException {
		String s = getProperty(name);
		if (s == null) {
			error("Property " + name + " was not found.");
		}
		log.finer("SimulationInput: Got string property " + name + " value " + s);
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
		log.finer("SimulationInput: Got int property " + name + " value " + s);
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
		log.finer("SimulationInput: Got double property " + name + " value " + s);
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
		log.finer("SimulationInput: Got time property " + name + " value " + s);
		DateTime t = null;
		try {
			t = new DateTime(s);
		} catch (IllegalArgumentException err) {
			error("Property " + name + ": " + err.toString());
		}
		return t;
	}

	/**
	 * @return
	 */
	public DateTime getBegin() {
		return begin;
	}

	/**
	 * @return
	 */
	public DateTime getEnd() {
		return end;
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
	public SiteCharacteristics getSite() {
		return site;
	}

	public SProject[] getProject() {
		return project;
	}
	
	public SUnit[] getSUnit() {
		return sUnit;
	}

	public SUnitSet[] getSUnitSet() {
		return unitSet;
	}

	public SPolicy[] getSPolicy() throws SimulationException {
		SPolicy[] x = new SPolicy [1];
		x[0] = new SPolicy ();
		x[0].setName("R1Policy");
		x[0].setVersion("V1.0");
		x[0].setComment("R1 Release -- 1 Oct, 2003");
		x[0].setScoreCalculation("RankingCalculation * SuccessCalculation");
		x[0].setSuccessCalculation("(w0*positionElevation + w1*positionMaximum  + w2*weather) / (w0 + w1 + w2)");
			// Provided position > minimumElevation and weather > 0
		x[0].setRankingCalculation("w2 * priority + w3 * sameProjectSameBand + " +
			"w4 * sameProjectDifferentBand + w5 * differentProjectSameBand + " +
			"w6 * differentProjectDifferentBand + w7 * newProject + w8 * OneSBRemaining");
		SPolicyFactor[] factor = new SPolicyFactor [10];
		for (int i = 0; i < factor.length; ++i)
			factor[i] = new SPolicyFactor ();
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
		return x;
	}


	public void setBeginEndTime() throws SimulationException {
		// The beginning time of the simulation.
		begin = getTime(Tag.beginTime);
		// The ending time of the simulation.
		end = getTime(Tag.endTime);
		if (end.le(begin))
			error("The ending simulation time (" + end + 
				") must be greater than the beginning simulation time(" + 
				begin + ").");
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
	public void setSourceData() throws SimulationException {
		// We must set the beginning and ending time of the simulation.
		
		// First, create all the projects and get all the project value strings.
		// The form is: numberProjects = N
		// 				project.<i> = projectName; PI; priority; numberTargets
		int n = getInt(Tag.numberProjects);
		project = new SProject [n];
		String[] projectValue = new String [n];
		for (int i = 0; i < n; ++i) {
			project[i] = new SProject ();
			projectValue[i] = getString(Tag.project + "." + i);
		}
		
		// We will accumulate all the unit sets and units in array lists.
		ArrayList set = new ArrayList ();
		ArrayList unit = new ArrayList ();
		
		// For each project entry, get the data and put that data in the SProject objects.
		// Format is: projectName; PI; priority; numberTargets
		String name = null;
		String pi = null;
		String priority = null;
		String numberTargetsS = null;
		int numberTargets = 0;
		StringTokenizer token = null;
		for (int i = 0; i < project.length; ++i) {
			token = new StringTokenizer(projectValue[i],";");
			try {
				name = token.nextToken().trim();
				pi = token.nextToken().trim();
				priority = token.nextToken().trim();
				numberTargetsS = token.nextToken().trim();
				try {
					numberTargets = Integer.parseInt(numberTargetsS);
				} catch (NumberFormatException err) {
					error("Invalid number format in " + numberTargetsS);
				}
			} catch (NoSuchElementException err) {
				error("Missing element in project string: " + projectValue[i]);
			}
 			project[i].setProjectName(name);
			project[i].setPI(pi);
			project[i].setObsProjectId(name + "_project");
			project[i].setProposalRef(name + "_proposal");
			project[i].setProjectVersion("v01");
			// Create the program and add it to the project.
			SUnitSet program = new SUnitSet ();
			set.add(program);
			project[i].setProgram(program);
			try {
				program.setScientificPriority(new Priority (priority));
			} catch (IllegalArgumentException err) {
				error(err.toString());
			}
			String[] targetValue = new String [numberTargets];
			System.out.println(name);
			for (int j = 0; j < numberTargets; ++j) {
				targetValue[j] = getString(Tag.target + "." + i + "." + j);
			}
			doTargets(program,targetValue,unit);
			// Set values in the project.
			project[i].setNumberUnitsCompleted(0);
			project[i].setNumberUnitsFailed(0);
			int totalTime = 0;
			int totalUnits = 0;
			SUnitSet s = project[i].getProgram();
			MemberOf[] m = s.getMember();
			SUnit x = null;
			for (int j = 0; j < m.length; ++j) {
				x = (SUnit)m[j];
				totalTime += x.getMaximumTimeInSeconds() * (x.getMaximumNumberOfRepeats() + 1);
				totalUnits += 1;
			}
			project[i].setTotalRequiredTimeInSeconds(totalTime);
			project[i].setTotalUnits(totalUnits);
			project[i].setTotalUsedTimeInSeconds(0);
			project[i].setProjectStatus(Status.READY);
		}
		
		// Save the unit sets and the units.
		unitSet = new SUnitSet [set.size()];
		unitSet = (SUnitSet[])set.toArray(unitSet);
		sUnit = new SUnit [unit.size()];
		sUnit = (SUnit[])unit.toArray(sUnit);
	}

	private void doTargets(SUnitSet program, String[] value, ArrayList unit) 
		throws SimulationException {
		// Syntax is: targetName; ra; dec; frequency; total-time; weather-condition 
		// First, declare arrays to hold the data.
		
		String[] name = new String [value.length];
		String[] raS = new String [value.length];
		double[] ra = new double [value.length];
		String[] decS = new String [value.length];
		double[] dec = new double [value.length];
		String[] freqS = new String [value.length];
		double[] freq = new double [value.length];
		String[] timeS = new String [value.length];
		int[] time = new int [value.length];
		String[] weather = new String [value.length];
		String[] repeatS = new String [value.length];
		int[] repeat = new int [value.length];
		for (int i = 0; i < repeat.length; ++i)
			repeat[i] = 0;
		
		// Now, get the data values.
		StringTokenizer token = null;
		for (int i = 0; i < value.length; ++i) {
			token = new StringTokenizer(value[i],";");
			try {
				name[i] = token.nextToken().trim();
				raS[i] = token.nextToken().trim();
				decS[i] = token.nextToken().trim();
				freqS[i] = token.nextToken().trim();
				timeS[i] = token.nextToken().trim();
				weather[i] = token.nextToken().trim();
				try {
					repeatS[i] = token.nextToken().trim();
					try {
						repeat[i] = Integer.parseInt(repeatS[i]);
					} catch (NumberFormatException err) {
						error("Invalid number format in " + repeatS[i]);
					}
				} catch (NoSuchElementException err) {
				}
				try {
					ra[i] = Double.parseDouble(raS[i]);
				} catch (NumberFormatException err) {
					error("Invalid number format in " + raS[i]);
				}
				try {
					dec[i] = Double.parseDouble(decS[i]);
				} catch (NumberFormatException err) {
					error("Invalid number format in " + decS[i]);
				}
				try {
					freq[i] = Double.parseDouble(freqS[i]);
				} catch (NumberFormatException err) {
					error("Invalid number format in " + freqS[i]);
				}
				try {
					// Convert time to seconds.
					time[i] = (int)((Double.parseDouble(timeS[i])) * 60.0 + 0.5);
				} catch (NumberFormatException err) {
					error("Invalid number format in " + timeS[i]);
				}
				// Validate weather words.
				boolean ok = false;
				if (weather[i].equals("exceptional"))
					ok = true;
				else if (weather[i].equals("excellent"))
					ok = true;
				else if (weather[i].equals("good"))
					ok = true;
				else if (weather[i].equals("average"))
					ok = true;
				else if (weather[i].equals("belowAverage"))
					ok = true;
				else if (weather[i].equals("poor"))
					ok = true;
				else if (weather[i].equals("dismal"))
					ok = true;
				else if (weather[i].equals("any"))
					ok = true;
				if (!ok)
					error("Invalid weather condition " + weather[i]);
			} catch (NoSuchElementException err) {
				error("Missing element in project string: " + value[i]);
			}
			System.out.println(name[i] + " " + ra[i] + " hrs " + dec[i] + " degs " +
				freq[i] + " Ghz " + time[i] + " secs weather is " + weather[i]); 
		}
		// OK, we've got the data.

		// Get the proper frequency bands.
		FrequencyBand[] band = site.getFrequencyBand();

		// We will create one scheduling unit per target wirh the coordinates
		// in the center of a 2-degree square box.
		// We will place each sheduling unit in the program unit set.
		double sizeTargetBox = 7200.0;
		SUnit u = null;
		Target t = null;
		WeatherCondition w = null;
		String[] cond = null;
		for (int i = 0; i < name.length; ++i) {
			u = new SUnit ();
			u.setSchedBlockId(name[i]);
			t = new Target (new Equatorial(ra[i],dec[i]),sizeTargetBox,sizeTargetBox);
			u.setTarget(t);
			u.setFrequency(freq[i]);
			u.setMaximumTimeInSeconds(time[i]);
			u.setScientificPriority(program.getScientificPriority());
			if (weather[i].equals("exceptional")) {
				cond = new String [3];
				cond[0] = "quality >= 0.9 -> 1.0";
				cond[1] = "quality >= 0.8 -> 0.8";
				cond[2] = "quality <  0.8 -> 0.0";
				w = new WeatherCondition (cond);
			} else if (weather[i].equals("excellent")) {
				cond = new String [3];
				cond[0] = "quality >= 0.8 -> 1.0";
				cond[1] = "quality >= 0.7 -> 0.8";
				cond[2] = "quality <  0.7 -> 0.0";
				w = new WeatherCondition (cond);
			}
			else if (weather[i].equals("good")) {
				cond = new String [3];
				cond[0] = "quality >= 0.7 -> 1.0";
				cond[1] = "quality >= 0.6 -> 0.8";
				cond[2] = "quality <  0.6 -> 0.0";
				w = new WeatherCondition (cond);
			}
			else if (weather[i].equals("average")) {
				cond = new String [3];
				cond[0] = "quality >= 0.6 -> 1.0";
				cond[1] = "quality >= 0.5 -> 0.8";
				cond[2] = "quality <  0.5 -> 0.0";
				w = new WeatherCondition (cond);
			}
			else if (weather[i].equals("belowAverage")) {
				cond = new String [3];
				cond[0] = "quality >= 0.5 -> 1.0";
				cond[1] = "quality >= 0.4 -> 0.8";
				cond[2] = "quality <  0.4 -> 0.0";
				w = new WeatherCondition (cond);
			}
			else if (weather[i].equals("poor")) {
				cond = new String [3];
				cond[0] = "quality >= 0.4 -> 1.0";
				cond[1] = "quality >= 0.3 -> 0.8";
				cond[2] = "quality <  0.3 -> 0.0";
				w = new WeatherCondition (cond);
			}
			else if (weather[i].equals("dismal")) {
				cond = new String [3];
				cond[0] = "quality >= 0.3 -> 1.0";
				cond[1] = "quality >= 0.2 -> 0.8";
				cond[2] = "quality <  0.2 -> 0.0";
				w = new WeatherCondition (cond);
			}
			else if (weather[i].equals("any")) {
				cond = new String [1];
				cond[0] = "quality >= 0.0 -> 1.0";
				w = new WeatherCondition (cond);
			}
			u.setWeatherConstraint(w);
			
			// Anything else we might want to set?
			
			// Set the repeat count.
			u.setMaximumNumberOfRepeats(repeat[i]);
			
			// Set the frequency band.
			int j = 0;
			for (; j < band.length; ++j) {
				if (u.getFrequency() >= band[j].getLowFrequency() && u.getFrequency() <= band[j].getHighFrequency()) {
					u.setFrequencyBand(j);
					break;
				}
			}
			if (j == band.length) {
				throw new SimulationException("SimulationInput","Frequency " + 
					u.getFrequency() + " is out of the range of valid frequency bands.");
			}
			
			// Set the status.
			u.setUnitStatus(Status.READY);
			
			//System.out.println("INPUT: weather");
			//for (int k = 0; k < cond.length; ++k)
			//	System.out.println("cond: " + k + " " + cond[k]);
			System.out.println("TARGET: " + u.getId() + " repeat " + u.getMaximumNumberOfRepeats());
			unit.add(u);
			program.addMember(u);
		}
	}
	
	public void setOut(PrintStream out) {
		this.out = out;
	}

	/**
	 * @return
	 */
	public PrintStream getOut() {
		return out;
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
	public int getAdvanceClock() {
		return advanceClock;
	}

	/**
	 * @return
	 */
	public int getChangeProjectTimeInSec() {
		return changeProjectTimeInSec;
	}

	/**
	 * @param time
	 */
	public void setBegin(DateTime time) {
		begin = time;
	}

	/**
	 * @param time
	 */
	public void setEnd(DateTime time) {
		end = time;
	}

}
