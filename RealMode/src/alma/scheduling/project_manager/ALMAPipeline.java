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
 
package ALMA.scheduling.project_manager;

import java.util.logging.Logger;
import java.util.logging.Level;

import alma.acs.container.ContainerServices;
import alma.acs.entityutil.EntitySerializer;

import alma.entity.xmlbinding.pipelineprocessingrequest.PipelineProcessingRequest;

import alma.pipelinescience.SciencePipeline;

import ALMA.scheduling.NothingCanBeScheduledEvent;
import ALMA.scheduling.master_scheduler.SchedulingException;

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
    private PipelineEventListener p_event;
    private ProjectManager projectManager;
    
	/**
	 * 
	 */
	public ALMAPipeline(boolean isSimulation, ContainerServices container,
        ProjectManager pm) {
        
		super();
        System.out.println("The PipelineProxy has been constructed.");
        this.containerServices = container;
        this.projectManager = pm;
        this.logger = containerServices.getLogger();
        
	}

	/* (non-Javadoc)
	 * @see alma.scheduling.project_manager.PipelineProxy#processRequest(alma.entity.xmlbinding.pipelineprocessingrequest.PipelineProcessingRequest)
	 */
    public String processRequest(PipelineProcessingRequest request)
	        throws SchedulingException {
        String requestRes="request result";
        logger.log(Level.INFO,"!!!!!!!!!!!!!!!"+requestRes);
        try {
            sciencePipelineComp = alma.pipelinescience.SciencePipelineHelper.narrow(
                containerServices.getComponent("SCIENCE_PIPELINE"));
            requestRes =sciencePipelineComp.processRequest(
                EntitySerializer.getEntitySerializer(logger).serializeEntity(request));
            p_event = new PipelineEventListener(this);
            try {
                p_event.addSubscription(ALMA.acsnc.DEFAULTTYPE.value);
                p_event.consumerReady();
            } catch(Exception e) {
                System.out.println("Error in ALMAPipeline: "+e.toString());
                System.err.println(e);
                e.printStackTrace();
                p_event.disconnect();
                alma.acs.nc.Helper.disconnect();
            }
            //containerServices.releaseComponent("SCIENCE_PIPELINE");
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error connecting to PIPELINE!");
        }
        return requestRes;
    }

    public void disconnect() {
        p_event.disconnect();
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

    public void sendNothingCanBeScheduledEvent(NothingCanBeScheduledEvent e) {
        projectManager.sendNothingCanBeScheduledEvent(e);
    }

	public static void main(String[] args) {
	}
}
