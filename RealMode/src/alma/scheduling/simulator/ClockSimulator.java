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
 * File ClockSimulator.java
 */
 
package ALMA.scheduling.simulator;

import ALMA.scheduling.define.ClockBase;
import ALMA.scheduling.define.STime;
import ALMA.scheduling.define.ArrayTime;

/**
 * Description 
 * 
 * @version 1.00  Jul 18, 2003
 * @author Allen Farris
 */
public class ClockSimulator extends ClockBase {

	/**
	 * 
	 */
	public ClockSimulator() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.Clock#getSTime()
	 */
	public STime getSTime() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.Clock#getArrayTime()
	 */
	public ArrayTime getArrayTime() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.Clock#synchronize()
	 */
	public void synchronize() {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
	}
}
