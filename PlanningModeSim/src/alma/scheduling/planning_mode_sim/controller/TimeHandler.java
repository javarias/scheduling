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

package alma.scheduling.planning_mode_sim.controller;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Independizes time data requests to the system.<br>
 * Allows to have a real date implementation and a simulated
 * date response.<br>
 * 
 * @author ahoffsta
 *
 */
public abstract class TimeHandler {
	
	protected static Logger logger = null;
	static private TimeHandler inst = null;
	
	static public TimeHandler initialize(Type ty){
		if( inst == null ){
			if( ty == Type.REAL)
				inst = new TimeReal();
			else if( ty == Type.SIMULATED)
				inst = new TimeSim();
		}
		if( logger == null )
			logger = LoggerFactory.getLogger(inst.getClass());
		return inst;
	}
	
	static public Logger getLogger(){
		return logger;
	}
	
	/**
	 * Sets a date as starting point for simulation.
	 * If not in simulation, it logs the event.
	 * @param sd Starting date for the simulation
	 */
	public abstract void setStartingDate(Date sd);
	
	/**
	 * Gives the date as considered by the implementation.
	 * @return The Date the implementation currently has.
	 */
	public abstract Date getTime();
	
	/**
	 * Steps forward a pre-established amount of time. 30 minutes.
	 */
	public abstract void step();

	/**
	 * Steps forward the indicated amount of time.
	 * @param time Amount of time in milliseconds to step forward the clock.
	 */
	public abstract void step(int time);
	
	/**
	 * Steps forward to the specified date.
	 * @param Date Date to which the internal clock must be moved to.
	 */
	public abstract void step(Date date);
	
	static public TimeHandler getHandler(){
		return inst;		
	}
	
	static public Date now(){
		return getHandler().getTime();
	}
	
	static public void stepAhead(){
		getHandler().step();
	}
	
	static public void stepAhead(int amount){
		getHandler().step(amount);
	}
	
	static public void stepAhead(double amount){
		getHandler().step(new Double(amount).intValue());
	}
	
	public enum Type{
		SIMULATED, REAL
	}

}
