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
 * File ALMAPipeline.java
 */
 
package alma.scheduling.project_manager;

import java.util.logging.Logger;
import java.util.logging.Level;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.acs.entityutil.EntitySerializer;

import alma.entity.xmlbinding.pipelineprocessingrequest.*;
import alma.entity.xmlbinding.pipelineprocessingrequest.types.*;

import alma.pipelinescience.SciencePipeline;

import alma.scheduling.master_scheduler.SchedulingException;

/**
 * Description 
 * 
 * @version 1.00  Jun 3, 2003
 * @author Allen Farris
 */
public class ALMAPipeline implements PipelineProxy {
    private ContainerServices containerServices;
    private SciencePipeline sciencePipelineComp;
    private Logger logger;
    //private ProjectManager projectManager;
    
	/**
	 * 
	 */
	public ALMAPipeline(boolean isSimulation, ContainerServices cs) {
     //   ProjectManager pm) {
        
		super();
        System.out.println("SCHEDULING: The PipelineProxy has been constructed.");
        this.containerServices = cs;
        //this.projectManager = pm;
        this.logger = containerServices.getLogger();
        
	}

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

	/* (non-Javadoc)
	 * @see alma.scheduling.project_manager.PipelineProxy#processRequest(alma.eServicesServicesntity.xmlbinding.pipelineprocessingrequest.PipelineProcessingRequest)
	 */
    public String processRequest(PipelineProcessingRequest request)
	        throws SchedulingException {
        String requestRes="request result";
        logger.log(Level.INFO,"SCHEDULING: "+requestRes);
        try {
            sciencePipelineComp = alma.pipelinescience.SciencePipelineHelper.narrow(
                containerServices.getComponent("SCIENCE_PIPELINE"));
            requestRes =sciencePipelineComp.processRequest(
                EntitySerializer.getEntitySerializer(logger).serializeEntity(request));
        } catch (Exception e) {
            logger.log(Level.SEVERE,"SCHEDULING: Error connecting to PIPELINE!");
        }
        return requestRes;
    }

    public void release() {
        containerServices.releaseComponent("SCIENCE_PIPELINE");
    }

	/* (non-Javadoc)
	 * @see alma.scheduling.project_manager.PipelineProxy#getStatus(java.lang.String)
	 */
	public PipelineStatus getStatus(String pipelineProcessingId) {
     /*
        try {
            sciencePipelineComp = alma.pipelinescience.SciencePipelineHelper.narrow(
            
        } catch (Exception e) {}
    */
		return null;
	}

	public static void main(String[] args) {
	}
}
