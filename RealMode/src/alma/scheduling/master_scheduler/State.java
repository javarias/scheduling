/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * All rights reserved
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 * 
 * File State.java
 * 
 */
package ALMA.scheduling.master_scheduler;

/**
 * The State class describes the state of the scheduling system.  The
 * states are new, initialized, executing, stopped, and error.
 * 
 * @version 1.00 May 2, 2003
 * @author Allen Farris
 */
public class State {
	static public final State NEW			= new State(0);
	static public final State INITIALIZED	= new State(1);
	static public final State EXECUTING		= new State(2);
	static public final State STOPPED		= new State(3);
	static public final State ERROR			= new State(4);
	
	private int state;
	private boolean isConstant;
	
	private State(int state) {
		this.state = state;
		this.isConstant = true;
	}
	
	/**
	 * Create a State object from a previously existing State
	 * object, which will usually be one of the static public final 
	 * State objects.
	 */
	public State (State x) {
		this.state = x.state;
		this.isConstant = false;
	}
	
	/**
	 * Change the state of this State object.
	 */
	public void setState(State x) {
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
	public boolean equals(State x) {
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
		State s = new State(State.NEW);
		System.out.println("state is " + s);
		s.setState(State.INITIALIZED);
		System.out.println("state is " + s);
		s.setState(State.EXECUTING);
		System.out.println("state is " + s);
		System.out.println("is state EXECUTING? " + s.equals(State.EXECUTING));
		System.out.println("is state STOPPED? " + s.equals(State.STOPPED));
		s.setState(State.STOPPED);
		System.out.println("state is " + s);
		System.out.println("is state EXECUTING? " + s.equals(State.EXECUTING));
		System.out.println("is state STOPPED? " + s.equals(State.STOPPED));
		s.setState(State.ERROR);
		System.out.println("state is " + s);
		s.setState(State.INITIALIZED);
		System.out.println("state is " + s);
		try {
			System.out.println("state is " + State.NEW);
			NEW.setState(State.STOPPED);
		} catch (UnsupportedOperationException err) {
			System.out.println(err.toString());
		}
		System.out.println("state is " + State.NEW);
	}
}


