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
 * File ControlSimulator.java
 */
 
package ALMA.scheduling.simulator;

import ALMA.scheduling.define.STime;
import ALMA.scheduling.master_scheduler.ControlProxy;

/**
 * Description 
 * 
 * @version 1.00  Jul 18, 2003
 * @author Allen Farris
 */
public class ControlSimulator extends BasicComponent implements ControlProxy {

	private ClockSimulator clock;
	private WeatherModel weather;

	/**
	 * @param mode
	 */
	public ControlSimulator(Mode mode) {
		super(mode);
		clock = new ClockSimulator ();
		weather = new WeatherModel ();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.ControlProxy#sendToControl(java.lang.String, alma.scheduling.define.STime)
	 */
	public void sendToControl(String id, STime time) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		System.out.println("Unit test of control simulator.");
		ControlSimulator control = new ControlSimulator(Mode.FULL);
		System.out.println("End unit test of control simulator.");
	}
}

