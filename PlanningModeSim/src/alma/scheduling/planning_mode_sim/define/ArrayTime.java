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
 * File ArrayTime.java
 */
 
package alma.scheduling.planning_mode_sim.define;

/**
 * ArrayTime is based on the definition of time from the 
 * OMG Time Service Specification.  It is the time that
 * controls the actual telescope.  Internally, the number
 * that is stored is the number of 100 ns since 
 * 1582-10-15 00:00:00. (See 
 * ftp://ftp.omg.org/pub/docs/formal/97-12-21.pdf)
 * 
 * @version 1.00  Jun 6, 2003
 * @author Allen Farris
 */
public class ArrayTime {

	private long time;

	/**
	 * Create an ArrayTime from a long.
	 */
	public ArrayTime(long time) {
		this.time = time;
	}

	/**
	 * Return the value of this array time as an DateTime object.
	 * @return
	 */
	public DateTime asDateTime() {
		return null;
	}

	/**
	 * Get the value of the array time as a long.
	 * @return
	 */
	public long getValue() {
		return time;
	}

}
