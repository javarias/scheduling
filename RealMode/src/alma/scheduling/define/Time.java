/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
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
 * File Time.java
 */
 
package alma.scheduling.define;

/**
 * The current time based on a 24-hour clock.
 * The main use of this class is in conjunction with the DateTime class.
 * 
 * @version 1.00  Jun 11, 2003
 * @author Allen Farris
 */
public class Time {

	/**
	 * The current time based on a 24-hour clock, in hours.
	 */
	private double time;

	/**
	 * Create a time, given hours, minutes, and seconds. 
	 * @param hh The hours, from 0 to 23.
	 * @param mm The minutes, from 0 to 59
	 * @param ss The seconds, from 0 to 59.99999...
	 * @exception An IllegalArgumentException is thrown if the
	 * 		arguments are not in the valid range.
	 */
	public Time (int hh, int mm, double ss) {
		if (hh < 0 || hh > 23 || mm < 0 || mm > 59 || ss < 0.0 || ss >= 60.0)
			throw new IllegalArgumentException("Invalid time: " 
				+ hh + ":" + mm + ":" + ss);
		time = (double)hh + ((double)mm / 60.0) + ss / 3600.0;
	}

	/**
	 * Create a time given the number of hours and fractions thereof.
	 * @param t The number of hours and fractions thereof.
	 * @exception An IllegalArgumentException is thrown if
	 * 		t < 0.0 or t >= 24.0.
	 */
	public Time (double t) {
		if (t < 0.0 || t >= 24.0)
		throw new IllegalArgumentException("Invalid time format: " + t);
		time = t;
	}
	
	/**
	 * Return the time as hours and fractions thereof.
	 */
	public double getTime() {
		return time;
	}
	
	/**
	 * Return the number of hours in this time.
	 */
	public int getHours() {
		return (int)time;
	}

	/**
	 * Return the number of minutes in this hour of the time.
	 */
	public int getMinutes() {
		return (int)((time - getHours()) * 60.0);
	}
	
	/**
	 * Return the number of seconds in this minute of this hour of the time.
	 */
	public double getSeconds() {
		double min = (time - ((int)time)) * 60.0;
		return (min - ((int)min)) * 60.0; 
	}
	
	/**
	 * Return the time as a string in the form "hh:mm:ss.ssss".
	 */
	public String toString() {
		return getHours() + ":" + getMinutes() + ":" + getSeconds();
	}

	/**
	 * Form a unit test.
	 */	
	public static void main(String[] args) {
		Time t = new Time (22, 45, 55.6789); System.out.println("Time is " + t);
		t = new Time (0, 0, 0.0); System.out.println("Time is " + t);
		t = new Time (0, 0, 0.1); System.out.println("Time is " + t);
		t = new Time (23, 59, 59.999999); System.out.println("Time is " + t);
		t = new Time (0.0); System.out.println("Time is " + t);
		t = new Time (1.0); System.out.println("Time is " + t);
		t = new Time (23.0); System.out.println("Time is " + t);
		t = new Time (23.999999); System.out.println("Time is " + t);
		try {
			t = new Time (24.0); System.out.println("Time is " + t);
		} catch (IllegalArgumentException err) {
			System.out.println(err.toString());
		}
		try {
			t = new Time (24, 0, 0.0); System.out.println("Time is " + t);
		} catch (IllegalArgumentException err) {
			System.out.println(err.toString());
		}
		try {
			t = new Time (-24.0); System.out.println("Time is " + t);
		} catch (IllegalArgumentException err) {
			System.out.println(err.toString());
		}
		try {
			t = new Time (-1, 0, 0.0); System.out.println("Time is " + t);
		} catch (IllegalArgumentException err) {
			System.out.println(err.toString());
		}
	}
}
