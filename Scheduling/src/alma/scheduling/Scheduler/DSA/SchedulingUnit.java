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
 * File SchedulingUnit.java
 */
package alma.scheduling.Scheduler.DSA;

/**
 * The SchedulingUnit class is an abstract class that contains methods that
 * are common to all particular types of scheduling units used by the various
 * dynamic scheduling algorithms.  The data contained in this class and its
 * extensions are, basically, a reference to the scheduling
 * unit plus visibility data (LST at maximum elevation, rise and setting 
 * times) and the various factors that are computed whenever the 
 * policy computes the score, rank, and success factors. 
 * 
 * @version $Id: SchedulingUnit.java,v 1.3 2004/11/23 21:22:07 sslucero Exp $
 * @author Allen Farris
 */
abstract class SchedulingUnit {

	/**
	 * 
	 */
	public SchedulingUnit() {
		super();
		// TODO Auto-generated constructor stub
	}

}
