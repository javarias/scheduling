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
 * File ALMAClock.java
 */
 
package ALMA.scheduling.master_scheduler;

import alma.acs.container.ContainerServices;
import ALMA.scheduling.define.STime;
import ALMA.scheduling.define.ArrayTime;
import ALMA.scheduling.define.ClockBase;
import ALMA.scheduling.define.ClockAlarmListener;

/**
 * The Clock class gives access to the array time, as implemented by
 * the Control System, in the real mode.  In the simulation mode, this
 * class accesses the ClockSimulator in the simulation class.
 * 
 * @version 1.00 May 5, 2003
 * @author Allen Farris
 */
public class ALMAClock extends ClockBase {
	int delta;

	/**
	 * Create a clock object.
	 * @param isSimulation	True, if this a simulation.
	 * @param container The object that implements the container services.
	 */
	public ALMAClock (boolean isSimulation, ContainerServices container) {
		System.out.println("The Clock has been constructed.");
		synchronize();
	}
	
	
	/**
	 * Return the current time as an STime object.  This time is
	 * an approximation to the array time.  It uses the current system
	 * time and adds a delta to it to synchronize it to the array time.
	 * @return The current time as an STime object.
	 */
	public STime getSTime() {
		return null;
	}
	
	/**
	 * Return the array time.  This time is from the control system,
	 * if this is real; if it is a simulation, then it is from the
	 * clock simulator.
	 * @return The current time as an ArrayTime object.
	 */
	public ArrayTime getArrayTime() {
		return null;
	}

	/**
	 * Synchronize the currrent system time and the array time.
	 * This class gets the array time and then saves a delta to 
	 * convert from the system time to the array time.
	 */
	public void synchronize() {
		delta = 0;
	}

	/**
	 * Set an alarm, to go off at a particular time.
	 * @param time		The time at which the alarm goes off.
	 * @param listener	The listener method to call to process the alarm.
	 */
	public void setAlarm(STime time, ClockAlarmListener listener) {
		// Spawn a thread that sleeps until now is equal to time.
		// Execute the listener method.
		// interface ClockAlarmListener
		//		wakeUp(STime time)
	}

	/**
	 * Set an alarm, to go off at N seconds from now.
	 * @param seconds	The number of seconds to wait for the alarm to go off.
	 * @param listener	The listener method to call to process the alarm.
	 */
	public void setAlarm(int seconds, ClockAlarmListener listener) {
		// Spawn a thread that sleeps for seconds.
		// Execute the listener method.
	}

	public static void main(String[] args) {
	}
}

