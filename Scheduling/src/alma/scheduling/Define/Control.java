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
 
package alma.scheduling.Define;

import alma.Control.CorrelatorType;

/**
 * The Control interface specifies those methods needed by Scheduling
 * from the Control system.  The methods are implemented by the ALMAControl
 * and by the ControlSimulator. 
 * 
 * @version $Id: Control.java,v 1.14 2010/02/19 23:25:07 rhiriart Exp $
 * @author Allen Farris
 */
public interface Control {

	/**
	 * Execute the selected SB with the specified identifier on the specified 
	 * sub-array at the specified time.
	 * @param name The array on which to execute the SB.
	 * @param best The array of choices to be executed.
	 * @param time The time at which the SB is to be started.
	 */
	public void execSB(String name, BestSB best, DateTime time)
		throws SchedulingException;

	/**
	 * Execute the selected SB with the specified identifier on the specified 
	 * sub-array immediately.
	 * @param name The array on which to execute the SB.
	 * @param best The array of choices to be executed.
	 */
	public void execSB(String name, BestSB best)
		throws SchedulingException;

	/**
	 * Execute the selected SB with the specified identifier on the specified 
	 * sub-array immediately.
	 * @param name The array on which to execute the SB.
	 * @param bestSB The string id of the best sb to schedule now
     */
	public void execSB(String name, String bestSB)
		throws SchedulingException;
	 

       
	/**
	 * Stop the currently executing SB.
	 * @param name The array on which the SB is currently executing.
	 * @param id The entity-id of the SB to stop.
	 */
	public void stopSB(String name, String id)
		throws SchedulingException;
	/**
	 * Aborts the currently executing SB.
	 * @param name The array on which the SB is currently executing.
	 * @param id The entity-id of the SB to stop.
	 */
	public void stopSBNow(String name, String id)
		throws SchedulingException;
	
	
	/**
	 * Create a sub-array.
	 * @param antenna The list of antennas that are to make up the sub-array.
	 * @return The id of the newly created sub-array. 
	 */
	public String createArray(String[]       antenna,
			                  String[]       photonics,
			                  CorrelatorType correlatorType,
			                  String         mode)
		throws SchedulingException;
	
	/**
	 * Destroy a current sub-array.
	 * @param name The id of the sub-array to be destroyed. 
	 */
	public void destroyArray(String name)
		throws SchedulingException;

	/**
	 * Get the current active sub-arrays.
	 * @return The list of sub-array ids that are currently active.  This number
	 * may be zero. 
	 */
	public String[] getActiveArray()
		throws SchedulingException;
	
	/**
	 * Get the list of antennas that are currently idle, but on-line. 
	 * @return The list of antenna ids that are currently idle, but on-line.  
	 * This number may be zero.
	 */
	public String[] getIdleAntennas()
		throws SchedulingException;
	
	/**
	 * Get the list of antennas currently allocated to the specified sub-array.
	 * @param name The array of interest.
	 * @return The list of antenna ids currently allocated to the specified sub-array.
	 */
	public String[] getArrayAntennas(String name)
		throws SchedulingException;

    public void setAntennaOfflineNow(String antennaId) 
        throws SchedulingException;

    public void setAntennaOnlineNow(String antennaId) 
        throws SchedulingException;
    /**
      * Stop all scheduling currently happening!
      */
    public void stopAllScheduling() throws SchedulingException ;
    
}
