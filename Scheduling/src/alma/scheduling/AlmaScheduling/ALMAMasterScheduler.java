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

import alma.xmlentity.XmlEntityStruct;
import alma.acs.nc.*;
import alma.acs.container.ContainerServices;
import alma.acs.component.ComponentLifecycle;
import alma.acs.component.ComponentLifecycleException;
import alma.ACS.ComponentStates;

import alma.scheduling.*;
/*
import alma.scheduling.InvalidOperation;
import alma.scheduling.NoSuchSB;
import alma.scheduling.SchedulingInfo;
import alma.scheduling.ArrayInfoSeq;
import alma.scheduling.NothingCanBeScheduledEnum;
import alma.scheduling.UnidentifiedResponse;
import alma.scheduling.MasterSchedulerIFOperations;
*/
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
 * @version $Id: ALMAMasterScheduler.java,v 1.51 2006/04/10 19:18:56 sslucero Exp $
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
    private Vector all_schedulers;
    
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
        this.all_schedulers = new Vector();
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
        this.control = new ALMAControl(containerServices, manager);
        this.telescope = new ALMATelescope();
        
        logger.config("SCHEDULING: MasterScheduler initialized");
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
        
    }

    /**
     * From ComponentLifecycle interface
     */
    public void cleanUp() {
        logger.info("SCHEDULING: cleaning up scheduling component for shutdown.");
        for(int i=0; i < is_controllers.size(); i++){
            logger.info("SCHEDULING: trying to release interactive gui # "+i+" in MS");
    	    try {
	            ((ArchiveQueryWindowController)is_controllers.elementAt(i)).close();
	        } catch(Exception e){
        		logger.severe("SCHEDULING: error = "+e.toString());
		        e.printStackTrace();
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
        policy.setDescription("R3.0Policy testing!");
        policy.setName("R3.0Policy");
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
                    Thread.currentThread(), dynamic, manager.getSpecialSBs(),
                    arrayName, clock, control, operator, telescope, 
                    manager, policy, logger);
        } else {
     
            return new SchedulerConfiguration(
                    Thread.currentThread(), dynamic, synchronous, (SBQueue)sbs, 
                    ((SBQueue)sbs).size(), sleepTime, arrayName, clock, control,
                    operator, telescope, manager, policy, logger);
        }
    }
    
    /**
     * Starts scheduling using a specific list of scheduling block Ids.
     * @param sbList
     * @throws InvalidOperationEx
     */
    public void startQueueScheduling(String[] sbList)//, String arrayName)
    	throws InvalidOperationEx {

        try {    
            manager.checkForProjectUpdates();
            //create a queue of sbs with these ids, 
            SBQueue sbs = manager.mapQueuedSBsToProjects(sbList);
            if(sbs == null || sbs.size() ==0){
                InvalidOperation e1 = new InvalidOperation("startQueueScheduling",
                        "Cannot schedule without a SB queue");
                AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
                throw e2.toInvalidOperationEx();
            }
            //create policy
            Policy s_policy = createPolicy();
            //create an array
            String[] antennas = null;
            try {
                antennas = control.getIdleAntennas();
            }catch(Exception e) {
                e.printStackTrace();
            }
            String arrayname = createArray(antennas, ArrayModeEnum.QUEUED);
            //then create a config 
            SchedulerConfiguration config = 
                createSchedulerConfiguration(
                        false, sbs, true, true, 5, arrayname, s_policy);
                
            //a scheduler and go from there!
            //DynamicScheduler scheduler = new DynamicScheduler(config);
            QueuedSBScheduler scheduler = new QueuedSBScheduler(config);
            all_schedulers.add(scheduler);
            Thread scheduler_thread = containerServices.getThreadFactory().newThread(scheduler);
            scheduler_thread.start();
            while(true) {
                try {
                    scheduler_thread.join();
                    break;
                } catch(InterruptedException e) {
//                    if(config.isNothingToSchedule()){
                        //config.respondStop();
                        logger.info("SCHEDULING: interrupted sched thread in MS");
                        manager.publishNothingCanBeScheduled(NothingCanBeScheduledEnum.OTHER);
  //                  }
                }
            }
            if(!config.isOperational()) {
                logger.info("SCHEDULING: Scheduler has ended at " + config.getActualEndTime());
            }
            //destroyArray(arrayname);
        } catch (Exception e){
            InvalidOperation e1 = new InvalidOperation("startQueueScheduling", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
            
	}

    /**
     * Starts scheduling using a specific Scheduling Policy
     * @param XmlEntityStruct
     * @throws InvalidOperationEx
     */
    public void startScheduling(XmlEntityStruct schedulingPolicy) 
        throws InvalidOperationEx {

        try {
            manager.checkForProjectUpdates();
        
            String[] allAntennas = null;
            try {
                allAntennas = control.getIdleAntennas();
            }catch(Exception e){
                e.printStackTrace();
            }
            //if there are fixed and regular sbs split antennas.
            //TODO when special sbs are changed to have antenna names included, the
            // array created for special sbs must have  those antennas included.
            String[] specialSbAntennas;
            String[] regularSbAntennas;
            if((manager.getSpecialSBs().size() > 0) && (sbQueue.size() > 0)) {
                //split array and start a scheduler for special sbs
                specialSbAntennas = new String[allAntennas.length/2];
                regularSbAntennas = new String[allAntennas.length/2];
                int x=0;
                for(int i=0; i < (allAntennas.length/2); i++){
                    specialSbAntennas[i] = allAntennas[x++];
                    regularSbAntennas[i] = allAntennas[x++];
                }
                scheduleSpecialSBs(specialSbAntennas);
                scheduleRegularSBs(regularSbAntennas);
            } else if(manager.getSpecialSBs().size() > 0) {
                specialSbAntennas = allAntennas;
                scheduleSpecialSBs(specialSbAntennas);
            } else if(sbQueue.size() > 0) {
                regularSbAntennas = allAntennas;
                scheduleRegularSBs(regularSbAntennas);
            }
        } catch(Exception e) {
            InvalidOperation e1 = new InvalidOperation("startScheduling", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }


    private void scheduleRegularSBs(String[] regularSbAntennas) throws InvalidOperationEx {
        try {
            //store scheduling policy int the archive.
            //archive.storeSchedulingPolicy(schedulingPolicy);
        
            //TODO Eventually populate s_policy with info from the schedulingPolicy
            Policy s_policy = createPolicy();
            // regular sb scheduling
            String arrayname = createArray(regularSbAntennas, ArrayModeEnum.DYNAMIC);
        
            SchedulerConfiguration config = 
                createSchedulerConfiguration (
                        false, sbQueue, true, true, 5, arrayname, s_policy);

            DynamicScheduler scheduler = new DynamicScheduler(config);
            all_schedulers.add(scheduler);
            Thread schedulerThread = containerServices.getThreadFactory().newThread(scheduler);
            schedulerThread.start();
            while(!stopCommand) {
                try {
                    schedulerThread.join();
                    break;
                } catch(InterruptedException ex) {
                    if(config.isNothingToSchedule()){
                        config.respondStop();
                        logger.info("SCHEDULING: interrupted regular sched thread in MS");
                        manager.publishNothingCanBeScheduled(NothingCanBeScheduledEnum.OTHER);
                    }
                }
            }
            if(!config.isOperational()) {
                logger.info("SCHEDULING: Scheduler has ended at " + config.getActualEndTime());
            }
            destroyArray(arrayname);
        } catch(Exception e) {
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
            SpecialSBScheduler specialScheduler = new SpecialSBScheduler(specialConfig);
            Thread specialSchedulerThread = 
                containerServices.getThreadFactory().newThread(specialScheduler);
            specialSchedulerThread.start();
            all_schedulers.add(specialScheduler);
        }catch(Exception e) {
            InvalidOperation e1 = new InvalidOperation("scheduleSpecialSBs", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
       
    }

    /**
      * @throws InvalidOperationEx
      */
    //public void startInteractiveScheduling(String arrayName) throws InvalidOperationEx {
    public void startInteractiveScheduling() throws InvalidOperationEx {
        logger.info("SCHEDULING: startInteractiveScheduling called");
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

            logger.info("SCHEDULING: Array name == "+arrayname);

            InteractiveScheduler scheduler = new InteractiveScheduler(config);
            ArchiveQueryWindowController interactiveGUI = 
                new ArchiveQueryWindowController(scheduler, containerServices);
            Thread scheduler_thread = containerServices.getThreadFactory().newThread(interactiveGUI);
            scheduler_thread.start();
            is_controllers.add(interactiveGUI);
            all_schedulers.add(scheduler);
        } catch (Exception e) {
            InvalidOperation e1 = new InvalidOperation ("startInteractiveScheduling", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
        
    }
    
    /**
     * Stops all scheduling
     * @throws InvalidOperationEx
     */
    public void stopScheduling() throws InvalidOperationEx {
        logger.info("SCHEDULING: Stop all scheduling called from OMC");
        try {
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
        return archive.getSBLites();
    }

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
        
        String name="";
        try {             
            if(schedulingMode == ArrayModeEnum.MANUAL) {
                logger.info("SCHEDULING: Creating an array for manual mode");
                name = control.createManualArray(antennaIdList);
            } else if(schedulingMode == ArrayModeEnum.DYNAMIC){
                logger.info("SCHEDULING: Creating an array for dynamic mode");
                name = control.createArray(antennaIdList);
            } else if(schedulingMode == ArrayModeEnum.QUEUED){
                logger.info("SCHEDULING: Creating an array for queued mode");
                name = control.createArray(antennaIdList);
            } else if(schedulingMode == ArrayModeEnum.INTERACTIVE){
                logger.info("SCHEDULING: Creating an array for interactive mode");
                name = control.createArray(antennaIdList);
            }
        } catch(SchedulingException e) {
            AcsJInvalidOperationEx e1 = new AcsJInvalidOperationEx(e);
            throw e1.toInvalidOperationEx();
        }
        return name;
    }


    /**
      * @param short
      * @throws InvalidOperation
      */
    public void destroyArray(String name) throws InvalidOperationEx {
        try {
            logger.info("SCHEDULING: Destroying array "+name);
            control.destroyArray(name);
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
    public void executeSB(String sbId, String name, String when) {
        logger.info ("SCHEDULING: executing SB "+sbId+" on array "
                +name+" at time "+when);
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
     
    /////////////////////////////////////////////////////////////////////
    // Internal MasterScheduler Functions.
    /////////////////////////////////////////////////////////////////////
    
}    
