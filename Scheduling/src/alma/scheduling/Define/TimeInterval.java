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
 * File TimeInterval.java
 */
 
package alma.Scheduling.Define;

/**
 * The TimeInterval is a utility class used to define an interval of time
 * between two DateTime objects. 
 * 
 * @version 1.30 May 10, 2004
 * @author Allen Farris
 */
public class TimeInterval {
	private DateTime start;
	private DateTime end;
	
	/**
	 * Define an interval of time given a starting time and a duration in 
	 * seconds.
	 * @param time The starting time.
	 * @param seconds The length of the interval in seconds.
	 */
	public TimeInterval(DateTime time, int seconds) {
		if (seconds < 0)
			throw new IllegalArgumentException(
				"Seconds cannot be less than zero.");
		this.start = time;
		this.end = DateTime.add(time,seconds);

	}
	
	/**
	 * Define an interval of time given the starting and ending time.
	 * @param start The starting time.
	 * @param end The ending time.
	 */
	public TimeInterval(DateTime start, DateTime end) {
		if (end.ge(start)) {
			this.start = start;
			this.end = end;
		} else {
			this.start = end;
			this.end = start;
		}
	}
	
	/**
	 * Define a duration of time in units of seconds.  In this case the 
	 * starting time is 0.
	 * @param seconds The length of the interval of time in seconds.
	 */
	public TimeInterval(int seconds) {
		if (seconds < 0)
			throw new IllegalArgumentException(
				"Seconds cannot be less than zero.");
		this.start = new DateTime();
		this.end = DateTime.add(start,seconds);
	}
	
	/**
	 * Create a new Interval that is a copy of the specofied interval.
	 */
	public TimeInterval(TimeInterval interval) {
		start = new DateTime(interval.start);
		end = new DateTime(interval.end);
	}

	/**
	 * Returns the length of the interval in seconds.
	 * @return int
	 */
	public int getLength() {
		return (int)((end.getJD() - start.getJD()) * 86400.0);
	}

	/**
	 * Returns the starting time.
	 * @return Time
	 */
	public DateTime getStart() {
		return new DateTime(start);
	}

	/**
	 * Returns the ending time.
	 * @return STime
	 */
	public DateTime getEnd() {
		return new DateTime(end);
	}
	
	/**
	 * Provide a toString method.
	 */
	public String toString() {
		return start.toString() + " - " + getEnd() + " (" + getLength() + " sec)";
	}

	/**
	 * A unit test.
	 */
	public static void main(String[] args) {
		TimeInterval x = new TimeInterval(new DateTime(2002,8,15,23,45,0), 1800);
		System.out.println("start: " + x.getStart() + " end: " + x.getEnd() + 
						   " duration: " + x.getLength() + " sec");
		System.out.println("x: " + x);
		x = new TimeInterval(new DateTime(2002,8,15,23,45,0),new DateTime(2002,8,16,1,15,0));
		System.out.println("x: " + x);
		x = new TimeInterval(150);
		System.out.println("x: " + x);
	}
	
}
