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
import alma.scheduling.GUI.InteractiveSchedGUI.ArchiveQueryWindowController;
import alma.scheduling.ObsProjectManager.ProjectManagerTaskControl;


/**
 * @author Sohaila Lucero
 * @version $Id: ALMAMasterScheduler.java,v 1.78 2006/11/17 15:29:04 sslucero Exp $
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
    private Vector is_controllers;
    private LinkedHashMap<String, Scheduler> allSchedulers;
    private Vector interactiveComps;
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
    
        //Start the MasterScheduler Thread! 
        this.msThread.start();
        this.is_controllers = new Vector(); 
        this.interactiveComps = new Vector();
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
    }

    /**
     * From ComponentLifecycle interface
     * @throws ComponentLifecycleException
     */
    public void execute() throws ComponentLifecycleException {
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
        
        logger.info("Execute complete in scheduling master scheduler and an alarm should have been sent.");
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
        for(int i=0; i < is_controllers.size(); i++){
            logger.info("SCHEDULING: trying to release interactive gui # "+i+" in MS");
    	    try {
	            ((ArchiveQueryWindowController)is_controllers.elementAt(i)).close();
	        } catch(Exception e){
        		logger.severe("SCHEDULING: error = "+e.toString());
		        e.printStackTrace(System.out);
	        }
        }
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
            logger.info("Sending ALARM");
            ACSAlarmSystemInterface alarmSource = ACSAlarmSystemInterfaceFactory.createSource(this.name());
            ACSFaultState state = ACSAlarmSystemInterfaceFactory.createFaultState(ff, fm, fc);
            Properties prop = new Properties();
            state.setUserProperties(prop);
            state.setDescriptor(fs);
            state.setUserTimestamp(new Timestamp(clock.getDateTime().getMillisec()));
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

            logger.info("SCHEDULING: Starting dynamic scheduling");
        try {
            manager.checkForProjectUpdates();
            logger.info("SCHEDULING: got project updates");
        
            String[] allAntennas = null;
            try {
                //gets the available antennas from control
                allAntennas = control.getIdleAntennas();
                logger.info("SCHEDULING: got antennas");
            }catch(Exception e){
                e.printStackTrace(System.out);
            }
            //if there are fixed and regular sbs split antennas.
            //TODO when special sbs are changed to have antenna names included, the
            // array created for special sbs must have  those antennas included.
            String[] specialSbAntennas;
            String[] regularSbAntennas;
            /*
            if((manager.getSpecialSBs().size() > 0) && (sbQueue.size() > 0)) {
                //split array and start a scheduler for special sbs
                //specialSbAntennas = new String[allAntennas.length/2];
                regularSbAntennas = new String[allAntennas.length/2];
                int x=0;
                for(int i=0; i < (allAntennas.length/2); i++){
                    specialSbAntennas[i] = allAntennas[x++];
                    regularSbAntennas[i] = allAntennas[x++];
                }
                //scheduleSpecialSBs(specialSbAntennas);
                scheduleRegularSBs(regularSbAntennas);
            } else if(manager.getSpecialSBs().size() > 0) {
                specialSbAntennas = allAntennas;
                scheduleSpecialSBs(specialSbAntennas);
            } else if(sbQueue.size() > 0) {
            */
                regularSbAntennas = allAntennas;
                scheduleRegularSBs(regularSbAntennas);
           // }
        } catch(Exception e) {
            InvalidOperation e1 = new InvalidOperation("startScheduling", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }
    public void startScheduling1(XmlEntityStruct schedulingPolicy, String arrayname) 
        throws InvalidOperationEx {
    }

    private void dynamicScheduling(XmlEntityStruct schedulingPolicy, 
            String arrayname) throws InvalidOperationEx{
        //TODO eventually use this inside the startScheduling methods
    }

    /**
     * Starts scheduling using a specific list of scheduling block Ids.
     * @param sbList
     * @throws InvalidOperationEx
     * @deprecated Use startQueuedScheduling(String[] sbList, String arrayname)
     */
    public void startQueueScheduling(String[] sbList)
    	throws InvalidOperationEx {

            logger.warning("SCHEDULING: This method will disappear soon!");
            logger.warning("SCHEDULING: Use startQueuedScheduling(sbList, arrayName)");
        try {    
            //create an array
            String[] antennas = null;
            try {
                antennas = control.getIdleAntennas();
            }catch(Exception e) {
                e.printStackTrace(System.out);
            }
            String arrayname = createArray(antennas, ArrayModeEnum.QUEUED);
            //then create a config 
            startQueuedScheduling(sbList, arrayname);
        } catch (Exception e){
            e.printStackTrace(System.out);
            InvalidOperation e1 = new InvalidOperation("startQueueScheduling", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
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
            //DynamicScheduler scheduler = new DynamicScheduler(config);
            logger.info("SCHEDULING: Master Scheduler creating queued scheduler");
            if(!isArrayInUse(arrayname)){
                setArrayInUse(arrayname);
            }
            QueuedSBScheduler scheduler = new QueuedSBScheduler(config);
            //get UID for scheduler
            String id = archive.getIdForScheduler();
            scheduler.setId(id);
            //add to Map
            allSchedulers.put(id, scheduler);
            
            Thread scheduler_thread = containerServices.getThreadFactory().newThread(scheduler);
            scheduler_thread.start();
            while(true) {
                try {
                    scheduler_thread.join();
                    break;
                } catch(InterruptedException e) {
//                    if(config.isNothingToSchedule()){
                        //config.respondStop();
                        logger.finest("SCHEDULING: interrupted sched thread in MS");
                        manager.publishNothingCanBeScheduled(NothingCanBeScheduledEnum.OTHER);
  //                  }
                }
            }
            if(!config.isOperational()) {
                logger.info("SCHEDULING: Queued scheduler has ended at " + config.getActualEndTime());
            }
            //destroyArray(arrayname);
        } catch (Exception e){
            e.printStackTrace(System.out);
            InvalidOperation e1 = new InvalidOperation("startQueueScheduling", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
            
	}

    public void createQueuedScheduler() 
        throws InvalidOperationEx{
    }
    /**
      * @throws InvalidOperationEx
      */
    //public void startInteractiveScheduling(String arrayName) throws InvalidOperationEx {
    public void startInteractiveScheduling() throws InvalidOperationEx {
        logger.info("SCHEDULING: startInteractiveScheduling called");
        logger.warning("SCHEDULING: this method will go away soon!");
        logger.warning("SCHEDULING: please use startInteractiveScheduling1(arrayname)");
        try {
            manager.checkForProjectUpdates();
            Policy s_policy = createPolicy();

            String[] antennas = null; 
            try {
                antennas = control.getIdleAntennas();
            }catch(Exception ex) {
                throw ex;
            }
            String arrayname = createArray(antennas, ArrayModeEnum.INTERACTIVE);
        
            SchedulerConfiguration config = 
                createSchedulerConfiguration(
                        false, new SBQueue(), false, true, 0, arrayname, s_policy);

            logger.info("SCHEDULING: Starting interactive scheduling on array "+arrayname);

            InteractiveScheduler scheduler = new InteractiveScheduler(config);
            ArchiveQueryWindowController interactiveGUI = 
                new ArchiveQueryWindowController(scheduler, containerServices);
            Thread scheduler_thread = containerServices.getThreadFactory().newThread(interactiveGUI);
            scheduler_thread.start();
            is_controllers.add(interactiveGUI);
        } catch (Exception e) {
            InvalidOperation e1 = new InvalidOperation ("startInteractiveScheduling", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
        
    }
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
            InvalidOperation e1 = new InvalidOperation(
                    "startInteractiveScheduling1", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }

    public void stopInteractiveScheduler(String n){
        for (int i=0; i < interactiveComps.size(); i++){
            Interactive_PI_to_Scheduling comp = (Interactive_PI_to_Scheduling)interactiveComps.elementAt(i);
            if(comp.name().equals(n)) {
                logger.info("SCHEDULING: Stopping interactive scheduler "+comp.name());
                containerServices.releaseComponent(comp.name());
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
           /* 
            for(int i=0; i< interactiveComps.size(); i++){
                Interactive_PI_to_Scheduling comp = (Interactive_PI_to_Scheduling)interactiveComps.elementAt(i);
                logger.info("SCHEDULING: Stopping component "+comp.name());
                containerServices.releaseComponent(comp.name());
            }
            */
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
            logger.fine("SCHEDULING: in MS. MessageID = "+messageId);
            logger.fine("SCHEDULING: in MS. Reply (sb id) = "+reply);
            logger.fine("SCHEDULING: in MS. messageQueue size = "+messageQueue.size());
       
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
                ((InteractiveScheduler)scheduler).execute(sbId);
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
       
    }

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
        //if(type.equals("*")){
            //dont need to search for specific sb types.
            //return what we have
          //  return results;
        //}
        return results;
        //Type is something different so now we get all the sbs with that type.
        /*
        schema = new String("SchedBlock");
        query = new String("/sbl:SchedBlock[sbl:modeName=\""+type+"\"]");
        String[] sbResults = new String[0];
        try {
            sbResults = manager.archiveQuery(query, schema);
        } catch(Exception e) {
            e.printStackTrace();
            sbResults[0] = new String(e.toString());
        }
        //now get all the projects which these sbs belong to.
        String[] totalResults = manager.getProjectSBUnion(results, sbResults); 
        return totalResults;
        */
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

    // Dynamic_Scheduler_to_MasterScheduler
    public String getArrayName(String schedulerId){
        Scheduler scheduler = getScheduler(schedulerId);
        return scheduler.getArrayName();
    }

    public SBLite[] getDynamicSBs(String schedulerId){
        return null;
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
            ((InteractiveScheduler)scheduler).execute(sbId);
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
        checkSchedulerType(scheduler.getType());
        try {
            ((InteractiveScheduler)scheduler).stop();
        } catch(Exception e) {
            InvalidOperation e1 = new InvalidOperation("stopInteractiveSB",
                   e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }

    public void addInteractiveSB(String projectId, String schedulerId)
        throws InvalidOperationEx, InvalidObjectEx, SBExistsEx {
         
        //Scheduler scheduler = getScheduler(schedulerId);
        //checkSchedulerType(scheduler.getType());
            /*
        try {
            //((InteractiveScheduler)scheduler.add();
            OpenOT ot = new OpenOT(projectId,containerServices);
            Thread t = containerServices.getThreadFactory().newThread(ot);
            t.start();
        } catch(Exception e) {
            InvalidOperation e1 = new InvalidOperation("stopInteractiveSB",
                   e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }*/
    }
    
    public void updateInteractiveSB(String projectId,String schedulerId)
        throws InvalidOperationEx, InvalidObjectEx, NoSuchSBEx {
            
        //Scheduler scheduler = getScheduler(schedulerId);
        //checkSchedulerType(scheduler.getType());
            /*
        try {
            OpenOT ot = new OpenOT(projectId,containerServices);
            Thread t = containerServices.getThreadFactory().newThread(ot);
            t.start();
            //((InteractiveScheduler)scheduler).update();
        } catch(Exception e) {
            InvalidOperation e1 = new InvalidOperation("startInteractiveSession",
                   e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }*/
    }

    public void deleteInteractiveSB(String sbId, String schedulerId)
        throws InvalidOperationEx, NoSuchSBEx{

        Scheduler scheduler = getScheduler(schedulerId);
        checkSchedulerType(scheduler.getType());
        try {
            ((InteractiveScheduler)scheduler).delete(sbId);
        } catch(Exception e) {
            e.printStackTrace();
            InvalidOperation e1 = new InvalidOperation("deleteInteractiveSB",
                   e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }
    
    public void startInteractiveSession(String pi, String projectId, 
            String schedulerId) throws InvalidOperationEx{

        Scheduler scheduler = getScheduler(schedulerId);
        checkSchedulerType(scheduler.getType());
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
        checkSchedulerType(scheduler.getType());
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

    private void checkSchedulerType(String type) throws InvalidOperationEx {
        if(!type.equals("interactive")){
            InvalidOperation e1 = new InvalidOperation("executeInteractiveSB",
                   "Wrong scheduler type: "+type);
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }
    
}    
