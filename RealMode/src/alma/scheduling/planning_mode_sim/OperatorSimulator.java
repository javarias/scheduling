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
 * File OperatorSimulator.java
 */
 
package alma.scheduling.planning_mode_sim;

import alma.scheduling.define.Operator;
import alma.scheduling.define.Antenna;
import alma.scheduling.define.BestSB;
import alma.scheduling.planning_mode_sim.define.BasicComponent;
import alma.scheduling.planning_mode_sim.define.SimulationException;

/**
 * Description 
 * 
 * @version 1.00  Jan 5, 2004
 * @author Allen Farris
 */
public class OperatorSimulator
	extends BasicComponent
	implements Operator {
		
	// The wait time in seconds.
	private int waitTime;
	
	// The telescope.
	private TelescopeSimulator telescope;

	/**
	 * 
	 */
	public OperatorSimulator() {
		super();
		waitTime = 0;
	}

	// Lifecycle methods
	
	public void initialize() throws SimulationException {
		telescope = (TelescopeSimulator)containerServices.getComponent(Container.TELESCOPE);
		logger.info(instanceName + ".initialized");
	} 

	/* (non-Javadoc)
	 * @see alma.scheduling.define.OperatorProxy#setWaitTime(int)
	 */
	public void setWaitTime(int seconds) {
		waitTime = seconds;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.define.OperatorProxy#send(java.lang.String)
	 */
	public void send(String message) {
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.define.OperatorProxy#selectSB(java.lang.String[])
	 */
	public void selectSB(BestSB best) {
		if (best == null || best.getNumberReturned() == 0) {
			logger.severe("OperatorSimulator: entityId cannot be null or have zero length.");
			return;
		}
		// Leave the default selection.
		return;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.define.OperatorProxy#confirmAntennaActive(java.lang.String)
	 */
	public boolean confirmAntennaActive(int antennaId) {
		Antenna ant = telescope.getAntenna(antennaId);
		if (ant == null) {
			logger.severe("OperatorSimulator: No such antenna as " + antennaId);
			return false;
		}
		if (!ant.isManual())
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.define.OperatorProxy#confirmSubarrayCreation(java.lang.String[])
	 */
	public boolean confirmSubarrayCreation(int[] antennaId) {
		Antenna ant = null;
		for (int i = 0; i < antennaId.length; ++i) {
			ant = telescope.getAntenna(antennaId[i]);
			if (ant == null) {
				logger.severe("OperatorSimulator: No such antenna as " + antennaId[i]);
				return false;
			}
			if (ant.isAllocated())
				return false;
		}
		return true;
	}

}
