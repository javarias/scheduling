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
 
package alma.scheduling.Define;

/**
 * The Archive interface defines the interface to the archive needed
 * by the scheduling subsystem. 
 * 
 * @version $Id: Archive.java,v 1.7 2004/12/21 21:37:01 sslucero Exp $
 * @author Allen Farris
 */
public interface Archive {
	
	// Project
    /**
      * @throws SchedulingException
      */
	public Project[] getAllProject() throws SchedulingException;
    /**
      * @throws SchedulingException
      */
	public Project[] getNewProject(DateTime time) throws SchedulingException;
    /**
      * @throws SchedulingException
      */
	public Project getProject(String id) throws SchedulingException;
    /**
      * @throws SchedulingException
      */
	public void updateProject(Project p) throws SchedulingException;

	// Program
    /**
      * @throws SchedulingException
      */
	public Program getProgram(String id) throws SchedulingException;
    /**
      * @throws SchedulingException
      */
	public void updateProgram(Program s) throws SchedulingException;

	// SB
    /**
      * @throws SchedulingException
      */
	public SB[] getAllSB() throws SchedulingException;
    /**
      * @throws SchedulingException
      */
	public SB[] getNewSB(DateTime time) throws SchedulingException;
    /**
      * @throws SchedulingException
      */
	public SB getSB(String id) throws SchedulingException;
    /**
      * @throws SchedulingException
      */
	public void updateSB(SB sb) throws SchedulingException;

	// SchedulingPolicy
    /**
      * @throws SchedulingException
      */
	public Policy[] getPolicy() throws SchedulingException;

    //PipelineProcessingRequest
    /**
      *
      */
    public void storePipelineProcessingRequest(SciPipelineRequest ppr);
    
    //Session
    //public String storeSession(Session s);
    //public void updateSession(String id);
    
    
    /*
    Scheduling
    Queries associated with ProjectStatus
    Get all ProjectStatus whose status is ready.
    Get all ProjectStatus whose status is ready and whose status.readytime is greater than a specified time.
    Get the ProjectStatus with a specified entity-id.
    Update this specified ProjectStaus.

    Queries associated with ObsProject
    Get the ObsProject with a specified entity-id.

    Queries associated with SchedBlock
    Get all SchedBlock associated with a specified ObsProject
    Get the SchedBlock with the specified entity-id.

    Queries associated with SchedulingPolicy
    Get all SchedulingPolicy.
    Get the SchedulingPolicy with the specified entity-id.
    Get the SchedulingPolicy with the specified name.

    Queries associated with PipelineProcessingRequest
    Get all PipelineProcessingRequest whose status is not complete.
    Get the PipelineProcessingRequest with the specified entity-id.
    Update this specified PipelineProcessingRequest.

    Queries associated with Session
    Store this specified Session.
    */
 
    

}
