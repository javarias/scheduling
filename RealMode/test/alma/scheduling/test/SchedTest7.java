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
 * File SchedTest7.java
 * 
 */
package alma.scheduling.test;

import java.util.logging.Logger;
import java.util.logging.Level;

import alma.xmlstore.*;
import alma.xmlstore.OperationalPackage.*;
import alma.xmlstore.CursorPackage.*;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.acs.component.client.ComponentClient;

import alma.acs.entityutil.EntitySerializer;
import alma.acs.entityutil.EntityDeserializer;
import alma.acs.entityutil.EntityException;

import alma.scheduling.MS;
import alma.scheduling.MSOperations;
import alma.scheduling.MSPOATie;
import alma.scheduling.NothingCanBeScheduledEvent;
import alma.scheduling.NothingCanBeScheduledEventHelper;
import alma.scheduling.master_scheduler.MSHelper;
import alma.scheduling.InvalidOperation;
import alma.scheduling.master_scheduler.MasterScheduler;
import alma.scheduling.master_scheduler.ALMAArchive;
import alma.scheduling.project_manager.ProjectManager;

import alma.xmlentity.XmlEntityStruct;
import alma.entity.xmlbinding.schedblock.SchedBlock;
import alma.entity.xmlbinding.schedblock.SchedBlockEntityT;
import alma.entity.xmlbinding.schedulingpolicy.SchedulingPolicy;
import alma.entity.xmlbinding.schedulingpolicy.SchedulingPolicyEntityT;
import alma.entity.xmlbinding.pipelineprocessingrequest.PipelineProcessingRequest;
import alma.entity.xmlbinding.pipelineprocessingrequest.PipelineProcessingRequestEntityT;


import org.omg.CosNotification.*;
import alma.acs.nc.*;

/**
 *  Test for R0+, Passing around a scheduling block!
 *  @author Sohaila Roberts
 */
public class SchedTest7 {
    private EntitySerializer entitySerializer;
    private EntityDeserializer entityDeserializer;
    private ContainerServices containerServices;
    private MS masterSchedulerComp;
    //private MasterScheduler masterSchedulerObj;
    private ProjectManager projectManager;
    private SchedBlock[] sbs;
    private Logger logger;
    private TestSchedConsumer consumer;

    
    /**
     *  Create 5 scheduling blocks and store in the archive.
     */
    public static void populateArchive(ContainerServices cs) {
        Logger logger = cs.getLogger();
        SchedBlock sb;
        SchedBlockEntityT sb_entity;
        try {
            alma.xmlstore.Operational op = alma.xmlstore.OperationalHelper.narrow(
                cs.getComponent("OPERATIONAL_ARCHIVE"));
            EntitySerializer es = EntitySerializer.getEntitySerializer(logger);
            for (int i=0; i< 5; i++) {
                sb = new SchedBlock();
                sb_entity  = new SchedBlockEntityT();
                cs.assignUniqueEntityId(sb_entity);
                //sb_uid[i] = sb_entity.getEntityId();
                sb.setSchedBlockEntity(sb_entity);
                op.update( es.serializeEntity(sb) );
            }
            cs.releaseComponent("OPERATIONAL_ARCHIVE");
        } catch(Exception e) {
            logger.log(Level.SEVERE, "ERROR: "+e.toString());
            cs.releaseComponent("OPERATIONAL_ARCHIVE");
        }
    }

    public SchedTest7(ContainerServices cs) {
        this.containerServices = cs;
        this.logger = cs.getLogger();
        entitySerializer = EntitySerializer.getEntitySerializer(logger);
        entityDeserializer = EntityDeserializer.getEntityDeserializer(logger);
        // Get the masterSchedulerObj to test how scheduling uses everything internally
        /*
        masterSchedulerObj = new MasterScheduler();
        masterSchedulerObj.setComponentName("MasterSchedulerObj");
        masterSchedulerObj.setContainerServices(containerServices);
        masterSchedulerObj.initialize();
        masterSchedulerObj.execute();
        logger.log(Level.FINE, "MS Object created");
        */
        // Get the MS interface as a component to test how the Exec will use it
        try {
            this.masterSchedulerComp = alma.scheduling.MSHelper.narrow(
                containerServices.getComponent("MASTER_SCHEDULER"));
            logger.log(Level.FINE, "MS Component created");
        } catch(ContainerException e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }
    
    /**
     *  Creates a PipelineProcessingRequest and assigns it a unique
     *  identifier.
     *  @return PipelineProcessingRequest
     */
    /*
    public PipelineProcessingRequest createPipelineProcessingRequest() {
        PipelineProcessingRequest ppr = new PipelineProcessingRequest();
        PipelineProcessingRequestEntityT ppr_entity = 
                    new PipelineProcessingRequestEntityT();
        try {
            containerServices.assignUniqueEntityId(ppr_entity);
            //ppr_uid = ppr_entity.getEntityId();
            ppr.setPipelineProcessingRequestEntity(ppr_entity);
        } catch (ContainerException e) {
            
        }
        return ppr;
    }
    */
    
    /**
     *  Calles the scheduling's startScheduling method using an
     *  empty (newly created) SchedulingPolicy
     */
    public void testStartScheduling() {
        try {
            SchedulingPolicy sp = new SchedulingPolicy() ; 
            SchedulingPolicyEntityT spe = new SchedulingPolicyEntityT();
            try {
                containerServices.assignUniqueEntityId(spe);
                sp.setSchedulingPolicyEntity(spe);
            } catch (ContainerException e) {
                logger.log(Level.SEVERE, "ContainerException: "+ e.toString());
            }
            XmlEntityStruct sp_struct = entitySerializer.serializeEntity(sp);
            //System.out.println("Scheduling Policy xml" +sp_struct.xmlString);
            masterSchedulerComp.startScheduling(sp_struct); 
            logger.log(Level.FINE, "MasterScheduler startScheduling called.");
        } catch(EntityException e) {
            logger.log(Level.SEVERE, "EntityException: SchedulingPolicy not serialized!");
            logger.log(Level.SEVERE, "EntityException: "+ e.toString());
        } catch(InvalidOperation e) {
            logger.log(Level.SEVERE, "InvalidOperation: startScheduling failed!");
            logger.log(Level.SEVERE, "InvalidOperation: "+ e.toString());
        }
        logger.log(Level.FINE, "Scheduling started"); 
        testNothingToSchedule();
    }
/*
    public void testQueryArchive() {
        ALMAArchive archive = masterSchedulerObj.getArchive();
        SchedBlock[] sbs = archive.getSchedBlock();
        
        if(sbs == null) {
            logger.log(Level.SEVERE, "Sbs null!?!?");
        } else {
            logger.log(Level.FINE, "Continuing, SBs not null.");
        }
    }
    */

    public void testNothingToSchedule() {
        consumer = new TestSchedConsumer(containerServices);
        System.out.println("SchedConsumer created");
        try {
            consumer.addSubscription(ALMA.acsnc.DEFAULTTYPE.value);
            consumer.consumerReady();
        } catch(Exception e) {
            consumer.disconnect();
            alma.acs.nc.Helper.disconnect();
        }
    }

    public void release() {
        containerServices.releaseComponent("MASTER_SCHEDULER");
    }

    public static void main(String[] args) {
        try {
            String name = "Test7";
            String managerLoc = System.getProperty("ACS.manager");
            ComponentClient client = new ComponentClient(null, managerLoc, name);
            ContainerServices cs = client.getContainerServices();
            //SchedTest7.populateArchive(cs);
            //Thread.sleep(5000);
            SchedTest7 test7 = new SchedTest7(cs);
            test7.testStartScheduling();
            //cs.releaseComponent("MASTER_SCHEDULER");
            //test7.testQueryArchive();
            //test7.testSelectSB();
            //test7.testNothingToSchedule();
            //System.out.println("Sleeping for 90 sec before releasing MS");
            Thread.sleep(1000*300);
            test7.release();
        } catch(Exception e) {
            System.out.println("Exception: " +e.toString() );
            //cs.releaseComponent("MASTER_SCHEDULER");
            System.exit(1);
        }
        System.exit(0);
    }
}
