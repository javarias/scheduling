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
 * File ObsUnitSet.java
 */
 
package alma.scheduling.planning_mode_sim.define;

import java.util.ArrayList;

/**
 * This is a substitute for the ObsUnitSet class that is really defined
 * in the ObsPrep subsystem. 
 * 
 * @version 1.00  Sep 3, 2003
 * @author Allen Farris
 */
public class ObsUnitSet {
	
	private String id;
	private DateTime timeSubmitted;
	private ArrayList member;

	/**
	 * Create an ObsUnitSet.
	 */
	public ObsUnitSet() {
		member = new ArrayList ();
	}
	
	public void addObsUnitSet(ObsUnitSet x) {
		member.add(x);
	}
	
	public void addSB(SchedBlock x) {
		member.add(x);
	}

	public Object[] getMember() {
		Object[] x = new Object [member.size()];
		x = member.toArray(x);
		return x;
	}
	
	/**
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return
	 */
	public DateTime getTimeSubmitted() {
		return timeSubmitted;
	}

	/**
	 * @param string
	 */
	public void setId(String string) {
		id = string;
	}

	/**
	 * @param time
	 */
	public void setTimeSubmitted(DateTime time) {
		timeSubmitted = time;
	}

}
