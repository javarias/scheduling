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
 * File Operator.java
 */
 
package alma.Scheduling.Define;

/**
 * The Operator interface defines those methods needed by the Scheduling
 * Subsystem to communicate with the telescope operator. 
 * 
 * @version 1.30 May 10, 2004
 * @author Allen Farris
 */
public interface Operator {

	/**
	 * Configure the wait time associated with commands that have a timeout.
	 */
	public void setWaitTime(int seconds);
	
	/**
	 * Send a message; no reply is expected. 
	 * There is no timeout associated with this command.
	 * 
	 * @param message The message to be sent to the operator.
	 */
	public void send (String message);

	/**
	 * Select an SB from the specified list to be executed.
	 * If there is no reply within the timeout period, entityId[0] is assumed.
	 * 
	 * @param best The list of SBs under consideration to be executed.
	 * @return The SB from the specified list that is to be executed.
	 */	
	public void selectSB (BestSB best);
	
	/**
	 * Reply true if and only if the specified antenna is in manual mode and
	 * it can be placed in active mode.
	 * There is no timeout associated with this command.
	 * 
	 * @param antennaId The antenna under consideration.
	 * @return True if and only if the specified antenna is in manual mode and
	 * it can be placed in active mode.
	 */
	public boolean confirmAntennaActive (int antennaId);
	
	/**
	 * Reply true if and only it is OK to create a subarray out of the specified
	 * list of antennas.
	 * If there is no reply within the timeout period, true is assumed.
	 * 
	 * @param antennaId The list of antennas under consideration.
	 * @return True if and only it is OK to create a subarray out of the specified
	 * list of antennas.
	 */
	public boolean confirmSubarrayCreation (int[] antennaId);

}
