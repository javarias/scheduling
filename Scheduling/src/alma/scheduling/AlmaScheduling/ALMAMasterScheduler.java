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

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import alma.ACS.ComponentStates;
import alma.SchedulingExceptions.CannotRunCompleteSBEx;
import alma.SchedulingExceptions.InvalidOperationEx;
import alma.SchedulingExceptions.NoSuchSBEx;
import alma.SchedulingExceptions.UnidentifiedResponseEx;
import alma.SchedulingExceptions.wrappers.AcsJCannotRunCompleteSBEx;
import alma.SchedulingExceptions.wrappers.AcsJInvalidOperationEx;
import alma.SchedulingExceptions.wrappers.AcsJUnidentifiedResponseEx;
import alma.acs.component.ComponentLifecycle;
import alma.acs.component.ComponentLifecycleException;
import alma.acs.component.ComponentQueryDescriptor;
import alma.acs.container.ContainerServices;
import alma.acs.logging.AcsLogger;
import alma.acs.logging.domainspecific.ArrayContextLogger;
import alma.acs.nc.CorbaNotificationChannel;
import alma.acs.nc.CorbaReceiver;
import alma.acs.nc.Receiver;
import alma.alarmsystem.source.ACSAlarmSystemInterface;
import alma.alarmsystem.source.ACSAlarmSystemInterfaceFactory;
import alma.alarmsystem.source.ACSFaultState;
import alma.asdmIDLTypes.IDLEntityRef;
import alma.log_audience.OPERATOR;
import alma.scheduling.ArrayInfo;
import alma.scheduling.ArrayModeEnum;
import alma.scheduling.Dynamic_Operator_to_Scheduling;
import alma.scheduling.Interactive_PI_to_Scheduling;
import alma.scheduling.InvalidOperation;
import alma.scheduling.MasterSchedulerIFOperations;
import alma.scheduling.NoSuchSB;
import alma.scheduling.NothingCanBeScheduledEnum;
import alma.scheduling.ProjectLite;
import alma.scheduling.Queued_Operator_to_Scheduling;
import alma.scheduling.SBLite;
import alma.scheduling.SchedulerInfo;
import alma.scheduling.SchedulingInfo;
import alma.scheduling.UnidentifiedResponse;
import alma.scheduling.Define.DateTime;
import alma.scheduling.Define.Policy;
import alma.scheduling.Define.PolicyFactor;
import alma.scheduling.Define.SB;
import alma.scheduling.Define.SBQueue;
import alma.scheduling.Define.SchedulingException;
import alma.scheduling.Define.Subarray;
import alma.scheduling.MasterScheduler.MasterScheduler;
import alma.scheduling.MasterScheduler.Message;
import alma.scheduling.MasterScheduler.MessageQueue;
import alma.scheduling.ObsProjectManager.ProjectManagerTaskControl;
import alma.scheduling.Scheduler.DynamicScheduler;
import alma.scheduling.Scheduler.InteractiveScheduler;
import alma.scheduling.Scheduler.QueuedSBScheduler;
import alma.scheduling.Scheduler.Scheduler;
import alma.scheduling.Scheduler.SchedulerConfiguration;
import alma.scheduling.Scheduler.SchedulerConfiguration.RunMode;
import alma.xmlentity.XmlEntityStruct;


/**
 * @author Sohaila Lucero
 * @version $Id: ALMAMasterScheduler.java,v 1.119 2009/11/09 22:58:45 rhiriart Exp $
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
    //Keeps track of the scheduler modes for each array
    private LinkedHashMap<String, ArrayModeEnum> schedModeForArray;
    
    //private ArrayContextLogger arraylogger;
    protected AcsLogger logger;
    protected ArrayContextLogger arrayLogger;
    
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
    
        try {
            this.containerServices = cs;
            this.instanceName = containerServices.getName();
            this.logger = containerServices.getLogger();
            this.arrayLogger = new ArrayContextLogger(logger);

            //Start the MasterScheduler Thread! 
            this.msThread.start();
            this.interactiveComps = new Vector();
            this.queuedComps = new Vector();
            this.dynamicComps = new Vector();
            allSchedulers = new LinkedHashMap<String, Scheduler>();
            schedModeForArray = new LinkedHashMap<String, ArrayModeEnum>();
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
            //arraylogger = new ArrayContextLogger(containerServices.getLogger());
            logger.finest("SCHEDULING: MasterScheduler initialized");
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
            //control_nc = AbstractNotificationChannel.getReceiver(
            //    AbstractNotificationChannel.CORBA, 
            //    alma.Control.CHANNELNAME_CONTROLSYSTEM.value,
            //        containerServices);
            control_nc = CorbaNotificationChannel.getCorbaReceiver(
                    alma.Control.CHANNELNAME_CONTROLSYSTEM.value,
                        containerServices);
            control_nc.attach("alma.Control.ExecBlockStartedEvent", eventreceiver);
            control_nc.attach("alma.Control.ExecBlockEndedEvent", eventreceiver);
            control_nc.attach("alma.offline.ASDMArchivedEvent", eventreceiver);
            control_nc.begin();
            // Connect to the TelCal NC
            //telcal_nc = AbstractNotificationChannel.getReceiver(
            //    AbstractNotificationChannel.CORBA, 
            //    alma.TelCalPublisher.CHANNELNAME_TELCALPUBLISHER.value,
            //    containerServices);
            telcal_nc = CorbaNotificationChannel.getCorbaReceiver(
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
            //pipeline_nc = AbstractNotificationChannel.getReceiver(
            //    AbstractNotificationChannel.CORBA, 
            //    alma.pipelinescience.CHANNELNAME_SCIPIPEMANAGER.value,
            //        containerServices);
            pipeline_nc = CorbaNotificationChannel.getCorbaReceiver(
                    alma.pipelinescience.CHANNELNAME_SCIPIPEMANAGER.value,
                        containerServices);
            pipeline_nc.attach("alma.pipelinescience.ScienceProcessingDoneEvent",eventreceiver);
            pipeline_nc.begin();
        
        } catch(Exception e){
            e.printStackTrace();
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
        logger.finest("SCHEDULING: cleaning up scheduling component for shutdown.");
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
        policy.setDescription("R5.0Policy testing!");
        policy.setName("R5.0Policy");
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
    	
    	SchedulerConfiguration result;
        if(special){
            result = new SchedulerConfiguration(
                    Thread.currentThread(), dynamic,// manager.getSpecialSBs(),
                    manager.getSpecialSBs(),arrayName, clock, control, operator, 
                    telescope, manager, policy, logger);
                    //ALMASchedulingUtility.getMasterSchedulerThread(), dynamic, 
        } else {
            logger.fine("SCHEDULING: creating scheduler configuration with "+((SBQueue)sbs).size()+" sbs");
     
            result = new SchedulerConfiguration(
                    Thread.currentThread(), dynamic, //synchronous, (SBQueue)sbs, 
                    synchronous, (SBQueue)sbs, ((SBQueue)sbs).size(), sleepTime, 
                    arrayName, clock, control, operator, telescope, manager, 
                    policy, logger);
                    //ALMASchedulingUtility.getMasterSchedulerThread(), dynamic, 
//            result.setRunMode(RunMode.FullAuto); // For testing porpoises
        }
        
        return result;
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
            
    	arrayLogger.log(Level.INFO, "SCHEDULING: Starting dynamic scheduling",
                OPERATOR.value, arrayname);
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
            scheduler.setType("dynamic");
            //add to Map
            allSchedulers.put(id, scheduler);
            manager.rememberSchedulerForArray(arrayname, scheduler);
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
                releaseDSComp(comp.name());
            }
//            destroyArray(arrayname);
        } catch(Exception e) {
        	/*
        	sendAlarm("Scheduling","SchedSchedulerConnAlarm",3,ACSFaultState.ACTIVE);
        	try {
            	Thread.sleep(1000);
            } catch (InterruptedException e1) {
            	e1.printStackTrace(System.out);
            }*/
        	
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
                    dynamicComps.remove(i);
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
        	//sendAlarm("Scheduling","SchedSchedulerConnAlarm",3,ACSFaultState.ACTIVE);
            InvalidOperation e1 = new InvalidOperation(
                    "createDyanmicSchedulingComponent", e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
   }

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
        	arrayLogger.log(Level.INFO, "SCHEDULING: Starting queued scheduling", OPERATOR.value, arrayname);
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
            
//            config.setRunMode(RunMode.FullAuto);
                
            //a scheduler and go from there!
            try {
                logger.fine("SCHEDULING: Master Scheduler creating queued scheduler");
            } catch(Exception e ){
                e.printStackTrace();
            }
            if(!isArrayInUse(arrayname)){
                setArrayInUse(arrayname);
            }
            QueuedSBScheduler scheduler = new QueuedSBScheduler(config);
            //get QS Comp and get id to map to scheduler
            Queued_Operator_to_Scheduling qsComp =
                alma.scheduling.Queued_Operator_to_SchedulingHelper.narrow(
                        containerServices.getComponent("QS_"+arrayname));
            scheduler.setId(qsComp.getSchedulerId());
            scheduler.setType("queued");
            //add to Map
            allSchedulers.put(qsComp.getSchedulerId(), scheduler);
            manager.rememberSchedulerForArray(arrayname, scheduler);
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
                logger.fine("SCHEDULING: Queued scheduler has ended at " + config.getActualEndTime());
            }
            //destroyArray(arrayname);
        } catch (Exception e){
        	/* not sure did we really need this alarm
        	sendAlarm("Scheduling","SchedSchedulerConnAlarm",3,ACSFaultState.ACTIVE);
        	try {
            	Thread.sleep(1000);
            } catch (InterruptedException e1) {
            	e1.printStackTrace(System.out);
            }
            */
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
        	//sendAlarm("Scheduling","SchedSchedulerConnAlarm",3,ACSFaultState.ACTIVE);
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

          arrayLogger.log(Level.INFO, 
                    "SCHEDULING: Starting interactive scheduling on array "+
                    arrayname, OPERATOR.value, arrayname);
            if(!isArrayInUse(arrayname)){
                setArrayInUse(arrayname);
            }
            InteractiveScheduler scheduler = new InteractiveScheduler(config);
            //Thread scheduler_thread = containerServices.getThreadFactory().newThread(scheduler);
            //scheduler_thread.start();

            String id = archive.getIdForScheduler();
            scheduler.setId(id);
            scheduler.setType("interactive");
            schedComp.setSchedulerId(id);
            String name = schedComp.name();
            //add to Map
            allSchedulers.put(id, scheduler);
            manager.rememberSchedulerForArray(arrayname, scheduler);
            
            /////
        //    Policy s_policy = createPolicy();
          //  SchedulerConfiguration config = 
            //    createSchedulerConfiguration(
              //          false, new SBQueue(), false, true, 0, arrayname, s_policy);
           // ((InteractiveScheduler)sched).setConfiguration(config);
            /////
            return name;
        } catch(Exception e){
        	/* not sure did we really need this alarm
        	sendAlarm("Scheduling","SchedSchedulerConnAlarm",3,ACSFaultState.ACTIVE);
        	try {
            	Thread.sleep(1000);
            } catch (InterruptedException e1) {
            	e1.printStackTrace(System.out);
            }
            */
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
                logger.fine("SCHEDULING: Stopping interactive scheduler component "
                        +comp.name());
                interactiveComps.remove(i);
                containerServices.releaseComponent(comp.name());
                id= comp.getSchedulerId();
                InteractiveScheduler scheduler = (InteractiveScheduler)allSchedulers.get(id);
                manager.forgetSchedulerForArray(scheduler.getArrayName());
                allSchedulers.remove(id);
                scheduler = null;
            }
        }
    }


    public void stopQueuedScheduler(String n){
        String id=null;
        for (int i=0; i < queuedComps.size(); i++){
            Queued_Operator_to_Scheduling comp = (Queued_Operator_to_Scheduling)queuedComps.elementAt(i);
            if(comp.name().equals(n)) {
                logger.fine("SCHEDULING: Stopping queued scheduler "+comp.name());
                id= comp.getSchedulerId();
                QueuedSBScheduler scheduler = (QueuedSBScheduler)allSchedulers.get(id);
                manager.forgetSchedulerForArray(scheduler.getArrayName());
                allSchedulers.remove(id);
                scheduler = null;
                containerServices.releaseComponent(comp.name());
                queuedComps.removeElementAt(i);
            }
        }
    }
        
    public void stopDynamicScheduler(String name){
        String id=null;
        logger.fine("Size of dynamicComps = "+dynamicComps.size());
        for (int i=0; i < dynamicComps.size(); i++){
            Dynamic_Operator_to_Scheduling comp = (Dynamic_Operator_to_Scheduling)dynamicComps.elementAt(i);
            if(comp.name().equals(name)) {
                logger.fine("SCHEDULING: Stopping dynamic scheduler "+comp.name());
                id= comp.getSchedulerId();
                DynamicScheduler scheduler = (DynamicScheduler)allSchedulers.get(id);
                manager.forgetSchedulerForArray(scheduler.getArrayName());
                allSchedulers.remove(id);
                scheduler = null;
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
        logger.fine("SCHEDULING: Stop scheduling called. Will Stop activitiy on ALL schedulers");
        try {
            //stop and release all interactive components
            for(int i=0; i< interactiveComps.size(); i++){
                Interactive_PI_to_Scheduling comp = (Interactive_PI_to_Scheduling)interactiveComps.elementAt(i);
                logger.fine("SCHEDULING: Stopping component "+comp.name());
                containerServices.releaseComponent(comp.name());
            }
            //stop and release all queued components
            //stop and release all dynamic components
            for(int i=0; i< dynamicComps.size(); i++){
                Dynamic_Operator_to_Scheduling comp = (Dynamic_Operator_to_Scheduling )dynamicComps.elementAt(i);
                logger.fine("SCHEDULING: Stopping component "+comp.name());
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

    public SBLite[] getSBLite(String sbMode,
                              String sbType){
        return manager.getSBLites(sbMode, sbType);
    }

    public SBLite[] getSBLite(String[] ids){
        return manager.getSBLite(ids);
    }
    
    public SBLite[] getExistingSBLite(String[] ids){
        return manager.getExistingSBLite(ids);
    }
    
    public ProjectLite[] getFilteredProjectLites(String projectName,
                                                 String piName, 
                                                 String projectType,
                                                 String arrayType)
    	throws InvalidOperationEx {
    	ProjectLite[] projects = new ProjectLite[0];
        try {
			projects = manager.getProjectLites(projectName, piName, projectType, arrayType);
		} catch (SchedulingException ex) {
			AcsJInvalidOperationEx ex2 = new AcsJInvalidOperationEx();
			ex2.setProperty("Details", ex.getMessage());
			throw ex2.toInvalidOperationEx();
		}
		return projects;
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
            logger.fine("SchedulerInfo => id="+info.schedulerId+"; type="+
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
            foo = control.getAllAutomaticArrays();
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
            foo = control.getAllManualArrays();
        } catch(Exception e){
            e.printStackTrace();
            logger.severe("SCHEDULING: Error returning active manual arrays");
            foo[0] = "Problem with method";
        }
        return foo;
    }

    /*
    public String[] getAllActiveArrays() {
        try {
            String[] allArrays = control.getActiveArray();
            return allArrays;
        }catch(Exception e){
            return new String[0];
        }
    }*/
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
            //if(logger == null) {
            //    System.out.println("SCHEDULING: logger is null!");
            //}
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
    public String createArray(String[] antennaIdList, String[] phothnicsChoice,ArrayModeEnum schedulingMode)
        throws InvalidOperationEx {
            
        Subarray a =null;
        String mode="n/a";
        String name="";
        String logMsg="";
        try {             
            if(schedulingMode == ArrayModeEnum.MANUAL) {
                logMsg = "SCHEDULING: Creating an array for manual mode with antennas; [";
                for(int i=0; i< antennaIdList.length; i++){
                    logMsg =logMsg + antennaIdList[i] +", ";
                }
                logMsg = logMsg + "]";
                name = control.createManualArray(antennaIdList,phothnicsChoice);
                logger.logToAudience(Level.INFO, "create manual array with name:"+name, OPERATOR.value);
                //a = new Subarray(name, antennaIdList);
                //a.setSchedulingMode("manual");
            } else if(schedulingMode == ArrayModeEnum.DYNAMIC){
                logMsg = "SCHEDULING: Creating an array for dynamic mode with antennas; [";
                for(int i=0; i< antennaIdList.length; i++){
                    logMsg = logMsg + antennaIdList[i] +", ";
                }
                logMsg = logMsg + "]";
                logger.logToAudience(Level.INFO, logMsg, OPERATOR.value);
                name = control.createArray(antennaIdList, phothnicsChoice,"dynamic");
                //a = new Subarray(name, antennaIdList);
                //a.setSchedulingMode("dynamic");
            } else if(schedulingMode == ArrayModeEnum.QUEUED){
                logMsg = "SCHEDULING: Creating an array for queued mode with antennas; [";
                for(int i=0; i< antennaIdList.length; i++){
                    logMsg = logMsg + antennaIdList[i] +", ";
                }
                logMsg = logMsg + "]";
                logger.logToAudience(Level.INFO, logMsg, OPERATOR.value);
                name = control.createArray(antennaIdList, phothnicsChoice,"queued");
                //a = new Subarray(name, antennaIdList);
                //a.setSchedulingMode("queued");
            } else if(schedulingMode == ArrayModeEnum.INTERACTIVE){
                logMsg = "SCHEDULING: Creating an array for interactive mode with antennas; [";
                for(int i=0; i< antennaIdList.length; i++){
                    logMsg = logMsg + antennaIdList[i] +", ";
                }
                logMsg = logMsg + "]";
                logger.logToAudience(Level.INFO, logMsg, OPERATOR.value);
                name = control.createArray(antennaIdList, phothnicsChoice,"interactive");
                //a = new Subarray(name, antennaIdList);
                //a.setSchedulingMode("interactive");
            }
            //telescope.addSubarray(a);
        } catch(SchedulingException e) {
            e.printStackTrace();
            logger.logToAudience(Level.WARNING,
                    "SCHEDULING: Error creating array. First check Control System logs.",
                    OPERATOR.value);
            AcsJInvalidOperationEx e1 = new AcsJInvalidOperationEx(e);
            throw e1.toInvalidOperationEx();
        }
        if(!logMsg.equals("")){
            logger.logToAudience(Level.INFO, logMsg, OPERATOR.value);
        } else {
            logger.logToAudience(Level.WARNING,
                    "SCHEDULING: Problem occured in createArray Method",
                    OPERATOR.value);
        }
        schedModeForArray.put(name, schedulingMode);
        
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
        logger.fine("SCHEDULING: array "+name+" is now in use");
        arraysInUse.add(name);
    }


    /**
      * @param short
      * @throws InvalidOperation
      */
    public void destroyArray(String name) throws InvalidOperationEx {
        try {
            logger.logToAudience(Level.INFO, 
                    "SCHEDULING: Destroying array "+name, OPERATOR.value);
            //arraylogger.log(Level.INFO, 
              //      "SCHEDULING: Destroying array "+name, 
                //    OPERATOR.value, name);
            control.destroyArray(name);
            for(int i=0; i < arraysInUse.size(); i++){
                if(arraysInUse.elementAt(i).equals(name)){
                    arraysInUse.removeElementAt(i);
                    break;
                }
            }
            schedModeForArray.remove(name);
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
        logger.fine("SCHEDULING: Pause Scheduling not implemented yet.");
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



    private Scheduler getScheduler(String id) {
        return allSchedulers.get(id);
    }
    

    /**
      * Returns the uids of the projects that match the given search criteria
      */
    public String[] queryForProject(String projname, String piname, String type, 
            String aType, boolean manualMode) throws InvalidOperationEx {

        String[] results = new String[0];    
        String schema = new String("ObsProject");
        String foo1, foo2, foo3;
        String query =  new String("/prj:ObsProject");
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
        
        if(manualMode){
        	foo3 = new String("prj:manualMode=\"true\"");
        }
        else {
        	foo3 = new String("prj:manualMode=\"false\"");
        }
        query = query + "["+foo1+" and "+foo2+" and " + foo3 + "]";
        boolean hasStart= false;
        boolean needEndBracket = false;
        if(!type.equals("All")){
            query = query + 
                "/prj:ObsProgram/prj:ObsPlan[prj:ObsUnitSet[prj:DataProcessingParameters[@projectType=\""+type+"\"]]";
            needEndBracket = true;
            hasStart = true;
        }
        if(!aType.equals("All")){
            if(hasStart){
                query = query + 
                    "[prj:ObsUnitControl[@arrayRequested=\""+aType+"\"]]";
            } else {
                query = query + 
                    "/prj:ObsProgram/prj:ObsPlan[prj:ObsUnitSet[prj:ObsUnitControl[@arrayRequested=\""+aType+"\"]]";
                needEndBracket = true;
            }
        }
        if(needEndBracket){
            query = query + "]";
        }
        logger.fine("Scheduling Query = "+ query);                
        try {
            System.out.println("ProjectQuery: "+query);
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
    
    /**
     * Returns the uids of the projects that match the given search criteria
     */
   public String[] queryForAllProject(String projname, String piname, String type, 
           String aType) throws InvalidOperationEx {

       String[] results = new String[0];    
       String schema = new String("ObsProject");
       String foo1, foo2, foo3;
       String query =  new String("/prj:ObsProject");
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
       
       query = query + "["+foo1+" and "+foo2+"]";
       boolean hasStart= false;
       boolean needEndBracket = false;
       if(!type.equals("All")){
           query = query + 
               "/prj:ObsProgram/prj:ObsPlan[prj:ObsUnitSet[prj:DataProcessingParameters[@projectType=\""+type+"\"]]";
           needEndBracket = true;
           hasStart = true;
       }
       if(!aType.equals("All")){
           if(hasStart){
               query = query + 
                   "[prj:ObsUnitControl[@arrayRequested=\""+aType+"\"]]";
           } else {
               query = query + 
                   "/prj:ObsProgram/prj:ObsPlan[prj:ObsUnitSet[prj:ObsUnitControl[@arrayRequested=\""+aType+"\"]]";
               needEndBracket = true;
           }
       }
       if(needEndBracket){
           query = query + "]";
       }
       logger.fine("Scheduling Query = "+ query);                
       try {
           System.out.println("ProjectQuery: "+query);
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
            //System.out.println("Query: "+query);
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

            logger.fine("MS: adding sb to queued scheduler");
            SB sb = sbQueue.get(sbid);
            Scheduler scheduler = getScheduler(schedulerId);
            ((QueuedSBScheduler)scheduler).addSB(sb);
    }
    
    public void removeQueuedSBs(String[] sbid, int[] i, String schedulerId)
        throws InvalidOperationEx, NoSuchSBEx {
            logger.fine("MS: removing sbs from queued scheduler");
            SB sb;
            Scheduler scheduler = getScheduler(schedulerId);
            for(int x=0; x < sbid.length; x++){
                sb= sbQueue.get(sbid[x]);
                ((QueuedSBScheduler)scheduler).removeSbAt(sb, i[x]);
            }
    }
    
    public void stopQueuedSB(String sbid, String schedulerId) 
        throws InvalidOperationEx, NoSuchSBEx {

        logger.fine("Stop queued SB in MS called");
        Scheduler scheduler = getScheduler(schedulerId);
        checkSchedulerType(scheduler.getType(), "queued");
        try{
            ((QueuedSBScheduler)scheduler).stop(sbid);
        } catch(SchedulingException e){
            logger.severe("SCHEDULING: Error stopping queued SB");
            e.printStackTrace();
        }
    }

    public void stopQueue(String schedulerId) throws InvalidOperationEx, NoSuchSBEx {
        logger.fine("Stop queue in MS called");
        Scheduler scheduler = getScheduler(schedulerId);
        checkSchedulerType(scheduler.getType(), "queued");
        try{
            ((QueuedSBScheduler)scheduler).stopQueue();
        } catch(SchedulingException e){
            logger.severe("SCHEDULING: Error stopping queue");
            e.printStackTrace();
        }
    }

    public void abortQueuedSB(String schedulerId) throws InvalidOperationEx, NoSuchSBEx {
        logger.fine("Abort queued SB in MS called");
        Scheduler scheduler = getScheduler(schedulerId);
        checkSchedulerType(scheduler.getType(), "queued");
        try{
            ((QueuedSBScheduler)scheduler).abortSB();
        } catch(SchedulingException e){
            logger.severe("SCHEDULING: Error stopping queued SB");
            e.printStackTrace();
        }
    }
    
    public void abortQueue(String schedulerId) throws InvalidOperationEx, NoSuchSBEx {
        logger.fine("Abort queue in MS called");
        Scheduler scheduler = getScheduler(schedulerId);
        checkSchedulerType(scheduler.getType(), "queued");
        try{
            ((QueuedSBScheduler)scheduler).abortQueue();
        } catch(SchedulingException e){
            logger.severe("SCHEDULING: Error stopping queue");
            e.printStackTrace();
        }
    }

    // Interactive_Scheduler_to_MasterScheduler
    public void executeInteractiveSB(String sbId, String schedulerId) 
        throws InvalidOperationEx, NoSuchSBEx, CannotRunCompleteSBEx {

        Scheduler scheduler = getScheduler(schedulerId);
        String type = scheduler.getType();
        if(!type.equals("interactive")){
            InvalidOperation e1 = new InvalidOperation("executeInteractiveSB",
                   "Wrong scheduler type: "+type);
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
        try {
            logger.fine("Looking for SB ("+sbId+") in queue... "+sbQueue.get(sbId)+" == is it there?");
            ((InteractiveScheduler)scheduler).execute(sbQueue.get(sbId));
        } catch(SchedulingException e) {
            if(e.getMessage().equals("SB has reached its maximum execution count.") ){
                AcsJCannotRunCompleteSBEx e1 = new AcsJCannotRunCompleteSBEx(e);
                throw e1.toCannotRunCompleteSBEx();
            } else {
                AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e);
                throw e2.toInvalidOperationEx();
            }
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

    public void stopInteractiveSBNow(String schedulerId) 
        throws InvalidOperationEx, NoSuchSBEx {
            
        Scheduler scheduler = getScheduler(schedulerId);
        checkSchedulerType(scheduler.getType(), "interactive");
        try {
            ((InteractiveScheduler)scheduler).stopNow();
        } catch(Exception e) {
            InvalidOperation e1 = new InvalidOperation("stopInteractiveSBNow",
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

    ///// Method for manual mode
    public IDLEntityRef startManualModeSession(String arrayName,String sbid) throws InvalidOperationEx {
        try {
            return manager.startManualModeSession(arrayName,sbid);
        } catch(SchedulingException e) {
            e.printStackTrace();
            InvalidOperation e1 = new InvalidOperation("startManualModeSession",
                   e.toString());
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
        
    }
    
    public void setManualArrayConfigure(String arrayName,String sbid){
    	try {
    	control.setManualModeConfigure(arrayName,sbid);
    	}
    	catch (SchedulingException e) {
    		e.printStackTrace();
    	}
    }
    
    ///// Methods for Commissioning_Scheduler_to_MasterScheduler Interface /////////
    public void executeCommissioningSB(String sbid, String schedulerid) 
        throws InvalidOperationEx, NoSuchSBEx, CannotRunCompleteSBEx {
    }

    public void addCommissioningSBToQueue(String sbid, String schedulerId) 
        throws InvalidOperationEx, NoSuchSBEx {
    }            
     
    public void removeCommissioningQueuedSBs(String[] sbid, int[] i, String schedulerId)
        throws InvalidOperationEx, NoSuchSBEx {
    }
    public void stopCommissioningSB(String schedulerid) throws InvalidOperationEx, NoSuchSBEx {
    }

    public void stopCommissioningSBNow(String schedulerid) 
        throws InvalidOperationEx, NoSuchSBEx {
    }
    public void stopCommissioningQueuedSB(String sbid, String schedulerId) 
        throws InvalidOperationEx, NoSuchSBEx {
    }

    /**
     * Scheduling can run in two modes: FullAuto mode, where if a SchedBlock has
     * repeat count > 1 transition immediately to the Ready state when it finishes the
     * execution; and SemiAuto mode, where it transition to Suspended. This method
     * sets the run mode in a Scheduler.
     * 
     * @param fullAutoRunMode
     *      If true, the run mode is set to FullAuto mode, if false it
     * 		is set to SemiAuto mode.
     * @param schedulerId Scheduler identifier
     */
    public void setFullAutoRunMode(boolean fullAutoRunMode, String schedulerId) {
    	logger.info("Setting run mode for Scheduler " + schedulerId + ": " + fullAutoRunMode);
    	Scheduler scheduler = getScheduler(schedulerId);
    	if (fullAutoRunMode)
        	scheduler.setRunMode(RunMode.FullAuto);
    	else
        	scheduler.setRunMode(RunMode.SemiAuto);
    }
    
    ////////////// Methods to set/get scheduler modes for a given array ///////////

    public ArrayModeEnum getSchedulerModeForArray(String arrayname) 
        throws InvalidOperationEx {
        // TODO check to see if array exists, if not throw exception
    	logger.info("ALMAMasteScheduler.getSchedulerModeForArray.arrayname:"+arrayname);
    	logger.info("array has map is "+schedModeForArray.size()+" array");
    	Set keys = schedModeForArray.keySet();
    	Iterator keyIter = keys.iterator();
    	while(keyIter.hasNext()) {
    		String keyName = (String)keyIter.next();
    		ArrayModeEnum arrayMode = schedModeForArray.get(keyName);
    		logger.info("arrayname:"+keyName+ "for mode "+arrayMode.toString());
    	}
    	
        return (ArrayModeEnum)schedModeForArray.get(arrayname);
    }
    ////////////////////////////////////////////////////////////////

    private void checkSchedulerType(String type, String shouldbe) throws InvalidOperationEx {
        logger.finest("MS: scheduler is of type "+type+" and should be "+shouldbe);
        if(!type.equals(shouldbe)){
            InvalidOperation e1 = new InvalidOperation("CheckSchedulerType",
                   "Wrong scheduler type: "+type);
            AcsJInvalidOperationEx e2 = new AcsJInvalidOperationEx(e1);
            throw e2.toInvalidOperationEx();
        }
    }
}        
