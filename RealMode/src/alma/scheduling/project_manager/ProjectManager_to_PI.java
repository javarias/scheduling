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
 * File ProjectManager_to_PI.java
 * 
 */
package alma.scheduling.project_manager;

/**
 * This interface is implemented by the scheduling PIProxy class using
 * interfaces in the Executive subsystem.  (See ICD section 3.2) This 
 * interface is used by the ProjectManager to send information to the 
 * PI about the progress of that PI's project.  In some cases replies 
 * are expected.
 * 
 * @version 1.00 May 2, 2003
 * @author Allen Farris
 */
public interface ProjectManager_to_PI {

	/**
	 * Send a progress message to the PI about the project.  No reply 
	 * is expected.
	 * @param piId			the identifier of the PI to whom the message is sent
	 * @param projectId	the identifier of the relevant project
	 * @param message		the message being sent to the PI
	 */
	void send(String piId, String projectId, String message);

	/**
	 * Send an error message to the PI about the project. A reply is 
	 * required.
	 * @param piId			the identifier of the PI to whom the message is sent
	 * @param projectId	the identifier of the relevant project
	 * @param messageId	A unique identifier that identifies the message being sent. 
	 * 						It must be included in the reply.
	 * @param message		the message being sent to the PI
	 */
	void error (String piId, String projectId, String messageId, String message); 

	/**
	 * Inform the PI that a breakpoint in the project has been
	 * reached. A reply is required.
	 * @param piId			the identifier of the PI to whom the message is sent
	 * @param projectId	the identifier of the relevant project
	 * @param breakpointId	the name of the breakpoint
	 * @param messageId	A unique identifier that identifies the message being sent. 
	 * 						It must be included in the reply.
	 * @param message		the message being sent to the PI
	 */
	void breakpoint (String piId, String projectId, 
    	 			     String breakpointId, String messageId,
    	 			     String message); 

}

