/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by AUI (in the framework of the ALMA collaboration),
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
 * File PIProxy.java
 * 
 */
package alma.scheduling.project_manager;

import alma.acs.container.ContainerServices;

/**
 * The interface to the PI.  Methods in this class use
 * interfaces in the Executive Subsystem in their implementation.
 * 
 * @version 1.00 May 5, 2003
 * @author Allen Farris
 */
public class PIProxy implements ProjectManager_to_PI {

	public PIProxy (boolean isSimulation, ContainerServices container) {
		System.out.println("SCHEDULING: The PIProxy has been constructed.");
	}

	/**
	 * @see master_scheduler.ProjectManager_to_PI#send(String, String, String)
	 */
	public void send(String piId, String projectId, String message) {
	}

	/**
	 * @see master_scheduler.ProjectManager_to_PI#error(String, String, String, String)
	 */
	public void error(String piId, String projectId, String messageId, String message) {
	}

	/**
	 * @see master_scheduler.ProjectManager_to_PI#breakpoint(String, String, String, String, String)
	 */
	public void breakpoint(String piId, String projectId, String breakpointId,
		String messageId, String message) {
	}

	public static void main(String[] args) {
	}
}

