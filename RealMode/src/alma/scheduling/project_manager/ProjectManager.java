/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by ESO (in the framework of the ALMA collaboration),
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
 * File ProjectManager.java
 * 
 */

package ALMA.scheduling.project_manager;

import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.Level;

import alma.entity.xmlbinding.obsproject.*;
import alma.entity.xmlbinding.pipelineprocessingrequest.*;
import alma.entities.commonentity.EntityRefT;

import ALMA.scheduling.NothingCanBeScheduledEvent;
import ALMA.scheduling.master_scheduler.ALMAArchive;
import ALMA.scheduling.master_scheduler.MasterSchedulerAction;
import ALMA.scheduling.master_scheduler.SchedulingException;

import alma.acs.component.ComponentLifecycle;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

import alma.entity.xmlbinding.pipelineprocessingrequest.*;
import alma.entity.xmlbinding.pipelineprocessingrequest.types.*;

/**
 * The ProjectManager class is the major controlling class in the 
 * project_manager package.  See Scheduling Subsystem Design document, 
 * section 3.2.2.
 * 
 * @version 1.00 Feb 27, 2003
 * @author Allen Farris
 *
 */
public class ProjectManager implements Runnable , ComponentLifecycle {
    private boolean isSimulation;
    private ContainerServices containerServices;
    private String componentName;
    private Logger logger;
    private ALMAArchive archive;
    private Vector projects;
    private ALMAPipeline pipeline;
    private MasterSchedulerAction action;
    
    public ProjectManager(){
        isSimulation = false;
        projects = new Vector();
    }

    public ProjectManager(boolean isSimulation,
                              ALMAArchive archive,
                                MasterSchedulerAction a) {
        this.isSimulation = isSimulation;
        this.archive = archive;
        this.action = a;
        projects = new Vector();
    }

    /** Lifecycle methods */

    /**
     *  @see alma.acs.component.ComponentLifecycle#setComponentName(String)
     */
    public void setComponentName(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException (
                "Component name cannot be a null string.");
        if (componentName != null)
            throw new UnsupportedOperationException (
                "Cannot change the name of a component that has already been named.");
        this.componentName = name;

    }

    /**
     *  @see alma.acs.component.ComponentLifecycle#setContainerServices(ContainerServices)
     */
    public void setContainerServices(ContainerServices containerServices) {
        this.containerServices = containerServices;
        this.logger = containerServices.getLogger();
        
        // create pipeline, did it here coz I had to wait for the containerServices!
        pipeline = new ALMAPipeline(isSimulation, containerServices, this);
        logger.log(Level.INFO,"PIPELINE CREATED IN PM");
    }

    /**
     * @see alma.acs.component.ComponentLifecycle#initialize()
     */
    public void initialize() {
        logger.log(Level.FINE, "Project Manager initialized.");
    }
    
    /**
     * @see alma.acs.component.ComponentLifecycle#execute()
     */
    public void execute() {
        logger.log(Level.FINE, "Project Manager is executing.");
    }
    
    /**
     * @see alma.acs.component.ComponentLifecycle#cleanUp()
     */
    public void cleanUp() {
        archive.release();
        logger.log(Level.FINE, "Project Manager is cleaning up and exiting.");
    }
    
    /**
     *  @see alma.acs.component.ComponentLifecycle#aboutToAbort()
     */
    public void aboutToAbort() {
        logger.log(Level.FINE, "Project Manager is about to abort.");
        cleanUp();
    }

    /** Runnable method */
    public void run() {
        
    }

    /**
     *  Starts the project manager thread (eventually).
     */
    public void start() {
    }
    /**
     *  Stops the project manager thread (eventually).
     */
    public void stop() {
    }
    
    public void addProjects(ObsProject[] proj) {
        for(int i = 0; i < proj.length; i++) {
            projects.add(proj[i]);
        }
    }
	
    /** 
     *  Adds the SB ids.
     */
    public void addSBUids(Vector uids) {
        for(int i=0; i < uids.size(); i++) {
            projects.add(uids.elementAt(i));;
        }
    }

    /**
     *  Creates a PipelineProcessingRequest and assigns it a unique
     *  identifier.
     *  @return PipelineProcessingRequest
     */
    public PipelineProcessingRequest createPipelineProcessingRequest() {
        PipelineProcessingRequest ppr = new PipelineProcessingRequest();
        PipelineProcessingRequestEntityT ppr_entity = 
                    new PipelineProcessingRequestEntityT();
        try {
            containerServices.assignUniqueEntityId(ppr_entity);
            ppr.setPipelineProcessingRequestEntity(ppr_entity);
        } catch (ContainerException e) {
            
        }
        return ppr;
    }

    public void startPipeline(String id) {
        EntityRefT ref = new EntityRefT();
        ref.setEntityId(id);
        PipelineProcessingRequest ppr = createPipelineProcessingRequest();
        ReductionUnitT ru = new ReductionUnitT();
        ru.setReductionUnitReference( ref );
        PipelineProcessingRequestSequenceItem pprsi = 
            new PipelineProcessingRequestSequenceItem();
        pprsi.setReductionUnit( ru );
        PipelineProcessingRequestSequence pprs = 
            new PipelineProcessingRequestSequence();
        pprs.setPipelineProcessingRequestSequenceItem( pprsi );
        //logger.log(Level.INFO,"pprs count = "+ ppr.getPipelineProcessingRequestSequenceCount());
        int pprs_count = ppr.getPipelineProcessingRequestSequenceCount();
        ppr.addPipelineProcessingRequestSequence( pprs );
        //ppr.setStatus(StatusType.VALUE_0);

        //Store the ppr in the archive.
        archive.addPipelineProcessingRequest(ppr);
        try {
            logger.log(Level.INFO, "Process Request called in PM!");
            String res = pipeline.processRequest(ppr);
            logger.log(Level.INFO, "Process Request returned "+res);
        } catch (SchedulingException se) {
            logger.log(Level.SEVERE, "SchedulingError when starting pipeline");
        }
    }

    public void disconnectFromPipeline() {
        pipeline.disconnect();
    }

    public void sendNothingCanBeScheduledEvent(NothingCanBeScheduledEvent e) {
        action.sendNothingCanBeScheduledEvent(e);
    }

    public static void main(String[] args) {
	}
}
