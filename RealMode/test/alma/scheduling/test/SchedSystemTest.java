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
 * File SchedSystemTest.java
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
 *  R1 Test! 
 *  @author Sohaila Roberts
 */
public class SchedSystemTest {
    private EntitySerializer entitySerializer;
    private EntityDeserializer entityDeserializer;
    private ContainerServices containerServices;
    private MS masterSchedulerComp;
    private ProjectManager projectManager;
    private SchedBlock[] sbs;
    private Logger logger;
    private TestSchedConsumer consumer;
    
    public SchedSystemTest(ContainerServices cs) {
        this.containerServices = cs;
        this.logger = cs.getLogger();
        entitySerializer = EntitySerializer.getEntitySerializer(logger);
        entityDeserializer = EntityDeserializer.getEntityDeserializer(logger);
        // Get the MS interface as a component to test how the Exec will use it
        try {
            this.masterSchedulerComp = alma.scheduling.MSHelper.narrow(
                containerServices.getComponent("MASTER_SCHEDULER"));
            logger.fine("SCHED_TEST: MS Component created");
        } catch(ContainerException e) {
            logger.severe("SCHED_TEST: "+e.toString());
        }
    }
    
    /**
     *  Calles the scheduling's startScheduling method using an
     *  empty (newly created) SchedulingPolicy
     */
    public void testStartScheduling() {
        XmlEntityStruct sp_struct = new XmlEntityStruct();
        logger.info("SCHED_TEST: About to call startScheduling");
        
        try {
        /*
            SchedulingPolicy sp = new SchedulingPolicy() ; 
            SchedulingPolicyEntityT spe = new SchedulingPolicyEntityT();
            try {
                containerServices.assignUniqueEntityId(spe);
                sp.setSchedulingPolicyEntity(spe);
            } catch (ContainerException e) {
                logger.severe("SCHED_TEST: ContainerException: "+ e.toString());
            }
            sp_struct = entitySerializer.serializeEntity(sp);
            */
            masterSchedulerComp.startScheduling(sp_struct); 
            logger.fine("SCHED_TEST: MasterScheduler startScheduling called.");
        } catch (Exception e) {
            logger.fine("SCHED_TEST: EXCEPTION!");
            logger.severe("SCHED_TEST: error :"+ e.toString());
        /*
        } catch(EntityException e) {
            logger.fine("SCHED_TEST: EntityException: SchedulingPolicy not serialized!");
            logger.severe("SCHED_TEST: EntityException: "+ e.toString());
        } catch(InvalidOperation e) {
            logger.severe("SCHED_TEST: InvalidOperation: startScheduling failed!");
            logger.severe("SCHED_TEST: InvalidOperation: "+ e.toString());
        */
        }
        logger.fine("SCHED_TEST: Scheduling started"); 
        //testNothingToSchedule();
    }

    public void testNothingToSchedule() {
        consumer = new TestSchedConsumer(containerServices);
        System.out.println("SCHED_TEST: SchedConsumer created");
        try {
            consumer.addSubscription(alma.acsnc.DEFAULTTYPE.value);
            consumer.consumerReady();
        } catch(Exception e) {
            consumer.disconnect();
            alma.acs.nc.Helper.disconnect();
        }
    }

    public void testGetStatus() {
        boolean status = masterSchedulerComp.getStatus();
        if(status) {
            logger.info("SCHED_TEST: MasterScheduler's status = executing!");
        } else {
            logger.info("SCHED_TEST: MasterScheduler's status is not executing!");
        }
    }

    public void testStopScheduling() {
        try {
            logger.info("SCHED_TEST: About to call stopScheduling");
            masterSchedulerComp.stopScheduling();
        } catch (InvalidOperation e) {
            logger.severe("SCHED_TEST: InvalidOperation: "+ e.toString());
        }
    }
    
    public void release() {
        logger.info("SCHED_TEST: About to release components");
        containerServices.releaseComponent("MASTER_SCHEDULER");
        //containerServices.releaseComponent("PUBLISHTELCALEVENTS1");
    }


    public static void main(String[] args) {
        try {
            String name = "Scheduling Subsystem Test";
            String managerLoc = System.getProperty("ACS.manager");
            ComponentClient client = new ComponentClient(null, managerLoc, name);
            ContainerServices cs = client.getContainerServices();
            
            SchedSystemTest test = new SchedSystemTest(cs);
            
            TestSchedSupplier telcal_events = new TestSchedSupplier();

            test.testStartScheduling();

            test.testNothingToSchedule();

            //Publish the telcalevents!
            telcal_events.sendTelCalEvents();
            
            Thread.sleep(1000*90);
            
            test.testGetStatus();
            
            test.testNothingToSchedule();

            test.testStopScheduling();
            
            telcal_events.disconnect(); 

            test.release();
        } catch(Exception e) {
            System.out.println("SCHED_TEST: Exception: " +e.toString() );
            System.exit(1);
        }
        System.exit(0);
    }
}
