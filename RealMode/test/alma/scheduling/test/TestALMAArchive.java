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
 * File TestALMAArchive.java
 * 
 */
package alma.scheduling.test;
import java.util.logging.Logger;

import alma.acs.entityutil.*;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.acs.component.client.ComponentClient;

import alma.xmlentity.XmlEntityStruct;
import alma.entity.xmlbinding.schedblock.*;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.obsproject.*;
import alma.entity.xmlbinding.schedulingpolicy.*;
import alma.entity.xmlbinding.pipelineprocessingrequest.*;

import alma.scheduling.master_scheduler.ALMAArchive;
import alma.Control.ExecBlockEvent;
/**
 *  Test the functions in the ALMAArchive
 *
 *  @author Sohaila Roberts
 */
public class TestALMAArchive {

    public static void main(String[] args) {
        System.out.println("This is the archive test.");
        try {
            String name = "Test ALMAArchive";
            String managerLoc = System.getProperty("ACS.manager");
            ComponentClient client = new ComponentClient(null, managerLoc, name);
            ContainerServices cs = client.getContainerServices();
            Logger logger = cs.getLogger();
            
            ALMAArchive archive = new ALMAArchive(true, cs);
            
            EntitySerializer es = EntitySerializer.getEntitySerializer(logger);
            EntityDeserializer ed = EntityDeserializer.getEntityDeserializer(logger);
            String id="";
            XmlEntityStruct xml_entity;

            logger.info("SCHED_TEST: function getSchedBlock() ");
            SchedBlock[] sbs = archive.getSchedBlock(); //hmm...
            for (int i = 0; i < sbs.length; i++) {
                xml_entity = es.serializeEntity(sbs[i]);
                id = xml_entity.entityId;
                //logger.info("SCHED_TEST: "+ xml_entity.xmlString);
            }

            logger.info("SCHED_TEST: function getSchedBlock(String id) ");
            SchedBlock sb = archive.getSchedBlock(id);
            xml_entity = es.serializeEntity(sb);
            //logger.info("SCHED_TEST: "+xml_entity.xmlString);
            logger.info("SCHED_TEST: sb with id="+
                sb.getSchedBlockEntity().getEntityId()+" has version="+
                    sb.getSchedBlockEntity().getDocumentVersion());//xml_entity.schemaVersion);

            logger.info("SCHED_TEST: function updateSchedBlock(ExecBlockEvent e) ");
            ExecBlockEvent event = new ExecBlockEvent();
            event.sbId = id;
            event.status = alma.Control.CompletionStatus.COMPLETED_OK;
            archive.updateSchedBlock(event);
            //retreive it to see that it updated
            sb = archive.getSchedBlock(id);
            xml_entity = es.serializeEntity(sb);
            //logger.info("SCHED_TEST: sb with id="+id+" has version="+xml_entity.schemaVersion);
            logger.info("SCHED_TEST: sb with id="+
                sb.getSchedBlockEntity().getEntityId()+" has version="+
                    sb.getSchedBlockEntity().getDocumentVersion());//xml_entity.schemaVersion);
            
            logger.info("SCHED_TEST: function updateSchedBlock(SchedBlock sb) ");
            archive.updateSchedBlock(sb);
            //retreive it to see that it updated
            sb = archive.getSchedBlock(id);
            xml_entity = es.serializeEntity(sb);
            //logger.info("SCHED_TEST: sb with id="+id+" has version="+xml_entity.schemaVersion);
            logger.info("SCHED_TEST: sb with id="+
                sb.getSchedBlockEntity().getEntityId()+" has version="+
                    sb.getSchedBlockEntity().getDocumentVersion());//xml_entity.schemaVersion);
            
            logger.info("SCHED_TEST: function getProject() ");
            ObsProject[] proj = archive.getProject();
            for( int i=0; i<proj.length; i++ ) {
                xml_entity = es.serializeEntity(proj[i]);
                id = xml_entity.entityId;
                //logger.info("SCHED_TEST: "+xml_entity.xmlString);
            }

            logger.info("SCHED_TEST: function getProject(String id)");
            ObsProject p = archive.getProject(id);
            if(p == null) {
                logger.info("project is null...");
            }  else { 
                xml_entity = es.serializeEntity(p);
            //logger.info("SCHED_TEST: "+xml_entity.xmlString);
            //logger.info("SCHED_TEST: project with id="+id+" has version="+xml_entity.schemaVersion);
            logger.info("SCHED_TEST: project with id="+
               p.getObsProjectEntity().getEntityId()+" has version="+
                    p.getObsProjectEntity().getDocumentVersion());//xml_entity.schemaVersion);
            
            logger.info("SCHED_TEST: function updateProject(ObsProject p)");
            archive.updateProject(p);
            //retreive it to see that it updated
            p = archive.getProject(id);
            xml_entity = es.serializeEntity(p);
            //logger.info("SCHED_TEST: project with id="+id+" has version="+xml_entity.schemaVersion);
            logger.info("SCHED_TEST: project with id="+
               p.getObsProjectEntity().getEntityId()+" has version="+
                    p.getObsProjectEntity().getDocumentVersion());//xml_entity.schemaVersion);
            }
            SchedulingPolicy sp = new SchedulingPolicy();
            SchedulingPolicyEntityT sp_entity = new SchedulingPolicyEntityT();
            cs.assignUniqueEntityId(sp_entity);
            sp.setSchedulingPolicyEntity(sp_entity);
            archive.store(sp, sp_entity);
            /*
            logger.info("SCHED_TEST: function getSchedulingPolicy()");
            SchedulingPolicy[] sps = archive.getSchedulingPolicy();
            for(int i = 0; i < sps.length; i++) {
                xml_entity = es.serializeEntity(sps[i]);
                //logger.info("SCHED_TEST: "+xml_entity.xmlString);
            }
            */
            logger.info("SCHED_TEST: function addPipelineProcessingRequest(PipelineProcessingRequest)");
            PipelineProcessingRequest ppr = new PipelineProcessingRequest();
            PipelineProcessingRequestEntityT ppr_entity = new PipelineProcessingRequestEntityT();
            cs.assignUniqueEntityId(ppr_entity);
            ppr.setPipelineProcessingRequestEntity(ppr_entity);
            archive.addPipelineProcessingRequest(ppr);

            logger.info("SCHED_TEST: function getPipelineProcessingRequest()");
            PipelineProcessingRequest[] pprs = archive.getPipelineProcessingRequest();
            for(int i=0; i< pprs.length; i++) {
                xml_entity = es.serializeEntity(pprs[i]);
                //logger.info("SCHED_TEST: "+xml_entity.xmlString);
            }
            logger.info("SCHED_TEST: function getPipelineProcessingRequest(String id)");
            xml_entity = archive.getPipelineProcessingRequest(xml_entity.entityId);
            //xml_entity = es.serializeEntity(ppr);
            //logger.info("SCHED_TEST: "+xml_entity.xmlString);
            
            logger.info("SCHED_TEST: function updatePipelineProcessingRequest(PipelineProcessingRequest ppr)");
            archive.updatePipelineProcessingRequest(ppr);
            //retreive it to see that it updated
            xml_entity = archive.getPipelineProcessingRequest(xml_entity.entityId);
            //xml_entity = es.serializeEntity(ppr);
            logger.info("SCHED_TEST: pipelineprocessingrequest with id="+
               xml_entity.entityId+" has version="+
                    ppr.getPipelineProcessingRequestEntity().getDocumentVersion());

        } catch(Exception e) {
            System.out.println("SCHED_TEST: Exception: " +e.toString() );
            System.exit(1);
        }
        //System.exit(0);
    }
}
