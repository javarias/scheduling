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
 * File PI.java
 */
package alma.scheduling.Define;

/**
 * The PI interface defines those methods needed by the Scheduling system
 * to communicate with the PI.  There are only three methods needed and they
 * all involve sending messages.  No replies to these messages are expected.
 * Responding to breakpoints and errors is done by posting a 
 * "BreakpointResponse" in the Archive. 
 * <ul>
 * <li> Send a progress message to the PI about the project
 * <li> Inform the PI that an unrecoverable error has occurred in the project.
 * <li> Inform the PI that a breakpoint in the project has been reached.
 * </ul>
 * 
 * @version 1.5 September 16, 2004
 * @author Allen Farris
 */
public interface PI {
	void sendProgress(String piId, String projectId, String message);
	void sendError(String piId, String projectId, String message);
	void sendBreakpoint(String piId, String projectId, String message);
}
