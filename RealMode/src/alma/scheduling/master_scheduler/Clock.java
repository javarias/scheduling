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
 * File CLock.java
 */
 
package alma.scheduling.master_scheduler;

import alma.scheduling.define.STime;
import alma.scheduling.define.ArrayTime;
/**
 * Description 
 * 
 * @version 1.00  Jun 3, 2003
 * @author Allen Farris
 */
public interface Clock {

	/**
	 * Return the current time as an STime object.  This time is
	 * an approximation to the array time.  It uses the current system
	 * time and adds a delta to it to synchronize it to the array time.
	 * @return The current time as an STime object.
	 */
	public STime getSTime();

	/**
	 * Return the array time.  This time is from the control system,
	 * if this is real; if it is a simulation, then it is from the
	 * clock simulator.
	 * @return The current time as an ArrayTime object.
	 */
	public ArrayTime getArrayTime();

	/**
	 * Synchronize the currrent system time and the array time.
	 */
	public void synchronize();

	/**
	 * Set an alarm, to go off at a particular time.
	 * @param time		The time at which the alarm goes off.
	 * @param listener	The listener method to call to process the alarm.
	 */
	public void setAlarm(STime time, ClockAlarmListener listener);

	/**
	 * Set an alarm, to go off at N seconds from now.
	 * @param seconds	The number of seconds to wait for the alarm to go off.
	 * @param listener	The listener method to call to process the alarm.
	 */
	public void setAlarm(int seconds, ClockAlarmListener listener);

}
