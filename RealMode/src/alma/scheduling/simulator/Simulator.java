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
 * File Simulator.java
 */
 
package ALMA.scheduling.simulator;

import java.util.logging.Logger;


/**
 * Description 
 * 
 * @version 1.00  May 21, 2003
 * @author Allen Farris
 */
public class Simulator {

	/**
	 * The simulation mode.
	 */
	private Mode mode;
	
	/**
	 * The container that controls the components.
	 */
	private Container container;

	/**
	 * 
	 */
	public Simulator(Mode mode) {
		this.mode = mode;
		container = new Container (mode);
	}

	/**
	 * @return
	 */
	public Container getContainer() {
		return container;
	}

	/**
	 * @return
	 */
	public Mode getMode() {
		return mode;
	}
	
	public static void main(String[] args) {
		try {
			Simulator simulator = new Simulator(Mode.FULL);
			Logger log = simulator.getContainer().getLogger();
			log.info("simulator.begin");
			ExecutiveSimulator executive = null;
			executive = (ExecutiveSimulator)(simulator.getContainer().getComponent(Container.EXECUTIVE));
			executive.initialize();
			executive.execute();
			// ...
			
			// ...
			log.info("simulator.end");
			simulator.getContainer().cleanUp();
		} catch (Exception err) {
			err.printStackTrace();
			System.exit(0);
		}
			
	}

}
