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

import java.util.Vector;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.sql.Timestamp;

import alma.xmlentity.XmlEntityStruct;
import alma.acs.nc.*;
import alma.acs.container.ContainerServices;
import alma.acs.component.ComponentLifecycle;
import alma.acs.component.ComponentLifecycleException;
import alma.acs.component.ComponentQueryDescriptor;
import alma.ACS.ComponentStates;
import si.ijs.maci.ComponentSpec;


import alma.alarmsystem.source.ACSAlarmSystemInterfaceFactory;
import alma.alarmsystem.source.ACSAlarmSystemInterface;
import alma.alarmsystem.source.ACSFaultState;
import cern.cmw.mom.pubsub.impl.ACSJMSTopicConnectionImpl;

import alma.scheduling.*;
import alma.SchedulingExceptions.InvalidOperationEx;
import alma.SchedulingExceptions.InvalidObjectEx;
import alma.SchedulingExceptions.UnidentifiedResponseEx;
import alma.SchedulingExceptions.SBExistsEx;
import alma.SchedulingExceptions.NoSuchSBEx;
import alma.SchedulingExceptions.wrappers.AcsJInvalidOperationEx;
import alma.SchedulingExceptions.wrappers.AcsJInvalidObjectEx;
import alma.SchedulingExceptions.wrappers.AcsJUnidentifiedResponseEx;
import alma.SchedulingExceptions.wrappers.AcsJNoSuchSBEx;
import alma.SchedulingExceptions.wrappers.AcsJSBExistsEx;

import alma.scheduling.Define.Policy;
import alma.scheduling.Define.PolicyFactor;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.Subarray;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.MasterScheduler.MasterScheduler;
import alma.scheduling.MasterScheduler.Message;
import alma.scheduling.MasterScheduler.MessageQueue;
import alma.scheduling.Scheduler.*;
//import alma.scheduling.GUI.InteractiveSchedGUI.GUIController;
//import alma.scheduling.GUI.InteractiveSchedGUI.ArchiveQueryWindowController;
import alma.scheduling.ObsProjectManager.ProjectManagerTaskControl;


/**
 * @author Sohaila Lucero
 * @version $Id: ALMAMasterScheduler.java,v 1.91 2007/05/01 21:08:06 sslucero Exp $
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
    //Interface to the pipeline subsystem
    //MOVED TO ALMAProjectManager
    //private ALMAPipeline pipeline;

    // receiver for control nc
    private Receiver control_nc;
    // receiver for telcal nc
    private Receiver telcal_nc;
    // receiver for pipeline nc
    private Receiver pipeline_nc;
    // event receiver for all events
    private ALMAReceiveEvent eventreceiver;
    private LinkedHashMap<String, Scheduler> allSchedulers;
    private Vector interactiveComps;
    private Vector queuedComps;
    private Vector dynamicComps;
    //queued scheduler count
    private int q_sched_count=0;
    //interactive scheduler count
    private int i_sched_count=0;
    //dyanmic scheduler count
    private int d_sched_count=0;
    //keep track of arrays
    private Vector arraysInUse;
    
    /** 
     * Constructor
     */
    public ALMAMasterScheduler() {
        super();
        ACSJMSTopicConnectionImpl.containerServices=containerServices;
    }

    /////////////////////////////////////////////////////////////////////
    // ComponentLifecycle methods.
    /////////////////////////////////////////////////////////////////////
    
    /**
     * From ComponentLifecycle interface
     * @throws ComponentLifecycleException
     */
    public void initialize(ContainerServices cs) 
        throws ComponentLifecycleException {
    
        try {
            //Start the MasterScheduler Thread! 
            this.msThread.start();
            this.interactiveComps = new Vector();
            this.queuedComps = new Vector();
            this.dynamicComps = new Vector();
            allSchedulers = new LinkedHashMap<String, Scheduler>();
            this.containerServices = cs;
            this.instanceName = containerServices.getName();
            this.logger = containerServices.getLogger();

            this.clock = new ALMAClock();
            this.archive = new ALMAArchive(containerServices, clock);
            this.sbQueue = new SBQueue();
            this.publisher = new ALMAPublishEvent(containerServices);
            this.messageQueue = new MessageQueue();
            this.operator = new ALMAOperator(containerServices, messageQueue);
            this.manager = new ALMAProjectManager(containerServices, operator, archive, sbQueue, publisher, clock);
            this.telescope = new ALMATelescope();
            this.control = new ALMAControl(containerServices, manager);
            this.arraysInUse = new Vector();
        
            logger.info("SCHEDULING: MasterScheduler initialized");
        } catch(Exception e){
            logger.severe("SCHEDULING: Error initializing MASTER SCHEDULER (initialize)");
            throw new ComponentLifecycleException(e.toString());
        }
    }

    /**
     * From ComponentLifecycle interface
     * @throws ComponentLifecycleException
     */
    public void execute() throws ComponentLifecycleException {
        try {
            //Start the project manager's thread!
            Thread pmThread = containerServices.getThreadFactory().newThread(manager);
            manager.setProjectManagerTaskControl(new ProjectManagerTaskControl(msThread, pmThread));
            pmThread.start();

            // Connect to the Control NC
            eventreceiver = new ALMAReceiveEvent(containerServices, manager, 
                                             (ALMAPublishEvent)publisher);
            control_nc = AbstractNotificationChannel.getReceiver(
                AbstractNotificationChannel.CORBA, 
                alma.Control.CHANNELNAME_CONTROLSYSTEM.value,
                    containerServices);
            control_nc.attach("alma.Control.ExecBlockStartedEvent", eventreceiver);
            control_nc.attach("alma.Control.ExecBlockEndedEvent", eventreceiver);
            control_nc.attach("alma.offline.ASDMArchivedEvent", eventreceiver);
            control_nc.begin();
            // Connect to the TelCal NC
            telcal_nc = AbstractNotificationChannel.getReceiver(
                AbstractNotificationChannel.CORBA, 
                alma.TelCalPublisher.CHANNELNAME_TELCALPUBLISHER.value,
                containerServices);
            telcal_nc.attach("alma.TelCalPublisher.AmpliCalReducedEvent", eventreceiver);
            telcal_nc.attach("alma.TelCalPublisher.AmpCurveReducedEvent", eventreceiver);
            telcal_nc.attach("alma.TelCalPublisher.AntennaPositionsReducedEvent", eventreceiver);
            telcal_nc.attach("alma.TelCalPublisher.AtmosphereReducedEvent", eventreceiver);
            telcal_nc.attach("alma.TelCalPublisher.DelayReducedEvent", eventreceiver);
            telcal_nc.attach("alma.TelCalPublisher.FocusReducedEvent", eventreceiver);
            telcal_nc.attach("alma.TelCalPublisher.PhaseCalReducedEvent", eventreceiver);
            telcal_nc.attach("alma.TelCalPublisher.PhaseCurveReducedEvent", eventreceiver);
            telcal_nc.attach("alma.TelCalPublisher.PointingReducedEvent", eventreceiver);
            telcal_nc.attach("alma.TelCalPublisher.PointingModelReducedEvent", eventreceiver);
            telcal_nc.attach("alma.TelCalPublisher.SkydipReducedEvent", eventreceiver);
            telcal_nc.begin();
        
            // Connect to the Pipeline NC
            pipeline_nc = AbstractNotificationChannel.getReceiver(
                AbstractNotificationChannel.CORBA, 
                alma.pipelinescience.CHANNELNAME_SCIPIPEMANAGER.value,
                    containerServices);
            pipeline_nc.attach("alma.pipelinescience.ScienceProcessingDoneEvent",eventreceiver);
            pipeline_nc.begin();
        
        } catch(Exception e){
            logger.severe("SCHEDULING: Error initializing MASTER SCHEDULER (execute)");
            throw new ComponentLifecycleException(e.toString());
        }
    }

    /**
     * From ComponentLifecycle interface
     */
    public void cleanUp() {
        try {
            stopScheduling();
        } catch(Exception e){
            e.printStackTrace(System.out);
        }
        logger.info("SCHEDULING: cleaning up scheduling component for shutdown.");
        super.setStopCommand(true);
        this.manager.setStopCommand(true);
        this.manager.managerStopped();
        this.control.releaseControlComp();
        this.archive.releaseArchiveComponents();
        control_nc.detach("alma.Control.ExecBlockStartedEvent", eventreceiver);
        control_nc.detach("alma.Control.ExecBlockEndedEvent", eventreceiver);
        ((CorbaReceiver)control_nc).disconnect();
        pipeline_nc.detach("alma.pipelinescience.ScienceProcessingDoneEvent",eventreceiver);
        ((CorbaReceiver)pipeline_nc).disconnect();
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
     * @return String
     */
    public String name() {
        return instanceName;
    }

    //////////////////////////////////
    public void sendAlarm(String ff, String fm, int fc, String fs) {
        try {
            //logger.info("Sending ALARM");
            ACSAlarmSystemInterface alarmSource = ACSAlarmSystemInterfaceFactory.createSource(this.name());
            ACSFaultState state = ACSAlarmSystemInterfaceFactory.createFaultState(ff, fm, fc);
            Properties prop = new Properties();
            state.setDescriptor(fs);
            state.setUserTimestamp(new Timestamp(clock.getDateTime().getMillisec()));
            prop.setProperty(ACSFaultState.ASI_PREFIX_PROPERTY, "prefix");
			prop.setProperty(ACSFaultState.ASI_SUFFIX_PROPERTY, "suffix");
			prop.setProperty("ALMAMasterScheduling_PROPERTY", "InvalidOperationException");
			state.setUserProperties(prop);
			alarmSource.push(state);
        } catch(Exception e) {
            logger.severe("Problem sending alarm: "+e.toString());
            e.printStackTrace();
        }
    }
    
    /////////////////////////////////////////////////////////////////////
    // MSOperations methods.
    /////////////////////////////////////////////////////////////////////
    /**
     * Temporary method to populate a Policy
     * @return Policy
     */
    public Policy createPolicy() {
        Policy policy = new Policy();
        policy.setId("Temp ID");
        policy.setTimeOfCreation(new DateTime(System.currentTimeMillis()));
        policy.setDescription("R4.0Policy testing!");
        policy.setName("R4.0Policy");
        PolicyFactor[] factors = new PolicyFactor[4];
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
        /*
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
        */
        /////////////////////
        policy.setFactor(factors);
        
        return policy;
    }

    /**
      * Creates a scheduler configuration
      */
    public SchedulerConfiguration createSchedulerConfiguration(boolean special, 
                                                               Object sbs,
                                                               boolean dynamic,
                                                               boolean synchronous,
                                                               int sleepTime,
                                                               String arrayName,
                                                               Policy policy) {
        if(special){
            return new SchedulerConfiguration(
                    Thread.currentThread(), dynamic,// manager.getSpecialSBs(),
                    manager.getSpecialSBs(),arrayName, clock, control, operator, 
                    telescope, manager, policy, logger);
                    //ALMASchedulingUtility.getMasterSchedulerThread(), dynamic, 
        } else {
            logger.info("SCHEDULING: creating scheduler configuration with "+((SBQueue)sbs).size()+" sbs");
     
            return new SchedulerConfiguration(
                    Thread.currentThread(), dynamic, //synchronous, (SBQueue)sbs, 
                    synchronous, (SBQueue)sbs, ((SBQueue)sbs).size(), sleepTime, 
                    arrayName, clock, control, operator, telescope, manager, 
                    policy, logger);
                    //ALMASchedulingUtility.getMasterSchedulerThread(), dynamic, 
        }
    }
    
///// START of Executive_to_Scheduling interface implementation

    /**
     * Starts scheduling using a specific Scheduling Policy
     * @param XmlEntityStruct
     * @throws InvalidOperationEx
     */
    public void startScheduling(XmlEntityStruct schedulingPolicy) 
        throws InvalidOperationEx {

        logger.warning("SCHEDULING: this method doesn't do anything!");
        logger.warning("SCHEDULING: please use startScheduling1(schedulingPolicy, arrayname)");
    }

    public void startScheduling1(XmlEntityStruct schedulingPolicy, String arrayname) {
            
        logger.fine("SCHEDULING: Starting dynamic scheduling");
        try {
            manager.checkForProjectUpdates();
            logger.fine("SCHEDULING: got project updates");
            Policy s_policy = createPolicy();
            //TODO: Handle this better..
            SBQueue dynamicSBs=manager.getDynamicSBQueue();
            logger.fine("SCHEDULING: got "+dynamicSBs.size()+" sbs for dynamic scheduling");
            SchedulerConfiguration config = 
                createSchedulerConfiguration (
                        false, dynamicSBs, true, true, 5, arrayname, s_policy);
            logger.fine("SCHEDULING: Master Scheduler creating dynamic scheduler for regular SBs");
            if(!isArrayInUse(arrayname)){
                setArrayInUse(arrayname);
            }
            DynamicScheduler scheduler = new DynamicScheduler(config);
            //get UID for scheduler
            String id = archive.getIdForScheduler();
            scheduler.setId(id);
            //add to Map
            allSchedulers.put(id, scheduler);
            Thread schedulerThread = containerServices.getThreadFactory().newThread(scheduler);
            schedulerThread.start();
            //get component and set its ID
            Dynamic_Operator_to_Scheduling comp = 
                alma.scheduling.Dynamic_Operator_to_SchedulingHelper.narrow(
                        containerServices.getComponent("DS_"+arrayname));
            comp.setSchedulerId(id);
            //dynamic scheduling component is set in dynamicComps when it is created
            //with the createDynamicSchedulingComponent. Split up coz of call back setting stuff..
            while(!stopCommand) {
                try {
                    schedulerThread.join();
                    break;
                } catch(InterruptedException ex) {
                    if(config.isNothingToSchedule()){
                        config.respondStop();
                        logger.finest("SCHEDULING: interrupted regular sched thread in MS");
                        manager.publishNothingCanBeScheduled(NothingCanBeScheduledEnum.OTHER);
                    }
                }
            }
            if(!config.isOperational()) {
                logger.fine("SCHEDULING: Dynamic Scheduler has ended at " + config.getActualEndTime());
                //release DSComp
                logger.fine("SCHEDULING: DS Component ("+comp.name()+") about to be released");
                //logger.info("SCHEDULING_MS: releasing "+comp.name());
                releaseDSComp(comp.name());
            }
//            destroyArray(arrayname);
        } catch(Exception e) {
        	sendAlarm("Scheduling","SchedSchedulerConnAlarm",3,ACSFaultState.ACTIVE);
        	try {
            	Thread.sleep(1000);
            } catch (InterruptedException e1) {
            	e1.printStackTrace(System.out);
            }
        //    InvalidOperation e1 = new InvalidOperation("startScheduling", e.toString());
        //    AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
        //    throw e2.toInvalidOperationEx();
        }
    }

    private void releaseDSComp(String name){
        for(int i=0; i < dynamicComps.size(); i++){
            if(((Dynamic_Operator_to_Scheduling)dynamicComps.elementAt(i)).name().equals(name)){
                try {
                    containerServices.releaseComponent(name);
                } catch(Exception e){
                    logger.warning("SCHEDULING: Error releasing dynamic scheduling component");
                    e.printStackTrace();
                }
            }
        }
    }

    public String createDynamicSchedulingComponent(String arrayname) 
        throws InvalidOperationEx {
   
        try {
            ComponentQueryDescriptor x = new ComponentQueryDescriptor(
                 "DS_"+arrayname, 
                 "IDL:alma/scheduling/Dynamic_Operator_to_Scheduling:1.0");
            Dynamic_Operator_to_Scheduling dsComp = 
                alma.scheduling.Dynamic_Operator_to_SchedulingHelper.narrow(
                    containerServices.getDynamicComponent(x, false));
            dynamicComps.add(dsComp);
            return dsComp.name();
        } catch (Exception e) {
            logger.severe("SCHEDULING: Error starting DS Comp; "+e.toString());
            e.printStackTrace();
        	sendAlarm("Scheduling","SchedSchedulerConnAlarm",3,ACSFaultState.ACTIVE);
            InvalidOperation e1 = new InvalidOperation(
                    "createDyanmicSchedulingComponent", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
   }

    //private void dynamicScheduling(XmlEntityStruct schedulingPolicy, 
    //        String arrayname) throws InvalidOperationEx{
        //TODO eventually use this inside the startScheduling methods
    //}

    /**
     * Starts scheduling using a specific list of scheduling block Ids.
     * @param sbList
     * @throws InvalidOperationEx
     * @deprecated Use startQueuedScheduling(String[] sbList, String arrayname)
     */
    public void startQueueScheduling(String[] sbList)
    	throws InvalidOperationEx {

            logger.warning("SCHEDULING: This method does nothing!!");
            logger.warning("SCHEDULING: Use startQueuedScheduling(sbList, arrayName)");
	}
    
    public void startQueuedScheduling(String[] sbList, String arrayname)
    	throws InvalidOperationEx {

        try {    
            manager.checkForProjectUpdates();
            //create a queue of sbs with these ids, 
            SBQueue sbs = manager.mapQueuedSBsToProjects(sbList);
            if(sbs == null || sbs.size() ==0){
                InvalidOperation e1 = new InvalidOperation("startQueuedScheduling",
                        "Cannot schedule without a SB queue");
                AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
                throw e2.toInvalidOperationEx();
            }
            //create policy
            Policy s_policy = createPolicy();
            //validate array
            if(arrayname.equals("") || arrayname == null) {
                InvalidOperation e1 = new InvalidOperation("startQueuedScheduling",
                        "Invalid array..");
                AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
                throw e2.toInvalidOperationEx();
            }
            //then create a config 
            SchedulerConfiguration config = 
                createSchedulerConfiguration(
                        false, sbs, true, true, 5, arrayname, s_policy);
                
            //a scheduler and go from there!
            logger.info("SCHEDULING: Master Scheduler creating queued scheduler");
            if(!isArrayInUse(arrayname)){
                setArrayInUse(arrayname);
            }
            QueuedSBScheduler scheduler = new QueuedSBScheduler(config);
            //get QS Comp and get id to map to scheduler
            Queued_Operator_to_Scheduling qsComp =
                alma.scheduling.Queued_Operator_to_SchedulingHelper.narrow(
                        containerServices.getComponent("QS_"+arrayname));
            scheduler.setId(qsComp.getSchedulerId());
            //add to Map
            allSchedulers.put(qsComp.getSchedulerId(), scheduler);
            containerServices.releaseComponent(qsComp.name());
            
            Thread scheduler_thread = containerServices.getThreadFactory().newThread(scheduler);
            scheduler_thread.start();
            while(true) {
                try {
                    scheduler_thread.join();
                    break;
                } catch(InterruptedException e) {
                        logger.finest("SCHEDULING: interrupted sched thread in MS");
                        manager.publishNothingCanBeScheduled(NothingCanBeScheduledEnum.OTHER);
                }
            }
            if(!config.isOperational()) {
                logger.info("SCHEDULING: Queued scheduler has ended at " + config.getActualEndTime());
            }
            //destroyArray(arrayname);
        } catch (Exception e){
        	sendAlarm("Scheduling","SchedSchedulerConnAlarm",3,ACSFaultState.ACTIVE);
        	try {
            	Thread.sleep(1000);
            } catch (InterruptedException e1) {
            	e1.printStackTrace(System.out);
            }
            e.printStackTrace(System.out);
            InvalidOperation e1 = new InvalidOperation("startQueueScheduling", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
            
	}

    public String createQueuedSchedulingComponent(String arrayname) 
        throws InvalidOperationEx{
       
        try {
            ComponentQueryDescriptor x = new ComponentQueryDescriptor(
                 "QS_"+arrayname, 
                 "IDL:alma/scheduling/Queued_Operator_to_Scheduling:1.0");
            Queued_Operator_to_Scheduling qsComp = 
                alma.scheduling.Queued_Operator_to_SchedulingHelper.narrow(
                    containerServices.getDynamicComponent(x, false));
            queuedComps.add(qsComp);
            //get UID for scheduler
            String id = archive.getIdForScheduler();
            qsComp.setSchedulerId(id);
            return qsComp.name();
        } catch (Exception e) {
            logger.severe("SCHEDULING: Error starting QS Comp; "+e.toString());
            e.printStackTrace();
        	sendAlarm("Scheduling","SchedSchedulerConnAlarm",3,ACSFaultState.ACTIVE);
            InvalidOperation e1 = new InvalidOperation(
                    "createQueuedSchedulerComponent", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx(); 
        }
    }
    /**
      * @throws InvalidOperationEx
      */
    public void startInteractiveScheduling() throws InvalidOperationEx {
       // logger.info("SCHEDULING: startInteractiveScheduling called");
        logger.warning("SCHEDULING: this method doesn't do anything");
        logger.warning("SCHEDULING: please use startInteractiveScheduling1(arrayname)");
    }
    /**
      * returns name of scheduler
      */
    public String startInteractiveScheduling1
        (String arrayname) throws InvalidOperationEx {
            
        try {
            /* check archive for updates */
            manager.checkForProjectUpdates();
            /* create temporary scheduling policy*/
            Policy s_policy = createPolicy();
            /* create interactive scheduling component */
            ComponentQueryDescriptor x = new ComponentQueryDescriptor(
                    "IS_"+arrayname, 
                    "IDL:alma/scheduling/Interactive_PI_to_Scheduling:1.0");
            Interactive_PI_to_Scheduling schedComp = 
                alma.scheduling.Interactive_PI_to_SchedulingHelper.narrow(
                    containerServices.getDynamicComponent(x, false));
            interactiveComps.add(schedComp);
            /* create interactive scheduler */
            
            SchedulerConfiguration config = 
                createSchedulerConfiguration(
                        false, new SBQueue(), false, true, 0, arrayname, s_policy);

            logger.info("SCHEDULING: Starting interactive scheduling on array "+arrayname);
            if(!isArrayInUse(arrayname)){
                setArrayInUse(arrayname);
            }
            InteractiveScheduler scheduler = new InteractiveScheduler(config);
            //Thread scheduler_thread = containerServices.getThreadFactory().newThread(scheduler);
            //scheduler_thread.start();

            String id = archive.getIdForScheduler();
            scheduler.setId(id);
            schedComp.setSchedulerId(id);
            String name = schedComp.name();
            //add to Map
            allSchedulers.put(id, scheduler);
            
            /////
        //    Policy s_policy = createPolicy();
          //  SchedulerConfiguration config = 
            //    createSchedulerConfiguration(
              //          false, new SBQueue(), false, true, 0, arrayname, s_policy);
           // ((InteractiveScheduler)sched).setConfiguration(config);
            /////
            return name;
        } catch(Exception e){
        	sendAlarm("Scheduling","SchedSchedulerConnAlarm",3,ACSFaultState.ACTIVE);
        	try {
            	Thread.sleep(1000);
            } catch (InterruptedException e1) {
            	e1.printStackTrace(System.out);
            }
            e.printStackTrace();
            InvalidOperation e1 = new InvalidOperation(
                    "startInteractiveScheduling1", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }

    /**
      * Stop and release scheduler with name = n
      */
    public void stopInteractiveScheduler(String n){
        String id=null;
        for (int i=0; i < interactiveComps.size(); i++){
            Interactive_PI_to_Scheduling comp = 
                (Interactive_PI_to_Scheduling)interactiveComps.elementAt(i);
            if(comp.name().equals(n)) {
                logger.info("SCHEDULING: Stopping interactive scheduler component "
                        +comp.name());
                containerServices.releaseComponent(comp.name());
            }
        }
    }


    public void stopQueuedScheduler(String n){
        String id=null;
        for (int i=0; i < queuedComps.size(); i++){
            Queued_Operator_to_Scheduling comp = (Queued_Operator_to_Scheduling)queuedComps.elementAt(i);
            if(comp.name().equals(n)) {
                logger.info("SCHEDULING: Stopping queued scheduler "+comp.name());
                id= comp.getSchedulerId();
                QueuedSBScheduler scheduler = (QueuedSBScheduler)allSchedulers.get(id);
                scheduler = null;
                allSchedulers.remove(id);
                containerServices.releaseComponent(comp.name());
                queuedComps.removeElementAt(i);
            }
        }
    }
        
    public void stopDynamicScheduler(String name){
        String id=null;
        logger.info("Size of dynamicComps = "+dynamicComps.size());
        for (int i=0; i < dynamicComps.size(); i++){
            Dynamic_Operator_to_Scheduling comp = (Dynamic_Operator_to_Scheduling)dynamicComps.elementAt(i);
            if(comp.name().equals(name)) {
                logger.info("SCHEDULING: Stopping dynamic scheduler "+comp.name());
                id= comp.getSchedulerId();
                DynamicScheduler scheduler = (DynamicScheduler)allSchedulers.get(id);
                scheduler = null;
                allSchedulers.remove(id);
                containerServices.releaseComponent(comp.name());
                dynamicComps.removeElementAt(i);
            }
        }
    }
    
    /**
     * Stops all scheduling
     * @throws InvalidOperationEx
     */
    public void stopScheduling() throws InvalidOperationEx {
        logger.info("SCHEDULING: Stop scheduling called. Will Stop activitiy on ALL schedulers");
        try {
            //stop and release all interactive components
            for(int i=0; i< interactiveComps.size(); i++){
                Interactive_PI_to_Scheduling comp = (Interactive_PI_to_Scheduling)interactiveComps.elementAt(i);
                logger.info("SCHEDULING: Stopping component "+comp.name());
                containerServices.releaseComponent(comp.name());
            }
            //stop and release all queued components
            //stop and release all dynamic components
            for(int i=0; i< dynamicComps.size(); i++){
                Dynamic_Operator_to_Scheduling comp = (Dynamic_Operator_to_Scheduling )dynamicComps.elementAt(i);
                logger.info("SCHEDULING: Stopping component "+comp.name());
                containerServices.releaseComponent(comp.name());
            }
            ///////////////////
            control.stopAllScheduling();
            super.stopScheduling();
            control.stopAllSchedulingOnAllAutomaticArrays();
        } catch(Exception e) {
            InvalidOperation e1 = new InvalidOperation("stopScheduling",e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }
   

    /**
     * Returns true if the scheduling subsystem is functional.
     * @return boolean 
     */
    public boolean getStatus() {
        return true;
    }

    /**
     * Returns the schedulingInfo object
     * @return SchedulingInfo
     */
    public SchedulingInfo getSchedulingInfo() {
        String comment = "Information about the scheduling system.";
        String currentTime = clock.getDateTime().toString();
        String timeZone = String.valueOf(telescope.getSite().getTimeZone());
        ArrayInfo[] arrayInfo = control.getAllArraysInfo();
        SchedulingInfo schedInfo =
            new SchedulingInfo(comment, currentTime, timeZone, arrayInfo);
        return schedInfo;
    }

    /**
      *
      */
    public SBLite[] getSBLites() {
        return manager.getSBLites();
    }

    public SBLite[] getSBLite(String[] ids){
        return manager.getSBLite(ids);
    }
    public ProjectLite[] getProjectLites(String[] ids) {
        return manager.getProjectLites(ids);
    }
    public SBLite[] getSBLitesForProject(String projectId) {
        return manager.getSBLitesForProject(projectId);
    }
    public ProjectLite getProjectLiteForSB(String sbId){
        return manager.getProjectLiteForSB(sbId);
    }


    public synchronized SchedulerInfo[] getAllActiveSchedulers(){
        int total = allSchedulers.size();
        //Set<String,Scheduler> entries = allSchedulers.entrySet(); -- didn't like
        Set entries = allSchedulers.entrySet();
        SchedulerInfo[] all = new SchedulerInfo[total];
        SchedulerInfo info;
        String type;
        int x=0;
        for(Iterator i= entries.iterator(); i.hasNext();){
            info = new SchedulerInfo();
            Map.Entry e = (Map.Entry) i.next();
            //scheduler id
            info.schedulerId =(String)e.getKey();
            //scheduler type
            type =((Scheduler)e.getValue()).getType(); 
            info.schedulerType =type;
            info.schedulerArray = ((Scheduler)e.getValue()).getArrayName();
            info.schedulerCompName = getComponentName(type, (String)e.getKey());
            logger.info("SchedulerInfo => id="+info.schedulerId+"; type="+
                    info.schedulerType+"; compName="+info.schedulerCompName);
            all[x++] = info;
        }
        return all;
    }

    ////////
    // These two give actual array names, not component names of the arrays    
    public String[] getActiveAutomaticArrays() {
        String[] foo = new String[1];
        try {
            foo = control.getActiveAutomaticArrays();
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("SCHEDULING: Error returning active automatic arrays");
            foo[0] = "Problem with method";
        }
        return foo;
    }

    public String[] getActiveManualArrays() { 
        String[] foo = new String[1];
        try {
            foo = control.getActiveManualArrays();
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("SCHEDULING: Error returning active manual arrays");
            foo[0] = "Problem with method";
        }
        return foo;
    }
    ///////////////////
    private synchronized String getComponentName(String type, String id){
        if(type.equals("interactive")){
            for(int i=0; i <  interactiveComps.size(); i++){
                if( ((Interactive_PI_to_Scheduling)interactiveComps.elementAt(i)).
                        getSchedulerId().equals(id) ){
                    return ((Interactive_PI_to_Scheduling)
                            interactiveComps.elementAt(i)).name();
                }
            }
        }else if (type.equals("queued")){
            for(int i=0; i < queuedComps.size(); i++){
                if( ((Queued_Operator_to_Scheduling)queuedComps.elementAt(i)).
                        getSchedulerId().equals(id) ){
                    return ((Queued_Operator_to_Scheduling)
                            queuedComps.elementAt(i)).name();
                }
            }
        }else if (type.equals("dynamic")){
            for(int i=0; i < dynamicComps.size();i++){
                if( ((Dynamic_Operator_to_Scheduling)dynamicComps.elementAt(i)).
                        getSchedulerId().equals(id) ){
                    return ((Dynamic_Operator_to_Scheduling)
                            dynamicComps.elementAt(i)).name();
                }
            }
        }
        return "Error";
    }

///// END of Executive_to_Scheduling interface implementation
    
///// START of TelescopeOperator_to_Scheduling interface implementation
    /**
     * 
     * @param String
     * @param String
     * @throws UnidentifiedResponse
     */
    public void response(String messageId, String reply) 
        throws UnidentifiedResponseEx {

        try {
            if(logger == null) {
                System.out.println("SCHEDULING: logger is null!");
            }
            logger.fine("SCHEDULING: in MS. MessageID = "+messageId+" with Reply(SB)"+reply);
            //logger.fine("SCHEDULING: in MS. Reply (sb id) = "+reply);
            //logger.fine("SCHEDULING: in MS. messageQueue size = "+messageQueue.size());
       
            if(messageQueue.size() < 1) {
                logger.warning("SCHEDULING: in MS. MessageQueue was empty. " +
                    "Try starting with startScheduling function!");
                return;
            }   
            
            Message item = messageQueue.getMessage(messageId);
            logger.fine("SCHEDULING: in MS. Got message with id="+item.getMessageId());
            item.setReply(reply);
            logger.fine("SCHEDULING: in MS. message = "+ messageId + 
                " gotten and reply = "+ reply + " sent.");
            item.getTimer().interrupt();
        }catch(Exception e) {
            AcsJUnidentifiedResponseEx e1 = new AcsJUnidentifiedResponseEx(e);
            throw e1.toUnidentifiedResponseEx();
        }
    }

    /**
      * @return SchedulingInfo
      */
    public SchedulingInfo getArrayInfo() {
        String comment = "Information about the current arrays.";
        String currentTime = clock.getDateTime().toString();
        String timeZone = String.valueOf(telescope.getSite().getTimeZone());
        ArrayInfo[] arrayInfo = control.getAllArraysInfo();
        SchedulingInfo schedInfo =
            new SchedulingInfo(comment, currentTime, timeZone, arrayInfo);
        return schedInfo;
    }

    /**
      * @param short[]
      * @param String
      * @return String
      * @throws InvalidOperation
      */
    public String createArray(String[] antennaIdList, ArrayModeEnum schedulingMode)
        throws InvalidOperationEx {
            
        Subarray a =null;
        String mode="n/a";
        String name="";
        try {             
            if(schedulingMode == ArrayModeEnum.MANUAL) {
                logger.info("SCHEDULING: Creating an array for manual mode");
                name = control.createManualArray(antennaIdList);
                //a = new Subarray(name, antennaIdList);
                //a.setSchedulingMode("manual");
            } else if(schedulingMode == ArrayModeEnum.DYNAMIC){
                logger.info("SCHEDULING: Creating an array for dynamic mode");
                name = control.createArray(antennaIdList, "dynamic");
                //a = new Subarray(name, antennaIdList);
                //a.setSchedulingMode("dynamic");
            } else if(schedulingMode == ArrayModeEnum.QUEUED){
                logger.info("SCHEDULING: Creating an array for queued mode");
                name = control.createArray(antennaIdList, "queued");
                //a = new Subarray(name, antennaIdList);
                //a.setSchedulingMode("queued");
            } else if(schedulingMode == ArrayModeEnum.INTERACTIVE){
                logger.info("SCHEDULING: Creating an array for interactive mode");
                name = control.createArray(antennaIdList, "interactive");
                //a = new Subarray(name, antennaIdList);
                //a.setSchedulingMode("interactive");
            }
            //telescope.addSubarray(a);
        } catch(SchedulingException e) {
            e.printStackTrace();
            AcsJInvalidOperationEx e1 = new AcsJInvalidOperationEx(e);
            throw e1.toInvalidOperationEx();
        }
        return name;
    }

    /**
      * Checks to see if another scheduler is using a given array
      * @param name Array Name
      * @return boolean True if array is already used
      */
    public boolean isArrayInUse(String name) throws InvalidOperationEx{
        boolean tmp = false;
        for(int i=0; i< arraysInUse.size(); i++){
            if(arraysInUse.elementAt(i).equals(name)){
                tmp = true;
                break;
            }
        }
        return tmp;
    }

    public void setArrayInUse(String name) {
        logger.info("SCHEDULING: array "+name+" is now in use");
        arraysInUse.add(name);
    }


    /**
      * @param short
      * @throws InvalidOperation
      */
    public void destroyArray(String name) throws InvalidOperationEx {
        try {
            logger.info("SCHEDULING: Destroying array "+name);
            control.destroyArray(name);
            for(int i=0; i < arraysInUse.size(); i++){
                if(arraysInUse.elementAt(i).equals(name)){
                    arraysInUse.removeElementAt(i);
                    break;
                }
            }
        } catch(SchedulingException e) {
            InvalidOperation e1 = new InvalidOperation("destroyArray", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();

        }
    }

    /**
      * @param String
      * @param short
      * @throws InvalidOperation
      */
    public void executeProject(String projectId, String name)
        throws InvalidOperationEx {
    }

    /**
      * @param String
      * @param short
      * @param String
      */
    public void executeSB(String sbId, String name, String schedulerId, String when) {
        Scheduler scheduler = getScheduler(schedulerId);
        String type = scheduler.getType();
        if(type.equals("interactive")){
            try {
                ((InteractiveScheduler)scheduler).execute(sbQueue.get(sbId));
                //((InteractiveScheduler)scheduler).execute(sbId);
            } catch(Exception e) {
                e.printStackTrace();
            }
        } else if(type.equals("queued")){
        }else if(type.equals("dynamic")){
        }

        //logger.finest ("SCHEDULING: executing SB "+sbId+" on array "
          //      +name+" at time "+when);
    }

    /**
     * Stops the scheduling block's activities.
     * @param String The Scheduling Block's id
     * @throws InvalidOperation
     * @throws NoSuchSB
     */
    public void stopSB(String sbId) throws InvalidOperationEx, NoSuchSBEx {
    
    }

    /**
     * Pauses all scheduling activity on the given array.
     * @param String The Array name
     */
    public void pauseScheduling(String name) {
        logger.info("SCHEDULING: Pause Scheduling not implemented yet.");
    }

    /**
      * @param short
      */
    public void resumeScheduling(String name) {
    }

    /**
      * @param short
      * @throws InvalidOperation
      */
    public void manualMode(String antennaId) throws InvalidOperationEx{
    }

    /**
      * @param short
      * @throws InvalidOperation
      */
    public void activeMode(String antennaId) throws InvalidOperationEx {
    }
///// END TelescopeOperator_to_Scheduling interface implementation
    /////////////////////////////////////////////////////////////////////
    // Internal MasterScheduler Functions.
    /////////////////////////////////////////////////////////////////////

    /*
    private void scheduleRegularSBs(String[] regularSbAntennas) throws InvalidOperationEx {
        try {
            //store scheduling policy int the archive.
            //archive.storeSchedulingPolicy(schedulingPolicy);
        
            //TODO Eventually populate s_policy with info from the schedulingPolicy
            Policy s_policy = createPolicy();
            // regular sb scheduling
            logger.info("SCHEDULING: create array with "+regularSbAntennas.length+" antennas");
            String arrayname = createArray(regularSbAntennas, ArrayModeEnum.DYNAMIC);
            //TODO: Handle this better..
            SBQueue dynamicSBs=manager.getDynamicSBQueue();
            logger.info("SCHEDULING: got "+dynamicSBs.size()+" sbs for dynamic scheduling");

            SchedulerConfiguration config = 
                createSchedulerConfiguration (
                        false, dynamicSBs, true, true, 5, arrayname, s_policy);

            logger.info("SCHEDULING: Master Scheduler creating dynamic scheduler for regular SBs");
            if(!isArrayInUse(arrayname)){
                setArrayInUse(arrayname);
            }
            DynamicScheduler scheduler = new DynamicScheduler(config);
            //get UID for scheduler
            String id = archive.getIdForScheduler();
            scheduler.setId(id);
            //add to Map
            allSchedulers.put(id, scheduler);
            Thread schedulerThread = containerServices.getThreadFactory().newThread(scheduler);
            schedulerThread.start();
            while(!stopCommand) {
                try {
                    schedulerThread.join();
                    break;
                } catch(InterruptedException ex) {
                    if(config.isNothingToSchedule()){
                        config.respondStop();
                        logger.finest("SCHEDULING: interrupted regular sched thread in MS");
                        manager.publishNothingCanBeScheduled(NothingCanBeScheduledEnum.OTHER);
                    }
                }
            }
            if(!config.isOperational()) {
                logger.info("SCHEDULING: Dynamic Scheduler has ended at " + config.getActualEndTime());
            }
            destroyArray(arrayname);
        } catch(Exception e) {
            e.printStackTrace();
            InvalidOperation e1 = new InvalidOperation("scheduleRegularSBs", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }
    

    private void scheduleSpecialSBs(String[] specialSbAntennas)  throws InvalidOperationEx {
        try {
            String specialSBarrayname = createArray(specialSbAntennas, ArrayModeEnum.DYNAMIC);
            //
            Policy specialPolicy = createPolicy();
            SchedulerConfiguration specialConfig = 
                createSchedulerConfiguration( 
                        true, manager.getSpecialSBs(), true, true, 
                            0, specialSBarrayname, specialPolicy);
            logger.info("SCHEDULING: Master Scheduler creating dynamic scheduler for special SBs");
            SpecialSBScheduler specialScheduler = new SpecialSBScheduler(specialConfig);
            //get UID for scheduler
            String id = archive.getIdForScheduler();
            specialScheduler.setId(id);
            //add to Map
            allSchedulers.put(id, specialScheduler);
            Thread specialSchedulerThread = 
                containerServices.getThreadFactory().newThread(specialScheduler);
            specialSchedulerThread.start();
        }catch(Exception e) {
            InvalidOperation e1 = new InvalidOperation("scheduleSpecialSBs", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
       
    }*/

    private Scheduler getScheduler(String id) {
        return allSchedulers.get(id);
    }
    

    /**
      * Returns the uids of the projects that match the given search criteria
      */
    public String[] queryForProject(String projname, String piname, String type) 
        throws InvalidOperationEx {

        String[] results = new String[0];    
        String schema = new String("ObsProject");
        String foo1, foo2, foo3;
        if(projname.equals("") || projname.equals("*") || projname.contains("*")){
            foo1=new String("prj:projectName=*");
        } else {
            foo1 =new String("prj:projectName=\""+projname+"\"");
        }

        if(piname.equals("") || piname.equals("*") || piname.contains("*")){
            foo2=new String("prj:pI=*");
        } else {
            foo2 =new String( "prj:pI=\""+piname+"\"");
        }
        //TODO Figure out how to put this in a query
        //if(!type.equals("All")){
        //    foo3 = "";
        //} else {
        //    foo3 = "prj:DataProcessingParameters[@projectType=\""+type+"\"]";
        //}
        String query = new String("/prj:ObsProject["+foo1+" and "+foo2+"]");
        logger.info("Scheduling Query = "+ query);                
        try {
            results = manager.archiveQuery(query, schema);
        } catch(Exception e) {
            e.printStackTrace();
            results[0] = new String(e.toString());
        }
        if( !piname.equals("*") && piname.contains("*")){
            //check to see if results match other part of piname
            results = manager.getWildCardResults(results, piname, "pI");
        }
        if( !projname.equals("*") && projname.contains("*")){
            //check to see if results match other part of projname
            results = manager.getWildCardResults(results, projname, "projectName");
        }
        return results;
    }
    public String[] getSBProjectUnion(String[] sbIds, String[] projectIds){
        return manager.getSBProjectUnion(sbIds, projectIds); 
    }
    
    public String[] getProjectSBUnion(String[] projectIds, String[] sbIds){
        return manager.getProjectSBUnion(projectIds,sbIds);
    }

    /**
      * Returns the uids of the entitiies that match the given search criteria
      */
    public String[] queryArchive(String query, String schema) 
        throws InvalidOperationEx {

        String[] results = new String[0];    
        try {
            results = manager.archiveQuery(query, schema);
        } catch(Exception e) {
            e.printStackTrace();
            results[0] = new String(e.toString());
        }
        return results;
    }

    ////////////////////////////////////////////////////////////////
    // Dynamic_Scheduler_to_MasterScheduler
    public String getArrayName(String schedulerId){
        Scheduler scheduler = getScheduler(schedulerId);
        return scheduler.getArrayName();
    }

    public SBLite[] getDynamicSBs(String schedulerId){
        return null;
    }
    // Queued_Scheduler_to_MasterScheduler
    public void addSBToQueue(String sbid, String schedulerId) 
        throws InvalidOperationEx, NoSuchSBEx {

            logger.info("MS: adding sb to queued scheduler");
            SB sb = sbQueue.get(sbid);
            Scheduler scheduler = getScheduler(schedulerId);
            ((QueuedSBScheduler)scheduler).addSB(sb);
    }
    
    public void removeQueuedSBs(String[] sbid, int[] i, String schedulerId)
        throws InvalidOperationEx, NoSuchSBEx {
            logger.info("MS: removing sbs from queued scheduler");
            SB sb;
            Scheduler scheduler = getScheduler(schedulerId);
            for(int x=0; x < sbid.length; x++){
                sb= sbQueue.get(sbid[x]);
                ((QueuedSBScheduler)scheduler).removeSbAt(sb, i[x]);
            }
    }
    
    public void stopQueuedSB(String sbid, String schedulerId) 
        throws InvalidOperationEx, NoSuchSBEx {

        Scheduler scheduler = getScheduler(schedulerId);
        checkSchedulerType(scheduler.getType(), "queued");
        try{
            ((QueuedSBScheduler)scheduler).stop(sbid);
        } catch(SchedulingException e){
            logger.severe("SCHEDULING: Error stopping queued SB");
            e.printStackTrace();
        }
    }

    // Interactive_Scheduler_to_MasterScheduler
    public void executeInteractiveSB(String sbId, String schedulerId) 
        throws InvalidOperationEx, NoSuchSBEx {

        Scheduler scheduler = getScheduler(schedulerId);
        String type = scheduler.getType();
        if(!type.equals("interactive")){
            InvalidOperation e1 = new InvalidOperation("executeInteractiveSB",
                   "Wrong scheduler type: "+type);
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
        try {
            logger.info("Looking for SB ("+sbId+") in queue... "+sbQueue.get(sbId)+" == is it there?");
            ((InteractiveScheduler)scheduler).execute(sbQueue.get(sbId));
        } catch(Exception e) {
            e.printStackTrace();
            InvalidOperation e1 = new InvalidOperation("executeInteractiveSB",
                   e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }
    
    public void stopInteractiveSB(String schedulerId) 
        throws InvalidOperationEx, NoSuchSBEx {
            
        Scheduler scheduler = getScheduler(schedulerId);
        checkSchedulerType(scheduler.getType(), "interactive");
        try {
            ((InteractiveScheduler)scheduler).stop();
        } catch(Exception e) {
            InvalidOperation e1 = new InvalidOperation("stopInteractiveSB",
                   e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }

    public void startInteractiveSession(String pi, String projectId, 
            String schedulerId) throws InvalidOperationEx{

        Scheduler scheduler = getScheduler(schedulerId);
        checkSchedulerType(scheduler.getType(), "interactive");
        try {
            SB[] sbs = manager.getSBsForProject(projectId);
            /*temporary forloop i think*/
            for(int i=0; i < sbs.length; i++){
                sbs[i].setType(SB.INTERACTIVE);
            }

            ((InteractiveScheduler)scheduler).getConfiguration().getQueue().clear();
            ((InteractiveScheduler)scheduler).getConfiguration().getQueue().add(sbs);
            ((InteractiveScheduler)scheduler).login(pi, projectId, sbs[0]);
        } catch(Exception e) {
            e.printStackTrace();
            InvalidOperation e1 = new InvalidOperation("startInteractiveSession",
                   e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }

    public void endInteractiveSession(String schedulerId) 
        throws InvalidOperationEx{

        Scheduler scheduler = getScheduler(schedulerId);
        checkSchedulerType(scheduler.getType(), "interactive");
        try {
            ((InteractiveScheduler)scheduler).logout();
        }catch(Exception e){
            e.printStackTrace();
            InvalidOperation e1 = new InvalidOperation("endInteractiveSession",
                   e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }

    ////////////////////////////////////////////////////////////////

    private void checkSchedulerType(String type, String shouldbe) throws InvalidOperationEx {
        //if(!type.equals("interactive"))
        if(!type.equals(shouldbe)){
            InvalidOperation e1 = new InvalidOperation("CheckSchedulerType",
                   "Wrong scheduler type: "+type);
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }
}        
