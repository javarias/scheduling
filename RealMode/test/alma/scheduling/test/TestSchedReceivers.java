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

package ALMA.scheduling.test;

import ALMA.scheduling.receivers.*;
import ALMA.scheduling.master_scheduler.ALMAArchive;
import ALMA.scheduling.master_scheduler.MasterSBQueue;
import ALMA.scheduling.project_manager.ALMAPipeline;

import ALMA.Control.ExecBlockEvent;
import ALMA.Control.ExecBlockEventHelper;
import ALMA.pipelinescience.ScienceProcessingRequestEnd;
import ALMA.pipelinescience.ScienceProcessingRequestEndHelper;

import ALMA.acsnc.*;
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
        
        String[] names = new String[3];
        names[SimpleSupplier.CHANNELPOS] = ALMA.Control.CHANNELNAME.value;
        names[SimpleSupplier.TYPEPOS] = ALMA.Control.EXECEVENTS.value;
        names[SimpleSupplier.HELPERPOS] = "ALMA.Control.ExecBlockEventHelper";
        controlSup = new SimpleSupplier(names);

        names[SimpleSupplier.CHANNELPOS] = ALMA.pipelinescience.CHANNELNAME.value;
        names[SimpleSupplier.TYPEPOS] = "ALMA.pipelinescience.ScienceProcessingRequestEnd";
        names[SimpleSupplier.HELPERPOS] = "ALMA.pipelinescience.ScienceProcessingRequestEndHelper";
        pipelineSup = new SimpleSupplier(names);

    }
    public void createReceivers() {
        try {
            telcal1 = new PointingReducedEventReceiver(container);
            telcal1.addSubscription("PointingReducedEvent");
            telcal1.consumerReady();
            telcal2 = new FocusReducedEventReceiver(container);
            telcal2.addSubscription("FocusReducedEvent");
            telcal2.consumerReady();
            control1 = new ControlEventReceiver(container, new ALMAPipeline(true, container),
                    new ALMAArchive(true, container), new MasterSBQueue());
            control1.addSubscription(ALMA.Control.EXECEVENTS.value);
            control1.consumerReady();
            //control2 = new SchedulerEventReceiver();
            pipeline1 = new PipelineEventReceiver(container, new ALMAPipeline(true, container),
                new ALMAArchive(true,container));
            pipeline1.addSubscription(
                "ALMA.pipelinescience.ScienceProcessingRequestEnd");
            pipeline1.consumerReady();
        } catch (Exception e){
        }
    }
    public void sendEvents() {
        telcalevents.sendTelCalEvents();
        try {
            ExecBlockEvent c_event = new ExecBlockEvent(
                    ALMA.Control.EventReason.START, 
                        "block id", "sb id",(short)1,
                            ALMA.Control.CompletionStatus.COMPLETED_OK, 
                                System.currentTimeMillis());
            controlSup.publishEvent(c_event);
            ScienceProcessingRequestEnd p_event = 
                new ScienceProcessingRequestEnd( "request id","threadname",
                    ALMA.pipelinescience.CompletionStatusEnum.COMPLETE_SUCCEEDED,
                        "TESTING!");
            pipelineSup.publishEvent(p_event);
        }catch(Exception e){ 
        }
    }
    
    public static void main(String[] args) {
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
