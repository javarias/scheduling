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
 */
package alma.scheduling.psm.sim;

import java.util.Date;

/**
 * Concrete implementation of TimeHandler that uses system time, and does not step forward time.
 * @author ahoffsta
 *
 */
public class TimeReal extends TimeHandler{
	
	protected TimeReal(){
	}
	
	public void setStartingDate(Date sd){
		logger.debug("Invocation of setStartingDate() method in a non-simulated environment");
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.planning_mode_sim.controller.TimeUtil#now()
	 */
	public Date getTime() {
		return new Date();
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.planning_mode_sim.controller.TimeUtil#step()
	 */
	public void step() {
		logger.debug("Invocation of step() method in a non-simulated environment");
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.planning_mode_sim.controller.TimeUtil#step(double)
	 */
	public void step(int time) {
		logger.debug("Invocation of step() method in a non-simulated environment");
	}

	@Override
	public void step(Date date) {
		logger.debug("Invocation of step() method in a non-simulated environment");		
	}

}
