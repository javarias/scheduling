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
 
package alma.scheduling.planning_mode_sim.define;

/**
 * The State class describes the state of the scheduling system.  The
 * states are: 
 * <ul>
 * 		<li> NEW
 * 		<li> INITIALIZED
 * 		<li> EXECUTING
 * 		<li> STOPPED
 * 		<li> ERROR
 * </ul>
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
	
	static private final int NA						= 0;
	static private final int IDLE					= 1;
	static private final int BUSY					= 2;
	
	private int state;
	private int mode;
	private boolean isConstant;
	
	private ComponentState(int state) {
		this.state = state;
		this.isConstant = true;
		this.mode = NA;
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
	 * Change the state of this component to EXECUTING and BUSY.
	 */
	public void setBusy() {
		if (this.state == EXECUTING.state)
			this.mode = BUSY;
		else {
			this.state = EXECUTING.state;
			this.mode = BUSY;
		}
	}

	/**
	 * Change the state of this component to EXECUTING and IDLE.
	 */
	public void setIdle() {
		if (this.state == EXECUTING.state)
			mode = IDLE;
		else {
			this.state = EXECUTING.state;
			mode = IDLE;
		}
	}
	
	/**
	 * Is this component EXECUTING and BUSY?
	 * @return true if and only if this component is executing and busy.
	 */
	public boolean isBusy() {
		return (this.state == EXECUTING.state && mode == BUSY);
	}
	
	/**
	 * Is this component EXECUTING and IDLE?
	 * @return true if and only if this component is executing and idle.
	 */
	public boolean isIdle() {
		return (this.state == EXECUTING.state && mode == IDLE);
	}
	
	/**
	 * Return the current state of this object as a string.
	 */
	public String getState() {
		switch (state) {
			case 0: return "new";
			case 1: return "initialized";
			case 2: if (mode == BUSY) 
						return "executing-busy";
					else if (mode == IDLE)
						return "executing-idle";
					else
						return "executing";
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
		s.setState(EXECUTING);
		System.out.println("state is " + s);
		System.out.println("is state EXECUTING? " + s.equals(EXECUTING));
		System.out.println("Is state busy? " + s.isBusy());
		System.out.println("Is state idle? " + s.isIdle());
		s.setBusy();
		System.out.println("state is " + s);
		System.out.println("is state EXECUTING? " + s.equals(EXECUTING));
		System.out.println("Is state busy? " + s.isBusy());
		System.out.println("Is state idle? " + s.isIdle());
		s.setIdle();
		System.out.println("state is " + s);
		System.out.println("is state EXECUTING? " + s.equals(EXECUTING));
		System.out.println("Is state busy? " + s.isBusy());
		System.out.println("Is state idle? " + s.isIdle());
	}
}


