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
 * File ProjectMember.java
 */
package alma.scheduling.Define;

import java.io.PrintStream;

/**
 * A ProjectMember is a member of the project tree; it is implemented 
 * by the Project, Program, SB, and ExecBlock classes.  
 * 
 * @version $Id: ProjectMember.java,v 1.3 2004/11/23 20:41:21 sslucero Exp $
 * @author Allen Farris
 */
public interface ProjectMember {

	public String toString();

	public void printTree(PrintStream out, String indent);
	
	public void printTreeLite(PrintStream out, String indent);
		
	/**
	 * @return Returns the project.
	 */
	public Project getProject();

	/**
	 * @param project The project to set.
	 */
	public void setProject(Project project);

	/**
	 * Return the time this project component was created.
	 * @return Returns the time this project component was created.
	 */
	public DateTime getTimeOfCreation();

	/**
	 * Set the time this project component was created.
	 * @param t The time this project component was created.
	 */
	public void setTimeOfCreation(DateTime timeOfCreation);

	/**
	 * Return the time this project component was updated.
	 * @return Returns the time this project component was updated.
	 */
	public DateTime getTimeOfUpdate();

	/**
	 * Set the time this project component was updated.
	 * @param t The time this project component was updated.
	 */
	public void setTimeOfUpdate(DateTime timeOfUpdate);

	/**
	 * Return the unique identifier of this project component.
	 * @return Returns the unique identifier of this project component.
	 */
	public String getId();

	/**
	 * Return the status of this project component.
	 * @return Returns the status of this project component.
	 */
	public Status getStatus();

}
