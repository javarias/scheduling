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
 * File ComponentState.java
 */
 
package ALMA.scheduling.define;

/**
 * The State class describes the state of the scheduling system.  The
 * states are new, initialized, executing, stopped, and error.
 * 
 * @version 1.00 May 2, 2003
 * @author Allen Farris
 */
public class ComponentState {
	static public final ComponentState NEW			= new ComponentState(0);
	static public final ComponentState INITIALIZED	= new ComponentState(1);
	static public final ComponentState EXECUTING	= new ComponentState(2);
	static public final ComponentState STOPPED		= new ComponentState(3);
	static public final ComponentState ERROR		= new ComponentState(4);
	
	private int state;
	private boolean isConstant;
	
	private ComponentState(int state) {
		this.state = state;
		this.isConstant = true;
	}
	
	/**
	 * Create a State object from a previously existing State
	 * object, which will usually be one of the static public final 
	 * State objects.
	 */
	public ComponentState (ComponentState x) {
		this.state = x.state;
		this.isConstant = false;
	}
	
	/**
	 * Change the state of this State object.
	 */
	public void setState(ComponentState x) {
		// Final objects are not allowed to be changed.
		if (isConstant)
			throw new UnsupportedOperationException(
			"Cannot change the state of a constant object.");
		this.state = x.state;
	}
	
	/**
	 * Return the current state of this object as a string.
	 */
	public String getState() {
		switch (state) {
			case 0: return "new";
			case 1: return "initialized";
			case 2: return "executing";
			case 3: return "stopped";
			case 4: return "error";
			default: return "***";
		}
	}
	
	/**
	 * Return true if this state is equal to the specified state.
	 */
	public boolean equals(ComponentState x) {
		return this.state == x.state;
	}
	
	/**
	 * Return this state object as a string.
	 */
	public String toString() {
		return getState();
	}

	/**
	 * Provide a unit test.
	 */
	public static void main(String[] args) {
		ComponentState s = new ComponentState(NEW);
		System.out.println("state is " + s);
		s.setState(INITIALIZED);
		System.out.println("state is " + s);
		s.setState(EXECUTING);
		System.out.println("state is " + s);
		System.out.println("is state EXECUTING? " + s.equals(EXECUTING));
		System.out.println("is state STOPPED? " + s.equals(STOPPED));
		s.setState(STOPPED);
		System.out.println("state is " + s);
		System.out.println("is state EXECUTING? " + s.equals(EXECUTING));
		System.out.println("is state STOPPED? " + s.equals(STOPPED));
		s.setState(ERROR);
		System.out.println("state is " + s);
		s.setState(INITIALIZED);
		System.out.println("state is " + s);
		try {
			System.out.println("state is " + NEW);
			NEW.setState(STOPPED);
		} catch (UnsupportedOperationException err) {
			System.out.println(err.toString());
		}
		System.out.println("state is " + NEW);
	}
}


