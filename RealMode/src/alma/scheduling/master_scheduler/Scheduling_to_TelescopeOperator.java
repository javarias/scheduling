/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
 * All rights reserved
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307  USA
 * 
 * File Scheduling_to_TelescopeOperator.java
 * 
 */
package alma.scheduling.master_scheduler;

/**
 * This interface is implemented in the TelescopeOperator class using
 * interfaces in the Executive subsystem. (See ICD sectio 3.4)  This 
 * interface is used by the MasterScheduler and Schedulers to send 
 * information to the telescope operator. In some cases replies are 
 * expected.
 * 
 * @version 1.00 Mar 12, 2003
 * @author Allen Farris
 */
public interface Scheduling_to_TelescopeOperator {

	/**
	 * Send a message to the Telescope Operator. No reply is needed.
	 * @param message		the message being sent to the telescope operator
	 */
	void send (String message);

	/**
	 * Select the next scheduling block to be executed from a list of SBs.
	 * There is a configurable timeout associated with this request.  
	 * If there is no reply, the first Scheduling block in the list is 
	 * executed.  A reply must include scheduling block id from this 
	 * submitted list.
	 * @param sbIdList	the list of SBs from which the selection is to be made
	 * @param messageId	A unique identifier that identifies the message 
	 * 				being sent.  It must be included in the reply.
     * @return String The unique identifier of the selected SB.
	 */
	String selectSB (String[] sbIdList, String messageId);

	/**
	 * Can a particular antenna be placed in active mode?  
	 * The reply is yes / no.
	 * @param antennaId	The antenna about which the inquiry is made
	 * @param messageId	A unique identifier that identifies the message 
	 *  				being sent.  It must be included in the reply.  
	 */
	void confirmAntennaActive (short antennaId, String messageId);

	/**
	 * Is it OK to create a sub-array?
	 * There is a configurable timeout associated with this request.  
	 * If there is no reply, the answer is assumed to be yes.  The
	 * reply is yes / no.
	 * @param antennaIdList 	the antennas in the sub-array to be created
	 * @param messageId		A unique identifier that identifies the message 
	 * 					being sent.  It must be included in the reply.
	 */
	void confirmSubarrayCreation (short[] antennaIdList, String messageId);
	
}

