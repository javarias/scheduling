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
 * File Date.java
 */
 
package alma.Scheduling.Define;

/**
 * Record the date as year, month, and day.
 * The main use of this class is in conjunction with the DateTime class.
 * 
 * @version 1.00  Jun 11, 2003
 * @author Allen Farris
 */
public class Date {
	
	private int year;
	private int month;
	private int day;

	/**
	 * Create a Date.
	 * @param year the year, e.g., 2003. 
	 * @param month The month, varying from 1 to 12, e.g., 7.
	 * @param day  The day, varying from 1 to 31, at most.
	 * @exception An IllegalArgumentException is thrown if month or day is
	 * 		out of bounds.
	 */
	public Date(int year, int month, int day) {
		if (month < 1 || month > 12)
			throw new IllegalArgumentException ("Illegal value of month: " 
				+ year + "-" + month + "-" + day);
		if ( (day < 1 || day > 31) ||
			 ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) ||
		     (month == 2 && (day > ((DateTime.isLeapYear(year) ? 29 : 28)))) )
			throw new IllegalArgumentException ("Illegal value of day: "
				+ year + "-" + month + "-" + day);
		this.year = year;
		this.month = month;
		this.day = day;
	}
	
	/**
	 * Get the year associated with this date.
	 * @return the year associated with this date.
	 */
	public int getYear() {
		return year;
	}
	
	/**
	 * Get the month associated with this date, varying from 1 to 12.
	 * @return the month associated with this date, varying from 1 to 12.
	 */
	public int getMonth() {
		return month;
	}
	
	/**
	 * Get the day associated with this date, varying from 1 to, at most, 31.
	 * @return the day associated with this date, varying from 1 to, at most, 31.
	 */
	public int getDay() {
		return day;
	}

	/**
	 * Return this date as a string in the format "year-month-day".
	 */
	public String toString() {
		return year + "-" + month + "-" + day; 
	}

	public static void main(String[] args) {
		Date d = new Date (2003,7,2); System.out.println("The date is " + d + ".");
		d = new Date (1999,12,31); System.out.println("The date is " + d + ".");
		d = new Date (2000,2,29); System.out.println("The date is " + d + ".");
		try {
			d = new Date (2003,8,32); 
		} catch (IllegalArgumentException err) {
			System.out.println(err.toString());
		}
		try {
			d = new Date (2003,8,0); 
		} catch (IllegalArgumentException err) {
			System.out.println(err.toString());
		}
		try {
			d = new Date (2003,6,31); 
		} catch (IllegalArgumentException err) {
			System.out.println(err.toString());
		}
		try {
			d = new Date (2003,2,29); 
		} catch (IllegalArgumentException err) {
			System.out.println(err.toString());
		}
	}
}
