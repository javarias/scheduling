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
 * File Tag.java
 */
 
package alma.scheduling.planning_mode_sim.simulator;

/**
 * The Tag class is a collection of static strings that are the names
 * of various properties and events that are used throughout the
 * simulation.  These names appear in the properties file that is input 
 * to the simulation, in the output log file, or, sometimes, both. 
 * 
 * @version 1.00  Aug 25, 2003
 * @author Allen Farris
 */
public class Tag {
	public static final String title 					= "Simulation.title";
	public static final String mode 					= "Simulation.mode";
	public static final String logLevel 				= "Simulation.logLevel";
	public static final String beginTime 				= "Simulation.beginTime";
	public static final String endTime 					= "Simulation.endTime";
	public static final String longitude 				= "Site.longitude";
	public static final String latitude 				= "Site.latitude";
	public static final String altitude 				= "Site.altitude";
	public static final String timeZone 				= "Site.timeZone";
	public static final String minimumElevationAngle 	= "Site.minimumElevationAngle";
	public static final String numberAntennas 			= "Site.numberAntennas";
	public static final String sbBegin	 				= "SB.begin";
	public static final String sbEnd	 				= "SB.end";
	public static final String subArrayCreate			= "SubArray.create";
	public static final String subArrayDestroy			= "SubArray.destroy";
	public static final String controlChannelName		= "Control.channelName";
	public static final String numberProjects			= "numberProjects";
	public static final String project					= "project";
	public static final String target					= "target";
	public static final String numberWeatherFunctions	= "Weather.numberFunctions";
	public static final String weather					= "Weather";
	public static final String setUpTime				= "Simulation.setUpTime";
	public static final String changeProjectTime		= "Simulation.changeProjectTime";
	public static final String advanceClock				= "Simulation.advanceClock";
	public static final String weightPositionElevation	= "Weight.positionElevation";
	public static final String weightPositionMaximum	= "Weight.positionMaximum";
	public static final String weightWeather			= "Weight.weather";
	public static final String weightPriority			= "Weight.priority";
	public static final String weightSameProjectSameBand			= "Weight.sameProjectSameBand";
	public static final String weightSameProjectDifferentBand		= "Weight.sameProjectDifferentBand";
	public static final String weightDifferentProjectSameBand		= "Weight.differentProjectSameBand";
	public static final String weightDifferentProjectDifferentBand	= "Weight.differentProjectDifferentBand";
	public static final String weightNewProject			= "Weight.newProject";
	public static final String weightOneSBRemaining		= "Weight.oneSBRemaining";
	public static final String numberOfBands			= "FrequencyBand.numberOfBands";
	public static final String band						= "FrequencyBand";
}
