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
 * File ClockSimulator.java
 */
 
package alma.scheduling.PlanningModeSim;

import alma.scheduling.PlanningModeSim.Define.BasicComponent;
import alma.scheduling.PlanningModeSim.Define.SimulationException;
import alma.scheduling.Define.Clock;
import alma.scheduling.Define.ClockAlarmListener;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.ArrayTime;
import alma.scheduling.Define.SiteCharacteristics;


/**
 * The ClockSimulator class implements a clock that is used in all
 * simulation modes.
 * 
 * There are two ways to implement a simulation clock.  First, one
 * can speed up time by some specified factor, say F.  In this method,
 * one second of real time would correspond to F seconds of simulated
 * time.  Second, one can have various components of the simulation
 * advance the clock by some period of time that corresponds to the
 * length of time some process might actaully take.  One of the 
 * difficulties of this second mode is accommodating multiple threads
 * of execution in the simulation.
 * 
 * Both methods are useful in the context of the scheduling simulator.
 * For example, in the FULL simulation mode, the first method is useful
 * in testing the full-simulation capabilities.  In the planning mode,
 * the second method is useful, since the control system's execution of
 * scheduling blocks is the basic driving factor that determines the 
 * time.
 * 
 * In this current implementation, we will only implement the 
 * second method.  This implementation does not use or access
 * the system time.  It uses DateTime with methods to set and advance
 * the clock.  In the future we may use a speeded-up version of the
 * system clock.
 * 
 * 
 * @version 1.10  Dec. 12, 2003
 * @author Allen Farris
 */
public class ClockSimulator extends BasicComponent implements Clock {

	static private final double radToDeg = 180.0 / Math.PI;
	static private final double radToHour =  12.0 / Math.PI;
	static private final double degToRad = Math.PI / 180.0;
	static private final double hourToRad =  Math.PI / 12.0;

	// The longitude in degrees West (i.e., degrees west of Greenwich are positive).
	private double longitudeInDegrees;
	private double longitudeInHours;
	private double longitude;
	// The latitude in degrees North.
	private double latitudeInDegrees;
	private double latitude;
	// The number of hours between the local time zone and UT, i.e., localTime - UT.
	private int timeZone;
	// The factor, in units of fractions of a day, that must be added to local time to get UT.
	private double convertToUT;
	
	// The current time of this clock.
	private DateTime time;

	/**
	 * Create a simulated clock.
	 */
	public ClockSimulator() {
		super();
		this.longitudeInDegrees = 0.0;
		this.longitudeInHours = 0.0;
		this.longitude = 0.0;
		this.latitudeInDegrees = 0.0;
		this.latitude = 0.0;
		this.timeZone = 0;
		this.convertToUT = 0.0;
		this.time = null;
	}

	// Lifecycle methods
	
	public void initialize() throws SimulationException {
		SimulationInput data = (SimulationInput)containerServices.getComponent(Container.SIMULATION_INPUT);
		SiteCharacteristics site = data.getSite();
		setClockCoordinates(site.getLongitude()* radToDeg, site.getLatitude() * radToDeg, site.getTimeZone());
		logger.info(instanceName + ".initialized");
	} 

	public void cleanUp() {
		time = null;
		super.cleanUp();
	} 

	// Set methods
	
	/* 
	 * @see alma.scheduling.define.Clock#setCoordiantes(double, double, int)
	 */
	public void setClockCoordinates(double lng, double lat, int zone) {
		this.longitudeInDegrees = lng;
		this.longitudeInHours = lng / 15.0;
		this.longitude = Math.toRadians(lng);
		this.latitudeInDegrees = lat;
		this.latitude = Math.toRadians(lat);
		this.timeZone = zone;
		this.convertToUT = -timeZone / 24.0;
	}

	/**
	 * Set this time to the specified time.
	 * @param time The time to which this time is being set.
	 */
	public void setTime(DateTime time) {
		this.time = new DateTime(time);
	}

	/**
	 * Advance the simulated clock by the specified number of seconds.
	 * @param seconds
	 */
	public void advance(int seconds) {
		time.add(seconds);
	}

	/* 
	 * @see alma.scheduling.define.Clock#setAlarm(alma.scheduling.define.DateTime, alma.scheduling.define.ClockAlarmListener)
	 */
	public void setAlarm(DateTime time, ClockAlarmListener listener) {
		// Not used in this simulation.
	}

	/* 
	 * @see alma.scheduling.define.Clock#setAlarm(int, alma.scheduling.define.ClockAlarmListener)
	 */
	public void setAlarm(int seconds, ClockAlarmListener listener) {
		// Not used in this simulation.
	}

	/* 
	 * Synchronize the current time to the array time.
	 * @see alma.scheduling.master_scheduler.Clock#synchronize()
	 */
	public void synchronize() {
		// In the simulated time, this is a do nothing method
		// because the two times are always synchronized.
	}

	// Get Methods
	
	/**
	 * Get the latitude of the clock in radians.
	 * @return The latitude of the clock in radians.
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Get the latitude of the clock in degrees.
	 * @return The latitude of the clock in degrees.
	 */
	public double getLatitudeInDegrees() {
		return latitudeInDegrees;
	}

	/**
	 * Get the longitude of the clock in radians.
	 * @return The longitude of the clock in radians.
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * Get the longitude of the clock in degrees.
	 * @return The longitude of the clock in degrees.
	 */
	public double getLongitudeInDegrees() {
		return longitudeInDegrees;
	}

	/**
	 * Get the longitude of the clock in hours.
	 * @return The longitude of the clock in hours.
	 */
	public double getLongitudeInHours() {
		return longitudeInHours;
	}

	/**
	 * Get the time zone of the clock, which is the local time zone relative to UT, i.e., 
	 * the number of hours between the local time zone and UT, i.e., localTime - UT.  
	 * If the local time zone is ahead of Greenwich mean time, then time zone is positive.  
	 * If the local time zone is behind Greenwich mean time, then time zone is negative.  
	 * For example, 9:00 Mountain Daylight Time is 15:00 UT, so if the local time is 
	 * MDT, the time zone is -6;
	 * @return The time zone of the clock.
	 */
	public int getTimeZone() {
		return timeZone;
	}

	/**
	 * Get the conversion factor from local time to UT, i.e., the factor, 
	 * in units of fractions of a day, that must be added to local time to get UT.
	 * @return The conversion factor from local time to UT, i.e., the factor, 
	 * in units of fractions of a day, that must be added to local time to get UT. 
	 */
	public double getConvertToUT() {
		return convertToUT;
	}

	/* 
	 * Return the current simulated time.
	 * @see alma.scheduling.master_scheduler.Clock#getDateTime()
	 */
	public DateTime getDateTime() {
		return new DateTime (time);
	}
	
	public double getJD() {
		return time.getJD();
	}

	/**
	 * Return the current time of day in hours, where 0.0 <= hours < 24.0.
	 * @return The current time of day in hours, where 0.0 <= hours < 24.0.
	 */
	public double getTimeOfDay() {
		return time.getTimeOfDay();
	}

	/* 
	 * Return the current simulated array time.
	 * @see alma.scheduling.master_scheduler.Clock#getArrayTime()
	 */
	public ArrayTime getArrayTime() {
		// We don't really need the array time in the simulator.
		return null;
	}

	/**
	 * Compare this time to the specified DateTime, returning a -1, 0, +1 if
	 * the specified time is less than, equal to, or greater than this time.
	 * @param time The time to which this time is being compared.
	 * @return -1, 0, +1 if the specified time is less than, equal to, or 
	 * greater than this time.
	 */
	public int compareTo(DateTime time) {
		return this.compareTo(time);
	}

	// TODO move this method to the test directory.
	public static void main(String[] args) {
		/*
		String dataFilename = "D:\\Users\\afarris\\projects\\workspace\\AFScheduling\\ALMA\\scheduling\\simulator\\data.txt";
		String logFilename = "D:\\Users\\afarris\\projects\\workspace\\AFScheduling\\ALMA\\scheduling\\simulator\\log.xml";

		// Create the logger and the log file.
		Logger log = Logger.getLogger("Scheduling.Simulator");
		FileHandler logfile = null;
		try {
			logfile = new FileHandler(logFilename);
		} catch (IOException ioerr) {
			 ioerr.printStackTrace(System.out);
			 System.exit(0);
		}
		// The log file will be an XML file.
		XMLFormatter formatter = new XMLFormatter();
		logfile.setFormatter(formatter);
		log.setLevel(Level.CONFIG);
		log.addHandler(logfile);

		
		SimulationInput data = null;
		try {
			data = new SimulationInput (dataFilename,log);
		} catch (SimulationException err) {
			System.out.println(err.toString());
			System.exit(0);
		}
		ClockSimulator x = new ClockSimulator(data);
		try {
			x.setComponentName("clock1");
			x.setContainerServices(new Container());
			x.initialize();
		} catch (ComponentLifecycleException err) {
			System.out.println(err.toString());
			System.exit(0);
		}
		System.out.println("This current time is " + x.getDateTime());
		System.out.println("The clock is located at " + x.getLatitudeInDegrees() + 
			" degrees latitude  and " + x.getLongitudeInDegrees() + " degrees longitude.");
		x.advance(3600);
		System.out.println("Now, the current time is " + x.getDateTime());
		*/ 
	}


}
