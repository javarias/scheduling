/*
 * ALMA - Atacama Large Millimeter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
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

import alma.xmlentity.XmlEntityStruct;

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
 * @author Sohaila Roberts
 */
public class ALMAPipeline implements PipelineProxy {
    private ContainerServices containerServices;
    private SciencePipeline sciencePipelineComp;
    private Logger logger;
    private boolean isSimulation;
    //private ProjectManager projectManager;
    
	/**
	 * 
	 */
	public ALMAPipeline(boolean isSimulation, ContainerServices cs) {
     //   ProjectManager pm) {
        
		super();
        System.out.println("SCHEDULING: The PipelineProxy has been constructed.");
        this.isSimulation = isSimulation;
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

	/** 
	 * 
	 */
    //public String processRequest(PipelineProcessingRequest request)
    public String processRequest(XmlEntityStruct request)
	        throws SchedulingException {
        String requestRes="request result";
        if(isSimulation) {
            
        } else {
            try {
                sciencePipelineComp = alma.pipelinescience.SciencePipelineHelper.narrow(
                    containerServices.getComponent("SCIENCE_PIPELINE"));
                XmlEntityStruct entity = EntitySerializer.getEntitySerializer(logger).serializeEntity(request);
                //entity.timeStamp = timeStamp;
                //logger.info("ALMAPipeline:"+ entity.xmlString);
                //logger.info("ALMAPipeline:"+ entity.timeStamp);
                logger.info("ALMAPipeline:"+ request.xmlString);
                logger.info("ALMAPipeline:"+ request.timeStamp);
                logger.info("ALMAPipeline:"+ requestRes);
                requestRes =sciencePipelineComp.processRequest(request);
                //requestRes = sciencePipelineComp.processRequest(entity);
                    
            } catch (Exception e) {
                logger.severe("SCHEDULING: Error connecting to PIPELINE!");
                logger.severe(e.toString());
            }
        }
        logger.info("SCHEDULING: "+requestRes);
        return requestRes;
    }

    public void release() {
        containerServices.releaseComponent("SCIENCE_PIPELINE");
    }

	/**
	 *
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
