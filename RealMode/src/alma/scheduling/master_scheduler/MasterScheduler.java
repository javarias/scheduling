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
 * File MasterScheduler.java
 * 
 */
package alma.scheduling.master_scheduler;

import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.DomainManager;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.Request;
import org.omg.CORBA.SetOverrideType;

import alma.acs.component.ComponentLifecycle;
import alma.acs.container.ContainerServices;
import alma.acs.container.ContainerException;
import alma.xmlentity.XmlEntityStruct;
import alma.entities.commonentity.EntityT;
import alma.entity.xmlbinding.schedblock.*;
import alma.entity.xmlbinding.obsproject.*;
//import alma.bo.SchedBlock;

import alma.scheduling.*;
import alma.scheduling.project_manager.ProjectManager;
import alma.scheduling.project_manager.ProjectManagerTaskControl;
import alma.scheduling.project_manager.PIProxy;
import alma.scheduling.scheduler.*;
import alma.scheduling.receivers.SchedulerEventReceiver;

import java.io.File;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Vector;
import java.net.InetAddress;
import java.net.UnknownHostException;

import alma.acs.nc.SimpleSupplier;
import alma.acs.component.client.ComponentClient;
//import alma.scheduling.MSOperations;
//import alma.scheduling.MS;


/**
 * The MasterScheduler class is the major controlling class in the Scheduling
 * Subsystem.  See Scheduling Subsystem Design document, section 3.2.1.
 * 
 * @version 1.00 May 1, 2003
 * @author Allen Farris
 */

public class MasterScheduler implements MS, ComponentLifecycle, Runnable {
	
	/** The scheduling subsystem's component name.  */
	private String componentName;
	/** The object that provides container services.  */
	private ContainerServices container;
	/** The state of the scheduling subsystem.  */
	private State schedulingState;
	/**
	 * A flag that is shared with threads to indicate that a command
	 * to stop has been received.
	 */
	private boolean stopCommand;
	/** 
	 * The operational mode of the scheduling subsystem.  It is either
	 * in simulation mode or in real mode.
	 */
	private boolean isSimulation;
	
	/** The object that Scheduling uses to communicate with the Archive */
	private ALMAArchive archive;
	/** Impl if TelescopeOperator */
	private ALMATelescopeOperator operator;
	/** PIProxy object */
	private PIProxy pi;
	/** The object scheduling uses to communicate with Control system */
	private ALMADispatcher dispatcher;
	/** The Clock */
	private ALMAClock clock;
	/** The object which holds all the SchedBlocks/SUnits */
	private MasterSBQueue sbQueue;
	/** The object that holds all the messages */
    private MessageQueue messageQueue;
    /** The ProjectManager object */
	private ProjectManager projectManager;
    /** A List of all the scheduling policies */
	private ArrayList schedulingPolicy;
    /** A List of all the antennas (active? all? available?) */
	private ArrayList antenna;
    /** A List of all the schedulers that are created */
	private ArrayList scheduler;
    /** The MasterScheduler periodic action object */
	private MasterSchedulerAction action;
    /** The thread for the MasterSchedulerAction */
    private Thread actionThread;
    /** The master scheduler's thread */
    private Thread msThread;
    /** MasterSchedulers TaskControlInfo */
    private TaskControlInfo msControlInfo;
    /** The logger */
    private Logger logger;
    /** Time the MS thread sleeps */
    private int msSleepTime = 300000;//5 minute sleep
    
	
	private void setNullReferences() {
		archive = null;
		operator = null;
		pi = null;
		dispatcher = null;
		clock = null;
		sbQueue = null;
        messageQueue = null;
		projectManager = null;
		schedulingPolicy = new ArrayList ();
		antenna = new ArrayList ();
		scheduler = new ArrayList();
		action = null;
	}
	
	/**
	 * Create a MasterScheduler to run in the "real" mode.
	 */
	public MasterScheduler() {
		this.schedulingState = new State(State.NEW);
		this.stopCommand = false;
		this.isSimulation = false;
		this.componentName = null;
		this.container = null;
        
		setNullReferences();
		System.out.println("SCHEDULING: The MasterScheduler has been constructed.");
	}

	/**
	 * Create a MasterScheduler to run in either the "real" or
	 * "simulation" mode.
	 * @param isSimulation If true, the system is set to run in the 
	 * simulation mode; otherwise it is set to run in the real mode.
	 */
    public MasterScheduler(boolean isSimulation) {
        this.schedulingState = new State(State.NEW);
        this.stopCommand = false;
        this.isSimulation = isSimulation;
        this.componentName = null;
        this.container = null;
        setNullReferences();
        System.out.println("SCHEDULING: The MasterScheduler has been constructed.");
	}

	/**
	 * Set this component's name -- done by the container.
	 * @see alma.acs.component.ComponentLifecycle#setComponentName(String)
	 */
	public void setComponentName(String name) {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException (
			"Component name cannot be a null string.");
		if (componentName != null)
			throw new UnsupportedOperationException (
			"Cannot change the name of a component that has already been named.");
		this.componentName = name;
	}

	/**
	 * Set the object that provides container services -- done by the container.
	 * @see alma.acs.component.ComponentLifecycle#setContainerServices(ContainerServices)
	 */
	public void setContainerServices(ContainerServices containerServices) {
		if (containerServices == null)
			throw new IllegalArgumentException (
			"ContainerServices object cannot be a null.");
		this.container = containerServices;
	}

	/**
	 * The initialize method completely initializes the scheduling subsystem.
	 * It cannot be called if the scheduling subsystem is in the "executing"
	 * state.  In addition the component name and container services must
	 * be provided before initialization can occur.  If any error occurs 
	 * during initialization, the scheduling subsystem is placed in the "error"
	 * state and an exception is thrown.  After initialization this object is
	 * not active; nothing begins executing until the execute method is called.
	 * 
	 * <p>
	 * The initialize method performs the following activities.
	 * <ul>
	 * <li> Creates all proxy objects, either in real or simulation mode.  These
	 * 		include the archive, pipeline, telescope operator, and PI proxies.
	 * <li> Creates the dispatcher and clock, either in real or simulation mode.
	 * <li> Reads the list of scheduling policies from the archive.
	 * <li> Gets the list of commissioned antennas from the archive.
	 * <li> Creates the Project Manager object.
	 * <li> Creates an empty queue of SchedBlocks.
	 * <li> Creates an empty queue of ObsProjects.
	 * <li> Create an empty list of Schedulers.
	 * <li> Create a periodic action object.
	 * <li> Set state to "initialized".
	 * </ul>
	 * 
	 * @see alma.acs.component.ComponentLifecycle#initialize()
	 */
	public void initialize() {
		if (componentName == null)
			throw new UnsupportedOperationException (
			"The component must be named before this object can be initialized.");
		if (container == null)
			throw new UnsupportedOperationException (
			"The ContainerServices must be set before this object can be initialized.");
		if (schedulingState.equals(State.EXECUTING))
			throw new UnsupportedOperationException (
			"Cannot initialize this object.  It is already executing!  It must be stopped first.");
		// This test allows an object to be initialized, or re-initialized if it is in a new, 
		// initialized, stopped, or error state.

		// If any error occurs during initialization, the component state is set to "error"
		// and an exception is thrown.
		
		try {
            //get logger from container
            logger = container.getLogger();
			// Create the Master SB queue.
			sbQueue = new MasterSBQueue ();
            // Create the Message queue
            messageQueue = new MessageQueue();
			// Create the archive proxy.
			archive = new ALMAArchive(isSimulation,container);			
            //logger.log(Level.FINE, "Got archive in MS");
            
			// Create the telescope operator proxy.
			operator = new ALMATelescopeOperator(isSimulation,container,archive);
            //logger.log(Level.FINE, "Got operator in MS");
			
			// Create the PI proxy.
			pi = new PIProxy(isSimulation,container);
            //logger.log(Level.FINE, "Got PIProxy in MS");
            
			// Create a periodic action object.
			action = new MasterSchedulerAction (archive, sbQueue);

			// Create the ALMADispatcher.
			dispatcher = new ALMADispatcher(isSimulation,container,archive);
            //logger.log(Level.FINE, "Got alma dispatcher in MS");

			// Create the Project Manager.
            projectManager = new ProjectManager(isSimulation, container, 
                                                 archive, sbQueue, dispatcher);
			
			// Create the ALMAClock.
			clock = new ALMAClock(isSimulation,container);
            //logger.log(Level.FINE, "Got alma clock in MS");
			
			// Get the list of scheduling policies from the archive.
			schedulingPolicy = new ArrayList ();
            //logger.log(Level.FINE, "Scheduling policy created in MS");
            //schedulingPolicy = archive.getSchedPolicy();
			
			// Get the list of commissioned antennas from the archive.
			antenna = new ArrayList ();
			// ...
            //antenna = archive.getAntennas();
			
			// Create an empty list of schedulers.
			scheduler = new ArrayList();

            // TODO Initialize Notification Channel
            
		} catch (Exception ex) {
			schedulingState.setState(State.ERROR);
			throw new UnsupportedOperationException(
			"Scheduling subsystem initialization failure! " + ex.toString());
		}
		
		// OK, we're done.
		schedulingState.setState(State.INITIALIZED);
		logger.info("SCHEDULING: The MasterScheduler has been initialized.");
	}

	/**
	 * The execute method begins the process of executing the scheduling subsystem.
	 * If any error occurs during execution, the scheduling subsystem is placed in 
	 * the "error" state and an exception is thrown. The execute method starts the
	 * project manager; however, it does not start the scheduling activity.  That
	 * must be explicitly done via the "startScheduling" method.
	 * 
	 * <p>
	 * The execute method performs the following activities.
	 * 
	 * <ul>
	 * <li> Get the full set of SBs from the archive that have not been completed
	 * 		and place them in the master SB queue.
	 * <li> Form a unique list of SB-ids from the master SB queue and give those to
	 * 		Project Manager to be added to its list of projects.
	 * <li>	Get the antenna state from the control system.
	 * <li> Get the state model from the control system.
	 * <li> Get the necessary notification channels.
	 * <li> Set up the notification channel receivers for the Project Manager.
	 * <li> Set up the proper listeners on the notification channels.
	 * <li> Start the Project Manager thread.
	 * <li> Start the perodic action object.
	 * </ul>
	 * 
	 * @see alma.acs.component.ComponentLifecycle#execute()
	 */
	public void execute() {
		// Set the state to executing.
		schedulingState.setState(State.EXECUTING);
        logger.info("SCHEDULING: starting execute");

        pollArchive();
        /*
        // get non-complete SBs from archive, right now it gets ALL sbs.
        SchedBlock[] sbs = archive.getSchedBlock();
        logger.info("SCHEDULING: Getting SBs from archive");

        // place SBs in master SB queue
        if(sbs != null) {
            logger.info("SCHEDULING: sbs not null storing into sbQueue");
            //queue.addNonCompleteSBsToQueue(sbs);   
            sbQueue.addSchedBlock(sbs);
        } else {
            logger.info("SCHEDULING: sbs null will result in error?");
        }
        */

        // form unique list of sb ids from queue
        //Vector uid = queue.getAllUid();
        // give list to project manager - adds them to list of projects 
        //projectManager.addSBUids(uid);

        // Get all project definitions out of the archive
        //ObsProject[] proj = archive.getProject();
        

        // TODO get antenna state from control
        // TODO get state model from control
        // TODO get notification channels
        // TODO setup notification channel recievers for PM - consumer
        // TODO setup notification channel listeners - 

        // Start perodic action obj
        actionThread = new Thread(action);
        actionThread.start();
        action.setTaskInfoThread(actionThread);
        // Create MasterScheduler Thread and start it.
        msThread = new Thread(this);
        msThread.start();
        // Create MasterScheduler TaskControlInfo
        msControlInfo = new TaskControlInfo(msThread,actionThread);
        // Set task control in action
        action.setTaskControlInfo(msControlInfo);
        // Start PM thread
        Thread pmThread = new Thread(projectManager);
        // Create ProjectManagerTaskControl
        ProjectManagerTaskControl pmtc = new ProjectManagerTaskControl(msThread, pmThread);
        projectManager.setProjectManagerTaskControl(pmtc);
        pmThread.start();
        dispatcher.setProjectManagerTaskControl(pmtc);
        
        operator.setMessageQueue(messageQueue);
        
		logger.info("SCHEDULING: The MasterScheduler is executing.");
	}

	/**
	 * The cleanUp method tells all threads in the subsystem to stop.
	 * It then waits for all threads to stop, sets all object 
     * references to null and sets the state to "stopped". 
	 * 
	 * @see alma.acs.component.ComponentLifecycle#cleanUp()
	 */
	public void cleanUp() {
		if (schedulingState.equals(State.STOPPED)) {
			return;
		}
		if (schedulingState.equals(State.NEW)) {
			schedulingState.setState(State.STOPPED);
			return;
		}
		if (schedulingState.equals(State.INITIALIZED)) {
			// Set all object references to null, except component name and container.
			// ...
			schedulingState.setState(State.STOPPED);
			return;
		}
		// This leaves the ERROR and EXECUTING states.
		
		// Set stopCommand to true.
		stopCommand = true;
		
		// If there is a periodic action thread, tell it to stop.
        if(action != null) {
            action.stop();
        }

        // Disconnect from other subsystem's notification channels
        /*
        try {
            dispatcher.disconnectFromControl();
            projectManager.disconnectFromPipeline();
        } catch(Exception e) {}
        */
		
		// If there is a Project Manager thread, tell it to stop.
        if(projectManager != null) {
            projectManager.stop();
        }
		
		// If there are any active Schedulers, tell them to stop.
        // i don't really like what i've done below..
        // this is also done in stopScheduling
        try {
            stopScheduling();
        } catch (InvalidOperation e) {
        }
        /*
        int size = scheduler.size();
        Scheduler[] tmp = new Scheduler[size];
        for(int i =0; i < size; i++) {
            tmp[i] = (Scheduler) scheduler.get(i);
            tmp[i].stop();
        }
        */
		
		// if there are active MasterScheduler notification channel listeners, deactivate them.
		

		// Those were the important steps.  If aboutToAbort() gets this far, we're probably OK.
		schedulingState.setState(State.STOPPED);

		// Wait for all threads to stop.
		
		// Set all object references to null.
		setNullReferences();
		
		// We're done.				
		logger.info("SCHEDULING: The MasterScheduler has been cleaned up and is now stopped.");
	
	}

	/**
	 * The aboutToAbort method merely calls the cleanUp method. The various 
	 * actions in the cleanUp method are arranged in order of importance. 
	 * If the execution gets to the point of waiting for all threads to stop, 
	 * the system will be in a relatively safe state and it should be ready 
	 * to resume operations without manual inteventation. 
	 * 
	 * @see alma.acs.component.ComponentLifecycle#aboutToAbort()
	 */
	public void aboutToAbort() {
		logger.info("SCHEDULING: The MasterScheduler has been requested to abort.");
		cleanUp();
	}

    /**
     * All the functions that deal with checking the archive periodically.
     */
    private void pollArchive() {
        // get non-complete SBs from archive, right now it gets ALL sbs.
        SchedBlock[] sbs = archive.getSchedBlock();
        logger.info("SCHEDULING: Getting SBs from archive");

        // place SBs in master SB queue
        if(sbs != null) {
            logger.info("SCHEDULING: sbs not null storing into sbQueue");
            //queue.addNonCompleteSBsToQueue(sbs);   
            sbQueue.addSchedBlock(sbs);
        } else {
            logger.info("SCHEDULING: sbs null will result in error?");
        }
    }

    /**
     * MasterScheduler's run method.
     */
    public void run() {
        while(!stopCommand) {
            try {
                pollArchive();
                logger.info("MSsleeping!");
                Thread.sleep(msSleepTime); 
                logger.log(Level.INFO,"SCHEDULING: MS Thread woken up.");
            } catch(InterruptedException e){
                logger.log(Level.INFO,"SCHEDULING: MS Thread interrupted.");
            }
        }    
    }

	/**
	 * The startScheduling method is called by the Executive subsystem in order to start
	 * the scheduling activity.  It must supply the scheduling policy to be used by the
	 * scheduler.
	 * 
	 * @param schedulingPolicy The scheduling policy to be used by the scheduler.
	 * @see alma.scheduling.Executive_to_SchedulingOperations#startScheduling(XmlEntityStruct)
	 */
	public void startScheduling(XmlEntityStruct schedulingPolicy)
		throws InvalidOperation {

        startScheduler("dynamic");
        //get scheduling policy from schedulingPolicy arraylist then set it to start scheduling
	}

	/**
	 * The stopScheduling method is called by the Executive subsystem in order to stop
	 * the scheduling activity.  
	 * 
	 * @see alma.scheduling.Executive_to_SchedulingOperations#stopScheduling()
	 */
	public void stopScheduling() throws InvalidOperation {
        for(int i=0; i < scheduler.size(); i++) {
            if( !((Scheduler)scheduler.get(i)).getSchedulerState().getState().equals("stopped")) {
            
                ((Scheduler)scheduler.get(i)).stop();
                logger.info("SCHEDULING: Scheduler stopped.");
            } else {
                logger.info("SCHEDULING: Scheduler already stopped.");
            }
        }
	}

	/**
	 * The getStatus method is called by the Executive subsystem in order to 
	 * determine if the scheduling subsystem is alive and functioning properly.
	 * It returns true if the system is in the "executing" state.
	 * 
	 * @see alma.scheduling.Executive_to_SchedulingOperations#getStatus()
	 */
	public boolean getStatus() {
        logger.info("SCHEDULING: checking to see if scheduling status is executing!");
		return schedulingState.equals(State.EXECUTING);
	}

	/**
	 * The getSchedulingInfo method returns detailed information about the current
	 * state of the scheduling subsystem.
	 * 
	 * @see alma.scheduling.Executive_to_SchedulingOperations#getSchedulingInfo()
	 */
	public XmlEntityStruct getSchedulingInfo() {
		return null;
	}
	
	/**
	 * @see alma.scheduling.TelescopeOperator_to_SchedulingOperations#response(String, String)
	 */
	public void response(String messageId, String reply) throws UnidentifiedResponse 
    {
        if(logger == null) {
            System.out.println("SCHEDULING: logger is null!");
        }
        logger.log(Level.INFO,"SCHEDULING: in MS. MessageID = "+messageId);
        logger.log(Level.INFO,"SCHEDULING: in MS. Reply (sb id) = "+reply);
        logger.log(Level.INFO,"SCHEDULING: in MS. messageQueue size = "+messageQueue.size());
       
       if(messageQueue.size() < 1) {
            logger.log(Level.INFO,"SCHEDULING: in MS. MessageQueue was empty. Try starting with startScheduling function!");
            return;
        } 
        
        Message item = messageQueue.getMessage(messageId);
        logger.log(Level.INFO,"SCHEDULING: in MS. Got message with id="+item.getMessageId());
        item.setReply(reply);
        logger.log(Level.INFO, "SCHEDULING: in MS. message = "+ messageId + 
            " gotten and reply = "+ reply + " sent.");
        item.getThread().interrupt();
        //messageQueue.removeMessage(messageId);
	}


	/**
	 * @see alma.scheduling.TelescopeOperator_to_SchedulingOperations#getSubarrayInfo()
	 */
	public XmlEntityStruct getSubarrayInfo() {
		return null;
	}

	/**
	 * @see alma.scheduling.TelescopeOperator_to_SchedulingOperations#createSubarray(short[], String)
	 */
	public short createSubarray(short[] antennaIdList, String schedulingMode)
		throws InvalidOperation {
		return 0;
	}

	/**
	 * @see alma.scheduling.TelescopeOperator_to_SchedulingOperations#destroySubarray(short)
	 */
	public void destroySubarray(short subarrayId) throws InvalidOperation {
	}

	/**
	 * @see alma.scheduling.TelescopeOperator_to_SchedulingOperations#executeProject(String, short)
	 */
	public void executeProject(String projectId, short subarrayId)
		throws InvalidOperation {
	}

	/**
	 * @see alma.scheduling.TelescopeOperator_to_SchedulingOperations#executeSB(String, short, String)
	 */
	public void executeSB(String sbId, short subarrayId, String when) {
	}

	/**
	 * @see alma.scheduling.TelescopeOperator_to_SchedulingOperations#stopSB(String)
	 */
	public void stopSB(String sbId) throws InvalidOperation, NoSuchSB {
	}

	/**
	 * @see alma.scheduling.TelescopeOperator_to_SchedulingOperations#pauseScheduling(short)
	 */
	public void pauseScheduling(short subarrayId) throws InvalidOperation {
	}

	/**
	 * @see alma.scheduling.TelescopeOperator_to_SchedulingOperations#resumeScheduling(short)
	 */
	public void resumeScheduling(short subarrayId) throws InvalidOperation {
	}

	/**
	 * @see alma.scheduling.TelescopeOperator_to_SchedulingOperations#manualMode(short)
	 */
	public void manualMode(short antennaId) throws InvalidOperation {
	}

	/**
	 * @see alma.scheduling.TelescopeOperator_to_SchedulingOperations#activeMode(short)
	 */
	public void activeMode(short antennaId) throws InvalidOperation {
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// NOTE:																				//
	// The follwing methods are required because a MasterScheduler object implements the	//
	// IDL interfaces Executive_to_Scheduling and TelescopeOperator_to_Scheduling.  The		//
	// IDL interfaces are extensions of the org.omg.CORBA.Object interface and these		//
	// methods are required by the org.omg.CORBA.Object interface.  Within the ALMA			//
	// context, it may not be necessary to really inplement them.  So, we will just leave	//
	// them as stubs until we have to really think about them.								//
	//////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @see org.omg.CORBA.Object#_is_a(String)
	 */
	public boolean _is_a(String arg0) {
		return false;
	}

	/**
	 * @see org.omg.CORBA.Object#_is_equivalent(Object)
	 */
	public boolean _is_equivalent(Object arg0) {
		return false;
	}

	/**
	 * @see org.omg.CORBA.Object#_non_existent()
	 */
	public boolean _non_existent() {
		return false;
	}

	/**
	 * @see org.omg.CORBA.Object#_hash(int)
	 */
	public int _hash(int arg0) {
		return 0;
	}

	/**
	 * @see org.omg.CORBA.Object#_duplicate()
	 */
	public Object _duplicate() {
		return null;
	}

	/**
	 * @see org.omg.CORBA.Object#_release()
	 */
	public void _release() {
	}

	/**
	 * @see org.omg.CORBA.Object#_get_interface_def()
	 */
	public Object _get_interface_def() {
		return null;
	}

	/**
	 * @see org.omg.CORBA.Object#_request(String)
	 */
	public Request _request(String arg0) {
		return null;
	}

	/**
	 * @see org.omg.CORBA.Object#_create_request(Context, String, NVList, NamedValue)
	 */
	public Request _create_request(
		Context arg0,
		String arg1,
		NVList arg2,
		NamedValue arg3) {
		return null;
	}

	/**
	 * @see org.omg.CORBA.Object#_create_request(Context, String, NVList, NamedValue, ExceptionList, ContextList)
	 */
	public Request _create_request(
		Context arg0,
		String arg1,
		NVList arg2,
		NamedValue arg3,
		ExceptionList arg4,
		ContextList arg5) {
		return null;
	}

	/**
	 * @see org.omg.CORBA.Object#_get_policy(int)
	 */
	public Policy _get_policy(int arg0) {
		return null;
	}

	/**
	 * @see org.omg.CORBA.Object#_get_domain_managers()
	 */
	public DomainManager[] _get_domain_managers() {
		return null;
	}

	/**
	 * @see org.omg.CORBA.Object#_set_policy_override(Policy[], SetOverrideType)
	 */
	public Object _set_policy_override(Policy[] arg0, SetOverrideType arg1) {
		return null;
	}


    /* Methods for testing, not sure if they'll be needed later */

    public ALMAArchive getArchive() {
        return archive;
    }
    public MessageQueue getMessageQueue() {
        return messageQueue;
    }
    public MasterSBQueue getSBQueue() {
        return sbQueue;
    }
    public ALMATelescopeOperator getOperator() {
        return operator;
    }
    public Scheduler getScheduler() {
        return (Scheduler)scheduler.get(0);
    }
    public ALMADispatcher getDispatcher() {
        return dispatcher;
    }
    
    

    /**
     *  Assigns a uid to the EntityT.
     *  @param et
     */
    public void assignId(EntityT et) {
        try {
            container.assignUniqueEntityId(et);
        } catch(Exception e) {
        }
    }
    
    /**
     *  Creates a scheduler in the mode submitted by the input parameter.
     *  @param mode Either dynamic or interactive
     */
    public void startScheduler(String mode) {
        if(!mode.equals("dynamic") && !mode.equals("interactive") ) {
            logger.severe("SCHEDULING: Scheduler not started. Invalid mode: "+mode);
            return;
        }
        Vector subSBQueue = sbQueue.queueToVector();
        // for now the subqueue is the queue
        SBSubQueue subQueue = new SBSubQueue(subSBQueue);
        Scheduler s = new Scheduler(isSimulation, container, operator, 
                                        dispatcher, subQueue, messageQueue, 
                                            clock, pi, mode); 
        logger.info("SCHEDULING: New scheduler started.");
        logger.info("SCHEDULING: Start of new project.");
        Thread schedThread = new Thread(s);
        SchedulerTaskControl stc = 
            new SchedulerTaskControl(msThread,schedThread);
        s.setSchedulerTaskControl(stc);
        schedThread.start();
        scheduler.add(s);
        // let scheduler listen to the control channel.
        try {
            SchedulerEventReceiver sched_listener = 
                new SchedulerEventReceiver(s);
            sched_listener.addSubscription(alma.Control.EXECEVENTS.value);
            sched_listener.consumerReady();
        } catch(Exception e) {
            logger.severe("SCHEDULING: Problem with scheduler event listener");
            logger.severe("SCHEDULING: "+e.toString());
        }
    }

	public static void main(String[] args) {
		MasterScheduler x = new MasterScheduler ();
		x.setComponentName("TestScheduler");
        
        try {
            ComponentClient c = new ComponentClient(
                Logger.getLogger("TestScheduler"),
                    "corbaloc::"+InetAddress.getLocalHost().getHostName()+":3000/Manager",
                        "TestScheduler");

		    x.setContainerServices(c.getContainerServices());
    		x.initialize();
	    	x.execute();
		    x.cleanUp();
        } catch(Exception e) {
            System.out.println("Exception! " + e.toString() );
        }
        
        System.exit(0); // 0 == normal termination
	}


}

