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
 * File PipelineStatus.java
 */
 
package alma.scheduling.project_manager;

/**
 * Define the status of a pipeline processing request. 
 * 
 * @version 1.00  Jun 3, 2003
 * @author Allen Farris
 */
public class PipelineStatus {
	static public final PipelineStatus COMPLETE = new PipelineStatus (0);
	static public final PipelineStatus RUNNING 	= new PipelineStatus (1);
	static public final PipelineStatus QUEUED 	= new PipelineStatus (2);

	private int status;
	private boolean isConstant;

	// Used to construct static objects.
	private PipelineStatus(int status) {
		this.status = status;
		this.isConstant = true;
	}

	/**
	 * Create a State object from a previously existing State
	 * object, which will usually be one of the static public final 
	 * State objects.
	 */
	public PipelineStatus (PipelineStatus x) {
		this.status = x.status;
		this.isConstant = false;
	}
	
	/**
	 * Change the state of this status object.
	 */
	public void setStatus(PipelineStatus x) {
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
			case 0: return "complete";
			case 1: return "running";
			case 2: return "queued";
			default: return "***";
		}
	}
	
	/**
	 * Return true if this state is equal to the specified state.
	 */
	public boolean equals(PipelineStatus x) {
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
		PipelineStatus s = new PipelineStatus(PipelineStatus.COMPLETE);
		System.out.println("status is " + s);
		s.setStatus(PipelineStatus.RUNNING);
		System.out.println("status is " + s);
		s.setStatus(PipelineStatus.QUEUED);
		System.out.println("status is " + s);
		System.out.println("is status RUNNING? " + s.equals(PipelineStatus.RUNNING));
		System.out.println("is status QUEUED? " + s.equals(PipelineStatus.QUEUED));
		try {
			System.out.println("status is " + PipelineStatus.COMPLETE);
			COMPLETE.setStatus(PipelineStatus.RUNNING);
		} catch (UnsupportedOperationException err) {
			System.out.println(err.toString());
		}
		System.out.println("status is " + PipelineStatus.COMPLETE);
	}
}
