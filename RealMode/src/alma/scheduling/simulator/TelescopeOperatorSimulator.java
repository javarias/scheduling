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
 * File TelescopeOperatorSimulator.java
 */
 
package ALMA.scheduling.simulator;

import ALMA.scheduling.master_scheduler.Scheduling_to_TelescopeOperator;

/**
 * Description 
 * 
 * @version 1.00  Jul 18, 2003
 * @author Allen Farris
 */
public class TelescopeOperatorSimulator
	implements Scheduling_to_TelescopeOperator {

	/**
	 * 
	 */
	public TelescopeOperatorSimulator() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.Scheduling_to_TelescopeOperator#send(java.lang.String)
	 */
	public void send(String message) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.Scheduling_to_TelescopeOperator#SelectSB(java.lang.String[], java.lang.String)
	 */
	public void SelectSB(String[] sbIdList, String messageId) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.Scheduling_to_TelescopeOperator#confirmAntennaActive(short, java.lang.String)
	 */
	public void confirmAntennaActive(short antennaId, String messageId) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see alma.scheduling.master_scheduler.Scheduling_to_TelescopeOperator#comfirmSubarrayCreation(short[], java.lang.String)
	 */
	public void comfirmSubarrayCreation(
		short[] antennaIdList,
		String messageId) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
	}
}
