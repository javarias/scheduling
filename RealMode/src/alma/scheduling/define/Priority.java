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
 * File Priority.java
 */
 
package alma.scheduling.define;

/**
 * The Priority class implements a priority that varies
 * from 1 (lowest) to 10 (highest).
 * 
 * @version 1.00  Jun 4, 2003
 * @author Allen Farris
 */
public class Priority {

	static public final Priority HIGHEST		= new Priority (10);
	static public final Priority HIGHER			= new Priority (9);
	static public final Priority HIGH			= new Priority (8);
	static public final Priority MEDIUMPLUS		= new Priority (7);
	static public final Priority MEDIUM			= new Priority (6);
	static public final Priority MEDIUMMINUS	= new Priority (5);
	static public final Priority LOW			= new Priority (4);
	static public final Priority LOWER			= new Priority (3);
	static public final Priority LOWEST			= new Priority (2);
	static public final Priority BACKGROUND		= new Priority (1);
	
	private int priority;
	private boolean isConstant;
	
	private Priority(int priority) {
		this.priority = priority;
		this.isConstant = true;
	}
	
	/**
	 * Create a Status object from a previously existing Status
	 * object, which will usually be one of the static public final 
	 * Status objects.
	 */
	public Priority (Priority x) {
		this.priority = x.priority;
		this.isConstant = false;
	}
	
	/**
	 * Change the state of this Status object.
	 */
	public void setPriority (Priority  x) {
		// Final objects are not allowed to be changed.
		if (isConstant)
			throw new UnsupportedOperationException(
			"Cannot change the state of a constant object.");
		this.priority  = x.priority ;
	}
	
	/**
	 * Return the current state of this object as a string.
	 */
	public String getPriority() {
		switch (priority) {
			case 10: return "highest";
			case  9: return "higher";
			case  8: return "high";
			case  7: return "mediumPlus";
			case  6: return "medium";
			case  5: return "mediumMinus";
			case  4: return "low";
			case  3: return "lower";
			case  2: return "lowest";
			case  1: return "background";
			default: return "***";
		}
	}
	
	/**
	 * Return true if this state is equal to the specified state.
	 */
	public boolean equals(Priority x) {
		return this.priority == x.priority;
	}
	
	/**
	 * Return this priority object as a string.
	 */
	public String toString() {
		return getPriority();
	}

	/**
	 * Provide a unit test.
	 */
	public static void main(String[] args) {
		Priority p = null;
		p = new Priority(Priority.HIGHEST); System.out.println("SCHEDULING: priority is " + p);
		p = new Priority(Priority.HIGHER); System.out.println("SCHEDULING: priority is " + p);
		p = new Priority(Priority.HIGH); System.out.println("SCHEDULING: priority is " + p);
		p = new Priority(Priority.MEDIUMPLUS); System.out.println("SCHEDULING: priority is " + p);
		p = new Priority(Priority.MEDIUM); System.out.println("SCHEDULING: priority is " + p);
		p = new Priority(Priority.MEDIUMMINUS); System.out.println("SCHEDULING: priority is " + p);
		p = new Priority(Priority.LOW); System.out.println("SCHEDULING: priority is " + p);
		p = new Priority(Priority.LOWER); System.out.println("SCHEDULING: priority is " + p);
		p = new Priority(Priority.LOWEST); System.out.println("SCHEDULING: priority is " + p);
		p = new Priority(Priority.BACKGROUND); System.out.println("SCHEDULING: priority is " + p);
		
		System.out.println("SCHEDULING: is priority low? " + p.equals(Priority.LOW));
		System.out.println("SCHEDULING: is priority background? " + p.equals(Priority.BACKGROUND));
		p.setPriority(Priority.LOW);
		System.out.println("SCHEDULING: priority is " + p);
		System.out.println("SCHEDULING: is priority low? " + p.equals(Priority.LOW));
		System.out.println("SCHEDULING: is priority background? " + p.equals(Priority.BACKGROUND));
		p.setPriority(Priority.MEDIUM);
		System.out.println("SCHEDULING: priority is " + p);
		p.setPriority(Priority.MEDIUMPLUS);
		System.out.println("SCHEDULING: priority is " + p);
		try {
			System.out.println("SCHEDULING: priority is " + Priority.HIGHEST);
			HIGHEST.setPriority(Priority.LOWEST);
		} catch (UnsupportedOperationException err) {
			System.out.println("SCHEDULING:"+ err.toString());
		}
		System.out.println("SCHEDULING: priority is " + Priority.HIGHEST);
	}
}


