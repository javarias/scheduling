/*
 * ALMA - Atacama Large Millimiter Array
 * (c) European Southern Observatory, 2002
 * (c) Associated Universities Inc., 2002
 * Copyright by AUI (in the framework of the ALMA collaboration),
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
 * File ALMAMasterScheduler.java
 *
 */

package alma.scheduling.AlmaScheduling;

//import java.util.logging.Logger;

import alma.xmlentity.XmlEntityStruct;
import alma.acs.nc.*;
import alma.acs.container.ContainerServices;
import alma.acs.component.ComponentLifecycle;
import alma.acs.component.ComponentLifecycleException;
import alma.ACS.ComponentStates;

import alma.scheduling.InvalidOperation;
import alma.scheduling.NoSuchSB;
import alma.scheduling.SchedulingInfo;
import alma.scheduling.UnidentifiedResponse;
import alma.scheduling.MasterSchedulerIFOperations;

import alma.scheduling.Define.Policy;
import alma.scheduling.Define.PolicyFactor;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.DateTime;
import alma.scheduling.MasterScheduler.MasterScheduler;
import alma.scheduling.MasterScheduler.Message;
import alma.scheduling.MasterScheduler.MessageQueue;
import alma.scheduling.Scheduler.Scheduler;
import alma.scheduling.Scheduler.SchedulerConfiguration;

/**
 * @author Sohaila Lucero
 */
public class ALMAMasterScheduler extends MasterScheduler 
    implements MasterSchedulerIFOperations, ComponentLifecycle {

    /////////////////////////////////////////////////////////////////////

    // The object that provides the container services
    private ContainerServices containerServices;
    // Name of the component instance
    private String instanceName;
    // The alma obs project manager
    private ALMAProjectManager manager;
    // The alma archive
    private ALMAArchive archive;
    // The clock
    private ALMAClock clock;
    //The master queue of all the scheduling blocks
    //which scheduling has taken out of the archive.
    private SBQueue sbQueue;
    //Interface to the control system
    private ALMAControl control;
    //Interface to the TelescopeOperator
    private ALMAOperator operator;
    // The queue which holds all the messages sent to the operator
    private MessageQueue messageQueue;
    // Information that scheduling wants to know about 
    // all the alma telescopes
    private ALMATelescope telescope;

    private Receiver control_nc;
    private Receiver telcal_nc;
    private Receiver pipeline_nc;
    private ALMAControlReceiver controlreceiver;
    //private ALMATelcalReceiver telcalreceiver;
    //private ALMAPipelineReceiver pipelinereceiver;
    
    /** 
     * Constructor
     */
    public ALMAMasterScheduler() {
        super();
    }

    /////////////////////////////////////////////////////////////////////
    // ComponentLifecycle methods.
    /////////////////////////////////////////////////////////////////////
    
    /**
     * From ComponentLifecycle interface
     */
    public void initialize(ContainerServices cs) 
        throws ComponentLifecycleException {
    
        this.containerServices = cs;
        this.instanceName = containerServices.getComponentInstanceName();
        this.logger = containerServices.getLogger();

        this.archive = new ALMAArchive(containerServices);
        this.sbQueue = new SBQueue();
        this.clock = new ALMAClock();
        this.manager = new ALMAProjectManager(containerServices, archive, sbQueue);
        this.control = new ALMAControl(containerServices);
        this.messageQueue = new MessageQueue();
        this.operator = new ALMAOperator(containerServices, messageQueue);
        this.telescope = new ALMATelescope();
        this.publisher = new ALMASchedPublisher(containerServices);
        
        logger.config("SCHEDULING: MasterScheduler initialized");
    }

    /**
     * From ComponentLifecycle interface
     */
    public void execute() throws ComponentLifecycleException {

        // Connect to the Control NC
        controlreceiver = new ALMAControlReceiver(containerServices);
        control_nc = AbstractNotificationChannel.getReceiver(
            AbstractNotificationChannel.CORBA, alma.Control.CHANNELNAME.value,
                containerServices);
        control_nc.attach("alma.Control.ExecBlockEvent", controlreceiver);
        control_nc.begin();
        // Connect to the TelCal NC
        /*
        telcalreceiver = new ALMATelcalReceiver(containerServices);
        telcal_nc = AbstractNotificationChannel.getReceiver(
            AbstractNotificationChannel.CORBA, alma.TelCalPublisher.CHANNELNAME.value,
                containerServices);
        telcal_nc.attach("alma.TelCalPublisher.FocusReducedEvent", telcalreceiver);
        telcal_nc.attach("alma.TelCalPublisher.PointingReducedEvent", telcalreceiver);
        telcal_nc.begin();
        
        // Connect to the Pipeline NC
        pipelinereceiver = new ALMAPipelineReceiver(containerServices);
        pipeline_nc = AbstractNotificationChannel.getReceiver(
            AbstractNotificationChannel.CORBA, alma.pipelinescience.CHANNELNAME.value,
                containerServices);
        pipeline_nc.attach("alma.pipelinescience.ScienceProcessingRequestEnd",pipelinereceiver);
        pipeline_nc.begin();
        */
    }

    /**
     * From ComponentLifecycle interface
     */
    public void cleanUp() {
        super.setStopCommand(true);
    }

    /**
     * From ComponentLifecycle interface
     */
    public void aboutToAbort() {
        cleanUp();
    }

    /////////////////////////////////////////////////////////////////////

    /**
     * Needed from ACSComponentOperations (MasterSchedulerIFOperations)
     * @return ComponentStates
     */
    public ComponentStates componentState() {
        ComponentStates state = containerServices.getComponentStateManager().getCurrentState();
        return state;
    }
    /**
     * Needed from ACSComponentOperations (MasterSchedulerIFOperations)
     */
    public String name() {
        return instanceName;
    }
    
    /////////////////////////////////////////////////////////////////////
    // MSOperations methods.
    /////////////////////////////////////////////////////////////////////
    private Policy createPolicy() {
        Policy policy = new Policy();
        policy.setId("Temp ID");
        policy.setTimeOfCreation(new DateTime(System.currentTimeMillis()));
        policy.setDescription("R2aPolicy testing!");
        policy.setName("R2aPolicy");
        PolicyFactor[] factors = new PolicyFactor[10];
        /////////////////////
        factors[0] = new PolicyFactor();
        factors[0].setName("positionElevation");
        factors[0].setWeight(0.25);
        factors[0].setDefinition("sin of the current elevation or 0.0 if the source is not visible, max = 1.0 min = 0.0");
        /////////////////////
        factors[1] = new PolicyFactor();
        factors[1].setName("positionMaximum");
        factors[1].setWeight(0.25);
        factors[1].setDefinition("cos of the difference between the current elevation and the maximum elelvation or 0.0 if the source is not visible, max = 1.0 min = 0.0");
        /////////////////////
        factors[2] = new PolicyFactor();
        factors[2].setName("weather");
        factors[2].setWeight(0.5);
        factors[2].setDefinition("weather expression evaluation, max = 1.0 min = 0.0");
        /////////////////////
        factors[3] = new PolicyFactor();
        factors[3].setName("priority");
        factors[3].setWeight(10.0);
        factors[3].setDefinition("the scientific priority, value: 10/9/8/7/6/5/4/3/2/1");
        /////////////////////
        factors[4] = new PolicyFactor();
        factors[4].setName("sameProjectSameBand");
        factors[4].setWeight(10.0);
        factors[4].setDefinition("SB belongs to same project and frequency band as current, value: 1/0");
        /////////////////////
        factors[5] = new PolicyFactor();
        factors[5].setName("sameProjectDifferentBand");
        factors[5].setWeight(-5.0);
        factors[5].setDefinition("SB belongs to same project as current but has a different frequency band, value: 1/0");
        /////////////////////
        factors[6] = new PolicyFactor();
        factors[6].setName("differentProjectSameBand");
        factors[6].setWeight(5.0);
        factors[6].setDefinition("SB belongs to a different project but has same frequency band as current, value: 1/0");
        /////////////////////
        factors[7] = new PolicyFactor();
        factors[7].setName("differentProjectDifferentBand");
        factors[7].setWeight(-10.0);
        factors[7].setDefinition("SB belongs to a different project and has different frequency band from current, value: 1/0");
        /////////////////////
        factors[8] = new PolicyFactor();
        factors[8].setName("newProject");
        factors[8].setWeight(-15.0);
        factors[8].setDefinition("the cost of starting a new project, value: 1/0");
        /////////////////////
        factors[9] = new PolicyFactor();
        factors[9].setName("oneSBRemaining");
        factors[9].setWeight(20.0);
        factors[9].setDefinition("only one SB remains in the project, value: 1/0");
        /////////////////////
        policy.setFactor(factors);
        
        return policy;
    }
    
    /**
     * Starts scheduling using a specific Scheduling Policy
     */
    public void startScheduling(XmlEntityStruct schedulingPolicy) 
        throws InvalidOperation {

        //store scheduling policy int the archive.
        //archive.storeSchedulingPolicy(schedulingPolicy);

        //TODO Eventually populate s_policy with info from the schedulingPolicy
        Policy s_policy = createPolicy();
        logger.info("SCHEDULING: sbqueue size = "+sbQueue.size());
        SchedulerConfiguration config = new SchedulerConfiguration(
            Thread.currentThread(), true, true, sbQueue, sbQueue.size(), 0, 
            (short)0, clock, control, operator, telescope, manager, s_policy, 
            logger, publisher);
        Scheduler scheduler = new Scheduler(config);
        Thread scheduler_thread = new Thread(scheduler);
        scheduler_thread.start();
    }

    public void startInteractiveScheduling() throws InvalidOperation {
        Policy s_policy = createPolicy();
        logger.info("SCHEDULING: sbqueue size = "+sbQueue.size());
        SchedulerConfiguration config = new SchedulerConfiguration(
            Thread.currentThread(), false, true, sbQueue, sbQueue.size(), 0, 
            (short)0, clock, control, operator, telescope, manager, s_policy, 
            logger, publisher);
        Scheduler scheduler = new Scheduler(config);
        Thread scheduler_thread = new Thread(scheduler);
        scheduler_thread.start();
    }
    
    /**
     * Stops all scheduling
     */
    public void stopScheduling() throws InvalidOperation {
        //super.stopScheduling();
    }
   

    /**
     * Returns true if the scheduling subsystem is functional.
     */
    public boolean getStatus() {
        return true;
    }

    /**
     * Returns the schedulingInfo object
     */
    public SchedulingInfo getSchedulingInfo() {
        return null;
    }

    /**
     * 
     */
    public void response(String messageId, String reply) 
        throws UnidentifiedResponse {

        if(logger == null) {
            System.out.println("SCHEDULING: logger is null!");
        }
        logger.info("SCHEDULING: in MS. MessageID = "+messageId);
        logger.info("SCHEDULING: in MS. Reply (sb id) = "+reply);
        logger.info("SCHEDULING: in MS. messageQueue size = "+messageQueue.size());
       
       if(messageQueue.size() < 1) {
            logger.info("SCHEDULING: in MS. MessageQueue was empty. " +
                "Try starting with startScheduling function!");
            return;
        } 
        
        Message item = messageQueue.getMessage(messageId);
        logger.info("SCHEDULING: in MS. Got message with id="+item.getMessageId());
        item.setReply(reply);
        logger.info("SCHEDULING: in MS. message = "+ messageId + 
            " gotten and reply = "+ reply + " sent.");
        item.getTimer().interrupt();
        //messageQueue.removeMessage(messageId);
    
    }

    /**
     *
     */
    public SchedulingInfo getSubarrayInfo() {
        return null;
    }

    /**
     *
     */
    public short createSubarray(short[] antennaIdList, String schedulingMode)
        throws InvalidOperation {
        
        return 0;
    }


    /**
     *
     */
    public void destroySubarray(short subarrayId) throws InvalidOperation {
    }

    /**
     *
     */
    public void executeProject(String projectId, short subarrayId)
        throws InvalidOperation {
    }

    /**
     *
     */
    public void executeSB(String sbId, short subarrayId, String when) {
    }

    /**
     *
     */
    public void stopSB(String sbId) throws InvalidOperation, NoSuchSB {
    
    }

    /**
     * 
     */
    public void pauseScheduling(short subarrayId) {
    }

    /**
     *
     */
    public void resumeScheduling(short subarrayId) {
    }

    /**
     *
     */
    public void manualMode(short antennaId) throws InvalidOperation{
    }

    /**
     *
     */
    public void activeMode(short antennaId) throws InvalidOperation {
    }
     
    /////////////////////////////////////////////////////////////////////
    // Internal MasterScheduler Functions.
    /////////////////////////////////////////////////////////////////////
    
}    
