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
 * File Control.java
 */
 
package alma.Scheduling.Define;

/**
 * The Control interface specifies those methods needed by Scheduling
 * from the Control system.  The methods are implemented by the ALMAControl
 * and by the ControlSimulator. 
 * 
 * @version 1.30 May 10, 2004
 * @author Allen Farris
 */
public interface Control {

	/**
	 * Execute the selected SB with the specified identifier on the specified 
	 * sub-array at the specified time.
	 * @param subarrayId The subarray on which to execute the SB.
	 * @param best The array of choices to be executed.
	 * @param time The time at which the SB is to be started.
	 */
	public void execSB(short subarrayId, BestSB best, DateTime time)
		throws SchedulingException;

	/**
	 * Execute the selected SB with the specified identifier on the specified 
	 * sub-array immediately.
	 * @param subarrayId The subarray on which to execute the SB.
	 * @param best The array of choices to be executed.
	 */
	public void execSB(short subarrayId, BestSB best)
		throws SchedulingException;

	/**
	 * Stop the currently executing SB.
	 * @param subarrayId The subarray on which the SB is currently executing.
	 * @param id The entity-id of the SB to stop.
	 */
	public void stopSB(short subarrayId, String id)
		throws SchedulingException;
	
	/**
	 * Create a sub-array.
	 * @param antenna The list of antennas that are to make up the sub-array.
	 * @return The id of the newly created sub-array. 
	 */
	public short createSubarray(short[] antenna)
		throws SchedulingException;
	
	/**
	 * Destroy a current sub-array.
	 * @param subarrayId The id of the sub-array to be destroyed. 
	 */
	public void destroySubarray(short subarrayId)
		throws SchedulingException;

	/**
	 * Get the current active sub-arrays.
	 * @return The list of sub-array ids that are currently active.  This number
	 * may be zero. 
	 */
	public short[] getActiveSubarray()
		throws SchedulingException;
	
	/**
	 * Get the list of antennas that are currently idle, but on-line. 
	 * @return The list of antenna ids that are currently idle, but on-line.  
	 * This number may be zero.
	 */
	public short[] getIdleAntennas()
		throws SchedulingException;
	
	/**
	 * Get the list of antennas currently allocated to the specified sub-array.
	 * @param subarrayId The sub-array of interest.
	 * @return The list of antenna ids currently allocated to the specified sub-array.
	 */
	public short[] getSubarrayAntennas(short subarrayId)
		throws SchedulingException;

}
