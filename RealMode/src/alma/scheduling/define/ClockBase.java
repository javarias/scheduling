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
 * File ClockBase.java
 */
 
package ALMA.scheduling.define;

class Timer implements Runnable {
	private Clock clock;
	private long milliseconds;
	private ClockAlarmListener alarm;
	Timer(Clock clock, ClockAlarmListener alarm, long milliseconds) {
		this.clock = clock;
		this.alarm = alarm;
		this.milliseconds = milliseconds;
	}
	public void run() {
		if (milliseconds <= 0)
			return;
		long startTime = clock.currentTimeMillis();
		long newStartTime = 0;
		long timeInterval = milliseconds;
		do {
			try {
				Thread.sleep(timeInterval);
			} catch (InterruptedException ex) {
				newStartTime = clock.currentTimeMillis();
				timeInterval -= newStartTime - startTime;
				startTime = newStartTime;
			}
		} while (timeInterval >= 0);
		alarm.wakeUp(clock.getSTime());
	}
}

/**
 * This abstract class from which actual implementations,
 * i.e. the ALMAClock and the ClockSimulator, are extended. 
 * 
 * @version 1.00  Aug 1, 2003
 * @author Allen Farris
 */
public abstract class ClockBase implements Clock {

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

	/**
	 * Create a ClockBase.
	 */
	protected ClockBase() {
		this.longitudeInDegrees = 0.0;
		this.longitudeInHours = 0.0;
		this.longitude = 0.0;
		this.latitudeInDegrees = 0.0;
		this.latitude = 0.0;
		this.timeZone = 0;
		this.convertToUT = 0.0;
	}

	/* 
	 * @see alma.scheduling.define.Clock#setCoordiantes(double, double, int)
	 */
	public void setCoordinates(double lng, double lat, int zone) {
		this.longitudeInDegrees = lng;
		this.longitudeInHours = lng / 15.0;
		this.longitude = Math.toRadians(lng);
		this.latitudeInDegrees = lat;
		this.latitude = Math.toRadians(lat);
		this.timeZone = zone;
		this.convertToUT = -timeZone / 24.0;
	}

	/* 
	 * Get the current time in milliseconds, using the system clock.  The actual
	 * quantity returned is the difference, measured in milliseconds, between the current 
	 * time and midnight, January 1, 1970 UTC.  If a different clock is used, this method 
	 * must be overridden.
	 * @return the difference, measured in milliseconds, between the current 
	 * time and midnight, January 1, 1970 UTC.
	 * @see alma.scheduling.define.Clock#currentTimeMillis()
	 */
	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}

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

	/* (non-Javadoc)
	 * @see alma.scheduling.define.Clock#setAlarm(alma.scheduling.define.STime, alma.scheduling.define.ClockAlarmListener)
	 */
	public void setAlarm(STime time, ClockAlarmListener listener) {
		long duration = time.getTime() - currentTimeMillis();
		if (duration <= 0)
			return;
		Timer t = new Timer(this,listener,duration);
		Thread x = new Thread(t);
		x.start();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.define.Clock#setAlarm(int, alma.scheduling.define.ClockAlarmListener)
	 */
	public void setAlarm(int seconds, ClockAlarmListener listener) {
		if (seconds <= 0)
			return;
		Timer t = new Timer(this,listener,seconds*1000L);
		Thread x = new Thread(t);
		x.start();
	}

}
