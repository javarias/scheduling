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
 * File TestSchedReceivers.java
 * 
 */

package alma.scheduling.test;

import alma.scheduling.receivers.*;
import alma.scheduling.master_scheduler.ALMAArchive;
import alma.scheduling.master_scheduler.MasterSBQueue;
import alma.scheduling.project_manager.ALMAPipeline;

import alma.Control.ExecBlockEvent;
import alma.Control.ExecBlockEventHelper;
import alma.pipelinescience.ScienceProcessingRequestEnd;
import alma.pipelinescience.ScienceProcessingRequestEndHelper;

import alma.acsnc.*;
import alma.acs.nc.*;
import org.omg.CosNotification.StructuredEvent;

import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.acs.component.client.ComponentClient;

import java.util.logging.Logger;

/**
 * This class tests the receivers in the Scheduling subsystem.
 * This class uses the TestSchedSupplier class coz it creates
 * all the TelCal Events.
 * @author Sohaila Roberts
 */
public class TestSchedReceivers {

    private ContainerServices container;
    private Logger logger;
    private SimpleSupplier controlSup;
    private SimpleSupplier pipelineSup;
    
    private PointingReducedEventReceiver telcal1;
    private FocusReducedEventReceiver telcal2;
    private ControlEventReceiver control1;
    private SchedulerEventReceiver control2;
    private PipelineEventReceiver pipeline1;
    
    private TestSchedSupplier telcalevents;
    
    public TestSchedReceivers(ContainerServices cs) {
        this.container = cs;
        logger = cs.getLogger();
        logger.info("SCHED_TEST: Testing Receivers!");
    }

    public void createSuppliers() {
        telcalevents = new TestSchedSupplier();
        controlSup = new SimpleSupplier ( alma.Control.CHANNELNAME.value,
                    alma.Control.ExecBlockEvent.class);
        pipelineSup = new SimpleSupplier ( alma.pipelinescience.CHANNELNAME.value,
                    alma.pipelinescience.ScienceProcessingRequestEnd.class );
        
        /*
        String[] names = new String[3];
        names[SimpleSupplier.CHANNELPOS] = alma.Control.CHANNELNAME.value;
        names[SimpleSupplier.TYPEPOS] = alma.Control.EXECEVENTS.value;
        names[SimpleSupplier.HELPERPOS] = "alma.Control.ExecBlockEventHelper";
        controlSup = new SimpleSupplier(names);

        names[SimpleSupplier.CHANNELPOS] = alma.pipelinescience.CHANNELNAME.value;
        names[SimpleSupplier.TYPEPOS] = "alma.pipelinescience.ScienceProcessingRequestEnd";
        names[SimpleSupplier.HELPERPOS] = "alma.pipelinescience.ScienceProcessingRequestEndHelper";
        pipelineSup = new SimpleSupplier(names);
        */

    }
    public void createReceivers() {
        try {
            telcal1 = new PointingReducedEventReceiver(container);
            telcal1.addSubscription(alma.TelCalPublisher.PointingReducedEvent.class);
            //telcal1.addSubscription("PointingReducedEvent");
            telcal1.consumerReady();
            telcal2 = new FocusReducedEventReceiver(container);
            telcal2.addSubscription(alma.TelCalPublisher.FocusReducedEvent.class);
            //telcal2.addSubscription("FocusReducedEvent");
            telcal2.consumerReady();
            control1 = new ControlEventReceiver(container, new ALMAPipeline(true, container),
                    new ALMAArchive(true, container), new MasterSBQueue());
            control1.addSubscription(alma.Control.EXECEVENTS.class);
            control1.consumerReady();
            //control2 = new SchedulerEventReceiver();
            pipeline1 = new PipelineEventReceiver(container, new ALMAPipeline(true, container),
                new ALMAArchive(true,container));
            pipeline1.addSubscription(
                alma.pipelinescience.ScienceProcessingRequestEnd.class);
            pipeline1.consumerReady();
        } catch (Exception e){
        }
    }
    public void sendEvents() {
        telcalevents.sendTelCalEvents();
        try {
            ExecBlockEvent c_event = new ExecBlockEvent(
                    alma.Control.EventReason.START, 
                        "block id", "sb id",(short)1,
                            alma.Control.CompletionStatus.COMPLETED_OK, 
                                System.currentTimeMillis());
            controlSup.publishEvent(c_event);
            ScienceProcessingRequestEnd p_event = 
                new ScienceProcessingRequestEnd( "request id","threadname",
                    alma.pipelinescience.CompletionStatusEnum.COMPLETE_SUCCEEDED,
                        "TESTING!");
            pipelineSup.publishEvent(p_event);
        }catch(Exception e){ 
        }
    }
    
    public static void main(String[] args) {
        System.out.println("This is the receivers test.");
        try {
            String name = "TestReceivers";
            String managerLoc = System.getProperty("ACS.manager");
            ComponentClient client = new ComponentClient(null, managerLoc, name);
            ContainerServices cs = client.getContainerServices();
    
            TestSchedReceivers test = new TestSchedReceivers(cs);
            test.createSuppliers();
            test.createReceivers();
            test.sendEvents();
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
