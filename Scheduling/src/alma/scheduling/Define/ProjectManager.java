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
 * File ProjectManager.java
 */
 
package alma.scheduling.Define;

/**
 * The ProjectManager interface defines public methods needed by various
 * components, both real and simulated.
 * 
 * @version $Id: ProjectManager.java,v 1.5 2004/11/23 20:41:21 sslucero Exp $
 * @author Allen Farris
 */
public interface ProjectManager {

	/**
	 * Return true if and only if the project to which the specified SB
	 * belongs is a new project, i.e., one that has no SBs that have ever 
	 * been executed. 
	 * @param unit
	 * @return
	 */
	public boolean newProject(SB unit);
	
	/**
	 * Return the number of SBs that remain to be executed in the project
	 * to which the specified SB belongs.
	 * @param unit
	 * @return
	 */
	public int numberRemaining(SB unit);
	

    public TaskControl getProjectManagerTaskControl();
    
    public ObservedSession createObservedSession(Program p);
    public void sendStartSessionEvent(ObservedSession session);
    public void sendEndSessionEvent(ObservedSession session);
}
