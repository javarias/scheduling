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
 * File SimulationException.java
 */
 
package alma.scheduling.planning_mode_sim.simulator;

import java.util.logging.Level;

/**
 * Description 
 * 
 * @version 1.00  Aug 21, 2003
 * @author Allen Farris
 */
public class SimulationException extends Exception {
	private String moduleName;
	private Level level;
	private String message;
	
	/**
	 * Create a simulation exception.
	 * @param moduleName The name of the module in which the exception occurred.
	 * @param level The logging level associated with the exception.
	 * @param message The message associated with the exception.
	 */
	public SimulationException(String moduleName, Level level, String message) {
		super(moduleName + " " + level + " " + message);
		this.moduleName = moduleName;
		this.level = level;
		this.message = message;
	}

	/**
	 * Create a simulation exception.
	 * @param moduleName The name of the module in which the exception occurred.
	 * @param level The logging level associated with the exception.
	 * @param message The message associated with the exception.
	 */
	public SimulationException(String moduleName, String message) {
		super(moduleName + " " + Level.SEVERE + " " + message);
		this.moduleName = moduleName;
		this.level = Level.SEVERE;
		this.message = message;
	}

	/**
	 * Get the logging level associated with the exception.
	 * @return The logging level associated with the exception.
	 */
	public Level getLevel() {
		return level;
	}

	/**
	 * Get the message associated with the exception.
	 * @return The message associated with the exception.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Get the name of the module in which the exception occurred.
	 * @return The name of the module in which the exception occurred.
	 */
	public String getModuleName() {
		return moduleName;
	}

}
