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
 * File ArchiveProxy.java
 */
 
package alma.scheduling.planning_mode_sim.master_scheduler;

import alma.scheduling.planning_mode_sim.define.DateTime;
import alma.scheduling.planning_mode_sim.define.SProject;
import alma.scheduling.planning_mode_sim.define.SUnitSet;
import alma.scheduling.planning_mode_sim.define.SUnit;
import alma.scheduling.planning_mode_sim.define.SPolicy;

/**
 * Description 
 * 
 * @version 1.00  Jun 3, 2003
 * @author Allen Farris
 */
public interface ArchiveProxy {
	
	// SProject
	public SProject[] getAllProject() throws SchedulingException;
	public SProject[] getNewProject(DateTime time) throws SchedulingException;
	public SProject getProject(String id) throws SchedulingException;
	public void updateProject(SProject p) throws SchedulingException;

	// SUnitSet
	public SUnitSet getSUnitSet(String id) throws SchedulingException;
	public void updateSUnitSet(SUnitSet s) throws SchedulingException;

	// SUnit
	public SUnit[] getAllSUnit() throws SchedulingException;
	public SUnit[] getNewSUnit(DateTime time) throws SchedulingException;
	public SUnit getSUnit(String id) throws SchedulingException;
	public void updateSUnit(SUnit sb) throws SchedulingException;

	// SchedulingPolicy
	public SPolicy[] getSPolicy() throws SchedulingException;

	// PipelineProcessingRequest
	//public Sppr[] getAllSppr() throws SchedulingException;
	//public Sppr getSppr(String id) throws SchedulingException;
	//public void newSppr(Sppr ppr) throws SchedulingException;
	//public void updateSppr(Sppr ppr) throws SchedulingException;

}
