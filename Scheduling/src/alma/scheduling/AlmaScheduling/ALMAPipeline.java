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

package alma.scheduling.AlmaScheduling;

import java.util.logging.Logger;

import alma.xmlentity.XmlEntityStruct;
import alma.entity.xmlbinding.pipelineprocessingrequest.*;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.acs.entityutil.*;

import alma.scheduling.Define.SciPipeline;
import alma.scheduling.Define.SciPipelineRequest;
import alma.scheduling.Define.Status;
import alma.scheduling.Define.SchedulingException;

import alma.pipelinescience.SciencePipeline;

/**
 * This class communicates with the Science Pipeline Subsystem
 * @author Sohaila Lucero
 */
public class ALMAPipeline implements SciPipeline {
    private ContainerServices containerServices;
    private Logger logger;
    private SciencePipeline pipelineComp;
    private EntitySerializer entitySerializer;
    
    public ALMAPipeline(ContainerServices cs) {
        this.containerServices = cs;
        this.logger = cs.getLogger();
        getPipelineComponents();
        entitySerializer = EntitySerializer.getEntitySerializer(cs.getLogger());
    }

    /**
     * Connect to the pipeline components
     */
    private void getPipelineComponents() {
        try {
            pipelineComp = alma.pipelinescience.SciencePipelineHelper.narrow(
                containerServices.getComponent("PIPELINE_SCIENCE"));
            
        } catch(ContainerException e) {
            logger.severe("SCHEDULING: Error getting pipeline component");
            e.printStackTrace();
        }
    }


    /**
     * Send the pipeline processing request to the pipeline subsystem.
     * @param ppr The xmlentitystruct reprenesentation of the pipeline processing request
     * @return String The result from the pipeline subsystem
     */
    public String processRequest(XmlEntityStruct ppr) {
        String requestResult = null;
        try { 
            requestResult = pipelineComp.processRequest(ppr);
            logger.info("SCHEDULING: result returned from Pipeline = "+ requestResult);
        } catch(Exception e) {
            logger.severe("SCHEDULING: Error sending request to pipeline");
            e.printStackTrace();
        }
        return  requestResult;
    }

    /**
     * Creates a pipeline processing request entity, ready for the archive, using
     * the given SchedBlock id. 
     *
     * @param ps_id The ProjectStatus id for the Reduction unit's entityId
     * @param oucs_id The ObsUnitSetStatus id for the Reduction unit's partId 
     * @return XmlEntityStruct The PipelineProcessingRequest converted to a struct
     */
    public XmlEntityStruct createPipelineProcessingRequest(String ps_id, String oucs_id) {
        XmlEntityStruct ppr_struct = null;
        try { 
            PipelineProcessingRequest ppr = new PipelineProcessingRequest();
            PipelineProcessingRequestEntityT ppr_entity = new PipelineProcessingRequestEntityT();
            containerServices.assignUniqueEntityId(ppr_entity);
            ppr.setPipelineProcessingRequestEntity(ppr_entity);
            logger.info("SCHEDULING: PPR created with id = "+ppr_entity.getEntityId());
            ReductionUnitT ru = new ReductionUnitT();
            ru.setEntityId(ps_id);
            ru.setPartId(oucs_id);
            ppr.setReductionUnit(ru);
            ppr_struct = entitySerializer.serializeEntity(ppr);
            System.out.println("PPR: "+ppr_struct.xmlString);
        } catch(Exception e) {
            logger.severe("SCHEDULING: Error creating PPR");
            e.printStackTrace();
        }
        return ppr_struct;
    }

    /**
      *
      */
    public void start(SciPipelineRequest ppr)
        throws SchedulingException {

        logger.info("SCHEDULING: Starting the science pipeline");
        processRequest(((ALMAPipelineProcessingRequest)
                ppr).getPipelineProcessingRequestStruct());
    }
    /**
      *
      */
    public Status getStatus(SciPipelineRequest ppr) 
        throws SchedulingException {

        return null;
    }
}


