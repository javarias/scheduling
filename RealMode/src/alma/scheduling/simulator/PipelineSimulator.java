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
 * File PipelineSimulator.java
 */
 
package alma.scheduling.simulator;

import java.util.logging.Logger;

import alma.xmlentity.XmlEntityStruct;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;

import alma.pipelinescience.PipelineStatusEnum;
import alma.pipelinescience.CompletionStatusEnum; 

import alma.entity.xmlbinding.pipelineprocessingrequest.*;
import alma.entity.xmlbinding.pipelineprocessingrequest.types.*;

import alma.scheduling.master_scheduler.SchedulingException;
import alma.scheduling.project_manager.PipelineProxy;
import alma.scheduling.project_manager.PipelineStatus;

import alma.pipelinescience.ScienceProcessingRequestEnd;
import alma.pipelinescience.PipelineStatusEnum;

import alma.xmlstore.*;
import alma.xmlstore.IdentifierPackage.*;
import alma.entities.commonentity.EntityT;

/**
 * Description 
 * 
 * @author Sohaila Roberts
 */
public class PipelineSimulator implements PipelineProxy {

    private ContainerServices container;
    private Logger logger;


	/**
	 * 
	 */
	public PipelineSimulator(ContainerServices cs) {
		super();
        this.container = cs;
        this.logger = cs.getLogger();
	}

	/**
	 *
	 */
	//public String processRequest(XmlEntityStruct request)
	public String processRequest(PipelineProcessingRequest request)
		throws SchedulingException {
        // send out a pipeline event to say that its done processing
        // ScienceProcessingRequestEnd
        //return the id of the pipelineprocessingrequestend thing
        ScienceProcessingRequestEnd ppre = new ScienceProcessingRequestEnd();
        ppre.PipelineRequestId = request.getPipelineProcessingRequestEntity().getEntityId();
        EntityT entity = new EntityT();
        try {
            container.assignUniqueEntityId(entity);
        } catch(Exception e) {
        }
        //"something unique to pipeline - id";
        ppre.PipelineProcessId = entity.getEntityId();
        ppre.completionStatus = CompletionStatusEnum.COMPLETE_SUCCEEDED;
        ppre.reason = "Simulation pipeline processing!";
        
		return ppre.PipelineRequestId;
	}
	public String processRequest(XmlEntityStruct request)
		throws SchedulingException {

        return null;
    }

	/**
	 *
	 */
	public PipelineStatus getStatus(String pipelineProcessingId) {
		// not sure what to do here so pretend its complete.
		return PipelineStatus.COMPLETE; 
	}

	public static void main(String[] args) {
		System.out.println("Unit test of pipeline simulator.");
		//PipelineSimulator pipeline = new PipelineSimulator();
		System.out.println("End unit test of pipeline simulator.");
	}
}
