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
 * File Archive.java
 */
 
package alma.Scheduling.Define;

/**
 * The Archive interface defines the interface to the archive needed
 * by the scheduling subsystem. 
 * 
 * @version 1.30 May 10, 2004
 * @author Allen Farris
 */
public interface Archive {
	
	// Project
	public Project[] getAllProject() throws SchedulingException;
	public Project[] getNewProject(DateTime time) throws SchedulingException;
	public Project getProject(String id) throws SchedulingException;
	public void updateProject(Project p) throws SchedulingException;

	// Program
	public Program getProgram(String id) throws SchedulingException;
	public void updateProgram(Program s) throws SchedulingException;

	// SB
	public SB[] getAllSB() throws SchedulingException;
	public SB[] getNewSB(DateTime time) throws SchedulingException;
	public SB getSB(String id) throws SchedulingException;
	public void updateSB(SB sb) throws SchedulingException;

	// SchedulingPolicy
	public Policy[] getPolicy() throws SchedulingException;

}
