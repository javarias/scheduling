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
 * File Status.java
 */
 
package ALMA.scheduling.define;

import alma.entity.xmlbinding.obsproject.types.SchedStatusT;

/**
 * Description 
 * 
 * @version 1.00  Jun 5, 2003
 * @author Allen Farris
 */
public class Status {
	static public final Status WAITING	= new Status(0);
	static public final Status READY	= new Status(1);
	static public final Status RUNNING	= new Status(2);
	static public final Status ABORTED	= new Status(3);
	static public final Status COMPLETE	= new Status(4);
	
	private int status;
	private boolean isConstant;
	
	private Status(int status) {
		this.status = status;
		this.isConstant = true;
	}
	
	/**
	 * Create a Status object from a previously existing Status
	 * object, which will usually be one of the static public final 
	 * Status objects.
	 */
	public Status (Status x) {
		this.status = x.status;
		this.isConstant = false;
	}

    /**
     * Create a Status object from a Status object from 
     * alma.entity.xmlbinding.obsproject.types.SchedStatusT;
     */
    public Status(SchedStatusT stat) {
        if(stat == SchedStatusT.WAITING) {
            this.status = 0;
        } else if(stat == SchedStatusT.READY) {
            this.status = 1;
        } else if(stat == SchedStatusT.RUNNING) { 
            this.status = 2;
        } else if(stat == SchedStatusT.ABORTED) {
            this.status = 3;
        } else if(stat == SchedStatusT.COMPLETED) {
            this.status = 4;
        }
    }
	
	/**
	 * Change the state of this Status object.
	 */
	public void setStatus(Status x) {
		// Final objects are not allowed to be changed.
		if (isConstant)
			throw new UnsupportedOperationException(
			"Cannot change the state of a constant object.");
		this.status = x.status;
	}
	
	/**
	 * Return the current state of this object as a string.
	 */
	public String getStatus() {
		switch (status) {
			case 0: return "waiting";
			case 1: return "ready";
			case 2: return "running";
			case 3: return "aborted";
			case 4: return "complete";
			default: return "***";
		}
	}

    /**
     * Returns the SchedStatus equivalent
     */
    public SchedStatusT getSchedStatus() {
        if(status == 0) {
            return SchedStatusT.WAITING;
        } else if(status == 1 ){
            return SchedStatusT.READY;
        } else if(status == 2 ){ 
            return SchedStatusT.RUNNING;
        } else if(status == 3 ) {
            return SchedStatusT.ABORTED;
        } else if(status  == 4) {
            return SchedStatusT.COMPLETED;
        }
        return null;
    }
	
	/**
	 * Return true if this state is equal to the specified state.
	 */
	public boolean equals(Status x) {
		return this.status == x.status;
	}
	
	/**
	 * Return this state object as a string.
	 */
	public String toString() {
		return getStatus();
	}

	/**
	 * Provide a unit test.
	 */
	public static void main(String[] args) {
		Status s = new Status(Status.WAITING);
		System.out.println("SCHEDULING: state is " + s);
		s.setStatus(Status.READY);
		System.out.println("SCHEDULING: state is " + s);
		s.setStatus(Status.RUNNING);
		System.out.println("SCHEDULING: state is " + s);
		System.out.println("SCHEDULING: is state running? " + s.equals(Status.RUNNING));
		System.out.println("SCHEDULING: is state complete? " + s.equals(Status.COMPLETE));
		s.setStatus(Status.COMPLETE);
		System.out.println("SCHEDULING: state is " + s);
		System.out.println("SCHEDULING: is state RUNNING? " + s.equals(Status.RUNNING));
		System.out.println("SCHEDULING: is state COMPLETE? " + s.equals(Status.COMPLETE));
		s.setStatus(Status.ABORTED);
		System.out.println("SCHEDULING: state is " + s);
		s.setStatus(Status.READY);
		System.out.println("SCHEDULING: state is " + s);
		try {
			System.out.println("SCHEDULING: state is " + Status.WAITING);
			WAITING.setStatus(Status.COMPLETE);
		} catch (UnsupportedOperationException err) {
			System.out.println("SCHEDULING: "+err.toString());
		}
		System.out.println("SCHEDULING: state is " + Status.WAITING);
	}
}


