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
 * File CLock.java
 */
 
package alma.Scheduling.Define;

import alma.Scheduling.Define.DateTime;
import alma.Scheduling.Define.ArrayTime;

/**
 * The Clock interface provides the current time, the precise geographical
 * location of the clock and time zone information.  It also provides 
 * methods for synchronizing the system time and the array time, as well as 
 * setting alarms, i.e. methods that provide for a notification at a particular 
 * time.
 * 
 * @version 1.30 May 10, 2004
 * @author Allen Farris
 */
public interface Clock {
	
	/**
	 * Set the coordinates of the clock, by specifying the geographical coordinates of the clock.
	 * @param lng The longitude in degrees West (i.e., degrees west of Greenwich are positive).
	 * @param lat The latitude in degrees North.
	 * @param zone The local time zone relative to UT, i.e., the number of hours 
	 * between the local time zone and UT, i.e., localTime - UT.  If the local time zone
	 * is ahead of Greenwich mean time, then zone should be positive.  If the local 
	 * time zone is behind Greenwich mean time, then zone should be negative.  
	 * For example, 9:00 Mountain Daylight Time is 15:00 UT, so if the local time is 
	 * MDT, zone is -6;
	 */
	public void setClockCoordinates(double lng, double lat, int zone);

	/**
	 * Convert this DateTime to a string in FITS format that
	 * corresponds to the local time.
	 */
	public String toString();

	/**
	 * Return the current time as an DateTime object.  This time is
	 * an approximation to the array time.  It uses the current system
	 * time and adds a delta to it to synchronize it to the array time.
	 * @return The current time as an DateTime object.
	 */
	public DateTime getDateTime();
	
	/**
	 * Return the current time of day in hours, where 0.0 <= hours < 24.0.
	 * @return The current time of day in hours, where 0.0 <= hours < 24.0.
	 */
	public double getTimeOfDay();
	
	/**
	 * Return the Julian day.
	 * @return The Julian day as a double.
	 */
	public double getJD();

	/**
	 * Return the array time.  This time is from the control system,
	 * if this is real; if it is a simulation, then it is from the
	 * clock simulator.
	 * @return The current time as an ArrayTime object.
	 */
	public ArrayTime getArrayTime();

	/**
	 * Get the latitude of the clock in radians.
	 * @return The latitude of the clock in radians.
	 */
	public double getLatitude();

	/**
	 * Get the latitude of the clock in degrees.
	 * @return The latitude of the clock in degrees.
	 */
	public double getLatitudeInDegrees();

	/**
	 * Get the longitude of the clock in radians.
	 * @return The longitude of the clock in radians.
	 */
	public double getLongitude();

	/**
	 * Get the longitude of the clock in degrees.
	 * @return The longitude of the clock in degrees.
	 */
	public double getLongitudeInDegrees();

	/**
	 * Get the longitude of the clock in hours.
	 * @return The longitude of the clock in hours.
	 */
	public double getLongitudeInHours();

	/**
	 * Get the time zone of the clock, which is the local time zone relative to UT, i.e., 
	 * the number of hours between the local time zone and UT, i.e., localTime - UT.  
	 * If the local time zone is ahead of Greenwich mean time, then time zone is positive.  
	 * If the local time zone is behind Greenwich mean time, then time zone is negative.  
	 * For example, 9:00 Mountain Daylight Time is 15:00 UT, so if the local time is 
	 * MDT, the time zone is -6;
	 * @return The time zone of the clock.
	 */
	public int getTimeZone();

	/**
	 * Get the conversion factor from local time to UT, i.e., the factor, 
	 * in units of fractions of a day, that must be added to local time to get UT.
	 * @return The conversion factor from local time to UT, i.e., the factor, 
	 * in units of fractions of a day, that must be added to local time to get UT. 
	 */
	public double getConvertToUT();

	/**
	 * Synchronize the currrent system time and the array time.
	 */
	public void synchronize();
	
	/**
	 * Set an alarm, to go off at a particular time.
	 * @param time		The time at which the alarm goes off.
	 * @param listener	The listener method to call to process the alarm.
	 */
	public void setAlarm(DateTime time, ClockAlarmListener listener);

	/**
	 * Set an alarm, to go off at N seconds from now.
	 * @param seconds	The number of seconds to wait for the alarm to go off.
	 * @param listener	The listener method to call to process the alarm.
	 */
	public void setAlarm(int seconds, ClockAlarmListener listener);


}
