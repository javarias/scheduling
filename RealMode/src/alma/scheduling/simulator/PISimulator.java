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
 * File PISimulator.java
 */
 
package ALMA.scheduling.simulator;

import ALMA.scheduling.project_manager.ProjectManager_to_PI;

/**
 * Description 
 * 
 * @version 1.00  Jul 18, 2003
 * @author Allen Farris
 */
public class PISimulator implements ProjectManager_to_PI {

	/**
	 * 
	 */
	public PISimulator() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.project_manager.ProjectManager_to_PI#send(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void send(String piId, String projectId, String message) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see alma.scheduling.project_manager.ProjectManager_to_PI#error(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void error(
		String piId,
		String projectId,
		String messageId,
		String message) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see alma.scheduling.project_manager.ProjectManager_to_PI#breakpoint(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void breakpoint(
		String piId,
		String projectId,
		String breakpointId,
		String messageId,
		String message) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
	}
}
